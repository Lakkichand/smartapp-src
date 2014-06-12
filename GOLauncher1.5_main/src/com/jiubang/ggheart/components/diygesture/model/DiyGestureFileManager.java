package com.jiubang.ggheart.components.diygesture.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureStore;
import android.gesture.GestureStroke;
import android.gesture.Prediction;
import android.graphics.RectF;

import com.jiubang.ggheart.launcher.LauncherEnv;

/***
 * 自定义手势文件操作类
 * 
 * @author chenbingdong@3g.net.cn
 * 
 */
public class DiyGestureFileManager {
	private File mDiyGestureFile = null; // 自定义手势的文件
	private GestureLibrary mDiyGestureLibrary; // 自定义手势的GestureLibrary，从文件直接读取到该对象
	private double mFactor = 1.600000023841858D; // 判断手势识别的阀值 copy from gaya
													// launcher

	public DiyGestureFileManager() {
		// 从指定路径读取自定义手势文件
		mDiyGestureFile = new File(LauncherEnv.Path.SDCARD + LauncherEnv.Path.DIY_GESTURE_PATH,
				"diyGestures");
		if (mDiyGestureLibrary == null) {
			mDiyGestureLibrary = GestureLibraries.fromFile(mDiyGestureFile);
			mDiyGestureLibrary.setOrientationStyle(GestureStore.ORIENTATION_SENSITIVE);
			mDiyGestureLibrary.setSequenceType(GestureStore.SEQUENCE_SENSITIVE);
		}
	}

	/**
	 * 从自定义手势文件加载数据
	 */
	public boolean loadDiyGestureFromFile() {
		if (mDiyGestureLibrary != null) {
			try {
				mDiyGestureLibrary.load(); // 没有手势时由于还没创建手势文件，返回是未false
				return true;
			} catch (Exception e) {
				return false; // 有些用户返回load报错
			}
		}
		return false;
	}

	/**
	 * 添加手势到文件
	 * 
	 * @param gestureName
	 * @param gesture
	 *            return ture 操作成功 false 操作失败
	 */
	public boolean addGestureToFile(String gestureName, Gesture gesture) {
		mDiyGestureLibrary.addGesture(gestureName, gesture);
		return mDiyGestureLibrary.save();
	}

	/**
	 * 从手势文件删除手势
	 * 
	 * @param gestureName
	 */
	public boolean removeGestureFromFile(String gestureName) {
		mDiyGestureLibrary.removeEntry(gestureName);
		return mDiyGestureLibrary.save();
	}

	/**
	 * 通过名字获取手势
	 * 
	 * @param name
	 * @return
	 */
	public Gesture getGestureByName(String name) {
		if (mDiyGestureLibrary.getGestures(name) != null) {
			return mDiyGestureLibrary.getGestures(name).get(0);
		}
		return null;
	}

	/**
	 * 从手势文件里获取所有手势
	 * 
	 * @return
	 */
	public ArrayList<Gesture> getAllGestureFromFile() {
		ArrayList<Gesture> gestureList = new ArrayList<Gesture>();
		if (mDiyGestureLibrary.load()) {
			for (String name : mDiyGestureLibrary.getGestureEntries()) {
				for (Gesture gesture : mDiyGestureLibrary.getGestures(name)) {
					gestureList.add(gesture);
				}
			}
		}
		return gestureList;
	}

	/**
	 * 从手势文件里获取所有手势名字
	 * 
	 * @return
	 */
	public ArrayList<String> getAllGestureNameFromFile() {
		ArrayList<String> gestureNameList = new ArrayList<String>();
		if (mDiyGestureLibrary.load()) {
			for (String name : mDiyGestureLibrary.getGestureEntries()) {
				gestureNameList.add(name);
			}
		}
		return gestureNameList;
	}

	/**
	 * 手势识别获取最相似名字列表
	 * 
	 * @param gesture
	 * @return
	 */
	public ArrayList<String> getRecogiserGestureNameList(Gesture gesture) {
		ArrayList<String> nameList = new ArrayList<String>();
		ArrayList<GestureScore> gestureScoreList = recognize(gesture);
		for (GestureScore gestureScore : gestureScoreList) {
			nameList.add(gestureScore.name);
		}
		return nameList;
	}

