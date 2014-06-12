package com.go.util.animation;

import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

import com.jiubang.ggheart.apps.desks.diy.frames.screen.CellLayout;

/**
 * 动画相关的工具类
 * 
 * @author dengweiming
 * 
 */
public class MyAnimationUtils {
	/** 缺省动画时间 */
	public final static int DEFAULT_DURATION = 250;

	public final static int SHOW_HIDE_DURATION = 500;

	/** 弹出到(竖屏)底部/(横屏)右侧 进入 */
	public final static int POP_FROM_LONG_END = 0;
	public final static int POP_FROM_LONG_END_SHOW = 1;
	/** 从(竖屏)底部/(横屏)右侧弹回 　　退出 */
	public final static int POP_TO_LONG_END = 2;
	public final static int POP_TO_LONG_END_HIDE = 3;
	/** 弹出到(竖屏)顶部/(横屏)左侧 进入 */
	public final static int POP_FROM_LONG_START = 4;
	public final static int POP_FROM_LONG_START_SHOW = 5;
	/** 从(竖屏)顶部/(横屏)左侧弹回 　　退出 */
	public final static int POP_TO_LONG_START = 6;
	public final static int POP_TO_LONG_START_HIDE = 7;
	/** 弹出到(竖屏)顶部/(横屏)顶部 进入 */
	public final static int POP_FROM_LONG_START_2 = 8;
	public final static int POP_FROM_LONG_START_SHOW_2 = 9;
	/** 从(竖屏)顶部/(横屏)顶部弹回 　　退出 */
	public final static int POP_TO_LONG_START_2 = 10;
	public final static int POP_TO_LONG_START_HIDE_2 = 11;

	/**
	 * 获取弹出/弹回的动画。不写成xml的原因就是因为要访问
	 * {@link com.jiubang.ggheart.apps.desks.diy.frames.screen.CellLayout}
	 * 的一些全局变量（假设已经初始化）
	 * 
	 * @param animId
	 *            指定从预定义的几个动画选取，取值为{@link #POP_FROM_BOTTOM},
	 *            {@link #POP_TO_BOTTOM}, {@link #POP_FROM_RIGHT},
	 *            {@link #POP_TO_RIGHT}
	 * @param duration
	 *            动画持续事件，如果为负数则使用默认时间{@link #DEFAULT_DURATION}
	 * @return
	 */
	public static Animation getPopupAnimation(int animId, int duration) {
		// // res/anim/popup_bottom.xml 内容备份：
		// <set xmlns:android="http://schemas.android.com/apk/res/android"
		// android:interpolator="@android:anim/decelerate_interpolator">
		// <translate
		// android:fromYDelta="100%"
		// android:toYDelta="0"
		// android:duration="250"/>
		//
		// <alpha
		// android:fromAlpha="0.0"
		// android:toAlpha="1.0"
		// android:duration="250" />
		// </set>
		Animation animation = null;
		// *2是为了退出动画不与垃圾桶重叠
		final int dist = CellLayout.sLongAxisEndPadding * 2;
		switch (animId) {
			case POP_FROM_LONG_END :
				if (CellLayout.sPortrait) {
					animation = new TranslateAnimation(0, 0, dist, 0);
				} else {
					animation = new TranslateAnimation(dist, 0, 0, 0);
				}
				animation.setDuration(duration < 0 ? DEFAULT_DURATION : duration);
				break;
			case POP_TO_LONG_END :
				if (CellLayout.sPortrait) {
					animation = new TranslateAnimation(0, 0, 0, dist);
				} else {
					animation = new TranslateAnimation(0, dist, 0, 0);
				}
				animation.setDuration(duration < 0 ? DEFAULT_DURATION : duration);
				break;

			case POP_FROM_LONG_END_SHOW :
				if (CellLayout.sPortrait) {
					animation = new TranslateAnimation(0, 0, dist / 2, 0);
				} else {
					animation = new TranslateAnimation(dist / 2, 0, 0, 0);
				}
				animation.setDuration(duration < 0 ? SHOW_HIDE_DURATION : duration);
				break;

			case POP_TO_LONG_END_HIDE :
				if (CellLayout.sPortrait) {
					animation = new TranslateAnimation(0, 0, 0, dist / 2);
				} else {
					animation = new TranslateAnimation(0, dist / 2, 0, 0);
				}
				animation.setDuration(duration < 0 ? SHOW_HIDE_DURATION : duration);
				break;

			case POP_FROM_LONG_START :

				break;

			case POP_FROM_LONG_START_SHOW :
				// 由于横屏情况下，垃圾箱也在上方出现，所以不区分横竖屏
				// if(CellLayout.sPortrait){
				animation = new TranslateAnimation(0, 0, -dist / 2, 0);
				// }
				// else{
				// animation = new TranslateAnimation(-dist/2, 0, 0, 0);
				// }
				animation.setDuration(duration < 0 ? SHOW_HIDE_DURATION / 2 : duration / 2);
				break;

			case POP_TO_LONG_START :

				break;

			case POP_TO_LONG_START_HIDE :
				if (CellLayout.sPortrait) {
					animation = new TranslateAnimation(0, 0, 0, -dist / 2);
				} else {
					animation = new TranslateAnimation(0, -dist / 2, 0, 0);
				}
				animation.setDuration(duration < 0 ? SHOW_HIDE_DURATION / 2 : duration / 2);
				break;

			case POP_FROM_LONG_START_2 :

				break;

			case POP_FROM_LONG_START_SHOW_2 :
				if (CellLayout.sPortrait) {
					animation = new TranslateAnimation(0, 0, -dist / 2, 0);
				} else {
					animation = new TranslateAnimation(0, 0, -dist / 2, 0);
				}
				animation.setDuration(duration < 0 ? SHOW_HIDE_DURATION / 2 : duration / 2);
				break;
			case POP_TO_LONG_START_2 :

				break;

			case POP_TO_LONG_START_HIDE_2 :
				if (CellLayout.sPortrait) {
					animation = new TranslateAnimation(0, 0, 0, -dist / 2);
				} else {
					animation = new TranslateAnimation(0, 0, 0, -dist / 2);
				}
				animation.setDuration(duration < 0 ? SHOW_HIDE_DURATION / 2 : duration / 2);
				break;
		}
		// animation.setDuration(duration < 0 ? DEFAULT_DURATION : duration);
		if (animation != null) {
			animation.setInterpolator(new DecelerateInterpolator(1.5f));
		}
		return animation;
	}

