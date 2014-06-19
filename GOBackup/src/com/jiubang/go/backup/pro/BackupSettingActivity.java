package com.jiubang.go.backup.pro;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.jiubang.go.account.GAccountManager;
import com.jiubang.go.account.IAccountCallBack;
import com.jiubang.go.account.activitys.NetworkSettingActivity;
import com.jiubang.go.account.common.GProtocol;
import com.jiubang.go.account.common.RequestDefin;
import com.jiubang.go.account.data.AccountUser;
import com.jiubang.go.account.data.ExtraInfo;
import com.jiubang.go.account.data.GAccessToken;
import com.jiubang.go.account.data.GToken;
import com.jiubang.go.account.util.AccessTokenUtil;
import com.jiubang.go.account.util.NetCheck;
import com.jiubang.go.account.util.TokenUtil;
import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.data.DefineData;
import com.jiubang.go.backup.pro.data.FeedBackInfo;
import com.jiubang.go.backup.pro.model.BackupManager;
import com.jiubang.go.backup.pro.model.RootShell;
import com.jiubang.go.backup.pro.net.sync.AccountInfo;
import com.jiubang.go.backup.pro.net.sync.CloudServiceManager;
import com.jiubang.go.backup.pro.net.sync.FileHostingServiceProvider;
import com.jiubang.go.backup.pro.net.sync.FileHostingServiceProvider.ActionListener;
import com.jiubang.go.backup.pro.net.version.VersionChecker;
import com.jiubang.go.backup.pro.net.version.VersionInfo;
import com.jiubang.go.backup.pro.product.manage.ProductManager;
import com.jiubang.go.backup.pro.product.manage.ProductPayInfo;
import com.jiubang.go.backup.pro.statistics.StatisticsDataManager;
import com.jiubang.go.backup.pro.statistics.StatisticsDataManager.FunctionState;
import com.jiubang.go.backup.pro.statistics.StatisticsKey;
import com.jiubang.go.backup.pro.ui.ExtraTextPreference;
import com.jiubang.go.backup.pro.ui.SingleButtonPreference;
import com.jiubang.go.backup.pro.ui.SingleChoicePreference;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 备份设置功能模块，继承PreferenceActivity,使用系统标准设置界面
 *
 * @author ReyZhang, mainyongshen
 */

public class BackupSettingActivity extends PreferenceActivity implements IAccountCallBack {
	private static final String URL_3G_CN = "http://3g.cn";
	private static final int WEBVIEW_REQUEST = 1006;
	private static final int WAIT_TIME = 800;

	private ProgressDialog mSpinnerProgressDialog;
	private Dialog mDeleteCloudBackupAlertDialog;
	private Dialog mLogoutAlertDialog;
	private Dialog mSelectBackupPathDialog;
	private Dialog mBackupPathChangeAlertDialog;
	private BackupPathAdapter mAdapter;

	private boolean mIsRootUser;
	// 设置备份路径
	private Preference mSettingBackupPath;
	// 批量删除备份
	private Preference mDeleteBackupedRecords;
	// 只备份有号码的联系人
	private CheckBoxPreference mOnlyBackupContactsHasNumber;
	// 合并重复联系人
	private CheckBoxPreference mMergeDuplicateContacts;
	// 联系人变更推送通知
	private CheckBoxPreference mContactChangePush;
	// 备份联系头像
	private CheckBoxPreference mBackupContactsAvatar;
	// Roo权限说明
	private Preference mRootIntroduction;
	// 备份/恢复应用程序数据
	private CheckBoxPreference mBackupOrRestoreAppData;
	// 静默安装模式(1.04版本已去除该选项)
	// private CheckBoxPreference mSilentlyInstallApp;
	// 注销帐号
	// private Preference mLogout;
	// 个人中心
	private Preference mUserCenter;
	// 反馈问题
	//	private Preference mFeedback;
	// 应用升级
	private Preference mApplicationUpdate;
	// 版本信息
	//	private Preference mVersionInfo;
	// 软件评分
	private Preference mRateSoft;
	// 版权信息
	private Preference mCopyrightInfo;
	// 云端备份账号管理
	private SingleChoicePreference mCloudServiceSelector;
	private SingleButtonPreference mCloudServiceAccountManager;
	// 清除云端备份
	private Preference mDeleteCloudBackup;
	// 高级版
//		private Preference mAdvanceUpdate;

	private PreferenceManager mPreferenceManager;
	private StatisticsDataManager mStatisticsDataManager;

	// 付费product
	private ProductPayInfo mGoBackupProduct;

	// GO账号相关
	private GAccountManager mAccountManager = null;
	private String mAppkey = null;
	private String mTicket = null;
	private String mAccessToken = null;
	private String mConsumerSecret = DefineData.BACKUP_CONSUMER_SECRET;

	private ExtraInfo mGoLauncherVirtualIMEI = new ExtraInfo();
	private String mSdCardRootPath;

	private AccountInfo mCloudServiceAccount = null;
	private FileHostingServiceProvider mService;
	private boolean mStartAuthentication = false;

	private boolean mStopped;

