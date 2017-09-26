package com.dmsys.airdiskpro.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.dmsys.airdiskpro.event.DisconnectEvent;
import com.dmsys.airdiskpro.filemanager.EditState;
import com.dmsys.airdiskpro.filemanager.FileOperationService;
import com.dmsys.airdiskpro.ui.MainFragment.OnEditableChange;
import com.dmsys.dmsdk.model.DMFile;
import com.umeng.analytics.MobclickAgent;
import com.dmsys.mainbusiness.R;

import de.greenrobot.event.EventBus;


public class PathSelectActivity extends FragmentActivity {

	private MainFragment mainFragment;
	private TextView btn_ok;
	private String errorPath;
	private String btnString;
	
	public int op = -1;
	public static final String EXTRA_OP = "OP";
	public static final String BTN_STRING = "BTN_STRING";
	
	public final static int COPYTO = 0;
	public final static int DECRYPTEDTO = 1;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pathselect);
		
		op = getIntent().getIntExtra(EXTRA_OP,-1);
		
		btn_ok = (TextView) findViewById(R.id.btn_ok);
		switch (op) {
		case COPYTO:
			btn_ok.setText(R.string.DM_Bottom_Bar_Button_Copy);
			break;
		case DECRYPTEDTO:
			btn_ok.setText(R.string.DM_Bottom_Bar_Button_Decrypt);
			break;
		default:
			btn_ok.setText(R.string.DM_Control_Definite);
			break;
		}
		
		
		btn_ok.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				
				switch (op) {
				case COPYTO:
					if (checkPathLegal()) {
						mainFragment.doFileOperation(FileOperationService.FILE_OP_COPYTO,FileOperationService.selectedList,mainFragment.getCurrentPath());
					}else {
						String content = String.format(getString(R.string.DM_Remind_CopyTo_Error), errorPath);
						Toast.makeText(PathSelectActivity.this, content, Toast.LENGTH_SHORT).show();
					}
					break;
				case DECRYPTEDTO:
					if (checkPathLegal()) {
						mainFragment.doFileOperation(FileOperationService.FILE_OP_DECRYPTED,FileOperationService.selectedList,mainFragment.getCurrentPath());
					}else {
						String content = String.format(getString(R.string.DM_Remind_CopyTo_Error), errorPath);
						Toast.makeText(PathSelectActivity.this, content, Toast.LENGTH_SHORT).show();
					}
					break;
				default:
					Intent intent=new Intent();  
					intent.putExtra("DES_PATH", mainFragment.getCurrentPath());  
					setResult(RESULT_OK, intent);  
					finish();  
					break;
				}
				
					
				
			}
		});
		
		setDefaultFragment(); 
		EventBus.getDefault().register(this);
	}
	
	private boolean checkPathLegal(){
		
		if (FileOperationService.selectedList != null) {
			System.out.println("not null");
		}else {
			System.out.println("size:"+FileOperationService.selectedList.size());
		}
		
		for (DMFile file : FileOperationService.selectedList) {
			String cur = mainFragment.getCurrentPath();
			if (file.isDir && cur.contains(file.mPath)) {
				errorPath = file.mPath;
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onResume(this);
		
		switch (op) {
		case COPYTO:
			mainFragment.setTitle(getString(R.string.DM_Task_Copy));
			break;
		case DECRYPTEDTO:
			btn_ok.setText(R.string.DM_Bottom_Bar_Button_Decrypt);
			break;
		default:
			mainFragment.setTitle(getString(R.string.DM_Navigation_Upload_Path));
			break;
		}
//		mainFragment.reloadItems();
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MobclickAgent.onPause(this);
	}
	
	private void setDefaultFragment() {
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction transaction = fm.beginTransaction();
		if (mainFragment == null) {
			mainFragment =  MainFragment.newInstance(MainFragment.FILE_TYPE_PATHSELECT);
			System.out.println("do:"+mainFragment);
		}
		transaction.add(R.id.main_content, mainFragment);
		transaction.commitAllowingStateLoss();
		
	}
	
	@Override
	public void onAttachFragment(Fragment fragment) {
		// TODO Auto-generated method stub
		super.onAttachFragment(fragment);
		
		if (MainFragment.class.isInstance(fragment)) {
			((MainFragment)fragment).setOnEditableChange(new OnEditableChange() {
				
				@Override
				public void onChange(boolean show) {
					// TODO Auto-generated method stub
					if (show) {
						btn_ok.setEnabled(true);
					}else {
						btn_ok.setEnabled(false);
					}
				}
			});
		}
		
	}
	
	public void onEventMainThread(DisconnectEvent event){
		finish();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			
			if (mainFragment != null && mainFragment.isVisible() && mainFragment.isEditMode()) {
				mainFragment.setEditState(EditState.STATE_NORMAL);;
				return true;
			}
			
			if (mainFragment != null && mainFragment.isVisible() && mainFragment.isCanToUpper()) {
				mainFragment.toUpper();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}
	

}
