package com.zhidian.wifibox.file.other;

import java.io.Serializable;

public class OtherItem implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int id;
	private String data;
	private int size;
	private int position;
	private int  format;
	private int parent;
	private int dateAdded;
	private int dateModified;
	private String mimeType;
	private String displayName;
	private String title;
	private String bucketId;
	private String bucketDisplayName;
	private String fileName;
	private String fileType;
	public boolean isSelected = false;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
	public int getFormat() {
		return format;
	}
	public void setFormat(int format) {
		this.format = format;
	}
	public int getParent() {
		return parent;
	}
	public void setParent(int parent) {
		this.parent = parent;
	}
	public int getDateAdded() {
		return dateAdded;
	}
	public void setDateAdded(int dateAdded) {
		this.dateAdded = dateAdded;
	}
	public int getDateModified() {
		return dateModified;
	}
	public void setDateModified(int dateModified) {
		this.dateModified = dateModified;
	}
	public String getMimeType() {
		return mimeType;
	}
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getBucketId() {
		return bucketId;
	}
	public void setBucketId(String bucketId) {
		this.bucketId = bucketId;
	}
	public String getBucketDisplayName() {
		return bucketDisplayName;
	}
	public void setBucketDisplayName(String bucketDisplayName) {
		this.bucketDisplayName = bucketDisplayName;
	}
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
	public boolean getIsSelected() {
		return isSelected;
	}
	public void setIsSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}
	
	@Override
	public String toString() {
		return "OtherItem [id=" + id + ", data=" + data + ", size=" + size
				+ ", format=" + format + ", parent=" + parent + ", dateAdded="
				+ dateAdded + ", dateModified=" + dateModified + ", mimeType="
				+ mimeType + ", title=" + title + ", bucketId=" + bucketId
				+ ", bucketDisplayName=" + bucketDisplayName + ", fileName="
				+ fileName + ", fileType=" + fileType + "]";
	}

}
