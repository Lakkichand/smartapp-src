package com.jiubang.go.backup.pro.data;

import java.util.Comparator;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.model.DrawableProvider.OnDrawableLoadedListener;

/**
 * BaseEntry
 *
 * @author maiyongshen
 */
public abstract class BaseEntry {
	private static final int CONTACTS_ENTRY_PROGRESS_WEIGHT = 20;
	private static final int SMS_ENTRY_PROGRESS_WEIGHT = 30;
	private static final int MMS_ENTRY_PROGRESS_WEIGHT = 10;
	private static final int CALLLOG_ENTRY_PROGRESS_WEIGHT = 5;
	private static final int USER_DICTIONARY_ENTRY_PROGRESS_WEIGHT = 5;
	private static final int WIFI_ENTRY_PROGRESS_WEIGHT = 5;
	private static final int LAUNCHER_DATA_ENTRY_PROGRESS_WEIGHT = 5;
	private static final int GO_LAUNCHER_SETTTING_ENTRY_PROGRESS_WEIGHT = 5;
	private static final int APP_ENTRY_PROGRESS_WEIGHT = 10;
	private static final int WALLPAPER_ENTRY_PROGRESS_WEIGHT = 10;
	private static final int RINGTONE_ENTRY_PROGRESS_WEIGHT = 5;
	private static final int CALENDAR_ENTRY_PROGRESS_WEIGHT = 5;
	private static final int BOOKMARK_ENTRY_PROGRESS_WEIGHT = 5;
	private static final int IMAGE_ENTRY_PROGRESS_WEIGHT = 5;

	/**
	 * EntryType
	 *
	 * @author maiyongshen
	 */
	public enum EntryType {
		TYPE_UNKNOWN,
		TYPE_SYSTEM_APP,
		TYPE_USER_APP,
		TYPE_USER_SMS,
		TYPE_USER_CONTACTS,
		TYPE_USER_CALL_HISTORY,
		TYPE_USER_BOOKMARK,
		TYPE_USER_CALENDAR,
		TYPE_USER_GOLAUNCHER_SETTING,
		TYPE_SYSTEM_WIFI,
		TYPE_SYSTEM_ACCOUNT,
		TYPE_SYSTEM_LAUNCHER_DATA,
		TYPE_USER_DICTIONARY,
		TYPE_USER_MMS,
		TYPE_SYSTEM_WALLPAPER,
		TYPE_SYSTEM_RINGTONE,
		TYPE_USER_IMAGE;

		public static String getDescription(Context context, EntryType type) {
			if (context == null || type == null) {
				return "";
			}
			switch (type) {
				case TYPE_SYSTEM_APP :
				case TYPE_USER_APP :
					return context.getString(R.string.applications);
				case TYPE_USER_CONTACTS :
					return context.getString(R.string.contacts);
				case TYPE_USER_SMS :
					return context.getString(R.string.sms);
				case TYPE_USER_MMS :
					return context.getString(R.string.mms);
				case TYPE_USER_CALL_HISTORY :
					return context.getString(R.string.call_log);
				case TYPE_USER_CALENDAR :
					return context.getString(R.string.calendar);
				case TYPE_USER_BOOKMARK :
					return context.getString(R.string.bookmark);
				case TYPE_USER_DICTIONARY :
					return context.getString(R.string.user_dictionary);
				case TYPE_USER_GOLAUNCHER_SETTING :
					return context.getString(R.string.golauncher_setting);
				case TYPE_SYSTEM_WALLPAPER :
					return context.getString(R.string.wallpaper);
				case TYPE_SYSTEM_RINGTONE :
					return context.getString(R.string.ringtone);
				case TYPE_SYSTEM_LAUNCHER_DATA :
					return context.getString(R.string.launcher_layout);
				case TYPE_SYSTEM_WIFI :
					return context.getString(R.string.wifi_access_points);
				case TYPE_USER_IMAGE :
					return context.getString(R.string.image);
				default :
					break;
			}
			return "";
		}
	}

	private boolean mIsSelected;
	private OnSelectedChangeListener mOnSelectedChangeListener;
	private Drawable mIcon;
	protected boolean mInitingIcon = false;
	protected boolean mIconInited = false;

	public boolean isSelected() {
		return mIsSelected;
	}

	public void setSelected(boolean selected) {
		if (selected != mIsSelected) {
			mIsSelected = selected;
			if (mOnSelectedChangeListener != null) {
				mOnSelectedChangeListener.onSelectedChange(this, mIsSelected);
			}
		}
	}

	public void setOnSelectedChangeListener(OnSelectedChangeListener l) {
		mOnSelectedChangeListener = l;
	}
	
	public OnSelectedChangeListener getOnSelectedChangeListener() {
		return mOnSelectedChangeListener;
	}

	private Drawable getDefaultIcon(Context context) {
		if (context == null) {
			return null;
		}
		PackageManager pm = context.getPackageManager();
		if (pm != null) {
			return pm.getDefaultActivityIcon();
		}
		return null;
	}

	protected void setIcon(Drawable icon) {
		if (icon != null) {
			mIcon = icon;
			mIconInited = true;
		}
	}

	public boolean isIconIniting() {
		return mInitingIcon;
	}

	public boolean hasIconInited() {
		return mIconInited;
	}

