package com.rkrzmail.oto.modules.sparepart;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.utils.Tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import static com.rkrzmail.utils.APIUrls.ATUR_LOKASI_PART;
import static com.rkrzmail.utils.APIUrls.VIEW_LOKASI_PART;
import static com.rkrzmail.utils.ConstUtils.CARI_PART_TERALOKASIKAN;
import static com.rkrzmail.utils.ConstUtils.DATA;

public class AturLokasiPart_Activity extends AppActivity {

    private static final String TAG = "Lokasi___";
    private static final int REQUEST_ATUR_LOKASI_PART = 3232;
    private EditText no_part, nama_part, merk_part;
    private Spinner sp_lokasi_part, sp_penempatan_part, sp_noRakPalet_part, sp_tinggiRak_part, sp_noFolder_part;
    private boolean isLokasi, isRak = false, isUpdate = false;
    private String
            noRak = "",
            noFolder = "",
            tinggkatRak = "",
            tempatPart = "",
            lokasiPart = "",
            penempatanPart = "", partId = "";
    int lokasiID = 0;
    private Nson lokasiArray = Nson.newArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_lokasi_part);
        initToolbar();
        // Tools.setViewAndChildrenEnabled(find(R.id.ly_lokasiPart, LinearLayout.class), false);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Atur Lokasi Part");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        no_part = findViewById(R.id.et_no_part);
        nama_part = findViewById(R.id.et_nama_part);
        merk_part = findViewById(R.id.et_merk_part);
        sp_lokasi_part = findViewById(R.id.sp_lokasiPart);
        sp_penempatan_part = findViewById(R.id.sp_penempatan_part);
        sp_noRakPalet_part = findViewById(R.id.sp_norakPalet_part);
        sp_tinggiRak_part = findViewById(R.id.sp_tinggiRak);
        sp_noFolder_part = findViewById(R.id.sp_noFolder_part);

        final Nson teralokasi = Nson.readJson(getIntentStringExtra(CARI_PART_TERALOKASIKAN));
        final Nson addNew = Nson.readJson(getIntentStringExtra(DATA));

        if (getIntent().hasExtra(CARI_PART_TERALOKASIKAN)) {
            isUpdate = true;
            Tools.setViewAndChildrenEnabled(find(R.id.ly_lokasiPart, LinearLayout.class), false);
            noRak = teralokasi.get("RAK").asString();
            tinggkatRak = teralokasi.get("").asString();
            noFolder = teralokasi.get("NO_FOLDER").asString();
            lokasiPart = teralokasi.get("LOKASI").asString();
            penempatanPart = teralokasi.get("PENEMPATAN").asString();
            partId = teralokasi.get("PART_ID").asString();
            lokasiID = teralokasi.get("LOKASI_ID").asInteger();
            no_part.setText(teralokasi.get("NO_PART").asString());
            nama_part.setText(teralokasi.get("NAMA_PART").asString());
            merk_part.setText(teralokasi.get("MERK").asString());
        } else {
            no_part.setText(addNew.get("NO_PART").asString());
            nama_part.setText(addNew.get("NAMA_PART").asString());
            merk_part.setText(addNew.get("MERK").asString());
            partId = addNew.get("PART_ID").asString();
        }

        if (!isUpdate) viewLokasiPart();
        setSpLokasiPart(lokasiPart);
        spinnerView();
        find(R.id.btn_simpan, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });
    }

    private void saveData() {
        String tinggirak = "";
        if (sp_tinggiRak_part.isEnabled()) {
            tinggirak = sp_tinggiRak_part.getSelectedItem().toString().toUpperCase();
        } else {
            tinggirak = "0";
        }
        final String nofolder = sp_noFolder_part.getSelectedItem().toString().toUpperCase();
        final String rakOrPalet = sp_noRakPalet_part.getSelectedItem().toString();
        final String finalTinggirak = tinggirak;
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "insertOrUpdate");
                args.put("tempat", tempatPart);
                if (tempatPart.equalsIgnoreCase("PALET")) {
                    args.put("palet", rakOrPalet);
                    args.put("rak", "0");
                } else {
                    args.put("rak", rakOrPalet);
                    args.put("palet", "0");
                }
                args.put("lokasiID", String.valueOf(lokasiID));
                args.put("folder", nofolder);
                args.put("tingkatrak", finalTinggirak);
                args.put("partid", partId);
                args.put("stock", "0");
                args.put("kode", kodePenempatan(tempatPart, rakOrPalet, finalTinggirak, nofolder));
                args.put("lokasi_part", sp_lokasi_part.getSelectedItem().toString());

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(ATUR_LOKASI_PART), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Berhasil Menempatkan Part");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    if (result.get("message").asString().toLowerCase().contains("duplicate")) {
                        showError("Data Lokasi Sudah di Masukkan");
                    } else {
                        showError(result.get("message").asString());
                    }
                }
            }
        });
    }

    private void viewLokasiPart() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("flag", "TERALOKASI");
                args.put("lokasi", "RUANG PART");
                args.put("partid", partId);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_LOKASI_PART), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data");
                    if (result.size() > 0) {
                        for (int i = 0; i < result.size(); i++) {
                            lokasiArray.add(result.get(i).get("LOKASI"));
                        }
                    }
                    if (result.size() >= 3) {
                        showInfoDialog("Konfirmasi", "PART SUDAH DI TEMPATKAN DI SEMUA LOKASI", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });
                    }
                    setSpLokasiPart(lokasiPart);
                }
            }
        });
    }

    private String kodePenempatan(String tempat, String no, String tingkat, String folder) {
        String kode;
        if (tempat.equals("RAK")) {
            kode = "R" + "." + no + "." + tingkat + "." + folder;
        } else {
            kode = "P" + "." + no + "." + folder;
        }
        return kode;
    }

    private void spinnerView() {
        List<String> noRakList = new ArrayList<String>();
        List<String> tinggiRakList = new ArrayList<String>();
        List<String> noFolderList = new ArrayList<String>();
        List<String> penempatanList = Arrays.asList(getResources().getStringArray(R.array.penempatan_lokasi_part));
        for (int i = 1; i <= 100; i++) {
            if (!noRakList.contains(i)) {
                noRakList.add(String.valueOf(i));
            }
            if (!noFolderList.contains(i)) {
                noFolderList.add(String.valueOf(i));
            }
            if (i <= 10) {
                tinggiRakList.add(String.valueOf(i));
            }
        }

        setSpinnerOffline(penempatanList, sp_penempatan_part, penempatanPart);
        setSpinnerOffline(noRakList, sp_noRakPalet_part, noRak);
        setSpinnerOffline(tinggiRakList, sp_tinggiRak_part, tinggkatRak);
        setSpinnerOffline(noFolderList, sp_noFolder_part, noFolder);

        sp_penempatan_part.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                tempatPart = parent.getItemAtPosition(position).toString();
                sp_tinggiRak_part.setEnabled(tempatPart.equalsIgnoreCase("RAK"));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @SuppressLint("NewApi")
    private void setSpLokasiPart(String selection) {
        List<String> lokasiList = new ArrayList<>();
        lokasiList.add("RUANG PART");
        lokasiList.add("DISPLAY");
        lokasiList.add("GUDANG");

        if (!isUpdate && lokasiArray.size() > 0) {
            lokasiList.removeIf(new Predicate<String>() {
                @Override
                public boolean test(String s) {
                    return lokasiArray.containsValue(s);
                }
            });
        }

        setSpinnerOffline(lokasiList, sp_lokasi_part, selection);
        sp_lokasi_part.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void backupSpLokasi() {
/*
        ArrayAdapter<String> lokasiAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, lokasiList) {
            @SuppressLint("WrongConstant")
            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = null;
                if (isLokasi) {
                    if(lokasiArray.size() > 0){
                        for (int i = 0; i < lokasiArray.size(); i++) {
                            if (lokasiArray.get(i).asString().equals(lokasiList.get(position))) {
                                TextView mTextView = new TextView(getContext());
                                mTextView.setVisibility(View.GONE);
                                mTextView.setHeight(0);
                                view = mTextView;
                            } else {
                                view = super.getDropDownView(position, convertView, parent);
                            }
                        }
                    }else{
                        view = super.getDropDownView(position, convertView, parent);
                    }
                }else{
                    view = super.getDropDownView(position, convertView, parent);
                }
                return view;
            }
        };


        lokasiAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //sp_lokasi_part.setAdapter(lokasiAdapter);
        if (!lokasiPart.isEmpty()) {

            for (int i = 0; i < lokasiList.size(); i++) {
                if (sp_lokasi_part.getItemAtPosition(i).toString().equals(lokasiPart)) {
                    if (isLokasi) {
                        sp_lokasi_part.setSelection(i);
                    } else {
                        sp_lokasi_part.setSelection(i);
                    }
                }
            }

        }
*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_OK) {
            Nson nson = Nson.readJson(getIntentStringExtra("row"));
            no_part.setText(nson.get("NO_PART_ID").asString());
            nama_part.setText(nson.get("NAMA").asString());
            merk_part.setText(nson.get("MERK_PART").asString());
        }
    }
}
