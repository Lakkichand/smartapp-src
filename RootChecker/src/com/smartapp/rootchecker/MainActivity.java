package com.smartapp.rootchecker;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public class MainActivity extends Activity {
    private final int mRootColor = 0xFF00FF00;
    private final int mNoRootColor = 0xFFFF0000;

    private FrameLayout mADContainer;

    /**
     * 广告条
     */
    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	setContentView(R.layout.activity_main);

	mADContainer = (FrameLayout) findViewById(R.id.adcontainer);

	// 添加广告条
	adView = new AdView(this, AdSize.BANNER, "a1527b85e5731c0");
	// 在其中添加 adView
	mADContainer.addView(adView);
	// 启动一般性请求并在其中加载广告
	adView.loadAd(new AdRequest());

	TextView device = (TextView) findViewById(R.id.device);
	TextView version = (TextView) findViewById(R.id.version);

	device.setText(getString(R.string.device) + android.os.Build.MODEL);
	version.setText(getString(R.string.version) + android.os.Build.VERSION.RELEASE);

	final TextView result = (TextView) findViewById(R.id.result);
	result.setText(getString(R.string.tips));

	Button btn = (Button) findViewById(R.id.btn);
	btn.setOnClickListener(new OnClickListener() {

	    @Override
	    public void onClick(View v) {
		final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		dialog.setMessage(getString(R.string.wait));
		dialog.setCancelable(false);
		dialog.show();

		Thread thread = new Thread() {
		    @Override
		    public void run() {
			boolean root = true;
			try {
			    RootShell shell = RootShell.getInstance();
			    if (shell != null) {
				root = shell.isRootValid();
			    } else {
				root = false;
			    }
			} catch (Exception e) {
			    e.printStackTrace();
			    root = false;
			}
			final boolean root_ = root;
			runOnUiThread(new Runnable() {

			    @Override
			    public void run() {
				dialog.dismiss();
				if (root_) {
				    result.setTextColor(mRootColor);
				    result.setText("Root");
				} else {
				    result.setTextColor(mNoRootColor);
				    result.setText("No Root");
				}
			    }
			});
		    }
		};
		thread.setPriority(Thread.MAX_PRIORITY);
		thread.start();
	    }
	});
    }

    @Override
    protected void onDestroy() {
	super.onDestroy();
	adView.destroy();
	android.os.Process.killProcess(android.os.Process.myPid());
    }

}
