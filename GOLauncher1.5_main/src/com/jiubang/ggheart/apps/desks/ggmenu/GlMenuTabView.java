/**
 * 
 */
package com.jiubang.ggheart.apps.desks.ggmenu;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.jiubang.core.framework.ICleanable;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.components.DeskTextView;

/**
 * @author ruxueqin
 * 
 */
public class GlMenuTabView extends RelativeLayout
		implements
			OnClickListener,
			TabViewListener,
			ICleanable {
	// 背景顶边距，目的是针对顶部有不可绘制限制的9 patch
	private int mBackgroundPaddingTop;
	// tab栏的高度
	private int mTabHeight;

	private PopupWindow mPopupWindow;

	private GlMenuGridViewsContainer mContainer;
	// tab栏布局
	private LinearLayout mTabsLayout;
	// tab下面直线的布局
	private LinearLayout mLineLayout;

	public static final int COMMONTAB_ID = 0; // “常用”tab的ID
	public static final int PERIPHERYTAB_ID = 1; // “周边”tab的ID
	public static final int MORETAB_ID = 2; // “更多”tab的ID

	private TextView mCommonTab; // 常用
	private TextView mPeripheryTab; // 周边
	private RelativeLayout mPeripheryTabGroup; // 更多添加了一个img
	private ImageView mPerIpheryTabNewImage;
	private TextView mMoreTab; // 更多

	public static final int DEFALUTSELECTTABCOLOR = 0xff51B801;
	public static final int DEFALUTUNSELECTTABCOLOR = 0xff919192;
	private int mSelectedColor; // tab选中的文字颜色
	private int mUnselectedColor; // tab没被选中的文字颜色

	private int mStartLeft;

	private ImageView mLineLight; // tab选中时的直线

	private ImageView mCommonLine; // “常用”下面的直线
	private ImageView mPeripheryLine; // “周边”下面的直线
	private ImageView mMoreLine; // “更多”下面的直线

	private Drawable mSelectedLine; // tab选中时的直线图片
	private Drawable mUnselectedLine; // tab没被选中时的直线图片

	/**
	 * @param context
	 * @param attrs
	 */
	public GlMenuTabView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		mContainer = (GlMenuGridViewsContainer) findViewById(R.id.container);
		mContainer.setTabViewListener(this);
		mTabsLayout = (LinearLayout) findViewById(R.id.menu_tabs);
		mLineLayout = (LinearLayout) findViewById(R.id.menu_lines);
		initLine();
		initTabTexts();

	}

	// 初始化文字信息
	private void initTabTexts() {
		mCommonTab = (TextView) mTabsLayout.findViewById(R.id.main_menu_text1);
		mCommonTab.setOnClickListener(this);
		mPeripheryTab = (TextView) mTabsLayout.findViewById(R.id.main_menu_text2);
		mPeripheryTab.setOnClickListener(this);
		mPeripheryTabGroup = (RelativeLayout) findViewById(R.id.main_menu_text2_group);
		mPeripheryTabGroup.setOnClickListener(this);
		mPerIpheryTabNewImage = (ImageView) mPeripheryTabGroup.findViewById(R.id.new_locker_theme);
		mMoreTab = (TextView) mTabsLayout.findViewById(R.id.main_menu_text3);
		mMoreTab.setOnClickListener(this);
		changeTabState(COMMONTAB_ID);
		PreferencesManager manager = new PreferencesManager(getContext(),
				IPreferencesIds.FEATUREDTHEME_CONFIG, Context.MODE_PRIVATE);
		boolean hasNewTheme = manager.getBoolean(IPreferencesIds.HASNEWTHEME, false);
		boolean hasNewLockerTheme = manager.getBoolean(IPreferencesIds.LOCKER_HASNEWTHEME, false);
		if (!hasNewTheme && hasNewLockerTheme) {
			addNewLockerThemeLogo();
		}
	}

	// 初始化直线信息
	private void initLine() {
		// mLineLight =
		// (ImageView)mTabsLayout.findViewById(R.id.menu_line_light);
		mCommonLine = (ImageView) mLineLayout.findViewById(R.id.main_menu_line1);
		mPeripheryLine = (ImageView) mLineLayout.findViewById(R.id.main_menu_line2);
		mMoreLine = (ImageView) mLineLayout.findViewById(R.id.main_menu_line3);
		mSelectedLine = mCommonLine.getBackground();
		mUnselectedLine = mMoreLine.getBackground();

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		int childheight = mContainer.getMeasuredHeight();
		//		int messageCount = MessageManager.getMessageManager(GOLauncherApp.getContext())
		//				.getUnreadedCnt();
		if (mContainer.getTabCount() > 1) {
			// 当tab栏数目大于1时，才显示tab的文字和直线
			mTabsLayout.setVisibility(View.VISIBLE);
			mLineLayout.setVisibility(View.VISIBLE);
			mTabHeight = mTabsLayout.getMeasuredHeight() + mLineLayout.getMeasuredHeight();
		} else {
			mTabsLayout.setVisibility(View.GONE);
			mLineLayout.setVisibility(View.GONE);
		}
		mPopupWindow.setHeight(mBackgroundPaddingTop + mTabHeight + childheight);

		setMeasuredDimension(getMeasuredWidth(), mPopupWindow.getHeight());
	}

	public void setmPopupWindow(PopupWindow popupWindow) {
		mPopupWindow = popupWindow;
	}

	@Override
	public void setBackgroundDrawable(Drawable d) {
		super.setBackgroundDrawable(d);

		if (null != d) {
			Rect rect = new Rect();
			d.getPadding(rect);
			mBackgroundPaddingTop = rect.top;
		}
	}

	@Override
	public void onClick(View v) {
		if (v == null) {
			return;
		}
		int tabId;
		if (v == mCommonTab) {
			tabId = COMMONTAB_ID;
			mContainer.gotoTab(COMMONTAB_ID);

			// int endLeft = mCommonTab.getLeft();
			// startAnimation(endLeft);
		} else if (v == mPeripheryTab || v == mPeripheryTabGroup) {
			tabId = PERIPHERYTAB_ID;
			mContainer.gotoTab(PERIPHERYTAB_ID);
			removeNewLockerThemeLogo();
			// int endLeft = mPeripheryTab.getLeft();
			// startAnimation(endLeft);

		} else if (v == mMoreTab) {
			tabId = MORETAB_ID;
			mContainer.gotoTab(MORETAB_ID);

			// int endLeft = mMoreTab.getLeft();
			// startAnimation(endLeft);
		}
	}

	/**
	 * 更换tab栏的状态
	 * 
	 * @param tabId
	 *            所选中的tab ID
	 */
	public void changeTabState(int tabId) {
		if (tabId == COMMONTAB_ID) {
			// 设置文字
			mCommonTab.setTextColor(mSelectedColor);
			mPeripheryTab.setTextColor(mUnselectedColor);
			mMoreTab.setTextColor(mUnselectedColor);
			// 设置线
			mCommonLine.setBackgroundDrawable(mSelectedLine);
			mPeripheryLine.setBackgroundDrawable(mUnselectedLine);
			mMoreLine.setBackgroundDrawable(mUnselectedLine);
		} else if (tabId == PERIPHERYTAB_ID) {
			mCommonTab.setTextColor(mUnselectedColor);
			mPeripheryTab.setTextColor(mSelectedColor);
			mMoreTab.setTextColor(mUnselectedColor);

			mCommonLine.setBackgroundDrawable(mUnselectedLine);
			mPeripheryLine.setBackgroundDrawable(mSelectedLine);
			mMoreLine.setBackgroundDrawable(mUnselectedLine);
			removeNewLockerThemeLogo();
		} else if (tabId == MORETAB_ID) {
			mCommonTab.setTextColor(mUnselectedColor);
			mPeripheryTab.setTextColor(mUnselectedColor);
			mMoreTab.setTextColor(mSelectedColor);

			mCommonLine.setBackgroundDrawable(mUnselectedLine);
			mPeripheryLine.setBackgroundDrawable(mUnselectedLine);
			mMoreLine.setBackgroundDrawable(mSelectedLine);
		}
	}

	/*
	 * private void startAnimation(int endLeft) { TranslateAnimation animation =
	 * new TranslateAnimation(mStartLeft, endLeft, 0, 0); mStartLeft = endLeft;
	 * animation.setDuration(300); animation.setFillAfter(true);
	 * mLineLight.startAnimation(animation); }
	 */

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN && mPopupWindow.isShowing()
				&& event.getY() < 0) {
			// 在菜单之上点击,event.getY() < 0
			mPopupWindow.dismiss();
		}

		return super.onTouchEvent(event);
	}

	@Override
	public void changeTabStateMent(int tabId) {
		changeTabState(tabId);
	}

	@Override
	public void cleanup() {
		if (mCommonTab != null && mCommonTab instanceof DeskTextView) {
			((DeskTextView) mCommonTab).selfDestruct();
		}

		if (mPeripheryTab != null && mPeripheryTab instanceof DeskTextView) {
			((DeskTextView) mPeripheryTab).selfDestruct();
		}
		if (mMoreTab != null && mMoreTab instanceof DeskTextView) {
			((DeskTextView) mMoreTab).selfDestruct();
		}
		if (null != mContainer) {
			mContainer.cleanup();
		}
		mSelectedLine = null;
		mUnselectedLine = null;
	}

	/**
	 * @param mSelectedColor
	 *            the mSelectedColor to set
	 */
	public void setmTabFontColor(int mSelectedColor, int mUnselectedColor) {
		this.mSelectedColor = mSelectedColor;
		this.mUnselectedColor = mUnselectedColor;
	}

	public void setmTabLineDrawable(Drawable selectDrawable, Drawable unselectDrawable) {
		mSelectedLine = selectDrawable;
		mUnselectedLine = unselectDrawable;
	}

	private void addNewLockerThemeLogo() {
		if (mPerIpheryTabNewImage != null && mPerIpheryTabNewImage.getVisibility() != View.VISIBLE) {
			mPerIpheryTabNewImage.setVisibility(View.VISIBLE);
		}
	}

	private void removeNewLockerThemeLogo() {
		if (mPerIpheryTabNewImage != null && mPerIpheryTabNewImage.getVisibility() != View.GONE) {
			mPerIpheryTabNewImage.setVisibility(View.GONE);
		}
	}
}
