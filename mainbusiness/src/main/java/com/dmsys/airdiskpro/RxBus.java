package com.dmsys.airdiskpro;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * Created by jiong103 on 17/3/15.
 * Description : Rxbus
 */

//demo
/*    接收：
    RxBus.getDefault().toObservable().subscribe(new Action1<TapEvent>() {
@Override
public void call(TapEvent event) {

        }
        });

 发送：
        RxBus.getDefault().send(new TapEvent());*/



public final class RxBus {

    private static volatile RxBus mDefaultInstance;

    private RxBus() {
    }

    public static RxBus getDefault() {
        if (mDefaultInstance == null) {
            synchronized (RxBus.class) {
                if (mDefaultInstance == null) {
                    mDefaultInstance = new RxBus();
                }
            }
        }
        return mDefaultInstance;
    }

    private final Subject<Object, Object> _bus = new SerializedSubject<>(PublishSubject.create());

    public void send(Object o) {
        _bus.onNext(o);
    }

    public Observable<Object> toObservable() {
        return _bus.onBackpressureBuffer();
    }

}
