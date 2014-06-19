package com.jiubang.go.backup.pro.bookmark;

import android.content.Context;

import com.jiubang.go.backup.pro.bookmark.BackupTwo.BookMarkBackupArgs;

/**
 * <br>类描述:备份抽象类
 * <br>功能详细描述:
 *
 * @author  jiangpeihe
 * @date  [2012-10-17]
 */
public interface BookMarkOperate {

	public abstract void backupBookMark(Context context, BookMarkBackupArgs args);

}
