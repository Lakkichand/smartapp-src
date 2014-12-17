package com.koushikdutta.superuser;

import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SuperuserRequestActivity extends Activity
{
	Button mYesButton;
	Button mNoButton;
	Button mAlwaysButton;
	Intent mIntent;
	SQLiteDatabase mDatabase;
	int mUid;
	int mPid;
	String mName;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.superuserrequest);

		Button yesButton = (Button) findViewById(R.id.YesButton);
		Button noButton = (Button) findViewById(R.id.NoButton);
		Button alwaysButton = (Button) findViewById(R.id.AlwaysButton);
		mIntent = getIntent();

		mUid = mIntent.getIntExtra("uid", -1);
		mPid = mIntent.getIntExtra("pid", -1);

		if (mUid == -1 || mPid == -1)
		{
			Toast toast = Toast.makeText(this, "This intent requires two int parameters: uid pid", Toast.LENGTH_LONG);
			toast.show();
			mIntent.putExtra("superuserresult", false);
			setResult(RESULT_CANCELED, mIntent);
			finish();
			return;
		}

		try
		{
			if (mUid >= 10000)
			{
				Process p = Runtime.getRuntime().exec("ps");
				InputStream reader = p.getInputStream();
				Thread.sleep(200);
				byte[] buff = new byte[10000];
				int read = reader.read(buff, 0, buff.length);
				String str = new String(buff);
				int id = mUid - 10000;
				String pattern = String.format("^app_%d+\\s(.*?)\n", id);
				Pattern regex = Pattern.compile(pattern, Pattern.MULTILINE);
				Matcher match = regex.matcher(str);
				mName = "";
				while (match.find())
				{
					String[] strings = match.group(1).split(" ");
					mName += strings[strings.length -1] + "\n";
				}

				TextView nameText = (TextView) findViewById(R.id.requestorprocesses);
				mName = mName.replace("'","");
				nameText.setText(mName);
			}
		}
		catch (Exception e)
		{
			Toast toast = Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG);
			toast.show();
			finish();
			return;
		}

		TextView packageText = (TextView) findViewById(R.id.requestoruid);
		packageText.setText("User ID: " + mUid);

		// check whitelist
		mDatabase = new SuperuserActivity.DatabaseHelper(this).getWritableDatabase();
		Cursor c = mDatabase.query("whitelist", new String[] { "_id", "name" }, "_id = " + mUid, null, null, null, null);

		yesButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				String delete = String.format("delete from whitelist where _id=%d;", mUid);
				mDatabase.execSQL(delete);
				String whitelistCommand = String.format("insert into whitelist values (%d, '%s', %d);", mUid, mName, 1);
				mDatabase.execSQL(whitelistCommand);
				finish();
			}
		});

		noButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				String delete = String.format("delete from whitelist where _id=%d;", mUid);
				mDatabase.execSQL(delete);
				String whitelistCommand = String.format("insert into whitelist values (%d, '%s', %d);", mUid, mName, -1);
				mDatabase.execSQL(whitelistCommand);
				finish();
			}
		});

		alwaysButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				String delete = String.format("delete from whitelist where _id=%d;", mUid);
				mDatabase.execSQL(delete);
				String whitelistCommand = String.format("insert into whitelist values (%d, '%s', %d);", mUid, mName, 10000);
				mDatabase.execSQL(whitelistCommand);
				finish();
			}
		});
	}
}
