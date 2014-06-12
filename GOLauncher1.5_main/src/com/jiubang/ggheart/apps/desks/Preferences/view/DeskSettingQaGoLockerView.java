package com.jiubang.ggheart.apps.desks.Preferences.view;

import android.content.Context;
import android.widget.LinearLayout;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingQaTutorialActivity;

/**
 * 
 * <br>类描述:桌面设置-关于-QA-Go Locker
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-10-15]
 */
public class DeskSettingQaGoLockerView extends LinearLayout {
	private String[] mPageList;
	private final int[] mPageNames = { R.array.locker_pages_names_cn_ch,
			R.array.locker_pages_names_en_us, R.array.locker_pages_names_cn_hk,
			R.array.locker_pages_names_cn_hk };
	private DeskSettingQaWebView mWebView;

	public DeskSettingQaGoLockerView(Context context) {
		super(context);
		mPageList = DeskSettingQaTutorialActivity.getPageList(mPageNames, 1, context);
		if (mPageList == null || mPageList.length <= 0) {
			return;
		}
		String url = DeskSettingQaTutorialActivity.URL_BASE + mPageList[0];
		mWebView = new DeskSettingQaWebView(getContext());
		mWebView.loadUrl(url);
		addView(mWebView);
	}

	/**
	 * <br>功能简述:退出时回收资源
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void recycle() {
		if (mWebView != null) {
			mWebView.onDestroy();
		}
	}
}
