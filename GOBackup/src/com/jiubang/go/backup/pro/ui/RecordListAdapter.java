package com.jiubang.go.backup.pro.ui;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.PreferenceManager;
import com.jiubang.go.backup.pro.data.IRecord;
import com.jiubang.go.backup.pro.data.RestorableRecord;
import com.jiubang.go.backup.pro.model.BackupManager;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 2.0 新UI 备份列表Adapter
 *
 * @author maiyongshen
 */

public class RecordListAdapter extends BaseAdapter {
	private static final String NORMAL_RECORD_DATE_FORMAT = "%tm-%td";
	private static final String NORMAL_RECORD_TIME_FORMAT = "%tH:%tM";
	private static final String SPECIAL_RECORD_DATE_FORMAT = "%tm-%td %tH:%tM";

	private static final int VIEW_TYPE_SPECIAL_RECORD = 0;
	private static final int VIEW_TYPE_NORMAL_RECORD = 1;
	private static final int VIEW_TYPE_RECORD_HEADER = 2;

	private LayoutInflater mInflater;
	private Context mContext;
	private List<IRecord> mRecords;
	private boolean mHasNormalRecord = false;
	private int mFirstNormalRecordPos = -1;

	public RecordListAdapter(Context context, List<IRecord> records) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mRecords = records;
		mHasNormalRecord = hasNormalRecord();
		
