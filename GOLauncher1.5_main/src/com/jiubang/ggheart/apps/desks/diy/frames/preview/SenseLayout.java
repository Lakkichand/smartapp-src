package com.jiubang.ggheart.apps.desks.diy.frames.preview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.gau.go.launcherex.R;
import com.go.util.multitouch.MultiTouchController;
import com.go.util.multitouch.MultiTouchController.MultiTouchObjectCanvas;
import com.go.util.multitouch.MultiTouchController.PointInfo;
import com.go.util.multitouch.MultiTouchController.PositionAndScale;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenIndicator;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.Workspace;

public class SenseLayout extends FrameLayout implements MultiTouchObjectCanvas<Object> {
	private SenseWorkspace mWorkspace;
	private ScreenIndicator mScreenIndicator;
	private MultiTouchController<Object> multiTouchController;

	public SenseLayout(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
		multiTouchController = new MultiTouchController<Object>(this, false);
	}

	public SenseLayout(Context c, AttributeSet att) {
		super(c, att);
		multiTouchController = new MultiTouchController<Object>(this, false);

	}

	public SenseWorkspace getWorkspace() {
		return mWorkspace;
	}

	public ScreenIndicator getIndicator() {
		return mScreenIndicator;
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		// TODO Auto-generated method stub
		super.onLayout(changed, left, top, right, bottom);
		int t = (int) (top + getContext().getResources().getDimension(R.dimen.sense_indicator_top));
		mScreenIndicator.layout(0, t, right,
				t + (int) getContext().getResources().getDimension(R.dimen.dots_indicator_height));
		mWorkspace.layout(left, top, right, bottom);
	}

	@Override
	protected void onFinishInflate() {
		// TODO Auto-generated method stub
		super.onFinishInflate();
		mWorkspace = (SenseWorkspace) findViewById(R.id.senseWorkspace);
		mScreenIndicator = (ScreenIndicator) findViewById(R.id.screenIndicator);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		boolean isdrag = GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
				IDiyMsgIds.PREVIEW_DRAG_ING, 0, null, null);
		if (!isdrag) { // 如果不是正常状态就不响应手势了
			if (multiTouchController.onTouchEvent(ev)) {
				return true;
			}
		}
		return super.onTouchEvent(ev);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		boolean isdrag = GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
				IDiyMsgIds.PREVIEW_DRAG_ING, 0, null, null);
		if (!isdrag) {// 如果卡片不是正常状态就不响应手势了
			if (multiTouchController.onTouchEvent(ev)) {
				return true;
			}
		}
		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public Object getDraggableObjectAtPoint(PointInfo pt) {
		return this;
	}

	@Override
	public void getPositionAndScale(Object obj, PositionAndScale objPosAndScaleOut) {
		objPosAndScaleOut.set(0.0f, 0.0f, true, 1.0f, false, 0.0f, 0.0f, false, 0.0f);
	}

	@Override
	public void selectObject(Object obj, PointInfo pt) {
	}

	@Override
	public boolean setPositionAndScale(Object obj, PositionAndScale update, PointInfo touchPoint) {
		float newRelativeScale = update.getScale();
		int targetZoom = (int) Math.round(Math.log(newRelativeScale) * Workspace.ZOOM_LOG_BASE_INV);
		// 暂时用>1,用0太灵敏
		if (targetZoom > 1 && GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, // 带动画
				IDiyMsgIds.PREVIEW_SHOWING, 0, null, null)) {
			// GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
			// //不带动画
			// IDiyMsgIds.PREVIEW_BACK_HADLE, 0, null, null);
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, // 带动画
					IDiyMsgIds.PREVIEW_TO_MAIN_SCREEN, 0, false, null);

		}
		return false;
	}

}
