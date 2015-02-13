package com.zhidian.wifibox.data;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * 垃圾清理数据bean
 * 
 * @author xiedezhi
 * 
 */
public class TransScanDataBean {

	public boolean isSelect;

	public String title;

	public long size;

	public String suggestion;
	/**
	 * 包含的SD卡文件
	 */
	public List<String> paths = new ArrayList<String>();

}
