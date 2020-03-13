package com.example.chatbotapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    TextView statusText;
    BroadcastReceiver broadcastReceiver;
    SmsManager smsManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.id_statusText);

    }

    @Override
    protected void onResume() {
        super.onResume();
        broadcastReceiver = new TextMonitor();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(broadcastReceiver, intentFilter);

        smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage("5554", null, "Hi There", null, null);

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }


    public class TextMonitor extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle allInfo = intent.getExtras();
            Object[] objectArr = (Object[]) allInfo.get("pdus");
            String format = intent.getStringExtra("format");
            SmsMessage[] smsArr = new SmsMessage[objectArr.length];
            for(int i = 0; i < objectArr.length; i++){
                smsArr[i]  = SmsMessage.createFromPdu((byte[])objectArr[i], format);
            }

            statusText.setText(smsArr[0].getMessageBody());

        }
    }
    
}
