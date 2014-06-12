package com.jiubang.ggheart.apps.appfunc.component;

import android.app.Activity;
import android.graphics.Canvas;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.gau.go.launcherex.R;
import com.jiubang.core.mars.IAnimateListener;
import com.jiubang.core.mars.XALinear;
import com.jiubang.core.mars.XAnimator;
import com.jiubang.core.mars.XComponent;
import com.jiubang.core.mars.XLinearLayout;
import com.jiubang.core.mars.XMotion;
import com.jiubang.core.mars.XPanel;
import com.jiubang.ggheart.apps.appfunc.setting.FunAppSetting;
import com.jiubang.ggheart.apps.appfunc.theme.AppFuncThemeController;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncFrame;
import com.jiubang.ggheart.apps.desks.appfunc.XBaseGrid;
import com.jiubang.ggheart.apps.desks.appfunc.animation.AnimationManager;
import com.jiubang.ggheart.apps.desks.appfunc.animation.AnimationManager.AnimationInfo;
import com.jiubang.ggheart.apps.desks.appfunc.handler.AppFuncHandler;
import com.jiubang.ggheart.apps.desks.appfunc.handler.FuncAppDataHandler;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUtils;
import com.jiubang.ggheart.apps.desks.appfunc.model.AppFuncAdapter;
import com.jiubang.ggheart.apps.desks.appfunc.model.DeliverMsgManager;
import com.jiubang.ggheart.apps.desks.appfunc.model.IMsgHandler;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 功能表基本风格Tab对应内容的基类
 * 
 * @author tanshu
 * 
 */
public abstract class AppFuncTabBasicContent extends XPanel implements IMsgHandler {
	/**
	 * 宫格
	 */
	protected XBaseGrid mGrid;
	/**
	 * 宫格对应的数据源适配器
	 */
	protected AppFuncAdapter mAdapter;
	/**
	 * 底部房子控件
	 */
	// protected AppFuncHomeIcon mHomeIcon;

	/**
	 * 竖屏时底部背景栏的高度id，以Hight Density为标准
	 */
	public static final int sBottomHeight_id = R.dimen.appfunc_bottomheight;

	protected AppFuncHomeComponent mHomeComponent;

	protected Activity mActivity;
	protected AppFuncUtils mUtils;
	/**
	 * 主题控制器
	 */
	protected AppFuncThemeController mThemeCtrl;
	/**
	 * 房子规格，以Hight Density为标准
	 */
	protected static final int HOME_SIZE = 80;

	private XLinearLayout mLinearLayout;

	/**
	 * 是否绘制一层颜色
	 */
	private boolean mIsDrawWrapColor;

	protected FunAppSetting mFunAppSetting;

	public AppFuncTabBasicContent(Activity activity, int tickCount, int x, int y, int width,
			int height, int gridId) {
		super(tickCount, x, y, width, height);
		mActivity = activity;
		mUtils = AppFuncUtils.getInstance(mActivity);
		mThemeCtrl = AppFuncFrame.getThemeController();
		// 创建宫格
		initGridView(tickCount, x, y, gridId);

		// 创建小房子
		// mHomeIcon = new AppFuncHomeIcon(activity, tickCount, 0, 0, 0, 0);

		mHomeComponent = new AppFuncHomeComponent(activity, tickCount, 0, 0, 0, 0);
		mHomeComponent.setZorder(100); // 画在最上面

		// 注册主题改变事件
		DeliverMsgManager.getInstance().registerDispenseMsgHandler(AppFuncConstants.THEME_CHANGE,
				this);
		// 注册装载主题资源事件
		DeliverMsgManager.getInstance().registerDispenseMsgHandler(AppFuncConstants.LOADTHEMERES,
				this);

		mLinearLayout = new XLinearLayout();
		setLayout(mLinearLayout);

		mIsDrawWrapColor = false;

		mFunAppSetting = GOLauncherApp.getSettingControler().getFunAppSetting();
	}

