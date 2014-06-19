package com.jiubang.go.backup.pro;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.data.AppEntryComparator.SORT_TYPE;
import com.jiubang.go.backup.pro.data.AppInstallStateSelector;
import com.jiubang.go.backup.pro.data.AppRestoreEntry;
import com.jiubang.go.backup.pro.data.AppRestoreEntry.AppRestoreType;
import com.jiubang.go.backup.pro.data.BaseEntry;
import com.jiubang.go.backup.pro.data.BaseRestoreEntry.RestorableState;
import com.jiubang.go.backup.pro.data.IRecord;
import com.jiubang.go.backup.pro.data.RestorableRecord;
import com.jiubang.go.backup.pro.model.BackupManager;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.model.RootShell;
import com.jiubang.go.backup.pro.product.manage.ProductManager;
import com.jiubang.go.backup.pro.product.manage.ProductPayInfo;
import com.jiubang.go.backup.pro.statistics.StatisticsDataManager;
import com.jiubang.go.backup.pro.statistics.StatisticsKey;
import com.jiubang.go.backup.pro.ui.PayUpdateHelpActivity;
import com.jiubang.go.backup.pro.ui.RecordDetailListAdpater;
import com.jiubang.go.backup.pro.ui.RecordDetailListAdpater.OnAdapterItemUpdateListener;
import com.jiubang.go.backup.pro.ui.RestorableRecordDetailAdapter;
import com.jiubang.go.backup.pro.util.LogUtil;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 恢复备份记录页面
 *
 * @author maiyongshen
 */

