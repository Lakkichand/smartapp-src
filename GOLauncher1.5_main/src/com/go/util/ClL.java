package com.go.util;

import android.app.Activity;
import android.os.Bundle;

/**
 * 隐藏界面
 * 
 * @author wanglingjun
 * 
 */
public class ClL extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.finish();
	}
}