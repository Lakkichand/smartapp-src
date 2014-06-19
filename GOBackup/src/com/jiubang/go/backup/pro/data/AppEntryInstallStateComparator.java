package com.jiubang.go.backup.pro.data;

import com.jiubang.go.backup.pro.model.BackupManager;

/**
 * 应用程序安装状态比较
 * 
 * @author wencan
 */
public class AppEntryInstallStateComparator extends AppEntryComparator<BaseEntry> {

	@Override
	public int compare(BaseEntry lhs, BaseEntry rhs) {
		final BackupManager bm = BackupManager.getInstance();
		AppInfo lhsAppInfo = ((AppEntry) lhs).getAppInfo();
		AppInfo rhsAppInfo = ((AppEntry) rhs).getAppInfo();
		boolean lhsInstalled = bm.isApplicationInstalled(lhsAppInfo);
		boolean rhsInstalled = bm.isApplicationInstalled(rhsAppInfo);
		if ((lhsInstalled && rhsInstalled) || (!lhsInstalled && !rhsInstalled)) {
			return new AppInfoNameComparator().compare(lhsAppInfo, rhsAppInfo);
		} else if (!lhsInstalled) {
			return -1;
		} else if (!rhsInstalled) {
			return 1;
		}
		return 0;
	}

}
