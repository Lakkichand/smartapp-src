/*
 * 文 件 名:  DownloadTask.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-8-16
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.appgame.download;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;

/**
 * <br>
 * 类描述: <br>
 * 功能详细描述:
 * 
 * @author liuxinyang
 * @date [2012-8-16]
 */
public class DownloadTask implements Parcelable {
	// 下载任务状态常量
	public static final int STATE_NEW = 0;
	public static final int STATE_WAIT = 1;
	public static final int STATE_START = 2;
	public static final int STATE_DOWNLOADING = 3;
	public static final int STATE_FAIL = 4;
	public static final int STATE_FINISH = 5;
	public static final int STATE_DELETE = 6;
	public static final int STATE_STOP = 7;
	public static final int STATE_RESTART = 8;
	
	//下载任务状态
	public static final int TASK_STATE_NORMAL = 0; //人为改动状态
	public static final int TASK_STATE_EXCEPTION = 1; //默认状态

	// TODO:wangzhuobin 与业务相关的字段，应该移除掉
	public static final int ICON_TYPE_URL = 101; // 图标的信息类型 ， URL类型
	public static final int ICON_TYPE_ID = 102; // 图标的信息类型 ， ID类型
	public static final int ICON_TYPE_LOCAL = 103; // 图标的信息类型 ，本地程序图标

	// 标识从应用推荐模块进入，点击应用更新
	public static final int DOWNLOAD_TYPE_FOR_APPS_RECOMMEND = 1;

	//下载任务下载时的网络状态，wifi状态，用于网络错误的下载任务重连
	public static final int NETWORK_TYPE_WIFI = 1001;
	//下载任务下载时的网络状态，除wifi的网络状态，用于网络错误的下载任务重连
	public static final int NETWORK_TYPE_OTHER = 1002;

	private long mId; // ID
	private long mDownloadId; // 标识一次下载的ID，每次都不相同
	private String mDownloadUrl; // 下载地址
	private String mDownloadName; // 下载名称
	private String mDownloadApkPkgName; // 下载的APK的包命
	private int mIconType = 0; // 图标类型
	private String mIconInfo = " "; // 图标的信息,有可能是图标的Url, 有可能是图标的id,参见mIconType , added
	// by liuxinyang

	private long mTotalSize; // 整个下载文件的大小
	private long mAlreadyDownloadSize; // 已经下载的字节数
	private int mAlreadyDownloadPercent; // 已经下载的百分比
	private String mSaveFilePath; // 下载文件保存路径

	private Object mDownloadListenersLock = new Object();

	private int mState = STATE_NEW; // 下载状态标志
	private String mETag; // 断点续传服务器文件标识
	private boolean mIsApkFile = true; // 是否APK文件(默认APK文件)
	private int mModule = Integer.MIN_VALUE; // 标识该下载任务是属于哪个业务模块的
	private int mDownloadType; // 下载的类别，0：应用更新下载；1：应用推荐下载; add by zhoujun
	private int mDownloadNetworkType = NETWORK_TYPE_OTHER;
	private int mRestartCount = 3;
	private long mConstructTime = 0;
//	private int mTreatment = 0;
//	private Exception mException;
	// TODO:下载wangzhuobin 对该列表的操作要考虑线程同步的问题
	private ArrayList<Exception> mExceptionList = new ArrayList<Exception>();

	private HashMap<Long, IAidlDownloadListener> mHashMapListeners = new HashMap<Long, IAidlDownloadListener>();

	public DownloadTask(String downloadUrl, String downloadName, String saveFilePath) {
		this(Long.MIN_VALUE, downloadUrl, downloadName, 0, 0, saveFilePath, null);
	}

	public DownloadTask(long id, String downloadUrl, String downloadName, String saveFilePath) {
		this(id, downloadUrl, downloadName, 0, 0, saveFilePath, null);
	}

	public DownloadTask(String downloadUrl, String downloadName, String saveFilePath,
			String packageName) {
		this(Long.MIN_VALUE, downloadUrl, downloadName, 0, 0, saveFilePath, packageName);
	}

	public DownloadTask(long id, String downloadUrl, String downloadName, String saveFilePath,
			String packageName) {
		this(id, downloadUrl, downloadName, 0, 0, saveFilePath, packageName);
	}

	public DownloadTask(long id, String downloadUrl, String downloadName, String saveFilePath,
			boolean isApkFile) {
		this(id, downloadUrl, downloadName, 0, 0, saveFilePath, "");
		mIsApkFile = isApkFile;
	}

