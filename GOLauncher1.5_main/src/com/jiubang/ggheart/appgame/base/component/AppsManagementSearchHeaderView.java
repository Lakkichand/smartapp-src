/*
 * 文 件 名:  AppsManagementSearchHeaderView.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-7-12
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.appgame.base.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;

/**
 * 类描述: 功能详细描述:
 * 
 * @author liuxinyang
 * @date [2012-7-12]
 */
public class AppsManagementSearchHeaderView extends RelativeLayout {

	private TextView mTextView = null;

	public AppsManagementSearchHeaderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onFinishInflate() {
		// TODO Auto-generated method stub
		mTextView = (TextView) this.findViewById(R.id.apps_management_search_header_text);
		this.setClickable(false);
		super.onFinishInflate();
	}

	public void viewGone() {
		this.setVisibility(View.GONE);
		mTextView.setVisibility(View.GONE);
	}

	public void showView(String SearchText, int count) {
		this.setVisibility(View.VISIBLE);
		mTextView.setVisibility(View.VISIBLE);

		String result = null;
		String resultFormat = getContext().getString(R.string.themestore_search_return_result);
		result = String.format(resultFormat, SearchText, count);
		mTextView.setText(result);
	}
}
