package com.jiubang.go.backup.pro.net.sync;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets.Details;
import com.google.api.client.googleapis.auth.oauth2.GoogleOAuthConstants;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.googleapis.media.MediaHttpDownloaderProgressListener;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;
import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.util.LogUtil;
import com.jiubang.go.backup.pro.util.Util;

/**
 *
 * @author maiyongshen
 */
public class GoogleDriveManager implements FileHostingServiceProvider {
	public static final String REDIRECT_URI = "https://play.google.com/store/apps/details?id=com.jiubang.go.backup.ex";
	public static final String CLIENT_ID = "561771909562.apps.googleusercontent.com";
	public static final String CLIENT_SECRET = "1bGjBGSljAVwbK8m_fLj5zV2";
	public static final List<String> SCOPES = Arrays.asList(
			"https://www.googleapis.com/auth/drive",
			"https://www.googleapis.com/auth/userinfo.email",
			"https://www.googleapis.com/auth/userinfo.profile");
	public static final String EXT_MIME_TYPE_PREFIX = "application/vnd.google.drive.ext-type.";

	private static final int MAX_RETRY_TIMES = 3;

	private Credential mCredential;
	private AccountInfo mGoogleDriveAccount;
	private Drive mService;
	private Context mContext;

	private Map<String, File> mCacheFiles = new HashMap<String, File>();

	public GoogleDriveManager(Context context) {
		if (context == null) {
			throw new IllegalArgumentException("context is null");
		}
		mContext = context.getApplicationContext();
		restoreSession(mContext);
		if (isSessionValid()) {
			mService = buildService(mCredential);
		}
	}

	//	/**
	//	 * 实例化GoogleDriveManager方法，对象单例
	//	 */
	//	public synchronized static GoogleDriveManager getInstance(Context context) {
	//		if (sINSTANCE == null) {
	//			sINSTANCE = new GoogleDriveManager(context);
	//		}
	//
	//		return sINSTANCE;
	//	}

	@Override
	public void startAuthentication(Context context) {
		Intent intent = new Intent(context, GoogleDriveBrowserActivity.class);
		if (!(context instanceof Activity)) {
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		}
		
		if (mCredential != null && mCredential.getRefreshToken() != null) {
			intent.putExtra(GoogleDriveBrowserActivity.EXTRA_REFRESH_TOKEN, true);
		}
		
		context.startActivity(intent);
	}

	@Override
	public void finishAuthentication(final Context context, final ActionListener listener) {
		final String serviceName = context.getString(R.string.google_drive);
		final String refreshToken = mCredential != null ? mCredential.getRefreshToken() : null;
		final String authorizationCode = GoogleDriveBrowserActivity.sLastAuthorizationCode;
		if (TextUtils.isEmpty(refreshToken) && TextUtils.isEmpty(authorizationCode)) {
			if (listener != null) {
				listener.onError(FileHostingServiceProvider.SERVER_UNAUTHORIZED_ERROR,
						context.getString(R.string.login_failed, serviceName), null);
			}
			return;
		}
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					if (!TextUtils.isEmpty(refreshToken)) {
						mCredential = createCredentialWithRefreshToken(refreshToken);
					} else if (!TextUtils.isEmpty(authorizationCode)) {
						mCredential = createCredentialWithAuthorizationCode(GoogleDriveBrowserActivity.sLastAuthorizationCode);
					}
					
					if (mCredential != null) {
						final PreferencesCredentialStore credentialStore = new PreferencesCredentialStore(context);
						saveCredential(new PreferencesCredentialStore(context), mCredential, null);
						mService = buildService(mCredential);
						if (!isAccountValid(mGoogleDriveAccount)) {
							try {
								mGoogleDriveAccount = getAccount(mCredential);
								saveAccountInfo(credentialStore, mGoogleDriveAccount);
							} catch (FileHostingServiceException e) {
								throw new IOException();
							}
						}
					} else {
						throw new IOException();
					}
					
					listener.onComplete(mGoogleDriveAccount);
				} catch (IOException e) {
					if (listener != null) {
						listener.onError(FileHostingServiceProvider.SERVER_UNAUTHORIZED_ERROR,
								context.getString(R.string.login_failed, serviceName), null);
					}
				}
			}
		}).start();
	}

	private GoogleAuthorizationCodeFlow getFlow() {
		GoogleClientSecrets secrets = new GoogleClientSecrets();
		secrets.setWeb(new Details().setClientId(CLIENT_ID).setClientSecret(CLIENT_SECRET));
		return new GoogleAuthorizationCodeFlow.Builder(new NetHttpTransport(),
				new JacksonFactory(), secrets, SCOPES).setAccessType("offline")
				.setApprovalPrompt("force").build();
	}
	
	private Credential createCredentialWithRefreshToken(String refreshToken) throws IOException {
		GoogleTokenResponse response = new GoogleRefreshTokenRequest(
				new NetHttpTransport(), new JacksonFactory(), refreshToken,
				CLIENT_ID, CLIENT_SECRET).execute();
		Credential credential = null;
		if (response != null) {
			credential = new Credential(BearerToken
					.authorizationHeaderAccessMethod())
					.setFromTokenResponse(response);
		}
		return credential;
	}
	
	private Credential createCredentialWithAuthorizationCode(String authorizationCode) throws IOException {
		GoogleAuthorizationCodeFlow flow = getFlow();
		GoogleTokenResponse response = flow
				.newTokenRequest(authorizationCode)
				.setRedirectUri(REDIRECT_URI).execute();
		return flow.createAndStoreCredential(response, null);
	}

