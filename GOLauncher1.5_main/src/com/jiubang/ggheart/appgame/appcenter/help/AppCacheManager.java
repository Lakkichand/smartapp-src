/*
 * 文 件 名:  AppCacheManager.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liguoliang
 * 修改时间:  2012-10-9
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.appgame.appcenter.help;

import java.util.List;

import com.gau.utils.cache.CacheManager;
import com.gau.utils.cache.ICacheListener;
import com.gau.utils.cache.impl.FileCacheImpl;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  liguoliang
 * @date  [2012-10-9]
 */
public class AppCacheManager {
	private CacheManager mCacheManager;

	private static AppCacheManager sInstance;

	private AppCacheManager() {
		mCacheManager = new CacheManager(new FileCacheImpl(
				LauncherEnv.Path.SAVE_CLASSIFICATION_APP_INFO_PATH));
	}

	public synchronized static AppCacheManager getInstance() {
		if (sInstance == null) {
			sInstance = new AppCacheManager();
		}
		return sInstance;
	}

	public void saveCache(String key, byte[] cacheData) {
		if (mCacheManager != null) {
			mCacheManager.saveCache(key, cacheData);
		}
	}

	public void saveCacheAsync(String key, byte[] cacheData, ICacheListener listener) {
		if (mCacheManager != null) {
			mCacheManager.saveCacheAsync(key, cacheData, listener);
		}
	}

	public byte[] loadCache(String key) {
		if (mCacheManager != null) {
			return mCacheManager.loadCache(key);
		}
		return null;
	}

	public void loadCacheAsync(String key, ICacheListener listener) {
		if (mCacheManager != null) {
			mCacheManager.loadCacheAsync(key, listener);
		}
	}

	public void clearCache(String key) {
		if (mCacheManager != null) {
			mCacheManager.clearCache(key);
		}
	}

	public void clearCache(List<String> keyList) {
		if (mCacheManager != null) {
			mCacheManager.clearCache(keyList);
		}
	}

	public boolean isCacheExist(String key) {
		if (mCacheManager != null) {
			return mCacheManager.isCacheExist(key);
		}
		return false;
	}

	public String buildKey(String module, String extra) {
		if (mCacheManager != null) {
			return mCacheManager.buildKey(module, extra);
		}
		return null;
	}

	public void saveModuleKey(String module, String extra) {
		if (mCacheManager != null) {
			mCacheManager.saveModuleKey(module, extra);
		}
	}

	public List<String> getModuleKeyList(String module) {
		if (mCacheManager != null) {
			return mCacheManager.getModuleKeyList(module);
		}
		return null;
	}

	public void clearModuleKeyList(String module) {
		if (mCacheManager != null) {
			mCacheManager.clearModuleKeyList(module);
		}
	}

	public void clearModuleKey(String module, String extra) {
		if (mCacheManager != null) {
			mCacheManager.clearModuleKey(module, extra);
		}
	}
}
