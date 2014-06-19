package com.jiubang.go.backup.pro.data;

/**
 * 消息处理
 * 
 * @author maiyongshen
 */
public interface MessageReceiver {
	// 返回值表示是否处理此消息
	public boolean handleMessage(int arg1, int arg2, Object obj);
}
