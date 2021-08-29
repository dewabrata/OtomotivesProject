package com.rkrzmail.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

public class PermissionUtils {

    public static boolean checkContactPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkLocagtionPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED ;
    }

    private boolean checkStoragePermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkCameraPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }



}
