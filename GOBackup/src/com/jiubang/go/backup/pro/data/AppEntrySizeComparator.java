package com.jiubang.go.backup.pro.data;

/**
 * 应用程序大小比较
 * 
 * @author wencan
 */
public class AppEntrySizeComparator extends AppEntryComparator<BaseEntry> {
	// 降序
	@Override
	public int compare(BaseEntry lhs, BaseEntry rhs) {
		long size1 = lhs.getSpaceUsage();
		long size2 = rhs.getSpaceUsage();
		return size1 > size2 ? -1 : size1 < size2 ? 1 : 0;
	}
}
