package com.jiubang.ggheart.appgame.base.component;

import com.gau.go.launcherex.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

/**
 * 
 * 应用游戏中心，列表加载下一页的进度条，浮在列表底部
 * 
 * @author  xiedezhi
 * @date  [2012-12-20]
 */
public class CommonProgress extends RelativeLayout {

	private ProgressBar mProgressBar;

	public CommonProgress(Context context) {
		super(context);
	}

	public CommonProgress(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CommonProgress(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mProgressBar = (ProgressBar) findViewById(R.id.common_progressbar);
		Drawable drawable = getContext().getResources().getDrawable(R.drawable.go_progress_green);
		mProgressBar.setIndeterminateDrawable(drawable);
	}

}
