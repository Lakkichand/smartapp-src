package com.jiubang.go.backup.pro.ui;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.PreferenceManager;
import com.jiubang.go.backup.pro.data.AppEntry;
import com.jiubang.go.backup.pro.data.AppEntryComparator;
import com.jiubang.go.backup.pro.data.AppEntryComparator.SORT_TYPE;
import com.jiubang.go.backup.pro.data.AppInfo;
import com.jiubang.go.backup.pro.data.AppInfoNameComparator;
import com.jiubang.go.backup.pro.data.AppRestoreEntry;
import com.jiubang.go.backup.pro.data.BaseEntry;
import com.jiubang.go.backup.pro.data.BaseEntry.EntryType;
import com.jiubang.go.backup.pro.data.BaseRestoreEntry;
import com.jiubang.go.backup.pro.data.BookMarkBackupEntry;
import com.jiubang.go.backup.pro.data.BookMarkRestoreEntry;
import com.jiubang.go.backup.pro.data.CalendarBackupEntry;
import com.jiubang.go.backup.pro.data.CalendarRestoreEntry;
import com.jiubang.go.backup.pro.data.CallLogBackupEntry;
import com.jiubang.go.backup.pro.data.CallLogRestoreEntry;
import com.jiubang.go.backup.pro.data.ContactsRestoreEntry;
import com.jiubang.go.backup.pro.data.IRecord;
import com.jiubang.go.backup.pro.data.MmsRestoreEntry;
import com.jiubang.go.backup.pro.data.RestorableRecord;
import com.jiubang.go.backup.pro.data.SmsBackupEntry;
import com.jiubang.go.backup.pro.data.SmsRestoreEntry;
import com.jiubang.go.backup.pro.image.util.FolderRestoreEntry;
import com.jiubang.go.backup.pro.lib.contacts.ContactsOperator;
import com.jiubang.go.backup.pro.mms.MmsBackup;
import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.model.BackupDBHelper.BaseBackupEntryInfo;
import com.jiubang.go.backup.pro.model.BackupManager;
import com.jiubang.go.backup.pro.model.DrawableProvider;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.model.RootShell;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 网络恢复记录adapter
 *
 * @author GoBackup Dev Team
 */
public class NetRestoreRecordAdapter extends RecordDetailListAdpater {
	private static final String BACKUP_DATE_FORMAT = "(%tY/%tm/%td %tH:%tM)";

	private RestorableRecord mRecord;
	private BackupDBHelper mOnlineBackupDBHelper;
//	private PackageManager mPackageManager;
	private DrawableProvider mDrawableProvider;
	
	private boolean mBeAbleToRestoreAppsData;

	private int mLocalContactCount;
	private int mLocalSMSCount;
	private int mLocalMMSCount;
	private int mLocalCallLogCount;
	private int mLocalCalendarCount;
	private int mLocalBookmarkCount;

	private int mOnlineContactCount;
	private int mOnlineSMSCount;
	private int mOnlineMMSCount;
	private int mOnlineCallLogCount;
	private int mOnlineCalendarCount;
	private int mOnlineBookmarkCount;

	private List<BaseBackupEntryInfo> mOnlineSystemBackupEntrisInfo = null;

	// private List<BaseBackupEntryInfo> mOnlineAppBackupEntriesInfo = null;

	public NetRestoreRecordAdapter(Context context, IRecord record) {
		super(context, record);
		mRecord = (RestorableRecord) record;
//		mPackageManager = getContext().getPackageManager();
		mDrawableProvider = DrawableProvider.getInstance();
		mBeAbleToRestoreAppsData = isRootValid()
				&& PreferenceManager.getInstance().getBoolean(getContext(),
						PreferenceManager.KEY_SHOULD_BACKUP_RESTORE_APP_DATA, true);
	}

