/**
 * 
 */
package com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.unfit;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;

import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.AbsDockView;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.AbsLineLayout;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.DockIconView;
import com.jiubang.ggheart.apps.desks.dock.DockUtil;

/**
 * 非自适应模式，dock行排版
 * 
 * @author ruxueqin
 * 
 */
public class UnfitLineLayout extends AbsLineLayout {

	private ArrayList<Integer> mAddIndex;

	private IAddIconResHandler mAddIconResHandler;

	private int mAddIconSize;

	private int mLayoutWidth;

	private int mLayoutHeight;

	private int mClickAddIndex = -1; // 点击+号的索引

	private int mLightAddIndex = -1; // 高亮+号索引

	private int mLongClickBlank = -1; // 长按空白索引

	public UnfitLineLayout(Context context) {
		super(context);

		mAddIndex = new ArrayList<Integer>();
		// 大小
		mAddIconSize = DockUtil.getIconSize(DockUtil.ICON_COUNT_IN_A_ROW);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.ViewGroup#onLayout(boolean, int, int, int, int)
	 */
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		mLayoutWidth = r - l;
		mLayoutHeight = b - t;

		if (AbsDockView.sPortrait) {
			layoutPort(changed, l, t, r, b);
		} else {
			layoutLand(changed, l, t, r, b);
		}

		updateAdd();
	}

	private void layoutPort(boolean changed, int l, int t, int r, int b) {
		int count = getChildCount();
		if (count == 0) {
			return;
		}
		int oneView_W = (r - l) / DockUtil.ICON_COUNT_IN_A_ROW;
		int bitmap_size = DockUtil.getIconSize(DockUtil.ICON_COUNT_IN_A_ROW);
		for (int i = 0; i < count; i++) {
			DockIconView view = (DockIconView) getChildAt(i);
			if (null != view.getInfo()) {
				int index = view.getInfo().getmIndexInRow();
				int left = (oneView_W - bitmap_size) / 2 + oneView_W * index;
				int top = 0;
				int right = left + bitmap_size;
				view.layout(left, top, right, b);
			}
		}
	}

	private void layoutLand(boolean changed, int l, int t, int r, int b) {
		int count = getChildCount();
		if (count == 0) {
			return;
		}
		int oneView_H = (b - t) / DockUtil.ICON_COUNT_IN_A_ROW;
		int bitmap_size = DockUtil.getIconSize(DockUtil.ICON_COUNT_IN_A_ROW);
		for (int i = 0; i < count; i++) {
			DockIconView view = (DockIconView) getChildAt(i);
			if (null != view.getInfo()) {
				int index = view.getInfo().getmIndexInRow();
				int left = 0;
				int top = oneView_H * (DockUtil.ICON_COUNT_IN_A_ROW - index - 1)
						+ (oneView_H - bitmap_size) / 2; // 控制点击范围
				int right = r;
				int bottom = top + bitmap_size;
				view.layout(left, top, right, bottom);
			}
		}
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		// 画+号
		if (null != mAddIconResHandler && null != mAddIconResHandler.getAddDrawable()
				&& null != mAddIndex) {
			int size = mAddIndex.size();
			Drawable drawable = mAddIconResHandler.getAddDrawable();
			for (int i = 0; i < size; i++) {
				int index = mAddIndex.get(i);

				if (AbsDockView.sPortrait) {
					int oneView_W = mLayoutWidth / DockUtil.ICON_COUNT_IN_A_ROW;
					int l = (oneView_W - mAddIconSize) / 2 + oneView_W * index;
					int t = (mLayoutHeight - mAddIconSize) / 2;
					int r = l + mAddIconSize;
					int b = t + mAddIconSize;
					drawable.setBounds(l, t, r, b);
				} else {
					int oneView_H = mLayoutHeight / DockUtil.ICON_COUNT_IN_A_ROW;
					int l = (mLayoutWidth - mAddIconSize) / 2;
					int t = (oneView_H - mAddIconSize) / 2 + oneView_H
							* (DockUtil.ICON_COUNT_IN_A_ROW - index - 1);
					int r = l + mAddIconSize;
					int b = t + mAddIconSize;
					drawable.setBounds(l, t, r, b);
				}
				drawable.draw(canvas);
				if (index == mLightAddIndex && null != mAddIconResHandler.getLightDrawable()) {
					// 画+号点击发光
					Drawable drawableLight = mAddIconResHandler.getLightDrawable();
					drawableLight.setBounds(drawable.getBounds());
					drawableLight.draw(canvas);
				}
			}
		}
		super.dispatchDraw(canvas);
	}

