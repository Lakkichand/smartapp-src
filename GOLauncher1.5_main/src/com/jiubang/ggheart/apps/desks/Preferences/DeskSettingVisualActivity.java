package com.jiubang.ggheart.apps.desks.Preferences;

import java.net.URI;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.appfunc.setting.FunAppSetting;
import com.jiubang.ggheart.apps.desks.Preferences.view.DeskSettingVisualBackgroundTabView;
import com.jiubang.ggheart.apps.desks.Preferences.view.DeskSettingVisualView;
import com.jiubang.ggheart.apps.desks.diy.CustomIconUtil;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.IRequestCodeIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.DockLogicControler;
import com.jiubang.ggheart.apps.desks.golauncherwallpaper.ChooseWallpaper;
import com.jiubang.ggheart.components.DeskToast;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 
 * <br>
 * 类描述:个性化设置activity <br>
 * 功能详细描述:
 * 
 * @author ruxueqin
 * @date [2012-9-12]
 */
public class DeskSettingVisualActivity extends DeskSettingBaseActivity {
	private DeskSettingVisualView mVisualView; // 根view

	public static final int DIALOG_ID_INIT_LIST = 1; // 正在加载主题弹出框

	private PreferencesManager mPreferencesManager = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mVisualView = (DeskSettingVisualView) inflater.inflate(
				R.layout.desk_setting_visual_view, null);
		setContentView(mVisualView);
		load();

