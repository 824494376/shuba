package com.qiwenge.android.utils;

import android.content.Context;

import com.qiwenge.android.entity.Book;
import com.qiwenge.android.entity.Chapter;
import com.qiwenge.android.entity.Progresses;
import com.qiwenge.android.utils.book.BookManager;

/**
 * 书架工具类。
 */
public class BookShelfUtils {

    private static final String TAG = "BookShelf";

    /**
     * 获取阅读的章节Number
     *
     * @param bookId
     * @return
     */
    public static int getReadNumber(String bookId) {
        Book book = BookManager.getInstance().getById(bookId);
        if (book != null && book.progresses != null) {
            return book.progresses.chapters;
        }
        return 0;
    }

    /**
     * 更新小说的进度。
     *
     * @param context
     * @param book
     * @param chapter
     * @param chars
     */
    public static void updateReadRecord(Context context, Book book, Chapter chapter, int chars) {
        Progresses progresses = new Progresses();
        progresses.chapter_id = chapter.getId();
        progresses.chapters = chapter.number;
        progresses.chars = chars;
        book.progresses = progresses;
        BookManager.getInstance().update(context, book);
    }

    /**
     * 更新本地小说的ChapterTotal
     *
     * @param context
     * @param bookId
     * @param chapterTotal
     */
    public static void updateChapterTotal(Context context, String bookId, int chapterTotal) {
        Book book = BookManager.getInstance().getById(bookId);
        if (book != null) {
            book.chapter_total = chapterTotal;
            BookManager.getInstance().update(context, book);
        }
    }
}
