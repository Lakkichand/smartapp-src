package com.jiubang.ggheart.appgame.base.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.jiubang.ggheart.appgame.base.bean.BoutiqueApp;
import com.jiubang.ggheart.appgame.base.bean.CategoriesDataBean;
import com.jiubang.ggheart.appgame.base.bean.ClassificationDataBean;
import com.jiubang.ggheart.appgame.base.bean.TabDataGroup;

/**
 * tab栏数据管理器，为TabController提供数据
 * 
 * @author xiedezhi
 * 
 */
public class TabDataManager {
	/**
	 * 缓存分类id对应的数据
	 */
	private Map<Integer, ClassificationDataBean> mTabDataCache = new HashMap<Integer, ClassificationDataBean>();

	/**
	 * 保存tab栏的层级状态
	 */
	private Stack<TabDataGroup> mTabDataStack = new Stack<TabDataGroup>();
	
	/**
	 * 单实例
	 */
	private volatile static TabDataManager sInstance = null;
	
	/**
	 * 获取TabDataManager实例对象
	 */
	public static TabDataManager getInstance() {
		if (sInstance == null) {
			synchronized (TabDataManager.class) {
				if (sInstance == null) {
					sInstance = new TabDataManager();
				}
			}
		}
		return sInstance;
	}
	
	/**
	 * 初始化函数
	 */
	private TabDataManager() {
	}

	/**
	 * 预加载，进来先读本地数据，后台加载每个子tab新数据，然后用户切换屏幕时刷新界面
	 * 
	 * @param 顶级tab栏各子tab栏的新数据
	 */
	public synchronized void refreshTopTabData(List<Integer> typeIds,
			List<ClassificationDataBean> subDataBeanList) {
		if (typeIds == null || typeIds.size() <= 0 || subDataBeanList == null || subDataBeanList.size() <= 0) {
			Log.e("TabDataManager",
					"refreshTopTabData typeIds == null || typeIds.size() <= 0 || beans == null || beans.size() <= 0");
			return;
		}
		if (typeIds.size() != subDataBeanList.size()) {
			Log.e("TabDataManager", "refreshTopTabData typeIds.size()(+" + typeIds.size()
					+ ") != subDataBeanList.size()(" + subDataBeanList.size() + ")");
			return;
		}
		int count = subDataBeanList.size();
		for (int i = 0; i < count; i++) {
			ClassificationDataBean newBean = subDataBeanList.get(i);
			if (newBean == null) {
				continue;
			}
			int typeid = typeIds.get(i);
			// 缓存数据以分类id为key
			if (mTabDataCache.containsKey(typeid)) {
				ClassificationDataBean cacheBean = mTabDataCache.get(typeid);
				if (cacheBean.dataType == newBean.dataType) {
					// 是分页数据
					if (cacheBean.dataType == ClassificationDataBean.SPECIALSUBJECT_TYPE
							|| cacheBean.dataType == ClassificationDataBean.ONEPERLINE_SPECIALSUBJECT_TYPE
							|| cacheBean.dataType == ClassificationDataBean.EDITOR_RECOMM_TYPE
							|| cacheBean.dataType == ClassificationDataBean.FEATURE_TYPE
							|| cacheBean.dataType == ClassificationDataBean.GRID_TYPE
							|| cacheBean.dataType == ClassificationDataBean.PRICE_ALERT
							|| cacheBean.dataType == ClassificationDataBean.WALLPAPER_GRID) {
						// 如果旧数据列表长于新数据列表，把旧数据的前n个换成新数据（n是新数据的列表长度）
						if (cacheBean.featureList != null && newBean.featureList != null
								&& cacheBean.featureList.size() >= newBean.featureList.size()) {
							int newBeanSize = newBean.featureList.size();
							for (int j = 0; j < newBeanSize; j++) {
								BoutiqueApp app = newBean.featureList.get(j);
								cacheBean.featureList.set(j, app);
							}
						} else {
							cacheBean.featureList = newBean.featureList;
						}
					} else {
						// 非分页数据，直接替换内容
						cacheBean.categoriesList = newBean.categoriesList;
						cacheBean.featureList = newBean.featureList;
					}
				} else if (cacheBean.dataType == ClassificationDataBean.EMPTY_TYPE) {
					// 如果缓存数据里的是空白类型，替换成新数据
					Log.e("TabDataManager", "refreshTopTabData cacheBean.dataType == ClassificationDataBean.EMPTY_TYPE");
					cacheBean.copyFrom(newBean);
				} else {
					Log.e("TabDataManager",
							"refreshTopTabData cacheBean.dataType("
									+ cacheBean.dataType
									+ ") != newBean.dataType("
									+ newBean.dataType + ")");
				}
			} else {
				Log.e("TabDataManager", "refreshTopTabData !tabDataCache.containsKey(" + typeid
						+ ")");
			}
		}
	}

