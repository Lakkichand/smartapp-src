package com.jiubang.ggheart.apps.desks.appfunc;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

import com.gau.go.launcherex.R;
import com.go.util.graphics.ImageUtil;
import com.jiubang.core.mars.EventType;
import com.jiubang.core.mars.IComponentEventListener;
import com.jiubang.core.mars.XComponent;
import com.jiubang.core.mars.XLinearLayout;
import com.jiubang.ggheart.apps.appfunc.component.AppFuncDockContent;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUtils;
import com.jiubang.ggheart.apps.desks.appfunc.model.DeliverMsgManager;
import com.jiubang.ggheart.apps.desks.appfunc.model.IMsgHandler;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.launcher.GOLauncherApp;
/**
 * 
 * <br>类描述:功能表tab栏组件，ui上表现为功能表整个tab栏
 * <br>功能详细描述:
 * 
 * @author  
 * @date  
 */
public class AppFuncTabTitle extends AppFuncDockContent implements IComponentEventListener, IMsgHandler {

	private OnTabTitleSelectionChanged mSelectionChangedListener;

	private int mSelectedTab = 0;

	private XLinearLayout mLinearLayout;

	private Paint mPaint;

	/**
	 * 竖屏时的Tab栏背景图片
	 */
	private Drawable mTabBg_v;
	/**
	 * 横屏时的Tab栏背景图片
	 */
	private Drawable mTabBg_h;
	/**
	 * Tab栏背景图片绘制方式
	 */
	private byte mTabBgDrawingWay;

	AppFuncTabTitle(Activity activity, int tickCount, int x, int y, int width, int height) {
		super(activity, tickCount, x, y, width, height, HIDE_TYPE_TOP);

		mActivity = activity;
		mLinearLayout = new XLinearLayout(XLinearLayout.HORIZONTAL, true);
		mThemeCtrl = AppFuncFrame.getThemeController();
		mUtils = AppFuncUtils.getInstance(mActivity);
		mPaint = new Paint();
		setLayout(mLinearLayout);
		// 注册主题改变事件
		DeliverMsgManager.getInstance().registerDispenseMsgHandler(AppFuncConstants.THEME_CHANGE,
				this);
		// 注册加载资源事件
		DeliverMsgManager.getInstance().registerDispenseMsgHandler(AppFuncConstants.LOADTHEMERES,
				this);
	}

	public int getTabCount() {
		return mStoreComponent.size();
	}

	public void addTabTitle(XComponent component) {
		addComponent(component);
		component.setEventListener(this);
	}

	public void removeTabTitle(XComponent component) {
		removeComponent(component);
		component.setEventListener(null);
	}

	public void focusCurrentTab(int index) {

		final int oldTab = mSelectedTab;

		// set the tab
		setCurrentTab(index);

		// change the focus if applicable.
		if (oldTab != index) {
			// getChildTabViewAt(index).requestFocus();
		}
	}

	public void setCurrentTab(int index) {

		if (index < 0 || index >= getTabCount()) {
			return;
		}

		mSelectedTab = index;
	}

	public boolean hasFocus() {

		return mFocusComponent != null;
	}

	public void setTabSelectionListener(OnTabTitleSelectionChanged selectionListerner) {

		mSelectionChangedListener = selectionListerner;
	}

	@Override
	protected void drawCurrentFrame(Canvas canvas) {

		drawBg(canvas);
		super.drawCurrentFrame(canvas);
	}

	private void drawBg(Canvas canvas) {
		if (mUtils.isVertical()) {
			if (mTabBg_v != null) {
				ImageUtil.drawImage(canvas, mTabBg_v, mTabBgDrawingWay, 0, 0, mWidth, mHeight,
						mPaint);
			} else {
				// // 如果是一张横屏的图片，需要旋转90度
				// if (mTabBg_h != null) {
				// // 如果是一张竖屏的图片，需要旋转-90度
				// canvas.save();
				// canvas.rotate(-90.0f);
				// canvas.translate(-mHeight, 0);
				//
				// ImageUtil.drawImage(canvas, mTabBg_h, mTabBgDrawingWay, 0,
				// 0, mHeight, mWidth, mPaint);
				// canvas.restore();
				// }
			}
		} else {
			if (mTabBg_h != null) {
				ImageUtil.drawImage(canvas, mTabBg_h, mTabBgDrawingWay, 0, 0, mWidth, mHeight,
						mPaint);
			} else {
				if (mTabBg_v != null) {
					// 如果是一张竖屏的图片，需要旋转-90度
					canvas.save();
					canvas.rotate(-90.0f);
					canvas.translate(-mHeight, 0);
					ImageUtil.drawImage(canvas, mTabBg_v, mTabBgDrawingWay, 0, 0, mHeight, mWidth,
							mPaint);
					canvas.restore();
				}
			}
		}
	}

