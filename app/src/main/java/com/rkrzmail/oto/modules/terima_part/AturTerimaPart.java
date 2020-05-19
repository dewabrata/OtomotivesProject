package com.rkrzmail.oto.modules.terima_part;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Selection;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.lokasi_part.AturLokasiPart_Activity;
import com.rkrzmail.oto.modules.lokasi_part.LokasiPart_Activity;
import com.rkrzmail.oto.modules.penugasan.AturPenugasan_Activity;
import com.rkrzmail.oto.modules.penugasan.PenugasanActivity;
import com.rkrzmail.utils.Tools;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;


public class AturTerimaPart extends AppActivity implements View.OnClickListener {

    private static final String TAG = "AturTerimaPart";
    private static final int REQUEST_ATUR_TERIMA_PART = 4141;
    private Spinner spinnerSupplier, spinnerPembayaran;
    private TextView tglPesan, tglTerima, tglJatuhTempo;
    private EditText txtNoDo, txtNamaSupplier, txtOngkosKirim;
    private Button btnSelanjutnya;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_terima_part);

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

        spinnerSupplier = findViewById(R.id.spinnerSupplier);
        spinnerPembayaran = findViewById(R.id.spinnerPembayaran);
        txtNamaSupplier = findViewById(R.id.txtNamaSupplier);
        txtNoDo = findViewById(R.id.txtNoDo);
        txtOngkosKirim = findViewById(R.id.txtOngkosKirim);
        tglPesan = findViewById(R.id.tglPesan);
        tglTerima = findViewById(R.id.tglTerima);
        tglJatuhTempo = findViewById(R.id.tglJatuhTempo);
        btnSelanjutnya = findViewById(R.id.btnSelanjutnya);

        tglPesan.setOnClickListener(this);
        tglTerima.setOnClickListener(this);
        tglJatuhTempo.setOnClickListener(this);

        spinnerSupplier.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                String item = parent.getItemAtPosition(position).toString();
                if (item.equalsIgnoreCase("Principal")) {
                    Tools.setViewAndChildrenEnabled(find(R.id.layout_nama_supplier), false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spinnerPembayaran.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                String item = parent.getItemAtPosition(position).toString();
                if (item.equalsIgnoreCase("Invoice")) {
                    Tools.setViewAndChildrenEnabled(find(R.id.layout_jatuh_tempo), false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        find(R.id.btnSelanjutnya, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                insertData();
            }
        });
    }



    private void insertData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                final Map<String, String> args = AppApplication.getInstance().getArgsData();

                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy");
                String dateTime = simpleDateFormat.format(calendar.getTime());

                String tglpesan = tglPesan.getText().toString();
                String tglterima = tglTerima.getText().toString();
                String jatuhtempo = tglJatuhTempo.getText().toString();
                String tipe = find(R.id.txtTipeSupplier, Spinner.class).getSelectedItem().toString().toUpperCase();
                String nama = find(R.id.txtNamaSupplier, EditText.class).getText().toString().toUpperCase();
                String nodo = find(R.id.txtNoDo, EditText.class).getText().toString().toUpperCase();
                String ongkir = find(R.id.txtOngkosKirim, EditText.class).getText().toString().toUpperCase();
                String pembayaran = find(R.id.txtPembayaran, Spinner.class).getSelectedItem().toString().toUpperCase();

                args.put("tglpesan", tglpesan);
                args.put("tglterima", tglterima);
                args.put("jatuhtempo", jatuhtempo);
                args.put("tipe", tipe);
                args.put("nama", nama);
                args.put("nodo", nodo);
                args.put("ongkir", ongkir);
                args.put("pembayaran", pembayaran);
                args.put("tanggal", dateTime);

               result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturterimapart"), args));

            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    Log.d(TAG, result.get("status").asString());
                    startActivity(new Intent(AturTerimaPart.this, DetailPartDiterima.class));
                    finish();
                } else {
                    showError(result.get("status").asString());
                    Log.d(TAG, "error");
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RESULT_OK && resultCode == REQUEST_ATUR_TERIMA_PART){
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tglPesan:

        }
    }
}
