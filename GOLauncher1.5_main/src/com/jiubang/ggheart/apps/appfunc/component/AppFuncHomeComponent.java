package com.jiubang.ggheart.apps.appfunc.component;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;

import com.go.util.AppUtils;
import com.go.util.graphics.ImageUtil;
import com.jiubang.core.mars.IAnimateListener;
import com.jiubang.core.mars.XALinear;
import com.jiubang.core.mars.XAnimator;
import com.jiubang.core.mars.XComponent;
import com.jiubang.core.mars.XMotion;
import com.jiubang.core.mars.XPanel;
import com.jiubang.ggheart.apps.appfunc.theme.AppFuncThemeController;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncFrame;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUtils;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.launcher.GOLauncherApp;
/**
 * 
 * <br>类描述:功能表底部操作栏组件
 * <br>功能详细描述:
 * 
 * @author  huangshaotao
 * @date  [2012-9-26]
 */
public class AppFuncHomeComponent extends XPanel implements IAnimateListener {

	private Activity mActivity;

	/**
	 * 编辑状态时的dock位置容器，由外部设置进来
	 */
	private AppFuncDockContent mEditStateDockContent;

	/**
	 * 房子按钮，搜索按钮，菜单按钮放到这个容器里面
	 */
	protected AppFuncDockContent mDockContent;

	/**
	 * 竖屏时的home背景图片
	 */
	private Drawable mBottomHomeV;
	/**
	 * 横屏时的home栏背景图片
	 */
	private Drawable mBottomHomeH;
	/**
	 * home背景图片绘制方式
	 */
	private byte mBottomHomeDrawingWay;

	/**
	 * 主题控制器
	 */
	protected AppFuncThemeController mThemeCtrl;

	/**
	 * 是否绘制一层颜色
	 */
	private boolean mIsDrawWrapColor;

	/**
	 * 文件夹打开时功能表的背景颜色
	 */
	public int mFolderOpenBgColor;
	public int mFolderOpenBgColorOnAnimate; // 动画时的值

	protected AppFuncUtils mUtils;

	private Paint mPaint;

	/**
	 * 动画
	 */
	private XMotion mMotion;

	private boolean mIsFolderShow = false;

	private boolean mIsRepaint;

	/**
	 * 非编辑模式下为true，编辑模式下为false
	 */
	private boolean mShowDock = true;

	private boolean mIsVertical = true;

	public AppFuncHomeComponent(Activity activity, int tickCount, int x, int y, int width,
			int height) {
		super(tickCount, x, y, width, height);

		setLayout(null);
		mActivity = activity;
		mUtils = AppFuncUtils.getInstance(mActivity);
		mThemeCtrl = AppFuncFrame.getThemeController();

		mPaint = new Paint();

		mIsDrawWrapColor = false;

	}

	/**
	 * 添加常规dock条容器
	 * 
	 * @param dockContent
	 */
	public void setDockContent(AppFuncDockContent dockContent) {
		if (mDockContent != dockContent) {
			if (mDockContent != null) {
				mDockContent.setAnimateListener(null);
				removeComponent(mDockContent);
			}
			mDockContent = dockContent;
			if (mDockContent != null) {
				mDockContent.setAnimateListener(this);
				addComponent(mDockContent);
			}
		}
	}

	public AppFuncDockContent getDockContent() {
		return mDockContent;
	}

	@Override
	protected void drawCurrentFrame(Canvas canvas) {
		if (!(getInScreenContent() instanceof AppMoveToDesk)
				&& !(getInScreenContent() instanceof ProManageEditDock)) {
			drawBg(canvas);
		}

		super.drawCurrentFrame(canvas);
	}

	@Override
	protected boolean animate() {
		boolean ret = false;
		if (mMotion != null) {
			if (mMotion.isFinished()) {
				clearAnimation();
			} else {
				// 动画的x值作为alpha值
				mFolderOpenBgColorOnAnimate = (mMotion.GetCurX() << 24)
						| (mFolderOpenBgColor & 0xFFFFFF);
			}
			ret = true;
		}
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
		layoutDockContent();
		layoutToDeskIcon();
	}

