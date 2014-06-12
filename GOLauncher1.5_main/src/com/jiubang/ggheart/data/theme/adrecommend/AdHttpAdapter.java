package com.jiubang.ggheart.data.theme.adrecommend;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Locale;

import android.content.Context;

import com.gau.utils.net.HttpConnectScheduler;
import com.gau.utils.net.IConnectListener;
import com.gau.utils.net.request.THttpRequest;

/**
 * 
 * @author HuYong
 * @version 1.0
 */
public class AdHttpAdapter {

	private Context mContext = null;
	private HttpConnectScheduler mConnectScheduler = null;
	private IConnectListener mConnectListener = null;
	public String mPId = "100"; // 客户端产品id
	// 默认结果对话框是否显示的标志位，只有在使用内部UI时又效
	public static boolean isShowDefaultListDialog = true;

	/**
	 * 无观察者构造函数，默认UI效果由内部实现。 注意：若UI效果交由内部实现，则必须在UI线程中构造该实例，否则，由于不能正常显示UI而异常。
	 * 
	 * @param context
	 *            ：当前上下文，用以获取网络服务。
	 * @param proId
	 *            ：当前产品id
	 */
	public AdHttpAdapter(Context context) {
		this(context, null);
	}

	/**
	 * 有观察者构造函数，UI效果可由外部通过observer返回的不同状态而自由实现。
	 * 
	 * @param context
	 *            ：当前上下文，用以获取网络服务或弹出提示框
	 * @param proId
	 *            ：当前产品id
	 * @param observer
	 *            ：网络状态观察者，可由外部自行控制网络连接状态
	 */
	public AdHttpAdapter(Context context, IConnectListener observer) {
		mContext = context;
		mConnectScheduler = new HttpConnectScheduler(context);
		mConnectListener = observer;
	}

	/**
	 * 对外提供获取软件列表数据的接口
	 * 
	 * @param hostAddress
	 *            服务器地址，为空则使用默认服务器地址。
	 * @param proId
	 *            插件打包id，为空则使用默认，中文182，英文184。
	 * @param ver
	 *            插件版本号，为空则使用默认值为1.0.0。
	 * @param funid
	 *            功能id，为空则使用默认值为2。
	 * @param pid
	 *            产品uid，为空则使用默认值为100。
	 * @param n
	 *            请求的条数，为空则使用默认值为10。
	 * @param userData
	 *            附上统计数据。
	 * @param fm
	 *            产品渠道号，为空则使用默认值为200。
	 * @param imei
	 *            手机imei号码，由于涉及到权限申请问题，交由外部来解决。
	 * @author HuYong
	 * @version 1.0
	 */
	public void getAdData(String hostAddress, String proId, String ver, String funid, String pid,
			int n, String userData, String fm, String imei) {
		String url = compoundAdUrl(hostAddress, proId, ver, funid, pid, n, userData, fm, imei);
		if (url == null) {
			// TODO 异常处理，返回
			return;
		}
		httpGetData(url);
		url = null;
	}

	/**
	 * 设置产品ID，该ID将在向后台请求数据时，发送。
	 * 
	 * @author huyong
	 * @param proId
	 *            :产品id
	 */
	public void setProId(String proId) {
		mPId = proId;
	}

	/**
	 * 
	 * 解析后需要返回的数据
	 * 
	 * @author HuYong
	 * @version 1.0
	 */
	public static class AdResponseData {
		public int mMaxAdId = 0; // 最大广告id,保留字段
		public int mAdCount = 0; // 广告条数
		public ArrayList<AdElement> mAdList = null; // 数据列表
	}

	/**
	 * 通过url获取数据
	 * 
	 * @author huyong
	 * @param url
	 *            获取数据的url地址
	 */
	private void httpGetData(final String url) {
		if (null == url) {
			return;
		}
		try {
			String getUrl = checkUrl(url);
			THttpRequest request = new THttpRequest(getUrl, null, mConnectListener);
			request.addHeader("Content-Type", "application/octet-stream");
			AdDataHttpOperator dataOperator = new AdDataHttpOperator();
			request.setOperator(dataOperator);
			mConnectScheduler.addRequest(request);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 校验url地址
	 * 
	 * @author huyong
	 * @param url
	 * @return
	 */
	private String checkUrl(String url) {
		String getUrl = null;
		if (url.indexOf("http://") < 0) {
			getUrl = "http://";
			getUrl += url;
		} else {
			getUrl = url;
		}
		return getUrl;
	}

	/**
	 * 拼接广告url
	 * 
	 * @return
	 */
	private String compoundAdUrl(String hostAddress, String proId, String ver, String funid,
			String pid, int n, String userData, String fm, String imei) {
		if (mContext == null) {
			return null;
		}
		String url = null;
		// 如果服务器地址为空，
		if (hostAddress == null || "".equals(hostAddress.trim())) {
			// 则使用默认地址
			url = "http://appadv.3g.cn/adv/adv.do?";
			// 测试地址
			// url = "http://61.145.124.64/adv/adv.do?";
			// 张华机子的地址
			// url = "http://192.168.214.81:8080/adv/adv.do?";
		} else {
			url = hostAddress.trim();
		}
		if (url.indexOf("?") < 0) {
			url += "?";
		}
		String connectMark = "&";

		if (proId == null || "".equals(proId.trim())) {
			Locale locale = Locale.getDefault();
			if (locale.getLanguage().equalsIgnoreCase("zh")) {
				url += "proId=182";
			} else {
				url += "proId=184";
			}
			// url += "proId=182";
		} else {
			url += "proId=" + proId.trim();
		}
		url += connectMark;

		if (ver == null || "".equals(ver.trim())) {
			url += "ver=1.0.0";
		} else {
			url += "ver=" + ver.trim();
		}
		url += connectMark;

		if (funid == null || "".equals(funid.trim())) {
			url += "funid=2";
		} else {
			url += "funid=" + funid.trim();
		}
		url += connectMark;

		url += "vps=";
		if (imei == null) {
			imei = "0000000000000000";
		}
		url += HttpUtil.getVps(mContext, imei);
		url += connectMark;

		if (pid == null || "".equals(pid.trim())) {
			url += "pid=100";
		} else {
			url += "pid=" + pid.trim();
			setProId(pid);
		}
		url += connectMark;

		if (n <= 0) {
			url += "n=10";
		} else {
			url += "n=" + n;
		}

		url += connectMark;
		url += "lang=";

		// 推荐系统，要求请求带上国家地区
		String language = null;
		Locale locale = Locale.getDefault();
		if (locale != null) {
			language = String.format("%s_%s", locale.getLanguage(), locale.getCountry())
					.toLowerCase();
		}
		// 如果取不到，默认为英语—美国
		if (language == null || "".equals(language.trim())) {
			language = "en_us";
		}

		url += language;
		url += connectMark;

		if (fm == null || "".equals(fm.trim())) {
			url += "fm=200";
		} else {
			url += "fm=" + fm.trim();
		}

		url += connectMark;
		url += String.valueOf("rd=");
		int random = (int) (Math.random() * 1000000);
		String rand = String.valueOf(random);
		url += rand;
		// 判断传进来的统计数据是否为空
		if (userData != null && !"".equals(userData.trim())) {
			// 如果不为空的就把外部传进来的统计数据带上
			userData = userData.trim();
			url += connectMark;
			url += userData;
		}

		// Log.e("goads", "URL is = " + url);
		return url;
	}

}
