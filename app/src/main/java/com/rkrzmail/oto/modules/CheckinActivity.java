package com.rkrzmail.oto.modules;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;

import com.naa.data.Nson;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.MainActivity;
import com.rkrzmail.oto.R;

public class CheckinActivity extends AppActivity {
    final int BARCODE_RESULT = 12;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkin);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("CHECIKIN");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =  new Intent(CheckinActivity.this, DaftarkanPelayananActivity.class);


                startActivity(intent);
            }
        });
        findViewById(R.id.imgBarcode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =  new Intent(CheckinActivity.this, BarcodeActivity.class);
                startActivityForResult(intent, BARCODE_RESULT);
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BARCODE_RESULT && resultCode == RESULT_OK){
            EditText editText = findViewById(R.id.txtNopol);

            editText.setText(getIntentStringExtra("TEXT"));
        }

    }
}
