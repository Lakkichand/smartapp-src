package com.jiubang.go.backup.pro.appwidget;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.BatchDeleteRecordsActivity;
import com.jiubang.go.backup.pro.model.BackupManager;

/**
 * @author jiangpeihe
 *
 */
public class ShowDialogActivity extends Activity {
	private Dialog mRecordLimitAlertDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		showRecordLimitAlertDialog(ShowDialogActivity.this);
	}
	private void showRecordLimitAlertDialog(final Context context) {
		if (mRecordLimitAlertDialog == null) {
			mRecordLimitAlertDialog = new AlertDialog.Builder(context)
					.setTitle(R.string.alert_dialog_title)
					.setCancelable(true)
					.setMessage(
							context.getString(R.string.msg_limit_record, BackupManager
									.getInstance().getMaxBackupCount()))
					.setNegativeButton(R.string.cancel, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							finish();
						}
					})
					.setPositiveButton(R.string.btn_limit_record_manage_record,
							new OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									startBatchDeleteRecordsActivity(context);
									dialog.dismiss();
									finish();
								}
							}).create();
		}
		mRecordLimitAlertDialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				finish();

			}
		});
		//		startBatchDeleteRecordsActivity(context);
		showDialog(mRecordLimitAlertDialog);
	}

	public void showDialog(Dialog dialog) {
		if (dialog == null) {
			return;
		}
		if (!dialog.isShowing()) {
			dialog.show();
		}
	}

	private void startBatchDeleteRecordsActivity(Context context) {
		Intent intent = new Intent(context, BatchDeleteRecordsActivity.class);
		//		PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent,
		//				PendingIntent.FLAG_UPDATE_CURRENT);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

}
