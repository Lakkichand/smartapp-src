package com.jiubang.ggheart.apps.desks.Preferences.info;

/**
 * 
 * <br>类描述:对象
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-9-10]
 */
public class DeskSettingInfo {
	private DeskSettingInfo mParentInfo; //父类对象，和2级菜单互相引用
	private DeskSettingInfo mSecondInfo; //二级菜单
	private DeskSettingSingleInfo mSingleInfo; //单选
	private DeskSettingMultiInfo mMultiInInfo; //多选对象
	private DeskSettingSeekBarInfo mSeekBarInfo; //进度条对象
	private DeskSettingNormalInfo mNormalInfo;	//普通对话框
	private DeskSettingGestureInfo mGestureInfo;	//手势对话框
	
	private int mType = -1; //0：单选 1：多选 ：2：进度条 3：带CheckBox的对话框 4：普通对话框
	private int mCustomPosition = -1; //2级菜单的位置

	
	public DeskSettingInfo getParentInfo() {
		return mParentInfo;
	}
	public void setParentInfo(DeskSettingInfo parentInfo) {
		this.mParentInfo = parentInfo;
	}
	
	
	public DeskSettingInfo getSecondInfo() {
		return mSecondInfo;
	}
	public void setSecondInfo(DeskSettingInfo secondInfo) {
		this.mSecondInfo = secondInfo;
	}
	public DeskSettingSingleInfo getSingleInfo() {
		return mSingleInfo;
	}
	public void setSingleInfo(DeskSettingSingleInfo singleInfo) {
		this.mSingleInfo = singleInfo;
	}
	public DeskSettingMultiInfo getMultiInInfo() {
		return mMultiInInfo;
	}
	public void setMultiInInfo(DeskSettingMultiInfo multiInInfo) {
		this.mMultiInInfo = multiInInfo;
	}
	public DeskSettingSeekBarInfo getSeekBarInfo() {
		return mSeekBarInfo;
	}
	public void setSeekBarInfo(DeskSettingSeekBarInfo seekBarInfo) {
		this.mSeekBarInfo = seekBarInfo;
	}

	public DeskSettingNormalInfo getNormalInfo() {
		return mNormalInfo;
	}
	public void setNormalInfo(DeskSettingNormalInfo normalInfo) {
		this.mNormalInfo = normalInfo;
	}

	public DeskSettingGestureInfo getmGestureInfo() {
		return mGestureInfo;
	}
	public void setmGestureInfo(DeskSettingGestureInfo mGestureInfo) {
		this.mGestureInfo = mGestureInfo;
	}
	public int getType() {
		return mType;
	}
	public void setType(int type) {
		this.mType = type;
	}
	public int getCustomPosition() {
		return mCustomPosition;
	}
	public void setCustomPosition(int customPosition) {
		this.mCustomPosition = customPosition;
	}

}
