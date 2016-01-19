package com.qiwenge.android.entity;

import android.os.Parcel;

import com.qiwenge.android.entity.base.BaseModel;

/**
 * 章节。 Chapter
 * <p/>
 * Created by Eric on 2014年5月5日
 */
public class Chapter extends BaseModel {

    /**
     * 章节标题
     */
    public String title;

    /**
     * 书籍名字
     */
    public String book_title;

    /**
     * 章节编号，整型
     */
    public int number;

    /**
     * 内容
     */
    public String content;

    /**
     * 审核状态
     * <p/>
     * 0：未审核；1:已经审核
     */
    public int status;

    /**
     * 内容来源，URL
     */
    public String source;

    /**
     * 上一页
     */
    public RefModel prev;

    /**
     * 下一页
     */
    public RefModel next;

    /**
     * 是否被选中，目录中定位到已经看过的地方。
     */
    public boolean isSelected = false;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.title);
        dest.writeString(this.book_title);
        dest.writeInt(this.number);
        dest.writeString(this.content);
        dest.writeInt(this.status);
        dest.writeString(this.source);
        dest.writeParcelable(this.prev, 0);
        dest.writeParcelable(this.next, 0);
        dest.writeByte(isSelected ? (byte) 1 : (byte) 0);
    }

    public Chapter() {
    }

    protected Chapter(Parcel in) {
        super(in);
        this.title = in.readString();
        this.book_title = in.readString();
        this.number = in.readInt();
        this.content = in.readString();
        this.status = in.readInt();
        this.source = in.readString();
        this.prev = in.readParcelable(RefModel.class.getClassLoader());
        this.next = in.readParcelable(RefModel.class.getClassLoader());
        this.isSelected = in.readByte() != 0;
    }

    public static final Creator<Chapter> CREATOR = new Creator<Chapter>() {
        public Chapter createFromParcel(Parcel source) {
            return new Chapter(source);
        }

        public Chapter[] newArray(int size) {
            return new Chapter[size];
        }
    };
}
