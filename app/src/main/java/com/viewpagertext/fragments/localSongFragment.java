package com.viewpagertext.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.viewpagertext.R;
import com.viewpagertext.activitys.MainActivity;
import com.viewpagertext.adapters.LocalSongRecAdapter;
import com.viewpagertext.constructor.Song;
import com.viewpagertext.help.MediaPlayerHelp;
import com.viewpagertext.utils.LoadSongUtils;
import com.viewpagertext.utils.StatusBarUtil;
import com.viewpagertext.views.MyRoundedImageView;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class localSongFragment extends Fragment{

    private RecyclerView mylist;
    private List<Song> list;
    private MediaPlayerHelp mMediaPlayerHelp;
    private SwipeRefreshLayout refreshLayout;
    private boolean isPlaying;
    private View v;
    private BottomSheetDialog bottomSheetDialog;
    private String song,singer,album,duration;
    private long size;
    private MyRoundedImageView localDialogIcon;
    private Bitmap bitmap;
    private LinearLayout loadTitleBar;
    private LocalSongRecAdapter adapter;
    @Override
    public View onCreateView( LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        v=inflater.inflate(R.layout.fragment_localsong,container,false);
        list = new ArrayList<>();
        StatusBarUtil.setTransparentForImageView(getActivity(),loadTitleBar);
        StatusBarUtil.StatusBarTextColor(getActivity(),true);

        newData();//??????????????????

        initView();//???????????????

        initRefreshLayout();//????????????
        return v;
    }

    private void initView(){
        mylist = v.findViewById(R.id.mylist);
        mylist.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter= new LocalSongRecAdapter(getActivity(),list);
        mylist.setAdapter(adapter);
        // ??????item???item????????????????????????
        adapter.setOnItemClickListener(MyItemClickListener);


        mMediaPlayerHelp=MediaPlayerHelp.getInstance(getActivity());


        refreshLayout =v.findViewById(R.id.refreshLayout);
        MyRoundedImageView local_back_icon = v.findViewById(R.id.local_back_icon);
        local_back_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), MainActivity.class));
            }
        });


        v.findViewById(R.id.loadTitleBar);

    }

    /**
     * item???item??????????????????????????????
     */
    private LocalSongRecAdapter.OnItemClickListener MyItemClickListener = new LocalSongRecAdapter.OnItemClickListener() {

        @Override
        public void onItemClick(View v, LocalSongRecAdapter.ViewName viewName, int position) {
            song=list.get(position).song;//????????????
            singer=list.get(position).singer;//????????????
            size=list.get(position).size;//????????????
            duration = LoadSongUtils.formatTime(list.get(position).duration);//????????????
            album=list.get(position).album;//????????????
            long albumId=list.get(position).albumId;
            bitmap=LoadSongUtils.getMusicBitemp(getActivity(), position, albumId);
            String p=list.get(position).path;//?????????????????????

            //viewName?????????item???item????????????
            switch (v.getId()){
                case R.id.localmenu:
//                    Toast.makeText(getActivity(),"????????????????????????"+(position+1),Toast.LENGTH_SHORT).show();
                    initDialog();
                    break;
//                case R.id.btn_refuse:
//                    Toast.makeText(MainActivity.this,"????????????????????????"+(position+1),Toast.LENGTH_SHORT).show();
//                    break;
                default:
//                    Toast.makeText(getActivity(),"????????????item??????"+(position+1),Toast.LENGTH_SHORT).show();
                    if (isPlaying){
                        stopMusic(p);
                    }else{
                        playMusic(p);
                    }
                    break;
            }
        }

        @Override
        public void onItemLongClick(View v) {

        }
    };


    private void initDialog() {
        View view = View.inflate(getActivity(), R.layout.local_dialog, null);
        TextView dialog_song=view.findViewById(R.id.dialog_song);//??????
        dialog_song.setText(song);
        TextView dialog_album=view.findViewById(R.id.dialog_album);//??????
        dialog_album.setText(album);
        TextView dialog_singer=view.findViewById(R.id.dialog_singer);//??????
        dialog_singer.setText(singer);
        TextView dialog_singer2=view.findViewById(R.id.dialog_singer2);//??????
        dialog_singer2.setText(singer);
        TextView dialog_time=view.findViewById(R.id.dialog_time);//??????
        dialog_time.setText(duration);
        TextView dialog_size=view.findViewById(R.id.dialog_size);//????????????
        dialog_size.setText(((new DecimalFormat("#.00")).format(Long.valueOf(size).floatValue()/1000000)));
        localDialogIcon=view.findViewById(R.id.localDialogIcon);
        MyRoundedImageView localDialogIcon=view.findViewById(R.id.localDialogIcon);//??????
        localDialogIcon.setImageBitmap(bitmap);

        bottomSheetDialog = new BottomSheetDialog(getActivity());
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.getWindow().findViewById(R.id.design_bottom_sheet).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));//??????BottomSheetDialog??????
        bottomSheetDialog.show();
    }

    //??????????????????
    private void initRefreshLayout(){
        refreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light, android.R.color.holo_orange_light);
        //???swipeRefreshLayout??????????????????
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //??????2?????????????????????????????????
                refresh();
            }
        });
    }

    private void refresh(){
        //??????1?????????????????????????????????
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Thread.sleep(1000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        newData();
                        adapter.notifyDataSetChanged();
                        refreshLayout.setRefreshing(false);
                    }
                });
            }
        }).start();
    }

    // ????????????????????????
    private void newData(){
        list.clear();
        list.addAll(LoadSongUtils.getmusic(getActivity()));
    }

    //????????????
    public void playMusic(String path){
//        mPath=path;
        isPlaying=true;
        /**
         * 1?????????????????????????????????????????????
         * 2?????????????????????????????????????????????????????????????????????????????????start??????
         * 3???????????????????????????????????????????????????????????????????????????setPath?????????
         */
        if (mMediaPlayerHelp.getPath()!=null
                && mMediaPlayerHelp.getPath().equals(path)){
            Toast.makeText(getActivity(),"????????????",Toast.LENGTH_SHORT).show();
            mMediaPlayerHelp.start();
        }else{
            mMediaPlayerHelp.setPath(path);
            mMediaPlayerHelp.setOnMediaPlayerHelperListener(new MediaPlayerHelp.OnMediaPlayerHelperListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mMediaPlayerHelp.start();
                    Toast.makeText(getActivity(),"????????????",Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    //????????????
    public void stopMusic(String path){
        isPlaying=false;
        if (mMediaPlayerHelp.getPath()!=null
                && mMediaPlayerHelp.getPath().equals(path)){
            mMediaPlayerHelp.pause();
            Toast.makeText(getActivity(),"????????????",Toast.LENGTH_SHORT).show();

        }else{
//        itemIvPlay.setVisibility(View.VISIBLE);
            mMediaPlayerHelp.setPath(path);
//            mMediaPlayerHelp.stop();

        }
    }

}
