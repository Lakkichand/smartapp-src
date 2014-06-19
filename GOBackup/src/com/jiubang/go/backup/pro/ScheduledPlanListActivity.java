package com.jiubang.go.backup.pro;

import java.util.Calendar;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.MainActivity.AccountAdapter;
import com.jiubang.go.backup.pro.model.BackupManager.BackupType;
import com.jiubang.go.backup.pro.net.sync.CloudServiceManager;
import com.jiubang.go.backup.pro.net.sync.FileHostingServiceProvider;
import com.jiubang.go.backup.pro.net.sync.FileHostingServiceProvider.ActionListener;
import com.jiubang.go.backup.pro.product.manage.ProductManager;
import com.jiubang.go.backup.pro.product.manage.ProductPayInfo;
import com.jiubang.go.backup.pro.schedules.BackupPlan;
import com.jiubang.go.backup.pro.schedules.BackupPlan.RepeatType;
import com.jiubang.go.backup.pro.schedules.Scheduler;
import com.jiubang.go.backup.pro.ui.PayUpdateHelpActivity;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 定时备份计划任务列表页面
 *
 * @author maiyongshen
 */
public class ScheduledPlanListActivity extends BaseActivity {
	private TextView mNextPlanInfo;
	private ListView mScheduledPlanListView;
	private CursorAdapter mAdapter;
	// private Button mNewPlanButton;
	private ViewGroup mPromptFrame;
	private ViewGroup mSchedulesListFrame;

	private Scheduler mPlanScheduler;
	private Dialog mTimeConflictDialog = null;
	private ProgressDialog mWaitDialog = null;

	private FileHostingServiceProvider mService;
	private boolean mStartAuthentication = false;

	private static final int MSG_DISMISS_DIALOG = 0x1003;
	private static final int MSG_SHOW_TOAST = 0x1005;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_DISMISS_DIALOG :
					dismissDialog((Dialog) msg.obj);
					break;
				case MSG_SHOW_TOAST :
					if (msg.obj instanceof String) {
						Toast.makeText(ScheduledPlanListActivity.this, msg.obj.toString(),
								Toast.LENGTH_LONG).show();
					}
					break;
				default :
					break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Log.d("GoBackup", "ScheduledPlanListActivity onCreate");
		super.onCreate(savedInstanceState);
		initViews();

