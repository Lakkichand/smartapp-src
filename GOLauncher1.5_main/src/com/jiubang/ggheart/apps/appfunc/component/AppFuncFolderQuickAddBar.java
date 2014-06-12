package com.jiubang.ggheart.apps.appfunc.component;

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.gau.go.launcherex.R;
import com.go.util.graphics.ImageUtil;
import com.go.util.window.OrientationControl;
import com.jiubang.core.mars.IAnimateListener;
import com.jiubang.core.mars.XAnimator;
import com.jiubang.core.mars.XComponent;
import com.jiubang.core.mars.XPanel;
import com.jiubang.ggheart.apps.appfunc.common.component.AppSingleLineContainer;
import com.jiubang.ggheart.apps.appfunc.component.AppFuncFolderQuickAddIcon.SpecialFolderItem;
import com.jiubang.ggheart.apps.appfunc.setting.AppFuncAutoFitManager;
import com.jiubang.ggheart.apps.desks.appfunc.OrientationInvoker;
import com.jiubang.ggheart.apps.desks.appfunc.XBaseGrid;
import com.jiubang.ggheart.apps.desks.appfunc.animation.AnimationManager;
import com.jiubang.ggheart.apps.desks.appfunc.animation.AnimationManager.AnimationInfo;
import com.jiubang.ggheart.apps.desks.appfunc.animation.AnimationManager.BatchAnimationObserver;
import com.jiubang.ggheart.apps.desks.appfunc.handler.AppFuncHandler;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.model.DeliverMsgManager;
import com.jiubang.ggheart.apps.desks.appfunc.model.FolderQuickAddAdapter;
import com.jiubang.ggheart.apps.desks.appfunc.model.IMsgHandler;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.data.info.FunAppItemInfo;
import com.jiubang.ggheart.data.info.FunFolderItemInfo;

/**
 * 功能表编辑状态时顶部快速添加文件夹栏
 * 
 * @author  yangguanxiang
 */
