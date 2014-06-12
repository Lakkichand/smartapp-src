package com.jiubang.ggheart.apps.appfunc.component;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.device.Machine;
import com.go.util.graphics.DrawUtils;
import com.go.util.graphics.ImageUtil;
import com.jiubang.core.mars.EventType;
import com.jiubang.core.mars.IComponentEventListener;
import com.jiubang.core.mars.XALinear;
import com.jiubang.core.mars.XComponent;
import com.jiubang.core.mars.XMotion;
import com.jiubang.core.mars.XPanel;
import com.jiubang.ggheart.apps.appfunc.component.AppFuncImageButton.OnClickListener;
import com.jiubang.ggheart.apps.appfunc.setting.FunAppSetting;
import com.jiubang.ggheart.apps.appfunc.theme.AppFuncThemeController;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogSingleChoice;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncExceptionHandler;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncFrame;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncModifyFolderActivity;
import com.jiubang.ggheart.apps.desks.appfunc.XBaseGrid;
import com.jiubang.ggheart.apps.desks.appfunc.XViewFrame;
import com.jiubang.ggheart.apps.desks.appfunc.handler.AppFuncHandler;
import com.jiubang.ggheart.apps.desks.appfunc.handler.FuncAppDataHandler;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants.MessageID;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUtils;
import com.jiubang.ggheart.apps.desks.appfunc.model.DeliverMsgManager;
import com.jiubang.ggheart.apps.desks.appfunc.model.FolderAdapter;
import com.jiubang.ggheart.apps.desks.appfunc.model.IBackgroundInfoChangedObserver;
import com.jiubang.ggheart.apps.desks.appfunc.model.IMsgHandler;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.IRequestCodeIds;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.components.DeskToast;
import com.jiubang.ggheart.components.renamewindow.RenameActivity;
import com.jiubang.ggheart.data.BroadCaster.BroadCasterObserver;
import com.jiubang.ggheart.data.DatabaseException;
import com.jiubang.ggheart.data.info.FunAppItemInfo;
import com.jiubang.ggheart.data.info.FunFolderItemInfo;
import com.jiubang.ggheart.data.info.FunItemInfo;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 功能表文件夹
 * @author yangguanxiang
 *
 */
