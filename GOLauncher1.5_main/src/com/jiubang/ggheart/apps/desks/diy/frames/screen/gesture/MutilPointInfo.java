package com.jiubang.ggheart.apps.desks.diy.frames.screen.gesture;

/**
 * 多点触摸的Touch Point信息
 * 
 * @author luopeihuan
 * 
 */
public class MutilPointInfo {

	private int mNumPoints; // 触摸点数
	private float[] mXs = new float[MultiTouchDetector.MAX_TOUCH_POINTS];  // X
	private float[] mYs = new float[MultiTouchDetector.MAX_TOUCH_POINTS]; // Y
	private float[] mPressures = new float[MultiTouchDetector.MAX_TOUCH_POINTS]; // 压力值
	private int[] mPointerIds = new int[MultiTouchDetector.MAX_TOUCH_POINTS];

	// 多点触摸中点的x,y坐标和压力值
	private float mXMid, mYMid, mPressureMid;

	// Width/diameter/angle of pinch operations
	private float mDx; // x1 - x0
	private float mDy; // y1 - y0
	private float mDistance; // sqrt(dx ^ 2 + dy ^ 2)
	private float mDistanceSquare; // distance ^ 2;
	private float mDngle; // atan2(dx, dy)

	// Whether or not these fields have already been calculated, for caching
	// purposes
	private boolean mDistanceSqIsCalculated; // distance 是否计算
	private boolean mDistanceIsCalculated; // distanceSquare 是否计算
	private boolean mAngleIsCalculated; // angle 是否计算

	// Event action code and event time
	private int mAction;
	private long mEventTime;
	private boolean mIsDown;
	private boolean mIsMultiTouch;

	private boolean mIsUpdate = false;

	public MutilPointInfo() {
	}

	public MutilPointInfo(final MutilPointInfo other) {
		if (other != null) {
			set(other);
		}
	}

	/**
	 * <br>
	 * 功能简述:设置点的信息 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param numPoints
	 * @param x
	 * @param y
	 * @param pressure
	 * @param pointerIds
	 * @param action
	 * @param isDown
	 * @param eventTime
	 */
	public void set(int numPoints, float[] x, float[] y, float[] pressure, int[] pointerIds,
			int action, boolean isDown, long eventTime) {
		this.mEventTime = eventTime;
		this.mAction = action;
		this.mNumPoints = numPoints;
		for (int i = 0; i < numPoints; i++) {
			this.mXs[i] = x[i];
			this.mYs[i] = y[i];
			this.mPressures[i] = pressure[i];
			this.mPointerIds[i] = pointerIds[i];
		}

		this.mIsDown = isDown;
		this.mIsMultiTouch = numPoints >= 2;
		if (mIsMultiTouch) {
			mXMid = (x[0] + x[1]) * 0.5f;
			mYMid = (y[0] + y[1]) * 0.5f;
			mPressureMid = (pressure[0] + pressure[1]) * 0.5f;
			mDx = Math.abs(x[1] - x[0]);
			mDy = Math.abs(y[1] - y[0]);
		} else {
			// Single-touch event
			mXMid = x[0];
			mYMid = y[0];
			mPressureMid = pressure[0];
			mDx = mDy = 0.0f;
		}

		mDistanceSqIsCalculated = mDistanceIsCalculated = mAngleIsCalculated = false;  // 标志距离、距离平方、角度平方都还没计算
	}

	/**
	 * Copy all fields from one PointInfo class to another. PointInfo objects
	 * are volatile so you should use this if you want to keep track of the last
	 * touch event in your own code.
	 */
	public void set(MutilPointInfo other) {
		mIsUpdate = true;
		this.mNumPoints = other.mNumPoints;
		for (int i = 0; i < mNumPoints; i++) {
			this.mXs[i] = other.mXs[i];
			this.mYs[i] = other.mYs[i];
			this.mPressures[i] = other.mPressures[i];
			this.mPointerIds[i] = other.mPointerIds[i];
		}
		this.mXMid = other.mXMid;
		this.mYMid = other.mYMid;
		this.mPressureMid = other.mPressureMid;
		this.mDx = other.mDx;
		this.mDy = other.mDy;
		this.mDistance = other.mDistance;
		this.mDistanceSquare = other.mDistanceSquare;
		this.mDngle = other.mDngle;
		this.mIsDown = other.mIsDown;
		this.mAction = other.mAction;
		this.mIsMultiTouch = other.mIsMultiTouch;
		this.mDistanceIsCalculated = other.mDistanceIsCalculated;
		this.mDistanceSqIsCalculated = other.mDistanceSqIsCalculated;
		this.mAngleIsCalculated = other.mAngleIsCalculated;
		this.mEventTime = other.mEventTime;
	}

	/** True if number of touch points >= 2. */
	public boolean isMultiTouch() {
		return mIsMultiTouch;
	}

	public boolean isUpdate() {
		return mIsUpdate;
	}

	/** Difference between x coords of touchpoint 0 and 1. */
	public float getMultiTouchWidth() {
		return mIsMultiTouch ? mDx : 0.0f;
	}

	/** Difference between y coords of touchpoint 0 and 1. */
	public float getMultiTouchHeight() {
		return mIsMultiTouch ? mDy : 0.0f;
	}

