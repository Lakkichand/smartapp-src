package com.jiubang.ggheart.apps.gowidget.gostore.util.url;

import java.util.HashMap;

import android.content.Context;

import com.jiubang.ggheart.appgame.download.IAidlDownloadListener;
import com.jiubang.ggheart.apps.gowidget.gostore.common.GoStorePublicDefine;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreFileUtil;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreOperatorUtil;

/**
 * 
 * <br>类描述:FTP下载处理方法
 * <br>功能详细描述:
 * 
 * @author  zhouxuewen
 * @date  [2012-9-12]
 */
public class FTPUrlOperator implements IUrlOperator {

	private IUrlOperator mUrlOperator = null;

	private static FTPUrlOperator sSelf = null;

	private String mName = null; // FTP下载名字
	private boolean mIsFree = false; // 下载应用是否免费
	private long mId = -1; // 下载应用ID
	private String mPackageName = null; // 下载应用包命
	private int mIconType = 0; // 下载应用Icon类型
	private String mIconInfo = null; // 下载应用Icon Info
	private int mVerCode = 0; //版本号

	private FTPUrlOperator() {
	}

	public synchronized static FTPUrlOperator getInstance() {
		if (null == sSelf) {
			sSelf = new FTPUrlOperator();
		}
		return sSelf;
	}

	@Override
	public boolean handleUrl(Context context, HashMap<Integer, String> urlHashMap) {
		// TODO Auto-generated method stub
		boolean result = false;
		if (context != null && urlHashMap != null && urlHashMap.size() > 0) {
			if (mIsFree) {
				// 只有是免费的才能直接下载
				String ftpUrl = urlHashMap.get(GoStorePublicDefine.URL_TYPE_HTTP_SERVER);
				String saveFilePath = GoStoreFileUtil.getFilePath(context, mPackageName, mVerCode);
				if (ftpUrl != null && !"".equals(ftpUrl.trim())) {
					// 如果有FTP地址
					if (ftpUrl.contains(".apk")) {
						GoStoreOperatorUtil.downloadFileDirectly(context, mName, ftpUrl, mId,
								mPackageName, saveFilePath, mIconType, mIconInfo);
					} else {
						// GoStoreOperatorUtil.gotoBrowser(context, ftpUrl);
						//						Intent intent = DownloadTool.initBaseDownloadIntent(mName, ftpUrl,
						//								mPackageName, mIconType, mIconInfo);
						//						context.sendBroadcast(intent);
						//						long currentTime = System.currentTimeMillis();
						mName = mName.trim();
						ftpUrl = ftpUrl.trim();
						GoStoreOperatorUtil.downloadFileDirectly(context, mName, ftpUrl,
								mId, mPackageName, saveFilePath, mIconType, mIconInfo);
					}
					result = true;
				} else {
					// 如果没有FTP地址
					if (mUrlOperator != null) {
						result = mUrlOperator.handleUrl(context, urlHashMap);
					}
				}
			} else {
				// 如果不是免费的
				if (mUrlOperator != null) {
					result = mUrlOperator.handleUrl(context, urlHashMap);
				}
			}
		}
		return result;
	}

	@Override
	public boolean handleUrl(Context context, HashMap<Integer, String> urlHashMap,
			Class<? extends IAidlDownloadListener.Stub>[] listenerClazzArray,
			String customDownloadFileName) {
		// TODO Auto-generated method stub
		boolean result = false;
		if (context != null && urlHashMap != null && urlHashMap.size() > 0) {
			if (mIsFree) {
				// 只有是免费的才能直接下载
				String ftpUrl = urlHashMap.get(GoStorePublicDefine.URL_TYPE_HTTP_SERVER);
				if (ftpUrl != null && !"".equals(ftpUrl.trim())) {
					// 如果有FTP地址
					if (ftpUrl.contains(".apk")) {
						GoStoreOperatorUtil.downloadFileDirectly(context, mName, ftpUrl, mId,
								mPackageName, listenerClazzArray, customDownloadFileName,
								mIconType, mIconInfo);
					} else {
						// GoStoreOperatorUtil.gotoBrowser(context, ftpUrl);
						//						Intent intent = DownloadTool.initBaseDownloadIntent(mName, ftpUrl,
						//								mPackageName, mIconType, mIconInfo);
						//						context.sendBroadcast(intent);
						long currentTime = System.currentTimeMillis();
						mName = mName.trim();
						ftpUrl = ftpUrl.trim();
						String saveFilePath = null;
						saveFilePath = GoStoreFileUtil.getFilePath(context, mPackageName, mVerCode);
						GoStoreOperatorUtil.downloadFileDirectly(context, mName, ftpUrl,
								currentTime, mPackageName, saveFilePath, mIconType, mIconInfo);
					}
					result = true;
				} else {
					// 如果没有FTP地址
					// //统计 应用更新 下载完成
					// AppManagementStatisticsUtil.getInstance()
					// .saveUpdataComplete(context,mPackageName,String.valueOf(mId),
					// 1);
					if (mUrlOperator != null) {
						result = mUrlOperator.handleUrl(context, urlHashMap);
					}
				}
			} else {
				// 如果不是免费的
				if (mUrlOperator != null) {
					result = mUrlOperator.handleUrl(context, urlHashMap);
				}
			}
		}
		return result;
	}
	public IUrlOperator getUrlOperator() {
		return mUrlOperator;
	}

