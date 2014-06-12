package com.jiubang.ggheart.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.jiubang.ggheart.appgame.base.database.AppGameDataBaseHelper;
import com.jiubang.ggheart.apps.gowidget.gostore.ThreadPoolManager;
import com.jiubang.ggheart.data.statistics.StatisticsDataBaseHelper;

/**
 * 
 * <br>类描述: 持久化管理者
 * <br>功能详细描述: 负责桌面持久化管理，包括异步和同步持久化机制
 * 
 * @author  yangguanxiang
 * @date  [2012-12-14]
 */
public class PersistenceManager {
	private static HashMap<String, PersistenceManager> sInstancePool = new HashMap<String, PersistenceManager>();
	public static final String DB_ANDROID_HEART = "androidheart.db";
	public static final String DB_APPGAME_CENTER = "appgamecenter.db";
	public static final String DB_LAUNCHERS = "launchers.db";
	public final static String DB_APP_CLASSIFY = "appclassify.db";
	private static final String ASYNC_INSERT = "async_insert";
	private static final String ASYNC_UPDATE = "async_update";
	private static final String ASYNC_DELETE = "async_delete";
	private static final String ASYNC_TRANSACTION = "async_transaction";
	private ThreadPoolManager mThreadManager;
	private SQLiteOpenHelper mDbHelper;
	private String mDbName;
	private ConcurrentHashMap<Long, TransactionObject> mTrancMap = new ConcurrentHashMap<Long, TransactionObject>();

	private PersistenceManager(String dbName, SQLiteOpenHelper helper) {
		mDbName = dbName;
		mDbHelper = helper;
		ThreadPoolManager.buildInstance(mDbName, 1, 1, 0, TimeUnit.SECONDS);
		mThreadManager = ThreadPoolManager.getInstance(mDbName);
	}

	public synchronized static PersistenceManager getInstance(Context context, String dbName) {
		if (!sInstancePool.containsKey(dbName)) {
			if (DB_ANDROID_HEART.equals(dbName)) {
				//完全舍弃DataProvider之前，都不能单独new DatabaseHelper，必须经由DataProvider获取DatabaseHelper对象，否则DB可能被锁
				//				sInstancePool.put(dbName, new PersistenceManager(dbName, new DatabaseHelper(
				//						context, dbName, DatabaseHelper.getDB_CUR_VERSION())));
				SQLiteOpenHelper helper = DataProvider.getInstance(context).getDatabaseHelper();
				sInstancePool.put(dbName, new PersistenceManager(dbName, helper));
			} else if (DB_APPGAME_CENTER.equals(dbName)) {
				sInstancePool.put(dbName, new PersistenceManager(dbName, new AppGameDataBaseHelper(
						context)));
			} else if (DB_LAUNCHERS.equals(dbName)) {
				sInstancePool.put(dbName, new PersistenceManager(dbName,
						new StatisticsDataBaseHelper(context)));
			} else if (DB_APP_CLASSIFY.equals(dbName)) {
				sInstancePool.put(dbName, new PersistenceManager(dbName,
						new AppClassifyDatabaseHelper(context)));
			}
		}
		return sInstancePool.get(dbName);
	}

	/**
	 * <br>功能简述: 把数据库设置成MODE_WORLD_READABLE
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @return
	 */
	public boolean openDBWithWorldReadable(Context context) {
		try {
			mDbHelper.close();
			if (context.openOrCreateDatabase(mDbName, Context.MODE_WORLD_READABLE, null) == null) {
				return false;
			}
		} catch (Exception e) {
			mDbHelper.close();
			return false;
		}
		return true;
	}

	/**
	 * <br>功能简述: 返回当前数据库是否是新创建的
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public boolean isNewDB() {
		boolean isNewDB = false;
		if (DB_ANDROID_HEART.equals(mDbName)) {
			isNewDB = ((DatabaseHelper) mDbHelper).isNewDB();
		}
		return isNewDB;
	}

	/**
	 * <br>功能简述: 单表查询
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param tableName
	 * @param projection
	 * @param selection
	 * @param selectionArgs
	 * @param sortOrder
	 * @return
	 */
	public Cursor query(String tableName, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		return query(tableName, projection, selection, selectionArgs, null, null, sortOrder);
	}

	/**
	 * <br>功能简述: 单表查询
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param tableName
	 * @param projection
	 * @param selection
	 * @param selectionArgs
	 * @param groupBy
	 * @param having
	 * @param sortOrder
	 * @return
	 */
	public Cursor query(String tableName, String[] projection, String selection,
			String[] selectionArgs, String groupBy, String having, String sortOrder) {
		Cursor cur = null;
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		if (db != null) {
			cur = db.query(tableName, projection, selection, selectionArgs, groupBy, having,
					sortOrder);
		}
		return cur;
	}

