package com.jiubang.ggheart.plugin.mediamanagement.inf;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  dingzijian
 * @date  [2012-11-23]
 */
public interface IMediaBaseDialog {

	public IMediaSingChoiceDialog createSingChoiceDialog();

	public IMediaConfirmDialog createConfirmDialog();
	
	public Dialog createBaseDialog();

	public View getView();

	/**
	 * <br>功能简述:设置对话框的宽度，使用屏幕宽度为横竖屏的宽度
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void setDialogWidth(LinearLayout layout, Context context);
	/**
	 * <br>功能简述:设置标题
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param titleString
	 */
	public void setTitle(String titleString);
	/**
	 * <br>功能简述:设置确认按钮的visible
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param visible
	 */
	public void setPositiveButtonVisible(int visible);
	/**
	 * <br>功能简述:设置取消按钮的visible
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param visible
	 */
	public void setNegativeButtonVisible(int visible);

	/**
	 * <br>功能简述:设置确定按钮点击事件
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param text 按钮显示内容-String
	 * @param listener 监听器
	 */
	public void setPositiveButton(CharSequence text, View.OnClickListener listener);
	/**
	 * <br>功能简述:设置取消按钮点击事件
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param text 按钮显示内容-String
	 * @param listener 监听器
	 */
	public void setNegativeButton(CharSequence text, View.OnClickListener listener);
	/**
	 * <br>功能简述:设置确认和取消按钮的基本点击事件
	 * <br>功能详细描述:关闭对话框
	 * <br>注意:
	 */
	public void setButtonLister();

	public void show();

	public boolean isShowing();

	public void dismiss();
}
