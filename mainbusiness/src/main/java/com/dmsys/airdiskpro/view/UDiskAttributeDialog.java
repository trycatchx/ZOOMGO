package com.dmsys.airdiskpro.view;

import android.content.ClipboardManager;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dmsys.mainbusiness.R;


public class UDiskAttributeDialog extends UDiskBaseDialog {

	public UDiskAttributeDialog(final Context context,int type) {
		super(context);
		// TODO Auto-generated constructor stub
		super.initView(type, R.layout.dialog_attribute);
		this.setCancelable(true);
		((TextView)getCustomView().findViewById(R.id.tv_attr_copy)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ClipboardManager clip = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
				clip.setText(getPathTextView().getText());
			}

		});;
	}
	
	public ImageView getTypeImageView()
	{
		return (ImageView)getCustomView().findViewById(R.id.iv_dialog_attribute_icon);		
	}
	
	public TextView getNameTextView()
	{
		return (TextView)getCustomView().findViewById(R.id.tv_dialog_attribute_name);		
	}
	
	public TextView getTypeTextView()
	{
		return (TextView)getCustomView().findViewById(R.id.tv_dialog_attribute_type);		
	}
	
	public TextView getPathTextView()
	{
		return (TextView)getCustomView().findViewById(R.id.tv_dialog_attribute_path);		
	}
	
	public TextView getSizeTextView()
	{
		return (TextView)getCustomView().findViewById(R.id.tv_dialog_attribute_size);		
	}
	
	public TextView getContainTextView()
	{
		return (TextView)getCustomView().findViewById(R.id.tv_dialog_attribute_contain);		
	}
	
	public TextView getLastModifyTextView()
	{
		return (TextView)getCustomView().findViewById(R.id.tv_dialog_attribute_lastmodify_time);		
	}
	
	public TextView getContainRelativeLayout()
	{
		return (TextView)getCustomView().findViewById(R.id.tv_dialog_attribute_contain);		
	}
	
	public RelativeLayout getPathLayout(){
		return (RelativeLayout) getCustomView().findViewById(R.id.layout_path);
	}
	
	public RelativeLayout getModifyLayout(){
		return (RelativeLayout) getCustomView().findViewById(R.id.layout_modify);
	}
	
	
}
