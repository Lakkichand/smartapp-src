package com.zhidian.wifibox.controller;

import com.ta.TAApplication;
import com.ta.mvc.command.TACommand;
import com.ta.mvc.common.TARequest;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.data.BannerDataBean;
import com.zhidian.wifibox.data.CDataDownloader;
import com.zhidian.wifibox.data.CategoriesDataBean;
import com.zhidian.wifibox.data.PageDataBean;
import com.zhidian.wifibox.data.TabDataGroup;
import com.zhidian.wifibox.data.TabDataManager;
import com.zhidian.wifibox.data.TopicDataBean;
import com.zhidian.wifibox.data.XDataDownload;

/**
 * 负责TabManageView的逻辑处理。
 * 
 * 跳转时告诉TabManageView接下来将显示多少个页面，每个页面显示类型是什么，请求数据URL是什么
 * 
 * 页面的数据请求由页面自己的Controller负责
 * 
 * TabController把数据封装成TabDataGroup，再传给TabManageView显示
 * 
 * @author xiedezhi
 * 
 */
public class TabController extends TACommand {
	/**
	 * 专题导航URL
	 */
	public static final String NAVIGATIONTOPIC = "@mNavigationTopic";
	/**
	 * 应用导航URL
	 */
	public static final String NAVIGATIONAPP = "@mNavigationApp";
	/**
	 * 推荐导航URL，这个页面有幻灯片显示
	 */
	public static final String NAVIGATIONFEATURE = "@mNavigationFeature";
	/**
	 * 游戏导航URL
	 */
	public static final String NAVIGATIONGAME = "@mNavigationGame";
	/**
	 * 管理导航URL
	 */
	public static final String NAVIGATIONMANAGE = "@mNavigationManage";
	/**
	 * 装机必备导航URL(极速模式）
	 */
	public static final String XNAVIGATIONMUST = "@xNavigationMust";
	/**
	 * 全新推荐URL(极速模式）
	 */
	public static final String XNAVIGATIONNEW = "@xmNavigationNew";
	/**
	 * 全部导航URL(极速模式）
	 */
	public static final String XNAVIGATIONALL = "@xmNavigationAll";
	/**
	 * 首页推荐，热门应用，点击进入下一层级
	 */
	public static final String FEATUREHOTAPP = "@FEATUREHOTAPP";
	/**
	 * 首页推荐，精品游戏，点击进入下一层
	 */
	public static final String FEATUREHOTGAME = "@FEATUREHOTGAME";
	/**
	 * 首页推荐，专题，点击进入下一层
	 */
	public static final String FEATURETOPIC = "@FEATURETOPIC";
	/**
	 * 首页推荐，排行榜，点击进入下一层
	 */
	public static final String FEATURECHARTS = "@FEATURECHARTS";
	/**
	 * 切换导航栏命令
	 */
	public static final String SWITCH_NAVIGATION = "TABCONTROLLER_SWITCH_NAVIGATION";
	/**
	 * 进入下一层命令
	 */
	public static final String JUMP_NEXT_LEVEL = "TABCONTROLLER_JUMP_NEXT_LEVEL";
	/**
	 * 回退到上一层命令
	 */
	public static final String FALLBACK_TAB = "TABCONTROLLER_FALLBACK_TAB";

	@Override
	protected void executeCommand() {
		// 处理请求，这里是子线程操作
		TARequest request = getRequest();
		String command = (String) request.getTag();
		if (command.equals(SWITCH_NAVIGATION)) {
			// 切换导航栏
			String url = (String) request.getData();
			TabDataGroup group = TabDataManager.getInstance().getDataGroup(url);
			if (group == null) {
				group = initNavDataGroup(url);
				TabDataManager.getInstance().cacheDataGroup(url, group);
			}
			// 处理数据栈
			TabDataManager.getInstance().clearTabStack();
			// 数据入栈
			TabDataManager.getInstance().pushTab(group);
			sendSuccessMessage(group);
		} else if (command.equals(JUMP_NEXT_LEVEL)) {
			// 进入下一层
			Object[] objs = (Object[]) request.getData();
			String url = (String) objs[0];
			Object obj = objs[1];
			TabDataGroup group = TabDataManager.getInstance().getDataGroup(url);
			if (group == null) {
				group = initLevelDataGroup(url, obj);
				TabDataManager.getInstance().cacheDataGroup(url, group);
			}
			// 数据入栈
			TabDataManager.getInstance().pushTab(group);
			sendSuccessMessage(group);
		} else if (command.equals(FALLBACK_TAB)) {
			// 回退上一层
			TabDataGroup group = TabDataManager.getInstance().fallBackTab();
			sendSuccessMessage(group);
		}
	}

