package com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.gau.go.launcherex.R;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenIndicator;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.Workspace;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.AddAppTab;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.AddFolderTab;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.AddGoShortCutTab;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.BaseTab;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.WidgetSubTab;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.data.info.ScreenSettingInfo;
import com.jiubang.ggheart.launcher.GOLauncherApp;
/**
 * 
 * <br>类描述:添加模块布局
 * <br>功能详细描述:设置子view的位置，大小，判断子view的状态，根据子view的状态来设置动画，并调用子view的方法刷新标题部分
 * 
 * @date  [2012-9-10]
 */
public class ScreenEditLayout extends LinearLayout implements AnimationListener {
	// 进出动画持续时间
	private static final int IN_OUT_DURATION_TIME = 300;
	private ScreenEditTabView mTabView;
	private ScreenEditLargeTabView mLargeTabView;
	public boolean mIsLargeShowing = false; //此变量用作返回键处理
	private int mAnimationType;
	private int mAnimationTypeLarge;
	public int mDetalHeight = 0; // 下移动画的移动高度
	public int mContainerMode = 0; // 默认第一次为普通容器的高度

	private boolean mIndicatorOnBottom = false; // 指示器在屏幕底部
	
	private String mCurrentLargeTab; //当前变高模式下的tab名称

	public static final int NORMALLAYOUT = 0; // 普通容器的高度
	public static final int APPSLAYOUT = 1; // 应用程序容器的高度
	public static final int GOWIDGETLAYOUT = 2; // Go小部件容器的高度
	public static final int PRE_APPSLAYOUT = 3; // 应用程序容器的高度（在变为
												// APPSLAYOUT前将高度拉高）
	public static final int PRE_GOWIDGETLAYOUT = 4; // Go小部件容器的高度（在变为
													// GOWIDGETLAYOUT前将高度拉高）

	public int mLayoutModeForNormal = 0; // Normal的高度
	public int mLayoutModeForLarge = 0; // Large的高度

	public boolean mIsAnimation = false; // 当前动画状态
	private int mNormalHeight;
	private int mAppHeight;

	public int getmAppHeight() {
		return mAppHeight;
	}

	private int mWidgetHeight;
	private Context mContext;
	//	private static final float NORMALSCALE = 0.243f;
	public static final float APPSCALE = 0.525f;          //App高度
	public static final float GOWIDGETSCALE = 0.425f;    //动态适配时widgettab高度

	public boolean mIsExiting;

	public boolean isIsExiting() {
		return mIsExiting;
	}

	public void setIsExiting(boolean isExiting) {
		this.mIsExiting = isExiting;
	}
	public int mLayoutL; // 详情组件四边与屏幕的边距
	public int mLayoutR;
	public int mLayoutT;
	public int mLayoutB;
	public ScreenEditLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mNormalHeight = (int) context.getResources().getDimension(
				R.dimen.screen_edit_box_container_normal);
		// mAppHeight = (int)
		// context.getResources().getDimension(R.dimen.screen_edit_box_container_app);
		// Add by xiangliang   如果屏幕分辨率小于800，则根据比例来计算layout高度
		if (DrawUtils.sHeightPixels < 800) {
			mWidgetHeight = (int) (DrawUtils.sHeightPixels * GOWIDGETSCALE);
		} else {
			mWidgetHeight = (int) context.getResources().getDimension(
					R.dimen.screen_edit_box_container_gowidgets);
		}
		// (mNormalHeight + 0.1f) / 800 ;
		// 0.243 0.425 0.525
		// mNormalHeight = (int) (DrawUtils.sHeightPixels * NORMALSCALE);
		mAppHeight = (int) (DrawUtils.sHeightPixels * APPSCALE);
		mIsExiting = false;
		
		GoSettingControler controler = GOLauncherApp.getSettingControler();
		ScreenSettingInfo mScreenInfo = controler.getScreenSettingInfo();
		mIndicatorOnBottom = mScreenInfo.mIndicatorPosition
				.equals(ScreenIndicator.INDICRATOR_ON_BOTTOM);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		mLayoutL = l;
		mLayoutR = r;
		mLayoutT = t;
		mLayoutB = b;

		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			View childView = getChildAt(i);
			if (childView instanceof ScreenEditTabView) {
				if (mLayoutModeForNormal == 1) {
					childView.layout(l, b, r, b + mNormalHeight);
				} else {
					childView.layout(l, b - mNormalHeight, r, b);
				}
			}

