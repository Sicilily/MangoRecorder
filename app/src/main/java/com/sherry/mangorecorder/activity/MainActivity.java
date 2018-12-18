package com.sherry.mangorecorder.activity;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.astuetz.PagerSlidingTabStrip;
import com.sherry.mangorecorder.R;
import com.sherry.mangorecorder.fragment.RecordFragment;
import com.sherry.mangorecorder.fragment.RecordListFragment;

public class MainActivity extends AppCompatActivity {

    private ViewPager pager;
    private PagerSlidingTabStrip tabs;

    //申请录音权限
    private static final int GET_RECODE_AUDIO = 1;
    private static String[] PERMISSION_AUDIO = { Manifest.permission.RECORD_AUDIO };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pager = findViewById(R.id.pager);
        pager.setAdapter(new MyFragmentAdapter(getSupportFragmentManager()));

        tabs = findViewById(R.id.tabs);
        tabs.setViewPager(pager);

        verifyAudioPermissions(this);
    }

    /** 申请录音权限 */
    public static void verifyAudioPermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, PERMISSION_AUDIO,
                    GET_RECODE_AUDIO);
        }
    }

    public class MyFragmentAdapter extends FragmentPagerAdapter {

        private String[] titles = {"录音", "列表"};

        public MyFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return RecordFragment.newInstance(position);
                case 1:
                    return RecordListFragment.newInstance(position);
            }
            return null;
        }

        @Override
        public int getCount() {
            return titles.length;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }
}
