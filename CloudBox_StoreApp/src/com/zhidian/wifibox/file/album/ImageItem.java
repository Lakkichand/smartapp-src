package com.zhidian.wifibox.file.album;

import java.io.Serializable;

public class ImageItem implements Serializable {

	public static final long serialVersionUID = 1L;
	
	private String imageId;
	private String thumbnailPath;
	private String imagePath;
	private String filePath;
	private int dateAdded;
	private int dateModified;
	private int size;
	private String displayName;
	private String bucketDisplayName;
	private String bucketId;
	private String picasaId;
	private String mimeType;
	private String title;
	private String dateTaken;
	private boolean isSelected = false;
	
	public String getImageId() {
		return imageId;
	}
	public void setImageId(String imageId) {
		this.imageId = imageId;
	}
	public String getThumbnailPath() {
		return thumbnailPath;
	}
	public void setThumbnailPath(String thumbnailPath) {
		this.thumbnailPath = thumbnailPath;
	}
	public String getImagePath() {
		return imagePath;
	}
	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
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
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getBucketDisplayName() {
		return bucketDisplayName;
	}
	public void setBucketDisplayName(String bucketDisplayName) {
		this.bucketDisplayName = bucketDisplayName;
	}
	public String getBucketId() {
		return bucketId;
	}
	public void setBucketId(String bucketId) {
		this.bucketId = bucketId;
	}
	public String getPicasaId() {
		return picasaId;
	}
	public void setPicasaId(String picasaId) {
		this.picasaId = picasaId;
	}
	public String getMimeType() {
		return mimeType;
	}
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public boolean getIsSelected() {
		return isSelected;
	}
	public void setIsSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}
	public String getDateTaken() {
		return dateTaken;
	}
	public void setDateTaken(String dateTaken) {
		this.dateTaken = dateTaken;
	}
	
	@Override
	public String toString() {
		return "ImageItem [imageId=" + imageId + ", thumbnailPath="
				+ thumbnailPath + ", imagePath=" + imagePath + ", filePath="
				+ filePath + ", dateAdded=" + dateAdded + ", dateModified="
				+ dateModified + ", size=" + size + ", displayName="
				+ displayName + ", bucketDisplayName=" + bucketDisplayName
				+ ", bucketId=" + bucketId + ", picasaId=" + picasaId
				+ ", mimeType=" + mimeType + ", title=" + title
				+ ", dateTaken=" + dateTaken + ", isSelected=" + isSelected
				+ "]";
	}

}
