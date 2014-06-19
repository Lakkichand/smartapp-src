package com.jiubang.go.backup.pro.ui;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.PreferenceManager;
import com.jiubang.go.backup.pro.data.AppBackupEntry;
import com.jiubang.go.backup.pro.data.AppEntry;
import com.jiubang.go.backup.pro.data.AppEntryComparator;
import com.jiubang.go.backup.pro.data.AppEntryComparator.SORT_TYPE;
import com.jiubang.go.backup.pro.data.AppInfo;
import com.jiubang.go.backup.pro.data.AppInfoNameComparator;
import com.jiubang.go.backup.pro.data.BaseBackupEntry;
import com.jiubang.go.backup.pro.data.BaseEntry;
import com.jiubang.go.backup.pro.data.BaseEntry.EntryType;
import com.jiubang.go.backup.pro.data.BookMarkBackupEntry;
import com.jiubang.go.backup.pro.data.CalendarBackupEntry;
import com.jiubang.go.backup.pro.data.CallLogBackupEntry;
import com.jiubang.go.backup.pro.data.ContactsBackupEntry;
import com.jiubang.go.backup.pro.data.IRecord;
import com.jiubang.go.backup.pro.data.MmsBackupEntry;
import com.jiubang.go.backup.pro.data.SmsBackupEntry;
import com.jiubang.go.backup.pro.image.util.FolderBackEntry;
import com.jiubang.go.backup.pro.lib.contacts.ContactsOperator;
import com.jiubang.go.backup.pro.mms.MmsBackup;
import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.model.BackupDBHelper.AppBackupEntryInfo;
import com.jiubang.go.backup.pro.model.BackupDBHelper.BaseBackupEntryInfo;
import com.jiubang.go.backup.pro.model.BackupManager;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.model.RootShell;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 网络备份记录adapter
 *
 * @author WenCan
 */
public class NetBackupRecordAdapter extends RecordDetailListAdpater {
	private static final EntryType[] DEFAULT_UNSELECTED_ENTRYTYPE_FILTER = {
			EntryType.TYPE_USER_DICTIONARY, EntryType.TYPE_USER_CALENDAR,
			EntryType.TYPE_USER_BOOKMARK };
	private static final String BACKUP_DATE_FORMAT = "(%tY/%tm/%td %tH:%tM)";
	private boolean mBeAbleToBackupAppsData;
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
	private Map<String, AppBackupEntryInfo> mAllOnlineAppInfoMap;
	private List<BaseBackupEntryInfo> mAllOnlineSystemBackupInfo;
	private BackupManager mBackupManager;

	public NetBackupRecordAdapter(Context context, IRecord record) {
		super(context, record);
		mBackupManager = BackupManager.getInstance();
		mBeAbleToBackupAppsData = isRootValid()
				&& PreferenceManager.getInstance().getBoolean(getContext(),
						PreferenceManager.KEY_SHOULD_BACKUP_RESTORE_APP_DATA, true);
	}
	
