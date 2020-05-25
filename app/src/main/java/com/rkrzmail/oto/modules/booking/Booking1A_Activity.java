package com.rkrzmail.oto.modules.booking;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NikitaAutoComplete;
import com.rkrzmail.srv.NsonAutoCompleteAdapter;

import java.util.Map;

public class Booking1A_Activity extends AppActivity {

    private static final int REQUEST_PENDAFTARAN = 23;
    private NikitaAutoComplete etJenisKendaraan, etNopol, etNoPonsel;
    private EditText etNamaPelanggan, etKeluhan, etKm;
    private Spinner spKondisiKendaraan, spLayanan, spPekerjaan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking1_a_);
        initToolbar();
        initComponent();
    }


    private void initToolbar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_booking1);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Booking 1A");
        setTitleColor(getResources().getColor(R.color.white_transparency));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent(){
        etNopol = (NikitaAutoComplete) findViewById(R.id.et_nopol_booking1a);
        etJenisKendaraan = (NikitaAutoComplete) findViewById(R.id.et_jenisKendaraan_booking1a);
        etNoPonsel = (NikitaAutoComplete) findViewById(R.id.et_noPonsel_booking1a);
        etNamaPelanggan = findViewById(R.id.et_namaPelanggan_booking1a);
        etKeluhan = findViewById(R.id.et_keluhan_booking1a);
        etKm = findViewById(R.id.et_km_booking1a);
        spKondisiKendaraan = findViewById(R.id.sp_kondisi_booking1a);
        spLayanan = findViewById(R.id.sp_layanan_booking1a);
        spPekerjaan = findViewById(R.id.sp_pekerjaan_booking1a);

        componentValidation();

        find(R.id.btn_history_booking1a, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        find(R.id.btn_lanjut_booking1a, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextBooking();
            }
        });
    }

    private void nextBooking(){
        final String nopol = etNopol.getText().toString().toUpperCase();
        final String nophone = etNoPonsel.getText().toString();
        final String namaPelanggan = etNamaPelanggan.getText().toString().toUpperCase();
        final String jenisKendaraan = etJenisKendaraan.getText().toString().toUpperCase();
        final String kondisiKendaraan = spKondisiKendaraan.getSelectedItem().toString().toUpperCase();
        final String km = etKm.getText().toString();
        final String keluhan = etKeluhan.getText().toString();
        final String layanan = spLayanan.getSelectedItem().toString().toUpperCase();
        final String pekerjaan = spPekerjaan.getSelectedItem().toString().toUpperCase();


        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("booking1"), args));

            }

            @Override
            public void runUI() {
                Nson nson = Nson.newObject();
                nson.set("nopol", nopol);
                nson.set("jeniskendaraan", jenisKendaraan);
                nson.set("nopon", nophone);
                nson.set("nama", namaPelanggan);

                if (find(R.id.cb_pemilik_booking1a, CheckBox.class).isChecked()) {
                    String pemilik = find(R.id.cb_pemilik_booking1a, CheckBox.class).getText().toString();
                    nson.set("pemilik", pemilik);
                } else {
                    nson.set("pemilik", "");
                }
                nson.set("kondisi", kondisiKendaraan);
                nson.set("keluhan", keluhan);
                nson.set("km", km);
                nson.set("layanan", layanan);
                nson.set("pekerjaan", pekerjaan);

            }
        });

    }

    private void componentValidation(){
        etNopol.setThreshold(3);
        etNopol.setAdapter(new NsonAutoCompleteAdapter(getActivity()){
            @Override
            public Nson onFindNson(Context context, String bookTitle) {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                String nopol = bookTitle.replace(" ", "").toUpperCase();
                args.put("nopol", nopol);
                Nson result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturbooking"), args));
                return result;
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

        etNopol.setLoadingIndicator((android.widget.ProgressBar) findViewById(R.id.pb_etNopol_booking1a));
        etNopol.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Nson n = Nson.readJson(String.valueOf(adapterView.getItemAtPosition(position)));
                etNopol.setText(formatNopol(n.get("NOPOL").asString()));
                checkHistory(n.get("NOPOL").asString());
            }
        });


        etJenisKendaraan.setThreshold(3);
        etJenisKendaraan.setAdapter(new NsonAutoCompleteAdapter(getActivity()){
            @Override
            public Nson onFindNson(Context context, String bookTitle) {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("nama", bookTitle);
                Nson result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturbooking"), args));
                return result;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.find_jenisken, parent, false);
                }

                findView(convertView, R.id.txtMerk, TextView.class).setText((getItem(position).get("MERK").asString()));
                findView(convertView, R.id.txtModel, TextView.class).setText((getItem(position).get("MODEL").asString()));
                findView(convertView, R.id.txtJenisVarian, TextView.class).setText((getItem(position).get("JENIS").asString()) + " " + (getItem(position).get("VARIAN").asString()));

                return convertView;
            }
        });

        etJenisKendaraan.setLoadingIndicator((android.widget.ProgressBar) findViewById(R.id.pb_etJenisKendaraan_booking1a));
        etJenisKendaraan.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Nson n = Nson.readJson(String.valueOf(adapterView.getItemAtPosition(position)));
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(n.get("MERK").asString()).append(" ");
                stringBuilder.append(n.get("MODEL").asString()).append(" ");
                stringBuilder.append(n.get("JENIS").asString()).append(" ");
                stringBuilder.append(n.get("VARIAN").asString()).append(" ");

                etJenisKendaraan.setText(stringBuilder.toString());
                etJenisKendaraan.setTag(String.valueOf(adapterView.getItemAtPosition(position)));

                find(R.id.txtJenisVarian, TextView.class).setText(n.get("JENIS") + n.get("VARIAN").asString());
                find(R.id.txtModel, TextView.class).setText(n.get("MODEL").asString());
                find(R.id.txtMerk, TextView.class).setText(n.get("MERK").asString());


            }
        });


        etNoPonsel.setThreshold(7);
        etNoPonsel.setAdapter(new NsonAutoCompleteAdapter(getActivity()){
            @Override
            public Nson onFindNson(Context context, String bookTitle) {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                String phone = bookTitle.replace(" ", "").toUpperCase();
                args.put("phone", phone);
                Nson result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturbooking"), args));

                return result;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.find_nophone, parent, false);
                }
                findView(convertView, R.id.txtPhone, TextView.class).setText(formatNopol(getItem(position).get("PHONE").asString()));

                return convertView;
            }
        });

        etNoPonsel.setLoadingIndicator((android.widget.ProgressBar) findViewById(R.id.pb_etNoPonsel_booking1a));
        etNoPonsel.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Nson n = Nson.readJson(String.valueOf(adapterView.getItemAtPosition(position)));
                etNoPonsel.setText(formatNopol(n.get("PHONE").asString()));
                checkHistory(n.get("PHONE").asString());
            }
        });
    }

    private void checkHistory(final String nopol) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                String nopol = etNopol.getText().toString();
                nopol = nopol.toUpperCase().trim().replace(" ", "");
                args.put("nopol", nopol);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrl("checkhistory.php"), args));
            }

            public void runUI() {
                if (result.get("HISTORY").asString().equalsIgnoreCase("TRUE")) {
                    find(R.id.tblHistory).setEnabled(true);
                } else {
                    find(R.id.tblHistory).setEnabled(false);
                }
                if (result.get("STATUS").asString().equalsIgnoreCase("OK")) {
                    if (result.containsKey("PHONE")) {
                        find(R.id.txtPhone, EditText.class).setText(result.get("PHONE").asString());
                    }
                    if (result.containsKey("NAMA")) {
                        find(R.id.txtNamaPelanngan, EditText.class).setText(result.get("NAMA").asString());
                    }
                    if (result.containsKey("PEMILIK") && result.get("PEMILIK").asBoolean()) {
                        find(R.id.cckPemilik, CheckBox.class).setChecked(true);
                    }
                    if (result.containsKey("KM")) {
                        find(R.id.txtKMSebelum, EditText.class).setText(result.get("KM").asString());
                    }
                }
            }
        });
    }
}
