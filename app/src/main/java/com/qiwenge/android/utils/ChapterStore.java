package com.qiwenge.android.utils;

import com.google.gson.Gson;
import com.liuguangqiang.framework.utils.GsonUtils;
import com.liuguangqiang.framework.utils.Logs;
import com.qiwenge.android.entity.Chapter;
import com.qiwenge.android.listeners.ChapterListener;
import com.qiwenge.android.utils.http.JHttpClient;
import com.qiwenge.android.utils.http.JsonResponseHandler;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * Created by Eric on 16/1/12.
 */
public class ChapterStore {

    public static void getChapter(String bookId, String chapterId, ChapterListener listener) {
        File file = OfflineUtils.getOfflineFile(bookId, chapterId);
        if (file != null && file.exists()) {
            getChapterForLocal(file, listener);
        } else {
            getChapterFromServer(chapterId, listener);
        }
    }

    private static void getChapterForLocal(final File file, final ChapterListener listener) {
        Logs.i("getChapterForLocal");
        rx.Observable.create(new rx.Observable.OnSubscribe<Chapter>() {
            @Override
            public void call(Subscriber<? super Chapter> subscriber) {
                try {
                    String json = getStringFromFile(file);
                    String result = GsonUtils.getResult(json, "result");
                    Chapter chapter = GsonUtils.getModel(result, Chapter.class);
                    Logs.i(chapter.title);
                    subscriber.onNext(chapter);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Chapter>() {
                    @Override
                    public void onCompleted() {
                        listener.onFinish();
                    }

                    @Override
                    public void onError(Throwable e) {
                        listener.onFailure();
                    }

                    @Override
                    public void onNext(Chapter chapter) {
                        listener.onSuccess(chapter);
                    }
                });
    }

    private static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    private static String getStringFromFile(File file) throws Exception {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
        String result = convertStreamToString(bufferedInputStream);
        bufferedInputStream.close();
        return result;
    }

    private static void getChapterFromServer(String chapterId, final ChapterListener listener) {
        Logs.i("getChapterFromServer");
        String url = ApiUtils.getChapter(chapterId);
        JHttpClient.get(url, null, new JsonResponseHandler<Chapter>(Chapter.class, false) {

            @Override
            public void onSuccess(final Chapter result) {
                listener.onSuccess(result);
            }

            @Override
            public void onStart() {
                listener.onStart();
            }

            @Override
            public void onFailure(String msg) {
                listener.onFailure();
            }

            @Override
            public void onFinish() {
                listener.onFinish();
            }
        });
    }

}
