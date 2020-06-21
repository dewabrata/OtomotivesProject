package com.rkrzmail.oto.modules.sparepart.lokasi_part;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class Penyesuain_Activity extends AppActivity {

    private Spinner sp_penyesuaian, sp_noFolder_penyesuain;
    private TextInputEditText ket;
    private ArrayList<String> indexOf_Penyesuaian = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_penyesuain);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_penyesuaian);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Penyesuaian");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {

        sp_penyesuaian = findViewById(R.id.sp_penyesuaian);
        sp_noFolder_penyesuain = findViewById(R.id.sp_noFolder_penyesuaian);
        ket = findViewById(R.id.et_ket_penyesuaian);

        spNoFolder();
        sp_penyesuaian.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {

                String item = parent.getItemAtPosition(position).toString();
                if (item.equalsIgnoreCase("DI PINDAHKAN KE LOKASI LAIN")) {
                    sp_noFolder_penyesuain.setEnabled(true);
                } else {
                    sp_noFolder_penyesuain.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        find(R.id.btn_simpan_penyesuaian, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUpdate();
            }
        });

    }

    private void spNoFolder() {
        //No folder Spinner
        List<Integer> noFolder = new ArrayList<Integer>();
        for (int i = 1; i <= 100; i++) {
            noFolder.add(i);
        }
        ArrayAdapter<Integer> folderAdapter = new ArrayAdapter<Integer>(getActivity(), android.R.layout.simple_spinner_item, noFolder);
        folderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_noFolder_penyesuain.setAdapter(folderAdapter);
    }

    private void saveUpdate() {
        final Nson data = Nson.readJson(getIntentStringExtra("penyesuaian"));

        final String penyesuaian = sp_penyesuaian.getSelectedItem().toString().toUpperCase();
        String noFolder = null;
        if (sp_noFolder_penyesuain.isEnabled()) {
            noFolder = sp_noFolder_penyesuain.getSelectedItem().toString().toUpperCase();
        } else {
            noFolder = "";
        }
        final String keterangan = ket.getText().toString();
        final String lokasi = data.get("lokasi").asString();
        final String tempat = data.get("tempat").asString();
        final String palet = data.get("palet").asString();
        final String rak = data.get("rak").asString();
        final String folder = data.get("folder").asString();
        final String user = data.get("user").asString();
        final String nopart = data.get("nopart").asString();
        final String stock = data.get("stock").asString();
        final String stockBeda = data.get("stockbeda").asString();

        final String finalNoFolder = noFolder;

        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                // CID, action(update), lokasi, tempat, palet, rak, folder, tanggal, user, nopart, stock,
                // folderlain, stockbeda, sebab, alasan
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy MMM dd");
                String tanggal = simpleDateFormat.format(calendar.getTime());

                args.put("action", "update");
                args.put("lokasi", lokasi);
                args.put("tempat", tempat);
                args.put("palet", palet);
                args.put("rak", rak);
                args.put("folder", folder);
                args.put("tanggal", tanggal);
                args.put("user", user);
                args.put("nopart", nopart);
                args.put("stock", stock);
                args.put("stockbeda", stockBeda);
                args.put("folderlain", finalNoFolder);
                args.put("sebab", penyesuaian);
                args.put("alasan", keterangan);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturlokasipart"), args));

            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    Toast.makeText(getActivity(), "Sukses Memperbaharui Data", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(getActivity(), LokasiPart_Activity.class));
                    finish();
                } else {
                    showInfo("Gagal Memperbarui Data");
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
}
