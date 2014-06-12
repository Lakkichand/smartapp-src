package com.jiubang.ggheart.apps.gowidget.gostore.net.databean;

import java.util.ArrayList;

/**
 * 图片数据
 * 
 * @author huyong
 * 
 */
public class ImagesBean extends BaseBean {

	public int mImgNum = 0; // 下发的图片数量

	public ArrayList<ImageData> mImageList; // 图片数据列表

	public class ImageData {
		public String mImgId = null; // 图片id
		public int mDataLength = 0; // 图片数据长度
		public byte[] mImgData = null; // 图片数据

		public ImageData() {
			// TODO Auto-generated constructor stub
		}
	}
}
