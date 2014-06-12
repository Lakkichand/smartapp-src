package com.jiubang.ggheart.apps.desks.diy.messagecenter.Beans;

/**
 * 过滤使用的apk信息
 * @author liulixia
 *
 */
public class MessageFilterBean {
	public static final int FILTERD_BY_PACKAGE_NAME = 0; //根据包名过滤
	public static final int FILTERD_BY_SIGNATURE = 1; //根据签名过滤
	public int filterType = -1;
	public String apkSignature;
	public String apkName;
}
