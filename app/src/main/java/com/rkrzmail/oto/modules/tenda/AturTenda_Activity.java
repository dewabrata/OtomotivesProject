package com.rkrzmail.oto.modules.tenda;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.MultiSelectionSpinner;
import com.rkrzmail.utils.Tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class AturTenda_Activity extends AppActivity implements View.OnClickListener{

    private static final String TAG = "AturTenda_Activity";
    private EditText etLokasi, etAlamat, etLonglat;
    private String[] hari;
    private TextView tvTglBuka, tvTglSelesai, tvJamMulai, tvJamTutup;
    private List<String> dummies = new ArrayList<>();
    private MultiSelectionSpinner spHari;
    private Spinner spTipeWaktu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_tenda_);
        initToolbar();
        initComponent();

    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_atur_tenda);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Tenda");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.animate().translationY(-toolbar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
    }

    private void initComponent() {

        tvTglBuka = findViewById(R.id.tv_tglMulai_tenda);
        tvTglSelesai = findViewById(R.id.tv_tglSelesai_tenda);
        etLokasi = findViewById(R.id.et_namaLokasi_tenda);
        etAlamat = findViewById(R.id.et_alamat_tenda);
        etLonglat = findViewById(R.id.et_longlat_tenda);
        tvJamMulai = findViewById(R.id.tv_jamMulai_tenda);
        tvJamTutup = findViewById(R.id.tv_jamSelesai_tenda);
        spHari = findViewById(R.id.sp_hari_tenda);
        spTipeWaktu = findViewById(R.id.sp_tipeWaktu_tenda);


        hari = getResources().getStringArray(R.array.hari_tenda);
        spHari.setItems(hari);
        spHari.setListener(new MultiSelectionSpinner.OnMultipleItemsSelectedListener() {
            @Override
            public void selectedIndices(List<Integer> indices) {

            }

            @Override
            public void selectedStrings(List<String> strings) {
                dummies.addAll(strings);
            }
        });

        spTipeWaktu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                String item = parent.getItemAtPosition(position).toString();
                if (item.equalsIgnoreCase("TANGGAL")) {
                    find(R.id.ly_hari_tenda, LinearLayout.class).setVisibility(View.GONE);
                    find(R.id.ly_tanggal_tenda, LinearLayout.class).setVisibility(View.VISIBLE);
                } else if (item.equalsIgnoreCase("HARI")) {
                    find(R.id.ly_hari_tenda, LinearLayout.class).setVisibility(View.VISIBLE);
                    find(R.id.ly_tanggal_tenda, LinearLayout.class).setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        tvTglBuka.setOnClickListener(this);
        tvTglSelesai.setOnClickListener(this);
        tvJamMulai.setOnClickListener(this);
        tvJamTutup.setOnClickListener(this);

        find(R.id.btn_simpan_tenda, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    saveData();

            }
        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_tglMulai_tenda:
                Tools.getDatePickerDialogTextView(getActivity(), tvTglBuka);
                break;
            case R.id.tv_tglSelesai_tenda:
                Tools.getDatePickerDialogTextView(getActivity(), tvTglSelesai);
                break;
            case R.id.tv_jamMulai_tenda:
                Tools.getTimePickerDialogTextView(getActivity(), tvJamMulai);
                break;
            case R.id.tv_jamSelesai_tenda:
                Tools.getTimePickerDialogTextView(getActivity(), tvJamTutup);
                break;

        }
    }

    private void saveData() {

        StringBuilder strHari = new StringBuilder();
        for (String hari : dummies) {
            strHari.append(hari);
        }
        final String tipeHari = strHari.toString();
        final String tglMulai = tvTglBuka.getText().toString();
        final String tglSelesai = tvTglSelesai.getText().toString();
        final String lokasi = etLokasi.getText().toString();
        final String longlat = etLonglat.getText().toString();
        final String alamat = etAlamat.getText().toString();
        final String jamBuka = tvJamMulai.getText().toString();
        final String jamTutup = tvJamTutup.getText().toString();

        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "save");
                if (etLonglat.isEnabled() && etAlamat.isEnabled()) {
                    args.put("alamat", alamat);
                    args.put("lokasi", longlat);
                } else {
                    args.put("alamat", "");
                    args.put("lokasi", "");
                }
                if (find(R.id.ly_hari_tenda, LinearLayout.class).getVisibility() == View.VISIBLE) {
                    args.put("hari", tipeHari);
                } else {
                    args.put("hari", "");
                }
                if (find(R.id.ly_tanggal_tenda, LinearLayout.class).getVisibility() == View.VISIBLE) {
                    args.put("tanggal", tglMulai);
                } else {
                    args.put("tanggal", "");
                }
                //args.put("tipe", tipe);

                args.put("namalokasi", lokasi);
                args.put("buka", jamBuka);
                args.put("tutup", jamTutup);
                args.put("status", "");

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturtenda"), args));
            }

            @Override
            public void runUI() {
                try {
                    Date tMulai = new SimpleDateFormat("dd/MM").parse(tglMulai);
                    Date tSelesai = new SimpleDateFormat("dd/MM").parse(tglSelesai);
                    Date jBuka = new SimpleDateFormat("HH:mm").parse(jamBuka);
                    Date jTutup = new SimpleDateFormat("HH:mm").parse(jamTutup);

                    if (find(R.id.ly_hari_tenda, LinearLayout.class).getVisibility() == View.VISIBLE) {
                        if (!tMulai.before(tSelesai)) {
                            showInfo("Tanggal Tidak Sesuai");
                        }
                    }
                    if (jBuka.before(jTutup)) {
                        if (result.get("status").asString().equalsIgnoreCase("OK")) {
                            showInfo("Berhasil Menyimpan Aktifitas");
                            startActivity(new Intent(getActivity(), Tenda_Activity.class));
                            finish();

                        } else {
                            showError("Gagal Menyiman Aktifitas ");
                        }
                    } else {
                        showInfo("Jam Selesai Tidak Sesuai!");
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
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
