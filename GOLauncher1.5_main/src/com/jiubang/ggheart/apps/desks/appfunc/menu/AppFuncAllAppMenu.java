package com.jiubang.ggheart.apps.desks.appfunc.menu;

import java.util.ArrayList;

import android.app.Activity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;

import com.gau.go.launcherex.R;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.apps.config.ChannelConfig;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncFrame;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncMainView;
import com.jiubang.ggheart.apps.desks.appfunc.XViewFrame;
import com.jiubang.ggheart.apps.desks.appfunc.handler.FuncAppDataHandler;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.data.statistics.AppManagementStatisticsUtil;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 所有程序菜单
 * @author yangguanxiang
 *
 */
public class AppFuncAllAppMenu extends BaseListMenu {

	// 功能表菜单
	// private int[] APPDRAWER_MENU_ALL_TEXTS = {};

	private AppFuncMainView mMainView;
	private ArrayList<BaseMenuItemInfo> mMenuItemList = null;

	public AppFuncAllAppMenu(Activity activity) {
		super(activity);
		initMenuItemResource();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		AppFuncAllAppMenuItemInfo itemInfo = (AppFuncAllAppMenuItemInfo) mAdapter.getItem(position);
		switch (itemInfo.mActionId) {
			case AppFuncAllAppMenuItemInfo.ACTION_SORT_ICON :
				mMainView.doOptionsItemSelected(AppFuncAllAppMenuItemInfo.ACTION_SORT_ICON);
				//用户行为统计。
				StatisticsData.countUserActionData(
						StatisticsData.FUNC_ACTION_ID_APPLICATION,
						StatisticsData.USER_ACTION_SEVEN,
						IPreferencesIds.APP_FUNC_ACTION_DATA);
				break;
			case AppFuncAllAppMenuItemInfo.ACTION_CREATE_NEW_FOLDER :
				mMainView.doOptionsItemSelected(AppFuncAllAppMenuItemInfo.ACTION_CREATE_NEW_FOLDER);
				//用户行为统计
				StatisticsData.countUserActionData(
						StatisticsData.FUNC_ACTION_ID_APPLICATION,
						StatisticsData.USER_ACTION_EIGHT,
						IPreferencesIds.APP_FUNC_ACTION_DATA);
				break;
			case AppFuncAllAppMenuItemInfo.ACTION_HIDE_APP :
				mMainView.doOptionsItemSelected(AppFuncAllAppMenuItemInfo.ACTION_HIDE_APP);
				//用户行为统计
				StatisticsData.countUserActionData(
						StatisticsData.FUNC_ACTION_ID_APPLICATION,
						StatisticsData.USER_ACTION_NINE,
						IPreferencesIds.APP_FUNC_ACTION_DATA);
				break;
			case AppFuncAllAppMenuItemInfo.ACTION_APP_CENTER :
				mMainView.doOptionsItemSelected(AppFuncAllAppMenuItemInfo.ACTION_APP_CENTER);
				AppManagementStatisticsUtil.getInstance().saveCurrentEnter(mActivity,
						AppManagementStatisticsUtil.ENTRY_TYPE_FUNTAB_ICON);
				break;
//			case AppFuncAllAppMenuItemInfo.ACTION_APP_SEARCH :
//				mMainView.doOptionsItemSelected(AppFuncAllAppMenuItemInfo.ACTION_APP_SEARCH);
//				//用户行为统计
//				StatisticsData.countUserActionData(
//						StatisticsData.FUNC_ACTION_ID_APPLICATION,
//						StatisticsData.USER_ACTION_ELEVEN,
//						IPreferencesIds.APP_FUNC_ACTION_DATA);
//				break;
			case AppFuncAllAppMenuItemInfo.ACTION_APPDRAWER_SETTING :
				mMainView.doOptionsItemSelected(AppFuncAllAppMenuItemInfo.ACTION_APPDRAWER_SETTING);
				//用户行为统计
				StatisticsData.countUserActionData(
						StatisticsData.FUNC_ACTION_ID_APPLICATION,
						StatisticsData.USER_ACTION_TWELVE,
						IPreferencesIds.APP_FUNC_ACTION_DATA);
				break;
			case AppFuncAllAppMenuItemInfo.ACTION_APP_MANAGEMENT :
				mMainView.doOptionsItemSelected(AppFuncAllAppMenuItemInfo.ACTION_APP_MANAGEMENT);
				//用户行为统计
				StatisticsData.countUserActionData(
						StatisticsData.FUNC_ACTION_ID_APPLICATION,
						StatisticsData.USER_ACTION_TEN,
						IPreferencesIds.APP_FUNC_ACTION_DATA);
				break;
			default :
				break;
		}
		mPopupWindow.dismiss();
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (v == mListView && keyCode == KeyEvent.KEYCODE_MENU) {
			if (isShowing()) {
				dismiss();
				return true;
			}

		}
		if (keyCode == KeyEvent.KEYCODE_SEARCH) {
			if (isShowing()) {
				dismiss();
				return true;
			}
			mMainView.onKey(event);
		}
		return false;
	}

