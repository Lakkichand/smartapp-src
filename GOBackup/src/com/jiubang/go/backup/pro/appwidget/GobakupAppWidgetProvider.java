package com.jiubang.go.backup.pro.appwidget;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;
import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.MainActivity;
import com.jiubang.go.backup.pro.ParcelableAction.DeleteRecordAction;
import com.jiubang.go.backup.pro.RecordsListActivity;
import com.jiubang.go.backup.pro.data.BackupableRecord;
import com.jiubang.go.backup.pro.data.BackupableRecord.RecordBackupArgs;
import com.jiubang.go.backup.pro.data.CallLogBackupEntry;
import com.jiubang.go.backup.pro.data.ContactsBackupEntry;
import com.jiubang.go.backup.pro.data.IRecord;
import com.jiubang.go.backup.pro.data.MmsBackupEntry;
import com.jiubang.go.backup.pro.data.RestorableRecord;
import com.jiubang.go.backup.pro.data.ResultBean;
import com.jiubang.go.backup.pro.data.SmsBackupEntry;
import com.jiubang.go.backup.pro.model.BackupManager;
import com.jiubang.go.backup.pro.model.BackupService;
import com.jiubang.go.backup.pro.model.ForegroundWorkerService;
import com.jiubang.go.backup.pro.model.ForegroundWorkerService.LocalBinder;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.track.ga.TrackerEvent;
import com.jiubang.go.backup.pro.util.Util;

/**
 * @author jiangpeihe
 *
 */
public class GobakupAppWidgetProvider extends AppWidgetProvider {
	private static final String GOBACKWIDGETFILE = "gobackup_widget_file";
	private final String mFAST_OPERATION = "com.jiubang.fastOperation";
	private final String mUPDATECHOOSECONTENT = "com.jiubang.APPWIDGET_UPDATE";
	private boolean mSelectedBackupContent[] = new boolean[4];
	private String mKeyArray[] = new String[] { "contact", "sms", "call_log", "mms" };
	private static final String RECORD_DATE_FORMAT = "yyyy-MM-dd";
	private ServiceConnection mBackupServiceConnection;
	private ForegroundWorkerService mBackupService;
	private static final int BACKUP_RESULT_NOTIFICATION_ID = 0x00ff20ff;
	private NotificationManager mNotificationManager;
	private final int mDELETE_BACKUP_FILE = 0x1002;
	private final int mSHOW_RESULT_DIALOG = 0x1004;
	private final int mUNBIND_CONNECTION = 0x1005;
	private final int mSHOW_RECORD_LIMIT_DIALOG = 0x1006;
	private final int mSHOW_FAILED_RESULT_TOAST_ON_DESK = 0x1007;
	private final int mSHOW_RESULT_TOAST_ON_DESK = 0x1008;
	private final int mWRITE_CHOOSE_CONTENT = 0x1009;
	private Tracker mTracker;

	private SharedPreferences mSp;
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		BackupManager backupManager = BackupManager.getInstance();
		backupManager.scanNewRecords(context);
		RestorableRecord lastRecord = backupManager.getLatestRestoreRecord();

