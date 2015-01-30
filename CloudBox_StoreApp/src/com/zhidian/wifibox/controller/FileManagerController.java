package com.zhidian.wifibox.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.trinea.android.common.util.StringUtils;

import com.ta.mvc.command.TACommand;
import com.ta.mvc.common.TARequest;
import com.zhidian.wifibox.file.album.AlbumHelper;
import com.zhidian.wifibox.file.album.ImageBucket;
import com.zhidian.wifibox.file.album.ImageItem;
import com.zhidian.wifibox.file.album.ImageItemGroup;
import com.zhidian.wifibox.file.audio.AudioHelper;
import com.zhidian.wifibox.file.audio.MusicData;
import com.zhidian.wifibox.file.other.OtherHelper;
import com.zhidian.wifibox.file.other.OtherItem;
import com.zhidian.wifibox.file.video.VideoHelper;
import com.zhidian.wifibox.file.video.VideoItem;
import com.zhidian.wifibox.util.TimeTool;

public class FileManagerController extends TACommand {
	
	private final static String TAG = FileManagerController.class.getSimpleName();
	
	/** 获取音乐文件 */
	public final static String GET_FILE_MUSIC = "get_file_music";
	
	/** 获取图片文件 */
	public final static String GET_FILE_PICTURE = "get_file_picture";
	
	/** 获取视频文件 */
	public final static String GET_FILE_VIDEO = "get_file_video";
	
	/** 获取其他文件 */
	public final static String GET_FILE_OTHER = "get_file_other";
	
	/** 获取图片文件详细 */
	public final static String GET_FILE_PICTURE_DETAIL = "get_file_picture_detail";
	
	public final static String FLAG_HELPER = "helper";
	public final static String FLAG_BUCKET_ID = "bucketId";
	public final static String FLAG_IS_SELECT = "isSelect";
	public final static String FLAG_DATA_LIST = "dataList";
	public final static String FLAG_CATEGORY_DATA = "categoryData";

	@Override
	protected void executeCommand() {
		TARequest request = getRequest();
		String commad = (String) request.getTag();
		
		if (commad.equals(GET_FILE_MUSIC)) {					
			// 获取音乐文件
			AudioHelper helper = (AudioHelper) request.getData();
			List<MusicData> data = helper.getMusicFileList();
			FileManagerController.this.sendSuccessMessage(data);
			
		} else if (commad.equals(GET_FILE_PICTURE)) {			
			// 获取图片文件
			AlbumHelper helper = (AlbumHelper) request.getData();
			List<ImageBucket> bucket = helper.getImagesBucketList(true);
			FileManagerController.this.sendSuccessMessage(bucket);
			
		} else if (commad.equals(GET_FILE_VIDEO)) {				
			// 获取视频文件
			VideoHelper helper = (VideoHelper) request.getData();
			List<VideoItem> bucket = helper.getVideoBucketList(true);
			FileManagerController.this.sendSuccessMessage(bucket);
			
		} else if (commad.equals(GET_FILE_OTHER)) {				
			// 获取其他文件
			OtherHelper helper = (OtherHelper) request.getData();
			List<OtherItem> bucket = helper.getOtherBucketList(true);
			FileManagerController.this.sendSuccessMessage(bucket);
			
		} else if (commad.equals(GET_FILE_PICTURE_DETAIL)) {	
			// 获取图片文件详细
			Map<String, Object> map = (Map<String, Object>) request.getData();
			AlbumHelper help = (AlbumHelper) map.get(FLAG_HELPER);
			String bucketId = (String) map.get(FLAG_BUCKET_ID); 
			boolean isSelect = (Boolean) map.get(FLAG_IS_SELECT);
			List<ImageItem> bucket = help.getImageItemList(bucketId, isSelect);
			List<ImageItemGroup> mCategoryData = new ArrayList<ImageItemGroup>();
			allocationData(bucket, mCategoryData, isSelect);
			Map<String, Object> result = new HashMap<String, Object>();
			result.put(FLAG_DATA_LIST, bucket);
			result.put(FLAG_CATEGORY_DATA, mCategoryData);
			FileManagerController.this.sendSuccessMessage(result);
			
		}

	}
	
	/** 按天把数据分类 */
	public void allocationData(List<ImageItem> dataList, List<ImageItemGroup> mCategoryData, boolean isSelect) {
		if (dataList != null && dataList.size() != 0) {
			String time = null;
			List<ImageItem> itemData = new ArrayList<ImageItem>();
			for (int i = 0; i < dataList.size(); i++) {
				ImageItem item = dataList.get(i);
				if (StringUtils.isEmpty(time)) {
					time = TimeTool.timestampToString(item.getDateTaken() + "");
					itemData.add(item);
				} else {
					String temp = TimeTool.timestampToString(item.getDateTaken() + "");
					if (time.equals(temp)) {
						time = temp;
						itemData.add(item);
					} else {
						ImageItemGroup group = new ImageItemGroup();
						group.setGroup(time);
						group.setChildren(itemData);
						group.setCount(itemData.size());
						group.setIsSelected(isSelect);
						mCategoryData.add(group);
						time = TimeTool.timestampToString(item.getDateTaken() + "");
						itemData = null;
						itemData = new ArrayList<ImageItem>();
						itemData.add(item);
					}
				}
				if ((i + 1) == dataList.size()) {
					ImageItemGroup group = new ImageItemGroup();
					group.setGroup(time);
					group.setChildren(itemData);
					group.setCount(itemData.size());
					group.setIsSelected(isSelect);
					mCategoryData.add(group);
					time = null;
					itemData = null;
				}
				
			}
		}
	}

}
