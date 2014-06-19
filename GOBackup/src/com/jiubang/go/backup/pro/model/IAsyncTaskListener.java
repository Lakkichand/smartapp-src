package com.jiubang.go.backup.pro.model;

/**
 * @author maiyongshen
 */
public interface IAsyncTaskListener {
	public void onStart(Object arg1, Object arg2);

	public void onProceeding(Object progress, Object arg2, Object arg3, Object arg4);

	public void onEnd(boolean success, Object arg1, Object arg2);
}
