/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dmsys.airdiskpro.utils;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import rx.Emitter;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Cancellable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;


public class HttpDownloadTool {

    private static CompositeSubscription mCompositeSubscription = new CompositeSubscription();

    public static void downloadFile(final String URL, final String savePath, final HttpProgressListener progressListener) throws Exception {

        Subscription subscription = Observable.create(new Action1<Emitter<ProgressModel>>() {
            @Override
            public void call(Emitter<ProgressModel> subscriber) {


                final AtomicBoolean unsubscribed = new AtomicBoolean(false);
                subscriber.setCancellation(new Cancellable() {
                    @Override
                    public void cancel() throws Exception {
                        unsubscribed.set(true);
                    }
                });

                try {
                    OkHttpClient httpClient = new OkHttpClient();
                    Call call = httpClient.newCall(new Request.Builder().url(URL).build());
                    Response response = call.execute();
                    if (response.code() == 200) {

                        makeFileFullPath(savePath);

                        File file = new File(savePath);

                        FileOutputStream outputStream = null;

                        InputStream inputStream = null;
                        try {

                            inputStream = response.body().byteStream();
                            outputStream = new FileOutputStream(file);
                            byte[] buff = new byte[1024 * 4];
                            long downloaded = 0;
                            long target = response.body().contentLength();

                            ProgressModel progressModel = new ProgressModel();
                            progressModel.updateData(0, target, false);
                            subscriber.onNext(progressModel);
                            int length = 0;
                            long oldPro = 0;
                            long tmp = 0;
                            while ((length = inputStream.read(buff)) != -1) {
                                outputStream.write(buff, 0, length);
                                downloaded += length;

                                tmp = (downloaded * 100 / target);

                                if (tmp != oldPro) {
                                    progressModel.updateData(downloaded, target, false);
                                    subscriber.onNext(progressModel);
                                    oldPro = tmp;
                                }

                                if (unsubscribed.get()) {
                                    break;
                                }
                            }
                            outputStream.flush();

                            if (downloaded == target) {
                                subscriber.onCompleted();
                            } else {
                                subscriber.onError(null);
                            }

                        } catch (IOException ignore) {
                            subscriber.onError(null);

                        } finally {
                            if (inputStream != null) {
                                inputStream.close();
                            }
                            if (outputStream != null) {
                                outputStream.close();
                            }
                        }
                    } else {
                        subscriber.onError(null);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }, Emitter.BackpressureMode.LATEST).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()) //当回调速度太快UI层来不及处理就把数据丢掉
                .subscribe(new Subscriber<ProgressModel>() {
                    @Override
                    public void onNext(ProgressModel s) {
                        if (s == null) return;

                        if (!progressListener.update(s.already, s.total, s.done)) {
                            unsubscribe();
                        }
                    }

                    @Override
                    public void onCompleted() {
                        progressListener.update(-1, -1, true);
                    }

                    @Override
                    public void onError(Throwable e) {
                        progressListener.update(-1, -1, false);
                    }
                });
        mCompositeSubscription.add(subscription);

    }


    private static boolean makeFileFullPath(String savePath) {
        boolean ret = false;
        File file = new File(savePath);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (file.exists()) {
            file.delete();
        }
        try {
            ret = file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    static class ProgressModel {
        public long already;
        public long total;
        public boolean done;

        public ProgressModel() {
        }

        public ProgressModel(long already, long total, boolean done) {
            this.already = already;
            this.total = total;
            this.done = done;
        }

        public void updateData(long already, long total, boolean done) {
            this.already = already;
            this.total = total;
            this.done = done;
        }
    }

    public interface HttpProgressListener {
        public boolean update(long bytesRead, long contentLength, boolean done);
    }
}
