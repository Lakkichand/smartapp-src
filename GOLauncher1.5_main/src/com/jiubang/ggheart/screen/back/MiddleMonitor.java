package com.jiubang.ggheart.screen.back;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;

/**
 * 中间层的监控器
 * @author jiangxuwen
 *
 */
public class MiddleMonitor {
	private static final String BROADCAST_REMOVE_MIDDLEVIEW = "com.jiubang.gomiddle.remove";
	private static final String BROADCAST_HIDE_MIDDLEVIEW = "com.jiubang.gomiddle.hide";
	private static final String BROADCAST_SHOW_MIDDLEVIEW = "com.jiubang.gomiddle.show";
	public final static int MSG_REMOVE_MIDDLE = 101;
	public final static int MSG_HIDE_MIDDLE = 102;
	public final static int MSG_SHOW_MIDDLE = 103;
	private Context mContext;
	private Handler mHandler;
	private BroadcastReceiver mReceiver;
	private IMiddleCallback mCallback;

	public MiddleMonitor(Context context) {
		mContext = context;
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case MSG_REMOVE_MIDDLE :
						handleRemoveCover();
						break;

					case MSG_HIDE_MIDDLE :
						handleHideMaskView();
						break;

					case MSG_SHOW_MIDDLE :
						handleShowMaskView();
						break;

					default :
						break;
				}
				super.handleMessage(msg);
			}
		};
		createReceive();
	}

	private void createReceive() {
		// 创建广播接收器
		mReceiver = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				if (mHandler == null) {
					return;
				}

				final String action = intent.getAction();

				if (BROADCAST_HIDE_MIDDLEVIEW.equals(action)) {
					Message msg = Message.obtain();
					msg.what = MSG_HIDE_MIDDLE;
					msg.obj = intent;
					mHandler.sendMessage(msg);
				} else if (BROADCAST_REMOVE_MIDDLEVIEW.equals(action)) {
					Message msg = Message.obtain();
					msg.what = MSG_REMOVE_MIDDLE;
					msg.obj = intent;
					mHandler.sendMessage(msg);
				} else if (BROADCAST_SHOW_MIDDLEVIEW.equals(action)) {
					Message msg = Message.obtain();
					msg.what = MSG_SHOW_MIDDLE;
					msg.obj = intent;
					mHandler.sendMessage(msg);
				}
			}
		};

		IntentFilter filter = new IntentFilter();
		filter.addAction(BROADCAST_HIDE_MIDDLEVIEW);
		filter.addAction(BROADCAST_SHOW_MIDDLEVIEW);
		filter.addAction(BROADCAST_REMOVE_MIDDLEVIEW);
		// 注册广播接收器
		mContext.registerReceiver(mReceiver, filter);
	}

	protected void handleRemoveCover() {
		if (mCallback != null) {
			mCallback.handleRemoveMiddleView();
		}
	}

	protected void handleHideMaskView() {
		if (mCallback != null) {
			mCallback.handleHideMiddleView();
		}
	}

	protected void handleShowMaskView() {
		if (mCallback != null) {
			mCallback.handleShowMiddleView();
		}
	}

	public void registerCoverCallback(IMiddleCallback callback) {
		mCallback = callback;
	}

	public void cleanup() {
		mContext.unregisterReceiver(mReceiver);
		mHandler = null;
		mReceiver = null;
	}

	/**
	 * 中间层的回调接口
	 * @author jiangxuwen
	 *
	 */
	public interface IMiddleCallback {
		/**
		 * 移除中间层
		 */
		void handleRemoveMiddleView();

		/**
		 * 隐藏主题中间层
		 */
		void handleHideMiddleView();

		/**
		 * 展现主题中间层
		 */
		void handleShowMiddleView();
	}
}
