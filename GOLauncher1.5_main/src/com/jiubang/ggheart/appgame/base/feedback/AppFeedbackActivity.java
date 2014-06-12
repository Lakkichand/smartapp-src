package com.jiubang.ggheart.appgame.base.feedback;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import org.acra.ErrorReporter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.device.Machine;
import com.go.util.log.LogConstants;
import com.jiubang.ggheart.appgame.base.utils.AppFeedback;
import com.jiubang.ggheart.apps.desks.imagepreview.ChangeThemeMenu;
import com.jiubang.ggheart.apps.gowidget.gostore.util.FileUtil;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStorePhoneStateUtil;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 
 * <br>类描述:应用中心邮箱反馈Activity
 * <br>功能详细描述:
 * 
 * @author  zhengxiangcan
 * @date  [2012-12-4]
 */
public class AppFeedbackActivity extends Activity implements OnClickListener {
	
	/**
	 * 未知网络状态
	 */
	private final static String NETWORK_STATE_UNKNOW = "UNKNOW";
	/**
	 * 当前网络状态为wifi连接
	 */
	private final static String NETWORK_STATE_WIFI = "WIFI";
	/**
	 * 当前网络状态为gprs
	 */
	private final static String NETWORK_STATE_2G = "2G";
	/**
	 * 当前网络状态为3G或4G
	 */
	private final static String NETWORK_STATE_3G4G = "3G/4G";
	public static final String ENTRANCE = "entrance";
	public static final String PACKAGENAME = "appname";
	public static final int ENTRANCE_FROM_APP_LIST = 1;
	public static final int ENTRANCE_FROM_APP_DETAIL = 2;
	private TextView mTitle; //标题
	private EditText mFeedbackContent;
	private Button mSubmitBtn;
	private ImageButton mBackButton;
	private String mType = null;
	private String[] mItems = null;
	private ChangeThemeMenu mChangeFeedbackTypeMenu = null;
	private RelativeLayout mChangeContentBtn; //切换内容按钮
	private TextView mContentName; //切换内容后显示的标题内容
	private String mAppPackageName = null; //详情页邮箱反馈应用包名

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		int entrance = intent.getIntExtra(ENTRANCE, ENTRANCE_FROM_APP_LIST);
		mAppPackageName = intent.getStringExtra(PACKAGENAME);
		getMenuItems(entrance);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.appgame_app_feedback);
		initView();
		initChangeFeedbackTypeMenu(mItems);
		changeContent(0);
	}
	
	private void getMenuItems(int value) {
		if (value == ENTRANCE_FROM_APP_LIST) {
			mItems = getResources().getStringArray(
					R.array.appgame_menu_feedback_array);
		} else {
			mItems = getResources().getStringArray(
					R.array.appgame_detail_feedback_array);
		}
	}

	private void initView() {
		mTitle = (TextView) findViewById(R.id.appgame_feedback_title);
		mFeedbackContent = (EditText) findViewById(R.id.appgame_feedback_content);
		mChangeContentBtn = (RelativeLayout) findViewById(R.id.appgame_feedback_changeContentBtn);
		mContentName = (TextView) findViewById(R.id.appgame_feedback_contentName);
		mChangeContentBtn.setOnClickListener(this);
		mBackButton = (ImageButton) findViewById(R.id.appgame_feedback_backBtn);
		mBackButton.setOnClickListener(this);
		mSubmitBtn = (Button) findViewById(R.id.appgame_feedback_commitBtn);
		mSubmitBtn.setOnClickListener(this);
	}
	
	private void initChangeFeedbackTypeMenu(CharSequence[] items) {
		ArrayList<String> datas = new ArrayList<String>();
		for (CharSequence charSequence : items) {
			datas.add((String) charSequence);
		}
		mChangeFeedbackTypeMenu = new ChangeThemeMenu(this, datas);
		mChangeFeedbackTypeMenu.setmItemClickListener(this);
	}

	/**
	 * <br>功能简述:更换显示内容
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param position
	 */
	public void changeContent(int position) {
		if (mChangeFeedbackTypeMenu != null) {
			mChangeFeedbackTypeMenu.dismiss();
			mContentName.setText(mChangeFeedbackTypeMenu.getmStrings().get(position)); // 设置标题名称
		}
	}

	@Override
	public void onClick(View v) {
		//mChangeFeedbackTypeMenu选择的内容
		if (v instanceof TextView && v.getTag() != null
				&& v.getTag() instanceof Integer) {
			int position = (Integer) v.getTag();
			changeContent(position);
		}

		//点击更换内容
		else if (v == mChangeContentBtn) {
			if (mChangeFeedbackTypeMenu != null) {
				mChangeFeedbackTypeMenu.show(mChangeContentBtn);
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mFeedbackContent.getWindowToken(),
						0);
			}
		}
		
		else if (v == mBackButton) {
			finish();
		}
		
		else if (v == mSubmitBtn) {
			String subject = "Feedback for AppCenter&GameZone("
					+ getType() + " "
					+ getString(R.string.curVersion) + ")";
			String body = "\n\n";
			body += "\n" + getFeedbackContent();
			buideFeedbackAttachment(this, mAppPackageName);
			File file1 = new File(
					LauncherEnv.Path.APP_MANAGER_CLASSIFICATION_EXCEPTION_RECORD_PATH);
			Uri uri1 = null;
			if (file1.exists()) {
				// 判断是否有网络日志信息存在，如果存在则添加到附件中
				uri1 = Uri
						.parse("file://"
								+ LauncherEnv.Path.APP_MANAGER_CLASSIFICATION_EXCEPTION_RECORD_PATH);
			}
			File file2 = new File(
					LauncherEnv.Path.APP_MANAGER_FEEDBACK_RECOPE_PATH);
			Uri uri2 = null;
			if (file2.exists()) {
				// 判断是否有网络日志信息存在，如果存在则添加到附件中
				uri2 = Uri
						.parse("file://"
								+ LauncherEnv.Path.APP_MANAGER_FEEDBACK_RECOPE_PATH);
			}
			startMalil(this, AppFeedback.FEEDBACK_RECEIVER, subject, body, uri1, uri2);
			finish();
		}
	}
	
	/**
	 * <br>功能简述:获取邮件反馈的类型
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	private String getType() {
		mType = mContentName.getText().toString();
		return mType;
	}
	
	/**
	 * <br>功能简述:获取邮件反馈的正文
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	private String getFeedbackContent() {
		String content = null;
		content = mFeedbackContent.getText().toString();
		return content;
	}
	
	private void buideFeedbackAttachment(Context context, String appPackageName) {
		try {
			String product = "Product=" + android.os.Build.PRODUCT;
			String phoneModel = "\nPhoneModel=" + android.os.Build.MODEL;
			String kernel = "\nKernel=" + Machine.getLinuxKernel();
			String rom = "\nROM=" + android.os.Build.DISPLAY;
			String board = "\nBoard=" + android.os.Build.BOARD;
			String device = "\nDevice=" + android.os.Build.DEVICE;
			String density = "\nDensity="
					+ String.valueOf(context.getResources().getDisplayMetrics().density);
			String packageName = "\nPackageName=" + context.getPackageName();
			String androidVersion = "\nAndroidVersion="
					+ android.os.Build.VERSION.RELEASE;
			String totalMemSize = "\nTotalMemSize="
					+ (ErrorReporter.getTotalInternalMemorySize() / 1024 / 1024)
					+ "MB";
			String freeMemSize = "\nFreeMemSize="
					+ (ErrorReporter.getAvailableInternalMemorySize() / 1024 / 1024)
					+ "MB";
			String romAppHeapSize = "\nRom App Heap Size="
					+ Integer
							.toString((int) (Runtime.getRuntime().maxMemory() / 1024L / 1024L))
					+ "MB";
			// uid
			String uid = "Uid = " + GoStorePhoneStateUtil.getUid(context);
			// build country code
			String countryCode = buildCountryCode(context);
			// sim operator
			String simOperator = buildSimOperator(context);
			// build Network conditions
			String networkState = buildNetworkState(context);
			// save attachment to SD card
			String attachment = product + phoneModel + kernel + rom + board
					+ device + density + packageName + androidVersion
					+ totalMemSize + freeMemSize + romAppHeapSize + "\n" + uid
					+ "\n" + countryCode + "\n" + simOperator + "\n"
					+ networkState + "\n";
			if (appPackageName != null) {
				attachment += "APP packagename = " + appPackageName;
			}
			String filename = LauncherEnv.Path.APP_MANAGER_FEEDBACK_RECOPE_PATH;
			FileUtil.saveByteToSDFile(attachment.getBytes("UTF-8"), filename);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取当前网络状态，wifi，GPRS，3G，4G
	 * 
	 * @param context
	 * @return
	 */
	private String buildNetworkState(Context context) {
		// build Network conditions
		String ret = "";
		try {
			ConnectivityManager manager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkinfo = manager.getActiveNetworkInfo();
			if (networkinfo.getType() == ConnectivityManager.TYPE_WIFI) {
				ret = NETWORK_STATE_WIFI;
			} else if (networkinfo.getType() == ConnectivityManager.TYPE_MOBILE) {
				int subtype = networkinfo.getSubtype();
				switch (subtype) {
					case TelephonyManager.NETWORK_TYPE_1xRTT :
					case TelephonyManager.NETWORK_TYPE_CDMA :
					case TelephonyManager.NETWORK_TYPE_EDGE :
					case TelephonyManager.NETWORK_TYPE_GPRS :
					case TelephonyManager.NETWORK_TYPE_IDEN :
						// 2G
						ret = NETWORK_STATE_2G + "(typeid = "
								+ networkinfo.getType() + "  typename = "
								+ networkinfo.getTypeName() + "  subtypeid = "
								+ networkinfo.getSubtype() + "  subtypename = "
								+ networkinfo.getSubtypeName() + ")";
						break;
					case TelephonyManager.NETWORK_TYPE_EVDO_0 :
					case TelephonyManager.NETWORK_TYPE_EVDO_A :
					case TelephonyManager.NETWORK_TYPE_HSDPA :
					case TelephonyManager.NETWORK_TYPE_HSPA :
					case TelephonyManager.NETWORK_TYPE_HSUPA :
					case TelephonyManager.NETWORK_TYPE_UMTS :
						// 3G,4G
						ret = NETWORK_STATE_3G4G + "(typeid = "
								+ networkinfo.getType() + "  typename = "
								+ networkinfo.getTypeName() + "  subtypeid = "
								+ networkinfo.getSubtype() + "  subtypename = "
								+ networkinfo.getSubtypeName() + ")";
						break;
					case TelephonyManager.NETWORK_TYPE_UNKNOWN :
					default :
						// unknow
						ret = NETWORK_STATE_UNKNOW + "(typeid = "
								+ networkinfo.getType() + "  typename = "
								+ networkinfo.getTypeName() + "  subtypeid = "
								+ networkinfo.getSubtype() + "  subtypename = "
								+ networkinfo.getSubtypeName() + ")";
						break;
				}
			} else {
				ret = NETWORK_STATE_UNKNOW + "(typeid = "
						+ networkinfo.getType() + "  typename = "
						+ networkinfo.getTypeName() + ")";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "net = " + ret;
	}

	/**
	 * 获取语言和国家地区的方法 格式: SIM卡方式：cn 系统语言方式：zh-CN
	 * 
	 * @param context
	 * @return
	 */
	private String buildCountryCode(Context context) {
		String ret = null;
		try {
			TelephonyManager telManager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			if (telManager != null) {
				ret = telManager.getSimCountryIso();
				if (ret != null && !ret.equals("")) {
					ret = String.format("%s || %s", "Local Language:"
							+ Locale.getDefault().getLanguage().toLowerCase(),
							"SIM Country ISO:" + ret.toLowerCase());
				}
			}
		} catch (Throwable e) {
			// e.printStackTrace();
		}
		if (ret == null || ret.equals("")) {
			ret = String.format("%s || %s", "Local Language:"
					+ Locale.getDefault().getLanguage().toLowerCase(),
					"Local Country:"
							+ Locale.getDefault().getCountry().toLowerCase());
		}
		return "Country = " + ret;
	}

	/**
	 * 获取运营商代码
	 * 
	 * @return
	 */
	private String buildSimOperator(Context context) {
		String ret = "SIM Opeartor = ";
		try {
			TelephonyManager telManager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			if (telManager != null) {
				ret += telManager.getSimOperator();
				ret += "  OpeartorName = " + telManager.getSimOperatorName();
			}
			return ret;
		} catch (Throwable e) {
		}
		return "SIM Opeartor = NULL";
	}
	
	private void startMalil(Context context, String[] receiver,
			String subject, String body, Uri uri1, Uri uri2) {
		// 开启邮箱，发送邮件
		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND_MULTIPLE);
		emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, receiver);

		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);

		emailIntent.putExtra(Intent.EXTRA_TEXT, body);

		ArrayList<Uri> uris = new ArrayList<Uri>();
		if (uri1 != null) {
			// 如果附件1存在
			uris.add(uri1);
		}
		if (uri2 != null) {
			// 如果附件2存在
			uris.add(uri2);
		}
		if (uris != null && uris.size() > 0) {
			emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
		}

		emailIntent.setType("plain/text");
		try {
			context.startActivity(emailIntent);
		} catch (Exception e) {
			Toast.makeText(context, R.string.appgame_error_record_noemail,
					Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}
	
	@Override
	public void onBackPressed() {
		try {
			super.onBackPressed();
		} catch (Exception e) {
			Log.e(LogConstants.HEART_TAG, "onBackPressed err " + e.getMessage());
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
}
