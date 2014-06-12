package com.jiubang.ggheart.apps.gowidget.gostore.component;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.gau.go.launcherex.R;

/**
 * 
 * @author liulixia
 *
 */
public class ThemeStoreProgressBar extends LinearLayout {
	public ThemeStoreProgressBar(Context context) {
		super(context);
	}

	public ThemeStoreProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		// TODO Auto-generated method stub
		super.onFinishInflate();
//		this.setBackgroundResource(R.drawable.themestore_main_list_item_selector2);
		// this.setBackgroundColor(R.color.theme_bg);
		ProgressBar progressBar = (ProgressBar) this.findViewById(R.id.themestore_btmprogress);
		progressBar.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
		Drawable drawable = getContext().getResources().getDrawable(R.drawable.go_progress_green);
		progressBar.setIndeterminateDrawable(drawable);
	}
}