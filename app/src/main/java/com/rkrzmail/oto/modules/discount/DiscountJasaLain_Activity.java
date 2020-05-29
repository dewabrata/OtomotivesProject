package com.rkrzmail.oto.modules.discount;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;

public class DiscountJasaLain_Activity extends AppActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discount_jasa_lain_);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_discJasa);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Discount Jasa Lain");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        FloatingActionButton fab = findViewById(R.id.fab_tambah_discJasa);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), AturDiscountJasaLain_Activity.class));
                finish();
            }
        });
    }
}
