package com.go.util.file.media;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 
 * @author 
 * @version 
 * 专辑信息封装类
 */
public class Category extends FileInfo {
	// files
	public ArrayList<FileInfo> files = new ArrayList<FileInfo>();
	// Category size
	public long size = 0;
	private HashMap<String, FileInfo> fileMap = new HashMap<String, FileInfo>();

	public boolean addFile(FileInfo info) {
		if (null == info || contains(info.fullFilePath)) {
			return false;
		}

		files.add(info);
		fileMap.put(info.fullFilePath, info);
		size += info.fileSize;
		return true;
	}

	public void deleteFile(String path) {
		// for (FileInfo info:files) {
		// if (info.fullFilePath.equals(path)) {
		// size -= info.fileSize;
		// files.remove(info);
		// break;
		// }
		// }

		if (contains(path)) {
			FileInfo fileInfo = getFile(path);
			files.remove(fileInfo);
			fileMap.remove(path);
			size -= fileInfo.fileSize;
		}
	}

	public boolean contains(String path) {
		return fileMap.containsKey(path);
	}

	public FileInfo getFile(String path) {
		return fileMap.get(path);
	}

	public void reset() {
		files.clear();
		fileMap.clear();
		size = 0;
		thumbnailPath = null;
	}
	
	public Category clone() {
		Category category = new Category();
		category.alias = alias;
		category.canRead = canRead;
		category.canWrite = canWrite;
		category.count = count;
		category.createDate = createDate;
		category.dbId = dbId;
		category.fileMap = fileMap;
		category.fileName = fileName;
		category.filePath = filePath;
		category.files = files;
		category.fileSize = fileSize;
		category.fullFilePath = fullFilePath;
		category.isDir = isDir;
		category.isHidden = isHidden;
		category.mimeType = mimeType;
		category.modifiedDate = modifiedDate;
		category.needHide = needHide;
		category.selected = selected;
		category.size = size;
		category.thumbnailId = thumbnailId;
		category.thumbnailPath = thumbnailPath;
		category.uri = uri;
		return category;
	}
}
