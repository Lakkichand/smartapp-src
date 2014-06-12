package com.jiubang.ggheart.apps.appfunc.controler;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.go.util.ConvertUtils;
import com.jiubang.core.framework.ICleanable;
import com.jiubang.ggheart.apps.appfunc.component.RecentAppsIcon;
import com.jiubang.ggheart.apps.desks.diy.AppInvoker;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.data.AppDataEngine;
import com.jiubang.ggheart.data.Controler;
import com.jiubang.ggheart.data.info.AppItemInfo;

/**
 * 最近打开程序节点信息管理 单例 桌面和功能表都要用到
 * 
 * @author 周玉
 * @version 1.0
 */
public class RecentAppControler extends Controler
		implements
			AppInvoker.IAppInvokerListener,
			ICleanable {

	// 通知添加了一个最近打开
	public final static int APPENDEDARECENTAPP = 0;
	// 通知删除了第一个最近打开
	public final static int DELETEDARECENTAPP = 1;
	// 通知清空了最近打开
	public final static int CLEAREDRECENTAPP = 2;
	// 通知最近打开已达到最大个数
	public final static int HADMAXRCENTAPPS = 3;
	// 通知已存在，并添加到最后
	public final static int HASAPPANDUPDATEE = 4;
	// 处理完卸载
	public final static int FINISHHANDLEUNINSTALL = 5;

	// 持久化操作
	// private DataModelTmp mRecentDataModel = null;
	private RecentDataModel mRecentDataModel = null;

	private boolean mHasInit = false;
	// 列表
	private ArrayList<AppItemInfo> mRecAppItemInfos = null;
	// 当在最近打开tap 启动任何应用后才实例的info列表
	private ArrayList<AppItemInfo> mRecAppItemsCopy = null;

	// /**
	// * 想要维持的程序队列实例
	// */
	// private static RecentAppControler mRecentAppControler = null;
	/**
	 * 想要维持的程序队列最大长度
	 */
	private int mAppListMaxLenth = 24;

	private static final int REMOVEAPPLOCATION = 0;

	//
	private Handler mHandler = null;

	// 内部同步消息
	private static final int MSG_ADD = 0; // 添加
	private static final int MSG_REMOVE = 1; // 删除
	private static final int MSG_CLEAR = 2; // 清空

	/**
	 * 最近打开管理的观察者
	 * 
	 * @author guodanyang
	 * 
	 */
	public static interface IRecentAppObserver {
		/**
		 * 通知观察者最近打开数据的变化
		 * 
		 * @param msgId
		 *            消息id
		 * @param param
		 *            保留参数
		 * @param object
		 *            改变的数据
		 * @param objects
		 *            改变的数据集
		 */
		void onRecentAppChange(int msgId, int param, Object object, List objects);
	}

	/**
	 * 构造函数 TODO:单例的构造函数应为private -- dy
	 * 
	 * @param context
	 *            上下文
	 * @param dbname
	 *            数据库名
	 */
	public RecentAppControler(Context context, AppDataEngine appDataEngine) {
		super(context);

		mRecentDataModel = new RecentDataModel(context, appDataEngine);

		mRecAppItemInfos = new ArrayList<AppItemInfo>();
		//
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
					case MSG_ADD : {
						handleAddRecentApp((Intent) msg.obj);
					}
						break;
					case MSG_REMOVE : {
						handleRemoveRecentApp((Intent) msg.obj);
					}
						break;
					case MSG_CLEAR : {
						handleRemoveRecentAppItems();
					}
						break;
					default :
						break;
				}
			}
		};
	}

	/**
	 * 获取最近打开程序节点信息
	 * 
	 * @return 最近打开程序节点信息列表
	 */
	public final ArrayList<AppItemInfo> getRecentAppItems() {
		if (mHasInit) {
			return mRecAppItemInfos;
		}

		mRecentDataModel.getRecentAppItems(mRecAppItemInfos);
		mHasInit = true;
		return mRecAppItemInfos;
	}

	public void removeRecentAppItems() {
		Message message = mHandler.obtainMessage();
		message.what = MSG_CLEAR;
		mHandler.sendMessage(message);
	}

	/**
	 * 清空最近打开
	 */
	public void handleRemoveRecentAppItems() {
		// 清除内存
		mRecAppItemInfos.clear();
		if (mRecAppItemsCopy != null) {
			mRecAppItemsCopy.clear();
			mRecAppItemsCopy = null;
		}
		// 复位标记
		RecentAppsIcon.sIsStartFromRencetTab = false;

		// 更新数据库
		mRecentDataModel.removeRecentAppItems();

		// 通知清空了最近打开
		broadCast(CLEAREDRECENTAPP, 0, 0, null);
	}

	/**
	 * 根据唯一标识Intent找到元素，若没有则返回-1
	 * 
	 * @param intent
	 *            唯一标识Intent
	 * @return 下标
	 */
	private int findInList(final Intent intent) {
		if (null == intent) {
			return -1;
		}

		AppItemInfo appItemInfo = null;
		int size = 0;
		if (RecentAppsIcon.sIsStartFromRencetTab) {
			if (mRecAppItemsCopy != null) {
				size = mRecAppItemsCopy.size();
			}
		} else {
			if (mRecAppItemInfos != null) {
				size = mRecAppItemInfos.size();
			}
		}
		for (int i = 0; i < size; ++i) {
			if (RecentAppsIcon.sIsStartFromRencetTab) {
				appItemInfo = mRecAppItemsCopy.get(i);
			} else {
				appItemInfo = mRecAppItemInfos.get(i);
			}
			if (null == appItemInfo) {
				continue;
			}

			if (ConvertUtils.intentCompare(intent, appItemInfo.mIntent)) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * 添加
	 * 
	 * @param intent
	 */
	public void addRecentApp(final Intent intent) {
		Message message = mHandler.obtainMessage();
		message.what = MSG_ADD;
		message.obj = intent;
		mHandler.sendMessage(message);
	}

	/**
	 * 删除
	 * 
	 * @param intent
	 */
	public void removeRecentApp(final Intent intent) {
		Message message = mHandler.obtainMessage();
		message.what = MSG_REMOVE;
		message.obj = intent;
		mHandler.sendMessage(message);
	}

	/**
	 * 处理卸载
	 * 
	 * @param appItemInfos
	 */
	private void handleUnInstall(final ArrayList<AppItemInfo> appItemInfos) {
		if (null == appItemInfos) {
			return;
		}

		// 批量删除
		AppItemInfo appItemInfo = null;
		int size = appItemInfos.size();
		for (int i = 0; i < size; ++i) {
			appItemInfo = appItemInfos.get(i);
			if (null == appItemInfo) {
				continue;
			}
			removeRecentApp(appItemInfo.mIntent);
		}

		// 通知
		broadCast(FINISHHANDLEUNINSTALL, 0, null, null);
	}

	/**
	 * 
	 * @param intent
	 */
	public void handleRemoveRecentApp(final Intent intent) {
		// 内存中的
		int idx = findInList(intent);
		if (idx >= 0) {
			if (RecentAppsIcon.sIsStartFromRencetTab) {
				mRecAppItemsCopy.remove(idx);
			} else {
				mRecAppItemInfos.remove(idx);
			}

		}
		// 数据库中的
		mRecentDataModel.removeRecentAppItem(intent);
	}

	/**
	 * 添加最近打开 TODO:把AppItemInfo改为intent
	 * 
	 * @param itemInfo
	 *            最近打开的程序信息
	 */
	public void handleAddRecentApp(final Intent intent) {
		if (null == intent) {
			return;
		}

		AppItemInfo appItemInfo = mRecentDataModel.getAppItem(intent);
		if (null == appItemInfo) {
			// 代表该intent不是显示的APP
			// TODO 后面通过匹配找到信息
			return;
		}
		mRecentDataModel.beginDBTransaction();
		try {
			int count = 0;
			// 3.15版本，在最近打开Tab点击应用不刷新显示，退出再返回的时候再刷新
			// modify by wuziyi 2012.8.24
			// 内存中的
			if (RecentAppsIcon.sIsStartFromRencetTab) {
				if (mRecAppItemsCopy == null) {
					mRecAppItemsCopy = (ArrayList<AppItemInfo>) mRecAppItemInfos.clone();
				}
				// 先清除该id在最近打开已存在的
				handleRemoveRecentApp(intent);
				mRecAppItemsCopy.add(0, appItemInfo);
				count = mRecAppItemsCopy.size();
			} else {
				// 先清除该id在最近打开已存在的
				handleRemoveRecentApp(intent);
				mRecAppItemInfos.add(0, appItemInfo);
				count = mRecAppItemInfos.size();
			}
			// 添加到数据库(第二个参数保留，可用于管理顺序)
			mRecentDataModel.addRecentAppItem(intent, 0);

			if (count > mAppListMaxLenth) {
				// 删除最后一条记录
				// 内存中的
				AppItemInfo removeInfo = null;
				if (RecentAppsIcon.sIsStartFromRencetTab) {
					removeInfo = mRecAppItemsCopy.remove(count - 1);
				} else {
					removeInfo = mRecAppItemInfos.remove(count - 1);
				}

				if (removeInfo != null) {
					// 数据库中的
					Intent removeIntent = removeInfo.mIntent;
					mRecentDataModel.removeRecentAppItem(removeIntent);
				}
			}
			mRecentDataModel.setDBTransactionSuccessful();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			mRecentDataModel.endDBTransaction();
		}
		//		broadCast(APPENDEDARECENTAPP, 0, intent, null); 
	}

	/**
	 * 设置最近打开程序节点信息列表最大长度
	 * 
	 * @param appListMaxLenth
	 *            最近打开程序节点信息列表最大长度
	 * 
	 */
	public void setAppListMaxLenth(int appListMaxLenth) {
		mAppListMaxLenth = appListMaxLenth;
	}

	/**
	 * 程序启动回调
	 * 
	 * @param object
	 *            启动程序信息
	 */
	@Override
	public void onInvokeApp(Intent intent) {
		try {
			if (null != intent) {
				addRecentApp(intent);
			} else {
				Log.w("RecentAppManager", "The object is null, can't be added to DB");
			}
		} catch (NoSuchFieldError e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
	}

	public int getRecentAppItemsCount() {
		return mRecAppItemInfos.size();
	}

	@Override
	protected void onHandleBCChange(int msgId, int param, Object object, List objects) {

		switch (msgId) {
		// case IDiyMsgIds.EVENT_UNINSTALL_APP:
		// {
		// handleRemoveRecentApp(((AppItemInfo)object).mIntent);
		// }
		// break;
			case IDiyMsgIds.EVENT_UNINSTALL_APP : {
				handleUnInstall((ArrayList<AppItemInfo>) objects);
			}
				break;
			default :
				break;
		}
	}

	@Override
	public void cleanup() {
		clearAllObserver();
	}

	public void refreshItemsFromCopy() {
		if (mRecAppItemsCopy != null && RecentAppsIcon.sIsStartFromRencetTab) {
			mRecAppItemInfos.clear();
			mRecAppItemInfos.addAll(mRecAppItemsCopy);
			mRecAppItemsCopy.clear();
			mRecAppItemsCopy = null;
			RecentAppsIcon.sIsStartFromRencetTab = false;
		}
	}
	// private void addInList(final AppItemInfo appItemInfo) {
	// mRecAppItemInfos.add(appItemInfo);
	// }
	//
	// private AppItemInfo removeInList(final int index) {
	// return mRecAppItemInfos.remove(index);
	// }
	//
	// private void clearInList() {
	// mRecAppItemInfos.clear();
	// }
}