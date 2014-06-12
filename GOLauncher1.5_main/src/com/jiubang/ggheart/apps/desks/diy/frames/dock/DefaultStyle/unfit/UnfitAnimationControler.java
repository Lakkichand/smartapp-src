package com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.unfit;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.AbsDockAnimationControler;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.AbsDockView;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.AreaModel;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.DockIconView;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.DockLineLayoutContainer;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.IDragPositionListner;
import com.jiubang.ggheart.apps.desks.dock.DockUtil;

/**
 * 
 * <br>类描述:非自适应模式dock移动动画类
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-7-1]
 */
public class UnfitAnimationControler extends AbsDockAnimationControler {
	private int mDockInAreaIndex = -1; // 拖动图标所在区域的位置

	private int mIconWidth; // 图标的宽度
	private int mIconHeight; // 图标的高度
	private int mOneAreaWidth; // 区域宽度
	private int mOneAreaHeight; // 区域的高度
	private int mPaddingTop;
	private int mPaddingBottom;
	private int mPaddingLeft;
	private int mPaddingRight;

	private AreaModel mAreaModel; // 正常的区域

	private int mCount = DockUtil.ICON_COUNT_IN_A_ROW;
	private int mLastAreaPosition = -1; // 最后一次移动到区域位置
	private int mLastMoveToDirection = -1; // 最后一次移动到区域的哪个方向

	private IDragPositionListner mDragPositionListner;

	public void setmDragPositionListner(IDragPositionListner mDragPositionListner) {
		this.mDragPositionListner = mDragPositionListner;
	}

	public UnfitAnimationControler(int mLayoutWidth, int mLayoutHeight, int mIcon_H_portrait,
			int mIcon_W_landscape, ArrayList<DockIconView> mDockViewList, Context mContext,
			View dragView, DockLineLayoutContainer mLineLayoutContainer) {
		super(mLayoutWidth, mLayoutHeight, mIcon_H_portrait, mIcon_W_landscape, mDockViewList,
				mContext, dragView, mLineLayoutContainer);
	}

	public boolean viewMoveAnimation(Point point, View dragView) {
		if (point == null || dragView == null) {
			return false;
		}

		if (mAreaModel == null) {
			initData();
			initDockInAreaIndex(dragView);
		}

		if (checkInDock(point)) {
			sendAnimationDockToDockMsg(point);
			return true;
		} else {
			mDragPositionListner.setRecycleDragCache();
			return false;
		}
	}

	/**
	 * 初始化数据
	 * 
	 * @param dockIconView
	 */
	public void initData() {
		initBorder();
		initDockIconViewPoint();
		initAreaModel();
	}

	/**
	 * 初始化基本数据
	 */
	public void initBorder() {
		// 根据具体情况来获取图标宽高
		mIconWidth = mIconHeight = DockUtil.getIconSize(mCount); // 图标的宽度

		if (AbsDockView.sPortrait) {
			mPaddingTop = mContext.getResources().getDimensionPixelSize(
					R.dimen.dock_icon_padding_top_port);
			mPaddingRight = mContext.getResources().getDimensionPixelSize(
					R.dimen.dock_icon_padding_bottom_port);
			mOneAreaWidth = mLayoutWidth / mCount;
			mPaddingLeft = (mOneAreaWidth - mIconWidth) / 2;
		} else {
			mPaddingLeft = mContext.getResources().getDimensionPixelSize(
					R.dimen.dock_icon_padding_left_land);
			mPaddingRight = mContext.getResources().getDimensionPixelSize(
					R.dimen.dock_icon_padding_right_land);
			mOneAreaHeight = mLayoutHeight / mCount;
			mPaddingTop = (mOneAreaHeight - mIconHeight) / 2;
		}
	}

	/**
	 * 初始化每个图标的初始坐标信息
	 */
	public void initDockIconViewPoint() {
		int point_x = 0;
		// 初始化所有ICON的初始位置
		for (int j = 0; j < mDockViewListSize; j++) {
			DockIconView dockIconView = mDockViewList.get(j);
			int indexInRow = dockIconView.getInfo().getmIndexInRow(); // VIEW所在位置
			// 保留每个位置
			if (AbsDockView.sPortrait) {
				point_x = mOneAreaWidth * indexInRow + mPaddingLeft;
			} else {
				point_x = mOneAreaHeight * (mCount - indexInRow) - mPaddingTop - mIconHeight;
			}
			dockIconView.setOldPointX(point_x); // 设置旧坐标
			dockIconView.setNewPointX(point_x); // 设置新坐标
			dockIconView.setAreaPosition(indexInRow); // 设置目前所在区域位置
		}
	}

