package com.rkrzmail.oto.modules.mekanik;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import static com.rkrzmail.utils.APIUrls.SET_CLAIM;
import static com.rkrzmail.utils.APIUrls.VIEW_MST;

import static com.rkrzmail.utils.ConstUtils.CARI_PART_CLAIM;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.PART;
import static com.rkrzmail.utils.ConstUtils.REQUEST_CARI_PART;
import static com.rkrzmail.utils.ConstUtils.REQUEST_FOTO_KTP;
import static com.rkrzmail.utils.ConstUtils.REQUEST_FOTO_PART;
import static com.rkrzmail.utils.ConstUtils.REQUEST_FOTO_STNK;

public class LkkClaimMekanik_Activity extends AppActivity {


    private Spinner sp_tipekerusakan,sp_kondisipelanggan,sp_kondisipart,sp_sebabkerusakan,sp_tindakan;
    private EditText et_namapart, et_nopart, et_merkpart, et_nikpemilik, et_nobuku, et_desc, et_info;
    private Button btn_fotostnk, btn_fotoktp, btn_simpan;
    private CheckBox cbClaim;
    private Nson dataSebabList = Nson.newArray(), SebabArray = Nson.newArray(), data;
    private String idpart = "", stockBengkel = "", fotoPart="", fotoStnk="", fotoKtp="", idCheckin="";
    private File fileStnk, fileKtp, filePart;
    private AlertDialog alertDialog;
    private Bitmap bitmapPart, bitmapKtp, bitmapStnk;
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
        Tools.setViewAndChildrenEnabled(find(R.id.ly_lkk_claim, LinearLayout.class), false);

        data = Nson.readJson(getIntentStringExtra(DATA));
        idCheckin= data.get("IDCHECKIN").asString();
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
                i.putExtra(CARI_PART_CLAIM, idCheckin);
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
        if (!checkPermission()) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, REQUEST_FOTO_PART);
        } else {
            if (checkPermission()) {
                requestPermissionAndContinue();
            } else {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, REQUEST_FOTO_PART);
            }
        }
    }

    private void setFotoStnk(){
        if (!checkPermission()) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, REQUEST_FOTO_STNK);
        } else {
            if (checkPermission()) {
                requestPermissionAndContinue();
            } else {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, REQUEST_FOTO_STNK);
            }
        }
    }

    private void setFotoKtp(){
        if (!checkPermission()) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, REQUEST_FOTO_KTP);
        } else {
            if (checkPermission()) {
                requestPermissionAndContinue();
            } else {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, REQUEST_FOTO_KTP);
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
        final String nik = et_nikpemilik.getText().toString().toUpperCase();
        final String descKerusakan = et_desc.getText().toString().toUpperCase();
        final String infoTambahan = find(R.id.et_info_tambahan,EditText.class).getText().toString().toUpperCase();
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
                    //showInfo("Sukses Menyimpan Data");
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
            Nson nson = Nson.readJson(getIntentStringExtra(data, PART));
            if (nson.get("GARANSI_LAYANAN").asString().equals("Y")){
                Tools.setViewAndChildrenEnabled(find(R.id.ly_lkk_claim, LinearLayout.class), true);
                et_merkpart.setEnabled(false);
                et_nopart.setEnabled(false);
                et_namapart.setEnabled(false);
                et_nikpemilik.setEnabled(false);
                et_nobuku.setEnabled(false);
                cbClaim.setChecked(false);
                cbClaim.setEnabled(false);
                btn_fotoktp.setEnabled(false);
                btn_fotostnk.setEnabled(false);
                et_merkpart.setText(nson.get("MERK").asString());
                et_nopart.setText(nson.get("NO_PART").asString());
                et_namapart.setText(nson.get("NAMA_PART").asString());
                idpart=nson.get("PART_ID").asString();
                stockBengkel=nson.get("STOCK").asString();
            }else {
                Tools.setViewAndChildrenEnabled(find(R.id.ly_lkk_claim, LinearLayout.class), false);
                cbClaim.setChecked(false);
                cbClaim.setEnabled(false);
                showInfo("Bukan Part Garansi Layanan");
            }
        }
        else if(resultCode == RESULT_OK && requestCode == REQUEST_FOTO_PART){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.layout_alert_camera, null);
            builder.setView(dialogView);

            ImageView img = (ImageView) dialogView.findViewById(R.id.img_alert_foto);
            Bundle extras = data.getExtras();
            bitmapPart = (Bitmap) extras.get("data");
            img.setImageBitmap(bitmapPart);

            dialogView.findViewById(R.id.btn_alert_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });
            dialogView.findViewById(R.id.btn_alert_save).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    filePart = SaveImage(bitmapPart,"PART");
                    fotoPart = FileUtility.encodeToStringBase64(filePart.getAbsolutePath());
                    showInfo("Sukses");
                    alertDialog.dismiss();
                }
            });
            alertDialog = builder.create();
            alertDialog.show();
        }
        else if(resultCode == RESULT_OK && requestCode == REQUEST_FOTO_STNK){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.layout_alert_camera, null);
            builder.setView(dialogView);

            ImageView img = (ImageView) dialogView.findViewById(R.id.img_alert_foto);
            Bundle extras = data.getExtras();
            bitmapStnk = (Bitmap) extras.get("data");
            img.setImageBitmap(bitmapStnk);

            dialogView.findViewById(R.id.btn_alert_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });
            dialogView.findViewById(R.id.btn_alert_save).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fileStnk = SaveImage(bitmapStnk,"STNK");
                    fotoStnk = FileUtility.encodeToStringBase64(fileStnk.getAbsolutePath());
                    showInfo("Sukses");
                    alertDialog.dismiss();
                }
            });
            alertDialog = builder.create();
            alertDialog.show();
        }
        else if(resultCode == RESULT_OK && requestCode == REQUEST_FOTO_KTP){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.layout_alert_camera, null);
            builder.setView(dialogView);

            ImageView img = (ImageView) dialogView.findViewById(R.id.img_alert_foto);
            Bundle extras = data.getExtras();
            bitmapKtp = (Bitmap) extras.get("data");
            img.setImageBitmap(bitmapKtp);

            dialogView.findViewById(R.id.btn_alert_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });
            dialogView.findViewById(R.id.btn_alert_save).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fileKtp = SaveImage(bitmapKtp,"KTP");
//                    Bitmap decodePart = BitmapFactory.decodeFile(filePart.getAbsolutePath());
                    fotoKtp = FileUtility.encodeToStringBase64(fileKtp.getAbsolutePath());
                    showInfo("Sukses");
                    alertDialog.dismiss();
                }
            });
            alertDialog = builder.create();
            alertDialog.show();
        }
    }

    private File SaveImage(Bitmap finalBitmap, String foto) {
        String root = Environment.getExternalStorageDirectory().getAbsolutePath();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File myDir = new File(root + "/Otomotives");
        myDir.mkdirs();

        String fname = foto + "-" + timeStamp +".png";
        File file = new File (myDir, fname);
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

}

