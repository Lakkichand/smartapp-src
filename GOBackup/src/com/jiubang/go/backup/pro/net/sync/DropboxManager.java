package com.jiubang.go.backup.pro.net.sync;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.http.conn.HttpHostConnectException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.DropboxInputStream;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.DropboxAPI.UploadRequest;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxFileSizeException;
import com.dropbox.client2.exception.DropboxIOException;
import com.dropbox.client2.exception.DropboxLocalStorageFullException;
import com.dropbox.client2.exception.DropboxParseException;
import com.dropbox.client2.exception.DropboxPartialFileException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.exception.DropboxUnlinkedException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session;
import com.dropbox.client2.session.Session.AccessType;
import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.PreferenceManager;
import com.jiubang.go.backup.pro.util.Logger;
import com.jiubang.go.backup.pro.util.Util;

/**
 * Dropbox 服务提供者，封装dropbox API实现
 *
 * @author maiyongshen
 * 注：Dropbox允许上传的单个文件最大大小为180MB
 */
public class DropboxManager implements FileHostingServiceProvider {
	private static final AccessType ACCESS_TYPE = AccessType.APP_FOLDER;
	private static final String DROPBOX_ACCOUNT_PREFS = "dropbox_account_prefs";
	private static final String PREF_ACCESS_KEY = "pref_access_key";
	private static final String PREF_ACCESS_SECRET = "pref_access_secret";
	private static final String PREF_ACCOUNT_NAME = "pref_account_name";
	private static final String PREF_ACCOUNT_UID = "pref_account_uid";

	private static final String APP_KEY = "17svniiuypo1gkj";
	private static final String APP_SECRET = "c06mglrlvsx5ypf";

	private static final int MAX_RETRY_COUNT = 3;

	private static final int MAX_WAIT_TIME = 30 * 1000;

	private Context mContext;
	private DropboxAPI<AndroidAuthSession> mDropboxAPI;
	private String mAccessKey;
	private String mAccessSecret;
	private AccountInfo mDropboxAccount;

	private static DropboxManager sInstance = null;

	public DropboxManager(Context context) {
		if (context == null) {
			throw new IllegalArgumentException("context cannot be null!");
		}
		mContext = context.getApplicationContext();
		mDropboxAPI = new DropboxAPI<AndroidAuthSession>((AndroidAuthSession) buildSession());
	}

	//	public synchronized static DropboxManager getInstance(Context context) {
	//		if (sInstance == null) {
	//			sInstance = new DropboxManager(context);
	//		}
	//		return sInstance;
	//	}

	private Session buildSession() {
		AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
		restoreSession(mContext);
		if (!TextUtils.isEmpty(mAccessKey) && !TextUtils.isEmpty(mAccessSecret)) {
			AccessTokenPair accessTokenPair = new AccessTokenPair(mAccessKey, mAccessSecret);
			return new AndroidAuthSession(appKeyPair, ACCESS_TYPE, accessTokenPair);
		}
		return new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
	}

	@Override
	public void startAuthentication(Context context) {
		mDropboxAPI.getSession().startAuthentication(context);
	}

	@Override
	public void finishAuthentication(Context context, final ActionListener listener) {
		final String serviceName = context.getString(R.string.dropbox);
		AndroidAuthSession session = mDropboxAPI.getSession();
		String ret = null;
		if (!session.authenticationSuccessful()) {
			if (listener != null) {
				listener.onError(FileHostingServiceProvider.SERVER_UNAVALIABLE_ERROR,
						context.getString(R.string.login_failed, serviceName), null);
			}
			return;
		}
		try {
			ret = session.finishAuthentication();
		} catch (IllegalStateException e) {
			e.printStackTrace();
			if (listener != null) {
				listener.onError(FileHostingServiceProvider.SERVER_UNAVALIABLE_ERROR,
						context.getString(R.string.login_failed, serviceName), null);
			}
			return;
			// TODO 出现异常如何处理 重新登录？
		}
		if (TextUtils.isEmpty(ret)) {
			Toast.makeText(mContext, R.string.login_failed, Toast.LENGTH_LONG).show();
			if (listener != null) {
				listener.onError(FileHostingServiceProvider.SERVER_UNAVALIABLE_ERROR,
						context.getString(R.string.login_failed, serviceName), null);
			}
			return;
		}

		AccessTokenPair accessToken = session.getAccessTokenPair();
		mAccessKey = accessToken.key;
		mAccessSecret = accessToken.secret;

		// step2 start asyncTask to get the AccountInfo

		getAccount(new ActionListener() {

			@Override
			public void onProgress(long progress, long total, Object data) {
			}

			@Override
			public void onError(int errCode, String errMessage, Object data) {
				if (listener != null) {
					listener.onError(errCode, errMessage, data);
				}
			}

			@Override
			public void onComplete(Object data) {
				mDropboxAccount = (AccountInfo) data;
				saveSession(mContext, mDropboxAccount);
				if (listener != null) {
					listener.onComplete(mDropboxAccount);
				}
			}

			@Override
			public void onCancel(Object data) {
			}
		});
	}

