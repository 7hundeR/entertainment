package com.thunder.entertainment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.transition.Explode;
import android.view.Window;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.thunder.entertainment.common.base.BaseActivity;
import com.thunder.entertainment.ui.fragment.ViewPageInfo;
import com.thunder.entertainment.ui.fragment.image.ImageFragment;
import com.thunder.entertainment.ui.fragment.news.NewsManagerFragment;
import com.thunder.entertainment.ui.fragment.video.VideoFragment;
import com.thunder.entertainment.ui.fragment.weitop.WeiTopFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;

public class
MainActivity extends BaseActivity {

    @BindView(R.id.bottom_navigation)
     AHBottomNavigation ahBottomNavigation;
    @BindView(R.id.view_pager)
    ViewPager mViewPager;

    private List<ViewPageInfo> fragList;
    //    protected FragmentStatePagerAdapter mAdapter;
    protected FragmentPagerAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // 设置一个exit transition
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            getWindow().setEnterTransition(new Explode());
            getWindow().setExitTransition(new Explode());
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initView() {
        initBottomNavigation();
        initViewPage();
    }

    private void initViewPage() {
        if (mAdapter == null){
            fragList = new ArrayList<>();
            ViewPageInfo newsItem = new ViewPageInfo("news", new NewsManagerFragment());
            ViewPageInfo imageItem = new ViewPageInfo("image", new ImageFragment());
//            ViewPageInfo musicItem = new ViewPageInfo("music", new MusicFragment());
            ViewPageInfo movieItem = new ViewPageInfo("movie", new VideoFragment());
            ViewPageInfo weiTop = new ViewPageInfo("weitop", new WeiTopFragment());
            fragList.add(newsItem);
            fragList.add(imageItem);
//            fragList.add(musicItem);
            fragList.add(movieItem);
            fragList.add(weiTop);
            //mainActivity ViewPage中的Fragment如果有Viewpage  使用FragmentStatePagerAdapter
            //mainActivity 必须用FragmentPagerAdapter，否则报错
            //原因暂时不详
            mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
                @Override
                public Fragment getItem(int position) {
                    return fragList.get(position).fragment;
                }

                @Override
                public int getCount() {
                    return fragList == null ? 0 : fragList.size();
                }
            };

            if (mViewPager !=null) {
                mViewPager.setAdapter(mAdapter);
            }

        }else{
            if (mViewPager !=null) {
                mViewPager.setAdapter(mAdapter);
            }
        }
        mViewPager.setOffscreenPageLimit(4);
    }

    public void initBottomNavigation() {
        // 创建底部导航的每一项
        AHBottomNavigationItem item1 = new AHBottomNavigationItem(R.string.tab_1, R.drawable.ic_import_contacts, R.color.color_tab_normal);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem(R.string.tab_2, R.drawable.ic_image, R.color.color_tab_normal);
//        AHBottomNavigationItem item3 = new AHBottomNavigationItem(R.string.tab_3, R.drawable.ic_audiotrack, R.color.color_tab_normal);
        AHBottomNavigationItem item4 = new AHBottomNavigationItem(R.string.tab_4, R.drawable.ic_live_tv, R.color.color_tab_normal);
        AHBottomNavigationItem item5 = new AHBottomNavigationItem(R.string.tab_5, R.drawable.tab_micro_selected, R.color.color_tab_normal);

        // 添加到导航中
        ahBottomNavigation.addItem(item1);
        ahBottomNavigation.addItem(item2);
//        ahBottomNavigation.addItem(item3);
        ahBottomNavigation.addItem(item4);
        ahBottomNavigation.addItem(item5);

        // 设置背景颜色
        ahBottomNavigation.setDefaultBackgroundColor(Color.parseColor("#FEFEFE"));

        // Change colors
        ahBottomNavigation.setAccentColor(Color.parseColor(getString(R.string.color_tab_accent)));
        ahBottomNavigation.setInactiveColor(Color.parseColor(getString(R.string.color_tab_inactive)));

        // Force to tint the drawable (useful for font with icon for example)
        ahBottomNavigation.setForceTint(false);

        //设置导航文字显示方式
        // TitleState.SHOW_WHEN_ACTIVE  选中才显示
        // TitleState.ALWAYS_SHOW       总是显示
        // TitleState.ALWAYS_HIDE       总是隐藏
        ahBottomNavigation.setTitleState(AHBottomNavigation.TitleState.SHOW_WHEN_ACTIVE);

        // 设置当前选中　
        ahBottomNavigation.setCurrentItem(0);
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initEvent() {
        ahBottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                mViewPager.setCurrentItem(position);
                return true;
            }
        });

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                ahBottomNavigation.setCurrentItem(position);
                ahBottomNavigation.restoreBottomNavigation(true);

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    protected void initData() {
    }


    @Override
    public void onBackPressed() {
        if (JCVideoPlayer.backPress()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        JCVideoPlayer.releaseAllVideos();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
