package com.jiubang.go.backup.pro.data;

import java.util.Comparator;

/**
 * 抽象类 应用程序比较
 * 
 * @author wencan
 * @param <T>
 */
public abstract class AppEntryComparator<T extends BaseEntry> implements Comparator<T> {

	/**
	 * 排序类型
	 * 
	 * @author wencan
	 */
	public enum SORT_TYPE {
		SORT_BY_APP_NAME, SORT_BY_APP_SIZE, SORT_BY_APP_INSTALL_STATE, SORT_BY_APP_INSTALL_TIME, SORT_ONLINE_BY_APP_SIZE, SORT_ONLINE_BY_APP_NAME
	};
}