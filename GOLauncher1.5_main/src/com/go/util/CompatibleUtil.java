package com.go.util;

import android.graphics.Matrix;
import android.view.View;

import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.PropertyValuesHolder;
import com.nineoldandroids.view.animation.AnimatorProxy;

/**
 * 兼容2.X的工具类
 * 
 * @author wangzhuobin
 * 
 */
public class CompatibleUtil {

	/**
	 * 是否需要往低版本兼容，低于3.0 public static final boolean NEEDS_PROXY =
	 * Integer.valueOf(Build.VERSION.SDK).intValue() <
	 * Build.VERSION_CODES.HONEYCOMB;
	 */
	// public static final boolean NEEDS_COMPATIBLE = AnimatorProxy.NEEDS_PROXY;
	public static final boolean NEEDS_COMPATIBLE = true;

	/**
	 * 取消代理，防止设置Visibility无效 原因：ViewGroup的dispatchDraw有如下的代码： final View child =
	 * children[i]; for (int i = 0; i < count; i++) { if ((child.mViewFlags &
	 * VISIBILITY_MASK) == VISIBLE || child.getAnimation() != null) { more |=
	 * drawChild(canvas, child, drawingTime); } }
	 * 如果child设置了Animation，就会调用drawChild进行绘制，而不区分是否可见。
	 * AnimatorProxy就是利用了Animation来实现的。并且设置了setFillAfter(true)。
	 * 
	 * IMPORTANT: 所以当不需要4.0以上的兼容实现接口时及早从AnimatorProxy中移除
	 * 
	 * @param view
	 */
	public static void unwrap(View view) {
		if (view != null) {
			if (NEEDS_COMPATIBLE) {
				AnimatorProxy.unwrap(view);
			}
		}
	}

	/**
	 * 使用{@link PropertyValuesHolder}和{@link ObjectAnimator}
	 * 来修改View属性的Animator需要先用此接口返回一个Object
	 * 
	 * @param view
	 * @return
	 */
	public static Object getPropertyValuesHolderObject(View view) {
		if (NEEDS_COMPATIBLE) {
			return AnimatorProxy.wrap(view);
		} else {
			return view;
		}
	}

	/**
	 * 兼容2.X所写的获取View透明度的方法
	 * 
	 * @return
	 */
	public static float getAlpha(View view) {
		float result = 1.0f;
		if (view != null) {
			if (NEEDS_COMPATIBLE) {
				AnimatorProxy animatorProxy = AnimatorProxy.wrap(view);
				result = animatorProxy.getAlpha();
			}/*
				* else { result = view.getAlpha(); }
				*/
		}
		return result;
	}

	/**
	 * 兼容2.X所写的设置View透明度的方法
	 * 
	 * @return
	 */
	public static void setAlpha(View view, float alpha) {
		if (view != null) {
			if (NEEDS_COMPATIBLE) {
				AnimatorProxy animatorProxy = AnimatorProxy.wrap(view);
				animatorProxy.setAlpha(alpha);
			}/*
				* else { view.setAlpha(alpha); }
				*/
		}
	}

	public static float getPivotX(View view) {
		if (view != null) {
			if (NEEDS_COMPATIBLE) {
				AnimatorProxy animatorProxy = AnimatorProxy.wrap(view);
				return animatorProxy.getPivotX();
			}
			// else {
			// return view.getPivotX();
			// }
		}
		return 0f;
	}

	public static void setPivotX(View view, float pivotX) {
		if (view != null) {
			if (NEEDS_COMPATIBLE) {
				AnimatorProxy animatorProxy = AnimatorProxy.wrap(view);
				animatorProxy.setPivotX(pivotX);
			}
			// else {
			// view.setPivotX(pivotX);
			// }
		}
	}

	public static float getPivotY(View view) {
		if (view != null) {
			if (NEEDS_COMPATIBLE) {
				AnimatorProxy animatorProxy = AnimatorProxy.wrap(view);
				return animatorProxy.getPivotY();
			}
			// else {
			// return view.getPivotY();
			// }
		}
		return 0f;
	}

	public static void setPivotY(View view, float pivotY) {
		if (view != null) {
			if (NEEDS_COMPATIBLE) {
				AnimatorProxy animatorProxy = AnimatorProxy.wrap(view);
				animatorProxy.setPivotY(pivotY);
			}
			// else {
			// view.setPivotY(pivotY);
			// }
		}
	}

	/**
	 * 兼容2.X所写的获取View x值的方法
	 * 
	 * @return
	 */
	public static float getX(View view) {
		float result = 0.0f;
		if (view != null) {
			if (NEEDS_COMPATIBLE) {
				AnimatorProxy animatorProxy = AnimatorProxy.wrap(view);
				result = animatorProxy.getX();
			}
			// else {
			// result = view.getX();
			// }
		}
		return result;
	}

	/**
	 * 兼容2.X所写的设置View x值的方法
	 * 
	 * @return
	 */
	public static void setX(View view, float x) {
		if (view != null) {
			if (NEEDS_COMPATIBLE) {
				AnimatorProxy animatorProxy = AnimatorProxy.wrap(view);
				animatorProxy.setX(x);
			}
			// else {
			// view.setX(x);
			// }
		}
	}

