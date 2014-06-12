package com.gau.go.launcherex.theme.cover;

import java.util.ArrayList;
import java.util.List;

/**
 * 罩子层的bean
 * 
 * @author jiangxuwen
 * 
 */
public class CoverBean {
	public static final String PACKAGE_NAME = "com.gau.go.launcherex.theme.christmas2";
	public static final int LAND_LEFT = 1;
	public static final int LAND_CENTER = 2;
	public static final int LAND_RIGHT = 3;
	public static final int PORT_TOP = 1;
	public static final int PORT_CENTER = 2;
	public static final int PORT_BOTTOM = 3;
	/*
	 * 版本信息
	 */
	public float mDeskVersion;
	public String mDeskVersionName;

	public TouchBean mTouchBean;
	public List<FallObjectsBean> mFallObjectsMap;
	public List<MoveBean> mMoveBeanMap;
	public List<RandomBean> mRandomMap;

	public List<NormalBean> mNormalMap;
	public List<SlideBean> mSlideMap;
	public List<RotateBean> mRotateMap;
	public List<TranslateBean> mTranslateMap;

	public List<LongTounchBean> mLongTounchMap;
	public List<SpriteBean> mSpriteMap;
	public List<ClockBean> mClockMap;
	public List<BalloonBean> mBalloonMap;

	public SleighBean mSleighBean;

	public CoverBean(String pkgName) {
		mNormalMap = new ArrayList<CoverBean.NormalBean>();
		mSlideMap = new ArrayList<CoverBean.SlideBean>();
		mRotateMap = new ArrayList<CoverBean.RotateBean>();
		mTranslateMap = new ArrayList<CoverBean.TranslateBean>();
		mRandomMap = new ArrayList<CoverBean.RandomBean>();
		mLongTounchMap = new ArrayList<CoverBean.LongTounchBean>();
		mSpriteMap = new ArrayList<CoverBean.SpriteBean>();
		mClockMap = new ArrayList<CoverBean.ClockBean>();
		mBalloonMap = new ArrayList<CoverBean.BalloonBean>();
		mMoveBeanMap = new ArrayList<CoverBean.MoveBean>();
		mSleighBean = new SleighBean();
	}

	/**
	 * 一般景物（静止的）
	 * 
	 * @author jiangxuwen
	 * 
	 */
	class NormalBean {
		public String mImageName;
		public boolean mAllowDrag;
		public StartLocation mStartLocation;
		public Shadow mShadow;
		public LimitArea mLimitArea;
		public RelativeLocation mRelativeLocation;
	}

	/**
	 * 圣诞车（包括圣诞老人、两只糜鹿、旗子、鞭子）
	 * 
	 * @author chenqiang
	 * 
	 */
	class SleighBean {
		public DeerBean mDeerBean;
		public SantaBean mSantaBean;
		public WhipBean mWhipBean;
		public FlagBean mFlagBean;
		public MovePointBean mMovePointBean;
		public GiftBean mGiftBean;
		public RabbitBean mRabbitBean;
		public PackageBean mPackageBean;
		public FallSantaBean mFallSantaBean;
		public UpSantaBean mUpSantaBean;
		public RunSantaBean mRunSantaBean;
		public EmptySleighBean mEmptySleighBean;

		public SleighBean() {
			mDeerBean = new DeerBean();
			mSantaBean = new SantaBean();
			mWhipBean = new WhipBean();
			mFlagBean = new FlagBean();
			mMovePointBean = new MovePointBean();
			mGiftBean = new GiftBean();
			mRabbitBean = new RabbitBean();
			mFallSantaBean = new FallSantaBean();
			mUpSantaBean = new UpSantaBean();
			mRunSantaBean = new RunSantaBean();
			mEmptySleighBean = new EmptySleighBean();
			mPackageBean = new PackageBean();
		}

	}

	/**
	 * 
	 * @author chenqiang
	 * 
	 */
	class DeerBean {
		public List<String> mImageNames;

