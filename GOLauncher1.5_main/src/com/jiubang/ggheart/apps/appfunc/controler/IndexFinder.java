package com.jiubang.ggheart.apps.appfunc.controler;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.go.util.SortUtils;
import com.jiubang.ggheart.apps.appfunc.setting.FunAppSetting;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncFrame;
import com.jiubang.ggheart.data.info.FunFolderItemInfo;
import com.jiubang.ggheart.data.info.FunItemInfo;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 功能表的index查找工具类
 */
public class IndexFinder {

	/*
	 * 通过比较字符串选择位置
	 */
	public static final int COMPARE_WITH_STRING = 1;

	/*
	 * 通过比较时间选择位置
	 */
	public static final int COMPARE_WITH_TIME = 2;

	/*
	 * 通过比较使用频率选择位置
	 */
	public static final int COMPARE_WITH_FREQUENCE = 3;

	/*
	 * 其他
	 */
	static final int COMPARE_WITH_OTHER = 4;

	public static int findFirstIndex(Context context, final List<? extends FunItemInfo> infos,
			final boolean inApps, int compareType, Object value, final String order) {
		int result = -1;
		boolean desc = 0 == order.compareTo("DESC");
		boolean found = false;
		int comp = 0;
		Intent it = null;
		FunItemInfo funItemInfo = null;
		int size = infos.size();
		// 如果为空，则放在末尾
		if (value == null) {
			return size;
		}
		for (int i = 0; i < size; ++i) {
			result = i;
			funItemInfo = infos.get(i);
			if (null == funItemInfo) {
				continue;
			}

			if (FunItemInfo.TYPE_FOLDER == funItemInfo.getType() && inApps) {
				continue;
			}

			if (FunItemInfo.TYPE_APP == funItemInfo.getType() && !inApps) {
				continue;
			}

			it = funItemInfo.getIntent();
			if (null == it) {
				continue;
			}

			// 排序比较时跳过：应用中心
			// Add by songzhaochun, 2012.06.14
			if (ICustomAction.ACTION_FUNC_SHOW_RECOMMENDCENTER.equals(it.getAction())) {
				continue;
			}

			// 排序比较时跳过：游戏中心
			// Add by songzhaochun, 2012.06.14
			if (ICustomAction.ACTION_FUNC_SHOW_GAMECENTER.equals(it.getAction())) {
				continue;
			}

			// 比较两个字符串大小
			if (funItemInfo.getTitle() == null) {
				continue;
			}
			switch (compareType) {
				case COMPARE_WITH_STRING :
					comp = SortUtils.compareInLetter((String) value, funItemInfo.getTitle());
					break;
				case COMPARE_WITH_FREQUENCE :
					int clickedCount = (Integer) value;
					int itemClickedCount = funItemInfo.getClickedCount(context);
					if (clickedCount > itemClickedCount) {
						comp = 1;
					} else if (clickedCount == itemClickedCount) {
						comp = 0;
					} else {
						comp = -1;
					}
					break;
				default :
					break;
			}
			// comp = SortUtils.compareInLetter(value, funItemInfo.getTitle());
			if (desc && comp >= 0) {
				result = i;
				found = true;
				break;
			} else if (!desc && comp <= 0) {
				result = i;
				found = true;
				break;
			}
		}
		// 没有找到则应用程序直接放到最后一位，文件夹放到第一位
		if (!found) {
			if (inApps) {
				result = size;
			} else {
				result = 0;
			}
		}

		return result;
	}

	/**
	 * 查找对应的index
	 * 
	 * @param infos
	 *            被查找的列表
	 * @param inApps
	 *            文件夹还是程序
	 * @param item
	 *            查找的图标
	 * @param packageMgr
	 * @param compareType
	 *            比较的类型，如string 还是 时间
	 * @param order
	 *            获取顺序的类型
	 * @return 寻找当前查找的图标在列表中应插入的位置
	 */

	public static int findFirstIndex(final Context context,
			final List<? extends FunItemInfo> infos, final boolean inApps,
			final FunItemInfo item, final PackageManager packageMgr, final int compareType,
			final String order) {
		int result = -1;
		// boolean cmpStringType = (COMPARE_WITH_STRING == compareType);
		boolean desc = 0 == order.compareTo("DESC");
		boolean found = false;
		int comp = 0;

		Intent it = null;
		FunItemInfo funItemInfo = null;

		int cmpSize = 0;
		int size = infos.size();
		for (int i = 0; i < size; ++i) {
			result = i;
			funItemInfo = infos.get(i);
			if (null == funItemInfo) {
				continue;
			}

			if (FunItemInfo.TYPE_FOLDER == funItemInfo.getType() && inApps) {
				continue;
			}

			if (FunItemInfo.TYPE_APP == funItemInfo.getType() && !inApps) {
				continue;
			}

			it = funItemInfo.getIntent();
			if (null == it) {
				continue;
			}

			// 排序比较时跳过：应用中心
			// Add by songzhaochun, 2012.06.14
			if (ICustomAction.ACTION_FUNC_SHOW_RECOMMENDCENTER.equals(it.getAction())) {
				continue;
			}

			// 排序比较时跳过：游戏中心
			// Add by songzhaochun, 2012.06.14
			if (ICustomAction.ACTION_FUNC_SHOW_GAMECENTER.equals(it.getAction())) {
				continue;
			}

			switch (compareType) {
				case COMPARE_WITH_STRING :
					comp = SortUtils.compareInLetter(item.getTitle(), funItemInfo.getTitle());
					break;
				case COMPARE_WITH_TIME :
					long timeComItem = item.getTime(packageMgr);
					long timeItem = funItemInfo.getTime(packageMgr);
					if (timeComItem > timeItem) {
						comp = 1;
					} else if (timeComItem == timeItem) {
						comp = 0;
					} else {
						comp = -1;
					}
					break;
				case COMPARE_WITH_FREQUENCE :
					int frequenceComItem = item.getClickedCount(context);
					int frequenceItem = funItemInfo.getClickedCount(context);
					if (frequenceComItem > frequenceItem) {
						comp = 1;
					} else if (frequenceComItem == frequenceItem) {
						comp = 0;
					} else {
						comp = -1;
					}
					break;
				default :
					break;
			}

			// 比较两个字符串大小
			// if (cmpStringType) {
			// comp = SortUtils.compareInLetter(item.getTitle(),
			// funItemInfo.getTitle());
			// } else {
			// // 比较时间大小
			// long timeComItem = item.getTime(packageMgr);
			// long timeItem = funItemInfo.getTime(packageMgr);
			// if (timeComItem > timeItem) {
			// comp = 1;
			// } else if (timeComItem == timeItem) {
			// comp = 0;
			// } else {
			// comp = -1;
			// }
			// }

			cmpSize++;
			if (desc && comp >= 0) {
				result = i;
				found = true;
				break;
			} else if (!desc && comp <= 0) {
				result = i;
				found = true;
				break;
			}
		}
		// 没有找到则应用程序直接放到最后一位，文件夹放到文件夹列表最后
		if (!found) {
			if (inApps) {
				result = size;
			} else {
				result = cmpSize++;
			}
		}

		return result;
	}

