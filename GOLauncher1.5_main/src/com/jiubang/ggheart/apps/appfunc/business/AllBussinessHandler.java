package com.jiubang.ggheart.apps.appfunc.business;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.jiubang.ggheart.apps.appfunc.controler.AppDrawerControler;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.model.DeliverMsgManager;

/**
 * 所有bussiness的返回消息控制
 * @author wuziyi
 *
 */
public class AllBussinessHandler extends Handler {
	// 内部同步消息
	public static final int MSG_STARTSAVE = 0; // 开始保存
	public static final int MSG_FINISHSAVE = 1; // 保存完成
	public static final int MSG_CACHEDAPPS = 2; // 批量安装卸载
	// public static final int MSG_UNINSTALLAPPS = 3; // 批量卸载
	public static final int MSG_SDCARDAPPS = 3; // SDcard缓存数据
	public static final int MSG_FINISHSORT = 4; // 排序完成
	public static final int MSG_SORTFAILED = 5; // 排序失败
	public static final int MSG_FINISHINIT = 6; // 初始化线程完成
	public static final int MSG_BATADD = 7; // 分批添加x个
	public static final int MSG_FINISHLOADINGSDCARD = 8; // SD卡加载完毕
	public static final int MSG_STARTLOADINGAPP = 9; // 开始加载
	public static final int MSG_ADDITEM = 10; // 开始加载
	public static final int MSG_ADDITEMS = 11; // 开始加载
	public static final int MSG_REMOVEITEM = 12; // 开始加载
	public static final int MSG_REMOVEITEMS = 13; // 开始加载
	
	private AppDrawerControler mAppDrawerControler;
	
	private boolean mIsInitedAllFunItemInfo = false;
	
	private boolean mIsStartedInitAllApp = false;
	
	private Context mContext;
	
	private Object mSaveLock = new Object();
	
	public AllBussinessHandler(Context context, AppDrawerControler appDrawerControler) {
		super();
		mContext = context;
		mAppDrawerControler = appDrawerControler;
	}

	@Override
	public void handleMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleMessage(msg);
		switch (msg.what) {
			case MSG_STARTSAVE: {
				// 通知前台Grid不能进入编辑模式
				mAppDrawerControler.broadCast(AppDrawerControler.STARTSAVE, 0, null, null);
			}
				break;
			case MSG_FINISHSAVE: {
				// 通知前台Grid能进入编辑模式
				mAppDrawerControler.broadCast(AppDrawerControler.FINISHSAVE, 0, null, null);
			}
				break;
			case MSG_CACHEDAPPS: {
//				if (!mIsCacheHandled) {
//					mIsCacheHandled = true;
					mAppDrawerControler.handleCachedAppsList();
//				}
			}
				break;
			// case MSG_UNINSTALLAPPS: {
			// if (!mIsUninstallHandled) {
			// mIsUninstallHandled = true;
			// handleCacheUnInstallList();
			// }
			// }
			// break;
			case MSG_SDCARDAPPS: {
//				if (!mIsSDHandled) {
//					mIsSDHandled = true;
//					mAppDrawerControler.handleCacheSDcardList();
//				}
			}
				break;
			case MSG_FINISHSORT: {
				mAppDrawerControler.broadCast(AppDrawerControler.SORTFINISH, 0, null, null);
			}
				break;
			case MSG_SORTFAILED: {
//				DeskToast.makeText(mContext, R.string.sort_fail, Toast.LENGTH_SHORT).show();
			}
				break;
			case MSG_FINISHINIT : {
				mIsInitedAllFunItemInfo = true;
				boolean isFirstCreate = (Boolean) msg.obj;
				if (isFirstCreate) {
					mAppDrawerControler.startSaveThread();
				}
			}
			break;
			case MSG_BATADD: {
//				synchronized (mLock) {
					mAppDrawerControler.broadCast(AppDrawerControler.BATADD, 0, null, null);
//					mLock.notify();
//				}
			}
				break;
			case MSG_FINISHLOADINGSDCARD: {
				mAppDrawerControler.broadCast(AppDrawerControler.FINISHLOADINGSDCARD, 0, null, null);
			}
			case MSG_STARTLOADINGAPP: {
				mIsStartedInitAllApp = true;
				DeliverMsgManager.getInstance().onChange(
						AppFuncConstants.APPFUNCFRAME,
						AppFuncConstants.PROGRESSBAR_HIDE, null);
				DeliverMsgManager.getInstance().onChange(
						AppFuncConstants.APPFUNCFRAME,
						AppFuncConstants.SHOW_SWITCH_MENU, null);
			}
				break;
			case MSG_ADDITEM : {
				mAppDrawerControler.broadCast(AppDrawerControler.ADDITEM, msg.arg1, msg.obj, null);
			}
				break;
			case MSG_ADDITEMS : {
				mAppDrawerControler.broadCast(AppDrawerControler.ADDITEMS, msg.arg1, msg.obj, null);
			}
				break;
			case MSG_REMOVEITEM : {
				mAppDrawerControler.broadCast(AppDrawerControler.REMOVEITEM, msg.arg1, msg.obj, null);
			}
				break;
			case MSG_REMOVEITEMS : {
				mAppDrawerControler.broadCast(AppDrawerControler.REMOVEITEMS, 0, msg.obj, null);
			}
				break;
			default:
				break;
		}
	}

	public boolean isInitedAllFunItemInfo() {
		return mIsInitedAllFunItemInfo;
	}

	public boolean isStartedInitAllApp() {
		return mIsStartedInitAllApp;
	}

}
