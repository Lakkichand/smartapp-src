package com.go.util.file.media;

import android.database.Cursor;
import android.provider.MediaStore.Images.ImageColumns;

public class ImageFile extends FileInfo {
	public String thumbnail;

	public int orientatain;

	public String bucketName;

	public static ImageFile getInfo(Cursor cur) {
		if (null == cur) {
			return null;
		}

		ImageFile file = new ImageFile();
		if (!file.init(cur)) {
			return null;
		}
		file.bucketName = MediaDbUtil.getString(cur, ImageColumns.BUCKET_DISPLAY_NAME);
		if (file.bucketName == null) {
			file.bucketName = "unknown";
		}
		file.alias = file.fileName;
		file.uri = file.fullFilePath;
		file.thumbnailId = file.dbId;
		file.thumbnailPath = file.fullFilePath;
		return file;
	}
}
