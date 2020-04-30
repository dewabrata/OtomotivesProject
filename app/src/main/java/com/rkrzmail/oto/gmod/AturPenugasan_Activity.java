package com.rkrzmail.oto.gmod;

import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TimePicker;

import com.naa.data.Nson;
import com.naa.data.UtilityAndroid;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.penugasan.PenugasanActivity;

import java.util.Calendar;
import java.util.Map;

public class AturPenugasan_Activity extends AppActivity implements View.OnClickListener {

    public static final String TAG = "AturPenugasan_Activity";
    private EditText etNama_Mekanik, etMulai_Kerja, etSelesai_Kerja, etMulai_istirahat, etSelesai_istirahat;
    private RadioGroup rg_status;
    private Spinner spTipe_antrian, spLokasi;
    private CheckBox cbHome, cbEmergency;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

//        outState.putInt("sp_lokasi", find(R.id.sp_lokasi, Spinner.class).getSelectedItemPosition());
//        outState.putInt("sp_antrian", find(R.id.sp_antrian, Spinner.class).getSelectedItemPosition());
//
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_penugasan_);

        initToolbar();
        etNama_Mekanik = findViewById(R.id.et_namaMekanik);
        etMulai_Kerja = findViewById(R.id.et_mulaiKerja);
        etSelesai_Kerja = findViewById(R.id.et_selesaiKerja);
        etMulai_istirahat = findViewById(R.id.et_mulaistirahat);
        etSelesai_istirahat = findViewById(R.id.et_selesaistirahat);
        rg_status = findViewById(R.id.rg_status);
        spTipe_antrian = findViewById(R.id.sp_antrian);
        spLokasi = findViewById(R.id.sp_lokasi);
        cbHome = findViewById(R.id.cb_home);
        cbEmergency = findViewById(R.id.cb_emergency);

        etMulai_Kerja.setOnClickListener(this);
        etSelesai_Kerja.setOnClickListener(this);
        etMulai_istirahat.setOnClickListener(this);
        etSelesai_istirahat.setOnClickListener(this);
        initComponent();





//        if(savedInstanceState != null){
//            find(R.id.sp_lokasi, Spinner.class).setSelection(savedInstanceState.getInt("sp_lokasi", 0));
//            find(R.id.sp_antrian, Spinner.class).setSelection(savedInstanceState.getInt("sp_antrain", 0));
//        }
//
            final Nson data = Nson.readJson(getIntentStringExtra("data"));

//        if (getIntentStringExtra("data") != null) {
//            getSupportActionBar().setTitle("Edit Penugasan Mekanik");




            newProses(new Messagebox.DoubleRunnable() {
                Nson result;
                @Override
                public void run() {
                    Map<String, String> args = AppApplication.getInstance().getArgsData();
                    //args.put("action", "get");//view
                    //args.put("id", data.get("id").asString());//view
                    args.put("id", getIntentStringExtra("id"));//view

                    result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturpenugasanmekanik"), args));
                }

                @Override
                public void runUI() {
                    etNama_Mekanik.setText(result.get(0).get("namamekanik").asString());
                    etMulai_Kerja.setText(result.get("masuk").asString());
                    etSelesai_Kerja.setText(result.get(0).get("pulang").asString());
                    etMulai_istirahat.setText(result.get(0).get("istirahat").asString());
                    etSelesai_istirahat.setText(result.get(0).get("selesai").asString());

                    if(result.get(0).get("status").asString().equalsIgnoreCase("ON")){
                        find(R.id.rbOn, RadioButton.class).setChecked(true);
                    }
                    if(result.get(0).get("status").asString().equalsIgnoreCase("OFF")){
                        find(R.id.rbOff, RadioButton.class).setChecked(true);
                    }
                    if(result.get(0).get("external").asString().equalsIgnoreCase("HOME")){
                        cbHome.setChecked(true);
                    }
                    if(result.get(0).get("external").asString().equalsIgnoreCase("EMERGENCY")){
                        cbEmergency.setChecked(true);
                    }



                }
            });



    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.et_mulaiKerja:
                getDateTime(etMulai_Kerja);
                break;
            case R.id.et_selesaiKerja:
                getDateTime(etSelesai_Kerja);
                break;
            case R.id.et_mulaistirahat:
                getDateTime(etMulai_istirahat);
                break;
            case R.id.et_selesaistirahat:
                getDateTime(etSelesai_istirahat);
                break;
        }
    }

    private void initComponent() {

        find(R.id.sp_lokasi, Spinner.class).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                String item = parent.getItemAtPosition(position).toString();
                if (item.equalsIgnoreCase("Tenda")) {
                    setViewAndChildrenEnabled(find(R.id.layout_external), false);
                } else if (item.equalsIgnoreCase("Bengkel")) {
                    setViewAndChildrenEnabled(find(R.id.layout_external), true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        find(R.id.btn_simpan, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (etNama_Mekanik.getText().toString().equalsIgnoreCase("")) {
                    etNama_Mekanik.setError("Harus di isi");
                } else if (etMulai_Kerja.getText().toString().equalsIgnoreCase("")) {
                    find(R.id.et_mulaiKerja, EditText.class).setError("Harus di isi");
                } else if (etSelesai_Kerja.getText().toString().equalsIgnoreCase("")) {
                    etSelesai_Kerja.setError("Harus di isi");
                } else if (etMulai_istirahat.getText().toString().equalsIgnoreCase("")) {
                    etMulai_istirahat.setError("Harus di isi");
                } else if (etSelesai_istirahat.getText().toString().equalsIgnoreCase("")) {
                    etSelesai_istirahat.setError("Harus di isi");
                }

                saveData();

            }

        });

    }

    private void initToolbar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_penugasan);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Atur Penugasan Mekanik");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void saveData() {

        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                int selectedId = rg_status.getCheckedRadioButtonId();

                String nama = etNama_Mekanik.getText().toString().trim().toUpperCase();
                String antrian = spTipe_antrian.getSelectedItem().toString().toUpperCase();
                String lokasi = spLokasi.getSelectedItem().toString().toUpperCase();
                String masuk = etMulai_Kerja.getText().toString().trim();
                String selesai = etSelesai_Kerja.getText().toString().trim();
                String istirahat = etMulai_istirahat.getText().toString();
                String selesai_istirahat = etSelesai_istirahat.getText().toString();
                String home = cbHome.getText().toString();
                String emergency = cbEmergency.getText().toString();

