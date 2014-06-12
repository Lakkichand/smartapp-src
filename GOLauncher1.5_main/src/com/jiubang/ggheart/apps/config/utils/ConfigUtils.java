package com.jiubang.ggheart.apps.config.utils;

import android.content.Intent;

import com.jiubang.ggheart.apps.config.ChannelConfig;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.FunAppItemInfo;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 配置信息的工具类
 * 
 * @author wangzhuobin
 * 
 */
public class ConfigUtils {

	/**
	 * 是否需要检查功能表有没有应用游戏中心假图标的方法 只有当本安装包的渠道不需要在功能表添加应用游戏中心假图标的时候需要检查
	 * 主要是避免用户使用有假图标的渠道包升级到没有假图标的渠道包时，假图标仍然存在
	 * 
	 * @return
	 */
	public static boolean isNeedCheckAppGameInFunItemByChannelConfig() {
		boolean result = false;
		ChannelConfig channelConfig = GOLauncherApp.getChannelConfig();
		if (channelConfig != null) {
			result = !channelConfig.isAddAppFunItem() || !channelConfig.isAddGameFunItem();
		}
		return result;
	}

	/**
	 * 根据参数，判断是不是应用中心或者游戏中心，再结合配置文件信息，检查是否需要移除对应数据的方法
	 * 
	 * @param intent
	 * @return
	 */
	public static boolean isNeedRemoveAppGameFromFunByChannelConfig(AppItemInfo appItemInfo) {
		boolean result = false;
		if (appItemInfo != null) {
			result = isNeedRemoveAppGameFromFunByChannelConfig(appItemInfo.mIntent);
		}
		return result;
	}

	/**
	 * 根据参数，判断是不是应用中心或者游戏中心，再结合配置文件信息，检查是否需要移除对应数据的方法
	 * 
	 * @param intent
	 * @return
	 */
	public static boolean isNeedRemoveAppGameFromFunByChannelConfig(FunAppItemInfo funAppItemInfo) {
		boolean result = false;
		if (funAppItemInfo != null) {
			result = isNeedRemoveAppGameFromFunByChannelConfig(funAppItemInfo.getIntent());
		}
		return result;
	}

	/**
	 * 根据参数，判断是不是应用中心或者游戏中心，再结合配置文件信息，检查是否需要移除对应数据的方法
	 * 
	 * @param intent
	 * @return
	 */
	public static boolean isNeedRemoveAppGameFromFunByChannelConfig(Intent intent) {
		boolean result = false;
		if (intent != null) {
			ChannelConfig channelConfig = GOLauncherApp.getChannelConfig();
			if (channelConfig != null) {
				result = (ICustomAction.ACTION_FUNC_SHOW_RECOMMENDCENTER.equals(intent.getAction()) && !channelConfig
						.isAddAppFunItem())
						|| (ICustomAction.ACTION_FUNC_SHOW_GAMECENTER.equals(intent.getAction()) && !channelConfig
								.isAddGameFunItem());
			}

		}
		return result;
	}
}
