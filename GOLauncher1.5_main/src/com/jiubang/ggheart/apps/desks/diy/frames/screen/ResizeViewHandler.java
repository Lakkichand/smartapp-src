/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jiubang.ggheart.apps.desks.diy.frames.screen;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;

import com.gau.go.launcherex.R;
import com.go.util.device.Machine;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;

// This class is used by CropImage to display a highlighted cropping rectangle
// overlayed with the image. There are two coordinate spaces in use. One is
// image, another is screen. computeLayout() uses mMatrix to map from image
// space to screen space.
/**
 * ResizeViewHandler
 */
class ResizeViewHandler extends View {
	public static final int HIT_TYPE_NONE = -1; //初始值
	public static final int HIT_TYPE_LETF_TOP = 0; //左上
	public static final int HIT_TYPE_LETF_BOTTOM = 1; //左下
	public static final int HIT_TYPE_RIGHT_TOP = 2; //右上
	public static final int HIT_TYPE_RIGHT_BOTTOM = 3; //右下
	public int mHitType = HIT_TYPE_NONE; //点击的类型
	
	
	

	public ResizeViewHandler(Context context) {
		super(context);
	}

	@SuppressWarnings("unused")
	private static final String TAG = "HighlightView";

	public static final int GROW_NONE = 1 << 0;
	public static final int GROW_LEFT_EDGE = 1 << 1;
	public static final int GROW_RIGHT_EDGE = 1 << 2;
	public static final int GROW_TOP_EDGE = 1 << 3;
	public static final int GROW_BOTTOM_EDGE = 1 << 4;
	public static final int MOVE = 1 << 5;

	/**
	 * 初始化边上的4个图片
	 */
	private void initDdgeImage() {
		Resources resources = getResources();
		try {
			mResizeDrawableCommon = resources.getDrawable(R.drawable.camera_crop_resize_width);
			mResizeDrawableLeft = resources.getDrawable(R.drawable.camera_crop_width_left);
			mResizeDrawableRight = resources.getDrawable(R.drawable.camera_crop_width_right);
			mResizeDrawableTop = resources.getDrawable(R.drawable.camera_crop_width_top);
			mResizeDrawableBottom = resources.getDrawable(R.drawable.camera_crop_width_bottom);
			mResizeDrawableWidthRight = mResizeDrawableCommon;
			mResizeDrawableWidthLeft = mResizeDrawableCommon;
			mResizeDrawableHeightTop = mResizeDrawableCommon;
			mResizeDrawableHeightBottom = mResizeDrawableCommon;
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
		}
	}

	boolean mIsFocused;
	boolean mHidden;

	@Override
	public boolean hasFocus() {
		return mIsFocused;
	}

	public void setFocus(boolean f) {
		mIsFocused = f;
	}

	public void setHidden(boolean hidden) {
		mHidden = hidden;
	}

