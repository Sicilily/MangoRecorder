package com.sherry.mangorecorder.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sherry.mangorecorder.DBHelper;
import com.sherry.mangorecorder.OnDatabaseChangedListener;
import com.sherry.mangorecorder.R;
import com.sherry.mangorecorder.fragment.PlayFragment;
import com.sherry.mangorecorder.model.Record;

import java.util.concurrent.TimeUnit;

/**
 * Author: Sherry
 * 作用:
 */
public class RecordListAdapter extends RecyclerView.Adapter<RecordListAdapter.ViewHolder> implements OnDatabaseChangedListener{

    private static final String TAG = "RecordListAdapter";
    private Context mContext;
    private DBHelper mDatabase;
    private Record record;
    private LinearLayoutManager layoutManager;

    public RecordListAdapter(Context mContext, LinearLayoutManager linearLayoutManager) {
        this.mContext = mContext;
        mDatabase = new DBHelper(mContext);
        mDatabase.setOnDatabaseChangedListener(this);
        layoutManager = linearLayoutManager;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_record, viewGroup, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        record = getItem(position);

        long duration = record.getLength();
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MILLISECONDS.toSeconds(minutes);

        holder.mLength.setText(String.format("%02d:%02d", minutes, seconds));
        holder.mName.setText(record.getName());
        holder.mDate.setText(DateUtils.formatDateTime(
                mContext,
                record.getTime(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_YEAR
                )
        );

        //点击播放
        holder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    PlayFragment playFragment = new PlayFragment().newInstance(getItem(holder.getLayoutPosition()));

                    FragmentTransaction transaction = ((FragmentActivity) mContext)
                            .getSupportFragmentManager()
                            .beginTransaction();

                    playFragment.show(transaction, "dialog_play");

                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        });

        //长按选择操作
        holder.mCardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDatabase.getCount();
    }

    public Record getItem(int position) {
        return  mDatabase.getItemAt(position);
    }

    @Override
    public void onNewDatabaseEntryAdded() {
        //处理新数据监听，插入数据
        notifyItemInserted(getItemCount() - 1);
        layoutManager.scrollToPosition(getItemCount() - 1);
    }

    @Override
    public void onDatabaseEntryRenamed() {

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CardView mCardView;
        ImageView mImage;
        TextView mName;
        TextView mLength;
        TextView mDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mCardView = itemView.findViewById(R.id.card_view);
            mImage = itemView.findViewById(R.id.file_image);
            mName = itemView.findViewById(R.id.file_name);
            mLength = itemView.findViewById(R.id.file_length);
            mDate = itemView.findViewById(R.id.file_date);
        }
    }
}
