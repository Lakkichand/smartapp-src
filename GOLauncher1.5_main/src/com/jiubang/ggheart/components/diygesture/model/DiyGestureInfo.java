package com.jiubang.ggheart.components.diygesture.model;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.gesture.Gesture;
import android.graphics.Rect;

import com.go.util.ConvertUtils;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.data.info.IDatabaseObject;

/***
 * 自定义手势数据项类
 */
public class DiyGestureInfo implements IDatabaseObject {

	private long mID = 0; // 手势主键，ID编号,gesturename由这个id值转化生成
	private String mName = null; // 手势名字
	private int mType; // 手势类型 {@link #DiyGestureConstants}
	private Intent mIntent = null; // 手势命令
	private Gesture mGesture = null; // 手势内容
	private String mTypeName = null; // 手势类型名称

	public DiyGestureInfo() {
	}

	public DiyGestureInfo(String name, int type, Intent intent, Gesture gesture) {
		this.mID = System.currentTimeMillis();
		mName = name;
		mType = type;
		mIntent = intent;
		mGesture = gesture;
	}

	public long getID() {
		return mID;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		this.mName = name;
	}

	public Intent getIntent() {
		return mIntent;
	}

	public void setIntent(Intent intent) {
		this.mIntent = intent;
	}

	public Gesture getmGesture() {
		return mGesture;
	}

	public void setGesture(Gesture gesture) {
		this.mGesture = gesture;
	}

	/**
	 * 识别出手势后，执行相应的跳转
	 */
	public void execute(Rect rect) {
		if (null != mIntent) {
			ArrayList<Object> posArrayList = null;
			if (null != rect) {
				posArrayList = new ArrayList<Object>();
				posArrayList.add(rect);
			}

			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IDiyMsgIds.START_ACTIVITY,
					-1, mIntent, posArrayList);
		}
	}

	@Override
	public void writeObject(ContentValues values, String table) {
		values.put(DiyGestureTable.ID, mID);
		values.put(DiyGestureTable.NAME, mName);
		values.put(DiyGestureTable.INTENT, mIntent.toUri(0));
		values.put(DiyGestureTable.TYPE, mType);
	}

	@Override
	public void readObject(Cursor cursor, String table) {
		final int id = cursor.getColumnIndex(DiyGestureTable.ID);
		final int name = cursor.getColumnIndex(DiyGestureTable.NAME);
		final int intent = cursor.getColumnIndex(DiyGestureTable.INTENT);
		final int type = cursor.getColumnIndex(DiyGestureTable.TYPE);

		mID = cursor.getLong(id);
		mName = cursor.getString(name);
		mIntent = ConvertUtils.stringToIntent(cursor.getString(intent));
		mType = cursor.getInt(type);
	}

	/**
	 * 获取手势存储文件夹名，即gesture的key
	 */
	public String getGestureFileName() {
		return String.valueOf(mID);
	}

	public int getType() {
		return mType;
	}

	public void setType(int mType) {
		this.mType = mType;
	}

	public String getTypeName() {
		return mTypeName;
	}

	public void setTypeName(String mTypeName) {
		this.mTypeName = mTypeName;
	}
}
