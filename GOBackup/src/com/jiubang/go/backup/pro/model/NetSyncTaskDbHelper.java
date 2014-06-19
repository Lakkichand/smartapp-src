package com.jiubang.go.backup.pro.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.text.TextUtils;

import com.jiubang.go.backup.pro.data.AppBackupEntry;
import com.jiubang.go.backup.pro.data.BaseBackupEntry;
import com.jiubang.go.backup.pro.data.BaseEntry;
import com.jiubang.go.backup.pro.data.BaseEntry.EntryType;
import com.jiubang.go.backup.pro.data.LauncherDataBackupEntry;
import com.jiubang.go.backup.pro.image.util.ImageBean;
import com.jiubang.go.backup.pro.image.util.OneImageBackupEntry;

/**
 * 网络备份缓存数据库，用于实现网络备份
 *
 * @author wencan
 */
// CHECKSTYLE:OFF
public class NetSyncTaskDbHelper extends SQLiteOpenHelper {

	private static final String DB_NAME = "net_sync_task.db";
	private static final int DB_VERSION = 1;

	private String mCurDbName;

	public NetSyncTaskDbHelper(Context context, String name, int version) {
		super(context, name, null, version);
		File dbFile = new File(name);
		mCurDbName = dbFile.getName();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		try {
			db.beginTransaction();
			db.execSQL(TaskTable.CREATE_TABLE);
			db.setTransactionSuccessful();
		} catch (Exception e) {
		} finally {
			db.endTransaction();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	public static String getDbName() {
		return DB_NAME;
	}

	public static int getDbVersion() {
		return DB_VERSION;
	}

	/**
	 * 获取当前打开的数据库的文件名
	 *
	 * @return
	 */
	public String getCurDbName() {
		return mCurDbName;
	}

	/**
	 * 从备份项中重构备份任务
	 *
	 * @param allBackupEntrys
	 * @return
	 */
	public boolean rebuildBackupTask(List<BaseEntry> allBackupEntrys) {
		if (allBackupEntrys == null || allBackupEntrys.size() == 0) {
			return true;
		}

		// 删除原先的item
		delete(TaskTable.TABLE_NAME, TaskTable.TASK_TYPE + "=" + TaskType.ONLINE_BACKUP.value(),
				null);

		Date date = new Date();
		// String dropboxDirPath = Constant.buildDropboxContentDir();
		ContentValues cv = new ContentValues();
		for (BaseEntry entry : allBackupEntrys) {
			if (!(entry instanceof BaseBackupEntry)) {
				continue;
			}

			cv.clear();
			cv.put(TaskTable.TIME, date.getTime());
			cv.put(TaskTable.TASK_TYPE, TaskType.ONLINE_BACKUP.value());
			cv.put(TaskTable.TASK_STATE, TaskState.NOT_START.value());
			// cv.put(TaskTable.PATH, dropboxDirPath);

			EntryType type = entry.getType();
			switch (type) {
				case TYPE_SYSTEM_APP :
				case TYPE_USER_APP :
					AppBackupEntry appEntry = (AppBackupEntry) entry;
					cv.put(TaskTable.TASK_OBJECT, TaskObject.APP.value());
					cv.put(TaskTable.EXTRA_DATA1, appEntry.getAppApkFileName());
					cv.put(TaskTable.EXTRA_DATA2, appEntry.getAppDataFileName());
					cv.put(TaskTable.EXTRA_DATA3, appEntry.getAppInfo().appName);
					cv.put(TaskTable.EXTRA_DATA4, appEntry.getAppInfo().packageName);
					cv.put(TaskTable.EXTRA_DATA5, appEntry.getAppInfo().appType);
					insert(TaskTable.TABLE_NAME, null, cv);
					break;

				case TYPE_SYSTEM_WIFI :
				case TYPE_USER_CALL_HISTORY :
				case TYPE_USER_CONTACTS :
				case TYPE_USER_DICTIONARY :
				case TYPE_USER_GOLAUNCHER_SETTING :
				case TYPE_USER_MMS :
				case TYPE_USER_SMS :
				case TYPE_SYSTEM_RINGTONE :
				case TYPE_SYSTEM_WALLPAPER :
				case TYPE_USER_BOOKMARK :
				case TYPE_USER_CALENDAR :
					cv.put(TaskTable.TASK_OBJECT, TaskObject.valueOf(type).value());
					insert(TaskTable.TABLE_NAME, null, cv);
					break;

				case TYPE_USER_IMAGE :
					ImageBean image = ((OneImageBackupEntry) entry).getImage();
					cv.put(TaskTable.TASK_OBJECT, TaskObject.valueOf(type).value());
					cv.put(TaskTable.EXTRA_DATA1, image.mImagePath);
					cv.put(TaskTable.EXTRA_DATA2, image.mImageShape);
					cv.put(TaskTable.EXTRA_DATA3, image.mImageDisplayName);
					cv.put(TaskTable.EXTRA_DATA4, image.mImageSize);
					cv.put(TaskTable.EXTRA_DATA5, image.mImageParentFilePath);
					insert(TaskTable.TABLE_NAME, null, cv);
					break;

				case TYPE_SYSTEM_LAUNCHER_DATA :
					LauncherDataBackupEntry launcherEntry = (LauncherDataBackupEntry) entry;
					cv.put(TaskTable.TASK_OBJECT, TaskObject.valueOf(type).value());
					cv.put(TaskTable.EXTRA_DATA4, launcherEntry.getAppInfo().packageName);
					insert(TaskTable.TABLE_NAME, null, cv);
					break;

				default :
					break;
			}
		}

		// 插入上传数据库task
		if (allBackupEntrys.size() > 0) {
			cv.clear();
			cv.put(TaskTable.TIME, date.getTime());
			cv.put(TaskTable.TASK_TYPE, TaskType.ONLINE_BACKUP.value());
			cv.put(TaskTable.TASK_STATE, TaskState.NOT_START.value());
			cv.put(TaskTable.TASK_OBJECT, TaskObject.UPLOAD_DB.value());
			insert(TaskTable.TABLE_NAME, null, cv);
		}
		return true;
	}

	public void clear() {
		SQLiteDatabase db = null;
		try {
			db = getWritableDatabase();
			if (db != null) {
				db.delete(TaskTable.TABLE_NAME, null, null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean hasOnlineTask(TaskType type) {
		if (type == null) {
			return false;
		}

		boolean hasTask = false;
		Cursor cursor = null;
		try {
			cursor = query(TaskTable.TABLE_NAME, new String[] { TaskTable.TASK_TYPE },
					TaskTable.TASK_TYPE + "=" + type.value(), null, null, null, null);
			if (cursor == null) {
				return false;
			}
			hasTask = cursor.getCount() > 0;
		} catch (Exception e) {
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return hasTask;
	}

	/**
	 * 获取所有未完成的某种类型的任务列表
	 *
	 * @param type
	 * @return
	 */
	public List<Task> getAllNotFinishedTasks(TaskType type) {
		if (type == null) {
			return null;
		}

		Cursor cursor = null;
		try {
			String where = TaskTable.TASK_STATE + "!=" + TaskState.FINISHED.value();
			if (type != null) {
				where += " AND " + TaskTable.TASK_TYPE + "=" + type.value();
			}
			cursor = query(TaskTable.TABLE_NAME, null, where, null, null, null,
					TaskTable.TASK_STATE);
		} catch (Exception e) {
			return null;
		}

		if (cursor == null) {
			return null;
		}
		if (cursor.getCount() == 0 || !cursor.moveToFirst()) {
			cursor.close();
			return null;
		}

		List<Task> tasks = new ArrayList<NetSyncTaskDbHelper.Task>();
		try {
			do {
				Task task = new Task(cursor);
				tasks.add(task);
			} while (cursor.moveToNext());
		} finally {
			cursor.close();
		}
		// 排序
		Collections.sort(tasks, new TaskComparator());
		return tasks;
	}

	/**
	 * 更新任务状态
	 *
	 * @param task
	 * @param state
	 */
	public int updateTaskState(Task task, TaskState state) {
		if (task == null) {
			return 0;
		}

		ContentValues cv = taskToContentValues(null, task);
		if (state != null) {
			cv.put(TaskTable.TASK_STATE, state.value());
		}

		String where = TaskTable.TASK_TYPE + "=?" + " AND "
				+ TaskTable.TASK_OBJECT + "=?";
		
		List<String> whereArgs = new ArrayList<String>();
		whereArgs.add(String.valueOf(task.taskType.value()));
		whereArgs.add(String.valueOf(task.taskObject.value()));
		
		if (task.taskObject == TaskObject.APP) {
			String packageName = task.extraInfo == null ? null : String.valueOf(task.extraInfo[1]);
			if (packageName != null) {
				where += " AND " + TaskTable.EXTRA_DATA4 + "=?";
				whereArgs.add(packageName);
			}
		} else if (task.taskObject == TaskObject.IMAGE) {
			String imagePath = task.paths == null ? null : String.valueOf(task.paths[0]);
			if (imagePath != null) {
				where += " AND " + TaskTable.EXTRA_DATA1 + "=?";
				whereArgs.add(imagePath);
			}
		}
		return update(TaskTable.TABLE_NAME, cv, where, whereArgs.toArray(new String[whereArgs.size()]));
	}

	private ContentValues taskToContentValues(ContentValues cv, Task task) {
		if (task == null) {
			return null;
		}

		if (cv == null) {
			cv = new ContentValues();
		} else {
			cv.clear();
		}

		cv.put(TaskTable.TASK_OBJECT, task.taskObject.value());
		cv.put(TaskTable.TASK_STATE, task.taskState.value());
		cv.put(TaskTable.TASK_TYPE, task.taskType.value());
		if (task.taskObject == TaskObject.APP) {
			cv.put(TaskTable.EXTRA_DATA1, task.paths == null ? null : task.paths[0]); // apk 文件名
			cv.put(TaskTable.EXTRA_DATA2, task.paths != null && task.paths.length > 1
					? task.paths[1]
					: null); // data文件名
			cv.put(TaskTable.EXTRA_DATA3,
					task.extraInfo == null ? null : String.valueOf(task.extraInfo[0])); // app 名字
			cv.put(TaskTable.EXTRA_DATA4,
					task.extraInfo == null ? null : String.valueOf(task.extraInfo[1])); // app 包名
			cv.put(TaskTable.EXTRA_DATA5,
					task.extraInfo == null ? null : String.valueOf(task.extraInfo[2])); //app类型
		} else if (task.taskObject == TaskObject.IMAGE) {
			cv.put(TaskTable.EXTRA_DATA1, task.paths == null ? null : task.paths[0]); // 照片的文件路径名
			cv.put(TaskTable.EXTRA_DATA2, task.paths == null ? null : task.paths.length > 1
					? task.paths[1]
					: null); // 照片缩略图的文件路径名
			cv.put(TaskTable.EXTRA_DATA3,
					task.extraInfo == null ? null : String.valueOf(task.extraInfo[0])); // 照片名
			cv.put(TaskTable.EXTRA_DATA4,
					task.extraInfo == null ? null : String.valueOf(task.extraInfo[1])); // 大小
			cv.put(TaskTable.EXTRA_DATA5,
					task.extraInfo == null ? null : String.valueOf(task.extraInfo[2])); //照片文件夹路径
		} else if (task.taskObject == TaskObject.LAUNCHER_DATA) {
			cv.put(TaskTable.EXTRA_DATA1, task.paths == null ? null : task.paths[0]); // 系统桌面数据描述文件
			cv.put(TaskTable.EXTRA_DATA2, task.paths == null ? null : task.paths[1]); // 系统桌面数据widget文件
			cv.put(TaskTable.EXTRA_DATA3, task.paths == null ? null : task.paths[2]); // 系统桌面数据data文件
			cv.put(TaskTable.EXTRA_DATA4, task.extraInfo == null
					? null
					: (String) (task.extraInfo[0])); // 应用包名
		} else if (task.taskObject == TaskObject.GOLAUNCHER_SETTING) {
			cv.put(TaskTable.EXTRA_DATA1, task.paths == null ? null : task.paths[0]); // GO桌面设置描述文件
			cv.put(TaskTable.EXTRA_DATA2, task.paths == null ? null : task.paths[1]); // GO桌面设置数据库我呢见
		} else if (task.taskObject == TaskObject.RINGTONE) {
			// TODO
			String datas[] = { TaskTable.EXTRA_DATA1, TaskTable.EXTRA_DATA2, TaskTable.EXTRA_DATA3,
					TaskTable.EXTRA_DATA4, TaskTable.EXTRA_DATA5, TaskTable.EXTRA_DATA6,
					TaskTable.EXTRA_DATA7, TaskTable.EXTRA_DATA8 };
			if (task.paths != null) {
				int count = task.paths.length;
				if (count > 0 && count <= datas.length) {
					int position = 0;
					for (String data : datas) {
						if (position < count) {
							cv.put(data, task.paths == null ? null : task.paths[position]);
						}
						position++;
					}
				}
			}
		} else if (task.taskObject == TaskObject.CONTACTS) {
			cv.put(TaskTable.EXTRA_DATA1, task.paths == null ? null : task.paths[0]);
			cv.put(TaskTable.EXTRA_DATA2, task.paths != null && task.paths.length >= 2
					? task.paths[1]
					: null);
			cv.put(TaskTable.EXTRA_DATA4,
					task.extraInfo == null ? null : String.valueOf(task.extraInfo[0]));
		} else if (task.taskObject == TaskObject.WIFI) {
			cv.put(TaskTable.EXTRA_DATA1, task.paths == null ? null : task.paths[0]);
			cv.put(TaskTable.EXTRA_DATA3,
					task.extraInfo == null ? null : String.valueOf(task.extraInfo[1]));
			cv.put(TaskTable.EXTRA_DATA4,
					task.extraInfo == null ? null : String.valueOf(task.extraInfo[0]));
		} else {
			cv.put(TaskTable.EXTRA_DATA1, task.paths == null ? null : task.paths[0]);
			cv.put(TaskTable.EXTRA_DATA4,
					task.extraInfo == null ? null : String.valueOf(task.extraInfo[0]));
		}
		return cv;
	}

	/**
	 * 更新任务 以taskType和taskObject作为主键
	 *
	 * @param task
	 */
	public void updateTask(Task task) {
		updateTaskState(task, null);
	}

	/**
	 * 从数据库中删除任务对象相符的某种类型的任务
	 *
	 * @param type
	 * @param object
	 * @return
	 */
	public boolean deleteTask(TaskType type, TaskObject object) {
		if (type == null || object == null) {
			return false;
		}

		String where = TaskTable.TASK_TYPE + "=" + type.value() + " AND " + TaskTable.TASK_OBJECT
				+ "=" + object.value();
		return delete(TaskTable.TABLE_NAME, where, null) > 0;
	}

	public boolean deleteTask(Task task) {
		if (task == null) {
			return false;
		}

		String where = TaskTable.TASK_TYPE + "=? AND "
				+ TaskTable.TASK_OBJECT + "=?";
		List<String> whereArgs = new ArrayList<String>();
		whereArgs.add(String.valueOf(task.taskType.value()));
		whereArgs.add(String.valueOf(task.taskObject.value()));
		
		if (task.taskObject == TaskObject.APP) {
			String packageName = task.extraInfo == null ? null : task.extraInfo[1].toString();
			if (packageName != null) {
				where += " AND " + TaskTable.EXTRA_DATA1 + "=?";
				whereArgs.add(packageName);
			}
		}
		return delete(TaskTable.TABLE_NAME, where, whereArgs.toArray(new String[whereArgs.size()])) > 0;
	}

	/**
	 * 将Task对象加入数据库
	 *
	 * @param task
	 * @return
	 */
	public boolean addTask(Task task) {
		if (task == null) {
			return false;
		}

		ContentValues cv = taskToContentValues(null, task);
		String columnHack = null;
		if (task.taskObject == TaskObject.APP) {
			columnHack = TaskTable.EXTRA_DATA4;
		} else {
			columnHack = TaskTable.TASK_OBJECT;
		}
		return insert(TaskTable.TABLE_NAME, columnHack, cv) > 0;
	}

	private synchronized long insert(String table, String nullColumnHack, ContentValues values) {
		SQLiteDatabase db = null;
		try {
			db = getWritableDatabase();
		} catch (SQLiteException e) {
			e.printStackTrace();
		}
		if (db == null) {
			return -1;
		}
		return db.insert(table, nullColumnHack, values);
	}

	private synchronized int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
		SQLiteDatabase db = null;
		try {
			db = getWritableDatabase();
		} catch (SQLiteException e) {
			e.printStackTrace();
		}

		if (db == null) {
			return 0;
		}
		return db.update(table, values, whereClause, whereArgs);
	}

	private synchronized int delete(String table, String whereClause, String[] whereArgs) {
		SQLiteDatabase db = null;
		try {
			db = getWritableDatabase();
		} catch (SQLiteException e) {
			e.printStackTrace();
		}

		if (db == null) {
			return 0;
		}
		return db.delete(table, whereClause, whereArgs);
	}

	private synchronized Cursor query(String table, String[] columns, String selection, String[] selectionArgs,
			String groupBy, String having, String orderBy) {
		SQLiteDatabase db = getWritableDatabase();
		if (db == null) {
			return null;
		}
		return db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
	}

	public static TaskObject entryTypeToTaskObject(EntryType entryType) {
		if (entryType == null) {
			return null;
		}
		return TaskObject.valueOf(entryType);
	}

	/**
	 * 任务对象
	 *
	 * @author maiyongshen
	 */
	public enum TaskObject {
		SMS(1),
		MMS(2),
		CONTACTS(3),
		CALLLOG(4),
		USER_DICTIONARY(5),
		WIFI(6),
		GOLAUNCHER_SETTING(7),
		LAUNCHER_DATA(8),
		APP(9),
		RINGTONE(10),
		WALLPAPER(11),
		UPLOAD_DB(12),
		CALENDAR(13),
		BOOKMARK(14),
		IMAGE(15);

		private final int mType;
		private static TaskObject values[] = { null, SMS, MMS, CONTACTS, CALLLOG, USER_DICTIONARY,
				WIFI, GOLAUNCHER_SETTING, LAUNCHER_DATA, APP, RINGTONE, WALLPAPER, UPLOAD_DB,
				CALENDAR, BOOKMARK, IMAGE };

		private TaskObject(int type) {
			this.mType = type;
		}

		public int value() {
			return mType;
		}

		public static TaskObject valueOf(int type) {
			return values[type];
		}

		public static TaskObject valueOf(EntryType type) {
			switch (type) {
				case TYPE_USER_SMS :
					return SMS;
				case TYPE_USER_MMS :
					return MMS;
				case TYPE_USER_CONTACTS :
					return CONTACTS;
				case TYPE_USER_CALL_HISTORY :
					return CALLLOG;
				case TYPE_USER_DICTIONARY :
					return USER_DICTIONARY;
				case TYPE_SYSTEM_WIFI :
					return WIFI;
				case TYPE_USER_GOLAUNCHER_SETTING :
					return GOLAUNCHER_SETTING;
				case TYPE_SYSTEM_LAUNCHER_DATA :
					return LAUNCHER_DATA;
				case TYPE_USER_APP :
				case TYPE_SYSTEM_APP :
					return APP;
				case TYPE_SYSTEM_RINGTONE :
					return RINGTONE;
				case TYPE_SYSTEM_WALLPAPER :
					return WALLPAPER;
				case TYPE_USER_CALENDAR :
					return CALENDAR;
				case TYPE_USER_BOOKMARK :
					return BOOKMARK;
				case TYPE_USER_IMAGE :
					return IMAGE;
				default :
					throw new IllegalArgumentException("invalid EntryType " + type != null
							? type.name()
							: null);
			}
		}

		public static EntryType toEntryType(TaskObject taskObject) {
			switch (taskObject) {
				case SMS :
					return EntryType.TYPE_USER_SMS;
				case MMS :
					return EntryType.TYPE_USER_MMS;
				case CONTACTS :
					return EntryType.TYPE_USER_CONTACTS;
				case CALLLOG :
					return EntryType.TYPE_USER_CALL_HISTORY;
				case USER_DICTIONARY :
					return EntryType.TYPE_USER_DICTIONARY;
				case GOLAUNCHER_SETTING :
					return EntryType.TYPE_USER_GOLAUNCHER_SETTING;
				case LAUNCHER_DATA :
					return EntryType.TYPE_SYSTEM_LAUNCHER_DATA;
				case RINGTONE :
					return EntryType.TYPE_SYSTEM_RINGTONE;
				case WIFI :
					return EntryType.TYPE_SYSTEM_WIFI;
				case APP :
					// TODO 没有区分系统程序和用户程序
					return EntryType.TYPE_USER_APP;
				case WALLPAPER :
					return EntryType.TYPE_SYSTEM_WALLPAPER;
				case CALENDAR :
					return EntryType.TYPE_USER_CALENDAR;
				case BOOKMARK :
					return EntryType.TYPE_USER_BOOKMARK;
				case IMAGE :
					return EntryType.TYPE_USER_IMAGE;
				default :
					break;
			}
			return null;
		}
	}

	/**
	 * 任务状态
	 *
	 * @author maiyongshen
	 */
	public enum TaskState {
		NOT_START(1),
		FINISHED(2),
		FAILED(3),
		CANCELED(4);

		private static TaskState values[] = { null, NOT_START, FINISHED, FAILED, CANCELED };
		private final int mState;

		private TaskState(int state) {
			this.mState = state;
		}

		public int value() {
			return mState;
		}

		public static TaskState valueOf(int state) {
			return values[state];
		}
	}

	/**
	 * @author maiyongshen
	 */
	public enum TaskType {
		ONLINE_BACKUP(1),
		ONLINE_RESTORE(2);

		private static TaskType values[] = { null, ONLINE_BACKUP, ONLINE_RESTORE };
		private int mType;

		private TaskType(int type) {
			this.mType = type;
		}

		public int value() {
			return mType;
		}

		public static TaskType valueOf(int type) {
			return values[type];
		}
	}

	/**
	 * @author maiyongshen
	 */
	public static class Task {
		public TaskType taskType;
		public TaskObject taskObject;
		public TaskState taskState;
		public Date time;
		public String[] paths;
		public Object[] extraInfo;

		public Task() {

		}

		public Task(Cursor cursor) {
			initWithCursor(cursor);
		}

		private void initWithCursor(Cursor cursor) {
			if (cursor == null) {
				return;
			}
			try {
				taskType = TaskType.valueOf(cursor.getInt(cursor
						.getColumnIndex(TaskTable.TASK_TYPE)));
				taskObject = TaskObject.valueOf(cursor.getInt(cursor
						.getColumnIndex(TaskTable.TASK_OBJECT)));
				taskState = TaskState.valueOf(cursor.getInt(cursor
						.getColumnIndex(TaskTable.TASK_STATE)));
				String strTime = cursor.getString(cursor.getColumnIndex(TaskTable.TIME));
				if (strTime != null) {
					Calendar calendar = Calendar.getInstance();
					calendar.setTimeInMillis(Long.valueOf(strTime));
					time = calendar.getTime();
				}
				if (taskObject == TaskObject.APP) {
					List<String> pathList = new ArrayList<String>();
					// apk文件路径
					String apkFileName = cursor.getString(cursor
							.getColumnIndex(TaskTable.EXTRA_DATA1));
					if (!TextUtils.isEmpty(apkFileName)) {
						pathList.add(apkFileName);
					}
					// data文件路径
					String dataFileName = cursor.getString(cursor
							.getColumnIndex(TaskTable.EXTRA_DATA2));
					if (!TextUtils.isEmpty(dataFileName)) {
						pathList.add(dataFileName);
					}
					if (pathList.size() > 0) {
						paths = pathList.toArray(new String[pathList.size()]);
					}
					extraInfo = new Object[] {
							// 应用名称
							cursor.getString(cursor.getColumnIndex(TaskTable.EXTRA_DATA3)),
							// 应用包名
							cursor.getString(cursor.getColumnIndex(TaskTable.EXTRA_DATA4)),
							// 应用类型
							cursor.getString(cursor.getColumnIndex(TaskTable.EXTRA_DATA5)) };
				} else if (taskObject == TaskObject.IMAGE) {
					paths = new String[] {
					// 照片文件路径
					cursor.getString(cursor.getColumnIndex(TaskTable.EXTRA_DATA1)) };
					extraInfo = new Object[] {
							// 照片名称
							cursor.getString(cursor.getColumnIndex(TaskTable.EXTRA_DATA3)),
							// 照片大小
							cursor.getString(cursor.getColumnIndex(TaskTable.EXTRA_DATA4)),
							// 照片父路径
							cursor.getString(cursor.getColumnIndex(TaskTable.EXTRA_DATA5)) };
				} else if (taskObject == TaskObject.LAUNCHER_DATA) {
					paths = new String[] {
							// 系统桌面数据描述文件
							cursor.getString(cursor.getColumnIndex(TaskTable.EXTRA_DATA1)),
							// 系统桌面数据widget文件
							cursor.getString(cursor.getColumnIndex(TaskTable.EXTRA_DATA2)),
							// 系统桌面数据data文件
							cursor.getString(cursor.getColumnIndex(TaskTable.EXTRA_DATA3)) };
					extraInfo = new Object[] {
					// 应用包名
					cursor.getString(cursor.getColumnIndex(TaskTable.EXTRA_DATA4)) };
				} else if (taskObject == TaskObject.GOLAUNCHER_SETTING) {
					paths = new String[] {
							// GO桌面设置描述文件
							cursor.getString(cursor.getColumnIndex(TaskTable.EXTRA_DATA1)),
							// GO桌面设置数据库文件
							cursor.getString(cursor.getColumnIndex(TaskTable.EXTRA_DATA2)) };
				} else if (taskObject == TaskObject.RINGTONE) {
					// TODO
					List<String> pathList = new ArrayList<String>();
					int columnData1 = cursor.getColumnIndex(TaskTable.EXTRA_DATA1);
					int columnData8 = cursor.getColumnIndex(TaskTable.EXTRA_DATA8);
					for (int i = columnData1; i <= columnData8; i++) {
						String path = cursor.getString(i);
						if (!TextUtils.isEmpty(path) && !pathList.contains(path)) {
							pathList.add(path);
						}
					}
					if (pathList.size() != 0) {
						paths = pathList.toArray(new String[pathList.size()]);
					}
				} else if (taskObject == TaskObject.CONTACTS) {
					List<String> pathList = new ArrayList<String>();
					// 联系人信息文件
					String dataFile = cursor
							.getString(cursor.getColumnIndex(TaskTable.EXTRA_DATA1));
					if (!TextUtils.isEmpty(dataFile)) {
						pathList.add(dataFile);
					}
					// 联系人高清头像文件夹
					String photoDir = cursor
							.getString(cursor.getColumnIndex(TaskTable.EXTRA_DATA2));
					if (!TextUtils.isEmpty(photoDir)) {
						pathList.add(photoDir);
					}
					if (pathList.size() > 0) {
						paths = pathList.toArray(new String[pathList.size()]);
					}
				} else if (taskObject == TaskObject.WIFI) {
					paths = new String[] {
					// 备份项文件
					cursor.getString(cursor.getColumnIndex(TaskTable.EXTRA_DATA1)) };
					extraInfo = new Object[] {
							cursor.getInt(cursor.getColumnIndex(TaskTable.EXTRA_DATA4)),
							// wifi路径
							cursor.getString(cursor.getColumnIndex(TaskTable.EXTRA_DATA3)) };
				} else {
					paths = new String[] {
					// 备份项文件
					cursor.getString(cursor.getColumnIndex(TaskTable.EXTRA_DATA1)) };
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 任务 表示一项备份或者恢复项
	 */
	public interface TaskTable extends BaseColumns {
		public static final String TABLE_NAME = "TaskTable";
		public static final String TASK_TYPE = "task_type";
		public static final String TASK_OBJECT = "task_object";
		public static final String TASK_STATE = "task_state";
		public static final String TIME = "time";
		public static final String EXTRA_DATA1 = "data1";
		public static final String EXTRA_DATA2 = "data2";
		// APP 应用程序名称
		public static final String EXTRA_DATA3 = "data3";
		// APP 应用程序包名
		// 短信、联系人等 存放条数
		public static final String EXTRA_DATA4 = "data4";
		public static final String EXTRA_DATA5 = "data5";
		public static final String EXTRA_DATA6 = "data6";
		public static final String EXTRA_DATA7 = "data7";
		public static final String EXTRA_DATA8 = "data8";

		public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "(" + _ID
				+ " INTEGER PRIMARY KEY," + TASK_TYPE + " INTEGER," + TASK_OBJECT + " INTEGER,"
				+ TASK_STATE + " INTEGER," + TIME + " INTEGER," + EXTRA_DATA1 + " TEXT,"
				+ EXTRA_DATA2 + " TEXT," + EXTRA_DATA3 + " TEXT," + EXTRA_DATA4 + " TEXT,"
				+ EXTRA_DATA5 + " TEXT," + EXTRA_DATA6 + " TEXT," + EXTRA_DATA7 + " TEXT,"
				+ EXTRA_DATA8 + " TEXT" + ")";
	}

	// 备份项的展示优先级，数字越小，优先级越高
	private static final int PRIORITY_SMS = 1;
	private static final int PRIORITY_MMS = 2;
	private static final int PRIORITY_CONTACTS = 3;
	private static final int PRIORITY_CALLLOG = 4;
	private static final int PRIORITY_GO_LAUNCHER_SETTING = 5;
	private static final int PRIORITY_BOOKMARK = 6;
	private static final int PRIORITY_USER_DICTIONARY = 7;
	private static final int PRIORITY_CALENDAR = 8;
	private static final int PRIORITY_RINGTONE = 9;
	private static final int PRIORITY_WALLPAPER = 10;
	private static final int PRIORITY_LAUNCHER_DATA = 11;
	private static final int PRIORITY_WIFI = 12;
	private static final int PRIORITY_USER_APP = 13;
	private static final int PRIORITY_IMAGE = 14;
	private static final int PRIORITY_TASK_DATABASE = 20;

	/**
	 * @author maiyongshen
	 */
	private class TaskComparator implements Comparator<Task> {
		@Override
		public int compare(Task lhs, Task rhs) {
			return getTaskPriority(lhs) - getTaskPriority(rhs);
		}

		private int getTaskPriority(Task task) {
			if (task == null) {
				return 0;
			}
			switch (task.taskObject) {
				case SMS :
					return PRIORITY_SMS;
				case MMS :
					return PRIORITY_MMS;
				case CONTACTS :
					return PRIORITY_CONTACTS;
				case CALLLOG :
					return PRIORITY_CALLLOG;
				case USER_DICTIONARY :
					return PRIORITY_USER_DICTIONARY;
				case GOLAUNCHER_SETTING :
					return PRIORITY_GO_LAUNCHER_SETTING;
				case RINGTONE :
					return PRIORITY_RINGTONE;
				case WALLPAPER :
					return PRIORITY_WALLPAPER;
				case LAUNCHER_DATA :
					return PRIORITY_LAUNCHER_DATA;
				case WIFI :
					return PRIORITY_WIFI;
				case APP :
					return PRIORITY_USER_APP;
				case UPLOAD_DB :
					return PRIORITY_TASK_DATABASE;
				case CALENDAR :
					return PRIORITY_CALENDAR;
				case BOOKMARK :
					return PRIORITY_BOOKMARK;
				case IMAGE :
					return PRIORITY_IMAGE;
				default :
					return 0;
			}
		}
	}
}
