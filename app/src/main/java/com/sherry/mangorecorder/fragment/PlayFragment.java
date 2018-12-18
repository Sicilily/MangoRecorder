package com.sherry.mangorecorder.fragment;


import android.app.AlertDialog;
import android.app.Dialog;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import com.sherry.mangorecorder.R;
import com.sherry.mangorecorder.model.Record;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlayFragment extends DialogFragment {

    private static final String TAG = "PlayFragment";
    private static final String RECORD_ITEM = "record";

    private TextView fileName;
    private TextView currentTime;
    private TextView totalTime;
    private SeekBar seekBar;
    private FloatingActionButton fab;

    private Record record;
    long minutes = 0;
    long seconds = 0;

    private MediaPlayer mPlayer;
    private boolean isPlaying = false;

    private Handler mHandler = new Handler();

    public static PlayFragment newInstance(Record record) {
        PlayFragment fragment = new PlayFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(RECORD_ITEM, record);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        record = getArguments().getParcelable(RECORD_ITEM);

        long itemDuration = record.getLength();
        minutes = TimeUnit.MILLISECONDS.toMinutes(itemDuration);
        seconds = TimeUnit.MILLISECONDS.toSeconds(itemDuration) - TimeUnit.MINUTES.toSeconds(minutes);

    }

    public PlayFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        Dialog dialog = super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_play, null);

        fileName = view.findViewById(R.id.file_name);
        currentTime = view.findViewById(R.id.current_time);
        totalTime = view.findViewById(R.id.total_time);
        seekBar = view.findViewById(R.id.seekBar);
        fab = view.findViewById(R.id.fab_play);

        //进度条
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(mPlayer != null && fromUser) {
                    mPlayer.seekTo(progress);
                    mHandler.removeCallbacks(mRunnable);

                    long minutes = TimeUnit.MILLISECONDS.toMinutes(mPlayer.getCurrentPosition());
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(mPlayer.getCurrentPosition())
                            - TimeUnit.MINUTES.toSeconds(minutes);
                    currentTime.setText(String.format("%02d:%02d", minutes,seconds));

                    updateSeekBar();

                } else if (mPlayer == null && fromUser) {
                    prepareMediaPlayerFromPoint(progress);
                    updateSeekBar();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if(mPlayer != null) {
                    // remove message Handler from updating progress bar
                    mHandler.removeCallbacks(mRunnable);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mPlayer != null) {
                    mHandler.removeCallbacks(mRunnable);
                    mPlayer.seekTo(seekBar.getProgress());

                    long minutes = TimeUnit.MILLISECONDS.toMinutes(mPlayer.getCurrentPosition());
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(mPlayer.getCurrentPosition())
                            - TimeUnit.MINUTES.toSeconds(minutes);
                    currentTime.setText(String.format("%02d:%02d", minutes,seconds));
                    updateSeekBar();
                }
            }
        });

        fileName.setText(record.getName());
        totalTime.setText(String.format("%02d:%02d", minutes,seconds));

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlay(isPlaying);
                isPlaying = !isPlaying;
            }
        });

        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        builder.setView(view);
        return builder.create();

    }

    private void prepareMediaPlayerFromPoint(int progress) {

        mPlayer = new MediaPlayer();

        try {
            mPlayer.setDataSource(record.getFilePath());
            mPlayer.prepare();
            seekBar.setMax(mPlayer.getDuration());
            mPlayer.seekTo(progress);

            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopPlaying();
                }
            });

        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }

        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    //updating mSeekBar
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if(mPlayer != null){

                int mCurrentPosition = mPlayer.getCurrentPosition();
                seekBar.setProgress(mCurrentPosition);

                long minutes = TimeUnit.MILLISECONDS.toMinutes(mCurrentPosition);
                long seconds = TimeUnit.MILLISECONDS.toSeconds(mCurrentPosition)
                        - TimeUnit.MINUTES.toSeconds(minutes);
                currentTime.setText(String.format("%02d:%02d", minutes, seconds));

                updateSeekBar();
            }
        }
    };

    private void updateSeekBar() {
        mHandler.postDelayed(mRunnable, 1000);
    }

    private void onPlay(boolean isPlaying) {
        if (!isPlaying) {
            if(mPlayer == null) {
                startPlaying();
            } else {
                resumePlaying();
            }
        } else {
            pausePlaying();
        }
    }

    private void startPlaying() {
        fab.setImageResource(R.drawable.ic_media_pause);
        mPlayer = new MediaPlayer();

        try {
            mPlayer.setDataSource(record.getFilePath());
            mPlayer.prepare();
            seekBar.setMax(mPlayer.getDuration());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

        mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mPlayer.start();
            }
        });

        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stopPlaying();
            }
        });
        updateSeekBar();
    }

    private void stopPlaying() {
        fab.setImageResource(R.drawable.ic_media_play);
        mPlayer.stop();
        mPlayer.reset();
        mPlayer.release();
        mPlayer = null;
        isPlaying = !isPlaying;

        seekBar.setProgress(seekBar.getMax());

        currentTime.setText(totalTime.getText());
        seekBar.setProgress(seekBar.getMax());
    }

    private void resumePlaying() {
        fab.setImageResource(R.drawable.ic_media_pause);
        mHandler.removeCallbacks(mRunnable);
        mPlayer.start();
        updateSeekBar();
    }

    private void pausePlaying() {
        fab.setImageResource(R.drawable.ic_media_play);
        mHandler.removeCallbacks(mRunnable);
        mPlayer.pause();

    }

    @Override
    public void onStart() {
        super.onStart();

        //set transparent background
        Window window = getDialog().getWindow();
        window.setBackgroundDrawableResource(android.R.color.transparent);

        //disable buttons from dialog
        AlertDialog alertDialog = (AlertDialog) getDialog();
        alertDialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
        alertDialog.getButton(Dialog.BUTTON_NEGATIVE).setEnabled(false);
        alertDialog.getButton(Dialog.BUTTON_NEUTRAL).setEnabled(false);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mPlayer != null) {
            stopPlaying();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mPlayer != null) {
            stopPlaying();
        }
    }
}
