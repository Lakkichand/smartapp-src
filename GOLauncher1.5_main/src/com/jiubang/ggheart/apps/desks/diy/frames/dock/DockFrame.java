package com.jiubang.ggheart.apps.desks.diy.frames.dock;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.animation.MyAnimationUtils;
import com.jiubang.core.framework.AbstractFrame;
import com.jiubang.core.framework.IFrameManager;
import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.core.message.IMessageHandler;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.IRequestCodeIds;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.AbsDockView;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.AbsLineLayout;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.DockIconView;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.DockLogicControler;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.IOperationHandler;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.autofit.DockView;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.unfit.UnfitDockView;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.DeskUserFolderFrame;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenUtils;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.Workspace;
import com.jiubang.ggheart.apps.desks.diy.themescan.EditDialog;
import com.jiubang.ggheart.apps.desks.dock.DockUtil;
import com.jiubang.ggheart.apps.desks.imagepreview.ChangeIconPreviewActivity;
import com.jiubang.ggheart.apps.desks.imagepreview.ImagePreviewResultType;
import com.jiubang.ggheart.apps.desks.settings.DockGestureRespond;
import com.jiubang.ggheart.components.IQuickActionId;
import com.jiubang.ggheart.components.QuickActionMenu;
import com.jiubang.ggheart.components.QuickActionMenu.onActionListener;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.DockItemControler;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.DockItemInfo;
import com.jiubang.ggheart.data.info.FeatureItemInfo;
import com.jiubang.ggheart.data.info.ItemInfo;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.data.info.ShortCutSettingInfo;
import com.jiubang.ggheart.data.info.UserFolderInfo;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 快捷条层
 * 
 * @author ruxueqin
 * @version 1.0
 */
