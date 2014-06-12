package com.jiubang.ggheart.apps.desks.appfunc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.AppUtils;
import com.go.util.Utilities;
import com.go.util.animation.AnimationFactory;
import com.go.util.file.media.AudioFile;
import com.go.util.file.media.FileInfo;
import com.go.util.file.media.ImageFile;
import com.go.util.file.media.ThumbnailManager;
import com.go.util.file.media.VideoFile;
import com.go.util.graphics.effector.united.CoupleScreenEffector;
import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.core.mars.EventType;
import com.jiubang.core.mars.IComponentEventListener;
import com.jiubang.core.mars.XComponent;
import com.jiubang.core.mars.XPanel;
import com.jiubang.ggheart.appgame.appcenter.component.AppsManagementActivity;
import com.jiubang.ggheart.appgame.base.component.MainViewGroup;
import com.jiubang.ggheart.apps.appfunc.component.AllAppTabBasicContent;
import com.jiubang.ggheart.apps.appfunc.component.AppFuncFolder;
import com.jiubang.ggheart.apps.appfunc.component.AppFuncFolderQuickAddBar;
import com.jiubang.ggheart.apps.appfunc.component.AppFuncTabBasicContent;
import com.jiubang.ggheart.apps.appfunc.component.AppFuncTabSingleTitle;
import com.jiubang.ggheart.apps.appfunc.component.ApplicationIcon;
import com.jiubang.ggheart.apps.appfunc.component.ProManageEditDock;
import com.jiubang.ggheart.apps.appfunc.component.ProManageTabBasicContent;
import com.jiubang.ggheart.apps.appfunc.component.RecentAppTabBasicContent;
import com.jiubang.ggheart.apps.appfunc.component.RecentAppsIcon;
import com.jiubang.ggheart.apps.appfunc.controler.AppDrawerControler;
import com.jiubang.ggheart.apps.appfunc.controler.FunControler;
import com.jiubang.ggheart.apps.appfunc.controler.IndexFinder;
import com.jiubang.ggheart.apps.appfunc.setting.AppFuncAutoFitManager;
import com.jiubang.ggheart.apps.appfunc.setting.FunAppSetting;
import com.jiubang.ggheart.apps.appfunc.theme.AppFuncThemeController;
import com.jiubang.ggheart.apps.appfunc.timer.ITask;
import com.jiubang.ggheart.apps.appfunc.timer.Scheduler;
import com.jiubang.ggheart.apps.appmanagement.component.AppsManageView;
import com.jiubang.ggheart.apps.desks.Preferences.FunAppUISettingMainActivity;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncTabComponent.OnSameTabClickListener;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncTabComponent.OnTabContentChangeListener;
import com.jiubang.ggheart.apps.desks.appfunc.handler.AppFuncHandler;
import com.jiubang.ggheart.apps.desks.appfunc.handler.FuncAppDataHandler;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUtils;
import com.jiubang.ggheart.apps.desks.appfunc.menu.AppFuncAllAppMenuItemInfo;
import com.jiubang.ggheart.apps.desks.appfunc.model.DeliverMsgManager;
import com.jiubang.ggheart.apps.desks.appfunc.model.IMsgHandler;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.font.FontBean;
import com.jiubang.ggheart.components.DeskToast;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.DatabaseException;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.data.info.FunAppItemInfo;
import com.jiubang.ggheart.data.info.FunFolderItemInfo;
import com.jiubang.ggheart.data.info.ScreenSettingInfo;
import com.jiubang.ggheart.data.statistics.AppRecommendedStatisticsUtil;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.plugin.mediamanagement.MediaCommonMenu;
import com.jiubang.ggheart.plugin.mediamanagement.MediaDialog;
import com.jiubang.ggheart.plugin.mediamanagement.MediaPluginFactory;
import com.jiubang.ggheart.plugin.mediamanagement.inf.AppFuncContentTypes;
import com.jiubang.ggheart.plugin.mediamanagement.inf.IMediaManager;
import com.jiubang.ggheart.plugin.mediamanagement.inf.IMediaUIManager;
/**
 * 
 * <br>类描述:功能表引擎ui组件的开始节点，功能表中所有ui组件都被直接或间接的加入到了一颗由该类为根节点的ui组件树结构中。
 * <br>功能详细描述:
 * 
 * @author  
 * @date  [2012-9-4]
 */
public class AppFuncMainView extends XPanel implements IMsgHandler, IComponentEventListener {

	public static final int MOTION_TYPE_OPEN = 1;
	public static final int MOTION_TYPE_CLOSE = 2;
	/**
	 * 文件夹动画总时长
	 */
	public static final long TOTAL_MOTION_TIME = 225;
	/**
	 * 文件夹打开后文件夹图标到文件夹垂直距离
	 */
	public static final int FOLDER_ICON_OFFSET = 8;

	private Activity mActivity;
	private AppFuncUtils mUtils;

	boolean mIsEditModeDisabled;
	/**
	 * 文件夹
	 */
	private AppFuncFolder mFolder;

	/**
	 * 是否需要重绘
	 */
	private boolean mIsRePaint;

	/**
	 * 是否在menu菜单打开了功能表设置
	 */
	public boolean mOpenFuncSetting;

	/**
	 * 被抓起图标当前的X轴坐标
	 */
	private int mDragX;
	/**
	 * 被抓起图标当前的轴坐标
	 */
	private int mDragY;

	/**
	 * 是否有文件夹中的应用程序被拖拽
	 */
	private boolean mIsIconInFolderDragged;

	/**
	 * 文件夹打开时功能表的背景颜色
	 */
	private int mFolderOpenBgColor;

	/**
	 * 当前被聚焦的Tab索引
	 */
	private int mFocusedIndex;

	/**
	 * 主题控制器
	 */
	private AppFuncThemeController mThemeCtrl;

	/**
	 * tab 组件
	 */
	private AppFuncTabComponent mTabComponent;

	/**
	 * all app 组件
	 */
	private AllAppTabBasicContent mAllAppContent;

	/**
	 * recent app 组件
	 */
	private RecentAppTabBasicContent mRecentAppContent;

	/**
	 * pro manage app 组件
	 */
	private ProManageTabBasicContent mProManageAppContent;

	/**
	 * 没有焦点状态的默认值
	 */
	private final static int NOFOCUS = -2;

	/**
	 * 当前焦点不在tab上
	 */
	private final static int TABNOFOCUS = -1;

	/**
	 * tab栏的最大索引数字
	 */
	private int mTabCountMaxIndex;
	/**
	 * 文件夹是否显示
	 */
	private boolean mIsFolderShow;
	/**
	 * 图标特效类型
	 */
	private int mIconEffect;

	public static final int TABID_ALL = 0;
	public static final int TABID_RECENT = 1;
	public static final int TABID_RUNNING = 2;
	/**
	 * 被打开的文件夹的图标
	 */
	private ApplicationIcon mSrcIcon = null;
	private Intent mSrcFolderIntent = null;
	/**
	 * 当前截图缓存图片
	 */
	private Bitmap mCurrentCache = null;
	/**
	 * 当前截图缓存图片画笔
	 */
	private Paint mCachePaint = null;
	/**
	 * 当前截图缓存图片画笔
	 */
	private Paint mFolderCachePaint = null;
	/**
	 * 打开文件夹时切割图片的开始位置
	 */
	private int mClipStartPos = 0;
	/**
	 * 打开文件夹时切割图片的开始位置上半部分的移动距离
	 */
	private int mMoveUp = 0;
	/**
	 * 打开文件夹时切割图片的开始位置下半部分的移动距离
	 */
	private int mMoveDown = 0;

	/**
	 * 动画
	 */
	private boolean mMotion;

	private int mMotionType = MOTION_TYPE_CLOSE;

	private int mCurrentStep = 0;
	private int mTotalStep = 1;
	private int mSpeed = 30;

	// private Drawable mFadeDrawable = null;
	// 文件夹的位置和宽度，主要是横屏模式需要这个参数
	private int mFolderX = 0;
	private int mFolderY = 0;
	private int mFolderW = 0;
	/**
	 * 文件夹打开时绘制在文件夹底部的半透明颜色
	 */
	private boolean mDrawFade = false;
	// private int mFadeColor = 0x4c000000;
	/**
	 * 功能表网格的滚动距离
	 */
	private int mGridOffset = 0;

	/**
	 * 文件夹缓存图片（用于动画时绘制）
	 */
	private Bitmap mFolderCache = null;

	private long mMotionStartTime = 0;

	private boolean mIsDrawSideEditBg = false;
	private Drawable mSideEditBg = null; // 屏幕预览区缩略图越界背景图片

	private XComponent mDragView = null;

	private boolean mInMediaManagement = false;
	/**
	 * 文件夹打开渐变的alpha值;
	 */
	private int mAlpha = 255;
	/**
	 * 文件夹打开每次渐变alpha值;
	 */
	private int mAlphaGradient = 18;

	private boolean mIsPapreClose;
	/**
	 * 是否从文件夹中拖动图标到功能表（造成移位）
	 */
	protected boolean mIsAddCellComponent = false;

	/**
	 * 是否关闭后开启另一个
	 */
	private boolean mIsCloseAndOpenFolder = false;
	private FunFolderItemInfo mFolderToShow;
	/**
	 * 文件夹中需要删除的图标信息
	 */
	private FunAppItemInfo mRemovedItemInfo = null;
	protected boolean mAddCellComponentDirectly;

	private XPanel mMediaManagementPanel;
	
	private AsyncTask<Object, Void, Object> mInitMediaPluginTask;
	private boolean mReadyShowFolder;
	private Handler mHandler;
	
	public AppFuncMainView(Activity activity, int tickCount, int x, int y, int width, int height) {
		super(tickCount, x, y, 0, 0);
		mActivity = activity;
		AppFuncHandler.getInstance().setAppFuncMainView(this, mActivity);
		setupTabCompoent(mActivity);

		setLayout(null);

		mThemeCtrl = AppFuncFrame.getThemeController();
		mUtils = AppFuncUtils.getInstance(mActivity);
		DeliverMsgManager.getInstance().registerMsgHandler(AppFuncConstants.TABCOMPONENT, this);
		DeliverMsgManager.getInstance().registerMsgHandler(AppFuncConstants.APP_FUNC_MAIN_VIEW,
				this);
		DeliverMsgManager.getInstance().registerDispenseMsgHandler(AppFuncConstants.LOADTHEMERES,
				this);
		mFocusedIndex = NOFOCUS; // 表示第一次进入功能表没有默认聚焦. -1表示当前聚焦不在Tab上
		mIsFolderShow = false;

		mCachePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mFolderCachePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

		mSideEditBg = mActivity.getResources().getDrawable(R.drawable.appfunc_allapp_crossover);
		setChildrenDrawnWithCacheEnabled(true);
		initHandler();
	}

	/**
	 * 启动或者横竖屏切换后重新布局
	 * 
	 * @param width
	 * @param height
	 */
	@Override
	public void layout(int left, int top, int right, int bottom) {

		setPosition(left, top, right, bottom);
		// if (mFadeDrawable!=null) {
		// mFadeDrawable.setBounds(0, 0, mWidth, mHeight);
		// }
		// 1 对焦点索引的改变
		// 2 对tabcomponent的布局
		if (mTabComponent != null && mStoreComponent.contains(mTabComponent)) {
			if (mUtils.isVertical()) {
				AppFuncTabSingleTitle tab = (AppFuncTabSingleTitle) (mTabComponent.getFirstTab());
				if (tab.mId != AppFuncConstants.ALLAPPS) {
					mTabComponent.changeComponentLayout();
					if (mFocusedIndex == 0) {
						mFocusedIndex = mTabCountMaxIndex;
					} else if (mFocusedIndex == mTabCountMaxIndex) {
						mFocusedIndex = 0;
					}
				}
			} else {
				AppFuncTabSingleTitle tab = (AppFuncTabSingleTitle) (mTabComponent.getFirstTab());
				if (tab.mId != AppFuncConstants.PROCESSMANAGEMENT) {
					mTabComponent.changeComponentLayout();
					if (mFocusedIndex == 0) {
						mFocusedIndex = mTabCountMaxIndex;
					} else if (mFocusedIndex == mTabCountMaxIndex) {
						mFocusedIndex = 0;
					}
				}
			}

			mTabComponent.layout(left, top, right, bottom);
		}
		if (mMediaManagementPanel != null && mStoreComponent.contains(mMediaManagementPanel)) {
			mMediaManagementPanel.layout(left, top, right, bottom);
		}

		if (isFolderShow()) {
			handleFolderParames();
			prepareForOpenFolder(true);
			mFolder.setFolderWidth(mFolderW);
			mFolder.layout();

			initFolderValues();
			//			mFolder.setIsRebuildBg(true);
			if (mSrcIcon != null) {
				// mFolder.setXY(mFolder.mX, mClipStartPos-mMoveUp);
				mFolder.setXY(mFolderX, mClipStartPos - mMoveUp);
			} else {
				mFolder.setXY(mFolderX, 0);
			}

			if (mCurrentCache != null && !mCurrentCache.isRecycled()) {
				mCurrentCache.recycle();
				mCurrentCache = null;
			}
			boolean temp = mIsFolderShow;
			mIsFolderShow = false;

			// mCurrentCache = XViewFrame.getInstance().buildCache();
			mCurrentCache = buildCache();
			mIsFolderShow = temp;
			addComponent(mFolder);
		}
	}

