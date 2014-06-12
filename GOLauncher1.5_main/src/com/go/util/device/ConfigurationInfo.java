package com.go.util.device;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

/**
 * 当前设备的配置信息，用来分析当前设备的硬件等配置是否属于高端等级
 * 
 * @author jiangxuwen
 * 
 */
public class ConfigurationInfo {

	public final static String CPU_FILE_DIR = "/sys/devices/system/cpu/";
	public final static int HIGH_DEVICE = 0x1; // 高端
	public final static int NORMAL_DEVICE = 0x2; // 普通
	public final static int LOW_DEVICE = 0x3; // 低端
	public final static long MEMORY_SIZE_HIGH = 680; // 单位是MB
	public final static long MEMORY_SIZE_NORMAL = 320; // 单位是MB
	public final static long HEAP_SIZE_HIGH = 48; // 单位是MB
	public final static long HEAP_SIZE_NORMAL = 28; // 单位是MB
	public final static long HEAP_SIZE_HIGH_LARGE = 128; // large_heap模式单位是MB
	public final static long HEAP_SIZE_NORMAL_LARGE = 64; // large_heap模式单位是MB
	public final static int CPU_CORE_HIGH = 2; // cpu个数两个或以上的就认为在计算能力上是高配
	public final static int CPU_CLOCK_HIGH = 1200000; // cpu高配的频率
	public final static int CPU_CLOCK_NORMAL = 900000; // cpu中配的频率
    private static int sLevel = -1; // -1标识level还未获取过硬件设备的情况，获取过之后值就不为-1，下次就直接放回level值
	/**
	 * 获取机器等级
	 * 
	 * @return
	 */
	public static int getDeviceLevel() {
		if (sLevel != -1) {
			return sLevel;
		}
		final int cpuLevel = getComputeLevel();
		final int memoryLevel = getMemoryLevel();
		final int totalPoint = cpuLevel + memoryLevel;
		if (totalPoint <= HIGH_DEVICE * 2) {
			sLevel = HIGH_DEVICE; // 两个都为高才为高
		} else if (totalPoint <= NORMAL_DEVICE + LOW_DEVICE) {
			sLevel = NORMAL_DEVICE;
		} else {
			sLevel = LOW_DEVICE; // 两个级别都为低才为低
		}
		return sLevel;
		// switch(type){
		// case DEVICE_WEIGHT_TYPE_COMPUTE:
		// return getComputeLevel();
		// case DEVICE_WEIGHT_TYPE_MEMORY:
		// return getMemoryLevel();
		// default:
		// return NORMAL_DEVICE;
		// }
	} // end getDeviceLevel

	/**
	 * 偏重计算的机器等级
	 * 
	 * @return
	 */
	private static int getComputeLevel() {
		int result = LOW_DEVICE;
		try {
			Integer cpuClock = Integer.valueOf(CpuManager.getMaxCpuFreq());
			if (cpuClock >= CPU_CLOCK_HIGH) {
				result = HIGH_DEVICE;
			} else if (cpuClock >= CPU_CLOCK_NORMAL) {
				result = NORMAL_DEVICE;
			} else {
				result = LOW_DEVICE;
			}
		} catch (Exception e) {
			result = NORMAL_DEVICE;
		}
		return result;
	}

	/**
	 * 偏重内存的机器等级
	 * 
	 * @return
	 */
	private static int getMemoryLevel() {
		int result = LOW_DEVICE;
		final long heapSize = Runtime.getRuntime().maxMemory() / 1024 / 1024;
		final long heapSizeHigh = Build.VERSION.SDK_INT >= 14
				? HEAP_SIZE_HIGH_LARGE
				: HEAP_SIZE_HIGH;
		final long heapSizeNormal = Build.VERSION.SDK_INT >= 14
				? HEAP_SIZE_NORMAL_LARGE
				: HEAP_SIZE_NORMAL;
		final long totalMemorySize = getTotalMemory() / 1024;
		if (totalMemorySize >= MEMORY_SIZE_HIGH && heapSize >= heapSizeHigh) {
			result = HIGH_DEVICE;
		} else if (totalMemorySize >= MEMORY_SIZE_NORMAL && heapSize >= heapSizeNormal) {
			result = NORMAL_DEVICE;
		}
		return result;
	}