	/**
	 * 初始化每个区域
	 */
	public void initAreaModel() {
		mAreaModel = new AreaModel();
		ArrayList<Rect> areasList = new ArrayList<Rect>();
		ArrayList<Rect> iconsList = new ArrayList<Rect>();
		Rect rect = null;
		if (AbsDockView.sPortrait) {
			for (int i = 0; i < mCount; i++) {
				rect = new Rect();
				rect.left = i * mOneAreaWidth;
				rect.right = (i + 1) * mOneAreaWidth;
				rect.top = mLayoutHeight - mIcon_H_portrait;
				rect.bottom = mLayoutHeight;
				areasList.add(rect);

				rect = new Rect();
				rect.left = i * mOneAreaWidth + mPaddingLeft;
				rect.right = (i + 1) * mOneAreaWidth - mPaddingLeft;
				rect.top = mLayoutHeight - mIcon_H_portrait + mPaddingTop;
				rect.bottom = mLayoutHeight - mPaddingBottom;
				iconsList.add(rect);
			}
		} else {
			for (int i = 0; i < mCount; i++) {
				rect = new Rect();
				rect.left = mLayoutWidth - mIcon_W_landscape;
				rect.right = mLayoutWidth;
				rect.top = (mCount - i - 1) * mOneAreaHeight;
				rect.bottom = (mCount - i) * mOneAreaHeight;
				areasList.add(rect);

				rect = new Rect();
				rect.left = mLayoutWidth - mIcon_W_landscape + mPaddingLeft;
				rect.right = mLayoutWidth - mPaddingRight;
				rect.top = (mCount - i - 1) * mOneAreaHeight + mPaddingTop;
				rect.bottom = (mCount - i) * mOneAreaHeight - mPaddingTop;
				iconsList.add(rect);
			}
		}
		mAreaModel.setAreasList(areasList);
		mAreaModel.setIconsList(iconsList);
	}

	/**
	 * 设置拿起DockIconView的位置为-1
	 * 
	 * @param view
	 */
	public void initDockInAreaIndex(View view) {
		if (view instanceof DockIconView) {
			DockIconView dragDockIconView = (DockIconView) view;
			dragDockIconView.setAreaPosition(-1);
		}
	}

	/**
	 * 设置插入到哪个区域和插入区域的哪个方向
	 * 
	 * @param areaModel
	 *            区域MODEL
	 * @param point
	 *            坐标
	 */
	public void setMoveToPosition(AreaModel areaModel, Point point) {
		ArrayList<Rect> areasList = areaModel.getAreasList();
		ArrayList<Rect> iconsList = areaModel.getIconsList();
		for (int i = 0; i < areasList.size(); i++) {
			Rect currentArea = areasList.get(i); // 区域
			if (currentArea.contains(point.x, point.y)) {
				// 获取合并的图标
				DockIconView dockIconView = null;
				for (DockIconView dockIconViewTemp : mDockViewList) {
					if (dockIconViewTemp.getAreaPosition() == i) {
						dockIconView = dockIconViewTemp;
						break;
					}
				}

				areaModel.setAreaPosition(i); // 插入的是第几个区域
				Rect iconRect = iconsList.get(i); // 图标空间
				areaModel.setMoveToDirection(DockUtil.MOVE_TO_OUT); // 设置默认的区域外
				if (AbsDockView.sPortrait) {
					// 右边
					if (point.x > iconRect.right) {
						areaModel.setMoveToDirection(DockUtil.MOVE_TO_RIGHT);
						mDragPositionListner.onRight(dockIconView);
						// Log.i("lch", i+":右边");
						return;
					}
					// 左边
					else if (point.x < iconRect.left) {
						areaModel.setMoveToDirection(DockUtil.MOVE_TO_LEFT);
						mDragPositionListner.onLeft(dockIconView);
						// Log.i("lch", i+":左边");
						return;
					}
					// 中间
					else if (iconRect.left < point.x && point.x < iconRect.right) {
						areaModel.setMoveToDirection(DockUtil.MOVE_TO_CENTENT);
						if (dockIconView != null) {
							mDragPositionListner.onMiddle(dockIconView, currentArea, i);
						}
						// Log.i("lch", i+":中间");
						return;
					}
				} else {
					// 右边
					if (point.y < iconRect.top) {
						areaModel.setMoveToDirection(DockUtil.MOVE_TO_RIGHT);
						mDragPositionListner.onRight(dockIconView);
						return;
					}
					// 左边
					else if (point.y > iconRect.bottom) {
						areaModel.setMoveToDirection(DockUtil.MOVE_TO_LEFT);
						mDragPositionListner.onLeft(dockIconView);
						return;
					}
					// 中间
					else if (iconRect.bottom > point.y && point.y > iconRect.top) {
						areaModel.setMoveToDirection(DockUtil.MOVE_TO_CENTENT);
						if (dockIconView != null) {
							mDragPositionListner.onMiddle(dockIconView, currentArea, i);
						}
						return;
					}
				}
				break;
			}
		}
		areaModel.setMoveToDirection(DockUtil.MOVE_TO_CENTENT);
	}

