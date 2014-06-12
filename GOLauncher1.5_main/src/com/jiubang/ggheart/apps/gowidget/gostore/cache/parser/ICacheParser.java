package com.jiubang.ggheart.apps.gowidget.gostore.cache.parser;

import android.content.Context;

import com.gau.utils.net.request.THttpRequest;

public interface ICacheParser {
	/**
	 * 保存页面请求缓存数据的方法
	 * 
	 * @param context
	 * @param cacheFilePath
	 *            缓存数据的保存路径
	 * @param request
	 *            缓存数据对应的THttpRequest
	 * @param object
	 *            要缓存的数据
	 */
	public void saveCacheData(Context context, String cacheFilePath, THttpRequest request,
			Object object);

	/**
	 * 获取页面请求缓存数据的方法
	 * 
	 * @param context
	 * @param cacheFilePath
	 *            缓存数据的保存路径
	 * @param request
	 *            缓存数据对应的THttpRequest
	 * @return
	 */
	public Object getCacheData(Context context, String cacheFilePath, THttpRequest request);

	/**
	 * 清空所有缓存文件的方法
	 * 
	 * @param context
	 */
	public void cleanAllCacheData(Context context);
}
