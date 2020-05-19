package com.rkrzmail.oto.modules.booking;

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


    private TextView tglBooking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking2_);
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_booking2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Booking 2");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        tglBooking = (TextView) findViewById(R.id.tv_tglBooking_booking2);
        find(R.id.btn_getTgl_booking2, Button.class).setOnClickListener(this);
        find(R.id.btn_getJam_booking2, Button.class).setOnClickListener(this);
        find(R.id.btn_getJemput_booking2, Button.class).setOnClickListener(this);
        find(R.id.btn_getAntar_booking2, Button.class).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_getTgl_booking2:
                Tools.getDatePickerDialog(getActivity(), (EditText) tglBooking);
                break;
            case R.id.btn_getJam_booking2:
                break;
            case R.id.btn_getJemput_booking2:
                break;
            case R.id.btn_getAntar_booking2:
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
}