	@Override
	protected void drawCurrentFrame(Canvas canvas) {
		// 执行动画
		doAnimate();

		if (isFolderShow() && mFolder != null) {
			// if (mDrawFade&&mFadeDrawable!=null) {
			// canvas.drawColor(mFolderOpenBgColor);
			// mFadeDrawable.draw(canvas);
			// }
			canvas.drawColor(mFolderOpenBgColor);
			if (mFolder.getGrid().isInDragStatus()) {
				// 绘制被裁剪的图标
				drawFolderCover(canvas);
				// 绘制文件夹
				drawFolder(canvas);

				FuncAppDataHandler handler = AppFuncFrame.getDataHandler();
				if (handler != null && handler.isShowActionBar()) {
					AppFuncHandler.getInstance().setHomeIconChangeDesk(true, false);
					mAllAppContent.getHomeComponent().paintCurrentFrame(canvas,
							mAllAppContent.getHomeComponent().getAbsX(),
							mAllAppContent.getHomeComponent().getAbsY());
				}
				if (handler != null && handler.isShowTabRow()) {
					AppFuncHandler.getInstance().setTopChange(true, false);
					mTabComponent.getTopBarContainer().paintCurrentFrame(canvas,
							mTabComponent.getTopBarContainer().getAbsX(),
							mTabComponent.getTopBarContainer().getAbsY());
				}

			} else {
				drawFolder(canvas);
				drawFolderCover(canvas);
			}
		} else {
			switch (AppFuncContentTypes.sType) {
				case AppFuncContentTypes.APP :
					if (mTabComponent != null) {
						mTabComponent.paintCurrentFrame(canvas, mTabComponent.mX, mTabComponent.mY);
					}
					break;
				case AppFuncContentTypes.IMAGE :
				case AppFuncContentTypes.MUSIC :
				case AppFuncContentTypes.VIDEO :
				case AppFuncContentTypes.MUSIC_PLAYER :
					if (mMediaManagementPanel != null) {
						mMediaManagementPanel.paintCurrentFrame(canvas, mMediaManagementPanel.mX,
								mMediaManagementPanel.mY);
					}
					break;
			}
		}

		if (mSideEditBg != null && mIsDrawSideEditBg) {
			mSideEditBg.draw(canvas);
		}
		// 绘制被拖拽的图标
		if (mDragView != null) {
			mDragView.paintCurrentFrame(canvas, mDragView.getAbsX(), mDragView.getAbsY());
		}
	}

	// 绘制文件夹
	private void drawFolder(Canvas canvas) {
		int store = canvas.save();
		if (isFolderShow() && mFolder != null && mMotion) {

			canvas.clipRect(mFolderX, mClipStartPos - mMoveUp
					* ((float) mCurrentStep / (float) mTotalStep), mFolderX + mFolderW,
					mClipStartPos + mMoveDown * ((float) mCurrentStep / (float) mTotalStep));
			if (mFolderCache == null || mFolderCache.isRecycled()) {
				buildFolderCache();
			}
			if (mFolderCache != null && !mFolderCache.isRecycled()) {
				canvas.drawBitmap(mFolderCache, mFolder.mX, mFolder.mY, mFolderCachePaint);
			} else {
				mFolder.paintCurrentFrame(canvas, mFolder.mX, mFolder.mY);
			}
		} else {
			mFolder.paintCurrentFrame(canvas, mFolder.mX, mFolder.mY);
		}

		// mFolder.paintCurrentFrame(canvas, mFolder.mX, mFolder.mY);

		canvas.restoreToCount(store);
	}

	/**
	 * 绘制文件夹打开以后盖在文件夹上面的图片，加上动画以后这个绘制应该随动画绘制
	 * 
	 * @param canvas
	 */
	private void drawFolderCover(Canvas canvas) {
		if (mCurrentCache != null && !mCurrentCache.isRecycled()) {
			// XViewFrame.getInstance().drawBackground(canvas);
			// 如果是横屏的时候还要绘制左右两部分的图片
			if (!mUtils.isVertical()) {
				// //画左边区域
				int count = canvas.save();
				canvas.clipRect(0, 0, mFolderX, mHeight);
				canvas.drawBitmap(mCurrentCache, 0, 0, mCachePaint);
				canvas.restoreToCount(count);
				// 画右边区域
				if (!mFolder.getGrid().isInDragStatus()) {
					count = canvas.save();
					canvas.clipRect(mFolderX + mFolderW, 0, mWidth, mHeight);
					canvas.drawBitmap(mCurrentCache, 0, 0, mCachePaint);
					canvas.restoreToCount(count);
				}
			}
			// 绘制文件夹上部的图片
			drawFolderCoverTop(canvas);
			// 绘制文件夹下部的图片
			drawFolderCoverBottom(canvas);
		}
	}

	private void drawFolderCoverTop(Canvas canvas) {
		if (mCurrentStep < 0) {
			mCurrentStep = 0;
		} else if (mCurrentStep > mTotalStep) {
			mCurrentStep = mTotalStep;
		}
		int count = canvas.save();
		canvas.translate(0, -mMoveUp * ((float) mCurrentStep / (float) mTotalStep));
		canvas.clipRect(mFolderX, 0, mFolderX + mFolderW, mClipStartPos);
		canvas.drawBitmap(mCurrentCache, 0, 0, mCachePaint);
		// 被打开的文件夹高亮。
		if (mSrcIcon != null) {
			mSrcIcon.paintCurrentFrame(canvas, mSrcIcon.getAbsX() - mGridOffset, mSrcIcon.getAbsY());
		}
		canvas.restoreToCount(count);
		// if (mFolder!=null&&mMotion) {
		// mFolder.setXY(mFolder.mX,
		// mClipStartPos-(int)(mMoveUp*((float)mCurrentStep/(float)mTotalStep)));
		// // mFolder.setSize(mFolder.getWidth(),
		// (int)(mMoveUp*((float)mCurrentStep/(float)mTotalStep)+mMoveDown*((float)mCurrentStep/(float)mTotalStep)));
		// }
	}

	private void drawFolderCoverBottom(Canvas canvas) {
		if (mCurrentStep < 0) {
			mCurrentStep = 0;
		} else if (mCurrentStep > mTotalStep) {
			mCurrentStep = mTotalStep;
		}

		if (!(mFolder.getGrid().isInDragStatus() && mMoveDown < mAllAppContent
				.getHomeComponetSize())) {
			int count = canvas.save();
			canvas.translate(0, mMoveDown * ((float) mCurrentStep / (float) mTotalStep));
			canvas.clipRect(mFolderX, mClipStartPos, mFolderX + mFolderW, mHeight);
			canvas.drawBitmap(mCurrentCache, 0, 0, mCachePaint);
			canvas.restoreToCount(count);
		}
	}

	@Override
	public boolean onEventFired(XComponent component, byte eventType, Object event, int arg,
			Object object) {
		if (component == mFolder) {
			if (eventType == EventType.HIDE_FOLDER) {
				hideFolder();
			} else if (eventType == EventType.REMOVE_FOLDER) {
				// 飞出动画完成，移除文件夹
				XBaseGrid curGrid = ((AppFuncTabBasicContent) mTabComponent.getCurrentContentView())
						.getXGrid();
				if (curGrid != null) {
					if (curGrid.isInDragStatus() != mFolder.isInEditMode()) {
						curGrid.setDragStatus(mFolder.isInEditMode());
					}
					curGrid.setSupportScroll(true);
				}
				// //将文件夹从列表中移除
				removeComponent(mFolder);
				mIsFolderShow = false;

				mAllAppContent.setIsDrawWrapColor(false);
				mFolder.unregisterObserver();
				mFolder.destroyDrawingCache();
				mIsRePaint = true;
			}
		}
		return false;
	}

	/**
	 * 隐藏文件夹
	 */
	public void hideFolder() {
		if (mIsFolderShow) {
			startAnimation(MOTION_TYPE_CLOSE);
		}
		// // 收起文件夹
		// prepareForOpenFolder(false);
		// mFolder.resetGrid();
		// int step = mFolder.startAnimation(mFolder.mX,
		// -(mFolder.getHeight() + mUtils.getStatusBarHeight()));
		// AppFuncHomeComponent homeComponent =
		// mAllAppContent.getHomeComponent();
		// homeComponent.startFadeAnimation(false, step);
		//
		// // 通知功能表XGrid文件夹已经收起
		// ((AppFuncTabBasicContent) mTabComponent.getCurrentContentView())
		// .getXGrid().setFolderClose();
		//
		// // 反注册
		// try {
		// AppFuncFrame.getDataHandler().deRegisterBgInfoChangeObserver(
		// mFolder.getAdapter());
		// } catch (NullPointerException e) {
		// e.printStackTrace();
		// }
	}