		public DeerBean() {
			mImageNames = new ArrayList<String>();
		}
	}

	/**
	 * 
	 * @author chenqiang
	 * 
	 */
	class SantaBean {
		public List<String> mImageNames;

		public SantaBean() {
			mImageNames = new ArrayList<String>();
		}
	}

	/**
	 * 
	 * @author chenqiang
	 * 
	 */
	class WhipBean {
		public List<String> mImageNames;

		public WhipBean() {
			mImageNames = new ArrayList<String>();
		}
	}

	/**
	 * 
	 * @author chenqiang
	 * 
	 */
	class FlagBean {
		public List<String> mLTRImageNames;
		public List<String> mRTLImageNames;

		public FlagBean() {
			mLTRImageNames = new ArrayList<String>();
			mRTLImageNames = new ArrayList<String>();
		}
	}

	/**
	 * 
	 * @author chenqiang
	 * 
	 */
	class MovePointBean {
		public List<String> mImageNames;

		public MovePointBean() {
			mImageNames = new ArrayList<String>();
		}
	}

	/**
	 * 
	 * @author chenqiangparceDeer(xmlPullParser, sleighBean);
	 * 
	 */
	class GiftBean {
		public List<String> mImageNames;

		public GiftBean() {
			mImageNames = new ArrayList<String>();
		}
	}

	/**
	 * 
	 * @author chenqiang
	 * 
	 */
	class RabbitBean {
		public List<String> mJumpImageNames;
		public List<String> mWaveImageNames;
		public List<String> mDismissImageNames;

		public RabbitBean() {
			mJumpImageNames = new ArrayList<String>();
			mWaveImageNames = new ArrayList<String>();
			mDismissImageNames = new ArrayList<String>();
		}
	}

	/**
	 * 
	 * @author chenqiang
	 * 
	 */
	class RunSantaBean {
		public List<String> mImageNames;

		public RunSantaBean() {
			mImageNames = new ArrayList<String>();
		}
	}

	/**
	 * 
	 * @author chenqiang
	 * 
	 */
	class FallSantaBean {
		public List<String> mImageNames;

		public FallSantaBean() {
			mImageNames = new ArrayList<String>();
		}
	}

	/**
	 * 
	 * @author chenqiang
	 * 
	 */
	class UpSantaBean {
		public List<String> mImageNames;

		public UpSantaBean() {
			mImageNames = new ArrayList<String>();
		}
	}

	/**
	 * 
	 * @author chenqiang
	 * 
	 */
	class EmptySleighBean {
		public List<String> mImageNames;

		public EmptySleighBean() {
			mImageNames = new ArrayList<String>();
		}
	}

	/**
	 * 
	 * @author chenqiang
	 * 
	 */
	class PackageBean {
		public List<String> mImageNames;

		public PackageBean() {
			mImageNames = new ArrayList<String>();
		}
	}

	/**
	 * 定时切换图片
	 * 
	 * @author maxiaojun
	 * 
	 */
	class SlideBean extends SpriteBean {
		public List<String> mImageNames;
		public boolean mAllowDrag;
		public String mImgName;
		public int mAnimationType;
		public int mShakeSpeed;
		public long mInterval; // 时间间隔
		public Shadow mShadow;
		public SlideBean() {
			mImageNames = new ArrayList<String>();
		}
	}

	/**
	 * 多帧图
	 * 
	 * @author chenqiang
	 * 
	 */
	class MultiFrameBean {
		public List<BitmapItem> mBitmapFrameTop;
		public List<BitmapItem> mBitmapFrameBottom;
		public boolean mAllowDrag;
		public int mLoopCount;
		public boolean mMissOnTouchUp;
		public StartLocation mStartLocation;
		public Shadow mShadow;
		public LimitArea mlLimitArea;
		public Life mLife;

