/**
 * 
 */
package com.jiubang.ggheart.screen.livewallpaper;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;

/**
 * @author liguoliang
 *
 */
public class LiveWallpaperSettingActivity extends PreferenceActivity {
	private final static String KEY_GOTO_LAUNCHER = "livewallpaper_goto_launcher";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.livewallpaper_setting);
	}
	
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		String key = preference.getKey();
		if (key == null) {
			return false;
		}
		if (KEY_GOTO_LAUNCHER.equals(key)) {
			startLauncher();
		}
		
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}
	
	private void startLauncher() {
		Intent intent = new Intent(this, GoLauncher.class);
		startActivity(intent);
	}
}
