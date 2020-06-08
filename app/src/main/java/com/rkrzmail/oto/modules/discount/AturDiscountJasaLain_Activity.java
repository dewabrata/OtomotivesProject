package com.rkrzmail.oto.modules.discount;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.MultiSelectionSpinner;
import com.rkrzmail.srv.PercentFormat;
import com.rkrzmail.utils.Tools;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class AturDiscountJasaLain_Activity extends AppActivity implements View.OnClickListener {

    private MultiSelectionSpinner spPekerjaan;
    private ArrayList<String> dummiesPekerjaan = new ArrayList<>();
    private EditText etDiscPart;
    private TextView tvTgl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_discount_jasa_lain_);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_atur_discJasa);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Atur Discount Jasa Lain");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        etDiscPart = findViewById(R.id.et_discPart_discJasa);
        tvTgl = findViewById(R.id.tv_tglEffect_discJasa);
        spPekerjaan = findViewById(R.id.sp_pekerjaan_discJasa);

        setMultiSelectionSpinnerFromApi(spPekerjaan, "nama", "PEKERJAAN", "viewmst", "PEKERJAAN");


        etDiscPart.addTextChangedListener(new PercentFormat(etDiscPart));
        tvTgl.setOnClickListener(this);

        find(R.id.sp_kategori_discJasa);
        find(R.id.cb_mssg_discJasa);

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_tglEffect_discJasa:
                Tools.getDatePickerDialogTextView(getActivity(), tvTgl);
                break;
        }
    }
}
