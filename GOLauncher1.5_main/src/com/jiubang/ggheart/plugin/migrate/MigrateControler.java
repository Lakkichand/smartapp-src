package com.jiubang.ggheart.plugin.migrate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;

import com.jiubang.ggheart.apps.desks.diy.IRequestCodeIds;
import com.jiubang.ggheart.apps.desks.diy.frames.drag.ISelfObject;
import com.jiubang.ggheart.launcher.ICustomAction;

public class MigrateControler implements ISelfObject {
	private Context mContext;
	private MigrateReceiver mReceiver;

	public MigrateControler(Context context) {
		mContext = context;
		selfConstruct();
	}

	@Override
	public void selfConstruct() {
		register();
	}

	@Override
	public void selfDestruct() {
		unregister();
		mContext = null;
	}

	private void register() {
		if (null == mReceiver) {
			IntentFilter filter = new IntentFilter();
			filter.addAction(ICustomAction.ACTION_DESK_MIGRATE_PREPARED);

			mReceiver = new MigrateReceiver();

			try {
				mContext.registerReceiver(mReceiver, filter);
			} catch (Exception e) {
				// 注册异常
				e.printStackTrace();
			}
		}
	}

	private void unregister() {
		if (null != mReceiver) {
			try {
				mContext.unregisterReceiver(mReceiver);
			} catch (Exception e) {
				e.printStackTrace();
			}
			mReceiver = null;
		}
	}

	class MigrateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (null != intent) {
				Bundle bundle = intent.getExtras();
				if (null != bundle) {
					int code = bundle.getInt("code");
					if (1000 == code || IRequestCodeIds.REQUEST_MIGRATE_DESK == code) {
						String uriStr = bundle.getString("uri");
						if (null != uriStr) {
							Uri uri = Uri.parse(uriStr);
							MigrateIntoDesk migtate = new MigrateIntoDesk(mContext, uri,
									IRequestCodeIds.REQUEST_MIGRATE_DESK == code);
							migtate.startMigrate();
						}
					}
				}
			}
		}
	}
}
