package com.youle.gamebox.ui.activity;

import android.content.Context;
import android.content.Intent;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.fragment.NewsListFragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.youle.gamebox.ui.fragment.StagoryDetailFragment;

/**
 * 新闻列表
 * 
 * @author zhaoyl
 * 
 */
public class NewsActivity extends BaseActivity {

	private NewsListFragment fragment;
    public static final String ID = "id";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        long id  = getIntent().getLongExtra(ID,-1);
            fragment = new NewsListFragment();
            addFragment(fragment, true);
            initTitleView();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (!fragment.isHidden()) {
				initTitleView();
			}
		}
		return super.onKeyDown(keyCode, event);

	}

	public void initTitleView() {
		View titleView = LayoutInflater.from(this).inflate(
				R.layout.default_title_layout, null);
		titleView.findViewById(R.id.back).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onBackPressed();
						//finish();
					}
				});
		TextView title = (TextView) titleView.findViewById(R.id.title);
		title.setText(R.string.news_phone_game);
		setmTitleView(titleView);
	}
    public static void startNewsDetail(Context context,long id ){
        Intent intent = new Intent(context,NewsActivity.class);
        intent.putExtra(ID,id);
        context.startActivity(intent);
    }
}
