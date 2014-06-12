package com.jiubang.ggheart.components.facebook;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.util.Log;
import android.widget.Toast;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionDefaultAudience;
import com.facebook.model.GraphObject;
import com.facebook.model.OpenGraphAction;
import com.gau.go.launcherex.R;
import com.gau.utils.net.util.NetUtil;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingBackupActivity;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;

/**
 * 
 * <br>
 * 类描述: <br>
 * 功能详细描述:
 * 
 * @author ruxueqin
 * @date [2012-11-30]
 */
public class GoFacebookUtil {

	public final static String APPID = "360931657329579"; // 桌面绑定的ID
	private static boolean sAble = true; // facebook是否可用

	private static UserInfo sUserInfo; // 用户信息
	private static final String POST_ACTION_PATH = "me/golauncher:apply";
	public static final String SERVER = "http://configbak.goforandroid.com/configbak/entrance?"; // 正式服务器
	// public static final String SERVER =
	// "http://ggtest.3g.net.cn:8081/configbak/entrance?"; // 测试服务器
	// public static final String SERVER =
	// "http://69.28.54.212/configbak/entrance?"; //　国外服务器
	// public static final String SERVER =
	// "http://192.168.215.121:8080/configbak/entrance?"; // 贤拨机器
	// public static final String SERVER =
	// "http://61.145.124.70:8081/configbak/entrance?"; // 新测试服务器

	private final static List<String> PERMISSIONS = new ArrayList<String>() {
		{
			add("publish_actions");
		}
	};

	public final static String sTAG = "gofacebook";
	public final static boolean DEBUG = false;

	// fb操作类型
	public static final int TYPE_NONE = 0; // 无
	public static final int TYPE_SHAREALINK = 1; // 分享一条链接
	public static final int TYPE_BACKUP = 2; // backup DB
	public static final int TYPE_RESTORE = 3; // restore DB
	public static final int TYPE_POSTMSG = 4; // 发送一条消息
	public static final int TYPE_OPEN_GRAPH = 5; // 发送Open Graph；
	public static final int TYPE_SHAREALINK_RESOTRE = 6; // 分享一条facebook恢复备份成功的链接

	// facebook上go桌面专页链接
	public static String sGOLAUNCHERPAGE_FACEBOOK = "http://www.facebook.com/golauncher";

	public static void initEnable(Activity activity) {
		Locale l = Locale.getDefault();
		String languageString = String.format("%s-%s", l.getLanguage(), l.getCountry());
		sAble = languageString != null && !languageString.contains("zh");
		
		//sdk=7,且没装facebook客户端的,不支持facebook登陆功能
		if (VERSION.SDK_INT < 8) {
			Intent intent = new Intent().setClassName("com.facebook.katana",
					"com.facebook.katana.ProxyAuth");
			PackageManager pm = activity.getPackageManager();
			List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);
			if (list == null || list.isEmpty()) {
				//没装facebook客户端
				sAble = false;
			}
		}
		

