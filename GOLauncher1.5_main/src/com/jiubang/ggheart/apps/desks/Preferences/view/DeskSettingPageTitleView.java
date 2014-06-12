package com.jiubang.ggheart.apps.desks.Preferences.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;

/**
 * 
 * <br>
 * 类描述: <br>
 * 功能详细描述:
 * 
 * @author licanhui
 * @date [2012-9-10]
 */
public class DeskSettingPageTitleView extends RelativeLayout implements OnClickListener {
	private Context mContext;
	private LinearLayout mBackLayout;
	private TextView mTitleTextView;

	public DeskSettingPageTitleView(Context context) {
		super(context);
	}

	public DeskSettingPageTitleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DeskSettingItemView);
		Drawable imageDrawable = a.getDrawable(R.styleable.DeskSettingItemView_image);
		CharSequence titleText = a.getText(R.styleable.DeskSettingItemView_titleText);
		int titleLineHeight = a.getDimensionPixelSize(
				R.styleable.DeskSettingItemView_titleTextLineHeight, -1);
		a.recycle();

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.desk_setting_page_title_view, this);

		ImageView imageView = (ImageView) view.findViewById(R.id.title_image);
		if (imageDrawable != null) {
			imageView.setImageDrawable(imageDrawable);
		}

		mTitleTextView = (TextView) view.findViewById(R.id.title_name);
		if (titleText != null) {
			mTitleTextView.setText(titleText);
		}

		if (titleLineHeight != -1) {
			ImageView lineImageView = (ImageView) view.findViewById(R.id.line);
			LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) lineImageView
					.getLayoutParams();
			lp.height = titleLineHeight;
		}

		mBackLayout = (LinearLayout) findViewById(R.id.back_layout);
		mBackLayout.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.back_layout :
				((Activity) mContext).finish();
				break;

			default :
				break;
		}

	}

	/**
	 * <br>功能简述:获取返回布局
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public LinearLayout getBackLayout() {
		return mBackLayout;
	}

	/**
	 * <br>功能简述:设置标题
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param textString
	 */
	public void setTitleText(String textString) {
		mTitleTextView.setText(textString);
	}

	/**
	 * <br>功能简述:设置标题
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param resId
	 */
	public void setTitleText(int resId) {
		mTitleTextView.setText(resId);
	}

	/**
	 * <br>功能简述:获取标题
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public TextView getTitleTextView() {
		return mTitleTextView;
	}
	
}
