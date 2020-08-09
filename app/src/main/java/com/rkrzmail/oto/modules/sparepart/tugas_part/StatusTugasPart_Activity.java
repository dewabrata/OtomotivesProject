package com.rkrzmail.oto.modules.sparepart.tugas_part;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.BarcodeActivity;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

import java.util.Map;

public class StatusTugasPart_Activity extends AppActivity {

    private static final int REQUEST_BARCODE = 11;
    private RecyclerView recyclerView;
    private EditText etTgl, etJam, etPelanggan, etUser;
    //hanya visible jika serah terima part
    private ImageView imgBarcode;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_tugas_part);
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();

        recyclerView = findViewById(R.id.recyclerView);
        etTgl = findViewById(R.id.et_tgl_statusTp);
        etJam = findViewById(R.id.et_jam_statusTp);
        etPelanggan = findViewById(R.id.et_pelanggan_statusTp);
        etUser = findViewById(R.id.et_user_statusTp);
        imgBarcode = findViewById(R.id.imgBarcode);

        imgBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), BarcodeActivity.class);
                setResult(RESULT_OK);
                startActivityForResult(i, REQUEST_BARCODE);
            }
        });

        find(R.id.btn_simpan_statusTp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void loadData(){
        Nson n = Nson.readJson(getIntentStringExtra("data"));
        String status = n.get("STATUS").asString();
        if(status.equalsIgnoreCase("PERMINTAAN")){
            getSupportActionBar().setTitle("PENYEDIAAN PART");

        }else if(status.equalsIgnoreCase("SIAP")){
            getSupportActionBar().setTitle("SERAH TERIMA PART");

        }else if(status.equalsIgnoreCase("SERAH TERIMA")){
            getSupportActionBar().setTitle("PENGEMBALIAN");
            imgBarcode.setVisibility(View.VISIBLE);
        }
        if(!n.get("NAMA_PELANGGAN").equals("")){
            etPelanggan.setVisibility(View.VISIBLE);
        }
        etTgl.setText(n.get("TANGGAL").asString());
        etJam.setText(n.get("JAM").asString());
        etPelanggan.setText(n.get("NAMA_PELANGGAN").asString());
        etUser.setText(n.get("USER").asString());

        initRecyclerView();
    }

    private void initRecyclerView(){
        catchData();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_status_tugas_part) {
                    @Override
                    public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                        super.onBindViewHolder(viewHolder, position);
                        //stock hanya visible jika status permintaan
                        //jumlah minta = serah terima & pengembalian
                        //jumlah minta = permintaan, jumlah sedia = serah terima & pengembalian

                        String status = nListArray.get(position).get("STATUS").asString();
                        if(status.equalsIgnoreCase("PERMINTAAN")) {
                            viewHolder.find(R.id.tv_stock_statusTp, TextView.class).setVisibility(View.VISIBLE);
                        }else if(status.equalsIgnoreCase("SERAH TERIMA")){
                            viewHolder.find(R.id.tv_noFolder_statusTp, TextView.class).setVisibility(View.VISIBLE);
                        }

                        viewHolder.find(R.id.tv_noFolder_statusTp, TextView.class).setText(nListArray.get(position).get("").asString());
                        viewHolder.find(R.id.tv_merk_statusTp, TextView.class).setText(nListArray.get(position).get("").asString());
                        viewHolder.find(R.id.tv_namaPart_statusTp, TextView.class).setText(nListArray.get(position).get("").asString());
                        viewHolder.find(R.id.tv_noPart_statusTp, TextView.class).setText(nListArray.get(position).get("").asString());
                        viewHolder.find(R.id.tv_jumlahSedia_jumlahSisa_statusTp, TextView.class).setText(nListArray.get(position).get("").asString());
                        viewHolder.find(R.id.tv_stock_statusTp, TextView.class).setText(nListArray.get(position).get("").asString());
                        viewHolder.find(R.id.tv_jumlahMinta_statusTp, TextView.class).setText(nListArray.get(position).get("").asString());

                    }

                }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Nson parent, View view, int position) {

                    }
                })
        );
    }

    private void catchData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturtugaspart"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    recyclerView.getAdapter().notifyDataSetChanged();
                } else {
                    showInfo("Gagal");
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == REQUEST_BARCODE){

        }
    }
}
