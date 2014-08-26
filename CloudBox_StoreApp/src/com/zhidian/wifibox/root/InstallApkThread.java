package com.zhidian.wifibox.root;

import android.content.Context;
import android.os.Handler;
import cn.trinea.android.common.util.PackageUtils;

public class InstallApkThread extends Thread {

	private String fileName;
	private Context mContext;
	private Handler handler;

	public InstallApkThread(Context context, String apkfileName, Handler handler) {
		fileName = apkfileName;
		mContext = context;
		this.handler = handler;
	}

	@Override
	public void run() {
		try {
			int status = PackageUtils.installSilent(mContext, fileName);
			if (handler != null) {
				handler.sendEmptyMessage(status);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