		public MultiFrameBean() {
			mBitmapFrameTop = new ArrayList<BitmapItem>();
			mBitmapFrameBottom = new ArrayList<BitmapItem>();
		}
	}

	/**
	 * 旋转变化的景物
	 * 
	 * @author jiangxuwen
	 * 
	 */
	class RotateBean extends NormalBean {
		public String mBodyImgName;
		public float mRopeHeight;
		public RotateAnim mRotateAnim;
		public List<ClickAction> mClickActionList;
		public DragAction mDragAction;

		public RotateBean() {
			mClickActionList = new ArrayList<CoverBean.ClickAction>();
		}
	}

	/**
	 * 位移变化景物
	 * 
	 * @author jiangxuwen
	 * 
	 */
	class TranslateBean extends NormalBean {
		public TranslateAnim mTranslateAnim;
	}

	/**
	 * 触摸屏幕出现的景物
	 * 
	 * @author jiangxuwen
	 * 
	 */
	class TouchBean extends RandomBean {
		public Life mLife;

		public TouchBean() {

		}
	}

	/**
	 * 掉落的物体
	 * 
	 * @author maxiaojun
	 * 
	 */
	class FallObjectsBean extends RandomBean {
		public Life mLife;
	}

	/**
	 * 长按屏幕出现的景物
	 * 
	 * @author jiangxuwen
	 * 
	 */
	class LongTounchBean extends MultiFrameBean {
		int mMaxCount = 10; // default Count
		int mInAnimationType;
	}

	/**
	 * 在屏幕上滑动出现的景物
	 * 
	 * @author jiangxuwen
	 * 
	 */
	class MoveBean extends TouchBean {
		
	}

	/**
	 * 屏幕上自由活动的精灵
	 * 
	 * @author jiangxuwen
	 * 
	 */
	class SpriteBean {
		public boolean mAllowDrag;
		public float mDefaultAngle;
		public RotateXY mRotateXY;
		public LimitArea mLimitArea;
		public StartLocation mStartLocation;
		public RelativeLocation mRelativeLocation;
		public SpriteAction mSpriteAction1;
		public SpriteAction mSpriteAction2;
		public DragAction mDragAction;
		public ShakeAction mShakeAction;

		public SpriteBean() {
			mSpriteAction1 = new SpriteAction();
			mSpriteAction2 = new SpriteAction();
		}
	}

	/**
	 * 随机出现的事物，包括自动出现，吹气出现和摇晃出现
	 * 
	 * @author jiangxuwen
	 * 
	 */
	class RandomBean {
		public List<String> mImageNames;
		public boolean mAllowDrag;
		public int mProbability;
		public int mRandomNum; // 每次出现的数量
		public long mInterval; // 两次出现的时间间隔
		public int mMinTumbleSpdX;
		public int mMaxTumbleSpdX;
		public int mMinTumbleSpdY;
		public int mMaxTumbleSpdY;
		public int mMinSpeedX;
		public int mMaxSpeedX;
		public int mMinSpeedY;
		public int mMaxSpeedY;
		public float mMinScale;
		public float mMaxScale;
		public int mAnimationType;
		public float mStartMinLocationX;
		public float mStartMinLocationY;
		public float mStartMaxLocationX;
		public float mStartMaxLocationY;
		public int mShowType = 1;

		public RandomBean() {
			mImageNames = new ArrayList<String>();
		}
	}

	/**
	 * 
	 * <br>
	 * 类描述: 钟表 <br>
	 * 功能详细描述:
	 * 
	 * @author maxiaojun
	 * @date [2012-11-7]
	 */
	class ClockBean {
		public boolean mAllowDrag;
		public Axis mAxis;
		public Shadow mShadow;
		public LimitArea mLimitArea;
		public String mBgName;
		public String mDailName;
		public String mScrewName;
		public ClockHand mMinuteHand;
		public ClockHand mHourHand;
		public ClockHand mSecondHand;
		public StartLocation mStartLocation;
	}

