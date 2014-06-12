package com.jiubang.ggheart.apps.desks.diy;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  dingzijian
 * @date  [2012-9-17]
 */
public class GuideTipsView extends RelativeLayout {
	private TextView mUpdatedetail2;
	private TextView mUpdatedetail3;
	private TextView mUpdatedetail4;
	private TextView mUpdatedetail5;
	private TextView mUpdatedetail6;
	private TextView mUpdatedetail7;
	private TextView mUpdatedetail8;
	private TextView mUpdatedetail9;
	private List<TextView> mTextViews;
//	private boolean mIsChinese;
	private Context mContext;
	// Go桌面推向微博分享有奖活动的url
//	private final static String GOLAUNCHER_WEIBO_SHARE_FUNCTION = "http://www.gobbs.com.cn/thread-20148-1-1.html";
	public GuideTipsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
//		Locale l = Locale.getDefault();
//		String language = String.format("%s-%s", l.getLanguage(), l.getCountry());
//		mIsChinese = language.startsWith("zh");
	}

	@Override
	protected void onFinishInflate() {
		mTextViews = new ArrayList<TextView>();
		mUpdatedetail2 = (TextView) findViewById(R.id.updatedetail2);
		addTextView(mUpdatedetail2);
		mUpdatedetail3 = (TextView) findViewById(R.id.updatedetail3);
		addTextView(mUpdatedetail3);
		mUpdatedetail4 = (TextView) findViewById(R.id.updatedetail4);
		addTextView(mUpdatedetail4);
		mUpdatedetail5 = (TextView) findViewById(R.id.updatedetail5);
		addTextView(mUpdatedetail5);
		mUpdatedetail6 = (TextView) findViewById(R.id.updatedetail6);
		addTextView(mUpdatedetail6);
		mUpdatedetail7 = (TextView) findViewById(R.id.updatedetail7);
		addTextView(mUpdatedetail7);
		mUpdatedetail8 = (TextView) findViewById(R.id.updatedetail8);
		addTextView(mUpdatedetail8);
		mUpdatedetail9 = (TextView) findViewById(R.id.updatedetail9);
		addTextView(mUpdatedetail9);
		for (TextView view : mTextViews) {
			if ("".equals(view.getText())) {
				view.setVisibility(View.GONE);
			}
		}
		super.onFinishInflate();
		//临时加入跳转微博分享活动地址
//		if (mIsChinese) {
//			String updateLog = mUpdatedetail2.getText().toString();
////			int start = updateLog.indexOf('(');
////			int end = updateLog.indexOf(')');
//			SpannableString s = new SpannableString(updateLog);
//			s.setSpan(new TextUrlSpan(GOLAUNCHER_WEIBO_SHARE_FUNCTION), 0, updateLog.length() - 1 ,
//					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
////			s.setSpan(new ForegroundColorSpan(0xFF55AB34), 0, updateLog.length(),
////					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//			mUpdatedetail2.setText(s);
//			mUpdatedetail2.setMovementMethod(LinkMovementMethod.getInstance());
//		}
	}

	private void addTextView(TextView view) {
		if (null != mTextViews && view != null) {
			mTextViews.add(view);
		}
	}
	/**
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 * 
	 * @author  dingzijian
	 * @date  [2012-9-17]
	 */
//	private class TextUrlSpan extends ClickableSpan {
//		private String mUrl;
//
//		public TextUrlSpan(String url) {
//			super();
//			mUrl = url;
//		}
//
//		@Override
//		public void onClick(View widget) {
//
//			AppUtils.gotoBrowser(mContext, mUrl);
//		}
//
//		@Override
//		public void updateDrawState(TextPaint ds) {
//			ds.setUnderlineText(false); // 去掉下划线
//		}
//	}
}