	public void init(BackupDBHelper cloudBackupDb) {
		initOnlineAppInfoMap(cloudBackupDb);
		initOnlineSystemBackupInfo(cloudBackupDb);

		mLocalContactCount = ContactsOperator.getSystemContactsCount(getContext());
		mLocalMMSCount = MmsBackup.getMmsCount(getContext());
		mLocalSMSCount = SmsBackupEntry.queryLocalSmsCount(getContext());
		mLocalCallLogCount = CallLogBackupEntry.queryLocalCallLogCount(getContext());
		mLocalCalendarCount = CalendarBackupEntry.getLocalCalendarEventCount(getContext());
		mLocalBookmarkCount = BookMarkBackupEntry.getLocalBookMarkCount(getContext());
		mOnlineContactCount = getOnlineBackupEntryCount(EntryType.TYPE_USER_CONTACTS);
		mOnlineSMSCount = getOnlineBackupEntryCount(EntryType.TYPE_USER_SMS);
		mOnlineMMSCount = getOnlineBackupEntryCount(EntryType.TYPE_USER_MMS);
		mOnlineCallLogCount = getOnlineBackupEntryCount(EntryType.TYPE_USER_CALL_HISTORY);
		mOnlineCalendarCount = getOnlineBackupEntryCount(EntryType.TYPE_USER_CALENDAR);
		mOnlineBookmarkCount = getOnlineBackupEntryCount(EntryType.TYPE_USER_BOOKMARK);
		
		List<BaseBackupEntry> appEntries = (List<BaseBackupEntry>) getRecord().getGroup(IRecord.GROUP_USER_APP);
		if (Util.isCollectionEmpty(appEntries)) {
			return;
		}
		for (BaseBackupEntry appEntry : appEntries) {
			if (appEntry instanceof AppEntry) {
				AppInfo appInfo = ((AppEntry) appEntry).getAppInfo();
				if (appInfo == null) {
					continue;
				}
				if (!appInfo.isApplicationNameValid()) {
					appInfo.appName = mBackupManager.getApplicationName(getContext(),
							appInfo.packageName);
				}
			}
		}
		sortAppEntries(SORT_TYPE.SORT_BY_APP_NAME, NetBackupRecordAdapter.this);
	}

	@Override
	public void release() {
		super.release();
		if (mAllOnlineAppInfoMap != null) {
			mAllOnlineAppInfoMap.clear();
		}
		if (mAllOnlineSystemBackupInfo != null) {
			mAllOnlineSystemBackupInfo.clear();
		}
	}

	private void initOnlineAppInfoMap(BackupDBHelper dbHelper) {
		if (dbHelper == null) {
			return;
		}
		List<BaseBackupEntryInfo> allAppEntryInfos = dbHelper.getAllAppEntriesInfo();
		if (Util.isCollectionEmpty(allAppEntryInfos)) {
			return;
		}

		if (mAllOnlineAppInfoMap == null) {
			mAllOnlineAppInfoMap = new HashMap<String, AppBackupEntryInfo>();
		}
		mAllOnlineAppInfoMap.clear();
		ListIterator<BaseBackupEntryInfo> listIterator = allAppEntryInfos.listIterator();
		if (listIterator == null) {
			return;
		}
		while (listIterator.hasNext()) {
			try {
				AppBackupEntryInfo appEntryInfo = (AppBackupEntryInfo) listIterator.next();
				mAllOnlineAppInfoMap.put(appEntryInfo.appInfo.packageName, appEntryInfo);
			} catch (Exception e) {
			}
		}
	}

	private void initOnlineSystemBackupInfo(BackupDBHelper dbHelper) {
		if (dbHelper == null) {
			return;
		}
		mAllOnlineSystemBackupInfo = dbHelper.getAllSystemDataEntriesInfo();
	}

	private int getOnlineBackupEntryCount(EntryType type) {
		if (type == null || mAllOnlineSystemBackupInfo == null) {
			return 0;
		}

		BaseBackupEntryInfo entryInfo = getSystemBackupEntryInfo(type);
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
		if (entry instanceof AppBackupEntry) {
			AppInfo localAppInfo = ((AppBackupEntry) entry).getAppInfo();
			AppInfo onlineAppInfo = null;
			AppBackupEntryInfo onlineEntryInfo = mAllOnlineAppInfoMap != null
					? ((AppBackupEntryInfo) mAllOnlineAppInfoMap.get(localAppInfo.packageName))
					: null;
			if (onlineEntryInfo != null) {
				onlineAppInfo = onlineEntryInfo.appInfo;
			}
			String appStateInfo = getAppState(localAppInfo, onlineAppInfo);
			info.setText(getContext().getString(R.string.parenthesized_msg, appStateInfo));
			return;
		}
		if (entry instanceof FolderBackEntry) {
			int totalCout = ((FolderBackEntry) entry).getCount();
			int selectCount = ((FolderBackEntry) entry).getSelectedCount();
			info.setVisibility(View.VISIBLE);
			String imageSelectedInfo = getContext().getString(R.string.progress_detail,
					selectCount, totalCout);
			info.setText(getContext().getString(R.string.parenthesized_msg, imageSelectedInfo));
			return;
		}
		// 其他项展示备份时间
		Date lastBackupDate = getEntryLastBackupDate(entry);
		if (lastBackupDate != null) {
			info.setText(String.format(BACKUP_DATE_FORMAT, lastBackupDate, lastBackupDate,
					lastBackupDate, lastBackupDate, lastBackupDate));
			return;
		}
		info.setVisibility(View.GONE);
	}

