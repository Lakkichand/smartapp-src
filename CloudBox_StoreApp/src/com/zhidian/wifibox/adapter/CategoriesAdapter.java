package com.zhidian.wifibox.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhidian.wifibox.R;
import com.zhidian.wifibox.activity.MainActivity;
import com.zhidian.wifibox.data.CDataDownloader;
import com.zhidian.wifibox.data.CategoriesDataBean;
import com.zhidian.wifibox.message.IDiyFrameIds;
import com.zhidian.wifibox.message.IDiyMsgIds;
import com.zhidian.wifibox.util.DrawUtil;
import com.zhidian.wifibox.util.PathConstant;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager.AsyncImageLoadedCallBack;

/**
 * 游戏分类adapter
 * 
 */
public class CategoriesAdapter extends BaseAdapter {

	private List<CategoriesDataBean> mList = new ArrayList<CategoriesDataBean>();

	private Context mContext;
	private LayoutInflater mInflater;

	private OnClickListener mItemClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// 跳转到分类内容列表
			CategoriesDataBean bean = (CategoriesDataBean) v
					.getTag(R.string.app_name);
			List<Object> list = new ArrayList<Object>();
			list.add(bean);
			// 通知TabManageView跳转下一层级，把TopicDataBean带过去
			MainActivity.sendHandler(null, IDiyFrameIds.TABMANAGEVIEW,
					IDiyMsgIds.ENTER_NEXT_LEVEL, -1,
					CDataDownloader.getCategoryContentUrl(bean.id, 1), list);
		}
	};

	public CategoriesAdapter(Context context) {
		this.mContext = context;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater
					.inflate(R.layout.list_item_categories, null);
			holder.itemLayout = convertView.findViewById(R.id.item_layout);
			holder.ivImage = (ImageView) convertView
					.findViewById(R.id.item_categories_image);
			holder.tvName = (TextView) convertView
					.findViewById(R.id.item_categories_name);
			holder.tvIntro = (TextView) convertView
					.findViewById(R.id.item_categories_intro);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		CategoriesDataBean cd = mList.get(position);
		String url = cd.iconUrl;
		String name = cd.name;
		String intro = cd.explain;
		
		if (position % 4 == 1 || position % 4 == 2) {
			holder.itemLayout.setBackgroundResource(R.drawable.btn_classify_gren_selector);
		}else {
			holder.itemLayout.setBackgroundResource(R.drawable.btn_clasify_white_selector);
		}

		final ImageView image = holder.ivImage;
		image.setTag(url);
		Bitmap bm = AsyncImageManager.getInstance().loadImage(
				PathConstant.ICON_ROOT_PATH, url.hashCode() + "", url, true,
				true, new AsyncImageLoadedCallBack() {

					@Override
					public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
						if (imageBitmap == null) {
							return;
						}
						if (image.getTag().equals(imgUrl)) {
							image.setImageBitmap(imageBitmap);
						}
					}
				});
		if (bm != null) {
			image.setImageBitmap(bm);
		} else {
			// 默认
			image.setImageBitmap(DrawUtil.sDefaultIcon);
		}
		holder.tvName.setText(name);
		holder.tvIntro.setText(stringFilter(intro));

		holder.itemLayout.setTag(R.string.app_name, mList.get(position));
		holder.itemLayout.setOnClickListener(mItemClickListener);
		return convertView;
	}

	/**
	 * 去除特殊字符或将所有中文标号替换为英文标号
	 * 
	 * @param str
	 * @return
	 */
	public static String stringFilter(String str) {
		str = str.replaceAll("【", "[").replaceAll("】", "]")
				.replaceAll("！", "!").replaceAll("：", ":").replaceAll("、", ",")
				.replaceAll("，", ",");// 替换中文标号
		String regEx = "[『』]"; // 清除掉特殊字符
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(str);
		return m.replaceAll("").trim();
	}

	static class ViewHolder {
		View itemLayout;
		ImageView ivImage; // 图片
		TextView tvName; // 类别名称
		TextView tvIntro; // 简介
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	/**
	 * 更新数据，并调用notifyDataSetChanged
	 */
	public void update(List<CategoriesDataBean> list) {
		mList.clear();
		if (list == null || list.size() <= 0) {
			notifyDataSetChanged();
			return;
		}
		mList.addAll(list);
		notifyDataSetChanged();
	}

}
