package com.jiubang.go.backup.pro.ui;

import java.util.ArrayList;

import android.content.Context;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jiubang.go.backup.ex.R;

/**
 * 生成恢复项view
 * 
 * @author maiyongshen
 */
public class StretchRecordItemView extends LinearLayout {
	private LayoutInflater mInflater = null;
	private ArrayList<ChildItemView> mChildViewList = new ArrayList<ChildItemView>();
	private ChildItemView mEllipsisItem = null;

	/**
	 * 子项，包括图标icon和文字描述
	 * 
	 * @author huanglun
	 */
	public static class ChildItemView {
		private ImageView mIcon = null;
//		private ImageView mEllIcon = null;
		private TextView mText = null;
		private View mRootView = null;
		private final static int BOARD_WIDTH = 2;

		ChildItemView(ViewGroup parent, View rootView) {
			mRootView = rootView;
//			mEllIcon = (ImageView) mRootView.findViewById(R.id.info_icon_ellipsis);
			mIcon = (ImageView) mRootView.findViewById(R.id.info_icon);
			mText = (TextView) mRootView.findViewById(R.id.info_data);
//			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//			if (parent.getChildCount() == 0) {
//				// 第一个child view的paddingLeft 设为0
//				int paddingLeft = 0;
//				int paddingRight = mRootView.getPaddingRight();
//				int paddingTop = mRootView.getPaddingTop();
//				int paddingBottom = mRootView.getPaddingBottom();
//				mRootView.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
//			}
			parent.addView(mRootView/*, params*/);
		}

		public void restore() {
//			mEllIcon.setVisibility(View.GONE);
			mIcon.setVisibility(View.VISIBLE);
			mText.setVisibility(View.VISIBLE);
		}

		public void setIcon(int resId) {
			if (mIcon != null) {
				mIcon.setBackgroundResource(resId);
			}
		}

		public void setText(String text) {
			if (mText != null) {
				mText.setText(text);
			}
		}

		public int getWidth() {
			int width = 0;
			if (mIcon != null) {
				width += mIcon.getBackground().getIntrinsicWidth();
			}

			if (mText != null) {
				TextPaint paint = mText.getPaint();
				String text = mText.getText().toString();
				width += paint.measureText(text);
			}

			width += mRootView.getPaddingLeft();
			width += mRootView.getPaddingRight();

			width += BOARD_WIDTH;
			return width;
		}
	}

	public StretchRecordItemView(Context context) {
		this(context, null);
	}

	public StretchRecordItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		initEllipisItemView();
	}

	public void addChildItem(int iconId, String text) {
		View childView = mInflater.inflate(R.layout.record_info_item, this, false);
		ChildItemView childItem = new ChildItemView(this, childView);
		childItem.setIcon(iconId);
		childItem.setText(text);
		mChildViewList.add(childItem);
		requestLayout();
	}

	private void initEllipisItemView() {
		View childView = mInflater.inflate(R.layout.record_info_item, this, false);
		mEllipsisItem = new ChildItemView(this, childView);
		mEllipsisItem.setIcon(R.drawable.item_ellipsis);
		mEllipsisItem.setText("");
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);

		int left = l, right = 0;
		int count = mChildViewList.size();
		if (count <= 0) {
			return;
		}

		int index = -1;
		for (int i = 0; i < count; i++) {
			ChildItemView childView = mChildViewList.get(i);
			childView.restore();
			right = left + childView.getWidth();
			
			if (right == r) {
				index = i;
				break;
			} else if (right > r) {
				index = i - 1;
				break;
			}

//			if (right > r) {
//				if (r - left >= mEllipsisItem.getWidth()) {
//					// 当前view设置省略号
//					index = i;
//				} else {
//					// 设置前一view的省略号
//					index = i - 1;
//				}
//				break;
//			}

			left += childView.getWidth();
		}

		if (index >= 0) {
			ImageView icon = (ImageView) getChildAt(index).findViewById(R.id.info_icon);
			icon.setBackgroundResource(R.drawable.item_ellipsis);
			TextView text = (TextView) getChildAt(index).findViewById(R.id.info_data);
			text.setText("");
			
			for (int j = index + 1; j < count; j++) {
				getChildAt(j).setVisibility(View.INVISIBLE);
			}
		}

		// super.onLayout(changed, l, t, r, b);
	}

	@Override
	public void removeAllViews() {
		super.removeAllViews();
		mChildViewList.clear();
	}
}
