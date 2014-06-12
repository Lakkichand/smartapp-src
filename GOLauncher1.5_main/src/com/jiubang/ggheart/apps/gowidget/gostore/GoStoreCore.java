package com.jiubang.ggheart.apps.gowidget.gostore;

import android.content.Context;

import com.jiubang.ggheart.apps.config.ChannelConfig;
import com.jiubang.ggheart.apps.config.GOLauncherConfig;
import com.jiubang.ggheart.apps.gowidget.gostore.cache.CacheManager;
import com.jiubang.ggheart.apps.gowidget.gostore.cache.GoStoreCacheManager;
import com.jiubang.ggheart.apps.gowidget.gostore.cache.parser.XmlCacheParser;
import com.jiubang.ggheart.apps.gowidget.gostore.net.SimpleHttpAdapter;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreStatisticsUtil;
import com.jiubang.ggheart.billing.ThemeAppInBillingManager;

/**
 * 
 * <br>类描述:GO精品基本准备数据
 * <br>功能详细描述:
 * 
 * @author  zhouxuewen
 * @date  [2012-9-12]
 */
public class GoStoreCore {

	public static final boolean IS_CHANNEL_SHUTDOWN = true; // 渠道开关

	public static GoStoreCore sSelf = null;
	public static boolean sIsPrepare = false;
	public static int sUserCount = 0;
	private Context mContext = null;
	public static ThemeAppInBillingManager sThemeAppinBilling; //公共内购管理器单例

	public synchronized static void prepare(Context context) {
		if (null == sSelf && context != null) {
			sSelf = new GoStoreCore(context);
		}
	}

	public static GoStoreCore getInstance() {
		return sSelf;
	}

	private GoStoreCore(Context context) {
		increaseUserCount();
		mContext = context;
		sThemeAppinBilling = ThemeAppInBillingManager.getInstance(mContext);
		init();
		sIsPrepare = true;
	}

	private void init() {
		if (mContext != null) {
			// 构建数据请求工具
			SimpleHttpAdapter.build(mContext);
			// 设置网络请求的最大并发数量
			SimpleHttpAdapter.getInstance().setMaxConnectThreadNum(8);
			// 渠道开关 判断
			if (!IS_CHANNEL_SHUTDOWN) {
				// 进行渠道包验证
				GoStoreChannelControl.getChannelControlInstance(mContext).checkChannelAvailable();
			}
			// 保存是否曾经进入GO Store的统计数据
			GoStoreStatisticsUtil.saveOnceEnterGostore(mContext,
					GoStoreStatisticsUtil.ONCE_ENTER_GOSTORE);
			// 初始化缓存处理器
			CacheManager.build(mContext, new XmlCacheParser(), null);
			GoStoreCacheManager.build(mContext, null);
			// CacheManager cacheManager = CacheManager.getInstance();
			// if(cacheManager != null){
			// cacheManager.checkViewDataUpdate();
			// }
			// 加载系统渠道配置信息
			GOLauncherConfig.getInstance(mContext).roadConfig();
		}
	}

	/**
	 * 获取渠道配置信息的方法
	 * 
	 * @param context
	 * @return
	 */
	public static ChannelConfig getChannelConfig(Context context) {
		return GOLauncherConfig.getInstance(context).getChannelConfig();
	}

	public static void checkViewDataUpdate() {
		/*CacheManager cacheManager = CacheManager.getInstance();
		if (cacheManager != null) {
			cacheManager.checkViewDataUpdate();
		}*/
		GoStoreCacheManager cacheManager = GoStoreCacheManager.getInstance();
		if (cacheManager != null)
		{
			cacheManager.checkViewDataUpdate();
		}
	}

	private void clearUp() {
		mContext = null;
	}

	public synchronized static void increaseUserCount() {
		sUserCount++;
	}

	public synchronized static void decreaseUserCount() {
		sUserCount--;
	}

	public static synchronized void destory() {
		decreaseUserCount();
		if (sUserCount <= 0 && sSelf != null) {
			sSelf.clearUp();
			sSelf = null;
			sIsPrepare = false;
			sUserCount = 0;
			if (sThemeAppinBilling != null) {
				sThemeAppinBilling.destory();
				sThemeAppinBilling = null;
			}
			// 销毁HttpAdapter
			SimpleHttpAdapter.destory();
			// 销毁HttpTool
			GoStoreHttpTool.destory();
			// 释放ImageManager里面的资源
			ImageManager.destory();
			// 销毁线程池
			ThreadPoolManager.destory();
			// 销毁缓存管理器
			CacheManager.destory();
			GoStoreCacheManager.destory();
			// 退出商店督促gc回收
			System.gc();
			// 把进程杀掉
			android.os.Process.killProcess(android.os.Process.myPid());
		}
	}

	public static synchronized void destroyNotKill() {
		decreaseUserCount();
		if (sUserCount <= 0 && sSelf != null) {
			sSelf.clearUp();
			sSelf = null;
			sIsPrepare = false;
			sUserCount = 0;
			// 销毁HttpAdapter
			SimpleHttpAdapter.destory();
			// 释放ImageManager里面的资源
			ImageManager.destory();
			// 销毁线程池
			ThreadPoolManager.destory();
			// 销毁缓存管理器
			CacheManager.destory();
			GoStoreCacheManager.destory();
			// 退出商店督促gc回收
			System.gc();
			// 这里不杀掉进程
		}
	}
}
