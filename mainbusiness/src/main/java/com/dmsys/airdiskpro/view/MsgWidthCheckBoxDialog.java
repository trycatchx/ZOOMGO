package com.dmsys.airdiskpro.view;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.dmsys.mainbusiness.R;

public class MsgWidthCheckBoxDialog extends UDiskBaseDialog {
	public MsgWidthCheckBoxDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		super.initView(TYPE_TWO_BTN, R.layout.dialog_msg_width_checkbox);
		this.setCancelable(false);
	}
	
	public MsgWidthCheckBoxDialog(Context context,int type) {
		super(context);
		// TODO Auto-generated constructor stub
		super.initView(type, R.layout.dialog_msg_width_checkbox);
		this.setCancelable(true);
	}
	
	public void setMessage(CharSequence text) {
		View customView = this.getCustomView();
		TextView msgView = (TextView) customView.findViewById(R.id.dialog_msg);
		msgView.setText(text);
	}
	
	public boolean getChecked() {
		View customView = this.getCustomView();
		CheckBox checkBox = (CheckBox) customView.findViewById(R.id.dialog_check);
		return checkBox.isChecked();
	}
	
	public void setChecked(boolean isChecked) {
		View customView = this.getCustomView();
		CheckBox checkBox = (CheckBox) customView.findViewById(R.id.dialog_check);
		checkBox.setChecked(isChecked);
	}
	
	public void setCheckText(CharSequence text) {
		View customView = this.getCustomView();
		CheckBox checkBox = (CheckBox) customView.findViewById(R.id.dialog_check);
		checkBox.setText(text);
	}
}
