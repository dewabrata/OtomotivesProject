package com.rkrzmail.oto.modules.primary.checkin;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.data.UtilityAndroid;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.gmod.Capture;
import com.rkrzmail.oto.modules.LoginActivity;
import com.rkrzmail.oto.modules.user.AturUser_Activity;
import com.rkrzmail.utils.Tools;

import java.util.Map;

public class Checkin4_Activity extends AppActivity implements View.OnClickListener {

    public static final int REQUEST_CODE_SIGN = 10;
    private static final String TAG = "Checking4____";
    private static final int REQUEST_MEKANIK = 11;
    private Bitmap ttd;
    private SeekBar seekBar;
    private Nson mekanikArray = Nson.newArray();

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
        setSpMekanik();
        find(R.id.btn_ttd_checkin4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Capture.class);
                startActivityForResult(intent, REQUEST_CODE_SIGN);
            }
        });
        seekBar = findViewById(R.id.seekBar_bbm);
        seekBar.setMax(100);
        seekBar.setProgress(0);
        seekBar.incrementProgressBy(20);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress = progress / 20;
                progress = progress * 20;
                find(R.id.tv_ketBbbm_checkin4, TextView.class).setText(progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        Nson getData = Nson.readJson(getIntentStringExtra("data"));
        Log.d(TAG, "initComponent: " + getData);

        find(R.id.et_antrian_checkin4, EditText.class);
        try {
            find(R.id.et_totalBiaya_checkin4, EditText.class).setText(formatRp(getData.get("TOTAL_BIAYA").asString()));
        } catch (Exception e) {
            Log.d(TAG, "initComponent: " + e.getMessage());
        }
        find(R.id.et_dp_checkin4, EditText.class);
        find(R.id.et_sisa_checkin4, EditText.class);
        find(R.id.tv_waktu_checkin4, TextView.class).setOnClickListener(this);
        find(R.id.cb_aggrement_checkin4, CheckBox.class);
        //find(R.id.cb_buangPart_checkin4, CheckBox.class).setChecked(true);
        find(R.id.cb_konfirmTambah_checkin4, CheckBox.class);
        find(R.id.cb_tidakMenunggu_checkin4, CheckBox.class);

        initBtn();
    }

    private void initBtn() {
        find(R.id.tv_startEstimasi_checkin4, TextView.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDatePickerDialogTextView(getActivity(), find(R.id.tv_startEstimasi_checkin4, TextView.class));
            }
        });

        find(R.id.tv_finish_checkin4, TextView.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getTimePickerDialogTextView(getActivity(), find(R.id.tv_finish_checkin4, TextView.class));
            }
        });

        find(R.id.btn_hapus, Button.class).setVisibility(View.VISIBLE);
        find(R.id.btn_hapus, Button.class).setText("BATAL");
        find(R.id.btn_hapus, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        find(R.id.btn_simpan, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void setSpMekanik() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson data;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                data = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("mekanik"), args));
            }

            @Override
            public void runUI() {
                if (data.get("status").asString().equalsIgnoreCase("OK")) {
                    if (data.get("data").asArray().size() == 0) {
                        showInfo("Mekanik Belum Tercatatkan, Silahkan Daftarkan Mekanik Di Menu USER");
                        Messagebox.showDialog(getActivity(), "Mekanik Belum Di Catatkan", "Catatkan Mekanik ?", "Ya", "Tidak", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivityForResult(new Intent(getActivity(), AturUser_Activity.class), REQUEST_MEKANIK);
                            }
                        }, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        return;
                    }
                    mekanikArray.add("--PILIH--");
                    for (int i = 0; i < data.get("data").size(); i++) {
                        //idMekanikArray.add(Nson.newObject().set("ID", data.get("data").get(i).get("ID").asString()).set("NAMA", data.get("data").get(i).get("NAMA").asString()));
                        mekanikArray.add(data.get("data").get(i).get("NAMA").asString());
                    }

                    Log.d(TAG, "List : " + mekanikArray);

                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, mekanikArray.asArray());
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    find(R.id.sp_namaMekanik_checkin4, Spinner.class).setAdapter(spinnerAdapter);
                } else {
                    showInfoDialog("Nama Mekanik Gagal Di Muat, Muat Ulang ?", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setSpMekanik();
                        }
                    });
                }

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_waktu_checkin4:
                getTimePickerDialogTextView(getActivity(), find(R.id.tv_waktu_checkin4, TextView.class));
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_SIGN) {
            Log.d(TAG, "onActivityResult: " + getIntentStringExtra(data, "imagePath"));
            ttd = (Bitmap) data.getExtras().get("imagePath");
            find(R.id.img_tandaTangan_checkin4, ImageView.class).setImageBitmap(ttd);
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_MEKANIK) {
            setSpMekanik();
            showSuccess("Berhasil Mencatatkan Mekanik");

        }
    }
}