	private String getAppState(AppInfo localAppInfo, AppInfo onlineAppInfo) {
		String state = null;
		if (onlineAppInfo != null) {
			if (onlineAppInfo.versionCode < localAppInfo.versionCode) {
				state = getContext().getString(R.string.app_updatable);
			} else if (onlineAppInfo.versionCode >= localAppInfo.versionCode) {
				state = getContext().getString(R.string.app_backuped);
			}
		} else {
			state = getContext().getString(R.string.app_not_backuped);
		}
		return state;
	}

	@Override
	protected void bindSummaryInfo(View view, BaseEntry entry) {
		TextView summary = (TextView) view;
		if (entry instanceof AppBackupEntry) {
			summary.setVisibility(View.VISIBLE);
			if (!mBeAbleToBackupAppsData) {
				summary.setText(R.string.entry_state_only_app);
			} else {
				summary.setText(R.string.entry_state_app_and_data);
			}
			return;
		}
		if (entry instanceof ContactsBackupEntry) {
			summary.setVisibility(View.VISIBLE);
			summary.setText(getContext().getString(R.string.local_count, mLocalContactCount));
			return;
		}
		if (entry instanceof SmsBackupEntry) {
			summary.setVisibility(View.VISIBLE);
			summary.setText(getContext().getString(R.string.local_count, mLocalSMSCount));
			return;
		}
		if (entry instanceof MmsBackupEntry) {
			summary.setVisibility(View.VISIBLE);
			summary.setText(getContext().getString(R.string.local_count, mLocalMMSCount));
			return;
		}
		if (entry instanceof CallLogBackupEntry) {
			summary.setVisibility(View.VISIBLE);
			summary.setText(getContext().getString(R.string.local_count, mLocalCallLogCount));
			return;
		}
		if (entry instanceof CalendarBackupEntry) {
			summary.setVisibility(View.VISIBLE);
			summary.setText(getContext().getString(R.string.local_count, mLocalCalendarCount));
			return;
		}

		if (entry instanceof BookMarkBackupEntry) {
			summary.setVisibility(View.VISIBLE);
			summary.setText(getContext().getString(R.string.local_count, mLocalBookmarkCount));
			return;
		}
		summary.setVisibility(View.GONE);
	}

	@Override
	protected void bindExtraSummaryInfo(View view, BaseEntry entry) {
		if (entry instanceof AppBackupEntry) {
			TextView size = (TextView) view;
			size.setVisibility(View.VISIBLE);
			size.setText(Util.formatFileSize(entry.getSpaceUsage()));
			return;
		}
		TextView count = (TextView) view;
		if (entry instanceof ContactsBackupEntry) {
			count.setVisibility(View.VISIBLE);
			count.setText(getContext().getString(R.string.online_count, mOnlineContactCount));
			return;
		}
		if (entry instanceof SmsBackupEntry) {
			count.setVisibility(View.VISIBLE);
			count.setText(getContext().getString(R.string.online_count, mOnlineSMSCount));
			return;
		}
		if (entry instanceof MmsBackupEntry) {
			count.setVisibility(View.VISIBLE);
			count.setText(getContext().getString(R.string.online_count, mOnlineMMSCount));
			return;
		}
		if (entry instanceof CallLogBackupEntry) {
			count.setVisibility(View.VISIBLE);
			count.setText(getContext().getString(R.string.online_count, mOnlineCallLogCount));
			return;
		}
		if (entry instanceof CalendarBackupEntry) {
			count.setVisibility(View.VISIBLE);
			count.setText(getContext().getString(R.string.online_count, mOnlineCalendarCount));
			return;
		}
		if (entry instanceof BookMarkBackupEntry) {
			count.setVisibility(View.VISIBLE);
			count.setText(getContext().getString(R.string.online_count, mOnlineBookmarkCount));
			return;
		}

		view.setVisibility(View.GONE);
	}

