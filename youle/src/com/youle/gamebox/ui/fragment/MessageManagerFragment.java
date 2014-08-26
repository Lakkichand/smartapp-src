package com.youle.gamebox.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.YouleAplication;
import com.youle.gamebox.ui.listener.NewMsgCallBackListener;

import java.util.Map;

/**
 * Created by Administrator on 14-6-25.
 */
public class MessageManagerFragment extends BaseFragment implements
		ViewPager.OnPageChangeListener {
	@InjectView(R.id.messageTabLayout)
	LinearLayout mMessageTabLayout;
	@InjectView(R.id.messageViewPage)
	ViewPager mMessageViewPage;
	private int[] tabIcon = new int[] { R.drawable.message_sys_icon,
			R.drawable.message_event_icon, R.drawable.message_gift_icon,
			R.drawable.message_comunity_icon, R.drawable.message_private_icon };
	private MessageListFragment giftMessage;
	private MessageListFragment eventMessage;
	private MessageListFragment systeMessage;
	private MessageListFragment communityMessage;
	private MessageListFragment privateMessage;

	@Override
	protected int getViewId() {
		return R.layout.fragment_message_manager;
	}

    @Override
    protected String getModelName() {
        return "消息管理";
    }

    @Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initTabView();
		mMessageViewPage.setAdapter(new MessageViewPagerAdapter());
		mMessageViewPage.setOnPageChangeListener(this);
		mMessageViewPage.setCurrentItem(0);
	}

	private void initTabView() {
		String[] tabTitle = getResources().getStringArray(R.array.message_tab);
		LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		p.weight = 1;
		for (int i = 0; i < tabTitle.length; i++) {
			mMessageTabLayout.addView(getTabView(i), p);
		}
        TabView tabView = (TabView) mMessageTabLayout.getChildAt(0);
        tabView.setSelected(true);
	}

	private View getTabView(int position) {
		TabView tabView = new TabView(getActivity(), position);
		tabView.setOnClickListener(tabOnclickListener);
		return tabView;
	}

	private View.OnClickListener tabOnclickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			resetTab();
			TabView tabView = (TabView) v;
			tabView.setSelected(true);
			mMessageViewPage.setCurrentItem(tabView.index);
		}
	};

	/*************************
	 * 显示未读通知个数
	 * @param map
	 **************************/
	private void setNewMsgUI(Map<String, Integer> map) {
		for (int i = 0; i < mMessageTabLayout.getChildCount(); i++) {
			TabView tabView = (TabView) mMessageTabLayout.getChildAt(i);
			int newNum;
			switch (i) {
			case 0:
				newNum = map.get("5");
				if (newNum > 0) {
					tabView.tvNewMsg.setVisibility(View.VISIBLE);
				}else {
					tabView.tvNewMsg.setVisibility(View.GONE);
				}
				tabView.tvNewMsg.setText("" + newNum);
				break;

			case 1:
				newNum = map.get("7");
				if (newNum > 0) {
					tabView.tvNewMsg.setVisibility(View.VISIBLE);
				}else {
					tabView.tvNewMsg.setVisibility(View.GONE);
				}
				tabView.tvNewMsg.setText("" + newNum);
				break;
			case 2:
				newNum = map.get("2");
				//newNum = 3;
				if (newNum > 0) {
					tabView.tvNewMsg.setVisibility(View.VISIBLE);
				}else {
					tabView.tvNewMsg.setVisibility(View.GONE);
				}
				tabView.tvNewMsg.setText("" + newNum);
				break;
			case 3:
				newNum = map.get("3");
				if (newNum > 0) {
					tabView.tvNewMsg.setVisibility(View.VISIBLE);
				}else {
					tabView.tvNewMsg.setVisibility(View.GONE);
				}
				tabView.tvNewMsg.setText("" + newNum);
				break;
			case 4:
				newNum = map.get("4");
				if (newNum > 0) {
					tabView.tvNewMsg.setVisibility(View.VISIBLE);
				}else {
					tabView.tvNewMsg.setVisibility(View.GONE);
				}
				tabView.tvNewMsg.setText("" + newNum);
				break;

			default:
				break;
			}
		}
	}

	private void resetTab() {
		for (int i = 0; i < mMessageTabLayout.getChildCount(); i++) {
			TabView tabView = (TabView) mMessageTabLayout.getChildAt(i);
			tabView.setSelected(false);
		}
	}

	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {

	}

	@Override
	public void onPageSelected(int position) {
		resetTab();
		TabView tabView = (TabView) mMessageTabLayout.getChildAt(position);
		tabView.setSelected(true);
        if(position==0){
            systeMessage.loadData();
        }else if(position==1){
            eventMessage.loadData();
        }else if(position==2){
            giftMessage.loadData();
        }else if(position==3){
            communityMessage.loadData();
        }else {
            privateMessage.loadData();
        }
	}

	@Override
	public void onPageScrollStateChanged(int state) {

	}

	class TabView extends LinearLayout {
		int index;
		ImageView iconImage;
		TextView tabText;
		TextView tvNewMsg;
		RelativeLayout layout;

		public TabView(Context context, int index) {
			super(context);
			this.index = index;
			LayoutInflater.from(getActivity()).inflate(
					R.layout.message_tab_item, this);
			iconImage = (ImageView) findViewById(R.id.tabIcon);
			iconImage.setImageResource(tabIcon[index]);
			tabText = (TextView) findViewById(R.id.tabTitle);
			tabText.setText(getResources().getStringArray(R.array.message_tab)[index]);
			tvNewMsg = (TextView) findViewById(R.id.number);
			layout = (RelativeLayout) findViewById(R.id.all_relative);
		}

		public void setSelected(boolean select) {
			iconImage.setSelected(select);
			tabText.setSelected(select);
			layout.setSelected(select);
//            if(select) {
//                tvNewMsg.setVisibility(GONE);
//                if(!TextUtils.isEmpty(tvNewMsg.getText())){
//                    int num = Integer.parseInt((String) tvNewMsg.getText());
//                    if(YouleAplication.messageNumberBean!=null) {
//                        YouleAplication.messageNumberBean.setMsgCount(YouleAplication.messageNumberBean.getMsgCount() - num);
//                    }
//                }
//            }
		}
	}

    private  NewMsgCallBackListener listener = new NewMsgCallBackListener() {
        @Override
        public void callBack(Map<String, Integer> map) {
            setNewMsgUI(map);
        }
    } ;

	class MessageViewPagerAdapter extends FragmentPagerAdapter {
		public MessageViewPagerAdapter() {
			super(getFragmentManager());
		}

		@Override
		public Fragment getItem(int position) {
			if (position == 0) {
				if (systeMessage == null) {
					TabView tabView = (TabView) mMessageTabLayout.getChildAt(position);
					tabView.setSelected(true);
					
					systeMessage = new MessageListFragment(
							MessageListFragment.SYSTEM,listener);
				}
				return systeMessage;
			} else if (position == 1) {
				if (eventMessage == null) {
					eventMessage = new MessageListFragment(
							MessageListFragment.MESSAGE_RECOMMENT,listener);
				}
				return eventMessage;
			} else if (position == 2) {
				if (giftMessage == null) {
					giftMessage = new MessageListFragment(
							MessageListFragment.GIFT,listener);
				}
				return giftMessage;
			} else if (position == 3) {
				if (communityMessage == null) {
					communityMessage = new MessageListFragment(
							MessageListFragment.COMUNITY,listener);
				}
				return communityMessage;
			} else if (position == 4) {
				if (privateMessage == null) {
					privateMessage = new MessageListFragment(
							MessageListFragment.PRIVATE_MESSAGE);
				}
				return privateMessage;
			}
			return null;
		}

		@Override
		public int getCount() {
			return 5;
		}
	}
	
	@Override
	public void onLoadStart() {
	}
}
