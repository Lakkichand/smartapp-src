package com.jiubang.ggheart.data.tables;

public class MediaManagementPlayListFileTable {

	public static final String TABLENAME = "playlistfiletable";

	public static final String PLAYLISTID = "playlistid";
	public static final String FILEID = "fileid";
	public static final String DATE = "date";

	public static String CREATETABLESQL = "create table " + TABLENAME + "(" + PLAYLISTID
			+ " INTEGER, " + FILEID + " INTEGER, " + DATE + " INTEGER " + ");";
}
