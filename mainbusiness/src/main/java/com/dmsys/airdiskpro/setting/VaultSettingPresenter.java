package com.dmsys.airdiskpro.setting;

import com.dmsys.airdiskpro.setting.DeviceVaultContract.IVaultView;
import com.dmsys.dmsdk.DMSdk;
import com.dmsys.dmsdk.model.DMIsOpeningVault;
import com.dmsys.dmsdk.model.DMRet;
import com.dmsys.dmsdk.model.DMVaultPwTips;

import java.util.concurrent.Callable;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static com.dmsys.airdiskpro.utils.Preconditions.checkNotNull;

public class VaultSettingPresenter implements DeviceVaultContract.IVaultPresenter {


    IVaultView mVaultView;
    ParamCheckUserCase mParamCheckUserCase;
    CompositeSubscription mCompositeSubscription;

    @Override
    public void stop() {

    }

    public VaultSettingPresenter(IVaultView mVaultView, ParamCheckUserCase mParamCheckUserCase, CompositeSubscription c) {
        super();
        // TODO Auto-generated constructor stub

        this.mVaultView = checkNotNull(mVaultView);
        this.mVaultView.setPresenter(this);
        this.mParamCheckUserCase = mParamCheckUserCase;
        this.mCompositeSubscription = c;
    }

    @Override
    public void start() {
        // TODO Auto-generated method stub
        getVaultStatus();
    }


    private void getVaultStatus() {


        Subscription subscription = Observable.fromCallable(new Callable<DMIsOpeningVault>() {

            @Override
            public DMIsOpeningVault call() {
                return DMSdk.getInstance().isOpeningVault();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<DMIsOpeningVault>() {
                    @Override
                    public void call(DMIsOpeningVault type) {
                        if (type != null && type.errorCode == DMRet.ACTION_SUCCESS && type.isOpen) {
                            mVaultView.updateVaultView(true);
                        } else {
                            mVaultView.updateVaultView(false);
                        }

                    }
                });


        mCompositeSubscription.add(subscription);

    }


    @Override
    public void createPassword(final String password, final String password1, final String contentTips) {
        // TODO Auto-generated method stub

        Subscription subscription = Observable.fromCallable(new Callable<Integer>() {

            @Override
            public Integer call() {
                return mParamCheckUserCase.setPasswordAndTips(password, password1, contentTips);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer type) {
                        mVaultView.onCreatePasswordResult(type);
                    }
                });


        mCompositeSubscription.add(subscription);

    }

    @Override
    public void closeVault(final String password) {
        // TODO Auto-generated method stub

        Subscription subscription = Observable.fromCallable(new Callable<Integer>() {

            @Override
            public Integer call() {
                return DMSdk.getInstance().closeVault(password);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer ret) {
                        mVaultView.onCloseVaultResult(ret);
                    }
                });


        mCompositeSubscription.add(subscription);

    }

    @Override
    public void checkVault() {
        // TODO Auto-generated method stub


        Subscription subscription = Observable.fromCallable(new Callable<Boolean>() {

            @Override
            public Boolean call() {
                return DMSdk.getInstance().isEmptyVault();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean type) {
                        mVaultView.onCheckVaultResult(type);
                    }
                });


        mCompositeSubscription.add(subscription);
    }

    @Override
    public void getPasswordTips() {
        // TODO Auto-generated method stub


        Subscription subscription = Observable.fromCallable(new Callable<DMVaultPwTips>() {

            @Override
            public DMVaultPwTips call() {
                return DMSdk.getInstance().getPwTips();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<DMVaultPwTips>() {
                    @Override
                    public void call(DMVaultPwTips ret) {

                        if (ret != null) {
                            DMVaultPwTips dMVaultPwTips = (DMVaultPwTips) ret;
                            if (dMVaultPwTips.errorCode == DMRet.ACTION_SUCCESS) {
                                mVaultView.onGetPasswordTipsResult(dMVaultPwTips.tips);
                                return;
                            }
                        }
                        mVaultView.onGetPasswordTipsResult(null);
                    }
                });


        mCompositeSubscription.add(subscription);
    }


}
