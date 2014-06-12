package com.jiubang.ggheart.components;

import java.util.Locale;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.CheckBoxPreference;
import android.preference.DialogPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.AppUtils;
import com.jiubang.ggheart.apps.desks.Preferences.view.DeskSettingItemBaseView;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.desks.diy.frames.drag.ISelfObject;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * DeskResourcesConfiguration
 */

public class DeskResourcesConfiguration implements ISelfObject// ,
// BroadCasterObserver
{
	private static final String ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android";
	private static final String GOLAUNCHER_NAMESPACE = "http://schemas.android.com/apk/res/com.gau.go.launcherex";

	public static final int ERROR_NONE = 0;
	public static final int ERROR_LANGUAGE_IS_INSTALL = 1;
	public static final int ERROR_LANGUAGE_NO_INSTALL = -1;
	public static final int ERROR_LANGUAGE_NEED_UPDATE = -2;

	private DeskResources mDeskResources;
	private int mErrorCode;
	
	private Context mContext;

	private static DeskResourcesConfiguration sInstance;
	
	private Locale mLocale;

	public static synchronized DeskResourcesConfiguration getInstance() {
		return sInstance;
	}

	public static synchronized DeskResourcesConfiguration createInstance(Context context) {
		if (null == sInstance) {
			sInstance = new DeskResourcesConfiguration(context);
		}
		return sInstance;
	}

	public static synchronized void destroyInstance() {
		if (null != sInstance) {
			sInstance.selfDestruct();
			sInstance = null;
		}
	}

	protected DeskResourcesConfiguration(Context context) {
		mContext = context;
		selfConstruct();
		PreferencesManager preferences = new PreferencesManager(context,
				IPreferencesIds.DESK_SHAREPREFERENCES_FILE,
				Context.MODE_PRIVATE);
		String currentlanguage = preferences.getString(
				IPreferencesIds.CURRENTSELETELANGUAGE, "");
		String sysLanguage = Locale.getDefault().toString();
		if (currentlanguage != null && currentlanguage.equals("")) {
			String language = sysLanguage.substring(0, 2);
			if (AppUtils.isAppExist(context,
					LauncherEnv.Plugin.LANGUAGE_PACKAGE + "." + language)) {
				currentlanguage = language;
			} else {
				String[] codesLanguages = context.getResources()
						.getStringArray(R.array.ingolaucher_codes_language);
				for (int i = 0; i < codesLanguages.length; i++) {
					if (sysLanguage.contains(codesLanguages[i])) {
						currentlanguage = sysLanguage;
					}
				}
			}
		}
		boolean isLanguageAPK = false;
		String[] supportLanguages = context.getResources().getStringArray(
				R.array.support_language);
		for (int i = 0; i < supportLanguages.length; i++) {
			if (supportLanguages[i].equals(currentlanguage)) {
				isLanguageAPK = true;
				break;
			}
		}
		try {
			if (!"".equals(currentlanguage)) {
//				WindowManager windowManager = (WindowManager) GOLauncherApp
//						.getContext().getApplicationContext()
//						.getSystemService(Context.WINDOW_SERVICE);
//				DisplayMetrics dm = new DisplayMetrics();
//				windowManager.getDefaultDisplay().getMetrics(dm);
				if (isLanguageAPK) {
					String languagePackage = LauncherEnv.Plugin.LANGUAGE_PACKAGE
							+ "." + currentlanguage;
					Context languageContext = context.createPackageContext(
							languagePackage, Context.CONTEXT_IGNORE_SECURITY);
					Resources languageResources = languageContext
							.getResources();
					mDeskResources = createDeskResources();
					mDeskResources.setLanguage(languagePackage,
							languageResources);
					mLocale = new Locale(currentlanguage);
					Configuration config = mDeskResources.getConfiguration();
					DisplayMetrics dm2 = mDeskResources.getDisplayMetrics();
//					if (!isEqual(dm2.density, dm.density)) {
//						dm2 = dm;
//					}
					config.locale = mLocale;
					mDeskResources.updateConfiguration(config, dm2);
				} else {
					mDeskResources = createDeskResources(); // 获得res资源对象
					Configuration config = mDeskResources.getConfiguration(); // 获得设置对象
					DisplayMetrics dm2 = mDeskResources.getDisplayMetrics(); // 获得屏幕参数：主要是分辨率，像素等。
					if (currentlanguage.length() == 5) {
						String language = currentlanguage.substring(0, 2);
						String country = currentlanguage.substring(3, 5);
						config.locale = new Locale(language, country);
					} else {
						config.locale = new Locale(currentlanguage);
					}
//					if (!isEqual(dm2.density, dm.density)) {
//						dm2 = dm;
//					}
					mDeskResources.updateConfiguration(config, dm2);
					mLocale = config.locale;
				}
			} else {
				// 如果当前语言末进行设置时。则取系统的local
				mLocale = Locale.getDefault();
			}
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean isEqual(float num1, float num2) {
		final double exp = 10E-5;
		if (Math.abs(num1 - num2) > -exp
				&& Math.abs(num1 - num2) < exp) {
			return true;
		}
		return false;
	}
	
	private int getSupportLanguageVersionCode(Context context, String language) {
		String[] supportLanguages = context.getResources().getStringArray(R.array.support_language);
		String[] supportLanguageVersionCodes = context.getResources().getStringArray(
				R.array.support_language_versioncode);
		if (supportLanguages.length != supportLanguageVersionCodes.length) {
			return 0;
		}
		for (int i = 0; i < supportLanguages.length; i++) {
			if (supportLanguages[i].equals(language)) {
				return Integer.valueOf(supportLanguageVersionCodes[i]).intValue();
			}
		}
		return 0;
	}

	@Override
	public void selfConstruct() {

	};

	@Override
	public void selfDestruct() {
		if (null != mDeskResources) {
			mDeskResources.selfDestruct();
			mDeskResources = null;
		}
	}

	public DeskResources getDeskResources() {
		return mDeskResources;
	}

	public int getErrorCode() {
		return mErrorCode;
	}

	// @Override
	// public void onBCChange(int msgId, int param, Object object, List objects)
	// {
	// // 语言发生变化会重启，这里就不用做处理了
	// }

	// 以下接口支持独立语言包

	// mKey mDependencyKey mDefaultValue 暂时不考虑
	public void configurationPreference(Preference preference, AttributeSet attrs) {
		if (null != mDeskResources) {
			int titleResId = attrs.getAttributeResourceValue(ANDROID_NAMESPACE, "title", 0);
			int summaryResId = attrs.getAttributeResourceValue(ANDROID_NAMESPACE, "summary", 0);
			if (titleResId > 0) {
				preference.setTitle(titleResId);
			}
			if (summaryResId > 0) {
				preference.setSummary(summaryResId);
			}
		}
	}
	
	/**
	 * <br>功能简述:新设置组件 DeskSettingItemBaseView支持独立语言包，语言更换
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param baseView
	 * @param attrs
	 */
	public void configurationDeskSettingItemBaseView(DeskSettingItemBaseView baseView, AttributeSet attrs) {
		if (null != mDeskResources) {
			int titleResId = attrs.getAttributeResourceValue(GOLAUNCHER_NAMESPACE, "titleText", 0);
			int summaryResId = attrs.getAttributeResourceValue(GOLAUNCHER_NAMESPACE, "summaryText", 0);
			if (titleResId > 0) {
				baseView.setTitleText(titleResId);
			}
			if (summaryResId > 0) {
				baseView.setSummaryText(summaryResId);
			}
		}
	}

	public void configurationPreference(PreferenceCategory preference, AttributeSet attrs) {
		configurationPreference((Preference) preference, attrs);
	}

	public void configurationPreference(PreferenceScreen preference, AttributeSet attrs) {
		configurationPreference((Preference) preference, attrs);
	}

	public void configurationPreference(CheckBoxPreference preference, AttributeSet attrs) {
		if (null != mDeskResources) {
			configurationPreference((Preference) preference, attrs);
			int onResId = attrs.getAttributeResourceValue(ANDROID_NAMESPACE, "summaryOn", 0);
			int offResId = attrs.getAttributeResourceValue(ANDROID_NAMESPACE, "summaryOff", 0);
			if (onResId > 0) {
				preference.setSummaryOn(onResId);
			}
			if (offResId > 0) {
				preference.setSummaryOff(offResId);
			}
		}
	}

	public void configurationPreference(DialogPreference preference, AttributeSet attrs) {
		if (null != mDeskResources) {
			configurationPreference((Preference) preference, attrs);
			int titleResId = attrs.getAttributeResourceValue(ANDROID_NAMESPACE, "dialogTitle", 0);
			int messageResId = attrs.getAttributeResourceValue(ANDROID_NAMESPACE, "dialogMessage",
					0);
			int positiveResId = attrs.getAttributeResourceValue(ANDROID_NAMESPACE,
					"positiveButtonText", 0);
			int negativeResId = attrs.getAttributeResourceValue(ANDROID_NAMESPACE,
					"negativeButtonText", 0);
			if (titleResId > 0) {
				preference.setDialogTitle(titleResId);
			} else {
				preference.setDialogTitle(preference.getTitle());
			}
			if (messageResId > 0) {
				preference.setDialogMessage(messageResId);
			}
			if (positiveResId > 0) {
				preference.setPositiveButtonText(positiveResId);
			}
			if (negativeResId > 0) {
				preference.setNegativeButtonText(negativeResId);
			}
		}
	}

	public void configurationPreference(ListPreference preference, AttributeSet attrs) {
		if (null != mDeskResources) {
			configurationPreference((DialogPreference) preference, attrs);
			int entriesResId = attrs.getAttributeResourceValue(ANDROID_NAMESPACE, "entries", 0);
			int valuesResId = attrs.getAttributeResourceValue(ANDROID_NAMESPACE, "entryValues", 0);
			if (entriesResId > 0) {
				preference.setEntries(entriesResId);
			}
			if (valuesResId > 0) {
				preference.setEntryValues(valuesResId);
			}
		}
	}

	public void configurationPreference(TextView view, AttributeSet attrs) {
		if (null != mDeskResources) {
			int textResId = attrs.getAttributeResourceValue(ANDROID_NAMESPACE, "text", 0);
			if (textResId > 0) {
				view.setText(textResId);
			}
			int hintResId = attrs.getAttributeResourceValue(ANDROID_NAMESPACE, "hint", 0);
			if (hintResId > 0) {
				view.setHint(hintResId);
			}
		}
	}

	public Locale getmLocale() {
		return mLocale;
	}
	
	protected DeskResources createDeskResources() {
		return new DeskResources(mContext.getResources());
	}
}
