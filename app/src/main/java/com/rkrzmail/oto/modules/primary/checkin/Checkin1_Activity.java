package com.rkrzmail.oto.modules.primary.checkin;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.BarcodeActivity;
import com.rkrzmail.oto.modules.primary.HistoryBookingCheckin_Activity;
import com.rkrzmail.oto.modules.primary.KontrolLayanan_Activity;
import com.rkrzmail.srv.NikitaAutoComplete;
import com.rkrzmail.srv.NsonAutoCompleteAdapter;

import java.util.ArrayList;
import java.util.Map;

public class Checkin1_Activity extends AppActivity implements View.OnClickListener {

    private static final int REQUEST_BARCODE = 11;
    private static final int REQUEST_HISTORY = 12;
    private static final int REQUEST_NEW_CS = 13;
    private NikitaAutoComplete etJenisKendaraan, etNopol, etNoPonsel;
    private EditText etNamaPelanggan, etKeluhan, etKm;
    private Spinner spPekerjaan;
    private ImageView imgBarcode;

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
                IntentIntegrator intentIntegrator = new IntentIntegrator(getActivity());
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
                Nson nson = Nson.newObject();
                nson.set("NOPOL", nopol);
                nson.set("JENIS_KENDARAAN", jenisKendaraan);
                nson.set("NO_PONSEL", nophone);
                nson.set("NAMA_PELANGGAN", namaPelanggan);
                nson.set("PEMILIK", find(R.id.cb_pemilik_checkin1, CheckBox.class).isChecked() ? "YA" : "TIDAK");
                nson.set("KELUHAN", keluhan);
                nson.set("KM", km);
                nson.set("PEKERJAAN", pekerjaan);

                ArrayList<String> data = new ArrayList<>();
                for (int i = 0; i < result.get("data").size(); i++) {
                    data.add(result.get("data").get(i).get("NOPOL").asString());
                }

                Intent intent;
                if (data.contains(nopol)) {
                    intent = new Intent(getActivity(), Checkin3_Activity.class);
                    intent.putExtra("data", nson.toJson());
                    startActivityForResult(intent, KontrolLayanan_Activity.REQUEST_CHECKIN);
                } else {
                    intent = new Intent(getActivity(), Checkin2_Activity.class);
                    intent.putExtra("data", nson.toJson());
                    startActivityForResult(intent, REQUEST_NEW_CS);
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
                etNoPonsel.setText(n.get("NO_PONSEL").asString());
                etNamaPelanggan.setText(n.get("NAMA_PELANGGAN").asString());
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

        etNoPonsel.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.toString().length() == 0) {
                    find(R.id.tl_nohp, TextInputLayout.class).setErrorEnabled(false);
                }

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() < 6) {
                    find(R.id.tl_nohp, TextInputLayout.class).setError("No. Hp Min. 6 Karakter");
                } else {
                    find(R.id.tl_nohp, TextInputLayout.class).setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() == 0) {
                    find(R.id.tl_nohp, TextInputLayout.class).setErrorEnabled(false);
                } else if (!s.toString().contains("+62 ")) {
                    etNoPonsel.setText("+62 ");
                    Selection.setSelection(etNoPonsel.getText(), etNoPonsel.getText().length());
                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == KontrolLayanan_Activity.REQUEST_CHECKIN) {
            finish();
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_BARCODE) {

        } else if (resultCode == RESULT_OK && requestCode == REQUEST_HISTORY) {

        } else if (resultCode == RESULT_OK && requestCode == REQUEST_NEW_CS) {
            Intent i = new Intent(getActivity(), Checkin3_Activity.class);
            i.putExtra("data", Nson.readJson(getIntentStringExtra(data, "data")).toJson());
            startActivityForResult(i, KontrolLayanan_Activity.REQUEST_CHECKIN);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_lanjut_checkin1:
                if (etNopol.getText().toString().isEmpty()) {
                    etNopol.setError("Harus Di Isi");
                } else if (etJenisKendaraan.getText().toString().isEmpty()) {
                    etJenisKendaraan.setError("Harus Di Isi");
                } else if (etNoPonsel.getText().toString().isEmpty() && etNoPonsel.getText().toString().length() < 6) {
                    etNoPonsel.setError("Harus Di Isi");
                } else if (etNamaPelanggan.getText().toString().isEmpty() && etNamaPelanggan.getText().toString().length() < 5) {
                    etNamaPelanggan.setError("Harus Di Isi");
                } else if (spPekerjaan.getSelectedItem().toString().equalsIgnoreCase("Belum Di Pilih")) {
                    showWarning("Silahkan Pilih Pekerjaan");
                } else if (etKm.getText().toString().isEmpty()) {
                    etKm.setError("Harus Di Isi");
                } else {
                    setSelanjutnya();
                }
                break;
            case R.id.btn_history_checkin1:
                startActivityForResult(new Intent(getActivity(), HistoryBookingCheckin_Activity.class), REQUEST_HISTORY);
                break;
        }
    }
}
