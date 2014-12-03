package com.escape.uninstaller.ui;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.escape.uninstaller.util.DrawUtil;
import com.smartapp.rootuninstaller.R;

public class UserAppFragment extends Fragment implements IFragment {

	public static final String TAG = "USERAPPFRAGMENT";

	private TestAdapter mAdapter = new TestAdapter();

	public static UserAppFragment newInstance() {
		UserAppFragment fragment = new UserAppFragment();
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		Log.e("", "UserAppFragment onAttach");
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.e("", "UserAppFragment onCreate");
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.e("", "UserAppFragment onCreateView  container = " + container);
		View frame = inflater.inflate(R.layout.userapp, null);
		ListView list = (ListView) frame.findViewById(R.id.tlistview);
		list.setAdapter(mAdapter);
		frame.findViewById(R.id.button).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						Button btn = (Button) v;
						btn.setText(System.currentTimeMillis() + "");
					}
				});
		return frame;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		Log.e("", "UserAppFragment onViewCreated");
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.e("", "UserAppFragment onActivityCreated");
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onStart() {
		Log.e("", "UserAppFragment onStart");
		super.onStart();
	}

	@Override
	public void onResume() {
		Log.e("", "UserAppFragment onResume");
		super.onResume();
	}

	@Override
	public void onPause() {
		Log.e("", "UserAppFragment onPause");
		super.onPause();
	}

	@Override
	public void onStop() {
		Log.e("", "UserAppFragment onStop");
		super.onStop();
	}

	@Override
	public void onDestroyView() {
		Log.e("", "UserAppFragment onDestroyView");
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		Log.e("", "UserAppFragment onDestroy");
		super.onDestroy();
	}

	@Override
	public void onDetach() {
		Log.e("", "UserAppFragment onDetach");
		super.onDetach();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	private class TestAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return 200;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView != null && convertView instanceof TextView) {
				TextView text = (TextView) convertView;
				text.setText("" + position);
			} else {
				convertView = new TextView(getActivity());
				TextView text = (TextView) convertView;
				text.setTextColor(Color.BLACK);
				text.setTextSize(DrawUtil.dip2px(getActivity(), 28));
				text.setText("" + position);
			}
			return convertView;
		}
	}

	@Override
	public void onAppAction(String packName) {
	}
}
