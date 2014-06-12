package com.jiubang.ggheart.apps.gowidget.gostore.component;

import com.gau.go.launcherex.R;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * 
 * <br>
 * 类描述:Go精品主题详情指示器 <br>
 * 功能详细描述:
 * 
 * @author lijunye
 * @date [2012-10-29]
 */
public class GoStoreIndicators extends LinearLayout {

	private int mIndicatorCount;

	public GoStoreIndicators(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public GoStoreIndicators(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setIndicatorCount(int c) {
		mIndicatorCount = c;
		Context ctx = getContext();
		for (int i = 0; i < c; i++) {
			SimpleImageView v = new SimpleImageView(ctx);
			v.setPadding(5, 0, 5, 0);
			if (c == 0) {
				v.setImageResource(R.drawable.gostore_theme_gallery_mark_first);
			} else {
				v.setImageResource(R.drawable.gostore_theme_gallery_mark);
			}
			addView(v);
		}
	}

	public void setHightlightIndicator(int i) {
		for (int j = 0; j < mIndicatorCount; j++) {
			SimpleImageView v = (SimpleImageView) getChildAt(j);
			if (j == 0) {
				v.setImageResource((j == i) ? R.drawable.gostore_theme_gallery_mark_first_selected
						: R.drawable.gostore_theme_gallery_mark_first);
			} else {
				v.setImageResource((j == i) ? R.drawable.gostore_theme_gallery_mark_selected
						: R.drawable.gostore_theme_gallery_mark); // 通过切换图片达到指示器的效果
			}

		}
	}

}
