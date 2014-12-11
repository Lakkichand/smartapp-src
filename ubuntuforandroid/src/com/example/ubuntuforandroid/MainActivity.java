package com.example.ubuntuforandroid;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.os.Bundle;
import android.app.Activity;
import android.content.res.AssetManager;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

	TextView test = null;
	Button btn = null;
	
// http://sunzeduo.blog.51cto.com/2758509/1376117
    private void initInjectFunction(String fileName) {
        try {

            if (!this.getFilesDir().exists()) {
            	this.getFilesDir().mkdirs();
            }

            File sdCardFile = new File(this.getFilesDir(), fileName);
            FileOutputStream out = new FileOutputStream(sdCardFile);
            AssetManager assetManager = this.getAssets();
            InputStream in = assetManager.open(fileName);
            byte[] buf = new byte[1024];
            int len = 0;
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }    		
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
                
        setContentView(R.layout.activity_main);
        
        initInjectFunction("testfile");
        
        test = (TextView)this.findViewById(R.id.testview);
        test.setText("点击卸载后提示按钮，你的应用在卸载以后会调用浏览器，然后调用你需要的页面。");
        
               
        btn = (Button)this.findViewById(R.id.testbtn);
        btn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String directory = MainActivity.this.getFilesDir().getAbsolutePath();
				String url = "http://www.sohu.com/";
				JniExec.Reguninstall(directory,url);
				test.setText("现在可以退出应用，然后卸载应用，看看是否有效果");

			}
		});
        
      
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
