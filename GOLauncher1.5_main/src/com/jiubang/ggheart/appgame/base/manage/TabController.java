package com.jiubang.ggheart.appgame.base.manage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.gau.go.launcherex.R;
import com.gau.utils.net.IConnectListener;
import com.gau.utils.net.INetRecord;
import com.gau.utils.net.request.THttpRequest;
import com.gau.utils.net.response.IResponse;
import com.go.util.device.Machine;
import com.jiubang.ggheart.appgame.appcenter.component.AppsManagementActivity;
import com.jiubang.ggheart.appgame.appcenter.help.AppCacheManager;
import com.jiubang.ggheart.appgame.base.bean.BoutiqueApp;
import com.jiubang.ggheart.appgame.base.bean.CategoriesDataBean;
import com.jiubang.ggheart.appgame.base.bean.ClassificationDataBean;
import com.jiubang.ggheart.appgame.base.bean.TabDataGroup;
import com.jiubang.ggheart.appgame.base.component.MainViewGroup;
import com.jiubang.ggheart.appgame.base.component.TabManageView;
import com.jiubang.ggheart.appgame.base.data.AppJsonOperator;
import com.jiubang.ggheart.appgame.base.data.ClassificationDataDownload;
import com.jiubang.ggheart.appgame.base.data.ClassificationDataParser;
import com.jiubang.ggheart.appgame.base.data.ClassificationDataParser.LocalJSON;
import com.jiubang.ggheart.appgame.base.data.ClassificationExceptionRecord;
import com.jiubang.ggheart.appgame.base.data.TabDataManager;
import com.jiubang.ggheart.appgame.base.net.AppGameNetRecord;
import com.jiubang.ggheart.appgame.base.net.AppHttpAdapter;
import com.jiubang.ggheart.appgame.base.net.DownloadUtil;
import com.jiubang.ggheart.appgame.base.utils.AppGameInstalledFilter;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.data.statistics.AppManagementStatisticsUtil;
import com.jiubang.ggheart.data.statistics.AppRecommendedStatisticsUtil;

/**
 * tab栏控制器，控制tab栏的切换回退等。控制器会从TabDataManager取数据，然后把数据交给TabManageView显示。
 * 
 * @author xiedezhi
 * 
 */
public class TabController {
	/**
	 * 当前正在取数据的线程
	 */
	private static LoadDataRunnable sCurrentDataRunnable = null;

	private static TabManageView sTabManageView = null;
	/**
	 * 跳转到下一级tab的情况的数据处理类
	 */
	private static AsyncDataHandler sNextTabHandler = new AsyncDataHandler() {

		@Override
		public void handle(TabDataGroup group, int targetIndex,
				int targetSubIndex) {
			if (group == null) {
				return;
			}
			if ((group.isIconTab || group.typeId == ClassificationDataBean.TOP_TYPEID)
					&& (group.categoryData == null || group.categoryData.size() == 0)) {
				// 如果是图标加文字的方式展示顶级tab栏并且是错误数据，展示5个本地写死的顶层分类并默认展示管理页
				showErrorTab();
			} else {
				// 把tab栏数据压栈
				TabDataManager.getInstance().pushTab(group);
				// 通知TabmanageView更新界面
				sTabManageView.updateContentAsyn(group, targetIndex,
						targetSubIndex);
			}
		}
	};
	/**
	 * 跳转到侧面tab的情况的数据处理类
	 */
	private static AsyncDataHandler sSideTabHandler = new AsyncDataHandler() {

		@Override
		public void handle(TabDataGroup group, int targetIndex,
				int targetSubIndex) {
			sTabManageView.updateContentSideAsyn(group, targetIndex,
					targetSubIndex);
		}
	};

	/**
	 * 初始化controller控制的TabManageView，只能初始化一次
	 * 
	 * @param view
	 */
	public static void setTabManageView(TabManageView view) {
		if (sTabManageView != null) {
			Log.e("TabController",
					"setTabManageView TabManageView can only be initialized once");
			sTabManageView.removeAllViews();
			sTabManageView = null;
		}
		if (view == null) {
			throw new IllegalArgumentException(
					"TabController setTabManageView view == null");
		}
		sTabManageView = view;
	}