	/**
	 * 兼容2.X所写的获取View y值的方法
	 * 
	 * @return
	 */
	public static float getY(View view) {
		float result = 0.0f;
		if (view != null) {
			if (NEEDS_COMPATIBLE) {
				AnimatorProxy animatorProxy = AnimatorProxy.wrap(view);
				result = animatorProxy.getY();
			}
			// else {
			// result = view.getY();
			// }
		}
		return result;
	}

	/**
	 * 兼容2.X所写的设置View y值的方法
	 * 
	 * @return
	 */
	public static void setY(View view, float y) {
		if (view != null) {
			if (NEEDS_COMPATIBLE) {
				AnimatorProxy animatorProxy = AnimatorProxy.wrap(view);
				animatorProxy.setY(y);
			}
			// else {
			// view.setY(y);
			// }
		}
	}

	/**
	 * 兼容2.X所写的获取View scaleX值的方法
	 * 
	 * @return
	 */
	public static float getScaleX(View view) {
		float result = 1.0f;
		if (view != null) {
			if (NEEDS_COMPATIBLE) {
				AnimatorProxy animatorProxy = AnimatorProxy.wrap(view);
				result = animatorProxy.getScaleX();
			}
			// else {
			// result = view.getScaleX();
			// }
		}
		return result;
	}

	/**
	 * 兼容2.X所写的设置View scaleX值的方法
	 * 
	 * @return
	 */
	public static void setScaleX(View view, float scaleX) {
		if (view != null) {
			if (NEEDS_COMPATIBLE) {
				AnimatorProxy animatorProxy = AnimatorProxy.wrap(view);
				animatorProxy.setScaleX(scaleX);
			}
			// else {
			// view.setScaleX(scaleX);
			// }
		}
	}

	/**
	 * 兼容2.X所写的获取View scaleY值的方法
	 * 
	 * @return
	 */
	public static float getScaleY(View view) {
		float result = 1.0f;
		if (view != null) {
			if (NEEDS_COMPATIBLE) {
				AnimatorProxy animatorProxy = AnimatorProxy.wrap(view);
				result = animatorProxy.getScaleY();
			}
			// else {
			// result = view.getScaleY();
			// }
		}
		return result;
	}

	/**
	 * 兼容2.X所写的设置View scaleY值的方法
	 * 
	 * @return
	 */
	public static void setScaleY(View view, float scaleY) {
		if (view != null) {
			if (NEEDS_COMPATIBLE) {
				AnimatorProxy animatorProxy = AnimatorProxy.wrap(view);
				animatorProxy.setScaleY(scaleY);
			}
			// else {
			// view.setScaleY(scaleY);
			// }
		}
	}

	/**
	 * 兼容2.X所写的获取View translationX值的方法
	 * 
	 * @return
	 */
	public static float getTranslationX(View view) {
		float result = 0.0f;
		if (view != null) {
			if (NEEDS_COMPATIBLE) {
				AnimatorProxy animatorProxy = AnimatorProxy.wrap(view);
				result = animatorProxy.getTranslationX();
			}
			// else {
			// result = view.getTranslationX();
			// }
		}
		return result;
	}

	/**
	 * 兼容2.X所写的设置View translationX值的方法
	 * 
	 * @return
	 */
	public static void setTranslationX(View view, float translationX) {
		if (view != null) {
			if (NEEDS_COMPATIBLE) {
				AnimatorProxy animatorProxy = AnimatorProxy.wrap(view);
				animatorProxy.setTranslationX(translationX);
			}
			// else {
			// view.setTranslationX(translationX);
			// }
		}
	}

	/**
	 * 兼容2.X所写的获取View translationY值的方法
	 * 
	 * @return
	 */
	public static float getTranslationY(View view) {
		float result = 0.0f;
		if (view != null) {
			if (NEEDS_COMPATIBLE) {
				AnimatorProxy animatorProxy = AnimatorProxy.wrap(view);
				result = animatorProxy.getTranslationY();
			}
			// else {
			// result = view.getTranslationY();
			// }
		}
		return result;
	}

	/**
	 * 兼容2.X所写的设置View translationY值的方法
	 * 
	 * @return
	 */
	public static void setTranslationY(View view, float translationY) {
		if (view != null) {
			if (NEEDS_COMPATIBLE) {
				AnimatorProxy animatorProxy = AnimatorProxy.wrap(view);
				animatorProxy.setTranslationY(translationY);
			}
			// else {
			// view.setTranslationY(translationY);
			// }
		}
	}

	/**
	 * 兼容2.X所写的获取View getMatrix值的方法
	 * 
	 * @return
	 */
	public static Matrix getMatrix(View view) {
		if (view != null) {
			if (NEEDS_COMPATIBLE) {
				AnimatorProxy animatorProxy = AnimatorProxy.wrap(view);
				return animatorProxy.getMatrix();
			}
			// else {
			// return view.getMatrix();
			// }
		}
		return null;
	}

	/**
	 * 兼容2.X所写的判断是否硬件加速的方法
	 * 
	 * @param canvas
	 * @return
	 */
	// public static boolean isHardwareAccelerated(Canvas canvas){
	// boolean result = false;
	// if(canvas != null && !NEEDS_COMPATIBLE){
	// result = canvas.isHardwareAccelerated();
	// }
	// return result;
	// }
}
