package com.dmsys.airdiskpro.setting;

import android.support.annotation.NonNull;

import com.dm.baselib.BaseValue;
import com.dmsys.dmsdk.DMSdk;
import com.dmsys.dmsdk.model.DMIsOpeningVault;
import com.dmsys.dmsdk.model.DMRet;
import com.dmsys.dmsdk.model.DMSupportFunction;

import java.util.concurrent.Callable;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.dmsys.airdiskpro.utils.Preconditions.checkNotNull;

public class DeviceVaultPresenter implements
        DeviceVaultContract.IDeviceVaultPresenter {

    DeviceVaultContract.IDeviceVaultView mDeviceVaultView;
    private Subscription subscription;

    public DeviceVaultPresenter(
            @NonNull DeviceVaultContract.IDeviceVaultView mDeviceVaultView) {
        this.mDeviceVaultView = checkNotNull(mDeviceVaultView);
        this.mDeviceVaultView.setPresenter(this);
    }

    @Override
    public void start() {
        // TODO Auto-generated method stub
        getDeviceVaultStatus();
    }

    @Override
    public void stop() {
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    /**
     * 把部分modle 的操作写到P这里面来了。后面重构，api 需重新定义
     */
    @Override
    public void getDeviceVaultStatus() {
        // TODO Auto-generated method stub
        int type = BaseValue.supportFucntion;
        if (type < 0) {
            mDeviceVaultView.updatePasswordModifyView(false, false);
            mDeviceVaultView.updateVaultView(false, false);
        } else {

            if (DMSupportFunction.isSupportSetPassword(type)) {
                getPasswordState();
            } else {
                mDeviceVaultView.updatePasswordModifyView(false, false);
            }


            if(true){
//             if(DMSupportFunction.isSupportVault(type)) {
                getVaultState();
            } else {
                mDeviceVaultView.updateVaultView(false, false);
            }
        }
    }

    /**
     * 调用 modle ， 获取PasswordState
     * <p>
     * <p>
     * 这种写法activity 内存泄漏。。后面换一种
     */
    private void getPasswordState() {
        // 开始进行异步请求
        subscription = Observable.fromCallable(new Callable<Integer>() {

            @Override
            public Integer call() {
                return DMSdk.getInstance().getPasswordState();
            }
        }).subscribeOn(Schedulers.io()) // 指定 subscribe() 发生在 IO 线程
                .observeOn(AndroidSchedulers.mainThread()) // 指定 Subscriber
                // 的回调发生在主线程
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer ret) {
                        int type = (int) ret;
                        if (type == 1) {
                            mDeviceVaultView.updatePasswordModifyView(true, true);
                        } else {
                            mDeviceVaultView.updatePasswordModifyView(true, false);
                        }
                    }
                });

    }


    public void getVaultState() {

        subscription = Observable.create(new OnSubscribe<DMIsOpeningVault>() {
            @Override
            public void call(Subscriber<? super DMIsOpeningVault> subscriber) {
                subscriber.onNext(DMSdk.getInstance().isOpeningVault());
            }
        }).subscribeOn(Schedulers.io()) // 指定 subscribe() 发生在 IO 线程
                .observeOn(AndroidSchedulers.mainThread()) // 指定 Subscriber
                // 的回调发生在主线程
                .subscribe(new Observer<DMIsOpeningVault>() {

                    @Override
                    public void onCompleted() {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void onError(Throwable arg0) {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void onNext(DMIsOpeningVault ret) {
                        // TODO Auto-generated method stub
                        if (ret != null && ret.errorCode== DMRet.ACTION_SUCCESS) {
                            if(ret.isOpen) {
                                mDeviceVaultView.updateVaultView(true, true);
                            } else {
                                mDeviceVaultView.updateVaultView(true, false);
                            }
                        } else {
                            mDeviceVaultView.updateVaultView(false, false);
                        }
                    }

                });
    }

}
