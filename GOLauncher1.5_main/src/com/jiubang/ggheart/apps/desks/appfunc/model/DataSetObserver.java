package com.jiubang.ggheart.apps.desks.appfunc.model;

import android.database.Cursor;

import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants.MessageID;

public abstract class DataSetObserver {
	public void onChanged() {
		// Do nothing
	}

	/**
	 * This method is called when the entire data becomes invalid, most likely
	 * through a call to {@link Cursor#deactivate()} or {@link Cursor#close()}
	 * on a {@link Cursor}.
	 */
	public void onInvalidated() {
		// Do nothing
	}

	public void onNotify(MessageID msgId, Object obj1, Object obj2) {
		// Do nothing
	}
}
