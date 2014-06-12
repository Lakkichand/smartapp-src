package com.jiubang.ggheart.apps.desks.Preferences;

import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.gau.go.launcherex.R;
import com.go.util.window.OrientationControl;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogConfirm;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogFactory;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogTypeId;
import com.jiubang.ggheart.apps.desks.Preferences.view.DeskSettingItemBaseView;
import com.jiubang.ggheart.apps.desks.Preferences.view.DeskSettingPageTitleView;
import com.jiubang.ggheart.components.TouchHelperChooser;
import com.jiubang.ggheart.components.facebook.GoFacebookUtil;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.plugin.mediamanagement.AppInfo;

/**
 * 
 * <br>
 * 类描述:Go设置的主界面 <br>
 * 功能详细描述: 圣诞节特效
 * 
 * @author zhujian
 * @date [2012-12-03]
 */
public class DeskSettingMainActivity extends DeskSettingBaseActivity implements
		OnClickListener {
	/**
	 * 个性化设置
	 */
	private DeskSettingItemBaseView mSettingVisual;

	/**
	 * 界面设置
	 */
	private DeskSettingItemBaseView mSettingScreen;

	/**
	 * 应用设置
	 */
	private DeskSettingItemBaseView mSettingApp;

	/**
	 * 高级设置
	 */
	private DeskSettingItemBaseView mSettingAdvanced;

	/**
	 * 备份
	 */
	private DeskSettingItemBaseView mSettingBackup;
	
	/**
	 * facebook
	 */
	private DeskSettingItemBaseView mFacebook;

	/**
	 * touchhelper
	 */
	private DeskSettingItemBaseView mTouchhelper;
	
	/**
	 * 关于
	 */
	private DeskSettingItemBaseView mSettingAbout;

	/**
	 * 退出
	 */
	private DeskSettingItemBaseView mSettingExit;
	
	/**
	 * touchhelper弹框
	 */
	private TouchHelperChooser mTouchHelperChooser;

	/**
	 * 圣诞节效果
	 */
//	private DeskSettingItemChristMasView mSettingChristMas;

	/**
	 * 保存圣诞节特效状态的PreferencesManager
	 */
//	private PreferencesManager mPreferencesManager = null;

	// private DialogConfirm mNormalDialog; //普通对话框

	private static final int REQUEST_CODE_VISUAL_SETTING = 0;
	private static final int REQUEST_CODE_APP_SETTING = 1;
	private static final int REQUEST_CODE_GO_ADVANCED_SETTING = 2;
	private static final int REQUEST_CODE_BACKUP_SETTING = 3;
	private static final int REQUEST_CODE_FACEBOOK_SETTING = 4;

	// 从高级设置界面返回时的返回码
	public static final int RESULT_CODE_RESTART_GO_LAUNCHER = 100;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.desk_setting_main);

		DeskSettingPageTitleView titleView = (DeskSettingPageTitleView) findViewById(R.id.main_title);
		titleView.getBackLayout().setClickable(false); // 设置点击事件。取消父类默认的返回事件

		// ======常规设置====
		// 个性化设置
		mSettingVisual = (DeskSettingItemBaseView) findViewById(R.id.setting_visual);
		mSettingVisual.setOnClickListener(this);

		// 桌面设置
		mSettingScreen = (DeskSettingItemBaseView) findViewById(R.id.setting_screen);
		mSettingScreen.setOpenIntent(new Intent(this,
				DeskSettingScreenActivity.class));

		// 应用设置
		mSettingApp = (DeskSettingItemBaseView) findViewById(R.id.setting_app);
		mSettingApp.setOnClickListener(this);

		// 高级设置
		mSettingAdvanced = (DeskSettingItemBaseView) findViewById(R.id.setting_advanced);
		mSettingAdvanced.setOnClickListener(this);

		// ======其他设置=====
		// 恢复&备份
		mSettingBackup = (DeskSettingItemBaseView) findViewById(R.id.setting_backup);
		mSettingBackup.setOnClickListener(this);
		
		// facebook
		mFacebook = (DeskSettingItemBaseView) findViewById(R.id.setting_facebook);
		mFacebook.setOnClickListener(this);
		//中文不显示Facebook
		if (!GoFacebookUtil.isEnable()) {
			mFacebook.setVisibility(View.GONE);
		} else {
			updateFacebookView();
		}
		
		//touchhelper
		initTouchhelperItem();

		// 关于
		mSettingAbout = (DeskSettingItemBaseView) findViewById(R.id.setting_about);
		mSettingAbout.setOpenIntent(new Intent(this,
				DeskSettingAboutActivity.class));

		// 退出GO桌面
		mSettingExit = (DeskSettingItemBaseView) findViewById(R.id.setting_exit);
		mSettingExit.setOnClickListener(this);
	}
	
	private void initTouchhelperItem() {
		mTouchhelper = (DeskSettingItemBaseView) findViewById(R.id.setting_touchhelper);
		
		List<AppInfo> appInfos = TouchHelperChooser.getAllTouchhelper(this);
		if (appInfos == null || appInfos.size() <= 1) {
			mTouchhelper.setVisibility(View.GONE);
		} else {
			mTouchhelper.setOnClickListener(this);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.setting_visual:
			startActivityForResult(new Intent(this,
					DeskSettingVisualActivity.class),
					REQUEST_CODE_VISUAL_SETTING);
			break;

		case R.id.setting_app:
			startActivityForResult(new Intent(this,
					DeskSettingAppActivity.class), REQUEST_CODE_APP_SETTING);
			break;

		case R.id.setting_advanced:
			startActivityForResult(new Intent(this,
					DeskSettingAdvancedActivity.class),
					REQUEST_CODE_GO_ADVANCED_SETTING);
			break;

		case R.id.setting_backup:
			startActivityForResult(new Intent(this,
					DeskSettingBackupActivity.class),
					REQUEST_CODE_BACKUP_SETTING);
			
			break;
			
		case R.id.setting_facebook:
			startActivityForResult(new Intent(this,
					DeskSettingFacebookActivity.class),
					REQUEST_CODE_FACEBOOK_SETTING);
			break;
			
		case R.id.setting_touchhelper:
			mTouchHelperChooser = new TouchHelperChooser(this);
			mTouchHelperChooser.showDialog();
			break;

		// 退出桌面
		case R.id.setting_exit:
			if (isDefault()) {
				showClearDefaultDialog(); // 显示清除默认桌面提示对话框
			} else {
				sendExit(false);
			}
			break;

		default:
			break;
		}
	}
	
	/**
	 * <br>
	 * 功能简述:显示清除默认桌面提示对话框 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 */
	synchronized void showClearDefaultDialog() {
		DialogConfirm mNormalDialog = (DialogConfirm) DialogFactory
				.produceDialog(this, DialogTypeId.TYPE_NORMAL_MESSAGE);
		mNormalDialog.show();
		mNormalDialog.setTitle(getString(R.string.clearDefault_title));
		mNormalDialog.setMessage(getString(R.string.clearDefault));
		mNormalDialog.setPositiveButton(null, new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					clearDefault();
					sendExit(false);
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		});

		mNormalDialog.setNegativeButton(null, new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					sendExit(false);
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * <br>
	 * 功能简述:退出桌面 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param restart
	 *            ture:重启 ，false：退出
	 */
	private void sendExit(boolean restart) {
		Intent i = getIntent();
		Bundle b = new Bundle();
		b.putInt("exit", 1);
		i.putExtras(b);
		setResult(RESULT_OK, i);
		this.finish();
		GOLauncherApp.getApplication().exit(restart);  // 修复ADT-9456 4.2系统，恢复默认设置后不会自动显示引导页，且桌面行列数没有自适应
		// GOLauncherApp.getApplication().exit(restart)放在finish（）前面导致没有把当前页面杀掉
	}

	/**
	 * <br>
	 * 功能简述:判断是否默认使用GO桌面 <br>
	 * 功能详细描述:点击HOME键弹出的默认使用此应用 <br>
	 * 注意:
	 * 
	 * @return
	 */
	private boolean isDefault() {
		PackageManager pm = this.getPackageManager();
		boolean isDefault = false;
		List<ComponentName> prefActList = new ArrayList<ComponentName>();
		// Intent list cannot be null. so pass empty list
		List<IntentFilter> intentList = new ArrayList<IntentFilter>();
		pm.getPreferredActivities(intentList, prefActList, null);
		if (0 != prefActList.size()) {
			for (int i = 0; i < prefActList.size(); i++) {
				if (this.getPackageName().equals(
						prefActList.get(i).getPackageName())) {
					isDefault = true;
					break;
				}
			}
		}
		return isDefault;
	}
	
	public void updateFacebookView() {
//		Session session = Session.getActiveSession();
//		if (session != null && session.isOpened()) {
			mFacebook.setTitleText(R.string.facebook_setting);
//		} else {
//			mFacebook.setTitleText(R.string.facebook_connect_with);
//		}
	}

	/**
	 * <br>
	 * 功能简述:清除默认使用GO桌面 <br>
	 * 功能详细描述:点击HOME键弹出的默认使用此应用 <br>
	 * 注意:
	 */
	private void clearDefault() {
		PackageManager pm = this.getPackageManager();
		pm.clearPackagePreferredActivities(this.getPackageName());
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {

		case REQUEST_CODE_APP_SETTING:
			// 检查屏幕翻转设置，并应用
			OrientationControl.setOrientation(this);
			break;

		case REQUEST_CODE_VISUAL_SETTING:
		case REQUEST_CODE_GO_ADVANCED_SETTING:
		case REQUEST_CODE_BACKUP_SETTING:
			if (resultCode == RESULT_CODE_RESTART_GO_LAUNCHER) {
				sendExit(true);
			}
			break;

		default:
			//facebook
//			Session session = Session.getActiveSession();
//			if (session != null) {
//				session.onActivityResult(this, requestCode, resultCode, data);
//			}
			break;
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
		if (mTouchHelperChooser != null && mTouchhelper.isShown()) {
			mTouchHelperChooser.configurationChange(newConfig.orientation);
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
//		updateFacebookView();
	}
}
