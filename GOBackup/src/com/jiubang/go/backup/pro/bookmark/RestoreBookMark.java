package com.jiubang.go.backup.pro.bookmark;

import android.content.Context;

import com.jiubang.go.backup.pro.bookmark.RestoreVersionTwo.BookMarkRestoreArgs;

/**
 * <br>类描述:恢复备份的抽象类
 * <br>功能详细描述:
 *
 * @author  jiangpeihe
 * @date  [2012-10-17]
 */
public abstract class RestoreBookMark {
	public abstract boolean restoreBookMark(Context context, BookMarkRestoreArgs agrs);

}
