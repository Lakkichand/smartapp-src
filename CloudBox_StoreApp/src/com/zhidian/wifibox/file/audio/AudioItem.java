package com.zhidian.wifibox.file.audio;

import java.io.Serializable;

public class AudioItem implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String id;
	private String data;
	private String displayName;
	private String filePath;
	private int size;
	private String mimeType;
	private String dateAdded;
	private String dateModified;
	private String title;
	private String titleKey;
	private int duration;
	private String artistId;
	private String albumId;
	private String track;
	private String year;
	private String bucketId;
	private String bucketDisplayName;
	private String titlePinYinKey;
	private String artistKey;
	private String artist;
	private String artistPingYinKey;
	private String albumKey;
	private String album;
	private String albumPinYinKey;
	public boolean isSelected = false;
	
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
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
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
	public String getTitleKey() {
		return titleKey;
	}
	public void setTitleKey(String titleKey) {
		this.titleKey = titleKey;
	}
	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
	public String getArtistId() {
		return artistId;
	}
	public void setArtistId(String artistId) {
		this.artistId = artistId;
	}
	public String getAlbumId() {
		return albumId;
	}
	public void setAlbumId(String albumId) {
		this.albumId = albumId;
	}
	public String getTrack() {
		return track;
	}
	public void setTrack(String track) {
		this.track = track;
	}
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
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
	public String getTitlePinYinKey() {
		return titlePinYinKey;
	}
	public void setTitlePinYinKey(String titlePinYinKey) {
		this.titlePinYinKey = titlePinYinKey;
	}
	public String getArtistKey() {
		return artistKey;
	}
	public void setArtistKey(String artistKey) {
		this.artistKey = artistKey;
	}
	public String getArtist() {
		return artist;
	}
	public void setArtist(String artist) {
		this.artist = artist;
	}
	public String getArtistPingYinKey() {
		return artistPingYinKey;
	}
	public void setArtistPingYinKey(String artistPingYinKey) {
		this.artistPingYinKey = artistPingYinKey;
	}
	public String getAlbumKey() {
		return albumKey;
	}
	public void setAlbumKey(String albumKey) {
		this.albumKey = albumKey;
	}
	public String getAlbum() {
		return album;
	}
	public void setAlbum(String album) {
		this.album = album;
	}
	public String getAlbumPinYinKey() {
		return albumPinYinKey;
	}
	public void setAlbumPinYinKey(String albumPinYinKey) {
		this.albumPinYinKey = albumPinYinKey;
	}
	public boolean isSelected() {
		return isSelected;
	}
	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}
	
	@Override
	public String toString() {
		return "AudioItem [id=" + id + ", data=" + data + ", displayName="
				+ displayName + ", filePath=" + filePath + ", size=" + size
				+ ", mimeType=" + mimeType + ", dateAdded=" + dateAdded
				+ ", dateModified=" + dateModified + ", title=" + title
				+ ", titleKey=" + titleKey + ", duration=" + duration
				+ ", artistId=" + artistId + ", albumId=" + albumId
				+ ", track=" + track + ", year=" + year + ", bucketId="
				+ bucketId + ", bucketDisplayName=" + bucketDisplayName
				+ ", titlePinYinKey=" + titlePinYinKey + ", artistKey="
				+ artistKey + ", artist=" + artist + ", artistPingYinKey="
				+ artistPingYinKey + ", albumKey=" + albumKey + ", album="
				+ album + ", albumPinYinKey=" + albumPinYinKey + "]";
	}
	

}
