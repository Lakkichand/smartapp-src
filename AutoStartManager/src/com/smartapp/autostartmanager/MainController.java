package com.smartapp.autostartmanager;

import java.util.List;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.ta.TAApplication;
import com.ta.mvc.command.TACommand;

/**
 * 逻辑控制器
 */
public class MainController extends TACommand {

	@Override
	protected void executeCommand() {
		Intent intent = new Intent(Intent.ACTION_BOOT_COMPLETED);
		List<ResolveInfo> resolveInfoList = TAApplication
				.getApplication()
				.getPackageManager()
				.queryBroadcastReceivers(intent,
						PackageManager.GET_DISABLED_COMPONENTS);
		
	}

}
