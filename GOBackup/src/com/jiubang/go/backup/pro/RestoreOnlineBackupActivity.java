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
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.data.AppEntryComparator.SORT_TYPE;
import com.jiubang.go.backup.pro.data.AppInfo;
import com.jiubang.go.backup.pro.data.AppInstallStateSelector;
import com.jiubang.go.backup.pro.data.AppRestoreEntry;
import com.jiubang.go.backup.pro.data.BaseEntry;
import com.jiubang.go.backup.pro.data.BaseEntry.EntryComparator;
import com.jiubang.go.backup.pro.data.BaseEntry.EntryType;
import com.jiubang.go.backup.pro.data.BaseEntry.OnSelectedChangeListener;
import com.jiubang.go.backup.pro.data.BaseRestoreEntry;
import com.jiubang.go.backup.pro.data.BaseRestoreEntry.RestorableState;
import com.jiubang.go.backup.pro.data.BookMarkRestoreEntry;
import com.jiubang.go.backup.pro.data.CalendarRestoreEntry;
import com.jiubang.go.backup.pro.data.CallLogRestoreEntry;
import com.jiubang.go.backup.pro.data.ContactsRestoreEntry;
import com.jiubang.go.backup.pro.data.GoLauncherSettingBackupEntry;
import com.jiubang.go.backup.pro.data.GoLauncherSettingRestoreEntry;
import com.jiubang.go.backup.pro.data.IRecord;
import com.jiubang.go.backup.pro.data.LauncherDataRestoreEntry;
import com.jiubang.go.backup.pro.data.LauncherDataRestoreEntry.LauncherDataExtraInfo;
import com.jiubang.go.backup.pro.data.MmsRestoreEntry;
import com.jiubang.go.backup.pro.data.RestorableRecord;
import com.jiubang.go.backup.pro.data.RingtoneRestoreEntry;
import com.jiubang.go.backup.pro.data.SmsRestoreEntry;
import com.jiubang.go.backup.pro.data.UserDictionaryRestoreEntry;
import com.jiubang.go.backup.pro.data.WallpaperRestoreEntry;
import com.jiubang.go.backup.pro.data.WifiRestoreEntry;
import com.jiubang.go.backup.pro.image.util.CustomImageAlertDialog;
import com.jiubang.go.backup.pro.image.util.GroupRestoreEntry;
import com.jiubang.go.backup.pro.image.util.ImageBean;
import com.jiubang.go.backup.pro.image.util.ImageRestoreEntry;
import com.jiubang.go.backup.pro.image.util.OneImageRestoreEntry;
import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.model.BackupDBHelper.AppBackupEntryInfo;
import com.jiubang.go.backup.pro.model.BackupDBHelper.BaseBackupEntryInfo;
import com.jiubang.go.backup.pro.model.BackupDBHelper.ImageBackupEntryInfo;
import com.jiubang.go.backup.pro.model.BackupDBHelper.LauncherDataEntryInfo;
import com.jiubang.go.backup.pro.model.BackupManager;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.model.NetSyncTaskDbHelper;
import com.jiubang.go.backup.pro.model.NetSyncTaskDbHelper.Task;
import com.jiubang.go.backup.pro.model.NetSyncTaskDbHelper.TaskObject;
import com.jiubang.go.backup.pro.model.NetSyncTaskDbHelper.TaskState;
import com.jiubang.go.backup.pro.model.NetSyncTaskDbHelper.TaskType;
import com.jiubang.go.backup.pro.model.RootShell;
import com.jiubang.go.backup.pro.net.sync.CancelableTask;
import com.jiubang.go.backup.pro.net.sync.CloudServiceManager;
import com.jiubang.go.backup.pro.net.sync.FileHostingServiceProvider;
import com.jiubang.go.backup.pro.net.sync.FileHostingServiceProvider.ActionListener;
import com.jiubang.go.backup.pro.net.sync.OnlineFileInfo;
import com.jiubang.go.backup.pro.ui.NetRestoreRecordAdapter;
import com.jiubang.go.backup.pro.ui.NetRestoreRecordAdapter.RestoreAppState;
import com.jiubang.go.backup.pro.ui.RecordDetailListAdpater;
import com.jiubang.go.backup.pro.ui.RecordDetailListAdpater.OnAdapterItemUpdateListener;
import com.jiubang.go.backup.pro.util.PackageUtil;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 云端恢复页面
 *
 * @author maiyongshen
 */
public class RestoreOnlineBackupActivity extends BaseActivity implements OnSelectedChangeListener {
	private static final String KEY_CURRENT_SORT_TYPE = "key_sort_type";

	private RestorableRecord mRecord;
	private ExpandableListView mListView;
	private RecordDetailListAdpater mAdapter;
	private Button mRestoreButton;
	private ImageButton mSortAppsButton;
	private ImageButton mAppSelectorButton;

	private ProgressDialog mSpinnerProgressDialog = null;
	private Dialog mAppSortTypePickerDialog = null;
	private Dialog mEnableWifiDialog = null;

