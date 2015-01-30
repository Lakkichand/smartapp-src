package com.zhidian.wifibox.file.video;

import java.util.List;

import android.provider.MediaStore;

/**
 * 
 * 按目录分类
 * 
 * @author shihuajian
 *
 */

public class VideoBucket {
	public int count = 0;
	public String bucketName;
	public List<VideoItem> videoList;
	
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public String getBucketName() {
		return bucketName;
	}
	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}
	public List<VideoItem> getImageList() {
		return videoList;
	}
	public void setImageList(List<VideoItem> imageList) {
		this.videoList = imageList;
	}
	
	@Override
	public String toString() {
		return "VideoBucket [count=" + count + ", bucketName=" + bucketName
				+ ", imageList=" + videoList + "]";
	}

}
