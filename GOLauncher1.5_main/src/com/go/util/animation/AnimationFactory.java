package com.go.util.animation;

import java.util.Random;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.appfunc.setting.FunAppSetting;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.data.info.AppSettingDefault;
import com.jiubang.ggheart.launcher.GOLauncherApp;

public class AnimationFactory {

	// private final static String TYPE = "anim";
	private static Random random = new Random();
	private static int MAX_RANDOM = 6;

	/**
	 * 
	 * @param effectId
	 *            动画的XML或者动画的ID
	 * @param context
	 * @param packageName
	 *            包路径
	 * @return 动画类
	 */
	public static Animation createEnterAnimation(int effectId, Context context) {
		// 随机
		if (effectId == 0) {
			int nextInt = random.nextInt(MAX_RANDOM) + 1;
			return getEnterAnimation(nextInt, context);
		} else if (effectId == -1) {
			GoSettingControler controler = GOLauncherApp.getSettingControler();
			if (controler != null) {
				FunAppSetting funAppSetting = controler.getFunAppSetting();
				if (funAppSetting != null) {
					int[] effects = funAppSetting.getAppInOutCustomRandomEffect();
					if (effects != null) {
						int nextInt = random.nextInt(effects.length);
						return getEnterAnimation(effects[nextInt], context);
					}
				}
			}
			return getEnterAnimation(AppSettingDefault.INOUTEFFECT, context);
		} else {
			return getEnterAnimation(effectId, context);
		}
	}

	/**
	 * 
	 * @param effectId
	 *            动画的XML或者动画的ID
	 * @param context
	 * @param packageName
	 *            包路径
	 * @return 动画类
	 */
	public static Animation createExitAnimation(int effectId, Context context) {
		// 随机
		if (effectId == 0) {
			int nextInt = random.nextInt(MAX_RANDOM) + 1;
			return getExitAnimation(nextInt, context);
		} else if (effectId == -1) {
			GoSettingControler controler = GOLauncherApp.getSettingControler();
			if (controler != null) {
				FunAppSetting funAppSetting = controler.getFunAppSetting();
				if (funAppSetting != null) {
					int[] effects = funAppSetting.getAppInOutCustomRandomEffect();
					if (effects != null) {
						int nextInt = random.nextInt(effects.length);
						return getExitAnimation(effects[nextInt], context);
					}
				}
			}
			return getExitAnimation(AppSettingDefault.INOUTEFFECT, context);
		} else {
			return getExitAnimation(effectId, context);
		}
	}

	private static Animation getEnterAnimation(int effectId, Context context) {
		Animation animation = null;
		switch (effectId) {
			case 1 : {
				animation = AnimationUtils.loadAnimation(context, R.anim.enter_appfunc_1);
			}
				break;
			case 2 : {
				animation = AnimationUtils.loadAnimation(context, R.anim.enter_appfunc_2);
			}
				break;

			// case 3: {
			// int identifier = context.getResources().getIdentifier(
			// "enter_appfunc_3", TYPE, packageName);
			// if (identifier <= 0) {
			// identifier = context.getResources().getIdentifier(
			// "enter_appfunc_3", TYPE, context.getPackageName());
			// animation = AnimationUtils.loadAnimation(context, identifier);
			// } else {
			// animation = AnimationUtils.loadAnimation(context, identifier);
			// }
			// }
			// break;
			case 4 : {
				animation = new DepthAnimation(-300, 0, 0, 1);
				animation.setDuration(450);
			}
				break;
			case 5 : { // 翻转效果
				animation = new Flip3DAnimation(90, 0);
				animation.setDuration(450);
			}
				break;
			case 3 : { // 电视机效果
				animation = new ScaleAnimation(2, 1, 0, 1, Animation.RELATIVE_TO_SELF, 0.5f,
						Animation.RELATIVE_TO_SELF, 0.5f);
				animation.setInterpolator(new ExponentialInterpolator());
				animation.setDuration(575);
			}
				break;
			case 6 : { // 无效果
				animation = null;
			}
				break;
			default :
				animation = AnimationUtils.loadAnimation(context, R.anim.enter_appfunc_1);
				break;
		}
		return animation;
	}

	private static Animation getExitAnimation(int effectId, Context context) {
		Animation animation = null;
		switch (effectId) {
			case 1 : {
				animation = AnimationUtils.loadAnimation(context, R.anim.exit_appfunc_1);
			}
				break;

			case 2 : {
				animation = AnimationUtils.loadAnimation(context, R.anim.exit_appfunc_2);
			}
				break;

			// case 3: {
			// int identifier = context.getResources().getIdentifier(
			// "exit_appfunc_3", TYPE, packageName);
			// if (identifier <= 0) {
			// identifier = context.getResources().getIdentifier(
			// "exit_appfunc_3", TYPE, context.getPackageName());
			// animation = AnimationUtils.loadAnimation(context, identifier);
			// } else {
			// animation = AnimationUtils.loadAnimation(context, identifier);
			// }
			// }
			// break;
			case 4 : {
				animation = new DepthAnimation(0, -300, 1, 0);
				animation.setDuration(400);
			}
				break;
			case 5 : { // 翻转效果
				animation = new Flip3DAnimation(0, -90);
				animation.setDuration(400);
			}
				break;
			case 3 : { // 电视机效果
				animation = new ScaleAnimation(1, 2, 1, 0, Animation.RELATIVE_TO_SELF, 0.5f,
						Animation.RELATIVE_TO_SELF, 0.5f);
				animation.setInterpolator(new ExponentialInterpolator());
				animation.setDuration(400);
			}
				break;
			case 6 : { // 无效果
				animation = null;
			}
				break;
			default :
				animation = AnimationUtils.loadAnimation(context, R.anim.exit_appfunc_1);
				break;
		}
		return animation;
	}

}
