package com.qiwenge.android.entity.base;

import android.os.Parcel;
import android.os.Parcelable;

public class BaseModel implements Parcelable {

    public String id;

    public Id _id;

    public String getId() {
        if (id == null)
            setId(_id.get$id());
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BaseModel() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeParcelable(this._id, 0);
    }

    protected BaseModel(Parcel in) {
        this.id = in.readString();
        this._id = in.readParcelable(Id.class.getClassLoader());
    }

}
