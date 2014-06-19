package com.jiubang.go.backup.pro.net.sync;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;
import com.jiubang.go.backup.pro.util.Util;
/**
 * 
 * @author ReyZhang
 *
 */
public class GoogleDriveFile implements OnlineFileInfo {
	/**
	 * ID of file.
	 */
	public String resourceid;

	/**
	 * Title of file.
	 */
	public String title;

	/**
	 * Description of file.
	 */
	public String root;

	/**
	 * downloadURL
	 */
	public String downloadURL;

	/**
	 * parents.
	 */
	public List<ParentReference> parents;

	/**
	 * file size
	 */
	public Long size;

	/**
	 * isExist
	 */
	public boolean isExist;
	
	/**
	 * md5checksumCode
	 */
	public String md5CheckSumCode;

	public boolean isDir;

	public static String sFolderMimeType = "application/vnd.google-apps.folder";
	
	private GoogleDriveManager mDrive;
	private List<OnlineFileInfo> mChildren = null;

	public GoogleDriveFile(File file, GoogleDriveManager drive) {
		this.resourceid = file.getId();
		this.title = file.getTitle();
		this.downloadURL = file.getDownloadUrl();
		this.parents = file.getParents();
		this.size = file.getFileSize();
		Boolean trashedFlag = file.getExplicitlyTrashed();
		this.isExist = trashedFlag == null ? true : trashedFlag;
		this.isDir = file.getMimeType().equals(sFolderMimeType);
		this.md5CheckSumCode = file.getMd5Checksum();
		mDrive = drive;
	}

	public File toFile() {
		File file = new File();
		file.setId(resourceid);
		file.setTitle(title);
		file.setDownloadUrl(downloadURL);
		file.setParents(parents);
		file.setFileSize(size);
		file.setMd5Checksum(md5CheckSumCode);
		return file;
	}

	@Override
	public String getFileName() {
		return this.title;
	}

	@Override
	public boolean isDirectory() {
		return isDir;
	}

	@Override
	public long getSize() {
		return this.size;
	}

	@Override
	public String hash() {
		return md5CheckSumCode;
	}

	@Override
	public String getPath() {
		return this.downloadURL;
	}

	@Override
	public String getParentPath() {
		return null;
	}

	@Override
	public String getRoot() {
		return null;
	}

	@Override
	public boolean exist() {
		return isExist;
	}

	@Override
	public Date lastModified() {
		return null;
	}

	@Override
	public Date clientModifiedTime() {
		return null;
	}

	@Override
	public OnlineFileInfo[] listContent() {
		if (!isDirectory()) {
			return null;
		}
		if (!Util.isCollectionEmpty(mChildren)) {
			return mChildren.toArray(new OnlineFileInfo[mChildren.size()]);
		}
		
		if (mDrive != null) {
			List<File> children = mDrive.listAllChildren(resourceid);
			if (!Util.isCollectionEmpty(children)) {
				mChildren = new ArrayList<OnlineFileInfo>();
				for (File child : children) {
					mChildren.add(new GoogleDriveFile(child, mDrive));
				}
			}
		}
		return Util.isCollectionEmpty(mChildren) ? null : mChildren.toArray(new OnlineFileInfo[mChildren.size()]);
	}

	@Override
	public String getRevCode() {
		return md5CheckSumCode;
	}

}
