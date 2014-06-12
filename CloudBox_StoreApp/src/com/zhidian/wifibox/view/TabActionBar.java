package com.zhidian.wifibox.view;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.zhidian.wifibox.R;

/**
 * 指示导航栏
 * 
 * @author xiedezhi
 * 
 */
public class TabActionBar extends LinearLayout {

	private TabObserver mListener;
	/**
	 * 按钮点击事件
	 */
	private OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int index = (Integer) v.getTag();
			mListener.handleChangeTab(index);
			setSelection(index);
		}
	};

	public TabActionBar(Context context, TabObserver listener) {
		super(context);
		this.setOrientation(LinearLayout.HORIZONTAL);
		mListener = listener;
	}

	/**
	 * 初始化
	 */
	public void init(List<String> list) {
		this.removeAllViews();
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
				LinearLayout.LayoutParams.MATCH_PARENT);
		lp.weight = 1.0f;

		LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(
				(int) getResources().getDimension(R.dimen.view_xian_width),
				LinearLayout.LayoutParams.MATCH_PARENT);
		for (int i = 0; i < list.size(); i++) {
			String str = list.get(i);
			Button btn = new Button(getContext());
			btn.setText(str);
			btn.setTag(i);
			btn.setBackgroundResource(R.drawable.top_download);
			btn.setOnClickListener(mOnClickListener);
			btn.setTextColor(0xFF000000);
			addView(btn, lp);
			if (i != list.size() - 1) {
				View view = new View(getContext());
				view.setBackgroundResource(R.drawable.line);
				addView(view, lp2);
			}

		}
	}

	/**
	 * 设置选中效果
	 */
	public void setSelection(int index) {
		if (index != 0) {
			index = index + 1;
		}
		for (int i = 0; i < getChildCount(); i++) {
			if (i != 1) {
				Button child = (Button) getChildAt(i);
				if (i == index) {
					child.setTextColor(0xFF0F6FDD);
					child.setBackgroundResource(R.drawable.top_update);
				} else {
					child.setTextColor(0xFF000000);
					child.setBackgroundResource(R.drawable.top_download);
				}
			}

		}
	}

	public interface TabObserver {
		public void handleChangeTab(int tabIndex);
	}

}
