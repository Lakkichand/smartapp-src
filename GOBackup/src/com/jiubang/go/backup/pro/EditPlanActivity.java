package com.jiubang.go.backup.pro;

import java.util.Calendar;
import java.util.Date;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
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
import com.jiubang.go.backup.pro.util.Util;

/**
 * 定时备份任务编辑页面
 *
 * @author maiyongshen
 */
public class EditPlanActivity extends BaseActivity {
	private static final int DAYS_OF_WEEK = 7;
	private static final int ONE_MINUTE_IN_MILLS = 60 * 1000;
	// private Spinner mBackupContentChooser;
	private Button mBackupContentChooser;
	private Button mDatePicker;
	private Button mTimePicker;
	private Spinner mRepeatModeChooser;
	private Spinner mAdvanceRemindTimeChooser;
	private Button mCancelButton;
	// private Button mDeleteButton;
	private Button mSaveButton;

	private DatePickerDialog mDatePickerDialog;
	private TimePickerDialog mTimePickerDialog;
	//	private Dialog mBackupContentChooseDialog;

	private int mYear;
	private int mMonth;
	private int mDay;
	private int mHour;
	private int mMinute;
	private int mDayOfWeek;
	private int mDayOfMonth;

	private Scheduler mPlanScheduler;
	// private BackupPlan mOrginPlan;
	private BackupPlan mPlan;
	private BackupType mBackupType;
	private BackupType mTempBackupType;

