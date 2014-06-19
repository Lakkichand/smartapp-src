package com.jiubang.go.backup.pro;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.data.IRecord;
import com.jiubang.go.backup.pro.data.RestorableRecord;
import com.jiubang.go.backup.pro.model.BackupManager;
import com.jiubang.go.backup.pro.product.manage.ProductManager;
import com.jiubang.go.backup.pro.product.manage.ProductPayInfo;
import com.jiubang.go.backup.pro.statistics.StatisticsKey;
import com.jiubang.go.backup.pro.ui.PayUpdateHelpActivity;
import com.jiubang.go.backup.pro.ui.RecordListAdapter;
import com.jiubang.go.backup.pro.util.LogUtil;
import com.jiubang.go.backup.pro.util.Util;

/**
 * @author jiangpeihe
 *整合备份包展示页
 */
public class ShowMergeRecordsActivity extends BaseActivity {

	private static final String NUM_FORMAT = "(%d)";

	private CheckBox mSelectButton;
	private ListView mListView;
	private Button mMergeButton;

	private ProgressDialog mWaitProgressDialog;

	private BackupManager mBackupManager;
	private BaseAdapter mAdapter;

	private RecordItem mMergedRecord;
	private RecordItem mScheduledRecord;
	private PreferenceManager mPreferenceManager;
	private Dialog mMergeRecordsAlertDialog = null;
	private Dialog mFreeUserAlertDialog = null;
	private IRecord mSmartMergedRecord = null;

	private boolean mShouldUpdateSelectButton = true;

