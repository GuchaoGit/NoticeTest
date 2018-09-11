package com.guc.noticetest.mqtt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.util.Log;

import com.guc.noticetest.utils.NoticeManager;

/**
 * Created by guc on 2018/9/10.
 * 描述：
 */
public class MqttReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("MqttReceiver","onReceive"+intent.getAction());
        switch (intent.getAction()){
            case MqttMessageEvent.TAG:
                break;
            case Intent.ACTION_BOOT_COMPLETED://启动
                break;
        }
    }
}
