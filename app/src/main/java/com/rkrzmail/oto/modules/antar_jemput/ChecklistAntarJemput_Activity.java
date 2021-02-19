package com.rkrzmail.oto.modules.antar_jemput;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Map;

public class ChecklistAntarJemput_Activity extends AppActivity {

    private static final int CAMERA_RESULT = 11;
    private RecyclerView rvCheck;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_basic_with_btn);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Check List Antar Jemput");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        rvCheck = findViewById(R.id.recyclerView);
        rvCheck.setLayoutManager(new LinearLayoutManager(this));
        rvCheck.setHasFixedSize(true);
        rvCheck.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_check_list) {
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {

                viewHolder.find(R.id.tv_tittle_checklist, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.cb_checklist, CheckBox.class).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    }
                });
                viewHolder.find(R.id.img_camera_checklist, TextView.class).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
                viewHolder.find(R.id.img_note_checklist, TextView.class).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });

            }

            @Override
            public int getItemCount() {
                if (nListArray.size() == 0) {
                    find(R.id.btn_simpan).setVisibility(View.GONE);
                } else {
                    find(R.id.btn_simpan).setVisibility(View.VISIBLE);

                }
                return super.getItemCount();
            }
        });
        catchData();
    }

    private void capturePhoto(String location){
        Calendar cal = Calendar.getInstance();
        File file = new File(Environment.getExternalStorageDirectory(), (cal.getTimeInMillis() + location + ".jpg"));
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            file.delete();
            try {
                file.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        Uri capturedImageUri = Uri.fromFile(file);
        Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        i.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri);
        startActivityForResult(i, CAMERA_RESULT);

        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
//                args.put("action", "view");
//                args.put("search", cari);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(""), args));
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

    private void catchData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(""), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    rvCheck.getAdapter().notifyDataSetChanged();
                } else {
                    showError("Mohon Di Coba Kembali");
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CAMERA_RESULT){

        }
    }
}
