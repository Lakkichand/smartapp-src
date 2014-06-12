package com.jiubang.ggheart.apps.appfunc.common.component;

import android.app.Activity;
import android.view.MotionEvent;

import com.go.util.AppUtils;
import com.jiubang.core.mars.IAnimateListener;
import com.jiubang.core.mars.XAnimator;
import com.jiubang.core.mars.XComponent;
import com.jiubang.core.mars.XPanel;
import com.jiubang.ggheart.apps.appfunc.component.AppFuncDockContent;
import com.jiubang.ggheart.apps.appfunc.theme.AppFuncThemeController;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncFrame;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUtils;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.launcher.GOLauncherApp;
/**
 * 
 * <br>类描述:功能表顶部可切换操作栏容器
 * <br>功能详细描述:
 * 
 * @author  huangshaotao
 * @date  [2012-9-25]
 */
public class AppFuncTopSwitchContainer extends XPanel implements IAnimateListener {

	private Activity mActivity;

	/**
	 * 编辑状态时的dock位置容器，由外部设置进来
	 */
	private AppFuncDockContent mEditBar;

	/**
	 * 房子按钮，搜索按钮，菜单按钮放到这个容器里面
	 */
	protected AppFuncDockContent mNormalBar;

	/**
	 * 主题控制器
	 */
	protected AppFuncThemeController mThemeCtrl;

	protected AppFuncUtils mUtils;

	private boolean mIsFolderShow = false;

	private boolean mIsRepaint;

	private boolean mShowNormalBar = true;

	private boolean mIsVertical = true;

	public AppFuncTopSwitchContainer(Activity activity, int tickCount, int x, int y, int width,
			int height) {
		super(tickCount, x, y, width, height);

		setLayout(null);
		mActivity = activity;
		mUtils = AppFuncUtils.getInstance(mActivity);
		mThemeCtrl = AppFuncFrame.getThemeController();
	}

	/**
	 * 添加常规操作条容器
	 * 
	 * @param dockContent
	 */
	public void setNormalBar(AppFuncDockContent dockContent) {
		if (mNormalBar != dockContent) {
			if (mNormalBar != null) {
				mNormalBar.setAnimateListener(null);
				removeComponent(mNormalBar);
			}
			mNormalBar = dockContent;
			if (mNormalBar != null) {
				mNormalBar.setAnimateListener(this);
				addComponent(mNormalBar);
			}
		}
	}

	public AppFuncDockContent getNormalBar() {
		return mNormalBar;
	}

	@Override
	protected boolean animate() {
		boolean ret = false;
		if (mIsRepaint) {
			mIsRepaint = false;
			ret = true;
		}
		return super.animate() || ret;
	}

	@Override
	public void layout(int left, int top, int right, int bottom) {
		setPosition(left, top, right, bottom);
		mIsVertical = AppFuncUtils.getInstance(mActivity).isVertical();
		layoutNormalBar();
		layoutEditBar();
	}

	private void layoutNormalBar() {
		layoutChild(mNormalBar, mShowNormalBar);
	}

	/**
	 * 移动到桌面控件布局
	 */
	private void layoutEditBar() {
		layoutChild(mEditBar, !mShowNormalBar);
	}

	private void layoutChild(XComponent child, boolean show) {
		if (child == null) {
			return;
		}
		child.setMotionFilter(null);

		if (show) {
			child.layout(0, 0, mWidth, mHeight);
			child.setVisible(true);
		} else {
			if (mIsVertical) {
				child.setXY(0, -mHeight);
			} else {
				child.setXY(-mWidth, 0);
			}
			child.setSize(0, 0);
			child.setVisible(false);
		}
	}

	public void loadResource() {
		String curPackageName = ThemeManager.getInstance(mActivity).getCurThemePackage();
		String packageName = null;
		if (!curPackageName.equals(GOLauncherApp.getSettingControler().getFunAppSetting()
				.getTabHomeBgSetting())) {
			packageName = GOLauncherApp.getSettingControler().getFunAppSetting()
					.getTabHomeBgSetting();
		}
		if (!AppUtils.isAppExist(mContext, packageName)) {
			packageName = GOLauncherApp.getThemeManager().getCurThemePackage();
		}

		if (mEditBar != null) {
			mEditBar.loadResource(packageName);
		}
		if (mNormalBar != null) {
			mNormalBar.loadResource(packageName);
		}
	}

	@Override
	public void onStart(XAnimator animator) {
	}

	@Override
	public void onProgress(XAnimator animator, int progress) {
	}

	@Override
	public boolean onTouch(MotionEvent motionevent) {
//		if (mIsFolderShow) {
//			return false;
//		} else {
			return super.onTouch(motionevent);
//		}
	}

