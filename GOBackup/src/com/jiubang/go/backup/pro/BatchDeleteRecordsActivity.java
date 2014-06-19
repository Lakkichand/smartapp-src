package com.jiubang.go.backup.pro;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.data.IRecord;
import com.jiubang.go.backup.pro.data.RestorableRecord;
import com.jiubang.go.backup.pro.model.BackupManager;
import com.jiubang.go.backup.pro.ui.RecordListAdapter;
import com.jiubang.go.backup.pro.util.LogUtil;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 批量删除备份记录页面
 *
 * @author maiyongshen
 */

public class BatchDeleteRecordsActivity extends BaseActivity {
	private static final String NUM_FORMAT = "(%d)";
	private ImageButton mCancelButton;
	private TextView mSelectCountTextView;
	private CheckBox mSelectButton;
	private ListView mListView;
	private Button mDeleteButton;

	private ProgressDialog mWaitProgressDialog;
	private Dialog mDeleteRecordsAlertDialog;

	private BackupManager mBackupManager;
	private BaseAdapter mAdapter;

	private RecordItem mMergedRecord;
	private RecordItem mScheduledRecord;

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
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initViews();
		init();
	}

	private void init() {
		mBackupManager = BackupManager.getInstance();

		List<IRecord> records = new ArrayList<IRecord>();
		// 将整合备份项加入第一个
		IRecord smartMergedRecord = getMergedRecord();
		if (smartMergedRecord != null) {
			records.add(smartMergedRecord);
			mMergedRecord = new RecordItem();
			mMergedRecord.record = smartMergedRecord;
			mMergedRecord.isSelected = false;
		}
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
				updateDeleteButton();
				updateSelectButton();
			}
		});

		mListView.setAdapter(mAdapter);
	}

	private void updateViews() {
		updateNormalRecordList();

		updateDeleteButton();

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
		setContentView(R.layout.delete_record_activity);

		mCancelButton = (ImageButton) findViewById(R.id.cancle_btn);
		mSelectCountTextView = (TextView) findViewById(R.id.selectCount);
		mSelectCountTextView.setText(getString(R.string.title_selection, 0));

		mSelectButton = (CheckBox) findViewById(R.id.checkbox);
		if (mSelectButton != null) {
			mSelectButton.setOnCheckedChangeListener(mSelectedBtnCheckedChangeListener);
		}

		mCancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

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

		mDeleteButton = (Button) findViewById(R.id.operation_btn);
		mDeleteButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showDeleteRecordsAlertDialog();
			}
		});
		updateDeleteButton();
	}

	private void deleteRecords() {
		showWaitProgessDialog();
		new Thread() {
			@Override
			public void run() {
				deleteRecordsInternal();
				Message.obtain(mHandler, MSG_DISMISS_DIALOG, mWaitProgressDialog).sendToTarget();
				mHandler.sendEmptyMessage(MSG_CHECK_SHOULD_EXIT);
			}
		}.start();
	}

	private void deleteRecordsInternal() {
		final RecordItemListAdapter adapter = (RecordItemListAdapter) mAdapter;
		adapter.deleteSelectedRecords();
	}

	private void showWaitProgessDialog() {
		if (mWaitProgressDialog == null) {
			mWaitProgressDialog = createSpinnerProgressDialog(false);
			mWaitProgressDialog.setMessage(getString(R.string.msg_wait));
		}
		showDialog(mWaitProgressDialog);
	}

	private void showDeleteRecordsAlertDialog() {
		if (mDeleteRecordsAlertDialog == null) {
			mDeleteRecordsAlertDialog = new AlertDialog.Builder(this)
					.setIcon(android.R.drawable.stat_notify_error).setTitle(R.string.attention)
					.setMessage(R.string.msg_delete_records)
					.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							deleteRecords();
							dialog.dismiss();
						}
					}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					}).create();
		}
		showDialog(mDeleteRecordsAlertDialog);
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

	private void updateDeleteButton() {
		if (mDeleteButton == null) {
			return;
		}
		boolean enabled = (mAdapter == null || mAdapter.isEmpty())
				? false
				: ((RecordItemListAdapter) mAdapter).hasItemSelected();
		if (mDeleteButton.isEnabled() != enabled) {
			mDeleteButton.setEnabled(enabled);
		}
		int number = 0;
		if (enabled) {
			number = ((RecordItemListAdapter) mAdapter).getSelectedItemCount();
		}
		mSelectCountTextView.setText(getString(R.string.title_selection, number));
		mDeleteButton.setText(getString(R.string.delete));
		//		if (enabled) {
		//			//			String number = String.format(NUM_FORMAT,
		//			//					((RecordItemListAdapter) mAdapter).getSelectedItemCount());
		//			mDeleteButton.setText(getString(R.string.delete));
		//			//			mDeleteButton.setText(getString(R.string.delete) + number);
		//		} else {
		//			mDeleteButton.setText(R.string.delete);
		//		}
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
					updateDeleteButton();
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
					updateDeleteButton();
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
				item.isSelected = false;
				mRecordItemList.add(item);
			}
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

		public void deleteSelectedRecords() {
			for (Iterator<RecordItem> it = iterator(); it.hasNext();) {
				RecordItem item = it.next();
				if (item != null && item.record != null && item.isSelected) {
					((RecordListAdapter) mAdapter).remove(item.record);
					mBackupManager.deleteRecordById(item.record.getId());
					it.remove();
				}
			}
			Message.obtain(mHandler, MSG_NOTIFY_CHANGE).sendToTarget();
		}

	}

	/**
	 * @author maiyongshen
	 */
	interface OnItemSelectedListener {
		public void onItemSelected(Object item, boolean selected);
	}
}
