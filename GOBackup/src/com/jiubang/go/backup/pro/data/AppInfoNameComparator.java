package com.jiubang.go.backup.pro.data;

import java.text.Collator;
import java.util.Comparator;

/**
 * 应用程序Info名称比较
 * 
 * @author wencan
 */
public class AppInfoNameComparator implements Comparator<AppInfo> {
	private final Collator mCollator = Collator.getInstance();

	// 升序排序
	@Override
	public int compare(AppInfo lhs, AppInfo rhs) {
		String lname = lhs.appName == null ? lhs.packageName : lhs.appName;
		String rname = rhs.appName == null ? rhs.packageName : rhs.appName;
		return mCollator.compare(lname, rname);
	}
}
