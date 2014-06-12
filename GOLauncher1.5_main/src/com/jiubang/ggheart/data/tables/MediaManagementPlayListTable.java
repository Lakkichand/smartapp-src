package com.jiubang.ggheart.data.tables;

/**
 * 播放列表表格
 * 
 * @author huangshaotao
 * 
 */
public class MediaManagementPlayListTable {
	public static final String TABLENAME = "playlisttable";

	public static final String ID = "id";
	public static final String TYPE = "type";// 类型，所有，最近播放，最近添加，自定义
	public static final String NAME = "name";
	public static final String CDATE = "cdate";// 创建日期
	public static final String UDATE = "udate";// 更新日期

	public static String CREATETABLESQL = "create table " + TABLENAME + "(" + ID + " INTEGER, "
			+ TYPE + " INTEGER, " + NAME + " TEXT, " + CDATE + " INTEGER, " + UDATE + " INTEGER "
			+ ");";

	// public static void initDatas(Context context, SQLiteDatabase db){
	// String all = "insert into "+TABLENAME+" values(" +
	// "null,"+
	// PlayListInfo.TYPE_ALL+","+
	// "'"+context.getString(R.string.playlist_all)+"',"+
	// System.currentTimeMillis()/1000+","+
	// System.currentTimeMillis()/1000+
	// ");";
	// String rp = "insert into "+TABLENAME+" values(" +
	// "null,"+
	// PlayListInfo.TYPE_RECENT_PLAY+","+
	// "'"+context.getString(R.string.playlist_recent_play)+"',"+
	// System.currentTimeMillis()/1000+","+
	// System.currentTimeMillis()/1000+
	// ");";
	// String ra = "insert into "+TABLENAME+" values(" +
	// "null,"+
	// PlayListInfo.TYPE_RECENT_ADD+","+
	// "'"+context.getString(R.string.playlist_recent_add)+"',"+
	// System.currentTimeMillis()/1000+","+
	// System.currentTimeMillis()/1000+
	// ");";
	// String add = "insert into "+TABLENAME+" values(" +
	// "null,"+
	// PlayListInfo.TYPE_NEW+","+
	// "'"+context.getString(R.string.playlist_new)+"',"+
	// System.currentTimeMillis()/1000+","+
	// System.currentTimeMillis()/1000+
	// ");";
	// db.execSQL(all);
	// db.execSQL(rp);
	// db.execSQL(ra);
	// db.execSQL(add);
	// };
}
