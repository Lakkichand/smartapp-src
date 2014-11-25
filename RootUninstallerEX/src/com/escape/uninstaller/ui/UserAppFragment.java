package com.escape.uninstaller.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smartapp.rootuninstaller.R;

public class UserAppFragment extends Fragment {

	public static UserAppFragment newInstance() {
		UserAppFragment fragment = new UserAppFragment();
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("", "UserAppFragment onCreate");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View ret = inflater.inflate(R.layout.userapp, null);
		return ret;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
}
