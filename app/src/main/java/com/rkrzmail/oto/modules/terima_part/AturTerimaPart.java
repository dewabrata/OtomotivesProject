package com.rkrzmail.oto.modules.terima_part;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.utils.Tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;


public class AturTerimaPart extends AppActivity implements View.OnClickListener {

    private static final String TAG = "AturTerimaPart";
    public static final int REQUEST_DETAIL_PART = 90;
    public static final int REQUEST_CONTACT = 91;
    private Spinner spinnerSupplier, spinnerPembayaran;
    private TextView tglPesan, tglTerima, tglJatuhTempo, txtNamaSupplier;
    private EditText txtNoDo, txtOngkosKirim;
    private Button btnSelanjutnya;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_terima_part);

        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("ATUR TERIMA PART");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {


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
        txtNamaSupplier.setOnClickListener(this);

        spinnerSupplier.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                String item = parent.getItemAtPosition(position).toString();
                if (item.equalsIgnoreCase("PRINCIPAL")) {
                    find(R.id.ly_namaSup_terimaPart).setVisibility(View.GONE);
                } else {
                    find(R.id.ly_namaSup_terimaPart).setVisibility(View.VISIBLE);
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
                if (item.equalsIgnoreCase("INVOICE")) {
                    find(R.id.layout_jatuh_tempo).setVisibility(View.VISIBLE);
                } else {
                    find(R.id.layout_jatuh_tempo).setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        txtOngkosKirim.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    txtOngkosKirim.setText("Rp. ");
                }
            }
        });

        find(R.id.btnSelanjutnya, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Date jatuhTempo = new SimpleDateFormat("dd/MM/yyyy").parse(String.valueOf(tglJatuhTempo));
                    Date tanggalTerima = new SimpleDateFormat("dd/MM/yyyy").parse(String.valueOf(tglTerima));
                    if (find(R.id.layout_jatuh_tempo).isEnabled()) {
                        if (!jatuhTempo.after(tanggalTerima) && !tanggalTerima.before(jatuhTempo)) {
                            showInfo("Tanggal Jatuh Tempo Invoice / Tanggal Terima Tidak Sesuai");
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (tglTerima.getText().toString().equalsIgnoreCase("TANGGAL TERIMA")) {
                    showInfo("Masukkan Tanggal Terima");
                } else if (tglPesan.getText().toString().equalsIgnoreCase("TANGGAL PESAN")) {
                    showInfo("Masukkan Tanggal PESAN");
                } else {
                    setBtnSelanjutnya();
                }

            }
        });
    }


    private void setBtnSelanjutnya() {

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

            @Override
            public void run() {
                Map<String, String> args2 = AppApplication.getInstance().getArgsData();
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("viewterimapart"), args2));
            }

            @Override
            public void runUI() {
                Nson nson = Nson.newObject();
                nson.set("nodo", nodo);
                nson.set("tglpesan", tglpesan);
                nson.set("tglterima", tglterima);
                nson.set("ongkir", ongkir);
                nson.set("pembayaran", pembayaran);
                nson.set("jatuhtempo", jatuhtempo);
                nson.set("nama", nama);
                nson.set("tipe", tipe);

                ArrayList<String> data = new ArrayList<>();
                for (int i = 0; i < result.get("data").size(); i++) {
                    data.add(result.get("data").get(i).get("TANGGAL_PESAN").asString());
                    data.add(result.get("data").get(i).get("TANGGAL_PENERIMAAN").asString());
                }
                if (data.contains(tglpesan)) {
                    Tools.alertDialog(getActivity(), "Penerimaan Part Telah Tercatat Sebelumnya");
                } else if (data.contains(tglterima)) {
                    Tools.alertDialog(getActivity(), "Penerimaan Part Telah Tercatat Sebelumnya");
                } else {
                    Intent i = new Intent(AturTerimaPart.this, DetailPartDiterima.class);
                    i.putExtra("detail", nson.toJson());
                    startActivityForResult(i, REQUEST_DETAIL_PART);
                    finish();
                }
            }
        });
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tglPesan:
                Tools.getDatePickerDialogTextView(getActivity(), tglPesan);
                break;
            case R.id.tglTerima:
                Tools.getDatePickerDialogTextView(getActivity(), tglTerima);
                break;
            case R.id.jatuhTempo:
                Tools.getDatePickerDialogTextView(getActivity(), tglJatuhTempo);
                break;
            case R.id.txtNamaSupplier:
//                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
//                startActivityForResult(intent, REQUEST_CONTACT);
                try {
                    Uri uri = Uri.parse("content://contacts");
                    Intent intent = new Intent(Intent.ACTION_PICK, uri);
                    intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                    startActivityForResult(intent, REQUEST_CONTACT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_DETAIL_PART && resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            finish();
        }
        if (requestCode == REQUEST_CONTACT) {
            if (resultCode == RESULT_OK) {
                Uri contactData = data.getData();
                Cursor cursor = managedQuery(contactData, null, null, null, null);
                cursor.moveToFirst();
                String number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                txtNamaSupplier.setText(contactName + "\n" + number);
            }
        }
    }
}

//    private void getAddMorePart(){
//        Nson nson = Nson.readNson(getIntentStringExtra("tambah"));
//        Intent intent = getIntent();
//        if(intent.hasExtra("tambah")){
//            tglPesan.setText(nson.get("tglpesan").asString());
//            tglTerima.setText(nson.get("tglterima").asString());
//            spinnerSupplier.setSelection(Tools.getIndexSpinner(spinnerSupplier, nson.get("LOKASI").asString()));
//        }
//    }

