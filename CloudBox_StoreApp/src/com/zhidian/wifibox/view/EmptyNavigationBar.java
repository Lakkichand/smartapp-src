package com.zhidian.wifibox.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.zhidian.wifibox.util.DrawUtil;

/**
 * EmptyContainer上面的导航标题栏
 * 
 * @author xiedezhi
 */
public class EmptyNavigationBar extends LinearLayout {

	private List<View> mViews = new ArrayList<View>();

	private List<Button> mBtns = new ArrayList<Button>();

	private IndexChangeListener mIndexChangeListener;

	private OnClickListener mClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int index = (Integer) v.getTag();
			setSelect(index);
		}
	};

	public EmptyNavigationBar(Context context) {
		super(context);
	}

	public EmptyNavigationBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setIndexChangeListener(IndexChangeListener listener) {
		mIndexChangeListener = listener;
	}

	/**
	 * 生成导航栏并初始化点击事件
	 */
	public void update(List<String> titles, List<View> views) {
		removeAllViews();
		mViews.clear();
		mBtns.clear();
		if (titles == null || titles.size() <= 1 || views == null
				|| views.size() <= 1 || titles.size() != views.size()) {
			setVisibility(View.GONE);
			return;
		}
		setVisibility(View.VISIBLE);
		mViews.addAll(views);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
				DrawUtil.dip2px(getContext(), 38));
		lp.weight = 1.0f;
		for (int i = 0; i < titles.size(); i++) {
			String title = titles.get(i);
			Button btn = new Button(getContext());
			btn.setText(title);
			btn.setTextSize(17f);
			//加粗
			btn.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
			btn.setTextColor(0xFF333333);
			btn.setBackgroundColor(0xFFf8f8f8);
			addView(btn, lp);
			btn.setTag(i);
			btn.setOnClickListener(mClickListener);
			mBtns.add(btn);
		}
	}

	/**
	 * 设置选中的页面
	 */
	public void setSelect(int index) {
		for (Button btn : mBtns) {
			btn.setTextColor(0xFF333333);
			btn.setBackgroundColor(0xFFf8f8f8);
		}
		for (View view : mViews) {
			view.setVisibility(View.GONE);
		}
		mBtns.get(index).setTextColor(0xFFFF7000);
		mBtns.get(index).setBackgroundColor(0xFFfff5de);
		mViews.get(index).setVisibility(View.VISIBLE);
		if (mIndexChangeListener != null) {
			mIndexChangeListener.indexChange(index);
		}
	}

	public interface IndexChangeListener {
		public void indexChange(int index);
	}

}
