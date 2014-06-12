package com.gau.go.launcherex.theme.cover.sensor;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * 类描述:感应器
 * 功能详细描述:
 * 
 * @author  guoyiqing
 * @date  [2012-10-15]
 */
public abstract class BaseSensorer {

	private List<OnSensorDataChangeListener> mListeners = new ArrayList<OnSensorDataChangeListener>();

	public void registeObsever(OnSensorDataChangeListener listener) {
		if (listener != null) {
			mListeners.remove(listener);
			mListeners.add(listener);
		}
	}

	public void unRegisteObsever(OnSensorDataChangeListener listener) {
		if (listener != null) {
			mListeners.remove(listener);
		}
	}

	public void notifyDataChange(boolean isAsync, int sensor_type, int event_type, BaseState state) {
		switch (event_type) {
			case ISensorType.Event.CHANGE_TYPE_ON_DATA_CHANGE :
				for (OnSensorDataChangeListener obsever : mListeners) {
					obsever.onDataChange(isAsync, sensor_type, state);
				}
				break;
			case ISensorType.Event.CHANGE_TYPE_ON_START :
				for (OnSensorDataChangeListener obsever : mListeners) {
					obsever.onBegin();
				}
				break;
			case ISensorType.Event.CHANGE_TYPE_ON_TOP :
				for (OnSensorDataChangeListener obsever : mListeners) {
					obsever.onEnd();
				}
				break;
			default :
				break;
		}
	}

	public abstract void start();

	public void stop() {
		if (mListeners != null) {
			mListeners.clear();
		}
	}

}
