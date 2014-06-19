package com.jiubang.go.backup.pro.net.sync;

import java.util.Map;

import com.google.api.services.drive.model.File;

/**
 * 
 * @author ReyZhang
 *
 */
public interface ScanGoogleDriveListener {
	public void onScanSuccess(Map<String, File> list);

	public void onScanFail();
}
