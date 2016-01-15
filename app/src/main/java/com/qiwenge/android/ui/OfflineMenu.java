package com.qiwenge.android.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.liuguangqiang.framework.utils.DisplayUtils;
import com.liuguangqiang.framework.utils.Logs;
import com.liuguangqiang.framework.utils.NetworkUtils;
import com.liuguangqiang.framework.utils.ToastUtils;
import com.qiwenge.android.R;
import com.qiwenge.android.adapters.OfflineMenuAdapter;
import com.qiwenge.android.entity.Book;
import com.qiwenge.android.entity.Chapter;
import com.qiwenge.android.entity.OfflineMenuItem;
import com.qiwenge.android.utils.OfflineUtils;
import com.qiwenge.android.utils.http.JHttpClient;
import com.qiwenge.android.utils.http.JsonResponseHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eric on 15/5/2.
 */
public class OfflineMenu extends RelativeLayout {

    private Book mBook;
    private Chapter mChapter;
    private TextView tvProgress;
    private GridView gvDownload;

    public OfflineMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void setBook(Book book) {
        mBook = book;
    }

    public void setChapter(Chapter chapter) {
        mChapter = chapter;
    }

    private void init(Context context) {
        setGravity(Gravity.CENTER_HORIZONTAL);

        addView(createGridView(context));
        addView(createStartBtn(context));
    }

    private GridView createGridView(Context context) {
        String[] titles = context.getResources().getStringArray(R.array.offline_items);
        final List<OfflineMenuItem> offlineList = new ArrayList<>();
        OfflineMenuItem offline;
        for (String title : titles) {
            offline = new OfflineMenuItem();
            offline.title = title;
            offlineList.add(offline);
        }

        gvDownload = new GridView(context);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                DisplayUtils.dip2px(context, 56));
        gvDownload.setLayoutParams(params);
        OfflineMenuAdapter adapter = new OfflineMenuAdapter(context, offlineList);
        gvDownload.setAdapter(adapter);
        gvDownload.setNumColumns(3);
        gvDownload.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (NetworkUtils.isAvailable(getContext())) {
                    switch (position) {
                        case 0:
                            startDownload(50);
                            break;
                        case 1:
                            startDownload(100);
                            break;
                        default:
                            startDownload(mBook.chapter_total - mChapter.number);
                            break;
                    }
                } else {
                    ToastUtils.show(getContext(), R.string.error_network);
                }
            }
        });

        return gvDownload;
    }

    private TextView createStartBtn(Context context) {
        tvProgress = new TextView(context);
        tvProgress.setVisibility(GONE);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                DisplayUtils.dip2px(context, 56));
        tvProgress.setLayoutParams(params);

        tvProgress.setText("");
        tvProgress.setGravity(Gravity.CENTER);
        tvProgress.setTextColor(context.getResources().getColor(R.color.white_p50));
        tvProgress.setTextSize(12);

        return tvProgress;
    }

    /**
     * 更新下载进度
     *
     * @param current
     */
    private void updateProgress(int current) {
        tvProgress.setText(getContext().getString(R.string.format_downloading, current, display));
    }

    /**
     * 需要下载的条数
     */
    private int count = 0;
    private int current = 0;
    private int display = 0;

    private void startDownload(int count) {
        gvDownload.setVisibility(GONE);
        tvProgress.setVisibility(VISIBLE);
        this.count = count;
        this.current = 0;
        this.display = count;
        if (mBook != null && mChapter != null) {
            OfflineUtils.createChapterFolder(mBook.id);
            download(mChapter.id);
        }
    }

    private void finishDownload() {
        gvDownload.setVisibility(VISIBLE);
        tvProgress.setVisibility(GONE);
    }

    private void download(final String chapterId) {

        String url = String.format("http://api.qiwenge.com/chapters/%s", chapterId);
        Logs.i("download:" + url);

        JHttpClient.get(url, null, new JsonResponseHandler<Chapter>(Chapter.class, false) {

            @Override
            public void onSuccess(final Chapter result) {
                if (result != null) {
                    Logs.i("download:" + result.title);
                    if (count > 0 && result.next != null) {
                        download(result.next.getId());
                    } else {
                        finishDownload();
                    }
                    count--;
                    current++;
                    updateProgress(current);
                }
            }

            @Override
            public void onSuccess(String json) {
                super.onSuccess(json);
                OfflineUtils.saveChapter(mBook.id, chapterId, json);
            }

            @Override
            public void onFailure(String msg) {
                finishDownload();
            }

        });
    }

}