	@Override
	public boolean isSessionValid() {
		return mAccessKey != null && mAccessSecret != null && isAccountValid(mDropboxAccount);
	}

	@Override
	public void saveSession(Context context, AccountInfo accountInfo) {
		long id = 0;
		if (accountInfo == null) {
			return;
		}
		id = (Long) accountInfo.getUID();
		SharedPreferences prefs = context.getSharedPreferences(DROPBOX_ACCOUNT_PREFS,
				Context.MODE_PRIVATE);
		Editor edit = prefs.edit();
		edit.putString(PREF_ACCESS_KEY + "_" + id, mAccessKey);
		edit.putString(PREF_ACCESS_SECRET + "_" + id, mAccessSecret);
		edit.putString(PREF_ACCOUNT_NAME + "_" + id, accountInfo.getDisplayName());
		edit.putLong(PREF_ACCOUNT_UID + "_" + id, id);
		edit.commit();

		PreferenceManager pm = PreferenceManager.getInstance();
		pm.putInt(context, PreferenceManager.KEY_NETWORK_BACKUP_TYPE,
				FileHostingServiceProvider.DROPBOX);
		pm.putLong(context, PreferenceManager.KEY_NETWORK_BACKUP_LOGIN_ID, id);
	}

	@Override
	public void restoreSession(Context context) {
		PreferenceManager pm = PreferenceManager.getInstance();
		long id = pm.getLong(context, PreferenceManager.KEY_NETWORK_BACKUP_LOGIN_ID, 0);
		// 存取两份preference文件 1.作为dropbox账号文件，2.作为默认登录文件
		SharedPreferences prefs = context.getSharedPreferences(DROPBOX_ACCOUNT_PREFS,
				Context.MODE_PRIVATE);
		mAccessKey = prefs.getString(PREF_ACCESS_KEY + "_" + id, null);
		mAccessSecret = prefs.getString(PREF_ACCESS_SECRET + "_" + id, null);
		long uid = prefs.getLong(PREF_ACCOUNT_UID + "_" + id, -1);
		String name = prefs.getString(PREF_ACCOUNT_NAME + "_" + id, null);
		mDropboxAccount = new DropboxAccount(uid, name);
	}

	@Override
	public AccountInfo getAccount(final ActionListener listener) {
		if (isAccountValid(mDropboxAccount)) {
			return mDropboxAccount;
		}
		Executors.newFixedThreadPool(1).execute(
				new GetAccountInfoTask<AccountInfo>(listener, mGetAccountCallable));
		return null;
	}

	@Override
	public void release() {

	}

	private boolean isAccountValid(AccountInfo account) {
		return account != null && ((Long) account.getUID()) > 0 && account.getDisplayName() != null;
	}

