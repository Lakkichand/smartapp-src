package com.jiubang.ggheart.data.info;

import android.content.Intent;

public class AppConfigInfo {
	private Intent mIntent;
	private boolean mHide; // 是否隐藏

	public Intent getIntent() {
		return mIntent;
	}

	public void setIntent(Intent intent) {
		this.mIntent = intent;
	}

	public boolean getHide() {
		return mHide;
	}

	public void setHide(boolean hide) {
		this.mHide = hide;
	}
}
