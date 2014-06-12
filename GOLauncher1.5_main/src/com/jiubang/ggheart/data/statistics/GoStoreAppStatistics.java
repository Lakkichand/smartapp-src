package com.jiubang.ggheart.data.statistics;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.go.util.AppUtils;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStorePhoneStateUtil;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreStatisticsUtil;
import com.jiubang.ggheart.data.statistics.tables.GoStoreAppTable;

/**
 * 精品应用统计类，专用于统计精品专务与应用有关（展业，点击，安装等）的统计数据
 * 
 * @author zhouxuewen
 * 
 */
public class GoStoreAppStatistics implements IAppInstallStatistics, IMonitorAppInstallListener {

	private static final String PROTOCOL_TITLE = "3||1||"; // 业务ID
	private static final String PROTOCOL_DIVIDER = "||"; // 业务分隔符
	private final static String ENTER_SPERATE = "#"; // 数组分隔符
	private final static String CLASS_SPERATE = "&&"; // 分类分隔符
	private static GoStoreAppStatistics sInstance = null;
	private StatisticsDataProvider mDataProvider; // 统计数据库类实例
	private MonitorAppstatisManager mAppsManager; // 安装监听类实例

	private static final int TYPE_SHOW = 0; // 普通展示
	private static final int TYPE_DETAIL_SHOW = 1; // 详情展示
	private static final int TYPE_CLICK = 2; // 安装点击
	private static final int TYPE_UPDATE_CLICK = 3; // 更新点击

	private Context mContext = null;

	private GoStoreAppStatistics(Context context) {
		mContext = context.getApplicationContext();
		mDataProvider = StatisticsDataProvider.getInstance(context);
		boolean isTabExist = mDataProvider.isExistTable(GoStoreAppTable.TABLENAME);
		if (!isTabExist) {
			mDataProvider.createTab(GoStoreAppTable.TABLENAME);
		}
		mAppsManager = MonitorAppstatisManager.getInstance(context);
	}

