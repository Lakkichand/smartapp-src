package com.jiubang.go.backup.pro.net.sync;

import com.google.api.services.drive.model.File;

/**
 * 
 * @author ReyZhang
 *
 */
public interface GoogleDriveFileListener {
	public void onFileExecuteSuccess(File file);

	public void onFileExecuteFail(String errMsg);
}
