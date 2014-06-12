package com.jiubang.ggheart.components.advert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;

import com.go.util.AppUtils;
import com.go.util.CompatibleUtil;
import com.go.util.device.Machine;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.desks.dock.DockUtil;
import com.jiubang.ggheart.apps.desks.imagepreview.ImagePreviewResultType;
import com.jiubang.ggheart.components.gohandbook.SharedPreferencesUtil;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.DesktopSettingInfo;
import com.jiubang.ggheart.data.info.IItemType;
import com.jiubang.ggheart.data.info.ItemInfo;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.data.info.UserFolderInfo;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;

/**
 * 
 * <br>类描述:15屏幕广告控制类
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-12-7]
 */
public class AdvertControl {
	private static AdvertControl sInstance;
	public Context mContext;
	public SharedPreferencesUtil mPreferencesUtil;
	public ArrayList<AdvertInfo> mAdvertInfosList;
	public int mAddSize = 0; //一共需要添加多少个图标	
	public int mImageDownSize = 0;	//已经完成下载的个数（包括成功和失败）
	public static final String ADVERT_CAN_REQUEST = "advert_can_request"; //是否可以请求广告数据
	private BroadcastReceiver mNetWorkReceiver = null;	//网络监听器
	private PreferencesManager mPreferencesManager = null;
	private PreferencesManager mOpenPreferencesManager = null;	//8小时提示缓存

	private JSONArray mAddScreenJsonArray;	//插入屏幕的临时JSON缓存
	
	private boolean mIsRequsetAgain = false; //是否24小时请求
	private HashMap<View, ValueAnimator> mShakeAnimatorMaps;  //图标抖动队列
	
	public static synchronized AdvertControl getAdvertControlInstance(Context context) {
		if (sInstance == null) {
			sInstance = new AdvertControl(context);
		}
		return sInstance;
	}

	public AdvertControl(Context context) {
		mContext = context;
		mPreferencesUtil = new SharedPreferencesUtil(mContext);
		mPreferencesManager = new PreferencesManager(context, IPreferencesIds.ADVERT_SCREEN_DATA,
				Context.MODE_WORLD_READABLE);
	}

	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			//下载数据成功
				case AdvertConstants.GET_ADVERT_DATA_SUCCESS :
					log("请求服务器数据--成功!");
					handleAdvertData(msg.obj);
					break;

				//下载数据失败	
				case AdvertConstants.GET_ADVERT_DATA_FAIL :
					log("请求服务器数据--失败！");
					break;

				//下载图片成功	
				case AdvertConstants.DOWN_IMAGE_SUCCESS :
					log("下载图片--成功");
					handleImageDown(msg.obj);
					break;

				//下载图片失败
				case AdvertConstants.DOWN_IMAGE_FAIL :
					log("下载图片--失败");
					handleImageDown(msg.obj);
					break;

				//统计上传成功
				case AdvertConstants.STATISTICS_REQUEST_SUCCESS :
					log("统计上传--成功");
					//					clearStatisticsData(); //上传成功就要清空本地统计数据
					break;

				//统计上传失败
				case AdvertConstants.STATISTICS_REQUEST_FAIL :
					log("统计上传--失败");

					break;

