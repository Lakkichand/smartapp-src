package com.jiubang.ggheart.appgame.gostore.base.component;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreDisplayUtil;

/**
 * 
 * <br>类描述:应用中心分类项
 * <br>功能详细描述:
 * 
 * @author  lijunye
 * @date  [2013-1-8]
 */
public class GridSortItem extends RelativeLayout {
	private ImageView mImageView = null;
	private TextView mTextView = null;
	private int mWidth = 0;
	
	public GridSortItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView();
	}
	
	public GridSortItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}
	
	public GridSortItem(Context context) {
		super(context);
		initView();
	}
	
	private void initView() {
		mWidth = DrawUtils.sWidthPixels / 2;
		int padding = GoStoreDisplayUtil.scalePxToMachine(getContext(), 4);
		this.setPadding(padding, padding, padding, padding);
		mImageView = new ImageView(getContext());
		mImageView.setScaleType(ScaleType.FIT_XY);
		RelativeLayout.LayoutParams imgParams = new RelativeLayout.LayoutParams(
				mWidth, (int) (mWidth * 0.6));
		imgParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		addView(mImageView, imgParams);
		mTextView = new TextView(getContext());
		mTextView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
		mTextView.setSingleLine(true);
		mTextView.setBackgroundResource(R.drawable.appgame_text_title_bg);
		mTextView.setTextColor(Color.parseColor("#ffffff"));
		mTextView.setTextSize(16);
		RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(
				mWidth, RelativeLayout.LayoutParams.WRAP_CONTENT);
		textParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		textParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		addView(mTextView, textParams);
	}
	
	public ImageView getImageView() {
		return mImageView;
	}
	
	public TextView getTextView() {
		return mTextView;
	}
}
