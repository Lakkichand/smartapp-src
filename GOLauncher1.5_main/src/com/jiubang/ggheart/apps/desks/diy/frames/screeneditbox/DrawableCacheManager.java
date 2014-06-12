package com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.graphics.drawable.Drawable;

/**
 * 
 * 类描述:Drawable缓存管理类 功能详细描述:缓存添加模块几个tab使用的图片
 * 
 * @author guoyiqing
 * @date [2012-8-9]
 */
public class DrawableCacheManager {

	public static final String CACHE_WALLPAPERSUBTAB = "cache_wallpapersubtab";
	public static final String CACHE_DESKTOPTHEMETAB = "cache_desktopthemetab";
	public static final String CACHE_LOCKERTHEMETAB = "cache_lockerthemetab";
	public static final String CACHE_WIDGETSUBTAB = "cache_widgetsubtab";
	public static final String CACHE_WIDGETTAB = "cache_widgettab";
	private HashMap<String, WeakReference<Drawable>> mDrawableCache;
	private static final int MAX_CACHE_COUNT = 50;
	private static DrawableCacheManager sManager;
	private List<String> mKeyList;

	private DrawableCacheManager() {
		mDrawableCache = new HashMap<String, WeakReference<Drawable>>(MAX_CACHE_COUNT);
		mKeyList = new ArrayList<String>(MAX_CACHE_COUNT);
	}

	public static synchronized DrawableCacheManager getInstance() {
		if (sManager == null) {
			sManager = new DrawableCacheManager();
		}
		return sManager;
	}

	/**
	 * 功能简述:从缓存中拿到Drawable 注意:使用前请判断是否是NULL
	 * 
	 * @param resKey
	 * @return
	 */
	public Drawable getDrawableFromCache(String resKey) {
		WeakReference<Drawable> drawableRef = mDrawableCache.get(resKey);
		if (drawableRef != null) {
			return drawableRef.get();
		}
		return null;
	}

	/**
	 * 功能简述:把当前Drawable放入缓存 注意:当缓存数量达到阀值时会清除时间较早的缓存
	 * 
	 * @param resKey
	 * @param drawable
	 */
	public void saveToCache(String resKey, Drawable drawable) {
		if (mDrawableCache.size() >= MAX_CACHE_COUNT) {
			mDrawableCache.remove(mKeyList.get(0));
			mKeyList.remove(0);
		}
		mDrawableCache.put(resKey, new WeakReference<Drawable>(drawable));
		mKeyList.add(resKey);
	}

	public void clearCache() {
		mDrawableCache.clear();
		mKeyList.clear();
	}

}
