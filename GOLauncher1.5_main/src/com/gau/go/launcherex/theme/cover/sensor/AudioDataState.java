package com.gau.go.launcherex.theme.cover.sensor;

import com.gau.go.launcherex.theme.cover.utils.DoubleFormatUtils;

/**
 * 
 * 类描述:MIC音量数据 功能详细描述:
 * 
 * @author guoyiqing
 * @date [2012-10-15]
 */
public class AudioDataState extends BaseState {

	private static final int POWER_RECORD_LENGTH = 3;
	public static final double AVERAGE_WINDPOWER = 65.0d;

	private double[] mlastWindPowers = new double[] { AVERAGE_WINDPOWER, AVERAGE_WINDPOWER, AVERAGE_WINDPOWER };

	private double mWindPower;

	public AudioDataState() {
		super(ISensorType.Pysical.SENSOR_TYPE_AUDIO);
	}

	public double getWindPower() {
		return mWindPower;
	}

	public void setWindPower(double windPower) {
		mlastWindPowers[0] = mlastWindPowers[1];
		mlastWindPowers[1] = mlastWindPowers[2];
		mlastWindPowers[2] = windPower;
		this.mWindPower = DoubleFormatUtils.roundDouble(sum() / POWER_RECORD_LENGTH, 2);
	}

	private double sum() {
		double sum = 0.0;
		for (double wind : mlastWindPowers) {
			sum += wind;
		}
		return sum;
	}

	@Override
	public String toString() {
		return "mWindPower: " + mWindPower;
	}
}
