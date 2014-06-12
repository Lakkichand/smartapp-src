package com.jiubang.ggheart.apps.desks.Preferences.view;

import android.content.Context;
import android.widget.LinearLayout;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingQaTutorialActivity;

/**
 * 
 * <br>类描述:桌面设置-关于-QA-Go Widget
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-10-15]
 */
public class DeskSettingQaGoWidgetView extends LinearLayout {
	private String[] mPageList;
	private final int[] mPageNames = { R.array.widget_pages_names_cn_ch,
			R.array.widget_pages_names_en_us, R.array.widget_pages_names_cn_hk,
			R.array.widget_pages_names_cn_hk };
	private DeskSettingQaWebView mWebView;

	public DeskSettingQaGoWidgetView(Context context) {
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
