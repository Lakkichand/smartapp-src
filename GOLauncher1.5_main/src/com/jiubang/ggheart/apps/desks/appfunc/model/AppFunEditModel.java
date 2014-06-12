package com.jiubang.ggheart.apps.desks.appfunc.model;

import java.util.ArrayList;
import java.util.LinkedList;

import android.graphics.Rect;

/**
 * 
 * @author wenjiaming
 * 
 */
public class AppFunEditModel {

	private RectManager mRectManager;
	/**
	 * 每一个View的位置信息
	 */
	private ArrayList<ViewRect> mViewList;

	public AppFunEditModel() {
		mRectManager = new RectManager();
		mViewList = new ArrayList<ViewRect>(20);
	}

	public void removeView(int position) {
		ViewRect rect = mViewList.remove(position);
		if (rect != null) {
			mRectManager.recycleRect(rect);
		}
	}

	public void addView(short position, int left, int right, int top, int bottom) {
		ViewRect rect = mRectManager.getRect();
		rect.mRect.left = left;
		rect.mRect.right = right;
		rect.mRect.top = top;
		rect.mRect.bottom = bottom;
		rect.mPostionId = position;
		mViewList.add(position, rect);
	}

	public void clearAllView() {
		for (ViewRect rect : mViewList) {
			mRectManager.recycleRect(rect);
		}
		mViewList.clear();
	}

	public int searchView(int position, Rect src) {
		int size = mViewList.size();
		for (int i = 0; i < size; i++) {
			if (position != i) {
				ViewRect viewRect = mViewList.get(i);
				if (viewRect.mRect.contains(src.centerX(), src.centerY())) {
					return viewRect.mPostionId;
				}
			}
		}
		return -1;
	}

	public boolean isCreateFolder(int position, Rect src) {
		ViewRect viewRect = mViewList.get(position);
		if (src.centerX() > (viewRect.mRect.centerX() - viewRect.mRect.width() * 0.1f)
				&& src.centerX() < (viewRect.mRect.centerX() + viewRect.mRect.width() * 0.1f)) {
			return true;
		}
		return false;
	}

	public int getSameRowPosition(int scrPosition, int targetPosition, Rect src) {
		ViewRect viewRect = mViewList.get(targetPosition);
		// scrPosition原位置右边一格
		if (scrPosition - targetPosition == -1) {
			if (src.centerX() > viewRect.mRect.centerX()) {
				return 1;
			}
		}
		// scrPosition原位置左边一格
		else if (scrPosition - targetPosition == 1) {
			if (src.centerX() < viewRect.mRect.centerX()) {
				return -1;
			}
		}
		// scrPosition原位置右边
		else if (scrPosition - targetPosition <= -1) {
			if (src.centerX() > viewRect.mRect.centerX()) {
				return targetPosition - scrPosition;
			} else {
				return targetPosition - scrPosition - 1;
			}
		}
		// scrPosition原位置左边
		else if (scrPosition - targetPosition >= 1) {
			if (src.centerX() < viewRect.mRect.centerX()) {
				return targetPosition - scrPosition;
			} else {
				return targetPosition - scrPosition + 1;
			}
		}
		return 0;
	}

	public int getPosition(int scrPosition, int targetPosition, Rect src) {
		ViewRect viewRect = mViewList.get(targetPosition);
		// 移动View相对位置原View左上方
		if ((scrPosition % 4) > (targetPosition % 4)) {
			if (src.centerX() < viewRect.mRect.centerX()) {
				return targetPosition - scrPosition;
			} else {
				return targetPosition - scrPosition + 1;
			}
		}
		// 移动View相对位置原View左下方
		else if ((scrPosition % 4) < (targetPosition % 4)) {
			if (src.centerX() > viewRect.mRect.centerX()) {
				return targetPosition - scrPosition;
			} else {
				return targetPosition - scrPosition - 1;
			}
		}
		// 移动View相对位置原View右上方
		else if ((scrPosition % 4) < (targetPosition % 4)) {
			if (src.centerX() > viewRect.mRect.centerX()) {
				return targetPosition - scrPosition - 1;
			} else {
				return targetPosition - scrPosition;
			}
		}
		// 移动View相对位置原View右下方
		else if ((scrPosition % 4) < (targetPosition % 4)) {
			if (src.centerX() > viewRect.mRect.centerX()) {
				return targetPosition - scrPosition;
			} else {
				return targetPosition - scrPosition + 1;
			}
		}
		// 遂直上面
		else if (scrPosition > targetPosition) {
			if (src.centerX() < viewRect.mRect.centerX()) {
				return targetPosition - scrPosition;
			} else {
				return targetPosition - scrPosition + 1;
			}
		}
		// 遂直下面
		else if (scrPosition < targetPosition) {
			if (src.centerX() > viewRect.mRect.centerX()) {
				return targetPosition - scrPosition - 1;
			} else {
				return targetPosition - scrPosition;
			}
		}
		return 0;
	}

	public ViewRect getViewRect(int position) {
		int size = mViewList.size();
		if (position < size) {
			return mViewList.get(position);
		}
		return null;
	}

	public void updateViewRectIndex(int src, int target) {
		for (int i = src; i < target; i++) {
			ViewRect rect = mViewList.get(src + 1);
			mViewList.add(src + 1, mViewList.get(src));
			mViewList.add(src, rect);
		}
	}

	// /////////////////////////////////////////////////////////////////////////
	static class RectManager {
		LinkedList<ViewRect> linkedList;

		public RectManager() {
			linkedList = new LinkedList<ViewRect>();
		}

		public ViewRect getRect() {
			ViewRect rect = linkedList.poll();
			if (rect == null) {
				rect = new ViewRect((short) 0, new Rect());
			}
			return rect;
		}

		public void recycleRect(ViewRect rect) {
			rect.mRect.setEmpty();
			rect.mPostionId = -1;
			linkedList.add(rect);
		}
	}

	// //////////////////////////////////////////////////////////////////
	public static class ViewRect {
		public short mPostionId;
		public Rect mRect;

		public ViewRect(short postionId, Rect rect) {
			mPostionId = postionId;
			mRect = rect;
		}

	}
}
