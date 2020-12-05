package com.rkrzmail.oto.modules.checkin;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import java.util.ArrayList;
import java.util.Map;

public class Booking1A_Activity extends AppActivity {

    public static final int REQUEST_HISTORY = 10;
    public static final int REQUEST_BOOKING_TIGA = 11;
    private static final int REQUEST_CHECKIN = 12;
    private NikitaAutoComplete etJenisKendaraan, etNopol, etNoPonsel;
    private EditText etNamaPelanggan, etKeluhan, etKm;
    private Spinner spKondisiKendaraan, spLayanan, spPekerjaan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking1_a_);
        initComponent();
    }


    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_booking1);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Booking");
        setTitleColor(getResources().getColor(R.color.white_transparency));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        etNopol = (NikitaAutoComplete) findViewById(R.id.et_nopol_booking1a);
        etJenisKendaraan = (NikitaAutoComplete) findViewById(R.id.et_jenisKendaraan_booking1a);
        etNoPonsel = (NikitaAutoComplete) findViewById(R.id.et_noPonsel_booking1a);
        etNamaPelanggan = findViewById(R.id.et_namaPelanggan_booking1a);
        etKeluhan = findViewById(R.id.et_keluhan_booking1a);
        etKm = findViewById(R.id.et_km_booking1a);
        spKondisiKendaraan = findViewById(R.id.sp_kondisi_booking1a);
        spLayanan = findViewById(R.id.sp_layanan_booking1a);
        spPekerjaan = findViewById(R.id.sp_pekerjaan_booking1a);

        setSpinnerFromApi(spPekerjaan, "nama", "PEKERJAAN", "viewmst", "PEKERJAAN");
        autoCompleteEditText();

        find(R.id.btn_history_booking1a, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), HistoryBookingCheckin_Activity.class);
                i.putExtra("NOPOL", etNopol.getText().toString().trim());
                startActivityForResult(i, REQUEST_HISTORY);
            }
        });

        find(R.id.btn_lanjut_booking1a, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextBooking();
                showWarning("Silahkan Lengkapi Semua Field");
            }
        });
    }

    private void nextBooking() {
        final String nopol = etNopol.getText().toString().replace(" ", "").toUpperCase();
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
                args.put("nopol", nopol);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("viewnopol"), args));
            }

            @Override
            public void runUI() {
                ArrayList<String> data = new ArrayList<>();
                for (int i = 0; i < result.get("data").size(); i++) {
                    data.add(result.get("data").get(i).get("NOPOL").asString());
                }

                final Nson nson = Nson.newObject();
                nson.set("nopol", nopol);
                nson.set("jeniskendaraan", jenisKendaraan);
                nson.set("nopon", nophone);
                nson.set("nama", namaPelanggan);
                nson.set("pemilik", find(R.id.cb_pemilik_booking1a, CheckBox.class).isChecked() ? "YA" : "TIDAK");
                nson.set("kondisi", kondisiKendaraan);
                nson.set("keluhan", keluhan);
                nson.set("km", km);
                nson.set("layanan", layanan);
                nson.set("pekerjaan", pekerjaan);
                nson.set("derek", find(R.id.cb_derek_booking1a, CheckBox.class).isChecked() ? "YA" : "TIDAK");
                if (find(R.id.cb_derek_booking1a, CheckBox.class).isChecked()) {
                    nson.set("statusbook", "BOOK DEREK");
                } else {
                    nson.set("statusbook", "");
                }

                if (spKondisiKendaraan.getSelectedItem().toString().equalsIgnoreCase("MASALAH MESIN/PENGGERAK")) {
                    showInfoDialog("Konfirmasi", "Layanan hanya dapat di lakukan di Bengkel, Gunakan Antar - Jemput ? ", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //OK
                            nson.set("statusbook", "BOOK JEMPUT");
                            Intent i = new Intent(getActivity(), Booking1B_Activity.class);
                            i.putExtra("data", nson.toJson());
                            startActivityForResult(i, KontrolBooking_Activity.REQUEST_BOOKING_LAYANAN);
                        }
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //NO
                            nson.set("statusbook", "BOOK  BENGKEL");
                            Intent i = new Intent(getActivity(), Booking3_Activity.class);
                            i.putExtra("data", nson.toJson());
                            startActivityForResult(i, REQUEST_BOOKING_TIGA);
                        }
                    });
                    return;
                }

                Intent intent;
                if (data.contains(nopol)) {
                    intent = new Intent(getActivity(), Booking1B_Activity.class);
                    intent.putExtra("data", nson.toJson());
                    startActivityForResult(intent, KontrolBooking_Activity.REQUEST_BOOKING_LAYANAN);
                } else {
                    intent = new Intent(getActivity(), Checkin2_Activity.class);
                    intent.putExtra("data", nson.toJson());
                    startActivityForResult(intent, REQUEST_CHECKIN);
                }
            }
        });
    }

    private void autoCompleteEditText() {
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
                    convertView = inflater.inflate(R.layout.item_suggestion_single, parent, false);
                }
                findView(convertView, R.id.tv_text_suggesttion, TextView.class).setText(formatNopol(getItem(position).get("NOPOL").asString()));
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
                etJenisKendaraan.setText(n.get("MODEL").asString());

                find(R.id.btn_history_booking1a).setEnabled(true);

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

                findView(convertView, R.id.txtJenis, TextView.class).setText((getItem(position).get("JENIS").asString()));
                findView(convertView, R.id.txtVarian, TextView.class).setText((getItem(position).get("VARIAN").asString()));
                findView(convertView, R.id.txtMerk, TextView.class).setText((getItem(position).get("MERK").asString()));
                findView(convertView, R.id.txtModel, TextView.class).setText((getItem(position).get("MODEL").asString()));

                return convertView;
            }
        });

        etJenisKendaraan.setLoadingIndicator((android.widget.ProgressBar) findViewById(R.id.pb_etJenisKendaraan_booking1a));
        etJenisKendaraan.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Nson n = Nson.readJson(String.valueOf(adapterView.getItemAtPosition(position)));

                StringBuilder stringBuilder = new StringBuilder();

                stringBuilder.append(n.get("MODEL").asString()).append(" ");
                stringBuilder.append(n.get("MERK").asString()).append(" ");
                stringBuilder.append(n.get("JENIS").asString()).append(" ");
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CHECKIN) {
            Intent i = new Intent(getActivity(), Booking1B_Activity.class);
            i.putExtra("data", Nson.readJson(getIntentStringExtra(data, "data")).toJson());
            startActivityForResult(i, KontrolBooking_Activity.REQUEST_BOOKING_LAYANAN);
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_BOOKING_TIGA) {
            Intent i = new Intent(getActivity(), Booking2_Activity.class);
            i.putExtra("data", Nson.readJson(getIntentStringExtra(data, "data")).toJson());
            startActivityForResult(i, KontrolBooking_Activity.REQUEST_BOOKING_LAYANAN);
        } else if (resultCode == RESULT_OK && requestCode == KontrolBooking_Activity.REQUEST_BOOKING_LAYANAN) {
            setResult(RESULT_OK);
            finish();
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_HISTORY) {

        }
    }
}
