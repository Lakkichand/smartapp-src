package com.jiubang.ggheart.components.diygesture.gesturemanageview;

import java.util.ArrayList;

import android.gesture.Gesture;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGestureListener;
import android.gesture.GesturePoint;
import android.gesture.GestureStroke;
import android.graphics.Path;
import android.view.MotionEvent;

public class DiyGestureOnGestureListener implements OnGestureListener {
	public boolean mIsDrawPoint = false; // 是否画"点"标志
	private float mStrokeWidth = 5; // 画笔大小

	public DiyGestureOnGestureListener(float strokeWidth) {
		if (strokeWidth > 0) {
			this.mStrokeWidth = strokeWidth;
		}

	}

	@Override
	public void onGestureStarted(GestureOverlayView overlay, MotionEvent event) {
		mIsDrawPoint = true;
	}

	@Override
	public void onGesture(GestureOverlayView overlay, MotionEvent event) {
		// 第一点需要画圆,只画一次
		if (mIsDrawPoint) {
			Path gesturePath = overlay.getGesturePath();
			ArrayList<GesturePoint> currentStrokeList = overlay.getCurrentStroke();
			if (gesturePath != null && currentStrokeList != null && currentStrokeList.size() > 0) {
				float x = currentStrokeList.get(0).x; // 起始点X坐标
				float y = currentStrokeList.get(0).y; // 起始点Y坐标
				gesturePath.addCircle(x, y, mStrokeWidth / 2, Path.Direction.CW); // 在起始点画圆
				overlay.getGesturePath().moveTo(x, y); // 需要移动到起始位置。不然画出来的路径有些问题
			}

			mIsDrawPoint = false;
		}
	}

	@Override
	public void onGestureEnded(GestureOverlayView overlay, MotionEvent event) {
		if (mIsDrawPoint) {
			// 如果是点就移除刚添加进去的点
			Gesture gesture = overlay.getGesture();
			if (gesture != null) {
				ArrayList<GestureStroke> gestureStrokes = gesture.getStrokes();
				if (gestureStrokes != null && gestureStrokes.size() > 0) {
					gestureStrokes.remove(gesture.getStrokesCount() - 1);
				}
			}
		}
	}

	@Override
	public void onGestureCancelled(GestureOverlayView overlay, MotionEvent event) {

	}
}