	public void init() {
		mOnlineBackupDBHelper = mRecord.getBackupDBHelper(getContext());
		mOnlineSystemBackupEntrisInfo = mOnlineBackupDBHelper.getAllSystemDataEntriesInfo();
		mLocalContactCount = ContactsOperator.getSystemContactsCount(getContext());
		mLocalMMSCount = MmsBackup.getMmsCount(getContext());
		mLocalSMSCount = SmsBackupEntry.queryLocalSmsCount(getContext());
		mLocalCallLogCount = CallLogBackupEntry.queryLocalCallLogCount(getContext());
		mLocalCalendarCount = CalendarBackupEntry.getLocalCalendarEventCount(getContext());
		mLocalBookmarkCount = BookMarkBackupEntry.getLocalBookMarkCount(getContext());

		mOnlineContactCount = getOnlineSystemBackupEntryCount(EntryType.TYPE_USER_CONTACTS);
		mOnlineSMSCount = getOnlineSystemBackupEntryCount(EntryType.TYPE_USER_SMS);
		mOnlineMMSCount = getOnlineSystemBackupEntryCount(EntryType.TYPE_USER_MMS);
		mOnlineCallLogCount = getOnlineSystemBackupEntryCount(EntryType.TYPE_USER_CALL_HISTORY);
		mOnlineCalendarCount = getOnlineSystemBackupEntryCount(EntryType.TYPE_USER_CALENDAR);
		mOnlineBookmarkCount = getOnlineSystemBackupEntryCount(EntryType.TYPE_USER_BOOKMARK);
	}

	@Override
	public void release() {
		super.release();
		if (mOnlineSystemBackupEntrisInfo != null) {
			mOnlineSystemBackupEntrisInfo.clear();
		}
		if (mOnlineBackupDBHelper != null) {
			mOnlineBackupDBHelper.close();
		}
	}

	private BaseBackupEntryInfo getOnlineSystemBackupEntryInfo(EntryType type) {
		if (Util.isCollectionEmpty(mOnlineSystemBackupEntrisInfo)) {
			return null;
		}
		for (BaseBackupEntryInfo entryInfo : mOnlineSystemBackupEntrisInfo) {
			if (entryInfo.type == type) {
				return entryInfo;
			}
		}
		return null;
	}

	private Date getOnlineSystemBackupEntryDate(EntryType type) {
		BaseBackupEntryInfo entryInfo = getOnlineSystemBackupEntryInfo(type);
		if (entryInfo != null) {
			return entryInfo.backupDate;
		}
		return null;
	}

	private int getOnlineSystemBackupEntryCount(EntryType type) {
		BaseBackupEntryInfo entryInfo = getOnlineSystemBackupEntryInfo(type);
		if (entryInfo != null) {
			return entryInfo.count;
		}
		return 0;
	}

	@Override
	protected void bindTitleExtraInfo(View view, BaseEntry entry) {
		TextView info = (TextView) view;
		info.setVisibility(View.VISIBLE);
		// 应用程序展示备份状态
		if (entry instanceof AppRestoreEntry) {
			AppInfo onlineAppInfo = ((AppRestoreEntry) entry).getAppInfo();
			AppInfo localAppInfo = getLocalAppInfo(onlineAppInfo.packageName);
			String state = null;
			if (localAppInfo != null) {
				if (onlineAppInfo.versionCode > localAppInfo.versionCode) {
					state = getContext().getString(R.string.app_updatable);
				} else if (onlineAppInfo.versionCode <= localAppInfo.versionCode) {
					state = getContext().getString(R.string.app_installed);
				}
			} else {
				state = getContext().getString(R.string.app_uninstalled);
			}
			info.setText(getContext().getString(R.string.parenthesized_msg, state));
			return;
		}
		if (entry instanceof FolderRestoreEntry) {
			int totalCout = ((FolderRestoreEntry) entry).getCount();
			int selectCount = ((FolderRestoreEntry) entry).getSelectedCount();
			info.setVisibility(View.VISIBLE);
			String imageSelectedInfo = getContext().getString(R.string.progress_detail,
					selectCount, totalCout);
			info.setText(getContext().getString(R.string.parenthesized_msg, imageSelectedInfo));
			return;
		}
		

		Date lastBackupDate = getOnlineSystemBackupEntryDate(entry.getType());
		if (lastBackupDate != null) {
			info.setText(String.format(BACKUP_DATE_FORMAT, lastBackupDate, lastBackupDate,
					lastBackupDate, lastBackupDate, lastBackupDate));
			return;
		}

		info.setVisibility(View.GONE);
	}

	private AppInfo getLocalAppInfo(String packageName) {
		if (TextUtils.isEmpty(packageName)) {
			return null;
		}
//		try {
//			PackageInfo pi = mPackageManager.getPackageInfo(packageName, 0);
//			return new AppInfo(pi, mPackageManager);
//		} catch (NameNotFoundException e) {
//			e.printStackTrace();
//			return null;
//		}
		return BackupManager.getInstance().getAppInfo(getContext(), packageName);
	}