	@Override
	public void createFolder(final String path, final ActionListener listener) {
		new Thread(new Runnable() {
			int mErrorCode = 0;
			String mErrorMsg;
			OnlineFileInfo mDropboxFile = null;

			@Override
			public void run() {
				try {
					mDropboxFile = new DropboxFile(mDropboxAPI.createFolder(path), mDropboxAPI);
				} catch (DropboxUnlinkedException e) {
					e.printStackTrace();
					mErrorCode = SERVICE_UNLINKED_ERROR;
					mErrorMsg = "This app wasn't authenticated properly.";
				} catch (DropboxIOException e) {
					e.printStackTrace();
					mErrorCode = NETWORK_IO_ERROR;
					mErrorMsg = "Network error";
				} catch (DropboxParseException e) {
					e.printStackTrace();
					mErrorCode = RESPONSE_PARSE_ERROR;
					mErrorMsg = "Dropbox error";
				} catch (DropboxServerException e) {
					e.printStackTrace();
					if (e.error == DropboxServerException._403_FORBIDDEN) {
						mErrorCode = SERVER_FORBIDEN_ERROR;
					} else if (e.error == DropboxServerException._404_NOT_FOUND) {
						mErrorCode = SERVER_RESOURCE_NOT_FOUND_ERROR;
					} else if (e.error == DropboxServerException._507_INSUFFICIENT_STORAGE) {
						mErrorCode = INSUFFICIENT_STORAGE_ERROR;
					} else {
					}
					mErrorMsg = e.body.userError;
					if (mErrorMsg == null) {
						mErrorMsg = e.body.error;
					}
				} catch (DropboxException e) {
					e.printStackTrace();
					mErrorCode = UNKNOWN_ERROR;
					mErrorMsg = "unknown error.";
				} catch (Exception e) {
					e.printStackTrace();
					mErrorCode = UNKNOWN_ERROR;
					mErrorMsg = "unknown error.";
				}
				if (listener == null) {
					return;
				}
				if (mErrorCode != 0) {
					listener.onError(mErrorCode, mErrorMsg, mDropboxFile);
					return;
				}
				listener.onComplete(mDropboxFile);
			}
		}).start();
	}

	@Override
	public CancelableTask uploadFile(File file, String destPath, boolean overwrite,
			final ActionListener listener) {
		// TODO 是否允许多个文件同时上传
		FileUploadTask task = new FileUploadTask(file, destPath, overwrite, listener);
		task.execute();
		return task;
	}

	@Override
	public CancelableTask downloadFile(String path, File dir, Object revision,
			final ActionListener listener) {
		// TODO 目前的实现没有考虑revision
		FileDownloadTask task = new FileDownloadTask(path, dir, listener);
		task.execute();
		return task;
	}

	@Override
	public void deleteFile(final String path, final ActionListener listener) {
		new Thread(new Runnable() {
			int mErrorCode = 0;
			String mErrorMsg;

			@Override
			public void run() {
				try {
					mDropboxAPI.delete(path);
				} catch (DropboxUnlinkedException e) {
					e.printStackTrace();
					mErrorCode = SERVICE_UNLINKED_ERROR;
					mErrorMsg = "This app wasn't authenticated properly.";
				} catch (DropboxIOException e) {
					e.printStackTrace();
					mErrorCode = NETWORK_IO_ERROR;
					mErrorMsg = "Network error.";
				} catch (DropboxParseException e) {
					e.printStackTrace();
					mErrorCode = RESPONSE_PARSE_ERROR;
					mErrorMsg = "Dropbox error.";
				} catch (DropboxServerException e) {
					e.printStackTrace();
					if (e.error == DropboxServerException._404_NOT_FOUND) {
						mErrorCode = SERVER_RESOURCE_NOT_FOUND_ERROR;
					} else {
						mErrorCode = UNKNOWN_ERROR;
					}
					mErrorMsg = e.body.userError;
					if (mErrorMsg == null) {
						mErrorMsg = e.body.error;
					}
				} catch (DropboxException e) {
					e.printStackTrace();
					mErrorCode = UNKNOWN_ERROR;
					mErrorMsg = "unknown error.";
				} catch (Exception e) {
					e.printStackTrace();
					mErrorCode = UNKNOWN_ERROR;
					mErrorMsg = "unknown error.";
				}
				if (listener == null) {
					return;
				}
				if (mErrorCode != 0) {
					listener.onError(mErrorCode, mErrorMsg, null);
					return;
				}
				listener.onComplete(null);
			}
		}).start();
	}

	@Override
	public void logout(Context context) {
		mDropboxAPI.getSession().unlink();
		clearStoredToken(context);
		release();
	}

