package com.dmsys.airdiskpro.view;

import android.content.Context;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dmsys.mainbusiness.R;


public class FolderSelector extends LinearLayout {
	private static final String TAG = FolderSelector.class.getSimpleName();
	private int mWidth = 0;
	private String mfolderName[];
	private TextView mTextViewForWidth;
	final static private int mIconWidth = 25;
	final static private int mFontSize = 14;
	final static private int mMargin = 24;
	
	private int mThreeDotWidth=0;
	
	private OnClickListener mListener;
	private ItemOnClickListener mItemListener = null;

	public FolderSelector(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public FolderSelector(final Context context, AttributeSet attrs) {
	    super(context, attrs); 

        LayoutInflater inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE); 

        inflater.inflate(R.layout.folder_selector, this); 
        mTextViewForWidth = new TextView(this.getContext());
        mTextViewForWidth.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
        mTextViewForWidth.setTextSize(mFontSize);
        
        
        mThreeDotWidth = 10+getWidthByText("...");
        
        mListener = new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int idx = (Integer) v.getTag();

				if (mItemListener != null)
				{
					mItemListener.onClick(idx);
				}
			}
        	
        };
	}
	
	public  void setFoder(String name, String[] array)
	{
		mWidth = getWidth();
		getHeight();
		mfolderName = array;
		
		removeAllViews();
		
		TextView myTextView = createItem(name,R.drawable.path_split_big, -1);
		myTextView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT));
		
		if (mfolderName != null)
		{
			mfolderName = array;
			mWidth -= getWidthByText(name);
			CreateView();
		}
			
	}
	
	public interface ItemOnClickListener{
		public void onClick(int idx);
	}
	public void setOnClickListener(ItemOnClickListener l)
	{
		mItemListener = l;
	}
	
	private int getTextWidth(int itemWidth)
	{
		return itemWidth - mIconWidth;
	}
	private int getWidthByText(String text)
	{
		mTextViewForWidth.setText(text);
		mTextViewForWidth.setSingleLine(true);
		mTextViewForWidth.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT));
		int width;
		if(text != null)
			width = (int)mTextViewForWidth.getPaint().measureText(text);
		else
			width = 0;
		return width+mIconWidth + mMargin+mMargin;	//ra:here is nullpoint text
	}
	
	private TextView CreateTextItem(String text, int tag)
	{
		LinearLayout linear = new LinearLayout(this.getContext());   
		linear.setOrientation(LinearLayout.VERTICAL);  
		LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT);
		
		linear.setLayoutParams(lp);
		
		TextView myTextView = new TextView(this.getContext());
		myTextView.setText(text);
		myTextView.setGravity(Gravity.CENTER);
		myTextView.setSingleLine(true);
		myTextView.setTextSize(mFontSize);
		myTextView.setPadding(mMargin, 0, mMargin, 0);
		linear.setClickable(true);
		
		linear.addView(myTextView);
		linear.setOnClickListener(mListener);
		linear.setTag(tag);
		
		this.addView(linear);
		
		return myTextView;
		
	}
	private TextView createItem(String text, int iconId, int tag)
	{
		TextView myTextView = CreateTextItem(text, tag);

		LinearLayout linear = new LinearLayout(this.getContext());   
		linear.setOrientation(LinearLayout.VERTICAL);  
		LayoutParams lp = new LayoutParams(mIconWidth,LayoutParams.WRAP_CONTENT);
		lp.gravity = Gravity.CENTER; 
		linear.setLayoutParams(lp);
		 
		ImageView image = new ImageView(linear.getContext());
		image.setImageResource(iconId);
		image.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		image.setScaleType(ScaleType.FIT_CENTER);
		
		linear.addView(image);
		
		//this.addView(myTextView);
		this.addView(linear);	
		
		return myTextView;
	}
	
	public void CreateView()
	{
		int totalSize = 0;
		
		int type = 0; //能完整显示
		
		for(int i=mfolderName.length-1; i>=0; --i)
		{
			
			int textSize = getWidthByText(mfolderName[i]);
			int size = totalSize + textSize;
			if ( size > mWidth)
			{
				if (mWidth-totalSize  > mThreeDotWidth && i == 0)   
				{
					type = 1; //不能完整显示，但没有三个点。
					
					TextView myTextView = createItem(mfolderName[0],R.drawable.path_split_small, 0);
					myTextView.setLayoutParams(new LayoutParams(getTextWidth(mWidth-totalSize),LayoutParams.MATCH_PARENT));
					myTextView.setEllipsize(TruncateAt.START); 
					
				}
				else
				{
					type = 2;  //不能完整显示，需要有三个点。
				}
				break;
				
			}
			totalSize = size;
		}
		
		if (type==0)
		{
			for(int i=0; i<mfolderName.length; ++i)
			{
				TextView myTextView = createItem(mfolderName[i],R.drawable.path_split_small, i);
				myTextView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT));
				
				myTextView.setTag(i);
				
			}
		}
		else if (type == 1)
		{
			
			TextView myTextView;
			for(int i=1; i<mfolderName.length; ++i)
			{
				myTextView = createItem(mfolderName[i],R.drawable.path_split_small, i);
				myTextView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT));
			}
			
		} else {
			
			TextView myTextView;
			int threeDotTag = 0;
			int width = mWidth - mThreeDotWidth;
			int index = mfolderName.length-1;
			
			int allSize = 0;
			int newWidth = 0;
			for( ; index>=0; --index)
			{
				
				int textSize = getWidthByText(mfolderName[index]);
				int size = allSize +  textSize;
				
				if ( size > width)
				{
					newWidth = width-allSize;	
					break;
				}
				allSize = size;		
			}
			
			if (newWidth >= mThreeDotWidth)
			{
				threeDotTag = -1;	
			}
			
			threeDotTag += index;
			
			TextView threeDotTextView = createItem("...",R.drawable.path_split_small, threeDotTag);
			threeDotTextView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT));
			
			if (newWidth >= mThreeDotWidth)
			{
				myTextView = createItem(mfolderName[index],R.drawable.path_split_small, index);
				myTextView.setLayoutParams(new LayoutParams(getTextWidth(newWidth),LayoutParams.MATCH_PARENT));				
				myTextView.setEllipsize(TruncateAt.START); 	
			}
			
			for (int i= index+1; i<mfolderName.length; ++i)
			{
				myTextView = createItem(mfolderName[i],R.drawable.path_split_small, i);
				myTextView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT));
			}
				
		}

	}
	

}
