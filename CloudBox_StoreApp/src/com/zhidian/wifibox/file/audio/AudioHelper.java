package com.zhidian.wifibox.file.audio;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore.Audio.Media;

public class AudioHelper {
	
	final String TAG = getClass().getSimpleName();
	
	private Context mContext;
	private ContentResolver cr;
	
	private static AudioHelper mInstance;
	
	public AudioHelper() {}
	
	public static AudioHelper getInstance() {
		if (mInstance == null) {
			mInstance = new AudioHelper();
		}
		return mInstance;
	}
	
	/**
	 * 初始化
	 * 
	 * @param context
	 */
	public void init(Context context) {
		if (this.mContext == null) {
			this.mContext = context;
			cr = mContext.getContentResolver();
		}
	}
	
	public List<MusicData> getMusicFileList() {
		List<MusicData> list = new ArrayList<MusicData>();
		
		String[] projection = new String[] {
				Media._ID,
				Media.TITLE,
				Media.DURATION,
				Media.DATE_MODIFIED,
				Media.DISPLAY_NAME,
				Media.DATE_ADDED,
				Media.MIME_TYPE,
				Media.DATA,
				Media.ARTIST,
				Media.SIZE
		};
		String selection = "(" + Media.DATA + " like ? or " +
				Media.DATA + " like ? or " +
				Media.DATA + " like ? or " +
				Media.DATA + " like ? or " +
				Media.DATA + " like ? or " +
				Media.DATA + " like ? or " +
				Media.DATA + " like ? or " +
				Media.DATA + " like ? or " +
				Media.DATA + " like ? or " +
				Media.DATA + " like ? or " +
				Media.DATA + " like ? or " +
				Media.DATA + " like ? ) AND " +
				Media.DATA + " not like ? ";
		String[] selectionArgs = new String[] { 
			"%.wave", "%.mp3", "%.flac", "%.cda",
			"%.wav", "%.midi", "%.lpac", "%.tta",
			"%.ape", "%.tak", "%.la", "%.rmvb", 
			File.separator + "data" + File.separator + "data" + File.separator + "%"
		};
		String sortOrder = "date_added DESC";
		
		Cursor cur = null;
		
		try {
			cur = cr.query(Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, sortOrder);
			if (cur.moveToFirst()) {
				
				int colIdIndex = cur.getColumnIndex(Media._ID);
				int colNameIndex = cur.getColumnIndex(Media.TITLE);
				int colTimeIndex = cur.getColumnIndex(Media.DURATION);
				int colModifiedIndex = cur.getColumnIndex(Media.DATE_MODIFIED);
				int colDisplayIndex = cur.getColumnIndex(Media.DISPLAY_NAME);
				int colAddedIndex = cur.getColumnIndex(Media.DATE_ADDED);
				int colTypeIndex = cur.getColumnIndex(Media.MIME_TYPE);
				int colPathIndex = cur.getColumnIndex(Media.DATA);
				int colArtistIndex = cur.getColumnIndex(Media.ARTIST);
				int colSizeIndex = cur.getColumnIndex(Media.SIZE);
				
				int i = 0;
				do {
					MusicData data = new MusicData();
					data.mId = cur.getInt(colIdIndex);
					data.mMusicName = cur.getString(colNameIndex);
					data.mMusicDisplayName = cur.getString(colDisplayIndex);
					data.mMusicType = cur.getString(colTypeIndex);
					data.mMusicTime = cur.getInt(colTimeIndex);
					data.mMusicDateModified = cur.getInt(colModifiedIndex);
					data.mMusicDateAdded = cur.getInt(colAddedIndex);
					data.mMusicPath = cur.getString(colPathIndex);
					data.mMusicAritst = cur.getString(colArtistIndex);
					data.mMusicSize = cur.getInt(colSizeIndex);
					data.mMusicPosition = i;
					
					list.add(data);
					i++;
				} while (cur.moveToNext());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cur != null) {
				cur.close();
			}
		}
		
		return list;
	}
	
	/**
	 * 根据ID删除相关的数据
	 * @param id
	 */
	public void delete(String id) {
		String where = Media._ID + "=?";
		String[] selectionArgs = new String[] { id };
		try {
			cr.delete(Media.EXTERNAL_CONTENT_URI, where, selectionArgs);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
