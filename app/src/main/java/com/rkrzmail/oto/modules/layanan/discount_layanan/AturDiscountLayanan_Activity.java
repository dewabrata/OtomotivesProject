package com.rkrzmail.oto.modules.layanan.discount_layanan;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.MultiSelectionSpinner;
import com.rkrzmail.srv.PercentFormat;
import com.rkrzmail.utils.Tools;

import java.util.ArrayList;
import java.util.Map;

public class AturDiscountLayanan_Activity extends AppActivity implements View.OnClickListener {

    private MultiSelectionSpinner spPekerjaan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_discount_layanan_);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Discount Layanan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        spPekerjaan = findViewById(R.id.sp_pekerjaan_discLayanan);
        find(R.id.et_discPart_discLayanan, EditText.class).addTextChangedListener(new PercentFormat
                (find(R.id.et_discPart_discLayanan, EditText.class)));
        find(R.id.tv_tglEffect_discLayanan).setOnClickListener(this);

        loadData();

        setMultiSelectionSpinnerFromApi(spPekerjaan, "nama",
                "PEKERJAAN", "viewmst", "PEKERJAAN");
        setSpinnerFromApi(find(R.id.sp_paketLayanan_discLayanan, Spinner.class),
                "", "", "viewlayanan", "NAMA_LAYANAN");

        find(R.id.btn_simpan_discLayanan, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addData();
            }
        });
    }

    private void loadData() {
        Nson n = Nson.readJson(getIntentStringExtra("disc"));
        Intent i = getIntent();
        if (i.hasExtra("disc")) {
            find(R.id.tv_tglEffect_discLayanan, TextView.class).setText(n.get("").asString());
            spPekerjaan.setSelection(n.get("").asStringArray());
            find(R.id.sp_paketLayanan_discLayanan, Spinner.class)
                    .setSelection(Tools.getIndexSpinner(
                            find(R.id.sp_paketLayanan_discLayanan, Spinner.class), n.get("").asString()));
            if (n.get("").asString().equalsIgnoreCase("YA")) {
                find(R.id.cb_mssg_discLayanan, CheckBox.class).setChecked(true);
            } else {
                find(R.id.cb_mssg_discLayanan, CheckBox.class).setChecked(false);
            }
            find(R.id.btn_hapus_discLayanan, Button.class).setVisibility(View.VISIBLE);
            find(R.id.btn_hapus_discLayanan, Button.class).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteData(Nson.readJson(getIntentStringExtra("part")));
                }
            });
            find(R.id.btn_simpan_discLayanan, Button.class).setText("UPDATE");
            find(R.id.btn_simpan_discLayanan, Button.class).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateData(Nson.readJson(getIntentStringExtra("part")));
                }
            });
        }
    }

    private void addData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
//                tambah data param : action(add), CID, tanggal, pekerjaan, nama, diskon, pesan
                args.put("action", "add");
                args.put("tanggal", find(R.id.tv_tglEffect_discLayanan, TextView.class).getText().toString());
                args.put("pekerjaan", spPekerjaan.getSelectedItemsAsString());
                args.put("nama", find(R.id.sp_paketLayanan_discLayanan, Spinner.class).getSelectedItem().toString());
                args.put("diskon", find(R.id.et_discPart_discLayanan, EditText.class).getText().toString());
                args.put("pesan", find(R.id.cb_mssg_discLayanan, CheckBox.class).isChecked() ? "YA" : "TIDAK");
                //args.put("", find(R.id.cb_tenda_discLayanan, Checkbox.class).isChecked() ? "YA":"TIDAK");
                //args.put("", find(R.id.cb_bengkel_discLayanan, Checkbox.class).isChecked() ? "YA":"TIDAK");

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturdiskonlayanan"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showInfo("Sukses Menyimpan Aktifitas");
                    startActivity(new Intent(getActivity(), DiscountLayanan_Activity.class));
                    finish();
                } else {
                    showError("Gagal Menyimpan Aktifitas");
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

//                tambah data param : action(add), CID, tanggal, pekerjaan, nama, diskon, pesan
//                delete : action(delete), id
//                update param : action(upadate), nama, tanggal, pekerjaan, diskon, pesan, id

                args.put("action", "add");
                args.put("id", id.get("ID").asString());
                args.put("tanggal", find(R.id.tv_tglEffect_discLayanan, TextView.class).getText().toString());
                args.put("pekerjaan", spPekerjaan.getSelectedItemsAsString());
                args.put("nama", find(R.id.sp_paketLayanan_discLayanan, Spinner.class).getSelectedItem().toString());
                args.put("diskon", find(R.id.et_discPart_discLayanan, EditText.class).getText().toString());
                args.put("pesan", find(R.id.cb_mssg_discLayanan, CheckBox.class).isChecked() ? "YA" : "TIDAK");

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturdiskonlayanan"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showInfo("Sukses Menyimpan Aktifitas");
                    startActivity(new Intent(getActivity(), DiscountLayanan_Activity.class));
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
//                delete : action(delete), id

                args.put("action", "delete");
                args.put("id", id.get("ID").asString());

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturdiskonlayanan"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showInfo("Sukses Menyimpan Aktifitas");
                    startActivity(new Intent(getActivity(), DiscountLayanan_Activity.class));
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
            case R.id.tv_tglEffect_discLayanan:
                getDatePickerDialogTextView(getActivity(), find(R.id.tv_tglEffect_discLayanan, TextView.class));
                break;
        }
    }
}
