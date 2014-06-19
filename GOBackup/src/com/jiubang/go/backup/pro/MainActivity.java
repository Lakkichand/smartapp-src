package com.jiubang.go.backup.pro;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;
import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.data.FeedBackInfo;
import com.jiubang.go.backup.pro.data.IRecord;
import com.jiubang.go.backup.pro.data.RestorableRecord;
import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.model.BackupManager;
import com.jiubang.go.backup.pro.model.Constant;
import com.jiubang.go.backup.pro.model.NetSyncTaskDbHelper;
import com.jiubang.go.backup.pro.model.NetSyncTaskDbHelper.TaskType;
import com.jiubang.go.backup.pro.model.RootShell;
import com.jiubang.go.backup.pro.net.sync.AccountInfo;
import com.jiubang.go.backup.pro.net.sync.CloudServiceManager;
import com.jiubang.go.backup.pro.net.sync.FileHostingServiceProvider;
import com.jiubang.go.backup.pro.net.sync.FileHostingServiceProvider.ActionListener;
import com.jiubang.go.backup.pro.net.version.VersionChecker;
import com.jiubang.go.backup.pro.product.manage.ProductManager;
import com.jiubang.go.backup.pro.statistics.StatisticsDataManager;
import com.jiubang.go.backup.pro.statistics.StatisticsKey;
import com.jiubang.go.backup.pro.statistics.StatisticsTool;
import com.jiubang.go.backup.pro.track.ga.TrackerEvent;
import com.jiubang.go.backup.pro.track.ga.TrackerLog;
import com.jiubang.go.backup.pro.ui.PayUpdateHelpActivity;
import com.jiubang.go.backup.pro.ui.ScrollerView.ScreenScroller;
import com.jiubang.go.backup.pro.ui.ScrollerView.ScreenScrollerListener;
import com.jiubang.go.backup.pro.ui.ScrollerView.ScrollerViewGroup;
import com.jiubang.go.backup.pro.util.AppFreezer;
import com.jiubang.go.backup.pro.util.LogUtil;
import com.jiubang.go.backup.pro.util.Logger;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 程序主界面
 *
 * @author maiyongshen
 */
