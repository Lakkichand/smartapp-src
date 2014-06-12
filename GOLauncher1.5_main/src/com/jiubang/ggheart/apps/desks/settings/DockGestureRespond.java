package com.jiubang.ggheart.apps.desks.settings;

import java.util.ArrayList;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.ConvertUtils;
import com.go.util.device.Machine;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingConstants;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IRequestCodeIds;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.DockIconView;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.IDockSettingMSG;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.OnDockSettingListener;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenModifyFolderActivity;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.AppDataEngine;
import com.jiubang.ggheart.data.SysShortCutControler;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.SysShortCutItemInfo;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 显示设置
 * 
 * @author ruxueqin
 * 
 */
public class DockGestureRespond
		implements
			DialogInterface.OnClickListener,
			DialogInterface.OnCancelListener,
			DialogInterface.OnDismissListener {
	// 这个只是用来模拟模式的临时变量
	private static final int ESTOP = 0;
	private static final int OPENAPP = 1;
	private static final int OPENSHORTCUT = 2;
	private static final int OPENGOSHORTCUT = 3;
	private static final int SHOWMAINSCREEN = 0;
	private static final int MAINSCREENFIRST = 1;
	private static final int SHOWPREVIEW = 2;
	private static final int OPENAPPFUNC = 3;
	private static final int SHOWEXPENDBAR = 4;
	private static final int SHOWORHIDESTATEBAR = 5;
	// private static final int ShowDock = 6;
	private static final int LOCKER = 6;
	private static final int OPENGOSTORE = 7;
	private static final int OPENTHEMESETTING = 8;
	private static final int OPENPREFERENCE = 9;
	private static final int OPENMAINMENU = 10;
	private static final int DIYGESTURE = 11;
	private static final int PHOTO = 12;
	private static final int MUSIC = 13;
	private static final int VIDEO = 14;

	public static final String GESID_STRING = "GesID";

	AlertDialog mDialog;
	AlertDialog mGoShortCutDialog;

	Activity mActivity;

	// 数据
	public OnDockSettingListener mListener = null;

	/**
	 * 对话框单例
	 */
	public static DockGestureRespond sDockGestureRespond;

	private DockIconView mCurrentIcon;

	/**
	 * 单例模型
	 * 
	 * @param activity
	 * @return
	 */
	public static DockGestureRespond getDockGestureRespond(Activity activity) {
		if (sDockGestureRespond == null) {
			sDockGestureRespond = new DockGestureRespond(activity);
		}
		return sDockGestureRespond;
	}

	private static void resetDockGestureRespond() {
		sDockGestureRespond = null;
	}

	public DockGestureRespond(Activity activity) {
		mActivity = activity;
		createDialog();

		ArrayList<DockIconView> list = new ArrayList<DockIconView>();
		GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME, IDiyMsgIds.DOCK_CURRENT_ICON, -1,
				null, list);
		if (!list.isEmpty()) {
			mCurrentIcon = list.get(0);
		}
		list.clear();
		list = null;
	}

	public void createGoShortCutDialog() {
		GoShortCutAdapter adapter = new GoShortCutAdapter(mActivity);

		final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		builder.setTitle(R.string.title_goLauncher_shortcut_dialog);
		builder.setAdapter(adapter, this);

		// test
		// CharSequence[] arg0 = {"aaaaa", "bbbbb", "ccccc"};
		// builder.setSingleChoiceItems(arg0, 1, null);
		// test over

		builder.setInverseBackgroundForced(true);

		mGoShortCutDialog = builder.create();
		mGoShortCutDialog.setOnCancelListener(this);
		mGoShortCutDialog.setOnDismissListener(this);
	}

	public void createDialog() {
		MyAdapter adapter = new MyAdapter(mActivity);

		final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		builder.setTitle(R.string.selecticongesture);
		builder.setAdapter(adapter, this);

		// test
		// CharSequence[] arg0 = {"aaaaa", "bbbbb", "ccccc"};
		// builder.setSingleChoiceItems(arg0, 1, null);
		// test over

		builder.setInverseBackgroundForced(true);

		mDialog = builder.create();
		mDialog.setOnCancelListener(this);
		mDialog.setOnDismissListener(this);
	}

	public void show(OnDismissListener dismissListener) {

		mDialog.setOnDismissListener(dismissListener);

		if (mActivity != null && !mActivity.isFinishing()) {
			// mActivity.isFinishing()用于判断当前activity还是前台运行
			mDialog.show();
		}

	}
	
	/**
	 * 
	 * @author ruxueqin
	 * 
	 */
	public final class ViewHolder {
		public TextView title;
	}

	/**
	 * 
	 * @author ruxueqin
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

			public ListItem(Resources res, int textResourceId, int imageResourceId, int actionTag) {
				text = res.getString(textResourceId);
				if (imageResourceId != -1) {
					image = res.getDrawable(imageResourceId);
				} else {
					image = null;
				}
			}
		}

		public MyAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);

			Resources res = context.getResources();

			mItems.add(new ListItem(res, R.string.disable, R.drawable.shortcut_dialog_blank, ESTOP));

			mItems.add(new ListItem(res, R.string.open_App, R.drawable.shortcut_dialog_application,
					OPENAPP));

			mItems.add(new ListItem(res, R.string.add_app_icon,
					R.drawable.shortcut_dialog_shortcut, OPENSHORTCUT));
			mItems.add(new ListItem(res, R.string.title_goLauncher_shortcut_dialog,
					R.drawable.go_shortcut, OPENGOSHORTCUT));
		}

		@Override
		public int getCount() {
			return mItems.size();
		}

		@Override
		public Object getItem(int position) {
			return mItems.get(position);
		}

		@Override
		public long getItemId(int arg0) {

			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ListItem item = (ListItem) getItem(position);

			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.dock_gesture_select, parent, false);
			}

			TextView textView = (TextView) convertView.findViewById(R.id.title);
			textView.setTag(item);
			textView.setText(item.text);
			textView.setCompoundDrawablesWithIntrinsicBounds(item.image, null, null, null);
			DeskSettingConstants.setTextViewTypeFace(textView);
			if (Machine.isLephone()) {
				textView.setTextColor(Color.WHITE);
				convertView.setBackgroundColor(0xb2000000);
			}
			RadioButton botton = (RadioButton) convertView.findViewById(R.id.button);
			if (position == findSelectItem()) {
				botton.setChecked(true);
			} else {
				botton.setChecked(false);
			}

			return convertView;
		}
	}

	// TODO 些接口用于返回用户的选择项，还没实现好，打算用一个布尔的数组打包数据返回给调用者
	public boolean[] getUserSelect() {

		return null;
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCancel(DialogInterface dialog) {
		resetDockGestureRespond();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (mGoShortCutDialog == dialog) {
			setGesture(which);
			dialog.dismiss();
		} else {
			switch (which) {
				case ESTOP :
					mDialog.cancel();
					mListener.onDataChange(IDockSettingMSG.GES_ESTOP);
					break;
				case OPENAPP :
//					Intent intent = new Intent(mActivity, AppList.class);
					Intent intent = new Intent(mActivity, ScreenModifyFolderActivity.class);
					intent.putExtra(ScreenModifyFolderActivity.DOCK_ADD_APPLICATION_GESTURE, true);
					// Bundle b = new Bundle();
					mActivity.startActivityForResult(intent,
							IRequestCodeIds.REQUEST_PICK_APPLICATION_IN_DOCK_GESTURE);

					mDialog.cancel();
					break;
				case OPENSHORTCUT :
					if (mListener != null) {
						mListener.selectShortCut(false);
					}

					mDialog.cancel();
					break;
				case OPENGOSHORTCUT : {
					createGoShortCutDialog();
					mGoShortCutDialog.show();
					mDialog.cancel();
				}
					break;

				default :
					break;
			}
		}
	}

	/**
	 * 根据手势intent匹配对应类型 <br>
	 * TODO:Version3.13把手势类型写入数据库，不用匹配了
	 * 
	 * @return
	 */
	private int findSelectItem() {
		if (mCurrentIcon == null || mCurrentIcon.getInfo() == null) {
			return -1;
		}

		Intent intent = mCurrentIcon.getInfo().mGestureInfo.mUpIntent;
		String intentString = ConvertUtils.intentToString(intent);

		if (intent == null || intentString.contains(ICustomAction.ACTION_NONE)) {
			return ESTOP;
		}
		if (-1 < findGoShortCutSelectItem()) {
			return OPENGOSHORTCUT;
		} else {
			AppDataEngine engine = GOLauncherApp.getAppDataEngine();
			AppItemInfo appInfo = null;
			if (engine != null) {
				appInfo = engine.getAppItem(intent);
			}

			if (appInfo != null) {
				return OPENAPP;
			} else {
				// 找shortcut
				SysShortCutControler sysShortCutControler = AppCore.getInstance()
						.getSysShortCutControler();
				if (sysShortCutControler != null) {
					SysShortCutItemInfo shortcutinfo = sysShortCutControler
							.getSysShortCutItemInfo(intent);
					if (shortcutinfo != null) {
						return OPENSHORTCUT;
					}
				}

				Set<String> categories = intent.getCategories();
				if (categories != null && categories.contains("android.intent.category.LAUNCHER")) {
					return OPENAPP;
				} else {
					return OPENSHORTCUT;
				}
			}
		}
	}

	private int findGoShortCutSelectItem() {
		if (mCurrentIcon == null || mCurrentIcon.getInfo() == null) {
			return -1;
		}

		Intent intent = mCurrentIcon.getInfo().mGestureInfo.mUpIntent;
		String intentString = ConvertUtils.intentToString(intent);
		if (intent == null || intentString.contains(ICustomAction.ACTION_NONE)) {
			return -1;
		}
		if (intentString.contains(ICustomAction.ACTION_SHOW_MAIN_SCREEN)) {
			return SHOWMAINSCREEN;
		} else if (intentString.contains(ICustomAction.ACTION_SHOW_MAIN_OR_PREVIEW)) {
			return MAINSCREENFIRST;
		} else if (intentString.contains(ICustomAction.ACTION_SHOW_PREVIEW)) {
			return SHOWPREVIEW;
		} else if (intentString.contains(ICustomAction.ACTION_SHOW_FUNCMENU)) {
			return OPENAPPFUNC;
		} else if (intentString.contains(ICustomAction.ACTION_SHOW_EXPEND_BAR)) {
			return SHOWEXPENDBAR;
		} else if (intentString.contains(ICustomAction.ACTION_SHOW_HIDE_STATUSBAR)) {
			return SHOWORHIDESTATEBAR;
		} else if (intentString.contains(ICustomAction.ACTION_ENABLE_SCREEN_GUARD)) {
			return LOCKER;
		} else if (intentString.contains(ICustomAction.ACTION_FUNC_SPECIAL_APP_GOSTORE)) {
			return OPENGOSTORE;
		} else if (intentString.contains(ICustomAction.ACTION_FUNC_SPECIAL_APP_GOTHEME)) {
			return OPENTHEMESETTING;
		} else if (intentString.contains(ICustomAction.ACTION_SHOW_PREFERENCES)) {
			return OPENPREFERENCE;
		} else if (intentString.contains(ICustomAction.ACTION_SHOW_MENU)) {
			return OPENMAINMENU;
		} else if (intentString.contains(ICustomAction.ACTION_SHOW_DIYGESTURE)) {
			return DIYGESTURE;
		} else if (intentString.contains(ICustomAction.ACTION_SHOW_PHOTO)) {
			return PHOTO;
		} else if (intentString.contains(ICustomAction.ACTION_SHOW_MUSIC)) {
			return MUSIC;
		} else if (intentString.contains(ICustomAction.ACTION_SHOW_VIDEO)) {
			return VIDEO;
		}
		return -1;
	}

	/**
	 * 外部调用，关闭此对话框
	 */
	public static void close() {
		if (sDockGestureRespond != null) {
			if (sDockGestureRespond.mDialog != null) {
				sDockGestureRespond.mDialog.cancel();
			}
		}
	}

	private void setGesture(int gesture) {
		switch (gesture) {
			case SHOWMAINSCREEN :
				mListener.onDataChange(IDockSettingMSG.GES_SHOW_MAIN_SCREEN);
				break;
			case MAINSCREENFIRST :
				mListener.onDataChange(IDockSettingMSG.GES_MAIN_SCREEN_FIRST);
				break;
			case SHOWPREVIEW :
				mListener.onDataChange(IDockSettingMSG.GES_SHOW_PREVIEW);
				break;
			case OPENAPPFUNC :
				mListener.onDataChange(IDockSettingMSG.GES_SHOW_HIDE_FUNCLIST);
				break;
			case SHOWEXPENDBAR :
				mListener.onDataChange(IDockSettingMSG.GES_SHOW_EXPEND_BAR);
				break;
			case SHOWORHIDESTATEBAR :
				mListener.onDataChange(IDockSettingMSG.GES_SHOW_HIDE_STATEBAR);
				break;
			case LOCKER :
				mListener.onDataChange(IDockSettingMSG.GES_LOCK_SCREEN);
				break;
			case OPENGOSTORE :
				mListener.onDataChange(IDockSettingMSG.GES_OPEN_GOSTORE);
				break;
			case OPENTHEMESETTING :
				mListener.onDataChange(IDockSettingMSG.GES_OPEN_THEMESETTIGN);
				break;
			case OPENPREFERENCE :
				mListener.onDataChange(IDockSettingMSG.GES_OPEN_PREFERENCE);
				break;
			case OPENMAINMENU :
				mListener.onDataChange(IDockSettingMSG.GES_SHOW_MENU);
				break;
			case DIYGESTURE :
				mListener.onDataChange(IDockSettingMSG.GES_SHOW_DIYGESTURE);
				break;
			case PHOTO :
				mListener.onDataChange(IDockSettingMSG.GES_SHOW_PHOTO);
				break;
			case MUSIC :
				mListener.onDataChange(IDockSettingMSG.GES_SHOW_MUSIC);
				break;
			case VIDEO :
				mListener.onDataChange(IDockSettingMSG.GES_SHOW_VIDEO);
				break;
		}

	}
	
	/**
	 * 
	 * @author ruxueqin
	 * 
	 */
	public class GoShortCutAdapter extends BaseAdapter {

		private LayoutInflater mInflater;

		private final ArrayList<ListItem> mItems = new ArrayList<ListItem>();

		/**
		 * Specific item in our list.
		 */
		public class ListItem {
			public final CharSequence text;
			public final Drawable image;
			public final int actionTag;

			public ListItem(Resources res, int textResourceId, int imageResourceId, int actionTag) {
				text = res.getString(textResourceId);
				if (imageResourceId != -1) {
					image = res.getDrawable(imageResourceId);
				} else {
					image = null;
				}
				this.actionTag = actionTag;
			}
		}

		public GoShortCutAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);

			Resources res = context.getResources();

			mItems.add(new ListItem(res, R.string.show_mainscreen,
					R.drawable.go_shortcut_mainscreen, SHOWMAINSCREEN));

			mItems.add(new ListItem(res, R.string.first_mainscreen,
					R.drawable.go_shortcut_main_or_preview, MAINSCREENFIRST));

			mItems.add(new ListItem(res, R.string.show_preview, R.drawable.go_shortcut_preview,
					SHOWPREVIEW));

			mItems.add(new ListItem(res, R.string.show_hide_funcitonmenu,
					R.drawable.go_shortcut_appdrawer, OPENAPPFUNC));

			mItems.add(new ListItem(res, R.string.show_expend_bar,
					R.drawable.go_shortcut_notification, SHOWEXPENDBAR));

			mItems.add(new ListItem(res, R.string.show_hide_shortcotbar,
					R.drawable.go_shortcut_statusbar, SHOWORHIDESTATEBAR));

			// mItems.add(new ListItem(res, R.string.goshortcut_showdockbar,
			// R.drawable.go_shortcut_hide_dock, ShowDock));

			mItems.add(new ListItem(res, R.string.goshortcut_lockscreen,
					R.drawable.go_shortcut_lockscreen, LOCKER));

			mItems.add(new ListItem(res, R.string.customname_gostore, R.drawable.go_shortcut_store,
					OPENGOSTORE));

			mItems.add(new ListItem(res, R.string.customname_themeSetting,
					R.drawable.go_shortcut_themes, OPENTHEMESETTING));

			mItems.add(new ListItem(res, R.string.customname_preferences,
					R.drawable.go_shortcut_preferences, OPENPREFERENCE));

			mItems.add(new ListItem(res, R.string.customname_mainmenu, R.drawable.go_shortcut_menu,
					OPENMAINMENU));

			mItems.add(new ListItem(res, R.string.customname_diygesture,
					R.drawable.go_shortcut_diygesture, DIYGESTURE));
			
			mItems.add(new ListItem(res, R.string.customname_photo,
					R.drawable.go_shortcut_photo, PHOTO));
			
			mItems.add(new ListItem(res, R.string.customname_music,
					R.drawable.go_shortcut_music, MUSIC));
			
			mItems.add(new ListItem(res, R.string.customname_video,
					R.drawable.go_shortcut_video, VIDEO));
		}

		@Override
		public int getCount() {
			return mItems.size();
		}

		@Override
		public Object getItem(int position) {
			return mItems.get(position);
		}

		@Override
		public long getItemId(int arg0) {

			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ListItem item = (ListItem) getItem(position);

			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.dock_gesture_select, parent, false);
			}

			TextView textView = (TextView) convertView.findViewById(R.id.title);
			textView.setTag(item);
			textView.setText(item.text);
			textView.setCompoundDrawablesWithIntrinsicBounds(item.image, null, null, null);
			if (Machine.isLephone()) {
				textView.setTextColor(Color.WHITE);
				convertView.setBackgroundColor(0xb2000000);
			}
			RadioButton botton = (RadioButton) convertView.findViewById(R.id.button);
			if (position == findGoShortCutSelectItem()) {
				botton.setChecked(true);
			} else {
				botton.setChecked(false);
			}

			return convertView;
		}

	}
}