	private SORT_TYPE mCurrentSortType;
	private boolean mSortingApps;

	private AppInstallStateSelector mAppSelector;
	private AppInstallStateSelector mTempAppSelector;

	private static final int SORT_ONLINE_BY_APP_NAME = 0;
	private static final int SORT_ONLINE_BY_APP_SIZE = 1;

	private FileHostingServiceProvider mService;
	private boolean mStartAuthentication = false;
	private File mOnlineBackupDBCacheFile;
	private File mSyncTaskDBCacheFile;

	private long mTotalBackupSize = 0;
	private Bitmap mDefaultImage = null;

	private CancelableTask mDownloadTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initRestoreBackupViews();
		init();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mService != null && mStartAuthentication) {
			finishFileHostingServiceAuthentication();
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
				finish();
			}

			@Override
			public void onComplete(Object data) {
				Message.obtain(mHandler, MSG_DISMISS_DIALOG, mSpinnerProgressDialog).sendToTarget();
				Message.obtain(mHandler, MSG_DOWNLOAD_DB_FILE).sendToTarget();
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

	private File getOnlineBackupCacheDbFile() {
		return BackupManager.getOnlineBackupCacheDbFile(this, mService);
	}

	private void init() {
		mDefaultImage = BitmapFactory.decodeResource(getResources(), R.drawable.defaultimage);
		mService = CloudServiceManager.getInstance().getCurrentService();
		if (mService == null) {
			finish();
			showToast(getString(R.string.account_error));
			return;
		}
		if (!mService.isSessionValid()) {
			try {
				mService.startAuthentication(RestoreOnlineBackupActivity.this);
			} catch (ActivityNotFoundException e) {
				Message.obtain(mHandler, MSG_SHOW_TOAST, getString(R.string.no_browser_to_login))
						.sendToTarget();
				finish();
			}
			mStartAuthentication = true;
			return;
		}

		// 默认不选中任何app
		mAppSelector = new AppInstallStateSelector();

		// 本地没有缓存数据库 直接下载
		mOnlineBackupDBCacheFile = getOnlineBackupCacheDbFile();
		if (mOnlineBackupDBCacheFile == null) {
			Message.obtain(mHandler, MSG_DOWNLOAD_DB_FILE).sendToTarget();
			return;
		}

		// 检查与网络上的数据库是不是相同版本
		showSpinnerProgressDialog(getString(R.string.tip_fetching_cloud_backup_info, ""), true);
		mService.getFileInfo(getOnlineBackupDbPath(), new ActionListener() {
			@Override
			public void onProgress(long progress, long total, Object data) {
			}

			@Override
			public void onComplete(Object data) {
				synchronized (RestoreOnlineBackupActivity.this) {
					if (isFinishing()) {
						return;
					}
				}

				Message.obtain(mHandler, MSG_DISMISS_DIALOG, mSpinnerProgressDialog).sendToTarget();
				OnlineFileInfo fileInfo = (OnlineFileInfo) data;
				String revCode = fileInfo.getRevCode();
				String cacheFileRevCode = BackupManager.getOnlineBackupDbVersion(
						RestoreOnlineBackupActivity.this, mService);

				// 如果对比值不相同，则删除本地这个文件，联网下载数据库文件
				if (!TextUtils.equals(revCode, cacheFileRevCode)) {
					mHandler.sendEmptyMessage(MSG_DOWNLOAD_DB_FILE);
				} else {
					// 在cache目录中重命名为Record可识别的名字
					String cacheDbPath = new File(getCacheDir(), BackupDBHelper.getDBName())
							.getAbsolutePath();
					// TODO 重命名文件失败？
					Util.copyFile(mOnlineBackupDBCacheFile.getPath(), cacheDbPath);
					mHandler.sendEmptyMessage(MSG_CREATE_RESTORE_RECORD);
				}
			}

			@Override
			public void onError(int errCode, String errMessage, Object data) {
				synchronized (RestoreOnlineBackupActivity.this) {
					if (isFinishing()) {
						return;
					}
				}

				Message.obtain(mHandler, MSG_DISMISS_DIALOG, mSpinnerProgressDialog).sendToTarget();
				if (errCode == FileHostingServiceProvider.SERVER_RESOURCE_NOT_FOUND_ERROR) {
					deleteLocalBackupDbCache();
				} else {
					Message.obtain(mHandler, MSG_SHOW_TOAST,
							getString(R.string.tip_failed_to_get_cloud_backup_info)).sendToTarget();
				}
				Message.obtain(mHandler, MSG_ON_NO_ONLINE_BACKUP).sendToTarget();
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
	}

	private void downloadOnlineBackupDbFile() {
		showSpinnerProgressDialog(getString(R.string.tip_fetching_cloud_backup_info, ""), true);
		final File cacheDir = getCacheDir();
		mDownloadTask = mService.downloadFile(getOnlineBackupDbPath(), cacheDir, null,
				new ActionListener() {
					@Override
					public void onProgress(long progress, long total, Object data) {
						synchronized (RestoreOnlineBackupActivity.this) {
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
						synchronized (RestoreOnlineBackupActivity.this) {
							if (isFinishing()) {
								return;
							}
						}

						if (errCode != FileHostingServiceProvider.SERVER_RESOURCE_NOT_FOUND_ERROR) {
							if (!TextUtils.isEmpty(errMessage)) {
								Message.obtain(mHandler, MSG_SHOW_TOAST, errMessage).sendToTarget();
							}
						}
						Message.obtain(mHandler, MSG_DISMISS_DIALOG, mSpinnerProgressDialog)
								.sendToTarget();
						Message.obtain(mHandler, MSG_ON_NO_ONLINE_BACKUP).sendToTarget();
					}

					@Override
					public void onComplete(Object data) {
						synchronized (RestoreOnlineBackupActivity.this) {
							if (isFinishing()) {
								return;
							}
						}

						if (data == null) {
							return;
						}
						OnlineFileInfo fileInfo = (OnlineFileInfo) data;
						File dbFile = new File(cacheDir, fileInfo.getFileName());
						if (fileInfo == null || !dbFile.exists()) {
							Message.obtain(mHandler, MSG_ON_NO_ONLINE_BACKUP).sendToTarget();
							return;
						}

						// 删除旧的数据库文件
						deleteLocalBackupDbCache();
						mOnlineBackupDBCacheFile = dbFile;

						// 把数据库文件重命名后在cache中复制一份
						BackupManager.saveOnlineBackupDbFileToCache(
								RestoreOnlineBackupActivity.this, mService,
								mOnlineBackupDBCacheFile, fileInfo.getRevCode());

						mHandler.sendEmptyMessage(MSG_CREATE_RESTORE_RECORD);
					}

					@Override
					public void onCancel(Object data) {
						if (!(data instanceof OnlineFileInfo)) {
							return;
						}
						synchronized (RestoreOnlineBackupActivity.this) {
							File cacheFile = new File(getCacheDir(), ((OnlineFileInfo) data)
									.getFileName());
							if (cacheFile != null && cacheFile.exists()) {
								cacheFile.delete();
							}
						}
					}
				});
	}

	private BackupDBHelper mRecordCacheDbHelper = null;

	private void createRestoreRecord(final File recordDir) {
		showSpinnerProgressDialog(getString(R.string.msg_wait), false);
		new Thread(new Runnable() {
			@Override
			public void run() {
				mRecord = new RestorableRecord(RestoreOnlineBackupActivity.this, recordDir
						.getAbsolutePath());
				mRecordCacheDbHelper = mRecord.getBackupDBHelper(RestoreOnlineBackupActivity.this);
				if (mRecordCacheDbHelper == null) {
					Message.obtain(mHandler, MSG_SHOW_TOAST,
							getString(R.string.tip_failed_to_get_cloud_backup_info)).sendToTarget();
					finish();
					return;
				}
				List<BaseBackupEntryInfo> allBackupEntriesInfo = mRecordCacheDbHelper
						.getAllBackupEntriesInfo();
				if (!Util.isCollectionEmpty(allBackupEntriesInfo)) {
					for (BaseBackupEntryInfo entryInfo : allBackupEntriesInfo) {
						BaseRestoreEntry restoreEntry = buildRestoreEntry(entryInfo, recordDir);
						if (restoreEntry == null) {
							continue;
						}

						switch (restoreEntry.getType()) {
							case TYPE_SYSTEM_APP :
								mRecord.addEntry(IRecord.GROUP_SYSTEM_APP, restoreEntry);
								break;
							case TYPE_USER_APP :
								mRecord.addEntry(IRecord.GROUP_USER_APP, restoreEntry);
								break;
							case TYPE_SYSTEM_LAUNCHER_DATA :
							case TYPE_SYSTEM_WIFI :
							case TYPE_SYSTEM_WALLPAPER :
							case TYPE_SYSTEM_RINGTONE :
								mRecord.addEntry(IRecord.GROUP_SYSTEM_DATA, restoreEntry);
								break;
							case TYPE_USER_CALL_HISTORY :
							case TYPE_USER_DICTIONARY :
							case TYPE_USER_GOLAUNCHER_SETTING :
							case TYPE_USER_CONTACTS :
							case TYPE_USER_MMS :
							case TYPE_USER_SMS :
							case TYPE_USER_BOOKMARK :
							case TYPE_USER_CALENDAR :
								mRecord.addEntry(IRecord.GROUP_USER_DATA, restoreEntry);
								break;
							case TYPE_USER_IMAGE :
								mRecord.addEntry(IRecord.GROUP_USER_IMAGE, restoreEntry);
								break;
							default :
								break;
						}
					}

					// 排序
					EntryComparator entryComparator = new EntryComparator();
					List<BaseEntry> userDataEntries = (List<BaseEntry>) mRecord
							.getGroup(IRecord.GROUP_USER_DATA);
					if (!Util.isCollectionEmpty(userDataEntries)) {
						Collections.sort(userDataEntries, entryComparator);
					}
					List<BaseEntry> systemDataEntries = (List<BaseEntry>) mRecord
							.getGroup(IRecord.GROUP_SYSTEM_DATA);
					if (!Util.isCollectionEmpty(systemDataEntries)) {
						Collections.sort(systemDataEntries, entryComparator);
					}
					List<BaseEntry> userAppEntries = (List<BaseEntry>) mRecord
							.getGroup(IRecord.GROUP_USER_APP);
					if (!Util.isCollectionEmpty(userAppEntries)) {
						Collections.sort(userAppEntries, entryComparator);
					}
					List<BaseEntry> systemAppEntries = (List<BaseEntry>) mRecord
							.getGroup(IRecord.GROUP_SYSTEM_APP);
					if (!Util.isCollectionEmpty(systemAppEntries)) {
						Collections.sort(systemAppEntries, entryComparator);
					}
				}
				Message.obtain(mHandler, MSG_INIT_LIST_ADAPTER, mRecord).sendToTarget();
			}
		}).start();
	}
	
	private void initListAdapter(RestorableRecord record) {
		mAdapter = new NetRestoreRecordAdapter(RestoreOnlineBackupActivity.this, record);
		new Thread(new Runnable() {
			@Override
			public void run() {
				((NetRestoreRecordAdapter) mAdapter).init();
				mAdapter.setOnEntrySelectedListener(RestoreOnlineBackupActivity.this);
				Message.obtain(mHandler, MSG_INIT_LIST_VIEW).sendToTarget();
			}
		}).start();
	}

	private BaseRestoreEntry buildRestoreEntry(BaseBackupEntryInfo entryInfo, File recordDir) {
		EntryType type = entryInfo.type;
		if (type == null) {
			return null;
		}

		BaseRestoreEntry restoreEntry = null;
		final String recordDirPath = recordDir.getAbsolutePath();
		switch (type) {
			case TYPE_USER_SMS :
				restoreEntry = new SmsRestoreEntry(this, recordDirPath);
				break;
			case TYPE_USER_MMS :
				restoreEntry = new MmsRestoreEntry(this, recordDirPath);
				break;
			case TYPE_USER_CONTACTS :
				restoreEntry = new ContactsRestoreEntry(this, recordDirPath);
				break;
			case TYPE_USER_CALL_HISTORY :
				restoreEntry = new CallLogRestoreEntry(this, recordDirPath);
				break;
			case TYPE_USER_DICTIONARY :
				restoreEntry = new UserDictionaryRestoreEntry(this, recordDirPath);
				break;
			case TYPE_SYSTEM_WIFI :
				restoreEntry = new WifiRestoreEntry(this, recordDirPath,
						mRecord.getRecordDescribe().mWifiPath);
				break;
			case TYPE_USER_GOLAUNCHER_SETTING :
				restoreEntry = new GoLauncherSettingRestoreEntry(this, recordDirPath);
				break;
			case TYPE_SYSTEM_LAUNCHER_DATA :
				restoreEntry = new LauncherDataRestoreEntry(this, recordDirPath,
						((LauncherDataEntryInfo) entryInfo).launcherDataExtraInfo);
				break;
			case TYPE_USER_APP :
			case TYPE_SYSTEM_APP :
				AppInfo appInfo = ((AppBackupEntryInfo) entryInfo).appInfo;
				restoreEntry = new AppRestoreEntry(appInfo, recordDirPath);
				break;
			case TYPE_SYSTEM_RINGTONE :
				restoreEntry = new RingtoneRestoreEntry(this, recordDirPath);
				break;
			case TYPE_SYSTEM_WALLPAPER :
				restoreEntry = new WallpaperRestoreEntry(this, recordDirPath);
				break;
			case TYPE_USER_BOOKMARK :
				restoreEntry = new BookMarkRestoreEntry(this, recordDirPath);
				break;
			case TYPE_USER_CALENDAR :
				restoreEntry = new CalendarRestoreEntry(this, recordDirPath);
				break;
			//照片回复
			case TYPE_USER_IMAGE :
				return new ImageRestoreEntry(this, mRecordCacheDbHelper, recordDirPath);
			default :
				break;
		}

		if (restoreEntry == null) {
			return null;
		}
		restoreEntry.setRestorableState(getRestoreEntryRestorableState(restoreEntry));
		return restoreEntry;
	}

	private RestorableState getRestoreEntryRestorableState(BaseRestoreEntry entry) {
		if (entry == null) {
			return null;
		}

		RestorableState state = null;
		if (entry.isNeedRootAuthority()) {
			boolean isRoot = RootShell.isRootValid();
			state = isRoot ? RestorableState.DATA_RESTORABLE : RestorableState.UNRESTORABLE;
		} else if (entry.getType() == EntryType.TYPE_USER_GOLAUNCHER_SETTING
				&& !PackageUtil.isPackageInstalled(this,
						GoLauncherSettingBackupEntry.GOLAUNCHER_PACKAGE_NAME)) {
			state = RestorableState.UNRESTORABLE;
		} else if (entry.getType() == EntryType.TYPE_USER_APP
				|| entry.getType() == EntryType.TYPE_SYSTEM_APP) {
			state = RestorableState.APP_DATA_RESTORABLE;
		} else if (entry.getType() == EntryType.TYPE_SYSTEM_LAUNCHER_DATA) {
			state = RestorableState.UNRESTORABLE;
			LauncherDataExtraInfo launcherDataExtraInfo = ((LauncherDataRestoreEntry) entry)
					.getLauncherDataExtraInfo();
			if (launcherDataExtraInfo == null) {
				return state;
			}

			List<ResolveInfo> resolveInfos = PackageUtil.getAppWithHomeAction(this);
			String packageName = launcherDataExtraInfo.packageName;
			int versionCode = launcherDataExtraInfo.versionCode != null ? Integer
					.valueOf(launcherDataExtraInfo.versionCode) : 0;
			if (packageName == null || versionCode == 0) {
				return state;
			}
			if (PackageUtil.isSystemAndLauncherApp(resolveInfos, packageName)
					&& PackageUtil.isAppWithSameVersionCode(this, packageName, versionCode)) {
				state = RestorableState.DATA_RESTORABLE;
			}
		} else {
			state = RestorableState.DATA_RESTORABLE;
		}
		return state;
	}

	private String getOnlineBackupDbPath() {
		return new File(mService.getOnlineBackupPath(), BackupDBHelper.getDBName())
				.getAbsolutePath();
	}

	private String getOnlineBackupFilePath(String fileName) {
		String dir = mService.getOnlineBackupPath();
		if (TextUtils.isEmpty(fileName)) {
			return dir;
		}
		File file = new File(fileName);
		if (!TextUtils.isEmpty(file.getParent())) {
			String imageDir = dir + File.separator
					+ fileName.substring(0, fileName.lastIndexOf(File.separator));
			String imageFile = fileName.substring(fileName.lastIndexOf(File.separator) + 1);
			return new File(imageDir, imageFile).getAbsolutePath();
		}
		return new File(dir, fileName).getAbsolutePath();
	}

	private void onNoOnlineBackup() {
		setContentView(R.layout.layout_no_online_backup);
		getWindow().setFormat(PixelFormat.RGBA_8888);
		initTitleBar();
	}

	private void onRecordCreated() {
		initRestoreBackupViews();
		mListView.setAdapter(mAdapter);
		boolean hasUserAppEntry = mAdapter.getChildrenCount(mAdapter
				.getGroupPositionByKey(IRecord.GROUP_USER_APP)) > 0;
		if (hasUserAppEntry) {
			setTabSortButtonVisible(true);
			setAppSelectorButtonVisible(true);
		} else {
			setTabSortButtonVisible(false);
			setAppSelectorButtonVisible(false);
		}
		initExpandableListView();
		Message.obtain(mHandler, MSG_DISMISS_DIALOG, mSpinnerProgressDialog).sendToTarget();
	}

	private void initRestoreBackupViews() {
		setContentView(R.layout.layout_record_detail);
		getWindow().setFormat(PixelFormat.RGBA_8888);

		initTitleBar();

		mListView = (ExpandableListView) findViewById(R.id.listview);
		mListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
					int childPosition, long id) {
				BaseEntry entry = mAdapter.getEntry(groupPosition, childPosition);
				if (entry instanceof GroupRestoreEntry) {
					getImageRestoreDialog(entry, parent, v, groupPosition, childPosition);
				} else {
					mAdapter.toggleEntry(groupPosition, childPosition);
					// 更新子视图状态
					mAdapter.updateAdapterChildItemView(v, groupPosition, childPosition);
					// 更新组视图状态
					long packedPos = ExpandableListView.getPackedPositionForGroup(groupPosition);
					int flatPos = mListView.getFlatListPosition(packedPos);
					int firstVisiblePos = mListView.getFirstVisiblePosition();
					int lastVisiblePos = mListView.getLastVisiblePosition();
					if (flatPos >= firstVisiblePos && flatPos <= lastVisiblePos) {
						final ExpandableListAdapter adapter = parent.getExpandableListAdapter();
						View convertView = mListView.getChildAt(flatPos - firstVisiblePos);
						((RecordDetailListAdpater) adapter).updateAdapterGroupItemView(convertView,
								groupPosition);
					}
				}
				return true;
			}
		});

		mRestoreButton = (Button) findViewById(R.id.operation_btn);
		mRestoreButton.setEnabled(false);
		mRestoreButton.setText(getString(R.string.btn_start_restore));
		mRestoreButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// generateRestoreTaskDb();
				handleRestoreButtonClick();
			}
		});
	}

	private void handleRestoreButtonClick() {
		// 检查wifi
		if (Util.isWifiEnable(this)) {
			generateRestoreTaskDb();
			return;
		}

		// wifi没有开启，弹出提示
		showEnableWifiAlertDialog();
	}

	//抛出照片对话框
	public void getImageRestoreDialog(final BaseEntry entry, final ExpandableListView parent,
			final View v, final int groupPosition, final int childPosition) {
		if (entry == null) {
			return;
		}
		entry.setOnSelectedChangeListener(null);
		entry.setOnSelectedChangeListener(RestoreOnlineBackupActivity.this);
		CustomImageAlertDialog.showImageDialog(RestoreOnlineBackupActivity.this, entry, parent, v,
				groupPosition, childPosition, mAdapter, mListView, mRecordCacheDbHelper,
				mDefaultImage);
	}

	private void initTitleBar() {
		TextView title = (TextView) findViewById(R.id.title);
		title.setText(getText(R.string.title_restore));

		View returnButton = findViewById(R.id.return_btn);
		returnButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mRecord != null) {
					mRecord.clear();
				}
				finish();
			}
		});

		mSortAppsButton = (ImageButton) findViewById(R.id.sort_btn);
		if (mSortAppsButton != null) {
			mSortAppsButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showAppSortTypePickerDialog();
				}
			});
		}

		mAppSelectorButton = (ImageButton) findViewById(R.id.app_selector);
		if (mAppSelectorButton != null) {
			mAppSelectorButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mTempAppSelector = new AppInstallStateSelector(mAppSelector);
					showAppPickerDialog();
				}
			});
		}
	}

	private void initExpandableListView() {
		final RecordDetailListAdpater adpater = (RecordDetailListAdpater) mListView
				.getExpandableListAdapter();
		if (adpater == null || adpater.isEmpty()) {
			return;
		}
		// 勾选全部用户数据项
		adpater.checkGroupAllEntries(adpater.getGroupPositionByKey(IRecord.GROUP_USER_DATA), true);

		// 排序
		sortAppEntries(SORT_TYPE.SORT_ONLINE_BY_APP_NAME);
		if (mSortAppsButton != null) {
			mSortAppsButton.setImageResource(R.drawable.sort_by_name);
		}

		int userDataGroupPos = adpater.getGroupPositionByKey(IRecord.GROUP_USER_DATA);
		if (userDataGroupPos >= 0) {
			mListView.expandGroup(userDataGroupPos);
		}

		int systemDataGroupPos = adpater.getGroupPositionByKey(IRecord.GROUP_SYSTEM_DATA);
		if (systemDataGroupPos >= 0) {
			mListView.expandGroup(systemDataGroupPos);
		}

		int userAppGroupPos = adpater.getGroupPositionByKey(IRecord.GROUP_USER_APP);
		if (/* mIsRootUser && */userAppGroupPos >= 0) {
			mListView.expandGroup(userAppGroupPos);
		}

		int systemAppGroupPos = adpater.getGroupPositionByKey(IRecord.GROUP_SYSTEM_APP);
		if (/* mIsRootUser && */systemAppGroupPos >= 0) {
			mListView.expandGroup(systemAppGroupPos);
		}

		adpater.setOnAdapterItemUpdateListener(new OnAdapterItemUpdateListener() {
			@Override
			public void onAdapterGroupItemUpdate(BaseExpandableListAdapter adapter, int groupPos) {
				if (mListView == null) {
					return;
				}
				long packedPos = ExpandableListView.getPackedPositionForGroup(groupPos);
				int flatPos = mListView.getFlatListPosition(packedPos);
				int firstVisiblePos = mListView.getFirstVisiblePosition();
				int lastVisiblePos = mListView.getLastVisiblePosition();
				if (flatPos >= firstVisiblePos && flatPos <= lastVisiblePos) {
					View convertView = mListView.getChildAt(flatPos - firstVisiblePos);
					adpater.updateAdapterGroupItemView(convertView, groupPos);
				}
			}

			@Override
			public void onAdapterChildItemUpdate(BaseExpandableListAdapter adapter, int groupPos,
					int childPos) {
				if (mListView == null) {
					return;
				}
				long packedPos = ExpandableListView.getPackedPositionForChild(groupPos, childPos);
				int flatPos = mListView.getFlatListPosition(packedPos);
				int firstVisiblePos = mListView.getFirstVisiblePosition();
				int lastVisiblePos = mListView.getLastVisiblePosition();
				if (flatPos >= firstVisiblePos && flatPos <= lastVisiblePos) {
					View convertView = mListView.getChildAt(flatPos - firstVisiblePos);
					adpater.updateAdapterChildItemView(convertView, groupPos, childPos);
				}
			}
		});
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
					synchronized (RestoreOnlineBackupActivity.this) {
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
		.setMultiChoiceItems(R.array.app_install_state, getCurrentAppSelector(),
				new DialogInterface.OnMultiChoiceClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which, boolean isChecked) {
						int target = -1;
						switch (which) {
							case 0 :
								target = AppInstallStateSelector.UNINSTALLED;
								break;
							case 1 :
								target = AppInstallStateSelector.UPDATABLE;
								break;
							case 2 :
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
				if (mAdapter.hasChildItemSelected()) {
					updateRestoreButton(true);
				} else {
					updateRestoreButton(false);
				}

				// 更新选中备份项大小
				mTotalBackupSize = mAdapter.getSelectedEntriesSpaceUsage();
				updateRestoreButtonText(mTotalBackupSize);
				
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
					.setMessage(R.string.restore_advice_of_wifi)
					.setPositiveButton(R.string.open_wifi, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// WifiManager wifiManager = (WifiManager)
							// NewOnlineBackupActivity.this.getSystemService(Context.WIFI_SERVICE);
							// wifiManager.setWifiEnabled(true);
							Intent it = new Intent();
							it.setAction(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
							startActivity(it);
							dialog.dismiss();
						}
					})
					.setNegativeButton(R.string.direct_restore,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									generateRestoreTaskDb();
									dialog.dismiss();
								}
							}).create();
		}
		showDialog(mEnableWifiDialog);
	}

	private static final int MSG_INIT_LISTVIEW = 0x1001;
	private static final int MSG_SHOW_PROGRESS_DIALOG = 0x1002;
	private static final int MSG_DISMISS_DIALOG = 0x1003;
	private static final int MSG_SHOW_TOAST = 0x1004;
	private static final int MSG_DOWNLOAD_DB_FILE = 0x1005;
	private static final int MSG_START_PROCESS_ACTIVITY = 0x1006;
	private static final int MSG_CREATE_RESTORE_RECORD = 0x1007;
	private static final int MSG_NOTIFY_DATASET_CHANGE = 0x1008;
	private static final int MSG_INIT_LIST_VIEW = 0x1009;
	private static final int MSG_ON_NO_ONLINE_BACKUP = 0x100a;
	private static final int MSG_INIT_LIST_ADAPTER = 0x100b;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_INIT_LISTVIEW :
					break;
				case MSG_SHOW_PROGRESS_DIALOG :
					if (msg.obj != null) {
						showSpinnerProgressDialog(msg.obj.toString(), msg.arg2 > 0);
					}
					break;
				case MSG_DISMISS_DIALOG :
					dismissDialog((Dialog) msg.obj);
					break;
				case MSG_SHOW_TOAST :
					showToast(msg.obj.toString());
					break;
				case MSG_DOWNLOAD_DB_FILE :
					downloadOnlineBackupDbFile();
					break;
				case MSG_START_PROCESS_ACTIVITY :
					startProcessActivity();
					break;
				case MSG_CREATE_RESTORE_RECORD :
					// 在cache目录重建record
					createRestoreRecord(getCacheDir());
					break;
				case MSG_NOTIFY_DATASET_CHANGE :
					if (mAdapter != null && !mAdapter.isEmpty()) {
						((BaseExpandableListAdapter) mAdapter).notifyDataSetChanged();
					}
					break;
				case MSG_INIT_LIST_VIEW :
					onRecordCreated();
					break;
				case MSG_ON_NO_ONLINE_BACKUP :
					onNoOnlineBackup();
					break;
				case MSG_INIT_LIST_ADAPTER:
					initListAdapter((RestorableRecord) msg.obj);
					break;
				default :
					break;
			}
		}
	};

	private void createRestoreTaskDb() {
		List<BaseEntry> selectedEntries = mRecord.getSelectedEntries();
		if (Util.isCollectionEmpty(selectedEntries)) {
			return;
		}
		BackupDBHelper dbHelper = mRecord.getBackupDBHelper(this);
		if (dbHelper == null) {
			return;
		}

		NetSyncTaskDbHelper taskDbHelper = new NetSyncTaskDbHelper(this,
				NetSyncTaskDbHelper.getDbName(), NetSyncTaskDbHelper.getDbVersion());
		taskDbHelper.clear();

		for (BaseEntry entry : selectedEntries) {
			BaseBackupEntryInfo entryInfo = null;
			if (entry instanceof AppRestoreEntry) {
				entryInfo = dbHelper
						.getAppEntryInfo(((AppRestoreEntry) entry).getAppInfo().packageName);
			} else if (entry instanceof OneImageRestoreEntry) {
				entryInfo = dbHelper.getImageEntryInfo(entry);
			} else {
				entryInfo = dbHelper.getSystemDataEntryInfo(entry.getType());
			}
			if (entryInfo == null) {
				continue;
			}
			Task task = new Task();
			task.taskType = TaskType.ONLINE_RESTORE;
			task.taskState = TaskState.NOT_START;
			task.time = entryInfo.backupDate;
			task.taskObject = TaskObject.valueOf(entryInfo.type);
			if (entryInfo instanceof AppBackupEntryInfo) {
				AppInfo appInfo = ((AppBackupEntryInfo) entryInfo).appInfo;
				task.extraInfo = new Object[] { appInfo.appName, appInfo.packageName,
						appInfo.appType };
			} else if (entryInfo instanceof ImageBackupEntryInfo) {
				ImageBackupEntryInfo imageInfo = (ImageBackupEntryInfo) entryInfo;
				ImageBean image = imageInfo.mImage;
				task.extraInfo = new Object[] { image.mImageDisplayName, image.mImageSize,
						image.mImageParentFilePath };
			} else if (entryInfo instanceof LauncherDataEntryInfo) {
				LauncherDataEntryInfo launcherDataEntryInfo = (LauncherDataEntryInfo) entryInfo;
				LauncherDataExtraInfo launcherDataExtraInfo = launcherDataEntryInfo.launcherDataExtraInfo;
				if (launcherDataExtraInfo != null) {
					task.extraInfo = new Object[] { launcherDataEntryInfo.launcherDataExtraInfo.packageName };
				}
			} else if (task.taskObject == TaskObject.WIFI) {
				task.extraInfo = new Object[] { entryInfo.count,
						mRecord.getRecordDescribe().mWifiPath };
			} else {
				task.extraInfo = new Object[] { entryInfo.count };
			}
			int pathCount = entryInfo.backupFileName.length;
			if (pathCount > 0) {
				task.paths = new String[pathCount];
				for (int i = 0; i < pathCount; i++) {
					task.paths[i] = getOnlineBackupFilePath(entryInfo.backupFileName[i]);
				}
			}
			taskDbHelper.addTask(task);
		}
		taskDbHelper.close();
		File taskDbFile = new File(getCacheDir(), NetSyncTaskDbHelper.getDbName());
		Util.copyFile(getDatabasePath(NetSyncTaskDbHelper.getDbName()).getAbsolutePath(),
				taskDbFile.getAbsolutePath());
		if (taskDbFile.exists()) {
			mSyncTaskDBCacheFile = taskDbFile;
		}
		deleteDatabase(NetSyncTaskDbHelper.getDbName());
		mRecord.releaseDBHelper(dbHelper);
	}

	private void generateRestoreTaskDb() {
		if (mOnlineBackupDBCacheFile == null) {
			return;
		}
		showSpinnerProgressDialog(getString(R.string.msg_wait), false);
		new Thread(new Runnable() {
			@Override
			public void run() {
				createRestoreTaskDb();
				Message.obtain(mHandler, MSG_DISMISS_DIALOG, mSpinnerProgressDialog).sendToTarget();
				Message.obtain(mHandler, MSG_START_PROCESS_ACTIVITY).sendToTarget();
			}
		}).start();
	}

	private void startProcessActivity() {
		Intent intent = new Intent(RestoreOnlineBackupActivity.this,
				RestoreOnlineBackupProcessAcitvity.class);
		intent.putExtra(RestoreOnlineBackupProcessAcitvity.EXTRA_TASK_DB_FILE, mSyncTaskDBCacheFile);
		startActivity(intent);
		finish();
	}

	private void updateRestoreButton(boolean enabled) {
		if (mRestoreButton.isEnabled() != enabled) {
			mRestoreButton.setEnabled(enabled);
		}
	}
	
	private void updateRestoreButtonText(long totalEntiresSize) {
		String str = mRestoreButton.getText().toString();
		if (str.contains("(")) {
			str = str.substring(0, str.indexOf("("));
		}
		if (mTotalBackupSize != 0) {
			str += "(" + Util.formatFileSize(totalEntiresSize) + ")";
			// 字体变色处理
		}
		mRestoreButton.setText(str);
	}

	private void updateRestoreButtonWhenEntrySelectedStateChanged(BaseEntry entry,
			boolean isSelected) {
		if (entry == null) {
			return;
		}
		if (mRestoreButton == null) {
			return;
		}

		if (isSelected) {
			mTotalBackupSize += entry.getSpaceUsage();
		} else {
			mTotalBackupSize -= entry.getSpaceUsage();
		}

		updateRestoreButtonText(mTotalBackupSize);
	}

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

	@Override
	public void onSelectedChange(BaseEntry entry, boolean isSelected) {
		if (!isSelected) {
			if (!mAdapter.hasChildItemSelected()) {
				updateRestoreButton(false);
			}
		} else {
			updateRestoreButton(true);
		}
		updateRestoreButtonWhenEntrySelectedStateChanged(entry, isSelected);
	}

	@Override
	protected void onDestroy() {
		// 删除cache里面的原始BackupDb
		File cacheDb = new File(getCacheDir(), BackupDBHelper.getDBName());
		Util.deleteFile(cacheDb.getAbsolutePath());

		if (mRecordCacheDbHelper != null) {
			mRecord.releaseDBHelper(mRecordCacheDbHelper);
			mRecordCacheDbHelper = null;
		}
		if (mRecord != null) {
			mRecord.clear();
		}
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
		super.onDestroy();
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

	private boolean[] getCurrentAppSelector() {
		if (mAppSelector == null) {
			return null;
		}
		boolean[] selector = new boolean[] { mAppSelector.isUninstalledSelected(),
				mAppSelector.isUpdatableSelected(), mAppSelector.isInstalledSelected() };
		return selector;
	}

	private void selectAppEntries(RestoreAppState state, boolean selected) {
		final NetRestoreRecordAdapter adpater = (NetRestoreRecordAdapter) mAdapter;
		adpater.selectUserAppEntry(state, selected);
//		adpater.selectSystemAppEntry(state, selected);
	}
}
