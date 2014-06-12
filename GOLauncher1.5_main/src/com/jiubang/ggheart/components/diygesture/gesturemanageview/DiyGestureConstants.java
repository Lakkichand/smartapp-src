package com.jiubang.ggheart.components.diygesture.gesturemanageview;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.gesture.Gesture;
import android.gesture.GestureStroke;
import android.graphics.Path;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingConstants;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.components.diygesture.model.DiyGestureInfo;
import com.jiubang.ggheart.components.diygesture.model.DiyGestureModelImpl;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.data.info.SysAppInfo;
import com.jiubang.ggheart.launcher.ICustomAction;

/**
 * 手势参数配置类
 * @author licanhui
 *
 */
public class DiyGestureConstants {
	public final static int APP_REQUEST_CODE = 10; // 应用程序的请求码
	public final static int GOSHORTCUT_REQUEST_CODE = 11; // GO快捷方式的请求码
	public final static int SHORTCUT_PAGE1_REQUEST_CODE = 12; // 系统快捷方式的1级菜单请求码
	public final static int SHORTCUT_PAGE2_REQUEST_CODE = 13; // 系统快捷方式的2级菜单请求码
	public final static int RECOGNISE_REQUEST_CODE = 14; // 手势识别弹框请求码

	public final static String APP_INTENT = "app_intent"; // 响应手势的应用程序intent
	public final static String APP_NAME = "app_name"; // 响应手势的应用程序名的key
	public final static String CHECK_GESTURE_SIZE = "check_gesture_size"; // 第一次打开手势
	public final static String IS_ADD_GESTURE = "is_add_gesture"; // 从输入界面添加手势

	// 响应类型
	public final static int TYPE_APP = 1; // 应用程序
	public final static int TYPE_GOSHORTCUT = 2; // GO快捷方式
	public final static int TYPE_SHORTCUT = 3; // 系统快捷方式

	public final static String CHANGE_GESTURE_NAME = "change_gesture_name"; // 修改手势

	/**
	 * 显示选择响应类型的dialog
	 */
	public static void showGestureAppsDialog(Context context) {
		if (context == null) {
			return;
		}
		final Dialog dialog = new Dialog(context, R.style.Dialog);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.gesture_edit_select_response, null);
		dialog.setContentView(view);
		dialog.show();

		final DiyGestureSelectRespondView selectRespondView = (DiyGestureSelectRespondView) view
				.findViewById(R.id.selectRespondView);
		selectRespondView.setDialog(dialog);

