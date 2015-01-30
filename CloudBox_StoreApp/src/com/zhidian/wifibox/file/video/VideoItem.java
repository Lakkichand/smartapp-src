package com.zhidian.wifibox.file.video;

import java.io.Serializable;

/**
 * 
 * 所有视频字段
 * 
 * @author shihuajian
 *
 */

public class VideoItem implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String id;
	private String data;
	private String filePath;
	private String displayName;
	private int size;
	private int position;
	private String mimeType;
	private String dateAdded;
	private String dateModified;
	private String title;
	private int duration;
	private String artist;
	private String album;
	private String resolution;
	private int datetaken;
	private String miniThumbMagic;
	private String bucketId;
	private String bucketDisplayName;
	private String width;
	private String height;
	private boolean isSelected = false;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
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
	public String getMimeType() {
		return mimeType;
	}
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	public String getDateAdded() {
		return dateAdded;
	}
	public void setDateAdded(String dateAdded) {
		this.dateAdded = dateAdded;
	}
	public String getDateModified() {
		return dateModified;
	}
	public void setDateModified(String dateModified) {
		this.dateModified = dateModified;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
	public String getArtist() {
		return artist;
	}
	public void setArtist(String artist) {
		this.artist = artist;
	}
	public String getAlbum() {
		return album;
	}
	public void setAlbum(String album) {
		this.album = album;
	}
	public String getResolution() {
		return resolution;
	}
	public void setResolution(String resolution) {
		this.resolution = resolution;
	}
	public int getDatetaken() {
		return datetaken;
	}
	public void setDatetaken(int datetaken) {
		this.datetaken = datetaken;
	}
	public String getMiniThumbMagic() {
		return miniThumbMagic;
	}
	public void setMiniThumbMagic(String miniThumbMagic) {
		this.miniThumbMagic = miniThumbMagic;
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
	public String getWidth() {
		return width;
	}
	public void setWidth(String width) {
		this.width = width;
	}
	public String getHeight() {
		return height;
	}
	public void setHeight(String height) {
		this.height = height;
	}
	public boolean getIsSelected() {
		return isSelected;
	}
	public void setIsSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}
	
	@Override
	public String toString() {
		return "VideoItem [id=" + id + ", data=" + data + ", filePath="
				+ filePath + ", displayName=" + displayName + ", size=" + size
				+ ", mimeType=" + mimeType + ", dateAdded=" + dateAdded
				+ ", dateModified=" + dateModified + ", title=" + title
				+ ", duration=" + duration + ", artist=" + artist + ", album="
				+ album + ", resolution=" + resolution + ", datetaken="
				+ datetaken + ", miniThumbMagic=" + miniThumbMagic
				+ ", bucketId=" + bucketId + ", bucketDisplayName="
				+ bucketDisplayName + ", width=" + width + ", height=" + height
				+ ", isSelected=" + isSelected + "]";
	}

}
