package com.jiubang.ggheart.data;

import java.util.List;

/**
 * 数据变更通知
 * 
 * @author masanbing
 * 
 */
public interface DataChangeListener {
	void dataChanged(int dataType, int param, Object object, List objects1);
}
