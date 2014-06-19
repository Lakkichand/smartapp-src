package com.jiubang.go.backup.pro.ui;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.data.AppInfo;
import com.jiubang.go.backup.pro.data.AppRestoreEntry;
import com.jiubang.go.backup.pro.data.AppRestoreEntry.AppRestoreType;
import com.jiubang.go.backup.pro.data.BaseEntry;
import com.jiubang.go.backup.pro.data.BaseRestoreEntry;
import com.jiubang.go.backup.pro.data.BaseRestoreEntry.RestorableState;
import com.jiubang.go.backup.pro.data.BookMarkRestoreEntry;
import com.jiubang.go.backup.pro.data.CalendarRestoreEntry;
import com.jiubang.go.backup.pro.data.CallLogRestoreEntry;
import com.jiubang.go.backup.pro.data.ContactsRestoreEntry;
import com.jiubang.go.backup.pro.data.IRecord;
import com.jiubang.go.backup.pro.data.MmsRestoreEntry;
import com.jiubang.go.backup.pro.data.RestorableRecord;
import com.jiubang.go.backup.pro.data.RingtoneRestoreEntry;
import com.jiubang.go.backup.pro.data.SmsRestoreEntry;
import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.model.BackupManager;
import com.jiubang.go.backup.pro.model.DrawableProvider;
import com.jiubang.go.backup.pro.product.manage.ProductManager;
import com.jiubang.go.backup.pro.product.manage.ProductPayInfo;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 恢复记录详情列表
 *
 * @author maiyongshen
 */
public class RestorableRecordDetailAdapter extends RecordDetailListAdpater {

	private RestorableRecord mRecord;
	private BackupDBHelper mBackupDBHelper;
	private DrawableProvider mDrawableProvider;
//	private boolean mBeAbleToRestoreAppsData;
	private AppRestoreType mAppRestoreType;
	private boolean mRootState = false;

	public RestorableRecordDetailAdapter(Context context, IRecord record) {
		super(context, record);
		mRecord = (RestorableRecord) record;
		mBackupDBHelper = mRecord.getBackupDBHelper(context);
		mDrawableProvider = DrawableProvider.getInstance();
//		mBeAbleToRestoreAppsData = mRootState;
	}
	
	public void updateWithRootStateChanged(boolean rootState) {
		if (mRootState != rootState) {
			mRootState = rootState;
//			mBeAbleToRestoreAppsData = rootState;
			notifyDataSetChanged();
		}
	}

/*	private boolean isRootValid() {
		return ((GoBackupApplication) getContext().getApplicationContext()).getRootProcess()
				.isRootProcessValid();
	}*/
	
	@Override
	public void release() {
		if (mBackupDBHelper != null) {
			mBackupDBHelper.close();
		}
	}

	@Override
	protected void bindTitleExtraInfo(View view, BaseEntry entry) {
		view.setVisibility(View.VISIBLE);
		TextView extraInfo = (TextView) view;

		if (entry instanceof AppRestoreEntry) {
			final AppInfo appInfo = ((AppRestoreEntry) entry).getAppInfo();
			String state = null;
			if (isAppInstalled(appInfo)) {
				state = getContext().getString(R.string.app_installed);
			} else {
				state = getContext().getString(R.string.app_uninstalled);
			}
			extraInfo.setText(getContext().getString(R.string.parenthesized_msg, state));
			return;
		}

		if (entry instanceof ContactsRestoreEntry) {
			int count = mRecord.getContactsCount();
			if (count > 0) {
				extraInfo.setText(getContext().getString(R.string.parenthesized_msg,
						String.valueOf(count)));
				return;
			}
		}

		if (entry instanceof SmsRestoreEntry) {
			int count = mRecord.getMessagesCount();
			if (count > 0) {
				extraInfo.setText(getContext().getString(R.string.parenthesized_msg,
						String.valueOf(count)));
				return;
			}
		}

		if (entry instanceof MmsRestoreEntry) {
			int count = mRecord.getMMSCount();
			if (count > 0) {
				extraInfo.setText(getContext().getString(R.string.parenthesized_msg,
						String.valueOf(count)));
				return;
			}
		}
		
		if (entry instanceof BookMarkRestoreEntry) {
			int count = mRecord.getBookMarkCount();
			if (count > 0) {
				extraInfo.setText(getContext().getString(R.string.parenthesized_msg,
						String.valueOf(count)));
				return;
			}
		}

		if (entry instanceof CallLogRestoreEntry) {
			int count = mRecord.getCallLogsCount();
			if (count > 0) {
				extraInfo.setText(getContext().getString(R.string.parenthesized_msg,
						String.valueOf(count)));
				return;
			}
		}

		if (entry instanceof CalendarRestoreEntry) {
			int count = mRecord.getCalendarEventCount();
			if (count > 0) {
				extraInfo.setText(getContext().getString(R.string.parenthesized_msg,
						String.valueOf(count)));
				return;
			}
		}

		if (entry instanceof RingtoneRestoreEntry) {
			int count = mRecord.getRingtoneCount();
			if (count > 0) {
				extraInfo.setText(getContext().getString(R.string.parenthesized_msg,
						String.valueOf(count)));
				return;
			}
		}

		view.setVisibility(View.GONE);
	}

