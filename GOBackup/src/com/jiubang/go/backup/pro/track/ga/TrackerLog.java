/**
 * 
 */
package com.jiubang.go.backup.pro.track.ga;

import android.util.Log;

/**
 * @author liguoliang
 *
 */
public class TrackerLog {
	private static final String TAG = "TrackerLog";
	
	private static boolean sLogEnable = false;
	
	public static void setLogEnable(boolean enable) {
		sLogEnable = enable;
	}
	
	public static void i(String msg) {
		if (sLogEnable) {
			Log.i(TAG, msg);
		}
	}
	
	public static void e(String msg) {
		if (sLogEnable) {
			Log.e(TAG, msg);
		}
	}
	
	public static void v(String msg) {
		if (sLogEnable) {
			Log.v(TAG, msg);
		}
	}
	
	public static void d(String msg) {
		if (sLogEnable) {
			Log.d(TAG, msg);
		}
	}
	
	public static void w(String msg) {
		if (sLogEnable) {
			Log.w(TAG, msg);
		}
	}
}
