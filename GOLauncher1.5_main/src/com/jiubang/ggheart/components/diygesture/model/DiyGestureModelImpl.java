package com.jiubang.ggheart.components.diygesture.model;

import java.util.ArrayList;
import java.util.Set;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.gesture.Gesture;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingGestureScreenActivity;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogConfirm;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.components.diygesture.gesturemanageview.DiyGestureConstants;
import com.jiubang.ggheart.data.AppDataEngine;
import com.jiubang.ggheart.data.info.AppItemInfo;

/***
 * 自定义手势数据层接口实现类
 */
public class DiyGestureModelImpl {

	private Context mContext;

	private DiyGestureDataModel mDataModel;
	private ArrayList<DiyGestureInfo> mDiyGestureInfoList = null; // 自定义手势列表
	private DiyGestureFileManager mDiyGestureFileManager = null; // 手势文件管理类
	private boolean mIsAllDiyGestureInfoLoaded = false; // 是否所有自定义手势数据加载完毕
	private LoadDiyGestureDataTask mLoadDiyGestureDataTask = null; // 用于加载自定义手势文件数据、数据库数据的异步线程

	private static DiyGestureModelImpl sInstance; // 静态单例

	public static int sOpeningActivityFlag = 0; // 用于记录手势的几个activity打开状态
	public static int sFLAG_RECONIZE = 0x1; // 手势识别activity
	public static int sFLAG_MANAGER = 0x2; // 手势管理activity
	public static int sFLAG_ADD = 0x4; // 手势添加activity
	public static int sFLAG_EDIT = 0x8; // 手势修改activity

