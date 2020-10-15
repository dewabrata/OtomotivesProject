package com.rkrzmail.oto.modules.checkin;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class Booking2_Activity extends AppActivity implements View.OnClickListener {

    private TextView tglBooking, jamJemput, jamAntar, jamBooking;
    public static final int REQUEST_BOOKING = 10;
    private int countClick = 0;
    private DecimalFormat formatter;

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
        formatter = new DecimalFormat("###,###,###");
        tglBooking = findViewById(R.id.tv_tglBooking_booking2);
        jamAntar = findViewById(R.id.tv_jamAntar_booking2);
        jamJemput = findViewById(R.id.tv_jamJemput_booking2);
        jamBooking = findViewById(R.id.tv_jamBooking_booking2);

        tglBooking.setOnClickListener(this);
        jamAntar.setOnClickListener(this);
        jamJemput.setOnClickListener(this);
        jamBooking.setOnClickListener(this);

        Nson nson = Nson.readNson(getIntentStringExtra("data"));
        if (nson.get("antarjemput").asString().equalsIgnoreCase("YA")
                && nson.get("lokasi").asString().equalsIgnoreCase("BENGKEL")) {
            Tools.setViewAndChildrenEnabled(find(R.id.ly_jemput_booking2, LinearLayout.class), true);
            Tools.setViewAndChildrenEnabled(find(R.id.ly_antar_booking2, LinearLayout.class), true);
        }else if (nson.get("antarjemput").asString().equalsIgnoreCase("TIDAK")) {
            Tools.setViewAndChildrenEnabled(find(R.id.ly_jemput_booking2, LinearLayout.class), false);
            Tools.setViewAndChildrenEnabled(find(R.id.ly_antar_booking2, LinearLayout.class), false);
            find(R.id.et_biayaTransport_booking2, EditText.class).setVisibility(View.GONE);
        }
        if (nson.get("lokasi").asString().equalsIgnoreCase("HOME")) {
            showInfo("Diperlukan Memilih Part & Jasa");
            if (countClick == 0) {
                find(R.id.btn_simpan_booking2, Button.class).setEnabled(false);
            }
        }

        find(R.id.btn_simpan_booking2, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(componentValidation()){
                    return;
                }
                saveData();
            }
        });

        find(R.id.btn_partJasa_booking2, Button.class).setOnClickListener(this);
    }

    private boolean componentValidation() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat sdfTimes = new SimpleDateFormat("HH:mm");
        String inputBook = tglBooking.getText().toString();
        String currentDate = sdfDate.format(Calendar.getInstance().getTime());
        String inputTime = jamBooking.getText().toString();
        String currentTime = sdfTimes.format(Calendar.getInstance().getTime());

        if (!tglBooking.getText().toString().isEmpty()) {
            try {
                Date dateBook = new SimpleDateFormat("dd/MM/yyyy").parse(inputBook);
                Date currentBook = new SimpleDateFormat("dd/MM/yyyy").parse(currentDate);
                if (dateBook.before(currentBook)) {
                    showInfo("Tanggal Booking Tidak Sesuai");
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }else{
            showInfo("Tanggal Booking Perlu Di Isi");
            return false;
        }
        if (!jamBooking.getText().toString().isEmpty()) {
            try {
                Date timeBook = new SimpleDateFormat("HH:mm").parse(inputTime);
                Date currentBook = new SimpleDateFormat("HH:mm").parse(currentTime);
                if (timeBook.before(currentBook)) {
                    showInfo("Jam Booking Tidak Sesuai");
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }else{
            showInfo("Jam Booking Perlu Di Isi");
            return false;
        }

        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_jamBooking_booking2:
                getTimePickerDialogTextView(getActivity(), jamBooking);
                break;
            case R.id.tv_tglBooking_booking2:
                getDatePickerDialogTextView(getActivity(), tglBooking);
                break;
            case R.id.tv_jamAntar_booking2:
                getTimePickerDialogTextView(getActivity(), jamAntar);
                break;
            case R.id.tv_jamJemput_booking2:
                getTimePickerDialogTextView(getActivity(), jamJemput);
                break;
            case R.id.btn_partJasa_booking2:
                Intent i = new Intent(getActivity(), Booking3_Activity.class);
                i.putExtra("booking1", Nson.readJson(getIntentStringExtra("data")).toJson());
                //Log.d("Booking2List", "KM : " + Nson.readJson(getIntentStringExtra("data")));
                startActivityForResult(i, REQUEST_BOOKING);
                break;
        }
    }

    private void saveData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                Nson nson = Nson.readNson(getIntentStringExtra("data"));

                if (nson.get("statusbook").asString().equalsIgnoreCase("BOOK JEMPUT")) {
                    args.put("action", "add");
                    args.put("statusbook", "BOOK JEMPUT"); //
                    args.put("nopol", nson.get("nopol").asString());
                    args.put("jeniskendaraan", nson.get("jeniskendaraan").asString());
                    args.put("nopon", nson.get("nopon").asString());
                    args.put("nama", nson.get("nama").asString());
                    args.put("pemilik", nson.get("pemilik").asString());
                    args.put("kondisi", nson.get("kondisi").asString());
                    args.put("keluhan", nson.get("keluhan").asString());
                    args.put("layanan", find(R.id.et_biayaLayanan_booking2, EditText.class).getText().toString());
                    args.put("km", nson.get("km").asString());
                    args.put("date", currentDateTime());
                    args.put("tanggalbook", tglBooking.getText().toString());
                    args.put("jambook", find(R.id.et_menit_booking2, EditText.class).getText().toString());
                    args.put("waktupengerjaan", find(R.id.et_hari_booking2, EditText.class).getText().toString() + find(R.id.et_menit_booking2, EditText.class).getText().toString());
                    args.put("biayalayanan", find(R.id.et_biayaLayanan_booking2, EditText.class).getText().toString());
                    args.put("total", find(R.id.et_totalBiaya_booking2, EditText.class).getText().toString());
                    args.put("dp", find(R.id.et_dp_booking2, EditText.class).getText().toString());
                    args.put("sisa", find(R.id.et_sisa_booking2, EditText.class).getText().toString());
                    args.put("antarjemput", nson.get("jemput").asString());
                    args.put("longlat", nson.get("longlat").asString());
                    args.put("alamat", nson.get("alamat").asString());
                    args.put("jemput", nson.get("jemput").asString());
                    args.put("antar", jamAntar.getText().toString());
                    args.put("biayatransport", find(R.id.et_biayaTransport_booking2, EditText.class).getText().toString());
                    args.put("lokasilayanan", nson.get("lokasi").asString());
                } else if (nson.get("statusbook").asString().equalsIgnoreCase("BOOK DEREK")) {
                    args.put("action", "add");
                    args.put("statusbook", "BOOK DEREK");
                    args.put("nopol", nson.get("nopol").asString());
                    args.put("jeniskendaraan", nson.get("jeniskendaraan").asString());
                    args.put("nopon", nson.get("nopon").asString());
                    args.put("nama", nson.get("nama").asString());
                    args.put("pemilik", nson.get("pemilik").asString());
                    args.put("kondisi", nson.get("kondisi").asString());
                    args.put("keluhan", nson.get("keluhan").asString());
                    args.put("layanan", find(R.id.et_biayaLayanan_booking2, EditText.class).getText().toString());
                    args.put("km", nson.get("km").asString());
                    args.put("lokasilayanan", nson.get("lokasi").asString());
                    args.put("antarjemput", nson.get("jemput").asString());
                    args.put("longlat", nson.get("longlat").asString());
                    args.put("alamat", nson.get("alamat").asString());
                    args.put("date", currentDateTime());
                    args.put("tanggalbook", tglBooking.getText().toString());
                    args.put("jambook", find(R.id.et_menit_booking2, EditText.class).getText().toString());
                    args.put("jemput", nson.get("jemput").asString());
                    args.put("waktupengerjaan", find(R.id.et_hari_booking2, EditText.class).getText().toString() + find(R.id.et_menit_booking2, EditText.class).getText().toString());
                    args.put("biayalayanan", find(R.id.et_biayaLayanan_booking2, EditText.class).getText().toString());
                    args.put("biayatransport", find(R.id.et_biayaTransport_booking2, EditText.class).getText().toString());
                    args.put("total", find(R.id.et_totalBiaya_booking2, EditText.class).getText().toString());
                    args.put("dp", find(R.id.et_dp_booking2, EditText.class).getText().toString());
                    args.put("sisa", find(R.id.et_sisa_booking2, EditText.class).getText().toString());
                } else if (nson.get("statusbook").asString().equalsIgnoreCase("BOOK BENGKEL")) {
                    args.put("action", "add");
                    args.put("statusbook", "BOOK BENGKEL");
                    args.put("nopol", nson.get("nopol").asString());
                    args.put("jeniskendaraan", nson.get("jeniskendaraan").asString());
                    args.put("nopon", nson.get("nopon").asString());
                    args.put("nama", nson.get("nama").asString());
                    args.put("pemilik", nson.get("pemilik").asString());
                    args.put("kondisi", nson.get("kondisi").asString());
                    args.put("keluhan", nson.get("keluhan").asString());
                    args.put("layanan", find(R.id.et_biayaLayanan_booking2, EditText.class).getText().toString());
                    args.put("km", nson.get("km").asString());
                    args.put("date", currentDateTime());
                    args.put("tanggalbook", tglBooking.getText().toString());
                    args.put("jambook", find(R.id.et_menit_booking2, EditText.class).getText().toString());
                    args.put("waktupengerjaan", find(R.id.et_hari_booking2, EditText.class).getText().toString() + find(R.id.et_menit_booking2, EditText.class).getText().toString());
                    args.put("biayalayanan", find(R.id.et_biayaLayanan_booking2, EditText.class).getText().toString());
                    args.put("total", find(R.id.et_totalBiaya_booking2, EditText.class).getText().toString());
                    args.put("dp", find(R.id.et_dp_booking2, EditText.class).getText().toString());
                    args.put("sisa", find(R.id.et_sisa_booking2, EditText.class).getText().toString());
                } else if (nson.get("statusbook").asString().equalsIgnoreCase("BOOK HOME")) {
                    args.put("action", "add");
                    args.put("statusbook", "BOOK HOME");
                    args.put("nopol", nson.get("nopol").asString());
                    args.put("jeniskendaraan", nson.get("jeniskendaraan").asString());
                    args.put("nopon", nson.get("nopon").asString());
                    args.put("nama", nson.get("nama").asString());
                    args.put("pemilik", nson.get("pemilik").asString());
                    args.put("kondisi", nson.get("kondisi").asString());
                    args.put("keluhan", nson.get("keluhan").asString());
                    args.put("layanan", find(R.id.et_biayaLayanan_booking2, EditText.class).getText().toString());
                    args.put("km", nson.get("km").asString());
                    args.put("longlat", nson.get("longlat").asString());
                    args.put("alamat", nson.get("alamat").asString());
                    args.put("date", currentDateTime());
                    args.put("tanggalbook", tglBooking.getText().toString());
                    args.put("jambook", find(R.id.et_menit_booking2, EditText.class).getText().toString());
                    args.put("waktupengerjaan", find(R.id.et_hari_booking2, EditText.class).getText().toString() + find(R.id.et_menit_booking2, EditText.class).getText().toString());
                    args.put("biayalayanan", find(R.id.et_biayaLayanan_booking2, EditText.class).getText().toString());
                    args.put("total", find(R.id.et_totalBiaya_booking2, EditText.class).getText().toString());
                    args.put("dp", find(R.id.et_dp_booking2, EditText.class).getText().toString());
                    args.put("sisa", find(R.id.et_sisa_booking2, EditText.class).getText().toString());
                }

                args.put("partbook", "" + nListArray);

                Log.d("Booking2List", "sendata : " + nListArray);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturbooking"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showInfo("Berhasil Menyimpan Aktivitas");
                    setResult(RESULT_OK);
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
            Nson nson = Nson.readJson(getIntentStringExtra(data, "booking3"));
            countClick++;
            if (countClick > 0) {
                find(R.id.btn_simpan_booking2, Button.class).setEnabled(true);
            }
            int total = nson.get("TOTAL").asInteger();
            nListArray.asArray().clear();

            nListArray.add(nson.get("partbook").toJson());
            String finalTotal = String.valueOf(total);
            //find(R.id.et_biayaLayanan_booking2, EditText.class).setText("Rp. " + formatter.format(Double.parseDouble(finalTotal)));
            find(R.id.et_totalBiaya_booking2, EditText.class).setText("Rp. " + formatter.format(Double.parseDouble(finalTotal)));
            Log.d("Booking2List", "NlistArray : " + nListArray.toJson());
        }
    }
}
