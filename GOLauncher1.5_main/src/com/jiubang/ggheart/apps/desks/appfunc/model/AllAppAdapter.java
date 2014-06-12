package com.jiubang.ggheart.apps.desks.appfunc.model;

import java.util.ArrayList;
import java.util.Iterator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.gau.go.launcherex.R;
import com.jiubang.core.mars.XComponent;
import com.jiubang.core.mars.XPanel;
import com.jiubang.ggheart.apps.appfunc.component.ApplicationIcon;
import com.jiubang.ggheart.apps.appfunc.component.BaseAppIcon;
import com.jiubang.ggheart.apps.appfunc.controler.FunControler;
import com.jiubang.ggheart.apps.appfunc.setting.FunAppSetting;
import com.jiubang.ggheart.apps.appfunc.theme.AppFuncThemeController;
import com.jiubang.ggheart.apps.config.ChannelConfig;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncFrame;
import com.jiubang.ggheart.apps.desks.appfunc.handler.AppFuncHandler;
import com.jiubang.ggheart.apps.desks.appfunc.handler.FuncAppDataHandler;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants.MessageID;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUtils;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.data.info.FunAppItemInfo;
import com.jiubang.ggheart.data.info.FunFolderItemInfo;
import com.jiubang.ggheart.data.info.FunItemInfo;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.plugin.notification.NotificationType;

/**
 * 所有程序Tab所对应的数据源适配器，功能表Xbasegrid通过在layout的时候调用该类的loadapp方法加载台数据
 * 
 * @author tanshu
 * 
 */
public class AllAppAdapter extends AppFuncAdapter {
	// 后台数据列表的引用，不可更改
	protected volatile ArrayList<? extends FunItemInfo> mApps;
	private ArrayList<DataSetObserver> mDataObserverlist;
	private AppFuncThemeController mThemeController;

	protected boolean mShowUpdate = false;
	// app是否显示更新图标
	protected int mAppIconControl;
	// goStore是否显示更新提示
	protected int mGostoreControl;

//	private BitmapDrawable mEditDrawable;
//	
//	private BitmapDrawable mEditLightDrawable;
	
	public AllAppAdapter(Activity activity, boolean drawText) {
		super(activity, drawText);
		mDataObserverlist = new ArrayList<DataSetObserver>();
		mApps = new ArrayList<FunItemInfo>();
		mThemeController = AppFuncFrame.getThemeController();
		
//		mEditDrawable = (BitmapDrawable) getDrawableById(mThemeController.getThemeBean().mAppIconBean.mDeletApp);
//		
//		mEditLightDrawable =  (BitmapDrawable) getDrawableById(mThemeController.getThemeBean().mAppIconBean.mDeletHighlightApp);
	}

	@Override
	public int getCount() {
		if (mApps == null) {
			return 0;
		} else {
			return mApps.size();
		}
	}

	@Override
	public Object getItem(int position) {
		return mApps.get(position);
	}

	@Override
	public long getItemId(int position) {
		return mApps.get(position).getIndex();
	}

	/**
	 * <br>功能简述:获取所有程序界面中某个位置图标程序的
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param position
	 * @return
	 */
	public Intent getItemIntent(int position) {
		FunItemInfo itemInfo = mApps.get(position);
		if (FunItemInfo.TYPE_FOLDER == itemInfo.getType()) {
			return mApps.get(position).getIntent();
		} else {
			return ((FunAppItemInfo) mApps.get(position)).getAppItemInfo().mIntent;
		}
	}

	@Override
	public XComponent getComponent(int position, int x, int y, int width, int height,
			XComponent convertView, XPanel parent) {

		ApplicationIcon appIcon = null;

		// “全部程序”和“文件夹”
		FunItemInfo info = mApps.get(position);

		if (info != null) {
			if (convertView == null || !(convertView instanceof ApplicationIcon)) {

				if (FunItemInfo.TYPE_FOLDER == info.getType()) {
					appIcon = createFolderIcon(info, x, y, width, height);
				} else {
					appIcon = createAppIcon(info, x, y, width, height);
				}
				appIcon.setEventListener(appIcon);

			} else {
				appIcon = (ApplicationIcon) convertView;
				if (FunItemInfo.TYPE_FOLDER == info.getType()) {
					setUpFolderIcon(appIcon, info);
				} else {
					setUpAppIcon(appIcon, info);
				}
				appIcon.setAppInfo(info);
				appIcon.setNameVisible(mDrawText);

			}
		}

		return appIcon;
	}

