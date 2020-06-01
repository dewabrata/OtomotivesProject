package com.rkrzmail.oto.modules.booking;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.utils.Tools;

import java.util.Map;

public class Booking2_Activity extends AppActivity implements View.OnClickListener {

    private TextView tglBooking, jamJemput, jamAntar, jamBooking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking2_);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_booking2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Booking");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        tglBooking = (TextView) findViewById(R.id.tv_tglBooking_booking2);
        jamAntar = findViewById(R.id.tv_jamAntar_booking2);
        jamJemput = findViewById(R.id.tv_jamJemput_booking2);
        jamBooking = findViewById(R.id.tv_jamBooking_booking2);

       tglBooking.setOnClickListener(this);
       jamAntar.setOnClickListener(this);
       jamJemput.setOnClickListener(this);
       jamBooking.setOnClickListener(this);

        find(R.id.btn_partJasa_booking2, Button.class).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_jamBooking_booking2:
                Tools.getTimePickerDialogTextView(getActivity(), jamBooking);
                break;
            case R.id.tv_tglBooking_booking2:
                Tools.getDatePickerDialogTextView(getActivity(), tglBooking);
                break;
            case R.id.tv_jamAntar_booking2:
                Tools.getTimePickerDialogTextView(getActivity(), jamAntar);
                break;
            case R.id.tv_jamJemput_booking2:
                Tools.getTimePickerDialogTextView(getActivity(), jamJemput);
                break;
            case R.id.btn_simpan_booking2:
                Intent i = new Intent(getActivity(), Booking3_Activity.class);
                startActivity(i);
                finish();
                break;

        }
    }

    private void saveData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("booking1"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    //Intent i = new Intent(getActivity(), Booking2_Activity.class);
//                    if(){
//
//                    }else{
//
//                    }
                } else {
                    showInfo("GAGAL!");
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
}