	private void updateAdd() {
		int count = getChildCount();
		boolean hasAnimation = false;
		for (int i = 0; i < count; i++) {
			DockIconView view = (DockIconView) getChildAt(i);
			if (view.getAnimation() != null) {
				hasAnimation = true;
				break;
			}
		}
		if (hasAnimation) {
			// 有动画,即在长按中,不自动更新+号位置,由外部控制+号位置
			return;
		}
		// 位置
		if (null == mAddIndex) {
			mAddIndex = new ArrayList<Integer>();
		}
		mAddIndex.clear();

		int[] array = { 0, 0, 0, 0, 0 };
		// 过滤１：放图标
		for (int i = 0; i < count; i++) {
			DockIconView view = (DockIconView) getChildAt(i);
			int index = view.getInfo().getmIndexInRow();
			if (view.getVisibility() == View.VISIBLE && null != view.getInfo() && 0 <= index
					&& index <= 4) {
				array[index] = -1;
			}
		}
		// 过滤２：放空白
		ArrayList<Integer> blankList = (null != mAddIconResHandler) ? mAddIconResHandler
				.getBlanks(mLineID) : null;
		if (null != blankList) {
			int size = blankList.size();
			for (int i = 0; i < size; i++) {
				int index = blankList.get(i);
				if (0 <= index && index <= 4) {
					array[index] = -1;
				}
			}
		}

		// 结果
		for (int i = 0; i < 5; i++) {
			if (array[i] == 0) {
				mAddIndex.add(i);
			}
		}
		postInvalidate();
	}

	public int getClickAddIndex() {
		return mClickAddIndex;
	}

	public int getLongClickBlank() {
		return mLongClickBlank;
	}

	public void setLightAddIndex(int index) {
		if (mLightAddIndex != index) {
			mLightAddIndex = index;
			postInvalidate();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		switch (action) {
			case MotionEvent.ACTION_DOWN :
				int index = -1;
				mClickAddIndex = -1;
				if (AbsDockView.sPortrait) {
					float x = event.getX();
					int iconWidth = mLayoutWidth / DockUtil.ICON_COUNT_IN_A_ROW;
					index = (int) (x / iconWidth);
				} else {
					float y = mLayoutHeight - event.getY();
					int iconHeight = mLayoutHeight / DockUtil.ICON_COUNT_IN_A_ROW;
					index = (int) (y / iconHeight);
				}
				int size = mAddIndex.size();
				for (int i = 0; i < size; i++) {
					if (mAddIndex.get(i) == index) {
						mClickAddIndex = index;
						setLightAddIndex(index);
					}
				}
				mLongClickBlank = index;

				break;

			case MotionEvent.ACTION_UP :

				break;

			default :
				break;
		}

		return super.onTouchEvent(event);
	}

	/**
	 * 外部调用，指定哪些索引图标需要画+号，0~4
	 * 
	 * @param list
	 *            +号显示索引集。 例：0,1,3显示+号，则传入包含0,1,3的list
	 */
	@SuppressWarnings({ "unchecked" })
	public void setAddIndex(ArrayList<Integer> list) {
		mAddIndex = (ArrayList<Integer>) list.clone();

		// 过滤：放空白
		ArrayList<Integer> blankList = (null != mAddIconResHandler) ? mAddIconResHandler
				.getBlanks(mLineID) : null;
		if (null != blankList) {
			int size = blankList.size();
			for (int i = 0; i < size; i++) {
				Integer index = blankList.get(i);
				if (0 <= index && index <= 4) {
					mAddIndex.remove(index);
				}
			}
		}

		postInvalidate();
	}

	public void setAddIconResHandler(IAddIconResHandler handler) {
		mAddIconResHandler = handler;
	}

	@Override
	public void updateLayout() {
		updateAdd();
	}
}
