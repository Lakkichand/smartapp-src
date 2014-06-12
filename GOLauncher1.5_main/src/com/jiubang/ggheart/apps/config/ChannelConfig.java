package com.jiubang.ggheart.apps.config;

import java.io.Serializable;

import android.content.Context;

import com.jiubang.ggheart.apps.config.parser.ChannelConfigParser;

/**
 * 渠道配置信息类 该类会根据本包的桌面渠道号和配置文件信息，决定最终的字段值
 * 
 * @author wangzhuobin
 * 
 */
@SuppressWarnings("serial")
public class ChannelConfig implements Serializable {
	/**
	 * 单例
	 */
	private static ChannelConfig sSelf = null;

	/**
	 * 配置信息的解释器
	 */
	private ChannelConfigParser mParser = null;

	/**
	 * 全部渠道匹配的配置常量值
	 */
	public static final String ALL_CHANNEL_VALUE = "*";

	// ///////////////////////////与应用游戏中心相关的配置信息--START
	/**
	 * 是否需要下载管理界面的入口
	 */
	private boolean mNeedDownloadManager = false;
	// 应用中心
	/**
	 * 是否需要应用中心
	 */
	private boolean mNeedAppCenter = false;
	/**
	 * 是否需要在桌面主菜单添加应用中心入口
	 */
	private boolean mAddAppMainMenuItem = false;
	/**
	 * 是否需要在功能表添加应用中心假图标
	 */
	private boolean mAddAppFunItem = false;
	/**
	 * 是否需要在功能表菜单添加应用中心入口
	 */
	private boolean mAddAppFunMenuItem = false;
	/**
	 * 是否需要在GO精品标题添加应用中心入口
	 */
	private boolean mAddAppGoStoreTitleEntrance = false;
	/**
	 * 是否需要在GO精品列表添加应用中心入口
	 */
	private boolean mAddAppGoStoreListEntrance = false;

	// 装机必备
	/**
	 * 是否需要装机必备
	 */
	private boolean mNeedAppsKit = false;
	/**
	 * 是否需要在桌面推荐里面添加装机必备的推荐图标
	 */
	private boolean mAddAppsKitDeskItem = false;

	// 游戏中心
	/**
	 * 是否需要游戏中心
	 */
	private boolean mNeedGameCenter = false;
	/**
	 * 是否需要在桌面主菜单添加游戏中心入口
	 */
	private boolean mAddGameMainMenuItem = false;
	/**
	 * 是否需要在功能表添加游戏中心假图标
	 */
	private boolean mAddGameFunItem = false;
	/**
	 * 是否需要在功能表菜单添加游戏中心入口
	 */
	private boolean mAddGameFunMenuItem = false;
	/**
	 * 是否需要在GO精品标题添加游戏中心入口
	 */
	private boolean mAddGameGoStoreTitleEntrance = false;
	/**
	 * 是否需要在GO精品列表添加游戏中心入口
	 */
	private boolean mAddGameGoStoreListEntrance = false;
	/**
	 * 应用中心游戏是否需要安全验证加载页面
	 */
	private boolean mNeedAppGameSecurityLoading = false;

	// 玩机必备
	/**
	 * 是否需要玩机必备
	 */
	private boolean mNeedGamesKit = false;
	/**
	 * 是否需要在桌面推荐里面添加玩机必备的推荐图标
	 */
	private boolean mAddGamesKitDeskItem = false;

	/**
	 * 是否允许使用长连接
	 * 目前全部渠道均打开长连接，不在客户端做控制，改为服务器端控制
	 */
	private boolean mKeepAliveEnable = false;

	// //////////////////////////与应用游戏中心相关的配置信息--END

	/**
	 * 是否需要下载服务，目前非200，210都需要
	 */
	private boolean mNeedDownloadService = true;
	
	/**
	 * 是否需要安装包管理
	 */
	private boolean mNeedPackageManagement = true;

	/**
	 * 是否需要内购的服务，目前只有200和210的需要，其他的都不要
	 */
	private boolean mNeedBillingService = false;

	/**
	 * 是否需要省流量提醒对话框,目前只有200渠道不需要
	 */
	private boolean mNeedShowSaveFlow = true;

	private ChannelConfig(Context context) {
		mParser = ChannelConfigParser.getInstance(context);
	}

	public synchronized static ChannelConfig getInstance(Context context) {
		if (null == sSelf) {
			sSelf = new ChannelConfig(context);
		}
		return sSelf;
	}

	/**
	 * 加载配置信息的方法
	 */
	public void roadConfig() {
		if (null != mParser) {
			mParser.parse(this);
		}
	}

	public boolean isNeedAppCenter() {
		return mNeedAppCenter;
	}

	public void setNeedAppCenter(boolean needAppCenter) {
		this.mNeedAppCenter = needAppCenter;
		if (!needAppCenter) {
			// 如果设置不需要应用中心时，默认将与应用中心相关的项设置为false
			mAddAppMainMenuItem = false;
			mAddAppFunItem = false;
			mAddAppFunMenuItem = false;
			mAddAppGoStoreTitleEntrance = false;
			mAddAppGoStoreListEntrance = false;
		}
	}

	public boolean isAddAppMainMenuItem() {
		return mAddAppMainMenuItem;
	}

	public void setAddAppMainMenuItem(boolean addAppMainMenuItem) {
		this.mAddAppMainMenuItem = addAppMainMenuItem;
	}

	public boolean isAddAppFunItem() {
		return mAddAppFunItem;
	}

	public void setAddAppFunItem(boolean addAppFunItem) {
		this.mAddAppFunItem = addAppFunItem;
	}

