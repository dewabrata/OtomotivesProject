package com.rkrzmail.oto.modules.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.naa.data.Nson;
import com.naa.data.UtilityAndroid;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.Adapter.Checkin_MainTab_Activity;
import com.rkrzmail.oto.modules.Adapter.NsonAutoCompleteAdapter;
import com.rkrzmail.oto.modules.checkin.Checkin2_Activity;
import com.rkrzmail.oto.modules.checkin.Checkin3_Activity;
import com.rkrzmail.oto.modules.checkin.History_Activity;
import com.rkrzmail.srv.MultipartRequest;
import com.rkrzmail.srv.NikitaAutoComplete;
import com.rkrzmail.srv.NumberFormatUtils;
import com.rkrzmail.utils.Tools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.rkrzmail.utils.APIUrls.SAVE_IMAGE_CHECKIN;
import static com.rkrzmail.utils.APIUrls.SET_CHECKIN;
import static com.rkrzmail.utils.APIUrls.VIEW_JENIS_KENDARAAN;
import static com.rkrzmail.utils.APIUrls.VIEW_NOMOR_POLISI;
import static com.rkrzmail.utils.APIUrls.VIEW_PELANGGAN;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.PERMISSION_REQUEST_CODE;
import static com.rkrzmail.utils.ConstUtils.REQUEST_BARCODE;
import static com.rkrzmail.utils.ConstUtils.REQUEST_CHECKIN;
import static com.rkrzmail.utils.ConstUtils.REQUEST_FOTO;
import static com.rkrzmail.utils.ConstUtils.REQUEST_NEW_CS;

public class Checkin1_Checkin_Fragment extends Fragment implements View.OnClickListener {

    private View fragmentView;
    private AppActivity activity;
    private NikitaAutoComplete etJenisKendaraan, etNopol, etNoPonsel, etNamaPelanggan;