	/**
	 * 缓存分类id对应的数据
	 */
	private synchronized void cacheTabData(int typeid, ClassificationDataBean bean) {
		if (bean == null) {
			Log.e("TabDataManager", "cacheTabData bean == null");
			return;
		}
		// 缓存数据以分类id为key，多页情况要把后面的页合并到之前的页中
		if (mTabDataCache.containsKey(typeid)) {
			ClassificationDataBean cbean = mTabDataCache.get(typeid);
			if (cbean.dataType == bean.dataType) {
				if (cbean.dataType == ClassificationDataBean.SPECIALSUBJECT_TYPE
						|| cbean.dataType == ClassificationDataBean.ONEPERLINE_SPECIALSUBJECT_TYPE
						|| cbean.dataType == ClassificationDataBean.EDITOR_RECOMM_TYPE
						|| cbean.dataType == ClassificationDataBean.FEATURE_TYPE
						|| cbean.dataType == ClassificationDataBean.PRICE_ALERT
						|| cbean.dataType == ClassificationDataBean.GRID_TYPE
						|| cbean.dataType == ClassificationDataBean.WALLPAPER_GRID) {
					// 是分页数据，把新的一页加到上一页的后面
					if (cbean.featureList != null
							&& bean.pageid == (cbean.pageid + 1)) { // 这里要判断新来的数据是否是已在内存的数据的下一页
						cbean.featureList.addAll(bean.featureList);
						cbean.pageid = bean.pageid;
						// 以新数据的总页码为准
						cbean.pages = bean.pages;
						return;
					} else {
						if (bean.pageid != (cbean.pageid + 1)) {
							Log.e("TabDataManager",
									"cacheTabData bean.pageid = " + bean.pageid
											+ "  cbean.pageid = "
											+ cbean.pageid);
						}
					}
				} else {
					// 不是分页数据，直接替换内容
					cbean.categoriesList = bean.categoriesList;
					cbean.featureList = bean.featureList;
				}
			} else if (cbean.dataType == ClassificationDataBean.EMPTY_TYPE) {
				// 如果缓存数据里的是空白类型，替换成新数据
				cbean.copyFrom(bean);
			} else {
				Log.e("TabDataManager", "cacheTabData Execution failed");
			}
		} else {
			mTabDataCache.put(typeid, bean);
		}
	}

	/**
	 * 缓存分类id对应的数据
	 */
	public synchronized void cacheTabData(List<Integer> typeIds, List<ClassificationDataBean> beans) {
		if (typeIds == null || typeIds.size() <= 0 || beans == null || beans.size() <= 0) {
			Log.e("TabDataManager",
					"cacheTabData typeIds == null || typeIds.size() <= 0 || beans == null || beans.size() <= 0");
			return;
		}
		if (typeIds.size() != beans.size()) {
			Log.e("TabDataManager", "cacheTabData typeIds.size()(+" + typeIds.size()
					+ ") != beans.size()(" + beans.size() + ")");
			return;
		}
		int count = beans.size();
		ClassificationDataBean bean = null;
		for (int i = 0; i < count; i++) {
			bean = beans.get(i);
			int typeId = typeIds.get(i);
			cacheTabData(typeId, bean);
		}
	}
	
	

