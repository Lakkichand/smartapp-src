package com.jiubang.ggheart.data.theme.adrecommend;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * 公共工具类
 * 
 * @author HuYong
 * @version 1.0
 */
public class HttpUtil {

	/**
	 * 根据vps定义规范，获取本机vps信息
	 * 
	 * @param context
	 * @param imei
	 * @return vps字符串
	 */
	public static String getVps(Context context, String imei) {
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager wMgr = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		wMgr.getDefaultDisplay().getMetrics(dm);
		// int width = (int) (dm.widthPixels * dm.density);
		// int height = (int) (dm.heightPixels * dm.density);
		int width = dm.widthPixels;
		int height = dm.heightPixels;

		StringBuilder vpsStringBuilder = new StringBuilder(64);
		vpsStringBuilder.append("1#");
		vpsStringBuilder.append("Android#");
		vpsStringBuilder.append(Build.MODEL + "#");
		vpsStringBuilder.append(imei + "#");
		vpsStringBuilder.append("166#");
		vpsStringBuilder.append(width + "_" + height + "#");
		vpsStringBuilder.append("01.01.00");
		String vps = vpsStringBuilder.toString();
		try {
			vps = URLEncoder.encode(vps, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return vps;
	}
}
