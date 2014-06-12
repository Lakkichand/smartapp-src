package com.jiubang.ggheart.apps.appfunc.component;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import com.go.util.graphics.ImageUtil;
import com.jiubang.core.mars.EventType;
import com.jiubang.core.mars.IComponentEventListener;
import com.jiubang.core.mars.XComponent;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.model.DeliverMsgManager;

public class AppFuncGoSearch extends XButton implements IComponentEventListener {

	// private BitmapDrawable mHome;
	// private BitmapDrawable mHomeBg;

	/**
	 * 搜索键规格，以Hight Density为标准
	 */
	protected static final int SEARCH_DENSITY = 80;

	public AppFuncGoSearch(Context context, int tickCount, int x, int y, int width, int height) {
		super(tickCount, x, y, width, height);
	}

	// public void setHomeIcon(BitmapDrawable homeIcon) {
	// mHome = homeIcon;
	// }
	//
	// public void setHomeIconBg(BitmapDrawable homeIconBg) {
	// mHomeBg = homeIconBg;
	// }

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
				if (component == this) {
					// 当home icon 被点击

					// 退出编辑模式(改为发送消息给allapptabconponent)
					// DeliverMsgManager.getInstance().onChange(handlerID,
					// msgID,
					// obj);
					// if (mAllAppEventListener != null) {
					// mAllAppEventListener.onEventFired(this, (byte) 0, null,
					// 0,
					// null);
					// }
					// // 移除文件夹
					// AppFuncHandler.getInstance().removeFolder();
					DeliverMsgManager.getInstance().onChange(
							AppFuncConstants.APP_FUNC_ALLAPP_CONTAINER,
							AppFuncConstants.ALL_APP_SEARCH_SHOW, null);
					return true;
				}
			}
		}
		return false;
	}
}
