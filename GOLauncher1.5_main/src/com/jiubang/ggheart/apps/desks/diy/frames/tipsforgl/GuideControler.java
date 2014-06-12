package com.jiubang.ggheart.apps.desks.diy.frames.tipsforgl;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;

import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.core.message.IMessageHandler;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.desks.diy.StatusBarHandler;
import com.jiubang.ggheart.apps.desks.diy.frames.cover.CoverFrame;
import com.jiubang.ggheart.apps.desks.diy.frames.preview.SensePreviewFrame;
import com.jiubang.ggheart.apps.desks.diy.frames.preview.SenseWorkspace;
import com.jiubang.ggheart.data.DataProvider;
import com.jiubang.ggheart.data.GlobalSetConfig;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.data.info.GestureSettingInfo;
import com.jiubang.ggheart.data.statistics.StaticTutorial;
import com.jiubang.ggheart.launcher.GOLauncherApp;
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  dingzijian
 * @date  [2013-1-7]
 */
public class GuideControler implements IMessageHandler {

	public static final int CLOUD_ID_DOCK_GESTURE = 0x1;
	public static final int CLOUD_ID_CUSTOM_GESTURE = 0x2;
	public static final int CLOUD_ID_SCREEN_PRIVIEW = 0x3;
	public static final int CLOUD_ID_SUPER_WIDGET = 0x4;
	

	public static final int CLOUD_TYPE_SCREEN = 0x4;
	public static final int CLOUD_TYPE_SCREEN_PRIVIEW = 0x5;

	private static GuideControler sGuideControler;
	private GuideCloudView mDockGesture;
	private GuideCloudView mCustomGesture;
	private GuideCloudView mScreenPriview;
	private GuideCloudView mSuperWidget;

	private HashMap<Integer, GuideCloudView> mCloudViewIDMap;

	private PreferencesManager mSharedPreferences;
	private Handler mHandler;

	private Context mContext;
	private GuideControler(Context context) {
		mContext = context;
		mCloudViewIDMap = new HashMap<Integer, GuideCloudView>();
		mSharedPreferences = new PreferencesManager(mContext,
				IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
		mHandler = new Handler(Looper.getMainLooper());
		GoLauncher.registMsgHandler(this);
	}

	public static GuideControler getInstance(Context context) {
		if (sGuideControler == null) {
			sGuideControler = new GuideControler(context);
		}
		return sGuideControler;
	}

	public void addToCoverFrame(int cloudVIewID) {
		View showVIew = null;
		switch (cloudVIewID) {
			case CLOUD_ID_DOCK_GESTURE :
				mDockGesture = new GuideCloudView(mContext);
				mDockGesture.setId(CLOUD_ID_DOCK_GESTURE);
				mCloudViewIDMap.put(CLOUD_TYPE_SCREEN, mDockGesture);
				showVIew = mDockGesture;
				break;
			case CLOUD_ID_CUSTOM_GESTURE :
				mCustomGesture = new GuideCloudView(mContext);
				mCustomGesture.setId(CLOUD_ID_CUSTOM_GESTURE);
				mCloudViewIDMap.put(CLOUD_TYPE_SCREEN, mCustomGesture);
				showVIew = mCustomGesture;
				break;
			case CLOUD_ID_SCREEN_PRIVIEW :
				mScreenPriview = new GuideCloudView(mContext);
				mScreenPriview.setId(CLOUD_ID_SCREEN_PRIVIEW);
				mCloudViewIDMap.put(CLOUD_TYPE_SCREEN_PRIVIEW, mScreenPriview);
				showVIew = mScreenPriview;
				break;
			case CLOUD_ID_SUPER_WIDGET :	
				mSuperWidget = new GuideCloudView(mContext);
				mSuperWidget.setId(CLOUD_ID_SUPER_WIDGET);
				mCloudViewIDMap.put(CLOUD_TYPE_SCREEN, mSuperWidget);
				showVIew = mSuperWidget;
			default :
				break;
		}
		FrameLayout container = new FrameLayout(mContext);
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		container.addView(showVIew, params);
		GoLauncher.sendMessage(mContext, IDiyFrameIds.SCHEDULE_FRAME,
				IDiyMsgIds.COVER_FRAME_ADD_VIEW, CoverFrame.COVER_VIEW_SCREEN_GUIDE, container,
				null);
	}

	public void removeFromCoverFrame(int cloudVIewID) {
		switch (cloudVIewID) {
			case CLOUD_ID_DOCK_GESTURE :
			case CLOUD_ID_CUSTOM_GESTURE :
			case CLOUD_ID_SUPER_WIDGET :
				mCloudViewIDMap.remove(CLOUD_TYPE_SCREEN);
				break;
			case CLOUD_ID_SCREEN_PRIVIEW :
				mCloudViewIDMap.remove(CLOUD_TYPE_SCREEN_PRIVIEW);
				break;
			default :
				break;
		}
		GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
				IDiyMsgIds.COVER_FRAME_REMOVE_VIEW, CoverFrame.COVER_VIEW_SCREEN_GUIDE, null, null);
	}