public class MainActivity extends BaseActivity
		implements
			ScreenScrollerListener,
			android.view.View.OnClickListener {
	private static final String RECORD_DATE_FORMAT = "yyyy-MM-dd";
	private static final int REQUEST_SHOW_TUTORIAL = 1000;

	private ViewGroup mPromptFrame;
	private ViewGroup mOperationFrame;

	private ImageButton mSchedulePlanButton;
	private ImageButton mSettingButton;
	private ImageButton mLoginButton;
	//	private Spinner mLoginButton;

	private ViewGroup mNewBackupButton;
	private ViewGroup mRestoreButton;
	private TextView mBackupButtonSummary;
	private TextView mRestoreButtonSummary;
	private TextView mBackupPath;
	private TextView mCloudBackupTip;
	private TextView mWebBackupPath;

	private ViewGroup mNewWebBackupButton;
	private ViewGroup mWebRestoreButton;
	private TextView mWebBackupButtonSummary;
	private TextView mWebRestoreButtonSummary;

	private ViewGroup mFreezeAppButton;
	private ViewGroup mUnfreezeAppButton;

	private ProgressBar mSdCardUsageProgress;
	private TextView mSdCardUsageDesc;
	private View mRootTabPrompt;

	private ProgressDialog mWaitDialog = null;
	private ProgressDialog mUpdateProgressDialog = null;
	private Dialog mRecordLimitAlertDialog = null;
	private Dialog mRedoCloudTaskAlertDialog = null;

	private PopupWindow mMenu;

	private PreferenceManager mPm;

	private FileHostingServiceProvider mService;
	private boolean mStartAuthentication = false;

	private String mSdCardRootPath;
	private long mSdCardCapacity;
	private long mSdCardUsedSpace;

	private BackupManager mBackupManager;
	private List<IRecord> mRecordsList;

	private boolean mPossibleRootUser;
	private boolean mIsRootUser;

	// 产品信息
	private boolean mIsProductPaid;

	private TaskType mUnfinishedTaskType = null;
	private Intent mPendingIntent = null;
	private File mOriginalCacheDbFile = null;
	private String mLastWebBackupedDate = null;
	private long mLastWebBackupedSize = -1;
	/**
	 * 本地备份界面
	 */
	public static final int LOCAL_VIEW_ID = 0;
	/**
	 * 云备份界面
	 */
	public static final int WEB_VIEW_ID = 1;

	private ViewGroup mTabBar = null;
	private ViewGroup mWebLayout = null;
	private ViewGroup mLocalLayout = null;
	private TextView mLocalTab = null;
	private TextView mWebTab = null;
	private ImageView mLocalImg = null;
	private ImageView mWebImg = null;
	private ScrollerViewGroup mScrollerViewGroup;
	private int mCurTabId = -1;

	// 是否大陆版
	private boolean mIsInland = false;
	// 重做上次未完成任务点击取消时执行的Runnable ， run执行的操作会在ui线程执行
	private Runnable mFutureRunnable = null;

	private HashMap<Integer, Integer> mAccountIcons = new HashMap<Integer, Integer>();

	// 用户购买高级版入口标识
	private int mPurchaseEntrance = StatisticsKey.PURCHASE_FROM_INVALID_VALUE;

	private Tracker mTracker;

	private int[] mTabIds = new int[] { R.id.local_backup_tab, R.id.cloud_backup_tab,
			R.id.app_tools_tab };

	private int mEnabledAppCount;
	private int mDisabledAppCount;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//		long t = System.currentTimeMillis();
		super.onCreate(savedInstanceState);
		try {
			Bundle intentBundle = getIntent().getExtras();
			String intentAction = (String) intentBundle.get("backuptype");
			if (intentAction.equals("netBackup")) {
				mCurTabId = R.id.cloud_backup_tab;
			}
		} catch (Exception e) {

		}
		registerVersionUpdateEventRecevier();
		//进入这程序后在上传一次统计数据
		StatisticsTool.uploadStatisticsData(this, true);
		mPm = PreferenceManager.getInstance();
		mIsInland = Util.isInland(getApplicationContext());
		mPossibleRootUser = Util.isRootRom(this);
		mBackupManager = BackupManager.getInstance();

		initViews();

		boolean shouldCheckUpdate = VersionChecker.getInstance().shouldCheckUpdateNow(this);
		if (shouldCheckUpdate) {
			VersionChecker.getInstance().checkUpdate(this, true);
		}

		boolean shouldShowRootAlertDialog = mPm.getBoolean(this,
				PreferenceManager.KEY_SHOULD_SHOW_ROOT_ALERT_DIALOG, true);
		if (shouldShowRootAlertDialog) {
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					showRootAlertDialog(mPossibleRootUser);
				}
			}, 100);
		} else {
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					tryToGetRootPermission();
				}
			}, 100);
		}

		int defaultServiceType = mPm.getInt(this, PreferenceManager.KEY_NETWORK_BACKUP_TYPE, -1);
		if (defaultServiceType > 0) {
			mService = CloudServiceManager.getInstance().switchService(this, defaultServiceType);
		}

		mAccountIcons.put(FileHostingServiceProvider.INVALID_SERVICE,
				R.drawable.go_account_usercenter_btn);
		mAccountIcons.put(FileHostingServiceProvider.DROPBOX, R.drawable.account_dropbox);
		mAccountIcons.put(FileHostingServiceProvider.GOOGLE_DRIVE, R.drawable.account_google_drive);

		//		t = System.currentTimeMillis() - t;
		//		LogUtil.d("MainActivity onCreate time = " + t);
	}

	private void init() {
		mRecordsList = new ArrayList<IRecord>();
		List<IRecord> normalRecords = mBackupManager.getAllRestoreRecords();
		if (normalRecords != null && normalRecords.size() > 0) {
			mRecordsList.addAll(normalRecords);
		}
		List<IRecord> mergedRecords = mBackupManager.getMergableRecords();
		if (mergedRecords != null && mergedRecords.size() > 0) {
			mRecordsList.addAll(mergedRecords);
		}
		List<IRecord> scheduleRecords = mBackupManager.getScheduleRecords();
		if (scheduleRecords != null && scheduleRecords.size() > 0) {
			mRecordsList.addAll(scheduleRecords);
		}
	}

	private void initViews() {
		if (mIsInland) {
			setContentView(R.layout.layout_main_iniland);
		} else {
			setContentView(R.layout.layout_main);
		}
		getWindow().setFormat(PixelFormat.RGBA_8888);

		mRootTabPrompt = findViewById(R.id.root_tab_prompt);
		mRootTabPrompt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, RootIntroductionActivity.class);
				startActivity(intent);
			}
		});
		/*
		 * ViewGroup upperFrame = (ViewGroup) findViewById(R.id.upper_frame);
		 * Drawable uppperBg = upperFrame.getBackground(); if (uppperBg
		 * instanceof LayerDrawable) { LayerDrawable bg = (LayerDrawable)
		 * uppperBg; Drawable diagonalDrawable =
		 * bg.getDrawable(bg.getNumberOfLayers() - 1); if (diagonalDrawable
		 * instanceof BitmapDrawable) { ((BitmapDrawable)
		 * diagonalDrawable).setTileModeXY(TileMode.REPEAT, TileMode.REPEAT); }
		 * } ViewGroup lowerFrame = (ViewGroup) findViewById(R.id.lower_frame);
		 * Drawable lowerBg = lowerFrame.getBackground(); if (lowerBg instanceof
		 * LayerDrawable) { LayerDrawable bg = (LayerDrawable) lowerBg; Drawable
		 * diagonalDrawable = bg.getDrawable(bg.getNumberOfLayers() - 1); if
		 * (diagonalDrawable instanceof BitmapDrawable) { ((BitmapDrawable)
		 * diagonalDrawable).setTileModeXY(TileMode.REPEAT, TileMode.REPEAT); }
		 * }
		 */

		mPromptFrame = (ViewGroup) findViewById(R.id.prompt_frame);
		mOperationFrame = (ViewGroup) findViewById(R.id.operation_frame);

		mSchedulePlanButton = (ImageButton) findViewById(R.id.schedule_btn);
		mSchedulePlanButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, ScheduledPlanListActivity.class);
				startActivity(intent);
			}
		});

		mSettingButton = (ImageButton) findViewById(R.id.setting);
		mSettingButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showHideMenu();
			}
		});

		mLoginButton = (ImageButton) findViewById(R.id.login_btn);
		mLoginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO: 判断是否存在有购买记录：先查找本地，如果没有，再联网google
				if (!mIsProductPaid) {
					mPurchaseEntrance = StatisticsKey.PURCHASE_FROM_CLOUD_BACKUP;
					startPayHelpActivity();
					return;
				}
				showSwitchAccountDialog();
			}
		});

		mNewBackupButton = (ViewGroup) findViewById(R.id.new_backup_btn);
		mNewBackupButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// GA统计
				TrackerLog.i("MainActivity newBackupButton onClick");
				mTracker.trackEvent(TrackerEvent.CATEGORY_UI_ACTION,
						TrackerEvent.ACTION_BUTTON_PRESS, TrackerEvent.LABEL_MAIN_NEWBACKUP_BUTTON,
						TrackerEvent.OPT_CLICK);

				handleNewbackupButtonClick();
			}
		});

		final ImageView backupBtnDrawable = (ImageView) mNewBackupButton
				.findViewById(R.id.drawable);
		backupBtnDrawable.setImageResource(R.drawable.new_backup_btn_drawable);

		final TextView backupBtnTitle = (TextView) mNewBackupButton.findViewById(R.id.title);
		backupBtnTitle.setText(R.string.btn_new_backup);

		mBackupButtonSummary = (TextView) mNewBackupButton.findViewById(R.id.summary);

		mRestoreButton = (ViewGroup) findViewById(R.id.restore_btn);
		mRestoreButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startRecordsListActivity();
			}
		});

		final ImageView restoreBtnDrawable = (ImageView) mRestoreButton.findViewById(R.id.drawable);
		restoreBtnDrawable.setImageResource(R.drawable.restore_btn_drawable);

		final TextView restoreBtnTitle = (TextView) mRestoreButton.findViewById(R.id.title);
		restoreBtnTitle.setText(R.string.btn_restore);

		mRestoreButtonSummary = (TextView) mRestoreButton.findViewById(R.id.summary);

		// 新建云端备份
		mNewWebBackupButton = (ViewGroup) findViewById(R.id.new_web_backup_btn);
		if (mNewWebBackupButton != null) {
			mNewWebBackupButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mIsProductPaid) {
						handleNewCloudBackupButtonClick();
					} else {
						//	startPayHelpActivity();
						// 未付费用户直接进入新建页面
						startNewOnlineBackupActivity(true);
					}
				}
			});
			final ImageView webBackupBtnDrawable = (ImageView) mNewWebBackupButton
					.findViewById(R.id.drawable);
			webBackupBtnDrawable.setImageResource(R.drawable.new_web_drawable);

			final TextView webBackupBtnTitle = (TextView) mNewWebBackupButton
					.findViewById(R.id.title);
			webBackupBtnTitle.setText(R.string.btn_web_new_backup);

			mWebBackupButtonSummary = (TextView) mNewWebBackupButton.findViewById(R.id.summary);
		}

		// 恢复云端备份
		mWebRestoreButton = (ViewGroup) findViewById(R.id.restore_web_btn);
		if (mWebRestoreButton != null) {
			mWebRestoreButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (!mIsProductPaid) {
						StatisticsDataManager.getInstance().increaseStatisticInt(
								getApplicationContext(),
								StatisticsKey.PREMIUM_ENTRANCE_CLOUD_BACKUP, 1);
						mPurchaseEntrance = StatisticsKey.PURCHASE_FROM_CLOUD_BACKUP;
						startPayHelpActivity();
						return;
					}

					handleOnlineRestoreButtonClick();
				}
			});
			final ImageView webRestoreBtnDrawable = (ImageView) mWebRestoreButton
					.findViewById(R.id.drawable);
			webRestoreBtnDrawable.setImageResource(R.drawable.restore_web_drawable);

			final TextView webRestoreBtnTitle = (TextView) mWebRestoreButton
					.findViewById(R.id.title);
			webRestoreBtnTitle.setText(R.string.btn_web_restore);
			mWebRestoreButtonSummary = (TextView) mWebRestoreButton.findViewById(R.id.summary);
		}

		// 本地备份路径
		mBackupPath = (TextView) findViewById(R.id.backup_path);

		// 云端备份路径
		mWebBackupPath = (TextView) findViewById(R.id.web_backup_path);
		mCloudBackupTip = (TextView) findViewById(R.id.cloud_backup_tip);

		mSdCardUsageProgress = (ProgressBar) findViewById(R.id.sd_card_space_usage_progress);

		mSdCardUsageDesc = (TextView) findViewById(R.id.sd_card_usage_desc);

		// 初始化滚动功能页
		initScrollView();

		// 初始化tab页
		initTabView();
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

	private PopupWindow buildMenu() {
		String[] items = getResources().getStringArray(R.array.setting_more);
		NewPopupAdapter pAdapter = null;
		if (Util.isInland(this)) {
			String[] menuItems = new String[items.length - 1];
			for (int i = 0; i < items.length - 1; i++) {
				menuItems[i] = items[i];
			}
			pAdapter = new NewPopupAdapter(MainActivity.this, Arrays.asList(menuItems));
		} else {
			pAdapter = new NewPopupAdapter(MainActivity.this, Arrays.asList(items));
		}
		ListView popupListView = new ListView(MainActivity.this);
		popupListView.setAdapter(pAdapter);
		popupListView.setSelector(R.drawable.tab_btn_bg);
		popupListView.setCacheColorHint(0);
		popupListView.setDivider(getResources().getDrawable(R.drawable.pop_menu_listview_divider));
		popupListView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));

		final PopupWindow popupMenu = new PopupWindow(MainActivity.this);
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
				hideMenu();
				if (position == 0) {
					//设置
					Intent intent = new Intent(MainActivity.this, BackupSettingActivity.class);
					startActivity(intent);
				} else if (position == 1) {
					//帮助
					Intent intent = new Intent(MainActivity.this, BackupHelpActivity.class);
					startActivity(intent);
				} else if (position == 2) {
					//反馈问题
					doCommandFeedback();
				} else if (position == 3) {
					//关于
					Intent intent = new Intent(MainActivity.this, BackupAboutActivity.class);
					startActivity(intent);
				} else if (position == 4) {
					//高级版
					StatisticsDataManager.getInstance().increaseStatisticInt(
							getApplicationContext(), StatisticsKey.PREMIUM_ENTRANCE_MENU, 1);
					mPurchaseEntrance = StatisticsKey.PURCHASE_FROM_MENU;
					startPayHelpActivity();
				}
			}
		});

		return popupMenu;
	}

	private void showHideMenu() {
		if (mMenu == null) {
			mMenu = buildMenu();
		}
		if (mMenu.isShowing()) {
			hideMenu();
		} else {
			int xoff = 0;
			final Drawable background = mMenu.getBackground();
			if (background != null) {
				Rect tempRect = new Rect();
				background.getPadding(tempRect);
				xoff = -tempRect.left;
			}
			mMenu.showAsDropDown(mSettingButton, xoff, 0);
		}
	}

	private void hideMenu() {
		if (mMenu == null) {
			return;
		}
		mMenu.dismiss();
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

	private void handleNewCloudBackupButtonClick() {
		if (!isLastOnlineTaskFinished()) {
			mFutureRunnable = new Runnable() {
				@Override
				public void run() {
					if (mPendingIntent != null) {
						startActivity(mPendingIntent);
						mPendingIntent = null;
					}
				}
			};

			mPendingIntent = new Intent(MainActivity.this, NewOnlineBackupActivity.class);
			showRedoCloudTaskAlertDialog();
			return;
		}

		if (mService == null || !mService.isSessionValid()) {
			mLoginButton.performClick();
			return;
		}

		// 启动网络新建备份
		startNewOnlineBackupActivity(false);
	}

	private void startNewOnlineBackupActivity(boolean experience) {
		Intent intent = new Intent(MainActivity.this, NewOnlineBackupActivity.class);
		if (experience) {
			intent.putExtra(NewOnlineBackupActivity.EXTRA_EXPERIENCE, true);
		}
		startActivity(intent);
	}

	private void handleOnlineRestoreButtonClick() {
		if (!isLastOnlineTaskFinished()) {
			mFutureRunnable = new Runnable() {
				@Override
				public void run() {
					if (mPendingIntent != null) {
						startActivity(mPendingIntent);
						mPendingIntent = null;
					}
				}
			};

			mPendingIntent = new Intent(MainActivity.this, RestoreOnlineBackupActivity.class);
			showRedoCloudTaskAlertDialog();
			return;
		}

		if (mService == null || !mService.isSessionValid()) {
			mLoginButton.performClick();
			return;
		}

		Intent intent = new Intent(MainActivity.this, RestoreOnlineBackupActivity.class);
		startActivity(intent);
	}

	private boolean isLastOnlineTaskFinished() {
		File cacheTaskDbFile = new File(getCacheDir(), NetSyncTaskDbHelper.getDbName());
		if (!cacheTaskDbFile.exists()) {
			return true;
		}

		if (!Util.copyFile(cacheTaskDbFile.getAbsolutePath(),
				getDatabasePath(NetSyncTaskDbHelper.getDbName()).getAbsolutePath())) {
			// TODO 拷贝数据库出错处理
			return true;
		}
		boolean finished = false;
		boolean isDbDamage = false;
		NetSyncTaskDbHelper taskDb = new NetSyncTaskDbHelper(this, NetSyncTaskDbHelper.getDbName(),
				NetSyncTaskDbHelper.getDbVersion());
		File onlineBackupCacheDbFile = BackupManager.getOnlineBackupCacheDbFile(this, mService);
		try {
			if (taskDb.hasOnlineTask(TaskType.ONLINE_BACKUP) && onlineBackupCacheDbFile != null) {
				mUnfinishedTaskType = TaskType.ONLINE_BACKUP;
			} else if (taskDb.hasOnlineTask(TaskType.ONLINE_RESTORE)) {
				mUnfinishedTaskType = TaskType.ONLINE_RESTORE;
			} else {
				// 数据损坏，没有办法重新上次任务，删除task数据库
				isDbDamage = true;
				finished = true;
			}
		} finally {
			taskDb.close();
			if (isDbDamage) {
				cacheTaskDbFile.delete();
			}
		}
		return finished;
	}

	private String getRedoUnfinishedTaskText() {
		String text = mUnfinishedTaskType != null ? mUnfinishedTaskType == TaskType.ONLINE_BACKUP
				? getString(R.string.msg_redo_cloud_backup_task)
				: getString(R.string.msg_redo_cloud_restore_task) : null;
		return text;
	}

	private TaskType getRedoUnfinishedTaskType() {
		return mUnfinishedTaskType;
	}

	private boolean redoLaskOnlineTask() {
		File cacheTaskDbFile = BackupManager.getCloudTaskCacheDbFile(this);
		File cacheBackupDbFile = BackupManager.getOnlineBackupCacheDbFile(this, mService);

		if (mUnfinishedTaskType == null) {
			Util.copyFile(cacheTaskDbFile.getAbsolutePath(),
					getDatabasePath(NetSyncTaskDbHelper.getDbName()).getAbsolutePath());
			NetSyncTaskDbHelper taskDb = new NetSyncTaskDbHelper(this,
					NetSyncTaskDbHelper.getDbName(), NetSyncTaskDbHelper.getDbVersion());
			if (taskDb.hasOnlineTask(TaskType.ONLINE_BACKUP)) {
				mUnfinishedTaskType = TaskType.ONLINE_BACKUP;
			} else if (taskDb.hasOnlineTask(TaskType.ONLINE_RESTORE)) {
				mUnfinishedTaskType = TaskType.ONLINE_RESTORE;
			}
			taskDb.close();
			if (mUnfinishedTaskType == null) {
				return false;
			}
		}

		if (mUnfinishedTaskType == TaskType.ONLINE_BACKUP) {
			startUploadProcessActivity(cacheTaskDbFile, cacheBackupDbFile);
		} else {
			startProcessActivity(cacheTaskDbFile);
		}
		return true;
	}

	private void startPayHelpActivity() {
		Intent intent = new Intent(MainActivity.this, PayUpdateHelpActivity.class);
		intent.putExtra(PayUpdateHelpActivity.EXTRA_IS_PAID, mIsProductPaid);
		intent.putExtra(PayUpdateHelpActivity.EXTRA_PURCHASE_REQUEST_SOURCE, mPurchaseEntrance);
		startActivity(intent);
		mPm.putBoolean(this, PreferenceManager.KEY_HAS_SHOWN_PAY_HELP_PAGE, true);
	}

	private void startUploadProcessActivity(File taskDbFile, File backupDbFile) {
		if (taskDbFile == null || backupDbFile == null) {
			return;
		}
		Intent intent = new Intent(MainActivity.this, UploadBackupProcessActivity.class);
		intent.putExtra(UploadBackupProcessActivity.EXTRA_TASK_DB_FILE, taskDbFile);
		intent.putExtra(UploadBackupProcessActivity.EXTRA_BACKUP_DB_FILE, backupDbFile);
		startActivity(intent);
	}

	private void startProcessActivity(File taskDbFile) {
		if (taskDbFile == null) {
			return;
		}
		Intent intent = new Intent(MainActivity.this, RestoreOnlineBackupProcessAcitvity.class);
		intent.putExtra(RestoreOnlineBackupProcessAcitvity.EXTRA_TASK_DB_FILE, taskDbFile);
		startActivity(intent);
	}

	private void showToast(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}

	private void finishFileHostingServiceAuthentication() {
		showWaitProgressDialog(R.string.tip_loging_in, false);
		mService.finishAuthentication(this, new ActionListener() {
			@Override
			public void onProgress(long progress, long total, Object data) {
			}

			@Override
			public void onCancel(Object data) {
			}

			@Override
			public void onError(int errCode, String errMessage, Object data) {
				mService = null;
				mStartAuthentication = false;
				Message.obtain(mHandler, MSG_DISMISS_DIALOG, mWaitDialog).sendToTarget();
				Message.obtain(mHandler, MSG_SHOW_TOAST, errMessage).sendToTarget();
				Message.obtain(mHandler, MSG_UPDATE_VIEWS).sendToTarget();
			}

			@Override
			public void onComplete(Object data) {
				Message.obtain(mHandler, MSG_DISMISS_DIALOG, mWaitDialog).sendToTarget();
				mStartAuthentication = false;
				Message.obtain(mHandler, MSG_UPDATE_VIEWS).sendToTarget();
			}
		});
	}

	private void handleNewbackupButtonClick() {
		// 检查普通备份记录个数
		List<IRecord> normalRecords = mBackupManager.getAllRestoreRecords();
		if (normalRecords != null
				&& normalRecords.size() >= BackupManager.getInstance().getMaxBackupCount()) {
			// 弹出限制对话框
			showRecordLimitAlertDialog();
		} else {
			startBackupActivity();
		}
	}

	public void checkRecordPackagesAndBackup() {
		// 检查普通备份记录个数
		List<IRecord> normalRecords = mBackupManager.getAllRestoreRecords();
		if (normalRecords != null
				&& normalRecords.size() >= BackupManager.getInstance().getMaxBackupCount()) {
			// 弹出限制对话框
			showRecordLimitAlertDialog();
		} else {
			//					去备份
			startBackupActivity();
		}
	}

	private void startBackupActivity() {
		if (checkReady()) {
			Intent intent = new Intent(this, NewBackupActivity.class);
			startActivity(intent);
		}
	}

	private void startRecordsListActivity() {
		if (checkReady()) {
			// BackupRecordsActivity
			Intent intent = new Intent(this, RecordsListActivity.class);
			startActivity(intent);
		}
	}

	// 检查存储卡是否已挂载
	private boolean checkReady() {
		boolean ret = Util.checkSdCardReady(this);
		return ret;
	}

	@Override
	protected void onStart() {
		//		long t = System.currentTimeMillis();
		super.onStart();

		mIsProductPaid = Util.isInland(this) || ProductManager.isPaid(this);

		init();

		mService = CloudServiceManager.getInstance().getCurrentService();

		updateSdCardInfo();

		updateViews();
		updateAppToolsFrame();
		//		t = System.currentTimeMillis() - t;
		//		LogUtil.d("MainActivity onStart time = " + t);

		EasyTracker.getInstance().activityStart(this);
		mTracker = EasyTracker.getTracker();
		//		mTracker.trackView("MainActivity");
	}

	@Override
	protected void onResume() {
		// Log.d("GoBackup", "onResume");
		super.onResume();
		updateRootTabPrompt();
		// 检查是否有需要升级数据库的旧版本记录 v2.0以下
		updateOldBackupRecord();

		if (mService != null && mStartAuthentication) {
			finishFileHostingServiceAuthentication();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();

		EasyTracker.getInstance().activityStop(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterVersionUpdateEventReceiver();
		release();
	}

	private void startBatchDeleteRecordsActivity() {
		Intent intent = new Intent(this, BatchDeleteRecordsActivity.class);
		startActivity(intent);
	}

	private void exit() {
		finish();
	}

	/**
	 * 更新界面上付费标记
	 */
	private void updatePayInfoMarker() {
		// Tab栏的付费标记
		final View cloudBackupTab = findViewById(R.id.cloud_backup_tab);
		if (cloudBackupTab != null) {
			final boolean hasShownCloudBackupTab = mPm.getBoolean(this,
					PreferenceManager.KEY_HAS_SHOWN_CLOUD_BACKUP_TAB, false);
			cloudBackupTab.findViewById(R.id.tab_purchase_icon).setVisibility(
					mIsProductPaid || hasShownCloudBackupTab ? View.GONE : View.VISIBLE);
		}

		final View appToolsTab = findViewById(R.id.app_tools_tab);
		if (appToolsTab != null) {
			final boolean hasShownAppToolsTab = mPm.getBoolean(this,
					PreferenceManager.KEY_HAS_SHOWN_APP_TOOLS_TAB, false);
			appToolsTab.findViewById(R.id.tab_purchase_icon).setVisibility(
					mIsProductPaid || hasShownAppToolsTab ? View.GONE : View.VISIBLE);
		}

		if (mNewWebBackupButton != null) {
			// 新建云端备份付费标记
			View shoppingCartIcon = mNewWebBackupButton.findViewById(R.id.mut_purchase_icon);
			if (shoppingCartIcon != null) {
				shoppingCartIcon.setVisibility(mIsProductPaid ? View.GONE : View.VISIBLE);
			}
		}

		if (mWebRestoreButton != null) {
			// 恢复云端备份付费标记
			View shoppingCartIcon = mWebRestoreButton.findViewById(R.id.mut_purchase_icon);
			if (shoppingCartIcon != null) {
				shoppingCartIcon.setVisibility(mIsProductPaid ? View.GONE : View.VISIBLE);
			}
		}
	}

	private void updateNewBackupButton() {
		if (mRecordsList != null && mRecordsList.size() > 0) {
			RestorableRecord latestRecord = mBackupManager.getLatestRestoreRecord();
			if (latestRecord != null) {
				String dateString = new SimpleDateFormat(RECORD_DATE_FORMAT).format(latestRecord
						.getDate());
				mBackupButtonSummary.setText(getString(R.string.msg_last_backup_time) + dateString);
			}
		} else {
			mBackupButtonSummary.setText(R.string.msg_no_backups);
		}
		
		final View newFeatureTag = mNewBackupButton.findViewById(R.id.new_feature_tag);
		if (newFeatureTag != null) {
			if (!mIsProductPaid && !mPm.getBoolean(this, PreferenceManager.KEY_BACKUP_DETAILS_NEW_FEATURE, false)) {
				newFeatureTag.setVisibility(View.VISIBLE);
			} else {
				newFeatureTag.setVisibility(View.GONE);
			}
		}
	}

	private void updateRestoreButton() {
		if (mRecordsList != null && mRecordsList.size() > 0) {
			final int recordCount = mRecordsList.size();
			mRestoreButtonSummary.setText(recordCount + getString(R.string.msg_backup_nums) + "  "
					+ Util.formatFileSize(calcAllBackupsSize()));
			
			final View newFeatureTag = mRestoreButton.findViewById(R.id.new_feature_tag);
			if (newFeatureTag != null) {
				if (!mIsProductPaid && !mPm.getBoolean(this, PreferenceManager.KEY_RESTORE_BACKUP_NEW_FEATURE, false)) {
					newFeatureTag.setVisibility(View.VISIBLE);
				} else {
					newFeatureTag.setVisibility(View.GONE);
				}
			}
		} else {
			mRestoreButtonSummary.setText(R.string.msg_no_backups);
		}
	}

	private void updateWebNewBackupButton() {
		// 查看缓存，判断是否有本地缓存的数据库，如果有则显示缓存的时间，否则显示"暂无数据"
		if (mWebBackupButtonSummary != null) {
			if (reflashLastNetBackupedDateAndSizeFromCacheDb()) {
				mWebBackupButtonSummary.setText(getString(R.string.msg_last_backup_time)
						+ mLastWebBackupedDate);
			} else {
				mWebBackupButtonSummary.setText(getString(R.string.main_net_no_data));
			}
		}
	}

	private boolean reflashLastNetBackupedDateAndSizeFromCacheDb() {
		File cacheDbFile = BackupManager.getOnlineBackupCacheDbFile(this, mService);
		if (cacheDbFile == null || !cacheDbFile.exists()) {
			// 本地如果没有缓存文件，reset变量
			mLastWebBackupedDate = null;
			mLastWebBackupedSize = -1;
			return false;
		}
		if (mOriginalCacheDbFile == null) {
			mOriginalCacheDbFile = cacheDbFile;
		}
		if (mOriginalCacheDbFile.getAbsolutePath().equals(cacheDbFile.getAbsolutePath())) {
			if (mLastWebBackupedDate != null && mLastWebBackupedSize != -1) {
				return true;
			}
		} else {
			mOriginalCacheDbFile = cacheDbFile;
		}

		File tempDbFile = new File(getCacheDir(), BackupDBHelper.getDBName());
		Util.copyFile(mOriginalCacheDbFile.getAbsolutePath(), tempDbFile.getAbsolutePath());
		RestorableRecord record = new RestorableRecord(this, tempDbFile.getParent());
		Date date = record.getDate();
		tempDbFile.delete();
		if (date == null) {
			mLastWebBackupedDate = null;
			mLastWebBackupedSize = -1;
			return false;
		}
		mLastWebBackupedDate = new SimpleDateFormat(RECORD_DATE_FORMAT).format(date);
		mLastWebBackupedSize = record.getSpaceUsage();
		return true;
	}

	private void updateWebRestoreButton() {
		// 查看缓存，显示备份大小，如果没有缓存，显示”暂无数据“
		if (mWebRestoreButtonSummary != null) {
			if (reflashLastNetBackupedDateAndSizeFromCacheDb()) {
				mWebRestoreButtonSummary.setText(Util.formatFileSize(mLastWebBackupedSize));
			} else {
				mWebRestoreButtonSummary.setText(getString(R.string.main_net_no_data));
			}
		}
	}

	private long calcAllBackupsSize() {
		if (mRecordsList == null || mRecordsList.size() <= 0) {
			return 0;
		}
		long size = 0;
		for (IRecord record : mRecordsList) {
			size += record.getSpaceUsage();
		}
		return size;
	}

	private String getBackupsFullPath() {
		if (TextUtils.isEmpty(mSdCardRootPath)) {
			return getString(R.string.unavailable);
		}
		final String backupPath = mSdCardRootPath + Constant.GOBACKUP_ROOT_DIR + "/";
		return backupPath;
	}

	private void updateSdCardInfo() {
		mSdCardRootPath = Util.ensureFileSeparator(Util.getSdRootPathOnPreference(this));
		mSdCardCapacity = getSdCardCapacity();
		mSdCardUsedSpace = getSdCardUsedSpace();
	}

	private void updateSdCardUsageProgress() {
		final int progressWeight = 100;
		final float sufficientStateThreshold = 0.5f;
		final float fineStateThreshold = 0.85f;
		int progress = (int) ((float) mSdCardUsedSpace / (float) mSdCardCapacity * progressWeight);
		if (progress != mSdCardUsageProgress.getProgress()) {
			Resources res = getResources();
			Drawable progressDrawable = null;
			if (mSdCardUsedSpace <= (long) (mSdCardCapacity * sufficientStateThreshold)) {
				progressDrawable = res.getDrawable(R.drawable.sd_card_usage_sufficient_progress);
			} else if (mSdCardUsedSpace < (long) (mSdCardCapacity * fineStateThreshold)) {
				progressDrawable = res.getDrawable(R.drawable.sd_card_usage_fine_progress);
			} else {
				progressDrawable = res.getDrawable(R.drawable.sd_card_usage_insufficient_progress);
			}
			// 修复进度条在设置新的progressDrawable后显示不出来的问题
			Drawable oldDrawable = mSdCardUsageProgress.getProgressDrawable();
			if (oldDrawable != null && oldDrawable.getBounds() != null) {
				progressDrawable.setBounds(oldDrawable.getBounds());
			}
			mSdCardUsageProgress.setProgressDrawable(progressDrawable);
			mSdCardUsageProgress.setProgress(progress);
		}
	}

	private long getSdCardCapacity() {
		if (TextUtils.isEmpty(mSdCardRootPath)) {
			return 0;
		}
		StatFs stat = null;
		try {
			stat = new StatFs(mSdCardRootPath);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.e("SdCard Error", "Sdcard path = " + mSdCardRootPath);
			Logger.flush();
			return 0;
		}
		return (long) stat.getBlockCount() * (long) stat.getBlockSize();
	}

	/*	private long getSdCardAvaliableSpace() {
			if (TextUtils.isEmpty(mSdCardRootPath)) {
				return 0;
			}
			StatFs stat = new StatFs(mSdCardRootPath);
			return (long) stat.getAvailableBlocks() * (long) stat.getBlockSize();
		}*/

	private long getSdCardUsedSpace() {
		if (TextUtils.isEmpty(mSdCardRootPath)) {
			return 0;
		}
		StatFs statFs = null;
		try {
			statFs = new StatFs(mSdCardRootPath);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.e("SdCard Error", "Sdcard path = " + mSdCardRootPath);
			Logger.flush();
			return 0;
		}
		return (long) (statFs.getBlockCount() - statFs.getAvailableBlocks())
				* (long) statFs.getBlockSize();
	}

	private void showOperationFrame() {
		mOperationFrame.setVisibility(View.VISIBLE);
		mPromptFrame.setVisibility(View.GONE);
	}

	private void showPromptFrame() {
		mOperationFrame.setVisibility(View.GONE);
		mPromptFrame.setVisibility(View.VISIBLE);
	}

	private void updateUpperFrame() {
		if (checkReady()) {
			showOperationFrame();
		} else {
			showPromptFrame();
		}
		updateNewBackupButton();
		updateRestoreButton();
		updateWebNewBackupButton();
		updateWebRestoreButton();
		updatePayInfoMarker();
	}

	private void updateLowerFrame() {
		final TextView sdCardUnmounted = (TextView) findViewById(R.id.sd_card_unmounted);
		final ViewGroup sdCardState = (ViewGroup) findViewById(R.id.sd_card_state);
		if (checkReady()) {
			sdCardUnmounted.setVisibility(View.INVISIBLE);
			sdCardState.setVisibility(View.VISIBLE);
		} else {
			sdCardUnmounted.setVisibility(View.VISIBLE);
			sdCardState.setVisibility(View.INVISIBLE);
		}

		mBackupPath.setText(getBackupsFullPath());
		String result = Util.formatFileSize(mSdCardUsedSpace) + "/"
				+ Util.formatFileSize(mSdCardCapacity);
		mSdCardUsageDesc.setText(result);
		updateSdCardUsageProgress();

		View pathDetail = findViewById(R.id.cloud_backup_path_detail);
		if (mService == null || !mService.isSessionValid()) {
			if (mCloudBackupTip != null) {
				mCloudBackupTip.setVisibility(View.GONE);
			}
			if (pathDetail != null) {
				pathDetail.setVisibility(View.GONE);
			}
		} else {
			if (mCloudBackupTip != null) {
				mCloudBackupTip.setVisibility(View.VISIBLE);
			}
			if (pathDetail != null) {
				pathDetail.setVisibility(View.VISIBLE);
			}
		}

		if (mCloudBackupTip != null && mService != null) {
			final CharSequence serviceName = getResources().getTextArray(
					R.array.cloud_service_provider)[mService.getType()];
			mCloudBackupTip.setText(getString(R.string.cloud_backup_tip, serviceName));
		}
		if (mWebBackupPath != null && mService != null) {
			final CharSequence format = getResources().getTextArray(
					R.array.cloud_service_backup_path)[mService.getType()];
			String path = String.format(format.toString(), mService.getOnlineBackupPath());
			mWebBackupPath.setText(path);
		}
	}

	private void updateViews() {
		updateUpperFrame();
		updateLowerFrame();
		if (mLoginButton != null) {
			if (isCloudServiceValid()) {
				mLoginButton.setImageResource(mAccountIcons.get(mService.getType()));
			} else {
				mLoginButton.setImageResource(mAccountIcons
						.get(FileHostingServiceProvider.INVALID_SERVICE));
			}
		}
	}

	private void updateViewPager() {
		if (mScrollerViewGroup != null) {
			View appToolsFrame = mScrollerViewGroup.findViewById(R.id.app_tools_frame);
			if (mIsRootUser && appToolsFrame == null) {
				mScrollerViewGroup.addView(initAppToolsFrame());
			} else if (!mIsRootUser && appToolsFrame != null) {
				mScrollerViewGroup.removeView(appToolsFrame);
			}
			mScrollerViewGroup.setScreenCount(mScrollerViewGroup.getChildCount());
		}
		if (mTabBar != null) {
			if (mIsRootUser) {
				mTabBar.findViewById(R.id.app_tools_tab).setVisibility(View.VISIBLE);
				mTabBar.findViewById(R.id.seporator_line_2).setVisibility(View.VISIBLE);
			} else {
				mTabBar.findViewById(R.id.app_tools_tab).setVisibility(View.GONE);
				mTabBar.findViewById(R.id.seporator_line_2).setVisibility(View.GONE);
			}
		}
		updateAppToolsFrame();
	}

	private View initAppToolsFrame() {
		View appToolsFrame = getLayoutInflater().inflate(R.layout.app_tools_frame, null);
		mFreezeAppButton = (ViewGroup) appToolsFrame.findViewById(R.id.freeze_app);
		((TextView) mFreezeAppButton.findViewById(R.id.title)).setText(R.string.title_freeze_app);
		((ImageView) mFreezeAppButton.findViewById(R.id.drawable))
				.setImageResource(R.drawable.btn_freeze_app);
		mFreezeAppButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
/*				if (!mIsProductPaid) {
					StatisticsDataManager.getInstance().increaseStatisticInt(
							getApplicationContext(), StatisticsKey.PREMIUM_ENTRANCE_FREEZE_APP, 1);
					mPurchaseEntrance = StatisticsKey.PURCHASE_FROM_FREEZE_APP;
					startPayHelpActivity();
					return;
				}*/

				startFreezeActivity();
			}
		});

		mUnfreezeAppButton = (ViewGroup) appToolsFrame.findViewById(R.id.unfreeze_app);
		((TextView) mUnfreezeAppButton.findViewById(R.id.title))
				.setText(R.string.title_unfreeze_app);
		((ImageView) mUnfreezeAppButton.findViewById(R.id.drawable))
				.setImageResource(R.drawable.btn_unfreeze_app);
		mUnfreezeAppButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!mIsProductPaid) {
					StatisticsDataManager.getInstance().increaseStatisticInt(
							getApplicationContext(), StatisticsKey.PREMIUM_ENTRANCE_FREEZE_APP, 1);
					mPurchaseEntrance = StatisticsKey.PURCHASE_FROM_FREEZE_APP;
					startPayHelpActivity();
					return;
				}

				startUnfreezeActivity();
			}
		});

		appToolsFrame.findViewById(R.id.bottom_bar).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, BackupHelpActivity.class);
				intent.putExtra(BackupHelpActivity.EXTRA_TAB_ID, BackupHelpActivity.RULES_TAB_ID);
				startActivity(intent);
			}
		});

		return appToolsFrame;
	}

	private void startFreezeActivity() {
		if (mEnabledAppCount <= 0) {
			showToast(getString(R.string.msg_no_app_to_freeze));
			return;
		}
		Intent intent = new Intent(MainActivity.this, FreezeAppActivity.class);
		intent.putExtra(FreezeAppActivity.EXTRA_ACTION, FreezeAppActivity.ACTION_FREEZE_APPS);
		startActivity(intent);
	}

	private void startUnfreezeActivity() {
		if (mDisabledAppCount <= 0) {
			showToast(getString(R.string.msg_no_app_to_unfreeze));
			return;
		}
		Intent intent = new Intent(MainActivity.this, FreezeAppActivity.class);
		intent.putExtra(FreezeAppActivity.EXTRA_ACTION, FreezeAppActivity.ACTION_UNFREEZE_APPS);
		startActivity(intent);
	}

	private void updateAppToolsFrame() {
		if (mFreezeAppButton != null) {
			((TextView) mFreezeAppButton.findViewById(R.id.summary))
					.setText(getString(R.string.msg_caculating));
			mFreezeAppButton.findViewById(R.id.mut_purchase_icon).setVisibility(
					mIsProductPaid ? View.GONE : View.VISIBLE);
		}
		if (mUnfreezeAppButton != null) {
			((TextView) mUnfreezeAppButton.findViewById(R.id.summary))
					.setText(getString(R.string.msg_caculating));
			mUnfreezeAppButton.findViewById(R.id.mut_purchase_icon).setVisibility(
					mIsProductPaid ? View.GONE : View.VISIBLE);
		}

		if (mFreezeAppButton != null && mUnfreezeAppButton != null) {
			postAsyncTask(new Runnable() {
				@Override
				public void run() {
					mEnabledAppCount = AppFreezer.getEnabledPackagesCount(getApplicationContext());
					mDisabledAppCount = AppFreezer
							.getDisabledPackagesCount(getApplicationContext());
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (mFreezeAppButton != null) {
								((TextView) mFreezeAppButton.findViewById(R.id.summary))
										.setText(getString(R.string.freezable_app_info,
												mEnabledAppCount));
							}
							if (mUnfreezeAppButton != null) {
								((TextView) mUnfreezeAppButton.findViewById(R.id.summary))
										.setText(getString(R.string.unfreezable_app_info,
												mDisabledAppCount));
							}
						}
					});
				}
			});
		}
	}

	private void onSdCardStateChanged() {
		updateSdCardInfo();
		updateViews();
	}

	@Override
	protected void onSdCardUnmounted() {
		super.onSdCardUnmounted();
		release();
		onSdCardStateChanged();
	}

	@Override
	protected void onSdCardMounted() {
		super.onSdCardMounted();
		init();
		onSdCardStateChanged();
	}

	private void showRootAlertDialog(final boolean possibleRootUser) {
		long t = System.currentTimeMillis();
		String rootState = possibleRootUser ? getString(R.string.root) : getString(R.string.unroot);
		String message = possibleRootUser
				? getString(R.string.msg_root_user_alert, rootState)
				: getString(R.string.msg_unroot_user_alert, rootState);
		Dialog dialog = new AlertDialog.Builder(this).setTitle(R.string.attention)
				.setIcon(android.R.drawable.ic_dialog_alert).setMessage(message)
				.setCancelable(false)
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						tryToGetRootPermission();
						mPm.putBoolean(MainActivity.this,
								PreferenceManager.KEY_SHOULD_SHOW_ROOT_ALERT_DIALOG, false);
					}
				}).create();
		showDialog(dialog);
		t = System.currentTimeMillis() - t;
		LogUtil.d("show root alert dialog time = " + t);
	}

	// 试图获取Root权限
	private void tryToGetRootPermission() {
		long t = System.currentTimeMillis();
		if (!mPossibleRootUser) {
			unauthorizedRoot();
			return;
		}
		showWaitProgressDialog(R.string.msg_loading, true);
		postAsyncTask(new Runnable() {
			@Override
			public void run() {
				mIsRootUser = RootShell.isRootValid();
				if (mIsRootUser) {
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							hideRootTabPrompt();
							updateViewPager();
						}
					});
				}
				Message.obtain(mHandler, MSG_DISMISS_DIALOG, mWaitDialog).sendToTarget();
			}
		});
		t = System.currentTimeMillis() - t;
