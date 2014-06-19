package com.jiubang.go.backup.pro.bookmark;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.Browser;
import android.text.TextUtils;
import android.util.Log;

import com.jiubang.go.backup.pro.data.BookMarkBackupEntry;
import com.jiubang.go.backup.pro.util.Util;

/**
 * <br>类描述:2.x系统手机书签恢复
 * <br>功能详细描述:
 *
 * @author  jiangpeihe
 * @date  [2012-10-16]
 */
public class RestoreVersionTwo extends RestoreBookMark {
	private List<String> mBookMarkListFromDb = new ArrayList<String>();

	@Override
	public boolean restoreBookMark(Context context, BookMarkRestoreArgs args) {
		if (!ensureArgsValid(args)) {
			return false;
		}
		Cursor queryCursor = null;
		try {
			queryCursor = context.getContentResolver().query(Browser.BOOKMARKS_URI,
					BookMarkBackupEntry.BOOKMARK_COLUMNS_WITH_FOLDER, null, null, null);
		} catch (Exception e) {
			e.printStackTrace();
			try {
				queryCursor = context.getContentResolver().query(Browser.BOOKMARKS_URI,
						BookMarkBackupEntry.STARNDARD_BOOKMARK_COLUMNS, null, null, null);
			} catch (Exception e1) {
				e1.printStackTrace();
				args.mHandler.sendEmptyMessage(BookMarkRestoreMsg.BOOKMARK_RESTORE_ERROR_OCCUR);
			}
		}
		try {
			if (queryCursor == null  || queryCursor.getCount() == 0) {
				args.mHandler.sendEmptyMessage(BookMarkRestoreMsg.BOOKMARK_RESTORE_ERROR_OCCUR);
				return false;
			}
			final int folderIndex = queryCursor.getColumnIndex("folder");
			if (folderIndex > -1) {
				folderRestore(context, args);
			} else {
				noFolderRestore(context, args);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			args.mHandler.sendEmptyMessage(BookMarkRestoreMsg.BOOKMARK_RESTORE_SMS_COUNT_ZERO);
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

	public void folderRestore(Context context, BookMarkRestoreArgs args) {
		// 通知开始恢复书签
		args.mHandler.sendEmptyMessage(BookMarkRestoreMsg.BOOKMARK_RESTORE_START);
		Map<String, String> folderMap = new HashMap<String, String>();
		List<BookMark> bookMarkList = FileUtil.readObject(context, args);
		List<String> urlTitleList = new ArrayList<String>();
		//String detial = uriUri + uriTitle + uriCreated;
		List<String> urlBookMarkList = getUrlList(context);
		try {
			if (!Util.isCollectionEmpty(urlBookMarkList)) {
				urlTitleList.addAll(urlBookMarkList);
			}
			if (Util.isCollectionEmpty(bookMarkList)) {
				args.mHandler.sendEmptyMessage(BookMarkRestoreMsg.BOOKMARK_RESTORE_SMS_COUNT_ZERO);
				return;
			}
			int index = 0;
			final int count = BookMarkBackupEntry.calcValidBookmarkCount(bookMarkList);
			for (int i = 0; i < bookMarkList.size(); i++) {
				String bookmark = bookMarkList.get(i).getBookmark();
				String url = bookMarkList.get(i).getUrl();
				String title = bookMarkList.get(i).getTitle();
				String created = bookMarkList.get(i).getCreated();
				String bookmarkDetail = url + title + created;
				// 通知外部正在更新第几个会话
				if (bookmark.equals("1") && url != null && !url.equals("")) {
					index++;
					Message.obtain(args.mHandler, BookMarkRestoreMsg.BOOKMARK_RESTORE_PROCEEDING,
							index, count).sendToTarget();
				}
				//数据表还存在的文件夹或者书签
				if ((urlTitleList != null && urlTitleList.contains(bookmarkDetail))
						|| (mBookMarkListFromDb != null && mBookMarkListFromDb
								.contains(url + title))) {
					//记录文件夹的title+created ,id，这样不用每次都查询数据库
					if (url == null || url.equals("") || bookmark.equals("2")) {
						Cursor idCursor = context.getContentResolver().query(Browser.BOOKMARKS_URI,
								BookMarkBackupEntry.STARNDARD_BOOKMARK_COLUMNS,
								"title='" + title + "' and created='" + created + "'", null, null);
						idCursor.moveToNext();
						String uriId = idCursor.getString(idCursor
								.getColumnIndex(Browser.BookmarkColumns._ID));
						String titleCreated = title + created;
						folderMap.put(titleCreated, uriId);
						idCursor.close();
					} else if (bookmark.equals("1") && (url != null) && !url.equals("")) {
						String parentTitle = bookMarkList.get(i).getParentTitle();
						String parentCreated = bookMarkList.get(i).getParentCreated();
						String parenttitleCreated = parentTitle + parentCreated;
						//如果书签存在，但是已经其他文件夹了，那么就把书签的的folder重新设为id
						if (folderMap.containsKey(parenttitleCreated)) {
							String updateFolderId = folderMap.get(parenttitleCreated);
							ContentValues updateCv = new ContentValues();
							updateCv.put("folder", updateFolderId);
							context.getContentResolver().update(
									Browser.BOOKMARKS_URI,
									updateCv,
									"title='" + title + "' and url='" + url + "' and created='"
											+ created + "'", null);
						}
					}
					continue;
				}
				//如果文件夹和书签已删除
				String date = bookMarkList.get(i).getDate();
				String visits = bookMarkList.get(i).getVisits();
				String folder = bookMarkList.get(i).getFolder();
				byte[] favicon = bookMarkList.get(i).getFavicon();
				byte[] thumbnail = bookMarkList.get(i).getThumbnail();
				byte[] touchicon = bookMarkList.get(i).getTouchicon();

				ContentValues markcv = new ContentValues();
				markcv.put(Browser.BookmarkColumns.BOOKMARK, bookmark);
				markcv.put(Browser.BookmarkColumns.TITLE, title);
				if (url != null || !TextUtils.isEmpty(url)) {
					markcv.put(Browser.BookmarkColumns.URL, url);
				}
				if (created != null || !TextUtils.isEmpty(created)) {
					markcv.put(Browser.BookmarkColumns.CREATED, created);
				}
				if (date != null || !TextUtils.isEmpty(date)) {
					markcv.put(Browser.BookmarkColumns.DATE, date);
				}
				//				if (visits != null || !TextUtils.isEmpty(visits)) {
				//					markcv.put(Browser.BookmarkColumns.VISITS, bookMarkList.get(i).getVisits());
				//				} else {
				markcv.put(Browser.BookmarkColumns.VISITS, "0");
				//				}
				if (favicon != null) {
					markcv.put(Browser.BookmarkColumns.FAVICON, bookMarkList.get(i).getFavicon());
				}
				if (thumbnail != null) {
					markcv.put("thumbnail", bookMarkList.get(i).getThumbnail());
				}
				if (touchicon != null) {
					markcv.put("touch_icon", bookMarkList.get(i).getTouchicon());
				}
				String titleCreated = title + created;
				//根目录里
				if (folder == null || folder.equals("0")) {
					markcv.put("folder", "0");
					Uri boomarkUri = context.getContentResolver().insert(Browser.BOOKMARKS_URI,
							markcv);
					if (url == null || url.equals("") || bookmark.equals("2")) {
						Long id = ContentUris.parseId(boomarkUri);
						String folderId = String.valueOf(id);
						folderMap.put(titleCreated, folderId);
					} else if (bookmark.equals("1") && url != null && !url.equals("")) {
						mBookMarkListFromDb.add(url + title);
					}
				} else {
					String parentTitle = bookMarkList.get(i).getParentTitle();
					String parentCreated = bookMarkList.get(i).getParentCreated();
					String parenttitleCreated = parentTitle + parentCreated;
					if (folderMap.containsKey(parenttitleCreated)) {
						String folderId = folderMap.get(parenttitleCreated);
						markcv.put("folder", folderId);
					} else {
						Log.d("Restore", "父文件夹还没有插进去");
					}
					Uri boomarkUri = context.getContentResolver().insert(Browser.BOOKMARKS_URI,
							markcv);
					if (url == null || url.equals("") || bookmark.equals("2")) {
						Long id = ContentUris.parseId(boomarkUri);
						String folderId = String.valueOf(id);
						folderMap.put(titleCreated, folderId);
					} else if (bookmark.equals("1") && url != null && !url.equals("")) {
						mBookMarkListFromDb.add(url + title);
					}
				}
			}
			//恢复结束
			args.mHandler.sendEmptyMessage(BookMarkRestoreMsg.BOOKMARK_RESTORE_END);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (mBookMarkListFromDb != null) {
				mBookMarkListFromDb.clear();
			}
			if (folderMap != null) {
				folderMap.clear();
			}
			if (urlTitleList != null) {
				urlTitleList.clear();
			}
			if (urlBookMarkList != null) {
				urlBookMarkList.clear();
			}
		}
	}
	//没有Folder字段
	public void noFolderRestore(Context context, BookMarkRestoreArgs args) {
		// 通知开始恢复书签
		args.mHandler.sendEmptyMessage(BookMarkRestoreMsg.BOOKMARK_RESTORE_START);
		List<BookMark> bookMarkList = FileUtil.readObject(context, args);
		List<String> urlTitleList = new ArrayList<String>();
		List<String> bookmarkContailsList = new ArrayList<String>();
		getUrlList(context);
		try {
			if (Util.isCollectionEmpty(bookMarkList)) {
				args.mHandler.sendEmptyMessage(BookMarkRestoreMsg.BOOKMARK_RESTORE_SMS_COUNT_ZERO);
				return;
			}

			int count = BookMarkBackupEntry.calcValidBookmarkCount(bookMarkList);
			int index = 0;
			for (int i = 0; i < bookMarkList.size(); i++) {
				String title = bookMarkList.get(i).getTitle();
				String uri = bookMarkList.get(i).getUrl();
				String created = bookMarkList.get(i).getCreated();
				String bookmark = bookMarkList.get(i).getBookmark();
				String visits = bookMarkList.get(i).getVisits();
				String uriTitle = uri + title;
				// 通知外部正在更新第几个会话

				if (bookmark.equals("1") && (uri != null) && !uri.equals("")) {
					index++;
					Message.obtain(args.mHandler, BookMarkRestoreMsg.BOOKMARK_RESTORE_PROCEEDING,
							index, count);
				}
				if (!bookmark.equals("1")
						|| mBookMarkListFromDb.contains(uriTitle)
						|| ((bookmarkContailsList != null) && bookmarkContailsList
								.contains(uriTitle))) {
					continue;
				}
				ContentValues cv = new ContentValues();
				cv.put(Browser.BookmarkColumns.TITLE, title);
				cv.put(Browser.BookmarkColumns.URL, uri);
				cv.put(Browser.BookmarkColumns.DATE, bookMarkList.get(i).getDate());
				cv.put(Browser.BookmarkColumns.CREATED, created);
				//				if (visits != null && !TextUtils.isEmpty(visits)) {
				//					cv.put(Browser.BookmarkColumns.VISITS, visits);
				//				} else {
				cv.put(Browser.BookmarkColumns.VISITS, "0");
				//				}
				cv.put(Browser.BookmarkColumns.FAVICON, bookMarkList.get(i).getFavicon());
				cv.put(Browser.BookmarkColumns.BOOKMARK, "1");
				cv.put("thumbnail", bookMarkList.get(i).getThumbnail());
				cv.put("touch_icon", bookMarkList.get(i).getTouchicon());
				context.getContentResolver().insert(Browser.BOOKMARKS_URI, cv);
				bookmarkContailsList.add(uriTitle);
			}
			//恢复结束
			args.mHandler.sendEmptyMessage(BookMarkRestoreMsg.BOOKMARK_RESTORE_END);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (mBookMarkListFromDb != null) {
				mBookMarkListFromDb.clear();
			}
			if (bookmarkContailsList != null) {
				bookmarkContailsList.clear();
			}
			if (urlTitleList != null) {
				urlTitleList.clear();
			}
		}

	}
	/** <br>功能简述:查询现在手机的书签信息，为了去重
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @return
	 */
	public List<String> getUrlList(Context context) {
		List<String> folderlList = new ArrayList<String>();
		Cursor cursor = null;
		try {
			cursor = context.getContentResolver().query(
					Browser.BOOKMARKS_URI,
					new String[] { Browser.BookmarkColumns.TITLE, Browser.BookmarkColumns.URL,
							Browser.BookmarkColumns.BOOKMARK, Browser.BookmarkColumns._ID,
							Browser.BookmarkColumns.CREATED, "folder" },
					Browser.BookmarkColumns.BOOKMARK + "<>0", null, null);
		} catch (Exception e) {
			e.printStackTrace();
			cursor = null;
		}
		if (cursor == null || cursor.getCount() == 0) {
			try {
				cursor = context.getContentResolver().query(
						Browser.BOOKMARKS_URI,
						new String[] { Browser.BookmarkColumns.TITLE, Browser.BookmarkColumns.URL,
								Browser.BookmarkColumns.BOOKMARK, Browser.BookmarkColumns._ID,
								Browser.BookmarkColumns.CREATED },
						Browser.BookmarkColumns.BOOKMARK + "<>0", null, null);
			} catch (Exception e) {
				cursor = null;
				e.printStackTrace();
			}
		}
		if (cursor == null || cursor.getCount() == 0) {
			return null;
		}
		try {
			if (cursor.getCount() <= 0 || !cursor.moveToFirst()) {
				return null;
			}

			int urlIndex = cursor.getColumnIndex(Browser.BookmarkColumns.URL);
			int titleIndex = cursor.getColumnIndex(Browser.BookmarkColumns.TITLE);
			int bookmarkIndex = cursor.getColumnIndex(Browser.BookmarkColumns.BOOKMARK);
			int createdIndex = cursor.getColumnIndex(Browser.BookmarkColumns.CREATED);
			ContentResolver resolver = context.getContentResolver();
			do {
				String uriUri = cursor.getString(urlIndex);
				String uriTitle = cursor.getString(titleIndex);
				String uriCreated = "0";
				String bookmark = cursor.getString(bookmarkIndex);
				if (bookmark.equals("1") && uriUri != null && !uriUri.equals("")) {
					String uriAddTitle = uriUri + uriTitle;
					if (mBookMarkListFromDb != null) {
						if (mBookMarkListFromDb.contains(uriAddTitle)) {
							//删除重复的
							String id = cursor.getString(cursor
									.getColumnIndex(Browser.BookmarkColumns._ID));
							int deleteId = Integer.parseInt(id);
							resolver.delete(Browser.BOOKMARKS_URI, "_id=" + deleteId, null);
							continue;
						}
						mBookMarkListFromDb.add(uriAddTitle);
					}
				} else {
					if (createdIndex > -1) {
						uriCreated = cursor.getString(createdIndex);
					}
					String detial = uriUri + uriTitle + uriCreated;
					if (detial != null || !detial.equals("")) {
						folderlList.add(detial);
					}
				}
			} while (cursor.moveToNext());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return folderlList;
	}

	/**
	 * <br>类描述:备份书签参数
	 * <br>功能详细描述:
	 *
	 * @author  jiangpeihe
	 * @date  [2012-10-11]
	 */
	public interface BookMarkRestoreMsg {
		public static int BOOKMARK_RESTORE_START = 0x2001;
		public static int BOOKMARK_RESTORE_END = 0x2002;
		public static int BOOKMARK_RESTORE_PROCEEDING = 0x2003;
		public static int BOOKMARK_RESTORE_SMS_COUNT_ZERO = 0x2004;
		public static int BOOKMARK_RESTORE_USER_CANCEL = 0x2005;
		public static int BOOKMARK_RESTORE_ERROR_OCCUR = 0x2006;
		public static int BOOKMARK_RESTORE_FILE_NOT_EXIT = 0x2007;
	}

	/**
	 * <br>类描述:备份书签参数
	 * <br>功能详细描述:
	 *
	 * @author  jiangpeihe
	 * @date  [2012-10-11]
	 */
	public static class BookMarkRestoreArgs {
		public Handler mHandler;
		public String mRestoreFilePath;
	}

}
