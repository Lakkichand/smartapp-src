package com.jiubang.go.backup.pro.data;

import java.io.File;

/**
 * 应用程序项安装数据比较
 * 
 * @author wencan
 */
public class AppEntryInstallDateComparator extends AppEntryComparator<BaseEntry> {
	// 降序
	@Override
	public int compare(BaseEntry lhs, BaseEntry rhs) {
		AppInfo lhsAppInfo = ((AppEntry) lhs).getAppInfo();
		AppInfo rhsAppInfo = ((AppEntry) rhs).getAppInfo();
		long date1 = new File(lhsAppInfo.publicSourceDir).lastModified();
		long date2 = new File(rhsAppInfo.publicSourceDir).lastModified();
		return date1 > date2 ? -1 : date1 < date2 ? 1 : 0;
	}
}
