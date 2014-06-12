package com.gau.go.launcherex.theme.cover;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import com.gau.go.launcherex.theme.cover.CoverBean.ClickAction;
import com.gau.go.launcherex.theme.cover.CoverBean.DragAction;
import com.gau.go.launcherex.theme.cover.CoverBean.MoveBean;
import com.gau.go.launcherex.theme.cover.CoverBean.RandomBean;
import com.gau.go.launcherex.theme.cover.CoverBean.ShakeAction;
import com.gau.go.launcherex.theme.cover.CoverBean.SlideBean;
import com.gau.go.launcherex.theme.cover.CoverBean.SpriteAction;
import com.gau.go.launcherex.theme.cover.CoverBean.SpriteBean;
import com.gau.go.launcherex.theme.cover.CoverBean.TouchBean;
import com.gau.go.launcherex.theme.cover.sensor.AccelerometerDataState;
import com.gau.go.launcherex.theme.cover.ui.AutoRandom;
import com.gau.go.launcherex.theme.cover.ui.Balloon;
import com.gau.go.launcherex.theme.cover.ui.ClockSpirit;
import com.gau.go.launcherex.theme.cover.ui.IActionCallback;
import com.gau.go.launcherex.theme.cover.ui.ICleanable;
import com.gau.go.launcherex.theme.cover.ui.IDrawable;
import com.gau.go.launcherex.theme.cover.ui.IMovable;
import com.gau.go.launcherex.theme.cover.ui.LongTouch;
import com.gau.go.launcherex.theme.cover.ui.Move;
import com.gau.go.launcherex.theme.cover.ui.Normal;
import com.gau.go.launcherex.theme.cover.ui.OnResponTouchListener;
import com.gau.go.launcherex.theme.cover.ui.Rotate;
import com.gau.go.launcherex.theme.cover.ui.Sleigh;
import com.gau.go.launcherex.theme.cover.ui.Slide;
import com.gau.go.launcherex.theme.cover.ui.Spirit;
import com.gau.go.launcherex.theme.cover.ui.Touch;
import com.gau.go.launcherex.theme.cover.ui.Translate;

/**
 * 显示景物的控制器
 * 
 * @author jiangxuwen
 * 
 */
