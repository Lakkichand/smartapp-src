package com.jiubang.ggheart.data.theme.adrecommend;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import android.graphics.BitmapFactory;

import com.gau.utils.net.operator.StreamHttpOperator;
import com.gau.utils.net.request.THttpRequest;
import com.gau.utils.net.response.BasicResponse;
import com.gau.utils.net.response.IResponse;
import com.jiubang.ggheart.data.theme.adrecommend.AdHttpAdapter.AdResponseData;

/**
 * 广告协议数据处理
 * 
 * @author HuYong
 * @version 1.0
 */
public class AdDataHttpOperator extends StreamHttpOperator {

	@Override
	public IResponse operateHttpResponse(THttpRequest request, HttpResponse response)
			throws IllegalStateException, IOException {
		HttpEntity entity = response.getEntity();
		InputStream ins = entity.getContent();
		AdResponseData adResponse = praseHttpStreamData(ins);
		BasicResponse result = new BasicResponse(IResponse.RESPONSE_TYPE_STREAM, adResponse);
		return result;
	}

	/**
	 * 解析http流数据
	 * 
	 * @param inStream
	 */
	private AdResponseData praseHttpStreamData(final InputStream inStream) {
		AdResponseData adResponseData = new AdResponseData();
		ArrayList<AdElement> adList = new ArrayList<AdElement>();
		try {
			DataInputStream dis = new DataInputStream(inStream);
			adResponseData.mMaxAdId = dis.readInt();
			int adCnt = dis.readInt();
			adResponseData.mAdCount = adCnt;
			for (int i = 0; i < adCnt; i++) {
				AdElement adElement = new AdElement();
				adElement.mAdName = dis.readUTF(); // 广告名
				adElement.mAdID = dis.readInt(); // 广告id
				adElement.mAppID = dis.readUTF(); // 软件uid(此处读取的是编辑广告信息的广告UID字段的值)
				adElement.mIconFormat = dis.readUTF(); // 图片类型
				// 图片数据
				int logoDataLen = dis.readInt();
				byte[] tmpIconData = new byte[logoDataLen];
				dis.readFully(tmpIconData);
				adElement.mIcon = BitmapFactory.decodeByteArray(tmpIconData, 0, logoDataLen);
				tmpIconData = null;
				adElement.mAdText = dis.readUTF(); // 广告内容
				adElement.mMaxDisplayCount = dis.readInt(); // 当此最多显示次数
				adElement.mDelay = dis.readInt(); // 弹出间隔
				adElement.mAdOptCode = dis.readInt(); // 操作码
				adElement.mSrcSize = dis.readInt(); // 供下载的资源大小
				adElement.mAdOptData = dis.readUTF(); // 操作数
				adList.add(adElement);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		adResponseData.mAdList = adList;
		return adResponseData;
	}
}