		mPlanScheduler = Scheduler.getInstance(this);
	}

	private void initViews() {
		setContentView(R.layout.layout_backup_plans_list);
		getWindow().setFormat(PixelFormat.RGBA_8888);

		TextView title = (TextView) findViewById(R.id.title);
		title.setText(R.string.title_scheduled_backup);

		View returnButton = findViewById(R.id.return_btn);
		returnButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		mNextPlanInfo = (TextView) findViewById(R.id.next_plan_info);

		mPromptFrame = (ViewGroup) findViewById(R.id.prompt_frame);
		mSchedulesListFrame = (ViewGroup) findViewById(R.id.schedules_list_frame);

		mScheduledPlanListView = (ListView) findViewById(R.id.plan_list);
		if (mScheduledPlanListView != null) {
			mScheduledPlanListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Cursor cursor = (Cursor) mAdapter.getItem(position);
					BackupPlan plan = new BackupPlan(cursor);
					if (plan.type.isCloudBackup() && !isPaidUser()) {
						startPayHelpAcitivty();
						return;
					}
					Intent intent = new Intent(ScheduledPlanListActivity.this,
							EditPlanActivity.class);
					intent.putExtra(Scheduler.EXTRA_PLAN_DATA, plan);
					startActivity(intent);
				}
			});
		}

		// mNewPlanButton = (Button) findViewById(R.id.btn_new_plan);
		// if (mNewPlanButton != null) {
		// mNewPlanButton.setOnClickListener(new View.OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// Intent intent = new Intent(ScheduledPlanListActivity.this,
		// EditPlanActivity.class);
		// startActivity(intent);
		// }
		// });
		// }
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mService != null && mStartAuthentication) {
			finishFileHostingServiceAuthentication();
		}
	}

	private void finishFileHostingServiceAuthentication() {
		showWaitProgressDialog(R.string.tip_loging_in, false);
		mService.finishAuthentication(this, new ActionListener() {
			@Override
			public void onProgress(long progress, long total, Object data) {
			}

			@Override
			public void onCancel(Object data) {
			}

			@Override
			public void onError(int errCode, String errMessage, Object data) {
				mService = null;
				mStartAuthentication = false;
				Message.obtain(mHandler, MSG_DISMISS_DIALOG, mWaitDialog).sendToTarget();
				Message.obtain(mHandler, MSG_SHOW_TOAST, errMessage).sendToTarget();
			}

			@Override
			public void onComplete(Object data) {
				Message.obtain(mHandler, MSG_DISMISS_DIALOG, mWaitDialog).sendToTarget();
				mStartAuthentication = false;
			}
		});
	}

	private void showLoginDialog() {
		final int[] serviceTypes = getResources().getIntArray(R.array.cloud_service_provider_code);
		new AlertDialog.Builder(this).setTitle(R.string.title_select_cloud_service_provider)
				.setSingleChoiceItems(new AccountAdapter(this, null), -1, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, final int which) {
						mService = CloudServiceManager.getInstance().switchService(
								getApplicationContext(), serviceTypes[which]);
						if (mService != null && !mService.isSessionValid()) {
							mService.startAuthentication(getApplicationContext());
							mStartAuthentication = true;
						}
						dialog.dismiss();
					}
				}).show();
	}

	private void showWaitProgressDialog(int messageResId, boolean cancelable) {
		if (mWaitDialog == null) {
			mWaitDialog = createSpinnerProgressDialog(cancelable);
			String msg = getString(messageResId);
			mWaitDialog.setMessage(msg);
		}
		showDialog(mWaitDialog);
	}

	private void startPayHelpAcitivty() {
		Intent intent = new Intent(this, PayUpdateHelpActivity.class);
		startActivity(intent);
	}

	private void init() {
		mPlanScheduler.disableExpiredPlan();

		List<BackupPlan> allBackupPlans = mPlanScheduler.getAllPlans();
		if (Util.isCollectionEmpty(allBackupPlans)) {
			BackupPlan plan = createDefaultPlan();
			mPlanScheduler.addPlan(plan);
			//			if (!Util.isInland(this)) {
			//				mPlanScheduler.addPlan(createDefaulCloudBackupPlan());
			//			}
		} /*else if (!Util.isInland(this)) {
			int i = 0;
			final int count = allBackupPlans.size();
			for (BackupPlan plan : allBackupPlans) {
				if (plan.type.isCloudBackup()) {
					break;
				}
				i++;
			}
			if (i >= count) {
				mPlanScheduler.addPlan(createDefaulCloudBackupPlan());
			}
			}*/

		Cursor allPlansCursor = mPlanScheduler.getAllScheduledPlansCursor();
		mAdapter = new ScheduledPlanListAdapter(this, allPlansCursor, true);
		if (mAdapter.isEmpty()) {
			showPromptFrame();
		} else {
			if (mScheduledPlanListView != null) {
				mScheduledPlanListView.setAdapter(mAdapter);
			}
			showSchedulesListFrame();
		}
	}

	@Override
	protected void onStart() {
		// Log.d("GoBackup", "ScheduledPlanListActivity onStart");
		super.onStart();
		// ((ScheduledPlanListAdapter) mAdapter).update();
		init();
		updateNextPlanInfo();
		mService = CloudServiceManager.getInstance().getCurrentService();
	}

	@Override
	protected void onStop() {
		super.onStop();
		((ScheduledPlanListAdapter) mAdapter).clear();
		mAdapter = null;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mPlanScheduler.release();
	}

	private void showPromptFrame() {
		if (mPromptFrame != null) {
			mPromptFrame.setVisibility(View.VISIBLE);
		}
		if (mSchedulesListFrame != null) {
			mSchedulesListFrame.setVisibility(View.GONE);
		}
	}

	private void showSchedulesListFrame() {
		if (mSchedulesListFrame != null) {
			mSchedulesListFrame.setVisibility(View.VISIBLE);
		}
		if (mPromptFrame != null) {
			mPromptFrame.setVisibility(View.GONE);
		}
	}

	private void updateNextPlanInfo() {
		if (mNextPlanInfo == null) {
			return;
		}
		BackupPlan nextPlan = mPlanScheduler.getNextBackupPlan();
		if (nextPlan == null) {
			mNextPlanInfo.setText(getString(R.string.msg_no_active_action));
			return;
		}
		mNextPlanInfo.setText(getString(R.string.msg_next_backup_time)
				+ getPlanNextStartDate(nextPlan));
	}

	private String getPlanNextStartDate(BackupPlan plan) {
		if (plan == null) {
			return null;
		}
		long nextStartTimeInMillis = Scheduler.calNextPlanTimeInMillis(plan);
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(nextStartTimeInMillis);
		String year = Integer.toString(calendar.get(Calendar.YEAR));
		String month = Integer.toString(calendar.get(Calendar.MONTH) + 1);
		String day = Integer.toString(calendar.get(Calendar.DAY_OF_MONTH)) + " ";
		String hour = Integer.toString(calendar.get(Calendar.HOUR_OF_DAY));
		String minutes = Util.pad(calendar.get(Calendar.MINUTE));
		String fullDate = getString(R.string.scheduled_plan_full_date_format, year, month, day,
				hour, minutes);
		return fullDate;
	}

	private void showTimeConflictDialog(final BackupPlan plan) {
		if (plan == null) {
			return;
		}
		mTimeConflictDialog = new AlertDialog.Builder(this).setTitle(R.string.attention)
				.setMessage(R.string.msg_plan_time_conflict)
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(ScheduledPlanListActivity.this,
								EditPlanActivity.class);
						intent.putExtra(Scheduler.EXTRA_PLAN_DATA, plan);
						startActivity(intent);
					}
				}).create();
		showDialog(mTimeConflictDialog);
	}

	private void showPlanSetToast(BackupPlan plan) {
		if (plan == null) {
			return;
		}
		String toastText = Scheduler.formatRemainingTimeToast(this,
				Scheduler.calNextPlanTimeInMillis(plan));
		Toast.makeText(this, toastText, Toast.LENGTH_LONG).show();
	}

	private boolean isPaidUser() {
		return ProductManager.getProductPayInfo(getApplicationContext(), ProductPayInfo.PRODUCT_ID)
				.isAlreadyPaid() || ProductPayInfo.sIsPaidUserByKey;
	}

	/*
	 * private String getBackupContentText(BackupPlan plan) { if (plan == null)
	 * { return null; } boolean backupUserData = plan.type.isBackupUserData();
	 * boolean backupUserApp = plan.type.isBackupUserApp(); boolean
	 * backupSystemData = plan.type.isBackupSystemData(); StringBuilder str =
	 * new StringBuilder(); if (backupUserData) {
	 * str.append(getString(R.string.backup_type_user_data)); } if
	 * (backupUserApp) { if (backupUserData) { str.append("&"); }
	 * str.append(getString(R.string.backup_type_user_app)); } if
	 * (backupSystemData) { if (backupUserData || backupUserApp) {
	 * str.append(","); }
	 * str.append(getString(R.string.backup_type_system_data)); } String result
	 * = getString(R.string.backup_type_content, str.substring(0)); return
	 * result; }
	 */

	/**
	 * @author maiyongshen
	 */
	private class ScheduledPlanListAdapter extends CursorAdapter {
		private LayoutInflater mInflater;

		public ScheduledPlanListAdapter(Context context, Cursor c, boolean autoRequery) {
			super(context, c, autoRequery);
			mInflater = LayoutInflater.from(context);
		}

		// public void update() {
		// onContentChanged();
		// notifyDataSetChanged();
		// }

		public void clear() {
			changeCursor(null);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return mInflater.inflate(R.layout.layout_plan_info, null);
		}

		@Override
		public void bindView(View view, Context context, final Cursor cursor) {
			final BackupPlan plan = new BackupPlan(cursor);
			TextView title = (TextView) view.findViewById(R.id.title);
			if (title != null) {
				// TODO
				String content = BackupPlan.getBackupContentText(context, plan.type);
				title.setText(content);
			}

			TextView summary = (TextView) view.findViewById(R.id.summary);
			if (summary != null) {
				summary.setText(getFormatedPlanSummary(plan));
			}

			final boolean isPaidUser = isPaidUser();

			CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
			if (checkBox != null) {
				checkBox.setVisibility(!plan.type.isCloudBackup() || isPaidUser
						? View.VISIBLE
						: View.INVISIBLE);
				boolean enabled = cursor.getInt(BackupPlan.Columns.COLUMN_INDEX_ENABLED) == 1;
				checkBox.setOnCheckedChangeListener(null);
				checkBox.setChecked(enabled);
				checkBox.setTag(plan);
				checkBox.setOnCheckedChangeListener(mPlanEnableChangeListener);
			}

			ImageView payMarkerView = (ImageView) view.findViewById(R.id.pay_marker);
			if (payMarkerView != null) {
				payMarkerView.setVisibility(!plan.type.isCloudBackup() || isPaidUser
						? View.GONE
						: View.VISIBLE);
			}
		}

		private String getFormatedPlanSummary(BackupPlan plan) {
			if (plan == null) {
				return null;
			}
			String date = getPlanNextStartDate(plan);
			String[] repeatModes = getResources().getStringArray(R.array.repeat_modes);
			String frequence = repeatModes[plan.repeatType.ordinal()];
			return date + " , " + frequence;
		}

		private OnCheckedChangeListener mPlanEnableChangeListener = new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				BackupPlan plan = (BackupPlan) buttonView.getTag();
				if (plan == null) {
					return;
				}
				if (isChecked) {
					if (mPlanScheduler.isTimeConflictedWithOthers(plan)) {
						showTimeConflictDialog(plan);
						return;
					}
					if (mPlanScheduler.isPlanExpired(plan)) {
						buttonView.setOnCheckedChangeListener(null);
						buttonView.setChecked(false);
						buttonView.setOnCheckedChangeListener(this);
						return;
					}

					if (plan.type.isCloudBackup()) {
						if (mService == null || !mService.isSessionValid()) {
							buttonView.setOnCheckedChangeListener(null);
							buttonView.setChecked(false);
							buttonView.setOnCheckedChangeListener(this);
							showLoginDialog();
							return;
						}
					}
				}
				mPlanScheduler.enablePlan(plan, isChecked);
				if (isChecked) {
					showPlanSetToast(plan);
				}
				updateNextPlanInfo();
			}
		};

	}

	private BackupPlan createDefaultPlan() {
		final int defaultHour = 14;
		final int defaultMinute = 0;
		BackupPlan plan = new BackupPlan();
		plan.dayOfWeek = Calendar.MONDAY;
		plan.type = new BackupType(BackupType.BACKUP_TYPE_USER_DATA);
		plan.enabled = false;
		plan.hour = defaultHour;
		plan.minutes = defaultMinute;
		plan.repeatType = RepeatType.WEEKLY;
		return plan;
	}

	private BackupPlan createDefaulCloudBackupPlan() {
		final int defaultHour = 14;
		final int defaultMinute = 0;
		BackupPlan plan = new BackupPlan();
		plan.dayOfWeek = Calendar.TUESDAY;
		plan.type = new BackupType(BackupType.BACKUP_TYPE_USER_DATA).enableCloudBackup();
		plan.enabled = false;
		plan.hour = defaultHour;
		plan.minutes = defaultMinute;
		plan.repeatType = RepeatType.WEEKLY;
		return plan;
	}
}
