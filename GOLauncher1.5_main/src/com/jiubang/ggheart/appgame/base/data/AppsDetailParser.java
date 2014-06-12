/**
 * 
 */
package com.jiubang.ggheart.appgame.base.data;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.jiubang.ggheart.appgame.base.bean.AppDetailInfoBean;
import com.jiubang.ggheart.appgame.base.bean.BoutiqueApp;
import com.jiubang.ggheart.appgame.base.bean.SecurityInfo;
import com.jiubang.ggheart.appgame.base.bean.SecurityInfo.ThirdSecurityItem;

/**
 * @author liguoliang
 * 
 */
public class AppsDetailParser {
	public static AppDetailInfoBean parseDetailInfo(JSONObject json) {
		if (json == null) {
			return null;
		}
		AppDetailInfoBean bean = new AppDetailInfoBean();
		bean.mAppId = json.optInt("appid", Integer.MIN_VALUE);
		bean.mPkgName = json.optString("packname", null);
		bean.mName = json.optString("name", null);
		bean.mIconUrl = json.optString("icon", null);
		bean.mVersion = json.optString("version", null);
		bean.mVersionCode = json.optString("versioncode", null);
		bean.mSize = json.optString("size", null);
		bean.mIsFree = json.optInt("isfree", 0) == 0 ? true : false;
		bean.mPrice = json.optString("price", null);
		bean.mGrade = json.optInt("grade", 0);
		bean.mDownloadCount = json.optString("dlcs", null);
		bean.mFirmwareSupport = json.optString("support", null);
		bean.mDeveloper = json.optString("developer", null);
		bean.mDetail = json.optString("detail", null);
		bean.mUpdateLog = json.optString("updatelog", null);
		bean.mDownloadType = json.optInt("downloadtype", 0);
		bean.mDownloadUrl = json.optString("downloadurl", null);
		bean.mPicUrl = json.optString("picurl", null);
		String strPicUrls = json.optString("pics", null);
		String strPicIds = json.optString("picids", null);
		bean.mUpdateTime = json.optString("updatetime", null);
        bean.mAtype = json.optString("typeinfo", "");
		JSONArray appArray = null;
		try {
			Object object = json.get("recmdapps");
			bean.mRecmdId = json.optInt("recmdid", Integer.MIN_VALUE);
			if (object != null && object instanceof JSONArray) {
				appArray = (JSONArray) object;
			}
		} catch (Exception e) {
		}
		bean.cback = json.optInt("cback", 0);
		bean.cbacktype = json.optInt("cbacktype", 0);
		bean.cbackurl = json.optString("cbackurl", null);
		bean.mRemdmsg = json.optString("remdmsg", null);
		bean.mAtype = json.optString("typeinfo", null);
		bean.mIcbackUrl = json.optString("icbackurl", "");
		// TODO 木瓜sdk
		bean.mAfCbackUrl = json.optString("mgcbackurl", "");

		// 安全认证信息			
		JSONObject securityJson = json.optJSONObject("security");
		bean.mSecurityInfo = parseSecurityInfo(securityJson);
		
		//付费类型
		String payTypeString = json.optString("paytype");
		String[] tmpPayType = null;
		int[] payType = null;
		if (payTypeString != null && !payTypeString.trim().equals(""))
		{
			tmpPayType = payTypeString.split("#");
			if (tmpPayType != null) {
				int count = tmpPayType.length;
				payType = new int[count];
				for (int i = 0; i < count; i++) {
					payType[i] = Integer.parseInt(tmpPayType[i]);
				}
			}
		}
		bean.mPayType = payType;
		if (strPicUrls != null && !strPicUrls.trim().equals("")) {
			// 解析图片url字符串:每张图片之间用@@分隔,小图大图之间用##分隔
			bean.mSmallPicUrls = new ArrayList<String>();
			bean.mLargePicUrls = new ArrayList<String>();
			String[] picUrls = strPicUrls.split("@@");
			for (String picUrl : picUrls) {
				int index = picUrl.indexOf("##");
				if (index != -1) {
					String smallPicUrl = picUrl.substring(0, index);
					String largePicUrl = picUrl.substring(index + 2, picUrl.length());
					bean.mSmallPicUrls.add(smallPicUrl);
					bean.mLargePicUrls.add(largePicUrl);
				}
			}
		}
		if (strPicIds != null && !strPicIds.trim().equals("")) {
			// 解析图片字符串:每张图片之间用@@分隔,小图大图之间用##分隔
			bean.mSmallPicIds = new ArrayList<String>();
			bean.mLargePicIds = new ArrayList<String>();
			String[] picIdS = strPicIds.split("@@");
			for (String picId : picIdS) {
				int index = picId.indexOf("##");
				if (index != -1) {
					String smallPicId = picId.substring(0, index);
					String largePicId = picId.substring(index + 2, picId.length());
					bean.mSmallPicIds.add(smallPicId);
					bean.mLargePicIds.add(largePicId);
				}
			}
		}
		//付费id
		bean.mPayId = json.optString("payid");
		//资源包下载地址
		bean.mResourceUrl = json.optString("resourceurl");
		if (appArray != null) {
			List<BoutiqueApp> ret = new ArrayList<BoutiqueApp>();
			for (int i = 0; i < appArray.length(); i++) {
				JSONObject appJson = appArray.optJSONObject(i);
				if (appJson != null) {
					BoutiqueApp app = new BoutiqueApp();
					FeatureDataParser.parseAppInfo(appJson, app.info);
					ret.add(app);
				}
			}
			bean.mRecomApps = ret;
		}
		bean.mDetailStlye = json.optInt("detailstyle", 0);
		bean.mTag = json.optInt("tag", 0);
		return bean;
	}

	private static SecurityInfo parseSecurityInfo(JSONObject json) {
		if (json == null) {
			return null;
		}
		SecurityInfo info = new SecurityInfo();
		info.mScore = json.optInt("score");
		info.mName = json.optString("name");
		info.mIcon = json.optString("icon");
		info.mPic = json.optString("pic");
		info.mResultMsg = json.optString("resultmsg");

		JSONArray thirdSecurityArray = json.optJSONArray("securitys");
		if (thirdSecurityArray != null) {
			int size = thirdSecurityArray.length();
			info.mThirdSecurityList = new ArrayList<ThirdSecurityItem>();
			for (int i = 0; i < size; ++i) {
				JSONObject thirdJson = thirdSecurityArray.optJSONObject(i);
				if (thirdJson == null) {
					continue;
				}
				ThirdSecurityItem item = info.new ThirdSecurityItem();
				item.mThirdPkgName = thirdJson.optString("packname");
				item.mThirdName = thirdJson.optString("name");
				item.mThirdIconUrl = thirdJson.optString("icon");
				item.mThirdResultMsg = thirdJson.optString("resultmsg");
				info.mThirdSecurityList.add(item);
			}
		}
		return info;
	}

}
