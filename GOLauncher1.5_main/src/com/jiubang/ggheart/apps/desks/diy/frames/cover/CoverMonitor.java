package com.jiubang.ggheart.apps.desks.diy.frames.cover;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;

/**
 * 罩子层的监控器
 * @author jiangxuwen
 *
 */
public class CoverMonitor {
	private static final String BROADCAST_REMOVE_MASKVIEW = "com.jiubang.gocover.remove";
	private static final String BROADCAST_HIDE_MASKVIEW = "com.jiubang.gocover.hide";
	private static final String BROADCAST_SHOW_MASKVIEW = "com.jiubang.gocover.show";
	public static final String BROADCAST_REMOVE_HOLIDAYVIEW = "com.jiubang.gocover.remove.holidayview";
	public final static int MSG_REMOVE_COVER = 101;
	public final static int MSG_HIDE_COVER = 102;
	public final static int MSG_SHOW_COVER = 103;
	public final static int MSG_HIDE_HOLIDAY = 104;
	private Context mContext;
	private Handler mHandler;
	private BroadcastReceiver mReceiver;
	private ICoverCallback mCallback;

	public CoverMonitor(Context context) {
		mContext = context;
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case MSG_REMOVE_COVER :
						handleRemoveCover();
						break;

					case MSG_HIDE_COVER :
						handleHideMaskView();
						break;

					case MSG_SHOW_COVER :
						handleShowMaskView();
						break;
                    
					case MSG_HIDE_HOLIDAY :
						handleRemoveHolidayView();
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

				if (BROADCAST_HIDE_MASKVIEW.equals(action)) {
					Message msg = Message.obtain();
					msg.what = MSG_HIDE_COVER;
					msg.obj = intent;
					mHandler.sendMessage(msg);
				} else if (BROADCAST_REMOVE_MASKVIEW.equals(action)) {
					Message msg = Message.obtain();
					msg.what = MSG_REMOVE_COVER;
					msg.obj = intent;
					mHandler.sendMessage(msg);
				} else if (BROADCAST_SHOW_MASKVIEW.equals(action)) {
					Message msg = Message.obtain();
					msg.what = MSG_SHOW_COVER;
					msg.obj = intent;
					mHandler.sendMessage(msg);
				} else if (BROADCAST_REMOVE_HOLIDAYVIEW.equals(action)) {
					Message msg = Message.obtain();
					msg.what = MSG_HIDE_HOLIDAY;
					msg.obj = intent;
					mHandler.sendMessage(msg);
				}
			}
		};

		IntentFilter filter = new IntentFilter();
		filter.addAction(BROADCAST_HIDE_MASKVIEW);
		filter.addAction(BROADCAST_SHOW_MASKVIEW);
		filter.addAction(BROADCAST_REMOVE_MASKVIEW);
		filter.addAction(BROADCAST_REMOVE_HOLIDAYVIEW);
		// 注册广播接收器
		mContext.registerReceiver(mReceiver, filter);
	}

	protected void handleRemoveCover() {
		if (mCallback != null) {
			mCallback.handleRemoveCoverView();
		}
	}

	protected void handleHideMaskView() {
		if (mCallback != null) {
			mCallback.handleHideMaskView();
		}
	}

	protected void handleShowMaskView() {
		if (mCallback != null) {
			mCallback.handleShowMaskView();
		}
	}

	protected void handleRemoveHolidayView() {
		if (mCallback != null) {
			mCallback.handleRemoveHolidayView();
		}
	}
	
	public void registerCoverCallback(ICoverCallback callback) {
		mCallback = callback;
	}

	public void cleanup() {
		mContext.unregisterReceiver(mReceiver);
		mHandler = null;
		mReceiver = null;
	}

	/**
	 * 罩子层的回调接口
	 * @author jiangxuwen
	 *
	 */
	public interface ICoverCallback {
		/**
		 * 移除罩子层
		 */
		void handleRemoveCoverView();

		/**
		 * 隐藏主题遮罩层
		 */
		void handleHideMaskView();

		/**
		 * 展现主题遮罩层
		 */
		void handleShowMaskView();
		
		/**
		 * 移除节日版的罩子
		 */
		void handleRemoveHolidayView();
	}
}
