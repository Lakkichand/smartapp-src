package com.jiubang.ggheart.data.info;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;

import com.go.util.ConvertUtils;
import com.jiubang.ggheart.data.tables.ShortcutTable;

public class GestureInfo implements IDatabaseObject {
	public final static int UPGESTURE = 0; // 向上手势
	public final static int DOWNGESTURE = 1; // 向下手势
	public final static int LEFTGESTURE = 2; // 向左手势
	public final static int RIGHTGESTURE = 3; // 向右手势
	public final static int MIDDLEGESTURE = 4; // 点击本身

	// 上下手势动作
	public Intent mUpIntent; // 向上手势动作
	public Intent mDownIntent; // 向下手势动作
	public String mUpAction; // 向上手势动作
	public String mDownAction; // 向下手势动作
	public int mIsUpInnerAction; // 向上手势内部响应
	public int mIsDownInnerAction; // 向下手势内部响应

	@Override
	public void writeObject(ContentValues values, String table) {
		if (table.equals(ShortcutTable.TABLENAME)) {
			String upintent = (mUpIntent == null) ? null : mUpIntent.toUri(0);
			String downintent = (mDownIntent == null) ? null : mDownIntent.toUri(0);

			values.put(ShortcutTable.UPINTENT, upintent);
			values.put(ShortcutTable.DOWNINTENT, downintent);
			values.put(ShortcutTable.UPACTION, mUpAction);
			values.put(ShortcutTable.DOWNACTION, mDownAction);
			values.put(ShortcutTable.UPINNERACTION, mIsUpInnerAction);
			values.put(ShortcutTable.DOWNINNERACTION, mIsDownInnerAction);
		}
	}

	@Override
	public void readObject(Cursor cursor, String table) {
		if (table.equals(ShortcutTable.TABLENAME)) {
			final int upIntentIndex = cursor.getColumnIndex(ShortcutTable.UPINTENT);
			final int downIntentIndex = cursor.getColumnIndex(ShortcutTable.DOWNINTENT);
			final int upActionIndex = cursor.getColumnIndex(ShortcutTable.UPACTION);
			final int downActionIndex = cursor.getColumnIndex(ShortcutTable.DOWNACTION);
			final int upInnerAction = cursor.getColumnIndex(ShortcutTable.UPINNERACTION);
			final int downInnerAction = cursor.getColumnIndex(ShortcutTable.DOWNINNERACTION);

			mUpIntent = ConvertUtils.stringToIntent(cursor.getString(upIntentIndex));
			mDownIntent = ConvertUtils.stringToIntent(cursor.getString(downIntentIndex));
			mUpAction = cursor.getString(upActionIndex);
			mDownAction = cursor.getString(downActionIndex);
			mIsUpInnerAction = cursor.getInt(upInnerAction);
			mIsDownInnerAction = cursor.getInt(downInnerAction);
		}
	}

	public void reset() {
		mUpIntent = null;
		mDownIntent = null;
		mUpAction = null;
		mDownAction = null;
		mIsUpInnerAction = 0;
		mIsDownInnerAction = 0;
	}

	public void copyGesture(GestureInfo info) {
		mUpIntent = info.mUpIntent;
		mDownIntent = info.mDownIntent;
		mUpAction = info.mUpAction;
		mDownAction = info.mDownAction;
		mIsUpInnerAction = info.mIsUpInnerAction;
		mIsDownInnerAction = info.mIsDownInnerAction;
	}
}
