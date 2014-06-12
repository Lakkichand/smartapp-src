package com.jiubang.ggheart.apps.appfunc.component;

import java.util.List;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.view.MotionEvent;

import com.jiubang.core.mars.MImage;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.model.DeliverMsgManager;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.data.BroadCaster.BroadCasterObserver;
import com.jiubang.ggheart.data.info.AppItemInfo;

/**
 * “最近打开”的图标
 * 
 * @author penglong
 * 
 */
public class RecentAppsIcon extends BaseAppIcon implements BroadCasterObserver {
	/**
	 * 应用程序信息
	 */
	private AppItemInfo mInfo; // 需要自己维护

	/**
	 * 标记是否在最近打开tab 启动应用
	 */
	public static boolean sIsStartFromRencetTab = false;
	/**
	 * 是否被放大
	 */
	// private boolean mIsEnlarged;

	public RecentAppsIcon(Activity activity, int tickCount, int x, int y, int width, int height,
			AppItemInfo info, BitmapDrawable editPic, boolean isDrawText) {
		super(activity, tickCount, x, y, width, height, info.mIcon, null, editPic, null,
				info.mTitle, isDrawText);
		// 注册加载资源事件
		DeliverMsgManager.getInstance().registerDispenseMsgHandler(AppFuncConstants.LOADTHEMERES,
				this);
		mInfo = info;
		mInfo.registerObserver(this);
	}

	/**
	 * 处理Click事件
	 * 
	 * @return
	 */
	@Override
	protected boolean onClickEvent(Object event, int arg, Object object) {
		if (event != null) {
			// 屏幕触摸事件
			int offsetX = arg;
			int offsetY = (Integer) object;
			MotionEvent motionEvent = (MotionEvent) event;

			if (inIconRange((int) motionEvent.getX(), (int) motionEvent.getY(), offsetX, offsetY)) {
				// 点击在应用程序图标范围内
				// 启动程序
				// AppmanagerFacade.getInstance(mActivity).invokeApp(mInfo);
				sIsStartFromRencetTab = true;
				GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						IDiyMsgIds.START_ACTIVITY, -1, mInfo.mIntent, null);
				ApplicationIcon.sIsStartApp = true;
				return true;
			}
		} else {
			// 回车键触发启动应用程序
			if (mEditMode == false) {
				// AppmanagerFacade.getInstance(mActivity).invokeApp(mInfo);
				GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						IDiyMsgIds.START_ACTIVITY, -1, mInfo.mIntent, null);
				return true;
			}
		}
		return false;
	}

	/**
	 * 处理Up&Down事件
	 */
	@Override
	protected boolean onUpDownEvent(Object event, int offsetX, int offsetY) {
		if (event != null) {
			MotionEvent motionEvent = (MotionEvent) event;
			if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
				// 无论是否在编辑模式，只要点击范围在图标范围内，图标就变成半透明效果
				if (inIconRange((int) motionEvent.getX(), (int) motionEvent.getY(), offsetX,
						offsetY)) {
					if (mAppPic != null) {
						mAppPic.setAlpha(128);
					}
					// mEditPic.setAlpha(128);
					if (mAppText != null) {
						if (!GoLauncher.getCustomTitleColor()) {
							mAppText.setPaintAlpha(128);
						}
					}
					mTouchDown = true;
					return true;
				}
			} else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
				// if(inIconRange((int)motionEvent.getX(),
				// (int)motionEvent.getY(), offsetX, offsetY)){
				if (mTouchDown == true) {
					if (mAppPic != null) {
						mAppPic.setAlpha(255);
					}
					// mEditPic.setAlpha(255);
					if (mAppText != null) {
						if (!GoLauncher.getCustomTitleColor()) {
							mAppText.setPaintAlpha(255);
						}
					}
					mTouchDown = false;
					return true;
				}
			}
		}
		return false;
	}

	@Override
	protected void drawCurrentFrame(Canvas canvas) {
		// mBgPaint.setColor(AppFuncConstants.ICON_BG_FOCUSED);
		// canvas.drawRoundRect(new RectF(0,0,mWidth,mHeight), 10, 10,
		// mBgPaint);
		// 先绘制背景图
		// if(mIsPressed){
		// //优先绘制按键背景
		// mBgPaint.setColor(AppFuncConstants.ICON_BG_PRESSED);
		// canvas.drawRoundRect(mBgRect, 10, 10, mBgPaint);
		// }
		// //长按产生放大和半透明效果
		// if(mIsEnlarged){
		// Log.e("RecentAppsIcon","drawCurrentFrame isEnlarged");
		// canvas.save();
		// canvas.scale(mScaleFactor, mScaleFactor, mWidth/2, mHeight/2);
		// }
		if (mIsFocused && mBgPaint != null) {
			// 绘制聚焦时的背景
			mBgPaint.setColor(sFocusedBgColor);
			canvas.drawRoundRect(mBgRect, 5, 5, mBgPaint);
		}
		drawAllChildComponents(canvas);
	}

	@Override
	protected boolean animate() {
		return false;
	}

	public void setAppInfo(AppItemInfo info) {
		if (info == null || info == mInfo) {
			// 如果设置的iteminfo指针一样，就没必要再进行Mimage生成和字符串的处理
			return;
		}
		if (mInfo != null) {
			mInfo.unRegisterObserver(this);
		}
		mInfo = info;
		mInfo.registerObserver(this);
		// info.mIcon.setTargetDensity(mActivity.getResources().getDisplayMetrics());
		if (mAppPic == null) {
			mAppPic = new MImage(info.mIcon);
		} else {
			mAppPic.setDrawable(info.mIcon);
		}
		mTitle = info.mTitle;
		if (mAppText != null) {
			mAppText.setNameTxt(info.mTitle);
		}
	}

	public AppItemInfo getInfo() {
		return mInfo;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void onBCChange(int msgId, int param, Object object, List objects) {
		// TODO Auto-generated method stub
		switch (msgId) {
			case AppItemInfo.INCONCHANGE : {
				// mInfo.mIcon = (BitmapDrawable)object;
				mAppPic = null;
				if (mAppPic == null) {
					mAppPic = new MImage((BitmapDrawable) object);
				} else {
					mAppPic.setDrawable((BitmapDrawable) object);
				}
				break;
			}
			case AppItemInfo.TITLECHANGE : {
				if (mAppText != null) {
					mAppText.setNameTxt((String) object);
				}
				break;
			}
			default :
				break;
		}
	}

}
