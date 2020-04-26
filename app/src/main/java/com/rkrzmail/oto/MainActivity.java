package com.rkrzmail.oto;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;
import com.naa.data.Nson;
import com.naa.data.UtilityAndroid;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.fragment.PageAdapter;
import com.rkrzmail.oto.fragment.SlideFragment;
import com.rkrzmail.oto.fragment.pageindicator.CirclePageIndicator;
import com.rkrzmail.oto.gmod.LayananActivity;
import com.rkrzmail.oto.gmod.MessageWA;
import com.rkrzmail.oto.modules.CheckinActivity;
import com.rkrzmail.oto.modules.LoginActivity;
import com.rkrzmail.oto.modules.part.PartActivity;
import com.rkrzmail.oto.modules.part.PartSearchActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class MainActivity extends AppActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        //Log.i("Firebase", refreshedToken);
        if (String.valueOf(refreshedToken).length()>=13){
            UtilityAndroid.setSetting(this, "FCMID", refreshedToken);
        }

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
}
