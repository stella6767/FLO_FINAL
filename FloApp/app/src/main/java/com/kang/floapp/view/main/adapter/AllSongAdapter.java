package com.kang.floapp.view.main.adapter;


import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.kang.floapp.R;
import com.kang.floapp.model.Song;
import com.kang.floapp.utils.CustomListViewDialog;
import com.kang.floapp.utils.eventbus.SongIdPassenger;
import com.kang.floapp.utils.eventbus.SongPassenger;
import com.kang.floapp.utils.notification.CreateNotification;
import com.kang.floapp.view.common.Constants;
import com.kang.floapp.view.main.MainActivity;
import com.kang.floapp.view.main.frag.home.FragHome;
import com.kang.floapp.view.main.frag.storage.FragStorage;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class AllSongAdapter extends RecyclerView.Adapter<AllSongAdapter.MyViewHolder> {

    private static final String TAG = "AllSongAdapter";
    private MainActivity mainActivity;
    public List<Song> songList = new ArrayList<>();
    private StorageAdapter storageAdapter;
    private TextView tvFloChart;

    public AllSongAdapter() { }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }


    public void setMusics(List<Song> songList) {
        this.songList = songList;
        notifyDataSetChanged();
    }


    public void setCount(TextView tvFloChart){
        this.tvFloChart = tvFloChart;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.song_item, parent, false);


        return new MyViewHolder(view); //view??? ??????????????? ?????? ?????????.
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        tvFloChart.setText("FLO ?????? "+ (getItemCount()+"")+"???");

        holder.setItem(songList.get(position));

    }

    @Override
    public int getItemCount() {
        return songList.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView tvSongArtist;
        private TextView tvSongTitle;
        private TextView tvSongId;
        private ImageView ivSongPlay;
        private ImageView ivSongArt;
        private ImageView ivStorageAddBtn1;
        private CustomListViewDialog customDialog;

        public MyViewHolder(@NonNull View itemView) {

            super(itemView);
            tvSongArtist = itemView.findViewById(R.id.tv_song_artist);
            tvSongTitle = itemView.findViewById(R.id.tv_song_title);
            tvSongId = itemView.findViewById(R.id.tv_song_Id);
            ivSongPlay = itemView.findViewById(R.id.iv_song_play);
            ivSongArt = itemView.findViewById(R.id.iv_song_art);
            ivStorageAddBtn1 = itemView.findViewById(R.id.iv_storage_add_btn1);


            ivStorageAddBtn1.setOnClickListener(v -> {

                storageAdapter = mainActivity.storageAdapter;
                Song song = songList.get(getAdapterPosition());
                Log.d(TAG, "MyViewHolder: add ?????? ?????????: " + song.getTitle());

                mainActivity.dialogAdapter.transSong(song);
                customDialog = new CustomListViewDialog(mainActivity, mainActivity.dialogAdapter);
                customDialog.show();
                customDialog.setCanceledOnTouchOutside(false);

                //((MainActivity)v.getContext()).replace(new FragStorage());

            });



            ivSongPlay.setOnClickListener(v -> {
                EventBus.getDefault().post(new SongPassenger(songList.get(getAdapterPosition()))); //??????????????? ????????? ??? ??????

            });

        }

        public void setItem(Song song) {

            if (song != null) {
                tvSongTitle.setText(song.getTitle());
                tvSongArtist.setText(song.getArtist());
                tvSongId.setText(song.getId().toString());

                String imgUrl = mainActivity.getImageUrl(song.getImg());

                Glide //?????? ???????????? ??? ????????? ???????????? ??????(??????)
                        .with(itemView)
                        .load(imgUrl)
                        .centerCrop()
                        .placeholder(R.drawable.ic_launcher_background)
                        .into(ivSongArt);
            }

        }


    }
}
