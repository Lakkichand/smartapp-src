package com.jiubang.go.backup.pro.bookmark;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.provider.Browser;

import com.jiubang.go.backup.pro.data.BookMarkBackupEntry;

/**
 * <br>类描述:2.x系统手机的备份，包含有文件夹和没有文件夹的来那个种备份
 * <br>功能详细描述:
 *
 * @author  jiangpeihe
 * @date  [2012-10-16]
 */
public class BackupTwo implements BookMarkOperate {

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
			final int folderIndex = queryCursor.getColumnIndex("folder");
			if (folderIndex > -1) {
				folderBackup(context, args);
			} else {
				noFolderBackup(context, args);
			}

		} catch (Exception e) {
			args.mHandler.sendEmptyMessage(BookMarkBackupMsg.BOOKMARK_BACKUP_ERROR_OCCUR);
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

	private void folderBackup(Context context, BookMarkBackupArgs args) {
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
			Cursor cursor = context.getContentResolver().query(Browser.BOOKMARKS_URI,
					BookMarkBackupEntry.BOOKMARK_COLUMNS_WITH_FOLDER,
					Browser.BookmarkColumns.BOOKMARK + "<>0", null, "folder");
			if (cursor == null) {
				args.mHandler.sendEmptyMessage(BookMarkBackupMsg.BOOKMARK_BACKUP_ERROR_OCCUR);
				return;
			}
			try {
				//获得备份书签的总数
				int bookmarkCount = BookMarkBackupEntry.getLocalBookMarkCount(context);
				if (bookmarkCount <= 0 || !cursor.moveToFirst()) {
					args.mHandler.sendEmptyMessage(BookMarkBackupMsg.BOOKMARK_COUNT_ZERO);
					return;
				}
				do {

					String bookmark = cursor.getString(cursor
							.getColumnIndex(Browser.BookmarkColumns.BOOKMARK));
					//				if (bookmark.equals("0")) {
					//					continue;
					//				}

					String title = cursor.getString(cursor
							.getColumnIndex(Browser.BookmarkColumns.TITLE));
					String url = cursor.getString(cursor
							.getColumnIndex(Browser.BookmarkColumns.URL));
					String date = cursor.getString(cursor
							.getColumnIndex(Browser.BookmarkColumns.DATE));
					String created = cursor.getString(cursor
							.getColumnIndex(Browser.BookmarkColumns.CREATED));
					String folder = cursor.getString(cursor.getColumnIndex("folder"));
					byte[] favicon = cursor.getBlob(cursor
							.getColumnIndex(Browser.BookmarkColumns.FAVICON));
					byte[] thumbnail = cursor.getBlob(cursor.getColumnIndex("thumbnail"));
					byte[] touchicon = cursor.getBlob(cursor.getColumnIndex("touch_icon"));

					BookMark bookMark = new BookMark();
					bookMark.setBookmark(bookmark);
					bookMark.setTitle(title);
					bookMark.setUrl(url);
					bookMark.setDate(date);
					bookMark.setCreated(created);
					bookMark.setFolder(folder);
					bookMark.setFavicon(favicon);
					bookMark.setThumbnail(thumbnail);
					bookMark.setTouchicon(touchicon);
					if (folder.equals("0")) {
						bookMark.setParentCreated("0");
						bookMark.setParentTitle("0");
					} else {
						Cursor parentCursor = context.getContentResolver().query(
								Browser.BOOKMARKS_URI,
								BookMarkBackupEntry.STARNDARD_BOOKMARK_COLUMNS,
								"_id = " + "'" + folder + "'", null, null);
						if (parentCursor.getCount() == 0) {
							continue;
						}
						parentCursor.moveToNext();
						String parentCreated = parentCursor.getString(cursor
								.getColumnIndex(Browser.BookmarkColumns.CREATED));
						String parentTitle = parentCursor.getString(cursor
								.getColumnIndex(Browser.BookmarkColumns.TITLE));
						bookMark.setParentCreated(parentCreated);
						bookMark.setParentTitle(parentTitle);
						parentCursor.close();
					}
					// 通知当前正在备份第几条短信
					if (bookmark.equals("1") && url != null && !url.equals("")) {
						curBookmarkIndex++;
						Message.obtain(args.mHandler, BookMarkBackupMsg.BOOKMARK_BACKUP_PROCEEDING,
								curBookmarkIndex, bookmarkCount).sendToTarget();
					}
					FileUtil.writeObject(context, bookMark, oos, args.mHandler);
				} while (cursor.moveToNext());
			} finally {
				cursor.close();
			}
			if (curBookmarkIndex == 0) {
				args.mHandler.sendEmptyMessage(BookMarkBackupMsg.BOOKMARK_COUNT_ZERO);
			} else {
				//最后把一个null写到文件组，当读文件的时候，如果为null，则表示读完文件
				FileUtil.writeObject(context, null, oos, args.mHandler);
			}
			//备份完成
			args.mHandler.sendEmptyMessage(BookMarkBackupMsg.BOOKMARK_BACKUP_END);
		} catch (Exception e) {
			e.printStackTrace();
			// 写文件失败
			args.mHandler.sendEmptyMessage(BookMarkBackupMsg.BOOKMARK_BACKUP_ERROR_OCCUR);
		} finally {
			try {
				oos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	//没有folder字段
	public void noFolderBackup(Context context, BookMarkBackupArgs args) {
		// 通知外部开始备份书签
		args.mHandler.sendEmptyMessage(BookMarkBackupMsg.BOOKMARK_BACKUP_START);
		FileOutputStream fos = null;
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
			Cursor cursor = context.getContentResolver().query(Browser.BOOKMARKS_URI,
					BookMarkBackupEntry.STARNDARD_BOOKMARK_COLUMNS,
					Browser.BookmarkColumns.BOOKMARK + "<>0", null, null);
			if (cursor == null) {
				args.mHandler.sendEmptyMessage(BookMarkBackupMsg.BOOKMARK_BACKUP_ERROR_OCCUR);
				return;
			}
			//获得备份书签的总数
			try {
				int bookmarkCount = BookMarkBackupEntry.getLocalBookMarkCount(context);
				if (bookmarkCount <= 0 || !cursor.moveToFirst()) {
					args.mHandler.sendEmptyMessage(BookMarkBackupMsg.BOOKMARK_COUNT_ZERO);
					return;
				}
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
					bookMark.setCreated(cursor.getString(cursor
							.getColumnIndex(Browser.BookmarkColumns.CREATED)));
					bookMark.setVisits(cursor.getString(cursor
							.getColumnIndex(Browser.BookmarkColumns.VISITS)));
					bookMark.setFavicon(cursor.getBlob(cursor
							.getColumnIndex(Browser.BookmarkColumns.FAVICON)));
					bookMark.setTouchicon(cursor.getBlob(cursor.getColumnIndex("touch_icon")));
					bookMark.setThumbnail(cursor.getBlob(cursor.getColumnIndex("thumbnail")));
					//其实数据库没有这个folder字段，主要是为了兼容有folder字段的系统
					bookMark.setFolder("0");
					FileUtil.writeObject(context, bookMark, oos, args.mHandler);
				} while (cursor.moveToNext());
			} finally {
				cursor.close();
			}
			//最后把一个null写到文件组，当读文件的时候，如果为null，则表示读完文件
			FileUtil.writeObject(context, null, oos, args.mHandler);
			if (cursor != null) {
				cursor.close();
			}
			//备份完毕
			args.mHandler.sendEmptyMessage(BookMarkBackupMsg.BOOKMARK_BACKUP_END);
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
	/**
	 * <br>类描述:备份书签信息
	 * <br>功能详细描述:
	 *
	 * @author  jiangpeihe
	 * @date  [2012-10-11]
	 */
	public interface BookMarkBackupMsg {
		public static int BOOKMARK_BACKUP_START = 0x1001;
		public static int BOOKMARK_BACKUP_END = 0x1002;
		public static int BOOKMARK_BACKUP_PROCEEDING = 0x1003;
		public static int BOOKMARK_COUNT_ZERO = 0x1004;
		public static int BOOKMARK_BACKUP_ERROR_OCCUR = 0x1006;
	}

	/**
	 * <br>类描述:备份书签参数
	 * <br>功能详细描述:
	 *
	 * @author  jiangpeihe
	 * @date  [2012-10-11]
	 */
	public static class BookMarkBackupArgs {
		public Handler mHandler; // 必须字段
		public String mBackupFilePath; // 必须字段
	}

}
