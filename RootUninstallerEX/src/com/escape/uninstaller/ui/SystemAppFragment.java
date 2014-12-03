package com.escape.uninstaller.ui;

import com.smartapp.rootuninstaller.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SystemAppFragment extends Fragment implements IFragment {

	public static final String TAG = "SYSTEMAPPFRAGMENT";

	public static SystemAppFragment newInstance() {
		SystemAppFragment fragment = new SystemAppFragment();
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("", "SystemAppFragment onCreate");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View ret = inflater.inflate(R.layout.sysapp, null);
		return ret;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onAppAction(String packName) {
	}
}
