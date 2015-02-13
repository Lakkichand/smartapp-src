package com.zhidian.wifibox.controller;

import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Log;

import com.ta.TAApplication;
import com.ta.util.cache.TAFileCache;
import com.zhidian.wifibox.data.XDataDownload;
import com.zhidian.wifibox.util.InfoUtil;

/**
 * 记录当前WIFI盒子信息
 * 
 * @author xiedezhi
 * 
 */
public class ModeManager {
	private static final String XMODEPREFIX = "MI-BOX";

	private static final String XMODEPREFIX_ = "Mi-Box_";

	private static final String XMODEPREFIX__ = "享wifi";

	/**
	 * -1表示还没检测过，1表示超速模式，0表示普通模式
	 */
	private static int sCheckRapidly = -1;

	/**
	 * 当处于极速模式时，记录连上的WIFI名称
	 */
	private String mRapName = "";

	/**
	 * 是否能连外网
	 */
	private boolean mIsExtranet = false;

	/**
	 * 单实例
	 */
	private volatile static ModeManager sInstance = null;

	/**
	 * 获取TabDataManager实例对象
	 */
	public static ModeManager getInstance() {
		if (sInstance == null) {
			synchronized (ModeManager.class) {
				if (sInstance == null) {
					sInstance = new ModeManager();
				}
			}
		}
		return sInstance;
	}

	/**
	 * 初始化函数
	 */
	private ModeManager() {
	}

	/**
	 * 记录当前WIFI盒子名称
	 */
	public synchronized void recordRapName() {
		if (checkRapidly()) {
			mRapName = InfoUtil.getCurWifiName(TAApplication.getApplication());
		} else {
			mRapName = "";
		}
	}

	/**
	 * 获取极速模式连上的WIFI名称
	 */
	public synchronized String getRapName() {
		return mRapName;
	}

	/**
	 * 记录能否连外网
	 */
	public synchronized void setExtranet(boolean ex) {
		mIsExtranet = ex;
	}

	/**
	 * 是否能连外网
	 */
	public synchronized boolean isExtranet() {
		return mIsExtranet;
	}

	/**
	 * 检查是否在极速模式
	 */
	public synchronized static boolean checkRapidly() {
		String wifiName = InfoUtil.getCurWifiName(TAApplication
				.getApplication());
		Log.e("", "wifiName = " + wifiName);
		if (wifiName != null && wifiName.indexOf(XMODEPREFIX) != -1) {
			return true;
		}
		if (wifiName != null
				&& wifiName.toLowerCase().indexOf(XMODEPREFIX.toLowerCase()) != -1) {
			return true;
		}
		if (wifiName != null && wifiName.indexOf(XMODEPREFIX_) != -1) {
			return true;
		}
		if (wifiName != null
				&& wifiName.toLowerCase().indexOf(XMODEPREFIX_.toLowerCase()) != -1) {
			return true;
		}
		if (wifiName != null
				&& wifiName.toLowerCase().replaceAll("\"", "")
						.startsWith(XMODEPREFIX__.toLowerCase())) {
			return true;
		}
		// 这种情况可能是被安全软件拦截
		if (wifiName != null
				&& wifiName.toLowerCase().replaceAll("\"", "").equals("wifi")) {
			// 判断接口
			// 判断过就不判断了
			if (sCheckRapidly == 1) {
				return true;
			} else if (sCheckRapidly == 0) {
				return false;
			}
			CheckRapidlyThread thread = new CheckRapidlyThread();
			thread.start();
			try {
				Thread.sleep(500);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (thread.mRapidly) {
				sCheckRapidly = 1;
			} else {
				sCheckRapidly = 0;
			}
			return thread.mRapidly;
		}
		return false;
	}

	/**
	 * 重新检查是否超速模式
	 */
	public synchronized static void reCheckRapidly() {
		sCheckRapidly = -1;
		String wifiName = InfoUtil.getCurWifiName(TAApplication
				.getApplication());
		// 这种情况可能是被安全软件拦截
		if (wifiName != null
				&& wifiName.toLowerCase().replaceAll("\"", "").equals("wifi")) {
			CheckRapidlyThread thread = new CheckRapidlyThread();
			thread.start();
			try {
				Thread.sleep(500);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (thread.mRapidly) {
				sCheckRapidly = 1;
			} else {
				sCheckRapidly = 0;
			}
		}
	}

	/**
	 * 检查超速模式接口的线程
	 * 
	 * @author xiedezhi
	 * 
	 */
	private static class CheckRapidlyThread extends Thread {

		public volatile boolean mRapidly = false;

		@Override
		public void run() {
			// 连网判读
			HttpURLConnection conn = null;
			try {
				URL realUrl = new URL(XDataDownload.getXBoxIdUrl());
				conn = (HttpURLConnection) realUrl.openConnection();
				conn.setRequestProperty("accept", "*/*");
				conn.setRequestProperty("connection", "Keep-Alive");
				conn.setRequestProperty("user-agent",
						"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
				conn.setConnectTimeout(1000);
				conn.setReadTimeout(1000);
				conn.connect();
				String content = new String(TAFileCache.readStream(conn
						.getInputStream()), "UTF-8").trim();
				Log.e("", "CheckRapidlyThread  content = " + content
						+ "  length = " + content.length());
				if (content.length() == 12) {
					mRapidly = true;
				} else {
					mRapidly = false;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (conn != null) {
						conn.disconnect();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