	private void setUpAppIcon(ApplicationIcon appIcon, FunItemInfo info) {
		FunAppItemInfo appInfo = null;
		if (info instanceof FunAppItemInfo) {
			appInfo = (FunAppItemInfo) info;
		}
		// Log.i("wuziyi", "Need to Setup Icon Title: " + appIcon.getTitle());

//		Drawable editDrawable = getDrawableById(mThemeController.getThemeBean().mAppIconBean.mDeletApp);
//		Drawable editLightDrawable = getDrawableById(mThemeController.getThemeBean().mAppIconBean.mDeletHighlightApp);

//		appIcon.setEditPic(mEditDrawable);
//		appIcon.setEditLightPic(mEditLightDrawable);

		if (appInfo != null && appInfo.isNew()) {
			Drawable drawable = getDrawableById(mThemeController.getThemeBean().mAppIconBean.mNewApp, false);
			if (drawable == null) {
				drawable = AppFuncUtils.getInstance(mActivity).getDrawable(
						R.drawable.new_install_app);
			}
			appIcon.setNewAppPic((BitmapDrawable) drawable);
			appIcon.setShowStyle(BaseAppIcon.APP_EDIT_BOTH_TOP);
		} else {
			int type = NotificationType.IS_NOT_NOTIFICSTION;
			if (appInfo != null) {
				type = appInfo.getNotificationType();
			}
			if (appInfo != null && appInfo.isUpdate() && mShowUpdate && mAppIconControl == 1) {
				appIcon.setShowUpadte(true);
				// appIcon.setUpdatePic((BitmapDrawable) AppFuncUtils
				// .getInstance(mActivity).getDrawable(
				// R.drawable.appfunc_app_update));
				Drawable drawable = getDrawableById(mThemeController.getThemeBean().mAppIconBean.mUpdateIcon);
				if (drawable == null) {
					drawable = AppFuncUtils.getInstance(mActivity).getDrawable(
							R.drawable.appfunc_app_update);
				}
				appIcon.setUpdatePic((BitmapDrawable) drawable);
				appIcon.setShowStyle(BaseAppIcon.APP_ICON_SHOWUPDATE);

			} else if (appInfo != null && mShowUpdate && null != appInfo.getIntent()
					&& null != appInfo.getIntent().getComponent() && showUpdateCount(appInfo)
					&& mGostoreControl == 1) {
				appIcon.setShowUpadte(true);
				Drawable drawable = mActivity.getResources().getDrawable(R.drawable.stat_notify);
				appIcon.setGOStoreNumPic(drawable);
				appIcon.setShowStyle(BaseAppIcon.APP_ICON_STORENUM);
			} else if (appInfo != null && type != NotificationType.IS_NOT_NOTIFICSTION) {
				int unreadCount = appInfo.getUnreadCount();
				Drawable drawable = mActivity.getResources().getDrawable(R.drawable.stat_notify);
				appIcon.setNotificationNumPic(drawable, unreadCount);
				appIcon.setShowStyle(BaseAppIcon.APP_ICON_NOTIFICATIONNUM);
			} else {
				appIcon.setShowUpadte(false);
				appIcon.setUpdatePic(null);
				appIcon.setNotificationNumPic(null, 0);
				appIcon.setGOStoreNumPic(null);
				appIcon.setShowStyle(BaseAppIcon.APP_ICON_ONLY);
			}
		}
	}
	private void setUpFolderIcon(ApplicationIcon appIcon, FunItemInfo itemInfo) {
		// Log.i("wuziyi", "Setup Folder Icon: " + appIcon.getTitle());
		// Log.i("wuziyi", "Setup Folder Icon Type: " + appIcon.getItemType());
		if (itemInfo instanceof FunFolderItemInfo) {
			int total = itemInfo.getUnreadCount();
			if (total > 0) {
				Drawable drawable = mActivity.getResources().getDrawable(R.drawable.stat_notify);
				appIcon.setNotificationNumPic(drawable, total);
				appIcon.setShowStyle(BaseAppIcon.APP_ICON_NOTIFICATIONNUM);
			} else {
				appIcon.setNotificationNumPic(null, 0);
				appIcon.setShowUpadte(false);
				appIcon.setShowStyle(BaseAppIcon.APP_ICON_ONLY);
			}
		}
		// Drawable editDrawable = getDrawableById(mThemeController
		// .getThemeBean().mAppIconBean.mEditFolder);
		// Drawable editLightDrawable = getDrawableById(mThemeController
		// .getThemeBean().mAppIconBean.mEditHighlightFolder);
		// String themePackage =
		// GOLauncherApp.getSettingControler().getScreenStyleSettingInfo().getFolderStyle();
		// Drawable appDrawable = getDrawableById(mThemeController
		// .getThemeBean().mFoldericonBean.mFolderIconBottomPath,themePackage);

//		Drawable editDrawable = getDrawableById(mThemeController.getThemeBean().mAppIconBean.mDeletApp);
//		Drawable editLightDrawable = getDrawableById(mThemeController.getThemeBean().mAppIconBean.mDeletHighlightApp);

		// appIcon.reSetFolderIcon();
//		appIcon.setEditPic((BitmapDrawable) mEditDrawable);
//		appIcon.setEditLightPic((BitmapDrawable) mEditLightDrawable);
		// appIcon.setShowUpadte(false);
		// appIcon.setShowStyle(BaseAppIcon.APP_ICON_ONLY);

	}