	@Override
	protected void bindSummaryInfo(View view, BaseEntry entry) {
		TextView summary = (TextView) view;
		
		// 备份项不支持恢复
		if (!((BaseRestoreEntry) entry).isRestorable()) {
			summary.setVisibility(View.VISIBLE);
			summary.setText(R.string.not_restorable);
			return;
		}
		
		if (entry instanceof AppRestoreEntry) {
			summary.setVisibility(View.VISIBLE);
			if (!mBeAbleToRestoreAppsData) {
				summary.setText(R.string.entry_state_only_app);
			} else {
				summary.setText(R.string.entry_state_app_and_data);
			}
			return;
		}

		summary.setVisibility(View.VISIBLE);
		if (entry instanceof ContactsRestoreEntry) {
			String localCountMsg = getContext().getString(R.string.local_count, mLocalContactCount);
			summary.setText(localCountMsg);
			return;
		}

		if (entry instanceof SmsRestoreEntry) {
			String localCountMsg = getContext().getString(R.string.local_count, mLocalSMSCount);
			summary.setText(localCountMsg);
			return;
		}

		if (entry instanceof CallLogRestoreEntry) {
			String localCountMsg = getContext().getString(R.string.local_count, mLocalCallLogCount);
			summary.setText(localCountMsg);
			return;
		}

		if (entry instanceof MmsRestoreEntry) {
			String localCountMsg = getContext().getString(R.string.local_count, mLocalMMSCount);
			summary.setText(localCountMsg);
			return;
		}

		if (entry instanceof CalendarRestoreEntry) {
			String localCountMsg = getContext().getString(R.string.local_count, mLocalCalendarCount);
			summary.setText(localCountMsg);
			return;
		}

		if (entry instanceof BookMarkRestoreEntry) {
			String localCountMsg = getContext().getString(R.string.local_count, mLocalBookmarkCount);
			summary.setText(localCountMsg);
			return;
		}

		summary.setVisibility(View.GONE);
	}

	@Override
	protected void bindExtraSummaryInfo(View view, BaseEntry entry) {
		if (entry instanceof AppRestoreEntry) {
			TextView size = (TextView) view;
			size.setVisibility(View.VISIBLE);
			size.setText(Util.formatFileSize(entry.getSpaceUsage()));
			return;
		}

		TextView count = (TextView) view;
		count.setVisibility(View.VISIBLE);
		if (entry instanceof ContactsRestoreEntry) {
			String onlineCountMsg = getContext().getString(R.string.online_count,
					mOnlineContactCount);
			count.setText(onlineCountMsg);
			return;
		}

		if (entry instanceof SmsRestoreEntry) {
			String onlineCountMsg = getContext().getString(R.string.online_count, mOnlineSMSCount);
			count.setText(onlineCountMsg);
			return;
		}

		if (entry instanceof CallLogRestoreEntry) {
			String onlineCountMsg = getContext().getString(R.string.online_count,
					mOnlineCallLogCount);
			count.setText(onlineCountMsg);
			return;
		}

		if (entry instanceof MmsRestoreEntry) {
			String onlineCountMsg = getContext().getString(R.string.online_count, mOnlineMMSCount);
			count.setText(onlineCountMsg);
			return;
		}

		if (entry instanceof CalendarRestoreEntry) {
			String onlineCountMsg = getContext().getString(R.string.online_count, mOnlineCalendarCount);
			count.setText(onlineCountMsg);
			return;
		}

		if (entry instanceof BookMarkRestoreEntry) {
			String onlineCountBookMark = getContext().getString(R.string.online_count, mOnlineBookmarkCount);
			count.setText(onlineCountBookMark);
			return;
		}
		view.setVisibility(View.GONE);
	}

	@Override
	protected void bindMarkerView(View view, BaseEntry entry) {
		view.setVisibility(View.GONE);
	}
	
	@Override
	protected void bindCheckbox(CheckBox checkBox, BaseEntry entry, int groupPos, int childPos) {
		super.bindCheckbox(checkBox, entry, groupPos, childPos);
		if (entry instanceof BaseRestoreEntry) {
			if (!((BaseRestoreEntry) entry).isRestorable()) {
				checkBox.setVisibility(View.GONE);
			} else {
				checkBox.setVisibility(View.VISIBLE);
			}
		}
	}

	private boolean isRootValid() {
		return RootShell.isRootValid();
	}

	public void selectUserAppEntry(RestoreAppState state, boolean selected) {
		if (state == null) {
			return;
		}
		selectAppEntry(IRecord.GROUP_USER_APP, state, selected);
	}

