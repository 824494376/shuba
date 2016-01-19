package com.qiwenge.android.fragments;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.baidu.mobad.feeds.BaiduNative;
import com.baidu.mobad.feeds.NativeErrorCode;
import com.baidu.mobad.feeds.NativeResponse;
import com.baidu.mobad.feeds.RequestParameters;
import com.liuguangqiang.framework.utils.Logs;
import com.qiwenge.android.R;
import com.qiwenge.android.adapters.BookShelfAdapter;
import com.qiwenge.android.async.AsyncRemoveBook;
import com.qiwenge.android.base.BaseFragment;
import com.qiwenge.android.constant.MyActions;
import com.qiwenge.android.entity.Book;
import com.qiwenge.android.entity.BookUpdate;
import com.qiwenge.android.ui.dialogs.MyDialog;
import com.qiwenge.android.utils.BookShelfUtils;
import com.qiwenge.android.utils.SkipUtils;
import com.qiwenge.android.utils.StyleUtils;
import com.qiwenge.android.utils.book.BookManager;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class BookshelfFragment extends BaseFragment {

    private SwipeRefreshLayout mSwipeLayout;
    private ListView lvBookShelf;
    private LinearLayout layoutEmpty;

    private List<Book> data = new ArrayList<>();
    private BookShelfAdapter adapter;

    private BookShelfReceiver bookShelfReceiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bookshelf, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initViews();

        bookShelfReceiver = new BookShelfReceiver();
        IntentFilter intentFilter = new IntentFilter(MyActions.UPDATE_BOOK_SHELF);
        getActivity().registerReceiver(bookShelfReceiver, intentFilter);

        testNativeAd();
    }

    private void testNativeAd() {
        Logs.i("testNativeAd");
        BaiduNative baiduNative = new BaiduNative(getActivity(), "2394443", new BaiduNative.BaiduNativeNetworkListener() {
            @Override
            public void onNativeLoad(List<NativeResponse> list) {
                Logs.i("onNativeLoad : " + list.toString());
            }

            @Override
            public void onNativeFail(NativeErrorCode nativeErrorCode) {
                Logs.i("onNativeFail:" + nativeErrorCode.toString());
            }
        });

        RequestParameters requestParameters = new RequestParameters.Builder().keywords("彩票,理财")
                .confirmDownloading(true)
                .build();
        baiduNative.makeRequest(requestParameters);
    }


    @Override
    public void onResume() {
        super.onResume();
        getBooks();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bookShelfReceiver != null) {
            getActivity().unregisterReceiver(bookShelfReceiver);
            bookShelfReceiver = null;
        }
    }

    private void initViews() {
        layoutEmpty = (LinearLayout) getView().findViewById(R.id.layout_empty);
        layoutEmpty.setVisibility(View.GONE);
        mSwipeLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipe_container);
        StyleUtils.setColorSchemeResources(mSwipeLayout);
        adapter = new BookShelfAdapter(getActivity(), data);
        lvBookShelf = (ListView) getView().findViewById(R.id.lv_book_shelf);
        lvBookShelf.setAdapter(adapter);
        lvBookShelf.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < data.size()) {
                    Book book = data.get(position);
                    SkipUtils.skipToReader(getActivity(), book);
                    updateChapterTotal(book);
                }
            }
        });
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getBooks();
            }
        });

        lvBookShelf.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showBookDialog(data.get(position));
                return true;
            }
        });
    }

    private void updateChapterTotal(Book book) {
        if (book.updateArrival > 0) {
            book.hasUpdate = false;
            book.updateArrival = 0;
            BookShelfUtils.updateChapterTotal(getActivity(), book.getId(), book.chapter_total);
            adapter.notifyDataSetChanged();
        }
    }

    private void showOrHideEmpty() {
        Logs.i("showOrHideEmpty");
        if (data.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    private void showBookDialog(final Book book) {
        MyDialog myDialog = new MyDialog(getActivity(), book.title);
        String[] items = getResources().getStringArray(R.array.book_detail_action_titles);
        myDialog.setItems(items, new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        updateChapterTotal(book);
                        SkipUtils.skipToBookDetail(getActivity(), book);
                        break;
                    case 1:
                        deleteBook(book);
                        break;
                }
            }
        });
        myDialog.show();
    }

    private void showUpdate(List<BookUpdate> updates) {
        Book book;
        BookUpdate bookUpdate;
        int shelfSize = data.size();
        int updateSize = updates.size();

        for (int i = 0; i < shelfSize; i++) {
            for (int j = 0; j < updateSize; j++) {
                book = data.get(i);
                bookUpdate = updates.get(j);
                if (book.getId().equals(bookUpdate.book_id)) {
                    book.hasUpdate = bookUpdate.updated();
                    book.updateArrival = bookUpdate.arrival;
                    book.chapter_total = bookUpdate.chapter_total;
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void deleteBook(Book book) {
        new AsyncRemoveBook(getActivity(), null).execute(book);
        data.remove(book);
        adapter.notifyDataSetChanged();
        showOrHideEmpty();
    }

    private void getBooks() {
        Logs.i("getBooks");
        Observable.create(new Observable.OnSubscribe<List<Book>>() {
            @Override
            public void call(Subscriber<? super List<Book>> subscriber) {
                subscriber.onNext(BookManager.getInstance().getAll());
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Book>>() {
                    @Override
                    public void onCompleted() {
                        showOrHideEmpty();
                        mSwipeLayout.setRefreshing(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(List<Book> books) {
                        if (books != null && !books.isEmpty()) {
                            data.clear();
                            adapter.add(books);
                        }
                    }
                });
    }

    public class BookShelfReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MyActions.UPDATE_BOOK_SHELF)) {
                getBooks();
            }
        }
    }

}
