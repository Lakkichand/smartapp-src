package com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs;

import java.util.ArrayList;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.graphics.ImageUtil;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogMultiChoice;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditBoxFrame;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditTabView;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.data.info.EffectSettingInfo;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 特效设置
 * 
 * @author jiangchao
 * 
 */
public class EffectTab extends BaseTab {

	private String[] mItemsRes;

	private String[] mItemNumValue;
	private EffectSettingInfo mEffectInfo;
	private Context mContext;
	private static final int EFFECTOR_TYPE_RANDOM_CUSTOM = -2;
	private static final int GRID_EFFECTOR_TYPE_CYLINDER = 15;
	private static final int GRID_EFFECTOR_TYPE_SPHERE = 16;

	private boolean[] mDeskCustomRandomCheckStatus;
	int mOldsetting = 0;
	int mOldselect = 0; // 从0 开始
	int mCancle2select; // 取消的时候，记录上次选中
	View mOldview;

	boolean mSuccessRandom = false;

	public EffectTab(Context context, String tag, int level) {
		super(context, tag, level);
		this.mContext = context;
		initData();
	}

	private void initData() {
		// 特效名
		mItemsRes = mContext.getResources().getStringArray(R.array.select_desktop_transition);
		// 数字
		mItemNumValue = mContext.getResources().getStringArray(

		R.array.desktop_transition_value);

		// 获取当前选择特效
		GoSettingControler controler = GOLauncherApp.getSettingControler();
		mEffectInfo = controler.getEffectSettingInfo();
		mOldsetting = mEffectInfo.mEffectorType;
		// 根据已选特效 判断第几个
		for (int i = 0; i < mItemNumValue.length; i++) {
			if (Integer.parseInt(mItemNumValue[i]) == mOldsetting) {
				mOldselect = i;
			}
		}
		// 初始化自定义特效
		getDeskCustomRandomCheckStatus();
	}

	@Override
	public ArrayList<Object> getDtataList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getItemCount() {
		// TODO Auto-generated method stub
		if (mItemsRes != null) {
			return mItemsRes.length;
		}
		return 0;
	}

	@Override
	public View getView(int position) {
		View view = mInflater.inflate(R.layout.screen_edit_item_effect, null);
		ImageView image = (ImageView) view.findViewById(R.id.screen_edit_effect_thumb);
		ImageView image_install = (ImageView) view
				.findViewById(R.id.screen_edit_effect_thumb_installed);
		
		TextView mText = (TextView) view.findViewById(R.id.screen_edit_effect_title);
		/*
		 * if (0 == position) { // 返回
		 * image.setImageResource(R.drawable.screem_visual_back);
		 * text.setText(R.string.back); view.setTag("0"); return view; }
		 */
		// 加入每种特效图标
		switch (Integer.parseInt(mItemNumValue[position])) {
			case 0 :
				image.setImageDrawable(getGoAppsIcons(R.drawable.screenedit_effect01_moren));
				break;
			case -1 :
				image.setImageDrawable(getGoAppsIcons(R.drawable.screenedit_effect02_radom));
				break;
			case -2 :
				image.setImageDrawable(getGoAppsIcons(R.drawable.screenedit_effect03_userdefine));
				break;
			case 9 :
				image.setImageDrawable(getGoAppsIcons(R.drawable.screenedit_effect04_wave));
				break;
			case 4 :
				image.setImageDrawable(getGoAppsIcons(R.drawable.screenedit_effect05_roll));
				break;
			case 8 :
				image.setImageDrawable(getGoAppsIcons(R.drawable.screenedit_effect06_easyroll));
				break;
			case 2 :
				image.setImageDrawable(getGoAppsIcons(R.drawable.screenedit_effect07_wallpicroll));
				break;
			case 7 :
				image.setImageDrawable(getGoAppsIcons(R.drawable.screenedit_effect08_windmills));
				break;
			case 6 :
				image.setImageDrawable(getGoAppsIcons(R.drawable.screenedit_effect09_boxin));
				break;
			case 1 :
				image.setImageDrawable(getGoAppsIcons(R.drawable.screenedit_effect10_boxout));
				break;
			case 3 :
				image.setImageDrawable(getGoAppsIcons(R.drawable.screenedit_effect11_bounce));
				break;
			case 5 :
				image.setImageDrawable(getGoAppsIcons(R.drawable.screenedit_effect12_push));
				break;
			case 11 :
				image.setImageDrawable(getGoAppsIcons(R.drawable.screenedit_effect13_doublechild));
				break;
			case 12 :
				image.setImageDrawable(getGoAppsIcons(R.drawable.screenedit_effect14_gun));
				break;
			case 13 :
				image.setImageDrawable(getGoAppsIcons(R.drawable.screenedit_effect15_shutter));
				break;
			case 14 :
				image.setImageDrawable(getGoAppsIcons(R.drawable.screenedit_effect16_xuan));
				break;
			case 15 :
				image.setImageDrawable(getGoAppsIcons(R.drawable.screenedit_effect17_cylinder));
				break;
			case 16 :
				image.setImageDrawable(getGoAppsIcons(R.drawable.screenedit_effect18_ball));
				break;
			case 10 :
				image.setImageDrawable(getGoAppsIcons(R.drawable.screenedit_effect_stack));
				break;
			default :
				image.setImageDrawable(getGoAppsIcons(R.drawable.screenedit_effect01_moren));
				break;
		}

		if (position == mOldselect) {
			image_install.setImageResource(R.drawable.folder_select);
		}
		// 记录当前view
		mOldview = view;
		mText.setText(mItemsRes[position]);
		view.setTag("" + position);
		mCancle2select = mOldselect;
		return view;
	}

