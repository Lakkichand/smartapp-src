package com.jiubang.go.backup.pro;

import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;
import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.data.AppBackupEntry;
import com.jiubang.go.backup.pro.data.AppBackupEntry.AppBackupType;
import com.jiubang.go.backup.pro.data.AppEntryComparator.SORT_TYPE;
import com.jiubang.go.backup.pro.data.BackupableRecord;
import com.jiubang.go.backup.pro.data.BaseEntry;
import com.jiubang.go.backup.pro.data.IRecord;
import com.jiubang.go.backup.pro.model.BackupManager;
import com.jiubang.go.backup.pro.model.BackupManager.BackupType;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.model.RootShell;
import com.jiubang.go.backup.pro.product.manage.ProductManager;
import com.jiubang.go.backup.pro.product.manage.ProductPayInfo;
import com.jiubang.go.backup.pro.statistics.StatisticsDataManager;
import com.jiubang.go.backup.pro.statistics.StatisticsKey;
import com.jiubang.go.backup.pro.track.ga.TrackerEvent;
import com.jiubang.go.backup.pro.track.ga.TrackerLog;
import com.jiubang.go.backup.pro.ui.BackupableRecordDetailAdapter;
import com.jiubang.go.backup.pro.ui.PayUpdateHelpActivity;
import com.jiubang.go.backup.pro.ui.RecordDetailListAdpater;
import com.jiubang.go.backup.pro.ui.RecordDetailListAdpater.OnAdapterItemUpdateListener;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 新建备份记录页面
 *
 * @author maiyongshen
 */

