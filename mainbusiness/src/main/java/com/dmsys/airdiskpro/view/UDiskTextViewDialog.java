package com.dmsys.airdiskpro.view;

import android.content.Context;
import android.widget.TextView;

import com.dmsys.mainbusiness.R;



public class UDiskTextViewDialog extends UDiskBaseDialog {

	
	public UDiskTextViewDialog(Context context,int type) {
		super(context);
		// TODO Auto-generated constructor stub
		super.initView(type, R.layout.dialog_textview);
		this.setCancelable(true);
	}
	
	public TextView getTextView()
	{
		return (TextView)getCustomView().findViewById(R.id.tv_content);		
	}
	
	public String getContent()
	{
		return ((TextView)getCustomView().findViewById(R.id.tv_content)).getText().toString();
	}
	
	public void setContent(String text)
	{
		((TextView)getCustomView().findViewById(R.id.tv_content)).setText(text);
	}





	
	
}