	@Override
	protected void bindMarkerView(View view, BaseEntry entry) {
		if (entry instanceof AppBackupEntry && ((AppBackupEntry) entry).isProtectedApp()) {
			view.setVisibility(View.VISIBLE);
			return;
		}
		view.setVisibility(View.GONE);
	}

	private boolean isRootValid() {
		return RootShell.isRootValid();
	}

	private Date getEntryLastBackupDate(BaseEntry entry) {
		// 目前应用程序没有备份时间
		if (entry == null || mAllOnlineSystemBackupInfo == null) {
			return null;
		}

		BaseBackupEntryInfo entryInfo = getSystemBackupEntryInfo(entry.getType());
		if (entryInfo != null) {
			return entryInfo.backupDate;
		}
		return null;
	}

	private BaseBackupEntryInfo getSystemBackupEntryInfo(EntryType type) {
		if (type == null) {
			return null;
		}

		ListIterator<BaseBackupEntryInfo> listIterator = mAllOnlineSystemBackupInfo.listIterator();
		while (listIterator.hasNext()) {
			BaseBackupEntryInfo entryInfo = listIterator.next();
			if (entryInfo.type == type) {
				return entryInfo;
			}
		}
		return null;
	}

	public void selectUserAppEntry(BackupAppState state, boolean selected) {
		if (state == null) {
			return;
		}
		int appGoupPos = getGroupPositionByKey(IRecord.GROUP_USER_APP);
		if (appGoupPos < 0) {
			return;
		}
		final List<BaseEntry> group = (List<BaseEntry>) getGroup(appGoupPos);
		if (Util.isCollectionEmpty(group)) {
			return;
		}
		selectAppEntry(group, state, selected);
	}

	public void selectSystemAppEntry(BackupAppState state, boolean selected) {
		if (state == null) {
			return;
		}
		int appGoupPos = getGroupPositionByKey(IRecord.GROUP_SYSTEM_APP);
		if (appGoupPos < 0) {
			return;
		}
		final List<BaseEntry> group = (List<BaseEntry>) getGroup(appGoupPos);
		if (Util.isCollectionEmpty(group)) {
			return;
		}
		selectAppEntry(group, state, selected);
	}

	private void selectAppEntry(List<BaseEntry> appEntrys, BackupAppState state, boolean selected) {
		for (BaseEntry entry : appEntrys) {
			final AppBackupEntry appEntry = (AppBackupEntry) entry;
			final AppInfo localAppInfo = appEntry.getAppInfo();
			AppBackupEntryInfo appEntryInfo = mAllOnlineAppInfoMap != null
					? (AppBackupEntryInfo) mAllOnlineAppInfoMap.get(localAppInfo.packageName)
					: null;
			final AppInfo onlineAppInfo = appEntryInfo != null ? appEntryInfo.appInfo : null;
			if (match(state, localAppInfo, onlineAppInfo)) {
				entry.setSelected(selected);
			}
		}
	}

	private boolean match(BackupAppState state, AppInfo localAppInfo, AppInfo onlineAppInfo) {
		if (state == null || localAppInfo == null) {
			return false;
		}
		if (state == BackupAppState.NOT_BACKUPED && onlineAppInfo == null) {
			return true;
		}
		if (state == BackupAppState.BACKUPED && onlineAppInfo != null
				&& onlineAppInfo.versionCode >= localAppInfo.versionCode) {
			return true;
		}
		if (state == BackupAppState.UPDATABLE && onlineAppInfo != null
				&& onlineAppInfo.versionCode < localAppInfo.versionCode) {
			return true;
		}
		return false;
	}

	/**
	 * 枚举 备份状态
	 *
	 * @author WenCan
	 */
	public enum BackupAppState {
		BACKUPED, NOT_BACKUPED, UPDATABLE
	}

