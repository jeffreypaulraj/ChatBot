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
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    TextView statusText;
    BroadcastReceiver broadcastReceiver;
    SmsManager smsManager;
    String senderNumber = "";
    int currentState = 0;
    boolean requestingToppings = false;
    boolean requestingBeverages = false;
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
            int timeInterval = (int)Math.random()*6000 + 4000;
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
                    statusText.setText("Greeting State");
                    if(checkForWord(receivedMessage, "Hi")||checkForWord(receivedMessage, "Hello")||checkForWord(receivedMessage, "Hey")){
                        sendMessage = "Welcome to Dave's Pizzeria! You can get any information you need here!";
                        currentState = 1;
                    }
                    else if(checkForWord(receivedMessage, "bye")||checkForWord(receivedMessage, "goodbye")){
                        sendMessage = "Why are you leaving? You just got here!";
                    }
                    else{
                        sendMessage = "You didn't say hello... How rude!";
                    }
                }
                else if(currentState == 1){
                    statusText.setText("Informational State");
                    if(checkForWord(receivedMessage, "what") || checkForWord(receivedMessage, "cost") || checkForWord(receivedMessage, "price")){
                        sendMessage = "Here's a quick look at our menu. We sell small, large, and medium pizzas at $9, $12, and $15 respectively." +
                                "Each topping is $0.50 extra and we sell small, medium, and large drinks at $1.50, $2.50, and $3.50 respectively";
                    }
                    else if(checkForWord(receivedMessage, "topping")||checkForWord(receivedMessage, "toppings")){
                        sendMessage = "Here is a list of all our toppings: Pepperoni,Pineapple, Olives, Chicken, Anchovies, and Peppers. Did you need anything else?";
                    }
                    else if(checkForWord(receivedMessage, "beverages")||checkForWord(receivedMessage, "drinks")){
                        sendMessage = "Here is a list of all our beverages: Coke,Pepsi, Fanta, Mountain Dew, Root Beer, Sprite, and Water. Did you need anything else? ";
                    }
                    else if(checkForWord(receivedMessage, "when")){
                        sendMessage = "We are open 7 days a week. On weekdays, we are open from 9 am to 7 pm, and on weekends, we are open from 8 am to 10 pm. Did you need anything else?";
                    }
                    else if(checkForWord(receivedMessage, "order")){
                        sendMessage = "It looks like you are ready to place your order! You can text me your order when you are ready!";
                        currentState = 2;
                    }
                    else{
                        sendMessage = "Sorry, I don't think I can help you out with that. Do you have any other questions or concerns?";
                    }
                }
                else if(currentState == 2){
                    statusText.setText("Order State");
                    ArrayList<String> beverageList = addBeverages(receivedMessage);
                    ArrayList<String> toppingList = addToppings(receivedMessage);
                    if(beverageList.size() == 0){
                        sendMessage = "You haven't ordered any beverages! Would you like to?";
                        requestingBeverages = true;
                    }
                    else if(toppingList.size() == 0){
                        sendMessage = "You haven't ordered any toppings! Would you like to?";
                        requestingToppings = true;
                    }
                    else if(requestingToppings){
                        if(checkForWord(receivedMessage, "yes") || checkForWord(receivedMessage, "yeah")){
                            sendMessage = "Please list any toppings you would like";
                        }
                        else{
                            sendMessage = "That's perfectly fine! Is your order complete";
                        }
                    }
                    else if(requestingBeverages){
                        if(checkForWord(receivedMessage, "yes") || checkForWord(receivedMessage, "yeah")){
                            sendMessage = "Please list any beverages you would like";
                        }
                        else{
                            sendMessage = "That's perfectly fine! Is your order complete";
                        }
                    }
                }
                else if(currentState == 3){
                    statusText.setText("Farewell State");
                }
                smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(senderNumber, null, sendMessage, null, null);
            }
        };

        return runnable;
    }

    public boolean checkForWord(String message, String key){
        ArrayList<String> wordsList = formatMessage(message);

        for(int i = 0; i < wordsList.size(); i++){
            if(wordsList.get(i).equalsIgnoreCase(key)){
                return true;
            }
        }
        return false;
    }

    public ArrayList<String> addToppings(String order){
        ArrayList<String> wordsList = formatMessage(order);
        ArrayList<String> toppingList = new ArrayList<>();
        ArrayList<String> availableToppings = new ArrayList<>(Arrays.asList("Pepperoni","Pineapple", "Olives", "Chicken","Anchovies","Peppers"));
        for(int i = 0; i < wordsList.size(); i++){
            for(int j = 0; j < availableToppings.size(); j++){
                if(wordsList.get(i).equalsIgnoreCase(availableToppings.get(j))){
                    toppingList.add(availableToppings.get(j));
                }
            }
        }
        return toppingList;
    }

    public ArrayList<String> addBeverages(String order){
        ArrayList<String> wordsList = formatMessage(order);
        ArrayList<String> beverageList = new ArrayList<>();
        ArrayList<String> availableBeverages = new ArrayList<>(Arrays.asList("Coke","Pepsi", "Fanta", "Mountain Dew","Root Beer","Sprite", "Water"));
        for(int i = 0; i < wordsList.size(); i++){
            for(int j = 0; j < availableBeverages.size(); j++){
                if(wordsList.get(i).equalsIgnoreCase(availableBeverages.get(j))){
                    beverageList.add(availableBeverages.get(j));
                }
            }
        }
        return beverageList;
    }

    public ArrayList<String> formatMessage(String message){
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

        return wordsList;
    }

}
