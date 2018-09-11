package com.guc.noticetest.mqtt;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by guc on 2018/9/11.
 * 描述：Mqtt接收类
 */
public class MqttMessageEvent implements Parcelable{
    public static final String TAG = "com.mqtt.mqtt_message_event";

    public String topic;
    public String value;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.topic);
        dest.writeString(this.value);
    }

    public MqttMessageEvent() {
    }

    protected MqttMessageEvent(Parcel in) {
        this.topic = in.readString();
        this.value = in.readString();
    }

    public static final Creator<MqttMessageEvent> CREATOR = new Creator<MqttMessageEvent>() {
        @Override
        public MqttMessageEvent createFromParcel(Parcel source) {
            return new MqttMessageEvent(source);
        }

        @Override
        public MqttMessageEvent[] newArray(int size) {
            return new MqttMessageEvent[size];
        }
    };
}
