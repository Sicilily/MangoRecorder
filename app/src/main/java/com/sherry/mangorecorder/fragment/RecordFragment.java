package com.sherry.mangorecorder.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.sherry.mangorecorder.R;
import com.sherry.mangorecorder.service.RecordService;

import java.io.File;


public class RecordFragment extends Fragment {

    private static final String TAG = "RecordFragment";
    private Chronometer chronometer;
    private FloatingActionButton fab;
    private TextView tips;

    private int position;
    private boolean isStart = true;
    private int mRecordPromptCount = 0;

    public RecordFragment() {
        // Required empty public constructor
    }

    public static RecordFragment newInstance(int position) {
        RecordFragment fragment = new RecordFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt("position");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record, container, false);
        chronometer = view.findViewById(R.id.chronometer);
        fab = view.findViewById(R.id.fab);
        tips = view.findViewById(R.id.tv_tips);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecording();
                isStart = !isStart;
            }
        });
        return view;
    }

    private void startRecording() {
        Intent intent = new Intent(getActivity(), RecordService.class);

        if (isStart) {
            fab.setImageResource(R.drawable.ic_media_stop);
            Toast.makeText(getActivity(), "开始录音", Toast.LENGTH_SHORT).show();

            File folder = new File(Environment.getExternalStorageDirectory() + "/MyRecorder");
            if (!folder.exists()) {
                //folder /SoundRecorder doesn't exist, create the folder
                folder.mkdir();
            }

            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();
            chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                @Override
                public void onChronometerTick(Chronometer chronometer) {
                    if (mRecordPromptCount == 0) {
                        tips.setText("正在录音" + ".");
                    } else if (mRecordPromptCount == 1) {
                        tips.setText("正在录音" + "..");
                    } else if (mRecordPromptCount == 2) {
                        tips.setText("正在录音" + "...");
                        mRecordPromptCount = -1;
                    }
                    mRecordPromptCount++;
                }
            });

            getActivity().startService(intent);
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            tips.setText("正在录音" + ".");
            mRecordPromptCount++;

        } else {
            fab.setImageResource(R.drawable.ic_mic_white_36dp);
            chronometer.stop();
            chronometer.setBase(SystemClock.elapsedRealtime());
            tips.setText("点击开始录音");

            getActivity().stopService(intent);
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

}
