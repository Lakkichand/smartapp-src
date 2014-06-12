package com.jiubang.ggheart.appgame.base.component;

import java.util.HashMap;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Transformation;
import android.widget.Gallery;

import com.go.util.graphics.DrawUtils;
import com.go.util.lib.ViewCompatProxy;

/**
 * 用Gallery 模拟 converFlow 效果
 * @author zhoujun
 *
 */
public class GalleryView extends Gallery {

	private static final float MIN_SCALE = 0.35F;
	private static final float MAX_SCALE = 1.0F;
	//	private float c = 0.7F;

	private int mCenterOfGallery;
	/**
	 * gallery 默认的item间距
	 */
	private static final int DEFAULT_ITEM_SPACING = -83;
	/**
	 * 是否正在触摸gallery，如果是，不执行自动滚动
	 */
	private boolean mIsTouching = false;
	/**
	 * coverflow自动切换时间间隔
	 */
	private int mCoverFlowScrollInterval = 3000;
	/**
	 * 每隔5秒自动切换coverflow的banner图
	 */
	private Runnable mCoverFlowRunnable = new Runnable() {

		@Override
		public void run() {
			if (GalleryView.this != null && GalleryView.this.getParent() != null
					&& GalleryView.this.getVisibility() == View.VISIBLE) {
				if (!mIsTouching) {
					onKeyDown(KeyEvent.KEYCODE_DPAD_LEFT, null);
				}
				removeCallbacks(mCoverFlowRunnable);
				postDelayed(mCoverFlowRunnable, mCoverFlowScrollInterval);
			}
		}
	};

	/**
	 * 存放当前屏显示的每个view的缩放比例。便于算点击位置
	 */
	private HashMap<Integer, Float> mScaleMap = new HashMap<Integer, Float>();
	
	/**
	 * 刷新界面runnable
	 */
	private Runnable mInvalidateRunnable = new Runnable() {

		@Override
		public void run() {
			for (int i = getChildCount() - 1; i >= 0; i--) {
				View child = getChildAt(i);
				if (child != null) {
					child.invalidate();
				}
			}
		}
	};

	public GalleryView(Context paramContext) {
		super(paramContext);
		init();
	}

	public GalleryView(Context paramContext, AttributeSet paramAttributeSet) {
		super(paramContext, paramAttributeSet);
		init();
	}

	public GalleryView(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
		super(paramContext, paramAttributeSet, paramInt);
		init();
	}

	/**
	 * 初始化
	 */
	private void init() {
		setItemSpacing(DrawUtils.dip2px(DEFAULT_ITEM_SPACING));
		setAnimationDuration(400);
		postDelayed(mCoverFlowRunnable, mCoverFlowScrollInterval * 3);
	}

	/**
	 * 设置itme的间距
	 * @param spacing
	 */
	public void setItemSpacing(int spacing) {
		setStaticTransformationsEnabled(true);
		setSpacing(spacing);
	}

	private int getCenterOfGallery() {
		return (getWidth() - getPaddingLeft() - getPaddingRight()) / 2 + getPaddingLeft();
	}

	protected int getChildDrawingOrder(int childCount, int i) {
		int selectedIndex = this.getSelectedItemPosition() - this.getFirstVisiblePosition();
		if (i != childCount - 1) {
			if (i < selectedIndex) {
				selectedIndex = i;
			} else {
				selectedIndex = childCount - 1 - (i - selectedIndex);
			}
		}
		return selectedIndex;
	}

