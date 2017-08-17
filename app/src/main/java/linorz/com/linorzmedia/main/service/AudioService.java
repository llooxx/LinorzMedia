package linorz.com.linorzmedia.main.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.widget.RemoteViews;
import android.widget.Toast;

import linorz.com.linorzmedia.R;
import linorz.com.linorzmedia.media.AudioPlay;

/**
 * Created by linorz on 2017/8/18.
 */

public class AudioService extends Service {
    public static final String ACTION_NOTIFICATION = "action_notification";
    public static final String BUTTON_INDEX = "button_index";
    public static final String BUTTON_PREV = "0";
    public static final String BUTTON_PLAY = "1";
    public static final String BUTTON_NEXT = "2";
    private NotificationManager notifyManager;
    private Notification notification;
    private RemoteViews remoteViews;
    private AudioPlay audioPlay;
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    remoteViews.setTextViewText(R.id.notification_name, audioPlay.getCurrentAudio().getTitle());
                    notifyManager.notify(233, notification);
                    break;
                case 2:
                    if (audioPlay.isPlaying()) {
                        remoteViews.setImageViewResource(R.id.notification_play, R.drawable.btn_play_white);
                    } else {
                        remoteViews.setImageViewResource(R.id.notification_play, R.drawable.btn_pause_white);
                    }
                    notifyManager.notify(233, notification);
                    break;
                case 3:
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
        Intent intent = new Intent(this, AudioService.class);
        intent.setAction(AudioService.ACTION_NOTIFICATION);
        PendingIntent pendingIntent;

        intent.putExtra(AudioService.BUTTON_INDEX, AudioService.BUTTON_PREV);
        pendingIntent = PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.notification_pre, pendingIntent);

        intent.putExtra(AudioService.BUTTON_INDEX, AudioService.BUTTON_PLAY);
        pendingIntent = PendingIntent.getService(this, 2, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.notification_play, pendingIntent);

        intent.putExtra(AudioService.BUTTON_INDEX, AudioService.BUTTON_NEXT);
        pendingIntent = PendingIntent.getService(this, 3, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.notification_next, pendingIntent);


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

        audioPlay = AudioPlay.instance;
        audioPlay.setAudioServiceAcion(new AudioPlay.AudioServiceAciton() {
            @Override
            public void start() {
                handler.sendMessage(Message.obtain(handler, 2));
            }

            @Override
            public void pause() {
                handler.sendMessage(Message.obtain(handler, 2));
            }

            @Override
            public void changeAudio() {
                handler.sendMessage(Message.obtain(handler, 1));
            }
        });
        handler.sendMessage(Message.obtain(handler, 1));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        String stringExtra = intent.getStringExtra(BUTTON_INDEX);

        if (action.equals(ACTION_NOTIFICATION)) {
            switch (stringExtra) {
                case BUTTON_PLAY:
                    if (audioPlay.isPlaying()) {
                        audioPlay.pause();
                    } else {
                        audioPlay.start();
                    }
                    break;
                case BUTTON_NEXT:
                    //下一个
                    if (!audioPlay.setAudio(++audioPlay.current_num, true)) {
                        audioPlay.current_num--;
                        Toast.makeText(this, "后面没有歌啦", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case BUTTON_PREV:
                    //前一个
                    if (!audioPlay.setAudio(--audioPlay.current_num, true)) {
                        audioPlay.current_num++;
                        Toast.makeText(this, "后面没有歌啦", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }
}
