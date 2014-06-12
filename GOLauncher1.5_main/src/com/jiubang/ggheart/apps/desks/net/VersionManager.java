package com.jiubang.ggheart.apps.desks.net;

import java.net.URISyntaxException;
import java.sql.Date;
import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.gau.go.launcherex.R;
import com.gau.util.unionprotocol.UnionProtocol;
import com.gau.utils.net.IConnectListener;
import com.gau.utils.net.request.THttpRequest;
import com.gau.utils.net.response.IResponse;
import com.jiubang.ggheart.appgame.base.utils.GoMarketOperatorUtil;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.net.VersionHttpOperator.ServiceIpInfoBean;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStorePhoneStateUtil;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.data.info.ThemeSettingInfo;
import com.jiubang.ggheart.data.statistics.Statistics;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.data.theme.PaidThemeInfoGetter;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 
 * 类描述:版本检查管理类
 * 功能详细描述:需要进行版本检查或上传统计数据时，需要用到该类
 * 
 * @author  huyong
 * @date  [2012-9-11]
 */
public class VersionManager {
	// 服务器地址列表机制事件代码
	public final static int EVENT_IP_SERVICE_SUCCESS = 1; // 成功
	public final static int EVENT_IP_SERVICE_EXCEPTION = 2; // 失败

	// 自动版本检测事件代码
	public final static int EVENT_AUTO_VERSION_CONTROL_SUCCESS = 1; // 成功
	public final static int EVENT_AUTO_VERSION_CONTROL_EXCEPTION = 2; // 失败
	public final static int EVENT_AUTO_VERSION_CONTROL_GET_IP = 3; // 从服务器获取IP地址列表
	public final static int EVENT_AUTO_VERSION_CONTROL_NEXT_IP = 4; // 从下一个IP地址上传

	public final static long DELAY_DAY = 5 * 24 * 60 * 60 * 1000; // 以后再说，间隔5*24
																	// 小时(按ms计)
	public final static long DELAY_DAY_SYS_NOTIC = 2 * 24 * 60 * 60 * 1000; // 以后再说，间隔2*24
																			// 小时(按ms计)

	// 统计
	private Statistics mStatistics = null;
	// 统计数据
	private String mStatisticsData = null;
	// 服务器地址列表机制Handler
	private Handler mIpServiceHandler = null;
	// 自动版本控制Handler
	private Handler mAutoVersionControlHandler = null;
	// 服务器地址列表机制网络监听器
	private IConnectListener mIpServiceNetListener = null;
	// 版本控制服务IP集合
	private ArrayList<String> mVersionControlServiceIpsArrayList = null;
	// 是否已经从服务器上获取IP列表
	private boolean mIsAlreadyGetIp = false;
	// //自动版本检测URL;
	private final static String AUTO_VERSION_CHECK_URL = "http://imupdate.3g.cn:8888/versions/check3?encrypt=1";
	// 自动版本检测测试服务器URL
//	private final static String AUTO_VERSION_CHECK_URL = "http://ggtest.3g.net.cn/versions/check3?encrypt=1";

	// 服务器地址列表服务URL
	private final static String IP_SERVICE_URL = "http://120.197.84.160:8080/serverinfo/entrance?pid=8";

	// ApplicationContext
	private Context mContext = null;

	// 网络请求开始时间
	private long mNetStartTime = 0;
	// 网络请求结束时间
	private long mNetEndTime = 0;
	
	private UnionProtocol mUnionProtocol = null;

	public VersionManager(Context context) {
		mContext = context;
		mStatistics = new Statistics(context);
		initAutoVersionControlHandler();
		initIpServiceHandler();
		initIpServiceNetListener();
		
		initVersionCheckUnionProtocol();
	}

	private void initVersionCheckUnionProtocol() {
		mUnionProtocol = new UnionProtocol(GoLauncher.getContext());
	}
	
	public Statistics getStatistics() {
		return mStatistics;
	}