public class AppFuncFolder extends XPanel
		implements
			IMsgHandler,
			IBackgroundInfoChangedObserver,
			IComponentEventListener,
			BroadCasterObserver,
			OnClickListener {
	
	/**
	 * 文件夹状态
	 */
	public static enum STATUS {
		INITIALIZED, // 初始状态
		ENTERING, // 正在进入
		ENTERED, // 进入完毕
		LEAVING, // 正在离开
	}

	/**
	 * 文件夹节点信息
	 */
	private FunFolderItemInfo mFolderInfo;

	private XText mText;

	private AppFuncImageButton mButton;
	private AppFuncImageButton mSortButton;

	private FolderAdapter mAdapter;

	private Activity mActivity;

	private XBaseGrid mGrid;

	private AppFuncUtils mUtils;

	private int mOrientation;

	private int mImageHeigth = 0;
	private int mImageWidth = 0;
	private int mImageHeigthBean = 0;

	/**
	 * 是否位于编辑模式
	 */
	private boolean mInEditMode;
	/**
	 * 才被显示并且没有收到Down事件时为True
	 */
	private boolean mShowWithoutTouch;
	/**
	 * 动画
	 */
	private XMotion mMotion;

	private int mTotalStep;
	private int mCurrentStep;

	/**
	 * 是否被按下
	 */
	private boolean mIsTouchDown;
	/**
	 * 当前画笔
	 */
	private Paint mPainter;
	/**
	 * 状态机
	 */
	private STATUS mStatus;
	/**
	 * 背景图
	 */
	private Drawable mBgImg;
	// private Bitmap mBgBt;
	/**
	 * 单例对象
	 */
	private static AppFuncFolder sInstance;

	/**
	 * 编辑栏的高度
	 */
	private int mEditTabHeight;
	/**
	 * 竖屏时编辑控件的宽度
	 */
	private int mEditBoxWidthV;
	/**
	 * 横屏时编辑控件的宽度
	 */
	private int mEditBoxWidthH;
	/**
	 * 编辑控件前端到文件夹边框的水平距离
	 */
	private int mMarginH;
	/**
	 * 字体大小
	 */
	private int mTextSize;
	/**
	 * 横屏时文件夹的宽度
	 */
	private int mWidthH;
	/**
	 * 横屏时Grid与文件夹边框的距离
	 */
	private int mGridMarginH;

	private int mGridPaddingLeft;

	private int mGridPaddingTop;

	private int mGridPaddingRight;

	private int mGridPaddingBottom;
	/**
	 * 编辑框高度
	 */
	private int mEditBoxHeight;
	/**
	 * 图标高度
	 */
	private int mIconHeight;
	/**
	 * 被抓起的图标
	 */
	private XComponent mDragIcon;
	/**
	 * 画线的画笔
	 */
	protected Paint mLinePaint;
	/**
	 * 主题控制器
	 */
	private AppFuncThemeController mThemeController;

	/**
	 * 收起按钮的规格：480*800为标准
	 */
	private static final int BUTTON_UP_WIDTH = 50;
	private static final int BUTTON_UP_HEIGHT = 50;

	/**
	 * 编辑控件前端到文件夹边框的水平距离
	 */
	private int mImageBottomH;
	/**
	 * 主题是否发生改变
	 */
	private boolean mIsThemeChanged = true;

	private ApplicationIcon mSrcIcon = null;
	/**
	 * 文件夹宽度
	 */
	private int mFolderW = 0;

	private boolean mIsDefaultTheme = false;

	public static final int BUTTON_PADDING = 8; // 按钮左右padding

	public static final int MARGIN_H = 28; // 输入框边距默认值

	/**
	 * 获得Folder单列对象。需要外部显示设置坐标
	 * 
	 * @param activity
	 * @param tickCount
	 * @param folderId
	 * @param folderInfo
	 * @return 已经排好版的对象
	 */
	public static AppFuncFolder getInstance(Activity activity, int tickCount,
			FunFolderItemInfo folderInfo, boolean inEditMode, boolean editModeDisabled,
			ApplicationIcon srcIcon, XBaseGrid srcGrid, int folderWidth) {
		if (sInstance == null) {
			sInstance = new AppFuncFolder(activity, tickCount, folderInfo, inEditMode,
					editModeDisabled, folderWidth);
		} else {
			sInstance.mInEditMode = inEditMode;
			sInstance.mShowWithoutTouch = true;
			sInstance.mDragIcon = null;
			// sInstance.mText.setEditable(inEditMode);
			sInstance.mFolderInfo = folderInfo;
			sInstance.mText
					.setTextSize((int) (GoLauncher.getAppFontSize() * DrawUtils.sDensity) + 5);
			sInstance.mText.setText(folderInfo.getTitle());
			sInstance.initAdapter(inEditMode);
			if (editModeDisabled) {
				sInstance.mGrid.startLoading();
			}
		}
		
		String theme = GOLauncherApp.getThemeManager().getCurThemePackage();
		// if(theme != null && theme.equals(ThemeManager.DEFAULT_THEME_PACKAGE))
		// {
		// sInstance.mIsDefaultTheme = true;
		// }
		// 3.0主题暂时当默认主题处理，modify by yangbing 2012-05-08
		if (ThemeManager.isAsDefaultThemeToDo(theme)) {
			sInstance.mIsDefaultTheme = true;
		}
		sInstance.mFolderW = folderWidth;
		sInstance.mSrcIcon = srcIcon;
		sInstance.setLayoutParams();
		// 如果主题发生改变就重新加载主题资源
		if (sInstance.mIsThemeChanged) {
			sInstance.loadResouce();
			sInstance.mIsThemeChanged = false;
		}
		sInstance.getGrid().dataInit();
		sInstance.layout();
		folderInfo.registerObserver(sInstance);
		sInstance.mStatus = STATUS.INITIALIZED;
		
		return sInstance;
	}

	public static AppFuncFolder getInstance() {
		return sInstance;
	}

	private AppFuncFolder(Activity activity, int tickCount, FunFolderItemInfo folderInfo,
			boolean inEditMode, boolean editModeDisabled, int folderWidth) {
		super(tickCount, 0, 0, 0, 0);
		mThemeController = AppFuncFrame.getThemeController();
		mActivity = activity;
		mInEditMode = inEditMode;
		mFolderW = folderWidth;
		mUtils = AppFuncUtils.getInstance(mActivity);
		// mBgImg = mUtils.getDrawable(R.drawable.appfunc_folder_frame);
		// mBgImg = mThemeController
		// .getDrawable(mThemeController.getThemeBean().mFolderBean.mFolderBgPath);
		mLinePaint = new Paint();
		mFolderInfo = folderInfo;
		setLayoutParams();
		mText = new XText(tickCount, 0, 0, 0, 0, activity);
		mText.setEventListener(this);
		// mText.setEditable(inEditMode);
		mText.setTextSize(mTextSize);
		boolean editable = !GOLauncherApp.getSettingControler().getScreenSettingInfo().mLockScreen;
		mText.setEditable(editable);
		// mText.setBgImage(mUtils.getDrawable(R.drawable.appfunc_rename));
		// mText.setBgImage(mThemeController.getDrawable(mThemeController
		// .getThemeBean().mFolderBean.mFolderEditBgPath));
		addComponent(mText);
		mButton = new AppFuncImageButton(mActivity, tickCount, 0, 0, 0, 0);
		mSortButton = new AppFuncImageButton(mActivity, tickCount, 0, 0, 0, 0);
		if (Machine.isTablet(mActivity)) {
			mButton.setSize(BUTTON_UP_WIDTH, BUTTON_UP_HEIGHT);
			mSortButton.setSize(BUTTON_UP_WIDTH, BUTTON_UP_HEIGHT);
		} else {
			mButton.setSize(mUtils.getStandardSize(BUTTON_UP_WIDTH),
					mUtils.getStandardSize(BUTTON_UP_HEIGHT));
			mSortButton.setSize(mUtils.getStandardSize(BUTTON_UP_WIDTH),
					mUtils.getStandardSize(BUTTON_UP_HEIGHT));
		}
		mButton.setClickListener(this);
		mSortButton.setClickListener(this);
		addComponent(mSortButton);
		addComponent(mButton);
		mOrientation = AppFuncConstants.NOORIENTATION;
		// 初始化数据适配器
		initAdapter(inEditMode);
		// 初始化XGrid
		mGrid = new XBaseGrid(mActivity, tickCount, 0, 0);
		// mGrid.setShowAddButton(true);
		mGrid.setAdapter(mAdapter);
		mGrid.setSupportScroll(true);
		mGrid.setDragable(editable);
		mGrid.setIsFolderEnable(false);
		mGrid.mId = AppFuncConstants.APPFOLDER_GRID;
		mGrid.setGridEdgeEffectEnabled(false, true); // 因为边缘淡化效果使用的是功能表背景，不适用于文件夹背景，所以禁用掉
		int scrollLoop = AppFuncFrame.getDataHandler().getScrollLoop();
		setCycleMode(scrollLoop == 1);
		if (editModeDisabled) {
			mGrid.startLoading();
		}
		// 将Grid注册给消息发送控制器
		DeliverMsgManager.getInstance().registerMsgHandler(mGrid.mId, mGrid);

		addComponent(mGrid);
		mGrid.setXY(0, mEditTabHeight + 2);
		mTotalStep = 8;
		mShowWithoutTouch = true;
		mDragIcon = null;
		mPainter = new Paint();
		// 注册自己
		DeliverMsgManager.getInstance().registerMsgHandler(AppFuncConstants.APPFOLDER, this);

		// 注册主题改变事件
		DeliverMsgManager.getInstance().registerDispenseMsgHandler(AppFuncConstants.THEME_CHANGE,
				this);
		// 注册获取主题图片事件
		DeliverMsgManager.getInstance().registerDispenseMsgHandler(AppFuncConstants.LOADTHEMERES,
				this);
		// 注册桌面锁屏事件
		DeliverMsgManager.getInstance().registerDispenseMsgHandler(AppFuncConstants.LOCKCHANGES,
				this);
		// 注册文件夹主题变换事件
		DeliverMsgManager.getInstance().registerDispenseMsgHandler(
				AppFuncConstants.RELOAD_FOLDER_THEMES, this);

		setLayout(null);

		// pdf = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);
		// mTriangleDefault =
		// (BitmapDrawable)mActivity.getResources().getDrawable(R.drawable.folder_top_triangle);
		// mTriangleMask =
		// (BitmapDrawable)mActivity.getResources().getDrawable(R.drawable.folder_mask_top);
		// if (mTriangleMask!=null) {
		// mTriangleH = mTriangleMask.getIntrinsicHeight();
		// }
	}

	// 在对象被销毁时使用
	private void unRegisterHandler() {
		DeliverMsgManager msgManager = DeliverMsgManager.getInstance();
		msgManager.unregisterMsgHandler(mGrid.mId);
		msgManager.unregisterMsgHandler(AppFuncConstants.APPFOLDER);
		msgManager.unRegisterDispenseMsgHandler(AppFuncConstants.THEME_CHANGE);
		msgManager.unRegisterDispenseMsgHandler(AppFuncConstants.LOADTHEMERES);
		msgManager.unRegisterDispenseMsgHandler(AppFuncConstants.RELOAD_FOLDER_THEMES);
	}

	private void initAdapter(boolean inDragStatus) {
		if (mAdapter == null) {
			// 是否显示名称
			boolean showName = (AppFuncFrame.getDataHandler().getShowName() < FunAppSetting.APPNAMEVISIABLEYES)
					? false
					: true;
			mAdapter = new FolderAdapter(mActivity, showName, mFolderInfo);
			// mAdapter.setShowAddButton(true);
		} else {
			mAdapter.changeFolderInfo(mFolderInfo);
			// mAdapter.setShowAddButton(true);
			mAdapter.loadApp();
		}
	}

	/**
	 * 根据横竖屏和文件夹中应用程序个数自动排版 初始化和横竖屏切换时通过外部显示调用
	 */
	public void layout() {
		if (mUtils.isVertical()) {
			if (mSrcIcon != null) {
				// setXY(0, mSrcIcon.getAbsY()+mSrcIcon.getHeight());
			} else {
				// setXY(0, 0);
			}

			mGridPaddingTop = mUtils.getStandardSize(24);
		} else {
			// setXY(0, 0);
			mGridPaddingTop = mUtils.getStandardSize(10);
		}

		setLayoutParams();
		layoutBgImage();
		layoutText();
		layoutButton();
		layoutGrid(true);
		clearMotion();
		mGrid.updateLayoutParams();
		mGrid.requestLayout();
		// 设置是否位于编辑模式
		mGrid.setDragStatus(mInEditMode);
		FuncAppDataHandler handler = AppFuncFrame.getDataHandler();
		if (handler != null && handler.isShowActionBar()) {
			if (mInEditMode) {
				AppFuncHandler.getInstance().setHomeIconChangeDesk(mInEditMode, false);
			}
		}
		if (handler != null && handler.isShowTabRow()) {
			if (mInEditMode) {
				AppFuncHandler.getInstance().setTopChange(mInEditMode, false);
			}
		}
	}

	/**
	 * 排版按键
	 */
	private void layoutButton() {
		if (mButton == null) {
			return;
		}

		if (mIsDefaultTheme) {
			mButton.mX = mMarginH + mText.getWidth() + mUtils.getStandardSize(BUTTON_PADDING)
					+ mSortButton.getWidth();
			mSortButton.mX = mMarginH + mText.getWidth() + mUtils.getStandardSize(BUTTON_PADDING);
		} else {
			mButton.mX = mMarginH + mText.getWidth() + mUtils.getStandardSize(BUTTON_PADDING - 2)
					+ mSortButton.getWidth();
			mSortButton.mX = mMarginH + mText.getWidth()
					+ mUtils.getStandardSize(BUTTON_PADDING - 2);
		}

		mButton.mY = mText.mY + (mText.getHeight() - mButton.getHeight()) / 2;
		mSortButton.mY = mText.mY + (mText.getHeight() - mButton.getHeight()) / 2;
	}

	/**
	 * 排版编辑框
	 */
	private void layoutText() {
		int y = mUtils.getStandardSize(2);
		if (mOrientation == AppFuncConstants.VERTICAL) {
			y = mUtils.getStandardSize(4);
		}
		// int margin_y = (mEditTabHeight - mEditBoxHeight) / 2 + y;
		// mText.setXY(mMargin_h, mTriangleH + (mEditTabHeight - mEditBoxHeight)
		// / 2 + y);
		mText.setXY(mMarginH, (mEditTabHeight - mEditBoxHeight) / 2 + y);
		if (GoLauncher.getOrientation() == Configuration.ORIENTATION_PORTRAIT) {
			mText.setSize(mEditBoxWidthV, mEditBoxHeight);
		} else {
			mText.setSize(mEditBoxWidthH, mEditBoxHeight);
		}
		mText.setTextAlign(XText.LEFT | XText.VCENTER);
		mText.setText(mFolderInfo.getTitle());

		if (mOrientation == AppFuncConstants.NOORIENTATION) {
			mOrientation = (mUtils.isVertical())
					? AppFuncConstants.VERTICAL
					: AppFuncConstants.HORIZONTAL;
		} else {
			if (mUtils.isVertical()) {
				if (mOrientation == AppFuncConstants.VERTICAL) {
					return;
				} else {
					mOrientation = AppFuncConstants.VERTICAL;
				}
			} else {
				if (mOrientation == AppFuncConstants.HORIZONTAL) {
					return;
				} else {
					mOrientation = AppFuncConstants.HORIZONTAL;
				}
			}
		}
	}

	/**
	 * 排版宫格：在应用程序数目发生改变时可被单独调用 同时改变整个控件的尺寸 mustLayout 是否必须重排版：在初始化或者横竖屏切换时为true
	 * return true 则需要重新设置边框图片
	 */
	public boolean layoutGrid(boolean mustLayout) {

		// 告诉XGrid拖入至桌面区域的信息
		// mGrid.setHomeIcon(mToDesk);
		if (mUtils.isVertical()) {
			// 手机竖屏时横向布局
			// 设置行列数(竖屏时列数与功能表中保持一致)
			FuncAppDataHandler handler = AppFuncFrame.getDataHandler();
			int col = mUtils.getFolderColumn(handler.getStandard());
			mGrid.setColunmNums(col);
			int numApps = 0;
			if (mAdapter != null) {
				mAdapter.loadApp();
				numApps = mAdapter.getCount();
				if (numApps < 4 && col < 4) {
					if (mGrid.getRowNums() == 1) {
						if (mustLayout == false) {
							return false;
						}
					}
					mGrid.setRowNums(1);
					mGrid.setSize(mUtils.getScreenWidth(), mGridPaddingTop + mIconHeight
							+ mGridPaddingBottom);
				} else if (numApps >= 4 && numApps <= 8 && col < 4) {
					if (mGrid.getRowNums() == 2) {
						if (mustLayout == false) {
							return false;
						}
					}
					mGrid.setRowNums(2);
					mGrid.setSize(mUtils.getScreenWidth(), mGridPaddingTop + 2 * mIconHeight
							+ mGridPaddingBottom);
				} else {
					int row;
					row = numApps / col + ((numApps % col > 0) ? 1 : 0);
					if (mUtils.getSmallerBound() <= 240) {
						if (row > 2) {
							row = 2;
						}
					} else {
						if (row > 3) {
							row = 3;
						}
					}

					if (mGrid.getRowNums() == row) {
						if (mustLayout == false) {
							return false;
						}
					}
					mGrid.setRowNums(row);
					mGrid.setSize(mUtils.getScreenWidth(), mGridPaddingTop + row * mIconHeight
							+ mGridPaddingBottom);
					// 只有一屏的时候不需要pading
					if (col * 3 >= numApps) {
						mGrid.setPaddingTop(0);
					} else {
						mGrid.setPaddingTop(mGridPaddingTop);
					}
				}
			} else {
				if (mGrid.getRowNums() == 1) {
					if (mustLayout == false) {
						return false;
					}
				}
				mGrid.setRowNums(1);
				mGrid.setSize(mUtils.getScreenWidth(), mGridPaddingTop + mIconHeight
						+ mGridPaddingBottom);
			}

			// 设置滑动方向
			mGrid.setVScroll(false);
			mGrid.setOrientation(XBaseGrid.VERTICAL);
			if (col * 3 >= numApps) {
				mGrid.setPaddingTop(0);
			} else {
				mGrid.setPaddingTop(mGridPaddingTop);
			}
			// 改变整个控件的大小
			setSize(mUtils.getScreenWidth(), mEditTabHeight + mGrid.getHeight() + mImageBottomH);
		} else {
			// 手机横向时垂直布局
			// 设置行列数
			int numApps = 0;
			if (mAdapter != null) {
				mAdapter.loadApp();
				numApps = mAdapter.getCount();
				if (numApps <= 5) {
					if (mGrid.getColunmNums() == 1) {
						if (mustLayout == false) {
							return false;
						}
					}
					mGrid.setColunmNums(1);
					mGrid.setSize(mWidthH - mGridMarginH - mGridPaddingLeft, mGridPaddingTop
							+ mIconHeight + mGridPaddingBottom);
				} else {
					if (mGrid.getColunmNums() == 2) {
						if (mustLayout == false) {
							return false;
						}
					}
					mGrid.setColunmNums(1);
					mGrid.setSize(mWidthH - mGridMarginH - mGridPaddingLeft, mGridPaddingTop
							+ mIconHeight + mGridPaddingBottom);
				}
			} else {
				if (mGrid.getColunmNums() == 1) {
					if (mustLayout == false) {
						return false;
					}
				}
				mGrid.setColunmNums(1);
				mGrid.setSize(mWidthH - mGridMarginH - mGridPaddingLeft, mGridPaddingTop
						+ mIconHeight + mGridPaddingBottom);
			}
			// if(smallerBound >240){
			// mGrid.setRowNums(mUtils.getFolderColumn(mIconHeight));
			// }else{
			// mGrid.setRowNums(6);
			// }
			mGrid.setRowNums(mUtils.getFolderColumn(mIconHeight));
			// 设置滑动方向
			mGrid.setVScroll(false);
			mGrid.setOrientation(XBaseGrid.HORIZONTAL);
			// 只有一屏的时候不要pading
			if (numApps <= mGrid.getColunm()) {
				mGrid.setPaddingTop(0);
			} else {
				mGrid.setPaddingTop(mGridPaddingTop);
			}
			// 改变整个控件的大小
			// mGrid.setSize(mGrid.getWidth() - mGridPaddingLeft,
			// mGrid.getHeight());
			setSize(mWidthH, mEditTabHeight + mGrid.getHeight() + mImageBottomH);
		}

		mGrid.setXY(0, mEditTabHeight + 2);
		mGrid.setPaddingLeft(mGridPaddingLeft);
		mGrid.setPaddingRight(mGridPaddingRight);
		mGrid.setPaddingBottom(mGridPaddingBottom);
		// setFrameImage();
		return true;
	}

	@Override
	public void notify(int key, Object obj) {
		switch (key) {
			case AppFuncConstants.LAYOUTFOLDERGRID : {
				layoutGrid(false);
				mGrid.updateLayoutParams();
				mGrid.requestLayout();
				break;
			}

			case AppFuncConstants.ENTEREDITMODE : {
				mInEditMode = true;
				break;
				// mText.setEditable(true);
			}

			case AppFuncConstants.EXITEDITMODEL : {
				mInEditMode = false;
				// mText.setEditable(false);
				break;
			}

			case AppFuncConstants.THEME_CHANGE : {
				mBgImg = null;
				mText.setBgImage(null);
				mSortButton.setIcon(null);
				mSortButton.setIconPressed(null);
				mButton.setIcon(null);
				mButton.setIconPressed(null);
				break;
			}

			case AppFuncConstants.LOADTHEMERES : {
				mIsThemeChanged = true;
				String theme = GOLauncherApp.getThemeManager().getCurThemePackage();
				if (theme != null && theme.equals(ThemeManager.DEFAULT_THEME_PACKAGE)) {
					mIsDefaultTheme = true;
				} else {
					mIsDefaultTheme = false;
				}
				break;
			}

			case AppFuncConstants.LOCKCHANGES : {
				boolean editable = !GOLauncherApp.getSettingControler().getScreenSettingInfo().mLockScreen;
				mGrid.setDragable(editable);
				mText.setEditable(editable);
				break;
			}
			case AppFuncConstants.RELOAD_FOLDER_THEMES : {
				mIsThemeChanged = true;
				break;
			}
			case AppFuncConstants.FOLDER_RENAME :
				Bundle bundle = (Bundle) obj;
				String name = bundle.getString("newName");
				long folderId = bundle.getLong("folderId");
				String oldName = mText.getText();
				if (name == null || (name != null && name.trim().length() == 0)) {
					name = GOLauncherApp.getApplication().getResources()
							.getString(R.string.folder_name);
				}
				FunFolderItemInfo folderInfo = (FunFolderItemInfo) AppFuncFrame.getFunControler().getFunAppItemInfo(folderId);
				if (folderInfo != null) {
					if (name.compareTo(oldName) != 0) {
						String text = name;
						text = text.replaceAll("\\s+", " ");
						try {
							folderInfo.setTitle(text);
						} catch (DatabaseException e) {
							AppFuncExceptionHandler.handle(e);
							return;
						}
						// 通知桌面重命名
						ArrayList<String> nameList = new ArrayList<String>();
						// 第一个为新的名字
						nameList.add(text);
						// 第二个为以前的名字
						nameList.add(oldName);
						GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
								IDiyMsgIds.SCREEN_FOLDER_RENAME, 0, folderInfo.getFolderId(),
								nameList);
						GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME,
								IDiyMsgIds.SCREEN_FOLDER_RENAME, 0, folderInfo.getFolderId(),
								nameList);
					}
				}
				break;
		}
	}

	@Override
	protected void drawCurrentFrame(Canvas canvas) {

		// int sc = canvas.save();
		// // ImageUtil.drawImage(canvas, mBgImg, 1, 0, 0, mWidth, mHeight,
		// // mPainter);
		// // drawMask(canvas);
		// if (mBgBt!=null&&!mBgBt.isRecycled()) {
		// canvas.drawBitmap(mBgBt, 0,0, mPainter);
		// }else {
		// ImageUtil.drawImage(canvas, mBgImg, 1, 0, 0, mWidth, mHeight,
		// mPainter);
		// }
		// canvas.restoreToCount(sc);
		/**
		 * @edit by huangshaotao
		 * @date 2012-5-21 去掉mask3角形 if (mRebuildBg) { mRebuildBg = false;
		 *       buidBGBitmap(); }
		 * 
		 *       if (mBgBt!=null&&!mBgBt.isRecycled()) {
		 *       canvas.drawBitmap(mBgBt, 0, 0, mPainter); }else { int sc =
		 *       canvas.saveLayer(0, 0, mWidth, mHeight, null,
		 *       Canvas.MATRIX_SAVE_FLAG | // Canvas.CLIP_SAVE_FLAG |
		 *       Canvas.HAS_ALPHA_LAYER_SAVE_FLAG |
		 *       Canvas.FULL_COLOR_LAYER_SAVE_FLAG //
		 *       |Canvas.CLIP_TO_LAYER_SAVE_FLAG ); ImageUtil.drawImage(canvas,
		 *       mBgImg, 1, 0, 0, mWidth, mHeight, mPainter); try {
		 *       drawMask(canvas); } catch (OutOfMemoryError e) { } catch
		 *       (StackOverflowError e) { } canvas.restoreToCount(sc); }
		 */
		drawBg(canvas);
		drawAllChildComponents(canvas);
		/**
		 * @date 2012-3-26
		 * @edit by huangshaotao 功能表新样式不需要画分割线
		 */
		// 画线
		// if (mIsDrawCutline != 0) {
		// int stopX;
		// if (mUtils.isVertical()) {
		// stopX = mUtils.getScreenWidth() - 10 * mUtils.getSmallerBound()
		// / 480;
		// } else {
		// stopX = mWidth - 10 * mUtils.getSmallerBound() / 480;
		// }
		// mLinePaint.setColor(0x9A000000);
		// canvas.drawLine(10 * mUtils.getSmallerBound() / 480,
		// mText.mY+mText.getHeight()+mMargin_h, stopX,
		// mText.mY+mText.getHeight()+mMargin_h, mLinePaint);
		// mLinePaint.setColor(0x33FFFFFF);
		// canvas.drawLine(10 * mUtils.getSmallerBound() / 480,
		// mText.mY+mText.getHeight()+mMargin_h, stopX,
		// mText.mY+mText.getHeight()+mMargin_h, mLinePaint);
		// }
	}

	@Override
	public boolean handleChanges(MessageID msgId, Object obj1, Object obj2) {
		switch (msgId) {
			case UPDATE_FOLDERTITLE : {
				// 得到后台通知更新文件夹名称
				// mFolderInfo.setFolderTitle((String)obj1);
				mText.setText(mFolderInfo.getTitle());
			}
				return true;
		}
		return false;
	}

	@Override
	public boolean onEventFired(XComponent component, byte eventType, Object event, int arg,
			Object object) {
		// if (component instanceof XButton) {
		// if (eventType == EventType.HIDE_FOLDER) {
		//
		// }
		// } else
		if (component instanceof XText) {
			if (eventType == EventType.SHOW_EDITDIALOG) {
				// 显示弹出框
				// FolderNamingHandler.getInstance(mActivity).showEditDialog((String)
				// event, mFolderInfo);
				Intent intent = new Intent(mActivity, RenameActivity.class);
				CharSequence name = (String) event;
				intent.putExtra(RenameActivity.NAME, name.toString());
				intent.putExtra(RenameActivity.HANDLERID, IDiyFrameIds.APPFUNC_FRAME);
				intent.putExtra(RenameActivity.SHOW_RECOMMENDEDNAME, true);
				intent.putExtra(RenameActivity.FINISH_WHEN_CHANGE_ORIENTATION, true);
				if (mFolderInfo != null) {
					intent.putExtra(RenameActivity.ITEMID, mFolderInfo.getFolderId());
				}
				mActivity.startActivityForResult(intent, IRequestCodeIds.REQUEST_RENAME);
			}
		}
		return false;
	}

	private void startEditActivity() {
		//		AppFuncMainView.sOpenFuncSetting = true;
		XViewFrame.getInstance().getAppFuncMainView().mOpenFuncSetting = true;
		Intent intent = new Intent(mActivity, AppFuncModifyFolderActivity.class);
		intent.putExtra(AppFuncConstants.FOLDER_ID, mFolderInfo.getFolderId());
		int requestCode = IRequestCodeIds.REQUEST_MODIFY_APPDRAWER_FOLDER;
		mActivity.startActivityForResult(intent, requestCode);
	}

	@Override
	public boolean onKey(KeyEvent event) {
		switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_BACK : {
				boolean isEmpty = false;
				if (mAdapter != null) {
					if (mAdapter.getCount() == 0) {
						isEmpty = true;
					}
				}
				if (mGrid.isInDragStatus() == false || isEmpty) {
					// 通知TabComponent收起文件夹
					if (event.getAction() == KeyEvent.ACTION_UP) {
						mGrid.setDragStatus(false);
						mEventListener.onEventFired(this, EventType.HIDE_FOLDER, null, 0, null);
					}
					return true;
				} else {
					if (event.getAction() == KeyEvent.ACTION_UP) {
						boolean ret = mGrid.onKey(event);
						mInEditMode = mGrid.isInDragStatus();
						return ret;
					}
				}
			}
				break;
			case KeyEvent.KEYCODE_DPAD_DOWN :
			case KeyEvent.KEYCODE_DPAD_RIGHT : {
				// 通知XGrid聚焦
				if (event.getAction() == KeyEvent.ACTION_UP) {
					if (mGrid.isFocused() == false) {
						mGrid.setFocused(true);
						// DeliverMsgManager.getInstance().onChange(mGrid.mId,
						// AppFuncConstants.FOCUS, null);
					}
				}
			}
				break;
		}
		return mGrid.onKey(event);
	}

	public boolean isInEditMode() {
		return mInEditMode;
	}

	public XBaseGrid getGrid() {
		return mGrid;
	}

	/**
	 * 开始平移动画
	 * 
	 * @param dstX
	 * @param dstY
	 * @return 动画持续帧数
	 */
	public int startAnimation(int dstX, int dstY) {
		if ((mMotion != null) && (mMotion.isFinished() == false)) {
			mMotion.stop();
			mMotion = null;
		}
		mCurrentStep = 0;
		// mTotalStep = mHeight / Speed;
		mTotalStep = 1;
		mMotion = new XALinear(1, XALinear.XALINEAR_ECSPEED, mX, mY, dstX, dstY, mTotalStep, 0, 0);
		setMotionFilter(mMotion);
		if (mY > dstY) {
			mGrid.clearDragStatus(); // 拖动图标到功能表的时候，文件夹收起，清除拖动状态，以免图标被文件夹和功能表都绘制
			mStatus = STATUS.LEAVING;
			// XGrid释放焦点
			if (mGrid.isFocused()) {
				mGrid.setFocused(false);
			}
		} else {
			mStatus = STATUS.ENTERING;
		}
		return mTotalStep;
	}

	@Override
	protected boolean animate() {
		if (mMotion != null) {
			if (mCurrentStep < mTotalStep) {
				mCurrentStep++;
				return true;
			} else {
				clearMotion();
				return true;
			}
		}
		return false;
	}

	public void clearMotion() {
		if (mMotion != null) {
			detachAnimator(mMotion);
			if (mMotion.GetStartY() >= mMotion.GetEndY()) {
				// 收起文件夹的动画完成，需要通知TabComponent移除文件夹
				if (mEventListener != null) {
					mEventListener.onEventFired(this, EventType.REMOVE_FOLDER, null, 0, null);
				}
				mIsTouchDown = false;
				mStatus = STATUS.INITIALIZED;
			} else {
				mStatus = STATUS.ENTERED;
			}
			mMotion = null;
			mCurrentStep = 0;
		}
	}

	public XMotion getMotion() {
		return mMotion;
	}

	public void stopMotion() {
		if (mMotion != null) {
			if (mMotion.isFinished() == false) {
				mMotion.stop();
			}
			mMotion = null;
		}
	}

	@Override
	public boolean onTouch(MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN : {
				mShowWithoutTouch = false;
				if (XYInRange((int) event.getX(), (int) event.getY())) {
					mIsTouchDown = true;
					return super.onTouch(event);
				}
			}
				return false;
			case MotionEvent.ACTION_MOVE : {
				if (mIsTouchDown) {
					mDragIcon = mGrid.getDragComponent();
					return super.onTouch(event);
				}
			}
				return false;
			case MotionEvent.ACTION_UP :
				if (mIsTouchDown) {
					mIsTouchDown = false;

					boolean ret = super.onTouch(event);
					if (ret == true) {
						// 如果点击区域不在folder范围且返回true，说明XGrid根据此事件
						// 做了某些“善后”工作，此时需要保持返回false
						if (XYInRange((int) event.getX(), (int) event.getY()) == false) {
							if (mDragIcon != null) {
								ret = false;
							}
						}
					} else {
						if (XYInRange((int) event.getX(), (int) event.getY()) == true) {
							ret = true;
						} else {
							if (mDragIcon == null) {
								ret = true;
							}
						}
					}
					return ret;
				}
				if (mShowWithoutTouch) {
					return true;
				}
		}
		return false;
	}

	public boolean isTouchDown() {
		return mIsTouchDown;
	}

	public FolderAdapter getAdapter() {
		return mAdapter;
	}

	public STATUS getStatus() {
		return mStatus;
	}

	public void setStatus(AppFuncFolder.STATUS status) {
		mStatus = status;
	}

	private void setLayoutParams() {

		AppFuncTabBasicContent atb = XViewFrame.getInstance().getAppFuncMainView()
				.getCurrentContent();
		int width = mUtils.getScreenWidth();
		if (atb instanceof AllAppTabBasicContent) {
			AllAppTabBasicContent allapptab = (AllAppTabBasicContent) atb;
			width = allapptab.getXGrid().getWidth();
		}

		mEditBoxWidthV = width
				- mUtils.getStandardSize(BUTTON_UP_WIDTH * 2 + MARGIN_H + BUTTON_PADDING * 2);
		mEditBoxWidthH = width
				- mUtils.getStandardSize(BUTTON_UP_WIDTH * 2 + MARGIN_H + BUTTON_PADDING * 2);

		if (Machine.isTablet(mActivity)) {
			mEditTabHeight = 60;
			mEditBoxHeight = 40;
		} else {
			mEditTabHeight = mUtils.getStandardSize(60);
			mEditBoxHeight = mUtils.getStandardSize(40);
		}
		mMarginH = mUtils.getStandardSize(MARGIN_H);
		// mTextSize = 24 * mUtils.getSmallerBound() / 480;
		setFontSize();
		mImageBottomH = mUtils.getStandardSize(34);
		// 横屏时的宽度
		// mWidth_h = mFolderW * mUtils.getLongerBound() / 800;
		mWidthH = mFolderW;
		mGridMarginH = mUtils.getStandardSize(7);
		mGridPaddingTop = mUtils.getStandardSize(24);
		DisplayMetrics metrics = mActivity.getResources().getDisplayMetrics();
		int padding = (int) (AppFuncConstants.SCROLL_SIZE * metrics.density);
		mGridPaddingLeft = mUtils.getStandardSize(padding);
		mGridPaddingRight = mUtils.getStandardSize(padding);
		mGridPaddingBottom = mUtils.getStandardSize(14);
		mIconHeight = GoLauncher.isLargeIcon() ? mUtils
				.getDimensionPixelSize(R.dimen.appfunc_folder_icon_size_large) : mUtils
				.getDimensionPixelSize(R.dimen.appfunc_folder_icon_size);
	}

	@Override
	public void onBCChange(int msgId, int param, Object object,
			@SuppressWarnings("rawtypes") List objects) {
		switch (msgId) {
			case FunFolderItemInfo.TITLECHANGED : {
				if (mText != null) {
					mText.setText((String) object);
				}
				break;
			}
			default :
				break;
		}
	}

	/**
	 * 取消后台信息的注册
	 */
	public void unregisterObserver() {
		if (mFolderInfo != null) {
			mFolderInfo.unRegisterObserver(this);
		}
	}

	/**
	 * 从文件夹删除当前抓起的图标 return 被删除图标的信息
	 * 
	 * @throws DatabaseException
	 */
	public FunAppItemInfo removeIcon() throws DatabaseException {
		if ((mGrid != null) && (mDragIcon instanceof ApplicationIcon)) {
			FunItemInfo dragIconInfo = ((ApplicationIcon) mDragIcon).getInfo();
			if ((mAdapter != null) && (dragIconInfo instanceof FunAppItemInfo)) {
				// 操作后台数据
				mAdapter.removeApp((FunAppItemInfo) dragIconInfo);
				ArrayList<FunAppItemInfo> appsInFolder = mFolderInfo.getFunAppItemInfosForShow();
				if (appsInFolder.size() <= 1) {
					AppFuncFrame.getFunControler().removeFolder(mFolderInfo);
				}
				return (FunAppItemInfo) dragIconInfo;
			}
		}
		return null;
	}

	public XComponent getDragIcon() {
		return mDragIcon;
	}

	public void resetDragIcon() {
		mDragIcon = null;
	}

	public static void destroyInstance() {
		if (sInstance != null) {
			sInstance.unregisterObserver();
			sInstance.unRegisterHandler();
			sInstance = null;
		}
	}

	public FunFolderItemInfo getFolderInfo() {
		return mFolderInfo;
	}

	private void layoutBgImage() {
		// if (isNinePatchDrawable)
		// return;

		int tempwidth = 0;
		int tempHeight = 0;

		if (mBgImg != null) {
			mImageWidth = mBgImg.getIntrinsicWidth();
			mImageHeigth = mBgImg.getIntrinsicHeight();
		}

		if (mUtils.isVertical()) {
			tempwidth = mUtils.getScreenWidth();
		} else {
			tempwidth = mWidthH;
		}

		if (mImageWidth > 0) {
			tempHeight = (tempwidth * mImageHeigth) / mImageWidth;
		}

		if (mImageHeigthBean != 0) {
			mImageBottomH = mUtils.getStandardSize(mImageHeigthBean);
			if (mImageBottomH > mIconHeight / 2) {
				mImageBottomH = mIconHeight / 2;
			}
		}

		mImageHeigth = tempHeight;
		mImageWidth = tempwidth;
	}

	private void loadResouce() {
		String themePackage = GOLauncherApp.getSettingControler().getScreenStyleSettingInfo()
				.getFolderStyle();
		Drawable bgDrawable = mThemeController.getDrawable(
				mThemeController.getThemeBean().mFolderBean.mFolderBgPath, themePackage, false);

		if (bgDrawable != null) {
			mBgImg = bgDrawable;
		}

		Drawable editDrawable = mThemeController.getDrawable(
				mThemeController.getThemeBean().mFolderBean.mFolderEditBgPath, themePackage, false);

		if (editDrawable != null) {
			mText.setBgImage(editDrawable);
		}

		mText.setTxtColor(mThemeController.getThemeBean().mFolderBean.mFolderEditTextColor);

		Drawable buttonDrawable = mThemeController.getDrawable(
				mThemeController.getThemeBean().mFolderBean.mFolderAddButton, themePackage, false);
		if (buttonDrawable == null) {
			buttonDrawable = mActivity.getResources().getDrawable(R.drawable.appfunc_up);
		}
		if (buttonDrawable != null) {
			mButton.setIcon(buttonDrawable);
		}
		//
		buttonDrawable = mThemeController.getDrawable(
				mThemeController.getThemeBean().mFolderBean.mFolderAddButtonLight, themePackage,
				false);
		if (buttonDrawable == null) {
			buttonDrawable = mActivity.getResources().getDrawable(R.drawable.appfunc_up_light);
		}
		if (buttonDrawable != null) {
			mButton.setIconPressed(buttonDrawable);
		}
		buttonDrawable = mThemeController.getDrawable(
				mThemeController.getThemeBean().mFolderBean.mFolderSortButton, themePackage, false);
		if (buttonDrawable != null) {
			mSortButton.setIcon(buttonDrawable);
		}
		buttonDrawable = mThemeController.getDrawable(
				mThemeController.getThemeBean().mFolderBean.mFolderSortButtonLight, themePackage,
				false);
		if (buttonDrawable != null) {
			mSortButton.setIconPressed(buttonDrawable);
		}
		mImageHeigthBean = mThemeController.getThemeBean().mFolderBean.mImageBottomH;
	}

	public void isDragalbe(boolean on) {
		mGrid.setDragable(on);
	}

	public void setGridEffector(int type) {
		mGrid.setGridEffector(type);
	}

	public void setCustRandomEffectors(int[] effects) {
		mGrid.setCustRandomEffectors(effects);
	}

	public void setCycleMode(boolean cycle) {
		mGrid.setCycleMode(cycle);
	}

	private void setFontSize() {
		mTextSize = (int) (GoLauncher.getAppFontSize() * DrawUtils.sDensity) + 5;
	}

	/**
	 * 文件夹退出时用于重置状态
	 */
	public void resetGrid() {
		if (mGrid != null) {
			mGrid.updateLayoutParams();
			// mGrid.requestLayout();
			mGrid.setIsMoveStop(false);
		}
	}

	/**
	 * 设置文件夹宽度
	 */
	public void setFolderWidth(int width) {
		mFolderW = width;
		mWidthH = mFolderW;
	}

	/**
	 * @edit by huangshaotao
	 * @date 2012-5-21 去掉mask3角形
	 */
	// private void drawMask(Canvas canvas){
	//
	// if (mTriangleMask!=null&&mSrcIcon!=null) {
	// int triangleW = mTriangleH;//三角型的宽度
	// int iconX = mSrcIcon.getAbsX();
	// if (mSrcGrid!=null&&!mSrcGrid.isVScroll()) {
	// iconX = iconX - mSrcGrid.getOffset();
	// }
	// if (mSrcGrid!=null&&mSrcGrid.isVScroll()) {
	// if (mSrcGrid.isShowSearch()) {
	// iconX = iconX - mSrcGrid.getWidth();
	// }
	// }
	// int triangleX = iconX -
	// getAbsX()+((mSrcIcon.getWidth()-triangleW)/2);//三角形的绘制位置
	// int maskH = mTriangleH;
	// int maskW = mWidth;
	// int maskX = 0;
	// int maskY = 0;
	// //创建一张全透明背景图片，该图宽为文件夹宽，高为三角形高
	// Bitmap temp = Bitmap.createBitmap(maskW, maskH, Bitmap.Config.ARGB_8888);
	// Canvas canvasTemp = new Canvas(temp);
	//
	// if (mIsDefaultTheme&&mTriangleDefault!=null) {
	// //如果是默认主题则绘制一个不透明的三角形
	// canvasTemp.drawBitmap(mTriangleDefault.getBitmap(), triangleX, 0,
	// mPainter);
	// canvas.drawBitmap(temp, maskX, maskY, mPainter);
	// }else {
	// //在这张全透明背景图上绘制透明三角形
	// canvasTemp.drawBitmap(mTriangleMask.getBitmap(), triangleX, 0, mPainter);
	// //把这张背景图绘制到文件夹上面，形成遮罩
	// Xfermode xf = mPainter.getXfermode();
	// mPainter.setXfermode(pdf);
	// canvas.drawBitmap(temp, maskX, maskY, mPainter);
	// mPainter.setXfermode(xf);
	// temp.recycle();
	// }
	// }
	// }

	public void setSrcFolderIcon(ApplicationIcon icon) {
		mSrcIcon = icon;
	}

	private void drawBg(Canvas canvas) {
		if (mIsDefaultTheme) {
			if (mBgImg != null) {
				ImageUtil.drawImage(canvas, mBgImg, 1, 0, 0, mWidth, mHeight, mPainter);
			}
			Drawable grankDrawable = mActivity.getResources().getDrawable(
					R.drawable.folder_bg_grank);
			// ImageUtil.drawImage(canvas, grankDrawable, 0, 0, mTriangleH + 1,
			// mWidth, mHeight - 1, new Paint());
			ImageUtil.drawImage(canvas, grankDrawable, 0, 0, 0, mWidth, mHeight - 1, new Paint());
		} else {
			if (mBgImg != null) {
				ImageUtil.drawImage(canvas, mBgImg, 1, 0, 0, mWidth, mHeight, mPainter);
			}
		}
	}

	// private void buidBGBitmap(){
	//
	// if (mBgBt!=null&&!mBgBt.isRecycled()) {
	// mBgBt.recycle();
	// }
	// try {
	// mBgBt = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
	// Canvas canvas = new Canvas(mBgBt);
	// ImageUtil.drawImage(canvas, mBgImg, 1, 0, 0, mWidth, mHeight,
	// mPainter);
	// if (mIsDefaultTheme)
	// {
	// Drawable grankDrawable =
	// mActivity.getResources().getDrawable(R.drawable.folder_bg_grank);
	// ImageUtil.drawImage(canvas, grankDrawable, 0, 0, mTriangleH + 1, mWidth,
	// mHeight - 1, new Paint());
	// }
	// // drawMask(canvas);
	// } catch (OutOfMemoryError e) {
	// mRebuildBg = true;
	// }
	//
	// }

	@Override
	public void onClick(XComponent view) {
		if (view == mSortButton) {
			showSelectSort();
			//用户行为统计
			StatisticsData.countUserActionData(
					StatisticsData.FUNC_ACTION_ID_APPLICATION,
					StatisticsData.USER_ACTION_THIRTEEN,
					IPreferencesIds.APP_FUNC_ACTION_DATA);
		}
		if (view == mButton) {
			if (!GOLauncherApp.getSettingControler().getScreenSettingInfo().mLockScreen) {
				startEditActivity();
				//用户行为统计
				StatisticsData.countUserActionData(
						StatisticsData.FUNC_ACTION_ID_APPLICATION,
						StatisticsData.USER_ACTION_EIGHTEEN,
						IPreferencesIds.APP_FUNC_ACTION_DATA);
			}
		}
	}

	public void showSelectSort() {
		SharedPreferences preferences = GoLauncher.getContext().getPreferences(Context.MODE_PRIVATE);
		int type = preferences.getInt(String.valueOf(mFolderInfo.getFolderId()), -1);
		try {
			DialogSingleChoice mDialog = new DialogSingleChoice(mActivity);
			mDialog.show();
			mDialog.setTitle(R.string.dlg_sortChangeTitle);
			final CharSequence[] items = mActivity.getResources().getTextArray(R.array.folder_select_sort_style);
			mDialog.setItemData(items, type, true);
			mDialog.setOnItemClickListener(new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int item) {
					beginSortIcon(item);
				}
			});
		} catch (Exception e) {
			try {
				DeskToast.makeText(mContext, R.string.alerDialog_error, Toast.LENGTH_SHORT).show();
			} catch (OutOfMemoryError error) {
				OutOfMemoryHandler.handle();
			}
		}
	}

	private void beginSortIcon(final int itemCaused) {
		ProgressDialog dialog = ProgressDialog.show(mActivity, null,
				mActivity.getString(R.string.sort_processing), true);
		dialog.show();
		SharedPreferences preferences = mActivity.getPreferences(Context.MODE_PRIVATE);
		Editor editor = preferences.edit().putInt(String.valueOf(mFolderInfo.getFolderId()),
				itemCaused);
		editor.commit();
		try {
			switch (itemCaused) {
				case FunAppSetting.SORTTYPE_LETTER :
					mFolderInfo.sortByLetterAndSave("ASC");
					//用户行为统计
					StatisticsData.countUserActionData(
							StatisticsData.FUNC_ACTION_ID_APPLICATION,
							StatisticsData.USER_ACTION_FOUTEEN,
							IPreferencesIds.APP_FUNC_ACTION_DATA);
					break;
				case FunAppSetting.SORTTYPE_TIMENEAR :
					mFolderInfo.sortByTimeAndSave(mContext, "DESC");
					//用户行为统计
					StatisticsData.countUserActionData(
							StatisticsData.FUNC_ACTION_ID_APPLICATION,
							StatisticsData.USER_ACTION_SIXTEEN,
							IPreferencesIds.APP_FUNC_ACTION_DATA);
					break;
				case FunAppSetting.SORTTYPE_TIMEREMOTE :
					mFolderInfo.sortByTimeAndSave(mContext, "ASC");
					//用户行为统计
					StatisticsData.countUserActionData(
							StatisticsData.FUNC_ACTION_ID_APPLICATION,
							StatisticsData.USER_ACTION_FIFTEEN,
							IPreferencesIds.APP_FUNC_ACTION_DATA);
					break;
				case FunAppSetting.SORTTYPE_FREQUENCY :
					mFolderInfo.sortByFrequencyAndSave(mContext, "DESC");
					//用户行为统计
					StatisticsData.countUserActionData(
							StatisticsData.FUNC_ACTION_ID_APPLICATION,
							StatisticsData.USER_ACTION_SEVENTEEN,
							IPreferencesIds.APP_FUNC_ACTION_DATA);
					break;
				default :
					break;
			}
			mGrid.requestLayout();
			XViewFrame.getInstance().getAppFuncMainView().requestLayout();
			dialog.dismiss();
		} catch (Exception e) {
			AppFuncExceptionHandler.handle(e);
			dialog.dismiss();
			dialog = null;
		}
	}

	public void removeFolderInfo() {
		mFolderInfo = null;
	}
}
