/*
 * 文 件 名:  InstallManager.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-9-24
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.appgame.download;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;

import com.jiubang.ggheart.appgame.base.component.AppInstallActivity;

/**
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  liuxinyang
 * @date  [2012-9-24]
 */
public class InstallManager {

	public static final int NEED_NEW_ACTIVITY = 0;

	public static final int NO_NEED_NEW_ACTIVITY = 1;
	// 单例
	private static InstallManager sMSelf = null;

	private Context mContext = null;

	private ArrayList<String> mList = new ArrayList<String>();

	private Object mlocker = new Object();

	private int mState = 0;

	protected InstallManager(Context context) {
		mContext = context;
	}

	public synchronized static InstallManager getInstance(Context context) {
		if (sMSelf == null) {
			sMSelf = new InstallManager(context);
		}
		return sMSelf;
	}

	public void addPkgToArray(String path) {
		synchronized (mlocker) {
			mList.add(path);
		}
		if (mState == NEED_NEW_ACTIVITY && mContext != null) {
			Intent intent = new Intent();
			intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setClass(mContext, AppInstallActivity.class);
			mContext.startActivity(intent);
		}
	}

	/**
	 * <br>功能简述:从队列里取出一个安装路径并返回，同时队列会删除这个安装路径
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public String getPkgFromArray() {
		String result = null;
		synchronized (mlocker) {
			if (mList.size() > 0) {
				result = mList.remove(0);
			}
		}
		return result;
	}

	/**
	 * <br>功能简述:从队列移除一个安装路径
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param path
	 */
	public void removePkgFromArray(String path) {
		synchronized (mlocker) {
			if (mList.contains(path)) {
				mList.remove(path);
			}
		}
	}

	public void setState(int state) {
		synchronized (mlocker) {
			this.mState = state;
		}
	}

	public int getState() {
		return mState;
	}
}
