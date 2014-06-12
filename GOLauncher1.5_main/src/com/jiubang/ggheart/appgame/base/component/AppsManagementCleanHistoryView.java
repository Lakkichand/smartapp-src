/*
 * 文 件 名:  AppsManagementCleanHistoryView.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-12-26
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.appgame.base.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.gau.go.launcherex.R;

/**
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  liuxinyang
 * @date  [2012-12-26]
 */
public class AppsManagementCleanHistoryView extends RelativeLayout {

	private Button mButton = null;

	private RelativeLayout mRelativeLayout = null;

	public AppsManagementCleanHistoryView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		mButton = (Button) this.findViewById(R.id.clean_history_button);
		mRelativeLayout = (RelativeLayout) this.findViewById(R.id.clean_history_relativelayout);
		super.onFinishInflate();
	}

	public void setOnButtonClickListener(OnClickListener listener) {
		if (mButton != null) {
			mButton.setOnClickListener(listener);
		}
	}

	public void viewGone() {
		this.setVisibility(View.GONE);
		mButton.setVisibility(View.GONE);
		mRelativeLayout.setVisibility(View.GONE);
	}

	public void viewVisible() {
		this.setVisibility(View.VISIBLE);
		mButton.setVisibility(View.VISIBLE);
		mRelativeLayout.setVisibility(View.VISIBLE);
	}
}
