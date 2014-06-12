package com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;
/**
 * 
 * <br>类描述:Drawable 加载任务的载体
 * 
 * @author guoyiqing
 * @date  [2012-9-11]
 * @param <V> 
 * @param <T>
 */
public class LoadingDrawableItem<V extends ImageView, T extends IDrawableLoader> {
	protected final T mTarget;
	protected final V mView;
	protected final int mPosition;
	protected Drawable mResult;

	public LoadingDrawableItem(V v, T target, int position) {
		mTarget = target;
		mView = v;
		mPosition = position;
	}

	public void doLoading() {
		if (mView.getTag() != Boolean.valueOf(true)) {
			mResult = mTarget.loadDrawable(mPosition, null);
			mView.setTag(Boolean.valueOf(true));
		}
	}

	public boolean isLoaded() {
		return mView.getTag() == Boolean.valueOf(true);
	}

	public void displayResult() {
		mTarget.displayResult(mView, mResult);

	}

}
