package com.rkrzmail.oto.modules.primary.checkin;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.BarcodeActivity;
import com.rkrzmail.oto.modules.primary.HistoryBookingCheckin_Activity;
import com.rkrzmail.srv.NikitaAutoComplete;
import com.rkrzmail.srv.NsonAutoCompleteAdapter;

import java.util.ArrayList;
import java.util.Map;

public class Checkin1_Activity extends AppActivity implements View.OnClickListener {

    private static final int REQUEST_BARCODE = 11;
    private NikitaAutoComplete etJenisKendaraan, etNopol, etNoPonsel;
    private EditText etNamaPelanggan, etKeluhan, etKm;
    private Spinner spPekerjaan;
    private ImageView imgBarcode;
    private static final int REQUEST_CHECKIN = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkin1_);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Check-In");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        etNopol = findViewById(R.id.et_nopol_checkin1);
        etJenisKendaraan = findViewById(R.id.et_jenisKendaraan_checkin1);
        etNoPonsel = findViewById(R.id.et_noPonsel_checkin1);
        etNamaPelanggan = findViewById(R.id.et_namaPelanggan_checkin1);
        etKeluhan = findViewById(R.id.et_keluhan_checkin1);
        etKm = findViewById(R.id.et_km_checkin1);
        spPekerjaan = findViewById(R.id.sp_pekerjaan_checkin1);
        imgBarcode = findViewById(R.id.imgBarcode_checkin1);

        imgBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getActivity(), BarcodeActivity.class), REQUEST_BARCODE);
            }
        });

        setSpinnerFromApi(spPekerjaan, "nama", "PEKERJAAN", "viewmst", "PEKERJAAN");

        componentValidation();

        find(R.id.btn_lanjut_checkin1).setOnClickListener(this);
        find(R.id.btn_history_checkin1).setOnClickListener(this);

    }

    private void setSelanjutnya() {

        final String nopol = etNopol.getText().toString().replace(" ", "").toUpperCase();
        final String nophone = etNoPonsel.getText().toString();
        final String namaPelanggan = etNamaPelanggan.getText().toString().toUpperCase();
        final String jenisKendaraan = etJenisKendaraan.getText().toString().toUpperCase();
        final String km = etKm.getText().toString();
        final String keluhan = etKeluhan.getText().toString();
        final String pekerjaan = spPekerjaan.getSelectedItem().toString().toUpperCase();

        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("nopol", nopol);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("viewnopol"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    Log.d("booking", result.get("data").asString());
                } else {
                    Log.d("booking", result.get("message").asString());
                }

                Nson nson = Nson.newObject();
                nson.set("nopol", nopol);
                nson.set("jeniskendaraan", jenisKendaraan);
                nson.set("nopon", nophone);
                nson.set("nama", namaPelanggan);
                nson.set("pemilik", find(R.id.cb_pemilik_checkin1, CheckBox.class).isChecked() ? "YA" : "TIDAK");
                nson.set("keluhan", keluhan);
                nson.set("km", km);
                nson.set("pekerjaan", pekerjaan);

                ArrayList<String> data = new ArrayList<>();
                for (int i = 0; i < result.get("data").size(); i++) {
                    data.add(result.get("data").get(i).get("NOPOL").asString());
                }

                if (keluhan.isEmpty()) {
                    showInfo("Silahkan Isi Keluhan");
                    return;
                }
                if (nopol.isEmpty()) {
                    showInfo("Silahkan Isi No. Polisi");
                    return;
                }
                if (jenisKendaraan.isEmpty()) {
                    showInfo("Silahkan Isi Jenis Kendaraan");
                    return;
                }
                if (nophone.isEmpty()) {
                    showInfo("Silahkan Isi No. Hp");
                    return;
                }
                if (namaPelanggan.isEmpty()) {
                    showInfo("Silahkan Isi Nama");
                    return;
                }
                if (pekerjaan.equalsIgnoreCase("BELUM DI PILIH")) {
                    showInfo("Silahkan Pilih Pekerjaan");
                    return;
                }

                Intent intent;
                if (data.contains(nopol)) {
                    intent = new Intent(getActivity(), Checkin3_Activity.class);
                    intent.putExtra("data", nson.toJson());
                    startActivity(intent);
                    finish();
                } else {
                    intent = new Intent(getActivity(), Checkin2_Activity.class);
                    intent.putExtra("data", nson.toJson());
                    setResult(RESULT_OK);
                    startActivityForResult(intent, REQUEST_CHECKIN);
                }
            }
        });
    }

    private void componentValidation() {
        etNopol.setThreshold(3);
        etNopol.setAdapter(new NsonAutoCompleteAdapter(getActivity()) {
            @Override
            public Nson onFindNson(Context context, String bookTitle) {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                String nopol = bookTitle.replace(" ", "").toUpperCase();
                args.put("nopol", nopol);
                Nson result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("viewnopol"), args));
                return result.get("data");
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.find_nopol, parent, false);
                }
                findView(convertView, R.id.txtNopol, TextView.class).setText(formatNopol(getItem(position).get("NOPOL").asString()));
                return convertView;
            }
        });

        etNopol.setLoadingIndicator((android.widget.ProgressBar) findViewById(R.id.pb_nopol_checkin));
        etNopol.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Nson n = Nson.readJson(String.valueOf(adapterView.getItemAtPosition(position)));
                etNopol.setText(formatNopol(n.get("NOPOL").asString()));
                etNoPonsel.setText(n.get("PHONE").asString());
                etNamaPelanggan.setText(n.get("NAMA").asString());
                etJenisKendaraan.setText(n.get("MODEL").asString());

                find(R.id.btn_history_checkin1).setEnabled(true);

            }
        });

        etJenisKendaraan.setThreshold(3);
        etJenisKendaraan.setAdapter(new NsonAutoCompleteAdapter(getActivity()) {
            @Override
            public Nson onFindNson(Context context, String bookTitle) {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("search", bookTitle);
                Nson result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("jeniskendaraan"), args));
                return result.get("data");
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.find_jenisken, parent, false);
                }

                findView(convertView, R.id.txtMerk, TextView.class).setText((getItem(position).get("VARIAN").asString()));
                findView(convertView, R.id.txtModel, TextView.class).setText((getItem(position).get("MODEL").asString()));
                findView(convertView, R.id.txtJenisVarian, TextView.class).setText((getItem(position).get("JENIS").asString()));

                return convertView;
            }
        });

        etJenisKendaraan.setLoadingIndicator((android.widget.ProgressBar) findViewById(R.id.pb_kendaraan_checkin));
        etJenisKendaraan.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Nson n = Nson.readJson(String.valueOf(adapterView.getItemAtPosition(position)));

                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(n.get("JENIS").asString()).append(" ");
                stringBuilder.append(n.get("MODEL").asString()).append(" ");
                stringBuilder.append(n.get("VARIAN").asString()).append(" ");

                etJenisKendaraan.setText(stringBuilder.toString());
                etJenisKendaraan.setTag(String.valueOf(adapterView.getItemAtPosition(position)));
            }
        });

        etNoPonsel.setThreshold(7);
        etNoPonsel.setAdapter(new NsonAutoCompleteAdapter(getActivity()) {
            @Override
            public Nson onFindNson(Context context, String bookTitle) {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                String phone = bookTitle.replace(" ", "").toUpperCase();
                args.put("nomorponsel", phone);
                Nson result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("nomorponsel"), args));

                return result.get("nomorponsel");
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.find_nophone, parent, false);
                }
                findView(convertView, R.id.txtPhone, TextView.class).setText(formatNopol(getItem(position).get("NO_PONSEL").asString()));

                return convertView;
            }
        });

        etNoPonsel.setLoadingIndicator((android.widget.ProgressBar) findViewById(R.id.pb_etNoPonsel_checkin));
        etNoPonsel.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Nson n = Nson.readJson(String.valueOf(adapterView.getItemAtPosition(position)));
                etNoPonsel.setText(formatNopol(n.get("NO_PONSEL").asString()));
                etNamaPelanggan.setText(n.get("NAMA_PELANGGAN").asString());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CHECKIN) {
            Intent i = new Intent(getActivity(), Checkin3_Activity.class);
            i.putExtra("data", getIntentStringExtra(data, "data"));
            startActivity(i);
            finish();
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_BARCODE) {

        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_lanjut_checkin1:
                setSelanjutnya();
                break;
            case R.id.btn_history_checkin1:
                startActivity(new Intent(getActivity(), HistoryBookingCheckin_Activity.class));
                break;
        }
    }
}
