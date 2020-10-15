package com.rkrzmail.oto.modules.mekanik;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.bengkel.AturTenda_Activity;
import com.rkrzmail.oto.modules.bengkel.AturUser_Activity;
import com.rkrzmail.srv.MultiSelectionSpinner;
import com.rkrzmail.utils.Tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class AturPenugasan_Activity extends AppActivity implements View.OnClickListener {

    private static final String TAG = "AturPenugasan___";
    private static final int REQUEST_MEKANIK = 99;
    private static final int REQUEST_LOKASI = 100;
    private static final String ERROR = "Silahkan Pilih";
    private MultiSelectionSpinner spTipe_antrian;
    private TextView tvMulai_Kerja, tvSelesai_Kerja, tvMulai_istirahat, tvSelesai_istirahat;
    private RadioGroup rg_status;
    private Spinner spLokasi, spMekanik;
    private CheckBox cbHome, cbEmergency, cbInspection, cbBook;
    private Nson mekanikArray = Nson.newArray(), idMekanikArray = Nson.newArray(), lokasiArray = Nson.newArray(), penugasanList = Nson.newArray();
    private boolean isRefresh = false, isAntrian, isLokasi;
    private Handler handler;
    private String namaMekanik = "", lokasi = "", userId = "";
    private String[] tipeAntrian;
    private List<String> listChecked = new ArrayList<>(), listAntrian = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_penugasan_);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Atur Penugasan Mekanik");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @SuppressLint("SetTextI18n")
    private void initComponent() {
        spMekanik = findViewById(R.id.sp_namaMekanik);
        tvMulai_Kerja = findViewById(R.id.tv_mulaiKerja);
        tvSelesai_Kerja = findViewById(R.id.tv_selesaiKerja);
        tvMulai_istirahat = findViewById(R.id.tv_mulaistirahat);
        tvSelesai_istirahat = findViewById(R.id.tv_selesaistirahat);
        rg_status = findViewById(R.id.rg_status);
        spTipe_antrian = findViewById(R.id.sp_antrian);
        spLokasi = findViewById(R.id.sp_lokasi);
        cbHome = findViewById(R.id.cb_home);
        cbEmergency = findViewById(R.id.cb_emergency);
        cbInspection = findViewById(R.id.cb_inspection);
        cbBook = findViewById(R.id.cb_bookBengkel);

        cbHome.setOnCheckedChangeListener(listener);
        cbEmergency.setOnCheckedChangeListener(listener);
        cbInspection.setOnCheckedChangeListener(listener);
        cbBook.setOnCheckedChangeListener(listener);

        viewPenugasanMekanik();
        final Nson data = Nson.readJson(getIntentStringExtra("data"));
        namaMekanik = data.get("NAMA_MEKANIK").asString();
        lokasi = data.get("LOKASI").asString();
        if (data.get("TIPE_ANTRIAN").asString().contains(",")) {
            String[] antrian = data.get("TIPE_ANTRIAN").asString().split(", ");
            if (antrian.length > 0) {
                listAntrian.addAll(Arrays.asList(antrian));
            }
        }


        if (getIntent().hasExtra("data")) {
            isAntrian = true;
            isLokasi = true;
            spMekanik.setEnabled(false);

            find(R.id.btn_simpan, Button.class).setText("UPDATE");
            find(R.id.btn_simpan, Button.class).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateData(data);
                }
            });
            find(R.id.btn_hapus, Button.class).setVisibility(View.VISIBLE);
            find(R.id.btn_hapus, Button.class).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteData(data);
                }
            });
        } else {
            isAntrian = false;
            isLokasi = false;
            find(R.id.btn_simpan, Button.class).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String masuk = tvMulai_Kerja.getText().toString().trim();
                    String selesai = tvSelesai_Kerja.getText().toString().trim();
                    String istirahat = tvMulai_istirahat.getText().toString();
                    String selesai_istirahat = tvSelesai_istirahat.getText().toString();

                    if (spMekanik.getSelectedItem().toString().equalsIgnoreCase("--PILIH--")) {
                        spMekanik.performClick();
                        showWarning(ERROR + "Nama Mekanik");
                        return;
                    }
                    if (spTipe_antrian.getSelectedItem().toString().equalsIgnoreCase("--PILIH--")) {
                        spTipe_antrian.performClick();
                        showWarning(ERROR + "Tipe Antrian");
                        return;
                    }
                    if (spLokasi.getSelectedItem().toString().equalsIgnoreCase("--PILIH--")) {
                        spLokasi.performClick();
                        showWarning(ERROR + "Lokasi");
                        return;
                    }
//                if(listChecked.size() == 0){
//                    showWarning("");
//                }
                    if (tvMulai_Kerja.getText().toString().equalsIgnoreCase("MULAI")) {
                        showWarning(ERROR + "Waktu Mulai Kerja");
                        tvMulai_Kerja.performClick();
                        return;
                    }
                    if (tvSelesai_Kerja.getText().toString().equalsIgnoreCase("SELESAI")) {
                        showWarning(ERROR + "Waktu Selesai Kerja");
                        tvSelesai_Kerja.performClick();
                        return;
                    }
                    if (tvMulai_istirahat.getText().toString().equalsIgnoreCase("MULAI")) {
                        showWarning(ERROR + "Waktu Mulai Istirahat");
                        tvMulai_istirahat.performClick();
                        return;
                    }
                    if (tvSelesai_istirahat.getText().toString().equalsIgnoreCase("SELESAI")) {
                        showWarning(ERROR + "Waktu Selesai Istirahat");
                        tvSelesai_istirahat.performClick();
                        return;
                    }
                    try {
                        Date jamMasuk = new SimpleDateFormat("HH:mm").parse(masuk);
                        Date jamPulang = new SimpleDateFormat("HH:mm").parse(selesai);

                        if (!jamMasuk.before(jamPulang)) {
                            showInfo("Jam Selesai Kerja Tidak Sesuai / Jam Masuk Kerja Tidak Sesuai");
                            return;
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    try {
                        Date jamMulaiIstirahat = new SimpleDateFormat("HH:mm").parse(istirahat);
                        Date jamSelesaiIstirahat = new SimpleDateFormat("HH:mm").parse(selesai_istirahat);

                        if (!jamMulaiIstirahat.before(jamSelesaiIstirahat)) {
                            showInfo("Jam Selesai Istirahat Tidak Sesuai / Jam Masuk Istirahat Tidak Sesuai");
                            return;
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    insertData();
                }

            });
        }

        if (data.get("INSPECTION").asString().equalsIgnoreCase("Y")) {
            cbInspection.setChecked(true);
        }
        if (data.get("BOOK_BENGKEL").asString().equalsIgnoreCase("Y")) {
            cbBook.setChecked(true);
        }
        if (data.get("HOME").asString().equalsIgnoreCase("Y")) {
            cbHome.setChecked(true);
        }
        if (data.get("EMERGENCY").asString().equalsIgnoreCase("Y")) {
            cbEmergency.setChecked(true);
        }

        tvMulai_Kerja.setText(data.get("JAM_MASUK").asString());
        tvSelesai_Kerja.setText(data.get("JAM_PULANG").asString());
        tvMulai_istirahat.setText(data.get("JAM_ISTIRAHAT").asString());
        tvSelesai_istirahat.setText(data.get("JAM_ISTIRAHAT_SELESAI").asString());

        spMekanik.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                for (int i = 0; i < idMekanikArray.size(); i++) {
                    if (idMekanikArray.get(i).get("NAMA").asString().equalsIgnoreCase(item)) {
                        userId = idMekanikArray.get(i).get("ID").asString();
                        break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        setSpTipe_antrian();
        setSpMekanik();
        setSpinnerFromApi(spMekanik, "", "", "mekanik", "NAMA", namaMekanik);
        setSpLokasi();
        spLokasi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!parent.getSelectedItem().toString().equalsIgnoreCase("BENGKEL")) {
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_tambahan, LinearLayout.class), false);
                } else {
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_tambahan, LinearLayout.class), true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        tvMulai_Kerja.setOnClickListener(this);
        tvSelesai_Kerja.setOnClickListener(this);
        tvMulai_istirahat.setOnClickListener(this);
        tvSelesai_istirahat.setOnClickListener(this);
    }

    private void setSpTipe_antrian() {
        if (isAntrian) {
            spTipe_antrian.setItems(listAntrian);
            spTipe_antrian.setSelection(listAntrian, true);
        } else {
            tipeAntrian = new String[]{"--PILIH--", "STANDARD", "EXPRESS", "H+"};
            spTipe_antrian.setItems(tipeAntrian);
        }

        spTipe_antrian.setListener(new MultiSelectionSpinner.OnMultipleItemsSelectedListener() {
            @Override
            public void selectedIndices(List<Integer> indices) {

            }

            @Override
            public void selectedStrings(List<String> strings) {

            }
        });
    }


    private void insertData() {
        final String antrian = spTipe_antrian.getSelectedItemsAsString();
        final int selectedId = rg_status.getCheckedRadioButtonId();
        final String nama = spMekanik.getSelectedItem().toString();
        final String lokasi = spLokasi.getSelectedItem().toString().toUpperCase();
        if (lokasi.contains("PILIH")) {
            lokasi.replace("--PILIH--", "");
        }
        final String masuk = tvMulai_Kerja.getText().toString().trim();
        final String selesai = tvSelesai_Kerja.getText().toString().trim();
        final String istirahat = tvMulai_istirahat.getText().toString();
        final String selesai_istirahat = tvSelesai_istirahat.getText().toString();
        final String[] status = {""};
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "add");
                args.put("userid", userId);
                switch (selectedId) {
                    case R.id.rbOn:
                        status[0] = find(R.id.rbOn, RadioButton.class).getText().toString();
                        args.put("status", status[0]);
                        break;
                    case R.id.rbOff:
                        status[0] = find(R.id.rbOff, RadioButton.class).getText().toString();
                        args.put("status", status[0]);
                        break;
                }
                args.put("namamekanik", nama);
                args.put("antrian", antrian);
                args.put("lokasi", lokasi);
                args.put("masuk", masuk);
                args.put("pulang", selesai);
                args.put("istirahat", istirahat);
                args.put("selesai", selesai_istirahat);
                args.put("inspection", cbInspection.isChecked() ? "Y" : "N");
                args.put("home", cbHome.isChecked() ? "Y" : "N");
                args.put("emergency", cbEmergency.isChecked() ? "Y" : "N");
                args.put("bookbengkel", cbBook.isChecked() ? "Y" : "N");

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturpenugasanmekanik"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Berhasil Menambahkan Tugas Mekanik");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError("Menambahkan data gagal!");
                }
            }
        });
    }

    private void updateData(final Nson nson) {
        final int selectedId = rg_status.getCheckedRadioButtonId();
        final String antrian = spTipe_antrian.getSelectedItem().toString().toUpperCase();
        final String lokasi = spLokasi.getSelectedItem().toString().toUpperCase();
        final String masuk = tvMulai_Kerja.getText().toString().trim();
        final String selesai = tvSelesai_Kerja.getText().toString().trim();
        final String istirahat = tvMulai_istirahat.getText().toString();
        final String selesai_istirahat = tvSelesai_istirahat.getText().toString();
        final String[] status = {""};
        newProses(new Messagebox.DoubleRunnable() {
            Nson data;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "update");
                args.put("id", nson.get("id").asString());
                args.put("antrian", antrian);
                args.put("lokasi", lokasi);
                args.put("masuk", masuk);
                args.put("pulang", selesai);
                switch (selectedId) {
                    case R.id.rbOn:
                        status[0] = find(R.id.rbOn, RadioButton.class).getText().toString();
                        args.put("status", status[0]);
                        break;
                    case R.id.rbOff:
                        status[0] = find(R.id.rbOff, RadioButton.class).getText().toString();
                        args.put("status", status[0]);
                        break;
                }
                args.put("istirahat", istirahat);
                args.put("selesai", selesai_istirahat);
                args.put("inspection", cbInspection.isChecked() ? "Y" : "N");
                args.put("home", cbHome.isChecked() ? "Y" : "N");
                args.put("emergency", cbEmergency.isChecked() ? "Y" : "N");
                args.put("bookbengkel", cbBook.isChecked() ? "Y" : "N");
                data = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturpenugasanmekanik"), args));
            }

            @Override
            public void runUI() {
                if (data.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Berhasil Update Tugas Mekanik");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError("Mohon Di Coba Kembali");
                }
            }
        });
    }

    CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                listChecked.add(buttonView.getText().toString());
                Log.d(TAG, "IsChecked : " + listChecked);
            } else {
                listChecked.remove(buttonView.getText().toString());
                Log.d(TAG, "deleted : " + listChecked);
            }
        }
    };

    private void deleteData(final Nson nson) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson data;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "delete");
                args.put("id", nson.get("id").asString());
                data = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturpenugasanmekanik"), args));
            }

            @Override
            public void runUI() {
                if (data.get("status").asString().equalsIgnoreCase("OK")) {
                    Log.d(TAG, "success delete data" + data.get("ID").asString());
                    startActivity(new Intent(AturPenugasan_Activity.this, PenugasanActivity.class));
                    finish();
                } else {
                    showError("Mohon Di Coba Kembali");
                }
            }
        });
    }

    private void setSpLokasi() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson data;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                data = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturtenda"), args));
            }

            @Override
            public void runUI() {
                if (data.get("status").asString().equalsIgnoreCase("OK")) {
                    if (data.get("data").asArray().size() == 0) {
                        isRefresh = true;
                        if (!isLokasi) {
                            Messagebox.showDialog(getActivity(), "Konfirmasi", "Lokasi Tenda Belum Tercatatkan, Catatkan Lokasi Tenda ?", "Ya", "Tidak", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivityForResult(new Intent(getActivity(), AturTenda_Activity.class), REQUEST_LOKASI);
                                }
                            }, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    showInfo("Lokasi Tersedia Hanya Bengkel");
                                }
                            });
                        }
                    }

                    lokasiArray.add("--PILIH--");
                    lokasiArray.add("BENGKEL");
                    for (int i = 0; i < data.get("data").size(); i++) {
                        lokasiArray.add(data.get("data").get(i).get("NAMA_LOKASI").asString());
                    }
                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, lokasiArray.asArray());
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spLokasi.setAdapter(spinnerAdapter);
                    if (!lokasi.isEmpty()) {
                        for (int in = 0; in < spLokasi.getCount(); in++) {
                            if (spLokasi.getItemAtPosition(in).toString().contains(lokasi)) {
                                spLokasi.setSelection(in);
                            }
                        }
                    }
                } else {
                    showInfoDialog("Lokasi Gagal Di Muat, Muat Ulang ?", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setSpLokasi();
                        }
                    });
                }
            }
        });
    }

    private void setSpMekanik() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson data;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                data = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("mekanik"), args));
            }

            @Override
            public void runUI() {
                if (data.get("status").asString().equalsIgnoreCase("OK")) {
                    if (data.get("data").asArray().size() == 0) {
                        isRefresh = true;
                        showInfo("Mekanik Belum Tercatatkan, Silahkan Daftarkan Mekanik Di Menu USER");
                        Messagebox.showDialog(getActivity(), "Mekanik Belum Di Catatkan", "Catatkan Mekanik ?", "Ya", "Tidak", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivityForResult(new Intent(getActivity(), AturUser_Activity.class), REQUEST_MEKANIK);
                            }
                        }, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });
                        return;
                    }
                    mekanikArray.add("--PILIH--");
                    for (int i = 0; i < data.get("data").size(); i++) {
                        idMekanikArray.add(Nson.newObject().set("ID", data.get("data").get(i).get("ID").asString()).set("NAMA", data.get("data").get(i).get("NAMA").asString()));
                        mekanikArray.add(data.get("data").get(i).get("NAMA").asString());
                    }
                    Log.d(TAG, "List : " + idMekanikArray);
                    ArrayAdapter mekanikAdapter = new ArrayAdapter<String>(getActivity(), R.layout.support_simple_spinner_dropdown_item, mekanikArray.asArray()) {
                        @Override
                        public boolean isEnabled(int position) {
                            for (int i = 0; i < penugasanList.size(); i++) {
                                if (penugasanList.get(i).get("NAMA_MEKANIK").asString().equals(mekanikArray.asArray().get(position))) {
                                    return false;
                                }
                            }
                            return true;
                        }

                        @Override
                        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                            View mView = super.getDropDownView(position, convertView, parent);
                            TextView mTextView = (TextView) mView;
                            ((TextView) mView).setGravity(Gravity.CENTER);
                            for (int i = 0; i < penugasanList.size(); i++) {
                                if (penugasanList.get(i).get("NAMA_MEKANIK").asString().equals(mekanikArray.asArray().get(position))) {
                                    mTextView.setVisibility(View.GONE);
                                    break;
                                } else {
                                    mTextView.setVisibility(View.VISIBLE);
                                }
                            }
                            return mView;
                        }
                    };
                    spMekanik.setAdapter(mekanikAdapter);
                    if (!namaMekanik.isEmpty()) {
                        for (int in = 0; in < spMekanik.getCount(); in++) {
                            if (spMekanik.getItemAtPosition(in).toString().contains(namaMekanik)) {
                                spMekanik.setSelection(in);
                            }
                        }
                    }
                } else {
                    showInfoDialog("Nama Mekanik Gagal Di Muat, Muat Ulang ?", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setSpMekanik();
                        }
                    });
                }

            }
        });
    }

    private void viewPenugasanMekanik() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson data;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "Penugasan");
                data = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("daftarpenugasan"), args));
            }

            @Override
            public void runUI() {
                if (data.get("status").asString().equalsIgnoreCase("OK")) {
                    penugasanList.asArray().addAll(data.get("data").asArray());
                }
            }
        });
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.tv_mulaiKerja:
                getTimePickerDialogTextView(getActivity(), tvMulai_Kerja);
                break;
            case R.id.tv_selesaiKerja:
                getTimePickerDialogTextView(getActivity(), tvSelesai_Kerja);
                break;
            case R.id.tv_mulaistirahat:
                getTimePickerDialogTextView(getActivity(), tvMulai_istirahat);
                break;
            case R.id.tv_selesaistirahat:
                getTimePickerDialogTextView(getActivity(), tvSelesai_istirahat);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_MEKANIK) {
            setSpMekanik();
            showSuccess("Berhasil Mencatatkan Mekanik");
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_LOKASI) {
            setSpLokasi();
            showSuccess("Berhasil Menambahkan Lokasi Tenda");
        }
    }
}
