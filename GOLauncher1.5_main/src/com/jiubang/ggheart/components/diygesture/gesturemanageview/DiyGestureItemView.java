package com.jiubang.ggheart.components.diygesture.gesturemanageview;

import android.content.Context;
import android.content.res.TypedArray;
import android.gesture.Gesture;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.gau.go.launcherex.R;

/**
 * 手势预览界面控件
 * 
 * @author licanhui
 */
public class DiyGestureItemView extends View {
	private int mWidth; // 当前图片的宽度
	private int mHeight; // 当前图片的高度
	private Paint mPaint; // 画笔
	private float mStrokeWidth; // 画笔大小
	private Path mGesturePath; // 获取手势路径
	private float mDx; // X坐标偏移量
	private float mDy; // Y坐标偏移量
	private float mScale; // 缩放比例
	private boolean mIsSmallPreview; // 是否小预览图
	private boolean mIsStartOneAnimation = false; // 是否单个手势做动画刷新
	private boolean mIsMoveToCenter = false; // 是否居中

	public DiyGestureItemView(Context context) {
		super(context);
	}

	public DiyGestureItemView(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DiyGestureItemView);
		mIsSmallPreview = a.getBoolean(R.styleable.DiyGestureItemView_is_small_preview, true);
		mStrokeWidth = a.getDimension(R.styleable.DiyGestureItemView_strokeWidth, context
				.getResources().getDimension(R.dimen.gesture_preview_width));
		mIsMoveToCenter = a.getBoolean(R.styleable.DiyGestureItemView_is_move_center, false);
		int paintColor = a.getColor(R.styleable.DiyGestureItemView_paintColor, context
				.getResources().getColor(R.color.gesture_draw_color));

		// 设置画笔信息
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setColor(paintColor);

		a.recycle();
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		mWidth = right - left;
		mHeight = bottom - top;
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		// 显示全部手势
		if (!mIsStartOneAnimation) {
			final RectF bounds = new RectF();
			mGesturePath.computeBounds(bounds, true);

			mScale = 1;
			if (bounds.width() > mWidth || bounds.height() > mHeight) {
				final float sx = (mWidth - 8) / bounds.width();
				final float sy = (mHeight - 8) / bounds.height();
				mScale = sx > sy ? sy : sx;
			}

			if (mIsSmallPreview) {
				mPaint.setStrokeWidth(mStrokeWidth / mScale); // 判断是否小预览图，小图的要特殊处理，不然画笔会太大
			} else {
				mPaint.setStrokeWidth(mStrokeWidth);
			}

			mDx = (mWidth - bounds.width() * mScale) / 2 - bounds.left * mScale;
			mDy = (mHeight - bounds.height() * mScale) / 2 - bounds.top * mScale;
			if (mIsMoveToCenter) {
				canvas.translate(mDx, mDy);
			} else {
				if (bounds.left <= 0 || bounds.top <= 0 || bounds.right > mWidth
						|| bounds.bottom > mHeight) {
					canvas.translate(mDx, mDy); // 大图如果超级边界才需要居中
				}
			}
			canvas.scale(mScale, mScale);
			canvas.drawPath(mGesturePath, mPaint);
		} else {
			canvas.translate(mDx, mDy);
			canvas.scale(mScale, mScale);
			canvas.drawPath(mGesturePath, mPaint);
		}
	}

	/**
	 * 设置手势预览图片
	 */
	public void setGestureImageView(Gesture gesture) {
		if (gesture != null) {
			mIsStartOneAnimation = false;
			mGesturePath = gesture.toPath();
			invalidate();
		}
	}

	/**
	 * 手势动画
	 * 
	 * @param path
	 */
	public void updateGestureAnimation(Path path) {
		if (path != null) {
			mIsStartOneAnimation = true;
			mGesturePath = path;
			invalidate();
		}
	}

	/**
	 * 设置图像是否居中
	 * 
	 * @param mIsMoveToCenter
	 */
	public void setIsMoveToCenter(boolean isMoveToCenter) {
		this.mIsMoveToCenter = isMoveToCenter;
	}
}
