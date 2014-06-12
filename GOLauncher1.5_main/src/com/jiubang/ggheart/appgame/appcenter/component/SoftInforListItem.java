package com.jiubang.ggheart.appgame.appcenter.component;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.gowidget.gostore.component.SimpleImageView;

/**
 * 
 * @author zhujian 我的应用
 */
public class SoftInforListItem extends RelativeLayout {

	private SimpleImageView mSoftImgView = null;
	private TextView mSoftNameTextView = null;
	private TextView mVerTextView = null;
	private Button mOperatorButton = null;
	private RelativeLayout mLeftLayout = null;

	public SoftInforListItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public SoftInforListItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public SoftInforListItem(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onFinishInflate()
	 */
	@Override
	protected void onFinishInflate() {
		// TODO Auto-generated method stub
		super.onFinishInflate();
		init();
	}

	/**
	 * 初始化方法
	 */
	private void init() {
		// TODO Auto-generated method stub
		mSoftImgView = (SimpleImageView) this.findViewById(R.id.softImageView);
		mSoftNameTextView = (TextView) this.findViewById(R.id.softNameTextView);
		mVerTextView = (TextView) this.findViewById(R.id.verTextView);
		mOperatorButton = (Button) this.findViewById(R.id.operatorbutton);
		mLeftLayout = (RelativeLayout) this
				.findViewById(R.id.contentRelativeLayout);
	}

	/**
	 * 重置默认状态的方法
	 */
	public void resetDefaultStatus() {
		this.setTag(null);
		if (mSoftImgView != null) {
			mSoftImgView.clearIcon();
		}
		if (mSoftNameTextView != null) {
			mSoftNameTextView.setText("");
		}
		if (mVerTextView != null) {
			mVerTextView.setText("");
		}
		if (mOperatorButton != null) {
			mOperatorButton
					.setBackgroundResource(R.drawable.themestore_mainview_update_selector);
		}
	}

	public void destory() {
		if (mSoftImgView != null) {
			mSoftImgView.recycle();
			mSoftImgView = null;
		}
		mSoftNameTextView = null;
		mVerTextView = null;
		if (mOperatorButton != null) {
			mOperatorButton.setOnTouchListener(null);
			mOperatorButton = null;
		}
	}

	/**
	 * @return the mSoftImgView
	 */
	public SimpleImageView getSoftImgView() {
		return mSoftImgView;
	}

	/**
	 * @param mSoftImgView
	 *            the mSoftImgView to set
	 */
	public void setSoftImgView(SimpleImageView softImgView) {
		this.mSoftImgView = softImgView;
	}

	/**
	 * @return the mSoftNameTextView
	 */
	public TextView getSoftNameTextView() {
		return mSoftNameTextView;
	}

	/**
	 * @param mSoftNameTextView
	 *            the mSoftNameTextView to set
	 */
	public void setSoftNameTextView(TextView softNameTextView) {
		this.mSoftNameTextView = softNameTextView;
	}

	/**
	 * @return the mVerTextView
	 */
	public TextView getVerTextView() {
		return mVerTextView;
	}

	/**
	 * @param mVerTextView
	 *            the mVerTextView to set
	 */
	public void setVerTextView(TextView verTextView) {
		this.mVerTextView = verTextView;
	}

	/**
	 * @return the mOperatorTextView
	 */
	public Button getOperatorButton() {
		return mOperatorButton;
	}

	/**
	 * @param mOperatorTextView
	 *            the mOperatorTextView to set
	 */
	public void setOperatorTextView(Button operatorButton) {
		this.mOperatorButton = operatorButton;
	}

}
