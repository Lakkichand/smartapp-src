package com.matrixxun.pulltozoomlistsimple;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

public class MainActivity extends Activity {
	PullToZoomListView listView;
	private String[] adapterData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		listView = (PullToZoomListView) findViewById(R.id.listview);
		adapterData = new String[] { "Activity", "Service", "Content Provider",
				"Intent", "BroadcastReceiver", "ADT", "Sqlite3", "HttpClient",
				"DDMS", "Android Studio", "Fragment", "Loader" };

		listView.setAdapter(new ArrayAdapter<String>(MainActivity.this,
				android.R.layout.simple_list_item_1, adapterData));
		listView.getHeaderView().setImageResource(R.drawable.splash01);
		listView.getHeaderView().setScaleType(ImageView.ScaleType.CENTER_CROP);
	}

}
