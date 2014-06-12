package com.jiubang.ggheart.apps.desks.settings;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
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
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.IDockSettingMSG;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.OnDockSettingListener;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenModifyFolderActivity;

/**
 * 显示设置
 * 
 * @author ruxueqin
 * 
 */
public class DockSettingDialog
		implements
			DialogInterface.OnClickListener,
			DialogInterface.OnCancelListener,
			DialogInterface.OnDismissListener {
	// 是否显示“屏幕翻转“选项
	private boolean mIsShowTurnScreen;

	AlertDialog mDialog;

	Activity mActivity;

	public OnDockSettingListener mListener = null;

	/**
	 * 对话框单例
	 */
	public static DockSettingDialog sDockSettingDialog;

	/**
	 * 单例模型，防止快速按两次+号，弹出两个同样的菜单
	 * 
	 * @param activity
	 * @return
	 */
	public static DockSettingDialog getDockSettingDialog(Activity activity) {
		if (sDockSettingDialog == null) {
			sDockSettingDialog = new DockSettingDialog(activity);
		}
		return sDockSettingDialog;
	}

	private static void resetDockSettingDialog() {
		sDockSettingDialog = null;
	}

	public DockSettingDialog(Activity activity) {
		mActivity = activity;
		createDialog();
	}

	private void createDialog() {
		MyAdapter adapter = new MyAdapter(mActivity);

		final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		builder.setTitle(R.string.select_app_icon);
		builder.setAdapter(adapter, this);

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
	 * <br>类描述:
	 * <br>功能详细描述:
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

		public MyAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);

			Resources res = context.getResources();

			mItems.add(new ListItem(res, R.string.open_App, R.drawable.shortcut_dialog_application,
					IDockSettingMSG.OPEN_APP));

			mItems.add(new ListItem(res, R.string.add_app_icon,
					R.drawable.shortcut_dialog_shortcut, IDockSettingMSG.OPEN_WIDGET));

			mItems.add(new ListItem(res, R.string.appfunc, R.drawable.shortcut_dialog_funclist,
					IDockSettingMSG.FUNCLIST));

			mItems.add(new ListItem(res, R.string.blank, R.drawable.shortcut_dialog_blank,
					IDockSettingMSG.BLANK));

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
		switch (which) {
			case IDockSettingMSG.OPEN_APP :
				if (mActivity != null) {
					Intent intent = new Intent(mActivity, ScreenModifyFolderActivity.class);
					intent.putExtra(ScreenModifyFolderActivity.ADD_APP_TO_DOCK_UNFIT, true);
					mActivity.startActivity(intent);
				}
				break;

			case IDockSettingMSG.OPEN_WIDGET :
				if (mListener != null) {
					mListener.selectShortCut(true);
				}
				break;

			case IDockSettingMSG.FUNCLIST :
				if (mListener != null) {
					mListener.setAppFunIcon();
				}
				break;

			case IDockSettingMSG.BLANK :
				if (mListener != null) {
					mListener.setBlank();
				}

			default :
				break;
		}
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
