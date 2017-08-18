package com.linorz.linorzmedia.customview;

import android.support.v7.widget.RecyclerView;

import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by linorz on 2016/4/26.
 * 滑动自动加载监听器
 */
public class AutoLoadScrollListener extends RecyclerView.OnScrollListener {

    private ImageLoader imageLoader;
    private final boolean pauseOnScroll;
    private final boolean pauseOnFling;

    public AutoLoadScrollListener(ImageLoader imageLoader, boolean pauseOnScroll, boolean pauseOnFling) {
        super();
        this.pauseOnScroll = pauseOnScroll;
        this.pauseOnFling = pauseOnFling;
        this.imageLoader = imageLoader;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        if (imageLoader != null) {
            switch (newState) {
                case 0:
                    imageLoader.resume();
                    break;
                case 1:
                    if (pauseOnScroll) imageLoader.pause();
                    else imageLoader.resume();
                    break;
                case 2:
                    if (pauseOnFling) imageLoader.pause();
                    else imageLoader.resume();
                    break;
            }
        }
    }
}