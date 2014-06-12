package com.jiubang.ggheart.apps.appfunc.component;

import java.util.List;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.view.MotionEvent;

import com.go.util.log.LogUnit;
import com.jiubang.core.mars.IAnimateListener;
import com.jiubang.core.mars.MImage;
import com.jiubang.core.mars.XAEngine;
import com.jiubang.core.mars.XALinear;
import com.jiubang.core.mars.XMElastic;
import com.jiubang.core.mars.XMotion;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncFrame;
import com.jiubang.ggheart.apps.desks.appfunc.OrientationInvoker;
import com.jiubang.ggheart.apps.desks.appfunc.animation.AnimationManager;
import com.jiubang.ggheart.apps.desks.appfunc.animation.AnimationManager.AnimationInfo;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.model.DeliverMsgManager;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.data.BroadCaster.BroadCasterObserver;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.AppSettingDefault;
import com.jiubang.ggheart.data.info.FunTaskItemInfo;

/**
 * “程序管理”的图标
 * 
 * @author penglong
 * 
 */
public class ProManageIcon extends BaseAppIcon implements BroadCasterObserver {
	/**
	 * 应用程序信息
	 */
	private FunTaskItemInfo mInfo;
	/**
	 * 是否需要重绘：TODO 后期去掉，框架统一管理
	 */
	private boolean mRePaint;
	// /////////////////////////////////////////////////////////////////////
	// ///////////////////////////处理图标长按事件////////////////////////////////
	/**
	 * 是否被放大
	 */
	private boolean mIsEnlarged;
	/**
	 * 长按时的缩放比例因子
	 */
	private float mScaleFactor;
	/**
	 * 图标抓起的动画
	 */
	private XALinear mDragUpMotion;
	/**
	 * 图标抓起的透明效果渐变因子
	 */
	private volatile int mDragUpMotionAlphaFactor;
	/**
	 * 图标抓起的的原透明度
	 */
	private int mDragSrcAlpha;
	/**
	 * 归位动画
	 */
	private XMotion mbackMotion;
	private XMotion mAlphaMotion;
	private int mAlphaMotionFactor;
	/**
	 * 长按过程是否发生位置改变
	 */
	private boolean mNoPlaceChanges;
	//	/**
	//	 * 关闭程序图标
	//	 */
	//	private BitmapDrawable mCloseAppDrawable = null;
	//	/**
	//	 * 高亮关闭程序图标
	//	 */
	//	private BitmapDrawable mCloseHighlightAppDrawable = null;
	//
	//	/**
	//	 * 锁图标
	//	 */
	//	private BitmapDrawable mLockDrawable = null;

	public ProManageIcon(Activity activity, /*ProManageEditDock proManageEdithome,*/int tickCount,
			int x, int y, int width, int height, FunTaskItemInfo info, BitmapDrawable lockPic,
			BitmapDrawable editPic, BitmapDrawable editLightPic, boolean isDrawText) {
		super(activity, tickCount, x, y, width, height, info != null
				? info.getAppItemInfo().mIcon
				: null, lockPic, editPic, editLightPic, info != null
				? info.getAppItemInfo().mTitle
				: null, isDrawText);
		// 注册加载资源事件
		DeliverMsgManager.getInstance().registerDispenseMsgHandler(AppFuncConstants.LOADTHEMERES,
				this);
		//		this.proManageEdithome = proManageEdithome;
		mInfo = info;
		if (info != null) {
			info.getAppItemInfo().registerObserver(this);
		}
		loadResource();
		mShowEditPic = true;
	}

	public void setShowStyle(int showStyle) {
		mIconStyle = showStyle;
	}

