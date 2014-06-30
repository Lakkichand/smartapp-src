package fq.router2;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.net.Uri;
import android.net.VpnService;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.CookieSyncManager;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.smartapp.easyvpn.R;
import com.tapjoy.TapjoyConnect;

import fq.router2.adservice.ADService;
import fq.router2.feedback.HandleAlertIntent;
import fq.router2.feedback.HandleFatalErrorIntent;
import fq.router2.life_cycle.ExitService;
import fq.router2.life_cycle.ExitedIntent;
import fq.router2.life_cycle.ExitingIntent;
import fq.router2.life_cycle.LaunchService;
import fq.router2.life_cycle.LaunchedIntent;
import fq.router2.life_cycle.LaunchingIntent;
import fq.router2.utils.AirplaneModeUtils;
import fq.router2.utils.IOUtils;
import fq.router2.utils.LogUtils;
import fq.router2.utils.Util;

public class MainActivity extends Activity implements LaunchedIntent.Handler,
		ExitedIntent.Handler, HandleFatalErrorIntent.Handler,
		DnsPollutedIntent.Handler, HandleAlertIntent.Handler,
		ExitingIntent.Handler, LaunchingIntent.Handler,
		SocksVpnConnectedIntent.Handler {

	public final static int SHOW_AS_ACTION_IF_ROOM = 1;
	private final static int ASK_VPN_PERMISSION = 1;
	public static boolean isReady;
	private Handler handler = new Handler();
	private static boolean dnsPollutionAcked = false;

	private AdView adView;

	private CheckBox checkBox;

	static {
		IOUtils.createCommonDirs();
	}

	public static final String SHOW_OFFER_KEY = "SHOW_OFFER_KEY";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.main);

		// TODO 修改app ID
		TapjoyConnect.requestTapjoyConnect(this,
				"45115e1c-423e-4b34-9cc6-be048ba4c12f", "GltP4s9SfMs9k33KCEPe");

		PreferenceManager.setDefaultValues(this, R.xml.preferences, true);
		setTitle(getString(R.string.app_name) + " "
				+ LaunchService.getMyVersion(this));
		LaunchedIntent.register(this);
		LaunchingIntent.register(this);
		ExitedIntent.register(this);
		HandleFatalErrorIntent.register(this);
		DnsPollutedIntent.register(this);
		HandleAlertIntent.register(this);
		ExitingIntent.register(this);
		SocksVpnConnectedIntent.register(this);
		CookieSyncManager.createInstance(this);

		checkBox = (CheckBox) findViewById(R.id.checkBox);
		checkBox.setClickable(false);
		if (isReady) {
			onReady();
			checkBox.setChecked(true);
		} else {
			checkBox.setChecked(false);
		}
		findViewById(R.id.switchframe).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (isReady) {
							exit();
						} else {
							findViewById(R.id.progressBar).setVisibility(
									View.VISIBLE);
							checkBox.setVisibility(View.GONE);
							LaunchService.execute(MainActivity.this);
						}
					}
				});

		initSetting();

		// 创建 adView a152afc831ea222
		adView = new AdView(this, AdSize.BANNER, "a152afc831ea222");
		// 查找 LinearLayout，假设其已获得
		LinearLayout layout = (LinearLayout) findViewById(R.id.mainLayout);
		// 在其中添加 adView
		layout.addView(adView, 2);
		// 启动一般性请求并在其中加载广告
		adView.loadAd(new AdRequest());

		SharedPreferences sharedPreferences = getSharedPreferences(
				getPackageName(), Context.MODE_PRIVATE);
		int enterTime = sharedPreferences.getInt("enterTime", 0);
		enterTime++;
		Editor editor = sharedPreferences.edit();
		editor.putInt("enterTime", enterTime);
		editor.commit();

		Intent intent = getIntent();
		int key = intent.getIntExtra(SHOW_OFFER_KEY, 0);
		if (key == 1) {
			TapjoyConnect.getTapjoyConnectInstance().showOffers();
		}

	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		int key = intent.getIntExtra(SHOW_OFFER_KEY, 0);
		if (key == 1) {
			TapjoyConnect.requestTapjoyConnect(this,
					"45115e1c-423e-4b34-9cc6-be048ba4c12f",
					"GltP4s9SfMs9k33KCEPe");
			TapjoyConnect.getTapjoyConnectInstance().showOffers();
		}
	}

	/**
	 * 设置项点击
	 */
	private void initSetting() {
		View dns = findViewById(R.id.dnsframe);
		dns.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Toast.makeText(MainActivity.this,
						getString(R.string.pleasewait), Toast.LENGTH_SHORT)
						.show();
				// 清理DNS污染
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							AirplaneModeUtils.toggle(MainActivity.this);
							showToast(R.string.dns_polluted_alert_toggle_succeed);
						} catch (Exception e) {
							LogUtils.e("failed to toggle airplane mode", e);
							showToast(R.string.dns_polluted_alert_toggle_failed);
						}
					}
				}).start();
			}
		});
		View recommandFrame = findViewById(R.id.recommandframe);
		recommandFrame.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				TapjoyConnect.getTapjoyConnectInstance().showOffers();
			}
		});
		View shareFrame = findViewById(R.id.shareframe);
		shareFrame.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 分享给好友
				String link = "电子市场: https://play.google.com/store/apps/details?id="
						+ getPackageName();
				final String extraText = getString(R.string.sharein) + "  "
						+ getString(R.string.app_name) + "  " + link;
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_SUBJECT,
						getResources().getText(R.string.app_name));
				intent.putExtra(Intent.EXTRA_TEXT, extraText);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(Intent.createChooser(intent, "Share"));
			}
		});

		View reviewFrame = findViewById(R.id.reviewframe);
		reviewFrame.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 评分
				try {
					Uri uri = Uri.parse("market://details?id="
							+ getPackageName());
					Intent it = new Intent(Intent.ACTION_VIEW, uri);
					startActivity(it);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		View feedbackFrame = findViewById(R.id.feedbackframe);
		feedbackFrame.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 意见反馈
				Intent emailIntent = new Intent(
						android.content.Intent.ACTION_SEND);
				emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				String[] receiver = new String[] { "yijiajia1988@gmail.com" };
				emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
						receiver);
				String subject = getString(R.string.app_name) + " Feedback";
				emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
						subject);
				String body = "\n\n";
				body += "\nTotalMemSize="
						+ (Util.getTotalInternalMemorySize() / 1024 / 1024)
						+ "MB";
				body += "\nAndroidVersion=" + android.os.Build.VERSION.RELEASE;
				body += "\nBoard=" + android.os.Build.BOARD;
				body += "\nFreeMemSize="
						+ (Util.getAvailableInternalMemorySize() / 1024 / 1024)
						+ "MB";
				body += "\nRom App Heap Size="
						+ Integer.toString((int) (Runtime.getRuntime()
								.maxMemory() / 1024L / 1024L)) + "MB";
				body += "\nROM=" + android.os.Build.DISPLAY;
				body += "\nKernel=" + Util.getLinuxKernel();
				body += "\nwidthPixels="
						+ getResources().getDisplayMetrics().widthPixels;
				body += "\nheightPixels="
						+ getResources().getDisplayMetrics().heightPixels;
				body += "\nDensity="
						+ getResources().getDisplayMetrics().density;
				body += "\ndensityDpi="
						+ getResources().getDisplayMetrics().densityDpi;
				body += "\nPackageName=" + getPackageName();
				body += "\nProduct=" + android.os.Build.PRODUCT;
				body += "\nPhoneModel=" + android.os.Build.MODEL;
				body += "\nDevice=" + android.os.Build.DEVICE + "\n\n";
				body += getString(R.string.feedbackin);
				emailIntent.putExtra(Intent.EXTRA_TEXT, body);
				emailIntent.setType("plain/text");
				try {
					startActivity(emailIntent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

	}

	@Override
	protected void onPause() {
		super.onPause();
		if (isReady) {
			CookieSyncManager.getInstance().sync();
		}
		TapjoyConnect.getTapjoyConnectInstance().appPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (isReady) {
			CheckDnsPollutionService.execute(this);
		}
		TapjoyConnect.getTapjoyConnectInstance().appResume();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		try {
			if (ASK_VPN_PERMISSION == requestCode) {
				if (resultCode == RESULT_OK) {
					if (LaunchService.SOCKS_VPN_SERVICE_CLASS == null) {
						onHandleFatalError("vpn class not loaded");
					} else {
						updateStatus(_(R.string.status_launch_vpn), 80);
						stopService(new Intent(this,
								LaunchService.SOCKS_VPN_SERVICE_CLASS));
						startService(new Intent(this,
								LaunchService.SOCKS_VPN_SERVICE_CLASS));
					}
				} else {
					onHandleFatalError(_(R.string.status_vpn_rejected));
					LogUtils.e("failed to start vpn service: " + resultCode);
				}
			} else {
				super.onActivityResult(requestCode, resultCode, data);
			}
		} catch (Exception e) {
			LogUtils.e("failed to handle onActivityResult", e);
		}
	}

	public static void displayNotification(Context context, String text) {
		if (!PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
				"NotificationEnabled", true)) {
			clearNotification(context);
			return;
		}
		if (LaunchService.isVpnRunning(context)) {
			clearNotification(context);
			return;
		}
		try {
			Intent openIntent = new Intent(context, MainActivity.class);
			Notification notification = new NotificationCompat.Builder(context)
					.setSmallIcon(R.drawable.ic_launcher)
					.setContentTitle(
							context.getResources().getString(
									R.string.notification_title))
					.setContentText(text)
					.setContentIntent(
							PendingIntent
									.getActivity(context, 0, openIntent, 0))
					.build();
			NotificationManager notificationManager = (NotificationManager) context
					.getSystemService(NOTIFICATION_SERVICE);
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
			notificationManager.notify(1983, notification);
		} catch (Exception e) {
			LogUtils.e("failed to display notification " + text, e);
		}
	}

	public static void clearNotification(Context context) {
		try {
			NotificationManager notificationManager = (NotificationManager) context
					.getSystemService(NOTIFICATION_SERVICE);
			notificationManager.cancel(1983);
		} catch (Exception e) {
			LogUtils.e("failed to clear notification", e);
		}
	}

	public void updateStatus(String status, int progress) {
		LogUtils.i(status);
		TextView textView = (TextView) findViewById(R.id.statusTextView);
		textView.setTextColor(0xFF0000FF);
		textView.setText(status);
		ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
		progressBar.setProgress(progress);
	}

	public void exit() {
		if (LaunchService.isVpnRunning(this)) {
			Toast.makeText(this, R.string.vpn_exit_hint, 5000).show();
			return;
		}
		findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
		checkBox.setVisibility(View.GONE);
		ExitService.execute(this);
		displayNotification(this, _(R.string.status_exiting));
	}

	private String _(int id) {
		return getResources().getString(id);
	}

	@Override
	public void onLaunched(boolean isVpnMode) {
		ActivityCompat.invalidateOptionsMenu(this);
		if (isVpnMode) {
			updateStatus(_(R.string.status_acquire_vpn_permission), 75);
			clearNotification(this);
			if (LaunchService.isVpnRunning(this)) {
				onReady();
			} else {
				startVpn();
			}
		} else {
			onReady();
		}
	}

	public void onReady() {
		isReady = true;
		ActivityCompat.invalidateOptionsMenu(this);
		updateStatus(_(R.string.status_ready), 100);
		displayNotification(this, _(R.string.status_ready));
		findViewById(R.id.progressBar).setVisibility(View.GONE);
		checkBox.setVisibility(View.VISIBLE);
		checkBox.setChecked(true);

		Intent intent = new Intent(MainActivity.this, ADService.class);
		startService(intent);
	}

	@Override
	public void onExited() {
		clearNotification(this);
		// 设置状态
		findViewById(R.id.progressBar).setVisibility(View.GONE);
		checkBox.setVisibility(View.VISIBLE);
		checkBox.setChecked(false);
		updateStatus(_(R.string.proxystatusclose), 0);
		
	}

	@SuppressLint("NewApi")
	private void startVpn() {
		if (LaunchService.isVpnRunning(this)) {
			LogUtils.e("vpn is already running, do not start it again");
			return;
		}
		String[] fds = new File("/proc/self/fd").list();
		if (null == fds) {
			LogUtils.e("failed to list /proc/self/fd");
			onHandleFatalError(_(R.string.status_vpn_rejected));
			return;
		}
		if (fds.length > 500) {
			LogUtils.e("too many fds before start: " + fds.length);
			onHandleFatalError(_(R.string.status_vpn_rejected));
			return;
		}
		Intent intent = VpnService.prepare(MainActivity.this);
		if (intent == null) {
			onActivityResult(ASK_VPN_PERMISSION, RESULT_OK, null);
		} else {
			startActivityForResult(intent, ASK_VPN_PERMISSION);
		}
	}

	@Override
	public void onHandleFatalError(String message) {
		LogUtils.e("fatal error: " + message);
		findViewById(R.id.progressBar).setVisibility(View.GONE);
		checkBox.setVisibility(View.VISIBLE);
		TextView statusTextView = (TextView) findViewById(R.id.statusTextView);
		statusTextView.setTextColor(Color.RED);
		statusTextView.setText(message);
	}

	@Override
	public void onDnsPolluted(final long pollutedAt) {
		if (!dnsPollutionAcked) {
			showDnsPollutedAlert();
		}
	}

	private void showDnsPollutedAlert() {
		new AlertDialog.Builder(MainActivity.this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.dns_polluted_alert_title)
				.setMessage(R.string.dns_polluted_alert_message)
				.setPositiveButton(R.string.dns_polluted_alert_fix,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(
									DialogInterface dialogInterface, int i) {
								dnsPollutionAcked = true;
								new Thread(new Runnable() {
									@Override
									public void run() {
										try {
											AirplaneModeUtils
													.toggle(MainActivity.this);
											showToast(R.string.dns_polluted_alert_toggle_succeed);
										} catch (Exception e) {
											LogUtils.e(
													"failed to toggle airplane mode",
													e);
											showToast(R.string.dns_polluted_alert_toggle_failed);
										}
									}
								}).start();
							}
						})
				.setNegativeButton(R.string.dns_polluted_alert_ack,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(
									DialogInterface dialogInterface, int i) {
								dnsPollutionAcked = true;
							}
						}).show();
	}

	private void showToast(final int message) {
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(MainActivity.this, message, 5000).show();
			}
		}, 0);
	}

	@Override
	public void onExiting() {
		displayNotification(this, _(R.string.status_exiting));
		isReady = false;
		ActivityCompat.invalidateOptionsMenu(this);
		findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
		checkBox.setVisibility(View.GONE);
		findViewById(R.id.statusTextView).setVisibility(View.VISIBLE);
		TextView statusTextView = (TextView) findViewById(R.id.statusTextView);
		statusTextView.setTextColor(0xFF0000FF);
		statusTextView.setText(_(R.string.status_exiting));
	}

	@Override
	public void onDestroy() {
		adView.destroy();
		// TODO 取消注册广播接受器
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		SharedPreferences sharedPreferences = getSharedPreferences(
				getPackageName(), Context.MODE_PRIVATE);
		int enterTime = sharedPreferences.getInt("enterTime", 0);
		if (enterTime < 2 || isReady) {
			super.onBackPressed();
			return;
		}
		boolean b = sharedPreferences.getBoolean("notshowdialog", false);
		if (b) {
			super.onBackPressed();
		} else {
			new AlertDialog.Builder(MainActivity.this)
					.setTitle(getResources().getString(R.string.app_name))
					.setCancelable(true)
					.setMessage(getResources().getString(R.string.exittitlemsg))
					.setPositiveButton(
							getResources().getString(R.string.gonow),
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									SharedPreferences sharedPreferences = getSharedPreferences(
											getPackageName(),
											Context.MODE_PRIVATE);
									Editor editor = sharedPreferences.edit();
									editor.putBoolean("notshowdialog", true);
									editor.commit();

									try {
										Uri uri = Uri
												.parse("market://details?id="
														+ getPackageName());
										Intent it = new Intent(
												Intent.ACTION_VIEW, uri);
										startActivity(it);
									} catch (Exception e) {
										e.printStackTrace();
										finish();
									}
								}
							})
					.setNegativeButton(
							getResources().getString(R.string.nexttime),
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									finish();
								}
							}).show();
		}
	}

}