	/**
	 * 
	 * <br>
	 * 类描述: 热气球 <br>
	 * 功能详细描述:
	 * 
	 * @author maxiaojun
	 * @date [2012-11-7]
	 */
	class BalloonBean {
		public boolean mAllowDrag;
		public LimitArea mLimitArea;
		public StartLocation mStartLocation;
		public String mBallonImgName;
		public String mAroundImgName;
		public List<String> mImageNames = new ArrayList<String>();
		public List<String> mIndistinctNames = new ArrayList<String>();
		public List<List<String>> mBlastImgNames = new ArrayList<List<String>>();
		public List<String> mNumImgNames = new ArrayList<String>();
	}

	/**
	 * 起始位置
	 * 
	 * @author jiangxuwen
	 * 
	 */
	class StartLocation {
		boolean mCreateShow;
		float mStartX;
		float mStartY;
		float mStartLandX;
		float mStartLandY;
		int mLocationType;
	}

	/**
	 * 影子
	 * 
	 * @author jiangxuwen
	 * 
	 */
	class Shadow {
		boolean mNeedShadow;
		String mImageName;
		int mNextToX;
		int mNextToY;
	}

	/**
	 * 限制的范围
	 * 
	 * @author jiangxuwen
	 * 
	 */
	class LimitArea {
		boolean mLimit;
		float mLeft;
		float mTop;
		float mRight;
		float mBottom;
		float mTouchOffsetX;
		float mTouchOffsetY;
	}

	/**
	 * 旋转动画信息
	 * 
	 * @author jiangxuwen
	 * 
	 */
	class RotateAnim {
		long mDuration;
		boolean mLoop;
		int mFromDegress;
		int mToDegress;
		float mPivotX;
		float mPivotY;
	}

	/**
	 * 位移动画信息
	 * 
	 * @author jiangxuwen
	 * 
	 */
	class TranslateAnim {
		long mDuration;
		boolean mLoop;
		boolean mBackRotate;
		int mFromXDelta;
		int mToXDelta;
		int mFromYDelta;
		int mToYDelta;
	}

	/**
	 * 生命时间
	 * 
	 * @author jiangxuwen
	 * 
	 */
	class Life {
		long mStartAge;
		long mEndAge;
	}

	/**
	 * 精灵旋转的点（相对于精灵size的百分比）
	 * 
	 * @author jiangxuwen
	 * 
	 */
	class RotateXY {
		float mPivotX;
		float mPivotY;
	}

	/**
	 * 表盘的轴心
	 * 
	 * @author jiangxuwen
	 * 
	 */
	class Axis {
		float mAxisX;
		float mAxisY;
	}

	/**
	 * 钟表的针
	 * 
	 * @author jiangxuwen
	 * 
	 */
	class ClockHand {
		String mImageName;
		float mPivotX;
		float mPivotY;
	}

	/**
	 * 
	 * <br>
	 * 类描述: <br>
	 * 功能详细描述:
	 * 
	 * @author maxiaojun
	 * @date [2012-11-24]
	 */
	abstract class BaseAction {
		String mNormalImage;
		Shadow mShadow;
		int mEndType;
		int mActionType;
		int mShakeSpeed; // 大于这个值，会响应shake事件
		long mDelayTime;
		List<String> mActionImageNames;
		boolean mNeedLoop;
		int mAnimationTime;
		boolean mIsBitmapSymmetric = true;
	}

	/**
	 * 精灵的动画
	 * 
	 * @author jiangxuwen
	 * 
	 */
	class SpriteAction extends BaseAction {
		SpriteAction mAdditionalAction;

		public SpriteAction() {
			mActionImageNames = new ArrayList<String>();
			mShadow = new Shadow();
		}
	}

	/**
	 * Rotate的动画
	 * 
	 * @author
	 * 
	 */
	class ClickAction extends BaseAction {
		public ClickAction() {
			mActionImageNames = new ArrayList<String>();
			mShadow = new Shadow();
		}
	}

