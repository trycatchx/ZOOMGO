package com.dmsys.airdiskpro.view;

import android.content.Context;
import android.graphics.Paint;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.dm.xunlei.udisk.Network.View.EditTextButtonView;
import com.dmsys.mainbusiness.R;

import java.lang.reflect.Field;


public class UDiskEditTextDialog extends UDiskBaseDialog {

	private boolean isLocked;
	
	public UDiskEditTextDialog(Context context,int type) {
		super(context);
		// TODO Auto-generated constructor stub
		super.initView(type, R.layout.dialog_edittext);
		this.setCancelable(true);
	}
	
	public EditTextButtonView getEditTextView()
	{
		return (EditTextButtonView)getCustomView().findViewById(R.id.et_content);		
	}
	
	public String getEditContent()
	{
		return ((EditTextButtonView)getCustomView().findViewById(R.id.et_content)).getContentText();
	}
	
	public void setEditContent(String text)
	{
		((EditTextButtonView)getCustomView().findViewById(R.id.et_content)).setContentText(text);
		((EditTextButtonView)getCustomView().findViewById(R.id.et_content)).getEditTextView().setSelection(text.length());
	}
	
	public void setToPassword()
	{
//		((EditTextButtonView)getCustomView().findViewById(R.id.et_content)).
//		setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		((EditTextButtonView)getCustomView().findViewById(R.id.et_content)).setStyle(EditTextButtonView.OptionalPasswordStyle);
	}
	
	public void showWarnText(int id)
	{
		((TextView)getCustomView().findViewById(R.id.text_warn)).setVisibility(View.VISIBLE);
		((TextView)getCustomView().findViewById(R.id.text_warn)).setText(id);
	}
	
	public void setWarnText(String warn)
	{
		((TextView)getCustomView().findViewById(R.id.text_warn)).setVisibility(View.VISIBLE);
		((TextView)getCustomView().findViewById(R.id.text_warn)).setText(warn);
	}
	
	public void hideWarnText(){
		((TextView)getCustomView().findViewById(R.id.text_warn)).setVisibility(View.GONE);
	}
	
	public void setTipText(String warn,View.OnClickListener l)
	{
		((TextView)getCustomView().findViewById(R.id.text_tip)).setVisibility(View.VISIBLE);
		((TextView)getCustomView().findViewById(R.id.text_tip)).setText(warn);
		((TextView)getCustomView().findViewById(R.id.text_tip)).getPaint().setFlags(Paint. UNDERLINE_TEXT_FLAG ); //下划线
		((TextView)getCustomView().findViewById(R.id.text_tip)).getPaint().setAntiAlias(true);//抗锯齿
		((TextView)getCustomView().findViewById(R.id.text_tip)).setOnClickListener(l);
	}
	
	public void hideTipText(){
		((TextView)getCustomView().findViewById(R.id.text_tip)).setVisibility(View.GONE);
	}
	
	
	public void lockDialog(){
		
		try { 
			Field field = getClass().getSuperclass().getSuperclass().getSuperclass().getDeclaredField("mShowing"); 
			field.setAccessible(true); 
			field.set(this, false);
			isLocked = true;
			System.out.println("ttttttttt");
		} catch (Exception e) { 
			e.printStackTrace(); 
		}
	}
	
	public void releaseDialog(){
		
		try {
			Field field = getClass().getSuperclass().getSuperclass().getSuperclass().getDeclaredField("mShowing");
			field.setAccessible(true);
			field.set(this, true);
			isLocked = false;
			System.out.println("rrrrrrr");
		 } catch (Exception e) {
			 e.printStackTrace();
		 }
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			
			if (isLocked) {
				releaseDialog();
			}
		}
		
		return super.onKeyDown(keyCode, event);
	}
	
	
	
}
