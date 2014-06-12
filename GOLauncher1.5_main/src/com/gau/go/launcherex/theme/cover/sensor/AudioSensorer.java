package com.gau.go.launcherex.theme.cover.sensor;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Process;
import android.util.Log;

import com.gau.go.launcherex.theme.cover.utils.DoubleFormatUtils;

/**
 * 
 * 类描述:MIC孔感应器 功能详细描述:
 * 
 * @author guoyiqing
 * @date [2012-10-15]
 */
public class AudioSensorer extends BaseSensorer implements Runnable {

	private static final int SAMPLE_RATE_IN_HZ = 8000;
	private static final int MIN_BUFFER_SIZE = 4096;
	private static final double FENBEI_LOG = 20;
	private static final String SENSOR_AUDIO_THREAD_NAME = "sensor_audio_thread_name";
	private boolean mIsInit = false;
	private boolean mIsStop = false;
	private int mBufferSize;
	private AudioRecord mRecord;
	private int mUpdateInterval = 150;
	private AudioDataState mState;

	public AudioSensorer() { 
		mBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_MONO,
				AudioFormat.ENCODING_PCM_16BIT);
		if (mBufferSize < MIN_BUFFER_SIZE) {
			mBufferSize = MIN_BUFFER_SIZE;
		}
		mRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_IN_HZ,
				AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, mBufferSize);
		if (mRecord.getState() == AudioRecord.STATE_INITIALIZED) {
			mIsInit = true;
			mState = new AudioDataState();
		} else {
			Log.e("AudioSensorer", "AudioSensorer init Error");
		}
	}

	public void run() {
		if (mIsInit) {
			Process.setThreadPriority(-19);
			mRecord.startRecording();
			notifyDataChange(true, ISensorType.Pysical.SENSOR_TYPE_AUDIO,
					ISensorType.Event.CHANGE_TYPE_ON_START, null);
			short[] buffer;
			while (!mIsStop) {
				buffer = new short[mBufferSize];
				int length = mRecord.read(buffer, 0, mBufferSize);
				if (length != AudioRecord.ERROR_INVALID_OPERATION
						&& length != AudioRecord.ERROR_BAD_VALUE && length != 0) {
					int sum = 0;
					for (int i = 0; i < length; i++) {
						if (buffer[i] > 0) {
							sum += buffer[i] * 2 - 1;
						} else {
							sum += Math.abs(buffer[i] * 2);
						}
					}
					double volume = FENBEI_LOG * (float) Math.log10(sum / length);
					volume = DoubleFormatUtils.roundDouble(volume, 2);
					mState.setLastUpdateTime(System.currentTimeMillis());
					mState.setWindPower(volume);
					notifyDataChange(true, ISensorType.Pysical.SENSOR_TYPE_AUDIO,
							ISensorType.Event.CHANGE_TYPE_ON_DATA_CHANGE, mState);
					sleep(mUpdateInterval);
				} else {
					Log.e("AudioSensorer", "AudioSensorer read data error");
				}
			}
			if (mRecord != null) {
				mRecord.stop();
				mRecord.release();
				mRecord = null;
			}
			notifyDataChange(true, ISensorType.Pysical.SENSOR_TYPE_AUDIO,
					ISensorType.Event.CHANGE_TYPE_ON_TOP, null);
		}
	}

	private void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			Log.e("AudioSensorer", e.getMessage());
		}
	}

	public void setUpdateInterval(int interval) {
		mUpdateInterval = interval;
	}

	@Override
	public void stop() {
		mIsStop = true;
		if (mRecord != null && mIsInit) {
			mRecord.stop();
			mRecord.release();
			mRecord = null;
		}
		super.stop();
	}

	@Override
	public void start() {
		new Thread(this, SENSOR_AUDIO_THREAD_NAME).start();
	}

	public BaseState getSensorState() {
		return mState;
	}

}
