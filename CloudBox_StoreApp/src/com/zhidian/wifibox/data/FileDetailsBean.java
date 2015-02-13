package com.zhidian.wifibox.data;

import java.io.Serializable;

public class FileDetailsBean implements Serializable {

	private static final long serialVersionUID = 6106386946309890845L;
	
	private String fileName;
	private String fileType;
	private int fileSize;
	private int fileDatetaken;
	private String filePath;
	
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getFileType() {
		return fileType;
	}
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
	public int getFileSize() {
		return fileSize;
	}
	public void setFileSize(int fileSize) {
		this.fileSize = fileSize;
	}
	public int getFileDatetaken() {
		return fileDatetaken;
	}
	public void setFileDatetaken(int fileDatetaken) {
		this.fileDatetaken = fileDatetaken;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	@Override
	public String toString() {
		return "FileDetailsBean [fileName=" + fileName + ", fileType="
				+ fileType + ", fileSize=" + fileSize + ", fileDatetaken="
				+ fileDatetaken + ", filePath=" + filePath + "]";
	}

}
