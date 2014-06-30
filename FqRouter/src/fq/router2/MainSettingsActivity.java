package fq.router2;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import com.smartapp.easyvpn.R;

import fq.router2.utils.ShellUtils;

public class MainSettingsActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        findPreference("OpenManager").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                finish();
                return false;
            }
        });
        if (!ShellUtils.checkRooted()) {
            getPreferenceScreen().removePreference(findPreference("AutoLaunchEnabled"));
            getPreferenceScreen().removePreference(findPreference("NotificationEnabled"));
        }
    }

    private String _(int id) {
        return getResources().getString(id);
    }
}
