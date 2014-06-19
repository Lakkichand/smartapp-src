package com.jiubang.go.backup.pro.statistics;

/**
 * 统计Key
 *
 * @author GoBackup Dev Team
 */
public class StatisticsKey {
	// 设备ID
	public static final String DEVICE_ID = "device_id";
	// 从菜单项进入“批量删除”的次数
	public static final String MENU_BATCH_DELETE = "menu_batch_delete";
	// 从设置项进入“批量删除”的次数
	public static final String SETTING_BATCH_DELETE = "setting_batch_delete";
	// 联系人备份设置
	public static final String CONTACTS_BACKUP_SETTING = "contacts_backup_setting";
	// 是否备份/恢复应用程序数据
	public static final String STATE_BACKUP_RESTORE_APP_DATA = "state_br_app_data";
	// 是否开启静默恢复
	public static final String STATE_SILENT_RESTORE = "state_silent_restore";
	// 是否曾经进入ROOT权限介绍页
	public static final String GOINTO_ROOT_INTRODUCTION = "go_into_root_intro";
	// 是否有使用过检查更新的功能
	public static final String HAS_CHECK_UPDATE = "has_checked_update";
	// 是否开启合并联系人功能
	public static final String MERGE_CONTACTS = "merge_contacts";
	// 是否开启联系人变更推送
	public static final String CONTACT_CHANGE_PUSH = "contact_change_push";
	// 上一次的日志上传时间
	public static final String LAST_UPLOAD_TIME = "last_upload_time";
	// 用户首次启动GO备份时间
	public static final String FIRST_LAUNCH_TIME = "first_launch_time";
	// 用户购买高级版时间
	public static final String PURCHASE_TIME = "purchase_time";
	// 是否打开定时备份开关
	public static final String ENABLE_SCHEDULE_BACKUP = "enable_schedule_backup";

	// 从设置菜单进入高级版页面的次数
	public static final String PREMIUM_ENTRANCE_MENU = "premium_entrance_menu";
	// 从备份大小限制进入高级版页面的次数
	public static final String PREMIUM_ENTRANCE_BACKUP_SIZE_LIMIT = "premium_entrance_bakcup_size_limit";
	// 从备份系统数据进入高级版页面的次数
	public static final String PREMIUM_ENTRANCE_BACKUP_SYSTEM_SETTING = "premium_entrance_backup_system_setting";
	// 从云端备份进入高级版页面的次数
	public static final String PREMIUM_ENTRANCE_CLOUD_BACKUP = "premium_entrance_cloud_backup";
	// 从编辑备份包入口进入高级版页面次数
	public static final String PREMIUM_ENTRANCE_EDIT_BACKUP = "premium_entrance_edit_backup";
	// 从仅备份应用数据功能入口进入高级版页面次数
	public static final String PREMIUM_ENTRANCE_BACKUP_APP_DATA_ONLY = "premium_entrance_backup_app_data_only";
	// 从冻结应用程序入口进入高级版次数
	public static final String PREMIUM_ENTRANCE_FREEZE_APP = "premium_entrance_freeze_app";

	// 用户购买高级版的入口
	public static final String PURCHASE_PREMIUM_VERSION_ENTRANCE = "purchase_premium_version_entrance";

	//进入高级功能介绍页面次数
	public static final String PURCHASE_PREMIUM_VERSION_HELP = "purchase_premium_version_help";

	//购买按钮点击次数
	public static final String PURCHASE_BUTTON_CLICK_COUNT = "purchase_button_count";

	//付费方式标志
	public static final String PURCHASE_METHOD = "payment_method";
	//默认还没有选择付费方式
	public static final int PURCHASE_UNSELECT_DEFEAULT = 0;
	//GETGAR
	public static final int PURCHASE_BY_GETGAR = 1;
	//内付费
	public static final int PURCHASE_BY_BILLING_IN_STORE = 2;
	//微薄分享
	public static final int PURCHASE_BY_SHARE_MICROBLOG = 3;
	//APK付费方式
	public static final int PURCHASE_BY_APK = 4;

	//付费方式点击数
	public static final String PURCHASE_METHOD_CLICK_COUNT = "payment_method_click_count";
	public static final int PURCHASE_METHOD_CLICK_COUNT_ZERO = 0;
	public static final int PURCHASE_METHOD_CLICK_COUNT_ONE = 1;

	//付费入口
	public static final int PURCHASE_FROM_INVALID_VALUE = -1;
	public static final int PURCHASE_FROM_CLOUD_BACKUP = 1;
	public static final int PURCHASE_FROM_BACKUP_SYSTEM_SETTING = 2;
	public static final int PURCHASE_FROM_BACKUP_SIZE_LIMIT = 3;
	public static final int PURCHASE_FROM_MENU = 4;
	public static final int PURCHASE_FROM_BACKUP_APP_DATA_ONLY = 5;
	public static final int PURCHASE_FROM_RESTORE_APP_DATA_ONLY = 6;
	public static final int PURCHASE_FROM_EDIT_BACKUP = 7;
	public static final int PURCHASE_FROM_FREEZE_APP = 8;
	
	
	//上传付费行为成功标志
	public static final String UPLOAD_ACTION_FLAS = "upload_action_flag";

}
