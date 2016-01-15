package com.qiwenge.android.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.liuguangqiang.framework.utils.StringUtils;
import com.qiwenge.android.act.BookDetailActivity;
import com.qiwenge.android.act.ChapterActivity;
import com.qiwenge.android.act.ReadActivity;
import com.qiwenge.android.entity.Book;
import com.qiwenge.android.utils.book.BookManager;

public class SkipUtils {

    public static void skipToBookDetail(Context context, Book book) {
        Bundle extra = new Bundle();
        extra.putParcelable(BookDetailActivity.EXTRA_BOOK, book);
        Intent intent = new Intent(context.getApplicationContext(), BookDetailActivity.class);
        intent.putExtras(extra);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 跳到阅读页面。
     *
     * @param context
     */
    public static void skipToReader(Context context, Book book) {
        String lastReadId = null;

        Book record = BookManager.getInstance().getById(book.getId());

        if (record != null && record.progresses != null) {
            lastReadId = record.progresses.chapter_id;
            Log.i("BookShelf", "lastReadId:" + lastReadId);
        }

        if (StringUtils.isEmptyOrNull(lastReadId)) {
            skipToChapter(context, book);
        } else {
            skipToReader(context, book, lastReadId);
        }
    }

    private static void skipToChapter(Context context, Book book) {
        Bundle extra = new Bundle();
        extra.putParcelable(ChapterActivity.EXTRA_BOOK, book);
        Intent intent = new Intent(context, ChapterActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtras(extra);
        context.startActivity(intent);
    }

    public static void skipToReader(Context context, Book book,
                                    String chapterId) {
        Bundle extra = new Bundle();
        extra.putParcelable(ReadActivity.Extra_Book, book);
        extra.putString(ReadActivity.Extra_ChapterId, chapterId);
        Intent intent = new Intent(context, ReadActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtras(extra);
        context.startActivity(intent);
    }

}
