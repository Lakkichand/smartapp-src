package com.jiubang.ggheart.data.statistics;

/**
 * 监控应用安装的统计接口
 * 
 * @author huyong
 * 
 */
public interface IMonitorAppInstallListener {
	/**
	 * app安装事件的处理
	 * 
	 * @param pkgName
	 *            ：监控到安装事件的包名
	 * @param listenKey
	 *            ：针对该安装包的监听者key
	 */
	public void onHandleAppInstalled(String pkgName, String listenKey);

}
