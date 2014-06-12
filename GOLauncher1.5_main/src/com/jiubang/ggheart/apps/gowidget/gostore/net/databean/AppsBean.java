package com.jiubang.ggheart.apps.gowidget.gostore.net.databean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

/**
 * 
 * @author zhujian 数据bean
 */

public class AppsBean extends BaseBean implements Cloneable, Serializable {

	private static final long serialVersionUID = -5675550838235216609L;

	public static final String KEY = "com_golauncher_appfunc_app_update_key";

	public String mDetailBaseUrl = null;
	public String mImgBaseUrl = null;
	// add by zhoujun 2012-05-14 添加控制开关
	public int mControlNum = 0;
	public HashMap<Integer, Byte> mControlcontrolMap = null;
	// add by zhoujun 2012-05-14 添加控制开关 end
	public ArrayList<AppBean> mListBeans = new ArrayList<AppsBean.AppBean>();

	/**
	 * 
	 * @author zhujian 数据bean
	 */
	public class AppBean implements Serializable {
		private static final long serialVersionUID = 2926837923355162815L;
		public int mAppId = 0; // 应用id
		public String mPkgName = null; // 包名
		public int mSource = 0; // 来源id 【0：精品 1：木瓜】
		public String mCallbackUrl = ""; // 回调URL
		public String mUpdateTime = null; // 更新时间
		public String mAppName = null; // 程序名
		// public String mWebMarket = null; //web版market地址
		// public String mIconId = null; //图标id
		public String mUpdateLog = null; // 应用更新说明
		public String mAppSize;
		/**
		 * 增量更新大小
		 */
		public String mAppDeltaSize;
		/**
		 * 增量更新包下载地址
		 */
		public String mXdeltaUrl = null;
		/**
		 * 是否可以增量更新 
		 * 0 可以     1 不可以
		 */
		public int mIsXdelta = 0;
		public String mVersionName;
		public boolean mIsOpen = false; // 用于判断弹窗是否显示
		public int mUrlNum = 0;
		public HashMap<Integer, String> mUrlMap = null; // 链接地址
		public static final int STATUS_NORMAL = 0;
		public static final int STATUS_WAITING_DOWNLOAD = 1;
		public static final int STATUS_DOWNLOADING = 2;
		public static final int STATUS_DOWNLOAD_COMPLETED = 3;
		public static final int STATUS_DOWNLOAD_FAILED = 4;
		public static final int STATUS_GET_READY = 5;
		public static final int STATUS_CANCELING = 6;
		public static final int STATUS_STOP = 7;
		private int mStatus = STATUS_NORMAL;

		private String mFilePath;
		private long mAlreadyDownloadSize;
		private int mPercent;
		private IAppBeanStatusChangeListener mStatusChangeListener;
		private IAppBeanDownloadListener mDownloadListener;
		
		public boolean mIsIngore;   //是否忽略更新

		public int getStatus() {
			return mStatus;
		}

		public void setStatus(int status) {
			this.mStatus = status;
		}

		public void setFilePath(String filePath) {
			mFilePath = filePath;
		}

		public String getFilePath() {
			return mFilePath;
		}

		public void setAlreadyDownloadSize(long size) {
			mAlreadyDownloadSize = size;
		}

		public long getAlreadyDownloadSize() {
			return mAlreadyDownloadSize;
		}

		public void setAlreadyDownloadPercent(int percent) {
			mPercent = percent;
		}

		public int getAlreadyDownloadPercent() {
			return mPercent;
		}

		public String getAppName(PackageManager pkgMgr) {
			if (mAppName == null) {
				try {
					ApplicationInfo info = pkgMgr.getApplicationInfo(mPkgName,
							0);
					if (info != null
							&& pkgMgr.getApplicationLabel(info) != null) {
						mAppName = pkgMgr.getApplicationLabel(info).toString();
					}
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				}
			}
			return mAppName;
		}

		public void setAppBeanStatusChangeListener(
				IAppBeanStatusChangeListener listener) {
			mStatusChangeListener = listener;
		}

		public void setAppBeanDownloadListener(IAppBeanDownloadListener listener) {
			mDownloadListener = listener;
		}
	}

	@Override
	public AppsBean clone() throws CloneNotSupportedException {
		AppsBean bean = new AppsBean();
		bean.mDetailBaseUrl = this.mDetailBaseUrl;
		bean.mFunId = this.mFunId;
		bean.mImgBaseUrl = this.mImgBaseUrl;
		bean.mLength = this.mLength;
		bean.mTimeStamp = this.mTimeStamp;
		if (this.mListBeans != null) {
			bean.mListBeans = (ArrayList<AppBean>) this.mListBeans.clone();
		} else {
			bean.mListBeans = null;
		}

		return bean;
	}

	/**
	 * 
	 * @author zhujian 数据bean接口
	 */
	public interface IAppBeanStatusChangeListener {
		public void onStatusChanged(int status);
	}

	/**
	 * 
	 * @author zhujian 数据bean接口
	 */
	public interface IAppBeanDownloadListener {
		public void onDownload(long alreadyDownloadSize, int percent);
	}
}
