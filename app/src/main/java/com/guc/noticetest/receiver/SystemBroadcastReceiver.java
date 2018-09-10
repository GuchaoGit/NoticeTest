package com.guc.noticetest.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by guc on 2018/9/10.
 * 描述：
 */
public class SystemBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("SystemBroadcastReceiver","onReceive"+intent.getAction());
    }
}
