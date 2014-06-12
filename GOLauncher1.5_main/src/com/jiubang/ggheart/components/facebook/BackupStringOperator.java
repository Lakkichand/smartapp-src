package com.jiubang.ggheart.components.facebook;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import android.app.Activity;
import android.content.Context;

import com.gau.utils.net.operator.IHttpOperator;
import com.gau.utils.net.request.THttpRequest;
import com.gau.utils.net.response.IResponse;
import com.go.util.file.FileUtil;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 
 * <br>类描述: 解析NET请求返回的数据转换成字符串
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-9-5]
 */
public class BackupStringOperator implements IHttpOperator {
	private final static int BYTE_SIZE = 1024;
	private Activity mActivity;
	
	BackupStringOperator(Activity activity) {
		mActivity = activity;
	}

	@Override
	public IResponse operateHttpResponse(THttpRequest request, HttpResponse response)
			throws IllegalStateException, IOException {
		FacebookResponse facebookResponse = new FacebookResponse();
		facebookResponse.setResponseType(facebookResponse.RESPONSE_TYPE_NODATA);
		
		HttpEntity entity = response.getEntity();
		InputStream is = entity.getContent();
		DataInput daipt = new DataInputStream(is);
		int funid = daipt.readInt();
		int length = daipt.readInt();
		if (length == 0) {
			GoFacebookUtil.log("BackupStringOperator length=0");
			return facebookResponse;
		}
		
		// 写时间戳
		long time = daipt.readLong();
		PreferencesManager sp = new PreferencesManager(GoLauncher.getContext(),
				IPreferencesIds.FACEBOOK_RECORD, Context.MODE_PRIVATE);
		sp.putLong(GoFacebookUtil.getUserInfo().getId(), time);
		sp.commit();
		
		GoFacebookUtil.log("BackupStringOperator funid=" + funid + " length=" + length + " time=" + time);
		
		backupSuccess();
		
		facebookResponse.setResponseType(facebookResponse.RESPONSE_TYPE_NORMAL);
		return facebookResponse;
	}
	
	private void backupSuccess() {
		String fbid = GoFacebookUtil.getUserInfo().getId();
		if (fbid == null) {
			return;
		}
		
		//删除打包文件
		String fileName = fbid + "_localzip";
		String filePath = LauncherEnv.Path.SDCARD + LauncherEnv.Path.FACEBOOK_DIR + fileName;
		File file = new File(filePath);
		if (file.exists()) {
			file.delete();
		}
		
		//把本地备份文件夹转成下载成功备份文件夹
		filePath = LauncherEnv.Path.SDCARD + LauncherEnv.Path.FACEBOOK_DIR + fbid;
		file = new File(filePath);
		File renameFile = new File(filePath + "_download");
		if (renameFile != null && renameFile.exists() && renameFile.isDirectory()) {
			FileUtil.deleteDirectory(filePath + "_download");
		}
		if (file != null && file.exists()) {
			file.renameTo(renameFile);
			if (file.isDirectory()) {
				// 删除id文件夹
				FileUtil.deleteDirectory(filePath);
			}
		}
		
		//写入最近备份时间
		PreferencesManager sp = new PreferencesManager(GoLauncher.getContext(),
				IPreferencesIds.FACEBOOK_RECORD, Context.MODE_PRIVATE);
		long localtime = System.currentTimeMillis();
		sp.putLong(IPreferencesIds.FACEBOOK_LAST_BACKUP_TIME, localtime);
		sp.commit();
	}
}