	private void layoutDockContent() {
		layoutChild(mDockContent, mShowDock);
	}

	/**
	 * 移动到桌面控件布局
	 */
	private void layoutToDeskIcon() {
		layoutChild(mEditStateDockContent, !mShowDock);
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
				child.setXY(0, mHeight);
			} else {
				child.setXY(mWidth, 0);
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
		mBottomHomeV = mThemeCtrl.getDrawable(mThemeCtrl.getThemeBean().mHomeBean.mHomeBgVerPath,
				packageName);

		mBottomHomeH = mThemeCtrl.getDrawable(mThemeCtrl.getThemeBean().mHomeBean.mHomeBgHorPath,
				packageName);

		mBottomHomeDrawingWay = mThemeCtrl.getThemeBean().mHomeBean.mHomeBgDrawingWay;

		if (mEditStateDockContent != null) {
			mEditStateDockContent.loadResource(packageName);
		}
		if (mDockContent != null) {
			mDockContent.loadResource(packageName);
		}
	}

	private void drawBg(Canvas canvas) {
		if (mUtils.isVertical()) {
			if (mBottomHomeV != null) {
				ImageUtil.drawImage(canvas, mBottomHomeV, mBottomHomeDrawingWay, 0, 0, mWidth,
						mHeight, mPaint);
			} else {
				// 如果是一张横屏的图片，需要旋转90度
				if (mBottomHomeH != null) {
					// 如果是一张竖屏的图片，需要旋转-90度

					canvas.save();
					canvas.rotate(-90.0f);
					canvas.translate(-mHeight, 0);
					ImageUtil.drawImage(canvas, mBottomHomeH, mBottomHomeDrawingWay, 0, 0,
							mHeight, mWidth, mPaint);
					canvas.restore();
				}
			}
		} else {
			if (mBottomHomeH != null) {
				ImageUtil.drawImage(canvas, mBottomHomeH, mBottomHomeDrawingWay, 0, 0, mWidth,
						mHeight, mPaint);
			} else {
				if (mBottomHomeV != null) {
					// 如果是一张竖屏的图片，需要旋转-90度
					canvas.save();
					canvas.rotate(-90.0f);
					canvas.translate(-mHeight, 0);
					ImageUtil.drawImage(canvas, mBottomHomeV, mBottomHomeDrawingWay, 0, 0,
							mHeight, mWidth, mPaint);
					canvas.restore();
				}
			}
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
		if (animator.equals(mDockContent.getMotion())) {
			if (mDockContent.isInScreen() == false) {
				mEditStateDockContent.startAnimation(0, 0);
			}
		} else if (animator.equals(mEditStateDockContent.getMotion())) {
			// 如果是编辑模式dock控件做完动画
			if (mEditStateDockContent.isInScreen() == false) {
				// 需要通知正常模式控件做升起动画
				mDockContent.startAnimation(0, 0);
			}
		}
	}

	/**
	 * 设置编辑状态时的dock容器组件
	 * 
	 * @param on
	 */
	public void setEditStateDockContent(AppFuncDockContent dockContent) {
		if (mEditStateDockContent != dockContent) {
			if (mEditStateDockContent != null) {
				mEditStateDockContent.setAnimateListener(null);
				removeComponent(mEditStateDockContent);
			}
			mEditStateDockContent = dockContent;
			mEditStateDockContent.setAnimateListener(this);
			addComponent(mEditStateDockContent);
		}
	}

	public AppFuncDockContent getEditStateDockContent() {
		return mEditStateDockContent;
	}
	/**
	 * <br>功能简述:显示编辑状态时的dock栏
	 * <br>功能详细描述:显示编辑状态时的dock栏，正常状态的dock栏会被隐藏。
	 * <br>注意:无
	 * @param needAnimation 是否需要动画
	 */
	public void showEditDockContent(boolean needAnimation) {
		if (mEditStateDockContent == null || mShowDock == false) {
			return;
		}
		
		layoutHideChild(mEditStateDockContent);
		mEditStateDockContent.setVisible(true);
		mShowDock = false;
		showChild(mEditStateDockContent, mDockContent, needAnimation);
	}
	/**
	 * <br>功能简述:显示正常状态时的dock栏
	 * <br>功能详细描述:显示正常状态时的dock栏，编辑状态的dock栏会被隐藏。
	 * <br>注意:无
	 * @param needAnimation 是否需要动画
	 */
	public void showDockContent(boolean needAnimation) {
		if (mDockContent == null || mShowDock == true) {
			return;
		}
		
		layoutHideChild(mDockContent);
		mDockContent.setVisible(true);
		mShowDock = true;
		showChild(mDockContent, mEditStateDockContent, needAnimation);
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
				boolean isVertical = AppFuncUtils.getInstance(mActivity).isVertical();
				if (isVertical) {
					childForHide.setXY(0, mHeight);
				} else {
					childForHide.setXY(mWidth, 0);
				}
			}
			if (mUtils.isVertical()) {
				if (childForHide.mY < 0) {
					childForHide.startAnimation(childForHide.mX, mHeight);
				} else {
					childForHide.startAnimation(childForHide.mX, childForHide.mY + mHeight);
				}
			} else {
				if (childForHide.mX < 0) {
					childForHide.startAnimation(mWidth, childForHide.mY);
				} else {
					childForHide.startAnimation(childForHide.mX + mWidth, childForHide.mY);
				}
			}
		} else {
			childForShow.setXY(0, 0);
			boolean isVertical = AppFuncUtils.getInstance(mActivity).isVertical();
			if (isVertical) {
				childForHide.setXY(0, mHeight);
			} else {
				childForHide.setXY(mWidth, 0);
			}
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		mIsRepaint = true;
	}

