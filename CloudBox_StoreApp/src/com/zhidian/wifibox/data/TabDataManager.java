package com.zhidian.wifibox.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.text.TextUtils;

import com.zhidian.wifibox.data.DetailDataBean.RelatedRecommendBean;

/**
 * 应用列表数据管理器，单例
 * 
 * 不同的页面请求URL不同，就以URL为key，缓存所有页面的数据
 * 
 * @author xiedezhi
 * 
 */
public class TabDataManager {
	/**
	 * 已加载的页面数据，以URL为KEY
	 */
	private Map<String, PageDataBean> mPageDataCache = new HashMap<String, PageDataBean>();
	/**
	 * 已加载的层级数据，以URL为KEY
	 */
	private Map<String, TabDataGroup> mDataGroupCache = new HashMap<String, TabDataGroup>();

	/**
	 * 已加载的详情页数据，以appId为KEY
	 */
	private Map<String, DetailDataBean> mDataAppDetail = new HashMap<String, DetailDataBean>();

	/**
	 * 已加载的相关推荐数据，以appId为KEY
	 */
	private Map<String, List<RelatedRecommendBean>> mDataRelated = new HashMap<String, List<RelatedRecommendBean>>();

	/**
	 * 保存tab的层级状态
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
	 * 缓存某层级的数据
	 */
	public synchronized void cacheDataGroup(String url, TabDataGroup group) {
		mDataGroupCache.put(url, group);
	}

	/**
	 * 获取所有TabDataGroup
	 */
	public synchronized List<TabDataGroup> getAllTabDataGroup() {
		List<TabDataGroup> ret = new ArrayList<TabDataGroup>();
		for (String key : mDataGroupCache.keySet()) {
			ret.add(mDataGroupCache.get(key));
		}
		return ret;
	}

	/**
	 * 缓存某详情页的数据
	 */
	public synchronized void cacheDataAppDetail(String url, DetailDataBean bean) {
		mDataAppDetail.put(url, bean);
	}

	/**
	 * 获取某详情页数据
	 * 
	 * @return 如果没有数据返回null
	 */
	public synchronized DetailDataBean getDataAppDetail(String url) {
		return mDataAppDetail.get(url);
	}

	/**
	 * 缓存某应用的相关推荐页的数据
	 */
	public synchronized void cacheDataRelated(String url,
			List<RelatedRecommendBean> list) {
		mDataRelated.put(url, list);
	}

	/**
	 * 获取某应用的相关推荐页数据
	 * 
	 * @return 如果没有数据返回null
	 */
	public synchronized List<RelatedRecommendBean> getDataAppRelated(String url) {
		return mDataRelated.get(url);
	}

	/**
	 * 获取某层级数据
	 * 
	 * @return 如果没有数据返回null
	 */
	public synchronized TabDataGroup getDataGroup(String url) {
		return mDataGroupCache.get(url);
	}

	/**
	 * 缓存页面数据，如果该页面是某页的下一页，添加到某页的后面
	 */
	public synchronized void cachePageData(PageDataBean newPage) {
		if (newPage == null) {
			return;
		}
		String url = newPage.mUrl;
		if (mPageDataCache.containsKey(url)) {
			mPageDataCache.get(url).append(newPage);
		} else {
			if (!TextUtils.isEmpty(newPage.mTitle)) {
				mPageDataCache.put(url, newPage);
			}
		}
	}

	/**
	 * 替换调当前已缓存数据
	 */
	public synchronized void replacePageData(PageDataBean newPage) {
		if (newPage == null) {
			return;
		}
		String url = newPage.mUrl;
		if (mPageDataCache.containsKey(url)) {
			mPageDataCache.get(url).replace(newPage);
		}
	}

	/**
	 * 获取url对应的页面数据
	 * 
	 * @return 如果没有数据返回null
	 */
	public synchronized PageDataBean getPageData(String url) {
		return mPageDataCache.get(url);
	}

	/**
	 * 重置数据
	 */
	public synchronized void resetPageData(String url) {
		PageDataBean bean = mPageDataCache.get(url);
		if (bean != null) {
			bean.reset();
		}
	}

	/**
	 * 移除缓存中指定的页面数据
	 */
	public synchronized void removePageData(String url) {
		mPageDataCache.remove(url);
	}

	/**
	 * 清空所有的缓存数据
	 */
	public synchronized void removeAllTabData() {
		mPageDataCache.clear();
	}

	/**
	 * 销毁该单实例
	 */
	public synchronized void destory() {
		mPageDataCache.clear();
		mDataGroupCache.clear();
		mDataAppDetail.clear();
		mDataRelated.clear();
		while (mTabDataStack.pop() != null) {
			// nothing
		}
	}

	/**
	 * 进入下一层级，把新的层级数据压栈
	 */
	public synchronized void pushTab(TabDataGroup group) {
		if (group == null) {
			return;
		}
		mTabDataStack.push(group);
	}

	/**
	 * 后退到上一层级，返回上一层级数据
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
	 * 弹出当前顶层数据
	 */
	public synchronized TabDataGroup popTab() {
		return mTabDataStack.pop();
	}

	/**
	 * 获取当前顶层数据，但不出栈
	 */
	public synchronized TabDataGroup peekTab() {
		return mTabDataStack.peek();
	}

	/**
	 * 返回当前层级数
	 */
	public synchronized int getTabStackSize() {
		if (mTabDataStack != null) {
			return mTabDataStack.size();
		}
		return 0;
	}

	/**
	 * 清空层级状态
	 */
	public synchronized void clearTabStack() {
		if (mTabDataStack != null) {
			while (mTabDataStack.pop() != null) {
				// nothing
			}
		}
	}

}
