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
import android.graphics.Color;
import android.net.Uri;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.smartapp.easyvpn.R;

import fq.router2.feedback.HandleAlertIntent;
import fq.router2.feedback.HandleFatalErrorIntent;
import fq.router2.life_cycle.ExitService;
import fq.router2.life_cycle.ExitedIntent;
import fq.router2.life_cycle.ExitingIntent;
import fq.router2.life_cycle.LaunchService;
import fq.router2.life_cycle.LaunchedIntent;
import fq.router2.life_cycle.LaunchingIntent;
import fq.router2.utils.AirplaneModeUtils;
import fq.router2.utils.ConfigUtils;
import fq.router2.utils.IOUtils;
import fq.router2.utils.LogUtils;

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

	static {
		IOUtils.createCommonDirs();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
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
		Button fullPowerButton = (Button) findViewById(R.id.fullPowerButton);
		CookieSyncManager.createInstance(this);
		fullPowerButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (Build.VERSION.SDK_INT < 14) {
					Uri uri = Uri.parse("http://127.0.0.1:"
							+ ConfigUtils.getHttpManagerPort());
					startActivity(new Intent(Intent.ACTION_VIEW, uri));
				} else {
					showWebView();
				}
			}
		});
		if (isReady) {
			onReady();
			showWebView();
		} else {
			LaunchService.execute(this);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (isReady) {
			WebView webView = (WebView) findViewById(R.id.webView);
			webView.loadUrl("javascript:onPause()");
			CookieSyncManager.getInstance().sync();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (isReady) {
			CheckDnsPollutionService.execute(this);
			WebView webView = (WebView) findViewById(R.id.webView);
			webView.loadUrl("javascript:onResume()");
			showWebView();
		}
	}

	private void loadWebView() {
		if (Build.VERSION.SDK_INT < 14) {
			return;
		}
		WebView webView = (WebView) findViewById(R.id.webView);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setAppCacheEnabled(false);
		webView.loadUrl("http://127.0.0.1:" + ConfigUtils.getHttpManagerPort()
				+ "/home");
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				LogUtils.i("url: " + url);
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
				return true;
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				CookieSyncManager.getInstance().sync();
			}
		});
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
					.addAction(
							android.R.drawable.ic_menu_close_clear_cancel,
							context.getResources()
									.getString(R.string.menu_exit),
							PendingIntent.getService(context, 0, new Intent(
									context, ExitService.class), 0))
					.addAction(
							android.R.drawable.ic_menu_manage,
							context.getResources().getString(
									R.string.menu_status),
							PendingIntent.getActivity(context, 0, new Intent(
									context, MainActivity.class), 0)).build();
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
		textView.setText(status);
		ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
		progressBar.setProgress(progress);
	}

	public void exit() {
		if (LaunchService.isVpnRunning(this)) {
			Toast.makeText(this, R.string.vpn_exit_hint, 5000).show();
			return;
		}
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
		findViewById(R.id.hintTextView).setVisibility(View.VISIBLE);
		findViewById(R.id.fullPowerButton).setVisibility(View.VISIBLE);
		loadWebView();
	}

	@Override
	public void onExited() {
		clearNotification(this);
		finish();
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
		findViewById(R.id.webView).setVisibility(View.GONE);
		findViewById(R.id.progressBar).setVisibility(View.GONE);
		findViewById(R.id.hintTextView).setVisibility(View.GONE);
		findViewById(R.id.fullPowerButton).setVisibility(View.GONE);
		findViewById(R.id.statusTextView).setVisibility(View.VISIBLE);
		TextView statusTextView = (TextView) findViewById(R.id.statusTextView);
		statusTextView.setText(_(R.string.status_exiting));
	}

	private void showWebView() {
		if (Build.VERSION.SDK_INT < 14) {
			return;
		}
		if (isReady) {
			final TextView statusTextView = (TextView) findViewById(R.id.statusTextView);
			statusTextView.setText(R.string.status_loading_page);
			statusTextView.setVisibility(View.VISIBLE);
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					statusTextView.setVisibility(View.GONE);
				}
			}, 2000);
			findViewById(R.id.progressBar).setVisibility(View.GONE);
			findViewById(R.id.hintTextView).setVisibility(View.GONE);
			findViewById(R.id.fullPowerButton).setVisibility(View.GONE);
			findViewById(R.id.webView).setVisibility(View.VISIBLE);
		}
	}

}