	@Override
	public void draw(Canvas canvas) {
		if (mHidden || mResizeDrawableWidthLeft == null || mResizeDrawableWidthRight == null
				|| mResizeDrawableHeightTop == null || mResizeDrawableHeightBottom == null) {
			return;
		}
		int saveid = canvas.save();
		Path path = new Path();
//		Rect viewDrawingRect = new Rect(); // 去除缩放widget时的遮罩
//		getDrawingRect(viewDrawingRect); // 去除缩放widget时的遮罩

		path.addRect(new RectF(mDrawRect), Path.Direction.CW);
//		mOutlinePaint.setColor(0xFFFF8A00);
		mOutlinePaint.setColor(0xFFBDFF00); // 新的边框颜色
		if (Machine.sLevelUnder3) {
			canvas.clipPath(path, Region.Op.DIFFERENCE);
		}
//		canvas.drawRect(viewDrawingRect, mNoFocusPaint); // 去除缩放widget时的遮罩

		canvas.restoreToCount(saveid);
		saveid = canvas.save();

		canvas.restoreToCount(saveid);
		canvas.drawPath(path, mOutlinePaint);

		int left = mDrawRect.left/* + 1 */;
		int right = mDrawRect.right /* + 1 */;
		int top = mDrawRect.top /* + 4 */;
		int bottom = mDrawRect.bottom /* + 4 */;

		int widthWidthLeft = mResizeDrawableWidthLeft.getIntrinsicWidth() / 2;
		int widthWidthRight = mResizeDrawableWidthRight.getIntrinsicWidth() / 2;
		int widthHeightLeft = mResizeDrawableWidthLeft.getIntrinsicHeight() / 2;
		int widthHeightRight = mResizeDrawableWidthRight.getIntrinsicHeight() / 2;
		int heightTopHeight = mResizeDrawableHeightTop.getIntrinsicHeight() / 2;
		int heightTopWidth = mResizeDrawableHeightTop.getIntrinsicWidth() / 2;
		int heightBottomHeight = mResizeDrawableHeightBottom.getIntrinsicHeight() / 2;
		int heightBottomWidth = mResizeDrawableHeightBottom.getIntrinsicWidth() / 2;

		int xMiddle = (mDrawRect.right + mDrawRect.left) / 2;
		int yMiddle = (mDrawRect.bottom + mDrawRect.top) / 2;

		mResizeDrawableWidthLeft.setBounds(left - widthWidthLeft, yMiddle - widthHeightLeft, left + widthWidthLeft,
				yMiddle + widthHeightLeft);
		mResizeDrawableWidthLeft.draw(canvas);

		mResizeDrawableWidthRight.setBounds(right - widthWidthRight, yMiddle - widthHeightRight, right
				+ widthWidthRight, yMiddle + widthHeightRight);
		mResizeDrawableWidthRight.draw(canvas);

		mResizeDrawableHeightTop.setBounds(xMiddle - heightTopWidth, top - heightTopHeight, xMiddle
				+ heightTopWidth, top + heightTopHeight);
		mResizeDrawableHeightTop.draw(canvas);

		mResizeDrawableHeightBottom.setBounds(xMiddle - heightBottomWidth, bottom - heightBottomHeight, xMiddle
				+ heightBottomWidth, bottom + heightBottomHeight);
		mResizeDrawableHeightBottom.draw(canvas);
		
//		if (mCollision && mStartReplayTime > 0) {
//			replayAnimation();
//		}
	}
	
//	float mDistanceToReplay = 0; // 需要回弹的距离
//	long mStartReplayTime = 0; //当前回弹的位置
	public void startReplyAnimation() {
//		mStartReplayTime = System.currentTimeMillis();
//		if((mMotionEdge & GROW_TOP_EDGE) == GROW_TOP_EDGE){
//			mDistanceToReplay = mCropRect.top - mOldCropRect.top;
//		}
		mCropRect.set(mOldCropRect);
		mOnValidateSizingListener.onValidateSize(mCropRect, mHitType);
		adjustCropRect();
		invalidate();
//		replayAnimation();
	}
	
	public void replayAnimation() {
//		if(System.currentTimeMillis() - mStartReplayTime > 300){
//			return;
//		}else{
//			if ((mMotionEdge & GROW_TOP_EDGE) == GROW_TOP_EDGE) {
//				mCropRect.top = mCropRect.top
//						- (mDistanceToReplay * (System.currentTimeMillis() - mStartReplayTime) / 300);
//			}
//			invalidate();
//		}
//		Log.v(TAG, "" + mCropRect.top);
	}
	
	public void setMode(ModifyMode mode) {
		if (mode != mMode) {
			mMode = mode;
			invalidate();
		}
	}

	/**
	 * 通过xy坐标点获取点击位置在那条边上（角上）
	 * @param x
	 * @param y
	 * @return
	 */
	public int getHit(float x, float y) {
		Rect r = computeLayout(); //获取裁剪区域
		final float hysteresis = 20F;
		int retval = GROW_NONE;

		//主要判断是否在上下左右中心点上，目前不需要只在中心点。屏蔽下面的
		boolean verticalCheck = true;
		boolean horizCheck = true;
//		boolean verticalCheck = (y >= r.top - mResizeDrawableHeightTop.getIntrinsicHeight() / 2) && (y < r.bottom + mResizeDrawableHeightBottom.getIntrinsicHeight() / 2);
//		boolean horizCheck = (x >= r.left - mResizeDrawableWidthLeft.getIntrinsicWidth() / 2) && (x < r.right + mResizeDrawableWidthRight.getIntrinsicWidth() / 2);
		
		if (r == null || mResizeDrawableWidthLeft == null || mResizeDrawableWidthRight == null
			|| mResizeDrawableHeightTop == null || mResizeDrawableHeightBottom == null) {
			return retval;
		}
		
		//用 |= 方法 判断点击那条边局，边距可以叠加
		if ((Math.abs(r.left - x) < mResizeDrawableWidthLeft.getIntrinsicWidth() / 2) && verticalCheck) {
			retval |= GROW_LEFT_EDGE;
		}
		if ((Math.abs(r.right - x) <  mResizeDrawableWidthRight.getIntrinsicWidth() / 2) && verticalCheck) {
			retval |= GROW_RIGHT_EDGE;
		}
		if ((Math.abs(r.top - y) < mResizeDrawableHeightTop.getIntrinsicHeight() / 2) && horizCheck) {
			retval |= GROW_TOP_EDGE;
		}
		if ((Math.abs(r.bottom - y) < mResizeDrawableHeightBottom.getIntrinsicHeight() / 2) && horizCheck) {
			retval |= GROW_BOTTOM_EDGE;
		}

		// 不在4个边缘上，且在裁剪区域内部
		if (retval == GROW_NONE && r.contains((int) x, (int) y)) {
			retval = MOVE;
		}

		return retval;
	}

