package com.jiubang.ggheart.data;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

import com.gau.go.launcherex.R;
import com.go.util.log.LogConstants;
import com.jiubang.ggheart.data.info.ThemeSettingInfo;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 
 * @author 
 *
 */
public class ForegroundDialog extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.foregound);

		Button btnYes = (Button) findViewById(R.id.yes_button);
		Button btnNo = (Button) findViewById(R.id.no_button);
		btnYes.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onYes();
			}
		});
		btnNo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onNo();
			}
		});
	}

	private void onNo() {
		// Context context = GOLauncherApp.getContext();
		// SharedPreferences spf = context.getSharedPreferences("Foregound", 0);
		// Editor edit = spf.edit();
		// edit.putBoolean("NEEDFOREGOUND", false);
		// try
		// {
		// edit.commit();
		// } catch (Exception e)
		// {
		// e.printStackTrace();
		// }
		ThemeSettingInfo mThemeInfo = GOLauncherApp.getSettingControler().getThemeSettingInfo();
		mThemeInfo.mIsPemanentMemory = false;
		GOLauncherApp.getSettingControler().updateThemeSettingInfo(mThemeInfo);
		stopAppService();
		finish();
		GOLauncherApp.getApplication().exit(true);
	}

	private void stopAppService() {
		Intent intent = new Intent();
		intent.setClass(getApplicationContext(), AppService.class);
		getApplicationContext().stopService(intent);
	}

	private void onYes() {
		finish();
	}
	
	@Override
	public void onBackPressed() {
		try {
			super.onBackPressed();
		} catch (Exception e) {
			Log.e(LogConstants.HEART_TAG, "onBackPressed err " + e.getMessage());
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
}
