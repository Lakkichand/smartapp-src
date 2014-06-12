package com.jiubang.ggheart.appgame.base.data;

import java.util.HashMap;

import android.content.Context;
import android.util.Log;

import com.jiubang.ggheart.appgame.base.net.DownloadUtil;

/**
 * 记录网络访问日志的控制类
 * 
 * @author zhoujun
 * 
 */
public class AppGameNetLogControll {
	/**
	 * 默认当前线程code
	 */
	public static final int DEFAULT_CURRENT_THREAD_CODE = -1;
	private static final String TAG = "zj";
	private static final boolean DEBUG = false;

	private volatile static AppGameNetLogControll mInstance;
	/**
	 * 零长度的byte数组对象创建起来将比任何对象都经济
	 */
	private static byte[] mLock = new byte[0]; // 特别的instance变量
	/**
	 * 存放不同线程的网络访问数据
	 */
	private HashMap<Integer, AppGameNetInfoLog> mNetInfoLogMap;

	/**
	 * 网络数据处理类
	 */
	private AppGameNetLogDataManager appGameDataManager;

	/**
	 * 上传网络信息的url
	 */
	private static final String POST_NETLOG_URL = "/recommendedapp/postlog.do?funid=1&rd=";

	private AppGameNetLogControll() {
		mNetInfoLogMap = new HashMap<Integer, AppGameNetInfoLog>();
		appGameDataManager = new AppGameNetLogDataManager();
	}

	public static AppGameNetLogControll getInstance() {
		if (mInstance == null) {
			synchronized (AppGameNetLogControll.class) {
				if (mInstance == null) {
					if (DEBUG) {
						Log.d(TAG, "getInstance is runnin---");
					}
					mInstance = new AppGameNetLogControll();
				}
			}
		}
		return mInstance;
	}

	/**
	 * 开始网络信息收集
	 * 
	 * @param context
	 * @param threadCode
	 *            开启记录的线程code，会有多线程同时进行网络请求
	 * @param 网络收集的类型
	 */
	public void startRecord(Context context, int threadCode, int type) {
		if (isNetLog(context)) {
			synchronized (mLock) {
				if (mNetInfoLogMap == null) {
					Log.e(TAG, "mNetInfoLogMap is null,clear data ");
					return;
				}
				if (threadCode == DEFAULT_CURRENT_THREAD_CODE) {
					threadCode = Thread.currentThread().hashCode();
				}
				AppGameNetInfoLog appGameNetInfo = mNetInfoLogMap.get(threadCode);
				if (appGameNetInfo == null) {
					appGameNetInfo = new AppGameNetInfoLog(context, type);
					mNetInfoLogMap.put(threadCode, appGameNetInfo);
				} else {
					// 如果已经存在，如何处理？？？？？？
				}
			}
		}
	}

	/**
	 * 停止指定线程的网络请求信息收集
	 * 
	 * @param threadCode
	 * @param context
	 */
	public void stopRecord(int threadCode, Context context) {
		synchronized (mLock) {
			AppGameNetInfoLog appGameNetInfo = getAppGameNetInfoByThreadCode(threadCode);
			if (appGameNetInfo != null) {
				// 要考虑多线程同时修改shareperferce，要加多线程保护
				String data = appGameNetInfo.createNetLogData();
				appGameDataManager.saveNetLogData(context, data);
				mNetInfoLogMap.remove(threadCode);
			}
		}
	}

	/**
	 * 设置网络请求的url
	 * 
	 * @param threadCode
	 * @param url
	 */
	public void setUrl(int threadCode, String url) {
		AppGameNetInfoLog appGameNetInfo = getAppGameNetInfoByThreadCode(threadCode);
		if (appGameNetInfo != null) {
			appGameNetInfo.setUrl(url);
		}
	}

	public void setLinkType(int threadCode, int linkType) {
		AppGameNetInfoLog appGameNetInfo = getAppGameNetInfoByThreadCode(threadCode);
		if (appGameNetInfo != null) {
			appGameNetInfo.setLinkType(linkType);
		}
	}

	/**
	 * 设置网络请求的连接时间
	 * 
	 * @param threadCode
	 * @param time
	 */
	public void setConnectionTime(int threadCode, long time) {
		AppGameNetInfoLog appGameNetInfo = getAppGameNetInfoByThreadCode(threadCode);
		if (appGameNetInfo != null) {
			appGameNetInfo.setmConnectionTime(time);
		}
	}

	/**
	 * 设置指定线程,网络下载速度
	 * 
	 * @param threadCode
	 * @param downloadSpeed
	 */
	public void setDownloadSpeed(int threadCode, String downloadSpeed) {
		AppGameNetInfoLog appGameNetInfo = getAppGameNetInfoByThreadCode(threadCode);
		if (appGameNetInfo != null) {
			appGameNetInfo.setmDwnloadSpeed(downloadSpeed);
		}
	}

	/**
	 * 记录网络错误信息
	 * 
	 * @param threadCode
	 * @param e
	 */
	public void setExceptionCode(int threadCode, Throwable e) {
		synchronized (mLock) {
			AppGameNetInfoLog appGameNetInfo = getAppGameNetInfoByThreadCode(threadCode);
			if (appGameNetInfo != null) {
				appGameNetInfo.setExceptionCode(e);
				String data = appGameNetInfo.createNetLogData();
				appGameDataManager.saveNetLogData(appGameNetInfo.getContext(), data);
				mNetInfoLogMap.remove(threadCode);
			}
		}
	}

	/**
	 * 向服务器上传网络信息日志
	 * 
	 * @param context
	 */
	public void sendNetLog(Context context) {
		synchronized (mLock) {
			if (isNetLog(context)) {
				String url = DownloadUtil.getAppCenterHost(context) + POST_NETLOG_URL
						+ DownloadUtil.sRandom.nextLong();
				appGameDataManager.sendNetLog(context, url);
			}
		}
	}

	/**
	 * 是否需要进行网络信息收集和上传
	 * 
	 * @param context
	 * @return
	 */
	public boolean isNetLog(Context context) {
		String netType = DownloadUtil.getNetLog(context, AppsDetailDownload.KEY_NETLOG_MARK);
		if (netType.equals("1")) {
			return true;
		}
		return false;
	}

	private AppGameNetInfoLog getAppGameNetInfoByThreadCode(int threadCode) {
		if (threadCode == DEFAULT_CURRENT_THREAD_CODE) {
			threadCode = Thread.currentThread().hashCode();
		}
		AppGameNetInfoLog appGameNetInfo = mNetInfoLogMap.get(threadCode);
		if (appGameNetInfo == null) {
			if (DEBUG) {
				Log.d(TAG, threadCode + " appGameNetInfo is null,this is wrong !!!!!!!!");
			}
		}
		return appGameNetInfo;
	}
}