	/**
	 * 外部获取静态单例
	 * 
	 * @param context
	 * @return
	 */
	public synchronized static DiyGestureModelImpl getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new DiyGestureModelImpl(context);
		}
		return sInstance;
	}

	private DiyGestureModelImpl(Context context) {
		mContext = context;
		mDataModel = new DiyGestureDataModel(context);
		mDiyGestureFileManager = new DiyGestureFileManager();
		mDiyGestureInfoList = new ArrayList<DiyGestureInfo>();
		mIsAllDiyGestureInfoLoaded = false;
		mLoadDiyGestureDataTask = new LoadDiyGestureDataTask();
		mLoadDiyGestureDataTask.execute();
	}

	/**
	 * 
	 * <br>类描述:异步加载手势数据类
	 * <br>功能详细描述:
	 * 
	 */
	private class LoadDiyGestureDataTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			if (mDiyGestureFileManager.loadDiyGestureFromFile()) {
				getAllDiyGestureInfoListFromDB();
				loadTitle();
				mIsAllDiyGestureInfoLoaded = true;
				return true;
			} else {
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (!result) {
				Toast.makeText(mContext,
						mContext.getResources().getString(R.string.gesture_load_fail), 0).show();
				((Activity) mContext).finish();
			}
		}
	}

	/**
	 * 从数据库加载所有的手势
	 */
	private void getAllDiyGestureInfoListFromDB() {
		if (mDiyGestureInfoList == null) {
			mDiyGestureInfoList = new ArrayList<DiyGestureInfo>();
		} else {
			mDiyGestureInfoList.clear();
		}
		ArrayList<DiyGestureInfo> list = mDataModel.getAllGestureInfos();
		int size = list.size();
		for (int i = 0; i < size; i++) {
			DiyGestureInfo diyGestureInfo = list.get(i);
			String gestureFileName = diyGestureInfo.getGestureFileName();
			if (gestureFileName != null) {
				Gesture gesture = mDiyGestureFileManager.getGestureByName(gestureFileName);
				if (gesture != null) {
					diyGestureInfo.setGesture(gesture);
					mDiyGestureInfoList.add(diyGestureInfo);
				}
			}
		}
	}

	/**
	 * 获取手势的个数
	 * 
	 * @return
	 */
	public int getAllGestureInfosSize() {
		ArrayList<DiyGestureInfo> list = mDataModel.getAllGestureInfos();
		if (list != null) {
			return list.size();
		}
		return 0;
	}

	/**
	 * 加载手势的类型及名称
	 */
	private void loadTitle() {
		if (mDiyGestureInfoList == null || mContext == null) {
			return;
		}

		// 前綴
		String openAppStr = mContext.getString(R.string.gesture_app);
		String shortcutStr = mContext.getString(R.string.gesture_shortcut);
		String goShortcutStr = mContext.getString(R.string.gesture_goshortcut);
		String uninstalled = mContext.getString(R.string.gesture_uninstalled);

		int size = mDiyGestureInfoList.size();
		AppDataEngine engine = null;
		try {
			engine = AppDataEngine.getInstance(mContext);
		} catch (Throwable e) {
			// 如果重新创建AppDataEngine，会报异常
		}
		PackageManager pm = mContext.getPackageManager();
		String launcherCategory = "android.intent.category.LAUNCHER";
		for (int i = 0; i < size; i++) {
			DiyGestureInfo info = mDiyGestureInfoList.get(i);
			Intent intent = info.getIntent();
			String name = info.getName();

			int type = info.getType();
			switch (type) {
				case DiyGestureConstants.TYPE_APP : {
					// 打开应用
					AppItemInfo appItemInfo = null;
					if (engine != null) {
						appItemInfo = engine.getAppItem(intent);
						if (appItemInfo != null) {
							name = appItemInfo.mTitle != null ? appItemInfo.mTitle : name;
							info.setName(name);
							info.setTypeName(openAppStr);
							break;
						}
					}

					// 已卸载
					ResolveInfo resloInfo = pm.resolveActivity(intent, 0);
					if (resloInfo == null) {
						info.setName(name);
						info.setTypeName(uninstalled);
						break;
					}

					info.setName(openAppStr + name);
				}
					break;

				case DiyGestureConstants.TYPE_SHORTCUT : {
					// 已卸载
					ResolveInfo resloInfo = pm.resolveActivity(intent, 0);
					if (resloInfo == null) {
						info.setName(name);
						info.setTypeName(uninstalled);
						break;
					}

					// 快捷方式，只可以读到最初的名字
					info.setName(name);
					info.setTypeName(shortcutStr);
				}
					break;

				case DiyGestureConstants.TYPE_GOSHORTCUT : {
					// Go快捷方式
					String goName = DiyGestureConstants.getGoShortcutName(mContext, intent);
					info.setName(goName);
					info.setTypeName(goShortcutStr);

				}
					break;

				default : {
					// 3.11前的老用户分支，3.11才加入新字段itemtype
					String typeName = null;
					ResolveInfo resloInfo = pm.resolveActivity(intent, 0);

					// Go快捷方式
					typeName = DiyGestureConstants.getGoShortcutName(mContext, intent);
					if (typeName != null) {
						info.setName(typeName);
						info.setTypeName(goShortcutStr);
						break;
					}

					// 打开应用
					Set<String> categoriesSet = intent.getCategories();
					if (resloInfo != null && categoriesSet != null
							&& categoriesSet.contains(launcherCategory)) {
						ActivityInfo activityInfo = resloInfo.activityInfo;
						if (activityInfo != null) {
							String label = activityInfo.loadLabel(pm).toString();
							name = label != null ? label : name;
						}
						info.setName(name);
						info.setTypeName(openAppStr);
						break;
					}

					if (resloInfo == null) {
						// Go桌面内置假图标
						if (engine != null) {
							AppItemInfo appItemInfo = engine.getAppItem(intent);
							if (appItemInfo != null) {
								name = appItemInfo.mTitle != null ? appItemInfo.mTitle : name;
								info.setName(name);
								info.setTypeName(openAppStr);
								break;
							}
						}

						// 已卸载
						info.setName(name);
						info.setTypeName(uninstalled);
						break;
					}

					// 系统快捷方式
					info.setName(name);
					info.setTypeName(shortcutStr);
				}
					break;
			}
		}
	}

	/**
	 * 是否所有自定义手势DB数据加载完毕
	 * 
	 * @return
	 */
	public boolean isAllDiyGestureDataLoaded() {
		return mIsAllDiyGestureInfoLoaded;
	}

	/**
	 * 获取所有自定义手势，必须在所有自定义手势DB数据加载完毕{@link #isAllDiyGestureDataLoaded()}
	 * =true才可以调用
	 * 
	 * @return
	 */
	public ArrayList<DiyGestureInfo> getAllDiyGestureInfoList() {
		return mDiyGestureInfoList;
	}

	/**
	 * 通过手势名称拿info
	 * 
	 * @param name
	 * @return
	 */
	public DiyGestureInfo getDiyGestureInfoByGestureName(String name) {
		DiyGestureInfo diyGestureInfo = null;
		for (DiyGestureInfo d : mDiyGestureInfoList) {
			if (d.getGestureFileName().equals(name)) {
				diyGestureInfo = d;
				break;
			}
		}
		return diyGestureInfo;
	}

	/**
	 * 获取手势匹配列表，得分从高到低排列
	 * 
	 * @param gesture
	 * @return
	 */
	public ArrayList<DiyGestureInfo> recogizeGesture(Gesture gesture) {
		ArrayList<DiyGestureInfo> recogizeGestureInfoList = new ArrayList<DiyGestureInfo>();
		// 先通过自定义手势文件管理类获得识别后相似手势的名字列表
		ArrayList<String> recogiserGestureNameList = mDiyGestureFileManager
				.getRecogiserGestureNameList(gesture);
		// 再通过名字列表查找存储所有自定义手势的mDiyGestureInfoList组合成recogizeGestureInfoList
		for (String recogiserGestureName : recogiserGestureNameList) {
			for (DiyGestureInfo diyGestureInfo : mDiyGestureInfoList) {
				if (diyGestureInfo.getGestureFileName().equals(recogiserGestureName)) {
					recogizeGestureInfoList.add(diyGestureInfo);
				}
			}
		}
		return recogizeGestureInfoList;
	}

	/**
	 * 添加手势记录
	 * 
	 * @param diyGestureInfo
	 * @return
	 */
	public boolean addGesture(DiyGestureInfo diyGestureInfo) {
		if (diyGestureInfo != null && diyGestureInfo.getGestureFileName() != null
				&& diyGestureInfo.getmGesture() != null) {
			// 增加到gesture文件
			boolean addFile = mDiyGestureFileManager.addGestureToFile(
					diyGestureInfo.getGestureFileName(), diyGestureInfo.getmGesture());

			if (addFile) {
				// 增加到数据库
				final ContentValues values = new ContentValues();
				diyGestureInfo.writeObject(values, null);
				boolean addDb = mDataModel.insertGesture(values);

				if (addDb) {
					// mDiyGestureInfoList列表刷新数据,首部插入
					mDiyGestureInfoList.add(0, diyGestureInfo);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 删除手势记录
	 */
	public boolean deleteGesture(DiyGestureInfo diyGestureInfo) {
		if (diyGestureInfo != null) {
			// gesture文件中删除
			boolean removeFile = mDiyGestureFileManager.removeGestureFromFile(diyGestureInfo
					.getGestureFileName());

			if (removeFile) {
				// 数据库中删除
				boolean removeDb = mDataModel.delGesture(diyGestureInfo.getID());
				if (removeDb) {
					mDiyGestureInfoList.remove(diyGestureInfo);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 删除所有手势
	 */
	public boolean deleteAllGesture(ArrayList<DiyGestureInfo> diyGestureInfoList) {
		int diyGestureInfoListSize = diyGestureInfoList.size();
		for (int i = 0; i < diyGestureInfoListSize; ++i) {
			DiyGestureInfo diyGestureInfo = diyGestureInfoList.get(i);
			if (!deleteGesture(diyGestureInfo)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 修改手势
	 * 
	 * @param diyGestureInfo
	 * @return
	 */
	public boolean modifyGestureResetGesture(DiyGestureInfo diyGestureInfo) {
		if (diyGestureInfo != null) {
			// gesture文件中删除原gesture
			boolean remove = mDiyGestureFileManager.removeGestureFromFile(diyGestureInfo
					.getGestureFileName());

			// gesture文件中新增新的gesture，但name和原来的一样
			boolean add = mDiyGestureFileManager.addGestureToFile(
					diyGestureInfo.getGestureFileName(), diyGestureInfo.getmGesture());

			// 数据库什么也不用做

			if (remove && add) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 修改响应
	 * 
	 * @param diyGestureInfo
	 * @return
	 */
	public boolean modifyGestureResetAction(DiyGestureInfo diyGestureInfo) {
		if (diyGestureInfo != null) {
			// gesture文件什么也不用做

			// 数据库修改
			long id = diyGestureInfo.getID();
			final ContentValues values = new ContentValues();
			diyGestureInfo.writeObject(values, null);
			return mDataModel.updateGesture(id, values);
		}
		return false;
	}

	public static void addFlag(int flag) {
		sOpeningActivityFlag |= flag;
	}

	public static void removeFlag(int flag) {
		sOpeningActivityFlag &= ~flag;
	}

	/**
	 * 退出时检查清理资源
	 */
	public static void checkClear() {
		if (sOpeningActivityFlag == 0) {
			//完全退出自定义手势功能
			sInstance = null;

			//是否弹出关闭自定义手势引导判断
			GoLauncher gl = GoLauncher.getContext();
			if (gl != null && !gl.isFinishing()) {
				PreferencesManager sharedPreferences = new PreferencesManager(gl,
						IPreferencesIds.DESK_SHAREPREFERENCES_FILE, Context.MODE_PRIVATE);
				int time = sharedPreferences.getInt(IPreferencesIds.CANCLE_DIYGESTURE_TIME, 0);
				if (time >= 0 && time < 3) {
					time++;
					sharedPreferences.putInt(IPreferencesIds.CANCLE_DIYGESTURE_TIME, time);
					sharedPreferences.commit();
					if (time == 3) {
						showCloseGestureDialog(gl);
					}
				}
			}
		}
	}

	/**
	 * <br>功能简述:关闭自定义手势引导弹框
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param activity
	 */
	private static void showCloseGestureDialog(final Activity activity) {
		DialogConfirm mNormalDialog = new DialogConfirm(activity);
		mNormalDialog.show();
		mNormalDialog.setTitle(R.string.close_gesture_dialog_title);
		String string1 = activity.getString(R.string.close_gesture_dialog_contend_1);
		String string2 = activity.getString(R.string.close_gesture_dialog_contend_2);
		string2 = "<font color=\"#92D060\">" + string2 + "</font>";
		String string3 = activity.getString(R.string.close_gesture_dialog_contend_3);
		String string = string1 + string2 + string3;
		mNormalDialog.setHtmlMessage(string);
		mNormalDialog.setPositiveButton(R.string.close_gesture_dialog_setbutton,
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(activity, DeskSettingGestureScreenActivity.class);
						activity.startActivity(intent);
					}
				});
	}
}