public class AppFuncFolderQuickAddBar extends AppSingleLineContainer
		implements
			IMsgHandler,
			BatchAnimationObserver {

	private static final int DEFAULT_ALPHA = 153; //透明度为60%
	private static final int FULL_ALPHA = 255;
	private static final int MSG_REFRESH_ALL = 0;

	/**
	 * 被抓取的图标对象
	 */
	private ApplicationIcon mAnimationIcon;

	private Handler mHandler;

	private Drawable mBg;

	public AppFuncFolderQuickAddBar(Activity activity, int tickCount, int x, int y, int width,
			int height) {
		super(activity, tickCount, x, y, width, height, HIDE_TYPE_TOP);
		mIndicatorSize = 45;
		initHandler();
		setItemSize(mUtils.getDimensionPixelSize(R.dimen.appfunc_quick_add_folder_grid_size));
		DeliverMsgManager.getInstance().registerMsgHandler(
				AppFuncConstants.APP_FUNC_FOLDER_QUICK_ADD_BAR, this);
	}

	private void initHandler() {
		mHandler = new Handler(Looper.getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case MSG_REFRESH_ALL :
						requestLayout();
						break;
					default :
						break;
				}
			}
		};
	}

	private AppFuncFolderQuickAddIcon findFolderIcon(FunFolderItemInfo info) {
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			AppFuncFolderQuickAddIcon icon = (AppFuncFolderQuickAddIcon) getChildAt(i);
			if (icon.getInfo() == info) {
				return icon;
			}
		}
		return null;
	}

	private AppFuncFolderQuickAddIcon findFolderIcon(int type) {
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			AppFuncFolderQuickAddIcon icon = (AppFuncFolderQuickAddIcon) getChildAt(i);
			if (((FunFolderItemInfo) icon.getInfo()).getFolderType() == type) {
				return icon;
			}
		}
		return null;
	}

	@Override
	public void layout(int left, int top, int right, int bottom) {
		mAnimationIcon = null;
		if (GoLauncher.isPortait()) {
			mBg = mActivity.getResources()
					.getDrawable(R.drawable.appfunc_folder_quick_add_bar_gb_v);
			setMaxColumnNumber(AppFuncAutoFitManager.getInstance(mActivity)
					.getFolderQuickAddBarItemCountV());
		} else {
			mBg = mActivity.getResources()
					.getDrawable(R.drawable.appfunc_folder_quick_add_bar_gb_h);
			setMaxColumnNumber(AppFuncAutoFitManager.getInstance(mActivity)
					.getFolderQuickAddBarItemCountH());
		}
		super.layout(left, top, right, bottom);
		changeAllFolderAlpha(DEFAULT_ALPHA, false); //透明度为60%
	}

	public boolean onDragEnter(final int x, final int y, final XPanel dragSource,
			final XComponent dragTarget) {
		changeAllFolderAlpha(FULL_ALPHA, true); //透明度为100%
		return true;
	}

	public boolean onDragMove(final int x, final int y, final XPanel dragSource,
			final XComponent dragTarget) {

		// 对传入的坐标进行预处理，加上滚动偏移量
		int cx = x - mOffsetX;
		int cy = y - mOffsetY;

		if (dragTarget instanceof ApplicationIcon) {
			ApplicationIcon appIcon = (ApplicationIcon) dragTarget;
			if (!appIcon.isFolder()) {
				AppFuncFolderQuickAddIcon targetIcon = null;
				int count = getChildCount();
				for (int i = 0; i < count; i++) {
					XComponent child = getChildAt(i);
					if (child instanceof AppFuncFolderQuickAddIcon) {
						AppFuncFolderQuickAddIcon folderIcon = (AppFuncFolderQuickAddIcon) child;
						if (targetIcon == null && folderIcon.XYInRange(cx, cy)) {
							targetIcon = folderIcon;
							folderIcon.setAlpha(FULL_ALPHA);
							folderIcon.setIsFolderReady(true);
						}
						if (folderIcon.XYInRange(cx, cy)) {
							if (targetIcon != folderIcon) {
								targetIcon = folderIcon;
								folderIcon.setAlpha(FULL_ALPHA);
								folderIcon.setIsFolderReady(true);
							}
						} else {
							folderIcon.setIsFolderReady(false);
						}						
					}
				}
			}
		}
		if (startSwitchScreenTask(x, y)) {
			//			clearTask(CLEAR_TASK_TYPE_BACK_WORKSPACE);
			return true;
		} else {
			clearSwitchScreenTask();
			DeliverMsgManager.getInstance().onChange(AppFuncConstants.APP_FUNC_MAIN_VIEW,
					AppFuncConstants.DRAW_SIDEBG_NO, null);
		}

		//		if (targetIcon == null) {
		// 发消息通知appfuncmainview不绘制高亮图

		//		}

		return false;
	}

	private void resetAllFolderStatus() {
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			XComponent child = getChildAt(i);
			if (child instanceof AppFuncFolderQuickAddIcon) {
				AppFuncFolderQuickAddIcon folderIcon = (AppFuncFolderQuickAddIcon) child;
				folderIcon.setIsFolderReady(false);
			}
		}
	}

	private void changeAllFolderAlpha(int alpha, boolean needAnimate) {
		int count = getChildCount();
		ArrayList<AnimationInfo> animInfoList = new ArrayList<AnimationInfo>(count);
		for (int i = 0; i < count; i++) {
			XComponent child = getChildAt(i);
			if (child instanceof AppFuncFolderQuickAddIcon) {
				AppFuncFolderQuickAddIcon folderIcon = (AppFuncFolderQuickAddIcon) child;
				if (needAnimate) {
					AnimationInfo animInfo = folderIcon.changeAlpha(folderIcon.getAlpha(), alpha,
							20, null, null, true);
					animInfoList.add(animInfo);
				} else {
					folderIcon.setAlpha(alpha);
				}
			}
		}
		if (!animInfoList.isEmpty()) {
			AnimationManager.getInstance(mActivity).attachBatchAnimations(-1, animInfoList, this,
					new OrientationInvoker() {
						
						@Override
						public void resetOrientation() {
							// 该动画完成后不执行横竖屏切换
							// do nothing
						}
						
						@Override
						public void keepCurrentOrientation() {
							OrientationControl.keepCurrentOrientation(mActivity);
						}
					});
		}
	}

	public void onDragExit(final int x, final int y, final XPanel dragSource,
			final XComponent dragTarget, boolean addFolder) {
		resetAllFolderStatus();
		if (!addFolder) {
			changeAllFolderAlpha(DEFAULT_ALPHA, true); //透明度为60%
		}
		clearSwitchScreenTask();
		// 发消息通知appfuncmainview不绘制高亮图
		DeliverMsgManager.getInstance().onChange(AppFuncConstants.APP_FUNC_MAIN_VIEW,
				AppFuncConstants.DRAW_SIDEBG_NO, null);
	}

	public boolean onDrop(final int x, final int y, final XPanel dragSource,
			final XComponent dragTarget) {

		clearSwitchScreenTask();
		if (dragSource instanceof XBaseGrid && dragTarget instanceof ApplicationIcon) {
			ApplicationIcon appIcon = (ApplicationIcon) dragTarget;
			if (!appIcon.isFolder()) {
				int cx = x - mOffsetX;
				int cy = y - mOffsetY;

				AppFuncFolderQuickAddIcon targetFolderIcon = null;
				int count = getChildCount();
				for (int i = 0; i < count; i++) {
					XComponent child = getChildAt(i);
					if (child instanceof AppFuncFolderQuickAddIcon) {
						AppFuncFolderQuickAddIcon folderIcon = (AppFuncFolderQuickAddIcon) child;
						if (folderIcon.XYInRange(cx, cy)) {
							targetFolderIcon = folderIcon;
							break;
						}
					}
				}
				if (targetFolderIcon != null) {
					int folderType = ((FunFolderItemInfo) targetFolderIcon.getInfo()).getFolderType();
					switch (folderType) {
						case FunFolderItemInfo.TYPE_NEW_FOLDER :
							createNewFolder((XBaseGrid) dragSource, targetFolderIcon, appIcon);
							break;
						case FunFolderItemInfo.TYPE_NORMAL :
							addItemToNormalFolder((XBaseGrid) dragSource, targetFolderIcon, appIcon);
							break;
						case FunFolderItemInfo.TYPE_GAME :
						case FunFolderItemInfo.TYPE_SOCIAL :
						case FunFolderItemInfo.TYPE_SYSTEM :
						case FunFolderItemInfo.TYPE_TOOL :
							addItemToSpecialFolder((XBaseGrid) dragSource, targetFolderIcon, appIcon);
							break;
						default :
							break;
					}
					return true;
				}
			}
		}
		return false;
	}

	private void createNewFolder(final XBaseGrid grid, final AppFuncFolderQuickAddIcon folderIcon,
			final ApplicationIcon appIcon) {
		grid.removeDragComponent();
		grid.resetOrientation();
		grid.onCreateNewFolder((FunAppItemInfo) appIcon.getInfo());
	}

	private void addItemToNormalFolder(final XBaseGrid grid,
			final AppFuncFolderQuickAddIcon folderIcon, final ApplicationIcon appIcon) {
		grid.onAddItemToNormalFolder((FunFolderItemInfo) folderIcon.getInfo());
		grid.removeDragComponent();
		PointF point = folderIcon.getNextFolderItemPoint();
		mAnimationIcon = makeAnimationIcon(appIcon);
		mAnimationIcon.startShrink(folderIcon.mX + point.x, folderIcon.mY + point.y, mOffsetX,
				mOffsetY, folderIcon.mIsInMid, folderIcon, new IAnimateListener() {

					@Override
					public void onStart(XAnimator animator) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onProgress(XAnimator animator, int progress) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onFinish(XAnimator animator) {
						mAnimationIcon.setIsShrink(false);
						grid.mergeItemToFolder();
//						grid.resetOrientation();
					}
				});
	}

	private void addItemToSpecialFolder(final XBaseGrid grid,
			final AppFuncFolderQuickAddIcon folderIcon, final ApplicationIcon appIcon) {
		grid.removeDragComponent();
		final FunFolderItemInfo folderInfo = (FunFolderItemInfo) folderIcon.getInfo();
		final FunAppItemInfo appInfo = (FunAppItemInfo) appIcon.getInfo();
		int folderType = folderInfo.getFolderType();
		setShowSpecialFolder(folderType, false);
		PointF point = folderIcon.getNextFolderItemPoint();
		mAnimationIcon = makeAnimationIcon(appIcon);
		mAnimationIcon.startShrink(folderIcon.mX + point.x, folderIcon.mY + point.y, mOffsetX,
				mOffsetY, folderIcon, new IAnimateListener() {

					@Override
					public void onStart(XAnimator animator) {
					}

					@Override
					public void onProgress(XAnimator animator, int progress) {
					}

					@Override
					public void onFinish(XAnimator animator) {
						mAnimationIcon.setIsShrink(false);
						AppFuncHandler.getInstance().getCurrentGrid()
								.onAddItemToSpecialFolder(folderInfo, appInfo);
						grid.resetOrientation();
					}
				});
	}

	private ApplicationIcon makeAnimationIcon(ApplicationIcon appIcon) {
		ApplicationIcon animationIcon = (ApplicationIcon) appIcon.clone();
		int absX = animationIcon.getAbsX() - mOffsetX;
		int absY = animationIcon.getAbsY() - mOffsetY;
		animationIcon.setXY(absX - getAbsX(), absY - getAbsY());
		return animationIcon;
	}
	
	private void setShowSpecialFolder(int folderType, boolean isShow) {
		switch (folderType) {
			case FunFolderItemInfo.TYPE_GAME :
				((FolderQuickAddAdapter) mAdapter).setShowGameFolder(isShow);
				break;
			case FunFolderItemInfo.TYPE_SOCIAL :
				((FolderQuickAddAdapter) mAdapter).setShowSocialFolder(isShow);
				break;
			case FunFolderItemInfo.TYPE_SYSTEM :
				((FolderQuickAddAdapter) mAdapter).setShowSystemFolder(isShow);
				break;
			case FunFolderItemInfo.TYPE_TOOL :
				((FolderQuickAddAdapter) mAdapter).setShowToolFolder(isShow);
				break;
			default : 
				break;
		}
	}

	@Override
	protected boolean animate() {
		boolean ret = false;
		ret = super.animate();
		if (mAnimationIcon != null) {
			mAnimationIcon.tick();
		}
		return ret;
	}

	@Override
	public void onScrollFinish(int currentScreen) {
		super.onScrollFinish(currentScreen);
		// 发消息通知allapptabbasiccontent不绘制高亮图
		DeliverMsgManager.getInstance().onChange(AppFuncConstants.APP_FUNC_MAIN_VIEW,
				AppFuncConstants.DRAW_SIDEBG_NO, null);
	}

	@Override
	public synchronized void notify(int key, Object obj) {
		switch (key) {
			case AppFuncConstants.REFRESH_FOLDER_QUICK_ADD_BAR :
				mHandler.sendEmptyMessage(MSG_REFRESH_ALL);
				break;
			case AppFuncConstants.REFRESH_FOLDER_QUICK_ADD_BAR_NORMAL_FOLDER :
				mAnimationIcon = null;
				if (obj instanceof FunFolderItemInfo) {
					FunFolderItemInfo info = (FunFolderItemInfo) obj;
					if (info.getSize() <= 1) {
						mHandler.sendEmptyMessage(MSG_REFRESH_ALL);
					} else {
						AppFuncFolderQuickAddIcon icon = findFolderIcon(info);
						if (icon != null) {
							icon.readyData();
							changeAllFolderAlpha(DEFAULT_ALPHA, true);
						} else {
							mHandler.sendEmptyMessage(MSG_REFRESH_ALL);
						}
					}
				}
				break;
			case AppFuncConstants.REFRESH_FOLDER_QUICK_ADD_BAR_SEPCIAL_FOLDER :
				mAnimationIcon = null;
				if (obj instanceof SpecialFolderItem) {
					SpecialFolderItem item = (SpecialFolderItem) obj;
					AppFuncFolderQuickAddIcon icon = findFolderIcon(item.mType);
					if (icon != null) {
						icon.setAppInfo(item.mInfo);
						changeAllFolderAlpha(DEFAULT_ALPHA, true);
					} else {
						mHandler.sendEmptyMessage(MSG_REFRESH_ALL);
					}
				}
				break;
			case AppFuncConstants.SET_SPECIAL_FOLDER_DISMISS : 
				if (obj instanceof Integer) {
					int folderType = ((Integer) obj).intValue();
					setShowSpecialFolder(folderType, false);
				}
				break;
			default :
				break;
		}
	}

	@Override
	public void initAdapter(Activity activity) {
		mAdapter = new FolderQuickAddAdapter(activity);
	}

	@Override
	protected void drawCurrentFrame(Canvas canvas) {
		if (mBg != null) {
			ImageUtil.drawImage(canvas, mBg, ImageUtil.STRETCHMODE, mX, mY, mX + mWidth, mY
					+ mHeight, null);
		}
		super.drawCurrentFrame(canvas);

		if (mAnimationIcon != null) {
			mAnimationIcon.paintCurrentFrame(canvas, mAnimationIcon.mX, mAnimationIcon.mY);
		}
	}

	@Override
	public synchronized void removeComponent(int index) throws IllegalArgumentException {
		XComponent component = getChildAt(index);
		if (component != null) {
			AppFuncFolderQuickAddIcon icon = (AppFuncFolderQuickAddIcon) component;
			icon.unRegister();
		}
		super.removeComponent(index);
	}

	@Override
	public synchronized void removeComponent(XComponent component) throws IllegalArgumentException {
		if (component != null) {
			AppFuncFolderQuickAddIcon icon = (AppFuncFolderQuickAddIcon) component;
			icon.unRegister();
		}
		super.removeComponent(component);
	}

	@Override
	public synchronized void removeAllComponent() throws IllegalArgumentException {
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			XComponent component = getChildAt(i);
			if (component != null) {
				AppFuncFolderQuickAddIcon icon = (AppFuncFolderQuickAddIcon) component;
				icon.unRegister();
			}
		}
		super.removeAllComponent();
	}

	@Override
	public void onStart(int what, Object[] params) {

	}

	@Override
	public void onFinish(int what, Object[] params) {

	}
}
