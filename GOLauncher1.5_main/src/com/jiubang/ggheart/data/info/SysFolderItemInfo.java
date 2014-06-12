package com.jiubang.ggheart.data.info;

import android.content.Intent;
import android.net.Uri;

import com.go.util.ConvertUtils;

public class SysFolderItemInfo extends AppItemInfo {
	// 应用的引用计数
	public int mRefCount;

	// public Uri mUri;
	// public int mDisplayMode;

	public SysFolderItemInfo() {

	}

	// public boolean isEqual(SysFolderItemInfo info)
	// {
	// return false;
	// }

	// 1. intent 相同
	// 2. uri 相同
	// 3. 但是不能全空
	public boolean isEqual(Intent intent, Uri uri) {
		boolean bIntentCompare = true;
		if (null == mIntent && null != intent) {
			return false;
		} else if (null != mIntent && null == intent) {
			return false;
		} else if (null != mIntent && null != intent) {
			if (!ConvertUtils.shortcutIntentCompare(mIntent, intent)) {
				return false;
			}
		} else {
			bIntentCompare = false;
		}

		if (null == mUri && null != uri) {
			return false;
		} else if (null != mUri && null == uri) {
			return false;
		} else if (null != mUri && null != uri) {
			String srcStr = ConvertUtils.uriToString(mUri);
			String desStr = ConvertUtils.uriToString(uri);
			if (!srcStr.equals(desStr)) {
				return false;
			}
		} else {
			if (!bIntentCompare) {
				return false;
			}
		}

		return true;
	}
}
