package com.jiubang.go.backup.pro.bookmark;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import android.content.Context;
import android.database.Cursor;
import android.os.Message;
import android.provider.Browser;
import android.util.Log;

import com.jiubang.go.backup.pro.bookmark.BackupTwo.BookMarkBackupArgs;
import com.jiubang.go.backup.pro.bookmark.BackupTwo.BookMarkBackupMsg;
import com.jiubang.go.backup.pro.data.BookMarkBackupEntry;

/**
 * <br>类描述:4.x手机的备份，都是没有folder字段的备份。对于如果有folder字段的备份留了一个备份方法
 * <br>功能详细描述:
 *
 * @author  jiangpeihe
 * @date  [2012-10-16]
 */
public class BackupFour implements BookMarkOperate {

	@Override
	public void backupBookMark(Context context, BookMarkBackupArgs args) {

		if (!ensureArgsValid(args)) {
			return;
		}
		Cursor queryCursor = null;
		try {
			queryCursor = context.getContentResolver().query(Browser.BOOKMARKS_URI,
					BookMarkBackupEntry.BOOKMARK_COLUMNS_WITH_FOLDER, null, null, null);
		} catch (Exception e) {
			e.printStackTrace();
			queryCursor = null;
		}
		if (queryCursor == null || queryCursor.getCount() == 0) {
			try {
				queryCursor = context.getContentResolver().query(Browser.BOOKMARKS_URI,
						BookMarkBackupEntry.STARNDARD_BOOKMARK_COLUMNS, null, null, null);
			} catch (Exception e1) {
				e1.printStackTrace();
				queryCursor = null;
				args.mHandler.sendEmptyMessage(BookMarkBackupMsg.BOOKMARK_BACKUP_ERROR_OCCUR);
			}
		}
		try {
			if (queryCursor == null || queryCursor.getCount() == 0) {
				args.mHandler.sendEmptyMessage(BookMarkBackupMsg.BOOKMARK_COUNT_ZERO);
				return;
			}
			if (queryCursor.getColumnIndex("folder") > -1) {
				Log.d("GoBackup", "youfolder字段");
			} else {
				noFolderBackup(context, args);
			}
		} finally {
			if (queryCursor != null) {
				queryCursor.close();
			}
		}
	}
	private boolean ensureArgsValid(BookMarkBackupArgs args) {
		if (args == null) {
			return false;
		}
		if (args.mHandler == null || args.mBackupFilePath == null) {
			return false;
		}
		return true;
	}

	/** <br>功能简述:没有folder字段的备份方法
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @param args
	 */
	public void noFolderBackup(Context context, BookMarkBackupArgs args) {
		// 通知外部开始备份书签
		args.mHandler.sendEmptyMessage(BookMarkBackupMsg.BOOKMARK_BACKUP_START);
		ObjectOutputStream oos = null;
		//正在备份第几条书签
		int curBookmarkIndex = 0;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(new File(args.mBackupFilePath)));
		} catch (IOException e) {
			e.printStackTrace();
			args.mHandler.sendEmptyMessage(BookMarkBackupMsg.BOOKMARK_BACKUP_ERROR_OCCUR);
			return;
		}

		try {
			//			int folderCount = BookMarkBackupEntry.getLocalFolderCount(context);
			Cursor cursor = context.getContentResolver().query(Browser.BOOKMARKS_URI,
					BookMarkBackupEntry.STARNDARD_BOOKMARK_COLUMNS,
					Browser.BookmarkColumns.BOOKMARK + "<>0", null, null);
			if (cursor == null) {
				args.mHandler.sendEmptyMessage(BookMarkBackupMsg.BOOKMARK_BACKUP_ERROR_OCCUR);
				return;
			}
			try {
				int bookmarkCount = BookMarkBackupEntry.getLocalBookMarkCount(context);
				if (bookmarkCount <= 0 || !cursor.moveToFirst()) {
					return;
				}
				int created_Index = cursor.getColumnIndex(Browser.BookmarkColumns.CREATED);
				do {
					String bookmark = cursor.getString(cursor
							.getColumnIndex(Browser.BookmarkColumns.BOOKMARK));
					String url = cursor.getString(cursor
							.getColumnIndex(Browser.BookmarkColumns.URL));
					//				if (!bookmark.equals("1")) {
					//					continue;
					//				}

					// 通知当前正在备份第几条短信
					if (bookmark.equals("1") && url != null && !url.equals("")) {
						curBookmarkIndex++;
						Message.obtain(args.mHandler, BookMarkBackupMsg.BOOKMARK_BACKUP_PROCEEDING,
								curBookmarkIndex, bookmarkCount).sendToTarget();
					}
					BookMark bookMark = new BookMark();
					bookMark.setTitle(cursor.getString(cursor
							.getColumnIndex(Browser.BookmarkColumns.TITLE)));
					bookMark.setUrl(url);
					bookMark.setBookmark(bookmark);
					bookMark.setDate(cursor.getString(cursor
							.getColumnIndex(Browser.BookmarkColumns.DATE)));
					if (created_Index > -1) {
						bookMark.setCreated(cursor.getString(created_Index));
					} else {
						//随机生成一个时间
						String createTime = "0";
						bookMark.setCreated(String.valueOf(createTime));
					}
					bookMark.setVisits(cursor.getString(cursor
							.getColumnIndex(Browser.BookmarkColumns.VISITS)));
					bookMark.setFavicon(cursor.getBlob(cursor
							.getColumnIndex(Browser.BookmarkColumns.FAVICON)));
					//folder,跟created字段没有的，主要是为了与其他书签兼容
					bookMark.setFolder("0");
					bookMark.setTouchicon(cursor.getBlob(cursor.getColumnIndex("touch_icon")));
					bookMark.setThumbnail(cursor.getBlob(cursor.getColumnIndex("thumbnail")));
					FileUtil.writeObject(context, bookMark, oos, args.mHandler);
				} while (cursor.moveToNext());
			} finally {
				cursor.close();
			}

			//备份完毕
			args.mHandler.sendEmptyMessage(BookMarkBackupMsg.BOOKMARK_BACKUP_END);
			//最后把一个null写到文件组，当读文件的时候，如果为null，则表示读完文件
			FileUtil.writeObject(context, null, oos, args.mHandler);
		} catch (Exception e) {
			e.printStackTrace();
			//备份出错
			args.mHandler.sendEmptyMessage(BookMarkBackupMsg.BOOKMARK_BACKUP_ERROR_OCCUR);
		} finally {
			try {
				oos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
