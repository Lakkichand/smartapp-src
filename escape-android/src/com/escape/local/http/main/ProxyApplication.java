package com.escape.local.http.main;

import android.app.Application;
import android.os.Environment;

public class ProxyApplication extends Application {

	public static String sCacheDir = Environment.getExternalStorageDirectory()
			.getAbsolutePath();

	@Override
	public void onCreate() {
		super.onCreate();
		sCacheDir = getCacheDir().getAbsolutePath();
	}

}
