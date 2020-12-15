package com.rkrzmail.oto.modules.mekanik;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.checkin.DetailKontrolLayanan_Activity;
import com.rkrzmail.oto.modules.checkin.HistoryBookingCheckin_Activity;
import com.rkrzmail.oto.modules.sparepart.CariPart_Activity;
import com.rkrzmail.oto.modules.sparepart.JumlahPart_JualPart_Activity;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.utils.FileUtility;
import com.rkrzmail.utils.Tools;
import com.valdesekamdem.library.mdtoast.MDToast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.ATUR_JUAL_PART;
import static com.rkrzmail.utils.APIUrls.SET_CHECKIN;
import static com.rkrzmail.utils.APIUrls.SET_CLAIM;
import static com.rkrzmail.utils.APIUrls.VIEW_KONTROL_LAYANAN;
import static com.rkrzmail.utils.APIUrls.VIEW_LAYANAN;
import static com.rkrzmail.utils.APIUrls.VIEW_MST;
import static com.rkrzmail.utils.APIUrls.VIEW_PERINTAH_KERJA_MEKANIK;
import static com.rkrzmail.utils.ConstUtils.CARI_PART_BENGKEL;

import static com.rkrzmail.utils.ConstUtils.CARI_PART_CLAIM;
import static com.rkrzmail.utils.ConstUtils.CARI_PART_LOKASI;
import static com.rkrzmail.utils.ConstUtils.CARI_PART_OTOMOTIVES;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.GARANSI_PART;
import static com.rkrzmail.utils.ConstUtils.PART;
import static com.rkrzmail.utils.ConstUtils.REQUEST_CARI_PART;
import static com.rkrzmail.utils.ConstUtils.REQUEST_DETAIL;
import static com.rkrzmail.utils.ConstUtils.MASTER_PART;
import static com.rkrzmail.utils.ConstUtils.REQUEST_FOTO_KTP;
import static com.rkrzmail.utils.ConstUtils.REQUEST_FOTO_PART;
import static com.rkrzmail.utils.ConstUtils.REQUEST_FOTO_STNK;
import static com.rkrzmail.utils.ConstUtils.RUANG_PART;

public class LkkClaimMekanik_Activity extends AppActivity {


    private Spinner sp_tipekerusakan,sp_kondisipelanggan,sp_kondisipart,sp_sebabkerusakan,sp_tindakan;
    private EditText et_namapart, et_nopart, et_merkpart, et_nikpemilik, et_nobuku, et_desc, et_info;
    private Button btn_fotostnk, btn_fotoktp, btn_simpan;
    private CheckBox cbClaim;
    private Nson dataSebabList = Nson.newArray(), SebabArray = Nson.newArray();
    private String idpart = "", stockBengkel = "", fotoPart="", fotoStnk="", fotoKtp="";
    private File fileStnk, fileKtp, filePart;
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
        sp_tipekerusakan =  findViewById(R.id.sp_tipe_kerusakan);
        sp_kondisipelanggan =  findViewById(R.id.sp_kondisi_pelanggan);
        sp_kondisipart =  findViewById(R.id.sp_kondisipart_lkkclaim);
        sp_sebabkerusakan =  findViewById(R.id.sp_sebab_kerusakan);
        sp_tindakan =  findViewById(R.id.sp_tindakan_perbaikan);
        btn_simpan = findViewById(R.id.btn_simpan_lkkclaim);
        cbClaim = (CheckBox) findViewById(R.id.cb_claimgaransi);

        setSpinnerFromApi(sp_kondisipart, "nama", "KONDISI PART", "viewmst", "KONDISI");
        setSpSebabKerusakan();

