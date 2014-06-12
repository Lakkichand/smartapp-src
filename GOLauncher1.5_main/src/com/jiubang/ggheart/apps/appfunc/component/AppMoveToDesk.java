package com.jiubang.ggheart.apps.appfunc.component;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.jiubang.core.mars.IAnimateListener;
import com.jiubang.core.mars.XALinear;
import com.jiubang.core.mars.XAnimator;
import com.jiubang.core.mars.XComponent;
import com.jiubang.core.mars.XPanel;
import com.jiubang.ggheart.apps.appfunc.common.component.AppSingleLineContainer;
import com.jiubang.ggheart.apps.appfunc.timer.ITask;
import com.jiubang.ggheart.apps.appfunc.timer.Scheduler;
import com.jiubang.ggheart.apps.desks.appfunc.XBaseGrid;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.model.AppFuncFolderInfoToDesk;
import com.jiubang.ggheart.apps.desks.appfunc.model.AppFuncScreenIconAdapter;
import com.jiubang.ggheart.apps.desks.appfunc.model.AppFuncScreenItemInfo;
import com.jiubang.ggheart.apps.desks.appfunc.model.DeliverMsgManager;
import com.jiubang.ggheart.apps.desks.appfunc.model.IMsgHandler;
import com.jiubang.ggheart.apps.desks.appfunc.model.UIThreadHandler;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.components.DeskToast;
import com.jiubang.ggheart.data.info.FunAppItemInfo;
import com.jiubang.ggheart.data.info.FunFolderItemInfo;
import com.jiubang.ggheart.data.info.IItemType;
import com.jiubang.ggheart.data.info.ItemInfo;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.data.info.UserFolderInfo;
import com.jiubang.ggheart.data.statistics.StatisticsData;
/**
 * 
 * <br>类描述:功能表编辑状态时底部的屏幕预览区域组件
 * <br>功能详细描述:
 * 
 * @author  huangshaotao
 * @date  [2012-9-25]
 */