	protected void initGridView(int tickCount, int x, int y, int gridId) {
		mGrid = new XBaseGrid(mActivity, tickCount, x, y);
		mGrid.mId = gridId;
		// 将Grid注册给消息发送控制器
		DeliverMsgManager.getInstance().registerMsgHandler(mGrid.mId, mGrid);
		// 获得规格
		mUtils.setGridStandard(AppFuncFrame.getDataHandler().getStandard(), mGrid, mGrid.mId);
		// 获得滑动方向
		if (AppFuncFrame.getDataHandler().getSlideDirection() == FunAppSetting.SCREENMOVEHORIZONTAL) {
			mGrid.setVScroll(false);
		} else {
			mGrid.setVScroll(true);
		}
		mGrid.setPaddingLeft(0);
		mGrid.setPaddingRight(7);
		mGrid.setPaddingTop(0);
		mGrid.setPaddingBottom(10);
		mGrid.setSupportScroll(true);
		mGrid.setDragable(true);
		// 初始化适配器并绑定
		mAdapter = initCurrentAdapter();
		mGrid.setAdapter(mAdapter);
	}

	public XBaseGrid getXGrid() {
		return mGrid;
	}

	public AppFuncAdapter getAdapter() {
		return mAdapter;
	}

	// public AppFuncHomeIcon getHomeIcon(){
	// return mHomeIcon;
	// }

	/**
	 * 初始化数据适配器
	 */
	protected abstract AppFuncAdapter initCurrentAdapter();

	/**
	 * 布局宫格
	 * 
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 */
	protected abstract void layoutGrid(int left, int top, int right, int bottom);

	/**
	 * XGrid排版参数设置
	 * 
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 */
	protected void setGridParameters() {
		FuncAppDataHandler handler = AppFuncFrame.getDataHandler();
		// 设置行列数
		mUtils.setGridStandard(handler.getStandard(), mGrid, mGrid.mId);
		// 读取滑动方向设置
		if (handler.getSlideDirection() == FunAppSetting.SCREENMOVEHORIZONTAL) {
			mGrid.setVScroll(false);
		} else {
			mGrid.setVScroll(true);
			mGrid.setVerticalEffect(handler.getVerticalScrollEffect());
		}
	}

	@Override
	protected void drawCurrentFrame(Canvas canvas) {
		if (mIsDrawWrapColor) {
			int size = mStoreComponent.size();
			for (int i = 0; i < size; i++) {
				XComponent component = getChildAt(i);
				if (component.isVisible()) {
					if (component.equals(mHomeComponent)) {
						continue;
					}
					component.checkIsShowed();
					component.paintCurrentFrame(canvas, component.mX, component.mY);
				}
			}
			// canvas.drawColor(mFolderOpenBgColor);
			if (mHomeComponent != null) {

				// canvas.save();
				// Rect rect = canvas.getClipBounds();
				//
				// if (AppFuncUtils.getInstance(mActivity).isVertical()) {
				// rect.right = mHomeComponent.mX + mWidth;
				// rect.bottom = mHomeComponent.mY;
				// // canvas.clipRect(0, 0, mHomeComponent.mX + mWidth,
				// // mHomeComponent.mY);
				// } else {
				// rect.right = mHomeComponent.mX ;
				// rect.bottom = mHomeComponent.mY + mHeight;
				//
				// }
				// canvas.clipRect(rect);
				// canvas.drawColor(mFolderOpenBgColor);
				// canvas.restore();

				mHomeComponent.checkIsShowed();
				mHomeComponent.paintCurrentFrame(canvas, mHomeComponent.mX, mHomeComponent.mY);
			}
		} else {
			drawAllChildComponents(canvas);
		}
	}

