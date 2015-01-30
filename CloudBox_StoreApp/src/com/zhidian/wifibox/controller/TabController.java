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
				page.mStatisticsTitle = "专题：专题";
			}
			group.mPageList.add(page);
			group.title = "专题";
		} else if (url.equals(NAVIGATIONAPP)) {
			// 应用：分类
			PageDataBean page = TabDataManager.getInstance().getPageData(
					CDataDownloader.getCategoryAppUrl());
			if (page == null) {
				page = new PageDataBean();
				page.mUrl = CDataDownloader.getCategoryAppUrl();
				page.mTitle = "分类";
				page.mStatuscode = -1;
				page.mPageIndex = 0;
				page.mDataType = PageDataBean.CATEGORIES_DATATYPE;
				page.mStatisticsTitle = "应用：分类";
			}
			group.mPageList.add(page);
			// 应用：精品
			page = TabDataManager.getInstance().getPageData(
					CDataDownloader.getFeatureAppUrl(1));
			if (page == null) {
				page = new PageDataBean();
				page.mUrl = CDataDownloader.getFeatureAppUrl(1);
				page.mTitle = "精品";
				page.mStatuscode = -1;
				page.mDataType = PageDataBean.EXTRA_DATATYPE;
				page.mStatisticsTitle = "应用：精品";
			}
			group.mPageList.add(page);
			// 应用：最新
			page = TabDataManager.getInstance().getPageData(
					CDataDownloader.getNewAppUrl(1));
			if (page == null) {
				page = new PageDataBean();
				page.mUrl = CDataDownloader.getNewAppUrl(1);
				page.mTitle = "最新";
				page.mStatuscode = -1;
				page.mDataType = PageDataBean.EXTRA_DATATYPE;
				page.mStatisticsTitle = "应用：最新";
			}
			group.mPageList.add(page);
			group.index = 1;
			group.title = "应用";
		} else if (url.equals(NAVIGATIONFEATURE)) {
			// 首页推荐
			// 如果是推荐页面，mShowBanner为true
			PageDataBean page = TabDataManager.getInstance().getPageData(
					CDataDownloader.getHomeFeatureUrl());
			if (page == null) {
				page = new PageDataBean();
				page.mUrl = CDataDownloader.getHomeFeatureUrl();
				page.mTitle = "推荐";
				page.mStatuscode = -1;
				page.mPageIndex = 0;
				page.mDataType = PageDataBean.HOME_FEATURE_DATATYPE;
				page.mLoadLocalDataFirst = true;
				page.mStatisticsTitle = "首页：推荐";
			}
			group.mPageList.add(page);
			// 首页必备
			page = TabDataManager.getInstance().getPageData(
					CDataDownloader.getHomeMandatoryUrl());
			if (page == null) {
				page = new PageDataBean();
				page.mUrl = CDataDownloader.getHomeMandatoryUrl();
				page.mTitle = "必备";
				page.mStatuscode = -1;
				page.mPageIndex = 0;
				page.mDataType = PageDataBean.EMPTY_DATATYPE;
				page.mStatisticsTitle = "首页：必备";
				PageDataBean sub = TabDataManager.getInstance().getPageData(
						CDataDownloader.getHomeMandatoryInstalledUrl(1));
				if (sub == null) {
					sub = new PageDataBean();
					sub.mUrl = CDataDownloader.getHomeMandatoryInstalledUrl(1);
					sub.mTitle = "装机必备";
					sub.mStatuscode = -1;
					sub.mPageIndex = 0;
					sub.mDataType = PageDataBean.EXTRA_DATATYPE;
					sub.mLoadLocalDataFirst = true;
					sub.mStatisticsTitle = "首页：必备：装机必备";
				}
				page.mSubContainer.add(sub);
				sub = TabDataManager.getInstance().getPageData(
						CDataDownloader.getHomeMandatoryGameUrl(1));
				if (sub == null) {
					sub = new PageDataBean();
					sub.mUrl = CDataDownloader.getHomeMandatoryGameUrl(1);
					sub.mTitle = "游戏达人";
					sub.mStatuscode = -1;
					sub.mPageIndex = 0;
					sub.mDataType = PageDataBean.EXTRA_DATATYPE;
					sub.mLoadLocalDataFirst = true;
					sub.mStatisticsTitle = "首页：必备：游戏达人";
				}
				page.mSubContainer.add(sub);
			}
			group.mPageList.add(page);
			// 首页排行
			page = TabDataManager.getInstance().getPageData(
					CDataDownloader.getHomeRankingUrl());
			if (page == null) {
				page = new PageDataBean();
				page.mUrl = CDataDownloader.getHomeRankingUrl();
				page.mTitle = "排行";
				page.mStatuscode = -1;
				page.mPageIndex = 0;
				page.mDataType = PageDataBean.EMPTY_DATATYPE;
				page.mStatisticsTitle = "首页：排行";
				PageDataBean sub = TabDataManager.getInstance().getPageData(
						CDataDownloader.getHomeRankingGameUrl(1));
				if (sub == null) {
					sub = new PageDataBean();
					sub.mUrl = CDataDownloader.getHomeRankingGameUrl(1);
					sub.mTitle = "游戏";
					sub.mStatuscode = -1;
					sub.mPageIndex = 0;
					sub.mDataType = PageDataBean.EXTRA_RANK;
					sub.mLoadLocalDataFirst = true;
					sub.mIsRanking = true;
					sub.mStatisticsTitle = "首页：排行：游戏";
				}
				page.mSubContainer.add(sub);
				sub = TabDataManager.getInstance().getPageData(
						CDataDownloader.getHomeRankingAppUrl(1));
				if (sub == null) {
					sub = new PageDataBean();
					sub.mUrl = CDataDownloader.getHomeRankingAppUrl(1);
					sub.mTitle = "应用";
					sub.mStatuscode = -1;
					sub.mPageIndex = 0;
					sub.mDataType = PageDataBean.EXTRA_RANK;
					sub.mLoadLocalDataFirst = true;
					sub.mIsRanking = true;
					sub.mStatisticsTitle = "首页：排行：应用";
				}
				page.mSubContainer.add(sub);
			}
			group.mPageList.add(page);
			// 超速下载
			page = TabDataManager.getInstance().getPageData(
					CDataDownloader.getHomeSpeedingDownloadsUrl());
			if (page == null) {
				page = new PageDataBean();
				page.mUrl = CDataDownloader.getHomeSpeedingDownloadsUrl();
				page.mTitle = "超速下载";
				page.mStatuscode = -1;
				page.mPageIndex = 0;
				page.mDataType = PageDataBean.SPEEDINGDOWNLOAD_DATATYPE;
				page.mStatisticsTitle = "首页：超速下载";
			}
			group.mPageList.add(page);
			group.title = TAApplication.getApplication().getString(
					R.string.app_name);
			if (ModeManager.checkRapidly()) {
				group.index = 3;
			}
		} else if (url.equals(NAVIGATIONGAME)) {
			// 游戏：分类
			PageDataBean page = TabDataManager.getInstance().getPageData(
					CDataDownloader.getCategoryGameUrl());
			if (page == null) {
				page = new PageDataBean();
				page.mUrl = CDataDownloader.getCategoryGameUrl();
				page.mTitle = "分类";
				page.mStatuscode = -1;
				page.mPageIndex = 0;
				page.mDataType = PageDataBean.CATEGORIES_DATATYPE;
				page.mStatisticsTitle = "游戏：分类";
			}
			group.mPageList.add(page);
			// 游戏：精品
			page = TabDataManager.getInstance().getPageData(
					CDataDownloader.getFeatureGameUrl(1));
			if (page == null) {
				page = new PageDataBean();
				page.mUrl = CDataDownloader.getFeatureGameUrl(1);
				page.mTitle = "精品";
				page.mStatuscode = -1;
				page.mDataType = PageDataBean.EXTRA_DATATYPE;
				page.mStatisticsTitle = "游戏：精品";
			}
			group.mPageList.add(page);
			// 游戏：最新
			page = TabDataManager.getInstance().getPageData(
					CDataDownloader.getNewGameUrl(1));
			if (page == null) {
				page = new PageDataBean();
				page.mUrl = CDataDownloader.getNewGameUrl(1);
				page.mTitle = "最新";
				page.mStatuscode = -1;
				page.mDataType = PageDataBean.EXTRA_DATATYPE;
				page.mStatisticsTitle = "游戏：最新";
			}
			group.mPageList.add(page);
			group.index = 1;
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
				page.mStatisticsTitle = "管理：管理";
			}
			group.mPageList.add(page);
			group.title = "管理";
		}
		// 保存PageDataBean
		for (PageDataBean bean : group.mPageList) {
			TabDataManager.getInstance().cachePageData(bean);
			// 子bean也要放到缓存
			if (bean.mDataType == PageDataBean.EMPTY_DATATYPE) {
				for (PageDataBean sub : bean.mSubContainer) {
					TabDataManager.getInstance().cachePageData(sub);
				}
			}
		}
		return group;
	}

	/**
	 * 生成子层级的TabDataGroup
	 */
	private TabDataGroup initLevelDataGroup(String url, Object obj) {
		TabDataGroup group = new TabDataGroup();
		group.index = 0;
		// 根据导航url生成不同的List<PageDataBean>
		if (CDataDownloader.isTopicContentUrl(url)) {
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
					page.mStatisticsTitle = "专题内容:" + tBean.title;
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
					page.mStatisticsTitle = "专题内容:" + bBean.title;
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
				page.mDataType = PageDataBean.EXTRA_DATATYPE;
				page.mStatisticsTitle = "分类内容:" + cBean.name;
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
