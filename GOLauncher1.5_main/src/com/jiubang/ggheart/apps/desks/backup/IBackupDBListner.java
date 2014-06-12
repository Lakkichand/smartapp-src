package com.jiubang.ggheart.apps.desks.backup;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  ruxueqin
 * @date  [2012-12-20]
 */
public interface IBackupDBListner {

	abstract public void onExportPreExecute();
	
	abstract public void onExportPostExecute(int type, final String msg);
	
	abstract public void onImportPreExecute();
	
	abstract public void onImportPostExecute(int type, final String msg);
}
