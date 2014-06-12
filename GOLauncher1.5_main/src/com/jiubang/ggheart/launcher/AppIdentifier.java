package com.jiubang.ggheart.launcher;

import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.ContactsContract;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.dock.DockUtil;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.IItemType;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.plugin.notification.NotificationType;
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  
 * @date  [2012-11-9]
 */
public class AppIdentifier {
	public static Intent createSelfDialIntent(Context context) {
		PackageManager pManager = context.getPackageManager();

		// 方式1问题：有些机型找不到拨号程序
		Intent intent = new Intent(Intent.ACTION_DIAL);

		List<ResolveInfo> infos = pManager.queryIntentActivities(intent,
				PackageManager.GET_RESOLVED_FILTER | PackageManager.GET_INTENT_FILTERS
						| PackageManager.MATCH_DEFAULT_ONLY);
		if (null == infos || infos.size() == 0) {
			// 方式2问题：这种方式在HD 2.3.3系统上这样起拨号，进入界面无历史纪录显示
			intent.setAction(Intent.ACTION_VIEW);
			Uri uri = Uri.parse("tel:");
			intent.setData(uri);
		}

		if (null != infos) {
			infos.clear();
			infos = null;
		}

		return intent;
	}

	public static Intent createSelfContactIntent(Context context) {
		Intent intent = new Intent(Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI);

		return intent;
	}

	public static Intent createSelfMessageIntent() {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setType("vnd.android-dir/mms-sms");
		return intent;
	}

	public static Intent createSelfBrowseIntent(PackageManager packageManager) {
		Intent intent = null;

		try {
			intent = getSysBrowserIntent();
			ComponentName cn = intent.getComponent();
			ActivityInfo info = packageManager.getActivityInfo(cn, 0);
		} catch (PackageManager.NameNotFoundException e) {
			intent = getGoogleUriIntent();
		}

		return intent;
	}

	public static Intent getSysBrowserIntent() {
		String packageName = "com.android.browser";
		String infoName = "com.android.browser.BrowserActivity";

		Intent intent = new Intent(Intent.ACTION_MAIN);
		ComponentName cn = new ComponentName(packageName, infoName);
		intent.setComponent(cn);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

		return intent;
	}

	public static Intent getGoogleUriIntent() {
		Uri uri = Uri.parse("http://www.google.com");
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		return intent;
	}

	public static Intent createAppdrawerIntent() {
		Intent intent = new Intent(ICustomAction.ACTION_SHOW_FUNCMENU);

		return intent;
	}

	/**
	 * 判断是不是、是什么通讯统计程序
	 * 
	 * @param context
	 * @param intent
	 *            被判断程序
	 * @return 是什么通讯程序，通讯程序定义在notificationtype.java
	 */
	public static int whichTypeOfNotification(Context context, AppItemInfo appItemInfo) {
		int type = NotificationType.IS_NOT_NOTIFICSTION;
		if (context == null || appItemInfo == null) {
			return type;
		}

		if (AppIdentifier.isDial(context, appItemInfo.mIntent)) {
			type = NotificationType.NOTIFICATIONTYPE_CALL;
		} else if (AppIdentifier.isGmail(context, appItemInfo.mIntent)) {
			type = NotificationType.NOTIFICATIONTYPE_GMAIL;
		} else if (AppIdentifier.isMessge(context, appItemInfo.mIntent)) {
			type = NotificationType.NOTIFICATIONTYPE_SMS;
		} else if (AppIdentifier.isK9mail(context, appItemInfo.mIntent)) {
			type = NotificationType.NOTIFICATIONTYPE_K9MAIL;
		} else if (AppIdentifier.isFacebook(context, appItemInfo.mIntent)) {
			type = NotificationType.NOTIFICATIONTYPE_FACEBOOK;
		} else if (AppIdentifier.isSinaWeibo(context, appItemInfo.mIntent)) {
			type = NotificationType.NOTIFICATIONTYPE_SinaWeibo;
		} else if (appItemInfo.getNotificationType() != NotificationType.IS_NOT_NOTIFICSTION) {
			type = appItemInfo.getNotificationType(); // 目前只有更多应用存放于appItemInfo
		}

		return type;
	}

	public static int getNotificationType(Context context, ShortCutInfo shortCutInfo) {
		int appType = NotificationType.IS_NOT_NOTIFICSTION;
		if (null != shortCutInfo) {
			if (shortCutInfo.mItemType == IItemType.ITEM_TYPE_SHORTCUT) {
				if (DockUtil.isDockDial(shortCutInfo.mIntent)) {
					appType = NotificationType.NOTIFICATIONTYPE_CALL;
				} else if (DockUtil.isDockSms(shortCutInfo.mIntent)) {
					appType = NotificationType.NOTIFICATIONTYPE_SMS;
				}
			} else if (shortCutInfo.mItemType == IItemType.ITEM_TYPE_APPLICATION) {
				appType = AppIdentifier.whichTypeOfNotification(context,
						shortCutInfo.getRelativeItemInfo());
			}
		}
		return appType;
	}

