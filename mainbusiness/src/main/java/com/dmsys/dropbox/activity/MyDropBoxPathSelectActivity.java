package com.dmsys.dropbox.activity;

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
import com.dmsys.airdiskpro.filemanager.FileOperationService;
import com.dmsys.dmsdk.model.DMFile;
import com.dmsys.dropbox.activity.MyDropBoxAllFileFragment.DropBoxEditState;
import com.dmsys.dropbox.activity.MyDropBoxAllFileFragment.OnDropBoxEditableChange;
import com.dmsys.dropbox.view.BaseDirView;
import com.umeng.analytics.MobclickAgent;
import com.dmsys.mainbusiness.R;

import de.greenrobot.event.EventBus;


public class MyDropBoxPathSelectActivity extends FragmentActivity {

	private MyDropBoxAllFileFragment myDropBoxAllFileFragment;
	private TextView btn_ok;
	private boolean operation_cp;
	private String errorPath;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pathselect);
		
		operation_cp = getIntent().getBooleanExtra("COPYTO", false);
		btn_ok = (TextView) findViewById(R.id.btn_ok);
		
		if (operation_cp) {
			btn_ok.setText(R.string.DM_Bottom_Bar_Button_Copy);
		}else {
			btn_ok.setText(R.string.DM_Control_Definite);
		}
		
		btn_ok.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (operation_cp) {
					
					if (checkPathLegal()) {
						myDropBoxAllFileFragment.doFileOperation(FileOperationService.FILE_OP_COPYTO,FileOperationService.selectedList,myDropBoxAllFileFragment.getCurrentPath());
					}else {
						String content = String.format(getString(R.string.DM_Remind_CopyTo_Error), errorPath);
						Toast.makeText(MyDropBoxPathSelectActivity.this, content, Toast.LENGTH_SHORT).show();
					}
					
				} else {
					Intent intent=new Intent();  
					intent.putExtra("DES_PATH", myDropBoxAllFileFragment.getCurrentPath());  
					setResult(RESULT_OK, intent);  
					finish();  
				}
			}
		});
		
		setDefaultFragment(); 
		EventBus.getDefault().register(this);
	}
	
	private boolean checkPathLegal(){
		for (DMFile file : FileOperationService.selectedList) {
			String cur = myDropBoxAllFileFragment.getCurrentPath();
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
		if (operation_cp) {
			myDropBoxAllFileFragment.setTitle(getString(R.string.DM_Task_Copy));
		}else {
			myDropBoxAllFileFragment.setTitle(getString(R.string.DM_Navigation_Upload_Path));
		}
		myDropBoxAllFileFragment.reloadItems();
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
		if (myDropBoxAllFileFragment == null) {
			Bundle bundle = new Bundle();
			bundle.putInt(MyDropBoxAllFileFragment.PAGER_TYPE, 
					BaseDirView.FILE_TYPE_PATHSELECT);
			myDropBoxAllFileFragment = new MyDropBoxAllFileFragment();
			myDropBoxAllFileFragment.setArguments(bundle);
		}
		transaction.add(R.id.main_content, myDropBoxAllFileFragment);
		transaction.commitAllowingStateLoss();
		
	}
	
	@Override
	public void onAttachFragment(Fragment fragment) {
		// TODO Auto-generated method stub
		super.onAttachFragment(fragment);
		
		if (MyDropBoxAllFileFragment.class.isInstance(fragment)) {
			((MyDropBoxAllFileFragment)fragment).setOnEditableChange(new OnDropBoxEditableChange() {
				
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
			
			if (myDropBoxAllFileFragment != null && myDropBoxAllFileFragment.isVisible() && myDropBoxAllFileFragment.isEditMode()) {
				myDropBoxAllFileFragment.setEditState(DropBoxEditState.STATE_NORMAL);;
				return true;
			}
			
			if (myDropBoxAllFileFragment != null && myDropBoxAllFileFragment.isVisible() && myDropBoxAllFileFragment.isCanToUpper()) {
				myDropBoxAllFileFragment.toUpper();
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
