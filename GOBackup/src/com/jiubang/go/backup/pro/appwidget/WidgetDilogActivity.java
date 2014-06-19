package com.jiubang.go.backup.pro.appwidget;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;

import com.jiubang.go.backup.ex.R;

/**
 * @author jiangpeihe
 *
 */
public class WidgetDilogActivity extends Activity {
	private static final String GOBACKWIDGETFILE = "gobackup_widget_file";
	private static final String KEY_CONTACT = "contact";
	private static final String KEY_SMS = "sms";
	private static final String KEY_CALL_LOG = "call_log";
	private static final String KEY_MMS = "mms";
	private boolean mSelectedBackupContent[] = new boolean[4];
	private String mKeyArray[] = new String[] { "contact", "sms", "call_log", "mms" };
	private SharedPreferences mSp;
	private ListView mListview;
	private Button mOkButton;
	private Button mCancelButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		showChooseBackupDilog(WidgetDilogActivity.this);
		//		initViews();
		//				initData();
	}

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			writePreferenceAndsendBroadcast();
		};
	};

	private void showChooseBackupDilog(Context context) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(WidgetDilogActivity.this);
		ListView listView = initViews();
		BaseAdapter contentAdapter = initAdapter();
		listView.setAdapter(contentAdapter);
		builder.setTitle(getString(R.string.nobackupentriesselected)).setView(listView)
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
						mHandler.sendEmptyMessage(1);
					}
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();

					}
				});
		AlertDialog alert = builder.create();
		alert.setCanceledOnTouchOutside(false);
		alert.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				finish();
			}
		});
		alert.show();
	}

	public ListView initViews() {
		//		setContentView(R.layout.layout_dialog_widget_choosecontent);
		mListview = new ListView(WidgetDilogActivity.this);
		mListview.setBackgroundColor(0Xffffffff);
		mListview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
				if (checkBox.isChecked()) {
					checkBox.setChecked(false);
				} else {
					checkBox.setChecked(true);
				}
			}
		});
		mSp = getSharedPreferences(GOBACKWIDGETFILE, Context.MODE_PRIVATE);
		return mListview;
	}

	public BaseAdapter initAdapter() {
		Map<String, ?> oldMap = null;

		try {
			oldMap = mSp.getAll();
		} catch (NullPointerException e) {
		}
		if (oldMap != null && oldMap.size() > 0) {
			Set<String> keys = oldMap.keySet();
			if (keys != null && keys.size() > 0) {

				for (String key : keys) {
					if (key.equals("mms")) {
						mSelectedBackupContent[3] = (Boolean) oldMap.get(key);
					} else if (key.equals("call_log")) {
						mSelectedBackupContent[2] = (Boolean) oldMap.get(key);
					} else if (key.equals("sms")) {
						mSelectedBackupContent[1] = (Boolean) oldMap.get(key);
					} else if (key.equals("contact")) {
						mSelectedBackupContent[0] = (Boolean) oldMap.get(key);
					}
				}
			}
		}
		String[] items = getResources().getStringArray(R.array.backupchoose);
		BackupChooseDialog backupDialogAdapter = new BackupChooseDialog(WidgetDilogActivity.this,
				Arrays.asList(items), mSelectedBackupContent);
		return backupDialogAdapter;

	}

	private void writePreferenceAndsendBroadcast() {
		for (int i = 0; i < mSelectedBackupContent.length; i++) {
			if (mSp != null) {
				Editor editor = mSp.edit();
				editor.putBoolean(mKeyArray[i], mSelectedBackupContent[i]);
				editor.commit();
			}
		}
		Intent intent = new Intent("com.jiubang.APPWIDGET_UPDATE");
		sendBroadcast(intent);
	}

}
