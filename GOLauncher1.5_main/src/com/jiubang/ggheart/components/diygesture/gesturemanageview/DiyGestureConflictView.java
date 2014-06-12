package com.jiubang.ggheart.components.diygesture.gesturemanageview;

import android.content.Context;
import android.gesture.Gesture;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

import com.gau.go.launcherex.R;

/**
 * 冲突界面控件
 * 
 * @author licanhui
 */
public class DiyGestureConflictView extends View {
	private int mWidth; // 当前图片的宽度
	private int mHeight; // 当前图片的高度
	private int mConflictViewHeight; // 冲突布局的界面高度
	private Paint mPaint; // 画笔
	private int mDrawColor = 0; // 画笔颜色
	private Context mContext;
	private long mAnimationStartTime = 0; // 动画开始时间
	private final int mAnimationDurationTime = 300; // 动画持续时间
	private Path mGesturePath; // 获取画笔路径
	private RectF mGesturePathBounds; // 获取画笔路径区域

	private DiyGestureConflictAnimationListner mConflictListner; // 动感完成监听器，动画完成后显示冲突界面

	public DiyGestureConflictView(Context context) {
		super(context);
	}

	public DiyGestureConflictView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mConflictViewHeight = (int) mContext.getResources().getDimension(
				R.dimen.gesture_conflict_view_height);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		mWidth = right - left;
		mHeight = bottom - top - mConflictViewHeight; // 减去冲突布局的高度
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (mGesturePath == null || mGesturePathBounds == null) {
			return;
		}

		int currentTime = 0;
		if (mAnimationStartTime == 0) {
			mAnimationStartTime = SystemClock.uptimeMillis();
		} else {
			currentTime = (int) (SystemClock.uptimeMillis() - mAnimationStartTime);
		}

		final float time = currentTime >= mAnimationDurationTime ? 1 : currentTime
				/ (float) mAnimationDurationTime;

		// 获取画笔颜色，其实颜色：(157,205,0),终止颜色:(181,181,181)
		int Rcolor = (int) DiyGestureConstants.easeOut(157, 181, time);
		int GRolor = (int) DiyGestureConstants.easeOut(205, 181, time);
		int BRolor = (int) DiyGestureConstants.easeOut(0, 181, time);
		mDrawColor = Color.rgb(Rcolor, GRolor, BRolor);
		mPaint.setColor(mDrawColor);

		float scale = 1;
		float currentScale = 1;

		if (mGesturePathBounds.width() > mWidth || mGesturePathBounds.height() > mHeight) {
			float scaleW = (mWidth - 10) / mGesturePathBounds.width(); // - 10
																		// 是防止截取了部分边界
			float scaleH = (mHeight - 10) / mGesturePathBounds.height();
			scale = scaleW > scaleH ? scaleH : scaleW; // 判断哪个比例更小
			currentScale = DiyGestureConstants.easeOut(1, scale, time); // 当前缩放比例
		}
		float x = (int) DiyGestureConstants
				.easeOut(0, (mWidth - mGesturePathBounds.width() * scale) / 2
						- mGesturePathBounds.left * scale, time);
		float y = (int) DiyGestureConstants.easeOut(0, (mHeight - mGesturePathBounds.height()
				* scale)
				/ 2 - mGesturePathBounds.top * scale, time);

		canvas.save();
		canvas.translate(x, y); // 移动必须放到缩放前面
		canvas.scale(currentScale, currentScale);
		canvas.drawPath(mGesturePath, mPaint);
		canvas.restore();

		if (currentTime > mAnimationDurationTime) {
			mConflictListner.setConflictViewVisable(); // 动感完成监听器，动画完成后显示冲突界面
			return;
		}
		postInvalidate(); // 重复刷新界面
	}

	/**
	 * 开始动画
	 */
	public void startConflictAnimation(Float strokeWidth, Gesture gesture) {
		if (gesture == null) {
			return;
		}
		mAnimationStartTime = 0;

		// 设置画笔信息
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setStrokeWidth(strokeWidth);

		mGesturePath = gesture.toPath(); // 获取画笔路径
		mGesturePathBounds = new RectF(); // 设置画笔区域
		mGesturePath.computeBounds(mGesturePathBounds, true);
		invalidate();
	}

	/**
	 * 设置动画完成的监听器
	 * 
	 * @param conflictListner
	 */
	public void setConflictAnimationListner(DiyGestureConflictAnimationListner conflictListner) {
		mConflictListner = conflictListner;
	}
}