	// Handles motion (dx, dy) in screen space.
	// The "edge" parameter specifies which edges the user is dragging.
	void handleMotion(int edge, float dx, float dy) {
		Rect r = computeLayout();
		if (edge == GROW_NONE) {
			return;
		} else if (edge == MOVE) {
			//在裁剪区域内，不处理
			// Convert to image space before sending to moveBy().
			// moveBy(dx * (mCropRect.width() / r.width()),
			// dy * (mCropRect.height() / r.height()));
		} else {
			//在4个边（4个角）上
			if (((GROW_LEFT_EDGE | GROW_RIGHT_EDGE) & edge) == 0) {
				dx = 0;
			}

			if (((GROW_TOP_EDGE | GROW_BOTTOM_EDGE) & edge) == 0) {
				dy = 0;
			}

			// Convert to image space before sending to growBy().
			float xDelta = dx * (mCropRect.width() / r.width()); // mCropRect.width()
																// /
																// r.width()比例系数基本为一
			float yDelta = dy * (mCropRect.height() / r.height());
			/*
			 * growBy((((edge & GROW_LEFT_EDGE) != 0) ? -1 : 1) * xDelta,
			 * (((edge & GROW_TOP_EDGE) != 0) ? -1 : 1) * yDelta);
			 */
			//遍历edge，判断哪些边需要进行修改
			if ((edge & GROW_LEFT_EDGE) == GROW_LEFT_EDGE) {
				growBy(-1 * xDelta, 0, true);
			} 
			if ((edge & GROW_RIGHT_EDGE) == GROW_RIGHT_EDGE) {
				growBy(1 * xDelta, 0, false);
			} 
			if ((edge & GROW_TOP_EDGE) == GROW_TOP_EDGE) {
				growBy(0, -1 * yDelta, true);
			} 
			if ((edge & GROW_BOTTOM_EDGE) == GROW_BOTTOM_EDGE) {
				growBy(0, 1 * yDelta, false);
			}
			
			setHitType(edge);
		}
	}
	
	/**
	 * 设置点击的类型
	 * @param edge
	 */
	public void setHitType(int edge) {
		mHitType = HIT_TYPE_NONE;
		
		//左上角
		if ((edge & GROW_LEFT_EDGE) == GROW_LEFT_EDGE && (edge & GROW_TOP_EDGE) == GROW_TOP_EDGE) {
			mHitType = HIT_TYPE_LETF_TOP;
		}
		
		//左下角
		else  if ((edge & GROW_LEFT_EDGE) == GROW_LEFT_EDGE && (edge & GROW_BOTTOM_EDGE) == GROW_BOTTOM_EDGE) {
			mHitType = HIT_TYPE_LETF_BOTTOM;
		} 
		
		//右上角
		else  if ((edge & GROW_RIGHT_EDGE) == GROW_RIGHT_EDGE && (edge & GROW_TOP_EDGE) == GROW_TOP_EDGE) {
			mHitType = HIT_TYPE_RIGHT_TOP;
		} 
		
		//右下角
		else  if ((edge & GROW_RIGHT_EDGE) == GROW_RIGHT_EDGE && (edge & GROW_BOTTOM_EDGE) == GROW_BOTTOM_EDGE) {
			mHitType = HIT_TYPE_RIGHT_BOTTOM;
		} 
		
		if (mHitType == HIT_TYPE_NONE) {
			//如果点击的是左边/上边,当作左上角
			if ((edge & GROW_LEFT_EDGE) == GROW_LEFT_EDGE || (edge & GROW_TOP_EDGE) == GROW_TOP_EDGE) {
				mHitType = HIT_TYPE_LETF_TOP;
			}
			
			//如果点击的是右边/下边,当作右下角
			else if ((edge & GROW_RIGHT_EDGE) == GROW_RIGHT_EDGE || (edge & GROW_BOTTOM_EDGE) == GROW_BOTTOM_EDGE) {
				mHitType = HIT_TYPE_RIGHT_BOTTOM;
			} 
		}
	}

