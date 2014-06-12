package com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle;

import android.content.Context;
import android.view.ViewGroup;

/**
 * dock行排版
 * 
 * @author ruxueqin
 * 
 */
public abstract class AbsLineLayout extends ViewGroup {
	protected int mLineID = -1; // 对应于数据库第几行

	public AbsLineLayout(Context context) {
		super(context);
	}

	public void setLineID(int id) {
		mLineID = id;
	}

	public int getLineID(int id) {
		return mLineID;
	}

	public void updateLayout() {

	}
}
