package com.jiubang.ggheart.apps.desks.appfunc.search;

import java.util.List;

import android.app.Activity;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

import com.gau.go.launcherex.R;
import com.go.util.animation.AnimationFactory;
import com.jiubang.core.framework.AbstractFrame;
import com.jiubang.core.framework.IFrameManager;
import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.ggheart.apps.desks.appfunc.XViewFrame;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.gowidget.gostore.views.BroadCaster;
import com.jiubang.ggheart.data.info.ThemeSettingInfo;
import com.jiubang.ggheart.data.statistics.StatisticsAppFuncSearch;
import com.jiubang.ggheart.launcher.GOLauncherApp;
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  dingzijian
 * @date  [2012-10-17]
 */
public class AppFuncSearchFrame extends AbstractFrame implements AnimationListener {

	private FrameLayout mContentView;
	private AppFuncSearchView mSearchView;
//	private Animation mAnimation;
	private AppfuncSearchController mSearchController;
	private static  BroadCaster sBroadCaster = new BroadCaster();
	public static boolean sIsSearchVisable = false;
	protected static final int STATE_ENTER = 0; // 进入功能表搜索
	protected static final int STATE_LEFT = 1; // 离开功能表搜索
	private int mState;
	private ThemeSettingInfo mThemeSettingInfo;
	public AppFuncSearchFrame(Activity activity, IFrameManager frameManager, int id) {
		super(activity, frameManager, id);
		initViews();
		mThemeSettingInfo = GOLauncherApp.getSettingControler().getThemeSettingInfo();
		GoLauncher.registMsgHandler(this);
	}
	
	public static void setSearchVisable(boolean visible) {
		sIsSearchVisable = visible;
	}

	@Override
	public View getContentView() {
		return mContentView;
	}

	private void initViews() {
		// if (mContentView != null) {
		// mContentView.recyle();
		// }
		mContentView = new FrameLayout(mActivity);
		mContentView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		mSearchView = (AppFuncSearchView) View.inflate(mActivity,
				R.layout.appfunc_search_main, null);
		mContentView.addView(mSearchView);
		mSearchView.setSearchFrame(this);
	}

