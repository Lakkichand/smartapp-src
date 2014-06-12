package com.jiubang.ggheart.apps.desks.Preferences;

import com.go.util.window.OrientationControl;
import com.jiubang.ggheart.apps.desks.Preferences.view.DeskSettingItemBaseView;
import com.jiubang.ggheart.components.DeskActivity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * 
 * <br>类描述:设置Activity的父类
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-9-12]
 */
public class DeskSettingBaseActivity extends DeskActivity
		implements
			OnValueChangeListener,
			OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		OrientationControl.setOrientation(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		//activity完全不可见时，save数据
		save();
	}

	/**
	 * <br>功能简述:加载数据
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void load() {

	}

	/**
	 * <br>功能简述:保存数据
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void save() {

	}

	@Override
	public boolean onValueChange(DeskSettingItemBaseView view, Object value) {
		return true;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
			// 实体键盘处于推出状态，在此处添加额外的处理代码
			if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {
				OrientationControl.changeOrientationByKeyboard(this, true, newConfig);
			} else if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
				OrientationControl.changeOrientationByKeyboard(this, false, newConfig);
			}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		//退出时间注销所有DeskTextView
		DeskSettingConstants.selfDestruct(getWindow().getDecorView());
	}
	
}
