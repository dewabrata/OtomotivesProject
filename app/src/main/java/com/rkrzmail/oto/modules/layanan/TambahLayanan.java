package com.rkrzmail.oto.modules.layanan;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.naa.data.Nson;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.sparepart.CariPart_Activity;
import com.rkrzmail.srv.PercentFormat;

public class TambahLayanan extends AppActivity {

    private static final String TAG = "TambahLayanan__";
    private EditText etMasterPart, etDiscPart, etDiscJasa, etKelompokPart, etWaktu;
    private Spinner spAktivitas;
    private boolean isKategori = false;
    private Intent i;
    private static final int CARI_PART = 10;
    private String namaPart = "", noPart = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_layanan);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Layanan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        etMasterPart = findViewById(R.id.et_masterPart_layanan);
        etDiscPart = findViewById(R.id.et_discPart_layanan);
        etDiscJasa = findViewById(R.id.et_discJasa_layanan);
        etKelompokPart = findViewById(R.id.et_kelompokPart_layanan);
        spAktivitas = findViewById(R.id.sp_aktivitas_layanan);
        etWaktu = findViewById(R.id.et_waktu_layanan);

        etDiscJasa.addTextChangedListener(new PercentFormat(etDiscJasa));
        etDiscPart.addTextChangedListener(new PercentFormat(etDiscPart));
        etWaktu.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int counting = (s == null) ? 0 : s.toString().length();
//                if (etWaktu.length() == 0) {
//                    etWaktu.setText(" MENIT");
//                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        i = getIntent();

        if (i.hasExtra("disc_part")) {
            find(R.id.ly_tambah_discPart_layanan, LinearLayout.class).setVisibility(View.VISIBLE);
            isKategori = true;
            find(R.id.btn_simpan_layanan, Button.class).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    giveItBackToParentActivity();
                }
            });

        } else if (i.hasExtra("jasa_lain")) {
            find(R.id.ly_tambah_jasaLain_layanan, LinearLayout.class).setVisibility(View.VISIBLE);
            isKategori = false;
            find(R.id.btn_simpan_layanan, Button.class).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    giveItBackToParentActivity();
                }
            });

        }
    }

    private void giveItBackToParentActivity() {
        Nson n = Nson.newObject();
        //jasa : kelompok, aktivitas, waktu
        //part : namaMaster,discpart, discJasa, (namaPart, noPart),
        n.set(isKategori ? "NAMA_MASTER" : "KELOMPOK_PART", isKategori ? etMasterPart.getText().toString() : etKelompokPart.getText().toString());
        n.set(isKategori ? "DISC_PART": "AKTIVITAS", isKategori ? etDiscPart.getText().toString() : ""); //n.set("DISC_AKTIVITAS", spAktivitas.getSelectedItem().toString());
        n.set(isKategori ? "DISC_JASA" : "WAKTU", isKategori ? etDiscJasa.getText().toString() : find(R.id.et_waktu_layanan, EditText.class).getText().toString());
        if(!namaPart.isEmpty() && !noPart.isEmpty()){
            n.set("NAMA_PART", namaPart);
            n.set("NO_PART", noPart);
        }
        //Log.d("Tambah____", "giveItBackToParentActivity: " + n.toJson());
        if(isKategori){
            if(!validateFields(find(R.id.ly_tambah_discPart_layanan, LinearLayout.class))){
                setIntent(n);
            }
        }else{
            if(!validateFields(find(R.id.ly_tambah_jasaLain_layanan, LinearLayout.class))){
                setIntent(n);
            }
        }
    }

    private void setIntent(Nson n){
        Intent i = new Intent(getActivity(), AturLayanan_Activity.class);
        i.putExtra("data", n.toJson());
        showInfo("Layanan di masukkan Ke Daftar");
        setResult(RESULT_OK, i);
        finish();
    }

    SearchView mSearchView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_part, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            i = new Intent(getActivity(), CariPart_Activity.class);
            i.putExtra(isKategori ? "MASTER_PART" : "KELOMPOK_PART", isKategori ? "MASTER_PART" : "PART");
            startActivityForResult(i, CARI_PART);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == CARI_PART) {
                Nson nama = Nson.readJson(getIntentStringExtra(data, "part"));
                etMasterPart.setText(nama.get("NAMA_MASTER").asString());
                etKelompokPart.setText(nama.get("KELOMPOK").asString());
                namaPart = nama.get("NAMA_PART").asString();
                noPart = nama.get("NOMOR_PART_NOMOR").asString();
                //namaPart += nama.get()
                Log.d(TAG, "Layanan : " + nama);
            }
        }
    }
}
