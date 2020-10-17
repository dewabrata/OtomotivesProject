package com.rkrzmail.oto;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.iid.FirebaseInstanceId;
import com.naa.data.UtilityAndroid;
import com.rkrzmail.oto.modules.LoginActivity;

public class MainActivity extends AppActivity {

    private String[] aksesApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        //Log.i("Firebase", refreshedToken);
        if (String.valueOf(refreshedToken).length()>=13){
            UtilityAndroid.setSetting(this, "FCMID", refreshedToken);
        }

//        RotateAnimation anim = new RotateAnimation(0f, 350f, 15f, 15f);
//        anim.setInterpolator(new LinearInterpolator());
//        anim.setRepeatCount(Animation.INFINITE);
//        anim.setDuration(700);

        //find(R.id.img_splash, ImageView.class).setAnimation(anim);
        find(R.id.id).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getSetting("L").equalsIgnoreCase("L")){
                    //login
                    Intent intent = new Intent(getActivity(), MenuActivity.class);
                    startActivity(intent);
                }else{
                    //menu
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                }
                finish();
            }
        },3000);
    }

    @Override
    public void onBackPressed() {

    }
}
