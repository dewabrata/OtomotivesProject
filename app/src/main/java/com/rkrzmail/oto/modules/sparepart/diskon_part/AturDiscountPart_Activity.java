package com.rkrzmail.oto.modules.sparepart.diskon_part;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.sparepart.CariPart_Activity;
import com.rkrzmail.srv.MultiSelectionSpinner;
import com.rkrzmail.srv.PercentFormat;
import com.rkrzmail.utils.Tools;

import java.util.Map;

public class AturDiscountPart_Activity extends AppActivity implements View.OnClickListener {

    private static final int REQUEST_CARI_PART = 10;
    private MultiSelectionSpinner spPekerjaan;
    private EditText etDiscPart, etDiscJasa, etNoPart, etNamaPart;
    private TextView tvTgl;

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

        setMultiSelectionSpinnerFromApi(spPekerjaan, "nama", "PEKERJAAN", "viewmst", "PEKERJAAN");

        tvTgl.setOnClickListener(this);
        etDiscJasa.addTextChangedListener(new PercentFormat(etDiscJasa));
        etDiscPart.addTextChangedListener(new PercentFormat(etDiscPart));

        find(R.id.btn_simpan_discPart, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });
    }


    private void saveData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
//                view biasa param : action(view)
//                view search param : search, action(view)
//                tambah data param : action(add), CID, tanggal, pekerjaan, nama, nopart, diskonpart, diskonjasa, pesan
//                delete : action(delete), id
//                update param : action(upadate), tanggal, pekerjaan, diskonpart, diskonjasa, pesan, id
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "add");
                args.put("tanggal", tvTgl.getText().toString());
                args.put("pekerjaan", spPekerjaan.getSelectedItemsAsString());
                args.put("nama", etNamaPart.getText().toString());
                args.put("nopart", etNoPart.getText().toString());
                args.put("diskonpart", etDiscPart.getText().toString());
                args.put("diskonjasa", etDiscJasa.getText().toString());
                args.put("pesan", find(R.id.cb_mssg_discPart, CheckBox.class).isChecked() ? "YA" : "TIDAK");
                //args.put("", find(R.id.cb_tenda_discPart, Checkbox.class).isChecked() ? "YA":"TIDAK");
                //args.put("", find(R.id.cb_bengkel_discPart, Checkbox.class).isChecked() ? "YA":"TIDAK");

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturdiskonpart"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showInfo("Sukses Menambahkan Diskon Part");
                    startActivity(new Intent(getActivity(), DiscountPart_Activity.class));
                    finish();
                } else {
                    showInfo("Gagal Menambahkan Diskon Part");
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_searchPart) {
            Intent i = new Intent(this, CariPart_Activity.class);
            i.putExtra("flag", "ALL");
            startActivityForResult(i, REQUEST_CARI_PART);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_tglEffect_discPart:
                Tools.getDatePickerDialogTextView(getActivity(), tvTgl);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CARI_PART) {
            Nson n = Nson.readJson(getIntentStringExtra(data, "part"));
            etNoPart.setText(n.get("NO_PART").asString());
            etNamaPart.setText(n.get("NAMA").asString());
        }
    }
}