	public static int findLastItemInApps(final List<? extends FunItemInfo> infos, boolean isApp) {
		FunItemInfo funItemInfo = null;
		int size = infos.size();
		int result = size;
		if (!isApp) {
			// 如果是文件夹，默认在最前面
			result = 0;
		}
		int type = isApp ? FunItemInfo.TYPE_APP : FunItemInfo.TYPE_FOLDER;
		for (int i = size - 1; i >= 0; --i) {
			funItemInfo = infos.get(i);
			if (null == funItemInfo) {
				continue;
			}

			if (type == funItemInfo.getType()) {
				result = i + 1;
				break;
			}
		}

		if (1 == result) {
			result = -1;
		}

		return result;
	}

	public static int findFirstItemInApps(final List<? extends FunItemInfo> infos,
			boolean isApp) {
		int result = -1;
		FunItemInfo funItemInfo = null;
		int size = infos.size();
		int type = isApp ? FunItemInfo.TYPE_APP : FunItemInfo.TYPE_FOLDER;
		for (int i = 0; i < size; ++i) {
			funItemInfo = infos.get(i);
			if (null == funItemInfo) {
				continue;
			}

			if (type == funItemInfo.getType()) {
				result = i;
				break;
			}
		}

		// 如果所在位置存在：游戏中心、应用中心，则索引放在其后
		// Add by songzhaochun, 2012.06.26
		if (result >= 0) {
			for (int i = result; i < infos.size(); i++) {
				FunItemInfo item = infos.get(i);
				if (item != null
						&& item.getIntent() != null
						&& ICustomAction.ACTION_FUNC_SHOW_RECOMMENDCENTER.equals(item.getIntent()
								.getAction())) {
					result = i + 1;
					continue;
				}
				if (item != null
						&& item.getIntent() != null
						&& ICustomAction.ACTION_FUNC_SHOW_GAMECENTER.equals(item.getIntent()
								.getAction())) {
					result = i + 1;
					continue;
				}
				break;
			}
		}

		return result;
	}

	/**
	 * 查找对应的index
	 * 
	 * @param context
	 * @param infos
	 * @param inApps
	 *            true代表查找App，fasle代表查找文件夹
	 * @param item
	 *            需要插入的图标
	 * @return
	 */
	public static int findIndex(final Context context,
			final List<? extends FunItemInfo> infos, final boolean inApps,
			final FunItemInfo item) {

		int countIndex = -1;
		int sortType = GOLauncherApp.getSettingControler().getFunAppSetting().getSortType();

		PackageManager packageMgr = context.getPackageManager();

		// 获取添加位置
		if (FunAppSetting.SORTTYPE_LETTER == sortType) {
			countIndex = findFirstIndex(context, infos, inApps, item, packageMgr,
					COMPARE_WITH_STRING, "ASC");
		} else if (FunAppSetting.SORTTYPE_TIMENEAR == sortType) {
			countIndex = findFirstIndex(context, infos, inApps, item, packageMgr,
					COMPARE_WITH_TIME, "DESC");
		} else if (FunAppSetting.SORTTYPE_TIMEREMOTE == sortType) {
			countIndex = findFirstIndex(context, infos, inApps, item, packageMgr,
					COMPARE_WITH_TIME, "ASC");
		} else if (FunAppSetting.SORTTYPE_FREQUENCY == sortType) {
			countIndex = findFirstIndex(context, infos, inApps, item, packageMgr,
					COMPARE_WITH_FREQUENCE, "DESC");
		} else {
			countIndex = findFirstIndex(context, infos, inApps, item, packageMgr,
					COMPARE_WITH_OTHER, "ASC");
		}

		if (countIndex == -1) {
			if (!inApps) {
				countIndex = 0;
			} else {
				FunControler funControler = AppFuncFrame.getFunControler();
				ArrayList<FunFolderItemInfo> list = funControler.getFunFolders();
				countIndex = list.get(list.size() - 1).getIndex() + 1;
			}
		}
		return countIndex;
	}
}
