package com.jiubang.ggheart.apps.desks.diy.frames.drag;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.view.View;

import com.go.util.graphics.DrawUtils;
import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
/**
 * 
 * @author jiangchao
 *
 */
public abstract class IDragObject {
	public static final int ANIMDURATION = 350;
	public static final int STATE_NORMAL = 0;
	public static final int STATE_AUTO_FLY = 1;

	private static final int FADE_IN = 0; // 渐现
	private static final int FADE_OUT = 1; // 渐隐
	// 速度减少的倍数
	private static final float VOLECITY_SCALE = DrawUtils.sDensity;
	// 位置信息
	protected int mLeft = 0;
	protected int mTop = 0;

	protected long mStartTime = 0;
	protected int mDrawState = STATE_NORMAL;
	protected boolean mAutoFly = false;
	protected int mAlpha = 255;
	protected int[] mSrcXY;
	protected int[] mDestXY;
	protected float[] mDecelerationXY;
	protected float[] mVelocityXY;

	public abstract int getLeft();

	public abstract int getTop();

	public abstract int getRight();

	public abstract int getBottom();

	public abstract int getWidth();

	public abstract int getHeight();

	public abstract void setVisable(boolean bVisable);

	public abstract boolean isVisable();

	public abstract void layout(int l, int t, int r, int b);

	public abstract void draw(Canvas canvas);

	public abstract void scale(float xScale, float yScale);

	public abstract void setColor(int color, PorterDuff.Mode mode);

	private View mDragView;
	private int mDragType;

	/**
	 * 自动飞行动画
	 * 
	 * @param srcXY
	 *            飞行起点
	 * @param destXY
	 *            飞行终点
	 * @param duration
	 *            飞行时间 -1 则为默认时间
	 */
	public void autoFly(int[] srcXY, int[] destXY, long duration, View dragView, int dragType) {
		mDragView = dragView;
		mAutoFly = true;
		mSrcXY = srcXY;
		mDestXY = destXY;
		mDragType = dragType;
		if (duration == -1) {
			duration = ANIMDURATION;
		}
		if (mDecelerationXY == null) {
			mDecelerationXY = new float[2];
		}
		if (mVelocityXY == null) {
			mVelocityXY = new float[2];
		}
		long t = duration;
		mDecelerationXY[0] = 2 * (mDestXY[0] - mSrcXY[0] + 0.1f) / (t * t);
		mDecelerationXY[1] = 2 * (mDestXY[1] - mSrcXY[1] + 0.1f) / (t * t);
		mVelocityXY[0] = mDecelerationXY[0] * t;
		mVelocityXY[1] = mDecelerationXY[1] * t;
		mDrawState = STATE_AUTO_FLY;
		layout(mSrcXY[0], mSrcXY[1], mSrcXY[0] + getWidth(), mSrcXY[1] + getHeight());
	};

	/**
	 * 自动飞行动画
	 * 
	 * @param velocityXY
	 *            飞行起始速度
	 * @param duration
	 *            飞行时间
	 */
	public void autoFly(float[] velocityXY, long duration) {
		mAutoFly = true;
		mSrcXY = new int[] { getLeft(), getTop() };
		if (duration == -1) {
			duration = ANIMDURATION;
		}
		if (mDecelerationXY == null) {
			mDecelerationXY = new float[2];
		}
		mVelocityXY = velocityXY;
		final long t = duration;
		mDecelerationXY[0] = mVelocityXY[0] / t;
		mDecelerationXY[1] = mVelocityXY[1] / t;
		mDrawState = STATE_AUTO_FLY;
		layout(mSrcXY[0], mSrcXY[1], mSrcXY[0] + getWidth(), mSrcXY[1] + getHeight());
	};

	/**
	 * 飞行到删除
	 * 
	 * @param velocityXY
	 *            飞行起始速度
	 * @param duration
	 *            飞行时间
	 */
	public void flyToDelete(float[] velocityXY, long duration) {
		mSrcXY = new int[] { getLeft(), getTop() };
		if (duration == -1) {
			duration = ANIMDURATION;
		}
		if (mDecelerationXY == null) {
			mDecelerationXY = new float[2];
		}
		if (mVelocityXY == null) {
			mVelocityXY = new float[2];
		}
		final long t = duration;
		mVelocityXY[0] = velocityXY[0] / (VOLECITY_SCALE + 0.01f);
		// float maxVelocityY = mSrcXY[1] * 2 / t;
		mVelocityXY[1] = velocityXY[1] / (VOLECITY_SCALE + 0.01f);
		mDecelerationXY[0] = mVelocityXY[0] / t;
		mDecelerationXY[1] = mVelocityXY[1] / t;
		mDrawState = STATE_AUTO_FLY;
		layout(mSrcXY[0], mSrcXY[1], mSrcXY[0] + getWidth(), mSrcXY[1] + getHeight());
	};

