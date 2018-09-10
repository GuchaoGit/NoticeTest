package com.guc.noticetest.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.guc.noticetest.utils.NoticeManager;

/**
 * Created by guc on 2018/9/10.
 * 描述：
 */
public class PushService extends Service {
    public static final String TAG = "PushService";
    private NoticeManager mNoticeManager;

    public static void startService(Context context){
        Intent intent = new Intent(context,PushService.class);
        context.startService(intent);
    }
    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG,"onCreate");
        mNoticeManager = NoticeManager.getInstance().init(this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mNoticeManager!=null){
                    try{
                        mNoticeManager.sendNotice(1, "收到一条通知消息", "服务接收到通知",true);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        },10000);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG,"onStartCommand");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG,"onDestroy");
    }
}
