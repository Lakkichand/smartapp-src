package com.jiubang.ggheart.apps.desks.diy;

import org.acra.ErrorReporter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.gau.go.launcherex.R;
import com.go.util.AppUtils;
import com.jiubang.ggheart.components.DeskActivity;

/**
 * 
 * 类描述: 弹出框评分内容入口类
 * 功能详细描述:
 * @date  [2012-10-12]
 */
public class RateDialogContentActivity extends DeskActivity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initView();
	}
	
	public void initView() {
		setContentView(R.layout.desk_rate_dialog_content);
		Button feedback_Button = (Button) findViewById(R.id.feedback);
		feedback_Button.setOnClickListener(this);
		Button rate_Button = (Button) findViewById(R.id.rate);
		rate_Button.setOnClickListener(this);
		Button never_Button = (Button) findViewById(R.id.remind_never);
		never_Button.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		PreferencesManager preferencesManager = new PreferencesManager(this,
				IPreferencesIds.DESK_SHAREPREFERENCES_FILE, Context.MODE_PRIVATE);
		switch (v.getId()) {
			case R.id.feedback :
				sendMail();
				break;

			case R.id.rate :
				String packageName = getApplication().getApplicationInfo().packageName;
				AppUtils.viewAppDetail(this, packageName);
				preferencesManager.putBoolean(IPreferencesIds.REMIND_RATE, false);
				preferencesManager.putBoolean(IPreferencesIds.FIRST_RUN_REMIND_RATE, false);
				preferencesManager.commit();
				break;

			case R.id.remind_never :
				preferencesManager.putBoolean(IPreferencesIds.REMIND_RATE, false);
				preferencesManager.putBoolean(IPreferencesIds.FIRST_RUN_REMIND_RATE, false);
				preferencesManager.commit();
				break;
			default :
				break;
		}
		this.finish();
	}
	
	private void sendMail() {
		Context context = getBaseContext();
		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		String[] receiver = new String[] { "golauncher@goforandroid.com" };
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, receiver);

		String suggestionForMailString = context.getResources().getString(
				R.string.feedback_select_type_suggestion_for_mail);
	
		String subject = "GO Launcher EX(v" + context.getString(R.string.curVersion)
				+ ") Feedback(" + suggestionForMailString + ")";
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
		String content = context.getString(R.string.rate_go_launcher_mail_content) + "\n\n";
		StringBuffer body = new StringBuffer(content);
		body.append("\nProduct=" + android.os.Build.PRODUCT);
		body.append("\nPhoneModel=" + android.os.Build.MODEL);
		body.append("\nROM=" + android.os.Build.DISPLAY);
		body.append("\nBoard=" + android.os.Build.BOARD);
		body.append("\nDevice=" + android.os.Build.DEVICE);
		body.append("\nDensity="
				+ String.valueOf(context.getResources().getDisplayMetrics().density));
		body.append("\nPackageName=" + context.getPackageName());
		body.append("\nAndroidVersion=" + android.os.Build.VERSION.RELEASE);
		body.append("\nTotalMemSize="
				+ (ErrorReporter.getTotalInternalMemorySize() / 1024 / 1024) + "MB");
		body.append("\nFreeMemSize="
				+ (ErrorReporter.getAvailableInternalMemorySize() / 1024 / 1024) + "MB");
		body.append("\nRom App Heap Size="
				+ Integer.toString((int) (Runtime.getRuntime().maxMemory() / 1024L / 1024L))
				+ "MB");
		emailIntent.putExtra(Intent.EXTRA_TEXT, body.toString());
		emailIntent.setType("plain/text");
		try {
			context.startActivity(emailIntent);
		} catch (Exception e) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse("http://golauncher.goforandroid.com"));
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setClassName("com.android.browser",
					"com.android.browser.BrowserActivity");
			context.startActivity(intent);
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		initView();
	}
}
