/**
 * 
 */
package com.jiubang.go.backup.pro.track.ga;

/**
 * @author liguoliang
 *
 */
public class TrackerEvent {
	public static final String CATEGORY_UI_ACTION = "Category_Ui_Action";
	public static final String CATEGORY_SCHEDULE = "Category_Schedule";
	public static final String CATEGORY_WIDGET = "Category_Widget";
	
	
	public static final String ACTION_BUTTON_PRESS = "Action_Button_Press";
	public static final String ACTION_PROGRESS = "Action_Progress";
	
	
	public static final String ACTION_WIDGET_ADD = "Action_Widget_Add";
	public static final String ACTION_WIDGET_DELETE = "Action_Widget_Delete";
	
	
	//widget
	public static final String LABEL_WIDGET_ADD = "Label_Widget_Add";
	public static final String LABEL_WIDGET_DELETE = "Label_Widget_Delete";
	
	// 主页
	public static final String LABEL_MAIN_NEWBACKUP_BUTTON = "Label_Main_NewBackup_Button";
	public static final String LABEL_MAIN_RESTORE_BUTTON = "Label_Main_Restore_Backup_Button";
	
	// 新建备份
	public static final String LABEL_NEWBACKUP_START_BUTTON = "Label_NewBackup_Start_Button";
	
	// 备份中
	public static final String LABEL_BACKUPPROCESS_STOP_DIALOG_OK_BUTTON = "Label_BackupProgcess_Stop_Dialog_Ok_Button";
	public static final String LABEL_BACKUPPROGRESS_FINISH = "Label_BackupProgress_Finsh";
	
	// 备份报告
	public static final String REPORT_SAVE_BUTTON = "Label_Report_Save_Button";
	public static final String REPORT_DISCARD_BUTTON = "Label_Report_Discard_Button";
	
	public static final long OPT_DOWN = 1;
	public static final long OPT_MOVE = 2;
	public static final long OPT_UP = 3;
	public static final long OPT_CANCEL = 4;
	public static final long OPT_CLICK = 5;
	public static final long OPT_WIDGET_ADD = 6;
	public static final long OPT_WIDGET_DELETE = 7;
	
}
