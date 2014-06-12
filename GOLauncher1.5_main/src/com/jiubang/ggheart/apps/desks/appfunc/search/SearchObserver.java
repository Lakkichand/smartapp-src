// Copyright 2010 Google Inc. All Rights Reserved.

package com.jiubang.ggheart.apps.desks.appfunc.search;

import java.util.List;

import android.content.Context;
import android.os.Handler;


/**
 * An interface for observing changes related to purchases. The main application
 * extends this class and registers an instance of that derived class with
 * {@link ResponseHandler}. The main application implements the callbacks
 * {@link #onBillingSupported(boolean)} and
 * {@link #onPurchaseStateChange(PurchaseState, String, int, long)}. These
 * methods are used to update the UI.
 */
public abstract class SearchObserver {
	private static final String TAG = "SearchObserver";
	private final Context mContext;
	private final Handler mHandler;

	public SearchObserver(Context context, Handler handler) {
		mContext = context;
		mHandler = handler;
	}

	public abstract void onSearchSupported(boolean supported);

	public void onSearchStart(String searchKey, int type, int requestTimes) {

	};

	public abstract void onSearchFinsh(String searchKey, List<?> list, int type, int resultCount,
			int currentPage);

	public void onSearchException(String searchKey, Object obj, int type) {

	};

	public abstract void onHistoryChange(List<?> list, int historyType, boolean hasHistory,
			boolean isNotifly);

	public abstract void onSearchWithoutData(String searchKey, int type);
}