	/**
	 * <br>功能简述: 插入新数据
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param tableName
	 * @param values
	 * @return
	 * @throws DatabaseException
	 */
	public synchronized long insert(final String tableName, final ContentValues values)
			throws DatabaseException {
		if (values != null) {
			long trancId = Thread.currentThread().getId();
			if (mTrancMap.containsKey(trancId)) {
				Log.i("PersistenceManager", trancId + " tranc insert " + tableName);
				mTrancMap.get(trancId).mTrancOpts.add(PersistenceOperation.newInsert(tableName,
						values));
			} else {
				Log.i("PersistenceManager", trancId + " insert " + tableName);
				SQLiteDatabase db = mDbHelper.getWritableDatabase();
				if (db != null) {
					try {
						return db.insert(tableName, null, values);
					} catch (Exception e) {
						throw new DatabaseException(e);
					}
				}
			}
		}
		return -1;
	}

	/**
	 * <br>功能简述: 异步插入新数据
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param tableName
	 * @param values
	 * @param callback
	 */
	public synchronized void insertAsync(final String tableName, final ContentValues values,
			final IAsyncPersistenceCallback callback) {
		long trancId = Thread.currentThread().getId();
		if (mTrancMap.containsKey(trancId)) {
			try {
				insert(tableName, values);
			} catch (DatabaseException e) {
				//do nothing
			}
		} else {
			Thread thread = new Thread(ASYNC_INSERT) {
				@Override
				public void run() {
					boolean success = false;
					long rowId = -1;
					DatabaseException exception = null;
					try {
						rowId = insert(tableName, values);
						success = true;
					} catch (DatabaseException e) {
						exception = e;
					}
					if (callback != null) {
						callback.callback(success, 0, rowId, exception);
					}
				}
			};
			mThreadManager.execute(thread);
		}
	}

	/**
	 * <br>功能简述: 更新数据
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param tableName
	 * @param values
	 * @param selection
	 * @param selectionArgs
	 * @return
	 * @throws DatabaseException
	 */
	public synchronized int update(final String tableName, final ContentValues values,
			final String selection, final String[] selectionArgs) throws DatabaseException {
		if (values != null) {
			long trancId = Thread.currentThread().getId();
			if (mTrancMap.containsKey(trancId)) {
				Log.i("PersistenceManager", trancId + " tranc update " + tableName);
				mTrancMap.get(trancId).mTrancOpts.add(PersistenceOperation.newUpdate(tableName,
						values, selection, selectionArgs));
			} else {
				Log.i("PersistenceManager", trancId + " update " + tableName);
				SQLiteDatabase db = mDbHelper.getWritableDatabase();
				if (db != null) {
					try {
						return db.update(tableName, values, selection, selectionArgs);
					} catch (Exception e) {
						throw new DatabaseException(e);
					}
				}
			}
		}
		return -1;
	}

	/**
	 * <br>功能简述: 异步更新数据
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param tableName
	 * @param values
	 * @param selection
	 * @param selectionArgs
	 * @param callback
	 */
	public synchronized void updateAsync(final String tableName, final ContentValues values,
			final String selection, final String[] selectionArgs,
			final IAsyncPersistenceCallback callback) {
		long trancId = Thread.currentThread().getId();
		if (mTrancMap.containsKey(trancId)) {
			try {
				update(tableName, values, selection, selectionArgs);
			} catch (DatabaseException e) {
				//do nothing
			}
		} else {
			Thread thread = new Thread(ASYNC_UPDATE) {
				@Override
				public void run() {
					boolean success = false;
					int rows = 0;
					DatabaseException exception = null;
					try {
						rows = update(tableName, values, selection, selectionArgs);
						success = true;
					} catch (DatabaseException e) {
						exception = e;
					}
					if (callback != null) {
						callback.callback(success, rows, -1, exception);
					}
				}
			};
			mThreadManager.execute(thread);
		}
	}

	/**
	 * <br>功能简述: 删除数据
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param tableName
	 * @param selection
	 * @param selectionArgs
	 * @return
	 * @throws DatabaseException
	 */
	public synchronized int delete(final String tableName, final String selection,
			final String[] selectionArgs) throws DatabaseException {
		long trancId = Thread.currentThread().getId();
		if (mTrancMap.containsKey(trancId)) {
			Log.i("PersistenceManager", trancId + " tranc delete " + tableName);
			mTrancMap.get(trancId).mTrancOpts.add(PersistenceOperation.newDelete(tableName,
					selection, selectionArgs));
		} else {
			Log.i("PersistenceManager", trancId + " delete " + tableName);
			SQLiteDatabase db = mDbHelper.getWritableDatabase();
			if (db != null) {
				try {
					return db.delete(tableName, selection, selectionArgs);
				} catch (Exception e) {
					throw new DatabaseException(e);
				}
			}
		}
		return 0;
	}