	/**
	 * 手势比较，用于手势识别精度提高
	 * 
	 * @param gesture1
	 * @param gesture2
	 * @param gestureScore
	 * @return
	 */
	private boolean compareGesture(Gesture gesture1, Gesture gesture2, GestureScore gestureScore) {
		int strokeCount1 = gesture1.getStrokesCount();
		int strokeCount2 = gesture2.getStrokesCount();
		ArrayList<GestureStroke> strokeList1;
		ArrayList<GestureStroke> strokeList2;

		boolean result = false;

		// 两者笔画数相同
		if (strokeCount1 == strokeCount2) {
			strokeList1 = gesture1.getStrokes();
			strokeList2 = gesture2.getStrokes();

			int count = strokeCount2;

			int i = 0;
			for (i = 0; i < count; i++) {
				Gesture temp1 = new Gesture();
				GestureStroke localGestureStroke1 = strokeList1.get(i);
				temp1.addStroke(localGestureStroke1);

				Gesture temp2 = new Gesture();
				GestureStroke localGestureStroke2 = strokeList2.get(i);
				temp2.addStroke(localGestureStroke2);

				GestureStore gs = new GestureStore();
				gs.setOrientationStyle(GestureStore.ORIENTATION_SENSITIVE);
				gs.setSequenceType(GestureStore.SEQUENCE_SENSITIVE);

				String str = String.valueOf(i);
				gs.addGesture(str, temp1);

				ArrayList<Prediction> scoreList = gs.recognize(temp2);
				// 对每个笔画进行单独的比较
				if (scoreList.isEmpty()) {
					break;
				}

				double score = scoreList.get(0).score;
				if (gestureScore.minScore > score) {
					gestureScore.minScore = score;
				}

				boolean rectSimilar = isRectSimilar(temp1.getBoundingBox(), temp2.getBoundingBox());
				if (score < mFactor || !rectSimilar) {
					break;
				}
			}

			if (i >= count) {
				result = true;
			}
		}
		return result;
	}

	/**
	 * 对比两个笔划矩形是否形状相似
	 * 
	 * @param rectSrc
	 * @param rectDst
	 * @return
	 */
	private boolean isRectSimilar(RectF rectSrc, RectF rectDst) {
		if (null == rectSrc || null == rectDst || rectSrc.width() <= 0 || rectSrc.height() <= 0
				|| rectDst.width() <= 0 || rectDst.height() <= 0) {
			return false;
		}

		boolean ret = false;
		float rateSrc = rectSrc.height() / rectSrc.width();
		float rateDst = rectDst.height() / rectDst.width();
		boolean reverseDirection = (rateSrc - 1) * (rateDst - 1) < 0;
		if (reverseDirection) {
			// 矩形反向：高矩形与宽矩形
			float lowrate = 0.6f;
			float hightrate = 1.4f;
			if (lowrate <= rateSrc && rateSrc <= hightrate && lowrate <= rateDst
					&& rateDst <= hightrate) {
				ret = true;
			}
		} else {
			if (rateSrc >= 1 && rateDst >= 1) {
				rateSrc = 1.0f / rateSrc;
				rateDst = 1.0f / rateDst;
			}
			float lowrate = rateSrc - 0.5f;
			float hightrate = rateSrc + 0.5f;
			if (lowrate <= rateDst && rateDst <= hightrate) {
				ret = true;
			}
		}
		return ret;
	}

	/**
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 * 
	 * @author  licanhui
	 * @date  [2012-9-26]
	 */
	private static class GestureScore {
		Gesture gesture;
		String name;
		double minScore = Double.MAX_VALUE; // 所有笔画的匹配中得分最低的分数
	}

	private GestureScore getMaxGestureScore(String name, Gesture g, double predictionScore) {
		ArrayList<Gesture> list = mDiyGestureLibrary.getGestures(name);
		if (list == null || list.isEmpty()) {
			return null;
		}

		int size = list.size();
		double maxScore = 0;
		GestureScore finalScore = null;
		for (int i = 0; i < size; i++) {
			GestureScore gestureScore = new GestureScore();
			gestureScore.gesture = list.get(i);
			gestureScore.name = name;

			boolean match = compareGesture(gestureScore.gesture, g, gestureScore);
			if (match && maxScore < gestureScore.minScore) {
				maxScore = gestureScore.minScore;
				finalScore = gestureScore;
			}
		}

		if (finalScore != null) {
			// 取所有笔画的最小分数 + 整体比较分数
			finalScore.minScore += predictionScore;
		}
		return finalScore;
	}

	/**
	 * 手势识别方法，对原有识别方法进行精度的提高
	 * 
	 * @param gesture
	 * @return
	 */
	private ArrayList<GestureScore> recognize(Gesture gesture) {
		ArrayList<GestureScore> gestureScoreList = new ArrayList<DiyGestureFileManager.GestureScore>();
		try {
			//TODO：这个方法系统有可能排序报错，暂没找到原因
			ArrayList<Prediction> scoreList = mDiyGestureLibrary.recognize(gesture);
			int size = scoreList.size();
			Prediction prediction = null;
			for (int i = 0; i < size; i++) {
				prediction = scoreList.get(i);
				if (prediction.score > mFactor) {
					GestureScore gestureScore = getMaxGestureScore(prediction.name, gesture,
							prediction.score);
					if (gestureScore != null) {
						gestureScoreList.add(gestureScore);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 根据minScore对gestureScoreList进行排序，按分数从高到低排列
		GestureScoreComparator comparator = new GestureScoreComparator();
		Collections.sort(gestureScoreList, comparator);
		return gestureScoreList;
	}

	/**
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 * 
	 * @author  licanhui
	 * @date  [2012-9-26]
	 */
	private class GestureScoreComparator implements Comparator<GestureScore> {

		@Override
		public int compare(GestureScore object1, GestureScore object2) {
			if (object1.minScore < object2.minScore) {
				return 1;
			}
			return -1;
		}
	}
}
