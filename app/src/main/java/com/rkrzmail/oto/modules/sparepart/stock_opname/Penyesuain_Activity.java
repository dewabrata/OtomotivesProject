package com.rkrzmail.oto.modules.sparepart.stock_opname;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.gmod.BarcodeActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.REQUEST_BARCODE;

public class Penyesuain_Activity extends AppActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_penyesuain);
        initToolbar();
        initButton();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Penyesuaian");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setPenyesuaian() {
        Nson penyesuaian = Nson.readJson(getIntentStringExtra(DATA));

        penyesuaian.set("SEBAB", find(R.id.sp_sebab_penyesuaian, Spinner.class).getSelectedItem().toString());
        penyesuaian.set("FOLDER_LAIN", find(R.id.et_no_folder_lain, EditText.class).getText().toString());
        penyesuaian.set("KET", find(R.id.et_ket_penyesuaian, EditText.class).getText().toString());
        penyesuaian.set("USER_SAKSI", find(R.id.et_user_saksi_penyesuaian, EditText.class).getText().toString());

        Intent i = new Intent();
        i.putExtra(DATA, penyesuaian.toJson());
        setResult(RESULT_OK, i);
        finish();
    }

    private void initButton(){
        find(R.id.btn_simpan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(find(R.id.sp_sebab_penyesuaian, Spinner.class).getSelectedItem().toString().equals("--PILIH--")){
                    find(R.id.sp_sebab_penyesuaian, Spinner.class).performClick();
                    showWarning("Sebab Penyesuaian Belum Di Pilih");
                }else{
                    setPenyesuaian();
                }
            }
        });

        find(R.id.img_scan_barcode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), BarcodeActivity.class);
                startActivityForResult(i, REQUEST_BARCODE);
            }
        });
    }

    public void getDataBarcode(final String user) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("viewsparepart"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data").get(0);

                } else {
                    showError(result.get("message").asString());
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == REQUEST_BARCODE){
            String barcode = getIntentStringExtra(data, "TEXT");
            getDataBarcode(barcode);
        }
    }
}
