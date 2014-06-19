package com.jiubang.go.backup.pro.data;

import java.util.Collection;
import java.util.Date;

/**
 * 恢复接口
 * 
 * @author maiyongshen
 */
public interface IRecord {
	//备份数据分组常量
	public static String GROUP_USER_DATA = "group_user_data";
	public static String GROUP_SYSTEM_DATA = "group_system_data";
	public static String GROUP_USER_APP = "group_user_app";
	public static String GROUP_SYSTEM_APP = "group_system_app";
	public static String GROUP_USER_IMAGE = "group_user_image";

	public long getId();

	public Date getDate();

	public long getSpaceUsage();

	public String getDescription();

	public int getGroupCount();

	public Object getGroup(String groupKey);

	public String[] getGroupKeys();

	public CharSequence getGroupDescription(String groupKey);

	public int getGroupItemsCount(String groupKey);

	public Object getEntry(String groupKey, int childPosition);

	public void addGroup(String groupKey, Collection<BaseEntry> entries);

	public void addEntry(String groupKey, BaseEntry entry);

	public Object removeGroup(String groupKey);

	public void removeEntry(BaseEntry entry);
}
