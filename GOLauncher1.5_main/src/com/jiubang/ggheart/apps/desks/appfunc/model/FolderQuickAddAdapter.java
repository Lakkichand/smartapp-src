package com.jiubang.ggheart.apps.desks.appfunc.model;

import java.util.ArrayList;
import java.util.Iterator;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;

import com.gau.go.launcherex.R;
import com.go.util.SortUtils;
import com.jiubang.core.mars.XComponent;
import com.jiubang.core.mars.XPanel;
import com.jiubang.ggheart.apps.appfunc.component.AppFuncFolderQuickAddIcon;
import com.jiubang.ggheart.apps.appfunc.controler.FunControler;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncFrame;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.data.info.FunFolderItemInfo;

/**
 * 文件夹快捷添加栏数据适配器
 * @author yangguanxiang
 *
 */
public class FolderQuickAddAdapter extends AppFuncAdapter {
	private static final String KEY_GAME_FOLDER = "KEY_GAME_FOLDER";
	private static final String KEY_SOCIAL_FOLDER = "KEY_SOCIAL_FOLDER";
	private static final String KEY_SYSTEM_FOLDER = "KEY_SYSTEM_FOLDER";
	private static final String KEY_TOOL_FOLDER = "KEY_TOOL_FOLDER";
	private static final int SHOW = 1;
	private PreferencesManager mPrefManager;
	private ArrayList<FunFolderItemInfo> mFolderInfoList = new ArrayList<FunFolderItemInfo>();

	private int mShowGameFolder = -1;
	private int mShowSocialFolder = -1;
	private int mShowSystemFolder = -1;
	private int mShowToolFolder = -1;

	public FolderQuickAddAdapter(Activity activity) {
		super(activity, true);
		mPrefManager = new PreferencesManager(activity);
		mShowGameFolder = mPrefManager.getInt(KEY_GAME_FOLDER, SHOW);
		mShowSocialFolder = mPrefManager.getInt(KEY_SOCIAL_FOLDER, SHOW);
		mShowSystemFolder = mPrefManager.getInt(KEY_SYSTEM_FOLDER, SHOW);
		mShowToolFolder = mPrefManager.getInt(KEY_TOOL_FOLDER, SHOW);
	}

	@Override
	public boolean dataSourceLoaded() {
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized void loadApp() {
		FunControler controler = AppFuncFrame.getFunControler();
		if (controler != null) {
			if (controler.isHandling()) {
				return;
			}
			mFolderInfoList.clear();
			FunFolderItemInfo folderInfo = new FunFolderItemInfo(controler.getFunDataModel(),
					mActivity.getString(R.string.quick_add_folder_create_new_folder), FunFolderItemInfo.TYPE_NEW_FOLDER);
			mFolderInfoList.add(folderInfo);
			ArrayList<FunFolderItemInfo> folderInfoList = controler.getFunFolders();
			if (folderInfoList != null) {
				folderInfoList = (ArrayList<FunFolderItemInfo>) folderInfoList.clone();
				Iterator<FunFolderItemInfo> it = folderInfoList.iterator();
				while (it.hasNext()) {
					FunFolderItemInfo info = it.next();
					if (info.getFunAppItemInfosForShow().isEmpty()) {
						it.remove();
					}
				}
				SortUtils.sort(folderInfoList, "getTitle", null, null, "ASC");
				mFolderInfoList.addAll(folderInfoList);
				if (isShowGameFolder()) {
					folderInfo = new FunFolderItemInfo(controler.getFunDataModel(),
							mActivity.getString(R.string.quick_add_folder_game_folder),	FunFolderItemInfo.TYPE_GAME);
					mFolderInfoList.add(folderInfo);
				}
				if (isShowSocialFolder()) {
					folderInfo = new FunFolderItemInfo(controler.getFunDataModel(),
							mActivity.getString(R.string.quick_add_folder_social_folder), FunFolderItemInfo.TYPE_SOCIAL);
					mFolderInfoList.add(folderInfo);
				}
				if (isShowSystemFolder()) {
					folderInfo = new FunFolderItemInfo(controler.getFunDataModel(),
							mActivity.getString(R.string.quick_add_folder_system_folder), FunFolderItemInfo.TYPE_SYSTEM);
					mFolderInfoList.add(folderInfo);
				}
				if (isShowToolFolder()) {
					folderInfo = new FunFolderItemInfo(controler.getFunDataModel(),
							mActivity.getString(R.string.quick_add_folder_tool_folder), FunFolderItemInfo.TYPE_TOOL);
					mFolderInfoList.add(folderInfo);
				}
			}
		}

	}

	public void setShowGameFolder(boolean show) {
		mShowGameFolder = show ? 1 : 0;
		mPrefManager.putInt(KEY_GAME_FOLDER, mShowGameFolder);
		mPrefManager.commit();
	}

	public void setShowSocialFolder(boolean show) {
		mShowSocialFolder = show ? 1 : 0;
		mPrefManager.putInt(KEY_SOCIAL_FOLDER, mShowSocialFolder);
		mPrefManager.commit();
	}

	public void setShowSystemFolder(boolean show) {
		mShowSystemFolder = show ? 1 : 0;
		mPrefManager.putInt(KEY_SYSTEM_FOLDER, mShowSystemFolder);
		mPrefManager.commit();
	}

	public void setShowToolFolder(boolean show) {
		mShowToolFolder = show ? 1 : 0;
		mPrefManager.putInt(KEY_TOOL_FOLDER, mShowToolFolder);
		mPrefManager.commit();
	}

	public boolean isShowGameFolder() {
		return mShowGameFolder == SHOW;
	}

	public boolean isShowSocialFolder() {
		return mShowSocialFolder == SHOW;
	}

	public boolean isShowSystemFolder() {
		return mShowSystemFolder == SHOW;
	}

	public boolean isShowToolFolder() {
		return mShowToolFolder == SHOW;
	}

	@Override
	public void reloadApps() {
		loadApp();
	}

	@Override
	public synchronized int getCount() {
		if (mFolderInfoList == null) {
			return 0;
		} else {
			return mFolderInfoList.size();
		}
	}

	@Override
	public synchronized Object getItem(int position) {
		if (mFolderInfoList != null && position < mFolderInfoList.size()) {
			return mFolderInfoList.get(position);
		} else {
			return null;
		}
	}

	@Override
	public XComponent getComponent(int position, int x, int y, int width, int height,
			XComponent convertView, XPanel parent) {
		AppFuncFolderQuickAddIcon icon = null;
		FunFolderItemInfo info = (FunFolderItemInfo) getItem(position);
		if (info != null) {
			if (convertView != null) {
				icon = (AppFuncFolderQuickAddIcon) convertView;
				icon.setAppInfo(info);
			} else {
				BitmapDrawable folderBg = null;
				if (info.getFolderType() == FunFolderItemInfo.TYPE_NEW_FOLDER) {
					folderBg = (BitmapDrawable) mActivity.getResources().getDrawable(
							R.drawable.appfunc_quick_add_new_folder_bg);
				} else {
					folderBg = (BitmapDrawable) mActivity.getResources().getDrawable(
							R.drawable.appfunc_quick_add_folder_bg);
				}

				icon = new AppFuncFolderQuickAddIcon(mActivity, AppFuncConstants.TICK_COUNT, x, y,
						width, height, info, folderBg, null, null, info.getTitle(), true);
			}
		}
		return icon;
	}
}
