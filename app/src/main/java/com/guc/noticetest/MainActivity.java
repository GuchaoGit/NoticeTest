package com.guc.noticetest;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.guc.noticetest.service.PushService;
import com.guc.noticetest.utils.NoticeManager;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    private NoticeManager mNoticeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mNoticeManager= NoticeManager.getInstance().init(this);
    }

    @OnClick({R.id.btn_start_service, R.id.btn_send_notice})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_start_service://模拟发送通知
                PushService.startService(this);
                break;
            case R.id.btn_send_notice://接收通知
                try{
                    mNoticeManager.sendNotice(1, "收到一条通知消息", "今天中午吃什么？",true);
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
        }
    }

}
