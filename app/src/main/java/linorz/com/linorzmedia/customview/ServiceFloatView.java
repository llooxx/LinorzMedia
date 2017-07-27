package linorz.com.linorzmedia.customview;

/**
 * Created by linorz on 2017/7/27.
 */

import android.content.Context;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import java.util.Timer;

//可以拖动的按钮
public class ServiceFloatView extends android.support.v7.widget.AppCompatImageView {
    private int basex = 0, basey = 0;
    private int lastx = 0, lasty = 0;
    private WindowManager.LayoutParams param;
    private WindowManager wm;
    private boolean is_moved = false;
    private boolean enable_touch = true;
    private int width, height, screenWidth = 0, screenHeight = 0;

    public ServiceFloatView(Context context, int width, int height) {
        super(context);
        this.width = dipTopx(context, width);
        this.height = dipTopx(context, height);
        setLayoutParams(new ViewGroup.LayoutParams(this.width, this.height));
        screenWidth = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
        screenHeight = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getHeight();
    }

    public void initView(WindowManager.LayoutParams wml, double startx, double starty) {
        wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        wm.addView(this, wml);
        basex = (int) (screenWidth * startx) - width / 2;
        basey = (int) (screenHeight * starty);
        enable_touch = true;
        // 此句必须在最后初始化（否则需要很多判断它是否为null）
        param = wml;
        moveToHere(basex, basey);
    }

    public void setTouchEnable(boolean enable) {
        enable_touch = enable;
    }


    int nowx;
    int nowy;

    @Override
    public boolean onTouchEvent(MotionEvent e1) {
        if (!enable_touch)
            return super.onTouchEvent(e1);
        nowx = (int) e1.getRawX();
        nowy = (int) e1.getRawY();
        switch (e1.getAction()) {
            case MotionEvent.ACTION_DOWN:
                is_moved = false;
                lastx = nowx;
                lasty = nowy;
                break;
            case MotionEvent.ACTION_MOVE:
                moveToHere(param.x + (nowx - lastx),
                        param.y + (nowy - lasty));
                is_moved = true;
                lastx = nowx;
                lasty = nowy;
                return true;
            case MotionEvent.ACTION_UP:
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        if (is_moved) {// 位置回归
                            //记录当前位置
                            int old_x = param.x + (nowx - lastx),
                                    old_y = param.y + (nowy - lasty);
                            int back_x = old_x, back_y = old_y;
                            //判断水平
                            if (nowx - width < 0)
                                back_x = -width / 2;
                            if (screenWidth - nowx - width < 0)
                                back_x = screenWidth - width / 2;
                            //判断垂直
                            if (nowy - height < 0)
                                back_y = -height / 2;
                            if (screenHeight - nowy - height < 0)
                                back_y = screenHeight - height/2;
                            //边缘停靠
                            if (back_x != old_x || back_y != old_y)
                                returnToScreen(back_x, back_y);
                            is_moved = false;
                        }
                    }
                }, 100);
                break;
        }
        return super.onTouchEvent(e1);
    }

    public boolean canDo() {
        if (nowx - width < 0) return false;
        if (screenWidth - nowx - width < 0) return false;
        if (nowy - height < 0) return false;
        if (screenHeight - nowy - height < 0) return false;
        return true;
    }

    private void returnToScreen(final int x, final int y) {
        // 开启移动动画//////////from与to是指相对当前位置的变化
        TranslateAnimation ta = new TranslateAnimation(0, -param.x + x, 0, -param.y + y);
        ta.setDuration(200);
        startAnimation(ta);
        // 设置回到正常的布局位置
        new Handler().postDelayed(new Runnable() {
            public void run() {
                clearAnimation();
                moveToHere(x, y);
            }
        }, 200);
    }

    private void moveToHere(int tempx, int tempy) {
        param.x = tempx;
        param.y = tempy;
        //刷新
        wm.updateViewLayout(this, param);
        invalidate();
    }

    private int dipTopx(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
