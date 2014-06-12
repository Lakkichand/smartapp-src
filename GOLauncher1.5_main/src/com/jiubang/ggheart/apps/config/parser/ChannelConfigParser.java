package com.jiubang.ggheart.apps.config.parser;

import java.io.Serializable;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.text.TextUtils;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.config.ChannelConfig;
import com.jiubang.ggheart.data.statistics.Statistics;

/**
 * 渠道配置信息的解释器
 * 
 * @author wangzhuobin
 * 
 */
@SuppressWarnings("serial")
public class ChannelConfigParser implements Serializable {

	/**
	 * 单例
	 */
	private static ChannelConfigParser sSelf = null;

	private Context mContext = null;

	/**
	 * 配置文件标签常量
	 */
	private static final String TAG_CHANNEL = "channel";
	private static final String TAG_APP_CENTER = "appcenter";
	private static final String TAG_ADD_MAINMENU_ITEM = "addMainMenuItem";
	private static final String TAG_ADD_FUN_ITEM = "addFunItem";
	private static final String TAG_ADD_FUNMENU_ITEM = "addFunMenuItem";
	private static final String TAG_ADD_GOSTORE_TITLE_ENTRANCE = "addGoStoreTitleEntrance";
	private static final String TAG_ADD_GOSTORE_LIST_ENTRANCE = "addGoStoreListEntrance";
	private static final String TAG_GAME_CENTER = "gamecenter";
	private static final String TAG_APPSKIT = "appskit";
	private static final String TAG_ADD_DESK_ITEM = "addDeskItem";
	private static final String TAG_GAMESKIT = "gameskit";
	private static final String TAG_DOWNLOADMANAGER = "downloadmanager";
	private static final String TAG_APPGME_SECURITY_LOADING = "securityAppGameLoading";
	private static final String TAG_START_DOWNLOADSERVICE = "downloadservice";
	private static final String TAG_START_BILLINGSERVICE = "billingservice";
	private static final String TAG_NEED_PACKAGEMANAGEMENT = "packageManagement";
	private static final String TAG_KEEP_ALIVE_ENABLE = "keepAliveEnable";
	private static final String TAG_SHOW_SAVE_FLOW = "showSaveFlow";
	
	/**
	 * 配置文件属性常量
	 * 
	 * @param context
	 */
	private static final String ATTRIBUTE_WANT = "want";
	private static final String ATTRIBUTE_UNWANT = "unwant";

	private ChannelConfigParser(Context context) {
		mContext = context;
	}

	public synchronized static ChannelConfigParser getInstance(Context context) {
		if (null == sSelf) {
			sSelf = new ChannelConfigParser(context);
		}
		return sSelf;
	}

