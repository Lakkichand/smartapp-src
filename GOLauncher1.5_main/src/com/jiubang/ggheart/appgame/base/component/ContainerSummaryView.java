package com.jiubang.ggheart.appgame.base.component;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.graphics.DrawUtils;

/**
 * 
 * 用于展示每个container中一句话简介的view
 * 
 * @author xiedezhi
 * @date [2012-9-25]
 */
public class ContainerSummaryView extends RelativeLayout {
	/**
	 * 展示一句话简介的textview
	 */
	private TextView mTextView;
	/**
	 * 左间隙
	 */
	private static final int PADDING_LEFT = DrawUtils.dip2px(26.6667f);
	/**
	 * 上间隙
	 */
	private static final int PADDING_TOP = DrawUtils.dip2px(13.3333f);
	/**
	 * 右间隙
	 */
	private static final int PADDING_RIGHT = DrawUtils.dip2px(26.6667f);
	/**
	 * 下间隙
	 */
	private static final int PADDING_BOTTOM = DrawUtils.dip2px(17.3333f);

	public ContainerSummaryView(Context context) {
		super(context);
	}

	public ContainerSummaryView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ContainerSummaryView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * 展示一句话简介
	 * 
	 * @param firstCapital
	 *            首字母是否大写
	 */
	public void fillUp(String text, boolean firstCapital) {
		try {
			if (text == null || text.trim().length() <= 0) {
				return;
			}
			// "###"代表换行
			text = text.replaceAll("###", "\n");
			if (!firstCapital) {
				if (mTextView != null) {
					mTextView.setText(text);
				}
				return;
			}
			Spannable word = new SpannableString(text);
			int index = 0;
			for (int i = 0; i < text.length(); i++) {
				char c = text.charAt(i);
				if (c <= ' ') {
					continue;
				}
				index = i;
				break;
			}
			word.setSpan(new AbsoluteSizeSpan(DrawUtils.dip2px(20.66667f)),
					index, index + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
			word.setSpan(new ForegroundColorSpan(0xff6a6a6a), index, index + 1,
					Spannable.SPAN_INCLUSIVE_INCLUSIVE);
			if (mTextView != null) {
				mTextView.setText(word);
			}
		} catch (Exception e) {
			e.printStackTrace();
			viewGone();
		}
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mTextView = (TextView) findViewById(R.id.appgame_container_summary_text);
	}

	/**
	 * 不展示该view
	 */
	public void viewGone() {
		mTextView.setBackgroundDrawable(null);
		mTextView.setVisibility(View.GONE);
		this.setVisibility(View.GONE);
	}

	/**
	 * 展示该view
	 */
	public void viewVisible() {
		this.setVisibility(View.VISIBLE);
		mTextView.setVisibility(View.VISIBLE);
		mTextView
				.setBackgroundResource(R.drawable.appgame_container_summary_bg);
		this.setPadding(0, 0, 0, 0);
		mTextView.setPadding(PADDING_LEFT, PADDING_TOP, PADDING_RIGHT,
				PADDING_BOTTOM);
	}

}
