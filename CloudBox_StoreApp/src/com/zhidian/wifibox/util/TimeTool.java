package com.zhidian.wifibox.util;

import com.ta.TAApplication;
import com.zhidian.wifibox.R;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

public class TimeTool {

	public static int[] TimeForString(int s) {

		int[] i = new int[5];
		int N = s / 3600;// 小时
		i[0] = N;
		
		s = s % 3600;
		int K = s / 60;// 分钟
		i[1] = K;		
		
		
		s = s % 60;
		int M = s;// 秒
		i[2] = M;	

		return i;

	}
	
//	public static int[] TimeForString(int s) {
//
//		int[] i = new int[5];
//		int N = s / 3600;// 小时
//		i[0] = N;
//		
//		s = s % 3600;
//		int K = s / 60;// 分钟
//		if (K < 10) {
//			i[1] = 0;
//			i[2] = K;
//		}else {
//			i[1] = (K / 10)%10;//取十位数
//			i[2] = K % 10;//取个位数
//		}
//		
//		
//		s = s % 60;
//		int M = s;// 秒
//		if (M < 10) {
//			i[3] = 0;
//			i[4] = M;
//		}else {
//			
//			i[3] = (M / 10)%10;
//			i[4] = M % 10;
//		}	
//
//		return i;
//
//	}

	public static int getMathBitmap(int i) {
		int seId = 0;
		switch (i) {
		case 0:
			seId =  R.drawable.zero;			
			break;
		case 1:
			seId =  R.drawable.one;
			break;
		case 2:
			seId =  R.drawable.two;
			break;
		case 3:
			seId =  R.drawable.three;
			break;
		case 4:
			seId =  R.drawable.four;
			break;
		case 5:
			seId =  R.drawable.five;
			break;
		case 6:
			seId =  R.drawable.six;
			break;
		case 7:
			seId =  R.drawable.seven;
			break;
		case 8:
			seId =  R.drawable.eight;
			break;
		case 9:
			seId =  R.drawable.nine;
			break;

		default:
			break;
		}
		return seId;

	}
	
//	public static Bitmap getMathBitmap(int i) {
//		Bitmap bitmap = null;
//		switch (i) {
//		case 0:
//			bitmap = ((BitmapDrawable) TAApplication.getApplication()
//					.getResources().getDrawable(R.drawable.zero)).getBitmap();
//			break;
//		case 1:
//			bitmap = ((BitmapDrawable) TAApplication.getApplication()
//					.getResources().getDrawable(R.drawable.one)).getBitmap();
//			break;
//		case 2:
//			bitmap = ((BitmapDrawable) TAApplication.getApplication()
//					.getResources().getDrawable(R.drawable.two)).getBitmap();
//			break;
//		case 3:
//			bitmap = ((BitmapDrawable) TAApplication.getApplication()
//					.getResources().getDrawable(R.drawable.three)).getBitmap();
//			break;
//		case 4:
//			bitmap = ((BitmapDrawable) TAApplication.getApplication()
//					.getResources().getDrawable(R.drawable.four)).getBitmap();
//			break;
//		case 5:
//			bitmap = ((BitmapDrawable) TAApplication.getApplication()
//					.getResources().getDrawable(R.drawable.five)).getBitmap();
//			break;
//		case 6:
//			bitmap = ((BitmapDrawable) TAApplication.getApplication()
//					.getResources().getDrawable(R.drawable.six)).getBitmap();
//			break;
//		case 7:
//			bitmap = ((BitmapDrawable) TAApplication.getApplication()
//					.getResources().getDrawable(R.drawable.seven)).getBitmap();
//			break;
//		case 8:
//			bitmap = ((BitmapDrawable) TAApplication.getApplication()
//					.getResources().getDrawable(R.drawable.eight)).getBitmap();
//			break;
//		case 9:
//			bitmap = ((BitmapDrawable) TAApplication.getApplication()
//					.getResources().getDrawable(R.drawable.nine)).getBitmap();
//			break;
//
//		default:
//			break;
//		}
//		return bitmap;
//
//	}
}
