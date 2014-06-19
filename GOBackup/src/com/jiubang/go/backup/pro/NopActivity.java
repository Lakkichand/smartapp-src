package com.jiubang.go.backup.pro;

import android.app.Activity;
import android.os.Bundle;

/**
 * 一个空的Activity，不执行任何操作，用于维护Activity堆栈状态 目前主要用于没有Root权限时恢复应用程序，限制用户从通知栏进入应用程序
 * 
 * @author maiyongshen
 */
public class NopActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		finish();
	}

}