	/**
	 * <br>功能简述: 异步删除数据
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param tableName
	 * @param selection
	 * @param selectionArgs
	 * @param callback
	 */
	public synchronized void deleteAsync(final String tableName, final String selection,
			final String[] selectionArgs, final IAsyncPersistenceCallback callback) {
		long trancId = Thread.currentThread().getId();
		if (mTrancMap.containsKey(trancId)) {
			try {
				delete(tableName, selection, selectionArgs);
			} catch (DatabaseException e) {
				//do nothing
			}
		} else {
			Thread thread = new Thread(ASYNC_DELETE) {
				@Override
				public void run() {
					boolean success = false;
					int rows = 0;
					DatabaseException exception = null;
					try {
						rows = delete(tableName, selection, selectionArgs);
						success = true;
					} catch (DatabaseException e) {
						exception = e;
					}
					if (callback != null) {
						callback.callback(success, rows, -1, exception);
					}
				}
			};
			mThreadManager.execute(thread);
		}
	}

	/**
	 * <br>功能简述: 执行相应的SQL语句
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param sql
	 * @throws DatabaseException
	 */
	public synchronized void exec(final String sql) throws DatabaseException {
		long trancId = Thread.currentThread().getId();
		if (mTrancMap.containsKey(trancId)) {
			Log.i("PersistenceManager", trancId + " tranc exec sql: " + sql);
			mTrancMap.get(trancId).mTrancOpts.add(PersistenceOperation.newExec(sql));
		} else {
			Log.i("PersistenceManager", trancId + " exec sql: " + sql);
			SQLiteDatabase db = mDbHelper.getWritableDatabase();
			try {
				db.execSQL(sql);
			} catch (SQLException e) {
				throw new DatabaseException(e);
			}
		}
	}

	/**
	 * <br>功能简述: 异步执行相应的SQL语句
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param sql
	 * @param callback
	 */
	public synchronized void execAsync(final String sql, final IAsyncPersistenceCallback callback) {
		long trancId = Thread.currentThread().getId();
		if (mTrancMap.containsKey(trancId)) {
			try {
				exec(sql);
			} catch (DatabaseException e) {
				//do nothing
			}
		} else {
			Thread thread = new Thread(ASYNC_DELETE) {
				@Override
				public void run() {
					boolean success = false;
					DatabaseException exception = null;
					try {
						exec(sql);
						success = true;
					} catch (DatabaseException e) {
						exception = e;
					}
					if (callback != null) {
						callback.callback(success, 0, -1, exception);
					}
				}
			};
			mThreadManager.execute(thread);
		}
	}

	/**
	 * <br>功能简述: 执行事务操作
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param opts
	 * @return
	 * @throws DatabaseException 
	 */
	private synchronized boolean applyTransaction(final ArrayList<PersistenceOperation> opts)
			throws DatabaseException {
		boolean success = false;
		if (opts != null && !opts.isEmpty()) {
			SQLiteDatabase db = mDbHelper.getWritableDatabase();
			if (db != null) {
				Log.i("PersistenceManager", Thread.currentThread().getId() + ": applyTransaction begin");
				try {
					db.beginTransaction();
					for (PersistenceOperation opt : opts) {
						switch (opt.mOpt) {
							case PersistenceOperation.OPT_INSERT :
								db.insert(opt.mTableName, null, opt.mValues);
								break;
							case PersistenceOperation.OPT_UPDATE :
								db.update(opt.mTableName, opt.mValues, opt.mSelection,
										opt.mSelectionArgs);
								break;
							case PersistenceOperation.OPT_DELETE :
								db.delete(opt.mTableName, opt.mSelection, opt.mSelectionArgs);
								break;
							case PersistenceOperation.OPT_EXEC :
								db.execSQL(opt.mSql);
								break;
						}
					}
					db.setTransactionSuccessful();
					success = true;
				} catch (Exception e) {
					throw new DatabaseException(e);
				} finally {
					try {
						db.endTransaction();
					} catch (Exception e) {
						throw new DatabaseException(e);
					}
					Log.i("PersistenceManager", Thread.currentThread().getId() + ": applyTransaction end");
				}
			}
		}
		return success;
	}

	/**
	 * <br>功能简述: 异步执行事务操作
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param opts
	 * @param callback
	 */
	private synchronized void applyTransactionAsync(final ArrayList<PersistenceOperation> opts,
			final IAsyncPersistenceCallback callback) {
		Thread thread = new Thread(ASYNC_TRANSACTION) {
			@Override
			public void run() {
				boolean success = false;
				DatabaseException exception = null;
				try {
					success = applyTransaction(opts);
				} catch (DatabaseException e) {
					exception = e;
				}
				if (callback != null) {
					callback.callback(success, 0, -1, exception);
				}
			}
		};
		mThreadManager.execute(thread);
	}

