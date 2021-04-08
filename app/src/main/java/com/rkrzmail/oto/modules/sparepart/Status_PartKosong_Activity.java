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
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.ATUR_TUGAS_PART;
import static com.rkrzmail.utils.APIUrls.VIEW_TUGAS_PART;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.REQUEST_CONTACT;

public class Status_PartKosong_Activity extends AppActivity {

    Spinner spStatus, spTipeSupplier;

    private String
            nopol = "", noAntrian = "",
            namaPelanggan = "", status = "", layanan = "",
            jamAntrian = "", partId ="", group = "",
            idCheckinDetail = "", idPartKosong = "", jumlahRequest = "";

    private boolean isTerima = false;
    private boolean isOrder = false;
    private boolean isBatal = false;
    private boolean isKosong = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_part_kosong);
        initToolbar();
        initComponent();
    }

    @SuppressLint("NewApi")
    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Check-In");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent(){
        spStatus = findViewById(R.id.sp_status);
        spTipeSupplier = findViewById(R.id.sp_tipe_supplier);
        setSpinnerOffline(Arrays.asList(getResources().getStringArray(R.array.status_part_kosong)), spStatus, "");
        setSpinnerOffline(Arrays.asList(getResources().getStringArray(R.array.tipe_supplier_2)), spTipeSupplier, "");
        initListener();
        initData();
    }

    private void initData(){
        Nson nson = Nson.readJson(getIntentStringExtra(DATA));
        idCheckinDetail = nson.get("CHECKIN_DETAIL_ID").asString();
        nopol = nson.get("NOPOL").asString();
        noAntrian = nson.get("NO_ANTRIAN").asString();
        namaPelanggan = nson.get("NAMA_PELANGGAN").asString();
        layanan =  nson.get("LAYANAN").asString();
        status =  nson.get("STATUS").asString();//status checkin
        jamAntrian = nson.get("ESTIMASI_SEBELUM").asString();//estimasi mulai checkin
        partId = nson.get("LAYANAN").asString();
        idPartKosong = nson.get("PART_KOSONG_ID").asString();
        jumlahRequest = nson.get("JUMLAH_PART_KOSONG").asString();
    }

    private void initListener(){
        spStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getSelectedItem().toString();
                if(item.equals("ORDER")){
                    spTipeSupplier.setEnabled(true);
                    find(R.id.tv_estimasi).setEnabled(true);
                    find(R.id.tv_nama_supplier).setBackground(getResources().getDrawable(R.drawable.background_edittext));
                    find(R.id.tv_estimasi).setBackground(getResources().getDrawable(R.drawable.background_edittext));
                    isOrder = true;
                }else{
                    spTipeSupplier.setEnabled(false);
                    find(R.id.tv_estimasi).setEnabled(false);
                    find(R.id.tv_nama_supplier).setBackground(getResources().getDrawable(R.drawable.bg_disable));
                    find(R.id.tv_estimasi).setBackground(getResources().getDrawable(R.drawable.bg_disable));
                    isOrder = false;
                }

                switch (item) {
                    case "TERIMA":
                        group = "TERIMA";
                        break;
                    case "BATAL KARENA BENGKEL":
                        group = "BATAL";
                        break;
                    case "TIDAK TERSEDIA":
                        group = "KOSONG";
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spTipeSupplier.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getSelectedItem().toString();
                if(item.equals("ECOMMERCE")){
                    find(R.id.et_nama_ecommerce).setEnabled(true);
                }else{
                    find(R.id.et_nama_ecommerce).setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        
        find(R.id.tv_estimasi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDatePicker();
            }
        });

        find(R.id.tv_nama_supplier).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("IntentReset")
            @Override
            public void onClick(View v) {
                try {
                    Uri uri = Uri.parse("content://contacts");
                    Intent intent = new Intent(Intent.ACTION_PICK, uri);
                    intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                    startActivityForResult(intent, REQUEST_CONTACT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        find(R.id.btn_simpan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(spStatus.getSelectedItem().toString().equals("--PILIH--")){
                    setErrorSpinner(spStatus, "STATUS HARUS DI PILIH");
                }else if(isOrder && spTipeSupplier.getSelectedItem().toString().equals("--PILIH--")){
                    setErrorSpinner(spTipeSupplier, "Tipe Supplier Belum di Pilih");
                }else if(isOrder && spTipeSupplier.getSelectedItem().toString().equals("ECOMMERCE") &&  find(R.id.et_nama_ecommerce, EditText.class).getText().toString().isEmpty()){
                    find(R.id.et_nama_ecommerce, EditText.class).setError("NAMA ECOMMERCE HARUS DI PILIH");
                    viewFocus(find(R.id.et_nama_ecommerce, EditText.class));
                }else if(isOrder && find(R.id.tv_estimasi, TextView.class).getText().toString().isEmpty()){
                    find(R.id.tv_estimasi, TextView.class).setError("TANGGAL ESTIMASI HARUS DI PILIH");
                    viewFocus(find(R.id.tv_estimasi, TextView.class));
                }else{
                    updatePartKosong();
                }
            }
        });
    }

    public void getDatePicker() {
        final Calendar cldr = Calendar.getInstance();
        final int day = cldr.get(Calendar.DAY_OF_MONTH);
        final int month = cldr.get(Calendar.MONTH);
        final int year = cldr.get(Calendar.YEAR);
        DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                String newDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                Date date = null;
                try {
                    date = sdf.parse(newDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                String formattedTime = sdf.format(date);
                find(R.id.tv_estimasi, TextView.class).setText(formattedTime);
            }
        }, year, month, day);

        datePickerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
               
            }
        });

        datePickerDialog.setMinDate(cldr);
        datePickerDialog.show(getFragmentManager(), "Datepickerdialog");
    }

    private void updatePartKosong(){
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "add");
                args.put("nopol", nopol);
                args.put("noAntrian", noAntrian);
                args.put("namaPelanggan", namaPelanggan);
                args.put("status", spStatus.getSelectedItem().toString());
                args.put("layanan", layanan);
                args.put("jamAntrian", jamAntrian);
                args.put("partId", partId);
                args.put("tanggal", currentDateTime("yyyy-MM-dd hh:mm"));
                args.put("group", group);

                switch (group) {
                    case "TERIMA":
                        args.put("isTersedia", "true");
                        args.put("jumlahRequest", jumlahRequest);
                        break;
                    case "BATAL KARENA BENGKEL":
                        args.put("isBatal", "true");
                        break;
                    case "TIDAK TERSEDIA":

                        break;
                }

                args.put("idPartKosong", idPartKosong);
                args.put("idCheckinDetail", idCheckinDetail);
                args.put("namaSupplier", find(R.id.tv_nama_supplier, TextView.class).getText().toString().replaceAll("[^a-zA-Z]", ""));
                args.put("noSupplier",  find(R.id.tv_nama_supplier, TextView.class).getText().toString().replaceAll("[^0-9]", ""));
                args.put("tipeSupplier", spTipeSupplier.getSelectedItem().toString());
                args.put("estimasiTiba", find(R.id.tv_estimasi, TextView.class).getText().toString());
                args.put("namaEcommerce", find(R.id.et_nama_ecommerce, EditText.class).getText().toString());

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(ATUR_TUGAS_PART), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Berhasil Menyimpan Aktivitas");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError(result.get("message").asString());
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == REQUEST_CONTACT){
            Uri contactData = null;
            if (data != null) {
                contactData = data.getData();
            }
            Cursor cursor = managedQuery(contactData, null, null, null, null);
            cursor.moveToFirst();
            String number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
            String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            find(R.id.tv_nama_supplier, TextView.class).setText(contactName + "\n" + number);
        }
    }
}