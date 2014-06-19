package com.jiubang.go.backup.pro.mergerecord;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.RingtoneManager;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.data.RestorableRecord;
import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.model.BackupDBHelper.DataTable;
import com.jiubang.go.backup.pro.model.BackupDBHelper.MimetypeTable;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.ringtone.RingtoneBackup;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 铃声整合
 *
 * @author chenchangming
 */
public class RingtoneBatchMergeAction extends BatchMergeAction {
	private Date mLatestDate = null;
	private BackupDBHelper mDbHelper;

	public RingtoneBatchMergeAction(RestorableRecord beMergedRecord, BackupDBHelper dbHelper,
			IAsyncTaskListener listener) {
		super(beMergedRecord, listener);
		mDbHelper = dbHelper;
	}

	@Override
	protected MergeAction buildMergeAction(RestorableRecord record) {
		mLatestDate = record.getDate();
		return new RingtoneMergeAction(mBeMergedRecord, record, mListener);
	}

	@Override
	protected boolean shouldMerge(RestorableRecord toMergeRecord) {
		if (getRingtoneOriginalFile(toMergeRecord) == null) {
			return false;
		}

		if (mLatestDate == null) {
			clear();
			return true;
		}

		if (mLatestDate.compareTo(toMergeRecord.getDate()) >= 0) {
			// 合并记录的记录比待合并记录新，不用更新合并
			return false;
		}

		clear();
		return true;
	}

	private File getRingtoneOriginalFile(RestorableRecord record) {
		File file = new File(record.getRecordRootDir(), RingtoneBackup.MANAGER_DATA);
		if (!file.exists()) {
			return null;
		}
		return file;
	}

	@Override
	protected void onPreprocessing() {
		super.onPreprocessing();
	}

	@Override
	protected boolean onPostprocessing(boolean[] subActionResults) {
		boolean ret = true;
		final int count = subActionResults.length;
		for (int i = 0; i < count; i++) {
			if (!subActionResults[i]) {
				ret = false;
				break;
			}
		}
		return ret;
	}

	@Override
	public String getDescription(Context context) {
		return context != null ? context.getString(R.string.ringtone) : super
				.getDescription(context);
	}

	/**
	 * 铃声整合Action
	 *
	 * @author chenchangming
	 */
	private class RingtoneMergeAction extends MergeAction {

		public RingtoneMergeAction(RestorableRecord beMergedRecord, RestorableRecord toMergeRecord,
				IAsyncTaskListener listener) {
			super(beMergedRecord, toMergeRecord, listener);
		}

		@Override
		public boolean execute() {
			return doCommandMergeRecords();
		}

		@Override
		public int getProgressWeight() {
			final int m5 = 5;
			return m5;
		}

		private boolean doCommandMergeRecords() {
			if (mDbHelper == null) {
				return false;
			}

			boolean result = false;
			if (mToMergeRecord == null) {
				return false;
			}

			FileFilter ringFilter = new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					if (pathname.getName().endsWith(RingtoneBackup.RINGTONE_SUFFIX)) {
						return true;
					}
					return false;
				}
			};
			// 重命名原来铃声文件
			File beRecordDir = new File(mBeMergedRecord.getRecordRootDir());
			File[] oldRingFiles = beRecordDir.listFiles(ringFilter);
			// 重命名
			oldRingFiles = renameRingtoneFile(oldRingFiles, ".temp", true);

			// 拷贝声音文件
			File recordDir = new File(mToMergeRecord.getRecordRootDir());
			File[] allRingFiles = recordDir.listFiles(ringFilter);
			if (allRingFiles == null || allRingFiles.length == 0) {
				return false;
			}
			for (File ringfile : allRingFiles) {
				result = Util.copyFile(ringfile.getAbsolutePath(),
						new File(mBeMergedRecord.getRecordRootDir(), ringfile.getName())
								.getAbsolutePath());
				if (!result) {
					break;
				}
			}

			File managerFile = null;
			File tempManagerFile = null;
			if (result) {
				// 重命名
				managerFile = new File(mBeMergedRecord.getRecordRootDir(),
						RingtoneBackup.MANAGER_DATA);
				File[] tempManagerFiles = renameRingtoneFile(new File[] { managerFile }, ".temp",
						true);
				tempManagerFile = tempManagerFiles == null ? null : tempManagerFiles[0];
				result = Util.copyFile(new File(mToMergeRecord.getRecordRootDir(),
						RingtoneBackup.MANAGER_DATA).getAbsolutePath(), managerFile
						.getAbsolutePath());
			}

			// 更新数据库
			if (result) {
				result = updateBackupDb();
			}