	private OnDateSetListener mOnDateSetListener = new OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			mYear = year;
			mMonth = monthOfYear;
			mDay = dayOfMonth;
			Calendar c = Calendar.getInstance();
			c.set(mYear, mMonth, mDay);
			mDayOfWeek = c.get(Calendar.DAY_OF_WEEK);
			mDayOfMonth = c.get(Calendar.DAY_OF_MONTH);
			updateDateText();
			updateRepeatModeChooserAdapter();
		}
	};

	private OnTimeSetListener mOnTimeSetListener = new OnTimeSetListener() {
		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			mHour = hourOfDay;
			mMinute = minute;
			updateTimeText();
		}
	};

	private FileHostingServiceProvider mService;
	private boolean mStartAuthentication = false;
	private ProgressDialog mWaitDialog = null;

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
						Toast.makeText(EditPlanActivity.this, msg.obj.toString(), Toast.LENGTH_LONG)
								.show();
					}
					break;
				default :
					break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initViews();
		init();
	}

	private void init() {
		mPlanScheduler = Scheduler.getInstance(this);
		Intent intent = getIntent();
		BackupPlan plan = null;
		if (intent != null && intent.getExtras() != null) {
			plan = intent.getParcelableExtra(Scheduler.EXTRA_PLAN_DATA);
		}

		mPlan = plan;
		mBackupType = mPlan != null ? mPlan.type : new BackupType(BackupType.BACKUP_TYPE_USER_DATA);
		mTempBackupType = new BackupType(mBackupType.getBackupType());

		if (mPlan != null) {
			updateViewsFromPlan(plan);
		} else {
			updateViewsWithoutPlan();
		}

		// if (mDeleteButton != null) {
		// if (mPlan == null || mPlan.id == -1) {
		// mDeleteButton.setVisibility(View.GONE);
		// } else {
		// mDeleteButton.setVisibility(View.VISIBLE);
		// }
		// }
	}

	private void initViews() {
		setContentView(R.layout.layout_edit_plan);
		getWindow().setFormat(PixelFormat.RGBA_8888);

		TextView title = (TextView) findViewById(R.id.title);
		title.setText(R.string.title_edit_plan);

		View returnButton = findViewById(R.id.return_btn);
		returnButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		mBackupContentChooser = (Button) findViewById(R.id.backup_content);
		if (mBackupContentChooser != null) {
			// ArrayAdapter<CharSequence> adapter =
			// ArrayAdapter.createFromResource(
			// this, R.array.schduled_backup_content,
			// R.layout.layout_simple_spinner_item);
			// adapter.setDropDownViewResource(R.layout.layout_simple_spinner_dropdown_item);
			// mBackupContentChooser.setAdapter(adapter);
			mBackupContentChooser.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mTempBackupType.copy(mBackupType);
					showBackupContentChooseDialog();
				}
			});
		}

		mRepeatModeChooser = (Spinner) findViewById(R.id.repeat_mode);

		mAdvanceRemindTimeChooser = (Spinner) findViewById(R.id.advance_remind);
		if (mAdvanceRemindTimeChooser != null) {
			ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
					R.array.advance_remind_time, R.layout.layout_simple_spinner_item);
			adapter.setDropDownViewResource(R.layout.layout_simple_spinner_dropdown_item);
			mAdvanceRemindTimeChooser.setAdapter(adapter);
		}

		mDatePicker = (Button) findViewById(R.id.date_picker);
		if (mDatePicker != null) {
			mDatePicker.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showDatePickerDialog();
				}
			});
		}

		mTimePicker = (Button) findViewById(R.id.time_picker);
		if (mTimePicker != null) {
			mTimePicker.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showTimePickerDialog();
				}
			});
		}

		mCancelButton = (Button) findViewById(R.id.cancel_btn);
		if (mCancelButton != null) {
			mCancelButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					finish();
				}
			});
		}

		// mDeleteButton = (Button) findViewById(R.id.delete_btn);
		// if (mDeleteButton != null) {
		// mDeleteButton.setOnClickListener(new View.OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// deletePlan();
		// }
		// });
		// }

		mSaveButton = (Button) findViewById(R.id.save_btn);
		if (mSaveButton != null) {
			mSaveButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mPlan = buildPlanFromUi(mPlan);
					if (mPlanScheduler.isTimeConflictedWithOthers(mPlan)) {
						showTimeConflictToast();
						return;
					}

					if (mPlan.type.isCloudBackup()) {
						if (mService == null || !mService.isSessionValid()) {
							showLoginDialog();
							return;
						}
					}

					long planStartTime = savePlan(mPlan);
					if (mPlan.enabled) {
						showPlanSetToast(planStartTime);
					}
					finish();
				}
			});
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		mService = CloudServiceManager.getInstance().getCurrentService();
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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mPlanScheduler != null) {
			mPlanScheduler.release();
		}
	}

	private boolean isPaidUser() {
		return ProductManager.getProductPayInfo(getApplicationContext(), ProductPayInfo.PRODUCT_ID)
				.isAlreadyPaid() || Util.isInland(this) || ProductPayInfo.sIsPaidUserByKey;
	}

	private int getContentArrayId(boolean isPaid) {
		return isPaid ? R.array.schduled_backup_content_paid : R.array.schduled_backup_content_free;
	}

	private boolean[] getCurrentBackupTypeSelectorState(boolean isPaid) {
		boolean[] backupType = null;
		if (isPaid) {
			backupType = new boolean[] { mBackupType.isBackupUserData(),
					mBackupType.isBackupUserApp(), mBackupType.isBackupSystemData() };
		} else {
			backupType = new boolean[] { mBackupType.isBackupUserData(),
					mBackupType.isBackupUserApp() };
		}
		return backupType;
	}

	private void updateRepeatModeChooserAdapter() {
		if (mRepeatModeChooser != null) {
			String[] repeatModes = getResources().getStringArray(R.array.repeat_modes_details);
			String dayOfWeek = getResources().getStringArray(R.array.week)[mDayOfWeek
					% DAYS_OF_WEEK];
			for (int i = RepeatType.WEEKLY.ordinal(); i <= RepeatType.MONTHLY.ordinal(); i++) {
				repeatModes[i] = String.format(repeatModes[i], dayOfWeek, mDayOfMonth);
			}
			ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this,
					R.layout.layout_simple_spinner_item, repeatModes);
			adapter.setDropDownViewResource(R.layout.layout_simple_spinner_dropdown_item);
			mRepeatModeChooser.setAdapter(adapter);
		}
	}

	private void updateViewsWithoutPlan() {
		updateNowDate();
		if (mBackupContentChooser != null) {
			// mBackupContentChooser.setSelection(0);
			mBackupContentChooser.setText(/* getBackupContentText() */BackupPlan
					.getBackupContentText(this, mBackupType));
		}

		if (mRepeatModeChooser != null) {
			updateRepeatModeChooserAdapter();
			mRepeatModeChooser.setSelection(0);
		}

		if (mAdvanceRemindTimeChooser != null) {
			mAdvanceRemindTimeChooser.setSelection(0);
		}
		updateDateText();
		updateTimeText();
	}

	private void updateViewsFromPlan(BackupPlan plan) {
		if (plan == null) {
			return;
		}
		updateDateFromPlan(plan);
		updateDateText();
		updateTimeText();
		updateBackupContentChooserSelection();
		updateRepeatModeChooserAdapter();
		updateRepeatModeChooserSelection(plan);
		updateReminderChooserSelection(plan);
	}

	private void updateDateFromPlan(BackupPlan plan) {
		if (plan == null) {
			return;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(Scheduler.calNextPlanTimeInMillis(plan));
		mYear = calendar.get(Calendar.YEAR);
		mMonth = calendar.get(Calendar.MONTH);
		mDay = calendar.get(Calendar.DAY_OF_MONTH);
		mHour = calendar.get(Calendar.HOUR_OF_DAY);
		mMinute = calendar.get(Calendar.MINUTE);
		mDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
		mDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
	}

	private void updateNowDate() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		mYear = calendar.get(Calendar.YEAR);
		mMonth = calendar.get(Calendar.MONTH);
		mDay = calendar.get(Calendar.DAY_OF_MONTH);
		mHour = calendar.get(Calendar.HOUR_OF_DAY);
		mMinute = calendar.get(Calendar.MINUTE);
		mDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
		mDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
	}

	private Date getSettedDate() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, mYear);
		calendar.set(Calendar.MONTH, mMonth);
		calendar.set(Calendar.DAY_OF_MONTH, mDay);
		calendar.set(Calendar.HOUR_OF_DAY, mHour);
		calendar.set(Calendar.MINUTE, mMinute);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	private void updateDateText() {
		if (mDatePicker != null) {
			String year = Integer.toString(mYear);
			String month = Integer.toString(mMonth + 1);
			String day = Integer.toString(mDay);
			String dayOfWeek = getResources().getStringArray(R.array.week_short)[mDayOfWeek
					% DAYS_OF_WEEK];
			String text = getString(R.string.scheduled_plan_date_format, year, month, day,
					dayOfWeek);
			mDatePicker.setText(text);
		}
	}

	private void updateTimeText() {
		if (mTimePicker != null) {
			String hour = Integer.toString(mHour);
			String minutes = Util.pad(mMinute);
			String text = getString(R.string.scheduled_plan_time_format, hour, minutes);
			mTimePicker.setText(text);
		}
	}

	private void updateBackupContentChooserSelection() {
		if (mBackupContentChooser != null) {
			mBackupContentChooser.setText(/* getBackupContentText() */BackupPlan
					.getBackupContentText(this, mBackupType));
		}
	}

	/*
	 * private String getBackupContentText() { if (mBackupType == null) { return
	 * null; } boolean backupUserData = mBackupType.isBackupUserData(); boolean
	 * backupUserApp = mBackupType.isBackupUserApp(); boolean backupSystemData =
	 * mBackupType.isBackupSystemData(); StringBuilder str = new
	 * StringBuilder(); if (backupUserData) {
	 * str.append(getString(R.string.backup_type_user_data)); } if
	 * (backupUserApp) { if (backupUserData) { str.append("&"); }
	 * str.append(getString(R.string.backup_type_user_app)); } if
	 * (backupSystemData) { if (backupUserData || backupUserApp) {
	 * str.append(","); }
	 * str.append(getString(R.string.backup_type_system_data)); } String result
	 * = getString(R.string.backup_type_content, str.substring(0)); return
	 * result; }
	 */

	private void updateRepeatModeChooserSelection(BackupPlan plan) {
		if (plan == null) {
			return;
		}
		if (mRepeatModeChooser != null) {
			mRepeatModeChooser.setSelection(plan.repeatType.ordinal());
		}
	}

	private void updateReminderChooserSelection(BackupPlan plan) {
		if (plan == null) {
			return;
		}
		int reminderInMinute = plan.reminder / ONE_MINUTE_IN_MILLS;
		int[] reminders = getResources().getIntArray(R.array.advance_reminder_minutes);
		int index = -1;
		for (int i = 0; i < reminders.length; i++) {
			if (reminderInMinute == reminders[i]) {
				index = i;
				break;
			}
		}
		if (mAdvanceRemindTimeChooser != null) {
			mAdvanceRemindTimeChooser.setSelection(index);
		}
	}

	private void deletePlan() {
		new AlertDialog.Builder(this).setTitle(R.string.attention)
				.setMessage(R.string.msg_delete_plan)
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (mPlan.id != -1) {
							mPlanScheduler.deletePlan(mPlan.id);
						}
						finish();
					}
				}).setNegativeButton(R.string.cancel, null).show();
	}

	private long savePlan(BackupPlan plan) {
		if (plan == null) {
			return -1;
		}
		return mPlanScheduler.savePlan(plan);
		// return Scheduler.calNextPlanTimeInMillis(plan);
	}

	private BackupPlan buildPlanFromUi(BackupPlan plan) {
		if (plan == null) {
			plan = new BackupPlan();
		}
		plan.type = /*
					 * BackupType.values()[mBackupContentChooser.
					 * getSelectedItemPosition()];
					 */mBackupType;
		plan.hour = mHour;
		plan.minutes = mMinute;
		plan.repeatType = RepeatType.values()[mRepeatModeChooser.getSelectedItemPosition()];
		plan.enabled = true;
		long timeInMillis = getResources().getIntArray(R.array.advance_reminder_minutes)[mAdvanceRemindTimeChooser
				.getSelectedItemPosition()] * ONE_MINUTE_IN_MILLS;
		plan.reminder = (int) timeInMillis;
		plan.startTime = getSettedDate().getTime();
		if (plan.repeatType != RepeatType.ONE_OFF) {
			plan.dayOfWeek = mDayOfWeek;
			plan.dayOfMonth = mDayOfMonth;
			if (plan.startTime <= System.currentTimeMillis()) {
				plan.startTime = Scheduler.calNextPlanTimeInMillis(plan);
			}
		}
		return plan;
	}

	private void showTimeConflictToast() {
		Toast.makeText(this, R.string.msg_plan_time_conflict, Toast.LENGTH_LONG).show();
	}

	private void showPlanSetToast(long timeInMillis) {
		String toastText = Scheduler.formatRemainingTimeToast(this, timeInMillis);
		Toast.makeText(this, toastText, Toast.LENGTH_LONG).show();
	}

	private void showDatePickerDialog() {
		if (mDatePickerDialog == null) {
			mDatePickerDialog = new DatePickerDialog(this, mOnDateSetListener, mYear, mMonth, mDay);
		}
		mDatePickerDialog.updateDate(mYear, mMonth, mDay);
		showDialog(mDatePickerDialog);
	}

	private void showTimePickerDialog() {
		if (mTimePickerDialog == null) {
			mTimePickerDialog = new TimePickerDialog(this, mOnTimeSetListener, mHour, mMinute, true);
		}
		mTimePickerDialog.updateTime(mHour, mMinute);
		showDialog(mTimePickerDialog);
	}

	private void showBackupContentChooseDialog() {
		final boolean isPaidUser = isPaidUser();
		new AlertDialog.Builder(this)
				.setTitle(R.string.choose_backup_content)
				.setMultiChoiceItems(getContentArrayId(isPaidUser),
						getCurrentBackupTypeSelectorState(isPaidUser),
						new OnMultiChoiceClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which, boolean isChecked) {
								int backupType = -1;
								switch (which) {
								// 用户数据
									case 0 :
										backupType = BackupType.BACKUP_TYPE_USER_DATA;
										break;
									// 用户程序
									case 1 :
										backupType = BackupType.BACKUP_TYPE_USER_APP;
										break;
									// 系统数据
									case 2 :
										backupType = BackupType.BACKUP_TYPE_SYSTEM_DATA;
										break;
									default :
										break;
								}
								if (backupType > 0) {
									if (isChecked) {
										mTempBackupType.enableBackupType(backupType);
									} else {
										mTempBackupType.disableBackupType(backupType);
									}
								}
							}
						}).setPositiveButton(R.string.ok, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (mBackupType.equals(mTempBackupType)) {
							return;
						}
						if (!mTempBackupType.isBackupTypeValid()) {
							return;
						}
						mBackupType.copy(mTempBackupType);
						updateBackupContentChooserSelection();
					}
				}).show();
	}

}
