package com.jiubang.ggheart.apps.gowidget.gostore.net.parser;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.BaseBean;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.ImagesBean;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.ImagesBean.ImageData;

/**
 * 图片流解析器
 * 
 * @author huyong
 * 
 */
public class ImageStreamParser extends HttpStreamParser {

	@Override
	public BaseBean parseHttpStreamData(DataInputStream dis) {

		ImagesBean imagesBean = new ImagesBean();
		try {
			imagesBean.mLength = dis.readInt(); // 下发的数据长度
			imagesBean.mTimeStamp = dis.readLong(); // 下发的数据时间戳
			int count = dis.readInt();
			imagesBean.mImgNum = count; // 下发的图片数量
			// 图片数据列表
			imagesBean.mImageList = new ArrayList<ImagesBean.ImageData>(count);
			for (int i = 0; i < count; i++) {
				ImageData imageData = imagesBean.new ImageData();
				imageData.mImgId = dis.readUTF(); // 图片id
				int dataLen = dis.readInt();
				imageData.mDataLength = dataLen; // 图片数据长度
				imageData.mImgData = new byte[dataLen];
				dis.readFully(imageData.mImgData); // 图片数据

				imagesBean.mImageList.add(imageData);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return imagesBean;
	}

}
