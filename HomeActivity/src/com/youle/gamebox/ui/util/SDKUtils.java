package com.youle.gamebox.ui.util;

import android.os.Environment;

import java.io.File;

public class SDKUtils {


	public static String getSKCardPath() {
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
			return sdDir.getAbsolutePath();
		} else {
			return null;
		}
	}

//
//	public static void saveSession(Context context,String sessionId) {
//		ConfigModel configModel = new ConfigDataProvider(context)
//				.getConfig();
//		if (configModel != null) {
//			try {
//				URL sdkUrl= new URL(configModel.getSdkServer());
//				CookiesUtil.getInstance(context).setCookies(
//						configModel.getSdkServer(), sessionId, sdkUrl.getHost());
//				URL securityUrl= new URL(configModel.getSecurityServer());
//				CookiesUtil.getInstance(context).setCookies(
//						configModel.getSecurityServer(), sessionId, securityUrl.getHost());
//				URL comunityUrl= new URL(configModel.getHomEServer());
//				CookiesUtil.getInstance(context).setCookies(
//						configModel.getForumServer(), sessionId, comunityUrl.getHost());
//			} catch (MalformedURLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//	}
}