	@Override
	public boolean isOpaque() {
		// 对动态壁纸的特殊
		if (XViewFrame.getInstance().isDrawMergeBg()) {
			return false;
		}

		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean handleMessage(Object who, int type, int msgId, int param, Object object,
			List objects) {
		switch (msgId) {
			case IDiyMsgIds.BACK_TO_MAIN_SCREEN : {
				// 移除自己
				exitSearchFrame();
			}
				return true;
			case IFrameworkMsgId.SYSTEM_HOME_CLICK : {
				exitSearchFrame();
				return true;
			}
			case IFrameworkMsgId.SYSTEM_ON_PAUSE : {
				
				return true;
			}
			case IFrameworkMsgId.SYSTEM_ON_NEW_INTENT : {
				exitSearchFrame();
				return true;
			}
			case IDiyMsgIds.MEDIA_DATA_LOAD_FINISH :
				mSearchController.setMediaSourceData(param, objects);
				
				break;
			case IDiyMsgIds.SYSTEM_CONFIGURATION_CHANGED :			
				mSearchView.clearMenu();
				int index = mSearchView.getScreenScroller().getCurrentScreen();
				int adapterIndex = mSearchView.whichAdapter(index);
				String key = mSearchView.getSearchKey();
					mSearchView = (AppFuncSearchView) View.inflate(mActivity,
							R.layout.appfunc_search_main, null);
					mSearchView.setSearchFrame(this);
					mContentView.removeAllViews();
					mContentView.addView(mSearchView);
					mContentView.requestLayout();
					mSearchView.setSearchKey(key);
					mSearchView.getScreenScroller().setCurrentScreen(index);
					mSearchView.setSearchTab(index);
					mSearchController.setUIHandler(mSearchView.getUIHandler());
					mSearchController.getEngineData(adapterIndex);
				break;
			case IDiyMsgIds.MEDIA_PLUGIN_CHANGE :
				if (mSearchView != null) {
					mSearchView.mediaPluginChange(object);
				}
			default :
				break;
		}
		return super.handleMessage(who, type, msgId, param, object, objects);
	}

	private void exitSearchFrame() {
		if (mSearchView != null) {
			mSearchView.finishSearchFrame(false);
		}
	}

	@Override
	public void onAdd() {
		super.onAdd();
		mFrameManager.registKey(this);
		mSearchController = AppfuncSearchController.getInstance(mActivity,
				mSearchView.getUIHandler());
		sBroadCaster.registerObserver(mSearchController);
		sBroadCaster.broadCast(AppfuncSearchController.BC_MSG_APPFUNC_FRAME_START, -1, null, null);
		setSearchVisable(true);
		XViewFrame.getInstance().invalidate();
		startAnimation(STATE_ENTER);
		GoLauncher.sendMessage(this, IDiyFrameIds.APPFUNC_FRAME, IDiyMsgIds.START_LOAD_MEDIA_DATA,
				0, null, null);
		StatisticsAppFuncSearch.countSearchStatistics(mActivity,
				StatisticsAppFuncSearch.APPFUNC_SEARCH_USED_TIMES);
	}
	@Override
	public void onVisiable(int visibility) {
		if (visibility != View.VISIBLE) {
			setSearchVisable(false);
		} else if (visibility != View.INVISIBLE) {
			setSearchVisable(true);
		}
		XViewFrame.getInstance().invalidate();
		super.onVisiable(visibility);
	}
	@Override
	public void onRemove() {
		super.onRemove();
		sBroadCaster.broadCast(AppfuncSearchController.BC_MSG_APPFUNC_FRAME_END, -1, null, null);
		sBroadCaster.clearAllObserver();
		setSearchVisable(false);
		if (mSearchView != null) {
			mSearchView.clearMenu();
			mSearchView.recyle();
		}
		mContentView = null;
		mFrameManager.unRegistKey(this);
	}
@Override
public boolean dispatchKeyEvent(KeyEvent arg0) {

	return super.dispatchKeyEvent(arg0);
}
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK :
				mSearchView.exitOrShowHistory();
				return true;
			default :
				break;
		}
		return super.onKeyUp(keyCode, event);
	}

	protected void startAnimation(int state) {
		mState = state;
		Animation animation = null;
//		if (!isAnimationWorking()) {
			if (mContentView != null) {
				int effect = 1;
//				if (mAnimation != null && !mAnimation.hasEnded()) {
//					mAnimation.reset();
//				}
				switch (state) {
					case STATE_ENTER :
//						mAnimation = AnimationFactory.createEnterAnimation(effect, mActivity);
//						if (mAnimation != null) {
//							GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
//									IFrameworkMsgId.SHOW_FRAME, IDiyFrameIds.ANIMATION_FRAME, null,
//									null);
//							GoLauncher.sendMessage(this, IDiyFrameIds.ANIMATION_FRAME,
//									IDiyMsgIds.SET_APP_DRAWER_TRANSITION_LISTENER, -1, this, null);
//							GoLauncher.sendMessage(this, IDiyFrameIds.ANIMATION_FRAME,
//									IDiyMsgIds.START_APP_DRAWER_SEARCH_ENTER_TRANSITION, -1, null,
//									null);
//							GoLauncher.sendMessage(this, IDiyFrameIds.ANIMATION_FRAME,
//									IDiyMsgIds.SET_APP_DRAWER_SEARCH_TRANSITION_SEARCH_ANIMATION,
//									-1, mAnimation, null);
//						} 
						animation = AnimationFactory.createEnterAnimation(effect, mActivity);
						//为null的时候就是无特效
					if (animation != null && !mThemeSettingInfo.mTransparentStatusbar) {
						mContentView.setDrawingCacheEnabled(true);
						animation.setAnimationListener(this);
						mContentView.startAnimation(animation);
					}
						else {
							onAnimationEnd(null);
						}
						break;
					case STATE_LEFT :
						animation = AnimationFactory.createExitAnimation(effect, mActivity);
//						if (mAnimation != null) {
//							GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
//									IFrameworkMsgId.SHOW_FRAME, IDiyFrameIds.ANIMATION_FRAME, null,
//									null);
//							GoLauncher.sendMessage(this, IDiyFrameIds.ANIMATION_FRAME,
//									IDiyMsgIds.SET_APP_DRAWER_TRANSITION_LISTENER, -1, this, null);
//							GoLauncher.sendMessage(this, IDiyFrameIds.ANIMATION_FRAME,
//									IDiyMsgIds.START_APP_DRAWER_SEARCH_LEAVE_TRANSITION, -1, null,
//									null);
//							GoLauncher.sendMessage(this, IDiyFrameIds.ANIMATION_FRAME,
//									IDiyMsgIds.SET_APP_DRAWER_SEARCH_TRANSITION_SEARCH_ANIMATION,
//									-1, mAnimation, null);
//						}
						if (animation != null && !mThemeSettingInfo.mTransparentStatusbar) {
							mContentView.setDrawingCacheEnabled(true);
							animation.setAnimationListener(this);
							mContentView.startAnimation(animation);
						}
						else {
							onAnimationEnd(null);
						}
						break;
					default :
						break;
				}

			}
//		}
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		if (mContentView != null) {
			mContentView.setDrawingCacheEnabled(false);
		}
		switch (mState) {
			case STATE_ENTER :
//				if (mContentView != null) {
//					mSearchView.showIM(true, 0);
//				}
				break;
			case STATE_LEFT :
				GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						IFrameworkMsgId.REMOVE_FRAME, getId(), null, null);
				XViewFrame.getInstance().postInvalidate();
				break;
			default :
				break;
		}
	}

	@Override
	public void onAnimationRepeat(Animation animation) {

	}

	@Override
	public void onAnimationStart(Animation animation) {

	}
	
	public static void broadCast(int msgId, int param, Object object, Object object2) {
		sBroadCaster.broadCast(msgId, param, object, object2);
	}
	
	public boolean isTopFrame() {
		if (mFrameManager.getTopFrame() instanceof AppFuncSearchFrame) {
			return true;
		}
		return false;
	}
}
