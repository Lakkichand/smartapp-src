package com.youle.gamebox.ui.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.widget.Toast;
import com.youle.gamebox.ui.util.LOGUtil;

public class PackageReceiver extends BroadcastReceiver {
	private final static String PACKAGE_ADDED = "android.intent.action.PACKAGE_ADDED";
    public final static int PACKAGE_ADDED_WHAT =10001;
	private final static String PACKAGE_REMOVED = "android.intent.action.PACKAGE_REMOVED";
    public final static int PACKAGE_REMOVED_WHAT =10002;
    public final static String PACKAGE_KEY = "packName";
	private static PackageReceiver packageReceiver = null;
	public static Handler handler = null;
	private String str = "";

	public static Handler getHandler() {
		return handler;
	}

	public static void setHandler(Handler handler) {
		PackageReceiver.handler = handler;
	}

    public static PackageReceiver getInstance(){
        if(packageReceiver ==null){
            packageReceiver = new PackageReceiver();
        }
        return packageReceiver;
    }

	public  void registPackReceiver(Context context) {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(PACKAGE_ADDED);
		intentFilter.addAction(PACKAGE_REMOVED);
        intentFilter.addDataScheme("package");
		context.registerReceiver(packageReceiver, intentFilter);
	}

	public  void unRegistPackReceiver(Context context) {
		if (packageReceiver != null) {
			context.unregisterReceiver(packageReceiver);
			handler = null;
		}

	}

	@Override
	public void onReceive(Context context, Intent intent) {
		LOGUtil.d("Xjun", "onReceive ...");
        Toast.makeText(context,"onReceive",Toast.LENGTH_LONG).show();
		if (intent.getAction().equals(PACKAGE_ADDED)) {
			str = intent.getDataString().substring(8);
			if (handler != null) {
				Bundle bundle = new Bundle();
				bundle.putString(PACKAGE_KEY, str);
				Message message = new Message();
				message.obj = bundle;
				message.what = PACKAGE_ADDED_WHAT;
				handler.sendMessage(message);
			}
		} else if (intent.getAction().equals(PACKAGE_REMOVED)) {
			str = intent.getDataString().substring(8);
			if (handler != null) {
				Bundle bundle = new Bundle();
				bundle.putString(PACKAGE_KEY, str);
				Message message = new Message();
				message.obj = bundle;
				message.what = PACKAGE_REMOVED_WHAT;
				handler.sendMessage(message);
			}
		}

	}

}
