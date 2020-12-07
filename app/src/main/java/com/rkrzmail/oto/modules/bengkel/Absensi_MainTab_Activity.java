package com.rkrzmail.oto.modules.bengkel;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.naa.data.Nson;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.gmod.MyCode;
import com.rkrzmail.oto.modules.BarcodeActivity;
import com.rkrzmail.oto.modules.Fragment.Absen_Absensi_Fragment;
import com.rkrzmail.oto.modules.Fragment.BatalPart_TugasPart_Fragment;
import com.rkrzmail.oto.modules.Fragment.PartKosong_TugasPart_Fragment;
import com.rkrzmail.oto.modules.Fragment.Permintaan_TugasPart_Fragment;
import com.rkrzmail.oto.modules.Fragment.Schedule_Absensi_Fragment;
import com.rkrzmail.oto.modules.Fragment.Tersedia_TugasPart_Fragment;
import com.rkrzmail.oto.modules.sparepart.OutSource_Activity;
import com.rkrzmail.srv.FragmentsAdapter;

import java.util.ArrayList;
import java.util.Objects;

import static com.rkrzmail.utils.ConstUtils.REQUEST_BARCODE;

public class Absensi_MainTab_Activity extends AppActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tab_layout);
        initToolbar();
        initComponent();
    }

    @SuppressLint("NewApi")
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Absen");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        ViewPager vpTugasParts = findViewById(R.id.vp);
        TabLayout tabLayoutTugasParts = findViewById(R.id.tablayout);
        tabLayoutTugasParts.setTabMode(TabLayout.MODE_SCROLLABLE);

        final ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(new Absen_Absensi_Fragment());
        fragments.add(new Schedule_Absensi_Fragment());

        FragmentsAdapter pagerAdapter = new FragmentsAdapter(getSupportFragmentManager(), this, fragments);
        vpTugasParts.setAdapter(pagerAdapter);
        vpTugasParts.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayoutTugasParts));
        tabLayoutTugasParts.setupWithViewPager(vpTugasParts);

        setScanBarcode();
    }

    private void setScanBarcode(){
        find(R.id.img_scan_barcode, ImageButton.class).setVisibility(View.VISIBLE);
        find(R.id.img_scan_barcode, ImageButton.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), BarcodeActivity.class);
                startActivityForResult(i, REQUEST_BARCODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == REQUEST_BARCODE){
            String barcodeResult = data != null ? data.getStringExtra("TEXT").replace("\n", "").trim() : "";
            MyCode.checkMyCode(this, barcodeResult, new MyCode.RunnableWD() {
                @Override
                public void runWD(Nson nson) {
                    if (nson.get("status").asString().equals("OK")) {
                        nson = nson.get("data").get(0);
                        if(nson.asArray().isEmpty()){
                            showWarning("User Tidak Valid");
                            return;
                        }else{
                            showSuccess("Scan Barcode Berhasil");
                        }

                        Log.d("Barcode__", "onActivityResult: " + nson);
                    } else {
                        showError(nson.get("message").asString());
                    }
                }
            });
        }
    }
}