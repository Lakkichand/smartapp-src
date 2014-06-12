package com.jiubang.ggheart.apps.desks.diy.frames.drag;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.graphics.DrawUtils;
import com.jiubang.core.framework.AbstractFrame;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.DockIconView;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.CellLayout;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.Workspace;
import com.jiubang.ggheart.apps.desks.dock.DockUtil;
import com.jiubang.ggheart.components.DeskToast;
import com.jiubang.ggheart.data.info.ShortCutSettingInfo;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 */
public class MyDragFrame implements ISelfObject {
	private static final String TAG = "MyDragFrame";

	public static final int EDGE_IGNORE = 20/* dpi */;
	public static final int EDGE_DOCK_IGNORE_H = 5/* dpi */;
	public static final int EDGE_DURATION = 500/* ms */;
	private long mEdgeDuration = EDGE_DURATION;

	// 内部Handle消息标记
	private static final int MSG_EDGE_IDENTITY = 0;
	private static final int MSG_SHOW_PRESCREEN = 1;
	private static final int MSG_SHOW_NEXTSCREEN = 2;

	private int mEdgeType = 0;
	private boolean mEdgeTiming = false;

	private boolean mIsSelfLayout;
	private DragLayout mDragLayout;
	private int[] mDragLayoutPosition;

	protected IDragObject mDragObject;
	protected Rect mDragRect;
	protected Point mDragPoint;
	protected IDragListener mDragListener;

	private boolean mDragFinished;
	private float mLastMotionX = -1f;
	private float mLastMotionY = -1f;

	// 用于通知桌面关闭弹出菜单
	private boolean mNotifyCloseMenu = false; // 第一次移动，需要通知桌面关闭弹出菜单
	private int mDownX = -1; // 第一次按下的x
	private int mDownY = -1; // 第一次按下的y
	private int mMaxMoveX = -1;
	private int mMaxMoveY = -1;

	private int mScreenH = 0;
	private int mScreenW = 0;
	private int mDockSize = 0;
	private int mMinimumFlingVelocity;
	private MotionEvent mCurrentDownEvent;
	private boolean mScaleForMerge = false;

	private boolean mIsSmallState = false;

	private DragFrameListener mDragFrameListener;
	// 上一个拖拽区域的记录，用来记录离开dock时进行消息通知
	private int mLastDragType = -1;

	private boolean mTrahshGone = false; // 垃圾箱是否隐藏
	private VelocityTracker mVelocityTracker; // 速度跟踪器
	private int mMaxVelocity; // 最大速度
	private static final float FLY_VELOCITY = 0.8f;

	public boolean mIsFlying; // 如果图标正在飞，不执行touch事件
	private int mFlingToDeleteThresholdVelocity; // 甩动删除的速度上限
	private static final float MAX_FLING_DEGREES = 35f;
	protected int mExtraDragX; // 拖拽松手时，拖拽点对x坐标的增减值（向左移屏就+screenWidth，向右就-screenWidth）
	private boolean mDragingInPreview = false; // 是否拖拽进入屏幕预览的标识
	
	public MyDragFrame(Context context) {
		mDragLayout = new DragLayout(context);
		ViewGroup.LayoutParams vlp = new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);
		mDragLayout.setLayoutParams(vlp);
		mIsSelfLayout = true;

		mDragLayoutPosition = new int[2];

