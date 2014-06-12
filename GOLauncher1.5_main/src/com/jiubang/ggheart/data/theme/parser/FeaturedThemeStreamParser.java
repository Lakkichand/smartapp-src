package com.jiubang.ggheart.data.theme.parser;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.go.util.ConvertUtils;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.BaseBean;
import com.jiubang.ggheart.apps.gowidget.gostore.net.parser.HttpStreamParser;
import com.jiubang.ggheart.data.theme.bean.FeaturedDataListBean;
import com.jiubang.ggheart.data.theme.bean.FeaturedDataListBean.FeaturedElement;
/**
 * 
 * <br>类描述:精品主题推荐数据解析
 * <br>功能详细描述:
 * 
 * @author  rongjinsong
 * @date  [2012-9-26]
 */

public class FeaturedThemeStreamParser extends HttpStreamParser {
	@Override
	public BaseBean parseHttpStreamData(DataInputStream dis) {
		// TODO Auto-generated method stub
		FeaturedDataListBean listElementBean = new FeaturedDataListBean();
		try {
			listElementBean.mLength = dis.readInt(); // 下发的数据长度
			listElementBean.mTimeStamp = dis.readLong(); // 下发的数据时间戳
			listElementBean.mRetryPayTimes = dis.readInt();
			if (listElementBean.mLength == 0) {
				// 下发数据为0，则直接返回。
				return listElementBean;
			}
			listElementBean.mShowStatusNotify = ConvertUtils.int2boolean(dis.readInt());
			listElementBean.mShowStartTime = dis.readLong();
			listElementBean.mShowEndTime = dis.readLong();
			listElementBean.mShowContent = dis.readUTF();
			listElementBean.mShowIconUrl = dis.readUTF();
			listElementBean.mHasNewTheme = ConvertUtils.int2boolean(dis.readInt());
			listElementBean.mTotalNum = dis.readInt(); // 总结果数量
			listElementBean.mCurrentPage = dis.readInt(); // 当前页码
			listElementBean.mTotalPage = dis.readInt(); // 总页数
			int count = dis.readInt(); // 下发了多少条数据
			listElementBean.mElementCount = count;
			listElementBean.mElementsList = new ArrayList<FeaturedDataListBean.FeaturedElement>(
					count);
			for (int i = 0; i < count; i++) {
				// 软件数据
				FeaturedElement element = listElementBean.new FeaturedElement();
				parseitem(dis, element);
				listElementBean.mElementsList.add(element);

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return listElementBean;
	}

	private void parseitem(DataInputStream dis, FeaturedElement element) {
		try {
			element.mId = dis.readInt();
			element.mIsNew = ConvertUtils.int2boolean(dis.readInt());
			element.mPayType = getPayType(dis.readUTF());
			element.mPayId = dis.readUTF();
			element.mDownurl = dis.readUTF();
			element.mMlocker = dis.readUTF();
			element.mMwidget = dis.readUTF();
			element.mIsall = dis.readInt();
			element.mIssale = dis.readInt();
			element.mName = dis.readUTF();
			element.mImgsource = dis.readInt();
			element.mImgUrlArrary = getImageUrlArray(dis.readUTF());
			element.mImgId = dis.readUTF();
			element.mDetail = dis.readUTF();
			element.mVersion = dis.readUTF();
			element.mVersionCode = dis.readInt();
			element.mPkgName = dis.readUTF();
			element.mFeeType = dis.readInt(); // 19版增加此字段
			element.mPrice = dis.readUTF();
			element.mSize = dis.readUTF();
			element.mUpdateTime = dis.readUTF();
			element.mDownloadCount = dis.readUTF();
			int urlCount = dis.readInt();
			element.mUrlNum = urlCount;
			if (urlCount > 0) {
				element.mUrlMap = new HashMap<Integer, String>(urlCount);
				for (int j = 0; j < urlCount; j++) {
					int urlKey = dis.readInt(); // urlkey
					String url = dis.readUTF(); // url值
					element.mUrlMap.put(urlKey, url);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private List<String> getPayType(String pay) {
		if (pay == null || pay.equals("")) {
			return null;
		}

		String[] types = pay.split("#");
		if (types != null && types.length > 0) {
			return Arrays.asList(types);
		}
		//		int type = 0;
		//		for (int i = 0; i < types.length; i++) {
		//			type |= Integer.valueOf(types[i]);
		//		}
		return null;
	}

	private List<String> getImageUrlArray(String urlString) {
		if (urlString == null || urlString.equals("")) {
			return null;
		}
		ArrayList<String> list;
		String[] urls = urlString.split("#");
		if (urls != null && urls.length > 0) {
			return Arrays.asList(urls);
		}
		return null;
	}
}
