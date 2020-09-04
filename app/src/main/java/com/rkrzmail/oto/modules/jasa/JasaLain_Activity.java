package com.rkrzmail.oto.modules.jasa;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.utils.Tools;

import java.util.ArrayList;
import java.util.Map;

public class JasaLain_Activity extends AppActivity {

    private RecyclerView rvJasa;
    private static final int REQUEST_BIAYA = 11;
    private EditText etAktivitasLain;
    private Nson nson = Nson.newArray();
    private int counting = 0;
    ArrayList<Boolean> flagChecked = new ArrayList<>();
    boolean flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_basic_2);
        initComponent();

    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Jasa Lain");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        rvJasa = findViewById(R.id.recyclerView);
        etAktivitasLain = findViewById(R.id.editText_list_basic2);
        etAktivitasLain.setVisibility(View.VISIBLE);

        catchData();
        rvJasa.setLayoutManager(new LinearLayoutManager(this));
        rvJasa.setHasFixedSize(true);
        rvJasa.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_jasa_lain) {
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                viewHolder.find(R.id.cb_jasaLain_jasa, CheckBox.class).setTag("check");
                viewHolder.find(R.id.cb_jasaLain_jasa, CheckBox.class).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (buttonView.isChecked()) {
                            buttonView.setChecked(true);
                            nson.add(nListArray.get(position));
                        } else {
                            buttonView.setChecked(false);
                            nson.asArray().remove(nson.get(nListArray.get(position)));
                        }
                    }
                });
                viewHolder.find(R.id.tv_masterPart_jasaLain, TextView.class).setText(nListArray.get(position).get("NAMA").asString());
                viewHolder.find(R.id.tv_lepasPasang_jasaLain, TextView.class).setText(nListArray.get(position).get("LEPAS_PASANG").asString());
                viewHolder.find(R.id.tv_waktu_jasaLain, TextView.class).setText(nListArray.get(position).get("WAKTU").asString());
                viewHolder.find(R.id.tv_mekanik_jasaLain, TextView.class).setText(nListArray.get(position).get("TIPE_MEKANIK").asString());
            }

            @Override
            public Nson getItem() {
                if (nListArray.size() == 0) {
                    etAktivitasLain.setVisibility(View.GONE);
                    find(R.id.btn_simpan, Button.class).setVisibility(View.GONE);
                }
                return nListArray;
            }
        });

        find(R.id.btn_simpan, Button.class).setText("Lanjut");
        find(R.id.btn_simpan, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), BiayaJasa_Activity.class);
                i.putExtra("data", nson.toJson());
                i.putExtra("jasa_lain", "");
                Log.d("JASA_LAIN_CLASS", "JASA : " + nson);
                startActivityForResult(i, REQUEST_BIAYA);
            }
        });
    }

    private void catchData() {
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("viewjasalain"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    rvJasa.getAdapter().notifyDataSetChanged();
                } else {
                    showError("Mohon Di Coba Kembali" + result.get("status").asString());
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_BIAYA) {
            Intent i = new Intent();
            i.putExtra("data", Nson.readJson(getIntentStringExtra(data, "data")).toJson());
            Log.d("JASA_LAIN_CLASS", "SENDD : " + Nson.readJson(getIntentStringExtra(data, "data")));
            setResult(RESULT_OK, i);
            finish();
        }
    }
}
