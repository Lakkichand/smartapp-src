package com.jiubang.ggheart.appgame.gostore.base.component;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher.ViewFactory;

import com.gau.go.launcherex.R;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreDisplayUtil;

/**
 * 
 * <br>类描述:应用中心主题九宫格列表，每一行
 * <br>功能详细描述:包括左、中、右三个view
 * 
 * @author  lijunye
 * @date  [2013-1-6]
 */
public class GridThreeCellItem extends LinearLayout {
	private RelativeLayout mLeftView = null;
	private TextView mLeftTextView = null;
	private ImageSwitcher mLeftImageSwitcher = null;
	private ThemesFeatureTag mLeftImageView = null;
	private RelativeLayout mMidView = null;
	private TextView mMidTextView = null;
	private ImageSwitcher mMidImageSwitcher = null;
	private ThemesFeatureTag mMidImageView = null;
	private RelativeLayout mRightView = null;
	private TextView mRightTextView = null;
	private ImageSwitcher mRightImageSwitcher = null;
	private ThemesFeatureTag mRightImageView = null;
	
	public GridThreeCellItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}
	
	public GridThreeCellItem(Context context) {
		super(context);
		initView();
	}
	
	private void initView() {
		View v = null;
		this.setOrientation(LinearLayout.HORIZONTAL);
		for (int i = 0; i < 3; i ++) {
			switch (i) {
				case 0 :
					mLeftView = initCell(i);
					v = mLeftView;
					break;
				case 1 :
					mMidView = initCell(i);
					v = mMidView;
					break;
				case 2 :
					mRightView = initCell(i);
					v = mRightView;
					break;
				default :
					break;
			}
			addView(v);
		}
	}
	
	private int getCellWidth() {
		int mod = DrawUtils.sWidthPixels % 3;
		int width = (DrawUtils.sWidthPixels - mod) / 3;
		return width;
	}
	
	private RelativeLayout initCell(int n) {
		RelativeLayout cellLayout = new RelativeLayout(getContext());
		int width = getCellWidth();
		int margin = (DrawUtils.sWidthPixels - (width * 3)) / 6 ;
		int padding = GoStoreDisplayUtil.scalePxToMachine(getContext(), 5);
		cellLayout.setBackgroundResource(R.drawable.recomm_app_list_item_selector);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				width, RelativeLayout.LayoutParams.WRAP_CONTENT);
		if (margin > 0) {
			params.setMargins(margin, 0, margin, 0);
		}
		cellLayout.setLayoutParams(params);
		RelativeLayout imgLayout = new RelativeLayout(getContext());
		imgLayout.setPadding(padding, padding, padding, padding);
		ImageSwitcher imageSwitcher = new ImageSwitcher(getContext());
		imageSwitcher.setFactory(new ViewFactory() {
			@Override
			public View makeView() {
				ImageView iv = new ImageView(getContext());
				iv.setScaleType(ScaleType.FIT_XY);
				iv.setLayoutParams(new ImageSwitcher.LayoutParams(
						ImageSwitcher.LayoutParams.FILL_PARENT, 
						ImageSwitcher.LayoutParams.FILL_PARENT));
				return iv;
			}
		});
		imageSwitcher.setInAnimation(getContext(), R.anim.appgame_fade_in);
		imageSwitcher.setOutAnimation(getContext(), R.anim.appgame_fade_out);
		RelativeLayout.LayoutParams imageSwitcherParams = new RelativeLayout.LayoutParams(
				width - (2 * padding), (int) ((width - (2 * padding)) / 0.6));
		imgLayout.addView(imageSwitcher, imageSwitcherParams);
		TextView textView = new TextView(getContext());
		textView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
		textView.setBackgroundResource(R.drawable.theme_title_bg);
		textView.setSingleLine(true);
		textView.setEllipsize(TruncateAt.END);
		textView.setTextColor(Color.parseColor("#ffffff"));
		textView.setTextSize(12);
		RelativeLayout.LayoutParams textViewParams = new RelativeLayout.LayoutParams(
				width, RelativeLayout.LayoutParams.WRAP_CONTENT);
		textViewParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		imgLayout.addView(textView, textViewParams);
		RelativeLayout.LayoutParams imgViewParams = new RelativeLayout.LayoutParams(
				(int) (width * 0.45), (int) (width * 0.45));
		imgViewParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		imgViewParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		imgViewParams.setMargins(0, GoStoreDisplayUtil.scalePxToMachine(getContext(), 1), 0, 0);
		RelativeLayout.LayoutParams imgLayoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT, 
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		cellLayout.addView(imgLayout, imgLayoutParams);
		ThemesFeatureTag imgView = new ThemesFeatureTag(getContext());
		imgView.setVisibility(GONE);
		imgView.setShining(false);
		cellLayout.addView(imgView, imgViewParams);
		switch (n) {
			case 0 :
				mLeftTextView = textView;
				mLeftImageSwitcher = imageSwitcher;
				mLeftImageView = imgView;
				break;
			case 1 :
				mMidTextView = textView;
				mMidImageSwitcher = imageSwitcher;
				mMidImageView = imgView;
				break;
			case 2 :
				mRightTextView = textView;
				mRightImageSwitcher = imageSwitcher;
				mRightImageView = imgView;
				break;
			default :
				break;
		}
		return cellLayout;
	}
	
	public RelativeLayout getLeftView() {
		return mLeftView;
	}
	
	public RelativeLayout getMidView() {
		return mMidView;
	}
	
	public RelativeLayout getRightView() {
		return mRightView;
	}
	
	public TextView getLeftTextView() {
		return mLeftTextView;
	}
	
	public ImageSwitcher getLeftImageSwitcher() {
		return mLeftImageSwitcher;
	}
	
	public TextView getMidTextView() {
		return mMidTextView;
	}
	
	public ImageSwitcher getMidImageSwitcher() {
		return mMidImageSwitcher;
	}
	
	public TextView getRightTextView() {
		return mRightTextView;
	}
	
	public ImageSwitcher getRightImageSwitcher() {
		return mRightImageSwitcher;
	}
	
	public ThemesFeatureTag getLeftImageView() {
		return mLeftImageView;
	}
	
	public ThemesFeatureTag getMidImageView() {
		return mMidImageView;
	}
	
	public ThemesFeatureTag getRightImageView() {
		return mRightImageView;
	}
}