		RemoteViews remoteViews = getRemoteViews(context);
		Map<String, ?> oldMap = getSharePreferencesContent(context);
		if (oldMap != null && oldMap.size() > 0) {
			updateAllRemoteViews(remoteViews, oldMap);
		}
		if (lastRecord == null) {
			remoteViews
					.setTextViewText(R.id.backupinfo, context.getString(R.string.msg_no_backups));
		} else {
			String dateString = context.getString(R.string.last_backup_time)
					+ new SimpleDateFormat(RECORD_DATE_FORMAT).format(lastRecord.getDate());
			remoteViews.setTextViewText(R.id.backupinfo, dateString);
		}
		appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	@Override
	public void onReceive(Context context, Intent intent) {

		String intentAction = intent.getAction();
		if (intentAction.equals(mFAST_OPERATION)) {
			if (!hasBackupEntriesSelected(context)) {
				Toast.makeText(context, R.string.nobackupentriesselected, Toast.LENGTH_LONG).show();
				return;
			}
			BackupManager backupManager = BackupManager.getInstance();
			backupManager.scanNewRecords(context);
			List<IRecord> normalRecords = backupManager.getAllRestoreRecords();
			if (normalRecords != null
					&& normalRecords.size() >= BackupManager.getInstance().getMaxBackupCount()) {
				Message.obtain(mHandler, mSHOW_RECORD_LIMIT_DIALOG, context).sendToTarget();
			} else {
				Intent isBackuingIntent = new Intent();
				isBackuingIntent.setAction("com.jiubang.BACKUPING");
				context.sendBroadcast(isBackuingIntent);

				BackupableRecord mRecord = new BackupableRecord(context);
				Map<String, ?> oldMap = getSharePreferencesContent(context);
				if (oldMap == null || oldMap.size() == 0) {
					return;
				}
				addBackupEntries(mRecord, context, oldMap);
				startBackup(mRecord, context);
			}

		} else if (intentAction.equals(mUPDATECHOOSECONTENT)) {
			Map<String, ?> oldMap = getSharePreferencesContent(context);
			RemoteViews remoteViews = getRemoteViews(context);
			if (oldMap != null && oldMap.size() > 0) {
				updateAllRemoteViews(remoteViews, oldMap);
			}
			updateRemoteViewsByAppManager(context, remoteViews);
		} else {
			super.onReceive(context, intent);
		}
	}

	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);
		context.getSharedPreferences("gobackup_widget_file", Context.MODE_PRIVATE).edit().clear()
				.commit();
		EasyTracker.getInstance().setContext(context);
		mTracker = EasyTracker.getTracker();
		mTracker.trackEvent(TrackerEvent.CATEGORY_WIDGET, TrackerEvent.ACTION_WIDGET_DELETE,
				TrackerEvent.LABEL_WIDGET_DELETE, TrackerEvent.OPT_WIDGET_DELETE);
	}

	private boolean hasBackupEntriesSelected(Context context) {
		Map<String, ?> oldMap = getSharePreferencesContent(context);
		Set<String> keys = oldMap.keySet();
		if (keys == null || keys.size() == 0) {
			return false;
		}
		for (String key : keys) {
			if ((Boolean) oldMap.get(key)) {
				return true;
			}
		}
		return false;

	}
	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
		writeDefaultBackupContent(context);
		EasyTracker.getInstance().setContext(context);
		mTracker = EasyTracker.getTracker();
		mTracker.trackEvent(TrackerEvent.CATEGORY_WIDGET, TrackerEvent.ACTION_WIDGET_ADD,
				TrackerEvent.LABEL_WIDGET_ADD, TrackerEvent.OPT_WIDGET_ADD);

	}

	private void writeDefaultBackupContent(Context context) {
		SharedPreferences sp = context.getSharedPreferences("gobackup_widget_file",
				Context.MODE_PRIVATE);
		if (sp != null) {
			Editor editor = sp.edit();
			editor.putBoolean("contact", true);
			editor.putBoolean("sms", true);
			editor.commit();
		}
	}

	private Map<String, ?> getSharePreferencesContent(Context context) {
		SharedPreferences sp = context.getSharedPreferences("gobackup_widget_file",
				Context.MODE_PRIVATE);
		Map<String, ?> oldMap = null;
		try {
			oldMap = sp.getAll();
		} catch (NullPointerException e) {
		}
		return oldMap;
	}

	private void startBackup(final BackupableRecord mBackupRecord, final Context context) {
		mContext = context;
		if (mBackupRecord == null) {
			return;
		}
		// TODO 完善备份路径以及是否root的初始化
		final RecordBackupArgs backupArgs = new RecordBackupArgs();
		backupArgs.mBackupPath = mBackupRecord.getBackupPath();
		mBackupServiceConnection = new ServiceConnection() {
			@Override
			public void onServiceDisconnected(ComponentName name) {
			}

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				mBackupService = ((LocalBinder) service).getService();
				if (mBackupService.isRunning()) {
					try {
						context.getApplicationContext().unbindService(mBackupServiceConnection);
					} catch (Exception e) {
						e.printStackTrace();
					}
					Toast.makeText(context, context.getString(R.string.msg_backuping),
							Toast.LENGTH_LONG).show();
					return;
				}
				mBackupService.startWork(context, mBackupRecord, backupArgs,
						new IAsyncTaskListener() {

							@Override
							public void onStart(Object arg1, Object arg2) {
							}

							@Override
							public void onProceeding(Object progress, Object arg2, Object arg3,
									Object arg4) {
								updateWidgetProgressBar(context, progress);

							}

							@Override
							public void onEnd(boolean success, Object arg1, Object arg2) {
								if (arg1 instanceof ResultBean[]) {
									if (!isSaveBackupFile((ResultBean[]) arg1)) {
										Message.obtain(mHandler, mDELETE_BACKUP_FILE, mBackupRecord)
												.sendToTarget();
										Message.obtain(mHandler, mSHOW_FAILED_RESULT_TOAST_ON_DESK,
												context).sendToTarget();
									} else {
										Message.obtain(mHandler, mSHOW_RESULT_TOAST_ON_DESK,
												context).sendToTarget();
										Message.obtain(mHandler, mSHOW_RESULT_DIALOG, context)
												.sendToTarget();
									}
								}
								try {
									context.getApplicationContext().unbindService(
											mBackupServiceConnection);

								} catch (Exception e) {
									e.printStackTrace();
								}

							}

						});
			}
		};
		Intent intent = new Intent(context, BackupService.class);
		context.getApplicationContext().bindService(intent, mBackupServiceConnection,
				Context.BIND_AUTO_CREATE);
	}

	private void updateWidgetProgressBar(Context context, Object progress) {
		SharedPreferences sp = context.getSharedPreferences("gobackup_widget_file",
				Context.MODE_PRIVATE);
		Map<String, ?> oldMap = null;
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
				R.layout.gobackup_appwidget);
		try {
			oldMap = sp.getAll();
		} catch (NullPointerException e) {
		}
		if (oldMap == null || oldMap.size() == 0) {
			return;
		}
		updateAllRemoteViews(remoteViews, oldMap);
		remoteViews.setViewVisibility(R.id.fast_operation, View.GONE);
		remoteViews.setViewVisibility(R.id.progressbarView, View.VISIBLE);
		remoteViews.setProgressBar(R.id.myProgressBar1, 100, (Integer) progress, false);
		remoteViews.setTextViewText(R.id.progeressValue, progress + "%");
		updateRemoteViewsByAppManager(context, remoteViews);
	}

	private void updateAllView(Context context) {
		Map<String, ?> oldMap = getSharePreferencesContent(context);
		RemoteViews remoteViews = getRemoteViews(context);
		if (oldMap == null || oldMap.size() == 0) {
			return;
		}
		updateAllRemoteViews(remoteViews, oldMap);
		remoteViews.setViewVisibility(R.id.fast_operation, View.VISIBLE);
		remoteViews.setViewVisibility(R.id.progressbarView, View.GONE);
		updateRemoteViewsByAppManager(context, remoteViews);
	}

	private Context mContext = null;

	final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case mDELETE_BACKUP_FILE :
					BackupManager.getInstance().scanNewRecords(mContext);
					new DeleteRecordAction(((BackupableRecord) msg.obj).getId()).execute();
					updateAllView(mContext);
					showResultNotification(mContext, false);
					break;

				case mSHOW_RESULT_DIALOG :
					updateAllView(mContext);
					showResultNotification((Context) msg.obj, true);
					break;

				case mSHOW_RECORD_LIMIT_DIALOG :
					showLimitRecordDialog((Context) msg.obj);
					break;

				case mSHOW_FAILED_RESULT_TOAST_ON_DESK :
					Toast.makeText((Context) msg.obj, R.string.msg_backup_failed, Toast.LENGTH_LONG)
							.show();
					break;

				case mSHOW_RESULT_TOAST_ON_DESK :
					Toast.makeText((Context) msg.obj, R.string.msg_backup_finished,
							Toast.LENGTH_LONG).show();
					break;

				case mWRITE_CHOOSE_CONTENT :
					writePreferenceAndsendBroadcast((Context) msg.obj);
					break;

				case mUNBIND_CONNECTION :
					try {
						//						sBackupServiceConnected = false;
						Toast.makeText((Context) msg.obj,
								((Context) msg.obj).getString(R.string.msg_backuping),
								Toast.LENGTH_LONG).show();
						//						((Context) msg.obj).getApplicationContext().unbindService(
						//								mBackupServiceConnection);

					} catch (Exception e) {
						e.printStackTrace();
					}
				default :
					break;
			}
		};

	};

	private void showLimitRecordDialog(Context context) {
		Intent dialogIntent = new Intent(context, ShowDialogActivity.class);
		dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(dialogIntent);
	}

	public void addBackupEntries(BackupableRecord mRecord, Context context, Map<String, ?> oldMap) {
		Set<String> keys = oldMap.keySet();
		if (keys == null || keys.size() == 0) {
			return;
		}
		for (String key : keys) {
			if (key.equals("mms")) {
				if ((Boolean) oldMap.get(key)) {
					MmsBackupEntry mmsBackupEntry = new MmsBackupEntry(context);
					mmsBackupEntry.setSelected(true);
					mRecord.addEntry(IRecord.GROUP_USER_DATA, mmsBackupEntry);

				}
			} else if (key.equals("call_log")) {
				if ((Boolean) oldMap.get(key)) {
					CallLogBackupEntry callLogBackupEntry = new CallLogBackupEntry(context);
					callLogBackupEntry.setSelected(true);
					mRecord.addEntry(IRecord.GROUP_USER_DATA, callLogBackupEntry);
				}
			} else if (key.equals("sms")) {
				if ((Boolean) oldMap.get(key)) {
					SmsBackupEntry smsBackupEntry = new SmsBackupEntry(context);
					smsBackupEntry.setSelected(true);
					mRecord.addEntry(IRecord.GROUP_USER_DATA, smsBackupEntry);
				}
			} else if (key.equals("contact")) {
				if ((Boolean) oldMap.get(key)) {
					ContactsBackupEntry contactsBackupEntry = new ContactsBackupEntry(context);
					contactsBackupEntry.setSelected(true);
					mRecord.addEntry(IRecord.GROUP_USER_DATA, contactsBackupEntry);
				}
			}
		}
	}

	private boolean isSaveBackupFile(ResultBean[] resultBean) {
		if (resultBean == null || resultBean.length <= 0) {
			return false;
		}
		boolean save = false;
		for (int i = 0; i < resultBean.length; i++) {
			ResultBean result = resultBean[i];
			if (result.result) {
				save = true;
				return save;
			}
		}
		return save;
	}

	private void updateRemoteViewsByAppManager(Context context, RemoteViews remoteViews) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		ComponentName componentName = new ComponentName(context, GobakupAppWidgetProvider.class);
		appWidgetManager.updateAppWidget(componentName, remoteViews);
	}

	public RemoteViews getRemoteViews(Context context) {
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
				R.layout.gobackup_appwidget);

		BackupManager backupManager = BackupManager.getInstance();
		backupManager.scanNewRecords(context);
		RestorableRecord lastRecord = backupManager.getLatestRestoreRecord();

		if (lastRecord == null) {
			remoteViews
					.setTextViewText(R.id.backupinfo, context.getString(R.string.msg_no_backups));
		} else {
			String dateString = context.getString(R.string.last_backup_time)
					+ new SimpleDateFormat(RECORD_DATE_FORMAT).format(lastRecord.getDate());
			remoteViews.setTextViewText(R.id.backupinfo, dateString);
		}

		//备份内容
		Intent intent = new Intent(context, WidgetDilogActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.allbackup, pendingIntent);

		//一键备份
		Intent fastOperationIntent = new Intent();
		fastOperationIntent.setAction(mFAST_OPERATION);
		PendingIntent fastOprationPendingIntent = PendingIntent.getBroadcast(context, 1,
				fastOperationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.fast_operation, fastOprationPendingIntent);

		//云端备份入口

		Intent netIntent = null;
		if (Util.isInland(context)) {
			netIntent = new Intent(context, MainActivity.class);
		} else {
			netIntent = new Intent(context, MainActivity.class);
			netIntent.putExtra("backuptype", "netBackup");
		}
		PendingIntent netPendingIntent = PendingIntent.getActivity(context, 1, netIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		remoteViews.setOnClickPendingIntent(R.id.netbackup, netPendingIntent);

		return remoteViews;

	}

	private void updateAllRemoteViews(RemoteViews remoteViews, Map<String, ?> oldMap) {

		Set<String> keys = oldMap.keySet();
		if (keys == null || keys.size() == 0) {
			return;
		}
		for (String key : keys) {
			if (key.equals("mms")) {
				if ((Boolean) oldMap.get(key)) {
					remoteViews.setImageViewResource(R.id.mms, R.drawable.widget_mms_light);
				} else {
					remoteViews.setImageViewResource(R.id.mms, R.drawable.widget_mms);
				}
			} else if (key.equals("call_log")) {
				if ((Boolean) oldMap.get(key)) {
					remoteViews.setImageViewResource(R.id.call_log,
							R.drawable.widget_call_log_light);
				} else {
					remoteViews.setImageViewResource(R.id.call_log, R.drawable.widget_call_log);
				}
			} else if (key.equals("sms")) {
				if ((Boolean) oldMap.get(key)) {
					remoteViews.setImageViewResource(R.id.sms, R.drawable.widget_sms_light);
				} else {
					remoteViews.setImageViewResource(R.id.sms, R.drawable.widget_sms);
				}
			} else if (key.equals("contact")) {
				if ((Boolean) oldMap.get(key)) {
					remoteViews
							.setImageViewResource(R.id.contacts, R.drawable.widget_contact_light);
				} else {
					remoteViews.setImageViewResource(R.id.contacts, R.drawable.widget_contact);
				}
			}

		}
	}

	public ListView initViews(Context context) {
		ListView listview = new ListView(context);
		listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
				if (checkBox.isChecked()) {
					checkBox.setChecked(false);
				} else {
					checkBox.setChecked(true);
				}
			}
		});
		mSp = context.getSharedPreferences(GOBACKWIDGETFILE, Context.MODE_PRIVATE);
		return listview;
	}

	public BaseAdapter initAdapter(Context context) {
		Map<String, ?> oldMap = null;

		try {
			oldMap = mSp.getAll();
		} catch (NullPointerException e) {
		}
		if (oldMap != null && oldMap.size() > 0) {
			Set<String> keys = oldMap.keySet();
			if (keys != null && keys.size() > 0) {

				for (String key : keys) {
					if (key.equals("mms")) {
						mSelectedBackupContent[3] = (Boolean) oldMap.get(key);
					} else if (key.equals("call_log")) {
						mSelectedBackupContent[2] = (Boolean) oldMap.get(key);
					} else if (key.equals("sms")) {
						mSelectedBackupContent[1] = (Boolean) oldMap.get(key);
					} else if (key.equals("contact")) {
						mSelectedBackupContent[0] = (Boolean) oldMap.get(key);
					}
				}
			}
		}
		String[] items = context.getResources().getStringArray(R.array.backupchoose);
		BackupChooseDialog backupDialogAdapter = new BackupChooseDialog(context,
				Arrays.asList(items), mSelectedBackupContent);
		return backupDialogAdapter;

	}

	private void writePreferenceAndsendBroadcast(Context context) {
		for (int i = 0; i < mSelectedBackupContent.length; i++) {
			if (mSp != null) {
				Editor editor = mSp.edit();
				editor.putBoolean(mKeyArray[i], mSelectedBackupContent[i]);
				editor.commit();
			}
		}
		Intent intent = new Intent("com.jiubang.APPWIDGET_UPDATE");
		context.sendBroadcast(intent);
	}

	private Notification getAdaptiveResultNotification(Context context, boolean success) {
		return Util.getAndroidSystemVersion() >= Build.VERSION_CODES.HONEYCOMB
				? getThemeHoloNotification(context, success)
				: getThemeNomalNotification(context, success);
	}

	private Notification getThemeNomalNotification(Context context, boolean success) {
		String finishResult = null;
		if (success) {
			finishResult = context.getString(R.string.msg_backup_finished);
		} else {
			finishResult = context.getString(R.string.msg_backup_failed);

		}
		Notification notification = new Notification();
		notification.icon = R.drawable.icon;
		RemoteViews contentView = new RemoteViews(context.getPackageName(),
				R.layout.layout_backup_restore_result_notification);
		contentView.setTextViewText(R.id.title, finishResult);
		contentView.setViewVisibility(R.id.result, View.GONE);
		notification.contentView = contentView;
		notification.contentIntent = PendingIntent.getActivity(context, 0, new Intent(context,
				success ? RecordsListActivity.class : MainActivity.class), 0);
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		notification.defaults &= ~Notification.DEFAULT_VIBRATE;
		notification.vibrate = null;
		return notification;
	}

	private Notification getThemeHoloNotification(Context context, boolean success) {
		String finishResult = null;
		if (success) {
			finishResult = context.getString(R.string.msg_backup_finished);
		} else {
			finishResult = context.getString(R.string.msg_backup_failed);

		}
		return new Notification.Builder(context)
				.setSmallIcon(R.drawable.notification_icon)
				.setContentTitle(finishResult)
				.setContentIntent(
						PendingIntent.getActivity(context, 0, new Intent(context, success
								? RecordsListActivity.class
								: MainActivity.class), 0)).setAutoCancel(true).getNotification();
	}

	private void showResultNotification(Context context, boolean success) {
		if (mNotificationManager != null) {
			mNotificationManager.notify(BACKUP_RESULT_NOTIFICATION_ID,
					getAdaptiveResultNotification(context, success));
		} else {
			mNotificationManager = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.notify(BACKUP_RESULT_NOTIFICATION_ID,
					getAdaptiveResultNotification(context, success));
		}
	}

}
