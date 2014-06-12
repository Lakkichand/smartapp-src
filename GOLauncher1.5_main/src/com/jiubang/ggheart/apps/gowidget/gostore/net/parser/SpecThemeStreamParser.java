/*
 * 文 件 名:  SpecThemeStreamParser.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  rongjinsong
 * 修改时间:  2012-10-18
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.apps.gowidget.gostore.net.parser;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.go.util.ConvertUtils;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.BaseBean;
import com.jiubang.ggheart.data.theme.bean.ThemeSpecDataBean;
import com.jiubang.ggheart.data.theme.bean.ThemeSpecDataBean.FeaturedElement;

/**
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  rongjinsong
 * @date  [2012-10-18]
 */
public class SpecThemeStreamParser extends HttpStreamParser {

	/** {@inheritDoc} */

	@Override
	public BaseBean parseHttpStreamData(DataInputStream dis) {
		// TODO Auto-generated method stub
		ThemeSpecDataBean bean = new ThemeSpecDataBean();
		try {
			bean.mLength = dis.readInt();
			if (bean.mLength > 0) {
				bean.mTimeStamp = dis.readLong();
				bean.mId = dis.readInt();
				bean.mName = dis.readUTF();
				bean.mStylepack = dis.readUTF();
				bean.mTotalNum = dis.readInt();
				bean.mCurrentPage = dis.readInt();
				bean.mTotalPage = dis.readInt();
				bean.mElementCount = dis.readInt();
				bean.mElementsList = new ArrayList<ThemeSpecDataBean.FeaturedElement>();
				for (int i = 0; i < bean.mElementCount; i++) {
					FeaturedElement element = bean.new FeaturedElement();
					parseitem(dis, element);
					element.mSortId = bean.mId;
					bean.mElementsList.add(element);
				}

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bean;
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
			element.mSource = dis.readInt();
			element.mIconUrls = getImageUrlArray(dis.readUTF());
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
		if (pay != null && !pay.equals("")) {
			String[] types = pay.split("#");
			if (types != null && types.length > 0) {
				return Arrays.asList(types);
			}
		}
		return null;
	}
	
	private List<String> getImageUrlArray(String urlString) {
		if (urlString == null || urlString.equals("")) {
			return null;
		}

		String[] urls = urlString.split("#");
		if (urls != null && urls.length > 0) {
			return Arrays.asList(urls);
		}
		return null;
	}
}
