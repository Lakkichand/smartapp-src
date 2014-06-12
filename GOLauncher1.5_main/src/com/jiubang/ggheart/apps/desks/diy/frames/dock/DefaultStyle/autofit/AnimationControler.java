package com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.autofit;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.AbsDockAnimationControler;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.AbsDockView;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.AreaModel;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.DockIconView;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.DockLineLayoutContainer;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.IDragPositionListner;
import com.jiubang.ggheart.apps.desks.dock.DockUtil;
import com.jiubang.ggheart.components.BubbleTextView;

/**
 * 
 * <br>类描述:自适应模式dock移动动画类
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-6-1]
 */
public class AnimationControler extends AbsDockAnimationControler {

	private int mDockInAreaIndex; // 拖动图标所在区域的位置
	private int mDockInListIndex; // 拖动图标所在List的位置

	private AreaModel mNormalAreaModel; // 正常的区域
	private AreaModel mDelAreaModel; // 减少后的区域
	private AreaModel mAddAreaModel; // 添加后的区域

	// 竖屏
	private int mIconWidth; // 图标的宽度
	private int mIconLeftpadding;
	private int mIconRightpadding;
	private int mNormalOneAreaWidth; // 正常的一个区域宽度
	private int mNormalPaddingLeft; // 正常pandding left的长度
	private int mDelOneAreaWidth; // 移除后的Icon的宽度
	private int mDelPaddingLeft; // 移除后pandding left的长度
	private int mAddOneAreaWidth; // 移除后的Icon的宽度
	private int mAddPaddingLeft; // 移除后pandding left的长度

	// 横屏
	private int mIconHeight; // 图标的高度
	private int mIconToppadding;
	private int mIconBottompadding;
	private int mNormalOneAreaHeight; // 正常一个区域的高度
	private int mNormalPaddingtop; // 正常时候的padding_top
	private int mDelOneAreaHeight; // 删除一个图标后区域的高度
	private int mDelPaddingtop; // 删除一个图标后区域的padding_top
	private int mAddOneAreaHeigh;
	private int mAddPaddingtop;

	// 标志位
	private boolean mIsDeskIconScrentToDock = true;
	private boolean mIsDeskIconDockToScreen = false;
	private boolean mIsDockIconDockToScrent = true;
	private boolean mIsDockIconScrentToDock = false;
	private boolean mIsInMiddle = false; // 记录图标是否在图标中心。用于控制动画不重复

	private float mZoomInProportion;
	private float mZoomOutProportion;

	private IDragPositionListner mDragPositionListner;

	public void setmDragPositionListner(IDragPositionListner mDragPositionListner) {
		this.mDragPositionListner = mDragPositionListner;
	}

	public AnimationControler(int mLayoutWidth, int mLayoutHeight, int mIcon_H_portrait,
			int mIcon_W_landscape, ArrayList<DockIconView> mDockViewList, Context mContext,
			View dragView, DockLineLayoutContainer mLineLayoutContainer) {
		super(mLayoutWidth, mLayoutHeight, mIcon_H_portrait, mIcon_W_landscape, mDockViewList,
				mContext, dragView, mLineLayoutContainer);
		initZoomProportion();
	}

