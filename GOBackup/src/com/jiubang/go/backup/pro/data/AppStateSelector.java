package com.jiubang.go.backup.pro.data;

import android.content.Context;

/**
 * @author maiyongshen
 *
 */
public abstract class AppStateSelector {

	protected int mFlag;

	public AppStateSelector() {
		mFlag = 0;
	}
	
	public AppStateSelector(AppStateSelector value) {
		this();
		if (value == null) {
			return;
		}
		mFlag = value.mFlag;
	}

	public AppStateSelector(int selectType) {
		mFlag = selectType;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof AppStateSelector)) {
			return false;
		}
		return this.mFlag == ((AppStateSelector) o).mFlag;
	}
	
	public void enableSelectApp(int selectedType) {
		mFlag = mFlag | selectedType;
	}

	public void disableSelectApp(int selectedType) {
		mFlag = mFlag & (~selectedType);
	}
	
	public abstract boolean match(Context context, AppInfo appInfo);
}
