package com.smartapp.appfreezer.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 备份主界面tab栏
 * 
 * @author xiedezhi
 * 
 */
public class GoBackupTabsBar extends LinearLayout {
	/**
	 * tab栏的总高度 41dp
	 */
	private static final int TAB_HEIGHT_BIG = 48;
	/**
	 * 所有tab条的集合
	 */
	private ArrayList<TextView> mTabsArrayList;
	/**
	 * 当前被选中的下标
	 */
	private int mCurrentIndex;

	/**
	 * tab点击回调事件
	 */
	private TabObserver mTabObserver;
	/**
	 * 底部滑动条
	 */
	private ScrollTip mScrllLineView = null;
	/**
	 * 选中颜色
	 */
	private final int mSelectedColor = 0xFF31b6e6;
	/**
	 * 未选中颜色
	 */
	private final int mUnSelectedColor = 0xFF8c8c8c;
	/**
	 * 分隔线颜色
	 */
	private final int mGapColor = 0xFF414141;

	public GoBackupTabsBar(Context context, TabObserver tabObserver) {
		super(context);
		init(tabObserver);
	}

	private void init(TabObserver tabObserver) {
		this.setOrientation(VERTICAL);
		// 设置布局属性
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, UIUtil.dip2px(getContext(),
						TAB_HEIGHT_BIG));
		lp.leftMargin = lp.rightMargin = UIUtil.dip2px(getContext(), 0f);
		this.setLayoutParams(lp);

		mTabsArrayList = new ArrayList<TextView>();
		mTabObserver = tabObserver;
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
			mCurrentIndex = 0;
			setButtonStatus(0, true);
		}
	}

	private void initElements(List<String> tabTitles) {
		Context context = getContext();
		if (context == null) {
			return;
		}
		if (tabTitles != null && tabTitles.size() > 0) {
			// 标题容器布局参数
			LinearLayout tabTexts = new LinearLayout(context);
			tabTexts.setOrientation(HORIZONTAL);
			LinearLayout.LayoutParams tabparams = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, 0);
			tabparams.weight = 1.0f;
			// 子标题布局参数
			LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
					0, LayoutParams.MATCH_PARENT);
			btnParams.weight = 1.0f;
			// 标题间分割线布局参数
			LinearLayout.LayoutParams gapParams = new LinearLayout.LayoutParams(
					UIUtil.dip2px(getContext(), 0.5f), UIUtil.dip2px(
							getContext(), 21.66666667f));
			gapParams.gravity = Gravity.CENTER_VERTICAL;
			gapParams.weight = 0;

			int count = tabTitles.size();
			TextView textView = null;
			ImageView imageView = null;
			for (int i = 0; i < count; i++) {
				textView = createTabButton(context, i, count, tabTitles.get(i));
				if (mTabsArrayList != null) {
					mTabsArrayList.add(textView);
				}
				tabTexts.addView(textView, btnParams);
				// 标题之间加一条分割线
				if (i != count - 1) {
					imageView = new ImageView(context);
					imageView.setBackgroundColor(mGapColor);
					tabTexts.addView(imageView, gapParams);
				}
			}
			this.addView(tabTexts, tabparams);
			// 底部滑动条布局
			LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(
					android.view.ViewGroup.LayoutParams.MATCH_PARENT,
					UIUtil.dip2px(getContext(), 4.0f));
			lineParams.weight = 0;
			mScrllLineView = new ScrollTip(context);
			// 背景透明
			mScrllLineView.setBackgroundDrawable(new ColorDrawable(0));
			mScrllLineView.setBlockDrawable(new ColorDrawable(0xFF31b6e6));
			mScrllLineView
					.setBackBlockHeight(UIUtil.dip2px(getContext(), 1.0f));
			mScrllLineView.setBackBlockDrawable(new ColorDrawable(0xFF4e4e4e));
			mScrllLineView.setBlockGap(UIUtil.dip2px(getContext(), 1f));
			mScrllLineView.setScaleType(ImageView.ScaleType.FIT_XY);
			mScrllLineView.setCurrentIndex(0);
			mScrllLineView.init(count);
			mScrllLineView.updateUI();
			this.addView(mScrllLineView, lineParams);
		}
	}

	private TextView createTabButton(Context context, int index, int count,
			String title) {
		TextView textView = new TextView(context);
		textView.setSingleLine(true);
		textView.setGravity(Gravity.CENTER);
		textView.setTextSize(14);
		// 不加粗
		textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
		textView.setText(title);
		textView.setTextColor(mUnSelectedColor);
		textView.setTag(index);

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
	public void setButtonSelected(int index, boolean animation) {
		if (index == mCurrentIndex) {
			return;
		}
		if (mTabsArrayList != null && index >= 0
				&& index < mTabsArrayList.size()) {
			TextView textView = mTabsArrayList.get(index);
			if (textView != null) {
				setButtonStatus(mCurrentIndex, false);
				setButtonStatus(index, true);
				postInvalidate();
			}
			setBlockIndex(index, animation);
			mCurrentIndex = index;
		}
	}

	private void setButtonStatus(int index, boolean isSelected) {
		if (mTabsArrayList == null || index < 0
				|| index >= mTabsArrayList.size()) {
			return;
		}
		TextView v = mTabsArrayList.get(index);
		if (v == null) {
			return;
		}
		if (isSelected) {
			v.setTextColor(mSelectedColor);
		} else {
			// 未选中状态
			v.setTextColor(mUnSelectedColor);
		}
	}

	public void setBlockIndex(int index, boolean animation) {
		if (animation) {
			if (mCurrentIndex < index) {
				mScrllLineView.moveRight(index - mCurrentIndex);
			} else {
				mScrllLineView.moveLeft(mCurrentIndex - index);
			}
		} else {
			mScrllLineView.setCurrentIndex(index);
			mScrllLineView.updateUI();
		}

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
		if (mScrllLineView != null) {
			mScrllLineView.recycle();
			mScrllLineView = null;
		}
		mTabObserver = null;
		mTabClick = null;
	}

	/**
	 * 准备跳转到下一层级tab时，先清理掉当前层的tab数据
	 */
	public void cleanData() {
		if (mScrllLineView != null) {
			mScrllLineView.recycle();
		}
		this.removeAllViews();
		if (mTabsArrayList != null) {
			for (TextView textView : mTabsArrayList) {
				if (textView != null) {
					textView.setBackgroundDrawable(null);
				}
			}
			mTabsArrayList.clear();
		}
	}

	/**
	 * 
	 * <br>
	 * 类描述: tab 点击事件回调 <br>
	 * 功能详细描述:
	 * 
	 * @author zhoujun
	 * @date [2012-9-11]
	 */
	public interface TabObserver {
		public void handleChangeTab(int tabIndex);
	}

	OnClickListener mTabClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			int tabIndex = mTabsArrayList.indexOf(v);
			if (mTabObserver != null) {
				mTabObserver.handleChangeTab(tabIndex);
			}
		}
	};
}