	public Drawable getIcon(Context context) {
		if (mIconInited && mIcon != null) {
			return mIcon;
		}
		return getDefaultIcon(context);
	}
	
	public abstract Drawable getIcon(Context context, OnDrawableLoadedListener listener);

	public boolean loadIcon(Context context) {
		return false;
	}

	@Override
	public String toString() {
		return getDescription();
	}

	public int getEntryProgressWeight() {
		int weight = 0;
		switch (getType()) {
			case TYPE_SYSTEM_APP :
				weight = APP_ENTRY_PROGRESS_WEIGHT;
				break;
			case TYPE_SYSTEM_LAUNCHER_DATA :
				weight = LAUNCHER_DATA_ENTRY_PROGRESS_WEIGHT;
				break;
			case TYPE_SYSTEM_RINGTONE :
				weight = RINGTONE_ENTRY_PROGRESS_WEIGHT;
				break;
			case TYPE_SYSTEM_WALLPAPER :
				weight = WALLPAPER_ENTRY_PROGRESS_WEIGHT;
				break;
			case TYPE_SYSTEM_WIFI :
				weight = WIFI_ENTRY_PROGRESS_WEIGHT;
				break;
			case TYPE_USER_APP :
				weight = APP_ENTRY_PROGRESS_WEIGHT;
				break;
			case TYPE_USER_BOOKMARK :
				weight = BOOKMARK_ENTRY_PROGRESS_WEIGHT;
				break;
			case TYPE_USER_CALENDAR :
				weight = CALENDAR_ENTRY_PROGRESS_WEIGHT;
				break;
			case TYPE_USER_CALL_HISTORY :
				weight = CALLLOG_ENTRY_PROGRESS_WEIGHT;
				break;
			case TYPE_USER_CONTACTS :
				weight = CONTACTS_ENTRY_PROGRESS_WEIGHT;
				break;
			case TYPE_USER_DICTIONARY :
				weight = USER_DICTIONARY_ENTRY_PROGRESS_WEIGHT;
				break;
			case TYPE_USER_MMS :
				weight = MMS_ENTRY_PROGRESS_WEIGHT;
				break;
			case TYPE_USER_GOLAUNCHER_SETTING :
				weight = GO_LAUNCHER_SETTTING_ENTRY_PROGRESS_WEIGHT;
				break;
			case TYPE_USER_SMS :
				weight = SMS_ENTRY_PROGRESS_WEIGHT;
				break;
			case TYPE_USER_IMAGE :
				weight = IMAGE_ENTRY_PROGRESS_WEIGHT;
				break;

			default :
				break;
		}
		return weight;
	}

	public boolean isPaidFunctionItem() {
		boolean isPaidFunction = false;
		switch (getType()) {
			case TYPE_SYSTEM_LAUNCHER_DATA :
			case TYPE_SYSTEM_RINGTONE :
			case TYPE_SYSTEM_WALLPAPER :
			case TYPE_SYSTEM_WIFI :
				isPaidFunction = true;
				break;

			default :
				isPaidFunction = false;
				break;
		}
		return isPaidFunction;
	}

	public abstract int getId();

	public abstract EntryType getType();

	public abstract long getSpaceUsage();

	public abstract String getDescription();

	public abstract boolean isNeedRootAuthority();

	/**
	 * 接口 监听选择状态
	 *
	 * @author maiyongshen
	 */
	public static interface OnSelectedChangeListener {
		public void onSelectedChange(BaseEntry entry, boolean isSelected);
	}

	/**
	 * EntryComparator
	 *
	 * @author maiyongshen
	 */
	public static class EntryComparator implements Comparator<BaseEntry> {
		@Override
		public int compare(BaseEntry lhs, BaseEntry rhs) {
			return getEntryComparatorFactor(lhs) - getEntryComparatorFactor(rhs);
		}

		private int getEntryComparatorFactor(BaseEntry entry) {
			if (entry == null) {
				return 0;
			}

			int factor = 0;
			switch (entry.getType()) {
				case TYPE_USER_SMS :
					factor = 1;
					break;
				case TYPE_USER_MMS :
					factor = 2;
					break;
				case TYPE_USER_CONTACTS :
					factor = 3;
					break;
				case TYPE_USER_CALL_HISTORY :
					factor = 4;
					break;
				case TYPE_USER_GOLAUNCHER_SETTING :
					factor = 5;
					break;
				case TYPE_USER_BOOKMARK :
					factor = 6;
					break;
				case TYPE_USER_DICTIONARY :
					factor = 7;
					break;
				case TYPE_USER_CALENDAR :
					factor = 8;
					break;
				case TYPE_SYSTEM_RINGTONE :
					factor = 9;
					break;
				case TYPE_SYSTEM_WALLPAPER :
					factor = 10;
					break;
				case TYPE_SYSTEM_LAUNCHER_DATA :
					factor = 11;
					break;
				case TYPE_SYSTEM_WIFI :
					factor = 12;
					break;
				case TYPE_USER_APP :
					factor = 13;
					break;
				case TYPE_SYSTEM_APP :
					factor = 14;
					break;
				case TYPE_USER_IMAGE :
					factor = 15;
					break;

				default :
					break;
			}
			return factor;
		}
	}
}