	/**
	 * 初始化大小图标缩放比例
	 */
	public void initZoomProportion() {
		float smallIconSize = DockUtil.getIconSize(DockUtil.ICON_COUNT_IN_A_ROW);
		float bigIconSize = DockUtil.getIconSize(DockUtil.ICON_COUNT_IN_A_ROW - 1);
		mZoomInProportion = bigIconSize / smallIconSize;
		mZoomOutProportion = smallIconSize / bigIconSize;
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
	 * 返回当前图标在哪个图标队列中的位置
	 * 
	 * @return
	 */
	public int getDockInListIndex() {
		return mDockInListIndex;
	}

	/**
	 * 初始化删除一个图标后的区域
	 */
	public void initDelAreaModel() {
		int count = mDockViewListSize - 1 == 0 ? 1 : mDockViewListSize - 1;

		if (AbsDockView.sPortrait) {
			mDelOneAreaWidth = mLayoutWidth / count;
			mDelPaddingLeft = (mDelOneAreaWidth - mIconWidth) / 2;
		} else {
			mDelOneAreaHeight = mLayoutHeight / count;
			mDelPaddingtop = (mDelOneAreaHeight - mIconHeight) / 2;
		}
		mDelAreaModel = getAreaPort(count); // -1区域
	}

	/**
	 * 初始化增加一个图标后的区域
	 */
	public void initAddAreaModel() {
		if (AbsDockView.sPortrait) {
			mAddOneAreaWidth = mLayoutWidth / (mDockViewListSize + 1); // 添加Area宽度
			mAddPaddingLeft = (mAddOneAreaWidth - mIconWidth) / 2; // 添加后pandding
																	// left的长度
		} else {
			mAddOneAreaHeigh = mLayoutHeight / (mDockViewListSize + 1);
			mAddPaddingtop = (mAddOneAreaHeigh - mIconHeight) / 2;
		}
		mDockInAreaIndex = -1; // 拖动图标所在位置
		mDockInListIndex = -1;
		mAddAreaModel = getAreaPort(mDockViewListSize + 1); // +1区域
	}

	/**
	 * 初始没做动画时的正常区域
	 */
	public void initNormalAreaModel() {
		int count = mDockViewListSize == 0 ? 1 : mDockViewListSize;

		// 根据具体情况来获取图标宽高
		mIconWidth = mIconHeight = DockUtil.getIconSize(mDockViewListSize); // 图标的宽度

		if (AbsDockView.sPortrait) {
			mIconToppadding = mContext.getResources().getDimensionPixelSize(
					R.dimen.dock_icon_padding_top_port);
			mIconBottompadding = mContext.getResources().getDimensionPixelSize(
					R.dimen.dock_icon_padding_bottom_port);
			mNormalOneAreaWidth = mLayoutWidth / count; // 原来的Area宽度
			mNormalPaddingLeft = (mNormalOneAreaWidth - mIconWidth) / 2; // 原来pandding
		} else {
			mIconLeftpadding = mContext.getResources().getDimensionPixelSize(
					R.dimen.dock_icon_padding_left_land);
			mIconRightpadding = mContext.getResources().getDimensionPixelSize(
					R.dimen.dock_icon_padding_right_land);
			mNormalOneAreaHeight = mLayoutHeight / count;
			mNormalPaddingtop = (mNormalOneAreaHeight - mIconHeight) / 2;
		}

		int point_x = 0;
		// 初始化所有ICON的初始位置
		for (int i = 0; i < mDockViewListSize; i++) {
			DockIconView dockIcon = mDockViewList.get(i);
			// 保留每个位置
			if (AbsDockView.sPortrait) {
				point_x = mNormalOneAreaWidth * i + mNormalPaddingLeft;
			} else {
				point_x = mNormalOneAreaHeight * (mDockViewListSize - i) - mNormalPaddingtop
						- mIconHeight;
			}
			dockIcon.setOldPointX(point_x); // 保存旧坐标
			dockIcon.setNewPointX(point_x); // 保存新坐标
			dockIcon.setAreaPosition(i); // 保存目前所在区域位置
		}
		mNormalAreaModel = getAreaPort(count); // 初始区域
	}

	/**
	 * 执行动画
	 * 
	 * @param dockIconView
	 *            需要动画的图标
	 * @param setListener
	 *            是否结束后监听
	 * @param fromXDelta
	 *            相对起始位置
	 * @param toXDelta
	 *            相对起始位置移动的距离
	 */
	public void startDockIconViewMoveAnimation(final DockIconView dockIconView, int newPoint_x,
			int type) {
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

		AnimationSet anim = new AnimationSet(true);

		if (type == DockUtil.ANIMATION_ZOOM_NORMAL_TO_BIG) {
			if (mDockViewListSize == DockUtil.ICON_COUNT_IN_A_ROW) {
				Animation myAnimation_Scale = new ScaleAnimation(1f, mZoomInProportion, 1f,
						mZoomInProportion, Animation.RELATIVE_TO_SELF, 0.5f,
						Animation.RELATIVE_TO_SELF, 0.5f);
				myAnimation_Scale.setDuration(100);
				anim.addAnimation(myAnimation_Scale);
			}
		}

		else if (type == DockUtil.ANIMATION_ZOOM_NORMAL_TO_SMALL) {
			if (mDockViewListSize == DockUtil.ICON_COUNT_IN_A_ROW - 1) {
				Animation myAnimation_Scale = new ScaleAnimation(1f, mZoomOutProportion, 1f,
						mZoomOutProportion, Animation.RELATIVE_TO_SELF, 0.5f,
						Animation.RELATIVE_TO_SELF, 0.5f);
				myAnimation_Scale.setDuration(duration);
				anim.addAnimation(myAnimation_Scale);
			}
		}

		else if (type == DockUtil.ANIMATION_ZOOM_BIG_TO_SMALL) {
			if (mDockViewListSize == DockUtil.ICON_COUNT_IN_A_ROW) {
				Animation myAnimation_Scale = new ScaleAnimation(mZoomInProportion, 1f,
						mZoomInProportion, 1f, Animation.RELATIVE_TO_SELF, 0.5f,
						Animation.RELATIVE_TO_SELF, 0.5f);
				myAnimation_Scale.setDuration(duration);
				anim.addAnimation(myAnimation_Scale);
			}
		}

		else if (type == DockUtil.ANIMATION_ZOOM_SMALL_TO_NORMAL) {
			if (mDockViewListSize == DockUtil.ICON_COUNT_IN_A_ROW - 1) {
				Animation myAnimation_Scale = new ScaleAnimation(mZoomOutProportion, 1f,
						mZoomOutProportion, 1f, Animation.RELATIVE_TO_SELF, 0.5f,
						Animation.RELATIVE_TO_SELF, 0.5f);
				myAnimation_Scale.setDuration(duration);
				anim.addAnimation(myAnimation_Scale);
			}
		}

		else if (type == DockUtil.ANIMATION_ZOOM_SMALL_TO_SMALL) {
			if (mDockViewListSize == DockUtil.ICON_COUNT_IN_A_ROW - 1) {
				Animation myAnimation_Scale = new ScaleAnimation(mZoomOutProportion,
						mZoomOutProportion, mZoomOutProportion, mZoomOutProportion,
						Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
				myAnimation_Scale.setDuration(duration);
				anim.addAnimation(myAnimation_Scale);
			}
		}

		anim.addAnimation(animationTranslate);
		anim.setFillAfter(true);
		dockIconView.startAnimation(anim);
	}

	/**
	 * 获取区域空间和图标空间
	 * 
	 * @param count
	 *            区域个数
	 * @return AreaModel
	 */
	public AreaModel getAreaPort(int count) {
		AreaModel areaModel = new AreaModel();
		ArrayList<Rect> areasList = new ArrayList<Rect>();
		ArrayList<Rect> iconsList = new ArrayList<Rect>();
		Rect rect = null;
		if (AbsDockView.sPortrait) {
			// -1状态
			if (count == mDockViewListSize - 1) {
				// 遍历4个空间
				for (int i = 0; i < count; i++) {
					// ICON空间
					rect = new Rect();
					rect.left = i * mDelOneAreaWidth;
					rect.right = (i + 1) * mDelOneAreaWidth;
					rect.top = mLayoutHeight - mIcon_H_portrait;
					rect.bottom = mLayoutHeight;
					areasList.add(rect);

					// 图标空间
					rect = new Rect();
					rect.left = i * mDelOneAreaWidth + mDelPaddingLeft;
					rect.right = (i + 1) * mDelOneAreaWidth - mDelPaddingLeft;
					rect.top = mLayoutHeight - mIcon_H_portrait + mIconToppadding;
					rect.bottom = mLayoutHeight - mIconBottompadding;
					iconsList.add(rect);
				}
			}
			// 初始状态
			else if (count == mDockViewListSize) {
				for (int i = 0; i < count; i++) {
					rect = new Rect();
					rect.left = i * mNormalOneAreaWidth;
					rect.right = (i + 1) * mNormalOneAreaWidth;
					rect.top = mLayoutHeight - mIcon_H_portrait;
					rect.bottom = mLayoutHeight;
					areasList.add(rect);

					rect = new Rect();
					rect.left = i * mNormalOneAreaWidth + mNormalPaddingLeft;
					rect.right = (i + 1) * mNormalOneAreaWidth - mNormalPaddingLeft;
					rect.top = mLayoutHeight - mIcon_H_portrait + mIconToppadding;
					rect.bottom = mLayoutHeight - mIconBottompadding;
					iconsList.add(rect);
				}
			}

			else if (count == mDockViewListSize + 1) {
				for (int i = 0; i < count; i++) {
					rect = new Rect();
					rect.left = i * mAddOneAreaWidth;
					rect.right = (i + 1) * mAddOneAreaWidth;
					rect.top = mLayoutHeight - mIcon_H_portrait;
					rect.bottom = mLayoutHeight;
					areasList.add(rect);

					rect = new Rect();
					rect.left = i * mAddOneAreaWidth + mAddPaddingLeft;
					rect.right = (i + 1) * mAddOneAreaWidth - mAddPaddingLeft;
					rect.top = mLayoutHeight - mIcon_H_portrait + mIconToppadding;
					rect.bottom = mLayoutHeight - mIconBottompadding;
					iconsList.add(rect);
				}
			}
		} else {
			// -1状态
			if (count == mDockViewListSize - 1) {
				for (int i = 0; i < count; i++) {
					rect = new Rect();
					rect.left = mLayoutWidth - mIcon_W_landscape;
					rect.right = mLayoutWidth;
					rect.top = (count - i - 1) * mDelOneAreaHeight;
					rect.bottom = (count - i) * mDelOneAreaHeight;
					areasList.add(rect);

					rect = new Rect();
					rect.left = mLayoutWidth - mIcon_W_landscape + mIconLeftpadding;
					rect.right = mLayoutWidth - mIconRightpadding;
					rect.top = (count - i - 1) * mDelOneAreaHeight + mDelPaddingtop;
					rect.bottom = (count - i) * mDelOneAreaHeight - mDelPaddingtop;
					iconsList.add(rect);
				}
			}
			// 初始状态
			else if (count == mDockViewListSize) {
				for (int i = 0; i < count; i++) {
					rect = new Rect();
					rect.left = mLayoutWidth - mIcon_W_landscape;
					rect.right = mLayoutWidth;
					rect.top = (count - i - 1) * mNormalOneAreaHeight;
					rect.bottom = (count - i) * mNormalOneAreaHeight;
					areasList.add(rect);

					rect = new Rect();
					rect.left = mLayoutWidth - mIcon_W_landscape + mIconLeftpadding;
					rect.right = mLayoutWidth - mIconRightpadding;
					rect.top = (count - i - 1) * mNormalOneAreaHeight + mNormalPaddingtop;
					rect.bottom = (count - i) * mNormalOneAreaHeight - mNormalPaddingtop;
					iconsList.add(rect);
				}
			}

			else if (count == mDockViewListSize + 1) {
				for (int i = 0; i < count; i++) {
					rect = new Rect();
					rect.left = mLayoutWidth - mIcon_W_landscape;
					rect.right = mLayoutWidth;
					rect.top = (count - i - 1) * mAddOneAreaHeigh;
					rect.bottom = (count - i) * mAddOneAreaHeigh;
					areasList.add(rect);

					rect = new Rect();
					rect.left = mLayoutWidth - mIcon_W_landscape + mIconLeftpadding;
					rect.right = mLayoutWidth - mIconRightpadding;
					rect.top = (count - i - 1) * mAddOneAreaHeigh + mAddPaddingtop;
					rect.bottom = (count - i) * mAddOneAreaHeigh - mAddPaddingtop;
					iconsList.add(rect);
				}
			}
		}
		areaModel.setAreasList(areasList);
		areaModel.setIconsList(iconsList);
		return areaModel;
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
				DockIconView dockIconView = null;
				for (DockIconView dockIconViewTemp : mDockViewList) {
					if (dockIconViewTemp.getAreaPosition() == i) {
						dockIconView = dockIconViewTemp;
					}
				}

				areaModel.setAreaPosition(i); // 插入的是第几个区域
				Rect iconRect = iconsList.get(i); // 图标空间
				areaModel.setMoveToDirection(DockUtil.MOVE_TO_OUT);
				if (AbsDockView.sPortrait) {
					// 右边
					if (point.x > iconRect.right) {
						areaModel.setMoveToDirection(DockUtil.MOVE_TO_RIGHT);
						mDragPositionListner.onRight(dockIconView);
						return;
					}
					// 左边
					else if (point.x < iconRect.left) {
						areaModel.setMoveToDirection(DockUtil.MOVE_TO_LEFT);
						mDragPositionListner.onLeft(dockIconView);
						return;
					}
					// 中间
					else if (iconRect.left < point.x && point.x < iconRect.right) {
						areaModel.setMoveToDirection(DockUtil.MOVE_TO_CENTENT);
						if (mDragView instanceof DockIconView) {
							if (dockIconView == mDragView && mDragView instanceof DockIconView) {
								mDragPositionListner.onMiddle(null, currentArea, i);
							} else {
								mDragPositionListner.onMiddle(dockIconView, currentArea, i);
							}
						} else if (mDragView instanceof BubbleTextView) {
							mDragPositionListner.onMiddle(dockIconView, currentArea, i);
						}
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
						if (dockIconView == mDragView) {
							mDragPositionListner.onMiddle(null, currentArea, i);
						} else {
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
	 * dock 图标 dock to screen 的交换动作
	 * 
	 * @param point
	 *            坐标
	 */
	public void setDockIconViewAnimationDockToScreen() {
		int new_icon_point = 0;
		int areaPosition = 0;
		for (int i = 0; i < mDockViewListSize; i++) {
			DockIconView dockIconView = mDockViewList.get(i);

			// 判断是否等于移除的图标
			if (i == mDockInListIndex) {
				dockIconView.setAreaPosition(-1); // 保存目前所在区域位置为-1
				continue;
			}
			if (AbsDockView.sPortrait) {
				if (i > mDockInListIndex) {
					new_icon_point = mDelOneAreaWidth * (i - 1) + mDelPaddingLeft; // 新的位置
																					// ，左移一位
				} else {
					new_icon_point = mDelOneAreaWidth * i + mDelPaddingLeft; // 新的位置，保持不变
				}
			} else {
				if (i < mDockInListIndex) {
					new_icon_point = mDelOneAreaHeight * (mDockViewListSize - i - 1)
							- mDelPaddingtop - mIconHeight; // 新的位置
					// ，左移一位
				} else {
					new_icon_point = mDelOneAreaHeight * (mDockViewListSize - i - 1)
							+ mDelPaddingtop; // 新的位置
					// ，左移一位
				}
			}
			dockIconView.setAreaPosition(areaPosition++); // 保存目前所在区域位置
			if (!mIsInMiddle) {
				startDockIconViewMoveAnimation(dockIconView, new_icon_point,
						DockUtil.ANIMATION_ZOOM_NORMAL_TO_BIG);
			}
		}
	}

	/**
	 * dock 图标screen to dock 的交换动作
	 * 
	 * @param point
	 *            坐标
	 */
	public void setDockIconViewAnimationScreenToDock(Point point) {
		setMoveToPosition(mDelAreaModel, point);
		int new_icon_point = 0; // 新的移动位置
		int moveToDirection = mDelAreaModel.getMoveToDirection(); // 插入移除后区域的哪个方向
		int areaPosition = mDelAreaModel.getAreaPosition(); // 当前ICON所谓区域
		for (int i = 0; i < mDockViewListSize - 1; i++) { // 目前区域-1
			// 在区域左边
			if (moveToDirection == DockUtil.MOVE_TO_LEFT) {
				mIsInMiddle = false;
				mDockInAreaIndex = areaPosition; // 记录当前所在区域
				// 该区域左边的区域
				if (i < areaPosition) {
					if (AbsDockView.sPortrait) {
						new_icon_point = mNormalOneAreaWidth * i + mNormalPaddingLeft;
					} else {
						new_icon_point = mNormalOneAreaHeight * (mDockViewListSize - i)
								- mNormalPaddingtop - mIconHeight;
					}
				} else {
					if (AbsDockView.sPortrait) {
						new_icon_point = mNormalOneAreaWidth * (i + 1) + mNormalPaddingLeft;
					} else {
						new_icon_point = mNormalOneAreaHeight * (mDockViewListSize - i - 1)
								- mNormalPaddingtop - mIconHeight;
					}
				}
			}

			// 在区域的右边
			else if (moveToDirection == DockUtil.MOVE_TO_RIGHT) {
				mIsInMiddle = false;
				mDockInAreaIndex = mDelAreaModel.getAreaPosition() + 1; // 记录当前所在区域

				// 该区域左边的区域+该区域
				if (i <= areaPosition) {
					if (AbsDockView.sPortrait) {
						new_icon_point = mNormalOneAreaWidth * i + mNormalPaddingLeft;
					} else {
						new_icon_point = mNormalOneAreaHeight * (mDockViewListSize - i)
								- mNormalPaddingtop - mIconHeight;
					}
				} else {
					if (AbsDockView.sPortrait) {
						new_icon_point = mNormalOneAreaWidth * (i + 1) + mNormalPaddingLeft;
					} else {
						new_icon_point = mNormalOneAreaHeight * (mDockViewListSize - i - 1)
								- mNormalPaddingtop - mIconHeight;
					}
				}
			}

			else if (moveToDirection == DockUtil.MOVE_TO_CENTENT) {
				mIsInMiddle = true;
				mIsDockIconScrentToDock = true; // 设置标志表示还没插入到Dock中
				return;
			}

			// 获取在I区域上的ICON进行移动
			for (DockIconView dockIcon : mDockViewList) {
				if (dockIcon.getAreaPosition() == i) { // 判断ICON所在的区域
					startDockIconViewMoveAnimation(dockIcon, new_icon_point,
							DockUtil.ANIMATION_ZOOM_BIG_TO_SMALL);
					break;
				}
			}
		}

		// 遍历所有图标重新设置DockIconView所在区域
		for (DockIconView dockIconView : mDockViewList) {
			int p = dockIconView.getAreaPosition();
			// 在区域左边
			if (moveToDirection == DockUtil.MOVE_TO_LEFT) {
				if (p == -1) { // -1代表拖走的VIEW的位置
					dockIconView.setAreaPosition(areaPosition); // 设置当前拖动图标在的位置
					continue;
				}
				if (p < mDelAreaModel.getAreaPosition()) { // 该区域左边的区域

				} else { // 该区域右边的区域
					dockIconView.setAreaPosition(p + 1);
				}
			}

			// 在区域的右边
			else if (moveToDirection == DockUtil.MOVE_TO_RIGHT) {
				if (p == -1) {
					dockIconView.setAreaPosition(areaPosition + 1); // 设置当前拖动图标在的位置
					continue;
				}
				if (p <= mDelAreaModel.getAreaPosition()) { // 该区域左边的区域+该区域

				} else {
					dockIconView.setAreaPosition(p + 1);
				}
			}
		}
	}

	/**
	 * dock 图标dock to dock 的交换动作
	 * 
	 * @param point
	 */
	public void setDockIconViewAnimationDockToDock(Point point) {
		if (mDockInListIndex < 0 || mDockViewList == null
				|| mDockViewList.size() <= mDockInListIndex) {
			return;
		}
		setMoveToPosition(mNormalAreaModel, point);
		mIsInMiddle = false;
		int new_icon_point = 0;
		int moveToDirection = mNormalAreaModel.getMoveToDirection(); // 插入移除后区域的哪个方向
		int areaPosition = mNormalAreaModel.getAreaPosition(); // 当前ICON所谓区域

		for (int i = 0; i < mDockViewListSize; i++) {
			// 在区域左边
			if (moveToDirection == DockUtil.MOVE_TO_LEFT) {
				// 如果当前区域在拿起区域左边 && 当前区域不在拿起区域范围
				if (i == mDockInAreaIndex - 1 && areaPosition < mDockInAreaIndex) {
					if (AbsDockView.sPortrait) {
						new_icon_point = mNormalOneAreaWidth * (i + 1) + mNormalPaddingLeft;
					} else {
						new_icon_point = mNormalOneAreaHeight * (mDockViewListSize - i - 1)
								- mNormalPaddingtop - mIconHeight;
					}

					for (DockIconView dockIconView : mDockViewList) {
						if (dockIconView.getAreaPosition() == mDockInAreaIndex - 1) {
							startDockIconViewMoveAnimation(dockIconView, new_icon_point,
									DockUtil.ANIMATION_ZOOM_NORMAL); // 动画
							dockIconView.setAreaPosition(mDockInAreaIndex); // 设置
							mDockViewList.get(mDockInListIndex).setAreaPosition(
									mDockInAreaIndex - 1); // 设置拿起ICON的位置
							mDockInAreaIndex = mDockInAreaIndex - 1;
							setMoveToPosition(mNormalAreaModel, point);
							return;
						}
					}
				}
			}

			// 在区域的右边
			else if (moveToDirection == DockUtil.MOVE_TO_RIGHT) {
				if ((i - 1) == mDockInAreaIndex && areaPosition > mDockInAreaIndex) {
					if (AbsDockView.sPortrait) {
						new_icon_point = mNormalOneAreaWidth * (i - 1) + mNormalPaddingLeft;
					} else {
						new_icon_point = mNormalOneAreaHeight * (mDockViewListSize - i + 1)
								- mNormalPaddingtop - mIconHeight;
					}
					for (DockIconView dockIconView : mDockViewList) {
						if (dockIconView.getAreaPosition() == mDockInAreaIndex + 1) {
							startDockIconViewMoveAnimation(dockIconView, new_icon_point,
									DockUtil.ANIMATION_ZOOM_NORMAL);
							dockIconView.setAreaPosition(mDockInAreaIndex);
							mDockViewList.get(mDockInListIndex).setAreaPosition(
									mDockInAreaIndex + 1);
							mDockInAreaIndex = mDockInAreaIndex + 1;
							setMoveToPosition(mNormalAreaModel, point);
							return;
						}
					}
				}
			}
			// 中间
			else if (moveToDirection == DockUtil.MOVE_TO_CENTENT) {
				return;
			}
		}
	}

	/**
	 * 桌面图标从screen to dock 交换动画
	 * 
	 * @param point
	 *            坐标
	 */
	public void setDeskIconAnimationScreenToDock(Point point) {
		setMoveToPosition(mNormalAreaModel, point);
		int new_icon_point = 0;
		int moveToDirection = mNormalAreaModel.getMoveToDirection(); // 插入移除后区域的哪个方向
		if (moveToDirection == DockUtil.MOVE_TO_OUT) {
			mIsDeskIconScrentToDock = true; // 下次重新从屏幕层进来
			mDockInAreaIndex = -1; // 记录当前所在区域
			return;
		}
		int areaPosition = mNormalAreaModel.getAreaPosition(); // 当前ICON所谓区域

		// 判断是否0个
		if (mDockViewListSize == 0) {
			mDockInAreaIndex = 0;
			return;
		}

		for (int i = 0; i < mDockViewListSize; i++) {
			DockIconView dockIconView = mDockViewList.get(i);
			if (moveToDirection == DockUtil.MOVE_TO_LEFT) {
				mIsInMiddle = false;
				mDockInAreaIndex = areaPosition; // 记录当前所在区域

				// 该区域左边的区域
				if (i < areaPosition) {
					if (AbsDockView.sPortrait) {
						new_icon_point = mAddOneAreaWidth * i + mAddPaddingLeft;
					} else {
						new_icon_point = mAddOneAreaHeigh * (mDockViewListSize - i + 1)
								- mAddPaddingtop - mIconHeight;
					}
					dockIconView.setAreaPosition(i);
				} else { // 该区域右边的区域
					if (AbsDockView.sPortrait) {
						new_icon_point = mAddOneAreaWidth * (i + 1) + mAddPaddingLeft;
					} else {
						new_icon_point = mAddOneAreaHeigh * (mDockViewListSize - i)
								- mAddPaddingtop - mIconHeight;
					}
					dockIconView.setAreaPosition(i + 1);
				}
				startDockIconViewMoveAnimation(dockIconView, new_icon_point,
						DockUtil.ANIMATION_ZOOM_NORMAL_TO_SMALL);
			}

			else if (moveToDirection == DockUtil.MOVE_TO_RIGHT) {
				mIsInMiddle = false;
				mDockInAreaIndex = areaPosition + 1; // 记录当前所在区域
				// 该区域左边的区域
				if (i <= areaPosition) {
					if (AbsDockView.sPortrait) {
						new_icon_point = mAddOneAreaWidth * i + mAddPaddingLeft;
					} else {
						new_icon_point = mAddOneAreaHeigh * (mDockViewListSize - i + 1)
								- mAddPaddingtop - mIconHeight;
					}
					dockIconView.setAreaPosition(i);
				} else { // 该区域右边的区域
					if (AbsDockView.sPortrait) {
						new_icon_point = mAddOneAreaWidth * (i + 1) + mAddPaddingLeft;
					} else {
						new_icon_point = mAddOneAreaHeigh * (mDockViewListSize - i)
								- mAddPaddingtop - mIconHeight;
					}
					dockIconView.setAreaPosition(i + 1);
				}
				startDockIconViewMoveAnimation(dockIconView, new_icon_point,
						DockUtil.ANIMATION_ZOOM_NORMAL_TO_SMALL);
			}

			else if (moveToDirection == DockUtil.MOVE_TO_CENTENT) {
				mIsInMiddle = true;
				mIsDeskIconScrentToDock = true; // 下次重新从屏幕层进来
				mDockInAreaIndex = areaPosition; // 记录当前所在区域
				return;
			}
		}
	}

	/**
	 * 桌面图标从dock to screen 动画
	 * 
	 * @param point
	 *            desk坐标
	 */
	public void setDeskIconAnimationDockToScreen() {
		int new_icon_point = 0;
		for (int i = 0; i < mDockViewListSize; i++) {
			DockIconView dockIconView = mDockViewList.get(i);
			dockIconView.setAreaPosition(i);
			if (AbsDockView.sPortrait) {
				new_icon_point = mNormalOneAreaWidth * i + mNormalPaddingLeft;
			} else {
				new_icon_point = mNormalOneAreaHeight * (mDockViewListSize - i) - mNormalPaddingtop
						- mIconHeight;
			}
			if (!mIsInMiddle) {
				startDockIconViewMoveAnimation(dockIconView, new_icon_point,
						DockUtil.ANIMATION_ZOOM_NORMAL);
			}

		}
		mDockInAreaIndex = -1;
	}

	/**
	 * 桌面图标从dock to dock 交换动画
	 * 
	 * @param point
	 *            坐标
	 */
	public void setDeskIconAnimationDockToDock(Point point) {
		setMoveToPosition(mAddAreaModel, point);
		mIsInMiddle = false;
		int new_icon_point = 0;
		int moveToDirection = mAddAreaModel.getMoveToDirection(); // 插入移除后区域的哪个方向
		int areaPosition = mAddAreaModel.getAreaPosition(); // 当前ICON所谓区域

		for (int i = 0; i < mDockViewListSize + 1; i++) { // 数量+1
			// 在区域左边
			if (moveToDirection == DockUtil.MOVE_TO_LEFT) {
				// 如果当前区域在拿起区域左边 && 当前区域不在拿起区域范围
				if (i == mDockInAreaIndex - 1 && areaPosition < mDockInAreaIndex) {
					if (AbsDockView.sPortrait) {
						new_icon_point = mAddOneAreaWidth * (i + 1) + mAddPaddingLeft;
					} else {
						new_icon_point = mAddOneAreaHeigh * (mDockViewListSize - i)
								- mAddPaddingtop - mIconHeight;
					}

					for (DockIconView dockIconView : mDockViewList) {
						if (dockIconView.getAreaPosition() == mDockInAreaIndex - 1) {
							startDockIconViewMoveAnimation(dockIconView, new_icon_point,
									DockUtil.ANIMATION_ZOOM_SMALL_TO_SMALL); // 动画
							dockIconView.setAreaPosition(mDockInAreaIndex); // 设置
							mDockInAreaIndex = mDockInAreaIndex - 1;
							return;
						}
					}
				}
			} else if (moveToDirection == DockUtil.MOVE_TO_RIGHT) {
				// 如果当前区域在拿起区域左边 && 当前区域不在拿起区域范围
				if (i == mDockInAreaIndex + 1 && areaPosition > mDockInAreaIndex) {
					if (AbsDockView.sPortrait) {
						new_icon_point = mAddOneAreaWidth * (i - 1) + mAddPaddingLeft;
					} else {
						new_icon_point = mAddOneAreaHeigh * (mDockViewListSize + 1 - i + 1)
								- mAddPaddingtop - mIconHeight;
					}

					for (DockIconView dockIconView : mDockViewList) {
						if (dockIconView.getAreaPosition() == mDockInAreaIndex + 1) {
							startDockIconViewMoveAnimation(dockIconView, new_icon_point,
									DockUtil.ANIMATION_ZOOM_SMALL_TO_SMALL); // 动画
							dockIconView.setAreaPosition(mDockInAreaIndex); // 设置
							mDockInAreaIndex = mDockInAreaIndex + 1;
							return;
						}
					}
				}
			}

			else if (moveToDirection == DockUtil.MOVE_TO_CENTENT) {
				return;
			}
		}
	}

	/**
	 * 删除当前图标所有的动画 和复位标志位
	 */
	public void clearAnimationAndresetFlag() {
		// 清除动画
		for (DockIconView dockIconView : mDockViewList) {
			dockIconView.clearAnimation();
		}
		mIsDockIconDockToScrent = true;
		mIsDockIconScrentToDock = false;
		mIsDeskIconScrentToDock = true;
		mIsDeskIconDockToScreen = false;
		mNormalAreaModel = null;
		mDelAreaModel = null;
		mAddAreaModel = null;
	}

	/**
	 * 初始化图标从screen to dock的区域
	 */
	public void initNormalAndAddArea() {
		if (mNormalAreaModel == null || mAddAreaModel == null) {
			initNormalAreaModel();
			initAddAreaModel();
		}
	}

	/**
	 * 初始化图标从dock to screen的区域
	 */
	public void initNormalAndDelArea(View dockIconView) {
		if (mNormalAreaModel == null || mDelAreaModel == null) {
			initNormalAreaModel();
			initDelAreaModel();

			mDockInAreaIndex = mDockViewList.indexOf(dockIconView); // 拖动图标所在位置
			mDockInListIndex = mDockInAreaIndex;
		}
	}

	public void setMoveToPosition(Point point) {
		setMoveToPosition(mNormalAreaModel, point);
	}

	/**
	 * Dock图标移动
	 * 
	 * @param point
	 *            当前的坐标
	 * @param dragView
	 *            抓起的DockIconView
	 */
	public boolean dockIconViewMove(Point point, View dragView) {
		if (point == null) {
			return false;
		}
		this.mDragView = dragView;
		initNormalAndDelArea(dragView);
		// 检查坐标是否在DOCK区域
		if (!checkInDock(point)) {
			if (mIsDockIconDockToScrent) {
				mIsDockIconDockToScrent = false;
				mIsDockIconScrentToDock = true;
				setDockIconViewAnimationDockToScreen();
				mDragPositionListner.setRecycleDragCache();
			}
			return false;
		} else {
			if (mIsDockIconScrentToDock) {
				mIsDockIconDockToScrent = true;
				mIsDockIconScrentToDock = false;
				setDockIconViewAnimationScreenToDock(point);
			} else {
				mIsDockIconDockToScrent = true;
				mIsDockIconScrentToDock = false;
				setDockIconViewAnimationDockToDock(point);
			}
			return true;
		}
	}

	/**
	 * 桌面图标移动
	 * 
	 * @param point
	 *            当前的坐标
	 * @param dragView
	 *            抓起的DockIconView
	 * @return
	 */
	public boolean deskIconViewMove(Point point) {
		if (point == null) {
			return false;
		}
		if (checkInDock(point)) {
			if (mIsDeskIconScrentToDock) {
				initNormalAndAddArea();
				if (mDockViewList.size() >= DockUtil.ICON_COUNT_IN_A_ROW) {
					setMoveToPosition(point);
					return true;
				}
				mIsDeskIconScrentToDock = false;
				mIsDeskIconDockToScreen = true;
				setDeskIconAnimationScreenToDock(point);
			} else {
				mIsDeskIconScrentToDock = false;
				mIsDeskIconDockToScreen = true;
				setDeskIconAnimationDockToDock(point);
			}
			return true;
		} else {
			mDragPositionListner.setRecycleDragCache();
			if (mIsDeskIconDockToScreen) {
				mIsDeskIconDockToScreen = false;
				mIsDeskIconScrentToDock = true;
				setDeskIconAnimationDockToScreen();
			}
			return false;
		}
	}

	public boolean getIsDockIconScrentToDock() {
		return mIsDockIconScrentToDock;
	}

	public boolean getIsDeskIconScrentToDock() {
		return mIsDeskIconScrentToDock;
	}
}
