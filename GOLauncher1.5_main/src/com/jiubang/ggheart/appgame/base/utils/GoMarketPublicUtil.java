package com.jiubang.ggheart.appgame.base.utils;

import android.content.Context;

import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;

/**
 * 应用市场公共Util
 * @author zhouxuewen
 *
 */
public class GoMarketPublicUtil {

	private static GoMarketPublicUtil sInstance = null;
	
	private boolean mIsFirstTime = true;
	
	private boolean mIsNeedShowSS = false;
	
	private Context mContext = null;
	
//	private Activity mActivity = null;
//
//	public static final String AF_APP_KEY = "aQm9J4LqjI1TlzdZ";
//	public static final String AF_SECRET_KEY = "CEYlzHV2359L50f790c0";
	
	private GoMarketPublicUtil(Context context) {
		mContext = context.getApplicationContext();
	}
	
	public synchronized static GoMarketPublicUtil getInstance(Context context) {
		if (sInstance == null) {
			try {
				sInstance = new GoMarketPublicUtil(context);
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		return sInstance;
	}
	
	/**
	 * 用于记录进入应用市场后是不是第一次获取公共协议
	 * @return
	 */
	public boolean getIsFristTiem() {
		boolean firstTime = mIsFirstTime;
		mIsFirstTime = false;
		return firstTime;
	}
	
	public void setIsNeedShowSS(boolean show) {
		try {
			PreferencesManager preferences = new PreferencesManager(mContext,
					IPreferencesIds.GOMARKET_PUBLIC_UTIL, Context.MODE_WORLD_READABLE);
			preferences.putBoolean("showss", show);
			preferences.commit();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	public boolean getIsNeedShowSS() {
		mIsNeedShowSS = true;
		try {
			PreferencesManager preferences = new PreferencesManager(mContext,
					IPreferencesIds.GOMARKET_PUBLIC_UTIL, Context.MODE_WORLD_READABLE);
			mIsNeedShowSS = preferences.getBoolean("showss", true);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return mIsNeedShowSS;
	}
	
//	public void setActivity(Activity activity) {
//		mActivity = activity;
//	}
//	
//	public Activity getActivity() {
//		return mActivity;
//	}
//	
//	public void setAfToken(String token) {
//		try {
//			PreferencesManager preferences = new PreferencesManager(mContext,
//					IPreferencesIds.GOMARKET_PUBLIC_UTIL, Context.MODE_WORLD_READABLE);
//			preferences.putString("af_token", token);
//			preferences.commit();
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
//	}
//	
//	public String getAfToken() {
//		String token = "";
//		try {
//			PreferencesManager preferences = new PreferencesManager(mContext,
//					IPreferencesIds.GOMARKET_PUBLIC_UTIL, Context.MODE_WORLD_READABLE);
//			token = preferences.getString("af_token", "");
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
//		return token;
//	}
	
	public void destroy() {
//		mActivity = null;
		sInstance = null;
	}
}
