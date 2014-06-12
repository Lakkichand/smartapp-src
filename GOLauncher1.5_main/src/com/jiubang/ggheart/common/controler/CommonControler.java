package com.jiubang.ggheart.common.controler;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;

import com.jiubang.ggheart.common.bussiness.AppClassifyBussiness;
import com.jiubang.ggheart.common.bussiness.RecommendAppBussiness;
import com.jiubang.ggheart.data.Controler;
import com.jiubang.ggheart.data.info.AppItemInfo;


/**
 * 公共控制器
 * 功能：（分类应用，产生默认文件夹名称）
 * @author wuziyi
 *
 */
public class CommonControler extends Controler {

	private static CommonControler sSelfObject;
	private AppClassifyBussiness mAppClassifyBussiness;
	private RecommendAppBussiness mRecommendAppBussiness;

	private CommonControler(Context context) {
		super(context);
		mAppClassifyBussiness = new AppClassifyBussiness(context);
		mRecommendAppBussiness = new RecommendAppBussiness(context);
	}
	
	public synchronized static CommonControler getInstance(Context Context) {
		if (sSelfObject == null) {
			sSelfObject = new CommonControler(Context);
		}
		return sSelfObject;
	}
	
	/**
	 * 查询多个应用分类(初始化时分类)
	 * @param infoList
	 */
	@SuppressWarnings("unchecked")
	public void initAllAppClassify() {
		mAppClassifyBussiness.initAllAppClassify();
	}

	/**
	 * 查询一个应用分类
	 * @param Info
	 */
	public void queryAppClassify(AppItemInfo info) {
		mAppClassifyBussiness.queryAppClassify(info);
	}
	
	/**
	 * 查询info列表的分类
	 * @param info
	 */
	public void queryAppsClassify(ArrayList<AppItemInfo> info) {
		mAppClassifyBussiness.queryAppsClassify(info);
	}
	
	/**
	 * 查询一个应用分类（安装新应用时调用，速度相对较快）
	 * @param pkgName 包名
	 * @param infoList 该包名对应的info列表
	 */
	public void queryAppsClassify(String packageName, ArrayList<AppItemInfo> appItemInfos) {
		mAppClassifyBussiness.queryAppClassify(packageName, appItemInfos);
	}
	
	/**
	 * 根据分类产生一个默认文件夹名称
	 * @return
	 */
	public String generateFolderName(ArrayList<AppItemInfo> infoList) {
		return mAppClassifyBussiness.generateFolderName(infoList);
	}
	
	/**
	 * 初始化，从数据库获取所有需要显示New标识的推荐应用
	 */
	public void initAllNewRecommendApps() {
		mRecommendAppBussiness.initAllNewRecommendApps();
	}
	
	/**
	 * 检查新安装的应用是否是推荐应用，是则为其加上New标识并加入推荐应用表
	 * @return
	 */
	public boolean checkInstallAppIsRecommend(String pkg, ArrayList<AppItemInfo> appItemInfos) {
		return mRecommendAppBussiness.checkInstallAppIsRecommend(pkg, appItemInfos);
	}
	
	/**
	 * 检查卸载的应用是否推荐应用，是则在表中删除
	 * @param pkg
	 * @param appItemInfos
	 */
	public boolean checkUnInstallAppIsRecommend(String pkg, ArrayList<AppItemInfo> appItemInfos) {
		return mRecommendAppBussiness.checkUnInstallAppIsRecommend(pkg, appItemInfos);
	}
	
	/**
	 * 推荐应用被打开，更新数据库
	 * @param intent
	 */
	public void recommendAppBeOpen(Intent intent) {
		mRecommendAppBussiness.recommendAppBeOpen(intent);
	}
	
	/**
	 * 检查单个系统快捷方式是否推荐应用，是则为其改变mIsRecommendApp变量为true
	 * @return
	 */
	public boolean checkShortCutIsIsRecommend(AppItemInfo appItemInfo) {
		return mRecommendAppBussiness.checkShortCutIsIsRecommend(appItemInfo);
	}
}