				default :
					break;
			}
		}

	};

	/**
	 * <br>功能简述:请求服务器获取广告数据
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void requestAdvertData() {
		//设置首屏幕首次默认图标的缓存
		setFistHomeScreenCache();
		new Thread() {
			public void run() {
				if (checkNeedRequestData()) {
					log("第一次请求");
					AdvertUtils.getAdvertData(mContext, mHandler);
					saveRequestTime(); //设置请求时间
				}
			}
		}.start();
	}

	/**
	 * <br>功能简述:注册网络监听器，监听网络的联通
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void registerNetWorkReceiver() {
		if (mNetWorkReceiver == null) {
			log("注册网络状态监听！");
			mNetWorkReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
						if (Machine.isNetworkOK(mContext)) {
							log("监听到网络联通！");
							unRegisterNetWorkReceiver(); //取消册网络状态监听
							requestAdvertData();
						}
					}
				}
			};
			IntentFilter filter = new IntentFilter();
			filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
			mContext.registerReceiver(mNetWorkReceiver, filter);
		}
	}

	/**
	 * <br>功能简述:取消册网络状态监听
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void unRegisterNetWorkReceiver() {
		if (mNetWorkReceiver != null) {
			log("取消册网络状态监听！");
			mContext.unregisterReceiver(mNetWorkReceiver);
			mNetWorkReceiver = null;
		}
	}

	/**
	 * <br>功能简述:检查是否需要请求服务器获取数据
	 * <br>功能详细描述:
	 * <br>如果没有网络时就会注册网络状态监听,存在网络代表会请求服务器获取数据。不管是否获取成功。否代表已经请求过。那下次就不会再请求了
	 * @return
	 */
	public boolean checkNeedRequestData() {
		//判断是否第一次请求
		boolean isCanRequest = mPreferencesUtil.getBoolean(ADVERT_CAN_REQUEST, false);
		if (!isCanRequest) {
			log("已经请求过，不再请求！");
			return false;
		}

		//判断可以插入到屏幕（是否一共5屏，是否1 5屏为空白屏幕）
		if (!isCanAddIconToScreen()) {
			log("15屏已经做过修改。不能插入！");
			//设置不能在请求数据
			setCanRequestAdvertState(false);
			return false;
		}

		if (!isCanAddIconToHomeScreen()) {
			log("首幕已经做过修改。不能插入！");
			//设置不能在请求数据
			setCanRequestAdvertState(false);
			return false;
		}

		//判断是否SD卡存在
		if (!isSdCardExist()) {
			log("没有SD卡！");
			return false;
		}

		//判断是否有网络
		boolean isHasNetWork = Machine.isNetworkOK(mContext);
		if (!isHasNetWork) {
			log("没有网络！");
			registerNetWorkReceiver();
			return false;
		} else {
			//如果存在网络则代表已经请求过。设置下次不能再请求
			setCanRequestAdvertState(false);
		}
		return true;
	}

	/**
	 * <br>功能简述:检查是否需要请求服务器获取数据
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public boolean checkCanAddIcon() {

		//判断是否SD卡存在
		if (!isSdCardExist()) {
			log("没有SD卡！");
			return false;
		}

		//判断可以插入到15幕（是否一共5屏，是否1 5屏为空白屏幕）
		if (!isCanAddIconToScreen()) {
			log("15屏幕已经做过修改。不能插入！");
			return false;
		}
		
		//判断可以插入到首幕（是否一共5屏，是否1 5屏为空白屏幕）
		if (!isCanAddIconToHomeScreen()) {
			log("首屏已经做过修改。不能插入！");
			return false;
		}

		return true;
	}

	/**
	 * <br>功能简述:判断可以插入到屏幕（是否一共5屏，是否1 5屏为空白屏幕）
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public boolean isCanAddIconToScreen() {
		boolean isCanAddIconToScreen = GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
				IDiyMsgIds.SCREEN_CAN_ADD_ADVERT_SHORT_CUT, -1, null, null);
		return isCanAddIconToScreen;
	}

	/**
	 * <br>功能简述:判断是否SD卡存在
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public boolean isSdCardExist() {
		boolean isSdCardExist = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
		return isSdCardExist;
	}

	/**
	 * <br>功能简述:处理请求返回的广告数据
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param data
	 */
	public void handleAdvertData(Object data) {
		if (data != null && data instanceof ArrayList) {
			mAdvertInfosList = (ArrayList<AdvertInfo>) data;
			for (AdvertInfo advertInfo : mAdvertInfosList) {
				if (advertInfo != null) {
					//不是文件夹
					if (advertInfo.mIsfile == AdvertConstants.IS_NO_FILE) {
						downLoadImage(advertInfo);
					}
					//是文件夹
					else if (advertInfo.mIsfile == AdvertConstants.IS_FILE) {
						ArrayList<AdvertInfo> fileAdvertInfoList = advertInfo.mFilemsg;
						if (fileAdvertInfoList != null) {
							for (AdvertInfo fileAdvertInfo : fileAdvertInfoList) {
								downLoadImage(fileAdvertInfo);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * <br>功能简述:下载图片
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param advertInfo
	 */
	public void downLoadImage(AdvertInfo advertInfo) {
		mAddSize = mAddSize + 1;	//总添加数+1
		AdvertUtils.getNetImageData(mContext, advertInfo, mHandler);
	}

	/**
	 * <br>功能简述:处理请求下载图片
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param data
	 */
	public void handleImageDown(Object data) {
		mImageDownSize = mImageDownSize + 1;
		//判断下载数量是否等于请求数量
		if (mImageDownSize == mAddSize) {
			log("全部图片下载完成！");
			
			//判断是否24小时后的重新请求，是就检查是否可以清空15屏，图标没有做过任何改变才能清空
			if (mIsRequsetAgain) {
				if (!isClecrAdvertIcon()) {
					log("删除15屏图标-失败！");
					return;
				} else {
					setHomeScreenCache(); //删除首屏图标后。要重新设置当前缓存，不然下面的checkCanAddIcon（）判断缓存时会不通过
					log("删除15屏图标--成功！");
				}
			}
			
			//判断15屏是否可以插入图标（是否为空且是5屏）
			if (!checkCanAddIcon()) {
				return;
			}
			
			AdvertUtils.clearInstallListCache(mContext);	//清空安装缓存提示列表
			
			mAddScreenJsonArray = new JSONArray();
			if (mAdvertInfosList != null && mAdvertInfosList.size() != 0) {
				for (AdvertInfo advertInfo : mAdvertInfosList) {
					if (advertInfo != null) {
						//不是文件夹
						if (advertInfo.mIsfile == AdvertConstants.IS_NO_FILE) {
							addShortCut(advertInfo);
						}

						//是文件夹
						else if (advertInfo.mIsfile == AdvertConstants.IS_FILE) {
							//不是轮换文件夹
							if (advertInfo.mIscarousel == AdvertConstants.IS_NO_CAROUSEL) {
								ArrayList<AdvertInfo> fileAdvertInfoList = advertInfo.mFilemsg;
								addFolder(advertInfo, fileAdvertInfoList);
							}
							
							//是轮换文件夹
							else if (advertInfo.mIscarousel == AdvertConstants.IS_CAROUSEL) {
								//获取那个图标可以添加上去
								AdvertInfo carouselAdverttInfo = getCarouselIcon(advertInfo);
								if (carouselAdverttInfo != null) {
									addShortCut(carouselAdverttInfo);
								}
							}
						}
					}
				}
				
				saveAddScreenCache(); //保存请求JSON缓存到xml
				setHomeScreenCache(); //第一次插入图标完成后把首屏图标信息保存起来
			}
			if (mAdvertInfosList != null) {
				mAdvertInfosList.clear();
			}
		}
	}

	/**
	 * <br>功能简述:设置不可以请求广告数据
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void setCanRequestAdvertState(boolean flag) {
		if (mPreferencesUtil != null) {
			log("设置请求广告参数：" + flag);
			mPreferencesUtil.saveBoolean(ADVERT_CAN_REQUEST, flag);
		}
	}

	/**
	 * <br>功能简述:添加广告图标到桌面
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param advertInfo
	 */
	public void addShortCut(AdvertInfo advertInfo) {
		try {
			ShortCutInfo shortCutInfo = initShortCutInfo(advertInfo);
			if (shortCutInfo != null) {
				AdvertUtils.setInstallListCache(mContext, shortCutInfo); //保存15屏幕推荐图标包名列表
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
						IDiyMsgIds.SCREEN_ADD_ADVERT_SHORT_CUT, advertInfo.mScreen, shortCutInfo,
						null);
				setAppIconCacheJson(advertInfo);	//设置json缓存
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * <br>功能简述:添加广告文件夹到桌面
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param advertInfo
	 */
	public void addFolder(AdvertInfo advertInfoFolder, ArrayList<AdvertInfo> advertInfosList) {
		//JSON缓存队列
		ArrayList<AdvertInfo> advertInfosListTemp = new ArrayList<AdvertInfo>();
		
		UserFolderInfo deskFolder = new UserFolderInfo();
		deskFolder.mInScreenId = System.currentTimeMillis();
		deskFolder.mFeatureTitle = advertInfoFolder.mTitle;

		int[] xy = getXY(advertInfoFolder.mPos);
		deskFolder.mCellX = xy[0];
		deskFolder.mCellY = xy[1];

		int size = advertInfosList.size();
		for (int i = 0; i < size; i++) {
			AdvertInfo advertInfo = advertInfosList.get(i);
			ShortCutInfo shortCutInfo = initShortCutInfo(advertInfo);
			if (shortCutInfo != null) {
				deskFolder.add(shortCutInfo);	
				advertInfosListTemp.add(advertInfo); //成功后的才写入缓存
				AdvertUtils.setInstallListCache(mContext, shortCutInfo); //保存15屏幕推荐图标包名列表
			}
		}

		//判断个数是否>0
		ArrayList<ItemInfo> contents = deskFolder.getContents();
		if (contents != null && contents.size() > 0) {
			GoLauncher
					.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
							IDiyMsgIds.SCREEN_ADD_ADVERT_FOLDER, advertInfoFolder.mScreen,
							deskFolder, null);
			setFolderIconCacheJson(advertInfoFolder, advertInfosListTemp); //设置非文件夹图标的json缓存
		}
	}

	/**
	 * <br>功能简述:获取ShortCutInfo对象。
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param advertInfo 广告对象
	 * @return
	 */
	public ShortCutInfo initShortCutInfo(AdvertInfo advertInfo) {
		if (advertInfo == null) {
			return null;
		}
		
		ShortCutInfo shortCutInfo = new ShortCutInfo();
		Intent intent = new Intent(ICustomAction.ACTION_SCREEN_ADVERT);
		//要设置不同包命，不然会认为是同一个应用。合并文件夹时会合并
		ComponentName advertCN = new ComponentName(advertInfo.mPackageName,
				ICustomAction.ACTION_SCREEN_ADVERT);
		intent.setComponent(advertCN);
		intent.putExtra(AdvertConstants.ADVERT_ID, advertInfo.mId);	//保存ID
		intent.putExtra(AdvertConstants.ADVERT_ACTVALUE, advertInfo.mActvalue);	//保存跳转信息
		intent.putExtra(AdvertConstants.ADVERT_PACK_NAME, advertInfo.mPackageName);	//保存程序包名
		intent.putExtra(AdvertConstants.ADVERT_CLICK_URL, advertInfo.mClickurl);	//对应的回调地址，只有点击时才需要上传
		intent.putExtra(AdvertConstants.ADVERT_MAPID, advertInfo.mMapid);	//统计id
		intent.putExtra(AdvertConstants.ADVERT_TITLE, advertInfo.mTitle); // 保存标题

		shortCutInfo.mIntent = intent;
		shortCutInfo.mInScreenId = System.currentTimeMillis();	//图标id
		shortCutInfo.mItemType = IItemType.ITEM_TYPE_APPLICATION; //类型

		int[] xy = getXY(advertInfo.mPos);

		shortCutInfo.mCellX = xy[0];
		shortCutInfo.mCellY = xy[1];

		String titleString = advertInfo.mTitle;	//标题
		shortCutInfo.mFeatureTitle = titleString;	//保存到数据库的标题
		shortCutInfo.setTitle(titleString, true);	//缓存的标题
		//设置图片类型为自定义路径
		String pathString = advertInfo.mIcon;
		//		Log.i("lch", "pathString:" + pathString);
		if (pathString.startsWith("http://")) {
			return null;
		}
		shortCutInfo.setFeatureIcon(null, ImagePreviewResultType.TYPE_IMAGE_FILE, null, 0,
				pathString);

		if (shortCutInfo.prepareFeatureIcon()) {
			shortCutInfo.mIcon = shortCutInfo.getFeatureIcon();
			return shortCutInfo;
		} else {
			return null;
		}
	}

	/**
	 * <br>功能简述:通过位置计算摆放位置
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param pos 图标所谓屏幕位置 1-16
	 * @return
	 */
	public static int[] getXY(int pos) {
		DesktopSettingInfo desktopSettingInfo = GOLauncherApp.getSettingControler().getDesktopSettingInfo();
		int rowSize = desktopSettingInfo.mRow;
		
		int cellX = 0;
		int cellY = 0;
		int lowSize = 4;
		
		int remainder = pos % lowSize;	//余数
		int divisor = pos / lowSize;	//除数
		
		if (remainder == 0) {
			cellX = 0;
			cellY = rowSize - divisor;
		} else {
			cellX = lowSize - remainder;
			cellY = rowSize - divisor - 1;
		}
		
		int[] xy = new int[2];
		xy[0] = cellX;
		xy[1] = cellY;

		return xy;
	}

	/**
	 * <br>功能简述:保存广告缓存
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param keyName
	 * @param id
	 * @param type
	 */
	public void saveAdvertStatOnClickCache(String keyName, String id, String mapId) {
		if (mPreferencesManager == null) {
			mPreferencesManager = new PreferencesManager(mContext,
					IPreferencesIds.ADVERT_SCREEN_DATA, Context.MODE_WORLD_READABLE);
		}
		StringBuffer buffer = new StringBuffer();

		//通过包命拿对应的数据
		String cacheDataString = mPreferencesManager.getString(keyName, "");

		String idString = id;
		int clickCount = 0;
		int installCount = 0;
		String mapIdString = mapId;
		if (mapIdString == null || mapIdString.equals("")) {
			mapIdString = "0";
		}

		//判断是否有缓存
		if (!cacheDataString.equals("")) {
			log("缓存内容：" + keyName + ":" + cacheDataString);

			String[] item = cacheDataString.split(";");

			if (item != null && item.length == 4) {
//				idString = item[0];	//消息ID
				clickCount = Integer.parseInt(item[1]);	//点击数量
				installCount = Integer.parseInt(item[2]);; //安装数量
				clickCount = clickCount + 1;
			}
		} else {
			clickCount = clickCount + 1;
		}

		buffer.append(idString).append(";"); 	 //消息id,直接每次用新的id。一个情况是不同屏幕推同一个应用，但id不一样
		buffer.append(clickCount).append(";");	 //点击数
		buffer.append(installCount).append(";"); //安装数
		buffer.append(mapIdString); 			 //统计ID

		log("保存内容：" + keyName + ":" + buffer.toString());
		mPreferencesManager.putString(keyName, buffer.toString());
		mPreferencesManager.commit();
	}

	/**
	 * <br>功能简述:保存广告缓存
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param keyName
	 * @param id
	 * @param type
	 */
	public void saveAdvertStatInstallCache(String keyName) {
		if (mPreferencesManager == null) {
			mPreferencesManager = new PreferencesManager(mContext,
					IPreferencesIds.ADVERT_SCREEN_DATA, Context.MODE_WORLD_READABLE);
		}
		StringBuffer buffer = new StringBuffer();

		//通过包命拿对应的数据
		String cacheDataString = mPreferencesManager.getString(keyName, "");

		//判断是否有缓存
		if (!cacheDataString.equals("")) {
			log("缓存内容：" + keyName + ":" + cacheDataString);

			String[] item = cacheDataString.split(";");

			if (item != null && item.length == 4) {
				String idString = item[0];	//消息ID
				int clickCount = Integer.parseInt(item[1]);	//点击数量
				int installCount = Integer.parseInt(item[2]);; //安装数量
				String mapIdString = item[3];	//统计id

				installCount = installCount + 1; //安装数量+1

				buffer.append(idString).append(";"); 	 //消息id
				buffer.append(clickCount).append(";");	 //点击数
				buffer.append(installCount).append(";"); //安装数
				buffer.append(mapIdString);  //统计ID

				log("保存内容：" + keyName + ":" + buffer.toString());
				mPreferencesManager.putString(keyName, buffer.toString());
				mPreferencesManager.commit();
			}
		} else {
			log("install缓存为空");
		}
	}

	/**
	 * <br>功能简述:统计点击数量
	 * <br>功能详细描述:存在包名就用包名作为缓存的key值，否则用id作为缓存的key
	 * <br>注意:
	 * @param packageName
	 */
	public void requestAdvertStatOnClick(String packageName, String idString, String clickUrl,
			String mapId) {
		if (packageName != null && !packageName.equals("")) {
			saveAdvertStatOnClickCache(packageName, idString, mapId);
		} else {
			if (idString != null && !idString.equals("")) {
				saveAdvertStatOnClickCache(idString, idString, mapId);
			}
		}
		requestAdvertStat(clickUrl);
	}

	/**
	 * <br>功能简述:统计安装数量
	 * <br>功能详细描述:因为安装数量只有存在包名的情况下才设置。所以用包名作为缓存的key值
	 * <br>注意:
	 * @param packageName
	 */
	public void requestAdvertStatInstall(String packageName) {
		if (packageName != null && !packageName.equals("")) {
			saveAdvertStatInstallCache(packageName);
			requestAdvertStat(null);
		}
		try {
			long time = System.currentTimeMillis();
			setOpenCache(packageName, String.valueOf(time));	//设置缓存设置该应用安装的时间
			showOpenDailog(packageName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * <br>功能简述:请求上传广告点击安装统计
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param clickUrl 对应的回调地址，只有点击时才需要上传,安装传NULL
	 */
	public void requestAdvertStat(String clickUrl) {
		AdvertUtils.requestAdvertStatistics(mContext, mHandler, clickUrl);
		//TODO：为了赶时间。应该不需要做缓存
		clearStatisticsData(); //点击上次后马上清空数据
	}

	/**
	 * <br>功能简述:清除统计数据
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void clearStatisticsData() {
		if (mPreferencesManager == null) {
			mPreferencesManager = new PreferencesManager(mContext,
					IPreferencesIds.ADVERT_SCREEN_DATA, Context.MODE_WORLD_READABLE);
		}

		Map<String, ?> data = mPreferencesManager.getAll();
		if (data != null) {
			Set<String> keys = data.keySet();
			for (String key : keys) {
				StringBuffer oneBuffer = new StringBuffer();

				Object obj = data.get(key);
				String reason = null;
				if (obj != null && obj instanceof String) {
					reason = (String) obj;
				}

				String[] item = null;
				if (reason != null && !reason.equals("")) {
					item = reason.split(";");
				}

				if (item != null && item.length == 4) {
					String idString = item[0];	//消息ID
					int clickCount = 0;	//点击数量
					int installCount = 0; //安装数量
					String mapIdString = item[3];

					oneBuffer.append(idString).append(";");	//消息id
					oneBuffer.append(clickCount).append(";"); 	//点击量
					oneBuffer.append(installCount).append(";"); //安装量
					oneBuffer.append(mapIdString); //安装量
					mPreferencesManager.putString(key, oneBuffer.toString());
					mPreferencesManager.commit();
					//					log("清空缓存：" + key + ":" + oneBuffer.toString());
				}
			}
		}
	}

	public void onDestory() {
		mPreferencesUtil = null;
		if (mAdvertInfosList != null) {
			mAdvertInfosList.clear();
			mAdvertInfosList = null;
		}

	}

	public void log(String content) {
//				Log.i("lch2", content);
	}

	public void log3(String content) {
//				Log.i("lch3", content);
	}

	/**
	 * <br>功能简述:安装完后打开对话框提示打开
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param packageName
	 */
	public void showOpenDailog(final String packageName) {
		Intent intent = new Intent(mContext, AdvertOpenTipDailog.class);
		intent.putExtra(AdvertConstants.ADVERT_PACK_NAME, packageName);
		mContext.startActivity(intent);
	}



	/**
	 * <br>功能简述:设置8小时请求对应的缓存信息
	 * <br>功能详细描述:没有打开过的设置当前时间值，否则设置ture表示已经打开过
	 * <br>注意:
	 * @param packageName
	 * @param content
	 */
	public void setOpenCache(String packageName, String content) {
		log3("设置缓存setOpenCache():" + packageName + ":" + content);

		if (mOpenPreferencesManager == null) {
			mOpenPreferencesManager = new PreferencesManager(mContext,
					IPreferencesIds.ADVERT_NEET_OPEN_DATA, Context.MODE_WORLD_READABLE);
		}
		mOpenPreferencesManager.putString(packageName, content);
		mOpenPreferencesManager.commit();
	}

	public void checkIsNotOpen() {
		if (mOpenPreferencesManager == null) {
			mOpenPreferencesManager = new PreferencesManager(mContext,
					IPreferencesIds.ADVERT_NEET_OPEN_DATA, Context.MODE_WORLD_READABLE);
		}
		Map<String, ?> allCache = mOpenPreferencesManager.getAll();

		if (allCache != null) {
			Set<String> keys = allCache.keySet();
			int i = 0;
			for (String key : keys) {
				i++;
				Object obj = allCache.get(key);
				String cache = null;
				if (obj != null && obj instanceof String) {
					cache = (String) obj;
				}
				log3(key + ":" + cache);
				//判断是否已经打开过
				if (cache != null && !cache.equals(AdvertConstants.ADVERT_IS_OPENED)) {
					try {
						long cacheTime = Long.parseLong(cache);
						long curTime = System.currentTimeMillis();
						long hour = curTime - cacheTime;

						log3("curTime - cacheTime:" + (curTime - cacheTime));

						//如果大于8个小时就进行同事栏提示
						if (hour >= AdvertConstants.ADVERT_TIPS_TIME) {
							log3("大于8小时");
							setOpenCache(key, String.valueOf(curTime));
							final AppItemInfo appItemInfo = AdvertConstants.getAppName(mContext, key);
							if (appItemInfo != null && appItemInfo.mTitle != null
									&& appItemInfo.mProcessName != null) {
								AdvertNotification.showNotification(mContext, appItemInfo.mTitle,
										appItemInfo.mProcessName, appItemInfo.mIcon, i);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void setAdvertAppIsOpen(Intent intent) {
		try {
			if (intent == null || intent.getComponent() == null
					|| intent.getComponent().getPackageName() == null) {
				return;
			}

			String packageName = intent.getComponent().getPackageName();
			log3("获取intent的包名packageName：" + packageName);

			if (mOpenPreferencesManager == null) {
				mOpenPreferencesManager = new PreferencesManager(mContext,
						IPreferencesIds.ADVERT_NEET_OPEN_DATA, Context.MODE_WORLD_READABLE);
			}
			Map<String, ?> allCache = mOpenPreferencesManager.getAll();

			if (allCache != null) {
				Set<String> keys = allCache.keySet();
				for (String key : keys) {
					if (key.equals(packageName)) {
						log3("设置已经打开过：" + packageName);
						mOpenPreferencesManager.putString(packageName,
								AdvertConstants.ADVERT_IS_OPENED);
						mOpenPreferencesManager.commit();
						return;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * <br>功能简述:24小时后检查是否可以重新请求数据
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void checkRequestAgain() {
		try {
			boolean isCanRequest = mPreferencesUtil.getBoolean(ADVERT_CAN_REQUEST, false);
			//判断是否已经请求过，如果还没请求就直接退出，因为会有监听网络重新重新请求
			if (isCanRequest) {
				log("第一次还没请求成功！等待第一次网络监听请求！");
				return;
			}
			
			//如果不能请求证明屏幕已经改变过
			boolean isCanRequestAgain = mPreferencesUtil.getBoolean(AdvertConstants.ADVERT_24_CAN_REQUEST, false); //设置24小时也不能再请求
			if (!isCanRequestAgain) {
				log("屏幕已经改变过，24不能再请求了！！！");
				return;
			}
				
			String requestTimeString = mPreferencesUtil.getString(AdvertConstants.ADVERT_REQUEST_TIME, "0");
				long requestTime = Long.parseLong(requestTimeString);
				long curTime = System.currentTimeMillis();	
				
				log("requestTime：" + requestTime);
				log("curTime：" + curTime);
				log("时间差：" + (curTime - requestTime));
				
				//如果大于24个小时就进行请求
//				if ((curTime - requestTime) >= 0) {
				if ((curTime - requestTime) >= AdvertConstants.ADVERT_24_TIME) {
					log("大于24小时");
					//判断是否有网络
					if (!Machine.isNetworkOK(mContext)) {
						log("24小时请求没有网络！");
						return;
					}
					
					//判断是否可以重新更新15图标
					if (isCanRefreshScreenIcon()) {
						log("24小时重新请求数据！");
						mIsRequsetAgain = true; //设置24小时请求标志
						AdvertUtils.getAdvertData(mContext, mHandler);	//重新请求
						saveRequestTime(); //设置请求时间
					} else {
						log("屏幕已修改，设置下次不能在24小时请求");
						setCanRequestAgainState(false); //屏幕已修改，设置下次不能在24小时请求
					}
				}
		} catch (Exception e) {
			return;
		}
	}

	
	
	
	/**
	 * <br>功能简述:判断桌面是否有改变，是否可以重新请求
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public boolean isCanRefreshScreenIcon() {
		try {
			ArrayList<AdvertInfo> advertInfoList = new ArrayList<AdvertInfo>();
			
			String cacheString = mPreferencesUtil.getString(AdvertConstants.ADVERT_ADD_SCREEN_CACHE, "");
			//如果缓存为空，证明第一次请求是失败的。没有缓存
			if (!cacheString.equals("")) {
				JSONArray msgsArray = null;
				msgsArray = new JSONArray(cacheString);
				advertInfoList = AdvertJsonUntil.getAdvrtArrary(mContext, msgsArray, true);
			}
			
			boolean isCanAddIconToScreen = GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
					IDiyMsgIds.SCREEN_CAN_CHANGE_ADVERT_SHORT_CUT, -1, advertInfoList, null);
		
			if (isCanAddIconToScreen) {
				log("可以插入，屏幕没改变");
				return true;
			} else {
				log("不可以插入，屏幕已改变");
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * <br>功能简述:设置非文件夹图标的json缓存
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param advertInfo
	 */
	public void setAppIconCacheJson(AdvertInfo advertInfo) {
		JSONObject value = AdvertJsonUntil.setAdvertAppJson(advertInfo);
		mAddScreenJsonArray.put(value);
	}
	
	
	/**
	 * <br>功能简述:设置文件夹图标的json缓存
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param advertInfo
	 */
	public void setFolderIconCacheJson(AdvertInfo advertInfo, ArrayList<AdvertInfo> advertInfosList) {
		JSONObject value = AdvertJsonUntil.setAdvertFolderJson(advertInfo, advertInfosList);
		mAddScreenJsonArray.put(value);
	}
	
	/**
	 * <br>功能简述:设置插入屏幕的图标缓存信息
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void saveAddScreenCache() {
		if (mAddScreenJsonArray != null) {
//			log("插入到数据库缓存:" + mAddScreenJsonArray.toString());
			mPreferencesUtil.saveString(AdvertConstants.ADVERT_ADD_SCREEN_CACHE, mAddScreenJsonArray.toString());
		}
	}
	
	/**
	 * <br>功能简述:设置请求时间
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void saveRequestTime() {
		long curTime = System.currentTimeMillis();
		mPreferencesUtil.saveString(AdvertConstants.ADVERT_REQUEST_TIME, String.valueOf(curTime));
	}
	
	/**
	 * <br>功能简述:设置不可以24小时后再次请求广告数据
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void setCanRequestAgainState(boolean flag) {
		if (mPreferencesUtil != null) {
			log("设置24小时参数：" + flag);
			mPreferencesUtil.saveBoolean(AdvertConstants.ADVERT_24_CAN_REQUEST, flag); //设置24小时也不能再请求
		}
	}
	
	/**
	 * <br>功能简述:清空15屏的图标
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public boolean isClecrAdvertIcon() {
		try {
			//判断是否可以重新更新15图标
			if (isCanRefreshScreenIcon()) {
				log("请求删除15屏/首屏图标！");
				
				ArrayList<AdvertInfo> advertInfoList = new ArrayList<AdvertInfo>();
				
				String cacheString = mPreferencesUtil.getString(AdvertConstants.ADVERT_ADD_SCREEN_CACHE, "");
				//如果缓存为空，证明第一次请求是失败的。没有缓存
				if (!cacheString.equals("")) {
					JSONArray msgsArray = null;
					msgsArray = new JSONArray(cacheString);
					advertInfoList = AdvertJsonUntil.getAdvrtArrary(mContext, msgsArray, true);
				}
				
				return GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
						IDiyMsgIds.SCREEN_CLEAR_ADVERT_ICON, -1, advertInfoList, null);
			} else {
				log("请求清空屏幕。但屏幕已修改，设置下次不能在24小时请求");
				setCanRequestAgainState(false); //屏幕已修改，设置下次不能在24小时请求
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	/**
	 * <br>功能简述:检测是否需要抖动的图标（推荐图标才抖动）
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param intent
	 * @return
	 */
	public boolean checkNeedShake(Intent intent) {
//		log3("checkNeedShake()");
		if (intent == null || intent.getComponent() == null
				|| intent.getComponent().getPackageName() == null) {
			return false;
		}
		//获取包名
		String packageName = intent.getComponent().getPackageName();
//		log3("获取intent的包名packageName：" + packageName);

		if (mOpenPreferencesManager == null) {
			mOpenPreferencesManager = new PreferencesManager(mContext,
					IPreferencesIds.ADVERT_NEET_OPEN_DATA, Context.MODE_WORLD_READABLE);
		}
		Map<String, ?> allCache = mOpenPreferencesManager.getAll();

		if (allCache != null) {
			Set<String> keys = allCache.keySet();
			for (String key : keys) {
				Object obj = allCache.get(key);
				String cache = null;
				if (obj != null && obj instanceof String) {
					cache = (String) obj;
				}
				log3(key + ":" + cache);
				// 判断是否已经打开过
				if (key.equals(packageName)) {
					if (cache != null && !cache.equals(AdvertConstants.ADVERT_IS_OPENED)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * <br>功能简述:设置广告图标抖动动画
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param mMoveToScreenView
	 */
	public void setShakeAnim(final View mMoveToScreenView) {
		try {
			if (mShakeAnimatorMaps == null) {
				mShakeAnimatorMaps = new HashMap<View, ValueAnimator>();
			}

			CompatibleUtil.unwrap(mMoveToScreenView);
			ValueAnimator mShakeAnimator = ValueAnimator.ofFloat(0f, 1f);
			mShakeAnimator.setRepeatMode(ValueAnimator.REVERSE);
			mShakeAnimator.setRepeatCount(ValueAnimator.INFINITE);
			mShakeAnimator.setDuration(DockUtil.MOVE_TO_SCREEN_SHAKE_DURATION);
			mShakeAnimator.setStartDelay(DockUtil.MOVE_TO_SCREEN_TRANSACTION_DURATION);

			mShakeAnimator.addUpdateListener(new AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					float r = ((Float) animation.getAnimatedValue()).floatValue();
					float y = r * (-10); // 位移-10
					CompatibleUtil.setTranslationY(mMoveToScreenView, y);
					float s = r * 1.05f + (1 - r); // 从1.0到1.05变化
					CompatibleUtil.setScaleX(mMoveToScreenView, s);
					CompatibleUtil.setScaleY(mMoveToScreenView, s);
				}
			});

			mShakeAnimator.start();
			mShakeAnimatorMaps.put(mMoveToScreenView, mShakeAnimator);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * <br>功能简述:2秒后清除抖动动画
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void cancleShakeAnimDelayed() {
		Message msg = new Message();
		msg.what = AdvertConstants.MESSAGE_CLERAR_ANIMATION;
		mShakeHandler.removeMessages(msg.what);
		mShakeHandler.sendMessageDelayed(msg, 2000);
	}

	Handler mShakeHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			//清除抖动图标信息
				case AdvertConstants.MESSAGE_CLERAR_ANIMATION :
					cancleShakeAnimNow();
					break;

				default :
					break;
			}
		}
	};

	/**
	 * <br>功能简述:马上清空抖动动画队列
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void cancleShakeAnimNow() {
		try {
			if (mShakeAnimatorMaps != null) {
				for (View key : mShakeAnimatorMaps.keySet()) {
					ValueAnimator shakeAnimator = mShakeAnimatorMaps.get(key);
					if (shakeAnimator != null) {
						shakeAnimator.cancel();
						CompatibleUtil.unwrap(key);
						shakeAnimator = null;
					}
				}
				mShakeAnimatorMaps.clear();
				mShakeAnimatorMaps = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * <br>功能简述:设置首屏幕首次默认图标的缓存
	 * <br>功能详细描述:主要用户判断首屏图标是否改变过
	 * <br>注意:
	 */
	public void setFistHomeScreenCache() {
		String cacheString = AdvertHomeScreenUtils.getHomeScreenCache(mContext);
		//为空代表第一次设置缓存
		if (TextUtils.isEmpty(cacheString)) {
			log3("第一次设置首屏缓存！");
			setHomeScreenCache();
		}
	}
	
	/**
	 * <br>功能简述:设置初始化首页的缓存
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	
	public void setHomeScreenCache() {
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
				IDiyMsgIds.SET_HOME_SCREEN_ICON_CACHE, -1, null, null);
	}
	
	/**
	 * <br>功能简述:判断是否可以插入到首屏图标
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public boolean isCanAddIconToHomeScreen() {
		boolean isCanAddIconToHomeScreen = GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
				IDiyMsgIds.SCREEN_CAN_ADD_ADVERT_TO_HOME_SCREEN, -1, null, null);
		if (isCanAddIconToHomeScreen) {
			log3("首屏没有改变，可以插入！");
		} else {
			log3("首屏已改变，不能插入！");
		}
		return isCanAddIconToHomeScreen;
	}
	
	/**
	 * <br>功能简述：获取需要添加的轮换图标
	 * <br>功能详细描述:遍历轮换图标文件夹，判断哪个图标是没有安装的
	 * <br>注意:
	 * @param fileAdvertInfoList
	 * @return
	 */
	public AdvertInfo getCarouselIcon(AdvertInfo folderAdvertInfo) {
		try {
			ArrayList<AdvertInfo> fileAdvertInfoList = folderAdvertInfo.mFilemsg;
			if (fileAdvertInfoList == null || fileAdvertInfoList.size() == 0) {
				return null;
			}
			
			Object[] list = fileAdvertInfoList.toArray();
			Arrays.sort(list); //对列表进行轮播优先级排序
			int size = list.length;
			for (int i = 0; i < size; i++) {
				AdvertInfo advertInfo = (AdvertInfo) list[i];
				String pathString = advertInfo.mIcon;
				if (!TextUtils.isEmpty(pathString) && !pathString.startsWith("http://")) {
					String packageName = advertInfo.mPackageName;
					if (!TextUtils.isEmpty(packageName)) {
						boolean isExist = AppUtils.isAppExist(mContext, packageName);
						if (!isExist) {
							log("轮播图标不存在,可以插入：" + packageName);
							//设置插入的轮换图标位置和屏幕位置为文件夹的位置
							advertInfo.mScreen = folderAdvertInfo.mScreen;
							advertInfo.mPos = folderAdvertInfo.mPos;
							return advertInfo;
						}
					}
				}
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
