package com.zhidian.wifibox.data;

import android.content.pm.ApplicationInfo;

/**
 * 缓存清理数据
 * 
 * @author xiedezhi
 * 
 */
public class CacheDataBean implements Comparable<CacheDataBean> {
	/**
	 * 应用名称
	 */
	public String mName;
	/**
	 * 应用信息
	 */
	public ApplicationInfo mInfo;
	/**
	 * cache大小
	 */
	public long mCache;

	@Override
	public int compareTo(CacheDataBean another) {
		// 根据使用内存排序
		long ret = another.mCache - mCache;
		if (ret == 0) {
			return 0;
		} else if (ret < 0) {
			return -1;
		} else {
			return 1;
		}
	}
}
