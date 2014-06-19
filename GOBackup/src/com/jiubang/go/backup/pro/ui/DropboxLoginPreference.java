package com.jiubang.go.backup.pro.ui;

import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.net.sync.FileHostingServiceProvider;

/**
 * 自定义Dropbox Preference
 *
 * @author ReyZhang
 */
public class DropboxLoginPreference extends Preference {

	private static final String DROPBOX_ACCOUNT_PREFS = "dropbox_account_prefs";
	private static final String PREF_ACCOUNT_NAME = "pref_account_name";
	private Button mLogoutBtn = null;
	private TextView mSummary = null;
	private TextView mTitle = null;
	private OnClickListener mOnButtonClickListener = null;

	public DropboxLoginPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setLayoutResource(R.layout.layout_preference_with_single_button);
	}

	public DropboxLoginPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setLayoutResource(R.layout.layout_preference_with_single_button);
	}

	public DropboxLoginPreference(Context context) {
		super(context);
		setLayoutResource(R.layout.layout_preference_with_single_button);
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);

		mLogoutBtn = (Button) view.findViewById(R.id.button);
		mSummary = (TextView) view.findViewById(R.id.summary);
		mTitle = (TextView) view.findViewById(R.id.title);

		mSummary.setVisibility(isEnabled() ? View.VISIBLE : View.GONE);

		// 获取sharedpreference值
		Map<String, ?> accounts = scanAccount(getContext(),
				FileHostingServiceProvider.DROPBOX);
		if (accounts == null || accounts.isEmpty()) {
			String summary = getContext().getString(R.string.entry_summary_unlogin_dropbox);
			mSummary.setText(summary);
			mLogoutBtn.setVisibility(View.GONE);
			return;
		}

		String name = null;
		Set<String> keys = accounts.keySet();
		for (String string : keys) {
			if (string.startsWith(PREF_ACCOUNT_NAME)) {
				name = accounts.get(string).toString();
				break;
			}
		}
		if (!TextUtils.isEmpty(name)) {
			mSummary.setText(name);
		}

		mLogoutBtn.setVisibility(View.VISIBLE);
		mLogoutBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mOnButtonClickListener != null) {
					mOnButtonClickListener.onClick(mLogoutBtn);
				}
			}
		});
	}

	@Override
	protected View onCreateView(ViewGroup parent) {
		return LayoutInflater.from(getContext()).inflate(
				R.layout.layout_preference_with_single_button, parent, false);
	}

	private Map<String, ?> scanAccount(Context context, int flag) {
		if (context == null) {
			return null;
		}
		Map<String, ?> accountMap = null;
		SharedPreferences prefs = null;

		if (flag == FileHostingServiceProvider.DROPBOX) {
			prefs = context.getSharedPreferences(DROPBOX_ACCOUNT_PREFS, Context.MODE_PRIVATE);
			accountMap = prefs.getAll();
			return accountMap;
		}
		return null;
	}

	public void setOnButtonClickListener(OnClickListener listener) {
		mOnButtonClickListener = listener;
	}
}