	/**
	 * Rotate的动画
	 * 
	 * @author
	 * 
	 */
	class DragAction extends BaseAction {
		public DragAction() {
			mActionImageNames = new ArrayList<String>();
			mShadow = new Shadow();
		}
	}

	/**
	 * 
	 * @author chenqiang
	 * 
	 */
	class ShakeAction extends BaseAction {
		public ShakeAction() {
			mActionImageNames = new ArrayList<String>();
			mShadow = new Shadow();
		}
	}

	public ClickAction createClickAction() {
		return new ClickAction();
	}

	public DragAction createDragAction() {
		return new DragAction();
	}

	public ShakeAction createShakeAction() {
		return new ShakeAction();
	}

	/**
	 * 相对位置
	 * 
	 * @author jiangxuwen
	 * 
	 */
	class RelativeLocation {
		/**
		 * 分左、中、右
		 */
		int mLandscapeType;

		/**
		 * 分上、中、下
		 */
		int mVerticalType;
	}

	/**
	 * 位图（包括名称和生命周期）
	 * 
	 * @author chenqiang
	 * 
	 */
	class BitmapItem {

		String mBitmapName;
		int mInAnimationType;
		int mDuration;
	}

	public NormalBean createNormalBean() {
		return new NormalBean();
	}

	public SlideBean createSlideBean() {
		return new SlideBean();
	}

	public SleighBean createSleighBean() {
		return new SleighBean();
	}

	public RotateBean createRotateBean() {
		return new RotateBean();
	}

	public TranslateBean createTranslateBean() {
		return new TranslateBean();
	}

	public TouchBean createTouchBean() {
		return new TouchBean();
	}

	public FallObjectsBean createFallObjectsBean() {
		return new FallObjectsBean();
	}

	public RandomBean createRandomBean() {
		return new RandomBean();
	}

	public ClockBean createClockBean() {
		return new ClockBean();
	}

	public BalloonBean createBallleanBean() {
		return new BalloonBean();
	}

	public LongTounchBean createLongTounchBean() {
		return new LongTounchBean();
	}

	public MoveBean createMoveBean() {
		return new MoveBean();
	}

	public SpriteBean createSpriteBean() {
		return new SpriteBean();
	}

	public StartLocation createStartLocation() {
		return new StartLocation();
	}

	public RotateXY createRotateXY() {
		return new RotateXY();
	}

	public Shadow createShadow() {
		return new Shadow();
	}

	public LimitArea createLimitArea() {
		return new LimitArea();
	}

	public RotateAnim createRotateAnim() {
		return new RotateAnim();
	}

	public TranslateAnim createTranslateAnim() {
		return new TranslateAnim();
	}

	public Life createLife() {
		return new Life();
	}

	public Axis createAxis() {
		return new Axis();
	}

	public ClockHand createClockHand() {
		return new ClockHand();
	}

	public SpriteAction createSpriteAction() {
		return new SpriteAction();
	}

	public RelativeLocation createRelativeLocation() {
		return new RelativeLocation();
	}

	public BitmapItem createBitmapItem() {
		return new BitmapItem();
	}

	public void cleanUp() {
		mDeskVersionName = null;
		if (mNormalMap != null) {
			mNormalMap.clear();
		}
		if (mRotateMap != null) {
			mRotateMap.clear();
		}
		if (mTranslateMap != null) {
			mTranslateMap.clear();
		}
		if (mTouchBean != null) {
			mTouchBean.mImageNames.clear();
		}
		if (mLongTounchMap != null) {
			mLongTounchMap.clear();
		}
		if (mMoveBeanMap != null) {
			mMoveBeanMap.clear();
		}
		if (mSpriteMap != null) {
			mSpriteMap.clear();
		}
		if (mRandomMap != null) {
			mRandomMap.clear();
		}
		if (mClockMap != null) {
			mClockMap.clear();
		}
	}
}
