package com.example.chatbotapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    TextView statusText;
    BroadcastReceiver broadcastReceiver;
    SmsManager smsManager;
    String senderNumber = "";
    int currentState = 0;
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
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }


    public class TextMonitor extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(MainActivity.this, "Text Received", Toast.LENGTH_SHORT).show();
            Bundle allInfo = intent.getExtras();
            Object[] objectArr = (Object[]) allInfo.get("pdus");
            String format = intent.getStringExtra("format");
            SmsMessage[] smsArr = new SmsMessage[objectArr.length];
            for(int i = 0; i < objectArr.length; i++){
                smsArr[i]  = SmsMessage.createFromPdu((byte[])objectArr[i], format);
            }

            statusText.setText(smsArr[0].getMessageBody());
            senderNumber = smsArr[0].getOriginatingAddress();

            Handler handler = new Handler();
            int timeInterval = (int)Math.random()*5000;
            handler.postDelayed(sendMessageRunnable(smsArr[0].getMessageBody()), timeInterval);

        }
    }


    public Runnable sendMessageRunnable(String message){
        final String receivedMessage = message;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                String sendMessage = "";
                if(currentState == 0){
                    if(checkForWord(receivedMessage, "Hi")||checkForWord(receivedMessage, "Hello")||checkForWord(receivedMessage, "Hey")){
                        sendMessage = "Greetings!";
                    }
                    else{
                        sendMessage = "You didn't say hello... How rude!";
                    }
                }
                smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(senderNumber, null, sendMessage, null, null);
            }
        };

        return runnable;
    }

    public boolean checkForWord(String message, String key){
        ArrayList<String> wordsList = new ArrayList<>();
        int currentIndex = 0;
        int numSpaces = 0;
        for(int i = 0; i < message.length(); i++){
            if(message.charAt(i) == ' '){
                wordsList.add(message.substring(currentIndex, i));
                currentIndex = i;
                numSpaces++;
            }
        }
        if(numSpaces == 0){
            wordsList.add(message);
        }

        for(int i = 0; i < wordsList.size(); i++){
            if(wordsList.get(i).equalsIgnoreCase(key)){
                return true;
            }
        }

        return false;
    }


}
