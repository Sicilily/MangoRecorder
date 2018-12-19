package com.sherry.mangorecorder.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sherry.mangorecorder.DBHelper;
import com.sherry.mangorecorder.OnDatabaseChangedListener;
import com.sherry.mangorecorder.R;
import com.sherry.mangorecorder.fragment.PlayFragment;
import com.sherry.mangorecorder.model.Record;

import java.io.File;
import java.util.ArrayList;
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
                ArrayList<String> entrys = new ArrayList<String>();
                entrys.add("分享");
                entrys.add("重命名");
                entrys.add("删除");

                final CharSequence[] items = entrys.toArray(new CharSequence[entrys.size()]);


                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("请选择操作");
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        if (item == 0) {
                            shareFileDialog(holder.getLayoutPosition());
                        } if (item == 1) {
                            renameFileDialog(holder.getLayoutPosition());
                        } else if (item == 2) {
                            deleteFileDialog(holder.getLayoutPosition());
                        }
                    }
                });
                builder.setCancelable(true);
                builder.setNegativeButton("取消",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert = builder.create();
                alert.show();
                return false;
            }
        });
    }

    private void shareFileDialog(int position) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(getItem(position).getFilePath())));
        shareIntent.setType("audio/amr");
        mContext.startActivity(Intent.createChooser(shareIntent, "分享到"));
    }

    private void renameFileDialog(final int position) {
        AlertDialog.Builder renameFileBuilder = new AlertDialog.Builder(mContext);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.dialog_rename, null);

        final EditText input = view.findViewById(R.id.new_name);

        renameFileBuilder.setTitle("请输入新名字");
        renameFileBuilder.setCancelable(true);
        renameFileBuilder.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            String value = input.getText().toString().trim() + ".mp3";
                            rename(position, value);

                        } catch (Exception e) {
                            Log.e(TAG, "exception", e);
                        }

                        dialog.cancel();
                    }
                });
        renameFileBuilder.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        renameFileBuilder.setView(view);
        AlertDialog alert = renameFileBuilder.create();
        alert.show();
    }

    public void rename(int position, String name) {

        String mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFilePath += "/MyRecorder/" + name;
        File f = new File(mFilePath);

        if (f.exists() && !f.isDirectory()) {
            Toast.makeText(mContext, "该名字已存在", Toast.LENGTH_SHORT).show();

        } else {
            //file name is unique, rename file
            File oldFilePath = new File(getItem(position).getFilePath());
            oldFilePath.renameTo(f);
            mDatabase.renameRecord(getItem(position), name, mFilePath);
            notifyItemChanged(position);
        }
    }

    private void deleteFileDialog(final int position) {
        AlertDialog.Builder confirmDelete = new AlertDialog.Builder(mContext);
        confirmDelete.setTitle("提示");
        confirmDelete.setMessage("确定删除吗？");
        confirmDelete.setCancelable(true);
        confirmDelete.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            //remove item from database, recyclerview, and storage
                            remove(position);

                        } catch (Exception e) {
                            Log.e(TAG, "exception", e);
                        }

                        dialog.cancel();
                    }
                });
        confirmDelete.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = confirmDelete.create();
        alert.show();
    }

    public void remove(int position) {

        File file = new File(getItem(position).getFilePath());
        file.delete();

        Toast.makeText(mContext, "已删除", Toast.LENGTH_SHORT).show();

        mDatabase.deleteRecord(getItem(position).getId());
        notifyItemRemoved(position);
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
