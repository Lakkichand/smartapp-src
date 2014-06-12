//CHECKSTYLE:OFF
package com.jiubang.ggheart.components;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;

/**
 * a popup menu for icon operation
 * 
 * @author luoph
 * 
 */
public class QuickActionMenu implements OnClickListener, OnKeyListener, OnTouchListener {
	public interface onActionListener {
		void onActionClick(int action, Object target);
	}

	// 用来判断是否全屏的参数，目标view的高度超过这个值，就认为是全屏
	private final static float FULL_SCREEN_FACTOR = 0.8f;

	// 用于处理2.3系统上快速点击出现透明区域的问题，延迟PopupWindow消失时间
	private final static int DISMISS_HACK_INTERVAL = 50;

	private final Context mContext;
	private final LayoutInflater mInflater;
	// private final WindowManager mWindowManager;

	private PopupWindow mPopupWindow;
	protected View mContentView;

	private Object mTarget;
	private Rect mAnchor;
	private int mScreenWidth;
	private int mScreenHeight;
	private LinearLayout mItemGroup;

	private ImageView mArrowUp;
	private ImageView mArrowDown;

	private onActionListener mListener;
	private View mPView;
	private final Rect mScrollRect = new Rect();
	private int mArrowPaddingLeft = 0;
	private int mArrowPaddingRight = 0;

	private int offsetY = 0;

	public QuickActionMenu(Context context, Object target, Rect rect, View parent,
			onActionListener listener) {
		// 弹出快捷菜单前，回收一次内存
		OutOfMemoryHandler.handle();

		mListener = listener;
		mAnchor = rect;
		mContext = context;
		mTarget = target;
		mPView = parent;

		// mPopupWindow = new PopupWindow(mContext);
		WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		mScreenWidth = wm.getDefaultDisplay().getWidth();
		mScreenHeight = wm.getDefaultDisplay().getHeight();

		mInflater = LayoutInflater.from(mContext);
		mContentView = mInflater.inflate(R.layout.quickactionmenu, null);
		mContentView.setOnKeyListener(this);
		mContentView.setOnTouchListener(this);
		mPopupWindow = new PopupWindow(mContentView);

		mItemGroup = (LinearLayout) mContentView.findViewById(R.id.tracks);
		mArrowUp = (ImageView) mContentView.findViewById(R.id.arrow_up);
		mArrowDown = (ImageView) mContentView.findViewById(R.id.arrow_down);

		final Resources resources = mContext.getResources();
		mArrowPaddingLeft = resources.getDimensionPixelOffset(R.dimen.qa_arrow_padding_left);
		mArrowPaddingRight = resources.getDimensionPixelOffset(R.dimen.qa_arrow_padding_right);
	}

	/**
	 * Anything you want to have happen when created. Probably should create a
	 * view and setup the event listeners on child views.
	 */
	protected void onCreate() {
	}

	/**
	 * In case there is stuff to do right before displaying.
	 */
	protected void onShow() {
	}

	protected void preShow() {
		if (mContentView == null) {
			throw new IllegalStateException("setContentView was not called with a view to display.");
		}

		onShow();

		// if using PopupWindow#setBackgroundDrawable this is the only values of
		// the width and hight that make it work
		// otherwise you need to set the background of the mContentView
		// viewgroup
		// and set the popupwindow background to an empty BitmapDrawable

		mPopupWindow.setWidth(LayoutParams.WRAP_CONTENT);
		mPopupWindow.setHeight(LayoutParams.WRAP_CONTENT);
		mPopupWindow.setTouchable(true);
		mPopupWindow.setFocusable(true);
		mPopupWindow.setOutsideTouchable(true);
	}

	/**
	 * 添加菜单项
	 * 
	 * @param id
	 *            菜单项对应的Id
	 * @param iconRes
	 *            图标id
	 * @param txtRes
	 *            文本id
	 * 
	 */
	public void addItem(int id, int iconRes, int txtRes) {
		try {
			if (mItemGroup.getChildCount() != 0) {
				// 分割线
				ImageView line = new ImageView(mContext);
				line.setImageDrawable(mContext.getResources().getDrawable(
						R.drawable.quickaction_line));
				mItemGroup.addView(line);
			}
			DeskTextView itemView = (DeskTextView) mInflater.inflate(R.layout.quickactionitem,
					mItemGroup, false);
			itemView.setTag(new Integer(id));
			itemView.setFocusable(true);
			Drawable icon = mContext.getResources().getDrawable(iconRes);
			itemView.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
			itemView.setText(txtRes);
			itemView.setOnClickListener(this);

			itemView.setBackgroundResource(R.drawable.qa_background_change);
			mItemGroup.addView(itemView);

		} catch (Exception e) {
			Log.i("QuickActionMenu", "addItem() " + id + " has exception " + e.getMessage());
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
		}
	}
	
	/**
	 * 添加菜单项
	 * 
	 * @param id
	 *            菜单项对应的Id
	 * @param iconRes
	 *            图标id
	 * @param txtRes
	 *            文本id
	 * 用于3D插件
	 */
	
	public void addItem(int id, Drawable iconRes, String txtRes) {
		try {
			if (mItemGroup.getChildCount() != 0) {
				// 分割线
				ImageView line = new ImageView(mContext);
				line.setImageDrawable(mContext.getResources().getDrawable(
						R.drawable.quickaction_line));
				mItemGroup.addView(line);
			}
			DeskTextView itemView = (DeskTextView) mInflater.inflate(R.layout.quickactionitem,
					mItemGroup, false);
			itemView.setTag(new Integer(id));
			itemView.setFocusable(true);
			itemView.setCompoundDrawablesWithIntrinsicBounds(null, iconRes, null, null);
			itemView.setText(txtRes);
			itemView.setOnClickListener(this);

			itemView.setBackgroundResource(R.drawable.qa_background_change);
			mItemGroup.addView(itemView);

		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
		}
	}
	