	@Override
	public void onFinish(XAnimator animator) {
		if (mNormalBar == null || mEditBar == null) {
			return;
		}
		if (animator.equals(mNormalBar.getMotion())) {
			if (mNormalBar.isInScreen() == false) {
				mEditBar.startAnimation(0, 0);
			}
		} else if (animator.equals(mEditBar.getMotion())) {
			// 如果是编辑模式控件做完动画
			if (mEditBar.isInScreen() == false) {
				// 需要通知正常模式控件做升起动画
				mNormalBar.startAnimation(0, 0);
			}
		}
	}

	/**
	 * 设置编辑状态时的dock容器组件
	 * 
	 * @param on
	 */
	public void setEditBar(AppFuncDockContent dockContent) {
		if (mEditBar != dockContent) {
			if (mEditBar != null) {
				mEditBar.setAnimateListener(null);
				removeComponent(mEditBar);
			}
			mEditBar = dockContent;
			if (mEditBar != null) {
				mEditBar.setAnimateListener(this);
				addComponent(mEditBar);
				requestLayout();
			}
		}
	}

	public AppFuncDockContent getEditBar() {
		return mEditBar;
	}
	/**
	 * <br>功能简述:显示编辑状态时的dock栏
	 * <br>功能详细描述:显示编辑状态时的dock栏，正常状态的dock栏会被隐藏。
	 * <br>注意:无
	 * @param needAnimation 是否需要动画
	 */
	public void showEditBar(boolean needAnimation) {
		if (mEditBar == null || mShowNormalBar == false) {
			return;
		}
		
		layoutHideChild(mEditBar);
		mEditBar.setVisible(true);
		mShowNormalBar = false;
		showChild(mEditBar, mNormalBar, needAnimation);
	}
	/**
	 * <br>功能简述:显示正常状态时的dock栏
	 * <br>功能详细描述:显示正常状态时的dock栏，编辑状态的dock栏会被隐藏。
	 * <br>注意:无
	 * @param needAnimation 是否需要动画
	 */
	public void showNormalBar(boolean needAnimation) {
		if (mNormalBar == null || mShowNormalBar == true) {
			return;
		}
		
		layoutHideChild(mNormalBar);
		mNormalBar.setVisible(true);
		mShowNormalBar = true;
		showChild(mNormalBar, mEditBar, needAnimation);
	}

	private void showChild(AppFuncDockContent childForShow, AppFuncDockContent childForHide,
			boolean needAnimation) {
		if (childForShow == null || childForHide == null) {
			return;
		}

		if (needAnimation) {
			if (childForShow.isInMotion() || childForHide.isInMotion()) {
				resetMotion();
				childForShow.setXY(0, 0);
				if (mUtils.isVertical()) {
					childForHide.setXY(0, -mHeight);
				} else {
					childForHide.setXY(-mWidth, 0);
				}
			}
			if (mUtils.isVertical()) {
				childForHide.startAnimation(0, -mHeight);
			} else {
				childForHide.startAnimation(-mWidth, 0);
			}
		} else {
			childForShow.setXY(0, 0);
			if (mUtils.isVertical()) {
				childForHide.setXY(0, -mHeight);
			} else {
				childForHide.setXY(-mWidth, 0);
			}
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		mIsRepaint = true;
	}

	public void changeHomeIconBackground(boolean b) {
		if (mEditBar != null) {
			mEditBar.setSelected(b);
		}
	}

	public void resetMotion() {
		if (mNormalBar != null) {
			mNormalBar.resetMotion();
		}
		if (mEditBar != null) {
			mEditBar.resetMotion();
		}

	}

	public void resetResource() {
		if (mNormalBar != null) {
			mNormalBar.resetResource();
		}

		if (mEditBar != null) {
			mEditBar.resetResource();
		}
	}

	/**
	 * 判断当前的移动的点是否在顶部组件类，当它超过home的下方时，让当作它在组件类
	 */
	public boolean isMovePointIn(int x, int y) {
		boolean isInRange = false;
		isInRange = XYInRange(x, y);
		if (!isInRange) {
			if (mUtils.isVertical()) {
				if (y > getAbsY()) {
					isInRange = true;
				}
			} else if (x > getAbsX()) {
				isInRange = true;
			}
		}

		return isInRange;
	}

	/**
	 * 获取HomeComponent中当前显示的子容器（使用时注意判空）
	 */
	public XComponent getInScreenContent() {
		if (mNormalBar != null && mNormalBar.isInScreen()) {
			return mNormalBar;
		} else {
			return mEditBar;
		}
	}

	//CHECKSTYLE:OFF
	@Override
	public boolean XYInRange(int i, int j) {
		boolean in = false;
		XComponent c = getInScreenContent();
		if (c != null) {
			in = c.XYInRange(i, j);
		}

		return in;
	}
	//CHECKSTYLE:ON
	
	public void setIsFolderShow(boolean show) {
		mIsFolderShow = show;
	}

	public boolean isFolderShow() {
		return mIsFolderShow;
	}
	
	private void layoutHideChild(XComponent child) {
		if (mIsVertical) {
			child.layout(0, -mHeight, mWidth, 0);
		} else {
			child.layout(-mWidth, 0, 0, mHeight);
		}
	}
}
