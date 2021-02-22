package com.rkrzmail.oto.modules.mekanik;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;

import java.util.Map;

import static com.rkrzmail.utils.APIUrls.SET_CLAIM;
import static com.rkrzmail.utils.Tools.setFormatDayAndMonthToDb;
import static java.nio.file.Files.find;

public class ClaimGaransiStatus_Activity extends AppActivity {

    private TextView tvTanggal;
    private Spinner spStatus;
    private EditText etNoclaim, etKeterangan, etResi, etTotalRefund, etRekening;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_claim_garansi_status);
        initComponent();
    }
    private void initComponent(){
        tvTanggal = findViewById(R.id.tv_tanggal_claim);
        spStatus = findViewById(R.id.sp_status_claim);
        etNoclaim = findViewById(R.id.et_no_claim);
        etKeterangan = findViewById(R.id.et_keterangan_claim);
        etResi = findViewById(R.id.et_noresi_claim);
        etTotalRefund = findViewById(R.id.et_totalRefund);
        etRekening = findViewById(R.id.et_noRek_claim);

        spStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getSelectedItem().toString();
                if(item.equalsIgnoreCase("FILLING")){
                    etNoclaim.setEnabled(true);
                    etResi.setEnabled(false);
                    etTotalRefund.setEnabled(false);
                    etRekening.setEnabled(false);
                }else if (item.equalsIgnoreCase("KIRIM PART")){
                    etResi.setEnabled(true);
                    etNoclaim.setEnabled(false);
                    etTotalRefund.setEnabled(false);
                    etRekening.setEnabled(false);
                }else if (item.equalsIgnoreCase("DITERIMA")){
                    etTotalRefund.setEnabled(true);
                    etRekening.setEnabled(true);
                    etResi.setEnabled(false);
                    etNoclaim.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        find(R.id.ic_tanggal_claim, TextView.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDatePickerDialogTextView(getActivity(),tvTanggal);
            }
        });


        find(R.id.btn_simpan, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpanData();
            }
        });
    }

    private void SimpanData(){
        final String tanggal = tvTanggal.getText().toString();
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "add");
                args.put("kategori", "CLAIM");
                args.put("claimId", etNoclaim.getText().toString());
                args.put("status", spStatus.getSelectedItem().toString());
                args.put("tanggal", setFormatDayAndMonthToDb(tanggal));
                args.put("keterangan", etKeterangan.getText().toString().toUpperCase());
                args.put("noResi", etResi.getText().toString());
                args.put("refundRp", etTotalRefund.getText().toString());
                args.put("rekInternal", etRekening.getText().toString());

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(SET_CLAIM), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showInfo("Sukses Menyimpan Data");
                    finish();
                } else {
                    showInfo("Gagagl Menyimpan Data");
                }
            }
        });

    }
}