	/**
	 * 下载管理 ，最后一个参数是图标的Url
	 * 
	 * @param id
	 * @param downloadUrl
	 * @param downloadName
	 * @param saveFilePath
	 * @param packageName
	 * @param iconUrl
	 */
	public DownloadTask(long id, String downloadUrl, String downloadName, String saveFilePath,
			String packageName, int iconType, String icon) {
		this(id, downloadUrl, downloadName, 0, 0, saveFilePath, packageName, iconType, icon);
	}

	public DownloadTask(long id, String downloadUrl, String downloadName, String saveFilePath,
			String packageName, int iconType, String icon, int module) {
		this(id, downloadUrl, downloadName, 0, 0, saveFilePath, packageName, iconType, icon);
		this.mModule = module;
	}

	/**
	 * 下载管理 ，最后一个参数是图标的Url
	 * 
	 * @param downloadUrl
	 * @param downloadName
	 * @param saveFilePath
	 * @param packageName
	 * @param iconUrl
	 */
	public DownloadTask(String downloadUrl, String downloadName, String saveFilePath,
			String packageName, int iconType, String iconUrl) {
		this(Long.MIN_VALUE, downloadUrl, downloadName, 0, 0, saveFilePath, packageName, iconType,
				iconUrl);
	}

	public DownloadTask(long id, String downloadUrl, String downloadName, long alreadyDownloadSize,
			int alreadyDownloadPercent, String saveFilePath, String packageName) {
		if (id == Long.MIN_VALUE) {
			mId = System.currentTimeMillis();
		} else {
			mId = id;
		}
		mDownloadUrl = downloadUrl;
		mDownloadName = downloadName;
		mAlreadyDownloadSize = alreadyDownloadSize;
		mAlreadyDownloadPercent = alreadyDownloadPercent;
		mSaveFilePath = saveFilePath;
		mDownloadApkPkgName = packageName;
		if (mDownloadApkPkgName == null) {
			mDownloadApkPkgName = "";
		}
	}

	public DownloadTask(long id, String downloadUrl, String downloadName, long alreadyDownloadSize,
			int alreadyDownloadPercent, String saveFilePath, String packageName, int downloadType) {
		this(id, downloadUrl, downloadName, 0, 0, saveFilePath, packageName);
		mDownloadType = downloadType;
	}

	/**
	 * 下载管理的DownloadTask构造方法，带有icon的url
	 * 
	 * @param id
	 * @param downloadUrl
	 * @param downloadName
	 * @param alreadyDownloadSize
	 * @param alreadyDownloadPercent
	 * @param saveFilePath
	 * @param packageName
	 * @param iconUrl
	 */
	public DownloadTask(long id, String downloadUrl, String downloadName, long alreadyDownloadSize,
			int alreadyDownloadPercent, String saveFilePath, String packageName, int iconType,
			String iconInfo) {
		if (id == Long.MIN_VALUE) {
			mId = System.currentTimeMillis();
		} else {
			mId = id;
		}
		mDownloadUrl = downloadUrl;
		mDownloadName = downloadName;
		mAlreadyDownloadSize = alreadyDownloadSize;
		mAlreadyDownloadPercent = alreadyDownloadPercent;
		mSaveFilePath = saveFilePath;
		mDownloadApkPkgName = packageName;
		mIconType = iconType;
		if (mIconInfo != null) {
			mIconInfo = iconInfo;
		}
		if (mDownloadApkPkgName == null) {
			mDownloadApkPkgName = "";
		}
	}

	/**
	 * 重置下载任务的数据
	 */
	public void resetDownloadTask() {
		mAlreadyDownloadSize = 0;
		mAlreadyDownloadPercent = 0;
		mState = STATE_START;
		// 新增
		mThreadNum = 0;
		mThreadDataMap.clear();
	}

	/**
	 * @return the mId
	 */
	public long getId() {
		return mId;
	}

	/**
	 * @param mId
	 *            the mId to set
	 */
	public void setId(long id) {
		this.mId = id;
	}

	/**
	 * @return the mDownLoadUrl
	 */
	public String getDownloadUrl() {
		return mDownloadUrl;
	}

	/**
	 * @param mDownLoadUrl
	 *            the mDownLoadUrl to set
	 */
	public void setDownloadUrl(String downloadUrl) {
		this.mDownloadUrl = downloadUrl;
	}

