package com.jiubang.ggheart.apps.gowidget.gostore.util.url;

import java.util.HashMap;

import android.content.Context;

import com.jiubang.ggheart.appgame.download.IAidlDownloadListener;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 */
public interface IUrlOperator {
	public boolean handleUrl(Context context, HashMap<Integer, String> urlHashMap);

	public boolean handleUrl(Context context, HashMap<Integer, String> urlHashMap,
			Class<? extends IAidlDownloadListener.Stub>[] listenerClazzArray,
			String customDownloadFileName);

	public boolean handleUrl(Context context, HashMap<Integer, String> urlHashMap,
			Class<? extends IAidlDownloadListener.Stub>[] listenerClazzArray,
			String customDownloadFileName, int iconType, String iconUrlInfo, int module);
}
