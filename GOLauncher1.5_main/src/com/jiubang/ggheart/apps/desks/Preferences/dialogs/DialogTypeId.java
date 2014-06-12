package com.jiubang.ggheart.apps.desks.Preferences.dialogs;


/**
 * 
 * <br>类描述:对话框类型
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-9-12]
 */
public class DialogTypeId {

	//注意：0-6对应attrs.xml的dialogType。如果要修改就要对应 一起修改
	/**
	 * 桌面设置-单选对话框
	 */
	public static final int TYPE_DESK_SETTING_SINGLECHOICE = 0;
	
	/**
	 * 桌面设置-多选对话框
	 */
	public static final int TYPE_DESK_SETTING_MULTICHOICE = 1;
	
	/**
	 * 桌面设置-调节条对话框
	 */
	public static final int TYPE_DESK_SETTING_SEEKBAR = 2;
	
	/**
	 * 桌面设置-带checkbox的单选框
	 */
	public static final int TYPE_DESK_SETTING_SINGLECHOICE_WITH_CHECKBOX = 3;
	
	/**
	 *  桌面设置-普通对话框
	 */
	public static final int TYPE_DESK_SETTING_NORMAL = 4;
	
	/**
	 * 桌面设置-手势对话框
	 */
	public static final int TYPE_DESK_SETTING_GESTURE = 5;

	/**
	 * 桌面设置-字体单选框
	 */
	public static final int TYPE_DESK_SETTING_SINGLECHOICE_FONT = 6;
	
	//注意-end
	
	/**
	 * 普通提示对话框
	 */
	public static final int TYPE_NORMAL_MESSAGE = 7; 
	
	/**
	 * 普通单选对话框
	 */
	public static final int TYPE_NORMAL_SINGLECHOICE = 8;

}
