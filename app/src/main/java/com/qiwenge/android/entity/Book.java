package com.qiwenge.android.entity;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

import com.qiwenge.android.entity.base.BaseModel;

/**
 * Book
 * <p/>
 * Created by Eric on 2014-5-6
 */
public class Book extends BaseModel implements Parcelable {

    public boolean hasUpdate = false;

    public int updateArrival = 0;

    public String title;

    public String description;

    public String author;

    public String cover;

    public int status;

    /**
     * 1：完本；0:连载
     */

    public int finish;

    public ArrayList<String> categories;

    public int chapter_total;

    public Progresses progresses;

    public Book() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeByte(hasUpdate ? (byte) 1 : (byte) 0);
        dest.writeInt(this.updateArrival);
        dest.writeString(this.title);
        dest.writeString(this.description);
        dest.writeString(this.author);
        dest.writeString(this.cover);
        dest.writeInt(this.status);
        dest.writeInt(this.finish);
        dest.writeStringList(this.categories);
        dest.writeInt(this.chapter_total);
        dest.writeParcelable(this.progresses, 0);
    }

    protected Book(Parcel in) {
        super(in);
        this.hasUpdate = in.readByte() != 0;
        this.updateArrival = in.readInt();
        this.title = in.readString();
        this.description = in.readString();
        this.author = in.readString();
        this.cover = in.readString();
        this.status = in.readInt();
        this.finish = in.readInt();
        this.categories = in.createStringArrayList();
        this.chapter_total = in.readInt();
        this.progresses = in.readParcelable(Progresses.class.getClassLoader());
    }

    public static final Creator<Book> CREATOR = new Creator<Book>() {
        public Book createFromParcel(Parcel source) {
            return new Book(source);
        }

        public Book[] newArray(int size) {
            return new Book[size];
        }
    };
}
