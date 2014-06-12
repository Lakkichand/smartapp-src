package com.jiubang.ggheart.components.facebook;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import android.content.Context;

import com.gau.utils.cache.utils.ZipFilesUtils;
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
public class RestoreStringOperator implements IHttpOperator {
	private final static int BYTE_SIZE = 1024;
	
	@Override
	public IResponse operateHttpResponse(THttpRequest request, HttpResponse response)
			throws IllegalStateException, IOException {
		FacebookResponse facebookResponse = new FacebookResponse();
		String fbid = GoFacebookUtil.getUserInfo().getId();
		if (fbid == null) {
			facebookResponse.setResponseType(FacebookResponse.RESPONSE_TYPE_NODATA);
			return facebookResponse;
		}

		HttpEntity entity = response.getEntity();
		InputStream is = entity.getContent();
		DataInput daipt = new DataInputStream(is);
		int funid = daipt.readInt();
		int length = daipt.readInt();
		if (length == 0) {
			GoFacebookUtil.log("RestoreStringOperator length=0");
			facebookResponse.setResponseType(FacebookResponse.RESPONSE_TYPE_NODATA);
			return facebookResponse;
		}

		long recordtime = -1;
		String path = LauncherEnv.Path.SDCARD + LauncherEnv.Path.LAUNCHER_FACEBOOK_DIR + "/" + fbid
				+ "_download";
		PreferencesManager sp = new PreferencesManager(GoLauncher.getContext(),
				IPreferencesIds.FACEBOOK_RECORD, Context.MODE_PRIVATE);
		File file = new File(path);
		if (file != null && file.exists()) {
			recordtime = sp.getLong(fbid, -1);
		}

		//时间戳
		long time = daipt.readLong();
		if (time == recordtime) {
			GoFacebookUtil.log("备份time相等");
			facebookResponse.setResponseType(FacebookResponse.RESPONSE_TYPE_LOCALISNEWEST);
			return facebookResponse;
		}

		final int filelength = daipt.readInt();

		//读取、存文件
		byte[] bytes = new byte[filelength];
		daipt.readFully(bytes);
		String fileName = fbid + "_downloadzip";
		String filePath = LauncherEnv.Path.SDCARD + LauncherEnv.Path.FACEBOOK_DIR + fileName;
		FileUtil.createFile(filePath, true);
		File outputFile = new File(filePath);
		FileOutputStream fos = new FileOutputStream(outputFile);
		fos.write(bytes);
		fos.close();

		downloadSuccess();

		// 写时间戳
		sp.putLong(GoFacebookUtil.getUserInfo().getId(), time);
		sp.commit();

		GoFacebookUtil.log("RestoreStringOperator funid=" + funid + " filelength=" + filelength
				+ " length=" + length + " time=" + time);

		return facebookResponse;
	}

	private boolean downloadSuccess() {
		String fbid = GoFacebookUtil.getUserInfo().getId();
		if (fbid == null) {
			return false;
		}

		String fileName = fbid + "_downloadzip";
		String filePath = LauncherEnv.Path.SDCARD + LauncherEnv.Path.FACEBOOK_DIR + fileName;
		File zipFile = new File(filePath);

		filePath = LauncherEnv.Path.SDCARD + LauncherEnv.Path.FACEBOOK_DIR;
		try {
			// 解压下载文件到facebook文件夹,应该是一个id文件夹
			ZipFilesUtils.upZipFile(zipFile, filePath);
			// 删除下载文件
			zipFile.delete();

			File renameFile = new File(filePath + fbid + "_download");
			if (renameFile != null && renameFile.exists() && renameFile.isDirectory()) {
				//　如果id_download文件夹存在,则先删除
				FileUtil.deleteDirectory(filePath + fbid + "_download");
			}

			filePath = LauncherEnv.Path.SDCARD + LauncherEnv.Path.FACEBOOK_DIR + fbid;
			File file = new File(filePath);

			// 把id文件夹重命名为id_download文件夹
			file.renameTo(renameFile);
			
			// 删除id文件夹
			FileUtil.deleteDirectory(filePath);
			
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return false;
	}
}