	@Override
	public boolean onEventFired(XComponent component, byte eventType, Object event, int arg,
			Object object) {
		switch (eventType) {
			case EventType.CLICKEVENT :
				if (mSelectionChangedListener != null) {
					mSelectionChangedListener.onTabSelectionChanged(component);
				}
				return true;

			default :
				break;
		}

		return false;
	}

	public void setOrientation(byte orientation) {
		if (mLinearLayout != null) {
			mLinearLayout.setOrientation(orientation);
		}
	}

	@Override
	public void layout(int left, int top, int right, int bottom) {
		setPosition(left, top, right, bottom);
		super.layout(left, top, right, bottom);
	}
	
	@Override
	public void loadResource(String pkName) {
		String packageName = GOLauncherApp.getSettingControler().getFunAppSetting()
				.getTabHomeBgSetting();
		if (packageName.equals(ThemeManager.DEFAULT_THEME_PACKAGE)) {
			mTabBg_v = mThemeCtrl.getDrawable(String.valueOf(R.drawable.tab_bg));
			// mTabBg_h =
			// mThemeCtrl.getDrawable(String.valueOf(R.drawable.tab_bg));
		} else {
			mTabBg_v = mThemeCtrl.getDrawable(
					mThemeCtrl.getThemeBean().mAllTabsBean.mAllTabsBgBottomVerPath, packageName);
			String hStr = mThemeCtrl.getThemeBean().mAllTabsBean.mAllTabsBgBottomHorPath;
			if (hStr != null && !"".equals(hStr)) {
				mTabBg_h = mThemeCtrl.getDrawable(hStr, packageName);
			}

		}
		mTabBgDrawingWay = mThemeCtrl.getThemeBean().mAllTabsBean.mAllTabsBgDrawingWay;
	}

	@Override
	public void resetResource() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void notify(int key, Object obj) {
		switch (key) {
			case AppFuncConstants.THEME_CHANGE : {
				mTabBg_v = null;
				mTabBg_h = null;
				break;
			}
			case AppFuncConstants.LOADTHEMERES : {
				loadResource(null);
				break;
			}
			default :
				break;
		}
	}

	public void setFocusIndex(int index, boolean isFocus) {
		XComponent compoent = getChildAt(index);
		compoent.setFocused(isFocus);
	}

	public void cleanFocus() {
		int size = mDrawComponent.size();
		for (int i = 0; i < size; i++) {
			XComponent component = getDrawComponent(i);
			component.setFocused(false);
		}
	}

	public XComponent getFirstComponent() {
		return getChildAt(0);
	}

	// 将第一个组件与最后一个组件交换位置
	public void changeComponent() {
		replaceComponent(getChildAt(0), getChildAt(getChildCount() - 1));
	}
	/**
	 * 
	 * <br>类描述:功能表当前tab项发生改变是的接口
	 * <br>功能详细描述:
	 * 
	 * @author  huangshaotao
	 * @date  [2012-9-26]
	 */
	interface OnTabTitleSelectionChanged {

		void onTabSelectionChanged(XComponent touchComponent);
	}

	// @Override
	// public boolean handleFocus(KeyEvent event) {
	// boolean handleFocus = false;
	// // 到达边界
	// if (mFocusComponent != null) {
	// // handleFocus = mFocusComponent.handleFocus(event);
	// int indexOfComponent = indexOfComponent(mFocusComponent);
	// if (!handleFocus) {
	// switch (event.getKeyCode()) {
	// case KeyEvent.KEYCODE_DPAD_LEFT: {
	// if (indexOfComponent != 0 && indexOfComponent != -1) {
	// // setCurrentTab(mSelectedTab-1);
	// mFocusComponent.setFocused(false);
	// mFocusComponent = getChildAt(indexOfComponent - 1);
	// mFocusComponent.handleFocus(event);
	// mFocusComponent.setFocused(true);
	// }
	// handleFocus = true;
	// }
	// break;
	// case KeyEvent.KEYCODE_DPAD_RIGHT: {
	// if (indexOfComponent != (getChildCount() - 1)
	// && indexOfComponent != -1) {
	// // setCurrentTab(mSelectedTab+1);
	// mFocusComponent.setFocused(false);
	// mFocusComponent = getChildAt(indexOfComponent + 1);
	// mFocusComponent.handleFocus(event);
	// mFocusComponent.setFocused(true);
	// }
	// handleFocus = true;
	// }
	// break;
	//
	// default:
	// break;
	// }
	// }
	// }
	// return handleFocus;
	//
	// }

	// public void changeFocusComponent(XComponent component) {
	//
	// if (component == null)
	// return;
	//
	// if (mFocusComponent != null) {
	// mFocusComponent.setFocused(false);
	// mFocusComponent = component;
	// }
	// }
}
