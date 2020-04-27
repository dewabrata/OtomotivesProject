package com.rkrzmail.oto.gmod;

import android.app.TimePickerDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TimePicker;

import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NikitaAutoComplete;

import java.util.Calendar;

public class Penugasan_Activity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener {

    private NikitaAutoComplete namaMekanik;
    private ProgressBar pbPenugasan;
    private  RadioGroup statusPenugasan;
    private Spinner lokasiPenugasan, tipeAntrian;
    private Button simpan, hapus;
    private CheckBox homePenugasan, emergencyPenugasan;
    private LinearLayout layoutExternal;
    private RadioButton onKerja, offKerja;
    private EditText mulaiKerja, selesaiKerja, mulaiIstirahat, selesaiIstirahat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_penugasan_);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Penugasan Mekanik");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        namaMekanik =  findViewById(R.id.tv_namaMekanik);
        pbPenugasan =  findViewById(R.id.pb_penugasan);
        statusPenugasan = findViewById(R.id.rgPenugasan);
        onKerja = findViewById(R.id.rbOn);
        offKerja = findViewById(R.id.rbOff);
        lokasiPenugasan = findViewById(R.id.sp_lokasi);
        tipeAntrian = findViewById(R.id.sp_antrian);
        homePenugasan = findViewById(R.id.cb_home);
        emergencyPenugasan = findViewById(R.id.cb_emergency);
        mulaiKerja = findViewById(R.id.et_mulaiKerja);
        selesaiKerja = findViewById(R.id.et_selesaiKerja);
        mulaiIstirahat = findViewById(R.id.et_mulaistirahat);
        selesaiIstirahat = findViewById(R.id.et_selesaistirahat);
        layoutExternal = findViewById(R.id.layout_external);
        simpan = findViewById(R.id.btn_simpan);
        hapus = findViewById(R.id.btn_hapus);

        simpan.setOnClickListener(this);
        hapus.setOnClickListener(this);
        mulaiKerja.setOnClickListener(this);
        selesaiKerja.setOnClickListener(this);
        mulaiIstirahat.setOnClickListener(this);
        selesaiIstirahat.setOnClickListener(this);

        lokasiPenugasan.setOnItemSelectedListener(this);

    }

    private void setData(){

        int selectedId = statusPenugasan.getCheckedRadioButtonId();
        String nama = namaMekanik.getText().toString();
        String antrian = tipeAntrian.getSelectedItem().toString().trim();
        String lokasi = lokasiPenugasan.getSelectedItem().toString().trim();
        String status = onKerja.getText().toString();
        String tidakKerja = offKerja.getText().toString();
        String emergency = emergencyPenugasan.getText().toString();
        String home = homePenugasan.getText().toString();

        if(nama.isEmpty()){
            namaMekanik.setError("Silahkan Di isi");
        }

        switch (selectedId){
            case R.id.rbOn:

                break;
            case R.id.rbOff:

                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String item = parent.getItemAtPosition(position).toString();
        if(item.equalsIgnoreCase("Tenda")){
            setViewAndChildrenEnabled(layoutExternal, false);
        }else if(item.equalsIgnoreCase("Bengkel")){
            setViewAndChildrenEnabled(layoutExternal, true);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

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

    @Override
    public void onClick(View view) {

        switch (view.getId()){

            case R.id.et_mulaiKerja:
                getDateTime(mulaiKerja);
                break;
            case R.id.et_selesaiKerja:
                getDateTime(selesaiKerja);
                break;
            case R.id.et_mulaistirahat:
                getDateTime(mulaiIstirahat);
                break;
            case R.id.et_selesaistirahat:
                getDateTime(selesaiIstirahat);
                break;
            case R.id.btn_simpan:

                break;
            case R.id.btn_hapus:

                break;

        }
    }

    private void getDateTime(final EditText editText) {

        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(Penugasan_Activity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
                editText.setText(hourOfDay + ":" + minutes);
            }
        }, currentHour, currentMinute, true);

        timePickerDialog.show();
    }
}
