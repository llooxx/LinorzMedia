package linorz.com.linorzmedia.media;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;

import linorz.com.linorzmedia.R;
import linorz.com.linorzmedia.customview.CustomSurfaceView;

/**
 * Created by linorz on 2016/5/31.
 */
public class MoreImagesActivity extends Activity {
    private Context context;
    private ViewPager viewPager;
    private ArrayList<View> views;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.more_img_activity);
        context = this;
        findViewById(R.id.zoom_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        viewPager = (ViewPager) findViewById(R.id.img_viewpager);
        Intent intent = getIntent();
        ArrayList<String> urls = intent.getStringArrayListExtra("urls");

        views = new ArrayList<>();
        views.add(getZoomView(urls.get(urls.size() - 1)));
        for (String url : urls) views.add(getZoomView(url));
        views.add(getZoomView(urls.get(0)));

        viewPager.setAdapter(new MoreImagesAdapter());

        viewPager.setCurrentItem(intent.getIntExtra("num" + 1, 1));
    }

    private View getZoomView(String imgurl) {
        final CustomSurfaceView csv = new CustomSurfaceView(context);
        csv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        ImageLoader.getInstance().loadImage(imgurl, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {
            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {
                csv.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.btn_start));
            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                csv.setBitmap(bitmap);
            }

            @Override
            public void onLoadingCancelled(String s, View view) {
                csv.setBitmap(null);
            }
        });

        return csv;
    }

    class MoreImagesAdapter extends PagerAdapter {
        public MoreImagesAdapter() {
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {
                }

                public void onPageScrolled(int position, float positionOffset,
                                           int positionOffsetPixels) {
                    if (positionOffset == 0.0 && views.size() > 3) {//完全切换完后跳转
                        if (position == 0) viewPager.setCurrentItem(views.size() - 2, false);
                        if (position == views.size() - 1) viewPager.setCurrentItem(1, false);
                    }
                }

                public void onPageScrollStateChanged(int state) {
                }
            });
        }

        @Override
        public int getCount() {
            return views.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(views.get(position), 0);
            return views.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            if (views.get(position).getParent() != null)
                container.removeView(views.get(position));
        }
    }
}
