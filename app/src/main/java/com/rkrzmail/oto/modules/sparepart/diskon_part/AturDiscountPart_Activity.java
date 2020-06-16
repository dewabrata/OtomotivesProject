package com.rkrzmail.oto.modules.sparepart.diskon_part;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
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
import com.rkrzmail.oto.modules.discount.SpotDiscount_Activity;
import com.rkrzmail.oto.modules.lokasi_part.CariPart_Activity;
import com.rkrzmail.oto.modules.sparepart.AturParts_Activity;
import com.rkrzmail.oto.modules.sparepart.AturPenjualanPart_Activity;
import com.rkrzmail.srv.MultiSelectionSpinner;
import com.rkrzmail.srv.PercentFormat;
import com.rkrzmail.utils.Tools;

import java.util.ArrayList;
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
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_atur_discPart);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Discount Part");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {

        spPekerjaan = findViewById(R.id.sp_pekerjaan_disc);
        etDiscPart = findViewById(R.id.et_discPart_disc);
        etDiscJasa = findViewById(R.id.et_discJasa_disc);
        etNoPart = findViewById(R.id.et_noPart_disc);
        etNamaPart = findViewById(R.id.et_namaPart_disc);
        tvTgl = findViewById(R.id.tv_tglEffect_discPart);

        setMultiSelectionSpinnerFromApi(spPekerjaan, "nama", "PEKERJAAN", "viewmst", "PEKERJAAN");

        tvTgl.setOnClickListener(this);
        etDiscJasa.addTextChangedListener(new PercentFormat(etDiscJasa));
        etDiscPart.addTextChangedListener(new PercentFormat(etDiscPart));

        find(R.id.btn_simpanPart_disc, Button.class).setOnClickListener(new View.OnClickListener() {
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

    SearchView mSearchView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView = new SearchView(getSupportActionBar().getThemedContext());

        final MenuItem searchMenu = menu.findItem(R.id.action_searchPart);

        searchMenu.setActionView(mSearchView);
        searchMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        searchMenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                startActivityForResult(new Intent(getActivity(), CariPart_Activity.class), REQUEST_CARI_PART);

                return false;
            }
        });
        //SearchView searchView = (SearchView)  menu.findItem(R.id.action_search).setActionView(mSearchView);
        // Assumes current activity is the searchable activity
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
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
