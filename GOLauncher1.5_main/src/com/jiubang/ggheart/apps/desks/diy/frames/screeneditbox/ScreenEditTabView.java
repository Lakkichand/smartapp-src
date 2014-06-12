package com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.DesktopIndicator;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.AddAppTab;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.AddFolderTab;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.BaseTab;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.WidgetSubTab;
import com.jiubang.ggheart.components.DeskTextView;
import com.jiubang.ggheart.data.statistics.StatisticsData;
/**
 * 
 * <br>类描述:负责普通tab的展示
 * <br>功能详细描述:完成布局，设置当前tab给container，并完成标题栏和指示器的操作
 */
public class ScreenEditTabView extends LinearLayout
		implements
			OnClickListener,
			TabIndicatorUpdateListner,
			TabActionListener {

	private ScreenEditBoxContainer mContainer;

	private LinearLayout mTabs; // 一级菜单
	private LinearLayout mBackTab; // 二级页面的返回菜单

	private static final int PRESS_COLOR = 0xffbfff00;
	private static final int UNPRESS_COLOR = 0x7fffffff;

	private TextView mTabApp;
	private TextView mTabWallpapers;
	private TextView mTabThemes;
	private TextView mTabEffects;
	private TextView mBackTabText;

	private DesktopIndicator mIndicator;
	private RelativeLayout mIndicatorLayout;
	private OnRespondTouch mOnRespondTouch;
	private String mCurTabTag; // 当前tab

	private ScreenEditLayout mEditLayout;

	public void setEditLayout(ScreenEditLayout mEditLayout) {
		this.mEditLayout = mEditLayout;
	}

	private DataEdngine mDataEdngine;

	private Context mContext;

	private int mHorizontalpading;
	private int mViewWidth;
	private int mItemsCount; // 当前屏最多图标数

	private ImageView mBackImg;  //返回的箭头
	private ImageView mBackIcon; // 图标
	
	public ScreenEditTabView(Context context, AttributeSet attrs) {
		super(context, attrs);

		mContext = context;
		mHorizontalpading = (int) context.getResources().getDimension(
				R.dimen.screen_edit_view_horizontal_space);
		mViewWidth = (int) mContext.getResources().getDimension(R.dimen.screen_edit_view_width);
		// 当前屏最多图标数
		mItemsCount = (GoLauncher.getDisplayWidth() - mHorizontalpading)
				/ (mViewWidth + mHorizontalpading);
		int rightSpace = GoLauncher.getDisplayWidth() - mHorizontalpading - mItemsCount
				* (mViewWidth + mHorizontalpading);
		if (rightSpace >= mViewWidth) {
			++mItemsCount;
		}

	}

	public void setmOnRespondTouch(OnRespondTouch mOnRespondTouch) {
		this.mOnRespondTouch = mOnRespondTouch;
	}

	public ScreenEditBoxContainer getContainer() {
		return mContainer;
	}

	@Override
	public void onClick(View v) {
		try {
			if (v == mTabApp) {
				if (mCurTabTag.equals(BaseTab.TAB_APP)) {
					return;
				}
				mCurTabTag = BaseTab.TAB_APP;
			} else if (v == mTabWallpapers) {
				if (mCurTabTag.equals(BaseTab.TAB_WALLPAPER)) {
					return;
				}
				mCurTabTag = BaseTab.TAB_WALLPAPER;
				//用户行为统计
				StatisticsData.countUserActionData(StatisticsData.DESK_ACTION_ID_SCREEN_EDIT,
						StatisticsData.USER_ACTION_ELEVEN, IPreferencesIds.DESK_ACTION_DATA);
			}

			else if (v == mTabThemes) {
				if (mCurTabTag.equals(BaseTab.TAB_THEMELOCKER)) {
					return;
				}
				mCurTabTag = BaseTab.TAB_THEMELOCKER;
				//用户行为统计
				StatisticsData.countUserActionData(StatisticsData.DESK_ACTION_ID_SCREEN_EDIT,
						StatisticsData.USER_ACTION_SIXTEEN, IPreferencesIds.DESK_ACTION_DATA);
			} else if (v == mTabEffects) {
				if (mCurTabTag.equals(BaseTab.TAB_EFFECTS)) {
					return;
				}
				mCurTabTag = BaseTab.TAB_EFFECTS;
				//用户行为统计
				StatisticsData.countUserActionData(StatisticsData.DESK_ACTION_ID_SCREEN_EDIT,
						StatisticsData.USER_ACTION_NINETEEN, IPreferencesIds.DESK_ACTION_DATA);
			}
			onTabClick(mCurTabTag);
		} catch (Exception e) {
			//异常情况下，安全退出添加页面
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
					IDiyMsgIds.SCREENEDIT_CLOSE_SCREEN_EDIT_LAYOUT, 1, null, null);
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_EDIT_BOX_FRAME,
					IDiyMsgIds.SCREENEDIT_SHOW_ANIMATION_OUT, 0, null, null);			// TODO: handle exception
		}
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		mTabs = (LinearLayout) findViewById(R.id.tabs);
		// int mGowidgetHeight = (int) (DrawUtils.sHeightPixels *
		// 0.425f)-DrawUtils.dip2px(30)-DrawUtils.dip2px(20);
		// FrameLayout.LayoutParams gowidgetParms = new
		// FrameLayout.LayoutParams(LayoutParams.FILL_PARENT,mGowidgetHeight);
		mContainer = (ScreenEditBoxContainer) findViewById(R.id.container);
		// mContainerForGoWidgets.setLayoutParams(gowidgetParms);

		mContainer.setIndicatorUpdateListner(this);

		mIndicator = (DesktopIndicator) findViewById(R.id.edit_indicator);
		mIndicator.setDefaultDotsIndicatorImage(R.drawable.screen_edit_indicator_cur,
				R.drawable.screen_edit_indicator_other);
		mIndicator.setIndicatorListner(this);

		mTabApp = (TextView) findViewById(R.id.tab1);
		mTabWallpapers = (TextView) findViewById(R.id.tab2);
		mTabThemes = (TextView) findViewById(R.id.tab3);
		mTabEffects = (TextView) findViewById(R.id.tab4);

		mTabApp.setOnClickListener(this);
		mTabThemes.setOnClickListener(this);
		mTabWallpapers.setOnClickListener(this);
		mTabEffects.setOnClickListener(this);

		mTabApp.setBackgroundResource(R.drawable.screen_edit_tab_selector);
		mTabThemes.setBackgroundResource(R.drawable.screen_edit_tab_selector);
		mTabWallpapers.setBackgroundResource(R.drawable.screen_edit_tab_selector);
		mTabEffects.setBackgroundResource(R.drawable.screen_edit_tab_selector);

		// 不允许指示器空白处的touch事件向下传递
		mIndicatorLayout = (RelativeLayout) findViewById(R.id.indicator_layout);
		mIndicatorLayout.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});
		iniBackTab();
	}
  // 设置当前对应tab
	@Override
	public void setCurrentTab(String tabs) {
		if (mDataEdngine == null) {
			return;
		}
		BaseTab tab = mDataEdngine.getTab(tabs);
		if (tab == null) {
			return;
		}
		if (tab.mTabActionListener == null) {
			tab.setTabActionListener(this);
		}
		mCurTabTag = tab.getTag();

		if (mContainer != null) {
			mContainer.setVisibility(VISIBLE);
			mContainer.setCurrentTab(tab);
			mContainer.requestLayout();
			mIndicator.setCurrent(0);
			mIndicator.setTotal(mContainer.getPageCount());
			updateTabLine();
		}
	}

	public void setCurrentBackTab(BaseTab tab) {
		if (tab == null) {
			return;
		}
		//go 小部件假图标跳进来mTabActionListener可能为空
		if (tab.mTabActionListener == null) {
			tab.setTabActionListener(this);
		}
		if (mDataEdngine == null) {
			return;
		}
		mCurTabTag = tab.getTag();
		if (mCurTabTag.equals(BaseTab.TAB_APP) || mCurTabTag.equals(BaseTab.TAB_THEME)
				|| mCurTabTag.equals(BaseTab.TAB_GOWIDGET)) {
			mEditLayout.setContainerMode(0);
		}

		if (mContainer != null) {
			mContainer.setVisibility(VISIBLE);
			if (mCurTabTag.equals(BaseTab.TAB_GOWIDGET)) {
				// 清空 widget预览列表的数据
				if (mDataEdngine.obtainTab(BaseTab.TAB_ADDGOWIDGET) != null) {
					WidgetSubTab tab1 = (WidgetSubTab) mDataEdngine
							.obtainTab(BaseTab.TAB_ADDGOWIDGET);
					tab1.clearData();
					mDataEdngine.removeData(BaseTab.TAB_ADDGOWIDGET);
					tab1 = null;
				}
				refreshBackTab(BaseTab.TAB_GOWIDGET);
			}
			// 清空 应用程序列表数据
			else if (mDataEdngine.obtainTab(BaseTab.TAB_ADDAPPS) != null) {

				((AddAppTab) mDataEdngine.getTab(BaseTab.TAB_ADDAPPS)).clearData();
				mDataEdngine.removeData(BaseTab.TAB_ADDAPPS);
			}
			// 清空 文件夹列表数据
			else if (mDataEdngine.obtainTab(BaseTab.TAB_ADDFOLDER) != null) {
				((AddFolderTab) mDataEdngine.getTab(BaseTab.TAB_ADDFOLDER)).clearData();
				mDataEdngine.removeData(BaseTab.TAB_ADDFOLDER);
			}
			mContainer.setCurrentTab(tab);
			mContainer.requestLayout();
			mIndicator.setCurrent(0);
			mIndicator.setTotal(mContainer.getPageCount());
			updateTabLine();
		}

	}
	// 更新tab标题栏选取状态显示
	private void updateTabLine() {
		if (mCurTabTag.equals(BaseTab.TAB_APP)) {
			mTabApp.setTextColor(PRESS_COLOR);
			mTabThemes.setTextColor(UNPRESS_COLOR);
			mTabWallpapers.setTextColor(UNPRESS_COLOR);
			mTabEffects.setTextColor(UNPRESS_COLOR);
		} else if (mCurTabTag.equals(BaseTab.TAB_THEMELOCKER)) {
			mTabApp.setTextColor(UNPRESS_COLOR);
			mTabThemes.setTextColor(PRESS_COLOR);
			mTabWallpapers.setTextColor(UNPRESS_COLOR);
			mTabEffects.setTextColor(UNPRESS_COLOR);
		} else if (mCurTabTag.equals(BaseTab.TAB_WALLPAPER)) {
			mTabApp.setTextColor(UNPRESS_COLOR);
			mTabThemes.setTextColor(UNPRESS_COLOR);
			mTabWallpapers.setTextColor(PRESS_COLOR);
			mTabEffects.setTextColor(UNPRESS_COLOR);
		} else if (mCurTabTag.equals(BaseTab.TAB_EFFECTS)) {
			mTabApp.setTextColor(UNPRESS_COLOR);
			mTabThemes.setTextColor(UNPRESS_COLOR);
			mTabWallpapers.setTextColor(UNPRESS_COLOR);
			mTabEffects.setTextColor(PRESS_COLOR);
		}
	}

	@Override
	public void updateIndicator(int num, int current) {
		if (num >= 0 && current >= 0 && current < num) {
			mIndicator.setTotal(num);
			mIndicator.setCurrent(current);
		}
	}

	@Override
	public void clickIndicatorItem(int index) {
		if (mEditLayout.getContainerMode() == 0) {
			if (mContainer != null) {
				mContainer.snapToScreen(index, false, -1);
			}
		}
	}

	@Override
	public void sliding(float percent) {
		if (0 <= percent && percent <= 100) {
			if (mEditLayout.getContainerMode() == ScreenEditLayout.NORMALLAYOUT) {
				if (mContainer != null) {
					mContainer.getScreenScroller().setScrollPercent(percent);
				}
			}

		}
	}

	@Override
	public void onScrollChanged(int offset) {
		Bundle dataBundle = new Bundle();
		dataBundle.putInt(DesktopIndicator.OFFSET, offset);
		if (mIndicator != null) {
			mIndicator.updateIndicator(DesktopIndicator.UPDATE_SLIDER_INDICATOR, dataBundle);
		}
	}

	@Override
	public void onScreenChanged(int newScreen) {
		Bundle dataBundle = new Bundle();
		dataBundle.putInt(DesktopIndicator.CURRENT, newScreen);
		if (mIndicator != null) {
			mIndicator.updateIndicator(DesktopIndicator.UPDATE_DOTS_INDICATOR, dataBundle);
		}
	}

	public String getCurTabTag() {
		return mCurTabTag;
	}
	public void selfDestruct() {
		if (mOnRespondTouch != null) {
			mOnRespondTouch = null;
		}
		if (mContainer != null) {	
			//释放字体
			mContainer.unInitFront();
			mContainer.unInitImage();
			mContainer.removeAllViews();
			mContainer = null;
		}
		if (mIndicator != null) {
			int count = mIndicator.getChildCount();
			for (int j = 0; j < count; j++) {
				ViewGroup v1 = (ViewGroup) mIndicator.getChildAt(j);
				for (int i = 0; i < v1.getChildCount(); i++) {
					View v2 = v1.getChildAt(i);
					if (v2 != null && v2 instanceof ImageView) {
						((ImageView) v2).setImageDrawable(null);
						v2 = null;
					}
				}
			}
			mIndicator.removeAllViews();
			mIndicator = null;
		}
		if (mDataEdngine != null) {
			mDataEdngine.clearData();
			mDataEdngine = null;
		}
		
		if (mTabApp != null && mTabApp instanceof DeskTextView) {
			((DeskTextView) mTabApp).selfDestruct();
			mTabApp.setBackgroundDrawable(null);
			mTabApp = null;
		}
		if (mTabThemes != null && mTabThemes instanceof DeskTextView) {
			((DeskTextView) mTabThemes).selfDestruct();
			mTabThemes.setBackgroundDrawable(null);
			mTabThemes = null;
		}
		if (mTabWallpapers != null && mTabWallpapers instanceof DeskTextView) {
			((DeskTextView) mTabWallpapers).selfDestruct();
			mTabWallpapers.setBackgroundDrawable(null);
			mTabWallpapers = null;
		}
		if (mTabEffects != null && mTabEffects instanceof DeskTextView) {
			((DeskTextView) mTabEffects).selfDestruct();
			mTabEffects.setBackgroundDrawable(null);
			mTabEffects = null;
		}
		if (mBackTabText != null && mBackTabText instanceof DeskTextView) {
			((DeskTextView) mBackTabText).selfDestruct();
			mBackTabText.setBackgroundDrawable(null);
			mBackTabText = null;
		}
		if (mBackImg != null) {
			mBackImg.setBackgroundDrawable(null);
			mBackImg = null;
		}
		if (mBackIcon != null) {
			mBackIcon.setBackgroundDrawable(null);
			mBackIcon = null;
		}
		// 添加界面退出，cancel异步加载线程和删除缓存
		DrawableLoadTasker.getInstance().cancel(true);
		DrawableLoadTasker.clear();
		DrawableCacheManager.getInstance().clearCache();
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		if (mOnRespondTouch != null) {
			return !mOnRespondTouch.isRespondTouch();
		} else {
			return super.onInterceptTouchEvent(event);
		}
	}

	public LinearLayout getTabs() {
		return mTabs;
	}

	public void setTabs(LinearLayout tabs) {
		mTabs = tabs;
	}

	/**
	 * 根据tab名称，返回一级目录
	 */
	public void backToFirstLevel(String tag) {
		if (mTabs != null && mBackTab != null) {
			mTabs.setVisibility(View.VISIBLE);
			mBackTab.setVisibility(View.GONE);
			BaseTab tab = mDataEdngine.getBackTab(tag);
			setCurrentBackTab(tab);
		}

	}

	/**
	 * 初始化二级页面上半部分的返回条
	 */
	private void iniBackTab() {
		mBackTab = (LinearLayout) findViewById(R.id.tabs_back);
		mBackImg = (ImageView) mBackTab.findViewById(R.id.tabs_back_img);
		mBackTab.setBackgroundResource(R.drawable.screen_edit_tab_selector);
		mBackTab.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					mBackImg.setBackgroundResource(R.drawable.screen_edit_tab_top_back_light);
				} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
					if (!mBackTab.isPressed()) {
						mBackImg.setBackgroundResource(R.drawable.screen_edit_tab_top_back);
					}
				}
				return false;
			}
		});
		mBackTab.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					mBackImg.setBackgroundResource(R.drawable.screen_edit_tab_top_back);
					// 离开文件夹新建页面
					GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
							IDiyMsgIds.LEAVE_NEW_FOLDER_STATE, -1, null, null);
					final int tabLevel = getCurTabLevel(getCurTabTag());
					if (tabLevel == BaseTab.TAB_LEVEL_3) {
						ScreenEditBoxFrame screenEditBoxFrame = (ScreenEditBoxFrame) GoLauncher
								.getFrame(IDiyFrameIds.SCREEN_EDIT_BOX_FRAME);
						if (screenEditBoxFrame != null) {
							ScreenEditLayout mLayView = (ScreenEditLayout) screenEditBoxFrame
									.getContentView();
							if (!mLayView.mIsAnimation) {
								// 如果还在动画过程中就不触发
								mLayView.changesizeForNormal();
							}
						}
					} else {
						backToFirstLevel(getCurTabTag());
					}
				} catch (Exception e) {
					//异常情况下，安全退出添加页面
					GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
							IDiyMsgIds.SCREENEDIT_CLOSE_SCREEN_EDIT_LAYOUT, 1, null, null);
					GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_EDIT_BOX_FRAME,
							IDiyMsgIds.SCREENEDIT_SHOW_ANIMATION_OUT, 0, null, null);
				}
			}
		});
	}

	/**
	 * 根据tab名称，刷新二级页面上半部分的显示
	 */
	public void refreshBackTab(final String tabname) {
		/*
		 * //清空 应用程序列表数据 if(mDataEdngine.getTab(BaseTab.TAB_ADDAPPS)!=null){
		 * ((AddAppTab)mDataEdngine.getTab(BaseTab.TAB_ADDAPPS)).clearData();
		 * mDataEdngine.removeData(BaseTab.TAB_ADDAPPS); }//清空 文件夹列表数据 else
		 * if(mDataEdngine.getTab(BaseTab.TAB_ADDFOLDER)!=null){
		 * ((AddFolderTab)mDataEdngine
		 * .getTab(BaseTab.TAB_ADDFOLDER)).clearData();
		 * mDataEdngine.removeData(BaseTab.TAB_ADDFOLDER); }
		 */
		mTabs.setVisibility(View.GONE);
		// 设置返回Tab为可见
		mBackTab.setVisibility(View.VISIBLE);
		mBackIcon = (ImageView) mBackTab.findViewById(R.id.tabs_back_icon);
		mBackTabText = (TextView) mBackTab.findViewById(R.id.tabs_back_text);
		if (tabname.equals(BaseTab.TAB_GOWIDGET)) {
			mBackTabText.setText(mContext.getString(R.string.tab_add_widget));
			mBackIcon.setBackgroundResource(R.drawable.tab_add_widget_icon);
		} else if (tabname.equals(BaseTab.TAB_THEME)) {
			mBackTabText.setText(mContext.getString(R.string.tab_add_visual_theme));
			mBackIcon.setBackgroundResource(R.drawable.change_theme_4_def3);
		} else if (tabname.equals(BaseTab.TAB_LOCKER)) {
			mBackTabText.setText(mContext.getString(R.string.tab_add_visual_locker));
			mBackIcon.setBackgroundResource(R.drawable.screen_edit_golocker);
		} else if (tabname.equals(BaseTab.TAB_GOWALLPAPER)) {
			mBackTabText.setText(mContext.getString(R.string.go_wallpaper));
			mBackIcon.setBackgroundResource(R.drawable.gowallpaper_logo);

		}

	}

	@Override
	public void onTabClick(String tag) {
		setCurrentTab(tag);

	}

	public void setTap(String tag) {
		if (mDataEdngine == null) {
			return;
		}
		setCurrentTab(tag);
	}

	/**
	 * 刷新指定tab的内容
	 * 
	 * @param tag
	 */
	public void updateTab(String tag) {
		if (mDataEdngine == null) {
			return;
		}
		mDataEdngine.updateTab(tag);
	}

	public int getCurTabLevel(String tabTag) {
		return mDataEdngine.getTabLevel(tabTag);
	}

	@Override
	public void onRefreshTab(String tabName, int index) {
		if (tabName != null && mCurTabTag.equals(tabName)) {
			setCurrentTab(tabName);
			// 计算第几页
			if (mItemsCount != 0) {
				getContainer().getScreenScroller().setCurrentScreen(index / mItemsCount);
			}
		}
	}

	@Override
	public void onRefreshTopBack(String tabName) {
		if (tabName != null) {
			refreshBackTab(tabName);
		}
	}

	public DataEdngine getDataEdngine() {
		return mDataEdngine;
	}

	public void setDataEdngine(DataEdngine mDataEdngine) {
		this.mDataEdngine = mDataEdngine;
	}

}
