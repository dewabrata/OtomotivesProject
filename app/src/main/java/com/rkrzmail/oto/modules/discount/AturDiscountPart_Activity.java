package com.rkrzmail.oto.modules.discount;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.sparepart.CariPart_Activity;
import com.rkrzmail.srv.NumberFormatUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.ATUR_DISKON_PART;
import static com.rkrzmail.utils.ConstUtils.CARI_PART_BENGKEL;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.REQUEST_CARI_PART;

public class AturDiscountPart_Activity extends AppActivity{

    private EditText etDiscPart, etDiscJasa, etNoPart, etNamaPart;
    private Spinner spPekerjaan;
    private List<String> listChecked = new ArrayList<>();
    private boolean flagTenda = false, flagBengkel = false, flagMssg = false;
    private String lokasi = "";
    private int partId;
    private static final String ERROR = "Harus Di isi";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_discount_part_);
        initToolbar();
        initComponent();
    }

    @SuppressLint("NewApi")
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Discount Part");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        spPekerjaan = findViewById(R.id.sp_pekerjaan_discPart);
        etDiscPart = findViewById(R.id.et_discPart_discPart);
        etDiscJasa = findViewById(R.id.et_discJasa_discPart);
        etNoPart = findViewById(R.id.et_noPart_discPart);
        etNamaPart = findViewById(R.id.et_namaPart_discPart);

        loadData();

        find(R.id.cb_bengkel_discPart, CheckBox.class).setOnCheckedChangeListener(listener);
        find(R.id.cb_tenda_discPart, CheckBox.class).setOnCheckedChangeListener(listener);

        etDiscJasa.addTextChangedListener(new NumberFormatUtils().percentTextWatcher(etDiscJasa));
        etDiscPart.addTextChangedListener(new NumberFormatUtils().percentTextWatcher(etDiscPart));

        find(R.id.btn_search, ImageButton.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), CariPart_Activity.class);
                i.putExtra(CARI_PART_BENGKEL, "");
                startActivityForResult(i, REQUEST_CARI_PART);
            }
        });
    }

    private void loadData() {
        Intent i = getIntent();
        List<String> statusList = Arrays.asList(getResources().getStringArray(R.array.aktif_tidak_aktif));
        final Nson nson = Nson.readJson(getIntentStringExtra(DATA));

        if (nson.get("LOKASI").asString().equalsIgnoreCase("TENDA") && nson.get("LOKASI").asString().equalsIgnoreCase("BENGKEL")) {
            flagTenda = true;
            flagBengkel = true;
        } else if (nson.get("LOKASI").asString().equalsIgnoreCase("TENDA")) {
            flagTenda = true;
        } else if (nson.get("LOKASI").asString().equalsIgnoreCase("BENGKEL")) {
            flagBengkel = true;
        }
        if (nson.get("MESSAGE_PELANGGAN").asString().equalsIgnoreCase("YA")) {
            flagMssg = true;
        }
        if (i.hasExtra(DATA)) {
            setSpinnerFromApi(spPekerjaan, "nama", "PEKERJAAN", "viewmst", "PEKERJAAN", nson.get("PEKERJAAN").asString());
            setSpinnerOffline(statusList, find(R.id.sp_status, Spinner.class), nson.get("STATUS").asString());
            etDiscJasa.setText(nson.get("DISCOUNT_JASA_PASANG").asString());
            etDiscPart.setText(nson.get("DISCOUNT_PART").asString());
            etNamaPart.setText(nson.get("NAMA_PART").asString());
            etNoPart.setText(nson.get("NO_PART").asString());
            Log.d("DISC___", "id: " + nson.get("ID"));
            Log.d("DISC___", "flagTenda: " + flagTenda + "\n" + "flagBengkel : " + flagBengkel + "\n" + "flagMssg : " + flagMssg);
            find(R.id.cb_bengkel_discPart, CheckBox.class).setChecked(flagBengkel);
            find(R.id.cb_tenda_discPart, CheckBox.class).setChecked(flagTenda);
            find(R.id.btn_hapus_discPart, Button.class).setVisibility(View.VISIBLE);
            find(R.id.btn_hapus_discPart, Button.class).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteData(nson);
                }
            });
            find(R.id.btn_simpan_discPart, Button.class).setText("UPDATE");
            find(R.id.btn_simpan_discPart, Button.class).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listChecked.size() == 0){
                        find(R.id.cb_bengkel_discPart, CheckBox.class).requestFocus();
                        showWarning("Silahkan Pilih Lokasi");
                        return;
                    }
                    if(etNoPart.getText().toString().isEmpty()){
                        etNoPart.setError(ERROR);
                        return;
                    }
                    if(etNamaPart.getText().toString().isEmpty()){
                        etNamaPart.setError(ERROR);
                        return;
                    }
                    if(spPekerjaan.getSelectedItem().toString().equals("--PILIH--")){
                        showWarning("Silahkan Pilih Pekerjaan");
                        spPekerjaan.performClick();
                        return;
                    }
                    if(etDiscPart.getText().toString().isEmpty() || etDiscJasa.getText().toString().isEmpty()){
                        showWarning("Silahkan Isi Discount");
                        return;
                    }

                    updateData(nson);
                }
            });
        } else {
            setSpinnerFromApi(spPekerjaan, "nama", "PEKERJAAN", "viewmst", "PEKERJAAN");
            setSpinnerOffline(statusList, find(R.id.sp_status, Spinner.class), "");
            find(R.id.btn_simpan_discPart, Button.class).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(listChecked.size() == 0){
                        find(R.id.cb_bengkel_discPart, CheckBox.class).requestFocus();
                        showWarning("Silahkan Pilih Lokasi");
                        return;
                    }
                    if(etNoPart.getText().toString().isEmpty()){
                        etNoPart.setError(ERROR);
                        return;
                    }
                    if(etNamaPart.getText().toString().isEmpty()){
                        etNamaPart.setError(ERROR);
                        return;
                    }
                    if(spPekerjaan.getSelectedItem().toString().equals("--PILIH--")){
                        showWarning("Silahkan Pilih Pekerjaan");
                        spPekerjaan.performClick();
                        return;
                    }
                    if(etDiscPart.getText().toString().isEmpty() || etDiscJasa.getText().toString().isEmpty()){
                        showWarning("Silahkan Isi Discount");
                        return;
                    }

                    saveData();
                }
            });
        }
    }


    private void saveData() {
        for (String lok : listChecked) {
            lokasi += lok;
        }
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "add");
                args.put("status", find(R.id.sp_status, Spinner.class).getSelectedItem().toString());
                args.put("pekerjaan", spPekerjaan.getSelectedItem().toString());
                args.put("namapart", etNamaPart.getText().toString());
                args.put("nopart", etNoPart.getText().toString());
                args.put("partid", String.valueOf(partId));
                args.put("diskonpart", etDiscPart.getText().toString());
                args.put("diskonjasa", etDiscJasa.getText().toString());
                args.put("lokasi", lokasi);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(ATUR_DISKON_PART), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Sukses Menambahkan Diskon Part");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError("Gagal Menambahkan Diskon Part");
                }
            }
        });
    }

    private void updateData(final Nson id) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "update");
                args.put("id", id.get("ID").asString());
                args.put("status", find(R.id.sp_status, Spinner.class).getSelectedItem().toString());
                args.put("pekerjaan", spPekerjaan.getSelectedItem().toString());
                args.put("diskonpart", etDiscPart.getText().toString());
                args.put("diskonjasa", etDiscJasa.getText().toString());
                for (String lok : listChecked) {
                    args.put("lokasi", lok);
                }

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(ATUR_DISKON_PART), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Sukses Menyimpan Aktifitas");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError("Gagal Menyimpan Aktifitas");
                }
            }
        });
    }

    private void deleteData(final Nson id) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "delete");
                args.put("id", id.get("ID").asString());
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(ATUR_DISKON_PART), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Berhasil Menghapus Aktifitas");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError("Gagal Menyimpan Aktifitas");
                }
            }
        });
    }

    CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                listChecked.add(buttonView.getText().toString());
            } else {
                listChecked.remove(buttonView.getText().toString());
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CARI_PART) {
            Nson n = Nson.readJson(getIntentStringExtra(data, "part"));
            etNoPart.setText(n.get("NO_PART").asString());
            etNamaPart.setText(n.get("NAMA_PART").asString());
            partId = n.get("PART_ID").asInteger();
        }
    }
}