	private static final int MSG_SHOW_PROGRESS_DIALOG = 0x1001;
	private static final int MSG_DISMISS_DIALOG = 0x1002;
	private static final int MSG_SHOW_TOAST = 0x1003;
	private static final int MSG_SD_MOUNT = 0x1004;
	private static final int MSG_SD_UNMOUNT = 0x1005;
	private static final int MSG_SHOW_BACKUP_PATH_CHANGE_DIALOG = 0x1006;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_SHOW_PROGRESS_DIALOG :
					showSpinnerProgressDialog(msg.arg1, msg.arg2 > 0);
					break;
				case MSG_DISMISS_DIALOG :
					dismissDialog((Dialog) msg.obj);
					break;
				case MSG_SHOW_TOAST :
					showToast(msg.obj.toString());
					break;
				case MSG_SD_MOUNT :
					resetBackupRootPath();
					updateSettingBackupPathPreference();
					updateDeleteBackupPreference();
					BackupManager.getInstance().init(BackupSettingActivity.this);
					break;
				case MSG_SD_UNMOUNT :
					//					Log.d("GOBackup", "mHandler : MSG_SD_UNMOUNT");
					resetBackupRootPath();
					updateSettingBackupPathPreference();
					updateDeleteBackupPreference();
					BackupManager.getInstance().releaseRestoreRecords();
					break;
				case MSG_SHOW_BACKUP_PATH_CHANGE_DIALOG :
					String tips = msg.obj != null ? (String) msg.obj : null;
					showBackupPathChangeAlertDialog(tips);
					break;
			}
		}
	};

	private void resetBackupRootPath() {
		String preferenceSdRootPath = Util.getSdRootPathOnPreference(this);
		String validSdRootPath = Util.getDefalutValidSdPath(this);
		//		Log.d("GOBackup", "resetBackupRootPath : preferenceSdRootPath = " + preferenceSdRootPath + ", validSdRootPath = " + validSdRootPath);
		if (validSdRootPath == null) {
			// 没有sd卡
			if (mPreferenceManager.isNoneSdCardAlertEnabled(this)) {
				Message.obtain(mHandler, MSG_SHOW_BACKUP_PATH_CHANGE_DIALOG,
						getString(R.string.msg_no_sd)).sendToTarget();
				mPreferenceManager.enableShowNoneSdCardAlert(this, false);
			}
			PreferenceManager.getInstance().putString(this, PreferenceManager.KEY_BACKUP_SD_PATH,
					"");
			return;
		}

		if (TextUtils.equals(preferenceSdRootPath, validSdRootPath)) {
			// 选择的路径存在
			return;
		}

		// 选择的路径的sd被移除
		if (TextUtils.isEmpty(preferenceSdRootPath)) {
			if (Util.isInternalSdPath(validSdRootPath)) {
				Message.obtain(
						mHandler,
						MSG_SHOW_BACKUP_PATH_CHANGE_DIALOG,
						getString(R.string.msg_reset_backup_path_tips,
								getString(R.string.msg_internal_storage))).sendToTarget();
			} else {
				Message.obtain(
						mHandler,
						MSG_SHOW_BACKUP_PATH_CHANGE_DIALOG,
						getString(R.string.msg_reset_backup_path_tips,
								getString(R.string.msg_external_storage))).sendToTarget();
			}
			PreferenceManager.getInstance().putString(this, PreferenceManager.KEY_BACKUP_SD_PATH,
					validSdRootPath);
			return;
		}

		if (Util.isInternalSdPath(preferenceSdRootPath)) {
			// 内置sd卡被移除
			Message.obtain(
					mHandler,
					MSG_SHOW_BACKUP_PATH_CHANGE_DIALOG,
					getString(R.string.msg_backup_path_change_tips,
							getString(R.string.msg_internal_storage),
							getString(R.string.msg_external_storage))).sendToTarget();
		} else {
			// 外置sd卡被移除
			Message.obtain(
					mHandler,
					MSG_SHOW_BACKUP_PATH_CHANGE_DIALOG,
					getString(R.string.msg_backup_path_change_tips,
							getString(R.string.msg_external_storage),
							getString(R.string.msg_internal_storage))).sendToTarget();
		}
		PreferenceManager.getInstance().putString(this, PreferenceManager.KEY_BACKUP_SD_PATH,
				validSdRootPath);
	}

	private BroadcastReceiver mLogoutReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BaseActivity.ACTION_LOGOUT.equals(action)) {
				finish();
			}
		}
	};

	private BroadcastReceiver mSDCardStateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (Intent.ACTION_MEDIA_EJECT.equals(action)) {
				//				Log.d("GOBackup", "BackupSettingActivity : ACTION_MEDIA_EJECT");
				if (mHandler != null) {
					if (mHandler.hasMessages(MSG_SD_UNMOUNT)) {
						mHandler.removeMessages(MSG_SD_UNMOUNT);
					}
					Message msg = Message.obtain(mHandler, MSG_SD_UNMOUNT);
					mHandler.sendMessageDelayed(msg, WAIT_TIME);
				}
			} else if (Intent.ACTION_MEDIA_REMOVED.equals(action)) {
				//				Log.d("GOBackup", "BackupSettingActivity : ACTION_MEDIA_REMOVED");
			} else if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
				//				Log.d("GOBackup", "BackupSettingActivity : ACTION_MEDIA_MOUNTED");
				mPreferenceManager.enableShowNoneSdCardAlert(context, true);
				Message.obtain(mHandler, MSG_SD_MOUNT).sendToTarget();
			} else if (Intent.ACTION_MEDIA_UNMOUNTED.equals(action)) {
				//				Log.d("GOBackup", "BackupSettingActivity : ACTION_MEDIA_UNMOUNTED");
				Message.obtain(mHandler, MSG_SD_UNMOUNT).sendToTarget();
			}
		}
	};

	public IntentFilter getSdCardReceiverIntentFilter() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_MEDIA_REMOVED);
		filter.addAction(Intent.ACTION_MEDIA_EJECT);
		filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		filter.addDataScheme("file");
		return filter;
	}

	private BroadcastReceiver mVersionUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			VersionInfo versionInfo = intent.getParcelableExtra(VersionChecker.EXTRA_VERSION_INFO);
			String message = intent.getStringExtra(VersionChecker.EXTRA_MESSAGE);
			if (VersionChecker.ACTION_NEW_UPDATE.equals(action)) {
				VersionChecker.showUpdateInfoDialog(BackupSettingActivity.this, versionInfo);
			} else if (VersionChecker.ACTION_FORCE_UPDATE.equals(action)) {
				VersionChecker.showForceUpdateDialog(BackupSettingActivity.this, versionInfo);
			} else if (VersionChecker.ACTION_SHOW_UPDATE_TIP.equals(action)) {
				VersionChecker.showTipDialog(BackupSettingActivity.this, message);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mPreferenceManager = PreferenceManager.getInstance();
		mStatisticsDataManager = StatisticsDataManager.getInstance();

		final CloudServiceManager csm = CloudServiceManager.getInstance();
		mService = csm.getCurrentService();

		mIsRootUser = isRootUser();

		//		mAccountManager = GAccountManager.getInstance(getApplicationContext());
		//		mAppkey = mAccountManager.getAppKey(this);

		registerReceiver(mLogoutReceiver, new IntentFilter(BaseActivity.ACTION_LOGOUT));
		registerReceiver(mSDCardStateReceiver, getSdCardReceiverIntentFilter());

		mGoLauncherVirtualIMEI.setLauncherid(Util.getGoLauncherVirtualIMEI(this));
		mGoBackupProduct = ProductManager.getProductPayInfo(getApplicationContext(),
				ProductPayInfo.PRODUCT_ID);

		initPreference();

		if (mService == null && mCloudServiceSelector != null) {
			int type = Integer.parseInt(mCloudServiceSelector.getValue());
			mService = csm.switchService(this, type);
		}
	}

	private void initPreference() {
		if (Util.isInland(this)) {
			addPreferencesFromResource(R.xml.go_backup_setting_preference_inland);
		} else {
			addPreferencesFromResource(R.xml.go_backup_setting_preference);
		}

		mSettingBackupPath = findPreference(getString(R.string.key_setting_backup_path));
		if (mSettingBackupPath != null) {
			mSettingBackupPath.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					showSelectBackupPathDialog();
					return true;
				}
			});
		}
		updateSettingBackupPathPreference();

		mDeleteBackupedRecords = findPreference(getString(R.string.key_delete_backuped_records));
		updateDeleteBackupPreference();
		if (mDeleteBackupedRecords != null) {
			mDeleteBackupedRecords.setSummary(R.string.entry_summary_delete_all_backup);
			mDeleteBackupedRecords.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					startBatchDeleteRecordsActivity();
					StatisticsDataManager.getInstance().increaseStatisticInt(
							BackupSettingActivity.this, StatisticsKey.SETTING_BATCH_DELETE, 1);
					return true;
				}
			});
		}

		mOnlyBackupContactsHasNumber = (CheckBoxPreference) findPreference(getString(R.string.key_only_backup_contacts_which_has_number));
		if (mOnlyBackupContactsHasNumber != null) {
			mOnlyBackupContactsHasNumber.setChecked(mPreferenceManager.getBoolean(this,
					PreferenceManager.KEY_ONLAY_BACKUP_CONTACT_HAS_NUMBER, true));
			mOnlyBackupContactsHasNumber
					.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
						@Override
						public boolean onPreferenceChange(Preference preference, Object newValue) {
							mPreferenceManager.putBoolean(BackupSettingActivity.this,
									PreferenceManager.KEY_ONLAY_BACKUP_CONTACT_HAS_NUMBER,
									(Boolean) newValue);
							mStatisticsDataManager.updateStatisticBoolean(
									BackupSettingActivity.this,
									StatisticsKey.CONTACTS_BACKUP_SETTING, (Boolean) newValue);
							return true;
						}
					});
		}

		mMergeDuplicateContacts = (CheckBoxPreference) findPreference(getString(R.string.key_merge_duplicate_contacts));
		if (mMergeDuplicateContacts != null) {
			mMergeDuplicateContacts.setChecked(mPreferenceManager.getBoolean(this,
					PreferenceManager.KEY_DISCARD_DUPLICATE_CONTACTS, true));
			mMergeDuplicateContacts.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					mPreferenceManager.putBoolean(BackupSettingActivity.this,
							PreferenceManager.KEY_DISCARD_DUPLICATE_CONTACTS, (Boolean) newValue);
					mStatisticsDataManager.updateStatisticBoolean(BackupSettingActivity.this,
							StatisticsKey.MERGE_CONTACTS, (Boolean) newValue);
					return true;
				}
			});
		}

		mContactChangePush = (CheckBoxPreference) findPreference(getString(R.string.key_contact_change_push));
		if (mContactChangePush != null) {
			mContactChangePush.setChecked(mPreferenceManager.getBoolean(this,
					PreferenceManager.KEY_CONTACT_CHANGE_PUSH, true));
			mContactChangePush.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					mPreferenceManager.putBoolean(BackupSettingActivity.this,
							PreferenceManager.KEY_CONTACT_CHANGE_PUSH, (Boolean) newValue);
					mStatisticsDataManager.updateStatisticBoolean(BackupSettingActivity.this,
							StatisticsKey.CONTACT_CHANGE_PUSH, (Boolean) newValue);
					return true;
				}
			});
		}

		mBackupContactsAvatar = (CheckBoxPreference) findPreference(getString(R.string.key_backup_contacts_avatar));
		if (mBackupContactsAvatar != null) {
			mBackupContactsAvatar.setChecked(mPreferenceManager.getBoolean(this,
					PreferenceManager.KEY_BACKUP_CONTACTS_PHOTO, true));
			mBackupContactsAvatar.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					mPreferenceManager.putBoolean(BackupSettingActivity.this,
							PreferenceManager.KEY_BACKUP_CONTACTS_PHOTO, (Boolean) newValue);
					return true;
				}
			});
		}

		mRootIntroduction = findPreference(getString(R.string.key_root_introduction));
		if (mRootIntroduction != null) {
			mRootIntroduction.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					Intent intent = new Intent(BackupSettingActivity.this,
							RootIntroductionActivity.class);
					startActivity(intent);
					return true;
				}
			});
		}

		mBackupOrRestoreAppData = (CheckBoxPreference) findPreference(getString(R.string.key_backup_restore_app_data));
		if (mBackupOrRestoreAppData != null) {
			if (!mIsRootUser) {
				mBackupOrRestoreAppData.setEnabled(false);
				mBackupOrRestoreAppData.setSummary(R.string.entry_summary_not_root);
			} else {
				mBackupOrRestoreAppData.setEnabled(true);
				mBackupOrRestoreAppData.setChecked(mPreferenceManager.getBoolean(this,
						PreferenceManager.KEY_SHOULD_BACKUP_RESTORE_APP_DATA, true));
			}
			mBackupOrRestoreAppData.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					mPreferenceManager.putBoolean(BackupSettingActivity.this,
							PreferenceManager.KEY_SHOULD_BACKUP_RESTORE_APP_DATA,
							(Boolean) newValue);
					FunctionState state = (Boolean) newValue
							? FunctionState.TRUE
							: FunctionState.FALSE;
					mStatisticsDataManager.updateStatisticInt(BackupSettingActivity.this,
							StatisticsKey.STATE_BACKUP_RESTORE_APP_DATA, state.ordinal());
					return true;
				}
			});
		}

		mUserCenter = findPreference(getString(R.string.key_user_center));
		if (mUserCenter != null) {
			mUserCenter.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					onUserCenter();
					return true;
				}
			});
		}

		//		mFeedback = findPreference(getString(R.string.key_feedback));
		//		if (mFeedback != null) {
		//			mFeedback.setOnPreferenceClickListener(new OnPreferenceClickListener() {
		//				@Override
		//				public boolean onPreferenceClick(Preference preference) {
		//					doCommandFeedback();
		//					return true;
		//				}
		//			});
		//		}

		mApplicationUpdate = findPreference(getString(R.string.key_version_info));
		if (mApplicationUpdate != null) {
			mApplicationUpdate.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					VersionChecker versionChecker = VersionChecker.getInstance();
					versionChecker.checkUpdate(BackupSettingActivity.this, false);
					versionChecker.planToCheckUpdateNextTime(BackupSettingActivity.this);
					StatisticsDataManager.getInstance().updateStatisticBoolean(
							BackupSettingActivity.this, StatisticsKey.HAS_CHECK_UPDATE, true);
					return true;
				}
			});
		}

