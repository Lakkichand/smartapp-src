package com.zhidian.wifibox.util;

import java.io.File;

import android.content.Intent;
import android.net.Uri;

public class IntentUtils {
	
	/**
	 * 打开文件关联应用的选择框
	 * 
	 * @param file
	 * @return
	 */
	public static Intent createFileOpenIntent(File file)
	{
		Intent intent = new Intent(Intent.ACTION_VIEW);		
		intent.setDataAndType(Uri.fromFile(file), FileUtil.getFileMimeType(file));
		return intent;
	}

}
