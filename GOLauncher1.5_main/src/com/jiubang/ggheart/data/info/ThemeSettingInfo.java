package com.jiubang.ggheart.data.info;

import android.content.ContentValues;
import android.database.Cursor;

import com.go.util.ConvertUtils;
import com.jiubang.ggheart.data.statistics.StaticScreenSettingInfo;
import com.jiubang.ggheart.data.tables.ThemeTable;
/**
 * 
 * <br>类描述:主题设置
 * <br>功能详细描述:
 * 
 * @author  
 * @date  [2012-9-18]
 */
public class ThemeSettingInfo {
	public String mThemeName;
	public int mVersion;
	public boolean mAutoCheckVersion;
	public String mBackgroundImage;
	public boolean mIsPemanentMemory;
	public boolean mIsCacheDesk;
	public String mFontStr;
	public long mLastShowTime;
	public boolean mPreventForceClose;
	public boolean mHighQualityDrawing;
	public boolean mTransparentStatusbar;
	public boolean mFirstRun;
	public boolean mTipCancelDefaultDesk;

	public ThemeSettingInfo() {
		mFontStr = "heiti";
		mAutoCheckVersion = true;
		mIsCacheDesk = false;
		mLastShowTime = 0L;
		mPreventForceClose = true;

		mIsPemanentMemory = StaticScreenSettingInfo.sIsPemanentMemory;
		// 2.3 默认开启高质量绘图
		mHighQualityDrawing = StaticScreenSettingInfo.sHighQualityDrawing;
		// 默认关闭高质量绘图，所以注释掉下面的语句，mHighQualityDrawing默认为false;
		// if (Build.VERSION.SDK_INT >= 9)
		// {
		// mHighQualityDrawing = true;
		// }
		// 默认为不开启，因为在某些机型会引起壁纸被放大（缩小）
		mTransparentStatusbar = false;
		mFirstRun = false;
		mTipCancelDefaultDesk = true;
	}

	/**
	 * 加入键值对
	 * 
	 * @param values
	 *            键值对
	 */
	public void contentValues(ContentValues values) {
		if (null == values) {
			return;
		}
		values.put(ThemeTable.THEMENAME, mThemeName);
		values.put(ThemeTable.VERSION, mVersion);
		values.put(ThemeTable.AUTOCHECKVERSION, ConvertUtils.boolean2int(mAutoCheckVersion));
		values.put(ThemeTable.BACKGROUNDIMAGE, mBackgroundImage);
		values.put(ThemeTable.ISPEMANENTMEMORY, ConvertUtils.boolean2int(mIsPemanentMemory));
		values.put(ThemeTable.ISCACHEDESK, ConvertUtils.boolean2int(mIsCacheDesk));
		values.put(ThemeTable.FONT, mFontStr);
		values.put(ThemeTable.LASTSHOWTIME, mLastShowTime);
		values.put(ThemeTable.PREVENTFORCECLOSE, ConvertUtils.boolean2int(mPreventForceClose));
		values.put(ThemeTable.HIGHQUALITYDRAWING, ConvertUtils.boolean2int(mHighQualityDrawing));
		values.put(ThemeTable.TRANSPARENTSTATUSBAR, ConvertUtils.boolean2int(mTransparentStatusbar));
		values.put(ThemeTable.FIRSTRUN, ConvertUtils.boolean2int(mFirstRun));
		values.put(ThemeTable.TIPCANCELDEFAULTDESK, ConvertUtils.boolean2int(mTipCancelDefaultDesk));
		values.put(ThemeTable.CLOUD_SECURITY, ConvertUtils.boolean2int(true));
	}

	/**
	 * 解析数据
	 * 
	 * @param cursor
	 *            数据集
	 */
	public boolean parseFromCursor(Cursor cursor) {
		if (null == cursor) {
			return false;
		}

		boolean bData = cursor.moveToFirst();
		if (bData) {
			int themenameIndex = cursor.getColumnIndex(ThemeTable.THEMENAME);
			int versionIndex = cursor.getColumnIndex(ThemeTable.VERSION);
			int autocheckversionIndex = cursor.getColumnIndex(ThemeTable.AUTOCHECKVERSION);
			int backgroundpicIndex = cursor.getColumnIndex(ThemeTable.BACKGROUNDIMAGE);
			int pemanentmemoryIndex = cursor.getColumnIndex(ThemeTable.ISPEMANENTMEMORY);
			int cachedeskIndex = cursor.getColumnIndex(ThemeTable.ISCACHEDESK);
			int fontIndex = cursor.getColumnIndex(ThemeTable.FONT);
			int lasttimeIndex = cursor.getColumnIndex(ThemeTable.LASTSHOWTIME);
			int preventforcecloseIndex = cursor.getColumnIndex(ThemeTable.PREVENTFORCECLOSE);
			int highQualityDrawingIndex = cursor.getColumnIndex(ThemeTable.HIGHQUALITYDRAWING);
			int transparentStatusbarIndex = cursor.getColumnIndex(ThemeTable.TRANSPARENTSTATUSBAR);
			int firstrunIndex = cursor.getColumnIndex(ThemeTable.FIRSTRUN);
			int tipCancelDefaultDeskIndex = cursor.getColumnIndex(ThemeTable.TIPCANCELDEFAULTDESK);

			if (themenameIndex >= 0) {
				mThemeName = cursor.getString(themenameIndex);
			}

			if (versionIndex >= 0) {
				mVersion = cursor.getInt(versionIndex);
			}

			if (autocheckversionIndex >= 0) {
				mAutoCheckVersion = ConvertUtils.int2boolean(cursor.getInt(autocheckversionIndex));
			}

			if (backgroundpicIndex >= 0) {
				mBackgroundImage = cursor.getString(backgroundpicIndex);
			}

			if (pemanentmemoryIndex >= 0) {
				mIsPemanentMemory = ConvertUtils.int2boolean(cursor.getInt(pemanentmemoryIndex));
			}

			if (cachedeskIndex >= 0) {
				mIsCacheDesk = ConvertUtils.int2boolean(cursor.getInt(cachedeskIndex));
			}

			if (fontIndex >= 0) {
				mFontStr = cursor.getString(fontIndex);
			}

			if (lasttimeIndex >= 0) {
				mLastShowTime = cursor.getLong(lasttimeIndex);
			}

			if (preventforcecloseIndex >= 0) {
				mPreventForceClose = ConvertUtils
						.int2boolean(cursor.getInt(preventforcecloseIndex));
			}

			if (highQualityDrawingIndex >= 0) {
				mHighQualityDrawing = ConvertUtils.int2boolean(cursor
						.getInt(highQualityDrawingIndex));
			}

			if (transparentStatusbarIndex >= 0) {
				mTransparentStatusbar = ConvertUtils.int2boolean(cursor
						.getInt(transparentStatusbarIndex));
			}

			if (firstrunIndex >= 0) {
				mFirstRun = ConvertUtils.int2boolean(cursor.getInt(firstrunIndex));
			}

			if (tipCancelDefaultDeskIndex >= 0) {
				mTipCancelDefaultDesk = ConvertUtils.int2boolean(cursor
						.getInt(tipCancelDefaultDeskIndex));
			}
		}

		return bData;
	}
}
