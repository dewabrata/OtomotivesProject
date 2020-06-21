package com.rkrzmail.oto.modules.primary.checkin;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.jasa.JasaLainBerkala_Activity;
import com.rkrzmail.oto.modules.jasa.JasaLain_Activity;
import com.rkrzmail.oto.modules.sparepart.new_part.PartBerkala_Activity;
import com.rkrzmail.oto.modules.sparepart.new_part.Part_Activity;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

import java.util.Map;

public class Checkin3_Activity extends AppActivity implements View.OnClickListener {

    private RecyclerView rvCheckin3;
    final int REQUEST_PART = 15;
    final int REQUEST_JASA_LAIN = 16;
    final int REQUEST_JASA_BERKALA = 17;
    final int REQUEST_PART_BERKALA = 19;
    final int REQUEST_PART_EXTERNAL = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkin3_);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_checkin3);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Check-In");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    private void initComponent() {
        initToolbar();

        find(R.id.btn_jasaLain_checkin3, Button.class).setOnClickListener(this);
        find(R.id.btn_sparePart_checkin3, Button.class).setOnClickListener(this);
        find(R.id.btn_jasaLainBerkala_checkin3, Button.class).setOnClickListener(this);
        find(R.id.btn_partBerkala_checkin3, Button.class).setOnClickListener(this);
        find(R.id.btn_partExternal_checkin3, Button.class).setOnClickListener(this);

        componentValidation();

        rvCheckin3 = findViewById(R.id.recyclerView_checkin3);
        rvCheckin3.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvCheckin3.setHasFixedSize(true);

        rvCheckin3.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_booking3_checkin3) {
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);

                viewHolder.find(R.id.tv_nama_booking3, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_harga_booking3, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_disc_booking3, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_net_booking3, TextView.class).setText(nListArray.get(position).get("").asString());

            }
        });

        find(R.id.btn_lanjut_checkin3, Button.class).setOnClickListener(this);

    }

    private void saveData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "add");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("booking3"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {

                } else {
                    showInfo("GAGAL!");
                }
            }
        });

    }

    private void componentValidation() {
        if (find(R.id.cb_estimasi_checkin3, CheckBox.class).isChecked()) {

        } else if (find(R.id.cb_konfirmBiaya_checkin3, CheckBox.class).isChecked()) {

        }
    }

    @Override
    public void onClick(View view) {
        Intent i;
        switch (view.getId()) {
            case R.id.btn_jasaLain_checkin3:
                i = new Intent(getActivity(), JasaLain_Activity.class);
                setResult(RESULT_OK);
                startActivityForResult(i, REQUEST_JASA_LAIN);
                break;
            case R.id.btn_sparePart_checkin3:
                i = new Intent(getActivity(), Part_Activity.class);
                setResult(RESULT_OK);
                startActivityForResult(i, REQUEST_PART);
                break;
            case R.id.btn_jasaLainBerkala_checkin3:
                i = new Intent(getActivity(), JasaLainBerkala_Activity.class);
                setResult(RESULT_OK);
                startActivityForResult(i, REQUEST_JASA_BERKALA);
                break;
            case R.id.btn_partBerkala_checkin3:
                i = new Intent(getActivity(), PartBerkala_Activity.class);
                setResult(RESULT_OK);
                startActivityForResult(i, REQUEST_PART_BERKALA);
                break;
            case R.id.btn_partExternal_checkin3:
                i = new Intent(getActivity(), Part_Activity.class);
                setResult(RESULT_OK);
                startActivityForResult(i, REQUEST_PART_EXTERNAL);
                break;
            case R.id.btn_lanjut_checkin3:
                startActivity(new Intent(getActivity(), Checkin4_Activity.class));
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_PART) {

        } else if (resultCode == RESULT_OK && requestCode == REQUEST_PART_BERKALA) {

        } else if (resultCode == RESULT_OK && requestCode == REQUEST_PART_EXTERNAL) {

        } else if (resultCode == RESULT_OK && requestCode == REQUEST_JASA_BERKALA) {

        } else if (resultCode == RESULT_OK && requestCode == REQUEST_JASA_LAIN) {

        }
    }
}
