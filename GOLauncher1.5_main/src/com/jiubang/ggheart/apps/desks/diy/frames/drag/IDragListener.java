package com.jiubang.ggheart.apps.desks.diy.frames.drag;

public interface IDragListener {
	// 手指位置改变
	public void onFingerPointF(IDragObject obj, float x, float y);

	// 中心位置改变
	public void onCenterPointF(IDragObject obj, float x, float y, int dragType);

	// 边缘位置
	public static final int EDGE_NONE = 0;
	public static final int EDGE_LEFT = 1;
	public static final int EDGE_TOP = 1 << 1;
	public static final int EDGE_RIGHT = 1 << 2;
	public static final int EDGE_BOTTOM = 1 << 3;
	public static final int EDGE_DOCK_LEFT = 1 << 4;
	public static final int EDGE_DOCK_RIGHT = 1 << 5;
	public static final int EDGE_MASK = 0xf;

	// 边缘长时间触发
	public void onEdge(IDragObject obj, int edgeType);

	public void onEnterEdge(IDragObject obj, int edgeType);

	public void onLeaveEdge(IDragObject obj, int edgeType);

	public void onDragFinish(IDragObject obj, float x, float y);

	public void onDragMove(IDragObject obj, float x, float y);

	public void focusTrash(IDragObject obj);
}
