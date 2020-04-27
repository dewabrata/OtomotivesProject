package com.rkrzmail.oto.gmod;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NikitaAutoComplete;

public class Penugasan_Activity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener {

    private NikitaAutoComplete namaMekanik;
    private ProgressBar pbPenugasan;
    private RadioGroup statusPenugasan;
    private Spinner lokasiPenugasan, tipeAntrian;
    private Button simpan, mulaiKerja, selesaiKerja, mulaiIstirahat, selesaiIstirahat, hapus;
    private CheckBox homePenugasan, emergencyPenugasan;
    private LinearLayout layoutExternal;
    private RadioButton onKerja, offKerja;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_penugasan_);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        setTitle("Penugasan Mekanik");

        namaMekanik = findViewById(R.id.tv_namaMekanik);
        pbPenugasan = findViewById(R.id.pb_penugasan);
        statusPenugasan = findViewById(R.id.rgPenugasan);
        onKerja = findViewById(R.id.rbOn);
        offKerja = findViewById(R.id.rbOff);
        lokasiPenugasan = findViewById(R.id.sp_lokasi);
        tipeAntrian = findViewById(R.id.sp_antrian);
        homePenugasan = findViewById(R.id.cb_home);
        emergencyPenugasan = findViewById(R.id.cb_emergency);
        mulaiKerja = findViewById(R.id.btn_mulaiKerja);
        selesaiKerja = findViewById(R.id.btn_selesaikerja);
        mulaiIstirahat = findViewById(R.id.btn_mulaistirahat);
        selesaiIstirahat = findViewById(R.id.btn_selesaistirahat);
        layoutExternal = findViewById(R.id.layout_external);
        simpan = findViewById(R.id.tblSimpan);
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
            case R.id.btn_mulaiKerja:

                break;
            case R.id.btn_selesaikerja:

                break;
            case R.id.btn_mulaistirahat:

                break;
            case R.id.btn_selesaistirahat:

                break;
            case R.id.btn_simpan:

                break;
            case R.id.btn_hapus:

                break;

        }
    }
}
