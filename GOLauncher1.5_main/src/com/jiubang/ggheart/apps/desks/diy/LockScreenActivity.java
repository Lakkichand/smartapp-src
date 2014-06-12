package com.jiubang.ggheart.apps.desks.diy;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.log.LogConstants;
import com.jiubang.ggheart.launcher.ICustomAction;
/**
 * 
 * @author
 *
 */
public class LockScreenActivity extends Activity {
	DevicePolicyManager mDevicepolicymanager;
	private static ComponentName sComponentname;
	private AlertDialog mDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		if (Build.VERSION.SDK_INT > 7) {
			if (sComponentname == null) {
				sComponentname = new ComponentName(this, LockScreenReceiver.class);
			}
			mDevicepolicymanager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
			setLockScreen();
		} else {
			Toast.makeText(this, R.string.lock_screen_func_tips, Toast.LENGTH_SHORT).show();
			finish();
		}
	}

	/**
	 * 尝试锁屏
	 */
	private void setLockScreen() {
		if (Build.VERSION.SDK_INT > 7) {
			if (mDevicepolicymanager == null) {
				finish();
				Toast.makeText(this, R.string.need_to_reboot, Toast.LENGTH_LONG).show();
				return;
			}
			if (mDevicepolicymanager.isAdminActive(sComponentname)) {
				try {
					finish();
					mDevicepolicymanager.lockNow(); // todo nullPoint
					sendGoLockBroadcast();
				} catch (SecurityException e) {
					e.printStackTrace();
					boolean isRemoveSucceeds = false;
					try {
						mDevicepolicymanager.removeActiveAdmin(sComponentname);
						sComponentname = null;
						isRemoveSucceeds = true;
					} catch (SecurityException ex) {
						ex.printStackTrace();
					}
					// 自动解除锁定失败
					if (!isRemoveSucceeds) {
						try {
							Intent intent = new Intent();
							intent.setClassName("com.android.settings",
									"com.android.settings.DeviceAdminSettings");
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							startActivity(intent);
							Toast.makeText(this, R.string.lock_internal_error_new_screen,
									Toast.LENGTH_SHORT).show();
							// 启动界面成功
						} catch (ActivityNotFoundException ex) {
							ex.printStackTrace();
							Toast.makeText(this, R.string.lock_internal_error_remove_componentname,
									Toast.LENGTH_SHORT).show();
						}
					} else { // 自动解除锁定成功
						Toast.makeText(this, R.string.lock_internal_error, Toast.LENGTH_SHORT)
								.show();
					}
				}
			} else {
				try {
					Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
					long pattern[] = { 0, 150 };
					vibrator.vibrate(pattern, -1);
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					Log.e("Vibrator error", " in showCancelDefaultLauncher");
				}
				// View view =
				// getLayoutInflater().inflate(R.layout.lock_warning_dialog,
				// null);
				Builder builder = new Builder(this).setMessage(R.string.lock_screen_dialog_tips)
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setPositiveButton(R.string.ok, new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								Intent i = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
								i.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, sComponentname);
								CharSequence text = getText(R.string.lock_screen_tips);
								i.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, text);
								startActivityForResult(i, 1);
							}
						}).setNegativeButton(R.string.cancel, new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								finish();
							}
						}).setOnCancelListener(new OnCancelListener() {

							@Override
							public void onCancel(DialogInterface dialog) {
								// TODO Auto-generated method stub
								finish();
							}
						});
				mDialog = builder.create();
				mDialog.show();
			}
		} else {
			Toast.makeText(this, R.string.lock_screen_func_tips, Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 给Go锁屏发送消息，现在要锁屏了
	 */
	private void sendGoLockBroadcast() {
		Intent intent = new Intent(ICustomAction.ACTION_LAUNCHER_LOCK);
		sendBroadcast(intent);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		finish();
		if (mDevicepolicymanager.isAdminActive(sComponentname)) {
			try {
				mDevicepolicymanager.lockNow();
				sendGoLockBroadcast();
			} catch (SecurityException e) {
				e.printStackTrace();
				boolean isRemoveSucceeds = false;
				try {
					mDevicepolicymanager.removeActiveAdmin(sComponentname);
					sComponentname = null;
					isRemoveSucceeds = true;
				} catch (SecurityException ex) {
					ex.printStackTrace();
				}
				// 自动解除锁定失败
				if (!isRemoveSucceeds) {
					try {
						Intent intent = new Intent();
						intent.setClassName("com.android.settings",
								"com.android.settings.DeviceAdminSettings");
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(intent);
						Toast.makeText(this, R.string.lock_internal_error_new_screen,
								Toast.LENGTH_SHORT).show();
						// 启动界面成功
					} catch (ActivityNotFoundException ex) {
						ex.printStackTrace();
						Toast.makeText(this, R.string.lock_internal_error_remove_componentname,
								Toast.LENGTH_SHORT).show();
					}
				} else { // 自动解除锁定成功
					Toast.makeText(this, R.string.lock_internal_error, Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	public static ComponentName getComponentname() {
		return sComponentname;
	}

	public static void setComponentname(ComponentName nomponentname) {
		LockScreenActivity.sComponentname = nomponentname;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mDialog != null && mDialog.isShowing()) {
			mDialog.dismiss();
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
