/*
 * 文 件 名:  FeaturedThemeDetailStreamParser.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  rongjinsong
 * 修改时间:  2012-8-21
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.data.theme.parser;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.go.util.ConvertUtils;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.BaseBean;
import com.jiubang.ggheart.apps.gowidget.gostore.net.parser.HttpStreamParser;
import com.jiubang.ggheart.data.theme.bean.FeaturedThemeDetailBean;

/**
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  rongjinsong
 * @date  [2012-8-21]
 */
public class FeaturedThemeDetailStreamParser extends HttpStreamParser {

	/** {@inheritDoc} */

	@Override
	public BaseBean parseHttpStreamData(DataInputStream dis) {
		// TODO Auto-generated method stub
		FeaturedThemeDetailBean bean = new FeaturedThemeDetailBean();
		try {
			bean.mLength = dis.readInt();
			bean.mTimeStamp = dis.readLong();
			bean.mId = dis.readInt();
			bean.mPayType = getPayType(dis.readUTF());
			bean.mPayId = dis.readUTF();
			bean.mDownurl = dis.readUTF();
			bean.mMlocker = dis.readUTF();
			bean.mMwidget = dis.readUTF();
			bean.mIsAll = ConvertUtils.int2boolean(dis.readInt());
			bean.mIsNew = dis.readByte();
			bean.mStar = Integer.valueOf(dis.readUTF());
			bean.mIconImgSource = dis.readInt();
			bean.mIconImgUrl = getImageUrlArray(dis.readUTF());
			bean.mIcon = dis.readUTF();
			bean.mName = dis.readUTF();
			bean.mSize = dis.readUTF();
			bean.mVersion = dis.readUTF();
			bean.mVersionNum = dis.readInt();
			bean.mPackageName = dis.readUTF();
			bean.mUpdateTime = dis.readUTF();
			bean.mFeeType = dis.readInt();
			bean.mPrice = dis.readUTF();
			bean.mSupport = dis.readUTF();
			bean.mDevelop = dis.readUTF();
			bean.mSummary = dis.readUTF();
			bean.mDetail = dis.readUTF();
			bean.mUpdateLog = dis.readUTF();
			bean.mImgSource = dis.readInt();
			bean.mImgUrl = getImageUrlArray(dis.readUTF());
			bean.mImgIds = dis.readUTF();
			bean.mBigImgSource = dis.readInt();
			bean.mBigImgUrl = getImageUrlArray(dis.readUTF());
			bean.mBigimgids = dis.readUTF();
			bean.mDownloadSize = dis.readUTF();
			bean.mUrlNum = dis.readInt();
			if (bean.mUrlNum > 0) {
				bean.mUrlMap = new HashMap<Integer, String>(bean.mUrlNum);
				for (int j = 0; j < bean.mUrlNum; j++) {
					int urlKey = dis.readInt(); // urlkey
					String url = dis.readUTF(); // url值
					bean.mUrlMap.put(urlKey, url);
				}
			}
			bean.mVimgUrl = dis.readUTF();
			bean.mVurl = dis.readUTF();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bean;
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
			return  Arrays.asList(urls);
		}
		return null;
	}
}
