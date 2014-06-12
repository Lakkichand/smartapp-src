package com.jiubang.ggheart.components.advert;

import java.util.ArrayList;

/**
 * 
 * <br>类描述:广告对象
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-12-1]
 */
public class AdvertInfo implements Comparable<AdvertInfo> {
	public String mId;	//广告id
	public String mTitle; //广告标题
	public String mIcon;	//广告图片
	public String mPackageName; //包名
	public int mActtype;	//点击的动作类型
	public String mActvalue;	//点击动作对应的参数值
	public int mScreen;	//位于第几屏
	public int mPos;	//位于屏幕的位置，从屏幕右下角开始为1，从右到左，从下到上，依次递增
	public String mStartTime;	//广告推送开始时间
	public String mEndTime;	//广告推送结束时间
	public int mIsfile; //0：否 1：是 如果是文件，则文件下面还有消息列表
	public int mIscarousel; //mIsfile = 1,且iscarousel = 1 代表文件夹里面的图标是轮播文件
	public ArrayList<AdvertInfo> mFilemsg; //文件下面的消息列表，如果不是文件，则为空
	public String mClickurl; //对应的回调地址，只有点击时才需要上传
	public String mMapid;	//统计id
	public int mCellX;
	public int mCellY;

	@Override
	public int compareTo(AdvertInfo another) {
		//用位置排序
		if (this.mPos > another.mPos) {
			return 1;
		} else {
			return -1;
		}
	}
}
