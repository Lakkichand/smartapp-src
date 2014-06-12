package com.go.util.file.media;

import android.database.Cursor;
import android.media.MediaPlayer;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.MediaColumns;

public class AudioFile extends FileInfo {
	private static MediaPlayer sMediaPlayer = new MediaPlayer();
	public long duration;

	public String album;
	public int albumId;

	public String displayName;

	public String author;

	public boolean mIsEditMode;
	public boolean mSelected;
	public boolean mIsPlaying;

	public static AudioFile getInfo(Cursor cur) {
		if (null == cur) {
			return null;
		}

		AudioFile file = new AudioFile();
		if (!file.init(cur)) {
			return null;
		}
		long duration = MediaDbUtil.getLong(cur, AudioColumns.DURATION);
		// 当从数据库拿出来的duration小于等于0或大于10分钟时，认为这个duration可能不准确，需要通过MediaPlayer拿到真正的duration
		if (duration <= 0 || duration > 600000) {
			try {
				sMediaPlayer.reset();
				sMediaPlayer.setDataSource(file.fullFilePath);
				sMediaPlayer.prepare();
				duration = sMediaPlayer.getDuration();
			} catch (Exception e) {
				// do nothing
			}
		}
		// 音乐时长小于10秒的过滤掉
		if (duration < 10 * 1000) {
			return null;
		}

		file.duration = duration;

		file.author = MediaDbUtil.getString(cur, AudioColumns.ARTIST);
		file.album = MediaDbUtil.getString(cur, AudioColumns.ALBUM);
		if (file.album == null) {
			file.album = "unknown";
		}
		file.albumId = MediaDbUtil.getInt(cur, AudioColumns.ALBUM_ID);
		file.displayName = MediaDbUtil.getString(cur, MediaColumns.TITLE);
		if (file.displayName == null) {
			file.displayName = "unknown";
		}
		file.alias = file.displayName;
		file.uri = file.fullFilePath;
		file.thumbnailId = file.dbId;
		file.thumbnailPath = file.fullFilePath;
		return file;
	}
}