	@Override
	protected void bindSummaryInfo(View view, BaseEntry entry) {
		TextView summary = (TextView) view;
		
		if (entry instanceof AppRestoreEntry) {
			summary.setVisibility(View.VISIBLE);
			RestorableState state = ((AppRestoreEntry) entry).getRestorableState();
			if (state == RestorableState.APP_DATA_RESTORABLE
					|| state == RestorableState.APP_RESTORABLE
					|| state == RestorableState.DATA_RESTORABLE) {
				if (state == RestorableState.APP_RESTORABLE) {
					if (mAppRestoreType == AppRestoreType.DATA_ONLY) {
						summary.setText(R.string.app_data_corruption);
					} else {
						summary.setText(R.string.entry_state_only_app);
					}
				} else if (state == RestorableState.APP_DATA_RESTORABLE) {
					if (!mRootState) {
						summary.setText(R.string.entry_state_only_app);
					} else {
						if (mAppRestoreType == AppRestoreType.APP) {
							summary.setText(R.string.entry_state_only_app);
						} else if (mAppRestoreType == AppRestoreType.DATA_ONLY) {
							if (((AppRestoreEntry) entry).getSpaceUsage(mAppRestoreType) <= 0) {
								summary.setText(R.string.no_app_data_to_backup);
							} else if (!isAppInstalled((AppRestoreEntry) entry)) {
								summary.setText(R.string.data_unrestorable_without_app);
							} else {
								summary.setText(R.string.entry_state_only_app_data);
							}
						} else {
							summary.setText(R.string.entry_state_app_and_data);
						}
					}
				} else if (state == RestorableState.DATA_RESTORABLE) {
					if (!isAppInstalled((AppRestoreEntry) entry)) {
						summary.setText(R.string.data_unrestorable_without_app);
						if (!isEntrySelectable(entry) && entry.isSelected()) {
							entry.setSelected(false);
							updateAllViews();
						}
					} else if (!mRootState) {
						summary.setText(R.string.data_unrestorable_without_root);
						if (!isEntrySelectable(entry) && entry.isSelected()) {
							entry.setSelected(false);
							updateAllViews();
						}
					} else if (mAppRestoreType == AppRestoreType.APP) {
						summary.setText(R.string.apk_corruption);
					} else {
						summary.setText(R.string.entry_state_only_app_data);
					}
				}
			} else if (state == RestorableState.DATA_CORRUPTION) {
				summary.setText(R.string.corrupted);
			} else {
				summary.setText(R.string.not_restorable);
			}
			return;
		}
		
		// 备份项不支持恢复
		if (!((BaseRestoreEntry) entry).isRestorable()) {
			summary.setVisibility(View.VISIBLE);
			summary.setText(R.string.not_restorable);
			return;
		}
		
		summary.setVisibility(View.GONE);
	}

	@Override
	protected void bindExtraSummaryInfo(View view, BaseEntry entry) {
		if (entry instanceof AppRestoreEntry) {
			final AppRestoreEntry appEntry = (AppRestoreEntry) entry;
			if (isEntrySelectable(appEntry)) {
				TextView size = (TextView) view;
				size.setVisibility(View.VISIBLE);
				size.setText(Util.formatFileSize(appEntry.getSpaceUsage(mAppRestoreType)));
				return;
			}
		}
		view.setVisibility(View.GONE);
	}

	@Override
	protected void bindMarkerView(View view, BaseEntry entry) {
		view.setVisibility(View.GONE);
	}

/*	@Override
	protected void bindCheckbox(CheckBox checkBox, BaseEntry entry, int groupPos, int childPos) {
		super.bindCheckbox(checkBox, entry, groupPos, childPos);
		if (entry instanceof BaseRestoreEntry) {
			if (!isEntrySelectable(entry)) {
				checkBox.setVisibility(View.GONE);
			} else {
				checkBox.setVisibility(View.VISIBLE);
			}
		}
	}*/

	private boolean isAppInstalled(AppInfo appInfo) {
		return BackupManager.getInstance().isApplicationInstalled(appInfo);
	}

