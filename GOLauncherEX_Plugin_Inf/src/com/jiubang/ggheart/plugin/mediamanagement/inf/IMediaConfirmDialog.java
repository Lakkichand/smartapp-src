package com.jiubang.ggheart.plugin.mediamanagement.inf;

import android.view.View;
import android.widget.CheckBox;
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  dingzijian
 * @date  [2012-11-23]
 */
public interface IMediaConfirmDialog extends IMediaBaseDialog {

	/**
	 * <br>功能简述:查找checkBox提示框View
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param view
	 */
	public void findTipCheckBox(View view);

	/**
	 * <br>功能简述:设置显示内容
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param msg
	 */
	public void setMessage(String msg);

	/**
	 * <br>功能简述:显示checkBox
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void showTipCheckBox();

	/**public MediaDialog(Context context, int theme) {
		super(context, theme);
	}
	 * <br>功能简述:获取提示框
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public CheckBox getTipCheckBox();

	/**
	 * <br>功能简述:设置checkBox提示框内容
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param text
	 */
	public void setTipCheckBoxText(CharSequence text);

}
