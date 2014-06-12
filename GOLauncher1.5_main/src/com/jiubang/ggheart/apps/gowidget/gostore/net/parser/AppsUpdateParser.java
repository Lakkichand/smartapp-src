package com.jiubang.ggheart.apps.gowidget.gostore.net.parser;

import java.io.DataInputStream;
import java.util.HashMap;

import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.AppsBean;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.AppsBean.AppBean;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.BaseBean;
/**
 * 
 * @author zhujian
 *数据解析类
 */
public class AppsUpdateParser extends HttpStreamParser {
	/*
	 * @Override public BaseBean parseHttpStreamData(DataInputStream dis) {
	 * 
	 * AppsBean appsBean = null; if (dis != null) { appsBean = new AppsBean();
	 * try { appsBean.mDetailBaseUrl = dis.readUTF(); appsBean.mImgBaseUrl =
	 * dis.readUTF(); int appNum = dis.readInt(); AppBean bean = null; while
	 * (appNum > 0) { bean = appsBean.new AppBean(); bean.mAppId =
	 * dis.readInt(); bean.mPkgName = dis.readUTF(); //包名 bean.mAppName =
	 * dis.readUTF(); //程序名 bean.mWebMarket = dis.readUTF(); //web版market地址
	 * bean.mUpdateTime = dis.readUTF(); //更新时间 bean.mIconId = dis.readUTF();
	 * //图标id
	 * 
	 * appsBean.mListBeans.add(bean); }
	 * 
	 * } catch (Exception e) { // TODO: handle exception e.printStackTrace(); }
	 * finally { if (dis != null) { try { dis.close(); } catch (IOException e) {
	 * // TODO Auto-generated catch block e.printStackTrace(); } } } } return
	 * appsBean; }
	 */

	@Override
	public BaseBean parseHttpStreamData(DataInputStream dis) {

		AppsBean appsBean = null;
		if (dis != null) {
			appsBean = new AppsBean();
			try {
				int length = dis.readInt();
				if (length <= 0) {
					return appsBean;
				}
				long timeStamp = dis.readLong();
				int appNum = dis.readInt();
				AppBean bean = null;
				while (appNum > 0) {
					bean = appsBean.new AppBean();
					bean.mAppId = dis.readInt();
					bean.mXdeltaUrl = dis.readUTF();
					bean.mAppDeltaSize = dis.readUTF();
					if (bean.mXdeltaUrl != null && !bean.mXdeltaUrl.equals("")) {
						bean.mIsXdelta = 0;
					} else {
						bean.mIsXdelta = 1;
					}
					bean.mUpdateLog = dis.readUTF();
					bean.mSource = dis.readInt();
					bean.mCallbackUrl = dis.readUTF();
					bean.mPkgName = dis.readUTF(); // 包名
					bean.mUpdateTime = dis.readUTF(); // 更新时间
					bean.mAppSize = dis.readUTF();
					bean.mVersionName = dis.readUTF();
					int urlCount = dis.readInt();
					bean.mUrlNum = urlCount;
					if (urlCount > 0) {
						bean.mUrlMap = new HashMap<Integer, String>(urlCount);
						while (urlCount > 0) {
							int urlKey = dis.readInt();
							String url = dis.readUTF();
							bean.mUrlMap.put(urlKey, url);
							--urlCount;
						}
					}
					appsBean.mListBeans.add(bean);
					--appNum;
				}
				// add by zhoujun 2012-05-14 添加可更新控制个数
				int controlCount = dis.readInt();
				appsBean.mControlNum = controlCount;
				if (controlCount > 0) {
					appsBean.mControlcontrolMap = new HashMap<Integer, Byte>(
							controlCount);
					while (controlCount > 0) {
						int urlKey = dis.readInt();
						byte value = dis.readByte();
						appsBean.mControlcontrolMap.put(urlKey, value);
						--controlCount;
					}
				}
				// add by zhoujun 2012-05-14 end
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		return appsBean;
	}
}
