package com.jiubang.ggheart.apps.config;

import java.io.Serializable;

import android.content.Context;

/**
 * 整个GO桌面的配置信息类
 * 
 * @author wangzhuobin
 * 
 */
@SuppressWarnings("serial")
public class GOLauncherConfig implements Serializable {

	/**
	 * 渠道配置信息类
	 */
	private ChannelConfig mChannelConfig = null;

	/**
	 * 单例
	 */
	private static GOLauncherConfig sSelf = null;

	private GOLauncherConfig(Context context) {
		mChannelConfig = ChannelConfig.getInstance(context);
	}

	public synchronized static GOLauncherConfig getInstance(Context context) {
		if (null == sSelf) {
			sSelf = new GOLauncherConfig(context);
		}
		return sSelf;
	}

	/**
	 * 加载配置信息的方法
	 */
	public void roadConfig() {
		if (null != mChannelConfig) {
			mChannelConfig.roadConfig();
		}
	}

	public ChannelConfig getChannelConfig() {
		return mChannelConfig;
	}

	public void setChannelConfig(ChannelConfig channelConfig) {
		this.mChannelConfig = channelConfig;
	}

}
