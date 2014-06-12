package com.jiubang.ggheart.appgame.base.utils;

import java.util.ArrayList;
import java.util.List;

import com.jiubang.ggheart.appgame.base.bean.BoutiqueApp;
import com.jiubang.ggheart.appgame.base.bean.ClassificationDataBean;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 应用游戏中心，已安装应用过滤器
 * 
 * @author  xiedezhi
 * @date  [2012-12-25]
 */
public class AppGameInstalledFilter {

	/**
	 * 最大过滤比例
	 */
	private static final float MAX_FILTER_PROPORTION = 0.6667f;

	/**
	 * 过滤已经安装应用
	 */
	public static List<BoutiqueApp> filterAppList(int datatype,
			List<BoutiqueApp> src) {
		if (src == null || src.size() <= 0) {
			return src;
		}
		//剩余可过滤数
		int remain = (int) (src.size() * MAX_FILTER_PROPORTION);
		List<BoutiqueApp> ret = new ArrayList<BoutiqueApp>();
		for (BoutiqueApp app : src) {
			if (remain <= 0) {
				ret.add(app);
			} else if (datatype == ClassificationDataBean.FEATURE_TYPE
					&& app != null && app.cellsize == 5) {
				// 精品推荐banner图不会过滤
				ret.add(app);
			} else if (app != null && app.info != null && app.info.packname != null) {
				boolean isInstall = AppGameInstallingValidator.getInstance().isAppExist(
						GOLauncherApp.getContext(), app.info.packname);
				if (!isInstall) {
					ret.add(app);
				} else {
					//过滤了一个应用，剩余可过滤数减一
					remain--;
				}
			} else {
				ret.add(app);
			}
		}
		return ret;
	}

	/**
	 * 过滤已经安装应用
	 */
	public static List<ClassificationDataBean> filterDataBeanList(List<ClassificationDataBean> src) {
		if (src == null || src.size() <= 0) {
			return src;
		}
		List<ClassificationDataBean> ret = new ArrayList<ClassificationDataBean>();
		for (ClassificationDataBean bean : src) {
			if (bean == null) {
				ret.add(bean);
			} else if (bean.dataType == ClassificationDataBean.FEATURE_TYPE
					|| bean.dataType == ClassificationDataBean.ONEPERLINE_SPECIALSUBJECT_TYPE
					|| bean.dataType == ClassificationDataBean.EDITOR_RECOMM_TYPE
					|| bean.dataType == ClassificationDataBean.PRICE_ALERT
					|| bean.dataType == ClassificationDataBean.GRID_TYPE
					|| bean.dataType == ClassificationDataBean.WALLPAPER_GRID) {
				//只有服务器指定要过滤已安装应用时才过滤
				if (bean.featureList != null && bean.filter == 1) {
					bean.featureList = filterAppList(bean.dataType,
							bean.featureList);
					ret.add(bean);
				} else {
					ret.add(bean);
				}
			} else {
				ret.add(bean);
			}
		}
		return ret;
	}

}