	@Override
	public void show(View parent) {
		initMenuItemResource();
		if (mMainView == null) {
			mMainView = ((XViewFrame) parent).getAppFuncMainView();
		}
		mAdapter.setItemPadding(DrawUtils.dip2px(9), 0, 0, 0);
		FuncAppDataHandler handler = AppFuncFrame.getDataHandler();
		if (handler != null) {
			if (GoLauncher.isPortait()) {
				show(parent, 0, handler.isShowActionBar() ? mMainView.getCurrentContent()
						.getHomeComponent().getHeight() : 0,
						(int) (GoLauncher.getScreenWidth() / 2.2), LayoutParams.WRAP_CONTENT);
			} else {
				show(parent, handler.isShowActionBar() ? mMainView.getCurrentContent()
						.getHomeComponent().getWidth() : 0, 0,
						(int) (GoLauncher.getScreenWidth() / 3.5), LayoutParams.WRAP_CONTENT);
			}
		}
	}

	private void initMenuItemResource() {
		// FunAppSetting funAppSetting =
		// GoSettingControler.getInstance(mActivity).getFunAppSetting();
		// boolean show = funAppSetting.isShowMediaManagement();
		// if (show) {
		// APPDRAWER_MENU_ALL_TEXTS = new int[]{
		// R.string.menuitem_sorticon, R.string.menuitem_createfolder,
		// R.string.menuitem_hide_tilt, R.string.menuitem_appfuncSetting,
		// R.string.menuitem_apps_mananement, R.string.menuitem_search,
		// R.string.appfunc_mediamanagement_hide
		// };
		// }else {
		// APPDRAWER_MENU_ALL_TEXTS = new int[]{
		// R.string.menuitem_sorticon, R.string.menuitem_createfolder,
		// R.string.menuitem_hide_tilt, R.string.menuitem_appfuncSetting,
		// R.string.menuitem_apps_mananement, R.string.menuitem_search,
		// R.string.appfunc_mediamanagement_show
		// };
		// }

		// APPDRAWER_MENU_ALL_TEXTS = new int[]{
		// R.string.menuitem_sorticon, R.string.menuitem_createfolder,
		// R.string.menuitem_hide_tilt,
		// R.string.appmgr_menuitem_app_center,
		// R.string.appmgr_menuitem_game_center, R.string.menuitem_search,
		// R.string.menuitem_appfuncSetting
		// };
		if (mMenuItemList == null) {
			mMenuItemList = new ArrayList<BaseMenuItemInfo>();
			mMenuItemList.add(new AppFuncAllAppMenuItemInfo(
					AppFuncAllAppMenuItemInfo.ACTION_SORT_ICON, R.string.menuitem_sorticon));
			mMenuItemList.add(new AppFuncAllAppMenuItemInfo(
					AppFuncAllAppMenuItemInfo.ACTION_CREATE_NEW_FOLDER,
					R.string.menuitem_createfolder));
			mMenuItemList.add(new AppFuncAllAppMenuItemInfo(
					AppFuncAllAppMenuItemInfo.ACTION_HIDE_APP, R.string.menuitem_hide_tilt));
			// 应用游戏分支的菜单项，要根据渠道配置信息来决定是否需要添加
			ChannelConfig channelConfig = GOLauncherApp.getChannelConfig();
			if (channelConfig != null) {
				if (channelConfig.isAddAppFunMenuItem()) {
					mMenuItemList.add(new AppFuncAllAppMenuItemInfo(
							AppFuncAllAppMenuItemInfo.ACTION_APP_CENTER,
							R.string.appmgr_menuitem_app_center));
				}
				if (channelConfig.isAddGameFunMenuItem()) {
					mMenuItemList.add(new AppFuncAllAppMenuItemInfo(
							AppFuncAllAppMenuItemInfo.ACTION_GAME_CENTER,
							R.string.appmgr_menuitem_game_center));
				}
				// 如果本渠道没有应用中心，要在功能表菜单里面加入应用管理
				if (!channelConfig.isNeedAppCenter()) {
					mMenuItemList.add(new AppFuncAllAppMenuItemInfo(
							AppFuncAllAppMenuItemInfo.ACTION_APP_MANAGEMENT,
							R.string.menuitem_apps_mananement));
				}
			}
//			mMenuItemList.add(new AppFuncAllAppMenuItemInfo(
//					AppFuncAllAppMenuItemInfo.ACTION_APP_SEARCH, R.string.menuitem_search));
			mMenuItemList.add(new AppFuncAllAppMenuItemInfo(
					AppFuncAllAppMenuItemInfo.ACTION_APPDRAWER_SETTING,
					R.string.menuitem_appfuncSetting));
		}
		setItemResources(mMenuItemList);
	}

	@Override
	public void setItemResources(ArrayList<BaseMenuItemInfo> itemInfos) {
		if (mAdapter == null) {
			mAdapter = new AppFuncAllAppMenuAdapter(mActivity, itemInfos);
		} else {
			mAdapter.setItemList(itemInfos);
		}
	}
}
