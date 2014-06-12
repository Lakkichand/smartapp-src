package com.jiubang.ggheart.apps.desks.diy.messagecenter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Random;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.Beans.MessageListBean.MessageHeadBean;

/**
 * 
 * 类描述:HttpUtil工具类
 * 功能详细描述:  获取VPS 、日期格式的字符串转换为长整型
 * 				消息列表排序、保存和获取消息的时间
 * @date  [2012-9-28]
 */
public class HttpUtil {

	private static final String VPS_VERSION = "01.01.01"; // 目前在用的VPS版本

	/**
	 * 根据vps定义规范，获取本机vps信息(没有经过UTF-8编码)
	 * 详细的请参考消息中心的协议文档
	 * @param context
	 * @param imei
	 * @return vps字符串
	 */
	public static String getVps(Context context, String imei) {
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager wMgr = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		wMgr.getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;
		int height = dm.heightPixels;

		StringBuilder vpsStringBuilder = new StringBuilder(64);
		vpsStringBuilder.append("1#");
		vpsStringBuilder.append("Android#");
		vpsStringBuilder.append(Build.MODEL + "#");
		vpsStringBuilder.append(imei + "#");
		vpsStringBuilder.append("166#");
		vpsStringBuilder.append(width + "_" + height + "#");
		vpsStringBuilder.append(VPS_VERSION);
		String vps = vpsStringBuilder.toString();
		// edit by chenguanyu
		try {
			vps = URLEncoder.encode(vps, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		// end edit
		return vps;
	}

	/**
	 * 将日期格式的字符串转换为长整型
	 * 
	 * @param date
	 * @param format
	 * @return
	 */
	public static long convert2long(String date, String format) {
		try {
			SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			return sf.parse(date).getTime();
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0l;
	}
	/**
	 * 功能简述: 消息列表排序
	 * 功能详细描述: 消息列表排序 ，未读的放在前面，已读的放在后面
	 */
	@SuppressWarnings("unchecked")
	public static void sortList(Vector<MessageHeadBean> list) {
		if (list == null) {
			return;
		}
		Vector<MessageHeadBean> unReadMessageTitles = new Vector<MessageHeadBean>();
		Vector<MessageHeadBean> readMessageTitles = new Vector<MessageHeadBean>();
		//按mMsgTimeStamp排序，比较规则在MessagaeComparator内
		MessageComparator comparator = new MessageComparator();
		Collections.sort(list, comparator);

		for (int i = 0; i < list.size(); i++) {
			MessageHeadBean item = list.get(i);
			if (item.misReaded) {
				readMessageTitles.add(item);
			} else {
				unReadMessageTitles.add(item);
			}
		}

		list.clear();
		list.addAll(unReadMessageTitles);
		list.addAll(readMessageTitles);

	}

	public static String getUrl(int type, String url) {
		StringBuffer buffer = new StringBuffer(url);
		Random random = new Random(new Date().getTime());
		if (ConstValue.URL_GET_MSG_LIST == type) {
			buffer.append("funid=1&rd=" + random.nextLong());
		} else if (ConstValue.URL_GET_MSG_CONTENT == type) {
			buffer.append("funid=2&rd=" + random.nextLong());
		} else if (ConstValue.URLPOST_MSG_STATICDATA == type) {
			buffer.append("funid=5&rd=" + random.nextLong());
		} else if (ConstValue.URL_GET_URL == type) { //桌面后台链接
			buffer.append("funid=3&rd=" + random.nextLong());
		}
		random = null;
		return buffer.toString();
	}

	public static void saveLastUpdateMsgTime(Context context, long time) {
		PreferencesManager sharedPreferences = new PreferencesManager(context,
				IPreferencesIds.SHAREDPREFERENCES_MSG_UPDATE, Context.MODE_PRIVATE);
		sharedPreferences.putLong(IPreferencesIds.SHAREDPREFERENCES_MSG_UPDATE, time);
		sharedPreferences.commit();
	}

	public static long getLastUpdateMsgTime(Context context) {
		PreferencesManager sharedPreferences = new PreferencesManager(context,
				IPreferencesIds.SHAREDPREFERENCES_MSG_UPDATE, Context.MODE_PRIVATE);
		long lastCheckedTime = 0L;
		if (sharedPreferences != null) {
			lastCheckedTime = sharedPreferences.getLong(
					IPreferencesIds.SHAREDPREFERENCES_MSG_UPDATE, 0L);
		}
		return lastCheckedTime;
	}
	/**
	 * 
	 */
	@SuppressWarnings("rawtypes")
	public static class MessageComparator implements Comparator {

		@Override
		public int compare(Object object1, Object object2) {
			// TODO Auto-generated method stub
			MessageHeadBean msg1 = (MessageHeadBean) object1;
			MessageHeadBean msg2 = (MessageHeadBean) object2;
			return msg2.mMsgTimeStamp.compareTo(msg1.mMsgTimeStamp);
		}

	}
	/**
	 * <br>功能简述:解压一个压缩文档 到指定位置
	 * <br>功能详细描述:
	 * <br>注意:如果制定文件夹内有相同文件则不覆盖
	 * @param zipFileString
	 * @param outPathString
	 */
	public static void unZipFolder(String zipFileString, String outPathString) {
		unZipFolder(zipFileString, outPathString, false);
	}
	/**
	 * 解压一个压缩文档 到指定位置
	 * @param zipFileString	压缩包的名字
	 * @param outPathString	指定的路径
	 * @param replace 是否替换相同文件
	 * @throws Exception
	 */
	public static void unZipFolder(String zipFileString, String outPathString, boolean replace) {
		File zipFile = new File(zipFileString);
		if (!zipFile.exists()) {
			return;
		}
		File outPutFile = null;
		ZipInputStream inZip = null;
		try {
			inZip = new ZipInputStream(new FileInputStream(zipFileString));
			ZipEntry zipEntry;
			String szName = "";
			File dir = new File(outPathString);
			if (!dir.exists()) {
				dir.mkdir();
			}
			while ((zipEntry = inZip.getNextEntry()) != null) {
				szName = zipEntry.getName();

				if (zipEntry.isDirectory()) {
					// get the folder name of the widget
					szName = szName.substring(0, szName.length() - 1);
					File folder = new File(outPathString + File.separator + szName);
					folder.mkdirs();

				} else {

					outPutFile = new File(outPathString + File.separator + szName);
					if (outPutFile.exists()) {
						if (replace) {
							outPutFile.delete();
						} else {
							continue;
						}
					}
					outPutFile.createNewFile();
					FileOutputStream out = new FileOutputStream(outPutFile);
					int len;
					byte[] buffer = new byte[1024];
					while ((len = inZip.read(buffer)) != -1) {
						out.write(buffer, 0, len);
						out.flush();
					}
					out.close();
				}
			}
			inZip.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			if (outPutFile != null && outPutFile.exists()) {
				outPutFile.delete();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			if (outPutFile != null && outPutFile.exists()) {
				outPutFile.delete();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public static void sortFile(String[] files) {
		if (files == null) {
			return;
		}
		FileWrapper[] fileWrappers = new FileWrapper[files.length];
		for (int i = 0; i < files.length; i++) {
			fileWrappers[i] = new FileWrapper(files[i]);
		}
		Arrays.sort(fileWrappers);
		for (int i = 0; i < files.length; i++) {
			files[i] = fileWrappers[i].getName();
		}
	}

	/**
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 * 
	 * @author  rongjinsong
	 * @date  [2012-10-17]
	 */
	public static class FileWrapper implements Comparable {
		/** File */
		public String mName;

		public FileWrapper(String file) {
			mName = file;
		}

		public int compareTo(Object obj) {
			assert obj instanceof FileWrapper;

			FileWrapper castObj = (FileWrapper) obj;

			if (mName.compareTo(castObj.mName) > 0) {
				return 1;
			} else if (mName.compareTo(castObj.mName) < 0) {
				return -1;
			} else {
				return 0;
			}
		}
		public String getName() {
			return mName;
		}
	}

}
