package com.qiwenge.android.utils;

import android.content.Context;
import android.util.Log;

import com.liuguangqiang.framework.utils.encrypt.Md5;
import com.qiwenge.android.constant.Constants;
import com.qiwenge.android.entity.Book;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

/**
 * Created by Eric on 14/12/3.
 */
public class PushUtils {

    private final static int STATUS_SUCCESS = 0;

    private final static int STATUS_TIMEOUT = 6002;

    private final static int RETRY_COUNT = 5;

    private final static String PUSH_MANAGER = "PUSH_MANAGER";

    private final static String IS_OPEN = "IS_OPEN";

    private Context mContext;

    public PushUtils(Context context) {
        mContext = context;
    }

    public void setAlias() {
        if (LoginManager.isLogin()) {
            setAlias(LoginManager.getUser().getId(), RETRY_COUNT);
        } else {
            clearAlias();
        }
    }

    private void setAlias(final String alias, final int retryCount) {
        JPushInterface.setAlias(mContext, alias, new TagAliasCallback() {
            @Override
            public void gotResult(int i, String s, Set<String> strings) {
                if (i == STATUS_TIMEOUT && retryCount > 0) {
                    setAlias(alias, retryCount - 1);
                } else if (i == STATUS_SUCCESS) {
                    Log.i("PushUtils", alias);
                }
            }
        });
    }

    //TODO setTags stg_bookid_mirrorid
    public void setTags(List<Book> books) {
//        Set<String> tags = new HashSet<>();
//        String format;
//        if (Constants.DEBUG) {
//            format = "stg_%s";
//        } else {
//            format = "%s";
//        }
//        for (Book book : books) {
//            tags.add(String.format(format, Md5.encode(book.getId() + "_" + book.currentMirrorId())));
//        }
//
//        Set<String> filterTags = JPushInterface.filterValidTags(tags);
//
//        setTags(filterTags, RETRY_COUNT);
    }

    private void setTags(final Set<String> tags, final int retryCount) {
        JPushInterface.setTags(mContext, tags, new TagAliasCallback() {
            @Override
            public void gotResult(int i, String s, Set<String> strings) {
                if (i == STATUS_TIMEOUT && retryCount > 0) {
                    setTags(tags, retryCount - 1);
                } else if (i == STATUS_SUCCESS) {
                }
            }
        });
    }

    public void clearAlias() {
        Log.i("PushUtils", "clearAlias");
        setAlias("", RETRY_COUNT);
    }

}
