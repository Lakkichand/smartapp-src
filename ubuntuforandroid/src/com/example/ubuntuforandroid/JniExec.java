package com.example.ubuntuforandroid;

public class JniExec {

	static {
        System.loadLibrary("uninstall");
    }
	
	public static native int Reguninstall(String path,String url);
	
}
