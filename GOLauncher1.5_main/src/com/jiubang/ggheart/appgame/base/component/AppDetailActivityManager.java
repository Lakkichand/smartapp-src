/**
 * 
 */
package com.jiubang.ggheart.appgame.base.component;

import java.util.Stack;

import android.app.Activity;

/**
 * 应用游戏中心详情页Activity管理,用于处理需要退出全部详情页Activity
 * 
 * @author liguoliang
 * 
 */
public class AppDetailActivityManager {
	private static AppDetailActivityManager sInstance;
	private Stack<Activity> mActivityStack;

	private AppDetailActivityManager() {
		mActivityStack = new Stack<Activity>();
	}

	public synchronized static AppDetailActivityManager getInstance() {
		if (sInstance == null) {
			sInstance = new AppDetailActivityManager();
		}
		return sInstance;
	}

	public void pushActivity(Activity activity) {
		if (mActivityStack == null) {
			return;
		}
		mActivityStack.push(activity);
	}

	public Activity popActivity() {
		if (mActivityStack != null && !mActivityStack.isEmpty()) {
			return mActivityStack.pop();
		}
		return null;
	}

	public int getActivityCount() {
		if (mActivityStack == null) {
			return 0;
		}
		return mActivityStack.size();
	}

	public void recycle() {
		if (mActivityStack != null) {
			mActivityStack.clear();
			mActivityStack = null;
		}
	}
	
	public synchronized void destory() {
		if (sInstance != null) {
			sInstance.recycle();
			sInstance = null;
		}
	}
}
