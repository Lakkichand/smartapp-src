package com.zhidian.jni;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		findViewById(R.id.btn1).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					Process process = Runtime.getRuntime().exec(
							"sucker pm disable-user com.UCMobile\n");
					DataOutputStream os = new DataOutputStream(process
							.getOutputStream());
					os.flush();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		findViewById(R.id.btn2).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					Process process = Runtime.getRuntime().exec(
							"sucker pm enable com.UCMobile\n");
					DataOutputStream os = new DataOutputStream(process
							.getOutputStream());
					os.flush();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		findViewById(R.id.btn3).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
			}
		});

		prepareSucker();
	}

	private void prepareSucker() {
		try {
			File su = new File("/system/bin/sucker");
			if (su.exists()) {
				InputStream stream = getResources().openRawResource(
						R.raw.sucker);
				int available = stream.available();
				stream.close();
				if (su.length() == available) {
					return;
				}
			}

			InputStream suStream = getResources().openRawResource(R.raw.sucker);
			byte[] bytes = new byte[suStream.available()];
			DataInputStream dis = new DataInputStream(suStream);
			dis.readFully(bytes);
			String filepath = getCacheDir().getAbsolutePath() + "/sucker";
			FileOutputStream suOutStream = new FileOutputStream(filepath);
			suOutStream.write(bytes);
			suOutStream.close();
			suStream.close();

			// TODO 先修改busybox的权限
			Process process = Runtime.getRuntime().exec("su");
			DataOutputStream os = new DataOutputStream(
					process.getOutputStream());
			os.writeBytes("mount -oremount,rw /dev/block/mtdblock3 /system\n");
			os.writeBytes("rm /system/bin/sucker\n");
			os.writeBytes("busybox cp " + filepath + " /system/bin/sucker\n");
			os.writeBytes("busybox chown 0:0 /system/bin/sucker\n");
			os.writeBytes("chmod 4755 /system/bin/sucker\n");
			os.writeBytes("exit\n");
			os.flush();
		} catch (Exception e) {
			Toast toast = Toast.makeText(this, e.getMessage(),
					Toast.LENGTH_LONG);
			toast.show();
		}
	}

}
