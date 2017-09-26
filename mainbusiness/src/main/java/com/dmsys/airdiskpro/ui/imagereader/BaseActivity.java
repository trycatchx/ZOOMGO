package com.dmsys.airdiskpro.ui.imagereader;

import android.app.Activity;

import cn.dm.longsys.library.imageloader.core.DMImageLoader;

/**
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public abstract class BaseActivity extends Activity {

	protected DMImageLoader imageLoader = DMImageLoader.getInstance();

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		getMenuInflater().inflate(R.menu.main_menu, menu);
//		return true;
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		switch (item.getItemId()) {
//			case R.id.item_clear_memory_cache:
//				imageLoader.clearMemoryCache();
//				return true;
//			case R.id.item_clear_disc_cache:
//				imageLoader.clearDiscCache();
//				return true;
//			default:
//				return false;
//		}
//	}
}