        cbClaim.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(compoundButton.isChecked()){
                    et_nikpemilik.setEnabled(true);
                    et_nobuku.setEnabled(true);
                    btn_fotoktp.setEnabled(true);
                    btn_fotostnk.setEnabled(true);
                }else {
                    et_nikpemilik.setEnabled(false);
                    et_nobuku.setEnabled(false);
                    btn_fotoktp.setEnabled(false);
                    btn_fotostnk.setEnabled(false);
                }
            }
        });

        findViewById(R.id.btn_simpan_lkkclaim).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(et_desc.getText().toString().isEmpty()){
                    showWarning("DESKRIPSI KERUSAKAN TIDAK BOLEH KOSONG");
                }else if (et_info.getText().toString().isEmpty()){
                    showWarning("INFO TAMBAHAN TIDAK BOLEH KOSONG");
                }else if (et_namapart.getText().toString().isEmpty()){
                    showWarning("NAMA PART TIDAK BOLEH KOSONG");
                }else if (et_nopart.getText().toString().isEmpty()){
                    showWarning("NO PART TIDAK BOLEH KOSONG");
                }else if (et_merkpart.getText().toString().isEmpty()){
                    showWarning("MERK PART TIDAK BOLEH KOSONG");
                }else if(sp_kondisipart.getSelectedItem().toString().equalsIgnoreCase("--PILIH--")){
                    showWarning("KONDISI PART TIDAK BOLEH KOSONG");
                }else if(sp_tipekerusakan.getSelectedItem().toString().equalsIgnoreCase("--PILIH--")) {
                    showWarning("TIPE KERUSAKAN TIDAK BOLEH KOSONG");
                }else if(sp_kondisipelanggan.getSelectedItem().toString().equalsIgnoreCase("--PILIH--")) {
                    showWarning("KONDISI PELANGGAN TIDAK BOLEH KOSONG");
                }else if(sp_sebabkerusakan.getSelectedItem().toString().equalsIgnoreCase("--PILIH--")) {
                    showWarning("SEBAB KERUSAKAN TIDAK BOLEH KOSONG");
                }else if(cbClaim.isChecked() && et_nikpemilik.getText().toString().isEmpty()) {
                    showWarning("NIK TIDAK BOLEH KOSONG");
                }else {
                    SimpanData();
                }
            }
        });

        findViewById(R.id.btn_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), CariPart_Activity.class);
                i.putExtra(CARI_PART_CLAIM, GARANSI_PART);
                startActivityForResult(i, REQUEST_CARI_PART);
            }
        });

        find(R.id.btn_foto_part, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFotoPart();
            }
        });
        find(R.id.btn_foto_stnk, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFotoStnk();
            }
        });
        find(R.id.btn_foto_ktp, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFotoKtp();
            }
        });
    }

    private void setFotoPart(){
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (i.resolveActivity(getPackageManager()) != null) {
             filePart= null;
            try {
                filePart = createImageFile();
            } catch (IOException ex) {

            }
            if (filePart != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.rkrzmail.oto.fileprovider",
                        filePart);
                i.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(i, REQUEST_FOTO_PART);
            }
        }
    }

    private void setFotoStnk(){
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (i.resolveActivity(getPackageManager()) != null) {
            fileStnk= null;
            try {
                fileStnk = createImageFile();
            } catch (IOException ex) {

            }
            if (fileStnk != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.rkrzmail.oto.fileprovider",
                        fileStnk);
                i.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(i, REQUEST_FOTO_STNK);
            }
        }
    }

    private void setFotoKtp(){
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (i.resolveActivity(getPackageManager()) != null) {
            fileKtp= null;
            try {
                fileKtp = createImageFile();
            } catch (IOException ex) {

            }
            if (fileKtp != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.rkrzmail.oto.fileprovider",
                        fileKtp);
                i.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(i, REQUEST_FOTO_KTP);
            }
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

    private void SimpanData(){
        final String namaPart = et_namapart.getText().toString().toUpperCase();
        final String nik = et_nikpemilik.getText().toString().toUpperCase();
        final String descKerusakan = et_desc.getText().toString().toUpperCase();
        final String infoTambahan = find(R.id.et_info_tambahan,EditText.class).getText().toString().toUpperCase();
        final String tipeKerusakan = sp_tipekerusakan.getSelectedItem().toString().toUpperCase();
        final String kondisiPelanggan = sp_kondisipelanggan.getSelectedItem().toString().toUpperCase();
        final String kondisiPart = sp_kondisipart.getSelectedItem().toString().toUpperCase();
        final String sebabKerusakan = sp_sebabkerusakan.getSelectedItem().toString().toUpperCase();
        final String tindakan = sp_tindakan.getSelectedItem().toString().toUpperCase();
        final String claim = find(R.id.cb_claimgaransi, CheckBox.class).isChecked() ? "Y" : "N";

        final Nson data = Nson.readJson(getIntentStringExtra(DATA));
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action ", "add");
                args.put("tanggal_checkin", data.get("TANGGAL_CHECKIN").asString());
                args.put("tanggal_lapor", currentDateTime("yyyy-MM-dd HH:mm:ss"));
                args.put("nama_mekanik",data.get("NAMA_MEKANIK").asString());
                args.put("noPolisi", data.get("NOPOL").asString());
                args.put("km", data.get("KM").asString());
                args.put("merk", data.get("MERK").asString());
                args.put("varian",data.get("VARIAN").asString());
                args.put("kodeTipe", data.get("KODE_TIPE").asString());
                args.put("tahunProduksi",data.get("TAHUN_PRODUKSI").asString());
                args.put("tanggalPembelian",data.get("TANGGAL_CHECKIN").asString());
                args.put("deskripsiPerusakan",descKerusakan);
                args.put("tipeKerusakan", tipeKerusakan);
                args.put("kondisiPelanggan", kondisiPelanggan);
                args.put("idPartRusak", idpart);
                args.put("kondisiPart", kondisiPart);
                args.put("sebabKerusakan", sebabKerusakan);
                args.put("tindakanPerbaikan",tindakan);
                args.put("claimGaransi",claim);
                args.put("informasiTambahan",infoTambahan);
                args.put("nik",nik);
                args.put("linkFotoPart",fotoPart);
                args.put("linkFotoKtp",fotoKtp);
                args.put("linkFotoStnk",fotoStnk);
                args.put("stockPart",stockBengkel);
                args.put("kerusakanSamaBulan3 ","");
                args.put("kerusakanSamaBulan2","");
                args.put("kerusakanSamaBulan1", "");
                args.put("partTerpakaiBulan3","");
                args.put("partTerpakaiBulan2","");
                args.put("partTerpakaiBulan1 ","");

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(SET_CLAIM), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showInfo("Sukses Menyimpan Data");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showInfo("Gagagl Menyimpan Data");
                }
            }
        });
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        //currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CARI_PART) {
            Nson nson = Nson.readJson(getIntentStringExtra(data, PART));
            if (nson.get("GARANSI_MASTER_PART_PABRIKAN").asString().equals("Y") && nson.get("GARANSI_PART_KM").asString().equals("Y") && nson.get("GARANSI_PART_BULAN").asString().equals("Y")) {
                cbClaim.setChecked(true);
                cbClaim.setSelected(true);
                cbClaim.setEnabled(true);
            }else {
                cbClaim.setChecked(false);
                cbClaim.setSelected(false);
            }
            et_merkpart.setText(nson.get("MERK").asString());
            et_nopart.setText(nson.get("NO_PART").asString());
            et_namapart.setText(nson.get("NAMA_PART").asString());
            idpart=nson.get("PART_ID").asString();
            stockBengkel=nson.get("STOCK").asString();
        }else if(resultCode == RESULT_OK && requestCode == REQUEST_FOTO_PART){
            Bitmap myBitmap = BitmapFactory.decodeFile(filePart.getAbsolutePath());
            fotoPart = FileUtility.encodeToStringBase64(filePart.getAbsolutePath());
            //find(R.id.testId, ImageView.class).setImageBitmap(myBitmap);
        }else if(resultCode == RESULT_OK && requestCode == REQUEST_FOTO_STNK){
            Bitmap myBitmap = BitmapFactory.decodeFile(fileStnk.getAbsolutePath());
            fotoStnk = FileUtility.encodeToStringBase64(fileStnk.getAbsolutePath());
        }else if(resultCode == RESULT_OK && requestCode == REQUEST_FOTO_KTP){
            Bitmap myBitmap = BitmapFactory.decodeFile(fileKtp.getAbsolutePath());
            fotoKtp = FileUtility.encodeToStringBase64(fileKtp.getAbsolutePath());
        }

    }





    //    private void takephoto() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext()), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()), new String[]{android.Manifest.permission.CAMERA}, 4);
