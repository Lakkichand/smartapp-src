package com.zhidian.wifibox.util;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * 系统应用冻结、解冻工具类
 * 
 * @author zhaoyl
 * 
 */
public class SystemAppUtil {

	public static final String DISABLE_APP = "pm disable-user ";// 冻结应用
	public static final String DISABLE_LOW_APP = "pm disable ";// 冻结应用，4.0以下用
	public static final String ENABLE_APP = "pm enable ";// 解冻应用
	
	public static String getDeVersion(){
		int version = CheckSDKVersion.check();
		if (version >= 14) {
			return DISABLE_APP;
		}else {
			return DISABLE_LOW_APP;
		}
		
		
	}

	/***********************
	 * 冻结、解冻应用
	 **********************/
	public static void FreezeApp(String packName, String command) {

		try {
			Process su = Runtime.getRuntime().exec("su");
			DataOutputStream outputStream = new DataOutputStream(
					su.getOutputStream());

			outputStream.writeBytes(command + packName + "\n");
			outputStream.flush();

			outputStream.writeBytes("exit\n");
			outputStream.flush();
			su.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}
	
	/***********************
	 * 彻底卸载系统应用
	 **********************/
	public static boolean execCommand(String apk) {
		
		try {
			Process su = Runtime.getRuntime().exec("su");
			DataOutputStream outputStream = new DataOutputStream(
					su.getOutputStream());

			outputStream.writeBytes("mount -o remount rw system " + "\n");
			outputStream.flush();
			
			outputStream.writeBytes("chmod 0777 /system/app " + "\n");
			outputStream.flush();
			
			outputStream.writeBytes("rm system/" + apk + "\n");
			outputStream.flush();

			outputStream.writeBytes("exit\n");
			outputStream.flush();
			su.waitFor();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (InterruptedException e) {			
			e.printStackTrace();
			return false;
		}
		
	
//		  Process process = null;
//		  try {
//		   process = Runtime.getRuntime().exec("system/bin/pm uninstall " + packname);
//		   process.waitFor();
//		  } catch (Exception e) {
//		   return false;
//		  } finally {
//		   try {
//		    process.destroy();
//		   } catch (Exception e) {
//		   }
//		  }
		  
		 }

}
