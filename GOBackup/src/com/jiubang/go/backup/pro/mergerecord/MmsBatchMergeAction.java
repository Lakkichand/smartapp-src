package com.jiubang.go.backup.pro.mergerecord;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.data.MmsBackupEntry;
import com.jiubang.go.backup.pro.data.RestorableRecord;
import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.util.MD5Util;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 彩信整合
 * 
 * @author ReyZhang
 */
public class MmsBatchMergeAction extends BatchMergeAction {

	private BackupDBHelper mDbHelper;
	private Context mContext = null;
	private File mMmsFileDir = null;
	private Map<String, File> mMmsMap = null;
	private static int count = 1;
	// 用于储存MD5码的值，以保证数据的一致性
	private List<String> mList = null;

	private final int mWeight = 10;

	public MmsBatchMergeAction(Context context, RestorableRecord beMergedRecord,
			BackupDBHelper dbHelper, IAsyncTaskListener listener) {
		super(beMergedRecord, listener);
		mContext = context;
		mDbHelper = dbHelper;
		if (mBeMergedRecord == null) {
			return;
		}

	}

	@Override
	protected MergeAction buildMergeAction(RestorableRecord record) {
		return new MmsMergeAction(mBeMergedRecord, record, mListener);
	}

	@Override
	protected boolean shouldMerge(RestorableRecord toMergeRecord) {
		boolean result = false;
		String beMmsPath = toMergeRecord.getRecordRootDir() + MmsBackupEntry.MMS_DIR_NAME;
		// 1. 如果没有彩信备份项，不进行合并
		File beMergeFiles = new File(beMmsPath);
		if (!beMergeFiles.isDirectory() || beMergeFiles.list() == null) {
			return result;
		}
		result = true;

		return result;
	}

	@Override
	protected void onPreprocessing() {
		if (mList == null) {
			mList = new ArrayList<String>();
		}
		// 1. 获取获取合并数据项下面的彩信
		mMmsMap = getMergeMmsRecord();
		// 如果map为空
		if (mMmsMap == null) {
			// 创建文件夹
			mMmsFileDir = new File(mBeMergedRecord.getRecordRootDir() + MmsBackupEntry.MMS_DIR_NAME
					+ "/");
			if (!mMmsFileDir.exists()) {
				mMmsFileDir.mkdir();
			}
			mMmsMap = new HashMap<String, File>();
		}
	}

	@Override
	protected boolean onPostprocessing(boolean[] subActionResults) {
		if (subActionResults != null) {
			final int len = subActionResults.length;
			for (int i = 0; i < len; i++) {
				if (subActionResults[i]) {
					break;
				}
			}
		}

		// 结果写入配置文件
		boolean updateResult = false;
		updateResult = doCommandUpdate();

		return updateResult;
	}

	private boolean doCommandUpdate() {
		boolean result = false;
		if (mBeMergedRecord == null) {
			return result;
		}
		String beMmsPath = mBeMergedRecord.getRecordRootDir() + MmsBackupEntry.MMS_DIR_NAME;

		// 做旧版兼容。。。。
		// 计算彩信文件夹里面的pdu数量
		File fileDir = new File(beMmsPath);
		int mmsCount = 0;
		String[] subFiles = fileDir.list();
		if (fileDir.isDirectory() && subFiles != null) {
			mmsCount = subFiles.length;
		}

		// BackupPropertiesConfig config =
		// mBeMergedRecord.getBackupPropertiesConfig();
		// if( config == null ){
		// 认为可能是新版
		MmsBackupEntry.updateBackupDb(mDbHelper, beMmsPath);
		return true;
		// }

		// MmsBackupEntry.updateBackupDb( mBeMergedRecord.getBackupDBHelper(
		// mContext ), beMmsPath );
		// return result;
	}

	@Override
	public String getDescription(Context context) {
		return context != null ? context.getString(R.string.mms) : super.getDescription(context);
	}

	/**
	 * 内部类，彩信整合Action
	 * 
	 * @author ReyZhang
	 */
	private class MmsMergeAction extends MergeAction {

		public MmsMergeAction(RestorableRecord beMergedRecord, RestorableRecord toMergeRecord,
				IAsyncTaskListener listener) {
			super(beMergedRecord, toMergeRecord, listener);
		}

		@Override
		public boolean execute() {
			boolean result = false;
			if (mBeMergedRecord == null) {
				return result;
			}
			result = doCommandMergeRecords();

			return result;
		}

		@Override
		public int getProgressWeight() {
			return mWeight;
		}

		private boolean doCommandMergeRecords() {

			boolean result = false;
			if (mToMergeRecord == null) {
				return false;
			}
			if (mMergeActions == null) {
				return result;
			}
			int actionCount = mMergeActions.size();
			String toMmsPath = mToMergeRecord.getRecordRootDir() + MmsBackupEntry.MMS_DIR_NAME;
			// 要被合并的彩信文件
			File toMergeFiles = new File(toMmsPath);
			if (!toMergeFiles.exists()) {
				return false;
			}
			File[] toMergeFileLists = toMergeFiles.listFiles();
			int toMergeFilesCount = toMergeFileLists.length;
			for (int i = 0; i < toMergeFilesCount; i++) {
				// 如果通过map get被合并彩信的md5码值为null，说明不是同一条彩信，并将该彩信也存入map中
				String toMd5Str = MD5Util.getFileMd5Code(toMergeFileLists[i]);
				if (mMmsMap.get(toMd5Str) == null) {
					mMmsMap.put(toMd5Str, toMergeFileLists[i]);
					mList.add(toMd5Str);
				}
			}
			// 如果达到了所有备份项的条数，说明，已经完成了对map数据的添加，进行文件的拷贝
			if (count == actionCount) {
				// 进行数据的拷贝
				String beMmsPath = mBeMergedRecord.getRecordRootDir() + MmsBackupEntry.MMS_DIR_NAME;
				File beMergeFiles = new File(beMmsPath);
				if (!beMergeFiles.exists()) {
					return false;
				}
				int mapCount = mMmsMap.size();

				if (mapCount == mList.size()) {
					File toFiles = null;
					for (int i = 0; i < mapCount; i++) {
						toFiles = mMmsMap.get(mList.get(i));
						File beFile = new File(beMmsPath + "/" + toFiles.getName());
						Util.copyFile(toFiles.getAbsolutePath(), beFile.getAbsolutePath());
					}
					count = 1;
					mMmsMap.clear();
					return true;
				}
			}
			count++;
			result = true;
			return result;
		}
	}

	private Map getMergeMmsRecord() {
		Map<String, File> map = null;
		// 合并文件夹的路径
		String beMmsPath = mBeMergedRecord.getRecordRootDir() + MmsBackupEntry.MMS_DIR_NAME;
		File file = new File(beMmsPath);
		if (!file.exists()) {
			return null;
		}
		File[] fileList = file.listFiles();
		int fileCount = fileList.length;
		if (file.isDirectory() && fileCount != 0) {
			map = new HashMap<String, File>();
			for (int i = 0; i < fileCount; i++) {
				String beMd5Str = MD5Util.getFileMd5Code(fileList[i]);
				map.put(beMd5Str, fileList[i]);
				mList.add(beMd5Str);
			}
		}
		return map;
	}

}