	/**
	 * <br>功能简述: 开启事务
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public synchronized void beginTransaction() {
		long trancId = Thread.currentThread().getId();
		if (!mTrancMap.containsKey(trancId)) {
			TransactionObject trancObj = new TransactionObject(trancId);
			mTrancMap.put(trancObj.mTrancId, trancObj);
		} else {
			throw new IllegalStateException("A transaction " + trancId
					+ " was begun. Not allow another transaction to inject");
		}
	}

	/**
	 * <br>功能简述: 设置事务成功
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public synchronized void setTransactionSuccessful() {
		long trancId = Thread.currentThread().getId();
		if (mTrancMap.containsKey(trancId)) {
			mTrancMap.get(trancId).mTrancSuccess = true;
		}
	}

	/**
	 * <br>功能简述: 结束事务，务必放在finally块内进行
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param async
	 * @param callback
	 * @throws DatabaseException 
	 */
	public synchronized void endTransaction(boolean async, IAsyncPersistenceCallback callback)
			throws DatabaseException {
		long trancId = Thread.currentThread().getId();
		if (mTrancMap.containsKey(trancId)) {
			TransactionObject trancObj = mTrancMap.get(trancId);
			try {			
				if (trancObj.mTrancSuccess && !trancObj.mTrancOpts.isEmpty()) {
					ArrayList<PersistenceOperation> opts = new ArrayList<PersistenceOperation>(
							trancObj.mTrancOpts);
					if (async) {
						applyTransactionAsync(opts, callback);
					} else {
						applyTransaction(opts);
					}
				}
			} finally {
				trancObj.mTrancId = -1;
				trancObj.mTrancOpts.clear();
				trancObj.mTrancSuccess = false;
				mTrancMap.remove(trancId);
			}
		}
	}
	/**
	 * 
	 * <br>类描述: 异步持久化回调接口
	 * <br>功能详细描述: 每次异步操作执行后都会回调该接口的callback方法
	 * 
	 * @author  yangguanxiang
	 * @date  [2012-12-14]
	 */
	public static interface IAsyncPersistenceCallback {
		public void callback(boolean success, int rowsAffected, long rowId,
				DatabaseException exception);
	}

	/**
	 * 
	 * <br>类描述: 持久化行为封装类
	 * <br>功能详细描述: 执行事务操作时使用，每一步对数据库操作的数据都封装到该类中
	 * 
	 * @author  yangguanxiang
	 * @date  [2012-12-14]
	 */
	public static class PersistenceOperation {
		private static final int OPT_INSERT = 0;
		private static final int OPT_UPDATE = 1;
		private static final int OPT_DELETE = 2;
		private static final int OPT_EXEC = 3;

		private int mOpt = -1;

		private String mTableName;

		private ContentValues mValues;

		private String mSelection;

		private String[] mSelectionArgs;

		private String mSql;

		private PersistenceOperation(int opt) {
			mOpt = opt;
		}

		public static PersistenceOperation newInsert(String tableName, ContentValues values) {
			PersistenceOperation opt = new PersistenceOperation(OPT_INSERT);
			opt.mTableName = tableName;
			opt.mValues = values;
			return opt;
		}

		public static PersistenceOperation newUpdate(String tableName, ContentValues values,
				String selection, String[] selectionArgs) {
			PersistenceOperation opt = new PersistenceOperation(OPT_UPDATE);
			opt.mTableName = tableName;
			opt.mValues = values;
			opt.mSelection = selection;
			opt.mSelectionArgs = selectionArgs;
			return opt;
		}

		public static PersistenceOperation newDelete(String tableName, String selection,
				String[] selectionArgs) {
			PersistenceOperation opt = new PersistenceOperation(OPT_DELETE);
			opt.mTableName = tableName;
			opt.mSelection = selection;
			opt.mSelectionArgs = selectionArgs;
			return opt;
		}

		public static PersistenceOperation newExec(String sql) {
			PersistenceOperation opt = new PersistenceOperation(OPT_EXEC);
			opt.mSql = sql;
			return opt;
		}
	}

	/**
	 * 
	 * <br>类描述: 事务信息封装类
	 * <br>功能详细描述:
	 * 
	 * @author  yangguanxiang
	 * @date  [2012-12-27]
	 */
	private class TransactionObject {
		public long mTrancId = -1;
		public boolean mTrancSuccess;
		public ArrayList<PersistenceOperation> mTrancOpts = new ArrayList<PersistenceOperation>();

		public TransactionObject(long trancId) {
			mTrancId = trancId;
		}

	}
}
