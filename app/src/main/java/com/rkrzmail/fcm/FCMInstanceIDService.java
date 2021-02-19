package com.rkrzmail.fcm;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.naa.data.UtilityAndroid;


/**
 * Created by rkrzmail on 31/01/2018.
 */

public class FCMInstanceIDService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.i("FirebaseMessage", refreshedToken);
        UtilityAndroid.setSetting(this, "FCMID", refreshedToken);
    }
}
