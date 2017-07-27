package linorz.com.linorzmedia.customview;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import linorz.com.linorzmedia.tools.StaticMethod;

//可以拖动的按钮
public class RandomFloatView extends android.support.v7.widget.AppCompatImageView {
    private int HANDLER_INT = 0;
    private int basex = 0, basey = 0, lastx = 0, lasty = 0, left, top;
    private int round = 0;
    private int last_dir = 0;
    private FrameLayout.LayoutParams param;
    private boolean is_moved = false;
    private Handler handler;
    private Timer timer;
    private boolean enable_touch = true;
    private int width, height, screenWidth = 0, screenHeight = 0;
    private Action action;

    public RandomFloatView(Context context, int width, int height) {
        super(context);
        this.width = dipTopx(context, width);
        this.height = dipTopx(context, height);
        setLayoutParams(new LayoutParams(this.width, this.height));
        screenWidth = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
        screenHeight = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getHeight();
    }

    public void initView(FrameLayout layout, double startx, double starty) {
        layout.addView(this);
        screenWidth = layout.getMeasuredWidth();
        screenHeight = layout.getMeasuredHeight();
        basex = (int) (screenWidth * startx - width / 2);
        basey = (int) (screenHeight * starty - height / 2);
        left = layout.getLeft();
        top = layout.getTop();
        round = 0;
        enable_touch = true;
        // 此句必须在最后初始化（否则需要很多判断它是否为null）
        param = (FrameLayout.LayoutParams) getLayoutParams();
        moveToHere(basex, basey);
    }

    public void startRandomMove(int round_limit, int start_dir) {
        if (round_limit == 0)
            return;
        last_dir = start_dir;// 目前是0-1
        round = round_limit;
        // 初始化handler
        handler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == HANDLER_INT) {
                    randomMove();
                }
                super.handleMessage(msg);
            }
        };
        // 时钟
        timer = new Timer(true);
        TimerTask task = new TimerTask() {
            public void run() {
                if (!is_moved)
                    handler.sendEmptyMessage(HANDLER_INT);
            }
        };
        // 开启时钟
        timer.schedule(task, 100, 100);
    }

    public void setTouchEnable(boolean enable) {
        enable_touch = enable;
    }

    // 随机移动算法以后可以改
    private void randomMove() {
        // 水平线性，垂直随机
        int movex = 0, movey = 0;
        int dir = (int) (Math.random() * 4);// 0-3
        dir = last_dir;
        if (param.leftMargin < basex - round) {// 左越界
            dir = last_dir = 1;
        } else if (param.leftMargin > basex + round) {
            dir = last_dir = 0;
        }
        movey = ((int) (Math.random() * 3) - 1) * ((int) (Math.random() * 3));
        if (param.topMargin < basey - round) {// 上越界
            movey = 1;
        } else if (param.topMargin > basey + round) {
            movey = -1;
        }
        last_dir = dir;
        switch (dir) {
            case 0:// 左
                movex = -1;
                break;
            case 1:
                movex = 1;
                break;
        }
        movex = param.leftMargin + movex;
        movey = param.topMargin + movey;
        moveToHere(movex, movey);
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
                if (action != null) action.down();
                break;
            case MotionEvent.ACTION_MOVE:
                moveToHere(param.leftMargin + (nowx - lastx),
                        param.topMargin + (nowy - lasty));
                is_moved = true;
                lastx = nowx;
                lasty = nowy;
                return true;
            case MotionEvent.ACTION_UP:
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        if (is_moved) {// 位置回归
                            //记录当前位置
                            int old_x = param.leftMargin + (nowx - lastx),
                                    old_y = param.topMargin + (nowy - lasty);
                            int back_x = old_x, back_y = old_y;
                            //判断水平
                            if (nowx - left - width < 0) back_x = -width / 2;
                            if (screenWidth + left - nowx - width < 0)
                                back_x = screenWidth - width / 2;
                            //判断垂直
//                            if (param.topMargin < 0) back_y = 0;
//                            if (screenHeight - 4 * height - param.topMargin < 0)
//                                back_y = screenHeight - 4 * height;
                            if (nowy - top - height < 0) back_y = -height / 2;
                            if (screenHeight + top - nowy - height < 0)
                                back_y = screenHeight - height / 2;
                            //边缘停靠
                            if (back_x != old_x || back_y != old_y)
                                returnToScreen(back_x, back_y);
                            is_moved = false;
                        }
                    }
                }, 100);
                if (action != null) action.up();
                break;
        }
        return super.onTouchEvent(e1);
    }

    public boolean canDo() {
        if (nowx - left - width < 0) return false;
        if (screenWidth + left - nowx - width < 0) return false;
        if (nowy - top - height < 0) return false;
        if (screenHeight + top - nowy - height < 0) return false;
        return true;
    }

    @Override
    protected void onDetachedFromWindow() {
        if (timer != null)
            timer.cancel();
        super.onDetachedFromWindow();
    }

    // 开启动画移动

    private void returnToBase() {
        // 开启移动动画//////////from与to是指相对当前位置的变化
        TranslateAnimation ta = new TranslateAnimation(0, -param.leftMargin
                + basex, 0, -param.topMargin + basey);
        ta.setDuration(200);
        startAnimation(ta);
        // 设置回到正常的布局位置
        new Handler().postDelayed(new Runnable() {
            public void run() {
                clearAnimation();
                moveToHere(basex, basey);
            }
        }, 200);
    }

    private void returnToScreen(final int x, final int y) {
        // 开启移动动画//////////from与to是指相对当前位置的变化
        TranslateAnimation ta = new TranslateAnimation(0, -param.leftMargin
                + x, 0, -param.topMargin + y);
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
        param.leftMargin = tempx;
        param.topMargin = tempy;
        setLayoutParams(param);
        invalidate();
    }

    private int dipTopx(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public interface Action {
        void up();

        void down();
    }
}
