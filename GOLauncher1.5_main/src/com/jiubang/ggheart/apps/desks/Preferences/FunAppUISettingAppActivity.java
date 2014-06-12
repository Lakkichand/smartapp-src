package com.jiubang.ggheart.apps.desks.Preferences;

import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.Preferences.view.DeskSettingScreenAppFunTabView;
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-10-19]
 */
public class FunAppUISettingAppActivity extends DeskSettingBaseActivity {
	DeskSettingScreenAppFunTabView mAppFunTabView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.appfunc_setting_effectorsetting);

		LinearLayout contentLayout = (LinearLayout) findViewById(R.id.content_layout);
		mAppFunTabView = new DeskSettingScreenAppFunTabView(this);
		load();
		contentLayout.addView(mAppFunTabView);

	}

	@Override
	public void load() {
		super.load();
		if (mAppFunTabView != null) {
			mAppFunTabView.load();
		}
	}

	@Override
	public void save() {
		mAppFunTabView.save();
		super.save();
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (mAppFunTabView != null) {
			mAppFunTabView.changeOrientation();
		}
	}

}
