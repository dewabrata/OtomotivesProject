package com.rkrzmail.oto.modules.mekanik;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.sparepart.CariPart_Activity;
import com.rkrzmail.utils.FileUtility;
import com.rkrzmail.utils.Tools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.rkrzmail.utils.APIUrls.SET_CLAIM;
import static com.rkrzmail.utils.APIUrls.VIEW_MST;

import static com.rkrzmail.utils.ConstUtils.CARI_PART_CLAIM;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.PART;
import static com.rkrzmail.utils.ConstUtils.PERMISSION_REQUEST_CODE;
import static com.rkrzmail.utils.ConstUtils.REQUEST_CARI_PART;
import static com.rkrzmail.utils.ConstUtils.REQUEST_FOTO;
import static com.rkrzmail.utils.ConstUtils.REQUEST_FOTO_KTP;
import static com.rkrzmail.utils.ConstUtils.REQUEST_FOTO_PART;
import static com.rkrzmail.utils.ConstUtils.REQUEST_FOTO_STNK;

public class LkkClaimMekanik_Activity extends AppActivity {


    private Spinner sp_tipekerusakan, sp_kondisipelanggan, sp_kondisipart, sp_sebabkerusakan, sp_tindakan;
    private EditText et_namapart, et_nopart, et_merkpart, et_nikpemilik, et_nobuku, et_desc, et_info;
    private Button btn_fotostnk, btn_fotoktp;
    private CheckBox cbClaim;
    private AlertDialog alertDialog;
    private Bitmap bitmapPart, bitmapKtp, bitmapStnk;
    private File tempFileStnk, tempFileKtp, tempFilePart;

