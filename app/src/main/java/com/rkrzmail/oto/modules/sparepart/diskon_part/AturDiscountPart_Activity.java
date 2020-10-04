package com.rkrzmail.oto.modules.sparepart.diskon_part;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.layanan.discount_layanan.DiscountLayanan_Activity;
import com.rkrzmail.oto.modules.sparepart.CariPart_Activity;
import com.rkrzmail.srv.MultiSelectionSpinner;
import com.rkrzmail.srv.PercentFormat;
import com.rkrzmail.utils.Tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.rkrzmail.utils.APIUrls.ATUR_DISKON_PART;
import static com.rkrzmail.utils.ConstUtils.DATA;

public class AturDiscountPart_Activity extends AppActivity implements View.OnClickListener {

    private static final int REQUEST_CARI_PART = 10;
    private MultiSelectionSpinner spPekerjaan;
    private EditText etDiscPart, etDiscJasa, etNoPart, etNamaPart;
    private TextView tvTgl;
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

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Discount Part");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        spPekerjaan = findViewById(R.id.sp_pekerjaan_discPart);
        etDiscPart = findViewById(R.id.et_discPart_discPart);
        etDiscJasa = findViewById(R.id.et_discJasa_discPart);
        etNoPart = findViewById(R.id.et_noPart_discPart);
        etNamaPart = findViewById(R.id.et_namaPart_discPart);
        tvTgl = findViewById(R.id.tv_tglEffect_discPart);
        loadData();
        find(R.id.cb_bengkel_discPart, CheckBox.class).setOnCheckedChangeListener(listener);
        find(R.id.cb_tenda_discPart, CheckBox.class).setOnCheckedChangeListener(listener);
        setMultiSelectionSpinnerFromApi(spPekerjaan, "nama", "PEKERJAAN", "viewmst", new MultiSelectionSpinner.OnMultipleItemsSelectedListener() {
            @Override
            public void selectedIndices(List<Integer> indices) {

            }

            @Override
            public void selectedStrings(List<String> strings) {

            }
        }, "PEKERJAAN", "");

        tvTgl.setOnClickListener(this);
        etDiscJasa.addTextChangedListener(new PercentFormat(etDiscJasa));
        etDiscPart.addTextChangedListener(new PercentFormat(etDiscPart));

        find(R.id.btn_search, ImageButton.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), CariPart_Activity.class);
                i.putExtra("bengkel", "");
                startActivityForResult(i, REQUEST_CARI_PART);
            }
        });
    }

    private void loadData() {
        Intent i = getIntent();
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
        if (i.hasExtra("data")) {
            etDiscJasa.setText(nson.get("DISCOUNT_JASA_PASANG").asString());
            etDiscPart.setText(nson.get("DISCOUNT_PART").asString());
            etNamaPart.setText(nson.get("NAMA_PART").asString());
            etNoPart.setText(nson.get("NO_PART").asString());
            //spPekerjaan.setSelection(nson.get("PEKERJAAN").asStringArray(), true);
            tvTgl.setText(nson.get("TANGGAL").asString());
            Log.d("DISC___", "id: " + nson.get("ID"));
            Log.d("DISC___", "flagTenda: " + flagTenda + "\n" + "flagBengkel : " + flagBengkel + "\n" + "flagMssg : " + flagMssg);
            find(R.id.cb_bengkel_discPart, CheckBox.class).setChecked(flagBengkel);
            find(R.id.cb_tenda_discPart, CheckBox.class).setChecked(flagTenda);
            find(R.id.cb_mssg_discPart, CheckBox.class).setChecked(flagMssg);
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
                    if(spPekerjaan.getSelectedItemsAsString().isEmpty()){
                        showWarning("Silahkan Pilih Pekerjaan");
                        spPekerjaan.performClick();
                        return;
                    }
                    if(etDiscPart.getText().toString().isEmpty() || etDiscJasa.getText().toString().isEmpty()){
                        showWarning("Silahkan Isi Discount");
                        return;
                    }

                    try {
                        Date tglSekarang = new SimpleDateFormat("dd/MM/yyyy").parse(currentDateTime("dd/MM/yyyy"));
                        Date tglEffective = new SimpleDateFormat("dd/MM/yyyy").parse(tvTgl.getText().toString());
                        if (tglEffective.before(tglSekarang)) {
                            showWarning("Tanggal Invalid");
                            tvTgl.setText("");
                            tvTgl.performClick();
                            return;
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    if (tvTgl.getText().toString().isEmpty()) {
                        showWarning("Tanggal Effective Harus Di isi");
                        return;
                    }
                    updateData(nson);
                }
            });
        } else {
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
                    if(spPekerjaan.getSelectedItemsAsString().isEmpty()){
                        showWarning("Silahkan Pilih Pekerjaan");
                        spPekerjaan.performClick();
                        return;
                    }
                    if(etDiscPart.getText().toString().isEmpty() || etDiscJasa.getText().toString().isEmpty()){
                        showWarning("Silahkan Isi Discount");
                        return;
                    }

                    try {
                        Date tglSekarang = new SimpleDateFormat("dd/MM/yyyy").parse(currentDateTime("dd/MM/yyyy"));
                        Date tglEffective = new SimpleDateFormat("dd/MM/yyyy").parse(tvTgl.getText().toString());
                        if (tglEffective.before(tglSekarang)) {
                            showWarning("Tanggal Invalid");
                            tvTgl.setText("");
                            tvTgl.performClick();
                            return;
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    if (tvTgl.getText().toString().isEmpty()) {
                        showWarning("Tanggal Effective Harus Di isi");
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
                args.put("tanggal", Tools.setFormatDayAndMonthToDb(tvTgl.getText().toString()));
                args.put("pekerjaan", spPekerjaan.getSelectedItemsAsString());
                args.put("namapart", etNamaPart.getText().toString());
                args.put("nopart", etNoPart.getText().toString());
                args.put("partid", String.valueOf(partId));
                args.put("diskonpart", etDiscPart.getText().toString());
                args.put("diskonjasa", etDiscJasa.getText().toString());
                args.put("pesan", find(R.id.cb_mssg_discPart, CheckBox.class).isChecked() ? "YA" : "TIDAK");
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
                args.put("tanggal", Tools.setFormatDayAndMonthToDb(tvTgl.getText().toString()));
                args.put("pekerjaan", spPekerjaan.getSelectedItemsAsString());
                args.put("diskonpart", etDiscPart.getText().toString());
                args.put("diskonjasa", etDiscJasa.getText().toString());
                args.put("pesan", find(R.id.cb_mssg_discPart, CheckBox.class).isChecked() ? "YA" : "TIDAK");
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_tglEffect_discPart:
                getDatePickerDialogTextView(getActivity(), tvTgl);
                break;
        }
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
