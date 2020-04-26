package com.rkrzmail.oto.gmod;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Spinner;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.data.UtilityAndroid;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.part.PartActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class Pendaftaran2 extends AppActivity {
    final int REQUEST_PENDAFTARAN = 123;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pendaftaran2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setTitle("PENDAFTARAN");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        find(R.id.img).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Get Current Date
                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                        new DatePickerDialog.OnDateSetListener() {
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {

                                //txtDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });
        find(R.id.tblLanjut).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Nson nson = Nson.readNson(getIntentStringExtra("dx"));
                nson.set("norangka", find(R.id.txtNoRangka, EditText.class).getText().toString());
                nson.set("nomesin", find(R.id.txtNoMesin, EditText.class).getText().toString());
                nson.set("tglbeli", find(R.id.txtTglBeli, EditText.class).getText().toString());

                Intent intent = new Intent(getActivity(), Pendaftaran3.class);
                intent.putExtra("dx", nson.toJson());

                startActivityForResult(intent, REQUEST_PENDAFTARAN);
            }
        });

        pekerjaan();
    }

     private void pekerjaan () {
     newProses(new Messagebox.DoubleRunnable() {
       Nson result;
       public void run() {
       Map<String, String> args = AppApplication.getInstance().getArgsData();
       //konek pekerjaan atau diskon.php
       result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrl("pekerjaan.php"), args));
         if (!result.isNsonArray()) {
         result = Nson.newArray();
         }
         // jenis pekerjaan ?
         result.add(Nson.readNson("{'NAMA_PEKERJAAN' : 'PEKERJAAN ACTIVE', 'MODE':'HCODE'}"));
         //result.add(Nson.readNson("{'NAMA_LAYANAN':'PERBAIKAN RINGAN','MODE':'HCODE'}"));
         }

       public void runUI() {
       Spinner spinner = find(R.id.spnPekerjaan);
       ArrayAdapter<List> adapter = new ArrayAdapter<List>(getActivity(), android.R.layout.simple_spinner_dropdown_item, result.asArray() ){
          public View getView(int position, @Nullable View convertView, ViewGroup parent) {
          View v =  super.getView(position, convertView, parent);
           if (v instanceof TextView){
           ((TextView) v).setText( result.get(position).get("NAMA_PEKERJAAN").asString());
           }
           return v;
           }
          public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
          View v =  super.getView(position, convertView, parent);
           if (v instanceof TextView){
           ((TextView) v).setText( result.get(position).get("NAMA_PEKERJAAN").asString());
           }
           return v;
          }
       };
       adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
       spinner.setAdapter(adapter);
       spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
           @Override
           public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (result.isNson()){
              if (result.size()>=1){
                 /*
                 find(R.id.txtNoRangka).setEnabled(false);
                 find(R.id.txtNoMesin).setEnabled(false);
                 find(R.id.txtTglBeli).setEnabled(false);
                  */

                  //No Rangka, No Mesin, Tgl Beli
                  find(R.id.txtNoRangka,EditText.class).setText(result.get(0).get("NORANGKA").asString());
                  find(R.id.txtNoMesin,EditText.class).setText(result.get(0).get("NOMESIN").asString());
                  find(R.id.txtTglBeli,EditText.class).setText(result.get(0).get("TGLBELI").asString());

                  return;
              }
            }
//               if (position==result.size()-1){
//                   find(R.id.tblPerbaikanRingan).setVisibility(View.VISIBLE);
//                   find(R.id.tblService).setVisibility(View.GONE);
//               }else{
//                   find(R.id.tblPerbaikanRingan).setVisibility(View.GONE);
//                   find(R.id.tblService).setVisibility(View.VISIBLE);
//               }
           }

           @Override
           public void onNothingSelected(AdapterView<?> adapterView) {

           }
       });
       }
     });

        find(R.id.tblLanjut).requestFocus();
    }
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PENDAFTARAN && resultCode == RESULT_OK){
            setResult(RESULT_OK);
            finish();
        }
    }
}
