package com.jiubang.ggheart.apps.desks.diy.frames.preview;

import java.util.ArrayList;

import android.view.View;

/**
 * 初始化屏幕预览时，屏幕层需传送的消息结构
 * 
 * @author maiyongshen
 * 
 */
public class ScreenPreviewMsgBean {
	public int mainScreenId = -1;
	public int currentScreenId = -1;

	/***
	 * 
	 * <br>类描述:卡片信息
	 * <br>功能详细描述:
	 * 
	 * @author  maxiaojun
	 * @date  [2012-10-9]
	 */
	public static class PreviewImg {
		public View previewView; // 各个屏幕预览图
		public int screenId; // 屏幕ID
		public boolean canDelete = false; // 是否可被删除
	}

	public ArrayList<PreviewImg> screenPreviewList = new ArrayList<PreviewImg>();
}