	/**
	 * 获取分类id对应的数据
	 * 
	 * @param typeid
	 *            分类id
	 * @return 如果没有数据返回null
	 */
	public synchronized ClassificationDataBean getTabData(int typeid) {
		return mTabDataCache.get(typeid);
	}

	/**
	 * 移除缓存中指定的分类数据
	 * 
	 * @param typeid
	 *            需要移除的数据对应的分类id
	 */
	public synchronized void removeTabData(int typeid) {
		mTabDataCache.remove(typeid);
	}

	/**
	 * 清空所有的缓存数据
	 */
	public synchronized void removeAllTabData() {
		mTabDataCache.clear();
	}
	
	/**
	 * 销毁该单实例
	 */
	public synchronized void destory() {
		mTabDataCache.clear();
		while (mTabDataStack.pop() != null) {
			// nothing
		}
	}
	
	/**
	 * 是否首层tab的子ID，用于给ContainerBuilder判断是否需要强引用container
	 * 
	 * 首层MultiContainer的子页面也属于首层tab的页面
	 */
	public synchronized boolean isTopTabId(int typeId) {
		ClassificationDataBean topBean = mTabDataCache.get(ClassificationDataBean.TOP_TYPEID);
		if (topBean != null && topBean.categoriesList != null) {
			for (CategoriesDataBean cBean : topBean.categoriesList) {
				if (cBean != null && cBean.feature == 0) {
					int sId = cBean.typeId;
					ClassificationDataBean subBean = mTabDataCache.get(sId);
					if (subBean != null && subBean.dataType == ClassificationDataBean.TAB_TYPE
							&& subBean.categoriesList != null) {
						for (CategoriesDataBean subCBean : subBean.categoriesList) {
							if (subCBean != null) {
								if (typeId == subCBean.typeId) {
									return true;
								} else {
									int subCTypeId = subCBean.typeId;
									ClassificationDataBean subSubBean = mTabDataCache
											.get(subCTypeId);
									if (subSubBean != null
											&& (subSubBean.dataType == ClassificationDataBean.TAB_TYPE || subSubBean.dataType == ClassificationDataBean.BUTTON_TAB)
											&& subSubBean.categoriesList != null) {
										for (CategoriesDataBean subSubCBean : subSubBean.categoriesList) {
											if (subSubCBean != null && typeId == subSubCBean.typeId) {
												return true;
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * 进入新的tab栏，把新的tab栏数据压栈
	 * 
	 * @param group
	 *            新的tab栏数据列表
	 */
	public synchronized void pushTab(TabDataGroup group) {
		if (group == null) {
			return;
		}
		// 把新的tab栏压栈
		mTabDataStack.push(group);
	}

	/**
	 * 后退到上一级tab栏，返回上一级tab栏数据列表
	 * 
	 * @return 上一级tab栏数据列表，当前已经是最第一级就返回null
	 */
	public synchronized TabDataGroup fallBackTab() {
		if (mTabDataStack.size() <= 1) {
			// 已经是最第一级，不能再回退了
			return null;
		}
		mTabDataStack.pop();
		return mTabDataStack.peek();
	}

	/**
	 * 弹出当前顶层tab栏数据
	 * 
	 * @return 弹出当前顶层tab栏数据
	 */
	public synchronized TabDataGroup popTab() {
		return mTabDataStack.pop();
	}

	/**
	 * 获取当前顶层tab栏数据，但不出栈
	 * 
	 * @return 获取当前顶层tab栏数据
	 */
	public synchronized TabDataGroup peekTab() {
		return mTabDataStack.peek();
	}

	/**
	 * 返回当前tab的层级数
	 * 
	 * @return
	 */
	public synchronized int getTabStackSize() {
		if (mTabDataStack != null) {
			return mTabDataStack.size();
		}
		return 0;
	}

	/**
	 * 返回当前是否是顶级TAB栏
	 * 
	 * @return
	 */
	public synchronized boolean isTopTab() {
		return getTabStackSize() == 1;
	}

}