		final int count = getCount();
		for (int i = 0; i < count; i++) {
			final IRecord record = (IRecord) getItem(i);
			if (record != null && !isSpecialRecord(record)) {
				mFirstNormalRecordPos = i;
				break;
			}
		}
	}

	@Override
	public int getCount() {
		final int recordCount = mRecords != null ? mRecords.size() : 0;
		return mHasNormalRecord ? recordCount + 1 : recordCount;
	}

	@Override
	public Object getItem(int position) {
		int recordCount = mRecords.size();
		if (recordCount == 0) {
			return null;
		}
		if (position >= recordCount && position - 1 < recordCount) {
			return mRecords.get(position - 1);
		}
		IRecord record = mRecords.get(position);
		if (isSpecialRecord(record)) {
			return record;
		}
		IRecord prev = null;
		IRecord next = null;
		if (position - 1 >= 0) {
			prev = mRecords.get(position - 1);
		}
		if (position + 1 < recordCount) {
			next = mRecords.get(position + 1);
		}
		if ((prev == null || isSpecialRecord(prev)) && !isSpecialRecord(next)) {
			// 分隔线
			return null;
		}
		// 普通备份
		return mRecords.get(position - 1);
	}

	@Override
	public long getItemId(int position) {
		IRecord record = (IRecord) getItem(position);
		if (record == null) {
			// 分隔线
			return -1;
		}
		return record.getId();
	}

	private View newView(int position, ViewGroup parent) {
		int viewType = getItemViewType(position);
		if (viewType == VIEW_TYPE_RECORD_HEADER) {
			return mInflater.inflate(R.layout.record_list_header, parent, false);
		}
		if (viewType == VIEW_TYPE_SPECIAL_RECORD) {
			return mInflater.inflate(R.layout.special_record_item, parent, false);
		}
		return mInflater.inflate(R.layout.normal_record_item, parent, false);
	}

	@Override
	public int getViewTypeCount() {
		return VIEW_TYPE_RECORD_HEADER - VIEW_TYPE_SPECIAL_RECORD + 1;
	}

	@Override
	public int getItemViewType(int position) {
		IRecord record = (IRecord) getItem(position);
		if (record == null) {
			return VIEW_TYPE_RECORD_HEADER;
		}
		if (isSpecialRecord(record)) {
			return VIEW_TYPE_SPECIAL_RECORD;
		}
		return VIEW_TYPE_NORMAL_RECORD;
	}

	@Override
	public boolean isEnabled(int position) {
		if (getItemViewType(position) == VIEW_TYPE_RECORD_HEADER) {
			return false;
		}
		return true;
	}

	private void bindListViewHeader(View view, int position) {
		TextView title = (TextView) view.findViewById(R.id.title);
		title.setText(R.string.normal_backup);
		final int mColor1 = 0xffff0000;
		final int mColor2 = 0xff818181;

		TextView extraInfo = (TextView) view.findViewById(R.id.extra_info);
		int normalRecordCount = getNormalRecordCount();
		extraInfo.setText(mContext.getString(R.string.parenthesized_msg,
				String.valueOf(normalRecordCount)));
		if (normalRecordCount >= BackupManager.getInstance().getMaxBackupCount()) {
			extraInfo.setTextColor(mColor1);
		} else {
			extraInfo.setTextColor(mColor2);
		}
		view.findViewById(R.id.checkbox).setVisibility(View.GONE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = newView(position, parent);
		}

		int viewType = getItemViewType(position);
		if (viewType == VIEW_TYPE_RECORD_HEADER) {
			bindListViewHeader(convertView, position);
			return convertView;
		}

		IRecord record = (IRecord) getItem(position);
		if (record == null) {
			return null;
		}

		if (viewType == VIEW_TYPE_SPECIAL_RECORD) {
			View recordDetailView = convertView.findViewById(R.id.record_detail);
			Rect viewPadding = getViewPadding(recordDetailView);
			if (isMergedRecord(record)) {
				recordDetailView.setBackgroundResource(R.drawable.merged_record_item_bg);
			} else if (isScheduleRecord(record)) {
				recordDetailView.setBackgroundResource(R.drawable.schedule_record_bg);
			}
			recordDetailView.setPadding(viewPadding.left, viewPadding.top, viewPadding.right,
					viewPadding.bottom);
		}

		StretchRecordItemView recordDetail = (StretchRecordItemView) convertView
				.findViewById(R.id.record_content_info);
		bindRecordDetailView(recordDetail, record);

		TextView title = (TextView) convertView.findViewById(R.id.title);
		if (isMergedRecord(record)) {
			title.setText(R.string.merged_backup);
		} else if (isScheduleRecord(record)) {
			title.setText(R.string.schedule_backup);
		} else {
			title.setText(R.string.normal_backup);
		}

		TextView recordSize = (TextView) convertView.findViewById(R.id.size_info);
		String size = Util.formatFileSize(record.getSpaceUsage());
		recordSize.setText(mContext.getString(R.string.parenthesized_msg, size));

		TextView recordDate = (TextView) convertView.findViewById(R.id.day_info);
		TextView recordTime = (TextView) convertView.findViewById(R.id.time_info);
		Date date = record.getDate();
		if (recordDate != null) {
			recordDate.setText(String.format(NORMAL_RECORD_DATE_FORMAT, date, date));
		}
		if (recordTime != null) {
			if (viewType == VIEW_TYPE_SPECIAL_RECORD) {
				recordTime.setText(String
						.format(SPECIAL_RECORD_DATE_FORMAT, date, date, date, date));
			} else {
				recordTime.setText(String.format(NORMAL_RECORD_TIME_FORMAT, date, date));
			}
		}

		convertView.findViewById(R.id.checkbox).setVisibility(View.GONE);
		
		final View newFeatureTag = convertView.findViewById(R.id.new_feature_tag);
		if (newFeatureTag != null) {
			if (!PreferenceManager.getInstance().getBoolean(mContext,
					PreferenceManager.KEY_RESTORE_BACKUP_NEW_FEATURE, false)
					&& position == mFirstNormalRecordPos) {
				newFeatureTag.setVisibility(View.VISIBLE);
			} else {
				newFeatureTag.setVisibility(View.GONE);
			}
		}

		return convertView;
	}

	public static void bindRecordDetailView(StretchRecordItemView parent, IRecord record) {
		if (parent == null || !(record instanceof RestorableRecord)) {
			return;
		}
//		parent.removeAllViewsInLayout();
		parent.removeAllViews();
		final RestorableRecord restorableRecord = (RestorableRecord) record;

		final int contactsCount = restorableRecord.getContactsCount();
		if (contactsCount > 0) {
			parent.addChildItem(R.drawable.item_contacts, String.valueOf(contactsCount));
		}

		final int smsCount = restorableRecord.getMessagesCount();
		if (smsCount > 0) {
			parent.addChildItem(R.drawable.item_sms, String.valueOf(smsCount));
		}

		final int mmsCount = restorableRecord.getMMSCount();
		if (mmsCount > 0) {
			parent.addChildItem(R.drawable.item_mms, String.valueOf(mmsCount));
		}

		final int callLogCount = restorableRecord.getCallLogsCount();
		if (callLogCount > 0) {
			parent.addChildItem(R.drawable.item_call_log, String.valueOf(callLogCount));
		}
		
		final int bookMarkCount = restorableRecord.getBookMarkCount();
		if (bookMarkCount > 0) {
			parent.addChildItem(R.drawable.item_bookmark, String.valueOf(bookMarkCount));
		}
		
		if (restorableRecord.hasWifiEntry()) {
			parent.addChildItem(R.drawable.item_wifi, String.valueOf(1));
		}

		final int appCount = restorableRecord.getUserAppEntryCount();
		if (appCount > 0) {
			parent.addChildItem(R.drawable.item_apps, String.valueOf(appCount));
		}

		if (restorableRecord.hasGoLauncherSettingEntry()) {
			parent.addChildItem(R.drawable.item_golauncher_setting, String.valueOf(1));
		}

		final int wordCount = restorableRecord.getUserDictionaryWordCount();
		if (wordCount > 0) {
			parent.addChildItem(R.drawable.item_user_dictionary, String.valueOf(wordCount));
		}

		if (restorableRecord.hasLauncherDataEntry()) {
			parent.addChildItem(R.drawable.item_launcher_setting, String.valueOf(1));
		}

		if (restorableRecord.hasWallpaperEntry()) {
			parent.addChildItem(R.drawable.item_wallpaper, String.valueOf(1));
		}

		final int ringtoneCount = restorableRecord.getRingtoneCount();
		if (restorableRecord.hasRingtoneEntry()) {
			parent.addChildItem(R.drawable.item_ringtone, String.valueOf(ringtoneCount));
		}

		final int calendarEventCount = restorableRecord.getCalendarEventCount();
		if (restorableRecord.hasCalendarEntry()) {
			parent.addChildItem(R.drawable.item_calendar, String.valueOf(calendarEventCount));
		}
	}

	public static boolean isSpecialRecord(IRecord record) {
		return isMergedRecord(record) || isScheduleRecord(record);
	}

	public static boolean isMergedRecord(IRecord record) {
		return BackupManager.isMergedRecord((RestorableRecord) record);
	}

	public static boolean isScheduleRecord(IRecord record) {
		return BackupManager.isScheduleRecord((RestorableRecord) record);
	}

	private boolean hasNormalRecord() {
		int recordCount = mRecords.size();
		for (int i = 0; i < recordCount; i++) {
			final IRecord record = mRecords.get(i);
			if (!isMergedRecord(record) && !isScheduleRecord(record)) {
				return true;
			}
		}
		return false;
	}

	private int getNormalRecordCount() {
		int count = 0;
		int recordCount = mRecords.size();
		for (int i = 0; i < recordCount; i++) {
			final IRecord record = mRecords.get(i);
			if (!isMergedRecord(record) && !isScheduleRecord(record)) {
				count++;
			}
		}
		return count;
	}

	private Rect getViewPadding(View view) {
		if (view == null) {
			return null;
		}
		return new Rect(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(),
				view.getPaddingRight());
	}

	public synchronized void remove(IRecord record) {
		if (record == null) {
			return;
		}
		mRecords.remove(record);
		mHasNormalRecord = hasNormalRecord();
	}

	public synchronized void remove(long recordId) {
		for (Iterator<IRecord> it = mRecords.iterator(); it.hasNext();) {
			IRecord record = it.next();
			if (record.getId() == recordId) {
				it.remove();
				mHasNormalRecord = hasNormalRecord();
				return;
			}
		}
	}

}
