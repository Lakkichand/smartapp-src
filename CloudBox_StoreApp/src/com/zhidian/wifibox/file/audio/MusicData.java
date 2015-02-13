package com.zhidian.wifibox.file.audio;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class MusicData implements Parcelable {

	public final static String KEY_MUSIC_DATA = "MusicData";
	
	private final static String KEY_MUSIC_ID = "Id";
	private final static String KEY_MUSIC_POSITION = "MusicPosition";	// ListView里面的ID
	private final static String KEY_MUSIC_NAME = "MusicName";
	private final static String KEY_MUSIC_DISPLAY_NAME = "MusicDisplayName";
	private final static String KEY_MUSIC_TIME = "MusicTime";
	private final static String KEY_MUSIC_DATE_MODIFIED = "MusicDateModified";
	private final static String KEY_MUSIC_DATE_ADDED = "MusicDateAdded";
	private final static String KEY_MUSIC_TYPE = "MusicType";
	private final static String KEY_MUSIC_PATH = "MusicPath";
	private final static String KEY_MUSIC_ARITST = "MusicAritst";
	private final static String KEY_MUSIC_SIZE = "MusicSize";
	private final static String KEY_MUSIC_IS_DEL = "IsDel";
	
	public int mId;
	public int mMusicPosition;
	public String mMusicName;
	public String mMusicDisplayName;
	public String mMusicType;
	public int  mMusicTime;
	public int  mMusicDateModified;
	public int  mMusicDateAdded;
	public String mMusicPath;
	public String mMusicAritst;
	public int mMusicSize;
	public boolean mIsDel;
	
	public MusicData() {
		mId = 0;
		mMusicPosition = 0;
		mMusicName = "";
		mMusicDisplayName = "";
		mMusicType = "";
		mMusicTime = 0;
		mMusicDateModified = 0;
		mMusicDateAdded = 0;
		mMusicPath = "";
		mMusicAritst = "";
		mMusicSize = 0;
		mIsDel = false;
		
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		Bundle mBundle = new Bundle();
		
		mBundle.putInt(KEY_MUSIC_ID, mId);
		mBundle.putInt(KEY_MUSIC_NAME, mMusicPosition);
		mBundle.putString(KEY_MUSIC_NAME, mMusicName);
		mBundle.putString(KEY_MUSIC_DISPLAY_NAME, mMusicDisplayName);
		mBundle.putString(KEY_MUSIC_TYPE, mMusicType);
		mBundle.putInt(KEY_MUSIC_TIME, mMusicTime);
		mBundle.putInt(KEY_MUSIC_DATE_MODIFIED, mMusicDateModified);
		mBundle.putInt(KEY_MUSIC_DATE_ADDED, mMusicDateAdded);
		mBundle.putString(KEY_MUSIC_PATH, mMusicPath);
		mBundle.putString(KEY_MUSIC_ARITST, mMusicAritst);
		mBundle.putInt(KEY_MUSIC_SIZE, mMusicSize);
		mBundle.putBoolean(KEY_MUSIC_IS_DEL, mIsDel);
		dest.writeBundle(mBundle);

	}
	
	public static final Parcelable.Creator<MusicData> CREATOR = new Creator<MusicData>() {
		
		@Override
		public MusicData[] newArray(int size) {
			return new MusicData[size];
		}
		
		@Override
		public MusicData createFromParcel(Parcel source) {
			MusicData Data = new MusicData();
			
			Bundle mBundle = new Bundle();
			mBundle = source.readBundle();
			Data.mId = mBundle.getInt(KEY_MUSIC_ID);
			Data.mMusicPosition = mBundle.getInt(KEY_MUSIC_POSITION);
			Data.mMusicName = mBundle.getString(KEY_MUSIC_NAME);
			Data.mMusicDisplayName = mBundle.getString(KEY_MUSIC_DISPLAY_NAME);
			Data.mMusicType = mBundle.getString(KEY_MUSIC_TYPE);
			Data.mMusicTime = mBundle.getInt(KEY_MUSIC_TIME);
			Data.mMusicDateModified = mBundle.getInt(KEY_MUSIC_DATE_MODIFIED);
			Data.mMusicDateAdded = mBundle.getInt(KEY_MUSIC_DATE_ADDED);
			Data.mMusicPath = mBundle.getString(KEY_MUSIC_PATH);
			Data.mMusicAritst = mBundle.getString(KEY_MUSIC_ARITST);
			Data.mMusicSize = mBundle.getInt(KEY_MUSIC_SIZE);
			Data.mIsDel = mBundle.getBoolean(KEY_MUSIC_IS_DEL);
			
			return Data;
		}
	};

}
