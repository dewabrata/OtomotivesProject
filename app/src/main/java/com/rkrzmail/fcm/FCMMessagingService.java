package com.rkrzmail.fcm;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.naa.data.Nson;
import com.naa.data.UtilityAndroid;

import com.rkrzmail.oto.MainActivity;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.mekanik.PerintahKerjaMekanik_Activity;
import com.rkrzmail.oto.modules.sparepart.TugasPart_MainTab_Activity;

import java.util.Random;


/**
 * Created by rkrzmail on 31/01/2018.
 */


public class FCMMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Nson data = Nson.newObject();

        if (remoteMessage.getNotification() != null) {
            data.set("msg", String.valueOf(remoteMessage.getNotification().getBody()));
            data.set("title", String.valueOf(remoteMessage.getNotification().getTitle()));
        }
        if (remoteMessage.getData() != null) {
            data.asObject().putAll(remoteMessage.getData());
        }

        Log.i("FirebasegetMessaging", data.toJson());

        /*if (data.containsKey("dispatch")) {
            UtilityAndroid.setSetting(getApplicationContext(), "FCM-DISP", "SYNC_NOW");
            Intent intent1 = new Intent();
            intent1.setAction("com.rkrzmail.toms.service");
            intent1.putExtra("Origin", "FCM-DISP");
            sendBroadcast(intent1);
        }
        if (data.containsKey("sync")) {
            //triger bacgroud proses
         Intent intent1 = new Intent(this, SmartService.class);
          intent1.putExtra("ACTION", "SYNC_NOW");
          startService(intent1);
        }*/
        if(data.get("CID").asString().equals(UtilityAndroid.getSetting(getApplicationContext(), "CID", ""))){
            if (data.get("msg").asString().equalsIgnoreCase("Perintah Kerja Mekanik") &&
                    data.get("USER").asString().equalsIgnoreCase(UtilityAndroid.getSetting(getApplicationContext(), "user", ""))) {
                sendBroadcast(new Intent().setAction("notifyMekanik"));
                showNotification(getApplicationContext(), data, "MEKANIK", new Intent(getApplicationContext(), PerintahKerjaMekanik_Activity.class));
            } else if (data.get("msg").asString().equalsIgnoreCase("Tambah Part - Jasa")) {
                showNotification(getApplicationContext(), data, "PART", new Intent(getApplicationContext(), TugasPart_MainTab_Activity.class));
            }
        }

        /*if (data.containsKey("msg") && data.get("USER").asString().equals(UtilityAndroid.getSetting(this, "user", ""))) {
            showNotification(data);

            if (data.get("id").asInteger() >= 100) {
                Intent intent1 = new Intent();
                intent1.setAction("com.rkrzmail.toms.fcm");
                intent1.putExtra("fcm", "inbox");
                sendBroadcast(intent1);
            }
        }*/
    }

    @SuppressLint({"NewApi", "LocalSuppress", "InlinedApi"})
    public void showNotification(Context context, Nson nson, String channelName, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationId = new Random().nextInt();
        int importance = NotificationManager.IMPORTANCE_HIGH;
        String channelId = "channel-01";

        NotificationChannel mChannel = new NotificationChannel(
                channelId, channelName, importance);
        notificationManager.createNotificationChannel(mChannel);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.speed)
                .setContentTitle(nson.get("tittle").asString())
                .setContentText(nson.get("msg").asString());

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(alarmSound);
        mBuilder.setOnlyAlertOnce(true);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        mBuilder.setContentIntent(resultPendingIntent);
        notificationManager.notify(notificationId, mBuilder.build());
    }

}
