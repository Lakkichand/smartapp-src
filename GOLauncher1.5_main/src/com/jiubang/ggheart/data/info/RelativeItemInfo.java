package com.jiubang.ggheart.data.info;

public class RelativeItemInfo extends ItemInfo {
	private AppItemInfo mRelativeAppItemInfo;

	public RelativeItemInfo() {
		super();
	}

	public RelativeItemInfo(RelativeItemInfo info) {
		super(info);
		setRelativeItemInfo(info.mRelativeAppItemInfo);
	}

	public boolean setRelativeItemInfo(AppItemInfo info) {
		if (null != mRelativeAppItemInfo) {
			mRelativeAppItemInfo.unRegisterObserver(this);
		}

		mRelativeAppItemInfo = info;
		if (null != mRelativeAppItemInfo) {
			mRelativeAppItemInfo.registerObserver(this);
		}
		return null != mRelativeAppItemInfo;
	}

	public AppItemInfo getRelativeItemInfo() {
		return mRelativeAppItemInfo;
	}

	@Override
	public void selfDestruct() {
		super.selfDestruct();

		if (null != mRelativeAppItemInfo) {
			mRelativeAppItemInfo.unRegisterObserver(this);
			mRelativeAppItemInfo = null;
		}
	}
}
