package com.jiubang.ggheart.apps.desks.ggmenu;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.GridView;

import com.gau.go.launcherex.R;

/**
 * 自定义的菜单GridView
 * 
 * @author ouyongqiang
 * 
 */
public class GGMenuGridView extends GridView {
	private int mColumns = GGMenuData.GGMENU_MAX_COLOUMNS;

	private int mVerticalSpacing;

	// 分割线高度
	private int mLineHeight;

	private Drawable mLineDrawable;

	private Rect[] mItemLineRects;

	/**
	 * 构造函数
	 * 
	 * @param context
	 *            程序上下文
	 * @param attr
	 *            属性集
	 */
	public GGMenuGridView(Context context, AttributeSet attr) {
		super(context, attr);
		setVerticalFadingEdgeEnabled(false);

		Resources resources = getResources();
		mLineHeight = (int) resources.getDimension(R.dimen.menu_divline_height);
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		GGMenuApdater apdater = (GGMenuApdater) getAdapter();

		int count = apdater.getCount();
		int perRowHeight = 0;
		try {
			perRowHeight = ((GGMenuItem) apdater.getItem(0)).getMeasuredHeight();
		} catch (Exception e) {
		}

		int rows = (count + mColumns - 1) / mColumns;
		int height = rows * perRowHeight + getPaddingTop() + getPaddingBottom() + mVerticalSpacing
				* (rows - 1);
		setMeasuredDimension(getMeasuredWidth(), height);
	}

	@Override
	public void setNumColumns(int numColumns) {
		mColumns = numColumns;
		super.setNumColumns(numColumns);
	}

	@Override
	public void setVerticalSpacing(int verticalSpacing) {
		super.setVerticalSpacing(verticalSpacing);
		mVerticalSpacing = verticalSpacing;
	}

	/**
	 * 设置分割线Drawable
	 * 
	 * @param divLine
	 */
	public void setDivLineDrawable(Drawable divLine) {
		mLineDrawable = divLine;
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);

		GGMenuApdater adapter = (GGMenuApdater) getAdapter();
		int count = adapter.getCount();
		if (count > 1) {
			if (null == mItemLineRects) {
				int count_tmp = count - 1;
				int row = (count % mColumns != 0) ? (count / mColumns) : (count / mColumns - 1);
				int rectcount = count_tmp - row;
				mItemLineRects = new Rect[rectcount];
				int lineWidth = (null != mLineDrawable) ? mLineDrawable.getIntrinsicWidth() : 0;
				for (int i = 0; i < rectcount; i++) {
					int lineColumns = mColumns - 1;
					int childviewrow = (i % lineColumns != 0 || i == 0)
							? (i / lineColumns)
							: (i / lineColumns);
					int childviewindex = i + childviewrow;
					GGMenuItem item_tmp = (GGMenuItem) adapter.getItem(childviewindex);
					int view_r = (null != item_tmp) ? item_tmp.getRight() : 0;
					int view_t = (null != item_tmp) ? item_tmp.getTop() : 0;
					int view_b = (null != item_tmp) ? item_tmp.getBottom() : 0;
					int l = view_r - lineWidth / 2;
					int t = (view_t + view_b) / 2 - mLineHeight / 2;
					int r = l + lineWidth;
					int b = t + mLineHeight;
					Rect rect = new Rect(l, t, r, b);
					mItemLineRects[i] = rect;
				}
			}
			int linecount = mItemLineRects.length;
			if (mLineDrawable != null) {
				for (int i = 0; i < linecount; i++) {
					mLineDrawable.setBounds(mItemLineRects[i]);
					mLineDrawable.draw(canvas);
				}
			}
		}
	}
}
