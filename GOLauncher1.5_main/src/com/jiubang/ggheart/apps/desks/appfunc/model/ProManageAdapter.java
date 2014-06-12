package com.jiubang.ggheart.apps.desks.appfunc.model;

import java.util.ArrayList;
import java.util.Iterator;

import android.app.Activity;

import com.jiubang.core.framework.ITimerListener;
import com.jiubang.core.framework.TimerBean;
import com.jiubang.core.mars.XComponent;
import com.jiubang.core.mars.XPanel;
import com.jiubang.ggheart.apps.appfunc.component.BaseAppIcon;
import com.jiubang.ggheart.apps.appfunc.component.ProManageIcon;
import com.jiubang.ggheart.apps.appfunc.setting.FunAppSetting;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncFrame;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants.MessageID;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.data.info.FunTaskItemInfo;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 
 * 适配器
 */
public class ProManageAdapter extends AppFuncAdapter implements ITimerListener {
	private final static int TIMER_DURATION = 1000;

	private volatile ArrayList<FunTaskItemInfo> mApps;
	private ArrayList<DataSetObserver> mDataObserverlist;

	/**
	 * 计时器数据bean
	 */
	private TimerBean mNotifyTimerBean;
	/**
	 * 是否正在处理消息
	 */
	private volatile boolean mProcessInProgress = false;
	//	private BitmapDrawable mLockPic;
	//	private BitmapDrawable mEditPic;
	//	private BitmapDrawable mEditLightPic;
	//	private AppFuncThemeController mThemeController;

	public ProManageAdapter(Activity activity, boolean drawText) {
		super(activity, drawText);
		mDataObserverlist = new ArrayList<DataSetObserver>();
		//		mThemeController = AppFuncFrame.getThemeController();
	}

	//	public void setProManageEdithome(ProManageEditDock proManageEdithome) {
	//		this.proManageEdithome = proManageEdithome;
	//		mThemeController = AppFuncFrame.getThemeController();
	//	}

	@Override
	public synchronized int getCount() {
		if (mApps == null) {
			return 0;
		} else {
			return mApps.size();
		}
	}

	@Override
	public synchronized Object getItem(int position) {
		return mApps.get(position);
	}

	@Override
	public XComponent getComponent(int position, int x, int y, int width, int height,
			XComponent convertView, XPanel parent) {
		ProManageIcon appIcon = null;
		FunTaskItemInfo info = null;
		// 处理“程序管理”
		info = getItemInApps(position);

		if (info != null) {
			//			Drawable drawable = mThemeController
			//					.getDrawable(mThemeController.getThemeBean().mAppIconBean.mLockApp);
			//			if (drawable != null && drawable instanceof BitmapDrawable
			//					&& !((BitmapDrawable) drawable).getBitmap().isRecycled()) {
			//				mLockPic = (BitmapDrawable) drawable;
			//			} else {
			//				mLockPic = (BitmapDrawable) AppFuncUtils.getInstance(mActivity).getDrawable(
			//						R.drawable.promanage_lock_icon);
			//			}

			//			drawable = mThemeController
			//					.getDrawable(mThemeController.getThemeBean().mAppIconBean.mKillApp);
			//			if (drawable != null && drawable instanceof BitmapDrawable) {
			//				mEditPic = (BitmapDrawable) drawable;
			//			} else {
			//				mEditPic = (BitmapDrawable) AppFuncUtils.getInstance(mActivity).getDrawable(
			//						R.drawable.promanage_close_normal);
			//			}
			//
			//			drawable = mThemeController
			//					.getDrawable(mThemeController.getThemeBean().mAppIconBean.mKillAppLight);
			//			if (drawable != null && drawable instanceof BitmapDrawable) {
			//				mEditLightPic = (BitmapDrawable) drawable;
			//			} else {
			//				mEditLightPic = (BitmapDrawable) AppFuncUtils.getInstance(mActivity).getDrawable(
			//						R.drawable.promanage_close_light);
			//			}
			if (convertView == null || !(convertView instanceof ProManageIcon)) {
				appIcon = new ProManageIcon(mActivity, /*proManageEdithome,*/
				AppFuncConstants.TICK_COUNT, x, y, width, height, info, null, null, null, mDrawText);
				appIcon.setEventListener(appIcon);
			} else {
				appIcon = (ProManageIcon) convertView;
				//				appIcon.setEditPic(mEditPic);
				//				appIcon.setEditLightPic(mEditLightPic);
				//				appIcon.setLockPic(mLockPic);
				appIcon.setAppInfo(info);
				appIcon.setNameVisible(mDrawText);
			}

			if (info.isInWhiteList()) {
				appIcon.setShowStyle(BaseAppIcon.APP_EDIT_BOTH);
			} else {
				appIcon.setShowStyle(BaseAppIcon.APP_ICON_ONLY);
			}
		}

		return appIcon;
	}