//            } else {
//                dispatchTakePictureIntent();
//            }
//        } else {
//            dispatchTakePictureIntent();
//        }
//    }
//
//    private void dispatchTakePictureIntent() {
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if (takePictureIntent.resolveActivity(Objects.requireNonNull(getContext()).getPackageManager()) != null) {
//            File photoFile = null;
//
//            try {
//                photoFile = createImageFile();
//            } catch (IOException ex) {
//                MDToast.makeText(Objects.requireNonNull(getContext()), "Kesalahan saat membuat foto", Toast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
//                return;
//            }
//
//            Uri photoURI;
//            if (photoFile != null) {
//                photoURI = FileProvider.getUriForFile(getContext(),"com.dms.mobileappraisal.fileprovider",
//                        photoFile);
//                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
//                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
//            }else{
//                MDToast.makeText(Objects.requireNonNull(getContext()), "Kesalahan saat membuat foto", Toast.LENGTH_SHORT, MDToast.TYPE_ERROR).show();
//                return;
//            }
//        }
  }
//    @SuppressLint("SimpleDateFormat")
//    private File createImageFile() throws IOException {
//        // Create an image file name
//        date = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
//        Object noAplikasi = null;
//        Object jenisPhoto = null;
//        Object noUrut = null;
//        String imageFileName = noAplikasi + jenisPhoto + noUrut + judul;
//        File storageDir = Objects.requireNonNull(getContext()).getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//        File image = File.createTempFile(
//                imageFileName,  /* prefix */
//                ".jpg",         /* suffix */
//                storageDir      /* directory */
//        );
//        // Save a file: path for use with ACTION_VIEW intents
//        mCurrentPhotoPath = image.getAbsolutePath();
//        imgPath.clear();
//        imgPath.add(mCurrentPhotoPath);
//        fileName = imageFileName + ".jpg";
//        Log.d("currentPath Image", "" + image.getAbsolutePath());
//        return image;
//  }

