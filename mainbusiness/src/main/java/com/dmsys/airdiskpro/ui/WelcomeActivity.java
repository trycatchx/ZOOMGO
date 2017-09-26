package com.dmsys.airdiskpro.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.RelativeLayout;

import com.umeng.analytics.MobclickAgent;
import com.dmsys.mainbusiness.R;

//import com.dmsys.mainbusiness.R;

public class WelcomeActivity extends Activity {

	private RelativeLayout layout_welcome;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_welcome);
		
		layout_welcome = (RelativeLayout) findViewById(R.id.layout_welcome);
		
		startAnimation();

	}
	
	
	private void startAnimation() {
		// TODO Auto-generated method stub
		AlphaAnimation animation = new AlphaAnimation(0, 1);
		animation.setDuration(2000);
		animation.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
				finish();
			}
		});
		
		layout_welcome.setAnimation(animation);
		
		animation.start();
		
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onResume(this);
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MobclickAgent.onPause(this);
	}
	
	
}
