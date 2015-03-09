package com.zhidian.wifibox.data;

import java.util.ArrayList;
import java.util.List;

import com.ta.TAApplication;
import com.zhidian.wifibox.util.AppUtils;

/**
 * 手机清理数据封装
 * 
 * @author xiedezhi
 * 
 */
public class CleanMasterDataBean {

	public List<CacheBean> cacheList = new ArrayList<CacheBean>();
	public List<RAMBean> ramList = new ArrayList<RAMBean>();
	public List<APKBean> apkList = new ArrayList<APKBean>();
	public List<TrashBean> trashList = new ArrayList<TrashBean>();
	public List<BigFileBean> bigFileList = new ArrayList<BigFileBean>();

	public List<APKBean> getAPKList() {
		List<APKBean> ret = new ArrayList<APKBean>();
		if (apkList != null) {
			ret.addAll(apkList);
		}
		return ret;
	}

	public List<TrashBean> getTrashList() {
		List<TrashBean> ret = new ArrayList<TrashBean>();
		if (trashList != null) {
			ret.addAll(trashList);
		}
		return ret;
	}

	public List<BigFileBean> getBigFileList() {
		List<BigFileBean> ret = new ArrayList<CleanMasterDataBean.BigFileBean>();
		if (bigFileList != null) {
			ret.addAll(bigFileList);
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
			if (!AppUtils.isSystemApp(TAApplication.getApplication(),
					this.pkgName)
					&& AppUtils.isSystemApp(TAApplication.getApplication(),
							another.pkgName)) {
				return -1;
			}
			if (AppUtils.isSystemApp(TAApplication.getApplication(),
					this.pkgName)
					&& !AppUtils.isSystemApp(TAApplication.getApplication(),
							another.pkgName)) {
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
		 * 1缩略图，2空文件夹，3临时文件，4日志文件，5空白文件
		 */
		public int type;
		public boolean isSelect;
	}

	public static class BigFileBean implements Comparable<BigFileBean> {
		public long size;
		public String path;
		public String show_path;
		public boolean isSelect;

		@Override
		public int compareTo(BigFileBean another) {
			if (this.size == another.size) {
				return 0;
			}
			return (this.size - another.size) > 0 ? -1 : 1;
		}
	}

}
