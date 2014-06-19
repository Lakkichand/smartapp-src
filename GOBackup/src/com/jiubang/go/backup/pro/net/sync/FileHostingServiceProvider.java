package com.jiubang.go.backup.pro.net.sync;

import java.io.File;

import android.content.Context;

/**
 * 文件服务
 *
 * @author maiyongshen
 */
public interface FileHostingServiceProvider {
	// 网络IO错误，通常是因为网络中断
	public static final int NETWORK_IO_ERROR = 0x00ffff01;
	// 文件过大，网盘服务无法支持
	public static final int FILE_OVERSIZE_ERROR = 0x00ffff02;
	// 文件传输未完成
	public static final int FILE_IMCOMPLETE_ERROR = 0x00ffff03;
	// 用户网盘空间不足
	public static final int INSUFFICIENT_STORAGE_ERROR = 0x00ffff04;
	// 响应解析出错
	public static final int RESPONSE_PARSE_ERROR = 0x00ffff05;
	// 与SSL相关的错误
	public static final int SSL_RELATED_ERROR = 0x00ffff06;
	// 未连接网盘服务
	public static final int SERVICE_UNLINKED_ERROR = 0x00ffff07;
	// AccessToken有误或失效，需重新验证
	public static final int SERVER_UNAUTHORIZED_ERROR = 0x00ffff08;
	// 禁止执行本操作，通常是因为AppKey失效，或无权限进行本操作
	public static final int SERVER_FORBIDEN_ERROR = 0x00ffff09;
	// 文件资源或路径不存在
	public static final int SERVER_RESOURCE_NOT_FOUND_ERROR = 0x00ffff0a;
	// 服务器暂时不可用
	public static final int SERVER_UNAVALIABLE_ERROR = 0x00ffff0b;
	// 本地文件不存在
	public static final int LOCAL_FILE_NOT_EXIST_ERROR = 0x00ffff0c;
	// 空目录
	public static final int EMPTY_DIRECTORY_ERROR = 0x00ffff0d;
	// 服务器不接受此项操作
	public static final int SERVER_NOT_ACCEPTABLE_ERROR = 0x00ffff0e;
	// 本地空间不足
	public static final int INSUFFICIENT_LOCAL_STORAGE_ERROR = 0x00ffff0f;
	// 文件IO出错
	public static final int FILE_IO_ERROR = 0x00ffff10;
	// 响应超时
	public static final int SERVER_TIME_OUT_ERROR = 0x00ffff11;
	// 无此用户
	public static final int NO_USER_INFO = 0x00ffff12;
	// 错误参数
	public static final int ILLEGAL_ARGUMENT = 0x00ffff13;
	// 未知错误
	public static final int UNKNOWN_ERROR = 0x00ffffff;

	public static final String GOBACKUP_CONTENT_DIR = "BackupContents";
	public static final String GOBACKUP_READ_ME_FILE = "readme.txt";

	// 无效类型
	public static final int INVALID_SERVICE = -1;
	// Dropbox
	public static final int DROPBOX = 1;
	// GoogleDrive
	public static final int GOOGLE_DRIVE = 2;

	public void startAuthentication(Context context);

	public void finishAuthentication(Context context, final ActionListener listener);

	public boolean isSessionValid();

	public void saveSession(Context context, AccountInfo accountInfo);

	public void restoreSession(Context context);

	public void createFolder(String path, final ActionListener listener);

	public CancelableTask uploadFile(File file, String destPath, boolean overwrite,
			final ActionListener listener);

	public CancelableTask downloadFile(String path, File dir, Object revision,
			final ActionListener listener);

	public void deleteFile(final String path, final ActionListener listener);

	public void getFileInfo(String path, ActionListener listener);

	public OnlineFileInfo getFileInfo(String path) throws FileHostingServiceException;

	public void logout(Context context);

	public void clearStoredToken(Context context);

	public AccountInfo getAccount(final ActionListener listener);

	public String getServiceProviderName();

	public String getRooPath();

	public String getOnlineBackupPath();

	public int getType();
	
	public void release();

	/**
	 * 行动监听
	 *
	 * @author maiyongshen
	 */
	public static interface ActionListener {
		public void onProgress(long progress, long total, Object data);

		public void onComplete(Object data);

		public void onError(int errCode, String errMessage, Object data);

		public void onCancel(Object data);
	}

	/**
	 * 文件服务Exception
	 *
	 * @author maiyongshen
	 */
	public static class FileHostingServiceException extends Exception {
		private int mErrorCode;

		public FileHostingServiceException() {
			super();
		}

		public FileHostingServiceException(int errCode, String errMessage) {
			super(errMessage);
			mErrorCode = errCode;
		}
		
		public int getErrorCode() {
			return mErrorCode;
		}
	}
	
	/**
	 * @author maiyongshen
	 *
	 */
	public static class ResourceNotFoundException extends FileHostingServiceException {
		public ResourceNotFoundException() {
			super(SERVER_RESOURCE_NOT_FOUND_ERROR, "cannot find the file");
		}
	}
	
	/**
	 * @author maiyongshen
	 *
	 */
	public static class NetworkIOException extends FileHostingServiceException {
		public NetworkIOException() {
			super(NETWORK_IO_ERROR, "network io error");
		}
	}
}