	@Override
	public void notify(int key, Object obj) {

		int currentTabId = getSeletedTab();

		if (key == AppFuncConstants.REFRESHPROCESSGRID) {
			if (currentTabId == AppFuncConstants.PROCESSMANAGEMENT) {
				// 如果当前显示的Tab是程序管理，则刷新页面
				if (mProManageAppContent != null) {
					mProManageAppContent.refreshGrid();
				}
			}
		} else if (key == AppFuncConstants.REFRESHICON) {
			// 刷新某个Icon
			XBaseGrid curGrid = getCurrentContent().getXGrid();
			if (curGrid != null) {
				curGrid.refreshCell((Long) obj);
			}

		} else if (key == AppFuncConstants.EXITAPPFUNCFROMHOME) {
			if (isFolderShow()) {
				// 将消息转发给文件夹的XGrid
				DeliverMsgManager.getInstance()
						.onChange(AppFuncConstants.APPFOLDER_GRID, key, null);

			} else {
				XBaseGrid curGrid = getCurrentContent().getXGrid();
				if (curGrid != null) {
					// 将消息转发给当前显示的XGrid
					DeliverMsgManager.getInstance().onChange(curGrid.mId, key, null);
				}
			}
		} else if (key == AppFuncConstants.SUSPENDEDITMODE) {
			// 收到后台的start save消息，暂停编辑模式
			mIsEditModeDisabled = true;
			AppFuncTabBasicContent content = (AppFuncTabBasicContent) mTabComponent
					.getCurrentContentView();
			XBaseGrid curGrid = content.getXGrid();
			if (curGrid != null) {
				curGrid.startLoading();
			}
			if (isFolderShow()) {
				mFolder.getGrid().startLoading();
			}
		} else if (key == AppFuncConstants.RESUMEEDITMODE) {
			// 收到后台的end save消息，恢复编辑模式
			mIsEditModeDisabled = false;
			XBaseGrid curGrid = getCurrentContent().getXGrid();
			if (curGrid != null) {
				curGrid.endLoading();
			}
			if (isFolderShow()) {
				mFolder.getGrid().endLoading();
			}
		} else if (key == AppFuncConstants.LOADTHEMERES) {
			mFolderOpenBgColor = mThemeCtrl.getThemeBean().mFolderBean.mFolderOpenBgColor;
			// mAllAppContent
			// .setDrawWrapColor(mThemeCtrl.getThemeBean().mFolderBean.mFolderOpenBgColor);
			mediaManagementPluginThemeChange();
		} else if (key == AppFuncConstants.THEME_NEW_INSTALLED) {
			AppFuncTabBasicContent content = (AppFuncTabBasicContent) mTabComponent
					.getCurrentContentView();
			XBaseGrid curGrid = content.getXGrid();
			if (curGrid != null) {
				curGrid.requestLayout();
			}
		} else if (key == AppFuncConstants.APP_COMP_SET_CURRENT_TAB_INDEX) {
			if (mTabComponent != null) {
				int count = mTabComponent.getTabCount();
				if (count > 0) {
					mTabComponent.switchTab((Integer) obj);
				}
			}
		} else if (key == AppFuncConstants.DRAW_SIDEBG_YES) {
			if (obj != null) {
				Rect rt = (Rect) obj;

				if (mSideEditBg != null) {
					mSideEditBg.setBounds(rt);
				}
				mIsDrawSideEditBg = true;
			}
		} else if (key == AppFuncConstants.DRAW_SIDEBG_NO) {
			mIsDrawSideEditBg = false;
		} else if (key == AppFuncConstants.ALL_APP_SWITCH_CONTENT_TYPE) {
			/**
			 * 索引0的参数始终对应的是内容类型 其余位置可以视需求定义
			 */
			Object[] params = (Object[]) obj;
			switchPanel(params);
//			requestLayout();
			mIsRePaint = true;
		}
		// else if(key == AppFuncConstants.SYSTEM_CONFIGURATION_CHANGED){
		// if (mFolder!=null) {
		// mFolder.setIsRebuildBg(true);
		// }
		// }
		else if (key == AppFuncConstants.SET_ALL_APPS_TAB) { // 每次退出功能表时将当前的Tab置为所有Tab
			boolean isVertical = AppFuncUtils.getInstance(mActivity).isVertical();

			if (isVertical) { // 横竖屏的所有程序tab栏id不一样
				if (mTabComponent.mCurrentTab != AppFuncConstants.ALLAPPS) {
					mTabComponent.switchTab(AppFuncConstants.ALLAPPS);
					setScrolleToFirst(); // 如果是竖向滚动的话，滚回列表起始位置
				}
			} else {
				if (mTabComponent.mCurrentTab != AppFuncConstants.PROCESSMANAGEMENT) {
					mTabComponent.switchTab(AppFuncConstants.PROCESSMANAGEMENT);
				}
			}
		}
		// 从通知栏音乐播放器返回的时候，回到播放界面 add by yangbing
		else if (key == AppFuncConstants.RETURN_MUSIC_PLAY) {
			mTabComponent.switchTab(0);
		} else if (key == AppFuncConstants.APP_FUNC_MENUKEY_LONGPRESS) {
			removeFolder();
		} else if (key == AppFuncConstants.LOCATE_MEDIA_ITEM) {
			Object[] params = (Object[]) obj;
			FileInfo fileInfo = (FileInfo) params[0];
			boolean needFocus = (Boolean) params[1];
			locateMediaItem(fileInfo, needFocus);
			mIsRePaint = true;
		} else if (key == AppFuncConstants.OPEN_IMAGE_BROWSER) {
			if (mMediaManagementPanel == null) {
				initMediaManagementPlugin(new Object[] { FROM_OPEN_IMAGE_BROWSER , obj});
			} else {
				Object[] objs = (Object[]) obj;
				Bitmap b = (Bitmap) objs[0];
				ArrayList<FileInfo> itemInfos = (ArrayList<FileInfo>) objs[1];
				if (b != null && itemInfos != null) {
					GoLauncher.sendMessage(this, IDiyFrameIds.MEDIA_CONTROLER,
						IDiyMsgIds.SET_IMAGE_BROWSER_DATA, FunControler.MEDIA_FILE_OPEN_BY_SEARCH, b, itemInfos);
				}
				GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						IFrameworkMsgId.SHOW_FRAME, IDiyFrameIds.IMAGE_BROWSER_FRAME, null, null);
			}
		}
	}

	@Override
	public synchronized boolean onKey(KeyEvent event) {

		if (isFolderShow()) {
			switch (mFolder.getStatus()) {
				case ENTERED : {
					return mFolder.onKey(event);
				}

				case INITIALIZED :
				case ENTERING : {
					// 不响应按键,除了返回键,预防在动画被打断时被锁死
					if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
						if (event.getAction() == KeyEvent.ACTION_UP) {
							mFolder.clearMotion();
							XBaseGrid curGrid = getCurrentContent().getXGrid();
							if (curGrid != null) {
								curGrid.setFolderClose();
							}
							removeComponent(mFolder);
							mIsFolderShow = false;
							mAllAppContent.setIsDrawWrapColor(false);
						}
					}
					return true;
				}
				case LEAVING : {
					// 移出文件夹
					if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
						if (event.getAction() == KeyEvent.ACTION_UP) {
							mFolder.clearMotion();
							XBaseGrid curGrid = getCurrentContent().getXGrid();
							if (curGrid != null) {
								curGrid.setFolderClose();
							}
							removeComponent(mFolder);
							mIsFolderShow = false;
							mAllAppContent.setIsDrawWrapColor(false);
						}
						return true;
					}
				}
			}
		}

		if (mFocusedIndex == -1 || event.getKeyCode() == KeyEvent.KEYCODE_BACK
				|| event.getKeyCode() == KeyEvent.KEYCODE_SEARCH) {
			// if (mFocusedIndex == -1 || event.getKeyCode() ==
			// KeyEvent.KEYCODE_BACK) {
			// 回退键或搜索键直接交给对应的XGrid处理
			// -1表示当前焦点不在Tab,把事件发给XGrid处理
			// return getCurrentContent().getXGrid().onKey(event);
			//			return getCurrentContent().onKey(event);
			switch (AppFuncContentTypes.sType) {
				case AppFuncContentTypes.APP :
					if (mTabComponent != null) {
						return mTabComponent.onKey(event);
					}
					break;
				case AppFuncContentTypes.IMAGE :
				case AppFuncContentTypes.MUSIC :
				case AppFuncContentTypes.VIDEO :
				case AppFuncContentTypes.MUSIC_PLAYER :
					if (mMediaManagementPanel != null) {
						return mMediaManagementPanel.onKey(event);
					}
					break;
			}
		}

		// if (DebugState.isSearchEnable() && event.getKeyCode() ==
		// KeyEvent.KEYCODE_SEARCH)
		// {
		// return getCurrentContent().onKey(event);
		// }

		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			int keycode[] = AppFuncUtils.getInstance(mActivity).getKeyCode();
			if (event.getKeyCode() == keycode[0]) {
				if (mFocusedIndex >= 0 && mFocusedIndex <= mTabCountMaxIndex) {

					mTabComponent.setTabFocus(mFocusedIndex, false);

					mFocusedIndex = -2;
				}
				return true;
			} else if (event.getKeyCode() == keycode[1]) {
				if (mFocusedIndex == -2) {
					mFocusedIndex = mTabComponent.getCurrentIndex();
					mTabComponent.setTabFocus(mFocusedIndex, true);
					return true;
				}

				// 将焦点移到当前的tab content上
				if (getCurrentContent().requestFocused()) {

					mTabComponent.setTabFocus(mFocusedIndex, false);
					mFocusedIndex = -1;
					// 通知content聚焦
					getCurrentContent().setFocused(true);
				}
				return true;
			} else if (event.getKeyCode() == keycode[2]) {
				if (mUtils.isVertical()) {
					if (mFocusedIndex != -2) {
						if (mFocusedIndex == 0) {
							mTabComponent.setTabFocus(mFocusedIndex, false);

							mFocusedIndex = mTabCountMaxIndex;
						} else {

							mTabComponent.setTabFocus(mFocusedIndex, false);
							mFocusedIndex--;
						}
						mTabComponent.setTabFocus(mFocusedIndex, true);
					}
				} else {
					if (mFocusedIndex == -2) {
						mFocusedIndex = mTabComponent.getCurrentIndex();
						mTabComponent.setTabFocus(mFocusedIndex, true);
						return true;
					} else if (mFocusedIndex == mTabCountMaxIndex) {
						mTabComponent.setTabFocus(mFocusedIndex, false);
						mFocusedIndex = 0;
					} else {
						mTabComponent.setTabFocus(mFocusedIndex, false);
						mFocusedIndex++;
					}

					mTabComponent.setTabFocus(mFocusedIndex, true);
				}
				return true;
			} else if (event.getKeyCode() == keycode[3]) {

				if (mUtils.isVertical()) {
					if (mFocusedIndex == -2) {
						mFocusedIndex = mTabComponent.getCurrentIndex();
						mTabComponent.setTabFocus(mFocusedIndex, true);
						return true;
					} else if (mFocusedIndex == mTabCountMaxIndex) {
						mTabComponent.setTabFocus(mFocusedIndex, false);
						mFocusedIndex = 0;
					} else {
						mTabComponent.setTabFocus(mFocusedIndex, false);
						mFocusedIndex++;
					}

					mTabComponent.setTabFocus(mFocusedIndex, true);
				} else {
					if (mFocusedIndex == 0) {
						mTabComponent.setTabFocus(mFocusedIndex, false);

						mFocusedIndex = mTabCountMaxIndex;
					} else {

						mTabComponent.setTabFocus(mFocusedIndex, false);
						mFocusedIndex--;
					}
					mTabComponent.setTabFocus(mFocusedIndex, true);
				}
				return true;
			}
		} else if (event.getAction() == KeyEvent.ACTION_UP) {
			// Enter键只能收到UP消息
			if ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
					|| (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER)) {
				if (mTabComponent.getCurrentTabTagId() == mFocusedIndex) {
					return true;
				} else {
					if (mFocusedIndex >= 0 && mFocusedIndex <= mTabCountMaxIndex) {
						mTabComponent.switchTab(mFocusedIndex);
						return true;
					}
				}
			}
		}
		boolean isHandled = false;
		if (event.getKeyCode() != KeyEvent.KEYCODE_BACK
				&& event.getKeyCode() != KeyEvent.KEYCODE_SEARCH) {
			isHandled = getCurrentContent().onKey(event);
		}

		return isHandled;
	}

	private void prepareForOpenFolder(boolean openFolder) {
		HashMap<String, Object> obj = new HashMap<String, Object>();
		if (mSrcFolderIntent != null) {
			mAllAppContent.getXGrid().handlePositionForOpenFolder(mSrcFolderIntent);
		}
		// 功能表图标变半透明
		if (openFolder) {
			mAllAppContent.getHomeComponent().setIsFolderShow(true);
			mDrawFade = true;
			obj.put("isopen", false);
			obj.put("target", mSrcFolderIntent);
			DeliverMsgManager.getInstance().onChange(AppFuncConstants.ALLAPPS_GRID,
					AppFuncConstants.ALL_APP_ALPHA_ICON, obj);
		} else {
			mAllAppContent.getHomeComponent().setIsFolderShow(false);
			mDrawFade = false;
		}
		obj.put("isopen", openFolder);
		obj.put("target", mSrcFolderIntent);
		DeliverMsgManager.getInstance().onChange(AppFuncConstants.ALLAPPS_GRID,
				AppFuncConstants.ALL_APP_ALPHA_ICON, obj);
		if (mCurrentCache != null && !mCurrentCache.isRecycled()) {
			mCurrentCache.recycle();
			mCurrentCache = null;
		}
		mCurrentCache = null;

		recycleFolderCache();

		if (openFolder) {
			mSrcIcon = mAllAppContent.getXGrid().getIconByInfo(mSrcFolderIntent);
			if (mFolder != null) {
				mFolder.setSrcFolderIcon(mSrcIcon);
			}
		}
	}

	/**
	 * 开启文件夹（关闭是hideFolder）
	 * @param folderInfo
	 * @param inEditMode
	 */
	public void initFolder(FunFolderItemInfo folderInfo, boolean inEditMode) {
		// ///// 在布局前设置文件夹中的Grid能支持拖拽，便于从文件夹拖拽一个图标再拖拽回去文件夹时，文件夹能马上抖动
		// if (mFolder != null) {
		// mFolder.isDragalbe(true);
		// }
		// /////

		handleFolderParames();
		mSrcFolderIntent = folderInfo.getIntent();
		prepareForOpenFolder(true);

		// mCurrentCache = XViewFrame.getInstance().buildCache();
		mCurrentCache = buildCache();
		mFolder = AppFuncFolder.getInstance(mActivity, 1, folderInfo, inEditMode,
				mIsEditModeDisabled, mSrcIcon, mAllAppContent.getXGrid(), mFolderW);

		if (mIconEffect == CoupleScreenEffector.EFFECTOR_TYPE_RANDOM_CUSTOM) {
			GoSettingControler controler = GOLauncherApp.getSettingControler();
			FunAppSetting setting = null;
			if (controler != null && (setting = controler.getFunAppSetting()) != null) {
				int[] effects = setting.getAppIconCustomRandomEffect();
				setCustomRandomEffects(effects);
			}
		}
		mFolder.setGridEffector(mIconEffect);
		mFolder.setEventListener(this);

		mFolder.setXY(mFolderX, -(mFolder.getHeight() + mUtils.getStatusBarHeight()));
		mFolder.setFolderWidth(mFolderW);
		addComponent(mFolder);
		mIsFolderShow = true;
		if (mSrcIcon != null) {
			mSrcIcon.readyData();
		}
		mAllAppContent.setIsDrawWrapColor(true);
		// mFolder.isDragalbe(true);
		initFolderValues();
		//		mFolder.setIsRebuildBg(true);
		int step = 0;
		// if (mUtils.isVertical()) {
		// if (mSrcIcon!=null) {
		// step = mFolder.startAnimation(mFolder.mX, mClipStartPos-mMoveUp);
		// }else {
		// step = mFolder.startAnimation(mFolder.mX, 0);
		// }
		//
		// } else {
		// step = mFolder.startAnimation(mFolder.mX, 0);
		// }
		// mFolder.setSize(mFolder.getWidth(), 0);
		if (mSrcIcon != null) {
			// mFolder.setXY(mFolder.mX, mClipStartPos-mMoveUp);
			step = mFolder.startAnimation(mFolder.mX, mClipStartPos - mMoveUp);
		} else {
			step = mFolder.startAnimation(mFolder.mX, 0);
		}

		startAnimation(MOTION_TYPE_OPEN);
	}

	/**
	 * 处理文件夹参数
	 */
	private void handleFolderParames() {

		int tabSize = mTabComponent.getTabSize();
		int homeSize = mAllAppContent.getHomeComponetSize();
		XBaseGrid grid = mAllAppContent.getXGrid();
		if (!mUtils.isVertical()) {
			if (grid.isVScroll()) {
				mFolderX = grid.getAbsX();
				mFolderW = mWidth - tabSize - homeSize;
			} else {
				mFolderX = grid.getAbsX();
				mFolderW = mWidth - tabSize - homeSize;
			}
		} else {
			mFolderX = 0;
			mFolderW = mWidth;
		}
		mGridOffset = grid.getOffset();
	}

	public boolean optionsItemSelected(MenuItem item) {
		return doOptionsItemSelected(item.getItemId());
	} // function end

	public boolean doOptionsItemSelected(int id) {
		switch (id) {
			case AppFuncAllAppMenuItemInfo.ACTION_CREATE_NEW_FOLDER : {
				mOpenFuncSetting = true;
				// 防止文件夹命名重复，把之前的数据清除。
				//				AppFuncFolder folder = AppFuncFolder.getInstance();
				//				if (folder != null) {
				//					folder.removeFolderInfo();
				//				}
				Intent newFolderIntent = new Intent(mActivity, AppFuncModifyFolderActivity.class);
				newFolderIntent.putExtra(AppFuncConstants.CREATEFOLDER, true);
				if (newFolderIntent != null) {
					mActivity.startActivity(newFolderIntent);
				}
				return true;
			}
			case AppFuncAllAppMenuItemInfo.ACTION_APPDRAWER_SETTING : {
				mOpenFuncSetting = true;
				// Intent intent = new Intent(mActivity,
				// AppFuncUISetting.class);
				Intent intent = new Intent(mActivity, FunAppUISettingMainActivity.class);
				//				intent.putExtra(FunAppUISetting.ENTRANCE, FunAppUISetting.ENTRANCE_FUNC);
				if (intent != null) {
					mActivity.startActivity(intent);
				}

				return true;
			}
			case AppFuncAllAppMenuItemInfo.ACTION_SORT_ICON : {
				// ((AllAppTabWidget)getChildAt(TAB_ALL)).showSelectSort();
				if (mAllAppContent != null) {
					mAllAppContent.showSelectSort();
				}
				return true;
			}

			// case AppFuncAllAppMenu.MENU_ID_APPDRAWER_LOCK: {
			// sOpenFuncSetting = true;
			// Intent lock_intent = new Intent(mActivity, LockList.class);
			//
			// if (lock_intent != null) {
			// mActivity.startActivity(lock_intent);
			// }
			//
			// return true;
			// }

			case AppFuncAllAppMenuItemInfo.ACTION_HIDE_APP : {
				mOpenFuncSetting = true;
				Intent hideIntent = new Intent(mActivity, HideAppActivity.class);

				if (hideIntent != null) {
					mActivity.startActivity(hideIntent);
				}

				return true;
			}

			case AppFuncAllAppMenuItemInfo.ACTION_APP_CENTER : {
				mOpenFuncSetting = true;
				// 启动应用中心：我的应用
				AppRecommendedStatisticsUtil.getInstance().saveCurrentEnter(mActivity,
						AppRecommendedStatisticsUtil.ENTRY_TYPE_FUNTAB_ICON);
				AppsManagementActivity
						.startAppCenter(mActivity, MainViewGroup.ACCESS_FOR_FUNC_MUNE, true);
				return true;
			}

			//			case AppFuncAllAppMenuItemInfo.ACTION_APP_SEARCH : {
			//				GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
			//						IFrameworkMsgId.SHOW_FRAME, IDiyFrameIds.APPFUNC_SEARCH_FRAME, null, null);
			//				return true;
			//			}

			case AppFuncAllAppMenuItemInfo.ACTION_APP_MANAGEMENT : {
				mOpenFuncSetting = true;
				// 启动appsmanagement activity
				AppCore.getInstance().getApplicationManager()
						.show(IDiyFrameIds.APPFUNC_FRAME, AppsManageView.MY_APPS_VIEW_ID);
				return true;
			}

			// case AppFuncAllAppMenu.MENU_ID_SHOWMEDIACOMPONENT: {
			// switchShowMediaManageMentIcon();
			// return true;
			// }
		}
		return false;
	} // function end

	@Override
	public synchronized boolean onTouch(MotionEvent event) {
		if (!mMotion || (MotionEvent.ACTION_UP == event.getAction())) {
			if (isFolderShow()) {
				switch (mFolder.getStatus()) {
					case ENTERED : {
						if (!mMotion
								|| (MotionEvent.ACTION_UP == event.getAction() && mAllAppContent
										.getXGrid().getDragComponent() != null)) {
							return folderTouch(event);
						}
					}
					case INITIALIZED :
					case ENTERING : {
						// 不响应事件
						return true;
					}
					case LEAVING :
						// 将事件传递给功能表
						if (MotionEvent.ACTION_UP == event.getAction()) {
							if (mAllAppContent.getXGrid().getDragComponent() != null) {
								mAllAppContent.getXGrid().onLongClickUp(true);
							} else {
								mAddCellComponentDirectly = true;
							}
							return true;
						}
						break;
				}
			}
			if (!mMotion
					|| (MotionEvent.ACTION_UP == event.getAction() && mAllAppContent.getXGrid()
							.getDragComponent() != null)) {
				switch (AppFuncContentTypes.sType) {
					case AppFuncContentTypes.APP :
						if (mTabComponent != null) {
							return mTabComponent.onTouch(event);
						}
						break;
					case AppFuncContentTypes.IMAGE :
					case AppFuncContentTypes.MUSIC :
					case AppFuncContentTypes.VIDEO :
					case AppFuncContentTypes.MUSIC_PLAYER :
						if (mMediaManagementPanel != null) {
							return mMediaManagementPanel.onTouch(event);
						}
						break;
				}

			}
		}
		return false;
	}

	/**
	 * 当文件夹显示时，处理Touch事件
	 * 
	 * @param event
	 * @return
	 */
	private boolean folderTouch(MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN : {
				if (mFolder.onTouch(event)) {
					// Do nothing
				} else if (mFolder.isInEditMode()) {
					boolean ret = getCurrentContent().getHomeComponent().onTouch(event);
					if (!ret) {
						mTabComponent.getTopBarContainer().onTouch(event);
					}
				}
				break;
			}

			case MotionEvent.ACTION_MOVE : {
				if (mFolder.onTouch(event)) {
					// Do nothing
				} else if (mFolder.isInEditMode()) {
					boolean ret = getCurrentContent().getHomeComponent().onTouch(event);
					if (!ret) {
						mTabComponent.getTopBarContainer().onTouch(event);
					}
				}

				// 如果有图标正位于被抓起的状态
				if (mFolder.getDragIcon() != null) {
					mDragX = (int) event.getX();
					mDragY = (int) event.getY();

					if ((mFolder.XYInRange(mDragX, mDragY) == false)
							&& (getCurrentContent().isInHomeComponent(mDragX, mDragY) == false)
							&& (mTabComponent.getTopBarContainer().XYInRange(mDragX, mDragY)) == false) {

						mIsIconInFolderDragged = true;

						Scheduler scheduler = Scheduler.getInstance();
						Scheduler.getInstance().schedule(Scheduler.TASK_FRAME,
								scheduler.getClock().getFrame() + 12, 12, 12, new FolderTask(),
								new int[] { mDragX, mDragY });
					} else {

						mIsIconInFolderDragged = false;
					}
				}

				break;
			}

			case MotionEvent.ACTION_UP : {
				mIsIconInFolderDragged = false;

				// boolean ret = mFolder.onTouch(event);

				mDragX = (int) event.getX();
				mDragY = (int) event.getY();

				// UP事件在文件夹外
				// 以下判定在会出问题,但暂时不严重:拖动工具条后，UP事件发生在文件夹内时
				if (!mFolder.XYInRange(mDragX, mDragY)) {
					if (mFolder.getDragIcon() == null) {
						// 有点难看，解决问题后再说
						if (mFolder.isInEditMode()) {
							if (getCurrentContent().getHomeComponent().onTouch(event) == false
									&& mTabComponent.getTopBarContainer().onTouch(event) == false) {
								hideFolder();
							}
						} else {
							hideFolder();
						}
					} else {
						// 如果有图标被抓起
						if (mAllAppContent == getCurrentContent()) {
							// Up事件在移动到桌面区域内
							if ((mAllAppContent.isInHomeComponent(mDragX, mDragY) == true)
									|| (mTabComponent.getTopBarContainer()
											.XYInRange(mDragX, mDragY) == true)) {
								// 后面会传递给控件做处理
							} else {
								// 1. 从文件夹删除当前抓起的图标并重排版文件夹
								// 2. 将此图标插入到功能表的XGrid
								// XBaseGrid currentGrid = getCurrentContent()
								// .getXGrid();
								FunControler funControler = AppFuncFrame.getFunControler();
								try {
									funControler.getFunDataModel().beginTransaction();
									FunAppItemInfo removeIcon = mFolder.removeIcon();
									mFolder.getFolderInfo().setMfolderchange(true);
									if (removeIcon != null) {
										int index = IndexFinder.findIndex(mActivity,
												funControler.getFunAppItems(), true, removeIcon);
										funControler.addFunAppItemInfo(index, removeIcon, true,
												true);
										AppFuncHandler.getInstance().layoutRootFuncGrid(removeIcon);
										requestLayout();
									}
									funControler.getFunDataModel().setTransactionSuccessful();
								} catch (DatabaseException e) {
									hideFolder();
									mFolder.clearMotion();
									mFolder.layoutGrid(false);
									mIsRePaint = true;
									AppFuncExceptionHandler.handle(e);
								} finally {
									funControler.getFunDataModel().endTransaction();
								}

								// 开始判断文件夹应用的数量
								ArrayList<FunAppItemInfo> appsInFolder = mFolder.getFolderInfo()
										.getFunAppItemInfosForShow();
								if (appsInFolder.size() <= 1) {
									hideFolder();
								}
								// 刷新
								// mFolder.getGrid().showAddButton(true);
								mFolder.clearMotion();
								mFolder.layoutGrid(false);
								// requestLayout();
								// currentGrid.updateLayoutParams();
								// currentGrid.requestLayout();
								mIsRePaint = true;
							}
						}
					}
				}
				mFolder.onTouch(event);
				// 重置
				mFolder.resetDragIcon();
				break;
			}

		}
		return true;
	}

	@Override
	protected boolean animate() {

		// if (mMotion) {
		// if (mMotionType == MOTION_TYPE_OPEN) {
		// if (mCurrentStep < mTotalStep) {
		// mCurrentStep++;
		// return true;
		// } else {
		// mMotion = false;
		// if (mFolder!=null) {
		// mFolder.setXY(mFolder.mX, mClipStartPos- mMoveUp);
		// }
		// return true;
		// }
		// }else {
		// if (mCurrentStep > 0) {
		// mCurrentStep--;
		// return true;
		// } else {
		// // 收起文件夹
		// prepareForOpenFolder(false);
		// mFolder.resetGrid();
		// int step = mFolder.startAnimation(mFolder.mX,
		// -(mFolder.getHeight() + mUtils.getStatusBarHeight()));
		// AppFuncHomeComponent homeComponent =
		// mAllAppContent.getHomeComponent();
		// homeComponent.startFadeAnimation(false, step);
		//
		// // 通知功能表XGrid文件夹已经收起
		// ((AppFuncTabBasicContent) mTabComponent.getCurrentContentView())
		// .getXGrid().setFolderClose();
		//
		// // 反注册
		// try {
		// AppFuncFrame.getDataHandler().deRegisterBgInfoChangeObserver(
		// mFolder.getAdapter());
		// } catch (NullPointerException e) {
		// e.printStackTrace();
		// }
		//
		// mMotion = false;
		// return true;
		// }
		// }
		//
		// }

		if (mIsRePaint) {
			mIsRePaint = false;
			return true;
		}
		return false;
	}

	/**
	 * 检查两点是否相同，有一定容错性
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean checkRegionSame(int x, int y) {
		if (mIsIconInFolderDragged && mFolder.getDragIcon() != null) {
			if (getCurrentContent().isInHomeComponent(x, y) == false
					|| mTabComponent.getTopBarContainer().XYInRange(x, y) == false) {
				if (Math.abs(x - mDragX) <= 5 && Math.abs(y - mDragY) <= 5) {
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * 
	 * <br>类描述:功能表文件夹定时任务
	 * <br>功能详细描述:
	 * 
	 * @author  
	 * @date  [2012-9-26]
	 */
	private class FolderTask implements ITask {

		@Override
		public void execute(long id, long time, Object userName) {
			int[] position = (int[]) userName;
			if (checkRegionSame(position[0], position[1])) {
				// FunAppItemInfo removedItemInfo = null;
				try {
					mRemovedItemInfo = mFolder.removeIcon();
				} catch (DatabaseException e) {
					AppFuncExceptionHandler.handle(e);
				}
				mFolder.getFolderInfo().setMfolderchange(true);
				// XBaseGrid curGrid = getCurrentContent().getXGrid();

				mIsAddCellComponent = true; // 标志从文件夹拖动图标到功能表，需要功能表移位

				// 在区域内的事件处理
				hideFolder();

				// 1. 从文件夹删除当前抓起的图标
				// 2. 更新文件夹缩略图
				// 3. 将当前抓起的图标和坐标告诉功能表XGrid进行插入

				// curGrid.refreshCell(mFolder.getAdapter().getFolderId());
				// if (curGrid != null) {
				// curGrid.requestLayout();
				// curGrid.setDragStatus(true);
				// //
				// // if (removedItemInfo != null) {
				// // curGrid.addCellComponent(removedItemInfo, mDragX,
				// // mDragY);
				// // }
				// }

				// // 重置
				mFolder.resetDragIcon();
				// if (mFolder.isInEditMode()) {
				// mFolder.mGrid.setDragStatus(false);
				// }
				// mTabComponent.onFolderHide();
			}
		}

		@Override
		public void finish(long id, long time, Object userName) {
		}
	}

	/**
	 * 返回已选择的Tab的索引
	 * 
	 * @return
	 */
	public int getSeletedTab() {

		if (mTabComponent != null) {
			return mTabComponent.getCurrentTabTagId();
		}

		return -1;
	}

	/**
	 * 返回已选择的Tab的内容
	 * 
	 * @return
	 */
	public XComponent getSeletedTabContentView() {

		if (mTabComponent != null) {
			return mTabComponent.getCurrentContentView();
		}

		return null;
	}

	private void setupTabCompoent(Activity activity) {

		AppFuncTabSingleTitle allAppTab = new AppFuncTabSingleTitle(activity, 1, 0, 0, 0, 0,
				activity.getResources().getString(R.string.tabs_allApps), AppFuncConstants.ALLAPPS);

		AppFuncTabSingleTitle recentAppTab = new AppFuncTabSingleTitle(activity, 1, 0, 0, 0, 0,
				activity.getResources().getString(R.string.tabs_recentApps),
				AppFuncConstants.RECENTAPPS);

		AppFuncTabSingleTitle proManageTab = new AppFuncTabSingleTitle(activity, 1, 0, 0, 0, 0,
				activity.getResources().getString(R.string.tabs_processManagement),
				AppFuncConstants.PROCESSMANAGEMENT);

		mAllAppContent = new AllAppTabBasicContent(activity, 1, 0, 0, 0, 0,
				AppFuncConstants.ALLAPPS_GRID);
		mRecentAppContent = new RecentAppTabBasicContent(activity, 1, 0, 0, 0, 0,
				AppFuncConstants.RECENTAPPS_GRID);
		mProManageAppContent = new ProManageTabBasicContent(activity, 1, 0, 0, 0, 0,
				AppFuncConstants.PROCESS_GRID);

		int iconEffect = AppFuncFrame.getDataHandler().getIconEffect();
		if (iconEffect == CoupleScreenEffector.EFFECTOR_TYPE_RANDOM_CUSTOM) {
			GoSettingControler controler = GOLauncherApp.getSettingControler();
			FunAppSetting setting = null;
			if (controler != null && (setting = controler.getFunAppSetting()) != null) {
				int[] effects = setting.getAppIconCustomRandomEffect();
				setCustomRandomEffects(effects);
			}
		}
		setGridEffector(iconEffect);
		int scrollLoop = AppFuncFrame.getDataHandler().getScrollLoop();
		setCycleMode(scrollLoop == 1);

		mTabComponent = new AppFuncTabComponent(activity, 1, 0, 0, 0, 0);
		mTabComponent.addTab(allAppTab, mAllAppContent, AppFuncConstants.ALLAPPS);
		mTabComponent.addTab(recentAppTab, mRecentAppContent, AppFuncConstants.RECENTAPPS);
		mTabComponent
				.addTab(proManageTab, mProManageAppContent, AppFuncConstants.PROCESSMANAGEMENT);

		mTabComponent.setOnTabChangedListener(new OnTabContentChangeListener() {

			@Override
			public void onTabChanged(int tabId) {
				mFocusedIndex = tabId;
				switch (tabId) {
					case TABID_ALL :
						StatisticsData.countMenuData(mContext, StatisticsData.FUNTAB_KEY_ALL);
						break;
					case TABID_RECENT :
						StatisticsData.countMenuData(mContext, StatisticsData.FUNTAB_KEY_RECENT);
						if (RecentAppsIcon.sIsStartFromRencetTab) {
							//将copy的还原到内存，并清空copy中资源
//							AppCore.getInstance().getRecentAppControler().refreshItemsFromCopy();
							AppDrawerControler.getInstance(mActivity).refreshItemsFromCopy();
						}
						break;
					case TABID_RUNNING :
						StatisticsData.countMenuData(mContext, StatisticsData.FUNTAB_KEY_RUNNING);
						break;
					default :
						break;
				}
				getCurrentContent().tabChangeUpdate();
			}
		});

		mTabComponent.setOnSameTabClickListener(new OnSameTabClickListener() {
			@Override
			public void onSameTabClick(int tabId) {
				mFocusedIndex = tabId;
				switch (tabId) {
					case TABID_ALL : // 点击所有程序tab返回第一页或页首
						try {
							XComponent currentView = mTabComponent.getCurrentContentView();
							if (currentView != null
									&& currentView instanceof AppFuncTabBasicContent) {
								XBaseGrid xBaseGrid = ((AppFuncTabBasicContent) currentView)
										.getXGrid();
								if (xBaseGrid != null) {
									xBaseGrid.scrollToFirst(); // 点击所有程序Tab栏，滚回顶端
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						break;
					case TABID_RECENT :
						break;
					case TABID_RUNNING : // 点击正在运行tab刷新
						DeliverMsgManager.getInstance().onChange(
								// 发消息通知刷新内存条
								AppFuncConstants.PROMANAGEHOMEICON,
								AppFuncConstants.PRO_MANAGE_REFRESH, null);
						mTabComponent.requestLayout(); // 点击正在运行Tab栏，刷新XBaseGrid
						break;
					default :
						break;
				}
			}
		});

		addComponent(mTabComponent);
		mTabCountMaxIndex = mTabComponent.getTabCount() - 1;
	}

	public AppFuncTabBasicContent getCurrentContent() {
		return (AppFuncTabBasicContent) mTabComponent.getCurrentContentView();
	}

	public boolean isFolderShow() {
		return mIsFolderShow;
		// boolean isShow = false;
		// if ((mFolder != null) && (indexOfComponent(mFolder) >= 0))
		// isShow = true;
		//
		// return isShow;
	}

	public void removeFolder() {
		if (isFolderShow()) {
			prepareForOpenFolder(false);
			removeComponent(mFolder);
			mIsFolderShow = false;
			mAllAppContent.setIsDrawWrapColor(false);
			mFolder.unregisterObserver();
			XBaseGrid curGrid = getCurrentContent().getXGrid();
			if (curGrid != null) {
				curGrid.setSupportScroll(true);
			}
			mIsRePaint = true;
		}
	}

	public void getTabFocus() {
		boolean isNoFolderAction = true;
		if (mFolder != null) {
			isNoFolderAction = mFolder.getStatus() != AppFuncFolder.STATUS.ENTERED;
		}

		if (mTabComponent != null && isNoFolderAction) {
			mFocusedIndex = mTabComponent.getCurrentIndex();
			mTabComponent.setTabFocus(mFocusedIndex, true);
		}
	}

	@Override
	protected void onHide() {
		super.onHide();
		mFocusedIndex = -2;
		// 收起文件夹
		// removeFolder();
		// 释放
		mProManageAppContent.release();
	}

	public void refreshAllAppGrid() {
		if (mAllAppContent != null) {
			mAllAppContent.getXGrid().updateLayoutParams();
			mAllAppContent.getXGrid().requestLayout();
		}
	}

	public void reloadAllApps() {
		if (mAllAppContent != null) {
			mAllAppContent.getXGrid().reloadAllApps();
		}
	}

	public void setGridEffector(int iconEffect) {
		if (mIconEffect == iconEffect) {
			return;
		}
		mIconEffect = iconEffect;
		mAllAppContent.getXGrid().setGridEffector(iconEffect);
		mRecentAppContent.getXGrid().setGridEffector(iconEffect);
		mProManageAppContent.getXGrid().setGridEffector(iconEffect);
		if (mFolder != null) {
			mFolder.setGridEffector(iconEffect);
		}
	}

	public void setCustomRandomEffects(int[] effects) {
		mAllAppContent.getXGrid().setCustRandomEffectors(effects);
		mRecentAppContent.getXGrid().setCustRandomEffectors(effects);
		mProManageAppContent.getXGrid().setCustRandomEffectors(effects);
		if (mFolder != null) {
			mFolder.setCustRandomEffectors(effects);
		}
	}

	public void setCycleMode(boolean cycle) {
		mAllAppContent.getXGrid().setCycleMode(cycle);
		mRecentAppContent.getXGrid().setCycleMode(cycle);
		mProManageAppContent.getXGrid().setCycleMode(cycle);
		if (mFolder != null) {
			mFolder.setCycleMode(cycle);
		}
	}

	public void gotoNextTab() {
		final int count = mTabComponent.getTabCount();
		if (count > 0) {
			mTabComponent.switchTab((mTabComponent.getCurrentIndex() + 1) % count);
		}
	}

	public void gotoPreviousTab() {
		final int count = mTabComponent.getTabCount();
		if (count > 0) {
			mTabComponent.switchTab((mTabComponent.getCurrentIndex() + count - 1) % count);
		}
	}

	/**
	 * 获取当前打开的文件夹
	 * 
	 * @return
	 */
	public AppFuncFolder getCurrentFolder() {
		return mFolder;
	}

	/**
	 * 根据当前文件夹图标的位置确认文件夹展开后的位置
	 */
	public void initFolderValues() {
		// 动画时间定为300毫秒
		if (mSrcIcon != null && mFolder != null) {

			int iconY = mSrcIcon.getAbsY();
			if (mAllAppContent.getXGrid().isVScroll()) {
				iconY = mSrcIcon.getAbsY() - mGridOffset;
			}
			int iconHeight = mSrcIcon.getIconHeight();
			// int folderY = mFolder.getAbsY();
			int folderHeight = mFolder.getHeight();

			// AppFuncHomeComponent homeComponent =
			// mAllAppContent.getHomeComponent();

			// int homeHeight = homeComponent.getHeight();
			// 如果文件夹底部超出了home栏顶部，说明空间不够，需要移动上面的部分
			int folderBottom = iconY + iconHeight + folderHeight;
			mClipStartPos = iconY + iconHeight + mUtils.getScaledSize(FOLDER_ICON_OFFSET);
			int bottomOffset = 0;
			int bottomY = 0;

			if (mUtils.isVertical()) {
				bottomOffset = mUtils.getStandardSize(90);
				bottomY = mHeight - bottomOffset;
				if (folderBottom > bottomY) {
					mMoveUp = folderBottom - bottomY;
				} else {
					// 如果没有超出，说明目前的界面能排下来,上面不需要移动，只需要移动下面就可以了
					mMoveUp = 0;
				}
			} else {
				bottomOffset = mUtils.getStandardSize(60);
				bottomY = mHeight - bottomOffset;
				if (folderBottom > mHeight - bottomOffset) {
					mMoveUp = folderBottom - (mHeight - bottomOffset);
				} else {
					// 如果没有超出，说明目前的界面能排下来,上面不需要移动，只需要移动下面就可以了
					mMoveUp = 0;
				}
			}
			mMoveDown = Math.abs(folderHeight - mMoveUp);
		}
		int max = Math.max(mMoveUp, mMoveDown);
		mSpeed =  max / (int) TOTAL_MOTION_TIME;
	}

	/**
	 * 获取当前屏幕截图
	 * 
	 * @return
	 */
	public Bitmap buildCache() {
		int width = mWidth;
		int height = mHeight;

		Bitmap mCache = null;
		if (mCache == null || mCache.getWidth() != width || mCache.getHeight() != height) {
			Bitmap.Config quality = Bitmap.Config.ARGB_8888;
			try {
				mCache = Bitmap.createBitmap(width, height, quality);
				if (mCache != null && mContext != null) {
					mCache.setDensity(mContext.getResources().getDisplayMetrics().densityDpi);
				}
			} catch (OutOfMemoryError e) {
				mCache = null;
				return null;
			} catch (StackOverflowError e) {
				mCache = null;
				return null;
			}

		}
		if (mCache != null) {
			Canvas canvas = new Canvas(mCache);
			final int restoreCount = canvas.save();
			boolean temp = mDrawFade;
			mDrawFade = false;
			drawCurrentFrame(canvas);
			mDrawFade = temp;
			canvas.restoreToCount(restoreCount);

		}
		return mCache;
	}

	/**
	 * 开始平移动画
	 * 
	 * @param dstX
	 * @param dstY
	 * @return 动画持续帧数
	 */
	public int startAnimation(int motionType) {
		// if ((mMotion != null) && (mMotion.isFinished() == false)) {
		// mMotion.stop();
		// mMotion = null;
		// }
		mMotionType = motionType;
		mMotion = true;
		mCurrentStep = 0;

		int max = Math.max(mMoveDown, mMoveUp);
		if (mSpeed != 0) {
			mTotalStep = max / mSpeed;	
		} else {
			mTotalStep = max;
		}
		mAlphaGradient = 255 / 14;
		if (mMotionType == MOTION_TYPE_CLOSE) {
			mCurrentStep = mTotalStep;
			if (mFolder != null) {
				mFolder.setStatus(AppFuncFolder.STATUS.LEAVING);
			}
		} else {
			mAlpha = mTabComponent.getDrawingCacheAlpha();
		}
		buildFolderCoverCache();
		buildFolderCache(); // 获取要做动画的截图

		// mMotion = new XALinear(1, XALinear.XALINEAR_ECSPEED, 0, 0, 0,
		// max, mTotalStep, 0, 0);
		// setMotionFilter(mMotion);
		// if (mY > dstY) {
		// mGrid.clearDragStatus(); // 拖动图标到功能表的时候，文件夹收起，清除拖动状态，以免图标被文件夹和功能表都绘制
		// mStatus = STATUS.LEAVING;
		// // XGrid释放焦点
		// if (mGrid.isFocused()) {
		// mGrid.setFocused(false);
		// }
		// } else {
		// mStatus = STATUS.ENTERING;
		// }
		// mAllAppContent.getHomeComponent().startFadeAnimation(mMotionType ==
		// MOTION_TYPE_OPEN, mTotalStep);
		mIsRePaint = true;
		return mTotalStep;
	}

	/**
	 * 执行动画计算，为了保证动画流畅性，不掉帧，将原来动画线程中进行的动画计算放在主线程中进行
	 */
	private void doAnimate() {

		if (mMotion) {

			if (mMotionStartTime == 0) {
				mMotionStartTime = SystemClock.uptimeMillis();
			}
			long timePassed = SystemClock.uptimeMillis() - mMotionStartTime;

			if (timePassed > TOTAL_MOTION_TIME) {
				timePassed = TOTAL_MOTION_TIME;
			}

			float rate = (float) timePassed / (float) TOTAL_MOTION_TIME;

			if (mMotionType == MOTION_TYPE_OPEN) {
				if (mCurrentStep < mTotalStep) {
					mCurrentStep = Math.round(mTotalStep * rate);
					mIsRePaint = true;
					if (mAlpha < 50) {
						mAlpha = 50;
					}
					mCachePaint.setAlpha(mAlpha);
					mAlpha = mAlpha - mAlphaGradient;
				} else {
					mAlpha = 50;
					mCachePaint.setAlpha(mAlpha);
					mMotion = false;
					mMotionStartTime = 0;
					// if (mFolder!=null) {
					// mFolder.setXY(mFolder.mX, mClipStartPos- mMoveUp);
					// }
					mIsRePaint = true;
					mIsPapreClose = true;
					// 打开动画完成后，销毁文件夹截图
					recycleFolderCache();
//					Debug.stopMethodTracing();
				}
			} else {
				if (mCurrentStep > 0) {
					if (!mIsPapreClose) {
						mAlpha = mAlpha + mAlphaGradient;
						if (mAlpha > 255) {
							// 文件夹背景渐变
							mAlpha = mTabComponent.getDrawingCacheAlpha();
						}
						mCachePaint.setAlpha(mAlpha);
					}
					mIsPapreClose = false;
					mCurrentStep = mTotalStep - Math.round(mTotalStep * rate);
					mIsRePaint = true;
				} else {
					// 收起文件夹
					XBaseGrid curGrid = ((AppFuncTabBasicContent) mTabComponent
							.getCurrentContentView()).getXGrid();
					if (curGrid != null) {
						if (curGrid.isInDragStatus() != mFolder.isInEditMode()) {
							curGrid.setDragStatus(mFolder.isInEditMode());
						}
						curGrid.setSupportScroll(true);
						FuncAppDataHandler handler = AppFuncFrame.getDataHandler();
						if (handler != null && handler.isShowTabRow()) {
							if (mFolder.isInEditMode()) {
								AppFuncHandler.getInstance().setTopChange(mFolder.isInEditMode(),
										false);
							}
						}
					}
					// //将文件夹从列表中移除
					// removeComponent(mFolder);
					// mIsFolderShow = false;

					mAllAppContent.setIsDrawWrapColor(false);
					mFolder.unregisterObserver();
					mFolder.destroyDrawingCache();
					mIsRePaint = true;

					prepareForOpenFolder(false);
					// mFolder.resetGrid();
					// int step = mFolder.startAnimation(mFolder.mX,
					// -(mFolder.getHeight() + mUtils.getStatusBarHeight()));
					// int step =
					// mFolder.startAnimation(mFolder.mX,mClipStartPos-mMoveUp);

					// AppFuncHomeComponent homeComponent =
					// mAllAppContent.getHomeComponent();
					// homeComponent.startFadeAnimation(false, step);

					// 通知功能表XGrid文件夹已经收起
					if (curGrid != null) {
						curGrid.setFolderClose();
					}

					if (mIsAddCellComponent) {
						// 1. 从文件夹删除当前抓起的图标
						// 2. 更新文件夹缩略图
						// 3. 将当前抓起的图标和坐标告诉功能表XGrid进行插入

						if (curGrid != null) {
							DeliverMsgManager.getInstance().onChange(
									AppFuncConstants.APP_FUNC_FOLDER_QUICK_ADD_BAR,
									AppFuncConstants.REFRESH_FOLDER_QUICK_ADD_BAR_NORMAL_FOLDER,
									mFolder.getFolderInfo());
							curGrid.requestLayout(false);
							curGrid.setDragStatus(true);

							if (mRemovedItemInfo != null) {
								curGrid.addCellComponent(mRemovedItemInfo, mDragX, mDragY,
										mAddCellComponentDirectly);
								mAddCellComponentDirectly = false;
							}
						}
						mIsAddCellComponent = false;
						// mFolder.resetDragIcon();
						mTabComponent.onFolderHide();
					}
					removeComponent(mFolder);
					mIsFolderShow = false;
					// 文件夹打开的时候不画遮罩。
					if (mSrcIcon != null) {
						mSrcIcon.readyData();
					}
					// 反注册
					try {
						AppFuncFrame.getDataHandler().deRegisterBgInfoChangeObserver(
								mFolder.getAdapter());
					} catch (NullPointerException e) {
						e.printStackTrace();
					}
					mMotionStartTime = 0;
					mMotion = false;
					mIsRePaint = true;
					if (mIsCloseAndOpenFolder) {
						scrollAndOpenFolder(mFolderToShow, curGrid);
						mIsCloseAndOpenFolder = false;
					}
				}
			}
		}
	}

	public void scrollAndOpenFolder(FunFolderItemInfo folderInfo, XBaseGrid curGrid) {
		mReadyShowFolder = true;
		boolean ret = curGrid.scrolltoTargetScreen(folderInfo.getIndex());
		if (ret) {
			Scheduler scheduler = Scheduler.getInstance();
			Scheduler.getInstance().schedule(Scheduler.TASK_TIME,
					scheduler.getClock().getTime() + 650, 750, 750, new OpenFolderTask(),
					folderInfo);
		} else {
			AppFuncHandler.getInstance().showFolder(folderInfo);
			mReadyShowFolder = false;
		}
	}

	/**
	 * 打开文件夹的Task
	 */
	private class OpenFolderTask implements ITask {
		
		@Override
		public void execute(long id, long time, Object folderInfo) {
			// TODO Auto-generated method stub
			if (folderInfo instanceof FunFolderItemInfo) {
				AppFuncHandler.getInstance().showFolder((FunFolderItemInfo) folderInfo);
				mReadyShowFolder = false;
			}
		}

		@Override
		public void finish(long id, long time, Object userName) {
			// TODO Auto-generated method stub

		}

	}
	public void setDragComponent(XComponent dragComponent) {
		mDragView = dragComponent;
	}

	/**
	 * 销毁文件夹缓存图片
	 */
	public void recycleFolderCache() {
		if (mFolderCache != null && !mFolderCache.isRecycled()) {
			mFolderCache.recycle();
			mFolderCache = null;
			return;
		}
		mFolderCache = null;
	}

	/**
	 * 创建功能表背景截图
	 */
	public void buildFolderCoverCache() {
		// 获取背景图截图
		if (mCurrentCache == null || mCurrentCache.isRecycled()) {
			mCurrentCache = buildCache();
		}
	}

	/**
	 * 创建文件夹截图
	 */
	public void buildFolderCache() {
		// 截取文件夹截图
		if (mFolder != null) {
			try {
				mFolderCache = Bitmap.createBitmap(mFolder.getWidth(), mFolder.getHeight(),
						Bitmap.Config.ARGB_8888);
				Canvas folderCanvas = new Canvas(mFolderCache);
				mFolder.paintCurrentFrame(folderCanvas, 0, 0);
			} catch (OutOfMemoryError e) {
			} catch (StackOverflowError e) {
			} catch (Exception e) {
				mFolderCache = null;
			}
		}
	}

	public AllAppTabBasicContent getAllAppContent() {
		return mAllAppContent;
	}

	private void switchShowMediaManageMentIcon() {
		FunAppSetting funAppSetting = GoSettingControler.getInstance(mActivity).getFunAppSetting();
		boolean show = funAppSetting.isShowMediaManagement();
		funAppSetting.setShowMediaManagement(!show);
		AppFuncTabBasicContent content = (AppFuncTabBasicContent) mTabComponent
				.getCurrentContentView();
		if (content != null && content.getHomeComponent() != null) {
			content.getHomeComponent().requestLayout();
		}
	}

	/**
	 * 功能表竖屏滚动的情况下将所有程序的列表拉回开头
	 */
	public void setScrolleToFirst() {
		boolean mIsVScroll = !(AppFuncFrame.getDataHandler().getSlideDirection() == FunAppSetting.SCREENMOVEHORIZONTAL);
		if (mIsVScroll) {
			XComponent currentView = mTabComponent.getCurrentContentView();
			if (currentView != null && currentView instanceof AppFuncTabBasicContent) {
				XBaseGrid xBaseGrid = ((AppFuncTabBasicContent) currentView).getXGrid();
				if (xBaseGrid != null) {
					xBaseGrid.setBaseGridScroll(0); // 退回顶端
				}
			}
		}
	}

	public void showTabRow() {
		if (mTabComponent != null) {
			mTabComponent.showTabTitles();
		}
	}

	public void hideTabRow() {
		if (mTabComponent != null) {
			mTabComponent.hideTabTitles();
		}
	}

	public void showActionBar() {
		AppFuncTabBasicContent content = getCurrentContent();
		if (content != null) {
			content.showActionBar();
		}
	}

	public void hideActionBar() {
		AppFuncTabBasicContent content = getCurrentContent();
		if (content != null) {
			content.hideActionBar();
		}
	}
	/**
	 * <br>功能简述:切换到tab正常模式
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void showTopNormalBar(boolean needAnimate) {
		if (mTabComponent != null) {
			mTabComponent.showTopNormalbar(needAnimate);
		}
	}
	/**
	 * <br>功能简述:切换到tab栏编辑模式
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void showTopEditBar(boolean needAnimate) {
		if (mTabComponent != null) {
			mTabComponent.showTopEditBar(needAnimate);
		}
	}

	/**
	 * 
	 * 获取顶部操作栏
	 * @return
	 */
	public AppFuncTabComponent getTabComponent() {
		return mTabComponent;
	}

	public AppFuncFolderQuickAddBar getFolderQuickAddBar() {
		return mTabComponent.getFolderQuickAddBar();
	}

	public ProManageEditDock getProManageEditDock() {
		return mTabComponent.getProManageEditDock();
	}

	public boolean isCloseAndOpenFolder() {
		return mIsCloseAndOpenFolder;
	}

	public void setIsCloseAndOpenFolder(boolean isCloseAndOpenFolder) {
		mIsCloseAndOpenFolder = isCloseAndOpenFolder;
	}

	public FunFolderItemInfo getFolderToShow() {
		return mFolderToShow;
	}

	public void setFolderToShow(FunFolderItemInfo folderToShow) {
		mFolderToShow = folderToShow;
	}

	private void switchPanel(Object... params) {
		int type = (Integer) params[0];
		if (AppFuncContentTypes.sType == type
				&& (AppFuncContentTypes.sType == AppFuncContentTypes.APP
				|| AppFuncContentTypes.sType == AppFuncContentTypes.SEARCH)) {
			return;
		}
		switch (type) {
			case AppFuncContentTypes.APP :
				GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IDiyMsgIds.REMOVE_FRAME, IDiyFrameIds.APPFUNC_SEARCH_FRAME, null, null);
				if (mMediaManagementPanel != null) {
					removeComponent(mMediaManagementPanel);
				}
				addComponent(mTabComponent);
				if (MediaPluginFactory.isMediaPluginExist(mContext)) { // 如果存在插件包，回首插件包所占内存
					IMediaManager mediaManager = MediaPluginFactory.getMediaManager();
					if (mediaManager != null) {
						mediaManager.onExitMediaManagement(); // 这里保留Container对象，不做清除
					}
					AppFuncFrame.getFunControler().destroyFileEngine(); // 销毁FileEngine
					ThumbnailManager.destory(); // 删除多媒体搜索缩略图缓存
				}
				break;
			case AppFuncContentTypes.IMAGE :
			case AppFuncContentTypes.MUSIC :
			case AppFuncContentTypes.VIDEO :
			case AppFuncContentTypes.MUSIC_PLAYER :
				if (mMediaManagementPanel == null) {
					initMediaManagementPlugin(new Object[] { FROM_SWITCH_PANEL, params });
				} else {
					GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IDiyMsgIds.REMOVE_FRAME, IDiyFrameIds.APPFUNC_SEARCH_FRAME, null, null);
					if (AppFuncFrame.getFunControler().getFileEngine() == null) { // 引擎会被销毁，重新进入时为插件包需要设置一次引擎的引用
						AppFuncFrame.getFunControler().buildFileEngine();
					}

					MediaPluginFactory.getMediaManager().switchContent(params);
					removeComponent(mTabComponent);
					addComponent(mMediaManagementPanel);
					requestLayout();
				}
				break;
			case AppFuncContentTypes.SEARCH :
				GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						IFrameworkMsgId.SHOW_FRAME, IDiyFrameIds.APPFUNC_SEARCH_FRAME, null,
						null);
				if (MediaPluginFactory.isMediaPluginHavePlayingBar()) {
					IMediaManager mediaManager = MediaPluginFactory.getMediaManager();
					if (mediaManager != null) { // 进入搜索层时需要通知一下插件包，如果正在播放音乐，需要显示通知栏
						MediaPluginFactory.getMediaManager().switchContent(
								new Object[] { AppFuncContentTypes.SEARCH });
					}
				}
				break;
			default :
				break;
		}
		AppFuncContentTypes.sType = type;
	}
	
	private void locateMediaItem(FileInfo fileInfo, boolean needFocus) {
		if (mMediaManagementPanel == null) {
			initMediaManagementPlugin(new Object[] { FROM_LOCAL_MEDIA_ITEM, fileInfo, needFocus });
		} else {
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IDiyMsgIds.REMOVE_FRAME, IDiyFrameIds.APPFUNC_SEARCH_FRAME, null, null);
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IDiyMsgIds.SHOW_APP_DRAWER, -1, null, null);
			if (AppFuncFrame.getFunControler().getFileEngine() == null) { // 引擎会被销毁，重新进入时为插件包需要设置一次引擎的引用
				AppFuncFrame.getFunControler().buildFileEngine();
			}
			removeComponent(mTabComponent);
			addComponent(mMediaManagementPanel);
			requestLayout();
			IMediaManager mediaManager = MediaPluginFactory.getMediaManager();
			if (mediaManager != null) {
				mediaManager.locateMediaItem(fileInfo, needFocus);
			}

			if (fileInfo instanceof ImageFile) {
				AppFuncContentTypes.sType = AppFuncContentTypes.IMAGE;
			} else if (fileInfo instanceof AudioFile) {
				AppFuncContentTypes.sType = AppFuncContentTypes.MUSIC;
			} else if (fileInfo instanceof VideoFile) {
				AppFuncContentTypes.sType = AppFuncContentTypes.VIDEO;
			}
		}
	}
	
	private final static int SHOW_INIT_MEDIA_PLUGIN_FAIL_TOAST = 1;
	private void initHandler() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch(msg.what) {
					case SHOW_INIT_MEDIA_PLUGIN_FAIL_TOAST :
						DeskToast.makeText(GoLauncher.getContext(), R.string.init_mediamanagement_plugin_fail, Toast.LENGTH_LONG).show();
						break;
					default :
						break;
				}
			}
		};
	}

	public final static int FROM_OPEN_IMAGE_BROWSER = 1;
	public final static int FROM_SWITCH_PANEL = 2;
	public final static int FROM_LOCAL_MEDIA_ITEM = 3;
	
	protected synchronized void initMediaManagementPlugin(final Object[] objects) {
		if (mInitMediaPluginTask != null || mMediaManagementPanel != null) {
			return;
		}
		mInitMediaPluginTask = new AsyncTask<Object, Void, Object>() {
			@Override
			protected void onPreExecute() {
				if (mMediaManagementPanel == null) {
					GoLauncher.sendMessage(this, IDiyFrameIds.APPFUNC_FRAME,
						IDiyMsgIds.APPDRAWER_PROGRESSBAR_SHOW, -1, null, null); // 显示loadding界面
				}
			}
			
			@Override
			protected synchronized Object doInBackground(Object... params) {
				boolean isCompatible = true;
				if (mMediaManagementPanel == null) {
					if (AppFuncUtils.getInstance(mActivity).isMediaPluginCompatible()) { // 桌面版本与插件包版本是否互相支持
						try {
							//					Duration.setStart("buildMediaPlugin");
							MediaPluginFactory.buildMediaPlugin(mActivity);
							//					Duration.setEnd("buildMediaPlugin");

							//					Duration.setStart("initData");
							AppFuncFrame.getFunControler().buildFileEngine();

							//初始化IMediaManager
							final IMediaManager mediaManager = MediaPluginFactory.getMediaManager();
							mediaManager.setSwitchMenuControler(MediaPluginFactory
									.getSwitchMenuControler());
							try {
								mediaManager.setMediaCommonMenu(new MediaCommonMenu(mActivity));
							} catch (RuntimeException e) {
								// 由于在2.2.2系统上会出现异步线程使用Handler从而抛错，这里移动到主线程上面来初始化MediaCommonMenu
								Handler handler = new Handler(Looper.getMainLooper());
								handler.post(new Runnable() {
									@Override
									public void run() {
										mediaManager.setMediaCommonMenu(new MediaCommonMenu(
												mActivity));
									}
								});
							}
							//		mediaManager.setMediaMessageManager(new MediaMessageManager());
							mediaManager.setMediaDialog(new MediaDialog(mActivity));
							FunAppSetting setting = GoSettingControler.getInstance(mContext)
									.getFunAppSetting();
							//		mediaManager.setImageDefaultOpenWay(setting.getImageOpenWay());
							//		mediaManager.setMusicDefaultOpenWay(setting.getAudioOpenWay());
							mediaManager.setFileEngine(AppFuncFrame.getFunControler()
									.getFileEngine()); // 此处可能未初始化，需要再进行一次
							//初始化IMediaUIManager
							IMediaUIManager uiManager = MediaPluginFactory.getMediaUIManager();
							if (MediaPluginFactory.isMediaPluginHavePlayingBar()) {
								uiManager.setNeedHideMusicPlayer(AppFuncAutoFitManager.getInstance(
										GOLauncherApp.getContext()).needHideMusicPlayer());
							}
							uiManager.setRootView(XViewFrame.getInstance());
							GoSettingControler goSettingControler = GOLauncherApp
									.getSettingControler();
							ScreenSettingInfo screenInfo = goSettingControler
									.getScreenSettingInfo();
							uiManager.setIndicatorShowMode(GoSettingControler
									.getInstance((Activity) mContext).getScreenStyleSettingInfo()
									.getIndicatorStyle());
							uiManager.setIndicatorPos(screenInfo.mIndicatorPosition);
							uiManager.setIndicatorScrollH(mUtils.getDrawable(R.drawable.scrollh));
							uiManager.setIndicatorScrollV(mUtils.getDrawable(R.drawable.scrollv));
							uiManager.setIndicatorTextSize(mUtils
									.getDimensionPixelSize(R.dimen.indicator_numeric_textsize));
							uiManager.setFontSize(GoLauncher.getAppFontSize());
							FontBean fontBean = goSettingControler.getUsedFontBean();
							uiManager.setFontType(fontBean.mFontTypeface, fontBean.mFontStyle);
							uiManager.setTurnScreenDirection(AppFuncFrame.getDataHandler()
									.getSlideDirection());
							uiManager.setScrollLoop(AppFuncFrame.getDataHandler().getScrollLoop());
							uiManager.setVerticalScrollEffect(AppFuncFrame.getDataHandler()
									.getVerticalScrollEffect());
							uiManager.setContainerHeight(mHeight);
							uiManager.setContainerWidth(mWidth);
							uiManager.setScreenWidth(GoLauncher.getScreenWidth());
							uiManager.setScreenHeight(GoLauncher.getScreenHeight());
							uiManager.setStandardIconSize(Utilities.getStandardIconSize(mActivity));
							uiManager.setIconTextDst(mUtils
									.getDimensionPixelSize(R.dimen.appfunc_icon_text_dst));
							uiManager.setOrientationType(GOLauncherApp.getSettingControler()
									.getGravitySettingInfo().mOrientationType);
							uiManager
									.setAppFuncBottomHeight(mUtils
											.getDimensionPixelSize(AppFuncTabBasicContent.sBottomHeight_id));
							String curPackageName = ThemeManager.getInstance(mActivity)
									.getCurThemePackage();
							String packageName = null;
							if (!curPackageName.equals(GOLauncherApp.getSettingControler()
									.getFunAppSetting().getTabHomeBgSetting())) {
								packageName = GOLauncherApp.getSettingControler()
										.getFunAppSetting().getTabHomeBgSetting();
							}
							if (!AppUtils.isAppExist(mActivity, packageName)) {
								packageName = GOLauncherApp.getThemeManager().getCurThemePackage();
							}

							//		uiManager.setMenuBgV(mThemeCtrl.getDrawable(
							//				mThemeCtrl.getThemeBean().mAllAppMenuBean.mMenuBgV, packageName));
							//		uiManager.setMenuBgH(mThemeCtrl.getDrawable(
							//				mThemeCtrl.getThemeBean().mAllAppMenuBean.mMenuBgH, packageName));
							//		uiManager.setMenuDividerV(mThemeCtrl.getDrawable(
							//				mThemeCtrl.getThemeBean().mAllAppMenuBean.mMenuDividerV, packageName));
							//		uiManager.setMenuDividerH(mThemeCtrl.getDrawable(
							//				mThemeCtrl.getThemeBean().mAllAppMenuBean.mMenuDividerH, packageName));
							//		uiManager.setMenuTextColor(mThemeCtrl.getThemeBean().mAllAppMenuBean.mMenuTextColor);
							//		uiManager.setMenuItemSelectedBg(mThemeCtrl.getDrawable(
							//				mThemeCtrl.getThemeBean().mAllAppMenuBean.mMenuItemSelected, packageName));
							uiManager
									.setShowStatusBar(goSettingControler.getDesktopSettingInfo().mShowStatusbar);
							uiManager.setCurrentThemePackage(packageName);
							mediaManagementPluginThemeChange();
							List<Animation> animations = new ArrayList<Animation>(2);
							animations.add(AnimationFactory.createEnterAnimation(4, mContext));
							animations.add(AnimationFactory.createExitAnimation(4, mContext));
							uiManager.setImgBrowserAnimation(animations);
							if (mIconEffect == CoupleScreenEffector.EFFECTOR_TYPE_RANDOM_CUSTOM) {
								GoSettingControler controler = GOLauncherApp.getSettingControler();
								if (controler != null && setting != null) {
									int[] effects = setting.getAppIconCustomRandomEffect();
									uiManager.setCustomRandomEffects(effects);
								}
							}
							uiManager.setGridEffector(mIconEffect);
							mMediaManagementPanel = mediaManager.getMediaManagementContainer();

							if (!MediaPluginFactory.getMediaManager().getIsMediaDataBeCopy()) { // 还未转移过则进行转移
								AppFuncFrame.getDataHandler().copyAllMediaData();
								AppFuncFrame.getDataHandler().deleteAllMediaData(); // 转移成功删除数据
								MediaPluginFactory.getMediaManager().setIsMediaDataBeCopy(true); // 转移成功写入plugin包的设置表
							}
							//					Duration.setEnd("initData");
							//					Log.e(".....", ""+Duration.getDuration("buildMediaPlugin"));
							//					Log.e(".....", ""+Duration.getDuration("initData"));
						} catch (Exception ex) {
							ex.printStackTrace();
							isCompatible = false;
							mHandler.sendEmptyMessage(SHOW_INIT_MEDIA_PLUGIN_FAIL_TOAST);
						}
					} else {
						isCompatible = false;
					}
				}
				Object[] p = new Object[params.length + 1];
				p[0] = isCompatible;
				for (int i = 0; i < params.length; i++) {
					p[i + 1] = params[i];
				}
				return p;
			}
			
			protected void onPostExecute(Object result) {
				GoLauncher.sendMessage(this, IDiyFrameIds.APPFUNC_FRAME,
						IDiyMsgIds.APPDRAWER_PROGRESSBAR_HIDE, -1, null, null); // 取消loadding界面
				Object[] objs = (Object[]) result;
				if ((Boolean) objs[0]) { // 是否兼容
					int type = (Integer) objs[1];
					if (type == FROM_SWITCH_PANEL) {
						DeliverMsgManager.getInstance().onChange(
								AppFuncConstants.APP_FUNC_MAIN_VIEW,
								AppFuncConstants.ALL_APP_SWITCH_CONTENT_TYPE, (Object[]) objs[2]);
					} else if (type == FROM_LOCAL_MEDIA_ITEM) {
						FileInfo fileInfo = (FileInfo) objs[2];
						boolean needFocus = (Boolean) objs[3];
						locateMediaItem(fileInfo, needFocus);
					} else if (type == FROM_OPEN_IMAGE_BROWSER) {
						DeliverMsgManager.getInstance().onChange(AppFuncConstants.APP_FUNC_MAIN_VIEW,
								AppFuncConstants.OPEN_IMAGE_BROWSER, objs[2]);
					}
				} else {
					if (AppFuncContentTypes.sType != AppFuncContentTypes.SEARCH) {
						AppFuncContentTypes.sType = AppFuncContentTypes.APP;
						mIsRePaint = true;
					}
				}
				mInitMediaPluginTask = null;
			};
		};
		
		mInitMediaPluginTask.execute(objects);
	}

	public static final int INDICATOR_SHOW_MODE_CHANGE = 1;
	public static final int INDICATOR_POSITION_CHANGE = 2;
	public static final int SCROLL_LOOP_CHANGE = 3;
	public static final int VERTICAL_SCROLL_EFFECTOR_CHANGE = 4;
	public static final int ORIENTATION_TYPE_CHANGE = 5;
	public static final int SHOW_STATUS_BAR_CHANGE = 6;
	public static final int GRID_EFFECTOR_CHANGE = 7;

	/**
	 * 设置发生改变时通知资源管理插件同时进行改变
	 */
	public void mediaManagementPluginSettingChange(int type, Object obj) {
		if (MediaPluginFactory.isMediaPluginExist(mContext)) { // 如果存在插件包，改变插件包对应设置
			FunAppSetting setting = GoSettingControler.getInstance(mContext).getFunAppSetting();
			// 初始化IMediaUIManager
			IMediaUIManager uiManager = MediaPluginFactory.getMediaUIManager();
			if (uiManager != null) {
				GoSettingControler goSettingControler = GOLauncherApp.getSettingControler();
				ScreenSettingInfo screenInfo = goSettingControler.getScreenSettingInfo();
				switch (type) {
					case INDICATOR_SHOW_MODE_CHANGE : // 指示器显示模式
						uiManager.setIndicatorShowMode(GoSettingControler
								.getInstance((Activity) mContext).getScreenStyleSettingInfo()
								.getIndicatorStyle());
						break;
					case INDICATOR_POSITION_CHANGE : // 指示器显示位置
						uiManager.setIndicatorPos(screenInfo.mIndicatorPosition);
						break;
					case SCROLL_LOOP_CHANGE :
						// 滚屏方式
						uiManager.setTurnScreenDirection(AppFuncFrame.getDataHandler()
								.getSlideDirection());
						// 是否滚屏循环
						uiManager.setScrollLoop(AppFuncFrame.getDataHandler().getScrollLoop());
						break;
					case VERTICAL_SCROLL_EFFECTOR_CHANGE : // 竖向滚屏特效
						uiManager.setVerticalScrollEffect(AppFuncFrame.getDataHandler()
								.getVerticalScrollEffect());
						break;
					case ORIENTATION_TYPE_CHANGE : // 屏幕方式
						uiManager.setOrientationType(GOLauncherApp.getSettingControler()
								.getGravitySettingInfo().mOrientationType);
						break;
					case SHOW_STATUS_BAR_CHANGE : // 是否显示状态栏(通知栏)
						uiManager.setShowStatusBar((Boolean) obj);
						break;
					case GRID_EFFECTOR_CHANGE : // 横屏滚屏特效
						if (mIconEffect == CoupleScreenEffector.EFFECTOR_TYPE_RANDOM_CUSTOM) {
							GoSettingControler controler = GOLauncherApp.getSettingControler();
							if (controler != null && setting != null) {
								int[] effects = setting.getAppIconCustomRandomEffect();
								uiManager.setCustomRandomEffects(effects);
							}
						}
						uiManager.setGridEffector(mIconEffect);
						break;
					default :
						break;
				}
			}
		}
	}

	/**
	 * 主题发生改变时通知资源管理插件同时进行改变
	 */
	public void mediaManagementPluginThemeChange() {
		if (MediaPluginFactory.isMediaPluginExist(mContext)) { // 如果存在插件包，通知插件包主题改变
			// 初始化IMediaUIManager
			IMediaUIManager uiManager = MediaPluginFactory.getMediaUIManager();
			if (uiManager != null) {
				String packageName = null;
				String curPackageName = ThemeManager.getInstance(mActivity).getCurThemePackage();
				if (!curPackageName.equals(GOLauncherApp.getSettingControler().getFunAppSetting()
						.getTabHomeBgSetting())) {
					packageName = GOLauncherApp.getSettingControler().getFunAppSetting()
							.getTabHomeBgSetting();
				}
				if (!AppUtils.isAppExist(mActivity, packageName)) {
					packageName = GOLauncherApp.getThemeManager().getCurThemePackage();
				}
				uiManager.setCurrentThemePackage(packageName);
				int titleColor = 0;
				if (GoLauncher.getCustomTitleColor()) {
					int color = GoLauncher.getAppTitleColor();
					if (color != 0) {
						titleColor = color;
					} else {
						titleColor = AppFuncConstants.ICON_TEXT_COLOR;
					}
				} else {
					titleColor = mThemeCtrl.getThemeBean().mAppIconBean.mTextColor;
				}
				uiManager.setTitleColor(titleColor);
				uiManager.setIndicatorCurrentHor(mThemeCtrl.getDrawable(
						mThemeCtrl.getThemeBean().mIndicatorBean.indicatorCurrentHor, GOLauncherApp
								.getSettingControler().getFunAppSetting().getIndicatorSetting(),
						false));
				uiManager.setIndicatorHor(mThemeCtrl.getDrawable(
						mThemeCtrl.getThemeBean().mIndicatorBean.indicatorHor, GOLauncherApp
								.getSettingControler().getFunAppSetting().getIndicatorSetting(),
						false));
				uiManager.setIndicatorSetting(GOLauncherApp.getSettingControler()
						.getFunAppSetting().getIndicatorSetting());
				uiManager.setActionBarBgV(mThemeCtrl.getDrawable(
						mThemeCtrl.getThemeBean().mHomeBean.mHomeBgVerPath, packageName));
				uiManager.setActionBarBgH(mThemeCtrl.getDrawable(
						mThemeCtrl.getThemeBean().mHomeBean.mHomeBgHorPath, packageName));
				uiManager
						.setActionBarBgDrawingWay(mThemeCtrl.getThemeBean().mHomeBean.mHomeBgDrawingWay);
				uiManager.setHomeButtonIcon(mThemeCtrl.getDrawable(
						mThemeCtrl.getThemeBean().mHomeBean.mHomeUnSelected, packageName));
				uiManager.setHomeButtonIconPressed(mThemeCtrl.getDrawable(
						mThemeCtrl.getThemeBean().mHomeBean.mHomeSelected, packageName));
				uiManager.setMenuButtonIcon(mThemeCtrl.getDrawable(
						mThemeCtrl.getThemeBean().mAllAppDockBean.mHomeMenu, packageName));
				uiManager.setMenuButtonIconPressed(mThemeCtrl.getDrawable(
						mThemeCtrl.getThemeBean().mAllAppDockBean.mHomeMenuSelected, packageName));
				
				Drawable drawable = mThemeCtrl.getDrawable(
						mThemeCtrl.getThemeBean().mSwitchButtonBean.mGalleryIcon, packageName);
				if (drawable == null) {
					drawable = mContext.getResources().getDrawable(R.drawable.appfunc_switch_button_gallery);
				}
				uiManager.setSwitchButtonImageIcon(drawable);
				
				drawable = mThemeCtrl.getDrawable(
						mThemeCtrl.getThemeBean().mSwitchButtonBean.mGalleryLightIcon,
						packageName);
				if (drawable == null) {
					drawable = mContext.getResources().getDrawable(R.drawable.appfunc_switch_button_gallery_light);
				}
				uiManager.setSwitchButtonImageIconPressed(drawable);
				
				drawable = mThemeCtrl.getDrawable(
						mThemeCtrl.getThemeBean().mSwitchButtonBean.mMusicIcon, packageName);
				if (drawable == null) {
					drawable = mContext.getResources().getDrawable(R.drawable.appfunc_switch_button_music);
				}
				uiManager.setSwitchButtonMusicIcon(drawable);
				
				drawable = mThemeCtrl.getDrawable(
						mThemeCtrl.getThemeBean().mSwitchButtonBean.mMusicLightIcon, packageName);
				if (drawable == null) {
					drawable = mContext.getResources().getDrawable(R.drawable.appfunc_switch_button_music_light);
				}
				uiManager.setSwitchButtonMusicIconPressed(drawable);
				
				drawable = mThemeCtrl.getDrawable(
						mThemeCtrl.getThemeBean().mSwitchButtonBean.mVideoIcon, packageName);
				if (drawable == null) {
					drawable = mContext.getResources().getDrawable(R.drawable.appfunc_switch_button_video);
				}
				uiManager.setSwitchButtonVideoIcon(drawable);
				
				drawable = mThemeCtrl.getDrawable(
						mThemeCtrl.getThemeBean().mSwitchButtonBean.mVideoLightIcon, packageName);
				if (drawable == null) {
					drawable = mContext.getResources().getDrawable(R.drawable.appfunc_switch_button_video_light);
				}
				uiManager.setSwitchButtonVideoIconPressed(drawable);
			}
		}
	}
	
	public boolean isMediaPluginInited() {
		return mMediaManagementPanel != null;
	}
	
	public boolean isReadyShowFolder() {
		return mReadyShowFolder;
	}

	public boolean isMotion() {
		return mMotion;
	}

}
