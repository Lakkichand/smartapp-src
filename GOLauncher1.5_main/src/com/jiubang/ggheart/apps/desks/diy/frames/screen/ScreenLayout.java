package com.jiubang.ggheart.apps.desks.diy.frames.screen;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;

import com.gau.go.launcherex.R;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditBoxFrame;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditLayout;
import com.jiubang.ggheart.apps.desks.dock.DockUtil;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.data.info.ScreenSettingInfo;
import com.jiubang.ggheart.data.info.ShortCutSettingInfo;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.screen.back.BackWorkspace;

/**
 * 
 * @author luopeihuan
 * 
 */
public class ScreenLayout extends FrameLayout implements IndicatorListner, AnimationListener {
	private BackWorkspace mBackWorkspace;
	private Workspace mWorkspace;
	private DesktopIndicator mIndicator;
	private boolean mIndicatorOnBottom = false; // 指示器在屏幕底部
	private boolean mIndicatorOnTop = false; // 指示器在屏幕底部
	private boolean mIsScreenEditState = false; // 是否是屏幕编辑中
	private boolean mDockVisible = true; // Dock的可见标识

	/**
	 * 
	 * @param c
	 *            Context
	 */
	public ScreenLayout(Context c) {
		this(c, null);
	}

	/**
	 * 
	 * @param c
	 *            context
	 * @param att
	 *            xml属性
	 */
	public ScreenLayout(Context c, AttributeSet att) {
		super(c, att);
	}

	/**
	 * 
	 * @return Workspace
	 */
	public Workspace getWorkspace() {
		return mWorkspace;
	}

	/**
	 * 
	 * @return mBackWorkspace
	 */
	public BackWorkspace getBackWorkspace() {
		return mBackWorkspace;
	}
	
	/**
	 * 设置workspace
	 * 
	 * @param workspace
	 *            workspace
	 */
	public void setWorkspace(Workspace workspace) {
		mWorkspace = workspace;
	}

	/**
	 * 设置屏幕指示器
	 * 
	 * @param indicator
	 *            指示器
	 */
	public void setIndicator(DesktopIndicator indicator) {
		mIndicator = indicator;
	}

	/**
	 * 
	 * @return 屏幕指示器
	 */
	public DesktopIndicator getIndicator() {
		return mIndicator;
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mIndicator = (DesktopIndicator) findViewById(R.id.desktop_indicator);
		mIndicator.setIndicatorListner(this);
		GoSettingControler controler = GOLauncherApp.getSettingControler();
		ScreenSettingInfo mScreenInfo = controler.getScreenSettingInfo();
		mIndicatorOnBottom = mScreenInfo.mIndicatorPosition
				.equals(ScreenIndicator.INDICRATOR_ON_BOTTOM);
		mIndicatorOnTop = mScreenInfo.mIndicatorPosition
				.equals(ScreenIndicator.INDICRATOR_ON_TOP);
		mWorkspace = (Workspace) findViewById(R.id.diyWorkspace);
		mBackWorkspace = (BackWorkspace) findViewById(R.id.backWorkspace);
		// 先初始化的dock数据
		controler.getShortCutSettingInfo();
		mDockVisible = ShortCutSettingInfo.sEnable;
	}

	@Override
	public void clickIndicatorItem(int index) {
		if (null != mWorkspace && index < mWorkspace.getChildCount() && mIndicator.getVisible()) {
			mWorkspace.snapToScreen(index, false, -1);
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		int indicatorHeight = getResources().getDimensionPixelSize(R.dimen.slider_indicator_height);
		if (mIndicatorOnBottom) {
			if (GoLauncher.getOrientation() == Configuration.ORIENTATION_PORTRAIT
			/* && GoLauncher.isLargeIcon() */) {
				if (mIsScreenEditState) {
					int screenEditBoxHeight = (int) getContext().getResources().getDimension(
							R.dimen.screen_edit_box_container_normal);
					int containerMode[] = new int[] { ScreenEditLayout.NORMALLAYOUT };
					GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_EDIT_BOX_FRAME,
							IDiyMsgIds.GET_SCREEN_EDIT_BOX_CONTAINER_MODE, -1, containerMode, null);
					if (containerMode[0] == ScreenEditLayout.GOWIDGETLAYOUT) {
						if (DrawUtils.sHeightPixels < 800) {
							screenEditBoxHeight = (int) (DrawUtils.sHeightPixels * ScreenEditLayout.GOWIDGETSCALE);
						} else {
							screenEditBoxHeight = (int) getContext().getResources().getDimension(
									R.dimen.screen_edit_box_container_gowidgets);
						}
					} else if (containerMode[0] == ScreenEditLayout.APPSLAYOUT) {
						screenEditBoxHeight = (int) (DrawUtils.sHeightPixels * ScreenEditLayout.APPSCALE);
					}
					//计算编辑容器的上边
					final int b = (int) (bottom - screenEditBoxHeight);
					//这个是我们看到的celllayout的下边（已经缩放了）
					//计算方法为celllayout的可见高：（bottom-top）减celllayout上下边距再乘当前缩放比率
					//最后加上workspace上边距sPageSpacingY
					int t = (int) (((bottom - top - CellLayout.getTopPadding() - CellLayout
							.getBottomPadding()) * Workspace.sLayoutScale) + Workspace.sPageSpacingY);
					//计算指示器的top顶点：celllayout的可见下边 + celllayout的可见下边到编辑容器的上便的距离减去指示器高度再平分
					t = t + (b - t - indicatorHeight) / 2;
					mIndicator.layout(0, t, right, t + indicatorHeight);
				} else {
					final int b = mDockVisible ? bottom - DockUtil.getBgHeight() : bottom; // activity.getResources().getDimensionPixelSize(R.dimen.dock_bg_large_height
																							// );
					mIndicator.layout(0, b - indicatorHeight + DrawUtils.dip2px(3.0f), right, b);
				}
			} else {
				mIndicator.layout(0, bottom - indicatorHeight + DrawUtils.dip2px(3.0f), right,
						bottom);
			}
		} else if (mIndicatorOnTop) {
			if (mIsScreenEditState) {
				int t = (Workspace.sPageSpacingY - indicatorHeight + CellLayout.getTopPadding()) / 2;
				mIndicator.layout(0, t, right, t + indicatorHeight);
			}
		}
//		top = (int) (mWorkspace.sLayoutScale < 1.0f
//				&& WindowControl.getIsFullScreen(GoLauncher.getContext()) ? StatusBarHandler
//				.getStatusbarHeight() : 0);
//		mWorkspace.onLayout(changed, left, top, right, bottom);
	}