	public static boolean isDial(Context context, Intent intent) {
		if (null == intent) {
			return false;
		}

		Uri uri = intent.getData();
		if (null != uri) {
			if (uri.toString().contains("tel:")) {
				return true;
			}
		}

		String dialArray[] = null;
		try {
			// 捕获包括context=null或取不到相应资源的情况
			dialArray = context.getResources().getStringArray(R.array.notifiction_dialers_array);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		// {
		// "dialer",
		// "android.intent.action.DIAL",
		// // "kz.mek.DialerOne",
		// "DialtactsActivity"
		// };
		return contains(intent, dialArray);
	}

	public static boolean isMessge(Context context, Intent intent) {

		if (null == intent) {
			return false;
		}

		String type = intent.getType();
		if (null != type) {
			if (type.contains("vnd.android-dir/mms-sms")) {
				// 这是GO桌面初始化时的type识别
				return true;
			}
		}

		String default_messageArray[] = null;
		String full_messageArray[] = null;

		try {
			// 捕获包括context=null或取不到相应资源的情况
			default_messageArray = context.getResources().getStringArray(
					R.array.notification_default_sms_array);
			full_messageArray = context.getResources().getStringArray(
					R.array.notification_full_sms_array);

		} catch (Throwable e) {
			e.printStackTrace();
		}
		// {
		//
		// //一般系统短信程序会包含的字段
		// "com.android.mms",
		// "messaging", //defy 系统信息
		// "ConversationList", //defy 文本信息
		// //特别出名的短信程序
		// "com.jb.mms", //go SMS
		// "com.handcent.nextsms", //handcent SMS
		// "com.p1.chompsms"
		// };
		return equals(intent, full_messageArray) || contains(intent, default_messageArray);
	}

	public static boolean isGmail(Context context, Intent intent) {
		if (null == intent) {
			return false;
		}

		String gmailArray[] = null;
		try {
			// 捕获包括context=null或取不到相应资源的情况
			gmailArray = context.getResources().getStringArray(R.array.notification_gmail_array);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		// {
		// "com.google.android.gm"
		// };
		return contains(intent, gmailArray);
	}

	public static boolean isK9mail(Context context, Intent intent) {
		if (null == intent) {
			return false;
		}

		String k9mailArray[] = null;
		try {
			// 捕获包括context=null或取不到相应资源的情况
			k9mailArray = context.getResources().getStringArray(R.array.notification_k9mail_array);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		// {
		// "com.google.android.gm"
		// };
		return contains(intent, k9mailArray);
	}

	public static boolean isFacebook(Context context, Intent intent) {
//		if (null == intent) {
//			return false;
//		}
//
//		String facebookArray[] = null;
//		try {
//			// 捕获包括context=null或取不到相应资源的情况
//			facebookArray = context.getResources().getStringArray(
//					R.array.notification_facebook_array);
//		} catch (Throwable e) {
//			e.printStackTrace();
//		}
//		return contains(intent, facebookArray);
		return false;
	}

	public static boolean isSinaWeibo(Context context, Intent intent) {
		if (null == intent) {
			return false;
		}

		String sinaweiboArray[] = null;
		try {
			sinaweiboArray = context.getResources().getStringArray(
					R.array.notification_sinaweibo_array);
		} catch (Throwable e) {
			e.printStackTrace();
		}

		return contains(intent, sinaweiboArray);
	}

	private static boolean contains(Intent intent, String[] contentArray) {
		boolean bRet = false;

		if (contentArray == null) {
			return false;
		}

		String intentString = null;
		try {
			intentString = intent.toUri(0);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		int len = contentArray.length;
		for (int i = 0; i < len; i++) {
			bRet |= intentString.contains(contentArray[i]);
		}
		return bRet;
	}

	// 按包全名的比较方法
	// private static boolean equals(Intent intent, String[] contentArray)
	// {
	// boolean bRet = false;
	// //包名开始的标识
	// String flagText = ";component=";
	// int start_index;
	// int end_index;
	// if(contentArray == null)
	// {
	// return false;
	// }
	//
	// String intentString = null;
	// try
	// {
	// /* 例子：Go短信的facebook插件intent.toUri(0)返回的结果为
	// *
	// #Intent;action=android.intent.action.MAIN;category=android.intent.category.LAUNCHER;launchFlags=0x10200000;component=com.jb.gosms.chat/com.jb.gosms.facebook.Facebook.LoginActivity;end
	// */
	// intentString = intent.toUri(0);
	// // intentString = intent.getPackage().toString();//注意有些app没有包名
	// start_index = intentString.indexOf(flagText)+flagText.length();
	// end_index = intentString.lastIndexOf("/");
	// //确保起始字符在结束字符前面
	// if( start_index<=end_index )
	// intentString = intentString.substring(start_index, end_index);
	//
	// }
	// catch (Exception e)
	// {
	// e.printStackTrace();
	// return false;
	// }
	// int len = contentArray.length;
	// for (int i = 0; i < len; i++)
	// {
	// bRet |= intentString.equals(contentArray[i]);
	// }
	// return bRet;
	// }

	// 按包全名的比较方法
	private static boolean equals(Intent intent, String[] contentArray) {
		boolean bRet = false;
		if (contentArray == null) {
			return false;
		}

		String intentString = null;
		try {
			/*
			 * 例子：Go短信的facebook插件intent.toUri(0)返回的结果为
			 * #Intent;action=android.intent
			 * .action.MAIN;category=android.intent.
			 * category.LAUNCHER;launchFlags
			 * =0x10200000;component=com.jb.gosms.chat
			 * /com.jb.gosms.facebook.Facebook.LoginActivity;end
			 */
			ComponentName componentName = intent.getComponent();
			if (componentName != null) {
				intentString = componentName.getPackageName();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		if (intentString != null) {
			int len = contentArray.length;
			for (int i = 0; i < len; i++) {
				bRet |= intentString.equals(contentArray[i]);
			}
		}
		return bRet;
	}

}
