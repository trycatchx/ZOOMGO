package com.dmsys.airdiskpro.setting;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dm.xunlei.udisk.Network.View.EditTextButtonView;
import com.dmairdisk.aodplayer.util.CommonAsync;
import com.dmsys.airdiskpro.event.DisconnectEvent;
import com.dmsys.airdiskpro.event.PasswordChangeEvent;
import com.dmsys.airdiskpro.ui.MainActivity;
import com.dmsys.dmsdk.DMSdk;
import com.dmsys.dmsdk.model.DMRet;
import com.dmsys.mainbusiness.R;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.greenrobot.event.EventBus;

public class PasswordModifyActivity extends Activity {

	private LinearLayout layout_first,layout_modify;
	
	private EditTextButtonView etbv_password1,etbv_password2;
	private EditTextButtonView etbv_password3,etbv_password4,etbv_password5;
	
	private RelativeLayout rl_errornote;
	private TextView tv_errornote;
	
	private Button btn_save;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_password);
		initViews();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
	}
	
	private void initViews() {
		// TODO Auto-generated method stub
		EventBus.getDefault().register(this);
		
		layout_first = (LinearLayout) findViewById(R.id.layout_first);
		layout_modify = (LinearLayout) findViewById(R.id.layout_modify);
		rl_errornote = (RelativeLayout) findViewById(R.id.rl_errornote);
		tv_errornote = (TextView) findViewById(R.id.tv_errornote);
		
		etbv_password1 = (EditTextButtonView) findViewById(R.id.etbv_password1);
		etbv_password1.setEditTextHint(getString(R.string.DM_Access_Password_Set_Enter));
		etbv_password2 = (EditTextButtonView) findViewById(R.id.etbv_password2);
		etbv_password2.setEditTextHint(getString(R.string.DM_Access_Password_Set_Enter_Again));
		etbv_password3 = (EditTextButtonView) findViewById(R.id.etbv_password3);
		etbv_password3.setEditTextHint(getString(R.string.DM_Access_Password_Reset_Enter));
		etbv_password4 = (EditTextButtonView) findViewById(R.id.etbv_password4);
		etbv_password4.setEditTextHint(getString(R.string.DM_Access_Password_Reset_Enter_New));
		etbv_password5 = (EditTextButtonView) findViewById(R.id.etbv_password5);
		etbv_password5.setEditTextHint(getString(R.string.DM_Access_Password_Reset_Enter_Again));
		etbv_password1.setStyle(EditTextButtonView.OptionalPasswordStyle);
		etbv_password2.setStyle(EditTextButtonView.OptionalPasswordStyle);
		etbv_password3.setStyle(EditTextButtonView.OptionalPasswordStyle);
		etbv_password4.setStyle(EditTextButtonView.OptionalPasswordStyle);
		etbv_password5.setStyle(EditTextButtonView.OptionalPasswordStyle);
		
		btn_save = (Button) findViewById(R.id.setting_title_save_button);
		
		Intent intent = getIntent();
		String state = intent.getStringExtra("pwd_state");
		if (state != null && state.equals(getString(R.string.DM_Setting_Access_Password_Set))) {
			layout_first.setVisibility(View.GONE);
			layout_modify.setVisibility(View.VISIBLE);
			btn_save.setText(R.string.DM_Access_Password_Reset_Confirm);
		}else {
			layout_first.setVisibility(View.VISIBLE);
			layout_modify.setVisibility(View.GONE);
		}
		
		((TextView)findViewById(R.id.setting_title_text)).setText(R.string.DM_Setting_Access_Password);;				
		((ImageView)findViewById(R.id.ibRefresh)).setVisibility(View.GONE);
		((ImageView)findViewById(R.id.setting_title_button)).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					finish();
				}
		});
		
		btn_save.setVisibility(View.VISIBLE);
		btn_save.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				savePassword();
			}
		});
		
	}


	protected void savePassword() {
		// TODO Auto-generated method stub
		
		if (btn_save.getText().equals(getString(R.string.DM_Access_Password_Reset_Confirm))) {
			
			if (etbv_password3.getContentText().length() < 8 || etbv_password4.getContentText().length() < 8  ||etbv_password5.getContentText().length() < 8  ) {
				tv_errornote.setText(R.string.DM_Error_PWD_Short);
				rl_errornote.setVisibility(View.VISIBLE);
			}else if(!etbv_password4.getContentText().equals(etbv_password5.getContentText()) ){
				tv_errornote.setText(R.string.DM_Access_Password_Reset_Not_Match);
				rl_errornote.setVisibility(View.VISIBLE);
			}else if (etbv_password3.getContentText().equals(etbv_password4.getContentText())) {
				tv_errornote.setText(R.string.DM_Access_Password_Reset_Repeat);
				rl_errornote.setVisibility(View.VISIBLE);
			}else {
				setPassword(1);
			}
			
			
		}else {
			
			if (etbv_password1.getContentText().length() < 8 || etbv_password2.getContentText().length() < 8 ) {
				tv_errornote.setText(R.string.DM_Error_PWD_Short);
				rl_errornote.setVisibility(View.VISIBLE);
			}else if (!etbv_password1.getContentText().equals(etbv_password2.getContentText())) {
				tv_errornote.setText(R.string.DM_Access_Password_Set_Not_Match);
				rl_errornote.setVisibility(View.VISIBLE);
			}else {
				setPassword(0);
			}
			
		}
	}

	private void setPassword(final int type){
		
		CommonAsync.Runnable mRunnable = new CommonAsync.Runnable() {
			
			@Override
			public void stop() {
				// TODO Auto-generated method stub
			}
			
			@Override
			public Object run() {
				// TODO Auto-generated method stub
				if (type == 0) {
					return DMSdk.getInstance().setPassword(etbv_password1.getContentText());
				}else {
					return DMSdk.getInstance().modifyPassword(etbv_password3.getContentText(), etbv_password4.getContentText());
				}
			}
		};
		
		CommonAsync.CommonAsyncListener mListener = new CommonAsync.CommonAsyncListener() {
			
			@Override
			public void onResult(Object ret) {
				// TODO Auto-generated method stub
				int result = (int) ret;
				if (result == DMRet.ACTION_SUCCESS) {
					
					if (type == 0) {
						Toast.makeText(PasswordModifyActivity.this, R.string.DM_Access_Password_Set_Success, Toast.LENGTH_SHORT).show();
					}else {
						Toast.makeText(PasswordModifyActivity.this, R.string.DM_Access_Password_Reset_Success, Toast.LENGTH_SHORT).show();
					}	
					
					startActivity(new Intent(PasswordModifyActivity.this,MainActivity.class));
					EventBus.getDefault().post(new PasswordChangeEvent());
					
					finish();
				}else {
					if (type == 0) {
						Toast.makeText(PasswordModifyActivity.this, R.string.DM_Access_Password_Set_Fail, Toast.LENGTH_SHORT).show();
					}else {
						if (result == 10222) {
							tv_errornote.setText(R.string.DM_Access_Password_Reset_Original_Wrong);
							rl_errornote.setVisibility(View.VISIBLE);
						}else {
							Toast.makeText(PasswordModifyActivity.this, R.string.DM_Access_Password_Reset_Fail, Toast.LENGTH_SHORT).show();
						}
					}
				}
				
			}
			
			@Override
			public void onError() {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onDestory() {
				// TODO Auto-generated method stub
			}
		};
	
		CommonAsync task = new CommonAsync(mRunnable, mListener);
		task.executeOnExecutor((ExecutorService) Executors.newCachedThreadPool());
	}
	
	public void onEventMainThread(DisconnectEvent event){
		finish();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}
	
}
