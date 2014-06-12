package com.jiubang.ggheart.components;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.go.util.Utilities;
import com.jiubang.ggheart.data.BroadCaster.BroadCasterObserver;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.launcher.GOLauncherApp;

public class DeskIcon extends DeskTextView implements BroadCasterObserver {
	public DeskIcon(Context context) {
		super(context);
		selfConstruct();
	}

	public DeskIcon(Context context, AttributeSet attrs) {
		super(context, attrs);
		selfConstruct();
	}

	public DeskIcon(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		selfConstruct();
	}

	/**
	 * 设置图标
	 * 
	 * @param icon
	 *            图标
	 */
	public void setIcon(Drawable icon) {
		icon = Utilities.createIconThumbnail(icon, getContext());
		setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void onBCChange(int msgId, int param, Object object, List objects) {
		switch (msgId) {
			case AppItemInfo.INCONCHANGE : {
				if (object != null && object instanceof Drawable) {
					final Object tag = getTag();
					final Drawable icon = (Drawable) object;
					post(new Runnable() {
						@Override
						public void run() {
							if (tag != null && tag instanceof ShortCutInfo) {
								final Drawable newIconDrawable = ((ShortCutInfo) tag).mIcon;
								setIcon(newIconDrawable);
							} else {
								setIcon(icon);
							}
						}
					});
				}
				break;
			}

			case AppItemInfo.TITLECHANGE : {
				final Object tag = getTag();
				if (tag != null && tag instanceof ShortCutInfo) {
					final ShortCutInfo info = (ShortCutInfo) tag;
					if (object != null && object instanceof String) {
						final CharSequence title = info.mTitle;
						final boolean showTitle = GOLauncherApp.getSettingControler()
								.getDesktopSettingInfo().isShowTitle();

						post(new Runnable() {
							@Override
							public void run() {
								if (showTitle) {
									if (!info.mIsUserTitle) {
										setText(title);
									}
								} else {
									setText(null);
								}
							}
						});
					}
				}
				break;
			}

			default :
				break;
		}
	}

}
