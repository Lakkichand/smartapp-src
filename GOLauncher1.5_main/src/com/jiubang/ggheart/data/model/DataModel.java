package com.jiubang.ggheart.data.model;

import java.util.Locale;

import android.content.Context;

import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.data.DataProvider;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 数据持久化操作 使用者:Controler
 * 
 * @author guodanyang
 * 
 */
public class DataModel {

	// 上下文
	protected Context mContext;

	// 数据持久化执行者
	protected DataProvider mDataProvider = null;

	public DataModel(Context context) {
		mContext = context;
		mDataProvider = DataProvider.getInstance(context);
	}

	/**
	 * 
	 * @return
	 */
	public boolean isNewDB() {
		return mDataProvider.isNewDB();
	}

	/**
	 * 检测当前语言，以便判断是否修改应用程序title TODO:与DiyScheduler的checkLanguage()统一
	 */
	public boolean checkLanguage() {
		PreferencesManager sp = new PreferencesManager(mContext,"DIY", Context.MODE_PRIVATE);
		Locale locale = Locale.getDefault();
		String language = String.format(LauncherEnv.LANGUAGE_FORMAT, locale.getLanguage(),
				locale.getCountry());

		String lCode = sp.getString(LauncherEnv.LANGUAGE, null);
		if (!language.equals(lCode)) {
			return true;
		} else {
			return false;
		}
	}
}
