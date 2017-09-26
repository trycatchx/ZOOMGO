package com.dmsys.airdiskpro.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.dmsys.airdiskpro.filemanager.EditState;
import com.dmsys.airdiskpro.filemanager.FileOperationService;
import com.dmsys.airdiskpro.ui.MainFragment.OnEditModeChangeListener;
import com.umeng.analytics.MobclickAgent;
import com.dmsys.mainbusiness.R;

public class MyDownloadActivity extends FragmentActivity {

	private MainFragment mainFragment;
	
	private TextView op_delete;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mydownload);
		
		op_delete = (TextView) findViewById(R.id.op_delete);
		op_delete.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mainFragment.doFileOperation(FileOperationService.FILE_OP_DELETE);
			}
		});
		
		setDefaultFragment(); 
	}
	
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onResume(this);
		mainFragment.resetFiles();
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
			mainFragment =  MainFragment.newInstance(MainFragment.FILE_TYPE_DOWNLOAD);
		}
		transaction.add(R.id.main_content, mainFragment);
		transaction.commitAllowingStateLoss();
	}
	
	@Override
	public void onAttachFragment(Fragment fragment) {
		// TODO Auto-generated method stub
		super.onAttachFragment(fragment);
		
		if (MainFragment.class.isInstance(fragment)) {
			((MainFragment)fragment).setOnEditModeChangeListener(new OnEditModeChangeListener() {
				
				@Override
				public void onEditModeChange(boolean edit) {
					// TODO Auto-generated method stub
					if (edit) {
						op_delete.setVisibility(View.VISIBLE);
					}else {
						op_delete.setVisibility(View.GONE);
					}
				}
			});
			
		}
		
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			
			if (mainFragment != null && mainFragment.isVisible() && mainFragment.isEditMode()) {
				mainFragment.setEditState(EditState.STATE_NORMAL);
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
	}
	

}
