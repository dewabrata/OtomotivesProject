package com.rkrzmail.oto.modules.primary.checkin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import com.rkrzmail.oto.modules.user.AturUser_Activity;
import com.rkrzmail.srv.NikitaAutoComplete;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.srv.NsonAutoCompleteAdapter;

import java.util.ArrayList;
import java.util.Map;

public class Checkin1_Activity extends AppActivity implements View.OnClickListener {

    private static final int REQUEST_BARCODE = 11;
    private static final int REQUEST_HISTORY = 12;
    private static final int REQUEST_NEW_CS = 13;
    private static final String TAG = "CHECKIN1___";
    private NikitaAutoComplete etJenisKendaraan, etNopol, etNoPonsel, etNamaPelanggan;
    private EditText etKeluhan, etKm;
    private Spinner spPekerjaan;
    private String noHp = "",
            tahunProduksi = "",
            varianKendaraan = "",
            batasanKm = "",
            batasanBulan = "",
            pekerjaan = "",
            merkKendaraan = "",
            jenisKendaraan = "",
            modelKendaraan = "",
            rangka = "",
            mesin = "", lokasi = "";
    private Nson nopolList = Nson.newArray(), keluhanList = Nson.newArray();
    private boolean keyDel = false, isNoHp = false, isNamaValid = false, isRemoved;
    private RecyclerView rvKeluhan;

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
        initLokasiPenugasan();
        etNopol = findViewById(R.id.et_nopol_checkin1);
        etJenisKendaraan = findViewById(R.id.et_jenisKendaraan_checkin1);
        etNoPonsel = findViewById(R.id.et_noPonsel_checkin1);
        etNamaPelanggan = findViewById(R.id.et_namaPelanggan_checkin1);
        etKeluhan = findViewById(R.id.et_keluhan_checkin1);
        etKm = findViewById(R.id.et_km_checkin1);
        spPekerjaan = findViewById(R.id.sp_pekerjaan_checkin1);
        rvKeluhan = findViewById(R.id.recyclerView_keluhan);