	public void drawAutoFly(Canvas canvas, long currentTime) {
		float t = currentTime;
		final int fadeType = mAutoFly ? FADE_IN : FADE_OUT;
		mAlpha = getAlpha(Math.min(1, t / ANIMDURATION), fadeType);
		float decDistance = mDecelerationXY[0] * t * t / 2;
		final int currentX = (int) (mSrcXY[0] + mVelocityXY[0] * t - decDistance);

		decDistance = mDecelerationXY[1] * t * t / 2;
		final int currentY = (int) (mSrcXY[1] + mVelocityXY[1] * t - decDistance);

		layout(currentX, currentY, currentX + getWidth(), currentY + getHeight());
		// 如果整个item已经fly出屏幕，则跳出动画
		final boolean stopFly = currentY + getHeight() <= 0 || currentX >= DrawUtils.sWidthPixels
				|| currentX + getWidth() <= 0 ? true : false;
		if (currentTime >= ANIMDURATION || stopFly) {
			mStartTime = 0;
			mDrawState = STATE_NORMAL;
			if (mAutoFly) {
				// 通知给监听者移动完成
				mAutoFly = false;
				GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						IFrameworkMsgId.REMOVE_FRAME, IDiyFrameIds.DRAG_FRAME, null, null);
				
				//检查是否DOCK条添加图标。
				if (checkDockAddIcon()) {
					return;
				}
				
				Rect rect = new Rect(getLeft(), getTop(), getRight(), getBottom());
				List<Rect> list = new ArrayList<Rect>();
				list.add(rect);
				//如果不在添加页面就停止最后的添加
				if (GoLauncher.getFrame(IDiyFrameIds.SCREEN_EDIT_BOX_FRAME) == null) {
					return;
				}
				if (mDragType == DragFrame.TYPE_ADD_ITEM_IN_FOLDER) { // 添加图标到新建文件夹
					GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
							IDiyMsgIds.FLY_APP_TO_FOLDER, DragFrame.TYPE_ADD_ITEM_IN_FOLDER,
							mDragView, list);
				} else { // 普通添加情况
					GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
							IDiyMsgIds.ADD_ITEM_TO_SCREEN, -1, mDragView, list);
				}
			} else {
				GoLauncher.sendMessage(this, IDiyFrameIds.DRAG_FRAME,
						IDiyMsgIds.CLOSE_SCREEN_DRAG_FRAME, -1, null, null);
			}
		}
	};
	
	/**
	 * <br>功能简述:判断是否DOCK条添加图标
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public boolean checkDockAddIcon() {
		//判断是否在dock添加图标页面
		if (GoLauncher.getFrame(IDiyFrameIds.DOCK_ADD_ICON_FRAME) != null && mDragType == DragFrame.TYPE_DOCK_ADD_ICON) {
			// ADT-9625 在dock栏中添加应用图标时，桌面中的指示器会出现丢失
			// 显示指示器,因为调用的dragframe会隐藏指示器，所以要发消息去取消
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.SHOW_INDICATOR, -1,
					null, null);
			//发送信息给dock条添加一个icon
			GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME, IDiyMsgIds.DOCK_ADD_ICON_ADD_ONE,
					1, mDragView.getTag(), null);
			//发送信息给dock条添加一个icon
			GoLauncher.sendMessage(this, IDiyFrameIds.SHELL_FRAME, IDiyMsgIds.DOCK_ADD_ICON_ADD_ONE,
					1, mDragView.getTag(), null);
			return true;
		}
		return false;
	}

	/**
	 * 减速的三次曲线插值
	 * 
	 * @param begin
	 * @param end
	 * @param t
	 *            应该位于[0, 1]
	 * @return
	 */
	private int getAlpha(float t, int fadeType) {
		t = 1 - t;
		int begin = 255;
		int end = 0;
		if (fadeType == FADE_IN) {
			begin = 0;
			end = 255;
		}
		return (int) (begin + (end - begin) * (1 - t * t * t));
	}
}