	@Override
	public void clearStoredToken(Context context) {
		// 根据ID清除相应的账号

		if (mDropboxAccount == null) {
			return;
		}
		long id = (Long) mDropboxAccount.getUID();
		SharedPreferences prefs = context.getSharedPreferences(DROPBOX_ACCOUNT_PREFS,
				Context.MODE_PRIVATE);
		Editor edit = prefs.edit();
		edit.remove(PREF_ACCESS_KEY + "_" + id);
		edit.remove(PREF_ACCESS_SECRET + "_" + id);
		edit.remove(PREF_ACCOUNT_NAME + "_" + id);
		edit.remove(PREF_ACCOUNT_UID + "_" + id);
		edit.commit();
		mAccessKey = null;
		mAccessSecret = null;

		mDropboxAccount = null;

		// 清除cookies
		Util.clearCookies(mContext);
	}

	@Override
	public String getServiceProviderName() {
		return "dropbox";
	}

	@Override
	public String getRooPath() {
		return File.separator;
	}

	@Override
	public String getOnlineBackupPath() {
		return File.separator + FileHostingServiceProvider.GOBACKUP_CONTENT_DIR;
	}

	@Override
	public void getFileInfo(String path, ActionListener listener) {
		new Thread(new GetFileInfoRunnable(path, listener)).start();
	}

	@Override
	public OnlineFileInfo getFileInfo(String path) {
		if (path == null) {
			return null;
		}
		try {
			Entry fileEntry = mDropboxAPI.metadata(path, 0, null, true, null);
			return new DropboxFile(fileEntry, mDropboxAPI);
		} catch (Exception e) {
		}
		return null;
	}

	@Override
	public int getType() {
		return DROPBOX;
	}

	/**
	 * GetFileInfo interface
	 */
	private class GetFileInfoRunnable implements Runnable {
		private int mErrorCode;
		private String mErrorMsg;
		private OnlineFileInfo mFileInfo;
		private String mPath;
		private ActionListener mListener;

		public GetFileInfoRunnable(String path, ActionListener listener) {
			if (TextUtils.isEmpty(path)) {
				StringBuilder sb = new StringBuilder();
				sb.append("GetFileInfoRunnable : ");
				sb.append("path = " + path);
				sb.append("\n");
				Logger.e("DropboxManager", sb.toString());
				Logger.flush();
				throw new IllegalArgumentException("invalid path!");
			}
			mPath = path;
			mListener = listener;
		}

		@Override
		public void run() {
			try {
				Entry fileEntry = mDropboxAPI.metadata(mPath, 0, null, true, null);
				mFileInfo = new DropboxFile(fileEntry, mDropboxAPI);
			} catch (DropboxUnlinkedException e) {
				mErrorCode = SERVICE_UNLINKED_ERROR;
				mErrorMsg = "This app wasn't authenticated properly.";
			} catch (DropboxServerException e) {
				if (e.error == DropboxServerException._304_NOT_MODIFIED) {
					mErrorCode = UNKNOWN_ERROR;
				} else if (e.error == DropboxServerException._404_NOT_FOUND) {
					mErrorCode = SERVER_RESOURCE_NOT_FOUND_ERROR;
				} else if (e.error == DropboxServerException._406_NOT_ACCEPTABLE) {
					mErrorCode = SERVER_NOT_ACCEPTABLE_ERROR;
				} else {
					mErrorCode = UNKNOWN_ERROR;
				}
				mErrorMsg = e.body.userError;
				if (mErrorMsg == null) {
					mErrorMsg = e.body.error;
				}
			} catch (DropboxIOException e) {
				mErrorCode = NETWORK_IO_ERROR;
				mErrorMsg = "Network error.";
			} catch (DropboxException e) {
				mErrorCode = UNKNOWN_ERROR;
				mErrorMsg = "unknown error.";
			} catch (Exception e) {
				e.printStackTrace();
				mErrorCode = UNKNOWN_ERROR;
				mErrorMsg = "unknown error.";
			} finally {
				if (mListener == null) {
					return;
				}
				if (mFileInfo != null) {
					if (!mFileInfo.exist()) {
						mListener.onError(SERVER_RESOURCE_NOT_FOUND_ERROR, null, null);
					} else {
						mListener.onComplete(mFileInfo);
					}
				} else {
					mListener.onError(mErrorCode, mErrorMsg, null);
				}
			}
		}
	}

