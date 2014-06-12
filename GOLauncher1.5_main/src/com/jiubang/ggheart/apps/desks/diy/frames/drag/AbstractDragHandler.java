package com.jiubang.ggheart.apps.desks.diy.frames.drag;

import android.view.View;

import com.jiubang.core.framework.IFrameManager;
import com.jiubang.ggheart.data.info.ItemInfo;

/**
 * 抽象的拖动层处理器
 * 
 * @author yuankai
 * @version 1.0
 */
public abstract class AbstractDragHandler implements IDragListener, ISelfObject {
	protected int mType;
	protected DragFrame mFrame;
	protected IFrameManager mFrameManager;
	protected View mDraggedView; // 屏幕层传递过来的视图
	protected Long mDragItemId; // 　拖动项id
	protected int mDragListener;

	protected AbstractDragHandler(int type, DragFrame dragFrame, IFrameManager frameManager,
			View draggedView) {
		mType = type;
		mFrame = dragFrame;
		mFrameManager = frameManager;
		mDraggedView = draggedView;
		if (null != mDraggedView && null != mDraggedView.getTag()
				&& mDraggedView.getTag() instanceof ItemInfo) {
			mDragItemId = ((ItemInfo) mDraggedView.getTag()).mInScreenId;
		}
	}

	public void setTrashGone() {

	}

	public void leaveImmediatly() {

	}

	@Override
	public void selfConstruct() {

	}

	@Override
	public void selfDestruct() {
		// mFrame = null;
		mFrameManager = null;
		// mDraggedView = null;
	}

	public void onDragFinish() {

	}
}
