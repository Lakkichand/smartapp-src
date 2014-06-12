/*
 * 文 件 名:  FeaturedThemeDetailBean.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  rongjinsong
 * 修改时间:  2012-8-21
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.data.theme.bean;

import java.util.HashMap;
import java.util.List;

import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.BaseBean;

/**
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  rongjinsong
 * @date  [2012-8-21]
 */
public class FeaturedThemeDetailBean extends BaseBean {
	public int mId;
	public List<String> mPayType;
	public String mPayId; //电子市场付费ID
	public String mDownurl;
	public String mMlocker;
	public String mMwidget;
	public boolean mIsAll; //是否大主题：0：否 1：是
	public int mIsNew;
	public int mStar; //软件的星级,1-10整数,1代表半颗星
	public int mIconImgSource; //0:使用imgid拿图片 1：使用imgurl拿图片，如果imgurl为空，则用imgid拿图片
	public List<String> mIconImgUrl; //图片ＵＲＬ,多个以#分隔
	public String mIcon;
	public String mName;
	public String mSize; //包大小，单位为kb
	public String mVersion; //版本描述
	public int mVersionNum; //版本编号，由小到大递增
	public String mPackageName; //包名,用于客户端判断是否安装该软件
	public String mUpdateTime; //更新时间，格式:	yyyyMMdd
	public int mFeeType; //应用费用，0：免费，1：收费，2：getjar，3：Google Play包月
	public String mPrice; //价格，如$1.99
	public String mSupport; //固件支持
	public String mDevelop; //开发商
	public String mSummary; //产品简介
	public String mDetail; //产品描述，产品详情
	public String mUpdateLog;
	public String mImgIds; //缩略图id串。所有图片均采用独立下载模式，可以让页面快速渲染。图片获取可以通过功能号funid=6多个imgid之间通过#分隔
	public String mBigimgids; //缩略图对应大图id串。所有图片均采用独立下载模式，可以让页面快速渲染。图片获取可以通过功能号funid=6	多个imgid之间通过#分隔
	public String mDownloadSize; //应用下载量
	public int mUrlNum; //下发的url个数（下载url统一下发一个）
	public HashMap<Integer, String> mUrlMap = null; // 链接地址
	public int mImgSource; //0:使用imgid拿图片 1：使用imgurl拿图片，如果imgurl为空，则用imgid拿图片
	public List<String> mImgUrl; //图片ＵＲＬ,多个以#分隔
	public int mBigImgSource; //0:使用imgid拿图片 1：使用imgurl拿图片，如果imgurl为空，则用imgid拿图片
	public List<String> mBigImgUrl; //图片ＵＲＬ,多个以#分隔
	public String mVimgUrl; //视频图片url
	public String mVurl; //视频url
}