//                String cid = "AHS700010100";
//                String action = "add";

//                args.put("CID", cid);
//                args.put("action", action);
                switch (selectedId){
                    case R.id.rbOn:
                        String statusKerja = find(R.id.rbOn, RadioButton.class).getText().toString();
                        args.put("status", statusKerja);
                        Log.d(TAG, statusKerja);
                        break;
                    case R.id.rbOff:
                        String statusTidakKerja = find(R.id.rbOff, RadioButton.class).getText().toString();
                        args.put("status", statusTidakKerja);
                        Log.d(TAG, statusTidakKerja);
                        break;
                }

                args.put("namamekanik", nama);
                args.put("antrian", antrian);
                args.put("lokasi", lokasi);
                if(cbHome.isChecked()){
                    args.put("external", home);
                    Log.d(TAG, home);
                }else if(cbEmergency.isChecked()){
                    args.put("external", emergency);
                    Log.d(TAG, emergency);
                }
                args.put("masuk", masuk);
                args.put("pulang", selesai);
                args.put("istirahat", istirahat);
                args.put("selesai", selesai_istirahat);


                Log.d(TAG, "put data");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrl("v3/aturpenugasanmekanik"), args));
                result.toJson().equalsIgnoreCase("data");

                //Log.i(TAG, InternetX.getHttpConnectionX(""));
//                http://otomotives.com/api/v3/aturpenugasanmekanik?CID=mekanik1


            }

            @Override
            public void runUI() {

                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    setResult(RESULT_OK);
                   // nListArray.asArray().addAll(result.get("data").asArray());
                    Log.d(TAG, "success");
                    startActivity(new Intent(AturPenugasan_Activity.this, PenugasanActivity.class));
                    finish();
                } else {
                    showError("Menambahkan data gagal!" + result.get("message").asString());
                    Log.d(TAG, "error");
                }
            }
        });
    }

    private void getActivityForResult() {

        MessageMsg.newTask(getActivity(), new Messagebox.DoubleRunnable() {
            @Override
            public void run() {


            }

            @Override
            public void runUI() {

                find(R.id.sp_lokasi, Spinner.class).setSelection(getIntentStringExtra("flag").indexOf(""));
                find(R.id.sp_antrian, Spinner.class).setSelection(getIntentStringExtra("flag").indexOf(""));

            }
        });
    }

    private static void setViewAndChildrenEnabled(View view, boolean enabled) {
        view.setEnabled(enabled);
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                setViewAndChildrenEnabled(child, enabled);
            }
        }
    }

    private void getDateTime(final EditText editText) {

        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(AturPenugasan_Activity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
                editText.setText(hourOfDay + ":" + minutes);
            }
        }, currentHour, currentMinute, true);

        timePickerDialog.show();
    }

}
