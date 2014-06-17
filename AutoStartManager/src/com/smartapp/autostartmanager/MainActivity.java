package com.smartapp.autostartmanager;

import android.app.Activity;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		String action = "";
		action = "android.intent.action.BOOT_COMPLETED";

		action = "android.intent.action.PACKAGE_CHANGED";
		action = "android.intent.action.PACKAGE_RESTARTED";
		action = "android.intent.action.PACKAGE_REMOVED";
		action = "android.net.conn.CONNECTIVITY_CHANGE";
		action = "android.net.wifi.WIFI_STATE_CHANGED";
		action = "android.net.wifi.STATE_CHANGE";
		action = "android.intent.action.MEDIA_EJECT";
		action = "android.intent.action.USER_PRESENT";
		action = "android.intent.action.PHONE_STATE";
		action = "android.intent.action.PACKAGE_ADDED";
		action = "android.intent.action.MEDIA_UNMOUNTED";
		action = "android.intent.action.MEDIA_REMOVED";
		action = "android.intent.action.MEDIA_CHECKING";
		action = "android.intent.action.PACKAGE_REPLACED";
		action = "android.intent.action.NEW_OUTGOING_CALL";
		action = "android.bluetooth.adapter.action.STATE_CHANGED";
	}
}
