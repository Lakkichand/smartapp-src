package com.jiubang.go.backup.pro.net.sync;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.RESTUtility;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.jiubang.go.backup.pro.util.Util;

/**
 * DropboxFile manager
 * 
 * @author maiyongshen
 */
public class DropboxFile implements OnlineFileInfo {

	private Entry mDropBoxEntry;
	private DropboxAPI<AndroidAuthSession> mDropboxAPI;

	public DropboxFile(Entry entry, DropboxAPI<AndroidAuthSession> dropboxAPI) {
		if (entry == null) {
			throw new IllegalArgumentException("invalide argument");
		}
		mDropBoxEntry = entry;
		mDropboxAPI = dropboxAPI;
	}

	@Override
	public String getFileName() {
		return mDropBoxEntry.fileName();
	}

	@Override
	public boolean isDirectory() {
		return mDropBoxEntry.isDir;
	}

	@Override
	public long getSize() {
		return mDropBoxEntry.bytes;
	}

	@Override
	public String hash() {
		return mDropBoxEntry.hash;
	}

	@Override
	public String getPath() {
		return mDropBoxEntry.path;
	}

	@Override
	public String getParentPath() {
		return mDropBoxEntry.parentPath();
	}

	@Override
	public String getRoot() {
		return mDropBoxEntry.root;
	}

	@Override
	public boolean exist() {
		return !mDropBoxEntry.isDeleted;
	}

	@Override
	public OnlineFileInfo[] listContent() {
		if (!isDirectory()) {
			return null;
		}
		if (Util.isCollectionEmpty(mDropBoxEntry.contents) && mDropboxAPI != null) {
			try {
				Entry entry = mDropboxAPI.metadata(getPath(), 0, null, true, null);
				mDropBoxEntry = entry;
			} catch (DropboxException e) {
				return null;
			}
		}
		List<Entry> contents = mDropBoxEntry.contents;
		List<OnlineFileInfo> files = new ArrayList<OnlineFileInfo>();
		for (Entry entry : contents) {
			files.add(new DropboxFile(entry, mDropboxAPI));
		}
		return files.toArray(new OnlineFileInfo[files.size()]);
	}

	@Override
	public Date lastModified() {
		return RESTUtility.parseDate(mDropBoxEntry.modified);
	}

	@Override
	public Date clientModifiedTime() {
		return RESTUtility.parseDate(mDropBoxEntry.clientMtime);
	}

	@Override
	public String getRevCode() {
		return mDropBoxEntry.rev;
	}

}
