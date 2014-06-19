package com.jiubang.go.backup.pro.net.sync;

import java.util.Date;

/**
 * 网盘文件信息接口
 * 
 * @author maiyongshen
 */
public interface OnlineFileInfo {

	public String getFileName();

	public boolean isDirectory();

	public long getSize();

	public String hash();

	public String getPath();

	public String getParentPath();

	public String getRoot();

	public boolean exist();

	public Date lastModified();

	public Date clientModifiedTime();

	public OnlineFileInfo[] listContent();

	public String getRevCode();
}
