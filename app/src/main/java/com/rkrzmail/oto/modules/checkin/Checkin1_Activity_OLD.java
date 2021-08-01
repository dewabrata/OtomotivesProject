package com.rkrzmail.oto.modules.checkin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
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
import android.widget.LinearLayout;
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
import com.rkrzmail.oto.modules.bengkel.Billing_Activity;
import com.rkrzmail.srv.NikitaAutoComplete;
import com.rkrzmail.oto.modules.Adapter.NsonAutoCompleteAdapter;
import com.rkrzmail.utils.Tools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
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

public class Checkin1_Activity_OLD extends AppActivity implements View.OnClickListener {

    private static final String TAG = "CHECKIN1___";
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkin1);
        if (!Tools.isNetworkAvailable(getActivity())) {
            showWarning("TIDAK ADA KONEKSI INTERNET", Toast.LENGTH_LONG);
        }

        if (getSetting("STATUS_BENGKEL").equals("BATAL") || getSetting("STATUS_BENGKEL").equals("NON AKTIVE")) {
            Intent intent = new Intent(getActivity(), Billing_Activity.class);
            showNotification(getActivity(), "Billing", "Billing Belum Terbayar", "CHECKIN", intent);
            showWarning("ANDA TIDAK BISA MELAKUKAN TRANSAKSI!");
            Tools.setViewAndChildrenEnabled(find(R.id.ly_checkin1, LinearLayout.class), false);
        }

        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Check-In");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        etNopol = findViewById(R.id.et_nopol_checkin1);
        etJenisKendaraan = findViewById(R.id.et_jenisKendaraan_checkin1);
        etNoPonsel = findViewById(R.id.et_noPonsel_checkin1);
        etNamaPelanggan = findViewById(R.id.et_namaPelanggan_checkin1);
        etKm = findViewById(R.id.et_km_checkin1);
        spPekerjaan = findViewById(R.id.sp_pekerjaan_checkin1);

        find(R.id.imgBarcode_checkin1, ImageButton.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), BarcodeActivity.class);
                startActivityForResult(i, REQUEST_BARCODE);
            }
        });

        find(R.id.btn_foto_km).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (kmBitmap == null) {
                    getImagePickOrCamera();
                } else {
                    showDialogPreviewFoto(true);
                }

            }
        });

        find(R.id.img_clear_jenis_kendaraan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etJenisKendaraan.setText("");
            }
        });

        setSpinnerFromApi(spPekerjaan, "nama", "PEKERJAAN", "viewmst", "PEKERJAAN");
        initAutoCompleteNopol();
        initAutoCompleteNamaPelanggan();
        initAutoCompleteKendaraan();
        initOnTextChange();

        find(R.id.btn_lanjut_checkin1).setOnClickListener(this);
        find(R.id.btn_history_checkin1).setOnClickListener(this);
        find(R.id.img_clear, ImageButton.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etNamaPelanggan.setText("");
                etNoPonsel.setText("");
            }
        });
    }

    private void getImagePickOrCamera() {
        if (ContextCompat.checkSelfPermission(getActivity(), CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{WRITE_EXTERNAL_STORAGE
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
                    if (result.size() > 0) {
                        find(R.id.btn_history_checkin1).setEnabled(true);
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
            int prevLength = 0;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                prevLength = s.length();
                Log.d("length__", "beforeTextChanged: " + prevLength);
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
                if (counting == 0) return;
                if (counting < 4 && !etNoPonsel.getText().toString().contains("+62 ")) {
                    etNoPonsel.setTag(null);
                    etNoPonsel.setText("+62 ");
                    Selection.setSelection(etNoPonsel.getText(), etNoPonsel.getText().length());
                } else if (counting < 12) {
                    find(R.id.tl_nohp, TextInputLayout.class).setError("No. Hp Min. 6 Karakter");
                    etNoPonsel.requestFocus();
                } else {
                    find(R.id.tl_nohp, TextInputLayout.class).setErrorEnabled(false);
                }

                if (prevLength > s.length()) {
                    isNoHp = false;
                }

                validateNoPonsel(formatOnlyNumber(etNoPonsel.getText().toString()));
                etNoPonsel.addTextChangedListener(this);
            }
        });
    }

    private void validateNoPonsel(@NonNull final String noPonsel) {
        if (noPonsel.length() > 10) {
            find(R.id.pb_etNoPonsel_checkin).setVisibility(View.VISIBLE);
        } else {
            return;
        }
        newTask(new Messagebox.DoubleRunnable() {
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
                find(R.id.pb_etNoPonsel_checkin).setVisibility(View.GONE);
                if (!etNamaPelanggan.getText().toString().isEmpty()) {
                    result = result.get("data").get(0);
                    String dataNama = result.get("NAMA_PELANGGAN").asString();
                    String dataNoponsel = result.get("NO_PONSEL").asString();
                    if (!etNamaPelanggan.getText().toString().equals(dataNama)
                            && noPonsel.equals(dataNoponsel)) {
                        find(R.id.tl_nohp, TextInputLayout.class).setError("NO PONSEL TIDAK VALID DENGAN NAMA PELANGGAN");
                    } else {
                        find(R.id.tl_nohp, TextInputLayout.class).setErrorEnabled(false);
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

        etNamaPelanggan.setLoadingIndicator((android.widget.ProgressBar) findViewById(R.id.pb_namaPelanggan));
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
                find(R.id.et_kode_pos, EditText.class).setText(n.get("KODE_POS").asString());
                find(R.id.et_email, EditText.class).setText(n.get("EMAIL").asString());

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
                etNopol.setText(formatNopol(n.get("NO_POLISI").asString()));
                etNoPonsel.setTag(nomor);
                etNoPonsel.setText("XXXXXXXX" + nomor);
                etNamaPelanggan.setText(n.get("NAMA_PELANGGAN").asString());
                etJenisKendaraan.setText(n.get("JENIS_KENDARAAN").asString());
                getHistoryCheckin(n.get("NO_POLISI").asString());

                setSpinnerFromApi(spPekerjaan, "nama", "PEKERJAAN", "viewmst", "PEKERJAAN", pekerjaan);
                if (n.get("PEMILIK").asString().equalsIgnoreCase("Y")) {
                    find(R.id.cb_pemilik_checkin1, CheckBox.class).setChecked(true);
                }

                //etJenisKendaraan.setEnabled(merkKendaraan.isEmpty() || varianKendaraan.isEmpty());
                find(R.id.tl_nohp, TextInputLayout.class).setErrorEnabled(false);
                find(R.id.img_clear, ImageButton.class).setVisibility(View.GONE);
            }
        });

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
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

                kendaraanId = n.get("ID").asInteger();
                merkKendaraan = n.get("MERK").asString();
                varianKendaraan = n.get("VARIAN").asString();
                Checkin1_Activity_OLD.this.kendaraan = n.get("TYPE").asString();
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

    private void setDefaultKendaraan() {
        kendaraanId = 0;
        merkKendaraan = "";
        varianKendaraan = "";
        Checkin1_Activity_OLD.this.kendaraan = "";
        modelKendaraan = "";
        tahunProduksi = "";
        jenisKendaraan = "";
    }

    private void setSelanjutnya() {
        if (!Tools.isNetworkAvailable(getActivity())) {
            showWarning("TIDAK ADA KONEKSI INTERNET", Toast.LENGTH_LONG);
            return;
        }
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
                args.put("noPonsel", isNoHp && etNoPonsel.getTag() != null ? noHp : formatOnlyNumber(etNoPonsel.getText().toString()));
                args.put("nama", Tools.isSingleQuote(namaPelanggan));
                args.put("isPemilik", pemilik);//dateKendaraan
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
                    nson.set("isExpiredKm", (Integer.parseInt(formatOnlyNumber(etKm.getText().toString())) - kmCheckinBefore) > expiredGaransiKm);
                    nson.set("isExpiredHari", totalHariCheckin > expiredGaransiHari);
                    nson.set("expiredKmVal", expiredGaransiKm);
                    nson.set("expiredHariVal", expiredGaransiHari);
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
                    showWarning(result.get("message").asString());
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CHECKIN) {
            setResult(RESULT_OK);
            finish();
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_BARCODE) {
            assert data != null;
            String antrian = data.getStringExtra("TEXT");
            getDataBarcode(antrian);
            String result = data.getStringExtra("FORMAT");
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_NEW_CS) {
            Intent i = new Intent(getActivity(), Checkin3_Activity.class);
            i.putExtra(DATA, Nson.readJson(getIntentStringExtra(data, DATA)).toJson());
            startActivityForResult(i, REQUEST_CHECKIN);
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_FOTO) {
            if (alertDialog != null && alertDialog.isShowing())
                alertDialog.dismiss();
            Bundle extras = null;
            Uri imageUri = null;
            if (data != null) {
                extras = data.getExtras();
                if (extras == null)
                    imageUri = data.getData();
            }

            try {
                getImageUri(imageUri, extras);
            } catch (Exception e) {
                showError(e.getMessage(), Toast.LENGTH_LONG);
            }
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_lanjut_checkin1:
                if (etNopol.getText().toString().isEmpty()) {
                    etNopol.setError("Harus Di Isi");
                    etNopol.requestFocus();
                } else if (etJenisKendaraan.getText().toString().isEmpty() || etJenisKendaraan.getText().toString().equals(" ") || varianKendaraan.isEmpty()) {
                    etJenisKendaraan.setError("Jenis Kendaraan Belum Lengkap");
                    viewFocus(etJenisKendaraan);
                } else if (kendaraanId == 0) {
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
                    if (!etKm.getText().toString().isEmpty() && kmBitmap == null) {
                        showWarning("PENGISIAN KM HARUS MENYERTAKAN FOTO", Toast.LENGTH_LONG);
                    } else {
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
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            int permissionCamera = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA);
            if (permissions.length > 0) {
                for (String permission : permissions) {
                    if (permission.equals(CAMERA) && permissionCamera == PackageManager.PERMISSION_DENIED) {
                        showWarning("Ijinkan Aplikasi Untuk Mengakses Kamera di Pengaturan");
                        break;
                    } else if (permission.equals(CAMERA) && permissionCamera == PackageManager.PERMISSION_GRANTED) {
                        getImagePickOrCamera();
                    }
                }
            }
        }
    }

    private void getImageUri(final Uri imageUri, final Bundle imgBundle) {
        newProses(new Messagebox.DoubleRunnable() {
            @Override
            public void run() {
                if (imageUri != null) {
                    try {
                        kmBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    kmBitmap = (Bitmap) (imgBundle != null ? imgBundle.get("data") : null);
                }

                if (kmBitmap != null) {
                    tempFile = new File(Objects.requireNonNull(getActivity()).getCacheDir(), "km.png");
                    try {
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();

                        kmBitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
                        byte[] bitmapdata = bos.toByteArray();
                        FileOutputStream fos = new FileOutputStream(tempFile);
                        fos.write(bitmapdata);
                        fos.flush();
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void runUI() {
                showDialogPreviewFoto(false);
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

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle("Preview KM");

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
                    if (kmBitmap != null) {
                        base64fotoKM[0] = bitmapToBase64(kmBitmap);
                    }
                    showSuccess("BERHASIL MENYIMPAN FOTO");
                    alertDialog.dismiss();
                }
            }
        });

        if (alertDialog.getWindow() != null)
            alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        alertDialog.show();
    }


}
