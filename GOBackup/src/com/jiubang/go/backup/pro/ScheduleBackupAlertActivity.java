package com.jiubang.go.backup.pro;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.schedules.BackupPlan;
import com.jiubang.go.backup.pro.schedules.Scheduler;
import com.jiubang.go.backup.pro.statistics.StatisticsKey;
import com.jiubang.go.backup.pro.ui.PayUpdateHelpActivity;

/**
 * 定时备份相关的警告页面
 *
 * @author wencan
 */
public class ScheduleBackupAlertActivity extends BaseActivity {
	public static final String KEY_PLAN = "key_plan";
	public static final String KEY_SIZE_LIMIT = "key_size_limit";
	
	public static final String EXTRA_MESSAGE_TYPE = "extra_message_type";
	public static final int MESSAGE_TYPE_BACKUP_SIZE_LIMIT = 0x2001;
	public static final int MESSAGE_TYPE_CLOUD_BACKUP_COUNTDOWN = 0x2002;

	private static final int REQUEST_GET_PURCHASE = 0x1001;

	private Dialog mFreeUserAlertDialog = null;

	private BackupPlan mBackupPlan;
//	private boolean mIsSizeLimit = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("GOBackup", "ScheduleBackupPurchaseAlertActivity : onCreate()");
		Intent intent = getIntent();
		if (intent == null) {
			return;
		}
		int messageType = intent.getIntExtra(EXTRA_MESSAGE_TYPE, -1);
		if (messageType < 0) {
			return;
		}
		switch (messageType) {
			case MESSAGE_TYPE_BACKUP_SIZE_LIMIT:
				mBackupPlan = intent.getParcelableExtra(KEY_PLAN);
				showFreeUserAlertDialog(mBackupPlan);
				break;
			case MESSAGE_TYPE_CLOUD_BACKUP_COUNTDOWN:
				break;
			default:
				break;
		}
	}

	private void showFreeUserAlertDialog(final BackupPlan backupPlan) {
		if (mFreeUserAlertDialog == null) {
			mFreeUserAlertDialog = new AlertDialog.Builder(this).setTitle(R.string.free_user_limit)
					.setMessage(getString(R.string.purchase_yes_or_no))
					.setNegativeButton(R.string.cancel, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							disableSchedulePlan(backupPlan);
							finish();
						}
					}).setPositiveButton(R.string.ok, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							startPurchaseActivity();
						}
					}).setOnCancelListener(new OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							finish();
						}
					}).create();
			mFreeUserAlertDialog.setCanceledOnTouchOutside(false);
		}
		showDialog(mFreeUserAlertDialog);
	}

	private void disableSchedulePlan(BackupPlan plan) {
		Scheduler.getInstance(this).enablePlan(plan, false);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d("GOBackup", "ScheduleBackupPurchaseAlertActivity : onDestroy()");
	}

	private void startPurchaseActivity() {
		// TODO 进入付费页面
		Intent intent = new Intent(ScheduleBackupAlertActivity.this,
				PayUpdateHelpActivity.class);
		intent.putExtra(PayUpdateHelpActivity.EXTRA_PURCHASE_REQUEST_SOURCE,
				StatisticsKey.PURCHASE_FROM_BACKUP_SIZE_LIMIT);

		startActivityForResult(intent, REQUEST_GET_PURCHASE);
	}

	private void redoBackupPlan() {
		if (mBackupPlan != null) {
			Scheduler scheduler = Scheduler.getInstance(this);
			scheduler.enablePlan(mBackupPlan, true);

			// 发送广播
			Intent it = new Intent();
			it.setAction(Scheduler.ACTION_SCHEDULED_BACKUP);
			it.putExtra(Scheduler.EXTRA_PLAN_DATA, mBackupPlan);
			sendBroadcast(it);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode != REQUEST_GET_PURCHASE) {
			// 如果购买不成功，直接退出
			finish();
			return;
		}
		if (resultCode != RESULT_OK) {
			finish();
			return;
		}

		redoBackupPlan();
		finish();
	}
}