	// Grows the cropping rectange by (dx, dy) in image space.
	/*
	 * void moveBy(float dx, float dy) { Rect invalRect = new Rect(mDrawRect);
	 * //截图框x、y坐标均加上偏移量 mCropRect.offset(dx, dy);
	 * 
	 * // Put the cropping rectangle inside image rectangle. mCropRect.offset(
	 * Math.max(0, mImageRect.left - mCropRect.left), Math.max(0, mImageRect.top
	 * - mCropRect.top));
	 * 
	 * mCropRect.offset( Math.min(0, mImageRect.right - mCropRect.right),
	 * Math.min(0, mImageRect.bottom - mCropRect.bottom));
	 * 
	 * mDrawRect = computeLayout();
	 * //更新这个矩形使它包含本身和指定的矩形。如果指定的矩形是空的,什么都不做。如果这个矩形是空的它被设定为指定的矩形。
	 * invalRect.union(mDrawRect); //正数为缩小指定矩形，负数为放大 invalRect.inset(-10, -10);
	 * //刷新一个矩形区域 invalidate(invalRect); }
	 */

	// Grows the cropping rectange by (dx, dy) in image space.
	void growBy(float dx, float dy, boolean from) {

		if (mMaintainAspectRatio) {
			if (dx != 0) {
				dy = dx / mInitialAspectRatio;
			} else if (dy != 0) {
				dx = dy * mInitialAspectRatio;
			}
		}

		// Don't let the cropping rectangle grow too fast.
		// Grow at most half of the difference between the image rectangle and
		// the cropping rectangle.
		RectF r = new RectF(mCropRect);
		if (dx > 0F && r.width() + 2 * dx > mImageRect.width()) {
			float adjustment = (mImageRect.width() - r.width()) / 2F;
			dx = adjustment;
			if (mMaintainAspectRatio) {
				dy = dx / mInitialAspectRatio;
			}
		}
		if (dy > 0F && r.height() + 2 * dy > mImageRect.height()) {
			float adjustment = (mImageRect.height() - r.height()) / 2F;
			dy = adjustment;
			if (mMaintainAspectRatio) {
				dx = dy * mInitialAspectRatio;
			}
		}

		// r.inset(-dx, -dy);
		if (from) {
			r.set(r.left - dx, r.top - dy, r.right, r.bottom);
			// 拉动的是左边或者是顶边
//			mdragBottomOrRight = false; //4个角都要处理。所以这个注掉 ， 在外部处理
		} else {
			// 不可进入Dock栏范围
			float index_y = r.bottom + dy > mImageRect.bottom ? r.bottom : r.bottom + dy;
			float index_x = r.right + dx > mImageRect.right ? r.right : r.right + dx;
			r.set(r.left, r.top, index_x, index_y);
			// r.set(r.left, r.top,r.right+dx,r.bottom+dy);
			// 拉动的是底边或者是右边
//			mdragBottomOrRight = true; //4个角都要处理。所以这个注掉
		}
		if (r.width() < mMinWidth) {
			r.left = mCropRect.left;
			r.right = mCropRect.right;
		}
		if (r.height() < mMinHeight) {
			r.top = mCropRect.top;
			r.bottom = mCropRect.bottom;
		}
		mCropRect.set(r);
		// Put the cropping rectangle inside the image rectangle.
		// adjustCropRect();

		// 以下一行语句是否多余？
		// mDrawRect = computeLayout();
		invalidate();
		// 这行语句暂时无操作
		dispatchTriggerEvent(mCropRect);
	}

	// Returns the cropping rectangle in image space.
	public Rect getCropRect() {
		return new Rect((int) mCropRect.left, (int) mCropRect.top, (int) mCropRect.right,
				(int) mCropRect.bottom);
	}