	public synchronized static GoStoreAppStatistics getInstance(Context context) {
		if (sInstance == null) {
			try {
				sInstance = new GoStoreAppStatistics(context);
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		return sInstance;
	}

	@Override
	public void onHandleAppInstalled(String pkgName, String listenKey) {
		// TODO Auto-generated method stub
		if (listenKey == null) {
			return;
		}
		// 把listenKey还原为寄存的数据
		String[] keys = listenKey.split(ENTER_SPERATE);
		String appid = null;
		String entry = null;
		String isUpdate = null;
		String classify = null;
		if (keys != null && keys.length >= 3) {
			appid = keys[0];
			entry = keys[1];
			isUpdate = keys[2];
			classify = keys[3];
		}

		if (isUpdate == null || isUpdate.equals("")) {
			isUpdate = "0";
		}
		// 更新安装量
		updateInstallCount(appid, entry, isUpdate.equals("1"), classify);
	}

	/**
	 * 手动调用安装成功
	 * @param pkgName	包名
	 */
	public void onAppInstalled(String pkgName) {
		try {
			if (mAppsManager != null) {
				mAppsManager.handleAppInstalled(pkgName);
			}
			// System.out.println("插入数据库");
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	/**
	 * 
	 * @param context
	 * @param pkgName
	 * @param appId
	 * @param appName
	 * @param postion
	 * @param entry
	 * @param type
	 * @return 是否已经记录了数据（创表会顺带记录数据），如已经记录，可不再向下处理
	 */
	private boolean checkData(Context context, String pkgName, String appId, String appName,
			int postion, String entry, int type, String classify, String clcikTime) {
		if (isDataExist(appId, entry, classify)) {
			return false;
		}
		createData(pkgName, appId, appName, postion, entry, type, classify, clcikTime);
		return true;
	}

	@Override
	public boolean isDataExist(String appid, String entry, String classify) {
		// TODO Auto-generated method stub
		boolean isExist = false;
		try {
			String selection = getSelection(appid, entry, classify);
			isExist = mDataProvider.isExistData(GoStoreAppTable.TABLENAME, selection, null);
		} catch (Exception e) {
			// TODO: handle exception
		}

		return isExist;
	}

	/**
	 * 获取标准Selection的方法，现时所在的表通过appid及entry就可以定位到目标行，如果以后统计维数增加，
	 * 只要再修改Selection条件就可
	 * 
	 * @param appid
	 * @param entry
	 * @return 标准Selection
	 */
	public String getSelection(String appid, String entry, String classify) {
		return GoStoreAppTable.APP_ID + " = '" + appid + "'" + " and " + GoStoreAppTable.ENTRY
				+ " = '" + entry + "'" + " and " + GoStoreAppTable.CLASSIFY + " = '" + classify
				+ "'";
	}

	@Override
	public void createData(String pkgName, String appId, String appName, int postion, String entry,
			int type, String classify, String clickTime) {
		// TODO Auto-generated method stub
		int showCount = 0;
		int detailShow = 0;
		int clickCount = 0;
		int updateClick = 0;

		switch (type) {
			case TYPE_SHOW :
				showCount = 1;
				break;
			case TYPE_DETAIL_SHOW :
				detailShow = 1;
				break;
			case TYPE_CLICK :
				clickCount = 1;
				break;
			case TYPE_UPDATE_CLICK :
				updateClick = 1;
				break;
			default :
				break;
		}

		ContentValues values = new ContentValues();
		values.put(GoStoreAppTable.PKG_NAME, pkgName);
		values.put(GoStoreAppTable.APP_ID, appId);
		values.put(GoStoreAppTable.APP_NAME, appName);
		values.put(GoStoreAppTable.POSTION, postion);
		values.put(GoStoreAppTable.SHOW_COUNT, showCount);
		values.put(GoStoreAppTable.CLICK_COUNT, clickCount);
		values.put(GoStoreAppTable.DETAIL_SHOW, detailShow);
		values.put(GoStoreAppTable.UPDATE_CLICK, updateClick);
		values.put(GoStoreAppTable.INSTALL_COUNT, 0);
		values.put(GoStoreAppTable.UPDATE_COUNT, 0);
		values.put(GoStoreAppTable.ENTRY, entry);
		values.put(GoStoreAppTable.CLASSIFY, classify);
		values.put(GoStoreAppTable.CLICK_TIME, clickTime);
		mDataProvider.insertData(GoStoreAppTable.TABLENAME, values);

		if (type == TYPE_CLICK || type == TYPE_UPDATE_CLICK) {
			sendMonitorAppInstallMessage(pkgName, appId, entry, String.valueOf(updateClick),
					classify);
		}
	}

	/**
	 * 发送预安装信息，等待安装成功的回调
	 * 
	 * @param pkgName
	 * @param appid
	 * @param entry
	 * @param clickType
	 */
	public void sendMonitorAppInstallMessage(String pkgName, String appid, String entry,
			String clickType, String classify) {
		// 需要寄存的统计安装数据，安装完成后此数据会返回，能通过些数据进行处理
		String listenrKey = appid + ENTER_SPERATE + entry + ENTER_SPERATE + clickType
				+ ENTER_SPERATE + classify;
		mAppsManager.handleMonitorAppInstall(pkgName, MonitorAppstatisManager.TYPE_FROM_GOSOTORE,
				listenrKey);
	}

	public void sendMonitorAppInstallMessage(String pkgName, String appid, String classify) {
		String newClassify = classify + ENTER_SPERATE + "0";
		String entry = String.valueOf(GoStoreStatisticsUtil.getCurrentEntry(mContext));
		sendMonitorAppInstallMessage(pkgName, appid, entry, "0", newClassify);
	}

	@Override
	public void updateInstallClick(String packageName, String appid, String entry,
			boolean isUpdate, String classify, String clickTime) {
		// TODO Auto-generated method stub
		// 先更新数据表的点击数

		String clickType;
		String updateKey;

		if (isUpdate) {
			clickType = GoStoreAppTable.UPDATE_CLICK;
			updateKey = "1";
		} else {
			clickType = GoStoreAppTable.CLICK_COUNT;
			updateKey = "0";
		}

		String sql = "update " + GoStoreAppTable.TABLENAME + " set " + clickType + " = "
				+ clickType + " + 1 " + "where " + getSelection(appid, entry, classify);

		mDataProvider.exeSql(sql);

		if (!clickTime.equals("0")) {
			updateClickTime(appid, entry, classify, clickTime);
		}
		sendMonitorAppInstallMessage(packageName, appid, entry, updateKey, classify);
	}

	@Override
	public void updateInstallCount(String appid, String entry, boolean isUpdate, String classify) {
		// TODO Auto-generated method stub
		String installType;

		if (isUpdate) {
			installType = GoStoreAppTable.UPDATE_COUNT;
		} else {
			installType = GoStoreAppTable.INSTALL_COUNT;
		}

		String sql = "update " + GoStoreAppTable.TABLENAME + " set " + installType + " = "
				+ installType + " + 1 " + "where " + getSelection(appid, entry, classify);

		mDataProvider.exeSql(sql);
	}

	@Override
	public void updateAppListShow(String appid, String entry, String classify) {
		// TODO Auto-generated method stub
		String sql = "update " + GoStoreAppTable.TABLENAME + " set " + GoStoreAppTable.SHOW_COUNT
				+ " = " + GoStoreAppTable.SHOW_COUNT + " + 1 " + "where "
				+ getSelection(appid, entry, classify);

		mDataProvider.exeSql(sql);
	}

	@Override
	public void updateAppDetailShow(String appid, String entry, String classify) {
		// TODO Auto-generated method stub

		String sql = "update " + GoStoreAppTable.TABLENAME + " set " + GoStoreAppTable.DETAIL_SHOW
				+ " = " + GoStoreAppTable.DETAIL_SHOW + " + 1 " + "where "
				+ getSelection(appid, entry, classify);

		mDataProvider.exeSql(sql);
	}

	public void updateClickTime(String appid, String entry, String classify, String clickTime) {
		try {
			String selection = GoStoreAppTable.APP_ID + " = '" + appid + "'" + " and "
					+ GoStoreAppTable.ENTRY + " = '" + entry + "'" + " and "
					+ GoStoreAppTable.CLASSIFY + " = '" + classify + "'" + " and "
					+ GoStoreAppTable.INSTALL_COUNT + " = 0";

			String sql = "update " + GoStoreAppTable.TABLENAME + " set "
					+ GoStoreAppTable.CLICK_TIME + " = '" + clickTime + "'" + "where " + selection;

			mDataProvider.exeSql(sql);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Override
	public String queryAllData() {
		// TODO Auto-generated method stub
		StringBuffer allBuf = new StringBuffer();
		StringBuffer singleBuf;
		Cursor cursor = null;
		String title = PROTOCOL_TITLE + GoStorePhoneStateUtil.getUid(mContext) + PROTOCOL_DIVIDER
				+ StatisticsFuncId.STATICTISC_LEVEL3_FUNID_GOSTORE_CONTENT + PROTOCOL_DIVIDER;

		// 获取精品Widget数据
		// singleBuf = new StringBuffer();
		// singleBuf.append(GoStoreStatisticsUtil.getWidgetRecord(mContext));
		// allBuf.append(singleBuf);	

		try {
			cursor = mDataProvider.queryData(GoStoreAppTable.TABLENAME, null, null, null, null,
					null, null);
			if (cursor != null && cursor.getCount() > 0) {
				int pkgNameIndex = cursor.getColumnIndex(GoStoreAppTable.PKG_NAME);
				int appIdIndex = cursor.getColumnIndex(GoStoreAppTable.APP_ID);
				int appNameIndex = cursor.getColumnIndex(GoStoreAppTable.APP_NAME);
				int postionIndex = cursor.getColumnIndex(GoStoreAppTable.POSTION);
				int showCountIndex = cursor.getColumnIndex(GoStoreAppTable.SHOW_COUNT);
				int clickCountIndex = cursor.getColumnIndex(GoStoreAppTable.CLICK_COUNT);
				int detailShowIndex = cursor.getColumnIndex(GoStoreAppTable.DETAIL_SHOW);
				int updateClickIndex = cursor.getColumnIndex(GoStoreAppTable.UPDATE_CLICK);
				int installCountIndex = cursor.getColumnIndex(GoStoreAppTable.INSTALL_COUNT);
				int updateCountIndex = cursor.getColumnIndex(GoStoreAppTable.UPDATE_COUNT);
				int entryIndex = cursor.getColumnIndex(GoStoreAppTable.ENTRY);
				int classifyIndex = cursor.getColumnIndex(GoStoreAppTable.CLASSIFY);
				int clickTimeIndex = cursor.getColumnIndex(GoStoreAppTable.CLICK_TIME);
				while (cursor.moveToNext()) {
					int clickCount = 0;
					int installCount = 0;
					String pkgName = cursor.getString(pkgNameIndex);
					singleBuf = new StringBuffer();
					singleBuf.append(title);
					singleBuf.append(cursor.getString(appIdIndex) + PROTOCOL_DIVIDER);
					singleBuf.append(cursor.getString(appNameIndex) + PROTOCOL_DIVIDER);
					singleBuf.append(cursor.getInt(postionIndex) + PROTOCOL_DIVIDER);
					singleBuf.append(cursor.getInt(showCountIndex) + PROTOCOL_DIVIDER);
					clickCount = cursor.getInt(clickCountIndex);
					singleBuf.append(clickCount + PROTOCOL_DIVIDER);
					singleBuf.append(cursor.getInt(detailShowIndex) + PROTOCOL_DIVIDER);
					singleBuf.append(cursor.getInt(updateClickIndex) + PROTOCOL_DIVIDER);
					installCount = cursor.getInt(installCountIndex);
					if (clickCount > 0 && installCount <= 0) {
						try {
							if (AppUtils.isAppExist(mContext, pkgName)) {
								installCount = 1;
							}
						} catch (Exception e) {
							// TODO: handle exception
						}
					}
					singleBuf.append(installCount + PROTOCOL_DIVIDER);
					singleBuf.append(cursor.getInt(updateCountIndex) + PROTOCOL_DIVIDER);
					singleBuf.append(cursor.getString(entryIndex) + PROTOCOL_DIVIDER);
					String classifyInfos = cursor.getString(classifyIndex);
					String classify = null;
					String pkgType = "0";
					if (classifyInfos != null && !classifyInfos.equals("")) {
						String[] classifyItems = classifyInfos.split(CLASS_SPERATE);
						if (classifyItems != null && classifyItems.length > 0) {
							classify = classifyItems[0];
							if (classifyItems.length > 1) {
								pkgType = classifyItems[1];
							}
						}
					}
					singleBuf.append(classify + PROTOCOL_DIVIDER);
					singleBuf.append(pkgType + PROTOCOL_DIVIDER);
					singleBuf.append(cursor.getString(clickTimeIndex));

					allBuf.append(singleBuf);
					allBuf.append("\r\n");
				}
			}
		} catch (Exception e) {

		} finally {
			if (cursor != null) {
				try {
					cursor.close();
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		}
		return allBuf.toString();
	}

	@Override
	public void clearAllData() {
		// TODO Auto-generated method stub
		mDataProvider.delete(GoStoreAppTable.TABLENAME, null, null);
	}

	@Override
	public void saveDataWhenShow(String pkgName, String appId, String appName, int postion,
			String entry, String classify) {
		// TODO Auto-generated method stub
		if (entry == null || entry.equals("")) {
			entry = String.valueOf(GoStoreStatisticsUtil.getCurrentEntry(mContext));
		}
		boolean isDone = checkData(mContext, pkgName, appId, appName, postion, entry, TYPE_SHOW,
				classify, "0");
		if (isDone) {
			return;
		}
		updateAppListShow(appId, entry, classify);
	}

	@Override
	public void saveDataWhenDetailShow(String pkgName, String appId, String appName, String entry,
			String classify) {
		// TODO Auto-generated method stub
		if (entry == null || entry.equals("")) {
			entry = String.valueOf(GoStoreStatisticsUtil.getCurrentEntry(mContext));
		}
		boolean isDone = checkData(mContext, pkgName, appId, appName, 0, entry, TYPE_DETAIL_SHOW,
				classify, "0");
		if (isDone) {
			return;
		}
		updateAppDetailShow(appId, entry, classify);
	}

	@Override
	public void saveDateWhenTouch(String pkgName, String appId, String appName, int postion,
			boolean isUpdate, String entry, String classify, String clickTime) {
		// TODO Auto-generated method stub
		if (entry == null || entry.equals("")) {
			entry = String.valueOf(GoStoreStatisticsUtil.getCurrentEntry(mContext));
		}
		boolean isDone = checkData(mContext, pkgName, appId, appName, postion, entry, isUpdate
				? TYPE_UPDATE_CLICK
				: TYPE_CLICK, classify, clickTime);
		if (isDone) {
			return;
		}
		updateInstallClick(pkgName, appId, entry, isUpdate, classify, clickTime);
	}

	public void saveDataWhenShow(String pkgName, String appId, String appName, int postion,
			String entry, String classify, int pkgType) {
		String newClassify = classify + CLASS_SPERATE + pkgType;
		saveDataWhenShow(pkgName, appId, appName, postion, entry, newClassify);
	}

	public void saveDataWhenDetailShow(String pkgName, String appId, String appName, String entry,
			String classify, int pkgType) {
		String newClassify = classify + CLASS_SPERATE + pkgType;
		saveDataWhenDetailShow(pkgName, appId, appName, entry, newClassify);
	}

	public void saveDateWhenTouch(String pkgName, String appId, String appName, int postion,
			boolean isUpdate, String entry, String classify, int pkgType) {
		String newClassify = classify + CLASS_SPERATE + pkgType;
		saveDateWhenTouch(pkgName, appId, appName, postion, isUpdate, entry, newClassify);
	}

	public void saveDateWhenTouch(String pkgName, String appId, String appName, int postion,
			boolean isUpdate, String entry, String classify, int pkgType, String clickTime) {
		String newClassify = classify + CLASS_SPERATE + pkgType;
		saveDateWhenTouch(pkgName, appId, appName, postion, isUpdate, entry, newClassify, clickTime);
	}

	public void saveDateWhenTouch(String pkgName, String appId, String appName, int postion,
			boolean isUpdate, String entry, String classify) {
		// TODO Auto-generated method stub
		saveDateWhenTouch(pkgName, appId, appName, postion, isUpdate, entry, classify, "0");
	}

}
