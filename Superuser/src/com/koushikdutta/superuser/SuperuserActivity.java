package com.koushikdutta.superuser;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ListActivity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SuperuserActivity extends ListActivity
{
	LayoutInflater mInflater;
	ListView mListView;
	WhitelistAdapter mAdapter;
	TextView mEmptyText;
	SQLiteDatabase mDatabase;
	Cursor mCursor;

	protected static class DatabaseHelper extends SQLiteOpenHelper
	{
		public DatabaseHelper(Context context)
		{
			super(context, "superuser.sqlite", null, 1);
		}

		@Override
		public void onCreate(SQLiteDatabase db)
		{
			String ddlWhitelist = "create table whitelist (_id integer primary key, name text, count integer);";
			db.execSQL(ddlWhitelist);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
		}
	}

	public class WhitelistAdapter extends CursorAdapter
	{
		public WhitelistAdapter(Context context, Cursor c)
		{
			super(context, c);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor)
		{
			TextView whitelistPackageText = (TextView) view.findViewById(R.id.WhitelistPackageText);
			int uid = cursor.getInt(0);
			String packageName = cursor.getString(1);
			whitelistPackageText.setText(packageName);
			TextView whitelistItemText = (TextView) view.findViewById(R.id.WhitelistItemText);
			whitelistItemText.setText("User ID: " + new Integer(uid).toString());
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent)
		{
			View view = mInflater.inflate(R.layout.whitelistitem, null);
			bindView(view, context, cursor);
			return view;
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		prepareSu();

		mEmptyText = (TextView) findViewById(R.id.EmptyWhitelistText);
		mListView = (ListView) findViewById(android.R.id.list);
		mInflater = getLayoutInflater();

		DatabaseHelper hlp = new DatabaseHelper(this);
		mDatabase = hlp.getWritableDatabase();

		refreshCursor();

		Toast toast = Toast.makeText(this, "Click and hold an application to to remove it from the whitelist", Toast.LENGTH_LONG);
		toast.show();

		mListView.setOnItemLongClickListener(new OnItemLongClickListener()
		{
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
			{
				mCursor.moveToPosition(position);
				int uid = mCursor.getInt(0);
				String command = "delete from whitelist where _id=" + uid + ";";
				mDatabase.execSQL(command);
				refreshCursor();
				return true;
			}
		});

		updateEmptyText();
	}

	private void refreshCursor()
	{
		mCursor = mDatabase.query(false, "whitelist", new String[] { "_id", "name" }, null, null, null, null, null, null);
		mAdapter = new WhitelistAdapter(this, mCursor);
		setListAdapter(mAdapter);
		updateEmptyText();
	}

	private void updateEmptyText()
	{
		mEmptyText.setVisibility(mCursor.getCount() != 0 ? View.GONE : View.VISIBLE);
	}

	public static int getUidForPackage(String packageName) throws Exception
	{
		// parse the uid given a package name (which should be visible in ps)
		Process p = Runtime.getRuntime().exec("ps");
		InputStream reader = p.getInputStream();
		Thread.sleep(200);
		byte[] buff = new byte[10000];
		int read = reader.read(buff, 0, buff.length);
		String str = new String(buff);
		String pattern = String.format("app_(\\d+).*?%s", packageName);
		Pattern regex = Pattern.compile(pattern);
		Matcher match = regex.matcher(str);
		if (match.find())
			return Integer.parseInt(match.group(1)) + 10000;

		throw new Exception("Unable to determine uid for package");
	}

	// Superuser upgrade path
	private void prepareSu()
	{
		try
		{
			File su = new File("/system/bin/su");
			if (!su.exists())
			{
				Toast toast = Toast.makeText(this, "Unable to find /system/bin/su.", Toast.LENGTH_LONG);
				toast.show();
				return;
			}

			InputStream suStream = getResources().openRawResource(R.raw.su);

			if (su.length() == suStream.available())
			{
				suStream.close();
				return;
			}

			File superuser = new File("/system/bin/superuser");

			if (superuser.exists())
			{
				// return device to original state
				Process process = Runtime.getRuntime().exec("superuser");
				DataOutputStream os = new DataOutputStream(process.getOutputStream());

				os.writeBytes("mount -oremount,rw /dev/block/mtdblock3 /system\n");
				os.writeBytes("busybox cp /system/bin/superuser /system/bin/su\n");
				os.writeBytes("busybox chown 0:0 /system/bin/su\n");
				os.writeBytes("chmod 4755 /system/bin/su\n");
				os.writeBytes("rm /system/bin/superuser\n");
				os.writeBytes("exit\n");
				os.flush();
			}
			
			byte[] bytes = new byte[suStream.available()];
			DataInputStream dis = new DataInputStream(suStream);
			dis.readFully(bytes);
			FileOutputStream suOutStream = new FileOutputStream("/data/data/com.koushikdutta.superuser/su");
			suOutStream.write(bytes);
			suOutStream.close();
			
			Process process = Runtime.getRuntime().exec("su");
			DataOutputStream os = new DataOutputStream(process.getOutputStream());
			os.writeBytes("mount -oremount,rw /dev/block/mtdblock3 /system\n");
			os.writeBytes("busybox cp /data/data/com.koushikdutta.superuser/su /system/bin/su\n");
			os.writeBytes("busybox chown 0:0 /system/bin/su\n");
			os.writeBytes("chmod 4755 /system/bin/su\n");
			os.writeBytes("exit\n");
			os.flush();
		}
		catch (Exception e)
		{
			Toast toast = Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG);
			toast.show();
		}
	}
}