public class DockFrame extends AbstractFrame
		implements
			AnimationListener,
			onActionListener,
			IMessageHandler,
			IOperationHandler {
	private DockLogicControler mDockControler; // 逻辑控制器
	private FrameLayout mContextView; // dockFrame根容器
	private AbsDockView mLayout; // 一级容器，可以区分是否自适应

	private int mAnimationType;
	private QuickActionMenu mQuickActionMenu; // 长按弹出菜单
	private boolean mIsAsycnLoadFinished = false; // 标志dockview是否初始化完全(启动桌面时，异步拿后台数据)

	public static final int HIDE_ANIMATION = 0; // 隐藏时带动画
	public static final int HIDE_ANIMATION_NO = 1; // 隐藏时不带动画

	/**
	 * 快捷条层构造方法
	 * 
	 * @param activity
	 * @param frameManager
	 */
	public DockFrame(Activity activity, IFrameManager frameManager, int id) {
		super(activity, frameManager, id);
		addLayout();
	}

	private void addLayout() {
		LayoutInflater inflater = (LayoutInflater) mActivity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (getSettingInfo().mAutoFit) {
			mLayout = (AbsDockView) inflater.inflate(R.layout.diy_dock, null);
		} else {
			mLayout = (AbsDockView) inflater.inflate(R.layout.diy_dock_unfit, null);
		}
		if (mContextView == null) {
			mContextView = new FrameLayout(mActivity);
		}
		mContextView.addView(mLayout);
		if (mDockControler == null) {
			mDockControler = new DockLogicControler(mActivity.getApplicationContext(),
					GOLauncherApp.getAppDataEngine(), AppCore.getInstance()
							.getSysShortCutControler());
		}
		mLayout.setHandler(this, mActivity, this, mDockControler);
	}

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			// DOCK条初始化
				case DockUtil.HANDLE_INIT_DOCK_FRAME :
					mIsAsycnLoadFinished = false;
					//5个特殊图标，使用主题
					DockItemControler controler = AppCore.getInstance().getDockItemControler();
					controler.useStyle(getSettingInfo().mStyle);
					initLayout();
					int curLine = (msg.arg1 >= 0 && msg.arg1 < mLayout.getLineLayoutContainer()
							.getChildCount()) ? msg.arg1 : 0;
					mLayout.getLineLayoutContainer().setCurrentScreen(curLine);
					mLayout.updateAllNotifications();
					mLayout.invalidate();

					// 是否隐藏DOCK条
					if (!ShortCutSettingInfo.sEnable) {
						GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
								IFrameworkMsgId.HIDE_FRAME, IDiyFrameIds.DOCK_FRAME, null, null);
					}
					mIsAsycnLoadFinished = true;
					break;
				// 弹出有脏数据提示框
				case DockUtil.HANDLE_SHOW_DIRTYDATA_TIPS : {
					TextView textView = new TextView(mActivity);
					textView.setText(mActivity.getResources().getString(
							R.string.found_dock_dirty_data)
							+ DockLogicControler.sDirtyDataStr);
					AlertDialog dialog = new AlertDialog.Builder(mActivity)
							.setTitle(
									mActivity.getResources().getString(
											R.string.found_dock_dirty_data_title))
							.setCancelable(true).setIcon(null).setPositiveButton("OK", null)
							.setView(textView).create();

					dialog.show();
				}
					break;
				case DockUtil.HANDLE_ANIMATION_ADD_ITEM_FROM_FOLDER : {
					try {
						View view = (View) msg.obj;
						ScaleAnimation scaleAnimation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f,
								view.getWidth() / 2, view.getHeight() / 2);
						scaleAnimation.setDuration(300);
						scaleAnimation.setAnimationListener(DockFrame.this);
						mAnimationType = DockUtil.HANDLE_ANIMATION_ADD_ITEM_FROM_FOLDER;
						view.startAnimation(scaleAnimation);
					} catch (Exception e) {
					}
				}
					break;
				default :
					break;
			}
		};
	};

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public boolean handleMessage(Object who, int type, int msgId, int param, Object object,
			List objects) {
		super.handleMessage(who, type, msgId, param, object, objects);
		boolean ret = false;
		switch (msgId) {
			case IDiyMsgIds.EVENT_LOAD_FINISH :// 收到广播,初始化加载完成
			{
				// 启动，异步拿后台数据
				mLayout.getShortCutItems();
				// 通知UI排版
				mHandler.sendEmptyMessage(DockUtil.HANDLE_INIT_DOCK_FRAME);
			}
				break;

			case IFrameworkMsgId.SYSTEM_CONFIGURATION_CHANGED : {
				hideQuickActionMenu(false);
				mLayout.configurationChange();
			}
				break;
			case IDiyMsgIds.BACK_TO_MAIN_SCREEN : {
				ret = true;
			}
				break;
			case IDiyMsgIds.DOCK_SHOW : {
				showDock();
			}
				break;
			case IDiyMsgIds.DOCK_HIDE : {
				hideDock(param);
			}
				break;
			case IDiyMsgIds.DOCK_SETTING_CHANGED : {
				settingChange();
			}
				break;

			case IDiyMsgIds.DOCK_SETTING_CHANGED_STYLE : {
				changeStyle(object);
			}
				break;

			case IDiyMsgIds.IS_EXIST_DOCK_TRASH_DATA : {
				ret = mDockControler.hasDirtyData();
			}
				break;

			case IDiyMsgIds.CLEAN_DOCK_TRASH_DATA : {
				mDockControler.clearDockDirtyData();
			}
				break;

			case IDiyMsgIds.NOTIFICATION_CHANGED : {
				if (object instanceof Integer && null != mLayout && mIsAsycnLoadFinished) {
					mLayout.notificationChanged(object, param);
				}
			}
				break;

			case IDiyMsgIds.DOCK_APP_UNINSTALL_NOTIFICATION : {
				uninstallNotification(objects);
			}
				break;

			case IDiyMsgIds.DESK_THEME_CHANGED : {
				mLayout.reloadFolderContent();
				mDockControler.doThemeChanged();
				mLayout.doStyleChange();

				// 如果切换回默认主题，需要手动更新ＤＯＣＫ背景，因为其他主题间的切换会改ＤＯＣＫ开关设置，会自动更新ＤＯＣＫ背景，但默认主题不会
				mLayout.updateSlaverBg();
			}
				break;

			case IDiyMsgIds.EVENT_REFLUSH_TIME_IS_UP : {
				mLayout.reloadFolderContent();
				mDockControler.handleEventReflushTimeIsUp();
			}
				break;

			case IDiyMsgIds.EVENT_REFLUSH_SDCARD_IS_OK : {
				mLayout.reloadFolderContent();
				mDockControler.handleEventReflashSdcardIsOk();
			}
				break;

			case IDiyMsgIds.EVENT_SD_MOUNT : {
				mLayout.reloadFolderContent();
				mDockControler.handleEventSdMount();
			}
				break;

			case IDiyMsgIds.EVENT_UNINSTALL_APP : {
				if (objects instanceof ArrayList<?> && mIsAsycnLoadFinished) {
					mDockControler.handleEventUninstallApps((ArrayList<AppItemInfo>) objects);
				}
			}
				break;

			case IDiyMsgIds.EVENT_UNINSTALL_PACKAGE : {
				if (object instanceof String && null != mLayout && mIsAsycnLoadFinished) {
					mDockControler.handleEventUninstallPackage((String) object);
				}
			}
				break;

			case IDiyMsgIds.DOCK_SHOW_DIRTYDATA_TIPS : {
				mHandler.sendEmptyMessage(DockUtil.HANDLE_SHOW_DIRTYDATA_TIPS);
			}
				break;

			case IDiyMsgIds.REMOVE_ACTION_MENU : {
				if (mQuickActionMenu != null && mQuickActionMenu.isShowing()) {
					hideQuickActionMenu(false);
					ret = true;
				}
				//可能是拖动中换页引起的，所以要清除移动到桌面动画
				mLayout.clearMoveToScreenAnim();
			}
				break;

			case IDiyMsgIds.QUICKACTION_EVENT : {
				ret = handleQuickEvent(object, param);
			}
				break;

			case IDiyMsgIds.EVENT_LOAD_ICONS_FINISH : {
				mDockControler.updataAllFolder();
			}
				break;

			case IDiyMsgIds.DOCK_SHOW_GESTURE_SELETION : {
				showGestureSeletion();
			}
				break;

			case IDiyMsgIds.DOCK_UNINSTALL_APP : {
				if (object instanceof DockIconView) {
					mDockControler.actionUninstall((DockIconView) object, mActivity);
				}
			}
				break;
			case IDiyMsgIds.DOCK_GET_VIEW_IMAGE :
				if (null != object && object instanceof Canvas) {
					Canvas canvas = (Canvas) object;
					canvas.save();
					mLayout.dispatchDraw(canvas);
					canvas.restore();
				}
				break;
			default :
				ret = mLayout.handleMessage(who, type, msgId, param, object, objects);
				break;
		}
		return ret;
	}

	/***
	 * 显示dock条
	 */
	private void showDock() {
		if (null != mLayout && mIsAsycnLoadFinished) {
			mLayout.clearAnimation();
			GOLauncherApp.getSettingControler().updateEnable(true);
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IFrameworkMsgId.SHOW_FRAME,
					IDiyFrameIds.DOCK_FRAME, null, null);
			mAnimationType = DockUtil.ANIMATION_ENTER_SHOW;
			if (!startAnimation(MyAnimationUtils.POP_FROM_LONG_END_SHOW)) {
				onAnimationEnd(null);
			}
			// 通知workspace格局发生变化
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
					IDiyMsgIds.SCREEN_REQUEST_LAYOUT, Workspace.CHANGE_SOURCE_DOCK, 1, null);
		}
	}

	/***
	 * 隐藏dock条
	 * 
	 * @param param
	 */
	private void hideDock(int param) {
		if (null != mLayout && mIsAsycnLoadFinished) {
			// 从设置隐藏DOCK条，直接隐藏。不做动画
			if (param == HIDE_ANIMATION_NO) {
				GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						IFrameworkMsgId.HIDE_FRAME, IDiyFrameIds.DOCK_FRAME, null, null);
				GOLauncherApp.getSettingControler().updateEnable(false);
				// 通知workspace格局发生变化
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
						IDiyMsgIds.SCREEN_REQUEST_LAYOUT, Workspace.CHANGE_SOURCE_DOCK, 0, null);
			}
			// 从快捷方式隐藏DOCK条，需要先做动画
			else {
				Animation animation = mLayout.getAnimation();
				if (null == animation || !animation.hasStarted()) {
					mAnimationType = DockUtil.ANIMATION_LEAVE_HIDE;
					if (!startAnimation(MyAnimationUtils.POP_TO_LONG_END_HIDE)) {
						onAnimationEnd(null);
					}
					// 通知workspace格局发生变化
					GoLauncher
							.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
									IDiyMsgIds.SCREEN_REQUEST_LAYOUT, Workspace.CHANGE_SOURCE_DOCK,
									0, null);
				}
			}
		}
	}

	/***
	 * Dock显示隐藏动画
	 * 
	 * @param animId
	 * @return
	 */
	private boolean startAnimation(int animId) {
		if (mLayout == null) {
			return false;
		}
		Animation animation = MyAnimationUtils.getPopupAnimation(animId, -1);
		if (animation != null) {
			animation.setAnimationListener(this);
			mLayout.startAnimation(animation);
			return true;
		}
		return false;
	}

	/**
	 * 更改自适应模式
	 */
	private void changeAutoFit() {
		int curLine = mLayout.getLineLayoutContainer().getCurLine();
		mLayout.clearSelf();
		mContextView.removeAllViews();
		mLayout = null;
		addLayout();

		Message msg = new Message();
		msg.what = DockUtil.HANDLE_INIT_DOCK_FRAME;
		msg.arg1 = curLine;
		mHandler.sendMessage(msg);

		// 换模式后，图标大小要更新
		int size = mDockControler.getShortCutItems().size();
		for (int i = 0; i < size; i++) {
			mLayout.updateLineLayoutIconsSize(i);
		}
	}

	/***
	 * DOCK设置更改了
	 */
	private void settingChange() {
		boolean needChangeAutoFit = getSettingInfo().mAutoFit
				? !(mLayout instanceof DockView)
				: !(mLayout instanceof UnfitDockView);

		if (needChangeAutoFit) {
			changeAutoFit();
		} else {
			mLayout.doWithSettingChange();
			mDockControler.controlNotification();
		}
	}

	/***
	 * DOCK风格更改了
	 * 
	 * @param object
	 */
	private void changeStyle(Object object) {
		if (object instanceof String) {
			String style = (String) object;
			// 5个特殊图标
			DockItemControler controler = AppCore.getInstance().getDockItemControler();
			controler.useStyle(style);
			mLayout.doStyleChange();
		}
	}

	/***
	 * 卸载了通讯程序
	 */
	@SuppressWarnings("rawtypes")
	private void uninstallNotification(List objects) {
		// 只有卸载的是通讯统计程序才收到消息
		if (objects instanceof ArrayList<?> && null != mLayout && mIsAsycnLoadFinished) {
			try {
				List<DockIconView> list = mLayout.getCurrentAllDockIcons();
				int size = (objects.size() < list.size()) ? objects.size() : list.size();
				for (int i = 0; i < size; i++) {
					if (objects.get(i) instanceof int[]) {
						int[] index = (int[]) (objects.get(i));
						AbsLineLayout lineLayout = (AbsLineLayout) mLayout.getLineLayoutContainer()
								.getChildAt(index[0]);
						DockIconView dockIconView = (DockIconView) lineLayout.getChildAt(index[1]);
						dockIconView.reset();
					}
				}
				list.clear();
				list = null;
			} catch (Throwable e) {
				// 有异常，不处理
				e.printStackTrace();
			}
		}
	}

	/**
	 * 显示操作菜单
	 */
	@Override
	public boolean showQuickActionMenu(View target) {
		// hideQuickActionMenu(false);
		if (target == null) {
			return false;
		}

		int[] xy = new int[2];
		target.getLocationInWindow(xy);
		Rect targetRect = new Rect(xy[0], xy[1], xy[0] + target.getWidth(), xy[1]
				+ target.getHeight());

		mQuickActionMenu = new QuickActionMenu(mActivity, target, targetRect, mLayout, this);

		mQuickActionMenu.addItem(IQuickActionId.CHANGE_ICON_DOCK, R.drawable.icon_change,
				R.string.menuitem_change_icon_dock);
		mQuickActionMenu.addItem(IQuickActionId.CHANGE_GESTURE_DOCK,
				R.drawable.dock_menu_change_gesture, R.string.menuitem_change_gesture_dock);
//		if (!DockUtil.isTheLastDockAppdrawer(target)) {
			mQuickActionMenu.addItem(IQuickActionId.DELETE, R.drawable.icon_del, R.string.deltext);
//		}
		mQuickActionMenu.show();
		return true;
	}

	/**
	 * 取消弹出菜单
	 * 
	 * @param dismissWithCallback
	 *            ， 是否回调， true仅取消菜单显示，false会回调到
	 *            {@link QuickActionMenu.onActionListener#onActionClick(int, View)}
	 *            并传回一个{@link IQuickActionId#CANCEL}事件
	 */
	@Override
	public void hideQuickActionMenu(boolean dismissWithCallback) {
		if (mQuickActionMenu != null) {
			if (dismissWithCallback) {
				mQuickActionMenu.cancel();
			} else {
				mQuickActionMenu.dismiss();
			}
			mQuickActionMenu = null;
		}
	}

	/***
	 * 初始化
	 */
	private void initLayout() {
		mLayout.setHandler(this, mActivity, this, mDockControler);
		initLayoutSetting();
		mLayout.init();
	}

	/**
	 * 初始化设置信息
	 */
	private void initLayoutSetting() {
		mLayout.getLineLayoutContainer().setCycle(getSettingInfo().mAutoRevolve); // 设置循环模式
		mDockControler.controlNotification();
	}

	@Override
	public void onActionClick(int action, Object target) {
		if (null == target || null == ((DockIconView) target).getInfo()) {
			return;
		}

		switch (action) {
			case IQuickActionId.DELETE :
				Long id = ((DockIconView) target).getInfo().mItemInfo.mInScreenId;
				if (((DockIconView) target).getInfo().mItemInfo instanceof UserFolderInfo) {
					// 如果是文件夹，先清除文件夹内容
					mDockControler.removeDockFolder(id);
				}
				handleMessage(this, -1, IDiyMsgIds.DELETE_DOCK_ITEM, -1, id, null);
				//用户统计
				StatisticsData.countUserActionData(
						StatisticsData.DESK_ACTION_ID_LONG_CLICK_DOCK_ICON,
						StatisticsData.USER_ACTION_THREE, IPreferencesIds.DESK_ACTION_DATA);
				break;

			case IQuickActionId.CHANGE_ICON_DOCK :
				String defaultNameString = "";
				Bitmap defaultBmp = null;
				Bundle bundle = new Bundle();

				FeatureItemInfo featureItemInfo = ((DockIconView) target).getInfo().mItemInfo;
				if (featureItemInfo instanceof UserFolderInfo) {
					ChangeIconPreviewActivity.sFromWhatRequester = ChangeIconPreviewActivity.DOCK_FOLDER_STYLE; // 文件夹
					CharSequence iconName = ((UserFolderInfo) featureItemInfo).mTitle;
					if (iconName != null) {
						defaultNameString = iconName.toString(); // 系统图标名称
					}
					featureItemInfo.mFeatureIconType = ImagePreviewResultType.TYPE_DEFAULT;
					defaultBmp = ScreenUtils.getFolderBackIcon().getBitmap();
				} else if (featureItemInfo instanceof ShortCutInfo) {
					ChangeIconPreviewActivity.sFromWhatRequester = ChangeIconPreviewActivity.DOCK_STYLE_FROM_EDIT; // 图标
					CharSequence iconName = ((ShortCutInfo) featureItemInfo).mTitle;
					if (iconName != null) {
						defaultNameString = iconName.toString(); // 系统图标名称
					} else {
						defaultNameString = ((ShortCutInfo) (((DockIconView) target).getInfo().mItemInfo))
								.getFeatureTitle(); // dock条自定义图标
					}
					BitmapDrawable drawableTemp = mDockControler
							.getOriginalIcon((ShortCutInfo) featureItemInfo);
					if (drawableTemp != null) {
						defaultBmp = drawableTemp.getBitmap();
					}
				}
				bundle.putString(ChangeIconPreviewActivity.DEFAULT_NAME, defaultNameString);
				if (defaultBmp != null) {
					bundle.putParcelable(ChangeIconPreviewActivity.DEFAULT_ICON_BITMAP, defaultBmp);
				}
				Intent intent = new Intent(mActivity, ChangeIconPreviewActivity.class);
				intent.putExtras(bundle);
				try {
					mActivity.startActivityForResult(intent, IRequestCodeIds.REQUEST_THEME_FORICON);
				} catch (SecurityException e) {
					Toast.makeText(mActivity, "SecurityException, operation Fail!", Toast.LENGTH_SHORT).show();
				}
				//用户统计
				StatisticsData.countUserActionData(
						StatisticsData.DESK_ACTION_ID_LONG_CLICK_DOCK_ICON,
						StatisticsData.USER_ACTION_ONE, IPreferencesIds.DESK_ACTION_DATA);
				break;

			case IQuickActionId.CHANGE_GESTURE_DOCK :
				showGestureSeletion();
				//用户统计
				StatisticsData.countUserActionData(
						StatisticsData.DESK_ACTION_ID_LONG_CLICK_DOCK_ICON,
						StatisticsData.USER_ACTION_TWO, IPreferencesIds.DESK_ACTION_DATA);
				break;
			default :
				break;
		}
	}

	/**
	 * 弹出手势选择框
	 */
	private void showGestureSeletion() {
		DockGestureRespond aDockGestureRespond = DockGestureRespond
				.getDockGestureRespond(mActivity);
		aDockGestureRespond.mListener = mLayout;
		aDockGestureRespond.show(null);
	}

	@Override
	public int getId() {
		return IDiyFrameIds.DOCK_FRAME;
	}

	/***
	 * 快捷菜单事件处理
	 * 
	 * @param object
	 * @param msg
	 * @return
	 */
	private boolean handleQuickEvent(Object object, int msg) {
		boolean ret = false;
		switch (msg) {
			case IQuickActionId.DELETE :
				ItemInfo itemInfo = (ItemInfo) ((View) object).getTag();
				if (itemInfo instanceof UserFolderInfo) {
					mDockControler.removeDockFolder(itemInfo.mInScreenId);
				}
				ret = true;
				break;
			case IQuickActionId.RENAME :
				if (null == mLayout.getCurretnIcon() || null == mLayout.getCurretnIcon().getInfo()) {
					return false;
				}
				final EditDialog editDialog = new EditDialog(mActivity, mActivity.getResources()
						.getString(R.string.text_rename));
				editDialog.setPositiveButton(mActivity.getResources().getString(R.string.ok),
						new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								final String text = editDialog.getText();
								updateFolderName(text);
							}
						});
				editDialog.setNegativeButton(mActivity.getResources().getString(R.string.cancle),
						null);
				CharSequence title = ScreenUtils
						.getItemTitle(mLayout.getCurretnIcon().getInfo().mItemInfo);
				if (null != title) {
					editDialog.setText(title.toString());
				}
				editDialog.showWithInputMethod();
				ret = true;
				break;
			default :
				break;
		}
		return ret;
	}

	/***
	 * 更新重命名
	 * 
	 * @param text
	 */
	private void updateFolderName(String text) {
		if (null != mLayout.getCurretnIcon()) {
			DockItemInfo item = mLayout.getCurretnIcon().getInfo();
			if (null != item && item.mItemInfo != null && item.mItemInfo instanceof UserFolderInfo) {
				item.mItemInfo.mFeatureTitle = text;
				((UserFolderInfo) item.mItemInfo).mTitle = text;
				mDockControler.updateDockItem(item.mItemInfo.mInScreenId, item);
				// 如果当前文件夹是打开的，则发消息更新编辑框的文字
				if (((UserFolderInfo) item.mItemInfo).mOpened) {
					GoLauncher.sendMessage(this, IDiyFrameIds.DESK_USER_FOLDER_FRAME,
							DeskUserFolderFrame.UPDATE_FOLDER_NAME, -1, text, null);
				}
			}
		}
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		switch (mAnimationType) {
			case DockUtil.ANIMATION_LEAVE_HIDE :
				GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						IFrameworkMsgId.HIDE_FRAME, IDiyFrameIds.DOCK_FRAME, null, null);
				GOLauncherApp.getSettingControler().updateEnable(false);
				break;
		}
		if (null != mLayout) {
			mLayout.setAnimation(null);
		}
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
	}

	@Override
	public void onAnimationStart(Animation animation) {
	}

	@Override
	public void cleanHandlerMsg() {
		if (mHandler != null) {
			mHandler.removeMessages(DockUtil.HANDLE_INIT_DOCK_FRAME);
			mHandler.removeMessages(DockUtil.HANDLE_SHOW_DIRTYDATA_TIPS);
			mHandler.removeMessages(DockUtil.HANDLE_ANIMATION_ADD_ITEM_FROM_FOLDER);
		}
	}

	@Override
	public View getContentView() {
		return mContextView;
	}

	@Override
	public void onVisiable(int visibility) {
		super.onVisiable(visibility);
		if (View.VISIBLE == visibility) {
			mLayout.requestLayout();
		}
	}

	@Override
	public void onAdd() {
		super.onAdd();
	}

	@Override
	public void onRemove() {
		super.onRemove();
	}

	@Override
	public void onResume() {
		super.onResume();
		mLayout.requestLayout();

		if (null != mLayout.getCurretnIcon()) {
			// 避免出现missing ACTION_UP而出现的DOCK焦点发光背景图片不消失问题
			mLayout.getCurretnIcon().setmIsBgShow(false);
		}
		AppCore.getInstance().getNotificationControler().checkNotification();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		clearSelf();
	}

	/**
	 * 释放资源
	 */
	protected void clearSelf() {
		cleanHandlerMsg();
		if (null != mLayout) {
			mLayout.clearSelf();
		}
	}

	@Override
	protected void setVisibility(int visibility) {
		// 特殊考虑，外部设置可见性不一定成功，要针对具体风格主题看是否永久隐藏dock
		boolean bool;
		if (ShortCutSettingInfo.sEnable || !mIsAsycnLoadFinished) {
			// 当dock设置为显示，或没加载完成时，响应外部setvisibility命令
			bool = true;
		} else {
			bool = false;
		}
		if (bool) {
			super.setVisibility(visibility);
		}
	};

	@Override
	public void onForeground() {
		// 特殊考虑，外部设置可见性不一定成功，要针对具体风格主题看是否永久隐藏dock
		boolean bool;
		if (ShortCutSettingInfo.sEnable) {
			bool = true;
		} else {
			bool = false;
		}
		if (bool) {
			super.onForeground();
		}
	}

	public ShortCutSettingInfo getSettingInfo() {
		return GOLauncherApp.getSettingControler().getShortCutSettingInfo();
	}

}
