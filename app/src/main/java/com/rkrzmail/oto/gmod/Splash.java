package com.rkrzmail.oto.gmod;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.naa.data.UtilityAndroid;

import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.MainActivity;

import java.util.ArrayList;


public class Splash extends AppActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (     (   isPermissionDenied(Manifest.permission.ACCESS_FINE_LOCATION)
                && isPermissionDenied(Manifest.permission.ACCESS_COARSE_LOCATION)
        )
                || isPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                || isPermissionDenied(Manifest.permission.READ_EXTERNAL_STORAGE)
                || isPermissionDenied(Manifest.permission.READ_CONTACTS)
                || isPermissionDenied(Manifest.permission.READ_PHONE_STATE)    )  {

            ArrayList<String> strings = new ArrayList<>();
            add(strings, Manifest.permission.ACCESS_FINE_LOCATION);
            add(strings, Manifest.permission.ACCESS_COARSE_LOCATION);
            add(strings, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            add(strings, Manifest.permission.READ_EXTERNAL_STORAGE);
            add(strings, Manifest.permission.READ_CONTACTS);
            add(strings, Manifest.permission.READ_PHONE_STATE);


            ActivityCompat.requestPermissions(getActivity(),   strings.toArray(new String[strings.size()]) ,123);
            Toast.makeText(this, "Access Permision yg dibutuhkan ditolak", Toast.LENGTH_LONG).show();
            return ;
        }





        next();

        //Log.i("Firebase",  FirebaseInstanceId.getInstance().getToken());

    }
    private void add (ArrayList arrayList, String permision){
        if (ActivityCompat.checkSelfPermission(this, permision) != PackageManager.PERMISSION_GRANTED){
            arrayList.add(permision);
        }
    }
    private boolean isPermissionDenied(String permision){
        return  ActivityCompat.checkSelfPermission(this, permision) != PackageManager.PERMISSION_GRANTED;
    }
    private void next(){
        if (UtilityAndroid.getSetting(getApplicationContext(),"L", "").equalsIgnoreCase("T")){
            Intent intent =  new Intent(Splash.this, MainActivity.class);
            startActivity(intent);
        }else{
            Intent intent =  new Intent(Splash.this, MainActivity.class);
            startActivity(intent);
        }
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 123 &&
            (
                    (  isPermissionDenied(Manifest.permission.ACCESS_FINE_LOCATION)
                      && isPermissionDenied(Manifest.permission.ACCESS_COARSE_LOCATION)
                    )
                    || isPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    || isPermissionDenied(Manifest.permission.READ_EXTERNAL_STORAGE)
                    || isPermissionDenied(Manifest.permission.READ_CONTACTS)
                    || isPermissionDenied(Manifest.permission.READ_PHONE_STATE)
            ))   {
              Toast.makeText(this, "Access Permision yg dibutuhkan ditolak,", Toast.LENGTH_LONG).show();
              finish();
        }else if (requestCode == 123){
            next();
        }
    }
}