		// 处理go桌面主题-设置透明背景
		mPreferencesManager = new PreferencesManager(this,
				IPreferencesIds.DESK_SETTING_VISUAL_BACKGROUND_TAB_VIEW,
				Context.MODE_WORLD_WRITEABLE);
	}

	@Override
	protected void onStop() {
		super.onStop();

		removeDialog(DIALOG_ID_INIT_LIST);
	}

	@Override
	public void load() {
		mVisualView.load();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		mVisualView.changeOrientation();
	}

	@Override
	public void save() {
		mVisualView.save();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		ProgressDialog dialog = null;
		if (id == DIALOG_ID_INIT_LIST) {
			dialog = new ProgressDialog(this);
			dialog.setMessage(getString(R.string.icon_style_loading_message));
			dialog.setCancelable(true);
			dialog.setOnCancelListener(new OnCancelListener() {

				@Override
				public void onCancel(DialogInterface dialog) {
					finish();
				}
			});
		}
		return dialog;
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mVisualView != null) {
			mVisualView.onResume();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (null == data) {
			// 回滚回之前的值
			if (IRequestCodeIds.REQUEST_OPERATION_SELECT_BACKGROUND == requestCode
					|| DeskSettingVisualBackgroundTabView.DRAWERBG_CLIP_CODE == requestCode) {
				mVisualView.restoreAppdrawerBgType();
			} else if (IRequestCodeIds.DOCK_CROP_CUSTOM_BG == requestCode
					|| IRequestCodeIds.REQUEST_CHANGE_CROP_ICON == requestCode
					|| IRequestCodeIds.DOCK_GO_THEME_BG == requestCode) {
				mVisualView.restoreDockBgType();
			}
			return;
		}

		switch (requestCode) {
		case IRequestCodeIds.DOCK_GO_THEME_BG: {
			if (resultCode == IRequestCodeIds.REQUEST_OPERATION_SELECT_DOCK_BACKGROUND
					&& null != AppCore.getInstance()) {
				// DOCK背景选择器
				Bundle bundle = data.getExtras();
				String pkgName = bundle
						.getString(ChooseWallpaper.BACGROUND_IMG_RESPKGNAME);
				String resName = bundle
						.getString(ChooseWallpaper.BACGROUND_IMG_NAME);

				if (pkgName.equals(ChooseWallpaper.TRANSPARENT_BG)) {
					mPreferencesManager
							.putBoolean(
									IPreferencesIds.DESK_SETTING_VISUAL_BACKGROUND_TAB_VIEW_DOCK,
									true);
					mPreferencesManager.commit();
					GOLauncherApp.getSettingControler()
							.updateCurThemeShortCutSettingBgSwitch(false);
					GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME,
							IDiyMsgIds.CLEAR_BG, -1, null, null);
					GoLauncher.sendMessage(this, IDiyFrameIds.SHELL_FRAME,
							IDiyMsgIds.CLEAR_BG, -1, null, null);
					mVisualView.showAndUpdateDockPicTransparent();
				} else {
					mPreferencesManager
							.putBoolean(
									IPreferencesIds.DESK_SETTING_VISUAL_BACKGROUND_TAB_VIEW_DOCK,
									false);
					mPreferencesManager.commit();
					GOLauncherApp.getSettingControler().updateShortCutBg(
							GOLauncherApp.getThemeManager()
									.getCurThemePackage(), pkgName, resName,
							false);

					// save();
					GOLauncherApp.getSettingControler()
							.updateCurThemeShortCutSettingBgSwitch(true);
					GOLauncherApp.getSettingControler()
							.updateCurThemeShortCutSettingCustomBgSwitch(true);
					GOLauncherApp.getSettingControler().updateShortCutCustomBg(
							false);
					GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME,
							IDiyMsgIds.UPDATE_DOCK_BG, -1, null, null);
					GoLauncher.sendMessage(this, IDiyFrameIds.SHELL_FRAME,
							IDiyMsgIds.UPDATE_DOCK_BG, -1, null, null);
					mVisualView.showAndUpdateDockPic();

				}

			} else {
				mVisualView.restoreDockBgType();
			}
		}
			break;

		case IRequestCodeIds.DOCK_CROP_CUSTOM_BG: {
			if (resultCode == Activity.RESULT_OK) {
				gotoCropCustomBg(data);
			}
		}
			break;

		case IRequestCodeIds.REQUEST_CHANGE_CROP_ICON: {
			if (resultCode == Activity.RESULT_OK) {
				try {
					GoSettingControler controler = GOLauncherApp
							.getSettingControler();
					controler.updateShortCutBg(GOLauncherApp.getThemeManager()
							.getCurThemePackage(), null, DockLogicControler
							.getDockBgReadFilePath(), true);

					// save();
					GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME,
							IDiyMsgIds.UPDATE_DOCK_BG, -1, null, null);
					GoLauncher.sendMessage(this, IDiyFrameIds.SHELL_FRAME,
							IDiyMsgIds.UPDATE_DOCK_BG, -1, null, null);
				} catch (Exception e) {
					// 防空指针
					e.printStackTrace();
				}
			}
			mVisualView.showAndUpdateDockPic();
		}
			break;

		case IRequestCodeIds.REQUEST_OPERATION_SELECT_BACKGROUND: {
			try {
				// 获取数据
				Uri imageUri = data.getData();
				// 不裁剪
				if (null == imageUri) {
					Bundle bundle = data.getExtras();
					if (null != bundle) {
						String pn = bundle
								.getString(ChooseWallpaper.BACGROUND_IMG_RESPKGNAME);
						if (pn.endsWith(ChooseWallpaper.TRANSPARENT_BG)) {
							mPreferencesManager
									.putBoolean(
											IPreferencesIds.DESK_SETTING_VISUAL_BACKGROUND_TAB_VIEW_APPDRAWER,
											true);
							mPreferencesManager.commit();
							mVisualView.saveAppdrawerPic(FunAppSetting.BG_NON,
									FunAppSetting.DEFAULTBGPATH);
							mVisualView.showAndUpdateAppdrawerPicTransparent();
						} else {
							mPreferencesManager
									.putBoolean(
											IPreferencesIds.DESK_SETTING_VISUAL_BACKGROUND_TAB_VIEW_APPDRAWER,
											false);
							mPreferencesManager.commit();
							String name = bundle
									.getString(ChooseWallpaper.BACGROUND_IMG_RESNAME);
							URI uri = new URI(
									DeskSettingVisualBackgroundTabView.BGSETTINGTAG,
									pn, name);

							// 成功更换功能表背景主题图片
							mVisualView.saveAppdrawerPic(
									FunAppSetting.BG_GO_THEME, uri.toString());
							mVisualView.showAndUpdateAppdrawerPic();
						}

					}
				} else {
					// 调用照相机的裁剪
					mVisualView.clipAppdrawerPic(imageUri);
				}
			} catch (Exception e) {
				String textString = this.getString(R.string.NotFindCROP);
				DeskToast.makeText(this, textString, Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}

		}
			break;

		case DeskSettingVisualBackgroundTabView.DRAWERBG_CLIP_CODE: {
			// 成功更换功能表自定义背景
			// 通知功能表背景改变
			mVisualView.saveAppdrawerPic(FunAppSetting.BG_CUSTOM);
			mVisualView.showAndUpdateAppdrawerPic();
		}
			break;

		default:
			break;
		}
	}

	/**
	 * 进入自定义图标
	 */
	private void gotoCropCustomBg(Intent data) {
		Intent intent = CustomIconUtil.getCropImageIntent(this, data,
				CustomIconUtil.DOCK_BG);
		if (intent != null) {
			startActivityForResult(intent,
					IRequestCodeIds.REQUEST_CHANGE_CROP_ICON);
		}
	}
}
