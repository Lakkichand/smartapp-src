package com.jiubang.ggheart.apps.appmanagement.bean;

import java.util.ArrayList;

/**
 * 推荐应用分类
 * 
 * @author zhoujun
 * 
 */
public class RecommendedAppCategory {

	// public int mId;
	public String mTypeId; // 分类id
	public String mName; // 分类名称
	public String mIcon; // 分类图标url(备用)
	public int mViewtype; // 展现方式 0:收缩展示，1: 展开展示
	public int mViewlocal; // 展现位置(备用) 0:默认位置展示，
	public int mCount; // 包含的应用数量
	public boolean mFirstShow = true;

	public ArrayList<RecommendedApp> mRecommendedAppList;

}