    private EditText etKm;
    private Spinner spPekerjaan;
    private File tempFile = null;
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
            kodeTipe = "",
            alamat = "",
            kotaKab = "",
            warna = "";
    private int expiredGaransiHari = 0, expiredGaransiKm = 0, kmCheckinBefore = 0, totalHariCheckin = 0;
    private int kendaraanId = 0;
    private Nson historyList = Nson.newArray();
    private boolean keyDel = false, isNoHp = false, isNamaValid = false;
    private boolean availHistory = false;
    private String tanggalBeliKendaraan = "";
    private final String[] base64fotoKM = {""};
    private Bitmap kmBitmap = null;
    private AlertDialog alertDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.activity_checkin1, container, false);
        activity = (Checkin_MainTab_Activity) getActivity();
        initHideToolbar();
        initComponent();
        initAutoCompleteNopol();
        initAutoCompleteNamaPelanggan();
        initAutoCompleteKendaraan();
        initOnTextChange();
        return fragmentView;
    }

    private void initHideToolbar() {
        AppBarLayout appBarLayout = fragmentView.findViewById(R.id.appbar);
        appBarLayout.setVisibility(View.GONE);
    }

    private void initComponent() {
        etNopol = fragmentView.findViewById(R.id.et_nopol_checkin1);
        etJenisKendaraan = fragmentView.findViewById(R.id.et_jenisKendaraan_checkin1);
        etNoPonsel = fragmentView.findViewById(R.id.et_noPonsel_checkin1);
        etNamaPelanggan = fragmentView.findViewById(R.id.et_namaPelanggan_checkin1);
        etKm = fragmentView.findViewById(R.id.et_km_checkin1);
        spPekerjaan = fragmentView.findViewById(R.id.sp_pekerjaan_checkin1);

        fragmentView.findViewById(R.id.img_clear_jenis_kendaraan).setOnClickListener(this);
        fragmentView.findViewById(R.id.btn_lanjut_checkin1).setOnClickListener(this);
        fragmentView.findViewById(R.id.btn_history_checkin1).setOnClickListener(this);
        fragmentView.findViewById(R.id.img_clear).setOnClickListener(this);
        find(R.id.btn_foto_km).setOnClickListener(this);

        activity.setSpinnerFromApi(spPekerjaan, "nama", "PEKERJAAN", "viewmst", "PEKERJAAN");
        activity.watcherNamaPelanggan(((ImageButton) fragmentView.findViewById(R.id.img_clear)), etNamaPelanggan);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_clear_jenis_kendaraan:
                etJenisKendaraan.setText("");
                break;
            case R.id.btn_lanjut_checkin1:
                if (etNopol.getText().toString().isEmpty()) {
                    etNopol.setError("Harus Di Isi");
                    etNopol.requestFocus();
                } else if (etJenisKendaraan.getText().toString().isEmpty() || etJenisKendaraan.getText().toString().equals(" ") || varianKendaraan.isEmpty()) {
                    etJenisKendaraan.setError("Jenis Kendaraan Belum Lengkap");
                    activity.viewFocus(etJenisKendaraan);
                } else if (kendaraanId == 0) {
                    activity.showWarning("Kendaraan Harus di isi dari Suggestion", Toast.LENGTH_LONG);
                    activity.viewFocus(etJenisKendaraan);
                } else if (etNoPonsel.getText().toString().isEmpty() || etNoPonsel.getText().toString().length() < 6) {
                    etNoPonsel.setError("Harus Di Isi");
                    activity.viewFocus(etNoPonsel);
                } else if (etNamaPelanggan.getText().toString().isEmpty() || etNamaPelanggan.getText().toString().length() < 5) {
                    etNamaPelanggan.setError("Harus Di Isi");
                    activity.viewFocus(etNamaPelanggan);
                } else if (spPekerjaan.getSelectedItem().toString().equalsIgnoreCase("--PILIH--")) {
                    activity.setErrorSpinner(spPekerjaan, "Silahkan Pilih Pekerjaan");
                } else {
                    if(kmBitmap != null && etKm.getText().toString().isEmpty() ){
                        etKm.setError("KM HARUS DI ISI");
                        activity.viewFocus(etKm);
                    }else {
                        setSelanjutnya();
                    }
                }
                break;
            case R.id.btn_history_checkin1:
                Intent intent = new Intent(getActivity(), History_Activity.class);
                intent.putExtra("ALL", "ALL");
                intent.putExtra("NOPOL", etNopol.getText().toString().replaceAll(" ", ""));
                startActivity(intent);
                break;
            case R.id.img_clear:
                etNamaPelanggan.setText("");
                etNoPonsel.setText("");
                break;
            case R.id.btn_foto_km:
                if (kmBitmap == null) {
                    getImagePickOrCamera();
                } else {
                    showDialogPreviewFoto(true);
                }
                break;
        }
    }

    private void initOnTextChange() {
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
            int prevLength = 0;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                prevLength = s.length();
                Log.d("length__", "beforeTextChanged: " + prevLength);
                if (s.toString().length() == 0) {
                    ((TextInputLayout) fragmentView.findViewById(R.id.tl_nohp)).setErrorEnabled(false);
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
                if (counting == 0) return;
                if (counting < 4 && !etNoPonsel.getText().toString().contains("+62 ")) {
                    etNoPonsel.setTag(null);
                    etNoPonsel.setText("+62 ");
                    Selection.setSelection(etNoPonsel.getText(), etNoPonsel.getText().length());
                } else if (counting < 12) {
                    ((TextInputLayout) fragmentView.findViewById(R.id.tl_nohp)).setError("No. Hp Min. 6 Karakter");
                    etNoPonsel.requestFocus();
                } else {
                    ((TextInputLayout) fragmentView.findViewById(R.id.tl_nohp)).setErrorEnabled(false);
                }

                if (prevLength > s.length()) {
                    isNoHp = false;
                }

                validateNoPonsel(NumberFormatUtils.formatOnlyNumber(etNoPonsel.getText().toString()));
                etNoPonsel.addTextChangedListener(this);
            }
        });
    }

    private void validateNoPonsel(@NonNull final String noPonsel) {
        if (noPonsel.length() > 10) {
            fragmentView.findViewById(R.id.pb_etNoPonsel_checkin).setVisibility(View.VISIBLE);
        } else {
            return;
        }
        activity.newTask(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "ALL PELANGGAN");
                args.put("search", noPonsel);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_PELANGGAN), args));
            }

            @Override
            public void runUI() {
                fragmentView.findViewById(R.id.pb_etNoPonsel_checkin).setVisibility(View.GONE);
                if (!etNamaPelanggan.getText().toString().isEmpty()) {
                    result = result.get("data").get(0);
                    String dataNama = result.get("NAMA_PELANGGAN").asString();
                    String dataNoponsel = result.get("NO_PONSEL").asString();
                    if (!etNamaPelanggan.getText().toString().equals(dataNama)
                            && noPonsel.equals(dataNoponsel)) {
                        ((TextInputLayout) fragmentView.findViewById(R.id.tl_nohp)).setError("NO PONSEL TIDAK VALID DENGAN NAMA PELANGGAN");
                    } else {
                        ((TextInputLayout) fragmentView.findViewById(R.id.tl_nohp)).setErrorEnabled(false);
                    }
                }
            }
        });
    }


    private void getHistoryCheckin(final String nopol) {
        activity.newProses(new Messagebox.DoubleRunnable() {
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
                    if (result.size() > 0) {
                        fragmentView.findViewById(R.id.btn_history_checkin1).setEnabled(true);
                        result = result.get(0);
                        kmCheckinBefore = result.get("KM").asInteger();
                        totalHariCheckin = result.get("TOTAL_HARI_CHECKIN").asInteger();
                        expiredGaransiKm = result.get("EXPIRATION_GARANSI_KM").asInteger();
                        expiredGaransiHari = result.get("EXPIRATION_GARANSI_HARI").asInteger();
                        availHistory = !result.get("LAYANAN").asString().equals("INSPEKSI & ESTIMASI");
                    }
                }
            }
        });
    }

    private void initAutoCompleteNamaPelanggan() {
        etNamaPelanggan.setThreshold(0);
        etNamaPelanggan.setAdapter(new NsonAutoCompleteAdapter(getActivity()) {
            @Override
            public Nson onFindNson(Context context, String bookTitle) {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "PELANGGAN");
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

        etNamaPelanggan.setLoadingIndicator((android.widget.ProgressBar) fragmentView.findViewById(R.id.pb_namaPelanggan));
        etNamaPelanggan.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Nson n = Nson.readJson(String.valueOf(adapterView.getItemAtPosition(position)));

                alamat = n.get("ALAMAT").asString();
                kotaKab = n.get("KOTA_KAB").asString();
                noHp = n.get("NO_PONSEL").asString();
                pekerjaan = n.get("PEKERJAAN").asString();
                isNoHp = !noHp.isEmpty();

                String nomor = n.get("NO_PONSEL").asString();
                if (nomor.length() > 4) {
                    nomor = nomor.substring(nomor.length() - 4);
                }

                etNoPonsel.setTag(nomor);
                etNoPonsel.setText("XXXXXXXX" + nomor);
                etNamaPelanggan.setText(n.get("NAMA_PELANGGAN").asString());
                ((EditText) fragmentView.findViewById(R.id.et_kode_pos)).setText(n.get("KODE_POS").asString());
                ((EditText) fragmentView.findViewById(R.id.et_email)).setText(n.get("EMAIL").asString());

                ((TextInputLayout) fragmentView.findViewById(R.id.tl_nohp)).setErrorEnabled(false);
                fragmentView.findViewById(R.id.img_clear).setVisibility(View.GONE);

                activity.setSpinnerFromApi(spPekerjaan, "nama", "PEKERJAAN", "viewmst", "PEKERJAAN", pekerjaan);
                if (n.get("PEMILIK").asString().equalsIgnoreCase("Y")) {
                    ((CheckBox) fragmentView.findViewById(R.id.cb_pemilik_checkin1)).setChecked(true);
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
                args.put("action", "SUGGESTION");
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
                findView(convertView, R.id.title, TextView.class).setText(activity.formatNopol(getItem(position).get("NO_POLISI").asString()));
                return convertView;
            }
        });

        etNopol.setLoadingIndicator((android.widget.ProgressBar) fragmentView.findViewById(R.id.pb_nopol_checkin));
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
                alamat = n.get("ALAMAT").asString();
                kotaKab = n.get("KOTA_KAB").asString();
                warna = n.get("WARNA").asString();
                isNoHp = !noHp.isEmpty();

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

                find(R.id.et_kode_pos, EditText.class).setText(n.get("KODE_POS").asString());
                find(R.id.et_email, EditText.class).setText(n.get("EMAIL").asString());
                etNopol.setText(activity.formatNopol(n.get("NO_POLISI").asString()));
                etNoPonsel.setTag(nomor);
                etNoPonsel.setText("XXXXXXXX" + nomor);
                etNamaPelanggan.setText(n.get("NAMA_PELANGGAN").asString());
                etJenisKendaraan.setText(n.get("JENIS_KENDARAAN").asString());
                getHistoryCheckin(n.get("NO_POLISI").asString());

                activity.setSpinnerFromApi(spPekerjaan, "nama", "PEKERJAAN", "viewmst", "PEKERJAAN", pekerjaan);
                if (n.get("PEMILIK").asString().equalsIgnoreCase("Y")) {
                    find(R.id.cb_pemilik_checkin1, CheckBox.class).setChecked(true);
                }

                //etJenisKendaraan.setEnabled(merkKendaraan.isEmpty() || varianKendaraan.isEmpty());
                find(R.id.tl_nohp, TextInputLayout.class).setErrorEnabled(false);
                find(R.id.img_clear, ImageButton.class).setVisibility(View.GONE);
            }
        });

    }

    private void initAutoCompleteKendaraan() {
        etJenisKendaraan.addTextChangedListener(new TextWatcher() {
            int startChar;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                etJenisKendaraan.removeTextChangedListener(this);
                int textLength = s.toString().length();
                if (textLength == 0) {
                    find(R.id.img_clear_jenis_kendaraan).setVisibility(View.GONE);
                    return;
                } else {
                    find(R.id.img_clear_jenis_kendaraan).setVisibility(View.VISIBLE);
                }
                etJenisKendaraan.addTextChangedListener(this);
            }
        });

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

        etJenisKendaraan.setLoadingIndicator((android.widget.ProgressBar) fragmentView.findViewById(R.id.pb_kendaraan_checkin));
        etJenisKendaraan.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Nson n = Nson.readJson(String.valueOf(adapterView.getItemAtPosition(position)));
                String kendaraan = UtilityAndroid.getSetting(getContext(), "JENIS_KENDARAAN", "");
                if (!kendaraan.equalsIgnoreCase(n.get("TYPE").asString())) {
                    activity.showWarning("Bengkel Hanya Melayani Kendaraan " + kendaraan, Toast.LENGTH_LONG);
                    etJenisKendaraan.setText("");
                    etJenisKendaraan.requestFocus();
                    return;
                }

                kendaraanId = n.get("ID").asInteger();
                merkKendaraan = n.get("MERK").asString();
                varianKendaraan = n.get("VARIAN").asString();
                Checkin1_Checkin_Fragment.this.kendaraan = n.get("TYPE").asString();
                modelKendaraan = n.get("MODEL").asString();
                tahunProduksi = n.get("TAHUN1").asString();
                jenisKendaraan = n.get("JENIS").asString();

                String stringBuilder = n.get("MERK").asString() + " " + n.get("VARIAN").asString() + " ";
                etJenisKendaraan.setText(stringBuilder);
                etJenisKendaraan.setTag(String.valueOf(adapterView.getItemAtPosition(position)));
            }
        });
    }


    public <T extends View> T find(int id, Class<? super T> s) {
        return (T) fragmentView.findViewById(id);
    }

    public <T extends View> T find(int id) {
        return (T) fragmentView.findViewById(id);
    }

    public <T extends View> T findView(View v, int id, Class<? super T> s) {
        return (T) v.findViewById(id);
    }

    private void setSelanjutnya() {
        if (!Tools.isNetworkAvailable(getActivity())) {
            activity.showWarning("TIDAK ADA KONEKSI INTERNET", Toast.LENGTH_LONG);
            return;
        }
        final String nopol = etNopol.getText().toString().replaceAll(" ", "").toUpperCase();
        final String namaPelanggan = etNamaPelanggan.getText().toString().toUpperCase();
        final String km = etKm.getText().toString();
        final String pekerjaan = spPekerjaan.getSelectedItem().toString().toUpperCase();
        final String pemilik = find(R.id.cb_pemilik_checkin1, CheckBox.class).isChecked() ? "Y" : "N";
        activity.newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "add");
                args.put("jenisCheckin", "1");
                args.put("nopol", nopol);
                args.put("jeniskendaraan", etJenisKendaraan.getText().toString().toUpperCase());//dateKendaraan
                args.put("noPonsel", isNoHp && etNoPonsel.getTag() != null ? noHp : NumberFormatUtils.formatOnlyNumber(etNoPonsel.getText().toString()));
                args.put("nama", Tools.isSingleQuote(namaPelanggan));
                args.put("isPemilik", pemilik);//dateKendaraan
                args.put("km", km);
                args.put("pekerjaan", pekerjaan);
                args.put("kendaraan", UtilityAndroid.getSetting(getContext(), "JENIS_KENDARAAN", ""));
                args.put("model", modelKendaraan);
                args.put("merk", merkKendaraan);//dateKendaraan
                args.put("varian", varianKendaraan);//dateKendaraan
                args.put("kendaraan_id", String.valueOf(kendaraanId));
                args.put("lokasiLayanan", UtilityAndroid.getSetting(getContext(), "LOKASI_PENUGASAN", ""));
                args.put("rangka", noRangka);
                args.put("mesin", noMesin);
                args.put("jenis", jenisKendaraan);//dateKendaraan
                args.put("noStnk", "");//dateKendaraan
                args.put("kadaluarsa", "");//dateKendaraan
                args.put("idDealer", "");//dateKendaraan
                args.put("asalData", "");//dateKendaraan
                args.put("type", kendaraan);//dateKendaraan
                args.put("kodeTipe", kodeTipe);
                args.put("tanggalbeli", tanggalBeliKendaraan);
                if (!km.isEmpty()) {
                    args.put("fotoKm", base64fotoKM[0]);
                }
                args.put("email", find(R.id.et_email, EditText.class).getText().toString());
                args.put("kodePos", find(R.id.et_kode_pos, EditText.class).getText().toString());

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(SET_CHECKIN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    if(kmBitmap != null){
                        uploadFotoKm(result.get("data").get("CHECKIN_ID").asString());
                    }

                    Nson nson = Nson.newObject();

                    nson.set("CHECKIN_ID", result.get("data").get("CHECKIN_ID").asString());
                    int idDataKendaraan;
                    if (result.get("data").containsKey("DATA_KENDARAAN_ID")) {
                        idDataKendaraan = result.get("data").get("DATA_KENDARAAN_ID").asInteger();
                    } else {
                        idDataKendaraan = result.get("data").get("DATA_KENDARAAN_ID_UPDATE").asInteger();
                    }

                    nson.set("WARNA", warna);
                    nson.set("KENDARAAN_ID", kendaraanId);
                    nson.set("KODE_TIPE", kodeTipe);
                    nson.set("DATA_KENDARAAN_ID", idDataKendaraan);
                    nson.set("kendaraan", kendaraan);
                    nson.set("model", modelKendaraan);
                    nson.set("merk", merkKendaraan);
                    nson.set("varian", varianKendaraan);
                    nson.set("tahunProduksi", tahunProduksi);
                    nson.set("km", etKm.getText().toString());
                    nson.set("NOPOL", nopol);
                    nson.set("isExpiredKm", (Integer.parseInt(NumberFormatUtils.formatOnlyNumber(etKm.getText().toString())) - kmCheckinBefore) > expiredGaransiKm);
                    nson.set("isExpiredHari", totalHariCheckin > expiredGaransiHari);
                    nson.set("expiredKmVal", expiredGaransiKm);
                    nson.set("expiredHariVal", expiredGaransiHari);
                    nson.set("jenisKendaraan", jenisKendaraan);
                    nson.set("noRangka", noRangka);
                    nson.set("noMesin", noMesin);
                    nson.set("tglBeli", tglBeli);
                    nson.set("tahunProduksi", tahunProduksi);
                    nson.set("pekerjaan", pekerjaan);
                    nson.set("noPonsel", isNoHp ? noHp : NumberFormatUtils.formatOnlyNumber(etNoPonsel.getText().toString()));
                    nson.set("nopol", nopol);
                    nson.set("kendaraanPelanggan", etJenisKendaraan.getText().toString());
                    nson.set("availHistory", availHistory);
                    nson.set("tanggalBeli", tanggalBeliKendaraan);
                    nson.set("KOTA", kotaKab);
                    nson.set("ALAMAT", alamat);
                    /*if (noRangka.isEmpty()) {
                        setIntentCheckin2(nson);
                    } else if (noMesin.isEmpty()) {
                        setIntentCheckin2(nson);
                    } */
                    if (alamat.isEmpty()) {
                        setIntentCheckin2(nson);
                    } else if (kotaKab.isEmpty()) {
                        setIntentCheckin2(nson);
                    } else if (kodeTipe.isEmpty()) {
                        setIntentCheckin2(nson);
                    } else if (tahunProduksi.isEmpty()) {
                        setIntentCheckin2(nson);
                    } else if (tglBeli.isEmpty()) {
                        setIntentCheckin2(nson);
                    } else {
                        setIntentCheckin3(nson);
                    }

                } else {
                    activity.showWarning(result.get("message").asString());
                }
            }
        });
    }

    private void setIntentCheckin2(Nson nson) {
        Intent intent = new Intent(getActivity(), Checkin2_Activity.class);
        intent.putExtra(DATA, nson.toJson());
        startActivityForResult(intent, REQUEST_NEW_CS);
    }

    private void setIntentCheckin3(Nson nson) {
        Intent intent = new Intent(getActivity(), Checkin3_Activity.class);
        intent.putExtra(DATA, nson.toJson());
        startActivityForResult(intent, REQUEST_CHECKIN);
    }

    private void getDataBarcode(final String antrian) {
        activity.newProses(new Messagebox.DoubleRunnable() {
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
                    activity.showWarning(result.get("message").asString());
                }
            }
        });
    }

    private void getImagePickOrCamera() {
        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext()), CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()), new String[]{WRITE_EXTERNAL_STORAGE
                    , READ_EXTERNAL_STORAGE, CAMERA}, PERMISSION_REQUEST_CODE);
        } else {
            final List<Intent> intents = new ArrayList<>();
            intents.add(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
            intents.add(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));

            Intent result = Intent.createChooser(intents.remove(0), null);
            result.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toArray(new Parcelable[]{}));
            startActivityForResult(result, REQUEST_FOTO);
        }
    }

    private String getPath(Uri selectedImageUri) {
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = getContext().getContentResolver().query(selectedImageUri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            cursor.moveToFirst();
            String result = cursor.getString(column_index);
            cursor.close();
            return result;
        } else
            return null;
    }

    private void getImageUri(final Uri imageUri, final Bundle imgBundle) {
        activity.newProses(new Messagebox.DoubleRunnable() {
            @Override
            public void run() {
                if (imageUri != null) {
                    ImageView dummy = new ImageView(getContext());
                    dummy.setImageURI(imageUri);
                    BitmapDrawable drawable = (BitmapDrawable) dummy.getDrawable();
                    kmBitmap = drawable.getBitmap();
                } else {
                    kmBitmap = (Bitmap) (imgBundle != null ? imgBundle.get("data") : null);
                }
            }

            @Override
            public void runUI() {
                try {
                    showDialogPreviewFoto(false);
                } catch (Exception e) {
                    activity.showError(e.getMessage());
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void showDialogPreviewFoto(final boolean isPreview) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_alert_camera, null);
        builder.setView(dialogView);
        alertDialog = builder.create();

        Toolbar toolbar = dialogView.findViewById(R.id.toolbar);
        ImageView img = (ImageView) dialogView.findViewById(R.id.img_alert_foto);
        Button btnCancel = dialogView.findViewById(R.id.btn_alert_cancel);
        Button btnSimpan = dialogView.findViewById(R.id.btn_alert_save);

        activity.setSupportActionBar(toolbar);
        if (activity.getSupportActionBar() != null)
            activity.getSupportActionBar().setTitle("Preview KM");

        if (kmBitmap != null)
            img.setImageBitmap(kmBitmap);

        if (isPreview) {
            btnCancel.setText("Tutup");
            btnSimpan.setText("Foto Ulang");
        }

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPreview) {
                    alertDialog.dismiss();
                } else {
                    if (kmBitmap != null) {
                        kmBitmap = null;
                    }
                    alertDialog.dismiss();
                }

            }
        });

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPreview) {
                    getImagePickOrCamera();
                } else {
                    activity.newProses(new Messagebox.DoubleRunnable() {
                        @Override
                        public void run() {
                            if (kmBitmap != null) {
                                //base64fotoKM[0] = activity.bitmapToBase64(kmBitmap);
                            }
                        }

                        @Override
                        public void runUI() {
                            activity.showSuccess("BERHASIL MENYIMPAN FOTO");
                            alertDialog.dismiss();
                        }
                    });
                }
            }
        });

        if (alertDialog.getWindow() != null)
            alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        alertDialog.show();
    }

    private void uploadFotoKm(final String checkinID) {
        Thread uploadThread = new Thread(new Runnable() {
            Nson response;

            @Override
            public void run() {
                MultipartRequest request = new MultipartRequest(getActivity());
                String name = activity.currentDateTime("ddMMyyyy") + "-" + etNopol.getText().toString() +  "-km.png";

                request.addString("CID", activity.getSetting("CID"));
                request.addString("nopol", etNopol.getText().toString());
                request.addString("jenisFoto", "km");
                request.addString("checkinID", checkinID);
                request.addImageFile("km", name, getBitmapAsByte(kmBitmap));

                response = Nson.readJson(request.execute(AppApplication.getBaseUrlV4(SAVE_IMAGE_CHECKIN)));

            }
        });

        uploadThread.start();
        threadAliveChecker(uploadThread);
    }

    private void threadAliveChecker(final Thread thread) {
        final int[] progress = {0};
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!thread.isAlive()) {
                    notificationUploadFoto(progress[0], true);
                    timer.cancel();
                } else {
                    progress[0] += 10;
                    notificationUploadFoto(progress[0], false);
                }
            }
        }, 500, 500);
    }

    private byte[] getBitmapAsByte(Bitmap bitmap) {
        if (bitmap == null) return new byte[]{};

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        try {
            stream.close();
        } catch (final IOException e) {
            Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.showError(e.getMessage());
                }
            });
            e.printStackTrace();
        }
        return stream.toByteArray();
    }

    private void notificationUploadFoto(int progress, boolean isSuccessfuly) {
        NotificationManager notificationManager = (NotificationManager) Objects.requireNonNull(getContext()).getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationId = 1;
        String channelId = "channel-01";
        @SuppressLint("InlinedApi") int importance = NotificationManager.IMPORTANCE_HIGH;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, "Upload Foto Kondisi", importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getContext(), channelId)
                .setOngoing(true)
                .setSmallIcon(R.drawable.speed)
                .setContentTitle("Upload Foto")
                .setContentText("Mengupload Foto")
                .setProgress(100, progress, false)
                .setPriority(Notification.PRIORITY_LOW);

        notificationManager.notify(notificationId, mBuilder.build());
        if(isSuccessfuly){
            notificationManager.cancel(notificationId);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CHECKIN) {
            Objects.requireNonNull(getActivity()).setResult(Activity.RESULT_OK);
            getActivity().finish();
        } else if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_BARCODE) {
            assert data != null;
            String antrian = data.getStringExtra("TEXT");
            getDataBarcode(antrian);
            String result = data.getStringExtra("FORMAT");
        } else if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_NEW_CS) {
            Intent i = new Intent(getActivity(), Checkin3_Activity.class);
            i.putExtra(DATA, Nson.readJson(activity.getIntentStringExtra(data, DATA)).toJson());
            startActivityForResult(i, REQUEST_CHECKIN);
        } else if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_FOTO) {
            if (alertDialog != null && alertDialog.isShowing())
                alertDialog.dismiss();
            try {
                Bundle extras = null;
                Uri imageUri = null;
                if (data != null) {
                    extras = data.getExtras();
                    if (extras == null)
                        imageUri = data.getData();
                }
                getImageUri(imageUri, extras);
            } catch (Exception e) {
                activity.showError(e.getMessage(), Toast.LENGTH_LONG);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            int permissionCamera = ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext()), Manifest.permission.CAMERA);
            if (permissions.length > 0) {
                for (String permission : permissions) {
                    if (permission.equals(CAMERA) && permissionCamera == PackageManager.PERMISSION_DENIED) {
                        activity.showWarning("Ijinkan Aplikasi Untuk Mengakses Kamera di Pengaturan");
                        break;
                    } else if (permission.equals(CAMERA) && permissionCamera == PackageManager.PERMISSION_GRANTED) {
                        getImagePickOrCamera();
                    }
                }
            }
        }
    }
}
