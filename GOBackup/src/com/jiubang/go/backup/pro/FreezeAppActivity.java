package com.jiubang.go.backup.pro;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.data.AppEntryComparator.SORT_TYPE;
import com.jiubang.go.backup.pro.data.AppInfo;
import com.jiubang.go.backup.pro.data.AppInfoNameComparator;
import com.jiubang.go.backup.pro.model.BackupManager;
import com.jiubang.go.backup.pro.model.DrawableProvider;
import com.jiubang.go.backup.pro.model.DrawableProvider.DrawableKey;
import com.jiubang.go.backup.pro.model.DrawableProvider.OnDrawableLoadedListener;
import com.jiubang.go.backup.pro.product.manage.ProductManager;
import com.jiubang.go.backup.pro.statistics.StatisticsKey;
import com.jiubang.go.backup.pro.ui.AppDetailsListAdapter;
import com.jiubang.go.backup.pro.ui.AppDetailsListAdapter.OnAdapterItemUpdateListener;
import com.jiubang.go.backup.pro.ui.AppDetailsListAdapter.OnSelectChangeListener;
import com.jiubang.go.backup.pro.ui.PayUpdateHelpActivity;
import com.jiubang.go.backup.pro.util.AppFreezer;
import com.jiubang.go.backup.pro.util.Util;

/**
 * @author maiyongshen
 *
 */
public class FreezeAppActivity extends BaseActivity {
	public static final String EXTRA_ACTION = "extra_action";
	public static final int ACTION_FREEZE_APPS = 0x1001;
	public static final int ACTION_UNFREEZE_APPS = 0x1002;
	
	private static final String KEY_SORT_TYPE = "app_sort_type";
	private static final int SORT_BY_APP_NAME = 0;
	private static final int SORT_BY_INSTALL_TIME = 1;
	
