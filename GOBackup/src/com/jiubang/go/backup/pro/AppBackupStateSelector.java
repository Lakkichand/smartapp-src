package com.jiubang.go.backup.pro;

import android.content.Context;
import android.text.TextUtils;

import com.jiubang.go.backup.pro.data.AppInfo;
import com.jiubang.go.backup.pro.data.AppStateSelector;
import com.jiubang.go.backup.pro.model.BackupManager;

/**
 * @author maiyongshen
 *
 */
public class AppBackupStateSelector extends AppStateSelector {
	public static final int UNBACKUPED = 0x0001;
	public static final int UPDATABLE = 0x0001 << 1;
	public static final int BACKUPED = 0x0001 << 2;
	
	public AppBackupStateSelector() {
		super();
	}
	
	public AppBackupStateSelector(AppBackupStateSelector value) {
		super(value);
	}
	
	public AppBackupStateSelector(int selectType) {
		super(selectType);
	}
	
	public boolean isUnbackupedSelected() {
		return (mFlag & UNBACKUPED) > 0;
	}

	public boolean isUpdatableSelected() {
		return (mFlag & UPDATABLE) > 0;
	}

	public boolean isBackupedSelected() {
		return (mFlag & BACKUPED) > 0;
	}
	
	private AppInfo getLocalAppInfo(Context context, String packageName) {
		if (TextUtils.isEmpty(packageName)) {
			return null;
		}
		return BackupManager.getInstance().getAppInfo(context, packageName);
	}

	@Override
	public boolean match(Context context, AppInfo appInfo) {
/*		if (appInfo == null) {
			return false;
		}
		AppInfo comparatorApp = getLocalAppInfo(context, appInfo.packageName);
		if (isBackupedSelected()) {
			if (comparatorApp != null && appInfo.versionCode >= comparatorApp.versionCode) {
				return true;
			}
		}
		
		if (isUnbackupedSelected()) {
			if (appInfo == comparatorApp) {
				return true;
			}
		}
		
		if (isUpdatableSelected()) {
			if (comparatorApp != null && appInfo.versionCode < comparatorApp.versionCode) {
				return true;
			}
		}*/
		
		return false;
	}

}
