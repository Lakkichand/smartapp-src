package com.jiubang.ggheart.components.gohandbook;

import android.content.Context;
import android.content.SharedPreferences;

import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;

/**
 * 
 * <br>类描述:保存共享的数据工具类
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-9-5]
 */
public class SharedPreferencesUtil {
	private SharedPreferences mSharedPreferences;

	public SharedPreferencesUtil(Context context) {
		mSharedPreferences = context.getSharedPreferences(
				IPreferencesIds.DESK_SHAREPREFERENCES_FILE, Context.MODE_PRIVATE);
	}

	// 保存字符串数据
	public void saveString(String key, String value) {
		if (value == null) {
			mSharedPreferences.edit().putString(key, "").commit();
		} else {
			mSharedPreferences.edit().putString(key, value).commit();
		}

	}

	// 获取字符串数据
	public String getString(String key, String... defValue) {
		if (defValue.length > 0) {
			return mSharedPreferences.getString(key, defValue[0]);
		} else {
			return mSharedPreferences.getString(key, "");
		}
	}

	// 保存整型数据
	public void saveInt(String key, int value) {
		mSharedPreferences.edit().putInt(key, value).commit();
	}

	// 获取整型数据
	public int getInt(String key, int defValue) {
		return mSharedPreferences.getInt(key, defValue);

	}

	// 保存布尔值数据
	public void saveBoolean(String key, Boolean value) {
		mSharedPreferences.edit().putBoolean(key, value).commit();
	}

	// 获取布尔值数据
	public Boolean getBoolean(String key, Boolean... defValue) {
		if (defValue.length > 0) {
			return mSharedPreferences.getBoolean(key, defValue[0]);
		} else {
			return mSharedPreferences.getBoolean(key, false);
		}
	}
}