	// Maps the cropping rectangle from image space to screen space.
	private Rect computeLayout() {
		RectF r = new RectF(mCropRect.left, mCropRect.top, mCropRect.right, mCropRect.bottom);

		if (mMatrix != null) {
			mMatrix.mapRect(r);
		}
		return new Rect(Math.round(r.left), Math.round(r.top), Math.round(r.right),
				Math.round(r.bottom));
	}

	@Override
	public void invalidate() {
		mDrawRect = computeLayout();
		super.invalidate();
	}

	public void setup(Matrix m, RectF imageRect, RectF cropRect, boolean circle,
			boolean maintainAspectRatio, float minWidth, float minHeight) {
		if (circle) {
			maintainAspectRatio = true;
		}
		initDdgeImage(); //初始化4个变上的图片
		mMatrix = new Matrix(m);

		if (imageRect != null) {
			mImageRect.set(imageRect); //一个CellLayout的矩形区域
		}
		if (cropRect != null) {
			mCropRect.set(cropRect); //设置新的裁剪区域
			mOldCropRect.set(cropRect); //设置旧的裁剪区域
			adjustCropRect();
		}

		mMaintainAspectRatio = maintainAspectRatio;

		mInitialAspectRatio = mCropRect.width() / mCropRect.height();
		mDrawRect = computeLayout();

		mFocusPaint.setARGB(125, 255, 50, 50);
		mNoFocusPaint.setARGB(125, 50, 50, 50);
		mOutlinePaint.setStrokeWidth(3F);
		mOutlinePaint.setStyle(Paint.Style.STROKE);
		mOutlinePaint.setAntiAlias(true);

		mMode = ModifyMode.None;
		mMinWidth = 0; //minWidth; // 为了缩到最小（1xM或者Mx1格）时缩放框还可以缩
		mMinHeight = 0; //minHeight;
		requestLayout();
	}

	/**
	 * ModifyMode
	 * @author Administrator
	 *
	 */
	enum ModifyMode {
		None, Move, Grow
	}

	private ModifyMode mMode = ModifyMode.None;

	Rect mDrawRect = new Rect(); // in screen space
	private RectF mImageRect = new RectF(); // in image space,一个CellLayout的矩形区域
	RectF mCropRect = new RectF(); // in image space
	RectF mOldCropRect = new RectF();
	Matrix mMatrix;

	private boolean mMaintainAspectRatio = false;
	private float mInitialAspectRatio; // 宽和高的比例

	private Drawable mResizeDrawableWidthLeft; // 横向边框图标
	private Drawable mResizeDrawableWidthRight;
	private Drawable mResizeDrawableHeightTop; // 纵向边框图标
	private Drawable mResizeDrawableHeightBottom;
	private Drawable mResizeDrawableCommon;
	private Drawable mResizeDrawableLeft;
	private Drawable mResizeDrawableRight;
	private Drawable mResizeDrawableTop;
	private Drawable mResizeDrawableBottom;

	private final Paint mFocusPaint = new Paint();
	private final Paint mNoFocusPaint = new Paint();
	private final Paint mOutlinePaint = new Paint();
	private float mLastX;
	private float mLastY;
	private int mMotionEdge; //点击的边缘的类型（上下左右、4个角）
	private float mMinWidth = 25F;
	private float mMinHeight = 25F;

