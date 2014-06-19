package com.jiubang.go.backup.pro;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.data.IRecord;
import com.jiubang.go.backup.pro.data.RestorableRecord;
import com.jiubang.go.backup.pro.model.BackupManager;
import com.jiubang.go.backup.pro.product.manage.ProductManager;
import com.jiubang.go.backup.pro.product.manage.ProductPayInfo;
import com.jiubang.go.backup.pro.schedules.AsyncHandler;
import com.jiubang.go.backup.pro.ui.PayUpdateHelpActivity;
import com.jiubang.go.backup.pro.ui.RecordListAdapter;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 备份列表记录展示页面
 *
 * @author maiyongshen
 */

public class RecordsListActivity extends BaseActivity {
	private static final int REQUEST_CODE_TUTORIAL = 0x3001;

	//	private static final String RECORD_DATA_FORMAT = "%tm-%td %tH:%tM";
	//	 private ViewGroup mTopFrame;
	//	 private ListView mBackupedRecordListView;
	//	 private BaseAdapter mAdapter;
	//	 private ViewGroup mPromptFrame;
	//	 private ImageView mPromptDrawable;
	//	 private TextView mPromptDesc;

	private ImageButton mDeleteButton;
	private ImageButton mSmartMergeButton;
	private ListView mRecordListView;
	private BaseAdapter mAdapter;

	private ProgressDialog mWaitProgressDialog = null;
	private Dialog mDeleteRecordAlertDialog = null;
	private Dialog mMergeRecordsAlertDialog = null;
	private Dialog mFreeUserAlertDialog = null;
	private Dialog mExportDialog = null;

	private long mToDeleteRecordId;
	private boolean mShouldEnableSmartMergeButton;

	private PreferenceManager mPreferenceManager;
	private BackupManager mBackupManager;

	private List<IRecord> mRecords = null;

	private Runnable mSmartMergeTutorialRunable = null;

	private boolean mShouldBackWhenTutorial = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// Log.d("GoBackup", "MainActivity onCreate");
		super.onCreate(savedInstanceState);
		getWindow().setFormat(PixelFormat.RGBA_8888);
		// initViews();