	private ApplicationIcon createFolderIcon(FunItemInfo info, int x, int y, int width, int height) {
		// Log.i("wuziyi", "Create Folder Icon: " + info.getTitle());
		ApplicationIcon appIcon = null;
		// 程序和文件夹图标与编辑按钮只支持位图
		String themePackage = GOLauncherApp.getSettingControler().getScreenStyleSettingInfo()
				.getFolderStyle();

		Drawable appDrawable = getDrawableById(
				mThemeController.getThemeBean().mFoldericonBean.mFolderIconBottomPath, themePackage);

		// Drawable editDrawable = getDrawableById(mThemeController
		// .getThemeBean().mAppIconBean.mEditFolder);
		// Drawable editLightDrawable = getDrawableById(mThemeController
		// .getThemeBean().mAppIconBean.mEditHighlightFolder);
		Drawable editDrawable = getDrawableById(mThemeController.getThemeBean().mAppIconBean.mDeletApp);
		Drawable editLightDrawable = getDrawableById(mThemeController.getThemeBean().mAppIconBean.mDeletHighlightApp);

		appIcon = new ApplicationIcon(mActivity, AppFuncConstants.TICK_COUNT, x, y, width, height,
				info, (BitmapDrawable) appDrawable, (BitmapDrawable) editDrawable,
				(BitmapDrawable) editLightDrawable, ((FunFolderItemInfo) info).getTitle(),
				mDrawText);
		return appIcon;
	}

	private ApplicationIcon createAppIcon(FunItemInfo info, int x, int y, int width, int height) {
		ApplicationIcon appIcon = null;
		FunAppItemInfo appInfo = null;
		if (info instanceof FunAppItemInfo) {
			appInfo = (FunAppItemInfo) info;
		}
		// Log.i("wuziyi", "Need to Create Icon Title: " + info.getTitle());
		Drawable appDrawable = ((FunAppItemInfo) info).getAppItemInfo().mIcon;

//		Drawable editDrawable = getDrawableById(mThemeController.getThemeBean().mAppIconBean.mDeletApp);
//		Drawable editLightDrawable = getDrawableById(mThemeController.getThemeBean().mAppIconBean.mDeletHighlightApp);

		appIcon = new ApplicationIcon(mActivity, AppFuncConstants.TICK_COUNT, x, y, width, height,
				info, (BitmapDrawable) appDrawable,  null, null,
				((FunAppItemInfo) info).getAppItemInfo().mTitle, mDrawText);

		if (appInfo != null && appInfo.isNew()) {
			Drawable drawable = getDrawableById(mThemeController.getThemeBean().mAppIconBean.mNewApp, false);
			if (drawable == null) {
				drawable = AppFuncUtils.getInstance(mActivity).getDrawable(
						R.drawable.new_install_app);
			}
			appIcon.setNewAppPic((BitmapDrawable) drawable);
			appIcon.setShowStyle(BaseAppIcon.APP_EDIT_BOTH_TOP);
		} else {
			int type = NotificationType.IS_NOT_NOTIFICSTION;
			if (appInfo != null) {
				type = appInfo.getNotificationType();
			}
			if (appInfo != null && appInfo.isUpdate() && mShowUpdate && mAppIconControl == 1) {
				appIcon.setShowUpadte(true);
				// appIcon.setUpdatePic((BitmapDrawable) AppFuncUtils
				// .getInstance(mActivity).getDrawable(
				// R.drawable.appfunc_app_update));
				Drawable drawable = getDrawableById(mThemeController.getThemeBean().mAppIconBean.mUpdateIcon);
				if (drawable == null) {
					drawable = AppFuncUtils.getInstance(mActivity).getDrawable(
							R.drawable.appfunc_app_update);
				}
				appIcon.setUpdatePic((BitmapDrawable) drawable);
				appIcon.setShowStyle(BaseAppIcon.APP_ICON_SHOWUPDATE);
			} else if (appInfo != null && mShowUpdate && null != appInfo.getIntent()
					&& null != appInfo.getIntent().getComponent() && showUpdateCount(appInfo)
					&& mGostoreControl == 1) {
				appIcon.setShowUpadte(true);
				Drawable drawable = mActivity.getResources().getDrawable(R.drawable.stat_notify);
				appIcon.setGOStoreNumPic(drawable);
				appIcon.setShowStyle(BaseAppIcon.APP_ICON_STORENUM);
			} else if (appInfo != null && type != NotificationType.IS_NOT_NOTIFICSTION) {
				int unreadCount = appInfo.getUnreadCount();
				Drawable drawable = mActivity.getResources().getDrawable(R.drawable.stat_notify);
				appIcon.setNotificationNumPic(drawable, unreadCount);
				appIcon.setShowStyle(BaseAppIcon.APP_ICON_NOTIFICATIONNUM);
			} else {
				appIcon.setShowUpadte(false);
				appIcon.setUpdatePic(null);
				appIcon.setNotificationNumPic(null, 0);
				appIcon.setGOStoreNumPic(null);
				appIcon.setShowStyle(BaseAppIcon.APP_ICON_ONLY);
			}
		}

		return appIcon;
	}

