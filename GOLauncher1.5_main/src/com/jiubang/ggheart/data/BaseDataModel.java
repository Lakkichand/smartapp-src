package com.jiubang.ggheart.data;

import android.content.Context;

import com.jiubang.ggheart.data.PersistenceManager.IAsyncPersistenceCallback;

/**
 * 
 * <br>类描述: 数据管理器基类
 * <br>功能详细描述: 所有数据管理器必须继承此类
 * 
 * @author  yangguanxiang
 * @date  [2012-12-27]
 */
public abstract class BaseDataModel {
	protected PersistenceManager mManager;
	protected Context mContext;
	public BaseDataModel(Context context, String dbName) {
		mManager = PersistenceManager.getInstance(context, dbName);
		mContext = context;
	}

	public synchronized void beginTransaction() {
		mManager.beginTransaction();
	}

	public synchronized void setTransactionSuccessful() {
		mManager.setTransactionSuccessful();
	}

	public synchronized void endTransaction() throws DatabaseException {
		mManager.endTransaction(false, null);
	}

	public synchronized void endTransactionAsync(IAsyncPersistenceCallback callback) {
		try {
			mManager.endTransaction(true, callback);
		} catch (DatabaseException e) {
			//do nothing
		}
	}
}
