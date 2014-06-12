package com.jiubang.ggheart.apps.appmanagement.bean;

import android.content.Intent;

public class NoPromptUpdateInfo {

	private Intent mIntent;
	private int mNoUpdate; // 是否忽略更新

	public Intent getIntent() {
		return mIntent;
	}

	public void setIntent(Intent intent) {
		this.mIntent = intent;
	}

	public int getmNoUpdate() {
		return mNoUpdate;
	}

	public void setmNoUpdate(int mNoUpdate) {
		this.mNoUpdate = mNoUpdate;
	}
}
