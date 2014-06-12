package com.jiubang.ggheart.apps.appfunc.component;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;

import com.go.util.graphics.ImageUtil;
import com.jiubang.core.mars.EventType;
import com.jiubang.core.mars.IComponentEventListener;
import com.jiubang.core.mars.XComponent;

public class AppFuncGoMenu extends XButton implements IComponentEventListener {
	/**
	 * 搜索键规格，以Hight Density为标准
	 */
	protected static final int SEARCH_DENSITY = 80;

	public AppFuncGoMenu(Context context, int tickCount, int x, int y, int width, int height) {
		super(tickCount, x, y, width, height);
	}

	@Override
	protected void drawCurrentFrame(Canvas canvas) {
		Drawable icon = getIcon();
		Drawable iconSelect = getIconSelected();
		if (mIsPressed && (getIcon() != null)) {
			ImageUtil.drawImage(canvas, iconSelect, ImageUtil.CENTERMODE, 0, 0, mWidth, mHeight,
					null);

		}
		if (icon != null) {
			ImageUtil.drawImage(canvas, icon, ImageUtil.CENTERMODE, 0, 0, mWidth, mHeight, null);
		}
	}

	@Override
	public boolean onEventFired(XComponent component, byte eventType, Object event, int arg,
			Object object) {
		switch (eventType) {
			case EventType.CLICKEVENT : {
				MotionEvent motionEvent = (MotionEvent) event;
				if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
					if (component == this) {

						return true;
					}
				}
				if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
					if (component == this) {

						return true;
					}
				}

			}
		}
		return false;
	}

	@Override
	public boolean onTouch(MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN :
				mEventListener.onEventFired(this, EventType.CLICKEVENT, event, 0, null);
				break;
			default :
				break;
		}
		return super.onTouch(event);
	}
}
