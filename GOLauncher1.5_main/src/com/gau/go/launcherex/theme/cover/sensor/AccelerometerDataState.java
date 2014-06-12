package com.gau.go.launcherex.theme.cover.sensor;

/**
 * 
 * 类描述:重力数据 功能详细描述:
 * 
 * @author guoyiqing
 * @date [2012-10-15]
 */
public class AccelerometerDataState extends BaseState {

	public static final int NOTIFY_TYPE_GRAVITY_CHANGE = 0;
	public static final int NOTIFY_TYPE_SPEED_CHANGE = 1;
	private int mNotifyType = NOTIFY_TYPE_GRAVITY_CHANGE;
	private float mX;
	private float mY;
	private float mZ;
	private float mSpeedX;
	private float mSpeedY;
	private float mSpeedZ;
	private float mMaxChangeSpeed;
	
	public AccelerometerDataState() {
		super(ISensorType.Pysical.SENSOR_TYPE_ACCELEROMETER);
	}
	
	public float getZ() {
		return mZ;
	}

	public void setZ(float z) {
		this.mZ = z;
	}

	public float getY() {
		return mY;
	}

	public void setY(float y) {
		this.mY = y;
	}

	public float getX() {
		return mX;
	}

	public void setX(float x) {
		this.mX = x;
	}

	public float getSpeedZ() {
		return mSpeedZ;
	}

	public void setSpeedZ(float speedZ) {
		this.mSpeedZ = speedZ;
	}

	public float getSpeedX() {
		return mSpeedX;
	}

	public void setSpeedX(float speedX) {
		this.mSpeedX = speedX;
	}

	public float getSpeedY() {
		return mSpeedY;
	}

	public void setSpeedY(float speedY) {
		this.mSpeedY = speedY;
	}

	public float getMaxChangeSpeed() {
		return mMaxChangeSpeed;
	}

	public void setMaxChangeSpeed(float maxChangeSpeed) {
		mMaxChangeSpeed = maxChangeSpeed;
	}

	@Override
	public String toString() {
		return "mX: " + mX + "\n" + "mY: " + mY + "\n" + "mZ: " + mZ + "\n" + "mSpeedX: " + mSpeedX
				+ "\n" + "mSpeedY: " + mSpeedY + "\n" + "mSpeedZ: " + mSpeedZ + "\n";
	}

	public int getNotifyType() {
		return mNotifyType;
	}

	public void setNotifyType(int mNotifyType) {
		this.mNotifyType = mNotifyType;
	}

}
