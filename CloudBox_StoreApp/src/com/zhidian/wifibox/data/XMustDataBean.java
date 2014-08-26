package com.zhidian.wifibox.data;

import java.util.ArrayList;
import java.util.List;

/**
 * 极速模式装机必备数据单元
 * 
 * @author xiedezhi
 * 
 */
public class XMustDataBean {
	/**
	 * 分类名称
	 */
	public String name;
	/**
	 * 分类状态，0标示关闭，1标示打开
	 */
	public int status;
	/**
	 * 该分类下的数据列表
	 */
	public List<XAppDataBean> mAppList = new ArrayList<XAppDataBean>();

}