	/**
	 * 解释配置文件的方法
	 * 
	 * @param channelConfig
	 */
	public void parse(ChannelConfig channelConfig) {
		if (null == mContext || null == channelConfig) {
			return;
		}
		String uid = Statistics.getUid(mContext);
		if (null == uid) {
			return;
		}
		XmlResourceParser xmlResourceParser = mContext.getResources().getXml(R.xml.channel_config);
		int eventType;
		String tagName = null;
		String parentTag = TAG_CHANNEL;
		try {
			eventType = xmlResourceParser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG) {
					tagName = xmlResourceParser.getName();
					if (tagName.equals(TAG_APP_CENTER)) {
						// 应用中心
						channelConfig.setNeedAppCenter(returnWantAnUnwantResult(xmlResourceParser,
								uid));
						parentTag = TAG_APP_CENTER;
					} else if (tagName.equals(TAG_GAME_CENTER)) {
						// 游戏中心
						channelConfig.setNeedGameCenter(returnWantAnUnwantResult(xmlResourceParser,
								uid));
						parentTag = TAG_GAME_CENTER;
					} else if (tagName.equals(TAG_ADD_MAINMENU_ITEM)) {
						// 桌面主菜单入口
						if (parentTag.equals(TAG_APP_CENTER)) {
							if (channelConfig.isNeedAppCenter()) {
								// 只有在本渠道需要应用中心的时候，才有解释下去的必要
								channelConfig.setAddAppMainMenuItem(returnWantAnUnwantResult(
										xmlResourceParser, uid));
							}
						} else if (parentTag.equals(TAG_GAME_CENTER)) {
							if (channelConfig.isNeedGameCenter()) {
								// 只有在本渠道需要游戏中心的时候，才有解释下去的必要
								channelConfig.setAddGameMainMenuItem(returnWantAnUnwantResult(
										xmlResourceParser, uid));
							}
						}
					} else if (tagName.equals(TAG_ADD_FUN_ITEM)) {
						// 功能表假图标
						if (parentTag.equals(TAG_APP_CENTER)) {
							if (channelConfig.isNeedAppCenter()) {
								// 只有在本渠道需要应用中心的时候，才有解释下去的必要
								channelConfig.setAddAppFunItem(returnWantAnUnwantResult(
										xmlResourceParser, uid));
							}
						} else if (parentTag.equals(TAG_GAME_CENTER)) {
							if (channelConfig.isNeedGameCenter()) {
								// 只有在本渠道需要游戏中心的时候，才有解释下去的必要
								channelConfig.setAddGameFunItem(returnWantAnUnwantResult(
										xmlResourceParser, uid));
							}
						}
					} else if (tagName.equals(TAG_ADD_FUNMENU_ITEM)) {
						// 功能表菜单入口
						if (parentTag.equals(TAG_APP_CENTER)) {
							if (channelConfig.isNeedAppCenter()) {
								// 只有在本渠道需要应用中心的时候，才有解释下去的必要
								channelConfig.setAddAppFunMenuItem(returnWantAnUnwantResult(
										xmlResourceParser, uid));
							}
						} else if (parentTag.equals(TAG_GAME_CENTER)) {
							if (channelConfig.isNeedGameCenter()) {
								// 只有在本渠道需要游戏中心的时候，才有解释下去的必要
								channelConfig.setAddGameFunMenuItem(returnWantAnUnwantResult(
										xmlResourceParser, uid));
							}
						}
					} else if (tagName.equals(TAG_ADD_GOSTORE_TITLE_ENTRANCE)) {
						// GO精品标题入口
						if (parentTag.equals(TAG_APP_CENTER)) {
							if (channelConfig.isNeedAppCenter()) {
								// 只有在本渠道需要应用中心的时候，才有解释下去的必要
								channelConfig
										.setAddAppGoStoreTitleEntrance(returnWantAnUnwantResult(
												xmlResourceParser, uid));
							}
						} else if (parentTag.equals(TAG_GAME_CENTER)) {
							if (channelConfig.isNeedGameCenter()) {
								// 只有在本渠道需要游戏中心的时候，才有解释下去的必要
								channelConfig
										.setAddGameGoStoreTitleEntrance(returnWantAnUnwantResult(
												xmlResourceParser, uid));
							}
						}
					} else if (tagName.equals(TAG_ADD_GOSTORE_LIST_ENTRANCE)) {
						// GO精品列表入口
						if (parentTag.equals(TAG_APP_CENTER)) {
							if (channelConfig.isNeedAppCenter()) {
								// 只有在本渠道需要应用中心的时候，才有解释下去的必要
								channelConfig
										.setAddAppGoStoreListEntrance(returnWantAnUnwantResult(
												xmlResourceParser, uid));
							}
						} else if (parentTag.equals(TAG_GAME_CENTER)) {
							if (channelConfig.isNeedGameCenter()) {
								// 只有在本渠道需要游戏中心的时候，才有解释下去的必要
								channelConfig
										.setAddGameGoStoreListEntrance(returnWantAnUnwantResult(
												xmlResourceParser, uid));
							}
						}
					} else if (tagName.equals(TAG_APPSKIT)) {
						// 装机必备
						channelConfig.setNeedAppsKit(returnWantAnUnwantResult(xmlResourceParser,
								uid));
						parentTag = TAG_APPSKIT;
					} else if (tagName.equals(TAG_GAMESKIT)) {
						// 玩机必备
						channelConfig.setNeedGamesKit(returnWantAnUnwantResult(xmlResourceParser,
								uid));
						parentTag = TAG_GAMESKIT;
					} else if (tagName.equals(TAG_ADD_DESK_ITEM)) {
						// 在桌面推荐添加装机必备和玩机必备
						if (parentTag.equals(TAG_APPSKIT)) {
							if (channelConfig.isNeedAppsKit()) {
								// 只有在本渠道需要装机必备的时候，才有解释下去的必要
								channelConfig.setAddAppsKitDeskItem(returnWantAnUnwantResult(
										xmlResourceParser, uid));
							}
						} else if (parentTag.equals(TAG_GAMESKIT)) {
							if (channelConfig.isNeedGamesKit()) {
								// 只有在本渠道需要玩机必备的时候，才有解释下去的必要
								channelConfig.setAddGamesKitDeskItem(returnWantAnUnwantResult(
										xmlResourceParser, uid));
							}
						}
					} else if (tagName.equals(TAG_DOWNLOADMANAGER)) {
						channelConfig.setNeedDownloadManager(returnWantAnUnwantResult(
								xmlResourceParser, uid));
					} else if (tagName.equals(TAG_APPGME_SECURITY_LOADING)) {
						// 是否需要显示应用游戏中心的安全验证加载页面 {
						if (channelConfig.isNeedAppCenter() || channelConfig.isNeedAppCenter()) {
							// 只有本渠道需要应用中心或游戏中心时，才有解释下去的必要
							channelConfig.setNeedAppGameSecurityLoading(returnWantAnUnwantResult(
									xmlResourceParser, uid));
						}
					} else if (tagName.equals(TAG_START_DOWNLOADSERVICE)) {
						channelConfig.setNeedDownloadService(returnWantAnUnwantResult(
								xmlResourceParser, uid));
					} else if (tagName.equals(TAG_START_BILLINGSERVICE)) {
						channelConfig.setNeedBillingService(returnWantAnUnwantResult(
								xmlResourceParser, uid));
					} else if (tagName.equals(TAG_KEEP_ALIVE_ENABLE)) {
						channelConfig.setKeepAliveEnable(returnWantAnUnwantResult(
								xmlResourceParser, uid));
					} else if (tagName.equals(TAG_SHOW_SAVE_FLOW)) {
						// 省流量模式提醒
						channelConfig.setShowSaveFlow(returnWantAnUnwantResult(
								xmlResourceParser, uid));
					} else if (tagName.equals(TAG_NEED_PACKAGEMANAGEMENT)) {
						// 安装包管理
						channelConfig.setNeedPackageManagement(returnWantAnUnwantResult(
								xmlResourceParser, uid));
					}
				}
				eventType = xmlResourceParser.next();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != xmlResourceParser) {
				xmlResourceParser.close();
			}
		}
	}

	/**
	 * 通过比较want和unwant属性的值，返回最终配置值的方法
	 * 
	 * @param xmlResourceParser
	 * @param uid
	 * @return
	 */
	private boolean returnWantAnUnwantResult(XmlResourceParser xmlResourceParser, String uid) {
		boolean result = false;
		if (xmlResourceParser != null && !TextUtils.isEmpty(uid)) {
			String want = xmlResourceParser.getAttributeValue(null, ATTRIBUTE_WANT);
			String unwant = xmlResourceParser.getAttributeValue(null, ATTRIBUTE_UNWANT);
			// want属性配置为全渠道或者是包含本包的渠道
			// unwant属性不是配置为全渠道并且不包含本包的渠道
			// 则结果为true
			result = (want.equals(ChannelConfig.ALL_CHANNEL_VALUE) || want.contains(uid))
					&& !unwant.equals(ChannelConfig.ALL_CHANNEL_VALUE) && !unwant.contains(uid);
		}
		return result;
	}
}
