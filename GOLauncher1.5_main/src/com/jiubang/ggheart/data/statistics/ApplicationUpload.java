package com.jiubang.ggheart.data.statistics;

import java.util.ArrayList;
import java.util.Locale;

import android.content.Context;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.data.info.AppItemInfo;

/**
 * 
 * 语言||版本\r\n软件1\r\n软件2 通过加密上传
 * 
 */
public class ApplicationUpload {
	private static final String SEPARATE = "||";
	private static final String LINE_BREAK = "\r\n";

	public static boolean mNeedUpload = false;

	public static String uploadContent(Context context, ArrayList<AppItemInfo> applications) {
		return language() + SEPARATE + version(context) + LINE_BREAK
				+ applicationPackages(applications);
	}

	private static String language() {

		String ret = null;
		try {
			ret = String.format("%s-%s", Locale.getDefault().getLanguage(), Locale.getDefault()
					.getCountry());
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
		return null == ret ? "error" : ret;
	}

	private static String version(Context context) {
		String ret = null;
		if (null != context) {
			ret = context.getString(R.string.curVersion);
		}
		return null == ret ? "error" : ret;
	}

	private static String applicationPackages(ArrayList<AppItemInfo> applications) {
		String ret = null;
		if (null != applications) {
			int sz = applications.size();
			for (int i = 0; i < sz; i++) {
				AppItemInfo app = applications.get(i);
				// 过滤条件
				if (null == app) {
					continue;
				}
				if (null == app.mIntent) {
					continue;
				}
				if (null == app.mIntent.getComponent()) {
					continue;
				}
				String packageStr = app.mIntent.getComponent().getPackageName();
				if (null == packageStr) {
					continue;
				}

				if (null == ret) {
					ret = packageStr;
				} else {
					ret += LINE_BREAK;
					ret += packageStr;
				}
			}
		}
		return ret == null ? "error" : ret;
	}
}
