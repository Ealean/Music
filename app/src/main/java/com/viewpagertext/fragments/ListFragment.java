package com.viewpagertext.fragments;

import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.viewpagertext.R;
import com.viewpagertext.adapters.ListRecAdapter;
import com.viewpagertext.constructor.ListToPlayEvent;
import com.viewpagertext.constructor.MessageEvent;
import com.viewpagertext.databinding.ActivityMovieDetailBinding;
import com.viewpagertext.json.ListFragmentSongMusics;
import com.viewpagertext.utils.CommonUtils;
import com.viewpagertext.utils.HttpUtil;
import com.viewpagertext.utils.StatusBarUtil;
import com.viewpagertext.utils.TimeUtil;
import com.viewpagertext.views.MyNestedScrollView;
import com.viewpagertext.views.MyRoundedImageView;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import jp.wasabeef.glide.transformations.BlurTransformation;
import okhttp3.Call;
import okhttp3.Response;
import static com.viewpagertext.adapters.MusicGridAdapter.FindImgUrlPath;
import static com.viewpagertext.adapters.MusicGridAdapter.FindSongsId;

/**
 * name:?????????
 * time:2019.5.4
 * Type:?????????Fragment
 */

public class ListFragment extends Fragment {

    public final static String PARAM = "isRecyclerView";
    private int imageBgHeight;// ?????????????????????????????????
    private int slidingDistance; // ????????????????????????
    private boolean isRecyclerView;
    private ActivityMovieDetailBinding binding;
    private ArrayList<ListFragmentSongMusics.DataBean> mData=new ArrayList<>();
    private ListRecAdapter adapter;
    private ViewPager viewPager;
    private String GetFindSongsId="http://v1.itooi.cn/netease/songList?&format=1&id="+FindSongsId;

//    public final static String IMAGE_URL_LARGE ="https://img3.doubanio.com/view/subject/m/public/s4477716.jpg";
//    public final static String IMAGE_URL_MEDIUM ="https://img3.doubanio.com/view/subject/s/public/s4477716.jpg";
//    public final static String IMAGE_URL_SMALL = "https://img3.doubanio.com/view/subject/s/public/s4477716.jpg";


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding=DataBindingUtil.inflate(inflater, R.layout.activity_movie_detail, container, false);
        EventBus.getDefault().register(this);//????????????
        if (getActivity().getIntent() != null) {
            isRecyclerView = getActivity().getIntent().getBooleanExtra(PARAM, true);
        }
        viewPager=getActivity().findViewById(R.id.play_viewpager);
        setTitleBar();
        setPicture();
        initSlideShapeTheme();
        sendRequestWithOkHttp();//????????????
        setAdapter();// RecyclerView????????????
        return binding.getRoot();
    }

    private void sendRequestWithOkHttp(){
        HttpUtil.sendOkHttpRequest(GetFindSongsId,new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String ListResponseData=response.body().string();//????????????????????????????????????
                parseJSONWithGSON(ListResponseData);
            }
        });
    }

    public ListFragmentSongMusics parseJSONWithGSON(String jsonData){

        mData.clear();
        try {
            JSONObject jsonObject=new JSONObject(jsonData);
            JSONArray jsonArray=jsonObject.getJSONArray("data");
//            Log.d("ListFragment","Data?????????????????????"+jsonArray.length());
            for (int i=0;i<jsonArray.length();i++){
                JSONObject jsonObject1=jsonArray.getJSONObject(i);
                String pic = jsonObject1.getString("pic");
                String name=jsonObject1.getString("name");
                String singer=jsonObject1.getString("singer");
                String id = jsonObject1.getString("id");
                int number=i+1;
                long time=jsonObject1.getLong("time");
                String lrc=jsonObject1.getString("lrc");
                String url=jsonObject1.getString("url");
                ListFragmentSongMusics.DataBean ld=new ListFragmentSongMusics.DataBean();
                ld.setPic(pic);
                ld.setName(name);
                ld.setSinger(singer);
                ld.setNumber(number+"");
                ld.setId(id);
                ld.setTime(time);
                ld.setLrc(lrc);
                ld.setUrl(url);
                mData.add(ld);
            }
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                }
            });


        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(),"???????????????????????????",Toast.LENGTH_SHORT).show();
        }
        return null;
    }
    //??????????????????????????????
    private void setPicture() {
        Glide.with(getActivity())
                .load(FindImgUrlPath)
                .override((int)CommonUtils.getDimens(R.dimen.dp_140), (int) CommonUtils.getDimens(R.dimen.dp_140))
                .into(binding.include.ivOnePhoto);

        // "14":????????????"3":????????????3?????????????????????
        Glide.with(getActivity())
                .load(FindImgUrlPath)
                .error(R.mipmap.stackblur_default)
                .placeholder(R.mipmap.stackblur_default)
//                .crossFade(3000)//??????????????????
                .dontAnimate()//??????????????????
                .bitmapTransform(new BlurTransformation(getActivity(), 200, 3))// ??????????????????
//                .bitmapTransform(new BlurTransformation(getActivity(), 14, 3))
                .into(binding.include.imgItemBg);
    }

    //??????FindFragment???RecyclerView???Item?????????
    @Subscribe(threadMode = ThreadMode.POSTING,sticky = true)
    public void messageEventBus(MessageEvent event){
        binding.include.tvOneCity.setText(event.getFindGridItemName());
    }


     // ??????RecyclerView
    private void setAdapter() {
        binding.tvTxt.setVisibility(View.GONE);
        binding.xrvList.setVisibility(View.VISIBLE);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        binding.xrvList.setLayoutManager(mLayoutManager);
        binding.xrvList.setNestedScrollingEnabled(false);//??????????????????????????????
        binding.xrvList.setHasFixedSize(false);
        adapter = new ListRecAdapter(getActivity(),mData);
        binding.xrvList.setAdapter(adapter);

        // ??????item???item????????????????????????
        adapter.setOnItemClickListener(MyItemClickListener);
    }

    //item???item??????????????????????????????
    private ListRecAdapter.OnItemClickListener MyItemClickListener = new ListRecAdapter.OnItemClickListener() {
        BottomSheetDialog bottomSheetDialog;
        @Override
        public void onItemClick(View v, ListRecAdapter.ViewName viewName, final int position) {
            String Pic= mData.get(position).getPic();
            String Name=mData.get(position).getName();
            String Singer=mData.get(position).getSinger();
            String Time=TimeUtil.formatSeconds(mData.get(position).getTime());
            String Lrc= mData.get(position).getLrc();
            String url=mData.get(position).getUrl();

            //viewName?????????item???item????????????
            switch (v.getId()){
                case R.id.ListFragment_menu://??????????????????
//                  Toast.makeText(getActivity(),"????????????????????????"+(position+1),Toast.LENGTH_SHORT).show();
                    View view = View.inflate(getActivity(), R.layout.list_dialog, null);
                    MyRoundedImageView localDialogIcon= view.findViewById(R.id.list_DialogIcon);//??????????????????
                    Glide.with(getActivity()).load(Pic).into(localDialogIcon);
                    TextView dialog_song=view.findViewById(R.id.list_dialog_song);//????????????
                    dialog_song.setText(Name);
                    TextView dialog_singer = view.findViewById(R.id.list_dialog_singer);//????????????
                    dialog_singer.setText(Singer);
                    TextView dialog_singer2 = view.findViewById(R.id.list_dialog_singer2);//??????
                    dialog_singer2.setText(Singer);
                    TextView dialog_album = view.findViewById(R.id.list_dialog_album);//??????
                    dialog_album.setText(Name);
                    TextView dialog_time = view.findViewById(R.id.list_dialog_time);//????????????
                    dialog_time.setText(Time);

                    RelativeLayout shareMusic = view.findViewById(R.id.shareMusic);//??????????????????
                    shareMusic.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent textIntent = new Intent(Intent.ACTION_SEND);
                            textIntent.setType("text/plain");
                            textIntent.putExtra(Intent.EXTRA_TEXT, "??????????????????https://v1.itooi.cn/netease/url?id="+mData.get(position).getId());
                            startActivity(Intent.createChooser(textIntent, "??????"));
                        }
                    });
                    RelativeLayout NetToPlay = view.findViewById(R.id.NetToPlay);//??????????????????
                    NetToPlay.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            viewPager.setCurrentItem(1);
                            bottomSheetDialog.hide();
                        }
                    });

                    bottomSheetDialog = new BottomSheetDialog(getActivity());
                    bottomSheetDialog.setContentView(view);
                    bottomSheetDialog.getWindow().findViewById(R.id.design_bottom_sheet).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));//??????BottomSheetDialog??????
                    bottomSheetDialog.show();
                    break;
                case R.id.ListintoPlayPage:
