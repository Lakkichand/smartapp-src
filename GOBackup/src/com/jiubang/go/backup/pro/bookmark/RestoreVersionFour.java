package com.jiubang.go.backup.pro.bookmark;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Message;
import android.provider.Browser;
import android.util.Log;

import com.jiubang.go.backup.pro.bookmark.RestoreVersionTwo.BookMarkRestoreArgs;
import com.jiubang.go.backup.pro.bookmark.RestoreVersionTwo.BookMarkRestoreMsg;
import com.jiubang.go.backup.pro.data.BookMarkBackupEntry;
import com.jiubang.go.backup.pro.util.Util;

/**
 * <br>类描述:4.x手机的恢复
 * <br>功能详细描述:
 *
 * @author  jiangpeihe
 * @date  [2012-10-16]
 */
public class RestoreVersionFour extends RestoreBookMark {

	@Override
	public boolean restoreBookMark(Context context, BookMarkRestoreArgs agrs) {

		if (!ensureArgsValid(agrs)) {
			return false;
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
				queryCursor = null;
				e1.printStackTrace();
				agrs.mHandler.sendEmptyMessage(BookMarkRestoreMsg.BOOKMARK_RESTORE_ERROR_OCCUR);
			}
		}
		try {
			if (queryCursor == null || queryCursor.getCount() == 0) {
				agrs.mHandler.sendEmptyMessage(BookMarkRestoreMsg.BOOKMARK_RESTORE_ERROR_OCCUR);
				return false;
			}
			final int folderIndex = queryCursor.getColumnIndex("folder");
			if (folderIndex > -1) {
				folderRestore(context, agrs);
			} else {
				noFolderRestore(context, agrs);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			agrs.mHandler.sendEmptyMessage(BookMarkRestoreMsg.BOOKMARK_RESTORE_SMS_COUNT_ZERO);
		} finally {
			if (queryCursor != null) {
				queryCursor.close();
			}
		}
		return true;

	}

	private boolean ensureArgsValid(BookMarkRestoreArgs args) {
		if (args == null) {
			return false;
		}
		if (args.mHandler == null || args.mRestoreFilePath == null) {
			return false;
		}
		return true;
	}

	public void folderRestore(Context context, BookMarkRestoreArgs agrs) {
		Log.d("Gobackup", "没有文件夹的备份还没有扩展");
	}

	/** <br>功能简述:没有Folder字段恢复
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @param args
	 */
	public void noFolderRestore(Context context, BookMarkRestoreArgs args) {
		// 通知开始恢复书签
		List<BookMark> bookMarkList = null;
		List<String> urlTitleList = null;
		args.mHandler.sendEmptyMessage(BookMarkRestoreMsg.BOOKMARK_RESTORE_START);
		Cursor cursor = context.getContentResolver().query(Browser.BOOKMARKS_URI,
				BookMarkBackupEntry.STARNDARD_BOOKMARK_COLUMNS, null, null, null);

		int created_Index = -1;
		if (cursor != null) {
			created_Index = cursor.getColumnIndex(Browser.BookmarkColumns.CREATED);
			cursor.close();
		}
		try {
			bookMarkList = FileUtil.readObject(context, args);
			if (Util.isCollectionEmpty(bookMarkList)) {
				args.mHandler.sendEmptyMessage(BookMarkRestoreMsg.BOOKMARK_RESTORE_SMS_COUNT_ZERO);
				return;
			}
			urlTitleList = new ArrayList<String>();
			urlTitleList = getUrlList(context);
			int index = 0;
			final int count = BookMarkBackupEntry.calcValidBookmarkCount(bookMarkList);
			for (int i = 0; i < bookMarkList.size(); i++) {
				String title = bookMarkList.get(i).getTitle();
				String uri = bookMarkList.get(i).getUrl();
				String bookmark = bookMarkList.get(i).getBookmark();
				String visits = bookMarkList.get(i).getVisits();
				String uriTitle = uri + title;
				// 通知外部正在更新第几个会话
				if (bookmark.equals("1") && uri != null && !uri.equals("")) {
					index++;
					Message.obtain(args.mHandler, BookMarkRestoreMsg.BOOKMARK_RESTORE_PROCEEDING,
							index, count);
				}
				if (uri == null || uri.equals("") || !bookmark.equals("1")
						|| ((urlTitleList != null) && urlTitleList.contains(uriTitle))) {
					continue;
				}
				ContentValues cv = new ContentValues();
				cv.put(Browser.BookmarkColumns.TITLE, title);
				cv.put(Browser.BookmarkColumns.URL, uri);
				cv.put(Browser.BookmarkColumns.DATE, bookMarkList.get(i).getDate());
				//				if (visits != null && !TextUtils.isEmpty(visits)) {
				//					cv.put(Browser.BookmarkColumns.VISITS, visits);
				//				} else {
				cv.put(Browser.BookmarkColumns.VISITS, "0");
				//				}
				cv.put(Browser.BookmarkColumns.FAVICON, bookMarkList.get(i).getFavicon());
				cv.put(Browser.BookmarkColumns.BOOKMARK, "1");
				if (created_Index > -1) {
					cv.put(Browser.BookmarkColumns.CREATED, bookMarkList.get(i).getCreated());
				}
				cv.put("thumbnail", bookMarkList.get(i).getThumbnail());
				cv.put("touch_icon", bookMarkList.get(i).getTouchicon());
				context.getContentResolver().insert(Browser.BOOKMARKS_URI, cv);
				//记住插入的书签
				if (urlTitleList == null) {
					urlTitleList = new ArrayList<String>();
				}
				urlTitleList.add(uriTitle);
			}
			//恢复结束
			args.mHandler.sendEmptyMessage(BookMarkRestoreMsg.BOOKMARK_RESTORE_END);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (bookMarkList != null) {
				bookMarkList.clear();
			}
			if (urlTitleList != null) {
				urlTitleList.clear();
			}
		}
	}
	/** <br>功能简述:从手机的数据库看看书签的信息，主用是为了去重
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @return
	 */
	public List<String> getUrlList(Context context) {
		List<String> mUrlList = new ArrayList<String>();
		Cursor cursor = null;
		try {
			cursor = context.getContentResolver().query(
					Browser.BOOKMARKS_URI,
					new String[] { Browser.BookmarkColumns.TITLE, Browser.BookmarkColumns.URL,
							Browser.BookmarkColumns.BOOKMARK, Browser.BookmarkColumns._ID },
					Browser.BookmarkColumns.BOOKMARK + "<>0", null, null);
			if (cursor == null) {
				return null;
			}
			if (cursor.getCount() <= 0 || !cursor.moveToFirst()) {
				cursor.close();
				return null;
			}
			
			int urlIndex = cursor.getColumnIndex(Browser.BookmarkColumns.URL);
			int titleIndex = cursor.getColumnIndex(Browser.BookmarkColumns.TITLE);
			int bookmarkIndex = cursor.getColumnIndex(Browser.BookmarkColumns.BOOKMARK);
			final ContentResolver resolver = context.getContentResolver();
			do {
				String uriUri = cursor.getString(urlIndex);
				String uriTitle = cursor.getString(titleIndex);
				String detial = uriUri + uriTitle;
				if (mUrlList.contains(detial)) {
					//删除重复的
					String id = cursor
							.getString(cursor.getColumnIndex(Browser.BookmarkColumns._ID));
					int deleteId = Integer.parseInt(id);
					resolver.delete(Browser.BOOKMARKS_URI, "_id=" + deleteId, null);
					continue;
				}
				mUrlList.add(detial);
			} while (cursor.moveToNext());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return mUrlList;
	}

}