	/**
	 * 设置当前空白的位置
	 * 
	 * @param areaModel
	 */
	public void setAddIconList(AreaModel areaModel) {
		areaModel.setAddIconList(getAddIconIndex());
	}

	/**
	 * 合并文件夹
	 */
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case DockUtil.HANDLE_ANIMATION_START_MOVE : {
					startAnimationDockToDock();
				}
					break;
			}
		}
	};

	/**
	 * 取消动画消息
	 */
	public void cancleAnimationMessage() {
		mHandler.removeMessages(DockUtil.HANDLE_ANIMATION_START_MOVE);
	}

	/**
	 * dock 图标dock to dock 的交换动作
	 * 
	 * @param point
	 */
	public void sendAnimationDockToDockMsg(Point point) {
		setMoveToPosition(mAreaModel, point);
		setAddIconList(mAreaModel);
		int moveToDirection = mAreaModel.getMoveToDirection(); // 插入移除后区域的哪个方向
		int areaPosition = mAreaModel.getAreaPosition(); // 当前ICON所谓区域

		// 判断是否在空白位置移动
		ArrayList<Integer> addIconList = mAreaModel.getAddIconList();
		if (addIconList.contains(areaPosition)) {
			mDockInAreaIndex = areaPosition; // 处理中间有2个空格左右移动的情况
			return;
		}

		if (mLastAreaPosition != areaPosition || mLastMoveToDirection != moveToDirection) {
			mHandler.removeMessages(DockUtil.HANDLE_ANIMATION_START_MOVE);
			mHandler.sendEmptyMessageDelayed(DockUtil.HANDLE_ANIMATION_START_MOVE, 50); // 延时操作
			mLastAreaPosition = areaPosition;
			mLastMoveToDirection = moveToDirection;
		}
	}

	/**
	 * 图标的交换动作
	 * 
	 * @param point
	 */
	public void startAnimationDockToDock() {
		int new_icon_point = 0;
		int moveToDirection = mAreaModel.getMoveToDirection(); // 插入移除后区域的哪个方向
		int areaPosition = mAreaModel.getAreaPosition(); // 当前ICON所谓区域
		int rightAddIconIndex = getRightAddIconIndex(areaPosition); // 当前区域右边第一个空白位置
		int leftAddIconIndex = getLeftAddIconIndex(areaPosition); // 当前区域左边第一个空白位置

		boolean isMove = false; // 记录是否移动了
		DockIconView dockIconView = null;
		// 在区域左边
		if (moveToDirection == DockUtil.MOVE_TO_LEFT) {
			for (int i = rightAddIconIndex - 1; areaPosition <= i; i--) {
				for (int j = 0; j < mDockViewListSize; j++) {
					dockIconView = mDockViewList.get(j);
					if (dockIconView.getAreaPosition() == i && dockIconView != mDragView) {
						if (AbsDockView.sPortrait) {
							new_icon_point = mOneAreaWidth * (i + 1) + mPaddingLeft;
						} else {
							new_icon_point = mOneAreaHeight * (mCount - i - 1) - mPaddingTop
									- mIconHeight;
						}
						isMove = true;
						mDockInAreaIndex = areaPosition;
						dockIconView.setAreaPosition(i + 1); // 图标往右移一位
						startDockIconViewMoveAnimation(dockIconView, new_icon_point);
					}
				}
			}

			if (!isMove) {
				// 右边没有空的位置，图标移动到最左边图标的左边区域
				if (leftAddIconIndex + 1 == areaPosition || leftAddIconIndex == -1) {
					mDockInAreaIndex = areaPosition - 1;
					return;
				}
				for (int i = leftAddIconIndex + 1; i < areaPosition; i++) {
					for (int j = 0; j < mDockViewListSize; j++) {
						dockIconView = mDockViewList.get(j);
						if (dockIconView.getAreaPosition() == i && dockIconView != mDragView) {
							if (AbsDockView.sPortrait) {
								new_icon_point = mOneAreaWidth * (i - 1) + mPaddingLeft;
							} else {
								new_icon_point = mOneAreaHeight * (mCount - i + 1) - mPaddingTop
										- mIconHeight;
							}
							isMove = true;
							mDockInAreaIndex = areaPosition - 1; // 拿起的图标位置-1
							dockIconView.setAreaPosition(i - 1);
							startDockIconViewMoveAnimation(dockIconView, new_icon_point);
						}
					}
				}
			}
		}

		// 在区域右边
		else if (moveToDirection == DockUtil.MOVE_TO_RIGHT) {
			if (leftAddIconIndex != -1) {
				for (int i = leftAddIconIndex + 1; i <= areaPosition; i++) {
					for (int j = 0; j < mDockViewListSize; j++) {
						dockIconView = mDockViewList.get(j);
						if (dockIconView.getAreaPosition() == i && dockIconView != mDragView) {
							if (AbsDockView.sPortrait) {
								new_icon_point = mOneAreaWidth * (i - 1) + mPaddingLeft;
							} else {
								new_icon_point = mOneAreaHeight * (mCount - i + 1) - mPaddingTop
										- mIconHeight;
							}
							isMove = true;
							mDockInAreaIndex = areaPosition;
							dockIconView.setAreaPosition(i - 1);
							startDockIconViewMoveAnimation(dockIconView, new_icon_point);
						}
					}
				}
			}

			if (!isMove) {
				if (rightAddIconIndex - 1 == areaPosition || rightAddIconIndex == -1) {
					mDockInAreaIndex = areaPosition + 1;
					return;
				}
				for (int i = rightAddIconIndex - 1; i > areaPosition; i--) {
					for (int j = 0; j < mDockViewListSize; j++) {
						dockIconView = mDockViewList.get(j);
						if (dockIconView.getAreaPosition() == i && dockIconView != mDragView) {
							if (AbsDockView.sPortrait) {
								new_icon_point = mOneAreaWidth * (i + 1) + mPaddingLeft;
							} else {
								new_icon_point = mOneAreaHeight * (mCount - i - 1) - mPaddingTop
										- mIconHeight;
							}
							isMove = true;
							mDockInAreaIndex = areaPosition + 1;
							dockIconView.setAreaPosition(i + 1);
							startDockIconViewMoveAnimation(dockIconView, new_icon_point);
						}
					}
				}
			}
		}
	}

	/**
	 * 获取右边最近的空位置
	 * 
	 * @param areaPosition
	 * @return
	 */
	public int getRightAddIconIndex(int areaPosition) {
		ArrayList<Integer> addIconList = mAreaModel.getAddIconList();
		int addIconListSize = addIconList.size();
		for (int i = 0; i < addIconListSize; i++) {
			if (addIconList.get(i) > areaPosition) {
				return addIconList.get(i);
			}
		}
		return -1;
	}

	/**
	 * 获取左边最近的空位置
	 * 
	 * @param areaPosition
	 * @return
	 */
	public int getLeftAddIconIndex(int areaPosition) {
		ArrayList<Integer> addIconList = mAreaModel.getAddIconList();
		int addIconListSize = addIconList.size();
		for (int i = addIconListSize - 1; i >= 0; i--) {
			if (addIconList.get(i) < areaPosition) {
				return addIconList.get(i);
			}
		}
		return -1;
	}

	/**
	 * 执行动画
	 * 
	 * @param dockIconView
	 * @param newPoint_x
	 *            新坐标位置
	 */
	public void startDockIconViewMoveAnimation(final DockIconView dockIconView, int newPoint_x) {
		int fromDelta = dockIconView.getNewPointX() - dockIconView.getOldPointX(); // 起始位置
		int toDelta = newPoint_x - dockIconView.getOldPointX(); // 移动距离
		dockIconView.setNewPointX(newPoint_x); // 保存新坐标

		long duration = 100; // 动画时间
		Animation animationTranslate = null;

		if (AbsDockView.sPortrait) {
			animationTranslate = new TranslateAnimation(fromDelta, toDelta, 0, 0);
		} else {
			animationTranslate = new TranslateAnimation(0, 0, fromDelta, toDelta);
		}

		animationTranslate.setDuration(duration);
		animationTranslate.setFillAfter(true);
		dockIconView.startAnimation(animationTranslate);

		// 设置加号位置
		mDragPositionListner.setAddIconIndex(getAddIconIndex());
	}

	public ArrayList<Integer> getAddIconIndex() {
		ArrayList<Integer> addIconList = new ArrayList<Integer>();
		for (int i = 0; i < mCount; i++) {
			boolean isInArea = false;
			for (int j = 0; j < mDockViewListSize; j++) {
				DockIconView dockIconView = mDockViewList.get(j);
				int areaPosition = dockIconView.getAreaPosition();
				if (areaPosition == i) {
					isInArea = true;
					break;
				}
			}
			if (!isInArea) {
				addIconList.add(i);
			}
			// Collections.sort(addIconList);
		}
		return addIconList;
	}

	/**
	 * 返回当前图标在哪个区域的位置
	 * 
	 * @return
	 */
	public int getDockInAreaIndex() {
		return mDockInAreaIndex;
	}

	/**
	 * 删除当前图标所有的动画 和复位标志位
	 */
	public void clearAnimationAndresetFlag() {
		// 清除动画
		for (DockIconView dockIconView : mDockViewList) {
			dockIconView.clearAnimation();
		}
		mAreaModel = null;
	}
}