			if (!result) {
				// 拷贝文件失败 删除已拷贝文件
				for (File ringfile : allRingFiles) {
					File tempFile = new File(mBeMergedRecord.getRecordRootDir(), ringfile.getName());
					tempFile.delete();
				}
				if (managerFile != null) {
					managerFile.delete();
				}

				// 重新讲temp文件重命名为铃声文件 //TODO
				renameRingtoneFile(allRingFiles, ".temp", false);
				renameRingtoneFile(new File[] { new File(mBeMergedRecord.getRecordRootDir(),
						RingtoneBackup.MANAGER_DATA + ".temp") }, ".temp", false);
				return result;
			} else {
				// 成功，删除之前的temp文件
				if (oldRingFiles != null) {
					for (File file : oldRingFiles) {
						file.delete();
					}
				}
				if (tempManagerFile != null) {
					tempManagerFile.delete();
				}
			}

			return result;
		}

		private File[] renameRingtoneFile(File[] allRingtoneFile, String suffix,
				boolean renameToTemp) {
			if (allRingtoneFile == null || suffix == null) {
				return null;
			}

			int index = 0;
			File[] result = new File[allRingtoneFile.length];
			for (File file : allRingtoneFile) {
				File tempFile = null;
				if (renameToTemp) {
					tempFile = new File(mBeMergedRecord.getRecordRootDir(), file.getName() + suffix);
				} else {
					String name = file.getName().replace(suffix, "");
					tempFile = new File(mBeMergedRecord.getRecordRootDir(), name);
				}
				file.renameTo(tempFile);
				result[index] = tempFile;
				index++;
			}
			return result;
		}

		private boolean updateBackupDb() {
			if (mDbHelper == null) {
				return false;
			}

			String managerData = readFile(new File(mBeMergedRecord.getRecordRootDir(),
					RingtoneBackup.MANAGER_DATA).getAbsolutePath());
			if (managerData == null) {
				return false;
			}

			JSONObject managerJson = null;
			try {
				managerJson = new JSONObject(managerData);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if (managerJson == null) {
				return false;
			}

			final int ringtonCount = managerJson.length();
			boolean ret = false;
			Cursor cursor = null;
			try {
				String whereCondition = DataTable.MIME_TYPE + "="
						+ MimetypeTable.MIMETYPE_VALUE_RINGTONE;
				cursor = mDbHelper.query(DataTable.TABLE_NAME, null, whereCondition, null, null);
				if (cursor != null && cursor.getCount() != 0) {
					mDbHelper.delete(DataTable.TABLE_NAME, whereCondition, null);
				}

				ContentValues cv = new ContentValues();
				cv.put(DataTable.MIME_TYPE, MimetypeTable.MIMETYPE_VALUE_RINGTONE);
				int count = ringtonCount;
				cv.put(DataTable.DATA1, count);

				cv.put(DataTable.DATA2, RingtoneBackup.MANAGER_DATA);

				JSONObject rongtoneJson = managerJson.optJSONObject(String
						.valueOf(RingtoneManager.TYPE_ALARM));
				if (rongtoneJson != null) {
					cv.put(DataTable.DATA3, new File(rongtoneJson.optString("_display_name")
							+ RingtoneBackup.RINGTONE_SUFFIX).getName());
				}
				rongtoneJson = managerJson.optJSONObject(String.valueOf(RingtoneManager.TYPE_ALL));
				if (rongtoneJson != null) {
					cv.put(DataTable.DATA4, new File(rongtoneJson.optString("_display_name")
							+ RingtoneBackup.RINGTONE_SUFFIX).getName());
				}
				rongtoneJson = managerJson.optJSONObject(String
						.valueOf(RingtoneManager.TYPE_NOTIFICATION));
				if (rongtoneJson != null) {
					cv.put(DataTable.DATA5, new File(rongtoneJson.optString("_display_name")
							+ RingtoneBackup.RINGTONE_SUFFIX).getName());
				}
				rongtoneJson = managerJson.optJSONObject(String
						.valueOf(RingtoneManager.TYPE_RINGTONE));
				if (rongtoneJson != null) {
					cv.put(DataTable.DATA6, new File(rongtoneJson.optString("_display_name")
							+ RingtoneBackup.RINGTONE_SUFFIX).getName());
				}

				cv.put(DataTable.DATA14, System.currentTimeMillis());
				ret = mDbHelper.reflashDatatable(cv);
			} catch (Exception e) {
				e.printStackTrace();
				ret = false;
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
			return ret;
		}

		private String readFile(String path) {
			String data = "";
			BufferedReader br;
			try {
				br = new BufferedReader(new FileReader(path));
				String r = br.readLine();
				while (r != null) {
					data += r;
					r = br.readLine();
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return data;
			} catch (Exception e) {
				e.printStackTrace();
				return data;
			}
			return data;
		}
	}
}