	private int mAction;
	private ListView mListView;
	private AppListAdapter mAdapter;
	private ImageButton mSortAppButton;
	private Button mActionButton;
	private SORT_TYPE mCurrentSortType;
	private SortAppTask mSortAppTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initViews();
		init();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mCurrentSortType != null) {
			outState.putInt(KEY_SORT_TYPE, mCurrentSortType.ordinal());
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		int sortType = savedInstanceState.getInt(KEY_SORT_TYPE, -1);
		if (sortType != -1) {
			mCurrentSortType = SORT_TYPE.values()[sortType];
		}
	}
	
	private void initViews() {
		setContentView(R.layout.app_list_activity);
		
		View returnButton = findViewById(R.id.return_btn);
		if (returnButton != null) {
			returnButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					finish();
				}
			});
		}
		
		mSortAppButton = (ImageButton) findViewById(R.id.sort_btn);
		mSortAppButton.setVisibility(View.VISIBLE);
		mSortAppButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showAppSorterDialog();
			}
		});
		
		mActionButton = (Button) findViewById(R.id.operation_btn);
		mActionButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!isPaidUser()) {
					startPayHelpActivity();
					return;
				}
				
				if (mAction == ACTION_FREEZE_APPS) {
					freezeApps();
				} else if (mAction == ACTION_UNFREEZE_APPS) {
					unfreezeApps();
				}
			}
		});
		
		mListView = (ListView) findViewById(R.id.listview);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final AppDetailsListAdapter adapter = (AppDetailsListAdapter) parent.getAdapter();
				if (adapter != null) {
					adapter.toggle(position);
					adapter.notifyListenerToUpdateView(position);
				}
			}
		});
		
	}
	
	private void init() {
		mCurrentSortType = SORT_TYPE.SORT_BY_APP_NAME;
		mAction = getIntent() != null ? getIntent().getIntExtra(EXTRA_ACTION, ACTION_FREEZE_APPS) : ACTION_FREEZE_APPS;
		
		updateTitleBar();
		mAdapter = new AppListAdapter(this, mAction);
		mAdapter.setOnItemUpdateListener(new OnAdapterItemUpdateListener() {
			@Override
			public void onItemUpdate(BaseAdapter adapter, int pos) {
				int firstVisiblePos = mListView.getFirstVisiblePosition();
				int lastVisiblePos = mListView.getLastVisiblePosition();
				if (pos >= firstVisiblePos && pos <= lastVisiblePos) {
					View convertView = mListView.getChildAt(pos - firstVisiblePos);
					((AppDetailsListAdapter) adapter).updateView(convertView, pos);
				}
			}
		});
		mAdapter.setOnItemSelectChangeListener(new OnSelectChangeListener() {
			@Override
			public void onSelectChange(int pos, boolean selected) {
				updateActionButton();
			}
		});
		updateActionButton();
		refreshAppList();
		mListView.setAdapter(mAdapter);
	}
	
	private void updateTitleBar() {
		TextView title = (TextView) findViewById(R.id.title);
		if (mAction == ACTION_FREEZE_APPS) {
			title.setText(R.string.title_freeze_app);
		} else if (mAction == ACTION_UNFREEZE_APPS) {
			title.setText(R.string.title_unfreeze_app);
		}
	}
	
	private void refreshAppList() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				new RefreshAppTask(FreezeAppActivity.this).execute();
			}
		});
	}
	
	public void updateActionButton() {
		mActionButton.setEnabled(mAdapter != null && mAdapter.hasItemSelected());
		final int selectedItemCount = mAdapter != null ? mAdapter.getSelectedItemCount() : 0;
		if (mAction == ACTION_FREEZE_APPS) {
			mActionButton.setText(selectedItemCount > 0 ? getString(R.string.btn_freeze_app,
					getString(R.string.parenthesized_msg, selectedItemCount)) : getString(
					R.string.btn_freeze_app, ""));
		} else if (mAction == ACTION_UNFREEZE_APPS) {
			mActionButton.setText(selectedItemCount > 0 ? getString(R.string.btn_unfreeze_app,
					getString(R.string.parenthesized_msg, selectedItemCount)) : getString(
					R.string.btn_unfreeze_app, ""));
		}
		
		final View tagView = findViewById(R.id.tag_view);
		if (tagView != null) {
			final boolean isPaidUser = isPaidUser();
			tagView.setVisibility(isPaidUser ? View.GONE : View.VISIBLE);
		}
	}
	
	private void updateSortButton(SORT_TYPE sortType) {
		if (mSortAppButton == null) {
			return;
		}
		
		int drawableId = -1;
		switch (sortType) {
			case SORT_BY_APP_NAME :
				drawableId = R.drawable.sort_by_name;
				break;
			case SORT_BY_APP_INSTALL_TIME :
				drawableId = R.drawable.sort_by_install_date;
				break;
			default :
				break;
		}
		if (drawableId > 0) {
			mSortAppButton.setImageResource(drawableId);
		}
	}
	
	private void sortApps(SORT_TYPE sortType) {
		if (mSortAppTask != null && !mSortAppTask.isFinished()) {
			return;
		}
		if (mAdapter == null || mAdapter.isEmpty()) {
			updateSortButton(mCurrentSortType);
			return;
		}
		mSortAppTask = new SortAppTask(this, mAdapter);
		mSortAppTask.execute(sortType);
		mCurrentSortType = sortType;
		updateSortButton(mCurrentSortType);
	}
	
	private void freezeApps() {
		new AppFreezeTask(FreezeAppActivity.this, ACTION_FREEZE_APPS).execute(mAdapter
				.getSelectedApps());
	}
	
	private void unfreezeApps() {
		new AppFreezeTask(FreezeAppActivity.this, ACTION_UNFREEZE_APPS).execute(mAdapter
				.getSelectedApps());
	}
	
	private int getCurrentSortTypeIndex() {
		Log.d("GOBackup", "getCurrentSortTypeIndex()");
		switch (mCurrentSortType) {
			case SORT_BY_APP_NAME :
				return SORT_BY_APP_NAME;
			case SORT_BY_APP_INSTALL_TIME :
				return SORT_BY_INSTALL_TIME;
			default :
				return -1;
		}
	}
	
	private void showAppSorterDialog() {
		new AlertDialog.Builder(this)
				.setTitle(R.string.title_sort_apps)
				.setSingleChoiceItems(R.array.freeze_app_sort_types, getCurrentSortTypeIndex(),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								switch (which) {
									case SORT_BY_APP_NAME :
										sortApps(SORT_TYPE.SORT_BY_APP_NAME);
										break;
									case SORT_BY_INSTALL_TIME :
										sortApps(SORT_TYPE.SORT_BY_APP_INSTALL_TIME);
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
				}).show();
	}
	
	private boolean isPaidUser() {
		return Util.isInland(this) || ProductManager.isPaid(this);
	}
	
	private void startPayHelpActivity() {
		Intent intent = new Intent(this, PayUpdateHelpActivity.class);
		intent.putExtra(PayUpdateHelpActivity.EXTRA_PURCHASE_REQUEST_SOURCE, StatisticsKey.PURCHASE_FROM_FREEZE_APP);
		startActivity(intent);
	}
	
	/**
	 * @author maiyongshen
	 *
	 */
	private class AppListAdapter extends AppDetailsListAdapter {
		private int mType;
		private List<AppItem> mAppList;
		
		public AppListAdapter(Context context, int type) {
			super(context);
			mType = type;
		}

		@Override
		public int getCount() {
			synchronized (this) {
				return !Util.isCollectionEmpty(mAppList) ? mAppList.size() : 0;
			}
		}

		@Override
		public Object getItem(int position) {
			if (position < 0 || position >= getCount()) {
				return null;
			}
			synchronized (this) {
				return mAppList.get(position);
			}
		}

		@Override
		public Drawable getIcon(int pos, OnDrawableLoadedListener listener) {
			final AppInfo appInfo = (AppInfo) getItem(pos);
			if (appInfo != null) {
				final DrawableKey key = DrawableProvider.buildDrawableKey(appInfo.packageName);
				final Drawable defaultDrawable = DrawableProvider.getDefaultActivityIcon(getContext());
				return DrawableProvider.getInstance().getDrawable(getContext(), key, defaultDrawable, listener);
			}
			return null;
		}

		@Override
		public void bindTitle(View view, int pos) {
			if (!(view instanceof TextView)) {
				return;
			}
			final AppInfo appInfo = (AppInfo) getItem(pos);
			if (appInfo != null) {
				((TextView) view).setText(appInfo.appName);
			}
		}

		@Override
		public void bindSummary(View view, int pos) {
			if (!(view instanceof TextView)) {
				return;
			}
			final AppInfo appInfo = (AppInfo) getItem(pos);
			if (appInfo != null) {
				((TextView) view).setText(appInfo.versionName);
			}
		}
		
		public void refresh() {
			refreshAppList(getContext());
		}
		
		private void refreshAppList(Context context) {
			List<String> packages = getPackages();
			synchronized (this) {
				if (!Util.isCollectionEmpty(mAppList)) {
					mAppList.clear();
				}
				if (mAppList == null) {
					mAppList = new ArrayList<AppItem>();
				}
				if (!Util.isCollectionEmpty(packages)) {
					final BackupManager bm = BackupManager.getInstance();
					final String goBackupPackageName = context.getPackageName();
					for (String packageName : packages) {
						if (TextUtils.equals(packageName, goBackupPackageName)) {
							continue;
						}
						AppInfo appInfo = bm.getAppInfo(context, packageName);
						if (appInfo == null) {
							continue;
						}
						if (appInfo.isSystemApp()) {
							continue;
						}
						appInfo.appName = bm.getApplicationName(context, packageName);
						mAppList.add(new AppItem(appInfo));
					}
				}
			}
		}
		
		private List<String> getPackages() {
			if (mType == ACTION_FREEZE_APPS) {
				return AppFreezer.getEnabledPackages(getContext());
			} else if (mType == ACTION_UNFREEZE_APPS) {
				return AppFreezer.getDisabledPackages(getContext());
			}
			return null;
		}
		
		public List<AppInfo> getSelectedApps() {
			final int count = getCount();
			List<AppInfo> selectedApps = new ArrayList<AppInfo>();
			for (int i = 0; i < count; i++) {
				if (isSelected(i)) {
					selectedApps.add((AppInfo) getItem(i));
				}
			}
			return !Util.isCollectionEmpty(selectedApps) ? selectedApps : null;
		}
		
		public List<AppItem> getAppList() {
			return mAppList;
		}

		@Override
		public SelectableItem getSelectableItem(int pos) {
			return (SelectableItem) getItem(pos);
		}
		
		/**
		 * @author maiyongshen
		 *
		 */
		private class AppItem extends AppInfo implements SelectableItem {
			boolean mIsSelected;
			
			public AppItem(AppInfo appInfo) {
				super(appInfo);
				mIsSelected = false;
			}

			@Override
			public boolean isSelected() {
				return mIsSelected;
			}

			@Override
			public void setSelected(boolean selected) {
				mIsSelected = selected;
			}
			
		}
	}
	
	/**
	 * @author maiyongshen
	 *
	 */
	private class AppFreezeTask extends AsyncTask<Object, Integer, Void> {
	
		private Activity mActivity;
		private List<AppInfo> mTaskObjects;
		private int mAction;
		private int mSuccessfulTaskCount;
		private AlertDialog mProgressDialog;
		private ProgressBar mProgressBar;
		private TextView mMessageView;
		private TextView mProgressPercent;
		private Handler mHandler;
		
		public AppFreezeTask(Activity activity, int action) {
			mActivity = activity;
			mAction = action;
			mSuccessfulTaskCount = 0;
			mHandler = new Handler();
		}

		@Override
		protected Void doInBackground(Object... params) {
			mTaskObjects = (List<AppInfo>) params[0];
			if (Util.isCollectionEmpty(mTaskObjects)) {
				return null;
			}
			final int taskCount = mTaskObjects.size();
			for (int i = 0; i < taskCount; i++) {
				if (isCancelled()) {
					break;
				}
				publishProgress(i);
				final String packageName = mTaskObjects.get(i).packageName;
				boolean result = mAction == ACTION_FREEZE_APPS ? AppFreezer.disablePackage(mActivity, packageName) :
					AppFreezer.enablePackage(mActivity, packageName);
				if (result) {
					mSuccessfulTaskCount++;
				}
			}
			onPostProcess();
			return null;
		}
		
		@Override
		protected void onPreExecute() {
			
		}
		
		private void createProgressDialog(int maxValue) {
			View view = mActivity.getLayoutInflater().inflate(R.layout.freeze_app_progress, null);
			mProgressDialog = new AlertDialog.Builder(mActivity)
					.setTitle(
							mAction == ACTION_FREEZE_APPS
									? R.string.freezing_apps
									: R.string.unfreezing_apps)
					.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							cancel(false);
						}
					}).setCancelable(false).setView(view).create();
			
