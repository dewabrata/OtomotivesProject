package com.rkrzmail.oto.modules.tenda;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class AturTenda_Activity extends AppActivity implements View.OnClickListener {

    private static final String TAG = "AturTenda_Activity";
    private static final int REQUEST_LOKASI = 18;
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
        Tools.setViewAndChildrenEnabled(find(R.id.tl_longlat, TextInputLayout.class), false);

        tvTglBuka = findViewById(R.id.tv_tglMulai_tenda);
        tvTglSelesai = findViewById(R.id.tv_tglSelesai_tenda);
        etLokasi = findViewById(R.id.et_namaLokasi_tenda);
        etAlamat = findViewById(R.id.et_alamat_tenda);
        etLonglat = findViewById(R.id.et_longlat_tenda);
        tvJamMulai = findViewById(R.id.tv_jamMulai_tenda);
        tvJamTutup = findViewById(R.id.tv_jamSelesai_tenda);
        spHari = findViewById(R.id.sp_hari_tenda);
        spTipeWaktu = findViewById(R.id.sp_tipeWaktu_tenda);

        minEntryEditText(etAlamat, 20, find(R.id.tl_alamat, TextInputLayout.class), "Entry Alamat Min. 20 Karakter");
        hari = getResources().getStringArray(R.array.hari_tenda);
        spHari.setItems(hari);
        spHari.setListener(new MultiSelectionSpinner.OnMultipleItemsSelectedListener() {
            @Override
            public void selectedIndices(List<Integer> indices) {

            }

            @Override
            public void selectedStrings(List<String> strings) {

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
                final String tglMulai = tvTglBuka.getText().toString();
                final String tglSelesai = tvTglSelesai.getText().toString();
                final String jamBuka = tvJamMulai.getText().toString();
                final String jamTutup = tvJamTutup.getText().toString();
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
                String tglSekarang = simpleDateFormat.format(calendar.getTime());

                if (etLokasi.getText().toString().isEmpty() && etLokasi.getText().toString().length() < 5) {
                    etLokasi.setError("Lengkapi Lokasi");
                    etLokasi.requestFocus();
                    return;
                } else if (etAlamat.getText().toString().isEmpty() && etAlamat.getText().toString().length() < 20) {
                    etAlamat.setError("Lengkap Alamat");
                    etAlamat.requestFocus();
                    return;
                } else if (etLonglat.isEnabled()) {
                    if (etLonglat.getText().toString().isEmpty()) {
                        etLonglat.setError("Lengkapi Longlat");
                        return;
                    }
                }
                if (jamBuka.equalsIgnoreCase("JAM MULAI")
                        && jamTutup.equalsIgnoreCase("JAM SELESAI")) {
                    showWarning("Silahkan Isi Jam Mulai / Jam Selesai");
                    return;
                } else {
                    try {
                        Date jBuka = new SimpleDateFormat("HH:mm").parse(jamBuka);
                        Date jTutup = new SimpleDateFormat("HH:mm").parse(jamTutup);
                        if (jTutup.before(jBuka)) {
                            showWarning("Jam Selesai Tidak Sesuai");
                            return;
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                        return;
                    }
                }
                if ((find(R.id.ly_tanggal_tenda, LinearLayout.class).getVisibility() == View.VISIBLE)) {
                    if (tglMulai.equalsIgnoreCase("TANGGAL MULAI")
                            || tglSelesai.equalsIgnoreCase("TANGGAL SELESAI")) {
                        showWarning("Silahkan Isi Tanggal Mulai / Tanggal Selesai");
                        return;
                    }
                    try {
                        Date tMulai = new SimpleDateFormat("dd/MM/yyyy").parse(tglMulai);
                        Date tNow = new SimpleDateFormat("dd/MM/yyyy").parse(tglSekarang);
                        if (tMulai.before(tNow)) {
                            showWarning("Tanggal Mulai Tidak Sesuai");
                            return;
                        }else{
                            showWarning("salah");
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    try {
                        Date tMulai = new SimpleDateFormat("dd/MM/yyyy").parse(tglMulai);
                        Date tSelesai = new SimpleDateFormat("dd/MM/yyyy").parse(tglSelesai);
                        if (!tMulai.before(tSelesai)) {
                            showWarning("Tanggal Selesai Tidak Sesuai");
                            return;
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                duplicateValidation();
            }
        });

        find(R.id.btn_peta_tenda, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_tglMulai_tenda:
                getDatePickerDialogTextView(getActivity(), tvTglBuka);
                break;
            case R.id.tv_tglSelesai_tenda:
                getDatePickerDialogTextView(getActivity(), tvTglSelesai);
                break;
            case R.id.tv_jamMulai_tenda:
                getTimePickerDialogTextView(getActivity(), tvJamMulai);
                break;
            case R.id.tv_jamSelesai_tenda:
                getTimePickerDialogTextView(getActivity(), tvJamTutup);
                break;

        }
    }

    private void saveData() {

        final String tipeHari = spHari.getSelectedItemsAsString();
        final String tglMulai = Tools.setFormatDayAndMonthToDb(tvTglBuka.getText().toString());
        final String tglSelesai = Tools.setFormatDayAndMonthToDb(tvTglSelesai.getText().toString());
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
                args.put("tipewaktu", spTipeWaktu.getSelectedItem().toString());
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
                    args.put("tanggalmulai", tglMulai);
                    args.put("tanggalselesai", tglSelesai);
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
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Berhasil Menyimpan Aktifitas");
                    startActivity(new Intent(getActivity(), Tenda_Activity.class));
                    finish();

                } else {
                    showError("Gagal Menyiman Aktifitas ");
                }
            }

        });
    }

    private void duplicateValidation() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturtenda"), args));
            }

            @Override
            public void runUI() {
                ArrayList<String> dummies = new ArrayList<>();
                for (int i = 0; i < result.get("data").size(); i++) {
                    dummies.add(result.get("data").get(i).get("NAMA_LOKASI").asString());
                    dummies.add(result.get("data").get(i).get("TANGGAL").asString());
                    dummies.add(result.get("data").get(i).get("LOCATION").asString());
                }
            }
        });
        if (dummies.contains(etLokasi.getText().toString())) {
            showWarning("Tenda Telah Terdaftar / Duplikasi");
        } else {
            saveData();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_LOKASI) {

        }
    }
}