	protected boolean getChildStaticTransformation(final View paramView,
			Transformation paramTransformation) {
		int centerOfView = paramView.getLeft() + paramView.getWidth() / 2;
		paramTransformation.clear();
		paramTransformation.setTransformationType(Transformation.TYPE_BOTH);
		float f;
		//		if (Math.abs(centerOfView - this.mCenterOfGallery) <= paramView.getWidth()) {
		//			float f1 = (float) ((this.MIN_SCALE - this.c) / Math.pow(this.mCenterOfGallery, 2.0D)
		//					* Math.pow(paramView.getWidth(), 2.0D) + this.c)
		//					- MAX_SCALE;
		//			f = (float) (f1 / Math.pow(paramView.getWidth(), 2.0D)
		//					* Math.pow(centerOfView - this.mCenterOfGallery, 2.0D) + this.MAX_SCALE);
		//		} else {
		//			f = (float) ((this.MIN_SCALE - this.c) / Math.pow(this.mCenterOfGallery, 2.0D)
		//					* Math.pow(centerOfView - this.mCenterOfGallery, 2.0D) + this.c);
		//		}

		f = (float) ((-MIN_SCALE) / this.mCenterOfGallery
				* Math.abs(centerOfView - this.mCenterOfGallery) + MAX_SCALE);
		if (centerOfView > this.mCenterOfGallery) {
			paramTransformation.getMatrix().setScale(f, f, 0.0F, paramView.getHeight() / 2);
		} else {
			paramTransformation.getMatrix().setScale(f, f, paramView.getWidth(),
					paramView.getHeight() / 2);
		}
		if (mScaleMap != null) {
			int position = getPositionForView(paramView) - getFirstVisiblePosition();
			mScaleMap.put(position, f);
		}
		// android 4.1如果开启了硬件加速，会导致getChildStaticTransformation不会重复调用，所以要强制invalidate
		// http://code.google.com/p/android/issues/detail?id=35178
		if (android.os.Build.VERSION.SDK_INT >= 16 && ViewCompatProxy.isHardwareAccelerated(this)) {
			removeCallbacks(mInvalidateRunnable);
			postDelayed(mInvalidateRunnable, 20);
		}
		return true;
	}

	protected void onSizeChanged(int paramInt1, int paramInt2, int paramInt3, int paramInt4) {
		super.onSizeChanged(paramInt1, paramInt2, paramInt3, paramInt4);
		this.mCenterOfGallery = getCenterOfGallery();
	}

	public int pointToPosition(int x, int y) {
		int i = -1;
		int j = getChildCount();
		Rect localRect = new Rect();
		int centerPosition = this.getSelectedItemPosition() - this.getFirstVisiblePosition();
		if (x >= getCenterOfGallery()) {
			for (int n = centerPosition; n < j; n++) {
				if (contains(n, localRect, x, y)) {
					i = n + getFirstVisiblePosition();
					break;
				}
			}
		} else {
			for (int n = centerPosition; n >= 0; n--) {
				if (contains(n, localRect, x, y)) {
					i = n + getFirstVisiblePosition();
					break;
				}
			}
		}
		return i;
	}

	private boolean contains(int position, Rect localRect, int x, int y) {
		try {
			View localView = getChildAt(position);
			if (localView == null) {
				return false;
			}
			if (localView.getVisibility() == View.VISIBLE) {
				localView.getHitRect(localRect);
				int left = localRect.left;
				int right = localRect.right;
				int width = localRect.width();
				if (mScaleMap != null) {
					//这里会报空指针错误，需要加入保护 add by xiedezhi 2012.11.15
					if (!mScaleMap.containsKey(position)) {
						return false;
					}
					float f = mScaleMap.get(position);
					if (f > 0) {
						int scaleWidth = (int) (width * f);
						if (x >= getCenterOfGallery()) {
							right = right - (width - scaleWidth);
						} else {
							left = left + width - scaleWidth;
						}
						return contains(left, right, localRect.top, localRect.bottom, x, y);
					}
				}
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private boolean contains(int left, int right, int top, int bottom, int x, int y) {
		return left < right && top < bottom  // check for empty first
				&& x >= left && x < right && y >= top && y < bottom;
	}

	public void clear() {
		if (mScaleMap != null) {
			mScaleMap.clear();
		}
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		return super.onFling(e1, e2, velocityX * 0.3f, velocityY);
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		try {
			return super.onScroll(e1, e2, distanceX, distanceY);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	@Override
	protected void onVisibilityChanged(View changedView, int visibility) {
		//如果看见就postRunnable，不可见就removecallback
		if (visibility == View.VISIBLE) {
			removeCallbacks(mCoverFlowRunnable);
			postDelayed(mCoverFlowRunnable, mCoverFlowScrollInterval);
		} else {
			removeCallbacks(mCoverFlowRunnable);
		}
		super.onVisibilityChanged(changedView, visibility);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN :
				mIsTouching = true;
				removeCallbacks(mCoverFlowRunnable);
				break;
			case MotionEvent.ACTION_UP :
			case MotionEvent.ACTION_CANCEL :
			case MotionEvent.ACTION_OUTSIDE :
				mIsTouching = false;
				postDelayed(mCoverFlowRunnable, mCoverFlowScrollInterval);
				break;
			default :
				break;
		}
		return super.onTouchEvent(event);
	}
	
	@Override
	public void playSoundEffect(int soundConstant) {
//		super.playSoundEffect(soundConstant);
	}
}