	/**
	 * FileUploadTask
	 *
	 * @author maiyongshen
	 */
	private class FileUploadTask extends AsyncTask<Void, Long, Boolean> implements CancelableTask {
		private File mFile;
		private String mPath;
		private boolean mOverwriteFlag;
		private UploadRequest mRequest;
		private ActionListener mListener;
		private static final int PROGRESS_UPDATE_INTERVAL = 500;
		private ProgressListener mProgressListener = new ProgressListener() {
			@Override
			public void onProgress(long bytes, long total) {
				publishProgress(bytes);
			}

			@Override
			public long progressInterval() {
				return PROGRESS_UPDATE_INTERVAL;
			}
		};
		private int mErrorCode;
		private String mErrorMsg;
		private OnlineFileInfo mUploadedFileInfo;
		private boolean mCanceled = false;
		private int mRetryCount;

		public FileUploadTask(File file, String path, boolean overwrite, ActionListener listener) {
			if (file == null || TextUtils.isEmpty(path)) {
				StringBuilder sb = new StringBuilder();
				sb.append("FileUploadTask : ");
				sb.append("path = " + path + ", ");
				sb.append("file = " + file);
				sb.append("\n");
				Logger.e("DropboxManager", sb.toString());
				Logger.flush();
				throw new IllegalArgumentException("invalid argument");
			}
			mFile = file;
			mPath = path;
			mOverwriteFlag = overwrite;
			mListener = listener;
			mRetryCount = 0;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				// TODO 暂不支持将整个文件夹上传
				if (mFile.exists() && mFile.isDirectory()) {
					throw new IllegalArgumentException("unsupport uploading whole directory!");
				}
				String fullPath = new File(mPath, mFile.getName()).getAbsolutePath();
				Entry fileEntry = uploadFile(fullPath, mFile);
				if (fileEntry != null) {
					mUploadedFileInfo = new DropboxFile(fileEntry, mDropboxAPI);
					return true;
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				mErrorCode = LOCAL_FILE_NOT_EXIST_ERROR;
				mErrorMsg = mFile.getAbsolutePath() + " not exists.";
			} catch (DropboxUnlinkedException e) {
				e.printStackTrace();
				mErrorCode = SERVICE_UNLINKED_ERROR;
				mErrorMsg = "This app wasn't authenticated properly.";
			} catch (DropboxFileSizeException e) {
				e.printStackTrace();
				mErrorCode = FILE_OVERSIZE_ERROR;
				mErrorMsg = "This file is too big to upload.";
			} catch (DropboxPartialFileException e) {
				e.printStackTrace();
			} catch (DropboxIOException e) {
				e.printStackTrace();
				mErrorCode = NETWORK_IO_ERROR;
				mErrorMsg = "Network error.";
			} catch (DropboxParseException e) {
				e.printStackTrace();
				mErrorCode = RESPONSE_PARSE_ERROR;
				mErrorMsg = "Dropbox error.";
			} catch (DropboxServerException e) {
				e.printStackTrace();
				mErrorCode = UNKNOWN_ERROR;
				if (e.error == DropboxServerException._401_UNAUTHORIZED) {
					mErrorCode = SERVER_UNAUTHORIZED_ERROR;
				} else if (e.error == DropboxServerException._403_FORBIDDEN) {
					mErrorCode = SERVER_FORBIDEN_ERROR;
				} else if (e.error == DropboxServerException._404_NOT_FOUND) {
					mErrorCode = SERVER_RESOURCE_NOT_FOUND_ERROR;
				} else if (e.error == DropboxServerException._507_INSUFFICIENT_STORAGE) {
					mErrorCode = INSUFFICIENT_STORAGE_ERROR;
				} else {
				}
				// This gets the Dropbox error, translated into the user's
				// language
				mErrorMsg = e.body.userError;
				if (mErrorMsg == null) {
					mErrorMsg = e.body.error;
				}
			} catch (DropboxException e) {
				e.printStackTrace();
				mErrorCode = UNKNOWN_ERROR;
				mErrorMsg = "unknown error.";
			} catch (HttpHostConnectException e) {
				e.printStackTrace();
				mErrorCode = SERVER_UNAVALIABLE_ERROR;
				mErrorMsg = "Network error.";
			} catch (Exception e) {
				e.printStackTrace();
				mErrorCode = UNKNOWN_ERROR;
				mErrorMsg = "unknown error.";
			}
			return false;
		}