	/**
	 * @return the mDisplayName
	 */
	public String getDownloadName() {
		return mDownloadName;
	}

	/**
	 * @param mDisplayName
	 *            the mDisplayName to set
	 */
	public void setDownloadName(String downloadName) {
		this.mDownloadName = downloadName;
	}

	/**
	 * @return the mAlreadyDownLoadSize
	 */
	public long getAlreadyDownloadSize() {
		return mAlreadyDownloadSize;
	}

	/**
	 * 
	 * @return
	 */
	public boolean getIsApkFile() {
		return mIsApkFile;
	}

	/**
	 * 
	 * @param mIsApkFile
	 */
	public void setIsApkFile(boolean isApkFile) {
		this.mIsApkFile = isApkFile;
	}

	/**
	 * @param mAlreadyDownLoadSize
	 *            the mAlreadyDownLoadSize to set
	 */
	public void setAlreadyDownloadSize(long alreadyDownloadSize) {
		this.mAlreadyDownloadSize = alreadyDownloadSize;
	}

	/**
	 * @return the mAlreadyDownLoadPercent
	 */
	public int getAlreadyDownloadPercent() {
		if (mAlreadyDownloadPercent > 100) {
			return 100;
		}
		return mAlreadyDownloadPercent;
	}

	/**
	 * @param mAlreadyDownLoadPercent
	 *            the mAlreadyDownLoadPercent to set
	 */
	public void setAlreadyDownloadPercent(int alreadyDownloadPercent) {
		this.mAlreadyDownloadPercent = alreadyDownloadPercent;
	}

	/**
	 * @return the mSaveFilePath
	 */
	public String getSaveFilePath() {
		return mSaveFilePath;
	}

	/**
	 * @param mSaveFilePath
	 *            the mSaveFilePath to set
	 */
	public void setSaveFilePath(String saveFilePath) {
		this.mSaveFilePath = saveFilePath;
	}

	public long addDownloadListener(IAidlDownloadListener downloadListener) {
		long id = -1;
		if (downloadListener != null) {
			synchronized (mDownloadListenersLock) {
				boolean result = false;
				Iterator<Entry<Long, IAidlDownloadListener>> iterator = mHashMapListeners
						.entrySet().iterator();
				while (iterator.hasNext()) {
					Entry<Long, IAidlDownloadListener> entry = (Entry<Long, IAidlDownloadListener>) iterator
							.next();
					if (entry.getValue().getClass().getName()
							.equals(downloadListener.getClass().getName())) {
						id = (Long) entry.getKey();
						result = true;
						break;
					}
				}
				if (!result) {
					id = System.currentTimeMillis();
					mHashMapListeners.put(id, downloadListener);
				}
			}
		}
		return id;
	}

	public void removeDownloadListener(long listenerId) {
		if (mHashMapListeners.containsKey(listenerId)) {
			synchronized (mDownloadListenersLock) {
				mHashMapListeners.remove(listenerId);
			}
		}
	}

	public void removeAllDownloadListener() {
		synchronized (mDownloadListenersLock) {
			mHashMapListeners.clear();
		}
	}

	/**
	 * @return the mState
	 */
	public int getState() {
		return mState;
	}

	/**
	 * @param mState
	 *            the mState to set
	 */
	public void setState(int state) {
		this.mState = state;
	}

	/**
	 * @return the mETag
	 */
	public String getETag() {
		return mETag;
	}

	/**
	 * @param mETag
	 *            the mETag to set
	 */
	public void setETag(String ETag) {
		this.mETag = ETag;
	}

	/**
	 * @return the mDownloadApkPkgName
	 */
	public String getDownloadApkPkgName() {
		return mDownloadApkPkgName;
	}

	/**
	 * @param mDownloadApkPkgName
	 *            the mDownloadApkPkgName to set
	 */
	public void setDownloadApkPkgName(String downloadApkPkgName) {
		this.mDownloadApkPkgName = downloadApkPkgName;
	}

	/**
	 * @return the mDownloadId
	 */
	public long getDownloadId() {
		return mDownloadId;
	}

	/**
	 * @param mDownloadId
	 *            the mDownloadId to set
	 */
	public void setDownloadId(long downloadId) {
		this.mDownloadId = downloadId;
	}

	public int getmDownloadType() {
		return mDownloadType;
	}

	/**
	 * 返回图标类型
	 * 
	 * @return
	 */
	public int getIconType() {
		return mIconType;
	}

