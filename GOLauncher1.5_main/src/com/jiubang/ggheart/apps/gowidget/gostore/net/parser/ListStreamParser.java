package com.jiubang.ggheart.apps.gowidget.gostore.net.parser;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.BaseBean;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.ListElementBean;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.ListElementBean.LinkedElement;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.ListElementBean.SoftwareElement;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.ListElementBean.WallpaperElement;

/**
 * 
 * <br>类描述: 一般应用列表的字节解释
 * <br>功能详细描述:
 * 
 * @author  zhouxuewen
 * @date  [2012-9-12]
 */
public class ListStreamParser extends HttpStreamParser {
	public int mFunid;

	@Override
	public BaseBean parseHttpStreamData(DataInputStream dis) {
		ListElementBean listElementBean = new ListElementBean();
		try {
			listElementBean.mLength = dis.readInt(); // 下发的数据长
			if (listElementBean.mLength == 0) {
				// 下发数据为0，则直接返回。
				return listElementBean;
			}
			listElementBean.mTimeStamp = dis.readLong(); // 下发的数据时间戳
			listElementBean.mTotalNum = dis.readInt(); // 总结果数量
			if (mFunid == 3) {
				listElementBean.mTypeId = dis.readInt();
				listElementBean.mPropertyId = dis.readInt();
				listElementBean.mBannerId = dis.readUTF();
			}
			listElementBean.mCurrentPage = dis.readInt();
			listElementBean.mTotalPage = dis.readInt();

			if (mFunid == 4) {
				listElementBean.mSearchId = dis.readInt();
			}

			if (mFunid == 8) {
				listElementBean.mPropertyId = dis.readInt();
			}

			int count = dis.readInt();
			listElementBean.mElementCount = count;
			listElementBean.mElementsList = new ArrayList<ListElementBean.Element>(count);
			for (int i = 0; i < count; i++) {
				// 首先读出数据类型
				listElementBean.mElementType = dis.readByte();
				// 是否热门
				int isHot = dis.readByte();
				// 是否新应用
				int isNew = dis.readByte();
				// 星级
				String star = dis.readUTF();
				// 应用iconID
				String logoIconId = dis.readUTF();
				if (listElementBean.mElementType == ListElementBean.ELEMENTTYPE_SOFTWARE) {
					// 软件数据
					SoftwareElement element = listElementBean.new SoftwareElement();
					element.mDateType = ListElementBean.ELEMENTTYPE_SOFTWARE;
					element.mIsHot = isHot;
					element.mIsNew = isNew;
					element.mStar = star;
					element.mLogoIconId = logoIconId;
					parseBaseSoftwareData(element, dis);
					listElementBean.mElementsList.add(element); 
				} else if (listElementBean.mElementType == ListElementBean.ELEMENTTYPE_LINKED) {
					// 链接数据
					LinkedElement element = listElementBean.new LinkedElement();
					element.mDateType = ListElementBean.ELEMENTTYPE_LINKED;
					element.mIsHot = isHot;
					element.mIsNew = isNew;
					element.mStar = star;
					element.mLogoIconId = logoIconId;
					element.mId = dis.readInt();
					element.mName = dis.readUTF();
					element.mType = dis.readUTF();
					element.mDetail = dis.readUTF();
					element.mSource = dis.readUTF();
					element.mUrl = dis.readUTF();
					element.mImgId = dis.readUTF();
					element.mUpdateTime = dis.readUTF();

					listElementBean.mElementsList.add(element);
				} else if (listElementBean.mElementType == ListElementBean.ELEMENTTYPE_SORT) {
					// 应用+分类数据
					SoftwareElement element = listElementBean.new SoftwareElement();
					element.mDateType = ListElementBean.ELEMENTTYPE_SORT;
					element.mIsHot = isHot;
					element.mIsNew = isNew;
					element.mStar = star;
					element.mLogoIconId = logoIconId;
					parseBaseSoftwareData(element, dis);
					element.mSortId = dis.readInt();
					element.mParentId = dis.readInt();
					element.mPropertyId = dis.readInt();
					element.mChildtype = dis.readByte();
					element.mSortName = dis.readUTF();

					listElementBean.mElementsList.add(element);
				} else if (listElementBean.mElementType == ListElementBean.ELEMENTTYPE_WALLPAPER) {
					// 静态壁纸类型
					WallpaperElement element = listElementBean.new WallpaperElement();
					element.mId = dis.readInt();
					element.mImgid = dis.readUTF();
					element.mPreimgid = dis.readUTF();
					element.mDownimgurl = dis.readUTF();
					element.mSize = dis.readUTF();

					listElementBean.mElementsList.add(element);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return listElementBean;
	}

	private void parseBaseSoftwareData(SoftwareElement element, DataInputStream dis) {

		try {
			element.mId = dis.readInt();
			element.mStyle = dis.readInt();
			String paytypes = dis.readUTF();
			if (paytypes != null && !paytypes.equals("")) {
				element.mPaytype = paytypes.split("#");
			}
			//////////////
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
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