	/**
	 * <br>
	 * 功能简述:通过drawableId拿推荐图标图片 <br>
	 * 功能详细描述:可以过滤某些图标进行download tag标签合成图片（tag图片共享一张，减少图片资源） <br>
	 * @param drawableId
	 * @return　经过合成规则处理后的图片
	 */
	private Drawable getGoAppsIcons(int drawableId) {
		Drawable tag = mContext.getResources().getDrawable(drawableId);

		Drawable drawable = mContext.getResources().getDrawable(R.drawable.screenedit_icon_bg);
		try {
			int width = drawable.getIntrinsicWidth();
			int height = drawable.getIntrinsicHeight();
			Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			Canvas cv = new Canvas(bmp);
			ImageUtil.drawImage(cv, drawable, ImageUtil.STRETCHMODE, 0, 0, width, height, null);
			ImageUtil.drawImage(cv, tag, ImageUtil.STRETCHMODE, 0, 0, width, height, null);
			BitmapDrawable bmd = new BitmapDrawable(bmp);
			bmd.setTargetDensity(mContext.getResources().getDisplayMetrics());
			drawable = bmd;
		} catch (Throwable e) {
			// 出错则不进行download Tag合成图
		}

		return drawable;
	}
	@Override
	public void onClick(View v) {
		super.onClick(v);
		getDeskCustomRandomCheckStatus();
		ScreenEditBoxFrame screenEditBoxFrame = (ScreenEditBoxFrame) GoLauncher
				.getFrame(IDiyFrameIds.SCREEN_EDIT_BOX_FRAME);
		if (screenEditBoxFrame != null) {
			ScreenEditTabView mLayOutView = screenEditBoxFrame.getTabView();
			String tag = (String) v.getTag();

			/*
			 * if (tag.equals("0")) {// 返回
			 * screenEditBoxFrame.setTap(BaseTab.TAB_EFFECTS); return; }
			 */
			String s = mItemNumValue[Integer.parseInt(tag)];
			// 在桌面编辑的时候选中圆柱和椭圆的特效不提示
			// showToast(Integer.parseInt(s));
			mOldselect = Integer.valueOf(tag);

			if (v.getTag().equals("18")) { //18为自定义特效的位置
				alertChooseDialog();
			/*// 设置选中效果
				if (mSuccessRandom) {
					mCancle2select = mOldselect;
					mOldselect = 18;
				}
				mLayOutView.getContainer().setCurrentTab(this);
				int horizontalpading = (int) v.getContext().getResources()
						.getDimension(R.dimen.screen_edit_view_horizontal_space);
				int viewWidth = (int) v.getContext().getResources()
						.getDimension(R.dimen.screen_edit_view_width);
				int mItemsCount = (GoLauncher.getDisplayWidth() - horizontalpading)
						/ (viewWidth + horizontalpading);
				mLayOutView.getContainer().getScreenScroller()
						.setCurrentScreen(mOldselect / mItemsCount);*/
				return;

			} else {

				mLayOutView.getContainer().setCurrentTab(this);
				int horizontalpading = (int) v.getContext().getResources()
						.getDimension(R.dimen.screen_edit_view_horizontal_space);
				int viewWidth = (int) v.getContext().getResources()
						.getDimension(R.dimen.screen_edit_view_width);
				int mItemsCount = (GoLauncher.getDisplayWidth() - horizontalpading)
						/ (viewWidth + horizontalpading);
				int rightSpace = GoLauncher.getDisplayWidth() - horizontalpading - mItemsCount
						* (viewWidth + horizontalpading);
				if (rightSpace >= viewWidth) {
					++mItemsCount;
				}

				mLayOutView.getContainer().getScreenScroller()
						.setCurrentScreen(mOldselect / mItemsCount);

				applyEfftect(s);
			}
		}

	}

