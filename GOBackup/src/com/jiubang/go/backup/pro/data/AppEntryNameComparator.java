package com.jiubang.go.backup.pro.data;

/**
 * 应用程序名称比较
 * 
 * @author wencan
 */
public class AppEntryNameComparator extends AppEntryComparator<BaseEntry> {
	@Override
	public int compare(BaseEntry lhs, BaseEntry rhs) {
		AppInfo lhsAppInfo = ((AppEntry) lhs).getAppInfo();
		AppInfo rhsAppInfo = ((AppEntry) rhs).getAppInfo();
		return new AppInfoNameComparator().compare(lhsAppInfo, rhsAppInfo);
	}
}