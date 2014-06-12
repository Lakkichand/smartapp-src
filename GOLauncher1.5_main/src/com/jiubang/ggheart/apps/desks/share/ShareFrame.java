package com.jiubang.ggheart.apps.desks.share;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

import com.gau.go.launcherex.R;
import com.go.util.window.OrientationControl;
import com.jiubang.core.framework.AbstractFrame;
import com.jiubang.core.framework.IFrameManager;
import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 
 * <br>类描述: 桌面分享层
 * <br>功能详细描述:
 * 
 * @author  maxiaojun
 * @date  [2012-9-25]
 */
public class ShareFrame extends AbstractFrame implements AnimationListener {
	public static final int TYPE_SHARE = 0;
	public static final int TYPE_CAPTURE = 1;
	/**
	 * 分享的类型:纯分享，截图分享
	 */
	private int mType = TYPE_CAPTURE;
	private Activity mActivity;
	private int mOritationTpye; // 记录进入分享时的横竖屏类型
	private ShareLayout myShareLayout; // 分享界面
	private static final int ANIMATION_DURATION = 300; // 动画持续时间

	private int mAnimeType; //动画类型
	private static final int TYPE_ANIMATION_IN = 0; // 进入动画
	private static final int TYPE_ANIMATION_OUT = 1; // 退出动画

	private boolean mIsShow = true;
	private int mOrientation;
	/**
	 * <默认构造函数>
	 */
	public ShareFrame(Activity activity, IFrameManager arg1, int arg2) {
		super(activity, arg1, arg2);
		mActivity = activity;
		LayoutInflater inflater = LayoutInflater.from(mActivity);
		myShareLayout = (ShareLayout) inflater.inflate(R.layout.share_layout, null);
		mOrientation = OrientationControl.getRequestOrientation(mActivity);
		setOritation();
//		Bundle bundle = new Bundle();
//		GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IDiyMsgIds.GET_SHARE_TYPE, -1,
//				bundle, null);
//		mType = SnapShotManager.;
		myShareLayout.init(mActivity, mType);
		enterAnimation();
		checkTutorial();
		mIsShow = true;

	}

	/***
	 * <br>功能简述:  检查是否第一次使用分享
	 * <br>功能详细描述: 如果第一次，菜单项需添加绿色背景
	 * <br>注意:
	 */
	private void checkTutorial() {
		PreferencesManager sharedPreferences = new PreferencesManager(mActivity,
				IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
		boolean needStartutorial = sharedPreferences.getBoolean(IPreferencesIds.SHOULD_SHOW_SHARE,
				true);
		if (needStartutorial) {
			sharedPreferences.putBoolean(IPreferencesIds.SHOULD_SHOW_SHARE, false);
			sharedPreferences.commit();
		}
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
	private void reStoreOritation() {
		if (mOritationTpye == OrientationControl.AUTOROTATION) {
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
					IDiyMsgIds.SCREEN_SET_ORIENTATION, OrientationControl.AUTOROTATION, null, null);
		}
	}

	@Override
	public boolean handleMessage(Object who, int type, int msgId, int param, Object object,
			@SuppressWarnings("rawtypes") List objects) {
		super.handleMessage(who, type, msgId, param, object, objects);
		boolean ret = false;
		switch (msgId) {
			case IFrameworkMsgId.SYSTEM_HOME_CLICK :
				if (!mIsShow) {
					return ret;
				}
				mIsShow = false;

				exitAnimation();
				break;
		}
		return ret;

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
			if (!mIsShow) {
				return super.onKeyDown(keyCode, event);
			}
			myShareLayout.setIsSharing(false);
			mIsShow = false;
			exitAnimation();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/***
	 * 进入
	 */
	private void enterAnimation() {
		if (null == myShareLayout) {
			return;
		}
		Animation animationIn = AnimationUtils.loadAnimation(mActivity, R.anim.zoom_enter);
		animationIn.setDuration(ANIMATION_DURATION);
		mAnimeType = TYPE_ANIMATION_IN;
		myShareLayout.startAnimation(animationIn);
	}

	/***
	 * 退出
	 */
	private void exitAnimation() {
		if (null == myShareLayout) {
			return;
		}
		Animation animationout = AnimationUtils.loadAnimation(mActivity, R.anim.zoom_exit);
		animationout.setDuration(ANIMATION_DURATION);
		mAnimeType = TYPE_ANIMATION_OUT;
		animationout.setAnimationListener(this);
		myShareLayout.startAnimation(animationout);
	}

	@Override
	public View getContentView() {
		return myShareLayout;
	}

	@Override
	public void onVisiable(int visible) {
		super.onVisiable(visible);
	}

	@Override
	public void onRemove() {
		if (null != myShareLayout) {
			myShareLayout.clear();
		}
		reStoreOritation();
		super.onRemove();
	}

	@Override
	public void onResume() {
		//针对关掉屏幕，翻转屏后再打开的情况
		GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
				IDiyMsgIds.SCREEN_SET_ORIENTATION, mOrientation, null, null);
		super.onResume();
	}

	@Override
	public void onDestroy() {
		if (null != myShareLayout) {
			myShareLayout.clear();
		}
		super.onDestroy();
	}

	@Override
	public void onAnimationStart(Animation animation) {

	}

	@Override
	public void onAnimationEnd(Animation animation) {
		if (mAnimeType == TYPE_ANIMATION_OUT) {
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IFrameworkMsgId.REMOVE_FRAME,
					IDiyFrameIds.SHARE_FRAME, null, null);
		}
	}

	@Override
	public void onAnimationRepeat(Animation animation) {

	}
}
