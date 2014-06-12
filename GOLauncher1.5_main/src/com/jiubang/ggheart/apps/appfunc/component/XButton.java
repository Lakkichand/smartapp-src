package com.jiubang.ggheart.apps.appfunc.component;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;

import com.go.util.graphics.ImageUtil;
import com.jiubang.core.mars.EventType;
import com.jiubang.core.mars.XComponent;

public class XButton extends XComponent {
	private Drawable mIconSelected;
	private Drawable mIcon;

	public XButton(int tickCount, int x, int y, int width, int height) {
		super(tickCount, x, y, width, height);
	}

	@Override
	protected void drawCurrentFrame(Canvas canvas) {
		if (isSelected()) {
			if (mIconSelected != null) {
				ImageUtil.drawImage(canvas, mIconSelected, ImageUtil.STRETCHMODE, 0, 0, mWidth,
						mHeight, null);
			}
		} else {
			if (mIcon != null) {
				ImageUtil.drawImage(canvas, mIcon, ImageUtil.STRETCHMODE, 0, 0, mWidth, mHeight,
						null);
			}
		}
	}

	@Override
	protected boolean animate() {
		return false;
	}

	public Drawable getIcon() {
		return mIcon;
	}

	public void setIcon(Drawable icon) {
		mIcon = icon;
	}

	public Drawable getIconSelected() {
		return mIconSelected;
	}

	public void setIconSelected(Drawable iconSelected) {
		mIconSelected = iconSelected;
	}

	@Override
	public boolean onTouch(MotionEvent event) {
		super.onTouch(event);
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN : {
				if (XYInRange((int) event.getX(), (int) event.getY())) {
					mIsSelected = true;
					return true;
				}
			}
				break;
			case MotionEvent.ACTION_UP : {
				if (mIsSelected) {
					if (XYInRange((int) event.getX(), (int) event.getY())) {
						// 通知Folder做隐藏动画
						mEventListener.onEventFired(this, EventType.HIDE_FOLDER, null, 0, null);
					}
					mIsSelected = false;
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 增大容错区
	 */
	@Override
	public boolean XYInRange(int x, int y) {
		if ((x >= getAbsX() - 5) && (x <= getAbsX() + mWidth + 5)) {
			if ((y >= getAbsY() - 5) && (y <= getAbsY() + mHeight + 5)) {
				return true;
			}
		}
		return false;
	}
}
