package com.jiubang.ggheart.apps.desks.diy.frames.screen;

import java.util.List;

import android.app.Activity;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.jiubang.core.framework.AbstractFrame;
import com.jiubang.core.framework.IFrameManager;
import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;

/**
 * widget 编辑层
 * 
 * @author luopeihuan
 * 
 */
public class WidgetEditFrame extends AbstractFrame
		implements
			ResizeViewHandler.OnSizeChangedListener,
			ResizeViewHandler.OnValidateSizeListener {
	@SuppressWarnings("unused")
	private LayoutInflater mInflater;
	private ResizeViewHandler mResizer;

	private RelativeLayout mLayout;

	private final static String LOG_TAG = "WidgetEditFrame";

	public final static int SCREEN_RECT_INDEX = 0;
	public final static int WIDGET_RECT_INDEX = 1;

	// Bundle key for setup ResizeViewHandler
	public final static String CIRCLE = "circle";
	public final static String MAINTAIN_RATIO = "maintain_ratio";
	public final static String MIN_WIDTH = "min_width";
	public final static String MIN_HEIGHT = "mi_height";

	private float mMinWidth;
	private float mMinHeight;

	public WidgetEditFrame(Activity activity, IFrameManager frameManager, int id) {
		super(activity, frameManager, id);
		mInflater = mActivity.getLayoutInflater();
		mLayout = new RelativeLayout(activity);
		RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.FILL_PARENT);
		mLayout.setLayoutParams(rlp);

		//初始化编辑层时把view加上去
		mResizer = new ResizeViewHandler(mActivity);
		mResizer.setOnValidateSizingRect(this);
		mResizer.setOnSizeChangedListener(this);
		mLayout.addView(mResizer);
	}

	@Override
	public void onAdd() {
		super.onAdd();
		mFrameManager.registKey(this);
	}

	@Override
	public void onRemove() {
		super.onRemove();
		mFrameManager.unRegistKey(this);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// boolean resumed = super.onKeyDown(keyCode, event);
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
					IDiyMsgIds.WIDGET_EDIT_FRAME_STOP_EIDT, -1, null, null);
		}
		// 返回 true，屏蔽轨迹球切换屏幕 by luopeihuan 2010-12-15 10:50
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean handleMessage(Object who, int type, int msgId, int param, Object object,
			List objects) {
		super.handleMessage(who, type, msgId, param, object, objects);
		boolean ret = false;
		switch (msgId) {
		
			//编辑层初始化数据
			case IDiyMsgIds.WIDGET_EDIT_FRAME_SETUP :
				if (object != null && objects != null && object instanceof Bundle) {
					Bundle bundle = (Bundle) object;
					final RectF imageRectF = (RectF) objects.get(SCREEN_RECT_INDEX); //屏幕celllayou的区域
					final RectF cropRectF = (RectF) objects.get(WIDGET_RECT_INDEX);  //widget的区域
					final boolean circle = bundle.getBoolean(CIRCLE);
					final boolean maintainRatio = bundle.getBoolean(MAINTAIN_RATIO);
					mMinWidth = bundle.getFloat(MIN_WIDTH);
					mMinHeight = bundle.getFloat(MIN_HEIGHT);

					mResizer.setup(null, imageRectF, cropRectF, circle, maintainRatio, mMinWidth,
							mMinHeight);

					Log.i(LOG_TAG, "setup imageRectF = " + imageRectF.toString());
					Log.i(LOG_TAG, "setup cropRectF = " + cropRectF.toString());
					Log.i(LOG_TAG, "setup mMinWidth = " + mMinWidth + " mMinHeight = " + mMinHeight);

					ret = true;
				}
				break;

			case IDiyMsgIds.BACK_TO_MAIN_SCREEN : {
				// 移除自己
				GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						IFrameworkMsgId.REMOVE_FRAME, getId(), null, null);
			}
				break;
			default :
				break;
		}
		return ret;
	}

	@Override
	public View getContentView() {
		return mLayout;
	}

	@Override
	public void onTrigger(RectF r) {
		// if (mMinHeight > 0 && mMinWidth > 0)
		// {
		// final float left = Math.round(r.left / mMinWidth) * mMinWidth;
		// final float top = Math.round(r.top / mMinHeight) * mMinHeight;
		// final float right = left + (Math.max(Math.round(r.width() /
		// (mMinWidth)), 1) * mMinWidth);
		// final float bottom = top + (Math.max(Math.round(r.height() /
		// (mMinHeight)), 1) * mMinHeight);
		// r.set(left, top, right, bottom);
		// }
	}

	@Override
	public void onValidateSize(RectF rect, int hitType) {
		if (mMinHeight <= 0 || mMinWidth <= 0) {
			return;
		}
		rect.offset(-CellLayout.getLeftPadding(), -CellLayout.getTopPadding());
		Log.i(LOG_TAG, "onValidateSize rect = " + rect.toString());
		
		// 如果是拉上边/左边/左上角，那么以bottom（right）为坐标定位基准
		if (hitType == ResizeViewHandler.HIT_TYPE_LETF_TOP) {
			final float right = Math.round(rect.right / mMinWidth) * mMinWidth;
			final float bottom = Math.round(rect.bottom / mMinHeight) * mMinHeight;
			final float left = right - (Math.max(Math.round(rect.width() / mMinWidth), 1) * mMinWidth);
			final float top = bottom - (Math.max(Math.round(rect.height() / mMinHeight), 1) * mMinHeight);
			rect.set(left, top, right, bottom);
		}
		
		// 左下角，那么以right（top）为坐标定位基准
		else if (hitType == ResizeViewHandler.HIT_TYPE_LETF_BOTTOM) {
			final float right = Math.round(rect.right / mMinWidth) * mMinWidth;
			final float top = Math.round(rect.top / mMinHeight) * mMinHeight;
			final float left = right - (Math.max(Math.round(rect.width() / mMinWidth), 1) * mMinWidth);
			final float bottom = top + (Math.max(Math.round(rect.height() / mMinHeight), 1) * mMinHeight);
			rect.set(left, top, right, bottom);
		}
				
		
		//右上角，那么以left（bottom）为坐标定位基准
		else if (hitType == ResizeViewHandler.HIT_TYPE_RIGHT_TOP) {
			final float left = Math.round(rect.left / mMinWidth) * mMinWidth;
			final float bottom = Math.round(rect.bottom / mMinHeight) * mMinHeight;
			final float right = left + (Math.max(Math.round(rect.width() / mMinWidth), 1) * mMinWidth);
			final float top = bottom - (Math.max(Math.round(rect.height() / mMinHeight), 1) * mMinHeight);
			rect.set(left, top, right, bottom);
		}
		
		
		// 如果是拉底边/右边/右下角，那么以left（top）为坐标定位基准
		else if (hitType == ResizeViewHandler.HIT_TYPE_RIGHT_BOTTOM) {
			final float left = Math.round(rect.left / mMinWidth) * mMinWidth;
			final float top = Math.round(rect.top / mMinHeight) * mMinHeight;
			final float right = left + (Math.max(Math.round(rect.width() / mMinWidth), 1) * mMinWidth);
			final float bottom = top + (Math.max(Math.round(rect.height() / mMinHeight), 1) * mMinHeight);
			rect.set(left, top, right, bottom);
		}
		
		Log.i(LOG_TAG, "onValidateSize rect2 = " + rect.toString());

		final int cellX = Math.round(rect.left / mMinWidth);
		final int cellY = Math.round(rect.top / mMinHeight);
		final int spanX = Math.max(Math.round(rect.width() / mMinWidth), 1);
		final int spanY = Math.max(Math.round(rect.height() / mMinHeight), 1);

		Rect r = new Rect(cellX, cellY, cellX + spanX, cellY + spanY);
		Log.i(LOG_TAG, "widget = " + r.toString());

		boolean collision = GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
				IDiyMsgIds.WIDGET_EDIT_FRAME_VALIDATE_RECT, -1, r, null);
//		collision |= GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
//				IDiyMsgIds.IS_WIDGET_EDIT_SIZE_SMALLER_THAN_MIN_SIZE, -1, r, null);
		mResizer.setColliding(collision);
	}
}
