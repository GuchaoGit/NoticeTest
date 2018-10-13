package com.guc.noticetest.utils;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.guc.noticetest.MainActivity;
import com.guc.noticetest.R;

/**
 * Created by guc on 2018/9/10.
 * 描述：通知管理工具
 */
public class NoticeManager {
    public static final String CHANNEL_ID = "notice";
    public static final String CHANNEL_ID_SUB = "notice_sub";

    private static NoticeManager mNotcieManager;
    private Context mContext;
    private NoticeManager(){
    }

    public static NoticeManager getInstance(){
        if (mNotcieManager == null){
            synchronized (NoticeManager.class){
                if (mNotcieManager==null)
                    mNotcieManager = new NoticeManager();
            }
        }
        return mNotcieManager;
    }

    public NoticeManager init(Context context){
        mContext = context;
        initNoticeChannel();
        return mNotcieManager;
    }
    /**
     *
     * @param id
     * @param title
     * @param text
     * @param autoCancle  是否自动取消
     */
    public void sendNotice(int id, String title, String text,boolean autoCancle) throws Exception{
        if (mContext == null) throw new Exception("context is not init");
        NotificationManager manager = (NotificationManager)mContext.getSystemService(mContext.NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_launcher_foreground))
                .setContentIntent(getPendingIntent())
                .setAutoCancel(autoCancle)
                .setDefaults(Notification.DEFAULT_ALL)
                .build();
//        notification.flags = Notification.FLAG_AUTO_CANCEL;
        manager.notify(id, notification);
    }
    private void initNoticeChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelName;
            int importance = NotificationManager.IMPORTANCE_HIGH;
            channelName = "通知消息";
            createNotificationChannel(CHANNEL_ID, channelName, importance);
            channelName = "订阅消息";
            importance = NotificationManager.IMPORTANCE_DEFAULT;
            createNotificationChannel(CHANNEL_ID_SUB, channelName, importance);
        }
    }
    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannel(String channelId, String channelName, int importance) {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        channel.setShowBadge(true);
        NotificationManager notificationManager = (NotificationManager)mContext.getSystemService(
                mContext.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
    }
    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setClass(mContext, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        Context context = mContext.getApplicationContext();
        PendingIntent contextIntent = PendingIntent.getActivity(context, 0, intent, 0);
        return contextIntent;
    }
}