//			view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//					ViewGroup.LayoutParams.WRAP_CONTENT));
			mProgressBar = (ProgressBar) view.findViewById(R.id.progress);
			mProgressBar.setMax(maxValue);
			mMessageView = (TextView) view.findViewById(R.id.title);
			mProgressPercent = (TextView) view.findViewById(R.id.progress_number);
//			mProgressDialog.setContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//					ViewGroup.LayoutParams.WRAP_CONTENT));
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			final int index = values[0];
			final int max = mTaskObjects.size();
			if (mProgressDialog == null) {
				createProgressDialog(max);
			}
			if (!mProgressDialog.isShowing()) {
				mProgressDialog.show();
				mProgressDialog.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			}
			if (!Util.isCollectionEmpty(mTaskObjects)) {
				AppInfo currentApp = mTaskObjects.get(index);
				mMessageView.setText(currentApp.appName);
				mProgressPercent.setText(String.format("%1d/%2d", index, max));
				mProgressBar.setProgress(index);
			}
		}
		
		@Override
		protected void onPostExecute(Void result) {
//			LogUtil.d("onPostExecute");
//			onPostProcess();
		}
		
		@Override
		protected void onCancelled() {
//			LogUtil.d("onCancelled count = " + mSuccessfulTaskCount + "   " + Thread.currentThread().getId());
		}
		
		private void onPostProcess() {
			showResultToast();
			dismissDialog();
			refreshAppList();
		}
		
		private void dismissDialog() {
			if (mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
		}
		
		private void showResultToast() {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(mActivity,
							mActivity.getString(mAction == ACTION_FREEZE_APPS
							? R.string.freeze_app_result
									: R.string.unfreeze_app_result, mSuccessfulTaskCount),
									Toast.LENGTH_LONG).show();
				}
			});
		}
	}
	
	/**
	 * @author maiyongshen
	 *
	 */
	private class SortAppTask extends AsyncTask<Object, Integer, Void> {
		private Activity mActivity;
		private AppListAdapter mAdapter;
		private ProgressDialog mDialog;
		private boolean mIsFinished = false;
		
		public SortAppTask(Activity activity, AppListAdapter adapter) {
			mActivity = activity;
			mAdapter = adapter;
		}

		@Override
		protected Void doInBackground(Object... params) {
			SORT_TYPE sortType = (SORT_TYPE) params[0];
			if (sortType == SORT_TYPE.SORT_BY_APP_NAME) {
				Collections.sort(mAdapter.getAppList(), new AppInfoNameComparator());
			} else if (sortType == SORT_TYPE.SORT_BY_APP_INSTALL_TIME) {
				Collections.sort(mAdapter.getAppList(), new AppInfo.InstallTimeComparator());
			}
			return null;
		}
		
		@Override
		protected void onPreExecute() {
			mDialog = BaseActivity.createSpinnerProgressDialog(mActivity, false);
			mDialog.setMessage(getString(R.string.msg_sorting));
			mDialog.show();
		}
		
		@Override
		protected void onPostExecute(Void result) {
			mAdapter.notifyDataSetChanged();
			mDialog.dismiss();
			mIsFinished = true;
		}
		
		public boolean isFinished() {
			return mIsFinished;
		}
	}
	
	private class RefreshAppTask extends AsyncTask<Integer, Integer, Void> {
		private Activity mActivity;
		private ProgressDialog mDialog;
		
		public RefreshAppTask(Activity activity) {
			mActivity = activity;
		}

		@Override
		protected Void doInBackground(Integer... params) {
			mAdapter.refresh();
			return null;
		}
		
		@Override
		protected void onPreExecute() {
			mDialog = createSpinnerProgressDialog(true);
			mDialog.setMessage(getString(R.string.msg_refresh_app_list));
			mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					cancel(false);
				}
			});
			showDialog(mDialog);
		}
		
		@Override
		protected void onPostExecute(Void result) {
			mAdapter.notifyDataSetChanged();
			dismissDialog(mDialog);
			if (mAdapter == null || mAdapter.isEmpty()) {
				mSortAppButton.setVisibility(View.GONE);
				finish();
			} else {
				mSortAppButton.setVisibility(View.VISIBLE);
				sortApps(mCurrentSortType);
			}
			updateActionButton();
		}
		
		@Override
		protected void onCancelled() {
			finish();
		}
	}
}
