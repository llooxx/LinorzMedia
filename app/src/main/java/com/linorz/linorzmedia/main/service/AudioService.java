package com.linorz.linorzmedia.main.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.linorz.linorzmedia.R;
import com.linorz.linorzmedia.main.activity.MainActivity;
import com.linorz.linorzmedia.mediatools.AudioPlay;
import com.linorz.linorzmedia.mediatools.Audio;

/**
 * Created by linorz on 2017/8/18.
 */

public class AudioService extends Service {
    private final String ACTION_NOTIFICATION = "action_notification";
    private final String BUTTON_INDEX = "button_index";
    private final String BUTTON_ICON = "0", BUTTON_PREV = "1",
            BUTTON_PLAY = "2", BUTTON_NEXT = "3",
            BUTTON_UP = "4", BUTTON_DOWN = "5";
    private final int HANDLER_CHANGE_AUDIO = 1, HANDLER_CHANGE_PLAY = 2, HANDLER_CHANGE_VOLUME = 3;
    private NotificationManager notifyManager;
    private Notification notification;
    private RemoteViews remoteViews;
    private AudioPlay audioPlay;//播放器控制
    private AudioPlay.AudioListener audioListener;//播放状态监听
    private Bitmap bitmap;//封面

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case HANDLER_CHANGE_AUDIO:
                    Audio audio = audioPlay.getCurrentAudio();
                    bitmap = audio.getArtwork(AudioService.this);
                    if (bitmap != null)
                        remoteViews.setImageViewBitmap(R.id.notification_icon, bitmap);
                    else
                        remoteViews.setImageViewResource(R.id.notification_icon, R.drawable.current);
                    remoteViews.setTextViewText(R.id.notification_name, audio.getTitle());
                    notifyManager.notify(233, notification);
                    break;
                case HANDLER_CHANGE_PLAY:
                    if (audioPlay.isPlaying())
                        remoteViews.setImageViewResource(R.id.notification_play, R.drawable.btn_pause_white);
                    else
                        remoteViews.setImageViewResource(R.id.notification_play, R.drawable.btn_play_white);
                    notifyManager.notify(233, notification);
                    break;
                case HANDLER_CHANGE_VOLUME:
                    remoteViews.setTextViewText(R.id.notification_volume, (int) (audioPlay.getVolume() * 10) + "");
                    notifyManager.notify(233, notification);
                    break;
            }
            return false;
        }
    });

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        remoteViews = new RemoteViews(getPackageName(), R.layout.notification);
        //返回activity
        Intent appIntent = new Intent(this, MainActivity.class);
        appIntent.setAction(Intent.ACTION_MAIN);
        appIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);//关键的一步，设置启动模式
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.notification_icon, contentIntent);

        //初始化
        Intent intent = new Intent(this, AudioService.class);
        intent.setAction(ACTION_NOTIFICATION);
        PendingIntent pendingIntent;
        //上一个
        intent.putExtra(BUTTON_INDEX, BUTTON_PREV);
        pendingIntent = PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.notification_pre, pendingIntent);
        //暂停播放
        intent.putExtra(BUTTON_INDEX, BUTTON_PLAY);
        pendingIntent = PendingIntent.getService(this, 2, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.notification_play, pendingIntent);
        //下一个
        intent.putExtra(BUTTON_INDEX, BUTTON_NEXT);
        pendingIntent = PendingIntent.getService(this, 3, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.notification_next, pendingIntent);
        //音量大
        intent.putExtra(BUTTON_INDEX, BUTTON_UP);
        pendingIntent = PendingIntent.getService(this, 4, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.notification_up, pendingIntent);
        //音量减
        intent.putExtra(BUTTON_INDEX, BUTTON_DOWN);
        pendingIntent = PendingIntent.getService(this, 5, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.notification_down, pendingIntent);


        //通知栏初始化
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
                .setPriority(Notification.PRIORITY_MAX) //设置该通知优先级
                .setOngoing(true)//ture，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
                .setSmallIcon(R.mipmap.ic_launcher);//设置通知小ICON
        notification = mBuilder.build();
        notification.bigContentView = remoteViews;
        notifyManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        notifyManager.notify(233, notification);
        //获取播放器控制
        audioPlay = AudioPlay.instance;
        audioListener = new AudioPlay.AudioListener() {
            @Override
            public void start() {
                Message.obtain(handler, HANDLER_CHANGE_PLAY).sendToTarget();
            }

            @Override
            public void pause() {
                Message.obtain(handler, HANDLER_CHANGE_PLAY).sendToTarget();
            }

            @Override
            public void changeAudio(boolean play) {
                Message.obtain(handler, HANDLER_CHANGE_AUDIO).sendToTarget();
            }

            @Override
            public void changeVolume(float volume) {
                Message.obtain(handler, HANDLER_CHANGE_VOLUME).sendToTarget();
            }
        };
        audioPlay.addAudioListener(audioListener);
        Message.obtain(handler, HANDLER_CHANGE_AUDIO).sendToTarget();
        Message.obtain(handler, HANDLER_CHANGE_PLAY).sendToTarget();
        Message.obtain(handler, HANDLER_CHANGE_VOLUME).sendToTarget();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        String stringExtra = intent.getStringExtra(BUTTON_INDEX);

        if (action.equals(ACTION_NOTIFICATION)) {
            switch (stringExtra) {
                case BUTTON_PLAY:
                    if (audioPlay.isPlaying()) audioPlay.pause();
                    else audioPlay.start();
                    break;
                case BUTTON_NEXT:
                    //下一个
                    audioPlay.playNext();
                    break;
                case BUTTON_PREV:
                    //前一个
                    audioPlay.playPrevious();
                    break;
                case BUTTON_UP:
                    //音量大
                    audioPlay.volumeUp();
                    break;
                case BUTTON_DOWN:
                    //音量小
                    audioPlay.volumeDown();
                    break;
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        audioPlay.removeAudioListener(audioListener);
        notifyManager.cancel(233);
        super.onDestroy();
    }
}
