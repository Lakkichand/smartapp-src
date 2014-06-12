/*
 * Copyright (c) 2010-2011, The MiCode Open Source Community (www.micode.net)
 *
 * This file is part of FileExplorer.
 *
 * FileExplorer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FileExplorer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SwiFTP.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.go.util.file.media;

import java.io.File;
import java.io.FilenameFilter;

import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore.MediaColumns;


public class FileInfo {

	public String fullFilePath;

	public String fileName;

	public String filePath;

	public long fileSize;

	public boolean isDir;

	public int count;

	/*
	 * 转换成字符串记得乘以1000L Date dataDate = new Date(file.modifiedDate*1000L);
	 * SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
	 * Log.i("date", formater.format(dataDate));
	 */
	// measured in milliseconds since January 1st, 1970
	public long createDate;
	// measured in milliseconds since January 1st, 1970
	public long modifiedDate;

	public boolean selected;

	public boolean canRead;

	public boolean canWrite;
	/**
	 * 该文件的属性是否为隐藏
	 */
	public boolean isHidden;

	public int dbId; // id in the database, if is from database

	public String mimeType;
	/**
	 * 别名,需要一个别名用来作为显示名
	 */
	public String alias = "";
	public String uri = "";
	public int thumbnailId;
	public String thumbnailPath = null;
	/**
	 * 是否需要隐藏起来
	 */
	public boolean needHide = false;

	public boolean init() {
		File lFile = new File(filePath);
		if (!lFile.exists()) {
			return false;
		}

		canRead = lFile.canRead();
		canWrite = lFile.canWrite();
		isHidden = lFile.isHidden();
		fileName = MediaFileUtil.getNameFromFilepath(filePath);
		modifiedDate = lFile.lastModified();
		isDir = lFile.isDirectory();
		fileSize = lFile.length();
		fullFilePath = lFile.getAbsolutePath();
		return true;
	}

	public boolean init(Cursor cur) {
		fullFilePath = cur.getString(cur.getColumnIndex(MediaColumns.DATA));
		if (!MediaFileUtil.isFileExist(fullFilePath) || MediaFileUtil.getFileSize(fullFilePath) == 0) {
			return false;
		}
		filePath = MediaFileUtil.getPathFromFilepath(fullFilePath);
		fileName = cur.getString(cur.getColumnIndex(MediaColumns.DISPLAY_NAME));
		if (fileName == null) {
			fileName = "unknown";
		}
		dbId = cur.getInt(cur.getColumnIndex(BaseColumns._ID));
		fileSize = cur.getInt(cur.getColumnIndex(MediaColumns.SIZE));
		createDate = cur.getLong(cur.getColumnIndex(MediaColumns.DATE_ADDED));
		modifiedDate = cur.getLong(cur.getColumnIndex(MediaColumns.DATE_MODIFIED));
		mimeType = cur.getString(cur.getColumnIndex(MediaColumns.MIME_TYPE));
		return true;
	}

	public static FileInfo getFileInfo(String filePath) {
		File lFile = new File(filePath);
		if (!lFile.exists()) {
			return null;
		}

		FileInfo lFileInfo = new FileInfo();
		lFileInfo.canRead = lFile.canRead();
		lFileInfo.canWrite = lFile.canWrite();
		lFileInfo.isHidden = lFile.isHidden();
		lFileInfo.fileName = MediaFileUtil.getNameFromFilepath(filePath);
		lFileInfo.modifiedDate = lFile.lastModified();
		lFileInfo.isDir = lFile.isDirectory();
		lFileInfo.filePath = filePath;
		lFileInfo.fileSize = lFile.length();
		lFileInfo.fullFilePath = lFile.getAbsolutePath();
		return lFileInfo;
	}

	public static FileInfo getFileInfo(File f, FilenameFilter filter, boolean showHidden) {
		FileInfo lFileInfo = new FileInfo();
		String filePath = f.getPath();
		File lFile = new File(filePath);
		lFileInfo.canRead = lFile.canRead();
		lFileInfo.canWrite = lFile.canWrite();
		lFileInfo.isHidden = lFile.isHidden();
		lFileInfo.fileName = f.getName();
		lFileInfo.modifiedDate = lFile.lastModified();
		lFileInfo.isDir = lFile.isDirectory();
		lFileInfo.filePath = filePath;
		lFileInfo.fullFilePath = f.getAbsolutePath();
		if (lFileInfo.isDir) {
			int lCount = 0;
			File[] files = lFile.listFiles(filter);

			// null means we cannot access this dir
			if (files == null) {
				return null;
			}

			for (File child : files) {
				if ((!child.isHidden() || showHidden)
						&& MediaFileUtil.isNormalFile(child.getAbsolutePath())) {
					lCount++;
				}
			}
			lFileInfo.count = lCount;

		} else {

			lFileInfo.fileSize = lFile.length();

		}
		return lFileInfo;
	}

	public String getTitle() {
		return alias;
	}

	public long getModifiedDate() {
		return modifiedDate;
	}
}