		// 上传文件出现网络异常时重连三次，其他异常直接抛出
		private Entry uploadFile(String path, File file) throws DropboxException, IOException {
			Entry fileEntry = null;
			while (mRetryCount <= MAX_RETRY_COUNT) {
				try {
					FileInputStream fis = new FileInputStream(file);
					try {
						if (mOverwriteFlag) {
							mRequest = mDropboxAPI.putFileOverwriteRequest(path, fis,
									mFile.length(), mProgressListener);
						} else {
							mRequest = mDropboxAPI.putFileRequest(path, fis, mFile.length(), null,
									mProgressListener);
						}
						if (mRequest != null) {
							fileEntry = mRequest.upload();
						}
						if (fileEntry != null) {
							break;
						}
					} catch (DropboxIOException e) {
						e.printStackTrace();
						mRetryCount++;
						if (mRetryCount > MAX_RETRY_COUNT) {
							throw e;
						}
					} finally {
						fis.close();
					}
				} catch (IOException e) {
					throw e;
				}
			}
			return fileEntry;
		}

		@Override
		protected void onProgressUpdate(Long... progress) {
			if (mListener != null) {
				final long fileSize = mFile.length();
				// int percent = (int) (100.0f * (double) progress[0] / fileSize
				// + 0.5f);
				mListener.onProgress(progress[0], fileSize, null);
			}
		}

		@Override
		protected void onCancelled() {
			if (mListener != null) {
				mListener.onCancel(null);
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (mListener == null) {
				return;
			}
			if (result == null || !result) {
				mListener.onError(mErrorCode, mErrorMsg, null);
			} else if (!mCanceled) {
				mListener.onComplete(mUploadedFileInfo);
			}
		}

		@Override
		public void cancel() {
			if (isCancelled() || mCanceled) {
				return;
			}
			new Thread(new Runnable() {
				@Override
				public void run() {
					if (mRequest != null) {
						mRequest.abort();
						mRequest = null;
					}
					mCanceled = true;
				}
			}).start();
			cancel(true);
		}
	}

	/**
	 * FileDownloadTask
	 *
	 * @author maiyongshen
	 */
	private class FileDownloadTask extends AsyncTask<Void, Long, Boolean> implements CancelableTask {
		private String mDropboxPath;
		private ActionListener mListener;
		private File mLocalDir;
		private int mErrorCode;
		private String mErrorMsg;
		private ProgressListener mProgressListener = new ProgressListener() {
			@Override
			public void onProgress(long bytes, long total) {
				publishProgress(bytes, total);
			}
		};
		private DropboxInputStream mDropboxInputStream;
		private OnlineFileInfo mCurrentFile;
		private boolean mCanceled = false;
		private int mRetryCount;
		private Object mLock = new byte[1];

		private int mCurFileIndex = 0;
		private int mFileCount = 0;
		private int mProgress;

