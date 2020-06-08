package com.rkrzmail.oto.modules.discount;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.MultiSelectionSpinner;
import com.rkrzmail.srv.PercentFormat;
import com.rkrzmail.utils.Tools;

import java.util.ArrayList;

public class AturDiscountLayanan_Activity extends AppActivity implements View.OnClickListener {

    private MultiSelectionSpinner spPekerjaan;
    private ArrayList<String> dummiesPekerjaan = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_discount_layanan_);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_atur_discLayanan);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Discount Layanan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        find(R.id.et_discPart_discLayanan, EditText.class).addTextChangedListener(new PercentFormat(find(R.id.et_discPart_discLayanan, EditText.class)));
        find(R.id.tv_tglEffect_discLayanan).setOnClickListener(this);
        spPekerjaan = findViewById(R.id.sp_pekerjaan_discLayanan);
        setMultiSelectionSpinnerFromApi(spPekerjaan, "nama", "PEKERJAAN", "viewmst", "PEKERJAAN");

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_tglEffect_discLayanan:
                Tools.getDatePickerDialogTextView(getActivity(), find(R.id.tv_tglEffect_discLayanan, TextView.class));
                break;
        }
    }
}