	@Override
	public void sliding(float percent) {
		if (0 <= percent && percent <= 100 && mIndicator.getVisible()) {
			mWorkspace.getScreenScroller().setScrollPercent(percent);
		}
	}

	// @Deprecated
	// public void hideIndicator()
	// {
	// mIndicator.setVisible(false);
	// mIndicator.setVisibility(View.INVISIBLE);
	// }
	//
	// @Deprecated
	// public void showIndicator()
	// {
	// mIndicator.setVisible(true);
	// mIndicator.show();
	// }

	public void setIndicatorOnBottom(boolean yes) {
		mIndicatorOnBottom = yes;
	}

	/**
	 * 指示器是否在底部
	 * 
	 * @return
	 */
	public boolean isIndicatorOnBottom() {
		return mIndicatorOnBottom;
	}

	public boolean isScreenEditState() {
		return mIsScreenEditState;
	}

	public void setScreenEditState(boolean isScreenEditState, boolean animation) {
		GoSettingControler controler = GOLauncherApp.getSettingControler();
		ScreenSettingInfo mScreenInfo = controler.getScreenSettingInfo();
		mIndicatorOnBottom = mScreenInfo.mIndicatorPosition
				.equals(ScreenIndicator.INDICRATOR_ON_BOTTOM);
		mIndicatorOnTop = mScreenInfo.mIndicatorPosition
				.equals(ScreenIndicator.INDICRATOR_ON_TOP);
		
		mIsScreenEditState = isScreenEditState;
		if (!mIndicatorOnBottom) {
			return;
		} else {
			//在下面就隐藏（指示器设置为在屏幕下面显示，添加界面打开程序或Widget样式界面后指示器被遮住）
//			mIndicator.hide();
		}
		if (animation && mIndicatorOnBottom) { // 带动画
//			final int dis = ( int ) (getContext().getResources().getDimension(
//					R.dimen.screen_edit_box_height) - DockUtil.getBgHeight());
			int screenEditBoxHeight = (int) getContext().getResources().getDimension(
					R.dimen.screen_edit_box_container_normal);
			int containerMode[] = new int[]{ScreenEditLayout.NORMALLAYOUT};
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_EDIT_BOX_FRAME, IDiyMsgIds.GET_SCREEN_EDIT_BOX_CONTAINER_MODE, -1, containerMode, null);
			if (containerMode[0] == ScreenEditLayout.GOWIDGETLAYOUT) {
				if (DrawUtils.sHeightPixels < 800) {
					screenEditBoxHeight = (int) (DrawUtils.sHeightPixels * ScreenEditLayout.GOWIDGETSCALE);
				} else {
					screenEditBoxHeight = (int) getContext().getResources().getDimension(
							R.dimen.screen_edit_box_container_gowidgets);
				}
			} else if (containerMode[0] == ScreenEditLayout.APPSLAYOUT) {
				screenEditBoxHeight = (int) (DrawUtils.sHeightPixels * ScreenEditLayout.APPSCALE);
			}
			if (isScreenEditState) { // 指示器上移动画
				TranslateAnimation animationIn = new TranslateAnimation(0, 0, screenEditBoxHeight, 0);
				animationIn.setDuration(ScreenEditBoxFrame.ANIMATION_DURATION);
				animationIn.setInterpolator(new DecelerateInterpolator());
				mIndicator.startAnimation(animationIn);
			} else { // 指示器下移动画
				TranslateAnimation animationOut = new TranslateAnimation(0, 0, -screenEditBoxHeight, 0);
				animationOut.setDuration(ScreenEditBoxFrame.ANIMATION_DURATION);
				animationOut.setInterpolator(new DecelerateInterpolator());
				mIndicator.startAnimation(animationOut);
			}
		} else { // 不带动画
			requestLayout();
			for (int i = 0; i < getWorkspace().getChildCount(); i++) {
				View screenView = getWorkspace().getChildAt(i);
				if (screenView != null) {
					screenView.requestLayout();
				}
			}
		}
	}

	@Override
	public void onAnimationStart(Animation animation) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		requestLayout();
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		// TODO Auto-generated method stub
	}

	protected void setDockVisibleFlag(boolean visible) {
		mDockVisible = visible;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		// 因为workspace的TouchEvent总是返回true，需要在此传TouchEvent给mScreenBackground
		return super.dispatchTouchEvent(event) | mBackWorkspace.dispatchTouchEvent(event);
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		try {
			super.onRestoreInstanceState(state);
		} catch (Exception e) {
			Log.i("ScreenLayout", "onRestoreInstanceState has exception " + e.getMessage());
		}
	}
	
	@Override
	protected void dispatchRestoreInstanceState(
			SparseArray<Parcelable> container) {
		try {
			super.dispatchRestoreInstanceState(container);
		} catch (Exception e) {
			Log.i("ScreenLayout", "onRestoreInstanceState has exception " + e.getMessage());
		}
	}
	
	
}
