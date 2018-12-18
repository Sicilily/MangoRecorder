package com.sherry.mangorecorder.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sherry.mangorecorder.R;
import com.sherry.mangorecorder.adapter.RecordListAdapter;


public class RecordListFragment extends Fragment {

    private static final String TAG = RecordFragment.class.getSimpleName();

    private int position;
    private TextView tip;
    private RecyclerView recyclerView;
    private RecordListAdapter adapter;
    private LinearLayoutManager layoutManager;

    public RecordListFragment() {
        // Required empty public constructor
    }

    public static RecordListFragment newInstance(int position) {
        RecordListFragment fragment = new RecordListFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt("position");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record_list, container, false);
        tip = view.findViewById(R.id.tv_tip);
        recyclerView = view.findViewById(R.id.recycleView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        //newest to oldest order (database stores from oldest to newest)
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        adapter = new RecordListAdapter(getActivity(), layoutManager);
        recyclerView.setAdapter(adapter);

        return view;
    }

}