		public FileDownloadTask(String dropboxPath, File localDir, ActionListener listener) {
			if (localDir == null) {
				StringBuilder sb = new StringBuilder();
				sb.append("FileUploadTask : ");
				sb.append("localDir = " + localDir);
				sb.append("\n");
				Logger.e("DropboxManager", sb.toString());
				Logger.flush();
				throw new IllegalArgumentException("invalid argument");
			}
			mDropboxPath = dropboxPath;
			mLocalDir = localDir;
			mListener = listener;
			mRetryCount = 0;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			if (!mLocalDir.exists()) {
				mLocalDir.mkdirs();
			}
			try {
				Entry fileEntry = mDropboxAPI.metadata(mDropboxPath, 0, null, true, null);
				if (fileEntry.isDir && Util.isCollectionEmpty(fileEntry.contents)) {
					mErrorCode = EMPTY_DIRECTORY_ERROR;
					mErrorMsg = "The directory is empty.";
					return false;
				}
				if (!fileEntry.isDir) {
					mCurFileIndex = 0;
					mFileCount = 1;
					mCurrentFile = buildFileInfo(fileEntry);
					downloadFile(mLocalDir, fileEntry.fileName(), mDropboxPath);
				} else {
					// 创建文件夹
					File locDir = new File(mLocalDir, fileEntry.fileName());
					if (!locDir.exists()) {
						locDir.mkdir();
					}
					mCurFileIndex = 0;
					mFileCount = fileEntry.contents.size();
					for (Entry subEntry : fileEntry.contents) {
						mCurrentFile = buildFileInfo(subEntry);
						downloadFile(locDir, subEntry.fileName(), subEntry.path);
						mCurFileIndex++;
					}
				}
				return true;
			} catch (DropboxUnlinkedException e) {
				e.printStackTrace();
				mErrorCode = SERVICE_UNLINKED_ERROR;
				mErrorMsg = "This app wasn't authenticated properly.";
			} catch (DropboxPartialFileException e) {
				e.printStackTrace();
			} catch (DropboxIOException e) {
				e.printStackTrace();
				mErrorCode = NETWORK_IO_ERROR;
				mErrorMsg = "Network error.";
			} catch (DropboxParseException e) {
				e.printStackTrace();
				mErrorCode = RESPONSE_PARSE_ERROR;
				mErrorMsg = "Dropbox error.";
			} catch (DropboxLocalStorageFullException e) {
				e.printStackTrace();
				mErrorCode = INSUFFICIENT_LOCAL_STORAGE_ERROR;
				mErrorMsg = "no space";
			} catch (HttpHostConnectException e) {
				e.printStackTrace();
				mErrorCode = SERVER_UNAVALIABLE_ERROR;
				mErrorMsg = "Network error.";
			} catch (DropboxServerException e) {
				e.printStackTrace();
				mErrorCode = UNKNOWN_ERROR;
				if (e.error == DropboxServerException._401_UNAUTHORIZED) {
					mErrorCode = SERVER_UNAUTHORIZED_ERROR;
				} else if (e.error == DropboxServerException._403_FORBIDDEN) {
					mErrorCode = SERVER_FORBIDEN_ERROR;
				} else if (e.error == DropboxServerException._404_NOT_FOUND) {
					mErrorCode = SERVER_RESOURCE_NOT_FOUND_ERROR;
				} else if (e.error == DropboxServerException._507_INSUFFICIENT_STORAGE) {
					mErrorCode = INSUFFICIENT_STORAGE_ERROR;
				} else if (e.error == DropboxServerException._406_NOT_ACCEPTABLE) {
					mErrorCode = SERVER_NOT_ACCEPTABLE_ERROR;
				} else if (e.error == DropboxServerException._304_NOT_MODIFIED) {

				} else {

				}
				// This gets the Dropbox error, translated into the user's
				// language
				mErrorMsg = e.body.userError;
				if (mErrorMsg == null) {
					mErrorMsg = e.body.error;
				}
			} catch (DropboxException e) {
				e.printStackTrace();
				mErrorMsg = "unknown error.";
				mErrorCode = UNKNOWN_ERROR;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				mErrorCode = FILE_IO_ERROR;
				mErrorMsg = null;
			} catch (Exception e) {
				e.printStackTrace();
				mErrorCode = UNKNOWN_ERROR;
				mErrorMsg = "unknown error.";
			}
			return false;
		}

		private void downloadFile(File localDir, String destFileName, String dropboxPath)
				throws DropboxException, IOException {
			mRetryCount = 0;
			while (mRetryCount <= MAX_RETRY_COUNT) {
				try {
					synchronized (mLock) {
						if (isCancelled()) {
							return;
						}
					}

					FileOutputStream fos = new FileOutputStream(new File(localDir, destFileName));
					try {
						synchronized (mLock) {
							mDropboxInputStream = mDropboxAPI.getFileStream(dropboxPath, null);
						}
						// 该方法结束后会自己关闭流
						mDropboxInputStream.copyStreamToOutput(fos, mProgressListener);
						break;
					} catch (DropboxIOException e) {
						e.printStackTrace();
						mRetryCount++;
						if (mRetryCount > MAX_RETRY_COUNT) {
							throw e;
						}
					} finally {
						fos.close();
					}
				} catch (IOException e) {
					throw e;
				}
			}
		}

