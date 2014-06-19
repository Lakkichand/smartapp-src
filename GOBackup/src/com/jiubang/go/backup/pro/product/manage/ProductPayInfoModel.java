package com.jiubang.go.backup.pro.product.manage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;

/**
 * 产品付费信息
 *
 * @author ReyZhang
 */
public class ProductPayInfoModel {
	private final SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private String mInstallDate;
	private String mCurrentDate;
	private int mSerialRamdonKey;
	private String mProductSerialCode;
	private boolean mPaid;

	public String getInstallDate() {
		return mInstallDate;
	}

	public void setInstallDate(String installDate) {
		this.mInstallDate = installDate;
	}

	public String getCurrentDate() {
		return mCurrentDate;
	}

	public void setCurrentDate(String currentDate) {
		this.mCurrentDate = currentDate;
	}

	public int getSerialRamdonKey() {
		return mSerialRamdonKey;
	}

	public void setSerialRamdonKey(int serialRamdonKey) {
		this.mSerialRamdonKey = serialRamdonKey;
	}

	public String getProductSerialCode() {
		return mProductSerialCode;
	}

	public void setProductSerialCode(String registerCode) {
		this.mProductSerialCode = registerCode;
	}

	public long getInstallTimeTick() {
		Date date = null;
		try {
			date = mFormat.parse(mInstallDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date != null ? date.getTime() : 0;
	}

	public long getCurrentTimeTick() {
		Date date = null;
		try {
			date = mFormat.parse(mCurrentDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date != null ? date.getTime() : 0;
	}

	public boolean isCurrentTimeLessThanInstallTimeTick() {
		long currentTimeTick = getCurrentTimeTick();
		long installTimeTick = getInstallTimeTick();
		if (currentTimeTick < installTimeTick) {
			return true;
		}
		return false;
	}

	public boolean isPaid() {
		return mPaid;
	}

	public void setPaid(boolean mPaid) {
		this.mPaid = mPaid;
		Log.v("GoBackup", mPaid + "");
	}

	public String getXmlContent() {
		String content = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + "<root>"
				+ "<installDate>" + getInstallDate() + "</installDate>" + "<currentDate>"
				+ getCurrentDate() + "</currentDate>" + "<serialRamdonKey>" + getSerialRamdonKey()
				+ "</serialRamdonKey>" + "<serialCode>" + getProductSerialCode() + "</serialCode>"
				+ "<paid>" + isPaid() + "</paid>" + "</root>";
		return content;
	}
}
