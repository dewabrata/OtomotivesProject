package com.rkrzmail.oto.modules.lokasi_part.stock_opname;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.gmod.BarcodeActivity;
import com.rkrzmail.oto.modules.lokasi_part.LokasiPart_Activity;
import com.rkrzmail.oto.modules.lokasi_part.Penyesuain_Activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class StockOpname_Activity extends AppActivity {

    private static final int REQUEST_BARCODE_STOCK_OPNAME = 12;
    private static final int REQUEST_PENYESUAIAN = 567;
    private EditText noFolder, noPart, jumlahData, jumlahAkhir, namaPart;
    private ImageView imgBarcode;
    private ArrayList<String> indexOf_Opname = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_opname);
        initToolbar();

        noFolder = findViewById(R.id.et_noFolder_stockOpname);
        noPart = findViewById(R.id.et_noPart_stockOpname);
        jumlahData = findViewById(R.id.et_jumlahdata_stockOpname);
        jumlahAkhir = findViewById(R.id.et_jumlahakhir_stockOpname);
        namaPart = findViewById(R.id.et_namaPart_stockOpname);
        imgBarcode = findViewById(R.id.imgBarcode_stockOpname);

        initComponent();
        final Nson data = Nson.readJson(getIntentStringExtra("NO_PART_ID"));
        Intent i = getIntent();
        if(i.hasExtra("NO_PART_ID")){
            noFolder.setText(data.get("NO_FOLDER").asString());
            noPart.setText(data.get("NO_PART_ID").asString());
            namaPart.setText(data.get("NAMA").asString());
            jumlahData.setText(data.get("STOCK").asString());

            indexOf_Opname.add(data.get("NO_FOLDER").asString());
            indexOf_Opname.add(data.get("NO_PART_ID").asString());
            indexOf_Opname.add(data.get("STOCK").asString());
            indexOf_Opname.add(data.get("PENEMPATAN").asString());
            indexOf_Opname.add(data.get("PALET").asString());
            indexOf_Opname.add(data.get("RAK").asString());
            indexOf_Opname.add(data.get("USER").asString());
            indexOf_Opname.add(data.get("LOKASI").asString());


            for(int in = 0; in < indexOf_Opname.size(); in++){
                Log.d("OPNAME", indexOf_Opname.get(in));
            }
        }



    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_stock_opname);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Stock Opname");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent(){
        find(R.id.btn_simpan_stockOpname, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                saveUpdate();
            }
        });

        imgBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(getActivity(), BarcodeActivity.class);
                startActivityForResult(i, REQUEST_BARCODE_STOCK_OPNAME);
            }
        });
    }

    private void saveUpdate(){

        final int stockAwal = Integer.parseInt(jumlahData.getText().toString());
        final int stockAkhir = Integer.parseInt(jumlahAkhir.getText().toString());
        final String lokasi = indexOf_Opname.get(7);
        final String tempat = indexOf_Opname.get(3);
        final String palet = indexOf_Opname.get(4);
        final String rak = indexOf_Opname.get(5);
        final String folder = indexOf_Opname.get(0);
        final String user = indexOf_Opname.get(6);
        final String nopart = indexOf_Opname.get(1);
        final String stock = indexOf_Opname.get(2);

        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy MMM dd");
                String tanggal = simpleDateFormat.format(calendar.getTime());

                args.put("action", "add");
                args.put("lokasi", lokasi);
                args.put("tempat", tempat);
                args.put("palet", palet);
                args.put("rak", rak);
                args.put("folder", folder);
                args.put("tanggal", tanggal);
                args.put("user", user);
                args.put("nopart", nopart);
                args.put("stock", stock);


                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturlokasipart"), args));

            }

            @Override
            public void runUI() {
                if(result.get("status").asString().equalsIgnoreCase("OK")){

                    int stockBeda = 0;
                    if(stockAwal > stockAkhir){
                        stockBeda = stockAwal - stockAkhir;

                        Intent i = new Intent(getActivity(), Penyesuain_Activity.class);
                        i.putExtra("NO_FOLDER", folder);
                        i.putExtra("NO_PART_ID", nopart);
                        i.putExtra("STOCK", stock);
                        i.putExtra("LOKASI", lokasi);
                        i.putExtra("USER", user);
                        i.putExtra("PENEMPATAN", tempat);
                        i.putExtra("PALET", palet);
                        i.putExtra("RAK", rak);
                        i.putExtra("STOCK_BEDA", stockBeda);
                        startActivityForResult(i, REQUEST_PENYESUAIAN);
                        Toast.makeText(getActivity(), "Diperlukan Penyesuaian", Toast.LENGTH_LONG).show();
                        finish();
                    }else{
                        startActivity(new Intent(getActivity(), LokasiPart_Activity.class));
                        Toast.makeText(getActivity(), "Sukses Opname Part", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
            }
        });
    }

    private void getBarcode(){
        newProses(new Messagebox.DoubleRunnable() {
            @Override
            public void run() {

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
        if(requestCode == REQUEST_BARCODE_STOCK_OPNAME && resultCode == RESULT_OK){
            String barcode = getIntentStringExtra(data, "TEXT");
            getBarcode();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
}