	/**
	 * 生成某个导航栏下面的TabDataGroup
	 */
	private TabDataGroup initNavDataGroup(String url) {
		// 根据导航url生成不同的List<PageDataBean>
		TabDataGroup group = new TabDataGroup();
		group.index = 0;
		if (url.equals(NAVIGATIONTOPIC)) {
			// 普通模式专题
			// 专题列表
			PageDataBean page = TabDataManager.getInstance().getPageData(
					CDataDownloader.getTopicUrl(1));
			if (page == null) {
				page = new PageDataBean();
				page.mUrl = CDataDownloader.getTopicUrl(1);
				page.mTitle = "专题";
				// 表示该page还没开始联网读取数据
				page.mStatuscode = -1;
				// 还没开始加载数据所以当前是第0页
				page.mPageIndex = 0;
				page.mDataType = PageDataBean.TOPIC_DATATYPE;
			}
			group.mPageList.add(page);
			group.title = "专题";
		} else if (url.equals(NAVIGATIONAPP)) {
			// 普通模式应用
			// 推荐应用
			PageDataBean page = TabDataManager.getInstance().getPageData(
					CDataDownloader.getRecommendAppUrl(1));
			if (page == null) {
				page = new PageDataBean();
				page.mUrl = CDataDownloader.getRecommendAppUrl(1);
				page.mTitle = TAApplication.getApplication().getString(
						R.string.recommApp);
				page.mStatuscode = -1;
				page.mPageIndex = 0;
				page.mDataType = PageDataBean.EXTRA_DATATYPE;
			}
			group.mPageList.add(page);
			// 应用分类
			page = TabDataManager.getInstance().getPageData(
					CDataDownloader.getCategoryAppUrl());
			if (page == null) {
				page = new PageDataBean();
				page.mUrl = CDataDownloader.getCategoryAppUrl();
				page.mTitle = TAApplication.getApplication().getString(
						R.string.CatApp);
				page.mStatuscode = -1;
				page.mDataType = PageDataBean.CATEGORIES_DATATYPE;
			}
			group.mPageList.add(page);
			group.title = "应用";
		} else if (url.equals(NAVIGATIONFEATURE)) {
			// 普通模式推荐
			// 如果是推荐页面，mShowBanner为true
			PageDataBean page = TabDataManager.getInstance().getPageData(
					CDataDownloader.getFeatureUrl(1));
			if (page == null) {
				page = new PageDataBean();
				page.mUrl = CDataDownloader.getFeatureUrl(1);
				page.mTitle = "首页推荐";
				page.mStatuscode = -1;
				page.mPageIndex = 0;
				page.mDataType = PageDataBean.FEATURE_DATATYPE;
				page.mShowBanner = true;
				page.mBannerMark = "0000";
				page.mLoadLocalDataFirst = true;
			}
			group.mPageList.add(page);
			group.title = "推荐";
		} else if (url.equals(NAVIGATIONGAME)) {
			// 普通模式游戏
			// 推荐游戏
			PageDataBean page = TabDataManager.getInstance().getPageData(
					CDataDownloader.getRecommendGameUrl(1));
			if (page == null) {
				page = new PageDataBean();
				page.mUrl = CDataDownloader.getRecommendGameUrl(1);
				page.mTitle = TAApplication.getApplication().getString(
						R.string.recommGame);
				page.mStatuscode = -1;
				page.mPageIndex = 0;
				page.mDataType = PageDataBean.EXTRA_DATATYPE;
			}
			group.mPageList.add(page);
			// 游戏分类
			page = TabDataManager.getInstance().getPageData(
					CDataDownloader.getCategoryGameUrl());
			if (page == null) {
				page = new PageDataBean();
				page.mUrl = CDataDownloader.getCategoryGameUrl();
				page.mTitle = TAApplication.getApplication().getString(
						R.string.CatGame);
				page.mStatuscode = -1;
				page.mPageIndex = 0;
				page.mDataType = PageDataBean.CATEGORIES_DATATYPE;
			}
			group.mPageList.add(page);
			group.title = "游戏";
		} else if (url.equals(NAVIGATIONMANAGE)) {
			// 普通模式管理
			PageDataBean page = TabDataManager.getInstance().getPageData(
					PageDataBean.MANAGER_URL);
			if (page == null) {
				page = new PageDataBean();
				page.mUrl = PageDataBean.MANAGER_URL;
				page.mTitle = TAApplication.getApplication().getString(
						R.string.navigationmanage);
				page.mStatuscode = -1;
				page.mDataType = PageDataBean.MANAGER_DATATYPE;
			}
			group.mPageList.add(page);
			group.title = "管理";
		} else if (url.equals(XNAVIGATIONMUST)) {
			// 极速模式装机必备
			PageDataBean page = TabDataManager.getInstance().getPageData(
					XDataDownload.getXMustIDUrl());
			if (page == null) {
				page = new PageDataBean();
				page.mUrl = XDataDownload.getXMustIDUrl();
				page.mTitle = "极速装机必备";
				// 表示该page还没开始联网读取数据
				page.mStatuscode = -1;
				// 还没开始加载数据所以当前是第0页
				page.mPageIndex = 0;
				page.mDataType = PageDataBean.XMUST_DATATYPE;
			}
			group.mPageList.add(page);
			group.title = "装机必备";
		} else if (url.equals(XNAVIGATIONNEW)) {
			// 极速模式新品推荐
			PageDataBean page = TabDataManager.getInstance().getPageData(
					XDataDownload.getXNewIDUrl());
			if (page == null) {
				page = new PageDataBean();
				page.mUrl = XDataDownload.getXNewIDUrl();
				page.mTitle = "极速新品推荐";
				// 表示该page还没开始联网读取数据
				page.mStatuscode = -1;
				// 还没开始加载数据所以当前是第0页
				page.mPageIndex = 0;
				page.mDataType = PageDataBean.XNEW_DATATYPE;
			}
			group.mPageList.add(page);
			group.title = "新品推荐";
		} else if (url.equals(XNAVIGATIONALL)) {
			// 极速模式全部
			PageDataBean page = TabDataManager.getInstance().getPageData(
					XDataDownload.getXAllUrl());
			if (page == null) {
				page = new PageDataBean();
				page.mUrl = XDataDownload.getXAllUrl();
				page.mTitle = "极速全部";
				// 表示该page还没开始联网读取数据
				page.mStatuscode = -1;
				// 还没开始加载数据所以当前是第0页
				page.mPageIndex = 0;
				page.mDataType = PageDataBean.XALL_DATATYPE;
			}
			group.mPageList.add(page);
			group.title = "全部";
		}
		// 保存PageDataBean
		for (PageDataBean bean : group.mPageList) {
			TabDataManager.getInstance().cachePageData(bean);
		}
		return group;
	}

