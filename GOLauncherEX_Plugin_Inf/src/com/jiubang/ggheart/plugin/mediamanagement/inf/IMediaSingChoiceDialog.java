package com.jiubang.ggheart.plugin.mediamanagement.inf;

import android.content.DialogInterface.OnClickListener;
/**
 * 多媒体单选对话框接口
 */
public interface IMediaSingChoiceDialog extends IMediaBaseDialog {

	public void setItemData(CharSequence[] items, int checkItem, boolean isShowCheckBox);

	public void setOnItemClickListener(OnClickListener listener);

}
