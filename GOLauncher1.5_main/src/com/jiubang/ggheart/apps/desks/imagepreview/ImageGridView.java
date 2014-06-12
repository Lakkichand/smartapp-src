package com.jiubang.ggheart.apps.desks.imagepreview;

import android.content.Context;
import android.widget.GridView;
import android.widget.RelativeLayout;

public class ImageGridView extends GridView {
	private ImageGridParam mParam;

	public ImageGridView(Context context, ImageGridParam param) {
		super(context);

		mParam = param;
		RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.FILL_PARENT);
		setLayoutParams(rlp);

		// setBackgroundColor(0xB31f1f1f);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		super.onLayout(changed, l, t, r, b);

		// if (!changed)
		{
			initGridAttr();
		}
	}

	private void initGridAttr() {
		if (null == mParam) {
			return;
		}
		int w = getWidth();
		// int h = getHeight();
		int columns = w / mParam.mWidth;
		// int griditemhspace = (w - (columns * mParam.mWidth)) / (columns - 1);
		// int rows = h / mParam.mHeight;
		// int griditemvspace = (h - rows * mParam.mHeight) / (rows - 1);

		setNumColumns(columns);
		// setHorizontalSpacing(griditemhspace);
		// setVerticalSpacing(20);
	}
}
