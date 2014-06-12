package com.jiubang.ggheart.apps.gowidget.gostore.controller;

import android.content.Context;
import android.os.AsyncTask;
/**
 * 
 * <br>类描述:基础的业务处理器
 * <br>功能详细描述:
 * 
 * @author  wangzhuobin
 * @date  [2012-9-14]
 */
public abstract class BaseController {

	public final static int STATE_RESPONSE_OK = Integer.MAX_VALUE;
	public final static int STATE_RESPONSE_ERR = Integer.MIN_VALUE;

	protected Context mContext;
	protected IModeChangeListener mChangeListener;
	protected boolean mIsRecycled = false; // 是否已经被回收

	public BaseController(Context context, IModeChangeListener listener) {
		super();
		mContext = context;
		this.mChangeListener = listener;
	}

	/**
	 * 通知来自视图组件的监听器模型组件数据已经改变
	 * 
	 * @param action
	 *            请求代码
	 * @param state
	 *            请求处理状态代码
	 * @param value
	 *            返回结果
	 */
	protected void notifyChange(int action, int state, Object value) {
		if (mChangeListener != null) {
			mChangeListener.onModleChanged(action, state, value);
		}
	}

	/**
	 * 发送同步请求
	 * 
	 * @param action
	 *            请求代码
	 * @param parames
	 *            请求参数
	 */
	public void sendRequest(int action, Object parames) {
		try {
			Object value = handleRequest(action, parames);
			notifyChange(action, STATE_RESPONSE_OK, value);
		} catch (Exception e) {
			notifyChange(action, STATE_RESPONSE_ERR, e);
		}
	}

	/**
	 * 发送异步请求
	 * 
	 * @param action
	 *            请求代码
	 * @param parames
	 *            请求参数
	 */
	public void sendAsyncRequest(final int action, final Object parames) {

		new AsyncTask<Object, Void, Object>() {
			@Override
			protected Object doInBackground(Object... params) {
				try {
					return handleRequest(action, parames);
				} catch (Exception e) {
					return e;
				}
			}

			@Override
			protected void onPostExecute(Object result) {
				if (result instanceof Exception) {
					notifyChange(action, STATE_RESPONSE_ERR, result);
					return;
				}
				notifyChange(action, STATE_RESPONSE_OK, result);
			}
		}.execute(parames);
	}

	/**
	 * 用于具体处理不同请求的方法，由子类实现
	 * 
	 * @param action
	 *            请求代码
	 * @param parames
	 *            请求参数
	 * @return 处理请求后的结果
	 */
	protected abstract Object handleRequest(int action, Object parames);

	/**
	 * 控制器自身回收的方法
	 */
	public abstract void destory();
}