/*	private void redirectToAuthorizationUrl(Context context) {
		String url = getFlow().newAuthorizationUrl().setRedirectUri(REDIRECT_URI).setState("xyz")
				.build();
		Intent intent = new Intent(context, GoogleDriveBrowserActivity.class);
		intent.setData(Uri.parse(url));
		context.startActivity(intent);
	}*/

	private Drive buildService(Credential credential) {
		return new Drive.Builder(new NetHttpTransport(), new JacksonFactory(), credential).build();
	}
	
	private boolean rebuildGoogleDriveService(Context context) {
		if (mCredential == null) {
			restoreSession(context);
		}
		final String refreshToken = mCredential != null ? mCredential.getRefreshToken() : null;
		if (TextUtils.isEmpty(refreshToken)) {
			return false;
		}
		try {
			mCredential = createCredentialWithRefreshToken(refreshToken);
			saveCredential(new PreferencesCredentialStore(context), mCredential, null);
			mService = buildService(mCredential);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public boolean isSessionValid() {
		return isCredentialValid(mCredential) && isAccountValid(mGoogleDriveAccount);
	}
	
	private boolean isCredentialValid(Credential credential) {
		if (credential == null) {
			return false;
		}
		long accessTokenExpirationTime = credential.getExpirationTimeMilliseconds();
		long now = System.currentTimeMillis();
		return now < accessTokenExpirationTime;
	}

	private void saveAccountInfo(PreferencesCredentialStore store, AccountInfo accountInfo) {
		store.saveAccountInfo(accountInfo);
	}

	private void saveCredential(PreferencesCredentialStore store, Credential credential,
			String userId) {
		try {
			store.store(userId, mCredential);
		} catch (IOException e) {
		};
	}

	@Override
	public void saveSession(Context context, AccountInfo accountInfo) {
		if (accountInfo == null || mCredential == null) {
			return;
		}
		PreferencesCredentialStore store = new PreferencesCredentialStore(context);
		saveAccountInfo(store, accountInfo);
		saveCredential(store, mCredential, accountInfo.getUID().toString());
	}

	private Credential buildEmptyCredential() {
		return new Credential.Builder(BearerToken.authorizationHeaderAccessMethod())
				.setJsonFactory(new JacksonFactory())
				.setTransport(new NetHttpTransport())
				.setClientAuthentication(
						new ClientParametersAuthentication(CLIENT_ID, CLIENT_SECRET))
				.setTokenServerUrl(new GenericUrl(GoogleOAuthConstants.TOKEN_SERVER_URL))
				.setTokenServerEncodedUrl(GoogleOAuthConstants.AUTHORIZATION_SERVER_URL).build();
	}

	@Override
	public void restoreSession(Context context) {
		mCredential = buildEmptyCredential();
		PreferencesCredentialStore store = new PreferencesCredentialStore(context);
		try {
			store.load(null, mCredential);
		} catch (IOException e) {
		};
		mGoogleDriveAccount = store.loadAccountInfo();
	}

	@Override
	public void createFolder(final String path, final ActionListener listener) {
		new Thread(new Runnable() {
			@Override
			public void run() {

			}
		}).start();
	}

	private List<File> listAllFiles(Files.List request) throws IOException {
		List<File> result = new ArrayList<File>();
		do {
			FileList files = request.execute();
			final List<File> allItems = files.getItems();
			if (!Util.isCollectionEmpty(allItems)) {
				result.addAll(files.getItems());
			}
			request.setPageToken(files.getNextPageToken());
		} while (request.getPageToken() != null && request.getPageToken().length() > 0);

		return !Util.isCollectionEmpty(result) ? result : null;
	}

	public List<File> listAllChildren(String parentId) {
		if (TextUtils.isEmpty(parentId)) {
			return null;
		}
		List<File> results = new ArrayList<File>();
		try {
			Files.List request = mService.files().list();
			request.setQ("'" + parentId + "' in parents and trashed = false");
			do {
				try {
					FileList fileList = request.execute();
					final List<File> children = fileList.getItems();
					if (!Util.isCollectionEmpty(children)) {
						results.addAll(children);
					}
					request.setPageToken(fileList.getNextPageToken());
				} catch (IOException e) {
					e.printStackTrace();
					request.setPageToken(null);
				}
			} while (request.getPageToken() != null && request.getPageToken().length() > 0);
		} catch (IOException e) {
			return null;
		}
		return !Util.isCollectionEmpty(results) ? results : null;
	}

	private Files.List searchFile(Files.List request, String fileName, String parentId) {
		StringBuilder builder = new StringBuilder();
		builder.append("title = ").append("'").append(fileName).append("'").append(" and ")
				.append("trashed = false").toString();
		if (parentId != null) {
			builder.append(" and ").append("'").append(parentId).append("'").append(" in parents");
		}
		String query = builder.toString();
		return request.setQ(query);
	}

	private Files.List searchFileInFolder(Files.List request, String fileName, String parentId,
			boolean isFolder) {
		request = searchFile(request, fileName, parentId);
		StringBuilder builder = new StringBuilder(request.getQ());
		if (isFolder) {
			builder.append(" and ").append("mimeType = ")
					.append("'application/vnd.google-apps.folder'");
		} else {
			builder.append(" and ").append("mimeType != ")
					.append("'application/vnd.google-apps.folder'");
		}
		String query = builder.toString();
		return request.setQ(query);
	}

	private File createFolderSync(String folderName, String parentId) throws IOException {
		Files.List request = mService.files().list();
		List<File> result = listAllFiles(searchFileInFolder(request, folderName, parentId, true));
		if (!Util.isCollectionEmpty(result)) {
			return result.get(0);
		}
		File fileFolder = new File();
		fileFolder.setTitle(folderName);
		fileFolder.setMimeType("application/vnd.google-apps.folder");
		if (!TextUtils.isEmpty(parentId)) {
			fileFolder.setParents(Arrays.asList(new ParentReference().setId(parentId)));
		}
		Drive.Files.Insert insert;
		insert = mService.files().insert(fileFolder);
		return insert.execute();
	}

	private File createFolder(String path) {
		java.io.File file = new java.io.File(path);
		String parent = file.getParent();
		File parentFile = null;
		if (parent != null) {
			parentFile = createFolder(parent);
		}
		File createdFile = null;
		try {
			if (parentFile != null) {
				createdFile = createFolderSync(file.getName(), parentFile.getId());
			} else {
				createdFile = createFolderSync(file.getName(), null);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		if (createdFile != null) {
			putFileToCache(createdFile, file.getPath());
		}
		return createdFile;
	}
	
	private boolean checkDriveServiceValid() {
		if (mService != null) {
			return true;
		} else if (isCredentialValid(mCredential)) {
			mService = buildService(mCredential);
			return true;
		}
		return rebuildGoogleDriveService(mContext);
	}

	@Override
	public CancelableTask uploadFile(java.io.File file, String path, boolean overwrite,
			ActionListener listener) {
		if (file == null || TextUtils.isEmpty(path)) {
			if (listener != null) {
				listener.onError(FileHostingServiceProvider.ILLEGAL_ARGUMENT, null, null);
			}
			return null;
		}

		GoogleDriveUploadFileTask task = new GoogleDriveUploadFileTask(file, path, overwrite,
				listener);
		task.execute();
		return task;
	}

	@Override
	public CancelableTask downloadFile(String path, java.io.File dir, Object revision,
			ActionListener listener) {
		if (TextUtils.isEmpty(path) || dir == null) {
			if (listener != null) {
				listener.onError(FileHostingServiceProvider.ILLEGAL_ARGUMENT, null, null);
			}
			return null;
		}

		GoogleDriveDownloadFileTask task = new GoogleDriveDownloadFileTask(path, dir, listener);
		task.execute();
		return task;
	}

	@Override
	public void deleteFile(String path, ActionListener listener) {
		if (TextUtils.isEmpty(path)) {
			if (listener != null) {
				listener.onError(FileHostingServiceProvider.ILLEGAL_ARGUMENT, null, null);
			}
			return ;
		}
		GoogleDriveDeleteFileTask task = new GoogleDriveDeleteFileTask(path, listener);
		task.execute(path);
	}

	@Override
	public void getFileInfo(final String path, final ActionListener listener) {
		if (listener == null) {
			return;
		}
		if (TextUtils.isEmpty(path)) {
			listener.onError(FileHostingServiceProvider.ILLEGAL_ARGUMENT, null, null);
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					OnlineFileInfo fileInfo = getFileInfo(path);
					listener.onComplete(fileInfo);
				} catch (FileHostingServiceException e) {
					listener.onError(e.getErrorCode(), e.getMessage(), null);
				}
			}
		}).start();
	}

	@Override
	public OnlineFileInfo getFileInfo(String path) throws FileHostingServiceException {
		if (TextUtils.isEmpty(path)) {
			throw new FileHostingServiceException(ILLEGAL_ARGUMENT, "path is null");
		}
		
		if (!checkDriveServiceValid()) {
			throw new FileHostingServiceException(SERVER_UNAUTHORIZED_ERROR, null);
		}
		
		File result = getFile(path);
		if (result != null) {
			return new GoogleDriveFile(result, this);
		}
		return null;
	}

/*	private File getFile(String path) {
		java.io.File pathFile = new java.io.File(path);
		File file = mCacheFiles.get(path);
		if (file != null) {
			return file;
		}
		try {
			List<File> results = listAllFiles(searchFile(mService.files().list(),
					pathFile.getName(), null));
			if (!Util.isCollectionEmpty(results)) {
				if (results.size() == 1) {
					return results.get(0);
				}
				for (File result : results) {
					List<ParentReference> parents = result.getParents();
					for (ParentReference parent : parents) {
						File parentFile = mService.files().get(parent.getId()).execute();
						if (parentFile != null
								&& TextUtils.equals(parentFile.getTitle(), pathFile.getParentFile()
										.getName())) {
							return result;
						}
					}
				}
			}
		} catch (IOException e) {
			return null;
		}
		return null;
	}*/
	
	private File requestFile(String fileName, String parentId) throws ResourceNotFoundException, NetworkIOException {
		int retryTime = 0;
		List<File> results = null;
		while (retryTime < MAX_RETRY_TIMES) {
			try {
				try {
/*					if (mService == null) {
						StringBuilder sb = new StringBuilder();
						sb.append("getFile : ");
						sb.append("mService = null");
						sb.append("\n");
						Logger.e("GoogleDriveManager", sb.toString());
						Logger.flush();
					}*/
					results = listAllFiles(searchFile(mService.files().list(),
							fileName, parentId));
				} catch (UnknownHostException e) {
					// 网络无连接
					throw new NetworkIOException();
				}
				break;
			} catch (IOException e) {
				e.printStackTrace();
				retryTime++;
				if (retryTime >= MAX_RETRY_TIMES) {
					throw new NetworkIOException();
				}
			}
		}
		if (Util.isCollectionEmpty(results)) {
			throw new ResourceNotFoundException();
		}
		// 若有同名文件，取最近修改的文件
		File dest = null;
		for (File file : results) {
			if (dest == null) {
				dest = file;
				continue;
			}
			DateTime date = file.getModifiedDate();
			DateTime latestDate = dest.getModifiedDate();
			if (date != null && latestDate != null && date.getValue() > latestDate.getValue()) {
				dest = file;
			}
		}
		return dest;
	}
	
	private File getFile(String path) throws ResourceNotFoundException, NetworkIOException {
		java.io.File pathFile = new java.io.File(path);
		File cacheFile = getCacheFile(path);
		if (cacheFile != null) {
			return cacheFile;
		}
		File parent = null;
		if (pathFile.getParent() != null && !TextUtils.equals(pathFile.getParent(), java.io.File.separator)) {
			parent = getFile(pathFile.getParent());
		} else {
			File result = requestFile(pathFile.getName(), null);
			if (result != null) {
				putFileToCache(result, path);
			}
			return result;
		}
		if (parent != null) {
			File result = requestFile(pathFile.getName(), parent.getId());
			if (result != null) {
				putFileToCache(result, path);
			}
			return result;
		}
		return null;
	}

	@Override
	public void logout(Context context) {
		clearStoredToken(context);
		mGoogleDriveAccount = null;
		mCredential = null;
		clearCacheFiles();
		Util.clearCookies(context);
		release();
	}

	@Override
	public void clearStoredToken(Context context) {
		try {
			new PreferencesCredentialStore(context).delete(null, null);
		} catch (IOException e) {
		};
	}

	@Override
	public AccountInfo getAccount(ActionListener listener) {
		if (isAccountValid(mGoogleDriveAccount)) {
			return mGoogleDriveAccount;
		}
		if (!isSessionValid()) {
			return null;
		}
		AccountInfo account = null;
		try {
			account = getAccount(mCredential);
		} catch (FileHostingServiceException e) {
			if (listener != null) {
				listener.onError(e.getErrorCode(), e.getMessage(), null);
			}
			return null;
		}
		if (listener != null) {
			listener.onComplete(account);
		}
		return null;
	}
	
	private AccountInfo getAccount(Credential credential) throws FileHostingServiceException {
		try {
			Oauth2 userInfoService = new Oauth2.Builder(new NetHttpTransport(), new JacksonFactory(),
					credential).build();
			Userinfo userInfo = userInfoService.userinfo().get().execute();
			if (userInfo != null) {
				return new GoogleDriveAccount(userInfo);
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new FileHostingServiceException(SERVER_UNAVALIABLE_ERROR, e.getMessage());
		}
		return null;
	}

	@Override
	public String getServiceProviderName() {
		return "googledrive";
	}

	@Override
	public String getRooPath() {
		return null;
	}

	@Override
	public String getOnlineBackupPath() {
		return "Go Backup" + java.io.File.separator
				+ FileHostingServiceProvider.GOBACKUP_CONTENT_DIR;
	}

	@Override
	public int getType() {
		return GOOGLE_DRIVE;
	}

	private boolean isAccountValid(AccountInfo account) {
		return account != null && account.getDisplayName() != null && account.getUID() != null;
	}

	private File getCacheFile(String key) {
		if (mCacheFiles == null || mCacheFiles.isEmpty()) {
			return null;
		}
		return mCacheFiles.get(key);
	}

	private void putFileToCache(File file, String key) {
		if (mCacheFiles == null) {
			mCacheFiles = new HashMap<String, File>();
		}
		if (TextUtils.isEmpty(key)) {
			throw new IllegalArgumentException("key cannot be null!");
		}
		mCacheFiles.put(key, file);
	}

	private void removeCacheFile(String key) {
		if (mCacheFiles != null) {
			for (Iterator<String> it = mCacheFiles.keySet().iterator(); it.hasNext();) {
				String itemKey = it.next();
				if (itemKey != null && itemKey.startsWith(itemKey)) {
					it.remove();
				}
			}
		}
	}

	private void clearCacheFiles() {
		if (mCacheFiles != null) {
			mCacheFiles.clear();
		}
	}
	
	@Override
	public void release() {
		clearCacheFiles();
	}

	/**
	 * @author ReyZhang
	 */
	private class GoogleDriveUploadFileTask extends AsyncTask<Void, Long, File>
			implements
				CancelableTask {

		private java.io.File mLocalFile;
		private String mMimeType;
		private boolean mOverwriteFlag;
		private ActionListener mListener;
		private String mPath;

		private int mErrorCode;
		private String mErrorMessage;

		private boolean mCanceled = false;
		AbstractInputStreamContent mInputStreamContent;

		private int mRetryTimes = 0;

		private MediaHttpUploaderProgressListener mProgressListener = new MediaHttpUploaderProgressListener() {
			@Override
			public void progressChanged(MediaHttpUploader uploader) throws IOException {
				if (uploader != null) {
					publishProgress(uploader.getNumBytesUploaded());
				}
			}
		};

		public GoogleDriveUploadFileTask(java.io.File file, String path, boolean overwriteFlag,
				ActionListener listener) {
			mLocalFile = file;
			String fileName = mLocalFile.getName();
			int fileNameLength = fileName.length();
			int subStart = fileName.lastIndexOf(".", fileNameLength);
			if (subStart >= 0) {
				mMimeType = EXT_MIME_TYPE_PREFIX + fileName.substring(subStart + 1, fileNameLength);
			}
			mOverwriteFlag = overwriteFlag;
			mPath = path;
			mListener = listener;
		}

		@Override
		protected void onCancelled() {
			if (mListener != null) {
				mListener.onCancel(null);
			}
		}

		@Override
		public void cancel() {
			if (isCancelled() || mCanceled) {
				return;
			}
			try {
				if (mInputStreamContent != null) {
					InputStream is = mInputStreamContent.getInputStream();
					is.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			mCanceled = true;
			cancel(true);
		}

		@Override
		protected File doInBackground(Void... params) {
			if (mLocalFile.exists() && mLocalFile.isDirectory()) {
				throw new IllegalArgumentException("unsupport uploading whole directory!");
			}
			
			if (!checkDriveServiceValid()) {
				mErrorCode = SERVER_UNAUTHORIZED_ERROR;
				mErrorMessage = "drive service invalid!";
				return null;
			}

			File parent = null;
			try {
				parent = getFile(mPath);
			} catch (ResourceNotFoundException e) {
				parent = createFolder(mPath);
			} catch (NetworkIOException e) {
				
			}

			if (parent == null) {
				mErrorCode = FileHostingServiceProvider.FILE_IO_ERROR;
				mErrorMessage = "create folder failed";
				return null;
			}

			try {
				File onlineFile = null;
				try {
					onlineFile = getFile(new java.io.File(mPath, mLocalFile.getName()).getPath());
				} catch (ResourceNotFoundException e) {
					
				} catch (NetworkIOException e) {
					mErrorCode = FileHostingServiceProvider.FILE_IO_ERROR;
					mErrorMessage = "create folder failed";
					return null;
				}

				if (onlineFile != null && mOverwriteFlag) {
					LogUtil.d("update file");
					return uploadFile(onlineFile, true);
				}
				LogUtil.d("new file");
				File newFile = new File().setTitle(mLocalFile.getName()).setMimeType(mMimeType)
						.setParents(Arrays.asList(new ParentReference().setId(parent.getId())));
				return uploadFile(newFile, false);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				mErrorCode = LOCAL_FILE_NOT_EXIST_ERROR;
				mErrorMessage = "cannot open local file";
			} catch (IOException e) {
				mErrorCode = NETWORK_IO_ERROR;
				mErrorMessage = "file upload error!";
			} catch (Exception e) {
				e.printStackTrace();
				mErrorCode = UNKNOWN_ERROR;
				mErrorMessage = "unkown error";
			}
			return null;
		}

		private File uploadFile(File file, boolean update) throws IOException {
			while (mRetryTimes < MAX_RETRY_TIMES) {
				try {
					if (update) {
						mInputStreamContent = new FileContent(mMimeType, mLocalFile);
						Drive.Files.Update operation = mService.files().update(file.getId(), file,
								mInputStreamContent);
						return operation.execute();
					} else {
						InputStream bis = new BufferedInputStream(new FileInputStream(mLocalFile));
						try {
							mInputStreamContent = new InputStreamContent(mMimeType, bis)
								.setRetrySupported(true).setLength(mLocalFile.length());
							Drive.Files.Insert insert = mService.files()
									.insert(file, mInputStreamContent);
							MediaHttpUploader uploader = insert.getMediaHttpUploader();
							uploader.setProgressListener(mProgressListener);
							return insert.execute();
						} finally {
							bis.close();
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
					mRetryTimes++;
					if (mRetryTimes >= MAX_RETRY_TIMES) {
						throw e;
					}
				}
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Long... progress) {
			if (mListener != null) {
				final long fileSize = mLocalFile.length();
				mListener.onProgress(progress[0], fileSize, null);
			}
		}

		@Override
		protected void onPostExecute(File result) {
			if (mListener == null) {
				return;
			}
			if (mErrorCode > 0 && result == null) {
				mListener.onError(mErrorCode, mErrorMessage, null);
			} else if (!mCanceled) {
				OnlineFileInfo file = new GoogleDriveFile(result, GoogleDriveManager.this);
				putFileToCache(result, new java.io.File(mPath, mLocalFile.getName()).getPath());
				mListener.onComplete(file);
			}
		}
	}

	/**
	 * GoogleDrive文件删除任务
	 *
	 * @author ReyZhang
	 */
	private class GoogleDriveDeleteFileTask extends AsyncTask<String, Void, Boolean> {

		private ActionListener mListener;
		private String mPath;
		private int mErrorCode;
		private String mErrorMessage;

		public GoogleDriveDeleteFileTask(String path, ActionListener listener) {
			mListener = listener;
			mPath = path;
		}

		@Override
		protected Boolean doInBackground(String... path) {
			if (!checkDriveServiceValid()) {
				mErrorCode = SERVER_UNAUTHORIZED_ERROR;
				mErrorMessage = "drive service invalid!";
				return false;
			}
			
			File file = null;
			try {
				file = getFile(mPath);
			} catch (FileHostingServiceException e) {
				e.printStackTrace();
				mErrorCode = e.getErrorCode();
				mErrorMessage = e.getMessage();
				return false;
			}
			try {
				Drive.Files.Delete delete = mService.files().delete(file.getId());
				delete.execute();
				removeCacheFile(mPath);
				return true;
			} catch (HttpResponseException e) {
				if (e.getStatusCode() == 404) {
					mErrorCode = SERVER_RESOURCE_NOT_FOUND_ERROR;
					mErrorMessage = "The file doesn't exist!";
				} else {
					mErrorCode = UNKNOWN_ERROR;
					mErrorMessage = "unknown error";
				}
			} catch (IOException e) {
				e.printStackTrace();
				mErrorCode = UNKNOWN_ERROR;
				mErrorMessage = "unknown error";
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (result && mListener != null) {
				mListener.onComplete(null);
				return;
			}
			if (mListener != null) {
				mListener.onError(mErrorCode, mErrorMessage, null);
			}
		}
	}

	/**
	 * GoogleDrive下载文件任务
	 *
	 * @author ReyZhang
	 */
	private class GoogleDriveDownloadFileTask extends AsyncTask<Void, Double, Boolean>
			implements
				CancelableTask {
		private ActionListener mListener;
		private java.io.File mLocalDir;
		private OnlineFileInfo mCurrentFile;
		private String mOnlineFilePath;
		private int mErrorCode;
		private String mErrorMessage;
		private int mFileIndex;
		private int mFileCount;
		private int mRetryTimes = 0;
		private boolean mCanceled = false;
		private byte[] mLock = new byte[0];
		private OutputStream mOutputStream;
		private int mProgress;
		private MediaHttpDownloaderProgressListener mProgressListener = new MediaHttpDownloaderProgressListener() {
			@Override
			public void progressChanged(MediaHttpDownloader downloader) throws IOException {
				publishProgress(downloader.getProgress());
			}
		};

		public GoogleDriveDownloadFileTask(String path, java.io.File localDir,
				ActionListener listener) {
			mListener = listener;
			mLocalDir = localDir;
			mOnlineFilePath = path;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			if (!checkDriveServiceValid()) {
				mErrorCode = SERVER_UNAUTHORIZED_ERROR;
				mErrorMessage = "drive service invalid!";
				return false;
			}
			
			if (!mLocalDir.exists()) {
				mLocalDir.mkdirs();
			}
			File destFile = null;
			try {
				destFile = getFile(mOnlineFilePath);
			} catch (FileHostingServiceException e) {
				e.printStackTrace();
				mErrorCode = e.getErrorCode();
				mErrorMessage = e.getMessage();
				return false;
			}
			if (destFile != null) {
				putFileToCache(destFile, mOnlineFilePath);
				mCurrentFile = buildFileInfo(destFile);
			}
			try {
				mFileIndex = 0;
				if (!mCurrentFile.isDirectory()) {
					mFileCount = 1;
					downloadFile(mLocalDir, destFile);
				} else {
					java.io.File localDir = new java.io.File(mLocalDir, mCurrentFile.getFileName());
					if (!localDir.exists()) {
						localDir.mkdirs();
					}
					List<File> children = listAllChildren(destFile.getId());
					if (Util.isCollectionEmpty(children)) {
						return true;
					}
					mFileCount = children.size();
					for (File child : children) {
						LogUtil.d("file index = " + mFileIndex);
						putFileToCache(child,
								new java.io.File(mOnlineFilePath, child.getTitle()).getPath());
						mCurrentFile = buildFileInfo(child);
						downloadFile(localDir, child);
						mFileIndex++;
					}
				}
				return true;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				mListener.onError(LOCAL_FILE_NOT_EXIST_ERROR, "file no found in GoogleDrive",
						mCurrentFile);
			} catch (IOException e) {
				e.printStackTrace();
				mListener.onError(NETWORK_IO_ERROR, "file download fail", mCurrentFile);
			}
			return false;
		}

		private void downloadFile(java.io.File dir, File file) throws IOException {
			mRetryTimes = 0;
			while (mRetryTimes < MAX_RETRY_TIMES) {
				synchronized (mLock) {
					if (mCanceled) {
						return;
					}
				}
				mOutputStream = new FileOutputStream(new java.io.File(dir, file.getTitle()));
				try {
					Drive.Files.Get get = mService.files().get(file.getId());
					MediaHttpDownloader downloader = get.getMediaHttpDownloader();
					downloader.setProgressListener(mProgressListener);
					String downloadUrl = file.getDownloadUrl();
					downloader.download(new GenericUrl(downloadUrl), mOutputStream);
					return;
				} catch (IOException e) {
					if (mCanceled) {
						return;
					}
					mRetryTimes++;
					LogUtil.d("retry times = " + mRetryTimes);
					if (mRetryTimes >= MAX_RETRY_TIMES) {
						throw e;
					}
				} finally {
					mOutputStream.close();
				}
			}
		}

		@Override
		protected void onProgressUpdate(Double... progress) {
			if (mListener != null) {
				mProgress = (int) ((mFileIndex + progress[0]) / mFileCount * 100);
				mListener.onProgress(mProgress, 100, mCurrentFile);
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (result == null) {
				return;
			}
			if (result && mListener != null) {
				mListener.onComplete(mCurrentFile);
				return;
			}
			if (mListener != null) {
				mListener.onError(mErrorCode, mErrorMessage, mCurrentFile);
			}
		}

		@Override
		public void cancel() {
			if (isCancelled() || mCanceled) {
				return;
			}
			if (mOutputStream != null) {
				try {
					mOutputStream.close();
				} catch (IOException e) {
				};
			}
			synchronized (mLock) {
				mCanceled = true;
				cancel(true);
			}
		}

		@Override
		protected void onCancelled() {
			if (mListener != null) {
				mListener.onCancel(mCurrentFile);
			}
		}

		private OnlineFileInfo buildFileInfo(File file) {
			return new GoogleDriveFile(file, GoogleDriveManager.this);
		}
	}
}
