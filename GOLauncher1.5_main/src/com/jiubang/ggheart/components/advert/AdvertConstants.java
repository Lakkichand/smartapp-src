package com.jiubang.ggheart.components.advert;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import android.content.Context;

import com.jiubang.ggheart.data.AppDataEngine;
import com.jiubang.ggheart.data.info.AppItemInfo;

/**
 * 
 * <br>类描述:15屏广告参数类
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-12-7]
 */
public class AdvertConstants {
	//测试地址
//	public static final String HOSTURL_BASE = "http://61.145.124.70:8183/golaunchermsg/advservice.do?";
	
	//正式地址
	public static final String HOSTURL_BASE = "http://launchermsg.3g.cn/golaunchermsg/advservice.do?";

	public static final int GET_ADVERT_DATA_SUCCESS = 0; //请求获取广告数据成功
	public static final int GET_ADVERT_DATA_FAIL = 1; //请求获取广告数据失败
	
	public static final int DOWN_IMAGE_SUCCESS = 3; //下载图片成功
	public static final int DOWN_IMAGE_FAIL = 4; //下载图片失败
	
	public static final int STATISTICS_REQUEST_SUCCESS = 5;	//统计上传成功
	public static final int STATISTICS_REQUEST_FAIL = 6;	//统计上传失败
	
	public static final int MESSAGE_CLERAR_ANIMATION = 7;	//清除抖动动画
	
	public static final String LTS_REQUEST_TIME = "lts_request_time"; // 请求URL成功
	
	public static final String ADVERT_ACTVALUE = "acvert_actvalue"; //广告图标调转字段
	public static final String ADVERT_PACK_NAME = "acvert_pack_name"; //广告图标对应的程序包名
	public static final String ADVERT_ID = "acvert_id"; //广告图标对应的id
	public static final String ADVERT_CLICK_URL = "acvert_clickurl"; //对应的回调地址，只有点击时才需要上传
	public static final String ADVERT_MAPID = "acvert_mapid"; //广告统计id
	public static final String ADVERT_TITLE = "advert_title"; // 广告标题
	
	public static final int ADVERT_STATISTICS_TYPE = 3; //桌面统计接口监听调用
	
	public static final String ADVERT_PACKAGE_NAME_LIST = "acvert_package_name_list"; //广告添加到桌面图标列表

	public static final String ADVERT_IS_OPENED = "is_open"; //纪录该应用已经打开过

	public static final long ADVERT_TIPS_TIME = 8 * 60 * 60 * 1000; //8小时，相隔多久进行通知栏提示
	
	
	public static final long ADVERT_24_TIME = 24 * 60 * 60 * 1000; //24小时，相隔24小时重新请求接口

	
	public static final String ADVERT_REQUEST_TIME = "acvert_request_time"; //第一次请求时间
 
	public static final String ADVERT_24_CAN_REQUEST = "acvert_24_can_request"; //24小时后是否可以被请求
	
	public static final String ADVERT_ADD_SCREEN_CACHE = "acvert_add_screen_cache"; //插入到数据库的缓存
	
	public static final String ADVERT_HOME_SCREEN_CACHE = "acvert_home_screen_cache"; //插入到数据库的缓存

	
	public static final int IS_CAROUSEL = 1; //是轮换文件夹
	public static final int IS_NO_CAROUSEL = 0; //不是轮换文件夹
	
	public static final int IS_FILE = 1; //是文件夹类型
	public static final int IS_NO_FILE = 0; //不是文件夹类型
	
	/**
	 * <br>功能简述:获取URL地址
	 * <br>功能详细描述:funid=x&rd=1234;其中rd是随机数，防止网关cache.
	 * <br>注意:1:服务器请求，2：统计
	 * @return
	 */
	public static String getUrl(String funid) {
		StringBuffer buffer = new StringBuffer(AdvertConstants.HOSTURL_BASE);
		Random random = new Random(new Date().getTime()); //随机数
		buffer.append("funid=" + funid + "&rd=" + random.nextLong());
		random = null;
		return buffer.toString();
	}
	
	/**
	 * <br>功能简述:获取包命对应的程序信息
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param packageName 包名
	 * @return
	 */
	public static AppItemInfo getAppName(Context context, String packageName) {
		ArrayList<AppItemInfo> appItemInfos = AppDataEngine.getInstance(context)
				.getAllAppItemInfos();
		if (appItemInfos != null) {
			for (AppItemInfo appItemInfo : appItemInfos) {
				if (appItemInfo != null && appItemInfo.mProcessName != null) {
					String appPackageName = appItemInfo.mProcessName;
					if (appPackageName.equals(packageName)) {
						return appItemInfo;
					}
				}
			}
		}
		return null;
	}
}