	private OnSizeChangedListener mOnTriggerListener = null;
	private OnValidateSizeListener mOnValidateSizingListener = null;
	// 拉动的是否为底边（右边）的标识
	private boolean mdragBottomOrRight;
	private boolean mCollision = false;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN :
				int edge = getHit(event.getX(), event.getY()); //通过xy坐标点获取点击位置在那条边上（角上）
				mMotionEdge = edge;
				if (edge != GROW_NONE) {
					mLastX = event.getX();
					mLastY = event.getY();
					setMode((edge == MOVE) ? ModifyMode.Move : ModifyMode.Grow);
					break;
				} else {
					//不在区域范围内就取消编辑
					GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.WIDGET_EDIT_FRAME_STOP_EIDT, -1, null, null);
				}
				break;
			case MotionEvent.ACTION_UP :
				setMode(ModifyMode.None);
				//只有大小有变化才进行处理
				if (mHitType != HIT_TYPE_NONE) {
					dispatchValidateSizingRect();
				}
				break;
			case MotionEvent.ACTION_MOVE :
				boolean outOfSide = checkMoveOutOfSide(mMotionEdge, event.getX(), event.getY());
				if (!outOfSide) {
					handleMotion(mMotionEdge, event.getX() - mLastX, event.getY() - mLastY);
					mLastX = event.getX();
					mLastY = event.getY();
				}
				
				break;
		}
		return true;
	}

	/**
	 * 判断是否超出边界
	 * @param edge
	 * @param dx
	 * @param dy
	 * @return
	 */
	public boolean checkMoveOutOfSide(int edge , float dx, float dy) {
		if ((edge & GROW_LEFT_EDGE) == GROW_LEFT_EDGE) {
			if (dx >= (float) mCropRect.right) {
				return true;
			}
		} 
		
		if ((edge & GROW_RIGHT_EDGE) == GROW_RIGHT_EDGE) {
			if (dx <= (float) mCropRect.left) {
				return true;
			}
		} 
		
		if ((edge & GROW_TOP_EDGE) == GROW_TOP_EDGE) {
			if (dy >= (float) mCropRect.bottom) {
				return true;
			}
		} 
		
		if ((edge & GROW_BOTTOM_EDGE) == GROW_BOTTOM_EDGE) {
			if (dy <= (float) mCropRect.top) {
				return true;
			}
		}
		
		return false;
	}
	
	
	public void setColliding(boolean colliding) {
		mCollision = colliding;
	}

	public void setOnValidateSizingRect(OnValidateSizeListener listener) {
		mOnValidateSizingListener = listener;
	}

	private void dispatchValidateSizingRect() {
		mCollision = false;
		if (mOnValidateSizingListener != null) {
			adjustCropRect();
			mOnValidateSizingListener.onValidateSize(mCropRect, mHitType);
			// adjust之前，bug已经出现
			if (mCollision) {
				startReplyAnimation();
			} else {
				mOldCropRect.set(mCropRect);
				adjustCropRect();
				invalidate();
			}

		}
	}

	public void setOnSizeChangedListener(OnSizeChangedListener listener) {
		mOnTriggerListener = listener;
	}

	/**
	 * Dispatches a trigger event to listener. Ignored if a listener is not set.
	 * 
	 * @param whichHandle
	 *            the handle that triggered the event.
	 */
	private void dispatchTriggerEvent(RectF r) {
		if (mOnTriggerListener != null) {
			mOnTriggerListener.onTrigger(r);
		}
	}

	/**
	 * Interface definition for a callback to be invoked when a resize triggered
	 * by moving the handlers beyond a threshold.
	 */
	public interface OnSizeChangedListener {
		/**
		 * Called when the user moves a handle beyond the threshold.
		 * 
		 * @param r
		 *            : the resulting Rect
		 */
		void onTrigger(RectF r);
	}

	/**
	 * OnValidateSizeListener
	 * @author Administrator
	 *
	 */
	public interface OnValidateSizeListener {
		void onValidateSize(RectF rect, int hitType);
	}

	private void adjustCropRect() {
		if (mImageRect == null || mCropRect == null) {
			return;
		}

		final RectF r = mCropRect;
		// mImageRect的坐标基本保持不变left=0，top = 8，right = 320，bottom = 403；
		r.offset(mImageRect.left, mImageRect.top);
		if (r.left < mImageRect.left) {
			r.left = mImageRect.left;
		}
		if (r.top < mImageRect.top) {
			r.top = mImageRect.top;
		}
		if (r.right > mImageRect.right) {
			r.right = mImageRect.right;
		}
		if (r.bottom > mImageRect.bottom) {
			r.bottom = mImageRect.bottom;
		}
		changeRightLeftDrawable();
	}
	
	private void changeRightLeftDrawable() {
		final RectF r = mCropRect;
		if (r.left <= mImageRect.left + 5) {
			mResizeDrawableWidthLeft = mResizeDrawableLeft;
		} else {
			mResizeDrawableWidthLeft = mResizeDrawableCommon;
		}
		if (r.right >= mImageRect.right - 5) {
			mResizeDrawableWidthRight = mResizeDrawableRight;
		} else {
			mResizeDrawableWidthRight = mResizeDrawableCommon;
		}
		if (r.top <= mImageRect.top + 5) {
			mResizeDrawableHeightTop = mResizeDrawableTop;
		} else {
			mResizeDrawableHeightTop = mResizeDrawableCommon;
		}
		if (r.bottom >= mImageRect.bottom - 5) {
			mResizeDrawableHeightBottom = mResizeDrawableBottom;
		} else {
			mResizeDrawableHeightBottom = mResizeDrawableCommon;
		}
	}
}