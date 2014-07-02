package com.emoji;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.ImageView.ScaleType;
import com.youle.gamebox.ui.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 
 * 表情主控件
 * 
 * @author Zhengcw
 * 
 *         2013-09-16
 * 
 */
public class EmojiView extends RelativeLayout {
	private ViewPager mViewPager;
	private CircleView mCircleView;
	private GridView mGridView;
	private Context mContext;
	private int msgType = 0;
	//private CallBack mCallBack;
	private ArrayList<EmojiInfo> emojiData;
	private int tabIndex = 0;
	private static final int VIEW_PAGER_HEIGTH_INCLUD_MARGIN = 240;
	private static final int VIEW_PAGER_HEIGTH = 170;
	private static final int TAB_VIEW_HEIGTH = 30;
	private static final int TAB_VIEW_WIDTH = 45;
	private static final int SLIDING_VIEW_HEIGTH = 20;
	
	private EditText targetEdit;//目标要被操作的View
	private int MAX_COUNT = 140; 

	public EmojiView(Context context) {
		super(context);
		init(context);
	}

	public EmojiView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public EmojiView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		this.mContext = context;
		View layoutView = LayoutInflater.from(context).inflate(R.layout.emoji_view_layout, this);
		mViewPager = (ViewPager) layoutView.findViewById(R.id.emiji_view_pager);
		mViewPager.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, VIEW_PAGER_HEIGTH_INCLUD_MARGIN));
		mCircleView = (CircleView) layoutView.findViewById(R.id.emoji_sliding_view);
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, SLIDING_VIEW_HEIGTH);
		params.addRule(CENTER_IN_PARENT);
		params.addRule(BELOW, mViewPager.getId());
		mCircleView.setLayoutParams(params);
		mGridView = (GridView) layoutView.findViewById(R.id.emoji_choose_grid_view);
	}
	
	/**
	 * 初始化默认的表情库
	 * @param context
	 */
	public void initDefaultEmojiDate(Context context){
		ArrayList<EmojiInfo> list = new ArrayList<EmojiInfo>();
		EmojiInfo faceEmojiInfo = new EmojiInfo();
		faceEmojiInfo.setEmojiColumns(7);
		faceEmojiInfo.setEmojiData(EmojiData.getInstance(context).getEmojiData());
		faceEmojiInfo.setEmojiLines(4);
		faceEmojiInfo.setEmojiType(1);
		faceEmojiInfo.setTabIconId(R.drawable.emoji_icon);
		list.add(faceEmojiInfo);
		setEmojiData(list);
	}

	/**
	 * 设置控件的原始数据
	 * 
	 */
	public void setEmojiData(ArrayList<EmojiInfo> emojiDataList) {
		this.emojiData = emojiDataList;
		if (emojiData != null && emojiData.size() > 0) {
			int chooseTabSize = emojiData.size();
			mGridView.setLayoutParams(new LinearLayout.LayoutParams(TAB_VIEW_WIDTH * 2 * chooseTabSize,
					LinearLayout.LayoutParams.WRAP_CONTENT));
			mGridView.setNumColumns(chooseTabSize);
			mGridView.setAdapter(new ChooseTabAdapter(mContext, emojiData));
			EmojiInfo info = emojiData.get(0);
			msgType = info.getEmojiType();
			initData(info.getEmojiData(), info.getEmojiColumns(), info.getEmojiLines());
		}
	}
	
	public void setTargetEdit(EditText edit){
		this.targetEdit = edit;
//		this.targetEdit.addTextChangedListener(new TextWatcher() {
//			public void onTextChanged(CharSequence s, int start, int before, int count) {
//				//Toast.makeText(mContext, s.subSequence(start,start + count), Toast.LENGTH_LONG).show();
//				CharSequence changedText = s.subSequence(start, start + count);
//				//targetEdit.getEditableText().replace(1, 4, "[我进来了]");
//				//targetEdit.getEditableText().delete(1, 4);
//			}
//			
//			public void beforeTextChanged(CharSequence s, int start, int count,
//					int after) {
//				//方法本为空
//			}
//			public void afterTextChanged(Editable s) {
//				//方法本为空
//			}
//		});
	}
	
	/**
     * 设置可输入的最大字数
     * @param mAX_COUNT
     */
	public void setTargetEditMaxCount(int mAX_COUNT) {
		MAX_COUNT = mAX_COUNT;
		targetEdit.addTextChangedListener(mTextWatcher);
	}

	/**
	 * 
	 * 显示对应的表情类型
	 * 
	 * 静态表情
	 * 
	 */
	private void initData(ArrayList<HashMap<String, Object>> data, int columns, int line) {
		List<GridView> images = new ArrayList<GridView>();
		int itemtCount = columns * line;
		int emojiCount = itemtCount - 1;
		int size = (data.size() / emojiCount) + (data.size() % emojiCount > 0 ? 1 : 0);
		mCircleView.initCircle(size);
		for (int i = 0; i < size; i++) {
			GridView gridView = new GridView(mContext);
			gridView.setNumColumns(columns);
			gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
			ArrayList<HashMap<String, Object>> tempData = new ArrayList<HashMap<String, Object>>();
			tempData.clear();
			if (i < size - 1) {
				tempData = new ArrayList<HashMap<String, Object>>();
				for (int j = i * emojiCount; j < i * emojiCount + emojiCount; j++) {
					tempData.add(data.get(j));
				}
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("imageItem", R.drawable.emoji_delete);
				map.put("textItem", "删除");
				tempData.add(map);
			} else {
				tempData = new ArrayList<HashMap<String, Object>>();
				int result = data.size() % emojiCount;
				int count;
				if (result != 0) {
					count = data.size() + emojiCount - result;
					for (int j = i * emojiCount; j < count; j++) {
						if (j < data.size()) {
							tempData.add(data.get(j));
						} else {
							tempData.add(null);
						}
					}
				} else {
					count = data.size();
					for (int j = i * emojiCount; j < count; j++) {
						tempData.add(data.get(j));
					}
				}

				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("imageItem", R.drawable.emoji_delete);
				map.put("textItem", "删除");
				tempData.add(map);
			}
			EmojiAdapter adapter = new EmojiAdapter(mContext, tempData, line, columns);
			gridView.setAdapter(adapter);
			images.add(gridView);
		}
		mCircleView.choosecircle(0);
		mViewPager.setAdapter(new ViewPAgerAdapter(mContext, images));

		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				mCircleView.choosecircle(position);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});
	}

	/**
	 * 
	 * 显示对应的表情类型
	 * 
	 * 动态表情
	 * 
	 */
	private void initDataForAnimation(ArrayList<HashMap<String, Object>> data, int columns, int line) {
		List<GridView> images = new ArrayList<GridView>();
		int itemtCount = columns * line;
		int size = (data.size() / itemtCount) + (data.size() % itemtCount > 0 ? 1 : 0);
		mCircleView.initCircle(size);
		for (int i = 0; i < size; i++) {
			GridView gridView = new GridView(mContext);
			gridView.setNumColumns(columns);
			gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
			ArrayList<HashMap<String, Object>> tempData = new ArrayList<HashMap<String, Object>>();
			tempData.clear();
			if (i < size - 1) {
				tempData = new ArrayList<HashMap<String, Object>>();
				for (int j = i * itemtCount; j < i * itemtCount + itemtCount; j++) {
					tempData.add(data.get(j));
				}
			} else {
				tempData = new ArrayList<HashMap<String, Object>>();
				for (int j = i * itemtCount; j < data.size(); j++) {
					tempData.add(data.get(j));
				}
			}
			EmojiAdapter adapter = new EmojiAdapter(mContext, tempData, line, columns);
			gridView.setAdapter(adapter);
			images.add(gridView);
		}
		mCircleView.choosecircle(0);
		mViewPager.setAdapter(new ViewPAgerAdapter(mContext, images));

		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				mCircleView.choosecircle(position);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});
	}

	/**
	 * 表情容器 adapter
	 * 
	 * @author Zhengcw
	 * 
	 */
	private class ViewPAgerAdapter extends PagerAdapter {

		private Context mContext;
		private List<GridView> mGridViewList;

		public ViewPAgerAdapter(Context context, List<GridView> list) {
			this.mContext = context;
			this.mGridViewList = list;
		}

		@Override
		public int getCount() {
			return mGridViewList.size();
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(mGridViewList.get(position));
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			GridView mGridView = mGridViewList.get(position);
			container.addView(mGridView);
			return mGridView;
		}

	}

	/**
	 * 表情类型选择栏 adapter
	 * 
	 * @author Zhengcw
	 * 
	 */
	private class ChooseTabAdapter extends BaseAdapter {
		private Context mContext;
		private ArrayList<EmojiInfo> list;

		public ChooseTabAdapter(Context context, ArrayList<EmojiInfo> emojiList) {
			this.mContext = context;
			this.list = emojiList;
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		public void notifyAdapter() {
			notifyDataSetChanged();
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ImageView imageView;
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.emoji_custom_item, null);
				imageView = (ImageView) convertView.findViewById(R.id.facedialog_ItemImage);
				LayoutParams params = new LayoutParams(TAB_VIEW_WIDTH, TAB_VIEW_HEIGTH);
				params.addRule(CENTER_IN_PARENT);
				params.topMargin = 25;
				params.bottomMargin = 25;
				imageView.setScaleType(ScaleType.CENTER_INSIDE);
				imageView.setLayoutParams(params);

				convertView.setTag(imageView);
			} else {
				imageView = (ImageView) convertView.getTag();
			}

			if (tabIndex == position) {
				convertView.setBackgroundResource(R.drawable.emoji_choose_tab_pressed_bg);
			} else {
				convertView.setBackgroundResource(R.drawable.emoji_choose_selector);
			}
			int id = (Integer) list.get(position).getTabIconId();
			imageView.setImageResource(id);

			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					tabIndex = position;
					EmojiInfo info = list.get(position);
					msgType = info.getEmojiType();
					notifyAdapter();
					if (msgType == 1) {
						initData(info.getEmojiData(), info.getEmojiColumns(), info.getEmojiLines());
					} else {
						initDataForAnimation(info.getEmojiData(), info.getEmojiColumns(), info.getEmojiLines());
					}

				}
			});
			return convertView;
		}
	}

	/**
	 * 
	 * 表情 adapter
	 * 
	 */
	private class EmojiAdapter extends BaseAdapter {
		private Context mContext;
		private ArrayList<HashMap<String, Object>> mList;
		private int itemlines;
		private int itemcolumns;
		private int itemWidth;
		private int margin;
		private int count;

		public EmojiAdapter(Context context, ArrayList<HashMap<String, Object>> data, int lines, int columns) {
			this.mContext = context;
			this.mList = data;
			this.itemlines = lines;
			this.itemcolumns = columns;
			itemWidth = VIEW_PAGER_HEIGTH / itemlines;
			margin = (VIEW_PAGER_HEIGTH_INCLUD_MARGIN - VIEW_PAGER_HEIGTH) / lines;
			count = itemcolumns * itemlines - 1;
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

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ImageView imageView;
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.emoji_custom_item, null);
				imageView = (ImageView) convertView.findViewById(R.id.facedialog_ItemImage);
				LayoutParams params = new LayoutParams(itemWidth, itemWidth);
				params.addRule(CENTER_IN_PARENT);
				params.topMargin = margin;
				params.leftMargin = margin / 2;
				params.rightMargin = margin / 2;
				imageView.setScaleType(ScaleType.CENTER_INSIDE);
				imageView.setLayoutParams(params);

				convertView.setTag(imageView);
			} else {
				imageView = (ImageView) convertView.getTag();
			}

			HashMap<String, Object> map = mList.get(position);
			if (map != null) {
				int id = (Integer) map.get("imageItem");
				imageView.setImageResource(id);
				convertView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						//if (mCallBack != null) {
							if (msgType != 1) {
								getEmojiContext(msgType, mList.get(position).get("textItem").toString(),(Integer) mList.get(position).get("imageItem"));
							} else {
								if (position > 0 && position % count == 0) {
									//动作按下
								    int action = KeyEvent.ACTION_DOWN;
								    //code:删除，其他code也可以，例如 code = 0
								    int code = KeyEvent.KEYCODE_DEL;
								    KeyEvent event = new KeyEvent(action, code);
								    targetEdit.onKeyDown(KeyEvent.KEYCODE_DEL, event); //抛给系统处理了
								} else {
									getEmojiContext(msgType, mList.get(position).get("textItem").toString(),(Integer) mList.get(position).get("imageItem"));
								}
							}
						//}
					}
				});
			} else {
				imageView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
			}

			return convertView;
		}
	}
	
	public void getEmojiContext(int MsgType, String MsgContext, final int sourceId) {
		if (MsgType == 1) {
			int index = targetEdit.getSelectionStart();//获取光标所在位置  
	        Editable edit = targetEdit.getEditableText();//获取EditText的文字  

			Drawable drawable = getResources().getDrawable(sourceId);   
	        drawable.setBounds(0, 0, 40, 40);   
	        //需要处理的文本，[smile]是需要被替代的文本   
	        SpannableString spannable = new SpannableString(MsgContext);   
	        //要让图片替代指定的文字就要用ImageSpan   
	        ImageSpan span = new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE);   
	        //开始替换，注意第2和第3个参数表示从哪里开始替换到哪里替换结束（start和end）   
	        //最后一个参数类似数学中的集合,[5,12)表示从5到12，包括5但不包括12
	
	        spannable.setSpan(span, 0, MsgContext.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);     
			
	        
	        if (index < 0 || index >= edit.length() ){  
	              edit.append(spannable);  
	        }else{  
	              edit.insert(index,spannable);//光标所在位置插入文字 
	        }
	        
			//targetEdit.setSelection(index + MsgContext.length());
		} 
	}
	
	/*************
	 * 
	 * 字数统计开始
	 * 
	 */
	
	private TextWatcher mTextWatcher = new TextWatcher() {  
		  
        private int editStart;  
  
        private int editEnd;  
  
        public void afterTextChanged(Editable s) {  
            editStart = targetEdit.getSelectionStart();  
            editEnd = targetEdit.getSelectionEnd();  
            // 先去掉监听器，否则会出现栈溢出  
            targetEdit.removeTextChangedListener(mTextWatcher);  
  
            // 注意这里只能每次都对整个EditText的内容求长度，不能对删除的单个字符求长度  
            // 因为是中英文混合，单个字符而言，calculateLength函数都会返回1  
            while (calculateLength(s.toString()) > MAX_COUNT) { // 当输入字符个数超过限制的大小时，进行截断操作  
            	s.delete(editStart - 1, editEnd);  
                editStart--;  
                editEnd--;  
            }  
            // mEditText.setText(s);将这行代码注释掉就不会出现后面所说的输入法在数字界面自动跳转回主界面的问题了，多谢@ainiyidiandian的提醒  
            targetEdit.setSelection(editStart);  
  
            // 恢复监听器  
            targetEdit.addTextChangedListener(mTextWatcher);  
  
            setLeftCount();  
        }  
  
        public void beforeTextChanged(CharSequence s, int start, int count,  
                int after) {  
  
        }  
  
        public void onTextChanged(CharSequence s, int start, int before,  
                int count) {  
  
        }  
  
    };  
  
    /** 
     * 计算分享内容的字数，一个汉字=两个英文字母，一个中文标点=两个英文标点 
     * 注意：该函数的不适用于对单个字符进行计算，因为单个字符四舍五入后都是1 
     *  
     * @param c 
     * @return 
     */  
    private long calculateLength(CharSequence c) {  
        double len = 0;  
        double tmpLen = 0;
        boolean startRecord = false;
        StringBuffer faceStr = new StringBuffer();
        //再计算其字母跟中文
        for (int i = 0; i < c.length(); i++) {  
            int tmp = (int) c.charAt(i);  
            if (tmp > 0 && tmp < 127) {  
                len += 0.5;
            } else {  
                len++;
            } 
            //开始记录
            if(tmp == 91){
            	startRecord = true;
            }
            if(startRecord){
            	if (tmp > 0 && tmp < 127) {  
                    tmpLen += 0.5;
                } else {  
                    tmpLen++;
                } 
            	faceStr.append(c.charAt(i));
            }
            //关闭记录
            if(tmp == 93){
            	//对比是否是表情
            	if(EmojiData.getInstance(getContext())
            			.isExit(faceStr.toString())){
            		len = len - tmpLen;
            		len = len + 1;
            	}
            	startRecord = false;
            	tmpLen = 0;
            	faceStr = new StringBuffer("");
            }
        } 
        
        return Math.round(len);  
    }  
    
    /** 
     * 刷新剩余输入字数,最大值新浪微博是140个字，人人网是200个字 
     */  
    private void setLeftCount() {  
    	//不显示字数统计
        //targetEdit.setText(String.valueOf((MAX_COUNT - getInputCount())));  
    }  
  
    /** 
     * 获取用户输入的分享内容字数 
     *  
     * @return 
     */  
    private long getInputCount() {  
        return calculateLength(targetEdit.getText().toString());  
    }  
	
	

//	public void setListener(CallBack callBack) {
//		this.mCallBack = callBack;
//	}

	/**
	 * 
	 * 选择表情后的回调函数
	 * 
	 * @author Administrator
	 * 
	 */
//	public interface CallBack {
//		public void getEmojiContext(int MsgType, String MsgContext,int sourceId);
//
//		public void deleteFuntion();
//	}
}