package com.jiubang.go.backup.pro.data;

import android.content.Context;
import android.text.TextUtils;

import com.jiubang.go.backup.pro.model.BackupManager;



/**
 * @author maiyongshen
 *
 */
public class AppInstallStateSelector extends AppStateSelector {
	public static final int UNINSTALLED = 0x0001;
	public static final int UPDATABLE = 0x0001 << 1;
	public static final int INSTALLED = 0x0001 << 2;
	
	public AppInstallStateSelector() {
		super();
	}
	
	public AppInstallStateSelector(int selectType) {
		super(selectType);
	}
	
	public AppInstallStateSelector(AppInstallStateSelector value) {
		super(value);
	}

	public boolean isUninstalledSelected() {
		return (mFlag & UNINSTALLED) > 0;
	}

	public boolean isUpdatableSelected() {
		return (mFlag & UPDATABLE) > 0;
	}

	public boolean isInstalledSelected() {
		return (mFlag & INSTALLED) > 0;
	}
	
	public void enableAll() {
		enableSelectApp(UNINSTALLED);
		enableSelectApp(INSTALLED);
		enableSelectApp(UPDATABLE);
	}
	
	public boolean isValid() {
		return mFlag > 0;
	}
	
	private AppInfo getLocalAppInfo(Context context, String packageName) {
		if (TextUtils.isEmpty(packageName)) {
			return null;
		}
		return BackupManager.getInstance().getAppInfo(context, packageName);
	}
	
	@Override
	public boolean match(Context context, AppInfo appInfo) {
		if (appInfo == null) {
			return false;
		}
		final AppInfo comparatorApp = getLocalAppInfo(context, appInfo.packageName);
		if (isUninstalledSelected() && comparatorApp == null) {
			return true;
		}
		if (isInstalledSelected() && comparatorApp != null && comparatorApp.versionCode >= appInfo.versionCode) {
			return true;
		}
		if (isUpdatableSelected()) {
			if (comparatorApp != null && appInfo.versionCode > comparatorApp.versionCode) {
				return true;
			}
		}
		return false;
	}
}
