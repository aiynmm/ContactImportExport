package com.sinosoft.mycontactexporttest;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Mars on 2017/7/6.
 */

public class TelephoneEntity implements Parcelable{

    public TelephoneEntity() {
    }

    private String number;
    private int phoneType;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public int getPhoneType() {
        return phoneType;
    }

    public void setPhoneType(int phoneType) {
        this.phoneType = phoneType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(number);
        dest.writeInt(phoneType);
    }

    public static final Parcelable.Creator<TelephoneEntity> CREATOR = new Creator<TelephoneEntity>() {
        @Override
        public TelephoneEntity createFromParcel(Parcel source) {
            return new TelephoneEntity(source);
        }

        @Override
        public TelephoneEntity[] newArray(int size) {
            return new TelephoneEntity[size];
        }
    };

    private TelephoneEntity(Parcel source) {
        //这里read字段的顺序要与write的顺序一致
        number=source.readString();
        phoneType=source.readInt();
    }
}