	/**
	 * 根据怀志需求，进入应用中心/游戏中心，先加载本地数据，然后后台取每个分页的最新数据，切换屏幕时更新界面。同时后台取新顶级tab栏的数据并保存在本地
	 * ， 用于下一次进入时加载 add by xiedezhi 2012-7-10
	 * 
	 * @param title
	 *            顶级tab栏标题
	 * @param access
	 *            入口值，仅在获取顶级tab栏数据时传 1:从快捷方式进入2:从menu进入3:从go精品进入4:
	 *            点击应用图标上面的更新提示进入5: 点击一键装机中的按钮进入 入口类型定义见{@link MainViewGroup}
	 * @param targetIndex
	 *            指定跳转到目标层级的哪个页面，如果不需要指定则传-1
	 * @param targetSubIndex
	 *            指定跳转到目标层级的哪个子层级页面，仅在跳转顶级双层tab栏时有效，如果不需要指定则传-1
	 * @param dataHandler
	 *            收到数据的处理类，如果为空就用mNextTabHandler
	 */
	private static void prevLoad(final String title, final int access,
			final int targetIndex, final int targetSubIndex,
			final AsyncDataHandler dataHandler) {
		if (sTabManageView == null) {
			Log.e("TabController", "skipToTheNextTab mTabManageView == null");
			return;
		}
		// 没有网络就展示5个写死的顶层tab
		if (!Machine.isNetworkOK(sTabManageView.getContext())) {
			// 避免在错误页看到有反馈按钮
			ClassificationExceptionRecord.getInstance().deleteAttachment();
			showErrorTab();
			return;
		}
		sTabManageView.showFakeGridTitleBar();
		// Log.e("XIEDEZHI", "checkPHead success");
		Thread prevLoadThread = new Thread("prevLoadThread") {
			@Override
			public void run() {
				AsyncDataHandler handler = (dataHandler == null) ? sNextTabHandler
						: dataHandler;
				// 这里检查“phead”是否已经改变，如果改变了则需要连网加载新数据
				if (!ClassificationDataDownload.checkPHead(sTabManageView.getContext())) {
					Log.e("TabController", "checkPHead fail");
					// 检查phead失败，把顶层导航栏的数据删除
					ClassificationDataBean topBean = ClassificationDataDownload.getLocalData(
							ClassificationDataBean.TOP_TYPEID, 1, null);
					if (topBean != null && topBean.categoriesList != null) {
						for (CategoriesDataBean category : topBean.categoriesList) {
							if (category != null) {
								// 数据保存键值
								String key = ClassificationDataDownload.buildClassificationKey(category.typeId, 1);
								// 删除本地数据
								AppCacheManager.getInstance().clearCache(key);
							}
						}
						// 数据保存键值
						String key = ClassificationDataDownload
								.buildClassificationKey(
										ClassificationDataBean.TOP_TYPEID, 1);
						// 删除本地数据
						AppCacheManager.getInstance().clearCache(key);
					}
					sCurrentDataRunnable = new LoadDataRunnable(
							ClassificationDataBean.TOP_TYPEID, title, access,
							targetIndex, targetSubIndex, handler);
					sCurrentDataRunnable.run();
					return;
				}
				try {
					// 读取本地数据
					ClassificationDataBean topBean = ClassificationDataDownload
							.getLocalData(ClassificationDataBean.TOP_TYPEID, 1,
									null);
					// 顶层tab栏的数据类型一定要是ICON_TAB_TYPE
					if (topBean != null && topBean.dataType == ClassificationDataBean.ICON_TAB_TYPE
							&& topBean.categoriesList != null && topBean.categoriesList.size() > 0) {
						// 找出首页的分类项数据
						CategoriesDataBean homeCategory = setTopHomePage(topBean.categoriesList,
								access);
						// 首页的分类数据
						ClassificationDataBean homeBean = null;
						// 如果能确定首页
						if (homeCategory != null) {
							// 读取本地保存的首页tab栏数据
							List<ClassificationDataBean> beanList = new ArrayList<ClassificationDataBean>();
							beanList.add(topBean);
							List<Integer> subIdList = new ArrayList<Integer>();
							subIdList.add(ClassificationDataBean.TOP_TYPEID);
							// 读取子tab栏数据是否成功
							boolean success = true;
							// 首页feature特性为0，数据有保存在本地
							if (homeCategory.feature == 0) {
								int typeid = homeCategory.typeId;
								subIdList.add(typeid);
								// 读取本地数据
								homeBean = ClassificationDataDownload
										.getLocalData(typeid, 1, null);
								if (homeBean == null) {
									success = false;
								}
								beanList.add(homeBean);
							}
							// 如果首页没有feature特性，是普通的分类数据
							if (success && homeCategory.feature == 0) {
								// 如果首页数据是分类数据，则再读取首页下的子分类数据
								if (homeBean.dataType == ClassificationDataBean.TAB_TYPE) {
									if (homeBean.categoriesList == null
											|| homeBean.categoriesList.size() <= 0) {
										success = false;
									} else {
										Map<Integer, String> map = new HashMap<Integer, String>();
										for (CategoriesDataBean category : homeBean.categoriesList) {
											if (category == null) {
												success = false;
												break;
											}
											String src = ClassificationDataDownload
													.getLocalString(
															category.typeId, 1);
											if (src == null) {
												success = false;
												break;
											}
											map.put(category.typeId, src);
										}
										if (success) {
											CategoriesDataBean firstScreen = setTopHomePage(
													homeBean.categoriesList,
													access);
											if (firstScreen == null) {
												success = false;
											} else {
												for (CategoriesDataBean subCategory : homeBean.categoriesList) {
													int typeid = subCategory.typeId;
													// 我的应用/应用更新/我的游戏/搜索/管理没有保存在本地
													if (subCategory.feature != 0) {
														continue;
													}
													if (subCategory == firstScreen) {
														List<LocalJSON> sTypeIds = ClassificationDataParser
																.getLocalSubTypeidList(typeid);
														for (LocalJSON sTypeId : sTypeIds) {
															subIdList
																	.add(sTypeId.mTypeId);
															// 读取本地数据
															ClassificationDataBean subBean = ClassificationDataDownload
																	.getLocalData(
																			sTypeId.mTypeId,
																			1,
																			sTypeId.mJson);
															if (subBean == null) {
																Log.e("TabController",
																		"prevLoad bean("
																				+ sTypeId.mTypeId
																				+ ") == null");
																success = false;
																break;
															}
															beanList.add(subBean);
															if (subBean != null
																	&& (subBean.dataType == ClassificationDataBean.TAB_TYPE || subBean.dataType == ClassificationDataBean.BUTTON_TAB)) {
																if (subBean.categoriesList != null
																		&& subBean.categoriesList
																				.size() > 0) {
																	setTopHomePage(
																			subBean.categoriesList,
																			access);
																}
															}
														}
													} else {
														String str = map
																.get(typeid);
														// 读取数据的datatype，如果是Integer.MIN_VALUE就读取失败，如果是coverflow或adbanner就解析，其他就换成EmptyContainer
														int datatype = ClassificationDataParser
																.getDataTypeFromString(str);
														if (datatype == Integer.MIN_VALUE) {
															success = false;
														} else if (datatype == ClassificationDataBean.COVER_FLOW
																|| datatype == ClassificationDataBean.AD_BANNER) {
															subIdList.add(typeid);
															// 读取本地数据
															ClassificationDataBean subBean = ClassificationDataDownload
																	.getLocalData(typeid , 1 , null);
															if (subBean == null) {
																Log.e("TabController",
																		"prevLoad bean("
																				+ typeid
																				+ ") == null");
																success = false;
																break;
															}
															beanList.add(subBean);
														} else {
															// 不需要后台获取新数据，等滑到该页再后台获取新数据
															ClassificationDataBean subBean = new ClassificationDataBean();
															subBean.dataType = ClassificationDataBean.EMPTY_TYPE;
															subBean.typeId = typeid;
															subIdList.add(typeid);
															beanList.add(subBean);
														}
													}
													if (!success) {
														break;
													}
												}
											}
										}
									}
								} else {
									success = false;
								}
							}
							// 如果读取顶级tab栏数据和首页的分类数据成功
							if (success) {
								// 把从本地读出来的数据放到缓存中
								TabDataManager.getInstance().cacheTabData(subIdList, AppGameInstalledFilter.filterDataBeanList(beanList));
								Log.e("TabController", "prevLoad success");
								// 启动LoadDataThread
								sCurrentDataRunnable = new LoadDataRunnable(
										ClassificationDataBean.TOP_TYPEID, title, access,
										targetIndex, targetSubIndex, handler);
								sCurrentDataRunnable.run();
								// 读完本地数据后把线程优先级降到最低
								this.setPriority(Thread.MIN_PRIORITY);
								// 读取每个子tab栏的新数据
								int[] subIdArray = new int[subIdList.size()];
								for (int i = 0; i < subIdList.size(); i++) {
									subIdArray[i] = subIdList.get(i);
								}
								// 更新预加载的tab栏数据
								getNewSubTabData(subIdArray, true);
								// -----------------统计START-----------------------//
								if (beanList != null
										&& beanList.size() > 0) {
									List<String> packNameList = new ArrayList<String>();
									List<String> typeIdList = new ArrayList<String>();
									List<Integer> indexList = new ArrayList<Integer>();
									for (ClassificationDataBean bean : beanList) {
										if (bean != null) {
											if (bean.dataType == ClassificationDataBean.SPECIALSUBJECT_TYPE
													|| bean.dataType == ClassificationDataBean.ONEPERLINE_SPECIALSUBJECT_TYPE
													|| bean.dataType == ClassificationDataBean.EDITOR_RECOMM_TYPE
													|| bean.dataType == ClassificationDataBean.FEATURE_TYPE
													|| bean.dataType == ClassificationDataBean.COVER_FLOW
													|| bean.dataType == ClassificationDataBean.AD_BANNER
													|| bean.dataType == ClassificationDataBean.PRICE_ALERT
													|| bean.dataType == ClassificationDataBean.GRID_TYPE
													|| bean.dataType == ClassificationDataBean.WALLPAPER_GRID) {
												List<BoutiqueApp> featureList = bean.featureList;
												if (featureList != null
														&& featureList.size() > 0) {
													for (int i = 0; i < featureList
															.size(); i++) {
														BoutiqueApp app = featureList
																.get(i);
														packNameList
																.add(app.info.packname);
														typeIdList
																.add(String
																		.valueOf(app.typeid));
														indexList.add(i + 1);
													}
												}
											}
										}
									}
									AppRecommendedStatisticsUtil
											.getInstance()
											.saveAppIssueDataList(
													sTabManageView.getContext(),
													packNameList, typeIdList,
													indexList);
								}
								// -----------------统计END-----------------------//
								return;
							} else {
								Log.e("TabController", "prevLoad fail");
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				// 本地没有顶级tab栏数据或者解析数据错误，重新连网取数据
				sCurrentDataRunnable = new LoadDataRunnable(ClassificationDataBean.TOP_TYPEID,
						title, access, targetIndex, targetSubIndex, handler);
				sCurrentDataRunnable.run();
			};
		};
		prevLoadThread.setPriority(Thread.MAX_PRIORITY);
		prevLoadThread.start();
	}
	
	/**
	 * 主页面上展示一个progressbar表示正在后台加载新数据
	 */
	private static void showPrevLoadProgress() {
		AppsManagementActivity.sendHandler("", IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME,
				IDiyMsgIds.SHOW_PREVLOAD_PROGRESS, -1, null, null);
	}

	/**
	 * 主页面上把progressbar移除表示后台已经加载完新数据
	 */
	private static void hidePrevLoadProgress() {
		AppsManagementActivity.sendHandler("", IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME,
				IDiyMsgIds.HIDE_PREVLOAD_PROGRESS, -1, null, null);
	}

	/**
	 * 预加载，更新当前tab栏的数据，并在用户切换屏幕时刷新界面
	 */
	private static void getNewSubTabData(final int[] subIdArray,
			final boolean showPrevLoadProgress) {
		if (subIdArray == null || subIdArray.length == 0) {
			return;
		}
		if (showPrevLoadProgress) {
			showPrevLoadProgress();
		}
		Context context = sTabManageView.getContext();
		String url = ClassificationDataDownload.getUrl(context);
		// 默认检查第一页数据是否有更新
		final int pageid = 1;
		// 默认入口为1
		int access = 1;
		int[] musts = ClassificationDataDownload.getMusts(subIdArray,
				pageid);
//		 for (int i = 0; i < musts.length; i++) {
//		 musts[i] = 1;
//		 }
		String[] marks = ClassificationDataDownload.getMarks(context, subIdArray);
		String version = ClassificationDataDownload.getVersion();
		final JSONObject postdata = DownloadUtil.getPostJson(context, version, musts, marks,
				subIdArray, access, pageid, 0);
		THttpRequest request = null;
		try {
			request = new THttpRequest(url, postdata.toString().getBytes(),
					new IConnectListener() {

				@Override
				public void onStart(THttpRequest request) {
				}

				@Override
				public void onFinish(THttpRequest request, IResponse response) {
					if (showPrevLoadProgress) {
						hidePrevLoadProgress();
					}
					if (response != null && response.getResponse() != null
							&& (response.getResponse() instanceof JSONObject)) {
						try {
							JSONObject json = (JSONObject) response.getResponse();
//							Log.e("TabController", "getNewSubTabData json = " + json);
							Map<Integer, ClassificationDataBean> map = ClassificationDataParser
									.parseNewSubTabData(sTabManageView.getContext(), subIdArray,
											json, pageid);
							if (map != null) {
								List<ClassificationDataBean> subDataBeanList = new ArrayList<ClassificationDataBean>();
								subDataBeanList.addAll(map.values());
								// 保存这次请求头phead信息，下次进来时判断当前phead与上次phead是否相同，如果不同，则向服务器请求新的数据
								ClassificationDataDownload.savePheadMark(
										sTabManageView.getContext(), postdata);
								List<Integer> ids = new ArrayList<Integer>();
								ids.addAll(map.keySet());
								List<Object> list = new ArrayList<Object>();
								list.add(ids);
								list.add(subDataBeanList);
								// 拿这一块数据放到主线程去更新当前数据，如果是分页数据，就把第一页替换掉
								AppsManagementActivity.sendHandler("",
										IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME,
										IDiyMsgIds.REFRESH_TOPTAB_DATA, -1, list, null);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}

				@Override
				public void onException(THttpRequest request, int reason) {
					if (showPrevLoadProgress) {
						hidePrevLoadProgress();
					}
				}
			});
		} catch (Exception e) {
			if (showPrevLoadProgress) {
				hidePrevLoadProgress();
			}
			return;
		}
		if (request != null) {
			// 设置备选url
			try {
				request.addAlternateUrl(ClassificationDataDownload.getAlternativeUrl(context));
			} catch (Exception e) {
				e.printStackTrace();
			}
			request.setRequestPriority(Thread.MIN_PRIORITY);
			request.setOperator(new AppJsonOperator());
			request.setNetRecord(new AppGameNetRecord(context, false));
			AppHttpAdapter httpAdapter = AppHttpAdapter.getInstance(context);
			httpAdapter.addTask(request, true);
		}
	}

	/**
	 * UI2.0预加载，根据入口设置顶层标题栏的首页
	 * 如果找不到首页就用第一个作为首页
	 * 
	 * @param list
	 *            顶层tab栏分类数据列表
	 * @param access
	 *            入口值， 入口类型定义见{@link MainViewGroup}
	 * 
	 * @return 返回被设为首页的分类项信息单元
	 */
	private static CategoriesDataBean setTopHomePage(
			List<CategoriesDataBean> list, int access) {
		if (list == null || list.size() <= 0) {
			return null;
		}
		// 是否已经找到首页
		boolean hashome = false;
		// 把服务器下发的首页标示去掉
		for (CategoriesDataBean category : list) {
			category.isHome = 0;
			if (!hashome) {
				List<Integer> accessHome = parseAccessHome(category.accesshome);
				// 如果该分类id在当前入口可以作为首页，则把isHome设为1
				if (accessHome != null && accessHome.contains(access)) {
					category.isHome = 1;
					hashome = true;
				}
			}
		}
		for (CategoriesDataBean category : list) {
			if (category.isHome == 1) {
				return category;
			}
		}
		list.get(0).isHome = 1;
		return list.get(0);
	}

	/**
	 * 解析服务器下发的accesshome字段，详情见{@link CategoriesDataBean.accesshome}
	 */
	private static List<Integer> parseAccessHome(String accesshome) {
		try {
			String[] array = accesshome.split("#");
			List<Integer> ret = new ArrayList<Integer>();
			for (int i = 0; i < array.length; i++) {
				String num = array[i];
				if (num != null && !"".equals(num)) {
					int access = Integer.parseInt(num);
					ret.add(access);
				}
			}
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 预加载时如果没网络或者本地没数据网络拿数据出错，展示6个写死的顶级tab选项，并默认首页显示管理页（这时候要显示反馈按钮）
	 */
	public static void showErrorTab() {
		Resources res = sTabManageView.getResources();
		List<CategoriesDataBean> categoryList = new ArrayList<CategoriesDataBean>();
		List<TabDataGroup> dataList = new ArrayList<TabDataGroup>();

		// 首页
		CategoriesDataBean category1 = new CategoriesDataBean();
		category1.name = res.getString(R.string.appgame_toptitle_apps);
		categoryList.add(category1);
		dataList.add(null);

		// 应用
		CategoriesDataBean category2 = new CategoriesDataBean();
		category2.name = res.getString(R.string.appgame_toptitle_app);
		categoryList.add(category2);
		dataList.add(null);

		// 游戏
		CategoriesDataBean category3 = new CategoriesDataBean();
		category3.name = res.getString(R.string.appgame_toptitle_game);
		categoryList.add(category3);
		dataList.add(null);

		// 主题
		CategoriesDataBean category6 = new CategoriesDataBean();
		category6.name = res.getString(R.string.gostore_theme);
		categoryList.add(category6);
		dataList.add(null);

		// 锁屏 
		CategoriesDataBean category4 = new CategoriesDataBean();
		category4.name = res.getString(R.string.gostore_locker);
		categoryList.add(category4);
		dataList.add(null);

		// 管理
		CategoriesDataBean category5 = new CategoriesDataBean();
		category5.name = res.getString(R.string.appgame_toptitle_manage);
		category5.feature = CategoriesDataBean.FEATURE_FOR_MANAGEMENT;
		categoryList.add(category5);
		dataList.add(null);

		TabDataGroup group = new TabDataGroup();
		group.typeId = ClassificationDataBean.TOP_TYPEID;
		group.isIconTab = true;
		group.categoryData = categoryList;
		group.subGroupList = dataList;
		// 应用中心显示首页，游戏中心显示管理页
		group.position = 0;

		// 把tab栏数据压栈
		TabDataManager.getInstance().pushTab(group);
		// 通知TabmanageView更新界面
		sTabManageView.updateContentAsyn(group, -1, -1);
	}

	/**
	 * 跳到下一级tab栏，如果是跳转到顶级tab栏，启动预加载
	 * 
	 * @param typeId
	 *            分类id，顶级tab栏传0
	 * @param title
	 *            这一层级tab的标题
	 * @param access
	 *            入口值，仅在获取顶级tab栏数据时传 1:从快捷方式进入2:从menu进入3:从go精品进入4:
	 *            点击应用图标上面的更新提示进入5: 点击一键装机中的按钮进入 入口类型定义见{@link MainViewGroup}
	 * @param isPrevLoad
	 *            是否启动预加载，仅在获取顶级tab栏数据时传
	 * @param targetIndex
	 *            指定跳转到目标层级的哪个页面，如果不需要指定则传-1
	 * @param targetSubIndex
	 *            指定跳转到目标层级的哪个子层级页面，仅在跳转顶级双层tab栏时有效，如果不需要指定则传-1
	 * @param dataHandler
	 *            收到数据的处理类，如果为空就用mNextTabHandler
	 */
	public static long mT = 0;
	
	public static void skipToTheNextTab(int typeId, String title, int access,
			boolean isPrevLoad, int targetIndex, int targetSubIndex,
			AsyncDataHandler dataHandler) {
		if (sTabManageView == null) {
			Log.e("TabController", "skipToTheNextTab mTabManageView == null");
			return;
		}
		// 如果有搜索的view，先移除
		AppsManagementActivity.sendHandler(sTabManageView.getContext(),
				IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME, IDiyMsgIds.REMOVE_SEARCH_VIEW, 0,
				null, null);
		if (sCurrentDataRunnable != null) {
			sCurrentDataRunnable.kill();
		}
		sTabManageView.prepareToUpdate(true, null);
		// 如果是顶级tab栏，展示一个本地的假GridTitleBar
		if (TabDataManager.getInstance().getTabStackSize() == 0) {
			sTabManageView.showFakeGridTitleBar();
		} else {
			// 如果不是顶级tab栏，展示普通的标题栏，并设置标题
			sTabManageView.setTitle(title);
		}
		// 如果是顶级tab栏并且顶级tab栏的分类数据存在，启动预加载
		if (typeId == ClassificationDataBean.TOP_TYPEID
				&& TabDataManager.getInstance().getTabStackSize() == 0 && isPrevLoad) {
			prevLoad(title, access, targetIndex, targetSubIndex, dataHandler);
			return;
		}
		// 判断当前层级数大于0，把图标隐藏，把返回按钮显示出来
		if (TabDataManager.getInstance().getTabStackSize() > 0) {
			sTabManageView.showIcon(false);
			sTabManageView.showBackButton(true);
		}
		AsyncDataHandler handler = (dataHandler == null) ? sNextTabHandler
				: dataHandler;
		sCurrentDataRunnable = new LoadDataRunnable(typeId, title, access,
				targetIndex, targetSubIndex, handler);
		// 开启线程取数据
		sCurrentDataRunnable.run();
	}

	/**
	 * 跳转到侧面的tab，根据UI2.0新增。
	 * 
	 * @param originalGroup
	 *            顶层tab栏数据
	 * @param targetIndex
	 *            需要更新的侧面tab的下标
	 * @param targetSubIndex
	 *            指定跳转到目标层级的哪个子层级页面，仅在跳转顶级双层tab栏时有效，如果不需要指定则传-1
	 */
	public static void skipToTheSideTab(TabDataGroup originalGroup,
			final int targetIndex, final int targetSubIndex) {
		if (sTabManageView == null || originalGroup == null) {
			Log.e("TabController",
					"skipToTheSideTab mTabManageView == null || originalGroup == null");
			return;
		}
		// 如果不是iconTab
		if (!originalGroup.isIconTab) {
			Log.e("TabController", "skipToTheSideTab !originalGroup.isIconTab");
			return;
		}
		// 如果有搜索的view，先移除
		AppsManagementActivity.sendHandler(sTabManageView.getContext(),
				IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME, IDiyMsgIds.REMOVE_SEARCH_VIEW, 0,
				null, null);
		if (sCurrentDataRunnable != null) {
			sCurrentDataRunnable.kill();
		}
		final CategoriesDataBean category = originalGroup.categoryData
				.get(targetIndex);
		// 先判断originalGroup已经加载的该子tab数据
		TabDataGroup subGroup = originalGroup.subGroupList.get(targetIndex);
		if (subGroup != null) {
			subGroup.title = category.name;
			sTabManageView.prepareToUpdate(false, null);
			sTabManageView.updateContentSideAsyn(subGroup, targetIndex,
					targetSubIndex);
			return;
		}
		// 如果没有加载，则重新加载
		sTabManageView.prepareToUpdate(true, category.name);
		// 本地数据，不需要连网取
		if (category.feature != 0) {
			TabDataGroup group = new TabDataGroup();
			group.title = category.name;
			List<ClassificationDataBean> dataList = new ArrayList<ClassificationDataBean>();
			// 管理页
			if (category.feature == CategoriesDataBean.FEATURE_FOR_MANAGEMENT) {
				// 应用更新
				ClassificationDataBean bean2 = new ClassificationDataBean();
				bean2.typeId = AppManagementStatisticsUtil.TAB_ID_APPUPDATE;
				bean2.dataType = ClassificationDataBean.UPDATE_APP_TYPE;
				bean2.title = sTabManageView.getResources().getString(R.string.apps_update)
						.toUpperCase();
				bean2.funbutton = "";
				dataList.add(bean2);
				// 我的应用
				ClassificationDataBean bean1 = new ClassificationDataBean();
				bean1.typeId = AppManagementStatisticsUtil.TAB_ID_MYAPP;
				bean1.dataType = ClassificationDataBean.MY_APP_TYPE;
				bean1.title = sTabManageView.getResources().getString(R.string.apps_uninstall)
						.toUpperCase();
				bean1.funbutton = "";
				dataList.add(bean1);
				// 高级管理
				ClassificationDataBean bean3 = new ClassificationDataBean();
				bean3.typeId = AppManagementStatisticsUtil.TAB_ID_ADVANCED;
				bean3.dataType = ClassificationDataBean.ADVANCED_MANAGEMENT;
				bean3.title = sTabManageView.getResources()
						.getString(R.string.appcenter_advanced_management).toUpperCase();
				bean3.funbutton = "";
				dataList.add(bean3);
				// 根据入口设定首页
				group.position = 0;
			} else if (category.feature == CategoriesDataBean.FEATURE_FOR_SEARCH) {
				ClassificationDataBean bean = new ClassificationDataBean();
				bean.typeId = AppManagementStatisticsUtil.TAB_ID_SEARCH;
				bean.dataType = featureToDataType(category.feature);
				bean.title = sTabManageView.getResources()
						.getString(R.string.appgame_toptitle_search).toUpperCase();
				bean.funbutton = "";
				dataList.add(bean);
			}
			group.data = dataList;
			sTabManageView.updateContentSideAsyn(group, targetIndex,
					targetSubIndex);
			return;
		}
		// 如果缓存已经有数据
		if (TabDataManager.getInstance().getTabData(category.typeId) != null) {
			sCurrentDataRunnable = new LoadDataRunnable(category.typeId, category.name, 0,
					targetIndex, targetSubIndex, sSideTabHandler);
			// 开启线程取数据
			sCurrentDataRunnable.run();
			return;
		}
		// 预先加载本地数据
		Thread sidePrevLoadThread = new Thread("sidePrevLoadThread") {
			@Override
			public void run() {
				ClassificationDataBean rootBean = ClassificationDataDownload.getLocalData(
						category.typeId, 1, null);
				if (rootBean != null
						&& rootBean.dataType == ClassificationDataBean.TAB_TYPE
						&& rootBean.categoriesList != null
						&& rootBean.categoriesList.size() > 0) {
					// 读取子页面数据是否成功
					boolean success = true;
					List<ClassificationDataBean> beanList = new ArrayList<ClassificationDataBean>();
					List<Integer> idList = new ArrayList<Integer>();
					beanList.add(rootBean);
					idList.add(category.typeId);
					for (CategoriesDataBean subCategory : rootBean.categoriesList) {
						int typeid = subCategory.typeId;
						// 我的应用/应用更新/我的游戏/搜索/管理没有保存在本地
						if (subCategory.feature != 0) {
							continue;
						}
						List<LocalJSON> sTypeIds = ClassificationDataParser
								.getLocalSubTypeidList(typeid);
						for (LocalJSON sTypeId : sTypeIds) {
							idList.add(sTypeId.mTypeId);
							// 先判断是否已经加载到内存了
							ClassificationDataBean subBean = ClassificationDataDownload
									.getLocalData(sTypeId.mTypeId, 1, sTypeId.mJson);
							if (subBean == null) {
								success = false;
								break;
							}
							beanList.add(subBean);
						}
						if (!success) {
							break;
						}
					}
					if (success) {
						// 把从本地读出来的数据放到缓存中
						TabDataManager.getInstance().cacheTabData(idList, AppGameInstalledFilter.filterDataBeanList(beanList));
						sCurrentDataRunnable = new LoadDataRunnable(category.typeId, category.name, 0,
								targetIndex, targetSubIndex, sSideTabHandler);
						// 显示界面
						sCurrentDataRunnable.run();
						this.setPriority(Thread.NORM_PRIORITY);
						// -----------------后台获取新数据START-----------------------//
						int[] subIdArray = new int[idList.size()];
						for (int i = 0; i < idList.size(); i++) {
							subIdArray[i] = idList.get(i);
						}
						getNewSubTabData(subIdArray, false);
						// -----------------后台获取新数据END-----------------------//
						this.setPriority(Thread.MIN_PRIORITY);
						// -----------------统计START-----------------------//
						if (beanList != null && beanList.size() > 0) {
							List<String> packNameList = new ArrayList<String>();
							List<String> typeIdList = new ArrayList<String>();
							List<Integer> indexList = new ArrayList<Integer>();
							for (ClassificationDataBean bean : beanList) {
								if (bean != null) {
									if (bean.dataType == ClassificationDataBean.SPECIALSUBJECT_TYPE
											|| bean.dataType == ClassificationDataBean.ONEPERLINE_SPECIALSUBJECT_TYPE
											|| bean.dataType == ClassificationDataBean.EDITOR_RECOMM_TYPE
											|| bean.dataType == ClassificationDataBean.FEATURE_TYPE
											|| bean.dataType == ClassificationDataBean.COVER_FLOW
											|| bean.dataType == ClassificationDataBean.AD_BANNER
											|| bean.dataType == ClassificationDataBean.PRICE_ALERT
											|| bean.dataType == ClassificationDataBean.GRID_TYPE
											|| bean.dataType == ClassificationDataBean.WALLPAPER_GRID) {
										List<BoutiqueApp> featureList = bean.featureList;
										if (featureList != null
												&& featureList.size() > 0) {
											for (int i = 0; i < featureList
													.size(); i++) {
												BoutiqueApp app = featureList
														.get(i);
												packNameList
														.add(app.info.packname);
												typeIdList.add(String
														.valueOf(app.typeid));
												indexList.add(i + 1);
											}
										}
									}
								}
							}
							AppRecommendedStatisticsUtil
									.getInstance()
									.saveAppIssueDataList(
											sTabManageView.getContext(),
											packNameList, typeIdList, indexList);
						}
						// -----------------统计END-----------------------//
						return;
					}
				}
				sCurrentDataRunnable = new LoadDataRunnable(category.typeId, category.name, 0,
						targetIndex, targetSubIndex, sSideTabHandler);
				// 开启线程取数据
				sCurrentDataRunnable.run();
			}
		};
		sidePrevLoadThread.setPriority(Thread.MAX_PRIORITY);
		sidePrevLoadThread.start();
	}

	/**
	 * 把服务器下发的feature字段转换成本地能理解的数据类型
	 * 
	 * @param feature
	 * @return
	 */
	public static int featureToDataType(int feature) {
		int dataType = 0;
		switch (feature) {
		case CategoriesDataBean.FEATURE_FOR_GAME_AND_APP:
			dataType = ClassificationDataBean.MY_APP_TYPE;
			break;
		case CategoriesDataBean.FEATURE_FOR_APP_UPDATE:
			dataType = ClassificationDataBean.UPDATE_APP_TYPE;
			break;
		case CategoriesDataBean.FEATURE_FOR_SEARCH:
			dataType = ClassificationDataBean.SEARCH_TYPE;
			break;
		case CategoriesDataBean.FEATURE_FOR_MANAGEMENT:
			dataType = ClassificationDataBean.MANAGEMENT_TYPE;
			break;
		default:
			break;
		}
		return dataType;
	}

	/**
	 * 回退到上一级tab
	 */
	public static void fallBackTab() {
		TabDataGroup group = TabDataManager.getInstance().fallBackTab();
		if (group == null) {
			return;
		}
		if (TabDataManager.getInstance().getTabStackSize() > 1) {
			// 设置标题
			sTabManageView.setTitle(group.title);
		}
		sTabManageView.prepareToUpdate(true, null);
		// 通知TabmanageView更新界面
		sTabManageView.updateContentAsyn(group, -1, -1);
	}

	/**
	 * 刷新当前的tab
	 */
	public static void refreshCurrentTab() {
		TabDataGroup originalGroup = TabDataManager.getInstance().popTab();
		if (originalGroup == null) {
			return;
		}
		// 清除TabDataManager里当前层级的数据
		if (originalGroup.typeId == ClassificationDataBean.TOP_TYPEID
		// 如果是刷新顶层tab，把所有数据清空，再去服务器拿取最新数据
				&& TabDataManager.getInstance().getTabStackSize() == 0) {
			TabDataManager.getInstance().removeAllTabData();
		} else {
			// 逐个数据清除
			if (originalGroup.subGroupList != null) {
				for (TabDataGroup subGroup : originalGroup.subGroupList) {
					if (subGroup != null) {
						if (subGroup.data != null) {
							for (ClassificationDataBean subBean : subGroup.data) {
								if (subBean != null) {
									TabDataManager.getInstance()
											.removeTabData(subBean.typeId);
								}
							}
						}
						TabDataManager.getInstance().removeTabData(subGroup.typeId);
					}
				}
			}
			if (originalGroup.data != null) {
				for (ClassificationDataBean bean : originalGroup.data) {
					if (bean != null) {
						TabDataManager.getInstance().removeTabData(bean.typeId);
					}
				}
			}
			if (originalGroup.categoryData != null) {
				for (CategoriesDataBean category : originalGroup.categoryData) {
					if (category != null) {
						TabDataManager.getInstance().removeTabData(category.typeId);
					}
				}
			}
			TabDataManager.getInstance().removeTabData(originalGroup.typeId);
		}
		sTabManageView.prepareToUpdate(true, null);
		// 禁止用户在刷新过程中切换tab
		sTabManageView.disableGridTitleBar();
		// 记下当前位置
		int targetIndex = originalGroup.position;
		int targetSubIndex = -1;
		if (originalGroup.isIconTab && originalGroup.subGroupList != null) {
			List<TabDataGroup> subGroupList = originalGroup.subGroupList;
			int position = originalGroup.position;
			if (position >= 0 && position < subGroupList.size()) {
				TabDataGroup subGroup = originalGroup.subGroupList
						.get(position);
				if (subGroup != null) {
					targetSubIndex = subGroup.position;
				}
			}
		}
		skipToTheNextTab(originalGroup.typeId, originalGroup.title,
				sTabManageView.getEntrance(), false, targetIndex,
				targetSubIndex, null);
	}

	/**
	 * tab是否正在刷新，如果是则返回true，否则返回false
	 */
	public static boolean isTabRefreshing() {
		if (sCurrentDataRunnable == null || sCurrentDataRunnable.isKilled()) {
			return false;
		}
		return true;
	}

	/**
	 * 如果页面正在刷新，用户点击返回键，停止刷新，返回前一页
	 */
	public static void stopRefreshing() {
		if (sCurrentDataRunnable != null) {
			sCurrentDataRunnable.kill();
		}
		TabDataGroup group = TabDataManager.getInstance().peekTab();
		if (group != null) {
			// 如果当前栈size为1，就显示图标，隐藏返回按钮
			if (TabDataManager.getInstance().getTabStackSize() == 1 && !group.isIconTab) {
				// 设置标题
				sTabManageView.setTitle(group.title);
				// 如果入口值不为GoStore，隐藏返回按钮
				if (sTabManageView.getEntrance() != MainViewGroup.ACCESS_FOR_GOSTORE
						&& sTabManageView.getEntrance() != MainViewGroup.ACCESS_FOR_GOSTORE_UPDATE) {
					sTabManageView.showBackButton(false);
					sTabManageView.showIcon(true);
				} else {
					// 如果从GoStore进入，显示返回键，隐藏图标
					sTabManageView.showBackButton(true);
					sTabManageView.showIcon(false);
				}
			}
			sTabManageView.updateContentAsyn(group, -1, -1);
		}
	}
	
	/**
	 * 跳转到应用更新
	 */
	public static void skipToUpdateContainer() {
		if (sTabManageView == null) {
			return;
		}
		if (TabDataManager.getInstance().getTabStackSize() == 1) {
			//当前已经在第一层级tab栏
			TabDataGroup group = TabDataManager.getInstance().peekTab();
			if (group != null && group.isIconTab) {
				List<CategoriesDataBean> list = group.categoryData;
				if (list != null) {
					for (int i = 0; i < list.size(); i++) {
						CategoriesDataBean bean = list.get(i);
						if (bean != null
								&& bean.feature == CategoriesDataBean.FEATURE_FOR_MANAGEMENT) {
							if (group.position != i
									|| (group.subGroupList != null && group.subGroupList.size() > i
											&& group.subGroupList.get(i) != null && group.subGroupList
											.get(i).position != 0)) {
								skipToTheSideTab(group, i, 0);
							}
							return;
						}
					}
				}
			}
		} else if (TabDataManager.getInstance().getTabStackSize() > 1) {
			//当前在第N层级tab栏
			while (TabDataManager.getInstance().getTabStackSize() > 1) {
				TabDataManager.getInstance().fallBackTab();
			}
			TabDataGroup originalGroup = TabDataManager.getInstance().peekTab();
			if (originalGroup != null && originalGroup.isIconTab) {
				List<CategoriesDataBean> list = originalGroup.categoryData;
				if (list != null) {
					for (int i = 0; i < list.size(); i++) {
						CategoriesDataBean bean = list.get(i);
						if (bean != null
								&& bean.feature == CategoriesDataBean.FEATURE_FOR_MANAGEMENT) {
							originalGroup.position = i;
							// 先判断originalGroup已经加载的该子tab数据
							TabDataGroup subGroup = originalGroup.subGroupList.get(i);
							if (subGroup == null || subGroup.data == null
									|| subGroup.data.size() <= 0) {
								// 如果没有加载，则重新加载
								TabDataGroup group = new TabDataGroup();
								List<ClassificationDataBean> dataList = new ArrayList<ClassificationDataBean>();
								// 应用更新
								ClassificationDataBean bean2 = new ClassificationDataBean();
								bean2.typeId = AppManagementStatisticsUtil.TAB_ID_APPUPDATE;
								bean2.dataType = ClassificationDataBean.UPDATE_APP_TYPE;
								bean2.title = sTabManageView.getResources().getString(R.string.apps_update)
										.toUpperCase();
								bean2.funbutton = "";
								dataList.add(bean2);
								// 我的应用
								ClassificationDataBean bean1 = new ClassificationDataBean();
								bean1.typeId = AppManagementStatisticsUtil.TAB_ID_MYAPP;
								bean1.dataType = ClassificationDataBean.MY_APP_TYPE;
								bean1.title = sTabManageView.getResources().getString(R.string.apps_uninstall)
										.toUpperCase();
								bean1.funbutton = "";
								dataList.add(bean1);
								// 高级管理
								ClassificationDataBean bean3 = new ClassificationDataBean();
								bean3.typeId = AppManagementStatisticsUtil.TAB_ID_ADVANCED;
								bean3.dataType = ClassificationDataBean.ADVANCED_MANAGEMENT;
								bean3.title = sTabManageView.getResources()
										.getString(R.string.appcenter_advanced_management).toUpperCase();
								bean3.funbutton = "";
								dataList.add(bean3);
								// 根据入口设定首页
								group.position = 0;
								group.data = dataList;
								originalGroup.subGroupList.set(i, group);
							} else {
								subGroup.position = 0;
							}
							sTabManageView.removeTitleBar();
							sTabManageView.prepareToUpdate(true, bean.name);
							// 通知TabmanageView更新界面
							sTabManageView.updateContentAsyn(originalGroup, -1, -1);
							return;
						}
					}
				}
			}
		}
	}

	/**
	 * 生成一个层级的tab栏数据
	 * 
	 * @param typeId
	 *            该层级tab栏数据的分类id
	 * @param title
	 *            该层级tab栏数据的标题
	 * @param bean
	 *            该层级tab栏数据的根分类数据单元
	 * @return 一个层级的tab栏数据
	 */
	private static TabDataGroup initTabDataGroup(int typeId, String title,
			ClassificationDataBean bean) {
		TabDataGroup group = new TabDataGroup();
		group.typeId = typeId;
		group.title = title;
		if (typeId == -1 || bean == null) {
			return group;
		}
		if (bean.dataType == ClassificationDataBean.ICON_TAB_TYPE) {
			group.isIconTab = true;
			// 标题栏采用图标加文字展现（根据UI2.0新增）
			List<CategoriesDataBean> categoryList = bean.categoriesList;
			if (categoryList == null || categoryList.size() == 0) {
				return group;
			}
			// 对列表按照seq字段排序
			Collections.sort(categoryList);
			group.categoryData = categoryList;
			// 初始化子层的tab栏数据
			group.subGroupList = new ArrayList<TabDataGroup>();
			for (int i = 0; i < group.categoryData.size(); i++) {
				group.subGroupList.add(null);
				CategoriesDataBean category = categoryList.get(i);
				if (category.isHome == 1) {
					group.position = i;
				}
			}
			return group;
		} else if (bean.dataType == ClassificationDataBean.TAB_TYPE) {
			// 多屏页面，分别取每一屏的数据
			List<CategoriesDataBean> categoryList = bean.categoriesList;
			if (categoryList == null || categoryList.size() == 0) {
				return group;
			}
			// 对列表按照seq字段排序
			Collections.sort(categoryList);
			//先把coverflow过滤掉
			List<CategoriesDataBean> tlist = new ArrayList<CategoriesDataBean>();
			for (int i = 0; i < categoryList.size(); i++) {
				CategoriesDataBean category = categoryList.get(i);
				int sTypeId = category.typeId;
				ClassificationDataBean cbean = TabDataManager.getInstance().getTabData(sTypeId);
				if (cbean != null && cbean.dataType == ClassificationDataBean.COVER_FLOW) {
					group.coverFlowBean = cbean;
				} else if (cbean != null && cbean.dataType == ClassificationDataBean.AD_BANNER) {
					group.adBean = cbean;
				} else {
					tlist.add(category);
				}
			}
			categoryList = tlist;
			// tab栏数据列表
			List<ClassificationDataBean> tabDataList = new ArrayList<ClassificationDataBean>();
			for (int i = 0; i < categoryList.size(); i++) {
				CategoriesDataBean category = categoryList.get(i);
				int sTypeId = category.typeId;
				ClassificationDataBean cbean = TabDataManager.getInstance().getTabData(sTypeId);
				if (cbean == null) {
					if (category.feature == CategoriesDataBean.FEATURE_FOR_GAME_AND_APP
							|| category.feature == CategoriesDataBean.FEATURE_FOR_APP_UPDATE
							|| category.feature == CategoriesDataBean.FEATURE_FOR_SEARCH
							|| category.feature == CategoriesDataBean.FEATURE_FOR_MANAGEMENT) {
						cbean = new ClassificationDataBean();
						cbean.typeId = sTypeId;
						cbean.dataType = featureToDataType(category.feature);
					} else {
						Log.e("TabController", sTypeId
								+ " data is null,but it should be in tabDataCache --");
						continue;
					}
				}
				cbean.funbutton = category.funButton;
				cbean.title = category.name;
				tabDataList.add(cbean);
				if (category.isHome == 1) {
					group.position = i;
				}
			}
			group.data = tabDataList;
			return group;
		} else {
			// 单屏页面
			List<ClassificationDataBean> list = new ArrayList<ClassificationDataBean>();
			list.add(bean);
			group.data = list;
			group.position = 0;
			// 单页tab栏统一显示搜索按钮
			bean.funbutton = "1";
			bean.title = title;
			return group;
		}
	}

	/**
	 * 该线程用于为TabController加载数据，加载得到的数据会封装在TabDataGroup中，交给传进来的handler处理
	 */
	private static class LoadDataRunnable implements Runnable {
		/**
		 * 该线程是否已被杀死
		 */
		private boolean mIsKilled = false;
		private Object mIsKilledLock = new Object();

		private int mTypeId;
		private String mTitle;
		private int mAccess;
		/**
		 * 指定跳转到目标层级的哪个页面
		 */
		private int mTargetIndex;
		/**
		 * 指定跳转到目标层级的哪个子层级页面，仅在跳转顶级双层tab栏时有效
		 */
		private int mTargetSubIndex;
		private AsyncDataHandler mHandler;

		/**
		 * 初始化加载数据的线程
		 * 
		 * @param typeId
		 *            需要加载数据的分类id
		 * @param title
		 *            分类id对应的标题
		 * @param access
		 *            入口值，见{@link MainViewGroup}
		 * @param targetIndex
		 *            指定跳转到目标层级的哪个页面，如果不需要指定则传-1
		 * @param targetSubIndex
		 *            指定跳转到目标层级的哪个子层级页面，仅在跳转顶级双层tab栏时有效，如果不需要指定则传-1
		 * @param handler
		 *            处理数据的handler
		 */
		public LoadDataRunnable(int typeId, String title, int access,
				int targetIndex, int targetSubIndex, AsyncDataHandler handler) {
			if (handler == null) {
				throw new IllegalArgumentException(
						"LoadDataThread handler can not be null~");
			}
			this.mTypeId = typeId;
			this.mTitle = title;
			this.mAccess = access;
			this.mTargetIndex = targetIndex;
			this.mTargetSubIndex = targetSubIndex;
			this.mHandler = handler;
		}

		/**
		 * 标志该线程已被杀死，后台拿到数据后不做处理
		 */
		public void kill() {
			synchronized (mIsKilledLock) {
				// Log.e("XIEDEZHI", "LoadDataThread kill");
				mIsKilled = true;
			}
		}

		/**
		 * 判断当前线程是否已被杀死
		 * 
		 */
		public boolean isKilled() {
			synchronized (mIsKilledLock) {
				return mIsKilled;
			}
		}

		@Override
		public void run() {
			Log.d("zxw", "run = "+(System.currentTimeMillis()-mT));
			mT = System.currentTimeMillis();
			if (mTypeId == -1) {
				TabDataGroup group = initTabDataGroup(mTypeId, mTitle, null);
				if (isKilled()) {
					return;
				}
				mHandler.handle(group, mTargetIndex, mTargetSubIndex);
				kill();
				return;
			}
			// 从缓存拿取数据
			ClassificationDataBean bean = TabDataManager.getInstance().getTabData(mTypeId);
			if (bean != null) {
				TabDataGroup group = initTabDataGroup(mTypeId, mTitle, bean);
				if (isKilled()) {
					return;
				}
				mT = System.currentTimeMillis();
				mHandler.handle(group, mTargetIndex, mTargetSubIndex);
				kill();
				return;
			}
			new Thread("LoadDataRunnableInnerThread") {
				public void run() {
					Context context = sTabManageView.getContext();
					String url = ClassificationDataDownload.getUrl(context);
					//TODO:XIEDEZHI 这里读取本地数据会不会很慢
					List<LocalJSON> typeIdList = ClassificationDataParser.getLocalSubTypeidList(mTypeId);
					int[] typeIdArray = new int[typeIdList.size()];
					for (int i = 0; i < typeIdList.size(); i++) {
						typeIdArray[i] = typeIdList.get(i).mTypeId;
					}
					final int[] typeIds = typeIdArray;
					final int pageId = 1;
					final JSONObject postdata = ClassificationDataDownload.getPostJson(context, typeIds,
							mAccess, pageId, 0);
					THttpRequest request = null;
					try {
						request = new THttpRequest(url, postdata.toString().getBytes(),
								new IConnectListener() {

									@Override
									public void onStart(THttpRequest request) {
										Log.d("zxw", "onStart = "+(System.currentTimeMillis()-mT));
										mT = System.currentTimeMillis();
									}

									@Override
									public void onFinish(THttpRequest request,
											IResponse response) {
										Log.d("zxw", "onFinish = "+(System.currentTimeMillis()-mT));
										mT = System.currentTimeMillis();
										if (response != null
												&& response.getResponse() != null
												&& (response.getResponse() instanceof JSONObject)) {
											try {
												JSONObject json = (JSONObject) response
														.getResponse();
												List<ClassificationDataBean> beans = ClassificationDataDownload
														.getClassificationData(json, postdata,
																sTabManageView.getContext(), typeIds,
																pageId, 1, true);
												stopRecord();
												if (beans != null && beans.size() > 0) {
													TabDataGroup group = initTabDataGroup(
															mTypeId, mTitle,
															beans.get(0));
													if (isKilled()) {
														return;
													}
													Log.d("zxw", "onFinish done= "+(System.currentTimeMillis()-mT));
													mT = System.currentTimeMillis();
													mHandler.handle(group,
															mTargetIndex,
															mTargetSubIndex);
													kill();
													return;
												}
											} catch (Exception e) {
												e.printStackTrace();
											}
										}
										stopRecord();
										// 显示错误页
										notifyError();
									}

									@Override
									public void onException(THttpRequest request,
											int reason) {
										stopRecord();
										// 显示错误页
										notifyError();
									}
								});
						request.setNetRecord(new INetRecord() {
							long mt = System.currentTimeMillis();
							@Override
							public void onTransFinish(THttpRequest arg0, Object arg1, Object arg2) {
								// TODO Auto-generated method stub
								Log.d("zxw", "onTransFinish = "+(System.currentTimeMillis()-mt));
								mt = System.currentTimeMillis();
							}
							
							@Override
							public void onStartConnect(THttpRequest arg0, Object arg1, Object arg2) {
								// TODO Auto-generated method stub
								Log.d("zxw", "onStartConnect = "+(System.currentTimeMillis()-mt));
								mt = System.currentTimeMillis();
							}
							
							@Override
							public void onException(Exception arg0, Object arg1, Object arg2) {
								// TODO Auto-generated method stub
								
							}
							
							@Override
							public void onConnectSuccess(THttpRequest arg0, Object arg1, Object arg2) {
								// TODO Auto-generated method stub
								Log.d("zxw", "onConnectSuccess = "+(System.currentTimeMillis()-mt));
								mt = System.currentTimeMillis();
							}
						});
					} catch (Exception e) {
						// 显示错误页
						notifyError();
						return;
					}
					if (request != null) {
						// 设置备选url
						try {
							request.addAlternateUrl(ClassificationDataDownload.getAlternativeUrl(context));
						} catch (Exception e) {
							e.printStackTrace();
						}
						// 设置线程优先级，读取数据的线程优先级应该最高（比拿取图片的线程优先级高）
						request.setRequestPriority(Thread.MAX_PRIORITY);
						request.setOperator(new AppJsonOperator());
//						request.setNetRecord(new AppGameNetRecord(context, true));
						AppHttpAdapter httpAdapter = AppHttpAdapter
								.getInstance(context);
						httpAdapter.addTask(request, true);
					}
				};
			}.start();
		}

		/**
		 * 显示错误页
		 */
		private void notifyError() {
			TabDataGroup group = initTabDataGroup(mTypeId, mTitle, null);
			if (isKilled()) {
				return;
			}
			mHandler.handle(group, mTargetIndex, mTargetSubIndex);
			kill();
		}

		/**
		 * 停止网络错误邮箱反馈记录
		 */
		private void stopRecord() {
			ClassificationExceptionRecord record = ClassificationExceptionRecord
					.getInstance();
			// 如果取数据过程中有错误信息记录下来了，就把信息保存在SD卡
			if (record.hasRecords()) {
				record.buildAttachment(sTabManageView.getContext());
			}
			// 关闭记录器
			record.stopRecord();
		}
	}

	/**
	 * LoadDataThread线程加载数据后的处理类
	 */
	private static interface AsyncDataHandler {
		/**
		 * 处理加载得到的数据
		 * 
		 * @param group
		 *            加载得到的数据
		 * @param targetIndex
		 *            指定跳转到目标层级的哪个页面，如果不需要指定则传-1
		 * @param targetSubIndex
		 *            指定跳转到目标层级的哪个子层级页面，仅在跳转顶级双层tab栏时有效，如果不需要指定则传-1
		 */
		public void handle(TabDataGroup group, int targetIndex,
				int targetSubIndex);
	}
}
