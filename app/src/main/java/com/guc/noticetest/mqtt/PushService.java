package com.guc.noticetest.mqtt;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.blankj.utilcode.util.NetworkUtils;
import com.guc.noticetest.utils.NoticeManager;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by guc on 2018/9/10.
 * 描述：
 */
public class PushService extends Service {
    public static final String TAG = "PushService";
    private final static int GRAY_SERVICE_ID = 1001;
    private NoticeManager mNoticeManager;
//    private final String URL = "tcp://192.168.20.158:1883";
    private final String URL = "tcp://192.168.10.204:1883?jms.prefetchPolicy.all=10";
//    private final String URL = "tcp://192.168.30.153:1883";
    private final String USER_NAME = "admin";
    private final String PASSWORD = "admin";
    private final int QOS = 1;
    private MqttAndroidClient mqttAndroidClient;
    private Map<String, String> topicsMap = new HashMap<>();

    public static final String PARAMS = "com.mqtt.params";
    public static final String ACTION_CONNECT = "com.mqtt.action_connect";
    public static final String ACTION_DISCONNECT = "com.mqtt.action_disconnect";
    public static final String ACTION_SUBSCRIBE = "com.mqtt.action_subscribe";
    public static final String ACTION_SUBSCRIBES = "com.mqtt.action_subscribes";
    public static final String ACTION_UNSUBSCRIBE = "com.mqtt.action_unsubscribe";
    public static final String ACTION_UNSUBSCRIBES = "com.mqtt.action_unsubscribes";
    public static final String ACTION_UNSUBSCRIBE_ALL = "com.mqtt.action_unsubscribe_all";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG,"onCreate");
        registerSystemReceiver();
        mNoticeManager = NoticeManager.getInstance().init(this);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                if (mNoticeManager!=null){
//                    try{
//                        mNoticeManager.sendNotice(1, "收到一条通知消息", "服务接收到通知",true);
//                    }catch (Exception e){
//                        e.printStackTrace();
//                    }
//                }
//            }
//        },10000);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG,"onStartCommand");
        startForeground();
        Bundle mBundle;
        String topic;
        String[] topics;
        if (intent==null||intent.getAction()==null) return START_STICKY;
        switch (intent.getAction()) {
            case PushService.ACTION_CONNECT:
                connect();
                break;
            case PushService.ACTION_DISCONNECT:
                disConnect();
                break;
            case PushService.ACTION_SUBSCRIBE:
                topic = intent.getStringExtra(PARAMS);
                subscribe(topic);
                break;
            case PushService.ACTION_SUBSCRIBES:
                mBundle = intent.getExtras();
                topics = mBundle.getStringArray(PARAMS);
                subscribe(topics);
                break;
            case PushService.ACTION_UNSUBSCRIBE:
                topic = intent.getStringExtra(PARAMS);
                unsubscribe(topic);
                break;
            case PushService.ACTION_UNSUBSCRIBES:
                mBundle = intent.getExtras();
                topics = mBundle.getStringArray(PARAMS);
                unsubscribe(topics);
                break;
            case PushService.ACTION_UNSUBSCRIBE_ALL:
                unsubscribeAllTopics();
                break;
        }
        return START_STICKY;
    }
    //region 对外

    /**
     * 连接mqtt
     * @param context
     */
    public static void connect(Context context){
        Intent intent = new Intent(context,PushService.class);
        intent.setAction(ACTION_CONNECT);
        context.startService(intent);
    }

    /**
     * 断开连接
     * @param context
     */
    public static void disConnect(Context context) {
        Intent mIntent = new Intent(context, PushService.class);
        mIntent.setAction(PushService.ACTION_DISCONNECT);
        context.startService(mIntent);
        context.stopService(mIntent);
    }

    /**
     * 订阅主题
     * @param context
     * @param topic
     */
    public static void subscribe(Context context, String topic) {
        Intent mIntent = new Intent(context, PushService.class);
        mIntent.setAction(PushService.ACTION_SUBSCRIBE);
        mIntent.putExtra(PushService.PARAMS, topic);
        context.startService(mIntent);
    }

    /**
     * 订阅主题（批量）
     * @param context
     * @param topics
     */
    public static void subscribe(Context context, String[] topics) {
        Bundle b = new Bundle();
        b.putStringArray(PushService.PARAMS, topics);
        Intent mIntent = new Intent(context, PushService.class);
        mIntent.setAction(PushService.ACTION_SUBSCRIBES);
        mIntent.putExtras(b);
        context.startService(mIntent);
    }

    /**
     * 取消订阅
     * @param context
     * @param topic
     */
    public static void unsubscribe(Context context, String topic) {
        Intent mIntent = new Intent(context, PushService.class);
        mIntent.setAction(PushService.ACTION_UNSUBSCRIBE);
        mIntent.putExtra(PushService.PARAMS, topic);
        context.startService(mIntent);
    }

    /**
     * 取消订阅（批量）
     * @param context
     * @param topics
     */
    public static void unsubscribe(Context context, String[] topics) {
        Bundle b = new Bundle();
        b.putStringArray(PushService.PARAMS, topics);
        Intent mIntent = new Intent(context, PushService.class);
        mIntent.setAction(PushService.ACTION_UNSUBSCRIBES);
        mIntent.putExtras(b);
        context.startService(mIntent);
    }

    /**
     * 取消订阅所有
     * @param context
     */
    public static void unsubscribeAll(Context context) {
        Intent mIntent = new Intent(context, PushService.class);
        mIntent.setAction(PushService.ACTION_UNSUBSCRIBE_ALL);
        context.startService(mIntent);
    }
    //endregion
    //region 私有方法
    private void connect() {
        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(),URL, "hello_1");
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                Log.e(TAG,"connectComplete，是否重连"+ reconnect);
                MqttMessageEvent event = new MqttMessageEvent();
                event.topic = MqttTopics.TOPIC_MQTT_CONNECTION;
                event.value = "1";
                mSendBroadcast(event);
                if (reconnect){//重新订阅主题
                    reSubscribe();
                }else {
                    subscribe("hello.ptp");
                    subscribe("hello");
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                Log.e(TAG,"connectionLost，连接丢失");
                MqttMessageEvent event = new MqttMessageEvent();
                event.topic = MqttTopics.TOPIC_MQTT_CONNECTION;
                event.value = "0";
                mSendBroadcast(event);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.e(TAG,"messageArrived，主题：" + topic + "\n内容：" + message.toString());
                mSendBroadcast(new MqttMessageEvent());
                mNoticeManager.sendNotice(2,topic,message.toString(),true);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setKeepAliveInterval(10);
        mqttConnectOptions.setConnectionTimeout(50);
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setUserName(USER_NAME);
        mqttConnectOptions.setPassword(PASSWORD.toCharArray());
        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.e(TAG,"mqttAndroidClient.connect onSuccess");

                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(3);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(true);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e(TAG,"mqttAndroidClient.connect onFailure"+ exception.getMessage());
                    exception.printStackTrace();
                }
            });

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 重新订阅主题
     */
    private void reSubscribe() {
        Iterator<Map.Entry<String, String>> it = topicsMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            subscribe(entry.getValue());
        }
    }

    /**
     * 关闭连接
     */
    private void disConnect(){
        try {
            if (mqttAndroidClient != null && mqttAndroidClient.isConnected()) {
                mqttAndroidClient.disconnect();
                mqttAndroidClient = null;
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
        topicsMap.clear();
    }

    /**
     * 订阅主题
     * @param topic
     */
    private void subscribe(String topic){
        if (mqttAndroidClient != null && mqttAndroidClient.isConnected()) {
            try {
                mqttAndroidClient.subscribe(topic, QOS);
                Log.e(TAG,"订阅了主题:"+topic);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
        topicsMap.put(topic, topic);
    }

    /**
     * 取消订阅主题
     * @param topic
     */
    private void unsubscribe(String topic){
        if (mqttAndroidClient != null && mqttAndroidClient.isConnected()) {
            try {
                mqttAndroidClient.unsubscribe(topic);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
        topicsMap.remove(topic);
    }

    /**
     * 批量订阅主题
     * @param topics
     */
    private void subscribe(String[] topics){
        int[] qoses = new int[topics.length];
        for (int i = 0; i < topics.length; i++) {
            qoses[i] = 0;
            topicsMap.put(topics[i], topics[i]);
        }
        if (mqttAndroidClient != null && mqttAndroidClient.isConnected()) {
            try {
                mqttAndroidClient.subscribe(topics, qoses);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 批量取消订阅主题
     * @param topics
     */
    private void unsubscribe(String[] topics){
        if (mqttAndroidClient != null && mqttAndroidClient.isConnected()) {
            try {
                mqttAndroidClient.unsubscribe(topics);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
        for (String topic : topics) {
            topicsMap.remove(topic);
        }
    }

    /**
     * 取消所有主题
     */
    private void unsubscribeAllTopics(){
        String[] topics = new String[topicsMap.size()];
        Iterator<Map.Entry<String, String>> it = topicsMap.entrySet().iterator();
        int i = 0;
        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            topics[i] = entry.getValue();
            i++;
        }
        unsubscribe(topics);
    }


    /**
     * 接收到的信息以广播形式转发
     * @param event
     */
    private void mSendBroadcast(MqttMessageEvent event) {

        Bundle mBundle = new Bundle();
        mBundle.putParcelable(MqttMessageEvent.TAG, event);
        Intent mIntent = new Intent();
        mIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        mIntent.setAction(MqttMessageEvent.TAG);
        mIntent.putExtras(mBundle);
        sendBroadcast(mIntent);
    }
    //endregion
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG,"onDestroy");
        unRegisterSystemReceiver();
    }
    //region 系统广播注册
    private SystemBroadcastReceiver mSysReceiver;

    /**
     * 监听网络变化
     */
    private void registerSystemReceiver() {
        mSysReceiver = new SystemBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mSysReceiver,intentFilter);
    }
    private void unRegisterSystemReceiver(){
        if (mSysReceiver!=null){
            unregisterReceiver(mSysReceiver);
            mSysReceiver = null;
        }
    }
    //endregion

    /**
     * 系统广播监听
     */
    private class SystemBroadcastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case ConnectivityManager.CONNECTIVITY_ACTION:
                    if (NetworkUtils.isConnected()){
                        Log.e("SystemBroadcastReceiver","网络连接上了");
                    }else {
                        Log.e("SystemBroadcastReceiver","网络断了");
                    }
                break;
            }
        }
    }

    //region service保活

    /**
     * 让Service保持前台运行
     */
    private void startForeground(){
        if (Build.VERSION.SDK_INT < 18) {
            startForeground(GRAY_SERVICE_ID, new Notification());//API < 18 ，此方法能有效隐藏Notification上的图标
        } else {
            Intent innerIntent = new Intent(this, GrayInnerService.class);
            startService(innerIntent);
            startForeground(GRAY_SERVICE_ID,new NotificationCompat.Builder(this, NoticeManager.CHANNEL_ID).build());
        }
    }
    /**
     * 给 API >= 18 的平台上用的灰色保活手段
     */
    public static class GrayInnerService extends Service {

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground(GRAY_SERVICE_ID, new NotificationCompat.Builder(this, NoticeManager.CHANNEL_ID).build());
            stopForeground(true);
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }

    }

    //endregion
}
