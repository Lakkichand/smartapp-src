package com.gau.go.launcherex.theme.cover;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.gau.go.launcherex.theme.cover.CoverBean.DeerBean;
import com.gau.go.launcherex.theme.cover.CoverBean.EmptySleighBean;
import com.gau.go.launcherex.theme.cover.CoverBean.FallSantaBean;
import com.gau.go.launcherex.theme.cover.CoverBean.FlagBean;
import com.gau.go.launcherex.theme.cover.CoverBean.GiftBean;
import com.gau.go.launcherex.theme.cover.CoverBean.MovePointBean;
import com.gau.go.launcherex.theme.cover.CoverBean.PackageBean;
import com.gau.go.launcherex.theme.cover.CoverBean.RabbitBean;
import com.gau.go.launcherex.theme.cover.CoverBean.RunSantaBean;
import com.gau.go.launcherex.theme.cover.CoverBean.SantaBean;
import com.gau.go.launcherex.theme.cover.CoverBean.SleighBean;
import com.gau.go.launcherex.theme.cover.CoverBean.UpSantaBean;
import com.gau.go.launcherex.theme.cover.CoverBean.WhipBean;
import com.gau.go.launcherex.theme.cover.utils.CoverterUtils;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.graphics.Color;
import android.util.Log;

/**
 * 罩子层主题解析器
 * 
 * @author jiangxuwen
 * 
 */
public class CoverParser {
	private static final String FILENAME = "cover";

	protected CoverBean createThemeBean(String pkgName) {
		return new CoverBean(pkgName);
	}

	/**
	 * 
	 * @param context
	 * @param themePackage
	 *            主题包名
	 * @return
	 */
	public CoverBean autoParseAppThemeXml(Context context, String themePackage) {
		// public ThemeBean autoParseAppThemeXml(Context context, String
		// themePackage) {
		CoverBean coverBean = null;
		// 解析应用程序过滤器信息
		if (FILENAME == null) {
//			Log.i("IParser",
//					"Auto Parse failed, you should init mAutoParserFileName first");
			return coverBean;
		}
		InputStream inputStream = null;
		XmlPullParser xmlPullParser = null;
		inputStream = XmlParserFactory.createInputStream(context, themePackage,
				FILENAME);
		if (inputStream == null) {
			// Context ctx = AppUtils.getAppContext(context, themePackage);
			Context ctx = context;
			if (ctx != null && FILENAME != null) {
				int id = ctx.getResources().getIdentifier(FILENAME, "raw",
						themePackage);
				try {
					xmlPullParser = ctx.getResources().getXml(id);
				} catch (NotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		if (xmlPullParser == null) {
			xmlPullParser = XmlParserFactory.createXmlParser(inputStream);
		}
		if (xmlPullParser != null) {
			coverBean = createThemeBean(themePackage);
			if (coverBean == null) {
				Log.i("IParser",
						"Auto Parse failed, you should override createThemeBean() method");
				return coverBean;
			}
			parseXml(xmlPullParser, coverBean);
		}
		// 关闭inputStream
		if (inputStream != null) {
			try {
				inputStream.close();
			} catch (IOException e) {
				Log.i("ThemeManager", "IOException for close inputSteam");
			}
		}
		return coverBean;
	}

	public void parseXml(XmlPullParser xmlPullParser, CoverBean bean) {
		// // 测试代码
		// bean = new DeskThemeBean();
		// xmlPullParser = mContext.getResources().getXml(R.xml.desk);
		// 数据验证
		if (null == xmlPullParser) {
			return;
		}
		CoverBean coverBean = null;
		if (bean instanceof CoverBean) {
			coverBean = (CoverBean) bean;
		}
		if (null == coverBean) {
			return;
		}

		String attributeValue = null;
		// 解析
		try {
			int eventType = xmlPullParser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG) {
					// 标签名
					String tagName = xmlPullParser.getName();

					if (tagName.equals(AttributeSet.COVER)) {
						attributeValue = xmlPullParser.getAttributeValue(null,
								AttributeSet.VERSION);
						coverBean.mDeskVersion = Float.valueOf(attributeValue);
					} else if (tagName.equals(AttributeSet.NORMAL)) {
						parceNormal(xmlPullParser, coverBean);
					} else if (tagName.equals(AttributeSet.ROTATE)) {
						parceRotate(xmlPullParser, coverBean);
					} else if (tagName.equals(AttributeSet.TRANSLATE)) {
						parceTranslate(xmlPullParser, coverBean);
					} else if (tagName.equals(AttributeSet.TOUCH)) {
						parceTouch(xmlPullParser, coverBean);
					} else if (tagName.equals(AttributeSet.LONGTOUCH)) {
						parceLongTouch(xmlPullParser, coverBean);
					} else if (tagName.equals(AttributeSet.MOVE)) {
						parceMove(xmlPullParser, coverBean);
					} else if (tagName.equals(AttributeSet.SPRITE)) {
						parceSprite(xmlPullParser, coverBean);
					} else if (tagName.equals(AttributeSet.RANDOM)) {
						parceRandom(xmlPullParser, coverBean);
					} else if (tagName.equals(AttributeSet.CLOCK)) {
						parceClock(xmlPullParser, coverBean);
					} else if (tagName.equals(AttributeSet.BALLOON)) {
						parceBalloon(xmlPullParser, coverBean);
					} else if (tagName.equals(AttributeSet.SLIDE)) {
						parceSlide(xmlPullParser, coverBean);
					} else if (tagName.equals(AttributeSet.SLEIGH)) {
						parceSleigh(xmlPullParser, coverBean);
					}
				}
				eventType = xmlPullParser.next();
			}
		} catch (XmlPullParserException e) {
//			Log.i("DeskThemeParser", "parseXml has XmlPullParserException = "
//					+ e.getMessage());
		} catch (IOException e) {
//			Log.i("DeskThemeParser",
//					"parseXml has IOException = " + e.getMessage());
		} catch (Exception e) {
			// e.printStackTrace();
//			Log.i("DeskThemeParser",
//					"parseXml has Exception = " + e.getMessage());
		}
	}

	private void parceNormalSelf(XmlPullParser xmlPullParser,
			CoverBean.NormalBean item) {
		String attributeValue = null;
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.IMAGENAME);
		item.mImageName = CoverterUtils.stringToString(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.ALLOWDRAG);
		// layer.mHeight = stringToInt(attributeValue);
		item.mAllowDrag = CoverterUtils.stringToBoolean(attributeValue);
	}

