package com.jiubang.ggheart.apps.gowidget.gostore.component;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.jiubang.ggheart.appgame.base.utils.AppGameDrawUtils;
import com.jiubang.ggheart.apps.gowidget.gostore.ImageManager;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreDisplayUtil;
import com.jiubang.ggheart.apps.gowidget.gostore.views.BroadCaster.BroadCasterObserver;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  lijunye
 * @date  [2012-11-22]
 */
public class SimpleImageView extends ImageView implements BroadCasterObserver {

	private String mImgId = null;
	private int mBitmapWidth = -1;
	private int mBitmapHeight = -1;

	private boolean mIsRecycle = false; // 是否已经回收的标记

	private Bitmap mBmp = null;

	private int mDefaultImageResId = 0; // 默认图片资源ID
	private boolean mIsNeedMask = false;

	public int getDefaultImageResId() {
		return mDefaultImageResId;
	}

	public void setDefaultImageResId(int defaultImageResId) {
		this.mDefaultImageResId = defaultImageResId;
	}

	public String getImgId() {
		return mImgId;
	}
	
	public void setIsNeedMask(boolean b) {
		mIsNeedMask = b;
	}
	
	public void setImgId(String imgId) {
		this.mImgId = imgId;
		mBmp = ImageManager.getInstance().getBitmap(this, mImgId);
		if (mBmp != null) {
			if (mIsNeedMask) {
				mBmp = AppGameDrawUtils.getInstance().createMaskBitmap(getContext(), mBmp);
				setImageBitmap(mBmp);
			} else {
				int bmpWidth = mBmp.getWidth();
				int bmpHeight = mBmp.getHeight();
				if ((mBitmapWidth < 0 && mBitmapHeight < 0)
						|| (bmpWidth == mBitmapWidth && bmpHeight == mBitmapHeight)) {
					setImageBitmap(mBmp);
				} else {
					Bitmap scaleBitmap = scaleBitmap(mBmp);
					setImageBitmap(scaleBitmap);
					ImageManager.getInstance().refreshBitmap(scaleBitmap, mImgId);
					scaleBitmap = null;
				}
			}
			
		} else {
			if (mIsNeedMask) {
				Drawable drawable = getResources().getDrawable(mDefaultImageResId);
				BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
				Bitmap bitmap = bitmapDrawable.getBitmap();
				bitmap = AppGameDrawUtils.getInstance().createMaskBitmap(getContext(), bitmap);
				setImageBitmap(bitmap);
			} else {
				if (mDefaultImageResId != 0) {
					setImageResource(mDefaultImageResId);
				}
			}
			
		}

		if (mImagechangeListener != null) {
			mImagechangeListener.onImageChange();
		}
	}

	/**
	 * 对请求返回的图片进行缩放的方法 可只对宽或者只对高进行缩放
	 * 
	 * @param imgId
	 * @param width
	 *            缩放后的宽，如果为负数，则不对宽缩放
	 * @param Height
	 *            缩放后的高，如果为负数，则不对高缩放
	 */
	public void setImgId(String imgId, int width, int Height) {
		mBitmapWidth = width;
		mBitmapHeight = Height;
		setImgId(imgId);
	}