	public void changeHomeIconBackground(boolean b) {
		if (mEditStateDockContent != null) {
			mEditStateDockContent.setSelected(b);
		}
	}

	public void resetMotion() {
		if (mDockContent != null) {
			mDockContent.resetMotion();
		}
		if (mEditStateDockContent != null) {
			mEditStateDockContent.resetMotion();
		}

	}

	public void resetResource() {
		if (mDockContent != null) {
			mDockContent.resetResource();
		}

		if (mEditStateDockContent != null) {
			mEditStateDockContent.resetResource();
		}
	}

	/**
	 * 判断当前的移动的点是否在组件类，当它超过home的下方时，让当作它在组件类
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

	public void setIsDrawWrapColor(boolean isDraw) {
		mIsDrawWrapColor = isDraw;
	}

	public void setDrawWrapColor(int color) {
		mFolderOpenBgColor = color;
	}

	public void clearAnimation() {
		if (mMotion != null) {
			detachAnimator(mMotion);
			mMotion = null;
		}
	}

	/**
	 * 开始文件夹打开/关闭时的前景颜色（相当于文件夹的背景）的渐变动画
	 * 
	 * @param fadeIn
	 *            淡入还是淡出
	 * @param totalStep
	 *            动画帧数
	 */
	public void startFadeAnimation(boolean fadeIn, int totalStep) {
		clearAnimation();
		int alpha = mFolderOpenBgColor >>> 24;
		if (fadeIn) {
			mMotion = new XALinear(1, XALinear.XALINEAR_ECSPEED, 0, 0, alpha, 0, totalStep, 0, 0);
		} else {
			mMotion = new XALinear(1, XALinear.XALINEAR_ECSPEED, alpha, 0, 0, 0, totalStep, 0, 0);
		}
		attachAnimator(mMotion);
	}

	/**
	 * 获取HomeComponent中当前显示的子容器（使用时注意判空）
	 */
	public XComponent getInScreenContent() {
		if (mDockContent != null && mDockContent.isInScreen()) {
			return mDockContent;
		} else {
			return mEditStateDockContent;
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
			child.layout(0, mHeight, mWidth, mHeight * 2);
		} else {
			child.layout(mWidth, 0, mWidth * 2, mHeight);
		}
	}
}