	/**
	 * 计算缩放中心位置的X坐标
	 * 
	 * @param left
	 *            原始左边界
	 * @param width
	 *            原始宽度
	 * @param left2
	 *            目标左边界
	 * @param width2
	 *            目标宽度， 如果和原始宽度相等则没有意义（因为不能缩放）
	 * @return
	 */
	public static float solveScaleCenterX(float left, float width, float left2, float width2) {
		/*
		 * 假设缩放中心为(cx, cy)，则在X轴上有 (cx - left) / (cx - left2) = (cx - right) /
		 * (cx - right2) = (cx - width + left) / (cx - width2 + left2) 解得 cx =
		 * (left * width2 - left2 * width) / (width2 - width)
		 */
		return width2 == width ? 0 : (left * width2 - left2 * width) / (width2 - width);
	}

	/**
	 * 计算缩放中心位置的Y坐标
	 * 
	 * @param top
	 *            原始上边界
	 * @param height
	 *            原始高度
	 * @param top2
	 *            目标上边界
	 * @param height2
	 *            目标高度， 如果和原始高度相等则没有意义（因为不能缩放）
	 * @return
	 */
	public static float solveScaleCenterY(float top, float height, float top2, float height2) {
		return height2 == height ? 0 : (top * height2 - top2 * height) / (height2 - height);
	}

	/**
	 * 获取两个区域（区域1和区域2）之间等比缩放或平移的动画（作用于区域1）。 注意两个区域要放在同一个坐标系衡量。
	 * 一般来说，区域1为整个View(即动画的载体)的大小，那么(x1, y1) = (0, 0)。
	 * 
	 * @param x1
	 *            区域1的左边界
	 * @param y1
	 *            区域1的上边界
	 * @param x2
	 *            区域2的左边界
	 * @param y2
	 *            区域2的上边界
	 * @param scale
	 *            区域2相对区域1的缩放比例
	 * @param reverse
	 *            为false表示从区域1变化到区域2，否则从区域2变化到区域1
	 * @param duration
	 *            动画持续时间
	 * @return
	 */
	public static Animation getRectAnimation(float x1, float y1, float x2, float y2, float scale,
			boolean reverse, int duration) {
		Animation animation = null;
		if (scale == 1) {
			if (reverse) {
				animation = new TranslateAnimation(x2 - x1, 0, y2 - y1, 0);
			} else {
				animation = new TranslateAnimation(0, x1 - x2, 0, y1 - y2);
			}
		} else {
			final float centerX = solveScaleCenterX(x1, 1, x2, scale);
			final float centerY = solveScaleCenterY(y1, 1, y2, scale);
			if (reverse) {
				animation = new ScaleAnimation(scale, 1, scale, 1, centerX, centerY);
			} else {
				animation = new ScaleAnimation(1, scale, 1, scale, centerX, centerY);
			}
		}
		animation.setDuration(duration < 0 ? DEFAULT_DURATION : duration);
		animation.setInterpolator(new OvershootInterpolator());
		return animation;
	}

}
