package com.sinosoft.mycontactexporttest;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mars on 2017/6/30.
 */

public class ContactEntity implements Parcelable {
    public ContactEntity() {
    }

    private String name;
    private ArrayList<TelephoneEntity> telephoneEntities;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public ArrayList<TelephoneEntity> getTelephoneEntities() {
        return telephoneEntities;
    }

    public void setTelephoneEntities(ArrayList<TelephoneEntity> telephoneEntities) {
        this.telephoneEntities = telephoneEntities;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeList(telephoneEntities);
    }

    public static final Parcelable.Creator<ContactEntity> CREATOR = new Creator<ContactEntity>() {
        @Override
        public ContactEntity createFromParcel(Parcel source) {
            return new ContactEntity(source);
        }

        @Override
        public ContactEntity[] newArray(int size) {
            return new ContactEntity[size];
        }
    };

    private ContactEntity(Parcel source) {
        //这里read字段的顺序要与write的顺序一致
        name = source.readString();
        telephoneEntities = source.readArrayList(TelephoneEntity.class.getClassLoader());
    }
}
