package com.jiubang.ggheart.appgame.widget;

import java.util.ArrayList;
import java.util.Observable;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.jiubang.ggheart.appgame.base.bean.ClassificationDataBean;
import com.jiubang.ggheart.launcher.ICustomAction;

/**
 * 数据层，负责管理数据
 * 
 * @author zhoujun
 * 
 */
public class AppGameWidgetDataManager extends Observable {

//	private static final String ACTION_LOAD_WIDGET_DATA = "com.jiubang.intent.action.ACTION_LOAD_WIDGET_DATA";
	private static final long LOAD_WIDGET_DATA_TIME = 8 * 60 * 60 * 1000;
	//	private static final long LOAD_WIDGET_DATA_TIME = 3 * 60 * 1000;
	private static AppGameWidgetDataManager sInstance;
	private ArrayList<ClassificationDataBean> mWidgetDataList;
	private Context mContext;
	private AlarmManager mAlarmManager;
	private TaskReceiver mReceiver;
	private PendingIntent mPendingIntent;
	private boolean mStopThread = false;
	private AppGameWidgetDataProvider mDataProvider;
	private AppGameWidgetDataManager(Context context) {
		mContext = context;
		mDataProvider = new AppGameWidgetDataProvider();
		mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		mReceiver = new TaskReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(ICustomAction.ACTION_LOAD_WIDGET_DATA);
		mContext.registerReceiver(mReceiver, filter);

		// 如果这里不添加，开机时没有网络，没有加载过数据，就一直不会再自动扫描。
		autoCheckUpdate();
	}

	private void autoCheckUpdate() {
		try {
			if (mAlarmManager != null) {
				final long tiggertTime = System.currentTimeMillis() + LOAD_WIDGET_DATA_TIME;
				Intent intent = new Intent(ICustomAction.ACTION_LOAD_WIDGET_DATA);
				mPendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);
				mAlarmManager.set(AlarmManager.RTC_WAKEUP, tiggertTime, mPendingIntent);
			}
		} catch (Exception e) {
			Log.e("AppGameWidgetDataManager", "updateData fault");
		}
	}
	
	/**
	 * 
	 * <br>类描述: 
	 * <br>功能详细描述:每隔8小时，从服务器获取最新的widget数据
	 * 
	 * @author  zhoujun
	 * @date  [2012-9-4]
	 */
	private class TaskReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			//			new Thread(new Runnable() {
			//				@Override
			//				public void run() {
			loadNetworkWidgetData(mWidgetDataList);
			//				}
			//			}).start();
		}
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			mWidgetDataList = (ArrayList<ClassificationDataBean>) msg.obj;
			setChanged();
			notifyObservers(mWidgetDataList);
		}
	};

	public synchronized static AppGameWidgetDataManager getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new AppGameWidgetDataManager(context);
		}
		return sInstance;
	}

	public ArrayList<ClassificationDataBean> getWidgetData() {
		if (mWidgetDataList != null && mWidgetDataList.size() > 0) {
			return mWidgetDataList;
		} else {
			loadWidgetData();
		}
		return null;
	}

	private void loadWidgetData() {
		// 先从本地加载数据
		ArrayList<ClassificationDataBean> widgetDataList = loadLocalWidgetData();
		if (mStopThread) {
			return;
		}
		if (widgetDataList != null) {
			//如果本地没有数据，先不刷新界面
			sendLoadSuccedMessage(widgetDataList);
		}
		// 从服务器加载数据
		loadNetworkWidgetData(widgetDataList);
	}

	/**
	 * 从本地获取数据
	 */
	private ArrayList<ClassificationDataBean> loadLocalWidgetData() {
		if (mDataProvider != null) {
			return mDataProvider.getLocalWidgetData();
		}
		return null;
	}

	/**
	 * 从网络获取数据
	 */
	private void loadNetworkWidgetData(final ArrayList<ClassificationDataBean> currWidgetDataList) {
		if (mDataProvider != null) {
			// 数据处理者
			AppGameWidgetDataProvider.WidgetDataHandler handler = new AppGameWidgetDataProvider.WidgetDataHandler() {

				@Override
				public void handle(ArrayList<ClassificationDataBean> widgetDataList) {
					if (mStopThread) {
						return;
					}
					if (widgetDataList != null
							|| (widgetDataList == null && currWidgetDataList == null)) {
						// 加载到数据，或者没有数据时，都要刷新界面
						sendLoadSuccedMessage(widgetDataList);
					}
					autoCheckUpdate();
				}
			};
			// 从服务器加载数据
			mDataProvider.getNetworkWidgetData(mContext, handler);
		}
	}

	private void sendLoadSuccedMessage(ArrayList<ClassificationDataBean> widgetDataList) {
		if (mHandler != null) {
			Message message = mHandler.obtainMessage();
			message.obj = widgetDataList;
			mHandler.sendMessage(message);
		}
	}

	public synchronized void cancel() {
		mStopThread = true;
		if (mPendingIntent != null) {
			mAlarmManager.cancel(mPendingIntent);
		}
		if (mContext != null) {
			mContext.unregisterReceiver(mReceiver);
			//			mContext = null;
		}

		cleanData();
		if (sInstance != null) {
			sInstance = null;
		}
		if (mHandler != null) {
			mHandler = null;
		}
	}

	public void cleanData() {
		if (mWidgetDataList != null) {
			mWidgetDataList.clear();
			mWidgetDataList = null;
		}
	}
}