	/**
	 * 由于除了XGrid，还有内存状态一栏要监听后台改变，故重载。
	 */
	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		for (DataSetObserver origObserver : mDataObserverlist) {
			if (origObserver == observer) {
				return;
			}
		}
		mDataObserverlist.add(observer);
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		for (DataSetObserver origObserver : mDataObserverlist) {
			if (origObserver == observer) {
				mDataObserverlist.remove(observer);
				return;
			}
		}
	}

	private ArrayList<FunTaskItemInfo> getFilteredList(ArrayList<FunTaskItemInfo> originArrayList) {
		if (null == originArrayList) {
			return null;
		}

		Iterator<FunTaskItemInfo> appIterator = originArrayList.iterator();

		// 设置不显示忽略列表内的程序
		if (FunAppSetting.NEGLECTAPPS == GOLauncherApp.getSettingControler().getFunAppSetting()
				.getShowNeglectApp()) {
			while (appIterator.hasNext()) {
				FunTaskItemInfo aInfo = appIterator.next();
				if (aInfo.isInWhiteList()) {
					appIterator.remove();
				}
			}
		}

		return originArrayList;
	}

	private void initTimerBean() {

		// 计时器启动
		mNotifyTimerBean = new TimerBean();
		mNotifyTimerBean.mTimes = 1;
		mNotifyTimerBean.mDelay = TIMER_DURATION / 2;
		mNotifyTimerBean.mPeriod = TIMER_DURATION;
		mNotifyTimerBean.mListeners.add(this);
		AppFuncFrame appFrame = (AppFuncFrame) GoLauncher.getFrame(IDiyFrameIds.APPFUNC_FRAME);
		if (appFrame != null) {
			appFrame.getFrameManager().startTimer(mNotifyTimerBean, "FunTaskManager");
		}
	}

	private synchronized FunTaskItemInfo getItemInApps(final int position) {
		if (mApps == null || mApps.size() <= position) {
			return null;
		}
		return mApps.get(position);
	}

	@SuppressWarnings("unchecked")
	private synchronized void updateAppsList() {
		mApps = null;
		if (AppFuncFrame.getDataHandler() != null) {
			mApps = getFilteredList(AppFuncFrame.getDataHandler().getProgresses());
		}
	}

	@Override
	public boolean handleChanges(MessageID msgId, Object obj1, Object obj2) {
		switch (msgId) {
			case SHOWNAME_CHANGED : {
				// 广播方式
				mDrawText = (AppFuncFrame.getDataHandler().getShowName() < FunAppSetting.APPNAMEVISIABLEYES)
						? false
						: true;

				return false;
			}

			case SINGLE_TASKMANAGE : {
				// 关闭单个进程:在功能表被拉到前台时才处理
				if (AppFuncFrame.sVisible && !mProcessInProgress) {
					mProcessInProgress = true;
					updateAppsList();

					for (DataSetObserver observer : mDataObserverlist) {
						observer.onInvalidated();
					}
					mProcessInProgress = false;
				}

				return true;
			}
			case ALL_TASKMANAGE : {
				// 关闭所有进程
				if (AppFuncFrame.sVisible && !mProcessInProgress) {
					mProcessInProgress = true;
					updateAppsList();
					for (DataSetObserver observer : mDataObserverlist) {
						observer.onInvalidated();
					}

					// 计时器启动
					initTimerBean();
				}

				return true;
			}
			case LOCK_LIST_CHANGED : {
				// 忽略列表发生变化
				notifyObserver();
			}

		}
		return false;
	}

	@Override
	public void onTimer(TimerBean bean) {
		if (bean == mNotifyTimerBean) {
			for (DataSetObserver observer : mDataObserverlist) {
				observer.onInvalidated();
			}

			// 终止前一个计时器
			AppFuncFrame appFrame = (AppFuncFrame) GoLauncher.getFrame(IDiyFrameIds.APPFUNC_FRAME);
			if (appFrame != null) {
				appFrame.getFrameManager().cancelTimer(mNotifyTimerBean);
			}
			mNotifyTimerBean = null;
			mProcessInProgress = false;
		}
	}

	@Override
	public void loadApp() {
		updateAppsList();
	}

	@Override
	public void notifyObserver() {
		for (DataSetObserver observer : mDataObserverlist) {
			observer.onInvalidated();
		}
	}

	@Override
	public boolean dataSourceLoaded() {
		return false;
	}

	public void teminateCurApps() {
		AppFuncFrame.getDataHandler().terminateAll(mApps);
	}

	public synchronized void release() {
		if (mApps != null) {
			mApps.clear();
			mApps = null;
		}
		// FuncAppDataHandler.getInstance().releaseTaskMgrControler();
	}

	@Override
	public void reloadApps() {
		// TODO Auto-generated method stub

	}

}
