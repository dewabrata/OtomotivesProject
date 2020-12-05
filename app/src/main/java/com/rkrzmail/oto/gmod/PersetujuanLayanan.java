package com.rkrzmail.oto.gmod;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
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

import java.util.Map;

public class PersetujuanLayanan extends AppActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_persetujuanlayanan);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setTitle("PENDAFTARAN LAYANAN");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        find(R.id.tv_text_suggesttion,EditText.class).setText(getIntentStringExtra("nopol"));
        find(R.id.txtNamaPelanggan,EditText.class).setText(getIntentStringExtra("nama"));
        //find(R.id.spnLayanan,EditText.class).setText(getIntentStringExtra("layanan"));


        find(R.id.tblBatal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        find(R.id.tblSimpan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Messagebox.showDialog(getActivity(), "Konfirmasi", "Apakah yakin akan simpan ?", "Ya", "Tidak", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        save();
                    }
                }, null);
            }
        });

        find(R.id.tblTtg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Capture.class);
                intent.putExtra("STATUS", "");

                startActivityForResult(intent, REQUEST_CODE_SIGN);
            }
        });



       // nListArray.add("");//hedaer
        nListArray.asArray().addAll(Nson.readJson(getIntentStringExtra("data")).asArray());

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rViewPart);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        recyclerView.setAdapter(new NikitaRecyclerAdapter(nListArray,R.layout.item_part_persetujuan){
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                if (position>=1){
                    viewHolder.find(R.id.txtName, TextView.class).setText(nListArray.get(position).get("NAMA").asString());
                    viewHolder.find(R.id.txtBiaya, TextView.class).setText("0");
                    viewHolder.find(R.id.txtDisc, TextView.class).setText("-0");

                }


            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
                //Toast.makeText(getActivity(),"HHHHH "+position, Toast.LENGTH_SHORT).show();
                /*Intent intent =  new Intent(getActivity(), ControlLayanan.class);
                intent.putExtra("ID", nListArray.get(position).get("MEKANIK").asInteger());
                intent.putExtra("DATA", nListArray.get(position).toJson());
                startActivityForResult(intent, REQUEST_CONTROL);*/
            }
        }));


    }
    final int REQUEST_CODE_SIGN = 66;
    public void save(){
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result ;
            @Override
            public void run() {
                Nson nson = Nson.readNson(getIntentStringExtra("dx"));
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("nopol", nson.get("nopol").asString());
                args.put("nama", nson.get("nama").asString());
                args.put("layanan", nson.get("layanan").asString());
                args.put("phone", nson.get("phone").asString());
                args.put("status", nson.get("status").asString());
                args.put("mekanik", nson.get("mekanik").asString());
                args.put("merk", nson.get("merk").asString());

                String out = InternetX.postHttpConnection(AppApplication.getBaseUrl("save.php"),args);

                result = Nson.readJson(out) ;

            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")){
                    setResult(RESULT_OK);
                    finish();
                }else{
                    showError(result.get("message").asString());
                }


            }
        });
    }
}