	@Override
	protected boolean isEntrySelectable(BaseEntry entry) {
		if (!(entry instanceof BaseRestoreEntry)) {
			return false;
		}
		BaseRestoreEntry restoreEntry = (BaseRestoreEntry) entry;
		if (!mRootState && restoreEntry.isNeedRootAuthority()) {
			return false;
		}
		
		if (restoreEntry instanceof AppRestoreEntry) {
			final AppRestoreEntry appEntry = (AppRestoreEntry) restoreEntry;
			RestorableState state = appEntry.getRestorableState();
			if (!mRootState || !isAppInstalled(appEntry)) {
				if (state == RestorableState.DATA_RESTORABLE || mAppRestoreType == AppRestoreType.DATA_ONLY) {
					return false;
				}
			}
			if (((AppRestoreEntry) entry).getSpaceUsage(mAppRestoreType) <= 0) {
				return false;
			}
			if (appEntry.getRestorableState() == RestorableState.DATA_RESTORABLE
					&& mAppRestoreType == AppRestoreType.APP) {
				return false;
			}
			if (appEntry.getRestorableState() == RestorableState.APP_RESTORABLE
					&& mAppRestoreType == AppRestoreType.DATA_ONLY) {
				return false;
			}
		}
		
		if (!restoreEntry.isRestorable()) {
			return false;
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
/*		if (!Util.isInland(getContext())) {
			String key = keys[groupPos];
			if (IRecord.GROUP_SYSTEM_DATA.equals(key) && !isPaidUser()) {
				final TextView titleExtraInfo = (TextView) convertView
						.findViewById(R.id.group_extra_info);
				titleExtraInfo.setText(R.string.advanced_of_function);
			}
		}*/
	}

	private boolean isPaidUser() {
		return ProductManager.getProductPayInfo(getContext().getApplicationContext(),
				ProductPayInfo.PRODUCT_ID).isAlreadyPaid();
	}
	
	@Override
	protected Drawable getEntryIcon(BaseEntry entry, int groupPos, int childPos) {
		if (entry instanceof AppRestoreEntry) {
			AppInfo appInfo = ((AppRestoreEntry) entry).getAppInfo();
			Drawable drawable = mDrawableProvider.getDrawableFromCaches(DrawableProvider.buildDrawableKey(appInfo.packageName));
			if (drawable == null) {
				byte[] iconData = mBackupDBHelper.getAppIconRawData(appInfo.packageName);
				drawable = ((AppRestoreEntry) entry).getIcon(getContext(), iconData, new EntryIconLoadListener(groupPos, childPos));
			}
			return drawable;
		}
		return super.getEntryIcon(entry, groupPos, childPos);
	}
	
	private boolean isAppInstalled(AppRestoreEntry app) {
		AppInfo appInfo = app.getAppInfo();
		return isAppInstalled(appInfo);
	}
	
	public void setAppRestoreType(AppRestoreType type) {
		if (type == null) {
			return;
		}
		if (mAppRestoreType != type) {
			mAppRestoreType = type;
			
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
//		mBeAbleToRestoreAppsData = mRootState && mAppRestoreType != AppRestoreType.APP;
	}
	
	public boolean isAnyAppsDataRestorable() {
		final List<BaseEntry> appEntries = getUserAppEntries();
		if (Util.isCollectionEmpty(appEntries)) {
			return false;
		}
		for (BaseEntry entry : appEntries) {
			if (!isEntrySelectable(entry)) {
				continue;
			}
			final AppRestoreEntry appEntry = (AppRestoreEntry) entry;
			final RestorableState state = appEntry.getRestorableState();
			if (state == RestorableState.APP_DATA_RESTORABLE || state == RestorableState.DATA_RESTORABLE) {
				return true;
			}
		}
		return false;
	}
	
	public Set<RestorableState> getUserAppRestorableStates() {
		List<BaseEntry> appEntries = getUserAppEntries();
		if (Util.isCollectionEmpty(appEntries)) {
			return null;
		}
		Set<RestorableState> set = new HashSet<BaseRestoreEntry.RestorableState>();
		for (BaseEntry entry : appEntries) {
			final AppRestoreEntry appEntry = (AppRestoreEntry) entry;
			final RestorableState state = appEntry.getRestorableState();
			set.add(state);
		}
		return !Util.isCollectionEmpty(set) ? set : null;
	}
	
	@Override
	protected long getEntrySpaceUsage(BaseEntry entry) {
		if (entry instanceof AppRestoreEntry) {
			return ((AppRestoreEntry) entry).getSpaceUsage(mAppRestoreType);
		}
		return super.getEntrySpaceUsage(entry);
	}
}
