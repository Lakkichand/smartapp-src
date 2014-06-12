package com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle;

import java.util.ArrayList;

import android.graphics.Rect;


/**
 * 
 * <br>类描述:dock图标拖动监听者
 * <br>功能详细描述:
 * 
 * @author  ruxueqin
 * @date  [2012-10-16]
 */
public interface IDragPositionListner {
	public abstract void onLeft(DockIconView dockIconView);

	public abstract void onMiddle(DockIconView dockIconView, Rect rect, int indexinrow);

	public abstract void onRight(DockIconView dockIconView);

	public abstract void setRecycleDragCache();

	public abstract void setAddIconIndex(ArrayList<Integer> list);
}
