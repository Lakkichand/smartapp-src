package com.jiubang.go.backup.pro.net.sync;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences.Editor;

import com.jiubang.go.backup.pro.PreferenceManager;
import com.jiubang.go.backup.pro.model.BackupManager;
import com.jiubang.go.backup.pro.util.Util;

/**
 * @author maiyongshen
 *
 */
public class CloudServiceManager {
	private static CloudServiceManager sInstance = null;
	private static Map<Integer, FileHostingServiceProvider> sPool = new HashMap<Integer, FileHostingServiceProvider>();
	private FileHostingServiceProvider mCurrentService;

	private CloudServiceManager() {

	}

	public synchronized static CloudServiceManager getInstance() {
		if (sInstance == null) {
			sInstance = new CloudServiceManager();
		}
		return sInstance;
	}

	public static FileHostingServiceProvider getService(Context context, int type) {
		FileHostingServiceProvider service = sPool.get(type);
		if (service != null) {
			return service;
		}
		switch (type) {
			case FileHostingServiceProvider.DROPBOX:
				service = new DropboxManager(context);
				break;
			case FileHostingServiceProvider.GOOGLE_DRIVE:
				service = new GoogleDriveManager(context);
				break;
			default: break;
		}
		if (service != null) {
			sPool.put(type, service);
		}
		return service;
	}

	public FileHostingServiceProvider getCurrentService() {
		return mCurrentService;
	}

	public FileHostingServiceProvider switchService(Context context, int type) {
		if (mCurrentService != null && mCurrentService.getType() == type) {
			return mCurrentService;
		}
		mCurrentService = getService(context, type);
		PreferenceManager.getInstance().putInt(context,
				PreferenceManager.KEY_NETWORK_BACKUP_TYPE, type);
		return mCurrentService;
	}

	public void logout(Context context, FileHostingServiceProvider service) {
		if (service != null) {
			service.logout(context);
			clearCacheFiles(context, service);
			sPool.remove(service.getType());
		}
		if (mCurrentService == service) {
			mCurrentService = null;
		}
		Editor editor = PreferenceManager.getInstance().getEditor(context);
		if (editor != null) {
			editor.remove(PreferenceManager.KEY_NETWORK_BACKUP_TYPE);
			editor.commit();
		}
	}
	
	public static void clearCacheFiles(Context context, FileHostingServiceProvider service) {
		if (context == null || service == null) {
			return;
		}
		File cacheDbFile = BackupManager.getOnlineBackupCacheDbFile(context, service);
		if (cacheDbFile != null && cacheDbFile.exists()) {
			Util.deleteFile(cacheDbFile.getAbsolutePath());
		}
	}

}
