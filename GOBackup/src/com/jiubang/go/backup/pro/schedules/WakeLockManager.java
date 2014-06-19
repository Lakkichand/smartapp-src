package com.jiubang.go.backup.pro.schedules;

import android.content.Context;
import android.os.PowerManager;

/**
 * @author maiyongshen
 */
public class WakeLockManager {
	private static PowerManager.WakeLock sCpuWakeLock;
	private static final String TAG = "GOBackup";

	public static PowerManager.WakeLock createPartialWakeLock(Context context) {
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		return pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
	}

	public static void acquireCpuWakeLock(Context context) {
		// Log.d("GoBackup", "acquireCpuWakeLock");
		if (sCpuWakeLock != null) {
			return;
		}
		sCpuWakeLock = createPartialWakeLock(context);
		sCpuWakeLock.acquire();
	}

	public static void releaseCpuLock() {
		// Log.d("GoBackup", "releaseCpuLock");
		if (sCpuWakeLock != null) {
			sCpuWakeLock.release();
			sCpuWakeLock = null;
		}
	}
}