		mScreenH = GoLauncher.getDisplayHeight();
		mScreenW = GoLauncher.getDisplayWidth();
		// mDockSize =
		// context.getResources().getDimensionPixelSize(R.dimen.screen_long_end_padding_port);
		final ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
		mMinimumFlingVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
		mMaxVelocity = viewConfiguration.getScaledMaximumFlingVelocity();
		mFlingToDeleteThresholdVelocity = (int) (context.getResources().getInteger(R.integer.config_flingToDeleteMinVelocity) * DrawUtils.sDensity);
	}

	@Override
	public void selfConstruct() {

	}

	@Override
	public void selfDestruct() {
		// 外部参数直接置NULL
		mDragObject = null;
		mDragRect = null;
		mDragPoint = null;
		mDragListener = null;

		// 自己生成，需释放
		stopEdgeTimers();
		mHandler = null;
		if (null != mDragLayout) {
			if (!mIsSelfLayout) {
				mDragLayout.selfDestruct();
			}
			mDragLayout = null;
		}
		mDragLayoutPosition = null;
	}

	public void init(IDragObject object, Rect objectRc, Point fingrePoint) {
		if (null == object) {
			Log.i(TAG, "init function param object is null");
			return;
		}
		if (null == objectRc) {
			Log.i(TAG, "init function param object rect is null");
			return;
		}

		mDragObject = object;
		mDragRect = objectRc;
		mDragPoint = fingrePoint;
		if (null == mDragPoint) {
			mDragPoint = new Point(0, 0);
		}

		if (object instanceof DragView) {
			DragView dragView = (DragView) object;
			if (null != dragView.getDragView()) {
				View trash = mDragLayout.findViewById(R.id.trash_area_stub);
				mDragLayout.removeAllViews();
				if (trash != null) {
					mDragLayout.addView(trash);
				}
				mDragObject
						.layout(mDragRect.left, mDragRect.top, mDragRect.right, mDragRect.bottom);
				mDragLayout.setDragView((DragView) mDragObject);
				// mDragLayout.addView(dragView.getDragView());
			}
		} else if (object instanceof DragImage) {
			mDragObject.layout(mDragRect.left, mDragRect.top, mDragRect.right, mDragRect.bottom);
			mDragLayout.setDragImage((DragImage) mDragObject);
		} else {
			Log.i(TAG, "init function param object is illegal");
		}
	}

	public void register(IDragListener listener) {
		mDragListener = listener;
	}

	public void unregister(IDragListener listener) {
		mDragListener = null;
	}

	public void setContentView(DragLayout layout) {
		if (null != layout) {
			mDragLayout = layout;
			mIsSelfLayout = false;
		}
	}

	public View getContentView() {
		return mDragLayout;
	}

	public IDragObject getDragObject() {
		return mDragObject;
	}

	public void setDragRect(Rect rect) {
		mDragRect = rect;
	}

	public Rect getDragRect() {
		return mDragRect;
	}

	public Point getDragPoint() {
		return mDragPoint;
	}

	public void setEdgeDuration(long duration) {
		mEdgeDuration = duration;
	}

	public long getEdgeDuration() {
		return mEdgeDuration;
	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @param distanceX
	 * @param distanceY
	 * @param dragUp 是否已经放开拖拽
	 * @return
	 */
	private boolean scroll(float x, float y, float distanceX, float distanceY, boolean dragUp) {
		// 显示
		// TODO DragLayout自己绘制 BubbleTextView不能显示,所以才会有设置隐藏、显示
		if (null != mDragObject) {
			boolean visable = mDragObject.isVisable();
			// 没有排版隐藏起来
			if (0 == mDragObject.getWidth() && 0 == mDragObject.getHeight()) {
				visable = visable ? false : visable;
			} else {
				visable = !visable ? true : visable;
			}

			mDragObject.setVisable(visable);
			// 排版完成后显示
			if (visable) {
				int offsetX = (int) distanceX;
				int offsetY = (int) distanceY;
				// 有偏移点
				// 由于Drag可能没有排版，这个条件存在缺陷
				// if (e2.getX() == -distanceX && e2.getY() == -distanceY)
				{
					if (0 != mDragPoint.x && 0 != mDragPoint.y) {
						offsetX = mDragPoint.x - (int) x;
						offsetY = mDragPoint.y - (int) y;
					} else {
						offsetX = 0;
						offsetY = 0;
					}
				}
				mDragPoint.set((int) x, (int) y);
				mDragRect.offset(-offsetX, -offsetY);
				mDragObject
						.layout(mDragRect.left, mDragRect.top, mDragRect.right, mDragRect.bottom);
			}
		}

		// 当前情况
		if (null != mDragListener) {
			mDragListener.onFingerPointF(mDragObject, x, y);
			if (null != mDragRect) {
				int dragType = ScreenDragHandler.DRAG_IN_WORKSPACE;
				View dragView = null;
				// 区分为三种区域内的拖拽workspace，dock， userFolder
				AbstractFrame folderFrame = GoLauncher
						.getFrame(IDiyFrameIds.DESK_USER_FOLDER_FRAME);
				final boolean isDragInFolder = folderFrame != null
						&& folderFrame.getVisibility() == View.VISIBLE;
				// 文件夹内
				if (isDragInFolder) {
					dragType = ScreenDragHandler.DRAG_IN_FOLDER;
				} else if (mDragFrameListener != null) {
					dragView = mDragFrameListener.getDragView();
					if (dragView != null
							&& Workspace.isOverDock(dragView, new Point(mDragRect.centerX(),
									mDragRect.centerY()), mDragLayout.getHeight(), DrawUtils.sWidthPixels)) {
						// 在dock内
						dragType = ScreenDragHandler.DRAG_IN_DOCK;
					}
				}
				// // 上次拖拽区域在dock内，并且当前区域不在dock内,快速拖动图标离开DOCK条，mLastDragType=-1
				// if((mLastDragType == -1 && dragView != null && dragView
				// instanceof DockIconView) ||
				// (mLastDragType == ScreenDragHandler.DRAG_IN_DOCK &&
				// mLastDragType != dragType)){
				// // 再执行一次dock的拖拽处理（离开dock区域的处理）
				// mDragListener.onCenterPointF(mDragObject,
				// mDragRect.centerX(), mDragRect.centerY(),
				// ScreenDragHandler.DRAG_IN_DOCK);
				// }

				// 上次拖拽区域在dock内，并且当前区域不在dock内,快速拖动图标离开DOCK条，mLastDragType=-1
				if ((mLastDragType == -1 && dragView != null && dragView instanceof DockIconView)
						|| mLastDragType != dragType) {
					if (mLastDragType != ScreenDragHandler.DRAG_IN_WORKSPACE) {
						// 再执行一次dock的拖拽处理（离开dock区域的处理）
						mDragListener.onCenterPointF(mDragObject, mDragRect.centerX(),
								mDragRect.centerY(), ScreenDragHandler.DRAG_IN_DOCK);
					}

				}
				if (mLastDragType != dragType) {
					int[] types = new int[] { mLastDragType, dragType };
					// 广播出去，通知拖拽区域发生了变更
					GoLauncher.sendBroadcastMessage(this, IDiyMsgIds.DRAG_AREA_CHANGE, -1, types,
							null);
				}
				mLastDragType = dragType;
				mDragListener.onCenterPointF(mDragObject, mDragRect.centerX(), mDragRect.centerY(),
						dragType);
			}// end mDragRect
		}

		// 边界情况
		int lastType = mEdgeType;
		mEdgeType = checkEdge(x, y);
		if (IDragListener.EDGE_NONE == mEdgeType
				|| (CellLayout.sPortrait && IDragListener.EDGE_BOTTOM == mEdgeType)) {
			stopEdgeTimers();

			if (null != mDragListener && lastType != IDragListener.EDGE_NONE) {
				mDragListener.onLeaveEdge(mDragObject, mEdgeType);
			}
		} else {
			// 拖拽到水平边缘才会进行屏幕切换
			final boolean landEdge = mEdgeType == IDragListener.EDGE_LEFT || mEdgeType == IDragListener.EDGE_RIGHT;
			// 拖拽到边缘分两种情况：1、普通状态下 2、编辑状态下（不会进入屏幕预览）
			if (landEdge && (mIsSmallState || dragUp)) {
				final boolean edgeLeft = mEdgeType == IDragListener.EDGE_LEFT;
				if (dragUp) {
					final int msgId = edgeLeft
							? IDiyMsgIds.SCREEN_SHOW_PRESCREEN
							: IDiyMsgIds.SCREEN_SHOW_NEXTSCREEN;
					if (!mDragingInPreview) {
						mEdgeTiming = true;
						//图标widget要移动到的目标屏幕空格是否充足
						boolean isVacantAvaliable = mDragFrameListener != null ? GoLauncher
								.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
										IDiyMsgIds.IS_CELLLAYOUT_HAS_ENOUGHT_VACANT, edgeLeft
												? -1
												: 1, mDragFrameListener.getDragView(), null) : true;
						if (isVacantAvaliable
								&& GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, msgId,
										1, null, null)) {
							mExtraDragX = edgeLeft
									? DrawUtils.sWidthPixels
									: -DrawUtils.sWidthPixels;
						} else {
							// 如果不是在左右两端且向左右两边的移动就弹出提示
							if (GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
									IDiyMsgIds.IS_PREV_NEXT_SCREEN_AVALIBLE, edgeLeft ? -1 : 1,
									null, null)) {
								//图标widget要移动到的目标屏幕空格不足就弹出提示
								DeskToast.makeText(GoLauncher.getContext(), R.string.no_more_room,
										Toast.LENGTH_SHORT).show();
								GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.BACK_TO_ORIGINAL_POSITION, -1, null,
										null);
							}
						}

					}
				} else if (mNotifyCloseMenu) {
					final int showId = edgeLeft ? MSG_SHOW_PRESCREEN : MSG_SHOW_NEXTSCREEN;
					startEdgeTimer(showId);
				}
			}// end if(mIsSmallState)
			else if (!mIsSmallState) {
				// 菜单未关闭时不进行计时
				if (mNotifyCloseMenu) {
					startEdgeTimer(MSG_EDGE_IDENTITY);
				}

				if (null != mDragListener && lastType == IDragListener.EDGE_NONE) {
					mDragListener.onEnterEdge(mDragObject, mEdgeType);
				}

			}
		}

		return true;
	}

	/**
	 * 
	 * @param MotionEvent
	 * @param dragType
	 *            拖拽的类型（区别是否为文件夹内的拖拽）
	 * @return
	 */
	public boolean onTouchEvent(MotionEvent e, int dragType) {
		if (mDragFinished || mIsFlying) {
			return false; // 已经拖动完成的时候，如果有动画，并且再触碰，
							// 就屏蔽触摸事件，防止把拖动的视图再次显示
		}
		VelocityTracker verTracker = null;
		if (dragType != DragFrame.TYPE_PREVIEW_DRAG) {
			acquireVelocityTrackerAndAddMovement(e);
			verTracker = mVelocityTracker;
		}
		boolean bRet = false;
		final float currentX = e.getX() >= 0 ? e.getX() : 0;
		final float currentY = e.getY() >= 0 ? e.getY() : 0;
		if (mDownX < 0 || mDownY < 0) {
			mDownX = (int) currentX;
			mDownY = (int) currentY;
		}

		switch (e.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN : {
				mLastMotionX = currentX;
				mLastMotionY = currentY;
				bRet = true;
				// 上次拖拽类型置为-1
				mLastDragType = -1;
			}
				break;

			case MotionEvent.ACTION_MOVE : {
				if (mLastMotionX < 0f || mLastMotionY < 0f) {
					mLastMotionX = currentX;
					mLastMotionY = currentY;
				} else {
					float moveX = currentX - mLastMotionX;
					float moveY = currentY - mLastMotionY;
					// float velocityY = 0;
					// if(dragType != DragFrame.TYPE_PREVIEW_DRAG){
					// verTracker.computeCurrentVelocity(1, mMaxVelocity);
					// velocityY = verTracker.getYVelocity();
					// }
					// if(velocityY >= - FLY_VELOCITY){
					// move target
					scroll(currentX, currentY, moveX, moveY, false);
					// 计算瞬间速度
					// mDragListener.onDragMove(mDragObject, e.getX(),
					// e.getY());
					// if (!mNotifyCloseMenu)
					final int absX = Math.abs(mDownX - (int) currentX);
					if (absX > mMaxMoveX) {
						mMaxMoveX = absX;
					}

					final int absY = Math.abs(mDownY - (int) currentY);
					if (absY > mMaxMoveY) {
						mMaxMoveY = absY;
					}
                    // TODO:拖拽时的变化值不应该定为15，有些机型无法满足以下条件
					if (mMaxMoveX > DrawUtils.sTouchSlop || mMaxMoveY > DrawUtils.sTouchSlop) {
//						if (null != mDragListener) {
//							mDragListener.onDragMove(mDragObject, e.getX(), e.getY());
//						}
						if (!mNotifyCloseMenu) {
							GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
									IDiyMsgIds.DRAG_MOVE, -1, null, null);
							// GoLauncher.sendMessage(this,
							// IDiyFrameIds.DRAG_FRAME,
							// IDiyMsgIds.DRAG_MOVE, -1, null, null);

							mNotifyCloseMenu = true;
						}
					}
				}
				mLastMotionX = currentX;
				mLastMotionY = currentY;
				// }
			}
				break;

			case MotionEvent.ACTION_CANCEL :
			case MotionEvent.ACTION_UP : {
				stopEdgeTimers();
				if (null != mDragListener) {
					mDragFinished = true;
					// 上次拖拽类型置为-1
					mLastDragType = -1;
					float velocityY = 0;
					float velocityX = 0;
					if (dragType != DragFrame.TYPE_PREVIEW_DRAG) {
						verTracker.computeCurrentVelocity(1, mMaxVelocity);
						// 计算瞬间速度
						velocityX = verTracker.getXVelocity();
						velocityY = verTracker.getYVelocity();
					}

					// final int centerX = (mDragRect.right - mDragRect.left) /
					// 2;
					// final int centerY = (mDragRect.bottom - mDragRect.top) /
					// 2;
					boolean dragFinish = true;
					if (isFlingingToDelete(velocityX, velocityY)) {
//						// 计算甩动的斜率,对于手机屏幕而言，在y轴上，向上为负
//						final float flingSlope = -velocityY / (velocityX + 0.01f);
//						final float minSlope = (mDragRect.top + 0.01f) / (0.001f - mDragRect.left);
//						final float maxSlope = (mDragRect.top + 0.01f)
//								/ (DrawUtils.sWidthPixels - mDragRect.left + 0.001f);
//						if (flingSlope <= minSlope || flingSlope >= maxSlope) {
							// 如果是文件的拖拽，需要关闭文件夹
							if (dragType == DragFrame.TYPE_SCREEN_FOLDER_DRAG
									|| dragType == DragFrame.TYPE_DOCK_FOLDERITEM_DRAG) {
								// 关闭快速菜单
								GoLauncher.sendMessage(this, IDiyFrameIds.DESK_USER_FOLDER_FRAME,
										IDiyMsgIds.REMOVE_ACTION_MENU, -1, null, null);
								// 关闭文件夹
								GoLauncher.sendMessage(this, IDiyFrameIds.DESK_USER_FOLDER_FRAME,
										IDiyMsgIds.BACK_TO_MAIN_SCREEN, -1, null, null);
							}
							GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
									IDiyMsgIds.SCREEN_FLING_TO_CLEAR_OUTLINE_BITMAP, 0, null, null);
							mDragObject.flyToDelete(new float[] { velocityX, velocityY }, -1);
							dragFinish = false;
							// 飞行item不改变颜色，所以传的对象是null
							// mDragListener.focusTrash(null);

//						}
					}

					if (dragFinish) {
						if (mLastMotionX >= 0f && mLastMotionY >= 0f) {
							float moveX = currentX - mLastMotionX;
							float moveY = currentY - mLastMotionY;
							scroll(currentX, currentY, moveX, moveY, true);
						}
						if (mDragListener != null && mDragObject != null) {
							mDragListener.onDragFinish(mDragObject, e.getX(), e.getY());
						}
					}
					if (dragType != DragFrame.TYPE_PREVIEW_DRAG) {
						// 销毁速度跟踪器
						releaseVelocityTracker();
					}
				}
				bRet = true;
			}
				break;

			case MotionEvent.ACTION_POINTER_UP : {
				int pid = e.getAction() >> MotionEvent.ACTION_POINTER_ID_SHIFT;
				if (pid == 1) {
					if (null == mCurrentDownEvent) {
						break;
					}
					float upX = e.getX(pid);
					float upY = e.getY(pid);
					float downX = mCurrentDownEvent.getX(pid);
					float downY = mCurrentDownEvent.getY(pid);
					final float velocityX = upX - downX;
					final float velocityY = upY - downY;

					if (Math.abs(velocityX) > mMinimumFlingVelocity) {
						onFling(mCurrentDownEvent, e, velocityX, velocityY);
					}
				}
			}
				break;
			case MotionEvent.ACTION_POINTER_DOWN : {
				int pid = e.getAction() >> MotionEvent.ACTION_POINTER_ID_SHIFT;
				if (pid == 1) {
					if (mCurrentDownEvent != null) {
						mCurrentDownEvent.recycle();
					}
					mCurrentDownEvent = MotionEvent.obtain(e);
				}
			}
				break;
			default :
				break;
		}

		return bRet;
	}

	private int checkEdge(float fingerX, float fingerY) {
		AbstractFrame folderFrame = GoLauncher.getFrame(IDiyFrameIds.DESK_USER_FOLDER_FRAME);
		boolean isShowed = folderFrame != null && folderFrame.getVisibility() == View.VISIBLE;
		int edge = IDragListener.EDGE_NONE;
		if (isShowed) {
			// 在文件夹内拖动不响应边界
			return edge;
		}

		final int ignore = DrawUtils.dip2px(EDGE_IGNORE);
		// 横屏下移动到dock边缘，判断是否进入屏幕预览的区域减小
		final int dockIgnoreH = DrawUtils.dip2px(EDGE_DOCK_IGNORE_H);
		// 相对偏移量
		getContentView().getLocationOnScreen(mDragLayoutPosition);
		if (null != mDragLayoutPosition) {
			fingerX -= mDragLayoutPosition[0];
			fingerY -= mDragLayoutPosition[1];
		}

		mDockSize = ShortCutSettingInfo.sEnable ? DockUtil.getBgHeight() : 0;
		if (CellLayout.sPortrait) {
			if (fingerX <= getContentView().getLeft() + ignore && fingerY <= mScreenH - mDockSize) {
				/*** fingerY >= DrawUtils.dip2px(53) 是为了拖动dock上时不会触发边界进入预览 */
				edge |= IDragListener.EDGE_LEFT;
			}
			if (fingerY <= getContentView().getTop() + ignore) {
				edge |= IDragListener.EDGE_TOP;
			}
			// ADT-10132 非必现，桌面widget，将桌面的图标抛到下一屏幕（屏幕空间未满）失败时，弹出提示错误
			// ADT-10154 桌面widget，非必现，长按桌面图标松手后图标被甩到下一屏
			int right = getContentView().getRight();
			if (right > 0 && fingerX >= right - ignore && fingerY <= mScreenH - mDockSize) {
				edge |= IDragListener.EDGE_RIGHT;
			}

			// 暂时取消DOCK条换页功能
			// //dock条左边界
			// if (fingerX <= getContentView().getLeft() + ignore && fingerY >
			// mScreenH - mDockSize) {
			// edge |= IDragListener.EDGE_DOCK_LEFT;
			// }
			//
			// //dock条右边界
			// if (fingerX >= getContentView().getRight() - ignore && fingerY >
			// mScreenH - mDockSize) {
			// edge |= IDragListener.EDGE_DOCK_RIGHT;
			// }

			// if (fingerY >= getContentView().getBottom() - ignore)
			// {
			// edge |= IDragListener.EDGE_BOTTOM;
			// }
		} else {
			if (fingerX <= getContentView().getLeft() + ignore) {
				edge |= IDragListener.EDGE_LEFT;
			}
			if (fingerY <= getContentView().getTop() + ignore && fingerX <= mScreenW - mDockSize) {
				edge |= IDragListener.EDGE_TOP;
			}

			// 屏蔽右边检测，防止进入DOCK条误操作
			if (fingerX >= getContentView().getRight() - dockIgnoreH) {
				edge |= IDragListener.EDGE_RIGHT;
			}
			if (fingerY >= getContentView().getBottom() - ignore && fingerX <= mScreenW - mDockSize) {
				edge |= IDragListener.EDGE_BOTTOM;
			}
			// 暂时取消DOCK条换页功能
			// //dock条左边界
			// if (fingerY <= getContentView().getTop() + ignore && fingerX >
			// mScreenW - mDockSize)
			// {
			// edge |= IDragListener.EDGE_DOCK_LEFT;
			// }
			//
			// //dock条右边界
			// if (fingerY >= getContentView().getBottom() - ignore && fingerX >
			// mScreenW - mDockSize)
			// {
			// edge |= IDragListener.EDGE_DOCK_RIGHT;
			// }
		}

		// if (fingerX <= getContentView().getLeft() + ignore)
		// {
		// edge |= IDragListener.EDGE_LEFT;
		// }
		// if (fingerY <= getContentView().getTop() + ignore)
		// {
		// edge |= IDragListener.EDGE_TOP;
		// }
		// if (fingerX >= getContentView().getRight() - ignore)
		// {
		// edge |= IDragListener.EDGE_RIGHT;
		// }
		// if (fingerY >= getContentView().getBottom() - ignore)
		// {
		// edge |= IDragListener.EDGE_BOTTOM;
		// }

		return edge;
	}

	private void startEdgeTimer(int identity) {
		if (mEdgeTiming) {
			return;
		}
		mEdgeTiming = true;
		mHandler.sendEmptyMessageDelayed(identity, mEdgeDuration);
	}

	private void stopEdgeTimer(int identity) {
		if (!mEdgeTiming) {
			return;
		}
		mEdgeTiming = false;
		mHandler.removeMessages(identity);
	}

	private void stopEdgeTimers() {
		if (!mEdgeTiming) {
			return;
		}
		mEdgeTiming = false;
		mHandler.removeMessages(MSG_EDGE_IDENTITY);
		mHandler.removeMessages(MSG_SHOW_PRESCREEN);
		mHandler.removeMessages(MSG_SHOW_NEXTSCREEN);
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			final int message = msg.what;
			stopEdgeTimer(message);
			switch (message) {
				case MSG_EDGE_IDENTITY :
					if (null != mDragListener) {
						mDragingInPreview = true;
						mDragListener.onEdge(mDragObject, mEdgeType);
					}
					break;
				case MSG_SHOW_PRESCREEN : {
					GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
							IDiyMsgIds.SCREEN_SHOW_PRESCREEN, 1, null, null);
				}

					break;
				case MSG_SHOW_NEXTSCREEN : {
					GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
							IDiyMsgIds.SCREEN_SHOW_NEXTSCREEN, 1, null, null);
				}
					break;

				default :
					break;
			}
		};
	};

	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.DRAG_MOVE, -1, null,
				null);
		if ((int) velocityX > 0) {
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
					IDiyMsgIds.SCREEN_SHOW_PRESCREEN, 1, null, null);
		} else {
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
					IDiyMsgIds.SCREEN_SHOW_NEXTSCREEN, 1, null, null);
		}
		return false;
	}

	public void setMergeScale(boolean scale) {
		mScaleForMerge = scale;
	}

	public boolean isMergeScale() {
		return mScaleForMerge;
	}

	public void setSmallState(boolean bool) {
		mIsSmallState = bool;
	}

	public void setDragFrameListener(DragFrameListener listener) {
		mDragFrameListener = listener;
	}

	/**
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 */
	public interface DragFrameListener {
		public View getDragView();
	}

	/**
	 * 更新拖拽点的坐标，例如从文件夹里面拖拽一图标出来，这个时候状态栏收起，拖拽点发生变化
	 * 
	 * @param offsetX
	 * @param offsetY
	 */
	protected void updateDragPoints(int offsetX, int offsetY) {
		mDragRect.offset(-offsetX, -offsetY);
		mDragObject.layout(mDragRect.left, mDragRect.top, mDragRect.right, mDragRect.bottom);
	}

	public boolean isTrashGone() {
		return mTrahshGone;
	}

	public void setTrashGone() {
		mTrahshGone = true;
	}

	private void acquireVelocityTrackerAndAddMovement(MotionEvent ev) {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(ev);
	}

	private void releaseVelocityTracker() {
		if (mVelocityTracker != null) {
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}
	}

	public void setTrashVisible() {
		mTrahshGone = false;
	}

	public void setFlyState() {
		setTrashGone();
		View trash = mDragLayout.findViewById(R.id.trash_area_stub);
		if (trash != null) {
			trash.setVisibility(View.GONE);
		}
		mIsFlying = true;
	}

	 /**
     * Determines whether the user flung the current item to delete it.
     *
     * @return the vector at which the item was flung, or null if no fling was detected.
     */
	private boolean isFlingingToDelete(float velocityX, float velocityY) {
		final float velocityMX = velocityX * 1000;
		final float velocityMY = velocityY * 1000;
		if (velocityMY < mFlingToDeleteThresholdVelocity) {
			// Do a quick dot product test to ensure that we are flinging upwards
			PointF vel = new PointF(velocityMX, velocityMY);
			PointF upVec = new PointF(0f, -1f);
			float theta = (float) Math.acos(((vel.x * upVec.x) + (vel.y * upVec.y))
					/ (vel.length() * upVec.length()));
			if (theta <= Math.toRadians(MAX_FLING_DEGREES)) {
				return true;
			}
		}
		return false;
	}
}
