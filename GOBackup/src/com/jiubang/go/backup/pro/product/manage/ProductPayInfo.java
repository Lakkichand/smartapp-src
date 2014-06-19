package com.jiubang.go.backup.pro.product.manage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.xml.sax.ContentHandler;

import android.content.Context;
import android.os.Environment;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.util.Log;
import android.util.Xml;

import com.jiubang.go.backup.pro.statistics.StatisticsTool;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 系统调用类：调用系统可执行程序
 *
 * @author CHEN GUANGMING
 */
public class ProductPayInfo {
	public static final String DEFALUTENCODING = "utf-8";
	public static final String PROBATION_RECORD_CATALOG = "/data-app";
	private static final String TAG = "GoBackup_ProductPayInfo";

	public static final String PRODUCT_ID = "com.jiubang.go.backup.pro_pay";

	private static boolean sIsPaidUser = false;
	public static boolean  sIsPaidUserByKey = false;

	private Context mContext;

	// private Properties properties;
	private String mProductId;
	private String mFileCatalog;
	private String mFilePath;

	private ProductPayInfoModel mPayInfoModel;

	private String mEncryptProductId;

	ProductPayInfo(Context ctx, String productId) {
		mContext = ctx;
		mProductId = productId;
		// properties = new Properties(ctx, productId);

		byte[] data = null;
		try {
			data = mProductId.getBytes(DEFALUTENCODING);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return;
		}

		mEncryptProductId = Base64.encodeToString(data, Base64.DEFAULT).replace("\n", "")
				.replace("=", "");

		String state = Environment.getExternalStorageState();
		if (state.equals(Environment.MEDIA_MOUNTED)) {
			mFileCatalog = Environment.getExternalStorageDirectory().getAbsolutePath()
					+ PROBATION_RECORD_CATALOG;
			mFilePath = mFileCatalog + "/" + mEncryptProductId;
			Util.createDir(mFileCatalog);
			File file = new File(mFilePath);
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (!file.canWrite()) {
				mFileCatalog = mContext.getApplicationInfo().dataDir + PROBATION_RECORD_CATALOG;
				mFilePath = mFileCatalog + "/" + mEncryptProductId;
			}
		} else {
			mFileCatalog = mContext.getApplicationInfo().dataDir + PROBATION_RECORD_CATALOG;
			mFilePath = mFileCatalog + "/" + mEncryptProductId;
		}

		readPayInfo();

		// 初始化购买状态
		initPaidState();
	}

	private static int getRandomKey() {
		final int m9000 = 9000;
		final int m1000 = 1000;
		SecureRandom random = new SecureRandom();
		// Random r = new Random(System.currentTimeMillis());
		int rand = random.nextInt(m9000) + m1000;
		return rand;
	}

	/** 解析购买信息 */
	private ContentHandler newHandler(final ProductPayInfoModel bean) {
		RootElement root = new RootElement("root");

		root.getChild("installDate").setEndTextElementListener(new EndTextElementListener() {
			@Override
			public void end(String body) {
				bean.setInstallDate(body);
			}
		});
		root.getChild("currentDate").setEndTextElementListener(new EndTextElementListener() {
			@Override
			public void end(String body) {
				bean.setCurrentDate(body);
			}
		});
		root.getChild("serialRamdonKey").setEndTextElementListener(new EndTextElementListener() {
			@Override
			public void end(String body) {
				bean.setSerialRamdonKey(Integer.parseInt(body));
			}
		});
		root.getChild("serialCode").setEndTextElementListener(new EndTextElementListener() {
			@Override
			public void end(String body) {
				bean.setProductSerialCode(body);
			}
		});
		root.getChild("paid").setEndTextElementListener(new EndTextElementListener() {
			@Override
			public void end(String body) {
				bean.setPaid(Boolean.parseBoolean(body));
			}
		});
		return root.getContentHandler();
	}

	/** 从文件中读取产品购买信息 */
	private void readPayInfo() {
		byte[] data = Util.readDataFromFile(mFilePath);
		mPayInfoModel = new ProductPayInfoModel();
		if (data == null) {
			generateDefaultPayInfo();
		} else {
			try {
				byte[] retData = EncryptArithmetic.tripleDesDencrypt(data);
				InputStream is = new ByteArrayInputStream(retData);

				ContentHandler handler = newHandler(mPayInfoModel);
				Xml.parse(is, Xml.Encoding.UTF_8, handler);
				is.close();
			} catch (Exception e) {
				e.printStackTrace();
				generateDefaultPayInfo();
			}
		}
	}