	/**
	 * 生成子层级的TabDataGroup
	 */
	TabDataGroup group = new TabDataGroup();

	private TabDataGroup initLevelDataGroup(String url, Object obj) {
		group.index = 0;
		// 根据导航url生成不同的List<PageDataBean>
		if (url.equals(FEATUREHOTAPP)) {
			// 热门应用
			PageDataBean page = TabDataManager.getInstance().getPageData(
					CDataDownloader.getHotAppUrl(1));
			if (page == null) {
				page = new PageDataBean();
				page.mUrl = CDataDownloader.getHotAppUrl(1);
				page.mTitle = TAApplication.getApplication().getString(
						R.string.hotapp);
				// 表示该page还没开始联网读取数据
				page.mStatuscode = -1;
				// 还没开始加载数据所以当前是第0页
				page.mPageIndex = 0;
				page.mDataType = PageDataBean.EXTRA_DATATYPE;
			}
			group.mPageList.add(page);
			group.title = TAApplication.getApplication().getString(
					R.string.hotapp);
		} else if (url.equals(FEATUREHOTGAME)) {
			// 精品游戏
			PageDataBean page = TabDataManager.getInstance().getPageData(
					CDataDownloader.getHotGameUrl(1));
			if (page == null) {
				page = new PageDataBean();
				page.mUrl = CDataDownloader.getHotGameUrl(1);
				page.mTitle = TAApplication.getApplication().getString(
						R.string.hotgame);
				// 表示该page还没开始联网读取数据
				page.mStatuscode = -1;
				// 还没开始加载数据所以当前是第0页
				page.mPageIndex = 0;
				page.mDataType = PageDataBean.EXTRA_DATATYPE;
			}
			group.mPageList.add(page);
			group.title = TAApplication.getApplication().getString(
					R.string.hotgame);
		} else if (url.equals(FEATURETOPIC)) {
			// 专题
			PageDataBean page = TabDataManager.getInstance().getPageData(
					CDataDownloader.getTopicUrl(1));
			if (page == null) {
				page = new PageDataBean();
				page.mUrl = CDataDownloader.getTopicUrl(1);
				page.mTitle = TAApplication.getApplication().getString(
						R.string.topic);
				// 表示该page还没开始联网读取数据
				page.mStatuscode = -1;
				// 还没开始加载数据所以当前是第0页
				page.mPageIndex = 0;
				page.mDataType = PageDataBean.TOPIC_DATATYPE;
			}
			group.mPageList.add(page);
			group.title = TAApplication.getApplication().getString(
					R.string.topic);
		} else if (url.equals(FEATURECHARTS)) {
			// 排行榜，应用
			PageDataBean page = TabDataManager.getInstance().getPageData(
					CDataDownloader.getWarmingAppUrl(1));
			if (page == null) {
				page = new PageDataBean();
				page.mUrl = CDataDownloader.getWarmingAppUrl(1);
				page.mTitle = TAApplication.getApplication().getString(
						R.string.app);
				// 表示该page还没开始联网读取数据
				page.mStatuscode = -1;
				// 还没开始加载数据所以当前是第0页
				page.mPageIndex = 0;
				page.mDataType = PageDataBean.RANKING_DATATYPE;
			}
			group.mPageList.add(page);
			// 排行榜，游戏
			page = TabDataManager.getInstance().getPageData(
					CDataDownloader.getWarmingGameUrl(1));
			if (page == null) {
				page = new PageDataBean();
				page.mUrl = CDataDownloader.getWarmingGameUrl(1);
				page.mTitle = TAApplication.getApplication().getString(
						R.string.game);
				// 表示该page还没开始联网读取数据
				page.mStatuscode = -1;
				// 还没开始加载数据所以当前是第0页
				page.mPageIndex = 0;
				page.mDataType = PageDataBean.RANKING_DATATYPE;
			}
			group.mPageList.add(page);
			group.title = TAApplication.getApplication().getString(
					R.string.ranking);
		} else if (CDataDownloader.isTopicContentUrl(url)) {
			if (obj instanceof TopicDataBean) {
				// 专题内容列表
				TopicDataBean tBean = (TopicDataBean) obj;
				PageDataBean page = TabDataManager.getInstance().getPageData(
						url);
				if (page == null) {
					page = new PageDataBean();
					page.mUrl = url;
					page.mTitle = tBean.title;
					page.mStatuscode = -1;
					page.mPageIndex = 0;
					page.mDataType = PageDataBean.TOPICCONTENT_DATATYPE;
				}
				group.mPageList.add(page);
				group.title = tBean.title;
			} else if (obj instanceof BannerDataBean) {
				BannerDataBean bBean = (BannerDataBean) obj;
				PageDataBean page = TabDataManager.getInstance().getPageData(
						url);
				if (page == null) {
					page = new PageDataBean();
					page.mUrl = url;
					page.mTitle = bBean.title;
					page.mStatuscode = -1;
					page.mPageIndex = 0;
					page.mDataType = PageDataBean.TOPICCONTENT_DATATYPE;
				}
				group.mPageList.add(page);
				group.title = bBean.title;
			}
		} else if (CDataDownloader.isCategoryContentUrl(url)) {
			CategoriesDataBean cBean = (CategoriesDataBean) obj;
			PageDataBean page = TabDataManager.getInstance().getPageData(url);
			if (page == null) {
				page = new PageDataBean();
				page.mUrl = url;
				page.mTitle = cBean.name;
				page.mStatuscode = -1;
				page.mPageIndex = 0;
				page.mDataType = PageDataBean.CATEGORY_CONTENT_DATATYPE;
			}
			group.mPageList.add(page);
			group.title = cBean.name;
		}
		// 保存PageDataBean
		for (PageDataBean bean : group.mPageList) {
			TabDataManager.getInstance().cachePageData(bean);
		}
		return group;
	}

}