//		LogUtil.d("try to get root time " + t);
	}

	private void unauthorizedRoot() {
		mIsRootUser = false;
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				showRootTabPrompt();
				updateViewPager();
			}
		});
	}

	private void showRootTabPrompt() {
		mRootTabPrompt.setVisibility(View.VISIBLE);
		hideRootTabPromptWhenNeed();
	}

	private void hideRootTabPrompt() {
		mRootTabPrompt.setVisibility(View.GONE);
	}

	private void hideRootTabPromptWhenNeed() {
		// 10秒后隐藏
		final int hidePromptDelay = 10 * 1000;
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				mRootTabPrompt.setVisibility(View.GONE);
				// pm.putBoolean(MainActivity.this,
				// PreferenceManager.KEY_SHOULD_SHOW_ROOT_TAB_PROMT, false);
			}
		}, hidePromptDelay);
	}

	private void updateRootTabPrompt() {
		// boolean shouldShowRootTabPrompt = pm.getBoolean(this,
		// PreferenceManager.KEY_SHOULD_SHOW_ROOT_TAB_PROMT, true);
		if (mRootTabPrompt.getVisibility() == View.VISIBLE) {
			hideRootTabPromptWhenNeed();
		}
	}

	public void release() {
		if (mRecordsList != null) {
			mRecordsList.clear();
		}
		mRecordsList = null;
	}

	private void showWaitProgressDialog(int messageResId, boolean cancelable) {
		if (mWaitDialog == null) {
			mWaitDialog = createSpinnerProgressDialog(cancelable);
			String msg = getString(messageResId);
			mWaitDialog.setMessage(msg);
		}
		showDialog(mWaitDialog);
	}

	private void showUpdateProgressDialog(int maxValue) {
		if (mUpdateProgressDialog == null) {
			mUpdateProgressDialog = new ProgressDialog(this);
			mUpdateProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mUpdateProgressDialog.setTitle(R.string.alert_dialog_title);
			mUpdateProgressDialog.setMessage(getString(R.string.msg_update_old_backup));
			mUpdateProgressDialog.setIndeterminate(false);
			mUpdateProgressDialog.setCancelable(false);
			mUpdateProgressDialog.setMax(maxValue);
		}
		showDialog(mUpdateProgressDialog);
	}

	private void updateProgressDialog(int progress) {
		if (mUpdateProgressDialog == null) {
			return;
		}
		mUpdateProgressDialog.setProgress(progress);
	}

	private void showRecordLimitAlertDialog() {
		if (mRecordLimitAlertDialog == null) {
			mRecordLimitAlertDialog = new AlertDialog.Builder(this)
					.setTitle(R.string.alert_dialog_title)
					.setCancelable(true)
					.setMessage(
							getString(R.string.msg_limit_record, BackupManager.getInstance()
									.getMaxBackupCount()))
					.setNegativeButton(R.string.cancel, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					})
					.setPositiveButton(R.string.btn_limit_record_manage_record,
							new OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									startBatchDeleteRecordsActivity();
									dialog.dismiss();
								}
							}).create();
		}
		showDialog(mRecordLimitAlertDialog);
	}

	private void showRedoCloudTaskAlertDialog() {
		if (mRedoCloudTaskAlertDialog == null) {
			mRedoCloudTaskAlertDialog = new AlertDialog.Builder(this)
					.setTitle(R.string.continue_last_task).setMessage(getRedoUnfinishedTaskText())
					.setCancelable(true).setPositiveButton(R.string.ok, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							redoLaskOnlineTask();
							dialog.dismiss();
							//							mLoginButton.setSelection(0);
							mPendingIntent = null;
						}
					}).setNegativeButton(R.string.no, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// 删除上次task文件 TODO
							File cacheTaskDbFile = BackupManager
									.getCloudTaskCacheDbFile(MainActivity.this);
							if (cacheTaskDbFile != null) {
								cacheTaskDbFile.delete();
							}
							File cacheBackupDbFile = BackupManager.getOnlineBackupCacheDbFile(
									MainActivity.this, mService);
							if (cacheBackupDbFile != null) {
								cacheBackupDbFile.delete();
							}

							if (mFutureRunnable != null) {
								MainActivity.this.runOnUiThread(mFutureRunnable);
								mFutureRunnable = null;
							}

							dialog.dismiss();
						}
					}).setOnCancelListener(new OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							mPendingIntent = null;
							mFutureRunnable = null;
						}
					}).create();
		} else {
			((AlertDialog) mRedoCloudTaskAlertDialog).setMessage(getRedoUnfinishedTaskText());
		}
		showDialog(mRedoCloudTaskAlertDialog);
	}

	/**
	 * 对2.0版本之前的备份记录的数据库进行升级
	 */
	private void updateOldBackupRecord() {
		final List<IRecord> recordsToUpdate = mBackupManager.getRecordsNeedToUpdate();
		if (Util.isCollectionEmpty(recordsToUpdate)) {
			return;
		}
		new UpdateOldBackupTask().execute();
	}

	private static final int MSG_DISMISS_DIALOG = 0x1003;
	private static final int MSG_UPDATE_PROGRESS_DIALOG = 0x1004;
	private static final int MSG_SHOW_TOAST = 0x1005;
	private static final int MSG_UPDATE_LOGIN_BUTTON = 0x1006;
	private static final int MSG_UPDATE_VIEWS = 0x1007;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_DISMISS_DIALOG :
					dismissDialog((Dialog) msg.obj);
					break;
				case MSG_UPDATE_PROGRESS_DIALOG :
					updateProgressDialog(msg.arg1);
					break;
				case MSG_SHOW_TOAST :
					if (msg.obj != null) {
						showToast(msg.obj.toString());
					}
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
	private class UpdateOldBackupTask extends AsyncTask<Void, Integer, Boolean> {
		List<IRecord> mOldRecords = null;

		@Override
		protected void onPreExecute() {
			mOldRecords = mBackupManager.getRecordsNeedToUpdate();
			showUpdateProgressDialog(mOldRecords.size());
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			if (Util.isCollectionEmpty(mOldRecords)) {
				return false;
			}
			final int count = mOldRecords.size();
			for (int i = 0; i < count; i++) {
				RestorableRecord record = (RestorableRecord) mOldRecords.get(i);
				record.updateRecordFromV11ToV20();
				publishProgress(i + 1);
			}
			return true;
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			updateProgressDialog(progress[0]);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			mOldRecords.clear();
			dismissDialog(mUpdateProgressDialog);
			updateUpperFrame();
		}
	}

	/**
	 * 初始化滚动功能页
	 */
	private void initScrollView() {
		mScrollerViewGroup = (ScrollerViewGroup) findViewById(R.id.scrollerPageView);
		if (mScrollerViewGroup != null) {
			mScrollerViewGroup.setScreenCount(mScrollerViewGroup.getChildCount());
			mScrollerViewGroup.setScreenScrollerListener(this);
			if (mCurTabId < 0) {
				mCurTabId = getTabIndex(R.id.local_backup_tab);
			}
			mScrollerViewGroup.gotoViewByIndex(mCurTabId);
		}
	}

	/**
	 * 初始化tab页
	 */
	private void initTabView() {
		mTabBar = (ViewGroup) findViewById(R.id.mainview_tab);
		if (mTabBar != null) {
			final int childCount = mTabBar.getChildCount();
			for (int i = 0; i < childCount; i++) {
				final View childTabView = mTabBar.getChildAt(i);
				if (childTabView instanceof ViewGroup) {
					childTabView.setOnClickListener(this);
				}
				final int id = childTabView.getId();
				final TextView textView = (TextView) childTabView.findViewById(R.id.tab_text);
				switch (id) {
					case R.id.local_backup_tab :
						textView.setText(R.string.local_backup);
						break;
					case R.id.cloud_backup_tab :
						textView.setText(R.string.cloud_backup);
						break;
					case R.id.app_tools_tab :
						textView.setText(R.string.app_tools);
						break;
					default :
						break;
				}
			}
			focusViewTab(R.id.local_backup_tab);
		}
	}

	/**
	 * 本地tab选中状态
	 */
	private void focusViewTab(int tabId) {
		if (mTabBar != null) {
			final int childCount = mTabBar.getChildCount();
			for (int i = 0; i < childCount; i++) {
				final View childView = mTabBar.getChildAt(i);
				if (childView instanceof ViewGroup) {
					TextView text = (TextView) childView.findViewById(R.id.tab_text);
					text.setTextColor(getResources().getColor(R.color.main_tab_text_unfocus));
					View indicator = childView.findViewById(R.id.tab_indicator);
					indicator.setVisibility(View.GONE);
				}
			}
			View selectedView = mTabBar.findViewById(tabId);
			if (selectedView != null) {
				TextView text = (TextView) selectedView.findViewById(R.id.tab_text);
				text.setTextColor(getResources().getColor(R.color.main_tab_text_focused));
				View indicator = selectedView.findViewById(R.id.tab_indicator);
				indicator.setVisibility(View.VISIBLE);
			}
		}

	}

	@Override
	public ScreenScroller getScreenScroller() {
		return null;
	}

	@Override
	public void setScreenScroller(ScreenScroller scroller) {

	}

	@Override
	public void onFlingIntercepted() {

	}

	@Override
	public void onScrollStart() {
		mHandler.removeCallbacks(mFreezeAppTutorial);
	}

	@Override
	public void onFlingStart() {

	}

	@Override
	public void onScrollChanged(int newScroll, int oldScroll) {

	}

	@Override
	public void onScreenChanged(int newScreen, int oldScreen) {

	}

	@Override
	public void onScrollFinish(int currentScreen) {
		if (currentScreen < 0 || currentScreen >= mTabIds.length) {
			return;
		}
		mCurTabId = mTabIds[currentScreen];
		switch (mCurTabId) {
			case R.id.local_backup_tab :
				mSchedulePlanButton.setVisibility(View.VISIBLE);
				mLoginButton.setVisibility(View.GONE);
				break;
			case R.id.cloud_backup_tab :
				mSchedulePlanButton.setVisibility(View.GONE);
				mLoginButton.setVisibility(View.VISIBLE);
				mPm.putBoolean(this, PreferenceManager.KEY_HAS_SHOWN_CLOUD_BACKUP_TAB, true);
				break;
			case R.id.app_tools_tab :
				mSchedulePlanButton.setVisibility(View.GONE);
				mLoginButton.setVisibility(View.GONE);
				mPm.putBoolean(this, PreferenceManager.KEY_HAS_SHOWN_APP_TOOLS_TAB, true);
				if (!mPm.getBoolean(this, PreferenceManager.KEY_HAS_SHOWN_FREEZE_APP_TUTORIAL,
						false)) {
					mHandler.postDelayed(mFreezeAppTutorial, 100);
				}
				break;
			default :
				break;
		}
		focusViewTab(mCurTabId);
	}

	@Override
	public void postInvalidate() {

	}

	@Override
	public void scrollBy(int x, int y) {

	}

	@Override
	public int getScrollX() {
		return 0;
	}

	@Override
	public int getScrollY() {
		return 0;
	}

	@Override
	public void onClick(View v) {
		mCurTabId = v.getId();
		if (mScrollerViewGroup != null) {
			mScrollerViewGroup.gotoViewByIndex(getTabIndex(mCurTabId));
		}
		focusViewTab(mCurTabId);
	}

	private int getTabIndex(int tabId) {
		for (int i = 0; i < mTabIds.length; i++) {
			if (tabId == mTabIds[i]) {
				return i;
			}
		}
		return -1;
	}

	private boolean hasShowPayHelpPage() {
		return mPm.getBoolean(this, PreferenceManager.KEY_HAS_SHOWN_PAY_HELP_PAGE, false);
	}

	private boolean isCloudServiceValid() {
		//		FileHostingServiceProvider service = CloudServiceManager.getInstance().getCurrentService();
		//		return service != null && service.isSessionValid();
		return mService != null && mService.isSessionValid();
	}

	private int getCurrentAccountIndex() {
		int currentServiceType = mService != null && mService.isSessionValid()
				? mService.getType()
				: -1;
		if (currentServiceType > 0) {
			int[] serviceTypes = getResources().getIntArray(R.array.cloud_service_provider_code);
			for (int i = 0; i < serviceTypes.length; i++) {
				if (serviceTypes[i] == currentServiceType) {
					return i;
				}
			}
		}
		return -1;
	}

	private void showSwitchAccountDialog() {
		final int checkedPosition = getCurrentAccountIndex();
		final int[] serviceTypes = getResources().getIntArray(R.array.cloud_service_provider_code);
		new AlertDialog.Builder(this)
				.setTitle(R.string.title_select_cloud_service_provider)
				.setSingleChoiceItems(new AccountAdapter(this, mService), checkedPosition,
						new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, final int which) {
								if (checkedPosition >= 0 && checkedPosition == which) {
									dialog.dismiss();
									return;
								}
								// 判断是否有未完成任务
								if (!isLastOnlineTaskFinished()
										&& getRedoUnfinishedTaskType() != null) {
									// 未完成
									TaskType taskType = getRedoUnfinishedTaskType();
									if (taskType == TaskType.ONLINE_BACKUP) {
										mPendingIntent = new Intent(MainActivity.this,
												NewOnlineBackupActivity.class);
									} else {
										mPendingIntent = new Intent(MainActivity.this,
												RestoreOnlineBackupActivity.class);
									}

									mFutureRunnable = new Runnable() {
										@Override
										public void run() {
											mService = CloudServiceManager.getInstance()
													.switchService(MainActivity.this,
															serviceTypes[which]);
											if (mService != null && !mService.isSessionValid()) {
												mService.startAuthentication(MainActivity.this);
												mStartAuthentication = true;
											}
											updateViews();
										}
									};

									showRedoCloudTaskAlertDialog();
									dialog.dismiss();
									return;
								}

								mService = CloudServiceManager.getInstance().switchService(
										MainActivity.this, serviceTypes[which]);
								if (mService != null && !mService.isSessionValid()) {
									mService.startAuthentication(MainActivity.this);
									mStartAuthentication = true;
								}
								updateViews();
								dialog.dismiss();
							}
						}).show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_SHOW_TUTORIAL) {
			mPm.putBoolean(this, PreferenceManager.KEY_HAS_SHOWN_FREEZE_APP_TUTORIAL, true);
			if (data != null && data.getBooleanExtra(TutorialActivity.KEY_SPOTLIGHTE_CLICK, false)) {
				startFreezeActivity();
			}
		}
	}
	
	private Runnable mFreezeAppTutorial = new Runnable() {
		@Override
		public void run() {
			if (mFreezeAppButton != null) {
				final ImageView destView = (ImageView) mFreezeAppButton
						.findViewById(R.id.drawable);
				int[] point = new int[2];
				destView.getLocationOnScreen(point);
				final Rect rect = new Rect(point[0], point[1], point[0]
						+ destView.getWidth(), point[1] + destView.getHeight());
				Intent intent = new Intent(MainActivity.this, TutorialActivity.class);
				intent.putExtra(TutorialActivity.KEY_LAYOUT_ID,
						R.layout.tutorial_freeze_app);
				intent.putExtra(TutorialActivity.KEY_SPOTLIGHT_RECT, rect);
				intent.putExtra(TutorialActivity.KEY_SPOTLIGHT_DRAWABLE_ID,
						R.drawable.tutorial_circle_big);
				startActivityForResult(intent, REQUEST_SHOW_TUTORIAL);
			}
		}
	};

	/**
	 * @author maiyongshen
	 *
	 */
	public static class AccountAdapter extends BaseAdapter {
		private static final int INVALID_ID = -1;
		private static final int INVALID_POSITION = 0;
		private static final int VIEW_TYPE_TITLE = 1;
		private static final int VIEW_TYPE_NORMAL_VIEW = 2;

		private Context mContext;
		private FileHostingServiceProvider mSelectedService;
		private LayoutInflater mInflater;
		private CharSequence[] mCloudServiceProviders;
		private int[] mProviderCodes;
		private CloudServiceManager mCloudServiceManager;
		private Map<Integer, Integer> mIcons = new HashMap<Integer, Integer>();
		private Map<Integer, Integer> mSpinnerIcons = new HashMap<Integer, Integer>();

		public AccountAdapter(Context context, FileHostingServiceProvider service) {
			mContext = context.getApplicationContext();
			mSelectedService = service;
			mInflater = LayoutInflater.from(context);
			mCloudServiceManager = CloudServiceManager.getInstance();
			mCloudServiceProviders = context.getResources().getTextArray(
					R.array.cloud_service_provider);
			mProviderCodes = context.getResources()
					.getIntArray(R.array.cloud_service_provider_code);

			mIcons.put(FileHostingServiceProvider.DROPBOX, R.drawable.dropbox_logo);
			mIcons.put(FileHostingServiceProvider.GOOGLE_DRIVE, R.drawable.google_drive_logo);

			mSpinnerIcons.put(INVALID_ID, R.drawable.go_account_usercenter_btn);
			mSpinnerIcons.put(FileHostingServiceProvider.DROPBOX, R.drawable.account_dropbox);
			mSpinnerIcons.put(FileHostingServiceProvider.GOOGLE_DRIVE,
					R.drawable.account_google_drive);
		}

		@Override
		public int getCount() {
			return mProviderCodes != null ? mProviderCodes.length : 0;
		}

		@Override
		public Object getItem(int position) {
			return CloudServiceManager.getService(mContext, mProviderCodes[position]);
		}

		@Override
		public long getItemId(int position) {
			if (mProviderCodes != null) {
				return mProviderCodes[position];
			}
			return INVALID_ID;
		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.cloud_service_account_info, parent, false);
			}

			ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
			CheckedTextView text = (CheckedTextView) convertView.findViewById(R.id.text);

			if (icon != null) {
				icon.setVisibility(View.VISIBLE);
				Integer resId = mIcons.get((int) getItemId(position));
				if (resId != null) {
					icon.setImageResource(resId);
				}
			}

			FileHostingServiceProvider currentService = (FileHostingServiceProvider) getItem(position);
			if (text != null) {
				if (currentService != null) {
					AccountInfo account = currentService.getAccount(null);
					if (account != null) {
						text.setText(account.getDisplayName());
					} else {
						text.setText(mContext.getString(R.string.sign_in));
					}
				}
				if (mSelectedService != null && mSelectedService.isSessionValid()
						&& mSelectedService.getType() == mProviderCodes[position]) {
					text.setChecked(true);
				} else {
					text.setChecked(false);
				}
			}

			return convertView;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return getDropDownView(position, convertView, parent);
		}
	}
}