package com.gau.go.launcherex.theme.cover.sensor;

/**
 * 
 * 类描述:感应器数据变化监听接口
 * 功能详细描述:
 * 
 * @author  guoyiqing
 * @date  [2012-10-15]
 */
public interface OnSensorDataChangeListener {

	/**
	 * 感应器数据变化的回调
	 * @param isAsync 是否来自异步
	 * @param sensor_type {@link ISensorType.Pysical}
	 * @param state {@link BaseState}
	 */
	public void onDataChange(boolean isAsync, int sensor_type, BaseState state);

	public void onBegin();

	public void onEnd();

}
