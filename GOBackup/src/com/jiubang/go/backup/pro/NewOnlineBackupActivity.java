package com.jiubang.go.backup.pro;

import java.io.File;
import java.util.Collections;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.MainActivity.AccountAdapter;
import com.jiubang.go.backup.pro.data.AppEntryComparator.SORT_TYPE;
import com.jiubang.go.backup.pro.data.BackupableRecord;
import com.jiubang.go.backup.pro.data.BaseEntry;
import com.jiubang.go.backup.pro.data.BaseEntry.OnSelectedChangeListener;
import com.jiubang.go.backup.pro.data.IRecord;
import com.jiubang.go.backup.pro.image.util.CustomImageAlertDialog;
import com.jiubang.go.backup.pro.image.util.GroupBackupEntry;
import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.model.BackupManager;
import com.jiubang.go.backup.pro.model.BackupManager.BackupType;
import com.jiubang.go.backup.pro.model.BackupManager.CreateRecordListener;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.model.NetSyncTaskDbHelper;
import com.jiubang.go.backup.pro.net.sync.CancelableTask;
import com.jiubang.go.backup.pro.net.sync.CloudServiceManager;
import com.jiubang.go.backup.pro.net.sync.FileHostingServiceProvider;
import com.jiubang.go.backup.pro.net.sync.FileHostingServiceProvider.ActionListener;
import com.jiubang.go.backup.pro.net.sync.OnlineFileInfo;
import com.jiubang.go.backup.pro.product.manage.ProductManager;
import com.jiubang.go.backup.pro.product.manage.ProductPayInfo;
import com.jiubang.go.backup.pro.statistics.StatisticsDataManager;
import com.jiubang.go.backup.pro.statistics.StatisticsKey;
import com.jiubang.go.backup.pro.ui.NetBackupRecordAdapter;
import com.jiubang.go.backup.pro.ui.NetBackupRecordAdapter.BackupAppState;
import com.jiubang.go.backup.pro.ui.PayUpdateHelpActivity;
import com.jiubang.go.backup.pro.ui.RecordDetailListAdpater;
import com.jiubang.go.backup.pro.ui.RecordDetailListAdpater.OnAdapterItemUpdateListener;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 新建云端备份页面
 *
 * @author maiyongshen
 */
public class NewOnlineBackupActivity extends BaseActivity implements OnSelectedChangeListener {
	public static final String EXTRA_EXPERIENCE = "extra_experience";

	private static final String KEY_CURRENT_SORT_TYPE = "key_sort_type";
	private static final int SORT_ONLINE_BY_APP_NAME = 0;
	private static final int SORT_ONLINE_BY_APP_SIZE = 1;

	private SORT_TYPE mCurrentSortType;
	private boolean mSortingApps;

	private AppBackupStateSelector mTempAppSelector;
	private AppBackupStateSelector mAppSelector;

	private ExpandableListView mBackupListView;
	private Button mBackupButton;
	private ImageButton mSortAppsButton;
	private ImageButton mAppSelectorButton;
	private RecordDetailListAdpater mAdapter;

	private ProgressDialog mSpinnerProgressDialog = null;
	private Dialog mAppSortTypePickerDialog = null;
	//	private Dialog mAppPickerDialog = null;
	private Dialog mEnableWifiDialog = null;

	private BackupableRecord mRecord;
	private File mOnlineBackupDBCacheFile;
	private BackupDBHelper mOnlineBackupDBHelper;
	private FileHostingServiceProvider mService;
	private boolean mStartAuthentication = false;

	private File mSyncTaskDBCacheFile;
	private long mTotalBackupSize = 0;

	private boolean mJustExperience;
	private Bitmap mDefaultImage = null;

