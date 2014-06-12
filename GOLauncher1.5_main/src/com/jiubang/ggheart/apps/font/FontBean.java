package com.jiubang.ggheart.apps.font;

import java.io.File;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;

import com.jiubang.ggheart.data.tables.FontTable;

public class FontBean {
	/**
	 * 系统字体
	 */
	public static final int FONTFILETYPE_SYSTEM = 0;

	/*
	 * 字体文件 来源 第三方包 应用间访问
	 */
	public static final int FONTFILETYPE_PACKAGE = 1;

	/*
	 * 字体文件 来源 SD卡文件 可以直接访问
	 */
	public static final int FONTFILETYPE_FILE = 2;

	/**
	 * FONTFILETYPE_SYSTEM FONTFILETYPE_PACKAGE FONTFILETYPE_FILE
	 */
	public int mFontFileType;

	/**
	 * FONTFILETYPE_SYSTEM : SYSTEM FONTFILETYPE_PACKAGE ：包名 FONTFILETYPE_FILE
	 * ：SDCARD
	 */
	public static final String SYSTEM = "system";
	public static final String SDCARD = "sdcard";
	public String mPackageName;
	/**
	 * 辅助字段，用来显示用
	 */
	public String mApplicationName;

	/**
	 * 字体文件名
	 */
	public String mFileName;

	/**
	 * 字体风格
	 */
	public String mStyle;

	// 真是数据体
	public Typeface mFontTypeface;
	public int mFontStyle;

	public FontBean() {
		mFontFileType = FONTFILETYPE_SYSTEM;
		mPackageName = SYSTEM;
		mApplicationName = SYSTEM;
		mFileName = FontTypeface.DEFAULT;
		mStyle = FontStyle.NORMAL;
	}

	/**
	 * {@inheritDoc} 重载一个Object类的方法，避免出现协变。
	 */
	@Override
	public boolean equals(Object object) {
		return super.equals(object);
	}

	/**
	 * {@inheritDoc} 重载一个Object类的方法，避免出现协变。
	 */
	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return super.hashCode();
	}

	public boolean equals(FontBean bean) {
		boolean bEqual = false;
		if (null == bean) {
			return bEqual;
		}

		if (mFontFileType != bean.mFontFileType) {
			return bEqual;
		}

		if (null != bean.mStyle) {
			bEqual = bean.mStyle.equals(mStyle);
			if (!bEqual) {
				return bEqual;
			}
		}

		// if (FONTFILETYPE_PACKAGE == mFontFileType)
		{
			if (null != bean.mPackageName && null != bean.mFileName) {
				bEqual = bean.mPackageName.equals(mPackageName) && bean.mFileName.equals(mFileName);
				if (!bEqual) {
					return bEqual;
				}
			}
		}

		// if (FONTFILETYPE_FILE == mFontFileType || FONTFILETYPE_SYSTEM ==
		// mFontFileType)
		// {
		// if (null != bean.mFileName)
		// {
		// bEqual = bean.mFileName.equals(mFileName);
		// if (!bEqual)
		// {
		// return bEqual;
		// }
		// }
		// }

		return true;
	}

	public void setValues(ContentValues values) {
		if (null == values) {
			return;
		}

		values.put(FontTable.FONTFILETYPE, mFontFileType);
		values.put(FontTable.FONTPACKAGE, mPackageName);
		values.put(FontTable.FONTTITLE, mApplicationName);
		values.put(FontTable.FONTFILE, mFileName);
		values.put(FontTable.FONTSTYLE, mStyle);
	}

	public void getValues(Cursor cursor) {
		if (null == cursor) {
			return;
		}

		int typeIndex = cursor.getColumnIndex(FontTable.FONTFILETYPE);
		int packageIndex = cursor.getColumnIndex(FontTable.FONTPACKAGE);
		int applicationIndex = cursor.getColumnIndex(FontTable.FONTTITLE);
		int fileIndex = cursor.getColumnIndex(FontTable.FONTFILE);
		int styleIndex = cursor.getColumnIndex(FontTable.FONTSTYLE);
		if (-1 == typeIndex || -1 == packageIndex || -1 == applicationIndex || -1 == fileIndex
				|| -1 == styleIndex) {
			return;
		}

		mFontFileType = cursor.getInt(typeIndex);
		mPackageName = cursor.getString(packageIndex);
		mApplicationName = cursor.getString(applicationIndex);
		mFileName = cursor.getString(fileIndex);
		mStyle = cursor.getString(styleIndex);
	}

	public boolean initTypeface(Context context) {
		if (null != mFontTypeface) {
			// 已经初始化
			return false;
		}

		if (FontBean.FONTFILETYPE_SYSTEM == mFontFileType) {
			mFontTypeface = FontTypeface.typeface(mFileName);
		} else if (FontBean.FONTFILETYPE_PACKAGE == mFontFileType) {
			mFontTypeface = FontTypeface.typeface(context, mPackageName, mFileName);
		} else if (FontBean.FONTFILETYPE_FILE == mFontFileType) {
			mFontTypeface = FontTypeface.typeface(new File(mFileName));
		}

		mFontStyle = FontStyle.style(mStyle);
		return true;
	}
}
