package com.jiubang.go.backup.pro;

import java.io.File;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.jiubang.go.backup.pro.model.BackupManager;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 命令模式的实现，用于Activity或Service之间作为数据传递的一种轻量级对象
 * 有利于解除消息的接收者与发送者之间的耦合，适用于参数较为简单的简单命令的实现 抽象基类
 * 
 * @author maiyongshen
 */
public abstract class ParcelableAction implements Parcelable {
	private static final int NULL_TYPE_ID = 0;

	private ParcelableAction() {

	}

	public abstract void execute();

	public static final Parcelable.Creator<ParcelableAction> CREATOR = new Parcelable.Creator<ParcelableAction>() {

		@Override
		public ParcelableAction createFromParcel(Parcel source) {
			int type = source.readInt();
			switch (type) {
				case NULL_TYPE_ID :
					return null;
				case DeleteFileAction.TYPE_ID :
					return DeleteFileAction.readFrom(source);
				case ReleaseBackupableRecordAction.TYPE_ID :
					return new ReleaseBackupableRecordAction();
				case RebootAction.TYPE_ID :
					return new RebootAction();
				case DeleteRecordAction.TYPE_ID :
					return DeleteRecordAction.readFrom(source);
			}
			throw new IllegalArgumentException("Unknown ParcelableAction type: " + type);
		}

		@Override
		public ParcelableAction[] newArray(int size) {
			return new ParcelableAction[size];
		}
	};

	/**
	 * 文件删除命令
	 * 
	 * @author maiyongshen
	 */
	public static class DeleteFileAction extends ParcelableAction {
		static final int TYPE_ID = 1;
		private String mPath;

		public DeleteFileAction(String fullFilePath) {
			if (fullFilePath == null) {
				throw new NullPointerException("filePath");
			}
			mPath = fullFilePath;
		}

		public static ParcelableAction readFrom(Parcel parcel) {
			return new DeleteFileAction(parcel.readString());
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeInt(TYPE_ID);
			dest.writeString(mPath);
		}

		@Override
		public void execute() {
			File destFile = new File(mPath);
			if (!destFile.exists()) {
				return;
			}
			Util.deleteFile(destFile.getAbsolutePath());
		}
	}

	/**
	 * 清除BackupManager的缓存备份记录
	 * 
	 * @author maiyongshen
	 */
	public static class ReleaseBackupableRecordAction extends ParcelableAction {
		static final int TYPE_ID = 2;

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeInt(TYPE_ID);
		}

		@Override
		public void execute() {
			BackupManager.getInstance().releaseBackupableRecord();
		}
	}

	/**
	 * 重启手机命令
	 * 
	 * @author maiyongshen
	 */
	public static class RebootAction extends ParcelableAction {
		static final int TYPE_ID = 3;

		private Context mContext;

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeInt(TYPE_ID);
		}

		public void setContext(Context context) {
			mContext = context;
		}

		@Override
		public void execute() {
			if (mContext == null) {
				return;
			}
			Util.reboot();
		}
	}

	/**
	 * 删除已备份的记录文件
	 * 
	 * @author maiyongshen
	 */
	public static class DeleteRecordAction extends ParcelableAction {
		static final int TYPE_ID = 4;
		private long mRecordId = -1;

		public DeleteRecordAction(long recordId) {
			mRecordId = recordId;
		}

		public static ParcelableAction readFrom(Parcel parcel) {
			return new DeleteRecordAction(parcel.readLong());
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeInt(TYPE_ID);
			dest.writeLong(mRecordId);
		}

		@Override
		public void execute() {
			BackupManager.getInstance().deleteRecordById(mRecordId);
		}

	}

}
