package com.jiubang.ggheart.data.info;

import android.content.Intent;

public class FunTaskAdditionalInfo {

	private Intent mIntent;
	private int mIsIgnore; // 是否忽略

	public Intent getIntent() {
		return mIntent;
	}

	public void setIntent(Intent intent) {
		this.mIntent = intent;
	}

	public int getIsIgnore() {
		return mIsIgnore;
	}

	public void setIsIgnore(int isIgnore) {
		this.mIsIgnore = isIgnore;
	}

}
