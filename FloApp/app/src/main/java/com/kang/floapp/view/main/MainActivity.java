package com.kang.floapp.view.main;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.kang.floapp.R;
import com.kang.floapp.model.PlaySong;
import com.kang.floapp.model.Song;
import com.kang.floapp.model.Storage;
import com.kang.floapp.model.StorageSong;
import com.kang.floapp.model.User;
import com.kang.floapp.model.dto.PlaySongSaveReqDto;
import com.kang.floapp.utils.SharedPreference;
import com.kang.floapp.utils.callback.AddCallback;
import com.kang.floapp.utils.PlayService;
import com.kang.floapp.utils.eventbus.NotificationBus;
import com.kang.floapp.utils.eventbus.SongIdPassenger;
import com.kang.floapp.utils.eventbus.SongPassenger;
import com.kang.floapp.utils.eventbus.UrlPassenger;
import com.kang.floapp.utils.notification.CreateNotification;
import com.kang.floapp.view.common.Constants;
import com.kang.floapp.view.main.adapter.AllSongAdapter;
import com.kang.floapp.view.main.adapter.CategoryListAdapter;
import com.kang.floapp.view.main.adapter.DialogAdapter;
import com.kang.floapp.view.main.adapter.PlayListAdapter;
import com.kang.floapp.view.main.adapter.StorageAdapter;
import com.kang.floapp.view.main.adapter.StorageSongAdapter;
import com.kang.floapp.view.main.frag.home.FragHome;
import com.kang.floapp.view.main.frag.FragPlaylist;
import com.kang.floapp.view.main.frag.search.FragSearch;
import com.kang.floapp.view.main.frag.storage.FragStorage;
import com.kang.floapp.view.main.frag.FragTour;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;


