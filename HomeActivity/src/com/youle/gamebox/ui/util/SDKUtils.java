package com.youle.gamebox.ui.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
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
    public static String getChanalID(Context context) {
        ApplicationInfo info;
        try {
            info = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
            String appId = info.metaData.getString("chanal");
            return  appId ;
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }


}
