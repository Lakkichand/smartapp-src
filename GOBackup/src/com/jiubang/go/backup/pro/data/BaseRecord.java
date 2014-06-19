package com.jiubang.go.backup.pro.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.TreeSet;

import android.content.Context;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.data.AppEntryComparator.SORT_TYPE;
import com.jiubang.go.backup.pro.data.BaseEntry.EntryComparator;
import com.jiubang.go.backup.pro.data.BaseEntry.EntryType;
import com.jiubang.go.backup.pro.image.util.FolderBackEntry;
import com.jiubang.go.backup.pro.image.util.FolderRestoreEntry;
import com.jiubang.go.backup.pro.image.util.ImageBackupEntry;
import com.jiubang.go.backup.pro.image.util.ImageRestoreEntry;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 恢复记录基类
 *
 * @author maiyongshen
 */
public abstract class BaseRecord implements IRecord {
	private HashMap<String, List<BaseEntry>> mRecordData;
	private Context mContext;

	public BaseRecord(Context context) {
		if (context == null) {
			throw new IllegalArgumentException(getClass().getName() + " context cannot be null!");
		}
		mContext = context.getApplicationContext();
		mRecordData = new HashMap<String, List<BaseEntry>>();
	}

	@Override
	public long getId() {
		Date date = getDate();
		return date != null ? date.getTime() : -1;
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public int getGroupCount() {
		return mRecordData == null ? 0 : mRecordData.size();
	}

	@Override
	public Object getGroup(String groupKey) {
		if (groupKey == null || mRecordData == null) {
			return null;
		}
		return mRecordData.get(groupKey);
	}

	@Override
	public String[] getGroupKeys() {
		if (mRecordData == null) {
			return null;
		}
		TreeSet<String> sortSet = new TreeSet<String>(new GroupComparator());
		Set<String> keySet = mRecordData.keySet();
		if (keySet != null) {
			sortSet.addAll(keySet);
		}
		return sortSet.toArray(new String[sortSet.size()]);
	}

	@Override
	public CharSequence getGroupDescription(String groupKey) {
		if (IRecord.GROUP_SYSTEM_APP.equals(groupKey)) {
			return mContext.getString(R.string.group_system_app);
		} else if (IRecord.GROUP_SYSTEM_DATA.equals(groupKey)) {
			return mContext.getString(R.string.group_system_data);
		} else if (IRecord.GROUP_USER_APP.equals(groupKey)) {
			return mContext.getString(R.string.group_user_app);
		} else if (IRecord.GROUP_USER_DATA.equals(groupKey)) {
			return mContext.getString(R.string.group_user_data);
		} else if (IRecord.GROUP_USER_IMAGE.equals(groupKey)) {
			return mContext.getString(R.string.group_user_image);
		}

		return null;
	}

	@Override
	public int getGroupItemsCount(String groupKey) {
		if (groupKey == null || mRecordData == null) {
			return 0;
		}
		final List<BaseEntry> entries = mRecordData.get(groupKey);
		return entries != null ? entries.size() : 0;
	}

	@Override
	public Object getEntry(String groupKey, int childPosition) {
		if (groupKey == null || mRecordData == null) {
			return null;
		}

		List<BaseEntry> entries = mRecordData.get(groupKey);
		if (Util.isCollectionEmpty(entries)) {
			return null;
		}
		if (entries.get(0) instanceof ImageBackupEntry) {
			return getImageEntry(groupKey, childPosition);
		}
		if (entries.get(0) instanceof ImageRestoreEntry) {
			return getImageRestoreEntry(groupKey, childPosition);
		}
		final int count = entries.size();
		if (count == 0 || childPosition < 0 || childPosition >= count) {
			return null;
		}
		return entries.get(childPosition);
	}

	//获得照片分组下的folderentry
	public Object getImageEntry(String groupKey, int childPosition) {
		List<BaseEntry> entries = mRecordData.get(groupKey);
		final int count = entries != null ? ((ImageBackupEntry) entries.get(0)).getEntryList()
				.size() : 0;
		if (count == 0 || childPosition < 0 || childPosition >= count) {
			return null;
		}
		ImageBackupEntry entry = (ImageBackupEntry) entries.get(0);
		return entry.getEntryList().get(childPosition);

	}

	//获得照片分组下的folderentry
	public Object getImageRestoreEntry(String groupKey, int childPosition) {
		List<BaseEntry> entries = mRecordData.get(groupKey);
		final int count = entries != null ? ((ImageRestoreEntry) entries.get(0)).getEntryList()
				.size() : 0;
		if (count == 0 || childPosition < 0 || childPosition >= count) {
			return null;
		}
		ImageRestoreEntry entry = (ImageRestoreEntry) entries.get(0);
		return entry.getEntryList().get(childPosition);

	}

	@Override
	public void addGroup(String groupKey, Collection<BaseEntry> entries) {
		if (groupKey == null || Util.isCollectionEmpty(entries)) {
			return;
		}
		for (BaseEntry entry : entries) {
			addEntry(groupKey, entry);
		}
	}

	@Override
	public void addEntry(String groupKey, BaseEntry entry) {
		if (groupKey == null || !shouldBeAdded(entry)) {
			return;
		}
		List<BaseEntry> group = mRecordData.get(groupKey);
		if (group == null) {
			group = new ArrayList<BaseEntry>();
			mRecordData.put(groupKey, group);
		}
		group.add(entry);
	}

	@Override
	public Object removeGroup(String groupKey) {
		return mRecordData.remove(groupKey);
	}

	@Override
	public void removeEntry(BaseEntry entry) {
		final Collection<List<BaseEntry>> values = mRecordData.values();
		for (List<BaseEntry> entries : values) {
			entries.remove(entry);
		}
	}

	public List<BaseEntry> getEntrysByEntryType(EntryType type) {
		if (type == null) {
			return null;
		}

		if (mRecordData == null || mRecordData.size() < 1) {
			return null;
		}
		List<BaseEntry> result = null;
		final Collection<List<BaseEntry>> values = mRecordData.values();
		for (List<BaseEntry> entries : values) {
			for (BaseEntry entry : entries) {
				if (entry.getType() != type) {
					continue;
				}

				if (result == null) {
					result = new ArrayList<BaseEntry>();
				}
				result.add(entry);
			}
		}
		return result;
	}

	public List<BaseEntry> getSelectedEntries() {
		if (mRecordData == null || mRecordData.size() < 1) {
			return null;
		}
		List<BaseEntry> result = new ArrayList<BaseEntry>();
		// 按用户数据、系统数据、用户程序排序
		List<BaseEntry> entries = getGroupSelectedEntries(IRecord.GROUP_USER_DATA);
		if (!Util.isCollectionEmpty(entries)) {
			result.addAll(entries);
		}

		entries = getGroupSelectedEntries(IRecord.GROUP_SYSTEM_DATA);
		if (!Util.isCollectionEmpty(entries)) {
			result.addAll(entries);
		}

		entries = getGroupSelectedEntries(IRecord.GROUP_USER_APP);
		if (!Util.isCollectionEmpty(entries)) {
			result.addAll(entries);
		}
		entries = getGroupSelectedEntries(IRecord.GROUP_USER_IMAGE);
		if (!Util.isCollectionEmpty(entries)) {
			result.addAll(entries);
		}
		return result;
	}

	private List<BaseEntry> getGroupSelectedEntries(String groupKey) {
		List<BaseEntry> entries = (List<BaseEntry>) getGroup(groupKey);

		if (Util.isCollectionEmpty(entries)) {
			return null;
		}
		if (entries.get(0) instanceof ImageBackupEntry) {
			List<BaseBackupEntry> folderEntries = ((ImageBackupEntry) entries.get(0))
					.getEntryList();
			entries.clear();
			for (BaseBackupEntry folderEntry : folderEntries) {
				entries.addAll(((FolderBackEntry) folderEntry).getEntryList());
			}
		}
		if (entries.get(0) instanceof ImageRestoreEntry) {
			List<BaseRestoreEntry> folderEntries = ((ImageRestoreEntry) entries.get(0))
					.getEntryList();
			entries.clear();
			for (BaseRestoreEntry folderEntry : folderEntries) {
				entries.addAll(((FolderRestoreEntry) folderEntry).getEntryList());
			}
		}
		List<BaseEntry> result = new ArrayList<BaseEntry>();
		for (BaseEntry entry : entries) {
			if (!entry.isSelected()) {
				continue;
			}
			result.add(entry);
		}
		return result;
	}

	public int getSelectedEntriesCount() {
		List<BaseEntry> entries = getSelectedEntries();
		return entries != null ? entries.size() : 0;
	}

	public void selectAllEntries(boolean selected) {
		final Collection<List<BaseEntry>> values = mRecordData.values();
		for (List<BaseEntry> entries : values) {
			for (BaseEntry entry : entries) {
				entry.setSelected(selected);
			}
		}
	}

	public Context getContext() {
		return mContext;
	}

	public Iterator<List<BaseEntry>> getRecordDataIterator() {
		return mRecordData.values().iterator();
	}

	public int getTotalEntriesCount() {
		int count = 0;
		final Collection<List<BaseEntry>> values = mRecordData.values();
		for (List<BaseEntry> entries : values) {
			count += entries.size();
		}
		return count;
	}

	public void clear() {
		final Collection<List<BaseEntry>> values = mRecordData.values();
		for (List<BaseEntry> entries : values) {
			entries.clear();
		}
		mRecordData.clear();
	}

	public boolean isEmpty() {
		return mRecordData == null || mRecordData.size() < 1;
	}

	public synchronized void sortEntries(SORT_TYPE sortType) {
		// 排序
		EntryComparator entryComparator = new EntryComparator();
		List<BaseEntry> userDataEntries = (List<BaseEntry>) getGroup(IRecord.GROUP_USER_DATA);
		if (!Util.isCollectionEmpty(userDataEntries)) {
			Collections.sort(userDataEntries, entryComparator);
		}
		List<BaseEntry> systemDataEntries = (List<BaseEntry>) getGroup(IRecord.GROUP_SYSTEM_DATA);
		if (!Util.isCollectionEmpty(systemDataEntries)) {
			Collections.sort(systemDataEntries, entryComparator);
		}
		sortAppEntries(sortType);
	}

	public synchronized void sortAppEntries(SORT_TYPE sortType) {
		if (sortType == null) {
			return;
		}
		List<BaseEntry> userAppEntries = (List<BaseEntry>) getGroup(IRecord.GROUP_USER_APP);
		List<BaseEntry> systemAppEntries = (List<BaseEntry>) getGroup(IRecord.GROUP_SYSTEM_APP);
		if (Util.isCollectionEmpty(userAppEntries) && Util.isCollectionEmpty(systemAppEntries)) {
			return;
		}
		if (sortType == SORT_TYPE.SORT_BY_APP_NAME) {
			AppEntryNameComparator appNameComparator = new AppEntryNameComparator();
			if (userAppEntries != null) {
				Collections.sort(userAppEntries, appNameComparator);
			}
			if (systemAppEntries != null) {
				Collections.sort(systemAppEntries, appNameComparator);
			}
			return;
		}
		if (sortType == SORT_TYPE.SORT_BY_APP_SIZE) {
			AppEntrySizeComparator appSizeComparator = new AppEntrySizeComparator();
			if (userAppEntries != null) {
				Collections.sort(userAppEntries, appSizeComparator);
			}
			if (systemAppEntries != null) {
				Collections.sort(systemAppEntries, appSizeComparator);
			}
			return;
		}
		if (sortType == SORT_TYPE.SORT_BY_APP_INSTALL_TIME) {
			AppEntryInstallDateComparator appInstallDateComparator = new AppEntryInstallDateComparator();
			if (userAppEntries != null) {
				Collections.sort(userAppEntries, appInstallDateComparator);
			}
			if (systemAppEntries != null) {
				Collections.sort(systemAppEntries, appInstallDateComparator);
			}
			return;
		}
		if (sortType == SORT_TYPE.SORT_BY_APP_INSTALL_STATE) {
			AppEntryInstallStateComparator appInstallStateComparator = new AppEntryInstallStateComparator();
			if (userAppEntries != null) {
				Collections.sort(userAppEntries, appInstallStateComparator);
			}
			if (systemAppEntries != null) {
				Collections.sort(systemAppEntries, appInstallStateComparator);
			}
			return;
		}
	}

	public long getSelectedEntriesSpaceUsed() {
		List<BaseEntry> selectedEntries = getSelectedEntries();
		if (Util.isCollectionEmpty(selectedEntries)) {
			return 0;
		}

		long totalSize = 0;
		ListIterator<BaseEntry> listIterator = selectedEntries.listIterator();
		while (listIterator.hasNext()) {
			try {
				BaseEntry entry = listIterator.next();
				if (entry != null) {
					totalSize += entry.getSpaceUsage();
				}
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}
		return totalSize;
	}

	protected abstract boolean shouldBeAdded(BaseEntry entry);

	/**
	 * GroupComparator
	 *
	 * @author maiyongshen
	 */
	private class GroupComparator implements Comparator<String> {
		@Override
		public int compare(String lhs, String rhs) {
			final int lhsFactor = calculateComparatorFactor(lhs);
			final int rhsFactor = calculateComparatorFactor(rhs);
			return rhsFactor - lhsFactor;
		}

		private int calculateComparatorFactor(String key) {
			final int m4 = 4;
			final int m3 = 3;

			if (IRecord.GROUP_USER_DATA.equals(key)) {
				return m4;
			}
			if (IRecord.GROUP_SYSTEM_DATA.equals(key)) {
				return m3;
			}
			if (IRecord.GROUP_USER_APP.equals(key)) {
				return 2;
			}
			if (IRecord.GROUP_SYSTEM_APP.equals(key)) {
				return 1;
			}
			return 0;
		}

	}
}
