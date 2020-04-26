package com.rkrzmail.fcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.naa.data.Nson;
import com.naa.data.UtilityAndroid;

import com.rkrzmail.oto.MainActivity;
import com.rkrzmail.oto.R;


/**
 * Created by rkrzmail on 31/01/2018.
 */

public class FCMMessagingService extends FirebaseMessagingService {
  public void onMessageReceived(RemoteMessage remoteMessage) {
      Nson data = Nson.newObject();

      if (remoteMessage.getNotification()!=null){
        data.set("msg",    String.valueOf(remoteMessage.getNotification().getBody()) );
        data.set("title",    String.valueOf(remoteMessage.getNotification().getTitle()) );
      }
      if (remoteMessage.getData()!=null){
        data.asObject().putAll(remoteMessage.getData());
      }

      Log.i("FirebasegetMessaging", data.toJson());

      if (data.containsKey("dispatch")){
          UtilityAndroid.setSetting(getApplicationContext() , "FCM-DISP", "SYNC_NOW");
          Intent intent1 = new Intent();
          intent1.setAction("com.rkrzmail.toms.service");
          intent1.putExtra("Origin", "FCM-DISP");
          sendBroadcast(intent1);
      }
      if (data.containsKey("sync")){
          //triger bacgroud proses
         /* Intent intent1 = new Intent(this, SmartService.class);
          intent1.putExtra("ACTION", "SYNC_NOW");
          startService(intent1);*/
      }
      if (data.containsKey("msg")){
          showNotification(data);

          if (data.get("id").asInteger()>=100){
              Intent intent1 = new Intent();
              intent1.setAction("com.rkrzmail.toms.fcm");
              intent1.putExtra("fcm", "inbox");
              sendBroadcast(intent1);
          }
      }
  }
  public void showNotification(Nson nson) {


    //Get an instance of NotificationManager//
    Intent intentAction = new Intent(this, MainActivity.class);
    intentAction.putExtra("target","notification");
    intentAction.putExtra("id", nson.get("id").asString());
    intentAction.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);


    PendingIntent pIntentlogin = PendingIntent.getBroadcast(this,1,intentAction,PendingIntent.FLAG_UPDATE_CURRENT);


    intentAction = new Intent(this, MainActivity.class);
    intentAction.putExtra("action","TUNDA");
    intentAction.putExtra("id",nson.get("id").asString());
    PendingIntent pIntentlogin3 = PendingIntent.getBroadcast(this,2,intentAction,PendingIntent.FLAG_UPDATE_CURRENT);


    Intent intent = new Intent( this , MainActivity. class );
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    intent.putExtra("target","notification");
    PendingIntent resultIntent = PendingIntent.getActivity( this , 0, intent,  PendingIntent.FLAG_ONE_SHOT);


    NotificationCompat.Builder mBuilder =
            new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.mn_checkin)
                    //.addAction(new NotificationCompat.Action(0, "OK", pIntentlogin))
                    //.addAction(new NotificationCompat.Action(0, "TUNDA", pIntentlogin3))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(nson.get("msg").asString()))
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setOngoing(nson.get("outgoing").asBoolean())
                    .setContentTitle(nson.get("title").asString())
                    .setContentIntent(resultIntent)
                    .setAutoCancel( true )
                    .setContentText(nson.get("msg").asString());



    // Gets an instance of the NotificationManager service//

    NotificationManager mNotificationManager =
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

    // When you issue multiple notifications about the same type of event,
    // it’s best practice for your app to try to update an existing notification
    // with this new information, rather than immediately creating a new notification.
    // If you want to update this notification at a later date, you need to assign it an ID.
    // You can then use this ID whenever you issue a subsequent notification.
    // If the previous notification is still visible, the system will update this existing notification,
    // rather than create a new one. In this example, the notification’s ID is 001//

    mNotificationManager.notify(nson.get("id").asInteger(), mBuilder.build());
  }
}
