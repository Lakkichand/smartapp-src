package com.jiubang.go.backup.pro.bookmark;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;

import com.jiubang.go.backup.pro.bookmark.RestoreVersionTwo.BookMarkRestoreArgs;
import com.jiubang.go.backup.pro.bookmark.RestoreVersionTwo.BookMarkRestoreMsg;
/**
 * <br>类描述:从文件夹读写对象
 * <br>功能详细描述:
 *
 * @author  jiangpeihe
 * @date  [2012-10-17]
 */
public class FileUtil {

	//把Bookmark对象写到文件里
	public static void writeObject(Context context, BookMark bookMark, ObjectOutputStream oos,
			Handler handler) {
		try {
			oos.writeObject(bookMark);
		} catch (IOException e) {
			e.printStackTrace();
			handler.sendEmptyMessage(BookMarkRestoreMsg.BOOKMARK_RESTORE_ERROR_OCCUR);
		}
	}

	//从文件里读出对象
	public static List<BookMark> readObject(Context context, BookMarkRestoreArgs args) {
		List<BookMark> bookMarkList = new ArrayList<BookMark>();
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new FileInputStream(new File(args.mRestoreFilePath)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			args.mHandler.sendEmptyMessage(BookMarkRestoreMsg.BOOKMARK_RESTORE_FILE_NOT_EXIT);
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			args.mHandler.sendEmptyMessage(BookMarkRestoreMsg.BOOKMARK_RESTORE_ERROR_OCCUR);
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			args.mHandler.sendEmptyMessage(BookMarkRestoreMsg.BOOKMARK_RESTORE_ERROR_OCCUR);
			return null;
		}
		try {
			Object obj = null;
			BookMark bookmark = new BookMark();
			while ((obj = ois.readObject()) != null) {
				bookmark = (BookMark) obj;
				bookMarkList.add(bookmark);

			}
		} catch (Exception e) {
			e.printStackTrace();
			args.mHandler.sendEmptyMessage(BookMarkRestoreMsg.BOOKMARK_RESTORE_ERROR_OCCUR);
			return bookMarkList;
		} finally {
			try {
				ois.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return bookMarkList;
	}
}
