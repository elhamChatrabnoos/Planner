package com.android.prj.planRiz.workRelatedClass;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity(tableName = "works_tbl")

public class WorkModel implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private String work_name;
    private boolean done;
    private boolean undone;
    private String alarm;
    private String workDate;
    private boolean alarmOn;

    public WorkModel() {
    }



    protected WorkModel(Parcel in) {
        id = in.readLong();
        work_name = in.readString();
        done = in.readByte() != 0;
        alarmOn = in.readByte() != 0;
        undone = in.readByte() != 0;
        alarm = in.readString();
        workDate = in.readString();
    }

    public static final Creator<WorkModel> CREATOR = new Creator<WorkModel>() {
        @Override
        public WorkModel createFromParcel(Parcel in) {
            return new WorkModel(in);
        }

        @Override
        public WorkModel[] newArray(int size) {
            return new WorkModel[size];
        }
    };

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public boolean isUndone() {
        return undone;
    }

    public void setUndone(boolean undone) {
        this.undone = undone;
    }

    public String getAlarm() {
        return alarm;
    }

    public void setAlarm(String alarm) {
        this.alarm = alarm;
    }

    public static Creator<WorkModel> getCREATOR() {
        return CREATOR;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getWork_name() {
        return work_name;
    }

    public void setWork_name(String work_name) {
        this.work_name = work_name;
    }

    public String getWorkDate() {
        return workDate;
    }

    public void setWorkDate(String workDate) {
        this.workDate = workDate;
    }

    public boolean isAlarmOn() {
        return alarmOn;
    }

    public void setAlarmOn(boolean alarmOn) {
        this.alarmOn = alarmOn;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeString(work_name);
        parcel.writeByte((byte) (done ? 1 : 0));
        parcel.writeByte((byte) (undone ? 1 : 0));
        parcel.writeByte((byte) (alarmOn ? 1 : 0));
        parcel.writeString(alarm);
        parcel.writeString(workDate);
    }
}
