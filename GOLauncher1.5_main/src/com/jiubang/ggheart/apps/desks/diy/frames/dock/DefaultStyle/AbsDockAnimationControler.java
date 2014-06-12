package com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Point;
import android.view.View;

/**
 * 
 * <br>类描述:dock移动动画基类
 * <br>功能详细描述:
 * 
 * @author  ruxueqin
 * @date  [2012-9-19]
 */
public class AbsDockAnimationControler {
	
	protected DockLineLayoutContainer mLineLayoutContainer;
	
	protected int mLayoutWidth; // 屏幕宽度
	protected int mLayoutHeight; // 屏幕高度
	
	protected int mIcon_H_portrait;
	protected int mIcon_W_landscape;
	
	protected View mDragView;
	protected Context mContext;
	
	protected int mDockViewListSize; // 当然有多少个图标
	
	protected ArrayList<DockIconView> mDockViewList; // 每条LineLayout所包含的DockIconView
	
	public AbsDockAnimationControler(int mLayoutWidth, int mLayoutHeight, int mIcon_H_portrait,
			int mIcon_W_landscape, ArrayList<DockIconView> mDockViewList, Context mContext,
			View dragView, DockLineLayoutContainer mLineLayoutContainer) {
		this.mLayoutWidth = mLayoutWidth;
		this.mLayoutHeight = mLayoutHeight;
		this.mIcon_H_portrait = mIcon_H_portrait;
		this.mIcon_W_landscape = mIcon_W_landscape;
		this.mDockViewList = mDockViewList;
		this.mContext = mContext;
		this.mDragView = dragView;
		this.mLineLayoutContainer = mLineLayoutContainer;
		this.mDockViewListSize = mDockViewList.size();
	}
	
	/**
	 * 检查坐标是否在Dock区域
	 * 
	 * @param point
	 *            坐标
	 * @return true or false
	 */
	public final boolean checkInDock(Point point) {
		int left = mLineLayoutContainer.getLeft(); // X坐标
		int top = mLineLayoutContainer.getTop(); // Y坐标

		if (AbsDockView.sPortrait) {
			if (top <= point.y && 0 <= point.x && point.x <= mLayoutWidth) {
				return true;
			}
		} else {
			if (left <= point.x && point.x <= mLayoutWidth && top <= point.y
					&& point.y <= mLayoutHeight + top) {
				return true;
			}
		}
		
		return false;
	}
}
