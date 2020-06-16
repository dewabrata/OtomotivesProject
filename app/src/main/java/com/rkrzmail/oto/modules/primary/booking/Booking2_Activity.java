package com.rkrzmail.oto.modules.primary.booking;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.utils.Tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class Booking2_Activity extends AppActivity implements View.OnClickListener {

    private TextView tglBooking, jamJemput, jamAntar, jamBooking;
    public static final int REQUEST_BOOKING = 10;

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

        Nson nson = Nson.readNson(getIntentStringExtra("data"));
        if (nson.get("jemput").asString().equalsIgnoreCase("YA")) {
            Tools.setViewAndChildrenEnabled(find(R.id.ly_jemput_booking2, LinearLayout.class), true);
            Tools.setViewAndChildrenEnabled(find(R.id.ly_antar_booking2, LinearLayout.class), true);
        } else if (nson.get("jemput").asString().equalsIgnoreCase("TIDAK")) {
            Tools.setViewAndChildrenEnabled(find(R.id.ly_jemput_booking2, LinearLayout.class), false);
            Tools.setViewAndChildrenEnabled(find(R.id.ly_antar_booking2, LinearLayout.class), false);
            find(R.id.et_biayaTransport_booking2, EditText.class).setVisibility(View.GONE);
        }
        find(R.id.btn_simpan_booking2, Button.class).setOnClickListener(this);
        find(R.id.btn_partJasa_booking2, Button.class).setOnClickListener(this);
    }

    private void componentValidation() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat sdfTimes = new SimpleDateFormat("HH:mm");

        String inputBook = tglBooking.getText().toString();
        String currentDate = sdfDate.format(Calendar.getInstance().getTime());
        String inputTime = jamBooking.getText().toString();
        String currentTime = sdfTimes.format(Calendar.getInstance().getTime());

        try {
            Date dateBook = new SimpleDateFormat("dd/MM/yyyy").parse(inputBook);
            Date currentBook = new SimpleDateFormat("dd/MM/yyyy").parse(currentDate);
            if (dateBook.before(currentBook)) {
                showInfo("Tanggal Booking Tidak Sesuai");
                tglBooking.setText("");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        try {
            Date timeBook = new SimpleDateFormat("HH:mm").parse(inputTime);
            Date currentBook = new SimpleDateFormat("HH:mm").parse(currentTime);
            if (timeBook.before(currentBook)) {
                showInfo("Jam Booking Tidak Sesuai");
                jamBooking.setText("");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
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
            case R.id.btn_partJasa_booking2:
                Intent i = new Intent(getActivity(), Booking3_Activity.class);
                startActivityForResult(i, REQUEST_BOOKING);
                break;
            case R.id.btn_simpan_booking2:
                if (!tglBooking.getText().toString().isEmpty()) {
                    componentValidation();
                    tglBooking.requestFocus();
                    return;
                }
                if (!jamBooking.getText().toString().isEmpty()) {
                    componentValidation();
                    jamBooking.requestFocus();
                    return;
                }
                Nson nson = Nson.readNson(getIntentStringExtra("data"));
                if (nson.get("lokasi").asString().equalsIgnoreCase("BENGKEL")
                        && nson.get("jemput").asString().equalsIgnoreCase("YA")) {
                    showInfo("Diperlukan Memilih Part & Jasa");
                    find(R.id.btn_partJasa_booking2, Button.class).callOnClick();
                    return;
                }
                saveData();
                break;
        }
    }

    private void saveData() {
        // add : CID, action(add), nopol, jeniskendaraan, nopon, nama,
        // pemilik, kondisi, keluhan, lokasi, jemput,
        // antar, layanan, km, alamat, date, user, status,
        // tanggalbook, jambook, jemput, antar, hari, jam, biayatrans, biayalayan, dp
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                Nson nson = Nson.readNson(getIntentStringExtra("data"));

                args.put("action", "add");
                args.put("nopol", nson.get("nopol").asString());
                args.put("jeniskendaraan", nson.get("jeniskendaraan").asString());
                args.put("nopon", nson.get("nopon").asString());
                args.put("nama", nson.get("nama").asString());
                args.put("pemilik", nson.get("pemilik").asString());
                args.put("kondisi", nson.get("kondisi").asString());
                args.put("keluhan", nson.get("keluhan").asString());
                args.put("lokasi", nson.get("lokasi").asString());
                args.put("jemput", nson.get("jemput").asString());
                args.put("tanggalbook", tglBooking.getText().toString());
                args.put("jam", find(R.id.et_menit_booking2, EditText.class).getText().toString());
                args.put("biayatrans", find(R.id.et_biayaTransport_booking2, EditText.class).getText().toString());
                args.put("biayalayanan", find(R.id.et_biayaLayanan_booking2, EditText.class).getText().toString());
                args.put("hari", find(R.id.et_hari_booking2, EditText.class).getText().toString());

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturbooking"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showInfo("Berhasil Menyimpan Aktivitas");
                    Intent i = new Intent(getActivity(), KontrolBooking_Activity.class);
                    startActivity(i);
                    finish();
                } else {
                    showInfo("Gagal Menyimpan Aktivitas");
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_BOOKING) {

        }
    }
}