	@Override
	protected boolean isEntrySelectable(BaseEntry entry) {
		return true;
	}

	@Override
	public void sortAppEntries(SORT_TYPE sortType, IAsyncTaskListener listener) {
		executeRunnable(new OnlineAppEntrySortor(sortType, listener));
	}

	/**
	 * 网络备份app排序sortor
	 *
	 * @author WenCan
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
			List<BaseBackupEntry> userEntries = (List<BaseBackupEntry>) (getRecord())
					.getGroup(IRecord.GROUP_USER_APP);
			List<BaseBackupEntry> systemAppEntries = (List<BaseBackupEntry>) (getRecord())
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
	 * 网络备份app状态比较
	 *
	 * @author WenCan
	 */
	private class OnlineAppEntryBackupStateComparator extends AppEntryComparator<BaseEntry> {

		@Override
		public int compare(BaseEntry lhs, BaseEntry rhs) {
			int lhsOriginal = getOnlineBackupState((AppBackupEntry) lhs).ordinal();
			int rhsOriginal = getOnlineBackupState((AppBackupEntry) rhs).ordinal();

			if (lhsOriginal == rhsOriginal) {
				return 0;
			}
			if (lhsOriginal > rhsOriginal) {
				return 1;
			} else {
				return -1;
			}
		}

	}

	/**
	 * 网络备份app名字比较
	 *
	 * @author WenCan
	 */
	private class OnlineAppEntryNameComparator extends AppEntryComparator<BaseEntry> {

		private OnlineAppEntryBackupStateComparator mOnlineAppEntryBackupStateComparator = new OnlineAppEntryBackupStateComparator();

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
	 * 网络备份app大小比较
	 *
	 * @author WenCan
	 */
	private class OnlineAppEntrySizeComparator extends AppEntryComparator<BaseEntry> {
		private OnlineAppEntryBackupStateComparator mOnlineAppEntryBackupStateComparator = new OnlineAppEntryBackupStateComparator();

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

	private OnlineBackupState getOnlineBackupState(AppBackupEntry appEntry) {
		if (appEntry == null) {
			throw new IllegalArgumentException("appEntry can not be null");
		}
		if (mAllOnlineAppInfoMap == null || mAllOnlineAppInfoMap.size() == 0) {
			return OnlineBackupState.NOT_BACKUPED;
		}

		AppInfo appInfo = appEntry.getAppInfo();
		if (!mAllOnlineAppInfoMap.containsKey(appInfo.packageName)) {
			return OnlineBackupState.NOT_BACKUPED;
		}
		AppBackupEntryInfo onlineAppBackupEntryInfo = mAllOnlineAppInfoMap.get(appInfo.packageName);
		return onlineAppBackupEntryInfo.appInfo.versionCode < appInfo.versionCode
				? OnlineBackupState.UPDATABLE
				: OnlineBackupState.BACKUPED;
	}

	/**
	 * 枚举，在线备份状态
	 *
	 * @author WenCan
	 */
	private enum OnlineBackupState {
		NOT_BACKUPED, UPDATABLE, BACKUPED
	}

	public void resetDefaultSelectedItem() {
		// 默认选择用户数据
		checkGroupAllEntries(getGroupPositionByKey(IRecord.GROUP_USER_DATA), true);

		// 日历和用户词典默认补选中
		List<BaseEntry> allUserDataEntries = (List<BaseEntry>) getGroup(getGroupPositionByKey(IRecord.GROUP_USER_DATA));
		if (allUserDataEntries == null || allUserDataEntries.size() == 0) {
			return;
		}

		final int length = DEFAULT_UNSELECTED_ENTRYTYPE_FILTER.length;
		for (int i = 0; i < length; i++) {
			for (BaseEntry entry : allUserDataEntries) {
				if (entry.isSelected() && entry.getType() == DEFAULT_UNSELECTED_ENTRYTYPE_FILTER[i]) {
					entry.setSelected(false);
				}
			}
		}
	}
}
