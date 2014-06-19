package com.jiubang.go.backup.pro.ui;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.data.AppBackupEntry;
import com.jiubang.go.backup.pro.data.AppBackupEntry.AppBackupType;
import com.jiubang.go.backup.pro.data.AppEntry;
import com.jiubang.go.backup.pro.data.AppEntryComparator.SORT_TYPE;
import com.jiubang.go.backup.pro.data.AppInfo;
import com.jiubang.go.backup.pro.data.BaseBackupEntry;
import com.jiubang.go.backup.pro.data.BaseEntry;
import com.jiubang.go.backup.pro.data.BaseEntry.EntryType;
import com.jiubang.go.backup.pro.data.IRecord;
import com.jiubang.go.backup.pro.image.util.FolderBackEntry;
import com.jiubang.go.backup.pro.model.BackupManager;
import com.jiubang.go.backup.pro.product.manage.ProductManager;
import com.jiubang.go.backup.pro.product.manage.ProductPayInfo;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 备份记录信息adapter
 *
 * @author GoBackup Dev Team
 */
public class BackupableRecordDetailAdapter extends RecordDetailListAdpater {
	private static final EntryType[] DEFAULT_UNSELECTED_ENTRYTYPE_FILTER = {
			EntryType.TYPE_USER_DICTIONARY, EntryType.TYPE_USER_CALENDAR,
			EntryType.TYPE_USER_BOOKMARK };

	//	private boolean mBeAbleToBackupAppsData;
	private BackupManager mBackupManager;
	private boolean mIsChineseUser;
	private AppBackupType mAppBackupType;

	public BackupableRecordDetailAdapter(Context context, IRecord record) {
		super(context, record);
		//		mBeAbleToBackupAppsData = isRootValid()
		//				&& PreferenceManager.getInstance().getBoolean(getContext(),
		//						PreferenceManager.KEY_SHOULD_BACKUP_RESTORE_APP_DATA, true);

		//		mAppBackupType = isRootValid()
		//				&& PreferenceManager.getInstance().getBoolean(getContext(),
		//						PreferenceManager.KEY_SHOULD_BACKUP_RESTORE_APP_DATA, true)
		//				? AppBackupType.APK_DATA
		//				: AppBackupType.APK;
		mIsChineseUser = Util.isInland(context);

		mBackupManager = BackupManager.getInstance();
		executeRunnable(new Runnable() {
			@Override
			public void run() {
				List<BaseBackupEntry> appEntries = (List<BaseBackupEntry>) getRecord().getGroup(
						IRecord.GROUP_USER_APP);
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
				sortAppEntries(SORT_TYPE.SORT_BY_APP_NAME, BackupableRecordDetailAdapter.this);
			}
		});
	}

	@Override
	protected void bindTitleExtraInfo(View view, BaseEntry entry) {
		view.setVisibility(View.GONE);
	}

	@Override
	protected void bindSummaryInfo(View view, BaseEntry entry) {
		TextView summary = (TextView) view;
		if (entry instanceof AppBackupEntry) {
			summary.setVisibility(View.VISIBLE);
			if (mAppBackupType == AppBackupType.APK_DATA) {
				summary.setText(R.string.entry_state_app_and_data);
			} else if (mAppBackupType == AppBackupType.APK) {
				summary.setText(R.string.entry_state_only_app);
			} else if (mAppBackupType == AppBackupType.DATA_ONLY) {
				if (((AppBackupEntry) entry).getSpaceUsage(mAppBackupType) <= 0) {
					summary.setText(R.string.no_app_data_to_backup);
				} else {
					summary.setText(R.string.entry_state_only_app_data);
				}
			}
			//			if (!mBeAbleToBackupAppsData) {
			//				summary.setText(R.string.entry_state_only_app);
			//			} else {
			//				summary.setText(R.string.entry_state_app_and_data);
			//			}
			return;
		}
		if (entry instanceof FolderBackEntry) {
			int totalCout = ((FolderBackEntry) entry).getCount();
			int selectCount = ((FolderBackEntry) entry).getSelectedCount();
			summary.setVisibility(View.VISIBLE);
			String imageSelectedInfo = getContext().getString(R.string.progress_detail,
					selectCount, totalCout);
			summary.setText(getContext().getString(R.string.parenthesized_msg, imageSelectedInfo));
			return;
		}
		summary.setVisibility(View.GONE);
	}

	/**
	 * 显示应用程序大小
	 */
	@Override
	protected void bindExtraSummaryInfo(View view, BaseEntry entry) {
		if (entry instanceof AppBackupEntry) {
			TextView size = (TextView) view;
			size.setVisibility(View.VISIBLE);
			final AppBackupEntry appEntry = (AppBackupEntry) entry;
			long sizeValue = appEntry.getSpaceUsage(mAppBackupType);
			if (sizeValue > 0) {
				size.setText(Util.formatFileSize(sizeValue));
				return;
			}
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

	/*	private boolean isRootValid() {
			return ((GoBackupApplication) getContext().getApplicationContext()).getRootProcess()
					.isRootProcessValid();
		}*/

	@Override
	protected boolean isEntrySelectable(BaseEntry entry) {
		if (entry instanceof AppBackupEntry) {
			if (((AppBackupEntry) entry).getSpaceUsage(mAppBackupType) <= 0) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void updateAdapterGroupItemView(View convertView, int groupPos) {
		super.updateAdapterGroupItemView(convertView, groupPos);

		String[] keys = getRecord().getGroupKeys();
		if (groupPos < 0 || groupPos >= keys.length) {
			return;
		}
		String key = keys[groupPos];
		ImageView markerView = (ImageView) convertView.findViewById(R.id.image_marker);
		if (markerView != null) {
			markerView.setVisibility(View.GONE);
			if (IRecord.GROUP_SYSTEM_DATA.equals(key) && !isPaidUser(getContext())) {
				//				final TextView titleExtraInfo = (TextView) convertView
				//						.findViewById(R.id.group_extra_info);
				//				String text = getContext().getString(R.string.advanced_of_function);
				//				titleExtraInfo.setText(getContext().getString(R.string.parenthesized_msg, text));
				markerView.setVisibility(View.VISIBLE);
			}
		}
	}

	private boolean isPaidUser(Context context) {
		return mIsChineseUser
				|| ProductManager.getProductPayInfo(getContext().getApplicationContext(),
						ProductPayInfo.PRODUCT_ID).isAlreadyPaid()
				|| ProductPayInfo.sIsPaidUserByKey;
	}

	public void resetDefaultSelectedItem() {
		// 默认选择用户数据
		checkGroupAllEntries(getGroupPositionByKey(IRecord.GROUP_USER_DATA), true);

		// 日历和用户词典默认不选中
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

	public void setAppBackupType(AppBackupType type) {
		if (mAppBackupType != type) {
			mAppBackupType = type;

			final List<BaseEntry> appEntries = getUserAppEntries();
			if (!Util.isCollectionEmpty(appEntries)) {
				for (BaseEntry entry : appEntries) {
					if (!isEntrySelectable(entry) && entry.isSelected()) {
						entry.setSelected(false);
					}
				}
			}

			notifyDataSetChanged();
		}
	}

	@Override
	protected long getEntrySpaceUsage(BaseEntry entry) {
		if (entry instanceof AppBackupEntry) {
			return ((AppBackupEntry) entry).getSpaceUsage(mAppBackupType);
		}
		return super.getEntrySpaceUsage(entry);
	}
}
