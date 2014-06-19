package com.jiubang.go.backup.pro.product.manage;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

/**
 * 产品管理
 * 
 * @author ReyZhang
 */
public class ProductManager {

	private static Map<String, ProductPayInfo> sProductMap = new HashMap<String, ProductPayInfo>();

	public static ProductPayInfo getProductPayInfo(Context context, String productId) {
		ProductPayInfo payInfo = sProductMap.get(productId);
		if (payInfo == null) {
			payInfo = new ProductPayInfo(context, productId);
			sProductMap.put(productId, payInfo);
		}
		return payInfo;
	}
	
	public static boolean isPaid(Context context, String productId) {
		ProductPayInfo productInfo = getProductPayInfo(context, productId);
		return productInfo != null && productInfo.isAlreadyPaid();
	}
	
	public static boolean isPaid(Context context) {
		return isPaid(context, ProductPayInfo.PRODUCT_ID) || ProductPayInfo.sIsPaidUserByKey;
	}
}
