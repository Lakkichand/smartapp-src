package com.zhidian.wifibox.root.tk;

import java.lang.reflect.Method;

import android.os.IBinder;
import android.util.Log;

public class RootInternal {

	public static void main(String[] args) {
		// 一次传多个pkgName进来，第一个参数是userid，后面的都是包名
		if (args == null || args.length < 2) {
			return;
		}
		int userid = 0;
		try {
			userid = Integer.valueOf(args[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Log.e("", "userid = " + userid);
		try {
			Class serviceManager = Class.forName("android.os.ServiceManager",
					false, Thread.currentThread().getContextClassLoader());
			Method getService = serviceManager.getMethod("getService",
					new Class[] { String.class });
			IBinder iBinder = (IBinder) getService.invoke(null,
					new Object[] { "activity" });
			Class activityManagerNative = Class.forName(
					"android.app.ActivityManagerNative", false, Thread
							.currentThread().getContextClassLoader());
			Method asInterface = activityManagerNative.getMethod("asInterface",
					new Class[] { IBinder.class });
			Object obj = asInterface.invoke(null, new Object[] { iBinder });
			Method[] methods = obj.getClass().getMethods();
			for (Method m : methods) {
				if ("forceStopPackage".equals(m.getName())) {
					for (int i = 1; i < args.length; i++) {
						String pkg = args[i];
						try {
							m.invoke(obj, pkg);
							Log.e("", "forceStopPackage1 pkg = " + pkg);
						} catch (Exception e) {
							e.printStackTrace();
						}
						try {
							m.invoke(obj, pkg, userid);
							Log.e("", "forceStopPackage2 pkg = " + pkg);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