	/**
	 * 重新布局
	 */
	@Override
	public void layout(int left, int top, int right, int bottom) {
		mHomeComponent.setMotionFilter(null);
		if (AppFuncFrame.getDataHandler().isShowActionBar()) {
			mHomeComponent.setVisible(true);
		} else {
			mHomeComponent.setVisible(false);
		}
		boolean isVertical = AppFuncUtils.getInstance(mActivity).isVertical();
		if (isVertical) {
			if (mLinearLayout != null) {
				mLinearLayout.setOrientation(XLinearLayout.VERTICAL);
			}
			mHomeComponent.setSize(mWidth, mUtils.getDimensionPixelSize(sBottomHeight_id));

		} else {
			if (mLinearLayout != null) {
				mLinearLayout.setOrientation(XLinearLayout.HORIZONTAL);
			}

			mHomeComponent.setSize(mUtils.getDimensionPixelSize(sBottomHeight_id), mHeight);
		}
		super.layout(left, top, right, bottom);
		GoLauncher.sendMessage(this, IDiyFrameIds.APPFUNC_FRAME,
				IDiyMsgIds.RESPONSE_GLIDE_UP_DOWN_ACTION, 1, null, null);
	}

	/**
	 * 获得HomeIcon组件
	 */
	public AppFuncHomeComponent getHomeComponent() {
		return mHomeComponent;
	}

	public void refreshGrid() {
		if (mGrid != null) {
			mGrid.updateLayoutParams();
			mGrid.requestLayout();
		}
	}

	// public void startDeleteFolderMotion(FunFolderItemInfo funitem) {
	//
	// ApplicationIcon applicationicon = (ApplicationIcon)
	// mGrid.getChildAt(funitem.getIndex());
	// applicationicon.startDeleteFolderMotion(funitem,funitem.getFolderId());
	// }

	public boolean isInHomeComponent(int x, int y) {
		return mHomeComponent.XYInRange(x, y);
	}

	/**
	 * 当前tab显示时会被调用
	 */
	public void tabChangeUpdate() {
		// TODO

	}

	/**
	 * 默认的聚焦组件
	 */
	@Override
	public void setFocused(boolean on) {
		if (mGrid != null) {
			mGrid.setFocused(on);
		}
	}

