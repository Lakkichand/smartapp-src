package com.go.util.file.media;

import android.database.Cursor;

public class VideoFile extends FileInfo {
	public String thumbnail;
	public long duration;

	public static VideoFile getInfo(Cursor cur) {
		if (null == cur) {
			return null;
		}

		VideoFile file = new VideoFile();
		if (!file.init(cur)) {
			return null;
		}
		file.duration = MediaDbUtil.getLong(cur, "duration");
		file.alias = file.fileName;
		file.uri = file.fullFilePath;
		file.thumbnailId = file.dbId;
		file.thumbnailPath = file.fullFilePath;
		return file;
	}
}
