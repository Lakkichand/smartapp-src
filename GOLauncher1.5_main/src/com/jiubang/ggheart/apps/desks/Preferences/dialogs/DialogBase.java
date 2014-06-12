package com.jiubang.ggheart.apps.desks.Preferences.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingConstants;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;

/**
 * 
 * <br>类描述:对话框 - 基类
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-10-29]
 */
public class DialogBase extends Dialog {
	public Context mContext = null;
	public LinearLayout mDialogLayout;
	public TextView mTitle; //标题
	public Button mOkButton; //确认按钮
	public Button mCancelButton; //取消按钮
	public View.OnClickListener mOkOnClickListener;
	public View.OnClickListener mCancleOnClickListener;

	public DialogBase(Context context) {
		this(context, R.style.Dialog);
	}

	public DialogBase(Context context, int theme) {
		super(context, theme);
		mContext = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View view = getView();
		if (view != null) {
			setDialogWidth(mDialogLayout, mContext);
			setButtonLister();
			setContentView(view);
		}
	}

	public View getView() {
		return null;
	}

	/**
	 * <br>功能简述:设置对话框的宽度，使用屏幕宽度为横竖屏的宽度
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public static void setDialogWidth(View layout, Context context) {
		if (layout != null) {
			int screenWidth = GoLauncher.getScreenWidth();
			int screenHeight = GoLauncher.getScreenHeight();
			int width;
			if (screenWidth <= screenHeight) {
				width = screenWidth;
			} else {
				width = screenHeight;
			}
			int pddingWidth = (int) context.getResources().getDimension(
					R.dimen.dialog_padding_width);
			layout.getLayoutParams().width = width - pddingWidth;
		}
	}

	/**
	 * <br>功能简述:设置标题
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param titleString
	 */
	public void setTitle(String titleString) {
		if (mTitle != null) {
			mTitle.setText(titleString);
		}
	}

	/**
	 * <br>功能简述:设置标题
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param titleString
	 */
	public void setTitle(int resId) {
		if (mTitle != null) {
			mTitle.setText(resId);
		}
	}

	/**
	 * <br>功能简述:设置确认按钮的visible
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param visible
	 */
	public void setPositiveButtonVisible(int visible) {
		if (mOkButton != null) {
			mOkButton.setVisibility(visible);
		}
	}

	/**
	 * <br>功能简述:设置取消按钮的visible
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param visible
	 */
	public void setNegativeButtonVisible(int visible) {
		if (mCancelButton != null) {
			mCancelButton.setVisibility(visible);
		}
	}

	/**
	 * <br>功能简述:设置确定按钮点击事件
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param text 按钮显示内容-String
	 * @param listener 监听器
	 */
	public void setPositiveButton(CharSequence text, View.OnClickListener listener) {
		if (mOkButton != null) {
			if (text != null) {
				mOkButton.setText(text);
			}
			mOkOnClickListener = listener;
		}
	}

	/**
	 * <br>功能简述:设置确定按钮点击事件
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param textId  text 按钮显示内容 - ID
	 * @param listener 监听器
	 */
	public void setPositiveButton(int textId, View.OnClickListener listener) {
		if (mOkButton != null) {
			mOkButton.setText(mContext.getText(textId));
			mOkOnClickListener = listener;
		}
	}

	/**
	 * <br>功能简述:设置取消按钮点击事件
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param text 按钮显示内容-String
	 * @param listener 监听器
	 */
	public void setNegativeButton(CharSequence text, View.OnClickListener listener) {
		if (mCancelButton != null) {
			if (text != null) {
				mCancelButton.setText(text);
			}
			mCancleOnClickListener = listener;
		}
	}

	/**
	 * <br>功能简述:设置取消按钮点击事件
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param textId  text 按钮显示内容 - ID
	 * @param listener 监听器
	 */
	public void setNegativeButton(int textId, View.OnClickListener listener) {
		if (mCancelButton != null) {
			mCancelButton.setText(mContext.getText(textId));
			mCancleOnClickListener = listener;
		}
	}

	/**
	 * <br>功能简述:设置确认和取消按钮的基本点击事件
	 * <br>功能详细描述:关闭对话框
	 * <br>注意:
	 */
	public void setButtonLister() {
		if (mOkButton != null) {
			mOkButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if (mOkOnClickListener != null) {
						mOkOnClickListener.onClick(v);	//调用外部设置的监听方法，然后关闭对话框
					}
					dismiss();
				}
			});
		}
		if (mCancelButton != null) {
			mCancelButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if (mCancleOnClickListener != null) {
						mCancleOnClickListener.onClick(v);	//调用外部设置的监听方法，然后关闭对话框
					}
					dismiss();
				}
			});
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		//对话框关闭时遍历所有控件，把DeskView和DeskButton反注册
		DeskSettingConstants.selfDestruct(getWindow().getDecorView());
	}
}