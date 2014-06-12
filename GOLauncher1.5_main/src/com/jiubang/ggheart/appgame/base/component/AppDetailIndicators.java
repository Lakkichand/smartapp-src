package com.jiubang.ggheart.appgame.base.component;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.gowidget.gostore.component.SimpleImageView;

/**
 * 
 * <br>
 * 类描述:应用详情指示器 <br>
 * 功能详细描述:
 * @author zhujian
 * 
 */
public class AppDetailIndicators extends LinearLayout {

	private int mIndicatorCount;

	public AppDetailIndicators(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public AppDetailIndicators(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setIndicatorCount(int c) {
		mIndicatorCount = c;
		Context ctx = getContext();
		for (int i = 0; i < c; i++) {
			SimpleImageView v = new SimpleImageView(ctx);
			v.setPadding(5, 0, 5, 0);
			addView(v);
		}
	}

	public void setHightlightIndicator(int i) {
		for (int j = 0; j < mIndicatorCount; j++) {
			SimpleImageView v = (SimpleImageView) getChildAt(j);
				v.setImageResource((j == i) ? R.drawable.app_detail_pointer_light
						: R.drawable.app_detail_pointer);
				// 通过切换图片达到指示器的效果

		}
	}

}
