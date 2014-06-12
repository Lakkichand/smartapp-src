package com.jiubang.ggheart.apps.desks.Preferences.dialogs;

import android.view.View;

/**
 * 
 * 类描述:对话框的按钮监听接口
 * 功能详细描述:
 * 
 * @author  guoyiqing
 * @date  [2012-9-15]
 */
public interface OnDialogButtonClickListener {

	/**
	 * 功能简述:ok Button被点击的回调
	 * 功能详细描述:
	 * 注意:返回值表示是否隐藏dialog
	 * @param view 
	 * @return 是否通知隐藏dialog
	 */
	public boolean onPositiveClick(View view);

	/**
	 * 功能简述:cancel Button被点击的回调
	 * 功能详细描述:
	 * 注意:返回值表示是否隐藏dialog
	 * @param view 
	 * @return 是否通知隐藏dialog
	 */
	public boolean onNegativeClick(View view);

}
