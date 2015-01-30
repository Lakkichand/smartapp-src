package com.zhidian.wifibox.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jiubang.core.message.IMessageHandler;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.message.IDiyFrameIds;
import com.zhidian.wifibox.message.IDiyMsgIds;
import com.zhidian.wifibox.util.DrawUtil;

/**
 * 二级tab栏
 * 
 * @author xiedezhi
 * 
 */
public class ActionBar extends LinearLayout implements IMessageHandler {
	/**
	 * tab栏的总高度 72
	 */
	private static final int TAB_HEIGHT_BIG = 68;
	/**
	 * tab栏的总高度 52
	 */
	private static final int TAB_HEIGHT_SMALL = 32;
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

	public ActionBar(Context context, TabObserver tabObserver) {
		super(context);
		init(tabObserver);
	}

	private void init(TabObserver tabObserver) {
		this.setOrientation(VERTICAL);
		small();
		mTabsArrayList = new ArrayList<TextView>();
		mTabObserver = tabObserver;
	}

	/**
	 * 高度调整为68像素
	 */
	public void big() {
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, DrawUtil.dip2px(getContext(),
						TAB_HEIGHT_BIG));
		lp.leftMargin = lp.rightMargin = DrawUtil.dip2px(getContext(), 0f);
		this.setLayoutParams(lp);
	}

	/**
	 * 高度调整为60像素
	 */
	public void small() {
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, DrawUtil.dip2px(getContext(),
						TAB_HEIGHT_SMALL));
		lp.leftMargin = lp.rightMargin = DrawUtil.dip2px(getContext(), 0f);
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
					LayoutParams.MATCH_PARENT, 0);
			tabparams.weight = 1.0f;
			// 子标题布局参数
			LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
					0, LayoutParams.MATCH_PARENT);
			btnParams.weight = 1.0f;
			// 标题间分割线布局参数
			LinearLayout.LayoutParams gapParams = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT, DrawUtil.dip2px(
							getContext(), 18.0f));
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
					imageView.setBackgroundDrawable(null);
					tabTexts.addView(imageView, gapParams);
				}
			}
			this.addView(tabTexts, tabparams);
			// 底部滑动条
			FrameLayout frame = new FrameLayout(getContext());
			View bottom_gap = new View(getContext());
			bottom_gap.setBackgroundColor(0xFFFFFFFF);
			FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(
					FrameLayout.LayoutParams.MATCH_PARENT, DrawUtil.dip2px(
							getContext(), 1));
			flp.gravity = Gravity.BOTTOM;
			frame.addView(bottom_gap, flp);
			mScrllLineView = new ScrollTip(context);
			mScrllLineView.setBackgroundDrawable(null);
			mScrllLineView.setBlockDrawable(getResources().getDrawable(
					R.drawable.v2_biaoqian));
			mScrllLineView.setScaleType(ImageView.ScaleType.FIT_XY);
			mScrllLineView.setCurrentIndex(0);
			mScrllLineView.init(count);
			mScrllLineView.updateUI();
			frame.addView(
					mScrllLineView,
					new FrameLayout.LayoutParams(
							FrameLayout.LayoutParams.MATCH_PARENT, DrawUtil
									.dip2px(getContext(), 2.6666666667f)));
			LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			lineParams.weight = 0;
			this.addView(frame, lineParams);
			// 最底下加一条白线过度
			ImageView line = new ImageView(context);
			line.setBackgroundResource(R.drawable.appgame_subtab_line);
			lineParams = new LinearLayout.LayoutParams(
					android.view.ViewGroup.LayoutParams.MATCH_PARENT,
					DrawUtil.dip2px(getContext(), 2f));
			lineParams.weight = 0;
			this.addView(line, lineParams);
		}
	}

	private TextView createTabButton(Context context, int index, int count,
			String title) {
		TextView textView = new TextView(context);
		textView.setSingleLine(true);
		textView.setGravity(Gravity.CENTER);
		textView.setTextSize(17f);
		// 加粗
		textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
		textView.setText(title);
		textView.setTextColor(0xFF666666);
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
			v.setTextColor(0xFF04afee);
		} else {
			// 未选中状态
			v.setTextColor(0xFF666666);
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
	 * @author xiedezhi
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

	public int getId() {
		return IDiyFrameIds.ACTIONBAR;
	}

	@Override
	public boolean handleMessage(Object who, int type, final int msgId,
			final int param, final Object object, final List objects) {
		switch (msgId) {
		case IDiyMsgIds.JUMP_TITLE: {
			int index = param;
			if (index >= 0 && index < mTabsArrayList.size()) {
				mTabsArrayList.get(index).performClick();
			}
			break;
		}
		}
		return false;
	}
}
