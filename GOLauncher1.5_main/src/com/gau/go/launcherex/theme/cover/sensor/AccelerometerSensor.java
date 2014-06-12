package com.gau.go.launcherex.theme.cover.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.gau.go.launcherex.theme.cover.utils.DoubleFormatUtils;

/**
 * 
 * 类描述:重力监听器 功能详细描述:
 * 
 * @author guoyiqing
 * @date [2012-10-15]
 */
public class AccelerometerSensor extends BaseSensorer {

	private float mMinSpeedChange = 35.0f;
	private AccelerometerDataState mState;
	private boolean mIsInit = false;
	private Sensor mAccelerometerSensor;
	private SensorEventListener mListener;
	private SensorManager manager;
	private int mUpdateInterval = 100;
	private static final int ONE_THOUSAND = 1000;

	public AccelerometerSensor(Context context) {
		mState = new AccelerometerDataState();
		init(context);
	}

	private void init(Context context) {
		manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		mAccelerometerSensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		if (mAccelerometerSensor != null) {
			mIsInit = true;
			mListener = new SensorEventListener() {

				public void onSensorChanged(SensorEvent event) {
					long current = System.currentTimeMillis();
					long interval = current - mState.getLastUpdateTime();
					if (interval < mUpdateInterval) {
						return;
					}
					float lastX = mState.getX();
					float lastY = mState.getY();
					float lastZ = mState.getZ();
					mState.setX(DoubleFormatUtils.roundFloat(event.values[SensorManager.DATA_X], 2));
					mState.setY(DoubleFormatUtils.roundFloat(event.values[SensorManager.DATA_Y], 2));
					mState.setZ(DoubleFormatUtils.roundFloat(event.values[SensorManager.DATA_Z], 2));
					mState.setSpeedX(DoubleFormatUtils.roundFloat((mState.getX() - lastX)
							/ interval * ONE_THOUSAND, 2));
					mState.setSpeedY(DoubleFormatUtils.roundFloat((mState.getY() - lastY)
							/ interval * ONE_THOUSAND, 2));
					mState.setSpeedZ(DoubleFormatUtils.roundFloat((mState.getZ() - lastZ)
							/ interval * ONE_THOUSAND, 2));
					mState.setLastUpdateTime(current);
					float maxChangeSpeed = Math.abs(mState.getSpeedY());
					if (Math.abs(mState.getSpeedX()) > maxChangeSpeed) {
						maxChangeSpeed = Math.abs(mState.getSpeedX());
					}
					if (maxChangeSpeed < Math.abs(mState.getSpeedZ())) {
						maxChangeSpeed = Math.abs(mState.getSpeedZ());
					}
					mState.setMaxChangeSpeed(maxChangeSpeed);
					mState.setNotifyType(maxChangeSpeed > mMinSpeedChange
							? AccelerometerDataState.NOTIFY_TYPE_SPEED_CHANGE
							: AccelerometerDataState.NOTIFY_TYPE_GRAVITY_CHANGE);
					notifyDataChange(false, ISensorType.Pysical.SENSOR_TYPE_ACCELEROMETER,
							ISensorType.Event.CHANGE_TYPE_ON_DATA_CHANGE, mState);

					//					Log.e("guoyiqing", mState.toString());
				}

				public void onAccuracyChanged(Sensor sensor, int accuracy) {

				}
			};
		}
	}

	public void setMinSpeedChange(float minSpeedChange) {
		mMinSpeedChange = minSpeedChange;
	}

	public void setUpdateInterval(int updateInterval) {
		mUpdateInterval = updateInterval;
	}

	@Override
	public void start() {
		if (mIsInit) {
			manager.registerListener(mListener, mAccelerometerSensor, SensorManager.SENSOR_DELAY_UI);
		}
	}

	@Override
	public void stop() {
		if (mIsInit) {
			manager.unregisterListener(mListener, mAccelerometerSensor);
			manager = null;
			mListener = null;
			mAccelerometerSensor = null;
		}
		super.stop();
	}

	public AccelerometerDataState getState() {
		return mState;
	}

}
