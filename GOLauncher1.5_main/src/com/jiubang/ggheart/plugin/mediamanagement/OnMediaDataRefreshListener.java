package com.jiubang.ggheart.plugin.mediamanagement;

import java.util.ArrayList;

import com.go.util.file.media.FileInfo;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  yangguanxiang
 * @date  [2012-11-16]
 */
public interface OnMediaDataRefreshListener {
	public void onImageDataRefreshed(ArrayList<FileInfo> dataList);
	public void onMusicDataRefreshed(ArrayList<FileInfo> dataList);
	public void onVideoDataRefreshed(ArrayList<FileInfo> dataList);
}