    private final String[] fotoPart = {""};
    private final String[] fotoStnk = {""};
    private final String[] fotoKtp = {""};
    private final Nson dataSebabList = Nson.newArray();
    private final Nson SebabArray = Nson.newArray();
    private Nson data;
    private String idpart = "", stockBengkel = "", idCheckin = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lkk_claim_mekanik);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("LKK CLAIM");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        et_desc = findViewById(R.id.et_desc_kerusakan);
        et_info = findViewById(R.id.et_info_tambahan);
        et_merkpart = findViewById(R.id.et_merkpart_lkkclaim);
        et_namapart = findViewById(R.id.et_namapart_lkkclaim);
        et_nopart = findViewById(R.id.et_nopart_lkkclaim);
        et_nobuku = findViewById(R.id.et_no_bukugaransi);
        et_nikpemilik = findViewById(R.id.et_nik_pemilik);
        btn_fotoktp = findViewById(R.id.btn_foto_ktp);
        btn_fotostnk = findViewById(R.id.btn_foto_stnk);
        sp_tipekerusakan = findViewById(R.id.sp_tipe_kerusakan);
        sp_kondisipelanggan = findViewById(R.id.sp_kondisi_pelanggan);
        sp_kondisipart = findViewById(R.id.sp_kondisipart_lkkclaim);
        sp_sebabkerusakan = findViewById(R.id.sp_sebab_kerusakan);
        sp_tindakan = findViewById(R.id.sp_tindakan_perbaikan);
        cbClaim = (CheckBox) findViewById(R.id.cb_claimgaransi);

        setSpinnerFromApi(sp_kondisipart, "nama", "KONDISI PART", "viewmst", "KONDISI");
        setSpSebabKerusakan();
        disableFields();

        data = Nson.readJson(getIntentStringExtra(DATA));
        idCheckin = data.get("IDCHECKIN").asString();
        cbClaim.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isChecked()) {
                    et_nikpemilik.setEnabled(true);
                    et_nobuku.setEnabled(true);
                    btn_fotoktp.setEnabled(true);
                    btn_fotostnk.setEnabled(true);
                } else {
                    et_nikpemilik.setEnabled(false);
                    et_nobuku.setEnabled(false);
                    btn_fotoktp.setEnabled(false);
                    btn_fotostnk.setEnabled(false);
                }
            }
        });

        find(R.id.btn_simpan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fotoPart[0] = bitmapToBase64(bitmapPart);
                fotoKtp[0] = bitmapToBase64(bitmapKtp);
                fotoStnk[0] = bitmapToBase64(bitmapStnk);

                if (et_desc.getText().toString().isEmpty()) {
                    showWarning("DESKRIPSI KERUSAKAN TIDAK BOLEH KOSONG");
                } else if (et_info.getText().toString().isEmpty()) {
                    showWarning("INFO TAMBAHAN TIDAK BOLEH KOSONG");
                } else if (et_namapart.getText().toString().isEmpty()) {
                    showWarning("NAMA PART TIDAK BOLEH KOSONG");
                } else if (et_nopart.getText().toString().isEmpty()) {
                    showWarning("NO PART TIDAK BOLEH KOSONG");
                } else if (et_merkpart.getText().toString().isEmpty()) {
                    showWarning("MERK PART TIDAK BOLEH KOSONG");
                } else if (sp_kondisipart.getSelectedItem().toString().equalsIgnoreCase("--PILIH--")) {
                    showWarning("KONDISI PART TIDAK BOLEH KOSONG");
                } else if (sp_tipekerusakan.getSelectedItem().toString().equalsIgnoreCase("--PILIH--")) {
                    showWarning("TIPE KERUSAKAN TIDAK BOLEH KOSONG");
                } else if (sp_kondisipelanggan.getSelectedItem().toString().equalsIgnoreCase("--PILIH--")) {
                    showWarning("KONDISI PELANGGAN TIDAK BOLEH KOSONG");
                } else if (sp_sebabkerusakan.getSelectedItem().toString().equalsIgnoreCase("--PILIH--")) {
                    showWarning("SEBAB KERUSAKAN TIDAK BOLEH KOSONG");
                } else if (cbClaim.isChecked() && et_nikpemilik.getText().toString().isEmpty()) {
                    showWarning("NIK TIDAK BOLEH KOSONG");
                } else if (fotoKtp[0].isEmpty() && btn_fotoktp.isEnabled()) {
                    showWarning("FOTO KTP HARUS DI MASUKKAN");
                } else if (fotoPart[0].isEmpty() && find(R.id.btn_foto_part, Button.class).isEnabled()) {
                    showWarning("FOTO PART HARUS DI MASUKKAN");
                } else if (fotoStnk[0].isEmpty() && btn_fotostnk.isEnabled()) {
                    showWarning("FOTO STNK HARUS DI MASUKKAN");
                } else {
                    SimpanData();
                }

            }
        });

        findViewById(R.id.btn_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), CariPart_Activity.class);
                i.putExtra(CARI_PART_CLAIM, idCheckin);
                startActivityForResult(i, REQUEST_CARI_PART);
            }
        });

        find(R.id.btn_foto_part, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (find(R.id.btn_foto_part, Button.class).getText().toString().equals("FOTO PART")) {
                    getImagePickOrCamera(REQUEST_FOTO_PART);
                } else {
                    showDialogPreviewFoto(bitmapPart, "Foto Part", fotoPart, true);
                }

            }
        });

        btn_fotostnk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btn_fotostnk.getText().toString().equals("FOTO STNK")) {
                    getImagePickOrCamera(REQUEST_FOTO_STNK);
                } else {
                    showDialogPreviewFoto(bitmapStnk, "Foto STNK", fotoStnk, true);
                }

            }
        });

        btn_fotoktp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btn_fotoktp.getText().toString().equals("FOTO KTP")) {
                    getImagePickOrCamera(REQUEST_FOTO_KTP);
                } else {
                    showDialogPreviewFoto(bitmapKtp, "Foto KTP", fotoKtp, true);
                }

            }
        });
    }

    private void getImagePickOrCamera(int requestCode) {
        if (ContextCompat.checkSelfPermission(getActivity(), CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{WRITE_EXTERNAL_STORAGE
                    , READ_EXTERNAL_STORAGE, CAMERA}, PERMISSION_REQUEST_CODE);
        } else {
            final List<Intent> intents = new ArrayList<>();
            intents.add(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
            intents.add(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));

            Intent result = Intent.createChooser(intents.remove(0), null);
            result.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toArray(new Parcelable[]{}));
            startActivityForResult(result, requestCode);
        }
    }


    private void setSpSebabKerusakan() {
        final Nson data = Nson.readJson(getIntentStringExtra(DATA));
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "view");
                args.put("nama", "SEBAB KERUSAKAN");
                args.put("status", "AKTIF");
                args.put("sebab kerusakan", data.get("sebab kerusakan").asString());
                args.put("claim", data.get("claim").asString());
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_MST), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data");
                    dataSebabList.asArray().addAll(result.asArray());
                    SebabArray.add("--PILIH--");
                    for (int i = 0; i < result.size(); i++) {
                        SebabArray.add(result.get(i).get("SEBAB_KERUSAKAN").asString());
                    }
                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, SebabArray.asArray());
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    sp_sebabkerusakan.setAdapter(spinnerAdapter);
                } else {
                    showInfo("Sebab Kerusakan Gagal Di Muat");
                    setSpSebabKerusakan();
                }
            }
        });

        sp_sebabkerusakan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getSelectedItem().toString();
                if (item.equalsIgnoreCase("--PILIH--")) {
                    cbClaim.setChecked(false);
                }

                for (int i = 0; i < dataSebabList.size(); i++) {
                    if (dataSebabList.get(i).get("SEBAB_KERUSAKAN").asString().equalsIgnoreCase(item)) {
                        if (dataSebabList.get(i).get("CLAIM").asString().equals("Y")) {
                            cbClaim.setChecked(true);
                            cbClaim.setEnabled(true);
                        } else {
                            cbClaim.setChecked(false);
                            cbClaim.setEnabled(false);
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void showDialogPreviewFoto(final Bitmap bitmap, String toolbarTittle, final String[] base64, final boolean isPreview) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_alert_camera, null);
        builder.setView(dialogView);

        Toolbar toolbar = dialogView.findViewById(R.id.toolbar);
        ImageView img = (ImageView) dialogView.findViewById(R.id.img_alert_foto);
        Button btnCancel = dialogView.findViewById(R.id.btn_alert_cancel);
        Button btnSimpan = dialogView.findViewById(R.id.btn_alert_save);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(toolbarTittle);

        if (bitmap != null)
            img.setImageBitmap(bitmap);

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
                    if (bitmap == bitmapPart) {
                        bitmapPart = null;
                    } else if (bitmap == bitmapStnk) {
                        bitmapStnk = null;
                    } else if (bitmap == bitmapKtp) {
                        bitmapStnk = null;
                    }
                    alertDialog.dismiss();
                }

            }
        });

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //fileFoto[0] = SaveImage(bitmapPart, "PART");
                //base64[0] = FileUtility.encodeToStringBase64(fileFoto[0].getAbsolutePath());
                if (isPreview) {
                    if (bitmap == bitmapPart) {
                        getImagePickOrCamera(REQUEST_FOTO_PART);
                    } else if (bitmap == bitmapStnk) {
                        getImagePickOrCamera(REQUEST_FOTO_STNK);
                    } else if (bitmap == bitmapKtp) {
                        getImagePickOrCamera(REQUEST_FOTO_KTP);
                    }
                } else {
                    if (bitmap != null && bitmap == bitmapPart) {
                        find(R.id.btn_foto_part, Button.class).setText("PREVIEW FOTO PART");
                    } else if (bitmap != null && bitmap == bitmapStnk) {
                        btn_fotostnk.setText("PREVIEW FOTO STNK");
                    } else if (bitmap != null && bitmap == bitmapKtp) {
                        btn_fotoktp.setText("PREVIEW FOTO KTP");
                    }

                    base64[0] = bitmapToBase64(bitmap);
                    showInfo("SUKSES MENYIMPAN FOTO");
                    alertDialog.dismiss();
                }
            }
        });

        alertDialog = builder.create();
        if (alertDialog.getWindow() != null)
            alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        alertDialog.show();
    }

    private void SimpanData() {
        final String nik = et_nikpemilik.getText().toString().toUpperCase();
        final String descKerusakan = et_desc.getText().toString().toUpperCase();
        final String infoTambahan = find(R.id.et_info_tambahan, EditText.class).getText().toString().toUpperCase();
        final String tipeKerusakan = sp_tipekerusakan.getSelectedItem().toString().toUpperCase();
        final String kondisiPelanggan = sp_kondisipelanggan.getSelectedItem().toString().toUpperCase();
        final String kondisiPart = sp_kondisipart.getSelectedItem().toString().toUpperCase();
        final String sebabKerusakan = sp_sebabkerusakan.getSelectedItem().toString().toUpperCase();
        final String tindakan = sp_tindakan.getSelectedItem().toString().toUpperCase();
        final String claim = find(R.id.cb_claimgaransi, CheckBox.class).isChecked() ? "Y" : "N";


        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action ", "add");
                args.put("kategori", "");
                args.put("tanggal_checkin", data.get("TANGGAL_CHECKIN").asString());
                args.put("tanggal_lapor", currentDateTime("yyyy-MM-dd HH:mm:ss"));
                args.put("nama_mekanik", data.get("NAMA_MEKANIK").asString());
                args.put("noPolisi", data.get("NOPOL").asString());
                args.put("km", data.get("KM").asString());
                args.put("merk", data.get("MERK").asString());
                args.put("varian", data.get("VARIAN").asString());
                args.put("kodeTipe", data.get("KODE_TIPE").asString());
                args.put("tahunProduksi", data.get("TAHUN_PRODUKSI").asString());
                args.put("tanggalPembelian", data.get("TANGGAL_CHECKIN").asString());
                args.put("deskripsiPerusakan", descKerusakan);
                args.put("tipeKerusakan", tipeKerusakan);
                args.put("kondisiPelanggan", kondisiPelanggan);
                args.put("idPartRusak", idpart);
                args.put("kondisiPart", kondisiPart);
                args.put("sebabKerusakan", sebabKerusakan);
                args.put("tindakanPerbaikan", tindakan);
                args.put("claimGaransi", claim);
                args.put("informasiTambahan", infoTambahan);
                args.put("nik", nik);
                args.put("linkFotoPart", fotoPart[0]);
                args.put("linkFotoKtp", fotoKtp[0]);
                args.put("linkFotoStnk", fotoStnk[0]);
                args.put("stockPart", stockBengkel);
                args.put("kerusakanSamaBulan3 ", "");
                args.put("kerusakanSamaBulan2", "");
                args.put("kerusakanSamaBulan1", "");
                args.put("partTerpakaiBulan3", "");
                args.put("partTerpakaiBulan2", "");
                args.put("partTerpakaiBulan1 ", "");
                args.put("checkinID", getIntentStringExtra("CHECKIN_ID"));

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(SET_CLAIM), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("SUKSES MENYIMPAN PART GARANSI");
                    Intent intent = new Intent();
                    intent.putExtra("IS_GARANSI_SUCCESS", true);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    showError(result.get("message").asString());
                }
            }
        });
    }

    private void disableFields() {
        et_merkpart.setEnabled(false);
        et_nopart.setEnabled(false);
        et_namapart.setEnabled(false);
        et_nikpemilik.setEnabled(false);
        et_nobuku.setEnabled(false);
        cbClaim.setChecked(false);
        cbClaim.setEnabled(false);
        btn_fotoktp.setEnabled(false);
        btn_fotostnk.setEnabled(false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bundle extras = null;
        if (data != null) {
            extras = data.getExtras();
        }
        Uri imageUri = null;
        if (data != null) {
            extras = data.getExtras();
            if (extras == null)
                imageUri = data.getData();
        }

        if (resultCode == RESULT_OK && requestCode == REQUEST_CARI_PART) {
            Nson nson = Nson.readJson(getIntentStringExtra(data, PART));
            if (nson.get("GARANSI_LAYANAN").asString().equals("Y")) {
                disableFields();
                et_merkpart.setText(nson.get("MERK").asString());
                et_nopart.setText(nson.get("NO_PART").asString());
                et_namapart.setText(nson.get("NAMA_PART").asString());
                idpart = nson.get("PART_ID").asString();
                stockBengkel = nson.get("STOCK").asString();
            } else {
                cbClaim.setChecked(false);
                cbClaim.setEnabled(false);
                showWarning("BUKAN PART GARANSI LAYANAN");
            }
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_FOTO_PART) {
            getImageUri("PART", imageUri, extras);
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_FOTO_STNK) {
            getImageUri("STNK", imageUri, extras);
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_FOTO_KTP) {
            getImageUri("KTP", imageUri, extras);
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
                        showSuccess("Aplikasi di Ijinkan Mengakses Kamera");
                        break;
                    }
                }
            }
        }
    }

    private void getImageUri(final String flagFoto, final Uri imageUri, final Bundle imgBundle) {
        newProses(new Messagebox.DoubleRunnable() {
            @Override
            public void run() {
                if (imageUri != null) {
                    try {
                        switch (flagFoto) {
                            case "PART":
                                bitmapPart = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                                break;
                            case "STNK":
                                bitmapStnk = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                                break;
                            case "KTP":
                                bitmapKtp = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                                break;
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    switch (flagFoto) {
                        case "PART":
                            bitmapPart = (Bitmap) (imgBundle != null ? imgBundle.get("data") : null);
                            break;
                        case "STNK":
                            bitmapStnk = (Bitmap) (imgBundle != null ? imgBundle.get("data") : null);
                            break;
                        case "KTP":
                            bitmapKtp = (Bitmap) (imgBundle != null ? imgBundle.get("data") : null);
                            break;
                    }
                }

                try {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    FileOutputStream fos = null;
                    switch (flagFoto) {
                        case "PART":
                            tempFilePart = new File(Objects.requireNonNull(getActivity()).getCacheDir(), "part.png");
                            bitmapPart.compress(Bitmap.CompressFormat.PNG, 100, bos);
                            fos = new FileOutputStream(tempFilePart);
                            break;
                        case "STNK":
                            tempFileStnk = new File(Objects.requireNonNull(getActivity()).getCacheDir(), "stmk.png");
                            bitmapStnk.compress(Bitmap.CompressFormat.PNG, 100, bos);
                            fos = new FileOutputStream(tempFileStnk);
                            break;
                        case "KTP":
                            tempFileKtp = new File(Objects.requireNonNull(getActivity()).getCacheDir(), "ktp.png");
                            bitmapKtp.compress(Bitmap.CompressFormat.PNG, 100, bos);
                            fos = new FileOutputStream(tempFileKtp);
                            break;
                    }

                    byte[] bitmapdata = bos.toByteArray();
                    fos.write(bitmapdata);
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void runUI() {
                switch (flagFoto) {
                    case "PART":
                        showDialogPreviewFoto(bitmapPart, "Foto Part", fotoPart, false);
                        break;
                    case "STNK":
                        showDialogPreviewFoto(bitmapStnk, "Foto STNK", fotoStnk, false);
                        break;
                    case "KTP":
                        showDialogPreviewFoto(bitmapKtp, "Foto KTP", fotoKtp, false);
                        break;
                }
            }
        });
    }

}

