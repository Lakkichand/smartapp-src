package com.jiubang.ggheart.data.statistics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;

import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.data.statistics.tables.GUIThemeTable;
import com.jiubang.ggheart.data.theme.GoLockerThemeManager;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.data.theme.bean.ThemeInfoBean;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * GUI收费数据统计模块
 * 
 * @author yangbing
 * 
 */
public class GuiThemeStatistics implements IMonitorAppInstallListener {

	/**
	 * 主题预览界面入口
	 * */
	public static final int THEME_PREVIEW_ENTRY = 1;
	/**
	 * GO精品入口
	 * */
	public static final int GO_STORE_ENTRY = 2;
	/**
	 * 桌面主题类型
	 * */
	public static final int THEME_LAUNCHER_TYPE = 1;
	/**
	 * 锁屏主题类型
	 * */
	public static final int THEME_LOCKER_TYPE = 2;

	private static final String PROTOCOL_TITLE = "15||1||";
	private static final String PROTOCOL_TITLE2 = "15||2||";
	private static final String PROTOCOL_DIVIDER = "||";
	private static GuiThemeStatistics sInstance = null;
	private StatisticsDataProvider mDataProvider;
	private MonitorAppstatisManager mAppsManager;
	private ThemeManager mThemeManager;
	private Context mContext;

	public final static byte ENTRY_TYPE_OTHER = 0; // 其它入口
	public final static byte ENTRY_GO_STORE = 1; // GO精品
	public final static byte ENTRY_GO_LOCKER = 2; // GO锁屏
	public final static byte ENTRY_MENU = 3; // 菜单（无NEW)
	public final static byte ENTRY_MESSAGE_CENTER = 4; // 消息中心
	public final static byte ENTRY_MESSAGE_PUSH = 5; // 消息推送
	public final static byte ENTRY_MENU_NEW = 6; // 菜单（有NEW)
	public final static byte ENTRY_DESK_ICON = 7; // 桌面图标
	public final static String ENTRY_SP_NAME = "gui_current_entry";

	private final static String ENTER_SPERATE = "#"; // 数组分隔符
	private final static String CLASS_SPERATE = "&&"; // 分类分隔符

	private GuiThemeStatistics(Context context) {
		mContext = context;
		mDataProvider = StatisticsDataProvider.getInstance(context);
		mAppsManager = MonitorAppstatisManager.getInstance(context);
		mThemeManager = ThemeManager.getInstance(context);
	}

