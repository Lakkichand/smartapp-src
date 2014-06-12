package com.jiubang.ggheart.apps.desks.Preferences.dialogs;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;

/**
 * 
 * <br>类描述:普通确认对话框
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-10-29]
 */
public class DialogConfirm extends DialogBase {
	private TextView mMsgView; //提示内容
	private LinearLayout mTipCheckBoxLayout;
	private CheckBox mTipCheckBox;
	private TextView mTipCheckBoxText;

	public DialogConfirm(Context context) {
		super(context);
	}

	public DialogConfirm(Context context, int theme) {
		super(context, theme);
	}

	@Override
	public View getView() {
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.desk_setting_normal_dialog, null);
		mDialogLayout = (LinearLayout) view.findViewById(R.id.dialog_layout);
		mTitle = (TextView) view.findViewById(R.id.dialog_title);
		mMsgView = (TextView) view.findViewById(R.id.dialog_msg);
		mOkButton = (Button) view.findViewById(R.id.dialog_ok);
		mCancelButton = (Button) view.findViewById(R.id.dialog_cancel);

		findTipCheckBox(view);
		
		return view;
	}

	/**
	 * <br>功能简述:查找checkBox提示框View
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param view
	 */
	public void findTipCheckBox(View view) {
		if (view == null) {
			return;
		}
		mTipCheckBoxLayout = (LinearLayout) view.findViewById(R.id.tip_layout);
		mTipCheckBox = (CheckBox) view.findViewById(R.id.tip_check_box);
		mTipCheckBoxText = (TextView) view.findViewById(R.id.tip_text);
	}

	/**
	 * <br>功能简述:设置显示内容
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param msg
	 */
	public void setMessage(String msg) {
		if (mMsgView != null) {
			mMsgView.setText(msg);
		}
	}
	
	/**
	 * <br>功能简述:设置显示内容
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param msg
	 */
	public void setMessage(CharSequence msg) {
		if (mMsgView != null) {
			mMsgView.setText(msg);
		}
	}

	/**
	 * <br>功能简述:设置显示内容
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param textId
	 */
	public void setMessage(int textId) {
		if (mMsgView != null) {
			mMsgView.setText(mContext.getText(textId));
		}
	}
	
	/**
	 * <br>功能简述:设置带html解释的string(例如:指定内空指定颜色)
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param string
	 */
	public void setHtmlMessage(String string) {
		if (mMsgView != null && string != null) {
			mMsgView.setText(Html.fromHtml(string));  
		}
	}

	/**
	 * <br>功能简述:显示checkBox
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void showTipCheckBox() {
		if (mTipCheckBoxLayout != null) {
			mTipCheckBoxLayout.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * <br>功能简述:获取提示框
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public CheckBox getTipCheckBox() {
		return mTipCheckBox;
	}

	/**
	 * <br>功能简述:设置checkBox提示框内容
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param text
	 */
	public void setTipCheckBoxText(CharSequence text) {
		if (mTipCheckBoxText != null) {
			if (text != null) {
				mTipCheckBoxText.setText(text);
			}
		}
	}

	/**
	 * <br>功能简述:设置checkBox提示框内容
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param textId ID
	 */
	public void setTipCheckBoxText(int textId) {
		if (mTipCheckBoxText != null) {
			mTipCheckBoxText.setText(textId);
		}
	}

}