	public void selectSystemAppEntry(RestoreAppState state, boolean selected) {
		if (state == null) {
			return;
		}
		selectAppEntry(IRecord.GROUP_SYSTEM_APP, state, selected);
	}

	private void selectAppEntry(String key, RestoreAppState state, boolean selected) {
		if (state == null || key == null) {
			return;
		}
		int appGoupPos = getGroupPositionByKey(key);
		if (appGoupPos < 0) {
			return;
		}
		final List<BaseEntry> appEntries = (List<BaseEntry>) getGroup(appGoupPos);
		if (Util.isCollectionEmpty(appEntries)) {
			return;
		}

		for (BaseEntry entry : appEntries) {
			final AppRestoreEntry appEntry = (AppRestoreEntry) entry;
			final AppInfo onlineAppInfo = appEntry.getAppInfo();
			final AppInfo localAppInfo = getLocalAppInfo(onlineAppInfo.packageName);
			if (match(state, localAppInfo, onlineAppInfo)) {
				entry.setSelected(selected);
			}
		}
	}

	private boolean match(RestoreAppState state, AppInfo localAppInfo, AppInfo onlineAppInfo) {
		if (state == null) {
			return false;
		}
		if (state == RestoreAppState.UNINSTALLED && localAppInfo == null) {
			return true;
		}
		if (state == RestoreAppState.INSTALLED && localAppInfo != null
				&& onlineAppInfo.versionCode <= localAppInfo.versionCode) {
			return true;
		}
		if (state == RestoreAppState.UPDATABLE && localAppInfo != null
				&& onlineAppInfo.versionCode > localAppInfo.versionCode) {
			return true;
		}
		return false;
	}

	/**
	 * 枚举，恢复app状态
	 *
	 * @author GoBackup Dev Team
	 */
	public enum RestoreAppState {
		INSTALLED, UNINSTALLED, UPDATABLE
	}

	@Override
	protected boolean isEntrySelectable(BaseEntry entry) {
		if (!(entry instanceof BaseRestoreEntry)) {
			return false;
		}
		if (!isRootValid() && entry.isNeedRootAuthority()) {
			return false;
		}
		if (!((BaseRestoreEntry) entry).isRestorable()) {
			return false;
		}
		return true;
	}

	@Override
	public void sortAppEntries(SORT_TYPE sortType, IAsyncTaskListener listener) {
		new Thread(new OnlineAppEntrySortor(sortType, listener)).start();
	}

	/**
	 * 在线app排序
	 *
	 * @author GoBackup Dev Team
	 */
	private class OnlineAppEntrySortor implements Runnable {
		private SORT_TYPE mSortType;
		private IAsyncTaskListener mListener;

		public OnlineAppEntrySortor(SORT_TYPE sortType, IAsyncTaskListener listener) {
			if (sortType == null) {
				throw new IllegalArgumentException("invalid sort type!");
			}
			mSortType = sortType;
			mListener = listener;
		}

		@Override
		public void run() {
			if (mListener != null) {
				mListener.onStart(null, null);
			}
			// TODO
			sort();
			if (mListener != null) {
				mListener.onEnd(true, null, null);
			}
		}

		private void sort() {
			List<BaseRestoreEntry> userEntries = (List<BaseRestoreEntry>) (getRecord())
					.getGroup(IRecord.GROUP_USER_APP);
			List<BaseRestoreEntry> systemAppEntries = (List<BaseRestoreEntry>) (getRecord())
					.getGroup(IRecord.GROUP_SYSTEM_APP);
			if (Util.isCollectionEmpty(userEntries) && Util.isCollectionEmpty(systemAppEntries)) {
				return;
			}

			if (mSortType == SORT_TYPE.SORT_ONLINE_BY_APP_NAME) {
				OnlineAppEntryNameComparator onlineNameComparator = new OnlineAppEntryNameComparator();
				if (userEntries != null) {
					Collections.sort(userEntries, onlineNameComparator);
				}
				if (systemAppEntries != null) {
					Collections.sort(systemAppEntries, onlineNameComparator);
				}
				return;
			}
			if (mSortType == SORT_TYPE.SORT_ONLINE_BY_APP_SIZE) {
				OnlineAppEntrySizeComparator onlineSizeComparator = new OnlineAppEntrySizeComparator();
				if (userEntries != null) {
					Collections.sort(userEntries, onlineSizeComparator);
				}
				if (systemAppEntries != null) {
					Collections.sort(systemAppEntries, onlineSizeComparator);
				}
				return;
			}
		}
	}

