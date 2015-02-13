package com.zhidian.wifibox.file.album;

import java.util.List;

/***
 * 
 * 一个目录的相册对象
 * 
 * @author shihuajian
 *
 */

public class ImageBucket {
	public String id;
	public int count = 0;
	public String bucketName;
	public String bucketPath;
	public List<ImageChildItem> imageList;
	public boolean isSelected = false;

}
