package com.sherry.mangorecorder.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.sherry.mangorecorder.DBHelper;

import java.io.File;
import java.io.IOException;

public class RecordService extends Service {

    private static final String TAG = "RecordService";

    private MediaRecorder mRecorder;
    private long startTime = 0;
    private long totalTime = 0;

    private String mFileName = null;
    private String mFilePath = null;

    private DBHelper mDatabase;

    public RecordService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDatabase = new DBHelper(getApplicationContext());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startRecord();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mRecorder != null) {
            stopRecord();
        }
        super.onDestroy();
    }

    private void startRecord() {
        setFileNameAndPath();

        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_WB);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
        mRecorder.setOutputFile(mFilePath);

        try {
            mRecorder.prepare();
            mRecorder.start();
            startTime = System.currentTimeMillis(); //设置开始时间为当前系统时间
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void setFileNameAndPath() {
        int count = 0;
        File file;

        do {
            count ++;
            mFileName = "MyRecord" + "_" + (mDatabase.getCount() + count) + ".amr";
            mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            mFilePath += "/MyRecorder/" + mFileName;
            file = new File(mFileName);

        } while (file.exists() && !file.isDirectory());
    }

    private void stopRecord() {
        mRecorder.stop();
        totalTime = (System.currentTimeMillis() - startTime);
        mRecorder.release();

        Toast.makeText(this, "录音文件已保存在" + mFilePath + "中", Toast.LENGTH_SHORT).show();
        mRecorder = null;

        try {
            mDatabase.addRecord(mFileName, mFilePath, totalTime);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

    }
}
