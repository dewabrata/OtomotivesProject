package com.rkrzmail.oto.modules.bengkel;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;

import java.util.Map;
import java.util.Objects;

import static android.support.annotation.Dimension.DP;
import static com.rkrzmail.utils.APIUrls.VIEW_PEMBAYARAN;
import static com.rkrzmail.utils.ConstUtils.RINCIAN_DP;
import static com.rkrzmail.utils.ConstUtils.RINCIAN_LAYANAN;

public class Rincian_Pembayaran_Activity extends AppActivity {

    private boolean isLayanan = false, isDp = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rincian_pembayaran);
        initComponent();
    }

    @SuppressLint("NewApi")
    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getIntent().hasExtra(RINCIAN_LAYANAN)){
            Objects.requireNonNull(getSupportActionBar()).setTitle("Rincian Layanan");
            isLayanan = true;
            find(R.id.row_dp_persen).setVisibility(View.GONE);
            find(R.id.row_sisa_dp_persen).setVisibility(View.GONE);
        }else if(getIntent().hasExtra(RINCIAN_DP)){
            Objects.requireNonNull(getSupportActionBar()).setTitle("Rincian Layanan");
            isDp = true;
            find(R.id.row_dp).setVisibility(View.GONE);
            find(R.id.row_sisa_biaya).setVisibility(View.GONE);
            find(R.id.row_disc_spot).setVisibility(View.GONE);
            find(R.id.row_total_2).setVisibility(View.GONE);
            find(R.id.ly_ket).setVisibility(View.GONE);
            find(R.id.et_catatan).setVisibility(View.GONE);
        }
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        viewRincianPembayaran();
        find(R.id.btn_jasa_part, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        find(R.id.btn_lanjut, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void viewRincianPembayaran(){
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result ;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                if(isLayanan){
                    args.put("typeRincian", "RINCIAN LAYANAN");

                }
                if(isDp){
                    args.put("typeRincian", "RINCIAN DP");

                }

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_PEMBAYARAN), args));
            }

            @Override
            public void runUI() {
                if(result.get("status").asString().equalsIgnoreCase("OK")){
                    if(result.get("DEREK").asString().equals("Y")){
                        find(R.id.cb_derek, CheckBox.class).setChecked(true);
                    }
                    if(result.get("BUANG_PART").asString().equals("Y")){
                        find(R.id.cb_buangPart, CheckBox.class).setChecked(true);
                    }
                    if(result.get("ANTAR_JEMPUT").asString().equals("Y")){
                        find(R.id.cb_antar_jemput, CheckBox.class).setChecked(true);
                    }
                    if(result.get("DISCOUNT_LAYANAN").asString().isEmpty() || result.get("DISCOUNT_LAYANAN").asString().equals("0")){
                        find(R.id.row_disc_layanan).setVisibility(View.GONE);
                    }
                    if(result.get("DISCOUNT_FREKWENSI").asString().isEmpty() || result.get("DISCOUNT_LAYANAN").asString().equals("0")){
                        find(R.id.row_disc_frekwensi).setVisibility(View.GONE);
                    }
                    if(result.get("DISCOUNT_PART").asString().isEmpty() || result.get("DISCOUNT_LAYANAN").asString().equals("0")){
                        find(R.id.row_disc_part).setVisibility(View.GONE);
                    }
                    if(result.get("DISCOUNT_JASA_PART").asString().isEmpty() || result.get("DISCOUNT_LAYANAN").asString().equals("0")){
                        find(R.id.row_disc_jasa_part).setVisibility(View.GONE);
                    }
                    if(result.get("DISCOUNT_JASA_LAIN").asString().isEmpty() || result.get("DISCOUNT_LAYANAN").asString().equals("0")){
                        find(R.id.row_disc_jasa_lain).setVisibility(View.GONE);
                    }
                    if(result.get("DEREK").asString().isEmpty() || result.get("DISCOUNT_LAYANAN").asString().equals("0")){
                        find(R.id.row_transport).setVisibility(View.GONE);
                    }
                    if(result.get("DP").asString().isEmpty() || result.get("DISCOUNT_LAYANAN").asString().equals("0")){
                        find(R.id.row_dp).setVisibility(View.GONE);
                    }
                    if(result.get("DISCOUNT_SPOOT").asString().isEmpty() || result.get("DISCOUNT_LAYANAN").asString().equals("0")){
                        find(R.id.row_disc_spot).setVisibility(View.GONE);
                    }

                    int total1 = 0, sisaBiaya = 0, total2;

                    find(R.id.tv_nama_pelanggan, TextView.class);
                    find(R.id.tv_no_ponsel , TextView.class).setText(formatRp(result.get("").asString()));
                    find(R.id.tv_layanan , TextView.class).setText(formatRp(result.get("").asString()));
                    find(R.id.tv_nopol , TextView.class).setText(formatRp(result.get("").asString()));
                    find(R.id.tv_frek , TextView.class).setText(formatRp(result.get("").asString()));
                    find(R.id.tv_totalLayanan , TextView.class).setText(formatRp(result.get("").asString()));
                    find(R.id.tv_disc_layanan , TextView.class).setText(formatRp(result.get("").asString()));
                    find(R.id.tv_disc_frekwensi , TextView.class).setText(formatRp(result.get("").asString()));
                    find(R.id.tv_totalPart , TextView.class).setText(formatRp(result.get("").asString()));
                    find(R.id.tv_disc_part , TextView.class).setText(formatRp(result.get("").asString()));
                    find(R.id.tv_harga_jasa_part , TextView.class).setText(formatRp(result.get("").asString()));
                    find(R.id.tv_disc_jasa_part , TextView.class).setText(formatRp(result.get("").asString()));
                    find(R.id.tv_harga_jasa_lain , TextView.class).setText(formatRp(result.get("").asString()));
                    find(R.id.tv_disc_jasa_lain , TextView.class).setText(formatRp(result.get("").asString()));
                    find(R.id.tv_harga_derek_transport , TextView.class).setText(formatRp(result.get("").asString()));
                    find(R.id.tv_harga_penyimpanan , TextView.class).setText(formatRp(result.get("").asString()));
                    find(R.id.tv_total_1 , TextView.class).setText(formatRp(result.get("").asString()));
                    find(R.id.tv_dp , TextView.class).setText(formatRp(result.get("").asString()));
                    find(R.id.tv_sisa_biaya , TextView.class).setText(formatRp(result.get("").asString()));
                    find(R.id.tv_disc_spot , TextView.class).setText(formatRp(result.get("").asString()));
                    find(R.id.tv_total_2 , TextView.class).setText(formatRp(result.get("").asString()));
                    find(R.id.et_ket_tambahan , EditText.class).setText(formatRp(result.get("").asString()));
                    find(R.id.et_catatan , EditText.class).setText(formatRp(result.get("").asString()));
                    /*find(R.id. , TextView.class).setText(formatRp(result.get("").asString()));
                    find(R.id. , TextView.class).setText(formatRp(result.get("").asString()));
                    find(R.id. , TextView.class).setText(formatRp(result.get("").asString()));
                    find(R.id. , TextView.class).setText(formatRp(result.get("").asString()));
                    find(R.id. , TextView.class).setText(formatRp(result.get("").asString()));
                    find(R.id. , TextView.class).setText(formatRp(result.get("").asString()));
                    find(R.id. , TextView.class).setText(formatRp(result.get("").asString()));
                    find(R.id. , TextView.class).setText(formatRp(result.get("").asString()));
                    find(R.id. , TextView.class).setText(formatRp(result.get("").asString()));
                    find(R.id. , TextView.class).setText(formatRp(result.get("").asString()));
                    find(R.id. , TextView.class).setText(formatRp(result.get("").asString()));
                    find(R.id. , TextView.class).setText(formatRp(result.get("").asString()));*/

                }else{
                    result.get("message").asString();
                }
            }
        });
    }

    private void viewJasaPart(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.activity_list_basic, null);
        builder.setView(dialogView);
        builder.setCancelable(true);

        builder.create();
        AlertDialog alertDialog = builder.show();
        alertDialog.setCancelable(false);
    }

    private void getJasaPart(){

    }

}