//				mAdvanceUpdate = findPreference(getString(R.string.key_advanced_update));
//				if (mAdvanceUpdate != null) {
//					final boolean isPaid = mGoBackupProduct.isAlreadyPaid();
//					if (isPaid) {
//						mAdvanceUpdate.setTitle(getString(R.string.category_title_update_advanced));
//					} else {
//						mAdvanceUpdate.setTitle(getString(R.string.entry_title_advanced_update));
//					}
//					mAdvanceUpdate.setOnPreferenceClickListener(new OnPreferenceClickListener() {
//
//						@Override
//						public boolean onPreferenceClick(Preference preference) {
//							Intent intent = new Intent(BackupSettingActivity.this,
//									PayUpdateHelpActivity.class);
//							intent.putExtra(PayUpdateHelpActivity.EXTRA_IS_PAID, isPaid);
//							startActivity(intent);
//							return true;
//						}
//					});
//				}

		//		mVersionInfo = findPreference(getString(R.string.key_version_info));
		//		if (mVersionInfo != null) {
		//			try {
		//				PackageManager mPreferenceManager = getPackageManager();
		//				final String versionName = mPreferenceManager.getPackageInfo(getPackageName(), 0).versionName;
		//				mVersionInfo.setSummary(versionName);
		//			} catch (Exception e) {
		//				e.printStackTrace();
		//			}
		//		}

		mRateSoft = findPreference(getString(R.string.key_soft_rate));
		if (mRateSoft != null) {
			mRateSoft.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					VersionChecker.gotoMarket(BackupSettingActivity.this, null);
					return true;
				}
			});
		}

		mCopyrightInfo = findPreference(getString(R.string.key_copyright_info));
		if (mCopyrightInfo != null) {
			mCopyrightInfo.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL_3G_CN));
					try {
						startActivity(intent);
					} catch (Exception e) {
						e.printStackTrace();
					}
					return true;
				}
			});
		}

		mCloudServiceSelector = (SingleChoicePreference) findPreference(getString(R.string.key_cloud_service_selector));
		if (mCloudServiceSelector != null) {
			updateCloudServiceSelector();

			mCloudServiceSelector.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					int type = Integer.parseInt(newValue.toString());
					mService = CloudServiceManager.getInstance().switchService(
							BackupSettingActivity.this, type);
					CharSequence serviceName = getCloudServiceProviderName(type);
					if (serviceName != null) {
						String text = getString(R.string.preference_text_format, serviceName);
						((SingleChoicePreference) preference).setExtraText(text);
						updateAccountManagerPreference(type);
					}
					return true;
				}
			});
		}

		mCloudServiceAccountManager = (SingleButtonPreference) findPreference(getString(R.string.key_cloud_service_account_manager));
		if (mCloudServiceAccountManager != null) {
			if (mCloudServiceSelector != null) {
				updateAccountManagerPreference(Integer.parseInt(mCloudServiceSelector.getValue()));
			}
			mCloudServiceAccountManager
					.setOnPreferenceClickListener(new OnPreferenceClickListener() {
						@Override
						public boolean onPreferenceClick(Preference preference) {
							// 获取网盘类型
							if (mService == null) {
								mService = CloudServiceManager.getInstance().switchService(
										BackupSettingActivity.this, getActiveCloudServiceType());
							}
							if (mService != null && !mService.isSessionValid()) {
								mService.startAuthentication(BackupSettingActivity.this);
								mStartAuthentication = true;
							}
							return true;
						}
					});
			mCloudServiceAccountManager.setOnButtonClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showLogoutAlertDialog();
				}
			});
		}

		mDeleteCloudBackup = findPreference(getString(R.string.key_delete_cloud_backup));
		if (mDeleteCloudBackup != null) {
			updateDeleteCloudBackupPreference();
			mDeleteCloudBackup.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					showDeleteCloudBackupAlertDialog();
					return true;
				}
			});
		}
	}

	private void updateDeleteBackupPreference() {
		if (mDeleteBackupedRecords != null) {
			mDeleteBackupedRecords.setEnabled(Util.getDefalutValidSdPath(this) == null
					? false
					: true);
		}
	}

	private void updateSettingBackupPathPreference() {
		if (mSettingBackupPath == null) {
			return;
		}

		boolean hasSdPath = true;
		String[] sdPaths = Util.getAllSdPath();
		if (sdPaths == null || sdPaths.length == 0) {
			hasSdPath = false;
		}

		//没有存储卡
		if (!hasSdPath) {
			//			Log.d("GOBackup", "updateSettingBackupPathPreference : sdPaths.length == 0");
			mSettingBackupPath.setSummary(getString(R.string.entry_summary_select_path_no_sd,
					getString(R.string.msg_internal_storage),
					getString(R.string.msg_external_storage)));
			((ExtraTextPreference) mSettingBackupPath).setRightSummary(null);
			if (mSettingBackupPath.isEnabled()) {
				mSettingBackupPath.setEnabled(false);
			}
			return;
		}

		//只有一个存储卡
		if (sdPaths.length == 1) {
			//			Log.d("GOBackup", "updateSettingBackupPathPreference : sdPaths.length == 1");
			mSettingBackupPath.setSummary(BackupManager.getBackupsRootFile(sdPaths[0])
					.getAbsolutePath());
			((ExtraTextPreference) mSettingBackupPath).setRightSummary(null);
			if (mSettingBackupPath.isEnabled()) {
				mSettingBackupPath.setEnabled(false);
			}
			return;
		}

		//		Log.d("GOBackup", "updateSettingBackupPathPreference : sdPaths.length == 2");
		//有两个存储卡以上
		mSettingBackupPath
				.setSummary(getString(R.string.entry_summary_select_path_both,
						getString(R.string.msg_internal_storage),
						getString(R.string.msg_external_storage)));
		String rightSummary = null;
		if (Util.isInternalSdPath(Util.getSdRootPathOnPreference(this))) {
			rightSummary = getString(R.string.entry_summary_select_path_internal_storage);
		} else {
			rightSummary = getString(R.string.entry_summary_select_path_external_storage);
		}
		((ExtraTextPreference) mSettingBackupPath).setRightSummary(rightSummary);
		if (!mSettingBackupPath.isEnabled()) {
			mSettingBackupPath.setEnabled(true);
		}
	}

	private void showSelectBackupPathDialog() {
		mSelectBackupPathDialog = createSelectBackupPathDialog();
		showDialog(mSelectBackupPathDialog);
	}

	@Override
	protected void onStart() {
		super.onStart();
		//		updateSdCardInfo();
		registerVersionUpdateEventRecevier();
		if (!TextUtils.isEmpty(mSdCardRootPath)) {
			BackupManager.getInstance().updateRestoreRecords(this);
		}
		updateCloudServiceSelector();
		mStopped = false;
	}

	@Override
	protected void onStop() {
		super.onStop();
		unregisterVersionUpdateEventReceiver();
		mStopped = true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mService != null && mStartAuthentication) {
			finishFileHostingServiceAuthentication();
		}
	}

	private void finishFileHostingServiceAuthentication() {
		showSpinnerProgressDialog(R.string.tip_loging_in, false);
		mService.finishAuthentication(this, new ActionListener() {
			@Override
			public void onProgress(long progress, long total, Object data) {
			}

			@Override
			public void onCancel(Object data) {
			}

			@Override
			public void onError(int errCode, String errMessage, Object data) {
				Log.d("GoBackup", "finishAuthentication error");
				Message.obtain(mHandler, MSG_DISMISS_DIALOG, mSpinnerProgressDialog).sendToTarget();
				Message.obtain(mHandler, MSG_SHOW_TOAST, errMessage).sendToTarget();
				mStartAuthentication = false;
			}

			@Override
			public void onComplete(Object data) {
				Log.d("GoBackup", "finishAuthentication complete");
				Message.obtain(mHandler, MSG_DISMISS_DIALOG, mSpinnerProgressDialog).sendToTarget();
				mStartAuthentication = false;
				if (data == null) {
					return;
				}
				mCloudServiceAccount = (AccountInfo) data;
				// 刷新UI
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						Log.d("GoBackup", "finishAuthentication notifyContentChange");
						notifyContentChange();
					}
				});
				//				mPreferenceManager.putInt(BackupSettingActivity.this,
				//						PreferenceManager.KEY_NETWORK_BACKUP_TYPE, mService.getType());
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mLogoutReceiver);
		unregisterReceiver(mSDCardStateReceiver);
		mHandler = null;
	}

	private void startBatchDeleteRecordsActivity() {
		if (BackupManager.getInstance().getRecordCount() < 1) {
			showToast(getString(R.string.msg_no_record_to_delete));
			return;
		}
		Intent intent = new Intent(this, BatchDeleteRecordsActivity.class);
		startActivity(intent);
	}

	private void showToast(String toast) {
		Toast.makeText(this, toast, Toast.LENGTH_LONG).show();
	}

	public void dismissDialog(Dialog dialog) {
		if (dialog != null && dialog.isShowing()) {
			dialog.dismiss();
		}
	}

	private void showSpinnerProgressDialog(int messageResId, boolean cancelable) {
		if (mSpinnerProgressDialog == null) {
			mSpinnerProgressDialog = BaseActivity.createSpinnerProgressDialog(this, cancelable);
		}
		mSpinnerProgressDialog.setMessage(getString(messageResId));
		showDialog(mSpinnerProgressDialog);
	}

	private void showDeleteCloudBackupAlertDialog() {
		if (mDeleteCloudBackupAlertDialog == null) {
			mDeleteCloudBackupAlertDialog = createDeleteCloudBackupAlertDialog();
		}
		showDialog(mDeleteCloudBackupAlertDialog);
	}

	private void showLogoutAlertDialog() {
		if (mLogoutAlertDialog == null) {
			mLogoutAlertDialog = createCloudServiceLogoutAlertDialog();
		}
		showDialog(mLogoutAlertDialog);
	}

	private void doCommandFeedback() {
		try {
			String body = FeedBackInfo.getProperties(this);
			Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
			emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			// String[] receiver = new String[] { "gobackup@goforandroid.com" };
			// v1.12 修改了反馈邮箱
			String[] receiver = new String[] { "golauncher@goforandroid.com" };
			emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, receiver);
			String subject = "GOBackup Feedback";
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
			emailIntent.setType("text/html");
			emailIntent.putExtra(Intent.EXTRA_TEXT, body);
			this.startActivity(emailIntent);
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, R.string.go_backup_noEmailApplication, Toast.LENGTH_LONG).show();
		}
	}

	/*
	 * private void showLogoutDialog() { new AlertDialog.Builder(this)
	 * .setMessage(R.string.msg_logout) .setPositiveButton(R.string.sure, new
	 * DialogInterface.OnClickListener() {
	 *
	 * @Override public void onClick(DialogInterface dialog, int which) {
	 * accessToken = new GAccessToken(AccessTokenUtil
	 * .loadAccessToken(BackupSettingActivity.this)) .getAccessToken(); String
	 * ticket = TokenUtil .readTokenCacheFile(BackupSettingActivity.this); if
	 * (TextUtils.isEmpty(accessToken) && TextUtils.isEmpty(ticket)) {
	 * Toast.makeText(getApplicationContext(),
	 * R.string.msg_account_has_logouted, Toast.LENGTH_LONG).show();
	 * dialog.dismiss(); return; } mAccountManager.userLogOut(accessToken,
	 * Appkey, requestCode, consumer_secret, BackupSettingActivity.this); } })
	 * .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
	 * {
	 *
	 * @Override public void onClick(DialogInterface dialog, int which) {
	 * dialog.dismiss(); } }).show(); }
	 */

	/*
	 * private boolean possibleRootUser() { return Util.IsRootRom(this); }
	 */

	private boolean isRootUser() {
		return RootShell.isRootValid();
	}

	// @Override
	// protected void onActivityResult(int requestCode, int resultCode, Intent
	// data) {
	// super.onActivityResult(requestCode, resultCode, data);
	// if( requestCode==WEBVIEW_REQUEST && resultCode==RESULT_OK ){
	// Intent logoutIntent = new Intent(BaseActivity.ACTION_LOGOUT);
	// logoutIntent.setPackage(getPackageName());
	// sendBroadcast(logoutIntent);
	// Intent intent = new Intent(this, AccountLoginActivity.class);
	// intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	// startActivity(intent);
	// finish();
	// }
	// }
	//
	private void startUserCenter() {
		if (!NetCheck.isNetworkAvailable(getApplicationContext())) {
			String lang = GProtocol.getLanguage();
			Intent intent = new Intent(BackupSettingActivity.this, NetworkSettingActivity.class);
			intent.putExtra("lang", lang);
			startActivity(intent);
			return;
		}
		mTicket = TokenUtil.readTokenCacheFile(this);
		mAccountManager.onPersonalCenter(mAppkey, mConsumerSecret, mTicket,
				BackupSettingActivity.this, WEBVIEW_REQUEST);
	}

	private void onUserCenter() {
		mAccountManager.LoadAccessToken(mAppkey, mConsumerSecret, WEBVIEW_REQUEST,
				mGoLauncherVirtualIMEI, this);
	}

	@Override
	public String getAccessToken() {
		String accessToken = new GAccessToken(AccessTokenUtil.loadAccessToken(this))
				.getAccessToken();
		startUserCenter();
		return accessToken;
	}

	@Override
	public void onError(int code, int requestCode) {
		toastOnError(code);
	}

	@Override
	public void setAccessToken(GAccessToken accessToken, int requestCode, AccountUser user) {
		// 保存accessToken
		AccessTokenUtil.saveCacheFile(this, accessToken);
		startUserCenter();
	};

	private void toastOnError(int errorCode) {
		switch (errorCode) {
			case RequestDefin.JSON_PARSE_ERROR :
			case RequestDefin.REQUEST_ERROR :
			case RequestDefin.TICKET_EXIST_OVER :
			case RequestDefin.DATA_INTACT_ERROR :
			case RequestDefin.ACCOUNT_IDENT_ERROR :
			case RequestDefin.ACCESS_TOKEN_INVALID : {
				showToast(getString(R.string.go_account_json_parse_error));
				String ticket = null;
				// 清除ticket
				GToken token = new GToken(TokenUtil.readTokenCacheFile(this));
				if (token != null) {
					ticket = token.getToken();
					if (!TextUtils.isEmpty(ticket)) {
						token.Clear(this);
					}
				}
				// 清除accessToken
				String accessToken = AccessTokenUtil.loadAccessToken(this);
				if (!TextUtils.isEmpty(accessToken)) {
					AccessTokenUtil.deleteFile(this);
				}
				ExtraInfo extraInfo = new ExtraInfo();
				extraInfo.setLauncherid("b8439e21-2cfc-4da1-bbac-63090c5e5d20");
				mAccountManager.NaveiToLogin(mAppkey, mConsumerSecret, WEBVIEW_REQUEST, null, this);
			}
				break;
			case RequestDefin.SERVER_RESP_ERROR :
				showToast(getString(R.string.go_account_server_error));
				break;
			case RequestDefin.NAME_PASSWORD_ERROR :
				showToast(getString(R.string.go_account_password_error));
				break;
			case RequestDefin.ACCOUNT_EXIST :
				showToast(getString(R.string.go_account_existed_error));
				break;
			case RequestDefin.ACCOUNT_NOT_EXIST :
				showToast(getString(R.string.go_account_existed_error));
				break;
			case RequestDefin.ACCOUNT_NOT_EMAIL_INFO :
				showToast(getString(R.string.go_account_accountMail_notSet_error));
				break;
			case RequestDefin.GET_PERMISSION_FAIL :
				showToast(getString(R.string.go_account_permission_fail_error));
				break;
			case RequestDefin.NULL_PERMISSION :
				showToast(getString(R.string.go_account_notPermission_error));
				break;
			default :
				showToast(getString(R.string.go_account_another_error));
				break;
		}
	}

	public void registerVersionUpdateEventRecevier() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(VersionChecker.ACTION_NEW_UPDATE);
		filter.addAction(VersionChecker.ACTION_FORCE_UPDATE);
		filter.addAction(VersionChecker.ACTION_SHOW_UPDATE_TIP);
		registerReceiver(mVersionUpdateReceiver, filter);
	}

	public void unregisterVersionUpdateEventReceiver() {
		try {
			unregisterReceiver(mVersionUpdateReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//	private void updateSdCardInfo() {
	//		// Util.updateGOBackupRootPathToPreference(this);
	//		String backupRootPath = Util.getBackupRootPathOnPreference(this);
	//		if (backupRootPath == null) {
	//			return;
	//		}
	//		mSdCardRootPath = new File(backupRootPath).getParent();
	//		String fullBackupPath = null;
	//		if (!TextUtils.isEmpty(mSdCardRootPath)) {
	//			fullBackupPath = Util.ensureFileSeparator(mSdCardRootPath) + Constant.GOBACKUP_ROOT_DIR
	//					+ File.separator + Constant.BACKUP_RES_ROOT_DIR + File.separator;
	//		}
	//		mDeleteBackupedRecords.setSummary(fullBackupPath);
	//	}

	private Dialog createCloudServiceLogoutAlertDialog() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle(R.string.alert_dialog_title);
		dialog.setMessage(R.string.title_logout_from_cloud_service);
		dialog.setPositiveButton(R.string.sure, new AlertDialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				CloudServiceManager csm = CloudServiceManager.getInstance();
				csm.logout(BackupSettingActivity.this, mService);
				mService = null;
				notifyContentChange();
			}
		});
		dialog.setNegativeButton(R.string.cancel, new AlertDialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		return dialog.create();
	}

	private Dialog createDeleteCloudBackupAlertDialog() {
		return new AlertDialog.Builder(this).setTitle(R.string.attention)
				.setMessage(R.string.msg_delete_cloud_backup)
				.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						deleteCloudBackup();
						CloudServiceManager.clearCacheFiles(BackupSettingActivity.this, mService);
						// 删除本地缓存的未完成task
						Util.deleteFile(BackupSettingActivity.this.getCacheDir().getAbsolutePath());
					}
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).create();
	}

	private Dialog createSelectBackupPathDialog() {
		mAdapter = (BackupPathAdapter) getSelectBackupPathAdapter();
		Dialog dialog = null;
		dialog = new AlertDialog.Builder(this)
				.setTitle(R.string.msg_setting_select_path_title)
				.setSingleChoiceItems(mAdapter, mAdapter.getCheckItemPosition(),
						new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								mAdapter.setCheck(which);
							}
						})
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mPreferenceManager.putString(BackupSettingActivity.this,
								PreferenceManager.KEY_BACKUP_SD_PATH, mAdapter.getBackupSdPath());
						updateSettingBackupPathPreference();
						BackupManager.getInstance().init(BackupSettingActivity.this);
					}
				}).create();
		return dialog;
	}

	private void showBackupPathChangeAlertDialog(String message) {
		if (mBackupPathChangeAlertDialog == null) {
			mBackupPathChangeAlertDialog = createBackupPathChangeDialog(message);
		} else {
			((AlertDialog) mBackupPathChangeAlertDialog).setMessage(message);
		}
		if (mBackupPathChangeAlertDialog.isShowing()) {
			return;
		}
		showDialog(mBackupPathChangeAlertDialog);
	}

	private Dialog createBackupPathChangeDialog(String message) {
		Dialog dialog = new AlertDialog.Builder(this).setTitle(R.string.attention)
				.setMessage(message).setPositiveButton(R.string.ok, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				}).create();
		return dialog;

	}

	private BaseAdapter getSelectBackupPathAdapter() {
		String[] allPaths = Util.getAllSdPath();
		mAdapter = new BackupPathAdapter(this, allPaths);
		return mAdapter;
	}

	private void deleteCloudBackup() {
		showSpinnerProgressDialog(R.string.msg_wait, false);
		if (mService != null) {
			String backupPath = mService.getOnlineBackupPath();
			mService.deleteFile(backupPath, new ActionListener() {

				@Override
				public void onProgress(long progress, long total, Object data) {
				}

				@Override
				public void onError(int errCode, String errMessage, Object data) {
					if (errCode == FileHostingServiceProvider.SERVER_RESOURCE_NOT_FOUND_ERROR) {
						Message.obtain(mHandler, MSG_SHOW_TOAST,
								getString(R.string.msg_cloud_backup_had_been_deleted))
								.sendToTarget();
					} else {
						Message.obtain(mHandler, MSG_SHOW_TOAST,
								getString(R.string.msg_failed_to_delete_cloud_backup))
								.sendToTarget();
					}
					Message.obtain(mHandler, MSG_DISMISS_DIALOG, mSpinnerProgressDialog)
							.sendToTarget();
				}

				@Override
				public void onComplete(Object data) {
					Message.obtain(mHandler, MSG_DISMISS_DIALOG, mSpinnerProgressDialog)
							.sendToTarget();
					Message.obtain(mHandler, MSG_SHOW_TOAST,
							getString(R.string.msg_delete_cloud_backup_successfully))
							.sendToTarget();
				}

				@Override
				public void onCancel(Object data) {
				}
			});
		}
	}

	private void updateDeleteCloudBackupPreference() {
		if (mDeleteCloudBackup != null) {
			mDeleteCloudBackup.setEnabled(mService != null ? mService.isSessionValid() : false);
		}
	}

	private CharSequence getCloudServiceProviderName(int type) {
		CharSequence[] providersName = getResources().getStringArray(R.array.cloud_service);
		if (type > 0 && type <= providersName.length) {
			return providersName[type - 1];
		}
		return "";
	}

	private int getActiveCloudServiceType() {
		if (mCloudServiceSelector != null) {
			return Integer.parseInt(mCloudServiceSelector.getValue());
		}
		return -1;
	}

	private void updateCloudServiceSelector() {
		if (mCloudServiceSelector != null) {
			final boolean isPaid = mGoBackupProduct.isAlreadyPaid();
			mCloudServiceSelector.setEnabled(isPaid);

			if (mService != null) {
				mCloudServiceSelector.setValue(String.valueOf(mService.getType()));
			}
			String text = getString(R.string.preference_text_format,
					getCloudServiceProviderName(getActiveCloudServiceType()));
			mCloudServiceSelector.setExtraText(text);
		}
	}

	private void updateAccountManagerPreference(int type) {
		if (mCloudServiceAccountManager != null) {
			final boolean isPaid = mGoBackupProduct.isAlreadyPaid();
			mCloudServiceAccountManager.setEnabled(isPaid);

			if (mService == null || !mService.isSessionValid()) {
				String title = getString(R.string.click_to_login, getCloudServiceProviderName(type));
				mCloudServiceAccountManager.setTitle(title);
				mCloudServiceAccountManager.setSummary(null);
				mCloudServiceAccountManager.setButtonVisibility(false);
			} else {
				mCloudServiceAccount = mService.getAccount(null);
				if (mCloudServiceAccount != null) {
					String title = getString(R.string.cloud_service_account,
							getCloudServiceProviderName(type));
					mCloudServiceAccountManager.setTitle(title);
					mCloudServiceAccountManager.setSummary(mCloudServiceAccount.getDisplayName());
					mCloudServiceAccountManager.setButtonVisibility(true);
				}
			}
		}
	}

	private void notifyContentChange() {
		updateDeleteCloudBackupPreference();
		updateAccountManagerPreference(Integer.parseInt(mCloudServiceSelector.getValue()));
	}

	/*
	 * 暂时屏蔽注销功能，用个人中心里面的注销功能
	 *
	 * @Override public void AccountLogOutSuccess() { // Intent launcherIntent =
	 * getPackageManager().getLaunchIntentForPackage(getPackageName()); //
	 * launcherIntent
	 * .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
	 * Intent logoutIntent = new Intent(CustomIntentAction.ACTION_LOGOUT);
	 * logoutIntent.setPackage(getPackageName()); sendBroadcast(logoutIntent);
	 * Intent intent = new Intent(this, AccountLoginActivity.class);
	 * intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); startActivity(intent);
	 * finish(); }
	 *
	 * @Override public void logOutError(int errorCode) { // accessToken = null;
	 * // Toast.makeText(getApplicationContext(), R.string.account_logout_fail,
	 * Toast.LENGTH_LONG).show(); }
	 */

	private boolean isStopped() {
		return mStopped;
	}

	public void showDialog(Dialog dialog) {
		if (dialog == null || isStopped() || isFinishing()) {
			return;
		}
		if (!dialog.isShowing()) {
			dialog.show();
		}
	}

	/**
	 *
	 * @author WenCan
	 */
	private static class BackupPathAdapter extends BaseAdapter {
		private Context mContext;
		private LayoutInflater mInflater;
		private String[] mPaths;
		private int mCheckItemPos = -1;

		/**
		 *
		 * @author WenCan
		 */
		private class ViewHolder {
			public TextView mTitle;
			public TextView mSummary;
			public CheckedTextView mCheckBox;
		}

		public BackupPathAdapter(Context context, String[] paths) {
			mContext = context;
			mInflater = LayoutInflater.from(mContext);
			mPaths = paths;
			mCheckItemPos = getPreferenceBackupPathPosition();
		}

		private int getExternalStoragePathPosition() {
			if (mPaths == null || mPaths.length < 1) {
				return -1;
			}

			final int length = mPaths.length;
			String internalStoragePath = Util.ensureFileSeparator(Util.getInternalSdPath());
			if (internalStoragePath == null) {
				return 0;
			}

			for (int i = 0; i < length; i++) {
				if (!TextUtils.equals(internalStoragePath, Util.ensureFileSeparator(mPaths[i]))) {
					return i;
				}
			}
			return -1;
		}

		private int getPreferenceBackupPathPosition() {
			if (mPaths == null || mPaths.length < 1) {
				return -1;
			}

			final int length = mPaths.length;
			String preferenceSdPath = Util.ensureFileSeparator(Util
					.getSdRootPathOnPreference(mContext));
			if (preferenceSdPath == null) {
				return -1;
			}

			for (int i = 0; i < length; i++) {
				if (TextUtils.equals(preferenceSdPath, Util.ensureFileSeparator(mPaths[i]))) {
					return i;
				}
			}
			return -1;
		}

		@Override
		public int getCount() {
			return mPaths != null ? mPaths.length : 0;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder = null;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.layout_setting_select_path_item, parent,
						false);
				viewHolder = new ViewHolder();
				viewHolder.mTitle = (TextView) convertView
						.findViewById(R.id.setting_select_path_title);
				viewHolder.mSummary = (TextView) convertView
						.findViewById(R.id.setting_select_path_summary);
				viewHolder.mCheckBox = (CheckedTextView) convertView
						.findViewById(R.id.setting_select_path_check);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			final int lenght = mPaths.length;
			if (position < 0 || position >= lenght) {
				return null;
			}

			if (mCheckItemPos == position) {
				if (!viewHolder.mCheckBox.isChecked()) {
					viewHolder.mCheckBox.setChecked(true);
				}
			} else if (viewHolder.mCheckBox.isChecked()) {
				viewHolder.mCheckBox.setChecked(false);
			}
			String path = mPaths[position];
			if (Util.isInternalSdPath(path)) {
				// 内置sd路径
				viewHolder.mTitle.setText(R.string.msg_internal_storage);
			} else {
				viewHolder.mTitle.setText(R.string.msg_external_storage);
			}
			viewHolder.mSummary.setText(BackupManager.getBackupsRootFile(path).getAbsolutePath());
			return convertView;
		}

		private void setCheck(int position) {
			if (position < 0 || position >= mPaths.length) {
				return;
			}
			mCheckItemPos = position;
			notifyDataSetChanged();
		}

		public int getCheckItemPosition() {
			return mCheckItemPos;
		}

		public String getBackupSdPath() {
			if (mCheckItemPos < 0 || mCheckItemPos >= mPaths.length) {
				return null;
			}
			return Util.ensureFileSeparator(mPaths[mCheckItemPos]);
		}
	}
}
