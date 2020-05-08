package com.rkrzmail.oto.modules.terima_part;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;

public class DetailPartDiterima extends AppActivity {

    private static final String TAG = "DetailPartDiterima";
    private static final int REQUEST_DETAIL_PART_DITERIMA = 4242;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_part_diterima);

        initToolbar();
        initComponent();
    }

    private void initToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("ATUR TERIMA PART");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent(){

    }

    private void insertdata(){
        newProses(new Messagebox.DoubleRunnable() {
            @Override
            public void run() {

            }

            @Override
            public void runUI() {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RESULT_OK && resultCode == REQUEST_DETAIL_PART_DITERIMA){

        }
    }
}
