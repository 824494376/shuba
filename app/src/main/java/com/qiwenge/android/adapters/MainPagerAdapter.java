package com.qiwenge.android.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.qiwenge.android.fragments.bookcity.BookCityFragment;
import com.qiwenge.android.fragments.BookshelfFragment;
import com.qiwenge.android.fragments.MeFragment;

/**
 * 书城适配器。
 */
public class MainPagerAdapter extends FragmentPagerAdapter {

    public static final int NUM = 3;

    private BookCityFragment bookCity;
    private BookshelfFragment bookShelf;

    public MainPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int arg0) {
        switch (arg0) {
            case 0:
                if (bookShelf == null) {
                    bookShelf = new BookshelfFragment();
                }
                return bookShelf;

            case 1:
                if (bookCity == null) {
                    bookCity = new BookCityFragment();
                }
                return bookCity;
            case 2:
                return new MeFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return NUM;
    }

}
