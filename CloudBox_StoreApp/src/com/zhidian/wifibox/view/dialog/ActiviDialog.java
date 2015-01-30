package com.zhidian.wifibox.view.dialog;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.ta.TAApplication;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.activity.ActivitActivity;
import com.zhidian.wifibox.activity.AppDetailActivity;
import com.zhidian.wifibox.activity.HTMLGameActivity;
import com.zhidian.wifibox.data.AppDataBean;
import com.zhidian.wifibox.data.CDataDownloader;
import com.zhidian.wifibox.data.CategoriesDataBean;
import com.zhidian.wifibox.data.PopupCommend;
import com.zhidian.wifibox.data.TopicDataBean;
import com.zhidian.wifibox.message.IDiyFrameIds;
import com.zhidian.wifibox.message.IDiyMsgIds;
import com.zhidian.wifibox.util.PathConstant;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager.AsyncImageLoadedCallBack;

/**
 * 活动对话框
 * 
 * @author zhaoyl
 * 
 */
public class ActiviDialog extends Dialog implements
		android.view.View.OnClickListener {

	private PopupCommend popupCommend;
	private Context mContext;

	public ActiviDialog(Context context, PopupCommend pop) {
		super(context, R.style.Dialog);
		setContentView(R.layout.dialog_activity);
		popupCommend = pop;
		mContext = context;
		ImageButton btnClose = (ImageButton) findViewById(R.id.activi_close);
		btnClose.setOnClickListener(this);
		final ImageView ivImage = (ImageView) findViewById(R.id.activi_img);
		ivImage.setOnClickListener(this);
		ivImage.setTag(pop.imageUrl);
		Bitmap bm = AsyncImageManager.getInstance().loadImage(
				PathConstant.ICON_ROOT_PATH, pop.imageUrl.hashCode() + "",
				pop.imageUrl, true, false, new AsyncImageLoadedCallBack() {

					@Override
					public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
						if (imageBitmap == null) {
							return;
						}
						if (ivImage.getTag().equals(imgUrl)) {
							ivImage.setImageBitmap(imageBitmap);
						}
					}
				});
		if (bm != null) {
			ivImage.setImageBitmap(bm);
		} else {
			// 默认
			// ivImage.setImageBitmap(DrawUtil.sDefaultIcon);
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.activi_close:
			dismiss();
			break;

		case R.id.activi_img:
			gotoActivitDetail();
			dismiss();
			break;

		default:
			break;
		}

	}

	/**
	 * 跳转到活动界面
	 */
	private void gotoActivitDetail() {
		switch (popupCommend.type) {
		case 1: {
			// 专题
			try {
				long id = Long.valueOf(popupCommend.target);
				// 跳转到专题内容
				List<Object> list = new ArrayList<Object>();
				TopicDataBean bean = new TopicDataBean();
				bean.id = id;
				bean.title = popupCommend.title;
				list.add(bean);
				// 通知TabManageView跳转下一层级，把TopicDataBean带过去
				TAApplication.sendHandler(null, IDiyFrameIds.TABMANAGEVIEW,
						IDiyMsgIds.ENTER_NEXT_LEVEL, -1,
						CDataDownloader.getTopicContentUrl(id, 1), list);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			break;
		}
		case 2: {
			// 活动
			String ActivitUrl = popupCommend.target;
			String title = popupCommend.title;
			Intent intent = new Intent();
			intent.setClass(mContext, ActivitActivity.class);
			intent.putExtra(ActivitActivity.TITLE, title);
			intent.putExtra(ActivitActivity.URL, ActivitUrl);
			mContext.startActivity(intent);
			break;
		}
		case 3: {
			// 应用详情
			AppDataBean bean = null;
			long id = Long.valueOf(popupCommend.target);
			Intent intent = new Intent(mContext, AppDetailActivity.class);
			intent.putExtra("bean", bean);
			intent.putExtra("appId", id);
			mContext.startActivity(intent);
			break;
		}
		case 4: {
			// 应用分类列表
			long id = Long.valueOf(popupCommend.target);
			// 跳转到分类内容列表
			CategoriesDataBean cbean = new CategoriesDataBean();
			List<Object> list = new ArrayList<Object>();
			cbean.name = popupCommend.title;
			list.add(cbean);
			// 通知TabManageView跳转下一层级，把TopicDataBean带过去
			TAApplication.sendHandler(null, IDiyFrameIds.TABMANAGEVIEW,
					IDiyMsgIds.ENTER_NEXT_LEVEL, -1,
					CDataDownloader.getCategoryContentUrl(id, 1), list);
			break;
		}
		case 5: {
			// HTML游戏
			Intent intent = new Intent(getContext(), HTMLGameActivity.class);
			intent.putExtra(HTMLGameActivity.GAMEURLKEY, popupCommend.target);
			getContext().startActivity(intent);
			break;
		}
		default:
			break;
		}
	}
}
