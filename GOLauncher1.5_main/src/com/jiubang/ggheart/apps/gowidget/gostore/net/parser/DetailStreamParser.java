package com.jiubang.ggheart.apps.gowidget.gostore.net.parser;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.BaseBean;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.DetailElementBean;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.DetailElementBean.DetailElement;

/**
 * 
 * <br>类描述:详情界面字节解释
 * <br>功能详细描述:
 * 
 * @author  zhouxuewen
 * @date  [2012-9-12]
 */
public class DetailStreamParser extends HttpStreamParser {

	@Override
	public BaseBean parseHttpStreamData(DataInputStream dis) {
		DetailElementBean detailElementBean = new DetailElementBean();
		try {
			detailElementBean.mLength = dis.readInt(); // 下发的数据长度
			detailElementBean.mTimeStamp = dis.readLong(); // 下发的数据时间戳
			detailElementBean.mId = dis.readInt();
			String paytypes = dis.readUTF();
			if (paytypes != null) {
				detailElementBean.mPaytype = paytypes.split("#");
			}
			detailElementBean.mPayid = dis.readUTF();
			detailElementBean.mZipDownurl = dis.readUTF();
			detailElementBean.mLocker = dis.readUTF();
			detailElementBean.mWidget = dis.readUTF();
			detailElementBean.mSource = dis.readInt();
			detailElementBean.mCallbackUrl = dis.readUTF();
			detailElementBean.mIsHot = dis.readByte();
			detailElementBean.mIsNew = dis.readByte();
			detailElementBean.mStar = dis.readUTF();
			detailElementBean.mIcon = dis.readUTF();
			detailElementBean.mName = dis.readUTF();
			detailElementBean.mSize = dis.readUTF();
			detailElementBean.mVersion = dis.readUTF();
			detailElementBean.mVersionCode = dis.readInt();
			detailElementBean.mPkgName = dis.readUTF();
			detailElementBean.mUpdatetime = dis.readUTF();
			detailElementBean.mPrice = dis.readUTF();
			detailElementBean.mSupport = dis.readUTF();
			detailElementBean.mDevelop = dis.readUTF();
			detailElementBean.mSummary = dis.readUTF();
			detailElementBean.mDetail = dis.readUTF();
			detailElementBean.mUpdatelog = dis.readUTF();
			detailElementBean.mImgids = new ArrayList<String>();
			String imgidsString = dis.readUTF();
			String[] imgids = imgidsString.split("#");
			int length = imgids.length;
			for (int i = 0; i < length; i++) {
				detailElementBean.mImgids.add(imgids[i]);
			}
			detailElementBean.mBigImgIds = new ArrayList<String>();
			String bigImgidsString = dis.readUTF();
			String[] bigImgids = bigImgidsString.split("#");
			length = bigImgids.length;
			for (int i = 0; i < length; i++) {
				detailElementBean.mBigImgIds.add(bigImgids[i]);
			}
			detailElementBean.mDownurl = dis.readUTF();
			detailElementBean.mMarketurl = dis.readUTF();
			detailElementBean.mOtherurl = dis.readUTF();
			detailElementBean.mDownloadCount = dis.readUTF();

			try {
				detailElementBean.mRecomId = dis.readInt();
				int count = dis.readInt();
				detailElementBean.mRecomCount = count;
				detailElementBean.mElementsList = new ArrayList<DetailElement>(count);
				for (int i = 0; i < count; i++) {
					DetailElement element = detailElementBean.new DetailElement();
					// 首先读出数据类型
					byte type = dis.readByte();
					// 是否热门
					element.mIsHot = dis.readByte();
					// 是否新应用
					element.mIsNew = dis.readByte();
					// 星级
					element.mStar = dis.readUTF();
					// 应用iconID
					element.mLogoIconId = dis.readUTF();
					element.mId = dis.readInt();
					String paytypes2 = dis.readUTF();
					if (paytypes2 != null && !paytypes.equals("")) {
						element.mPaytype = paytypes2.split("#");
					}
					element.mPayid = dis.readUTF();
					element.mDownurl = dis.readUTF();
					element.mLocker = dis.readUTF();
					element.mWidget = dis.readUTF();
					element.mSource = dis.readInt();
					element.mCallbackUrl = dis.readUTF();
					element.mName = dis.readUTF();
					element.mType = dis.readUTF();
					element.mDetail = dis.readUTF();
					element.mVersion = dis.readUTF();
					element.mVersionCode = dis.readInt();
					element.mPkgName = dis.readUTF();
					element.mPrice = dis.readUTF();
					element.mSize = dis.readUTF();
					int urlCount = dis.readInt();
					element.mUrlNum = urlCount;
					if (urlCount > 0) {
						element.mUrlMap = new HashMap<Integer, String>(urlCount);
						for (int j = 0; j < urlCount; j++) {
							int urlKey = dis.readInt(); // urlkey值
							String url = dis.readUTF(); // url值
							element.mUrlMap.put(urlKey, url);
						}
					}

					element.mImgId = dis.readUTF();
					element.mUpdateTime = dis.readUTF();
					element.mDownloadCount = dis.readUTF();
					detailElementBean.mElementsList.add(element);
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return detailElementBean;
	}

}
