package com.jiubang.ggheart.data.info;

import android.content.Intent;

import com.go.util.ConvertUtils;

public class SysShortCutItemInfo extends AppItemInfo {
	// 应用的引用计数
	public int mRefCount;

	public SysShortCutItemInfo() {

	}

	// public boolean isEqual(SysFolderItemInfo info)
	// {
	// return false;
	// }

	// 自身 intent 不可能为空
	public boolean isEqual(Intent intent) {
		if (null == intent || null == mIntent) {
			return false;
		}

		if (ConvertUtils.shortcutIntentCompare(mIntent, intent)) {
			return true;
		}
		return false;
	}
}
