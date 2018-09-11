package com.guc.noticetest;

import android.app.Application;
import android.util.Log;

import com.blankj.utilcode.util.Utils;

/**
 * Created by guc on 2018/9/11.
 * 描述：
 */
public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
    }

}