//????????? Kang8 Branch
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity2";
    //private MainActivity mContext = MainActivity.this;
    private Context mContext = MainActivity.this;
    public int playlistChange = 1;


    NotificationManager notificationManager;

    //?????????
    public CategoryListAdapter categoryListAdapter;
    public AllSongAdapter allSongAdapter;
    public PlayListAdapter playListAdapter;
    public StorageAdapter storageAdapter;
    public DialogAdapter dialogAdapter;
    public StorageSongAdapter storageSongAdapter;


    //??????
    public MediaPlayer mp;
    private PlayService playService;
    public MainActivityViewModel mainViewModel;
    private BottomNavigationView bottomNav;
    private Handler handler = new Handler();
    public Thread uiHandleThread;


    //playView slidng panel
    public TextView tvCurrentTime;
    public TextView tvTotalTime;
    public ImageView ivPlayViewBar;
    public SeekBar playViewSeekBar;
    public TextView tvPlayViewTitle;
    public TextView tvPlayViewArtist;
    public ImageView ivPlayViewPrev;
    public ImageView ivPlayViewNext;
    public TextView tvLyrics;
    public ImageView ivPlayViewArt;
    public ImageView ivRepeat;
    public ImageView ivRandome;
    public TextView tvRelaseDate;


    // ??? ?????? ?????? ????????????
    public SeekBar mainSeekbar;
    public ImageView ivBarPlay;
    public TextView tvTitle;
    public TextView tvArtist;
    public ImageView ivPrev;
    public ImageView ivNext;
    public ImageView ivSelect;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        initView();
        dataObserver();
        serviceObservers();
        createChannel();


        //new??? ?????? ????????? playlist ???????????? ???????????? ????????? ???.
        Fragment playlistFrag = new FragPlaylist(mp, playListAdapter, mainViewModel, mContext);


        listner();


        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FragHome()).commit(); //?????? ??????(???????????????)
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            switch (item.getItemId()) {
                case R.id.bottom_home:
                    selectedFragment = new FragHome();
                    break;
                case R.id.bottom_tour:
                    selectedFragment = new FragTour();
                    break;
                case R.id.bottom_search:
                    selectedFragment = new FragSearch();
                    break;
                case R.id.bottom_storage:
                    selectedFragment = new FragStorage();
                    break;

            }
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit(); //??????????????? ????????????
            return true;
        });

        ivPlayViewNext.setOnClickListener(this::onClick);
        ivNext.setOnClickListener(this::onClick);
        ivPrev.setOnClickListener(this::onClick);
        ivPlayViewPrev.setOnClickListener(this::onClick);
        ivBarPlay.setOnClickListener(this::onClick);
        ivPlayViewBar.setOnClickListener(this::onClick);

        ivSelect.setOnClickListener(v -> { //??????????????? ??????
            playlistChange = playlistChange * -1;
            if (playlistChange == -1) {
                getSupportFragmentManager().beginTransaction().addToBackStack("").replace(R.id.fragment_container, playlistFrag).commit();
            } else {
                getSupportFragmentManager().popBackStack();
            }
        });


        ivRepeat.setOnClickListener(v -> {
            playRepeat();
        });

        ivRandome.setOnClickListener(v -> {
            playRandom();
        });

        seekBarInit();

    }

    public void createChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(CreateNotification.CHANNEL_ID, "park", NotificationManager.IMPORTANCE_LOW);

            notificationManager = getSystemService(NotificationManager.class);
            if(notificationManager != null){
                notificationManager.createNotificationChannel(channel);
            }

        }
    }

    public void dataObserver() {

        mainViewModel.subscribe().observe(this, new Observer<List<Song>>() {
            @Override
            public void onChanged(List<Song> songs) {
                allSongAdapter.setMusics(songs);
            }
        });


        mainViewModel.PlayListSubscribe().observe(this, playSongs -> {
           playListAdapter.setMySongList(playSongs);
        });

        User user = userValidaionCheck();

        if(user == null){
            Toast.makeText(mContext, "????????? ?????????????????????. ?????? ????????????????????????.", Toast.LENGTH_SHORT).show();
        }else{
            mainViewModel.findPlaylist(user.getId());
        }

        mainViewModel.categoryListSubscribe().observe(this, new Observer<List<Song>>() {
            @Override
            public void onChanged(List<Song> songs) {
                categoryListAdapter.setMusics(songs);
            }
        });


        mainViewModel.storageListSubscribe().observe(this, new Observer<List<Storage>>() {
            @Override
            public void onChanged(List<Storage> storages) {
                Log.d(TAG, "onChanged: ??? ???????????? ?????? ??????.");
                storageAdapter.setStorage(storages);  //?????? ??? ??????
                dialogAdapter.setStorage(storages);
            }
        });

        mainViewModel.findStorage(); //storage??? ????????? get?????? ????????? ??????

        mainViewModel.storageSongListSubscribe().observe(this, new Observer<List<StorageSong>>() {
            @Override
            public void onChanged(List<StorageSong> storageSongs) {
                storageSongAdapter.setStorageSong(storageSongs);
            }
        });



    }

    public String getSongUrl(String file){
        return Constants.BASEURL + Constants.FILEPATH + file;
    }

    public String getImageUrl(String image){
        return Constants.BASEURL + Constants.IMAGEPATH + image;
    }


    public User userValidaionCheck(){
        Gson gson = new Gson();
        String principal = SharedPreference.getAttribute((MainActivity)mContext, "principal");
        Log.d(TAG, "onCreateView: ??????" + principal);
        User user = gson.fromJson(principal, User.class);
        Log.d(TAG, "onCreateView: ????" + user);

        return user;
    }


    public void playRandom(){
        Random random = new Random();

        if(playListAdapter.playList != null) {
            int randomsong = random.nextInt(playListAdapter.getItemCount());
            Log.d(TAG, "playRandom: " + randomsong);
            Toast.makeText(mContext, "??? ???????????? ??? ????????? ????????? ??????????????????", Toast.LENGTH_SHORT).show();
            ??????????????????(playListAdapter.playList.get(randomsong).getSong());
            Constants.prevNext = randomsong;
            String songUrl = getSongUrl(playListAdapter.playList.get(randomsong).getSong().getFile());
            setSongText();
            EventBus.getDefault().post(new UrlPassenger(songUrl, Constants.isPlaying));
        }
    }


    public void nextORPrevClick(int nextOrPrev) { //???????????? ?????????, ????????? ??????

        Log.d(TAG, "nextORPrevClick: " + nextOrPrev + ", " + Constants.prevNext);

        if (nextOrPrev == 1 && Constants.prevNext < playListAdapter.getItemCount()) {  //1=next, ??? ??? prev
            Log.d(TAG, "?????? nextORPrevClick: " + Constants.prevNext + "    " + nextOrPrev);
            Constants.prevNext = Constants.prevNext + 1;
            Log.d(TAG, "onCreate: songPosition" + Constants.prevNext + "    " + nextOrPrev);

            if (Constants.prevNext < playListAdapter.getItemCount()) {
                setSongText();
            }

        } else if (nextOrPrev == -1 && Constants.prevNext >= 0) {
            Constants.prevNext = Constants.prevNext - 1;
            Log.d(TAG, "onCreate: songPosition" + Constants.prevNext);

            if (Constants.prevNext >= 0) {
                setSongText();
            }

        }
    }


    public void setSongText() {

        Constants.nowSong = playListAdapter.playList.get(Constants.prevNext).getSong();

        tvTitle.setText(playListAdapter.playList.get(Constants.prevNext).getSong().getTitle()); //?????? ????????? ???????????? ??????, ???????????? ????????????.
        tvArtist.setText(playListAdapter.playList.get(Constants.prevNext).getSong().getArtist());
        tvPlayViewArtist.setText(playListAdapter.playList.get(Constants.prevNext).getSong().getArtist());
        tvPlayViewTitle.setText(playListAdapter.playList.get(Constants.prevNext).getSong().getTitle());
        tvLyrics.setText(playListAdapter.playList.get(Constants.prevNext).getSong().getLyrics());
        tvRelaseDate.setText(playListAdapter.playList.get(Constants.prevNext).getSong().getRelaseDate());


        String imageUrl = getImageUrl(playListAdapter.playList.get(Constants.prevNext).getSong().getImg());

        Glide
                .with(mContext)
                .load(imageUrl)
                .centerCrop()
                .placeholder(R.drawable.ic_launcher_background)
                .into(ivPlayViewArt);


        String songUrl = getSongUrl(playListAdapter.playList.get(Constants.prevNext).getSong().getFile());


        Glide.with((MainActivity)mContext)
                .asBitmap().load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .listener(new RequestListener<Bitmap>() {
                              @Override
                              public boolean onLoadFailed(@Nullable GlideException e, Object o, Target<Bitmap> target, boolean b) {
                                  Log.d(TAG, "onLoadFailed: ??????" + e.getMessage());
                                  return false;
                              }

                              @Override
                              public boolean onResourceReady(Bitmap bitmap, Object o, Target<Bitmap> target, DataSource dataSource, boolean b) {
                                  Log.d(TAG, "?????????????????????0 => " + bitmap);
                                  Constants.songBitmap = bitmap;
                                  CreateNotification.createNotificaion((MainActivity)mContext,Constants.nowSong, bitmap, R.drawable.ic_glyph_solid_pause);
                                  return false;
                              }
                          }
                ).submit();

        Log.d(TAG, "setSongText: " + songUrl);
        EventBus.getDefault().post(new UrlPassenger(songUrl, Constants.isPlaying)); //???????????? ?????? or ?????? ??? ??????
    }


    public void setTotalDuration() {
        Integer totalTime = mp.getDuration();

        int m = totalTime / 60000;
        int s = (totalTime % 60000) / 1000;
        String strTime = String.format("%02d:%02d", m, s);

        tvTotalTime.setText(strTime);
    }


    private void listner() { //????????? ????????? ???????????? ??????

        mainSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // ????????? SeekBar??? ????????? ???
                if (fromUser) {
                    mp.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        playViewSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (fromUser) {
                    mp.seekTo(progress);
                }
                int m = progress / 60000;
                int s = (progress % 60000) / 1000;
                String strTime = String.format("%02d:%02d", m, s);
                tvCurrentTime.setText(strTime);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

    }


    private void initView() {

        mainViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);

        //????????? ??????
        allSongAdapter = new AllSongAdapter();
        categoryListAdapter = new CategoryListAdapter();
        playListAdapter = new PlayListAdapter(mContext);//My ??????????????????
        storageAdapter = new StorageAdapter((MainActivity)mContext, mainViewModel);
        dialogAdapter = new DialogAdapter((MainActivity)mContext, mainViewModel);
        storageSongAdapter = new StorageSongAdapter((MainActivity)mContext, mainViewModel);

        //????????????????????? ??????
        bottomNav = findViewById(R.id.bottom_navigation);


        mainSeekbar = findViewById(R.id.mainSeekBar);
        tvTitle = findViewById(R.id.tv_title);
        tvArtist = findViewById(R.id.tv_artist);
        ivNext = findViewById(R.id.iv_next);
        ivPrev = findViewById(R.id.iv_prev);
        ivBarPlay = findViewById(R.id.iv_bar_play);
        tvCurrentTime = findViewById(R.id.tv_current_time);
        tvTotalTime = findViewById(R.id.tv_total_time);
        ivSelect = findViewById(R.id.iv_select);

        ivPlayViewBar = findViewById(R.id.iv_play_view_bar);
        playViewSeekBar = findViewById(R.id.playViewSeekBar);
        tvPlayViewTitle = findViewById(R.id.tv_playView_title);
        tvPlayViewArtist = findViewById(R.id.tv_playView_artist);
        ivPlayViewPrev = findViewById(R.id.iv_playView_prev);
        ivPlayViewNext = findViewById(R.id.iv_playView_next);
        tvLyrics = findViewById(R.id.tv_lyrics);
        ivPlayViewArt = findViewById(R.id.ivPlayViewArt);
        ivRepeat = findViewById(R.id.iv_repeat);
        ivRandome = findViewById(R.id.iv_random);
        tvRelaseDate = findViewById(R.id.tv_relasedate);

    }



    public void playRepeat(){
        Constants.isRepeat = Constants.isRepeat * -1;

        if(Constants.isRepeat == 1) {
            Toast.makeText(mContext, "?????? ??????????????? ????????????.", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(mContext, "?????? ??????????????? ????????????.", Toast.LENGTH_SHORT).show();
        }
        Log.d(TAG, "playRepeat: isRepeat: " + Constants.isRepeat);
    }






    public void seekBarUiHandle() {

        uiHandleThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (Constants.isPlaying == 1) {

                    handler.post(new Runnable() {// runOnUiThread??? ??????, ?????? ????????? ?????? uiHandleThread ???????????? ??????????????? ????????????
                        @Override //UI ???????????? ?????? ?????? ??????????????? ???????????? ??????
                        public void run() {
                            mainSeekbar.setProgress(mp.getCurrentPosition());
                            //((MainActivity) getActivity()).playViewSeekBar.setProgress(mp.getCurrentPosition()); // ????????? ???????????? ??????
                            playViewSeekBar.setProgress(mp.getCurrentPosition());

                            if (mp.getCurrentPosition() >= mp.getDuration() && Constants.isRepeat == -1) {
                                mp.setLooping(false);
                                //songStop();
                                songAgain();
                            }else if(mp.getCurrentPosition() >= mp.getDuration() && Constants.isRepeat == 1){
                                mp.setLooping(true);
                                Log.d(TAG, "run: ????????????");
                            }
                        }

                    });

                    try {
                        Thread.sleep(1000);
                        //Log.d(TAG, "run: 33333333");
                        if (Constants.threadStatus) {
                            //Log.d(TAG, "run: 222222222");
                            uiHandleThread.interrupt(); //??? ?????? ????????? ??????????????? ??????(????????????), sleep??? ????????? ????????? ??????. ???????????? ??????????????? ????????? ?????????
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        //Log.d(TAG, "run: adadsasdda");
                    }

                }
            }
        });

        uiHandleThread.start();

    }


    public void seekBarInit() {
        mainSeekbar.setMax(100000);
        mainSeekbar.setProgress(0);
        playViewSeekBar.setProgress(0);
    }


    public void songPause() {

        if( playListAdapter != null) {
            CreateNotification.createNotificaion((MainActivity) mContext, Constants.nowSong, Constants.songBitmap, R.drawable.ic_glyph_solid_play);
        }

        mp.pause();
        Constants.isPlaying = -1;
        ivBarPlay.setImageResource(android.R.drawable.ic_media_play);
        ivPlayViewBar.setImageResource(android.R.drawable.ic_media_play);
    }


    public void songAgain(){
        mp.seekTo(0);
        mainSeekbar.setProgress(0);
        mp.pause();
        Constants.isPlaying = -1;
        ivBarPlay.setImageResource(android.R.drawable.ic_media_play);
        ivPlayViewBar.setImageResource(android.R.drawable.ic_media_play);
    }



    public void songStop() {
        mp.reset();
        mp.seekTo(0);
        mainSeekbar.setProgress(0);
        Constants.threadStatus = true;
        ivBarPlay.setImageResource(android.R.drawable.ic_media_play);
        ivPlayViewBar.setImageResource(android.R.drawable.ic_media_play);
        Constants.isPlaying = -1;
    }


    public void songPlay() {

        if( playListAdapter != null) {
            CreateNotification.createNotificaion((MainActivity) mContext, Constants.nowSong, Constants.songBitmap, R.drawable.ic_glyph_solid_pause);
        }

        mainSeekbar.setMax(mp.getDuration());
        playViewSeekBar.setMax(mp.getDuration());


        Log.d(TAG, "songPlay: why???");
        Constants.isPlaying = 1;
        setTotalDuration();
        ivBarPlay.setImageResource(android.R.drawable.ic_media_pause);
        ivPlayViewBar.setImageResource(android.R.drawable.ic_media_pause);

        mp.start();
        seekBarUiHandle();
    }


    public void onPrepared(String songUrl) throws IOException { //?????? ????????? ????????????


        mp.reset();
        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() { //??? ?????? ????????????
            @Override
            public void onPrepared(MediaPlayer mp) {
                //EventBus.getDefault().post(new SongEvent(songUrl, mainActivity.isPlaying));
                songPlay();
            }
        });
        mp.setDataSource(songUrl);
        mp.prepareAsync();
    }


    public void playBtnListner() {

//        CreateNotification.createNotificaion() = R.drawable.ic_glyph_solid_pause;
        if (Constants.isPlaying == 1) {
            Log.d(TAG, "onCreate: ????????? ?????? ???????????? ????????????" + Constants.isPlaying);
            songPause();
        } else {
            Log.d(TAG, "onCreate: ????????????" + Constants.isPlaying);
            songPlay();
        }
    }

    public void ??????????????????(Song song) {
        tvTitle.setText(song.getTitle());
        tvArtist.setText(song.getArtist());
        tvPlayViewArtist.setText(song.getArtist());
        tvPlayViewTitle.setText(song.getTitle());
        tvLyrics.setText(song.getLyrics());
        tvRelaseDate.setText(song.getRelaseDate());
    }

    public void Replace(Fragment fragment) { //???????????? ??????, fragment ??????

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction().addToBackStack("");
        fragmentTransaction.replace(R.id.fragment_container, fragment).commit();
    }


    public void backFragment(){
        getSupportFragmentManager().popBackStack();
    }



    public String getNowTime() {
        long lNow;
        Date dt;
        SimpleDateFormat sdfFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        lNow = System.currentTimeMillis();
        dt = new Date(lNow);
        return sdfFormat.format(dt);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void songPrepare(UrlPassenger urlPassenger) throws IOException {
        seekBarInit();
        Log.d(TAG, "songPrepare: url ??????");

        Constants.isPlaying = Constants.isPlaying * -1;
        Log.d(TAG, "songPlay: Song ??????");
        onPrepared(urlPassenger.songUrl);

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void nextSong(SongIdPassenger songIdPassenger) {  // ?????? private?????? ??????. eventbus??? public method???!!
        Log.d(TAG, "nextSong: " + songIdPassenger.songId);
        Constants.prevNext = songIdPassenger.songId; //????????? songId??? ?????? listposition??? ???????????????..
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void playlistAdd(SongPassenger songPassenger) {
        //Log.d(TAG, "playlistAdd: ??? ??????????????? song ??????" + songPassenger.song);

        //Constants.prevNext = songPassenger.song.getId();

        Constants.nowSong = songPassenger.song;


        Song song = songPassenger.song;

        String imageUrl = getImageUrl(song.getImg());

        Glide //?????? ???????????? ??? ????????? ???????????? ??????(??????)
                .with((MainActivity)mContext)
                .load(imageUrl)
                .centerCrop()
                .placeholder(R.drawable.ic_launcher_background)
                .into(ivPlayViewArt);


        Glide.with((MainActivity)mContext)
                .asBitmap().load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .listener(new RequestListener<Bitmap>() {
                              @Override
                              public boolean onLoadFailed(@Nullable GlideException e, Object o, Target<Bitmap> target, boolean b) {
                                  Log.d(TAG, "onLoadFailed: ??????" + e.getMessage());
                                  return false;
                              }

                              @Override
                              public boolean onResourceReady(Bitmap bitmap, Object o, Target<Bitmap> target, DataSource dataSource, boolean b) {
                                  Log.d(TAG, "?????????????????????0 => " + bitmap);
                                  Constants.songBitmap = bitmap;
                                  CreateNotification.createNotificaion((MainActivity)mContext, song, bitmap, R.drawable.ic_glyph_solid_pause);

                                  return false;
                              }
                          }
                ).submit();







        String songUrl = getSongUrl(songPassenger.song.getFile());
        Log.d(TAG, "playlistAdd: songUrl: " + songUrl);


        ??????????????????(songPassenger.song);

        //EventBus.getDefault().post(new UrlPassenger(songUrl, Constants.isPlaying));

        User user = userValidaionCheck();

        if(user != null){
            Log.d(TAG, "playlistAdd: "+user);

            mainViewModel.addAndCallbackPlaysong(new PlaySongSaveReqDto(user, songPassenger.song), (MainActivity)mContext);



//            mainViewModel.addAndCallbackPlaysong(new PlaySongSaveReqDto(user,songPassenger.song), (MainActivity)mContext, new AddCallback<PlaySong>(){ //??????????????? ????????????.
//                @Override
//                public void onSucess(PlaySong playSong) {
//                    int result = playListAdapter.addSong(playSong);
//                    if (result == 1) {
//                        Toast.makeText(mContext, "??????????????? ?????? ?????????????????????.", Toast.LENGTH_SHORT).show();
//                    }else if(result == -1){
//                        String songUrl = getSongUrl(playSong.getSong().getFile());
//                        Log.d(TAG, "onSucess: songUrl: " + songUrl);
//
//                        if (playListAdapter.playList != null) {
//                            for (PlaySong play : playListAdapter.playList) {
//                                if (playSong.getSong().getId() == play.getSong().getId()) {
//                                    EventBus.getDefault().post(new SongIdPassenger(play.getId()-1));
//                                    Log.d(TAG, "addSong: ?????????" + play.getId());
//                                }
//                            }
//                        }
//
//                        EventBus.getDefault().post(new UrlPassenger(songUrl, Constants.isPlaying));
//                    }
//                }
//
//                @Override
//                public void onFailure() {
//                    Log.d(TAG, "onFailure: ??????...");
//                }
//            });


        }else{

            Toast.makeText((MainActivity)mContext, "??? ??????????????? ?????? ????????? ??? ????????????. ????????? ?????????????????????. ?????? ????????????????????????.", Toast.LENGTH_SHORT).show();


        }



    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_next:
            case R.id.iv_playView_next:
                nextORPrevClick(1);
                break;

            case R.id.iv_playView_prev:
            case R.id.iv_prev:
                nextORPrevClick(-1);
                break;

            case R.id.iv_bar_play:
            case R.id.iv_play_view_bar:
                playBtnListner();
                break;

        }
    }


    // ????????? ??????~~~~~~~
    private void serviceObservers() {

        mainViewModel.getBinder().observe(this, new Observer<PlayService.LocalBinder>() {
            @Override
            public void onChanged(PlayService.LocalBinder localBinder) {
                if (localBinder == null) {
                    Log.d(TAG, "onChanged: unbound from service");
                    mp.stop();
                    mp.release();

                } else {
                    Log.d(TAG, "onChanged: bound to service.");
                    playService = localBinder.getService();
                    mp = playService.getMediaPlayer();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        startService(); //????????? ?????????
    }

    private void startService() {
        Intent serviceIntent = new Intent(this, PlayService.class);
        startService(serviceIntent);
        bindService();
    }

    private void bindService() {
        Intent serviceBindIntent = new Intent(this, PlayService.class);
        bindService(serviceBindIntent, mainViewModel.getServiceConnection(), Context.BIND_AUTO_CREATE);
    }






    @Override   //???????????? ?????? ??????????????? ?????????????????? ??????
    protected void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)  //??????
    public void notificationObsever(NotificationBus notificationBus) {
        Log.d(TAG, "notificaionObsever: ????" + notificationBus.getPlayOrNext());

        if(notificationBus.getPlayOrNext() == 0){
            playBtnListner();

        }else if(notificationBus.getPlayOrNext() == -1){
            nextORPrevClick(-1);

        }else if(notificationBus.getPlayOrNext() == 1){
            nextORPrevClick(1);

        }
    }


}