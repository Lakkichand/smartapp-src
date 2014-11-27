package com.youle.gamebox.ui.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class CopyPasteUtils {

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	public static void setText(Context context, String str){
		int sdk = android.os.Build.VERSION.SDK_INT;
		if(sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
		    @SuppressWarnings("deprecation")
			android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
		    clipboard.setText(str);
		} else {
		    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE); 
		    android.content.ClipData clip = android.content.ClipData.newPlainText("text label", str);
		    clipboard.setPrimaryClip(clip);
		}
	}
	
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	public static String getText(Context context){
		int sdk = android.os.Build.VERSION.SDK_INT;
		if(sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
		    @SuppressWarnings("deprecation")
			android.text.ClipboardManager clipboard = 
			(android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
		    return clipboard.getText().toString();
		} else {
		    android.content.ClipboardManager clipboard = 
		    		(android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE); 
		    android.content.ClipData clip = clipboard.getPrimaryClip();
		    String text=null;
		    if (clip != null) {
		        android.content.ClipData.Item item = clip.getItemAt(0);
		        text = item.coerceToText(context).toString();//强制为文本
		    }
		    return text;
		}
	}
}