	@Override
	public void clearData() {
		// TODO Auto-generated method stub
		mItemsRes = null;
		super.clearData();

	}

	// 什么都没选就设置回当前特效
	private void applyEfftect(String s) {
		// EffectSettingInfo effectSettingInfo = null;
		if (null != mEffectInfo) {
			boolean bChanged = false;

			int type = Integer.parseInt(s);
			if (mEffectInfo.mEffectorType != type) {
				mEffectInfo.mEffectorType = type;
				bChanged = true;
			}
			if (bChanged) {
				GOLauncherApp.getSettingControler().updateEffectSettingInfo(mEffectInfo);
			}
			// 进行预览
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
					IDiyMsgIds.SCREENEDIT_SHOW_TAB_EFFECT_SETTING, mEffectInfo.mEffectorType, null,
					null);
		}

	}

	private void showToast(int value) {
		// 考虑到部分用户的机子性能不好，不能很好支持实现圆柱和球特效，所以提醒用户
		if (value == GRID_EFFECTOR_TYPE_CYLINDER || value == GRID_EFFECTOR_TYPE_SPHERE) {
			Toast.makeText(mContext, mContext.getString(R.string.effect_warn), Toast.LENGTH_LONG)
					.show();
		}
	}

	private void getDeskCustomRandomCheckStatus() {
		String[] itemValue = mContext.getResources().getStringArray(
				R.array.desktop_transition_value);
		int[] checkedValue = mEffectInfo.mEffectCustomRandomEffects;
		// 数字3(去掉默认,随机,自定义随机的长度) 数字2(从数组的第三项开始到倒数第二项--因为有3来限制长度)
		mDeskCustomRandomCheckStatus = new boolean[itemValue.length - 3];
		for (int i = 0; i < mDeskCustomRandomCheckStatus.length; i++) {
			mDeskCustomRandomCheckStatus[i] = false;
		}
		for (int j = 0; j < checkedValue.length; j++) {
			for (int c = 2; c < itemValue.length; c++) {
				if (Integer.valueOf(itemValue[c]) == checkedValue[j]) {
					mDeskCustomRandomCheckStatus[c - 2] = true;
					break;
				}
			}
		}

	}

	public boolean alertChooseDialog() {
		// 数字3(去掉默认,随机,自定义随机的长度) 数字2(从数组的第三项开始到倒数第二项--因为有3来限制长度)
		// 获取用户自定义特效
		getDeskCustomRandomCheckStatus();
		String[] items = new String[mItemsRes.length - 3];
		for (int i = 0; i < items.length; i++) {
			items[i] = mItemsRes[i + 2];
		}
		DialogMultiChoice dialog = new DialogMultiChoice(mContext);
		dialog.show();
		dialog.setTitle(R.string.dialog_title_custom_effect);
		dialog.setItemData(items, mDeskCustomRandomCheckStatus,	new DialogInterface.OnMultiChoiceClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				mDeskCustomRandomCheckStatus[which] = isChecked;
			}
		});
		dialog.setPositiveButton(null, new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String[] items = mContext.getResources().getStringArray(
						R.array.desktop_transition_value);
				ArrayList<Integer> arrayItems = new ArrayList<Integer>();
				ScreenEditBoxFrame screenEditBoxFrame = (ScreenEditBoxFrame) GoLauncher
						.getFrame(IDiyFrameIds.SCREEN_EDIT_BOX_FRAME);
				ScreenEditTabView mLayOutView = null;
				if (screenEditBoxFrame != null) {
					 mLayOutView = screenEditBoxFrame.getTabView();
				} 
				for (int i = 0; i < mDeskCustomRandomCheckStatus.length; i++) {
					if (mDeskCustomRandomCheckStatus[i]) {
						arrayItems.add(Integer.parseInt(items[i + 2]));
					}
				}
				if (arrayItems.isEmpty()) {

					Toast.makeText(mContext, R.string.toast_msg_noeffect_select, Toast.LENGTH_SHORT).show();

						GoSettingControler controler = GOLauncherApp.getSettingControler();
						mEffectInfo = controler.getEffectSettingInfo();
						mOldsetting = mEffectInfo.mEffectorType;
						for (int i = 0; i < mItemNumValue.length; i++) {
							if (Integer.parseInt(mItemNumValue[i]) == mOldsetting) {
								mOldselect = i;
							}
						}

					return;
				}
				int[] effects = new int[arrayItems.size()];
				for (int i = 0; i < effects.length; i++) {
					effects[i] = arrayItems.get(i);
				}

				mSuccessRandom = true;
				mEffectInfo.mEffectCustomRandomEffects = effects;
				mEffectInfo.mEffectorType = EFFECTOR_TYPE_RANDOM_CUSTOM;
				GOLauncherApp.getSettingControler().updateEffectSettingInfo(mEffectInfo);

				arrayItems.clear();
				arrayItems = null;
				// 进行预览
				if (mSuccessRandom) {
					GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
							IDiyMsgIds.SCREENEDIT_SHOW_TAB_EFFECT_SETTING,
							mEffectInfo.mEffectorType, null, null);
				}
				if (mLayOutView != null) {
					mLayOutView.getContainer().setCurrentTab(EffectTab.this);
					int horizontalpading = (int) v
							.getContext()
							.getResources()
							.getDimension(
									R.dimen.screen_edit_view_horizontal_space);
					int viewWidth = (int) v.getContext().getResources()
							.getDimension(R.dimen.screen_edit_view_width);
					int mItemsCount = (GoLauncher.getDisplayWidth() - horizontalpading)
							/ (viewWidth + horizontalpading);
					mLayOutView.getContainer().getScreenScroller()
							.setCurrentScreen(18 / mItemsCount); //18为自定义特效的位置
				}
			}
		});
		dialog.setNegativeButton(null, new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ScreenEditBoxFrame screenEditBoxFrame = (ScreenEditBoxFrame) GoLauncher
						.getFrame(IDiyFrameIds.SCREEN_EDIT_BOX_FRAME);
				if (screenEditBoxFrame != null) {
					ScreenEditTabView mLayOutView = screenEditBoxFrame.getTabView();

					GoSettingControler controler = GOLauncherApp.getSettingControler();
					mEffectInfo = controler.getEffectSettingInfo();
					mOldsetting = mEffectInfo.mEffectorType;
					for (int i = 0; i < mItemNumValue.length; i++) {
						if (Integer.parseInt(mItemNumValue[i]) == mOldsetting) {
							mOldselect = i;
						}
					}

					/*mLayOutView.getContainer().setCurrentTab(EffectTab.this);
					int horizontalpading = (int) v.getContext().getResources()
							.getDimension(R.dimen.screen_edit_view_horizontal_space);
					int viewWidth = (int) v.getContext().getResources()
							.getDimension(R.dimen.screen_edit_view_width);
					int mItemsCount = (GoLauncher.getDisplayWidth() - horizontalpading)
							/ (viewWidth + horizontalpading);
					mLayOutView.getContainer().getScreenScroller()
							.setCurrentScreen(18 / mItemsCount);*/
				}
			}
		});
		return mSuccessRandom;
	}

	@Override
	public void resetData() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onLongClick(View v) {
		// TODO Auto-generated method stub
		return false;
	}
}
