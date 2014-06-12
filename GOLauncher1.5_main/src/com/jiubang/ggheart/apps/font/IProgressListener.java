package com.jiubang.ggheart.apps.font;

public interface IProgressListener {
	public void onStart(Object listenEntity);

	public void onProgress(Object listenEntity, Object progressPrama);

	public void onFinish(Object listenEntity);

	public void onCancel(Object listenEntity);
}
