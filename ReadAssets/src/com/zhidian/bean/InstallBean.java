package com.zhidian.bean;

public class InstallBean {

	public String boxNum;//盒子编号
	public String code;
	public String downloadUrl;//下载地址
	public String versionCode;//版本号
	public String status;//验证结果。0：验证通过 1：验证失败
	public String installTime;//安装时间
	public String msg;//错误信息
	public String unloadStatus;//上传服务器情况：0表示上传成功，1表示上传失败。
}
