package com.gau.go.launcherex.theme.cover.ui.action;

import android.graphics.Bitmap;
/**
 * 
 * <br>类描述:action控制
 * <br>功能详细描述:
 * 
 * @author  guoyiqing
 * @date  [2012-11-5]
 */
public class ActionControler {
    
	public static final int ACTION_TYPE_EMPTY = -1;
	public static final int ACTION_TYPE_LINE = 0;
	public static final int ACTION_TYPE_BEZIER = 1;
	public static final int ACTION_TYPE_BROKENLINE = 2;
	public static final int ACTION_TYPE_NORMAL = 3;
	public static final int ACTION_TYPE_RULESTRACE = 4;
	public static final int ACTION_TYPE_CURVELINE = 5;
	public static final int ACTION_TYPE_DIFFUSION = 6;
	public static final int ACTION_TYPE_SHAKE = 7; // 云，抖动
	public static final int ACTION_TYPE_SINE = 9; //正弦曲线
	public static final int ACTION_TYPE_SHAKE_LEAVE = 10; //上下震动后离开(飞机)
	public static final int ACTION_TYPE_SLIGHT_MOVEMENT = 11; //眨眼，震动翅膀(猫头鹰)
	
	public static final int ADDITIONAL_ACTION_TYPE_FADEOUT = 100;
	//Dragaction
	public static final int ACTION_TYPE_DRAG = 201; //太阳月亮，上下拉动
	//控制器
	public static final int ACTION_TYPE_CONTROL = 301; //控制器
	//Shakeaction
	public static final int ACTION_TYPE_FLY_COME_AND_GO = 401; //猫头鹰飞走又飞回来
	
	public static final int ACTION_TYPE_FALL_SNOW = 402; //松树上的雪花落下来
	
	private static ActionControler sInstance;

	private ActionControler() {
	}

	public static synchronized ActionControler getInstance() {
		if (sInstance == null) {
			sInstance = new ActionControler();
		}
		return sInstance;
	}
	
	public BaseAction getAction(int actionIndex, int actionType, Drivenable drivenable, int actionDelay,
			Bitmap[] actionBitmaps, Bitmap shadow, int animatingBitmapType,
			Bitmap[] action1Bitmaps, Bitmap[] action2Bitmaps, Bitmap action1Shadow,
			Bitmap action2Shadow, Bitmap defaultAction2Bitmap, boolean needLoop, int animationTime,
			boolean isBitmapSymmetric, int shakeSpeed) {
		BaseAction action = null;
		switch (actionType) {
			case ACTION_TYPE_BEZIER :
				action = new BezierAction(drivenable, actionIndex, actionDelay, actionBitmaps,
						animatingBitmapType, needLoop, animationTime, isBitmapSymmetric);
				break;
			case ACTION_TYPE_BROKENLINE :
				action = new BrokenLineAction(drivenable, actionIndex, animatingBitmapType, action1Bitmaps,
						action2Bitmaps, action1Shadow, action2Shadow, needLoop, isBitmapSymmetric);
				break;
			case ACTION_TYPE_LINE :
				action = new LineAction(drivenable, actionIndex, actionDelay, actionBitmaps,
						animatingBitmapType, defaultAction2Bitmap, isBitmapSymmetric);
				break;
			case ACTION_TYPE_RULESTRACE :
				action = new RulesTraceAction(drivenable, actionIndex, actionBitmaps, animatingBitmapType,
						isBitmapSymmetric);
				break;
			case ACTION_TYPE_NORMAL :
				action = new NormalAction(drivenable, actionIndex, animatingBitmapType, actionBitmaps,
						isBitmapSymmetric);
				break;
			case ACTION_TYPE_EMPTY :
				action = new EmptyAction(drivenable, actionIndex, animatingBitmapType, isBitmapSymmetric);
				break;
			case ACTION_TYPE_CURVELINE :
				action = new CurveLineRandomAction(drivenable, actionIndex, animatingBitmapType, actionBitmaps,
						shadow, isBitmapSymmetric);
				break;
			case ACTION_TYPE_SHAKE:
				action = new JetAction(drivenable, actionIndex, animatingBitmapType, isBitmapSymmetric, actionBitmaps); 
				break;
			case ACTION_TYPE_DRAG:
				action = new DragAction(drivenable, actionIndex, animatingBitmapType, isBitmapSymmetric, actionBitmaps); 
				break;
			case ACTION_TYPE_SINE:
				action = new SineAction(drivenable, actionBitmaps, actionIndex, animatingBitmapType, isBitmapSymmetric); 
				break;
			case ACTION_TYPE_SHAKE_LEAVE:
				action = new ShakeLeaveAction(drivenable, actionIndex, animatingBitmapType, isBitmapSymmetric, actionBitmaps); 
				break;
			case ACTION_TYPE_CONTROL:
				action = new KeepSideAction(drivenable, actionBitmaps, actionIndex, animatingBitmapType, isBitmapSymmetric);
				break;
			case ACTION_TYPE_FLY_COME_AND_GO:
				action = new FlyComeAndGoAction(drivenable, actionBitmaps, shakeSpeed, shakeSpeed, isBitmapSymmetric, shakeSpeed); 
				break;
			case ACTION_TYPE_SLIGHT_MOVEMENT:
				action = new SlightMovementAction(drivenable, actionBitmaps, actionIndex, animatingBitmapType, isBitmapSymmetric);
				break;
			case ACTION_TYPE_FALL_SNOW:
				action = new FallObjectAction(drivenable, actionBitmaps, actionIndex, animatingBitmapType, isBitmapSymmetric, shakeSpeed);
				break;
			default :
				action = new EmptyAction(drivenable, actionIndex, animatingBitmapType, isBitmapSymmetric);
				break;
		}
		return action;
	}

	public BaseAction getAdditionalAction(int actionIndex, int actionType, Drivenable drivenable, int actionDelay,
			Bitmap[] actionBitmaps, Bitmap shadow, int animatingBitmapType, boolean needLoop,
			int animationTime, boolean isBitmapSymmetric) {
		BaseAction action = null;
		switch (actionType) {
			case ADDITIONAL_ACTION_TYPE_FADEOUT :
				action = new FadeOutAction(drivenable, actionIndex, animatingBitmapType, actionBitmaps, shadow,
						isBitmapSymmetric);
				break;
		}
		return action;
	}

	public void prepareAction(BaseAction action, int actionType, Bitmap[] actionBitmaps,
			Bitmap shadow, Bitmap[] action1Bitmaps, Bitmap[] action2Bitmaps, Bitmap action1Shadow,
			Bitmap action2Shadow, Bitmap defaultAction2Bitmap) {
		action.onResume(actionBitmaps, shadow, action1Bitmaps, action2Bitmaps, action1Shadow,
				action2Shadow, defaultAction2Bitmap);
	}

	public static void clearInstance() {
		if (sInstance != null) {
			sInstance = null;
		}
	}

}