		@Override
		protected void onProgressUpdate(Long... progress) {
			if (mListener != null && !isCancelled()) {
				float curFileProgress = (float) progress[0] / (float) progress[1];
				mProgress = (int) ((mCurFileIndex + curFileProgress) / mFileCount * 100);
				mListener.onProgress(mProgress, 100, mCurrentFile);
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (mListener == null) {
				return;
			}
			if (result == null || !result) {
				Log.d("GOBackup", "DropboxManager : onPostExecute : result = " + result);
				mListener.onError(mErrorCode, mErrorMsg, mCurrentFile);
			} else if (!mCanceled) {
				mListener.onComplete(mCurrentFile);
			}
		}

		@Override
		protected void onCancelled() {
			if (mListener != null) {
				mListener.onCancel(mCurrentFile);
			}
		}

		@Override
		public void cancel() {
			if (isCancelled() || mCanceled) {
				return;
			}
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						synchronized (mLock) {
							if (mDropboxInputStream != null) {
								mDropboxInputStream.close();
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						mDropboxInputStream = null;
					}
				}
			}).start();
			mCanceled = true;
			cancel(true);
		}

		private OnlineFileInfo buildFileInfo(Entry entry) {
			return new DropboxFile(entry, mDropboxAPI);
		}
	}

	/**
	 * 获取账号信息任务
	 *
	 * @author maiyongshen
	 * @param <V>
	 */
	private class GetAccountInfoTask<V> extends FutureTask<V> {
		private ActionListener mListener;
		private int mErrorCode;
		private String mErrorMsg;

		public GetAccountInfoTask(ActionListener listener, Callable<V> callable) {
			super(callable);
			mListener = listener;
		}

		@Override
		protected void done() {
			V accountInfo = null;
			try {
				accountInfo = get(MAX_WAIT_TIME, TimeUnit.MILLISECONDS);
			} catch (TimeoutException e) {
				mErrorCode = SERVER_TIME_OUT_ERROR;
				mErrorMsg = "server time out";
			} catch (ExecutionException e) {
				Throwable cause = e.getCause();
				if (cause instanceof FileHostingServiceException) {
					mErrorCode = ((FileHostingServiceException) cause).getErrorCode();
					mErrorMsg = ((FileHostingServiceException) cause).getMessage();
				}
			} catch (InterruptedException e) {
			} finally {
				postResult(accountInfo);
			}
		}

		private void postResult(V result) {
			if (mListener == null) {
				return;
			}
			if (result == null) {
				mListener.onError(mErrorCode, mErrorMsg, null);
			} else {
				mListener.onComplete(result);
			}
		}
	};

	private Callable<AccountInfo> mGetAccountCallable = new Callable<AccountInfo>() {
		@Override
		public AccountInfo call() throws Exception {
			AccountInfo accountInfo = null;
			int errorCode = 0;
			String errorMessage = null;
			try {
				accountInfo = new DropboxAccount(mDropboxAPI.accountInfo());
			} catch (DropboxUnlinkedException e) {
				errorCode = SERVICE_UNLINKED_ERROR;
				errorMessage = "This app wasn't authenticated properly.";
			} catch (DropboxIOException e) {
				errorCode = NETWORK_IO_ERROR;
				errorMessage = "Network error";
			} catch (DropboxParseException e) {
				errorCode = RESPONSE_PARSE_ERROR;
				errorMessage = "Dropbox error";
			} catch (DropboxServerException e) {
				errorCode = UNKNOWN_ERROR;
				errorMessage = "Dropbox error";
			} catch (DropboxException e) {
				errorCode = UNKNOWN_ERROR;
				errorMessage = "Unknown error";
			} catch (Exception e) {
				errorCode = UNKNOWN_ERROR;
				errorMessage = "Unknown error";
			}
			if (errorCode > 0) {
				throw new FileHostingServiceException(errorCode, errorMessage);
			}
			return accountInfo;
		}
	};

}
