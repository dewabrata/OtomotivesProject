package com.rkrzmail.oto.modules.layanan;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.naa.data.Nson;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.sparepart.CariPart_Activity;
import com.rkrzmail.utils.Tools;

public class TambahLayanan extends AppActivity {

    //untuk tambah part dan tambah jasa lain
    private EditText etMasterPart, etDiscPart, etDiscJasa, etKelompokPart;
    private Spinner spAktivitas;
    private boolean namaLayanan = false;
    private Intent i ;
    private static final int CARI_PART = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_layanan);
        initComponent();
    }

    private void initToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Layanan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent(){
        initToolbar();
        etMasterPart = findViewById(R.id.et_masterPart_layanan);
        etDiscPart = findViewById(R.id.et_discPart_layanan);
        etDiscJasa = findViewById(R.id.et_discJasa_layanan);
        etKelompokPart = findViewById(R.id.et_kelompokPart_layanan);
        spAktivitas = findViewById(R.id.sp_aktivitas_layanan);
        find(R.id.tv_waktuKerja_layanan, TextView.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDatePickerDialogTextView(getActivity(), find(R.id.tv_waktuKerja_layanan, TextView.class));
            }
        });

        i = getIntent();

        if(i.hasExtra("disc_part")){
            find(R.id.ly_tambah_discPart_layanan, LinearLayout.class).setVisibility(View.VISIBLE);
            namaLayanan = true;
            find(R.id.btn_simpan_layanan, Button.class).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    giveItBackToParentActivity();
                }
            });

        }else if(i.hasExtra("jasa_lain")){
            find(R.id.ly_tambah_jasaLain_layanan, LinearLayout.class).setVisibility(View.VISIBLE);
            namaLayanan = false;
            find(R.id.btn_simpan_layanan, Button.class).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    giveItBackToParentActivity();
                }
            });

        }
    }

    private void giveItBackToParentActivity(){
        Nson n = Nson.newObject();
        //disc_part
        n.set("NAMA_LAYANAN", namaLayanan ? etMasterPart.getText().toString() : etKelompokPart.getText().toString());
        n.set("DISC_AKTIVITAS", etDiscPart.getText().toString());
        n.set("DISC_WAKTU_KERJA", etDiscJasa.getText().toString());
        //jasa_lain
        n.set("NAMA_LAYANAN", etMasterPart.getText().toString());
        n.set("DISC_AKTIVITAS", spAktivitas.getSelectedItem().toString());
        n.set("DISC_WAKTU_KERJA",  find(R.id.tv_waktuKerja_layanan, TextView.class).getText().toString());
        Intent i = new Intent(getActivity(), AturLayanan_Activity.class);
        i.putExtra("data", n.toJson());
        setResult(RESULT_OK);
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
        if(item.getItemId() == R.id.action_search){
            i = getIntent();
            if(i.hasExtra("disc_part")){
                i = new Intent(getActivity(), CariPart_Activity.class);
                i.putExtra("flag master part", "MASTER_PART");
                startActivityForResult(i, CARI_PART);
            }else if(i.hasExtra("jasa_lain")){
                i = new Intent(getActivity(), CariPart_Activity.class);
                i.putExtra("flag jasa lain", "KELOMPOK_PART");
                startActivityForResult(i, CARI_PART);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == CARI_PART){
                etMasterPart.setText(getIntentStringExtra(data, "part"));
            }
        }
    }
}
