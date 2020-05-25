package com.rkrzmail.oto.modules.terima_part;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
        tglJatuhTempo = findViewById(R.id.jatuhTempo);
        btnSelanjutnya = findViewById(R.id.btnSelanjutnya);

        tglPesan.setOnClickListener(this);
        tglTerima.setOnClickListener(this);
        tglJatuhTempo.setOnClickListener(this);

        spinnerSupplier.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                String item = parent.getItemAtPosition(position).toString();
                if (item.equalsIgnoreCase("PRINCIPAL")) {
                    // Tools.setViewAndChildrenEnabled(find(R.id.layout_nama_supplier), false);
                }else {
                    //Tools.setViewAndChildrenEnabled(find(R.id.layout_nama_supplier), true);
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
                if (item.equalsIgnoreCase("INV")) {
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

        final String tglpesan = tglPesan.getText().toString();
        final String tglterima = tglTerima.getText().toString();
        final String jatuhtempo = tglJatuhTempo.getText().toString();
        final String tipe = spinnerSupplier.getSelectedItem().toString().toUpperCase();
        final String nama = txtNamaSupplier.getText().toString().toUpperCase();
        final String nodo = txtNoDo.getText().toString().toUpperCase();
        final String ongkir = txtOngkosKirim.getText().toString().toUpperCase();
        final String pembayaran = spinnerPembayaran.getSelectedItem().toString().toUpperCase();

        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            Nson result2;
            @Override
            public void run() {
                final Map<String, String> args = AppApplication.getInstance().getArgsData();
                final Map<String, String> args2 = AppApplication.getInstance().getArgsData();
                args.put("tglpesan", tglpesan);
                args.put("tglterima", tglterima);
                args.put("jatuhtempo", jatuhtempo);
                args.put("tipe", tipe);
                args.put("nama", nama);
                args.put("nodo", nodo);
                args.put("ongkir", ongkir);
                args.put("pembayaran", pembayaran);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturterimapart"), args));
               result2 = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("viewterimapart"), args2));

            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    Log.d(TAG, result.get("status").asString());
                    List<String> data = new ArrayList<>();
                    for (int i = 0; i < result2.get("data").size(); i++) {
                        data.add(result2.get("data").get(i).get("TANGGAL_PESAN").asString());
                        data.add(result2.get("data").get(i).get("TANGGAL_PENERIMAAN").asString());

                    }
                    if (data.contains(tglpesan) && data.contains(tglterima)) {
                        alertDialog();
                    } else {
                        startActivity(new Intent(AturTerimaPart.this, TerimaPart.class));
                        finish();
                    }
                } else {
                    showError(result.get("status").asString());
                    Log.d(TAG, "error");
                }
            }
        });
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tglPesan:
                Tools.getDatePickerDialogTextView(getActivity(),tglPesan);
                break;
            case R.id.tglTerima:
                Tools.getDatePickerDialogTextView(getActivity(),tglTerima);
                break;
            case R.id.jatuhTempo:
                Tools.getDatePickerDialogTextView(getActivity(),tglJatuhTempo);
                break;
        }
    }
    private void alertDialog(){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
        builder1.setMessage("Penerimaan Part Telah Tercatat Sebelumnya");
        builder1.setCancelable(true);
        builder1.setPositiveButton(
                "Edit",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert11 = builder1.create();
        alert11.show();
    }
}