	/**
	 * 
	 * @return the mIconUrl
	 */
	public String getIconInfo() {
		return mIconInfo;
	}

	/**
	 * 设置图标的url信息
	 * 
	 * @param iconType
	 * @param iconUrl
	 */
	public void setIconInfo(int iconType, String iconInfo) {
		mIconType = iconType;
		mIconInfo = iconInfo;
	}

	public long getTotalSize() {
		return mTotalSize;
	}

	public void setTotalSize(long totalSize) {
		this.mTotalSize = totalSize;
	}

	public int getModule() {
		return mModule;
	}

	public void setModule(int module) {
		this.mModule = module;
	}

//	public Exception getmException() {
//		return mException;
//	}
//
//	public void setmException(Exception mException) {
//		this.mException = mException;
//	}

	public ArrayList<Exception> getExceptionList() {
		ArrayList<Exception> list = (ArrayList<Exception>) mExceptionList.clone();
		mExceptionList.clear();
		return list;
	}

	public void addException(Exception e) {
		mExceptionList.add(e);
	}

	public int getDownloadNetWorkType() {
		return mDownloadNetworkType;
	}

	public void setDownloadNetWorkType(int type) {
		if (type == NETWORK_TYPE_WIFI) {
			this.mDownloadNetworkType = NETWORK_TYPE_WIFI;
		} else {
			this.mDownloadNetworkType = NETWORK_TYPE_OTHER;
		}
	}

	public int getRestartCount() {
		return mRestartCount;
	}

	public void setRestartCount(int count) {
		mRestartCount = count;
	}

	public long getConstructTime() {
		return mConstructTime;
	}

