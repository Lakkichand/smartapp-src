/*
 * 文 件 名:  BannerStreamParser.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  rongjinsong
 * 修改时间:  2012-10-18
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.data.theme.parser;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.BaseBean;
import com.jiubang.ggheart.apps.gowidget.gostore.net.parser.HttpStreamParser;
import com.jiubang.ggheart.data.theme.bean.ThemeBannerBean;
import com.jiubang.ggheart.data.theme.bean.ThemeBannerBean.BannerElement;

/**
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  rongjinsong
 * @date  [2012-10-18]
 */
public class ThemeBannerStreamParser extends HttpStreamParser {

	/** {@inheritDoc} */

	@Override
	public BaseBean parseHttpStreamData(DataInputStream dis) {
		// TODO Auto-generated method stub
		ThemeBannerBean bean = new ThemeBannerBean();
		try {
			bean.mLength = dis.readInt();
			if (bean.mLength != 0) {
				bean.mTimeStamp = dis.readLong();
				bean.mLoopNum = dis.readInt();
				bean.mElements = new ArrayList<ThemeBannerBean.BannerElement>();
				for (int i = 0; i < bean.mLoopNum; i++) {
					BannerElement element = bean.new BannerElement();
					element.mId = dis.readInt();
					element.mSDate = dis.readUTF();
					element.mEDate = dis.readUTF();
					element.mPropertyid = dis.readInt();
					element.mName = dis.readUTF();
					element.mSource = dis.readInt();
					element.mImgUrl = getImageUrlArray(dis.readUTF());
					element.mImgids = getImgIdArray(dis.readUTF());
					String group = dis.readUTF();
					if (group != null) {
						element.mGroup = group.trim().replace("\n", "");
					}
					bean.mElements.add(element);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bean;
	}
	private String[] getImgIdArray(String imgs) {
		if (imgs == null || imgs.equals("")) {
			return null;
		}

		String[] idArray = imgs.split("#");
		return idArray;
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