	public void hideCloudView() {
		GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
				IDiyMsgIds.COVER_FRAME_REMOVE_VIEW, CoverFrame.COVER_VIEW_SCREEN_GUIDE, null, null);
	}
	/**
	 * 功能简述:弹出dock图标提示
	 * 功能详细描述:检查是否需要弹出dock图标提示并根据情况弹出提示
	 * 注意:
	 * @return true 需要弹出提示，并弹出提示
	 * 		   false 不需要弹出提示
	 */
	public void showDockGesture() {
		boolean shouldshowguide = mSharedPreferences.getBoolean(
				IPreferencesIds.SHOULD_SHOW_DOCK_BAR_ICON_GESTURE, true);
		if (shouldshowguide) {
			if (mDockGesture != null && mDockGesture.isShown()
					&& isCloudViewShowing(CLOUD_ID_DOCK_GESTURE)) {
				return;
			}
			addToCoverFrame(CLOUD_ID_DOCK_GESTURE);
			mDockGesture.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					removeFromCoverFrame(CLOUD_ID_DOCK_GESTURE);
					GuideForGlFrame.setmGuideType(GuideForGlFrame.GUIDE_TYPE_DOCK_BAR_ICON_GESTURE);
					GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
							IFrameworkMsgId.SHOW_FRAME, IDiyFrameIds.GUIDE_GL_FRAME, null, null);
					StaticTutorial.sCheckDockBarIcon = false;
					mSharedPreferences.putBoolean(
							IPreferencesIds.SHOULD_SHOW_DOCK_BAR_ICON_GESTURE, false);
					mSharedPreferences.commit();
				}
			});
		} else {
			StaticTutorial.sCheckDockBarIcon = false;
		}
	}
	public void showSuperWidgetGuide() {
		boolean shouldshowguide = mSharedPreferences.getBoolean(
				IPreferencesIds.SHOULD_SHOW_FULL_SCREEN_WIDGET, true);
		if (shouldshowguide) {
			if (mSuperWidget != null && mSuperWidget.isShown()
					&& isCloudViewShowing(CLOUD_ID_SUPER_WIDGET)) {
				return;
			}
			if (isCloudViewShowing(CLOUD_ID_DOCK_GESTURE)
					|| isCloudViewShowing(CLOUD_ID_CUSTOM_GESTURE)) {
				removeFromCoverFrame(CLOUD_ID_SUPER_WIDGET);
				removeFromCoverFrame(CLOUD_ID_SUPER_WIDGET);
			}
			addToCoverFrame(CLOUD_ID_SUPER_WIDGET);
			mSuperWidget.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					removeFromCoverFrame(CLOUD_ID_SUPER_WIDGET);
					GuideForGlFrame.setmGuideType(GuideForGlFrame.GUIDE_TYPE_FULL_SCREEN_WIDGET);
					GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
							IFrameworkMsgId.SHOW_FRAME, IDiyFrameIds.GUIDE_GL_FRAME, null, null);
					StaticTutorial.sCheckSuperWidget = false;
					mSharedPreferences.putBoolean(IPreferencesIds.SHOULD_SHOW_FULL_SCREEN_WIDGET,
							false);
					mSharedPreferences.commit();

				}
			});
		} else {
			StaticTutorial.sCheckSuperWidget = false;
		}

	}
	public void showCustomGesture() {

		boolean shouldshowguide = mSharedPreferences.getBoolean(
				IPreferencesIds.SHOULD_SHOW_CUSTOM_GESTURE, true);
		if (shouldshowguide && checkNeedShowCustomGestureGuide()) {
			if (mCustomGesture != null && mCustomGesture.isShown()
					&& isCloudViewShowing(CLOUD_ID_CUSTOM_GESTURE)) {
				return;
			}
			addToCoverFrame(CLOUD_ID_CUSTOM_GESTURE);
			mCustomGesture.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					removeFromCoverFrame(CLOUD_ID_CUSTOM_GESTURE);
					GuideForGlFrame.setmGuideType(GuideForGlFrame.GUIDE_TYPE_CUSTOM_GESTURE);
					GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
							IFrameworkMsgId.SHOW_FRAME, IDiyFrameIds.GUIDE_GL_FRAME, null, null);
					StaticTutorial.sCheckCustomGesture = false;
					mSharedPreferences
							.putBoolean(IPreferencesIds.SHOULD_SHOW_CUSTOM_GESTURE, false);
					mSharedPreferences.commit();
				}
			});
		} else {
			StaticTutorial.sCheckCustomGesture = false;
		}
	}
	public void showPriviewGuide(final int currentScreen) {

		boolean needStartutorial = mSharedPreferences.getBoolean(
				IPreferencesIds.SHOULD_SHOW_PRIVIEW_GUIDE, true);
		if (needStartutorial) {
			if (SensePreviewFrame.sIsEnterFromQA) {
				showTutorial(currentScreen);
				SensePreviewFrame.sIsEnterFromQA = false;
				return;
			}
			if (mScreenPriview != null && mScreenPriview.isShown()
					&& isCloudViewShowing(CLOUD_ID_SCREEN_PRIVIEW)) {
				return;
			}
			addToCoverFrame(CLOUD_ID_SCREEN_PRIVIEW);
			mScreenPriview.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					removeFromCoverFrame(CLOUD_ID_SCREEN_PRIVIEW);
					showTutorial(currentScreen);
				}
			});
		} else {
			StaticTutorial.sCheckShowScreenEdit = false;
		}
	}

	private void showTutorial(int currentScreen) {
		StaticTutorial.sCheckShowScreenEdit = false;
		// 得到当前屏幕状态
		if (!StatusBarHandler.isHide()) {
			// SensePreviewFrame.previewOperate = true;
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
					IDiyMsgIds.SHOW_HIDE_STATUSBAR, -2, true, null);
			SenseWorkspace.showStatusBar = true;
		}

		GuideForGlFrame.setmGuideType(GuideForGlFrame.GUIDE_TYPE_SCREEN_EDIT);
		if (GoLauncher.getTopFrame().getId() != IDiyFrameIds.SCREEN_PREVIEW_FRAME) {
			return;
		}
		GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IFrameworkMsgId.SHOW_FRAME,
				IDiyFrameIds.GUIDE_GL_FRAME, null, null);
		GoLauncher.sendMessage(this, IDiyFrameIds.GUIDE_GL_FRAME,
				IDiyMsgIds.PREVIEW_CURRENT_SCREEN_INDEX, currentScreen, null, null);
		mSharedPreferences.putBoolean(IPreferencesIds.SHOULD_SHOW_PRIVIEW_GUIDE, false);
		mSharedPreferences.commit();
	}
	/**
	 * 功能描述：检测是否需要弹出自定义手势提示
	 * 过滤条件：若为新安装，返回false
	 * 如果已使用过自定义手势，存在自定义手势的记录，返回false
	 * 分别检测HOME键、上滑、下滑手势，若存在一个设为自定义手势，则返回false		
	 * @return boolean值    若为true，则需要弹出自定义手势提示，false则不需要
	 */
	private static boolean checkNeedShowCustomGestureGuide() {
		try {
			// 判断是否使用过自定义手势
			DataProvider dataProvider = DataProvider.getInstance(GOLauncherApp.getContext());
			Cursor cursor = dataProvider.queryDiyGestures();
			if (cursor != null) {
				try {
					if (cursor.getCount() != 0) {
						return false;
					}
				} catch (Exception e) {
				} finally {
					cursor.close();
				}
			}
			GoSettingControler controler = GOLauncherApp.getSettingControler();
			GestureSettingInfo info = null;
			// 检测HOME键手势
			info = controler.getGestureSettingInfo(GestureSettingInfo.GESTURE_HOME_ID);
			if (info.mGestureAction == GlobalSetConfig.GESTURE_GOSHORTCUT
					&& info.mGoShortCut == GlobalSetConfig.GESTURE_SHOW_DIYGESTURE) {
				return false;
			}
			// 检测上滑手势
			info = controler.getGestureSettingInfo(GestureSettingInfo.GESTURE_UP_ID);
			if (info.mGestureAction == GlobalSetConfig.GESTURE_GOSHORTCUT
					&& info.mGoShortCut == GlobalSetConfig.GESTURE_SHOW_DIYGESTURE) {
				return false;
			}
			// 检测下滑手势
			info = controler.getGestureSettingInfo(GestureSettingInfo.GESTURE_DOWN_ID);
			if (info.mGestureAction == GlobalSetConfig.GESTURE_GOSHORTCUT
					&& info.mGoShortCut == GlobalSetConfig.GESTURE_SHOW_DIYGESTURE) {
				return false;
			}
			// 检测双击手势
			info = controler.getGestureSettingInfo(GestureSettingInfo.GESTURE_DOUBLLE_CLICK_ID);
			if (info.mGestureAction != GlobalSetConfig.GESTURE_GOSHORTCUT
					|| info.mGoShortCut != GlobalSetConfig.GESTURE_SHOW_DIYGESTURE) {
				return false;
			}
		} catch (Throwable e) {
			return false;
		}
		// 判断是否是新安装
		GoLauncher launcher = GoLauncher.getContext();
		if (null != launcher && launcher.getFirstRun()) {
			return false;
		}
		return true;
	}

	public boolean isCloudViewShowing(int cloudVIewID) {
		if (mCloudViewIDMap == null) {
			return false;
		}
		GuideCloudView cloudView = null;
		switch (cloudVIewID) {
			case CLOUD_ID_DOCK_GESTURE :
				cloudView = mCloudViewIDMap.get(CLOUD_TYPE_SCREEN);
				if (cloudView == null) {
					return false;
				} else {
					return cloudView.getId() == CLOUD_ID_DOCK_GESTURE;
				}
			case CLOUD_ID_CUSTOM_GESTURE :
				cloudView = mCloudViewIDMap.get(CLOUD_TYPE_SCREEN);
				if (cloudView == null) {
					return false;
				} else {
					return cloudView.getId() == CLOUD_ID_CUSTOM_GESTURE;
				}
			case CLOUD_ID_SCREEN_PRIVIEW :
				cloudView = mCloudViewIDMap.get(CLOUD_TYPE_SCREEN_PRIVIEW);
				if (cloudView == null) {
					return false;
				} else {
					return cloudView.getId() == CLOUD_ID_SCREEN_PRIVIEW;
				}
			case CLOUD_ID_SUPER_WIDGET :
				cloudView = mCloudViewIDMap.get(CLOUD_TYPE_SCREEN);
				if (cloudView == null) {
					return false;
				} else {
					return cloudView.getId() == CLOUD_ID_SUPER_WIDGET;
				}
			default :
				return false;
		}
	}

	@Override
	public int getId() {
		// TODO Auto-generated method stub
		return -1;
	}

	@Override
	public boolean handleMessage(Object who, int type, int msgId, int param, Object object,
			List objects) {
		switch (msgId) {

			case IDiyMsgIds.SYSTEM_ON_RESUME :
				
			case IDiyMsgIds.SYSTEM_CONFIGURATION_CHANGED :
				if (isCloudViewShowing(CLOUD_ID_DOCK_GESTURE)
						|| isCloudViewShowing(CLOUD_ID_CUSTOM_GESTURE) || isCloudViewShowing(CLOUD_ID_SUPER_WIDGET)) {
					hideCloudView();
				}
				mHandler.postDelayed(new Runnable() {

					@Override
					public void run() {
						//检测是否应该继续存在。
						if (isCloudViewShowing(CLOUD_ID_DOCK_GESTURE)) {
							showDockGesture();
						} else if (isCloudViewShowing(CLOUD_ID_SUPER_WIDGET)) {
							showSuperWidgetGuide();
						} else if (isCloudViewShowing(CLOUD_ID_CUSTOM_GESTURE)) {
							showCustomGesture();
						}
						if (StaticTutorial.sCheckSuperWidget) {
							showSuperWidgetGuide();
						}
					}
				}, 600);
				return true;
		}
		return false;
	}

}
