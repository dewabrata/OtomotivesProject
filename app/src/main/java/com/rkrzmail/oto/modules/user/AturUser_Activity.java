package com.rkrzmail.oto.modules.user;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
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
import com.rkrzmail.srv.MultiSelectionSpinner;
import com.rkrzmail.srv.RupiahFormat;
import com.rkrzmail.utils.Tools;

import java.util.ArrayList;
import java.util.Map;

public class AturUser_Activity extends AppActivity implements View.OnClickListener {

    private static final String TAG = "AturUser_Activity";
    private MultiSelectionSpinner spAkses, spPosisi;
    private Spinner spStatus;
    private Nson layanan = Nson.newArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_user);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Atur User");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        loadData();
        spAkses = findViewById(R.id.spinnerAksesApp);
        spPosisi = findViewById(R.id.spinnerPosisi);
        spStatus = (Spinner) findViewById(R.id.spinnerStatus);

        setMultiSelectionSpinnerFromApi(spPosisi, "nama", "POSISI", "viewmst", "NAMA");

        find(R.id.spinnerStatus, Spinner.class).setSelection(-1);

        find(R.id.txtGaji, TextView.class).addTextChangedListener(new RupiahFormat(find(R.id.txtGaji, EditText.class)));

        find(R.id.txtTglMasuk, TextView.class).setOnClickListener(this);
        find(R.id.tblSimpan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!componentValidation()) {
                    addData();
                }
            }
        });
    }

    private void loadData() {
        final Nson data = Nson.readJson(getIntentStringExtra("NAMA"));
        final Intent i = getIntent();
        if (i.hasExtra("NAMA")) {
            Tools.setViewAndChildrenEnabled(find(R.id.ly_tglLahir_aturKaryawan, LinearLayout.class), false);

            find(R.id.txtNamaKaryawan, EditText.class).setText(data.get("NAMA").asString());
            find(R.id.txtNoPonsel, EditText.class).setText(data.get("NO_PONSEL").asString());
            find(R.id.txtTglMasuk, TextView.class).setText(data.get("TANGGAL_MASUK").asString());

            find(R.id.tblSimpan, Button.class).setText("Update");
            find(R.id.tblSimpan).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updateData();
                }
            });
        }
    }

    private void addData() {
        final String aksesApp = spAkses.getSelectedItemsAsString();
        final String posisi = spPosisi.getSelectedItemsAsString();
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                Map<String, String> args2 = AppApplication.getInstance().getArgsData();
                Nson data = Nson.readJson(getIntentStringExtra("NAMA"));
//                add :  CID, action(add), nama, nik, tanggallahir, kelamin, nopol, email, alamat, tanggalmasuk,
//                posisi, status, penggajian, gaji, akses
                args2.put("action", "view");
                args.put("action", "add");
                args.put("nama", data.get("NAMA").asString());
                args.put("nik", data.get("NIK").asString());
                args.put("tanggallahir", find(R.id.txtTglLahir, TextView.class).getText().toString());
                args.put("kelamin", find(R.id.spinnerKelamin, Spinner.class).getSelectedItem().toString());
                args.put("nopol", find(R.id.txtNoPonsel, TextView.class).getText().toString());
                args.put("email", find(R.id.txtEmail, TextView.class).getText().toString());
                args.put("alamat", find(R.id.txtAlamat, TextView.class).getText().toString());
                args.put("tangalmasuk", find(R.id.txtTglMasuk, TextView.class).getText().toString());
                args.put("posisi", posisi);
                args.put("status", find(R.id.spinnerStatus, Spinner.class).getSelectedItem().toString());
                //args.put("penggajian", find(R.id.spinnerPenggajian, Spinner.class).getSelectedItem().toString());
                args.put("gaji", find(R.id.txtGaji, EditText.class).getText().toString());
                //args.put("akses", aksesApp);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturkaryawan"), args));
            }

            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    startActivity(new Intent(AturUser_Activity.this, User_Activity.class));
                    finish();
                } else {
                    showError("Gagal Menambahkan Aktifitas");
                }
            }
        });
    }

    private void updateData() {
        final String aksesApp = spAkses.getSelectedItemsAsString();
        final String posisi = spPosisi.getSelectedItemsAsString();
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
//                update :  CID, action(update),  nopol, email, alamat, tanggalmasuk, posisi, status, penggajian, gaji, akses

                args.put("action", "update");
                args.put("nopol", find(R.id.txtNoPonsel, TextView.class).getText().toString());
                args.put("email", find(R.id.txtEmail, TextView.class).getText().toString());
                args.put("alamat", find(R.id.txtAlamat, TextView.class).getText().toString());
                args.put("tangalmasuk", find(R.id.txtTglMasuk, TextView.class).getText().toString());
                args.put("posisi", posisi);
                args.put("status", find(R.id.spinnerStatus, Spinner.class).getSelectedItem().toString());
                args.put("penggajian", find(R.id.spinnerPenggajian, Spinner.class).getSelectedItem().toString());
                args.put("gaji", find(R.id.txtGaji, TextView.class).getText().toString());
                args.put("akses", aksesApp);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturkaryawan"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    startActivity(new Intent(AturUser_Activity.this, User_Activity.class));
                    finish();
                } else {
                    showError("Gagal Memperbarui Data");
                }
            }
        });
    }

    private boolean componentValidation() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturkaryawan"), args));
            }
            @Override
            public void runUI() {
                ArrayList<String> data = new ArrayList<>();
                for (int i = 0; i < result.get("data").size(); i++) {
                    data.add(result.get("data").get(i).get("NO_PONSEL").asString());
                }
                if (data.contains(find(R.id.txtNoPonsel, TextView.class).getText().toString())) {
                    Tools.alertDialog(getActivity(), "Nomor Ponsel Sudah Terdaftar");
                    return;
                }
            }
        });
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.txtTglMasuk:
                Tools.getDatePickerDialogTextView(getActivity(), find(R.id.txtTglMasuk, TextView.class));
                break;
        }
    }
}

