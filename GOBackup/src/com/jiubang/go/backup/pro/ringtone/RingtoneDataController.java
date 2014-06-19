package com.jiubang.go.backup.pro.ringtone;

import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Media;
import android.text.TextUtils;

/**
 * 铃声备份数据
 *
 * @author ReyZhang
 */
public class RingtoneDataController {
	private String mData;
	private String mDisplayName;
	private String mDuration;
	private String mIsAlarm;
	private String mIsMusic;
	private String mIsNotification;
	private String mIsPodcast;
	private String mIsRingtone;
	private String mMimeType;
	private String mSize;
	private String mTitle;

	private static int s_DATA_INDEX;
	private static int s_DISPLAY_NAME_INDEX;
	private static int s_DURATION_INDEX;
	private static int s_IS_ALARM_INDEX;
	private static int s_IS_MUSIC_INDEX;
	private static int s_IS_NOTIFICATION_INDEX;
	private static int s_IS_PODCAST_INDEX;
	private static int s_IS_RINGTONE_INDEX;
	private static int s_MIME_TYPE_INDEX;
	private static int s_SIZE_INDEX;
	private static int s_TITLE_INDEX;

	private Context mContext;

	private static RingtoneDataController s_Controller = null;

	public static RingtoneDataController getInstance(Context context) {
		if (s_Controller != null) {
			return s_Controller;
		}
		s_Controller = new RingtoneDataController(context);
		return s_Controller;
	}

	private RingtoneDataController(Context context) {
		mContext = context;
	}

	public JSONObject getRingtoneJson(Uri uri) {
		JSONObject json = null;
		Cursor cursor = null;
		try {
			cursor = getCursor(uri);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				json = new JSONObject();
				try {
					if (s_DATA_INDEX > 0) {
						mData = cursor.getString(s_DATA_INDEX);
						if (!TextUtils.isEmpty(mData)) {
							json.put(MediaStore.Audio.Media.DATA, mData);
						} else {
							json.put(MediaStore.Audio.Media.DATA, "");
						}
					}
					if (s_DISPLAY_NAME_INDEX > 0) {
						mDisplayName = cursor.getString(s_DISPLAY_NAME_INDEX);
						if (!TextUtils.isEmpty(mDisplayName)) {
							json.put(MediaStore.Audio.Media.DISPLAY_NAME, mDisplayName);
						} else {
							json.put(MediaStore.Audio.Media.DISPLAY_NAME, "");
						}
					}
					if (s_DURATION_INDEX > 0) {
						mDuration = cursor.getString(s_DURATION_INDEX);
						if (!TextUtils.isEmpty(mDuration)) {
							json.put(MediaStore.Audio.Media.DURATION, mDuration);
						} else {
							json.put(MediaStore.Audio.Media.DURATION, "");
						}
					}
					if (s_IS_ALARM_INDEX > 0) {
						mIsAlarm = cursor.getString(s_IS_ALARM_INDEX);
						if (!TextUtils.isEmpty(mIsAlarm)) {
							json.put(MediaStore.Audio.Media.IS_ALARM, mIsAlarm);
						} else {
							json.put(MediaStore.Audio.Media.IS_ALARM, "");
						}
					}
					if (s_IS_MUSIC_INDEX > 0) {
						mIsMusic = cursor.getString(s_IS_MUSIC_INDEX);
						if (!TextUtils.isEmpty(mIsMusic)) {
							json.put(MediaStore.Audio.Media.IS_MUSIC, mIsMusic);
						} else {
							json.put(MediaStore.Audio.Media.IS_MUSIC, "");
						}
					}
					if (s_IS_NOTIFICATION_INDEX > 0) {
						mIsNotification = cursor.getString(s_IS_NOTIFICATION_INDEX);
						if (!TextUtils.isEmpty(mIsNotification)) {
							json.put(MediaStore.Audio.Media.IS_NOTIFICATION, mIsNotification);
						} else {
							json.put(MediaStore.Audio.Media.IS_NOTIFICATION, "");
						}
					}
					if (s_IS_PODCAST_INDEX > 0) {
						mIsPodcast = cursor.getString(s_IS_PODCAST_INDEX);
						if (!TextUtils.isEmpty(mIsPodcast)) {
							json.put(MediaStore.Audio.Media.IS_PODCAST, mIsPodcast);
						} else {
							json.put(MediaStore.Audio.Media.IS_PODCAST, "");
						}
					}
					if (s_IS_RINGTONE_INDEX > 0) {
						mIsRingtone = cursor.getString(s_IS_RINGTONE_INDEX);
						if (!TextUtils.isEmpty(mIsRingtone)) {
							json.put(MediaStore.Audio.Media.IS_RINGTONE, mIsRingtone);
						} else {
							json.put(MediaStore.Audio.Media.IS_RINGTONE, "");
						}
					}
					if (s_MIME_TYPE_INDEX > 0) {
						mMimeType = cursor.getString(s_MIME_TYPE_INDEX);
						if (!TextUtils.isEmpty(mMimeType)) {
							json.put(MediaStore.Audio.Media.MIME_TYPE, mMimeType);
						} else {
							json.put(MediaStore.Audio.Media.MIME_TYPE, "");
						}
					}
					if (s_SIZE_INDEX > 0) {
						mSize = cursor.getString(s_SIZE_INDEX);
						if (!TextUtils.isEmpty(mSize)) {
							json.put(MediaStore.Audio.Media.SIZE, mSize);
						} else {
							json.put(MediaStore.Audio.Media.SIZE, "");
						}
					}
					if (s_TITLE_INDEX > 0) {
						mTitle = cursor.getString(s_TITLE_INDEX);
						if (!TextUtils.isEmpty(mTitle)) {
							json.put(MediaStore.Audio.Media.TITLE, mTitle);
						} else {
							json.put(MediaStore.Audio.Media.TITLE, "");
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}

		return json;
	}

	public String getData() {
		return mData;
	}

	private Cursor getCursor(Uri uri) {
		Cursor cursor = null;
		if (uri == null) {
			return null;
		}

		String scheme = uri.getScheme();
		if (scheme == null || scheme.equals("file")) {
			String path = uri.getPath();
			String where = Media.DATA + "='" + path + "'";
			cursor = mContext.getContentResolver().query(Media.INTERNAL_CONTENT_URI, null, where,
					null, null);
			if (cursor != null && cursor.getCount() == 0) {
				cursor.close();
				cursor = null;
			}
			if (cursor == null) {
				cursor = mContext.getContentResolver().query(Media.EXTERNAL_CONTENT_URI, null,
						where, null, null);
			}
		} else {
			cursor = mContext.getContentResolver().query(uri, null, null, null, null);
		}

		if (cursor != null) {
			s_DATA_INDEX = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
			s_DISPLAY_NAME_INDEX = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
			s_DURATION_INDEX = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
			s_IS_ALARM_INDEX = cursor.getColumnIndex(MediaStore.Audio.Media.IS_ALARM);
			s_IS_MUSIC_INDEX = cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC);
			s_IS_NOTIFICATION_INDEX = cursor.getColumnIndex(MediaStore.Audio.Media.IS_NOTIFICATION);
			s_IS_PODCAST_INDEX = cursor.getColumnIndex(MediaStore.Audio.Media.IS_PODCAST);
			s_IS_RINGTONE_INDEX = cursor.getColumnIndex(MediaStore.Audio.Media.IS_RINGTONE);
			s_MIME_TYPE_INDEX = cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE);
			s_SIZE_INDEX = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE);
			s_TITLE_INDEX = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
		}
		return cursor;
	}

}
