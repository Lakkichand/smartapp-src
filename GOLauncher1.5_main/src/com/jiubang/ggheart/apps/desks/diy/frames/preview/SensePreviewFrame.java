package com.jiubang.ggheart.apps.desks.diy.frames.preview;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.LinearLayout;

import com.gau.go.launcherex.R;
import com.go.util.animation.MyAnimationUtils;
import com.go.util.graphics.DrawUtils;
import com.go.util.window.OrientationControl;
import com.jiubang.core.framework.AbstractFrame;
import com.jiubang.core.framework.IFrameManager;
import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingMainActivity;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.desks.diy.StatusBarHandler;
import com.jiubang.ggheart.apps.desks.diy.frames.preview.ScreenPreviewMsgBean.PreviewImg;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.IndicatorListner;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenIndicator;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.Workspace;
import com.jiubang.ggheart.apps.desks.diy.frames.tipsforgl.GuideControler;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.data.info.DesktopSettingInfo;
import com.jiubang.ggheart.data.info.EffectSettingInfo;
import com.jiubang.ggheart.data.info.ItemInfo;
import com.jiubang.ggheart.data.statistics.StaticTutorial;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * Sense风格预览层
 * 
 * @author yuankai
 * @version 1.0
 */
public class SensePreviewFrame extends AbstractFrame
		implements
			SenseWorkspace.ISenseWorkspaceListener,
			IndicatorListner,
			AnimationListener {
	public final static String FIELD_ABS_INDEX = "absolute_index";
	public final static String FIELD_DEST_SCREEN = "dest_screen";
	public final static String FIELD_SRC_SCREEN = "src_screen";
	public final static String FIELD_CUR_SCREEN_START_INDEX = "start_index";
	public final static String FIELD_SCREEN_COUNT = "screen_count";
	public final static String FIELD_SCROLL_DURATION = "scroll_duration";

	public final static int SCREEN_LOADED = 0x0; // 已经加载完毕
	public final static int SCREEN_LOADING = 0x1; // 正在加载
	public final static int FROM_SETTING = 0x2; // 从设置返回

	private final static int ASYNC_LOAD_VIEW = 1;
	private final static int TIP_TIMES = 5;

	private SenseLayout mLayout; //最上层layout
	private SenseWorkspace mWorkspace; //卡片管理容器
	private ScreenIndicator mIndicator; //指示器
	private ScreenPreviewMsgBean mPreviewBean; //预览层数据包
	private boolean mHasAddPreviewBean = false;
	private List<Integer> mAsyncLoadListeners = new ArrayList<Integer>();
	private List<Integer> mReplaceFinishListeners = new ArrayList<Integer>();
	private Object mMutex;
	private View mDragView = null;
	private int mCurrentScreen = -1;
	private boolean mStateAdd = false;

	public static boolean sScreenFrameStatus = false; // 是否隐藏状态栏
	public static boolean sPreLongscreenFrameStatus = false; // 是否隐藏状态栏(数据库中设置的状态)
	public static boolean sPreviewLongClick = false; // 是否长按键

	/**
	 * 此处为一个特殊处理 由于需求要求，在设置中点击屏幕设置，则会显示本界面
	 * 再点击返回键需要回到屏幕设置所在的设置界面，故由此标志位标识是否需要返回到设置界面
	 */
	private static boolean sNeedGotoSetting = false;
	private static boolean sBackFromSetting = false;

	private static boolean sIsEnterFromDragView = false; //  拖拽图标（或widget进入屏幕预览的标识）
	
	public static boolean sIsEnterFromQA = false;
	public static int sScreenW;
	public static int sScreenH;

	private PreviewController mPreviewController; // 预览控制

	private boolean mRefresh = false;

	private boolean mToMainScreen = false;

	private LinearLayout mTextLayout; // 卡片图标已满提示

	private boolean mTextLayoutvisiable = false;

	private boolean mDragapptextLayoutvisiable = false;

	private boolean mDragFinish = false;

	public static int sCurScreenId; // 记录当前屏的索引

	public static boolean sIsHOME = false; // add by jiang 设置为默认桌面后  按home键跳屏幕预览

	private int mOrientation;
	private int mOritationTpye; // 记录进入分享时的横竖屏类型
	/**
	 * sense风格预览
	 * 
	 * @param activity
	 * @param frameManager
	 * @param id
	 */
	public SensePreviewFrame(Activity activity, IFrameManager frameManager, int id) {
		super(activity, frameManager, id);
		getScreenWH();
		mPreviewController = new PreviewController(activity);

		LayoutInflater inflater = (LayoutInflater) mActivity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mLayout = (SenseLayout) inflater.inflate(R.layout.sense_screen, null);
		mWorkspace = mLayout.getWorkspace();
		mWorkspace.setListener(this);
		mWorkspace.setPreviewController(mPreviewController);
		mIndicator = mLayout.getIndicator();
		mIndicator.setListner(this);
		mMutex = new Object();
		mRefresh = false;
		mTextLayout = (LinearLayout) mLayout.findViewById(R.id.textlayout);

	}

	/***
	 * 设置当前状态不可旋转
	 */
	private void setOritation() {
		mOritationTpye = GOLauncherApp.getSettingControler().getGravitySettingInfo().mOrientationType;
		if (mOritationTpye == OrientationControl.AUTOROTATION) {
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
					IDiyMsgIds.SCREEN_SET_ORIENTATION, mOrientation, null, null);
		}
	}

	/***
	 * 恢复屏幕状态
	 */
	private void resetOritation() {
		if (mOritationTpye == OrientationControl.AUTOROTATION) {
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
					IDiyMsgIds.SCREEN_SET_ORIENTATION, OrientationControl.AUTOROTATION, null, null);
		}
	}

	@Override
	public void onResume() {
		if (getVisibility() == View.VISIBLE) {
			//针对关掉屏幕，翻转屏后再打开的情况
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
					IDiyMsgIds.SCREEN_SET_ORIENTATION, mOrientation, null, null);
		}
		super.onResume();
	}

	@Override
	public void onForeground() {
		super.onForeground();
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
				IDiyMsgIds.SCREEN_CLOSE_ALL_FOLDERS, -1, null, null);
	}

	@Override
	public void onVisiable(int visibility) {
		super.onVisiable(visibility);
		if (visibility == View.VISIBLE) {
			mOrientation = OrientationControl.getRequestOrientation(mActivity);
			setOritation();
			mFrameManager.registKey(this);
			if (StaticTutorial.sCheckDockBarIcon || StaticTutorial.sCheckCustomGesture) {
				GuideControler cloudView = GuideControler.getInstance(mActivity);
				if (cloudView != null) {
					cloudView.hideCloudView();
				}
			}
		} else {
			resetOritation();
			AbstractFrame topFram = mFrameManager.getTopFrame();
			if (mToMainScreen || topFram.getId() != IDiyFrameIds.GUIDE_GL_FRAME) {
				mFrameManager.unRegistKey(this);
				synchronized (mMutex) {
					setBackFromSetting(false);
					// 退出再发一次消息显示screenFrame与dockFrame,避免出现screenFrame与dockFrame都消失的bug
					// bug原因：进入动画是异步加载到当前屏卡片才通过Handler来remove
					// screenFrame与dockFrame的，
					// 可能会出现其他事件先移除了sensepreivewFrame,通知显示screenFrame与dockFrame，然后才收到
					// 刚才动画发出的remove screenFrame与dockFrame消息。
					// 使用handler来处理的原因：与上述的handler处理统一，保证同一线程中顺序的执行
					if (!sNeedGotoSetting) {
						mWorkspace.post(new Runnable() {
							@Override
							public void run() {
								notifyDesktop(true);
							}
						});
					}
					recycle();
					// 预览返回时看情况决定是否回收
					OutOfMemoryHandler.gcIfAllocateOutOfHeapSize();
				}
			}
		}
	}

	@Override
	public boolean handleMessage(Object who, int type, int msgId, int param, Object object,
			List objects) {
		super.handleMessage(who, type, msgId, param, object, objects);
		boolean ret = false;
		switch (msgId) {
			case IDiyMsgIds.SCREEN_FINISH_LOADING : {
				mWorkspace.setEnableUpdate(true);
				break;
			}

			case IDiyMsgIds.PREVIEW_INIT :
				setBackFromSetting(false);
				if (object != null && object instanceof ScreenPreviewMsgBean) {
					getScreenWH();
					if (mWorkspace != null) {
						mWorkspace.getDrawingResource();
					}

					if (mHasAddPreviewBean) {
						//翻转屏
						replaceCardContent((ScreenPreviewMsgBean) object);
						mRefresh = true;
					} else {
						//第一次进
						init(param, (ScreenPreviewMsgBean) object);
					}
					if (sIsHOME) {
						//隐藏加号屏 jiang设置为默认桌面后  按home键跳屏幕预览
						mWorkspace.hideAddCard();
					} else {
						// 进入过了屏幕预览就不响应home键处理
						PreferencesManager sharedPreferences = new PreferencesManager(mActivity,
								IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
						sharedPreferences.putBoolean(IPreferencesIds.SHOULD_SHOW_PREVIEW_HOME,
								false);
						sharedPreferences.commit();
						SensePreviewFrame.sIsHOME = false;
					}
					ret = true;
				}
				break;

			case IFrameworkMsgId.SYSTEM_CONFIGURATION_CHANGED : {
				if (mDragapptextLayoutvisiable) {
					mTextLayout.setVisibility(View.INVISIBLE);
					mDragapptextLayoutvisiable = false;
				}

				mWorkspace.setEnableUpdate(false);
				if (sIsEnterFromDragView) {
					sIsEnterFromDragView = false;
					if (!SensePreviewFrame.sScreenFrameStatus) {
						SensePreviewFrame.setIsEnterFromDragView(false);
						// 要求显示全屏并重新排版
						GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
								IDiyMsgIds.SHOW_HIDE_STATUSBAR, -2, false, null);
						// 通知workspace格局发生变化
						GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
								IDiyMsgIds.SCREEN_REQUEST_LAYOUT,
								Workspace.CHANGE_SOURCE_INDICATOR, 0, null);

					}
					if (CardLayout.sDrawRoom) {
						CardLayout.sDrawRoom = false;
					}
				} else {
					if (getVisibility() == View.VISIBLE) {
						new Handler().post(new Runnable() {
							@Override
							public void run() {
								// 异步要求屏幕层再进入预览层，是希望其他层都拿到横竖屏消息之后。
								GoLauncher.postMessage(this, IDiyFrameIds.SCREEN_FRAME,
										IDiyMsgIds.SCREEN_SHOW_PREVIEW, sNeedGotoSetting ? 1 : 0,
										null, null);
								// mSavedCurScreen =
								// mWorkspace.getCurScreenIndex();
							}
						});
					}
				}

				ret = true;
			}
				break;

			case IDiyMsgIds.PREVIEW_CAN_SNAP_NEXT : {
				ret = mWorkspace.cansnapToNextScreen();
			}
				break;

			case IDiyMsgIds.PREVIEW_CAN_SNAP_PRE : {
				ret = mWorkspace.cansnapToPreScreen();
			}
				break;

			case IDiyMsgIds.PREVIEW_SNAP_NEXT : {
				if (objects != null) {
					mWorkspace.snapToNextScreen();
					objects.clear();
					objects.addAll(mWorkspace.getCurScreenRects());
					if (object != null && object instanceof Bundle) {
						((Bundle) object).putInt(FIELD_CUR_SCREEN_START_INDEX,
								mWorkspace.getCurScreenStartIndex());
					}
					ret = true;
				}
			}
				break;

			case IDiyMsgIds.PREVIEW_SNAP_PRE : {
				if (objects != null) {
					mWorkspace.snapToPreScreen();
					objects.clear();
					objects.addAll(mWorkspace.getCurScreenRects());
					if (object != null && object instanceof Bundle) {
						((Bundle) object).putInt(FIELD_CUR_SCREEN_START_INDEX,
								mWorkspace.getCurScreenStartIndex());
					}
					ret = true;
				}
			}
				break;

			case IDiyMsgIds.PREVIEW_GET_CUR_CARDS_RECT : {
				if (objects != null) {
					objects.clear();
					List<Rect> rects = mWorkspace.getCurScreenRects();
					objects.addAll(rects);
					ret = true;
				}
			}
				break;

			case IDiyMsgIds.PREVIEW_HIDE_ADD_CARD : {
				// 此标记位设为ＴＲＵＥ，当卡片全加载完时会隐藏+号卡片
				sIsEnterFromDragView = true;
				ret = true;
			}
				break;

			case IDiyMsgIds.PREVIEW_ENLARGE_CARD : {
				if (object != null && (object instanceof Rect)) {
					Rect enlargeRect = mWorkspace.enlargeCard(param);
					if (enlargeRect != null) {
						((Rect) object).set(enlargeRect);
						ret = true;
					}
					enlargeRect = null;
				}
			}
				break;

			case IDiyMsgIds.PREVIEW_RESUME_CARD : {
				if (param > -1) {
					mWorkspace.resumeCard(param);
					ret = true;
				}
			}
				break;

			case IDiyMsgIds.PREVIEW_REGISTER_LOAD_LISTENER : {
				final Integer integer = new Integer(param);
				if (!isListenerExists(integer)) {
					ret = mAsyncLoadListeners.add(integer);
				}
			}
				break;

			case IDiyMsgIds.PREVIEW_UNREGISTER_LOAD_LISTENER : {
				final Integer integer = new Integer(param);
				ret = mAsyncLoadListeners.remove(integer);
			}
				break;

			case IDiyMsgIds.PREVIEW_REGISTER_REPLACE_LISTENER : {
				final Integer integer = new Integer(param);
				if (!isReplaceListenerExists(integer)) {
					ret = mReplaceFinishListeners.add(integer);
				}
			}
				break;

			case IDiyMsgIds.PREVIEW_UNREGISTER_REPLACE_LISTENER : {
				final Integer integer = new Integer(param);
				ret = mReplaceFinishListeners.remove(integer);
			}
				break;

			case IDiyMsgIds.PREVIEW_GET_ABS_SCREEN_INDEX : {
				if (object != null && object instanceof Bundle) {
					Bundle bundle = (Bundle) object;
					bundle.putInt(FIELD_ABS_INDEX,
							mWorkspace.getAbsScreenIndex(bundle.getInt(FIELD_ABS_INDEX)));
					ret = true;
				}
			}
				break;

			case IDiyMsgIds.PREVIEW_LEAVE_ANIMATE : {
				if (param >= 0 && param < mWorkspace.getChildCount()) {
					preview(param);
					ret = true;
				}
			}
				break;
			case IDiyMsgIds.PREVIEW_TO_MAIN_SCREEN_ANIMATE : {
				int screenIndex = 0;
				if (param > 0) {
					screenIndex = param;
				}
				// 要求预览层作离开动画
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
						IDiyMsgIds.PREVIEW_LEAVE_ANIMATE, screenIndex, null, null);
				// 将选取信息发送给屏幕层并进入屏幕层
				GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						IFrameworkMsgId.REMOVE_FRAME, IDiyFrameIds.DRAG_FRAME, null, null);
				if (CardLayout.sDrawRoom) {
					CardLayout.sDrawRoom = false;
				}
			}
				break;
			case IDiyMsgIds.PREVIEW_BACK_HADLE :
				backHandle();
				ret = true;
				break;
			case IDiyMsgIds.PREVIEW_REPLACE_CARD : {
				if (object != null && object instanceof Bundle && objects != null) {
					final Bundle bundle = (Bundle) object;
					final int srcScreenIndex = bundle.getInt(FIELD_SRC_SCREEN);
					final int destScreenIndex = bundle.getInt(FIELD_DEST_SCREEN);
					@SuppressWarnings("unchecked")
					List<Rect> rects = objects;
					// TODO 通知屏幕层保存设置
					boolean isSaveSuccess = GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
							IDiyMsgIds.PREVIEW_REPLACE_CARD, -1, bundle, null);
					if (isSaveSuccess) {
						mWorkspace.replaceCard(srcScreenIndex, destScreenIndex, rects);
						mRefresh = true;
					}
					ret = true;
				}
			}
				break;

			case IDiyMsgIds.PREVIEW_REPLACE_CARD_BACK : {
				if (object instanceof Rect) {
					mWorkspace.replaceBack(param, (Rect) object);
				}
			}
				break;

			case IDiyMsgIds.REPLACE_DRAG_OVER : {
				final int cardIndex = param;
				mWorkspace.post(new Runnable() {
					@Override
					public void run() {
						mWorkspace.handleReplaceFinish();
						mWorkspace.endReplace();
						// 放到Replace Back动画归位后在加上
						// mWorkspace.showAddCard();
						mWorkspace.showCard(cardIndex);
						mWorkspace.unlock();
					}
				});
				ret = true;
				//用户行为统计
				StatisticsData.countUserActionData(
						StatisticsData.DESK_ACTION_ID_SCREEN_PREVIEW_EDIT,
						StatisticsData.USER_ACTION_THREE, IPreferencesIds.DESK_ACTION_DATA);
			}
				break;
			case IDiyMsgIds.REPLACE_DRAG_OVER_SYNC : { // 同步结束拖动操作
				final int cardIndex = param;
				mWorkspace.handleReplaceFinish();
				mWorkspace.endReplace();
				// 放到Replace Back动画归位后在加上
				// mWorkspace.showAddCard();
				mWorkspace.showCard(cardIndex);
				mWorkspace.unlock();

				ret = true;
			}
				break;

			case IDiyMsgIds.REPLACE_DRAG_CANCEL : {
				mWorkspace.endReplace();
				mWorkspace.showAddCard();
				mWorkspace.showCard(param);
				mWorkspace.unlock();
				ret = true;
			}
				break;

			case IDiyMsgIds.PREVIEW_GET_START_SCREEN_WIDTH : {
				if (object != null && object instanceof Bundle) {
					((Bundle) object).putInt(FIELD_SCREEN_COUNT, mWorkspace.getStartScreenWidth());
					ret = true;
				}
			}
				break;

			case IDiyMsgIds.BACK_TO_MAIN_SCREEN : {
				mToMainScreen = true;
				leave(true);
				ret = true;
			}
				break;

			case IDiyMsgIds.PREVIEW_MAKE_TIP : {
				makeTip();
				ret = true;
			}
				break;

			case IDiyMsgIds.PREVIEW_SCROLL_DURATION : {
				if (null != object && object instanceof Bundle) {
					((Bundle) object).putInt(FIELD_SCROLL_DURATION, mWorkspace.getScrollDuration());
					ret = true;
				}
			}
				break;

			case IDiyMsgIds.DESK_THEME_CHANGED : {
				// 主题更换
				mIndicator.applyTheme();
				break;
			}

			case IDiyMsgIds.INDICATOR_CHANGE_SHOWMODE : {
				if (null != mIndicator) {
					mIndicator.doWithShowModeChanged();
				}
				ret = true;
				break;
			}

			case IDiyMsgIds.INDICATOR_SLIDE_PERCENT : {
				mWorkspace.getScreenScroller().setScrollPercent(param);
				break;
			}

			case IDiyMsgIds.PREVIEW_ADD_GOWIDGET : {
				mWorkspace.setmGoWidgetBundle((Bundle) object);
				mWorkspace.setEnoughSpaceList((ArrayList<Integer>) objects);

				ret = true;
				break;
			}
			case IDiyMsgIds.PREVIEW_DELETE_SCREEN : {
				final int cardIndex = param;
				final CardLayout cardLayout = (CardLayout) object;
				mWorkspace.post(new Runnable() {
					@Override
					public void run() {
						mWorkspace.completeRemoveCard(cardLayout);
						mWorkspace.handleReplaceFinish();
						mWorkspace.endReplace();
						// 发信息给屏幕交换层，通知动画结束
						GoLauncher.sendMessage(this, IDiyFrameIds.REPLACE_DRAG_FRAME,
								IDiyMsgIds.REPLACE_DRAG_FINISH, -1, null, null);
						// 放到Replace Back动画归位后在加上
						mWorkspace.showAddCard();
						mWorkspace.setCaptionY();
						mWorkspace.showCard(cardIndex);
						mWorkspace.unlock();
					}
				});
				ret = true;
				break;
			}

			case IDiyMsgIds.CARDLAYOUTTURN_TO_RED : {
				if (!mDragapptextLayoutvisiable) {
					mDragapptextLayoutvisiable = true;
				}
				Animation animation = MyAnimationUtils.getPopupAnimation(
						MyAnimationUtils.POP_FROM_LONG_START_SHOW_2, -1);
				if (animation != null && mTextLayout != null) {
					mTextLayout.startAnimation(animation);
					mTextLayout.setVisibility(View.VISIBLE);
				}
				mIndicator.setVisibility(View.INVISIBLE);
				int index = param + mWorkspace.getCurScreenStartIndex();
				CardLayout cardLayout = (CardLayout) mWorkspace.getChildAt(index);
				cardLayout.setLightNoRoom();
				cardLayout.postInvalidate();
				break;
			}

			case IDiyMsgIds.CARDLAYOUTTURN_TO_NORMAL : {
				if (mDragapptextLayoutvisiable) {
					mDragapptextLayoutvisiable = false;
				}

				mDragFinish = (Boolean) object;
				int index = param + mWorkspace.getCurScreenStartIndex();
				Animation animation = MyAnimationUtils.getPopupAnimation(
						MyAnimationUtils.POP_TO_LONG_START_HIDE_2, -1);
				if (animation != null && mTextLayout != null) {
					animation.setAnimationListener(this);
					// animation.setDuration(100);
					mTextLayout.startAnimation(animation);
					mTextLayout.setVisibility(View.INVISIBLE);
				}

				mIndicator.setVisibility(View.VISIBLE);
				CardLayout cardLayout = (CardLayout) mWorkspace.getChildAt(index);
				cardLayout.setNoRoom();
				cardLayout.resume();
				ret = true;
				break;
			}
			case IDiyMsgIds.SENDVIEWTOPREVIEW : {
				mDragView = (View) object;
				break;
			}

			case IDiyMsgIds.IS_ADD_CARD : {
				// 判断是否为加号
				ret = mWorkspace.isAddCard(param);
				break;
			}

			case IDiyMsgIds.TURNADD_TO_CARD : {
				mWorkspace.setNormal(param);
				// 变成一个屏幕的样子
				// 把这个cardLayout放大
				if (object != null && (object instanceof Rect)) {
					Rect enlargeRect = mWorkspace.enlargeCard(param);
					if (enlargeRect != null) {
						((Rect) object).set(enlargeRect);
						ret = true;
					}
					enlargeRect = null;
				}
				break;
			}
			case IDiyMsgIds.TURNCARD_TO_ADD : {
				mWorkspace.setAdd(param);
				// 变成一个+号的样子
				// 把这个cardLayout放大
				if (param > -1) {
					mWorkspace.resumeCard(param);
					ret = true;
				}
				break;
			}
			case IDiyMsgIds.ADD_NEW_CARD : {
				mWorkspace.completeAddEmptyCard();

				mCurrentScreen = param;
				mStateAdd = (Boolean) object;
				break;
			}

			case IDiyMsgIds.IS_SET_CONTENT : {
				int index = param + mWorkspace.getCurScreenStartIndex();
				ret = GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
						IDiyMsgIds.IS_SET_CONTENT, index, object, null);
				break;
			}
			case IDiyMsgIds.PREVIEW_NOLESS_SCREEN : {
				mWorkspace.endReplace();
				mWorkspace.showCard(param);
				mWorkspace.unlock();
				mWorkspace.replaceBack(param, (Rect) object);
				mWorkspace.showAddCard();
				mWorkspace.showToast(R.string.no_less_screen);
			}
				break;
			case IDiyMsgIds.REFRESH_SCREENPREW : {
				mWorkspace.postInvalidate();
			}
				break;
			case IDiyMsgIds.GET_ENOUGHSPACELIST : {
				mWorkspace.setEnoughSpaceList((ArrayList<Integer>) objects);
			}
				break;

			case IDiyMsgIds.ADD_WIDGET_NOROOM : {
				if (!mTextLayoutvisiable) {
					mTextLayoutvisiable = true;
					// 全屏显示
					GoSettingControler settingControler = GOLauncherApp.getSettingControler();
					DesktopSettingInfo info = settingControler.getDesktopSettingInfo();
					sPreLongscreenFrameStatus = !info.mShowStatusbar;

					// 得到当前屏幕状态
					if (!sPreLongscreenFrameStatus) {
						// //隐藏指示器
						GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
								IDiyMsgIds.SHOW_HIDE_STATUSBAR, -2, true, null);
						SenseWorkspace.showStatusBar = true;
					}

					Animation animation = MyAnimationUtils.getPopupAnimation(
							MyAnimationUtils.POP_FROM_LONG_START_SHOW_2, -1);
					if (animation != null && mTextLayout != null) {
						// animation.setDuration(20);
						mTextLayout.startAnimation(animation);
						mTextLayout.setVisibility(View.VISIBLE);
					}
					mIndicator.setVisibility(View.INVISIBLE);
				}
				break;
			}

			case IDiyMsgIds.ADD_WIDGET_NOROOM_TONOMAL : {
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
				ret = true;
				break;
			}
			case IDiyMsgIds.DEPLAY_INDICATOR : {
				mLayout.getIndicator().setVisibility(View.VISIBLE);
				break;
			}
			case IDiyMsgIds.PREVIEW_REPLACE_FINISH :
				ret = !mWorkspace.isRelpaceing();
				break;
			case IDiyMsgIds.PREVIEW_NORMAL_STATE :
				ret = mWorkspace.isNormalState();
				break;
			case IDiyMsgIds.PREVIEW_DRAG_ING :
				if (mWorkspace.getIsHideAddCard()
						&& mWorkspace.getChildCount() != SenseWorkspace.MAX_CARD_NUMS) {
					ret = true;
				}
				break;
			case IDiyMsgIds.SET_ORIENTATION :
				mOrientation = OrientationControl.getRequestOrientation(mActivity);
				setOritation();
				break;
			default :
				break;
		}
		return ret;
	}

	private synchronized void init(final int param, ScreenPreviewMsgBean msgBean) {
		if (sIsEnterFromDragView) {
			// 设置全屏显示
			sScreenFrameStatus = StatusBarHandler.isHide();
			boolean trashGone = GoLauncher.sendMessage(this, IDiyFrameIds.DRAG_FRAME,
					IDiyMsgIds.IS_TRASH_GONE, -1, null, null);
			// 得到当前屏幕状态
			if (!sScreenFrameStatus && !trashGone) {
				GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						IDiyMsgIds.SHOW_HIDE_STATUSBAR, -2, true, null);
			}
		}
		EffectSettingInfo info = GOLauncherApp.getSettingControler().getEffectSettingInfo();
		mWorkspace.setScrollSetting(info);
		// 保护
		if (mHasAddPreviewBean) {
			return;
		}

		// 如果是从设置直接进来的，则点击返回键需要回到设置界面
		sNeedGotoSetting = (param & FROM_SETTING) == FROM_SETTING;
		// 正在加载的时候不能删除、移动屏幕
		final boolean enableUpdate = (param & SCREEN_LOADING) == SCREEN_LOADED;
		mWorkspace.setEnableUpdate(enableUpdate);

		mPreviewBean = msgBean;

		mWorkspace.removeAllViews();

		if (sNeedGotoSetting) {
			// 从设置界面进入预览，没有进入动画
			// TODO: 设置当前屏幕id
			mWorkspace.handleEnterFinished();
			notifyDesktop(false);
		} else {
			// 从非设置界面进入预览，有进入动画
			mWorkspace.setmStatus(SenseWorkspace.SENSE_WAIT_FOR_ENTERING);
			mWorkspace.setBackgroundColor(0);
		}
		// TODO 临时做法
		// 这个方法不可靠，Workspace还自己在加视图
		if (null != msgBean) {
			SenseWorkspace.setCardCount(msgBean.screenPreviewList.size() + 1);
		}
		// 异步加载形式，保证进入速度
		asyncLoadBean();
	}

	/***
	 * <br>功能简述: 替换卡片内容(屏幕翻转后执行的操作)
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param bean
	 */
	private void replaceCardContent(ScreenPreviewMsgBean bean) {
		if (null == bean) {
			return;
		}
		// 替换视图
		int sz = bean.screenPreviewList.size();
		int count = mWorkspace.getChildCount();
		int min = sz < count ? sz : count;

		for (int i = 0; i < min; i++) {
			PreviewImg img = bean.screenPreviewList.get(i);
			if (null == img) {
				continue;
			}
			CardLayout card = (CardLayout) mWorkspace.getChildAt(i);
			if (null == card) {
				continue;
			}
			card.setPreViewInfo(img.previewView, img.canDelete);
		}
	}

	private void enterScreenSetting() {
		mActivity.startActivity(new Intent(mActivity, DeskSettingMainActivity.class));
		setBackFromSetting(true);
	}

	synchronized void makeTip() {
		PreferencesManager manager = new PreferencesManager(mActivity,
				IPreferencesIds.DESK_SHAREPREFERENCES_FILE, Context.MODE_PRIVATE);
		int tipTime = manager.getInt(IPreferencesIds.PREVIEW_TIP_TIME, 0);
		final int remainTipTimes = TIP_TIMES - tipTime;
		if (remainTipTimes > 0) {
			tipTime++;
			manager.putInt(IPreferencesIds.PREVIEW_TIP_TIME, tipTime);
			manager.commit();
		}
	}
	private boolean isReplaceListenerExists(Integer integer) {
		final int count = mReplaceFinishListeners.size();
		for (int i = 0; i < count; i++) {
			if (mReplaceFinishListeners.get(i).intValue() == integer.intValue()) {
				return true;
			}
		}
		return false;
	}

	private boolean isListenerExists(Integer integer) {
		final int count = mAsyncLoadListeners.size();
		for (int i = 0; i < count; i++) {
			if (mAsyncLoadListeners.get(i).intValue() == integer.intValue()) {
				return true;
			}
		}
		return false;
	}

	/***
	 * <br>功能简述:异步加载数据
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void asyncLoadBean() {
		mHasAddPreviewBean = true;

		// 上锁
		synchronized (mMutex) {
			// TODO 此处对内存情况进行检验，保证在解析XML时不会出现内存不足的情况
			OutOfMemoryHandler.gcIfAllocateOutOfHeapSize();

			try {
				List<ScreenPreviewMsgBean.PreviewImg> previews = mPreviewBean.screenPreviewList;
				final int size = previews.size();

				sCurScreenId = mPreviewBean.currentScreenId;
				final int mainScreenId = mPreviewBean.mainScreenId;

				mWorkspace.setFirstLayout(false);

				int formCardIndex = -1;
				if (mDragView != null && mDragView.getTag() != null
						&& mDragView.getTag() instanceof ItemInfo) {
					ItemInfo itemInfo = (ItemInfo) mDragView.getTag();
					formCardIndex = itemInfo.mScreenIndex;
				}

				for (int i = 0; i < size; i++) {
					// TODO 此处采用异步方式进行添加View
					final PreviewImg previewImg = previews.get(i);
					if (previewImg == null) {
						continue;
					}

					CardLayout card = null;

					try {
						card = new CardLayout(mActivity, CardLayout.TYPE_PREVIEW,
								previewImg.previewView, previewImg.canDelete, mWorkspace);

						// 被捉起的view
						if (mDragView != null) {
							int index = i;
							boolean enough = GoLauncher.sendMessage(this,
									IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.IS_SET_CONTENT, index,
									mDragView, null);
							if (formCardIndex != i && !enough) {
								card.setNoRoom();
							}
						}
					} catch (OutOfMemoryError e) {
						OutOfMemoryHandler.handle();
					} catch (Exception e) {
					}

					// 作保护
					if (card == null) {
						continue;
					}

					// 设置主屏
					if (i == mainScreenId) {
						card.setHome(true);
					}

					// 设置当前屏
					if (sCurScreenId == i) {
						card.setCurrent(true);
					}

					// 完成了上述的setHome　setCurrent　的UI操作才可把此card添加到桌面的UI框架中，
					// 因为此线程非main线程，如果先加入到UI框架中，setHome setCurrent会提示错误：
					// android.view.ViewRoot$CalledFromWrongThreadException:
					// Only the original thread that created a view hierarchy
					// can touch its views.
					mHandler.sendMessage(mHandler.obtainMessage(ASYNC_LOAD_VIEW, i, size, card));
				}
				mDragView = null;
				if (!GOLauncherApp.getSettingControler().getScreenSettingInfo().mLockScreen) {
					// 锁屏时不显示+号卡片

					CardLayout addCard = new CardLayout(mActivity, CardLayout.TYPE_ADD, null,
							false, mWorkspace);
					mHandler.sendMessage(mHandler.obtainMessage(ASYNC_LOAD_VIEW, size, size,
							addCard));
				}
				if (SenseWorkspace.sCardCount > SenseWorkspace.MAX_CARD_NUMS) {
					mWorkspace.hideAddCard();
				}
				mWorkspace.setCaptionY();

			} catch (IndexOutOfBoundsException e) {
				return;
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
				OutOfMemoryHandler.handle();
			} catch (NullPointerException e) {
				// 异步加载，对象不保证一定存在，特别是横竖屏切换时，所以加保护
				e.printStackTrace();
			}
		}
	}

	/**
	 * 更新指示器
	 * 
	 * @param current
	 *            当前屏索引
	 * @param total
	 *            总数
	 */
	@Override
	public void updateIndicator(final int current, final int total) {
		mIndicator.setScreen(current, total);
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
	public void onBackground() {
		// TODO Auto-generated method stub
		super.onBackground();
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case ASYNC_LOAD_VIEW : {
					if (msg.obj != null && msg.obj instanceof CardLayout) {
						mWorkspace.addInScreen((CardLayout) (msg.obj));

						if (msg.arg1 == sCurScreenId) {
							if (!sNeedGotoSetting) {
								// 从非设置界面进入预览，才有进入动画
								// 加载完当前屏，进入动画
								notifyDesktop(false);
								mWorkspace.enterCard(sCurScreenId);
							}
						}

						if (msg.arg1 == msg.arg2) {
							// 当排版完成时要求调到当前屏所在屏
							mWorkspace.setFirstLayout(true);
							if (mPreviewBean != null
									&& mPreviewBean.screenPreviewList != null
									&& mPreviewBean.screenPreviewList.size() >= SenseWorkspace.MAX_CARD_NUMS) {
								mWorkspace.hideAddCard();
							}
							mWorkspace.setCaptionY();
						}
					}
				}
					break;

				default :
					break;
			}
		};
	};

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (sPreviewLongClick) {
			return false;
		}
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			backHandle();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
				|| keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			// 屏蔽
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/***
	 * 按返回键的返回操作
	 */
	public void backHandle() {
		//消除功能提示的view
		checkGuide();
		// 显示指示器
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.DISPLAY_INDICATOR, -1,
				true, null);
		CardLayout.sDrawRoom = false;
		if (sNeedGotoSetting) {
			enterScreenSetting();
		} else {
			// leave(true);
			if (GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.PREVIEW_SHOWING,
					0, null, null)) {
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, // 带动画退出
						IDiyMsgIds.PREVIEW_TO_MAIN_SCREEN, 0, false, null);
			}
		}
	}

	private void leave(boolean sync) {
		GoLauncher.sendMessage(this, IDiyFrameIds.REPLACE_DRAG_FRAME,
				IDiyMsgIds.REPLACE_DRAG_HOME_CLICK, 0, null, null);

		if (mRefresh) {
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
					IDiyMsgIds.SCREEN_REFRESH_INDEX, -1, null, null);
		}
		// 如果把元素放放在加号位置,设置加号产生的屏幕为当前屏
		if (mStateAdd) {
			mStateAdd = false;
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.SET_CURRENTSCREEN,
					mCurrentScreen, null, null);
		}
		// 跳转
		if (sync) {
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IFrameworkMsgId.REMOVE_FRAME,
					getId(), null, null);
		} else {
			GoLauncher.postMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IFrameworkMsgId.REMOVE_FRAME,
					getId(), null, null);
		}
		if (!SensePreviewFrame.sScreenFrameStatus && SensePreviewFrame.isEnterFromDragView()) {
			SensePreviewFrame.setIsEnterFromDragView(false);
			// 要求显示全屏并重新排版
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
					IDiyMsgIds.SHOW_HIDE_STATUSBAR, -2, false, null);
		}
	}

	private void recycle() {
		mWorkspace.recycle();

		mAsyncLoadListeners.clear();
		mReplaceFinishListeners.clear();

		// 回收
		if (mPreviewBean != null && mPreviewBean.screenPreviewList != null) {
			mPreviewBean.screenPreviewList.clear();
			mPreviewBean = null;
		}
		mHasAddPreviewBean = false;

		sNeedGotoSetting = false;

		sNeedGotoSetting = false;

		//add by jiang 设置为默认桌面后  按home键跳屏幕预览
		sIsHOME = false;
		// 不可见时显示指示器
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.DISPLAY_INDICATOR, -1,
				false, null);
	}

	@Override
	public View getContentView() {
		return mLayout;
	}

	@Override
	public boolean preAddCard() {
		return GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.SCREEN_ADD, -1,
				null, null);
	}

	@Override
	public void removeCard(int cardId) {
		mRefresh = true;
		GoLauncher.postMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.SCREEN_REMOVE, cardId,
				null, null);
	}

	@Override
	public void setCardHome(int cardId) {
		GoLauncher.postMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.SCREEN_SET_HOME, cardId,
				null, null);
	}

	@Override
	public void preview(int cardId) {
		// 显示指示器
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.DISPLAY_INDICATOR, -1,
				false, null);
		// 先显示指示器，再显示状态栏，顺序不能调换
		if (!SensePreviewFrame.sScreenFrameStatus && SensePreviewFrame.isEnterFromDragView()) {
			SensePreviewFrame.setIsEnterFromDragView(false);
		}
		if (!StatusBarHandler.isHide()) {
			// SensePreviewFrame.previewOperate = true;
			// 要求显示全屏并重新排版
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
					IDiyMsgIds.SHOW_HIDE_STATUSBAR, -2, false, null);
		}
		// 获取对应的视图
		List<View> list = new ArrayList<View>();
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.SCREEN_GET_CELLLAYOUT,
				cardId, null, list);
		CardLayout.sDrawRoom = false;
		if (list.size() > 0) {
			// // 作离开动画
			// // 离开时间依赖于设置
			// EffectSettingInfo effectSettingInfo =
			// AppCore.getInstance(mActivity).
			// getScreenControler().getScreenSettingControler().getEffectSettingInfo();
			// mWorkspace.leaveCard(cardId, (View)(list.get(0)),
			// effectSettingInfo.getDuration());
			mWorkspace.leaveCard(cardId, list.get(0));
			mIndicator.setVisibility(View.INVISIBLE);
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IFrameworkMsgId.SHOW_FRAME,
					IDiyFrameIds.DOCK_FRAME, null, null); // 让Dock栏立即显示
		} else {
			// 直接离开
			leave(true);
		}
		checkGuide();
		// 发送进入屏消息
		Integer duration = new Integer(mWorkspace.getLeaveDuration());
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.SCREEN_ENTER, cardId,
				duration, null);
	}

	private void checkGuide() {
		if (StaticTutorial.sCheckShowScreenEdit) {
			GuideControler cloudView = GuideControler.getInstance(mActivity);
			if (cloudView != null) {
				cloudView.removeFromCoverFrame(GuideControler.CLOUD_ID_SCREEN_PRIVIEW);
			}
		}

		if (StaticTutorial.sCheckDockBarIcon) {
			GuideControler cloudView = GuideControler.getInstance(mActivity);
			if (cloudView != null) {
				boolean isShow = cloudView.isCloudViewShowing(GuideControler.CLOUD_ID_DOCK_GESTURE);
				if (isShow) {
					cloudView.showDockGesture();
				}
			}
		}

		if (StaticTutorial.sCheckCustomGesture) {
			GuideControler cloudView = GuideControler.getInstance(mActivity);
			if (cloudView != null) {
				boolean isShow = cloudView
						.isCloudViewShowing(GuideControler.CLOUD_ID_CUSTOM_GESTURE);
				if (isShow) {
					cloudView.showCustomGesture();
				}
			}
		}
		
		if (StaticTutorial.sCheckSuperWidget) {
			GuideControler cloudView = GuideControler.getInstance(mActivity);
			if (cloudView != null) {
				boolean isShow = cloudView
						.isCloudViewShowing(GuideControler.CLOUD_ID_SUPER_WIDGET);
				if (isShow) {
					cloudView.showSuperWidgetGuide();
				}
			}
		}
	}

	@Override
	public void firstLayoutComplete() {
		// 通知监听者加载完成
		final int count = mAsyncLoadListeners.size();
		for (int i = 0; i < count; i++) {
			final int id = mAsyncLoadListeners.get(i).intValue();
			GoLauncher.sendMessage(this, id, IDiyMsgIds.PREVIEW_LOAD_COMPLETE, -1, null, null);
		}

		// if(mSavedCurScreen > -1)
		// {
		// mHandler.sendEmptyMessage(SNAP_TO_SAVED_SCREEN);
		// }
	}

	@Override
	public void leaveFinish() {
		leave(false);
	}

	@Override
	public void replaceFinish() {
		final int count = mReplaceFinishListeners.size();
		for (int i = 0; i < count; i++) {
			GoLauncher.sendMessage(this, mReplaceFinishListeners.get(i).intValue(),
					IDiyMsgIds.PREVIEW_REPLACE_COMPLETE, -1, null, null);
		}
	}

	@Override
	public void setCurrent(int cardId) {
		GoLauncher.postMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.SCREEN_SET_CURRENT,
				cardId, null, null);
	}

	@Override
	public void previewLongClick(int cardIndex) {
		OrientationControl.keepCurrentOrientation(mActivity);
		sPreviewLongClick = true;
		GoSettingControler settingControler = GOLauncherApp.getSettingControler();
		DesktopSettingInfo info = settingControler.getDesktopSettingInfo();
		sPreLongscreenFrameStatus = !info.mShowStatusbar;

		// 得到当前屏幕状态
		if (!sPreLongscreenFrameStatus) {
			// //隐藏指示器
			// GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
			// IDiyMsgIds.HIDDEN_INDICATOR, -1, null, null);
			// previewOperate = true;
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
					IDiyMsgIds.SHOW_HIDE_STATUSBAR, -2, true, null);
			SenseWorkspace.showStatusBar = true;
		}
		mLayout.getIndicator().setVisibility(View.INVISIBLE);
		// 隐藏添加卡片
		mWorkspace.hideAddCard();
		// setDragCaption();
		List<Rect> rects = mWorkspace.getCurScreenRects();
		// 将工作区锁住
		mWorkspace.lock();
		// 起预览替换层
		View child = mWorkspace.getChildAt(cardIndex);
		boolean isSuccess = GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
				IFrameworkMsgId.SHOW_FRAME, IDiyFrameIds.REPLACE_DRAG_FRAME, null, null);
		if (isSuccess) {
			vibrate();
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.GET_HOME_CURRENT,
					-1, null, null);
			GoLauncher
					.sendMessage(this, IDiyFrameIds.REPLACE_DRAG_FRAME,
							IDiyMsgIds.REPLACE_DRAG_INIT, mWorkspace.getCurScreenStartIndex(),
							child, rects);
		}
	}

	private void vibrate() {
		mLayout.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
				HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
	}

	private void getScreenWH() {
		sScreenW = DrawUtils.sWidthPixels;
		sScreenH = DrawUtils.sHeightPixels;

		// 设置PreviewController的显示模式，横或竖，以正确获取资源
		if (sScreenH > sScreenW) {
			PreviewController.sDisplayMode = PreviewController.PORT;
		} else {
			PreviewController.sDisplayMode = PreviewController.LAND;
		}
	}

	/**
	 * 设置桌面是否可见
	 * 
	 * @param show
	 *            true显示桌面
	 */
	private void notifyDesktop(boolean show) {
		if (show) {
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
					IDiyMsgIds.PREVIEW_NOTIFY_DESKTOP, 1, null, null);
			mFrameManager.setFrameVisiable(IDiyFrameIds.DOCK_FRAME, View.VISIBLE);
		} else {
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
					IDiyMsgIds.PREVIEW_NOTIFY_DESKTOP, 0, null, null);
			mFrameManager.setFrameVisiable(IDiyFrameIds.DOCK_FRAME, View.GONE);
		}
	}

	@Override
	public void setIndicatorVisible(boolean isVisible) {
		if (mIndicator == null) {
			return;
		}

		if (isVisible) {
			mIndicator.setVisibility(View.VISIBLE);
			mIndicator.requestLayout();
		} else {
			mIndicator.setVisibility(View.INVISIBLE);
		}
	}

	private static void setBackFromSetting(boolean value) {
		sBackFromSetting = value;
	}

	public static boolean backFromSetting() {
		return sBackFromSetting;
	}

	@Override
	public void clickIndicatorItem(int index) {
		if (null != mWorkspace && index < mWorkspace.getScreenCount()) {
			mWorkspace.snapToScreen(index);
		}
	}

	@Override
	public void sliding(float percent) {

		if (0 <= percent && percent <= 100) {
			mWorkspace.getScreenScroller().setScrollPercent(percent);
		}
	}

	/**
	 * 是否拖拽图标（或widget）进入屏幕预览
	 * */
	public static boolean isEnterFromDragView() {
		return sIsEnterFromDragView;
	}

	public static void setIsEnterFromDragView(boolean isEnterFromDragView) {
		sIsEnterFromDragView = isEnterFromDragView;
	}
	
	public static void setIsEnterFromQA(boolean isEnterFromQA) {
		sIsEnterFromQA = isEnterFromQA;
	}

	@Override
	public void onAnimationStart(Animation animation) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAnimationEnd(Animation animation) {
		if (mTextLayoutvisiable) {
			mTextLayoutvisiable = false;
			if (!sPreLongscreenFrameStatus && SenseWorkspace.showStatusBar) {
				// previewOperate = true;
				GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						IDiyMsgIds.SHOW_HIDE_STATUSBAR, -2, false, null);
				SenseWorkspace.showStatusBar = false;
			}
		}
		if (mDragFinish) {
			mDragFinish = false;
			// 通知DragFrame隐藏屏幕不足的文字提示
			GoLauncher.sendMessage(this, IDiyFrameIds.DRAG_FRAME,
					IDiyMsgIds.FINISH_DRAG_WHEN_NO_ROOM, -1, null, null);
		}
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		// TODO Auto-generated method stub

	}
}
