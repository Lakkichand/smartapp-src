package com.jiubang.ggheart.appgame.gostore.base.component;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.Scroller;

import com.gau.go.launcherex.R;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager.AsyncImageLoadedCallBack;
import com.jiubang.ggheart.appgame.base.utils.AppGameDrawUtils;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreDisplayUtil;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 
 * <br>类描述:壁纸详情底部icon view
 * <br>功能详细描述:
 * 
 * @author  lijunye
 * @date  [2012-11-22]
 */
public class GoStoreDetailHorizontalScrollView extends HorizontalScrollView {
	private LinearLayout mLinearLayout = null;
	private int mPosition = 0;
	private GoStoreDetailRefreshListener mRefreshDetail = null;
	private int mLastSelectPostion = -1;
	private int mWidth = 0;
	private int mHeight = 0;
	private AsyncImageManager mImgManager;
	private Drawable mDefaultIcon;
	private Bitmap mDefaultBmp;
	private HashMap<Integer, Integer> mPageIndex;
	private int mCountEachPage = 0;
	private int mScrollWidth = 0;
	private int mPages = 0;
	private int mCurrentPage = -1;
	private Scroller mScroller = null;
	private Context mContext = null;
	private boolean mGoToDirectly = false;
	private ArrayList<String> mIcons = null;
	private int mCount = 0;
	
	
	private ArrayList<LinearLayout> mIconLayout = null;

	public GoStoreDetailHorizontalScrollView(Context context) {
		super(context);
		mContext = context;
		initView();
	}

	public GoStoreDetailHorizontalScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initView();
	}

	public GoStoreDetailHorizontalScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		initView();
	}
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
	}
	private void initView() {
		mImgManager = AsyncImageManager.getInstance();
		mDefaultIcon = getResources().getDrawable(R.drawable.appcenter_theme_detail_default_icon);
		BitmapDrawable bitmapDrawable = (BitmapDrawable) mDefaultIcon;
		Bitmap bitmap = bitmapDrawable.getBitmap();
		mDefaultBmp = AppGameDrawUtils.getInstance().createMaskBitmap(getContext(), bitmap);
		mScroller = new Scroller(mContext);
	}
	public void setChildWidth(int width) {
		mWidth = width;
		mScrollWidth = mWidth + GoStoreDisplayUtil.scalePxToMachine(getContext(), 2)
				+ GoStoreDisplayUtil.scalePxToMachine(getContext(), 7) * 2;
		mCountEachPage = DrawUtils.sWidthPixels / mScrollWidth;
	}
	public void setChildHeight(int height) {
		mHeight = height;
	}
	public void setListBean(ArrayList<String> icons, int position,
			GoStoreDetailRefreshListener detailRefreshListener) {
		mPosition = position;
		mRefreshDetail = detailRefreshListener;
		mIcons = icons;
		mCount = icons.size();
		loadIcon(icons);
	}

	private void loadIcon(final ArrayList<String> urls) {
		if (mLinearLayout == null) {
			mLinearLayout = (LinearLayout) findViewById(R.id.bottom_thumbnails_layout);
			mLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
			mLinearLayout.setGravity(Gravity.CENTER);
		}
		if (urls == null) {
			return;
		}
		int size = urls.size();
		mIconLayout = new ArrayList<LinearLayout>(size);
		mPages = size / mCountEachPage;
		if (size % mCountEachPage != 0) {
			++mPages;
		}
		int pageIndex = 0;
		mPageIndex = new HashMap<Integer, Integer>(size);
		int n = 0;
		int eachPage = 0;
		for (final String url : urls) {
			if (eachPage >= mCountEachPage) {
				pageIndex ++;
				eachPage = 0;
			}
			mPageIndex.put(n, pageIndex);
			n ++;
			eachPage ++;
			final ImageView imgView = new ImageView(getContext());
			LinearLayout layout = new LinearLayout(getContext());
			int width = mWidth + GoStoreDisplayUtil.scalePxToMachine(getContext(), 2);
			int height = mHeight + GoStoreDisplayUtil.scalePxToMachine(getContext(), 2);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
			int imgWidth = mWidth;
			int imgHeight = mHeight;
			LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(imgWidth, imgHeight);
			int margin = GoStoreDisplayUtil.scalePxToMachine(getContext(), 7);
			params.setMargins(margin, 0, margin, 0);
			layout.setLayoutParams(params);
			layout.setGravity(Gravity.CENTER);
			String imgName = String.valueOf(url.hashCode());
			imgView.setScaleType(ScaleType.FIT_CENTER);
			if (mPosition < mCountEachPage + 1) {
				if (n <= mCountEachPage + 1) {
					Bitmap bmp = mImgManager.loadImageForList(n - 1, LauncherEnv.Path.GOSTORE_ICON_PATH, imgName, url, true,
							false, AppGameDrawUtils.getInstance().mMaskIconOperator, new AsyncImageLoadedCallBack() {
								@Override
								public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
									imgView.setImageBitmap(imageBitmap);
								}
							});
					if (bmp != null) {
						imgView.setImageBitmap(bmp);
					} else {
						imgView.setImageBitmap(mDefaultBmp);
					}
				} else {
					imgView.setImageBitmap(mDefaultBmp);
				}
			} else {
				if (n - 1 <= mPosition + mCountEachPage || n - 1 >= mPosition - mCountEachPage) {
					Bitmap bmp = mImgManager.loadImage(LauncherEnv.Path.GOSTORE_ICON_PATH, imgName, url, true,
							false, AppGameDrawUtils.getInstance().mMaskIconOperator, new AsyncImageLoadedCallBack() {
								@Override
								public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
									imgView.setImageBitmap(imageBitmap);
								}
							});
					if (bmp != null) {
						imgView.setImageBitmap(bmp);
					} else {
						imgView.setImageBitmap(mDefaultBmp);
					}
				} else {
					imgView.setImageBitmap(mDefaultBmp);
				}
			}
			layout.setBackgroundResource(R.drawable.gostore_theme_detail_icons_selector);
			layout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					int position = urls.indexOf(url);
					if (position != mPosition) {
						mPosition = position;
						if (mRefreshDetail != null) {
							mRefreshDetail.refresh(position);
							setHightLight(position);
						}
					}
				}
			});
			mIconLayout.add(layout);
			layout.addView(imgView, imgParams);
			mLinearLayout.addView(layout);
		}
	}
	public int getCurrentViewX() {
		if (mPosition <= mLinearLayout.getChildCount()) {
			return (mWidth + GoStoreDisplayUtil.scalePxToMachine(getContext(), 2) + GoStoreDisplayUtil
					.scalePxToMachine(getContext(), 7) * 2) * mPosition;
		}
		return 0;
	}
	
	@Override
	public void computeScroll() {
		// 如果返回true，表示动画还没有结束
		// 因为前面startScroll，所以只有在startScroll完成时 才会为false
		if (mScroller.computeScrollOffset()) {
			// 产生了动画效果 每次滚动一点
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			// 刷新View 否则效果可能有误差
			postInvalidate();
		} else {
		}
	}
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
	}
	@Override
	public void scrollBy(int x, int y) {
		super.scrollBy(x, y);
	}
	
	private static final int TOUCH_STATE_REST = 0;
	private static final int TOUCH_STATE_SCROLLING = 1;
	private int mTouchState = TOUCH_STATE_REST;
	// --------------------------
	// 处理触摸事件 ~
	public final static int SNAP_VELOCITY = 400;
	private int mTouchSlop = 0;
	private float mLastionMotionX = 0;
	private float mLastX = 0;
	// 处理触摸的速率
	private VelocityTracker mVelocityTracker = null;

	// 这个感觉没什么作用 不管true还是false 都是会执行onTouchEvent的 因为子view里面onTouchEvent返回false了
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		final int action = ev.getAction();
		// 表示已经开始滑动了，不需要走该Action_MOVE方法了(第一次时可能调用)。
		if ((action == MotionEvent.ACTION_MOVE)
				&& (mTouchState != TOUCH_STATE_REST)) {
			return true;
		}

		final float x = ev.getX();

		switch (action) {
		case MotionEvent.ACTION_MOVE:
			final int xDiff = (int) Math.abs(mLastionMotionX - x);
			// 超过了最小滑动距离
			if (xDiff > mTouchSlop) {
				mTouchState = TOUCH_STATE_SCROLLING;
			}
			break;

		case MotionEvent.ACTION_DOWN:
			mLastionMotionX = x;
			mLastX = x;
			mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST
					: TOUCH_STATE_SCROLLING;

			break;

		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			mTouchState = TOUCH_STATE_REST;
			break;
		}
		return mTouchState != TOUCH_STATE_REST;
	}

	public boolean onTouchEvent(MotionEvent event) {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}

		mVelocityTracker.addMovement(event);
		try {
			super.onTouchEvent(event);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}

		// 手指位置地点
		float x = event.getX();

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			// 如果屏幕的动画还没结束，你就按下了，我们就结束该动画
			if (mScroller != null) {
				if (!mScroller.isFinished()) {
					mScroller.abortAnimation();
				}
			}

			mLastionMotionX = x;
			mLastX = x;
			break;
		case MotionEvent.ACTION_MOVE:
			int deltaX = (int) (mLastionMotionX - x);
			scrollBy(deltaX, 0);
			mLastionMotionX = x;

			break;
		case MotionEvent.ACTION_UP:

			final VelocityTracker velocityTracker = mVelocityTracker;
			velocityTracker.computeCurrentVelocity(1000);

			int velocityX = (int) velocityTracker.getXVelocity();

			// 滑动速率达到了一个标准(快速向右滑屏，返回上一个屏幕) 马上进行切屏处理
			if (velocityX > SNAP_VELOCITY && mCurrentPage > 0) {
				// Fling enough to move left
				snapToScreen(mCurrentPage - 1, false);
			}
			// 快速向左滑屏，返回下一个屏幕)
			else if (velocityX < -SNAP_VELOCITY
					&& mCurrentPage < (getChildCount() - 1)) {
				snapToScreen(mCurrentPage + 1, false);
			}
			// 以上为快速移动的 ，强制切换屏幕
			else {
				// 我们是缓慢移动的，因此先判断是保留在本屏幕还是到下一屏幕
				deltaX = (int) (x - mLastX);
				snapToDestination(deltaX);
				mLastX = x;
			}

			if (mVelocityTracker != null) {
				mVelocityTracker.recycle();
				mVelocityTracker = null;
			}
			
			mTouchState = TOUCH_STATE_REST;
			break;
		case MotionEvent.ACTION_CANCEL:
			mTouchState = TOUCH_STATE_REST;
			break;
		}

		return true;
	}
	
	/**
	 * 缓慢移动
	 */
	private void snapToDestination(int deltaX) {
		if (deltaX < 0) {
			int destScreen = (int) ((getScrollX() + (mCountEachPage * mScrollWidth) * 0.8)
					/ (mCountEachPage * mScrollWidth));
			snapToScreen(destScreen, false);
		} else if (deltaX > 0) {
			int destScreen = (int) ((getScrollX() - (mCountEachPage * mScrollWidth) * 0.8)
					/ (mCountEachPage * mScrollWidth));
			snapToScreen(destScreen, false);
		}
	}

	private void snapToScreen(int whichScreen, boolean isDirectly) {
		// 简单的移到目标屏幕，可能是当前屏或者下一屏幕
		// 需要再次滑动的距离 屏或者下一屏幕的继续滑动距离
		if (mCurrentPage < 0) {
			mCurrentPage = whichScreen;
		} else if (isDirectly) {
			mCurrentPage = whichScreen;
			mGoToDirectly = true;
		} else if (whichScreen > mCurrentPage) {
			mCurrentPage ++;
		} else if (whichScreen < mCurrentPage) {
			mCurrentPage --;
		}
		if (mCurrentPage > mPages) {
			mCurrentPage = mPages;
		} else if (mCurrentPage < 0) {
			mCurrentPage = 0;
		}
		int dx = mCurrentPage * mCountEachPage * mScrollWidth - getScrollX();
		if (mGoToDirectly) {
			mScroller.startScroll(getScrollX(), 0, dx, 0, 0);
			mGoToDirectly = false;
		} else {
			mScroller.startScroll(getScrollX(), 0, dx, 0, Math.abs(dx)
					* 4);
		}
		// 此时需要手动刷新View 否则没效果
		invalidate();
		for (int i = 0; i < mCount; i++) {
			if (mPageIndex != null) {
				final View v = mLinearLayout.getChildAt(i);
				final ImageView imageView = (ImageView) ((LinearLayout) v).getChildAt(0);
				String url = mIcons.get(i);
				if (mPageIndex.get(i) <= mCurrentPage + 1 && mPageIndex.get(i) >= mCurrentPage - 1) {
					String imgName = String.valueOf(url);
					Bitmap bmp = mImgManager.loadImageForList(i, LauncherEnv.Path.GOSTORE_ICON_PATH, imgName, url, true,
							false, AppGameDrawUtils.getInstance().mMaskIconOperator, new AsyncImageLoadedCallBack() {
								@Override
								public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
									imageView.setImageBitmap(imageBitmap);
								}
							});
					if (bmp != null) {
						imageView.setImageBitmap(bmp);
					} else {
						imageView.setImageBitmap(mDefaultBmp);
					}
				} else {
					try {
						imageView.setImageBitmap(mDefaultBmp);
					} catch (OutOfMemoryError e) {
						System.gc();
					}
					
				}
			}
		}
	}

	public void setHightLight(int position) {
		if (position >= 0 && position < mLinearLayout.getChildCount()) {
			mPosition = position;
			if (mPageIndex != null) {
				int page = mPageIndex.get(position);
				if (page != mCurrentPage && mLastSelectPostion != -1) {
					snapToScreen(page, true);
				}
				View v = mLinearLayout.getChildAt(position);
				if (v instanceof LinearLayout) {
					LinearLayout linearLayout = (LinearLayout) v;
					linearLayout.setBackgroundResource(R.drawable.appcenter_theme_detail_icon_click);
				}
				if (mLastSelectPostion == -1) {
					mLastSelectPostion = position;
					mGoToDirectly = true;
					snapToScreen(page, true);
					return;
				}
				v = mLinearLayout.getChildAt(mLastSelectPostion);
				if (v instanceof LinearLayout) {
					LinearLayout linearLayout = (LinearLayout) v;
					linearLayout.setBackgroundResource(R.drawable.gostore_theme_detail_icons_selector);
				}
				mLastSelectPostion = position;
			}
		}
	}
	
	public void recycle() {
		if (mIconLayout != null) {
			for (LinearLayout layout : mIconLayout) {
				View view = layout.getChildAt(0);
				if (view != null && view instanceof ImageView) {
					((ImageView) view).setImageBitmap(null);
				}
				layout.setOnClickListener(null);
				layout.removeAllViews();
			}
			mIconLayout.clear();
			mIconLayout = null;
		}
		if (mPageIndex != null) {
			mPageIndex.clear();
			mPageIndex = null;
		}
		if (mLinearLayout != null) {
			mLinearLayout.removeAllViews();
			mLinearLayout.setBackgroundDrawable(null);
			mLinearLayout = null;
		}
		
		if (mRefreshDetail != null) {
			mRefreshDetail = null;
		}
		if (mImgManager != null) {
			mImgManager.removeAllTask();
			mImgManager = null;
		}
	}
}
