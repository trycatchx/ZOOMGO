package com.dmsys.airdiskpro.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class NetWorkUtil implements Callable<Boolean> {
	/**
	 * 判断当前网络连接状况
	 * 
	 * @param act
	 * @return
	 */
	TimerTask mTimerTask;
	Timer mTimer;
	boolean timeOut;
	Process p;
	String ip;

	public NetWorkUtil() {
		super();
		// TODO Auto-generated constructor stub
	}

	public NetWorkUtil(String ip) {
		this.ip = ip;
	}

	@Override
	public Boolean call() throws Exception {
		// TODO Auto-generated method stub
		return ping(this.ip);

	}

	public boolean manyPing(List<String> list) {
		ExecutorService pool = Executors.newFixedThreadPool(list.size());
		List<Future<Boolean>> resultList = new ArrayList<Future<Boolean>>();
		// 把要执行的多个网站ping都提交到线程池去执行
		for (String l : list) {
			// submit之后会自动执行call的回调函数，可在Future.get（）;得到对应线程的一个返回值
			Future<Boolean> future = pool.submit(new NetWorkUtil(l));
			// 将任务执行结果存储到List中
			resultList.add(future);
		}
		// 启动一次关闭，执行以前提交的任务，但不接受新任务。如果已经关闭，则调用也没有其他作用。
		pool.shutdown();

		boolean ret = false;
		for (Future<Boolean> fs : resultList) {
			try {
				ret = fs.get(); // 打印各个线程（任务）执行的结果
				if (ret)
					break;
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		return ret;
	}

	public boolean ping(String ip) {

		String result = null;

		try {

			initTimer();

			/**
			 * ping网址1次,1秒超时
			 */
			if (p != null) {
				p.destroy();
			}
			p = Runtime.getRuntime().exec("ping -c 1 -w 2 " + ip);
			// ping的状态
			int status = p.waitFor();
			cancleTimer();

			if (status == 0) {

				result = "success";

				return true;

			} else {

				result = "failed";

			}

		} catch (IOException e) {

			result = "IOException";

		} catch (InterruptedException e) {

			result = "InterruptedException";

		} finally {
			p.destroy();
		}
		return false;

	}

	private void initTimer() {
		cancleTimer();
		mTimerTask = new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				p.destroy();
			}
		};
		mTimer = new Timer();
		mTimer.schedule(mTimerTask, 2500);

	}

	private void cancleTimer() {
		timeOut = false;
		if (mTimerTask != null) {
			mTimerTask.cancel();
			mTimerTask = null;
		}
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
	}

}
