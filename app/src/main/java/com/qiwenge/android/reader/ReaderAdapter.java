package com.qiwenge.android.reader;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.mobads.AdView;
import com.liuguangqiang.framework.utils.DisplayUtils;
import com.qiwenge.android.R;
import com.qiwenge.android.constant.Constants;
import com.qiwenge.android.entity.Page;
import com.qiwenge.android.utils.ThemeUtils;

/**
 * 阅读适配器。Created by Eric at 2014.
 */
public class ReaderAdapter extends PagerAdapter {

    private List<Page> pages = new ArrayList<Page>();
    private LayoutInflater mInflater;
    private int mTextSize = Constants.DEFAULT_TEXT_SIZE;

    public ReaderAdapter(Context context, List<Page> pages) {
        this.pages = pages;
        mInflater = LayoutInflater.from(context);
    }

    public void setTextSize(int textSize) {
        mTextSize = textSize;
    }

    @Override
    public View instantiateItem(ViewGroup container, final int position) {
        View view = mInflater.inflate(R.layout.item_reader, container, false);
//        RelativeLayout adContainer = (RelativeLayout) view.findViewById(R.id.layout_read_ad);
//        patchAd(adContainer);

        TextView tv = (TextView) view.findViewById(R.id.tv_reader);
        tv.setText(pages.get(position).content);
        tv.setTextSize(mTextSize);
        ThemeUtils.setTextColor(tv);
        container.addView(view, 0);
        return view;
    }

    private void patchAd(RelativeLayout container) {
        String adPlaceID = "2394442";
        AdView adView = new AdView(container.getContext(), adPlaceID);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                DisplayUtils.dip2px(container.getContext(), 56));
        container.addView(adView, params);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return pages.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        final View view = (View) object;
        container.removeView(view);
    }


}
