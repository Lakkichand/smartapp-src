package com.jiubang.go.backup.pro;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;
import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.data.ResultBean;
import com.jiubang.go.backup.pro.track.ga.TrackerEvent;
import com.jiubang.go.backup.pro.track.ga.TrackerLog;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 结果报告页面 本地备份、恢复、整合以及云端备份、恢复都使用同一个报告页，通过Intent的参数配置展示不同的布局样式
 *
 * @author maiyongshen
 */
public class ReportActivity extends BaseActivity {
	public static final String EXTRA_RESULT = "extra_result";
	public static final String EXTRA_TITLE = "extra_title";
	public static final String EXTRA_DATE = "extra_date";
	public static final String EXTRA_TIP = "extra_tip";
	public static final String EXTRA_POSITIVE_ACTION = "extra_positive_action";
	public static final String EXTRA_NEGATIVE_ACTION = "extra_negative_action";
	public static final String EXTRA_ENABLE_BACK_KEY = "extra_enable_back_key";
	public static final String EXTRA_POSITIVE_BUTTON_TEXT = "extra_positive_btn_text";
	public static final String EXTRA_NEGATIVE_BUTTON_TEXT = "extra_negative_btn_text";
	public static final String EXTRA_SHOULD_REBOOT = "extra_should_reboot";
	public static final String EXTRA_MESSAGES = "extra_messages";

	private static final String DATE_FORMAT = "yyyy-MM-dd  HH:mm:ss";
	private static final int DIALOG_PROGRESS = 0xff000001;

	private String mTitle;
	private Date mDate;
	private String mExtraTip;
	private ParcelableAction mPositiveButtonAction;
	private ParcelableAction mNegativeButtonAction;
	private String mPositiveButtonText;
	private String mNegativeButtonText;
	private Parcelable[] mExtraMessages;
	private boolean mEnableBackKey;
	private boolean mNeedReboot;

	private ProgressDialog mWaitProgressDialog = null;
	private Dialog mRebootDialog = null;
	
	private Tracker mTracker;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mEnableBackKey = true;
		mNeedReboot = false;
		boolean result = true;
		Intent intent = getIntent();
		if (intent != null) {
			result = intent.getBooleanExtra(EXTRA_RESULT, true);
			mTitle = intent.getStringExtra(EXTRA_TITLE);
			mDate = new Date(intent.getLongExtra(EXTRA_DATE, System.currentTimeMillis()));
			mExtraTip = intent.getStringExtra(EXTRA_TIP);
			mExtraMessages = intent.getParcelableArrayExtra(EXTRA_MESSAGES);
			mNeedReboot = intent.getBooleanExtra(EXTRA_SHOULD_REBOOT, mNeedReboot);
			mEnableBackKey = intent.getBooleanExtra(EXTRA_ENABLE_BACK_KEY, mEnableBackKey);
			mPositiveButtonText = intent.getStringExtra(EXTRA_POSITIVE_BUTTON_TEXT);
			mNegativeButtonText = intent.getStringExtra(EXTRA_NEGATIVE_BUTTON_TEXT);
			mPositiveButtonAction = (ParcelableAction) intent
					.getParcelableExtra(EXTRA_POSITIVE_ACTION);
			mNegativeButtonAction = (ParcelableAction) intent
					.getParcelableExtra(EXTRA_NEGATIVE_ACTION);
		}

