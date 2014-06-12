package com.jiubang.ggheart.apps.desks.Preferences;

import android.os.Bundle;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.appfunc.setting.FunAppSetting;
import com.jiubang.ggheart.apps.desks.Preferences.view.DeskSettingItemBaseView;
import com.jiubang.ggheart.apps.desks.Preferences.view.DeskSettingItemCheckBoxView;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-9-10]
 */
public class DeskSettingGestureFunctionActivity extends DeskSettingBaseActivity {
	private FunAppSetting mFunAppSetting;

	/**
	 * 启用上滑手势
	 */
	private DeskSettingItemCheckBoxView mSettingGlideUp;

	/**
	 * 启用下滑手势
	 */
	private DeskSettingItemCheckBoxView mSettingGlideDown;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.desk_setting_gesture_function);

		GoSettingControler goSettingControler = GOLauncherApp.getSettingControler();
		mFunAppSetting = goSettingControler.getFunAppSetting();

		initView();
		load();
	}

	public void initView() {

		mSettingGlideUp = (DeskSettingItemCheckBoxView) findViewById(R.id.glide_up);
		mSettingGlideUp.setOnValueChangeListener(this);

		mSettingGlideDown = (DeskSettingItemCheckBoxView) findViewById(R.id.glide_down);
		mSettingGlideDown.setOnValueChangeListener(this);
	}

	@Override
	public void load() {
		super.load();

		if (mFunAppSetting != null) {
			mSettingGlideUp.setIsCheck(mFunAppSetting.getGlideUpAction() == FunAppSetting.ON);

			mSettingGlideDown.setIsCheck(mFunAppSetting.getGlideDownAction() == FunAppSetting.ON);

			//判断功能表翻屏方向，竖向就不启动
			if (mFunAppSetting.getTurnScreenDirection() == FunAppSetting.SCREEN_SCROLL_VERTICAL) {
				mSettingGlideUp.setEnabled(false);
				mSettingGlideDown.setEnabled(false);
			} else {
				mSettingGlideUp.setEnabled(true);
				mSettingGlideDown.setEnabled(true);
			}
		}
	}

	@Override
	public void save() {
		super.save();
	}


	@Override
	public boolean onValueChange(DeskSettingItemBaseView view, Object value) {
		if (mFunAppSetting != null) {
			if (view == mSettingGlideUp) {
				mFunAppSetting.setGlideUpAction(DeskSettingConstants.boolean2Int((Boolean) value));
			}

			else if (view == mSettingGlideDown) {
				mFunAppSetting
						.setGlideDownAction(DeskSettingConstants.boolean2Int((Boolean) value));
			}
		}

		return true;
	}

}
