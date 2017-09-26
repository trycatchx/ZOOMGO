package com.dmsys.airdiskpro.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dmsys.mainbusiness.R;


public class FileActionView extends RelativeLayout {
	public static final int ACTION_EDIT = 0;
	public static final int ACTION_SEL_ALL = 1;
	public static final int ACTION_SEL_NONE = 2;
	
	
	private STATE mState = STATE.NORMAL;

	public enum STATE {
		NORMAL,SEL_ALL,SEL_NONE,EDIT
	}
	
	private Context mContext;
	private OnClickListener mListener;
	private OnBackIconClickListener mIconClickListener;
	
	private boolean isLoading = false;
	private TextView editAction;
	private TextView tvTitleLocation;
	public interface OnClickListener {
		void OnClick(STATE actionId);
	}
	
	public interface OnBackIconClickListener {
		void onClick(View v);
	}
	
	
	
	
	public FileActionView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		mContext = context;
		initView();
	}
	
	public FileActionView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mContext = context;
		initView();
	}
	
	public FileActionView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		mContext = context;
		initView();
	}
	
	
	public void attachClickListener(OnClickListener listener) {
		mListener = listener;
	}
	
	public void reset() {
		setState(STATE.NORMAL);
	}
		
	
	public void showEditBtn(boolean show){
		if (show) {
			editAction.setVisibility(View.VISIBLE);
		}else {
			editAction.setVisibility(View.INVISIBLE);
		}
	}
	
	public void setEditBtnText(String text){
		editAction.setText(text);
	}
	
	public void setBackIconClickListener(OnBackIconClickListener listener) {
		mIconClickListener = listener;
	}
	
	public void setState(STATE state) {
		mState = state;
		
		switch (state) {
		case NORMAL:
			editAction.setText(R.string.DM_Control_Uncheck_All);
			break;
		case EDIT:
		case SEL_ALL:
			editAction.setText(R.string.DM_Control_Select);
			break;
		case SEL_NONE:
			editAction.setText(R.string.DM_Control_Uncheck_All);
			break;	
		default:
			break;
		}
	}
	
	private STATE getState() {
		return mState;
	}
	
	private void initView() {
		LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.file_action_view, this);
		editAction = (TextView) rootView.findViewById(R.id.action_edit);
		ImageView leftView = (ImageView) rootView.findViewById(R.id.titlebar_left);
		tvTitleLocation = (TextView) rootView.findViewById(R.id.tv_title_location);
		
		View.OnClickListener listener = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				STATE actionId = STATE.NORMAL;
				switch (getState()) {
					case NORMAL:
						setState(STATE.SEL_ALL);
						actionId = STATE.EDIT;
						break;
					case SEL_ALL:
						setState(STATE.SEL_NONE);
						actionId = STATE.SEL_ALL;
						break;
					case SEL_NONE:
						setState(STATE.SEL_ALL);
						actionId = STATE.SEL_NONE;
						break;
					}
					if (mListener != null) {
						mListener.OnClick(actionId);
					}
			}
		};
		
		editAction.setOnClickListener(listener);
		leftView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(mIconClickListener != null) {
					mIconClickListener.onClick(v);
				}
			}
		});
	}
	
	public void hideTitle() {
		if(tvTitleLocation != null) {
			tvTitleLocation.setVisibility(View.GONE);
		}
	}
	public void setTitleText(String text)
	{
		if(tvTitleLocation!=null)
		{
			tvTitleLocation.setText(text);
		}
	}
}
