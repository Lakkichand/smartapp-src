package com.jiubang.ggheart.apps.font;

import java.io.File;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.util.Log;

public class FontTypeface {
	public static final String TAG = "FontTypeface";

	public static final String DEFAULT = "DEFAULT";
	public static final String DEFAULT_BOLD = "DEFAULT_BOLD";
	public static final String SANS_SERIF = "SANS_SERIF";
	public static final String SERIF = "SERIF";
	public static final String MONOSPACE = "MONOSPACE";

	/*
	 * 系统默认的 Typeface
	 */
	public static Typeface typeface(String typeface) {
		Typeface type = null;

		if (typeface.equals(DEFAULT_BOLD)) {
			type = Typeface.DEFAULT_BOLD;
		} else if (typeface.equals(SANS_SERIF)) {
			type = Typeface.DEFAULT_BOLD;
		} else if (typeface.equals(SERIF)) {
			type = Typeface.DEFAULT_BOLD;
		} else if (typeface.equals(MONOSPACE)) {
			type = Typeface.DEFAULT_BOLD;
		} else {
			type = Typeface.DEFAULT;
		}

		return type;
	}

	/*
	 * SD卡的字体文件
	 */
	public static Typeface typeface(File fontFile) {
		if (null == fontFile) {
			Log.i(TAG, "typeface funcion param font file is null");
			return Typeface.DEFAULT;
		}
		if (!fontFile.exists()) {
			Log.i(TAG, "typeface funcion param font file is not exsit");
			return Typeface.DEFAULT;
		}

		Typeface typeface = null;
		try {
			typeface = Typeface.createFromFile(fontFile);
		} catch (Exception e) {
			Log.i(TAG, "create type face from file exception");
			typeface = Typeface.DEFAULT;
		}
		return typeface;
	}

	/**
	 * 安装包下的字体文件 目前：1. 只扫描Asset文件夹，该文件有大小限制 (1M) 2. 文件名,看扫描出来的文件名是否需要处理
	 */
	public static Typeface typeface(Context context, String packageName, String fontFile) {
		// 参数
		if (null == context) {
			Log.i(TAG, "typeface funcion param context is null");
			return Typeface.DEFAULT;
		}
		if (null == fontFile) {
			Log.i(TAG, "typeface funcion param font file is null");
			return Typeface.DEFAULT;
		}

		boolean bCurRes = false;
		Resources res = null;
		if (null == packageName || packageName.equals(context.getPackageName())) {
			bCurRes = true;
			res = context.getResources();
		} else {
			try {
				Context ct = context.createPackageContext(packageName,
						Context.CONTEXT_IGNORE_SECURITY);
				res = ct.getResources();
				ct = null;
			} catch (Exception e) {
				Log.i(TAG, "create package context exception");
				return Typeface.DEFAULT;
			}
		}
		AssetManager am = null == res ? null : res.getAssets();
		if (null == am) {
			Log.i(TAG, "get resourse assert exception");
			return Typeface.DEFAULT;
		}

		Typeface typeface = null;
		try {
			typeface = Typeface.createFromAsset(am, fontFile);
		} catch (Exception e) {
			Log.i(TAG, "create type face from assert file exception");
			typeface = Typeface.DEFAULT;
		}
		if (!bCurRes) {
			// am.close();
		}
		return typeface;
	}
}