public class RestoreBackupActivity extends BaseActivity
		implements
			BaseEntry.OnSelectedChangeListener {
	private static final String KEY_CURRENT_SORT_TYPE = "key_sort_type";

	public static final String RECORD_ID = "record_id";
	private static final String RECORD_DATE_FORMAT = "yyyy-MM-dd  HH:mm";

	private static final int SORT_BY_APP_NAME = 0;
	private static final int SORT_BY_APP_SIZE = 1;
	private static final int SORT_BY_INSTALL_STATUS = 2;

	private static final int RESTORE_APP_AND_DATA = 0;
	private static final int RESTORE_APP_ONLY = 1;
	private static final int RESTORE_DATA_ONLY = 2;

	private static final int MENU_INDEX_APP_SELECT = 0;
	private static final int MENU_INDEX_APP_SORT = 1;

	private TextView mTabTitle;
	private ImageButton mDeleteItemButton;
	private ImageButton mSortAppsButton;
	private ImageButton mAppSelectButton;
	private ImageButton mMenuMoreButton;
	private ExpandableListView mRestoreListView;
	private RestorableRecordDetailAdapter mAdapter;
	private Button mRestoreButton;
	private ImageButton mAppRestoreTypeChooser;

	private ProgressDialog mWaitProgressDialog;
	private Dialog mAppSortTypePickerDialog;

	private long mRecordId;
	private RestorableRecord mRecord;

	private boolean mIsRootUser = false;
	private PreferenceManager mPreferenceManager;

	private SORT_TYPE mCurrentSortType;
	private boolean mSortingApps;

	private Dialog mFreeUserLimitAlertDialog = null;
	// private long mTotalBackupSize = 0;

	private AppInstallStateSelector mAppSelector;
	private AppInstallStateSelector mTempAppSelector;

	private AppRestoreType mAppRestoreType;
	private Set<RestorableState> mAppRestorableStates;

	private BaseAdapter mAppContentChooserAdapter;

	private PopupWindow mMenu;
	private BaseAdapter mMenuAdapter;

	private static final int MSG_RECORD_LOAD_DATA_FINSIH = 0x1001;
	private static final int MSG_SHOW_PROGRESS_DIALOG = 0x1002;
	private static final int MSG_DISMISS_DIALOG = 0x1003;
	private static final int MSG_SHOW_TOAST = 0x1004;
	private static final int MSG_NOTIFY_DATASET_CHANGE = 0x1006;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_RECORD_LOAD_DATA_FINSIH :
					onRecordDataLoaded();
					break;
				case MSG_SHOW_PROGRESS_DIALOG :
					showWaitProgressDialog(msg.arg1, msg.arg2 > 0);
					break;
				case MSG_DISMISS_DIALOG :
					dismissDialog((Dialog) msg.obj);
					break;
				case MSG_SHOW_TOAST :
					showToast(getString(msg.arg1));
					break;
				case MSG_NOTIFY_DATASET_CHANGE :
					if (mAdapter != null && !mAdapter.isEmpty()) {
						mAdapter.notifyDataSetChanged();
					}
					break;
				default :
					super.handleMessage(msg);
					break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initViews();
		init(savedInstanceState);
		mPreferenceManager.putBoolean(getApplicationContext(),
				PreferenceManager.KEY_RESTORE_BACKUP_NEW_FEATURE, true);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (!mIsRootUser) {
			if (mAppRestoreTypeChooser != null) {
				mAppRestoreTypeChooser.setVisibility(View.GONE);
			}
			mAppRestoreType = AppRestoreType.APP;
			updateAppRestoreTypeChooser();
		} else if (mAppRestoreType == null) {
			mAppRestoreType = areOnlyAppRestorable()
					? AppRestoreType.APP
					: areOnlyAppDataRestorable()
							? AppRestoreType.DATA_ONLY
							: AppRestoreType.APP_DATA;
		}
		
		if (mRecord == null) {
			mRecord = BackupManager.getInstance().getRecordById(mRecordId);
			if (mRecord != null) {
				if (mRecord.dataAvailable()) {
					onRecordDataLoaded();
				} else {
					showWaitProgressDialog(R.string.msg_loading, true);
					mRecord.loadData(this, new IAsyncTaskListener() {
						@Override
						public void onStart(Object arg1, Object arg2) {
						}

						@Override
						public void onProceeding(Object progress, Object arg2, Object arg3, Object arg4) {

						}

						@Override
						public void onEnd(boolean success, Object arg1, Object arg2) {
							Message.obtain(mHandler, MSG_RECORD_LOAD_DATA_FINSIH).sendToTarget();
							Message.obtain(mHandler, MSG_DISMISS_DIALOG, mWaitProgressDialog)
									.sendToTarget();
						}
					});
				}
			} else {
				updateRestoreButton();
			}
		}
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
		LogUtil.d("onSaveInstanceState save record id = " + mRecordId);
		outState.putLong(RECORD_ID, mRecordId);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		int sortType = savedInstanceState.getInt(KEY_CURRENT_SORT_TYPE, -1);
		if (sortType != -1) {
			mCurrentSortType = SORT_TYPE.values()[sortType];
		}
		long recordId = savedInstanceState.getLong(RECORD_ID, -1);
		if (recordId != -1) {
			mRecordId = recordId;
		}
	}

	private void init(Bundle savedInstanceState) {
		mPreferenceManager = PreferenceManager.getInstance();
		mIsRootUser = RootShell.isRootValid();

		if (savedInstanceState != null) {
			mRecordId = savedInstanceState.getLong(RECORD_ID, -1);
			LogUtil.d("record id = " + mRecordId);
		} else if (getIntent() != null) {
			mRecordId = getIntent().getLongExtra(RECORD_ID, -1);
		}

		// 标题栏的日期显示
		TextView titleDesc = (TextView) findViewById(R.id.desc);
		if (titleDesc != null && mRecord != null) {
			titleDesc.setVisibility(View.VISIBLE);
			final Date recordDate = mRecord.getDate();
			if (recordDate != null) {
				titleDesc.setText(new SimpleDateFormat(RECORD_DATE_FORMAT).format(recordDate));
			}
		}

		mAppSelector = new AppInstallStateSelector();
	}

	private void onRecordDataLoaded() {
		if (mRecord != null && mRecord.dataAvailable()) {
			if (mRecord.getTotalEntriesCount() != 0) {
				mRecord.selectAllEntries(true);
			}
			if (mAdapter == null) {
				mAdapter = new RestorableRecordDetailAdapter(this, mRecord);
			}
			mAdapter.updateWithRootStateChanged(mIsRootUser);
			mAdapter.setOnEntrySelectedListener(this);
			mAdapter.setAppRestoreType(mAppRestoreType);
			boolean hasUserAppEntry = mAdapter.getChildrenCount(mAdapter
					.getGroupPositionByKey(IRecord.GROUP_USER_APP)) > 0;
			//			setTabSortButtonVisible(hasUserAppEntry);
			//			setAppSelectButtonVisible(hasUserAppEntry);
			if (hasUserAppEntry) {
				mMenuAdapter = new NewPopupAdapter(this, Arrays.asList(getResources()
						.getStringArray(R.array.restore_activity_menu)));
				sortAppEntries(SORT_TYPE.SORT_BY_APP_INSTALL_STATE);
				mAppRestorableStates = mAdapter.getUserAppRestorableStates();
				updateAppRestoreTypeChooser();
			}
			mMenuMoreButton.setVisibility(hasUserAppEntry ? View.VISIBLE : View.GONE);

			mDeleteItemButton.setVisibility(View.VISIBLE);

			initExpandableListView();
		}
		updateRestoreButton();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private void initViews() {
		setContentView(R.layout.layout_record_detail);
		getWindow().setFormat(PixelFormat.RGBA_8888);

		mTabTitle = (TextView) findViewById(R.id.title);
		mTabTitle.setText(getText(R.string.title_restore));
		//		mTabTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX,
		//				getResources().getDimension(R.dimen.restore_detail_page_title_size));

		View returnButton = findViewById(R.id.return_btn);
		if (returnButton != null) {
			returnButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					finish();
				}
			});
		}
		mDeleteItemButton = (ImageButton) findViewById(R.id.delete_item_btn);
		mDeleteItemButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startDeleteItemRecordActivity();
			}
		});
		findViewById(R.id.pay_flag_edit_backup).setVisibility(
				isPaidUser() ? View.GONE : View.VISIBLE);

		mAppSelectButton = (ImageButton) findViewById(R.id.app_selector);
		if (mAppSelectButton != null) {
			// V2.9 暂时隐藏筛选按钮
			mAppSelectButton.setVisibility(View.GONE);
			mAppSelectButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showAppPickerDialog();
				}
			});
		}

		mSortAppsButton = (ImageButton) findViewById(R.id.sort_btn);
		// V2.9 暂时隐藏排序按钮
		mSortAppsButton.setVisibility(View.GONE);
		mSortAppsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showAppSortTypePickerDialog();
			}
		});

		mMenuMoreButton = (ImageButton) findViewById(R.id.menu_more_btn);
		mMenuMoreButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showOrHideMenu(mMenuMoreButton);
			}
		});

		mRestoreListView = (ExpandableListView) findViewById(R.id.listview);
		mRestoreListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
					int childPosition, long id) {
				mAdapter.toggleEntry(groupPosition, childPosition);
				// ((BaseExpandableListAdapter)
				// mAdapter).notifyDataSetChanged();
				// 更新子视图状态
				mAdapter.updateAdapterChildItemView(v, groupPosition, childPosition);
				// 更新组视图状态
				long packedPos = ExpandableListView.getPackedPositionForGroup(groupPosition);
				int flatPos = mRestoreListView.getFlatListPosition(packedPos);
				int firstVisiblePos = mRestoreListView.getFirstVisiblePosition();
				int lastVisiblePos = mRestoreListView.getLastVisiblePosition();
				if (flatPos >= firstVisiblePos && flatPos <= lastVisiblePos) {
					final ExpandableListAdapter adapter = parent.getExpandableListAdapter();
					View convertView = mRestoreListView.getChildAt(flatPos - firstVisiblePos);
					((RecordDetailListAdpater) adapter).updateAdapterGroupItemView(convertView,
							groupPosition);
				}
				return true;
			}
		});

		mRestoreButton = (Button) findViewById(R.id.operation_btn);
		mRestoreButton.setText(getString(R.string.btn_start_restore));
		mRestoreButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				readyToRestore();
			}
		});

		mAppRestoreTypeChooser = (ImageButton) findViewById(R.id.app_content_chooser);
		if (mAppRestoreTypeChooser != null) {
			mAppRestoreTypeChooser.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showAppResotreTypeChooser();
				}
			});
		}

		mAppContentChooserAdapter = new ArrayAdapter<String>(this,
				R.layout.app_content_chooser_item, R.id.text, getResources().getStringArray(
						R.array.app_restore_type)) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = super.getView(position, convertView, parent);
				final ImageView purchaseIcon = (ImageView) view.findViewById(R.id.purchase_icon);
				if (position == RESTORE_DATA_ONLY) {
					purchaseIcon.setVisibility(!isPaidUser() ? View.VISIBLE : View.GONE);
				} else {
					purchaseIcon.setVisibility(View.GONE);
				}

				final RadioButton radioButton = (RadioButton) view.findViewById(R.id.radio_btn);
				radioButton.setChecked(position == getCurrentAppRestoreTypeIndex());
				return view;
			}
		};
	}

	private void startDeleteItemRecordActivity() {
		Intent intent = new Intent(RestoreBackupActivity.this, DeleteItemRecordActivity.class);
		intent.putExtra(RECORD_ID, mRecord.getId());
		startActivity(intent);
		//		finish();
	}

	private void readyToRestore() {
		Intent intent = new Intent(RestoreBackupActivity.this, RestoreProcessActivity.class);
		intent.putExtra(RECORD_ID, mRecord.getId());
		intent.putExtra(RestoreProcessActivity.EXTRA_ROOT, mIsRootUser);
		intent.putExtra(RestoreProcessActivity.EXTRA_SHOULD_RESOTRE_SILENTLY, mIsRootUser);
		intent.putExtra(RestoreProcessActivity.EXTRA_APP_RESTORE_TYPE, mAppRestoreType);
		startActivity(intent);
		finish();
	}

	private void initExpandableListView() {
		mRestoreListView.setAdapter(mAdapter);
		final RecordDetailListAdpater adpater = mAdapter;
		if (adpater == null || adpater.isEmpty()) {
			return;
		}
		int groupCount = adpater.getGroupCount();
		// 勾选所有组
		final int systemDataGroup = adpater.getGroupPositionByKey(IRecord.GROUP_SYSTEM_DATA);
		for (int i = 0; i < groupCount; i++) {
			if (systemDataGroup == i) {
				if (!isPaidUser()) {
					// 免费用户不能恢复系统设置
					continue;
				}
			}
			adpater.checkGroupAllEntries(i, true);
		}
		//		mAppSelector = new AppStateSelector();
		//		mAppSelector.enableAll();

		int userDataGroupPos = adpater.getGroupPositionByKey(IRecord.GROUP_USER_DATA);
		if (userDataGroupPos >= 0) {
			mRestoreListView.expandGroup(userDataGroupPos);
		}

		int systemDataGroupPos = adpater.getGroupPositionByKey(IRecord.GROUP_SYSTEM_DATA);
		if (systemDataGroupPos >= 0) {
			mRestoreListView.expandGroup(systemDataGroupPos);
		}

		int userAppGroupPos = adpater.getGroupPositionByKey(IRecord.GROUP_USER_APP);
		if (/* mIsRootUser && */userAppGroupPos >= 0) {
			mRestoreListView.expandGroup(userAppGroupPos);
		}

		int systemAppGroupPos = adpater.getGroupPositionByKey(IRecord.GROUP_SYSTEM_APP);
		if (/* mIsRootUser && */systemAppGroupPos >= 0) {
			mRestoreListView.expandGroup(systemAppGroupPos);
		}

		adpater.setOnAdapterItemUpdateListener(new OnAdapterItemUpdateListener() {
			@Override
			public void onAdapterGroupItemUpdate(BaseExpandableListAdapter adapter, int groupPos) {
				if (mRestoreListView == null) {
					return;
				}
				long packedPos = ExpandableListView.getPackedPositionForGroup(groupPos);
				int flatPos = mRestoreListView.getFlatListPosition(packedPos);
				int firstVisiblePos = mRestoreListView.getFirstVisiblePosition();
				int lastVisiblePos = mRestoreListView.getLastVisiblePosition();
				if (flatPos >= firstVisiblePos && flatPos <= lastVisiblePos) {
					View convertView = mRestoreListView.getChildAt(flatPos - firstVisiblePos);
					adpater.updateAdapterGroupItemView(convertView, groupPos);
				}
			}

			@Override
			public void onAdapterChildItemUpdate(BaseExpandableListAdapter adapter, int groupPos,
					int childPos) {
				if (mRestoreListView == null) {
					return;
				}
				long packedPos = ExpandableListView.getPackedPositionForChild(groupPos, childPos);
				int flatPos = mRestoreListView.getFlatListPosition(packedPos);
				int firstVisiblePos = mRestoreListView.getFirstVisiblePosition();
				int lastVisiblePos = mRestoreListView.getLastVisiblePosition();
				if (flatPos >= firstVisiblePos && flatPos <= lastVisiblePos) {
					View convertView = mRestoreListView.getChildAt(flatPos - firstVisiblePos);
					adpater.updateAdapterChildItemView(convertView, groupPos, childPos);
				}
			}
		});
	}

	private void setTabSortButtonVisible(boolean visible) {
		mSortAppsButton.setVisibility(visible ? View.VISIBLE : View.GONE);
	}

	private void setAppSelectButtonVisible(boolean visible) {
		if (mAppSelectButton != null) {
			mAppSelectButton.setVisibility(visible ? View.VISIBLE : View.GONE);
		}
	}

	private void showToast(String toast) {
		if (toast != null) {
			Toast.makeText(this, toast, Toast.LENGTH_LONG).show();
		}
	}

	private void updateRestoreButton() {
		final RecordDetailListAdpater adapter = mAdapter;
		mRestoreButton.setEnabled(adapter != null && !adapter.isEmpty() ? adapter
				.hasChildItemSelected() : false);
	}

	@Override
	public void onSelectedChange(BaseEntry entry, boolean isSelected) {
		// 恢复系统设置无需付费
		/*		if (isSelected && entry.isPaidFunctionItem() && !isPaidUser()) {
					entry.setSelected(false);
					showFreeUserLimitAlertDialog(getString(R.string.restore_system_need_pay));
					return;
				}*/
		updateRestoreButton();
	}

	private boolean isPaidUser() {
		return ProductManager.getProductPayInfo(getApplicationContext(), ProductPayInfo.PRODUCT_ID)
				.isAlreadyPaid() || Util.isInland(this) || ProductPayInfo.sIsPaidUserByKey;
	}

	@Override
	protected void onSdCardUnmounted() {
		super.onSdCardMounted();
		updateRestoreButton();
	}

	@Override
	protected void onSdCardMounted() {
		super.onSdCardUnmounted();
		updateRestoreButton();
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
			case SORT_BY_APP_INSTALL_STATE :
				drawableId = R.drawable.sort_by_install_state;
				break;
			default :
				break;
		}
		if (drawableId > 0) {
			mSortAppsButton.setImageResource(drawableId);
		}
	}

	private void sortAppEntries(final SORT_TYPE sortType) {
		if (mSortingApps || mAdapter == null || mAdapter.isEmpty()/* || sortType == mCurrentSortType*/) {
			return;
		}
		final RecordDetailListAdpater adapter = mAdapter;
		mSortingApps = true;
		updateSortButton(sortType);
		//		Log.d("GOBackup",
		//				"RestoreBackupActivity : sortAppEntries : start : MSG_SHOW_PROGRESS_DIALOG");
		showWaitProgressDialog(R.string.msg_loading, true);
		adapter.sortAppEntries(sortType, new IAsyncTaskListener() {
			@Override
			public void onStart(Object arg1, Object arg2) {
				List<BaseEntry> userAppEntries = adapter.getUserAppEntries();
				if (Util.isCollectionEmpty(userAppEntries)) {
					return;
				}
				for (BaseEntry entry : userAppEntries) {
					((AppRestoreEntry) entry).setAppRestoreType(mAppRestoreType);
				}
			}

			@Override
			public void onProceeding(Object progress, Object arg2, Object arg3, Object arg4) {
			}

			@Override
			public void onEnd(boolean success, Object arg1, Object arg2) {
				//				Log.d("GOBackup",
				//						"RestoreBackupActivity : sortAppEntries : onEnd : MSG_DISMISS_DIALOG");
				Message.obtain(mHandler, MSG_DISMISS_DIALOG, mWaitProgressDialog).sendToTarget();
				mHandler.sendEmptyMessage(MSG_NOTIFY_DATASET_CHANGE);
				mCurrentSortType = sortType;
				mSortingApps = false;
			}
		});
		/*
		 * final List<AppRestoreEntry> appList = (List<AppRestoreEntry>) adpater
		 * .getGroup(adpater.getGroupPositionByKey(IRecord.GROUP_APP)); if
		 * (appList == null || appList.size() <= 0) { return; } mSortingApps =
		 * true; updateSortButton(sortType); showDialog(DIALOG_PROGRESS); new
		 * Thread(new Runnable() {
		 *
		 * @Override public void run() { switch (sortType) { case
		 * SORT_BY_APP_NAME: sortAppsByName(appList); break; case
		 * SORT_BY_APP_SIZE : sortAppsBySize(appList); break; case
		 * SORT_BY_APP_INSTALL_STATE: sortAppsByInstallState(appList); break;
		 * default: break; }
		 * mHandler.sendEmptyMessage(MSG_DISMISS_PROGRESS_DIALOG);
		 * mHandler.sendEmptyMessage(MSG_NOTIFY_DATASET_CHANGE);
		 * mCurrentSortType = sortType; mSortingApps = false; } }).start();
		 */
	}

	/*
	 * private void sortAppsByName(List<AppRestoreEntry> appList) {
	 * Collections.sort(appList, new AppRestoreEntryNameComparator()); } private
	 * void sortAppsBySize(List<AppRestoreEntry> appList) {
	 * Collections.sort(appList, new AppEntrySizeComparator()); } private void
	 * sortAppsByInstallState(List<AppRestoreEntry> appList) { if
	 * (mInstalledPackagesMap == null) { mInstalledPackagesMap = new
	 * HashMap<String, PackageInfo>(); List<PackageInfo> installedPackages =
	 * Util.syncLoadInstalledPackages(this); for (PackageInfo pi :
	 * installedPackages) { mInstalledPackagesMap.put(pi.packageName, pi); } }
	 * List<AppRestoreEntry> uninstalledPackages = new
	 * ArrayList<AppRestoreEntry>(); List<AppRestoreEntry> installedPackages =
	 * new ArrayList<AppRestoreEntry>(); for (AppRestoreEntry app : appList) {
	 * AppInfo appInfo = app.getAppInfo(); if (appInfo == null) { continue; } if
	 * (mInstalledPackagesMap.containsKey(appInfo.packageName)) {
	 * installedPackages.add(app); } else { uninstalledPackages.add(app); } }
	 * sortAppsByName(uninstalledPackages); sortAppsByName(installedPackages);
	 * appList.clear(); appList.addAll(uninstalledPackages);
	 * appList.addAll(installedPackages); uninstalledPackages = null;
	 * installedPackages = null; }
	 */

	private int getCurrentSortTypeIndex() {
		if (mCurrentSortType == null) {
			return -1;
		}
		switch (mCurrentSortType) {
			case SORT_BY_APP_NAME :
				return SORT_BY_APP_NAME;
			case SORT_BY_APP_SIZE :
				return SORT_BY_APP_SIZE;
			case SORT_BY_APP_INSTALL_STATE :
				return SORT_BY_INSTALL_STATUS;
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
							dialog.dismiss();
						}
					}).setPositiveButton(R.string.ok, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO: 购买流程
							Intent intent = new Intent(RestoreBackupActivity.this,
									PayUpdateHelpActivity.class);
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

	private void showWaitProgressDialog(int messageResId, boolean cancelable) {
		//		Log.d("GOBackup", "showWaitProgressDialog");
		if (mWaitProgressDialog == null) {
			mWaitProgressDialog = createSpinnerProgressDialog(cancelable);
			String msg = getString(messageResId);
			mWaitProgressDialog.setMessage(msg);
		}
		if (mWaitProgressDialog.isShowing()) {
			return;
		}
		showDialog(mWaitProgressDialog);
	}

	private void showAppSortTypePickerDialog() {
		if (mAppSortTypePickerDialog == null) {
			mAppSortTypePickerDialog = new AlertDialog.Builder(this)
					.setTitle(R.string.title_sort_apps)
					.setSingleChoiceItems(R.array.restore_sort_type_desc,
							getCurrentSortTypeIndex(), new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									switch (which) {
										case SORT_BY_APP_NAME :
											sortAppEntries(SORT_TYPE.SORT_BY_APP_NAME);
											break;
										case SORT_BY_APP_SIZE :
											sortAppEntries(SORT_TYPE.SORT_BY_APP_SIZE);
											break;
										case SORT_BY_INSTALL_STATUS :
											sortAppEntries(SORT_TYPE.SORT_BY_APP_INSTALL_STATE);
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
		showDialog(mAppSortTypePickerDialog);
	}

	private boolean[] getCurrentAppSelector() {
		if (mAppSelector == null) {
			return null;
		}
		return new boolean[] { mAppSelector.isUninstalledSelected(),
				mAppSelector.isInstalledSelected() };
	}

	private void showAppPickerDialog() {
		new AlertDialog.Builder(this)
				.setTitle(R.string.title_select_apps)
				.setMultiChoiceItems(R.array.app_state, getCurrentAppSelector(),
						new DialogInterface.OnMultiChoiceClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which, boolean isChecked) {
								int target = -1;
								switch (which) {
									case 0 :
										target = AppInstallStateSelector.UNINSTALLED;
										break;
									case 1 :
										target = AppInstallStateSelector.INSTALLED;
										break;
									default :
										break;
								}
								if (target > 0) {
									if (mTempAppSelector == null) {
										mTempAppSelector = new AppInstallStateSelector(mAppSelector);
									}
									if (isChecked) {
										mTempAppSelector.enableSelectApp(target);
									} else {
										mTempAppSelector.disableSelectApp(target);
									}
								}
							}
						}).setPositiveButton(R.string.ok, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (mAdapter == null) {
							return;
						}
						if (mTempAppSelector != null && !mTempAppSelector.equals(mAppSelector)) {
							mAppSelector = new AppInstallStateSelector(mTempAppSelector);
						}
						mAdapter.selectUserAppEntries(mAppSelector);
						mAdapter.notifyDataSetChanged();
						mTempAppSelector = null;
					}
				}).setOnCancelListener(new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						mTempAppSelector = null;
					}
				}).show();
	}

	private int getCurrentAppRestoreTypeIndex() {
		if (mAppRestoreType == null) {
			return -1;
		}
		if (mAppRestoreType == AppRestoreType.APP_DATA) {
			return RESTORE_APP_AND_DATA;
		} else if (mAppRestoreType == AppRestoreType.APP) {
			return RESTORE_APP_ONLY;
		} else if (mAppRestoreType == AppRestoreType.DATA_ONLY) {
			return RESTORE_DATA_ONLY;
		}
		return -1;
	}

	private void showAppResotreTypeChooser() {
		new AlertDialog.Builder(this)
				.setTitle(R.string.title_select_app_restore_type)
				.setSingleChoiceItems(mAppContentChooserAdapter, getCurrentAppRestoreTypeIndex(),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								switch (which) {
									case RESTORE_APP_AND_DATA :
										mAppRestoreType = AppRestoreType.APP_DATA;
										break;
									case RESTORE_APP_ONLY :
										mAppRestoreType = AppRestoreType.APP;
										break;
									case RESTORE_DATA_ONLY :
										if (!isPaidUser()) {
											StatisticsDataManager
													.getInstance()
													.increaseStatisticInt(
															getApplicationContext(),
															StatisticsKey.PREMIUM_ENTRANCE_BACKUP_APP_DATA_ONLY,
															1);
											startPurchaseActivity();
											dialog.dismiss();
											return;
										}
										mAppRestoreType = AppRestoreType.DATA_ONLY;
										break;
									default :
										break;
								}
								mAdapter.setAppRestoreType(mAppRestoreType);
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

	private boolean areOnlyAppRestorable() {
		if (mAppRestorableStates == null) {
			return false;
		}
		final Set<RestorableState> states = mAppRestorableStates;
		return states.contains(RestorableState.APP_RESTORABLE)
				&& !states.contains(RestorableState.APP_DATA_RESTORABLE)
				&& !states.contains(RestorableState.DATA_RESTORABLE);
	}

	private boolean areOnlyAppDataRestorable() {
		if (mAppRestorableStates == null) {
			return false;
		}
		final Set<RestorableState> states = mAppRestorableStates;
		return states.contains(RestorableState.DATA_RESTORABLE)
				&& !states.contains(RestorableState.APP_DATA_RESTORABLE)
				&& !states.contains(RestorableState.APP_RESTORABLE);
	}

	private void updateAppRestoreTypeChooser() {
		if (mAppRestoreTypeChooser != null) {
			if (!mIsRootUser) {
				mAppRestoreTypeChooser.setVisibility(View.GONE);
				return;
			}
			if (areOnlyAppDataRestorable() || areOnlyAppRestorable()) {
				mAppRestoreTypeChooser.setVisibility(View.GONE);
				return;
			}
			mAppRestoreTypeChooser.setVisibility(View.VISIBLE);
			findViewById(R.id.pay_flag_app_data_only).setVisibility(
					isPaidUser() || mAppRestoreTypeChooser.getVisibility() != View.VISIBLE
							? View.GONE
							: View.VISIBLE);
		}
	}

	private void startPurchaseActivity() {
		Intent intent = new Intent(this, PayUpdateHelpActivity.class);
		intent.putExtra(PayUpdateHelpActivity.EXTRA_PURCHASE_REQUEST_SOURCE,
				StatisticsKey.PURCHASE_FROM_RESTORE_APP_DATA_ONLY);
		startActivity(intent);
	}

	private PopupWindow buildMenu() {
		ListView popupListView = new ListView(this);
		popupListView.setAdapter(mMenuAdapter);
		popupListView.setSelector(R.drawable.tab_btn_bg);
		popupListView.setCacheColorHint(0);
		popupListView.setDivider(getResources().getDrawable(R.drawable.pop_menu_listview_divider));
		popupListView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));

		final PopupWindow popupMenu = new PopupWindow(this);
		popupMenu.setContentView(popupListView);
		popupMenu.setBackgroundDrawable(getResources().getDrawable(R.drawable.pop_menu_bg));
		int width = measurePopupMenuWidth(popupListView);
		Drawable popupBackground = popupMenu.getBackground();
		if (popupBackground != null) {
			Rect tempRect = new Rect();
			popupBackground.getPadding(tempRect);
			width += tempRect.left + tempRect.right;
		}

		popupMenu.setWidth(width);
		popupMenu.setHeight(LayoutParams.WRAP_CONTENT);
		popupMenu.setWindowLayoutMode(width, LayoutParams.WRAP_CONTENT);
		popupMenu.setFocusable(true);
		popupMenu.setAnimationStyle(-1);
		popupMenu.setTouchable(true);
		popupMenu.setOutsideTouchable(true);

		popupListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				popupMenu.dismiss();
				if (position == MENU_INDEX_APP_SELECT) {
					showAppPickerDialog();
				} else if (position == MENU_INDEX_APP_SORT) {
					showAppSortTypePickerDialog();
				}
			}
		});
		return popupMenu;
	}

	private void showOrHideMenu(View anchorView) {
		if (mMenu == null) {
			mMenu = buildMenu();
		}
		if (mMenu.isShowing()) {
			mMenu.dismiss();
		} else {
			int xoff = 0;
			final Drawable background = mMenu.getBackground();
			if (background != null) {
				Rect tempRect = new Rect();
				background.getPadding(tempRect);
				xoff = -tempRect.left;
			}
			mMenu.showAsDropDown(anchorView, xoff, 0);
		}
	}

	private int measurePopupMenuWidth(ListView listView) {
		if (listView == null) {
			return 0;
		}

		final BaseAdapter adapter = (BaseAdapter) listView.getAdapter();
		if (adapter == null) {
			return 0;
		}

		int width = 0;
		View itemView = null;
		int itemType = 0;
		final int widthMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		final int heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);

		int start = 0;
		final int end = adapter.getCount();
		for (int i = start; i < end; i++) {
			final int positionType = adapter.getItemViewType(i);
			if (positionType != itemType) {
				itemType = positionType;
				itemView = null;
			}
			itemView = adapter.getView(i, itemView, listView);
			if (itemView.getLayoutParams() == null) {
				itemView.setLayoutParams(new ViewGroup.LayoutParams(
						ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
			}
			itemView.measure(widthMeasureSpec, heightMeasureSpec);
			LogUtil.d("textview width = " + itemView.getMeasuredWidth());
			width = Math.max(width, itemView.getMeasuredWidth());
		}

		// Add background padding to measured width
		final Drawable background = listView.getBackground();
		Rect tempRect = new Rect();
		if (background != null) {
			background.getPadding(tempRect);
			width += tempRect.left + tempRect.right;
		}
		return width;
	}

}