	private void generateDefaultPayInfo() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date now = new Date();
		String installDate = format.format(now);
		String currentDate = installDate;
		mPayInfoModel.setInstallDate(installDate);
		mPayInfoModel.setCurrentDate(currentDate);
		mPayInfoModel.setPaid(false);

		String deviceID = getDeviceId();
		// 需要获取一个可用的deviceID
		int randomKey = getRandomKey();
		mPayInfoModel.setSerialRamdonKey(randomKey);
		mPayInfoModel.setProductSerialCode(generateSerialNo(deviceID, randomKey));

		// properties.saveString(DEVICE_ID_KEY, deviceID);

		savePayInfo();
	}

	/** 把购买信息固化到文件中 */
	private void savePayInfo() {
		FileOutputStream fos = null;
		byte[] data = null;
		byte[] ret = null;
		String content = mPayInfoModel.getXmlContent();
		try {
			data = content.getBytes(DEFALUTENCODING);
			ret = EncryptArithmetic.tripleDesEncrypt(data);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			return;
		}
		if (Util.createDir(mFileCatalog)) {
			try {
				fos = new FileOutputStream(mFilePath, false);
				try {
					fos.write(ret, 0, ret.length);
					fos.flush();
				} finally {
					fos.close();
				}
				// Log.v("Gobackup_ProductPayInfo",
				// "文件已加密到："+mFilePath.toString());
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
			}
		}
	}

	/**
	 * 生成本商品的序列号
	 *
	 * @param deviceUniqueId
	 * @return
	 */
	private String generateSerialNo(String deviceUniqueId, int ramdonKey) {
		String serialNo = deviceUniqueId + mProductId + ramdonKey;

		String encryptSerialNo = "";

		// MD5算法，生成摘要，摘要作为“序列号”
		try {
			String tempEncryptSerialNo = EncryptArithmetic.md5EncryptSerialCode(serialNo
					.getBytes(DEFALUTENCODING));
			// 根据随机key进行偏移加密
			String encryptSerialRandomKey = EncryptArithmetic.offsetEncrypt(ramdonKey + "",
					tempEncryptSerialNo);

			encryptSerialNo = tempEncryptSerialNo + encryptSerialRandomKey;
		} catch (UnsupportedEncodingException e) {
		}
		return encryptSerialNo;
	}

	/**
	 * 是否已经购买本商品
	 */
	public boolean isAlreadyPaid() {
		// boolean ret = false;
		if (Util.IS_DEBUG) {
			File file = new File("/sdcard/isPaid.loc");
			if (file.exists()) {
				return true;
			}
		}
		return sIsPaidUser;
	}

	private void initPaidState() {
		boolean ret = false;

		// 测试
//		File file = new File("/sdcard/isPaid.loc");
//		if (file.exists()) {
//			ret = true;
//			mIsPaidUser = ret;
//			return;
//		}

		String deviceID = getDeviceId();
		// Log.v("isAlreadyPaid deviceID", deviceID);
		String currentSerialCode = generateSerialNo(deviceID, mPayInfoModel.getSerialRamdonKey());
		boolean paid = mPayInfoModel.isPaid();
		if (currentSerialCode.equals(mPayInfoModel.getProductSerialCode()) && paid) {
			ret = true;
		}
		sIsPaidUser = ret;
	}

	/**
	 * 设置是否购买了本商品
	 */
	public boolean setAlreadyPaid(boolean paid) {
		boolean ret = false;
		String deviceID = "";
		try {
			deviceID = getDeviceId();
			// Log.v("setAlreadyPaid deviceId", deviceID);
			int randomKey = getRandomKey();
			// Log.v("setAlreadyPaid randomKey", randomKey+"");
			mPayInfoModel.setSerialRamdonKey(randomKey);
			String currentSerialCode = generateSerialNo(deviceID, randomKey);
			// Log.v("setAlreadyPaid currentSerialCode", currentSerialCode);
			mPayInfoModel.setProductSerialCode(currentSerialCode);
			mPayInfoModel.setPaid(paid);
			Log.v("mPayInfoModel", paid + "");

			savePayInfo();
			ret = true;
		} catch (Exception e) {
			e.printStackTrace();
			ret = false;
		}

		initPaidState();

		return ret;
	}

	private String getDeviceId() {
		String deviceID = "";
		try {
			deviceID = StatisticsTool.getVirtualDeviceId(mContext);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return deviceID;
	}
}
