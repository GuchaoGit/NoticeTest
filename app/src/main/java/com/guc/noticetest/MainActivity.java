package com.guc.noticetest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.guc.noticetest.mqtt.PushService;
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
                PushService.connect(this);
                break;
            case R.id.btn_send_notice://接收通知
                for (int i = 0;i<15;i++){
                    try{
                        mNoticeManager.sendNotice(1, "收到一条通知消息", "今天中午吃什么？",true);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

}
