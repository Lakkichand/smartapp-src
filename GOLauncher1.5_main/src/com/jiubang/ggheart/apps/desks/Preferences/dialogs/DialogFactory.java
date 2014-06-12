package com.jiubang.ggheart.apps.desks.Preferences.dialogs;

import android.app.Dialog;
import android.content.Context;

import com.jiubang.ggheart.apps.desks.Preferences.info.DeskSettingInfo;

/**
 * 
 * <br>
 * 类描述:对话框工厂类 <br>
 * 功能详细描述:根据外部传入参数，生成不同的对话框
 * 
 * @author chenguanyu
 * @date [2012-9-12]
 */
public class DialogFactory {

	/**
	 * <br>功能简述:
	 * <br>功能详细描述:
	 * @param context
	 * @param deskSettingInfo
	 * @param onDialogSelectListener
	 */
	public static DeskSettingBaseDialog produceDialog(Context context,
			DeskSettingInfo deskSettingInfo, OnDialogSelectListener onDialogSelectListener) {
		if (context != null && deskSettingInfo != null) {

			switch (deskSettingInfo.getType()) {
				case DialogTypeId.TYPE_DESK_SETTING_SINGLECHOICE :
				case DialogTypeId.TYPE_DESK_SETTING_SINGLECHOICE_FONT :
					return new DeskSettingSingleChoiceDialog(context, deskSettingInfo,
							onDialogSelectListener);

				case DialogTypeId.TYPE_DESK_SETTING_SINGLECHOICE_WITH_CHECKBOX :
					return new DeskSettingSingleChoiceWithCheckboxDialog(context, deskSettingInfo,
							onDialogSelectListener);

				case DialogTypeId.TYPE_DESK_SETTING_MULTICHOICE :
					return new DeskSettingMultiChoiceDialog(context, deskSettingInfo,
							onDialogSelectListener);

				case DialogTypeId.TYPE_DESK_SETTING_SEEKBAR :
					return null;
//							new DeskSettingSeekbarDialog(context, deskSettingInfo,
//							onDialogSelectListener);

				case DialogTypeId.TYPE_DESK_SETTING_GESTURE :
					return new DeskSettingGestureDialog(context, deskSettingInfo,
							onDialogSelectListener);

				default :
					return null;
			}
		}
		return null;
	}

	/**
	 * <br>功能简述:
	 * <br>功能详细描述:
	 * @param context
	 * @param deskSettingInfo
	 * @param onDialogSelectListener
	 */
	public static Dialog produceDialog(Context context, int dialogType) {
		if (context == null) {
			return null;
		}
		switch (dialogType) {
			//普通确认对话框	
			case DialogTypeId.TYPE_NORMAL_MESSAGE :
				return new DialogConfirm(context);
			
			//普通单选对话框	
			case DialogTypeId.TYPE_NORMAL_SINGLECHOICE :
				return new DialogSingleChoice(context);

			default :
				return null;
		}
	}

}