	/**
	 * 获取当前设备的等级数
	 * 
	 * @return
	 */
	public static String getDeviceInfo(Context context) {
		final long heapSize = Runtime.getRuntime().maxMemory();
		final long aviableMemorySize = getAvailableInternalMemorySize();
		final long totalMemorySize = getTotalInternalMemorySize();
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("Device = " + android.os.Build.MODEL + "\n");
		stringBuffer.append("AndroidVersion = " + android.os.Build.VERSION.RELEASE + "\n");
		stringBuffer.append("HeapSize = " + (heapSize / 1024 / 1024) + " MB" + "\n");
		stringBuffer.append("系统盘总容量 = " + (totalMemorySize / 1024 / 1024) + " MB" + "\n");
		stringBuffer.append("系统盘可用空间 = " + (aviableMemorySize / 1024 / 1024) + " MB" + "\n");
		stringBuffer.append("总内存 = " + (getTotalMemory() / 1024) + " MB\n");
		stringBuffer.append("可用内存 = " + (getAvailableMemory(context) / 1024) + " MB" + "\n");
		stringBuffer.append("CPU count = " + getNumCores());
		return stringBuffer.toString();
	} // end getDeviceLevel

	/**
	 * Gets the number of cores available in this device, across all processors.
	 * Requires: Ability to peruse the filesystem at "/sys/devices/system/cpu"
	 * The number of cores, or 1 if failed to get result
	 */
	public static int getNumCores() {
		// Private Class to display only CPU devices in the directory listing
		/**
		 * 
		 * @author jiangxuwen
		 *
		 */
		class CpuFilter implements FileFilter {
			@Override
			public boolean accept(File pathname) {
				// Check if filename is "cpu", followed by a single digit number
				if (Pattern.matches("cpu[0-9]", pathname.getName())) {
					return true;
				}
				return false;
			}
		}

		try {
			// Get directory containing CPU info
			File dir = new File("/sys/devices/system/cpu/");
			// Filter to only list the devices we care about
			File[] files = dir.listFiles(new CpuFilter());
			Log.v("Yugi", "CPU Count: " + files.length);
			// Return the number of cores (virtual CPU devices)
			return files.length;
		} catch (Exception e) {
			// Print exception
			Log.d("Yugi", "CPU Count: Failed.");
			e.printStackTrace();
			// Default to return 1 core
			return 1;
		}
	}

	/**
	 * Calculates the free memory of the device. This is based on an inspection
	 * of the filesystem, which in android devices is stored in RAM.
	 * 
	 * @return Number of bytes available.
	 */
	public static long getAvailableInternalMemorySize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return availableBlocks * blockSize;
	}

	/**
	 * Calculates the total memory of the device. This is based on an inspection
	 * of the filesystem, which in android devices is stored in RAM.
	 * 
	 * @return Total number of bytes.
	 */
	public static long getTotalInternalMemorySize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		return totalBlocks * blockSize;
	}

	private static long getTotalMemory() {
		long totalMemroy = 0;
		try {
			String cat = null;
			FileReader fileReader = new FileReader("/proc/meminfo");
			BufferedReader bReader = new BufferedReader(fileReader, 4096);
			cat = bReader.readLine(); // 读取第一行“MemTotal: 415268 kB”
			bReader.close();

			if (cat != null) {
				String[] array = cat.split("\\s+");
				if (array != null && array.length >= 1) {
					// 数组第2个为内存大小
					totalMemroy = Long.parseLong(array[1]);
				}
				array = null;
			}
			cat = null;
			bReader = null;
			fileReader = null;
		} catch (NumberFormatException e) {
			Log.e("taskManager", "getTotalMemory error");
		} catch (FileNotFoundException e1) {
			Log.e("taskManager", "getTotalMemory error");
		} catch (IOException e) {
			Log.e("taskManager", "getTotalMemory error");
		}
		return totalMemroy;
	}

	/**
	 * 获取手机当前可用内存
	 * 
	 * @return 可用内存(KB)
	 */
	private static long getAvailableMemory(Context context) {
		MemoryInfo memoryInfo = new MemoryInfo();
		final ActivityManager manager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		manager.getMemoryInfo(memoryInfo);
		return memoryInfo.availMem >> 10; // div 1024
	}
}
