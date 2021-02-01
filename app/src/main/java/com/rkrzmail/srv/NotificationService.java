package com.rkrzmail.srv;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.naa.data.Nson;
import com.naa.data.UtilityAndroid;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.mekanik.PerintahKerjaMekanik_Activity;

import java.util.Map;
import java.util.Random;

import static com.rkrzmail.utils.APIUrls.VIEW_PERINTAH_KERJA_MEKANIK;
import static com.rkrzmail.utils.APIUrls.VIEW_TUGAS_PART;

public class NotificationService extends Service {

    private final long IDDLE_TIME = 2 * 60 * 1000; // 15 MINS IDLE TIME
    private final IBinder mBinder = new LocalBinder();
    private Handler handler;
    public static CountDownTimer refreshNotifications;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());
        refreshNotifications = new CountDownTimer(IDDLE_TIME, 1000) {
            @SuppressLint("DefaultLocale")
            @Override
            public void onTick(long timer) {
                int seconds = (int) (timer / 1000) % 60;
                int minutes = (int) ((timer / (1000 * 60)) % 60);

                String time = String.format("%02d:%02d", minutes, seconds);
                Log.e("logoutService", "service running " + time);
            }
            @Override
            public void onFinish() {
                Log.e("logoutService", "Call Logout by Service");
                viewPartPermintaan();
                viewPerintahMekanik();
                stopSelf();
            }
        };

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public NotificationService getService() {
            return NotificationService.this;
        }
    }

    public void startTimer(){
        refreshNotifications.start();
    }

    public void stopTimer(){
        refreshNotifications.cancel();
    }

    @SuppressLint("NewApi")
    public void viewPartPermintaan() {
        newTask(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "NOTIFICATION");

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_TUGAS_PART), args));
            }

            @SuppressLint("NewApi")
            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data");
                    if (result.size() > 0) {
                        saveBySP("TUGAS_PART_SIZE", "" + result.size());
                    }
                }
            }
        });
    }

    private void viewPerintahMekanik() {
        newTask(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_PERINTAH_KERJA_MEKANIK), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data");
                    if(result.size() > 0){
                        Intent intent = new Intent(NotificationService.this, PerintahKerjaMekanik_Activity.class);
                        intent.putExtra("NOPOL", result.get(0).get("NOPOL").asString());
                        showNotification(
                                NotificationService.this,
                                "Perintah Kerja Mekanik",
                                "Mekanik",
                                "MEKANIK",
                                intent);
                    }
                }
            }
        });
    }

    public void saveBySP(String key, String value) {
        UtilityAndroid.setSetting(NotificationService.this, key, value);
    }

    public void newTask(Messagebox.DoubleRunnable runnable) {
        MessageMsg.newTask(null, runnable);
    }

    public void showNotification(Context context, String title, String body, String channelName, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationId = new Random().nextInt();
        String channelId = "channel-01";
        @SuppressLint("InlinedApi") int importance = NotificationManager.IMPORTANCE_HIGH;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.speed)
                .setContentTitle(title)
                .setContentText(body);

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
