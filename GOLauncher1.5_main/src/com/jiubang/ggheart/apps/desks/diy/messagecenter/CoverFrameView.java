/*
 * 文 件 名:  CoverFrameView.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  rongjinsong
 * 修改时间:  2012-10-16
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.apps.desks.diy.messagecenter;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.Beans.MessageListBean.MessageHeadBean;

/**
 * <br>类描述:消息中心添加到罩子层的自定义view
 * <br>功能详细描述:主要包含进场和表演动画
 * 
 * @author  rongjinsong
 * @date  [2012-10-16]
 */
public class CoverFrameView extends View {

	private ArrayList<Bitmap> mEnterRes; // 进场动画资源
	private ArrayList<Bitmap> mActingRes; // 表演动画资源
	private MessageHeadBean mHead;
	private Animation mEnterAnimation;
	private int mShowStatus; //显示状态
	private static final int STATUS_ENTER = 0; //正在进场
	private static final int STATUS_ACTING = 1; //正在表演
	private static final int STATUS_IMMOBILE = 2; //静止状态
	private int mEnterIndex = 0;
	private int mActingIndex = 0;
	private int mLeft;
	private int mRight;
	private int mTop;
	private int mBottom;

	private static final int LEFT_TOP = 1;
	private static final int LEFT_MIDDLE = 2;
	private static final int RIGHT_TOP = 3;
	private static final int RIGHT_MIDDLE = 4;
	private final static int DEFAULT_SHAKE_TIMES = 4; //默认信封抖动次数
	private static final int CORNER_MARGIN_TOP = 10;
	private static final int VIEW_UPDATE_TIME = 300;

	private boolean mIsDefaultRes;
	private Bitmap mDefaultActingBmp1;
	private Bitmap mDefaultActingBmp2;
	private Bitmap mDefaultImmobileBmp;
	private Bitmap mDefaultCloseBmp;
	private Rect mDefaultCloseArea;
	private boolean mHasPost = false;
	/** <默认构造函数>
	 */
	public CoverFrameView(Context context, MessageHeadBean head, ArrayList<Bitmap> enterRes,
			ArrayList<Bitmap> showRes) {
		super(context);
		// TODO Auto-generated constructor stub
		mHead = head;
		if (enterRes != null && showRes != null && !enterRes.isEmpty() && !showRes.isEmpty()) {
			mEnterRes = (ArrayList<Bitmap>) enterRes.clone(); // 拷贝一份防止异步线程改变了传入的参数
			mActingRes = (ArrayList<Bitmap>) showRes.clone();
		} else {
			mIsDefaultRes = true;
		}
		init();
	}

