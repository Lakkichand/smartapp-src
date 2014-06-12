/*
 * 文 件 名:  DataBaseReceiver.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  rongjinsong
 * 修改时间:  2012-9-11
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.data.DBImport;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.data.DataProvider;
import com.jiubang.ggheart.launcher.ICustomAction;

/**
 * <br>
 * 类描述: <br>
 * 功能详细描述:
 * 
 * @author rongjinsong
 * @date [2012-9-11]
 */
public class DataBaseReceiver extends BroadcastReceiver {

	/** {@inheritDoc} */

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction();
		final String pkg = intent.getStringExtra("pkg");
		if (pkg == null) {
			return;
		}
		if (action.equals(ICustomAction.ACTION_ADD_DB_READ_PERMISSION)
				&& pkg.equals(context.getPackageName())) {
			Intent it = null;
			if (!LauncherSelectorActivity.sImportDB
					&& DataProvider.getInstance(context).openWithWorldReadable()) {
				it = new Intent(ICustomAction.ACTION_REMOTE_DB_READ_PERMISSION_OK);
			} else {
				it = new Intent(ICustomAction.ACTION_REMOTE_DB_READ_PERMISSION_FAILED);
			}
			it.putExtra("pkg", context.getPackageName());
			context.sendBroadcast(it);
		} else if (action.equals(ICustomAction.ACTION_REMOVE_DB_READ_PERMISSION)
				&& pkg.equals(context.getPackageName())) {
			DataProvider.getInstance(context).openWithDefaultMode();
		} else if (action.equals(ICustomAction.ACTION_REMOTE_DB_READ_PERMISSION_OK)
				&& !pkg.equals(context.getPackageName()) && LauncherSelectorActivity.sImportDB) {
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IDiyMsgIds.IMPORT_OTHER_DB,
					-1, pkg, null);
		} else if (action.equals(ICustomAction.ACTION_REMOTE_DB_READ_PERMISSION_FAILED)
				&& !pkg.equals(context.getPackageName()) && LauncherSelectorActivity.sImportDB) {
			LauncherSelectorActivity.sImportDB = false;
			int duration = 600;
			Toast.makeText(context, R.string.db_import_failed, duration).show();
		}

	}
}