	private OnCheckedChangeListener mSelectedBtnCheckedChangeListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			mShouldUpdateSelectButton = false;
			if (isChecked) {
				((RecordItemListAdapter) mAdapter).selectAllItem();
			} else {
				((RecordItemListAdapter) mAdapter).unselectAllItem();
			}
			mShouldUpdateSelectButton = true;
			updateSelectButton();
		}
	};

	private Toast mToast;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initViews();
		init();
		mPreferenceManager = PreferenceManager.getInstance();
	}

	private void init() {
		mBackupManager = BackupManager.getInstance();

		List<IRecord> records = new ArrayList<IRecord>();
		//		// 将整合备份项加入第一个
		mSmartMergedRecord = getMergedRecord();
		//		if (smartMergedRecord != null) {
		//			records.add(smartMergedRecord);
		//			mMergedRecord = new RecordItem();
		//			mMergedRecord.record = smartMergedRecord;
		//			mMergedRecord.isSelected = false;
		//		}

		// 讲定时备份项加入第二个
		IRecord scheduleRecord = getScheduleRecord();
		if (scheduleRecord != null) {
			records.add(scheduleRecord);
			mScheduledRecord = new RecordItem();
			mScheduledRecord.record = scheduleRecord;
			mScheduledRecord.isSelected = false;
		}

		List<IRecord> normalRecords = mBackupManager.getAllRestoreRecords();
		if (normalRecords != null && normalRecords.size() > 0) {
			records.addAll(normalRecords);
		}

		mAdapter = new RecordItemListAdapter(this, new RecordListAdapter(this, records));
		((RecordItemListAdapter) mAdapter).setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(Object item, boolean selected) {
				updateMergeButton();
				updateSelectButton();
			}
		});

		mListView.setAdapter(mAdapter);
		updateMergeButton();
		updateSelectButton();
	}

	private void updateViews() {
		updateNormalRecordList();

		updateMergeButton();

		updateSelectButton();
	}

	private void updateNormalRecordList() {
		BaseAdapter adapter = (BaseAdapter) mListView.getAdapter();
		if (adapter == null || adapter.isEmpty()) {
			mListView.setVisibility(View.GONE);
		} else {
			adapter.notifyDataSetChanged();
		}
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

	private void initViews() {
		setContentView(R.layout.merge_record_activity);

		TextView title = (TextView) findViewById(R.id.title);
		title.setText(R.string.tutorial_merge_title);

		View returnButton = findViewById(R.id.return_btn);
		returnButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				exit();
			}
		});

		mSelectButton = (CheckBox) findViewById(R.id.select_btn);
		if (mSelectButton != null) {
			mSelectButton.setOnCheckedChangeListener(mSelectedBtnCheckedChangeListener);
		}

		mListView = (ListView) findViewById(R.id.record_list);
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final Adapter adapter = parent.getAdapter();
				if (adapter != null) {
					boolean selected = ((RecordItemListAdapter) adapter).isItemSelected(position);
					((RecordItemListAdapter) adapter).setItemSelected(position, !selected);
				}
			}
		});

		mMergeButton = (Button) findViewById(R.id.merge_btn);
		mMergeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!isMeetConditionToMerge()) {
					showFreeUserAlertDialog();
				} else if (mSmartMergedRecord == null) {
					startMergeActivity();
					finish();
				} else {
					showMergeRecordsAlertDialog();
				}
			}
		});
		updateMergeButton();
	}

	private void showWaitProgessDialog() {
		if (mWaitProgressDialog == null) {
			mWaitProgressDialog = createSpinnerProgressDialog(false);
			mWaitProgressDialog.setMessage(getString(R.string.msg_wait));
		}
		showDialog(mWaitProgressDialog);
	}

	private void showMergeRecordsAlertDialog() {

		if (mMergeRecordsAlertDialog == null) {
			mMergeRecordsAlertDialog = createMergeAlertDialog();
		}
		showDialog(mMergeRecordsAlertDialog);
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
							finish();
							startMergeActivity();
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
		//		List<IRecord> normalRecords = mBackupManager.getAllRestoreRecords();
		List<RecordItem> recordItemList = ((RecordItemListAdapter) mAdapter).getRecordItemList();
		if (Util.isCollectionEmpty(recordItemList)) {
			return 0;
		}

		long totalSize = 0;
		for (RecordItem recordItem : recordItemList) {
			if (recordItem != null && recordItem.isSelected) {
				totalSize += recordItem.record.getSpaceUsage();
			}
		}

		return totalSize;
	}

	private void showFreeUserAlertDialog() {
		if (mFreeUserAlertDialog == null) {
			mFreeUserAlertDialog = createFreeUserLimitAlertDialog();
		}
		showDialog(mFreeUserAlertDialog);
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
		intent.putExtra(PayUpdateHelpActivity.EXTRA_PURCHASE_REQUEST_SOURCE,
				StatisticsKey.PURCHASE_FROM_BACKUP_SIZE_LIMIT);

		startActivity(intent);
		mPreferenceManager.putBoolean(this, PreferenceManager.KEY_HAS_SHOWN_PAY_HELP_PAGE, true);
	}

	private void startMergeActivity() {
		Intent intent = new Intent(this, MergeProcessActivity.class);
		intent.putExtra("mergeRcords",
				((RecordItemListAdapter) mAdapter).putAllSelectedRcordIdInStr());
		startActivity(intent);
	}
	private void exit() {
		finish();
	}

	private void updateSelectButton() {
		if (!mShouldUpdateSelectButton) {
			return;
		}
		if (mSelectButton != null) {
			mSelectButton.setOnCheckedChangeListener(null);
			mSelectButton.setChecked(((RecordItemListAdapter) mAdapter).areAllItemSelected());
			mSelectButton.setOnCheckedChangeListener(mSelectedBtnCheckedChangeListener);
		}
	}

	private void updateMergeButton() {
		if (mMergeButton == null) {
			return;
		}
		boolean enabled = (mAdapter == null || mAdapter.isEmpty())
				? false
				: ((RecordItemListAdapter) mAdapter).shouleMergeRecord();
		if (mMergeButton.isEnabled() != enabled) {
			mMergeButton.setEnabled(enabled);
		}
		mMergeButton.setText(getString(R.string.beginmerge));
	}

	private void showToast(String msg) {
		if (mToast != null) {
			mToast.setText(msg);
		} else {
			mToast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG);
		}
		mToast.show();
	}

	private void hideToast() {
		if (mToast != null) {
			mToast.cancel();
			mToast = null;
		}
	}

	private static final int MSG_SHOW_PROGRESS_DIALOG = 0x1002;
	private static final int MSG_DISMISS_DIALOG = 0x1003;
	private static final int MSG_UPDATE_BUTTON = 0x1005;
	private static final int MSG_CHECK_SHOULD_EXIT = 0x1006;
	private static final int MSG_NOTIFY_CHANGE = 0x1007;
	private static final int MSG_UPDATE_VIEWS = 0x1009;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_SHOW_PROGRESS_DIALOG :
					showWaitProgessDialog();
					break;
				case MSG_DISMISS_DIALOG :
					dismissDialog((Dialog) msg.obj);
					break;
				case MSG_UPDATE_BUTTON :
					updateMergeButton();
					break;
				case MSG_CHECK_SHOULD_EXIT :
					if (mAdapter != null
							&& ((RecordItemListAdapter) mAdapter).getValidRecordItemCount() == 0) {
						exit();
					}
					break;
				case MSG_NOTIFY_CHANGE :
					if (mAdapter != null) {
						mAdapter.notifyDataSetChanged();
					}
					//删除后通知更新widget
					Intent updateWidgetIntent = new Intent();
					updateWidgetIntent.setAction("com.jiubang.APPWIDGET_UPDATE");
					sendBroadcast(updateWidgetIntent);
					updateMergeButton();
					updateSelectButton();
					break;
				case MSG_UPDATE_VIEWS :
					updateViews();
					break;
				default :
					break;
			}
		}
	};

	/**
	 * @author maiyongshen
	 */
	private class RecordItem {
		public IRecord record;
		public boolean isSelected;
	}

	/**
	 * @author maiyongshen
	 */
	class RecordItemListAdapter extends BaseAdapter {
		private OnItemSelectedListener mOnItemSelectedListener;
		private BaseAdapter mAdapter;
		private boolean mNotificationEnabled;
		private List<RecordItem> mRecordItemList = new ArrayList<RecordItem>();

		public RecordItemListAdapter(Context context, BaseAdapter adapter) {
			if (adapter == null) {
				throw new IllegalArgumentException("do not accept an empty adapter");
			}
			mAdapter = adapter;
			setNotificationsEnabled(true);
			init();
		}

		private void init() {
			int count = mAdapter.getCount();
			for (int i = 0; i < count; i++) {
				IRecord record = (IRecord) mAdapter.getItem(i);
				// 过滤掉分隔线
				if (record == null) {
					mRecordItemList.add(null);
					continue;
				}
				RecordItem item = new RecordItem();
				item.record = (IRecord) mAdapter.getItem(i);
				item.isSelected = isRecordSelectable((RestorableRecord) item.record);
				mRecordItemList.add(item);
			}
		}

		public List<RecordItem> getRecordItemList() {
			return mRecordItemList;
		}

		@Override
		public int getCount() {
			return mAdapter.getCount();
		}

		@Override
		public Object getItem(int position) {
			return mAdapter.getItem(position);
		}

		@Override
		public long getItemId(int position) {
			return mAdapter.getItemId(position);
		}

		public Iterator<RecordItem> iterator() {
			return mRecordItemList != null ? mRecordItemList.iterator() : null;
		}

		public boolean isItemSelected(int position) {
			RecordItem recordItem = mRecordItemList.get(position);
			if (recordItem != null) {
				return recordItem.isSelected;
			}
			return false;
		}

		public void setItemSelected(int position, boolean selected) {
			RecordItem recordItem = mRecordItemList.get(position);
			if (recordItem != null) {
				final RestorableRecord record = (RestorableRecord) recordItem.record;
				if (selected && !isRecordSelectable(record)) {
					showToast("暂不支持只有数据的应用程序整合");
					notifyDataSetChanged();
					return;
				}
				recordItem.isSelected = selected;
				if (mOnItemSelectedListener != null) {
					mOnItemSelectedListener.onItemSelected(getItem(position), selected);
				}
				notifyDataSetChanged();
			}
		}

		public boolean hasItemSelected() {
			int count = mAdapter.getCount();
			for (int i = 0; i < count; i++) {
				RecordItem item = mRecordItemList.get(i);
				if (item != null && item.isSelected) {
					return true;
				}
			}
			return false;
		}

		public boolean shouleMergeRecord() {
			int count = mAdapter.getCount();
			int isSelectedCount = 0;
			for (int i = 0; i < count; i++) {
				RecordItem item = mRecordItemList.get(i);
				if (item != null && item.isSelected) {
					isSelectedCount++;
				}
			}
			if (mSmartMergedRecord != null) {
				isSelectedCount++;
			}
			if (isSelectedCount > 1) {
				return true;
			}
			return false;
		}

		public int getValidRecordItemCount() {
			int validRecordCount = 0;
			final int count = mRecordItemList.size();
			for (int i = 0; i < count; i++) {
				if (mRecordItemList.get(i) != null) {
					validRecordCount++;
				}
			}
			return validRecordCount;
		}

		public int getSelectedItemCount() {
			int count = 0;
			int len = mAdapter.getCount();
			for (int i = 0; i < len; i++) {
				RecordItem item = mRecordItemList.get(i);
				if (item != null && item.isSelected) {
					count++;
				}
			}
			return count;
		}

		public boolean areAllItemSelected() {
			return getSelectedItemCount() == getValidRecordItemCount();
		}

		public boolean areAllItemUnselected() {
			return !hasItemSelected();
		}

		public void selectAllItem() {
			if (areAllItemSelected()) {
				return;
			}
			setNotificationsEnabled(false);
			int count = mAdapter.getCount();
			for (int i = 0; i < count; i++) {
				setItemSelected(i, true);
			}
			setNotificationsEnabled(true);
			notifyDataSetChanged();
		}

		public void unselectAllItem() {
			if (!hasItemSelected()) {
				return;
			}
			setNotificationsEnabled(false);
			int count = mAdapter.getCount();
			for (int i = 0; i < count; i++) {
				setItemSelected(i, false);
			}
			setNotificationsEnabled(true);
			notifyDataSetChanged();
		}

		public void setOnItemSelectedListener(OnItemSelectedListener l) {
			mOnItemSelectedListener = l;
		}

		@Override
		public void notifyDataSetChanged() {
			if (!mNotificationEnabled) {
				return;
			}
			super.notifyDataSetChanged();
		}

		public void setNotificationsEnabled(boolean flag) {
			mNotificationEnabled = flag;
		}

		/*
		 * public View getHeaderView(Context context, ViewGroup parent) { return
		 * mAdapter.newHeaderView(context, parent, 0); }
		 */

		@Override
		public boolean isEnabled(int position) {
			return mAdapter.isEnabled(position);
		}

		@Override
		public int getViewTypeCount() {
			return mAdapter.getViewTypeCount();
		}

		@Override
		public int getItemViewType(int position) {
			return mAdapter.getItemViewType(position);
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			long t = System.currentTimeMillis();
			View view = mAdapter.getView(position, convertView, parent);
			if (view == null) {
				return null;
			}

			// 表示分隔线
			if (getItem(position) == null) {
				bindNormalRecordSelector(view);
				return view;
			}

			CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
			checkBox.setVisibility(View.VISIBLE);
			checkBox.setOnCheckedChangeListener(null);
			checkBox.setChecked(isItemSelected(position));
			checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					setItemSelected(position, isChecked);
				}
			});
			t = System.currentTimeMillis() - t;
			LogUtil.d("getView time = " + t);
			return view;
		}

		private void bindNormalRecordSelector(View view) {
			CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
			checkBox.setVisibility(onlyHaveNormalRecords() ? View.GONE : View.VISIBLE);
			checkBox.setOnCheckedChangeListener(null);
			checkBox.setChecked(areAllNormalRecordSelected());
			checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					selectAllNormalRecords(isChecked);
				}
			});
		}

		public void selectAllNormalRecords(boolean selected) {
			setNotificationsEnabled(false);
			final int count = mRecordItemList.size();
			for (int i = 0; i < count; i++) {
				RecordItem item = mRecordItemList.get(i);
				if (item == null) {
					continue;
				}
				if (!RecordListAdapter.isSpecialRecord(item.record)) {
					setItemSelected(i, selected);
				}
			}
			setNotificationsEnabled(true);
			notifyDataSetChanged();
		}

		public boolean areAllNormalRecordSelected() {
			final int count = mRecordItemList.size();
			for (int i = 0; i < count; i++) {
				RecordItem item = mRecordItemList.get(i);
				if (item == null) {
					continue;
				}
				if (!RecordListAdapter.isSpecialRecord(item.record) && !item.isSelected) {
					return false;
				}
			}
			return true;
		}

		public int getNormalRecordCount() {
			int normalRecordCount = 0;
			final int count = mRecordItemList.size();
			for (int i = 0; i < count; i++) {
				RecordItem item = mRecordItemList.get(i);
				if (item == null) {
					continue;
				}
				if (!RecordListAdapter.isSpecialRecord(item.record)) {
					normalRecordCount++;
				}
			}
			return normalRecordCount;
		}

		public boolean onlyHaveNormalRecords() {
			return getValidRecordItemCount() == getNormalRecordCount();
		}

		//把已经选中包的Id组成字符串
		private String putAllSelectedRcordIdInStr() {
			String selectedRecordId = "";
			for (RecordItem recordItem : mRecordItemList) {
				if (recordItem == null) {
					continue;
				}
				if (recordItem.isSelected) {
					selectedRecordId += recordItem.record.getId() + "#";
				}
			}
			return selectedRecordId;
		}

	}

	/**
	 * @author maiyongshen
	 */
	interface OnItemSelectedListener {
		public void onItemSelected(Object item, boolean selected);
	}

	private File[] listApkFiles(RestorableRecord record) {
		File recordDir = new File(record.getRecordRootDir());
		return recordDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.contains(".apk");
			}
		});
	}

	private File[] listAppDataFiles(RestorableRecord record) {
		File recordDir = new File(record.getRecordRootDir());
		return recordDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.contains(".tar.gz");
			}
		});
	}

	private boolean hasApkFile(RestorableRecord record) {
		File[] files = listApkFiles(record);
		return files != null && files.length > 0;
	}

	private boolean hasAppDataFile(RestorableRecord record) {
		File[] files = listAppDataFiles(record);
		return files != null && files.length > 0;
	}

	private boolean isRecordSelectable(RestorableRecord record) {
		return !(hasAppDataFile(record) && !hasApkFile(record));
	}
}