	public synchronized static GuiThemeStatistics getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new GuiThemeStatistics(context);
		}
		return sInstance;
	}

	/**
	 * 设置当前Gui统计入口
	 * 
	 * @param entryType
	 */
	public synchronized static void setCurrentEntry(byte entryType, Context context) {
		if (context == null) {
			return;
		}
		PreferencesManager entrySp = new PreferencesManager(context, ENTRY_SP_NAME,
				Context.MODE_PRIVATE);
		if (entrySp != null) {
			Editor edit = entrySp.edit();
			if (edit != null) {
				edit.putInt(ENTRY_SP_NAME, entryType); // 标记当前入口
				edit.commit();
			}
		}
	}

	/**
	 * 读取当前入口
	 */
	public static int getCurrentEntry(Context context) {
		if (context == null) {
			return ENTRY_TYPE_OTHER;
		}
		PreferencesManager entrySp = new PreferencesManager(context, ENTRY_SP_NAME,
				Context.MODE_PRIVATE);
		if (entrySp != null) {
			int entryType = entrySp.getInt(ENTRY_SP_NAME, ENTRY_TYPE_OTHER);
			Editor edit = entrySp.edit();
			if (edit != null) {
				edit.putInt("" + entryType, 1).commit(); // 标记该渠道号为活跃入口
			}
			return entryType;
		}
		return ENTRY_TYPE_OTHER;
	}

	@Override
	public void onHandleAppInstalled(String pkgName, String listenKey) {
		// System.out.println("处理安装事件回调，更新安装数");
		// System.out.println(pkgName+"::::"+listenKey);
		// 处理安装事件回调，更新安装数
		if (pkgName == null || listenKey == null) {
			return;
		}
		// 把listenKey还原为寄存的数据
		String[] keys = listenKey.split(ENTER_SPERATE);
		int position = 0;
		String pkgType = null;
		int entry = 0;
		if (keys != null && keys.length > 0) {
			position = Integer.parseInt(keys[0]);
			if (keys.length > 1) {
				pkgType = keys[1];
			}
			if (keys.length > 2) {
				entry = Integer.parseInt(keys[2]);
			}
		}
		if (isPackageInstallCountStatisticsed(pkgName, pkgType)) {
			return;
		}
		try {
			String sql = "update " + GUIThemeTable.TABLENAME + " set "
					+ GUIThemeTable.INSTALL_COUNT + " = 1 " + "where " + GUIThemeTable.PACKAGE
					+ " = '" + pkgName + "'" + " and " + GUIThemeTable.POSITION + " = " + position
					+ " and " + GUIThemeTable.ENTRY + " = " + entry;
			// System.out.println(sql);
			mDataProvider.exeSql(sql);
			// System.out.println("插入数据库");
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	/**
	 * 手动调用安装成功
	 * 
	 * @param pkgName
	 *            包名
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
	 * 判断此包名对应的主题安装数是否已经统计过
	 * 
	 * @param pkgName
	 * */
	private boolean isPackageInstallCountStatisticsed(String pkgName, String pkgType) {
		// 判断该包是否已经安装过
		Cursor cursor = null;
		try {
			String selection = "";
			if (pkgType == null) {
				selection = GUIThemeTable.PACKAGE + " = '" + pkgName + "'";
			} else {
				selection = GUIThemeTable.PACKAGE + " = '" + pkgName + "'" + " and "
						+ GUIThemeTable.PKG_TYPE + " = '" + pkgType + "'";
			}
			cursor = mDataProvider
					.queryData(GUIThemeTable.TABLENAME,
							new String[] { GUIThemeTable.INSTALL_COUNT }, selection, null, null,
							null, null);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					int installCount = cursor.getInt(0);
					if (installCount == 1) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}

		return false;
	}

	private String getSelection(String pkgName, int position, String pkgType, int entry) {
		String selection = "";

		selection = GUIThemeTable.PACKAGE + " = '" + pkgName + "'" + " and "
				+ GUIThemeTable.POSITION + " = " + position + " and " + GUIThemeTable.PKG_TYPE
				+ " = '" + pkgType + "'" + " and " + GUIThemeTable.ENTRY + " = " + entry;

		return selection;
	}

	/**
	 * 统计用户点击
	 * 
	 * @param context
	 * @param packageName
	 *            :主题包名
	 * @param position
	 *            :位置
	 * @param entry
	 *            :入口
	 * @param themeType
	 *            :主题分类
	 * */
	public void saveUserTouch(Context context, String packageName, int position, int themeType,
			String pkgType, String classId) {
		// 根据包名和位置判断数据库里是否已经存在该条记录
		int entry = getCurrentEntry(context);
		String pkgTypeAndClass = pkgType + CLASS_SPERATE + classId;
		String selection = getSelection(packageName, position, pkgTypeAndClass, entry);
		boolean isExist = mDataProvider.isExistData(GUIThemeTable.TABLENAME, selection,
				new String[] { GUIThemeTable.THEME_TYPE });
		if (isExist) {
			String sql = "update " + GUIThemeTable.TABLENAME + " set " + GUIThemeTable.CLICK_COUNT
					+ " = " + GUIThemeTable.CLICK_COUNT + " + 1 " + "where " + selection;

			mDataProvider.exeSql(sql);
		} else {

			// 不存在，插入一条新数据
			ContentValues values = new ContentValues();
			values.put(GUIThemeTable.PACKAGE, packageName);
			values.put(GUIThemeTable.POSITION, position);
			values.put(GUIThemeTable.ENTRY, entry);
			values.put(GUIThemeTable.THEME_TYPE, themeType);
			values.put(GUIThemeTable.CLICK_COUNT, 1);
			values.put(GUIThemeTable.INSTALL_COUNT, 0);
			values.put(GUIThemeTable.PKG_TYPE, pkgTypeAndClass);
			mDataProvider.insertData(GUIThemeTable.TABLENAME, values);
		}
		// 需要统计安装数的处理
		String listenrKey = String.valueOf(position) + ENTER_SPERATE + pkgTypeAndClass + ENTER_SPERATE
				+ entry;
		mAppsManager.handleMonitorAppInstall(packageName,
				MonitorAppstatisManager.TYPE_FROM_THEMESMANAGER, listenrKey);
	}
	
	/**
	 * 统计详情点击
	 * 
	 * @param context
	 * @param packageName
	 *            :主题包名
	 * @param position
	 *            :位置
	 * @param entry
	 *            :入口
	 * @param themeType
	 *            :主题分类
	 * */
	public void saveUserDetailClick(Context context, String packageName, int position, int themeType,
			String pkgType, String classId) {
		// 根据包名和位置判断数据库里是否已经存在该条记录
		int entry = getCurrentEntry(context);
		String pkgTypeAndClass = pkgType + CLASS_SPERATE + classId;
		String selection = getSelection(packageName, position, pkgTypeAndClass, entry);
		boolean isExist = mDataProvider.isExistData(GUIThemeTable.TABLENAME, selection,
				new String[] { GUIThemeTable.THEME_TYPE });
		if (isExist) {
			String sql = "update " + GUIThemeTable.TABLENAME + " set " + GUIThemeTable.CLICK_COUNT
					+ " = " + GUIThemeTable.DETAIL_CLICK + " + 1 " + "where " + selection;

			mDataProvider.exeSql(sql);
		} else {

			// 不存在，插入一条新数据
			ContentValues values = new ContentValues();
			values.put(GUIThemeTable.PACKAGE, packageName);
			values.put(GUIThemeTable.POSITION, position);
			values.put(GUIThemeTable.ENTRY, entry);
			values.put(GUIThemeTable.THEME_TYPE, themeType);
			values.put(GUIThemeTable.CLICK_COUNT, 0);
			values.put(GUIThemeTable.INSTALL_COUNT, 0);
			values.put(GUIThemeTable.PKG_TYPE, pkgTypeAndClass);
			values.put(GUIThemeTable.DETAIL_CLICK, 1);
			values.put(GUIThemeTable.DETAIL_GET_CLICK, 0);
			mDataProvider.insertData(GUIThemeTable.TABLENAME, values);
		}
	}
	
	/**
	 * 统计用户详情获取点击
	 * 
	 * @param context
	 * @param packageName
	 *            :主题包名
	 * @param position
	 *            :位置
	 * @param entry
	 *            :入口
	 * @param themeType
	 *            :主题分类
	 * */
	public void saveUserDetailGet(Context context, String packageName, int position, int themeType,
			String pkgType, String classId) {
		// 根据包名和位置判断数据库里是否已经存在该条记录
		int entry = getCurrentEntry(context);
		String pkgTypeAndClass = pkgType + CLASS_SPERATE + classId;
		String selection = getSelection(packageName, position, pkgTypeAndClass, entry);
		boolean isExist = mDataProvider.isExistData(GUIThemeTable.TABLENAME, selection,
				new String[] { GUIThemeTable.THEME_TYPE });
		if (isExist) {
			String sql = "update " + GUIThemeTable.TABLENAME + " set " + GUIThemeTable.CLICK_COUNT
					+ " = " + GUIThemeTable.DETAIL_GET_CLICK + " + 1 " + "where " + selection;

			mDataProvider.exeSql(sql);
		} else {

			// 不存在，插入一条新数据
			ContentValues values = new ContentValues();
			values.put(GUIThemeTable.PACKAGE, packageName);
			values.put(GUIThemeTable.POSITION, position);
			values.put(GUIThemeTable.ENTRY, entry);
			values.put(GUIThemeTable.THEME_TYPE, themeType);
			values.put(GUIThemeTable.CLICK_COUNT, 0);
			values.put(GUIThemeTable.INSTALL_COUNT, 0);
			values.put(GUIThemeTable.PKG_TYPE, pkgTypeAndClass);
			values.put(GUIThemeTable.DETAIL_CLICK, 0);
			values.put(GUIThemeTable.DETAIL_GET_CLICK, 1);
			mDataProvider.insertData(GUIThemeTable.TABLENAME, values);
		}
		// 需要统计安装数的处理
		String listenrKey = String.valueOf(position) + ENTER_SPERATE + pkgTypeAndClass + ENTER_SPERATE
				+ entry;
		mAppsManager.handleMonitorAppInstall(packageName,
				MonitorAppstatisManager.TYPE_FROM_THEMESMANAGER, listenrKey);
	}

	/**
	 * 查询所有记录
	 * 
	 * @return 按GUI收费数据统计协议拼装成的字符串 协议格式为：
	 *         15||1||主题类型||主题包名||入口类型||入口位置||点击数||安装数||是否正在使用
	 * */
	public String queryAllData() {
		StringBuffer allBuf = new StringBuffer();
		StringBuffer singleBuf = new StringBuffer();
		Cursor cursor = null;
		try {
			cursor = mDataProvider.queryData(GUIThemeTable.TABLENAME, null, null, null, null, null,
					null);
			if (cursor != null && cursor.getCount() > 0) {
				String curThemeName = mThemeManager.getCurThemePackage();
				int typeIndex = cursor.getColumnIndex(GUIThemeTable.THEME_TYPE);
				int packageNameIndex = cursor.getColumnIndex(GUIThemeTable.PACKAGE);
				int entryIndex = cursor.getColumnIndex(GUIThemeTable.ENTRY);
				int positionIndex = cursor.getColumnIndex(GUIThemeTable.POSITION);
				int clickCountIndex = cursor.getColumnIndex(GUIThemeTable.CLICK_COUNT);
				int installCountIndex = cursor.getColumnIndex(GUIThemeTable.INSTALL_COUNT);
				int pkgTypeIndex = cursor.getColumnIndex(GUIThemeTable.PKG_TYPE);
				int detailClickIndex = cursor.getColumnIndex(GUIThemeTable.DETAIL_CLICK);
				int detailGetIndex = cursor.getColumnIndex(GUIThemeTable.DETAIL_GET_CLICK);
				while (cursor.moveToNext()) {
					String packageName = cursor.getString(packageNameIndex);
					singleBuf.delete(0, singleBuf.length());
					singleBuf.append(PROTOCOL_TITLE);
					singleBuf.append(cursor.getInt(typeIndex) + PROTOCOL_DIVIDER);
					singleBuf.append(packageName + PROTOCOL_DIVIDER);
					singleBuf.append(cursor.getInt(entryIndex) + PROTOCOL_DIVIDER);
					singleBuf.append(cursor.getInt(positionIndex) + PROTOCOL_DIVIDER);
					singleBuf.append(cursor.getInt(clickCountIndex) + PROTOCOL_DIVIDER);
					singleBuf.append(cursor.getInt(installCountIndex) + PROTOCOL_DIVIDER);
					if (curThemeName.equals(packageName)) {
						singleBuf.append(1 + PROTOCOL_DIVIDER);
					} else {
						singleBuf.append(0 + PROTOCOL_DIVIDER);
					}
					String pkyTypeAndClass = cursor.getString(pkgTypeIndex);
					String pkgType = "0";
					String classId = "0";
					if (pkyTypeAndClass != null && !pkyTypeAndClass.equals("")) {
						String[] datas =  pkyTypeAndClass.split(CLASS_SPERATE);
						if (datas.length > 0) {
							pkgType = datas[0];
						}
						if (datas.length > 1) {
							classId = datas[1];
						}
					}
					singleBuf.append(pkgType + PROTOCOL_DIVIDER);
					singleBuf.append(classId + PROTOCOL_DIVIDER);
					singleBuf.append(cursor.getInt(detailClickIndex) + PROTOCOL_DIVIDER);
					singleBuf.append(cursor.getInt(detailGetIndex));
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

	/**
	 * 包名统计
	 * 
	 * @return 按GUI收费数据统计协议拼装成的字符串 协议格式为： 15||2||主题类型||主题包名||正在使用包名
	 * */
	public String queryPackageData() {
		StringBuffer allBuf = new StringBuffer();
		StringBuffer singleBuf = new StringBuffer();
		try {
			// 桌面主题
			ArrayList<ThemeInfoBean> themeInfoBeans = mThemeManager.getAllInstalledThemeInfos();
			if (themeInfoBeans != null && themeInfoBeans.size() > 0) {
				String currentLauncherTheme = mThemeManager.getCurThemePackage();
				for (ThemeInfoBean infoBean : themeInfoBeans) {
					if (singleBuf.length() > 0) {
						singleBuf.delete(0, singleBuf.length());
					}
					singleBuf.append(PROTOCOL_TITLE2);
					singleBuf.append(THEME_LAUNCHER_TYPE + PROTOCOL_DIVIDER);
					singleBuf.append(infoBean.getPackageName() + PROTOCOL_DIVIDER);
					singleBuf.append(currentLauncherTheme);
					allBuf.append(singleBuf);
					allBuf.append("\r\n");
				}
			}
			// 锁屏主题
			String currentLockerTheme = mThemeManager.getCurLockerTheme();
			// 默认
			if (singleBuf.length() > 0) {
				singleBuf.delete(0, singleBuf.length());
			}
			singleBuf.append(PROTOCOL_TITLE2);
			singleBuf.append(THEME_LOCKER_TYPE + PROTOCOL_DIVIDER);
			singleBuf.append(LauncherEnv.GO_LOCK_PACKAGE_NAME + PROTOCOL_DIVIDER);
			singleBuf.append(currentLockerTheme);
			allBuf.append(singleBuf);
			allBuf.append("\r\n");
			// 随机主题
			if (singleBuf.length() > 0) {
				singleBuf.delete(0, singleBuf.length());
			}
			singleBuf.append(PROTOCOL_TITLE2);
			singleBuf.append(THEME_LOCKER_TYPE + PROTOCOL_DIVIDER);
			singleBuf.append("com.jiubang.goscreenlock.theme.random" + PROTOCOL_DIVIDER);
			singleBuf.append(currentLockerTheme);
			allBuf.append(singleBuf);
			allBuf.append("\r\n");
			Map<CharSequence, CharSequence> mInstalledLockerThemeMap = new GoLockerThemeManager(
					mContext).queryInstalledTheme();
			Iterator<CharSequence> iterator = mInstalledLockerThemeMap.keySet().iterator();
			while (iterator.hasNext()) {
				if (singleBuf.length() > 0) {
					singleBuf.delete(0, singleBuf.length());
				}
				CharSequence packageName = iterator.next();
				singleBuf.append(PROTOCOL_TITLE2);
				singleBuf.append(THEME_LOCKER_TYPE + PROTOCOL_DIVIDER);
				singleBuf.append(packageName + PROTOCOL_DIVIDER);
				singleBuf.append(currentLockerTheme);
				allBuf.append(singleBuf);
				allBuf.append("\r\n");
			}
		} catch (Exception e) {

		}
		return allBuf.toString();
	}

	/**
	 * 包名统计(For Zip)
	 * 
	 * @author zhouxuewen
	 * @return 按GUI ZIP包收费数据统计协议拼装成的字符串 协议格式为： 15||2||主题类型||主题包名||正在使用包名
	 * */
	public String queryPackageDataForZip() {
		StringBuffer allBuf = new StringBuffer();
		StringBuffer singleBuf = new StringBuffer();
		try {
			// 桌面主题
			ArrayList<ThemeInfoBean> themeInfoBeans = new ArrayList<ThemeInfoBean>();

			ConcurrentHashMap<String, ThemeInfoBean> map = mThemeManager.scanAllZipThemes();
			Set<String> keys = map.keySet();
			for (String key : keys) {
				themeInfoBeans.add(map.get(key));
			}

			if (themeInfoBeans != null && themeInfoBeans.size() > 0) {
				String currentLauncherTheme = mThemeManager.getCurThemePackage();
				for (ThemeInfoBean infoBean : themeInfoBeans) {
					if (singleBuf.length() > 0) {
						singleBuf.delete(0, singleBuf.length());
					}
					singleBuf.append(PROTOCOL_TITLE2);
					singleBuf.append(THEME_LAUNCHER_TYPE + PROTOCOL_DIVIDER);
					singleBuf.append(infoBean.getPackageName() + PROTOCOL_DIVIDER);
					singleBuf.append(currentLauncherTheme);
					allBuf.append(singleBuf);
					allBuf.append("\r\n");
				}
			}
		} catch (Exception e) {

		}
		return allBuf.toString();
	}

	/**
	 * 上传数据后，清空数据
	 * */
	public void clearData() {
		// String sql ="DROP TABLE "+GUIThemeTable.TABLENAME;
		// mDataProvider.exeSql(sql);
		mDataProvider.delete(GUIThemeTable.TABLENAME, null, null);
	}
}
