package com.gau.go.launcherex.theme.cover.sensor;

/**
 * 
 * 类描述:感应器种类
 * 功能详细描述:
 * 
 * @author  guoyiqing
 * @date  [2012-10-15]
 */
public interface ISensorType {

	/**
	 * 
	 * <br>类描述:事件
	 * <br>功能详细描述:
	 * 
	 * @author  guoyiqing
	 * @date  [2012-10-20]
	 */
	public interface Event {

		public static final int CHANGE_TYPE_ON_START = 0;

		public static final int CHANGE_TYPE_ON_TOP = 1;

		public static final int CHANGE_TYPE_ON_DATA_CHANGE = 2;

	}

	/**
	 * 
	 * <br>类描述:物理感应
	 * <br>功能详细描述:
	 * 
	 * @author  guoyiqing
	 * @date  [2012-10-20]
	 */
	public interface Pysical {

		public static final int SENSOR_TYPE_AUDIO = 0;

		public static final int SENSOR_TYPE_ACCELEROMETER = 1;

	}

}
