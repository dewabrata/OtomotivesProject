package com.rkrzmail.oto.modules.checkin;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;

public class Checkin1_Activity extends AppActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkin1_);
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_checkin1);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Check-In");
        setTitleColor(getResources().getColor(R.color.white_transparency));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {

    }
}
