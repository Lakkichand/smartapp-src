package com.jiubang.ggheart.apps.desks.diy.themescan;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Selection;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.jiubang.ggheart.components.DeskAlertDialog;
import com.jiubang.ggheart.components.DeskEditText;

/**
 * 编辑对话框控件
 * 
 * @author ouyongqiang
 * 
 */
public class EditDialog extends DeskAlertDialog {

	public final static int MSG_SHOW_INPUTMETHOD = 1; // 内部消息，显示输入法
	/**
	 * 编辑框的左边界
	 */
	private final static int EDIT_TEXT_MARGIN_LEFT = 20;

	/**
	 * 编辑框的右边界
	 */
	private final static int EDIT_TEXT_MARGIN_RIGHT = 20;

	/**
	 * 编辑框的上边界
	 */
	private final static int EDIT_TEXT_MARGIN_TOP = 20;

	/**
	 * 编辑框的下边界
	 */
	private final static int EDIT_TEXT_MARGIN_Bottom = 20;

	private LinearLayout mLayout;
	private EditText mEditText;

	/**
	 * 构造函数
	 * 
	 * @param context
	 *            程序上下文
	 * @param title
	 *            对话框标题
	 */
	public EditDialog(Context context, String title) {
		super(context);
		setTitle(title);

		mLayout = new LinearLayout(context);
		mLayout.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));
		mLayout.setOrientation(LinearLayout.VERTICAL);

		mEditText = new DeskEditText(context);
		MarginLayoutParams marginParams = new MarginLayoutParams(new LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		marginParams.setMargins(EDIT_TEXT_MARGIN_LEFT, EDIT_TEXT_MARGIN_TOP,
				EDIT_TEXT_MARGIN_RIGHT, EDIT_TEXT_MARGIN_Bottom);
		android.widget.LinearLayout.LayoutParams linearParams = new android.widget.LinearLayout.LayoutParams(
				marginParams);
		mEditText.setHorizontallyScrolling(true);
		mEditText.setGravity(Gravity.FILL_HORIZONTAL);
		// mEditText.setTextAppearance(context,
		// android.R.style.TextAppearance_Medium);
		// mEditText.setTextAppearance(context,
		// android.R.color.primary_text_dark);

		mLayout.addView(mEditText, linearParams);
		setView(mLayout);
	}

	/**
	 * 设置编辑框内容
	 * 
	 * @param text
	 *            编辑框内容
	 */
	public void setText(String text) {
		mEditText.setText(text);
		Editable editable = mEditText.getText();
		Selection.setSelection(editable, editable.length());
	}

	/**
	 * 获取编辑框文本内容
	 * 
	 * @return String 返回的编辑框文本内容
	 */
	public String getText() {
		return mEditText.getText().toString();
	}

	/**
	 * 设置确定按钮
	 * 
	 * @param text
	 *            按钮显示的文本
	 * @param listener
	 *            确定按钮的监听者
	 */
	public void setPositiveButton(String text, DialogInterface.OnClickListener listener) {
		setButton(DialogInterface.BUTTON_POSITIVE, text, listener);
	}

	/**
	 * 设置取消按钮
	 * 
	 * @param text
	 *            按钮显示的文本
	 * @param listener
	 *            取消按钮的监听者
	 */
	public void setNegativeButton(String text, DialogInterface.OnClickListener listener) {
		setButton(DialogInterface.BUTTON_NEGATIVE, text, listener);
	}

	/*
	 * 显示对话框的同时，焦点处在编辑框中，并显示输入法。
	 */
	public void showWithInputMethod() {
		super.show();
		Message message = new Message();
		message.what = MSG_SHOW_INPUTMETHOD;
		mMsgHandler.sendMessage(message);
	}

	/**
	 * 内部消息处理
	 */
	private Handler mMsgHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case MSG_SHOW_INPUTMETHOD :
					InputMethodManager inputMethodManager = (InputMethodManager) getContext()
							.getSystemService(Context.INPUT_METHOD_SERVICE);
					if (inputMethodManager != null) {
						// 显示输入法
						inputMethodManager.showSoftInput(mEditText, 0);
					}
					break;
				default :
					break;
			}
		}
	};

	@Override
	public void selfDestruct() {
		super.selfDestruct();
	}
}