	public void setUrlOperator(IUrlOperator urlOperator) {
		this.mUrlOperator = urlOperator;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		this.mName = name;
	}

	public String getIconInfo() {
		return mIconInfo;
	}

	public void setIconInfo(String info) {
		this.mIconInfo = info;
	}

	public int getIconType() {
		return mIconType;
	}

	public void setIconType(int type) {
		this.mIconType = type;
	}

	public boolean isFree() {
		return mIsFree;
	}

	public void setFree(boolean isFree) {
		this.mIsFree = isFree;
	}

	public long getId() {
		return mId;
	}

	public void setId(long id) {
		this.mId = id;
	}

	public String getPackageName() {
		return mPackageName;
	}

	public void setPackageName(String packageName) {
		this.mPackageName = packageName;
	}

	public int getVerCode() {
		return mVerCode;
	}

	public void setVerCode(int verCode) {
		this.mVerCode = verCode;
	}

	@Override
	public boolean handleUrl(Context context, HashMap<Integer, String> urlHashMap,
			Class<? extends IAidlDownloadListener.Stub>[] listenerClazzArray,
			String customDownloadFileName, int iconType, String iconUrlInfo, int module) {
		// TODO Auto-generated method stub
		boolean result = false;
		if (context != null && urlHashMap != null && urlHashMap.size() > 0) {
			if (mIsFree) {
				// 只有是免费的才能直接下载
				String ftpUrl = urlHashMap.get(GoStorePublicDefine.URL_TYPE_HTTP_SERVER);
				if (ftpUrl != null && !"".equals(ftpUrl.trim())) {
					// 如果有FTP地址
					if (ftpUrl.contains(".apk")) {
						GoStoreOperatorUtil.downloadFileDirectly(context, mName, ftpUrl, mId,
								mPackageName, listenerClazzArray, customDownloadFileName, iconType,
								iconUrlInfo, module);
					} else {
						// GoStoreOperatorUtil.gotoBrowser(context, ftpUrl);
						//						Intent intent = DownloadTool.initBaseDownloadIntent(mName, ftpUrl,
						//								mPackageName, iconType, iconUrlInfo);
						//						context.sendBroadcast(intent);
						mName = mName.trim();
						ftpUrl = ftpUrl.trim();
						String saveFilePath = GoStoreFileUtil.getFilePath(context, mPackageName,
								mVerCode);
						if (mId == -1) {
							mId = System.currentTimeMillis();
							GoStoreOperatorUtil.downloadFileDirectly(context, mName, ftpUrl,
									mId, mPackageName, saveFilePath, mIconType, mIconInfo);
						} else {
							GoStoreOperatorUtil.downloadFileDirectly(context, mName, ftpUrl, mId,
									mPackageName, listenerClazzArray, customDownloadFileName, iconType,
									iconUrlInfo, module);
						}
						
					}
					result = true;
				} else {
					// 如果没有FTP地址
					// //统计 应用更新 下载完成
					// AppManagementStatisticsUtil.getInstance()
					// .saveUpdataComplete(context,mPackageName,String.valueOf(mId),
					// 1);
					if (mUrlOperator != null) {
						result = mUrlOperator.handleUrl(context, urlHashMap);
					}
				}
			} else {
				// 如果不是免费的
				if (mUrlOperator != null) {
					result = mUrlOperator.handleUrl(context, urlHashMap);
				}
			}
		}
		return result;
	}

}