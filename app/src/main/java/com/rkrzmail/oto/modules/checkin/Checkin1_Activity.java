package com.rkrzmail.oto.modules.checkin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.BarcodeActivity;
import com.rkrzmail.srv.NikitaAutoComplete;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.srv.NsonAutoCompleteAdapter;
import com.rkrzmail.utils.Tools;

import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.SET_CHECKIN;
import static com.rkrzmail.utils.APIUrls.VIEW_JENIS_KENDARAAN;
import static com.rkrzmail.utils.APIUrls.VIEW_NOMOR_POLISI;
import static com.rkrzmail.utils.APIUrls.VIEW_PELANGGAN;
import static com.rkrzmail.utils.APIUrls.VIEW_SUGGESTION;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.ERROR_INFO;
import static com.rkrzmail.utils.ConstUtils.REQUEST_BARCODE;
import static com.rkrzmail.utils.ConstUtils.REQUEST_CHECKIN;
import static com.rkrzmail.utils.ConstUtils.REQUEST_HISTORY;
import static com.rkrzmail.utils.ConstUtils.REQUEST_NEW_CS;

public class Checkin1_Activity extends AppActivity implements View.OnClickListener {

    private static final String TAG = "CHECKIN1___";
    private NikitaAutoComplete etJenisKendaraan, etNopol, etNoPonsel, etNamaPelanggan, etKeluhan;
    private EditText etKm;
    private Spinner spPekerjaan;
    private String noHp = "",
            tahunProduksi = "",
            varianKendaraan = "",
            batasanKm = "",
            batasanBulan = "",
            pekerjaan = "",
            merkKendaraan = "",
            kendaraan = "",
            modelKendaraan = "",
            noRangka = "",
            noMesin = "",
            lokasi = "",
            jenisKendaraan = "",
            tglBeli = "",
            kodeTipe = "";
    private int expiredGaransiHari = 0, expiredGaransiKm = 0;
    private String isGaransiHari = "";
    private int kendaraanId = 0;
    private Nson keluhanList = Nson.newArray(), historyList = Nson.newArray();
    private boolean keyDel = false, isNoHp = false, isNamaValid = false, isRemoved;
    private boolean availHistory = false;
    private String tanggalBeliKendaraan = "";
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
        //initLokasiPenugasan();
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


        setSpinnerFromApi(spPekerjaan, "nama", "PEKERJAAN", "viewmst", "PEKERJAAN");
        initAutoCompleteNopol();
        initAutoCompleteNamaPelanggan();
        initAutoCompleteKendaraan();
        initAutoCompleteKeluhan();
        initOnTextChange();

