package com.jiubang.ggheart.components.diygesture.gesturemanageview;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenEditAddGoLauncher;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenModifyFolderActivity;

public class DiyGestureSelectRespondView extends LinearLayout {
	private ListView mListView;
	private final int DIALOG_TYPE_APP = 0; // 应用程序
	private final int DIALOG_TYPE_GOSHORTCUT = 1; // GO快捷方式
	private final int DIALOG_TYPE_SHORTCUT = 2; // 系统快捷方式

	private Dialog mDialog = null;

	public DiyGestureSelectRespondView(Context context) {
		super(context);
	}

	public DiyGestureSelectRespondView(Context context, AttributeSet attrs) {
		super(context, attrs);
		final Activity activity = (Activity) context;
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.gesture_select_response_view, this);
		mListView = (ListView) findViewById(R.id.selectResponseListView);
		mListView.setAdapter(getSimpleAdapter(context));
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				view.invalidate(); // 刷新一下，不然打开应用程序响应第二次返回，会保持高亮状态
				if (activity == null) {
					return;
				}

				// 选择类型响应
				switch (position) {
				// 打开应用程序的响应
					case DIALOG_TYPE_APP : {
						startAppSelectionBox(activity);
						dismissDialog();
						break;
					}
					// 打开GO快捷方式的响应
					case DIALOG_TYPE_GOSHORTCUT : {
						startGoshortcutSelectionBox(activity);
						dismissDialog();
						break;
					}

					// 打开系统快捷方式
					case DIALOG_TYPE_SHORTCUT : {
						startShortcutSelectionBox(activity);
						dismissDialog();
						break;
					}

					default :
						break;
				}
			}

		});
	}

	/**
	 * 起应用程序选择框
	 * 
	 * @param activity
	 */
	private static void startAppSelectionBox(Activity activity) {
		Intent intent = new Intent();
		intent.setClass(activity, ScreenModifyFolderActivity.class);
		intent.putExtra(ScreenModifyFolderActivity.GESTURE_FOR_APP, true);
		activity.startActivityForResult(intent, DiyGestureConstants.APP_REQUEST_CODE);
	}

	/**
	 * 起GO快捷方式选择框
	 * 
	 * @param activity
	 */
	private static void startGoshortcutSelectionBox(Activity activity) {
		Intent intent = new Intent();
		intent.setClass(activity, ScreenEditAddGoLauncher.class);
		intent.putExtra(ScreenEditAddGoLauncher.GESTURE_FOR_SHORTCUT, true);
		activity.startActivityForResult(intent, DiyGestureConstants.GOSHORTCUT_REQUEST_CODE);
	}

	private static void startShortcutSelectionBox(Activity activity) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_PICK_ACTIVITY);
		intent.putExtra(Intent.EXTRA_INTENT, new Intent(Intent.ACTION_CREATE_SHORTCUT));
		intent.putExtra(Intent.EXTRA_TITLE, activity.getText(R.string.select_app_icon));
		try {
			activity.startActivityForResult(intent, DiyGestureConstants.SHORTCUT_PAGE1_REQUEST_CODE);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(activity, "Activity not founded.", Toast.LENGTH_LONG).show();
		}
	}

	private static SimpleAdapter getSimpleAdapter(Context context) {
		String itemIcon = "itemIcon";
		String itemText = "itemText";
		Resources resources = context.getResources();
		ArrayList<HashMap<String, Object>> data = null;
		HashMap<String, Object> hashMap = null;
		int[] icons = new int[] { R.drawable.gesture_application,
				R.drawable.screen_edit_go_shortcut, R.drawable.tab_add_shortcut_icon };
		String[] names = resources.getStringArray(R.array.gesture_dialog_types);
		if (icons.length != names.length) {
			return null;
		} else {
			data = new ArrayList<HashMap<String, Object>>();
			for (int i = 0; i < names.length; i++) {
				hashMap = new HashMap<String, Object>();
				hashMap.put(itemIcon, icons[i]);
				hashMap.put(itemText, names[i]);
				data.add(hashMap);
			}
		}
		SimpleAdapter simpleAdapter = new SimpleAdapter(context, data,
				R.layout.gesture_dialog_item, new String[] { itemIcon, itemText }, new int[] {
						R.id.gesture_dialog_item_icon, R.id.gesture_dialog_item_text });
		return simpleAdapter;
	}

	/**
	 * 手势列表修改手势列表打开的对话框
	 * 
	 * @param dialog
	 */
	public void setDialog(Dialog dialog) {
		if (dialog != null) {
			mDialog = dialog;
		}
	}

	/**
	 * 
	 * 关闭手势列表修改手势列表打开的对话框
	 */
	public void dismissDialog() {
		if (mDialog != null) {
			mDialog.dismiss();
			mDialog = null;
		}
	}

}
