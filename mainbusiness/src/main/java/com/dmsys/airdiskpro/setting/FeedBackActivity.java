package com.dmsys.airdiskpro.setting;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.alibaba.sdk.android.feedback.impl.FeedbackAPI;
import com.dmsys.mainbusiness.R;


/**
 * 设置-意见反馈页
 *
 * @author admin
 *
 */
public class FeedBackActivity extends FragmentActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fragment_feedback);

		FeedbackAPI.activity = this; // 设置activity
		// support-v4包中的Fragment
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction transaction = fm.beginTransaction();
		Fragment feedback = FeedbackAPI.getFeedbackFragment();
		transaction.replace(R.id.content, feedback);
		transaction.commit();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		FeedbackAPI.cleanFeedbackFragment();
		FeedbackAPI.cleanActivity();
	}

	
}
