package linorz.com.linorzmedia.customview;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;

import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by linorz on 2017/8/7.
 */

public class LinorzRecyclerView extends RecyclerView {
    private Adapter adapter;
    private int LINEAR = 1, GRID = 2, STAGGERED = 3;
    private int LAYOUTMANAGER;
    private boolean isLoading = false;
    private LoadMore mLoadMore;

    public LinorzRecyclerView(Context context) {
        super(context);
    }

    public LinorzRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LinorzRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        this.adapter = adapter;
    }

    @Override
    public void setLayoutManager(LayoutManager layout) {
        super.setLayoutManager(layout);
        if (layout instanceof GridLayoutManager) {
            LAYOUTMANAGER = GRID;
        } else if (layout instanceof LinearLayoutManager) {
            LAYOUTMANAGER = LINEAR;
        } else if (layout instanceof StaggeredGridLayoutManager) {
            LAYOUTMANAGER = STAGGERED;
        }
    }

    public void setLoadMore(LoadMore loadMore) {
        this.mLoadMore = loadMore;
    }

    /**
     * 获得最大的位置
     */
    private int getMaxPosition(int[] positions) {
        int size = positions.length;
        int maxPosition = Integer.MIN_VALUE;
        for (int i = 0; i < size; i++) {
            maxPosition = Math.max(maxPosition, positions[i]);
        }
        return maxPosition;
    }

    private int getLastVisiblePosition() {
        int position;
        if (LAYOUTMANAGER == LINEAR) {
            position = ((LinearLayoutManager) getLayoutManager()).findLastVisibleItemPosition();
        } else if (LAYOUTMANAGER == STAGGERED) {
            StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) getLayoutManager();
            int[] lastPositions = layoutManager.findLastVisibleItemPositions(new int[layoutManager.getSpanCount()]);
            position = getMaxPosition(lastPositions);
        } else if (LAYOUTMANAGER == GRID) {
            position = ((GridLayoutManager) getLayoutManager()).findLastVisibleItemPosition();
        } else {
            position = getLayoutManager().getItemCount() - 1;
        }
        return position;
    }

    public void endLoading() {
        isLoading = false;
    }

    /**
     * 配置显示图片，需要设置这几个参数，快速滑动时，暂停图片加载
     *
     * @param imageLoader   ImageLoader实例对象
     * @param pauseOnScroll
     * @param pauseOnFling
     */
    public void setOnPauseListenerParams(ImageLoader imageLoader, boolean pauseOnScroll, boolean pauseOnFling) {
        super.addOnScrollListener(new AutoLoadListener(imageLoader, pauseOnScroll, pauseOnFling) {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0 && !isLoading) {
                    int lastVisiblePosition = getLastVisiblePosition();
                    System.out.println(lastVisiblePosition + "/" + adapter.getItemCount());
                    if (lastVisiblePosition == adapter.getItemCount()) {
                        System.out.println("加载" + dy);
                        isLoading = true;
                        mLoadMore.loadMore();
                    }
                }
            }
        });

    }


    private class AutoLoadListener extends RecyclerView.OnScrollListener {

        private ImageLoader imageLoader;
        private final boolean pauseOnScroll;
        private final boolean pauseOnFling;

        public AutoLoadListener(ImageLoader imageLoader, boolean pauseOnScroll, boolean pauseOnFling) {
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

    public interface LoadMore {
        void loadMore();
    }
}
