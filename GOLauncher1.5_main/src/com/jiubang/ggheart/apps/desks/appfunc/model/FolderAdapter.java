package com.jiubang.ggheart.apps.desks.appfunc.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Context;

import com.jiubang.ggheart.apps.appfunc.controler.FunControler;
import com.jiubang.ggheart.apps.appfunc.setting.FunAppSetting;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncFrame;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants.MessageID;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.data.BroadCaster.BroadCasterObserver;
import com.jiubang.ggheart.data.DatabaseException;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.FunAppItemInfo;
import com.jiubang.ggheart.data.info.FunFolderItemInfo;
import com.jiubang.ggheart.data.info.FunItemInfo;

/**
 * 文件夹数据适配器
 * @author yangguanxiang
 *
 */
public class FolderAdapter extends AllAppAdapter implements BroadCasterObserver {

	private FunFolderItemInfo mInfo;

	// /**
	// * 是否显示加号按钮
	// */
	// private boolean mShowAddButton = true;
	// private AppFuncImageButton mAddButton = null;

	public FolderAdapter(Activity activity, boolean drawText, FunFolderItemInfo folderInfo) {
		super(activity, drawText);
		mInfo = folderInfo;
		mInfo.registerObserver(this);
		// mAddButton = new AppFuncImageButton(activity,1, 0, 0, 0, 0);
		// mAddButton.setIcon(activity.getResources().getDrawable(R.drawable.appfunc_folder_add_button));
		// mAddButton.setIcon_pressed(activity.getResources().getDrawable(R.drawable.appfunc_folder_add_button_press));
		// mAddButton.setEventListener(new IComponentEventListener() {
		//
		// @Override
		// public boolean onEventFired(XComponent xcomponent, byte type, Object
		// obj,
		// int i, Object obj1) {
		// if (type==EventType.CLICKEVENT) {
		// startEditActivity();
		// } else if (obj instanceof MotionEvent){
		// MotionEvent e = (MotionEvent)obj;
		// if (e.getAction()==MotionEvent.ACTION_DOWN) {
		// mAddButton.setIsPressed(true);
		// } else if
		// (e.getAction()==MotionEvent.ACTION_UP&&EventType.CLICKEVENT==type) {
		// mAddButton.setIsPressed(false);
		// startEditActivity();
		// }else{
		// mAddButton.setIsPressed(false);
		// }
		// }
		// return true;
		// }
		// });
		loadApp();
	}

	// private void startEditActivity(){
	// AppFuncMainView.sOpenFuncSetting = true;
	// Intent intent = new Intent(mActivity,
	// AppFuncModifyFolderActivity.class);
	// intent.putExtra(AppFuncConstants.FOLDER_ID,
	// mInfo.getFolderId());
	// int requestCode = IRequestCodeIds.REQUEST_MODIFY_APPDRAWER_FOLDER;
	// mActivity.startActivityForResult(intent, requestCode);
	// }

