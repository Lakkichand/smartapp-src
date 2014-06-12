package com.jiubang.ggheart.apps.desks.diy.frames.screen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.view.InflateException;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.AppUtils;
import com.go.util.AsyncHandler;
import com.go.util.ConvertUtils;
import com.go.util.Utilities;
import com.go.util.animation.MyAnimationUtils;
import com.go.util.device.Machine;
import com.go.util.graphics.BitmapUtility;
import com.go.util.graphics.DrawUtils;
import com.go.util.graphics.effector.united.CoupleScreenEffector;
import com.go.util.lib.AppWidgetManagerWrapper;
import com.go.util.log.LogConstants;
import com.jiubang.core.framework.AbstractFrame;
import com.jiubang.core.framework.IFrameManager;
import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.core.message.IMsgType;
import com.jiubang.ggheart.apps.desks.appfunc.model.AppFuncScreenItemInfo;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.IRequestCodeIds;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.desks.diy.StatusBarHandler;
import com.jiubang.ggheart.apps.desks.diy.WallpaperControler;
import com.jiubang.ggheart.apps.desks.diy.frames.cover.CoverFrame;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.DockIconView;
import com.jiubang.ggheart.apps.desks.diy.frames.drag.DragFrame;
import com.jiubang.ggheart.apps.desks.diy.frames.drag.ScreenDragHandler;
import com.jiubang.ggheart.apps.desks.diy.frames.preview.ScreenPreviewMsgBean;
import com.jiubang.ggheart.apps.desks.diy.frames.preview.SensePreviewFrame;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditBoxFrame;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.AppTab;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.BaseTab;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.LockerThemeTab;
import com.jiubang.ggheart.apps.desks.dock.DockUtil;
import com.jiubang.ggheart.apps.desks.imagepreview.ChangeIconPreviewActivity;
import com.jiubang.ggheart.apps.desks.imagepreview.ImagePreviewResultType;
import com.jiubang.ggheart.apps.gowidget.AbsWidgetInfo;
import com.jiubang.ggheart.apps.gowidget.GoWidgetActionReceiver;
import com.jiubang.ggheart.apps.gowidget.GoWidgetConstant;
import com.jiubang.ggheart.apps.gowidget.GoWidgetManager;
import com.jiubang.ggheart.apps.gowidget.GoWidgetProviderInfo;
import com.jiubang.ggheart.apps.gowidget.InnerWidgetInfo;
import com.jiubang.ggheart.apps.gowidget.WidgetParseInfo;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreStatisticsUtil;
import com.jiubang.ggheart.common.controler.CommonControler;
import com.jiubang.ggheart.components.BubbleTextView;
import com.jiubang.ggheart.components.DeskToast;
import com.jiubang.ggheart.components.GoProgressBar;
import com.jiubang.ggheart.components.IQuickActionId;
import com.jiubang.ggheart.components.QuickActionMenu;
import com.jiubang.ggheart.components.QuickActionMenu.onActionListener;
import com.jiubang.ggheart.components.advert.AdvertConstants;
import com.jiubang.ggheart.components.advert.AdvertControl;
import com.jiubang.ggheart.components.advert.AdvertHomeScreenUtils;
import com.jiubang.ggheart.components.advert.AdvertInfo;
import com.jiubang.ggheart.components.renamewindow.RenameActivity;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.AppDataEngine;
import com.jiubang.ggheart.data.BroadCaster.BroadCasterObserver;
import com.jiubang.ggheart.data.DataProvider;
import com.jiubang.ggheart.data.DataType;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.DesktopSettingInfo;
import com.jiubang.ggheart.data.info.DockItemInfo;
import com.jiubang.ggheart.data.info.EffectSettingInfo;
import com.jiubang.ggheart.data.info.FavoriteInfo;
import com.jiubang.ggheart.data.info.FeatureItemInfo;
import com.jiubang.ggheart.data.info.GoWidgetBaseInfo;
import com.jiubang.ggheart.data.info.IItemType;
import com.jiubang.ggheart.data.info.ItemInfo;
import com.jiubang.ggheart.data.info.RelativeItemInfo;
import com.jiubang.ggheart.data.info.ScreenAppWidgetInfo;
import com.jiubang.ggheart.data.info.ScreenFolderInfo;
import com.jiubang.ggheart.data.info.ScreenLiveFolderInfo;
import com.jiubang.ggheart.data.info.ScreenSettingInfo;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.data.info.ShortCutSettingInfo;
import com.jiubang.ggheart.data.info.ThemeSettingInfo;
import com.jiubang.ggheart.data.info.UserFolderInfo;
import com.jiubang.ggheart.data.statistics.AppRecommendedStatisticsUtil;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.data.theme.ImageExplorer;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.data.theme.bean.ThemeInfoBean;
import com.jiubang.ggheart.launcher.AppIdentifier;
import com.jiubang.ggheart.launcher.CheckApplication;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.LauncherEnv;
import com.jiubang.ggheart.launcher.LauncherWidgetHost;
import com.jiubang.ggheart.launcher.PackageName;
import com.jiubang.ggheart.launcher.ThreadName;
import com.jiubang.ggheart.plugin.notification.NotificationControler;
import com.jiubang.ggheart.plugin.notification.NotificationType;
import com.jiubang.ggheart.screen.back.BackWorkspace;

/**
 * 屏幕层
 * 
 * @author luo
 * @version 1.0
 */
public class ScreenFrame extends AbstractFrame
		implements
			Search.ISearchEventListener,
			BroadCasterObserver,
			onActionListener,
			AnimationListener {
	private final static String LOG_TAG = "screenFrame";

	// for init
	private final static int START_DESKTOP_LOADER = 1;
	private final static int REPLACE_FAV_WIDGET = 2;

	// for setting
	private final static int UPDATE_DESKTOP_SETTING = 10;
	private final static int UPDATE_SCREEN_SETTING = 11;
	private final static int UPDATE_EFFECT_SETTING = 12;
	private final static int UPDATE_THEME_SETTING = 13;

	// for preview
	private final static int DELETE_SCREEN = 20;
	private final static int ADD_SCREEN = 21;
	private final static int SET_CURRENT_SCREEN = 23;
	private final static int ENTER_SCREEN = 24;
	private final static int ENTER_PREVIEW = 25;
	private final static int REFRESH_INDEX = 26;

	// for database
	private final static int REFRESH_UNINSTALL = 30;
	private final static int UPDATE_ITEMS_IN_SDCARD = 31;
	private final static int UPDATE_ALL_FOLDER = 32;
	private final static int UPDATE_FOLDER_LIST = 33;

	// for folder
	private final static int REFRESH_FOLDER_CONTENT = 34;
	private final static int DELETE_FOLDER = 35;
	private final static int DELETE_FOLDER_ANIMATION = 36;
	private final static int ADD_ITEM_FROM_FOLDER_ANIMATION = 37;
	private final static int ASK_OPEN_MERGE_FOLDER = 38; // 询问是否需要打开合并文件夹

	// for gowidget
	private final static int GOTO_SPECIFIC_GOWIDGET = 40;

	// for debug lost icon
	public final static int SHOW_LOST_ICON_ERRORCODE = 50;

	// for workspaceEditor
	private final static int ADD_BLANK_SCREEN = 60;
	private final static int WIDGET_DELAY_REF = 61;

	private final static int FINISH_MERGING = 100;

	private ScreenLayout mLayout;
	private LayoutInflater mInflater;
	public Workspace mWorkspace;
	public BackWorkspace mBackWorkspace;
	private SpannableStringBuilder mDefaultKeySsb;

	LauncherWidgetHost mWidgetHost;
	private AppWidgetManager mWidgetManager;
	private static final int APPWIDGET_HOST_ID = 1024;

	private ScreenSettingInfo mScreenSettingInfo = null;
	private DesktopSettingInfo mDesktopSettingInfo = null;
	private EffectSettingInfo mEffectSettingInfo = null;
	private ThemeSettingInfo mThemeSettingInfo = null;

	private boolean mInitWorkspace = false;
	private HashMap<Integer, ArrayList<ItemInfo>> mDesktopItems = null;

	/**
	 * 当前屏幕没有实时更新，慎用 -By Yugi.2012.5.8
	 */
	int mCurrentScreen = -1;
	private int mDragType = DragFrame.TYPE_SCREEN_ITEM_DRAG;
	private ScreenFolderInfo mCurrentFolderInfo = null;

	private View mWidgetView = null;
	private boolean mIsWidgetEditMode = false;

	ScreenControler mControler;
	private ScreenThemeSpreader mThemeSpreader;

	private DesktopBinder mBinder;
	private boolean mIsLoading = false;

	private GoWidgetActionReceiver mGoWidgetActionReceiver;
	private boolean mCheckDelUserFolder;

	private QuickActionMenu mQuickActionMenu;
	// 用于相应弹出菜单，找到对应编辑的view
	private long mDraggedItemId = -1;
	protected boolean mShowIndicator = true;
	private View mTmpDockView = null;
	private AlphaAnimation mDockAnimationAlpha;
	// 同步锁
	private byte[] mLockData = new byte[0];

	// 是否正在展示预览的标识，表示图标从预览进入
	private boolean mIsShowPreview = false;

	private boolean mNeedToCatch = false;

	private UserFolderInfo mDeleteFolderInfo; // 在动画完成后要删除的文件夹

	private View mDragView = null;

	private boolean mEnterPrew = false;

	private boolean mScreenToPreview = false;

	public static ArrayList<Integer> sEnoughSpaceList = null;

	private BubbleTextView mWaitingForResume; // 记录被点击的icon
	private boolean[] mStartActivityResult = new boolean[1]; // 点击icon后的app启动结果

	public int mPreCurrentScreen = 0;
	public int mPreHomeScreen = 0;
	private int mPreNewCurrentScreen = 0;
	private boolean mModifyCurrentScreen = false;

	private LinearLayout mTextLayout;

	private boolean mTextLayoutvisiable = false;

	private DesktopIndicator mIndicator;

	private FolderIcon mMergeFordleIconToOpen; // 刚完成合并，等待自动打开的文件夹
	private Rect mRectTemp = null; // 保存dragOver之后的目标矩形
	private int[] mCellPos; // 用于添加应用程序或是widget到桌面，保存添加元素的区域大小
	private View mViewTemp = null; // 保存dragOver处理时的dragView
	// 文件夹合并进文件夹时的等待进度条
	private GoProgressBar mGoProgressBar;
	private long mNewFolderId = 0; // 新建文件夹的ID

	private FolderIcon mNewFolderIcon = null;
	// 保存将要进入添加模块tab值
	private String mCurTab;
	// 保存将要进入添加模块包名（Gowidget或是多屏多壁纸）
	private String mPkg_Screenedit;
		
	public int mGotoMainScreen = 0 ; //判断Home键是否跳主屏-0代表跳转主屏
	
	private boolean mIsNeedQuickActionMenu = false;
	
	//目标屏幕已满，图标返回原位标志
	private boolean mDontMoveDragObject = false;
	
	// 用来显示状态栏和指示器
	private Runnable mResetStatusAndIndicator = new Runnable() {
		@Override
		public void run() {
			boolean showIndicator = true;
			if (!StatusBarHandler.isHide() && Workspace.getLayoutScale() >= 1.0f) {
				if (mWorkspace.mDragging) {
					mWorkspace.mDragging = false;
					GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
							IDiyMsgIds.SHOW_HIDE_STATUSBAR, -2, false, null);
				} else {
					showIndicator = false;
				}
			}

			if (showIndicator) {
				// 显示指示器(指示器在下端时继续隐藏不显示)
				if (!mLayout.isIndicatorOnBottom()) {
					showIndicator();
				}
			}
			mHandler.sendEmptyMessage(ASK_OPEN_MERGE_FOLDER);
		}
	};

	/**
	 * 屏幕层，在背景层之上，管理桌面上的图标、widget和文件夹
	 * 
	 * @param activity
	 *            拥有该frame的 Activity
	 * @param frameManager
	 * @param id
	 * @param commonModeOper
	 *            后台数据操作类
	 */
	public ScreenFrame(Activity activity, IFrameManager frameManager, int id) {
		super(activity, frameManager, id);
		Log.i(LogConstants.HEART_TAG, "create screen frame");
		mInflater = LayoutInflater.from(activity);
		mNeedToCatch = Machine.isHuaweiAndOS2_2_1();
		// 获取ScreenControler并注册监听
		mWidgetHost = new LauncherWidgetHost(mActivity, APPWIDGET_HOST_ID);
		mWidgetManager = AppWidgetManager.getInstance(mActivity);
		mControler = new ScreenControler(mActivity.getApplicationContext(), mWidgetHost);

		// 初始化屏幕组件
		mLayout = (ScreenLayout) mInflater.inflate(R.layout.diy_screen, null);
		mGoProgressBar = (GoProgressBar) mLayout.findViewById(R.id.go_screen_progress);

		mBackWorkspace = mLayout.getBackWorkspace();
		mWorkspace = mLayout.getWorkspace();
		mWorkspace.setWallpaperDrawer(mBackWorkspace);
		
		mWorkspace.setListener(this);
		mWorkspace.registerProvider();
		mWorkspace.setmNeedToCatch(mNeedToCatch);
		mIndicator = mLayout.getIndicator();
		mTextLayout = (LinearLayout) mLayout.findViewById(R.id.workspace_textlayout);
		// for default key handler
		mDefaultKeySsb = new SpannableStringBuilder();
		Selection.setSelection(mDefaultKeySsb, 0);

		// 主题展示，应用主题
		mThemeSpreader = new ScreenThemeSpreader(mActivity, AppCore.getInstance()
				.getDeskThemeControler(), mWorkspace, mLayout.getIndicator());
		mPpaintFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
				| Paint.FILTER_BITMAP_FLAG);
		registObserver();
	}

	private void registObserver() {
		GOLauncherApp.getSettingControler().registerObserver(this);
	}

	private void unRegistObserver() {
		GOLauncherApp.getSettingControler().unRegisterObserver(this);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public boolean handleMessage(Object who, int type, int msgId, int param, Object object,
			List objects) {
		super.handleMessage(who, type, msgId, param, object, objects);
		boolean ret = false;
		switch (msgId) {
			case IDiyMsgIds.IS_CELLLAYOUT_HAS_ENOUGHT_VACANT : {
				int index = getCurrentScreenIndex();
				if (object != null && object instanceof View) {
					ItemInfo info = (ItemInfo) ((View) object).getTag();
					ret = ScreenUtils.findVacant(new int[2], info.mSpanX, info.mSpanY, index
							+ param, mWorkspace);
				} else {
					ret = false;
				}
			}
				break;
			case IDiyMsgIds.IS_WIDGET_EDIT_SIZE_SMALLER_THAN_MIN_SIZE : {
				ret = false;
				if (mWidgetView != null && mWidgetView.getTag() != null) {
					if (object != null && object instanceof Rect) {
						Rect rect = (Rect) object;
						ScreenAppWidgetInfo widgetInfo = (ScreenAppWidgetInfo) mWidgetView.getTag();
						AppWidgetProviderInfo info = mWidgetManager
								.getAppWidgetInfo(widgetInfo.mAppWidgetId);
						if (info != null
								&& (info.minHeight > rect.height() * CellLayout.sCellHeight || info.minWidth > rect
										.width() * CellLayout.sCellRealWidth)) {
							ret = true;
						} 
					}
				}
			}
				break;
			case IDiyMsgIds.IS_PREV_NEXT_SCREEN_AVALIBLE : {
				ret = true;
				if (mWorkspace != null) {
					int index = mWorkspace.getCurrentScreen();
					if (param == -1) {
						if (index == 0) {
							ret = false;
						}
					} else {
						if (index == mWorkspace.getChildCount() - 1) {
							ret = false;
						}
					}
				}
			}
				break;
			case IDiyMsgIds.BACK_TO_ORIGINAL_POSITION : {
				mDontMoveDragObject = true;
			}
				break;
			case IDiyMsgIds.SEARCH_EVENT_FILTER : {
				ret = searchEventFilter(param, (KeyEvent) object);
				break;
			}

			case IDiyMsgIds.PREVIEW_NOTIFY_DESKTOP : {
				changeDrawState(param == 1);
				break;
			}

			case IDiyMsgIds.SENDVIEWTOPREVIEW : {
				mDragView = (View) object;
				break;
			}

			case IDiyMsgIds.SCREEN_SHOW_PREVIEW : {
				AbstractFrame frame = GoLauncher.getTopFrame();
				if (isLoading()
						&& (frame != null && frame.getId() != IDiyFrameIds.SCREEN_PREVIEW_FRAME)) {
					ScreenUtils.showToast(R.string.loading_screen, mActivity);
					return false;
				}

				if (null != mTmpDockView) {
					mTmpDockView.clearAnimation();
					ItemInfo info = (ItemInfo) mTmpDockView.getTag();
					info.unRegisterObserver((BubbleTextView) mTmpDockView);
					mWorkspace.removeInScreen(mTmpDockView, info.mScreenIndex);
					mTmpDockView = null;
				}
				mDockAnimationAlpha = null;

				if (type == IMsgType.SYNC) {
					ret = showPreview(param == 1);
				} else {
					mHandler.obtainMessage(ENTER_PREVIEW, param, -1).sendToTarget();
				}
				break;
			}
			case IDiyMsgIds.BACK_TO_MAIN_SCREEN : {
				// 通知widget进入显示区域
				fireWidgetVisible(true, mWorkspace.getCurrentScreen());
				mLayout.setVisibility(View.VISIBLE);
				mWorkspace.setDrawState(Workspace.DRAW_STATE_ALL);
				mWorkspace.postInvalidate();
				break;
			}

			case IDiyMsgIds.SCREEN_SHOW_HOME : {
				showMainScreen();
				ret = true;
				break;
			}

			case IDiyMsgIds.SCREEN_ADD : {
				ret = true;
				synchronized (mLockData) {
					if (null != mControler) {
						mControler.addScreen(param);
					}
					addCellLayout();
				}
				break;
			}

			case IDiyMsgIds.SCREEN_ADD_BLANK : {
				ret = true;
				addBlankCellLayout();
				break;
			}

			case IDiyMsgIds.SCREEN_BLANK_TO_NORMAL : {
				ret = true;
				synchronized (mLockData) {
					if (null != mControler) {
						mControler.addScreen(param);
						mWorkspace.sendBroadcastToMultipleWallpaper(false, true);
					}
				}
				break;
			}

			case IDiyMsgIds.SCREEN_REMOVE : {
				final int screenIndex = param;
				synchronized (mLockData) {
					if (null != mControler) {
						// 清除引用，释放资源
						unbindObjectInScreen(screenIndex);
						mControler.delScreen(screenIndex);
					}
					mHandler.obtainMessage(DELETE_SCREEN, screenIndex, -1).sendToTarget();
				}
				ret = true;
			}
				break;

			case IDiyMsgIds.SCREEN_SET_HOME : {
				// 设置为主屏
				final int mainScreen = param;

				// 更新数据库
				if (mControler != null && mScreenSettingInfo != null
						&& mScreenSettingInfo.mMainScreen != mainScreen) {
					GoSettingControler settingControler = GOLauncherApp.getSettingControler();
					mScreenSettingInfo = settingControler.getScreenSettingInfo();
					mScreenSettingInfo.mMainScreen = mainScreen;
					settingControler.updateScreenSettingInfo(mScreenSettingInfo, false);
				}

				mWorkspace.setMainScreen(mainScreen);
				ret = true;
			}
				break;

			case IDiyMsgIds.SCREEN_GET_CELLLAYOUT : {
				final int screenIndex = param;
				if (objects != null && screenIndex >= 0 && screenIndex < mWorkspace.getChildCount()) {
					objects.add(mWorkspace.getChildAt(screenIndex));
					ret = true;
				}
			}
				break;

			case IDiyMsgIds.PREVIEW_REPLACE_CARD : {
				if (object != null && object instanceof Bundle) {
					// moveScreen((Bundle) object);
					ret = true;
				}
			}
				break;
			case IDiyMsgIds.SAVE_SCREEN_DATA : {
				mModifyCurrentScreen = true;
				if (object != null && object instanceof Bundle) {
					moveScreen((Bundle) object);
					ret = true;
				}
				if (object != null && object instanceof Boolean) {
					if ((Boolean) object) {
						final int currentScreen = mPreCurrentScreen;
						// 删除的屏幕在当前屏左边．则新的当前屏减1
						if (param < currentScreen) {
							mPreNewCurrentScreen = currentScreen - 1;
						} else if (param == currentScreen) {
							// 如果相等，则把第一屏设为当前屏
							mPreNewCurrentScreen = 0;
						} else if (param > currentScreen) {
							// 如果相等，则把第一屏设为当前屏
							mPreNewCurrentScreen = currentScreen;
						}
						SensePreviewFrame.sCurScreenId = mPreNewCurrentScreen;
					}
				}
			}
				break;
			case IDiyMsgIds.SCREEN_SET_CURRENT : {
				// 设置为当前屏
				mHandler.obtainMessage(SET_CURRENT_SCREEN, param, -1).sendToTarget();
				ret = true;
			}
				break;

			case IDiyMsgIds.SCREEN_ENTER : {
				final int screenIndex = param;
				// 从屏幕预览回来，切屏时间使用参数指定值
				int duration = 300;
				boolean isDrag = false;
				if (object != null && object instanceof Integer) {
					duration = ((Integer) object).intValue();
					if (duration <= -100) {
						duration = 300;
						isDrag = true;
					}
				}
				mWorkspace.mDragging = isDrag;
				turnToScreen(screenIndex, true, duration);
				ret = true;
			}
				break;

			case IDiyMsgIds.SCREEN_SHOW_MAIN_SCREEN_OR_PREVIEW : {
				hideQuickActionMenu(true);
				final int currentScreen = mWorkspace.getCurrentScreen();
				final int mainScreen = mWorkspace.getMainScreen();
				if (isTop()) {
					if (currentScreen != mainScreen) {
						turnToScreen(mWorkspace.getMainScreen(), true, -1);
						ret = true;
					} else {
						ret = showPreview(false);
					}
				}
			}
				break;

			case IDiyMsgIds.SCREEN_SLIDE_DOWN :
			case IDiyMsgIds.SCREEN_SLIDE_UP :
			case IDiyMsgIds.SCREEN_PINCH_IN :
			case IDiyMsgIds.SCREEN_LONG_CLICK : {
				if (msgId == IDiyMsgIds.SCREEN_LONG_CLICK && isLoading()) {
					ScreenUtils.showToast(R.string.loading_screen, mActivity);
					return false;
				}
				ret = GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, msgId, param,
						object, objects);
				break;
			}
			case IDiyMsgIds.SCREEN_DOUBLE_CLICK :
				ret = GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, msgId, param,
						object, objects);
				break;
			case IDiyMsgIds.SCREEN_DOUBLE_CLICK_VALID :
				ret = GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, msgId, param,
						object, objects);
				break;
			case IDiyMsgIds.UPDATE_WALLPAPER_OFFSET : {
				GoLauncher.postMessage(this, IDiyFrameIds.SCHEDULE_FRAME, msgId, param, object,
						objects);
				ret = true;
				break;
			}

			case IDiyMsgIds.SCREEN_UPDATE_INDICATOR : {
				if (object != null && object instanceof Bundle) {
					updateIndicator(param, (Bundle) object);
					ret = true;
				}
				break;
			}

			case IFrameworkMsgId.SYSTEM_HOME_CLICK : {
				hideQuickActionMenu(true);
				//防止屏幕按Home键回主屏无过渡动画
			if (!Workspace.sFlag && Workspace.sLayoutScale >= 1.0f) {
			} else if (mWorkspace != null) {
				mWorkspace.smallToNormal(false);
			}
				break;
			}
			case IFrameworkMsgId.SYSTEM_ON_NEW_INTENT :
			case IDiyMsgIds.SCREEN_CANCEL_LONG_CLICK : {
				hideQuickActionMenu(true);
				break;
			}

			case IDiyMsgIds.DRAG_MOVE : {
				/**
				 * NOTICE: 开始拖动，关闭显示的菜单，不需要回调， 否则会导致拖动时已经隐藏的图标重新显示的问题
				 */
//				hideQuickActionMenu(false);
				mIsNeedQuickActionMenu = false;
				GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME,
						IDiyMsgIds.REMOVE_ACTION_MENU, -1, null, null);
				GoLauncher.sendMessage(this, IDiyFrameIds.DESK_USER_FOLDER_FRAME,
						IDiyMsgIds.REMOVE_ACTION_MENU, -1, null, null);
				ret = true;
				break;
			}

			case IDiyMsgIds.DRAG_MOVING : {
				if (object != null && objects != null && objects.size() > 0
						&& objects.get(0) instanceof Point) {
					// 当前dragView的位置
					Point point = (Point) objects.get(0);
					boolean isAddState = false;
					if (mDragType == DragFrame.TYPE_ADD_APP_DRAG) {
						isAddState = true;
					}
					mWorkspace.visualizeDropLocation((View) object, point, isAddState);
				}
			}
				break;

			case IDiyMsgIds.DRAG_START : {
				ret = startDrag((View) object, objects, param);
				mIsNeedQuickActionMenu = true;
				break;
			}

			case IDiyMsgIds.DRAG_OVER : {
				if (!isLoading()) {
					ret = dragOver(param, object, objects);
					mWorkspace.refreshSubView();
					if (Workspace.getLayoutScale() < 1) {
						// 用户行为统计---屏幕元素调整位置。
						StatisticsData.countUserActionData(
								StatisticsData.DESK_ACTION_ID_SCREEN_EDIT,
								StatisticsData.USER_ACTION_FOUR, IPreferencesIds.DESK_ACTION_DATA);
					}
				} else {
					ScreenUtils.showToast(R.string.loading_screen, mActivity);
				}
				break;
			}

			case IDiyMsgIds.DRAG_CANCEL : {
				int status = CellLayout.DRAW_STATUS_NORMAL;
				if (null != getCurrentScreen()) {
					status = getCurrentScreen().getDrawStatus();
					clearDragState();
					if (status == CellLayout.DRAW_STATUS_INTO_FOLDER_ZOOMOUT) {
						// 如果之前是缩小动画状态，这里不改变状态，不然会打断动画
						getCurrentScreen().setmDrawStatus(status);
					}
				} else {
					clearDragState();
				}
				ret = true;
			}
				break;

			case IDiyMsgIds.SCREEN_FOLDER_ICON_STATE : {
				// boolean open = param == FolderIcon.FOLDER_ICON_OPEN;
				// ScreenUtils.changeFoderIconState(open, (Rect) object,
				// mWorkspace);
				//
				// final int current = mWorkspace.getCurrentScreen();
				// CellLayout layout = (CellLayout) mWorkspace.getChildAt(current);
				// if (layout == null) {
				// break;
				// }
				// if (open) {
				// layout.setOutlineVisible(false);
				// } else {
				// layout.setOutlineVisible(true);
				// }
				break;
			}

			case IDiyMsgIds.SCREEN_CLICK_SHORTCUT : {
				if (object instanceof View) {
					ret = launchApp((View) object);
					if (mStartActivityResult[0] && object instanceof BubbleTextView) {
						mWaitingForResume = (BubbleTextView) object;
						mWaitingForResume.setStayPressed(true);
						mStartActivityResult[0] = false;
					}
				}
				break;
			}

			case IFrameworkMsgId.SYSTEM_CONFIGURATION_CHANGED : {
				hideQuickActionMenu(true);
				// 解决桌面数据加载导致功能表拿到桌面缩略图与实际有差别问题
				if (mFrameManager.getTopFrame().getId() != IDiyFrameIds.APPFUNC_FRAME) {
					reloadDesktop();
					if (!mEnterPrew) {
						mWorkspace.requestLayout(Workspace.CHANGE_SOURCE_INDICATOR, 0);
					}
				}
				break;
			}

			case IDiyMsgIds.QUICKACTION_EVENT : {
				hideQuickActionMenu(false);
				if (param == IQuickActionId.CHANGE_ICON) {
					actionChangeIcon((Bundle) object);
				} else {
					try {
						mDragType = (Integer) objects.get(0);
					} catch (Exception e) {
					}
					onActionClick(param, object);
				}
				break;
			}

			case IDiyMsgIds.SCREEN_ADD_SHORTCUT : {
				ret = addShortcut(object);
			}
				break;

			case IDiyMsgIds.SCREEN_ADD_APPLICATIONS : {
				ArrayList<ShortCutInfo> infos = (ArrayList<ShortCutInfo>) objects;
				if (infos != null) {
					int size = infos.size();
					for (int i = 0; i < size; i++) {
						final ShortCutInfo shortCutInfo = infos.get(i);
						addShortcut(shortCutInfo);
					}
				}
				infos = null;
			}
				break;

			case IDiyMsgIds.SCREEN_ADD_SHORTCUT_COMPLETE : {
				// 处理其他程序发生的添加快捷方式请求
				ret = installShortcut((ShortCutInfo) object);
				break;
			}

			case IDiyMsgIds.ADD_GO_WIDGET : {
				// 成功添加widget至桌面

				int screenindex;

				screenindex = mWorkspace.getCurrentScreen();
				ret = addGoWidget((Bundle) object, screenindex, param);
				// add by jiang添加完允许滑动
				mWorkspace.allowTouch();

				if (ret && Workspace.getLayoutScale() < 1.0f)
				// 增加是否从桌面编辑模块进行添加的判断
				{
					GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_EDIT_BOX_FRAME,
							IDiyMsgIds.SCREENEDIT_PICK_WIDGET_FININSH, 0, null, null);
				}

				// 新处理，

				break;
			}

			// 判断widget的大小是否能添加到当前屏幕
			case IDiyMsgIds.ADD_GO_WIDGET_BYCONFIG : {
				// 成功添加widget至桌面
				int screenindex;
				screenindex = mWorkspace.getCurrentScreen();
				ret = addGoWidgetByCinfig((Bundle) object, screenindex);

				break;
			}

			case IDiyMsgIds.APPLY_GO_WIDGET_THEME : {
				ret = applyGoWidgetTheme(param, (Bundle) object);
				break;
			}

			case IDiyMsgIds.SCREEN_GET_ALLOCATE_APPWIDGET_ID : {
				// 获取由host生成的新的appwidget的ID
				if (object instanceof Bundle) {
					Bundle bundle = (Bundle) object;
					bundle.putInt("id", mWidgetHost.allocateAppWidgetId());
					ret = true;
				}
			}
				break;

			case IDiyMsgIds.SCREEN_GET_SHORTCUT_ISEXIST : {
				// 判断Intent对应的应用是否已经存在于当前桌面上
				ret = ScreenUtils.isExistShortcut((Intent) object, mWorkspace);
				break;
			}

			case IDiyMsgIds.SCREEN_DEL_APPWIDGET_ID : {
				// host删除一个appwidgetID
				// mWidgetHost.deleteAppWidgetId(param);
				if (GoWidgetManager.isGoWidget(param)) {
					AppCore.getInstance().getGoWidgetManager().deleteWidget(param);
				} else
				// 系统widget
				{
					mWidgetHost.deleteAppWidgetId(param);
				}
				ret = true;
			}
				break;

			case IDiyMsgIds.SCREEN_WIDGET_REQUEST_FOCUS : {
				int screenIndex = ScreenUtils.getScreenIndexofWidget(param, mDesktopItems);
				if (screenIndex >= 0) {
					turnToScreen(screenIndex, true, -1);
					ret = true;
				}
				break;
			}

			/*----------------BEGIN widget进入或离开屏幕通知 ----------------*/
			case IDiyMsgIds.SCREEN_FIRE_WIDGET_ONENTER : {
				if (param == -1) {
					param = mWorkspace.getCurrentScreen();
				}
				fireWidgetVisible(true, param);
			}
				break;

			case IDiyMsgIds.SCREEN_FIRE_WIDGET_ONLEAVE : {
				if (param == -1) {
					param = mWorkspace.getCurrentScreen();
				}
				fireWidgetVisible(false, param);
			}
				break;

			case IDiyMsgIds.SCREEN_CANCEL_WIDGET_ONENTER : {
				cancelFireWidgetVisible(true);
			}
				break;

			case IDiyMsgIds.SCREEN_CANCEL_WIDGET_ONLEAVE : {
				cancelFireWidgetVisible(false);
			}
				break;
			/*----------------END widget进入或离开屏幕通知 ----------------*/

			case IDiyMsgIds.SCREEN_ADD_APPWIDGET : {
				ret = addAppWidget(param);
				break;
			}

			case IDiyMsgIds.SCREEN_ADD_LIVE_FOLDER : {
				ret = addLiveFolder(object);
				break;
			}

			case IDiyMsgIds.SCREEN_ADD_USER_FOLDER : {
				ret = addDeskUserFolder(object, param);
				break;
			}

			// 添加前先判断屏幕空间
			case IDiyMsgIds.SCREEN_EDIT_PRE_ADD : {
				ret = checkVacant(objects);

				break;
			}

			case IDiyMsgIds.CREATE_DESK_USERFOLDER : {
				if (object != null && object instanceof UserFolderInfo) {
					UserFolderInfo userFolderInfo = (UserFolderInfo) object;
					FolderIcon newFolder = FolderIcon.fromXml(getFolderIconId(), mActivity,
							mWorkspace.getCurrentScreenView(), userFolderInfo,
							getDisplayTitle(userFolderInfo.mTitle));
					newFolder.close();
					newFolder.setShowShadow(getShadowState());
					// customDeskTopBackground(newFolder);
					mWorkspace.addInCurrentScreen(newFolder, userFolderInfo.mCellX,
							userFolderInfo.mCellY, 1, 1);
					mNewFolderIcon = newFolder;

				}
				break;
			}

			case IDiyMsgIds.SCREEN_ADD_APPDRAWER_FOLDER : {
				ret = addAppDrawerFolder(objects);
				break;
			}

			case IDiyMsgIds.SCREEN_GET_VANCANT_COUNT : {
				int screenIndex = param;
				int count[] = (int[]) object;
				if (null != count && count.length > 0) {
					count[0] = ScreenUtils.findSingleVancant(
							screenIndex == -1 ? mWorkspace.getCurrentScreen() : screenIndex, null,
							mWorkspace);
					ret = true;
				}
				break;
			}

			case IDiyMsgIds.SCREEN_SHOW_PRESCREEN : {
				// 切换到上一屏
				hideQuickActionMenu(true);
				mIsNeedQuickActionMenu = false;
				if (null != mTmpDockView) {
					mTmpDockView.clearAnimation();
					ItemInfo info = (ItemInfo) mTmpDockView.getTag();
					info.unRegisterObserver((BubbleTextView) mTmpDockView);
					mWorkspace.removeInScreen(mTmpDockView, info.mScreenIndex);
					mTmpDockView = null;
					mDockAnimationAlpha = null;
				}

				// CellLayout currentScreenView = (CellLayout)
				// mWorkspace.getCurrentScreenView();
				// if (currentScreenView != null){
				// currentScreenView.resetReplace();
				// }
				final int curScreen = mWorkspace.getCurrentScreen();
				turnToScreen(curScreen - 1, false, -1);
				// 返回的结果是屏幕实际是否有发生偏移
				if (ret = mWorkspace.getCurrentScreen() != curScreen) {
					mWorkspace.setCellLayoutGridState(curScreen, false, -1);
					mWorkspace.getCurrentScreenView().markOccupied();
				}
				break;
			}

			case IDiyMsgIds.SCREEN_SHOW_NEXTSCREEN : {
				// 切换下一屏
				hideQuickActionMenu(true);
				mIsNeedQuickActionMenu = false;
				if (null != mTmpDockView) {
					mTmpDockView.clearAnimation();
					ItemInfo info = (ItemInfo) mTmpDockView.getTag();
					info.unRegisterObserver((BubbleTextView) mTmpDockView);
					mWorkspace.removeInScreen(mTmpDockView, info.mScreenIndex);
					mTmpDockView = null;
					mDockAnimationAlpha = null;
				}
				final int curScreen = mWorkspace.getCurrentScreen();
				turnToScreen(curScreen + 1, false, -1);
				// 返回的结果是屏幕实际是否有发生偏移
				if (ret = mWorkspace.getCurrentScreen() != curScreen) {
					mWorkspace.setCellLayoutGridState(curScreen, false, -1);
					mWorkspace.getCurrentScreenView().markOccupied();
				}
				break;
			}

			case IDiyMsgIds.SCREEN_ADD_SEARCH_WIDGET : {
				// 添加搜索组件
				addSearchWidget();
				break;
			}

			case IDiyMsgIds.SCREEN_FOLDER_EVENT : {
				handleFolderEvent(param, object, objects);
				ret = true;
				break;
			}

			case IDiyMsgIds.WIDGET_EDIT_FRAME_VALIDATE_RECT : {
				if (object != null && object instanceof Rect) {
					ret = validateRect((Rect) object);
				}
				break;
			}

			case IDiyMsgIds.WIDGET_EDIT_FRAME_STOP_EIDT : {
				ret = true;
				stopWidgetEdit();
				break;
			}

			case IDiyMsgIds.SEND_WALLPAPER_COMMAND : {
				ret = true;
				GoLauncher.sendMessage(type, IDiyFrameIds.SCHEDULE_FRAME, msgId, param, object,
						objects);
				break;
			}

			case IDiyMsgIds.SCREEN_CLOSE_ALL_FOLDERS : {
				closeAllFolders();
				ret = true;
				break;
			}

			case IDiyMsgIds.RESET_DEFAULT_ICON : {
				resetDefaultIcon();
				ret = true;
				break;
			}

			case IDiyMsgIds.SET_WALLPAPER_DRAWABLE : {
				final Drawable bg = (Drawable) object;
				final int offset = param;
				mWorkspace.setWallpaper(bg, offset);
				mWorkspace.invalidate();
				break;
			}

			case IFrameworkMsgId.SYSTEM_FULL_SCREEN_CHANGE : {
				// 一般3.X的pad状态栏在下面，上面是没有的，所以不需要重新排版
				if (!(Machine.IS_HONEYCOMB && !Machine.IS_ICS)) {
					final boolean isFullScreen = param == 1;
					final int yOffset = isFullScreen ? 0 : StatusBarHandler.getStatusbarHeight();
					mWorkspace.setWallpaperYOffset(yOffset);
					if (!mEnterPrew) {
						mWorkspace.requestLayout(Workspace.CHANGE_SOURCE_STATUSBAR, param);
					}
					mWorkspace.invalidate();
				}
				break;
			}

			case IDiyMsgIds.DRAW_BACKGROUND : {
				mWorkspace.drawBackground((Canvas) object, param);
				break;
			}

			case IDiyMsgIds.GET_BACKGROUND : {
				ret = true;
				Drawable bg = null;
				int[] bgOffset = new int[2];
				if (mWorkspace != null) {
					bg = mWorkspace.getBackground();
					bgOffset[0] = mWorkspace.getScreenScroller().getBackgroundOffsetX();
					bgOffset[1] = mWorkspace.getScreenScroller().getBackgroundOffsetY();
				}
				ArrayList<int[]> list = new ArrayList<int[]>();
				list.add(bgOffset);
				GoLauncher.sendMessage(this, param, IDiyMsgIds.SEND_BACKGROUND, -1, bg, list);
				list.clear();
				list = null;
				break;
			}

			case IDiyMsgIds.SCREEN_FOLDER_ADDITEMS : {
				if (object instanceof Long) {
					if (param == 0) {
						addFolderItems(((Long) object).longValue(),
								(ArrayList<AppItemInfo>) objects);
					} else if (param == 1) {
						refreshFolderItems(((Long) object).longValue(), true);
					}
					ret = true;
				}
				break;
			}

			case IDiyMsgIds.SCREEN_FOLDER_REMOVEITEMS : {
				if (object instanceof Long) {
					if (param == ScreenModifyFolderActivity.REMOVE_ITEMS) {
						refreshFolderItems(((Long) object).longValue(), true);
						if (Workspace.getLayoutScale() < 1.0f) {
							// 为了刷新文件夹内容
							mWorkspace.postInvalidateDelayed(500);
						}
					} else {
						mCheckDelUserFolder = param < 0;
						removeFolderItems(((Long) object).longValue(),
								(ArrayList<AppItemInfo>) objects);
					}
					ret = true;
				}
				break;
			}

			case IDiyMsgIds.SCREEN_FOLDER_RENAME : {
				if (object instanceof Long) {
					// 0 现在名
					// 1 原有名
					ArrayList<String> names = (ArrayList<String>) objects;
					if (null != names && names.size() > 0) {
						renameItem(((Long) object).longValue(), names.get(0));
					}
					ret = true;
				}
				break;
			}
			case IDiyMsgIds.PREVIEW_TO_MAIN_SCREEN :
				mIsShowPreview = false;
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
						IDiyMsgIds.PREVIEW_TO_MAIN_SCREEN_ANIMATE,
						// mScreenSettingInfo.mMainScreen,
						SensePreviewFrame.sCurScreenId, null, null);
				break;
			case IDiyMsgIds.PREVIEW_SHOWING :
				ret = mIsShowPreview;
				break;
			case IDiyMsgIds.GET_ORIGIN_ICON : {
				if (object != null && object instanceof RelativeItemInfo
						&& null != ((RelativeItemInfo) object).getRelativeItemInfo()) {
					BitmapDrawable drawable = ((RelativeItemInfo) object).getRelativeItemInfo()
							.getIcon();
					objects.add(drawable);
					ret = true;
				}
				break;
			}

			case IDiyMsgIds.IS_EXIST_TRASH_DATA : {
				ret = mControler.isScreenDirtyData();
				break;
			}

			case IDiyMsgIds.CLEAN_TRASH_DATA : {
				mControler.clearScreenDirtyData();
				ret = true;
				break;
			}

			case IDiyMsgIds.NOTIFICATION_CHANGED : {
				if (object instanceof Integer) {
					int count = ((Integer) object).intValue();
					updateNotification(param, count);
				}
				break;
			}

			case IDiyMsgIds.DESK_THEME_CHANGED : {
				mThemeSpreader.applyTheme();
				dismissProgressDialog();
				break;
			}

			case IDiyMsgIds.APPCORE_DATACHANGE : {
				handleAppCoreChange(param, object);
				break;
			}

			case IDiyMsgIds.SCREEN_IS_SHOW_QUICKACTION : {
				ret = mQuickActionMenu != null && mQuickActionMenu.isShowing();
				break;
			}

			case IDiyMsgIds.INDICATOR_CHANGE_SHOWMODE : {
				// 指示器改变了模式
				mLayout.getIndicator().doWithShowModeChanged();
				ret = true;
				break;
			}

			case IDiyMsgIds.INDICATOR_SLIDE_PERCENT : {
				mWorkspace.mScroller.setScrollPercent(param);
				break;
			}

			case IDiyMsgIds.HIDE_INDICATOR : {
				// mLayout.hideIndicator();
				mLayout.getIndicator().hide();
				break;
			}

			case IDiyMsgIds.SHOW_INDICATOR : {
				showIndicator();
				break;
			}

			/********************** BEGINE AppDateEngine 发送的消息 *******************************/
			case IDiyMsgIds.EVENT_LOAD_FINISH : {
				loadScreen();
				break;
			}

			case IDiyMsgIds.EVENT_INSTALL_APP : {
				String pkgString = (String) object;
				replaceFavoriteGowidget(pkgString);
				pkgString = null;
			}
				break;

			case IDiyMsgIds.EVENT_INSTALL_PACKAGE : {
				String pkgString = (String) object;
				replaceFavoriteGowidget(pkgString);
				pkgString = null;
				break;
			}

			case IDiyMsgIds.EVENT_UNINSTALL_APP : {
				handleUninstallApps((ArrayList<AppItemInfo>) objects);
				break;
			}

			case IDiyMsgIds.EVENT_LOAD_ICONS_FINISH : {
				mHandler.sendEmptyMessage(UPDATE_ALL_FOLDER);
				break;
			}

			case IDiyMsgIds.EVENT_REFLUSH_TIME_IS_UP :
			case IDiyMsgIds.EVENT_SD_MOUNT :
			case IDiyMsgIds.EVENT_REFLUSH_SDCARD_IS_OK : {
				ArrayList<ItemInfo> changes = mControler.handleSDIsReady();
				refreshItemList(changes);
				// 更新文件夹图标
				mHandler.sendEmptyMessage(UPDATE_ALL_FOLDER);

				// 停止正在编辑的widget
				stopWidgetEdit();
				break;
			}

			/********************** END AppDateEngine 发送的消息 *******************************/
			case IDiyMsgIds.SHOW_LOST_ICON_ERRORCODE : {
				// for debug lost icon
				Message msg = new Message();
				msg.what = SHOW_LOST_ICON_ERRORCODE;
				msg.arg1 = param;
				mHandler.sendMessage(msg);
				break;
			}

			case IDiyMsgIds.REMOVE_DESK_ITEMINFO : {
				if (object != null && object instanceof ItemInfo) {
					ScreenUtils.removeViewByItemInfo((ItemInfo) object, mWorkspace);
				}
			}
				break;

			case IDiyMsgIds.SET_WORKSPACE_DRAWING_CACHE : {
				mWorkspace.setDrawingCacheEnabled(param == 1);
			}
				break;

			case IDiyMsgIds.SCREEN_DOCKEXCHANGE_PREVIEW : {
				ShortCutInfo info = (ShortCutInfo) object;
				exchangeDockToScreenPreview(info);
			}
				break;

			case IDiyMsgIds.SCREEN_REMOVE_FOLDER_ITEM :
				if (mCurrentFolderInfo != null) {
					ItemInfo item = (ItemInfo) ((View) object).getTag();
					mControler.removeItemFromFolder(item, mCurrentFolderInfo.mInScreenId, false);

					// 更新图标
					item.selfConstruct();
					((UserFolderInfo) mCurrentFolderInfo).remove(item.mInScreenId);
					if (!deleteFolderOrNot((UserFolderInfo) mCurrentFolderInfo, true)) {
						// if (item instanceof ShortCutInfo) {
						// int appType =
						// AppIdentifier.whichTypeOfNotification(mActivity,
						// (( ShortCutInfo ) item).mIntent);
						// if (appType != NotificationType.IS_NOT_NOTIFICSTION) {
						// (( UserFolderInfo ) mCurrentFolderInfo).mTotleUnreadCount
						// -= (( ShortCutInfo ) item).mCounter;
						// }
						// }
						updateFolderIconAsync((UserFolderInfo) mCurrentFolderInfo, false, false);
					}
				}
				mCurrentFolderInfo = null;
				object = null;
				break;

			case IDiyMsgIds.SCREEN_DELETE_FOLDER_ITEM : {
				if (object != null && object instanceof Long && objects != null
						&& !objects.isEmpty()) {
					long folderId = (Long) object;
					ItemInfo itemInfo = (ItemInfo) objects.get(0);
					mControler.removeItemFromFolder(itemInfo, folderId, false);

					View view = ScreenUtils.getViewByItemId(folderId, -1, mWorkspace);
					if (view != null && view.getTag() != null
							&& view.getTag() instanceof UserFolderInfo) {
						UserFolderInfo userFolderInfo = (UserFolderInfo) view.getTag();
						itemInfo.selfConstruct();
						userFolderInfo.remove(itemInfo.mInScreenId);
						if (!deleteFolderOrNot(userFolderInfo, true)) {
							updateFolderIconAsync(userFolderInfo, false, false);
						}
					}
				}
			}
				break;

			case IDiyMsgIds.SCREEN_REMOVE_FOLDER_VIEW : {
				ItemInfo itemInfo = (ItemInfo) ((View) object).getTag();
				mControler.removeDesktopItem(itemInfo);
				View targetView = ScreenUtils.getViewByItemId(itemInfo.mInScreenId,
						itemInfo.mScreenIndex, mWorkspace);

				if (targetView != null) {
					ViewParent parent = targetView.getParent();
					if (parent != null && parent instanceof ViewGroup) {
						((ViewGroup) parent).removeView(targetView);
					}
				}
				ScreenUtils.unbindeUserFolder((UserFolderInfo) itemInfo);
				mControler.removeDesktopItem(itemInfo);
			}
				break;

			case IDiyMsgIds.SCREEN_ISLOAD_FINISH : {
				ret = isLoading();
			}
				break;

			case IDiyMsgIds.SCREEN_CHANGE_VIEWS_POSITIONS : {
				if (param >= 0 || null != objects || objects instanceof ArrayList<?>) {
					ArrayList<ItemInfo> itemInfos = (ArrayList<ItemInfo>) objects;
					for (ItemInfo itemInfo : itemInfos) {
						updateDesktopItem(param, itemInfo);
					}
				}
			}
				break;

			case IDiyMsgIds.SCREEN_REFRESH_INDEX : {
				synchronized (mLockData) {
					mHandler.obtainMessage(REFRESH_INDEX, -1, -1).sendToTarget();
				}
				ret = true;
			}
				break;

			// case IDiyMsgIds.SCREEN_NEED_CHECK_SHOW_SCREEN_EFFECT_GUIDE:
			// {
			// mWorkspace.setmNeedToCheckShowGuide(true);
			// }
			// break;

			case IDiyMsgIds.SCREEN_FRAME_IS_TOP : {
				ret = isTop();
			}
				break;

			case IDiyMsgIds.CHECK_FOLDER_NEED_DELETE : {
				if (null != object && object instanceof Long) {
					UserFolderInfo userFolderInfo = getFolderItemInfo((Long) object);
					ret = deleteFolderOrNot(userFolderInfo, false);
				}
			}
				break;

			case IDiyMsgIds.SCREEN_GOTO_GOWIDGET_PAGE : {
				/*if (null != object && object instanceof String) {
					String pkg = (String) object;
					Message msg = new Message();
					msg.what = GOTO_SPECIFIC_GOWIDGET;
					msg.obj = pkg;
					mHandler.sendMessage(msg);
				}*/
				//add by jiangchao 1, 桌面外GO小部件下载跳添加界面 2,桌面外多屏多壁纸跳添加界面
				//注:只在桌面启动完成时才允许跳转,第一次进入不跳转(以防止桌面第一次进入跳转后,重启后还会跳转)
			if (mWorkspace != null
					&& GoLauncher.getContext().getSystemHomeKeyAct()) {
				String pkg = (String) object;
				if (pkg != null
						&& pkg.equals(LauncherEnv.Plugin.MULTIPLEWALLPAPER_PKG_NAME)) {
					mCurTab = BaseTab.TAB_WALLPAPER; // 多屏多壁纸跳转
				} else {
					mCurTab = BaseTab.TAB_GOWIDGET; // Gowidget跳转
				}
				mPkg_Screenedit = pkg;
				//判断跳转前是否在添加界面
				if (GoLauncher.getTopFrame().getId() != IDiyFrameIds.SCREEN_EDIT_BOX_FRAME) {
					mWorkspace.normalToSmallPreAction(mCurTab, mPkg_Screenedit);
				}
			}
			}
				break;
			case IDiyMsgIds.SCREEN_FORCE_REDRAW_CELLLAYOUT : {
				CellLayout screen = getCurrentScreen();
				if (screen != null) {
					screen.invalidate();
				}
			}
				break;

			case IDiyMsgIds.FOLDER_REPLACE_INDEX : {
				try {
					UserFolderInfo userFolderInfo = (UserFolderInfo) object;
					ArrayList<ItemInfo> infos = (ArrayList<ItemInfo>) objects;

					mControler.updateFolderIndex(userFolderInfo.mInScreenId, infos);
					// 文件夹排序方式改变，刷新数据
					if (param == 1) {
						mControler.updateDBItem(userFolderInfo);
					}
					updateFolderIconAsync(userFolderInfo, true, true);
				} catch (Exception e) {
				}
			}
				break;
			case IDiyMsgIds.IS_SET_CONTENT : {
				if (mWorkspace.getCurrentScreen() == param && mScreenToPreview) {
					ret = true;
					break;
				}
				View v = (View) object;
				ItemInfo info = (ItemInfo) v.getTag();
				int xy[] = new int[2];
				ret = ScreenUtils.findVacant(xy, info.mSpanX, info.mSpanY, param, mWorkspace);
			}
				break;

			case IDiyMsgIds.NEW_ADD_MOCCUPIED : {
				// 当前屏幕是横屏还是竖屏
				CellLayout cellLayout = mWorkspace.getScreenView(param);
				boolean[][] occupied = null;
				// 横屏
				if (CellLayout.sPortrait) {
					cellLayout.mShortAxisCells = CellLayout.getRows();
					cellLayout.mLongAxisCells = CellLayout.getColumns();
					occupied = new boolean[cellLayout.mShortAxisCells][cellLayout.mLongAxisCells];
				}
				// 竖屏
				else {
					cellLayout.mShortAxisCells = CellLayout.getColumns();
					cellLayout.mLongAxisCells = CellLayout.getRows();
					occupied = new boolean[cellLayout.mLongAxisCells][cellLayout.mShortAxisCells];
				}
				cellLayout.mOccupied = occupied;
				cellLayout.mTmpOccupied = occupied.clone();
			}
				break;

			case IDiyMsgIds.SET_CURRENTSCREEN : {
				mWorkspace.setCurrentScreen(param);
			}
				break;

			case IDiyMsgIds.REDRAW_SCREEN : {
				mWorkspace.mworkRequest();
			}
				break;

			case IDiyMsgIds.DISPLAY_INDICATOR : {
				if (mEnterPrew) {
					mEnterPrew = false;
					// mLayout.getIndicator().setVisible(true);
					// 加入指示器的展现条件是为了减少被多次调用
					if (!mScreenSettingInfo.mAutoHideIndicator) {
						mLayout.getIndicator().show();
					}
				}
				// if(mScreenToPreview){
				// mScreenToPreview = false;
				// }
				if (mModifyCurrentScreen && (Boolean) object) {
					mModifyCurrentScreen = false;
					mWorkspace.setCurrentScreen(mPreNewCurrentScreen);
				}
			}
				break;
			case IDiyMsgIds.DISPLAY_INDICATOR_IMMEDIATELY : {
				mLayout.getIndicator().show();
				break;
			}
			case IDiyMsgIds.SCREENTOPREW : {
				mScreenToPreview = true;
			}
				break;

			case IDiyMsgIds.HIDDEN_INDICATOR : {
				// mLayout.getIndicator().setVisible(false);
				mLayout.getIndicator().hide();
			}
				break;
			case IDiyMsgIds.PREVIEWWTOSCREEN :
				mScreenToPreview = false;
				mWorkspace.mDragging = true;
				mIsShowPreview = false;
				break;
			case IDiyMsgIds.GET_ENOUGHSPACELIST : {
				sEnoughSpaceList = getEnoughSpaceList((Bundle) object);
				// 再发送给屏幕层进行参数设置
				// GoLauncher.sendMessage(this,
				// IDiyFrameIds.SCREEN_PREVIEW_FRAME,
				// IDiyMsgIds.GET_ENOUGHSPACELIST, 0, null, enoughSpaceList);
				break;
			}
			// 设置特效预览
			case IDiyMsgIds.SCREENEDIT_SHOW_TAB_EFFECT_SETTING : {
				if (mWorkspace != null) {
					mWorkspace.effectorAutoShow(param);
				}
			}
				break;

			case IDiyMsgIds.GO_LOCKER_PRECHANGE : {
				if (!mTextLayoutvisiable) {
					mTextLayoutvisiable = true;
					LockerThemeTab.sCHANG_LOCKER_THEME = true;
					Animation animation = MyAnimationUtils.getPopupAnimation(
							MyAnimationUtils.POP_FROM_LONG_START_SHOW_2, -1);
					if (animation != null && mTextLayout != null) {
						mTextLayout.startAnimation(animation);
						mTextLayout.setVisibility(View.VISIBLE);
					}
					mIndicator.setVisibility(View.INVISIBLE);
				}
				break;
			}

			case IDiyMsgIds.SCREEN_CLEAR_OUTLINE_BITMAP : {
				final int screenIndex = mWorkspace.getCurrentScreen();
				CellLayout currentScreen = (CellLayout) mWorkspace.getChildAt(screenIndex);
				if (currentScreen != null) {
					currentScreen.clearVisualizeDropLocation();
					currentScreen.revertTempState();
					mWorkspace.setCellLayoutGridState(screenIndex, false, -1);
					mWorkspace.clearDragState();
				}
				if (param == 1) {
					// 延迟100毫秒
					mWorkspace.postDelayed(mResetStatusAndIndicator, 100);
				}
				mWorkspace.mFirstGetFolderRects = true;
			}
				break;

			case IDiyMsgIds.SCREEN_FLING_TO_CLEAR_OUTLINE_BITMAP : {
				final int screenIndex = mWorkspace.getCurrentScreen();
				CellLayout currentScreen = (CellLayout) mWorkspace.getChildAt(screenIndex);
				if (currentScreen != null) {
					currentScreen.clearVisualizeDropLocation();
					currentScreen.revertTempState();
				}
			}
				break;

			case IDiyMsgIds.SCREEN_INDICRATOR_POSITION : {
				mLayout.getIndicator().getHeight();
				setIndicatorPost((String) object);
			}
				break;

			case IDiyMsgIds.GET_HOME_CURRENT : {
				if (mModifyCurrentScreen) {
					mModifyCurrentScreen = false;
					mPreCurrentScreen = mPreNewCurrentScreen;
				} else {
					mPreCurrentScreen = mWorkspace.getCurrentScreen();
				}
				mPreHomeScreen = mWorkspace.getMainScreen();
			}
				break;

			case IDiyMsgIds.FOLDER_RENAME : {
				if (null != object && null != objects && !objects.isEmpty()
						&& null != objects.get(0) && object instanceof Long
						&& objects.get(0) instanceof String) {
					long inscreenid = (Long) object;
					String name = (String) objects.get(0);
					actionRename(name, inscreenid);
				}
			}
				break;

			case IDiyMsgIds.SCREEN_OPEN_FOLDER_DATA : {
				if (object instanceof View && objects instanceof ArrayList<?> && !objects.isEmpty()) {
					Rect positionRect = (Rect) objects.get(0);
					startOpenFolderLayout((View) object, positionRect, param);
				}
			}
				break;

			case IDiyMsgIds.FOLDER_CLOSED : {
				showViewsNameWhenCloseFolder();
				// 文件夹关闭完成后再开始隐藏状态栏
				if (mWorkspace.mDragging && !StatusBarHandler.isHide() && object != null
						&& object instanceof View) {
					// 要求显示全屏并重新排版
					GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
							IDiyMsgIds.SHOW_HIDE_STATUSBAR, -2, true, null);
					mLayout.getIndicator().hide();
					ArrayList<Integer> list = new ArrayList<Integer>();
					list.add(0);
					list.add(-StatusBarHandler.getStatusbarHeight());
					// 通知拖拽层变更拖拽点的坐标
					GoLauncher.sendMessage(this, IDiyFrameIds.DRAG_FRAME,
							IDiyMsgIds.UPDATE_DRAG_POINTS, -1, null, list);
				}
			}
				break;

			case IDiyMsgIds.GET_CURRENT_VIEW_CACHE_BMP : {
				if (object != null && !(object instanceof FolderRelativeLayout)) {
					return false;
				}

				FolderRelativeLayout folderLayout = (FolderRelativeLayout) object;
				CellLayout currentCellLayout = getCurrentScreen();

				int count = (currentCellLayout != null) ? currentCellLayout.getChildCount() : 0;
				for (int i = 0; i < count; i++) {
					View childView = currentCellLayout.getChildAt(i);
					Bitmap childBmp = null;
					try {
						childBmp = Bitmap.createBitmap(childView.getWidth(), childView.getHeight(),
								Config.ARGB_8888);
					} catch (Throwable e) {
						// 出现异常,这张bmp不生成
						continue;
					}
					if (null != childBmp) {
						Canvas cv = new Canvas(childBmp);
						childView.draw(cv);

						Rect rect = new Rect(childView.getLeft(), childView.getTop(),
								childView.getRight(), childView.getBottom());
						folderLayout.addCacheBmp(childBmp, rect);
					}
				}
			}
				break;

			case IDiyMsgIds.GO_LOCKER_CHANGED : {
				if (mTextLayoutvisiable) {
					Animation animation = MyAnimationUtils.getPopupAnimation(
							MyAnimationUtils.POP_TO_LONG_START_HIDE_2, -1);
					if (animation != null && mTextLayout != null) {
						animation.setAnimationListener(this);
						mTextLayout.startAnimation(animation);
						mTextLayout.setVisibility(View.INVISIBLE);
					}
					mIndicator.setVisibility(View.VISIBLE);
				}
				break;
			}

			case IDiyMsgIds.SCREENEDIT_ENTER_SCREEN_EDIT_LAYOUT :
				if (mWorkspace != null) {
					// boolean animation = param == 1 ? true : false;
					String editTab = (String) object;
					mCurTab = editTab;
					mWorkspace.normalToSmallPreAction(editTab, null);
				}
				break;

			case IDiyMsgIds.SCREENEDIT_CLOSE_SCREEN_EDIT_LAYOUT :
				if (mWorkspace != null) {
					boolean animation = param == 1 ? true : false;
					mWorkspace.smallToNormal(animation);
				}
				break;

			case IDiyMsgIds.SCREENEDIT_SCREEN_ZOOM :
				if (object != null && object instanceof Float) {
					mWorkspace.prepareToZoom((Float) object);
				}
				break;

			case IDiyMsgIds.SCREEN_GENERATE_PREVIEW_BMP :
				if (objects != null && objects instanceof ArrayList) {
					getAppDrawCards((ArrayList<Object>) objects);
				}
				break;
			case IDiyMsgIds.SCREEN_GENERATE_SCREEN_BMP :
				if (object != null && object instanceof AppFuncScreenItemInfo) {
					((AppFuncScreenItemInfo) object).setScreenBmp(getAppDrawCardBitmap(param));
					((AppFuncScreenItemInfo) object).setVancantCellCnt(ScreenUtils
							.findSingleVancant(param == -1 ? mWorkspace.getCurrentScreen() : param,
									null, mWorkspace));
				}
				break;
			case IDiyMsgIds.APPDRAWER_ADD_ITEM_TO_SCREEN :
				if (object != null && objects != null) {
					if (!isLoading()) {
						ItemInfo itemInfo = (ItemInfo) objects.get(0);
						int[] screenRelativeCoordinate = (int[]) object;
						int screenIndex = param;
						if (itemInfo != null && screenIndex >= 0) {
							int[] cellPos = ScreenUtils.findNearestVacant(
									screenRelativeCoordinate[0], screenRelativeCoordinate[1],
									itemInfo.mSpanX, itemInfo.mSpanY, screenIndex, mWorkspace);
							if (cellPos != null) {
								ScreenUtils.cellToRealCenterPoint(screenIndex, cellPos[0],
										cellPos[1], mWorkspace, screenRelativeCoordinate);
								itemInfo.mCellX = cellPos[0];
								itemInfo.mCellY = cellPos[1];
								if (itemInfo instanceof ShortCutInfo) {
									// add shortcut onto screen
									ShortCutInfo scInfo = (ShortCutInfo) itemInfo;
									BubbleTextView bubble = inflateBubbleTextView(scInfo.mTitle,
											scInfo.mIcon, scInfo);
									mWorkspace.addInScreen(bubble, screenIndex, scInfo.mCellX,
											scInfo.mCellY, scInfo.mSpanX, scInfo.mSpanY, true);
									addDesktopItem(screenIndex, scInfo);
								} else if (itemInfo instanceof UserFolderInfo) {
									// add folder onto screen
									final UserFolderInfo folderInfo = (UserFolderInfo) itemInfo;
									addUserFolder(screenIndex, folderInfo.mCellX,
											folderInfo.mCellY, folderInfo);
									// 异步添加文件夹元素解决ADT-4712功能表文件夹中图标比较多时，
									// 将文件夹添加到桌面时文件夹停留在屏幕预览的时间过长的问题
									new Thread(ThreadName.ADD_USER_FOLDER_CONTENT) {
										@Override
										public void run() {
											int count = folderInfo.getChildCount();
											ArrayList<ItemInfo> items = new ArrayList<ItemInfo>(
													count);
											for (int i = 0; i < count; i++) {
												items.add(folderInfo.getChildInfo(i));
											}
											addUserFolderContent(folderInfo.mInScreenId,
													folderInfo, items, true);
										}
									}.start();
								}
								int widthSpec = MeasureSpec.makeMeasureSpec(
										mWorkspace.getMeasuredWidth(), MeasureSpec.EXACTLY);
								int heightSpec = MeasureSpec.makeMeasureSpec(
										mWorkspace.getMeasuredHeight(), MeasureSpec.EXACTLY);
								mWorkspace.requestLayout();
								mWorkspace.measure(widthSpec, heightSpec);
							}
						}
					} else {
						ScreenUtils.showToast(R.string.loading_screen, mActivity);
					}
				}
				break;

			case IDiyMsgIds.SCREEN_RELOAD_DESK : {
				reloadDesktop();
			}
				break;

			case IDiyMsgIds.SCREENEDIT_WORKSPACE_INDICATOR_UP :
				mLayout.setScreenEditState(true, param == 1);
				break;

			case IDiyMsgIds.SCREENEDIT_WORKSPACE_INDICATOR_DOWN :
				mLayout.setScreenEditState(false, param == 1);
				break;

			case IDiyMsgIds.MOVE_SCREEN_FOLDER_TO_DOCK : {
				// 删数据
				View view = (View) object;
				ItemInfo itemInfo = (ItemInfo) view.getTag();
				mControler.removeDesktopItem(itemInfo);
				// 删view
				CellLayout cellLayout = (CellLayout) view.getParent();
				cellLayout.removeView(view);
			}
				break;

			case IDiyMsgIds.MOVE_SCREEN_SHORTCUT_TO_DOCK : {
				try {
					if (object != null && object instanceof View) {
						// 删数据
						View view = (View) object;
						ItemInfo itemInfo = (ItemInfo) view.getTag();
						mControler.removeDesktopItemInDBAndCache(itemInfo);
						ArrayList<BroadCasterObserver> observers = itemInfo.getObserver();
						if (null != observers) {
							int size = observers.size();
							for (int i = 0; i < size; i++) {
								BroadCasterObserver observer = observers.get(i);
								if (!(observer instanceof DockItemInfo)) {
									itemInfo.unRegisterObserver(observer);
									size = observers.size();
									i--;
								}
							}
						}
						// 删view
						CellLayout cellLayout = (CellLayout) view.getParent();
						if (cellLayout != null) {
							cellLayout.removeView(view);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
				break;
			case IDiyMsgIds.SCREEN_REQUEST_LAYOUT : {
				if (object != null && object instanceof Integer) {
					mWorkspace.requestLayout(param, (Integer) object);
				}
			}
				break;
			case IDiyMsgIds.SCREEN_REPLACE_RECOMMEND_ICON : {
				String pkgName = (String) object;
				// replaceDeskIcon(IItemType.ITEM_TYPE_SHORTCUT,ICustomAction.ACTION_RECOMMEND_DOWNLOAD,
				// pkgName);
				replaceDeskIcon(pkgName); // modify by Ryan 2012.08.09
			}
				break;

			// case IDiyMsgIds.SCREEN_REPLACE_EVERNOTE_ICON:
			// {
			// replaceDeskIcon(IItemType.ITEM_TYPE_SHORTCUT,ICustomAction.ACTION_SHOW_EVERNOTE,
			// LauncherEnv.Plugin.RECOMMEND_EVERNOTE_PACKAGE);
			// }
			// break;

			case IDiyMsgIds.REPLACE_RECOMMAND_ICON_IN_FOLDER : {
				String pkgName = (String) object;
				replaceRecommandIconInFolder(pkgName);
				break;
			}

			case IDiyMsgIds.SCREEN_MENU_SHOW : {
				ret = GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, msgId, param,
						object, objects);
				break;
			}

			case IDiyMsgIds.GET_DOCK_OPEN_FOLDER_ICON_LAYOUTDATA : {
				if (null != object && object instanceof Long && null != objects
						&& objects instanceof ArrayList<?>) {
					View view = ScreenUtils.getViewByItemId((Long) object, getCurrentScreenIndex(),
							mWorkspace);
					Bitmap bmp = BitmapUtility.createBitmap(view, 1.0f);
					if (null != bmp) {
						objects.add(bmp);
					}
				}
			}
				break;

			case IDiyMsgIds.UPDATE_FOLDER_ITEM_INFO : {
				boolean updated = false;
				if (null != object && object instanceof Long && null != objects) {
					long folderid = (Long) object;
					ArrayList<ShortCutInfo> infos = (ArrayList<ShortCutInfo>) objects;
					int size = infos.size();
					for (int i = 0; i < size; i++) {
						ShortCutInfo info = infos.get(i);
						mControler.updateFolderItem(folderid, info);
						updated = true;
					}
					if (updated) {
						// 更新后，要更新文件夹缩略图
						UserFolderInfo userFolderInfo = getFolderItemInfo(folderid);
						updateFolderIconAsync(userFolderInfo, false, false);
					}
				}
			}
				break;

			case IDiyMsgIds.SCREEN_RESET_DEFAULT : {
				resetDefaultIcon();
			}
				break;

			case IDiyMsgIds.RENAME : {
				if (null != object && object instanceof Long && null != objects
						&& !objects.isEmpty()) {
					long itemid = (Long) object;
					String name = (String) objects.get(0);
					actionRename(name, itemid);
				}
			}
				break;
			case IDiyMsgIds.CHECK_GRID_STATE :
				mWorkspace.checkGridState(param, -1);
				break;
			case IDiyMsgIds.SHOW_PROGRESSBAR :
				showProgressDialog();
				break;
			case IDiyMsgIds.SCREEN_AUTO_FLY :
				screenAutoFly(object, param);
				break;
			case IDiyMsgIds.SCREEN_DEL_ITEM_FROM_FOLDER :
				ret = screenDeleteItemFromFolder(object);
				break;
			case IDiyMsgIds.FLY_APP_TO_FOLDER :
				if (object != null && object instanceof View) {
					Rect rect = null;
					if (objects != null && objects.size() > 0 && objects.get(0) instanceof Rect) {
						// 最后放置的位置
						rect = (Rect) objects.get(0);
					}
					flyAppToFolder((View) object, rect, param);
				}
				break;
			case IDiyMsgIds.IN_NEW_FOLDER_STATE :
				inNewFolderState();
				break;
			case IDiyMsgIds.LEAVE_NEW_FOLDER_STATE :
				leaveNewFolderState();
				break;
			case IDiyMsgIds.ADD_ITEM_TO_SCREEN :
				flyIconToScreen(object, objects);
				// add by jiang添加完允许滑动
				mWorkspace.allowTouch();
				break;
			case IDiyMsgIds.GET_SHARE_IMAGE_NUM :
				if (null != object && object instanceof Bundle) {
					((Bundle) object).putInt("imagenum", mWorkspace.getChildCount());
				}
				break;
			case IDiyMsgIds.GET_SHARE_IMAGE :
				if (objects != null && objects instanceof ArrayList) {
					float index = (Float) objects.get(0);
					float scale = (Float) objects.get(1);
					if (null != object && object instanceof Bundle) {
						((Bundle) object).putParcelable("image",
								createShareImage(Math.round(index), scale));
					}
				}
				break;
			case IDiyMsgIds.CAN_GET_WALLPAPER :
				if (mWorkspace != null && mWorkspace.mScroller != null
						&& (mWorkspace.mScroller.getBackground() != null)) {
					ret = true;
				}
				break;
			case IDiyMsgIds.GET_MAIN_SCREEN_INDEX :
				if (null != object && object instanceof Bundle) {
					((Bundle) object).putInt("mainscreen", mWorkspace.getMainScreen());
				}
				break;
			case IDiyMsgIds.MOVE_DOCK_ITEM_TO_SCREEN :
				if (object != null && object instanceof ItemInfo) {
					int screenIndex = mWorkspace.getCurrentScreen();
					ItemInfo itemInfo = (ItemInfo) object;
					int[] cell = new int[] { itemInfo.mCellX, itemInfo.mCellY };
					if (ScreenUtils.findVacant(cell, 1, 1, screenIndex, mWorkspace)) {
						addDesktopItem(screenIndex, itemInfo);
						addDesktopView(itemInfo, screenIndex, true);
						ret = true;
					}
				}
				break;
			case IDiyMsgIds.GET_DROP_DOCK_LOCATION :
				if (object != null && object instanceof View && objects != null) {
					View dragView = (View) object;
					int[] center = (int[]) objects.get(0);
					if (dragView instanceof BubbleTextView
							&& dragView.getParent() == mWorkspace.getCurrentScreenView()) {
						CellLayout.LayoutParams lp = (CellLayout.LayoutParams) dragView
								.getLayoutParams();
						int[] cell = new int[] { lp.cellX, lp.cellY };
						objects.add(cell);
						mWorkspace.getCurrentScreenView().cellToCenterPoint(cell[0], cell[1],
								center);
						ret = true;
					} else {
						int[] xy = new int[2];
						boolean vacant = ScreenUtils.findVacant(xy, 1, 1,
								mWorkspace.getCurrentScreen(), mWorkspace);
						if (vacant) {
							objects.add(xy);
							mWorkspace.getCurrentScreenView().cellToCenterPoint(xy[0], xy[1],
									center);
							ret = true;
						}
					}
				}
				break;

			case IDiyMsgIds.DRAG_AREA_CHANGE : {
				if (object != null && object instanceof int[]) {
					int[] types = (int[]) object;
					// 由workspace拖进dock区域,如果是一开始就快速拖动的话，上次的dragType将是-1而不是DRAG_IN_WORKSPACE
					if ((types[0] == ScreenDragHandler.DRAG_IN_WORKSPACE || types[0] == -1)
							&& types[1] == ScreenDragHandler.DRAG_IN_DOCK) {
						mWorkspace.setCurrentDropLayout(null);
						clearVisualize();
					}
				}
			}
				break;
			case IDiyMsgIds.WIDGET_DELAY_REFRESH :
				mHandler.postDelayed(mRefreshRunnable, param);
				break;

			case IDiyMsgIds.EVENT_THEME_CHANGED : {
			if (object != null && object instanceof String) {
				ThemeInfoBean infoBean = ThemeManager.getInstance(mActivity).getCurThemeInfoBean();
				if (infoBean != null) {
					boolean remove = true;
					ThemeInfoBean.MiddleViewBean middleViewBean = infoBean.getMiddleViewBean();
					if (middleViewBean != null) {
						if (middleViewBean.mHasMiddleView) {
							mBackWorkspace.setMiddleView(infoBean.getPackageName(),
									middleViewBean.mIsSurfaceView);
							remove = false;
						}
					} 
					if (remove) {
						mBackWorkspace.removeMiddleView();
					}
				} // end infoBean
			} // end object
			}
			    break;
			    
			case IDiyMsgIds.SCREEN_HIDE_MIDDLE_VIEW : {
				mBackWorkspace.hideMiddleView();
				mBackWorkspace.onStateMethod(BackWorkspace.STATE_ON_STOP);
			}
			    break;
			
			case IDiyMsgIds.SCREEN_SHOW_MIDDLE_VIEW : {
				mBackWorkspace.showMiddleView();
				mBackWorkspace.onStateMethod(BackWorkspace.STATE_ON_RESUME);
			}
			    break;
			    
			case IDiyMsgIds.SCREEN_SEND_BROADCASTTO_MULTIPLEWALLPAPER :
				// add by chenbingdong
				// 通过ScreenFrame来发送广播给多屏多壁纸
				mWorkspace.sendBroadcastToMultipleWallpaper(false, false);
				break;
				
			case IDiyMsgIds.SCREEN_SMALL_TO_NORMAL :
				// add by chenbingdong
				// 退出添加界面，恢复正常
				if (mWorkspace != null) {
					mWorkspace.smallToNormal(false);
				}
				break;
				
			case IDiyMsgIds.CHANGE_GOTOMAINSCREEN_FOR_HOMECLICK	:
				if (param != 0) {
					mGotoMainScreen = 0; // 跳转主屏
				} else {
					mGotoMainScreen++;
				}
				break;
				
			case IDiyMsgIds.SCREEN_HOLIDAY_SWITCHER_STATE_CHANGE :
			{
//				final int state = param == AlarmHandler.SWITCHER_STATE_ON ? param : AlarmHandler.SWITCHER_STATE_OFF;
//				mWorkspace.setSwitcher(state);
			}
			   break;
			
			//判断桌面1、5屏是否可以插入广告图标	
			case IDiyMsgIds.SCREEN_CAN_ADD_ADVERT_SHORT_CUT:	
				ret = isCanAddAdvertIcon();
				break;
			
			//桌面1、5屏添加广告图标
			case IDiyMsgIds.SCREEN_ADD_ADVERT_SHORT_CUT:
				ret = addAdvertShortCut(object, param);
				break;
				
			//桌面1、5屏添加文件夹广告图标	
			case IDiyMsgIds.SCREEN_ADD_ADVERT_FOLDER:
				ret = addAdvertFolder(object, param);
				break;
				
			//判断桌面1、5屏/首屏第一次请求成功后判断是否对屏幕做过修改
			case IDiyMsgIds.SCREEN_CAN_CHANGE_ADVERT_SHORT_CUT:
				ret = isCanChangeAdvertIcon(object);
				break;	
				
			//清除15屏广告图标
			case IDiyMsgIds.SCREEN_CLEAR_ADVERT_ICON:
				ret = clearAdvertIcon(object);
				break;		
				
			//检查是抖动推荐图标
			case IDiyMsgIds.RECOMMEND_ICON_SHANK:
				ret = checkRecommendIconShake(param);
				break;		
				
			case IDiyMsgIds.START_INDICATOR_ANIMATION_FOR_SCREEN_EDIT:
				animationForScreenEdit(param, (Boolean) object);
				break;
				
			case IDiyMsgIds.SCREEN_SHOW_QUICK_ACTION_MENU : {
				if (object != null && object instanceof View) {
					showQuickActionMenu((View) object);
				}
			}
			break;
			
			//设置首屏图标信息缓存
			case IDiyMsgIds.SET_HOME_SCREEN_ICON_CACHE:
				ret = setHomeScreenIconCache();
				break;	
			
			//判断桌面首屏是否可以插入广告图标
			case IDiyMsgIds.SCREEN_CAN_ADD_ADVERT_TO_HOME_SCREEN:
				ret = isCanChangeHomeScreenAdvertIcon();
				break;	
				
			//判断桌面首屏是否可以插入广告图标
			case IDiyMsgIds.ADD_STORE_RECOMMEND_ICON_AND_SHAKE:
				ret = addAppStoreRecommendIcon(object);
				break;	
				
			default :
				break;
		}
		return ret;
	}

	private Runnable mRefreshRunnable = new Runnable() {
		public void run() {
			mHandler.sendEmptyMessage(WIDGET_DELAY_REF);
		}
	};

	private PaintFlagsDrawFilter mPpaintFilter;

	public DesktopIndicator getIndicator() {
		return mIndicator;
	}

	/***
	 * 拿图片
	 * 
	 * @return
	 */
	private Bitmap createShareImage(int index, float scale) {
		int width = (int) (GoLauncher.getDisplayWidth() * scale);
		int height = (int) (GoLauncher.getDisplayHeight() * scale);
		// 创建空画布
		Bitmap img = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas();
		canvas.setBitmap(img);
		if (null != mPpaintFilter) {
			canvas.setDrawFilter(mPpaintFilter);
		}
		canvas.scale(scale, scale);
		/** 壁纸 */
		mWorkspace.drawScreenBg(canvas, index);

		/** 桌面图片并拼图 */
		CellLayout cellLayout = mWorkspace.getScreenView(index);
		cellLayout.dispatchDraw(canvas);

		if (ShortCutSettingInfo.sEnable) {
			/** dock条 */
			GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME, IDiyMsgIds.DOCK_GET_VIEW_IMAGE,
					-1, canvas, null);
		}

		/** 指示器 */
		int tempCurrent = mIndicator.getCurrent();
		mIndicator.setCurrent(index);
		mIndicator.dispatchDraw(canvas);
		mIndicator.setCurrent(tempCurrent);

		return img;
	}

	/***
	 * 屏幕飞图标
	 * 
	 * @param object
	 * @param dragType
	 * @return
	 */
	private boolean screenAutoFly(Object object, int dragType) {
		mCellPos = new int[2];
		if (dragType == DragFrame.TYPE_ADD_ITEM_IN_FOLDER) {
			// 考虑页面发生拖动的情况。（现在已经不允许拖动）
			// int folderInScreenIndex =
			// mWorkspace.findScreenIndex(mNewFolderId);
			// if(folderInScreenIndex!=-1 &&
			// mWorkspace.getCurrentScreen()!=folderInScreenIndex){
			// mWorkspace.snapToScreen(folderInScreenIndex, true, 100);
			// }
			View folder = mWorkspace.getFolderByRefId(mNewFolderId, mWorkspace.getCurrentScreen());
			if (folder == null || !(folder instanceof FolderIcon)
					|| folder.getLayoutParams() == null) {
				return false;
			}
			mNewFolderIcon = (FolderIcon) folder;
			// 新建文件夹可能不在当前页上
			if (((FolderIcon) folder).getInfo().mScreenIndex != mWorkspace.getCurrentScreen()) {
				return false;
			}
			CellLayout.LayoutParams lp = (CellLayout.LayoutParams) folder.getLayoutParams();
			mCellPos[0] = lp.cellX;
			mCellPos[1] = lp.cellY;

		} else if (dragType == DragFrame.TYPE_ADD_WIDGET_DRAG && object instanceof View) {
			// go小部件添加
			AbsWidgetInfo info = (AbsWidgetInfo) ((View) object).getTag();
			if (info != null) {
				int wRow = info.mRow;
				int mCol = info.mCol;
				ScreenUtils.findVacant(mCellPos, mCol, wRow, mWorkspace.getCurrentScreen(),
						mWorkspace);
			}
		} else {
			// 普通应用程序添加
			ScreenUtils.findVacant(mCellPos, 1, 1, mWorkspace.getCurrentScreen(), mWorkspace);
		}
		if (null != mCellPos && mCellPos.length == 2) {
			int[] pos = new int[2];
			ScreenUtils.cellToPoint(mWorkspace.getCurrentScreen(), mCellPos[0], mCellPos[1],
					mWorkspace, pos);
			if (Workspace.getLayoutScale() < 1.0) {
				float[] realXY = new float[2];
				Workspace.realPointToVirtual(pos[0], pos[1], realXY);
				pos[0] = (int) realXY[0];
				pos[1] = (int) realXY[1];
			}
			// 用于最后的图标生成（flyIconToScreen）
			mRectTemp = new Rect(pos[0], pos[1], pos[0] + mCellPos[0], pos[1] + mCellPos[1]);
			ArrayList<Integer> list = new ArrayList<Integer>();
			list.add(pos[0]);
			list.add(pos[1]);
			// 准备拖拽层
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IFrameworkMsgId.SHOW_FRAME,
					IDiyFrameIds.DRAG_FRAME, null, null);
			// 显示指示器(指示器在下端时继续隐藏不显示)
			if (!mLayout.isIndicatorOnBottom()) {
				showIndicator();
			}
			// add by jiang 添加的时候禁止滑动
			mWorkspace.unTouch();
			// 让图标飞
			GoLauncher.sendMessage(this, IDiyFrameIds.DRAG_FRAME, IDiyMsgIds.START_TO_AUTO_FLY,
					dragType, object, list);
		}
		return true;
	}

	/***
	 * 删除文件夹中的一项
	 * 
	 * @param object
	 * @return
	 */
	private boolean screenDeleteItemFromFolder(Object object) {
		// 考虑页面发生拖动的情况。（现在已经不允许拖动）
		// int folderInScreenIndex = mWorkspace.findScreenIndex(mNewFolderId);
		// if(folderInScreenIndex!=-1 && mWorkspace.getCurrentScreen() !=
		// folderInScreenIndex){
		// mWorkspace.snapToScreen(folderInScreenIndex, true, 200);
		// }
		View folder = mWorkspace.getFolderByRefId(mNewFolderId, mWorkspace.getCurrentScreen());
		if (folder == null || !(folder instanceof FolderIcon) || folder.getLayoutParams() == null) {
			return false;
		}
		mCurrentFolderInfo = ((FolderIcon) folder).getInfo();
		if (mCurrentFolderInfo != null) {
			View targetView = (View) object;
			ItemInfo tagInfo = (ItemInfo) targetView.getTag();
			if (null != mControler) {
				mControler.moveDesktopItemFromFolder(tagInfo, mWorkspace.getCurrentScreen(),
						mCurrentFolderInfo.mInScreenId);
				GoLauncher.sendMessage(this, IDiyFrameIds.DESK_USER_FOLDER_FRAME,
						IDiyMsgIds.DELETE_CACHE_INFO_IN_FOLDER, -1, tagInfo, null);
			}

			// 更新缓存
			if (mCurrentFolderInfo instanceof UserFolderInfo) {
				((UserFolderInfo) mCurrentFolderInfo).remove(tagInfo.mInScreenId);
				// 更新文件夹图标
				updateFolderIconAsync((UserFolderInfo) mCurrentFolderInfo, false, false);
			}
			mCurrentFolderInfo = null;
		}
		return true;
	}

	/***
	 * 飞图标到文件夹
	 * 
	 * @param targetView
	 * @param rect
	 * @param dragType
	 * @return
	 */
	private boolean flyAppToFolder(View targetView, Rect rect, int dragType) {
		Object tagObject = targetView.getTag();
		if (tagObject == null) {
			return false;
		}
		if (dragType == DragFrame.TYPE_ADD_ITEM_IN_FOLDER) {
			if (null == mNewFolderIcon) {
				return false;
			}
			CellLayout currentScreen = mWorkspace.getCurrentScreenView();
			if (currentScreen == null) {
				return false;
			}
			if (rect != null) {
				ShortCutInfo itemInfo = (ShortCutInfo) tagObject;
				if (targetView instanceof BubbleTextView) {
					itemInfo.unRegisterObserver((BubbleTextView) targetView);
				}
				UserFolderInfo folderInfo = (UserFolderInfo) mNewFolderIcon.getTag();
				if (null == folderInfo) {
					return false;
				}
				if (null != mControler) {
					// 先修改数据库
					mControler.moveDesktopItemToFolder(itemInfo, folderInfo.mInScreenId);
					// 获取应用的未读数
					AppItemInfo appItemInfo = GOLauncherApp.getAppDataEngine().getAppItem(
							itemInfo.mIntent);
					itemInfo.setRelativeItemInfo(appItemInfo);
					int appType = AppIdentifier.getNotificationType(mActivity, itemInfo);
					if (appType != NotificationType.IS_NOT_NOTIFICSTION) {
						if (appType == NotificationType.NOTIFICATIONTYPE_MORE_APP) {
							itemInfo.mCounter = appItemInfo.getUnreadCount();
						} else {
							itemInfo.mCounter = AppCore.getInstance().getNotificationControler()
									.getNotification(appType);
						}
						itemInfo.mCounterType = appType;
					}
					// 后添加到缓存
					folderInfo.add(itemInfo);
				}
				// 更新文件夹图标
				folderInfo.mIsFirstCreate = false;
				updateFolderIconAsync((UserFolderInfo) mNewFolderIcon.getTag(), false, false);

				// 开始进入文件夹缩小动画
				if (null != mNewFolderIcon.getTag()
						&& mNewFolderIcon.getTag() instanceof UserFolderInfo) {
					currentScreen.startIntoFolderZoomoutAnimation(targetView, mNewFolderIcon, rect,
							((UserFolderInfo) mNewFolderIcon.getTag()).getChildCount());
				}
				currentScreen.setmDrawStatus(CellLayout.DRAW_STATUS_INTO_FOLDER_ZOOMOUT);
				clearDragState();
				currentScreen.setStatusNormal();
			}
		}
		return true;
	}

	/***
	 * 添加页面，开始添加新文件夹
	 */
	private synchronized void inNewFolderState() {
		if (mWorkspace.getTouchState()) {
			mWorkspace.unTouch();

			CellLayout currentCellLayout = getCurrentScreen();
			Bitmap bmp = null;
			try {
				bmp = Bitmap.createBitmap(currentCellLayout.getWidth(),
						currentCellLayout.getHeight(), Config.ARGB_8888);
			} catch (Throwable e) {
				e.printStackTrace();
			}
			if (null != bmp) {
				Canvas cv = new Canvas(bmp);
				int count = currentCellLayout.getChildCount();
				Paint paint = new Paint();
				paint.setAlpha(120);
				for (int i = 0; i < count; i++) {
					View childView = currentCellLayout.getChildAt(i);
					if (null != childView && childView != mNewFolderIcon) {
						Bitmap childBmp = BitmapUtility.createBitmap(childView, 1.0f);
						if (null != childBmp) {
							cv.drawBitmap(childBmp, childView.getLeft(), childView.getTop(), paint);
						}
					}
				}
				currentCellLayout.startNewFolder(new BitmapDrawable(bmp), null, mNewFolderIcon);
				// 把相邻的两侧的卡片也设成半透明
				int index = mWorkspace.getCurrentScreen();
				if (index - 1 >= 0) {
					CellLayout leftCellLayout = mWorkspace.getScreenView(index - 1);
					if (null != leftCellLayout) {
						leftCellLayout.setCoverWithBg(true);
					}
				}
				if (index + 1 < mWorkspace.getChildCount()) {
					CellLayout rightCellLayout = mWorkspace.getScreenView(index + 1);
					if (null != rightCellLayout) {
						rightCellLayout.setCoverWithBg(true);
					}
				}
			}
			if (null != mIndicator) {
				mIndicator.setTouchable(false);
			}
		}
	}

	/***
	 * 添加页面，结束添加新文件夹
	 */
	private synchronized void leaveNewFolderState() {
		if (null != mIndicator) {
			mIndicator.setTouchable(true);
		}
		if (!mWorkspace.getTouchState()) {
			mWorkspace.allowTouch();
			if (null != mNewFolderIcon) {
				UserFolderInfo folderInfo = (UserFolderInfo) mNewFolderIcon.getTag();
				deleteFolderOrNot(folderInfo, false);
				//文件夹智能命名-添加界面新建文件夹
				ArrayList<AppItemInfo> infoList = new ArrayList<AppItemInfo>();
				for (int i = 0; i < folderInfo.getChildCount(); i++) {
					ShortCutInfo itemInfo = folderInfo.getChildInfo(i);
					if (itemInfo != null) {
						infoList.add(itemInfo.getRelativeItemInfo());
					}
				}
				String smartfoldername = CommonControler.getInstance(mActivity)
						.generateFolderName(infoList);
				actionRename(smartfoldername, folderInfo.mInScreenId);
				// 进行反注册销毁，否则会存在内存泄漏
				folderInfo.selfDestruct();
				folderInfo = null;
				mNewFolderIcon.selfDestruct();
				mNewFolderIcon = null;
			}
			for (int i = 0; i < mWorkspace.getChildCount(); i++) {
				CellLayout cellLayout = mWorkspace.getScreenView(i);
				if (null != cellLayout) {
					cellLayout.startNewFolder(null, null, null);
					cellLayout.setCoverWithBg(false);
				}
			}
		}

	}

	/***
	 * 显示指示器
	 */
	private void showIndicator() {
		mEnterPrew = false;
		// 加入指示器的展现条件是为了减少被多次调用
		boolean showIndicator = true;
		if (mScreenToPreview) {
			mScreenToPreview = false;
			showIndicator = mScreenSettingInfo.mAutoHideIndicator ? false : true;
		}
		if (showIndicator) {
			mLayout.getIndicator().show();
		}
	}

	/**
	 * 重置指示器位置
	 */
	private void setIndicatorPost(String position) {
		if (position != null) {
			mLayout.setIndicatorOnBottom(position.equals(ScreenIndicator.INDICRATOR_ON_BOTTOM));
		}
		mLayout.requestLayout();
		for (int i = 0; i < mLayout.getWorkspace().getChildCount(); i++) {
			View screenView = mLayout.getWorkspace().getChildAt(i);
			if (screenView != null) {
				screenView.requestLayout();
			}
		}
	}

	private void startOpenFolderLayout(View folderView, Rect rect, int arrowDirection) {
		//		boolean isFolderIcon = false;
		//		if (folderView != null) {
		//			if (folderView instanceof FolderIcon) {
		//				// 如果是文件夹，则把罩子去掉
		//				FolderIcon folderIcon = (FolderIcon) folderView;
		//				Drawable foldDrawable = new BitmapDrawable(FolderIcon.combinDraw(
		//						folderIcon.getRawIcon(), folderIcon.getInfo(),
		//						Utilities.getStandardIconSize(mActivity), true));
		//				folderIcon.setIcon(foldDrawable);
		//				isFolderIcon = true;
		//			}
		//		}
		Bitmap bmp = BitmapUtility.createBitmap(folderView, 1.0f);
		//		ArrayList<Object> list = new ArrayList<Object>();
		ArrayList<Rect> list = new ArrayList<Rect>();
		list.add(rect);
		//		if (isFolderIcon) {
		//			list.add(1, folderView);
		//		}
		GoLauncher.sendMessage(this, IDiyFrameIds.DESK_USER_FOLDER_FRAME,
				IDiyMsgIds.FOLDER_LAYOUT_DATA, arrowDirection, bmp, list);
	}

	// 刷新文件夹里面内容
	private void refreshFolderItems(long folderId, boolean checkDel) {
		UserFolderInfo info = getFolderItemInfo(folderId);
		if (info != null) {
			Message msg = new Message();
			msg.what = REFRESH_FOLDER_CONTENT;
			msg.obj = info;
			msg.arg1 = checkDel ? 1 : 0;
			mHandler.sendMessage(msg);
			msg = null;
		}
	}

	private void refreshItemList(ArrayList<ItemInfo> itemInfos) {
		if (itemInfos != null && itemInfos.size() > 0) {
			mHandler.sendMessage(mHandler.obtainMessage(UPDATE_ITEMS_IN_SDCARD, itemInfos));
		}
	}

	private void replaceFavWidgets(String pkgName) {
		if (pkgName == null
				|| (!pkgName.contains(ICustomAction.MAIN_GOWIDGET_PACKAGE) && !pkgName
						.equals(LauncherEnv.GOSMS_PACKAGE))) {
			return;
		}

		if (pkgName.equals(LauncherEnv.GOSMS_PACKAGE)) {
			int versionCode = AppUtils
					.getVersionCodeByPkgName(mActivity, LauncherEnv.GOSMS_PACKAGE);
			if (versionCode < 80) {
				return;
			}
		}

		if (null == mControler) {
			return;
		}

		// 任务管理器需要特殊处理
		GoWidgetManager widgetManager = AppCore.getInstance().getGoWidgetManager();
		pkgName = widgetManager.getInflatePackage(pkgName);
		int screenCount = mWorkspace.getChildCount();
		for (int i = 0; i < screenCount; i++) {
			CellLayout layout = (CellLayout) mWorkspace.getChildAt(i);
			if (layout == null) {
				continue;
			}

			int index = 0;
			while (index < layout.getChildCount()) {
				View v = layout.getChildAt(index);
				if (v != null) {
					ItemInfo info = (ItemInfo) v.getTag();
					if (info != null && info.mItemType == IItemType.ITEM_TYPE_FAVORITE) {
						GoWidgetBaseInfo widgetBaseInfo = ((FavoriteInfo) info).mWidgetInfo;
						// 任务管理器的话要转换一下包名
						if (widgetBaseInfo != null && pkgName.equals(widgetBaseInfo.mPackage)) {
							// 删除旧视图
							layout.removeView(v);
							// 防止屏幕可能被删减，所以推荐widget的当前屏数有变动
							info.mScreenIndex = i;
							mControler.removeDesktopItem(info);

							// 添加新视图
							AppCore.getInstance().getGoWidgetManager().addGoWidget(widgetBaseInfo);
							ScreenAppWidgetInfo widgetInfo = new ScreenAppWidgetInfo(
									widgetBaseInfo.mWidgetId, null, info);
							mControler.addDesktopItem(info.mScreenIndex, widgetInfo);
							View widgetView = filterWidgetView(widgetInfo);
							mWorkspace.addInScreen(widgetView, widgetInfo.mScreenIndex,
									widgetInfo.mCellX, widgetInfo.mCellY, widgetInfo.mSpanX,
									widgetInfo.mSpanY, false);

							// 暂时保留，如果短信等widget初始化时有默认值则去除
							if (!pkgName.equals(ThemeManager.DEFAULT_THEME_PACKAGE)) {
								reloadDesktop();
							}
							return; // NOTE：请确认时return还是continue
						}
					}
				}
				index++;
			}
		}
	}

	private void handleUninstallIntent(Intent intent) {
		mHandler.sendMessage(mHandler.obtainMessage(REFRESH_UNINSTALL, intent));
		final ArrayList<ItemInfo> itemInfos = mControler.unInstallApp(intent);
		if (null != itemInfos) {
			mHandler.sendMessage(mHandler.obtainMessage(UPDATE_FOLDER_LIST, itemInfos));
		}
	}

	private void handleUninstallApps(ArrayList<AppItemInfo> infos) {
		if (infos == null) {
			return;
		}
		int size = infos.size();

		for (int i = 0; i < size; i++) {
			AppItemInfo info = infos.get(i);
			if (null == info) {
				continue;
			}

			handleUninstallIntent(info.mIntent);
		}
	}

	private void registGoWidgetAction() {
		mGoWidgetActionReceiver = new GoWidgetActionReceiver();
		IntentFilter filter = new IntentFilter(ICustomAction.ACTION_CONFIG_FINISH);
		filter.addAction(ICustomAction.ACTION_REQUEST_FOCUS);
		filter.addAction(ICustomAction.ACTION_GOSTORE_DESTORY);
		// filter.addAction(GoWidgetConstant.ACTION_ON_OFF_RECEIVER_CAN_UNINSTALL);
		mActivity.registerReceiver(mGoWidgetActionReceiver, filter);

	}

	private void unRegistGoWidgetAction() {
		if (mGoWidgetActionReceiver != null) {
			mActivity.unregisterReceiver(mGoWidgetActionReceiver);
			mGoWidgetActionReceiver = null;
		}
	}

	/**
	 * 处理数据改变
	 * 
	 * @param dataType
	 *            改变的数据类型
	 * @return 是否已处理
	 */
	private boolean handleAppCoreChange(int dataType, Object object) {
		boolean ret = false;
		switch (dataType) {
			case DataType.DATATYPE_DESKTOPSETING : {
				ret = true;
				mHandler.sendEmptyMessage(UPDATE_DESKTOP_SETTING);
				break;
			}

			case DataType.DATATYPE_EFFECTSETTING : {
				ret = true;
				mHandler.sendEmptyMessage(UPDATE_EFFECT_SETTING);
				break;
			}

			case DataType.DATATYPE_SCREENSETTING : {
				ret = true;
				mHandler.sendEmptyMessage(UPDATE_SCREEN_SETTING);
				break;
			}

			case DataType.DATATYPE_THEMESETTING : {
				ret = true;
				mHandler.sendEmptyMessage(UPDATE_THEME_SETTING);
				break;
			}

			case DataType.DATATYPE_APPDATA_REMOVE : {
				mHandler.sendMessage(mHandler.obtainMessage(REFRESH_UNINSTALL, object));
				break;
			}

			default :
				break;
		}
		return ret;
	}

	@Override
	public View getContentView() {
		return mLayout;
	}

	@Override
	public void onAdd() {
		super.onAdd();
		startListening();

		// 加载设置，初始化worksapce
		loadSetting();
		if (null != mScreenSettingInfo) {
			mShowIndicator = mScreenSettingInfo.mEnableIndicator;
		}
		initWrokspace();
	}

	@Override
	public void onRemove() {
		super.onRemove();
	}

	@Override
	public void onForeground() {
	}

	@Override
	public void onBackground() {
	}

	@Override
	public void onVisiable(int visibility) {
		super.onVisiable(visibility);
		if (visibility == View.VISIBLE) {
			mFrameManager.registKey(this);
		} else {
			mFrameManager.unRegistKey(this);
		}
	}

	private void loadSetting() {
		if (mControler == null) {
			return;
		}

		GoSettingControler settingControler = GOLauncherApp.getSettingControler();
		mScreenSettingInfo = settingControler.getScreenSettingInfo();
		mEffectSettingInfo = settingControler.getEffectSettingInfo();
		mDesktopSettingInfo = settingControler.getDesktopSettingInfo();
		mThemeSettingInfo = settingControler.getThemeSettingInfo();
	}

	// 接到后台消息后加载桌面
	private void loadScreen() {
		if (!mInitWorkspace) {
			loadScreenInfo();
			mHandler.sendEmptyMessage(START_DESKTOP_LOADER);
		}
	}

	private void initWrokspace() {
		mWorkspace.setFocusable(true);
		if (mDesktopSettingInfo != null) {
			mWorkspace.setmAutoStretch(mDesktopSettingInfo.mAutofit);
			mWorkspace.setDesktopRowAndCol(mDesktopSettingInfo.getRows(),
					mDesktopSettingInfo.getColumns());
		}

		handleEffectSettingChange(mEffectSettingInfo);
		handleThemeSettingChange(mThemeSettingInfo);

		// 先初始化的dock数据
		GoSettingControler.getInstance(mActivity).getShortCutSettingInfo();
		// dock条处于隐藏状态
		if (!ShortCutSettingInfo.sEnable) {
			mWorkspace.requestLayout(Workspace.CHANGE_SOURCE_DOCK, 0);
		}

		// 取屏幕个数
		int screenCount = mControler.getScreenCount();
		for (int i = 0; i < screenCount; i++) {
			CellLayout screen = (CellLayout) mInflater.inflate(R.layout.workspace_screen,
					mWorkspace, false);
			screen.setNeedToTryCatch(mNeedToCatch);
			mWorkspace.addView(screen);
		}

		// 屏幕设置
		if (mScreenSettingInfo != null) {
			mLayout.getIndicator().setVisible(mShowIndicator);
			if (mCurrentScreen >= 0) {
				mWorkspace.setCurrentScreen(mCurrentScreen);
			} else {
				mCurrentScreen = mScreenSettingInfo.mMainScreen;
				mWorkspace.setCurrentScreen(mCurrentScreen);
			}
			mWorkspace.setMainScreen(mScreenSettingInfo.mMainScreen);
			mWorkspace.setWallpaperScroll(mScreenSettingInfo.mWallpaperScroll);
			mWorkspace.setCycleMode(mScreenSettingInfo.mScreenLooping);
			mLayout.getIndicator().setAutoHide(mScreenSettingInfo.mAutoHideIndicator);
		}
		// 刷新格子状态列表
		mWorkspace.refreshSubView();
		mLayout.getIndicator().setTotal(mWorkspace.getChildCount());
		mLayout.getIndicator().setCurrent(mCurrentScreen);
		mLayout.postInvalidate();
	}

	/**
	 * 添加GOWidget
	 * 
	 * @param bundle
	 * @param screenindex
	 *            第几个屏幕
	 * @return
	 */
	private boolean addGoWidget(Bundle bundle, int screenindex, int param) {
		if (bundle == null) {
			return false;
		}

		boolean ret = false;
		GoWidgetBaseInfo info = new GoWidgetBaseInfo();
		info.mWidgetId = bundle.getInt(GoWidgetConstant.GOWIDGET_ID);
		info.mType = bundle.getInt(GoWidgetConstant.GOWIDGET_TYPE);
		info.mLayout = bundle.getString(GoWidgetConstant.GOWIDGET_LAYOUT);
		info.mTheme = bundle.getString(GoWidgetConstant.GOWIDGET_THEME);
		info.mThemeId = bundle.getInt(GoWidgetConstant.GOWIDGET_THEMEID, -1);
		info.mPrototype = bundle.getInt(GoWidgetConstant.GOWIDGET_PROTOTYPE,
				GoWidgetBaseInfo.PROTOTYPE_NORMAL);
		// 统计GO STORE为手动添加
		if (info.mPrototype == GoWidgetBaseInfo.PROTOTYPE_GOSTORE) {
			GoStoreStatisticsUtil.saveWidgetRecord(mActivity, info.mWidgetId + "",
					GoStoreStatisticsUtil.WIDGET_KEY_ADD_TYPE, "1");
			GoStoreStatisticsUtil.saveWidgetRecord(mActivity, info.mWidgetId + "",
					GoStoreStatisticsUtil.WIDGET_KEY_TYPE, info.mType + "");
		}
		GoWidgetManager widgetManager = AppCore.getInstance().getGoWidgetManager();
		// // 尝试找出其他内置的widget，如任务管理器widget

		AppWidgetProviderInfo providerInfo = bundle
				.getParcelable(GoWidgetConstant.GOWIDGET_PROVIDER);
		int minWidth = 0, minHeight = 0;
		if (providerInfo != null) {
			minWidth = providerInfo.minWidth;
			minHeight = providerInfo.minHeight;
			if (providerInfo.provider != null) {
				info.mPackage = providerInfo.provider.getPackageName();
			}
			if (providerInfo.configure != null) {
				info.mClassName = providerInfo.configure.getClassName();
			}
			// update by zhoujun 应用中心的widget和gostore的
			// widget包名一样，这里需要用mPrototype来区分他们
			// InnerWidgetInfo innerWidgetInfo =
			// widgetManager.getInnerWidgetInfo(info.mPackage);
			InnerWidgetInfo innerWidgetInfo = widgetManager.getInnerWidgetInfo(info.mPrototype);
			// update by zhoujun 2012-08-13 end
			// 内置
			if (innerWidgetInfo != null) {
				// 更新包名为实际inflate xml的包名
				info.mPackage = innerWidgetInfo.mInflatePkg;
				info.mPrototype = innerWidgetInfo.mPrototype;
			}
		}

		boolean add = widgetManager.addGoWidget(info);

		CellLayout cellLayout = mWorkspace.getScreenView(screenindex);
		if (cellLayout == null || !add) {
			return false;
		}

		// AbstractFrame topFrame = mFrameManager.getTopFrame();
		// final boolean fromScreenEditFrame = (topFrame != null &&
		// topFrame.getId() == IDiyFrameIds.SCREEN_EDIT_BOX_FRAME);
		// 修改是否在添加界面的判断
		boolean fromScreenEditFrame = false;
		if (Workspace.sLayoutScale < 1.0f) {
			fromScreenEditFrame = true;
		}

		int xy[] = new int[2];
		final int[] spans = cellLayout.rectToCell(minWidth, minHeight);
		boolean vacant = ScreenUtils.findVacant(xy, spans[0], spans[1], screenindex, mWorkspace);
		if (!vacant) {
			// ScreenUtils.showToast(R.string.no_more_room, mActivity);

			// 对所有屏幕进行计算，并保持有足够空间添加gowidget的屏幕下标
			ArrayList<Integer> enoughSpaceIndexList = new ArrayList<Integer>();
			// 如果是用屏幕编辑进来的话，最后一个是虚拟屏，不需要包括在内，所以要-1
			final int count = fromScreenEditFrame ? (mWorkspace.getChildCount() - 1) : mWorkspace
					.getChildCount();
			boolean hasEnoughSpace;
			for (int i = 0; i < count; i++) {
				cellLayout = mWorkspace.getScreenView(i);
				int[] cell = cellLayout.rectToCell(minWidth, minHeight);
				hasEnoughSpace = ScreenUtils.findVacant(xy, cell[0], cell[1], i, mWorkspace);
				if (hasEnoughSpace) {
					enoughSpaceIndexList.add(i);
				}
			}
			if (fromScreenEditFrame) {
				setScreenRedBg();
			}

		} else {
			View widgetView = widgetManager.createView(info.mWidgetId);
			if (widgetView == null) {
				ScreenUtils.showToast(R.string.add_widget_failed, mActivity);
			} else {
				ScreenAppWidgetInfo appWidgetInfo = new ScreenAppWidgetInfo(info.mWidgetId);
				widgetView.setTag(appWidgetInfo);
				appWidgetInfo.mCellX = xy[0];
				appWidgetInfo.mCellY = xy[1];
				appWidgetInfo.mSpanX = spans[0];
				appWidgetInfo.mSpanY = spans[1];
				// TODO:传入选择屏的id
				mWorkspace.addInScreen(widgetView, screenindex, xy[0], xy[1], spans[0], spans[1],
						false);
				widgetManager.startWidget(info.mWidgetId, bundle);
				addDesktopItem(screenindex, appWidgetInfo);
				ret = true;
			}
		}

		if (!ret) {
			widgetManager.deleteWidget(info.mWidgetId);
		}

		return ret;
	}

	/**
	 * 判断当前widget是否能成功添加至桌面
	 * 
	 * @param bundle
	 * @param screenindex
	 * @return
	 */
	private boolean addGoWidgetByCinfig(Bundle bundle, int screenindex) {
		if (bundle == null) {
			return false;
		}
		AppWidgetProviderInfo providerInfo = bundle
				.getParcelable(GoWidgetConstant.GOWIDGET_PROVIDER);
		int minWidthByCinfig = 0, minHeightByCinfig = 0;
		if (providerInfo != null) {
			minWidthByCinfig = providerInfo.minWidth;
			minHeightByCinfig = providerInfo.minHeight;
		}

		CellLayout cellLayout = mWorkspace.getScreenView(screenindex);
		if (cellLayout == null) {
			return false;
		}
		int xy[] = new int[2];
		final int[] spans = cellLayout.rectToCell(minWidthByCinfig, minHeightByCinfig);
		boolean vacant = ScreenUtils.findVacant(xy, spans[0], spans[1], screenindex, mWorkspace);
		return vacant;
	}

	private boolean applyGoWidgetTheme(int widgetId, Bundle bundle) {
		if (bundle == null) {
			return false;
		}

		AppCore.getInstance().getGoWidgetManager().applyWidgetTheme(widgetId, bundle);
		return true;
	}

	private boolean addAppWidget(int widgetId) {
		boolean ret = false;
		int xy[] = new int[2];
		AppWidgetProviderInfo info = mWidgetManager.getAppWidgetInfo(widgetId);
		CellLayout cellLayout = mWorkspace.getCurrentScreenView();

		if (cellLayout == null || info == null) {
			ScreenUtils.showToast(R.string.add_widget_failed, mActivity);
		} else // 合法
		{
			final int[] spans = cellLayout.rectToCell(info.minWidth, info.minHeight);
			boolean vacant = ScreenUtils.findVacant(xy, spans[0], spans[1],
					mWorkspace.getCurrentScreen(), mWorkspace);

			if (!vacant) {
				setScreenRedBg();
			} else {
				OutOfMemoryHandler.gcIfAllocateOutOfHeapSize();
				boolean getHostView = true;
				AppWidgetHostView widgetView = null;
				try {
					widgetView = mWidgetHost.createView(mActivity, widgetId, info);
				} catch (OutOfMemoryError e) {
					OutOfMemoryHandler.handle();
					getHostView = false;
				} catch (Throwable e) {
					Log.i(LOG_TAG, "add widget Exception:" + e.toString());
					getHostView = false;
				}

				if (widgetView == null || !getHostView) {
					ScreenUtils.showToast(R.string.add_widget_failed, mActivity);
				} else {
					ScreenAppWidgetInfo appWidgetInfo = new ScreenAppWidgetInfo(widgetId,
							info.provider);
					appWidgetInfo.mHostView = widgetView;

					widgetView.setTag(appWidgetInfo);
					appWidgetInfo.mCellX = xy[0];
					appWidgetInfo.mCellY = xy[1];
					appWidgetInfo.mSpanX = spans[0];
					appWidgetInfo.mSpanY = spans[1];
					mWorkspace.addInCurrentScreen(widgetView, xy[0], xy[1], spans[0], spans[1],
							false);

					// 调整屏幕位置防止添加widget横竖屏切换位置偏移
					// mWorkspace.changeOrientation(true);

					addDesktopItem(mWorkspace.getCurrentScreen(), appWidgetInfo);

					// broadcast add widget
					ScreenUtils.appwidgetReadyBroadcast(widgetId, info.provider, spans, mActivity);

					ret = true;
				}
			}
		}

		if (!ret) // 添加失败需要删除已经分配的widgetid
		{
			mWidgetHost.deleteAppWidgetId(widgetId);
		}
		return ret;
	}

	// 显示屏幕预览
	private boolean showPreview(boolean fromSetting) {

		if (!mWorkspace.getScreenScroller().isFinished()) {
			return false;
		}
		mWorkspace.mDragging = false;

		// 隐藏指示器
		hideIndicator();
		mWorkspace.setCellLayoutGridState(mWorkspace.getCurrentScreen(), false,
				CellLayout.DRAW_STATUS_NORMAL);
		// widget离开通知
		fireWidgetVisible(false, mWorkspace.getCurrentScreen());

		// 隐藏菜单
		hideQuickActionMenu(false);
		// 展示预览
		mIsShowPreview = true;

		int param = isLoading()
				? SensePreviewFrame.SCREEN_LOADING
				: SensePreviewFrame.SCREEN_LOADED;
		if (fromSetting) {
			param = SensePreviewFrame.FROM_SETTING | param;
		}

		boolean ret = false;
		GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IFrameworkMsgId.SHOW_FRAME,
				IDiyFrameIds.SCREEN_PREVIEW_FRAME, null, null);
		// 清除焦点
		mWorkspace.clearFocus();
		final int screenCount = mWorkspace.getChildCount();
		ScreenPreviewMsgBean bean = new ScreenPreviewMsgBean();
		bean.currentScreenId = mWorkspace.getCurrentScreen();
		bean.mainScreenId = mWorkspace.getMainScreen();
		for (int i = 0; i < screenCount; i++) {
			ScreenPreviewMsgBean.PreviewImg image = new ScreenPreviewMsgBean.PreviewImg();
			image.previewView = mWorkspace.getChildAt(i);
			((CellLayout) image.previewView).setChildrenDrawnWithCacheEnabled(false);
			((CellLayout) image.previewView).destroyChildrenDrawingCache();
			image.screenId = i;
			image.canDelete = !mWorkspace.hasChildElement(i);
			bean.screenPreviewList.add(image);
		}
		if (mDragView != null) {
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
					IDiyMsgIds.SENDVIEWTOPREVIEW, -1, mDragView, null);
		}
		mDragView = null;
		ret = GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
				IDiyMsgIds.PREVIEW_INIT, param, bean, null);

		if (fromSetting) {
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
					IDiyMsgIds.PREVIEW_MAKE_TIP, -1, null, null);
		}
		return ret;
	}

	// 隐藏指示器
	private void hideIndicator() {
		if (mShowIndicator) {
			mEnterPrew = true;
			mLayout.getIndicator().hide();
		}
	}

	// 启动程序
	private boolean launchApp(View view) {
		boolean ret = false;
		Object tag = view.getTag();
		if (tag != null && tag instanceof ShortCutInfo) {
			ShortCutInfo shortcut = (ShortCutInfo) tag;
			Rect rect = new Rect();
			view.getGlobalVisibleRect(rect);

			ArrayList<Object> posArrayList = new ArrayList<Object>();

			posArrayList.add(rect);
			posArrayList.add(mStartActivityResult);

			Intent intent = DockUtil.filterDockBrowserIntent(mActivity, shortcut.mItemType,
					shortcut.mIntent);
			ret = GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
					IDiyMsgIds.START_ACTIVITY, -1, intent, posArrayList);
			if (intent != null) {
				final String action = intent.getAction();
				if (action != null && action.equals(ICustomAction.ACTION_FUNC_SPECIAL_APP_GOSTORE)) {
					StatisticsData.countStatData(mActivity, StatisticsData.ENTRY_KEY_GOFOLDER);
//					GoStoreStatisticsUtil.setCurrentEntry(
//							GoStoreStatisticsUtil.ENTRY_TYPE_FUNTAB_ICON, mActivity);
					AppRecommendedStatisticsUtil.getInstance().saveCurrentEnter(mActivity, 
							AppRecommendedStatisticsUtil.ENTRY_TYPE_APPFUNC_ICO_GOSTORE);
				}
			} // end if shortcut.mIntent
			posArrayList.clear();
			posArrayList = null;
		} else if (tag instanceof ScreenFolderInfo) {

			AbstractFrame folderFrame = GoLauncher.getFrame(IDiyFrameIds.DESK_USER_FOLDER_FRAME);
			boolean isShowed = folderFrame != null && folderFrame.getVisibility() == View.VISIBLE;
			if (isShowed) {
				return false;
			}

			view.setPressed(false);

			if (view instanceof FolderIcon) {
				hideViewsNameWhenOpenFolder(view);
			}
			handleFolderClick((ScreenFolderInfo) tag);
			mStartActivityResult[0] = true;

			// cellayout folder打开动画
			int bottom = view.getBottom();
			if (view instanceof BubbleTextView) {
				BubbleTextView bubbleTextView = (BubbleTextView) view;
				bottom = view.getTop() + (int) bubbleTextView.getTextRectTopLine();
			}
			Rect rect = new Rect(view.getLeft(), view.getTop(), view.getRight(), bottom);
			int arrowdirection = FolderRelativeLayout.ARROW_DIRECTION_UP;
			int folderBottomMargin = mActivity.getResources().getDimensionPixelSize(
					R.dimen.folder_margin_botton);
			if (bottom >= GoLauncher.getDisplayHeight() - folderBottomMargin) {
				arrowdirection = FolderRelativeLayout.ARROW_DIRECTION_DOWN;
			}
			startOpenFolderLayout(view, rect, arrowdirection);
			ret = true;

			// 用户行为统计--文件夹打开。
			StatisticsData.countUserActionData(StatisticsData.DESK_ACTION_ID_CLICK_FLODER,
					StatisticsData.USER_ACTION_DEFAULT, IPreferencesIds.DESK_ACTION_DATA);
		} else if (tag instanceof FavoriteInfo) {
			FavoriteInfo info = (FavoriteInfo) tag;
			if (info.mWidgetInfo != null) {
				final GoWidgetManager widgetManager = AppCore.getInstance().getGoWidgetManager();
				String packageName = widgetManager.getWidgetPackage(info.mWidgetInfo);
				if (packageName != null) {
					// 推荐widget下载 直接进入store,跳转到详情界面
					// GoStoreOperatorUtil.gotoStoreDetailDirectly(mActivity,
					// packageName);

					if (packageName.equals("com.gau.go.launcherex.gowidget.taskmanager")) {
						packageName = LauncherEnv.Plugin.RECOMMAND_GOTASKMANAGER_PACKAGE;
					}

					// (旧版本)推荐widget下载 通过电子市场 或者通过浏览器下载
					String linkArray[] = { packageName, info.mUrl };
					String title = packageName; // 默认使用包名做为文件名
					if (info.mTitleId > 0) {
						title = mActivity.getString(info.mTitleId);
					}

					boolean isCnUser = Machine.isCnUser(mActivity);
					String content = /*
										* isCnUser ? mActivity
										* .getString(R.string.fav_content_cn) :
										*/mActivity.getString(R.string.fav_content);
					// CheckApplication.showDownloadDirectlyTip(mActivity,
					// title, content,
					// linkArray, isCnUser, info.mTitleId);
					CheckApplication.downloadAppFromMarketFTPGostore(mActivity, content, linkArray,
							LauncherEnv.GOLAUNCHER_FORWIDGET_GOOGLE_REFERRAL_LINK, title,
							System.currentTimeMillis(), isCnUser,
							CheckApplication.FROM_SCREEN_FAVORITE_WIDGET);

					linkArray = null;
					title = null;
					content = null;
				}
			}
		}
		return ret;
	}

	/**
	 * 打开文件夹前，对文件夹图标及同行图标隐藏名字
	 * 
	 * @param view
	 */
	private void hideViewsNameWhenOpenFolder(View view) {
		if (null == view || null == view.getLayoutParams() || null == getCurrentScreen()
				|| !mDesktopSettingInfo.isShowTitle()) {
			return;
		}

		CellLayout.LayoutParams lp = (CellLayout.LayoutParams) view.getLayoutParams();
		int cellY = lp.cellY;
		CellLayout cellLayout = getCurrentScreen();
		int count = cellLayout.getChildCount();
		for (int i = 0; i < count; i++) {
			View child = cellLayout.getChildAt(i);
			if (null != child && child instanceof BubbleTextView) {
				CellLayout.LayoutParams lpChild = (CellLayout.LayoutParams) child.getLayoutParams();
				if (lpChild.cellY == cellY) {
					ScreenUtils.setBubbleTextTitle(false, (BubbleTextView) child);
				}
			}
		}
	}

		/**
		 * 关闭文件夹后，对文件夹图标及同行图标恢复显示名字
		 */
		private void showViewsNameWhenCloseFolder() {
			if (!mDesktopSettingInfo.isShowTitle() || null == getCurrentScreen()) {
				return;
			}
	
			CellLayout cellLayout = getCurrentScreen();
			int count = cellLayout.getChildCount();
			for (int i = 0; i < count; i++) {
				View child = cellLayout.getChildAt(i);
				if (null != child && child instanceof BubbleTextView) {
					BubbleTextView bubbleTextView = (BubbleTextView) child;
					if (null == bubbleTextView.getText()) {
						ScreenUtils.setBubbleTextTitle(true, bubbleTextView);
					}
				}
			}
		}
	

	private void clearDragState() {
		// 移动取消事件
		mWorkspace.clearDragState();
		// mDragFromFolder = false;
		mDragType = DragFrame.TYPE_SCREEN_ITEM_DRAG;
		// mCurrentFolderInfo = null;
		clearVisualize();

	}

	private void clearVisualize() {
		for (int i = 0; i < mWorkspace.getChildCount(); i++)// 清除所有屏幕中可能存在的格子
		{
			View view = mWorkspace.getChildAt(i);
			if (view != null && view instanceof CellLayout) {
				((CellLayout) view).clearVisualizeDropLocation();
				((CellLayout) view).revertTempState();
			}
		}
	}

	// 结束拖拽
	private boolean dragOver(int screenId, Object object, List<?> objects) {
		boolean ret = false;
		if (objects != null && objects.size() > 0 && objects.get(0) instanceof Rect) {
			// 最后放置的位置
			Rect rect = (Rect) objects.get(0);
			mRectTemp = rect;
			/*change by jiangchao 
			移到下面防止内存泄漏. 原因:mViewTemp变量只会在文件夹合并时需要使用,其他业务不需要赋值.
			mViewTemp = (View) object;*/
			int screenIndex = screenId;
			boolean dropAnimate = false;
			if (screenId == -1) {
				// 移动到当前屏幕
				screenIndex = mWorkspace.getCurrentScreen();
				dropAnimate = true;
			}
			Object tagObject = ((View) object).getTag();
			if (tagObject == null) {
				return false;
			}
			final int destX = rect.left;
			final int destY = rect.top;
			// if(mDragType == DragFrame.TYPE_ADD_APP_DRAG){
			// if(!mWorkspace.isOverWorkspace(realX, realY)){
			// return false;
			// }
			// }

			// //发消息到添加层,添加widget至桌面
			// if(Workspace.getLayoutScale() < 1.0 && tagObject instanceof
			// AbsWidgetInfo){
			// GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_EDIT_BOX_FRAME,
			// IDiyMsgIds.SCREEN_EDIT_ADD_GOWIDGET_TO_SCREEN,
			// ((AbsWidgetInfo)tagObject).mAddIndex, null, null);
			// return true;
			// }
			ItemInfo tagInfo = null;
			if (tagObject instanceof ItemInfo) {
				tagInfo = (ItemInfo) tagObject;
			}

			// 是否需要刷新桌面的标识，防止在功能表里面拖拽出来的图标没有刷新
			boolean requestLayout = false;
			final boolean isOpenLiveFolder = mWorkspace.getOpenFolder() != null ? true : false;
			if (!isOpenLiveFolder && isMoveToFolder((View) object, rect)) {
				clearDragState();
				if (null != getCurrentScreen()) {
					getCurrentScreen().setmDrawStatus(CellLayout.DRAW_STATUS_INTO_FOLDER_ZOOMOUT);
				}
				ret = true;
				// 用户行为统计,移动图标到文件夹。
				if (Workspace.getLayoutScale() < 1) {
					StatisticsData.countUserActionData(StatisticsData.DESK_ACTION_ID_SCREEN_EDIT,
							StatisticsData.USER_ACTION_THREE, IPreferencesIds.DESK_ACTION_DATA);
				}
			} else if (!isOpenLiveFolder && isMergeFolder((View) object, screenIndex)) {
				// final boolean doComplete = mProgressDialog == null ||
				// !mProgressDialog.isShowing();
				// mViewTemp只需要在合并文件夹时赋值.
				mViewTemp = (View) object;
				
				final boolean doComplete = mGoProgressBar == null
						|| (mGoProgressBar.getVisibility() == View.INVISIBLE);
				if (doComplete) {
					completeMergeFolder((View) object, rect);
				}
				requestLayout = true;
				ret = true;
				// 用户行为统计，新建文件夹。
				if (Workspace.getLayoutScale() < 1) {
					StatisticsData.countUserActionData(StatisticsData.DESK_ACTION_ID_SCREEN_EDIT,
							StatisticsData.USER_ACTION_TWO, IPreferencesIds.DESK_ACTION_DATA);
				}
			} else if (tagInfo != null
					&& (DragFrame.TYPE_SCREEN_ITEM_DRAG != mDragType || tagInfo.mInScreenId == 0 || tagInfo.mInScreenId == -1)) // 如果是从文件夹中拖出来的图标
																																// ,从DOCK条,DOCK条文件夹或从功能表拖进来
			{

				// int[] xy = mWorkspace.estimateDropCell(realX, realY,
				// tagInfo.mSpanX,
				// tagInfo.mSpanY, null, screenIndex, null);
				int centerX = rect.centerX();
				int centerY = rect.centerY();
				if (Workspace.getLayoutScale() < 1.0) {
					float[] realXY = new float[2];
					// 先转换为真实值
					Workspace.virtualPointToReal(centerX, centerY, realXY);
					centerX = (int) realXY[0];
					centerY = (int) realXY[1];
				}
				int[] xy = mWorkspace.getDropCell(centerX, centerY, tagInfo.mSpanX, tagInfo.mSpanY,
						null, screenIndex, null);

				if (xy == null || xy[0] < 0 || xy[1] < 0) {
					if (isOpenLiveFolder) {
						showMoveToLiveFolderToast();
					} else {
						setScreenRedBg();
					}
					if (DragFrame.TYPE_DOCK_FOLDERITEM_DRAG == mDragType
							|| DragFrame.TYPE_DOCK_DRAG == mDragType) {
						GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME,
								IDiyMsgIds.DRAG_CANCEL, mDragType, object, null);
					}
				} else {
					if (mCurrentFolderInfo != null) {
						if (null != mControler) {
							mControler.moveDesktopItemFromFolder(tagInfo, screenIndex,
									mCurrentFolderInfo.mInScreenId);
							GoLauncher.sendMessage(this, IDiyFrameIds.DESK_USER_FOLDER_FRAME,
									IDiyMsgIds.DELETE_CACHE_INFO_IN_FOLDER, -1, tagInfo, null);
						}

						// 更新缓存
						if (mCurrentFolderInfo instanceof UserFolderInfo) {
							((UserFolderInfo) mCurrentFolderInfo).remove(tagInfo.mInScreenId);
							if (!deleteFolderOrNot((UserFolderInfo) mCurrentFolderInfo, true)) {
								// if (tagInfo instanceof ShortCutInfo) {
								// int type =
								// AppIdentifier.whichTypeOfNotification(mActivity,
								// (( ShortCutInfo ) tagInfo).mIntent);
								// if (type !=
								// NotificationType.IS_NOT_NOTIFICSTION) {
								// (( UserFolderInfo )
								// mCurrentFolderInfo).mTotleUnreadCount -= ((
								// ShortCutInfo ) tagInfo).mCounter;
								// }
								// }
								// 更新文件夹图标
								updateFolderIconAsync((UserFolderInfo) mCurrentFolderInfo, false,
										false);
							}
						}
						mCurrentFolderInfo = null;
					}
					if (tagInfo instanceof ShortCutInfo) {
						Log.i(LogConstants.HEART_TAG, "drag over app");
						mWorkspace.blankCellToNormal(mWorkspace.getCurrentScreenView());
						// 应用程序图标
						tagInfo.mCellX = xy[0];
						tagInfo.mCellY = xy[1];
						addDesktopItem(screenIndex, tagInfo);
						addDesktopView(tagInfo, screenIndex, true);
						if (mDragType != DragFrame.TYPE_DOCK_DRAG) {
							requestLayout = true;
						}
					} else if (tagInfo instanceof UserFolderInfo) {
						if (mDragType == DragFrame.TYPE_DOCK_DRAG) {
							tagInfo.mCellX = xy[0];
							tagInfo.mCellY = xy[1];
							addDesktopItem(screenIndex, tagInfo);
							addDesktopView(tagInfo, screenIndex, true);
						} else {
							dragFolderFormAllApps((UserFolderInfo) tagInfo, xy, screenIndex);
							requestLayout = true;
						}
					}
					ret = true;
				}
			}
			// NOTE:这里加入if(null !=
			// tagInfo)判断是因为2.39报过一个tagInfo=null的空指针，原因不明，加保护，这种异常情况下的结果是拖动不成功
			else if (null != tagInfo && !mDontMoveDragObject) {
				int[] xy = new int[2];
				boolean[] move = new boolean[1];
				final float scale = Math.max(1, rect.width() / ((View) object).getWidth());
				boolean isDropSuccess = mWorkspace.drop(destX, destY, xy, screenIndex, move, scale,
						dropAnimate);

				if (!isDropSuccess) {
					if (isOpenLiveFolder) {
						showMoveToLiveFolderToast();
					} else {
						setScreenRedBg();
					}
				} else if (move[0]) {
					// hack for hide quickactionmenu by luopeihuan
					hideQuickActionMenu(false);
					tagInfo.mCellX = xy[0];
					tagInfo.mCellY = xy[1];
					updateDesktopItem(screenIndex, tagInfo);
					ret = true;
				}
			}
			if (requestLayout) {
				mWorkspace.requestLayout();
			}

			clearDragState();
			if (null != getCurrentScreen()) {
				getCurrentScreen().setStatusNormal();
			}
		}
		if (!ret && object != null && object instanceof View && mIsNeedQuickActionMenu && mDragType == DragFrame.TYPE_SCREEN_ITEM_DRAG && Workspace.sLayoutScale >= 1.0f) {
			// 显示弹出菜单
			showQuickActionMenu((View) object);
		}
		return ret;
	}

	private void flyIconToScreen(Object object, List<?> objects) {
		if (objects != null && objects.size() > 0 && objects.get(0) instanceof Rect) {
			// 最后放置的位置
			mViewTemp = (View) object;
			int screenIndex = mWorkspace.getCurrentScreen();
			Object tagObject = ((View) object).getTag();
			if (tagObject == null) {
				return;
			}
			if (mRectTemp == null) {
				Rect rect = (Rect) objects.get(0);
				mRectTemp = rect;
			}
			int realX = mRectTemp.left;
			int realY = mRectTemp.top;
			mRectTemp = null;
			if (Workspace.getLayoutScale() < 1.0) {
				float[] realXY = new float[2];
				// 先转换为真实值
				Workspace.virtualPointToReal(realX, realY, realXY);
				realX = (int) realXY[0];
				realY = (int) realXY[1];
			}
			if (mDragType == DragFrame.TYPE_ADD_APP_DRAG) {
				if (!mWorkspace.isOverWorkspace(realX, realY)) {
					return;
				}
			}
			// 发消息到添加层,添加widget至桌面
			if (tagObject instanceof AbsWidgetInfo) {
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_EDIT_BOX_FRAME,
						IDiyMsgIds.SCREEN_EDIT_ADD_GOWIDGET_TO_SCREEN,
						((AbsWidgetInfo) tagObject).mAddIndex, null, null);
				return;
			}

			ItemInfo tagInfo = null;
			if (tagObject instanceof ItemInfo) {
				tagInfo = (ItemInfo) tagObject;
			}

			if (tagInfo == null) {
				return;
			}
			// change 位置不需要再次计算
			// int[] xy = mWorkspace.estimateDropCell(realX, realY,
			// tagInfo.mSpanX, tagInfo.mSpanY,
			// null, screenIndex, null);
			if (mCellPos == null) {
				setScreenRedBg();
			} else {
				if (mCurrentFolderInfo != null) {
					if (null != mControler) {
						mControler.moveDesktopItemFromFolder(tagInfo, screenIndex,
								mCurrentFolderInfo.mInScreenId);
						GoLauncher.sendMessage(this, IDiyFrameIds.DESK_USER_FOLDER_FRAME,
								IDiyMsgIds.DELETE_CACHE_INFO_IN_FOLDER, -1, tagInfo, null);
					}
					// 更新缓存
					if (mCurrentFolderInfo instanceof UserFolderInfo) {
						((UserFolderInfo) mCurrentFolderInfo).remove(tagInfo.mInScreenId);
						if (!deleteFolderOrNot((UserFolderInfo) mCurrentFolderInfo, true)) {
							// if (tagInfo instanceof ShortCutInfo) {
							// int type =
							// AppIdentifier.whichTypeOfNotification(mActivity,
							// (( ShortCutInfo ) tagInfo).mIntent);
							// if (type != NotificationType.IS_NOT_NOTIFICSTION)
							// {
							// (( UserFolderInfo )
							// mCurrentFolderInfo).mTotleUnreadCount -= ((
							// ShortCutInfo ) tagInfo).mCounter;
							// }
							// }
							// 更新文件夹图标
							updateFolderIconAsync((UserFolderInfo) mCurrentFolderInfo, false, false);
						}
					}
					mCurrentFolderInfo = null;
				}
				if (tagInfo instanceof ShortCutInfo) {
					Log.i(LogConstants.HEART_TAG, "drag over app");
					mWorkspace.blankCellToNormal(mWorkspace.getCurrentScreenView());
					// 应用程序图标
					tagInfo.mCellX = mCellPos[0];
					tagInfo.mCellY = mCellPos[1];
					addDesktopItem(screenIndex, tagInfo);
					addDesktopView(tagInfo, screenIndex, true);
				}
			}
			mCellPos = null;
			clearDragState();
			if (null != getCurrentScreen()) {
				getCurrentScreen().setStatusNormal();
			}
		}
	}

	/**
	 * 判断结束拖动时是否合并文件夹
	 * 
	 * @return
	 */
	private boolean isMergeFolder(View dragView, int screenIndex) {
		final CellLayout currentCellLayout = getCurrentScreen();
		if (null != currentCellLayout
				&& mWorkspace.getDragMode() == Workspace.DRAG_MODE_CREATE_FOLDER/*
																				 * &&
																				 * currentCellLayout
																				 * .
																				 * getDrawStatus
																				 * (
																				 * )
																				 * ==
																				 * CellLayout
																				 * .
																				 * DRAW_STATUS_MERGE_FOLDER
																				 */) {
			// 如果当前屏绘制状态是DRAW_STATUS_MERGE_FOLDER，就判定为合并文件夹
			// 合并文件夹操作
			// View target = currentCellLayout.getmMergerFolderChildView();
			View target = currentCellLayout.getChildViewByCell(mWorkspace.mTargetCell);
			View source = dragView;
			if (target == null || source == null) {
				return false;
			}

			ItemInfo targetInfo = (ItemInfo) target.getTag();
			ItemInfo sourceInfo = (ItemInfo) source.getTag();
			if (targetInfo == null || sourceInfo == null) {
				return false;
			}
			if (screenIndex != targetInfo.mScreenIndex) {
				return false;
			}

			if (targetInfo instanceof ShortCutInfo && sourceInfo instanceof ShortCutInfo) {
				//判断是否是无效图标（如：名称为加载中的图标）
				Intent it = ((ShortCutInfo) sourceInfo).mIntent;
				if (it == null) {
					return false;
				}
				//文件夹智能命名 封装list
				ArrayList<AppItemInfo> infoList = new ArrayList<AppItemInfo>();
				infoList.add(((ShortCutInfo) sourceInfo).getRelativeItemInfo());
				infoList.add(((ShortCutInfo) targetInfo).getRelativeItemInfo());
				// 添加文件夹
				UserFolderInfo folderInfo = new UserFolderInfo();
				folderInfo.mTitle = mActivity.getText(R.string.folder_name);
				folderInfo.mInScreenId = System.currentTimeMillis();
				folderInfo.mCellX = targetInfo.mCellX;
				folderInfo.mCellY = targetInfo.mCellY;
				addDesktopItem(mWorkspace.getCurrentScreen(), folderInfo);

				if (mDragType == DragFrame.TYPE_SCREEN_FOLDER_DRAG) {
					// 从文件夹拖出來的，还要删除文件夹内的图标
					if (mCurrentFolderInfo != null) {
						if (null != mControler) {
							mControler.moveDesktopItemFromFolder(sourceInfo,
									mWorkspace.getCurrentScreen(), mCurrentFolderInfo.mInScreenId);
							mControler.addItemInfoToFolder(sourceInfo, folderInfo.mInScreenId);
							GoLauncher.sendMessage(this, IDiyFrameIds.DESK_USER_FOLDER_FRAME,
									IDiyMsgIds.DELETE_CACHE_INFO_IN_FOLDER, -1, sourceInfo, null);
						}

						// 更新缓存
						if (mCurrentFolderInfo instanceof UserFolderInfo) {
							((UserFolderInfo) mCurrentFolderInfo).remove(sourceInfo.mInScreenId);
							if (!deleteFolderOrNot((UserFolderInfo) mCurrentFolderInfo, true)) {
								// if (sourceInfo instanceof ShortCutInfo) {
								// int type =
								// AppIdentifier.whichTypeOfNotification(mActivity,
								// (( ShortCutInfo ) sourceInfo).mIntent);
								// if (type !=
								// NotificationType.IS_NOT_NOTIFICSTION) {
								// (( UserFolderInfo )
								// mCurrentFolderInfo).mTotleUnreadCount -= ((
								// ShortCutInfo ) sourceInfo).mCounter;
								// }
								// }

								// 更新文件夹图标
								updateFolderIconAsync((UserFolderInfo) mCurrentFolderInfo, false,
										false);
							}
						}
						mCurrentFolderInfo = null;
					}
				} else {
					// 删除桌面项
					if (mDragType == DragFrame.TYPE_DOCK_DRAG && sourceInfo.mInScreenId == 0) {
						sourceInfo.mInScreenId = System.currentTimeMillis();
					}
					mControler.moveDesktopItemToFolder(sourceInfo, folderInfo.mInScreenId);

					// 删除ＵＩ
					ScreenUtils.removeViewByItemInfo(sourceInfo, mWorkspace);
				}
				//add by:zzf 相同程序合并时，过滤掉一个
				if (((ShortCutInfo) targetInfo).mIntent != null
						&& ((ShortCutInfo) sourceInfo).mIntent != null) {
					boolean equal = false;
					// 通过componentName比较，不用转String
					equal = ConvertUtils.intentCompare(((ShortCutInfo) targetInfo).mIntent,
							((ShortCutInfo) sourceInfo).mIntent);

					if (!equal) {
						//如果不同，进行移动操作
						mControler.moveDesktopItemToFolder(targetInfo, folderInfo.mInScreenId);
					} else {
						//删除桌面程序DB信息 
						mControler.	removeDesktopItemInDBAndCache(targetInfo);
					}
					//移除View
					ScreenUtils.removeViewByItemInfo(targetInfo, mWorkspace);
				}
				// 增加ＵＩ
				FolderIcon newFolder = FolderIcon.fromXml(getFolderIconId(), mActivity,
						mWorkspace.getCurrentScreenView(), folderInfo,
						getDisplayTitle(folderInfo.mTitle));
				newFolder.close();
				// customDeskTopBackground(newFolder);
				newFolder.setShowShadow(getShadowState());
				mWorkspace
						.addInCurrentScreen(newFolder, folderInfo.mCellX, folderInfo.mCellY, 1, 1);

				// 刷新文件夹内容
				refreshFolderItems(folderInfo.mInScreenId, false);

				mMergeFordleIconToOpen = newFolder;
				
				folderInfo.mIsFirstCreate = true;
				// 文件夹智能命名-场景：图标重叠创建文件夹
				String smartfoldername = CommonControler.getInstance(mActivity)
						.generateFolderName(infoList);
				actionRename(smartfoldername, folderInfo.mInScreenId);
				
				return true;
			} else if (targetInfo instanceof UserFolderInfo && sourceInfo instanceof UserFolderInfo) {
				// 显示转圈圈
				showProgressDialog();
				// 以拖拽目标作为新文件夹的壳子
				final UserFolderInfo newFolderInfo = (UserFolderInfo) targetInfo;
				// 被拖拽的文件夹将被删除
				final UserFolderInfo delFolderInfo = (UserFolderInfo) sourceInfo;
				// 如果是屏幕的拖拽，先删除屏幕上的文件夹
				if (mDragType == DragFrame.TYPE_SCREEN_ITEM_DRAG) {
					ScreenUtils.removeViewByItemInfo(delFolderInfo, mWorkspace);
				}
				//文件夹智能命名-场景：文件夹合并创建文件夹
				ArrayList<AppItemInfo> infoList = new ArrayList<AppItemInfo>();
				String defaultname = mActivity.getResources().getString(
						R.string.folder_name);
				//未命名文件夹才参与智能命名
				if (newFolderInfo.mTitle.equals(defaultname)) {
					for (int i = 0; i < newFolderInfo.getChildCount(); i++) {
						ShortCutInfo itemInfo = newFolderInfo.getChildInfo(i);
						if (itemInfo != null) {
							infoList.add(itemInfo.getRelativeItemInfo());
						}
					}
					for (int i = 0; i < delFolderInfo.getChildCount(); i++) {
						ShortCutInfo itemInfo = delFolderInfo.getChildInfo(i);
						if (itemInfo != null) {
							infoList.add(itemInfo.getRelativeItemInfo());
						}
					}
					String smartfoldername = CommonControler.getInstance(
							mActivity).generateFolderName(infoList);
					actionRename(smartfoldername, targetInfo.mInScreenId);
				}

				new Thread(ThreadName.SCREEN_FOLDER_MERGING) {
					@Override
					public void run() {
						final int count = delFolderInfo.getChildCount();
						ArrayList<ItemInfo> items = new ArrayList<ItemInfo>();
						for (int i = 0; i < count; i++) {
							ShortCutInfo itemInfo = delFolderInfo.getChildInfo(i);
							if (itemInfo != null) {
								items.add(itemInfo);
								// 图标去重
								mControler.removeItemsFromFolder(newFolderInfo, itemInfo);
							}
						}
						final boolean dragFromAppfunc = mDragType == DragFrame.TYPE_APPFUNC_FOLDERITEM_DRAG
								? true
								: false;
						addUserFolderContent(newFolderInfo.mInScreenId, newFolderInfo, items,
								dragFromAppfunc);
						newFolderInfo.addAll(items);
						newFolderInfo.mTotleUnreadCount += delFolderInfo.mTotleUnreadCount;
						// refreshFolderItems(newFolderInfo.mInScreenId, false);

						if (mDragType == DragFrame.TYPE_SCREEN_ITEM_DRAG) {
							mControler.removeUserFolder(delFolderInfo);
							// 删除文件夹
							ScreenUtils.unbindeUserFolder(delFolderInfo);
						}
						// 合并完成，通知关闭进度条
						Message message = mHandler.obtainMessage();
						message.what = FINISH_MERGING;
						message.obj = new long[] { newFolderInfo.mInScreenId };
						mHandler.sendMessage(message);
					};
				}.start();
				return true;

			}// end else if
		}
		return false;
	}

	/**
	 * 
	 * 判断是否删除文件夹
	 * 
	 * @param userFolderInfo
	 * @param deleteOne
	 *            //文件夹中剩一个图标时是否删除文件夹
	 * @return
	 */
	private boolean deleteFolderOrNot(UserFolderInfo userFolderInfo, boolean deleteOne) {
		if (null == userFolderInfo) {
			return false;
		}

		boolean ret = false;
		int count = 0;
		synchronized (userFolderInfo) {
			/**
			 * ADT-3723 非必现：桌面文件夹消失，有消失动画 步骤：1、创建文件件 2、打开文件夹。拖动图标进行排序
			 * 3、点击+号按钮，添加或删除部分程序 4、完成 原因：换位线程与添加程序进文件夹线程同步问题
			 * 修改方法：对userFolderInfo加锁
			 */
			count = userFolderInfo.getChildCount();
		}
		if (count == 0) {
			// delete folder
			Message msg = new Message();
			msg.what = DELETE_FOLDER_ANIMATION;
			msg.obj = userFolderInfo;
			mHandler.sendMessage(msg);

			ret = true;
		} else if (deleteOne && count == 1) {
			// delete folder & move item to desktop
			Message msg = new Message();
			ShortCutInfo shortCutInfo = userFolderInfo.getChildInfo(0);
			shortCutInfo.mScreenIndex = userFolderInfo.mScreenIndex;
			shortCutInfo.mCellX = userFolderInfo.mCellX;
			shortCutInfo.mCellY = userFolderInfo.mCellY;

			View delView = ScreenUtils.getViewByItemId(userFolderInfo.mInScreenId,
					userFolderInfo.mScreenIndex, mWorkspace);
			msg.arg1 = (null != delView) ? delView.getWidth() : Utilities
					.getStandardIconSize(GOLauncherApp.getContext());
			msg.arg2 = (null != delView) ? delView.getHeight() : Utilities
					.getStandardIconSize(GOLauncherApp.getContext());
			deleteItem(userFolderInfo, userFolderInfo.mScreenIndex);

			addDesktopItem(userFolderInfo.mScreenIndex, shortCutInfo);
			View addView = addDesktopView(shortCutInfo, userFolderInfo.mScreenIndex, true);
			msg.what = ADD_ITEM_FROM_FOLDER_ANIMATION;
			msg.obj = addView;
			mHandler.sendMessage(msg);

			ret = true;
		} else {
			ret = false;
		}

		return ret;
	}
	

	private void exchangeDockToScreenPreview(ItemInfo itemInfo) {
		showDockInDesktop(itemInfo, itemInfo.mScreenIndex, true);
	}

	private boolean dragToFolder(ShortCutInfo itemInfo, UserFolderInfo folderInfo) {
		if (null != folderInfo) {
			if (null != itemInfo) {
				if (itemInfo.mInScreenId == 0 || itemInfo.mInScreenId == -1) {
					itemInfo.mInScreenId = System.currentTimeMillis();
				}
				// 图标去重
				mControler.removeItemsFromFolder(folderInfo, itemInfo);
				// 修改数据库
				mControler.addItemInfoToFolder(itemInfo, folderInfo.mInScreenId);
				folderInfo.add(itemInfo); // 增加dock条上的item到文件夹
				// int type = AppIdentifier.whichTypeOfNotification(mActivity,
				// itemInfo.mIntent);
				// if (type != NotificationType.IS_NOT_NOTIFICSTION) {
				// folderInfo.mTotleUnreadCount += itemInfo.mCounter;
				// }
			}
			// 更新文件夹图标
			updateFolderIconAsync(folderInfo, false, false);
			return true;
		}
		return false;
	}

	// 添加快捷方式
	private boolean addShortcut(Object object) {
		boolean ret = false;
		if (object == null || !(object instanceof ShortCutInfo)) {
			return ret;
		}

		ShortCutInfo itemInfo = (ShortCutInfo) object;
		int[] xy = new int[2];
		boolean vacant = ScreenUtils
				.findVacant(xy, 1, 1, mWorkspace.getCurrentScreen(), mWorkspace);
		if (!vacant) {
			setScreenRedBg();
		} else {
			BubbleTextView bubble = inflateBubbleTextView(itemInfo.mTitle, itemInfo.mIcon, itemInfo);
			itemInfo.mCellX = xy[0];
			itemInfo.mCellY = xy[1];
			itemInfo.mSpanX = 1;
			itemInfo.mSpanY = 1;
			mWorkspace.addInCurrentScreen(bubble, xy[0], xy[1], 1, 1);
			addDesktopItem(mWorkspace.getCurrentScreen(), itemInfo);
			ret = true;
		}
		return ret;
	}
	
	/**
	 * <br>功能简述:判断是否可以在1 5屏幕插入广告图标
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public boolean isCanAddAdvertIcon() {
		try {
			//判断是否有5屏
			if (mWorkspace.getChildCount() != 5) {
				return false;
			}
			
			//判断第1屏是否有图标
			int firstScreenViewSize = mWorkspace.getScreenView(0).getChildCount();
			if (firstScreenViewSize != 0) {
				return false;
			}
			//判断第5屏是否有图标
			int fiveScreenViewSize = mWorkspace.getScreenView(4).getChildCount();
			if (fiveScreenViewSize != 0) {
				return false;
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	/**
	 * <br>功能简述:判断桌面1、5屏第一次请求成功后判断是否对屏幕做过修改
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public boolean isCanChangeAdvertIcon(Object object) {
		try {
			if (object == null || !(object instanceof ArrayList)) {
				return false;
			}
			
			//判断是否有5屏
			if (mWorkspace.getChildCount() != 5) {
				return false;
			}
			
			int firstScreenViewSize = mWorkspace.getScreenView(0).getChildCount();
			int fiveScreenViewSize = mWorkspace.getScreenView(4).getChildCount();
			
			int firstAdvertInfoSize = 0;
			int fiveAdvertInfoSize = 0;
			ArrayList<AdvertInfo> advertInfoList = (ArrayList<AdvertInfo>) object;
			for (AdvertInfo advertInfo : advertInfoList) {
				if (advertInfo.mScreen == 0) {
					firstAdvertInfoSize = firstAdvertInfoSize + 1;
				}
				
				else if (advertInfo.mScreen == 4) {
					fiveAdvertInfoSize = fiveAdvertInfoSize + 1;
				}
			}
			
			//判断15屏图标个数是否一致
			if (firstScreenViewSize != firstAdvertInfoSize || fiveScreenViewSize != fiveAdvertInfoSize) {
//				Log.i("lch", "15屏图标数量和缓存数量不相同");
				return false;
			}
			
			//先判断首屏当前图标信息是否和上次缓存个数等其他信息一样
			boolean isCanChangeHomeScreenAdvertIcon = isCanChangeHomeScreenAdvertIcon();
			if (!isCanChangeHomeScreenAdvertIcon) {
//				Log.i("lch", "首屏图标数量和缓存数量不相同，或者首屏其他图标有改变");
				return false;
			}
			
			
			for (AdvertInfo advertInfo : advertInfoList) {
//				Log.i("lch", "=========================");
				
				if (advertInfo.mScreen == 0 || advertInfo.mScreen == 4 || advertInfo.mScreen == 2) {
					if (isAdvertIconChange(advertInfo)) {
						return false; 
					}
				} else {
//					Log.i("lch", "mScreen不是15屏/首屏");
					return false;
				}
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	
	
	
	
	/**
	 * <br>功能简述:检查广告图标是否存在
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param advertInfo
	 * @param screenIndex
	 * @return
	 */
	public boolean isAdvertIconChange(AdvertInfo advertInfo) {
		CellLayout cellLayout = mWorkspace.getScreenView(advertInfo.mScreen);
//		Log.i("lch", "是否文件夹:" + advertInfo.mIsfile);
//		Log.i("lch", "该图标所在屏幕:" + advertInfo.mScreen);
		//app图标
		if (advertInfo.mIsfile == AdvertConstants.IS_NO_FILE) {
			//判断对应的图标是否改变
			if (isAdvertAppIconChange(cellLayout, advertInfo)) {
				return true;
			}
		}
			
		//文件夹
		else if (advertInfo.mIsfile == AdvertConstants.IS_FILE) {
			//判断对应的图标是否改变
			if (isAdvertFolderChange(cellLayout, advertInfo)) {
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * <br>功能简述:广告图标是否已经改变
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param cellLayout
	 * @param advertInfo
	 * @return true - 已经改变
	 */
	public boolean isAdvertFolderChange(CellLayout cellLayout, AdvertInfo advertInfo) {
		int screenViewSize = cellLayout.getChildCount();
		//遍历对应的屏幕控件
		for (int i = 0; i < screenViewSize; i++) {
			Object object = cellLayout.getChildAt(i).getTag();
			if (object != null && object instanceof UserFolderInfo) {
				UserFolderInfo folderInfo = (UserFolderInfo) object;
				
				//判断该位置文件夹符合是否存在
				if (advertInfo.mCellX == folderInfo.mCellX && advertInfo.mCellY == folderInfo.mCellY
					&& advertInfo.mTitle.equals(folderInfo.mFeatureTitle)) {
					ArrayList<AdvertInfo> fileItemCacheList = advertInfo.mFilemsg;	//文件夹缓存列表
					ArrayList<ItemInfo> folderItemIconList = folderInfo.getContents();	//当前文件夹列表
					//文件夹缓存每个和文件夹图标进行匹配
					//判断文件夹里面的图标是否有改变
					boolean isFolderItemChange = isAdvertFolderItemChange(fileItemCacheList, folderItemIconList);
					//如果文件夹里面图标已经改变
					if (!isFolderItemChange) {
//						Log.i("lch", "文件夹存在-且item没有改变：" + folderInfo.mFeatureTitle);
						return false;
					} else {
//						Log.i("lch", "已经改变-文件夹item已改变：" + folderInfo.mFeatureTitle);
					}
				}
			}
		}
//		Log.i("lch", "已经改变-文件夹：" + advertInfo.mTitle);
		return true;
	}
	
	
	/**
	 * <br>功能简述:判断广告文件夹里面图标是否改变
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param folderItemCacheList
	 * @param folderItemIconList
	 * @return true - 有变化，
	 */
	public boolean isAdvertFolderItemChange(ArrayList<AdvertInfo> folderItemCacheList, ArrayList<ItemInfo> folderItemIconList) {
		
		//判断文件夹里面的个数是否一致
		if (folderItemCacheList.size() != folderItemIconList.size()) {
//			Log.i("lch", "已经改变-文件夹存个数不一样！");
			return true;
		}
		
		
		//先遍历现在有的文件夹图标是否已经安装改变了图标性质
		for (ItemInfo itemInfo : folderItemIconList) {
			ShortCutInfo shortCutInfo = (ShortCutInfo) itemInfo;
			//判断是否广告图标
			Intent intent = shortCutInfo.mIntent;
			if (!intent.getAction().equals(ICustomAction.ACTION_SCREEN_ADVERT)) {
//				Log.i("lch", "已经改变-文件夹里面图标action不一样");
				return true;
			}
		}
		
		//遍历每隔图标的内容
		for (AdvertInfo advertInfo : folderItemCacheList) {
			boolean isExist = false;
			
			for (ItemInfo itemInfo : folderItemIconList) {
				ShortCutInfo shortCutInfo = (ShortCutInfo) itemInfo;
				
				//文件夹图标不能判断位置
				if (advertInfo.mTitle.equals(shortCutInfo.mTitle)) {
//					Log.i("lch", "文件夹里图标没有改变：" + advertInfo.mTitle);
					isExist = true;
					break;
				}
			}
			
			//判断图标是否存在
			if (!isExist) {
//				Log.i("lch", "已经改变-文件夹里图标不存在：" + advertInfo.mTitle);
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * <br>功能简述:15屏广告图标是否改变
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param cellLayout
	 * @param advertInfo
	 * @return
	 */
	public boolean isAdvertAppIconChange(CellLayout cellLayout, AdvertInfo advertInfo) {
		int screenViewSize = cellLayout.getChildCount();
		//遍历对应的屏幕控件
		for (int i = 0; i < screenViewSize; i++) {
			Object object = cellLayout.getChildAt(i).getTag();
			if (object != null && object instanceof ShortCutInfo) {
				ShortCutInfo cutInfo = (ShortCutInfo) object;
				
				if (advertInfo.mCellX == cutInfo.mCellX && advertInfo.mCellY == cutInfo.mCellY
					&& advertInfo.mTitle.equals(cutInfo.mTitle)) {
					//判断是否广告图标
					Intent intent = cutInfo.mIntent;
					if (!intent.getAction().equals(ICustomAction.ACTION_SCREEN_ADVERT)) {
//						Log.i("lch", "退出：action不一样：" + cutInfo.mTitle);
						return true;
					} else {
//						Log.i("lch", "app图标存在：" + cutInfo.mTitle);
						return false;
					}
				}
			}
		}
//		Log.i("lch", "已经改变-app图标：" + advertInfo.mTitle);
		return true;
	}
	
	/**
	 * <br>功能简述:清除15屏广告图标
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public boolean clearAdvertIcon(Object object) {
		try {
			if (object == null || !(object instanceof ArrayList)) {
				return false;
			}
			
			//判断是否有5屏
			if (mWorkspace.getChildCount() != 5) {
				return false;
			}
						
			//删除首屏/1 5屏幕图标
			if (deledAllHomeScreenAdverIcon(object) 
					&& deleteOneScreenAllIcon(0) && deleteOneScreenAllIcon(4)) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * <br>功能简述:删除首屏幕所有广告图标
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param object
	 */
	public boolean deledAllHomeScreenAdverIcon(Object object) {
		try {
			ArrayList<ItemInfo> viewItemInfoList = new ArrayList<ItemInfo>();
			CellLayout homeCellLayout = mWorkspace.getScreenView(2);
			int homeCellLayoutSize = homeCellLayout.getChildCount();
			ArrayList<AdvertInfo> advertInfoList = (ArrayList<AdvertInfo>) object;
			for (AdvertInfo advertInfo : advertInfoList) {
				if (advertInfo.mScreen == 2) {
					for (int i = 0; i < homeCellLayoutSize; i++) {
						Object tag = homeCellLayout.getChildAt(i).getTag();
						if (tag != null && tag instanceof ItemInfo) {
							ItemInfo itemInfo = (ItemInfo) tag;
							if (advertInfo.mCellX == itemInfo.mCellX && advertInfo.mCellY == itemInfo.mCellY) {
								viewItemInfoList.add(itemInfo);	//先纪录当前屏幕图标信息
							}
						}
					}
				}
			}
			
			//遍历所有图标删除
			for (ItemInfo itemInfo : viewItemInfoList) {
				deleteItem(itemInfo, 2);
//				Log.i("lch", "首屏图标删除成功：" + itemInfo.toString());
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * <br>功能简述:清除指定屏幕里面的图标
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param screenIndex
	 * @return
	 */
	public boolean deleteOneScreenAllIcon(int screenIndex) {
		try {
			ArrayList<ItemInfo> viewItemInfoList = new ArrayList<ItemInfo>();
			CellLayout cellLayout = mWorkspace.getScreenView(screenIndex);
			int firstCellLayoutSize = cellLayout.getChildCount();
			for (int i = 0; i < firstCellLayoutSize; i++) {
				Object object = cellLayout.getChildAt(i).getTag();
				if (object != null && object instanceof ItemInfo) {
					viewItemInfoList.add((ItemInfo) object);	//先纪录当前屏幕图标信息
				}
			}
			
			//遍历所有图标删除
			for (ItemInfo itemInfo : viewItemInfoList) {
				deleteItem(itemInfo, screenIndex);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * <br>功能简述:插入广告图标到桌面
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param object ShortCutInfo
	 * @param addScreen 需要插入的屏幕
	 * @return
	 */
	private boolean addAdvertShortCut(Object object, int addScreen) {
		boolean ret = false;
		if (object == null || !(object instanceof ShortCutInfo)) {
			return ret;
		}

		int screenCount = mWorkspace.mScroller.getScreenCount();
		
		if (addScreen < 0 || addScreen >= screenCount) {
			return ret;
		}
		
		ShortCutInfo shortCutInfo = (ShortCutInfo) object;
		
		//如果是首屏要先判断对应的位置是否有图标。进行删除
		if (addScreen == 2) {
			deleteOneAdvertIcon(addScreen, shortCutInfo); //删除需要插入广告图标对应位置原有的图标
		}
		
		
		BubbleTextView bubble = inflateBubbleTextView(shortCutInfo.mTitle, shortCutInfo.mIcon, shortCutInfo);
		shortCutInfo.mSpanX = 1;
		shortCutInfo.mSpanY = 1;
		
		mWorkspace.addInScreen(bubble, addScreen, shortCutInfo.mCellX, shortCutInfo.mCellY, 1, 1, true);
		addDesktopItem(addScreen, shortCutInfo);
		ret = true;
		return ret;
	}
	
	/**
	 * <br>功能简述:删除需要插入广告图标对应位置原有的图标
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param screenIndex
	 * @param shortCutInfo
	 * @return
	 */
	public boolean deleteOneAdvertIcon(int screenIndex, ItemInfo shortCutInfo) {
		try {
			CellLayout cellLayout = mWorkspace.getScreenView(screenIndex);
			int firstCellLayoutSize = cellLayout.getChildCount();
			for (int i = 0; i < firstCellLayoutSize; i++) {
				Object object = cellLayout.getChildAt(i).getTag();
				if (object != null && object instanceof ItemInfo) {
					ItemInfo itemInfo =	(ItemInfo) object;
					if (shortCutInfo.mCellX == itemInfo.mCellX && shortCutInfo.mCellY == itemInfo.mCellY) {
						deleteItem(itemInfo, screenIndex);
//						Log.i("lch", "删除图标成功！");
						return true;
					}
				}
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * <br>功能简述:插入广告文件夹到桌面
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param object 文件夹
	 * @param addScreen 插入的屏幕
	 * @return
	 */
	private boolean addAdvertFolder(Object object, int addScreen) {
		boolean ret = false;
		// 添加一个新的文件夹
		if (object == null || !(object instanceof UserFolderInfo)) {
			return ret;
		}
		
		int screenCount = mWorkspace.mScroller.getScreenCount();
		
		if (addScreen < 0 || addScreen >= screenCount) {
			return ret;
		}
		
		UserFolderInfo folderInfo = (UserFolderInfo) object;
		
		//如果是首屏要先判断对应的位置是否有图标。进行删除
		if (addScreen == 2) {
			deleteOneAdvertIcon(addScreen, folderInfo); //删除需要插入广告图标对应位置原有的图标
		}
		
		try {
			addDesktopItem(addScreen, folderInfo);	//添加到数据库
			
			//关联图标，标题等
			FolderIcon newFolder = FolderIcon.fromXml(getFolderIconId(), mActivity,
					mWorkspace.getCurrentScreenView(), folderInfo,
					getDisplayTitle(folderInfo.mTitle));
			newFolder.close();
			newFolder.setShowShadow(getShadowState());
			
			//插入到屏幕
			mWorkspace.addInScreen(newFolder, addScreen, folderInfo.mCellX, folderInfo.mCellY, 1, 1, true);
			ret = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (ret) {
			//添加文件夹内容
			addUserFolderContent(folderInfo.mInScreenId, folderInfo, folderInfo.getContents(), false);
		}
			
		return ret;
	}
	
	
	// 添加文件夹
	private boolean addLiveFolder(Object object) {
		boolean ret = false;
		if (object == null || !(object instanceof ScreenLiveFolderInfo)) {
			return ret;
		}

		ScreenLiveFolderInfo folderInfo = (ScreenLiveFolderInfo) object;
		int[] xy = new int[2];
		boolean vacant = ScreenUtils
				.findVacant(xy, 1, 1, mWorkspace.getCurrentScreen(), mWorkspace);
		if (!vacant) {
			setScreenRedBg();
		} else {
			BubbleTextView bubble = inflateBubbleTextView(folderInfo.mTitle, folderInfo.mIcon,
					folderInfo);
			folderInfo.mCellX = xy[0];
			folderInfo.mCellY = xy[1];
			mWorkspace.addInCurrentScreen(bubble, xy[0], xy[1], 1, 1);
			addDesktopItem(mWorkspace.getCurrentScreen(), folderInfo);
			ret = true;
		}
		return ret;
	}

	private boolean addDeskUserFolder(Object object, int createType) {
		boolean ret = false;
		// 添加一个新的文件夹
		if (object == null || !(object instanceof UserFolderInfo)) {
			return ret;
		}

		int[] xy = new int[2];
		boolean vacant = ScreenUtils
				.findVacant(xy, 1, 1, mWorkspace.getCurrentScreen(), mWorkspace);
		if (!vacant) {
			xy = null;
			setScreenRedBg();
		} else {
			if (createType != AppTab.APP_ADD_TAB_ADD) {
				CellLayout layout = mWorkspace.getCurrentScreenView();
				if (layout != null/*
									* && layout.mState ==
									* CellLayout.STATE_BLANK_CONTENT
									*/
				) {
					mWorkspace.blankCellToNormal(mWorkspace.getCurrentScreenView());
					UserFolderInfo userFolderInfo = (UserFolderInfo) object;
					userFolderInfo.mCellX = xy[0];
					userFolderInfo.mCellY = xy[1];
					mControler.addDesktopItem(mWorkspace.getCurrentScreen(), userFolderInfo);
					mNewFolderId = userFolderInfo.mInScreenId;
					GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
							IDiyMsgIds.CREATE_DESK_USERFOLDER, 0, object, null);
					return ret;
				}
			}

			Intent intent = new Intent(mActivity, ScreenModifyFolderActivity.class);
			intent.putExtra(ScreenModifyFolderActivity.CREATE_TYPE, createType);
			intent.putExtra(ScreenModifyFolderActivity.FOLDER_CUR_SCREEN,
					mWorkspace.getCurrentScreen());
			intent.putExtra(ScreenModifyFolderActivity.FOLDER_LOCATION, xy);

			if (createType == AppTab.APP_ADD_TAB_ADD) {
				intent.putExtra(ScreenModifyFolderActivity.FOLDER_CREATE, false);
				mActivity.startActivityForResult(intent, IRequestCodeIds.REQUEST_SCREEN_ADD_APP);
			} else {
				intent.putExtra(ScreenModifyFolderActivity.FOLDER_CREATE, true);
				mActivity.startActivityForResult(intent,
						IRequestCodeIds.REQUEST_DESKTOP_FOLDER_EDIT);
			}
			ret = true;
		}
		return ret;
	}

	/**
	 * 添加界面，判断当前屏是否有足够空间
	 */
	private boolean checkVacant(List<Integer> objects) {
		if (objects != null && objects instanceof ArrayList && objects.size() > 1) {
			int spanX = objects.get(0);
			int spanY = objects.get(1);
			int[] xy = new int[2];
			boolean vacant = ScreenUtils.findVacant(xy, spanX, spanY,
					mWorkspace.getCurrentScreen(), mWorkspace);
			if (!vacant) {
				xy = null;
				setScreenRedBg();
				return false;
			}
			return true;
		}
		return false;
	}

	private boolean addUserFolder(Object object) {
		boolean ret = false;
		// 添加一个新的文件夹
		if (object == null || !(object instanceof UserFolderInfo)) {
			return ret;
		}

		UserFolderInfo folderInfo = (UserFolderInfo) object;

		// 进行数据过滤
		ArrayList<ComponentName> mComponentNamelist = AppDataEngine.getInstance(mActivity)
				.getHideComponentList();
		ArrayList<ShortCutInfo> hideList = new ArrayList<ShortCutInfo>();
		for (int i = 0; i < folderInfo.getChildCount(); i++) {
			if (folderInfo.getChildInfo(i).mIntent != null) {
				if (mComponentNamelist.contains(folderInfo.getChildInfo(i).mIntent.getComponent())) {
					hideList.add(folderInfo.getChildInfo(i));
				}
			}
		}
		int size = hideList.size();
		for (int i = 0; i < size; i++) {
			folderInfo.remove(hideList.get(i));
		}

		FolderIcon newFolder = FolderIcon.fromXml(getFolderIconId(), mActivity,
				mWorkspace.getCurrentScreenView(), folderInfo, getDisplayTitle(folderInfo.mTitle));
		newFolder.close();
		// customDeskTopBackground(newFolder);
		newFolder.setShowShadow(getShadowState());
		int[] xy = new int[2];
		boolean vacant = ScreenUtils
				.findVacant(xy, 1, 1, mWorkspace.getCurrentScreen(), mWorkspace);
		if (!vacant) {
			setScreenRedBg();
		} else {
			folderInfo.mCellX = xy[0];
			folderInfo.mCellY = xy[1];
			mWorkspace.addInCurrentScreen(newFolder, xy[0], xy[1], 1, 1);
			folderInfo.mTitle = folderInfo.mTitle == null ? "" : folderInfo.mTitle;
			// 添加FeatureTitle，否则从功能表拉出来的自定义文件夹名字将变为“文件夹”
			folderInfo.setFeatureTitle(folderInfo.mTitle.toString());
			addDesktopItem(mWorkspace.getCurrentScreen(), folderInfo);
			ret = true;
		}
		hideList.clear();
		hideList = null;
		mComponentNamelist.clear();
		mComponentNamelist = null;

		return ret;
	}

	private boolean addUserFolder(int screenIndex, int cellX, int cellY, Object object) {
		boolean ret = false;
		// 添加一个新的文件夹
		if (object == null || !(object instanceof UserFolderInfo)) {
			return ret;
		}

		UserFolderInfo folderInfo = (UserFolderInfo) object;

		int count = folderInfo.getChildCount();

		// 进行数据过滤
		ArrayList<ComponentName> mComponentNamelist = AppDataEngine.getInstance(mActivity)
				.getHideComponentList();
		ArrayList<ShortCutInfo> hideList = new ArrayList<ShortCutInfo>();
		for (int i = 0; i < count; i++) {
			if (folderInfo.getChildInfo(i).mIntent != null) {
				if (mComponentNamelist.contains(folderInfo.getChildInfo(i).mIntent.getComponent())) {
					hideList.add(folderInfo.getChildInfo(i));
				}
			}
		}
		int size = hideList.size();
		for (int i = 0; i < size; i++) {
			folderInfo.remove(hideList.get(i));
		}
		folderInfo.mCellX = cellX;
		folderInfo.mCellY = cellY;
		FolderIcon newFolder = FolderIcon.fromXml(getFolderIconId(), mActivity,
				mWorkspace.getScreenView(screenIndex), folderInfo,
				getDisplayTitle(folderInfo.mTitle));
		newFolder.close();
		newFolder.setShowShadow(getShadowState());
		if (!ScreenUtils.isOccupied(screenIndex, cellX, cellY, mWorkspace)) {
			mWorkspace.addInScreen(newFolder, screenIndex, folderInfo.mCellX, folderInfo.mCellY,
					folderInfo.mSpanX, folderInfo.mSpanY, true);
			folderInfo.mTitle = folderInfo.mTitle == null ? "" : folderInfo.mTitle;
			folderInfo.setFeatureTitle(folderInfo.mTitle.toString());
			addDesktopItem(screenIndex, folderInfo);
			ret = true;
		} else {
			DeskToast.makeText(mActivity, R.string.cell_has_been_occupied, Toast.LENGTH_SHORT);
		}
		hideList.clear();
		hideList = null;
		mComponentNamelist.clear();
		mComponentNamelist = null;

		return ret;
	}

	private boolean addAppDrawerFolder(List<?> objects) {
		boolean ret = false;
		if (null == objects) {
			return ret;
		}
		int sz = objects.size();
		for (int i = 0; i < sz; i++) {
			Object object = objects.get(i);
			ret = addAppDrawerFolder(object);
			if (!ret) {
				// 空间不足

				setScreenRedBg();
			}
		}
		return ret;
	}

	// 添加文件夹内容
	public void addUserFolderContent(long folderId, UserFolderInfo folderInfo,
			ArrayList<ItemInfo> items, boolean isFromDrawer) {
		if (null == items) {
			mControler.addUserFolderContent(folderInfo, isFromDrawer);
		} else {
			mControler.addUserFolderContent(folderId, items, isFromDrawer);
		}
	}

	// // 添加文件夹内容
	// public void addUserFolderContent(long folderId, ArrayList<ItemInfo>
	// items, boolean isFromDrawer)
	// {
	// mControler.addUserFolderContent(folderId, items, isFromDrawer);
	// }

	// 删除文件夹内容
	public void removeUserFolderConent(long folderId, ArrayList<ItemInfo> items,
			boolean isFromDrawer) {
		mControler.removeUserFolderContent(folderId, items, isFromDrawer);
	}

	private boolean addAppDrawerFolder(Object object) {
		boolean ret = false;
		if (object == null || !(object instanceof UserFolderInfo)) {
			return ret;
		}

		ret = addUserFolder(object);
		if (ret) {
			// 异步加载数据
			final UserFolderInfo folderInfo = (UserFolderInfo) object;
			if (mBinder != null) {
				mBinder.synchFolderFromDrawer(folderInfo, null, false);
			}
			return true;
		}
		return false;
	}

	private boolean handleDesktopSettingChange() {
		boolean ret = false;
		boolean reload = false;
		mDesktopSettingInfo = GOLauncherApp.getSettingControler().getDesktopSettingInfo();
		if (mDesktopSettingInfo != null) {
			final int row = mWorkspace.getDesktopRows();
			final int col = mWorkspace.getDesktopColumns();
			final boolean autofit = mWorkspace.getmAutoStretch();

			if (mDesktopSettingInfo.isReload()) {
				mDesktopSettingInfo.setReload(false);
				reload = true;
			}
			if (row != mDesktopSettingInfo.getRows() || col != mDesktopSettingInfo.getColumns()) {
				mWorkspace.setDesktopRowAndCol(mDesktopSettingInfo.getRows(),
						mDesktopSettingInfo.getColumns());
				reload = true;
			}
			if (autofit != mDesktopSettingInfo.mAutofit) {
				mWorkspace.setmAutoStretch(mDesktopSettingInfo.mAutofit);
				reload = true;
			}
			if (reload) {
				reloadDesktop();
			} else {
				final boolean showlabel = mDesktopSettingInfo.isShowTitle();
				final boolean showShadow = !mDesktopSettingInfo.isTransparentBg();
				final int count = mWorkspace.getChildCount();
				for (int i = 0; i < count; i++) {
					CellLayout screen = (CellLayout) mWorkspace.getChildAt(i);
					final int childCount = screen.getChildCount();
					for (int j = 0; j < childCount; j++) {
						View child = screen.getChildAt(j);
						if (child instanceof BubbleTextView) {
							BubbleTextView bubbleView = (BubbleTextView) child;
							bubbleView.setShowShadow(showShadow);
							bubbleView.setFontSize();
							ScreenUtils.setBubbleTextTitle(showlabel, bubbleView);
							bubbleView.setTitleColor();
						}
					}
				}
			}

			ret = true;
		}
		return ret;
	}

	private boolean handleScreenSettingChange() {
		boolean ret = false;
		mScreenSettingInfo = GOLauncherApp.getSettingControler().getScreenSettingInfo();
		if (mScreenSettingInfo != null) {
			mWorkspace.setMainScreen(mScreenSettingInfo.mMainScreen);
			mWorkspace.setWallpaperScroll(mScreenSettingInfo.mWallpaperScroll);
			mWorkspace.setCycleMode(mScreenSettingInfo.mScreenLooping);
			mLayout.getIndicator().setVisible(mScreenSettingInfo.mEnableIndicator);
			mShowIndicator = mScreenSettingInfo.mEnableIndicator;
			mLayout.getIndicator().setAutoHide(mScreenSettingInfo.mAutoHideIndicator);
			mWorkspace.requestLayout(Workspace.CHANGE_SOURCE_INDICATOR, 0);
			ret = true;
		}
		return ret;
	}

	private boolean handleThemeSettingChange(final ThemeSettingInfo settingInfo) {
		if (settingInfo != null) {
			mThemeSettingInfo = settingInfo;
		} else {
			mThemeSettingInfo = GOLauncherApp.getSettingControler().getThemeSettingInfo();
		}

		boolean ret = false;
		if (mThemeSettingInfo != null) {
			mWorkspace.setPreventFC(mThemeSettingInfo.mPreventForceClose);
		}
		return ret;
	}

	private boolean handleEffectSettingChange(final EffectSettingInfo settingInfo) {
		if (settingInfo != null) {
			mEffectSettingInfo = settingInfo;
		} else {
			mEffectSettingInfo = GOLauncherApp.getSettingControler().getEffectSettingInfo();
		}

		boolean ret = false;
		if (mEffectSettingInfo != null) {
			mWorkspace.setScrollDuration(mEffectSettingInfo.getDuration());
			if (mEffectSettingInfo.mEffectorType == CoupleScreenEffector.EFFECTOR_TYPE_RANDOM_CUSTOM) {
				mWorkspace
						.setCustomRandomEffectorEffects(mEffectSettingInfo.mEffectCustomRandomEffects);
				mWorkspace.setEffector(mEffectSettingInfo.mEffectorType);
			} else {
				mWorkspace.setEffector(mEffectSettingInfo.mEffectorType); // 先设置效果器，限制当前使用的弹力
			}
			mWorkspace.setOvershootAmount(mEffectSettingInfo.getOvershootAmount());
			mWorkspace.setAutoTweakElasicity(mEffectSettingInfo.mAutoTweakElasticity);
			ret = true;
		}
		return ret;
	}

	/**
	 * loadShortcutAsync到UI上回调
	 * 
	 * @param item
	 */
	public void postLoadShortcut(ShortCutInfo item) {
		if (item == null) {
			return;
		}

		final View view = inflateBubbleTextView(item.mTitle, item.mIcon, item);
		if (view != null) {
			mWorkspace.addInScreen(view, item.mScreenIndex, item.mCellX, item.mCellY, item.mSpanX,
					item.mSpanY, false);
		} else {
			ScreenMissIconBugUtil.showToast(ScreenMissIconBugUtil.ERROR_ADDVIEW_APP_SHORTCUT_NULL);
		}
	}

	private View createShortcutIcon(ItemInfo item, boolean sync) {
		if (item == null || !(item instanceof ShortCutInfo)) {
			ScreenMissIconBugUtil
					.showToast(ScreenMissIconBugUtil.ERROR_CREATEDESKTOPVIEW_APP_INFO_NULL);
			return null;
		}

		if (sync) {
			final ShortCutInfo shortcutInfo = (ShortCutInfo) item;
			loadCompleteInfo(shortcutInfo);
			return inflateBubbleTextView(shortcutInfo.mTitle, shortcutInfo.mIcon, shortcutInfo);
		} else {
			if (mBinder != null) {
				mBinder.loadShortcutAsync((ShortCutInfo) item);
			}
		}
		return null;
	}

	private View createAppWidgetView(ItemInfo item) {
		if (item instanceof ScreenAppWidgetInfo) {
			final ScreenAppWidgetInfo widgetInfo = (ScreenAppWidgetInfo) item;
			return filterWidgetView(widgetInfo);
		}
		return null;
	}

	private View createUserFolder(ItemInfo item) {
		if (item != null && item instanceof UserFolderInfo) {
			UserFolderInfo folderInfo = (UserFolderInfo) item;
			FolderIcon newFolder = FolderIcon.fromXml(getFolderIconId(), mActivity,
					mWorkspace.getCurrentScreenView(), folderInfo,
					getDisplayTitle(folderInfo.mTitle));
			newFolder.setTag(folderInfo);
			newFolder.close();
			newFolder.setShowShadow(getShadowState());
			// customDeskTopBackground(newFolder);
			return newFolder;
		}
		return null;
	}

	private View createFavoriteView(ItemInfo item) {
		if (item instanceof FavoriteInfo) {
			FavoriteInfo favInfo = mControler.getFavoriteInfo((FavoriteInfo) item);
			if (favInfo != null && favInfo.mPreview > 0) {
				ImageView imageView = (ImageView) mInflater.inflate(R.layout.favorite_widget,
						mWorkspace.getCurrentScreenView(), false);
				imageView.setImageResource(favInfo.mPreview);
				imageView.setTag(item);
				return imageView;
			}
		}
		return null;
	}

	private View createLiveFolder(ItemInfo item) {
		if (item instanceof ScreenLiveFolderInfo) {
			ScreenLiveFolderInfo folderInfo = (ScreenLiveFolderInfo) item;
			return inflateBubbleTextView(folderInfo.mTitle, folderInfo.mIcon, folderInfo);
		}
		return null;
	}

	private void addCellLayout() {
		CellLayout screen = (CellLayout) mInflater.inflate(R.layout.workspace_screen, null);
		screen.setNeedToTryCatch(mNeedToCatch);
		mWorkspace.addScreen(screen, mWorkspace.getChildCount());
		StatisticsData.sSCREEN_COUNT = mWorkspace.getChildCount();
		// 添加新的一个屏，屏幕数发生变化，发广播给多屏多壁纸
		mWorkspace.sendBroadcastToMultipleWallpaper(false, true);
	}

	/**
	 * 添加带有“+”号的空白屏幕
	 */
	private void addBlankCellLayout() {
		CellLayout blankScreen = (CellLayout) mInflater.inflate(R.layout.workspace_screen, null);
		blankScreen.setNeedToTryCatch(mNeedToCatch);
		blankScreen.setLayoutScale(Workspace.sLayoutScale);
		blankScreen.setBlank(CellLayout.STATE_BLANK_CONTENT);
		mWorkspace.addScreen(blankScreen, mWorkspace.getChildCount());
		// 添加了一个空白屏，屏幕数发生变化，发广播给多屏多壁纸
		mWorkspace.sendBroadcastToMultipleWallpaper(true, true);
	}

	private void cleanHandlerMsg() {
		if (mHandler != null) {
			mHandler.removeMessages(START_DESKTOP_LOADER);
			mHandler.removeMessages(UPDATE_DESKTOP_SETTING);
			mHandler.removeMessages(UPDATE_SCREEN_SETTING);
			mHandler.removeMessages(UPDATE_EFFECT_SETTING);
			mHandler.removeMessages(UPDATE_THEME_SETTING);

			mHandler.removeMessages(DELETE_SCREEN);
			mHandler.removeMessages(ADD_SCREEN);
			mHandler.removeMessages(SET_CURRENT_SCREEN);
			mHandler.removeMessages(ENTER_SCREEN);
			mHandler.removeMessages(ENTER_PREVIEW);
			mHandler.removeMessages(REFRESH_INDEX);

			mHandler.removeMessages(REFRESH_UNINSTALL);
			mHandler.removeMessages(UPDATE_ITEMS_IN_SDCARD);
			mHandler.removeMessages(UPDATE_ALL_FOLDER);
			mHandler.removeMessages(UPDATE_FOLDER_LIST);

			mHandler.removeMessages(REFRESH_FOLDER_CONTENT);
			mHandler.removeMessages(DELETE_FOLDER);

			mHandler.removeMessages(DELETE_FOLDER_ANIMATION);
			mHandler.removeMessages(ADD_ITEM_FROM_FOLDER_ANIMATION);
			mHandler.removeMessages(ASK_OPEN_MERGE_FOLDER);

			mHandler.removeMessages(GOTO_SPECIFIC_GOWIDGET);
			mHandler.removeMessages(ADD_BLANK_SCREEN);
		}
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case START_DESKTOP_LOADER : {
					mWorkspace.setCurrentScreen(mCurrentScreen);
					mInitWorkspace = true;
					startDesktopLoader();
					break;
				}

				case UPDATE_ALL_FOLDER : {
					updateAllFolder(true);
					break;
				}

				case UPDATE_FOLDER_LIST : {
					@SuppressWarnings("unchecked")
					ArrayList<ItemInfo> folderList = (ArrayList<ItemInfo>) msg.obj;
					updateFolderList(folderList);
					break;
				}

				case REFRESH_FOLDER_CONTENT : {
					UserFolderInfo info = (UserFolderInfo) msg.obj;
					boolean checkDel = msg.arg1 == 1 ? true : false;
					reloadFolderContent(info, checkDel);
					break;
				}

				case DELETE_FOLDER_ANIMATION : {
					if (null != msg.obj && msg.obj instanceof UserFolderInfo) {
						UserFolderInfo userFolderInfo = (UserFolderInfo) msg.obj;
						View targetView = ScreenUtils.getViewByItemId(userFolderInfo.mInScreenId,
								userFolderInfo.mScreenIndex, mWorkspace);

						if (null != targetView) {
							mDeleteFolderInfo = userFolderInfo;
							ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 0.0f, 1.0f,
									0.0f, targetView.getWidth() / 2, targetView.getHeight() / 2);
							scaleAnimation.setDuration(500);
							scaleAnimation.setAnimationListener(ScreenFrame.this);
							targetView.startAnimation(scaleAnimation);
						}
					}
				}
					break;

				case DELETE_FOLDER : {
					deleteItem((ItemInfo) msg.obj, msg.arg1);
					break;
				}

				case UPDATE_ITEMS_IN_SDCARD : {
					if (msg.obj != null) {
						@SuppressWarnings("unchecked")
						ArrayList<ItemInfo> itemList = (ArrayList<ItemInfo>) msg.obj;
						updateItemsIconAndTitle(itemList);
						GOLauncherApp.getAppDataEngine().onHandleFolderThemeIconStyleChanged();
					}
					break;
				}

				case DELETE_SCREEN : {
					mWorkspace.removeScreen(msg.arg1);
					StatisticsData.sSCREEN_COUNT = mWorkspace.getChildCount();
					break;
				}

				case ADD_SCREEN : {
					addCellLayout();
					break;
				}

				case ADD_BLANK_SCREEN : {
					addBlankCellLayout();
					break;
				}

				case SET_CURRENT_SCREEN : {
					mWorkspace.setCurrentScreen(msg.arg1);
					break;
				}

				case ENTER_SCREEN : {
					turnToScreen(msg.arg1, true, -1);
					break;
				}

				case UPDATE_DESKTOP_SETTING :
					handleDesktopSettingChange();
					break;

				case UPDATE_EFFECT_SETTING :
					handleEffectSettingChange(null);
					break;

				case UPDATE_SCREEN_SETTING :
					handleScreenSettingChange();
					break;

				case UPDATE_THEME_SETTING :
					handleThemeSettingChange(null);
					break;

				case REFRESH_UNINSTALL :
					// 卸载后更新屏幕
					if (msg.obj != null && msg.obj instanceof Intent) {
//						uninstallApp((Intent) msg.obj);
						Intent intent = (Intent) msg.obj;
						uninstallApp(intent);
						ComponentName name = intent.getComponent();
						if (name != null && name.getPackageName().equals(PackageName.MEDIA_PLUGIN)) {
							// 多媒体插件包卸载，重启桌面
							GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
									IDiyMsgIds.RESTART_GOLAUNCHER, -1, null, null);
						}
					}
					break;

				case ENTER_PREVIEW : {
					showPreview(msg.arg1 == 1);
					break;
				}

				case REPLACE_FAV_WIDGET : {
					// 有虚拟widget的时候才替换
					if (mControler.isExistFavorite()) {
						replaceFavWidgets((String) msg.obj);
					}
					break;
				}

				case SHOW_LOST_ICON_ERRORCODE : {
					// for debue lost icon
					Toast.makeText(mActivity, "ErrorCode: " + Integer.toString(msg.arg1),
							Toast.LENGTH_LONG).show();
					break;
				}

				case REFRESH_INDEX : {
					mWorkspace.refreshScreenIndex();
					break;
				}

				case ADD_ITEM_FROM_FOLDER_ANIMATION : {
					try {
						View view = (View) msg.obj;
						ScaleAnimation scaleAnimation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f,
								msg.arg1 / 2, msg.arg2 / 2);
						scaleAnimation.setDuration(300);
						scaleAnimation.setAnimationListener(ScreenFrame.this);
						view.startAnimation(scaleAnimation);
					} catch (Exception e) {
					}
				}
					break;

				case GOTO_SPECIFIC_GOWIDGET : {
					String pkg = (String) msg.obj;
					int screenindex = findSpecificGoWidgetScreenIndex(pkg);
					if (screenindex >= 0 && screenindex < mWorkspace.getChildCount()) {
						mWorkspace.snapToScreen(screenindex, true, 0);
						mCurrentScreen = screenindex;
					}
				}
					break;

				case ASK_OPEN_MERGE_FOLDER : {
					if (null != mMergeFordleIconToOpen) {
						boolean canOpen = false;
						UserFolderInfo userFolderInfo = null;
						try {
							userFolderInfo = (UserFolderInfo) mMergeFordleIconToOpen.getTag();
							if (userFolderInfo.getChildCount() == 2) {
								// 加载完了，有两个图标，才可以打开
								canOpen = true;
							}
						} catch (Exception e) {
						}
						if (canOpen) {
							FolderIcon.prepareIcon(mMergeFordleIconToOpen, userFolderInfo);
							mMergeFordleIconToOpen.performClick();
						}
						mMergeFordleIconToOpen = null;
					} else {
						// 询问dock是否有待自动打开的合并文件夹
						GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME,
								IDiyMsgIds.DOCK_ASK_OPEN_MERGE_FOLDER, -1, null, null);
					}
				}
					break;

				case FINISH_MERGING : {
					dismissProgressDialog();
					if (msg.obj != null && msg.obj instanceof long[]) {
						long[] foldersId = (long[]) msg.obj;
						refreshFolderItems(foldersId[0], false);
						completeMergeFolder(mViewTemp, mRectTemp);
					}// end if
				}
					break;
				case WIDGET_DELAY_REF :
					mWorkspace.getCurrentScreenView().invalidate();
					break;
				default :
					break;
			}
		};
	};

	private int findSpecificGoWidgetScreenIndex(String pkg) {
		if (null == pkg) {
			return -1;
		}

		ArrayList<GoWidgetBaseInfo> list = mControler.getAllGoWidgetInfos();
		if (null != list && !list.isEmpty()) {
			int size = list.size();
			for (int i = 0; i < size; i++) {
				GoWidgetBaseInfo goWidgetBaseInfo = list.get(i);
				if (null != goWidgetBaseInfo
						&& pkg.equals(goWidgetBaseInfo.mPackage)
						&& !(pkg.equals(LauncherEnv.PACKAGE_NAME) && goWidgetBaseInfo.mPrototype == GoWidgetBaseInfo.PROTOTYPE_GOSTORE)) {
					list.clear();
					list = null;
					return mControler.findSpecificGoWidgetScreenIndex(goWidgetBaseInfo.mWidgetId);
				}
			}
			list.clear();
			list = null;
		}

		return -1;
	}

	private void actionRename(String newName, long inscreenid) {
		// 重命名
		View editingView = ScreenUtils.getViewByItemId(inscreenid, mWorkspace.getCurrentScreen(),
				mWorkspace);

		if (editingView == null || !(editingView instanceof BubbleTextView)) {
			return;
		}

		Object tag = ((BubbleTextView) editingView).getTag();
		if (null != tag && tag instanceof ItemInfo) {
			if (tag instanceof FeatureItemInfo) {
				((FeatureItemInfo) tag).setFeatureTitle(newName);
			}
			if (null != mControler) {
				ItemInfo info = (ItemInfo) tag;
				mControler.updateDesktopItem(info.mScreenIndex, info);
			}

			if (tag instanceof ShortCutInfo) {
				((ShortCutInfo) tag).setTitle(newName, true);
			} else if (tag instanceof ScreenFolderInfo) {
				((ScreenFolderInfo) tag).mTitle = newName;
			}
			// else if (tag instanceof ScreenFolderInfo)
			// {
			// ((ScreenFolderInfo) tag).mTitle = newName;
			// // 如果当前文件夹是打开的，则发消息更新编辑框的文字
			// if (((ScreenFolderInfo) tag).mOpened)
			// {
			// GoLauncher.sendMessage(this,
			// IDiyFrameIds.DESK_USER_FOLDER_FRAME,
			// DeskUserFolderFrame.UPDATE_FOLDER_NAME, -1,
			// newName, null);
			// }// end if
			// }

		}

		if (mDesktopSettingInfo != null && mDesktopSettingInfo.isShowTitle()) {
			((BubbleTextView) editingView).setText(newName);
		} else {
			((BubbleTextView) editingView).setText(null);
		}
	}

	void setItemIcon(View targetView, Drawable icon, boolean isUserIcon) {
		if (targetView != null) {
			ItemInfo targetInfo = (ItemInfo) targetView.getTag();
			if (targetInfo != null) {
				if (targetInfo instanceof ShortCutInfo) {
					((ShortCutInfo) targetInfo).mIcon = icon;
					((ShortCutInfo) targetInfo).mIsUserIcon = isUserIcon;

				} else if (targetInfo instanceof UserFolderInfo) {
					((UserFolderInfo) targetInfo).mIcon = icon;
					((UserFolderInfo) targetInfo).mIsUserIcon = isUserIcon;
					updateFolderIconAsync((UserFolderInfo) targetInfo, false, false);
				} else if (targetInfo instanceof ScreenLiveFolderInfo) {
					((ScreenLiveFolderInfo) targetInfo).mIcon = icon;
				}
			}
		}
	}

	private void resetDefaultIcon() {
		if (mDraggedItemId >= 0) {
			View editingView = ScreenUtils.getViewByItemId(mDraggedItemId,
					mWorkspace.getCurrentScreen(), mWorkspace);
			mDraggedItemId = -1;

			if (editingView != null) {
				if (editingView.getTag() instanceof RelativeItemInfo) {
					RelativeItemInfo tagInfo = (RelativeItemInfo) editingView.getTag();
					BitmapDrawable iconDrawable = null;
					if (null != tagInfo.getRelativeItemInfo()) {
						iconDrawable = tagInfo.getRelativeItemInfo().getIcon();
					}
					if (tagInfo instanceof FeatureItemInfo) {
						((FeatureItemInfo) tagInfo).resetFeature();
					}

					updateDesktopItem(tagInfo.mScreenIndex, tagInfo);
					if (null != iconDrawable) {
						((BubbleTextView) editingView).setIcon(iconDrawable);
						setItemIcon(editingView, iconDrawable, false);
					} else {
						updateIconAndTitle(tagInfo, editingView);
					}
				}
			}
		}
	}

	private void actionChangeIcon(Bundle iconBundle) {
		if (mDraggedItemId >= 0) {
			View editingView = ScreenUtils.getViewByItemId(mDraggedItemId,
					mWorkspace.getCurrentScreen(), mWorkspace);
			mDraggedItemId = -1;

			if ((editingView != null) && (editingView instanceof BubbleTextView)
					&& (iconBundle != null)) {
				ItemInfo tagInfo = (ItemInfo) editingView.getTag();
				if (tagInfo == null) {
					Log.i(LOG_TAG, "change icon fail tagInfo == null");
					return;
				}

				Drawable iconDrawable = null;
				boolean isDefaultIcon = false;
				int type = iconBundle.getInt(ImagePreviewResultType.TYPE_STRING);
				if (ImagePreviewResultType.TYPE_RESOURCE_ID == type) {
					int id = iconBundle.getInt(ImagePreviewResultType.IMAGE_ID_STRING);
					iconDrawable = mActivity.getResources().getDrawable(id);

					if (tagInfo instanceof FeatureItemInfo) {
						((FeatureItemInfo) tagInfo).setFeatureIcon(iconDrawable, type, null, id,
								null);
						updateDesktopItem(tagInfo.mScreenIndex, tagInfo);
					}
				} else if (ImagePreviewResultType.TYPE_IMAGE_FILE == type) {
					if (tagInfo instanceof FeatureItemInfo) {
						String path = iconBundle
								.getString(ImagePreviewResultType.IMAGE_PATH_STRING);
						((FeatureItemInfo) tagInfo).setFeatureIcon(null, type, null, 0, path);
						if (((FeatureItemInfo) tagInfo).prepareFeatureIcon()) {
							iconDrawable = ((FeatureItemInfo) tagInfo).getFeatureIcon();
							updateDesktopItem(tagInfo.mScreenIndex, tagInfo);
						}
					}
				} else if (ImagePreviewResultType.TYPE_IMAGE_URI == type) {
					if (tagInfo instanceof FeatureItemInfo) {
						String path = iconBundle
								.getString(ImagePreviewResultType.IMAGE_PATH_STRING);
						((FeatureItemInfo) tagInfo).setFeatureIcon(null, type, null, 0, path);
						if (((FeatureItemInfo) tagInfo).prepareFeatureIcon()) {
							iconDrawable = ((FeatureItemInfo) tagInfo).getFeatureIcon();
							updateDesktopItem(tagInfo.mScreenIndex, tagInfo);
						}
					}
				} else if (ImagePreviewResultType.TYPE_PACKAGE_RESOURCE == type
						|| ImagePreviewResultType.TYPE_APP_ICON == type) {
					String packageStr = iconBundle
							.getString(ImagePreviewResultType.IMAGE_PACKAGE_NAME);
					String path = iconBundle.getString(ImagePreviewResultType.IMAGE_PATH_STRING);

					ImageExplorer imageExplorer = ImageExplorer.getInstance(mActivity);
					iconDrawable = imageExplorer.getDrawable(packageStr, path);

					if (null != iconDrawable) {
						if (iconDrawable instanceof NinePatchDrawable) {
							Toast.makeText(mActivity,
									R.string.folder_change_ninepatchdrawable_toast,
									Toast.LENGTH_LONG).show();
							return;
						}
						if (tagInfo instanceof FeatureItemInfo) {
							((FeatureItemInfo) tagInfo).setFeatureIcon(iconDrawable, type,
									packageStr, 0, path);
							updateDesktopItem(tagInfo.mScreenIndex, tagInfo);
						}
					}
				} else {
					BitmapDrawable bmp = null;
					if (tagInfo instanceof RelativeItemInfo) {
						bmp = ((RelativeItemInfo) tagInfo).getRelativeItemInfo().getIcon();
					}

					if (null != bmp) {
						bmp.setTargetDensity(mActivity.getResources().getDisplayMetrics());
						iconDrawable = bmp;
						isDefaultIcon = true;
					}

					if (tagInfo instanceof FeatureItemInfo) {
						((FeatureItemInfo) tagInfo).resetFeature();
						updateDesktopItem(tagInfo.mScreenIndex, tagInfo);
					}
				}

				if (iconDrawable == null) {
					Toast.makeText(mActivity, R.string.save_image_error, Toast.LENGTH_LONG).show();
					return;
				}

				((BubbleTextView) editingView).setIcon(iconDrawable);
				setItemIcon(editingView, iconDrawable, !isDefaultIcon);
			}
		}
	}

	private void actionUninstall(View editView) {
		if (editView == null) {
			return;
		}

		if ((editView instanceof BubbleTextView) || (editView instanceof TextView)) {
			if (editView.getTag() instanceof ShortCutInfo) {
				ShortCutInfo shortCutInfo = (ShortCutInfo) editView.getTag();
				if (shortCutInfo.mIntent != null) {
					final ComponentName componentName = shortCutInfo.mIntent.getComponent();
					if (componentName != null) {
						try {
							// go主题和go精品假图标提示用户不能删除
							if (ICustomAction.ACTION_FUNC_SPECIAL_APP_GOSTORE
									.equals(shortCutInfo.mIntent.getAction())
									|| ICustomAction.ACTION_FUNC_SPECIAL_APP_GOTHEME
											.equals(shortCutInfo.mIntent.getAction())
									|| ICustomAction.ACTION_FUNC_SPECIAL_APP_GOWIDGET
											.equals(shortCutInfo.mIntent.getAction())) {
								ScreenUtils.showToast(R.string.uninstall_fail, mActivity);
							} else {
								AppUtils.uninstallPackage(mActivity, componentName.getPackageName());
							}
						} catch (Exception e) {
							// 处理卸载异常
							ScreenUtils.showToast(R.string.uninstall_fail, mActivity);
						}
					}
				} else {
					// 卸载失败
					ScreenUtils.showToast(R.string.uninstall_fail, mActivity);
				}
			}
		}
	}

	private void actionChangeWidgetSkin(ScreenAppWidgetInfo widgetInfo) {
		if (widgetInfo != null && GoWidgetManager.isGoWidget(widgetInfo.mAppWidgetId)) {
			GoWidgetBaseInfo info = AppCore.getInstance().getGoWidgetManager()
					.getWidgetInfo(widgetInfo.mAppWidgetId);

			if (info != null) {
				GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						IFrameworkMsgId.SHOW_FRAME, IDiyFrameIds.WIDGET_THEME_CHOOSE, null, null);

				GoLauncher.sendMessage(this, IDiyFrameIds.WIDGET_THEME_CHOOSE,
						IDiyMsgIds.WIDGETCHOOSE_SKIN, -1, info, null);
			}
		}
	}

	private boolean acceptFilter() {
		final InputMethodManager inputManager = (InputMethodManager) mActivity
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		return !inputManager.isFullscreenMode();
	}

	public String getTypedText() {
		return mDefaultKeySsb.toString();
	}

	private void clearTypedText() {
		mDefaultKeySsb.clear();
		mDefaultKeySsb.clearSpans();
		Selection.setSelection(mDefaultKeySsb, 0);
	}

	private boolean searchEventFilter(int keyCode, KeyEvent event) {
		if (acceptFilter() && keyCode != KeyEvent.KEYCODE_ENTER) {
			boolean gotKey = TextKeyListener.getInstance().onKeyDown(mWorkspace, mDefaultKeySsb,
					keyCode, event);
			if (gotKey && mDefaultKeySsb != null && mDefaultKeySsb.length() > 0) {
				// something usable has been typed - start a search
				// the typed text will be retrieved and cleared by
				// showSearchDialog()
				// If there are multiple keystrokes before the search dialog
				// takes focus,
				// onSearchRequested() will be called for every keystroke,
				// but it is idempotent, so it's fine.
				return showSearchDialog(null, false, null, true);
			}
		}
		return false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean handled = super.onKeyDown(keyCode, event);
		if (!handled) {
			final FolderView folderView = mWorkspace.getOpenFolder();
			if (keyCode == KeyEvent.KEYCODE_BACK) {
				ScreenUtils.closeFolderView(folderView);
				stopWidgetEdit();
				clearDragState();
				// mWorkspace.smallToNormal();
				handled = true;
			} else if (keyCode == KeyEvent.KEYCODE_MENU) {
				// 文件夹打开时屏幕菜单键
				handled = (folderView == null) ? false : true;
			}
		}
		return handled;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		FolderView folderView = mWorkspace.getOpenFolder();
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			// 文件夹打开时屏幕菜单键
			return (folderView == null) ? false : true;
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onSearchKeyDown(int keyCode, KeyEvent event) {
		return onKeyDown(keyCode, event);
	}

	@Override
	public boolean onSearchKeyUp(int keyCode, KeyEvent event) {
		return onKeyUp(keyCode, event);
	}

	@Override
	public boolean onSearchKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
		return onKeyMultiple(keyCode, repeatCount, event);
	}

	@Override
	public boolean showSearchDialog(String initialQuery, boolean selectInitialQuery,
			Bundle appSearchData, boolean globalSearch) {
		Bundle bundle = new Bundle();
		if (initialQuery == null) {
			initialQuery = getTypedText();
			clearTypedText();
		}
		bundle.putString(Search.FIELD_INITIAL_QUERY, initialQuery);
		bundle.putBoolean(Search.FIELD_SELECT_INITIAL_QUERY, selectInitialQuery);
		bundle.putBundle(Search.FIELD_SEARCH_DATA, appSearchData);
		bundle.putBoolean(Search.FIELD_GLOBAL_SEARCH, globalSearch);
		return GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
				IDiyMsgIds.SHOW_SEARCH_DIALOG, -1, bundle, null);
	}

	private void addSearchWidget() {
		Search search = (Search) mInflater.inflate(R.layout.widget_search, null);
		search.setSearchEventListener(this);
		ScreenAppWidgetInfo searchWidgetInfo = new ScreenAppWidgetInfo(
				ICustomWidgetIds.SEARCH_WIDGET);

		// 固定长宽
		final int spanX = 4;
		final int spanY = 1;
		searchWidgetInfo.mSpanX = spanX;
		searchWidgetInfo.mSpanY = spanY;

		int[] xy = new int[2];
		boolean vacant = ScreenUtils.findVacant(xy, spanX, spanY, mWorkspace.getCurrentScreen(),
				mWorkspace);
		if (!vacant) {
			setScreenRedBg();
		} else {
			searchWidgetInfo.mCellX = xy[0];
			searchWidgetInfo.mCellY = xy[1];
			search.setTag(searchWidgetInfo);

			mWorkspace.addInCurrentScreen(search, xy[0], xy[1], spanX, spanY, false);
			addDesktopItem(mWorkspace.getCurrentScreen(), searchWidgetInfo);
		}
	}

	// 创建自定义widget
	private View createCustomWidget(ScreenAppWidgetInfo widgetInfo) {
		if (widgetInfo == null) {
			ScreenMissIconBugUtil.showToast(ScreenMissIconBugUtil.ERROR_CUSTOM_WIDGET_INFO_NULL);
			return null;
		}

		// 搜索widget
		if (widgetInfo.mAppWidgetId == ICustomWidgetIds.SEARCH_WIDGET) {
			Search search = null;
			try {
				search = (Search) mInflater.inflate(R.layout.widget_search, null);
				widgetInfo.mHostView = search;
				if (search != null) {
					search.setSearchEventListener(this);
					search.setTag(widgetInfo);
					return search;
				}
			} catch (InflateException e) {
				ScreenMissIconBugUtil.showToast(ScreenMissIconBugUtil.ERROR_SERCH_INFLATEEXCEPTION);
			}
		} else // 创建GOWidget
		{
			return createGoWidget(widgetInfo);
		}
		return null;
	}

	private View createGoWidget(ScreenAppWidgetInfo widgetInfo) {
		GoWidgetManager goWidgetManager = AppCore.getInstance().getGoWidgetManager();
		View goWidgetView = goWidgetManager.createView(widgetInfo.mAppWidgetId);
		if (goWidgetView != null) {
			widgetInfo.mHostView = goWidgetView;
			goWidgetView.setTag(widgetInfo);
		} else {
			ScreenMissIconBugUtil.showToast(ScreenMissIconBugUtil.ERROR_CREATE_CUSTOM_NULL);
		}
		return goWidgetView;
	}

	private void createHostViewAndBrocast(final ScreenAppWidgetInfo sawInfo,
			final AppWidgetProviderInfo awpInfo) {
		try {
			AppWidgetHostView widgetView = mWidgetHost.createView(mActivity, sawInfo.mAppWidgetId,
					awpInfo);
			if (widgetView != null) {
				sawInfo.mHostView = widgetView;
				widgetView.setTag(sawInfo);

				int[] span = new int[] { sawInfo.mSpanX, sawInfo.mSpanY };
				// broadcast add widget
				ScreenUtils.appwidgetReadyBroadcast(sawInfo.mAppWidgetId, awpInfo.provider, span,
						mActivity);
				span = null;
			}
		} catch (Exception e) {
			Log.i(LOG_TAG, "createHostViewAndBrocast fail, AppWidgetProviderInfo = " + awpInfo);
			// e.printStackTrace();
		}
	}

	private View filterWidgetView(final ScreenAppWidgetInfo widgetInfo) {
		OutOfMemoryHandler.handle();
		final int widgetId = widgetInfo.mAppWidgetId;

		if (widgetId < 0) // 自定义的widget
		{
			return createCustomWidget(widgetInfo);
		} else // 标准widget
		{
			AppWidgetProviderInfo info = mWidgetManager.getAppWidgetInfo(widgetId);
			if (info == null && widgetInfo.mProviderIntent != null) {
				// 如果取不到, 重新获取一个新的widgetid，再次绑定
				int newId = 0;
				try {
					// 申请新的widgetid
					newId = mWidgetHost.allocateAppWidgetId();
					widgetInfo.mAppWidgetId = newId;

					final ComponentName provider = widgetInfo.mProviderIntent.getComponent();
					if (provider != null) {
						AppWidgetManagerWrapper.bindAppWidgetId(mWidgetManager, newId, provider);
					}

					info = mWidgetManager.getAppWidgetInfo(newId);

					// 更新到数据库
					mControler.updateDBItem(widgetInfo);
				} catch (RuntimeException e) {
					Log.e(LOG_TAG, "Problem binding appWidgetId " + newId);
					if (newId > 0) {
						mWidgetHost.deleteAppWidgetId(newId);
					}
				}
			}
			createHostViewAndBrocast(widgetInfo, info);
			return widgetInfo.mHostView;
		}
	}

	private BubbleTextView inflateBubbleTextView(CharSequence title, Drawable icon, ItemInfo tag) {
		BubbleTextView bubble = null;
		// OutOfMemoryHandler.gcIfAllocateOutOfHeapSize();
		// 为了解决一些图标比正常图标位置高出的问题，将布局文件进行调整，无需系统自行选择 - by Yugi 2012.9.12
		final boolean isPort = GoLauncher.getOrientation() == Configuration.ORIENTATION_PORTRAIT;
		final int appLayoutId = GoLauncher.isLargeIcon() ? isPort
				? R.layout.application_large_port
				: R.layout.application_large_land : isPort
				? R.layout.application_port
				: R.layout.application_land;
		try {
			bubble = (BubbleTextView) mInflater.inflate(appLayoutId,
					mWorkspace.getCurrentScreenView(), false);
		} catch (InflateException e) {
			ScreenMissIconBugUtil
					.showToast(ScreenMissIconBugUtil.ERROR_INFLATEBUBBLETEXT_INFLATEEXCEPTION);
		}

		if (bubble == null) {
			ScreenMissIconBugUtil.showToast(ScreenMissIconBugUtil.ERROR_BUBBLE_NULL);
			return null;
		}

		bubble.setIcon(icon);
		/* liyh 2010-11-23 12:00 增加判断是否需要显示名称 */
		if (mDesktopSettingInfo != null) {
			if (mDesktopSettingInfo.isShowTitle()) {
				bubble.setText(title);
			} else {
				bubble.setText(null);
			}
			bubble.setShowShadow(!mDesktopSettingInfo.isTransparentBg());
		}
		// customDeskTopBackground(bubble);
		bubble.setTag(tag);
		tag.registerObserver(bubble);
		return bubble;
	}

	private View addDesktopView(ItemInfo itemInfo, final int screenIndex, boolean sync) {
		final View view = createDesktopView(itemInfo, screenIndex, sync);
		if (view != null) {
			mWorkspace.post(new Runnable() {
				@Override
				public void run() {
					final ItemInfo info = (ItemInfo) view.getTag();
					if (info != null) {
						try {
							mWorkspace.addInScreen(view, screenIndex, info.mCellX, info.mCellY,
									info.mSpanX, info.mSpanY, false);
						} catch (IllegalStateException e) {
							// 由于多线程的原因，可能会导致同一个view被加入两次的问题，出现这种情况捕获该问题
							e.printStackTrace();
							Log.e("illegalstateException", "IllegalStateException add in screen");
						}
					}
				}
			});
		}
		return view;
	}

	private void showDockInDesktop(ItemInfo itemInfo, final int screenIndex, boolean sync) {
		if (null != mTmpDockView) {
			ItemInfo info = (ItemInfo) mTmpDockView.getTag();
			info.unRegisterObserver((BubbleTextView) mTmpDockView);
			mWorkspace.removeInScreen(mTmpDockView, info.mScreenIndex);
			mTmpDockView.clearAnimation();
			mTmpDockView.setTag(null);
		}

		mTmpDockView = null;
		final View view = createDesktopView(itemInfo, screenIndex, sync);
		mTmpDockView = view;

		if (null == mDockAnimationAlpha) {
			mDockAnimationAlpha = new AlphaAnimation(1.0f, 0.1f);
			mDockAnimationAlpha.setDuration(1000);
			mDockAnimationAlpha.setRepeatCount(Animation.INFINITE);
			mDockAnimationAlpha.setRepeatMode(Animation.RESTART);
		} else {
			// mDockAnimation_Alpha.cancel();
			mDockAnimationAlpha.reset();
		}

		if (view != null) {
			// view.setAnimation(mDockAnimation_Alpha);
			final ItemInfo info = (ItemInfo) view.getTag();
			if (info != null) {
				mWorkspace.previewDockItem(view, screenIndex, info.mCellX, info.mCellY,
						info.mSpanX, info.mSpanY);
			}
			view.startAnimation(mDockAnimationAlpha);
		}
	}

	private synchronized View createDesktopView(ItemInfo itemInfo, int screenIndex, boolean sync) {
		if (itemInfo == null) {
			PreferencesManager pm = new PreferencesManager(mActivity);
			pm.putBoolean("need_to_send_db", true);
			pm.commit();
			throw new RuntimeException("itemInfo is null");
			//			ScreenMissIconBugUtil
			//					.showToast(ScreenMissIconBugUtil.ERROR_CREATEDESKTOPVIEW_APP_INFO_NULL);
			//			return null;
		}

		View addView = null;
		itemInfo.mScreenIndex = screenIndex;
		switch (itemInfo.mItemType) {
			case IItemType.ITEM_TYPE_APPLICATION :
				addView = createShortcutIcon(itemInfo, sync);
				break;

			case IItemType.ITEM_TYPE_SHORTCUT :
				addView = createShortcutIcon(itemInfo, true);
				break;

			case IItemType.ITEM_TYPE_APP_WIDGET :
				addView = createAppWidgetView(itemInfo);
				break;

			case IItemType.ITEM_TYPE_LIVE_FOLDER :
				addView = createLiveFolder(itemInfo);
				break;

			case IItemType.ITEM_TYPE_USER_FOLDER :
				addView = createUserFolder(itemInfo);
				break;

			case IItemType.ITEM_TYPE_FAVORITE :
				addView = createFavoriteView(itemInfo);
				break;

			default :
				break;
		}
		return addView;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void onBCChange(int msgId, int param, Object object, List objects) {
		switch (msgId) {
			case IDiyMsgIds.APPCORE_DATACHANGE : {
				handleAppCoreChange(param, object);
				break;
			}
			default :
				break;
		}
	}

	/**************** 文件夹 相关 *******************************/
	private void handleFolderClick(ScreenFolderInfo folderInfo) {
		FolderView openFolder = mWorkspace.getFolderForTag(folderInfo);
		if (openFolder == null) {
			openFolder(folderInfo);
		} else {
			final int folderScreen = mWorkspace.getScreenForView(openFolder);
			if (folderScreen != mWorkspace.getCurrentScreen()) {
				// Close any folder open on the current screen
				closeFolder();

				// Pull the folder onto this screen
				openFolder(folderInfo);
			}
		}
	}

	private void closeAllFolders() {
		List<FolderView> list = mWorkspace.getAllOpenFolders();
		final int size = list.size();
		for (int i = 0; i < size; i++) {
			ScreenUtils.closeFolderView(list.get(i));
		}

		// 当前屏幕获得焦点
		final CellLayout screen = mWorkspace.getCurrentScreenView();
		if (screen != null) {
			screen.requestFocus();
		}
	}

	private void closeFolder() {
		FolderView folderView = mWorkspace.getOpenFolder();
		ScreenUtils.closeFolderView(folderView);

	}

	private void openFolder(ScreenFolderInfo folderInfo) {

		if (folderInfo instanceof UserFolderInfo) {
			OutOfMemoryHandler.gcIfAllocateOutOfHeapSize();
			try {
				GoLauncher
						.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IFrameworkMsgId.SHOW_FRAME,
								IDiyFrameIds.DESK_USER_FOLDER_FRAME, null, null);
				// 另外发一个消息，传送folderInfo给文件夹
				GoLauncher.sendMessage(this, IDiyFrameIds.DESK_USER_FOLDER_FRAME,
						IDiyMsgIds.USER_FOLDER_ADD_INFO, -1, folderInfo, null);
			} catch (InflateException e) {
			}
		} else if (folderInfo instanceof ScreenLiveFolderInfo) {
			OutOfMemoryHandler.gcIfAllocateOutOfHeapSize();
			FolderView openFolder = null;

			try {
				openFolder = LiveFolder.fromXml(mActivity, folderInfo);
			} catch (InflateException e) {
			}
			if (openFolder == null) {
				return;
			}

			// 必须先设置消息处理者，再绑定数据
			openFolder.setMessageHandler(this);
			openFolder.bind(folderInfo);
			folderInfo.mOpened = true;

			mWorkspace.addInScreen(openFolder, mWorkspace.getCurrentScreen(), 0, 0,
					mWorkspace.getDesktopColumns(), mWorkspace.getDesktopRows(), false);

			openFolder.onOpen();
			openFolder.setActivity(mActivity);
		}

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void handleFolderEvent(int msgId, Object object, List objList) {
		switch (msgId) {
			case IScreenFolder.CLOSE_FOLDER :
				closeFolder();
				break;

			case IScreenFolder.LONG_PRESS_TITLE :
				break;

			case IScreenFolder.START_DRAG : {
				startDrag((View) object, null, DragFrame.TYPE_SCREEN_FOLDER_DRAG);
				if (objList != null && objList.size() > 0) {
					mCurrentFolderInfo = ((ArrayList<ScreenFolderInfo>) objList).get(0);
				}
			}
				break;

			case IScreenFolder.START_ACTIVITY :
				if (object instanceof Intent) {
					// 点击live folder的item
					GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
							IDiyMsgIds.START_ACTIVITY, -1, object, null);
				} else if (object instanceof View) {
					launchApp((View) object);
				}
				break;

			case IScreenFolder.START_MANAGING_CURSOR :
				if (object instanceof Cursor) {
					mActivity.startManagingCursor((Cursor) object);
				}
				break;

			case IScreenFolder.STOP_MANAGING_CURSOR :
				if (object instanceof Cursor) {
					mActivity.stopManagingCursor((Cursor) object);
				}
				break;

			case IScreenFolder.QUERY_CURSOR :
				if (objList != null) {
					Cursor cursor = LiveFolderAdapter.query(mActivity,
							(ScreenLiveFolderInfo) object);
					objList.add(cursor);
				}
				break;

			default :
				break;
		}
	}

	private boolean isMoveToFolder(View targetView, Rect rect) {
		if (targetView == null || mIsShowPreview || null == rect) {
			// 设置进入预览的标识为false，否则下次将无法拖动图标进文件夹
			mIsShowPreview = false;
			return false;
		}

		Object tagObject = targetView.getTag();
		if (tagObject == null || !(tagObject instanceof ShortCutInfo)) {
			return false;
		}

		final int screenIndex = mWorkspace.getCurrentScreen();
		CellLayout currentScreen = (CellLayout) mWorkspace.getChildAt(screenIndex);
		if (currentScreen == null) {
			return false;
		}

		Rect childRect = new Rect();
		for (int i = 0; i < currentScreen.getChildCount(); i++) {
			View childView = currentScreen.getChildAt(i);
			// 目标文件夹必须与打开的文件夹不同
			if (childView instanceof FolderIcon) {
				childView.getHitRect(childRect);
				int centerX = rect.centerX();
				int centerY = rect.centerY();
				if (Workspace.sLayoutScale < 1.0f) {
					float[] realXY = new float[2];
					Workspace.virtualPointToReal(centerX, centerY, realXY);
					centerX = (int) realXY[0];
					centerY = (int) realXY[1];
				}
				// 目标区域落在文件夹上面
				if (childRect.contains(centerX, centerY)) {
					// 关闭弹出菜单
					hideQuickActionMenu(true);
					ShortCutInfo itemInfo = (ShortCutInfo) tagObject;
					if (mDragType == DragFrame.TYPE_SCREEN_FOLDER_DRAG) {
						// 从文件夹拖出來的，还要删除文件夹内的图标
						if (mCurrentFolderInfo != null) {
							if (mCurrentFolderInfo == (UserFolderInfo) childView.getTag()) {
								return true;
							}

							ItemInfo tagInfo = (ItemInfo) tagObject;
							if (null != mControler) {
								mControler.moveDesktopItemFromFolder(tagInfo, screenIndex,
										mCurrentFolderInfo.mInScreenId);
								GoLauncher.sendMessage(this, IDiyFrameIds.DESK_USER_FOLDER_FRAME,
										IDiyMsgIds.DELETE_CACHE_INFO_IN_FOLDER, -1, tagInfo, null);
							}

							// 更新缓存
							if (mCurrentFolderInfo instanceof UserFolderInfo) {
								((UserFolderInfo) mCurrentFolderInfo).remove(tagInfo.mInScreenId);
								if (!deleteFolderOrNot((UserFolderInfo) mCurrentFolderInfo, true)) {
									// if (tagInfo instanceof ShortCutInfo) {
									// int type =
									// AppIdentifier.whichTypeOfNotification(mActivity,
									// (( ShortCutInfo ) tagInfo).mIntent);
									// if (type !=
									// NotificationType.IS_NOT_NOTIFICSTION) {
									// (( UserFolderInfo )
									// mCurrentFolderInfo).mTotleUnreadCount -=
									// (( ShortCutInfo ) tagInfo).mCounter;
									// }
									// }
									// 更新文件夹图标
									updateFolderIconAsync((UserFolderInfo) mCurrentFolderInfo,
											false, false);
								}
							}
							mCurrentFolderInfo = null;
						}
					} else {
						if (targetView instanceof BubbleTextView) {
							if (screenIndex == itemInfo.mScreenIndex) {
								currentScreen.removeView(targetView);
							} else {
								deleteItem(itemInfo, -1);
							}
						}
					}

					if (targetView instanceof DockIconView) {
						dragToFolder((ShortCutInfo) tagObject, (UserFolderInfo) childView.getTag());
					} else {
						if (targetView instanceof BubbleTextView) {
							itemInfo.unRegisterObserver((BubbleTextView) targetView);
						}

						UserFolderInfo folderInfo = (UserFolderInfo) childView.getTag();
						// 如果是从文件夹拖出去再拖进来的就不需要添加到数据库中
						if (null != mControler) {
							// 图标去重
							mControler.removeItemsFromFolder(folderInfo, itemInfo);
							// 先修改数据库
							mControler.moveDesktopItemToFolder(itemInfo, folderInfo.mInScreenId);
							// 后添加到缓存
							folderInfo.add(itemInfo);

							// int type =
							// AppIdentifier.whichTypeOfNotification(mActivity,
							// itemInfo.mIntent);
							// if (type != NotificationType.IS_NOT_NOTIFICSTION)
							// {
							// folderInfo.mTotleUnreadCount +=
							// itemInfo.mCounter;
							// }
						}
					}
					
					//文件夹智能命名-场景：拖动图标到未命名文件夹
					String defaultname = mActivity.getResources().getString(
							R.string.folder_name);
					if (((UserFolderInfo) childView.getTag()).mTitle
							.equals(defaultname)) {
						ArrayList<AppItemInfo> infoList = new ArrayList<AppItemInfo>();
						for (int j = 0; j < ((UserFolderInfo) childView
								.getTag()).getChildCount(); j++) {
							ShortCutInfo item = ((UserFolderInfo) childView
									.getTag()).getChildInfo(j);
							if (item != null) {
								infoList.add(item.getRelativeItemInfo());
							}
						}
						String smartfoldername = CommonControler.getInstance(
								mActivity).generateFolderName(infoList);

						actionRename(
								smartfoldername,
								((UserFolderInfo) childView.getTag()).mInScreenId);
					}
					// 更新文件夹图标
					updateFolderIconAsync((UserFolderInfo) childView.getTag(), false, false);

					// 开始进入文件夹缩小动画
					if (null != childView.getTag() && childView.getTag() instanceof UserFolderInfo) {
						currentScreen.startIntoFolderZoomoutAnimation(targetView, childView, rect,
								((UserFolderInfo) childView.getTag()).getChildCount());
					}

					return true;
				}
			}
		}
		return false;
	}

	private boolean startDrag(View targetView, List<?> objects, int dragType) {
		// 显示弹出菜单
		if (isLoading()) {
			ScreenUtils.showToast(R.string.loading_screen, mActivity);
			mWorkspace.clearDragState();
			Log.i(LOG_TAG, "loading... can not drag icon");
			return false;
		}

		mWorkspace.mDragging = true;
		mDontMoveDragObject = false;
		
		//boolean isDragTheLastDockAppdrawer = DockUtil.isTheLastDockAppdrawer(targetView);
		if (!StatusBarHandler.isHide() && Workspace.getLayoutScale() >= 1.0f
				&& dragType != DragFrame.TYPE_SCREEN_FOLDER_DRAG
				&& dragType != DragFrame.TYPE_DOCK_FOLDERITEM_DRAG) {
			// 要求显示全屏并重新排版
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
					IDiyMsgIds.SHOW_HIDE_STATUSBAR, -2, true, null);
		}

		mWorkspace.setCellLayoutGridState(mWorkspace.getCurrentScreen(), true, -1);
		mDragType = dragType;

		// 长按桌面图标提示，前三次提示  add by zhengxiangcan
		if (targetView != null && targetView.getTag() instanceof ItemInfo) {
			ItemInfo info = (ItemInfo) targetView.getTag();
			// 当长按图标为桌面应用的图标（非文件夹、widget）
			if (info != null && dragType == DragFrame.TYPE_SCREEN_ITEM_DRAG) {
				if (info.mItemType == IItemType.ITEM_TYPE_APPLICATION
						|| info.mItemType == IItemType.ITEM_TYPE_SHORTCUT) {
					PreferencesManager manager = new PreferencesManager(mActivity,
							IPreferencesIds.DESK_SHAREPREFERENCES_FILE, Context.MODE_PRIVATE);
					int count = manager.getInt(IPreferencesIds.SCREEN_LONG_PRESS_TIP, 0);
					if (count < 3) {
						count++;
						manager.putInt(IPreferencesIds.SCREEN_LONG_PRESS_TIP, count);
						manager.commit();
						ScreenUtils.showToast(R.string.screen_long_press_new_tip, mActivity,
								Toast.LENGTH_LONG);
					} // end if count
				}
			} // end if info
		} // end if targetView

		// mDragFromFolder = fromFolder;
		GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IFrameworkMsgId.SHOW_FRAME,
				IDiyFrameIds.DRAG_FRAME, null, null);

		boolean trashGone = mDragType == DragFrame.TYPE_SCREEN_FOLDER_DRAG
				|| mDragType == DragFrame.TYPE_DOCK_FOLDERITEM_DRAG
				|| mDragType == DragFrame.TYPE_ADD_APP_DRAG;
		if (trashGone) {
			GoLauncher.sendMessage(this, IDiyFrameIds.DRAG_FRAME, IDiyMsgIds.TRASH_GONE, -1, null,
					null);
			showIndicator();
		}
		boolean ret = GoLauncher.sendMessage(this, IDiyFrameIds.DRAG_FRAME, IDiyMsgIds.DRAG_START,
				mDragType, targetView, objects);
		if (ret) {
			mWorkspace.lock();
			// // 发送文件夹位置信息
			// GoLauncher.sendMessage(this, IDiyFrameIds.DRAG_FRAME,
			// IDiyMsgIds.SCREEN_FOLDER_AREA_LIST, -1, -1,
			// mWorkspace.getFolderRects(mWorkspace.getCurrentScreen(), null));
		}

		return true;
	}

	// public void sendFolderAreaInfo(int screenIndex) {
	// mWorkspace.lock();
	// mWorkspace.mDragging = true;
	// mWorkspace.setCellLayoutGridState(screenIndex, true, -1);
	// if(!StatusBarHandler.isHide() && Workspace.getLayoutScale() >= 1.0f){
	// //要求显示全屏并重新排版
	// GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
	// IDiyMsgIds.SHOW_HIDE_STATUSBAR,
	// -2, true, null);
	// }
	// // 发送文件夹位置信息
	// GoLauncher
	// .sendMessage(
	// this,
	// IDiyFrameIds.DRAG_FRAME,
	// IDiyMsgIds.SCREEN_FOLDER_AREA_LIST,
	// -1,
	// -1,
	// mWorkspace.getFolderRects(
	// mWorkspace.getCurrentScreen(), null));
	// }

	private boolean installShortcut(ShortCutInfo shortCutInfo) {
		if (shortCutInfo == null) {
			return false;
		}

		boolean ret = false;
		final int currnetScreen = mWorkspace.getCurrentScreen();
		int[] xy = new int[2];
		boolean isExistVacant = ScreenUtils.findVacant(xy, shortCutInfo.mSpanX,
				shortCutInfo.mSpanY, currnetScreen, mWorkspace);

		final int screenCount = mWorkspace.getChildCount();
		int screen = currnetScreen;
		if (!isExistVacant) {
			for (int i = 0; i < screenCount; i++) {
				if (i != currnetScreen) {
					isExistVacant = ScreenUtils.findVacant(xy, shortCutInfo.mSpanX,
							shortCutInfo.mSpanY, i, mWorkspace);

					if (isExistVacant) {
						screen = i;
						break;
					}
				}
			}
		}

		if (isExistVacant) {
			shortCutInfo.mCellX = xy[0];
			shortCutInfo.mCellY = xy[1];
			final View view = createDesktopView(shortCutInfo, screen, true);
			if (view != null) {
				final ItemInfo info = (ItemInfo) view.getTag();
				if (info != null) {
					try {
						mWorkspace.addInScreen(view, screen, info.mCellX, info.mCellY, info.mSpanX,
								info.mSpanY, false);
					} catch (IllegalStateException e) {
						e.printStackTrace();
						Log.e("illegalstateException", "IllegalStateException add in screen");
					}
				}
			}
			// addDesktopView(shortCutInfo, screen, true);
			addDesktopItem(screen, shortCutInfo);
			ret = true;
		}
		return ret;
	}

	/**
	 * 当把位置为src的项移动到dst位置时，计算原来在cur位置的项的新位置
	 * 
	 * @return
	 */

	private void moveScreen(Bundle bundle) {
		final int srcScreenIndex = bundle.getInt(SensePreviewFrame.FIELD_SRC_SCREEN);
		final int destScreenIndex = bundle.getInt(SensePreviewFrame.FIELD_DEST_SCREEN);
		final int currentScreen = mPreCurrentScreen;
		final int homeScreen = mPreHomeScreen;
		final int newCurrentScreen = ScreenUtils.computeIndex(currentScreen, srcScreenIndex,
				destScreenIndex);
		mPreNewCurrentScreen = newCurrentScreen;
		final int newHomeScreen = ScreenUtils.computeIndex(homeScreen, srcScreenIndex,
				destScreenIndex);

		// 如果目标位置位于目的位置之后，索引需-1
		CellLayout cellLayout = (CellLayout) mWorkspace.getChildAt(srcScreenIndex);
		mWorkspace.removeView(cellLayout);
		int realDestIndex = destScreenIndex;
		if (destScreenIndex >= mWorkspace.getChildCount()) {
			// 添加到最后
			realDestIndex = -1;
		}
		mWorkspace.addView(cellLayout, realDestIndex);

		// 做数据库操作
		if (null != mControler) {
			mControler.moveScreen(srcScreenIndex, destScreenIndex);
		}

		if (newCurrentScreen != currentScreen) {
			// 防止由于背景切换速度慢而造成的索引不匹配的情况
			// mWorkspace.setCurrentScreen(newCurrentScreen);
		}

		if (newHomeScreen != homeScreen) {
			mWorkspace.setMainScreen(newHomeScreen);
			if (mControler != null && mScreenSettingInfo != null
					&& mScreenSettingInfo.mMainScreen != newHomeScreen) {
				mScreenSettingInfo.mMainScreen = newHomeScreen;
				GOLauncherApp.getSettingControler().updateScreenSettingInfo(mScreenSettingInfo,
						false);
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mInitWorkspace) {
			int screenIndex = mWorkspace.getCurrentScreen();
			ScreenUtils.pauseGoWidget(screenIndex, mDesktopItems);
			fireWidgetVisible(false, screenIndex);
		}
		mBackWorkspace.onStateMethod(BackWorkspace.STATE_ON_PAUSE);
//		mWorkspace.cancleAlarm();
	}

	@Override
	protected void onStop() {
		super.onStop();
		mBackWorkspace.onStateMethod(BackWorkspace.STATE_ON_PAUSE);
//		mWorkspace.cancleAlarm();
	}
	
	@Override
	public void onResume() {
		//防止一些界面中按了返回键回到桌面还需要按两次Home键才能返回主屏
		mGotoMainScreen = 0;
		boolean hideIndicator = mEnterPrew;
		if (hideIndicator) {
			mLayout.getIndicator().hide();
		} else {
			mLayout.getIndicator().show();
		}
		closeFolder();
		if (mInitWorkspace) {
			int screenIndex = mWorkspace.getCurrentScreen();
			ScreenUtils.resumeGoWidget(screenIndex, mDesktopItems);
			fireWidgetVisible(true, screenIndex);
		}
		StatisticsData.sSCREEN_COUNT = mWorkspace.getChildCount();
		if (mWaitingForResume != null) {
			mWaitingForResume.setStayPressed(false);
		}
		mWorkspace.setCellLayoutGridState(mWorkspace.getCurrentScreen(), false, -1);
		mBackWorkspace.onStateMethod(BackWorkspace.STATE_ON_RESUME);
//		mWorkspace.addState(AlarmHandler.SCREEN_ONRESUME_MASK);
	}

	private void startListening() {
		try {
			mWidgetHost.startListening();
			registGoWidgetAction();
		} catch (Throwable e) {
			// 保护Widget可能导致的异常，如内存溢出
		}
	}

	private void stopListening() {
		try {
			mWidgetHost.stopListening();
			unRegistGoWidgetAction();
		} catch (Throwable e) {
			// 保护Widget可能导致的异常，如内存溢出
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		stopDesktopLoader();
		cleanHandlerMsg();
		unRegistObserver();
		stopListening();
		mThemeSpreader.cancel();
		mResetStatusAndIndicator = null;
		// 清除drawable callback
		unbindDrawable();
		if (mWorkspace != null) {
			int screenCount = mWorkspace.getChildCount();
			for (int i = 0; i < screenCount; i++) {
				CellLayout child = (CellLayout) mWorkspace.getChildAt(i);
				child.recycleBitmap();
				child.removeAllViews();
			}
//			mWorkspace.destroyAlarm();
			mWorkspace.resetOrientation();
			mWorkspace.removeAllViews();
			mWorkspace.unregisterProvider();
			mWorkspace.unbindWidgetScrollableViews();
		}
		mBackWorkspace.onStateMethod(BackWorkspace.STATE_ON_DESTROY);
		
	}

	private void unbindDrawable() {
		if (mDesktopItems != null) {
			final int size = mDesktopItems.size();
			for (int i = 0; i < size; i++) {
				unbindObjectInScreen(i);
			}
		}
	}

	// 清除桌面注册到DeskItemInfo的View
	private void unRigistDesktopObject() {
		if (mDesktopItems != null) {
			final int size = mDesktopItems.size();
			for (int i = 0; i < size; i++) {
				ArrayList<ItemInfo> screenInfos = mDesktopItems.get(i);
				if (screenInfos != null) {
					int count = screenInfos.size();
					for (int j = 0; j < count; j++) {
						ItemInfo itemInfo = screenInfos.get(j);
						if (itemInfo != null) {
							// 仅清除到BubbleTextView的注册关系
							itemInfo.clearAllObserver();
						}
					}
				}
			}
		}
	}

	private void uninstallApp(Intent intent) {
		// 清理UI
		final int screenCount = mWorkspace.getChildCount();
		CellLayout layout = null;
		for (int i = 0; i < screenCount; i++) {
			layout = (CellLayout) mWorkspace.getChildAt(i);
			if (layout == null) {
				continue;
			}

			int index = 0;
			while (index < layout.getChildCount()) {
				final View childView = layout.getChildAt(index);
				if (childView != null) {
					final Object tag = childView.getTag();
					if (tag != null) {
						if (tag instanceof ShortCutInfo) {
							ShortCutInfo shortCutInfo = (ShortCutInfo) tag;
							/**
							 * resolved by dengdazhong date: 2012.7.27
							 * 修复：ADT-6899 添加一键锁屏快捷方式到桌面，卸载GO锁屏时没有清除该快捷方式
							 * 移除桌面图标时除了判断intent是否一致外
							 * ，还要判断他们是否属于同一个程序，因为有可能是快捷方式，被卸载程序的快捷方式也需要移除 if
							 * (ConvertUtils.intentCompare(intent,
							 * shortCutInfo.mIntent))
							 */
							if (ConvertUtils.intentCompare(intent, shortCutInfo.mIntent)
									|| ConvertUtils.isIntentsBelongSameApp(intent,
											shortCutInfo.mIntent)) {
								layout.removeView(childView);
								if (null != mControler) {
									mControler.removeDesktopItem((ShortCutInfo) tag);
								}
								continue;
							}
						} else if (tag instanceof UserFolderInfo) {
							final UserFolderInfo folderInfo = (UserFolderInfo) tag;
							ArrayList<ShortCutInfo> list = folderInfo.remove(intent);
							if (list.size() > 0) {
								updateFolderIconAsync(folderInfo, false, true);
							}
						}
					}
				}
				++index;
			}
		}

		// 清理数据
		if (mDesktopItems != null) {
			int sz = mDesktopItems.size();
			for (int i = 0; i < sz; i++) {
				ArrayList<ItemInfo> infos = mDesktopItems.get(i);
				if (null != infos) {
					int j = 0;
					while (j < infos.size()) {
						ItemInfo info = infos.get(j);
						if (info instanceof ShortCutInfo) {
							ShortCutInfo shortCutInfo = (ShortCutInfo) info;
							if (ConvertUtils.intentCompare(intent, shortCutInfo.mIntent)) {
								infos.remove(j);
								if (null != mControler) {
									mControler.removeDesktopItem(shortCutInfo);
								}
								continue;
							}
						} else if (info instanceof UserFolderInfo) {
							final UserFolderInfo folderInfo = (UserFolderInfo) info;
							folderInfo.remove(intent);
						}
						j++;
					}
				}
			}
		}
	}

	private void loadScreenInfo() {
		if (null != mControler) {
			mDesktopItems = mControler.loadScreen();
		}
	}

	private void addDesktopItem(int screenIndex, ItemInfo itemInfo) {
		if (null != mControler) {
			mControler.addDesktopItem(screenIndex, itemInfo);
			
			if (itemInfo != null && itemInfo.mItemType == IItemType.ITEM_TYPE_SHORTCUT) {
				AppItemInfo appItemInfo = ((ShortCutInfo) itemInfo).getRelativeItemInfo();
				if (appItemInfo != null) {
					CommonControler.getInstance(GOLauncherApp.getContext()).checkShortCutIsIsRecommend(
							appItemInfo); // 检查加入屏幕的快捷方式是否推荐应用
				}
			}
		}
	}

	private void updateDesktopItem(final int screenIndex, ItemInfo itemInfo) {
		if (null != mControler) {
			mControler.updateDesktopItem(screenIndex, itemInfo);
			mWorkspace.refreshSubView();
		}
	}

	private void updateIndicator(int type, Bundle bundle) {
		mLayout.getIndicator().updateIndicator(type, bundle);
	}

	
	/**
	 * 长按widget弹出大小调整编辑框
	 * @param widgetView
	 */
	private void editWdiget(View widgetView) {
		if (widgetView == null || mWorkspace == null) {
			return;
		}

		mIsWidgetEditMode = true;
		final CellLayout screen = mWorkspace.getCurrentScreenView();
		if (screen == null) {
			return;
		}

		// 显示widget编辑层
		GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IFrameworkMsgId.SHOW_FRAME,
				IDiyFrameIds.SCREEN_WIDGET_EDIT_FRAME, null, null);
		mWidgetView = widgetView;

		final float minw = CellLayout.sCellRealWidth; //每个小空格的宽度
		final float minh = CellLayout.sCellRealHeight; //每个小空格的高度

		ScreenAppWidgetInfo widgetInfo = (ScreenAppWidgetInfo) widgetView.getTag();

		final int screenRectLeft = CellLayout.getLeftPadding();
		final int screenRectTop = CellLayout.getTopPadding();
		final int screenPaddingRight = CellLayout.getRightPadding();
		final int screenPaddingBottom = CellLayout.getBottomPadding();

		final int screenWdith = screen.getWidth(); //屏幕宽度
		final int screenHeight = screen.getHeight(); //屏幕高度

		//屏幕所占的矩形区域
		final RectF screenRect = new RectF(screenRectLeft, screenRectTop, screenWdith
				- screenPaddingRight, screenHeight - screenPaddingBottom);

		final int widgetX = widgetInfo.mCellX * (int) minw;  //widget左边距 = 所在区域位置 X 每个空置的宽度
		final int widgetY = widgetInfo.mCellY * (int) minh;
		final float widgetWidth = widgetInfo.mSpanX * minw;
		final float widgetHeight = widgetInfo.mSpanY * minh;

		//widget所在的矩形区域
		RectF widgetRect = new RectF(widgetX, widgetY, widgetX + widgetWidth, widgetY
				+ widgetHeight);

		// 传递数据到widget编辑层
		ArrayList<RectF> list = new ArrayList<RectF>(2);
		list.add(WidgetEditFrame.SCREEN_RECT_INDEX, screenRect);
		list.add(WidgetEditFrame.WIDGET_RECT_INDEX, widgetRect);

		Bundle dataBundle = new Bundle();
		dataBundle.putBoolean(WidgetEditFrame.CIRCLE, false);
		dataBundle.putBoolean(WidgetEditFrame.MAINTAIN_RATIO, false);
		dataBundle.putFloat(WidgetEditFrame.MIN_WIDTH, minw);
		dataBundle.putFloat(WidgetEditFrame.MIN_HEIGHT, minh);

		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_WIDGET_EDIT_FRAME,
				IDiyMsgIds.WIDGET_EDIT_FRAME_SETUP, -1, dataBundle, list);
	}

	private void stopWidgetEdit() {
		if (!mIsWidgetEditMode) {
			mWidgetView = null;
			return;
		}

		mIsWidgetEditMode = false;
		if (mWidgetView != null) {
			Object tag = mWidgetView.getTag();
			if (tag != null && tag instanceof ScreenAppWidgetInfo) {
				ScreenAppWidgetInfo widgetInfo = (ScreenAppWidgetInfo) tag;

				// 更新数据库
				if (mWorkspace != null) {
					updateDesktopItem(mWorkspace.getCurrentScreen(), widgetInfo);
				}
			}
			mWidgetView = null;
		}

		// 隐藏 widget编辑层
		GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IFrameworkMsgId.REMOVE_FRAME,
				IDiyFrameIds.SCREEN_WIDGET_EDIT_FRAME, null, null);
	}

	private boolean validateRect(Rect rect) {
		if (mWidgetView == null || mWidgetView.getTag() == null) {
			return false;
		}

		boolean collision = false;
		ScreenAppWidgetInfo widgetInfo = (ScreenAppWidgetInfo) mWidgetView.getTag();
		collision = ScreenUtils.ocuppiedArea(mWorkspace.getCurrentScreen(),
				widgetInfo.mAppWidgetId, rect, mWorkspace);
		//没有冲突
		if (!collision) {
			//检查是直接删除widget重新添加一个还是只是更改当前widget布局宽高
			boolean isNeedChange = checkWidgetNeedChange(widgetInfo, rect);
			if (!isNeedChange) {
				final CellLayout.LayoutParams lp = (CellLayout.LayoutParams) mWidgetView
						.getLayoutParams();
				lp.cellX = rect.left;
				lp.cellY = rect.top;
				lp.cellHSpan = rect.right - rect.left;
				lp.cellVSpan = rect.bottom - rect.top;
				mWidgetView.setLayoutParams(lp);
	
				widgetInfo.mCellX = lp.cellX;
				widgetInfo.mCellY = lp.cellY;
				widgetInfo.mSpanX = lp.cellHSpan;
				widgetInfo.mSpanY = lp.cellVSpan;
				mWidgetView.setTag(widgetInfo);
	
				final int appWidgetId = widgetInfo.mAppWidgetId;
				// 系统widget通知 blur ui
				if (!GoWidgetManager.isGoWidget(appWidgetId)) {
					final Intent motosize = new Intent(ICustomAction.ACTION_SET_WIDGET_SIZE);
					final AppWidgetProviderInfo appWidgetInfo = mWidgetManager
							.getAppWidgetInfo(appWidgetId);
					if (appWidgetInfo != null) {
						motosize.setComponent(appWidgetInfo.provider);
					}
	
					motosize.putExtra("appWidgetId", appWidgetId);
					motosize.putExtra("com.motorola.blur.home.EXTRA_NEW_WIDGET", true);
	
					motosize.putExtra("spanX", lp.cellHSpan);
					motosize.putExtra("spanY", lp.cellVSpan);
	
					// send the broadcast
					mActivity.sendBroadcast(motosize);
				}
			}
		}

		return collision;
	}
	
	
	 /**
	  * 检查widget是否需要更改样式
	  * 只针对go天气和go任务管理器
	  * @param curwidgetView
	  * @param rect
	  * @return
	  */
	private boolean checkWidgetNeedChange(ScreenAppWidgetInfo curwidgetView, Rect rect) {
		try {
			//通过id获取当前widgt的基本信息
			GoWidgetManager goWidgetManager = AppCore.getInstance().getGoWidgetManager();
			GoWidgetBaseInfo curWidgetInfo = goWidgetManager.getWidgetInfo(curwidgetView.mAppWidgetId);
			if (null == curWidgetInfo) {
				return false;
			}
			
			//不用goWidgetBaseInfo.mPackage获取包名。因为go任务管理器要特殊处理
			String packageName = goWidgetManager.getWidgetPackage(curWidgetInfo); 
//			Log.i("lch", "packageName:" + packageName);
			if (packageName == null) {
				return false;
			}
			
			//获取包命对应widget的样式列表
		    ArrayList<WidgetParseInfo> widgetStyleList = ScreenUtils.getWidgetStyle(mActivity, packageName);

		    //获取widget对应的信息
		    GoWidgetProviderInfo widgetProviderInfo = ScreenUtils.getWidgetProviderInfo(mActivity, packageName);
			
		    
		    WidgetParseInfo newWidgetInfo = null;
			
		    //go天气
			if (packageName.equals(LauncherEnv.Plugin.RECOMMAND_GOWEATHEREX_PACKAGE)) {
				 //获取符合匹配的go天气样式
				newWidgetInfo = ScreenUtils.getWeatherWidgetStyle(widgetStyleList, widgetProviderInfo, rect, curwidgetView.mSpanX, curwidgetView.mSpanY, curWidgetInfo);
				
			}
			
			//go任务管理器,有旧版本和新版本2个
			else if (packageName.equals(LauncherEnv.Plugin.TASK_PACKAGE) || packageName.equals(LauncherEnv.Plugin.RECOMMAND_GOTASKMANAGER_PACKAGE)) {
				 //获取符合匹配的go天气样式
				newWidgetInfo = ScreenUtils.getTaskWidgetStyle(widgetStyleList, widgetProviderInfo, rect, curwidgetView.mSpanX, curwidgetView.mSpanY, curWidgetInfo);
			}

			else {
				return false;
			}
			
			if (newWidgetInfo == null) {
				return false;
			}
			
		   
			
			
			GoWidgetBaseInfo info = new GoWidgetBaseInfo();
			info.mWidgetId = goWidgetManager.allocateWidgetId();
			info.mType = newWidgetInfo.type;
			info.mLayout = newWidgetInfo.layoutID;
			info.mTheme = curWidgetInfo.mTheme;
			info.mThemeId = curWidgetInfo.mThemeId;
			info.mPrototype = curWidgetInfo.mPrototype;
			
			AppWidgetProviderInfo provider = widgetProviderInfo.mProvider;
			if (provider != null) {
				provider.minHeight = DrawUtils.dip2px(newWidgetInfo.minHeight);
				provider.minWidth = DrawUtils.dip2px(newWidgetInfo.minWidth);
				if (provider.provider != null) {
					info.mPackage = provider.provider.getPackageName();
				}
				if (provider.configure != null) {
					info.mClassName = provider.configure.getClassName();
				}
			}
			InnerWidgetInfo innerWidgetInfo = goWidgetManager.getInnerWidgetInfo(info.mPrototype);
			//go任务管理器以前是内置了。所以还残留内置的代码。这里需要做一下判断
			// 内置
			if (innerWidgetInfo != null) {
				// 更新包名为实际inflate xml的包名
				info.mPackage = innerWidgetInfo.mInflatePkg;
				info.mPrototype = innerWidgetInfo.mPrototype;
			}
			
			//设置传递参数，传递给gowidget内部使用
			Bundle bundle = new Bundle();
			bundle.putInt(GoWidgetConstant.GOWIDGET_ID, info.mWidgetId);
			bundle.putInt(GoWidgetConstant.GOWIDGET_TYPE, info.mType);
			bundle.putString(GoWidgetConstant.GOWIDGET_LAYOUT, info.mLayout);
			bundle.putString(GoWidgetConstant.GOWIDGET_THEME, info.mTheme);
			bundle.putInt(GoWidgetConstant.GOWIDGET_THEMEID, info.mThemeId);
			bundle.putInt(GoWidgetConstant.GOWIDGET_PROTOTYPE, info.mPrototype); // 内置类型
			bundle.putParcelable(GoWidgetConstant.GOWIDGET_PROVIDER, provider);
			bundle.putBoolean(GoWidgetConstant.GOWIDGET_ADD_TO_SCREEN, true);
			
			
			int curScreen = getCurrentScreenIndex(); //当前屏幕数
			deleteItem(curwidgetView, curScreen); //删除当前widget
			changeNewGoWidget(info, bundle, curScreen, rect); //重新添加新的widget
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
		
	/**
	 * 切换新样式widget
	 * 只针对go天气和go任务管理器
	 * @param info
	 * @param bundle
	 * @param screenindex
	 * @param rect
	 * @return
	 */
	private boolean changeNewGoWidget(GoWidgetBaseInfo info, Bundle bundle, int screenindex, Rect rect) {
		if (bundle == null || info == null) {
			return false;
		}

		boolean ret = false;
		
		//创建新的widget
		GoWidgetManager widgetManager = AppCore.getInstance().getGoWidgetManager();
		boolean add = widgetManager.addGoWidget(info);
		
		CellLayout cellLayout = mWorkspace.getScreenView(screenindex);
		if (cellLayout == null || !add) {
			return false;
		}

		int cellX =  rect.left; //x坐标
		int cellY =  rect.top;  //y坐标
		int cols = rect.width(); //列数
		int rows = rect.height(); //行数
		
		View widgetView = widgetManager.createView(info.mWidgetId); //创建新的widget
		if (widgetView != null) {
			ScreenAppWidgetInfo appWidgetInfo = new ScreenAppWidgetInfo(info.mWidgetId);
			widgetView.setTag(appWidgetInfo);
			appWidgetInfo.mCellX = cellX; 
			appWidgetInfo.mCellY = cellY;
			appWidgetInfo.mSpanX = cols; 
			appWidgetInfo.mSpanY = rows;
			mWorkspace.addInScreen(widgetView, screenindex, cellX, cellY, cols, rows, false); //添加到桌面
			widgetManager.startWidget(info.mWidgetId, bundle); //启动widget内部的初始化
			addDesktopItem(screenindex, appWidgetInfo); //写入数据库
			
			mWidgetView = widgetView; //重新设置当前编辑的widget是新的widget
			ret = true;
		}
			
		if (!ret) {
			widgetManager.deleteWidget(info.mWidgetId);
		}

		return ret;
	}
	
	
	
	

	void dragFolderFormAllApps(UserFolderInfo folderInfo, int[] xy, int screenIndex) {
		if (folderInfo == null || xy == null) {
			return;
		}

		Log.i(LogConstants.HEART_TAG, "drag folder from apps drawer");
		// 添加文件夹到桌面
		folderInfo.mCellX = xy[0];
		folderInfo.mCellY = xy[1];
		addDesktopView(folderInfo, screenIndex, true);
		// 保护从功能表拖出来的文件夹的自定义名字存在
		CharSequence title = folderInfo.mTitle;
		title = title == null ? "" : title;
		folderInfo.setFeatureTitle(title.toString());
		// 添加到数据库
		addDesktopItem(screenIndex, folderInfo);

		// 异步添加文件夹内部item
		if (mBinder != null) {
			mBinder.synchFolderFromDrawer(folderInfo, null, false);
		}
	}

	private void clearAllDesktopView() {
		// 清除view
		final int screenCount = mWorkspace.getChildCount();
		for (int i = 0; i < screenCount; i++) {
			CellLayout group = (CellLayout) (mWorkspace.getChildAt(i));
			if (group != null) {
				try {
					// NOTE: 调用removeAllViewsInLayout可以避免layout
					group.removeAllViewsInLayout();
				} catch (IllegalArgumentException e) {
					// add by chenguanyu
					// sharedPreferences记录5次
					int count = 0;
					PreferencesManager preferences = new PreferencesManager(mActivity,
							"count_five", Context.MODE_PRIVATE);
					if (null != preferences) {
						count = preferences.getInt("count", 0);
						preferences.putInt("count", ++count);
						preferences.commit();
					}
					if (count <= 5) {
						// 5次后不弹toast
						DeskToast.makeText(mActivity, R.string.clear_all_desktop_view,
								Toast.LENGTH_LONG).show();
					}
				}
			}
		}
		mWorkspace.removeAllViewsInLayout();

		// 清除缓存的GoWidget View
		AppCore.getInstance().getGoWidgetManager().cleanView();
	}

	private void reloadDesktop() {
		// 停止正在替换错误的widget
		stopWidgetEdit();

		// 如果此时在桌面编辑状态就退出再刷新
		if (Workspace.getLayoutScale() < 1.0f) {
			// 关闭上半部分
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
					IDiyMsgIds.SCREENEDIT_CLOSE_SCREEN_EDIT_LAYOUT, 0, null, null);
		}
		AppCore.getInstance().getGoWidgetManager().cancelReplaceWidget();
		mThemeSpreader.cancel();
		if (!mInitWorkspace) {
			return;
		}

		if (mBinder != null) {
			// 停止加载组件
			mBinder.cancel();
			mBinder = null;
		}

		// 记录当前屏索引
		mCurrentScreen = mWorkspace.getCurrentScreen();

		// 重新加载
		loadSetting();
		loadScreenInfo();

		mLayout.post(new Runnable() {
			@Override
			public void run() {
				// 设置加载状态
				setLoading(true);
				// 清除已经注册到ItemInfo的所有View
				unRigistDesktopObject();
				// 清除所有视图
				clearAllDesktopView();

				initWrokspace();
				startDesktopLoader();
			}
		});
	}

	private void showMainScreen() {
		stopWidgetEdit();
		// 文件夹气泡框取消
		GoLauncher.sendMessage(this, IDiyFrameIds.DESK_USER_FOLDER_FRAME,
						IDiyMsgIds.REMOVE_ACTION_MENU, -1, null, null);
		// 大于0则不跳转主屏
		if (mGotoMainScreen > 0) {
			mGotoMainScreen = 0;
			return;
		}
		// 屏幕层气泡框判断
		if (mQuickActionMenu != null && mQuickActionMenu.isShowing()) {
			return;
		}
		// Dock层气泡框判断
		if (GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME,
				IDiyMsgIds.REMOVE_ACTION_MENU, -1, null, null)) {
			return;
		}
		if (isTop()) {
			PreferencesManager sharedPreferences = new PreferencesManager(mActivity,
					IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
			boolean firstShow = sharedPreferences.getBoolean(
					IPreferencesIds.SHOULD_SHOW_PREVIEW_HOME, true);
			if (firstShow) {
				Editor editor = sharedPreferences.edit();
				editor.putBoolean(IPreferencesIds.SHOULD_SHOW_PREVIEW_HOME, false);
				editor.commit();
				SensePreviewFrame.sIsHOME = true;
				GoLauncher.sendMessage(IMsgType.SYNC, IDiyFrameIds.SCREEN_FRAME,
						IDiyMsgIds.SCREEN_SHOW_PREVIEW, 0, null, null);
			} else {
				turnToScreen(mWorkspace.getMainScreen(), true, -1);
			}

		} else {
			mWorkspace.setCurrentScreen(mWorkspace.getMainScreen());
		}
	}

	private boolean isTop() {
		final AbstractFrame topFrame = mFrameManager.getTopFrame();
		if (topFrame != null) {
			final int id = topFrame.getId();
			return id == IDiyFrameIds.DOCK_FRAME || id == IDiyFrameIds.DESK_USER_FOLDER_FRAME
					|| id == getId();
		}
		return false;
	}

	/**
	 * @param screen
	 * @param noElastic
	 *            是否使用弹性效果
	 * @param duration
	 *            小于0则自动计算时间
	 */
	private void turnToScreen(int screen, boolean noElastic, int duration) {
		// 没有编辑widget才可跳转屏幕
		if (mWorkspace != null /*
								 * && screen >= 0 && screen <
								 * mWorkspace.getChildCount() &&
								 */ && !mIsWidgetEditMode) {
			ScreenSettingInfo screenSetInfo = GOLauncherApp.getSettingControler()
					.getScreenSettingInfo();
			if (screenSetInfo.mScreenLooping) {
				if (screen < 0) {
					screen = mWorkspace.getChildCount() - 1;
				} else if (screen >= mWorkspace.getChildCount()) {
					screen = 0;
				}
			} else {
				if (screen < 0) {
					screen = 0;
				} else if (screen >= mWorkspace.getChildCount()) {
					screen = mWorkspace.getChildCount() - 1;
				}
			}
			mWorkspace.snapToScreen(screen, noElastic, duration);

		}
	}

	private void updateIconAndTitle(ItemInfo itemInfo, View view) {
		if (view == null || itemInfo == null) {
			return;
		}

		boolean showTitle = true;
		if (mDesktopSettingInfo != null) {
			showTitle = mDesktopSettingInfo.isShowTitle();
		}

		if (view instanceof BubbleTextView) {
			BubbleTextView bubble = (BubbleTextView) view;
			if (itemInfo instanceof ShortCutInfo) {
				bubble.setIcon(((ShortCutInfo) itemInfo).mIcon);
				view.setTag(itemInfo);
			} else if (itemInfo instanceof UserFolderInfo) {
				FolderIcon folder = (FolderIcon) view;
				FolderIcon.prepareIcon(folder, (UserFolderInfo) itemInfo);
				view.setTag(itemInfo);
			} else if (itemInfo instanceof ScreenLiveFolderInfo) {
				bubble.setIcon(((ScreenLiveFolderInfo) itemInfo).mIcon);
				view.setTag(itemInfo);
			}
			ScreenUtils.setBubbleTextTitle(showTitle, bubble);
		}
	}

	private void updateItemsIconAndTitle(ArrayList<ItemInfo> list) {
		if (list == null || mWorkspace == null) {
			return;
		}

		for (ItemInfo itemInfo : list) {
			if (itemInfo != null) {
				View targetView = ScreenUtils.getViewByItemId(itemInfo.mInScreenId, -1, mWorkspace);
				updateIconAndTitle(itemInfo, targetView);
			}
		}
	}

	public Search findSearchWidgetOnCurrentScreen() {
		return ScreenUtils.findSearchOnCurrentScreen(mWorkspace);
	}

	private void updateAllFolder(boolean reloadContent) {
		if (mWorkspace == null) {
			return;
		}

		if (mDesktopItems != null) {
			final int size = mDesktopItems.size();
			for (int i = 0; i < size; i++) {
				ArrayList<ItemInfo> itemList = mDesktopItems.get(i);
				if (itemList != null) {
					int count = itemList.size();
					for (int j = 0; j < count; j++) {
						final ItemInfo itemInfo = itemList.get(j);
						if (itemInfo != null && itemInfo instanceof UserFolderInfo) {
							updateFolderIconAsync((UserFolderInfo) itemInfo, reloadContent, false);
						}
					}
				}
			}
		}
	}

	private void updateFolderList(ArrayList<ItemInfo> folderList) {
		if (mWorkspace == null || folderList == null) {
			return;
		}

		for (ItemInfo itemInfo : folderList) {
			if (itemInfo != null && itemInfo instanceof UserFolderInfo) {
				updateFolderIconAsync((UserFolderInfo) itemInfo, false, false);
			}
		}
	}

	void updateFolderIconAsync(final UserFolderInfo folderInfo, boolean reload, boolean checkDel) {
		if (folderInfo == null) {
			return;
		}
		final FolderIcon folderIcon = (FolderIcon) ScreenUtils.getViewByItemId(
				folderInfo.mInScreenId, folderInfo.mScreenIndex, mWorkspace);
		if (folderIcon != null && folderInfo.mIsFirstCreate) {
			ArrayList<ItemInfo> items = mControler.getFolderItems(folderInfo.mInScreenId);
			int count = 0;
			for (ItemInfo itemInfo : items) {
				int type = NotificationType.IS_NOT_NOTIFICSTION;
				if (itemInfo instanceof ShortCutInfo) {
					ShortCutInfo shortCutInfo = (ShortCutInfo) itemInfo;
					type = AppIdentifier.getNotificationType(mActivity, shortCutInfo);
					if (type != NotificationType.IS_NOT_NOTIFICSTION) {
						NotificationControler controler = AppCore.getInstance()
								.getNotificationControler();
						int unreadCount = 0;
						if (type == NotificationType.NOTIFICATIONTYPE_MORE_APP) {
							unreadCount = shortCutInfo.getRelativeItemInfo().getUnreadCount();
						} else {
							unreadCount = controler.getNotification(type);
						}
						count += unreadCount;
						folderIcon.setCounterType(NotificationType.NOTIFICATIONTYPE_DESKFOLDER);
					}
				}
			}
			folderInfo.mTotleUnreadCount = count;
			folderInfo.mIsFirstCreate = false;
		}
		if (folderIcon != null
				&& folderIcon.getCounterType() == NotificationType.NOTIFICATIONTYPE_DESKFOLDER) {
			folderIcon.setCounter(folderInfo.mTotleUnreadCount);
		}
		if (reload) {
			folderInfo.mContentsInit = false;
		}

		if (folderIcon != null && mBinder != null) {
			mBinder.updateFolderIconAsync(folderIcon, checkDel);
		}
	}

	public ArrayList<ItemInfo> getFolderContent(long folderId) {
		return mControler.getFolderItems(folderId);
	}

	/**
	 * 从数据库重新读取Folder内容
	 * 
	 * @param userFolderInfo
	 * @return
	 */
	public ArrayList<ItemInfo> getFolderContentFromDB(UserFolderInfo userFolderInfo) {
		synchronized (userFolderInfo) {
			/**
			 * ADT-3723 非必现：桌面文件夹消失，有消失动画 步骤：1、创建文件件 2、打开文件夹。拖动图标进行排序
			 * 3、点击+号按钮，添加或删除部分程序 4、完成 原因：换位线程与添加程序进文件夹线程同步问题
			 * 修改方法：对userFolderInfo加锁
			 */
			return mControler.getFolderItems(userFolderInfo.mInScreenId);
		}
	}

	public void loadCompleteInfo(ShortCutInfo info) {
		if (info == null || info.mItemType == IItemType.ITEM_TYPE_SHORTCUT) {
			return;
		}

		// 如果是系统应用程序，在这里更新一次图标和title
		final AppDataEngine dataEngine = GOLauncherApp.getAppDataEngine();
		final AppItemInfo ainfo = dataEngine.getCompletedAppItem(info.mIntent);
		if (ainfo != null) {
			// 需要判断是自定义图标还是系统图标来进行是否修改图标的值
			if (!info.mIsUserIcon || info.mIcon == null) {
				// 在没有使用自定义图标或者没有图标信息的情况下赋予程序图标
				info.mIcon = ainfo.getIcon();
			}

			if (!info.mIsUserTitle || info.mTitle == null) {
				// 在没有使用自定义title或没有title信息情况下赋予程序title信息
				info.mTitle = ainfo.getTitle();
			}
		}
	}

	private void startDesktopLoader() {
		setLoading(true);
		mBinder = new DesktopBinder(this, mDesktopItems);
		mBinder.startBinding();
	}

	private void stopDesktopLoader() {
		if (mBinder != null) {
			// 停止加载组件
			mBinder.cancel();
			mBinder = null;
		}
		setLoading(false);
	}

	void bindShortcut(LinkedList<ItemInfo> shortcuts) {
		int count = Math.min(DesktopBinder.ITEMS_COUNT, shortcuts.size());
		while (count-- > 0) {
			ItemInfo itemInfo = shortcuts.removeFirst();
			View addView = null;
			boolean isBreak = false;
			if (itemInfo.mItemType == IItemType.ITEM_TYPE_APP_WIDGET) {
				addView = filterWidgetView((ScreenAppWidgetInfo) itemInfo);
				isBreak = true;
			} else {
				addView = createDesktopView(itemInfo, itemInfo.mScreenIndex, false);
				if (itemInfo.mItemType == IItemType.ITEM_TYPE_FAVORITE) {
					isBreak = true;
				}
			}

			if (addView != null) {
				mWorkspace.addInScreen(addView, itemInfo.mScreenIndex, itemInfo.mCellX,
						itemInfo.mCellY, itemInfo.mSpanX, itemInfo.mSpanY, false);
				mWorkspace.postInvalidate();

				if (isBreak) {
					break;
				}
			} else if (addView == null && itemInfo.mItemType == IItemType.ITEM_TYPE_APP_WIDGET) {
				ScreenMissIconBugUtil.showToast(ScreenMissIconBugUtil.ERROR_ADDVIEW_WIDGET_NULL);
			}
		}

		if (shortcuts.isEmpty()) {
			if (mBinder != null) {
				mBinder.notifyLoadFinish();
			}
		}
	}

	boolean isLoading() {
		return mIsLoading;
	}

	void setLoading(boolean loading) {
		mIsLoading = loading;
	}

	void loadFinish() {
		setLoading(false);
		// 通知widget加载数据
		AppCore.getInstance().getGoWidgetManager().startListening();
		updateAllFolder(true);
		GoLauncher.sendBroadcastMessage(this, IDiyMsgIds.SCREEN_FINISH_LOADING, -1, null, null);
		
		// 发送广播给多屏多壁纸应用，通知当前屏幕数与当前屏幕下标
		if (mWorkspace != null) {
			mWorkspace.sendBroadcastToMultipleWallpaper(false, true);
		}
		
		if (mWorkspace != null && mWorkspace.mNeedToSmall) {
			mWorkspace.normalToSmall(mCurTab , mPkg_Screenedit);
			mCurTab = null;
			mPkg_Screenedit = null;
		}

		// 启动硬件加速
		// if (ViewCompat.isHardwareAccelerated(mWorkspace)) {
		// mWorkspace.post(new Runnable() {
		// @Override
		// public void run() {
		// mWorkspace.buildChildHardwareLayers();
		// }
		// });
		// }
//		if (isNeedToRecommend()) {
//			if (WallpaperControler.adjustWallpaperDimension(null)) {
//				WallpaperDensityUtil.setWallpaperDimension(GoLauncher.getContext());
//			}
//			saveSharedPreferences(false);
//		}
		if (isNeedToRecommend()) {
			WallpaperControler.adjustWallpaperDimension(null);
			saveSharedPreferences(false);
		}

		startTheme2MaskView();
		
		// 发送广播给多屏多壁纸应用，通知当前屏幕数与当前屏幕下标
//		mWorkspace.sendBroadcastToMultipleWallpaper(false, true);
	}

	// 通知罩子层显示主题2.0界面
	private void startTheme2MaskView() {
		ThemeInfoBean infoBean = ThemeManager.getInstance(mActivity).getCurThemeInfoBean();
		if (infoBean != null) {
			if (infoBean.isMaskView()) {
				GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						IDiyMsgIds.COVER_FRAME_ADD_VIEW, CoverFrame.COVER_VIEW_THEME,
						infoBean.getPackageName(), null);
			}
			ThemeInfoBean.MiddleViewBean middleViewBean = infoBean.getMiddleViewBean();
			if (middleViewBean != null) {
				if (middleViewBean.mHasMiddleView) {
					mBackWorkspace.setMiddleView(infoBean.getPackageName(),
							middleViewBean.mIsSurfaceView);
				}
			} else {
				mBackWorkspace.removeMiddleView();
			}
		}
	}
	/**
	 * 更新 通讯统计
	 * 
	 * @param type
	 *            统计类型
	 * @param count
	 *            未读短信（邮件、来电）个数
	 */
	private void updateNotification(int type, int count) {
		final int screen = mWorkspace.getChildCount();
		final NotificationControler notificationControler = AppCore.getInstance()
				.getNotificationControler();

		for (int i = 0; i < screen; i++) {
			final CellLayout layout = (CellLayout) mWorkspace.getChildAt(i);
			if (layout == null) {
				continue;
			}

			final int size = layout.getChildCount();
			for (int j = 0; j < size; j++) {
				View child = layout.getChildAt(j);
				if (child != null && child instanceof BubbleTextView) {
					final BubbleTextView bubble = (BubbleTextView) child;
					switch (bubble.getCounterType()) {
						case NotificationType.NOTIFICATIONTYPE_CALL :
							bubble.setCounter(notificationControler.getUnreadCallCount());
							break;

						case NotificationType.NOTIFICATIONTYPE_SMS :
							bubble.setCounter(notificationControler.getUnreadSMSCount());
							break;

						case NotificationType.NOTIFICATIONTYPE_GMAIL :
							bubble.setCounter(notificationControler.getUnreadGmailCount());
							break;

						case NotificationType.NOTIFICATIONTYPE_K9MAIL :
							bubble.setCounter(notificationControler.getUnreadK9mailCount());
							break;

						case NotificationType.NOTIFICATIONTYPE_FACEBOOK :
							bubble.setCounter(notificationControler.getUnreadFacebookCount());
							break;

						case NotificationType.NOTIFICATIONTYPE_SinaWeibo :
							bubble.setCounter(notificationControler.getUnreadSinaWeiboCount());
							break;

						case NotificationType.NOTIFICATIONTYPE_DESKFOLDER :
							if (bubble instanceof FolderIcon) {
								FolderIcon folder = (FolderIcon) bubble;
								UserFolderInfo userFolderInfo = folder.getInfo();
								// getFolderContentFromDB(userFolderInfo);
								if (userFolderInfo.mItemType == IItemType.ITEM_TYPE_USER_FOLDER) {
									NotificationControler controler = AppCore.getInstance()
											.getNotificationControler();
									int total = controler.getTotalUnreadCount(userFolderInfo);
									userFolderInfo.mTotleUnreadCount = total;
									folder.setCounter(total);
								}
							}
							break;

						default :
							break;
					}
					bubble.invalidate();
				}
			}
		}
		// 清除缓存，防止滑动时未更新缓存
		mWorkspace.destroyChildrenDrawingCache(false);
		mWorkspace.postInvalidate();
	}

	private void unbindObjectInScreen(int screenId) {
		if (mDesktopItems != null) {
			final ArrayList<ItemInfo> sc = mDesktopItems.get(screenId);
			ScreenUtils.unbindDesktopObject(sc);
		}
	}

	// 获取对应文件夹的ItemInfo
	private UserFolderInfo getFolderItemInfo(long refFolderId) {
		ArrayList<ItemInfo> items = getItemFromRefId(refFolderId);
		if (null != items && items.size() > 0) {
			ItemInfo info = items.get(0);
			if (info != null && info instanceof UserFolderInfo) {
				return (UserFolderInfo) info;
			}
		}
		return null;
	}

	// 文件夹同步，考虑到很多操作用到此类，暂不分离出去
	private void addFolderItems(long refFolderId, ArrayList<AppItemInfo> infos) {
		ArrayList<ItemInfo> items = getItemFromRefId(refFolderId);
		if (null != items) {
			int sz = items.size();
			for (int i = 0; i < sz; i++) {
				addFolderItems(items.get(i), infos);
			}
		}
	}

	private void addFolderItems(ItemInfo info, ArrayList<AppItemInfo> infos) {
		final ItemInfo itemInfo = info;
		if (null == itemInfo || !(itemInfo instanceof UserFolderInfo)) {
			return;
		}

		final ArrayList<ItemInfo> contents = getItemInfoList(infos);
		if (null == contents || contents.size() <= 0) {
			return;
		}

		final UserFolderInfo folderInfo = (UserFolderInfo) itemInfo;
		// 异步添加文件夹内部item
		if (mBinder != null) {
			mBinder.synchFolderFromDrawer(folderInfo, contents, true);
		}
	}

	private void removeFolderItems(long refFolderId, ArrayList<AppItemInfo> infos) {
		ArrayList<ItemInfo> items = getItemFromRefId(refFolderId);
		if (null != items) {
			int sz = items.size();
			for (int i = 0; i < sz; i++) {
				removeFolderItems(items.get(i), infos);
			}
		}
	}

	private void removeFolderItems(ItemInfo info, ArrayList<AppItemInfo> infos) {
		final ItemInfo itemInfo = info;
		if (null != itemInfo && info instanceof UserFolderInfo) {
			final ArrayList<ItemInfo> contents = getItemInfoList(infos);
			final UserFolderInfo folderInfo = (UserFolderInfo) itemInfo;
			// 异步删除文件夹内部item
			if (mBinder != null) {
				mBinder.removeFolderContent(folderInfo, contents, mCheckDelUserFolder);
			}
		}
	}

	private void renameItem(long refItemId, String newName) {
		ArrayList<ItemInfo> items = getItemFromRefId(refItemId);
		if (null != items) {
			int sz = items.size();
			for (int i = 0; i < sz; i++) {
				renameItem(items.get(i), newName);
			}
		}
	}

	private void renameItem(ItemInfo info, String newName) {
		final ItemInfo itemInfo = info;
		if (null == itemInfo) {
			return;
		}

		if (info instanceof FeatureItemInfo) {
			((FeatureItemInfo) info).setFeatureTitle(newName);
			mControler.updateDesktopItem(itemInfo.mScreenIndex, info);
		}
		if (itemInfo instanceof ShortCutInfo) {
			((ShortCutInfo) itemInfo).setTitle(newName, true);
		} else if (itemInfo instanceof ScreenFolderInfo) {
			((ScreenFolderInfo) itemInfo).mTitle = newName;
		}

		View targetView = ScreenUtils.getViewByItemId(itemInfo.mInScreenId, -1, mWorkspace);
		if (null != targetView && (targetView instanceof BubbleTextView)) {
			if (mDesktopSettingInfo != null && mDesktopSettingInfo.isShowTitle()) {
				((BubbleTextView) targetView).setText(newName);
			} else {
				((BubbleTextView) targetView).setText(null);
			}
		}
	}

	/**
	 * 删除屏幕一项
	 * 
	 * @param itemInfo
	 *            　
	 * @param screenindex
	 *            　屏幕索引，-1可以遍历屏幕
	 */
	public synchronized void deleteItem(ItemInfo itemInfo, int screenindex) {
		if (null == itemInfo) {
			return;
		}

		// 删除view
		View targetView = ScreenUtils
				.getViewByItemId(itemInfo.mInScreenId, screenindex, mWorkspace);
		if (targetView != null) {
			ViewParent parent = targetView.getParent();
			if (parent != null && parent instanceof ViewGroup) {
				((ViewGroup) parent).removeView(targetView);
			}
		}

		mControler.removeDesktopItem(itemInfo);

		int type = itemInfo.mItemType;
		if (type == IItemType.ITEM_TYPE_SHORTCUT || type == IItemType.ITEM_TYPE_APP_WIDGET
				|| type == IItemType.ITEM_TYPE_LIVE_FOLDER) {
			if (type == IItemType.ITEM_TYPE_SHORTCUT) {
				ScreenUtils.unbindShortcut((ShortCutInfo) itemInfo);
			} else if (type == IItemType.ITEM_TYPE_LIVE_FOLDER) {
				ScreenUtils.unbindLiveFolder((ScreenLiveFolderInfo) itemInfo);
			}

			if (itemInfo instanceof ScreenAppWidgetInfo) {
				int widgetId = ((ScreenAppWidgetInfo) itemInfo).mAppWidgetId;
				if (GoWidgetManager.isGoWidget(widgetId)) {
					AppCore.getInstance().getGoWidgetManager().deleteWidget(widgetId);
				} else
				// 系统widget
				{
					mWidgetHost.deleteAppWidgetId(widgetId);
				}
			}
		} else if (type == IItemType.ITEM_TYPE_USER_FOLDER) {
			// 删除文件夹
			ScreenUtils.unbindeUserFolder((UserFolderInfo) itemInfo);
			mControler.removeUserFolder(itemInfo);
		}
	}

	private synchronized void actionDelete(ItemInfo itemInfo) {
		if (null == itemInfo) {
			return;
		}

		// 从文件夹拖出
		// if(mDragFromFolder)
		if (DragFrame.TYPE_SCREEN_FOLDER_DRAG == mDragType) {
			if (mCurrentFolderInfo != null) {
				if (null != mControler) {
					// final int screenIndex = mWorkspace.getCurrentScreen();
					// mControler.moveDesktopItemFromFolder(itemInfo,
					// screenIndex,
					// mCurrentFolderInfo.mInScreenId);
					mControler
							.removeItemFromFolder(itemInfo, mCurrentFolderInfo.mInScreenId, false);
				}
				// 更新缓存
				if (mCurrentFolderInfo instanceof UserFolderInfo) {
					itemInfo.selfDestruct();
					((UserFolderInfo) mCurrentFolderInfo).remove(itemInfo.mInScreenId);
					if (!deleteFolderOrNot((UserFolderInfo) mCurrentFolderInfo, true)) {
						// if (itemInfo instanceof ShortCutInfo) {
						// int type =
						// AppIdentifier.whichTypeOfNotification(mActivity,
						// (( ShortCutInfo ) itemInfo).mIntent);
						// if (type != NotificationType.IS_NOT_NOTIFICSTION) {
						// (( UserFolderInfo )
						// mCurrentFolderInfo).mTotleUnreadCount -= ((
						// ShortCutInfo ) itemInfo).mCounter;
						// }
						// }
						// 更新图标
						updateFolderIconAsync((UserFolderInfo) mCurrentFolderInfo, false, false);
					}
				}
			}

			// mDragFromFolder = false;
			mDragType = DragFrame.TYPE_SCREEN_ITEM_DRAG;
			mCurrentFolderInfo = null;
		} else {
			// 删除view
			mControler.removeDesktopItem(itemInfo);
			View targetView = ScreenUtils.getViewByItemId(itemInfo.mInScreenId,
					itemInfo.mScreenIndex, mWorkspace);

			if (targetView != null) {
				ViewParent parent = targetView.getParent();
				if (parent != null && parent instanceof ViewGroup) {
					((ViewGroup) parent).removeView(targetView);
				}
			}

			int type = itemInfo.mItemType;
			if (type == IItemType.ITEM_TYPE_SHORTCUT || type == IItemType.ITEM_TYPE_APP_WIDGET
					|| type == IItemType.ITEM_TYPE_LIVE_FOLDER) {
				if (type == IItemType.ITEM_TYPE_SHORTCUT) {
					ScreenUtils.unbindShortcut((ShortCutInfo) itemInfo);
				} else if (type == IItemType.ITEM_TYPE_LIVE_FOLDER) {
					ScreenUtils.unbindLiveFolder((ScreenLiveFolderInfo) itemInfo);
				}

				if (itemInfo instanceof ScreenAppWidgetInfo) {
					int widgetId = ((ScreenAppWidgetInfo) itemInfo).mAppWidgetId;
					if (GoWidgetManager.isGoWidget(widgetId)) {
						AppCore.getInstance().getGoWidgetManager().deleteWidget(widgetId);
					} else // 系统widget
					{
						mWidgetHost.deleteAppWidgetId(widgetId);
					}
				}
			} else if (type == IItemType.ITEM_TYPE_USER_FOLDER) {
				// 删除文件夹
				ScreenUtils.unbindeUserFolder((UserFolderInfo) itemInfo);
				mControler.removeUserFolder(itemInfo);
			}
		}
	}

	/**
	 * 强制刷新文件内容、图标
	 * 
	 * @param info
	 */
	public synchronized void reloadFolderContent(UserFolderInfo info, boolean checkDel) {
		if (null != info) {
			info.clear();
			// 由于一些绑定的原因，这里直接重新设置初始化标志
			info.mContentsInit = false;
			info.mIsFirstCreate = true;
			updateFolderIconAsync(info, true, checkDel);
		}
	}

	private synchronized ArrayList<ItemInfo> getItemFromRefId(long refId) {
		ArrayList<ItemInfo> retInfos = new ArrayList<ItemInfo>();
		int screenCount = mWorkspace.getChildCount();

		for (int i = 0; i < screenCount; i++) {
			ViewGroup viewGroup = (ViewGroup) mWorkspace.getChildAt(i);
			if (null == viewGroup) {
				continue;
			}

			int viewCount = viewGroup.getChildCount();
			for (int j = 0; j < viewCount; j++) {
				View view = viewGroup.getChildAt(j);
				if (null == view) {
					continue;
				}

				Object obj = view.getTag();
				if (null != obj && obj instanceof ItemInfo) {
					ItemInfo info = (ItemInfo) obj;
					if (info.mRefId == refId || info.mInScreenId == refId) {
						retInfos.add(info);
					}
				}
			}
		}

		return retInfos;
	}

	private ArrayList<ItemInfo> getItemInfoList(ArrayList<AppItemInfo> infos) {
		if (null == infos) {
			return null;
		}

		ArrayList<ItemInfo> rets = new ArrayList<ItemInfo>();
		int sz = infos.size();
		for (int i = 0; i < sz; i++) {
			AppItemInfo info = infos.get(i);
			if (null != info) {
				ShortCutInfo ret = new ShortCutInfo();
				ret.mIcon = info.mIcon;
				ret.mIntent = info.mIntent;
				ret.mItemType = IItemType.ITEM_TYPE_APPLICATION;
				ret.mSpanX = 1;
				ret.mSpanY = 1;
				ret.mTitle = info.mTitle;
				rets.add(ret);
			}
		}

		return rets;
	}

	private void clearFocus() {
		mLayout.clearFocus();
	}

	/**
	 * 显示操作菜单
	 */
	private boolean showQuickActionMenu(View target) {
		hideQuickActionMenu(false);
		if (target == null) {
			return false;
		}

		int[] xy = new int[2];
		target.getLocationInWindow(xy);
		if (Workspace.getLayoutScale() < 1.0f) {
			xy[0] *= Workspace.getLayoutScale();
			xy[1] *= Workspace.getLayoutScale();
			xy[0] += Workspace.sPageSpacingX / 2;
			xy[1] += Workspace.sPageSpacingY / 2;
		}
		Rect targetRect = new Rect(xy[0], xy[1], xy[0] + target.getWidth(),
				(int) (xy[1] + target.getHeight() * 0.9));

		ItemInfo itemInfo = (ItemInfo) target.getTag();
		if (itemInfo != null && itemInfo.mItemType != IItemType.ITEM_TYPE_FAVORITE) {
			int itemType = itemInfo.mItemType;

			mQuickActionMenu = new QuickActionMenu(mActivity, target, targetRect, mLayout, this);
			switch (itemType) {
				case IItemType.ITEM_TYPE_APPLICATION : {
				ShortCutInfo cutInfo = (ShortCutInfo) itemInfo;
					//判断是否屏幕广告图标,是就不给换图标和卸载
					if (cutInfo.mIntent != null
							&& cutInfo.mIntent.getAction() != null
							&& cutInfo.mIntent.getAction().equals(
									ICustomAction.ACTION_SCREEN_ADVERT)) {
						mQuickActionMenu.addItem(IQuickActionId.RENAME, R.drawable.icon_rename,
								R.string.renametext);
						mQuickActionMenu.addItem(IQuickActionId.DELETE, R.drawable.icon_del,
								R.string.deltext);
					} else {
						mQuickActionMenu.addItem(IQuickActionId.CHANGE_ICON,
								R.drawable.icon_change, R.string.changeicontext);
						mQuickActionMenu.addItem(IQuickActionId.RENAME, R.drawable.icon_rename,
								R.string.renametext);
//						mQuickActionMenu.addItem(IQuickActionId.FACEBOOK_SHARE_APP, R.drawable.icon_rename,
//								R.string.fbsharetext);
						mQuickActionMenu.addItem(IQuickActionId.DELETE, R.drawable.icon_del,
								R.string.deltext);
						mQuickActionMenu.addItem(IQuickActionId.UNINSTALL,
								R.drawable.icon_uninstall, R.string.uninstalltext);
					}
				
				}
					break;

				case IItemType.ITEM_TYPE_SHORTCUT :
				case IItemType.ITEM_TYPE_LIVE_FOLDER : {
					mQuickActionMenu.addItem(IQuickActionId.CHANGE_ICON, R.drawable.icon_change,
							R.string.changeicontext);
					mQuickActionMenu.addItem(IQuickActionId.RENAME, R.drawable.icon_rename,
							R.string.renametext);
//					if (!DockUtil.isTheLastDockAppdrawer(target)) {
						mQuickActionMenu.addItem(IQuickActionId.DELETE, R.drawable.icon_del,
								R.string.deltext);
//					}
				}
					break;

				case IItemType.ITEM_TYPE_USER_FOLDER : {
					mQuickActionMenu.addItem(IQuickActionId.CHANGE_ICON, R.drawable.icon_change,
							R.string.changeicontext);
					//新增文件夹重命名
					mQuickActionMenu.addItem(IQuickActionId.RENAME_FOLDER, R.drawable.icon_rename,
							R.string.renametext);
					mQuickActionMenu.addItem(IQuickActionId.EDIT, R.drawable.icon_add,
							R.string.tab_add_app_add);
					mQuickActionMenu.addItem(IQuickActionId.DELETE, R.drawable.icon_del,
							R.string.deltext);
				}
					break;

				case IItemType.ITEM_TYPE_APP_WIDGET : {
					mQuickActionMenu.addItem(IQuickActionId.RESIZE, R.drawable.icon_zoom,
							R.string.zoomtext);

					ScreenAppWidgetInfo widgetInfo = (ScreenAppWidgetInfo) itemInfo;
					if (GoWidgetManager.isGoWidget(widgetInfo.mAppWidgetId)) {
						GoWidgetBaseInfo baseInfo = AppCore.getInstance().getGoWidgetManager()
								.getWidgetInfo(widgetInfo.mAppWidgetId);
						if (baseInfo != null && baseInfo.mPackage != null
								&& baseInfo.mClassName != null && baseInfo.mClassName.length() > 0) {
							mQuickActionMenu.addItem(IQuickActionId.CONFIG, R.drawable.config,
									R.string.configtext);
						}
						// update by zhoujun 应用游戏中心的widget 不需要换肤操作 (goStore也暂时不换)
						if (baseInfo == null
								|| (baseInfo.mPrototype != GoWidgetBaseInfo.PROTOTYPE_APPGAME && baseInfo.mPrototype != GoWidgetBaseInfo.PROTOTYPE_GOSTORE)) {
							// GOwidget换肤入口
							mQuickActionMenu.addItem(IQuickActionId.THEME, R.drawable.skin,
									R.string.skintext);
						}
						// update by zhoujun 2012-08-16 end
					}
					
					mQuickActionMenu.addItem(IQuickActionId.DELETE, R.drawable.icon_del,
							R.string.deltext);
				}
					break;

				default :
					break;
			}
			mQuickActionMenu.show();
		}
		return true;
	}
	
	
	private boolean checkRecommendIconShake(int screenIndex) {
		boolean ret = true;
		try {
			AdvertControl advertControl = AdvertControl.getAdvertControlInstance(mActivity);
			advertControl.cancleShakeAnimNow(); //清空动画队列
			
			CellLayout cellLayout = mWorkspace.getScreenView(screenIndex);
			if (cellLayout == null) {
				return ret;
			}
			
			int size = cellLayout.getChildCount();
			for (int i = 0; i < size; i++) {
				View childView = cellLayout.getChildAt(i);
				if (childView instanceof BubbleTextView) {
					Object object = childView.getTag();
					if (object instanceof ShortCutInfo) {
						ShortCutInfo info = (ShortCutInfo) object;
						//检查图标是否15/首屏推荐图标
						boolean isneedShake = advertControl.checkNeedShake(info.mIntent);
						if (isneedShake) {
							advertControl.setShakeAnim(childView);
						}
					}
				}
			}
			advertControl.cancleShakeAnimDelayed(); //2秒后抖动清除动画
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	
	

	/**
	 * 取消弹出菜单
	 * 
	 * @param dismissWithCallback
	 *            ， 是否回调， true仅取消菜单显示，false会回调到
	 *            {@link QuickActionMenu.onActionListener#onActionClick(int, View)}
	 *            并传回一个{@link IQuickActionId#CANCEL}事件
	 */
	private void hideQuickActionMenu(boolean dismissWithCallback) {
		if (mQuickActionMenu != null) {
			if (dismissWithCallback) {
				mQuickActionMenu.cancel();
			} else {
				mQuickActionMenu.dismiss();
			}
			mQuickActionMenu = null;
		}
	}

	@Override
	public void onActionClick(int action, Object target) {
		ItemInfo targetInfo = null;
		if (target != null && target instanceof ItemInfo) {
			targetInfo = (ItemInfo) target;
		} else if (target == null || !(target instanceof View)) {
			return;
		}

		targetInfo = targetInfo == null ? (ItemInfo) ((View) target).getTag() : targetInfo;
		if (targetInfo == null) {
			return;
		}

		mDraggedItemId = targetInfo.mInScreenId;
		// 用户行为统计 add by dingzijian 2012-9-14
		int actionId = -1;
		if (targetInfo instanceof ShortCutInfo) {
			actionId = StatisticsData.DESK_ACTION_ID_LONG_CLICK_ICON;
		} else if (targetInfo instanceof ScreenAppWidgetInfo) {
			actionId = StatisticsData.DESK_ACTION_ID_LONG_CLICK_WIDGET;
		} else if (targetInfo instanceof UserFolderInfo) {
			actionId = StatisticsData.DESK_ACTION_ID_LONG_CLICK_FLODER;
		}

		switch (action) {
			case IQuickActionId.CANCEL : {
				GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						IFrameworkMsgId.REMOVE_FRAME, IDiyFrameIds.DRAG_FRAME, null, null);
				break;
			}

			case IQuickActionId.CHANGE_ICON : {
				BitmapDrawable iconDrawable = null;
				String defaultNameString = "";
				if (mDraggedItemId >= 0) {
					View editingView = ScreenUtils.getViewByItemId(mDraggedItemId,
							mWorkspace.getCurrentScreen(), mWorkspace);

					if (editingView != null) {
						if (editingView.getTag() instanceof RelativeItemInfo) {
							RelativeItemInfo tagInfo = (RelativeItemInfo) editingView.getTag();

							if (tagInfo instanceof ShortCutInfo) {
								if (null != tagInfo.getRelativeItemInfo()) {
									iconDrawable = tagInfo.getRelativeItemInfo().getIcon();
									if (tagInfo.getRelativeItemInfo().mTitle != null) {
										defaultNameString = tagInfo.getRelativeItemInfo().mTitle
												.toString();
									}
								}
							} else if (editingView instanceof FolderIcon) {
								iconDrawable = ScreenUtils.getFolderBackIcon();
								if (((FolderIcon) editingView).getText() != null) {
									defaultNameString = ((FolderIcon) editingView).getText()
											.toString();
								}
							}
						}
					}
				}
				if (target instanceof FolderIcon) {
					ChangeIconPreviewActivity.sFromWhatRequester = ChangeIconPreviewActivity.USER_FOLDER_STYLE; // 文件夹
				} else {
					ChangeIconPreviewActivity.sFromWhatRequester = ChangeIconPreviewActivity.SCREEN_STYLE; // 图标
				}

				Bundle bundle = new Bundle();
				if (null != iconDrawable) {
					bundle.putParcelable(ChangeIconPreviewActivity.DEFAULT_ICON_BITMAP,
							iconDrawable.getBitmap());
				}
				bundle.putString(ChangeIconPreviewActivity.DEFAULT_NAME, defaultNameString);

				try {
					Intent intent = new Intent(mActivity, ChangeIconPreviewActivity.class);
					intent.putExtras(bundle);
					mActivity.startActivityForResult(intent, IRequestCodeIds.REQUEST_THEME_FORICON);
				} catch (Exception e) {
					e.printStackTrace();
				}
				StatisticsData.countUserActionData(actionId, StatisticsData.USER_ACTION_ONE,
						IPreferencesIds.DESK_ACTION_DATA);
			}
				break;

			case IQuickActionId.RENAME : {
				Intent intent = new Intent(mActivity, RenameActivity.class);
				CharSequence title = ScreenUtils.getItemTitle(targetInfo);
				intent.putExtra(RenameActivity.NAME, title);
				intent.putExtra(RenameActivity.HANDLERID, IDiyFrameIds.SCREEN_FRAME);
				intent.putExtra(RenameActivity.ITEMID, targetInfo.mInScreenId);
				intent.putExtra(RenameActivity.SHOW_RECOMMENDEDNAME, false);
				intent.putExtra(RenameActivity.FINISH_WHEN_CHANGE_ORIENTATION, false);
				mActivity.startActivityForResult(intent, IRequestCodeIds.REQUEST_RENAME);
				StatisticsData.countUserActionData(StatisticsData.DESK_ACTION_ID_LONG_CLICK_ICON,
						StatisticsData.USER_ACTION_TWO, IPreferencesIds.DESK_ACTION_DATA);
			}
				break;
				
			case IQuickActionId.FACEBOOK_SHARE_APP : {
				if (targetInfo instanceof ShortCutInfo) {
					ShortCutInfo shortCutInfo = (ShortCutInfo) targetInfo;
					AppItemInfo appItemInfo = shortCutInfo.getRelativeItemInfo();
					if (appItemInfo != null) {
						try {
							String pkg = appItemInfo.getAppPackageName(); // 包名
							Bitmap icon = appItemInfo.getIcon() != null ? appItemInfo.getIcon().getBitmap() : null; // 图标
							String title = appItemInfo.getTitle(); // 程序名字
							if (pkg != null && icon != null && title != null) {
								//TODO:向梁在此接入facebook分享接口
								Log.i("rxq", "pkg=" + pkg + " title=" + title + " icon=" + (icon != null));
							}
						} catch (Exception e) {
							// DO NOTHING
						}
					}
				}
			}
				break;

			case IQuickActionId.DELETE : {
				if (null != targetInfo) {
					actionDelete(targetInfo);
					StatisticsData.countUserActionData(actionId, StatisticsData.USER_ACTION_THREE,
							IPreferencesIds.DESK_ACTION_DATA);
				}
			}
				break;

			case IQuickActionId.UNINSTALL : {
				actionUninstall((View) target);
				StatisticsData.countUserActionData(StatisticsData.DESK_ACTION_ID_LONG_CLICK_ICON,
						StatisticsData.USER_ACTION_FOUR, IPreferencesIds.DESK_ACTION_DATA);
			}
				break;

			case IQuickActionId.RESIZE : {
				editWdiget((View) target);
				StatisticsData.countUserActionData(StatisticsData.DESK_ACTION_ID_LONG_CLICK_WIDGET,
						StatisticsData.USER_ACTION_ONE, IPreferencesIds.DESK_ACTION_DATA);
			}
				break;

			case IQuickActionId.THEME : {
				actionChangeWidgetSkin((ScreenAppWidgetInfo) targetInfo);
				StatisticsData.countUserActionData(StatisticsData.DESK_ACTION_ID_LONG_CLICK_WIDGET,
						StatisticsData.USER_ACTION_FOUR, IPreferencesIds.DESK_ACTION_DATA);
			}
				break;

			case IQuickActionId.CONFIG : {
				ScreenAppWidgetInfo info = (ScreenAppWidgetInfo) targetInfo;
				if (info != null) {
					final GoWidgetManager widgetManager = AppCore.getInstance()
							.getGoWidgetManager();
					GoWidgetBaseInfo baseInfo = widgetManager.getWidgetInfo(info.mAppWidgetId);
					try {
						Intent intent = new Intent();
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						final ComponentName cn = widgetManager.getConfigComponent(baseInfo);
						if (cn != null) {
							intent.setComponent(cn);
						}

						Bundle bundle = new Bundle();
						bundle.putBoolean(GoWidgetConstant.GOWIDGET_SETTING_ENTRY, true);
						bundle.putInt(GoWidgetConstant.GOWIDGET_ID, info.mAppWidgetId);

						// 传递而外的信息，方便设置activity更换主题
						bundle.putString(GoWidgetConstant.GOWIDGET_THEME, baseInfo.mTheme);
						bundle.putInt(GoWidgetConstant.GOWIDGET_THEMEID, baseInfo.mThemeId);
						bundle.putInt(GoWidgetConstant.GOWIDGET_TYPE, baseInfo.mType);

						intent.putExtras(bundle);
						mActivity.startActivity(intent);
						StatisticsData.countUserActionData(
								StatisticsData.DESK_ACTION_ID_LONG_CLICK_WIDGET,
								StatisticsData.USER_ACTION_THREE, IPreferencesIds.DESK_ACTION_DATA);
					} catch (Exception e) {
						Log.i(LOG_TAG, "start gowidget config error, widgetid = "
								+ info.mAppWidgetId);
					}
				}
			}
				break;

			case IQuickActionId.EDIT : {
				if (targetInfo instanceof UserFolderInfo) {
					startEditFolderActivity((UserFolderInfo) targetInfo);
					StatisticsData.countUserActionData(
							StatisticsData.DESK_ACTION_ID_LONG_CLICK_FLODER,
							StatisticsData.USER_ACTION_TWO, IPreferencesIds.DESK_ACTION_DATA);
				}
			}
				break;
			case IQuickActionId.RENAME_FOLDER : {
				//长按文件夹气泡框重命名
				Intent intent = new Intent(mActivity, RenameActivity.class);
				CharSequence title = ScreenUtils.getItemTitle(targetInfo);
				intent.putExtra(RenameActivity.NAME, title);
				intent.putExtra(RenameActivity.HANDLERID, IDiyFrameIds.SCREEN_FRAME);
				intent.putExtra(RenameActivity.ITEMID, targetInfo.mInScreenId);
				intent.putExtra(RenameActivity.SHOW_RECOMMENDEDNAME, true);
				intent.putExtra(RenameActivity.FINISH_WHEN_CHANGE_ORIENTATION, true);
				mActivity.startActivityForResult(intent, IRequestCodeIds.REQUEST_RENAME);
				StatisticsData.countUserActionData(StatisticsData.DESK_ACTION_ID_LONG_CLICK_ICON,
						StatisticsData.USER_ACTION_TWO, IPreferencesIds.DESK_ACTION_DATA);
				
			}
			break;
			default :
				break;
		}

		// 清除拖动、编辑状态
		mWorkspace.clearDragState();
	}

	private void startEditFolderActivity(UserFolderInfo userFolderInfo) {
		if (null == userFolderInfo) {
			return;
		}

		CharSequence folderNameString = userFolderInfo.mTitle;
		Intent intent = new Intent(mActivity, ScreenModifyFolderActivity.class);
		intent.putExtra(ScreenModifyFolderActivity.FOLDER_ID, userFolderInfo.mInScreenId);
		intent.putExtra(ScreenModifyFolderActivity.FOLDER_TITLE, folderNameString);
		intent.putExtra(ScreenModifyFolderActivity.FOLDER_CREATE, false);
		mActivity.startActivityForResult(intent, IRequestCodeIds.REQUEST_DESKTOP_FOLDER_EDIT);
		folderNameString = null;
	}

	public static boolean sForceHide = false;

	private void changeDrawState(boolean showAll) {
		if (showAll) {
			if (mScreenSettingInfo != null) {
				boolean showTrash = GoLauncher.sendMessage(this, IDiyFrameIds.DRAG_FRAME,
						IDiyMsgIds.CHECK_SHOW_TRASH, -1, null, null);
				if (showTrash || sForceHide) {
					// mLayout.getIndicator().setVisible(false);
					mLayout.getIndicator().hide();
				} else {
					// mLayout.getIndicator().setVisible(mScreenSettingInfo.mEnableIndicator);
					mLayout.getIndicator().show();
				}
			}

			// 桌面恢复普通状态
			mWorkspace.setDrawState(Workspace.DRAW_STATE_ALL);
		} else {
			clearFocus();
			// mLayout.getIndicator().setVisible(false);
			mLayout.getIndicator().hide();
			mWorkspace.setDrawState(Workspace.DRAW_STATE_ONLY_BACKGROUND);
		}
		mWorkspace.postInvalidate();
	}

	/**
	 * 通知widget进入/离开屏幕显示区域
	 * 
	 * @param visible
	 */
	private void fireWidgetVisible(boolean visible, int screenIndex) {
		try {
			final Workspace.WidgetRunnable r = mWorkspace.getWidgetRunnable(visible);
			r.setScreen((ViewGroup) mWorkspace.getChildAt(screenIndex));
			final GoWidgetManager widgetManager = AppCore.getInstance().getGoWidgetManager();
			AsyncHandler asyncHandler = widgetManager.getAsyncHandler();

			if (visible) {
				// 通知进入需要延时
				asyncHandler.postDelayed(r, Workspace.FIRE_WIDGET_DELAY);
			} else {
				// 先取消掉之前的进入通知
				asyncHandler.removeCallbacks(mWorkspace.getWidgetRunnable(true));
				asyncHandler.post(r);
			}
		} catch (Exception e) {
			Log.i(LOG_TAG, "fireVisible err " + visible);
		}
	}

	/**
	 * 取消通知widget进入/离开屏幕显示区域
	 * 
	 * @param visible
	 */
	private void cancelFireWidgetVisible(boolean visible) {
		try {
			final Runnable r = mWorkspace.getWidgetRunnable(visible);
			final GoWidgetManager widgetManager = AppCore.getInstance().getGoWidgetManager();
			AsyncHandler asyncHandler = widgetManager.getAsyncHandler();
			asyncHandler.removeCallbacks(r);
		} catch (Exception e) {
			Log.i(LOG_TAG, "fireVisible err " + visible);
		}
	}

	private boolean getShadowState() {
		if (mDesktopSettingInfo != null) {
			return !mDesktopSettingInfo.isTransparentBg();
		}
		return true;
	}

	private CharSequence getDisplayTitle(CharSequence title) {
		/* 增加判断是否需要显示名称 */
		if (mDesktopSettingInfo != null && !mDesktopSettingInfo.isShowTitle()) {
			return null;
		}
		return title;
	}

	private int getFolderIconId() {
		return GoLauncher.isLargeIcon() ? R.layout.folder_icon_large : R.layout.folder_icon;
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		if (animation instanceof ScaleAnimation && null != mDeleteFolderInfo) {
			deleteItem(mDeleteFolderInfo, mDeleteFolderInfo.mScreenIndex);
			mDeleteFolderInfo = null;
		}
		if (animation instanceof TranslateAnimation) {
			if (mTextLayoutvisiable) {
				mTextLayoutvisiable = false;
				if (LockerThemeTab.sCHANG_LOCKER_THEME) {
					LockerThemeTab.sCHANG_LOCKER_THEME = false;
				}
			}
		}
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAnimationStart(Animation animation) {
		// TODO Auto-generated method stub

	}

	protected void postInvalidate() {
		mWorkspace.postInvalidate();
	}

	/**
	 * 如果pkgString是gowidget，尝试更换推荐gowidget视图
	 * 
	 * @param pkgString
	 */
	private void replaceFavoriteGowidget(String pkgString) {
		if (pkgString != null
				&& (pkgString.contains(ICustomAction.MAIN_GOWIDGET_PACKAGE) || pkgString
						.equals(LauncherEnv.GOSMS_PACKAGE))) {
			mHandler.obtainMessage(REPLACE_FAV_WIDGET, pkgString).sendToTarget();
		}
	}

	/** 图标丢失调查 BEGIN ****************************************************************/
	// @Deprecated
	// public HashMap<Integer, ArrayList<ItemInfo>> getmScreenHashMap()
	// {
	// return mControler.getmScreenHashMap();
	// }

	public int getCurrentScreenIndex() {
		return mWorkspace.getCurrentScreen();
	}

	public CellLayout getCurrentScreen() {
		return mWorkspace.getCurrentScreenView();
	}

	// @Deprecated
	// public void getCurrentScreenDbData(ArrayList<ItemInfo> checkedList)
	// {
	// mControler.getScreenItems(mWorkspace.getCurrentScreen(), checkedList);
	// }
	/** 图标丢失调查 END ****************************************************************/
	/**
	 * 
	 * 获取屏幕层能放置该组件的屏幕list
	 * */
	public ArrayList<Integer> getEnoughSpaceList(Bundle bundle) {
		AppWidgetProviderInfo providerInfo = bundle
				.getParcelable(GoWidgetConstant.GOWIDGET_PROVIDER);
		int minWidth = 0, minHeight = 0;
		if (providerInfo != null) {
			minWidth = providerInfo.minWidth;
			minHeight = providerInfo.minHeight;
		}
		int xy[] = new int[2];
		// 对所有屏幕进行计算，并保持有足够空间添加gowidget的屏幕下标
		ArrayList<Integer> enoughSpaceIndexList = new ArrayList<Integer>();
		int count = mWorkspace.getChildCount();
		boolean hasEnoughSpace;
		for (int i = 0; i < count; i++) {
			CellLayout cellLayout = mWorkspace.getScreenView(i);
			int[] cell = cellLayout.rectToCell(minWidth, minHeight);
			hasEnoughSpace = ScreenUtils.findVacant(xy, cell[0], cell[1], i, mWorkspace);
			if (hasEnoughSpace) {
				enoughSpaceIndexList.add(i);
			}
		}
		return enoughSpaceIndexList;
	}

	// begin ----功能表获得桌面预览图
	private void getAppDrawCards(ArrayList<Object> itemList) {
		int count = mWorkspace.getChildCount();
		for (int i = 0; i < count; i++) {
			AppFuncScreenItemInfo info = getAppDrawCardByIndex(i);
			itemList.add(info);

		}
	}

	private AppFuncScreenItemInfo getAppDrawCardByIndex(int index) {
		int h = (int) mActivity.getResources().getDimension(R.dimen.app_screen_card_height);
		int w = (int) mActivity.getResources().getDimension(R.dimen.app_screen_card_width);
		int count = mWorkspace.getChildCount();
		if (index >= count) {
			return null;
		}
		AppFuncScreenItemInfo info = new AppFuncScreenItemInfo();
		View child = mWorkspace.getChildAt(index);

		/**
		 * @edit by huangshaotao
		 * @date 2012-4-27 功能表获取屏幕缩略图 调用child.draw(canvas);绘制某个屏幕时因为此时桌面已经不可见了，
		 *       某些widget可能已经在不可见时释放了图片资源导致异常发生（trying to use a recycled bitmap）
		 *       因此这里加个保护
		 */
		Bitmap bmp = null;
		try {
			bmp = Bitmap.createBitmap(w, h, Config.ARGB_8888);
			Canvas canvas = new Canvas(bmp);
			canvas.scale((float) w / child.getWidth(), (float) h / child.getHeight());
			child.draw(canvas);
		} catch (Throwable e) {
		}

		info.setVancantCellCnt(ScreenUtils.findSingleVancant(
				index == -1 ? mWorkspace.getCurrentScreen() : index, null, mWorkspace));
		info.setScreenPreviewData(bmp, index);
		return info;
	}

	private Bitmap getAppDrawCardBitmap(int index) {
		mWorkspace.forceLayout();
		int h = (int) mActivity.getResources().getDimension(R.dimen.app_screen_card_height);
		int w = (int) mActivity.getResources().getDimension(R.dimen.app_screen_card_width);
		int count = mWorkspace.getChildCount();
		if (index >= count) {
			return null;
		}
		View child = mWorkspace.getChildAt(index);
		Bitmap bmp = Bitmap.createBitmap(w, h, Config.ARGB_8888);
		Canvas canvas = new Canvas(bmp);
		canvas.scale((float) w / child.getWidth(), (float) h / child.getHeight());
		child.draw(canvas);
		canvas = null;
		return bmp;
	}

	// end -- 功能表获得桌面预览图

	/***
	 * 屏幕变红一秒钟
	 */
	public void setScreenRedBg() {
		DeskToast.makeText(mActivity, R.string.no_more_room, Toast.LENGTH_SHORT).show();
		mWorkspace.resetScreenBg(true);
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				mWorkspace.resetAllScreenBg();
			}
		}, 1000);
	}

	private void showMoveToLiveFolderToast() {
		DeskToast.makeText(mActivity, R.string.not_move_to_folder, Toast.LENGTH_SHORT).show();
		mWorkspace.resetScreenBg(true);
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				mWorkspace.resetAllScreenBg();
			}
		}, 1000);
	}

	// /**
	// * 替换放在桌面上的下载图标为指定应用
	// */
	// private void replaceDeskIcon(int itemType,String action,String
	// packageName)
	// {
	// if(null == action || null == packageName)
	// {
	// return;
	// }
	// int screenCount = mWorkspace.getChildCount();
	// for (int i = 0; i < screenCount; i++)
	// {
	// CellLayout layout = (CellLayout) mWorkspace.getChildAt(i);
	// if (layout != null)
	// {
	// int index = 0;
	// while (index < layout.getChildCount())
	// {
	// View v = layout.getChildAt(index);
	// index++;
	// if (v != null)
	// {
	// ItemInfo info = (ItemInfo) v.getTag();
	// if(info != null && info.mItemType == itemType)
	// {
	// if(((ShortCutInfo)info).mIntent != null &&
	// ((ShortCutInfo)info).mIntent.getAction() != null
	// && ((ShortCutInfo)info).mIntent.getAction().equals(action))
	// {
	// ShortCutInfo newInfo = new ShortCutInfo();
	// final AppDataEngine dataEngine = GOLauncherApp.getAppDataEngine();
	// ArrayList<AppItemInfo> dbItemInfos = dataEngine.getAllAppItemInfos();
	// for(int j = 0; j < dbItemInfos.size();j++)
	// {
	// AppItemInfo dbItemInfo = dbItemInfos.get(j);
	// if (null == dbItemInfo.mIntent.getComponent())
	// {
	// continue;
	// }
	// String dbPackageName =
	// dbItemInfo.mIntent.getComponent().getPackageName();
	// if (dbPackageName.equals(packageName)){
	// newInfo.mIntent = dbItemInfo.mIntent;
	// newInfo.mTitle = dbItemInfo.mTitle;
	// newInfo.setRelativeItemInfo(dbItemInfo);
	// newInfo.mIcon = dbItemInfo.mIcon;
	// break;
	// }
	// }
	// newInfo.mCellX = info.mCellX;
	// newInfo.mCellY = info.mCellY;
	// newInfo.mScreenIndex = info.mScreenIndex;
	// newInfo.mSpanX = 1;
	// newInfo.mSpanY = 1;
	// newInfo.mInScreenId = System.currentTimeMillis();
	// deleteItem(info,info.mScreenIndex);
	// BubbleTextView bubble = inflateBubbleTextView(
	// newInfo.mTitle, newInfo.mIcon, newInfo);
	// mWorkspace.addInScreen(bubble, newInfo.mScreenIndex,
	// newInfo.mCellX, newInfo.mCellY,
	// newInfo.mSpanX, newInfo.mSpanY, true);
	// addDesktopItem(newInfo.mScreenIndex, newInfo);
	//
	// }
	// }
	// }
	// }
	// }
	// }
	// }
	//
	/**
	 * 功能简述:替换放在桌面上的下载图标为指定应用 功能详细描述: 注意:
	 * 
	 * @author chenguanyu
	 * @param packageName
	 */
	private void replaceDeskIcon(String packageName) {
		if (null == packageName) {
			return;
		}
		int screenCount = mWorkspace.getChildCount();
		for (int i = 0; i < screenCount; i++) {
			CellLayout layout = (CellLayout) mWorkspace.getChildAt(i);
			if (layout != null) {
				int index = 0;
				while (index < layout.getChildCount()) {
					View v = layout.getChildAt(index);
					index++;
					if (v != null) {
						ItemInfo info = (ItemInfo) v.getTag();
						boolean isAdvertIcon = checkIsAdvertIcon(info);
						if (info != null && (info.mItemType == IItemType.ITEM_TYPE_SHORTCUT || isAdvertIcon)) {
							Intent tempIntent = ((ShortCutInfo) info).mIntent;
							if (tempIntent != null
									&& tempIntent.getComponent() != null
									&& tempIntent.getComponent().getPackageName() != null
									&& (tempIntent.getComponent().getPackageName()
											.equals(packageName) || (tempIntent.getComponent()
											.getPackageName()
											.equals(LauncherEnv.Plugin.RECOMMAND_GOLOCKER_PACKAGE) && packageName
											.equals(LauncherEnv.Plugin.LOCKER_PRO_PACKAGE)))) {
								ShortCutInfo newInfo = new ShortCutInfo();
								final AppDataEngine dataEngine = GOLauncherApp.getAppDataEngine();
								ArrayList<AppItemInfo> dbItemInfos = dataEngine
										.getAllAppItemInfos();
								for (int j = 0; j < dbItemInfos.size(); j++) {
									AppItemInfo dbItemInfo = dbItemInfos.get(j);
									if (null == dbItemInfo.mIntent.getComponent()) {
										continue;
									}
									String dbPackageName = dbItemInfo.mIntent.getComponent()
											.getPackageName();
									if (dbPackageName.equals(packageName)
											|| (dbPackageName
													.equals(LauncherEnv.Plugin.RECOMMAND_GOLOCKER_PACKAGE) && packageName
													.equals(LauncherEnv.Plugin.LOCKER_PRO_PACKAGE))) {
										newInfo.mIntent = dbItemInfo.mIntent;
										newInfo.mTitle = dbItemInfo.mTitle;
										newInfo.setRelativeItemInfo(dbItemInfo);
										newInfo.mIcon = dbItemInfo.mIcon;
										break;
									}
								}
								newInfo.mCellX = info.mCellX;
								newInfo.mCellY = info.mCellY;
								newInfo.mScreenIndex = info.mScreenIndex;
								newInfo.mSpanX = 1;
								newInfo.mSpanY = 1;
								newInfo.mInScreenId = System.currentTimeMillis();
								deleteItem(info, info.mScreenIndex);
								BubbleTextView bubble = inflateBubbleTextView(newInfo.mTitle,
										newInfo.mIcon, newInfo);
								mWorkspace.addInScreen(bubble, newInfo.mScreenIndex,
										newInfo.mCellX, newInfo.mCellY, newInfo.mSpanX,
										newInfo.mSpanY, true);
								addDesktopItem(newInfo.mScreenIndex, newInfo);

							}
						}
					}
				}
			}
		}
	}

	/**
	 * <br>功能简述:检查是否15屏广告图标。
	 * <br>功能详细描述:如果是15屏幕广告图标。把图片类型由自定义改为默认类型
	 * <br>注意:
	 * @param info
	 * @return
	 */
	public boolean checkIsAdvertIcon(ItemInfo info) {
		try {
			if (info == null) {
				return false;
			}
			Intent tempIntent = ((ShortCutInfo) info).mIntent;
			if (tempIntent != null && tempIntent.getAction() != null) {
				if (tempIntent.getAction().equals(ICustomAction.ACTION_SCREEN_ADVERT) && info.mItemType == IItemType.ITEM_TYPE_APPLICATION) {
					//把图片类型由自定义改为默认类型
					((ShortCutInfo) info).mFeatureIconType = ImagePreviewResultType.TYPE_DEFAULT;
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private void replaceRecommandIconInFolder(String packageName) {
		if (null == packageName) {
			return;
		}

		int screenCount = mWorkspace.getChildCount();
		FolderIcon folderIcon = null;
		for (int i = 0; i < screenCount; i++) {
			CellLayout layout = (CellLayout) mWorkspace.getChildAt(i);
			if (layout == null) {
				continue;
			}
			int index = 0;
			final int childCount = layout.getChildCount();
			while (index < childCount) {
				View v = layout.getChildAt(index);
				index++;
				if (v == null) {
					continue;
				}
				
				// 查找文件夹内符合要求的推荐图标
				ItemInfo info = (ItemInfo) v.getTag();
				if (info.mItemType == IItemType.ITEM_TYPE_USER_FOLDER) {
					int count = ((UserFolderInfo) info).getChildCount();
					ShortCutInfo newInfo = null;
					for (int j = 0; j < count; j++) {
						ShortCutInfo shortCutInfo = ((UserFolderInfo) info).getChildInfo(j);
						if (shortCutInfo != null && shortCutInfo.mIntent != null
								&& shortCutInfo.mIntent.getComponent() != null) {
							String pkgName = shortCutInfo.mIntent.getComponent().getPackageName();
							if (pkgName.equals(packageName) || (pkgName.equals(LauncherEnv.Plugin.LOCKER_PACKAGE) && packageName
									.equals(LauncherEnv.Plugin.LOCKER_PRO_PACKAGE))) {
								checkIsAdvertIcon(shortCutInfo); //如果是15屏幕广告图标。把图片类型由自定义改为默认类型
								newInfo = shortCutInfo;
								if (v instanceof FolderIcon) {
									folderIcon = (FolderIcon) v;
								}
								break;
							}
						}
					}

					if (newInfo == null) {
						continue;
					}
					final PackageManager pm = mActivity.getPackageManager();
					newInfo.mIntent = pm.getLaunchIntentForPackage(packageName);
					if (newInfo.mIntent != null) {
						final ResolveInfo resolveInfo = pm.resolveActivity(newInfo.mIntent, 0);
						if (resolveInfo != null) {
							newInfo.mTitle = resolveInfo.loadLabel(pm); // 获得应用程序的Label
							newInfo.mIcon = resolveInfo.loadIcon(pm); // 获得应用程序图标
							newInfo.mItemType = IItemType.ITEM_TYPE_APPLICATION;
							String intentString = ConvertUtils.intentToString(newInfo.mIntent);
							DataProvider dataProvider = DataProvider.getInstance(mActivity);
							dataProvider.updateFolderIntentAndType(info.mInScreenId,
									newInfo.mInScreenId, intentString, newInfo.mItemType, newInfo.mFeatureIconType);
							if (folderIcon != null) {
								FolderIcon.prepareIcon(folderIcon, (UserFolderInfo) info);
							}
							return;
						}
					}
				}
			}
		}
	}

	// 展现进度条
	private void showProgressDialog() {
		if (mGoProgressBar.getVisibility() == View.INVISIBLE) {
			mGoProgressBar.setVisibility(View.VISIBLE);
		}
	}

	// 关闭进度条
	private void dismissProgressDialog() {
		if (mGoProgressBar.getVisibility() == View.VISIBLE) {
			mGoProgressBar.setVisibility(View.INVISIBLE);
		}
	}

	// 合并文件夹后的处理
	private void completeMergeFolder(View folder, Rect rect) {
		clearDragState();
		// NOTE:做缩小图标动画
		final CellLayout currentCellLayout = getCurrentScreen();
		if (null != currentCellLayout) {
			if (currentCellLayout.startIntoFolderZoomoutAnimation(folder,
					currentCellLayout.getmMergerFolderChildView(), rect, 1)) {
				currentCellLayout.setmDrawStatus(CellLayout.DRAW_STATUS_INTO_FOLDER_ZOOMOUT);
			} else {
				currentCellLayout.setStatusNormal();
			}
		}
		mViewTemp = null;
		mRectTemp = null;
	} // end completeMergeFolder

	/*-----------------------------------以下代码仅在覆盖安装检测壁纸是否放大用到 by Yugi 12.7.6----------------------------------------*/
	private static final String PREFERENCES_FOR_ADJUST_WALLPAPER = "needToAdjustWallpaperDimension";
	private static final String PREFERENCES_FOR_ADJUST_WALLPAPER_VALUE = "needToAdjustWallpaperDimensionValue";

	/**
	 * 是否需要添加检测壁纸放大
	 * 
	 * @return
	 */
	private boolean isNeedToRecommend() {
		boolean result = false;
		GoLauncher goLauncher = GoLauncher.getContext();
		if (goLauncher == null) {
			return result;
		}
		if (goLauncher.getNewVeriosnFirstRun() && !goLauncher.getFirstRun()) {
			PreferencesManager prefs = new PreferencesManager(mActivity,
					PREFERENCES_FOR_ADJUST_WALLPAPER, Context.MODE_PRIVATE);
			return prefs.getBoolean(PREFERENCES_FOR_ADJUST_WALLPAPER_VALUE, true);
		}
		return result;
	}

	private void saveSharedPreferences(boolean result) {
		PreferencesManager prefs = new PreferencesManager(mActivity,
				PREFERENCES_FOR_ADJUST_WALLPAPER, Context.MODE_PRIVATE);
		prefs.putBoolean(PREFERENCES_FOR_ADJUST_WALLPAPER_VALUE, result);
		prefs.commit();
	}

	public void refScreen(FolderIcon folderIcon) {
		// 针对4.0以上手机，添加文件夹刷新不及时的问题
		folderIcon.setVisibility(View.INVISIBLE);
		folderIcon.setVisibility(View.VISIBLE);
		mWorkspace.postInvalidate();
	}
	
	public void animationForScreenEdit(int height, boolean isUp) {
		mLayout.requestLayout();
		if (isUp) { // 指示器上移动画
			final int normalH = (int) GoLauncher.getContext().getResources()
					.getDimension(R.dimen.screen_edit_box_container_normal);
			final int dis = (int) (height - normalH);
			TranslateAnimation animationIn = new TranslateAnimation(0, 0, dis, 0);
			animationIn.setDuration(ScreenEditBoxFrame.ANIMATION_DURATION);
			animationIn.setInterpolator(new DecelerateInterpolator());
			mIndicator.startAnimation(animationIn);
		} else { // 指示器下移动画
			TranslateAnimation animationOut = new TranslateAnimation(0, 0, -height, 0);
			animationOut.setDuration(ScreenEditBoxFrame.ANIMATION_DURATION);
			animationOut.setInterpolator(new DecelerateInterpolator());
			mIndicator.startAnimation(animationOut);
		}
	}
	
	/**
	 * <br>功能简述:获取首屏图标当前图标的信息
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public String getHomeScreenIconInfo() {
		//判断是否有5屏和是否首屏
		if (mWorkspace.getChildCount() != 5) {
			return null;
		}
		
		CellLayout cellLayout = mWorkspace.getScreenView(2); //获取首屏
		
		int screenViewSize = cellLayout.getChildCount();
		StringBuffer buffer = new StringBuffer();
		//遍历对应的屏幕控件
		for (int i = 0; i < screenViewSize; i++) {
			Object object = cellLayout.getChildAt(i).getTag();
			String cacheString = AdvertHomeScreenUtils.getIconInfoString(object);
			buffer.append(cacheString);
		}
		return buffer.toString();
	}
	
	/**
	 * <br>功能简述:设置首屏图标当前图标的信息缓存
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public boolean setHomeScreenIconCache() {
		String iconInfoString = getHomeScreenIconInfo();
		AdvertHomeScreenUtils.saveHomeScreenCache(mActivity, iconInfoString);
		return true;
	}
	
	/**
	 * <br>功能简述:判断是否可以重新请求首屏广告图标
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public boolean isCanChangeHomeScreenAdvertIcon() {
		try {
			//判断是否有5屏和是否首屏
			if (mWorkspace.getChildCount() != 5) {
				return false;
			}
			String cacheString = AdvertHomeScreenUtils.getHomeScreenCache(mActivity);
			if (!TextUtils.isEmpty(cacheString)) {
				String[] cacheList = cacheString.split(";");
				int cacheSize = cacheList.length;
				
				CellLayout cellLayout = mWorkspace.getScreenView(2); //获取首屏
				int screenViewSize = cellLayout.getChildCount();
//				Log.i("lch", "首屏缓存：" + cacheString);
//				Log.i("lch", "当前屏幕图标个数：" + screenViewSize);
//				Log.i("lch", "首屏缓存图标个数：" + cacheSize);
				
				if (cacheSize != screenViewSize) {
//					Log.i("lch", "缓存图标个数和当前屏幕图标个数不一致");
					return false;
				}
				
				//遍历对应的屏幕控件
				for (int i = 0; i < screenViewSize; i++) {
					Object object = cellLayout.getChildAt(i).getTag();
					String iconInfoString = AdvertHomeScreenUtils.getIconInfoString(object);
					if (!cacheString.contains(iconInfoString)) {
//						Log.i("lch3", "首屏图标已改变：" + iconInfoString);
						return false;
					} 
				}
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	
	/**
	 * <br>功能简述:应用中心推荐图标在桌面生成快捷方式
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param object
	 * @return
	 */
	public boolean addAppStoreRecommendIcon(Object object) {
		try {
			if (object == null || !(object instanceof String)) {
				return false;
			}
			String packageName = String.valueOf(object);
			
			if (mScreenSettingInfo == null) {
				mScreenSettingInfo = GOLauncherApp.getSettingControler().getScreenSettingInfo();
			}
			int mainScreenIndex = mScreenSettingInfo.mMainScreen;
			int screenTotalSize = mWorkspace.getChildCount();
			if (screenTotalSize == 0) {
				return false;
			}
			
			for (int i = mainScreenIndex; i < screenTotalSize; i++) {
				int checkScreenIndex = i;
				
				int [] cellPos = new int[2];
				// 判断是否可以添加图标
				boolean isCanAdd = ScreenUtils.findVacant(cellPos, 1, 1, checkScreenIndex, mWorkspace);
				if (isCanAdd && cellPos != null && cellPos.length == 2) {
					//创建需要添加的图标ShortCutInfo
					ShortCutInfo newInfo = new ShortCutInfo();
					final AppDataEngine dataEngine = GOLauncherApp.getAppDataEngine();
					ArrayList<AppItemInfo> dbItemInfos = dataEngine.getAllAppItemInfos();
					for (AppItemInfo dbItemInfo : dbItemInfos) {
						if (dbItemInfo.mIntent.getComponent() == null) {
							continue;
						}
						String dbPackageName = dbItemInfo.mIntent.getComponent().getPackageName();
						if (dbPackageName.equals(packageName)) {
							newInfo.mIntent = dbItemInfo.mIntent;
							newInfo.mTitle = dbItemInfo.mTitle;
							dbItemInfo.mIsNewRecommendApp = true; //添加右上角new标志
							newInfo.setRelativeItemInfo(dbItemInfo);
							newInfo.mIcon = dbItemInfo.mIcon;
							break;
						}
					}
					
					newInfo.mCellX = cellPos[0];
					newInfo.mCellY = cellPos[1];
					newInfo.mScreenIndex = checkScreenIndex;
					newInfo.mSpanX = 1;
					newInfo.mSpanY = 1;
					newInfo.mInScreenId = System.currentTimeMillis();
					BubbleTextView bubble = inflateBubbleTextView(newInfo.mTitle, newInfo.mIcon, newInfo);
					mWorkspace.addInScreen(bubble, newInfo.mScreenIndex, newInfo.mCellX, newInfo.mCellY, newInfo.mSpanX,
							newInfo.mSpanY, true);
					addDesktopItem(newInfo.mScreenIndex, newInfo);
					
					//设置缓存设置该应用安装的时间,做图标抖动和8小时请求参数判断
					long time = System.currentTimeMillis();
					AdvertControl.getAdvertControlInstance(mActivity).setOpenCache(packageName, String.valueOf(time));
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