	/**
	 * 处理Click事件
	 * 
	 * @return
	 */
	@Override
	@SuppressWarnings("finally")
	protected boolean onClickEvent(Object event, int arg, Object object) {
		if (event != null) {
			// 屏幕触摸事件
			int offsetX = arg;
			int offsetY = (Integer) object;
			MotionEvent motionEvent = (MotionEvent) event;
			if (mEditMode == true) {
				// 编辑模式下，关闭程序
				if (isInEditPicComponent((int) motionEvent.getX() + offsetX,
						(int) motionEvent.getY() + offsetY)) {
					AppFuncFrame.getDataHandler().terminateApp(mInfo.getPid());
					return false;
				}
				// 点击图标打开弹出菜单
				else {
					int x = getAbsX() - offsetX;
					int y = (int) ((getAbsY() + GoLauncher.getStatusbarHeight() * 1.5) - offsetY);
					Rect rect = new Rect(x, y, x + mWidth, y + mHeight);
					ProManageIconInfo info = new ProManageIconInfo(rect, mInfo);
					DeliverMsgManager.getInstance().onChange(AppFuncConstants.XVIEW,
							AppFuncConstants.SHOWQUICKACTIONMENU, info);
				}
				return false;
			}
			if (inIconRange((int) motionEvent.getX(), (int) motionEvent.getY(), offsetX, offsetY)) {
				ApplicationIcon.sIsStartApp = true;
				GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						IDiyMsgIds.START_ACTIVITY, -1, mInfo.getAppItemInfo().mIntent, null);
			}
		}
		return false;
	}

	// /**
	// * 处理Up&Down事件
	// */
	// @Override
	// protected boolean onUpDownEvent(Object event, int offsetX, int offsetY) {
	// if (event != null) {
	// MotionEvent motionEvent = ((MotionEvent) event);
	// if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
	// // 无论是否在编辑模式，只要点击范围在图标范围内，图标就变成半透明效果
	//
	// if (inIconRange((int) motionEvent.getX(),
	// (int) motionEvent.getY(), offsetX, offsetY)) {
	// if (mAppPic != null) {
	// mAppPic.setAlpha(128);
	// }
	// if (mEditPic != null) {
	// mEditPic.setAlpha(128);
	// }
	// if (mAppText != null) {
	// if (!GoLauncher.getCustomTitleColor()) {
	// mAppText.setPaintAlpha(128);
	// }
	// }
	// mTouchDown = true;
	// return true;
	// }
	// } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
	// // if(inIconRange((int)motionEvent.getX(),
	// // (int)motionEvent.getY(), offsetX, offsetY)){
	// if (mTouchDown == true) {
	// if (mAppPic != null) {
	// mAppPic.setAlpha(255);
	// }
	// if (mEditPic != null) {
	// mEditPic.setAlpha(255);
	// }
	// if (mAppText != null) {
	// if (!GoLauncher.getCustomTitleColor()) {
	// mAppText.setPaintAlpha(255);
	// }
	// }
	// mTouchDown = false;
	// return true;
	// }
	// }
	// }
	// return false;
	// }

	@Override
	protected void drawCurrentFrame(Canvas canvas) {
		if (mAlphaMotion != null) {
			setAlpha(mAlphaMotionFactor);
			drawAllChildComponents(canvas);
		}
		// 长按产生放大和半透明效果
		if (mIsEnlarged) {
			canvas.save();
			canvas.scale(mScaleFactor, mScaleFactor, mWidth / 2, mHeight / 2);
			mDragSrcAlpha = getAlpha();
			if (!mIsInEdge) {
				setAlpha((int) (2.55f * mDragUpMotionAlphaFactor));
			}
		}

		if (mIsFocused && mBgPaint != null) {
			// 绘制聚焦时的背景
			mBgPaint.setColor(sFocusedBgColor);
			canvas.drawRoundRect(mBgRect, 5, 5, mBgPaint);
		}
		drawAllChildComponents(canvas);
		if (mIsEnlarged) {
			setAlpha(mDragSrcAlpha);
			canvas.restore();
		}
	}

	@Override
	protected boolean animate() {
		boolean isAnimate = false;
		if (mAlphaMotion != null) {
			mIsEnlarged = false;
			if (mAlphaMotion.isFinished()) {
				mAnimManager.cancelAnimation(this, mAlphaMotion);
				//				detachAnimator(mAlphaMotion);
				mAlphaMotion = null;
			} else {
				mAlphaMotionFactor = mAlphaMotion.GetCurX();
			}
			isAnimate = true;
		} else if (null != mbackMotion) {
			if (mbackMotion.isFinished()) {
				mAnimManager.cancelAnimation(this, mbackMotion);
				//				detachAnimator(mbackMotion);
				mbackMotion = null;
				mIsEnlarged = false;
				if (mIsDrawText) {
					setAppTextDrawingCacheEnable(true);
				}
				// setAlpha(255);
			} else {
				int currentX = mbackMotion.GetCurX();
				int currentY = mbackMotion.GetCurY();
				if (mNoPlaceChanges == false) {
					mX = mbackMotion.GetCurX();
					mY = mbackMotion.GetCurY();
				}
				int factor;
				if (mbackMotion.GetEndX() != mbackMotion.GetStartX()) {
					factor = Math.abs((currentX - mbackMotion.GetStartX())
							/ (mbackMotion.GetEndX() - mbackMotion.GetStartX()));
				} else {
					factor = Math.abs((currentY - mbackMotion.GetStartY())
							/ (mbackMotion.GetEndY() - mbackMotion.GetStartY()));
				}
				mScaleFactor = 1.2f - 0.2f * factor;
				int alpha = 128 + factor * 127;
				if (alpha > 255) {
					alpha = 255;
				}
				setAlpha(alpha);
			}
			isAnimate = true;
		} else
		// 图标抓起动画
		if (mDragUpMotion != null) {
			if (mDragUpMotion.isFinished()) {
				mAnimManager.cancelAnimation(this, mDragUpMotion);
				//				detachAnimator(mDragUpMotion);
				mDragUpMotion = null;
			} else {
				// 3.25取消半透明效果
				//				mDragUpMotionAlphaFactor = mDragUpMotion.GetCurX();
				mScaleFactor = 1.2f * mDragUpMotion.GetCurY() * 0.01f;
			}
			isAnimate = true;
		}
		if (mRePaint) {
			mRePaint = false;
			isAnimate = true;
		}
		return isAnimate;
	}

	public void setAppInfo(FunTaskItemInfo info) {
		if (mInfo != null && info != null && info.equals(mInfo)) {
			return;
		}
		if (mInfo != null) {
			mInfo.getAppItemInfo().unRegisterObserver(this);
		}
		mInfo = info;
		if (info != null && info.getAppItemInfo() != null) {
			if (mAppPic == null) {
				mAppPic = new MImage(info.getAppItemInfo().mIcon);
			} else {
				mAppPic.setDrawable(info.getAppItemInfo().mIcon);
			}
			mTitle = info.getAppItemInfo().mTitle;
			if (mAppText != null) {
				mAppText.setNameTxt(info.getAppItemInfo().mTitle);
			}
			info.getAppItemInfo().registerObserver(this);
		}
	}

	public FunTaskItemInfo getInfo() {
		return mInfo;
	}

	@Override
	public void onBCChange(int msgId, int param, Object object, List objects) {
		// TODO Auto-generated method stub
		switch (msgId) {
			case AppItemInfo.INCONCHANGE : {
				if (mAppPic == null) {
					mAppPic = new MImage((BitmapDrawable) object);
				} else {
					mAppPic.setDrawable((BitmapDrawable) object);
				}
				mRePaint = true;
				break;
			}
			case AppItemInfo.TITLECHANGE : {
				if (mAppText != null) {
					mAppText.setNameTxt((String) object);
				}
				mRePaint = true;
				break;
			}
			default :
				break;
		}
	}

	/**
	 * 处理长按事件
	 * */
	@Override
	protected boolean onLongClickEvent(Object event, int offsetX, int offsetY) {
		// 放大图标和文字，并且产生半透明效果
		if (event != null) {
			MotionEvent motionEvent = (MotionEvent) event;

			if (mEditMode && mShowEditPic) {
				if (isInEditPicComponent((int) motionEvent.getX() + offsetX,
						(int) motionEvent.getY() + offsetY)) {
					mIsEditPress = true;
					return false;
				}
			}

			if (inIconRange((int) motionEvent.getX(), (int) motionEvent.getY(), offsetX, offsetY)) {
				noticeHomeComponet(false);
				startShake();
				return true;
			}
		}
		return false;
	}

	/**
	 * 开始抖动
	 */
	public void startShake() {
		setAlpha(255);
		mScaleFactor = 1.2f * 85 * 0.01f;
		mAnimManager.cancelAnimation(this, mDragUpMotion);
		//		detachAnimator(mDragUpMotion);
		mDragUpMotion = null;
		mDragUpMotionAlphaFactor = 100;
		mDragUpMotion = new XALinear(1, XALinear.XALINEAR_ECSPEED, 100, 85, 50, 100, 6, 1, 1);
		// attachAnimator(mDragUpMotion);
		AnimationInfo animInfo = new AnimationInfo(AnimationInfo.TYPE_SIMPLE, this, mDragUpMotion,
				null);
		mAnimManager.attachAnimation(animInfo, this);
		mIsEnlarged = true;
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
					if (mEditMode == true) {
						if (isInEditPicComponent((int) motionEvent.getX() + offsetX,
								(int) motionEvent.getY() + offsetY)) {
							mIsEditPress = true;
							return true;
						}
					} else {
						setAlpha(128);
						mTouchDown = true;
						return true;
					}
				}
			} else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
				if (mTouchDown == true && mEditMode == false) {
					//					proManageEdithome.clearEditDockBg();
					DeliverMsgManager.getInstance().onChange(AppFuncConstants.PRO_MANAGE_EDIT_DOCK,
							AppFuncConstants.PRO_MANAGE_EDIT_DOCK_CLEAR_EDIT_DOCK_BG, null);
					setAlpha(255);
					mTouchDown = false;
					return true;
				}
				mIsEditPress = false;

			}
		}
		return false;
	}

	/**
	 * 通知底部dock域改变 锁定程序 和取消锁定
	 * 
	 * @param isReset
	 *            true 恢复到 锁定程序
	 * */
	public void noticeHomeComponet(boolean isReset) {
		boolean temp = false;
		if (isReset) {
			//			proManageEdithome.changeLockText(true);
			temp = true;
		} else {
			if (mInfo.isInWhiteList()) {
				//				proManageEdithome.changeLockText(false);
				temp = false;
			} else {
				//				proManageEdithome.changeLockText(true);
				temp = true;
			}
		}
		DeliverMsgManager.getInstance().onChange(AppFuncConstants.PRO_MANAGE_EDIT_DOCK,
				AppFuncConstants.PRO_MANAGE_EDIT_DOCK_CHANGE_LOCK_TEXT, temp);
	}

	/**
	 * 设置是否为编辑模式
	 * */
	@Override
	public void setEditMode(boolean editModeEnabled) {
		if (mEditMode != editModeEnabled) {
			mEditMode = editModeEnabled;
			if (mIconImage != null) {
				if (mEditMode) {
					if (AppSettingDefault.APPFUNC_OPEN_EFFECT) {
						startMotion();
					}
				} else {
					if (mElastic != null) {
						mIconImage.detachAnimator(mElastic);
						mElastic = null;
					}
					mIconImage.exitEditMode();
				}
			}
		}
	}

	/**
	 * 抖动
	 * */
	private void startMotion() {
		mElastic = new XMElastic(1, XMElastic.XMElastic_EBackForth, -4, 0, 4, 0, 24); // -3,0,3,0,6);//
		mElastic.SetRepeatMode(true);

		mElastic.delay(XAEngine.ANIMATE_TYPE_FRAME, mDelayfactor);
		if (mDelayfactor > 6) {
			mDelayfactor = 0;
		} else {
			mDelayfactor++;
		}

		mIconImage.setRotateAnimator(mElastic);
	}

	/**
	 * 长按后的弹起事件
	 */
	@Override
	public void onLongClickUp(int x, int y, IAnimateListener listener,
			OrientationInvoker orientationInvoker) {
		// 如果没有发生位置偏移，则不产生移动动画
		// mIsFocused = true;
		noticeHomeComponet(true);
		mIsEditPress = false;
		if ((mX != x) || (mY != y)) {
			if (null != mbackMotion) {
				mAnimManager.cancelAnimation(this, mbackMotion);
				//				detachAnimator(mbackMotion);
				mbackMotion = null;
			}
			mNoPlaceChanges = false;
			LogUnit.d("ApplicationIcon new 557");
			mbackMotion = new XALinear(1, XALinear.XALINEAR_ECSPEED, mX, mY, x, y, 6, 0, 0);
			// attachAnimator(mbackMotion);
			// mbackMotion.setAnimateListener(listener);
			AnimationInfo animInfo = new AnimationInfo(AnimationInfo.TYPE_SIMPLE, this,
					mbackMotion, listener);
			AnimationManager.getInstance(mActivity).attachAnimation(animInfo, orientationInvoker);
		} else {
			setAlpha(255);
			mNoPlaceChanges = true;
			LogUnit.d("ApplicationIcon new 565");
			mbackMotion = new XALinear(1, XALinear.XALINEAR_ECSPEED, mX, mY, mX + 2, mY + 2, 4, 0,
					0);
			// attachAnimator(mbackMotion);
			AnimationInfo animInfo = new AnimationInfo(AnimationInfo.TYPE_SIMPLE, this,
					mbackMotion, null);
			AnimationManager.getInstance(mActivity).attachAnimation(animInfo, orientationInvoker);
		}
		mAnimManager.cancelAnimation(this, mDragUpMotion);
		//		detachAnimator(mDragUpMotion);
		mDragUpMotion = null;
	}

	/**
	 * 清楚动画效果
	 */
	public void clearMotion() {
		if (null != mbackMotion) {
			setAlpha(255);
			mAnimManager.cancelAnimation(this, mbackMotion);
			//			detachAnimator(mbackMotion);
			mbackMotion = null;
			mIsEnlarged = false;
		}
	}

	@Override
	public String getTitle() {
		if (mInfo != null && mInfo.getAppItemInfo() != null) {
			return mInfo.getAppItemInfo().mTitle;
		}
		return "Unknown App";

	}

	@Override
	public void setAlpha(int alpha) {
		if (mAlpha == alpha) {
			return;
		}
		mAlpha = alpha;
		if (mAppPic != null) {
			mAppPic.setAlpha(alpha);
		}

		//		if (mEditPic != null) {
		//			if (alpha == 255) {
		//				mEditPic.setAlpha(alpha);
		//				if (mEditLightPic != null) {
		//					mEditLightPic.setAlpha(alpha);
		//				}
		//				if (mColseAppDrawable != null && mEditPic != null
		//						&& mEditPic.getBitmap() != mColseAppDrawable.getBitmap()) {
		//					mEditPic.setDrawable(mColseAppDrawable);
		//				}
		//				if (mColseHighlightAppDrawable != null && mEditLightPic != null
		//						&& mEditLightPic.getBitmap() != mColseHighlightAppDrawable.getBitmap()) {
		//					mEditLightPic.setDrawable(mColseHighlightAppDrawable);
		//				}
		//			} else {
		//				BitmapDrawable temp = AppFuncUtils.getInstance(mActivity).getColseIconCopy();
		//				if (mEditPic != null && temp != null && mEditPic.getBitmap() != temp.getBitmap()) {
		//					mEditPic.setDrawable(temp);
		//				}
		//
		//				temp = AppFuncUtils.getInstance(mActivity).getColseLightIconCopy();
		//				if (mEditLightPic != null && temp != null
		//						&& mEditLightPic.getBitmap() != temp.getBitmap()) {
		//					mEditLightPic.setDrawable(temp);
		//				}
		//				if (mEditPic != null) {
		//					mEditPic.setAlpha(alpha);
		//				}
		//				if (mEditLightPic != null) {
		//					mEditLightPic.setAlpha(alpha);
		//				}
		//			}
		//		}
		if (mEditPic != null) {
			mEditPic.setAlpha(alpha);
		}
		if (mEditLightPic != null) {
			mEditLightPic.setAlpha(alpha);
		}
		if (mAppText != null) {
			if (isChildrenDrawnWithCacheEnabled() && mAppText.isDrawingCacheEnabled()) {
				if (!GoLauncher.getCustomTitleColor()) {
					mAppText.setPaintAlpha(255);
				}
				mAppText.setDrawingCacheAlpha(alpha);
			} else if (!GoLauncher.getCustomTitleColor()) {
				mAppText.setPaintAlpha(alpha);
			}
		}

	}

	/**
	 * 加载主题资源
	 */
	private void loadResource() {
		loadEditPicResource();
		loadLockPicResource();
	}

	private void loadEditPicResource() {
		BitmapDrawable closeAppDrawable = (BitmapDrawable) mThemeController
				.getDrawable(mThemeController.getThemeBean().mAppIconBean.mKillApp);
		BitmapDrawable closeHighlightAppDrawable = (BitmapDrawable) mThemeController
				.getDrawable(mThemeController.getThemeBean().mAppIconBean.mKillAppLight);
		setEditPic(closeAppDrawable);
		setEditLightPic(closeHighlightAppDrawable);
	}

	private void loadLockPicResource() {
		BitmapDrawable lockDrawable = (BitmapDrawable) mThemeController
				.getDrawable(mThemeController.getThemeBean().mAppIconBean.mLockApp);
		setLockPic(lockDrawable);
	}

	@Override
	public BaseAppIcon clone() {
		return new ProManageIcon(this.mActivity, /*this.proManageEdithome,*/this.mTickCount,
				this.mX, this.mY, this.mWidth, this.mHeight, this.mInfo, new BitmapDrawable(
						this.mLockPic.getBitmap()), new BitmapDrawable(this.mEditPic.getBitmap()),
				new BitmapDrawable(this.mEditLightPic.getBitmap()), this.mIsDrawText);
	}

	@Override
	public AnimationInfo changeAlpha(int sourceAlpha, int targetAlpha, int totalStep,
			IAnimateListener listener, OrientationInvoker orientationInvoker, boolean batchMode) {
		if (sourceAlpha < 0) {
			sourceAlpha = 0;
		} else if (sourceAlpha > 255) {
			sourceAlpha = 255;
		}
		setAlpha(sourceAlpha);
		if (mAlphaMotion != null) {
			mAnimManager.cancelAnimation(this, mAlphaMotion);
			//			detachAnimator(mAlphaMotion);
			mAlphaMotion = null;
		}
		mAlphaMotion = new XALinear(1, XALinear.XALINEAR_ECSPEED, sourceAlpha, sourceAlpha,
				targetAlpha, targetAlpha, totalStep, 1, 1);
		// if (listener != null) {
		// mAlphaMotion.setAnimateListener(listener);
		// }
		// attachAnimator(mAlphaMotion);
		AnimationInfo animInfo = new AnimationInfo(AnimationInfo.TYPE_SIMPLE, this, mAlphaMotion,
				listener);
		if (!batchMode) {
			mAnimManager.attachAnimation(animInfo, orientationInvoker);
		}
		return animInfo;
	}

	/**
	 * 正在运行图标信息
	 * @author yangguanxiang
	 *
	 */
	public class ProManageIconInfo {
		public Rect mRect;
		public FunTaskItemInfo mTaskInfo;

		public ProManageIconInfo(Rect rect, FunTaskItemInfo taskInfo) {
			mRect = rect;
			mTaskInfo = taskInfo;
		}
	}

	@Override
	public void notify(int key, Object obj) {
		// TODO Auto-generated method stub
		super.notify(key, obj);
		switch (key) {
			case AppFuncConstants.LOADTHEMERES :
				loadResource();
				break;

			default :
				break;
		}
	}

	@Override
	public void keepCurrentOrientation() {
		// do nothing
	}

	@Override
	public void resetOrientation() {
		// do nothing
	}

}