	public long getFolderId() {
		return mInfo.getFolderId();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void loadApp() {
		ArrayList<FunAppItemInfo> clone = (ArrayList<FunAppItemInfo>) mInfo.getFunAppItemInfos()
				.clone();
		Iterator<? extends FunItemInfo> iterator = clone.iterator();
		while (iterator.hasNext()) {
			FunItemInfo next = iterator.next();
			if (next.getType() == FunItemInfo.TYPE_APP) {
				if (next.isHide()) {
					iterator.remove();
				}
			}
		}
		PreferencesManager preferences = new PreferencesManager(mActivity,
				IPreferencesIds.DESK_SHAREPREFERENCES_FILE, Context.MODE_PRIVATE);
		mAppIconControl = preferences.getInt(FunControler.APPICON_SHOW_MESSSAGE, 0);
		mGostoreControl = preferences.getInt(FunControler.GOSTORE_SHOW_MESSAGE, 0);
		mShowUpdate = AppFuncFrame.getDataHandler().isShowAppUpdate();
		AppFuncFrame.getFunControler().checkAppUpdate(clone, true);

		// if (getShowAddButton()) {
		// //显示加号按钮
		// clone.add(new FunAppItemInfo(null, null));
		// }

		mApps = clone;
	}

	// @Override
	// public XComponent getComponent(int position, int x, int y, int width,int
	// height, XComponent convertView, XPanel parent) {
	// // if (getShowAddButton()&&position==mApps.size()-1) {
	// if (position==mApps.size()-1) {
	// int addBtn_height = mAddButton.getIcon().getIntrinsicHeight();
	// int addBtn_width = mAddButton.getIcon().getIntrinsicWidth();
	// int padding_horizontal = (width - addBtn_width) / 2;
	// int padding_vertical = (height - addBtn_height) / 2;
	// mAddButton.setImageHPadding(padding_horizontal);
	// mAddButton.setImageVPadding(padding_vertical);
	//
	// mAddButton.layout(x, y, x+width, y+height);
	// return mAddButton;
	// }
	// return super.getComponent(position, x, y, width, height, convertView,
	// parent);
	// }

	public void changeFolderInfo(FunFolderItemInfo folderInfo) {
		if (mInfo != null) {
			mInfo.unRegisterObserver(this);
		}
		mInfo = folderInfo;
		mDrawText = (AppFuncFrame.getDataHandler().getShowName() < FunAppSetting.APPNAMEVISIABLEYES)
				? false
				: true;
		mInfo.registerObserver(this);
	}

	@Override
	public boolean handleChanges(MessageID msgId, Object obj1, Object obj2) {
		switch (msgId) {
		// case APP_ADDED:{
		// //操作内存
		// if(obj1 instanceof FunAppItemInfo){
		// addApp((FunAppItemInfo)obj1);
		// }
		// DeliverMsgManager.getInstance().onChange(AppFuncConstants.APPFOLDER,
		// AppFuncConstants.LAYOUTFOLDERGRID, null);
		// return true;
		// }
		// case APPLIST_ADDED:{
		// //操作内存
		// if(obj2 instanceof List<?>){
		// addAppList((List<FunAppItemInfo>)obj2);
		// }
		// DeliverMsgManager.getInstance().onChange(AppFuncConstants.APPFOLDER,
		// AppFuncConstants.LAYOUTFOLDERGRID, null);
		// return true;
		// }
			case SHOWNAME_CHANGED : {
				mDrawText = (AppFuncFrame.getDataHandler().getShowName() < FunAppSetting.APPNAMEVISIABLEYES)
						? false
						: true;
				return false;
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
				boolean success = mInfo.moveFunAppItem2(mApps.get(origPos).getAppItemIndex(), mApps
						.get(newPos).getAppItemIndex());
				if (success) {
					// 通知功能表XGrid，顶部工具条刷新文件夹缩略图
					if ((origPos < 4) || (newPos < 4)) {
						DeliverMsgManager.getInstance().onChange(AppFuncConstants.TABCOMPONENT,
								AppFuncConstants.REFRESHICON, mInfo.getFolderId());
						DeliverMsgManager.getInstance().onChange(
								AppFuncConstants.APP_FUNC_FOLDER_QUICK_ADD_BAR,
								AppFuncConstants.REFRESH_FOLDER_QUICK_ADD_BAR_NORMAL_FOLDER, mInfo);
					}
					return true;
				}
			}
		}
		return false;
	}

	public void removeApp(FunAppItemInfo removedItem) throws DatabaseException {
		mInfo.removeFunAppItemInfo(removedItem, true);
		// 通知桌面同步更新桌面文件夹
		if (removedItem != null) {
			ArrayList<AppItemInfo> removeList = new ArrayList<AppItemInfo>();
			removeList.add(removedItem.getAppItemInfo());
			
			GoLauncher.sendMessage(GoLauncher.getFrame(IDiyFrameIds.APPFUNC_FRAME),
					IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.SCREEN_FOLDER_REMOVEITEMS, 0,
					mInfo.getFolderId(), removeList);
			GoLauncher.sendHandler(GoLauncher.getFrame(IDiyFrameIds.APPFUNC_FRAME),
					IDiyFrameIds.DOCK_FRAME, IDiyMsgIds.SCREEN_FOLDER_REMOVEITEMS, 0,
					mInfo.getFolderId(), removeList);
		}
	}

	@Override
	public void onBCChange(int msgId, int param, Object object, List objects) {
		switch (msgId) {
			case FunFolderItemInfo.ADDITEM : {
				// 目前暂不支持
				break;
			}
			case FunFolderItemInfo.REMOVEITEM : {
				if ((object != null) && (object instanceof FunAppItemInfo)) {
					if ((mInfo != null) && (mInfo instanceof FunFolderItemInfo)) {
						if ((param >= 0) && (param < 4)) {
							// 通知AppFuncFolder重新设置宽高
							DeliverMsgManager.getInstance().onChange(AppFuncConstants.APPFOLDER,
									AppFuncConstants.LAYOUTFOLDERGRID, null);
						}
					}
				}
				break;
			}
		}
	}

	// public synchronized void setShowAddButton(boolean show){
	// mShowAddButton = show;
	// if(GOLauncherApp.getSettingControler().getScreenSettingInfo().mLockScreen){
	// mShowAddButton = false;
	// }
	// }
	//
	// public synchronized boolean getShowAddButton(){
	// if(GOLauncherApp.getSettingControler().getScreenSettingInfo().mLockScreen){
	// mShowAddButton = false;
	// }
	// return mShowAddButton;
	// }
}
