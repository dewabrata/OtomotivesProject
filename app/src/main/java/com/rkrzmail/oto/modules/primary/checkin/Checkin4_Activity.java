package com.rkrzmail.oto.modules.primary.checkin;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.gmod.Capture;
import com.rkrzmail.utils.Tools;

public class Checkin4_Activity extends AppActivity implements View.OnClickListener {

    public static final int REQUEST_CODE_SIGN = 10;
    private Bitmap ttd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkin4_);
        initComponent();
        ttd = null;
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Check-In");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        find(R.id.btn_ttd_checkin4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Capture.class);
                intent.putExtra("STATUS", "");
                setResult(RESULT_OK);
                startActivityForResult(intent, REQUEST_CODE_SIGN);
            }
        });

        find(R.id.seekBar_bbm, SeekBar.class).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int seekBarProgress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekBarProgress = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                find(R.id.tv_ketBbbm_checkin4, TextView.class).setText(seekBarProgress + "%");
            }
        });

        find(R.id.et_antrian_checkin4, EditText.class);
        find(R.id.et_Emulai_checkin4, EditText.class);
        find(R.id.et_Eselesai_checkin4, EditText.class);
        find(R.id.et_Elama_checkin4, EditText.class);
        find(R.id.et_totalBiaya_checkin4, EditText.class);
        find(R.id.et_dp_checkin4, EditText.class);
        find(R.id.et_sisa_checkin4, EditText.class);
        find(R.id.tv_waktu_checkin4, TextView.class).setOnClickListener(this);
        find(R.id.cb_aggrement_checkin4, CheckBox.class);
        find(R.id.cb_buangPart_checkin4, CheckBox.class);
        find(R.id.cb_konfirmTambah_checkin4, CheckBox.class);
        find(R.id.cb_tidakMenunggu_checkin4, CheckBox.class);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_waktu_checkin4:
                Tools.getDatePickerDialogTextView(getActivity(), find(R.id.tv_waktu_checkin4, TextView.class));
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_SIGN) {
            ttd = (Bitmap) data.getExtras().get("imagePath");
            find(R.id.img_tandaTangan_checkin4, ImageView.class).setImageBitmap(ttd);
        }
    }
}
