package com.rkrzmail.oto.gmod;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.data.UtilityAndroid;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.MenuActivity;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.gmod.part.AdapterListBasic;
import com.rkrzmail.oto.gmod.part.People;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.utils.DataGenerator;

import java.util.List;
import java.util.Map;

public class PengambilanPartActivity extends AppActivity {
    private View parent_view;

    private RecyclerView recyclerView;
    private AdapterListBasic mAdapter;

    final int REQUEST_BARCODE = 13;
//    final int REQUEST_PENGAMBILAN_PART = 777;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pengambilan_part);
        parent_view = findViewById(android.R.id.content);

        initToolbar();
        initComponent();


        find(R.id.imgBarcode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), BarcodeActivity.class);
                startActivityForResult(intent, REQUEST_BARCODE);
            }
        });

        find(R.id.tblSimpan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), MenuActivity.class);
                setResult(RESULT_OK, intent);
                simpan();
                finish();
            }
        });

        find(R.id.tblPrint).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });

    }

    private void simpan() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();


//                String penjualan = find(R.id.sp_penjualan_part, Spinner.class).getText().toString();
//                penjualan = penjualan.toUpperCase().trim().replace(" ", "");

                String nopol = find(R.id.txtNopol, EditText.class).getText().toString();
                nopol = nopol.toUpperCase().trim().replace(" ", "");

                String no_ponsel = find(R.id.txtPhone, EditText.class).getText().toString();
                no_ponsel = no_ponsel.toUpperCase().trim().replace(" ", "");


//                args.put("penjualan", penjualan);
                args.put("nopol", nopol);
                args.put("no_ponsel", no_ponsel);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrl("jualpartgrosir.php"), args));
            }

            public void runUI() {

                find(R.id.txtLayanan, EditText.class).setText("Jenis Kendaraan : " + result.get(0).get("LAYANAN").asString());
                find(R.id.txtNamaPelanggan, EditText.class).setText("Nama Pelanggan : " + result.get(0).get("NAMA_PELANGGAN").asString());
                find(R.id.txtNoPol, EditText.class).setText("Nama Usaha : " + result.get(0).get("NAMA_USAHA").asString());
                find(R.id.txtJamAntrian, EditText.class).setText("Nama Usaha : " + result.get(0).get("NAMA_USAHA").asString());
                find(R.id.txtMekanikSA, EditText.class).setText("Nama Usaha : " + result.get(0).get("NAMA_USAHA").asString());
            }
        });
    }


    private void initToolbar() {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            //toolbar.setNavigationIcon(R.drawable.ic_menu);
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("PENGAMBILAN PART");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void reload(final String nama) {
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();


                args.put("nama", nama);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrl("tugaspart.php"), args));

            }

            @Override
            public void runUI() {
                if (result.isNsonArray()) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.asArray());
                    recyclerView.getAdapter().notifyDataSetChanged();
                }
            }
        });
    }

    private void initComponent() {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        List<People> items = DataGenerator.getPeopleData(this);
        items.addAll(DataGenerator.getPeopleData(this));
        items.addAll(DataGenerator.getPeopleData(this));

       /* //set data and list adapter
        mAdapter = new AdapterListBasic(this, items);
        recyclerView.setAdapter(mAdapter);

        // on item list clicked
        mAdapter.setOnItemClickListener(new AdapterListBasic.OnItemClickListener() {
            @Override
            public void onItemClick(View view, People obj, int position) {
                Snackbar.make(parent_view, "Item " + obj.name + " clicked", Snackbar.LENGTH_SHORT).show();
            }
        });*/

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_daftar_tugas_part) {
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {

                viewHolder.find(R.id.txtLayanan, TextView.class).setText(nListArray.get(position).get("LAYANAN").asString());

                viewHolder.find(R.id.txtNama, TextView.class).setText(nListArray.get(position).get("NAMA").asString());

                viewHolder.find(R.id.txtNoPol, TextView.class).setText(nListArray.get(position).get("NO_POL").asString());

                viewHolder.find(R.id.txtJamAntrian, TextView.class).setText("Jam Antrian : " + nListArray.get(position).get("JAM_ANTRIAN").asString());

                viewHolder.find(R.id.txtMekanik, TextView.class).setText("Mekanik : " + nListArray.get(position).get("MEKANIK_SA").asString());

            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
                //Toast.makeText(getActivity(),"HHHHH "+position, Toast.LENGTH_SHORT).show();
                /*Intent intent =  new Intent(getActivity(), ControlLayanan.class);
                intent.putExtra("ID", nListArray.get(position).get("MEKANIK").asInteger());
                intent.putExtra("DATA", nListArray.get(position).toJson());
                startActivityForResult(intent, REQUEST_CONTROL);*/

                //Snackbar.make(parent_view, "Item  "+position+"  clicked", Snackbar.LENGTH_SHORT).show();

//                Intent intent = new Intent(getActivity(), PengambilanPartActivity.class);
//                intent.putExtra("DATA", nListArray.get(position).toJson());
//                startActivityForResult(intent, REQUEST_PENGAMBILAN_PART);
            }
        }));

        reload("");
    }


        public void barcode(){
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result ;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("custid", UtilityAndroid.getSetting(getApplicationContext(),"CID",""));
                args.put("email", UtilityAndroid.getSetting(getApplicationContext(),"EMA",""));
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrl("barcode.php"),args)) ;

            }

            @Override
            public void runUI() {


                setResult(RESULT_OK);

                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_BARCODE && resultCode == RESULT_OK){
            String barcode = getIntentStringExtra(data, "TEXT");
            barcode();
//        } else if (requestCode == REQUEST_PENGAMBILAN_PART && resultCode == RESULT_OK) {
//                Intent intent = new Intent();
//                intent.putExtra("DATA", getIntentStringExtra(data, "DATA"));
//                setResult(RESULT_OK, intent);
//                finish();
        }
    }
}
