package com.sherry.mangorecorder.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Author: Sherry
 * 作用:
 */
public class Record implements Parcelable {

    private String Name; // file name
    private String FilePath; //file path
    private int Id; //id in database
    private int Length; // length of recording in seconds
    private long Time; // date/time of the recording

    public Record() {
    }

    public Record(Parcel in) {
        Name = in.readString();
        FilePath = in.readString();
        Id = in.readInt();
        Length = in.readInt();
        Time = in.readLong();
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getFilePath() {
        return FilePath;
    }

    public void setFilePath(String filePath) {
        FilePath = filePath;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public int getLength() {
        return Length;
    }

    public void setLength(int length) {
        Length = length;
    }

    public long getTime() {
        return Time;
    }

    public void setTime(long time) {
        Time = time;
    }

    public static final Creator<Record> CREATOR = new Creator<Record>() {
        @Override
        public Record createFromParcel(Parcel in) {
            return new Record(in);
        }

        @Override
        public Record[] newArray(int size) {
            return new Record[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(Id);
        dest.writeInt(Length);
        dest.writeLong(Time);
        dest.writeString(FilePath);
        dest.writeString(Name);
    }
}