		mPreferenceManager = PreferenceManager.getInstance();
		mBackupManager = BackupManager.getInstance();
	}

	@Override
	protected void onStart() {
		super.onStart();
		init();
	}

	/*
	 * private void initViews() {
	 * setContentView(R.layout.layout_backuped_records_list); initTitleBar();
	 * mTopFrame = (ViewGroup) findViewById(R.id.main); mPromptFrame =
	 * (ViewGroup) findViewById(R.id.prompt); mPromptDrawable = (ImageView)
	 * findViewById(R.id.prompt_drawable); mPromptDesc = (TextView)
	 * findViewById(R.id.prompt_desc); mBackupedRecordListView = (ListView)
	 * findViewById(R.id.listview);
	 * mBackupedRecordListView.setOnItemClickListener(new
	 * AdapterView.OnItemClickListener() {
	 *
	 * @Override public void onItemClick(AdapterView<?> parent, View view, int
	 * position, long id) { final Adapter adapter = parent.getAdapter(); if
	 * (adapter != null) { startRestoreAcvitiy(adapter.getItemId(position)); } }
	 * }); registerForContextMenu(mBackupedRecordListView); mAdapter = new
	 * BackupedRecordListAdapter(this); if (mBackupedRecordListView != null) {
	 * mBackupedRecordListView.setAdapter(mAdapter); if (mBackupedRecordListView
	 * instanceof PinnedHeaderListView) { ((PinnedHeaderListView)
	 * mBackupedRecordListView)
	 * .setPinnedHeaderView(((BackupedRecordListAdapter) mAdapter)
	 * .newHeaderView(this, mBackupedRecordListView, 0)); } } }
	 */

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mSmartMergeButton != null) {
			mSmartMergeButton.removeCallbacks(mSmartMergeTutorialRunable);
		}
	}

	private void initTitleBar() {
		View returnButton = findViewById(R.id.return_btn);
		returnButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				exit();
			}
		});

		TextView title = (TextView) findViewById(R.id.title);
		title.setText(R.string.title_restore);

		mSmartMergeButton = (ImageButton) findViewById(R.id.smart_merge_btn);
		mSmartMergeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				handleSmartMergeButtonClick();
			}
		});

		mDeleteButton = (ImageButton) findViewById(R.id.delete_btn);
		if (mDeleteButton != null) {
			mDeleteButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					startBatchDeleteRecordsActivity();
				}
			});
		}

		checkNeedShowMergeTutorial();
	}

	private void onNoBackupRecords() {
		setContentView(R.layout.empty_record_list);
		initTitleBar();
	}

	private void onRecordsLoaded() {
		setContentView(R.layout.record_list_activity);
		initTitleBar();

		mRecordListView = (ListView) findViewById(R.id.record_list);
		mRecordListView.setAdapter(mAdapter);

		registerForContextMenu(mRecordListView);
		mRecordListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final Adapter adapter = parent.getAdapter();
				if (adapter != null) {
					startRestoreAcvitiy(adapter.getItemId(position));
				}
			}
		});
	}

	private void init() {
		if (!checkReady()) {
			onSdCardUnmounted();
			return;
		}

		List<IRecord> allRecords = new ArrayList<IRecord>();
		IRecord mergedRecord = getMergedRecord();
		if (mergedRecord != null) {
			allRecords.add(mergedRecord);
		}

		IRecord scheduleRecord = getScheduleRecord();
		if (scheduleRecord != null) {
			allRecords.add(scheduleRecord);
		}

		mShouldEnableSmartMergeButton = false;
		List<IRecord> normalRecords = mBackupManager.getAllRestoreRecords();
		int normalSize = mBackupManager.getAllRestoreRecordsCount();
		final List<IRecord> scheduleRecords = mBackupManager.getScheduleRecords();
		int shedulesSize = Util.isCollectionEmpty(scheduleRecords) ? 0 : scheduleRecords.size();
		final List<IRecord> mergeRecords = mBackupManager.getMergableRecords();
		int mergereSize = Util.isCollectionEmpty(mergeRecords) ? 0 : mergeRecords.size();
		int totalSize = normalSize + shedulesSize + mergereSize;
		if (!Util.isCollectionEmpty(normalRecords)) {
			allRecords.addAll(normalRecords);
		}
		if (totalSize >= 2) {
			// 更新智能合并button按钮
			mShouldEnableSmartMergeButton = true;
		}

		mAdapter = new RecordListAdapter(this, allRecords);
		if (!mAdapter.isEmpty()) {
			onRecordsLoaded();
		} else {
			onNoBackupRecords();
		}
	}

	private void checkNeedShowMergeTutorial() {
		if (!mPreferenceManager.getBoolean(this, PreferenceManager.KEY_TUTORIAL_MERGE_SHOW, true)) {
			mShouldBackWhenTutorial = true;
			return;
		}

		if (mSmartMergeTutorialRunable == null) {
			mSmartMergeTutorialRunable = new Runnable() {
				@Override
				public void run() {
					if (isStopped() || isFinishing()) {
						return;
					}
					int[] point = new int[2];
					if (mSmartMergeButton == null) {
						mSmartMergeButton = (ImageButton) findViewById(R.id.smart_merge_btn);
					}
					mSmartMergeButton.getLocationOnScreen(point);
					Rect rect = new Rect(point[0], point[1], point[0]
							+ mSmartMergeButton.getWidth(), point[1]
							+ mSmartMergeButton.getHeight());

					Intent intent = new Intent(RecordsListActivity.this, TutorialActivity.class);
					intent.putExtra(TutorialActivity.KEY_LAYOUT_ID, R.layout.layout_tutorial_merge);
					intent.putExtra(TutorialActivity.KEY_SPOTLIGHT_RECT, rect);
					startActivityForResult(intent, REQUEST_CODE_TUTORIAL);
				}
			};
		}

		//		final int showTutorialPageDelay = 800;
		//		mSmartMergeButton.postDelayed(mSmartMergeTutorialRunable, showTutorialPageDelay);
		mSmartMergeButton.post(mSmartMergeTutorialRunable);
	}

	private IRecord getMergedRecord() {
		List<IRecord> mergableRecords = mBackupManager.getMergableRecords();
		if (Util.isCollectionEmpty(mergableRecords)) {
			return null;
		}
		final String preferSdCardPath = Util.getSdRootPathOnPreference(this);
		for (IRecord record : mergableRecords) {
			String recordDir = ((RestorableRecord) record).getRecordRootDir();
			if (recordDir.startsWith(preferSdCardPath)) {
				return record;
			}
		}
		return mergableRecords.get(0);
	}

	private IRecord getScheduleRecord() {
		List<IRecord> scheduleRecords = mBackupManager.getScheduleRecords();
		if (Util.isCollectionEmpty(scheduleRecords)) {
			return null;
		}
		final String preferSdCardPath = Util.getSdRootPathOnPreference(this);
		for (IRecord record : scheduleRecords) {
			String recordDir = ((RestorableRecord) record).getRecordRootDir();
			if (recordDir.startsWith(preferSdCardPath)) {
				return record;
			}
		}
		return scheduleRecords.get(0);
	}

	/*
	 * private void initRecordView(View recordView, IRecord record) { if
	 * (recordView == null || recordView.getVisibility() != View.VISIBLE ||
	 * record == null) { return; } TextView title = (TextView)
	 * recordView.findViewById(R.id.title); if (record == mMergedRecord) {
	 * title.setText(R.string.merged_backup);
	 * recordView.setBackgroundResource(R.drawable.merged_record_item_bg); }
	 * else if (record == mScheduledRecord) {
	 * title.setText(R.string.schedule_backup);
	 * recordView.setBackgroundResource(R.drawable.schedule_record_bg); }
	 * TextView recordSize = (TextView) recordView.findViewById(R.id.size_info);
	 * String size = Util.formatFileSize(record.getSpaceUsage());
	 * recordSize.setText(getString(R.string.parenthesized_msg, size)); TextView
	 * recordDate = (TextView) recordView.findViewById(R.id.time_info); Date
	 * date = record.getDate();
	 * recordDate.setText(String.format(RECORD_DATA_FORMAT, date, date, date,
	 * date)); StretchRecordItemView recordDetail = (StretchRecordItemView)
	 * recordView.findViewById(R.id.record_content_info);
	 * RecordListAdapter.bindRecordDetailView(recordDetail, record);
	 * recordView.findViewById(R.id.checkbox).setVisibility(View.GONE); }
	 */

	/*
	 * private Runnable mBackupPrompt = new Runnable() {
	 *
	 * @Override public void run() { final PreferenceManager mPreferenceManager
	 * = PreferenceManager.getInstance(); final int screenWidth =
	 * getWindowManager().getDefaultDisplay().getWidth(); final View anchor =
	 * mNewBackupButton; if (anchor != null) { PopupWindow window =
	 * initPopupWindow(getString(R.string.prompt_tap_to_backup),
	 * (int)(screenWidth * 0.65), 120, false); if (window != null) {
	 * window.setOnDismissListener(new PopupWindow.OnDismissListener() {
	 *
	 * @Override public void onDismiss() {
	 * mPreferenceManager.putBoolean(MainActivity.this,PreferenceManager
	 * .KEY_HAS_SHOWN_BACKUP_PROMPT, true); } }); int xOffset =
	 * (anchor.getWidth() - window.getWidth()) / 2;
	 * window.showAsDropDown(anchor, xOffset, 10);
	 * mPreferenceManager.putBoolean(MainActivity.this
	 * ,PreferenceManager.KEY_HAS_SHOWN_BACKUP_PROMPT, true); } }
	 * mTopFrame.removeCallbacks(this); } };
	 */

	/*
	 * private Runnable mRestorePrompt = new Runnable() {
	 *
	 * @Override public void run() { final PreferenceManager mPreferenceManager
	 * = PreferenceManager.getInstance(); final int screenWidth =
	 * getWindowManager().getDefaultDisplay().getWidth(); final View anchor =
	 * mBackupedRecordListView.getChildAt(0); if (anchor != null) { PopupWindow
	 * window = initPopupWindow(getString(R.string.prompt_tap_to_restore),
	 * (int)(screenWidth * 0.82), 120, true); if (window != null) {
	 * window.setOnDismissListener(new PopupWindow.OnDismissListener() {
	 *
	 * @Override public void onDismiss() {
	 * mPreferenceManager.putBoolean(MainActivity.this,PreferenceManager
	 * .KEY_HAS_SHOWN_RESTORE_PROMPT, true); } }); int xOffset =
	 * (anchor.getWidth() - window.getWidth()) / 2;
	 * window.showAsDropDown(anchor, xOffset, 10);
	 * mPreferenceManager.putBoolean(RecordsListActivity
	 * .this,PreferenceManager.KEY_HAS_SHOWN_RESTORE_PROMPT, true); } }
	 * mTopFrame.removeCallbacks(this); } };
	 */

	/*
	 * private void initPrompt() { final Adapter adapter =
	 * mBackupedRecordListView.getAdapter(); final PreferenceManager
	 * mPreferenceManager = PreferenceManager.getInstance(); if (adapter == null
	 * || adapter.isEmpty()) { boolean hasShownBackupPrompt =
	 * mPreferenceManager.getBoolean(this,
	 * PreferenceManager.KEY_HAS_SHOWN_BACKUP_PROMPT, false); if
	 * (!hasShownBackupPrompt) { // 在最上层的view post
	 * runnable，在View显示出来之后将PopupWindow显示出来， //
	 * 不可在Activity的生命周期中让PopupWindow显示，此时窗口仍未可见 if (mTopFrame != null) {
	 * mTopFrame.post(mBackupPrompt); } } } else { if (adapter.getCount() > 1){
	 * mPreferenceManager.putBoolean(this,
	 * PreferenceManager.KEY_HAS_SHOWN_BACKUP_PROMPT, true);
	 * mPreferenceManager.putBoolean(this,
	 * PreferenceManager.KEY_HAS_SHOWN_RESTORE_PROMPT, true); } else if
	 * (adapter.getCount() == 1) { boolean hasShownRestorePrompt =
	 * mPreferenceManager.getBoolean(this,
	 * PreferenceManager.KEY_HAS_SHOWN_RESTORE_PROMPT, false); if
	 * (!hasShownRestorePrompt && mTopFrame != null) {
	 * mTopFrame.post(mRestorePrompt); } } } }
	 */

	// 检查存储卡是否已挂载
	private boolean checkReady() {
		return Util.checkSdCardReady(this);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle(R.string.dialog_title_operation);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.backuped_records_list_context_menu, menu);
		if (menuInfo instanceof AdapterContextMenuInfo) {
			long recordId = ((AdapterContextMenuInfo) menuInfo).id;
			if (recordId <= 0) {
				return;
			}
			final RestorableRecord record = mBackupManager.getRecordById(recordId);
			if (record != null) {
				if (!record.hasContactsEntry()) {
					menu.removeItem(R.id.operation_export_contacts);
				}
				if (!record.hasSmsEntry()) {
					menu.removeItem(R.id.operation_export_sms);
				}
			}
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
//			case R.id.operation_restore :
//				startRestoreAcvitiy(menuInfo.id);
//				return true;
			case R.id.operation_export_contacts :
			case R.id.operation_export_sms :
				showExportDialog(menuInfo.id, item.getItemId());
				return true;
			case R.id.operation_delete :
				mToDeleteRecordId = menuInfo.id;
				showDeleteRecordAlertDialog();
				return true;
			default :
				return super.onContextItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_TUTORIAL) {
			mPreferenceManager.putBoolean(this, PreferenceManager.KEY_TUTORIAL_MERGE_SHOW, false);

			if (data != null) {
				if (data.getBooleanExtra(TutorialActivity.KEY_SPOTLIGHTE_CLICK, false)) {
					handleSmartMergeButtonClick();
				}
			}
			mShouldBackWhenTutorial = true;
		}
	}

	@Override
	public void onBackPressed() {
		if (!mShouldBackWhenTutorial) {
			return;
		}
		super.onBackPressed();

	}

	private void executeExport(final long recordId, final int operationId) {
		showWaitProgressDialog(R.string.msg_wait, false);
		new Thread() {
			@Override
			public void run() {
				if (executeExportInternal(recordId, operationId)) {
					Message.obtain(mHandler, MSG_SHOW_TOAST, R.string.msg_export_successfully, -1)
							.sendToTarget();
				} else {
					Message.obtain(mHandler, MSG_SHOW_TOAST, R.string.msg_export_failed, -1)
							.sendToTarget();
				}
				Message.obtain(mHandler, MSG_DISMISS_DIALOG, mWaitProgressDialog).sendToTarget();
			}
		}.start();
	}

	private boolean executeExportInternal(final long recordId, final int operationId) {
		if (recordId > 0) {
			RestorableRecord record = mBackupManager.getRecordById(recordId);
			if (record == null) {
				return false;
			}
			if (operationId == R.id.operation_export_contacts) {
				return record.exportContactsFile(this, getExportPath(record, operationId));
			} else if (operationId == R.id.operation_export_sms) {
				return record.exportSmsFile(this, getExportPath(record, operationId));
			}
		}
		return false;
	}

	private String getExportPath(RestorableRecord record, int operationId) {
		String destFileName = null;
		if (record != null) {
			final String recordDate = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(record
					.getDate());
			if (operationId == R.id.operation_export_contacts) {
				destFileName = "contacts_gobackup_" + recordDate + ".vcf";
			} else if (operationId == R.id.operation_export_sms) {
				destFileName = "messages_gobackup_" + recordDate + ".xml";
			}
		}
		return destFileName;
	}

	private void showExportDialog(final long recordId, final int operationId) {
		String msg = null;
		RestorableRecord record = null;
		record = mBackupManager.getRecordById(recordId);
		if (record != null) {
			String exportPath = getExportPath(record, operationId);
			if (operationId == R.id.operation_export_contacts) {
				msg = getString(R.string.msg_export_contacts) + "\"" + "sdcard/" + exportPath
						+ "\"";
			} else if (operationId == R.id.operation_export_sms) {
				msg = getString(R.string.msg_export_sms) + "\"" + "sdcard/" + exportPath + "\"";
			}
		}

		mExportDialog = new AlertDialog.Builder(this).setTitle(R.string.title_confirm_export)
				.setMessage(msg)
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						executeExport(recordId, operationId);
						dialog.dismiss();
					}
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).create();
		showDialog(mExportDialog);
	}

	private void deleteRecord() {
		showWaitProgressDialog(R.string.msg_wait, false);
		AsyncHandler.post(new Runnable() {
			@Override
			public void run() {
				if (mToDeleteRecordId != -1) {
					deleteRecordInternal(mToDeleteRecordId);
					mToDeleteRecordId = -1;
				}
				Message.obtain(mHandler, MSG_DISMISS_DIALOG, mWaitProgressDialog).sendToTarget();
			}
		});
	}

	private boolean deleteRecordInternal(long recordId) {
		if (mAdapter == null || mAdapter.isEmpty()) {
			return false;
		}
		((RecordListAdapter) mAdapter).remove(recordId);
		mBackupManager.deleteRecordById(recordId);
		mHandler.sendEmptyMessage(MSG_LIST_DATASET_CHANGED);
		mHandler.sendEmptyMessage(MSG_UPDATE_VIEWS);
		return true;
	}

	private void exit() {
		finish();
	}

	private void startRestoreAcvitiy(long recordId) {
		Intent intent = new Intent(RecordsListActivity.this, RestoreBackupActivity.class);
		intent.putExtra(RestoreBackupActivity.RECORD_ID, recordId);
		startActivity(intent);
	}

	private boolean hasRecords() {
		return mAdapter != null && !mAdapter.isEmpty();
	}

	private void startBatchDeleteRecordsActivity() {
		if (!hasRecords()) {
			showToast(getString(R.string.msg_no_record_to_delete));
			return;
		}
		Intent intent = new Intent(this, BatchDeleteRecordsActivity.class);
		startActivity(intent);
	}

	private AlertDialog createAlertDialog(int iconId, int titleResId, int msgResId) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		if (iconId > 0) {
			builder.setIcon(iconId);
		}
		if (titleResId > 0) {
			builder.setTitle(titleResId);
		}
		if (msgResId > 0) {
			builder.setMessage(msgResId);
		}
		return builder.create();
	}

	private Dialog createDeleteRecordAlertDialog() {
		return new AlertDialog.Builder(this).setIcon(android.R.drawable.stat_notify_error)
				.setTitle(R.string.attention).setMessage(R.string.msg_delete_record)
				.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mHandler.sendEmptyMessage(MSG_DELETE_RECORD);
						dialog.dismiss();
					}
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).create();
	}

	private Dialog createMergeAlertDialog() {
		return new AlertDialog.Builder(this).setTitle(R.string.alert_dialog_title)
				.setMessage(R.string.msg_start_smart_merge)
				.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (!isMeetConditionToMerge()) {
							showFreeUserAlertDialog();
						} else {
							//							startMergeActivity();
							//跳到整合备份页
							startShowRecordsActivity();
						}
						dialog.dismiss();
					}
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).create();
	}

	private Dialog createFreeUserLimitAlertDialog() {
		return new AlertDialog.Builder(this).setTitle(R.string.purchase_title)
				.setMessage(R.string.too_large_need_pay)
				.setNegativeButton(R.string.cancel, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).setPositiveButton(R.string.ok, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						startPayHelpActivity();
						dialog.dismiss();
					}
				}).create();
	}

	private void startPayHelpActivity() {
		Intent intent = new Intent(this, PayUpdateHelpActivity.class);
		startActivity(intent);
		mPreferenceManager.putBoolean(this, PreferenceManager.KEY_HAS_SHOWN_PAY_HELP_PAGE, true);
	}

	/*
	 * private PopupWindow initPopupWindow(String msg, int width, int height,
	 * boolean arrowUp) { if (msg == null || msg.equals("") || width <= 0 ||
	 * height <= 0) { return null; } final LayoutInflater inflater =
	 * LayoutInflater.from(this); View contentView =
	 * inflater.inflate(R.layout.layout_popup_text_promp, null); ((TextView)
	 * contentView.findViewById(R.id.message)).setText(msg); final PopupWindow
	 * popupWindow = new PopupWindow(contentView,
	 * ViewGroup.LayoutParams.WRAP_CONTENT,
	 * ViewGroup.LayoutParams.WRAP_CONTENT); final PopupWindow popupWindow = new
	 * PopupWindow(contentView, width, height);
	 * popupWindow.setOutsideTouchable(true); // 让PopupWindow可接受到窗口外部的触屏事件 if
	 * (!arrowUp) {
	 * popupWindow.setBackgroundDrawable(getResources().getDrawable(
	 * R.drawable.popup_prompt_arrow_down_bg)); } else {
	 * popupWindow.setBackgroundDrawable
	 * (getResources().getDrawable(R.drawable.popup_prompt_arrow_up_bg)); }
	 * popupWindow.setTouchInterceptor(new View.OnTouchListener() {
	 *
	 * @Override public boolean onTouch(View v, MotionEvent event) { if
	 * (popupWindow.isShowing()) { popupWindow.dismiss(); } return true; } });
	 * return popupWindow; }
	 */

	private void updateViews() {
		if (hasRecords()) {
			onRecordsLoaded();
		} else {
			onNoBackupRecords();
		}
	}

	/*
	 * private void showListFrame() { if (mPromptFrame != null) {
	 * mPromptFrame.setVisibility(View.INVISIBLE); } if (mBackupedRecordListView
	 * != null) { mBackupedRecordListView.setVisibility(View.VISIBLE); } }
	 * private void showPromptFrame() { if (mPromptFrame != null) {
	 * mPromptFrame.setVisibility(View.VISIBLE); } if (mBackupedRecordListView
	 * != null) { mBackupedRecordListView.setVisibility(View.GONE); } } private
	 * void onRecrodsListEmptied() { if (mPromptDrawable != null) {
	 * mPromptDrawable.setImageResource(R.drawable.no_backup_record_prompt); }
	 * if (mPromptDesc != null) {
	 * mPromptDesc.setText(R.string.no_backup_record_prompt); }
	 * showPromptFrame(); }
	 */

	private void showToast(String toast) {
		if (toast != null) {
			Toast.makeText(this, toast, Toast.LENGTH_LONG).show();
		}
	}

	/*
	 * @Override public boolean onCreateOptionsMenu(Menu menu) {
	 * getMenuInflater().inflate(R.menu.backuped_records_options_menu, menu);
	 * return true; }
	 *
	 * @Override public boolean onOptionsItemSelected (MenuItem item) { switch
	 * (item.getItemId()) { case R.id.menu_item_delete_records:
	 * startBatchDeleteRecordsActivity();
	 * StatisticsDataManager.getInstance().increaseStatisticInt(this,
	 * StatisticsKey.MENU_BATCH_DELETE, 1); return true; default: return
	 * super.onOptionsItemSelected(item); } }
	 */

	@Override
	protected void onSdCardMounted() {
		super.onSdCardMounted();
		init();
	}

	@Override
	protected void onSdCardUnmounted() {
		super.onSdCardUnmounted();
		onNoBackupRecords();
	}

	private void handleSmartMergeButtonClick() {
		if (!mShouldEnableSmartMergeButton) {
			showToast(getString(R.string.msg_no_record_to_merge));
			return;
		}
		startShowRecordsActivity();
		//		showMergeRecordsAlertDialog();
	}

	private boolean isMeetConditionToMerge() {
		final long totalRecordSize = calculateAllToMergeRecordSize();
		if (totalRecordSize < BackupManager.getInstance().getMaxBackupSizeLimit(this)) {
			return true;
		}

		// 大小超过限制，判断是否付费用户
		return isPaidUser();
	}

	private boolean isPaidUser() {
		return ProductManager.getProductPayInfo(getApplicationContext(), ProductPayInfo.PRODUCT_ID)
				.isAlreadyPaid() || ProductPayInfo.sIsPaidUserByKey;
	}

	private long calculateAllToMergeRecordSize() {
		List<IRecord> normalRecords = mBackupManager.getAllRestoreRecords();
		if (Util.isCollectionEmpty(normalRecords)) {
			return 0;
		}

		long totalSize = 0;
		for (IRecord record : normalRecords) {
			totalSize += record.getSpaceUsage();
		}

		return totalSize;
	}

	//整合备份页内展示定时备份包和所有普通备份包
	private void startShowRecordsActivity() {
		Intent intent = new Intent(this, ShowMergeRecordsActivity.class);
		startActivity(intent);
	}

	private void startMergeActivity() {
		Intent intent = new Intent(this, MergeProcessActivity.class);
		startActivity(intent);
	}

	private void showWaitProgressDialog(int messageResId, boolean cancelable) {
		if (mWaitProgressDialog == null) {
			mWaitProgressDialog = createSpinnerProgressDialog(cancelable);
		}
		mWaitProgressDialog.setMessage(getString(messageResId));
		showDialog(mWaitProgressDialog);
	}

	private void showDeleteRecordAlertDialog() {
		if (mDeleteRecordAlertDialog == null) {
			mDeleteRecordAlertDialog = createDeleteRecordAlertDialog();
		}
		showDialog(mDeleteRecordAlertDialog);
	}

	private void showMergeRecordsAlertDialog() {
		if (mMergeRecordsAlertDialog == null) {
			mMergeRecordsAlertDialog = createMergeAlertDialog();
		}
		showDialog(mMergeRecordsAlertDialog);
	}

	private void showFreeUserAlertDialog() {
		if (mFreeUserAlertDialog == null) {
			mFreeUserAlertDialog = createFreeUserLimitAlertDialog();
		}
		showDialog(mFreeUserAlertDialog);
	}

	private static final int MSG_SHOW_PROGRESS_DIALOG = 0x1002;
	private static final int MSG_DISMISS_DIALOG = 0x1003;
	private static final int MSG_SHOW_LIST_FRAME = 0x1004;
	private static final int MSG_SHOW_PROMPT_FRAME = 0x1005;
	private static final int MSG_INIT_PROMPT = 0x1006;
	private static final int MSG_UPDATE_VIEWS = 0x1007;
	private static final int MSG_SD_CARD_UNMOUNTED = 0x1008;
	private static final int MSG_EMPTY_RECORDS_LIST = 0x1009;
	private static final int MSG_DELETE_RECORD = 0x100b;
	private static final int MSG_SHOW_TOAST = 0x100c;
	private static final int MSG_LIST_DATASET_CHANGED = 0x100d;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_SHOW_PROGRESS_DIALOG :
					if (msg.arg1 > 0) {
						showDialog(msg.arg1);
					}
					break;
				case MSG_DISMISS_DIALOG :
					dismissDialog((Dialog) msg.obj);
					break;
				case MSG_UPDATE_VIEWS :
					updateViews();
					break;
				case MSG_SD_CARD_UNMOUNTED :
					onSdCardUnmounted();
					break;
				case MSG_EMPTY_RECORDS_LIST :
					onNoBackupRecords();
					break;
				case MSG_DELETE_RECORD :
					deleteRecord();
					break;
				case MSG_SHOW_TOAST :
					if (msg.arg1 > 0) {
						showToast(getString(msg.arg1));
					}
					break;
				case MSG_LIST_DATASET_CHANGED :
					if (mAdapter != null) {
						mAdapter.notifyDataSetChanged();
					}
					break;
				default :
					break;
			}
		}
	};
}