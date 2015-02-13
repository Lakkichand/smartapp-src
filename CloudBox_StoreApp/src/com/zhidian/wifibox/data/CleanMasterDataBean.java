package com.zhidian.wifibox.data;

import java.util.ArrayList;
import java.util.List;

/**
 * 手机清理数据封装
 * 
 * @author xiedezhi
 * 
 */
public class CleanMasterDataBean {

	public List<CacheBean> cacheList = new ArrayList<CacheBean>();
	public List<RAMBean> ramList = new ArrayList<RAMBean>();
	public List<APKBean> apkList1 = new ArrayList<APKBean>();
	public List<TrashBean> trashList1 = new ArrayList<TrashBean>();
	public List<APKBean> apkList2 = new ArrayList<APKBean>();
	public List<TrashBean> trashList2 = new ArrayList<TrashBean>();
	public List<APKBean> apkList3 = new ArrayList<APKBean>();
	public List<TrashBean> trashList3 = new ArrayList<TrashBean>();

	public List<APKBean> getAPKList() {
		List<APKBean> ret = new ArrayList<APKBean>();
		if (apkList1 != null) {
			ret.addAll(apkList1);
		}
		if (apkList2 != null) {
			ret.addAll(apkList2);
		}
		if (apkList3 != null) {
			ret.addAll(apkList3);
		}
		return ret;
	}

	public List<TrashBean> getTrashList() {
		List<TrashBean> ret = new ArrayList<TrashBean>();
		if (trashList1 != null) {
			ret.addAll(trashList1);
		}
		if (trashList2 != null) {
			ret.addAll(trashList2);
		}
		if (trashList3 != null) {
			ret.addAll(trashList3);
		}
		return ret;
	}

	/**
	 * 缓存
	 */
	public static class CacheBean implements Comparable<CacheBean> {
		public String name;
		public String pkgName;
		public long cache;
		public String cache_str;
		public boolean isSelect;

		@Override
		public int compareTo(CacheBean another) {
			if (this.isSelect && !another.isSelect) {
				return -1;
			}
			if (!this.isSelect && another.isSelect) {
				return 1;
			}
			if (this.cache == another.cache) {
				return 0;
			}
			return (this.cache - another.cache) > 0 ? -1 : 1;
		}
	}

	/**
	 * 安装包
	 */
	public static class APKBean implements Comparable<APKBean> {
		public String path;
		public long size;
		public String size_str;
		public String name;
		public String pkgName;
		public int versionCode;
		public boolean damage;
		public boolean isSelect;

		@Override
		public int compareTo(APKBean another) {
			if (this.isSelect && !another.isSelect) {
				return -1;
			}
			if (!this.isSelect && another.isSelect) {
				return 1;
			}
			if (this.size == another.size) {
				return 0;
			}
			return (this.size - another.size) > 0 ? -1 : 1;
		}
	}

	/**
	 * 内存加速
	 */
	public static class RAMBean implements Comparable<RAMBean> {
		public String name;
		public String pkgName;
		public long ram;
		public String ram_str;
		public boolean isSelect;

		@Override
		public int compareTo(RAMBean another) {
			if (this.isSelect && !another.isSelect) {
				return -1;
			}
			if (!this.isSelect && another.isSelect) {
				return 1;
			}
			if (this.ram == another.ram) {
				return 0;
			}
			return (this.ram - another.ram) > 0 ? -1 : 1;
		}
	}

	/**
	 * 残留文件
	 */
	public static class TrashBean {
		public long size;
		public String path;
		/**
		 * 1缩略图，2空文件夹，3临时文件，4日志文件
		 */
		public int type;
		public boolean isSelect;
	}

}
