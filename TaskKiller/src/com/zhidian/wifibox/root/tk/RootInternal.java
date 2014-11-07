package com.zhidian.wifibox.root.tk;

import java.lang.reflect.Method;

import android.os.IBinder;
import android.util.Log;

public class RootInternal {

	public static void main(String[] args) {
		if (args == null || args.length != 2) {
			return;
		}
		String pkg = args[0];
		int userid = 0;
		try {
			userid = Integer.valueOf(args[1]);
		} catch (Exception e) {
		}
		Log.e("", "pkg = " + pkg + "  userid = " + userid);
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
					try {
						m.invoke(obj, pkg);
						Log.e("", "forceStopPackage 1");
					} catch (Exception e) {
						e.printStackTrace();
					}
					try {
						m.invoke(obj, pkg, userid);
						Log.e("", "forceStopPackage 2");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