	/**
	 * <br>功能简述: 判断图标是否需要显示更新数字
	 * <br>功能详细描述:
	 * <br>注意: 只有GO精品或应用中心的图标上面需要显示
	 * @param appInfo
	 * @return
	 * add by zhoujun 2012-09-26
	 */
	private boolean showUpdateCount(FunAppItemInfo appInfo) {
		if (null != appInfo.getIntent() && null != appInfo.getIntent().getComponent()) {
			String commponent = appInfo.getIntent().getComponent().toString();
			if (AppFuncConstants.APPGAME_APP_CENTER_COMPENTANME.equals(commponent)
					|| AppFuncConstants.GOSTORECOMPONENTNAME.equals(commponent)) {
				final ChannelConfig channelConfig = GOLauncherApp.getChannelConfig();
				if (!channelConfig.isNeedAppCenter()) {
					// 国内353渠道包，只有Go精品图标上面需要显示更新数字
					if (AppFuncConstants.GOSTORECOMPONENTNAME.equals(commponent)) {
						return true;
					}
				} else {
					//其他渠道，只有应用中心图标上需要显示更新数字
					if (AppFuncConstants.APPGAME_APP_CENTER_COMPENTANME.equals(commponent)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	private Drawable getDrawableById(String id) {
		Drawable drawable = mThemeController.getDrawable(id);
		if ((drawable instanceof BitmapDrawable) == false) {
			drawable = null;
		}
		return drawable;
	}

	private Drawable getDrawableById(String id, boolean addToHash) {
		Drawable drawable = mThemeController.getDrawable(id, addToHash);
		if ((drawable instanceof BitmapDrawable) == false) {
			drawable = null;
		}
		return drawable;
	}
	
	private Drawable getDrawableById(String id, String packageName) {
		Drawable drawable = mThemeController.getDrawable(id, packageName, false);
		if ((drawable instanceof BitmapDrawable) == false) {
			drawable = null;
		}
		return drawable;
	}

	/**
	 * 由于除了XGrid，还有procssing条要监听，故重载。
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

	/**
	 * 通知observers刷新
	 */
	@Override
	public void notifyObserver() {
		if (mDataObserverlist != null) {
			for (int i = 0; i < mDataObserverlist.size(); i++) {
				DataSetObserver origObserver = mDataObserverlist.get(i);
				origObserver.onInvalidated();
			}
		}
	}

	@Override
	public boolean handleChanges(MessageID msgId, Object obj1, Object obj2) {
		switch (msgId) {
			case APP_REMOVED : {
				// 此消息还需要最近打开处理，因此返回false
				for (DataSetObserver observer : mDataObserverlist) {
					observer.onInvalidated();
				}
				return false;
			}
			case APP_ADDED : {
				if ((mApps != null) && (mApps.size() >= 1)) {
					AppFuncHandler.getInstance().refreshAllAppGrid();
					// addCellComponent(mApps.size() - 1);
				} else {
					// ((XGrid)(DeliverMsgManager.getInstance().
					// getMsgHandler(AppFuncConstants.ALLAPPS_GRID))).
					// addCellComponent(0);
					// TODO: 当XGrid修改完毕后，改为上面的方式
					for (DataSetObserver observer : mDataObserverlist) {
						observer.onInvalidated();
					}
				}
				return true;
			}
			case APPLIST_ADDED : {
				for (DataSetObserver observer : mDataObserverlist) {
					observer.onInvalidated();
				}
				return true;
			}
			case ALL_PROGRAMSORT : {
				for (DataSetObserver observer : mDataObserverlist) {
					observer.onInvalidated();
				}
				return true;
			}
			case ALL_SORTSETTING : {
				for (DataSetObserver observer : mDataObserverlist) {
					observer.onNotify(msgId, obj1, obj2);
				}
				return true;
			}
			case SHOWNAME_CHANGED : {
				mDrawText = (AppFuncFrame.getDataHandler().getShowName() < FunAppSetting.APPNAMEVISIABLEYES)
						? false
						: true;
				for (DataSetObserver observer : mDataObserverlist) {
					observer.onInvalidated();
				}
				return false;
			}
			case SDLOADINGFINISH : {
				for (DataSetObserver observer : mDataObserverlist) {
					observer.onInvalidated();
				}
				// 还需要文件夹处理，返回false
				return false;
			}
			case ADD_BATCH_APP :
			case UPDATE_BATCH_APP : {
				for (DataSetObserver observer : mDataObserverlist) {
					observer.onInvalidated();
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * 图标位置被改变，需要更新刷新内存数据和数据库数据
	 */
	@Override
	public boolean switchPosition(int origPos, int newPos) {
		// loadApp();
		if (mApps != null) {
			if ((origPos >= 0) && (origPos < mApps.size()) && (newPos >= 0)
					&& (newPos < mApps.size())) {
				FunItemInfo app = mApps.get(origPos);
				// mApps.add(newPos, app);
				// 更新数据库
				// AppCore.getInstance(mActivity).getFunControler().moveAppItem(app.mItemInAppIndex,
				// newPos);
				int destPos;
				if (newPos >= mApps.size()) {
					newPos = mApps.size() - 1;
				}
				destPos = mApps.get(newPos).getAppItemIndex();
				boolean success = AppFuncFrame.getFunControler().moveFunAppItemInfo2(
						app.getIndex(), destPos);
				if (success) {
					return true;
				}
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void loadApp() {
		if (AppFuncFrame.getDataHandler() == null) {
			return;
		}

		PreferencesManager preferences = new PreferencesManager(mActivity,
				IPreferencesIds.DESK_SHAREPREFERENCES_FILE, Context.MODE_PRIVATE);
		mAppIconControl = preferences.getInt(FunControler.APPICON_SHOW_MESSSAGE, 0);
		mGostoreControl = preferences.getInt(FunControler.GOSTORE_SHOW_MESSAGE, 0);

		mShowUpdate = AppFuncFrame.getDataHandler().isShowAppUpdate();
		ArrayList<? extends FunItemInfo> temp = (ArrayList<? extends FunItemInfo>) AppFuncFrame
				.getDataHandler().getLaunchApps().clone();
		Iterator<? extends FunItemInfo> iterator = temp.iterator();
		while (iterator.hasNext()) {
			FunItemInfo next = iterator.next();
			if (next != null) {
				if (next.getType() == FunItemInfo.TYPE_APP) {
					// 是否新安装(仅仅针对go主题特别处理)
					FunAppItemInfo faii = (FunAppItemInfo) next;
					Intent tmpIntent = next.getIntent();
					if (tmpIntent != null
							&& tmpIntent.getAction() != null
							&& ICustomAction.ACTION_FUNC_SPECIAL_APP_GOTHEME.equals(tmpIntent
									.getAction()) && FunControler.sInstalledNewTheme) {
						FunControler.sInstalledNewTheme = false;
						faii.setIsNew(true);
					}

					// 是否隐藏
					if (next.isHide()) {
						iterator.remove();
					}
				} else if (next.getType() == FunItemInfo.TYPE_FOLDER) {
					FunFolderItemInfo faii = (FunFolderItemInfo) next;
					ArrayList<FunAppItemInfo> appsInFolder = faii.getFunAppItemInfosForShow();
					// if(faii.isMfolderchange()){
					if (null == appsInFolder || appsInFolder.isEmpty()) {
						iterator.remove();
					}
					// }
				}
			} else {
				iterator.remove();
			}
		}
		mApps = temp;
	}

	@Override
	public void reloadApps() {
		FuncAppDataHandler appHandler = AppFuncFrame.getDataHandler();
		if (appHandler != null) {
			appHandler.reloadApps();
		}
	}

	@Override
	public boolean dataSourceLoaded() {
		if (mApps == null) {
			return false;
		} else {
			return true;
		}
	}
}