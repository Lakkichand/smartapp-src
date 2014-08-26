package com.zhidian.wifibox.util;

import java.lang.reflect.Field;

/**
 * 查询手机SDK版本
 * @author zhaoyl
 *
 */
public class CheckSDKVersion {
	
	public static int check(){
		
		int version = 0;
		Class<android.os.Build.VERSION> build_version_class = android.os.Build.VERSION.class;
		try {
			Field field = build_version_class.getField("SDK_INT");
			version = (Integer) field.get(new android.os.Build.VERSION());
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return version;
		
	}

}