	/**
	 * 添加菜单项(无图片)
	 * 
	 * @param id
	 *            菜单项对应的Id
	 * @param txtRes
	 *            文本id
	 * 
	 */
	public void addItem(int id, int txtRes) {
		try {
			DeskTextView itemView = (DeskTextView) mInflater
					.inflate(R.layout.quickactionitem, null);
			itemView.setTag(new Integer(id));
			itemView.setFocusable(true);
			itemView.setText(txtRes);
			itemView.setOnClickListener(this);
			mItemGroup.addView(itemView);
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
		}
	}

	/**
	 * Shows the quick actions window
	 */
	public void show() {
		if (isShowing()) {
			dismiss();
		}

		preShow();
		// 计算菜单实际宽高
		mContentView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		mContentView.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		mContentView.setFocusableInTouchMode(true);
		mContentView.setFocusable(true);
		mContentView.requestFocus();

		int rootHeight = mContentView.getMeasuredHeight();
		int rootWidth = mContentView.getMeasuredWidth();

		int center = mAnchor.centerX();
		int startX = center - rootWidth / 2;
		if (rootWidth > mScreenWidth) {
			startX = 0;
		}
		if (center + rootWidth / 2 > mScreenWidth) {
			startX = mScreenWidth - rootWidth;
		}

		if (startX < 0) {
			startX = 0;
		}

		final float targetHeightRatio = (float) mAnchor.height() / (float) mScreenHeight;
		boolean isFullScreen = targetHeightRatio > FULL_SCREEN_FACTOR;

		boolean onTop = true;
		int startY = 0;
		if (isFullScreen) {
			startY = mAnchor.centerY() - rootHeight / 2;
			onTop = false;
		} else {
			if (mAnchor.top > rootHeight + offsetY) {
				// 显示下箭头
				startY = mAnchor.top - rootHeight;
				onTop = true;
			} else {
				// 显示上箭头
				onTop = false;
				startY = mAnchor.bottom;
			}
		}
		showArrow(((onTop) ? R.id.arrow_down : R.id.arrow_up), center - startX, rootWidth);
		setAnimationStyle(onTop);
		mPopupWindow.showAtLocation(mPView, Gravity.NO_GRAVITY, startX, startY);
	}

	private void setAnimationStyle(boolean onTop) {
		if (onTop) {
			mPopupWindow.setAnimationStyle(R.style.QuickActionAboveAnimation);
		} else {
			mPopupWindow.setAnimationStyle(R.style.QuickActionBelowAnimation);
		}
	}

	/**
	 * 取消显示，调用此方法会有一个回调，事件id为IQuickActionId.CANCEL 如果不需要回调可以直接调用dismiss接口
	 */
	public void cancel() {
		dismiss();
		if (mListener != null) {
			mListener.onActionClick(IQuickActionId.CANCEL, mTarget);
		}
	}

	public void dismiss() {
		if (!isShowing()) {
			return;
		}

		// 把mItemGroup里的DeskView反注册
		int childCount = mItemGroup.getChildCount();
		for (int i = 0; i < childCount; i++) {
			View v = mItemGroup.getChildAt(i);
			if (v instanceof DeskTextView) {
				((DeskTextView) v).selfDestruct();
			}
		}

		/**
		 * 在2.3系统上快速点击会导致出现透明区域的问题 推迟对话框延迟消失
		 */
		mContentView.postDelayed(new Runnable() {
			@Override
			public void run() {
				mPopupWindow.dismiss();
			}
		}, DISMISS_HACK_INTERVAL);
	}

	public boolean isShowing() {
		return mPopupWindow.isShowing();
	}

	/**
	 * Shows the correct call-out arrow based on a {@link R.id} reference.
	 */
	private void showArrow(int whichArrow, int requestedX, int contentWidth) {
		final View showArrow = (whichArrow == R.id.arrow_up) ? mArrowUp : mArrowDown;
		final View hideArrow = (whichArrow == R.id.arrow_up) ? mArrowDown : mArrowUp;

		final int arrowWidth = mArrowUp.getMeasuredWidth();
		showArrow.setVisibility(View.VISIBLE);

		ViewGroup.MarginLayoutParams param = (ViewGroup.MarginLayoutParams) showArrow
				.getLayoutParams();

		// 限制左右边界,箭头的起始位置相对于mContentView而不是屏幕
		int leftMargin = requestedX - arrowWidth / 2;
		leftMargin = Math.max(mArrowPaddingLeft, leftMargin);
		if (leftMargin + arrowWidth > contentWidth - mArrowPaddingRight) {
			leftMargin = contentWidth - mArrowPaddingRight - arrowWidth;
		}

		param.leftMargin = leftMargin;
		hideArrow.setVisibility(View.GONE);
	}

	@Override
	public void onClick(View v) {
		dismiss();
		Object tag = v.getTag();
		if (tag != null && tag instanceof Integer) {
			int id = ((Integer) tag).intValue();

			if (mListener != null) {
				mListener.onActionClick(id, mTarget);
			}
		}
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			return false;
		}

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (isShowing()) {
				cancel();
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP) {
			int x = (int) event.getX();
			int y = (int) event.getY();
			mContentView.getHitRect(mScrollRect);
			if (isShowing() && !mScrollRect.contains(x, y)) {
				cancel();
				return true;
			}
		}
		return false;
	}

	public void setOffsetY(int value) {
		offsetY = value;
	}
}