		PreferencesManager sp = new PreferencesManager(activity, IPreferencesIds.FACEBOOK_RECORD,
				Context.MODE_PRIVATE);
		boolean showFacebookRestoreDialog = sp.getBoolean(
				IPreferencesIds.FACEBOOK_RESTART_AFTER_RESTORE, false);
		if (showFacebookRestoreDialog) {
			showShareResotreDialog(activity);
			sp.putBoolean(IPreferencesIds.FACEBOOK_RESTART_AFTER_RESTORE, false);
			sp.commit();
		}
	}

	public static boolean isEnable() {
		return sAble;
	}

	/**
	 * <br>
	 * 功能简述:通过fb分享一条链接 <br>
	 * 功能详细描述: <br>
	 * 注意: 1:登陆fb; 2:分享链接;
	 * 
	 * @param activity
	 * @param link
	 */
	public static void shareALink(final Activity activity) {
		if (activity == null || activity.isFinishing()) {
			return;
		}

		if (NetUtil.getNetWorkType(activity) == NetUtil.NETWORKTYPE_NULL) {
			Toast.makeText(activity, R.string.facebook_networkerror, Toast.LENGTH_SHORT).show();
		} else {
			SessionStatusCallback callback = new SessionStatusCallback(activity, TYPE_SHAREALINK,
					null);
			Session session = Session.getActiveSession();
			if (session != null && session.isOpened()) {
				showShareDialog(activity);
			} else {
				if (session != null && !session.isClosed()) {
					session.close();
				}
				login(activity, callback, false);
			}
		}
	}

	/**
	 * <br>
	 * 功能简述:发布一条facebook信息 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param activity
	 * @param msg
	 *            信息内容
	 */
	public static void postAMsg(final Activity activity, String msg) {
		if (activity == null || activity.isFinishing()) {
			return;
		}

		if (NetUtil.getNetWorkType(activity) == NetUtil.NETWORKTYPE_NULL) {
			Toast.makeText(activity, R.string.facebook_networkerror, Toast.LENGTH_SHORT).show();
		} else {
			SessionStatusCallback callback = new SessionStatusCallback(activity, TYPE_POSTMSG, msg);
			Session session = Session.getActiveSession();
			if (session != null && session.isOpened()) {
				callback.shareALink(null, msg);
			} else {
				if (session != null && !session.isClosed()) {
					session.close();
				}
				login(activity, callback, false);
			}
		}
	}

	protected static void showShareDialog(final Activity activity) {
		if (activity != null && !activity.isFinishing()) {
			final FacebookShareDialog dialog = new FacebookShareDialog(activity);
			dialog.show();
		}
	}

	/**
	 * <br>功能简述:弹出facebook分享备份提示框
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param activity
	 */
	private static void showShareResotreDialog(final Activity activity) {
		if (activity != null && !activity.isFinishing()) {
			final FacebookShareDialog dialog = new FacebookShareDialog(activity);
			dialog.setType(FacebookShareDialog.TYPE_RESTORE);
			dialog.show();
			String title = activity.getString(R.string.facebook_restore_succeed_title);
			String content = activity.getString(R.string.facebook_restore_succeed_content);
			dialog.setTitle(title);
			dialog.setContent(content);
		}
	}

	/**
	 * <br>功能简述:facebook更换主题分享
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param activity
	 * @param object
	 */
	public static void sendOpenGraphByLogin(Activity activity, OpenGraphObject object) {
		if (activity == null || activity.isFinishing() || object == null) {
			return;
		}

		if (NetUtil.getNetWorkType(activity) == NetUtil.NETWORKTYPE_NULL) {
//			Toast.makeText(activity, R.string.facebook_networkerror, Toast.LENGTH_SHORT).show();
		} else {
			SessionStatusCallback callback = new SessionStatusCallback(activity, TYPE_OPEN_GRAPH,
					object);
			Session session = Session.getActiveSession();
			if (session != null && session.isOpened()) {
				FacebookOpenGraphUtil.requestFacebookOG(activity, object.getPkgName(),
						object.getThemeName(), new OGRequestServerHandler());
			} else {
				if (session != null && !session.isClosed()) {
					session.close();
				}
				login(activity, callback, false);
			}
		}
	}

	/**
	 * <br>
	 * 功能简述:登陆fb <br>
	 * 功能详细描述: <br>
	 * 注意:第一次登陆，会跳转fb界面进行fb授权验证（如果用户安装了fb,则跳fb应用的登陆界面；否则，跳转网页登陆）；否则，有记录帐户信息
	 * 
	 * @param activity
	 * @param callback
	 * @param useWebviewLoginDialog 是否强制使用webview登录框
	 */
	public static void login(Activity activity, SessionStatusCallback callback,
			boolean useWebviewLoginDialog) {
		Session currentSession = Session.getActiveSession();
		if (currentSession == null || currentSession.getState().isClosed()) {
			Session.setActiveSession(null);
			Session session = new Session.Builder(activity).setApplicationId(APPID).build();
			Session.setActiveSession(session);
			currentSession = session;
		}
		if (!currentSession.isOpened()) {
			Session.OpenRequest openRequest = new Session.OpenRequest(activity);

			if (openRequest != null) {
				PreferencesManager preferencesManager = new PreferencesManager(activity,
						IPreferencesIds.FACEBOOK_RECORD, Context.MODE_PRIVATE);
				boolean useDialogLogin = false;
				if (useWebviewLoginDialog && VERSION.SDK_INT >= 8) {
					useDialogLogin = preferencesManager.getBoolean(
							IPreferencesIds.FACEBOOK_USE_DIALOG_LOGIN, false);
				}
//				Session.setUseLoginDialog(useDialogLogin);
				openRequest.setCallback(callback);
				openRequest.setPermissions(PERMISSIONS);
				try {
					currentSession.openForPublish(openRequest);
				} catch (UnsupportedOperationException e) {
					// 可能由于快速点击，出现Session: an attempt was made to open an
					// already opened
					// session.java.lang.UnsupportedOperationException: Session:
					// an attempt was made to open an already opened session.
				}
			}
		}
	}

	public static boolean backupDB(DeskSettingBackupActivity activity) {
		if (NetUtil.getNetWorkType(activity) == NetUtil.NETWORKTYPE_NULL) {
			Toast.makeText(activity, R.string.facebook_networkerror, Toast.LENGTH_SHORT).show();
			return false;
		} else {
			SessionStatusCallback callback = new SessionStatusCallback(activity, TYPE_BACKUP, null);
			Session session = Session.getActiveSession();
			if (session != null && session.isOpened()) {
				if (GoFacebookUtil.getUserInfo().getId() != null) {
					FacebookBackupUtil.backupFacebookDB(activity);
				} else {
					callback.getUserInfo();
				}
			} else {
				if (session != null && !session.isClosed()) {
					session.close();
				}
				login(activity, callback, true);
			}
			return true;
		}
	}

	public static boolean restoreDB(DeskSettingBackupActivity activity) {
		if (NetUtil.getNetWorkType(activity) == NetUtil.NETWORKTYPE_NULL) {
			Toast.makeText(activity, R.string.facebook_networkerror, Toast.LENGTH_SHORT).show();
			return false;
		} else {
			SessionStatusCallback callback = new SessionStatusCallback(activity, TYPE_RESTORE, null);
			Session session = Session.getActiveSession();
			if (session != null && session.isOpened()) {
				if (GoFacebookUtil.getUserInfo().getId() != null) {
					FacebookBackupUtil.restoreFacebookDB(activity);
				} else {
					callback.getUserInfo();
				}
			} else {
				if (session != null && !session.isClosed()) {
					session.close();
				}
				login(activity, callback, true);
			}
			return true;
		}
	}

	/**
	 * <br>
	 * 功能简述:facebook帐号登出 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 */
	public static void logout(Context context) {
		Session session = Session.getActiveSession();
		if (session == null) {
			Session.setActiveSession(null);
			session = new Session.Builder(context).setApplicationId(APPID).build();
			Session.setActiveSession(session);
		}
		if (session != null) {
			if (!session.isClosed()) {
				session.closeAndClearTokenInformation();
			}
//			Session.setUseLoginDialog(false);

			PreferencesManager preferencesManager = new PreferencesManager(context,
					IPreferencesIds.FACEBOOK_RECORD, Context.MODE_PRIVATE);
			preferencesManager.putBoolean(IPreferencesIds.FACEBOOK_USE_DIALOG_LOGIN, true);
			preferencesManager.putString(IPreferencesIds.FACEBOOK_LOGIN_AS_USER, null);
			preferencesManager.putBoolean(IPreferencesIds.FACEBOOK_OPEN_GRAPH_SWITCH, false);
			preferencesManager.commit();
			Toast.makeText(context, R.string.facebook_logout_succeed, Toast.LENGTH_SHORT).show();
			GoFacebookUtil.log("logout succeed");
		}
	}

	// public static void requestSeverForFaceBook(String pkgName,Context
	// context){
	// //自用测试服务器
	// Toast.makeText(context, "theme pkg is "+pkgName,
	// Toast.LENGTH_LONG).show();
	// sendOpenGraph(context,"http://www.zqh.me/debugtheme.html");
	// }
	public static void sendOpenGraph(final String url) {
		Session session = Session.getActiveSession();
		if (session == null || !session.isOpened() || !hasPermissions(session)) {
			return;
		}

		AsyncTask<Void, Void, Response> facebookRequestTask = new AsyncTask<Void, Void, Response>() {
			@Override
			protected Response doInBackground(Void... params) {
				// TODO Auto-generated method stub
				ApplyAction applyAction = GraphObject.Factory.create(ApplyAction.class);
				ThemeGraphObject theme = GraphObject.Factory.create(ThemeGraphObject.class);
				theme.setUrl(url);
				applyAction.setTheme(theme);
				Request request = new Request(Session.getActiveSession(), POST_ACTION_PATH, null,
						HttpMethod.POST);
				request.setGraphObject(applyAction);
				return request.executeAndWait();
			}

			@Override
			protected void onPostExecute(Response result) {
				// TODO Auto-generated method stub
				onPostActionResponse(result);
			}
		};
		facebookRequestTask.execute();
	}

	private static void onPostActionResponse(Response response) {

		PostResponse postResponse = response.getGraphObjectAs(PostResponse.class);

		if (postResponse != null && postResponse.getId() != null) {
			// TODO: 分享成功
		} else {
			// TODO: 分享失败
		}
	}

	public static void requestNewPublishPermissions(Session session, Context activity) {
		if (session != null) {
			Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(
					(Activity) activity, PERMISSIONS)
					.setDefaultAudience(SessionDefaultAudience.FRIENDS);
			session.requestNewPublishPermissions(newPermissionsRequest);
		}
	}

	public static UserInfo getUserInfo() {
		if (sUserInfo == null) {
			sUserInfo = new UserInfo(null, null);
		}
		return sUserInfo;
	}
	/**
	 * 
	 * @author xiangliang
	 *
	 */
	private interface PostResponse extends GraphObject {
		String getId();
	}
	/**
	 * 
	 * @author xiangliang
	 *
	 */
	private interface ApplyAction extends OpenGraphAction {
		public ThemeGraphObject getTheme();

		public void setTheme(ThemeGraphObject theme);
	}
	/**
	 * 
	 * @author xiangliang
	 *
	 */
	private interface ThemeGraphObject extends GraphObject {
		public String getUrl();

		public void setUrl(String url);

		public String getId();

		public void setId(String id);
	}

	public static boolean hasPermissions(Session session) {
		List<String> permissions = session.getPermissions();
		List<String> publish_permission = new ArrayList<String>() {
			{
				add("publish_actions");
			}
		};
		return permissions.containsAll(publish_permission);
	}

	public static void log(String text) {
		if (DEBUG) {
			Log.i(sTAG, text);
		}
	}
}
