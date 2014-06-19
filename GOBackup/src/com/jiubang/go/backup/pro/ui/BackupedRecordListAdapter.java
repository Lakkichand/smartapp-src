package com.jiubang.go.backup.pro.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.data.IRecord;
import com.jiubang.go.backup.pro.data.RestorableRecord;
import com.jiubang.go.backup.pro.model.BackupManager;
import com.jiubang.go.backup.pro.selfdef.ui.PinnedHeaderListView;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 备份记录列表adapter
 *
 * @author GoBackup Dev Team
 */
public class BackupedRecordListAdapter extends BaseRecordsListAdapter
		implements
			PinnedHeaderListView.PinnedHeaderAdapter {
	private static final String RECORD_DATE_FORMAT = "%tH:%tM";
	// private static final int MONTH_OF_YEAR = 12;
	private static final String PARTITION_KEY_FORMAT = "%tY-%tm";

	private static final int INITIAL_PARTITION_CAPACITY = 5;

	private static final int VIEW_TYPE_INVALID = -1;
	private static final int VIEW_TYPE_HEADER_VIEW = 0;
	private static final int VIEW_TYPE_NORMAL_VIEW = 1;

	private String[] mMonths = null;
	private int[] mDaysDrawable = new int[] { R.drawable.num_0, R.drawable.num_1, R.drawable.num_2,
			R.drawable.num_3, R.drawable.num_4, R.drawable.num_5, R.drawable.num_6,
			R.drawable.num_7, R.drawable.num_8, R.drawable.num_9 };
	// private List<IRecord> mRecordList;
	private Context mContext;
	// private Partition[] mPartitions;
	// private int mPartitionCount;
	private List<Partition> mPartitions;
	private boolean mDataValid;
	private int mCount;
	private boolean mNotificationsEnabled = true;

	// private boolean[] mHeaderVisibility;

	/**
	 * Partition
	 */
	private class Partition {
		private List<RecordItem> mItems;
		private boolean mHasHeader;
		private boolean mShowIfEmpty;
		private String mKey;

		public Partition(String key) {
			this(key, true, false);
		}

		public Partition(String key, boolean hasHeader, boolean showIfEmpty) {
			mItems = new ArrayList<RecordItem>();
			this.mHasHeader = hasHeader;
			this.mShowIfEmpty = showIfEmpty;
			if (key == null) {
				key = "";
			}
			this.mKey = key;
		}

		public boolean shouldShowIfEmpty() {
			return mShowIfEmpty;
		}

		public boolean hasHeader() {
			return mHasHeader;
		}

		public String getKey() {
			return mKey;
		}

		public String getDescription() {
			if (mItems != null && !mItems.isEmpty()) {
				Date date = mItems.get(0).mData.getDate();
				return new SimpleDateFormat("yyyy-MM").format(date);
			}
			return null;
		}

		public boolean isEmpty() {
			return mItems == null || mItems.size() == 0;
		}

		public int getCount() {
			int count = mItems.size();
			if (mHasHeader) {
				if (count != 0 || mShowIfEmpty) {
					count++;
				}
			}
			return count;
		}

		public int getItemCount() {
			if (mItems == null) {
				return 0;
			}
			return mItems.size();
		}

		public RecordItem getItem(int position) {
			if (position < 0 || position >= mItems.size()) {
				return null;
			}
			return mItems.get(position);
		}

		public void clear() {
			mItems.clear();
		}

		public synchronized RecordItem remove(int position) {
			if (position < 0 || position > mItems.size()) {
				return null;
			}
			RecordItem removedItem = mItems.remove(position);
			if (removedItem != null) {
				if (removedItem.mPrev != null) {
					removedItem.mPrev.mNext = removedItem.mNext;
				}
				if (removedItem.mNext != null) {
					removedItem.mNext.mPrev = removedItem.mPrev;
				}
			}
			return removedItem;
		}

		public synchronized boolean addItem(RecordItem item) {
			if (item == null) {
				return false;
			}
			if (!item.getKey().equals(mKey)) {
				return false;
			}
			final Calendar calendar = Calendar.getInstance();
			final Date date = item.mData.getDate();
			if (date == null) {
				return false;
			}
			calendar.setTime(date);
			final int day = calendar.get(Calendar.DAY_OF_MONTH);
			final int count = mItems.size();
			for (int i = 0; i < count; i++) {
				RecordItem other = mItems.get(i);
				final Date otherDate = other.mData.getDate();
				calendar.setTime(otherDate);
				final int otherDateDay = calendar.get(Calendar.DAY_OF_MONTH);
				if (day > otherDateDay) {
					other.mPrev = item;
					item.mNext = other;
					mItems.add(i, item);
					return true;
				} else if (day < otherDateDay) {
					continue;
				} else {
					if (date.compareTo(otherDate) >= 0) {
						item.mPrev = null;
						item.mNext = other;
						other.mPrev = item;
						mItems.add(i, item);
						return true;
					} else {
						int index = i + 1;
						RecordItem lastItem = other;
						RecordItem nextItem = null;
						for (other = other.mNext; other != null; other = other.mNext) {
							Date nextDate = other.mData.getDate();
							if (date.compareTo(nextDate) >= 0) {
								nextItem = other;
								break;
							}
							lastItem = other;
							index++;
						}
						item.mPrev = lastItem;
						item.mNext = nextItem;
						if (lastItem != null) {
							lastItem.mNext = item;
						}
						if (nextItem != null) {
							nextItem.mPrev = item;
						}
						mItems.add(index, item);
						return true;
					}
				}
			}
			mItems.add(item);
			return true;
		}

		// 是否是特殊分组
		// 目前将含有整合记录项的分组识为特殊分组
		public boolean isSpecial() {
			boolean special = false;
			RecordItem firstItem = getItem(0);
			if (firstItem != null) {
				RestorableRecord record = (RestorableRecord) firstItem.mData;
				special = record.isSmartMergedRecord();
				if (!special) {
					special = record.isScheduleRecord();
				}
				return special;
			}
			return false;
		}
	}

	/**
	 * RecordItem
	 *
	 * @author GoBackup Dev Team
	 */
	public static class RecordItem {
		IRecord mData;
		RecordItem mPrev;
		RecordItem mNext;
		boolean mIsSelected;

		public RecordItem(IRecord record) {
			mData = record;
			mPrev = null;
			mNext = null;
			mIsSelected = false;
		}

		public void setSelected(boolean selected) {
			mIsSelected = selected;
		}

		public boolean isSelected() {
			return mIsSelected;
		}

		public String getKey() {
			if (mData == null) {
				return "";
			}
			// 整合备份记录的key是特殊的
			if (mData instanceof RestorableRecord) {
				RestorableRecord restorableRecord = (RestorableRecord) mData;
				if (restorableRecord.isSmartMergedRecord()) {
					return BackupManager.SMART_MERGED_BACKUP;
				}
				if (restorableRecord.isScheduleRecord()) {
					return BackupManager.SCHEDULE_BACKUP;
				}
			}

			Date date = mData.getDate();
			if (date != null) {
				String key = String.format(PARTITION_KEY_FORMAT, date, date);
				return key;
			}
			return "";
		}
	}

	public BackupedRecordListAdapter(Context context) {
		super(context);
		mContext = context;
		// mRecordList = new ArrayList<IRecord>();
		mMonths = context.getResources().getStringArray(R.array.months);
		mPartitions = new ArrayList<Partition>(INITIAL_PARTITION_CAPACITY);
	}

	// 按月份分组
	/*
	 * private void dividePartition() { mPartitions = new
	 * Partition[MONTH_OF_YEAR]; final List<IRecord> recordList = mRecordList;
	 * if (recordList == null || recordList.size() <= 0) { return; } final
	 * Calendar calendar = Calendar.getInstance(); for (IRecord record :
	 * recordList) { final Date recordDate = record.getDate();
	 * calendar.setTime(recordDate); final int month =
	 * calendar.get(Calendar.MONTH); if (month < MONTH_OF_YEAR) {
	 * mPartitions[month].addItem(new RecordItem(record)); } } }
	 */

	@Override
	public void notifyDataSetChanged() {
		if (!mNotificationsEnabled) {
			return;
		}
		ensureDataValid();
		super.notifyDataSetChanged();
	}

	private void invalidate() {
		mDataValid = false;
	}

	/*
	 * private void initPartitions() { mPartitions = new
	 * Partition[MONTH_OF_YEAR]; for (int i = 0; i < MONTH_OF_YEAR; i++) {
	 * mPartitions[i] = new Partition(); } }
	 */

	private int findPartitionWithKey(String key) {
		if (key == null) {
			throw new IllegalArgumentException("key cannot be null");
		}
		final int count = mPartitions.size();
		for (int i = 0; i < count; i++) {
			if (key.equals(mPartitions.get(i).getKey())) {
				return i;
			}
		}
		return -1;
	}

	private void addPartition(String key, boolean hasHeader, boolean showIfEmpty) {
		addPartition(new Partition(key, hasHeader, showIfEmpty));
	}

	private void addPartition(Partition newPartition) {
		final String key = newPartition.getKey();
		if (key == null) {
			throw new IllegalArgumentException("partition key cannot be null");
		}
		final int partitionCount = getPartitionCount();
		int i;
		for (i = 0; i < partitionCount; i++) {
			final Partition partition = mPartitions.get(i);
			int result = 0;
			// 整合记录的Key为最大，放在最前面
			if (key.equals(BackupManager.SMART_MERGED_BACKUP)) {
				result = 1;
			} else {
				result = key.compareTo(partition.getKey());
			}
			if (result > 0) {
				mPartitions.add(i, newPartition);
				break;
			} else if (result == 0) {
				return;
			} else if (result < 0) {
				continue;
			}
		}
		if (i >= partitionCount) {
			mPartitions.add(newPartition);
		}
		invalidate();
		notifyDataSetChanged();
	}

	private void removePartition(int index) {
		// mPartitions[index].clear();
		// System.arraycopy(mPartitions, index + 1, mPartitions, index,
		// mPartitionCount - index - 1);
		// mPartitionCount--;
		mPartitions.get(index).clear();
		mPartitions.remove(index);
		invalidate();
		notifyDataSetChanged();
	}

	private void ensureDataValid() {
		if (mDataValid) {
			return;
		}
		// dividePartition();
		mCount = 0;
		for (Partition partition : mPartitions) {
			mCount += partition.getCount();
		}
		mDataValid = true;
	}

	public View newHeaderView(Context context, ViewGroup parent, int partitionIndex) {
		final LayoutInflater inflater = LayoutInflater.from(context);
		// Partition partition = getPartition(partitionIndex);
		// if (partition != null && partition.isSpecial()) {
		// return
		// inflater.inflate(R.layout.layout_smart_merged_record_header_view,
		// parent, false);
		// }
		return inflater.inflate(R.layout.layout_record_list_header_view, parent, false);
	}

	protected void bindHeaderView(View view, Context context, int partition) {
		final Partition part = getPartition(partition);
		if (part == null) {
			return;
		}
		// 含整合备份的分组不需要更新headerView
		if (part.isSpecial()) {
			view.findViewById(R.id.title_smart_merge_record).setVisibility(View.VISIBLE);
			view.findViewById(R.id.partition_info).setVisibility(View.INVISIBLE);
		} else {
			view.findViewById(R.id.title_smart_merge_record).setVisibility(View.INVISIBLE);
			view.findViewById(R.id.partition_info).setVisibility(View.VISIBLE);
		}

		TextView partitionDesc = (TextView) view.findViewById(R.id.partition_desc);
		if (partitionDesc != null) {
			partitionDesc.setText(part.getDescription());
		}

		TextView partitionItemCount = (TextView) view.findViewById(R.id.partiton_item_count);
		if (partitionItemCount != null) {
			partitionItemCount.setText(String.valueOf(part.getItemCount()));
		}
	}

	@Override
	protected View newView(Context context, int position, ViewGroup parent) {
		final LayoutInflater inflater = LayoutInflater.from(context);
		int viewType = getItemViewType(position);
		if (viewType == VIEW_TYPE_HEADER_VIEW) {
			return newHeaderView(context, parent, getPartitionForPosition(position));
		} else if (viewType == VIEW_TYPE_NORMAL_VIEW) {
			return inflater.inflate(R.layout.layout_backuped_record_info, parent, false);
		}
		return null;
	}

	private void bindDateView(View dateView, View parent, IRecord record, int position) {
		final TextView monthView = (TextView) dateView.findViewById(R.id.date_month);
		final ImageView dayHighDigit = (ImageView) dateView.findViewById(R.id.date_day_high);
		final ImageView dayLowDigit = (ImageView) dateView.findViewById(R.id.date_day_low);
		final TextView timeView = (TextView) parent.findViewById(R.id.backup_time);
		final Date recordDate = record.getDate();
		final int mColorValue = 0xcc535353;
		if (recordDate != null) {
			final Calendar calendar = Calendar.getInstance();
			calendar.setTime(recordDate);
			if (monthView != null) {
				int month = calendar.get(Calendar.MONTH);
				monthView.setText(mMonths[month]);
				Partition partition = getPartition(getPartitionForPosition(position));
				// 含整合备份的记录的日历使用绿色背景
				if (partition != null && partition.isSpecial()) {
					monthView.setBackgroundResource(R.drawable.calendar_month_bg_highlight);
					monthView.setTextColor(0xffffffff);
				} else {
					monthView.setBackgroundResource(R.drawable.calendar_month_bg_normal);
					monthView.setTextColor(mColorValue);
				}
				// if (getPartitionForPosition(position) == 0 &&
				// getOffsetInPartition(position) == 0) {
				// monthView.setBackgroundResource(R.drawable.calendar_month_bg_highlight);
				// monthView.setTextColor(0xffffffff);
				// }
			}

			if (dayHighDigit != null && dayLowDigit != null) {
				final int m10 = 10;
				int day = calendar.get(Calendar.DAY_OF_MONTH);
				dayHighDigit.setImageResource(mDaysDrawable[day / m10]);
				dayLowDigit.setImageResource(mDaysDrawable[day % m10]);
			}

			if (timeView != null) {
				String hourAndMinute = String.format(RECORD_DATE_FORMAT, recordDate, recordDate);
				timeView.setText(hourAndMinute);
			}
		}
	}

	private void bindDetailItemView(View view, int resId, int count) {
		if (view == null) {
			return;
		}
		if (count <= 0) {
			view.setVisibility(View.GONE);
			return;
		}
		view.setVisibility(View.VISIBLE);
		final ImageView drawable = (ImageView) view.findViewById(R.id.drawable);
		if (drawable.getDrawable() == null) {
			drawable.setImageResource(resId);
		}
		final TextView num = (TextView) view.findViewById(R.id.numbers);
		num.setText(String.valueOf(count));
	}

	private void bindRecordDetailView(IRecord record, View parent) {
		if (!(record instanceof RestorableRecord)) {
			return;
		}
		final RestorableRecord restorableRecord = (RestorableRecord) record;
		ViewGroup recordDetailList = (ViewGroup) parent.findViewById(R.id.record_detail_item_list);
		if (recordDetailList == null) {
			return;
		}
		final ViewGroup contactsDetail = (ViewGroup) recordDetailList
				.findViewById(R.id.contacts_detail);
		if (contactsDetail != null) {
			final int count = restorableRecord.getContactsCount();
			bindDetailItemView(contactsDetail, R.drawable.item_contacts, count);
		}

		final ViewGroup smsDetail = (ViewGroup) recordDetailList.findViewById(R.id.sms_detail);
		if (smsDetail != null) {
			final int count = restorableRecord.getMessagesCount();
			bindDetailItemView(smsDetail, R.drawable.item_sms, count);
		}

		final ViewGroup mmsDetail = (ViewGroup) recordDetailList.findViewById(R.id.mms_detail);
		if (mmsDetail != null) {
			final int count = restorableRecord.getMMSCount();
			bindDetailItemView(mmsDetail, R.drawable.item_mms, count);
		}

		final ViewGroup callLogsDetail = (ViewGroup) recordDetailList
				.findViewById(R.id.call_log_detail);
		if (callLogsDetail != null) {
			final int count = restorableRecord.getCallLogsCount();
			bindDetailItemView(callLogsDetail, R.drawable.item_call_log, count);
		}

		final ViewGroup wifiDetail = (ViewGroup) recordDetailList.findViewById(R.id.wifi_detail);
		if (wifiDetail != null) {
			bindDetailItemView(wifiDetail, R.drawable.item_wifi, restorableRecord.hasWifiEntry()
					? 1
					: 0);
		}

		final ViewGroup appsDetail = (ViewGroup) recordDetailList.findViewById(R.id.apps_detail);
		if (appsDetail != null) {
			final int count = restorableRecord.getUserAppEntryCount();
			bindDetailItemView(appsDetail, R.drawable.item_apps, count);
		}

		final ViewGroup settingDetail = (ViewGroup) recordDetailList
				.findViewById(R.id.setting_detail);
		if (settingDetail != null) {
			bindDetailItemView(settingDetail, R.drawable.item_golauncher_setting,
					restorableRecord.hasGoLauncherSettingEntry() ? 1 : 0);
		}

		final ViewGroup dictionaryDetail = (ViewGroup) recordDetailList
				.findViewById(R.id.dictionary_detail);
		if (dictionaryDetail != null) {
			bindDetailItemView(dictionaryDetail, R.drawable.item_user_dictionary,
					restorableRecord.getUserDictionaryWordCount());
		}

		final ViewGroup launcherSettingDetail = (ViewGroup) recordDetailList
				.findViewById(R.id.launcher_setting_detail);
		if (launcherSettingDetail != null) {
			bindDetailItemView(launcherSettingDetail, R.drawable.item_launcher_setting,
					restorableRecord.hasLauncherDataEntry() ? 1 : 0);
		}

		final ViewGroup ringtoneDetail = (ViewGroup) recordDetailList
				.findViewById(R.id.ringtone_detail);
		if (ringtoneDetail != null) {
			bindDetailItemView(ringtoneDetail, R.drawable.item_ringtone,
					restorableRecord.hasRingtoneEntry() ? 1 : 0);
		}

		final ViewGroup calendarDetail = (ViewGroup) recordDetailList
				.findViewById(R.id.calendar_detail);
		if (calendarDetail != null) {
			bindDetailItemView(calendarDetail, R.drawable.item_calendar,
					restorableRecord.getCalendarEventCount());
		}

		final ViewGroup bookmarkDetail = (ViewGroup) recordDetailList
				.findViewById(R.id.bookmark_detail);
		if (bookmarkDetail != null) {
			bindDetailItemView(bookmarkDetail, R.drawable.item_bookmark,
					restorableRecord.getBookMarkCount());
		}

		final ViewGroup ellipsis = (ViewGroup) recordDetailList.findViewById(R.id.ellipsis);
		ellipsis.setVisibility(View.GONE);

		int firstVisibleChild = -1;
		int lastVisibleChild = -1;
		int visibleChildCount = 0;
		int childCount = recordDetailList.getChildCount();
		// 省略号不计入
		for (int i = 0; i < childCount - 1; i++) {
			View child = recordDetailList.getChildAt(i);
			if (child.getVisibility() == View.VISIBLE) {
				if (firstVisibleChild == -1) {
					firstVisibleChild = i;
				}
				lastVisibleChild = i;
				visibleChildCount++;
			}
		}
		if (firstVisibleChild != -1) {
			// 第一个可见的view的margin设置为0
			View firstVisibleChildView = recordDetailList.getChildAt(firstVisibleChild);
			MarginLayoutParams lp = (MarginLayoutParams) firstVisibleChildView.getLayoutParams();
			lp.leftMargin = 0;
			firstVisibleChildView.setLayoutParams(lp);
			// 后面的view的margin为正常值
			for (int i = firstVisibleChild + 1; i < childCount; i++) {
				final View view = recordDetailList.getChildAt(i);
				lp = (MarginLayoutParams) view.getLayoutParams();
				lp.leftMargin = (int) mContext.getResources().getDimension(
						R.dimen.record_item_margin);
				view.setLayoutParams(lp);
			}
		}
		// 最多展示5个有效child view（不包括省略号）,超出展示省略号
		final int mMAXCHILDVIEWCOUNT = 5;
		if (visibleChildCount > mMAXCHILDVIEWCOUNT) {
			// 显示省略号
			ellipsis.setVisibility(View.VISIBLE);
			final ImageView drawable = (ImageView) ellipsis.findViewById(R.id.drawable);
			if (drawable.getDrawable() == null) {
				drawable.setImageResource(R.drawable.item_ellipsis);
			}
			ellipsis.findViewById(R.id.numbers).setVisibility(View.GONE);
			MarginLayoutParams lp = (MarginLayoutParams) ellipsis.getLayoutParams();
			lp.leftMargin = (int) mContext.getResources().getDimension(
					R.dimen.record_item_ellipsis_margin_left);
			ellipsis.setLayoutParams(lp);
			// 多出的child view隐藏
			for (int i = lastVisibleChild, j = 0; i >= 0
					&& j < visibleChildCount - mMAXCHILDVIEWCOUNT; i--) {
				View view = recordDetailList.getChildAt(i);
				if (view == null || view.getVisibility() != View.VISIBLE) {
					continue;
				}
				view.setVisibility(View.GONE);
				j++;
			}
		}
	}

	@Override
	protected void bindView(View view, Context context, int position) {
		int viewType = getItemViewType(position);
		if (viewType == VIEW_TYPE_HEADER_VIEW) {
			bindHeaderView(view, context, getPartitionForPosition(position));
			return;
		}

		final RecordItem recordItem = getRecordItem(position);
		if (recordItem == null) {
			return;
		}
		final IRecord record = recordItem.mData;
		// 日期
		final ViewGroup dateView = (ViewGroup) view.findViewById(R.id.date);
		if (dateView != null) {
			bindDateView(dateView, view, record, position);
			if (recordItem.mPrev != null) {
				dateView.setVisibility(View.INVISIBLE);
			} else {
				dateView.setVisibility(View.VISIBLE);
			}
		}

		final TextView recordSpaceUsage = (TextView) view.findViewById(R.id.space_usage);
		if (recordSpaceUsage != null) {
			recordSpaceUsage.setText(Util.formatFileSize(record.getSpaceUsage()));
		}

		bindRecordDetailView(record, view);

		final CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
		if (checkBox != null) {
			checkBox.setVisibility(View.GONE);
		}
	}

	/*
	 * @Override public View getView(int position, View convertView, ViewGroup
	 * parent) { final IRecord record = ((IRecord) getItem(position)); if
	 * (record == null) { return null; } return super.getView(position,
	 * convertView, parent); }
	 */

	public int getPartitionCount() {
		ensureDataValid();
		return mPartitions.size();
	}

	@Override
	public int getViewTypeCount() {
		return super.getViewTypeCount() + 1;
	}

	@Override
	public int getItemViewType(int position) {
		ensureDataValid();
		int partition = getPartitionForPosition(position);
		if (partition >= 0) {
			int offset = getOffsetInPartition(position);
			if (offset < 0) {
				return VIEW_TYPE_HEADER_VIEW;
			} else {
				return VIEW_TYPE_NORMAL_VIEW;
			}
		}
		return VIEW_TYPE_INVALID;
	}

	public Partition getPartition(int partition) {
		if (partition < 0 || partition >= getPartitionCount()) {
			throw new ArrayIndexOutOfBoundsException(partition);
		}
		return mPartitions.get(partition);
	}

	public int getPositionForPartition(int partition) {
		ensureDataValid();
		if (partition < 0 || partition >= getPartitionCount()) {
			return -1;
		}
		int position = 0;
		for (int i = 0; i < partition; i++) {
			position += mPartitions.get(i).getCount();
		}
		return position;
	}

	public int getPartitionForPosition(int position) {
		if (position < 0) {
			throw new ArrayIndexOutOfBoundsException(position);
		}
		ensureDataValid();
		int start = 0;
		final int partitionCount = getPartitionCount();
		for (int i = 0; i < partitionCount; i++) {
			int end = start + mPartitions.get(i).getCount();
			if (position >= start && position < end) {
				return i;
			}
			start = end;
		}
		return -1;
	}

	public int getOffsetInPartition(int position) {
		if (position < 0) {
			throw new ArrayIndexOutOfBoundsException(position);
		}
		ensureDataValid();
		int start = 0;
		final int partitionCount = getPartitionCount();
		for (int i = 0; i < partitionCount; i++) {
			final Partition partition = mPartitions.get(i);
			int end = start + partition.getCount();
			if (position >= start && position < end) {
				int offset = position - start;
				if (partition.hasHeader()) {
					offset--;
				}
				return offset;
			}
			start = end;
		}
		return -1;
	}

	@Override
	public void clear() {
		if (mPartitions != null) {
			for (Partition partition : mPartitions) {
				partition.clear();
			}
			mPartitions.clear();
		}
		invalidate();
		notifyDataSetChanged();
	}

	@Override
	public synchronized IRecord remove(int position) {
		if (position < 0 || position >= getCount()) {
			return null;
		}
		int destPartition = getPartitionForPosition(position);
		if (destPartition < 0) {
			return null;
		}
		int offset = getOffsetInPartition(position);
		if (offset < 0) {
			return null;
		}
		final Partition partition = mPartitions.get(destPartition);
		// TODO 目前是不允许空的Partition存在
		RecordItem removedItem = partition.remove(offset);
		if (partition.isEmpty()) {
			removePartition(destPartition);
		}
		if (removedItem != null) {
			invalidate();
			notifyDataSetChanged();
			return removedItem.mData;
		}
		return null;
	}

	@Override
	public synchronized boolean add(IRecord record) {
		if (record == null) {
			return false;
		}
		RecordItem recordItem = new RecordItem(record);
		String recordItemKey = recordItem.getKey();
		int partitionIndex = findPartitionWithKey(recordItemKey);
		if (partitionIndex >= 0) {
			mPartitions.get(partitionIndex).addItem(recordItem);
		} else {
			Partition newPartition = new Partition(recordItemKey, true, false);
			newPartition.addItem(recordItem);
			addPartition(newPartition);
		}
		invalidate();
		notifyDataSetChanged();

		return true;
	}

	@Override
	public synchronized boolean addAll(List<IRecord> records) {
		if (records == null) {
			return false;
		}
		if (records.size() == 0) {
			return true;
		}
		setNotificationsEnabled(false);
		for (IRecord record : records) {
			add(record);
		}
		setNotificationsEnabled(true);
		invalidate();
		notifyDataSetChanged();
		return true;
	}

	@Override
	public boolean isEnabled(int position) {
		int viewType = getItemViewType(position);
		if (viewType == VIEW_TYPE_NORMAL_VIEW) {
			return true;
		}
		return false;
	}

	@Override
	public int getCount() {
		ensureDataValid();
		return mCount;
	}

	public RecordItem getRecordItem(int position) {
		ensureDataValid();
		if (position < 0 || position >= getCount()) {
			return null;
		}
		int destPartition = getPartitionForPosition(position);
		if (destPartition < 0) {
			return null;
		}
		int offset = getOffsetInPartition(position);
		if (offset < 0) {
			return null;
		}
		return mPartitions.get(destPartition).getItem(offset);
	}

	public int getAllValidRecordItemCount() {
		int count = 0;
		for (Partition partition : mPartitions) {
			count += partition.getItemCount();
		}
		return count;
	}

	@Override
	public Object getItem(int position) {
		RecordItem recordItem = getRecordItem(position);
		if (recordItem != null) {
			return recordItem.mData;
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		ensureDataValid();
		IRecord item = (IRecord) getItem(position);
		if (item != null) {
			return item.getId();
		}
		return -1;
	}

	public void setNotificationsEnabled(boolean flag) {
		mNotificationsEnabled = flag;
	}

	public boolean isPartitionEmpty(int partition) {
		if (partition < 0 || partition >= getPartitionCount()) {
			return false;
		}
		ensureDataValid();
		return mPartitions.get(partition).isEmpty();
	}

	public boolean hasHeader(int partition) {
		return mPartitions.get(partition).hasHeader();
	}

	protected boolean isPinnedPartitionHeaderVisible(int partition) {
		return hasHeader(partition) && !isPartitionEmpty(partition);
	}

	/*
	 * @Override public View getPinnedHeaderView(int viewIndex, View
	 * convertView, ViewGroup parent) { View view = null; if (convertView ==
	 * null) { view = newHeaderView(mContext, viewIndex, parent); } else { view
	 * = convertView; } view.setFocusable(false); bindHeaderView(view, mContext,
	 * viewIndex); return view; }
	 */

	@Override
	public int getPinnedHeaderState(int position) {
		if (getCount() == 0) {
			return PINNED_HEADER_GONE;
		}
		int currentPartition = getPartitionForPosition(position);
		int nextPartitionPosition = getPositionForPartition(currentPartition + 1);
		if (nextPartitionPosition != -1 && position == nextPartitionPosition - 1) {
			return PINNED_HEADER_PUSHED_UP;
		}
		return PINNED_HEADER_VISIBLE;
	}

	@Override
	public void configurePinnedHeader(View header, int position, int alpha) {
		int partition = getPartitionForPosition(position);
		if (partition >= 0) {
			bindHeaderView(header, mContext, partition);
		}
	}

	@Override
	public View getHeaderView(ViewGroup parent, int position) {
		int partition = getPartitionForPosition(position);
		return newHeaderView(mContext, parent, partition);
	}

	/*
	 * public void onScroll(AbsListView view, int firstVisibleItem, int
	 * visibleItemCount, int totalItemCount) { if (view instanceof
	 * PinnedHeaderListView) {
	 * ((PinnedHeaderListView)view).configureHeaderView(firstVisibleItem); } }
	 */

}
