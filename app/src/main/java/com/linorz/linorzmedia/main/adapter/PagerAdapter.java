package linorz.com.linorzmedia.main.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.List;

import linorz.com.linorzmedia.main.fragment.MediaFragment;

/**
 * Created by LLX on 2015/10/28.
 */
public class PagerAdapter extends FragmentPagerAdapter {
    private List<MediaFragment> fragmentList;
    private List<String> list_Title;

    public PagerAdapter(FragmentManager fm, List<MediaFragment> fragmentList, List<String> list_Title) {
        super(fm);
        this.fragmentList = fragmentList;
        this.list_Title = list_Title;
    }

    @Override
    public Fragment getItem(int pos) {
        return fragmentList.get(pos % fragmentList.size());
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        Fragment fragment = ((Fragment) object);
        container.removeView(fragment.getView());
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return list_Title == null ? "" : list_Title.get(position % list_Title.size());
    }
}