package com.qiwenge.android.utils.book;

import android.content.Context;

import com.google.gson.Gson;
import com.liuguangqiang.framework.utils.Logs;
import com.liuguangqiang.framework.utils.PreferencesUtils;
import com.liuguangqiang.framework.utils.StringUtils;
import com.qiwenge.android.entity.Book;
import com.qiwenge.android.entity.BookList;
import com.qiwenge.android.entity.Mirror;
import com.qiwenge.android.entity.Progresses;
import com.qiwenge.android.utils.LoginManager;
import com.qiwenge.android.utils.PushUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * BookManager.
 * <p>书籍管理,管理书架中的小说<p/>
 * Created by Eric on 15/1/15.
 */
public class BookManager {

    private static final String SHUBA_BOOK_SHELF = "SHUBA_BOOK_SHELF";

    /**
     * 未登录的时候，用DEFAULT_KEY来保存数据。
     */
    private static final String DEFAULT_KEY = "DEFAULT_KEY";

    private static ArrayList<Book> books = new ArrayList<>();

    private static BookManager ourInstance = new BookManager();

    public static BookManager getInstance() {
        return ourInstance;
    }

    private BookManager() {
    }

    public void init(Context context) {
        books.clear();
        String json = PreferencesUtils.getString(context, SHUBA_BOOK_SHELF, getSaveKey());
        if (!StringUtils.isEmptyOrNull(json)) {
            BookList bookList = new Gson().fromJson(json, BookList.class);
            if (bookList != null && bookList.result != null) {
                books.addAll(bookList.result);
            }
        }
        transferToUserAccount(context);
        new PushUtils(context).setTags(books);
    }

    /**
     * 把在未登录情况下保存的小说，转移到重新登陆的用户名下。
     */
    private void transferToUserAccount(Context context) {
        if (LoginManager.isLogin()) {
            String json = PreferencesUtils.getString(context, SHUBA_BOOK_SHELF, DEFAULT_KEY);
            if (!StringUtils.isEmptyOrNull(json)) {
                BookList bookList = new Gson().fromJson(json, BookList.class);
                if (bookList != null && bookList.result != null) {
                    merge(context, bookList.result);
                }
            }
        }
    }

    private void merge(Context context, List<Book> bookList) {
        for (Book book : bookList) {
            if (!contains(book)) {
                addWithoutSave(book);
            }
        }
        save(context);
        clearUnloginBook(context);
    }

    private String getSaveKey() {
        if (LoginManager.isLogin()) return LoginManager.getUser().getId();
        return DEFAULT_KEY;
    }

    public void save(Context context) {
        BookList bookList = new BookList();
        bookList.result.addAll(books);
        String json = new Gson().toJson(bookList);
        PreferencesUtils.putString(context, SHUBA_BOOK_SHELF, getSaveKey(), json);
    }


    public void clear() {
        books.clear();
    }

    private void clearUnloginBook(Context context) {
        PreferencesUtils.putString(context, SHUBA_BOOK_SHELF, DEFAULT_KEY, "");
    }

    public List<Book> getAll() {
        return books;
    }

    public Book getById(String id) {
        if (books.isEmpty()) return null;

        List<Book> list = books;
        int size = list.size();
        for (int i = 0; i < size; i++) {
            if (list.get(i).getId().equals(id)) {
                return list.get(i);
            }
        }
        return null;
    }

    public void addWithoutSave(Book book) {
        books.add(book);
    }

    public void add(Context context, Book book) {
        Logs.i("add:" + book.title);
        books.add(book);
        save(context);
    }

    public void delete(Context context, Book book) {
        Logs.i("delete:" + book.title);
        int p = indexOf(book);
        if (p >= 0) {
            books.remove(p);
            save(context);
            Logs.i("delete success");
        }
    }

    public void update(Context context, Book book) {
        int position = indexOf(book);
        if (position >= 0) {
            books.set(position, book);
            save(context);
        }
    }

    private int indexOf(Book book) {
        if (books.isEmpty()) return -1;

        List<Book> list = books;
        int size = list.size();
        for (int i = 0; i < size; i++) {
            if (list.get(i).getId().equals(book.getId())) {
                return i;
            }
        }
        return -1;
    }

    public boolean contains(Book book) {
        if (books.isEmpty()) return false;

        return indexOf(book) >= 0;
    }

}