        find(R.id.imgBarcode_checkin1, ImageButton.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Intent i = new Intent(getActivity(), BarcodeActivity.class);
               startActivityForResult(i, REQUEST_BARCODE);
            }
        });

        getHistoryNopol();
        setSpinnerFromApi(spPekerjaan, "nama", "PEKERJAAN", "viewmst", "PEKERJAAN");
        initAutoCompleteNopol();
        initAutoCompleteNamaPelanggan();
        initAutoCompleteKendaraan();
        initOnTextChange();

        find(R.id.btn_lanjut_checkin1).setOnClickListener(this);
        find(R.id.btn_history_checkin1).setOnClickListener(this);
        find(R.id.fab_tambah_keluhan, ImageButton.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etKeluhan.getText().toString().isEmpty() || etKeluhan.getText().toString().length() < 5){
                    etKeluhan.setError("Keluhan Min 5 Karakter");
                }else{
                    rvKeluhan.setVisibility(View.VISIBLE);
                    isRemoved = true;
                    keluhanList.add(Nson.newObject().set("KELUHAN", etKeluhan.getText().toString()));
                    initRecylerViewKeluhan();
                    rvKeluhan.getAdapter().notifyDataSetChanged();
                    etKeluhan.setText("");
                }
            }
        });
        find(R.id.img_clear, ImageButton.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etNamaPelanggan.setText("");
                etNoPonsel.setText("");
            }
        });
    }

    private void initLokasiPenugasan() {
        if (getSetting("LOKASI_PENUGASAN") == null || getSetting("LOKASI_PENUGASAN").equals("")) {
            Messagebox.showDialog(getActivity(), "Penugasan", "Tentukan Lokasi Penugasan", "TENDA", "BENGKEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    setSetting("LOKASI_PENUGASAN", "TENDA");
                    showSuccess("Lokasi Penugasan TENDA");
                }
            }, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    setSetting("LOKASI_PENUGASAN", "BENGKEL");
                    showSuccess("Lokasi Penugasan BENGKEL");
                }
            });
        }
    }

    private void getHistoryNopol() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("nopol", etNopol.getText().toString().replace(" ", "").toUpperCase());
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("viewnopol"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data");
                    for (int i = 0; i < result.size(); i++) {
                        nopolList.add(result.get(i).get("NOPOL").asString());
                    }
                } else {
                    showInfo("Gagal memuat Data History");
                }
            }
        });
    }

    private void initOnTextChange() {
        watcherNamaPelanggan(find(R.id.img_clear, ImageButton.class), etNamaPelanggan);
        etNopol.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }


            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @SuppressLint("SetTextI18n")
            @Override
            public void afterTextChanged(Editable s) {
//                etNopol.removeTextChangedListener(this);
//                int counting = (s == null) ? 0 : s.toString().length();
//                assert s != null;
//                String text = s.toString();
//                if(!keyDel){
//                    if (counting == 1 || counting == 6) {
//                       text = text + " ";
//                    }
//                }
//                etNopol.setText(text);
//                etNopol.setSelection(etNopol.getText().length());
//
//                etNopol.addTextChangedListener(this);
            }
        });

        etNopol.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    event.getAction();
                    keyDel = true;
                } else {
                    keyDel = false;
                }
                return false;
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
                int counting = (s == null) ? 0 : s.toString().length();
                if (counting < 4) {
                    etNoPonsel.setText("+62 ");
                    Selection.setSelection(etNoPonsel.getText(), etNoPonsel.getText().length());
                } else if (counting < 12) {
                    find(R.id.tl_nohp, TextInputLayout.class).setError("No. Hp Min. 6 Karakter");
                    etNoPonsel.requestFocus();
                } else {
                    find(R.id.tl_nohp, TextInputLayout.class).setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void initAutoCompleteNamaPelanggan() {
        etNamaPelanggan.setThreshold(0);
        etNamaPelanggan.setAdapter(new NsonAutoCompleteAdapter(getActivity()) {
            @Override
            public Nson onFindNson(Context context, String bookTitle) {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "Pelanggan");
                args.put("nama", bookTitle);
                Nson result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("datapelanggan"), args));
                return result.get("data");
            }

            @SuppressLint("SetTextI18n")
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.find_nophone, parent, false);
                }
                String nama = getItem(position).get("NAMA_PELANGGAN").asString();
                String nomor = "";
                noHp = getItem(position).get("NO_PONSEL").asString();
                if (noHp.length() > 4) {
                    nomor += noHp.substring(noHp.length() - 4);
                }

                findView(convertView, R.id.txtPhone, TextView.class).setText(nama + " " + nomor);
                return convertView;
            }
        });

        etNamaPelanggan.setLoadingIndicator((android.widget.ProgressBar) findViewById(R.id.pb_namaPelanggan));
        etNamaPelanggan.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Nson n = Nson.readJson(String.valueOf(adapterView.getItemAtPosition(position)));
                isNoHp = true;
                String nomor = "";
                noHp = n.get("NO_PONSEL").asString();
                if (noHp.length() > 4) {
                    nomor += noHp.substring(noHp.length() - 4);
                }
                etNoPonsel.setText("XXXXXXXX" + nomor);
                find(R.id.tl_nohp, TextInputLayout.class).setErrorEnabled(false);
                etNamaPelanggan.setText(n.get("NAMA_PELANGGAN").asString());
                pekerjaan = n.get("PEKERJAAN").asString();
                setSpinnerFromApi(spPekerjaan, "nama", "PEKERJAAN", "viewmst", "PEKERJAAN", pekerjaan);
                if (n.get("PEMILIK").asString().equalsIgnoreCase("Y")) {
                    find(R.id.cb_pemilik_checkin1, CheckBox.class).setChecked(true);
                }
                find(R.id.tl_nohp, TextInputLayout.class).setErrorEnabled(false);
                find(R.id.img_clear, ImageButton.class).setVisibility(View.GONE);
                Log.d(TAG, "onItemClick: " + noHp);
            }
        });
    }

    private void initAutoCompleteNopol() {
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
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Nson n = Nson.readJson(String.valueOf(adapterView.getItemAtPosition(position)));

                merkKendaraan = n.get("MERK").asString();
                jenisKendaraan = n.get("TYPE").asString();
                varianKendaraan = n.get("VARIAN").asString();
                modelKendaraan = n.get("MODEL").asString();
                noHp = n.get("NO_PONSEL").asString();

                String nomor = "";
                if (noHp.length() > 4) {
                    nomor += noHp.substring(noHp.length() - 4);
                }

                etNopol.setText(n.get("NOPOL").asString());
                etNoPonsel.setText("XXXXXXXX" + nomor);
                etNamaPelanggan.setText(n.get("NAMA_PELANGGAN").asString());
                etJenisKendaraan.setText(n.get("JENIS_KENDARAAN").asString());
                pekerjaan = n.get("PEKERJAAN").asString();
                setSpinnerFromApi(spPekerjaan, "nama", "PEKERJAAN", "viewmst", "PEKERJAAN", pekerjaan);
                if (n.get("PEMILIK").asString().equalsIgnoreCase("Y")) {
                    find(R.id.cb_pemilik_checkin1, CheckBox.class).setChecked(true);
                }

                find(R.id.tl_nohp, TextInputLayout.class).setErrorEnabled(false);
                find(R.id.btn_history_checkin1).setEnabled(true);
                find(R.id.img_clear, ImageButton.class).setVisibility(View.GONE);
            }
        });
    }

    private void initAutoCompleteKendaraan() {
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

        etJenisKendaraan.setLoadingIndicator((android.widget.ProgressBar) findViewById(R.id.pb_kendaraan_checkin));
        etJenisKendaraan.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Nson n = Nson.readJson(String.valueOf(adapterView.getItemAtPosition(position)));
                String jenisKendaraan = getSetting("JENIS_KENDARAAN");
                if (!jenisKendaraan.equalsIgnoreCase(n.get("TYPE").asString())) {
                    showWarning("Bengkel Hanya Melayani Kendaraan " + jenisKendaraan, Toast.LENGTH_LONG);
                    etJenisKendaraan.setText("");
                    etJenisKendaraan.requestFocus();
                    return;
                }
                StringBuilder stringBuilder = new StringBuilder();
                //stringBuilder.append(n.get("MODEL").asString()).append(" ");
                stringBuilder.append(n.get("MERK").asString()).append(" ");
                stringBuilder.append(n.get("JENIS").asString()).append(" ");
                stringBuilder.append(n.get("VARIAN").asString()).append(" ");

                merkKendaraan = n.get("MERK").asString();
                varianKendaraan = n.get("VARIAN").asString();
                modelKendaraan = n.get("MODEL").asString();
                tahunProduksi = n.get("TAHUN2").asString();
                jenisKendaraan = n.get("TYPE").asString();

                etJenisKendaraan.setText(stringBuilder.toString());
                etJenisKendaraan.setTag(String.valueOf(adapterView.getItemAtPosition(position)));
            }
        });
    }

    private void setSelanjutnya() {
        final String nopol = etNopol.getText().toString().replace(" ", "").toUpperCase();
        final String namaPelanggan = etNamaPelanggan.getText().toString().toUpperCase();
        final String jenisKendaraan = etJenisKendaraan.getText().toString().toUpperCase();
        final String km = etKm.getText().toString();
        final String pekerjaan = spPekerjaan.getSelectedItem().toString().toUpperCase();
        final String pemilik = find(R.id.cb_pemilik_checkin1, CheckBox.class).isChecked() ? "Y" : "N";
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "add");
                args.put("regris", "1");
                args.put("nopol", nopol);
                args.put("jeniskendaraan", jenisKendaraan);
                args.put("nopon", isNoHp ? noHp : etNoPonsel.getText().toString().replaceAll("[^0-9]+", ""));
                args.put("nama", namaPelanggan);
                args.put("pemilik", pemilik);
                args.put("keluhan_list", keluhanList.toJson());
                args.put("km", km);
                args.put("pekerjaan", pekerjaan);
                args.put("kendaraan", getSetting("JENIS_KENDARAAN"));
                args.put("model", modelKendaraan);
                args.put("merk", merkKendaraan);
                args.put("varian", varianKendaraan);
                args.put("rangka", "");
                args.put("mesin", "");

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("checkin"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data").get(0);
                    Nson nson = Nson.newObject();

                    nson.set("id", result.get("ID").asString());
                    nson.set("jeniskendaraan", jenisKendaraan);
                    nson.set("model", modelKendaraan);
                    nson.set("merk", merkKendaraan);
                    nson.set("varian", varianKendaraan);
                    nson.set("tahunProduksi", tahunProduksi);
                    nson.set("km", etKm.getText().toString());
                    nson.set("kendaraan", getSetting("JENIS_KENDARAAN"));

                    Intent intent;
                    if (nopolList.asArray().contains(nopol)) {
                        intent = new Intent(getActivity(), Checkin3_Activity.class);
                        intent.putExtra("data", nson.toJson());
                        startActivityForResult(intent, KontrolLayanan_Activity.REQUEST_CHECKIN);
                    } else {
                        intent = new Intent(getActivity(), Checkin2_Activity.class);
                        intent.putExtra("data", nson.toJson());
                        startActivityForResult(intent, REQUEST_NEW_CS);
                    }
                } else {
                    showWarning(result.get("status").asString());
                }
            }
        });
    }

    private void getDataBarcode(final String antrian){
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "barcode");
                args.put("antrian", antrian);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("checkin"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {

                } else {
                    showWarning(result.get("status").asString());
                }
            }
        });
    }

    private void initRecylerViewKeluhan() {
        rvKeluhan.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvKeluhan.setHasFixedSize(true);
        rvKeluhan.setAdapter(new NikitaRecyclerAdapter(keluhanList, R.layout.item_keluhan) {
            @Override
            public void onBindViewHolder(@NonNull final NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                viewHolder.find(R.id.tv_keluhan, TextView.class).setText(keluhanList.get(position).get("KELUHAN").asString());
                viewHolder.find(R.id.img_delete, ImageButton.class).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isRemoved = false;
                        keluhanList.asArray().remove(position);
                        notifyItemRemoved(position);
                        rvKeluhan.getAdapter().notifyDataSetChanged();
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == KontrolLayanan_Activity.REQUEST_CHECKIN) {
            finish();
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_BARCODE) {
            String antrian = data.getStringExtra("TEXT");
            getDataBarcode(antrian);
            String result = data.getStringExtra("FORMAT");
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
                    etNopol.requestFocus();
                } else if (etJenisKendaraan.getText().toString().isEmpty()) {
                    etJenisKendaraan.setError("Harus Di Isi");
                    etJenisKendaraan.requestFocus();
                } else if (etNoPonsel.getText().toString().isEmpty() || etNoPonsel.getText().toString().length() < 6) {
                    etNoPonsel.setError("Harus Di Isi");
                    etNoPonsel.requestFocus();
                } else if (etNamaPelanggan.getText().toString().isEmpty() ||  etNamaPelanggan.getText().toString().length() < 5) {
                    etNamaPelanggan.setError("Harus Di Isi");
                    etNamaPelanggan.requestFocus();
                } else if (spPekerjaan.getSelectedItem().toString().equalsIgnoreCase("Belum Di Pilih")) {
                    showWarning("Silahkan Pilih Pekerjaan");
                    spPekerjaan.performClick();
                } else if (etKm.getText().toString().isEmpty()) {
                    etKm.setError("Harus Di Isi");
                    etKm.requestFocus();
                } else if (spPekerjaan.getSelectedItem().toString().equalsIgnoreCase("--PILIH--")) {
                    spPekerjaan.performClick();
                    showWarning("Pekerjaan Harus Di Pilih");
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