	public SimpleImageView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub

	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		if (mBmp != null && mBmp.isRecycled()) {
			setImgId(mImgId);
			return;
		}
		super.onDraw(canvas);
	}

	public SimpleImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onBCChange(int msgId, int param, Object object, Object object2) {
		// TODO Auto-generated method stub
		if (mIsRecycle) {
			// 如果已经回收
			return;
		}
		switch (msgId) {
			case ImageManager.EVENT_NETWORK_ICON_CHANGE : {
				if (object == null) {
					break;
				}

				mBmp = (Bitmap) object;
				if (mIsNeedMask) {
					mBmp = AppGameDrawUtils.getInstance().createMaskBitmap(getContext(), mBmp);
					setImageBitmap(mBmp);
				} else {
					// 匹配机型的缩放
					Bitmap bitmapToMachine = GoStoreDisplayUtil
							.scaleBitmapToMachine(getContext(), mBmp);
					Bitmap scaleBitmap = scaleBitmap(bitmapToMachine);
					// bitmapToMachine与bitmap可能为同一个.
					if (bitmapToMachine != null && !bitmapToMachine.equals(mBmp)
							&& !bitmapToMachine.equals(scaleBitmap)) {
						// 临时产生的中间变量值，需要回收。
						bitmapToMachine.recycle();
					}
					mBmp = null;
					bitmapToMachine = null;
					ImageManager.getInstance().refreshBitmap(scaleBitmap, mImgId);
					setImageBitmap(scaleBitmap);
					scaleBitmap = null;
				}
				this.invalidate();
			}
				break;
			case ImageManager.EVENT_LOCAL_ICON_EXIT : {
				if (object != null && object instanceof Bitmap) {
					mBmp = (Bitmap) object;
					if (mBmp != null) {
						if (mIsNeedMask) {
							mBmp = AppGameDrawUtils.getInstance().createMaskBitmap(getContext(), mBmp);
							setImageBitmap(mBmp);
						} else {
							int bmpWidth = mBmp.getWidth();
							int bmpHeight = mBmp.getHeight();
							if ((mBitmapWidth < 0 && mBitmapHeight < 0)
									|| (bmpWidth == mBitmapWidth && bmpHeight == mBitmapHeight)) {
								setImageBitmap(mBmp);
							} else {
								Bitmap scaleBitmap = scaleBitmap(mBmp);
								setImageBitmap(scaleBitmap);
								ImageManager.getInstance().refreshBitmap(scaleBitmap, mImgId);
								scaleBitmap = null;
							}
						}
						this.invalidate();
					}
				}
			}
				break;
			default :
				break;
		}
	}

	/**
	 * 缩放图片。若不能缩放，则直接返回原图。
	 * 
	 * @author wangzhuobin
	 * @param sourceBitmap
	 * @return
	 */
	private Bitmap scaleBitmap(Bitmap sourceBitmap) {
		Bitmap destBitmap = sourceBitmap;
		if (sourceBitmap != null) {
			if (mBitmapWidth > 0 && mBitmapHeight > 0) {
				destBitmap = GoStoreDisplayUtil.scaleBitmapToDisplay(getContext(), sourceBitmap,
						mBitmapWidth, mBitmapHeight);
			}
		}
		return destBitmap;
	}

	public void clearIcon() {
		setBackgroundDrawable(null);
		setImageBitmap(null);
		// 停止加载图片的任务
		ImageManager.getInstance().cancelLoadImg(mImgId);
		// 反注册对应图片的监听
		ImageManager.getInstance().unRegisterObserverFromSimpleImage(this, mImgId);
		mImgId = null;
	}

	public void recycle() {
		mIsRecycle = true;
		setBackgroundDrawable(null);
		setImageBitmap(null);
		// 释放本应用对应的图片
		ImageManager.getInstance().releaseBitmap(this, mImgId);
		mImgId = null;
	}

	public Bitmap getBmp() {
		return mBmp;
	}

	/**
	 * 把SimpleImageView的bmp释放并设置默认资源ID,等于回到初始状态又能把内存释放掉
	 * 
	 * @author zhouxuewen
	 */
	public void recycleBmpAndSetResId(int resId) {
		if (mBmp != null) {
			setImageBitmap(null);
			setImageResource(resId);
			ImageManager.getInstance().releaseBitmap(this, mImgId);
			mBmp.recycle();
			mBmp = null;
		}
	}

	private OnImageChangeListener mImagechangeListener;

	public void setOnImageChangeListener(OnImageChangeListener l) {
		mImagechangeListener = l;
	}

	public void setImageBitmap(int width, int height, Bitmap bm) {
		// TODO Auto-generated method stub
		mBitmapHeight = height;
		mBitmapWidth = width;
		mBmp = scaleBitmap(bm);
		super.setImageBitmap(mBmp);
	}
}
