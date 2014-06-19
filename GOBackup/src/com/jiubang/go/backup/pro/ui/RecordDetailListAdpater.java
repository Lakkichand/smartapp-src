package com.jiubang.go.backup.pro.ui;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.data.AppEntry;
import com.jiubang.go.backup.pro.data.AppEntryComparator.SORT_TYPE;
import com.jiubang.go.backup.pro.data.AppInfo;
import com.jiubang.go.backup.pro.data.AppStateSelector;
import com.jiubang.go.backup.pro.data.BaseBackupEntry;
import com.jiubang.go.backup.pro.data.BaseEntry;
import com.jiubang.go.backup.pro.data.BaseRecord;
import com.jiubang.go.backup.pro.data.BaseRestoreEntry;
import com.jiubang.go.backup.pro.data.IRecord;
import com.jiubang.go.backup.pro.image.util.FolderBackEntry;
import com.jiubang.go.backup.pro.image.util.FolderRestoreEntry;
import com.jiubang.go.backup.pro.image.util.ImageBackupEntry;
import com.jiubang.go.backup.pro.image.util.ImageRestoreEntry;
import com.jiubang.go.backup.pro.model.DrawableProvider.OnDrawableLoadedListener;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 恢复详细列表adapter
 *
 * @author maiyongshen
 */
