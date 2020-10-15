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

import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.REQUEST_CONTACT;

public class StatusPartKosong_Activity extends AppActivity {

    Spinner spStatus, spTipeSupplier;
    private boolean isOrder = false;

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
        viewStatusPartKosong();
        setSpinnerOffline(Arrays.asList(getResources().getStringArray(R.array.status_part_kosong)), spStatus, "");
        setSpinnerOffline(Arrays.asList(getResources().getStringArray(R.array.tipe_supplier_2)), spTipeSupplier, "");
        initListener();
        initData();
    }

    private void initData(){
        Nson data = Nson.readJson(getIntentStringExtra(DATA));
    }

    private void viewStatusPartKosong(){
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "delete");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturdiskonjasalain"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {

                } else {
                    showError(result.get("message").asString());
                }
            }
        });
    }

    private void initListener(){
        spStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getSelectedItem().toString();
                if(item.equals("ORDER")){
                    spTipeSupplier.setEnabled(true);
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_nama_supplier, LinearLayout.class), true);
                    find(R.id.tv_tanggal).setEnabled(true);
                    isOrder = true;
                }else{
                    spTipeSupplier.setEnabled(false);
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_nama_supplier, LinearLayout.class), false);
                    find(R.id.tv_tanggal).setEnabled(false);
                    isOrder = false;
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
        
        find(R.id.tv_tanggal).setOnClickListener(new View.OnClickListener() {
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
                    spStatus.requestFocus();
                    spStatus.performClick();
                    showWarning("Status Belum di Pilih");
                }else if(isOrder && spTipeSupplier.getSelectedItem().toString().equals("--PILIH--")){
                    spTipeSupplier.requestFocus();
                    spTipeSupplier.performClick();
                    showWarning("Tipe Supplier Belum di Pilih");
                }else if(isOrder && spTipeSupplier.getSelectedItem().toString().equals("ECOMMERCE") &&  find(R.id.et_nama_ecommerce, EditText.class).getText().toString().isEmpty()){
                    find(R.id.et_nama_ecommerce, EditText.class).setError("Nama Ecommerce Harus Di Pilih");
                }else if(isOrder && find(R.id.tv_tanggal, TextView.class).getText().toString().isEmpty()){
                    showWarning("Tanggal Belum Di Masukkan");
                    find(R.id.tv_tanggal, TextView.class).performClick();
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
                find(R.id.tv_tanggal, TextView.class).setText(formattedTime);
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
                args.put("action", "delete");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturdiskonjasalain"), args));
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