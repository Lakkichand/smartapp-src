package com.jiubang.ggheart.appgame.base.component;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.appgame.base.utils.ButtonUtils;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreDisplayUtil;

/**
 * 应用中心二级tab栏
 * 
 * @author zhoujun
 * 
 */
public class AppGameTabsBar extends LinearLayout {
	/**
	 * tab栏的总高度 72
	 */
	private static final int TAB_HEIGHT_BIG = 68;
	/**
	 * tab栏的总高度 52
	 */
	private static final int TAB_HEIGHT_SMALL = 60;
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

	public AppGameTabsBar(Context context, TabObserver tabObserver) {
		super(context);
		init(tabObserver);
	}

	private void init(TabObserver tabObserver) {
		this.setOrientation(VERTICAL);
		big();
		mTabsArrayList = new ArrayList<TextView>();
		mTabObserver = tabObserver;
	}

	/**
	 * 高度调整为68像素
	 */
	public void big() {
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, GoStoreDisplayUtil.scalePxToMachine(
						getContext(), TAB_HEIGHT_BIG));
		lp.leftMargin = lp.rightMargin = DrawUtils.dip2px(0f);
		this.setLayoutParams(lp);
	}

	/**
	 * 高度调整为60像素
	 */
	public void small() {
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, GoStoreDisplayUtil.scalePxToMachine(
						getContext(), TAB_HEIGHT_SMALL));
		lp.leftMargin = lp.rightMargin = DrawUtils.dip2px(0f);
		this.setLayoutParams(lp);
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
					LayoutParams.FILL_PARENT, 0);
			tabparams.weight = 1.0f;
			// 子标题布局参数
			LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
					0, LayoutParams.FILL_PARENT);
			btnParams.weight = 1.0f;
			// 标题间分割线布局参数
			LinearLayout.LayoutParams gapParams = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					DrawUtils.dip2px(18.0f));
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
					imageView
							.setBackgroundResource(R.drawable.appgame_subtab_gap);
					tabTexts.addView(imageView, gapParams);
				}
			}
			this.addView(tabTexts, tabparams);
			// 底部滑动条
			LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(
					android.view.ViewGroup.LayoutParams.FILL_PARENT,
					DrawUtils.dip2px(2.6666666667f));
			lineParams.weight = 0;
			mScrllLineView = new ScrollTip(context);
			mScrllLineView.setBackgroundResource(R.drawable.appgame_subtab_scrollline);
			mScrllLineView.setBlockDrawable(getResources().getDrawable(
					R.drawable.appgame_subtab_scrollline_block));
			mScrllLineView.setScaleType(ImageView.ScaleType.FIT_XY);
			mScrllLineView.setCurrentIndex(0);
			mScrllLineView.init(count);
			mScrllLineView.updateUI();
			this.addView(mScrllLineView, lineParams);
			//最底下加一条白线过度
			ImageView line = new ImageView(context);
			line.setBackgroundResource(R.drawable.appgame_subtab_line);
			lineParams = new LinearLayout.LayoutParams(
					android.view.ViewGroup.LayoutParams.FILL_PARENT,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
			lineParams.weight = 0;
			this.addView(line, lineParams);
		}
	}

	private TextView createTabButton(Context context, int index, int count,
			String title) {
		TextView textView = new TextView(context);
		boolean isZH = ButtonUtils.isZH(title);
		textView.setSingleLine(true);
		textView.setGravity(Gravity.CENTER);
		if (isZH) {
			textView.setTextSize(16f);
			//不加粗
			textView.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
		} else {
			textView.setTextSize(14f);
			//加粗
			textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
		}
		textView.setText(title);
		textView.setTextColor(0xFF5e5e5e);
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
			v.setTextColor(0xFF6ba001);
		} else {
			// 未选中状态
			v.setTextColor(0xFF5e5e5e);
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
