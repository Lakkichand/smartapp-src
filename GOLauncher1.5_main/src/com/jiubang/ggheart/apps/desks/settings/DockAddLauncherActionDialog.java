package com.jiubang.ggheart.apps.desks.settings;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LauncherActivity.ListItem;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.device.Machine;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.AddGoShortCutTab;
import com.jiubang.ggheart.data.info.IItemType;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.launcher.ICustomAction;

/**
 * Dock添加GO桌面快捷方式对话框
 * 
 * @author jiangxuwen
 * 
 */
public class DockAddLauncherActionDialog
		implements
			DialogInterface.OnClickListener,
			DialogInterface.OnCancelListener,
			DialogInterface.OnDismissListener {

	// 是否显示“屏幕翻转“选项
	private boolean mIsShowTurnScreen;

	AlertDialog mDialog;

	private MyAdapter mAdapter;

	Activity mActivity;

	/**
	 * 对话框单例
	 */
	public static DockAddLauncherActionDialog sDockSettingDialog;

	/**
	 * 单例模型，防止快速按两次+号，弹出两个同样的菜单
	 * 
	 * @param activity
	 * @return
	 */
	public static DockAddLauncherActionDialog getDialog(Activity activity) {
		if (sDockSettingDialog == null) {
			sDockSettingDialog = new DockAddLauncherActionDialog(activity);
		}
		return sDockSettingDialog;
	}

	private static void resetDockSettingDialog() {
		sDockSettingDialog = null;
	}

	public DockAddLauncherActionDialog(Activity activity) {
		mActivity = activity;
		createDialog();
	}

	public void createDialog() {
		mAdapter = new MyAdapter(mActivity);

		final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		builder.setTitle(R.string.launcher_action_list);
		builder.setAdapter(mAdapter, this);

		mDialog = builder.create();

		mDialog.setOnCancelListener(this);
		mDialog.setOnDismissListener(this);
	}

	public void show() {
		if (mActivity != null && !mActivity.isFinishing()) {
			// mActivity.isFinishing()用于判断当前activity还是前台运行
			mDialog.show();
		}
	}

	/**
	 * 
	 * @author jiangxuwen
	 * 
	 */
	public final class ViewHolder {
		public TextView title;
	}

	/**
	 * 
	 * @author jiangxuwen
	 * 
	 */
	private class MyAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		private final ArrayList<ListItem> mItems = new ArrayList<ListItem>();

		/**
		 * Specific item in our list.
		 */
		public class ListItem {
			public final CharSequence text;
			public final Drawable image;
			public final int actionTag;

			public ListItem(Resources res, int textResourceId, Drawable drawable, int actionTag) {
				text = res.getString(textResourceId);
				image = drawable;
				this.actionTag = actionTag;
			}
		}

		public MyAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);

			Resources res = context.getResources();

			mItems.add(new ListItem(res, R.string.customname_mainscreen, AddGoShortCutTab
					.getItemImage(null, AddGoShortCutTab.MAIN_SCREEN, mActivity, null),
					AddGoShortCutTab.MAIN_SCREEN));

			mItems.add(new ListItem(res, R.string.customname_mainscreen_or_preview,
					AddGoShortCutTab.getItemImage(null, AddGoShortCutTab.MAIN_SCREEN_OR_PREVIEW,
							mActivity, null), AddGoShortCutTab.MAIN_SCREEN_OR_PREVIEW));

			mItems.add(new ListItem(res, R.string.customname_Appdrawer, AddGoShortCutTab
					.getItemImage(null, AddGoShortCutTab.FUNCMENU, mActivity, null),
					AddGoShortCutTab.FUNCMENU));

			mItems.add(new ListItem(res, R.string.customname_notification, AddGoShortCutTab
					.getItemImage(null, AddGoShortCutTab.NOTIFICATION, mActivity, null),
					AddGoShortCutTab.NOTIFICATION));

			mItems.add(new ListItem(res, R.string.customname_status_bar, AddGoShortCutTab
					.getItemImage(null, AddGoShortCutTab.STATUS_BAR, mActivity, null),
					AddGoShortCutTab.STATUS_BAR));

			mItems.add(new ListItem(res, R.string.customname_themeSetting, AddGoShortCutTab
					.getItemImage(null, AddGoShortCutTab.THEME_SETTING, mActivity, null),
					AddGoShortCutTab.THEME_SETTING));

			mItems.add(new ListItem(res, R.string.customname_preferences, AddGoShortCutTab
					.getItemImage(null, AddGoShortCutTab.PREFERENCES, mActivity, null),
					AddGoShortCutTab.PREFERENCES));

			mItems.add(new ListItem(res, R.string.customname_gostore, AddGoShortCutTab
					.getItemImage(null, AddGoShortCutTab.GO_STORE, mActivity, null),
					AddGoShortCutTab.GO_STORE));

			mItems.add(new ListItem(res, R.string.customname_preview, AddGoShortCutTab
					.getItemImage(null, AddGoShortCutTab.PREVIEW, mActivity, null),
					AddGoShortCutTab.PREVIEW));

			mItems.add(new ListItem(res, R.string.goshortcut_lockscreen, AddGoShortCutTab
					.getItemImage(null, AddGoShortCutTab.LOCK_SCREEN, mActivity, null),
					AddGoShortCutTab.LOCK_SCREEN));

			mItems.add(new ListItem(res, R.string.goshortcut_showdockbar, AddGoShortCutTab
					.getItemImage(null, AddGoShortCutTab.DOCK_BAR, mActivity, null),
					AddGoShortCutTab.DOCK_BAR));

			mItems.add(new ListItem(res, R.string.customname_mainmenu, AddGoShortCutTab
					.getItemImage(null, AddGoShortCutTab.MAIN_MENU, mActivity, null),
					AddGoShortCutTab.MAIN_MENU));

			mItems.add(new ListItem(res, R.string.customname_diygesture, AddGoShortCutTab
					.getItemImage(null, AddGoShortCutTab.DIY_GESTURE, mActivity, null),
					AddGoShortCutTab.DIY_GESTURE));

			mItems.add(new ListItem(res, R.string.customname_photo, AddGoShortCutTab.getItemImage(
					null, AddGoShortCutTab.PHOTO, mActivity, null), AddGoShortCutTab.PHOTO));

			mItems.add(new ListItem(res, R.string.customname_music, AddGoShortCutTab.getItemImage(
					null, AddGoShortCutTab.MUSIC, mActivity, null), AddGoShortCutTab.MUSIC));

			mItems.add(new ListItem(res, R.string.customname_video, AddGoShortCutTab.getItemImage(
					null, AddGoShortCutTab.VIDEO, mActivity, null), AddGoShortCutTab.VIDEO));
		}

		@Override
		public int getCount() {
			return mItems.size();
		}

		@Override
		public Object getItem(int position) {
			if (position >= getCount()) {
				return null;
			} else {
				return mItems.get(position);
			}
		}

		@Override
		public long getItemId(int arg0) {

			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ListItem item = (ListItem) getItem(position);

			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.pick_item, parent, false);
			}

			TextView textView = (TextView) convertView;
			textView.setTag(item);
			textView.setText(item.text);
			if (Machine.isLephone()) {
				textView.setTextColor(Color.WHITE);
				textView.setBackgroundColor(0xb2000000);
			}
			textView.setCompoundDrawablesWithIntrinsicBounds(item.image, null, null, null);
			return convertView;
		}
	}

	// TODO 些接口用于返回用户的选择项，还没实现好，打算用一个布尔的数组打包数据返回给调用者
	public boolean[] getUserSelect() {

		return null;
	}

	@Override
	public void onDismiss(DialogInterface dialog) {

	}

	@Override
	public void onCancel(DialogInterface dialog) {
		resetDockSettingDialog();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (mActivity == null) {
			mDialog.cancel();
			return;
		}
		final String goComponentName = "com.gau.launcher.action";
		ShortCutInfo itemInfo = new ShortCutInfo();
		Intent intent = null;
		String intentString = null;
		String titleString = null;
		final int drawIndex = which;
		ComponentName cmpName = null;

		switch (which) {
			case AddGoShortCutTab.MAIN_SCREEN : {
				intentString = ICustomAction.ACTION_SHOW_MAIN_SCREEN;
				titleString = mActivity.getString(R.string.customname_mainscreen);
			}
				break;

			case AddGoShortCutTab.MAIN_SCREEN_OR_PREVIEW : {
				intentString = ICustomAction.ACTION_SHOW_MAIN_OR_PREVIEW;
				titleString = mActivity.getString(R.string.customname_mainscreen_or_preview);
			}
				break;

			case AddGoShortCutTab.FUNCMENU : {
				intentString = ICustomAction.ACTION_SHOW_FUNCMENU_FOR_LAUNCHER_ACITON;
				titleString = mActivity.getString(R.string.customname_Appdrawer);
			}
				break;

			case AddGoShortCutTab.NOTIFICATION : {
				intentString = ICustomAction.ACTION_SHOW_EXPEND_BAR;
				titleString = mActivity.getString(R.string.customname_notification);
			}
				break;

			case AddGoShortCutTab.STATUS_BAR : {
				intentString = ICustomAction.ACTION_SHOW_HIDE_STATUSBAR;
				titleString = mActivity.getString(R.string.customname_status_bar);
			}
				break;

			case AddGoShortCutTab.THEME_SETTING : {
				intentString = ICustomAction.ACTION_FUNC_SPECIAL_APP_GOTHEME;
				titleString = mActivity.getString(R.string.customname_themeSetting);
			}
				break;

			case AddGoShortCutTab.PREFERENCES : {
				intentString = ICustomAction.ACTION_SHOW_PREFERENCES;
				titleString = mActivity.getString(R.string.customname_preferences);
			}
				break;

			case AddGoShortCutTab.GO_STORE : {
				intentString = ICustomAction.ACTION_FUNC_SPECIAL_APP_GOSTORE;
				titleString = mActivity.getString(R.string.customname_gostore);
			}
				break;

			case AddGoShortCutTab.PREVIEW : {
				intentString = ICustomAction.ACTION_SHOW_PREVIEW;
				titleString = mActivity.getString(R.string.customname_preview);
			}
				break;

			case AddGoShortCutTab.MAIN_MENU : {
				intentString = ICustomAction.ACTION_SHOW_MENU;
				titleString = mActivity.getString(R.string.customname_mainmenu);
			}
				break;

			case AddGoShortCutTab.LOCK_SCREEN : {
				intentString = ICustomAction.ACTION_ENABLE_SCREEN_GUARD;
				titleString = mActivity.getString(R.string.goshortcut_lockscreen);
			}
				break;
			case AddGoShortCutTab.DOCK_BAR : {
				intentString = ICustomAction.ACTION_SHOW_DOCK;
				titleString = mActivity.getString(R.string.goshortcut_showdockbar);
			}
				break;
			case AddGoShortCutTab.DIY_GESTURE : {
				intentString = ICustomAction.ACTION_SHOW_DIYGESTURE;
				titleString = mActivity.getString(R.string.customname_diygesture);
			}
				break;
			case AddGoShortCutTab.PHOTO : {
				intentString = ICustomAction.ACTION_SHOW_PHOTO;
				titleString = mActivity.getString(R.string.customname_photo);
			}
				break;
			case AddGoShortCutTab.MUSIC : {
				intentString = ICustomAction.ACTION_SHOW_MUSIC;
				titleString = mActivity.getString(R.string.customname_music);
			}
				break;
			case AddGoShortCutTab.VIDEO : {
				intentString = ICustomAction.ACTION_SHOW_VIDEO;
				titleString = mActivity.getString(R.string.customname_video);
			}
				break;

			default :
				break;
		}

		intent = new Intent(intentString);
		cmpName = new ComponentName(goComponentName, intentString);
		intent.setComponent(cmpName);
		itemInfo.mIntent = intent;
		itemInfo.mItemType = IItemType.ITEM_TYPE_SHORTCUT;
		itemInfo.mTitle = titleString;
		MyAdapter.ListItem item = (MyAdapter.ListItem) mAdapter.getItem(drawIndex);
		if (item != null) {
			itemInfo.mIcon = ((MyAdapter.ListItem) mAdapter.getItem(drawIndex)).image;
		}
		GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME, IDiyMsgIds.DOCK_ADD_SHORTCUT, -1,
				itemInfo, null);
		itemInfo = null;
		intent = null;
		cmpName = null;
		mDialog.cancel();
	}

	/**
	 * @return the mIsShowTurnScreen
	 */
	public boolean mIsShowTurnScreen() {
		return mIsShowTurnScreen;
	}

	/**
	 * @param mIsShowTurnScreen
	 *            the mIsShowTurnScreen to set
	 */
	public void setShowTurnScreen(boolean mIsShowTurnScreen) {
		this.mIsShowTurnScreen = mIsShowTurnScreen;
	}

	/**
	 * 外部调用，关闭此对话框
	 */
	public static void close() {
		if (sDockSettingDialog != null) {
			if (sDockSettingDialog.mDialog != null) {
				sDockSettingDialog.mDialog.cancel();
			}
		}
	}

}
