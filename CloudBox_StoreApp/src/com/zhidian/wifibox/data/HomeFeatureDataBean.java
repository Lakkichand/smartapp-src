package com.zhidian.wifibox.data;

import java.util.ArrayList;
import java.util.List;

/**
 * 首页推荐数据bean
 * 
 * @author xiedezhi
 * 
 */
public class HomeFeatureDataBean {

	/**
	 * 幻灯片数据列表
	 */
	public List<BannerDataBean> mBannerList = new ArrayList<BannerDataBean>();
	/**
	 * 标签
	 */
	public List<String> mTagList = new ArrayList<String>();
	/**
	 * 专题id
	 */
	public List<Long> mIdList = new ArrayList<Long>();
	/**
	 * 颜色
	 */
	public List<Integer> mBackGroundList = new ArrayList<Integer>();
	/**
	 * 应用
	 */
	public List<AppDataBean> mAppList = new ArrayList<AppDataBean>();
	/**
	 * 游戏
	 */
	public List<AppDataBean> mGameList = new ArrayList<AppDataBean>();

}
