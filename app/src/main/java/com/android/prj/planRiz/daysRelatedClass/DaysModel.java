package com.android.prj.planRiz.daysRelatedClass;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "dates_tbl")

public class DaysModel implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    private int id = 0;
    private String date;
    private String grDate;


    public DaysModel() {
    }


    protected DaysModel(Parcel in) {
        id = in.readInt();
        date = in.readString();
        grDate = in.readString();
    }


    public static final Creator<DaysModel> CREATOR = new Creator<DaysModel>() {
        @Override
        public DaysModel createFromParcel(Parcel in) {
            return new DaysModel(in);
        }

        @Override
        public DaysModel[] newArray(int size) {
            return new DaysModel[size];
        }
    };

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGrDate() {
        return grDate;
    }

    public void setGrDate(String grDate) {
        this.grDate = grDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeString(date);
        parcel.writeString(grDate);
    }
}