public class ViewControl implements IDrawable, OnResponTouchListener, IMovable,
		ICleanable, OnCreateAtomListener, IActionCallback {

	public static final float GRAVITY_MAX_VALUE = 10.0f; // 重力感应的最大值
	public static final float GRAVITY_MOVE_VALUE_FOR_ROTATE = 0.5f; // 旋转景物开始响应重力感应的临界值
	public static final float GRAVITY_MOVE_VALUE_FOR_NORMAL = 1.5f; // 静态景物开始响应重力感应的临界值
	private static final byte[] AUTO_RANDOM_MUTEX = new byte[0];
	private static final byte[] MOVE_MUTEX = new byte[0];
	private static final byte[] TOUCH_MUTEX = new byte[0];
	private static final int VIEWCONFIG_MOVE_FACTOR = 2;
	private static final int MAX_AUTO_RANDOM_COUNT = 500;
	private static final int MAX_AUTO_MOVE_COUNT = 40;

	private List<MoveBean> mMoveTemplates;
	private int mMoveBeanLength;
	private List<Move> mMoveMap;
	private List<Move> mInvisibleMoveMap;
	private List<AutoRandom> mRandomMap;
	private List<AutoRandom> mInvisibleRandomsMap;
	private List<Bitmap[]> mMoveBitmapsMap;
	private static final int RANDOM_SHOW_TYPE_BYTIME = 2;
	private static final int RANDOM_SHOW_TYPE_BYSHAKE = 1;
	// private static final int DEFAULT_RANDOM_BYTIME_INTERVAL = 1000 * 20;
	private long mRandomBytimeInterval = 2000;
	// private static final int DEFAULT_RANDOM_SHAKESPEED = 40 * 3;
	private long mLastUpdateRandomByTime;
	private List<RandomBean> mRandomTimeTemplates;
	private List<RandomBean> mRandomShakeTemplates;
	private RandomBean mNextRandomBean;
	private Bitmap[] mNextRandomBeanBitmaps;
	private List<Bitmap[]> mRandomTimeBitmaps;
	private List<Bitmap[]> mRandomShakeBitmaps;
	private Bitmap[] mTouchBitmaps;
	private TouchBean mTouchBean;
	private int mMoveRandomIndex;
	private List<Spirit> mSpiritMap;
	private List<Normal> mNormalMap;
	private List<Slide> mSlideMap;
	private List<Rotate> mRotateMap;
	private List<Translate> mTranslateMap;
	private List<ClockSpirit> mClockMap;
	private List<Balloon> mBalloonMap;
	private List<LongTouch> mLongTouchsMap;
	private List<LongTouch> mInvisibleLongTouchsMap;
	private LongTouch mShowingLongTouch; // 刚创建，正在展示的
	private List<Touch> mTouchMap;
	private List<Touch> mInvisibleTouchsMap;
	private LongTouch mLongTouchTemplate;
	private int mMaxLongTouchCount;
	private boolean mBroken = true;
	private int mViewWidth;
	private int mViewHeight;
	private Context mContext;

	private volatile int mTouchState = TOUCH_STATE_NORMAL;
	private static final int TOUCH_STATE_NORMAL = 0;
	private static final int TOUCH_STATE_CLICK = 1;
	private static final int TOUCH_STATE_MOVE = 2;
	private static final int TOUCH_STATE_LONGCLICK = 3;
	private static final int LONG_CLICK_SLOP = 350;
	private static final int RANDOM_UNIT = 40;
	private static final float GRAVITYX_CHANGE_SLOP = 0.4f;
	private Random mRandom = new Random();
	private PointF mLastDownPosition = new PointF();
	private volatile long mLastDownTime = 0;
	private boolean mIsActionDownInvoke;
	private boolean mIsActionMoveInvoke;
	private float mLastGravityX;
	private float mLastGravityY;
	private float mGravityDiffX;
	private float mGravityDiffY;
	private long mLastGravityUpdateTime;
	private volatile boolean mIsGravityAnimating;
	private static final int GRAVITY_UPDATE_INTERVAL = 1000;

	private Sleigh mSleigh;

	private CoverBitmapLoader mBitmapLoader;
	private float[] mLastMovePoint = new float[] { -5, -5 };

	public ViewControl(Context context, int viewWidth, int viewHeight) {
		mViewHeight = viewHeight;
		mViewWidth = viewWidth;
		mMoveTemplates = new ArrayList<CoverBean.MoveBean>();
		mMoveMap = new ArrayList<Move>();
		mInvisibleMoveMap = new ArrayList<Move>();
		mRandomMap = new ArrayList<AutoRandom>();
		mNormalMap = new ArrayList<Normal>();
		mSlideMap = new ArrayList<Slide>();
//		mClockMap = new ArrayList<ClockSpirit>();
//		mBalloonMap = new ArrayList<Balloon>();
		mRotateMap = new ArrayList<Rotate>();
		mTranslateMap = new ArrayList<Translate>();
		mSpiritMap = new ArrayList<Spirit>();
//		mLongTouchsMap = new ArrayList<LongTouch>();
//		mInvisibleLongTouchsMap = new ArrayList<LongTouch>();
		mTouchMap = new ArrayList<Touch>();
		mInvisibleTouchsMap = new ArrayList<Touch>();
		mInvisibleRandomsMap = new ArrayList<AutoRandom>();
		mMoveBitmapsMap = new ArrayList<Bitmap[]>();
		mRandomTimeBitmaps = new ArrayList<Bitmap[]>();
		mRandomShakeBitmaps = new ArrayList<Bitmap[]>();
		mContext = context;
		mBitmapLoader = CoverBitmapLoader.getLoader(mContext);
	}

	public void initMaps(CoverBean coverBean) {
		if (coverBean == null) {
			mBroken = true;
			return;
		}
		initNormals(coverBean);
		initSlide(coverBean);
//		initClockSpirit(coverBean);
		initSpirit(coverBean);
//		initLongTouches(coverBean);
		initTouchMap(coverBean);
		initRandomMap(coverBean);
		initMoveMap(coverBean);
		initRotateMap(coverBean);
//		initBalloon(coverBean);
		initTranslateMap(coverBean);
		initSleigh(coverBean);
	}

	/**
	 * 罩子层恢复的时候调用
	 * 
	 * @param coverBean
	 */
	public void onResume(CoverBean coverBean) {
		registerAll();
		prepareMaps(coverBean);
	}

	/**
	 * <br>
	 * 功能简述:恢复beanMap里的Bitmap <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param coverBean
	 */
	private void prepareMaps(CoverBean coverBean) {
		if (coverBean.mRotateMap != null && coverBean.mRotateMap.size() > 0) {
			int size = coverBean.mRotateMap.size();
			for (int i = 0; i < size; i++) {
				mRotateMap.get(i).mBitmap = mBitmapLoader
						.getCoverBitmap(coverBean.mRotateMap.get(i).mImageName);
			}
		}
		if (coverBean.mTranslateMap != null
				&& coverBean.mTranslateMap.size() > 0) {
			int size = coverBean.mTranslateMap.size();
			for (int i = 0; i < size; i++) {
				mTranslateMap.get(i).mBitmap = mBitmapLoader
						.getCoverBitmap(coverBean.mTranslateMap.get(i).mImageName);
			}
		}
		initMoveMap(coverBean);
		initRandomMap(coverBean);
		initTouchMap(coverBean);
		if (coverBean.mLongTounchMap != null
				&& coverBean.mLongTounchMap.size() > 0) {
			for (CoverBean.LongTounchBean bean : coverBean.mLongTounchMap) {
				int size = bean.mBitmapFrameTop.size();
				Bitmap[] bitmaps1 = new Bitmap[size];
				for (int i = 0; i < size; i++) {
					bitmaps1[i] = mBitmapLoader
							.getCoverBitmap(bean.mBitmapFrameTop.get(i).mBitmapName);
				}
				size = bean.mBitmapFrameBottom.size();
				Bitmap[] bitmaps2 = new Bitmap[size];
				for (int i = 0; i < size; i++) {
					bitmaps2[i] = mBitmapLoader
							.getCoverBitmap(bean.mBitmapFrameBottom.get(i).mBitmapName);
				}
				Bitmap shadow = mBitmapLoader
						.getCoverBitmap(bean.mShadow.mImageName);
				mLongTouchTemplate.mTopBitmaps = bitmaps1;
				mLongTouchTemplate.mBottomBitmaps = bitmaps2;
				mLongTouchTemplate.mShadow = shadow;
				break;
			}
		}
		if (coverBean.mNormalMap != null && coverBean.mNormalMap.size() > 0) {
			int size = coverBean.mNormalMap.size();
			for (int i = 0; i < size; i++) {
				mNormalMap.get(i).mBitmap = mBitmapLoader
						.getCoverBitmap(coverBean.mNormalMap.get(i).mImageName);
			}
		}
		prepareSlideMap(coverBean);
		prepareSpirit(coverBean);
		prepareRotateMap(coverBean);
		initSleigh(coverBean);
	}

	private void prepareSlideMap(CoverBean coverBean) {
		if (coverBean.mSlideMap != null && coverBean.mSlideMap.size() > 0) {
			int count = Math.min(coverBean.mSlideMap.size(), mSlideMap.size());
			SlideBean bean;
			Slide slide;
			for (int i = 0; i < count; i++) {
				bean = coverBean.mSlideMap.get(i);
				slide = mSlideMap.get(i);
				if (bean == null || slide == null) {
					return;
				}
				List<String> imageNames = bean.mImageNames;
				int size = imageNames.size();
				Bitmap[] bitmaps = new Bitmap[size];
				for (int j = 0; j < size; j++) {
					bitmaps[j] = mBitmapLoader
							.getCoverBitmap(imageNames.get(j));
				}
				slide.mBitmaps = bitmaps;
				slide.mBitmap = mBitmapLoader.getCoverBitmap(bean.mImgName);
				prepareSlideShakeAction(bean.mShakeAction, slide);
			}
		}
	}

	private void prepareSpirit(CoverBean coverBean) {
		if (mSpiritMap != null && mSpiritMap.size() > 0
				&& coverBean.mSpriteMap != null
				&& coverBean.mSpriteMap.size() > 0) {
			int length = coverBean.mSpriteMap.size();
			SpriteBean bean;
			Spirit spirit;
			for (int j = 0; j < length; j++) {
				bean = coverBean.mSpriteMap.get(j);
				int size = bean.mSpriteAction1.mActionImageNames.size();
				Bitmap[] bitmaps1 = new Bitmap[size];
				for (int i = 0; i < size; i++) {
					bitmaps1[i] = mBitmapLoader
							.getCoverBitmap(bean.mSpriteAction1.mActionImageNames
									.get(i));
				}
				if (bean.mSpriteAction2.mShadow == null) {
					bean.mSpriteAction2 = bean.mSpriteAction1;
				}
				size = bean.mSpriteAction2.mActionImageNames.size();
				Bitmap[] bitmaps2 = new Bitmap[size];
				for (int i = 0; i < size; i++) {
					bitmaps2[i] = mBitmapLoader
							.getCoverBitmap(bean.mSpriteAction2.mActionImageNames
									.get(i));
				}
				if (bitmaps2.length == 0) {
					bitmaps2 = bitmaps1;
				}
				Bitmap defaultAction1 = mBitmapLoader
						.getCoverBitmap(bean.mSpriteAction1.mNormalImage);
				Bitmap defaultAction2 = mBitmapLoader
						.getCoverBitmap(bean.mSpriteAction2.mNormalImage);
				Bitmap action1ShadowBitmap = mBitmapLoader
						.getCoverBitmap(bean.mSpriteAction1.mShadow.mImageName);
				Bitmap action2ShadowBitmap = mBitmapLoader
						.getCoverBitmap(bean.mSpriteAction2.mShadow.mImageName);
				spirit = mSpiritMap.get(j);
				spirit.prepare(bitmaps1, bitmaps2, action1ShadowBitmap,
						action2ShadowBitmap, defaultAction1, defaultAction2);
				prepareAdditionActionOne(bean.mSpriteAction1.mAdditionalAction,
						spirit);
				prepareAdditionActionTwo(bean.mSpriteAction2.mAdditionalAction,
						spirit);
				prepareSpiritDragAction(bean.mDragAction, spirit);
				prepareSpiritShakeAction(bean.mShakeAction, spirit);

			}
		}
	}

	public void prepareAdditionActionTwo(SpriteAction additionalAction,
			Spirit spirit) {
		if (additionalAction != null) {
			int add = additionalAction.mActionImageNames.size();
			Bitmap[] addBitmaps = new Bitmap[add];
			for (int i = 0; i < add; i++) {
				addBitmaps[i] = mBitmapLoader
						.getCoverBitmap(additionalAction.mActionImageNames
								.get(i));
			}
			Bitmap additionalShadow = mBitmapLoader
					.getCoverBitmap(additionalAction.mShadow.mImageName);
			spirit.prepareAdditionalActionTwo(addBitmaps, additionalShadow,
					null, null, null, null, null);
		}
	}

	public void prepareAdditionActionOne(SpriteAction additionalAction,
			Spirit spirit) {
		if (additionalAction != null) {
			int add = additionalAction.mActionImageNames.size();
			Bitmap[] addBitmaps = new Bitmap[add];
			for (int i = 0; i < add; i++) {
				addBitmaps[i] = mBitmapLoader
						.getCoverBitmap(additionalAction.mActionImageNames
								.get(i));
			}
			Bitmap additionalShadow = mBitmapLoader
					.getCoverBitmap(additionalAction.mShadow.mImageName);
			spirit.prepareAdditionalActionOne(addBitmaps, additionalShadow,
					null, null, null, null, null);
		}
	}

	private void initRotateMap(CoverBean coverBean) {
		if (coverBean.mRotateMap != null && coverBean.mRotateMap.size() > 0) {
			for (CoverBean.RotateBean bean : coverBean.mRotateMap) {
				Bitmap imageBitmap = mBitmapLoader
						.getCoverBitmap(bean.mImageName);
				Bitmap bodyBitmap = mBitmapLoader
						.getCoverBitmap(bean.mBodyImgName);
				int ropeHeight = DrawUtils.percentY2px(bean.mRopeHeight);
				boolean allowDrag = bean.mAllowDrag;
				boolean createShow = bean.mStartLocation.mCreateShow;
				int startX = DrawUtils.percentX2px(bean.mStartLocation.mStartX);
				int startY = DrawUtils.percentY2px(bean.mStartLocation.mStartY);
				Bitmap shadow = mBitmapLoader
						.getCoverBitmap(bean.mShadow.mImageName);
				boolean needShadow = bean.mShadow.mNeedShadow;
				int nextToX = bean.mShadow.mNextToX;
				int nextToY = bean.mShadow.mNextToY;
				boolean limit = bean.mLimitArea.mLimit;
				Rect limitRect = new Rect();
				percentRect2px(limitRect, bean.mLimitArea);
				int landType = bean.mRelativeLocation.mLandscapeType;
				int portType = bean.mRelativeLocation.mVerticalType;

				int offsetX = DrawUtils.isPort() ? startX : DrawUtils
						.percentY2px(bean.mStartLocation.mStartX);
				int offsetY = DrawUtils.isPort() ? startY : DrawUtils
						.percentX2px(bean.mStartLocation.mStartY);

				if (landType == CoverBean.LAND_RIGHT) {
					startX = limitRect.right - imageBitmap.getWidth() + offsetX;
					limitRect.left = DrawUtils.getScreenViewWidth()
							- imageBitmap.getWidth();
				}
				if (portType == CoverBean.PORT_BOTTOM) {
					startY = limitRect.bottom - imageBitmap.getHeight()
							+ offsetY;
					limitRect.top = DrawUtils.getScreenViewHeight()
							- imageBitmap.getHeight();
				}

				long duration = bean.mRotateAnim.mDuration;
				boolean loop = bean.mRotateAnim.mLoop;
				float perAngle = bean.mRotateAnim.mToDegress
						/ (GRAVITY_MAX_VALUE - GRAVITY_MOVE_VALUE_FOR_ROTATE);
				float fromAngle = bean.mRotateAnim.mFromDegress;
				float pivoX = bean.mRotateAnim.mPivotX;
				float pivoY = bean.mRotateAnim.mPivotY;
				Rotate rotate = new Rotate(mContext, imageBitmap, bodyBitmap,
						ropeHeight, allowDrag, createShow, startX, startY,
						shadow, needShadow, nextToX, nextToY, limit, limitRect,
						duration, loop, perAngle, fromAngle, pivoX, pivoY);

				if (bean.mClickActionList != null
						&& bean.mClickActionList.size() > 0) {
					int size = bean.mClickActionList.size();
					for (int i = 0; i < size; i++) {
						setClickAction(i, rotate, bean.mClickActionList.get(i));
					}
				}

				setDragAction(rotate, bean.mDragAction);
				mRotateMap.add(rotate);
			}
		}
	}


	private void prepareRotateMap(CoverBean coverBean) {
		if (coverBean.mRotateMap != null && coverBean.mRotateMap.size() > 0) {
			int size = coverBean.mRotateMap.size();
			Rotate rotate;
			for (int i = 0; i < size; i++) {
				CoverBean.RotateBean rotateBean = coverBean.mRotateMap.get(i);
				rotate = mRotateMap.get(i);
				rotate.mRopeBitmap = mBitmapLoader
						.getCoverBitmap(rotateBean.mImageName);
				rotate.mBodyBitmap = mBitmapLoader
						.getCoverBitmap(rotateBean.mBodyImgName);
				prepareClickAction(rotateBean.mClickActionList, rotate);
				prepareDragAction(rotateBean.mDragAction, rotate);
			}

		}
	}

	private void prepareClickAction(List<ClickAction> actionList, Rotate rotate) {
		for (int i = 0; i < actionList.size(); i++) {
			ClickAction clickAction = actionList.get(i);
			if (clickAction != null) {
				int add = clickAction.mActionImageNames.size();
				Bitmap[] addBitmaps = new Bitmap[add];
				for (int j = 0; j < add; j++) {
					addBitmaps[j] = mBitmapLoader
							.getCoverBitmap(clickAction.mActionImageNames
									.get(j));
				}
				Bitmap additionalShadow = mBitmapLoader
						.getCoverBitmap(clickAction.mShadow.mImageName);
				rotate.prepareClickAction(i, addBitmaps, additionalShadow,
						null, null, null, null, null);
			}
		}
	}

	private void prepareDragAction(DragAction action, Rotate rotate) {
		if (action != null) {
			int add = action.mActionImageNames.size();
			Bitmap[] addBitmaps = new Bitmap[add];
			for (int j = 0; j < add; j++) {
				addBitmaps[j] = mBitmapLoader
						.getCoverBitmap(action.mActionImageNames.get(j));
			}
			Bitmap additionalShadow = mBitmapLoader
					.getCoverBitmap(action.mShadow.mImageName);
			rotate.prepareDragAction(addBitmaps, additionalShadow, null, null,
					null, null, null);
		}
	}

	private void prepareSpiritDragAction(DragAction action, Spirit spirit) {
		if (action != null) {
			int add = action.mActionImageNames.size();
			Bitmap[] addBitmaps = new Bitmap[add];
			for (int j = 0; j < add; j++) {
				addBitmaps[j] = mBitmapLoader
						.getCoverBitmap(action.mActionImageNames.get(j));
			}
			Bitmap additionalShadow = mBitmapLoader
					.getCoverBitmap(action.mShadow.mImageName);
			spirit.prepareDragAction(addBitmaps, additionalShadow, null, null,
					null, null, null);
		}
	}

	private void prepareSpiritShakeAction(ShakeAction action, Spirit spirit) {
		if (action != null) {
			int add = action.mActionImageNames.size();
			Bitmap[] addBitmaps = new Bitmap[add];
			for (int j = 0; j < add; j++) {
				addBitmaps[j] = mBitmapLoader
						.getCoverBitmap(action.mActionImageNames.get(j));
			}
			Bitmap additionalShadow = mBitmapLoader
					.getCoverBitmap(action.mShadow.mImageName);
			spirit.prepareShakeAction(addBitmaps, additionalShadow, null, null,
					null, null, null);
		}
	}

	private void prepareSlideShakeAction(ShakeAction action, Slide slide) {
		if (action != null) {
			int add = action.mActionImageNames.size();
			Bitmap[] addBitmaps = new Bitmap[add];
			for (int j = 0; j < add; j++) {
				addBitmaps[j] = mBitmapLoader
						.getCoverBitmap(action.mActionImageNames.get(j));
			}
			Bitmap additionalShadow = mBitmapLoader
					.getCoverBitmap(action.mShadow.mImageName);
			slide.prepareShakeAction(addBitmaps, additionalShadow, null, null,
					null, null, null);
		}
	}

	private void setClickAction(int actionIndex, Rotate rotate,
			ClickAction clickAction) {
		if (clickAction != null) {
			int size = clickAction.mActionImageNames.size();
			Bitmap[] addBitmaps = new Bitmap[size];
			for (int i = 0; i < size; i++) {
				addBitmaps[i] = mBitmapLoader
						.getCoverBitmap(clickAction.mActionImageNames.get(i));
			}
			Bitmap additionalShadow = mBitmapLoader
					.getCoverBitmap(clickAction.mShadow.mImageName);
			rotate.setClickAction(actionIndex, clickAction.mActionType,
					(int) clickAction.mDelayTime, clickAction.mEndType,
					addBitmaps, additionalShadow, clickAction.mNeedLoop,
					clickAction.mAnimationTime, clickAction.mIsBitmapSymmetric);
		}
	}

	private void setDragAction(Rotate rotate, DragAction dragAction) {
		if (dragAction != null) {
			int size = dragAction.mActionImageNames.size();
			Bitmap[] addBitmaps = new Bitmap[size];
			for (int i = 0; i < size; i++) {
				addBitmaps[i] = mBitmapLoader
						.getCoverBitmap(dragAction.mActionImageNames.get(i));
			}
			Bitmap additionalShadow = mBitmapLoader
					.getCoverBitmap(dragAction.mShadow.mImageName);
			rotate.setDragAction(dragAction.mActionType,
					(int) dragAction.mDelayTime, dragAction.mEndType,
					addBitmaps, additionalShadow, dragAction.mNeedLoop,
					dragAction.mAnimationTime, dragAction.mIsBitmapSymmetric);
		}
	}

	private void setDragAction(Spirit spirit, DragAction dragAction) {
		if (dragAction != null) {
			int size = dragAction.mActionImageNames.size();
			Bitmap[] addBitmaps = new Bitmap[size];
			for (int i = 0; i < size; i++) {
				addBitmaps[i] = mBitmapLoader
						.getCoverBitmap(dragAction.mActionImageNames.get(i));
			}
			Bitmap additionalShadow = mBitmapLoader
					.getCoverBitmap(dragAction.mShadow.mImageName);
			spirit.setDragAction(dragAction.mActionType,
					(int) dragAction.mDelayTime, dragAction.mEndType,
					addBitmaps, additionalShadow, dragAction.mNeedLoop,
					dragAction.mAnimationTime, dragAction.mIsBitmapSymmetric);
		}
	}

	private void setShakeAction(Spirit spirit, ShakeAction shakeAction) {
		if (shakeAction != null) {
			int size = shakeAction.mActionImageNames.size();
			Bitmap[] addBitmaps = new Bitmap[size];
			for (int i = 0; i < size; i++) {
				addBitmaps[i] = mBitmapLoader
						.getCoverBitmap(shakeAction.mActionImageNames.get(i));
			}
			Bitmap additionalShadow = mBitmapLoader
					.getCoverBitmap(shakeAction.mShadow.mImageName);
			spirit.setShakeAction(shakeAction.mActionType,
					(int) shakeAction.mDelayTime, shakeAction.mEndType,
					addBitmaps, additionalShadow, shakeAction.mNeedLoop,
					shakeAction.mAnimationTime, shakeAction.mIsBitmapSymmetric,
					shakeAction.mShakeSpeed);
		}
	}

	private void setShakeAction(Slide slide, ShakeAction shakeAction) {
		if (shakeAction != null) {
			int size = shakeAction.mActionImageNames.size();
			Bitmap[] addBitmaps = new Bitmap[size];
			for (int i = 0; i < size; i++) {
				addBitmaps[i] = mBitmapLoader
						.getCoverBitmap(shakeAction.mActionImageNames.get(i));
			}
			Bitmap additionalShadow = mBitmapLoader
					.getCoverBitmap(shakeAction.mShadow.mImageName);
			slide.setShakeAction(shakeAction.mActionType,
					(int) shakeAction.mDelayTime, shakeAction.mEndType,
					addBitmaps, additionalShadow, shakeAction.mNeedLoop,
					shakeAction.mAnimationTime, shakeAction.mIsBitmapSymmetric,
					shakeAction.mShakeSpeed);
		}
	}

	private void initSleigh(CoverBean coverBean) {

		if (coverBean.mSleighBean != null) {

			int size = coverBean.mSleighBean.mDeerBean.mImageNames.size();
			Bitmap[] deerBitmaps = new Bitmap[size];
			for (int i = 0; i < size; i++) {
				deerBitmaps[i] = mBitmapLoader
						.getCoverBitmap(coverBean.mSleighBean.mDeerBean.mImageNames
								.get(i));
			}
			size = coverBean.mSleighBean.mSantaBean.mImageNames.size();
			Bitmap[] santaBitmaps = new Bitmap[size];
			for (int i = 0; i < size; i++) {
				santaBitmaps[i] = mBitmapLoader
						.getCoverBitmap(coverBean.mSleighBean.mSantaBean.mImageNames
								.get(i));
			}

			size = coverBean.mSleighBean.mWhipBean.mImageNames.size();
			Bitmap[] whipBitmaps = new Bitmap[size];
			for (int i = 0; i < size; i++) {
				whipBitmaps[i] = mBitmapLoader
						.getCoverBitmap(coverBean.mSleighBean.mWhipBean.mImageNames
								.get(i));
			}

			size = coverBean.mSleighBean.mFlagBean.mLTRImageNames.size();
			Bitmap[] flagFanBitmaps = new Bitmap[size];
			for (int i = 0; i < size; i++) {
				flagFanBitmaps[i] = mBitmapLoader
						.getCoverBitmap(coverBean.mSleighBean.mFlagBean.mLTRImageNames
								.get(i));
			}

			size = coverBean.mSleighBean.mFlagBean.mRTLImageNames.size();
			Bitmap[] flagBitmaps = new Bitmap[size];
			for (int i = 0; i < size; i++) {
				flagBitmaps[i] = mBitmapLoader
						.getCoverBitmap(coverBean.mSleighBean.mFlagBean.mRTLImageNames
								.get(i));
			}

			size = coverBean.mSleighBean.mGiftBean.mImageNames.size();
			Bitmap[] giftBitmaps = new Bitmap[size];
			for (int i = 0; i < size; i++) {
				giftBitmaps[i] = mBitmapLoader
						.getCoverBitmap(coverBean.mSleighBean.mGiftBean.mImageNames
								.get(i));
			}

			size = coverBean.mSleighBean.mMovePointBean.mImageNames.size();
			Bitmap[] pointBitmaps = new Bitmap[size];
			for (int i = 0; i < size; i++) {
				pointBitmaps[i] = mBitmapLoader
						.getCoverBitmap(coverBean.mSleighBean.mMovePointBean.mImageNames
								.get(i));
			}

			size = coverBean.mSleighBean.mRabbitBean.mJumpImageNames.size();
			Bitmap[] jumpBitmaps = new Bitmap[size];
			for (int i = 0; i < size; i++) {
				jumpBitmaps[i] = mBitmapLoader
						.getCoverBitmap(coverBean.mSleighBean.mRabbitBean.mJumpImageNames
								.get(i));
			}

			size = coverBean.mSleighBean.mRabbitBean.mWaveImageNames.size();
			Bitmap[] waveBitmaps = new Bitmap[size];
			for (int i = 0; i < size; i++) {
				waveBitmaps[i] = mBitmapLoader
						.getCoverBitmap(coverBean.mSleighBean.mRabbitBean.mWaveImageNames
								.get(i));
			}

			size = coverBean.mSleighBean.mRabbitBean.mDismissImageNames.size();
			Bitmap[] dismissImageNames = new Bitmap[size];
			for (int i = 0; i < size; i++) {
				dismissImageNames[i] = mBitmapLoader
						.getCoverBitmap(coverBean.mSleighBean.mRabbitBean.mDismissImageNames
								.get(i));
			}

			size = coverBean.mSleighBean.mFallSantaBean.mImageNames.size();
			Bitmap[] fallSantaImageNames = new Bitmap[size];
			for (int i = 0; i < size; i++) {
				fallSantaImageNames[i] = mBitmapLoader
						.getCoverBitmap(coverBean.mSleighBean.mFallSantaBean.mImageNames
								.get(i));
			}

			size = coverBean.mSleighBean.mUpSantaBean.mImageNames.size();
			Bitmap[] upSantaImageNames = new Bitmap[size];
			for (int i = 0; i < size; i++) {
				upSantaImageNames[i] = mBitmapLoader
						.getCoverBitmap(coverBean.mSleighBean.mUpSantaBean.mImageNames
								.get(i));
			}

			size = coverBean.mSleighBean.mRunSantaBean.mImageNames.size();
			Bitmap[] runSantaImageNames = new Bitmap[size];
			for (int i = 0; i < size; i++) {
				runSantaImageNames[i] = mBitmapLoader
						.getCoverBitmap(coverBean.mSleighBean.mRunSantaBean.mImageNames
								.get(i));
			}

			size = coverBean.mSleighBean.mEmptySleighBean.mImageNames.size();
			Bitmap[] emptySleighImageNames = new Bitmap[size];
			for (int i = 0; i < size; i++) {
				emptySleighImageNames[i] = mBitmapLoader
						.getCoverBitmap(coverBean.mSleighBean.mEmptySleighBean.mImageNames
								.get(i));
			}

			size = coverBean.mSleighBean.mPackageBean.mImageNames.size();
			Bitmap[] packageImageNames = new Bitmap[size];
			for (int i = 0; i < size; i++) {
				packageImageNames[i] = mBitmapLoader
						.getCoverBitmap(coverBean.mSleighBean.mPackageBean.mImageNames
								.get(i));
//				Log.e("chenqiang", "packageImageNames:"
//						+ coverBean.mSleighBean.mPackageBean.mImageNames.get(i));
			}

			mSleigh = new Sleigh(mBitmapLoader, mViewWidth, mViewHeight,
					deerBitmaps, santaBitmaps, whipBitmaps, flagBitmaps,
					flagFanBitmaps, giftBitmaps, waveBitmaps, jumpBitmaps,
					dismissImageNames, pointBitmaps, fallSantaImageNames,
					upSantaImageNames, runSantaImageNames,
					emptySleighImageNames, packageImageNames);
		}

	}

	private void initTranslateMap(CoverBean coverBean) {
		if (coverBean.mTranslateMap != null
				&& coverBean.mTranslateMap.size() > 0) {
			for (CoverBean.TranslateBean translateBean : coverBean.mTranslateMap) {
				Translate translate = new Translate();
				translate.mAllowDrag = translateBean.mAllowDrag;
				translate.mBitmap = mBitmapLoader
						.getCoverBitmap(translateBean.mImageName);
				translate.mX = translate.mDefaultX = DrawUtils
						.percentX2px(translateBean.mStartLocation.mStartX);
				translate.mY = translate.mDefaultY = DrawUtils
						.percentY2px(translateBean.mStartLocation.mStartY);
				translate.mWidth = translate.mBitmap.getWidth();
				translate.mHeight = translate.mBitmap.getHeight();
				translate.mScreenWidth = mViewWidth;
				translate.mScreenHeight = mViewHeight;
				percentRect2px(translate.mLimitRect, translateBean.mLimitArea);
				translate.mLoop = translateBean.mTranslateAnim.mLoop;
				translate.mBackRotate = translateBean.mTranslateAnim.mBackRotate;
				translate.mDuration = translateBean.mTranslateAnim.mDuration;
				translate.mSpeedX = (float) (mViewWidth + translate.mWidth)
						/ translateBean.mTranslateAnim.mDuration;
				translate.mSpeedY = 0;
				mTranslateMap.add(translate);
			} // end for
		} // end if
	}

	private void initMoveMap(CoverBean coverBean) {
		if (coverBean.mMoveBeanMap != null && coverBean.mMoveBeanMap.size() > 0) {
			mMoveBeanLength = coverBean.mMoveBeanMap.size();
			for (CoverBean.MoveBean moveBean : coverBean.mMoveBeanMap) {
				List<String> imageNames = moveBean.mImageNames;
				int size = imageNames.size();
				Bitmap[] bitmaps1 = new Bitmap[size];
				for (int i = 0; i < size; i++) {
					bitmaps1[i] = mBitmapLoader.getCoverBitmap(imageNames
							.get(i));
				}
				mMoveBitmapsMap.add(bitmaps1);
			}
			mMoveTemplates = coverBean.mMoveBeanMap;
			randomMove();
		}
	}

	private void initRandomMap(CoverBean coverBean) {
		if (coverBean.mRandomMap != null && coverBean.mRandomMap.size() > 0) {
			mRandomTimeTemplates = new ArrayList<CoverBean.RandomBean>();
			mRandomShakeTemplates = new ArrayList<CoverBean.RandomBean>();
			for (RandomBean bean : coverBean.mRandomMap) {
				List<String> imageNames = bean.mImageNames;
				int size = imageNames.size();
				Bitmap[] bitmaps1 = new Bitmap[size];
				for (int i = 0; i < size; i++) {
					bitmaps1[i] = mBitmapLoader.getCoverBitmap(imageNames
							.get(i));
				}
				if ((bean.mShowType & RANDOM_SHOW_TYPE_BYTIME) == RANDOM_SHOW_TYPE_BYTIME) {
					mRandomTimeBitmaps.add(bitmaps1);
					mRandomTimeTemplates.add(bean);
					mRandomBytimeInterval = bean.mInterval;
				}
				if ((bean.mShowType & RANDOM_SHOW_TYPE_BYSHAKE) == RANDOM_SHOW_TYPE_BYSHAKE) {
					mRandomShakeBitmaps.add(bitmaps1);
					mRandomShakeTemplates.add(bean);
				}
			}
		}
	}

	private void initLongTouches(CoverBean coverBean) {
		if (coverBean.mLongTounchMap != null
				&& coverBean.mLongTounchMap.size() > 0) {
			for (CoverBean.LongTounchBean bean : coverBean.mLongTounchMap) {
				int size = bean.mBitmapFrameTop.size();
				int[] topBitmapIntervals = new int[size];
				Bitmap[] bitmaps1 = new Bitmap[size];
				for (int i = 0; i < size; i++) {
					bitmaps1[i] = mBitmapLoader
							.getCoverBitmap(bean.mBitmapFrameTop.get(i).mBitmapName);
					topBitmapIntervals[i] = bean.mBitmapFrameTop.get(i).mDuration;
				}
				size = bean.mBitmapFrameBottom.size();
				Bitmap[] bitmaps2 = new Bitmap[size];
				int[] bottomBitmapIntervals = new int[size];
				for (int i = 0; i < size; i++) {
					bitmaps2[i] = mBitmapLoader
							.getCoverBitmap(bean.mBitmapFrameBottom.get(i).mBitmapName);
					bottomBitmapIntervals[i] = bean.mBitmapFrameBottom.get(i).mDuration;
				}
				Bitmap shadow = mBitmapLoader
						.getCoverBitmap(bean.mShadow.mImageName);
				Rect limt = new Rect();
				percentRect2px(limt, bean.mlLimitArea);
				LongTouch longTouch = new LongTouch(bitmaps1, bitmaps2, shadow,
						DrawUtils.percentX2px(bean.mStartLocation.mStartX),
						DrawUtils.percentY2px(bean.mStartLocation.mStartY),
						bean.mAllowDrag, bean.mShadow.mNextToX,
						bean.mShadow.mNextToX, limt, bean.mLife.mEndAge
								- bean.mLife.mStartAge, bean.mLoopCount,
						bean.mMissOnTouchUp, topBitmapIntervals,
						bottomBitmapIntervals,
						bean.mStartLocation.mLocationType,
						DrawUtils.percentX2px(bean.mStartLocation.mStartX),
						DrawUtils.percentY2px(bean.mStartLocation.mStartY),
						bean.mInAnimationType,
						DrawUtils.percentX2px(bean.mlLimitArea.mTouchOffsetX),
						DrawUtils.percentY2px(bean.mlLimitArea.mTouchOffsetY));
				if (mLongTouchTemplate == null) {
					mLongTouchTemplate = longTouch;
				}
				mMaxLongTouchCount = bean.mMaxCount;
				if (bean.mStartLocation.mCreateShow) {
					mLongTouchsMap.add(longTouch);
				}
			}
		}
	}

	private void initTouchMap(CoverBean coverBean) {
		if (coverBean.mTouchBean != null) {
			int size = coverBean.mTouchBean.mImageNames.size();
			Bitmap[] bitmaps = new Bitmap[size];
			for (int i = 0; i < size; i++) {
				bitmaps[i] = mBitmapLoader
						.getCoverBitmap(coverBean.mTouchBean.mImageNames.get(i));
			}
			mTouchBean = coverBean.createTouchBean();
			mTouchBean.mAnimationType = coverBean.mTouchBean.mAnimationType;
			mTouchBean.mLife = coverBean.mTouchBean.mLife;
			mTouchBitmaps = bitmaps;
		}
	}

	private void initNormals(CoverBean coverBean) {
		if (coverBean.mNormalMap != null && coverBean.mNormalMap.size() > 0) {
			for (CoverBean.NormalBean bean : coverBean.mNormalMap) {
				Normal normal = new Normal();
				normal.mAllowDrag = bean.mAllowDrag;
				normal.mBitmap = mBitmapLoader.getCoverBitmap(bean.mImageName);
				normal.mWidth = normal.mBitmap.getWidth();
				normal.mHeight = normal.mBitmap.getHeight();
				normal.mX = normal.mDefaultX = DrawUtils
						.percentX2px(bean.mStartLocation.mStartX);
				normal.mY = normal.mDefaultY = DrawUtils
						.percentY2px(bean.mStartLocation.mStartY);
				percentRect2px(normal.mLimitRect, bean.mLimitArea);
				final int landType = bean.mRelativeLocation.mLandscapeType;
				final int portType = bean.mRelativeLocation.mVerticalType;

				int offsetX = (int) (DrawUtils.isPort() ? normal.mX : DrawUtils
						.percentY2px(bean.mStartLocation.mStartX));
				int offsetY = (int) (DrawUtils.isPort() ? normal.mY : DrawUtils
						.percentX2px(bean.mStartLocation.mStartY));

				if (landType == CoverBean.LAND_RIGHT) {
					normal.mX = normal.mDefaultX = normal.mLimitRect.right
							- normal.mWidth + offsetX;
					normal.mLimitRect.left = DrawUtils.getScreenViewWidth()
							- normal.mWidth + offsetX;
				}
				if (portType == CoverBean.PORT_BOTTOM) {
					normal.mY = normal.mDefaultY = normal.mLimitRect.bottom
							- normal.mHeight + offsetY;
					normal.mLimitRect.top = DrawUtils.getScreenViewHeight()
							- normal.mHeight + offsetY;
				}
				normal.init(normal.mY, normal.mY - offsetY);
				mNormalMap.add(normal);
			}
		}
	}

	private void initSlide(CoverBean coverBean) {
		if (coverBean.mSlideMap != null && coverBean.mSlideMap.size() > 0) {
			for (CoverBean.SlideBean bean : coverBean.mSlideMap) {
				Slide slide = new Slide(this, mViewWidth, mViewHeight);
				// slide.mAllowDrag = bean.mAllowDrag;
				slide.mBitmap = mBitmapLoader.getCoverBitmap(bean.mImgName);
				int size = bean.mImageNames.size();
				Bitmap[] bitmaps = new Bitmap[size];
				for (int i = 0; i < size; i++) {
					bitmaps[i] = mBitmapLoader.getCoverBitmap(bean.mImageNames
							.get(i));
				}
				slide.mBitmaps = bitmaps;
				slide.mInterval = bean.mInterval;
				slide.mAnimationType = bean.mAnimationType;
				slide.mShakeSpeed = bean.mShakeSpeed;
				slide.mShadow = mBitmapLoader
						.getCoverBitmap(bean.mShadow.mImageName);
				slide.mX = DrawUtils.percentX2px(bean.mStartLocation.mStartX);
				slide.mY = DrawUtils.percentY2px(bean.mStartLocation.mStartY);
				slide.mLimitRect = new Rect();
				percentRect2px(slide.mLimitRect, bean.mLimitArea);
				final int landType = bean.mRelativeLocation.mLandscapeType;
				final int portType = bean.mRelativeLocation.mVerticalType;
				if (landType == CoverBean.LAND_RIGHT) {
					slide.mX = slide.mLimitRect.right
							- slide.mBitmap.getWidth();
					slide.mLimitRect.left = (int) slide.mX;
				}
				if (portType == CoverBean.PORT_BOTTOM) {
					slide.mY = slide.mLimitRect.bottom
							- slide.mBitmap.getHeight();
					slide.mLimitRect.top = (int) slide.mY;
				}

				slide.init();
				setShakeAction(slide, bean.mShakeAction);
				mSlideMap.add(slide);
			}
		}
	}

	private void percentRect2px(Rect dstRect, CoverBean.LimitArea limitArea) {
		if (dstRect != null && limitArea != null) {
			dstRect.left = DrawUtils.percentX2px(limitArea.mLeft);
			dstRect.right = DrawUtils.percentX2px(limitArea.mRight);
			dstRect.top = DrawUtils.percentY2px(limitArea.mTop);
			dstRect.bottom = DrawUtils.percentY2px(limitArea.mBottom);
		}
	}

	private void initSpirit(CoverBean coverBean) {
		if (coverBean.mSpriteMap != null && coverBean.mSpriteMap.size() > 0) {
			for (CoverBean.SpriteBean bean : coverBean.mSpriteMap) {
				int size = bean.mSpriteAction1.mActionImageNames.size();
				Bitmap[] bitmaps1 = new Bitmap[size];
				for (int i = 0; i < size; i++) {
					bitmaps1[i] = mBitmapLoader
							.getCoverBitmap(bean.mSpriteAction1.mActionImageNames
									.get(i));
				}
				size = bean.mSpriteAction2.mActionImageNames.size();
				Bitmap[] bitmaps2 = new Bitmap[size];
				for (int i = 0; i < size; i++) {
					bitmaps2[i] = mBitmapLoader
							.getCoverBitmap(bean.mSpriteAction2.mActionImageNames
									.get(i));
				}
				if (bitmaps2.length == 0) {
					bitmaps2 = bitmaps1;
				}
				Bitmap defaultAction1 = mBitmapLoader
						.getCoverBitmap(bean.mSpriteAction1.mNormalImage);
				Bitmap defaultAction2 = mBitmapLoader
						.getCoverBitmap(bean.mSpriteAction2.mNormalImage);
				Bitmap action1ShadowBitmap = mBitmapLoader
						.getCoverBitmap(bean.mSpriteAction1.mShadow.mImageName);
				Bitmap action2ShadowBitmap = mBitmapLoader
						.getCoverBitmap(bean.mSpriteAction2.mShadow.mImageName);
				Rect limit = new Rect();
				percentRect2px(limit, bean.mLimitArea);

				final int landType = bean.mRelativeLocation.mLandscapeType;
				final int portType = bean.mRelativeLocation.mVerticalType;
				int startX = DrawUtils.percentX2px(bean.mStartLocation.mStartX);
				int startY = DrawUtils.percentY2px(bean.mStartLocation.mStartY);
				int startLandX = DrawUtils
						.percentX2px(bean.mStartLocation.mStartLandX);
				int startLandY = DrawUtils
						.percentY2px(bean.mStartLocation.mStartLandY);
				if (landType == CoverBean.LAND_RIGHT && defaultAction1 != null) {
					startX = DrawUtils.getScreenViewWidth()
							- defaultAction1.getWidth();
				}
				if (portType == CoverBean.PORT_BOTTOM && defaultAction1 != null) {
					startY = DrawUtils.getScreenViewHeight()
							- defaultAction1.getHeight();
				}

				Spirit spirit = new Spirit(this, mContext, this, bitmaps1, bitmaps2,
						action1ShadowBitmap, action2ShadowBitmap,
						defaultAction1, defaultAction2,
						bean.mSpriteAction1.mShadow.mNextToX,
						bean.mSpriteAction1.mShadow.mNextToY,
						bean.mSpriteAction2.mEndType, limit, false,
						bean.mRotateXY.mPivotX, bean.mRotateXY.mPivotY,
						(int) bean.mSpriteAction1.mDelayTime,
						(int) bean.mSpriteAction2.mDelayTime, startX, startY,
						startLandX, startLandY, (int) bean.mDefaultAngle,
						bean.mSpriteAction1.mActionType,
						bean.mSpriteAction2.mActionType,
						bean.mSpriteAction1.mNeedLoop,
						bean.mSpriteAction1.mAnimationTime,
						bean.mSpriteAction1.mIsBitmapSymmetric,
						bean.mSpriteAction2.mIsBitmapSymmetric);
				mSpiritMap.add(spirit);
				SpriteAction additionalAction1 = bean.mSpriteAction1.mAdditionalAction;
				setAdditionalAction(1, spirit, additionalAction1);
				SpriteAction additionalAction2 = bean.mSpriteAction2.mAdditionalAction;
				setAdditionalAction(2, spirit, additionalAction2);
				setDragAction(spirit, bean.mDragAction);
				setShakeAction(spirit, bean.mShakeAction);
			}
		}
	}

	private void setAdditionalAction(int additionalNum, Spirit spirit,
			SpriteAction additionalAction) {
		if (additionalAction != null) {
			int add = additionalAction.mActionImageNames.size();
			Bitmap[] addBitmaps = new Bitmap[add];
			for (int i = 0; i < add; i++) {
				addBitmaps[i] = mBitmapLoader
						.getCoverBitmap(additionalAction.mActionImageNames
								.get(i));
			}
			Bitmap additionalShadow = mBitmapLoader
					.getCoverBitmap(additionalAction.mShadow.mImageName);
			spirit.setAdditionalAction(additionalNum,
					additionalAction.mActionType,
					(int) additionalAction.mDelayTime,
					additionalAction.mEndType, addBitmaps, additionalShadow,
					additionalAction.mNeedLoop,
					additionalAction.mAnimationTime,
					additionalAction.mIsBitmapSymmetric);
		}
	}

	/**
	 * （一般）静态景物的偏移
	 */
	private void offsetNormals(float lastGravityX, float lastGravityY) {
		for (Normal normal : mNormalMap) {
			normal.onOffset(lastGravityX, lastGravityY);
		}
	}

	private void initClockSpirit(CoverBean coverBean) {
		if (coverBean.mClockMap != null && coverBean.mClockMap.size() > 0) {
			for (CoverBean.ClockBean bean : coverBean.mClockMap) {
				ClockSpirit clock = new ClockSpirit(mViewWidth, mViewHeight,
						this);
				clock.mAllowDrag = bean.mAllowDrag;
				clock.mDailAxisX = bean.mAxis.mAxisX;
				clock.mDailAxisY = bean.mAxis.mAxisY;
				clock.mDail = mBitmapLoader.getCoverBitmap(bean.mDailName);
				clock.mHourhand = mBitmapLoader
						.getCoverBitmap(bean.mHourHand.mImageName);
				clock.mHourPerX = bean.mHourHand.mPivotX;
				clock.mHourPerY = bean.mHourHand.mPivotY;
				clock.mMinutehand = mBitmapLoader
						.getCoverBitmap(bean.mMinuteHand.mImageName);
				clock.mMinutePerX = bean.mMinuteHand.mPivotX;
				clock.mMinutePerY = bean.mMinuteHand.mPivotY;

				clock.mSecondhand = mBitmapLoader
						.getCoverBitmap(bean.mSecondHand.mImageName);
				clock.mSecondPerX = bean.mSecondHand.mPivotX;
				clock.mSecondPerY = bean.mSecondHand.mPivotY;

				clock.mClockScrew = mBitmapLoader
						.getCoverBitmap(bean.mScrewName);

				clock.mStartPerX = bean.mStartLocation.mStartX;
				clock.mStartPerY = bean.mStartLocation.mStartY;
				clock.mStartLandPerX = bean.mStartLocation.mStartLandX;
				clock.mStartLandPerY = bean.mStartLocation.mStartLandY;
				clock.mLimit = bean.mLimitArea.mLimit;
				clock.mLimitLeftPer = bean.mLimitArea.mLeft;
				clock.mLimitRightPer = bean.mLimitArea.mRight;
				clock.mLimitTopPer = bean.mLimitArea.mTop;
				clock.mLimitBottomPer = bean.mLimitArea.mBottom;
				clock.init(mContext);
				mClockMap.add(clock);
			}
		}
	}

	private void initBalloon(CoverBean coverBean) {
		if (coverBean.mBalloonMap != null && coverBean.mBalloonMap.size() > 0) {
			for (CoverBean.BalloonBean bean : coverBean.mBalloonMap) {
				Balloon balloon = new Balloon(mViewWidth, mViewHeight);
				balloon.mAllowDrag = bean.mAllowDrag;
				balloon.mStartPerX = bean.mStartLocation.mStartX;
				balloon.mStartPerY = bean.mStartLocation.mStartY;
				balloon.mLimit = bean.mLimitArea.mLimit;
				balloon.mLimitLeftPer = bean.mLimitArea.mLeft;
				balloon.mLimitRightPer = bean.mLimitArea.mRight;
				balloon.mLimitTopPer = bean.mLimitArea.mTop;
				balloon.mLimitBottomPer = bean.mLimitArea.mBottom;
				balloon.mAroundImg = mBitmapLoader
						.getCoverBitmap(bean.mAroundImgName);

				int size = bean.mImageNames.size();
				balloon.mSmallBallImgList = new ArrayList<Bitmap>(size);
				for (int i = 0; i < size; i++) {
					balloon.mSmallBallImgList.add(mBitmapLoader
							.getCoverBitmap(bean.mImageNames.get(i)));
				}

				size = bean.mIndistinctNames.size();
				balloon.mIndisTinctImgList = new ArrayList<Bitmap>(size);
				for (int i = 0; i < size; i++) {
					balloon.mIndisTinctImgList.add(mBitmapLoader
							.getCoverBitmap(bean.mIndistinctNames.get(i)));
				}

				size = bean.mNumImgNames.size();
				balloon.mNumBitmapsList = new ArrayList<Bitmap>(size);
				for (int i = 0; i < size; i++) {
					balloon.mNumBitmapsList.add(mBitmapLoader
							.getCoverBitmap(bean.mNumImgNames.get(i)));
				}

				size = bean.mBlastImgNames.size();
				balloon.mBlastImgNames = new ArrayList<List<Bitmap>>(size);
				for (int i = 0; i < size; i++) {
					int num = bean.mBlastImgNames.get(i).size();
					List<Bitmap> names = new ArrayList<Bitmap>(num);
					for (int m = 0; m < num; m++) {
						names.add(mBitmapLoader
								.getCoverBitmap(bean.mBlastImgNames.get(i).get(
										m)));
					}
					balloon.mBlastImgNames.add(names);
				}
				balloon.mBalloonImg = mBitmapLoader
						.getCoverBitmap(bean.mBallonImgName);
				balloon.init(mContext);
				mBalloonMap.add(balloon);
			}
		}
	}

	@Override
	public void doDraw(Camera camera, Matrix matrix, Canvas canvas, Paint paint) {
		if (mBroken) {
			return;
		}

		for (Normal normal : mNormalMap) {
			normal.doDraw(camera, matrix, canvas, paint);
		}
		
		mSleigh.doDraw(camera, matrix, canvas, paint);

		for (Slide slide : mSlideMap) {
			slide.doDraw(camera, matrix, canvas, paint);
		}

		for (Rotate rotate : mRotateMap) {
			rotate.doDraw(camera, matrix, canvas, paint);
		}


		for (Translate translate : mTranslateMap) {
			translate.doDraw(camera, matrix, canvas, paint);
		}
		synchronized (TOUCH_MUTEX) {
			for (Touch touch : mTouchMap) {
				touch.doDraw(camera, matrix, canvas, paint);
				if (!touch.mAlive) {
					mInvisibleTouchsMap.add(touch);
				}
			}
			for (Touch touch : mInvisibleTouchsMap) {
				mTouchMap.remove(touch);
				touch.cleanUp();
				touch = null;
			}
			mInvisibleTouchsMap.clear();
		}

		synchronized (AUTO_RANDOM_MUTEX) {
			for (AutoRandom auto : mRandomMap) {
				auto.doDraw(camera, matrix, canvas, paint);
				if (!auto.mAlive) {
					mInvisibleRandomsMap.add(auto);
				}
			}
			for (AutoRandom random : mInvisibleRandomsMap) {
				mRandomMap.remove(random);
				random.cleanUp();
				random = null;
			}
			mInvisibleRandomsMap.clear();

		}

		synchronized (MOVE_MUTEX) {
			for (Move move : mMoveMap) {
				move.doDraw(camera, matrix, canvas, paint);
				if (!move.isActive()) {
					mInvisibleMoveMap.add(move);
				}
			}
			for (Move move : mInvisibleMoveMap) {
				mMoveMap.remove(move);
				move.cleanUp();
				move = null;
			}
			mInvisibleMoveMap.clear();
		}

		for (Spirit spirit : mSpiritMap) {
			spirit.doDraw(camera, matrix, canvas, paint);
		}

		long current = System.currentTimeMillis();
		if (current - mLastUpdateRandomByTime > mRandomBytimeInterval) {
			mLastUpdateRandomByTime = current;
			if (mRandomTimeTemplates != null && mRandomTimeTemplates.size() > 0
					&& mRandomTimeBitmaps != null
					&& mRandomTimeBitmaps.size() > 0) {
				int index = mRandom.nextInt(mRandomTimeTemplates.size());
//				setRandomTime();
//				if (mNextRandomBeanBitmaps == null || mNextRandomBean == null) {
					mNextRandomBeanBitmaps = mRandomTimeBitmaps.get(index);
					mNextRandomBean = mRandomTimeTemplates.get(index);
//				}
				int num = mNextRandomBean.mRandomNum;
				shakeRandoms(mRandom.nextInt(num * RANDOM_UNIT) + num
						* RANDOM_UNIT / 2);
			}
		}
	}

//	private void setRandomTime() {
//		int probability = mRandom.nextInt(100);
//		int sum = 0;
//		int count = Math.min(mRandomTimeTemplates.size(), mRandomTimeBitmaps.size());
//		for (int i = 0; i < count; i++) {
//			sum += mRandomTimeTemplates.get(i).mProbability;
//			if (sum > probability) {
//				mNextRandomBeanBitmaps = mRandomTimeBitmaps.get(i);
//				mNextRandomBean = mRandomTimeTemplates.get(i);
//				break;
//			}
//		}
//	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean ret = false;
		final int action = event.getAction() & MotionEvent.ACTION_MASK;
		final float x = event.getX();
		final float y = event.getY();

		if (ret) {
			clearLongTouch();
			mTouchState = TOUCH_STATE_NORMAL;
			return ret;
		}

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mTouchState = TOUCH_STATE_CLICK;
			if (!mIsActionDownInvoke) {
				mLastDownPosition.x = x;
				mLastDownPosition.y = y;
				mIsActionDownInvoke = true;
				mIsActionMoveInvoke = false;
			}
			mLastDownTime = System.currentTimeMillis();
			break;

		case MotionEvent.ACTION_MOVE:
			float tempXDelta = x - mLastDownPosition.x;
			float tempYDelta = y - mLastDownPosition.y;
			if (Math.sqrt(tempXDelta * tempXDelta + tempYDelta * tempYDelta) > ViewConfiguration
					.get(mContext).getScaledTouchSlop()
					* VIEWCONFIG_MOVE_FACTOR) {
				mTouchState = TOUCH_STATE_MOVE;
				for (int i = 0; i < 3; i++) {        //每次move事件创建2个TouchMove对象
					createTouchMove(x, y, -(mRandom.nextInt(2) + 2));
				}
				clearLongTouch();
				mIsActionMoveInvoke = true;
			}
			mIsActionDownInvoke = false;
			break;
		case MotionEvent.ACTION_UP:
			mIsActionDownInvoke = false;
			if (mTouchState != TOUCH_STATE_LONGCLICK && !mIsActionMoveInvoke) {
				createTouch(x, y, -5);
			}
			mTouchState = TOUCH_STATE_NORMAL;
			clearLongTouch();
			randomMove();
			break;
		case MotionEvent.ACTION_CANCEL:
			mIsActionDownInvoke = false;
			if (mTouchState != TOUCH_STATE_LONGCLICK && !mIsActionMoveInvoke) {
				createTouch(x, y, -5);
			}
			mTouchState = TOUCH_STATE_NORMAL;
			clearLongTouch();
			randomMove();
			break;
		default:
			break;
		}

		for (Spirit spirit : mSpiritMap) {
			ret = ret | spirit.onTouchEvent(event);
		}
		if (ret) {
			return ret;
		}

		for (Rotate rotate : mRotateMap) {
			ret = ret | rotate.onTouchEvent(event);
		}
		if (mSleigh != null) {
			ret = ret | mSleigh.onTouchEvent(event);
		}
		for (Slide slide : mSlideMap) {
			ret = ret | slide.onTouchEvent(event);
		}
		return ret;
	}

	private void clearLongTouch() {
		if (mShowingLongTouch != null) {
			mShowingLongTouch.cancel();
		}
	}

	private void randomMove() {
		int probability = mRandom.nextInt(100);
		int sum = 0;
		int i;
		for (i = 0; i < mMoveBeanLength; i++) {
			sum += mMoveTemplates.get(i).mProbability;
			if (sum > probability) {
				mMoveRandomIndex = i;
				break;
			}
		}
	}

	private void createTouchMove(float x, float y, int speedAlpha) {
		if (mMoveBitmapsMap == null || mMoveBitmapsMap.size() <= 0
				|| mMoveMap.size() > MAX_AUTO_MOVE_COUNT) {
			return;
		}
		if (Math.abs(mLastMovePoint[0] - x) >= 2
				&& Math.abs(mLastMovePoint[1] - y) >= 2) {
			mLastMovePoint[0] = x;
			mLastMovePoint[1] = y;
			MoveBean bean = mMoveTemplates.get(mMoveRandomIndex);
			Move move = new Move(mMoveBitmapsMap.get(mMoveRandomIndex),
					mViewWidth, mViewHeight, (int) x, (int) y, speedAlpha,
					2000, bean.mMinTumbleSpdX, bean.mMaxTumbleSpdX,
					bean.mMinTumbleSpdY, bean.mMaxTumbleSpdY, bean.mMinSpeedX,
					bean.mMaxSpeedX, bean.mMinSpeedY, bean.mMaxSpeedY,
					bean.mMinScale, bean.mMaxScale);
			// }
			if (move != null) {
				synchronized (MOVE_MUTEX) {
					// mMoveMap.addAll(moves);
					mMoveMap.add(move);
				}

			}
		}
	}

	private void createTouch(float x, float y, int speedAlpha) {
		if (mTouchBitmaps == null) {
			return;
		}
		int count = mRandom.nextInt(3) + 4;
		List<Touch> touchs = new ArrayList<Touch>();
		Bitmap bitmap = mTouchBitmaps[mRandom.nextInt(mTouchBitmaps.length)];
		for (int i = 0; i < count; i++) {
			touchs.add(new Touch(mViewWidth, mViewHeight, x, y, bitmap,
					mTouchBean.mAnimationType, mTouchBitmaps));
		}
		synchronized (TOUCH_MUTEX) {
			if (mTouchBean.mAnimationType == Touch.TYPE_FLICKER) {
				mTouchMap.clear();
			}
			mTouchMap.addAll(touchs);
		}
	}

	@Override
	public void moving() {
		for (Spirit spirit : mSpiritMap) {
			spirit.moving();
		}
		try { // 无同步保护
//			normalsMoving();
			for (Normal normal : mNormalMap) {
				normal.moving();
			}
			for (Rotate rotate : mRotateMap) {
				rotate.moving();
			}

			for (Translate translate : mTranslateMap) {
				translate.moving();
			}

			for (AutoRandom auto : mRandomMap) {
				auto.moving();
			}

			for (Slide slide : mSlideMap) {
				slide.moving();
			}

			for (Move move : mMoveMap) {
				move.moving();
			}

			for (Touch touch : mTouchMap) {
				touch.moving();
			}

			if (mSleigh != null) {
				mSleigh.moving();
			}

		} catch (Exception e) {
//			e.printStackTrace();
		}
	}

	@Override
	public boolean isTaped(float x, float y) {
		return false;
	}

	/**
	 * 响应重力变化事件
	 * 
	 * @param gravity
	 */
	public void handleGravityChange(AccelerometerDataState state) {
		// final float x = state.getX();
		// final float y = state.getY();
		// 改变静态景物位置
		// changeNormalsOffset(x, y);
		// 旋转景物
		for (Rotate rotate : mRotateMap) {
			rotate.handleGravityChange(state);
		}

	}

	/**
	 * 响应甩动事件
	 * 
	 * @param speed
	 */
	public void handShake(float speed) {
		// 随机景物
		if (mRandomShakeTemplates != null && mRandomShakeTemplates.size() > 0
				&& mRandomShakeBitmaps != null
				&& mRandomShakeBitmaps.size() > 0) {
			int index = mRandom.nextInt(mRandomShakeTemplates.size());
			mNextRandomBeanBitmaps = mRandomShakeBitmaps.get(index);
			mNextRandomBean = mRandomShakeTemplates.get(index);
		}
		shakeRandoms(speed);
		// 旋转景物
		for (Rotate rotate : mRotateMap) {
			rotate.handShake(speed);
		}
		// for (Slide slide : mSlideMap) {
		// slide.handShake(speed);
		// }

		if (mSleigh != null) {
			mSleigh.handShake(speed);
		}
		
//		for (Spirit spirit : mSpiritMap) {
//			spirit.handShake(speed);
//		}
		
		for (Normal normal : mNormalMap) {
			normal.handShake(speed);
		}
		
	}

	/**
	 * 随机出现景物响应甩动事件
	 * 
	 * @param speed
	 *            甩动的速度
	 */
	public void shakeRandoms(float speed) {
		if (mRandomMap.size() > MAX_AUTO_RANDOM_COUNT
				|| mNextRandomBean == null || mNextRandomBeanBitmaps == null) {
			return;
		}
		List<AutoRandom> randoms = new ArrayList<AutoRandom>();
		int count = (int) (speed / RANDOM_UNIT);
		for (int i = 0; i < count; i++) {
			randoms.add(new AutoRandom(mNextRandomBeanBitmaps,
					mNextRandomBean.mAllowDrag, mViewWidth, mViewHeight,
					mNextRandomBean.mMinTumbleSpdX,
					mNextRandomBean.mMaxTumbleSpdX,
					mNextRandomBean.mMinTumbleSpdY,
					mNextRandomBean.mMaxTumbleSpdY, mNextRandomBean.mMinSpeedX,
					mNextRandomBean.mMaxSpeedX, mNextRandomBean.mMinSpeedY,
					mNextRandomBean.mMaxSpeedY, mNextRandomBean.mMinScale,
					mNextRandomBean.mMaxScale, mNextRandomBean.mAnimationType,
					mNextRandomBean.mShowType,
					mNextRandomBean.mStartMinLocationX,
					mNextRandomBean.mStartMaxLocationX,
					mNextRandomBean.mStartMinLocationY,
					mNextRandomBean.mStartMaxLocationY));
		}
		synchronized (AUTO_RANDOM_MUTEX) {
			mRandomMap.addAll(randoms);
		}
	}

	/**
	 * 必须先终止其他线程，如Moving线程，否则可能会有同步问题 {@inheritDoc}
	 */
	@Override
	public void cleanUp() {
		unRegisterAll();
		if (mMoveMap != null) {
			mMoveMap.clear();
		}
		if (mInvisibleMoveMap != null) {
			mInvisibleMoveMap.clear();
		}
		if (mRandomMap != null) {
			mRandomMap.clear();
		}
		if (mInvisibleRandomsMap != null) {
			mInvisibleRandomsMap.clear();
		}
		if (mTouchMap != null) {
			mTouchMap.clear();
		}
		if (mInvisibleTouchsMap != null) {
			mInvisibleTouchsMap.clear();
		}
		if (mLongTouchsMap != null) {
			mLongTouchsMap.clear();
		}
		if (mInvisibleLongTouchsMap != null) {
			mInvisibleLongTouchsMap.clear();
		}
		if (mBitmapLoader != null) {
			mBitmapLoader.recyleAllCoverBitmap();
		}
		if (mMoveBitmapsMap != null) {
			mMoveBitmapsMap.clear();
		}
		if (mRandomShakeBitmaps != null) {
			mRandomShakeBitmaps.clear();
		}
		if (mRandomTimeBitmaps != null) {
			mRandomTimeBitmaps.clear();
		}
        if (mTouchBean != null) {
        	mTouchBean = null;
        }
        
		if (mRotateMap != null) {
			for (Rotate rotate : mRotateMap) {
				rotate.cleanUp();
			}
		}
		if (mSleigh != null) {
			mSleigh.cleanUp();
		}
	}

	public void destroy() {
		cleanUp();
		if (mSpiritMap != null) {
			mSpiritMap.clear();
			mSpiritMap = null;
		}
		if (mNormalMap != null) {
			mNormalMap.clear();
			mNormalMap = null;
		}

		if (mSlideMap != null) {
			mSlideMap.clear();
			mSlideMap = null;
		}

		if (mRotateMap != null) {
			mRotateMap.clear();
			mRotateMap = null;
		}
		if (mTranslateMap != null) {
			mTranslateMap.clear();
			mTranslateMap = null;
		}

		mTouchBitmaps = null;
		mBitmapLoader = null;
		mLongTouchTemplate = null;
	}

	private void registerAll() {
		if (mRotateMap != null) {
			for (Rotate rotate : mRotateMap) {
				rotate.regist();
			}
		}
	}

	private void unRegisterAll() {
		if (mRotateMap != null) {
			for (Rotate rotate : mRotateMap) {
				rotate.unRegiste();
			}
		}
	}

	public void setScreenSize(int width, int height) {
		mViewWidth = width;
		mViewHeight = height;
	}

	public void onConfigurationChanged(CoverBean coverBean,
			boolean backToDefault) {

		if (coverBean == null) {
			return;
		}
		// 静态景物
		if (mNormalMap != null && mNormalMap.size() > 0
				&& coverBean.mNormalMap != null
				&& coverBean.mNormalMap.size() > 0) {
			Normal normal = null;
			CoverBean.NormalBean bean = null;
			final int count = Math.min(mNormalMap.size(),
					coverBean.mNormalMap.size());
			for (int i = 0; i < count; i++) {
				normal = mNormalMap.get(i);
				bean = coverBean.mNormalMap.get(i);
				if (normal != null && bean != null) {
					normal.mX = normal.mDefaultX = DrawUtils
							.percentX2px(bean.mStartLocation.mStartX);
					normal.mY = normal.mDefaultY = DrawUtils
							.percentY2px(bean.mStartLocation.mStartY);
					percentRect2px(normal.mLimitRect, bean.mLimitArea);
					final int landType = bean.mRelativeLocation.mLandscapeType;
					final int portType = bean.mRelativeLocation.mVerticalType;

					int offsetX = (int) (DrawUtils.isPort() ? normal.mX
							: DrawUtils.percentY2px(bean.mStartLocation.mStartX));
					int offsetY = (int) (DrawUtils.isPort() ? normal.mY
							: DrawUtils.percentX2px(bean.mStartLocation.mStartY));

					if (landType == CoverBean.LAND_RIGHT) {
						normal.mX = normal.mDefaultX = normal.mLimitRect.right
								- normal.mWidth + offsetX;
						normal.mLimitRect.left = DrawUtils.getScreenViewWidth()
								- normal.mWidth + offsetX;
					}
					if (portType == CoverBean.PORT_BOTTOM) {
						normal.mY = normal.mDefaultY = normal.mLimitRect.bottom
								- normal.mHeight + offsetY;
						normal.mLimitRect.top = DrawUtils.getScreenViewHeight()
								- normal.mHeight + offsetY;
					}
					normal.init(normal.mY, normal.mY - offsetY);
				}
			} // end for
		} // end mNormalMap

		// 定时更换图片的景物
		if (mSlideMap != null && mSlideMap.size() > 0
				&& coverBean.mSlideMap != null
				&& coverBean.mSlideMap.size() > 0) {
			Slide slide = null;
			CoverBean.SlideBean bean = null;
			final int count = Math.min(mSlideMap.size(),
					coverBean.mSlideMap.size());
			for (int i = 0; i < count; i++) {
				slide = mSlideMap.get(i);
				if (slide == null) {
					continue;
				}
				bean = coverBean.mSlideMap.get(i);
				if (bean != null) {
					slide.mX = DrawUtils
							.percentX2px(bean.mStartLocation.mStartX);
					slide.mY = DrawUtils
							.percentY2px(bean.mStartLocation.mStartY);
					percentRect2px(slide.mLimitRect, bean.mLimitArea);
					final int landType = bean.mRelativeLocation.mLandscapeType;
					final int portType = bean.mRelativeLocation.mVerticalType;
					if (landType == CoverBean.LAND_RIGHT) {
						slide.mX = slide.mLimitRect.right
								- slide.mBitmap.getWidth();
						slide.mLimitRect.left = DrawUtils.getScreenViewWidth()
								- slide.mBitmap.getWidth();
					}
					if (portType == CoverBean.PORT_BOTTOM) {
						slide.mY = slide.mLimitRect.bottom
								- slide.mBitmap.getHeight();
						slide.mLimitRect.top = DrawUtils.getScreenViewHeight()
								- slide.mBitmap.getHeight();
					}
				}
				slide.resetData(mViewWidth, mViewHeight, backToDefault);
			}
		}

		// 精灵
		if (mSpiritMap != null && mSpiritMap.size() > 0
				&& coverBean.mSpriteMap != null
				&& coverBean.mSpriteMap.size() > 0) {
			Spirit spirit = null;
			CoverBean.SpriteBean bean = null;
			final int count = Math.min(mSpiritMap.size(),
					coverBean.mSpriteMap.size());
			for (int i = 0; i < count; i++) {
				spirit = mSpiritMap.get(i);
				bean = coverBean.mSpriteMap.get(i);
				if (spirit != null && bean != null) {
					final int landType = bean.mRelativeLocation.mLandscapeType;
					final int portType = bean.mRelativeLocation.mVerticalType;
					int startX;
					int startY;
					if (DrawUtils.isPort()) {
						startX = DrawUtils
								.percentX2px(bean.mStartLocation.mStartX);
						startY = DrawUtils
								.percentY2px(bean.mStartLocation.mStartY);
					} else {
						startX = DrawUtils
								.percentX2px(bean.mStartLocation.mStartLandX);
						startY = DrawUtils
								.percentY2px(bean.mStartLocation.mStartLandY);
					}

					Bitmap defaultAction1 = spirit.mAction1Bitmaps[0];
					if (landType == CoverBean.LAND_RIGHT
							&& defaultAction1 != null) {
						startX = DrawUtils.getScreenViewWidth()
								- defaultAction1.getWidth();
					}
					if (portType == CoverBean.PORT_BOTTOM
							&& defaultAction1 != null) {
						startY = DrawUtils.getScreenViewHeight()
								- defaultAction1.getHeight();
					}
					Rect limit = new Rect();
					percentRect2px(limit, bean.mLimitArea);
					spirit.resetData(startX, startY, limit, backToDefault);
				}
			} // end for
		} // end mSpiritMap
			// 蔓藤
		if (mRotateMap != null && mRotateMap.size() > 0
				&& coverBean.mRotateMap != null
				&& coverBean.mRotateMap.size() > 0) {
			Rotate rotate = null;
			CoverBean.RotateBean bean = null;
			final int count = Math.min(mRotateMap.size(),
					coverBean.mRotateMap.size());
			for (int i = 0; i < count; i++) {
				rotate = mRotateMap.get(i);
				bean = coverBean.mRotateMap.get(i);
				if (rotate != null && bean != null) {
					Rect limitRect = new Rect();
					percentRect2px(limitRect, bean.mLimitArea);
					final int landType = bean.mRelativeLocation.mLandscapeType;
					final int portType = bean.mRelativeLocation.mVerticalType;
					int startX = DrawUtils
							.percentX2px(bean.mStartLocation.mStartX);
					int startY = DrawUtils
							.percentY2px(bean.mStartLocation.mStartY);
					Bitmap bitmap = rotate.mBitmap;

					int offsetX = DrawUtils.isPort() ? startX : DrawUtils
							.percentY2px(bean.mStartLocation.mStartX);
					int offsetY = DrawUtils.isPort() ? startY : DrawUtils
							.percentX2px(bean.mStartLocation.mStartY);

					if (landType == CoverBean.LAND_RIGHT) {
						startX = limitRect.right - bitmap.getWidth() + offsetX;
					}
					if (portType == CoverBean.PORT_BOTTOM) {
						startY = limitRect.bottom - bitmap.getHeight()
								+ offsetY;
					}

					Rect limit = new Rect();
					percentRect2px(limit, bean.mLimitArea);
					rotate.resetData(startX, startY, limit, backToDefault);
				}
			} // end for
		} // end mRotateMap

		if (mSleigh != null) {
			mSleigh.onConfigurationChanged(mViewWidth, mViewHeight);
		}

	} // end onConfigurationChanged

	public void setBroken(boolean broken) {
		mBroken = broken;
	}

	@Override
	public boolean onCreateAtom(int type, int startX, int startY) {
		// TODO Auto-generated method stub
		switch (type) {
		case OnCreateAtomListener.TYPE_ATOM_TOUCH:

			break;
		}

		return false;
	}

	public static final int OBJECT_TYPE_SLIDE = 0;
	public static final int OBJECT_TYPE_SPIRIT = 1;
	
	@Override
	public void onActionCallback(int objectType, int actionType) {
		// TODO Auto-generated method stub
		switch(objectType) {
		case OBJECT_TYPE_SLIDE: {
			int count = mSlideMap.size();
			Slide slide;
			for (int i = 0; i < count; i++) {
				slide = mSlideMap.get(i);
				if (slide != null) {
					slide.handAnimate();
				}
			}
		}
			break;
		case OBJECT_TYPE_SPIRIT: {
			int count = mSpiritMap.size();
			Spirit spirit;
			for (int i = 0; i < count; i++) {
				spirit = mSpiritMap.get(i);
				if (spirit != null) {
					spirit.handAction();
				}
			}
		}
			break;
		}

	}

}