	private CancelableTask mDownloadTask = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initViews();
		init();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mService != null && mStartAuthentication) {
			finishFileHostingServiceAuthentication();
		}

		// "开始上传"按钮的小车标志
		View tagView = findViewById(R.id.tag_view);
		if (tagView != null) {
			tagView.setVisibility(ProductManager.isPaid(this) || ProductPayInfo.sIsPaidUserByKey
					? View.GONE
					: View.VISIBLE);
			tagView.setBackgroundResource(R.drawable.purchase_icon_red);
		}
	}

	private void finishFileHostingServiceAuthentication() {
		showSpinnerProgressDialog(getString(R.string.tip_loging_in), false);
		mService.finishAuthentication(this, new ActionListener() {

			@Override
			public void onProgress(long progress, long total, Object data) {
			}

			@Override
			public void onCancel(Object data) {
			}

			@Override
			public void onError(int errCode, String errMessage, Object data) {
				Message.obtain(mHandler, MSG_DISMISS_DIALOG, mSpinnerProgressDialog).sendToTarget();
				Message.obtain(mHandler, MSG_SHOW_TOAST, errMessage).sendToTarget();
				// 登录失败，退出
				mStartAuthentication = false;
				if (!mJustExperience) {
					finish();
				}
			}

			@Override
			public void onComplete(Object data) {
				Message.obtain(mHandler, MSG_DISMISS_DIALOG, mSpinnerProgressDialog).sendToTarget();
				if (!mJustExperience) {
					Message.obtain(mHandler, MSG_UPDATE_LOACAL_BACKUP_DB_CACHE).sendToTarget();
				}
				mStartAuthentication = false;
			}
		});
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

	private void initViews() {
		setContentView(R.layout.layout_record_detail);
		getWindow().setFormat(PixelFormat.RGBA_8888);

		TextView title = (TextView) findViewById(R.id.title);
		title.setText(getText(R.string.title_new_cloud_backup));

		View returnButton = findViewById(R.id.return_btn);
		returnButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				BackupManager.getInstance().releaseBackupableRecord();
				finish();
			}
		});

		mSortAppsButton = (ImageButton) findViewById(R.id.sort_btn);
		mSortAppsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showAppSortTypePickerDialog();
			}
		});

		mAppSelectorButton = (ImageButton) findViewById(R.id.app_selector);
		mAppSelectorButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showAppPickerDialog();
			}
		});

		mBackupListView = (ExpandableListView) findViewById(R.id.listview);
		mBackupListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
					int childPosition, long id) {
				BaseEntry entry = mAdapter.getEntry(groupPosition, childPosition);
				if (entry instanceof GroupBackupEntry) {
					getImageDialog(entry, parent, v, groupPosition, childPosition);
				} else {

					mAdapter.toggleEntry(groupPosition, childPosition);
					// 更新子视图状态
					mAdapter.updateAdapterChildItemView(v, groupPosition, childPosition);
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
				}
				return true;
			}
		});

		mBackupButton = (Button) findViewById(R.id.operation_btn);
		mBackupButton.setText(getString(R.string.btn_start_upload));
		mBackupButton.setEnabled(false);
		mBackupButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// generateBackupTaskDb();
				handleBackupButtonClick();
			}
		});
	}

	//抛出对话框
	public void getImageDialog(final BaseEntry entry, final ExpandableListView parent,
			final View v, final int groupPosition, final int childPosition) {
		if (entry == null) {
			return;
		}
		entry.setOnSelectedChangeListener(null);
		entry.setOnSelectedChangeListener(NewOnlineBackupActivity.this);
		CustomImageAlertDialog.showImageDialog(NewOnlineBackupActivity.this, entry, parent, v,
				groupPosition, childPosition, mAdapter, mBackupListView, null, mDefaultImage);
	}

	private void init() {
		// 默认不选中任何app
		mAppSelector = new AppBackupStateSelector();
		mDefaultImage = BitmapFactory.decodeResource(getResources(), R.drawable.defaultimage);
		mJustExperience = getIntent() != null
				&& getIntent().getBooleanExtra(EXTRA_EXPERIENCE, false);
		if (mJustExperience) {
			createBackupableRecord();
			return;
		}

		mService = CloudServiceManager.getInstance().getCurrentService();
		if (mService == null) {
			// TODO 提示
			finish();
			showToast(getString(R.string.account_error));
			return;
		}
		if (!mService.isSessionValid()) {
			try {
				mService.startAuthentication(NewOnlineBackupActivity.this);
			} catch (ActivityNotFoundException e) {
				Message.obtain(mHandler, MSG_SHOW_TOAST, getString(R.string.no_browser_to_login))
						.sendToTarget();
				finish();
				return;
			}
			mStartAuthentication = true;
			return;
		}

		// 检查本地缓存数据库
		updateLocalBackupDbFileWhenNecessary();
	}

	private void handleBackupButtonClick() {
		// 未付费用户先跳转付费页
		if (!ProductManager.isPaid(this) && !ProductPayInfo.sIsPaidUserByKey) {
			startPayHelpActivity();
			return;
		}

		// 登录
		if (mService == null || !mService.isSessionValid()) {
			showLoginDialog();
			return;
		}

		// 检查wifi
		if (Util.isWifiEnable(this)) {
			generateBackupTaskDb();
			return;
		}

		// wifi没有开启，弹出提示
		showEnableWifiAlertDialog();
	}

	private File getOnlineBackupCacheDbFile() {
		return BackupManager.getOnlineBackupCacheDbFile(this, mService);
	}

	private void updateLocalBackupDbFileWhenNecessary() {
		mOnlineBackupDBCacheFile = getOnlineBackupCacheDbFile();
		// 本地没有缓存数据库 直接下载
		if (mOnlineBackupDBCacheFile == null) {
			Message.obtain(mHandler, MSG_DOWNLOAD_ONLINEDB).sendToTarget();
			return;
		}

		// 检查本地的缓存数据库文件与网络备份是否一致
		showSpinnerProgressDialog(getString(R.string.tip_fetching_cloud_backup_info, ""), true);
		mService.getFileInfo(getOnlineBackupDbPath(), new ActionListener() {
			@Override
			public void onProgress(long progress, long total, Object data) {
			}

			@Override
			public void onError(int errCode, String errMessage, Object data) {
				synchronized (NewOnlineBackupActivity.this) {
					if (isFinishing()) {
						return;
					}
				}

				Message.obtain(mHandler, MSG_DISMISS_DIALOG, mSpinnerProgressDialog).sendToTarget();
				if (errCode == FileHostingServiceProvider.SERVER_RESOURCE_NOT_FOUND_ERROR) {
					// 找不到网络上的备份数据库，则应清除本地数据库，并直接创建新记录
					deleteLocalBackupDbCache();
					Message.obtain(mHandler, MSG_CREATE_RECORD).sendToTarget();
				} else if (errCode == FileHostingServiceProvider.SERVICE_UNLINKED_ERROR
						|| errCode == FileHostingServiceProvider.SERVER_UNAUTHORIZED_ERROR) {
					Message.obtain(mHandler, MSG_SHOW_TOAST,
							getString(R.string.tip_authorized_out_of_date)).sendToTarget();
					finish();
				} else {
					Message.obtain(mHandler, MSG_SHOW_TOAST,
							getString(R.string.tip_failed_to_get_cloud_backup_info)).sendToTarget();
					// TODO 目前出现网络错误的处理是直接退出，应优化用户体验
					finish();
				}
			}

			@Override
			public void onComplete(Object data) {
				synchronized (NewOnlineBackupActivity.this) {
					if (isFinishing()) {
						return;
					}
				}

				OnlineFileInfo fileInfo = (OnlineFileInfo) data;
				String revCode = fileInfo.getRevCode();
				String cacheFileRevCode = BackupManager.getOnlineBackupDbVersion(
						NewOnlineBackupActivity.this, mService);
				// 如果对比值不相同，则删除本地这个文件，联网下载数据库文件
				if (!TextUtils.equals(cacheFileRevCode, revCode)) {
					mHandler.sendEmptyMessage(MSG_DOWNLOAD_ONLINEDB);
				} else {
					initDbHelper(mOnlineBackupDBCacheFile);
				}
			}

			@Override
			public void onCancel(Object data) {
			}
		});
	}

	private void deleteLocalBackupDbCache() {
		if (mOnlineBackupDBCacheFile != null && mOnlineBackupDBCacheFile.exists()) {
			mOnlineBackupDBCacheFile.delete();
		}
		mOnlineBackupDBCacheFile = null;
	}

	private void initDbHelper(File tempBackupDbFile) {
		if (tempBackupDbFile != null && tempBackupDbFile.exists()) {
			mOnlineBackupDBHelper = new BackupDBHelper(NewOnlineBackupActivity.this,
					tempBackupDbFile.getAbsolutePath(), BackupDBHelper.getDBVersion());
			// 数据库更新完毕后创建本地备份记录
			createBackupableRecord();
		}
	}

	private void downloadDbFile() {
		mDownloadTask = mService.downloadFile(getOnlineBackupDbPath(), getCacheDir(), null,
				new ActionListener() {
					@Override
					public void onProgress(long progress, long total, Object data) {
						synchronized (NewOnlineBackupActivity.this) {
							if (isFinishing()) {
								return;
							}
						}

						if (mSpinnerProgressDialog != null && mSpinnerProgressDialog.isShowing()) {
							final int downloadProgress = (int) (progress * 1.0f / total * 100);
							mSpinnerProgressDialog
									.setMessage(getString(
											R.string.tip_fetching_cloud_backup_info,
											getString(
													R.string.parenthesized_msg,
													getString(R.string.progress_format,
															downloadProgress))));
						}
					}

					@Override
					public void onError(int errCode, String errMessage, Object data) {
						synchronized (NewOnlineBackupActivity.this) {
							if (isFinishing()) {
								return;
							}
						}

						File cacheDbFile = new File(getCacheDir(), BackupDBHelper.getDBName());
						if (cacheDbFile.exists()) {
							cacheDbFile.delete();
						}
						Message.obtain(mHandler, MSG_DISMISS_DIALOG, mSpinnerProgressDialog)
								.sendToTarget();
						if (errCode == FileHostingServiceProvider.SERVICE_UNLINKED_ERROR
								|| errCode == FileHostingServiceProvider.SERVER_UNAUTHORIZED_ERROR) {
							Message.obtain(mHandler, MSG_SHOW_TOAST,
									getString(R.string.tip_authorized_out_of_date)).sendToTarget();
							finish();
						} else if (errCode == FileHostingServiceProvider.SERVER_RESOURCE_NOT_FOUND_ERROR) {
							// 云端找不到备份数据库，新建备份
							Message.obtain(mHandler, MSG_CREATE_RECORD).sendToTarget();
						} else {
							Message.obtain(mHandler, MSG_SHOW_TOAST,
									getString(R.string.tip_failed_to_get_cloud_backup_info))
									.sendToTarget();
							finish();
						}
					}

					@Override
					public void onComplete(Object data) {
						synchronized (NewOnlineBackupActivity.this) {
							if (isFinishing()) {
								return;
							}
						}

						OnlineFileInfo fileInfo = (OnlineFileInfo) data;
						File localDbFile = new File(getCacheDir(), BackupDBHelper.getDBName());
						if (!localDbFile.exists() || fileInfo == null) {
							Message.obtain(mHandler, MSG_SHOW_TOAST,
									getString(R.string.tip_failed_to_get_cloud_backup_info))
									.sendToTarget();
							finish();
							return;
						}

						deleteLocalBackupDbCache();

						mOnlineBackupDBCacheFile = BackupManager.saveOnlineBackupDbFileToCache(
								NewOnlineBackupActivity.this, mService, localDbFile,
								fileInfo.getRevCode());

						localDbFile.delete();
						initDbHelper(mOnlineBackupDBCacheFile);
					}

					@Override
					public void onCancel(Object data) {
						if (!(data instanceof OnlineFileInfo)) {
							return;
						}
						synchronized (NewOnlineBackupActivity.this) {
							File cacheFile = new File(getCacheDir(), ((OnlineFileInfo) data)
									.getFileName());
							if (cacheFile != null && cacheFile.exists()) {
								cacheFile.delete();
							}
						}
					}
				});
	}

	private void createBackupableRecord() {
		BackupManager.getInstance().createBackupableRecord(
				this,
				new BackupType(BackupType.BACKUP_TYPE_SYSTEM_APP | BackupType.BACKUP_TYPE_USER_DATA
						| BackupType.BACKUP_TYPE_USER_APP | BackupType.BACKUP_TYPE_SYSTEM_DATA
						| BackupType.BACKUP_TYPE_USER_IMAGE), new CreateRecordListener() {
					@Override
					public void onStartCreateRecord() {
						Message.obtain(mHandler, MSG_SHOW_PROGRESS_DIALOG,
								getString(R.string.msg_wait)).sendToTarget();
					}

					@Override
					public void onFinishCreateRecord(IRecord record) {
						Message.obtain(mHandler, MSG_INIT_LIST_ADAPTER, record).sendToTarget();
					}
				});
	}

	private boolean[] getCurrentAppSelector() {
		if (mAppSelector == null) {
			return null;
		}
		boolean[] selector = new boolean[] { mAppSelector.isUnbackupedSelected(),
				mAppSelector.isUpdatableSelected(), mAppSelector.isBackupedSelected() };
		return selector;
	}

	private void closeOnlineBackupDatabase() {
		if (mOnlineBackupDBHelper != null) {
			mOnlineBackupDBHelper.close();
		}
		mOnlineBackupDBHelper = null;
	}

	private void setTabSortButtonVisible(boolean visible) {
		if (mSortAppsButton != null) {
			mSortAppsButton.setVisibility(visible ? View.VISIBLE : View.GONE);
		}
	}

	private void setAppSelectorButtonVisible(boolean visible) {
		if (mAppSelectorButton != null) {
			mAppSelectorButton.setVisibility(visible ? View.VISIBLE : View.GONE);
		}
	}
	
	private void initListAdapter(IRecord record) {
		mRecord = (BackupableRecord) record;
		mAdapter = new NetBackupRecordAdapter(NewOnlineBackupActivity.this, record);
		new Thread(new Runnable() {
			@Override
			public void run() {
				((NetBackupRecordAdapter) mAdapter).init(mOnlineBackupDBHelper);
				closeOnlineBackupDatabase();
				mAdapter.setOnEntrySelectedListener(NewOnlineBackupActivity.this);
				mAdapter.setOnAdapterItemUpdateListener(new OnAdapterItemUpdateListener() {
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
							((RecordDetailListAdpater) adapter).updateAdapterGroupItemView(convertView,
									groupPos);
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
							((RecordDetailListAdpater) adapter).updateAdapterChildItemView(convertView,
									groupPos, childPos);
						}
					}
				});
				
				Message.obtain(mHandler, MSG_DISMISS_DIALOG, mSpinnerProgressDialog).sendToTarget();
				Message.obtain(mHandler, MSG_INIT_LIST_VIEWS).sendToTarget();
			}
		}).start();
	}

	private void initListView() {
		((NetBackupRecordAdapter) mAdapter).resetDefaultSelectedItem();
		mBackupListView.setAdapter(mAdapter);
		boolean hasUserAppEntry = mAdapter != null
				&& mAdapter
						.getChildrenCount(mAdapter.getGroupPositionByKey(IRecord.GROUP_USER_APP)) > 0;
		if (hasUserAppEntry) {
			setTabSortButtonVisible(true);
			setAppSelectorButtonVisible(true);
		} else {
			setTabSortButtonVisible(false);
			setAppSelectorButtonVisible(false);
		}

		// 排序
		sortAppEntries(SORT_TYPE.SORT_ONLINE_BY_APP_NAME);
		if (mSortAppsButton != null) {
			mSortAppsButton.setImageResource(R.drawable.sort_by_name);
		}
		mCurrentSortType = SORT_TYPE.SORT_ONLINE_BY_APP_NAME;
	}

	@Override
	public void onSelectedChange(BaseEntry entry, boolean isSelected) {
		if (!isSelected) {
			if (!mAdapter.hasChildItemSelected()) {
				updateBackupButton(false);
			}
		} else {
			updateBackupButton(true);
		}
		updateBackupButtonWhenEntrySelectedStateChanged(entry, isSelected);
	}

	private void updateBackupButtonText(long totalEntriesSize) {
		String str = mBackupButton.getText().toString();
		if (str.contains("(")) {
			str = str.substring(0, str.indexOf("("));
		}
		if (totalEntriesSize != 0) {
			str += "(" + Util.formatFileSize(totalEntriesSize) + ")";
			// 字体变色处理
		}
		mBackupButton.setText(str);
	}

	private void updateBackupButton(boolean enabled) {
		if (mBackupButton.isEnabled() != enabled) {
			mBackupButton.setEnabled(enabled);
		}
	}

	private void updateBackupButtonWhenEntrySelectedStateChanged(BaseEntry entry, boolean isSelected) {
		if (entry == null) {
			return;
		}
		if (mBackupButton == null) {
			return;
		}

		if (isSelected) {
			mTotalBackupSize += entry.getSpaceUsage();
		} else {
			mTotalBackupSize -= entry.getSpaceUsage();
		}

		String str = mBackupButton.getText().toString();
		if (str.contains("(")) {
			str = str.substring(0, str.indexOf("("));
		}
		if (mTotalBackupSize != 0) {
			str += "(" + Util.formatFileSize(mTotalBackupSize) + ")";
			// 字体变色处理
		}
		mBackupButton.setText(str);
	}

	private String getOnlineBackupDbPath() {
		return new File(mService.getOnlineBackupPath(), BackupDBHelper.getDBName())
				.getAbsolutePath();
	}

	private void showToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}

	private void showSpinnerProgressDialog(String message, boolean cancelable) {
		if (mSpinnerProgressDialog == null) {
			mSpinnerProgressDialog = createSpinnerProgressDialog(cancelable);
		}
		mSpinnerProgressDialog.setCancelable(cancelable);
		mSpinnerProgressDialog.setMessage(message);
		if (cancelable) {
			mSpinnerProgressDialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					synchronized (NewOnlineBackupActivity.this) {
						if (mDownloadTask != null) {
							mDownloadTask.cancel();
						}
						finish();
					}
				}
			});
		} else {
			mSpinnerProgressDialog.setOnCancelListener(null);
		}
		showDialog(mSpinnerProgressDialog);
	}

	private void showAppPickerDialog() {
		new AlertDialog.Builder(this)
				.setTitle(R.string.title_select_apps)
				.setMultiChoiceItems(R.array.backupable_app_state, getCurrentAppSelector(),
						new DialogInterface.OnMultiChoiceClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which, boolean isChecked) {
								int target = -1;
								switch (which) {
									case 0 :
										target = AppBackupStateSelector.UNBACKUPED;
										break;
									case 1 :
										target = AppBackupStateSelector.UPDATABLE;
										break;
									case 2 :
										target = AppBackupStateSelector.BACKUPED;
										break;
									default :
										break;
								}
								if (target > 0) {
									if (mTempAppSelector == null) {
										mTempAppSelector = new AppBackupStateSelector(mAppSelector);
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
							mAppSelector = new AppBackupStateSelector(mTempAppSelector);
						}
						selectAppEntries(BackupAppState.NOT_BACKUPED,
								mAppSelector.isUnbackupedSelected());
						selectAppEntries(BackupAppState.UPDATABLE,
								mAppSelector.isUpdatableSelected());
						selectAppEntries(BackupAppState.BACKUPED, mAppSelector.isBackupedSelected());
						mAdapter.notifyDataSetChanged();
						if (mAdapter.hasChildItemSelected()) {
							updateBackupButton(true);
						} else {
							updateBackupButton(false);
						}

						// 更新选中备份项大小
						mTotalBackupSize = mAdapter.getSelectedEntriesSpaceUsage();
						updateBackupButtonText(mTotalBackupSize);

						mTempAppSelector = null;
					}
				}).setOnCancelListener(new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						mTempAppSelector = null;
					}
				}).show();
	}

	private void showAppSortTypePickerDialog() {
		if (mAppSortTypePickerDialog == null) {
			mAppSortTypePickerDialog = new AlertDialog.Builder(this)
					.setTitle(R.string.title_sort_apps)
					.setCancelable(true)
					.setSingleChoiceItems(R.array.online_backup_sort_type_desc,
							getCurrentSortTypeIndex(), new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									switch (which) {
										case SORT_ONLINE_BY_APP_NAME :
											sortAppEntries(SORT_TYPE.SORT_ONLINE_BY_APP_NAME);
											break;
										case SORT_ONLINE_BY_APP_SIZE :
											sortAppEntries(SORT_TYPE.SORT_ONLINE_BY_APP_SIZE);
											break;
										default :
											break;
									}
									dialog.dismiss();
								}
							})
					.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					}).create();
		}
		showDialog(mAppSortTypePickerDialog);
	}

	private void showEnableWifiAlertDialog() {
		if (mEnableWifiDialog == null) {
			mEnableWifiDialog = new AlertDialog.Builder(this)
					.setTitle(R.string.attention)
					.setCancelable(true)
					.setMessage(R.string.backup_advice_of_wifi)
					.setPositiveButton(R.string.open_wifi, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent it = new Intent();
							it.setAction(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
							startActivity(it);
							dialog.dismiss();
						}
					})
					.setNegativeButton(R.string.direct_upload,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									generateBackupTaskDb();
									dialog.dismiss();
								}
							}).create();
		}
		showDialog(mEnableWifiDialog);
	}

	private static final int MSG_INIT_LIST_VIEWS = 0x1001;
	private static final int MSG_SHOW_PROGRESS_DIALOG = 0x1002;
	private static final int MSG_DISMISS_DIALOG = 0x1003;
	private static final int MSG_SHOW_TOAST = 0x1004;
	private static final int MSG_DOWNLOAD_ONLINEDB = 0x1005;
	private static final int MSG_START_PROCESS_ACTIVITY = 0x1006;
	private static final int MSG_UPDATE_LOACAL_BACKUP_DB_CACHE = 0x1007;
	private static final int MSG_NOTIFY_DATASET_CHANGE = 0x1008;
	private static final int MSG_CREATE_RECORD = 0x1009;
	private static final int MSG_INIT_LIST_ADAPTER = 0x100a;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_INIT_LIST_VIEWS :
					initListView();
					break;
				case MSG_SHOW_PROGRESS_DIALOG :
					if (msg.obj != null) {
						showSpinnerProgressDialog(msg.obj.toString(), false);
					}
					break;
				case MSG_DISMISS_DIALOG :
					dismissDialog((Dialog) msg.obj);
					break;
				case MSG_SHOW_TOAST :
					showToast(msg.obj.toString());
					break;
				case MSG_DOWNLOAD_ONLINEDB :
					showSpinnerProgressDialog(
							getString(R.string.tip_fetching_cloud_backup_info, ""), true);
					downloadDbFile();
					break;
				case MSG_START_PROCESS_ACTIVITY :
					startUploadProcessActivity();
					break;
				case MSG_UPDATE_LOACAL_BACKUP_DB_CACHE :
					updateLocalBackupDbFileWhenNecessary();
					break;
				case MSG_NOTIFY_DATASET_CHANGE :
					if (mAdapter != null && !mAdapter.isEmpty()) {
						((BaseExpandableListAdapter) mAdapter).notifyDataSetChanged();
					}
					break;
				case MSG_CREATE_RECORD :
					createBackupableRecord();
					break;
				case MSG_INIT_LIST_ADAPTER:
					initListAdapter((IRecord) msg.obj);
					break;
				default :
					break;
			}
		}
	};

	private void generateBackupTaskDb() {
		showSpinnerProgressDialog(getString(R.string.msg_wait), false);
		new Thread(new Runnable() {
			@Override
			public void run() {
				createBackupTaskDbFile();
				Message.obtain(mHandler, MSG_DISMISS_DIALOG, mSpinnerProgressDialog).sendToTarget();
				Message.obtain(mHandler, MSG_START_PROCESS_ACTIVITY).sendToTarget();
			}
		}).start();
	}

	private void createBackupTaskDbFile() {
		List<BaseEntry> selectedEntries = mRecord.getSelectedEntries();
		if (Util.isCollectionEmpty(selectedEntries)) {
			return;
		}
		Collections.reverse(selectedEntries);
		NetSyncTaskDbHelper tempTaskDbHelper = new NetSyncTaskDbHelper(this,
				NetSyncTaskDbHelper.getDbName(), NetSyncTaskDbHelper.getDbVersion());
		tempTaskDbHelper.clear();
		tempTaskDbHelper.rebuildBackupTask(selectedEntries);
		tempTaskDbHelper.close();

		File taskDbFile = new File(getCacheDir(), NetSyncTaskDbHelper.getDbName());
		Util.copyFile(getDatabasePath(NetSyncTaskDbHelper.getDbName()).getAbsolutePath(),
				taskDbFile.getAbsolutePath());
		deleteDatabase(NetSyncTaskDbHelper.getDbName());
		mSyncTaskDBCacheFile = taskDbFile;
	}

	private void startUploadProcessActivity() {
		Intent intent = new Intent(NewOnlineBackupActivity.this, UploadBackupProcessActivity.class);
		Log.d("GOBackup", "startUploadProcessActivity : mOnlineBackupDBCacheFile = "
				+ mOnlineBackupDBCacheFile);
		intent.putExtra(UploadBackupProcessActivity.EXTRA_TASK_DB_FILE, mSyncTaskDBCacheFile);
		intent.putExtra(UploadBackupProcessActivity.EXTRA_BACKUP_DB_FILE, mOnlineBackupDBCacheFile);
		startActivity(intent);
		finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// deleteTempBackupDbFile();
		if (mAdapter != null) {
			mAdapter.release();
		}
		if (mService != null) {
			mService.release();
		}
		if (mDefaultImage != null) {
			mDefaultImage.recycle();
			mDefaultImage = null;
		}
	};

	private void updateSortButton(SORT_TYPE sortType) {
		if (mSortAppsButton == null) {
			return;
		}
		int drawableId = -1;
		switch (sortType) {
			case SORT_ONLINE_BY_APP_NAME :
				drawableId = R.drawable.sort_by_name;
				break;
			case SORT_ONLINE_BY_APP_SIZE :
				drawableId = R.drawable.sort_by_size;
				break;
			default :
				break;
		}
		if (drawableId > 0) {
			mSortAppsButton.setImageResource(drawableId);
		}
	}

	private void selectAppEntries(BackupAppState state, boolean selected) {
		final NetBackupRecordAdapter adpater = (NetBackupRecordAdapter) mAdapter;
		if (adpater != null) {
			adpater.selectUserAppEntry(state, selected);
			adpater.selectSystemAppEntry(state, selected);
		}
	}

	private void sortAppEntries(final SORT_TYPE sortType) {
		if (mSortingApps || mAdapter == null || mAdapter.isEmpty() || sortType == mCurrentSortType) {
			return;
		}

		final RecordDetailListAdpater adpater = mAdapter;
		mSortingApps = true;
		updateSortButton(sortType);
		showSpinnerProgressDialog(getString(R.string.msg_wait), false);
		adpater.sortAppEntries(sortType, new IAsyncTaskListener() {

			@Override
			public void onStart(Object arg1, Object arg2) {
			}

			@Override
			public void onProceeding(Object progress, Object arg2, Object arg3, Object arg4) {
			}

			@Override
			public void onEnd(boolean success, Object arg1, Object arg2) {
				Message.obtain(mHandler, MSG_DISMISS_DIALOG, mSpinnerProgressDialog).sendToTarget();
				mHandler.sendEmptyMessage(MSG_NOTIFY_DATASET_CHANGE);
				mCurrentSortType = sortType;
				mSortingApps = false;
			}
		});
	}

	private int getCurrentSortTypeIndex() {
		if (mCurrentSortType == null) {
			return -1;
		}

		switch (mCurrentSortType) {
			case SORT_ONLINE_BY_APP_NAME :
				return SORT_ONLINE_BY_APP_NAME;
			case SORT_ONLINE_BY_APP_SIZE :
				return SORT_ONLINE_BY_APP_SIZE;
			default :
				return -1;
		}
	}

	/*	*//**
			* @author maiyongshen
			*/
	/*
	private class OnlineAppSelectorType {
	public static final int UNBACKUPED = 0x0001;
	public static final int UPDATABLE = 0x0001 << 1;
	public static final int BACKUPED = 0x0001 << 2;

	private int mSelectorType;

	public OnlineAppSelectorType() {
		mSelectorType = 0;
	}

	public OnlineAppSelectorType(int selectType) {
		mSelectorType = selectType;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof OnlineAppSelectorType)) {
			return false;
		}
		return this.mSelectorType == ((OnlineAppSelectorType) o).mSelectorType;
	}

	public void copy(OnlineAppSelectorType type) {
		if (type == null) {
			return;
		}
		this.mSelectorType = type.mSelectorType;
	}

	public boolean isUnbackupedSelected() {
		return (mSelectorType & UNBACKUPED) > 0;
	}

	public boolean isUpdatableSelected() {
		return (mSelectorType & UPDATABLE) > 0;
	}

	public boolean isBackupedSelected() {
		return (mSelectorType & BACKUPED) > 0;
	}

	public void enableSeletedState(int selectedType) {
		mSelectorType = mSelectorType | selectedType;
	}

	public void disableSelectedState(int selectedType) {
		mSelectorType = mSelectorType & (~selectedType);
	}
	}*/

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

	private void startPayHelpActivity() {
		StatisticsDataManager.getInstance().increaseStatisticInt(getApplicationContext(),
				StatisticsKey.PREMIUM_ENTRANCE_CLOUD_BACKUP, 1);

		Intent intent = new Intent(this, PayUpdateHelpActivity.class);
		intent.putExtra(PayUpdateHelpActivity.EXTRA_PURCHASE_REQUEST_SOURCE,
				StatisticsKey.PURCHASE_FROM_CLOUD_BACKUP);
		startActivity(intent);

		PreferenceManager.getInstance().putBoolean(this,
				PreferenceManager.KEY_HAS_SHOWN_PAY_HELP_PAGE, true);
	}
}
