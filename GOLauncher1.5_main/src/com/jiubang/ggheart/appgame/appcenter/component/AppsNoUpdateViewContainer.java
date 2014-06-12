package com.jiubang.ggheart.appgame.appcenter.component;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.appgame.base.component.AppsDetail;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.gowidget.gostore.common.GoStorePublicDefine;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.AppsBean.AppBean;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreOperatorUtil;
import com.jiubang.ggheart.data.statistics.AppManagementStatisticsUtil;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 忽略更新应用的页面
 * 
 * @author zhoujun
 * 
 */
public class AppsNoUpdateViewContainer extends LinearLayout {

	private LayoutInflater mLayoutInflater;
	// private ThemeTitle mThemeTitle = null;
	private LinearLayout mLinearlayout;
	// 全部恢复/刷新
	private Button mOperationButton;
	/**
	 * 刷新操作
	 */
	private static final int OPERATION_TYPE_REFRESH = 0;
	/**
	 * 全部恢复更新操作
	 */
	private static final int OPERATION_TYPE_REPROMPT_UPDATE = 1;

	private int mOperationType = 0;
	private TextView mUpdateText;

	private ListView mListView;
	private NoUpdateAdapter mNoUpdateApdater;
	private Context mContext;
	private Handler mHandler;
	private OnButtonClick mButtonClick;
	/**
	 * 无数据时，显示的提示信息
	 */
	private LinearLayout mNoDataLinear;
	private TextView mNoDataInfoText;

	public void setmButtonClick(OnButtonClick buttonClick) {
		this.mButtonClick = buttonClick;
	}

	/**
	 * 按钮点击
	 */
	public interface OnButtonClick {
		public void click(String packageName, int position);
	}

	public AppsNoUpdateViewContainer(Context context) {
		super(context);
		mContext = context;
		init();
	}

	public AppsNoUpdateViewContainer(Context context, AttributeSet attr) {
		super(context, attr);
		mContext = context;
		init();
	}

	private void init() {
		this.setOrientation(LinearLayout.VERTICAL);
		setBackgroundColor(Color.parseColor("#faf9f9"));
		mLayoutInflater = LayoutInflater.from(mContext);
		initView();
	}