		Button cancelBtn = (Button) view.findViewById(R.id.cancelBtn);
		cancelBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
				DeskSettingConstants.selfDestruct(selectRespondView);
			}
		});
	}

	public static String getGoShortcutName(Context context, Intent intent) {
		String action = intent != null ? intent.getAction() : null;
		if (action == null) {
			return null;
		}

		String ret = null;
		if (ICustomAction.ACTION_SHOW_MAIN_SCREEN.equals(action)) {
			ret = context.getResources().getString(R.string.customname_mainscreen);
		} else if (ICustomAction.ACTION_SHOW_MAIN_OR_PREVIEW.equals(action)) {
			ret = context.getResources().getString(R.string.customname_mainscreen_or_preview);
		} else if (ICustomAction.ACTION_SHOW_FUNCMENU_FOR_LAUNCHER_ACITON.equals(action)) {
			ret = context.getResources().getString(R.string.customname_Appdrawer);
		} else if (ICustomAction.ACTION_SHOW_EXPEND_BAR.equals(action)) {
			ret = context.getResources().getString(R.string.customname_notification);
		} else if (ICustomAction.ACTION_SHOW_HIDE_STATUSBAR.equals(action)) {
			ret = context.getResources().getString(R.string.customname_status_bar);
		} else if (ICustomAction.ACTION_FUNC_SPECIAL_APP_GOTHEME.equals(action)) {
			ret = context.getResources().getString(R.string.customname_themeSetting);
		} else if (ICustomAction.ACTION_SHOW_PREFERENCES.equals(action)) {
			ret = context.getResources().getString(R.string.customname_preferences);
		} else if (ICustomAction.ACTION_FUNC_SPECIAL_APP_GOSTORE.equals(action)) {
			ret = context.getResources().getString(R.string.customname_gostore);
		} else if (ICustomAction.ACTION_SHOW_PREVIEW.equals(action)) {
			ret = context.getResources().getString(R.string.customname_preview);
		} else if (ICustomAction.ACTION_ENABLE_SCREEN_GUARD.equals(action)) {
			ret = context.getResources().getString(R.string.goshortcut_lockscreen);
		} else if (ICustomAction.ACTION_SHOW_DOCK.equals(action)) {
			ret = context.getResources().getString(R.string.goshortcut_showdockbar);
		} else if (ICustomAction.ACTION_SHOW_MENU.equals(action)) {
			ret = context.getResources().getString(R.string.customname_mainmenu);
		} else if (ICustomAction.ACTION_SHOW_DIYGESTURE.equals(action)) {
			ret = context.getResources().getString(R.string.customname_diygesture);
		} else if (ICustomAction.ACTION_SHOW_PHOTO.equals(action)) {
			ret = context.getResources().getString(R.string.customname_photo);
		} else if (ICustomAction.ACTION_SHOW_MUSIC.equals(action)) {
			ret = context.getResources().getString(R.string.customname_music);
		} else if (ICustomAction.ACTION_SHOW_VIDEO.equals(action)) {
			ret = context.getResources().getString(R.string.customname_video);
		}

		return ret;
	}

	/**
	 * 横竖屏切换
	 * 
	 * @param context
	 * @param uiLayout
	 */
	public static void checkLandChange(Context context, LinearLayout uiLayout) {
		int height = (int) context.getResources().getDimension(R.dimen.gesture_ui_layout_height);
		LayoutParams params;
		if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			params = new LayoutParams(height, android.view.ViewGroup.LayoutParams.FILL_PARENT);
		} else {
			params = new LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT, height);
		}
		uiLayout.setLayoutParams(params);
	}

	/**
	 * 返回的结果的处理
	 * 
	 * @param context
	 * @param diyGestureModelImpl
	 * @param mGesture
	 * @param requestCode
	 * @param resultCode
	 * @param intent
	 */
	public static void onActivityResult(Context context, DiyGestureModelImpl diyGestureModelImpl,
			Gesture mGesture, int requestCode, int resultCode, Intent intent) {
		if (intent == null) {
			return;
		}

		switch (requestCode) {
		// 处理应用程序
			case DiyGestureConstants.APP_REQUEST_CODE : {
				if (resultCode == Activity.RESULT_OK) {
					// 处理应用程序返回的数据
					String appName = intent.getStringExtra(DiyGestureConstants.APP_NAME);
					if (appName == null) {
						return;
					}
					Intent addIntent = intent.getParcelableExtra(DiyGestureConstants.APP_INTENT);
					DiyGestureInfo diyGestureInfo = new DiyGestureInfo(appName,
							DiyGestureConstants.TYPE_APP, addIntent, mGesture);
					String typeName = context.getResources().getString(R.string.gesture_app);
					addGesture(diyGestureModelImpl, diyGestureInfo, context, typeName);
				}
				((Activity) context).finish();
			}
				break;

			// 处理GO快捷方式的返回的数据
			case DiyGestureConstants.GOSHORTCUT_REQUEST_CODE : {
				if (resultCode == Activity.RESULT_OK) {
					// 处理应用程序返回的数据
					String appName = intent.getStringExtra(DiyGestureConstants.APP_NAME);
					if (appName == null) {
						return;
					}
					Intent addIntent = intent.getParcelableExtra(DiyGestureConstants.APP_INTENT);
					DiyGestureInfo diyGestureInfo = new DiyGestureInfo(appName,
							DiyGestureConstants.TYPE_GOSHORTCUT, addIntent, mGesture);
					String typeName = context.getResources().getString(R.string.gesture_goshortcut);
					addGesture(diyGestureModelImpl, diyGestureInfo, context, typeName);
				}
				((Activity) context).finish();
			}
				break;

			case DiyGestureConstants.SHORTCUT_PAGE2_REQUEST_CODE : {
				// 处理快捷方式
				if (resultCode == Activity.RESULT_OK) {
					// 获取出intent中包含的应用
					final ShortCutInfo info = SysAppInfo.createFromShortcut(context, intent);
					if (info != null) {
						intent = info.mIntent;
						if (intent != null) {
							String name = (String) info.mTitle;
							DiyGestureInfo diyGestureInfo = new DiyGestureInfo(name,
									DiyGestureConstants.TYPE_SHORTCUT, intent, mGesture);
							String typeName = context.getResources().getString(
									R.string.gesture_shortcut);
							addGesture(diyGestureModelImpl, diyGestureInfo, context, typeName);
						}
					}
				}
				((Activity) context).finish();
			}
				break;

			case DiyGestureConstants.SHORTCUT_PAGE1_REQUEST_CODE :
				if (resultCode == Activity.RESULT_OK) {
					((Activity) context).startActivityForResult(intent,
							DiyGestureConstants.SHORTCUT_PAGE2_REQUEST_CODE);
				}
				break;

			default :
				break;
		}
	}

	private static void addGesture(DiyGestureModelImpl diyGestureModelImpl,
			DiyGestureInfo diyGestureInfo, Context context, String typeName) {
		if (diyGestureModelImpl.addGesture(diyGestureInfo)) {
			diyGestureInfo.setTypeName(typeName);
			diyGestureInfo.setName(diyGestureInfo.getName());
			context.startActivity(new Intent(context, MyGesture.class)); // 添加成功，跳转管理列表
			Toast.makeText(context,
					context.getResources().getString(R.string.add_new_gesture_success),
					Toast.LENGTH_SHORT).show();
			
			//如果添加数据,则永远不弹出关闭手势向导框
			PreferencesManager sharedPreferences = new PreferencesManager(context,
					IPreferencesIds.DESK_SHAREPREFERENCES_FILE, Context.MODE_PRIVATE);
			int time = sharedPreferences.getInt(IPreferencesIds.CANCLE_DIYGESTURE_TIME, 0);
			if (time < 3) {
				sharedPreferences.putInt(IPreferencesIds.CANCLE_DIYGESTURE_TIME, 100);
				sharedPreferences.commit();
			}
		} else {
			Toast.makeText(context,
					context.getResources().getString(R.string.add_new_gesture_fail),
					Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 动画比例
	 * 
	 * @param begin
	 *            开始数据
	 * @param end
	 *            结束数据
	 * @param t
	 *            当前时间
	 * @return
	 */
	public static float easeOut(float begin, float end, float t) {
		t = 1 - t;
		return begin + (end - begin) * (1 - t * t * t);
	}

	/**
	 * 设置预览图第一笔加粗
	 * 
	 * @param gesture
	 *            手势
	 * @param strokeWidth
	 *            画笔大小
	 */
	public static void setFirstPointCircle(Gesture gesture, float strokeWidth) {
		ArrayList<GestureStroke> gestureStrokesList = gesture.getStrokes();
		if (gestureStrokesList != null) {
			int gestureSrokesSize = gestureStrokesList.size();
			for (int i = 0; i < gestureSrokesSize; i++) {
				GestureStroke gestureStroke = gestureStrokesList.get(i);
				if (gestureStroke != null && gestureStroke.length >= 2) {
					float x = gestureStroke.points[0];
					float y = gestureStroke.points[1];
					gestureStroke.getPath().addCircle(x, y, strokeWidth / 2, Path.Direction.CW);
				}
			}
		}
	}
}