	public void setConstructTime(long time) {
		this.mConstructTime = time;
	}

//	public int getTreatment() {
//		return mTreatment;
//	}
//
//	public void setTreatment(int treatment) {
//		if (treatment == 1) {
//			this.mTreatment = 1;
//		} else {
//			this.mTreatment = 0;
//		}
//	}
	/**
	 * 通知监听器的方法
	 * 
	 * @param methodName
	 * @param downloadTask
	 */
	@SuppressWarnings("unchecked")
	public void notifyListener(int methodId) {
		HashMap<Long, IAidlDownloadListener> listeners = null;
		if (mHashMapListeners == null) {
			return;
		}
		synchronized (mDownloadListenersLock) {
			listeners = (HashMap<Long, IAidlDownloadListener>) mHashMapListeners.clone();
		}
		Iterator it = listeners.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			IAidlDownloadListener downloadListener = (IAidlDownloadListener) entry.getValue();
			try {
				if (downloadListener != null) {
					switch (methodId) {
						case IDownloadListenerConstance.METHOD_ON_START_DOWNLOAD_ID : {
							downloadListener.onStart(this);
						}
							break;
						case IDownloadListenerConstance.METHOD_ON_RESET_DOWNLOAD_TASK_ID : {
							downloadListener.onReset(this);
						}
							break;
						case IDownloadListenerConstance.METHOD_ON_PROGRESS_UPDATE_ID : {
							downloadListener.onUpdate(this);
						}
							break;
						case IDownloadListenerConstance.METHOD_ON_DOWNLOAD_STOP_ID : {
							downloadListener.onStop(this);
						}
							break;
						case IDownloadListenerConstance.METHOD_ON_DOWNLOAD_FAIL_ID : {
							downloadListener.onFail(this);
						}
							break;
						case IDownloadListenerConstance.METHOD_ON_DOWNLOAD_COMPLETE_ID : {
							downloadListener.onComplete(this);
						}
							break;
						case IDownloadListenerConstance.METHOD_ON_WAIT_DOWNLOAD_ID : {
							downloadListener.onWait(this);
						}
							break;
						case IDownloadListenerConstance.METHOD_ON_DOWNLOAD_DELETE_ID : {
							downloadListener.onCancel(this);
						}
							break;
						case IDownloadListenerConstance.METHOD_ON_CONNECTION_SUCCESS : {
							downloadListener.onConnectionSuccess(this);
						}
							break;
						case IDownloadListenerConstance.METHOD_ON_EXCEPTION : {
							downloadListener.onException(this);
						}
							break;
						default :
							break;
					}
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized void destory() {
		mDownloadUrl = null;
		mDownloadName = null;
		mSaveFilePath = null;
		synchronized (mDownloadListenersLock) {
			Iterator it = mHashMapListeners.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry) it.next();
				IAidlDownloadListener downloadListener = (IAidlDownloadListener) entry.getValue();
				if (downloadListener != null) {
					try {
						downloadListener.onDestroy(this);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
			mHashMapListeners.clear();
		}
		mETag = null;
		mDownloadApkPkgName = null;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(this.mId);
		dest.writeLong(this.mDownloadId);
		dest.writeString(this.mDownloadUrl);
		dest.writeString(this.mDownloadName);
		dest.writeString(this.mDownloadApkPkgName);
		dest.writeInt(this.mIconType);
		dest.writeString(this.mIconInfo);
		dest.writeLong(this.mTotalSize);
		dest.writeLong(this.mAlreadyDownloadSize);
		dest.writeInt(this.mAlreadyDownloadPercent);
		dest.writeString(this.mSaveFilePath);
		dest.writeInt(this.mState);
		dest.writeString(this.mETag);
		dest.writeBooleanArray(new boolean[] { this.mIsApkFile });
		dest.writeInt(this.mModule);
		dest.writeInt(this.mDownloadType);
		dest.writeInt(this.mDownloadNetworkType);
		dest.writeInt(this.mRestartCount);
		dest.writeLong(this.mConstructTime);
//		dest.writeInt(this.mTreatment);
	}

	public static final Parcelable.Creator<DownloadTask> CREATOR = new Parcelable.Creator<DownloadTask>() {
		@Override
		public DownloadTask createFromParcel(Parcel source) {
			// 从Parcel中读取数据，返回DownloadTask对象
			return new DownloadTask(source);
		}

		@Override
		public DownloadTask[] newArray(int size) {
			return new DownloadTask[size];
		}
	};

	public DownloadTask(Parcel in) {
		mId = in.readLong();
		mDownloadId = in.readLong();
		mDownloadUrl = in.readString();
		mDownloadName = in.readString();
		mDownloadApkPkgName = in.readString();
		mIconType = in.readInt();
		mIconInfo = in.readString();
		mTotalSize = in.readLong();
		mAlreadyDownloadSize = in.readLong();
		mAlreadyDownloadPercent = in.readInt();
		mSaveFilePath = in.readString();
		mState = in.readInt();
		mETag = in.readString();
		boolean[] booleanArray = new boolean[1];
		in.readBooleanArray(booleanArray);
		mIsApkFile = booleanArray[0];
		mModule = in.readInt();
		mDownloadType = in.readInt();
		mDownloadNetworkType = in.readInt();
		mRestartCount = in.readInt();
		mConstructTime = in.readLong();
//		mTreatment = in.readInt();
	}

	private int mThreadNum = 0;
	/**
	 * 
	 * <br>类描述:下载任务对应的下载线程数据BEAN
	 * <br>功能详细描述:
	 * 
	 * @author  wangzhuobin
	 * @date  [2012-12-4]
	 */
	public static class ThreadData {
		String mTag;
		long mStartPos;
		long mEndPos;
	}
	private ConcurrentHashMap<String, ThreadData> mThreadDataMap = new ConcurrentHashMap<String, ThreadData>();

	public int getThreadNum() {
		return mThreadNum;
	}

	public void setThreadNum(int num) {
		mThreadNum = num;
	}

	public ConcurrentHashMap<String, ThreadData> getThreadDataMap() {
		return mThreadDataMap;
	}

	public JSONArray getThreadDataAsJson() {
		JSONArray array = new JSONArray();
		Iterator<Entry<String, ThreadData>> iter = mThreadDataMap.entrySet().iterator();
		try {
			while (iter.hasNext()) {
				Entry<String, ThreadData> entry = iter.next();
				ThreadData data = entry.getValue();
				JSONObject obj = new JSONObject();
				obj.put("startPos", data.mStartPos);
				obj.put("endPos", data.mEndPos);
				obj.put("tag", data.mTag);
				array.put(obj);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return array;
	}

	public void setThreadData(JSONArray array) {
		setThreadNum(array.length());
		try {
			for (int i = 0; i < array.length(); i++) {
				JSONObject obj = array.getJSONObject(i);
				ThreadData data = new ThreadData();
				data.mStartPos = obj.optInt("startPos", 0);
				data.mEndPos = obj.optInt("endPos", 0);
				data.mTag = obj.optString("tag", "");
				mThreadDataMap.put(data.mTag, data);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
