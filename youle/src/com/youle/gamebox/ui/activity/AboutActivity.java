package com.youle.gamebox.ui.activity;

import android.content.Intent;
import android.net.Uri;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.util.YouleUtils;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class AboutActivity extends Activity implements OnClickListener {

    private View ivBack; // 返回按钮
    private TextView tvTitle;// 标题
    private TextView tvVersion; //版本

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        Button button = (Button) findViewById(R.id.youle_index_btn);
        button.setOnClickListener(this);
        button = (Button) findViewById(R.id.youle_community_btn);
        button.setOnClickListener(this);

        tvVersion = (TextView) findViewById(R.id.version);
        ivBack = findViewById(R.id.back);
        tvTitle = (TextView) findViewById(R.id.title);
        tvTitle.setText("关于");
        ivBack.setOnClickListener(this);

        String version = YouleUtils.getVersionName(this);
        tvVersion.setText("(版本：" + version + ")");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.youle_index_btn:// 游乐首页网站
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse("http://n.y6.cn/");
                intent.setData(content_url);
                startActivity(intent);
                break;

            case R.id.youle_community_btn:// 游乐手游社区
                Intent i= new Intent();
                i.setAction("android.intent.action.VIEW");
                Uri c= Uri.parse("http://bbs.y6.cn/forum.php");
                i.setData(c);
                startActivity(i);
                break;

            case R.id.back:
                this.finish();
                break;

            default:
                break;
        }

    }
}