	public boolean isAddAppFunMenuItem() {
		return mAddAppFunMenuItem;
	}

	public void setAddAppFunMenuItem(boolean addAppFunMenuItem) {
		this.mAddAppFunMenuItem = addAppFunMenuItem;
	}

	public boolean isAddAppGoStoreTitleEntrance() {
		return mAddAppGoStoreTitleEntrance;
	}

	public void setAddAppGoStoreTitleEntrance(boolean addAppGoStoreTitleEntrance) {
		this.mAddAppGoStoreTitleEntrance = addAppGoStoreTitleEntrance;
	}

	public boolean isAddAppGoStoreListEntrance() {
		return mAddAppGoStoreListEntrance;
	}

	public void setAddAppGoStoreListEntrance(boolean addAppGoStoreListEntrance) {
		this.mAddAppGoStoreListEntrance = addAppGoStoreListEntrance;
	}

	public boolean isNeedAppsKit() {
		return mNeedAppsKit;
	}

	public void setNeedAppsKit(boolean needAppsKit) {
		this.mNeedAppsKit = needAppsKit;
		if (!needAppsKit) {
			// 如果设置不需要装机必备时，默认将与装机必备相关的项设置为false
			mAddAppsKitDeskItem = false;
		}
	}

	public boolean isAddAppsKitDeskItem() {
		return mAddAppsKitDeskItem;
	}

	public void setAddAppsKitDeskItem(boolean addAppsKitDeskItem) {
		this.mAddAppsKitDeskItem = addAppsKitDeskItem;
	}

	public boolean isNeedGameCenter() {
		return mNeedGameCenter;
	}

	public void setNeedGameCenter(boolean needGameCenter) {
		this.mNeedGameCenter = needGameCenter;
		if (!needGameCenter) {
			// 如果设置不需要游戏中心时，默认将与游戏中心相关的项设置为false
			mAddGameMainMenuItem = false;
			mAddGameFunItem = false;
			mAddGameFunMenuItem = false;
			mAddGameGoStoreTitleEntrance = false;
			mAddGameGoStoreListEntrance = false;
		}
	}

	public boolean isAddGameMainMenuItem() {
		return mAddGameMainMenuItem;
	}

	public void setAddGameMainMenuItem(boolean addGameMainMenuItem) {
		this.mAddGameMainMenuItem = addGameMainMenuItem;
	}

	public boolean isAddGameFunItem() {
		return mAddGameFunItem;
	}

	public void setAddGameFunItem(boolean addGameFunItem) {
		this.mAddGameFunItem = addGameFunItem;
	}

	public boolean isAddGameFunMenuItem() {
		return mAddGameFunMenuItem;
	}

	public void setAddGameFunMenuItem(boolean addGameFunMenuItem) {
		this.mAddGameFunMenuItem = addGameFunMenuItem;
	}

	public boolean isAddGameGoStoreTitleEntrance() {
		return mAddGameGoStoreTitleEntrance;
	}

	public void setAddGameGoStoreTitleEntrance(boolean addGameGoStoreTitleEntrance) {
		this.mAddGameGoStoreTitleEntrance = addGameGoStoreTitleEntrance;
	}

	public boolean isAddGameGoStoreListEntrance() {
		return mAddGameGoStoreListEntrance;
	}

	public void setAddGameGoStoreListEntrance(boolean addGameGoStoreListEntrance) {
		this.mAddGameGoStoreListEntrance = addGameGoStoreListEntrance;
	}

	public boolean isNeedGamesKit() {
		return mNeedGamesKit;
	}

	public void setNeedGamesKit(boolean needGamesKit) {
		this.mNeedGamesKit = needGamesKit;
		if (!needGamesKit) {
			// 如果设置不需要玩机必备时，默认将与玩机必备相关的项设置为false
			mAddGamesKitDeskItem = false;
		}
	}

	public boolean isAddGamesKitDeskItem() {
		return mAddGamesKitDeskItem;
	}

	public void setAddGamesKitDeskItem(boolean addGamesKitDeskItem) {
		this.mAddGamesKitDeskItem = addGamesKitDeskItem;
	}

	public boolean isNeedDownloadManager() {
		return mNeedDownloadManager;
	}

	public void setNeedDownloadManager(boolean needDownloadManager) {
		this.mNeedDownloadManager = needDownloadManager;
	}

	public boolean isNeedAppGameSecurityLoading() {
		return mNeedAppGameSecurityLoading;
	}

	public void setNeedAppGameSecurityLoading(boolean needSecurityLoading) {
		this.mNeedAppGameSecurityLoading = needSecurityLoading;
	}

	public boolean isNeedDownloadService() {
		return mNeedDownloadService;
	}

	public void setNeedDownloadService(boolean needDownloadService) {
		this.mNeedDownloadService = needDownloadService;
	}

	public boolean isNeedBillingService() {
		return mNeedBillingService;
	}

	public void setNeedBillingService(boolean needBillingService) {
		this.mNeedBillingService = needBillingService;
	}

	public boolean isKeepAliveEnable() {
		return mKeepAliveEnable;
	}

	public void setKeepAliveEnable(boolean keepAliveEnable) {
		mKeepAliveEnable = keepAliveEnable;
	}
	
	public void setNeedPackageManagement(boolean needPackage) {
		mNeedPackageManagement = needPackage;
	}
	
	public boolean isNeedPackageManagement() {
		return mNeedPackageManagement;
	}

	public boolean isNeedShowSaveFlow() {
		return mNeedShowSaveFlow;
	}

	public void setShowSaveFlow(boolean showSaveFlow) {
		this.mNeedShowSaveFlow = showSaveFlow;
	}
		
}
