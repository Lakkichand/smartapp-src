package com.jiubang.ggheart.appgame.base.component;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.gau.go.launcherex.R;

/**
 * 
 * <br>类描述:应用中心加载loading进度条
 * <br>功能详细描述:
 * 
 * @author  zhengxiangcan
 * @date  [2012-12-14]
 */
public class AppDetailProgressBar extends LinearLayout {
	public AppDetailProgressBar(Context context) {
		super(context);
	}

	public AppDetailProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		// TODO Auto-generated method stub
		super.onFinishInflate();
		this.setBackgroundResource(R.drawable.appdetail_background);
		// this.setBackgroundColor(R.color.theme_bg);
		ProgressBar progressBar = (ProgressBar) this.findViewById(R.id.themestore_btmprogress);
		progressBar.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
		Drawable drawable = getContext().getResources().getDrawable(R.drawable.go_progress_green);
		progressBar.setIndeterminateDrawable(drawable);
	}
}
