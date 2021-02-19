package com.rkrzmail.srv;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.naa.data.UtilityAndroid;
import com.rkrzmail.oto.gmod.Splash;
import com.rkrzmail.oto.gmod.Update;


public class OtoReceiver extends BroadcastReceiver {
	
     public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()){
            case "notifyMekanik":
                Log.e("otoReceiver", "onReceive: " + "mekanik");
                break;
            case "notifyPart":
                Log.e("otoReceiver", "onReceive: " + "part");
                break;
        }
         if (intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")) {
             //internet(context, intent);
         }
         if (intent.getAction().equals("android.net.wifi.WIFI_STATE_CHANGED")) {
             //internet(context, intent);
         }
         if ("com.nikita.generator.service".equals(intent.getAction())) {
             //delay
             new Thread(new Runnable() {
                 Intent intent;
                 Context context;
                 public void run() {
                     try {
                         Thread.sleep(300);
                     }catch (Exception e){}
                     if (String.valueOf(intent.getStringExtra("action")).equalsIgnoreCase("logout")){
                         UtilityAndroid.removeSettingAll(context);
                         Intent intent = new Intent(context, Splash.class);
                         intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                         context.startActivity(intent);
                     }else if (String.valueOf(intent.getStringExtra("action")).equalsIgnoreCase("update")){
                         UtilityAndroid.removeSettingAll(context);
                         Intent intent = new Intent(context,Update.class);
                         intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                         context.startActivity(intent);
                     }
                 }
                 public Runnable get(Context context, Intent intent){
                     this.context = context;
                     this.intent = intent;
                     return this;
                 }
             }.get(context, intent)).start();

         }
    }
    private void internet(Context context, Intent filter){
        Intent intent = new Intent();
        intent.setAction("com.rkrzmail.loyalty");
        context.sendBroadcast(intent);
    }

}