		if (result) {
			showNormalResult();
		} else {
			showExceptionResult();
		}
	}

	private void showNormalResult() {
		setContentView(R.layout.layout_normal_report);

		TextView title = (TextView) findViewById(R.id.title);
		title.setText(mTitle);

		TextView date = (TextView) findViewById(R.id.date);
		date.setText(new SimpleDateFormat(DATE_FORMAT).format(mDate));

		TextView extraTipView = (TextView) findViewById(R.id.extra_tip);
		if (!TextUtils.isEmpty(mExtraTip)) {
			extraTipView.setVisibility(View.VISIBLE);
			extraTipView.setText(mExtraTip);
		} else {
			extraTipView.setVisibility(View.GONE);
		}

		if (mExtraMessages != null) {
			String message = ((ResultBean) mExtraMessages[0]).title;
			TextView messageView = (TextView) findViewById(R.id.message);
			messageView.setText(message);
		}
		initOperationButtons();
	}

	private void showExceptionResult() {
		setContentView(R.layout.layout_exception_report);

		TextView title = (TextView) findViewById(R.id.title);
		title.setText(mTitle);

		TextView extraTipView = (TextView) findViewById(R.id.extra_tip);
		if (!TextUtils.isEmpty(mExtraTip)) {
			extraTipView.setVisibility(View.VISIBLE);
			extraTipView.setText(mExtraTip);
		} else {
			extraTipView.setVisibility(View.GONE);
		}

		ListView listView = (ListView) findViewById(R.id.details_list);
		listView.setAdapter(new ReportDetailsAdapter());

		initOperationButtons();
	}

	private void initOperationButtons() {
		Button positiveButton = null;
		Button negativeButton = null;
		if (mPositiveButtonAction != null && mNegativeButtonAction != null) {
			findViewById(R.id.single_button).setVisibility(View.GONE);
			positiveButton = (Button) findViewById(R.id.positive_btn);
			negativeButton = (Button) findViewById(R.id.negative_btn);
		} else {
			findViewById(R.id.optional_buttons).setVisibility(View.GONE);
			positiveButton = (Button) findViewById(R.id.single_button);
		}

		if (positiveButton != null) {
			positiveButton.setText(mPositiveButtonText);
			positiveButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// GA统计
					TrackerLog.i("ReportActivity save Button onClick");
					mTracker.trackEvent(TrackerEvent.CATEGORY_UI_ACTION,
							TrackerEvent.ACTION_BUTTON_PRESS, TrackerEvent.REPORT_SAVE_BUTTON,
							TrackerEvent.OPT_CLICK);
					
					if (mPositiveButtonAction != null) {
						showProgressDialog();
						runOnSeperateThread(new Runnable() {
							@Override
							public void run() {
								mPositiveButtonAction.execute();
								dismissProgressDialog();
								executeDefaultAction();
							}
						});
						return;
					}
					executeDefaultAction();
				}
			});
		}

		if (negativeButton != null) {
			negativeButton.setText(mNegativeButtonText);
			negativeButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// GA统计
					TrackerLog.i("ReportActivity discard Button onClick");
					mTracker.trackEvent(TrackerEvent.CATEGORY_UI_ACTION,
							TrackerEvent.ACTION_BUTTON_PRESS, TrackerEvent.REPORT_DISCARD_BUTTON,
							TrackerEvent.OPT_CLICK);
					
					if (mNegativeButtonAction != null) {
						showProgressDialog();
						runOnSeperateThread(new Runnable() {
							@Override
							public void run() {
								mNegativeButtonAction.execute();
								dismissProgressDialog();
								executeDefaultAction();
							}
						});
						return;
					}
					executeDefaultAction();
				}
			});
		}
	}

	@Override
	public void onBackPressed() {
		if (!mEnableBackKey) {
			return;
		}
		if (mNeedReboot) {
			showRebootDialog();
			return;
		}
		super.onBackPressed();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		EasyTracker.getInstance().activityStart(this);
		mTracker = EasyTracker.getTracker();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		
		EasyTracker.getInstance().activityStop(this);
	}

	private void showProgressDialog() {
		if (mWaitProgressDialog == null) {
			mWaitProgressDialog = createSpinnerProgressDialog(true);
			mWaitProgressDialog.setMessage(getString(R.string.msg_wait));
		}
		showDialog(mWaitProgressDialog);
	}

	private void dismissProgressDialog() {
		dismissDialog(mWaitProgressDialog);
	}

	private void showRebootDialog() {
		mRebootDialog = new AlertDialog.Builder(this).setTitle(R.string.attention)
				.setIcon(android.R.drawable.ic_dialog_alert).setMessage(R.string.dialog_msg_reboot)
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				}).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).create();
		showDialog(mRebootDialog);
	}

	private void executeDefaultAction() {
		if (mNeedReboot) {
			Util.reboot();
		}
		finish();
	}

	private void runOnSeperateThread(Runnable runnable) {
		new Thread(runnable).start();
	}

	/**
	 * @author maiyongshen
	 */
	private class ReportDetailsAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		public ReportDetailsAdapter() {
			mInflater = LayoutInflater.from(ReportActivity.this);
		}

		@Override
		public int getCount() {
			return mExtraMessages != null ? mExtraMessages.length : 0;
		}

		@Override
		public Object getItem(int position) {
			return mExtraMessages[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.layout_result_report_item, null);
			}
			ResultBean resultBean = (ResultBean) getItem(position);

			ImageView marker = (ImageView) convertView.findViewById(R.id.marker);
			marker.setImageResource(resultBean.result
					? R.drawable.marker_successful
					: R.drawable.marker_failed);

			TextView title = (TextView) convertView.findViewById(R.id.title);
			title.setText(resultBean.title);

			TextView desc = (TextView) convertView.findViewById(R.id.desc);
			if (TextUtils.isEmpty(resultBean.desc)) {
				desc.setVisibility(View.GONE);
			} else if (!resultBean.result) {
				desc.setVisibility(View.VISIBLE);
			}
			desc.setText(resultBean.desc);

			int height = (int) getResources().getDimension(
					R.dimen.backup_restore_result_list_item_height);
			AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
					ViewGroup.LayoutParams.FILL_PARENT, height);
			convertView.setLayoutParams(lp);

			return convertView;
		}

	}
}
