package com.rkrzmail.oto;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatDelegate;
import android.telephony.TelephonyManager;

import com.naa.data.Utility;
import com.naa.data.UtilityAndroid;
import com.naa.utils.InternetX;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by rkrzmail on 13/06/2017.
 */

public class AppApplication extends Application {
    private static AppApplication appSystem;

    @Override
    public void onCreate() {
        super.onCreate();
        appSystem = this;

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

    }

    public static AppApplication getInstance() {
        return appSystem;
    }

    public static String getBaseUrl(String name) {
        return "https://otomotives.com/api/" + name;
    }

    public static String getBaseUrlV2(String name) {
        return "https://otomotives.com/api/v2/apitest/" + name;
    }

    //production
    /*public static String getBaseUrlV3(String name) {
        return "https://otomotives.com/api/v3/" + name;
    }

    public static String getBaseUrlV4(String name) {//ready for antrian
        return "https://otomotives.com/api/v4/" + name;
    }*/

    //dev
   public static String getBaseUrlV3(String name) {
        return BuildConfig.getBaseUrlV3 + name;
    }

    public static String getBaseUrlV4(String name) {
        return BuildConfig.getBaseUrlV4 + name;
    }

    /*Mas Brahma, ni link nya :
    Pengajuan,
    http://202.56.171.19:8185/loyaltyui/home/pengajuan
    MyAccount,
    http://202.56.171.19:8185/loyaltyui/menu/account*/

    public static void getMessageTrigger() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                InternetX.postHttpConnection("https://oto.neyama.com/wapi/queueoto.php", args);
            }
        }).start();
    }

    public static String getUrl(String name) {
        return getBaseUrl(name) + "?key=" + UtilityAndroid.getSetting(getInstance().getApplicationContext(), "KEY", "");
    }


    private static String slocation = "0,0";

    public static String getLastCurrentLocation() {
        return UtilityAndroid.getSetting(getInstance().getApplicationContext(), "LOCATION", "0,0");
    }

    public static void setLastCurrentLocation(Location location) {
        if (location != null) {
            slocation = location.getLatitude() + "," + location.getLongitude();
            UtilityAndroid.setSetting(getInstance().getApplicationContext(), "LOCATION", slocation);
        }
    }

    @SuppressLint("HardwareIds")
    public HashMap<String, String> getArgsData() {
        HashMap<String, String> hashtable = new HashMap();

        //hashtable.put("userId", UtilityAndroid.getSetting(getApplicationContext(), "USER_ID", ""));
        if(!UtilityAndroid.getSetting(getApplicationContext(), "MERK_KENDARAAN_ARRAY", "").isEmpty()){
            hashtable.put("merkKendaraanArray", UtilityAndroid.getSetting(getApplicationContext(), "MERK_KENDARAAN_ARRAY", ""));
        }
        if(!UtilityAndroid.getSetting(getApplicationContext(), "BIDANG_USAHA_ARRAY", "").isEmpty()){
            hashtable.put("bidangUsahaArray", UtilityAndroid.getSetting(getApplicationContext(), "BIDANG_USAHA_ARRAY", ""));
        }
        hashtable.put("merkKendaraanBengkel", UtilityAndroid.getSetting(getApplicationContext(), "MERK_KENDARAAN_BENGKEL", ""));
        hashtable.put("bidangUsahaBengkel", UtilityAndroid.getSetting(getApplicationContext(), "KATEGORI_BENGKEL", ""));
        hashtable.put("user", UtilityAndroid.getSetting(getApplicationContext(), "user", ""));
        hashtable.put("session", UtilityAndroid.getSetting(getApplicationContext(), "session", ""));
        hashtable.put("namaUser", UtilityAndroid.getSetting(getApplicationContext(), "NAMA_USER", ""));
        hashtable.put("CID", UtilityAndroid.getSetting(getApplicationContext(), "CID", "").trim());
        hashtable.put("FCM", UtilityAndroid.getSetting(getApplicationContext(), "FCMID", ""));
        hashtable.put("jenisKendaraan", UtilityAndroid.getSetting(getApplicationContext(), "JENIS_KENDARAAN", ""));
        hashtable.put("date", Utility.NowOnlyDate());
        hashtable.put("dateTime", Utility.NowDateTime());
        hashtable.put("time", Utility.NowTime());
        hashtable.put("Location", AppApplication.getLastCurrentLocation());
        if (hashtable.containsValue("--PILIH--")) {
            hashtable.values().remove("--PILIH--");
        }

        int vi = TimeZone.getDefault().getRawOffset() / 1000;
        hashtable.put("xzona", String.valueOf(vi / 60 / 60));//gmt
        hashtable.put("time", String.valueOf(Calendar.getInstance().getTimeInMillis()));
        try {
            PackageInfo pInfo = AppApplication.getInstance().getPackageManager().getPackageInfo(AppApplication.getInstance().getPackageName(), 0);
            hashtable.put("version", pInfo.versionName);
        } catch (Exception e) {
            hashtable.put("version", UtilityAndroid.getSetting(getApplicationContext(), "VERSION", ""));
        }
        hashtable.put("xcount", "1");
        TelephonyManager mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

            hashtable.put("imei", "");
            hashtable.put("imsi", "");
            hashtable.put("simsn", "");
            hashtable.put("line", "");
        } else {
           /* hashtable.put("imei", String.valueOf(mTelephonyMgr.getDeviceId()));
            hashtable.put("imsi", String.valueOf(mTelephonyMgr.getSubscriberId()));
            hashtable.put("simsn", String.valueOf(mTelephonyMgr.getSimSerialNumber()));
            hashtable.put("line", String.valueOf(mTelephonyMgr.getLine1Number()));*/
        }


        /*try {
            TelephonyManager telephonyManager = (TelephonyManager) AppApplication.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
            hashtable.put("imei", telephonyManager.getDeviceId() );
        } catch (Exception e) { hashtable.put("imei", ""); }*/


        return hashtable;
    }

}
