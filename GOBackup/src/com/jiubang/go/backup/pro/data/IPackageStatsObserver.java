package com.jiubang.go.backup.pro.data;

import android.content.pm.PackageStats;

/**
 * 接口 packagestatsObserver
 * 
 * @author maiyongshen
 */
public interface IPackageStatsObserver {
	void onGetStatsCompleted(PackageStats pStats, boolean succeeded);
}
