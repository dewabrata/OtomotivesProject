package com.rkrzmail.oto.modules.mekanik;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
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
import static com.rkrzmail.utils.APIUrls.VIEW_KONTROL_LAYANAN;
import static com.rkrzmail.utils.ConstUtils.CARI_PART_LOKASI;
import static com.rkrzmail.utils.ConstUtils.CARI_PART_OTOMOTIVES;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.PART;
import static com.rkrzmail.utils.ConstUtils.REQUEST_CARI_PART;
import static com.rkrzmail.utils.ConstUtils.REQUEST_DETAIL;
import static com.rkrzmail.utils.ConstUtils.MASTER_PART;
import static com.rkrzmail.utils.ConstUtils.RUANG_PART;

public class LkkClaimMekanik_Activity extends AppActivity {

    private Spinner sp_desckerusakan,sp_tipekerusakan,sp_kondisipelanggan,sp_kondisipart,sp_sebabkerusakan,sp_tindakan,sp_infotambahan;
    private EditText et_namapart, et_nopart, et_merkpart, et_nikpemilik, et_nobuku;
    private Button btn_fotostnk, btn_fotoktp, btn_simpan;
    private CheckBox cbClaim;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lkk_claim_mekanik);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("LKK CLAIM ");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        et_merkpart = findViewById(R.id.et_merkpart_lkkclaim);
        et_namapart = findViewById(R.id.et_nama_part);
        et_nikpemilik = findViewById(R.id.et_nik_pemilik);
        et_nobuku = findViewById(R.id.et_no_bukugaransi);
        et_nopart = findViewById(R.id.et_no_part);
        btn_fotoktp = findViewById(R.id.btn_foto_ktp);
        btn_fotostnk = findViewById(R.id.btn_foto_stnk);
        sp_desckerusakan =  findViewById(R.id.sp_desc_kerusakan);
        sp_tipekerusakan =  findViewById(R.id.sp_tipe_kerusakan);
        sp_kondisipelanggan =  findViewById(R.id.sp_kondisi_pelanggan);
        sp_kondisipart =  findViewById(R.id.sp_kondisipart_lkkclaim);
        sp_sebabkerusakan =  findViewById(R.id.sp_sebab_kerusakan);
        sp_tindakan =  findViewById(R.id.sp_tindakan_perbaikan);
        sp_infotambahan =  findViewById(R.id.sp_infotambahan);
        btn_simpan = findViewById(R.id.btn_simpan_lkkclaim);


        setSpinnerFromApi(sp_sebabkerusakan, "nama", "SEBAB KERUSAKAN", "viewmst", "SEBAB_KERUSAKAN");
        setSpinnerFromApi(sp_kondisipart, "nama", "KONDISI PART", "viewmst", "KONDISI");


        cbClaim = (CheckBox) findViewById(R.id.cb_claimgaransi);

        cbClaim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cbClaim.isChecked()){
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
                SimpanData();
            }
        });

        findViewById(R.id.btn_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), CariPart_Activity.class);
                i.putExtra(CARI_PART_OTOMOTIVES, RUANG_PART);
                startActivityForResult(i, REQUEST_CARI_PART);
            }
        });
    }

    private void SimpanData(){
        final String namaPart = et_namapart.getText().toString().toUpperCase();
        final String noPart = et_nopart.getText().toString().toUpperCase();
        final String merkPart = et_merkpart.getText().toString().toUpperCase();
        final String nik = et_nikpemilik.getText().toString().toUpperCase();
        final String noBuku = et_nobuku.getText().toString().toUpperCase();
        final String descKerusakan = sp_desckerusakan.getSelectedItem().toString().toUpperCase();
        final String tipeKerusakan = sp_tipekerusakan.getSelectedItem().toString().toUpperCase();
        final String kondisiPelanggan = sp_kondisipelanggan.getSelectedItem().toString().toUpperCase();
        final String kondisiPart = sp_kondisipart.getSelectedItem().toString().toUpperCase();
        final String sebabKerusakan = sp_sebabkerusakan.getSelectedItem().toString().toUpperCase();
        final String tindakan = sp_tindakan.getSelectedItem().toString().toUpperCase();
        final String infoTambahan = sp_infotambahan.getSelectedItem().toString().toUpperCase();
        final String claim = find(R.id.cb_claimgaransi, CheckBox.class).isChecked() ? "Y" : "N";


        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action ", "add");
                args.put("tanggal_checkin ", namaPart);
                args.put("tanggal_lapor ",namaPart);
                args.put("jam",namaPart);
                args.put("nama_mekanik",namaPart);
                args.put("noPolisi", namaPart);
                args.put("km", namaPart);
                args.put("merk", namaPart);
                args.put("varian",namaPart);
                args.put("kodeTipe  ", namaPart);
                args.put("tahunProduksi",namaPart);
                args.put("tanggalPembelian",namaPart);
                args.put("deskripsiPerusakan",namaPart);
                args.put("tipeKerusakan", namaPart);
                args.put("kondisiPelanggan", namaPart);
                args.put("idPartRusak", namaPart);
                args.put("kondisiPart", namaPart);
                args.put("sebabKerusakan", namaPart);
                args.put("tindakanPerbaikan ",namaPart);
                args.put("claimGaransi",namaPart);
                args.put("informasiTambahan",namaPart);
                args.put("nik",namaPart);
                args.put("linkFotoPart",namaPart);
                args.put("linkFotoKtp",namaPart);
                args.put("linkFotoStnk",namaPart);
                args.put("stockPart",namaPart);
                args.put("kerusakanSamaBulan3 ",namaPart);
                args.put("kerusakanSamaBulan2",namaPart);
                args.put("kerusakanSamaBulan1", namaPart);
                args.put("partTerpakaiBulan3",namaPart);
                args.put("partTerpakaiBulan2",namaPart);
                args.put("partTerpakaiBulan1 ",namaPart);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(SET_CHECKIN), args));
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CARI_PART) {
            Nson nson =  Nson.readJson(getIntentStringExtra(data, PART));

            Intent i = new Intent(getActivity(), LkkClaimMekanik_Activity.class);
            i.putExtra(PART, nson.toJson());
            startActivityForResult(i, REQUEST_DETAIL);

        }else if (resultCode == RESULT_OK && requestCode == REQUEST_DETAIL) {
            Nson fromCariPart = Nson.readJson(getIntentStringExtra(data, PART));
            Log.d("terimaData", "data "+ fromCariPart);
//            nListArray.add(fromCariPart);
//            //rvTotalJualPart.getAdapter().notifyDataSetChanged();
//            int harga = 0;
//            for (int i = 0; i < nListArray.size(); i++) {
//                String hargaJual = formatOnlyNumber(nListArray.get(i).get("TOTAL").asString());
//                harga = harga + Integer.parseInt(hargaJual);
//            }
//            String finalTotal = String.valueOf(harga);
            //tvTotal.setText("Total : Rp. " + formatter.format(Double.valueOf(finalTotal)));
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
//    }
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
//    }
}