	/**
	 * 自动检查版本
	 * 
	 * @author huyong
	 */
	public void autoCheckVersion() {
		if (mContext != null) {
			// 进行版本检查和上传统计数据
			versionCheckAndPostStatisticData(mContext, AUTO_VERSION_CHECK_URL);
			
			try {
				// 进行应用市场自动加载内容
				GoMarketOperatorUtil.autoGetData(mContext);
				//获取四合一付费主题相关信息
				PaidThemeInfoGetter.getPaidThemesInfo(mContext);
			} catch (Throwable e) {
				// TODO: handle exception
			}
		}
	}

	/**
	 * 初始化服务器地址列表机制的Handler
	 */
	private void initIpServiceHandler() {
		mIpServiceHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				if (msg != null) {
					switch (msg.arg1) {
						case EVENT_IP_SERVICE_SUCCESS : {
							// 请求服务IP地址成功
							if (mContext != null) {
								Object object = msg.obj;
								if (object != null && ((ArrayList<String>) object).size() > 0) {
									mVersionControlServiceIpsArrayList = (ArrayList<String>) object;
									// 如果有版本控制服务IP地址,就再进行上传
									checkAndStatistic(mContext);
								} else {
									// 重置相关变量值
									resetVersionAndStatisticData();
								}
							}
						}
							break;
						case EVENT_IP_SERVICE_EXCEPTION : {
							// 请求服务IP地址失败
							// 重置相关变量值
							resetVersionAndStatisticData();
						}
							break;
						default :
							break;
					}
				}
			}
		};
	}

	/**
	 * 获取版本控制IP集合
	 * 
	 * @return
	 */
	private ArrayList<String> getVersionControlIpsByBeans(
			ArrayList<ServiceIpInfoBean> serviceIpInfoBeans) {
		ArrayList<String> ips = null;
		if (serviceIpInfoBeans != null && serviceIpInfoBeans.size() > 0) {
			for (ServiceIpInfoBean serviceIpInfoBean : serviceIpInfoBeans) {
				if (serviceIpInfoBean != null) {
					if (ServiceIpInfoBean.SERVICE_ID_VERSION_CONTROL == serviceIpInfoBean
							.getServerId()) {
						ips = serviceIpInfoBean.getIps();
					}
				}
			}
		}
		return ips;
	}

	/**
	 * 初始化服务器地址列表机制的网络监听器的方法
	 */
	private void initIpServiceNetListener() {
		mIpServiceNetListener = new IConnectListener() {
			@Override
			public void onStart(THttpRequest arg0) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onFinish(THttpRequest arg0, IResponse arg1) {
				// TODO Auto-generated method stub
				// 服务IP请求成功
				Message message = null;
				if (mIpServiceHandler != null) {
					message = mIpServiceHandler.obtainMessage();
					message.arg1 = EVENT_IP_SERVICE_SUCCESS;
					message.obj = null;
				}
				if (arg1 != null && arg1.getResponseType() == IResponse.RESPONSE_TYPE_STREAM) {
					Object object = arg1.getResponse();
					if (object != null) {
						ArrayList<ServiceIpInfoBean> serviceIpInfoBeans = (ArrayList<ServiceIpInfoBean>) object;
						ArrayList<String> ips = getVersionControlIpsByBeans(serviceIpInfoBeans);
						if (ips != null && ips.size() > 0) {
							// 如果返回的数据有版本检查服务IP地址
							if (message != null) {
								message.obj = ips;
							}
						}
					}
				}
				if (message == null || message.obj == null) {
					// 如果返回的数据没版本检查服务IP地址
					// 通知各模块
					if (mStatistics != null) {
						mStatistics.exceptionStatisticsData();
					}
				}
				if (mIpServiceHandler != null && message != null) {
					mIpServiceHandler.sendMessage(message);
				}
			}

			@Override
			public void onException(THttpRequest arg0, int arg1) {
				// 如果请求列表地址也失败了，通知各模块
				StatisticsData.saveHttpExceptionDate(mContext, arg0, arg1);
				if (mStatistics != null) {
					mStatistics.exceptionStatisticsData();
				}
				// 发送消息
				if (mIpServiceHandler != null) {
					Message message = mIpServiceHandler.obtainMessage();
					message.arg1 = EVENT_IP_SERVICE_EXCEPTION;
					mIpServiceHandler.sendMessage(message);
				}
			}
		};
	}

	/**
	 * 进行版本检测和上传统计数据的方法
	 */
	private void checkAndStatistic(Context context) {
		if (context != null) {
			if (mVersionControlServiceIpsArrayList != null
					&& mVersionControlServiceIpsArrayList.size() > 0) {
				String ip = mVersionControlServiceIpsArrayList.get(0);
				if (ip != null && !"".equals(ip.trim())) {
					String format = context.getString(R.string.version_control_url_format);
					String url = String.format(format, ip);
					if (url != null && !"".equals(url.trim())) {
						versionCheckAndPostStatisticData(context, url);
					}
				}
			}
		}
	}

	/**
	 * 进行版本检测和上传统计数据的方法
	 */
	private void versionCheckAndPostStatisticData(Context context, String url) {
		if (context != null && url != null && !"".equals(url.trim())) {
			// 获取统计数据
			if (mStatisticsData == null) {
				if (mStatistics != null) {
					// 获取统计数据
					mStatisticsData = mStatistics.getStatisticsData();
				}
			}
			String uid = GoStorePhoneStateUtil.getUid(context);
//			UnionProtocol unionProtocol = new UnionProtocol(GoLauncher.getContext());
			// unionProtocol.checkGOWidget(uid, url, mStatisticsData, new
			// UnionNetCallBack(unionProtocol));

			mUnionProtocol.checkGOPlugin(uid, url, mStatisticsData, new UnionNetCallBack(
					mUnionProtocol));
		}
	}

	/**
	 * 自动版本检查回调
	 * 
	 * @author wangzhuobin
	 * 
	 */
	private class UnionNetCallBack extends UnionProtocol.ConnectListener {

		public UnionNetCallBack(UnionProtocol unionProtocol) {
			unionProtocol.super();
		}

		@Override
		public void onException(THttpRequest request, int reason) {
			super.onException(request, reason);
			StatisticsData.saveHttpExceptionDate(mContext, request, reason);
			// Log.i("getView",
			// "onException--------------------------------------------------------");
			Message message = null;
			if (mAutoVersionControlHandler != null) {
				message = mAutoVersionControlHandler.obtainMessage();
				message.arg1 = EVENT_AUTO_VERSION_CONTROL_EXCEPTION;
			}
			if (mIsAlreadyGetIp) {
				// 如果已经 尝试从服务器获取IP列表了
				if (mVersionControlServiceIpsArrayList != null
						&& mVersionControlServiceIpsArrayList.size() > 1) {
					// 如果还有下一个IP地址
					message.arg1 = EVENT_AUTO_VERSION_CONTROL_NEXT_IP;
				} else {
					// 如果已经没有其它地址了，通知各模块
					if (mStatistics != null) {
						mStatistics.exceptionStatisticsData();
					}
					if (message != null) {
						message.arg1 = EVENT_AUTO_VERSION_CONTROL_EXCEPTION;
					}
				}
			} else {
				// 没有从服务器取过地址，就发送消息去取地址
				if (message != null) {
					message.arg1 = EVENT_AUTO_VERSION_CONTROL_GET_IP;
				}
			}
			if (mAutoVersionControlHandler != null && message != null) {
				mAutoVersionControlHandler.sendMessage(message);
			}
		}

		@Override
		public void onFinish(THttpRequest request, IResponse response) {
			// 记录成功返回时间
			mNetEndTime = System.currentTimeMillis();

			super.onFinish(request, response);
			// Log.i("getView",
			// "onFinish------------------------------------------");
			// 版本检测和统计数据上传成功
			// 通知清空原来的统计数据
			if (mStatistics != null) {
				mStatistics.clearStatisticsData();
			}
			StatisticsData.resetData(mContext);
			// 发送统计上传成功消息
			if (mAutoVersionControlHandler != null) {
				Message message = mAutoVersionControlHandler.obtainMessage();
				message.arg1 = EVENT_AUTO_VERSION_CONTROL_SUCCESS;
				mAutoVersionControlHandler.sendMessage(message);
			}
		}

		@Override
		public void onStart(THttpRequest request) {
			super.onStart(request);
			// 记录开始时间
			mNetStartTime = System.currentTimeMillis();
		}
	}

	/**
	 * 初始化版本控制Handler
	 */
	private void initAutoVersionControlHandler() {
		mAutoVersionControlHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				if (msg != null) {
					switch (msg.arg1) {
						case EVENT_AUTO_VERSION_CONTROL_GET_IP : {
							if (mContext != null) {
								// 改变标志位
								mIsAlreadyGetIp = true;
								// 请求服务IP列表
								getServiceIpsFromNet(IP_SERVICE_URL);
							}
						}
							break;
						case EVENT_AUTO_VERSION_CONTROL_NEXT_IP : {
							if (mVersionControlServiceIpsArrayList != null
									&& mVersionControlServiceIpsArrayList.size() > 1) {
								// 先把原来上传失败的地址移除
								mVersionControlServiceIpsArrayList.remove(0);
								if (mContext != null) {
									// 重新检测与上传
									checkAndStatistic(mContext);
								}
							}
						}
							break;
						case EVENT_AUTO_VERSION_CONTROL_SUCCESS : {
							// 保存网络请求成功使用时间
							if (mStatistics != null) {
								mStatistics.saveNetUseTime(mNetEndTime - mNetStartTime);
							}
							// 重置相关变量值
							resetVersionAndStatisticData();
						}
							break;
						case EVENT_AUTO_VERSION_CONTROL_EXCEPTION : {
							// 重置相关变量值
							resetVersionAndStatisticData();
						}
							break;
						default :
							break;
					}
				}
			}
		};
	}

	/**
	 * 重置版本控制与统计的相关变量值
	 */
	private void resetVersionAndStatisticData() {
		// 清空统计数据
		mStatisticsData = null;
		// 改变标志
		mIsAlreadyGetIp = false;
		// 清空IP地址列表地址集合
		if (mVersionControlServiceIpsArrayList != null) {
			mVersionControlServiceIpsArrayList.clear();
			mVersionControlServiceIpsArrayList = null;
		}
		mNetEndTime = 0;
		mNetStartTime = 0;
	}

	/**
	 * 从服务器请求服务IP地址的方法
	 * 
	 * @param context
	 * @param url
	 */
	private void getServiceIpsFromNet(String url) {
		if (url != null && !"".equals(url.trim())) {
			try {
				THttpRequest request = new THttpRequest(url, null, mIpServiceNetListener);
				VersionHttpOperator operator = new VersionHttpOperator();
				request.setOperator(operator);
				AppCore.getInstance().getHttpAdapter().addTask(request);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void startCheckUpdate() {
		// 自动版本检查
		new Thread() {
			public void run() {
				autoCheckVersion();
			};
		}.start();
	}

	public void saveCurTime() {
		// 保存当前时间值
		final GoSettingControler settingControler = GOLauncherApp.getSettingControler();
		final ThemeSettingInfo info = settingControler.getThemeSettingInfo();
		info.mLastShowTime = System.currentTimeMillis();
		settingControler.updateThemeSettingInfo(info);
	}

	/**
	 * 清除本对象内存数据
	 * 
	 * @author huyong
	 */
	public void clearData() {
		mStatistics = null;
		mStatisticsData = null;
		mIpServiceHandler = null;
		mAutoVersionControlHandler = null;
		mIpServiceNetListener = null;
		if (mVersionControlServiceIpsArrayList != null) {
			mVersionControlServiceIpsArrayList.clear();
			mVersionControlServiceIpsArrayList = null;
		}
		mContext = null;
	}

	/**
	 * 计算从指定时间开始到当前时间的间隔值，单位是小时
	 * 
	 * @author huyong
	 * @param theDate
	 *            单位是milliseconds
	 * @return
	 */
	private long getPeriodToCurTime(final long theDate) {
		Date curDate = new Date(System.currentTimeMillis());
		Date preDate = new Date(theDate);
		// delay单位是小时
		long delay = curDate.getTime() - preDate.getTime();
		return delay;
	}

}
