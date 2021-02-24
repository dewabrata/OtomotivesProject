package com.rkrzmail.oto.modules.mekanik;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.rkrzmail.srv.NumberFormatUtils;
import com.rkrzmail.utils.Tools;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.SET_CLAIM;
import static com.rkrzmail.utils.APIUrls.SET_REKENING_BANK;
import static com.rkrzmail.utils.Tools.setFormatDayAndMonthToDb;
import static java.nio.file.Files.find;

public class ClaimGaransiStatus_Activity extends AppActivity {

    private TextView tvTanggal;
    private Spinner spStatus , spNorek;
    private EditText etNoclaim, etKeterangan, etResi, etTotalRefund;
    private Nson rekeningList = Nson.newArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_claim_garansi_status);
        initToolbar();
        initComponent();
    }

    @SuppressLint("NewApi")
    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Claim Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent(){
        tvTanggal = findViewById(R.id.tv_tanggal_claim);
        spStatus = findViewById(R.id.sp_status_claim);
        etNoclaim = findViewById(R.id.et_no_claim);
        etKeterangan = findViewById(R.id.et_keterangan_claim);
        etResi = findViewById(R.id.et_noresi_claim);
        etTotalRefund = findViewById(R.id.et_totalRefund);
        spNorek = findViewById(R.id.sp_norek);

        setSpRek();
        etTotalRefund.addTextChangedListener(new NumberFormatUtils().rupiahTextWatcher(etTotalRefund));

        spStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getSelectedItem().toString();
                if(item.equalsIgnoreCase("FILLING")){
                    etNoclaim.setEnabled(true);
                    etResi.setEnabled(false);
                    etTotalRefund.setEnabled(false);
                    spNorek.setEnabled(false);
                }else if (item.equalsIgnoreCase("KIRIM PART")){
                    etResi.setEnabled(true);
                    etNoclaim.setEnabled(false);
                    etTotalRefund.setEnabled(false);
                    spNorek.setEnabled(false);
                }else if (item.equalsIgnoreCase("DITERIMA")){
                    etTotalRefund.setEnabled(true);
                    spNorek.setEnabled(true);
                    etResi.setEnabled(false);
                    etNoclaim.setEnabled(false);
                }else {
                    etTotalRefund.setEnabled(false);
                    spNorek.setEnabled(false);
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
                if (tvTanggal.getText().toString().isEmpty()) {
                    showWarning("Tanggal Belum di Isi");
                    find(R.id.ic_tanggal_claim, TextView.class).performClick();
                } else if (spStatus.getSelectedItem().toString().equalsIgnoreCase("--PILIH--")) {
                    showWarning("Status Belum di Pilih");
                    spStatus.performClick();
                } else {
                    SimpanData();
                }
            }

        });
    }

    private void SimpanData(){
        final String tanggal = tvTanggal.getText().toString();
        final String noRek = spNorek.getSelectedItem().toString();
        if (noRek.contains("--PILIH--")){
            noRek.replace("--PILIH--", "");
        }

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
                args.put("rekInternal", noRek);

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

    public void setSpRek() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(SET_REKENING_BANK), args));
            }

            @Override
            public void runUI() {
                ArrayList<String> str = new ArrayList<>();
                result = result.get("data");
                str.add("--PILIH--");
                rekeningList.add("");
                for (int i = 0; i < result.size(); i++) {
                    rekeningList.add(Nson.newObject()
                            .set("ID", result.get(i).get("ID"))
                            .set("BANK_NAME", result.get(i).get("BANK_NAME"))
                            .set("NO_REKENING", result.get(i).get("NO_REKENING").asString())
                            .set("EDC", result.get(i).get("EDC_ACTIVE"))
                            .set("OFF_US", result.get(i).get("OFF_US"))
                            .set("COMPARISON", result.get(i).get("BANK_NAME").asString() + " - " + result.get(i).get("NO_REKENING").asString()));
                    str.add(result.get(i).get("BANK_NAME").asString() + " - " + result.get(i).get("NO_REKENING").asString());
                }

                ArrayList<String> newStr = Tools.removeDuplicates(str);
                ArrayAdapter adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, newStr);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spNorek.setAdapter(adapter);
            }
        });

        spNorek.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (adapterView.getSelectedItem().toString().equals(rekeningList.get(i).get("COMPARISON").asString())) {
//                    noRek = rekeningList.get(i).get("NO_REKENING").asString();
//                    namaBank = rekeningList.get(i).get("BANK_NAME").asString();
//                    offUs = rekeningList.get(i).get("OFF_US").asString();
                } else {
//                    noRek = "";
//                    namaBank = "";
//                    offUs = "";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
}