public class NewBackupActivity extends BaseActivity
		implements
			BackupManager.CreateRecordListener,
			BaseEntry.OnSelectedChangeListener {
	private static final String KEY_CURRENT_SORT_TYPE = "key_sort_type";

	private static final int SORT_BY_APP_NAME = 0;
	private static final int SORT_BY_APP_SIZE = 1;
	private static final int SORT_BY_INSTALL_TIME = 2;

	private static final int BACKUP_APP_AND_DATA = 0;
	private static final int BACKUP_APP_ONLY = 1;
	private static final int BACKUP_DATA_ONLY = 2;

	private BackupableRecord mBackupableRecord = null;

	private TextView mTabTitle;
	private ImageButton mAppBackupTypeChooser;
	private ImageButton mSortAppsButton;
	private ExpandableListView mBackupListView;
	private ExpandableListAdapter mAdapter;
	private Button mBackupButton;

	private ProgressDialog mWaitProgressDialog = null;
	private Dialog mAppSorterDialog = null;

	private boolean mShouldBackupAppData;
	private boolean mIsRootUser = false;

	private boolean mSortingApps;
	private SORT_TYPE mCurrentSortType;

	private PreferenceManager mPm;
	private BackupManager mBackupManager;

	private long mTotalBackupSize = 0;

	private Dialog mFreeUserLimitAlertDialog = null;

	private AppBackupType mAppBackupType;

	private BaseAdapter mAppContentChooserAdapter;

	// 用户购买高级版入口标识
	private int mPurchaseEntrance = StatisticsKey.PURCHASE_FROM_INVALID_VALUE;
	private boolean mHasStartPurchaseActivity = false;

	private Tracker mTracker;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
		initViews();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mBackupListView != null) {
			RecordDetailListAdpater adapter = (RecordDetailListAdpater) mBackupListView
					.getExpandableListAdapter();
			if (adapter != null) {
				int groupPos = adapter.getGroupPositionByKey(IRecord.GROUP_SYSTEM_DATA);
				updateExpandableListGroupView(adapter, groupPos);
			}
		}
	}

	private void initViews() {
		setContentView(R.layout.layout_record_detail);
		getWindow().setFormat(PixelFormat.RGBA_8888);

		mTabTitle = (TextView) findViewById(R.id.title);
		mTabTitle.setText(getText(R.string.title_new_backup));

		View returnButton = findViewById(R.id.return_btn);
		if (returnButton != null) {
			returnButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					exit();
				}
			});
		}

		mAppBackupTypeChooser = (ImageButton) findViewById(R.id.app_content_chooser);
		if (mAppBackupTypeChooser != null) {
			mAppBackupTypeChooser.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showAppBackupTypeChosser();
				}
			});
		}

		mSortAppsButton = (ImageButton) findViewById(R.id.sort_btn);
		mSortAppsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showAppSorterDialog();
			}
		});

		mBackupListView = (ExpandableListView) findViewById(R.id.listview);
		mBackupListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
					int childPosition, long id) {
				((RecordDetailListAdpater) mAdapter).toggleEntry(groupPosition, childPosition);
				// ((BaseExpandableListAdapter)
				// mAdapter).notifyDataSetChanged();
				// 更新子视图状态
				((RecordDetailListAdpater) mAdapter).updateAdapterChildItemView(v, groupPosition,
						childPosition);
				// 更新组视图状态
				long packedPos = ExpandableListView.getPackedPositionForGroup(groupPosition);
				int flatPos = mBackupListView.getFlatListPosition(packedPos);
				int firstVisiblePos = mBackupListView.getFirstVisiblePosition();
				int lastVisiblePos = mBackupListView.getLastVisiblePosition();
				if (flatPos >= firstVisiblePos && flatPos <= lastVisiblePos) {
					final ExpandableListAdapter adapter = parent.getExpandableListAdapter();
					View convertView = mBackupListView.getChildAt(flatPos - firstVisiblePos);
					((RecordDetailListAdpater) adapter).updateAdapterGroupItemView(convertView,
							groupPosition);
				}
				return true;
			}
		});

		mBackupButton = (Button) findViewById(R.id.operation_btn);
		mBackupButton.setTag(R.drawable.stateful_green_btn);
		mBackupButton.setText(getString(R.string.btn_start_backup));
		mBackupButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// GA统计
				TrackerLog.i("NewBackupActivity startBackup onClick");
				mTracker.trackEvent(TrackerEvent.CATEGORY_UI_ACTION,
						TrackerEvent.ACTION_BUTTON_PRESS,
						TrackerEvent.LABEL_NEWBACKUP_START_BUTTON, TrackerEvent.OPT_CLICK);

				handleBackupButtonClick();
			}
		});

		mAppContentChooserAdapter = new ArrayAdapter<String>(this,
				R.layout.app_content_chooser_item, R.id.text, getResources().getStringArray(
						R.array.app_backup_type)) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = super.getView(position, convertView, parent);
				final ImageView purchaseIcon = (ImageView) view.findViewById(R.id.purchase_icon);
				if (position == BACKUP_DATA_ONLY) {
					purchaseIcon.setVisibility(!isPaidUser() ? View.VISIBLE : View.GONE);
				} else {
					purchaseIcon.setVisibility(View.GONE);
				}

				final RadioButton radioButton = (RadioButton) view.findViewById(R.id.radio_btn);
				radioButton.setChecked(position == getCurrentAppBackupTypeIndex());
				return view;
			}
		};
	}

	private void handleBackupButtonClick() {
		final long maxBackupSize = mBackupManager.getMaxBackupSizeLimit(this);
		if (!isPaidUser() && mTotalBackupSize >= maxBackupSize) {
			mPurchaseEntrance = StatisticsKey.PURCHASE_FROM_BACKUP_SIZE_LIMIT;
			// 未付费用户
			//			showFreeUserLimitAlertDialog(getString(R.string.too_large_need_pay));
			StatisticsDataManager.getInstance().increaseStatisticInt(getApplicationContext(),
					StatisticsKey.PREMIUM_ENTRANCE_BACKUP_SIZE_LIMIT, 1);
			startPurchaseActivity();
			return;
		}
		startNewbackupProcessActivity();
	}

	private void startNewbackupProcessActivity() {
		Intent intent = new Intent(NewBackupActivity.this, BackupProcessActivity.class);
		intent.putExtra(BackupProcessActivity.EXTRA_ROOT, mIsRootUser);
		//		intent.putExtra(BackupProcessActivity.EXTRA_SHOULD_BACKUP_APP_DATA, mShouldBackupAppData);
		intent.putExtra(BackupProcessActivity.EXTRA_APP_BACKUP_TYPE, mAppBackupType);
		startActivity(intent);
		finish();
	}

	private boolean isPaidUser() {
		return Util.isInland(this) ? true : ProductManager.getProductPayInfo(
				getApplicationContext(), ProductPayInfo.PRODUCT_ID).isAlreadyPaid()
				|| ProductPayInfo.sIsPaidUserByKey;
	}

	private void updateSelectedEntriesSize() {
		if (mAdapter != null) {
			mTotalBackupSize = ((BackupableRecordDetailAdapter) mAdapter)
					.getSelectedEntriesSpaceUsage();
		}
	}

	private void updateBackupButtonText() {
		String str = mBackupButton.getText().toString();
		if (str.contains("(")) {
			str = str.substring(0, str.indexOf("("));
		}
		if (mTotalBackupSize != 0) {
			str += "(" + Util.formatFileSize(mTotalBackupSize) + ")";
		}
		mBackupButton.setText(str);

		final long maxBackupSize = mBackupManager.getMaxBackupSizeLimit(this);
		/*int newResId = mTotalBackupSize > maxBackupSize && !isPaidUser()
				? R.drawable.red_round_button
				: R.drawable.stateful_green_btn;
		final int oldResId = (Integer) mBackupButton.getTag();
		if (newResId != oldResId) {
			mBackupButton.setBackgroundResource(newResId);
			mBackupButton.setTag(newResId);
		}*/
		final ImageView purchaseIcon = (ImageView) findViewById(R.id.tag_view);
		if (purchaseIcon != null) {
			purchaseIcon.setBackgroundResource(R.drawable.purchase_icon_red);
			if (mTotalBackupSize > maxBackupSize && !isPaidUser()) {
				purchaseIcon.setVisibility(View.VISIBLE);
			} else {
				purchaseIcon.setVisibility(View.GONE);
			}
		}
	}

	private void updateBackupButtonWhenEntrySelectedStateChanged(BaseEntry entry, boolean isSelected) {
		if (entry == null) {
			return;
		}

		final long size = entry instanceof AppBackupEntry ? ((AppBackupEntry) entry)
				.getSpaceUsage(mAppBackupType) : entry.getSpaceUsage();

		if (isSelected) {
			mTotalBackupSize += size;
		} else {
			mTotalBackupSize -= size;
		}

		updateBackupButtonText();
	}

	private void init() {
		mPm = PreferenceManager.getInstance();
		mIsRootUser = RootShell.isRootValid();
		mShouldBackupAppData = mIsRootUser ? mPm.getBoolean(this,
				PreferenceManager.KEY_SHOULD_BACKUP_RESTORE_APP_DATA, true) : false;
		mAppBackupType = mShouldBackupAppData ? AppBackupType.APK_DATA : AppBackupType.APK;

		mBackupManager = BackupManager.getInstance();
		mBackupManager.createBackupableRecord(this, new BackupType(
				BackupType.BACKUP_TYPE_SYSTEM_DATA | BackupType.BACKUP_TYPE_USER_DATA
						| BackupType.BACKUP_TYPE_USER_APP), this);
		
		mPm.putBoolean(this, PreferenceManager.KEY_BACKUP_DETAILS_NEW_FEATURE, true);
	}

	@Override
	protected void onStart() {
		super.onStart();
		mHasStartPurchaseActivity = false;

		EasyTracker.getInstance().activityStart(this);
		mTracker = EasyTracker.getTracker();
	}

	@Override
	protected void onStop() {
		super.onStop();

		EasyTracker.getInstance().activityStop(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mCurrentSortType != null) {
			outState.putInt(KEY_CURRENT_SORT_TYPE, mCurrentSortType.ordinal());
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		int sortType = savedInstanceState.getInt(KEY_CURRENT_SORT_TYPE, -1);
		if (sortType != -1) {
			mCurrentSortType = SORT_TYPE.values()[sortType];
		}
	}

	// 不在onDestroy释放资源，因为在其他界面仍需用到这些数据
	@Override
	public void onBackPressed() {
		exit();
	}

	private void exit() {
		mBackupManager.releaseBackupableRecord();
		finish();
	}

	private void onBackupableRecordCreated(BackupableRecord record) {
		mAdapter = new BackupableRecordDetailAdapter(this, record);
		final RecordDetailListAdpater adpater = (RecordDetailListAdpater) mAdapter;
		((BackupableRecordDetailAdapter) adpater).setAppBackupType(mAppBackupType);
		adpater.setOnEntrySelectedListener(NewBackupActivity.this);
		boolean hasAppEntry = adpater.getChildrenCount(adpater
				.getGroupPositionByKey(IRecord.GROUP_USER_APP)) > 0;
		if (hasAppEntry) {
			// TODO 设置按钮为“按名称排序”的图标
			if (mSortAppsButton != null) {
				mSortAppsButton.setImageResource(R.drawable.sort_by_name);
			}
			mCurrentSortType = SORT_TYPE.SORT_BY_APP_NAME;
		}
		setTabSortButtonVisible(hasAppEntry);

		mAppBackupTypeChooser.setVisibility(hasAppEntry && mShouldBackupAppData ? View.VISIBLE : View.GONE);
		findViewById(R.id.pay_flag_app_data_only).setVisibility(
				isPaidUser() || mAppBackupTypeChooser.getVisibility() != View.VISIBLE
						? View.GONE
						: View.VISIBLE);

		mBackupListView.setAdapter(mAdapter);
		initExpandableListView();
	}

	private void initExpandableListView() {
		final RecordDetailListAdpater adpater = (RecordDetailListAdpater) mBackupListView
				.getExpandableListAdapter();
		if (adpater == null || adpater.isEmpty()) {
			return;
		}
		// 勾选用户数据全部项
		adpater.checkGroupAllEntries(adpater.getGroupPositionByKey(IRecord.GROUP_USER_DATA), true);
		((BackupableRecordDetailAdapter) adpater).resetDefaultSelectedItem();

		// int userDataGroupPos =
		// adpater.getGroupPositionByKey(IRecord.GROUP_USER_DATA);
		// if (userDataGroupPos >= 0) {
		// mBackupListView.expandGroup(userDataGroupPos);
		// }
		//
		// int systemDataGroupPos =
		// adpater.getGroupPositionByKey(IRecord.GROUP_SYSTEM_DATA);
		// if (systemDataGroupPos >= 0) {
		// mBackupListView.expandGroup(systemDataGroupPos);
		// }
		//
		// int userAppGroupPos =
		// adpater.getGroupPositionByKey(IRecord.GROUP_USER_APP);
		// if (/*mIsRootUser && */userAppGroupPos >= 0) {
		// mBackupListView.expandGroup(userAppGroupPos);
		// }
		//
		// int systemAppGroupPos =
		// adpater.getGroupPositionByKey(IRecord.GROUP_SYSTEM_APP);
		// if (/*mIsRootUser && */systemAppGroupPos >= 0) {
		// mBackupListView.expandGroup(systemAppGroupPos);
		// }

		adpater.setOnAdapterItemUpdateListener(new OnAdapterItemUpdateListener() {
			@Override
			public void onAdapterGroupItemUpdate(BaseExpandableListAdapter adapter, int groupPos) {
				if (mBackupListView == null) {
					return;
				}
				long packedPos = ExpandableListView.getPackedPositionForGroup(groupPos);
				int flatPos = mBackupListView.getFlatListPosition(packedPos);
				int firstVisiblePos = mBackupListView.getFirstVisiblePosition();
				int lastVisiblePos = mBackupListView.getLastVisiblePosition();
				if (flatPos >= firstVisiblePos && flatPos <= lastVisiblePos) {
					View convertView = mBackupListView.getChildAt(flatPos - firstVisiblePos);
					adpater.updateAdapterGroupItemView(convertView, groupPos);
				}
			}

			@Override
			public void onAdapterChildItemUpdate(BaseExpandableListAdapter adapter, int groupPos,
					int childPos) {
				if (mBackupListView == null) {
					return;
				}
				long packedPos = ExpandableListView.getPackedPositionForChild(groupPos, childPos);
				int flatPos = mBackupListView.getFlatListPosition(packedPos);
				int firstVisiblePos = mBackupListView.getFirstVisiblePosition();
				int lastVisiblePos = mBackupListView.getLastVisiblePosition();
				if (flatPos >= firstVisiblePos && flatPos <= lastVisiblePos) {
					View convertView = mBackupListView.getChildAt(flatPos - firstVisiblePos);
					adpater.updateAdapterChildItemView(convertView, groupPos, childPos);
				}
			}
		});
	}

	private void setTabSortButtonVisible(boolean visible) {
		mSortAppsButton.setVisibility(visible ? View.VISIBLE : View.GONE);
	}

	private void updateExpandableListGroupView(RecordDetailListAdpater adapter, int groupPos) {
		long packedPos = ExpandableListView.getPackedPositionForGroup(groupPos);
		int flatPos = mBackupListView.getFlatListPosition(packedPos);
		int firstVisiblePos = mBackupListView.getFirstVisiblePosition();
		int lastVisiblePos = mBackupListView.getLastVisiblePosition();
		if (flatPos >= firstVisiblePos && flatPos <= lastVisiblePos) {
			View convertView = mBackupListView.getChildAt(flatPos - firstVisiblePos);
			adapter.updateAdapterGroupItemView(convertView, groupPos);
		}
	}

	private void showToast(String toast) {
		if (toast != null) {
			Toast.makeText(this, toast, Toast.LENGTH_LONG).show();
		}
	}

	private void updateBackupButton(boolean enabled) {
		if (mBackupButton.isEnabled() != enabled) {
			mBackupButton.setEnabled(enabled);
		}
	}

	@Override
	public void onStartCreateRecord() {
		mHandler.sendEmptyMessage(MSG_SHOW_PROGRESS_DIALOG);
	}

	@Override
	public void onFinishCreateRecord(IRecord record) {
		if (isFinishing()) {
			return;
		}
		if (record instanceof BackupableRecord) {
			mBackupableRecord = (BackupableRecord) record;
			mBackupableRecord.sortEntries(SORT_TYPE.SORT_BY_APP_NAME);
			Message.obtain(mHandler, MSG_INIT_LISTVIEW, record).sendToTarget();
		}
		Message.obtain(mHandler, MSG_DISMISS_PROGRESS_DIALOG).sendToTarget();
	}

	private static final int MSG_INIT_LISTVIEW = 0x1001;
	private static final int MSG_SHOW_PROGRESS_DIALOG = 0x1002;
	private static final int MSG_DISMISS_DIALOG = 0x1003;
	private static final int MSG_SHOW_TOAST = 0x1004;
	private static final int MSG_TRY_GET_ROOT = 0x1005;
	private static final int MSG_SHOW_SU_UNSUPPORT_DIALOG = 0x1006;
	private static final int MSG_NOTIFY_DATASET_CHANGE = 0x1007;
	private static final int MSG_DISMISS_PROGRESS_DIALOG = 0x1008;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_INIT_LISTVIEW :
					onBackupableRecordCreated((BackupableRecord) msg.obj);
					break;
				case MSG_SHOW_PROGRESS_DIALOG :
					showWaitProgressDialog();
					break;
				case MSG_DISMISS_PROGRESS_DIALOG :
					dismissDialog(mWaitProgressDialog);
					break;
				case MSG_DISMISS_DIALOG :
					dismissDialog((Dialog) msg.obj);
					break;
				case MSG_SHOW_TOAST :
					showToast(getString(msg.arg1));
					break;
				case MSG_TRY_GET_ROOT :
					break;
				case MSG_NOTIFY_DATASET_CHANGE :
					if (mAdapter != null && !mAdapter.isEmpty()) {
						((BaseExpandableListAdapter) mAdapter).notifyDataSetChanged();
					}
					break;
				default :
					break;
			}
		}
	};

	@Override
	public void onSelectedChange(BaseEntry entry, boolean isSelected) {
		if (isSelected && entry.isPaidFunctionItem() && !isPaidUser()) {
			entry.setSelected(false);
			// String msg = "备份系统设置是高级版功能，是否购买？";
			mPurchaseEntrance = StatisticsKey.PURCHASE_FROM_BACKUP_SYSTEM_SETTING;
			//			showFreeUserLimitAlertDialog(getString(R.string.backup_system_need_pay));
			StatisticsDataManager.getInstance().increaseStatisticInt(getApplicationContext(),
					StatisticsKey.PREMIUM_ENTRANCE_BACKUP_SYSTEM_SETTING, 1);
			if (!mHasStartPurchaseActivity) {
				startPurchaseActivity();
			}
			return;
		}

		if (!isSelected) {
			final RecordDetailListAdpater adpater = (RecordDetailListAdpater) mAdapter;
			if (!adpater.hasChildItemSelected()) {
				updateBackupButton(false);
			}
		} else {
			updateBackupButton(true);
		}
		updateBackupButtonWhenEntrySelectedStateChanged(entry, isSelected);
	}

	@Override
	public IntentFilter getSdCardReceiverIntentFilter() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_MEDIA_REMOVED);
		filter.addAction(Intent.ACTION_MEDIA_EJECT);
		filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		filter.addDataScheme("file");
		return filter;
	}

	@Override
	protected void onSdCardUnmounted() {
		super.onSdCardUnmounted();
		updateBackupButton(false);
	}

	@Override
	protected void onSdCardMounted() {
		super.onSdCardMounted();
		updateBackupButton(true);
	}

	private void updateSortButton(SORT_TYPE sortType) {
		if (mSortAppsButton == null) {
			return;
		}
		int drawableId = -1;
		switch (sortType) {
			case SORT_BY_APP_NAME :
				drawableId = R.drawable.sort_by_name;
				break;
			case SORT_BY_APP_SIZE :
				drawableId = R.drawable.sort_by_size;
				break;
			case SORT_BY_APP_INSTALL_TIME :
				drawableId = R.drawable.sort_by_install_date;
				break;
			default :
				break;
		}
		if (drawableId > 0) {
			mSortAppsButton.setImageResource(drawableId);
		}
	}

	private void sortAppEntries(final SORT_TYPE sortType) {
		//		Log.d("GOBackup", "NewBackupActivity : sortAppEntries : mSortingApps == " + mSortingApps);
		if (mSortingApps || mAdapter == null || mAdapter.isEmpty()/* || sortType == mCurrentSortType*/) {
			return;
		}

		final RecordDetailListAdpater adapter = (RecordDetailListAdpater) mAdapter;
		mSortingApps = true;
		updateSortButton(sortType);
		showWaitProgressDialog();
		adapter.sortAppEntries(sortType, new IAsyncTaskListener() {

			@Override
			public void onStart(Object arg1, Object arg2) {
				List<BaseEntry> userAppEntries = adapter.getUserAppEntries();
				if (Util.isCollectionEmpty(userAppEntries)) {
					return;
				}
				for (BaseEntry entry : userAppEntries) {
					((AppBackupEntry) entry).setAppBackupType(mAppBackupType);
				}
			}

			@Override
			public void onProceeding(Object progress, Object arg2, Object arg3, Object arg4) {
			}

			@Override
			public void onEnd(boolean success, Object arg1, Object arg2) {
				//				Log.d("GOBackup", "NewBackupActivity : sortAppEntries : onEnd()");
				Message.obtain(mHandler, MSG_DISMISS_DIALOG, mWaitProgressDialog).sendToTarget();
				mHandler.sendEmptyMessage(MSG_NOTIFY_DATASET_CHANGE);
				mCurrentSortType = sortType;
				mSortingApps = false;
			}
		});

		/*
		 * final List<AppBackupEntry> appList = (List<AppBackupEntry>) adpater
		 * .getGroup(adpater.getGroupPositionByKey(IRecord.GROUP_APP)); if
		 * (appList == null || appList.size() <= 0) { return; } mSortingApps =
		 * true; updateSortButton(sortType); showDialog(DIALOG_PROGRESS); new
		 * Thread(new Runnable() {
		 *
		 * @Override public void run() { switch (sortType) { case
		 * SORT_BY_APP_NAME: sortAppsByName(appList); break; case
		 * SORT_BY_APP_SIZE : sortAppsBySize(appList); break; case
		 * SORT_BY_APP_INSTALL_TIME: sortAppsByInstallDate(appList); break;
		 * default: break; }
		 * mHandler.sendEmptyMessage(MSG_DISMISS_PROGRESS_DIALOG);
		 * mHandler.sendEmptyMessage(MSG_NOTIFY_DATASET_CHANGE);
		 * mCurrentSortType = sortType; mSortingApps = false; } }).start();
		 */
	}

	/*
	 * private void sortAppsByName(List<AppBackupEntry> appList) {
	 * Collections.sort(appList, new AppEntryNameComparator()); //
	 * pm.putInt(this, PreferenceManager.KEY_BACKUP_SORT_TYPE,
	 * SORT_TYPE.SORT_BY_APP_NAME.ordinal()); } private boolean
	 * areAllAppEntriesSizeInited(List<AppBackupEntry> appList) { if (appList ==
	 * null || appList.size() <= 0) { return true; } final int count =
	 * appList.size(); for (int i = count - 1; i >= 0; i--) { final
	 * AppBackupEntry entry = appList.get(i); if (!entry.hasInitSizeFinish()) {
	 * return false; } } return true; } private void
	 * sortAppsBySize(List<AppBackupEntry> appList) {
	 * while(!areAllAppEntriesSizeInited(appList)) { try { Thread.sleep(300); }
	 * catch (InterruptedException e) { } } Collections.sort(appList, new
	 * AppEntrySizeComparator()); // pm.putInt(this,
	 * PreferenceManager.KEY_BACKUP_SORT_TYPE,
	 * SORT_TYPE.SORT_BY_APP_SIZE.ordinal()); } private void
	 * sortAppsByInstallDate(List<AppBackupEntry> appList) {
	 * Collections.sort(appList, new AppEntryInstallDateComparator()); //
	 * pm.putInt(this, PreferenceManager.KEY_BACKUP_SORT_TYPE,
	 * SORT_TYPE.SORT_BY_APP_INSTALL_TIME.ordinal()); }
	 */

	private int getCurrentSortTypeIndex() {
		Log.d("GOBackup", "getCurrentSortTypeIndex()");
		switch (mCurrentSortType) {
			case SORT_BY_APP_NAME :
				return SORT_BY_APP_NAME;
			case SORT_BY_APP_SIZE :
				return SORT_BY_APP_SIZE;
			case SORT_BY_APP_INSTALL_TIME :
				return SORT_BY_INSTALL_TIME;
			default :
				return -1;
		}
	}

	private Dialog createFreeUserLimitAlertDialog() {
		if (mFreeUserLimitAlertDialog == null) {
			mFreeUserLimitAlertDialog = new AlertDialog.Builder(this)
					.setTitle(R.string.purchase_title)
					.setNegativeButton(R.string.cancel, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							mPurchaseEntrance = StatisticsKey.PURCHASE_FROM_INVALID_VALUE;
							dialog.dismiss();
						}
					}).setPositiveButton(R.string.ok, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// 统计用户进入高级版入口
							if (mPurchaseEntrance == StatisticsKey.PURCHASE_FROM_BACKUP_SIZE_LIMIT) {
								StatisticsDataManager.getInstance().increaseStatisticInt(
										getApplicationContext(),
										StatisticsKey.PREMIUM_ENTRANCE_BACKUP_SIZE_LIMIT, 1);
							} else if (mPurchaseEntrance == StatisticsKey.PURCHASE_FROM_BACKUP_SYSTEM_SETTING) {
								StatisticsDataManager.getInstance().increaseStatisticInt(
										getApplicationContext(),
										StatisticsKey.PREMIUM_ENTRANCE_BACKUP_SYSTEM_SETTING, 1);
							}

							Intent intent = new Intent(NewBackupActivity.this,
									PayUpdateHelpActivity.class);
							intent.putExtra(PayUpdateHelpActivity.EXTRA_PURCHASE_REQUEST_SOURCE,
									mPurchaseEntrance);
							startActivity(intent);
							dialog.dismiss();
						}
					}).create();
			mFreeUserLimitAlertDialog.setOnDismissListener(new Dialog.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					mHandler.sendEmptyMessage(MSG_NOTIFY_DATASET_CHANGE);
				}
			});
		}
		return mFreeUserLimitAlertDialog;
	}

	private void showFreeUserLimitAlertDialog(String msg) {
		if (mFreeUserLimitAlertDialog == null) {
			mFreeUserLimitAlertDialog = createFreeUserLimitAlertDialog();
		}
		if (mFreeUserLimitAlertDialog.isShowing()) {
			return;
		}

		if (msg != null) {
			((AlertDialog) mFreeUserLimitAlertDialog).setMessage(msg);
		}
		showDialog(mFreeUserLimitAlertDialog);
	}

	private void createWaitProgressDialog() {
		if (mWaitProgressDialog == null) {
			mWaitProgressDialog = createSpinnerProgressDialog(true);
			mWaitProgressDialog.setMessage(getString(R.string.msg_loading));
			mWaitProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					finish();
				}
			});
		}
	}

	private void showWaitProgressDialog() {
		if (mWaitProgressDialog == null) {
			createWaitProgressDialog();
		}
		showDialog(mWaitProgressDialog);
	}

	private void showAppSorterDialog() {
		if (mAppSorterDialog == null) {
			mAppSorterDialog = new AlertDialog.Builder(this)
					.setTitle(R.string.title_sort_apps)
					.setSingleChoiceItems(R.array.backup_sort_type_desc, getCurrentSortTypeIndex(),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									switch (which) {
										case SORT_BY_APP_NAME :
											sortAppEntries(SORT_TYPE.SORT_BY_APP_NAME);
											break;
										case SORT_BY_APP_SIZE :
											sortAppEntries(SORT_TYPE.SORT_BY_APP_SIZE);
											break;
										case SORT_BY_INSTALL_TIME :
											sortAppEntries(SORT_TYPE.SORT_BY_APP_INSTALL_TIME);
											break;
										default :
											break;
									}
									dialog.dismiss();
								}
							})
					.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					}).create();
		}
		showDialog(mAppSorterDialog);
	}

	private int getCurrentAppBackupTypeIndex() {
		if (mAppBackupType == null) {
			return -1;
		}
		if (mAppBackupType == AppBackupType.APK_DATA) {
			return BACKUP_APP_AND_DATA;
		} else if (mAppBackupType == AppBackupType.APK) {
			return BACKUP_APP_ONLY;
		} else if (mAppBackupType == AppBackupType.DATA_ONLY) {
			return BACKUP_DATA_ONLY;
		}
		return -1;
	}

	private void showAppBackupTypeChosser() {
		new AlertDialog.Builder(this)
				.setTitle(R.string.title_select_app_backup_type)
				.setSingleChoiceItems(mAppContentChooserAdapter, getCurrentAppBackupTypeIndex(),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								switch (which) {
									case BACKUP_APP_AND_DATA :
										mAppBackupType = AppBackupType.APK_DATA;
										break;
									case BACKUP_APP_ONLY :
										mAppBackupType = AppBackupType.APK;
										break;
									case BACKUP_DATA_ONLY :
										if (!isPaidUser()) {
											StatisticsDataManager
													.getInstance()
													.increaseStatisticInt(
															getApplicationContext(),
															StatisticsKey.PREMIUM_ENTRANCE_BACKUP_APP_DATA_ONLY,
															1);
											mPurchaseEntrance = StatisticsKey.PURCHASE_FROM_BACKUP_APP_DATA_ONLY;
											startPurchaseActivity();
											dialog.dismiss();
											return;
										}
										mAppBackupType = AppBackupType.DATA_ONLY;
										break;
									default :
										break;
								}
								((BackupableRecordDetailAdapter) mAdapter)
										.setAppBackupType(mAppBackupType);
								updateSelectedEntriesSize();
								updateBackupButtonText();
								sortAppEntries(mCurrentSortType);
								dialog.dismiss();
							}
						})
				.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();
	}

	private void startPurchaseActivity() {
		if (mHasStartPurchaseActivity) {
			return;
		}
		Intent intent = new Intent(this, PayUpdateHelpActivity.class);
		intent.putExtra(PayUpdateHelpActivity.EXTRA_PURCHASE_REQUEST_SOURCE, mPurchaseEntrance);
		startActivity(intent);
		mHasStartPurchaseActivity = true;
	}
}
