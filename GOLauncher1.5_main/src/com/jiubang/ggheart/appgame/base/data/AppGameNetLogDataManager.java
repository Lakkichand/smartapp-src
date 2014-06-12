package com.jiubang.ggheart.appgame.base.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;

import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.data.statistics.Statistics;

/**
 * 网络信息的数据管理类，包括网络信息的保存和上传
 * 
 * @author zhoujun
 * 
 */
public class AppGameNetLogDataManager {
	/**
	 * 保存网络日志的shareferfenrce
	 */
	private String mSharePreferenceName = "";
	// private Context mContext ;
//	public AppGameNetLogDataManager(Context context) {
//		// mContext = context;
//	};

	public AppGameNetLogDataManager() {
		this(IPreferencesIds.PREFENRCE_NETLOG_STATISTICS_DATA);
	}

	public AppGameNetLogDataManager(String sharePreferenceName) {
		mSharePreferenceName = sharePreferenceName;
	}

	/**
	 * 保存网络日志
	 * 
	 * @param context
	 * @param data
	 */
	public synchronized void saveNetLogData(Context context, String data) {
		PreferencesManager sp = new PreferencesManager(context, mSharePreferenceName,
				Context.MODE_PRIVATE);
		int size = 1;
		if (!"".equals(sp.getString("1", ""))) {
			size = sp.getAll().size() + 1;
		}
		// Log.d("zj", "size:" + size + " and save data:" + data);
		sp.putString(String.valueOf(size), data);
		sp.commit();
	}

	/**
	 * 上传网络信息日志
	 * 
	 * @param context
	 */
	public void sendNetLog(final Context context, final String url) {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				String netLogData = getNetLogData(context);
//				 Log.d("zj", "send data:" + netLogData);
				byte[] zipData = zipNetLogData(netLogData);
				if (zipData != null && zipData.length > 0) {
					if (sendNetLog(zipData, url)) {
						// 删除之前的网络日志
						clearNetLogData(context);
					}
				}
			}
		});
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
	}

	/**
	 * 获取SharedPreferences中收集的网络信息
	 * 
	 * @param context
	 * @return
	 */
	private String getNetLogData(Context context) {
		PreferencesManager sp = new PreferencesManager(context, mSharePreferenceName,
				Context.MODE_PRIVATE);
		Map<String, ?> netLogMap = sp.getAll();
		StringBuffer dataBuffer = new StringBuffer();
		if (netLogMap != null) {
			Iterator<String> iter = netLogMap.keySet().iterator();
			while (iter.hasNext()) {
				dataBuffer.append(netLogMap.get(iter.next())).append("\n");
			}
		}
		return dataBuffer.toString();
	}

	/**
	 * 删除之前的网络日志
	 * 
	 * @param context
	 */
	private synchronized void clearNetLogData(Context context) {
		PreferencesManager sp = new PreferencesManager(context, mSharePreferenceName,
				Context.MODE_PRIVATE);
		sp.clear();
	}

	/**
	 * 网络请求，发送日志
	 * 
	 * @param headerInfo
	 * @param url
	 * @return
	 */
	private boolean sendNetLog(byte[] headerInfo, String url) {
		boolean success = false;
		try {
			ByteArrayEntity be = new ByteArrayEntity(headerInfo);
			HttpPost hp = new HttpPost(url);
			hp.setEntity(be);
			new DefaultHttpClient().execute(hp);
			success = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return success;
	}

	/**
	 * 对上传的数据进行压缩
	 * 
	 * @param statistics
	 * @return
	 */
	private byte[] zipNetLogData(String statistics) {
		byte[] result = null;
		if (statistics != null && !"".equals(statistics)) {
			ByteArrayOutputStream byteOutputStream = null;
			GZIPOutputStream gzipOutputStream = null;
			try {
				byteOutputStream = new ByteArrayOutputStream();
				gzipOutputStream = new GZIPOutputStream(byteOutputStream);
				gzipOutputStream.write(statistics.getBytes(Statistics.STATISTICS_DATA_CODE));
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (gzipOutputStream != null) {
						gzipOutputStream.flush();
						gzipOutputStream.close();
					}
					if (byteOutputStream != null) {
						byteOutputStream.flush();
						byteOutputStream.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			result = byteOutputStream.toByteArray();
		}
		return result;
	}

}
