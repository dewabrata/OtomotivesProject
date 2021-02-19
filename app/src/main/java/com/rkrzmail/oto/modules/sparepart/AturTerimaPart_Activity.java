package com.rkrzmail.oto.modules.sparepart;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.rkrzmail.oto.modules.bengkel.AturRekening_Activity;
import com.rkrzmail.srv.NumberFormatUtils;
import com.rkrzmail.utils.Tools;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.rkrzmail.utils.APIUrls.SET_REKENING_BANK;
import static com.rkrzmail.utils.ConstUtils.REQUEST_CONTACT;
import static com.rkrzmail.utils.ConstUtils.REQUEST_REKENING;


public class AturTerimaPart_Activity extends AppActivity implements View.OnClickListener {

    private static final String TAG = "AturTerimaPart";
    public static final int MIN_SPINNER = 2;
    private Spinner spinnerSupplier, spinnerPembayaran, spRek;
    private TextView tvTglPesan, tvTglTerima, tvTglJatuhTempo, tvNamaSupplier;
    private EditText txtNoDo, txtOngkosKirim;
    private Button btnSelanjutnya;
    private List<String> data = new ArrayList<>();
    private String tglPesan, tglTerima;
    private boolean flagValidation = false;
    private DialogInterface dialogInterface;
    private ArrayAdapter<String> spRekAdapter;
    final long[] tglPesanTimeMilis = {0};

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
        getSupportActionBar().setTitle("Terima Part");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkTgl();
    }

    private void initComponent() {
        spRek = findViewById(R.id.sp_rekAsal_terimaPart);
        spinnerSupplier = findViewById(R.id.spinnerSupplier);
        spinnerPembayaran = findViewById(R.id.spinnerPembayaran);
        tvNamaSupplier = findViewById(R.id.txtNamaSupplier);
        txtNoDo = findViewById(R.id.txtNoDo);
        txtOngkosKirim = findViewById(R.id.txtOngkosKirim);
        tvTglPesan = findViewById(R.id.tglPesan);
        tvTglTerima = findViewById(R.id.tglTerima);
        tvTglJatuhTempo = findViewById(R.id.jatuhTempo);
        btnSelanjutnya = findViewById(R.id.btnSelanjutnya);
        tvTglPesan.setOnClickListener(this);
        tvTglTerima.setOnClickListener(this);
        tvTglJatuhTempo.setOnClickListener(this);
        tvNamaSupplier.setOnClickListener(this);

        setSpRek();
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
                    find(R.id.ly_rek, LinearLayout.class).setVisibility(View.GONE);
                } else if (item.equalsIgnoreCase("TRANSFER")) {
                    find(R.id.ly_rek, LinearLayout.class).setVisibility(View.VISIBLE);
                    if(spRek.getCount() < MIN_SPINNER){
                        showInfoDialog("Rekening Belum Di tambah, Tambah Rekening ? ", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialogInterface = dialog;
                                startActivityForResult(new Intent(getActivity(), AturRekening_Activity.class), REQUEST_REKENING);
                            }
                        });
                    }
                    find(R.id.layout_jatuh_tempo).setVisibility(View.GONE);
                }else if(item.equals("KONSIGNMENT")){
                    showWarning("TIPE PEMBAYARAN BELUM AKTIF");
                    spinnerPembayaran.post(new Runnable() {
                        @Override
                        public void run() {
                            spinnerPembayaran.performClick();
                        }
                    });
                } else {
                    find(R.id.ly_rek, LinearLayout.class).setVisibility(View.GONE);
                    find(R.id.layout_jatuh_tempo).setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        txtOngkosKirim.addTextChangedListener(new NumberFormatUtils().rupiahTextWatcher(txtOngkosKirim));

        find(R.id.btnSelanjutnya, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
                String tglSekarang = simpleDateFormat.format(calendar.getTime());
                final String tglpesan = tvTglPesan.getText().toString();
                final String tglterima = tvTglTerima.getText().toString();

                if (spinnerSupplier.getSelectedItem().toString().equalsIgnoreCase("--PILIH--")) {
                    showWarning("Supplier Harus Di Pilih");
                    spinnerSupplier.requestFocus();
                    return;
                }
                if (find(R.id.ly_namaSup_terimaPart, LinearLayout.class).getVisibility() == View.VISIBLE) {
                    if (tvNamaSupplier.getText().toString().isEmpty()) {
                        tvNamaSupplier.setError("Supplier Tidak Boleh Kosong");
                        tvNamaSupplier.requestFocus();
                        return;
                    }
                }
                if (txtNoDo.getText().toString().isEmpty()) {
                    txtNoDo.setError("No. DO Tidak Boleh Kosong");
                    txtNoDo.requestFocus();
                    return;
                }
                if (spinnerPembayaran.getSelectedItem().toString().equalsIgnoreCase("--PILIH--")) {
                    showWarning("Pembayaran Harus Di Pilih");
                    spinnerSupplier.requestFocus();
                    return;
                }
                if (find(R.id.ly_rek, LinearLayout.class).getVisibility() == View.VISIBLE) {
                    if (spRek.getSelectedItem().toString().equalsIgnoreCase("--PILIH--")) {
                        spRek.requestFocus();
                        showWarning("Nomor Rekening Harus di Pilih");
                        return;
                    }
                }
//                if (txtOngkosKirim.getText().toString().isEmpty()) {
//                    txtOngkosKirim.setError("Masukkan Ongkos Kirim");
//                    txtOngkosKirim.requestFocus();
//                    return;
//                }
                if (find(R.id.layout_jatuh_tempo).getVisibility() == View.VISIBLE) {
                    if (tvTglJatuhTempo.getText().toString().isEmpty()) {
                        tvTglJatuhTempo.setError("Masukkan Tanggal Jatuh Tempo");
                        tvTglJatuhTempo.requestFocus();
                        return;
                    }
                }
                if (tglpesan.equalsIgnoreCase("TANGGAL PESAN")) {
                    showWarning("Masukkan Tanggal Pesan");
                    return;
                }
                if (tglterima.equalsIgnoreCase("TANGGAL TERIMA")) {
                    showWarning("Masukkan Tanggal Terima");
                    return;
                }
                try {
                    @SuppressLint("SimpleDateFormat") Date tanggalTerima = new SimpleDateFormat("dd/MM/yyyy").parse(tglterima);
                    @SuppressLint("SimpleDateFormat") Date pesan = new SimpleDateFormat("dd/MM/yyyy").parse(tglpesan);
                    if (tanggalTerima.before(pesan)) {
                        showWarning("Tanggal Pesan Tidak Boleh Melebihi Tanggal Terima");
                        return;
                    } else if (pesan.after(tanggalTerima)) {
                        showWarning("Tanggal Terima Tidak Boleh Melebihi Tanggal Pesan");
                        return;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                try {
                    @SuppressLint("SimpleDateFormat") Date tanggalTerima = new SimpleDateFormat("dd/MM/yyyy").parse(tglterima);
                    @SuppressLint("SimpleDateFormat") Date currentDate = new SimpleDateFormat("dd/MM/yyyy").parse(tglSekarang);
                    if (tanggalTerima.after(currentDate)) {
                        showWarning("Tanggal Terima Tidak Boleh Melebihi Tanggal Sekarang");
                        return;
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }

                try {
                    @SuppressLint("SimpleDateFormat") Date jatuhTempo = new SimpleDateFormat("dd/MM/yyyy").parse(tvTglJatuhTempo.getText().toString());
                    @SuppressLint("SimpleDateFormat") Date tanggalTerima2 = new SimpleDateFormat("dd/MM/yyyy").parse(tglterima);
                    if (find(R.id.layout_jatuh_tempo).getVisibility() == View.VISIBLE) {
                        if (!jatuhTempo.after(tanggalTerima2) && !tanggalTerima2.before(jatuhTempo)) {
                            showWarning("Tanggal Jatuh Tempo Invoice / Tanggal Terima Tidak Sesuai");
                            return;
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                try {
                    @SuppressLint("SimpleDateFormat") Date sekarang = new SimpleDateFormat("dd/MM/yyyy").parse(tglSekarang);
                    @SuppressLint("SimpleDateFormat") Date pesan = new SimpleDateFormat("dd/MM/yyyy").parse(tglpesan);
                    if (pesan.after(sekarang)) {
                        showWarning("Tanggal Pesan Tidak Boleh Melebihi Tanggal Sekarang");
                        return;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (data.contains(tglPesan)) {
                    showInfoDialog("Penerimaan Part Telah Tercatat Sebelumnya", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    return;
                }
                if (data.contains(tglTerima)) {
                    showInfoDialog("Penerimaan Part Telah Tercatat Sebelumnya", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                        }
                    });
                    return;
                }

                Intent i = new Intent(AturTerimaPart_Activity.this, AturDetail_TerimaPart_Activity.class);
                i.putExtra("detail", sendObject().toJson());
                startActivityForResult(i, TerimaPart_Activity.REQUEST_TERIMA_PART);
            }
        });
    }

    public void setSpRek() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(SET_REKENING_BANK), args));
            }

            @Override
            public void runUI() {
                ArrayList<String> str = new ArrayList<>();
                str.add("--PILIH--");
                for (int i = 0; i < result.get("data").size(); i++) {
                    str.add(result.get("data").get(i).get("BANK_NAME").asString() + " - " + result.get("data").get(i).get("NO_REKENING").asString());
                }
                ArrayList<String> newStr = Tools.removeDuplicates(str);
                spRekAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, newStr);
                spRekAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                if(spRekAdapter.getCount() < 1){
                    showWarning("Data Belum di Set");
                }
                spRek.setAdapter(spRekAdapter);
                spRekAdapter.notifyDataSetChanged();
            }
        });
    }

    private void checkTgl() {
        newTask(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args2 = AppApplication.getInstance().getArgsData();
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("viewterimapart"), args2));
            }

            @Override
            public void runUI() {
                for (int i = 0; i < result.get("data").size(); i++) {
                    data.add(result.get("data").get(i).get("TANGGAL_PESAN").asString());
                    data.add(result.get("data").get(i).get("TANGGAL_PENERIMAAN").asString());
                }
            }
        });
    }

    private Nson sendObject() {
        String tglpesan = Tools.setFormatDayAndMonthToDb(tvTglPesan.getText().toString());
        String tglterima =  Tools.setFormatDayAndMonthToDb(tvTglTerima.getText().toString());
        String rek = spRek.getSelectedItem().toString();
        String jatuhtempo =  Tools.setFormatDayAndMonthToDb(tvTglJatuhTempo.getText().toString());
        String tipe = spinnerSupplier.getSelectedItem().toString().toUpperCase();
        String nama = tvNamaSupplier.getText().toString().toUpperCase();
        String nodo = txtNoDo.getText().toString().toUpperCase();
        String ongkir = NumberFormatUtils.formatOnlyNumber(txtOngkosKirim.getText().toString());
        String pembayaran = spinnerPembayaran.getSelectedItem().toString().toUpperCase();

        Nson nson = Nson.newObject();
        nson.set("nodo", nodo);
        nson.set("tglpesan", tglpesan);
        nson.set("tglterima", tglterima);
        nson.set("ongkir", ongkir);
        nson.set("pembayaran", pembayaran);
        nson.set("jatuhtempo", jatuhtempo);
        nson.set("namaSupplier", nama.replaceAll("[^a-zA-Z]", ""));
        nson.set("noSupplier", nama.replaceAll("[^0-9]", ""));
        nson.set("tipe", tipe);
        nson.set("rek", rek);
        nson.set("rekening", spRek.getSelectedItem().toString());

        showInfo("Catatkan Detail Part");
        return nson;
    }

    private String getAllNumberFromString(String nama){
        if(nama == null) return "";
        StringBuilder noSupplier = new StringBuilder();
        for(char number : nama.toCharArray()){
            if(Character.isDigit(number)){
                noSupplier.append(number);
            }
        }
        return noSupplier.toString().replaceAll("[^0-9]+", "");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tglPesan:
                final Calendar cldr = Calendar.getInstance();
                final int day = cldr.get(Calendar.DAY_OF_MONTH);
                final int month = cldr.get(Calendar.MONTH);
                final int year = cldr.get(Calendar.YEAR);
                DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                        String newDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                        Date date = null;
                        try {
                            date = sdf.parse(newDate);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        String formattedTime = sdf.format(date);
                        tglPesanTimeMilis[0] = date != null ? date.getTime() : 0;
                        tvTglPesan.setText(formattedTime);
                    }
                }, year, month, day);
                datePickerDialog.show(getFragmentManager(), "Datepickerdialog");
                break;
            case R.id.tglTerima:
                if(tglPesanTimeMilis[0] > 0){
                    getDatePickerDialogTextView(getActivity(), tvTglTerima, parseTglPesan(tglPesanTimeMilis[0]));
                }else{
                    showWarning("Tanggal Pesan Belum di Input");
                }

                break;
            case R.id.jatuhTempo:
                getDatePickerDialogTextView(getActivity(), tvTglJatuhTempo);
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

    private Calendar parseTglPesan(long tglPesan){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(tglPesan);
        return calendar;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TerimaPart_Activity.REQUEST_TERIMA_PART && resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            finish();
        } else if (requestCode == REQUEST_CONTACT) {
            if (resultCode == RESULT_OK) {
                Uri contactData = data != null ? data.getData() : null;
                Cursor cursor = managedQuery(contactData, null, null, null, null);
                cursor.moveToFirst();
                String number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                tvNamaSupplier.setText(contactName + "\n" + number);
            }
        }else if(resultCode == RESULT_OK && requestCode == REQUEST_REKENING){
            dialogInterface.dismiss();
            setSpRek();
        }
    }

    private void dialog() {

    }
}