package com.sherry.mangorecorder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.sherry.mangorecorder.model.Record;

/**
 * Author: Sherry
 * 作用:
 */
public class DBHelper extends SQLiteOpenHelper {

    private Context mContext;
    private static final String TAG = "DBHelper";
    private static OnDatabaseChangedListener mOnDatabaseChangedListener;

    private static final String DATABASE_NAME = "record.db";

    public static abstract class RecordItem implements BaseColumns {
        private static final String TABLE_NAME = "record";
        private static final String NAME = "record_name";
        private static final String PATH = "record_path";
        private static final String LENGTH = "record_length";
        private static final String TIME = "record_time";
    }
    private static final String CREATE_TABLE = "create table " + RecordItem.TABLE_NAME + "("
            + RecordItem._ID + " integer primary key,"
            + RecordItem.NAME + " text,"
            + RecordItem.PATH + " text,"
            + RecordItem.LENGTH + " integer,"
            + RecordItem.TIME + " integer)";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        mContext = context;
    }

    public static void setOnDatabaseChangedListener(OnDatabaseChangedListener listener) {
        mOnDatabaseChangedListener = listener;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public int getCount() {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {RecordItem._ID};
        Cursor cursor = db.query(RecordItem.TABLE_NAME, projection, null, null, null, null, null);
        int count = cursor.getCount();

        return count;
    }

    public Record getItemAt(int position) {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                RecordItem._ID,
                RecordItem.NAME,
                RecordItem.PATH,
                RecordItem.LENGTH,
                RecordItem.TIME
        };
        Cursor cursor = db.query(RecordItem.TABLE_NAME, projection, null,null,null,null,null);

        if (cursor.moveToPosition(position)) {
            Record record = new Record();
            record.setId(cursor.getInt(cursor.getColumnIndex(RecordItem._ID)));
            record.setName(cursor.getString(cursor.getColumnIndex(RecordItem.NAME)));
            record.setFilePath(cursor.getString(cursor.getColumnIndex(RecordItem.PATH)));
            record.setLength(cursor.getInt(cursor.getColumnIndex(RecordItem.LENGTH)));
            record.setTime(cursor.getLong(cursor.getColumnIndex(RecordItem.TIME)));

            cursor.close();
            return record;
        }
        return null;
    }

    public long addRecord(String name, String path, long length) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(RecordItem.NAME, name);
        cv.put(RecordItem.PATH, path);
        cv.put(RecordItem.LENGTH, length);
        cv.put(RecordItem.TIME, System.currentTimeMillis());

        long rowId = db.insert(RecordItem.TABLE_NAME, null, cv);

        if (mOnDatabaseChangedListener != null) {
            mOnDatabaseChangedListener.onNewDatabaseEntryAdded();
        }

        return rowId;
    }
}
