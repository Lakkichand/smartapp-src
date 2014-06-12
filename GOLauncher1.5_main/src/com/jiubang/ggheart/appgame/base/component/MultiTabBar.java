package com.jiubang.ggheart.appgame.base.component;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.graphics.DrawUtils;

/**
 * 
 * MultiContainer顶部的按钮形式tab栏，如果初始化按钮只有一个就把自己设为GONE
 * 
 * @author  xiedezhi
 * @date  [2012-11-28]
 */
public class MultiTabBar extends LinearLayout {
	/**
	 * 所有tab条的集合
	 */
	private ArrayList<TextView> mTabsArrayList;
	/**
	 * 当前选中的button项
	 */
	private TextView mCurrentSelectedButton;
	/**
	 * tab点击回调事件
	 */
	private TabObserver mTabObserver;
	/**
	 * 按钮高度
	 */
	private final int mButtonHeight = DrawUtils.dip2px(30.6f);
	/**
	 * 按钮选中颜色
	 */
	private final int mUnSelectedColor = 0xFF666666;
	/**
	 * 按钮未选中颜色mCurrentSelectedButton
	 */
	private final int mSelectedColor = 0xFFFFFFFF;

	private LinearLayout.LayoutParams mButtonLayoutParams = null;

	public MultiTabBar(Context context, TabObserver tabObserver) {
		super(context);
		init(tabObserver);
	}

	private void init(TabObserver tabObserver) {
		this.setOrientation(HORIZONTAL);
		this.setGravity(Gravity.CENTER);
		mTabsArrayList = new ArrayList<TextView>();
		mTabObserver = tabObserver;

		mButtonLayoutParams = new LinearLayout.LayoutParams(0, mButtonHeight);
		mButtonLayoutParams.weight = 1.0f;
		mButtonLayoutParams.gravity = Gravity.CENTER;
	}

	/**
	 * 设置tab栏要显示的tab信息
	 * 
	 * @param tabTitles
	 */
	public void initTabsBar(List<String> tabTitles) {
		if (mTabsArrayList == null) {
			mTabsArrayList = new ArrayList<TextView>();
		}
		if (mTabsArrayList.size() > 0) {
			mTabsArrayList.clear();
		}
		// tab数据为null或者只有一个tab，不显示tab栏
		if (tabTitles == null || tabTitles.size() <= 1) {
			this.setVisibility(View.GONE);
			return;
		}
		this.setVisibility(View.VISIBLE);
		initElements(tabTitles);
		if (mTabsArrayList.size() > 0) {
			mCurrentSelectedButton = mTabsArrayList.get(0);
			setButtonStatus(mCurrentSelectedButton, true);
		}
	}

	private void initElements(List<String> tabTitles) {
		Context context = getContext();
		if (context == null) {
			return;
		}
		if (tabTitles != null && tabTitles.size() > 0) {
			int count = tabTitles.size();
			TextView textView = null;
			for (int i = 0; i < count; i++) {
				textView = createTabButton(context, i, count, tabTitles.get(i));
				if (mTabsArrayList != null) {
					mTabsArrayList.add(textView);
				}
				this.addView(textView, mButtonLayoutParams);
			}
		}
	}

	private TextView createTabButton(Context context, int index, int count, String title) {
		TextView textView = new TextView(context);

		textView.setSingleLine(true);
		textView.setGravity(Gravity.CENTER);
		textView.setTextSize(13.2f);
		textView.setText(title);
		textView.setTextColor(mUnSelectedColor);
		textView.setTag(index);

		if (index == 0) {
			// 设置左边的底图
			textView.setBackgroundResource(R.drawable.app_mgr_tab_left);
		} else if (index == count - 1) {
			// 设置右边的底图
			textView.setBackgroundResource(R.drawable.app_mgr_tab_right);
		} else {
			// 设置中间的底图
			textView.setBackgroundResource(R.drawable.app_mgr_tab);
		}

		if (count > 1) {
			textView.setOnClickListener(mTabClick);
		}
		return textView;
	}

	/**
	 * 通过下标设置某个按钮选中
	 * 
	 * @param index
	 */
	public void setButtonSelected(int index) {
		if (mTabsArrayList != null && index >= 0 && index < mTabsArrayList.size()) {
			TextView textView = mTabsArrayList.get(index);
			if (textView != null && !textView.equals(mCurrentSelectedButton)) {
				setButtonStatus(mCurrentSelectedButton, false);
				setButtonStatus(textView, true);
				mCurrentSelectedButton = textView;
				postInvalidate();
			}
		}
	}

	private void setButtonStatus(View v, boolean isSelected) {
		if (mTabsArrayList == null) {
			return;
		}
		if (isSelected) {
			((TextView) v).setTextColor(mSelectedColor);;
		} else {
			// 未选中状态
			((TextView) v).setTextColor(mUnSelectedColor);;
		}

		int position = (Integer) v.getTag();
		int resourcesId = 0;
		if (position == 0) {
			if (isSelected) {
				resourcesId = R.drawable.app_mgr_tab_left_light;
			} else {
				resourcesId = R.drawable.app_mgr_tab_left;
			}
		} else if (position == mTabsArrayList.size() - 1) {
			if (isSelected) {
				resourcesId = R.drawable.app_mgr_tab_right_light;
			} else {
				resourcesId = R.drawable.app_mgr_tab_right;
			}
		} else {
			if (isSelected) {
				resourcesId = R.drawable.app_mgr_tab_light;
			} else {
				resourcesId = R.drawable.app_mgr_tab;
			}
		}
		v.setBackgroundResource(resourcesId);
	}

	/**
	 * 回收资源
	 */
	public void recycle() {
		this.setBackgroundDrawable(null);
		this.removeAllViews();
		if (mTabsArrayList != null) {
			for (TextView textView : mTabsArrayList) {
				if (textView != null) {
					textView.setBackgroundDrawable(null);
				}
			}
			mTabsArrayList.clear();
			mTabsArrayList = null;
		}

		mCurrentSelectedButton = null;
		mTabObserver = null;
		mTabClick = null;
	}

	/**
	 * 准备跳转到下一层级tab时，先清理掉当前层的tab数据
	 */
	public void cleanData() {
		this.removeAllViews();
		if (mTabsArrayList != null) {
			for (TextView textView : mTabsArrayList) {
				if (textView != null) {
					textView.setBackgroundDrawable(null);
				}
			}
			mTabsArrayList.clear();
		}
		mCurrentSelectedButton = null;
	}

	/**
	 * 按钮点击观察者
	 */
	public interface TabObserver {
		public void handleChangeTab(int tabIndex);
	}

	/**
	 * 按钮点击事件
	 */
	private OnClickListener mTabClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			int tabIndex = mTabsArrayList.indexOf(v);
			if (mTabObserver != null) {
				mTabObserver.handleChangeTab(tabIndex);
			}
		}
	};
}