	/**
	 * Fast integer sqrt, by Jim Ulery. Much faster than Math.sqrt() for
	 * integers. 注意：经过验证，该方法实际上是比Math.sqrt()慢10倍左右
	 */
	public static int juleryIsqrt(int val) {
		int temp, g = 0, b = 0x8000, bshft = 15;
		do {
			if (val >= (temp = ((g << 1) + b) << bshft--)) {
				g += b;
				val -= temp;
			}
		} while ((b >>= 1) > 0);
		return g;
	}

	public static int getDistance(float dx, float dy) {
		float distance = dx * dx + dy * dy;
		if (distance != 0.0f) {
			distance = juleryIsqrt((int) (256 * distance)) / 16.0f;
		}

		if (distance < dx) {
			distance = dx;
		}

		if (distance < dy) {
			distance = dy;
		}
		return (int) distance;
	}

	/**
	 * 弧度转角度
	 * 
	 * @param radian
	 * @return
	 */
	public static float radian2Degree(float radian) {
		return radian * 180.0f / (float) Math.PI;
	}

	/**
	 * Calculate the squared diameter of the multitouch event, and cache it. Use
	 * this if you don't need to perform the sqrt.
	 */
	public float getFingerDiameterSq() {  // 计算多点触摸直径的平方
		if (!mDistanceSqIsCalculated) {
			mDistanceSquare = mIsMultiTouch ? mDx * mDx + mDy * mDy : 0.0f;
			mDistanceSqIsCalculated = true;
		}
		return mDistanceSquare;
	}

	/**
	 * Calculate the diameter of the multitouch event, and cache it. Uses fast
	 * int sqrt but gives accuracy to 1/16px.
	 */
	public float getFingerDistance() { // 多点触摸手指的距离
		if (!mDistanceIsCalculated) {
			if (!mIsMultiTouch) {
				mDistance = 0.0f;
			} else {
				// Get 1/16 pixel's worth of subpixel accuracy, works on screens
				// up to 2048x2048
				// before we get overflow (at which point you can reduce or
				// eliminate subpix
				// accuracy, or use longs in julery_isqrt())
				float diamSq = getFingerDiameterSq();
				if (diamSq != 0.0f) {
					mDistance = juleryIsqrt((int) (256 * diamSq)) / 16.0f;  // 比Math.sqrt()更快的求开方的方法
				}

				// Make sure diameter is never less than dx or dy, for trig
				// purposes
				if (mDistance < mDx) {
					mDistance = mDx;
				}

				if (mDistance < mDy) {
					mDistance = mDy;
				}
			}
			mDistanceIsCalculated = true;
		}
		return mDistance;
	}

	/**
	 * Calculate the angle of a multitouch event, and cache it. Actually gives
	 * the smaller of the two angles between the x axis and the line between the
	 * two touchpoints, so range is [0,Math.PI/2]. Uses Math.atan2().
	 * 计算多指操作的角度，然后缓存 Math.atan2()接受两个参数x和y,方法如下:angel=Math.atan2(y,x) x 指定点的 x
	 * 坐标的数字。y 指定点的 y 坐标的数字。 计算出来的结果angel是一个弧度值,也可以表示相对直角三角形对角的角，其中 x 是临边边长，而 y
	 * 是对边边长。
	 */
	public float getFingerAngle() {
		if (!mAngleIsCalculated) {
			if (!mIsMultiTouch) {
				mDngle = 0.0f;
			} else {
				mDngle = (float) Math.atan2(mYs[1] - mYs[0], mXs[1] - mXs[0]);
			}
			mAngleIsCalculated = true;
		}
		return mDngle;
	}

	// -------------------------------------------------------------------------------------------------------------------------------------------

	/** Return the total number of touch points */
	public int getPointCount() {
		return mNumPoints;
	}

	/**
	 * Return the X coord of the first touch point if there's only one, or the
	 * midpoint between first and second touch points if two or more.
	 */
	public float getX() {
		return mXMid;
	}

	/**
	 * Return the array of X coords -- only the first getNumTouchPoints() of
	 * these is defined.
	 */
	public float[] getXs() {
		return mXs;
	}

	/**
	 * Return the X coord of the first touch point if there's only one, or the
	 * midpoint between first and second touch points if two or more.
	 */
	public float getY() {
		return mYMid;
	}

	/**
	 * Return the array of Y coords -- only the first getNumTouchPoints() of
	 * these is defined.
	 */
	public float[] getYs() {
		return mYs;
	}

	/**
	 * Return the array of pointer ids -- only the first getNumTouchPoints() of
	 * these is defined. These don't have to be all the numbers from 0 to
	 * getNumTouchPoints()-1 inclusive, numbers can be skipped if a finger is
	 * lifted and the touch sensor is capable of detecting that that particular
	 * touch point is no longer down. Note that a lot of sensors do not have
	 * this capability: when finger 1 is lifted up finger 2 becomes the new
	 * finger 1. However in theory these IDs can correct for that. Convert back
	 * to indices using MotionEvent.findPointerIndex().
	 */
	public int[] getPointerIds() {
		return mPointerIds;
	}

	/**
	 * Return the pressure the first touch point if there's only one, or the
	 * average pressure of first and second touch points if two or more.
	 */
	public float getPressure() {
		return mPressureMid;
	}

	/**
	 * Return the array of pressures -- only the first getNumTouchPoints() of
	 * these is defined.
	 */
	public float[] getPressures() {
		return mPressures;
	}

	public boolean isDown() {
		return mIsDown;
	}

	public int getAction() {
		return mAction;
	}

	public long getEventTime() {
		return mEventTime;
	}
}