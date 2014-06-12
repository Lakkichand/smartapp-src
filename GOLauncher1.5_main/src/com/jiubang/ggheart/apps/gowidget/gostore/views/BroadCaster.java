package com.jiubang.ggheart.apps.gowidget.gostore.views;

import java.util.ArrayList;

/**
 * 广播器
 * 
 * @author huyong
 * 
 */
public class BroadCaster {

	private ArrayList<BroadCasterObserver> mObservers;

	/**
	 * 广播器观察者
	 * 
	 * @author huyong
	 * 
	 */
	public static interface BroadCasterObserver {
		/**
		 * 回调
		 * 
		 * @param msgId
		 *            id
		 * @param param
		 *            辅助参数
		 * @param object
		 *            辅助参数对象
		 * @param objects
		 *            辅助参数对象数组
		 */
		public void onBCChange(int msgId, int param, Object object, Object object2);
	}

	/**
	 * 注册观察者
	 * 
	 * @param oberver
	 *            观察者
	 */
	public synchronized void registerObserver(BroadCasterObserver observer) {
		if (observer == null) {
			// TODO:打日志
			return;
		}
		if (mObservers == null) {
			mObservers = new ArrayList<BroadCasterObserver>();
		}

		// TODO:此类中的接口改为同步的
		try {
			if (mObservers.indexOf(observer) < 0) {
				mObservers.add(observer);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} catch (OutOfMemoryError error) {
			System.gc();
		}
	}

	/**
	 * 去注册观察者
	 * 
	 * @param observer
	 *            观察者
	 * @return 是否去注册成功 true:成功 false:不成功或不存在此观察者
	 */
	public synchronized boolean unRegisterObserver(BroadCasterObserver observer) {
		if (null == mObservers) {
			return false;
		}

		return mObservers.remove(observer);
	}

	/**
	 * 清理所有的观察者
	 */
	public synchronized void clearAllObserver() {
		if (mObservers != null) {
			mObservers.clear();
			mObservers = null;
		}
	}

	/**
	 * 广播
	 * 
	 * @param msgId
	 * @param param
	 * @param object
	 * @param objects
	 */
	public synchronized void broadCast(int msgId, int param, Object object, Object object2) {
		if (mObservers == null) {
			return;
		}
		BroadCasterObserver broadCasterObserver = null;
		for (int i = 0; i < mObservers.size(); ++i) {
			broadCasterObserver = mObservers.get(i);
			if (broadCasterObserver != null) {
				broadCasterObserver.onBCChange(msgId, param, object, object2);
			}
		}
	}

	public ArrayList<BroadCasterObserver> getObserver() {
		return mObservers;
	}
}
