package com.jiubang.go.backup.pro;

import java.io.File;
import java.util.List;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.data.AppEntryComparator.SORT_TYPE;
import com.jiubang.go.backup.pro.data.BaseEntry;
import com.jiubang.go.backup.pro.data.BaseRestoreEntry;
import com.jiubang.go.backup.pro.data.IRecord;
import com.jiubang.go.backup.pro.data.RestorableRecord;
import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.model.BackupManager;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.product.manage.ProductManager;
import com.jiubang.go.backup.pro.product.manage.ProductPayInfo;
import com.jiubang.go.backup.pro.statistics.StatisticsDataManager;
import com.jiubang.go.backup.pro.statistics.StatisticsKey;
import com.jiubang.go.backup.pro.ui.PayUpdateHelpActivity;
import com.jiubang.go.backup.pro.ui.RecordDetailListAdpater;
import com.jiubang.go.backup.pro.ui.RecordDetailListAdpater.OnAdapterItemUpdateListener;
import com.jiubang.go.backup.pro.ui.RestorableRecordDetailAdapter;
import com.jiubang.go.backup.pro.util.Util;

/**
 * @author jiangpeihe
 * 
 */
public class DeleteItemRecordActivity extends BaseActivity implements
		BaseEntry.OnSelectedChangeListener {
	private static final String KEY_CURRENT_SORT_TYPE = "key_sort_type";

	public static final String RECORD_ID = "record_id";

	private ImageButton mCancelButton;
	private TextView mSelectCountTextView;
	private ExpandableListView mRestoreListView;
	private RestorableRecordDetailAdapter mAdapter;
	private Button mDeleteButton;
	private CheckBox mSeleteAllCheckBox;

	private ProgressDialog mWaitProgressDialog;

	private RestorableRecord mRecord;

	private boolean mIsRootUser = false;
	private ImageView mPaidImageView;

	private SORT_TYPE mCurrentSortType;
	private boolean mSortingApps;

	private Dialog mFreeUserLimitAlertDialog = null;

	private static final int MSG_RECORD_LOAD_DATA_FINSIH = 0x1001;
	private static final int MSG_SHOW_PROGRESS_DIALOG = 0x1002;
	private static final int MSG_DISMISS_DIALOG = 0x1003;
	private static final int MSG_SHOW_TOAST = 0x1004;
	private static final int MSG_DELETE_ITEM = 0x1005;
	private static final int MSG_NOTIFY_DATASET_CHANGE = 0x1006;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_RECORD_LOAD_DATA_FINSIH:
				onRecordDataLoaded();
				break;
			case MSG_SHOW_PROGRESS_DIALOG:
				showWaitProgressDialog((String) msg.obj, false);
				break;
			case MSG_DISMISS_DIALOG:
				dismissDialog((Dialog) msg.obj);
				break;
			case MSG_SHOW_TOAST:
				showToast(getString(msg.arg1));
				break;
			case MSG_NOTIFY_DATASET_CHANGE:
				if (mAdapter != null && !mAdapter.isEmpty()) {
					mAdapter.notifyDataSetChanged();
				}
				break;
			case MSG_DELETE_ITEM:
				startDelete();
				break;
			default:
				super.handleMessage(msg);
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initViews();
		init();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mAdapter != null) {
			mAdapter.release();
		}
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

	private void init() {
		// mIsRootUser = ((GoBackupApplication)
		// getApplication()).getRootProcess()
		// .isRootProcessValid();

		Intent intent = getIntent();
		if (intent != null) {
			long recordId = intent.getLongExtra(RECORD_ID, -1);
			if (recordId >= 0) {
				mRecord = BackupManager.getInstance().getRecordById(recordId);
				mRecord.selectAllEntries(false);
			}
		}
		if (mRecord == null) {
			updateDeleteButton();
			return;
		}

		if (mRecord.dataAvailable()) {
			onRecordDataLoaded();
		} else {
			showWaitProgressDialog(getString(R.string.msg_loading), true);
			mRecord.loadData(this, new IAsyncTaskListener() {
				@Override
				public void onStart(Object arg1, Object arg2) {
				}

				@Override
				public void onProceeding(Object progress, Object arg2,
						Object arg3, Object arg4) {

				}

				@Override
				public void onEnd(boolean success, Object arg1, Object arg2) {
					Message.obtain(mHandler, MSG_RECORD_LOAD_DATA_FINSIH)
							.sendToTarget();
					Message.obtain(mHandler, MSG_DISMISS_DIALOG,
							mWaitProgressDialog).sendToTarget();
				}
			});
		}
		// // 标题栏的日期显示
		// TextView titleDesc = (TextView) findViewById(R.id.desc);
		// if (titleDesc != null && mRecord != null) {
		// titleDesc.setVisibility(View.VISIBLE);
		// final Date recordDate = mRecord.getDate();
		// if (recordDate != null) {
		// titleDesc.setText(new
		// SimpleDateFormat(RECORD_DATE_FORMAT).format(recordDate));
		// }
		// }
	}

	private void onRecordDataLoaded() {
		if (mRecord != null && mRecord.dataAvailable()) {
			if (mAdapter == null) {
				mAdapter = new RestorableRecordDetailAdapter(this, mRecord) {
					@Override
					protected boolean isEntrySelectable(BaseEntry entry) {
						return true;
					}
				};
			}
			// 模拟已获取ROOT权限，以展示正确的程序状态
			mAdapter.updateWithRootStateChanged(true);
			final RecordDetailListAdpater adpater = mAdapter;
			adpater.setOnEntrySelectedListener(this);
			boolean hasUserAppEntry = adpater.getChildrenCount(adpater
					.getGroupPositionByKey(IRecord.GROUP_USER_APP)) > 0;

			initExpandableListView();
		}
		updateDeleteButton();
	}

	private void initViews() {
		setContentView(R.layout.layout_delete_item_detail);
		getWindow().setFormat(PixelFormat.RGBA_8888);
		mCancelButton = (ImageButton) findViewById(R.id.cancle_btn);
		mSelectCountTextView = (TextView) findViewById(R.id.selectCount);
		mSelectCountTextView.setText(getString(R.string.title_selection, 0));
		mSeleteAllCheckBox = (CheckBox) findViewById(R.id.checkbox);
		mRestoreListView = (ExpandableListView) findViewById(R.id.listview);
		mRestoreListView
				.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
					@Override
					public boolean onChildClick(ExpandableListView parent,
							View v, int groupPosition, int childPosition,
							long id) {
						mAdapter.toggleEntry(groupPosition, childPosition);
						// ((BaseExpandableListAdapter)
						// mAdapter).notifyDataSetChanged();
						// 更新子视图状态
						mAdapter.updateAdapterChildItemView(v, groupPosition,
								childPosition);
						// 更新组视图状态
						long packedPos = ExpandableListView
								.getPackedPositionForGroup(groupPosition);
						int flatPos = mRestoreListView
								.getFlatListPosition(packedPos);
						int firstVisiblePos = mRestoreListView
								.getFirstVisiblePosition();
						int lastVisiblePos = mRestoreListView
								.getLastVisiblePosition();
						if (flatPos >= firstVisiblePos
								&& flatPos <= lastVisiblePos) {
							final ExpandableListAdapter adapter = parent
									.getExpandableListAdapter();
							View convertView = mRestoreListView
									.getChildAt(flatPos - firstVisiblePos);
							((RecordDetailListAdpater) adapter)
									.updateAdapterGroupItemView(convertView,
											groupPosition);
						}
						return true;
					}
				});

		mCancelButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		mSeleteAllCheckBox.setOnCheckedChangeListener(mOnCheckedChangeListener);

		mPaidImageView = (ImageView) findViewById(R.id.mut_purchase_icon);
		final boolean isPaid = isPaidUser();
		if (!isPaid) {
			mPaidImageView.setVisibility(View.VISIBLE);
		}
		mDeleteButton = (Button) findViewById(R.id.operation_btn);
		mDeleteButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isPaid) {
					readyToDelete();
				} else {
					StatisticsDataManager.getInstance().increaseStatisticInt(
							getApplicationContext(),
							StatisticsKey.PREMIUM_ENTRANCE_EDIT_BACKUP, 1);
					Intent intent = new Intent(DeleteItemRecordActivity.this,
							PayUpdateHelpActivity.class);
					intent.putExtra(
							PayUpdateHelpActivity.EXTRA_PURCHASE_REQUEST_SOURCE,
							StatisticsKey.PURCHASE_FROM_EDIT_BACKUP);
					startActivity(intent);

				}
			}
		});
	}

	private OnCheckedChangeListener mOnCheckedChangeListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			final RecordDetailListAdpater adpater = mAdapter;
			if (adpater == null || adpater.isEmpty()) {
				return;
			}
			int groupCount = adpater.getGroupCount();
			for (int i = 0; i < groupCount; i++) {
				adpater.checkGroupAllEntries(i, isChecked);
			}
		}
	};

	private void readyToDelete() {
		showWaitProgressDialog(getString(R.string.msg_wait), false);
		Message.obtain(mHandler, MSG_DELETE_ITEM, 1).sendToTarget();
		// finish();
	}

	private void startDelete() {
		List<BaseEntry> allDeleteEntries = mRecord.getSelectedEntries();
		if (allDeleteEntries == null || allDeleteEntries.isEmpty()) {
			return;
		}
		String recordRootPah = mRecord.getRecordRootDir();
		BackupDBHelper backupDBHelper = mRecord
				.getBackupDBHelper(DeleteItemRecordActivity.this);
		File file = new File(recordRootPah);
		if (!file.exists()) {
			allDeleteEntries.clear();
			Message.obtain(mHandler, MSG_DISMISS_DIALOG, mWaitProgressDialog)
					.sendToTarget();
			finish();
			return;
		}
		// 如果全选的话，则删除整个备份包
		if (allDeleteEntries.size() == mRecord.getTotalEntriesCount()) {
			BackupManager mBackupManager = BackupManager.getInstance();
			mBackupManager.deleteRecordById(mRecord.getId());
			for (BaseEntry entry : allDeleteEntries) {
				if (entry == null) {
					continue;
				}
				mRecord.removeEntry(entry);
			}
			allDeleteEntries.clear();
			String[] groupKeys = mRecord.getGroupKeys();
			for (String groupKey : groupKeys) {
				if (mRecord.getGroupItemsCount(groupKey) == 0) {
					mRecord.removeGroup(groupKey);
				}
			}
			Message.obtain(mHandler, MSG_DISMISS_DIALOG, mWaitProgressDialog)
					.sendToTarget();
			finish();
			return;
		}

		if (backupDBHelper == null) {
			allDeleteEntries.clear();
			Message.obtain(mHandler, MSG_DISMISS_DIALOG, mWaitProgressDialog)
					.sendToTarget();
			finish();
			return;
		}
		for (BaseEntry entry : allDeleteEntries) {
			if (entry == null) {
				continue;
			}
			((BaseRestoreEntry) entry).deleteEntryInformationFromRecord(
					backupDBHelper, entry, recordRootPah);
			mRecord.removeEntry(entry);
		}
		String[] groupKeys = mRecord.getGroupKeys();
		for (String groupKey : groupKeys) {
			if (mRecord.getGroupItemsCount(groupKey) == 0) {
				mRecord.removeGroup(groupKey);
			}
		}
		allDeleteEntries.clear();

		Message.obtain(mHandler, MSG_DISMISS_DIALOG, mWaitProgressDialog)
				.sendToTarget();
		finish();

	}

	private void initExpandableListView() {
		mRestoreListView.setAdapter(mAdapter);
		final RecordDetailListAdpater adpater = mAdapter;
		if (adpater == null || adpater.isEmpty()) {
			return;
		}
		// int groupCount = adpater.getGroupCount();
		int userDataGroupPos = adpater
				.getGroupPositionByKey(IRecord.GROUP_USER_DATA);
		if (userDataGroupPos >= 0) {
			mRestoreListView.expandGroup(userDataGroupPos);
		}

		int systemDataGroupPos = adpater
				.getGroupPositionByKey(IRecord.GROUP_SYSTEM_DATA);
		if (systemDataGroupPos >= 0) {
			mRestoreListView.expandGroup(systemDataGroupPos);
		}

		int userAppGroupPos = adpater
				.getGroupPositionByKey(IRecord.GROUP_USER_APP);
		if (/* mIsRootUser && */userAppGroupPos >= 0) {
			mRestoreListView.expandGroup(userAppGroupPos);
		}

		int systemAppGroupPos = adpater
				.getGroupPositionByKey(IRecord.GROUP_SYSTEM_APP);
		if (/* mIsRootUser && */systemAppGroupPos >= 0) {
			mRestoreListView.expandGroup(systemAppGroupPos);
		}

		adpater.setOnAdapterItemUpdateListener(new OnAdapterItemUpdateListener() {
			@Override
			public void onAdapterGroupItemUpdate(
					BaseExpandableListAdapter adapter, int groupPos) {
				if (mRestoreListView == null) {
					return;
				}
				long packedPos = ExpandableListView
						.getPackedPositionForGroup(groupPos);
				int flatPos = mRestoreListView.getFlatListPosition(packedPos);
				int firstVisiblePos = mRestoreListView
						.getFirstVisiblePosition();
				int lastVisiblePos = mRestoreListView.getLastVisiblePosition();
				if (flatPos >= firstVisiblePos && flatPos <= lastVisiblePos) {
					View convertView = mRestoreListView.getChildAt(flatPos
							- firstVisiblePos);
					adpater.updateAdapterGroupItemView(convertView, groupPos);
				}
			}

			@Override
			public void onAdapterChildItemUpdate(
					BaseExpandableListAdapter adapter, int groupPos,
					int childPos) {
				if (mRestoreListView == null) {
					return;
				}
				long packedPos = ExpandableListView.getPackedPositionForChild(
						groupPos, childPos);
				int flatPos = mRestoreListView.getFlatListPosition(packedPos);
				int firstVisiblePos = mRestoreListView
						.getFirstVisiblePosition();
				int lastVisiblePos = mRestoreListView.getLastVisiblePosition();
				if (flatPos >= firstVisiblePos && flatPos <= lastVisiblePos) {
					View convertView = mRestoreListView.getChildAt(flatPos
							- firstVisiblePos);
					adpater.updateAdapterChildItemView(convertView, groupPos,
							childPos);
				}
			}
		});
	}

	private void showToast(String toast) {
		if (toast != null) {
			Toast.makeText(this, toast, Toast.LENGTH_LONG).show();
		}
	}

	private void updateDeleteButton() {
		final RecordDetailListAdpater adapter = mAdapter;
		mDeleteButton
				.setEnabled(adapter != null && !adapter.isEmpty() ? adapter
						.hasChildItemSelected() : false);
	}

	private boolean isAllChildItemSeleted() {
		final String[] keys = mRecord.getGroupKeys();
		for (String groupKey : keys) {
			int groupPosition = mAdapter.getGroupPositionByKey(groupKey);
			if (!mAdapter.isAllChildItemSelected(groupPosition)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void onSelectedChange(BaseEntry entry, boolean isSelected) {
		updateDeleteButton();
		mSeleteAllCheckBox.setOnCheckedChangeListener(null);
		if (isAllChildItemSeleted()) {
			mSeleteAllCheckBox.setChecked(true);
		} else {
			mSeleteAllCheckBox.setChecked(false);
		}
		mSeleteAllCheckBox.setOnCheckedChangeListener(mOnCheckedChangeListener);
		mSelectCountTextView.setText(getString(R.string.title_selection,
				mRecord.getSelectedEntriesCount()));
	}

	private boolean isPaidUser() {
		return ProductManager.getProductPayInfo(getApplicationContext(),
				ProductPayInfo.PRODUCT_ID).isAlreadyPaid()
				|| Util.isInland(this) || ProductPayInfo.sIsPaidUserByKey;
	}

	@Override
	protected void onSdCardUnmounted() {
		super.onSdCardMounted();
		updateDeleteButton();
	}

	@Override
	protected void onSdCardMounted() {
		super.onSdCardUnmounted();
		updateDeleteButton();
	}

	private void showWaitProgressDialog(String msg, boolean cancelable) {
		if (mWaitProgressDialog == null) {
			mWaitProgressDialog = createSpinnerProgressDialog(cancelable);
		}
		mWaitProgressDialog.setMessage(msg);
		if (mWaitProgressDialog.isShowing()) {
			return;
		}
		showDialog(mWaitProgressDialog);
	}

}