			if (childView instanceof ScreenEditLargeTabView) {
				if (mLayoutModeForLarge == 1) {
					// 应用程序
					childView.layout(l, b - mAppHeight, r, b);
				} else if (mLayoutModeForLarge == 2) {
					// Go小部件
					childView.layout(l, b - mWidgetHeight, r, b);
				} else {
					// 正常情况
					childView.layout(l, b, r, b + mAppHeight);
				}
			}
		}
	}

	@Override
	public void onAnimationStart(Animation animation) {
		mIsAnimation = true;
		// 动画结束 指示器不可见
		mTabView.findViewById(R.id.indicator_layout).setVisibility(INVISIBLE);
		mLargeTabView.findViewById(R.id.indicator_layout_large).setVisibility(INVISIBLE);
	}
	// 动画完成时，清空之前tab数据，刷新当前tab
	@Override
	public void onAnimationEnd(Animation animation) {
		try {
			TranslateAnimation anim = new TranslateAnimation(0, 0, 0, 0);
			anim.setDuration(0);
			mTabView.startAnimation(anim);
			mLargeTabView.startAnimation(anim);
			// 变到应用程序
			if (ANIMATION_ADDAPP_UP_LARGE == mAnimationTypeLarge) {
//				mContainerMode = APPSLAYOUT;
				mLayoutModeForNormal = 1;
				mAnimationType = 0;
				mLayoutModeForLarge = 1;
				mLargeTabView.setCurrentTab(mCurrentLargeTab);
				mCurrentLargeTab = "";
				mAnimationTypeLarge = 0;
			} // 普通模式
			else if (ANIMATION_NORMAL_DOWN == mAnimationTypeLarge) {
				BaseTab tab = mLargeTabView.getDataEdngine().getTab(mLargeTabView.getCurTabTag());
				mLargeTabView.getContainer().unInitAppFont();
				mLargeTabView.getContainer().unInitImage();
				mLargeTabView.getGowidgetContainer().unInitWidgetFont();
				mLargeTabView.getGowidgetContainer().unInitImage();
				
				if (tab != null && tab instanceof WidgetSubTab) {
					mLargeTabView.getGowidgetContainer().removeAllViews();
					// 清空 widget预览列表的数据
					tab.clearData();
					mLargeTabView.getDataEdngine().removeData(BaseTab.TAB_ADDGOWIDGET);
					tab = null;
				} else if (tab != null && tab instanceof AddAppTab) {
					mLargeTabView.getContainer().removeAllViews();
					tab.clearData();
					mLargeTabView.getDataEdngine().removeData(BaseTab.TAB_ADDAPPS);
					tab = null;
				} else if (tab != null && tab instanceof AddFolderTab) {
					mLargeTabView.getContainer().removeAllViews();
					tab.clearData();
					mLargeTabView.getDataEdngine().removeData(BaseTab.TAB_ADDFOLDER);
					tab = null;
				} else if (tab != null && tab instanceof AddGoShortCutTab) {
					mLargeTabView.getContainer().removeAllViews();
					tab.clearData();
					mLargeTabView.getDataEdngine().removeData(BaseTab.TAB_ADDGOSHORTCUT);
					tab = null;
				}
//				mContainerMode = APPSLAYOUT;
				mLayoutModeForNormal = 0;
				mAnimationType = 0;
				requestLayout();
				mLayoutModeForLarge = 0;
				mAnimationTypeLarge = 0;
			}
			// GO小部件
			else if (ANIMATION_ADDGOWIDGET_UP == mAnimationTypeLarge) {
//				mContainerMode = GOWIDGETLAYOUT;
				mLayoutModeForNormal = 1; //消失在下面
				mAnimationType = 0;
				mLayoutModeForLarge = 2;
				mLargeTabView.refreshBackTab(BaseTab.TAB_ADDGOWIDGET);
				mLargeTabView.setCurrentTab(BaseTab.TAB_ADDGOWIDGET);
			}
			mIsAnimation = false;

			// 动画结束 指示器显示
			mTabView.findViewById(R.id.indicator_layout).setVisibility(VISIBLE);
		} catch (Exception e) {
			//异常情况下，安全退出添加页面
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
					IDiyMsgIds.SCREENEDIT_CLOSE_SCREEN_EDIT_LAYOUT, 1, null, null);
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_EDIT_BOX_FRAME,
					IDiyMsgIds.SCREENEDIT_SHOW_ANIMATION_OUT, 0, null, null);
		}

	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		mIsAnimation = true;
	}

	@Override
	public void removeView(View view) {

		super.removeView(view);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		if (mIsExiting) {
			return true;
		}
		return false;
	}

	public boolean mIsExitIng = false;
	private final static int ANIMATION_ADDAPP_UP_LARGE = 1000; // 应用程序添加向上动画
	private final static int ANIMATION_NORMAL_DOWN = 2000; // 恢复为普通高度向下动画
	private final static int ANIMATION_ADDGOWIDGET_UP = 3000; // go小部件添加向上动画

	// 切换为应用程序添加
	public void changesizeForApp(String tabs) {
		// add by chenbingdong
		// 因为界面缩放，会涉及到壁纸的滑动，影响多屏多壁纸，因此发送广播调整
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
				IDiyMsgIds.SCREEN_SEND_BROADCASTTO_MULTIPLEWALLPAPER, -1, null,
				null);
		
		mCurrentLargeTab = tabs;
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.SCREENEDIT_SCREEN_ZOOM,
				-1, Workspace.SCALE_FACTOR_FOR_ADD_APP_PORTRAIT, null);
		AnimationSet animationSet = new AnimationSet(true);
		AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
		alphaAnimation.setDuration(IN_OUT_DURATION_TIME);
		alphaAnimation.setFillEnabled(true);
		alphaAnimation.setFillAfter(true);
		TranslateAnimation animation = new TranslateAnimation(0, 0, 0, -mAppHeight);
		animation.setDuration(IN_OUT_DURATION_TIME);
		//		animation.setInterpolator(new AccelerateInterpolator());
		animation.setFillEnabled(true);
		animation.setFillAfter(true);
		animationSet.addAnimation(animation);
		animationSet.addAnimation(alphaAnimation);
		animationSet.setAnimationListener(this);
		mAnimationTypeLarge = ANIMATION_ADDAPP_UP_LARGE;
		mLargeTabView.startAnimation(animationSet);

		mLargeTabView.refreshBackTab(mCurrentLargeTab);  // 刷新二级页面title部分
		AlphaAnimation alpha = new AlphaAnimation(0.8f, 0.1f);
		alpha.setDuration(IN_OUT_DURATION_TIME / 2);
		alpha.setFillEnabled(true);
		alpha.setFillAfter(true);
		mTabView.startAnimation(alpha);
		mIsLargeShowing = true;

		FrameLayout.LayoutParams appParms = new FrameLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT, mAppHeight - DrawUtils.dip2px(50));
		mLargeTabView.findViewById(R.id.edit_tab_progress).setLayoutParams(appParms);
		mContainerMode = APPSLAYOUT;
		startIndicatorAnimation(mAppHeight, true);
	}

	// 切换为go小部件添加
	public void changesizeForWidget() {
		// add by chenbingdong
		// 因为界面缩放，会涉及到壁纸的滑动，影响多屏多壁纸，因此发送广播调整
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
				IDiyMsgIds.SCREEN_SEND_BROADCASTTO_MULTIPLEWALLPAPER, -1, null,
				null);
		
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.SCREENEDIT_SCREEN_ZOOM,
				-1, Workspace.SCALE_FACTOR_FOR_ADD_GOWIDGET_PORTRAIT, null);
		AnimationSet animationSet = new AnimationSet(true);
		TranslateAnimation animation = new TranslateAnimation(0, 0, 0, -mWidgetHeight);
		animation.setDuration(IN_OUT_DURATION_TIME);
		animation.setInterpolator(new DecelerateInterpolator());
		animation.setFillEnabled(true);
		animation.setFillAfter(true);
		AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
		alphaAnimation.setDuration(IN_OUT_DURATION_TIME);
		alphaAnimation.setFillEnabled(true);
		alphaAnimation.setFillAfter(true);
		animationSet.addAnimation(animation);
		animationSet.addAnimation(alphaAnimation);
		animationSet.setAnimationListener(this);
		mAnimationTypeLarge = ANIMATION_ADDGOWIDGET_UP;
		mLargeTabView.refreshBackTab(BaseTab.TAB_ADDGOWIDGET); // 刷新二级页面title部分
		mLargeTabView.startAnimation(animationSet);

		AlphaAnimation alpha = new AlphaAnimation(0.8f, 0.1f);
		alpha.setDuration(IN_OUT_DURATION_TIME / 2);
		//		alpha.setInterpolator(new DecelerateInterpolator());
		alpha.setFillEnabled(true);
		alpha.setFillAfter(true);
		mTabView.startAnimation(alpha);
		mIsLargeShowing = true;

		FrameLayout.LayoutParams appParms = new FrameLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT, mWidgetHeight
						- DrawUtils.dip2px(50));
		mLargeTabView.findViewById(R.id.edit_tab_progress).setLayoutParams(appParms);
		mContainerMode = GOWIDGETLAYOUT;
		startIndicatorAnimation(mWidgetHeight, true);
	}

	// 切换为普通高度状态
	public void changesizeForNormal() {
		// add by chenbingdong
		// 因为界面缩放，会涉及到壁纸的滑动，影响多屏多壁纸，因此发送广播调整
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
				IDiyMsgIds.SCREEN_SEND_BROADCASTTO_MULTIPLEWALLPAPER, -1, null,
				null);
		
		mTabView.findViewById(R.id.indicator_layout).setVisibility(VISIBLE);
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.SCREENEDIT_SCREEN_ZOOM,
				-1, Workspace.SCALE_FACTOR_FOR_EDIT_PORTRAIT, null);
		int dis = 0;
		String tab = mLargeTabView.getCurTabTag();
		if (tab != null) {
			if (tab.equals(BaseTab.TAB_ADDGOWIDGET)) {
				dis = mWidgetHeight - mNormalHeight;
			} else {
				dis = mAppHeight - mNormalHeight;
			}
		}

		TranslateAnimation animation = new TranslateAnimation(0, 0, 0, dis);
		animation.setDuration(IN_OUT_DURATION_TIME);
		//		animation.setInterpolator(new DecelerateInterpolator());
		animation.setFillEnabled(true);
		animation.setFillAfter(true);
		animation.setAnimationListener(this);
		mAnimationTypeLarge = ANIMATION_NORMAL_DOWN;

		AnimationSet animationSet = new AnimationSet(true);
		AlphaAnimation alphaAnimation = new AlphaAnimation(0.8f, 0);
		alphaAnimation.setDuration(IN_OUT_DURATION_TIME);
		alphaAnimation.setFillEnabled(true);
		alphaAnimation.setFillAfter(true);
		animationSet.addAnimation(animation);
		animationSet.addAnimation(alphaAnimation);
		mLargeTabView.startAnimation(animationSet);

		AlphaAnimation alpha = new AlphaAnimation(0, 1);
		alpha.setDuration(IN_OUT_DURATION_TIME);
		alpha.setFillEnabled(true);
		alpha.setFillAfter(true);
		mTabView.setVisibility(VISIBLE);
		mTabView.layout(mLayoutL, mLayoutB - mNormalHeight, mLayoutR, mLayoutB);
		mTabView.startAnimation(alpha);
		mContainerMode = NORMALLAYOUT;
		mIsLargeShowing = false;
		startIndicatorAnimation(dis, false);
	}

	public void startIndicatorAnimation(int containerHeight, boolean isUp) {
		if (mIndicatorOnBottom) {
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
					IDiyMsgIds.START_INDICATOR_ANIMATION_FOR_SCREEN_EDIT, containerHeight, isUp,
					null);
		}
	}
	
	public ScreenEditTabView getTabView() {
		return mTabView;
	}
	public void setTabView(ScreenEditTabView mTabView) {
		this.mTabView = mTabView;
	}
	public ScreenEditLargeTabView getLargeTabView() {
		return mLargeTabView;
	}
	public void setLargeTabView(ScreenEditLargeTabView mLargeTabView) {
		this.mLargeTabView = mLargeTabView;
	}
	public int getContainerMode() {
		return mContainerMode;
	}
	public void setContainerMode(int mContainerMode) {
		this.mContainerMode = mContainerMode;
	}
}