	public void setMessageHead(MessageHeadBean bean) {
		mHead = bean;
		if (mHead != null && mHead.mIsColsed) {
			mDefaultCloseBmp = ((BitmapDrawable) getContext().getResources().getDrawable(
					R.drawable.cover_frame_close)).getBitmap();
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		if (mShowStatus == STATUS_ENTER) {
			if (mIsDefaultRes) {
				if (mDefaultImmobileBmp != null && !mDefaultImmobileBmp.isRecycled()) {
					canvas.drawBitmap(mDefaultImmobileBmp, 0, 0, null);
				}
			} else if (mEnterRes != null) {
				if (mEnterIndex < mEnterRes.size()) {
					canvas.drawBitmap(mEnterRes.get(mEnterIndex), 0, 0, null);
					mEnterIndex++;
					invalidate();
				} else {
					canvas.drawBitmap(mEnterRes.get(mEnterRes.size() - 1), 0, 0, null);
				}
			}
		} else if (mShowStatus == STATUS_ACTING) {
			if ((mActingRes != null && mActingIndex < mActingRes.size())
					|| (mIsDefaultRes && mActingIndex < DEFAULT_SHAKE_TIMES)) {
				mLeft = 0;
				Bitmap showBmp = null;
				if (mIsDefaultRes) {
					if (mActingIndex % 2 == 0) {
						showBmp = mDefaultActingBmp1;
					} else {
						showBmp = mDefaultActingBmp2;
					}
				} else {
					showBmp = mActingRes.get(mActingIndex);
				}
				if (showBmp != null && !showBmp.isRecycled()) {
					if (mHead.mZpos == RIGHT_MIDDLE || mHead.mZpos == RIGHT_TOP) {
						mLeft = GoLauncher.getDisplayWidth() - showBmp.getWidth();
					}
					mRight = mLeft + showBmp.getWidth();
					mBottom = mTop + showBmp.getHeight();
					canvas.drawBitmap(showBmp, mLeft, mTop, null);
					mActingIndex++;
					postInvalidateDelayed(VIEW_UPDATE_TIME);
				}
			} else {
				mShowStatus = STATUS_IMMOBILE;
				invalidate();
			}
		} else {
			Bitmap bmp = null;
			if (mIsDefaultRes || mActingRes == null || mActingRes.isEmpty()) {
				bmp = mDefaultImmobileBmp;
			} else {
				bmp = mActingRes.get(mActingRes.size() - 1);
			}
			if (bmp != null && !bmp.isRecycled()) {
				canvas.drawBitmap(bmp, mLeft, mTop, null);
				mRight = mLeft + bmp.getWidth();
				mBottom = mTop + bmp.getHeight();
				if (mHead != null && mHead.mIsColsed && mDefaultCloseBmp != null) {
					int closeBmpWidth = mDefaultCloseBmp.getWidth();
					int closeBmpHeight = mDefaultCloseBmp.getHeight();
					if (mDefaultCloseArea == null) {
						mDefaultCloseArea = new Rect();
					}
					mDefaultCloseArea.top = mTop;
					mDefaultCloseArea.right = mRight;
					mDefaultCloseArea.bottom = mTop + closeBmpHeight;
					mDefaultCloseArea.left = mRight - closeBmpWidth;
					canvas.drawBitmap(mDefaultCloseBmp, mDefaultCloseArea.left, mTop, null);
				}
				
				long delay = 60 * 1000;
				if (mHead != null && mHead.mZtime > 0) {
					delay = mHead.mZtime * 1000;
					if (!mHasPost) {
						mHasPost = true;
						postDelayed(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								init();
								mHasPost = false;
							}
						}, delay);
					}
				}
			}
		}
	}
	private void init() {
		int pos = mHead.mZpos;
		calculateCoordinate(pos);
		int picWidth = 0;
		mShowStatus = 0;
		mEnterIndex = 0;
		mActingIndex = 0;
		if (mIsDefaultRes) {
			Context context = getContext();
			if (context != null) {
				if (mDefaultActingBmp1 == null) {
					mDefaultActingBmp1 = ((BitmapDrawable) context.getResources().getDrawable(
							R.drawable.new_message_show1)).getBitmap();
				}
				if (mDefaultActingBmp2 == null) {
					mDefaultActingBmp2 = ((BitmapDrawable) context.getResources().getDrawable(
							R.drawable.new_message_show2)).getBitmap();
				}
				if (mDefaultImmobileBmp == null) {
					mDefaultImmobileBmp = ((BitmapDrawable) context.getResources().getDrawable(
							R.drawable.new_message)).getBitmap();
				}
				
				if (mDefaultActingBmp1 != null) {
					picWidth = mDefaultActingBmp1.getWidth();
				}
			}
			
		} else {
			picWidth = mEnterRes.get(0).getWidth();
		}
		if (pos == LEFT_MIDDLE || pos == LEFT_TOP) {
			mEnterAnimation = new TranslateAnimation(-picWidth, 0, mTop, mTop);
		} else {
			int w = GoLauncher.getDisplayWidth();
			mEnterAnimation = new TranslateAnimation(w, w - picWidth, mTop, mTop);
		}
		mShowStatus = STATUS_ENTER;
		mEnterAnimation.setDuration(500);
		mEnterAnimation.setRepeatCount(0);
		startAnimation(mEnterAnimation);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		if (event.getAction() == MotionEvent.ACTION_MOVE) {
			return false;
		}
		float x = event.getX();
		float y = event.getY();
		if (x > mLeft & x < mRight && y > mTop && y < mBottom) {
			if (mHead.mIsColsed && mDefaultCloseArea != null && mShowStatus == STATUS_IMMOBILE) {
				if (mDefaultCloseArea.contains((int) x, (int) y)) {
					MessageManager.getMessageManager(getContext()).clickCloseCoverFrameView(mHead);
					return false;
				}
			}
			return super.onTouchEvent(event);
		}
		return false;
	}

	@Override
	protected void onAnimationEnd() {
		// TODO Auto-generated method stub
		super.onAnimationEnd();
		if (mShowStatus == STATUS_ENTER) {
			mShowStatus = STATUS_ACTING;
			clearAnimation();
			invalidate();
		}
	}
	
	/**
	 * <br>功能简述:计算摆放位置
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param pos
	 */
	private void calculateCoordinate(int pos) {
		switch (pos) {
			case LEFT_TOP :
			case RIGHT_TOP :
				mTop = CORNER_MARGIN_TOP;
				break;
			case LEFT_MIDDLE :
			case RIGHT_MIDDLE :
				int screenH = GoLauncher.getDisplayHeight();
				mTop = screenH / 4;
				break;

			default :
				break;
		}
	}
}
