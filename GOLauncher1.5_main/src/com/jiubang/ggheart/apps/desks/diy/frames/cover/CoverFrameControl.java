package com.jiubang.ggheart.apps.desks.diy.frames.cover;

import java.util.ArrayList;
import java.util.List;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.jiubang.core.framework.ICleanable;
import com.jiubang.ggheart.apps.desks.diy.frames.cover.CoverFrame.CoverView;

/**
 * 罩子层的view管理器
 * @author jiangxuwen
 *
 */
public class CoverFrameControl implements ICleanable {

	private List<CoverView> mCoverViews;
	private ViewGroup mCoverParent;

	public CoverFrameControl() {
		mCoverViews = new ArrayList<CoverView>();
	}

	protected void setContainer(ViewGroup coverParent) {
		mCoverParent = coverParent;
	}

	public void hideAllCoverViews() {
		setAllViewVisiable(View.INVISIBLE);
	}

	public void showAllCoverViews() {
		setAllViewVisiable(View.VISIBLE);
	}

	public void hideCoverView(int viewId) {
		if (viewId != CoverFrame.COVER_VIEW_EXTRA) {
			setViewVisiable(viewId, View.INVISIBLE);
		}
	}

	public void showCoverView(int viewId) {
		setViewVisiable(viewId, View.VISIBLE);
	}

	public synchronized void removeCoverView(int viewId) {
		CoverView view = getCoverView(viewId);
		if (view != null && view.getView() != null) {
			mCoverParent.removeView(view.getView());
			mCoverViews.remove(view);
			// 如果移除的是主题2.0的罩子层，那么也把额外的view移除
//			if (viewId == CoverFrame.COVER_VIEW_THEME) {
//				removeCoverView(CoverFrame.COVER_VIEW_EXTRA);
//			}
		}
	}

	private void setViewVisiable(int viewId, int visivility) {
		CoverView view = getCoverView(viewId);
		if (view != null && view.getView() != null) {
			view.getView().setVisibility(visivility);
		}
	}

	private void setAllViewVisiable(int visivility) {
		final int size = mCoverViews.size();
		for (int i = 0; i < size; i++) {
			CoverView view = mCoverViews.get(i);
			if (view != null && view.getView() != null && view.getId() != CoverFrame.COVER_VIEW_EXTRA) {
				view.getView().setVisibility(visivility);
			}
		}
	}

	public synchronized CoverView getCoverView(int viewId) {
		final int size = mCoverViews.size();
		for (int i = 0; i < size; i++) {
			CoverView view = mCoverViews.get(i);
			if (view != null && view.getId() == viewId) {
				return view;
			}
		}
		return null;
	}

	public synchronized boolean addCoverView(CoverView view, int visibility) {
		return addCoverView(view, mCoverViews.size(), visibility);
	}

	public synchronized boolean addCoverView(CoverView view, int index, int visibility) {
		if (view == null) {
			throw new IllegalArgumentException("view cannot be null");
		}

		// 索引保护
		if (index < 0) {
			throw new IllegalArgumentException("index cannot be negative");
		}
		boolean ret = false;
		if (isViewExits(view)) {
			removeCoverView(view.getId());
		}
		final int size = mCoverViews.size();
		if (index > size) {
			mCoverViews.add(view);
			mCoverParent.addView(view.getView());
		} else {
			mCoverViews.add(index, view);
			// 因为主界面已经有了桌面显示层，罩子层的下标必须从1开始
			mCoverParent.addView(view.getView(), Math.max(1, index));
		}
		ret = true;

		return ret;
	}

	private boolean isViewExits(CoverView view) {
		if (view != null) {
			for (CoverView aview : mCoverViews) {
				if (aview == view || aview.getId() == view.getId()) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean dispatchTouchEvent(MotionEvent event) {
		boolean ret = false;
		final int size = mCoverViews.size();
		for (int i = size - 1; i >= 0; i--) {
			CoverView view = mCoverViews.get(i);
			if (view != null && view.getView() != null && view.getView().getVisibility() == View.VISIBLE
					&& view.getId() != CoverFrame.COVER_VIEW_EXTRA) {
				if (ret = view.getView().onTouchEvent(event)) {
					break;
				}
			}
		}
		return ret;
	}

	// 移除罩子层的所有view
	protected synchronized void removeAllCoverViews() {
		final int size = mCoverViews.size();
		for (int i = 0; i < size; i++) {
			CoverView view = mCoverViews.get(i);
			if (view != null && view.getView() != null) {
				mCoverParent.removeView(view.getView());
			}
		}
	}

	protected synchronized int getCoverSize() {
		return mCoverViews.size();
	}

	@Override
	public void cleanup() {
		removeAllCoverViews();
		mCoverViews.clear();
	}
}