        find(R.id.btn_lanjut_checkin1).setOnClickListener(this);
        find(R.id.btn_history_checkin1).setOnClickListener(this);
        find(R.id.fab_tambah_keluhan, ImageButton.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etKeluhan.getText().toString().isEmpty() || etKeluhan.getText().toString().length() < 5) {
                    etKeluhan.setError("Keluhan Min 5 Karakter");
                } else {
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
            Messagebox.showDialog(getActivity(), "Penugasan", "Lokasi Saat Ini", "TENDA", "BENGKEL", new DialogInterface.OnClickListener() {
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

    private void getHistoryCheckin(final String nopol) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "HISTORY");
                args.put("nopol", nopol);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(SET_CHECKIN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data");
                    if (result.asArray().size() > 0) {
                        find(R.id.btn_history_checkin1).setEnabled(true);
                        historyList.asArray().addAll(result.asArray());
                        for (int i = 0; i < result.size(); i++) {
                            if (result.get(i).get("GARANSI_LAYANAN_BULAN").asString().equals("VALID")) {
                                isGaransiHari = result.get(i).get("GARANSI_LAYANAN_HARI").asString();
                                expiredGaransiHari = !result.get(i).get("EXPIRATION_GARANSI_HARI").asString().isEmpty() ? result.get(i).get("EXPIRATION_GARANSI_HARI").asInteger() : 0;
                                expiredGaransiKm = !result.get(i).get("EXPIRATION_GARANSI_HARI").asString().isEmpty() ? result.get(i).get("EXPIRATION_GARANSI_KM").asInteger() : 0;
                                availHistory = true;
                                break;
                            }
                        }
                    }
                }
            }
        });
    }

    private void initOnTextChange() {
        watcherNamaPelanggan(find(R.id.img_clear, ImageButton.class), etNamaPelanggan);
        etNopol.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    keyDel = true;
                } else {
                    keyDel = false;
                }
                return false;
            }
        });

        etNopol.addTextChangedListener(new TextWatcher() {
            int lastLength;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                lastLength = s.length();
            }


            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @SuppressLint("SetTextI18n")
            @Override
            public void afterTextChanged(Editable s) {
//                if(!keyDel){
//                    String nopol = s.toString();
//                    int counting = s.toString().length();
//                    if (counting == 1 || counting == 6) {
//                        nopol = nopol + " ";
//                    }
//                    etNopol.setText(nopol);
//                    etNopol.setSelection(etNopol.getText().length());
//                }
            }
        });

        etNoPonsel.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && !etNoPonsel.getText().toString().contains("+62 ")) {
                    etNoPonsel.setText("+62 ");
                }
            }
        });

        etNoPonsel.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.toString().length() == 0) {
                    find(R.id.tl_nohp, TextInputLayout.class).setErrorEnabled(false);
                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                etNoPonsel.removeTextChangedListener(this);
                int counting = (s == null) ? 0 : s.toString().length();
                if (counting < 4 && !etNoPonsel.getText().toString().contains("+62 ")) {
                    etNoPonsel.setText("+62 ");
                    Selection.setSelection(etNoPonsel.getText(), etNoPonsel.getText().length());
                } else if (counting < 12) {
                    find(R.id.tl_nohp, TextInputLayout.class).setError("No. Hp Min. 6 Karakter");
                    etNoPonsel.requestFocus();
                } else {
                    find(R.id.tl_nohp, TextInputLayout.class).setErrorEnabled(false);
                }
                etNoPonsel.addTextChangedListener(this);
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
                Nson result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_PELANGGAN), args));
                return result.get("data");
            }

            @SuppressLint("SetTextI18n")
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.item_suggestion, parent, false);
                }

                String nama = getItem(position).get("NAMA_PELANGGAN").asString();
                String nomor = getItem(position).get("NO_PONSEL").asString();
                if (nomor.length() > 4) {
                    nomor = nomor.substring(nomor.length() - 4);
                }

                findView(convertView, R.id.title, TextView.class).setText(nama + " " + nomor);
                return convertView;
            }
        });

        etNamaPelanggan.setLoadingIndicator((android.widget.ProgressBar) findViewById(R.id.pb_namaPelanggan));
        etNamaPelanggan.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Nson n = Nson.readJson(String.valueOf(adapterView.getItemAtPosition(position)));

                noHp = n.get("NO_PONSEL").asString();
                pekerjaan = n.get("PEKERJAAN").asString();
                if (!noHp.isEmpty()) {
                    isNoHp = true;
                }

                String nomor = n.get("NO_PONSEL").asString();
                if (nomor.length() > 4) {
                    nomor = nomor.substring(nomor.length() - 4);
                }

                etNoPonsel.setText("XXXXXXXX" + nomor);
                etNamaPelanggan.setText(n.get("NAMA_PELANGGAN").asString());

                find(R.id.tl_nohp, TextInputLayout.class).setErrorEnabled(false);
                find(R.id.tl_nohp, TextInputLayout.class).setErrorEnabled(false);
                find(R.id.img_clear, ImageButton.class).setVisibility(View.GONE);

                setSpinnerFromApi(spPekerjaan, "nama", "PEKERJAAN", "viewmst", "PEKERJAAN", pekerjaan);
                if (n.get("PEMILIK").asString().equalsIgnoreCase("Y")) {
                    find(R.id.cb_pemilik_checkin1, CheckBox.class).setChecked(true);
                }
                Tools.hideKeyboard(getActivity());
            }
        });
    }

    private void initAutoCompleteNopol() {
        etNopol.setThreshold(3);
        etNopol.setAdapter(new NsonAutoCompleteAdapter(getActivity()) {
            @Override
            public Nson onFindNson(Context context, String bookTitle) {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                String nopol = bookTitle.replaceAll(" ", "").toUpperCase();
                args.put("nopol", nopol);
                Nson result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_NOMOR_POLISI), args));
                return result.get("data");
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.item_suggestion, parent, false);
                }
                findView(convertView, R.id.title, TextView.class).setText(formatNopol(getItem(position).get("NO_POLISI").asString()));
                return convertView;
            }
        });

        etNopol.setLoadingIndicator((android.widget.ProgressBar) findViewById(R.id.pb_nopol_checkin));
        etNopol.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Nson n = Nson.readJson(String.valueOf(adapterView.getItemAtPosition(position)));

                tanggalBeliKendaraan = n.get("TANGGAL_BELI").asString();
                merkKendaraan = n.get("MERK").asString();
                kendaraan = n.get("TYPE").asString();
                varianKendaraan = n.get("VARIAN").asString();
                modelKendaraan = n.get("MODEL").asString();
                kendaraanId = n.get("KENDARAAN_ID").asInteger();
                noHp = n.get("NO_PONSEL").asString();

                if (!noHp.isEmpty())
                    isNoHp = true;

                pekerjaan = n.get("PEKERJAAN").asString();
                noRangka = n.get("NO_RANGKA").asString();
                noMesin = n.get("NO_MESIN").asString();
                tglBeli = n.get("TANGGAL_BELI").asString();
                tahunProduksi = n.get("TAHUN_PRODUKSI").asString();
                jenisKendaraan = n.get("JENIS").asString();
                kodeTipe = n.get("CODE_TYPE").asString();

                String nomor = n.get("NO_PONSEL").asString();
                if (nomor.length() > 4) {
                    nomor = nomor.substring(nomor.length() - 4);
                }

                etNopol.setText(formatNopol(n.get("NO_POLISI").asString()));
                etNoPonsel.setText("XXXXXXXX" + nomor);
                etNamaPelanggan.setText(n.get("NAMA_PELANGGAN").asString());
                etJenisKendaraan.setText(n.get("JENIS_KENDARAAN").asString());
                getHistoryCheckin(n.get("NO_POLISI").asString());

                setSpinnerFromApi(spPekerjaan, "nama", "PEKERJAAN", "viewmst", "PEKERJAAN", pekerjaan);
                if (n.get("PEMILIK").asString().equalsIgnoreCase("Y")) {
                    find(R.id.cb_pemilik_checkin1, CheckBox.class).setChecked(true);
                }

                etJenisKendaraan.setEnabled(merkKendaraan.isEmpty() || varianKendaraan.isEmpty());
                find(R.id.tl_nohp, TextInputLayout.class).setErrorEnabled(false);
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
                Nson result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_JENIS_KENDARAAN), args));
                return result.get("data");
            }


            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.find_jenisken, parent, false);
                }

                //findView(convertView, R.id.txtJenis, TextView.class).setText((getItem(position).get("JENIS").asString()));
                findView(convertView, R.id.txtVarian, TextView.class).setText((getItem(position).get("VARIAN").asString()));
                findView(convertView, R.id.txtMerk, TextView.class).setText((getItem(position).get("MERK").asString()));
                //findView(convertView, R.id.txtModel, TextView.class).setText((getItem(position).get("MODEL").asString()));

                return convertView;
            }
        });

        etJenisKendaraan.setLoadingIndicator((android.widget.ProgressBar) findViewById(R.id.pb_kendaraan_checkin));
        etJenisKendaraan.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Nson n = Nson.readJson(String.valueOf(adapterView.getItemAtPosition(position)));
                String kendaraan = getSetting("JENIS_KENDARAAN");
                if (!kendaraan.equalsIgnoreCase(n.get("TYPE").asString())) {
                    showWarning("Bengkel Hanya Melayani Kendaraan " + kendaraan, Toast.LENGTH_LONG);
                    etJenisKendaraan.setText("");
                    etJenisKendaraan.requestFocus();
                    return;
                }
                //stringBuilder.append(n.get("MODEL").asString()).append(" ");

                kendaraanId = n.get("ID").asInteger();
                merkKendaraan = n.get("MERK").asString();
                varianKendaraan = n.get("VARIAN").asString();
                Checkin1_Activity.this.kendaraan = n.get("TYPE").asString();
                modelKendaraan = n.get("MODEL").asString();
                tahunProduksi = n.get("TAHUN1").asString();
                jenisKendaraan = n.get("JENIS").asString();

                String stringBuilder = n.get("MERK").asString() + " " +
                        //stringBuilder.append(n.get("JENIS").asString()).append(" ");
                        n.get("VARIAN").asString() + " ";
                etJenisKendaraan.setText(stringBuilder);
                etJenisKendaraan.setTag(String.valueOf(adapterView.getItemAtPosition(position)));
            }
        });
    }

    private void initAutoCompleteKeluhan() {
        etKeluhan.setThreshold(0);
        etKeluhan.setAdapter(new NsonAutoCompleteAdapter(getActivity()) {
            @Override
            public Nson onFindNson(Context context, String bookTitle) {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "KELUHAN");
                args.put("keluhan", bookTitle);
                Nson result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_SUGGESTION), args));
                return result.get("data");
            }

            @SuppressLint("SetTextI18n")
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.item_suggestion, parent, false);
                }

                findView(convertView, R.id.title, TextView.class).setText(getItem(position).get("KELUHAN").asString());
                return convertView;
            }
        });

        etKeluhan.setLoadingIndicator((android.widget.ProgressBar) findViewById(R.id.pb_keluhan));
        etKeluhan.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Nson n = Nson.readJson(String.valueOf(adapterView.getItemAtPosition(position)));
                etKeluhan.setText(n.get("KELUHAN").asString());
                Tools.hideKeyboard(getActivity());
            }
        });

    }

    private void setSelanjutnya() {
        final String nopol = etNopol.getText().toString().replaceAll(" ", "").toUpperCase();
        final String namaPelanggan = etNamaPelanggan.getText().toString().toUpperCase();
        final String km = etKm.getText().toString();
        final String pekerjaan = spPekerjaan.getSelectedItem().toString().toUpperCase();
        final String pemilik = find(R.id.cb_pemilik_checkin1, CheckBox.class).isChecked() ? "Y" : "N";
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "add");
                args.put("jenisCheckin", "1");
                args.put("nopol", nopol);
                args.put("jeniskendaraan", etJenisKendaraan.getText().toString().toUpperCase());//dateKendaraan
                args.put("noPonsel", isNoHp ? noHp : formatOnlyNumber(etNoPonsel.getText().toString()));
                args.put("nama", Tools.isSingleQuote(namaPelanggan));
                args.put("isPemilik", pemilik);//dateKendaraan
                args.put("keluhan_list", keluhanList.toJson());
                args.put("km", km);
                args.put("pekerjaan", pekerjaan);
                args.put("kendaraan", getSetting("JENIS_KENDARAAN"));
                args.put("model", modelKendaraan);
                args.put("merk", merkKendaraan);//dateKendaraan
                args.put("varian", varianKendaraan);//dateKendaraan
                args.put("kendaraan_id", String.valueOf(kendaraanId));
                args.put("lokasiLayanan", getSetting("LOKASI_PENUGASAN"));
                args.put("rangka", noRangka);
                args.put("mesin", noMesin);
                args.put("jenis", jenisKendaraan);//dateKendaraan
                args.put("noStnk", "");//dateKendaraan
                args.put("kadaluarsa", "");//dateKendaraan
                args.put("idDealer", "");//dateKendaraan
                args.put("asalData", "");//dateKendaraan
                args.put("type", kendaraan);//dateKendaraan
                args.put("kodeTipe", kodeTipe);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(SET_CHECKIN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    Nson nson = Nson.newObject();

                    nson.set("CHECKIN_ID", result.get("data").get("CHECKIN_ID").asString());
                    int idKendaraan;
                    if (result.get("data").containsKey("DATA_KENDARAAN_ID")) {
                        idKendaraan = result.get("data").get("DATA_KENDARAAN_ID").asInteger();
                    } else {
                        idKendaraan = result.get("data").get("DATA_KENDARAAN_ID_UPDATE").asInteger();
                    }

                    nson.set("KODE_TIPE", kodeTipe);
                    nson.set("DATA_KENDARAAN_ID", idKendaraan);
                    nson.set("kendaraan", kendaraan);
                    nson.set("model", modelKendaraan);
                    nson.set("merk", merkKendaraan);
                    nson.set("varian", varianKendaraan);
                    nson.set("tahunProduksi", tahunProduksi);
                    nson.set("km", etKm.getText().toString());
                    nson.set("NOPOL", nopol);
                    nson.set("isExpiredKm", Integer.parseInt(formatOnlyNumber(etKm.getText().toString())) < expiredGaransiKm);
                    nson.set("isExpiredHari", isGaransiHari.equals("VALID"));
                    nson.set("expiredKmVal", Integer.parseInt(formatOnlyNumber(etKm.getText().toString())) < expiredGaransiKm ? expiredGaransiKm : 0);
                    nson.set("expiredHariVal", isGaransiHari.equals("VALID") ? expiredGaransiHari : 0);
                    nson.set("jenisKendaraan", jenisKendaraan);
                    nson.set("noRangka", noRangka);
                    nson.set("noMesin", noMesin);
                    nson.set("tglBeli", tglBeli);
                    nson.set("tahunProduksi", tahunProduksi);
                    nson.set("pekerjaan", pekerjaan);
                    nson.set("noPonsel", isNoHp ? noHp : formatOnlyNumber(etNoPonsel.getText().toString()));
                    nson.set("nopol", nopol);
                    nson.set("kendaraanPelanggan", etJenisKendaraan.getText().toString());
                    nson.set("availHistory", availHistory);
                    nson.set("tanggalBeli", tanggalBeliKendaraan);

                    Intent intent;
                    if (!noRangka.isEmpty() && !noMesin.isEmpty()) {
                        intent = new Intent(getActivity(), Checkin3_Activity.class);
                        intent.putExtra(DATA, nson.toJson());
                        startActivityForResult(intent, REQUEST_CHECKIN);
                    } else {
                        intent = new Intent(getActivity(), Checkin2_Activity.class);
                        intent.putExtra(DATA, nson.toJson());
                        startActivityForResult(intent, REQUEST_NEW_CS);
                    }
                } else {
                    showWarning(result.get("message").asString());
                }
            }
        });
    }

    private void getDataBarcode(final String antrian) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "barcode");
                args.put("antrian", antrian);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(SET_CHECKIN), args));
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
        rvKeluhan.setHasFixedSize(false);
        rvKeluhan.setAdapter(new NikitaRecyclerAdapter(keluhanList, R.layout.item_keluhan) {
            @Override
            public void onBindViewHolder(@NonNull final NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                viewHolder.find(R.id.tv_keluhan, TextView.class).setText(keluhanList.get(position).get("KELUHAN").asString());
                viewHolder.find(R.id.img_delete, ImageButton.class).setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("NewApi")
                    @Override
                    public void onClick(View v) {
                        isRemoved = false;
                        keluhanList.asArray().remove(position);
                        notifyItemRemoved(position);
                        Objects.requireNonNull(rvKeluhan.getAdapter()).notifyDataSetChanged();
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CHECKIN) {
            setResult(RESULT_OK);
            finish();
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_BARCODE) {
            String antrian = data.getStringExtra("TEXT");
            getDataBarcode(antrian);
            String result = data.getStringExtra("FORMAT");
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_HISTORY) {

        } else if (resultCode == RESULT_OK && requestCode == REQUEST_NEW_CS) {
            Intent i = new Intent(getActivity(), Checkin3_Activity.class);
            i.putExtra(DATA, Nson.readJson(getIntentStringExtra(data, DATA)).toJson());
            startActivityForResult(i, REQUEST_CHECKIN);
        } else {
            finish();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_lanjut_checkin1:
                if (etNopol.getText().toString().isEmpty()) {
                    etNopol.setError("Harus Di Isi");
                    etNopol.requestFocus();
                } else if (etJenisKendaraan.getText().toString().isEmpty() || etJenisKendaraan.getText().toString().equals(" ")) {
                    etJenisKendaraan.setError("Harus Di Isi");
                    etJenisKendaraan.requestFocus();
                }else if(kendaraanId == 0){
                    showWarning("Kendaraan Harus di isi dari Suggestion", Toast.LENGTH_LONG);
                    etJenisKendaraan.requestFocus();
                } else if (etNoPonsel.getText().toString().isEmpty() || etNoPonsel.getText().toString().length() < 6) {
                    etNoPonsel.setError("Harus Di Isi");
                    etNoPonsel.requestFocus();
                } else if (etNamaPelanggan.getText().toString().isEmpty() || etNamaPelanggan.getText().toString().length() < 5) {
                    etNamaPelanggan.setError("Harus Di Isi");
                    etNamaPelanggan.requestFocus();
                } else if (spPekerjaan.getSelectedItem().toString().equalsIgnoreCase("Belum Di Pilih")) {
                    showWarning("Silahkan Pilih Pekerjaan");
                    spPekerjaan.performClick();
                } else if (spPekerjaan.getSelectedItem().toString().equalsIgnoreCase("--PILIH--")) {
                    spPekerjaan.performClick();
                    showWarning("Pekerjaan Harus Di Pilih");
                } else {
                    setSelanjutnya();
                }
                break;
            case R.id.btn_history_checkin1:
                Intent i = new Intent(getActivity(), HistoryBookingCheckin_Activity.class);
                i.putExtra(DATA, historyList.toJson());
                startActivityForResult(i, REQUEST_HISTORY);
                break;
        }
    }


}
