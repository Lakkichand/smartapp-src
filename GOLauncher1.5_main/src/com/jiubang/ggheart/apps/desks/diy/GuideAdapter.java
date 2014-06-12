package com.jiubang.ggheart.apps.desks.diy;

import android.app.Activity;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gau.go.launcherex.R;

public class GuideAdapter extends BaseAdapter {

	private GoGuideActivity mActivity;
	public static int UPDATEGUIDECOUNT = 1;
	public static int SHOWLASTPAGECOUNT = 2;
	private float mTextSize;
	boolean mUpDate = false;

	public GuideAdapter(Activity context) {
		mActivity = (GoGuideActivity) context;
	}

	@Override
	public int getCount() {
		return 3;
	}

	@Override
	public Object getItem(int position) {

		return position;
	}

	@Override
	public long getItemId(int position) {

		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		// if(mUpDate){//更新模式
		// if(showQuickSettingPackage() && position == getCount()-2)
		// {
		// if(null == convertView)
		// {
		// convertView =
		// LayoutInflater.from(mActivity).inflate(R.layout.guide_lastpage,
		// null);
		// }
		// // setUpQuickSettingPage(convertView);
		// }
		// else if(!showQuickSettingPackage() && position == getCount()-1 ){
		// if(null == convertView)
		// {
		// convertView =
		// LayoutInflater.from(mActivity).inflate(R.layout.guidelayoutitem,
		// null);
		// }
		// ImageView img = (ImageView) convertView.findViewById(R.id.img_guid);
		// img.setImageDrawable(null);
		// }
		// else
		// {
		// if(null == convertView)
		// {
		// convertView =
		// LayoutInflater.from(mActivity).inflate(R.layout.guidelayoutitem,
		// null);
		// }
		// ImageView img = (ImageView) convertView.findViewById(R.id.img_guid);
		//
		// // TextView txt1 = (TextView)
		// convertView.findViewById(R.id.guide_txt1);
		// TextView txt2 = (TextView) convertView.findViewById(R.id.guide_txt2);
		// // TextView txt3 = (TextView)
		// convertView.findViewById(R.id.guide_txt3);
		// txt2.setVisibility(View.INVISIBLE);
		// // txt3.setVisibility(View.INVISIBLE);
		// // txt1.setTextSize(mTitleSize);
		// txt2.setTextSize(mTextSize);
		// // txt3.setTextSize(mTextSize);
		// if(position >= mRes.length)
		// {
		// img.setImageDrawable(null);
		// }
		//
		// switch (position)
		// {
		// case 0:
		// String aboutTitle = "V"
		// + mActivity.getString(R.string.curVersion);
		//
		// img.setVisibility(View.GONE);
		// // txt1.setText(aboutTitle);
		// txt2.setVisibility(View.GONE);
		// // txt3.setVisibility(View.GONE);
		// // setUpChangeLogPage( convertView);
		// break;
		// default:
		// img.setImageDrawable(null);
		// // txt1.setVisibility(View.GONE);
		// break;
		// }
		// }
		// }
		// else {//全新安装

		// if(position == getCount()-1)
		// {
		// if(null == convertView)
		// {
		// convertView =
		// LayoutInflater.from(mActivity).inflate(R.layout.guide_lastpage,
		// null);
		// }
		// setUpQuickSettingPage(convertView);
		// }
		// else
		// {
		Resources resources = mActivity.getResources();
		if (null == convertView) {
			convertView = LayoutInflater.from(mActivity).inflate(R.layout.guidelayoutitem, null);
		}
		ImageView img = (ImageView) convertView.findViewById(R.id.img_guid);
		TextView txt2 = (TextView) convertView.findViewById(R.id.guide_txt2);
		txt2.setVisibility(View.INVISIBLE);
		txt2.setTextSize(mTextSize);
		switch (position) {
			case 0 :
				txt2.setText(R.string.guid_theme_title);
				img.setImageDrawable(resources.getDrawable(R.drawable.guide001));
				txt2.setVisibility(View.VISIBLE);
				break;
			case 1 :
				txt2.setText(R.string.guid_effect_title);
				img.setImageDrawable(resources.getDrawable(R.drawable.guide002));
				txt2.setVisibility(View.VISIBLE);
				break;
			case 2 :
				txt2.setText(R.string.guid_widget_title);
				img.setImageDrawable(resources.getDrawable(R.drawable.guide003));
				txt2.setVisibility(View.VISIBLE);
				break;
			default :
				img.setImageDrawable(null);
				break;
		}
		return convertView;
	}
}