	private void parceRotateSelf(XmlPullParser xmlPullParser,
			CoverBean.RotateBean item) {
		String attributeValue = null;
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.IMAGENAME);
		item.mImageName = CoverterUtils.stringToString(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.BODYIMGNAME_STRING);
		item.mBodyImgName = CoverterUtils.stringToString(attributeValue);

		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.ROPEHEIGHT);
		item.mRopeHeight = CoverterUtils.stringToFloat(attributeValue);

		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.ALLOWDRAG);
		item.mAllowDrag = CoverterUtils.stringToBoolean(attributeValue);
	}

	private void parceTranslateSelf(XmlPullParser xmlPullParser,
			CoverBean.TranslateBean item) {
		parceNormalSelf(xmlPullParser, item);
	}

	private void parceTouchSelf(XmlPullParser xmlPullParser,
			CoverBean.TouchBean item) {
		parceRandomSelf(xmlPullParser, item);
	}

	private void parceRandomSelf(XmlPullParser xmlPullParser,
			CoverBean.RandomBean item) {
		String attributeValue = null;
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.ALLOWDRAG);
		item.mAllowDrag = CoverterUtils.stringToBoolean(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.PROBABILITY);
		item.mProbability = CoverterUtils.stringToInt(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.MINTUMBLESPDX);
		item.mMinTumbleSpdX = CoverterUtils.stringToInt(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.MAXTUMBLESPDX);
		item.mMaxTumbleSpdX = CoverterUtils.stringToInt(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.MINTUMBLESPDY);
		item.mMinTumbleSpdY = CoverterUtils.stringToInt(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.MAXTUMBLESPDY);
		item.mMaxTumbleSpdY = CoverterUtils.stringToInt(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.MINSPEEDX);
		item.mMinSpeedX = CoverterUtils.stringToInt(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.MAXSPEEDX);
		item.mMaxSpeedX = CoverterUtils.stringToInt(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.MINSPEEDY);
		item.mMinSpeedY = CoverterUtils.stringToInt(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.MAXSPEEDY);
		item.mMaxSpeedY = CoverterUtils.stringToInt(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.MINSCALE);
		item.mMinScale = CoverterUtils.stringToFloat(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.MAXSCALE);
		item.mMaxScale = CoverterUtils.stringToFloat(attributeValue);

		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.ANIMATIONTYPE);
		item.mAnimationType = CoverterUtils.stringToInt(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.STARTMINLOCATIONX);
		item.mStartMinLocationX = CoverterUtils.stringToFloat(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.STARTMINLOCATIONY);
		item.mStartMinLocationY = CoverterUtils.stringToFloat(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.STARTMAXLOCATIONX);
		item.mStartMaxLocationX = CoverterUtils.stringToFloat(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.STARTMAXLOCATIONY);
		item.mStartMaxLocationY = CoverterUtils.stringToFloat(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.SHOWTYPE);
		item.mShowType = CoverterUtils.stringToInt(attributeValue);

		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.RANDOMNUM);
		item.mRandomNum = CoverterUtils.stringToInt(attributeValue);

		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.INTERVAL);
		item.mInterval = CoverterUtils.stringToLong(attributeValue);

	}

	private void parceClockSelf(XmlPullParser xmlPullParser,
			CoverBean.ClockBean item) {
		String attributeValue = null;
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.ALLOWDRAG);
		item.mAllowDrag = CoverterUtils.stringToBoolean(attributeValue);
	}

	private void parceBallonSelf(XmlPullParser xmlPullParser,
			CoverBean.BalloonBean item) {
		String attributeValue = null;
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.ALLOWDRAG);
		item.mAllowDrag = CoverterUtils.stringToBoolean(attributeValue);
	}

	private void parceLongTouchSelf(XmlPullParser xmlPullParser,
			CoverBean.LongTounchBean item) {

		String attributeValue = null;
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.ALLOWDRAG);
		item.mAllowDrag = CoverterUtils.stringToBoolean(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.LOOP);
		item.mLoopCount = CoverterUtils.stringToInt(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.MISSONTOUCHUP);
		item.mMissOnTouchUp = CoverterUtils.stringToBoolean(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.MAXCOUNT);
		item.mMaxCount = CoverterUtils.stringToInt(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.INANIMATIONTYPE);
		item.mInAnimationType = CoverterUtils.stringToInt(attributeValue);
	}

	private void parceMoveSelf(XmlPullParser xmlPullParser,
			CoverBean.MoveBean item) {
		String attributeValue = null;
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.PROBABILITY);
		item.mProbability = CoverterUtils.stringToInt(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.MINTUMBLESPDX);
		item.mMinTumbleSpdX = CoverterUtils.stringToInt(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.MAXTUMBLESPDX);
		item.mMaxTumbleSpdX = CoverterUtils.stringToInt(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.MINTUMBLESPDY);
		item.mMinTumbleSpdY = CoverterUtils.stringToInt(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.MAXTUMBLESPDY);
		item.mMaxTumbleSpdY = CoverterUtils.stringToInt(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.MINSPEEDX);
		item.mMinSpeedX = CoverterUtils.stringToInt(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.MAXSPEEDX);
		item.mMaxSpeedX = CoverterUtils.stringToInt(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.MINSPEEDY);
		item.mMinSpeedY = CoverterUtils.stringToInt(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.MAXSPEEDY);
		item.mMaxSpeedY = CoverterUtils.stringToInt(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.MINSCALE);
		item.mMinScale = CoverterUtils.stringToFloat(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.MAXSCALE);
		item.mMaxScale = CoverterUtils.stringToFloat(attributeValue);
	}

	private void parceSpriteSelf(XmlPullParser xmlPullParser,
			CoverBean.SpriteBean item) {
		String attributeValue = null;
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.ALLOWDRAG);
		item.mAllowDrag = CoverterUtils.stringToBoolean(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.DEFAULTANGLE);
		item.mDefaultAngle = CoverterUtils.stringToFloat(attributeValue);
	}

	private void parceStartLocation(XmlPullParser xmlPullParser,
			CoverBean.StartLocation location) {
		String attributeValue = null;
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.CREATESHOW);
		location.mCreateShow = CoverterUtils.stringToBoolean(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.STARTX);
		location.mStartX = CoverterUtils.stringToFloat(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.STARTXLAND);
		location.mStartLandX = CoverterUtils.stringToFloat(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.STARTY);
		location.mStartY = CoverterUtils.stringToFloat(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.STARTYLAND);
		location.mStartLandY = CoverterUtils.stringToFloat(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.LOCATIONTYPE);
		location.mLocationType = CoverterUtils.stringToInt(attributeValue);
	}

	private void parceRotateXY(XmlPullParser xmlPullParser,
			CoverBean.RotateXY rotateXY) {
		String attributeValue = null;
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.PIVOTX);
		rotateXY.mPivotX = CoverterUtils.stringToFloat(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.PIVOTY);
		rotateXY.mPivotY = CoverterUtils.stringToFloat(attributeValue);
	}

	private void parceShadow(XmlPullParser xmlPullParser,
			CoverBean.Shadow shadow) {
		String attributeValue = null;
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.IMAGENAME);
		shadow.mImageName = CoverterUtils.stringToString(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.NEEDSHADOW);
		shadow.mNeedShadow = CoverterUtils.stringToBoolean(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.NEXTTOX);
		shadow.mNextToX = CoverterUtils.stringToInt(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.NEXTTOY);
		shadow.mNextToY = CoverterUtils.stringToInt(attributeValue);
	}

	private void parceLimitArea(XmlPullParser xmlPullParser,
			CoverBean.LimitArea area) {
		String attributeValue = null;
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.LIMIT);
		area.mLimit = CoverterUtils.stringToBoolean(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.LEFT);
		area.mLeft = CoverterUtils.stringToFloat(attributeValue);
		attributeValue = xmlPullParser
				.getAttributeValue(null, AttributeSet.TOP);
		area.mTop = CoverterUtils.stringToFloat(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.RIGHT);
		area.mRight = CoverterUtils.stringToFloat(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.BOTTOM);
		area.mBottom = CoverterUtils.stringToFloat(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.TOUCHOFFSETX);
		area.mTouchOffsetX = CoverterUtils.stringToFloat(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.TOUCHOFFSETY);
		area.mTouchOffsetY = CoverterUtils.stringToFloat(attributeValue);
	}

	private void parceRelativeLocation(XmlPullParser xmlPullParser,
			CoverBean.RelativeLocation location) {
		String attributeValue = null;
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.LANDSCAPETYPE);
		location.mLandscapeType = CoverterUtils.stringToInt(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.VERTICALTYPE);
		location.mVerticalType = CoverterUtils.stringToInt(attributeValue);
	}

	private void parceRotateAnim(XmlPullParser xmlPullParser,
			CoverBean.RotateAnim anim) {
		String attributeValue = null;
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.DURATION);
		anim.mDuration = CoverterUtils.stringToLong(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.LOOP);
		anim.mLoop = CoverterUtils.stringToBoolean(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.FROMDEGRESS);
		anim.mFromDegress = CoverterUtils.stringToInt(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.TODEGRESS);
		anim.mToDegress = CoverterUtils.stringToInt(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.PIVOTX);
		anim.mPivotX = CoverterUtils.stringToFloat(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.PIVOTY);
		anim.mPivotY = CoverterUtils.stringToFloat(attributeValue);
	}

	private void parceTranslateAnim(XmlPullParser xmlPullParser,
			CoverBean.TranslateAnim anim) {
		String attributeValue = null;
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.DURATION);
		anim.mDuration = CoverterUtils.stringToLong(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.LOOP);
		anim.mLoop = CoverterUtils.stringToBoolean(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.BACKROTATE);
		anim.mBackRotate = CoverterUtils.stringToBoolean(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.FROMXDELTA);
		anim.mFromXDelta = CoverterUtils.stringToInt(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.TOXDELTA);
		anim.mToXDelta = CoverterUtils.stringToInt(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.FROMYDELTA);
		anim.mFromYDelta = CoverterUtils.stringToInt(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.TOYDELTA);
		anim.mToYDelta = CoverterUtils.stringToInt(attributeValue);
	}

	private void parceLife(XmlPullParser xmlPullParser, CoverBean.Life life) {
		String attributeValue = null;
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.STARTAGE);
		life.mStartAge = CoverterUtils.stringToInt(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.ENDAGE);
		life.mEndAge = CoverterUtils.stringToInt(attributeValue);
	}

	private void parceAxis(XmlPullParser xmlPullParser, CoverBean.Axis axis) {
		String attributeValue = null;
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.AXISX);
		axis.mAxisX = CoverterUtils.stringToFloat(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.AXISY);
		axis.mAxisY = CoverterUtils.stringToFloat(attributeValue);
	}

	private void parceClockHand(XmlPullParser xmlPullParser,
			CoverBean coverBean, CoverBean.ClockBean clockBean)
			throws XmlPullParserException, IOException {
		// 子属性
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				// 解析完毕
				// if (tagName.equals(TagSet.SHOWITEMLAYER))
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(AttributeSet.MINUTEHAND)) {
					CoverBean.ClockHand minuteHand = coverBean
							.createClockHand();
					parceCellHand(xmlPullParser, minuteHand);
					clockBean.mMinuteHand = minuteHand;
				} else if (tagName.equals(AttributeSet.HOURHAND)) {
					CoverBean.ClockHand hourHand = coverBean.createClockHand();
					parceCellHand(xmlPullParser, hourHand);
					clockBean.mHourHand = hourHand;
				} else if (tagName.equals(AttributeSet.SECONDHAND)) {
					CoverBean.ClockHand secondHand = coverBean
							.createClockHand();
					parceCellHand(xmlPullParser, secondHand);
					clockBean.mSecondHand = secondHand;
				}
			}
			eventType = xmlPullParser.next();
		}
	}

	private void parceCellHand(XmlPullParser xmlPullParser,
			CoverBean.ClockHand clockHand) {
		String attributeValue = null;
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.IMAGENAME);
		clockHand.mImageName = CoverterUtils.stringToString(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.PIVOTX);
		clockHand.mPivotX = CoverterUtils.stringToFloat(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.PIVOTY);
		clockHand.mPivotY = CoverterUtils.stringToFloat(attributeValue);
	}

	private void parceBitmapFrame(XmlPullParser xmlPullParser,
			List<String> imageNames) throws XmlPullParserException, IOException {
		// 子属性
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				// 解析完毕
				// if (tagName.equals(TagSet.SHOWITEMLAYER))
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(AttributeSet.ITEM)) {
					String attributeValue = xmlPullParser.getAttributeValue(
							null, AttributeSet.IMAGENAME);
					imageNames
							.add(CoverterUtils.stringToString(attributeValue));
				}
			}
			eventType = xmlPullParser.next();
		}
	}

	private void parceItems(XmlPullParser xmlPullParser, List<String> imageNames)
			throws XmlPullParserException, IOException {
		// 子属性
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				// 解析完毕
				// if (tagName.equals(TagSet.SHOWITEMLAYER))
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(AttributeSet.ITEM)) {
					String attributeValue = xmlPullParser.getAttributeValue(
							null, AttributeSet.IMAGENAME);
					imageNames
							.add(CoverterUtils.stringToString(attributeValue));
				}
			}
			eventType = xmlPullParser.next();
		}
	}

	/**
	 * LongTouch特别处理
	 * 
	 * @param xmlPullParser
	 * @param imageNames
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private void parceLongTouchBitmapFrame(XmlPullParser xmlPullParser,
			List<CoverBean.BitmapItem> imageTop,
			List<CoverBean.BitmapItem> imageBottom, CoverBean coverBean)
			throws XmlPullParserException, IOException {
		// 子属性
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				// 解析完毕
				// if (tagName.equals(TagSet.SHOWITEMLAYER))
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(AttributeSet.FRAMETOP)) {
					parceBitmapFrameTop(xmlPullParser, imageTop, coverBean);
				} else if (tagName.equals(AttributeSet.FRAMEBOTTOM)) {
					parceBitmapFrameBottom(xmlPullParser, imageBottom,
							coverBean);
				}
			}
			eventType = xmlPullParser.next();
		}
	}

	/**
	 * LongTouch特别处理
	 * 
	 * @param xmlPullParser
	 * @param imageNames
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private void parceBitmapFrameTop(XmlPullParser xmlPullParser,
			List<CoverBean.BitmapItem> imageTop, CoverBean coverBean)
			throws XmlPullParserException, IOException {
		// 子属性
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				// 解析完毕
				// if (tagName.equals(TagSet.SHOWITEMLAYER))
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(AttributeSet.ITEM)) {
					CoverBean.BitmapItem bitmapItem = coverBean
							.createBitmapItem();
					bitmapItem.mBitmapName = xmlPullParser.getAttributeValue(
							null, AttributeSet.IMAGENAME);
					bitmapItem.mDuration = CoverterUtils
							.stringToInt(xmlPullParser.getAttributeValue(null,
									AttributeSet.DURATION));
					imageTop.add(bitmapItem);
				}
			}
			eventType = xmlPullParser.next();
		}
	}

	/**
	 * LongTouch特别处理
	 * 
	 * @param xmlPullParser
	 * @param imageNames
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private void parceBitmapFrameBottom(XmlPullParser xmlPullParser,
			List<CoverBean.BitmapItem> imageBottom, CoverBean coverBean)
			throws XmlPullParserException, IOException {
		// 子属性
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				// 解析完毕
				// if (tagName.equals(TagSet.SHOWITEMLAYER))
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(AttributeSet.ITEM)) {
					CoverBean.BitmapItem bitmapItem = coverBean
							.createBitmapItem();
					bitmapItem.mBitmapName = xmlPullParser.getAttributeValue(
							null, AttributeSet.IMAGENAME);
					bitmapItem.mDuration = CoverterUtils
							.stringToInt(xmlPullParser.getAttributeValue(null,
									AttributeSet.DURATION));
					imageBottom.add(bitmapItem);
				}
			}
			eventType = xmlPullParser.next();
		}
	}

	private void parceBitmapFrameAndShadow(XmlPullParser xmlPullParser,
			CoverBean coverBean, CoverBean.SpriteAction action)
			throws XmlPullParserException, IOException {
		// 子属性
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				// 解析完毕
				// if (tagName.equals(TagSet.SHOWITEMLAYER))
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(AttributeSet.ITEM)) {
					String attributeValue = xmlPullParser.getAttributeValue(
							null, AttributeSet.IMAGENAME);
					action.mActionImageNames.add(CoverterUtils
							.stringToString(attributeValue));
				} else if (tagName.equals(AttributeSet.SHADOW)) {
					CoverBean.Shadow shadow = coverBean.createShadow();
					parceShadow(xmlPullParser, shadow);
					action.mShadow = shadow;
				} else if (tagName.equals(AttributeSet.ADDITIONALACTION)) {
					action.mAdditionalAction = coverBean.createSpriteAction();
					String attributeValue = xmlPullParser.getAttributeValue(
							null, AttributeSet.IMAGENAME);
					action.mAdditionalAction.mNormalImage = CoverterUtils
							.stringToString(attributeValue);
					attributeValue = xmlPullParser.getAttributeValue(null,
							AttributeSet.ENDTYPE);
					action.mAdditionalAction.mEndType = CoverterUtils
							.stringToInt(attributeValue);
					attributeValue = xmlPullParser.getAttributeValue(null,
							AttributeSet.ACTIONTYPE);
					action.mAdditionalAction.mActionType = CoverterUtils
							.stringToInt(attributeValue);
					attributeValue = xmlPullParser.getAttributeValue(null,
							AttributeSet.DELAYTIME);
					action.mAdditionalAction.mDelayTime = CoverterUtils
							.stringToLong(attributeValue);
					attributeValue = xmlPullParser.getAttributeValue(null,
							AttributeSet.ANIMATIONTIME);
					action.mAdditionalAction.mAnimationTime = CoverterUtils
							.stringToInt(attributeValue);
					attributeValue = xmlPullParser.getAttributeValue(null,
							AttributeSet.NEEDLOOP);
					action.mAdditionalAction.mNeedLoop = CoverterUtils
							.stringToBoolean(attributeValue);

					parceBitmapFrameAndShadow(xmlPullParser, coverBean,
							action.mAdditionalAction);
				}
			}
			eventType = xmlPullParser.next();
		}
	}

	private void parceSpriteAction(XmlPullParser xmlPullParser,
			CoverBean coverBean, CoverBean.SpriteBean spriteBean)
			throws XmlPullParserException, IOException {
		// 子属性
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				// 解析完毕
				// if (tagName.equals(TagSet.SHOWITEMLAYER))
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(AttributeSet.ACTIONONE)) {
					String attributeValue = xmlPullParser.getAttributeValue(
							null, AttributeSet.IMAGENAME);
					spriteBean.mSpriteAction1.mNormalImage = CoverterUtils
							.stringToString(attributeValue);
					attributeValue = xmlPullParser.getAttributeValue(null,
							AttributeSet.ENDTYPE);
					spriteBean.mSpriteAction1.mEndType = CoverterUtils
							.stringToInt(attributeValue);
					attributeValue = xmlPullParser.getAttributeValue(null,
							AttributeSet.ACTIONTYPE);
					spriteBean.mSpriteAction1.mActionType = CoverterUtils
							.stringToInt(attributeValue);
					attributeValue = xmlPullParser.getAttributeValue(null,
							AttributeSet.DELAYTIME);
					spriteBean.mSpriteAction1.mDelayTime = CoverterUtils
							.stringToLong(attributeValue);
					attributeValue = xmlPullParser.getAttributeValue(null,
							AttributeSet.ANIMATIONTIME);
					spriteBean.mSpriteAction1.mAnimationTime = CoverterUtils
							.stringToInt(attributeValue);
					attributeValue = xmlPullParser.getAttributeValue(null,
							AttributeSet.NEEDLOOP);
					spriteBean.mSpriteAction1.mNeedLoop = CoverterUtils
							.stringToBoolean(attributeValue);
					attributeValue = xmlPullParser.getAttributeValue(null,
							AttributeSet.ISBITMAPSYMMETRIC);
					spriteBean.mSpriteAction1.mIsBitmapSymmetric = CoverterUtils
							.stringToBoolean(attributeValue);

					parceBitmapFrameAndShadow(xmlPullParser, coverBean,
							spriteBean.mSpriteAction1);
				} else if (tagName.equals(AttributeSet.ACTIONTWO)) {
					String attributeValue = xmlPullParser.getAttributeValue(
							null, AttributeSet.IMAGENAME);
					spriteBean.mSpriteAction2.mNormalImage = CoverterUtils
							.stringToString(attributeValue);
					attributeValue = xmlPullParser.getAttributeValue(null,
							AttributeSet.ENDTYPE);
					spriteBean.mSpriteAction2.mEndType = CoverterUtils
							.stringToInt(attributeValue);
					attributeValue = xmlPullParser.getAttributeValue(null,
							AttributeSet.ACTIONTYPE);
					spriteBean.mSpriteAction2.mActionType = CoverterUtils
							.stringToInt(attributeValue);
					attributeValue = xmlPullParser.getAttributeValue(null,
							AttributeSet.DELAYTIME);
					spriteBean.mSpriteAction2.mDelayTime = CoverterUtils
							.stringToLong(attributeValue);
					spriteBean.mSpriteAction2.mAnimationTime = CoverterUtils
							.stringToInt(attributeValue);
					attributeValue = xmlPullParser.getAttributeValue(null,
							AttributeSet.NEEDLOOP);
					spriteBean.mSpriteAction2.mNeedLoop = CoverterUtils
							.stringToBoolean(attributeValue);
					parceBitmapFrameAndShadow(xmlPullParser, coverBean,
							spriteBean.mSpriteAction2);
				} else if (tagName.equals(AttributeSet.DRAG_ACTION)) {
					parceSpirtDragAction(xmlPullParser, coverBean, spriteBean);
				} else if (tagName.equals(AttributeSet.SHAKE_ACTION)) {
					parceSpirtShakeAction(xmlPullParser, coverBean, spriteBean);
				}
			}
			eventType = xmlPullParser.next();
		}
	}

	private void parceNormalItem(XmlPullParser xmlPullParser,
			CoverBean coverBean) throws XmlPullParserException, IOException {
		CoverBean.NormalBean normalBean = coverBean.createNormalBean();
		// 自身属性
		parceNormalSelf(xmlPullParser, normalBean);
		// 子属性
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				// 解析完毕
				// if (tagName.equals(TagSet.SHOWITEMLAYER))
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(AttributeSet.STARTLOCATION)) {
					CoverBean.StartLocation location = coverBean
							.createStartLocation();
					parceStartLocation(xmlPullParser, location);
					normalBean.mStartLocation = location;
				} else if (tagName.equals(AttributeSet.SHADOW)) {
					CoverBean.Shadow shadow = coverBean.createShadow();
					parceShadow(xmlPullParser, shadow);
					normalBean.mShadow = shadow;
				} else if (tagName.equals(AttributeSet.LIMITAREA)) {
					CoverBean.LimitArea area = coverBean.createLimitArea();
					parceLimitArea(xmlPullParser, area);
					normalBean.mLimitArea = area;
				} else if (tagName.equals(AttributeSet.RELATIVELOCATION)) {
					CoverBean.RelativeLocation relativeLocation = coverBean
							.createRelativeLocation();
					parceRelativeLocation(xmlPullParser, relativeLocation);
					normalBean.mRelativeLocation = relativeLocation;
				}
			}
			eventType = xmlPullParser.next();
		}
		// 添加进集合
		coverBean.mNormalMap.add(normalBean);
	}

	private void parceRotateItem(XmlPullParser xmlPullParser,
			CoverBean coverBean) throws XmlPullParserException, IOException {
		CoverBean.RotateBean rotateBean = coverBean.createRotateBean();
		// 自身属性
		parceRotateSelf(xmlPullParser, rotateBean);
		// 子属性
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				// 解析完毕
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(AttributeSet.STARTLOCATION)) {
					CoverBean.StartLocation location = coverBean
							.createStartLocation();
					parceStartLocation(xmlPullParser, location);
					rotateBean.mStartLocation = location;
				} else if (tagName.equals(AttributeSet.SHADOW)) {
					CoverBean.Shadow shadow = coverBean.createShadow();
					parceShadow(xmlPullParser, shadow);
					rotateBean.mShadow = shadow;
				} else if (tagName.equals(AttributeSet.LIMITAREA)) {
					CoverBean.LimitArea area = coverBean.createLimitArea();
					parceLimitArea(xmlPullParser, area);
					rotateBean.mLimitArea = area;
				} else if (tagName.equals(AttributeSet.RELATIVELOCATION)) {
					CoverBean.RelativeLocation relativeLocation = coverBean
							.createRelativeLocation();
					parceRelativeLocation(xmlPullParser, relativeLocation);
					rotateBean.mRelativeLocation = relativeLocation;
				} else if (tagName.equals(AttributeSet.ROTATEANIM)) {
					CoverBean.RotateAnim anim = coverBean.createRotateAnim();
					parceRotateAnim(xmlPullParser, anim);
					rotateBean.mRotateAnim = anim;
				} else if (tagName.equals(AttributeSet.CLICK_ACTION)) {
					parceClickAction(xmlPullParser, coverBean, rotateBean);
				} else if (tagName.equals(AttributeSet.DRAG_ACTION)) {
					parceDragAction(xmlPullParser, coverBean, rotateBean);
				}
			}
			eventType = xmlPullParser.next();
		}
		// 添加进集合
		coverBean.mRotateMap.add(rotateBean);
	}

	private void parceDragAction(XmlPullParser xmlPullParser,
			CoverBean coverBean, CoverBean.RotateBean rotateBean)
			throws XmlPullParserException, IOException {
		String attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.IMAGENAME);
		CoverBean.DragAction dragAction = coverBean.createDragAction();
		dragAction.mNormalImage = CoverterUtils.stringToString(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.ENDTYPE);
		dragAction.mEndType = CoverterUtils.stringToInt(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.ACTIONTYPE);
		dragAction.mActionType = CoverterUtils.stringToInt(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.DELAYTIME);
		dragAction.mDelayTime = CoverterUtils.stringToLong(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.ANIMATIONTIME);
		dragAction.mAnimationTime = CoverterUtils.stringToInt(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.NEEDLOOP);
		dragAction.mNeedLoop = CoverterUtils.stringToBoolean(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.ISBITMAPSYMMETRIC);
		dragAction.mIsBitmapSymmetric = CoverterUtils
				.stringToBoolean(attributeValue);
		parceActionItem(xmlPullParser, dragAction);
		rotateBean.mDragAction = dragAction;
	}

	private void parceSpirtDragAction(XmlPullParser xmlPullParser,
			CoverBean coverBean, CoverBean.SpriteBean bean)
			throws XmlPullParserException, IOException {
		String attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.IMAGENAME);
		CoverBean.DragAction dragAction = coverBean.createDragAction();
		dragAction.mNormalImage = CoverterUtils.stringToString(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.ENDTYPE);
		dragAction.mEndType = CoverterUtils.stringToInt(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.ACTIONTYPE);
		dragAction.mActionType = CoverterUtils.stringToInt(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.DELAYTIME);
		dragAction.mDelayTime = CoverterUtils.stringToLong(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.ANIMATIONTIME);
		dragAction.mAnimationTime = CoverterUtils.stringToInt(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.NEEDLOOP);
		dragAction.mNeedLoop = CoverterUtils.stringToBoolean(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.ISBITMAPSYMMETRIC);
		dragAction.mIsBitmapSymmetric = CoverterUtils
				.stringToBoolean(attributeValue);
		parceActionItem(xmlPullParser, dragAction);
		bean.mDragAction = dragAction;
	}

	private void parceSpirtShakeAction(XmlPullParser xmlPullParser,
			CoverBean coverBean, CoverBean.SpriteBean bean)
			throws XmlPullParserException, IOException {
		String attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.IMAGENAME);
		CoverBean.ShakeAction shakeAction = coverBean.createShakeAction();
		shakeAction.mNormalImage = CoverterUtils.stringToString(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.ENDTYPE);
		shakeAction.mEndType = CoverterUtils.stringToInt(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.ACTIONTYPE);
		shakeAction.mActionType = CoverterUtils.stringToInt(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.DELAYTIME);
		shakeAction.mDelayTime = CoverterUtils.stringToLong(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.ANIMATIONTIME);
		shakeAction.mAnimationTime = CoverterUtils.stringToInt(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.NEEDLOOP);
		shakeAction.mNeedLoop = CoverterUtils.stringToBoolean(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.ISBITMAPSYMMETRIC);
		shakeAction.mIsBitmapSymmetric = CoverterUtils
				.stringToBoolean(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.SHAKESPEED);
		shakeAction.mShakeSpeed = CoverterUtils.stringToInt(attributeValue);

		parceActionItem(xmlPullParser, shakeAction);
		bean.mShakeAction = shakeAction;
	}

	private void parceClickAction(XmlPullParser xmlPullParser,
			CoverBean coverBean, CoverBean.RotateBean rotateBean)
			throws XmlPullParserException, IOException {
		// 子属性
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				// 解析完毕
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(AttributeSet.CLICKACTIONITEM)) {
					String attributeValue = xmlPullParser.getAttributeValue(
							null, AttributeSet.IMAGENAME);
					CoverBean.ClickAction clickAction = coverBean
							.createClickAction();
					clickAction.mNormalImage = CoverterUtils
							.stringToString(attributeValue);
					attributeValue = xmlPullParser.getAttributeValue(null,
							AttributeSet.ENDTYPE);
					clickAction.mEndType = CoverterUtils
							.stringToInt(attributeValue);
					attributeValue = xmlPullParser.getAttributeValue(null,
							AttributeSet.ACTIONTYPE);
					clickAction.mActionType = CoverterUtils
							.stringToInt(attributeValue);
					attributeValue = xmlPullParser.getAttributeValue(null,
							AttributeSet.DELAYTIME);
					clickAction.mDelayTime = CoverterUtils
							.stringToLong(attributeValue);
					attributeValue = xmlPullParser.getAttributeValue(null,
							AttributeSet.ANIMATIONTIME);
					clickAction.mAnimationTime = CoverterUtils
							.stringToInt(attributeValue);
					attributeValue = xmlPullParser.getAttributeValue(null,
							AttributeSet.NEEDLOOP);
					clickAction.mNeedLoop = CoverterUtils
							.stringToBoolean(attributeValue);
					attributeValue = xmlPullParser.getAttributeValue(null,
							AttributeSet.ISBITMAPSYMMETRIC);
					clickAction.mIsBitmapSymmetric = CoverterUtils
							.stringToBoolean(attributeValue);
					parceActionItem(xmlPullParser, clickAction);
					rotateBean.mClickActionList.add(clickAction);
				}
			}
			eventType = xmlPullParser.next();
		}
	}

	private void parceActionItem(XmlPullParser xmlPullParser,
			CoverBean.BaseAction baseAction) throws XmlPullParserException,
			IOException {
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(AttributeSet.ITEM)) {
					String attributeValue = xmlPullParser.getAttributeValue(
							null, AttributeSet.IMAGENAME);
					baseAction.mActionImageNames.add(CoverterUtils
							.stringToString(attributeValue));
				}
			}
			eventType = xmlPullParser.next();
		}
	}

	private void parceTranslateItem(XmlPullParser xmlPullParser,
			CoverBean coverBean) throws XmlPullParserException, IOException {
		CoverBean.TranslateBean translateBean = coverBean.createTranslateBean();
		// 自身属性
		parceTranslateSelf(xmlPullParser, translateBean);
		// 子属性
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				// 解析完毕
				// if (tagName.equals(TagSet.SHOWITEMLAYER))
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(AttributeSet.STARTLOCATION)) {
					CoverBean.StartLocation location = coverBean
							.createStartLocation();
					parceStartLocation(xmlPullParser, location);
					translateBean.mStartLocation = location;
				} else if (tagName.equals(AttributeSet.SHADOW)) {
					CoverBean.Shadow shadow = coverBean.createShadow();
					parceShadow(xmlPullParser, shadow);
					translateBean.mShadow = shadow;
				} else if (tagName.equals(AttributeSet.LIMITAREA)) {
					CoverBean.LimitArea area = coverBean.createLimitArea();
					parceLimitArea(xmlPullParser, area);
					translateBean.mLimitArea = area;
				} else if (tagName.equals(AttributeSet.TRANSLATEANIM)) {
					CoverBean.TranslateAnim anim = coverBean
							.createTranslateAnim();
					parceTranslateAnim(xmlPullParser, anim);
					translateBean.mTranslateAnim = anim;
				}
			}
			eventType = xmlPullParser.next();
		}
		// 添加进集合
		coverBean.mTranslateMap.add(translateBean);
	}

	private void parceBalloonItem(XmlPullParser xmlPullParser,
			CoverBean coverBean) throws XmlPullParserException, IOException {
		CoverBean.BalloonBean ballBean = coverBean.createBallleanBean();
		parceBallonSelf(xmlPullParser, ballBean);
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(AttributeSet.LIMITAREA)) {

					CoverBean.LimitArea area = coverBean.createLimitArea();
					parceLimitArea(xmlPullParser, area);
					ballBean.mLimitArea = area;
				} else if (tagName.equals(AttributeSet.STARTLOCATION)) {
					CoverBean.StartLocation location = coverBean
							.createStartLocation();
					parceStartLocation(xmlPullParser, location);
					ballBean.mStartLocation = location;
				} else if (tagName.equals(AttributeSet.BALLOONIMG)) {
					String attributeValue = xmlPullParser.getAttributeValue(
							null, AttributeSet.IMAGENAME);
					ballBean.mBallonImgName = CoverterUtils
							.stringToString(attributeValue);
				} else if (tagName.equals(AttributeSet.BITMAPFRAME)) {
					List<String> imageNames = new ArrayList<String>();
					parceBitmapFrame(xmlPullParser, imageNames);
					ballBean.mImageNames.addAll(imageNames);
					imageNames.clear();
				} else if (tagName.equals(AttributeSet.AROUNDBALLS)) {
					String attributeValue = xmlPullParser.getAttributeValue(
							null, AttributeSet.IMAGENAME);
					ballBean.mAroundImgName = CoverterUtils
							.stringToString(attributeValue);
				} else if (tagName.equals(AttributeSet.NUMBERITEM)) {
					List<String> imageNames = new ArrayList<String>();
					parceItems(xmlPullParser, imageNames);
					ballBean.mNumImgNames.addAll(imageNames);
					imageNames.clear();
				} else if (tagName.equals(AttributeSet.INDISTINCT)) {
					List<String> imageNames = new ArrayList<String>();
					parceItems(xmlPullParser, imageNames);
					ballBean.mIndistinctNames.addAll(imageNames);
					imageNames.clear();
				} else if (tagName.equals(AttributeSet.BLAST)) {
					List<List<String>> imageNames = new ArrayList<List<String>>();
					parceBlast(xmlPullParser, imageNames);
					ballBean.mBlastImgNames.addAll(imageNames);
					imageNames.clear();
				}
			}
			eventType = xmlPullParser.next();
		}
		coverBean.mBalloonMap.add(ballBean);

	}

	private void parceBlast(XmlPullParser xmlPullParser,
			List<List<String>> imageNames) throws XmlPullParserException,
			IOException {
		// 子属性
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				// 解析完毕
				// if (tagName.equals(TagSet.SHOWITEMLAYER))
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(AttributeSet.BLUE)) {
					List<String> names = new ArrayList<String>();
					parceItems(xmlPullParser, names);
					imageNames.add(names);
				} else if (tagName.equals(AttributeSet.ORANGE)) {
					List<String> names = new ArrayList<String>();
					parceItems(xmlPullParser, names);
					imageNames.add(names);
				} else if (tagName.equals(AttributeSet.YELLOW)) {
					List<String> names = new ArrayList<String>();
					parceItems(xmlPullParser, names);
					imageNames.add(names);
				}
			}
			eventType = xmlPullParser.next();
		}
	}

	private void parceClockItem(XmlPullParser xmlPullParser, CoverBean coverBean)
			throws XmlPullParserException, IOException {
		CoverBean.ClockBean clockBean = coverBean.createClockBean();
		// 自身属性
		parceClockSelf(xmlPullParser, clockBean);
		// 子属性
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {

				// 解析完毕
				// if (tagName.equals(TagSet.SHOWITEMLAYER))
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(AttributeSet.SHADOW)) {
					CoverBean.Shadow shadow = coverBean.createShadow();
					parceShadow(xmlPullParser, shadow);
					clockBean.mShadow = shadow;
				} else if (tagName.equals(AttributeSet.LIMITAREA)) {
					CoverBean.LimitArea area = coverBean.createLimitArea();
					parceLimitArea(xmlPullParser, area);
					clockBean.mLimitArea = area;
				} else if (tagName.equals(AttributeSet.AXIS)) {
					CoverBean.Axis axis = coverBean.createAxis();
					parceAxis(xmlPullParser, axis);
					clockBean.mAxis = axis;
				} else if (tagName.equals(AttributeSet.BACKGROUND)) {
					String attributeValue = xmlPullParser.getAttributeValue(
							null, AttributeSet.IMAGENAME);
					clockBean.mBgName = CoverterUtils
							.stringToString(attributeValue);
				} else if (tagName.equals(AttributeSet.DAIL)) {
					String attributeValue = xmlPullParser.getAttributeValue(
							null, AttributeSet.IMAGENAME);
					clockBean.mDailName = CoverterUtils
							.stringToString(attributeValue);
				} else if (tagName.equals(AttributeSet.CLOCKHAND)) {
					parceClockHand(xmlPullParser, coverBean, clockBean);

				} else if (tagName.equals(AttributeSet.STARTLOCATION)) {
					CoverBean.StartLocation location = coverBean
							.createStartLocation();
					parceStartLocation(xmlPullParser, location);
					clockBean.mStartLocation = location;
				} else if (tagName.equals(AttributeSet.CLOCKSCREW)) {
					String attributeValue = xmlPullParser.getAttributeValue(
							null, AttributeSet.IMAGENAME);
					clockBean.mScrewName = CoverterUtils
							.stringToString(attributeValue);
				}
			}
			eventType = xmlPullParser.next();
		}
		// 添加进集合
		coverBean.mClockMap.add(clockBean);
	}

	private void parceSpriteItem(XmlPullParser xmlPullParser,
			CoverBean coverBean) throws XmlPullParserException, IOException {
		CoverBean.SpriteBean spriteBean = coverBean.createSpriteBean();
		// 自身属性
		parceSpriteSelf(xmlPullParser, spriteBean);
		// 子属性
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				// 解析完毕
				// if (tagName.equals(TagSet.SHOWITEMLAYER))
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(AttributeSet.ROTATEXY)) {
					CoverBean.RotateXY rotateXY = coverBean.createRotateXY();
					parceRotateXY(xmlPullParser, rotateXY);
					spriteBean.mRotateXY = rotateXY;
				} else if (tagName.equals(AttributeSet.LIMITAREA)) {
					CoverBean.LimitArea area = coverBean.createLimitArea();
					parceLimitArea(xmlPullParser, area);
					spriteBean.mLimitArea = area;
				} else if (tagName.equals(AttributeSet.STARTLOCATION)) {
					CoverBean.StartLocation location = coverBean
							.createStartLocation();
					parceStartLocation(xmlPullParser, location);
					spriteBean.mStartLocation = location;

				} else if (tagName.equals(AttributeSet.RELATIVELOCATION)) {
					CoverBean.RelativeLocation relativeLocation = coverBean
							.createRelativeLocation();
					parceRelativeLocation(xmlPullParser, relativeLocation);
					spriteBean.mRelativeLocation = relativeLocation;
				} else if (tagName.equals(AttributeSet.ACTION)) {
					parceSpriteAction(xmlPullParser, coverBean, spriteBean);
				}
			}
			eventType = xmlPullParser.next();
		}
		// 添加进集合
		coverBean.mSpriteMap.add(spriteBean);
	}

	private void parceNormal(XmlPullParser xmlPullParser, CoverBean coverBean)
			throws XmlPullParserException, IOException {
		// 自身属性
		// Normal暂时没有自身属性
		// 子属性
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				// 解析完毕
				// if (tagName.equals(TagSet.ICONSTYLE))
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(AttributeSet.ITEM)) {
					parceNormalItem(xmlPullParser, coverBean);
				}
			}
			eventType = xmlPullParser.next();
		}
	}

	private void parceSlide(XmlPullParser xmlPullParser, CoverBean coverBean)
			throws XmlPullParserException, IOException {
		// 子属性
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				// 解析完毕
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(AttributeSet.ITEM)) {
					parceSlideItem(xmlPullParser, coverBean);
				}
			}
			eventType = xmlPullParser.next();
		}
	}

	private void parceSlideItem(XmlPullParser xmlPullParser, CoverBean coverBean)
			throws XmlPullParserException, IOException {
		CoverBean.SlideBean slideBean = coverBean.createSlideBean();
		// 自身属性
		parceSlideSelf(xmlPullParser, slideBean);
		// 子属性
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(AttributeSet.LIMITAREA)) {
					CoverBean.LimitArea area = coverBean.createLimitArea();
					parceLimitArea(xmlPullParser, area);
					slideBean.mLimitArea = area;
				} else if (tagName.equals(AttributeSet.SHADOW)) {
					CoverBean.Shadow shadow = coverBean.createShadow();
					parceShadow(xmlPullParser, shadow);
					slideBean.mShadow = shadow;
				} else if (tagName.equals(AttributeSet.STARTLOCATION)) {
					CoverBean.StartLocation location = coverBean
							.createStartLocation();
					parceStartLocation(xmlPullParser, location);
					slideBean.mStartLocation = location;
				} else if (tagName.equals(AttributeSet.RELATIVELOCATION)) {
					CoverBean.RelativeLocation relativeLocation = coverBean
							.createRelativeLocation();
					parceRelativeLocation(xmlPullParser, relativeLocation);
					slideBean.mRelativeLocation = relativeLocation;
				} else if (tagName.equals(AttributeSet.BITMAPFRAME)) {
					List<String> imageNames = new ArrayList<String>();
					parceBitmapFrame(xmlPullParser, imageNames);
					slideBean.mImageNames.addAll(imageNames);
					imageNames.clear();
				} else if (tagName.equals(AttributeSet.ACTION)) {
					parceSpriteAction(xmlPullParser, coverBean, slideBean);
				}
			}
			eventType = xmlPullParser.next();
		}
		// 添加进集合
		coverBean.mSlideMap.add(slideBean);
	}

	private void parceSlideSelf(XmlPullParser xmlPullParser,
			CoverBean.SlideBean item) {
		String attributeValue = null;
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.ALLOWDRAG);
		item.mAllowDrag = CoverterUtils.stringToBoolean(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.IMAGENAME);
		item.mImgName = CoverterUtils.stringToString(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.INTERVAL);
		item.mInterval = CoverterUtils.stringToLong(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.ANIMATIONTYPE);
		item.mAnimationType = CoverterUtils.stringToInt(attributeValue);
		attributeValue = xmlPullParser.getAttributeValue(null,
				AttributeSet.SHAKESPEED);
		item.mShakeSpeed = CoverterUtils.stringToInt(attributeValue);
	}

	private void parceSleigh(XmlPullParser xmlPullParser, CoverBean coverBean)
			throws XmlPullParserException, IOException {
		CoverBean.SleighBean sleighBean = coverBean.createSleighBean();
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				// 解析完毕
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(AttributeSet.DEER)) {
					parceDeer(xmlPullParser, sleighBean);
				} else if (tagName.equals(AttributeSet.SANTA)) {
					parceSanta(xmlPullParser, sleighBean);
				} else if (tagName.equals(AttributeSet.WHIP)) {
					parceWhip(xmlPullParser, sleighBean);
				} else if (tagName.equals(AttributeSet.FLAG)) {
					parceFlag(xmlPullParser, sleighBean);
				} else if (tagName.equals(AttributeSet.MOVEPOINT)) {
					parceMovePoint(xmlPullParser, sleighBean);
				} else if (tagName.equals(AttributeSet.GIFT)) {
					parceGift(xmlPullParser, sleighBean);
				} else if (tagName.equals(AttributeSet.RABBIT)) {
					parceRabbit(xmlPullParser, sleighBean);
				} else if (tagName.equals(AttributeSet.FALLSANTA)) {
					parceFallSanta(xmlPullParser, sleighBean);
				} else if (tagName.equals(AttributeSet.UPSANTA)) {
					parceUpSanta(xmlPullParser, sleighBean);
				} else if (tagName.equals(AttributeSet.RUNSANTA)) {
					parceRunSanta(xmlPullParser, sleighBean);
				} else if (tagName.equals(AttributeSet.EMPTYSLEIGH)) {
					parceEmptySleigh(xmlPullParser, sleighBean);
				} else if (tagName.equals(AttributeSet.PACKAGE)) {
					parcePakage(xmlPullParser, sleighBean);
				}
			}
			eventType = xmlPullParser.next();
		}
		coverBean.mSleighBean = sleighBean;
	}

	private void parceDeer(XmlPullParser xmlPullParser, SleighBean sleighBean)
			throws XmlPullParserException, IOException {
		DeerBean deerBean = sleighBean.mDeerBean;
		// 自身属性
		// parceRotateSelf(xmlPullParser, deerBean);
		// 子属性
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				// 解析完毕
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(AttributeSet.BITMAPFRAME)) {
					List<String> imageNames = new ArrayList<String>();
					parceBitmapFrame(xmlPullParser, imageNames);
					deerBean.mImageNames.addAll(imageNames);
					imageNames.clear();
				}
			}
			eventType = xmlPullParser.next();
		}
	}

	private void parceSanta(XmlPullParser xmlPullParser, SleighBean sleighBean)
			throws XmlPullParserException, IOException {
		SantaBean santaBean = sleighBean.mSantaBean;
		// 自身属性
		// parceRotateSelf(xmlPullParser, deerBean);
		// 子属性
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				// 解析完毕
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(AttributeSet.BITMAPFRAME)) {
					List<String> imageNames = new ArrayList<String>();
					parceBitmapFrame(xmlPullParser, imageNames);
					santaBean.mImageNames.addAll(imageNames);
					imageNames.clear();
				}
			}
			eventType = xmlPullParser.next();
		}
	}

	private void parceWhip(XmlPullParser xmlPullParser, SleighBean sleighBean)
			throws XmlPullParserException, IOException {
		WhipBean whipBean = sleighBean.mWhipBean;
		// 自身属性
		// parceRotateSelf(xmlPullParser, deerBean);
		// 子属性
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				// 解析完毕
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(AttributeSet.BITMAPFRAME)) {
					List<String> imageNames = new ArrayList<String>();
					parceBitmapFrame(xmlPullParser, imageNames);
					whipBean.mImageNames.addAll(imageNames);
					imageNames.clear();
				}
			}
			eventType = xmlPullParser.next();
		}
	}

	private void parceFlag(XmlPullParser xmlPullParser, SleighBean sleighBean)
			throws XmlPullParserException, IOException {
		FlagBean flagBean = sleighBean.mFlagBean;
		// 自身属性
		// parceRotateSelf(xmlPullParser, deerBean);
		// 子属性
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				// 解析完毕
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(AttributeSet.RIGHTTOLEFTACTION)) {
					List<String> imageNames = new ArrayList<String>();
					parceRightToLeftAction(xmlPullParser, imageNames);
					flagBean.mRTLImageNames.addAll(imageNames);
					imageNames.clear();
				} else if (tagName.equals(AttributeSet.LEFTTORIGHTACTION)) {
					List<String> imageNames = new ArrayList<String>();
					parceLeftToRightAction(xmlPullParser, imageNames);
					flagBean.mLTRImageNames.addAll(imageNames);
					imageNames.clear();
				}
			}
			eventType = xmlPullParser.next();
		}
	}

	private void parceMovePoint(XmlPullParser xmlPullParser,
			SleighBean sleighBean) throws XmlPullParserException, IOException {
		MovePointBean movePointBean = sleighBean.mMovePointBean;
		// 自身属性
		// parceRotateSelf(xmlPullParser, deerBean);
		// 子属性
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				// 解析完毕
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(AttributeSet.BITMAPFRAME)) {
					List<String> imageNames = new ArrayList<String>();
					parceBitmapFrame(xmlPullParser, imageNames);
					movePointBean.mImageNames.addAll(imageNames);
					imageNames.clear();
				}
			}
			eventType = xmlPullParser.next();
		}
	}

	private void parceGift(XmlPullParser xmlPullParser, SleighBean sleighBean)
			throws XmlPullParserException, IOException {
		GiftBean giftBean = sleighBean.mGiftBean;
		// 自身属性
		// parceRotateSelf(xmlPullParser, deerBean);
		// 子属性
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				// 解析完毕
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(AttributeSet.BITMAPFRAME)) {
					List<String> imageNames = new ArrayList<String>();
					parceBitmapFrame(xmlPullParser, imageNames);
					giftBean.mImageNames.addAll(imageNames);
					imageNames.clear();
				}
			}
			eventType = xmlPullParser.next();
		}
	}

	private void parceFallSanta(XmlPullParser xmlPullParser,
			SleighBean sleighBean) throws XmlPullParserException, IOException {
		FallSantaBean fallSantaBean = sleighBean.mFallSantaBean;
		// 自身属性
		// parceRotateSelf(xmlPullParser, deerBean);
		// 子属性
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				// 解析完毕
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(AttributeSet.BITMAPFRAME)) {
					List<String> imageNames = new ArrayList<String>();
					parceBitmapFrame(xmlPullParser, imageNames);
					fallSantaBean.mImageNames.addAll(imageNames);
					imageNames.clear();
				}
			}
			eventType = xmlPullParser.next();
		}
	}

	private void parceUpSanta(XmlPullParser xmlPullParser, SleighBean sleighBean)
			throws XmlPullParserException, IOException {
		UpSantaBean upSantaBean = sleighBean.mUpSantaBean;
		// 自身属性
		// parceRotateSelf(xmlPullParser, deerBean);
		// 子属性
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				// 解析完毕
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(AttributeSet.BITMAPFRAME)) {
					List<String> imageNames = new ArrayList<String>();
					parceBitmapFrame(xmlPullParser, imageNames);
					upSantaBean.mImageNames.addAll(imageNames);
					imageNames.clear();
				}
			}
			eventType = xmlPullParser.next();
		}
	}

	private void parceRunSanta(XmlPullParser xmlPullParser,
			SleighBean sleighBean) throws XmlPullParserException, IOException {
		RunSantaBean runSantaBean = sleighBean.mRunSantaBean;
		// 自身属性
		// parceRotateSelf(xmlPullParser, deerBean);
		// 子属性
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				// 解析完毕
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(AttributeSet.BITMAPFRAME)) {
					List<String> imageNames = new ArrayList<String>();
					parceBitmapFrame(xmlPullParser, imageNames);
					runSantaBean.mImageNames.addAll(imageNames);
					imageNames.clear();
				}
			}
			eventType = xmlPullParser.next();
		}
	}

	private void parceEmptySleigh(XmlPullParser xmlPullParser,
			SleighBean sleighBean) throws XmlPullParserException, IOException {
		EmptySleighBean emptySleighBean = sleighBean.mEmptySleighBean;
		// 自身属性
		// parceRotateSelf(xmlPullParser, deerBean);
		// 子属性
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				// 解析完毕
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(AttributeSet.BITMAPFRAME)) {
					List<String> imageNames = new ArrayList<String>();
					parceBitmapFrame(xmlPullParser, imageNames);
					emptySleighBean.mImageNames.addAll(imageNames);
					imageNames.clear();
				}
			}
			eventType = xmlPullParser.next();
		}
	}

	private void parcePakage(XmlPullParser xmlPullParser, SleighBean sleighBean)
			throws XmlPullParserException, IOException {
		PackageBean packageBean = sleighBean.mPackageBean;
		// 自身属性
		// parceRotateSelf(xmlPullParser, deerBean);
		// 子属性
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				// 解析完毕
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(AttributeSet.BITMAPFRAME)) {
					List<String> imageNames = new ArrayList<String>();
					parceBitmapFrame(xmlPullParser, imageNames);
					packageBean.mImageNames.addAll(imageNames);
					imageNames.clear();
				}
			}
			eventType = xmlPullParser.next();
		}
	}

	private void parceRabbit(XmlPullParser xmlPullParser, SleighBean sleighBean)
			throws XmlPullParserException, IOException {
		RabbitBean rabbitBean = sleighBean.mRabbitBean;
		// 自身属性
		// parceRotateSelf(xmlPullParser, deerBean);
		// 子属性
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				// 解析完毕
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(AttributeSet.JUMPACTION)) {
					List<String> imageNames = new ArrayList<String>();
					parceJumpAction(xmlPullParser, imageNames);
					rabbitBean.mJumpImageNames.addAll(imageNames);
					imageNames.clear();
				} else if (tagName.equals(AttributeSet.WAVEACTION)) {
					List<String> imageNames = new ArrayList<String>();
					parceWaveAction(xmlPullParser, imageNames);
					rabbitBean.mWaveImageNames.addAll(imageNames);
					imageNames.clear();
				} else if (tagName.equals(AttributeSet.DISMISSACTION)) {
					List<String> imageNames = new ArrayList<String>();
					parceDismissAction(xmlPullParser, imageNames);
					rabbitBean.mDismissImageNames.addAll(imageNames);
					imageNames.clear();
				}
			}
			eventType = xmlPullParser.next();
		}
	}

	private void parceLeftToRightAction(XmlPullParser xmlPullParser,
			List<String> imageNames) throws XmlPullParserException, IOException {
		// 子属性
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				// 解析完毕
				// if (tagName.equals(TagSet.SHOWITEMLAYER))
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(AttributeSet.ITEM)) {
					String attributeValue = xmlPullParser.getAttributeValue(
							null, AttributeSet.IMAGENAME);
					imageNames
							.add(CoverterUtils.stringToString(attributeValue));
				}
			}
			eventType = xmlPullParser.next();
		}
	}

	private void parceRightToLeftAction(XmlPullParser xmlPullParser,
			List<String> imageNames) throws XmlPullParserException, IOException {
		// 子属性
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				// 解析完毕
				// if (tagName.equals(TagSet.SHOWITEMLAYER))
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(AttributeSet.ITEM)) {
					String attributeValue = xmlPullParser.getAttributeValue(
							null, AttributeSet.IMAGENAME);
					imageNames
							.add(CoverterUtils.stringToString(attributeValue));
				}
			}
			eventType = xmlPullParser.next();
		}
	}

	private void parceJumpAction(XmlPullParser xmlPullParser,
			List<String> imageNames) throws XmlPullParserException, IOException {
		// 子属性
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				// 解析完毕
				// if (tagName.equals(TagSet.SHOWITEMLAYER))
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(AttributeSet.ITEM)) {
					String attributeValue = xmlPullParser.getAttributeValue(
							null, AttributeSet.IMAGENAME);
					imageNames
							.add(CoverterUtils.stringToString(attributeValue));
				}
			}
			eventType = xmlPullParser.next();
		}
	}

	private void parceWaveAction(XmlPullParser xmlPullParser,
			List<String> imageNames) throws XmlPullParserException, IOException {
		// 子属性
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				// 解析完毕
				// if (tagName.equals(TagSet.SHOWITEMLAYER))
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(AttributeSet.ITEM)) {
					String attributeValue = xmlPullParser.getAttributeValue(
							null, AttributeSet.IMAGENAME);
					imageNames
							.add(CoverterUtils.stringToString(attributeValue));
				}
			}
			eventType = xmlPullParser.next();
		}
	}

	private void parceDismissAction(XmlPullParser xmlPullParser,
			List<String> imageNames) throws XmlPullParserException, IOException {
		// 子属性
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				// 解析完毕
				// if (tagName.equals(TagSet.SHOWITEMLAYER))
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(AttributeSet.ITEM)) {
					String attributeValue = xmlPullParser.getAttributeValue(
							null, AttributeSet.IMAGENAME);
					imageNames
							.add(CoverterUtils.stringToString(attributeValue));
				}
			}
			eventType = xmlPullParser.next();
		}
	}

	private void parceRotate(XmlPullParser xmlPullParser, CoverBean coverBean)
			throws XmlPullParserException, IOException {
		// 子属性
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				// 解析完毕
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(AttributeSet.ITEM)) {
					parceRotateItem(xmlPullParser, coverBean);
				}
			}
			eventType = xmlPullParser.next();
		}
	}

	private void parceTranslate(XmlPullParser xmlPullParser, CoverBean coverBean)
			throws XmlPullParserException, IOException {
		// 自身属性
		// Normal暂时没有自身属性
		// 子属性
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				// 解析完毕
				// if (tagName.equals(TagSet.ICONSTYLE))
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(AttributeSet.ITEM)) {
					parceTranslateItem(xmlPullParser, coverBean);
				}
			}
			eventType = xmlPullParser.next();
		}
	}

	private void parceTouch(XmlPullParser xmlPullParser, CoverBean coverBean)
			throws XmlPullParserException, IOException {
		CoverBean.TouchBean touchBean = coverBean.createTouchBean();
		// 自身属性
		parceTouchSelf(xmlPullParser, touchBean);
		// 子属性
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				// 解析完毕
				// if (tagName.equals(TagSet.ICONSTYLE))
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(AttributeSet.BITMAPFRAME)) {
					List<String> imageNames = new ArrayList<String>();
					parceBitmapFrame(xmlPullParser, imageNames);
					touchBean.mImageNames.addAll(imageNames);
					imageNames.clear();
				} else if (tagName.equals(AttributeSet.LIFE)) {
					CoverBean.Life life = coverBean.createLife();
					parceLife(xmlPullParser, life);
					touchBean.mLife = life;
				}
			}
			eventType = xmlPullParser.next();
		}
		coverBean.mTouchBean = touchBean;

	}

	private void parceRandom(XmlPullParser xmlPullParser, CoverBean coverBean)
			throws XmlPullParserException, IOException {
		CoverBean.RandomBean randomBean = coverBean.createRandomBean();
		// 自身属性
		parceRandomSelf(xmlPullParser, randomBean);
		// 子属性
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				// 解析完毕
				// if (tagName.equals(TagSet.ICONSTYLE))
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(AttributeSet.BITMAPFRAME)) {
					List<String> imageNames = new ArrayList<String>();
					parceBitmapFrame(xmlPullParser, imageNames);
					randomBean.mImageNames.addAll(imageNames);
					imageNames.clear();
				}
			}
			eventType = xmlPullParser.next();
		}
		coverBean.mRandomMap.add(randomBean);
	}

	private void parceClock(XmlPullParser xmlPullParser, CoverBean coverBean)
			throws XmlPullParserException, IOException {
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				// 解析完毕
				// if (tagName.equals(TagSet.ICONSTYLE))
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(AttributeSet.ITEM)) {

					parceClockItem(xmlPullParser, coverBean);
				}
			}
			eventType = xmlPullParser.next();
		}
	}

	private void parceBalloon(XmlPullParser xmlPullParser, CoverBean coverBean)
			throws XmlPullParserException, IOException {
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				// 解析完毕
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(AttributeSet.ITEM)) {
					parceBalloonItem(xmlPullParser, coverBean);
				}
			}
			eventType = xmlPullParser.next();
		}
	}

	private void parceLongTouch(XmlPullParser xmlPullParser, CoverBean coverBean)
			throws XmlPullParserException, IOException {
		CoverBean.LongTounchBean longTounchBean = coverBean
				.createLongTounchBean();
		// 自身属性
		// Normal暂时没有自身属性
		parceLongTouchSelf(xmlPullParser, longTounchBean);
		// 子属性
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				// 解析完毕
				// if (tagName.equals(TagSet.ICONSTYLE))
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(AttributeSet.STARTLOCATION)) {
					CoverBean.StartLocation location = coverBean
							.createStartLocation();
					parceStartLocation(xmlPullParser, location);
					longTounchBean.mStartLocation = location;
				} else if (tagName.equals(AttributeSet.SHADOW)) {
					CoverBean.Shadow shadow = coverBean.createShadow();
					parceShadow(xmlPullParser, shadow);
					longTounchBean.mShadow = shadow;
				} else if (tagName.equals(AttributeSet.LIMITAREA)) {
					CoverBean.LimitArea area = coverBean.createLimitArea();
					parceLimitArea(xmlPullParser, area);
					longTounchBean.mlLimitArea = area;
				} else if (tagName.equals(AttributeSet.LIFE)) {
					CoverBean.Life life = coverBean.createLife();
					parceLife(xmlPullParser, life);
					longTounchBean.mLife = life;
				} else if (tagName.equals(AttributeSet.BITMAPFRAME)) {
					List<CoverBean.BitmapItem> imageTop = new ArrayList<CoverBean.BitmapItem>();
					List<CoverBean.BitmapItem> imageBottom = new ArrayList<CoverBean.BitmapItem>();
					parceLongTouchBitmapFrame(xmlPullParser, imageTop,
							imageBottom, coverBean);
					longTounchBean.mBitmapFrameTop.addAll(imageTop);
					longTounchBean.mBitmapFrameBottom.addAll(imageBottom);
					imageTop.clear();
					imageBottom.clear();
				}
			}
			eventType = xmlPullParser.next();
		}
		coverBean.mLongTounchMap.add(longTounchBean);
	}

	private void parceMove(XmlPullParser xmlPullParser, CoverBean coverBean)
			throws XmlPullParserException, IOException {
		CoverBean.MoveBean moveBean = coverBean.createMoveBean();
		// 自身属性
		parceMoveSelf(xmlPullParser, moveBean);
		// 子属性
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				// 解析完毕
				// if (tagName.equals(TagSet.ICONSTYLE))
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(AttributeSet.BITMAPFRAME)) {
					List<String> imageNames = new ArrayList<String>();
					parceBitmapFrame(xmlPullParser, imageNames);
					moveBean.mImageNames.addAll(imageNames);
					imageNames.clear();
				} else if (tagName.equals(AttributeSet.LIFE)) {
					CoverBean.Life life = coverBean.createLife();
					parceLife(xmlPullParser, life);
					moveBean.mLife = life;
				}
			}
			eventType = xmlPullParser.next();
		}
		coverBean.mMoveBeanMap.add(moveBean);
	}

	private void parceSprite(XmlPullParser xmlPullParser, CoverBean coverBean)
			throws XmlPullParserException, IOException {
		// 自身属性
		// Normal暂时没有自身属性
		// 子属性
		String parceTagName = xmlPullParser.getName();
		int eventType = xmlPullParser.next();
		while (XmlPullParser.END_DOCUMENT != eventType) {
			String tagName = xmlPullParser.getName();
			if (XmlPullParser.END_TAG == eventType) {
				// 解析完毕
				// if (tagName.equals(TagSet.ICONSTYLE))
				if (tagName.equals(parceTagName)) {
					break;
				}
			} else if (XmlPullParser.START_TAG == eventType) {
				if (tagName.equals(AttributeSet.ITEM)) {
					parceSpriteItem(xmlPullParser, coverBean);
				}
			}
			eventType = xmlPullParser.next();
		}
	}

	// private CoverBean.Fill stringToFill(String value) {
	// if (null == value) {
	// return CoverBean.Fill.None;
	// }
	//
	// if (value.equals("1")) {
	// return CoverBean.Fill.Center;
	// }
	// if (value.equals("2")) {
	// return CoverBean.Fill.Tensile;
	// }
	// if (value.equals("3")) {
	// return CoverBean.Fill.Tensile;
	// }
	// if (value.equals("4")) {
	// return CoverBean.Fill.Nine;
	// } else {
	// return CoverBean.Fill.None;
	// }
	// }
	//
	// private CoverBean.Valign stringToValign(String value) {
	// if (null == value) {
	// return CoverBean.Valign.None;
	// }
	//
	// if (value.equals("1")) {
	// return CoverBean.Valign.Top;
	// }
	// if (value.equals("2")) {
	// return CoverBean.Valign.Mid;
	// }
	// if (value.equals("3")) {
	// return CoverBean.Valign.Botton;
	// } else {
	// return CoverBean.Valign.None;
	// }
	// }
	//
	// private CoverBean.Halign stringToHalign(String value) {
	// if (null == value) {
	// return CoverBean.Halign.None;
	// }
	//
	// if (value.equals("1")) {
	// return CoverBean.Halign.Left;
	// }
	// if (value.equals("2")) {
	// return CoverBean.Halign.Center;
	// }
	// if (value.equals("3")) {
	// return CoverBean.Halign.Right;
	// } else {
	// return CoverBean.Halign.None;
	// }
	// }
	//
	// private CoverBean.ShowlightMode stringToShowlightMode(String value) {
	// if (null == value) {
	// return CoverBean.ShowlightMode.None;
	// }
	//
	// if (value.equals("1")) {
	// return CoverBean.ShowlightMode.AndroidSytem;
	// }
	// if (value.equals("2")) {
	// return CoverBean.ShowlightMode.Light;
	// } else {
	// return CoverBean.ShowlightMode.None;
	// }
	// }

	public static int parseColor(String colorString) {
		if (colorString == null || colorString.length() == 0) {
			return Color.TRANSPARENT;
		}

		try {
			final int color = Color.parseColor(colorString);
			return color;
		} catch (IllegalArgumentException e) {
			// TODO: handle exception
		}
		return Color.TRANSPARENT;
	}
}