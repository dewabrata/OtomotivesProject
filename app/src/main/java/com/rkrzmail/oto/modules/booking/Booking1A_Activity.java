package com.rkrzmail.oto.modules.booking;

import android.content.Context;
import android.content.Intent;
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
import com.rkrzmail.oto.modules.booking.Booking1B_Activity;
import com.rkrzmail.srv.NikitaAutoComplete;
import com.rkrzmail.srv.NsonAutoCompleteAdapter;
import com.rkrzmail.utils.Tools;

import java.util.ArrayList;
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
            //Nson result;
            @Override
            public void run() {
                //Map<String, String> args = AppApplication.getInstance().getArgsData();
                //  result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("booking1"), args));

            }

            @Override
            public void runUI() {
                // add : CID, action(add), nopol, jeniskendaraan, nopon, nama,
                // pemilik, kondisi, keluhan, lokasi, jemput,
                // antar, layanan, km, alamat, date, user, status,
                // tanggalbook, jambook, jemput, antar, hari, jam, biayatrans, biayalayan, dp
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

                if (pekerjaan.equalsIgnoreCase("SERVICE KECIL")) {
                    if (!find(R.id.cb_pemilik_booking1a, CheckBox.class).isChecked()) {
                        Tools.alertDialog(getActivity(), "KM Wajib Di Isi!");
                    }
                } else if (pekerjaan.equalsIgnoreCase("SERVICE BESAR")) {

                }

                Intent i = new Intent(getActivity(), Booking1B_Activity.class);
                startActivity(i);

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

                //   args.put("action", "view");
                args.put("search", nopol);

                Nson result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturbooking"), args));
                //StringBuilder sb = new StringBuilder();
                for (int i = 0; i < result.get("data").size(); i++) {
                    //sb.append(result.get("data").get(i).get("NOPOL").asJson());
                    if (result.get("data").get(i).get("NOPOL").asArray().contains(nopol)) {
                        return result;
                    } else {
                        return result.get("data");
                    }
                }
                return result.get("search");
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
                etNoPonsel.setText(n.get("NO_PONSEL").asString());
                etNamaPelanggan.setText(n.get("NAMA_PELANGGAN").asString());
                etJenisKendaraan.setText(n.get("JENIS_KENDARAAN").asString());
                if (!etNopol.getText().toString().isEmpty()) {
                    find(R.id.btn_history_booking1a).setEnabled(true);
                }
            }
        });


        etJenisKendaraan.setThreshold(3);
        etJenisKendaraan.setAdapter(new NsonAutoCompleteAdapter(getActivity()){
            @Override
            public Nson onFindNson(Context context, String bookTitle) {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("search", bookTitle);
                Nson result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturbooking"), args));
                for (int i = 0; i < result.get("data").size(); i++) {
                    //sb.append(result.get("data").get(i).get("NOPOL").asJson());
                    if (result.get("data").get(i).get("JENIS_KENDARAAN").asArray().contains(bookTitle)) {
                        return result;
                    } else {
                        return result.get("data");
                    }
                }
                return result.get("search");
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.find_jenisken, parent, false);
                }

//                findView(convertView, R.id.txtMerk, TextView.class).setText((getItem(position).get("MERK").asString()));
//                findView(convertView, R.id.txtModel, TextView.class).setText((getItem(position).get("MODEL").asString()));
                findView(convertView, R.id.txtJenisVarian, TextView.class).setText((getItem(position).get("JENIS_KENDARAAN").asString()));

                return convertView;
            }
        });

        etJenisKendaraan.setLoadingIndicator((android.widget.ProgressBar) findViewById(R.id.pb_etJenisKendaraan_booking1a));
        etJenisKendaraan.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Nson n = Nson.readJson(String.valueOf(adapterView.getItemAtPosition(position)));

                StringBuilder stringBuilder = new StringBuilder();
//                stringBuilder.append(n.get("MERK").asString()).append(" ");
//                stringBuilder.append(n.get("MODEL").asString()).append(" ");
                stringBuilder.append(n.get("JENIS_KENDARAAN").asString()).append(" ");
                // stringBuilder.append(n.get("VARIAN").asString()).append(" ");

                etJenisKendaraan.setText(stringBuilder.toString());
                etJenisKendaraan.setTag(String.valueOf(adapterView.getItemAtPosition(position)));

//                find(R.id.txtJenisVarian, TextView.class).setText(n.get("JENIS_KENDARAAN").asString());
//                find(R.id.txtModel, TextView.class).setText(n.get("MODEL").asString());
//                find(R.id.txtMerk, TextView.class).setText(n.get("MERK").asString());


            }
        });


        etNoPonsel.setThreshold(7);
        etNoPonsel.setAdapter(new NsonAutoCompleteAdapter(getActivity()){
            @Override
            public Nson onFindNson(Context context, String bookTitle) {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                String phone = bookTitle.replace(" ", "").toUpperCase();
                args.put("search", phone);
                Nson result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturbooking"), args));
                for (int i = 0; i < result.get("data").size(); i++) {
                    //sb.append(result.get("data").get(i).get("NOPOL").asJson());
                    if (result.get("data").get(i).get("NO_PONSEL").asArray().contains(bookTitle)) {
                        return result;
                    } else {
                        return result.get("data");
                    }
                }
                return result.get("search");
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

        etNoPonsel.setLoadingIndicator((android.widget.ProgressBar) findViewById(R.id.pb_etNoPonsel_booking1a));
        etNoPonsel.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Nson n = Nson.readJson(String.valueOf(adapterView.getItemAtPosition(position)));
                etNoPonsel.setText(formatNopol(n.get("NO_PONSEL").asString()));
                etNamaPelanggan.setText(n.get("NAMA_PELANGGAN").asString());
            }
        });
    }

    private void checkHistory() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrl("aturbooking"), args));
            }

            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    if (result.containsKey("PHONE")) {
                        etNoPonsel.setText(result.get("data").get("PHONE").asString());
                    }
                    if (result.containsKey("NAMA")) {
                        etNamaPelanggan.setText(result.get("data").get("NAMA").asString());
                    }
                    if (result.containsKey("IS_PEMILIK") && result.get("IS_PEMILIK").asBoolean()) {
                        find(R.id.cb_pemilik_booking1a, CheckBox.class).setChecked(true);
                    }
                    if (result.containsKey("JENIS_KENDARAAN")) {
                        etJenisKendaraan.setText(result.get("data").get("JENIS_KENDARAAN").asString());
                    }
                }
            }
        });
    }
}