	/**
	 * 网络备份app恢复状态比较
	 *
	 * @author GoBackup Dev Team
	 */
	private class OnlineAppEntryRestoreStateComparator extends AppEntryComparator<BaseEntry> {

		@Override
		public int compare(BaseEntry lhs, BaseEntry rhs) {
			return getRestoreStateComparatorFactor(lhs) - getRestoreStateComparatorFactor(rhs);
		}

		private int getRestoreStateComparatorFactor(BaseEntry entry) {
			final int m3 = 3;
			if (entry == null) {
				return 0;
			}
			if (!(entry instanceof AppRestoreEntry)) {
				return 0;
			}
			OnlineRestoreState state = getOnlineRestoreState((AppRestoreEntry) entry);
			if (state == OnlineRestoreState.UNINSTALLED) {
				return 1;
			}
			if (state == OnlineRestoreState.UPDATABLE) {
				return 2;
			}
			if (state == OnlineRestoreState.INSTALLED) {
				return m3;
			}
			return 0;
		}
	}

	/**
	 * 网络恢复app名称比较
	 *
	 * @author GoBackup Dev Team
	 */
	private class OnlineAppEntryNameComparator extends AppEntryComparator<BaseEntry> {

		private OnlineAppEntryRestoreStateComparator mOnlineAppEntryBackupStateComparator = new OnlineAppEntryRestoreStateComparator();

		@Override
		public int compare(BaseEntry lhs, BaseEntry rhs) {
			int ret = mOnlineAppEntryBackupStateComparator.compare(lhs, rhs);
			if (ret != 0) {
				return ret;
			}

			AppInfo lhsAppInfo = ((AppEntry) lhs).getAppInfo();
			AppInfo rhsAppInfo = ((AppEntry) rhs).getAppInfo();
			return new AppInfoNameComparator().compare(lhsAppInfo, rhsAppInfo);
		}

	}

	/**
	 * 网络恢复，app大小比较
	 *
	 * @author GoBackup Dev Team
	 */
	private class OnlineAppEntrySizeComparator extends AppEntryComparator<BaseEntry> {
		private OnlineAppEntryRestoreStateComparator mOnlineAppEntryBackupStateComparator = new OnlineAppEntryRestoreStateComparator();

		@Override
		public int compare(BaseEntry lhs, BaseEntry rhs) {
			int ret = mOnlineAppEntryBackupStateComparator.compare(lhs, rhs);
			if (ret != 0) {
				return ret;
			}

			long size1 = lhs.getSpaceUsage();
			long size2 = rhs.getSpaceUsage();
			return size1 > size2 ? -1 : size1 < size2 ? 1 : 0;
		}

	}

	private OnlineRestoreState getOnlineRestoreState(AppRestoreEntry appEntry) {
		if (appEntry == null) {
			throw new IllegalArgumentException("appEntry can not be null");
		}

		AppInfo onlineAppInfo = appEntry.getAppInfo();
		AppInfo localAppInfo = getLocalAppInfo(onlineAppInfo.packageName);
		if (localAppInfo == null) {
			return OnlineRestoreState.UNINSTALLED;
		}

		if (localAppInfo.versionCode < onlineAppInfo.versionCode) {
			return OnlineRestoreState.UPDATABLE;
		}

		return OnlineRestoreState.INSTALLED;
	}

	/**
	 *  网络恢复状态
	 *
	 * @author ReyZhang
	 */
	private enum OnlineRestoreState {
		UNINSTALLED, INSTALLED, UPDATABLE
	}
	
	@Override
	protected Drawable getEntryIcon(BaseEntry entry, int groupPos, int childPos) {
		if (entry instanceof AppRestoreEntry) {
			AppInfo appInfo = ((AppRestoreEntry) entry).getAppInfo();
			Drawable drawable = mDrawableProvider.getDrawableFromCaches(DrawableProvider.buildDrawableKey(appInfo.packageName));
			if (drawable == null) {
				byte[] iconData = mOnlineBackupDBHelper.getAppIconRawData(appInfo.packageName);
				if (iconData != null && iconData.length > 0) {
					return ((AppRestoreEntry) entry).getIcon(getContext(), iconData, new EntryIconLoadListener(groupPos, childPos));
				}
			}
		}
		return super.getEntryIcon(entry, groupPos, childPos);
	}
}
