package org.acra;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 错误报告配置 通过这个类配置
 * 
 * @author luopeihuan
 * 
 */
public final class CrashReportConfig {
	/** 通知栏用到的资源 */
	public final static int RES_NOTIF_ICON = android.R.drawable.stat_notify_error;
	public final static int RES_NOTIF_TICKER_TEXT = R.string.crash_notif_ticker_text;
	public final static int RES_NOTIF_TITLE = R.string.crash_notif_title;
	public final static int RES_NOTIF_TEXT = R.string.crash_notif_text;

	/** 对话框用到的资源 */
	public final static int RES_DIALOG_ICON = android.R.drawable.ic_dialog_info;
	public final static int RES_DIALOG_TITLE = R.string.crash_dialog_title;
	public final static int RES_DIALOG_TEXT = R.string.crash_dialog_text;

	/** 对话框布局 */
	public final static int RES_DIALOG_LAYOUT = R.layout.report;
	/** 对话确定按钮id */
	public final static int RES_DIALOG_YES_BTN_ID = R.id.sure_report;
	/** 对话取消按钮id */
	public final static int RES_DIALOG_NO_BTN_ID = R.id.cancel_report;

	/** 邮件标题的字符串id */
	public final static int RES_EMAIL_SUBJECT = R.string.crash_subject;

	/** 收件邮箱 */
	public final static String EMAIL_RECEIVER = "golauncherexbug@gmail.com";

	/** 版本号描述字符串id */
	public final static int RES_APP_VERSION = R.string.curVersion;

	/** 程序名 */
	public final static String APP_NAME = LauncherEnv.APP_NAME;

	/** 崩溃日志保存路径 */
	public final static String LOG_PATH = LauncherEnv.Path.SDCARD + LauncherEnv.Path.LOG_DIR;

	/**
	 * 是否搜集额外的包信息 为ture需要配置 {@link #ADDITIONAL_TAG} 和
	 * {@link #ADDITIONAL_PACKAGES}
	 * */
	public final static boolean REPORT_ADDITIONAL_INFO = true;

	/** 额外的程序包标签 */
	public final static String ADDITIONAL_TAG = "GOWidget";

	/** 额外显示的包信息 (eg.GOWidget) */
	public final static String[] ADDITIONAL_PACKAGES = {
			LauncherEnv.Plugin.TASK_PACKAGE,
			LauncherEnv.Plugin.CONTACT_PACKAGE,
			LauncherEnv.Plugin.CALENDAR_PACKAGE,
			LauncherEnv.Plugin.RECOMMAND_GOSMS_PACKAGE,
			LauncherEnv.Plugin.GOSEARCH_PACKAGE,
			LauncherEnv.Plugin.FACEBOOK_PACKAGE,
			LauncherEnv.Plugin.TWITTER_PACKAGE,
			LauncherEnv.Plugin.SWITCH_PACKAGE,
			LauncherEnv.Plugin.CLOCK_PACKAGE,
			LauncherEnv.Plugin.BOOKMARK_PACKAGE,
			LauncherEnv.Plugin.NOTE_PACKAGE,
			LauncherEnv.Plugin.SINA_PACKAGE,
			LauncherEnv.Plugin.NEWS3G_PACKAGE,
			LauncherEnv.Plugin.TENCNT_PACKAGE,
			LauncherEnv.Plugin.EMAIL_PACKAGE,
			LauncherEnv.Plugin.RECOMMAND_GOWEATHEREX_PACKAGE,
			LauncherEnv.Plugin.RECOMMAND_GOPOWERMASTER_PACKAGE,
			"com.gau.go.launcherex.gowidget.searchwidget",
			"com.gau.go.launcherex.gowidget.smswidget",
			"com.android.vending",
			"com.go.multiplewallpaper",
			"com.gau.golauncherex.mediamanagement",
			"com.daqi.debughelper",
			LauncherEnv.GO_TOUCHHELPER_PACKAGE_NAME,
			"com.gau.go.launcherex.theme.papertown2",
			"com.gau.go.launcherex.theme.acidplanet2",
			"com.gau.go.launcherex.theme.dreamland",
			"com.gau.go.launcherex.theme.christmas2",
			"com.gau.go.launcherex.theme.Next2",
			"com.gau.go.launcherex.theme.Dryad2"};
}