//                    Toast.makeText(getActivity(),"????????????item??????"+(position+1),Toast.LENGTH_SHORT).show();
//                  ????????????????????????
                    EventBus.getDefault().postSticky(new ListToPlayEvent(Pic,Name,Singer,Lrc,url,Time));
                    viewPager.setCurrentItem(1);
                  break;
                default:
                    break;
            }
        }

        @Override
        public void onItemLongClick(View v) {

        }
    };

    //toolbar??????
    private void setTitleBar() {
        ((AppCompatActivity) getActivity()).setSupportActionBar(binding.titleToolBar);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            //????????????Title??????
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.mipmap.back);
        }
        binding.titleToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
    }


    //?????????????????????
    private void initSlideShapeTheme() {

        setImgHeaderBg();

        // toolbar?????????
        int toolbarHeight = binding.titleToolBar.getLayoutParams().height;
        // toolbar+??????????????????
        final int headerBgHeight = toolbarHeight + StatusBarUtil.getStatusBarHeight(getActivity());

        // ??????????????????????????????????????????????????????toolbar+??????????????????
        binding.ivTitleHeadBg.setVisibility(View.VISIBLE);
        ViewGroup.LayoutParams params = binding.ivTitleHeadBg.getLayoutParams();
        ViewGroup.MarginLayoutParams ivTitleHeadBgParams = (ViewGroup.MarginLayoutParams) binding.ivTitleHeadBg.getLayoutParams();
        int marginTop = params.height - headerBgHeight;
        ivTitleHeadBgParams.setMargins(0, -marginTop, 0, 0);
        binding.ivTitleHeadBg.setImageAlpha(0);

        // ????????????View??????????????????????????????
        StatusBarUtil.setTranslucentImageHeader(getActivity(), 0, binding.titleToolBar);

        ViewGroup.LayoutParams imgItemBgparams = binding.include.imgItemBg.getLayoutParams();
        // ??????????????????????????????
        imageBgHeight = imgItemBgparams.height;

        // ?????????????????????
        initScrollViewListener();

    }


    //??????titlebar??????,??????????????????????????????
    private void setImgHeaderBg() {
        Glide.with(getActivity())
                .load(FindImgUrlPath)
//                .placeholder(R.mipmap.stackblur_default)
                .error(R.mipmap.stackblur_default)
                .bitmapTransform(new BlurTransformation(getActivity(), 200, 3))// ??????????????????
                .listener(new RequestListener<String, GlideDrawable>() {//??????????????????
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        binding.titleToolBar.setBackgroundColor(Color.TRANSPARENT);
                        binding.ivTitleHeadBg.setImageAlpha(0);
                        binding.ivTitleHeadBg.setVisibility(View.VISIBLE);
                        return false;
                    }
                }).into(binding.ivTitleHeadBg);
    }

    private void initScrollViewListener() {
        // ????????????api23??????
        binding.nsvScrollview.setOnMyScrollChangeListener(new MyNestedScrollView.ScrollInterface() {
            @Override
            public void onScrollChange(int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                scrollChangeHeader(scrollY);
            }
        });

        int titleBarAndStatusHeight = (int) (CommonUtils.getDimens(R.dimen.dp_58) + StatusBarUtil.getStatusBarHeight(getActivity()));
        slidingDistance = imageBgHeight - titleBarAndStatusHeight - (int) (CommonUtils.getDimens(R.dimen.dp_30));
    }

    //??????????????????????????????Header???????????????
    private void scrollChangeHeader(int scrolledY) {

//        DebugUtil.error("---scrolledY:  " + scrolledY);
//        DebugUtil.error("-----slidingDistance: " + slidingDistance);

        if (scrolledY < 0) {
            scrolledY = 0;
        }
        float alpha = Math.abs(scrolledY) * 1.0f / (slidingDistance);
        Drawable drawable = binding.ivTitleHeadBg.getDrawable();
//        DebugUtil.error("----alpha:  " + alpha);

        if (drawable != null) {
            if (scrolledY <= slidingDistance) {
                // title???????????????
                drawable.mutate().setAlpha((int) (alpha * 255));
                binding.ivTitleHeadBg.setImageDrawable(drawable);
            } else {
                drawable.mutate().setAlpha(255);
                binding.ivTitleHeadBg.setImageDrawable(drawable);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.xrvList.setFocusable(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);//?????????
    }

}

