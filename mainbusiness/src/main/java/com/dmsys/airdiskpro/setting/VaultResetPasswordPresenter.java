package com.dmsys.airdiskpro.setting;

import com.dmsys.airdiskpro.setting.DeviceVaultContract.IVaultReSetPasswordView;

import java.util.concurrent.Callable;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static com.dmsys.airdiskpro.utils.Preconditions.checkNotNull;

public class VaultResetPasswordPresenter implements DeviceVaultContract.IVaultReSetPasswordPresenter {

    IVaultReSetPasswordView mVaultView;
    ParamCheckUserCase mParamCheckUserCase;
    CompositeSubscription mSubscriptions;

    public VaultResetPasswordPresenter(IVaultReSetPasswordView mVaultView,
                                       ParamCheckUserCase mParamCheckUserCase, CompositeSubscription mSubscriptions) {
        super();
        // TODO Auto-generated constructor stub

        this.mVaultView = checkNotNull(mVaultView);
        this.mVaultView.setPresenter(this);
        this.mParamCheckUserCase = mParamCheckUserCase;
        this.mSubscriptions = mSubscriptions;
    }

    @Override
    public void start() {
        // TODO Auto-generated method stub
        //nothing
    }

    @Override
    public void stop() {

    }

    @Override
    public void resetPassword(final String oldPassword, final String password, final String password1,
                              final String contentTips) {
        // TODO Auto-generated method stub


        Subscription subscription = Observable.fromCallable(new Callable<Integer>() {

            @Override
            public Integer call() {
                return mParamCheckUserCase.reSetPasswordAndTips(oldPassword, password, password1, contentTips);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer ret) {
                        mVaultView.onResetPasswordResult((int) ret);

                    }
                });
        mSubscriptions.add(subscription);


    }


}
