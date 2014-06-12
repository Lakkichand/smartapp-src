package com.jiubang.ggheart.apps.desks.Preferences.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.Preferences.OnValueChangeListener;

/**
 * 
 * 
 * <br>类描述: 带有CheckBox的设置ITEM控件
 * <br>功能详细描述:
 * 
 * @author licanhui
 * @date [2012-9-10]
 */
public class DeskSettingItemCheckBoxView extends DeskSettingItemBaseView {
	private Context mContext;
	private Boolean mIsBtnCheckBox;
	private CheckBox mCheckBox;

	private OnValueChangeListener mOnValueChangeListener; //监听器

	public DeskSettingItemCheckBoxView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DeskSettingItemView);
		mIsBtnCheckBox = a.getBoolean(R.styleable.DeskSettingItemView_isBtnCheckBox, false);
		a.recycle();

		initCheckBox();
		setOnClickListener(this);
		setOnCheckedChangeListener();
	}

	public void initCheckBox() {
		int width = 0;
		int paddingRight = 0;
		int buttonDrawable;
		if (mIsBtnCheckBox) {
			width = (int) this.getResources().getDimension(
					R.dimen.desk_setting_checkbox_button_width);
			buttonDrawable = R.drawable.desk_setting_checkbox_button;
		} else {
			width = (int) mContext.getResources().getDimension(R.dimen.desk_setting_checkbox_width);
			paddingRight = (int) mContext.getResources().getDimension(
					R.dimen.desk_setting_checkbox_padding_right);
			buttonDrawable = R.drawable.desk_setting_checkbox;
		}
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		LinearLayout checkBoxLayout = (LinearLayout) findViewById(R.id.checkbox_layout);
		checkBoxLayout.setPadding(0, 0, paddingRight, 0); //checkBox会被挡住
		mCheckBox = new CheckBox(mContext);
		mCheckBox.setButtonDrawable(buttonDrawable);
		checkBoxLayout.addView(mCheckBox, params);
	}

	/**
	 * <br>功能简述:	设置CheckBox点击监听事件
	 * <br>功能详细描述:
	 * <br>注意:不能设置CheckBox的setOnCheckedChangeListener事件。不然初始化设值的时候也会调用onValueChange的回调
	 */
	public void setOnCheckedChangeListener() {
		if (mCheckBox != null) {
			mCheckBox.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					if (mOnValueChangeListener != null) {
						boolean isCheck = mCheckBox.isChecked();
						mOnValueChangeListener.onValueChange(DeskSettingItemCheckBoxView.this,
								isCheck);
					}

				}
			});
		}
	}

	@Override
	public void onClick(View v) {
		boolean isCheck = mCheckBox.isChecked();
		mCheckBox.setChecked(!isCheck);
		if (mOnValueChangeListener != null) {
			mOnValueChangeListener.onValueChange(DeskSettingItemCheckBoxView.this, !isCheck);
		}
	}

	/**
	 * <br>功能简述:设置CheckBox的值
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param isChecked
	 */
	public void setIsCheck(boolean isChecked) {
		mCheckBox.setChecked(isChecked);
	}

	/**
	 * <br>功能简述:检测是否已勾选
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public boolean getIsCheck() {
		return mCheckBox == null ? false : mCheckBox.isChecked();
	}

	public CheckBox getCheckBox() {
		return mCheckBox;
	}

	public void setmCheckBox(CheckBox mCheckBox) {
		this.mCheckBox = mCheckBox;
	}

	/**
	 * <br>功能简述:设置值更改监听器
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param onValueChangeListener
	 */
	public void setOnValueChangeListener(OnValueChangeListener onValueChangeListener) {
		mOnValueChangeListener = onValueChangeListener;
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		mCheckBox.setEnabled(enabled);
	}
}