	/**
	 * 默认的处理是否可以聚焦
	 */
	public boolean requestFocused() {
		if (getAdapter().getCount() > 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 默认的键盘处理事件
	 */
	@Override
	public boolean onKey(KeyEvent event) {
		if (mGrid != null) {
			return mGrid.onKey(event);
		}

		return false;
	}

	/**
	 * 默认的焦点返回处理
	 */
	public void passFocus() {
		AppFuncHandler.getInstance().setTabHasFocus();
	}

	public void setIsDrawWrapColor(boolean isDraw) {
		mIsDrawWrapColor = isDraw;
		mHomeComponent.setIsDrawWrapColor(isDraw);
	}

	public void setDrawWrapColor(int color) {

		mHomeComponent.setDrawWrapColor(color);
	}

	@Override
	public boolean onTouch(MotionEvent event) {
		if (super.onTouch(event)) {
			return true;
		}
		if (mGrid != null && mGrid.isUsingWaterfallEffect()) {
			// mGrid使用3D瀑布特效的时候，在翻折部分（mGrid外）的触摸事件也有mGrid处理
			return mGrid.onTouch(event);
		}
		return false;
	}

	@Override
	public void setSize(int width, int height) {
		super.setSize(width, height);
		int bottomHeight = 0;
		if (AppFuncFrame.getDataHandler().isShowActionBar()) {
			bottomHeight = mUtils.getDimensionPixelSize(sBottomHeight_id);
		}
		XBaseGrid grid = getXGrid();
		if (grid != null) {
			if (GoLauncher.isPortait()) {
				grid.setSize(width, height - bottomHeight);
			} else {
				grid.setSize(width - bottomHeight, height);
			}
		}
	}

	protected void checkAndChangeHomeComponentStatus() {
		if (getXGrid().isInDragStatus()) {
			mHomeComponent.showEditDockContent(false);
		} else {
			mHomeComponent.showDockContent(false);
		}
	}

	public void showActionBar() {
		mFunAppSetting.setShowActionBar(FunAppSetting.ON);
		final XBaseGrid grid = getXGrid();
		if (grid != null && indexOfComponent(mHomeComponent) > 0) {
			GoLauncher.sendMessage(this, IDiyFrameIds.APPFUNC_FRAME,
					IDiyMsgIds.RESPONSE_GLIDE_UP_DOWN_ACTION, 0, null, null);
			mHomeComponent.setVisible(true);
			int endX = 0;
			int endY = 0;
			if (GoLauncher.isPortait()) {
				endX = mHomeComponent.mX;
				endY = mHomeComponent.mY - mUtils.getDimensionPixelSize(sBottomHeight_id);
				mHomeComponent.mX = 0;
				mHomeComponent.mY = mHomeComponent.mY;
			} else {
				endX = mHomeComponent.mX - mUtils.getDimensionPixelSize(sBottomHeight_id);
				endY = mHomeComponent.mY;
				mHomeComponent.mX = mHomeComponent.mX;
				mHomeComponent.mY = 0;
			}
			checkAndChangeHomeComponentStatus();

			final int gridWidth = grid.getWidth();
			final int gridHeight = grid.getHeight();
			XMotion moveMotion = new XALinear(1, XALinear.XALINEAR_ECSPEED, mHomeComponent.mX,
					mHomeComponent.mY, endX, endY, 20, 1, 1);
			AnimationInfo info = new AnimationInfo(AnimationInfo.TYPE_CHANGE_POSITION,
					mHomeComponent, moveMotion, new IAnimateListener() {

						@Override
						public void onStart(XAnimator animator) {
						}

						@Override
						public void onProgress(XAnimator animator, int progress) {
							int diff = Math.round(progress
									* mUtils.getDimensionPixelSize(sBottomHeight_id) / 100f);
							if (GoLauncher.isPortait()) {
								grid.setSize(gridWidth, gridHeight - diff);
							} else {
								grid.setSize(gridWidth - diff, gridHeight);
							}
							grid.fixIconsPosition();
						}

						@Override
						public void onFinish(XAnimator animator) {
							requestLayout();
						}
					});
			AnimationManager.getInstance(mActivity).attachAnimation(info, null);
		} else {
			requestLayout();
		}
	}

	public void hideActionBar() {
		mFunAppSetting.setShowActionBar(FunAppSetting.OFF);
		final XBaseGrid grid = getXGrid();
		if (grid != null && indexOfComponent(mHomeComponent) > 0) {
			GoLauncher.sendMessage(this, IDiyFrameIds.APPFUNC_FRAME,
					IDiyMsgIds.RESPONSE_GLIDE_UP_DOWN_ACTION, 0, null, null);
			int endX = 0;
			int endY = 0;
			if (GoLauncher.isPortait()) {
				endX = mHomeComponent.mX;
				endY = mHomeComponent.mY + mUtils.getDimensionPixelSize(sBottomHeight_id);
			} else {
				endX = mHomeComponent.mX + mUtils.getDimensionPixelSize(sBottomHeight_id);
				endY = mHomeComponent.mY;
			}

			final int gridWidth = grid.getWidth();
			final int gridHeight = grid.getHeight();
			XMotion moveMotion = new XALinear(1, XALinear.XALINEAR_ECSPEED, mHomeComponent.mX,
					mHomeComponent.mY, endX, endY, 20, 1, 1);
			AnimationInfo info = new AnimationInfo(AnimationInfo.TYPE_CHANGE_POSITION,
					mHomeComponent, moveMotion, new IAnimateListener() {

						@Override
						public void onStart(XAnimator animator) {
						}

						@Override
						public void onProgress(XAnimator animator, int progress) {
							int diff = Math.round(progress
									* mUtils.getDimensionPixelSize(sBottomHeight_id) / 100f);
							if (GoLauncher.isPortait()) {
								grid.setSize(gridWidth, gridHeight + diff);
							} else {
								grid.setSize(gridWidth + diff, gridHeight);
							}
							grid.fixIconsPosition();
						}

						@Override
						public void onFinish(XAnimator animator) {
							requestLayout();
						}
					});
			AnimationManager.getInstance(mActivity).attachAnimation(info, null);
		} else {
			requestLayout();
		}
	}
}
