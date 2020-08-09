package com.rkrzmail.oto.modules.sparepart;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.naa.data.Nson;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;

public class DetailCariPart_Activity extends AppActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_cari_part_);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("DETAIL PART");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    void initComponent(){
        initToolbar();
        Nson getData = Nson.readJson(getIntentStringExtra("part"));

        find(R.id.tv_merk_cariPart, TextView.class).setText(getData.get("MERK").asString());
        find(R.id.tv_namaPart_cariPart, TextView.class).setText(getData.get("NAMA").asString());
        find(R.id.tv_noPart_cariPart, TextView.class).setText(getData.get("NO_PART").asString());
        find(R.id.tv_berkala_cariPart, TextView.class).setText(getData.get("BERKALA_KM").asString());
        find(R.id.tv_harga_cariPart, TextView.class).setText(getData.get("HARGA_JUAL").asString());
        find(R.id.tv_frt_cariPart, TextView.class).setText(getData.get("").asString());
        find(R.id.tv_satuan_cariPart, TextView.class).setText(getData.get("").asString());
        find(R.id.tv_warna_cariPart, TextView.class).setText(getData.get("").asString());
        find(R.id.tv_km_cariPart, TextView.class).setText(getData.get("BERKALA_KM").asString() + getData.get("BERKALA_BULAN").asString());
        find(R.id.tv_disc_cariPart, TextView.class).setText(getData.get("").asString());
        find(R.id.tv_jasa_cariPart, TextView.class).setText(getData.get("").asString());
        find(R.id.tv_min_cariPart, TextView.class).setText(getData.get("").asString());

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_OK);
        finish();
    }
}