	private void initView() {
		mLinearlayout = (LinearLayout) mLayoutInflater.inflate(
				R.layout.apps_no_prompt_update_list_container, null);
		mOperationButton = (Button) mLinearlayout
				.findViewById(R.id.no_operation_button);
		mOperationButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mOperationType == OPERATION_TYPE_REFRESH) {
					refreshView();
				} else {
					promptUpdateApp(null, -1);
				}
			}
		});
		mUpdateText = (TextView) mLinearlayout
				.findViewById(R.id.no_update_info);
		mListView = (ListView) mLinearlayout
				.findViewById(R.id.no_upate_list_view);
		mNoUpdateApdater = new NoUpdateAdapter(mContext);
		mListView.setAdapter(mNoUpdateApdater);
		mNoUpdateApdater.setDefaultIcon(getResources().getDrawable(R.drawable.default_icon));
		mListView.setOnScrollListener(mScrollListener);

		mNoDataLinear = (LinearLayout) mLinearlayout
				.findViewById(R.id.no_upate_no_data_linear);
		mNoDataInfoText = (TextView) mLinearlayout
				.findViewById(R.id.no_data_text);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.FILL_PARENT);
		this.addView(mLinearlayout, params);
	}

	public void setmHandler(Handler mHandler) {
		this.mHandler = mHandler;
	}

	public void setmAppBeanList(ArrayList<AppBean> mAppBeanList) {
		// this.mAppBeanList = mAppBeanList;

		if (mNoUpdateApdater != null) {
			mNoUpdateApdater.refreshData(mAppBeanList);
			mNoUpdateApdater.notifyDataSetChanged();
			setUpdateText();
		}

	}

	public void refreshView() {
		if (mNoUpdateApdater != null) {
			mNoUpdateApdater.notifyDataSetChanged();
			setUpdateText();
		}
	}

	/**
	 * 设置忽略更新信息
	 */
	private void setUpdateText() {
		if (mNoUpdateApdater != null) {
			ArrayList<AppBean> appList = mNoUpdateApdater.getmNoUpdateInfos();
			int size = appList != null ? appList.size() : 0;
			String text = mContext
					.getString(R.string.apps_management_none_for_no_update);
			if (size <= 0) {
				// 改变颜色
				// operationButton.setEnabled(false);
				// operationButton.setTextColor(Color.parseColor("#acacac"));

				if (mNoDataLinear.getVisibility() == View.GONE) {
					mNoDataLinear.setVisibility(View.VISIBLE);
				}
				mNoDataInfoText.setVisibility(View.VISIBLE);
				if (!GoLauncher.isPortait()) {
					mNoDataInfoText.setVisibility(View.GONE);
				}
				mOperationButton.setText(R.string.refresh);
				mOperationType = OPERATION_TYPE_REFRESH;
			} else {
				text = size + " " + mContext.getString(R.string.apps_management_has_no_update_item);
				mNoDataLinear.setVisibility(View.GONE);
				mOperationButton
						.setText(R.string.apps_management_reprompt_all_update);
				mOperationType = OPERATION_TYPE_REPROMPT_UPDATE;
			}
			mUpdateText.setText(text);
		}
	}

	public void clean() {
		if (mListView != null) {
			int count = mListView.getChildCount();
			if (count > 0) {
				AppsNoUpdateInfoListItem listItemView = null;
				for (int i = 0; i < count; i++) {
					listItemView = (AppsNoUpdateInfoListItem) mListView
							.getChildAt(i);
					listItemView.destory();
				}
			}
			mListView.setAdapter(null);
		}
		if (mNoUpdateApdater != null) {
			mNoUpdateApdater = null;
		}

	}

	/**
	 * 数据适配器
	 */
	class NoUpdateAdapter extends BaseAdapter {
		private ArrayList<AppBean> mNoUpdateInfos;

		// private ArrayList<NoPromptUpdateInfo> mNoUpdateInfos;
		private Context mContext;
		private LayoutInflater mInflater = null;

		/**
		 * 默认图标
		 */
		private Bitmap mDefaultBitmap = null;
		// private HashMap<Integer, Boolean> mSaveIsOpenInAdapter = new
		// HashMap<Integer, Boolean>();

		public NoUpdateAdapter(Context context) {
			mContext = context;
			mInflater = LayoutInflater.from(mContext);
		}

		public void refreshData(ArrayList<AppBean> noUpdateInfos) {
			// 不要直接引用外部传进来的List，这样有可能报IllegalStateException：The content of the adapter has changed but ListView did not receive a notification.
			// 改成把传进来的bean添加到自己的list中  add by xiedezhi 2013.01.23
			if (mNoUpdateInfos == null) {
				mNoUpdateInfos = new ArrayList<AppBean>();
			} else {
				mNoUpdateInfos.clear();
			}
			if (noUpdateInfos != null) {
				for (AppBean bean : noUpdateInfos) {
					if (bean != null) {
						mNoUpdateInfos.add(bean);
					}
				}
			}
		}

		@Override
		public int getCount() {
			return mNoUpdateInfos == null ? 0 : mNoUpdateInfos.size();
		}

		@Override
		public Object getItem(int position) {
			return mNoUpdateInfos.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, final View convertView,
				ViewGroup parent) {

			AppsNoUpdateInfoListItem noUpdateInfoListItem = null;
			if (mNoUpdateInfos != null && position < mNoUpdateInfos.size()) {
				final AppBean appBean = mNoUpdateInfos.get(position);
				if (convertView != null
						&& convertView instanceof AppsNoUpdateInfoListItem) {
					noUpdateInfoListItem = (AppsNoUpdateInfoListItem) convertView;
					noUpdateInfoListItem.resetDefaultStatus();
				}

				if (noUpdateInfoListItem == null) {
					noUpdateInfoListItem = (AppsNoUpdateInfoListItem) mInflater
							.inflate(
									R.layout.recomm_appsmanagement_no_upate_list_item,
									null);
				}
				noUpdateInfoListItem.bindAppBean(mContext, position, appBean, mDefaultBitmap);
				noUpdateInfoListItem.getmOperationButton().setOnClickListener(
						mOnClickListen);
				noUpdateInfoListItem.getmContentLayout().setOnClickListener(
						mPopClickListen);
			}

			return noUpdateInfoListItem;
		}

		public ArrayList<AppBean> getmNoUpdateInfos() {
			// 不要直接返回内部的list，这样有可能报IllegalStateException：The content of the adapter has changed but ListView did not receive a notification.
			// 改成copy一个新的list返回  add by xiedezhi 2013.01.23
			ArrayList<AppBean> ret = new ArrayList<AppBean>();
			if (mNoUpdateInfos != null) {
				for (AppBean bean : mNoUpdateInfos) {
					if (bean != null) {
						ret.add(bean);
					}
				}
			}
			return ret;
		}

		private OnClickListener mOnClickListen = new OnClickListener() {

			@Override
			public void onClick(View v) {
				// AppBean appBean = (AppBean) v.getTag();
				int position = (Integer) v.getTag();
				if (mNoUpdateInfos != null && position < mNoUpdateInfos.size()) {
					AppBean appBean = mNoUpdateInfos.get(position);
					// buttonClick(appBean.mPkgName, position);
					promptUpdateApp(appBean.mPkgName, position);

				}
				// sendMessage(appBean.mPkgName,v.getId());

			}
		};
		private OnClickListener mPopClickListen = new OnClickListener() {

			@Override
			public void onClick(View v) {
				int position = (Integer) v.getTag();
				if (mNoUpdateInfos != null && position < mNoUpdateInfos.size()) {
					AppBean appBean = mNoUpdateInfos.get(position);
					showPopup(v, appBean);
				}

			}
		};

		/**
		 * 根据包名启动程序
		 * 
		 * @param context
		 * @param pkgName
		 * @return
		 */
		public boolean startApp(Context context, String pkgName) {
			if (context == null) {
				throw new IllegalArgumentException("context can not be null");
			}
			if (pkgName == null) {
				return false;
			}
			PackageManager pm = context.getPackageManager();
			if (pm == null) {
				return false;
			}
			// 需要做这样的判断，否则用户删除该包之后，下面的startActivity会产生异常，导致程序挂掉
			Intent intent = pm.getLaunchIntentForPackage(pkgName);
			if (intent == null) {
				return false;
			}
			try {
				context.startActivity(intent);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		}

		private void doOnItemClick(AppBean appBean) {
			AppManagementStatisticsUtil.getInstance()
					.saveCurrentUIEnter(getContext(),
							AppManagementStatisticsUtil.UIENTRY_TYPE_LIST);

			// 统计详细点击
			AppManagementStatisticsUtil.getInstance().saveDetailsClick(
					getContext(), appBean.mPkgName, appBean.mAppId, 1);

			HashMap<Integer, String> urlHashMap = appBean.mUrlMap;
			if (urlHashMap != null && urlHashMap.size() > 0) {
				// 走ftp，跳转到精品详情页面
				String detailUrl = urlHashMap
						.get(GoStorePublicDefine.URL_TYPE_DETAIL_ADDRESS);
				if (detailUrl != null && !"".equals(detailUrl)) {
					// //应用更新中，状态为等待下载和正在下载的应用，在详情里面，更新按钮不可点击
					// int downloadStatus = 0 ;
					// if (appBean.getStatus() ==
					// AppBean.STATUS_WAITING_DOWNLOAD ||
					// appBean.getStatus() == AppBean.STATUS_DOWNLOADING) {
					// downloadStatus = 1;
					// }
					// GoStoreOperatorUtil.gotoStoreDetailDirectly(
					// getContext(),
					// appBean.mAppId,downloadStatus,ItemDetailActivity.START_TYPE_APPMANAGEMENT,null);

					AppsDetail.gotoDetailDirectly(getContext(), AppsDetail.START_TYPE_APPMANAGEMENT,
							appBean.mAppId, appBean.mPkgName, appBean);

					// 统计：国内---不保存点击更新(times = 0)
					AppManagementStatisticsUtil.getInstance().saveUpdataClick(
							getContext(), appBean.mPkgName, appBean.mAppId, 0);

				} else {
					// 跳转到电子市场
					detailUrl = urlHashMap
							.get(GoStorePublicDefine.URL_TYPE_GOOGLE_MARKET);
					if (detailUrl != null && !"".equals(detailUrl)) {
						detailUrl = detailUrl.trim()
								+ LauncherEnv.GOLAUNCHER_GOOGLE_REFERRAL_LINK;
						// 统计：国外---保存点击更新统计(记作点击过更新)
//						AppManagementStatisticsUtil.getInstance()
//								.saveUpdataClick(getContext(),
//										appBean.mPkgName, appBean.mAppId, 1);
						GoStoreOperatorUtil.gotoMarket(getContext(), detailUrl);
					} else {
						// 跳转到web版电子市场
						detailUrl = urlHashMap
								.get(GoStorePublicDefine.URL_TYPE_WEB_GOOGLE_MARKET);
						if (detailUrl == null || "".equals(detailUrl)) {
							// 跳转到其他地址
							detailUrl = urlHashMap
									.get(GoStorePublicDefine.URL_TYPE_OTHER_ADDRESS);
						}
						if (detailUrl != null && !"".equals(detailUrl)) {
							detailUrl = detailUrl
									+ LauncherEnv.GOLAUNCHER_GOOGLE_REFERRAL_LINK;
							GoStoreOperatorUtil.gotoBrowser(getContext(),
									detailUrl);
						} else {
							// 跳转失败
							Toast.makeText(getContext(),
									R.string.themestore_url_fail,
									Toast.LENGTH_LONG).show();
						}
					}

					// 统计：国外---保存点击更新统计(记作点击过更新)
					AppManagementStatisticsUtil.getInstance().saveUpdataClick(
							getContext(), appBean.mPkgName, appBean.mAppId, 1);
				}

				// 统计：应用更新：再保存UI入口：2
				AppManagementStatisticsUtil.getInstance().saveCurrentUIEnter(
						getContext(),
						AppManagementStatisticsUtil.UIENTRY_TYPE_DETAIL);

			}
		}

		/**
		 * 弹出框的高度，暂时不知如何计算，写死先
		 */
		private final int mPOPUP_HEIGHT = DrawUtils.dip2px(62.0f);

		/**
		 * 弹出框箭头的高度，暂时不知如何计算，写死先
		 */
		private final int mPOPUP_ARROW_HEIGHT = DrawUtils.dip2px(8.0f);
		private RelativeLayout mPopupUpLayout = null;
		private RelativeLayout mPopupDownLayout = null;
		private TextView mUpDetailPage = null;
		private TextView mDownDetailPage = null;
		private TextView mUpOpenApp = null;
		private TextView mDownOpenApp = null;
		private PopupWindow mPopupWindow;

		private void showPopup(View parent, final AppBean bean) {
			if (mPopupWindow == null) {
				View view = mInflater.inflate(
						R.layout.recomm_apps_management_no_update_popup_layout,
						null);
				mPopupWindow = new PopupWindow(view, LayoutParams.FILL_PARENT,
						LayoutParams.WRAP_CONTENT);
				// 设置Popup
				mPopupWindow.setFocusable(true);
				mPopupWindow.setBackgroundDrawable(new ColorDrawable(0));
				mPopupUpLayout = (RelativeLayout) view
						.findViewById(R.id.popwindow_up);
				mUpDetailPage = (TextView) view
						.findViewById(R.id.detail_page_up);
				mDownDetailPage = (TextView) view
						.findViewById(R.id.detail_page_down);
				mPopupDownLayout = (RelativeLayout) view
						.findViewById(R.id.popwindow_down);
				mUpOpenApp = (TextView) view.findViewById(R.id.open_app_up);
				mDownOpenApp = (TextView) view.findViewById(R.id.open_app_down);
			}
			if (mPopupWindow.isShowing()) {
				mPopupWindow.dismiss();
			} else {
				// mPopup.showAtLocation(parent, Gravity.TOP | Gravity.LEFT, 0,
				// 0);
				// mPopup.showAsDropDown(parent);
				// 下面的方法是用屏幕上的绝对坐标显示，mPopupWindow
				// 我们往往不知道mPopupWindow要显示的精确位置，通常先计算页面上某个元素mView的位置，在进行偏移

				// 得到mView在屏幕中的坐标
				int[] pos = new int[2];
				parent.getLocationOnScreen(pos);
				int screenHeight = getScreenHeight(mContext);
				int viewTopPos = pos[1];
				int offsetY = 0;
				if (viewTopPos < screenHeight / 2) {
					mPopupWindow
							.setAnimationStyle(R.style.PopupAnimation_apps_magane_up);
					// 如果是在上半屏幕，则mPopup在组件下方
					offsetY = pos[1] + parent.getHeight() - mPOPUP_ARROW_HEIGHT;
					mPopupUpLayout.setVisibility(View.VISIBLE);
					mPopupDownLayout.setVisibility(View.GONE);
					mDownDetailPage.setOnClickListener(null);
					mDownOpenApp.setOnClickListener(null);
					mUpDetailPage.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							try {
								mPopupWindow.dismiss();
							} catch (Exception e) {
							}

							doOnItemClick(bean);
						}
					});

					mUpOpenApp.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							try {
								mPopupWindow.dismiss();
							} catch (Exception e) {
							}
							startApp(getContext(), bean.mPkgName);
						}
					});
				} else {
					mPopupWindow
							.setAnimationStyle(R.style.PopupAnimation_apps_magane_down);

					// 如果是在下半屏幕，则mPopup在组件上方
					offsetY = pos[1] - mPOPUP_HEIGHT + mPOPUP_ARROW_HEIGHT;
					mPopupUpLayout.setVisibility(View.GONE);
					mPopupDownLayout.setVisibility(View.VISIBLE);
					mUpDetailPage.setOnClickListener(null);
					mUpOpenApp.setOnClickListener(null);
					mDownDetailPage.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							try {
								mPopupWindow.dismiss();
							} catch (Exception e) {
							}

							doOnItemClick(bean);
						}
					});

					mDownOpenApp.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							try {
								mPopupWindow.dismiss();
							} catch (Exception e) {
							}
							startApp(getContext(), bean.mPkgName);
						}
					});
				}
				mPopupWindow.showAtLocation(parent, Gravity.TOP, 0, offsetY);
			}
		}

		public int getScreenHeight(Context context) {
			DisplayMetrics dm = new DisplayMetrics();
			WindowManager wMgr = (WindowManager) context
					.getSystemService(Context.WINDOW_SERVICE);
			wMgr.getDefaultDisplay().getMetrics(dm);
			int width = dm.widthPixels;
			int height = dm.heightPixels;
			return height;
		}

		/**
		 * 设置列表展现的默认图标
		 */
		public void setDefaultIcon(Drawable drawable) {
			if (drawable != null && drawable instanceof BitmapDrawable) {
				mDefaultBitmap = ((BitmapDrawable) drawable).getBitmap();
			}
		}
		
	}

	/**
	 * 恢复更新的应用
	 * 
	 * @param packageName
	 */
	private void promptUpdateApp(String packageName, int position) {
//		ApplicationManager.getInstance(getContext()).deleteNoUpdateApp(
//				packageName);
		if (packageName == null) {
			// 恢复所有更新
			AppsManagementActivity.sendMessage(this,
					IDiyFrameIds.APPS_MANAGEMENT_UPDATE_APP_FRAME,
					IDiyMsgIds.SEND_APP_TO_UPDATE_VIEW, 0,
					mNoUpdateApdater.getmNoUpdateInfos(), null);
		} else {
			AppsManagementActivity.sendMessage(this,
					IDiyFrameIds.APPS_MANAGEMENT_UPDATE_APP_FRAME,
					IDiyMsgIds.SEND_APP_TO_UPDATE_VIEW, 1,
					mNoUpdateApdater.getItem(position), null);
		}
	}
	
	/**
	 * listview滑动监听器
	 */
	private OnScrollListener mScrollListener = new OnScrollListener() {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			switch (scrollState) {
				case OnScrollListener.SCROLL_STATE_IDLE : {
					//列表停止滚动时
					//找出列表可见的第一项和最后一项
					int start = view.getFirstVisiblePosition();
					int end = view.getLastVisiblePosition();
					//如果有添加HeaderView，要减去
					ListView lisView = null;
					if (view instanceof ListView) {
						lisView = (ListView) view;
					}
					if (lisView != null) {
						int headViewCount = lisView.getHeaderViewsCount();
						start -= headViewCount;
						end -= headViewCount;
					}
					if (end >= view.getCount()) {
						end = view.getCount() - 1;
					}
					//对图片控制器进行位置限制设置
					AsyncImageManager.getInstance().setLimitPosition(start, end);
					//然后解锁通知加载
					AsyncImageManager.getInstance().unlock();
				}
					break;
				case OnScrollListener.SCROLL_STATE_FLING : {
					//列表在滚动，图片控制器加锁
					AsyncImageManager.getInstance().lock();
				}
					break;
				case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL : {
					//列表在滚动，图片控制器加锁
					AsyncImageManager.getInstance().lock();
				}
					break;
				default :
					break;
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
				int totalItemCount) {
		}
	};
	
}
