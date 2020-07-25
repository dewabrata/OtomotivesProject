package com.rkrzmail.oto.modules.sparepart.new_part;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.utils.Tools;

import java.util.Map;

public class JumlahHargaPart_Activity extends AppActivity implements View.OnClickListener {

    private EditText etHpp, etHargaJual, etDiscPart, etJasa, etDiscJasa, etDp, etWaktuPesan, etJumlah;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jumlah_harga_part_);
        initComponent();
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Jumlah & Harga Part");
    }

    private void initComponent() {
        initToolbar();
        Nson n = Nson.readJson(getIntentStringExtra("data"));
        Log.d("JUMLAH_HARGA_PART", "INTENT : "   + n.toJson());
        int stock = n.get("STOCK").asInteger();
        if(stock == 0){
            Messagebox.showDialog(getActivity(),
                    "Konfirmasi", "Buka Form Part Kosong ? ", "Ya", "Tidak", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            find(R.id.ly_hpp_jumlah_harga_part, TextInputLayout.class).setVisibility(View.VISIBLE);
                            find(R.id.ly_disc_jumlah_harga_part, TextInputLayout.class).setVisibility(View.VISIBLE);
                            find(R.id.ly_discJasa_jumlah_harga_part, TextInputLayout.class).setVisibility(View.VISIBLE);
                            find(R.id.ly_jumlahHarga_partKosong, LinearLayout.class).setVisibility(View.VISIBLE);
                        }
                    },  new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
        }else{
            find(R.id.ly_jumlahHarga_part, LinearLayout.class).setVisibility(View.VISIBLE);
        }
        etHpp = findViewById(R.id.et_hpp_jumlah_harga_part);
        etHargaJual = findViewById(R.id.et_hargaJual_jumlah_harga_part);
        etDiscJasa = findViewById(R.id.et_discJasa_jumlah_harga_part);
        etDiscPart = findViewById(R.id.et_discPart_jumlah_harga_part);
        etHargaJual = findViewById(R.id.et_hargaJual_jumlah_harga_part);
        etDp = findViewById(R.id.et_dp_jumlah_harga_part);
        etWaktuPesan = findViewById(R.id.et_waktu_jumlah_harga_part);
        etJumlah = findViewById(R.id.et_jumlah_jumlah_harga_part);
        etJasa = findViewById(R.id.et_jasa_jumlah_harga_part);

        find(R.id.img_clear, ImageButton.class).setOnClickListener(this);
        find(R.id.img_clear2, ImageButton.class).setOnClickListener(this);

        watcher(find(R.id.img_clear, ImageButton.class), etHargaJual);
        watcher(find(R.id.img_clear2, ImageButton.class), etJasa);

        find(R.id.ly_jumlahHarga_part, LinearLayout.class);
        find(R.id.ly_jumlahHarga_partKosong, LinearLayout.class);
        find(R.id.btn_simpan_jumlah_harga_part, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextForm();
            }
        });
    }

    private void nextForm() {
        Nson nson = Nson.readJson(getIntentStringExtra("data"));
        String harga =  etHargaJual.getText().toString().replaceAll("[^0-9]+", "");
        String jasa = etJasa.getText().toString().replaceAll("[^0-9]+", "");
        nson.set("JASA", jasa);
        if(!harga.isEmpty() && !jasa.isEmpty()){
            int totall = Integer.parseInt(harga) * Integer.parseInt(etJumlah.getText().toString());
            nson.set("HARGA_PART", "Rp" + totall);
            nson.set("NET", "Rp" + (Integer.parseInt(jasa) + totall));
        }

        Intent i = new Intent();
        i.putExtra("data", nson.toJson());
        setResult(RESULT_OK, i);
        finish();
            //nson.set("")
//            etHpp.setEnabled(true);
//            etJasa.setEnabled(true);
//            etWaktuPesan.setEnabled(true);
//            etDp.setEnabled(true);
//            etHargaJual.setEnabled(true);
//            etDiscJasa.setEnabled(true);
//            etDiscPart.setEnabled(true);
    }

    void watcher (final ImageButton imageButton, final EditText editText){
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if(s.toString().length() == 0){
                    imageButton.setVisibility(View.GONE);
                }else{
                    imageButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (editText == null) return;
                String str = s.toString();
                if (str.isEmpty()) return;
                editText.removeTextChangedListener(this);
                try {
                    String cleanString = str.replaceAll("[^0-9]", "");
                    String formatted = Tools.formatRupiah(cleanString);
                    editText.setText(formatted);
                    editText.setSelection(formatted.length());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                editText.addTextChangedListener(this);
            }
        };
        editText.addTextChangedListener(textWatcher);
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.img_clear:
                etHargaJual.setText("");
                break;
            case R.id.img_clear2:
                etJasa.setText("");
                break;
        }
    }
}