public abstract class RecordDetailListAdpater extends BaseExpandableListAdapter
		implements
			IAsyncTaskListener {
	/*
	 * private static final int SYSTEM_DATA = 0; private static final int
	 * USER_DATA = SYSTEM_DATA + 1;
	 */
	private static final int MSG_NOTIFY_DATASET_CHANGED = 0x1001;
	private static final int MSG_SORT_RECORD = 0x1002;
	private static final int MSG_NOTIFY_ADAPTER_CHILD_ITEM_UPDATE_LISTENER = 0x1004;
	private static final int MSG_NOTIFY_ADAPTER_GROUP_ITEM_UPDATE_LISTENER = 0x1005;

	private static final int DEFAULT_POOL_SIZE = 2;
	private IRecord mRecord;
	private Context mContext;
	private LayoutInflater mInflater;
	private BaseEntry.OnSelectedChangeListener mOnEntrySelectedListener;
	private OnAdapterItemUpdateListener mAdapterItemUpdateListener;
	private ExecutorService mThreadPool;

	public RecordDetailListAdpater(Context context, IRecord record) {
		if (context == null) {
			throw new IllegalArgumentException("invalid argument");
		}
		mContext = context.getApplicationContext();
		mInflater = LayoutInflater.from(context);
		mRecord = record;
		mThreadPool = Executors.newFixedThreadPool(DEFAULT_POOL_SIZE);
	}

	public void release() {

	}

	public Context getContext() {
		return mContext;
	}

	@Override
	public int getGroupCount() {
		if (mRecord != null) {
			return mRecord.getGroupCount();
		}
		return 0;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		if (mRecord != null && groupPosition >= 0 && groupPosition < getGroupCount()) {
			final String[] keys = mRecord.getGroupKeys();
			if (keys[groupPosition].equals(IRecord.GROUP_USER_IMAGE)) {
				return getImageGroupItemsCount(groupPosition);
			}
			return mRecord.getGroupItemsCount(keys[groupPosition]);
		}
		return 0;
	}

	//获得照片分组下的项个数
	public int getImageGroupItemsCount(int groupPos) {
		int count = 0;
		final String[] keys = mRecord.getGroupKeys();
		if (keys[groupPos].equals(IRecord.GROUP_USER_IMAGE)) {
			List<BaseEntry> entry = (List<BaseEntry>) mRecord.getGroup(keys[groupPos]);
			count = getImageEntriesCount(entry.get(0));
		}
		return count;
	}

	//返回文件夹个数
	public int getImageEntriesCount(BaseEntry entry) {
		int count = 0;
		if (entry instanceof ImageBackupEntry) {
			List<BaseBackupEntry> folderEntry = ((ImageBackupEntry) entry).getEntryList();
			count = folderEntry != null ? folderEntry.size() : 0;
		} else {
			List<BaseRestoreEntry> folderEntry = ((ImageRestoreEntry) entry).getEntryList();
			count = folderEntry != null ? folderEntry.size() : 0;
		}

		return count;

	}

	@Override
	public Object getGroup(int groupPosition) {
		if (mRecord != null && groupPosition >= 0 && groupPosition < getGroupCount()) {
			final String[] keys = mRecord.getGroupKeys();
			return mRecord.getGroup(keys[groupPosition]);
		}
		return null;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		if (mRecord != null) {
			final String[] keys = mRecord.getGroupKeys();
			if (keys != null && groupPosition < keys.length) {
				return mRecord.getEntry(keys[groupPosition], childPosition);
			}
		}
		return null;
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		BaseEntry child = getEntry(groupPosition, childPosition);
		if (child != null) {
			return child.getId();
		}
		return -1;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public View getGroupView(final int groupPosition, boolean isExpanded, View convertView,
			ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.layout_record_group_view, parent, false);
		}
		updateAdapterGroupItemView(convertView, groupPosition);
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
			View convertView, ViewGroup parent) {
		// Log.d("GoBackup", "getChildView");
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.layout_record_entry_view, parent, false);
		}
		updateAdapterChildItemView(convertView, groupPosition, childPosition);
		return convertView;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		BaseEntry entry = getEntry(groupPosition, childPosition);
		if (entry == null) {
			return false;
		}
		return isEntrySelectable(entry);
	}

	public BaseEntry getEntry(int groupPos, int childPos) {
		return (BaseEntry) getChild(groupPos, childPos);
	}

	public void updateAdapterGroupItemView(View convertView, int groupPos) {
		if (convertView == null) {
			return;
		}
		final TextView groupTitle = (TextView) convertView.findViewById(R.id.group_title);
		if (groupTitle != null) {
			groupTitle.setText(getGroupDescription(groupPos));
		}

		final TextView titleExtraInfo = (TextView) convertView.findViewById(R.id.group_extra_info);
		if (titleExtraInfo != null) {
			final int childCount = getChildCount(groupPos);
			final int checkedChildCount = getCheckedChildItemCount(groupPos);
			String checkChildInfo = mContext.getString(R.string.progress_detail, checkedChildCount,
					childCount);
			titleExtraInfo.setText(mContext.getString(R.string.parenthesized_msg, checkChildInfo));
		}

		final CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkbox);
		if (checkBox != null) {
			if (areAllChildrenUnselectable(groupPos)) {
				checkBox.setVisibility(View.INVISIBLE);
			} else {
				checkBox.setVisibility(View.VISIBLE);
				boolean result = isAllChildItemSelected(groupPos);
				checkBox.setOnCheckedChangeListener(null);
				checkBox.setChecked(result);
				checkBox.setTag(groupPos);
				checkBox.setOnCheckedChangeListener(mGroupCheckListener);
			}
		}
	}

	//获得照片的张数，或者是其他的备份项数
	public int getChildCount(int groupPos) {
		int count = 0;
		final String[] keys = mRecord.getGroupKeys();
		if (keys[groupPos].equals(IRecord.GROUP_USER_IMAGE)) {
			List<BaseEntry> entry = (List<BaseEntry>) mRecord.getGroup(keys[groupPos]);
			count = getImageCount(entry.get(0));
		} else {
			count = getChildrenCount(groupPos);
		}
		return count;

	}

	//可能是备份，或者是恢复类别
	public int getImageCount(BaseEntry entry) {
		int count = 0;
		if (entry instanceof ImageBackupEntry) {
			count = ((ImageBackupEntry) entry).getCount();
		} else if (entry instanceof ImageRestoreEntry) {
			count = ((ImageRestoreEntry) entry).getCount();
		}
		return count;

	}

	public void updateAdapterChildItemView(View convertView, int groupPos, int childPos) {
		if (convertView == null) {
			return;
		}
		BaseEntry entry = getEntry(groupPos, childPos);
		if (entry == null) {
			return;
		}

		final ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
		//		icon.setImageDrawable(entry.getIcon(mContext));
		//		if (!entry.isIconIniting() && !entry.hasIconInited()) {
		//			mThreadPool.execute(new RecordEntryIconLoader(entry, groupPos, childPos));
		//		}
		if (icon != null) {
			icon.setImageDrawable(getEntryIcon(entry, groupPos, childPos));
		}

		final TextView title = (TextView) convertView.findViewById(R.id.entry_title);
		if (title != null) {
			title.setText(entry.getDescription());
		}

		final TextView extraTitle = (TextView) convertView.findViewById(R.id.title_extra_info);
		if (extraTitle != null) {
			bindTitleExtraInfo(extraTitle, entry);
		}

		final TextView summary = (TextView) convertView.findViewById(R.id.entry_summary1);
		if (summary != null) {
			bindSummaryInfo(summary, entry);
		}

		final TextView extraSummary = (TextView) convertView.findViewById(R.id.entry_summary2);
		if (extraSummary != null) {
			bindExtraSummaryInfo(extraSummary, entry);
		}

		final ImageView markerView = (ImageView) convertView.findViewById(R.id.private_flag);
		if (markerView != null) {
			bindMarkerView(markerView, entry);
		}

		final CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkbox);
		if (checkBox != null) {
			bindCheckbox(checkBox, entry, groupPos, childPos);
		}
	}

	public void setOnEntrySelectedListener(BaseEntry.OnSelectedChangeListener l) {
		mOnEntrySelectedListener = l;
	}

	public void setOnAdapterItemUpdateListener(OnAdapterItemUpdateListener l) {
		mAdapterItemUpdateListener = l;
	}

	public void selectEntry(int groupPos, int childPos, boolean selected) {
		final BaseEntry entry = getEntry(groupPos, childPos);
		selectEntry(entry, selected);
	}

	public void toggleEntry(int groupPos, int childPos) {
		final BaseEntry entry = getEntry(groupPos, childPos);
		if (entry == null) {
			return;
		}
		selectEntry(entry, !entry.isSelected());
	}

	private void selectEntry(BaseEntry entry, boolean selected) {
		if (entry == null || entry.isSelected() == selected || !isEntrySelectable(entry)) {
			return;
		}
		entry.setOnSelectedChangeListener(mOnEntrySelectedListener);
		entry.setSelected(selected);
	}

	private int getCheckedChildItemCount(int groupPos) {
		final List<BaseEntry> group = (List<BaseEntry>) getGroup(groupPos);
		if (group == null) {
			return 0;
		}
		if (group.size() <= 0) {
			return 0;
		}
		int count = 0;
		if (group.get(0) instanceof ImageBackupEntry) {
			count = getImageSelectdCount(group);
			return count;
		}
		if (group.get(0) instanceof ImageRestoreEntry) {
			count = getImageRestoreSelectdCount(group);
			return count;
		}
		for (BaseEntry entry : group) {
			if (entry.isSelected() && isEntrySelectable(entry)) {
				count++;
			}
		}
		return count;
	}

	//备份时获得选中照片个数
	private int getImageSelectdCount(List<BaseEntry> group) {
		int count = 0;
		if (group == null) {
			return 0;
		}
		List<BaseBackupEntry> folderEntry = ((ImageBackupEntry) group.get(0)).getEntryList();
		for (BaseBackupEntry entry : folderEntry) {
			List<BaseBackupEntry> imageEntryList = ((FolderBackEntry) entry).getEntryList();
			for (BaseBackupEntry imageEntry : imageEntryList) {
				if (imageEntry.isSelected()) {
					count++;
				}
			}
		}
		return count;
	}

	//恢复时获得选中照片个数
	private int getImageRestoreSelectdCount(List<BaseEntry> group) {
		int count = 0;
		if (group == null) {
			return 0;
		}
		List<BaseRestoreEntry> folderEntry = ((ImageRestoreEntry) group.get(0)).getEntryList();
		for (BaseRestoreEntry entry : folderEntry) {
			List<BaseRestoreEntry> imageEntryList = ((FolderRestoreEntry) entry).getEntryList();
			for (BaseRestoreEntry imageEntry : imageEntryList) {
				if (imageEntry.isSelected()) {
					count++;
				}
			}
		}
		return count;
	}

	public synchronized boolean isAllChildItemSelected(int groupPosition) {
		final List<BaseEntry> group = (List<BaseEntry>) getGroup(groupPosition);
		if (group == null) {
			return false;
		}
		for (BaseEntry entry : group) {
			// 跳过不可用的项
			if (entry != null && !entry.isSelected()) {
				if (!isEntrySelectable(entry)) {
					continue;
				}
				return false;
			}
		}
		return true;
	}

	private synchronized boolean areAllChildrenUnselectable(int groupPos) {
		final List<BaseEntry> group = (List<BaseEntry>) getGroup(groupPos);
		if (group == null) {
			return true;
		}
		for (BaseEntry entry : group) {
			if (entry != null && isEntrySelectable(entry)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 获取选中项的使用的空间
	 * @return
	 */
	public synchronized int getSelectedEntriesSpaceUsage() {
		int spaceUsage = 0;
		final int groupCount = getGroupCount();
		if (groupCount <= 0) {
			return 0;
		}
		for (int i = 0; i < groupCount; i++) {
			final List<BaseEntry> group = (List<BaseEntry>) getGroup(i);
			if (group == null) {
				continue;
			}
			for (BaseEntry entry : group) {
				if (entry.isSelected()) {
					spaceUsage += getEntrySpaceUsage(entry);
				}
			}
		}
		return spaceUsage;
	}

	public synchronized boolean hasChildItemSelected() {
		final int groupCount = getGroupCount();
		if (groupCount <= 0) {
			return false;
		}
		for (int i = 0; i < groupCount; i++) {
			final List<BaseEntry> group = (List<BaseEntry>) getGroup(i);
			if (group == null) {
				continue;
			}
			for (BaseEntry entry : group) {
				if (entry.isSelected() && isEntrySelectable(entry)) {
					return true;
				} else if (group.get(0) instanceof ImageBackupEntry) {
					return hasImageItemSeleted(group);
				} else if (group.get(0) instanceof ImageRestoreEntry) {
					return hasImageItemRestoreSeleted(group);
				}
			}
		}
		return false;
	}

	//备份时判断有没有相片选中
	public boolean hasImageItemSeleted(List<BaseEntry> group) {
		boolean ret = false;
		if (group == null) {
			return false;
		}
		for (BaseEntry entry : group) {
			List<BaseBackupEntry> folderEntry = ((ImageBackupEntry) entry).getEntryList();
			for (BaseBackupEntry folder : folderEntry) {
				List<BaseBackupEntry> imageEntry = ((FolderBackEntry) folder).getEntryList();
				for (BaseBackupEntry baseEntry : imageEntry) {
					if (baseEntry.isSelected()) {
						ret = true;
						return ret;
					}
				}
			}
		}
		return ret;
	}

	//恢复是判断有没有相片选中
	public boolean hasImageItemRestoreSeleted(List<BaseEntry> group) {
		boolean ret = false;
		if (group == null) {
			return false;
		}
		for (BaseEntry entry : group) {
			List<BaseRestoreEntry> folderEntry = ((ImageRestoreEntry) entry).getEntryList();
			for (BaseRestoreEntry folder : folderEntry) {
				List<BaseRestoreEntry> imageEntry = ((FolderRestoreEntry) folder).getEntryList();
				for (BaseRestoreEntry baseEntry : imageEntry) {
					if (baseEntry.isSelected()) {
						ret = true;
						return ret;
					}
				}
			}
		}
		return ret;
	}

	private CharSequence getGroupDescription(int groupPosition) {
		if (mRecord != null && groupPosition >= 0 && groupPosition < getGroupCount()) {
			final String[] keys = mRecord.getGroupKeys();
			return mRecord.getGroupDescription(keys[groupPosition]);
		}
		return null;
	}

	public int getGroupPositionByKey(String key) {
		if (key == null || mRecord == null || getGroupCount() <= 0) {
			return -1;
		}
		String[] groupKeys = mRecord.getGroupKeys();
		if (groupKeys == null || groupKeys.length <= 0) {
			return -1;
		}
		final int count = groupKeys.length;
		for (int i = 0; i < count; i++) {
			if (key.equals(groupKeys[i])) {
				return i;
			}
		}
		return -1;
	}

	public boolean hasAppEntrySelected() {
		int userAppGoupPos = getGroupPositionByKey(IRecord.GROUP_USER_APP);
		int systemAppGoupPos = getGroupPositionByKey(IRecord.GROUP_SYSTEM_APP);
		if (userAppGoupPos < 0 && systemAppGoupPos < 0) {
			return false;
		}
		final List<BaseEntry> userGroup = (List<BaseEntry>) getGroup(userAppGoupPos);
		final List<BaseEntry> systemGroup = (List<BaseEntry>) getGroup(systemAppGoupPos);
		if (userGroup != null) {
			for (BaseEntry entry : userGroup) {
				if (entry.isSelected()) {
					return true;
				}
			}
		}

		if (systemGroup != null) {
			for (BaseEntry entry : systemGroup) {
				if (entry.isSelected()) {
					return true;
				}
			}
		}
		return false;
	}

	public void checkGroupAllEntries(int position, boolean isChecked) {
		if (position < 0 || position > getGroupCount()) {
			return;
		}
		final List<BaseEntry> group = (List<BaseEntry>) getGroup(position);
		if (group == null) {
			return;
		}
		for (BaseEntry entry : group) {
			if (!isEntrySelectable(entry)) {
				if (entry.isSelected()) {
					entry.setSelected(false);
				}
				continue;
			}
			selectEntry(entry, isChecked);
		}
		notifyDataSetChanged();
	}

	public void selectUserAppEntries(AppStateSelector selector) {
		if (selector == null) {
			return;
		}
		final List<BaseEntry> appEntries = getUserAppEntries();
		if (Util.isCollectionEmpty(appEntries)) {
			return;
		}
		for (BaseEntry entry : appEntries) {
			if (!isEntrySelectable(entry)) {
				if (entry.isSelected()) {
					entry.setSelected(false);
				}
				continue;
			}
			final AppEntry appEntry = (AppEntry) entry;
			final AppInfo appInfo = appEntry.getAppInfo();
			entry.setSelected(selector.match(getContext(), appInfo));
		}
	}

	public List<BaseEntry> getUserAppEntries() {
		int appGoupPos = getGroupPositionByKey(IRecord.GROUP_USER_APP);
		if (appGoupPos < 0) {
			return null;
		}
		return (List<BaseEntry>) getGroup(appGoupPos);
	}

	public IRecord getRecord() {
		return mRecord;
	}

	private OnCheckedChangeListener mGroupCheckListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if (buttonView.getTag() instanceof Integer) {
				int groupPosition = (Integer) buttonView.getTag();
				checkGroupAllEntries(groupPosition, isChecked);
			}
		}
	};

	private OnCheckedChangeListener mEntryCheckedChangeListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if (buttonView.getTag() instanceof EntryHolder) {
				EntryHolder entryInfo = (EntryHolder) buttonView.getTag();
				BaseEntry entry = entryInfo.mEntry;
				selectEntry(entry, isChecked);
				Message.obtain(mHandler, MSG_NOTIFY_ADAPTER_GROUP_ITEM_UPDATE_LISTENER,
						entryInfo.mGroupPos, entryInfo.mChildPos).sendToTarget();
				notifyDataSetChanged();
			}
		}
	};

	public void sortAppEntries(final SORT_TYPE sortType, IAsyncTaskListener listener) {
		mThreadPool.execute(new AppEntriesSorter(sortType, listener));
	}

	public void executeRunnable(Runnable runnable) {
		if (runnable == null) {
			return;
		}
		mThreadPool.execute(runnable);
	}

	protected void updateAllViews() {
		if (!mHandler.hasMessages(MSG_NOTIFY_DATASET_CHANGED)) {
			mHandler.sendEmptyMessage(MSG_NOTIFY_DATASET_CHANGED);
		}
	}

	protected void updateGroupView(int groupPos) {
		Message.obtain(mHandler, MSG_NOTIFY_ADAPTER_GROUP_ITEM_UPDATE_LISTENER, groupPos, -1)
				.sendToTarget();
	}

	protected void updateChildView(int groupPos, int childPos) {
		Message.obtain(mHandler, MSG_NOTIFY_ADAPTER_CHILD_ITEM_UPDATE_LISTENER, groupPos, childPos)
				.sendToTarget();
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_NOTIFY_DATASET_CHANGED :
					notifyDataSetChanged();
					break;
				case MSG_NOTIFY_ADAPTER_CHILD_ITEM_UPDATE_LISTENER :
					if (mAdapterItemUpdateListener != null) {
						mAdapterItemUpdateListener.onAdapterChildItemUpdate(
								RecordDetailListAdpater.this, msg.arg1, msg.arg2);
					}
					break;
				case MSG_NOTIFY_ADAPTER_GROUP_ITEM_UPDATE_LISTENER :
					if (mAdapterItemUpdateListener != null) {
						mAdapterItemUpdateListener.onAdapterGroupItemUpdate(
								RecordDetailListAdpater.this, msg.arg1);
					}
					break;
				default :
					break;
			}
		}
	};

	@Override
	public void onStart(Object arg1, Object arg2) {

	}

	@Override
	public void onProceeding(Object progress, Object arg2, Object arg3, Object arg4) {

	}

	@Override
	public void onEnd(boolean success, Object arg1, Object arg2) {
		mHandler.sendEmptyMessage(MSG_NOTIFY_DATASET_CHANGED);
	}

	/**
	 * app排序
	 *
	 * @author GoBackup Dev Team
	 */
	private class AppEntriesSorter implements Runnable {

		private SORT_TYPE mSortType;
		private IAsyncTaskListener mListener;

		public AppEntriesSorter(SORT_TYPE sortType, IAsyncTaskListener listener) {
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
			((BaseRecord) getRecord()).sortAppEntries(mSortType);
			if (mListener != null) {
				mListener.onEnd(true, null, null);
			}
		}
	}

	/**
	 * @author maiyongshen
	 *
	 */
	protected class EntryIconLoadListener implements OnDrawableLoadedListener {
		private int mGroupPos;
		private int mChildPos;

		public EntryIconLoadListener(int groupPos, int childPos) {
			mGroupPos = groupPos;
			mChildPos = childPos;
		}

		@Override
		public void onDrawableLoaded(Drawable drawable) {
			if (drawable != null) {
				Message.obtain(mHandler, MSG_NOTIFY_ADAPTER_CHILD_ITEM_UPDATE_LISTENER, mGroupPos,
						mChildPos).sendToTarget();
			}
		}
	}

	/**
	 * Entry Holder
	 *
	 * @author GoBackup Dev Team
	 */
	private class EntryHolder {
		BaseEntry mEntry;
		int mGroupPos;
		int mChildPos;
	}

	/**
	 * @author GoBackup Dev Team
	 */
	public static interface OnAdapterItemUpdateListener {
		public void onAdapterGroupItemUpdate(BaseExpandableListAdapter adapter, int groupPos);

		public void onAdapterChildItemUpdate(BaseExpandableListAdapter adapter, int groupPos,
				int childPos);
	}

	protected void bindCheckbox(CheckBox checkBox, BaseEntry entry, int groupPos, int childPos) {
		checkBox.setOnCheckedChangeListener(null);
		checkBox.setChecked(entry.isSelected());
		EntryHolder holder = new EntryHolder();
		holder.mEntry = entry;
		holder.mGroupPos = groupPos;
		holder.mChildPos = childPos;
		checkBox.setTag(holder);
		checkBox.setOnCheckedChangeListener(mEntryCheckedChangeListener);
		if (!isEntrySelectable(entry)) {
			checkBox.setVisibility(View.GONE);
		} else {
			checkBox.setVisibility(View.VISIBLE);
		}
	}

	protected Drawable getEntryIcon(BaseEntry entry, int groupPos, int childPos) {
		if (entry != null) {
			return entry.getIcon(getContext(), new EntryIconLoadListener(groupPos, childPos));
		}
		return null;
	}

	protected long getEntrySpaceUsage(BaseEntry entry) {
		return entry.getSpaceUsage();
	}

	protected abstract void bindTitleExtraInfo(View view, BaseEntry entry);

	protected abstract void bindSummaryInfo(View view, BaseEntry entry);

	protected abstract void bindExtraSummaryInfo(View view, BaseEntry entry);

	protected abstract void bindMarkerView(View view, BaseEntry entry);

	protected abstract boolean isEntrySelectable(BaseEntry entry);

}
