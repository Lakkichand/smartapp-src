package com.jiubang.go.backup.pro.bookmark;

import java.io.Serializable;
import java.util.List;

import android.text.TextUtils;

import com.jiubang.go.backup.pro.util.Util;

/**
 * <br>类描述:用来存储书签信息的类
 *
 * @author  jiangpeihe
 * @date  [2012-10-16]
 */
public class BookMark implements Serializable {
	private static final long serialVersionUID = -2842623291225301706L;
	public static final String TYPE_VALID_BOOKMARK = "1";

	private String mTitle;
	private String mUrl;
	private String mVisits;
	private String mDate;
	private String mCreated;
	private String mBookmark;
	private String mFolder;
	private String mParentCreated;
	private String mParentTitle;
	private byte[] mFavicon;
	private byte[] mThumbnail;
	private byte[] mTouchicon;

	public String getFolder() {
		return mFolder;
	}

	public void setFolder(String folder) {
		mFolder = folder;
	}

	public String getParentCreated() {
		return mParentCreated;
	}

	public void setParentCreated(String parentCreated) {
		mParentCreated = parentCreated;
	}

	public String getParentTitle() {
		return mParentTitle;
	}

	public void setParentTitle(String parentTitle) {
		mParentTitle = parentTitle;
	}

	public String getCreated() {
		return mCreated;
	}

	public void setCreated(String created) {
		mCreated = created;
	}

	public byte[] getFavicon() {
		return mFavicon;
	}

	public void setFavicon(byte[] favicon) {
		mFavicon = favicon;
	}

	public byte[] getThumbnail() {
		return mThumbnail;
	}

	public void setThumbnail(byte[] thumbnail) {
		mThumbnail = thumbnail;
	}

	public byte[] getTouchicon() {
		return mTouchicon;
	}

	public void setTouchicon(byte[] touchicon) {
		mTouchicon = touchicon;
	}

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String title) {
		mTitle = title;
	}

	public String getUrl() {
		return mUrl;
	}

	public void setUrl(String url) {
		mUrl = url;
	}

	public String getVisits() {
		return mVisits;
	}

	public void setVisits(String visits) {
		mVisits = visits;
	}

	public String getDate() {
		return mDate;
	}

	public void setDate(String date) {
		mDate = date;
	}

	public String getBookmark() {
		return mBookmark;
	}

	public void setBookmark(String bookmark) {
		mBookmark = bookmark;
	}

	public boolean isValidBookmark() {
		return TextUtils.equals(getBookmark(), TYPE_VALID_BOOKMARK);
	}

	public static int calcValidBookmarkCount(List<BookMark> bookmarks) {
		if (Util.isCollectionEmpty(bookmarks)) {
			return 0;
		}
		int count = 0;
		for (BookMark bookmark : bookmarks) {
			if (bookmark.isValidBookmark()) {
				count++;
			}
		}
		return count;
	}

}