public class AppMoveToDesk extends AppSingleLineContainer
		implements IMsgHandler {
	private static final int BACK_TO_WORKSPACE_DELAY = 1000;
	private static final int RESTORE_SCREEN_STATUS_DELAY = 300;
	private static final int ITEM_SIZE = 65;
	/**
	 * 清除回到桌面定时器
	 */
	public static final int CLEAR_TASK_TYPE_BACK_WORKSPACE = 1;
	/**
	 * 清除移动到屏幕预览区下一屏定时器
	 */
	public static final int CLEAR_TASK_TYPE_GO_ANOTHER_GOROUP = 2;
	/**
	 * 清除当前所有定时器
	 */
	public static final int CLEAR_TASK_TYPE_ALL = 3;

	/**
	 * 用于判断拖拽的图标是否已经进入屏幕预览区域，会随着图标的缩放而缩放，默认状态和组件自身一样大小，缩放状态以放大倍数最的图标为准。
	 */
	private Rect mScaleRegion = null;
	
	private int mMaxScaleY = AppFuncScreenIcon.MAXSCALE_Y;
	/**
	 * 当前缩放因子，为0则不缩放
	 */
	private float mCurrentScaleFactory = 0;

	private BackToWorkspaceTask mBackToWorkspaceTask = null;
	private ApplicationIcon mAnimationIcon;
	private boolean mScaleAnimationFinish = false;
	private boolean mMoveAnimationFinish = false;
	private Drawable mBg;

	/**
	 * 高度由内部自适应，外部传入参数无效
	 * 
	 * @param activity
	 * @param tickCount
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public AppMoveToDesk(Activity activity, int tickCount, int x, int y, int width, int height) {
		super(activity, tickCount, x, y, width, height, HIDE_TYPE_BOTTOM);
		setItemSize(mUtils.getStandardSize(ITEM_SIZE));
		mMaxScaleY = mUtils.getStandardSize(42);
		
		mScaleRegion = new Rect();

		// 注册消息监听器
		DeliverMsgManager.getInstance().registerMsgHandler(AppFuncConstants.APP_FUNC_APPTODESK,
				this);
		DeliverMsgManager.getInstance().registerMsgHandler(
				AppFuncConstants.SYSTEM_CONFIGURATION_CHANGED, this);
	}

	@Override
	protected void drawCurrentFrame(Canvas canvas) {
//		if (mBg != null) {
//			ImageUtil.drawImage(canvas, mBg, ImageUtil.STRETCHMODE, mX, mY, mX + mWidth, mY
//					+ mHeight, null);
//		}
		super.drawCurrentFrame(canvas);
		if (mAnimationIcon != null) {
			mAnimationIcon.paintCurrentFrame(canvas, mAnimationIcon.mX, mAnimationIcon.mY);
		}
	}

	@Override
	public void layout(int left, int top, int right, int bottom) {
		if (GoLauncher.isPortait()) {
			mBg = mActivity.getResources()
					.getDrawable(R.drawable.appfunc_app_move_to_desk_gb_v);
		} else {
			mBg = mActivity.getResources()
					.getDrawable(R.drawable.appfunc_app_move_to_desk_gb_h);
		}
		super.layout(left, top, right, bottom);
		mScaleRegion.set(getAbsX(), getAbsY(), getAbsX() + mWidth, getAbsY() + mHeight);
	}

	public boolean onDragMove(final int x, final int y, final XPanel dragSource,
			final XComponent dragTarget) {

		// 对传入的坐标进行预处理，加上滚动偏移量
		int cx = x - mOffsetX;
		int cy = y - mOffsetY;

		scaleScreenIcons(cx, cy);

		AppFuncScreenIcon targetIcon = null;
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			XComponent child = getChildAt(i);
			if (child instanceof AppFuncScreenIcon) {
				AppFuncScreenIcon screenIcon = (AppFuncScreenIcon) child;
				if (targetIcon == null && screenIcon.XYInRange(cx, cy)) {
					screenIcon.setPress(true);
					targetIcon = screenIcon;
				} else {
					screenIcon.setPress(false);
				}
			}
		}

		if (startSwitchScreenTask(x, y)) {
			clearTask(CLEAR_TASK_TYPE_BACK_WORKSPACE);
			return true;
		} else {
			clearTask(CLEAR_TASK_TYPE_GO_ANOTHER_GOROUP);
		}

		if (targetIcon != null) {
			if (mBackToWorkspaceTask == null) {
				createBackToWorkspaceTask(dragSource, targetIcon);
				return true;
			} else {
				if (targetIcon.getInfo().mIndex != mBackToWorkspaceTask.mScreenIndex) {
					clearTask(CLEAR_TASK_TYPE_BACK_WORKSPACE);
					createBackToWorkspaceTask(dragSource, targetIcon);
					return true;
				}
			}
		}
		// else if (goToAnotherScreenIconGroup(x, y)) {
		// clearTask(CLEAR_TASK_TYPE_BACK_WORKSPACE);
		// return true;
		// }
		else {
			// clearTask(CLEAR_TASK_TYPE_ALL);
			clearTask(CLEAR_TASK_TYPE_BACK_WORKSPACE);
			// 发消息通知appfuncmainview不绘制高亮图
			DeliverMsgManager.getInstance().onChange(AppFuncConstants.APP_FUNC_MAIN_VIEW,
					AppFuncConstants.DRAW_SIDEBG_NO, null);
		}

		return false;
	}

	private void createBackToWorkspaceTask(final XPanel dragSource, AppFuncScreenIcon targetIcon) {
		mBackToWorkspaceTask = new BackToWorkspaceTask();
		long time = mScheduler.getClock().getTime();
		mBackToWorkspaceTask.mMsgHandler = (IMsgHandler) dragSource;
		mBackToWorkspaceTask.mScreenIndex = targetIcon.getInfo().mIndex;
		mBackToWorkspaceTask.mTaskId = mScheduler.schedule(Scheduler.TASK_TIME, time
				+ BACK_TO_WORKSPACE_DELAY, 100, 75, mBackToWorkspaceTask, null);
	}

	private void scaleScreenIcons(int x, int y) {
		int avgDistance = 0;
		if (mUtils.isVertical()) {
			avgDistance = mIconWidth + mSpaceWidth;
		} else {
			avgDistance = mIconHeight + mSpaceWidth;
		}
		avgDistance = 2 * avgDistance;
		int count = getChildCount();
		float maxScale = 0.0f;
		for (int i = 0; i < count; i++) {
			XComponent child = getChildAt(i);
			if (child instanceof AppFuncScreenIcon) {
				AppFuncScreenIcon screenIcon = (AppFuncScreenIcon) child;
				int[] screenCenterPoint = screenIcon.getCenterPoint();
				int distance = 0;
				if (mUtils.isVertical()) {
					distance = Math.abs(x - screenCenterPoint[0]);
				} else {
					distance = Math.abs(y - screenCenterPoint[1]);
				}
				if (distance < avgDistance) {
					float scale = 1.0f - 1.0f * distance / avgDistance;
					screenIcon.scale(scale);
					if (scale > maxScale) {
						maxScale = scale;
					}
				} else {
					screenIcon.scale(0.0f);
				}
			}
		}
		updateScaleRegion(maxScale);
	}

	private void scaleScreenIcons(float scale) {
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			XComponent child = getChildAt(i);
			if (child instanceof AppFuncScreenIcon) {
				AppFuncScreenIcon screenIcon = (AppFuncScreenIcon) child;
				screenIcon.scale(scale);
			}
		}
		updateScaleRegion(scale);
	}

	public void onDragExit(final int x, final int y, final XPanel dragSource,
			final XComponent dragTarget, boolean addToWorkspaceSuccess) {
		clearTask(CLEAR_TASK_TYPE_ALL);
		if (!addToWorkspaceSuccess) {
			resetAllScreenIconStatus();
		}
		// 发消息通知appfuncmainview不绘制高亮图
		DeliverMsgManager.getInstance().onChange(AppFuncConstants.APP_FUNC_MAIN_VIEW,
				AppFuncConstants.DRAW_SIDEBG_NO, null);
	}

	public boolean onDrop(final int x, final int y, final XPanel dragSource,
			final XComponent dragTarget) {

		clearTask(CLEAR_TASK_TYPE_ALL);

		// 对传入的坐标进行预处理，加上滚动偏移量
		int cx = x - mOffsetX;
		int cy = y - mOffsetY;

		// onDragExit(x,y,dragSource,dragTarget);
		AppFuncScreenIcon targetIcon = null;
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			XComponent child = getChildAt(i);
			if (child instanceof AppFuncScreenIcon) {
				AppFuncScreenIcon screenIcon = (AppFuncScreenIcon) child;
				if (screenIcon.XYInRange(cx, cy)) {
					targetIcon = screenIcon;
					break;
				}
			}
		}
		if (targetIcon != null) {
			final AppFuncScreenItemInfo info = targetIcon.getInfo();
			if (info != null) {
				int screenIndex = info.mIndex;
				int[] vancantCount = new int[1];
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
						IDiyMsgIds.SCREEN_GET_VANCANT_COUNT, screenIndex, vancantCount, null);
				if (vancantCount[0] > 0) {
					// 添加图标到该屏幕
					if (dragTarget != null && dragTarget instanceof ApplicationIcon) {
						IMsgHandler msgHandler = null;
						if (dragSource instanceof IMsgHandler) {
							msgHandler = (IMsgHandler) dragSource;
						}
						lockScreen(msgHandler);
						ItemInfo itemInfo = null;
						ApplicationIcon appIcon = (ApplicationIcon) dragTarget;
						mAnimationIcon = (ApplicationIcon) appIcon.clone();
						int absX = mAnimationIcon.getAbsX();
						int absY = mAnimationIcon.getAbsY();
						mAnimationIcon.setXY(absX - getAbsX(), absY - getAbsY());
						int[] screenRelativeCoordinate = new int[2];
						screenRelativeCoordinate[0] = targetIcon.getScreenRelativeX(cx
								- targetIcon.getAbsX());
						screenRelativeCoordinate[1] = targetIcon.getScreenRelativeY(cy
								- targetIcon.getAbsY());
						if (appIcon.isFolder()) {
							itemInfo = createFolderInfo(appIcon);
						} else {
							itemInfo = createShortCutInfo(appIcon);
						}
						List<ItemInfo> list = new ArrayList<ItemInfo>(1);
						list.add(itemInfo);
						GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
								IDiyMsgIds.APPDRAWER_ADD_ITEM_TO_SCREEN, screenIndex,
								screenRelativeCoordinate, list);
						int[] targetRelativeCenterPoint = screenRelativeCoordinate;
						int animationTargetX = (int) (targetIcon.getAbsX() + mOffsetX
								+ targetIcon.mCurrentPaddingH + targetRelativeCenterPoint[0]
								* targetIcon.getRelativeHScale() - mAnimationIcon.getWidth() / 2 - getAbsX());
						int animationTargetY = (int) (targetIcon.getAbsY() + mOffsetY
								+ targetIcon.mCurrentPaddingV + targetRelativeCenterPoint[1]
								* targetIcon.getRelativeVScale() - mAnimationIcon.getHeight() / 2 - getAbsY());
						final IMsgHandler finalMsgHandler = msgHandler;
						final AppFuncScreenIcon finalTargetIcon = targetIcon;
						mAnimationIcon.changeScale(-1, -1, targetIcon.getRelativeHScale(),
								targetIcon.getRelativeVScale(), new IAnimateListener() {

									@Override
									public void onStart(XAnimator animator) {
										mScaleAnimationFinish = false;
										if (mAnimationIcon.mIsEnlarged) {
											mAnimationIcon.mIsEnlarged = false;
										}

									}

									@Override
									public void onProgress(XAnimator animator, int progress) {
									}

									@Override
									public void onFinish(XAnimator animator) {
										if (mAnimationIcon != null) {
											synchronized (mAnimationIcon) {
												mScaleAnimationFinish = true;
												onAddToWorkspaceFinish(finalTargetIcon,
														finalMsgHandler);
											}
										}
									}
								});
						mAnimationIcon.moveIcon(XALinear.class, XALinear.XALINEAR_ECSPEED,
								mAnimationIcon.mX, mAnimationIcon.mY, animationTargetX,
								animationTargetY, 40, 1, 1, new IAnimateListener() {

									@Override
									public void onStart(XAnimator animator) {
										mMoveAnimationFinish = false;
									}

									@Override
									public void onProgress(XAnimator animator, int progress) {
									}

									@Override
									public void onFinish(XAnimator animator) {
										if (mAnimationIcon != null) {
											synchronized (mAnimationIcon) {
												mMoveAnimationFinish = true;
												onAddToWorkspaceFinish(finalTargetIcon,
														finalMsgHandler);
											}
										}
										//用户行为统计
										StatisticsData.countUserActionData(StatisticsData.FUNC_ACTION_ID_APPLICATION,
												StatisticsData.USER_ACTION_FIVE,
												IPreferencesIds.APP_FUNC_ACTION_DATA);
									}
								}, null, false);
						return true;
					}
				} else {
					DeskToast.makeText(mActivity, R.string.no_enough_room, Toast.LENGTH_SHORT)
							.show();
					resetAllScreenIconStatus();
					return true;
				}
			}
		}
		return false;
	}

	private void onAddToWorkspaceFinish(AppFuncScreenIcon targetIcon, IMsgHandler msgHandler) {
		if (mScaleAnimationFinish && mMoveAnimationFinish) {
			if (mAnimationIcon != null) {
				mAnimationIcon.close();
				mAnimationIcon = null;
			}
			if (targetIcon != null) {
				targetIcon.setPress(false);
				((AppFuncScreenIconAdapter) mAdapter).reloadApp(targetIcon.getInfo());
			}
			
			long time = mScheduler.getClock().getTime();
			mScheduler.schedule(Scheduler.TASK_TIME, time + RESTORE_SCREEN_STATUS_DELAY, 100, 75,
					new ITask() {

						@Override
						public void finish(long id, long time, Object userName) {

						}

						@Override
						public void execute(long id, long time, Object userName) {
							resetAllScreenIconStatus();
						}
					}, null);
			unlockScreen(msgHandler);
		}
	}

	public void resetAllScreenIconStatus() {
		scaleScreenIcons(0.0f);
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			XComponent child = getChildAt(i);
			if (child instanceof AppFuncScreenIcon) {
				AppFuncScreenIcon screenIcon = (AppFuncScreenIcon) child;
				screenIcon.setPress(false);
				screenIcon.setLockScreen(false);
			}
		}
	}

	private void lockScreen(IMsgHandler msgHandler) {
		if (msgHandler != null) {
			msgHandler.notify(AppFuncConstants.ADD_ITEM_TO_WORKSPACE_START, null);
		}
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			XComponent child = getChildAt(i);
			if (child instanceof AppFuncScreenIcon) {
				AppFuncScreenIcon screenIcon = (AppFuncScreenIcon) child;
				screenIcon.setLockScreen(true);
			}
		}
	}

	private void unlockScreen(IMsgHandler msgHandler) {
		if (msgHandler != null) {
			msgHandler.notify(AppFuncConstants.ADD_ITEM_TO_WORKSPACE_FINISH, null);
		}
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			XComponent child = getChildAt(i);
			if (child instanceof AppFuncScreenIcon) {
				AppFuncScreenIcon screenIcon = (AppFuncScreenIcon) child;
				screenIcon.setLockScreen(false);
			}
		}
	}

	private ShortCutInfo createShortCutInfo(ApplicationIcon appIcon) {
		ShortCutInfo shortCutInfo = new ShortCutInfo();
		shortCutInfo.mId = -1;
		shortCutInfo.mInScreenId = -1;
		shortCutInfo.mIcon = appIcon.getIcon();
		shortCutInfo.mIntent = appIcon.getIntent();
		shortCutInfo.mItemType = IItemType.ITEM_TYPE_APPLICATION;
		shortCutInfo.mTitle = appIcon.getTitle();
		shortCutInfo.setRelativeItemInfo(((FunAppItemInfo) appIcon.getInfo()).getAppItemInfo());
		return shortCutInfo;
	}

	private UserFolderInfo createFolderInfo(ApplicationIcon appIcon) {
		FunFolderItemInfo funFolderInfo = (FunFolderItemInfo) (appIcon.getInfo());
		AppFuncFolderInfoToDesk folderToDesk = new AppFuncFolderInfoToDesk(funFolderInfo);
		UserFolderInfo folderInfo = folderToDesk.toUserFolderInfo();
		return folderInfo;
	}

	private void clearTask(int type) {
		switch (type) {
			case CLEAR_TASK_TYPE_BACK_WORKSPACE : {
				if (mBackToWorkspaceTask != null) {
					mScheduler.terminate(mBackToWorkspaceTask.mTaskId);
				}
				break;
			}
			case CLEAR_TASK_TYPE_GO_ANOTHER_GOROUP : {
				clearSwitchScreenTask();
				break;
			}
			case CLEAR_TASK_TYPE_ALL : {

				if (mBackToWorkspaceTask != null) {
					mScheduler.terminate(mBackToWorkspaceTask.mTaskId);
				}
				clearSwitchScreenTask();
				break;

			}
		}
	}
	/**
	 * 
	 * <br>类描述:回到桌面定时任务
	 * <br>功能详细描述:
	 * 
	 * @author  
	 * @date  [2012-9-25]
	 */
	class BackToWorkspaceTask implements ITask {
		public long mTaskId;
		public IMsgHandler mMsgHandler;
		public int mScreenIndex;

		@Override
		public void execute(long id, long time, Object object) {
			if (XBaseGrid.sIsExit) {
				return;
			}
			Message message = Message.obtain();
			message.what = AppFuncConstants.EXITAPPFUNCFROMHOME;
			message.obj = mMsgHandler;
			message.arg1 = mScreenIndex;
			UIThreadHandler.getInstance().sendMessage(message);
			//用户行为统计
			StatisticsData.countUserActionData(StatisticsData.FUNC_ACTION_ID_APPLICATION,
					StatisticsData.USER_ACTION_SIX,
					IPreferencesIds.APP_FUNC_ACTION_DATA);
		}

		@Override
		public void finish(long id, long time, Object object) {
			mBackToWorkspaceTask = null;
		}
	}

	@Override
	protected boolean animate() {
		boolean ret = false;
		ret = super.animate();

		if (mMotion != null && (mScaleRegion.left != getAbsX() || mScaleRegion.top != getAbsY())) {
			mScaleRegion.set(getAbsX(), getAbsY(), getAbsX() + mWidth, getAbsY() + mHeight);
		}
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

	/**
	 * ----------------------------------分屏滚动监听器接口实现方法结束------------------------
	 * -----------------------------
	 */

	@Override
	public void notify(int key, Object obj) {
		// TODO Auto-generated method stub
		switch (key) {
			case AppFuncConstants.SCREEN_LOAD_FINISH :
				mAdapter.loadApp();
				requestLayout();
				break;
			case AppFuncConstants.SYSTEM_CONFIGURATION_CHANGED :
				mScroller.gotoScreen(mCurScreen, -1, true);
			default :
				break;
		}
	}

	//CHECKSTYLE:OFF
	@Override
	public boolean XYInRange(int x, int y) {
		if ((x >= mScaleRegion.left) && (x <= mScaleRegion.right)) {
			if ((y >= mScaleRegion.top) && (y <= mScaleRegion.bottom)) {
				return true;
			}
		}
		return false;
	}
	//CHECKSTYLE:ON
	
	@Override
	public void setXY(int i, int j) {
		super.setXY(i, j);
		mScaleRegion.set(getAbsX(), getAbsY(), getAbsX() + mWidth, getAbsY() + mHeight);
	}

	/**
	 * 根据缩放因子缩放屏幕预览区域（不缩放组件本身，只缩放其判断区域）
	 * 
	 * @param scalefactory
	 *            放大最多的屏幕缩略图的缩放因子（值在0-1之间，0不放大，1放到最大）
	 */
	private void updateScaleRegion(float scalefactory) {
		if (mCurrentScaleFactory == scalefactory) {
			return;
		}
		mCurrentScaleFactory = scalefactory;
		int offset = 0;
		if (mUtils.isVertical()) {
			offset = Math.round(scalefactory * mMaxScaleY);
			mScaleRegion
					.set(getAbsX(), getAbsY() - offset, getAbsX() + mWidth, getAbsY() + mHeight);
		} else {
			offset = Math.round(scalefactory * mMaxScaleY);
			mScaleRegion
					.set(getAbsX() - offset, getAbsY(), getAbsX() + mWidth, getAbsY() + mHeight);
		}

	}
	
	@Override
	public void initAdapter(Activity activity) {
		mAdapter = new AppFuncScreenIconAdapter(activity);
	}

}
