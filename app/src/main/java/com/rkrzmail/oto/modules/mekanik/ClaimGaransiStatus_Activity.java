package com.rkrzmail.oto.modules.mekanik;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NikitaAutoComplete;
import com.rkrzmail.oto.modules.Adapter.NsonAutoCompleteAdapter;
import com.rkrzmail.srv.NumberFormatUtils;
import com.rkrzmail.utils.Tools;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.SET_CLAIM;
import static com.rkrzmail.utils.APIUrls.SET_REKENING_BANK;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.Tools.setFormatDayAndMonthToDb;

public class ClaimGaransiStatus_Activity extends AppActivity {

    private TextView tvTanggal;
    private Spinner spStatus , spNorek;
    private EditText etNoclaim, etKeterangan, etResi, etTotalRefund, etBiayaTf;
    private Nson rekeningList = Nson.newArray();
    private Nson data;
    private NikitaAutoComplete etBankTerbayar;
    private String noRek="", namaBank="",idClaim="",totalRefund="",updateOrsave="";
    private final Nson cekDataList = Nson.newArray();

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
        etBankTerbayar = findViewById(R.id.et_bankTerbayar_claim);
        etBiayaTf = findViewById(R.id.et_biayaTf_claim);
        spNorek = findViewById(R.id.sp_norek);

        etBankTerbayar.setEnabled(false);
        etBiayaTf.setEnabled(false);

        setSpRek();
        loadData();
        CekData();
        initAutoCompleteNamaBank();
        initListener();
    }

    private void initListener(){
        etTotalRefund.addTextChangedListener(new NumberFormatUtils().rupiahTextWatcher(etTotalRefund));
        etBiayaTf.addTextChangedListener(new NumberFormatUtils().rupiahTextWatcher(etBiayaTf));

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
                }else if (item.equalsIgnoreCase("TERIMA REFUND")){
                    etTotalRefund.setEnabled(true);
                    spNorek.setEnabled(true);
                    etResi.setEnabled(false);
                    etNoclaim.setEnabled(false);
                }else if (item.equalsIgnoreCase("BAYAR CLAIM")){
                    etTotalRefund.setEnabled(true);
                    spNorek.setEnabled(true);
                    etResi.setEnabled(false);
                    etNoclaim.setEnabled(false);
                    etBankTerbayar.setEnabled(true);
                    etBiayaTf.setEnabled(true);
                    if(totalRefund != null || totalRefund != "0"){
                        etTotalRefund.setText(totalRefund);
                    }else {
                        etTotalRefund.setText("");
                    }
                }else {
                    etTotalRefund.setEnabled(false);
                    spNorek.setEnabled(false);
                    etResi.setEnabled(false);
                    etNoclaim.setEnabled(false);
                    etBankTerbayar.setEnabled(false);
                    etBiayaTf.setEnabled(false);
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
                } else if (spStatus.getSelectedItem().toString().equals("FILLING")){
                    if (etNoclaim.getText().toString().isEmpty()){
                        showWarning("No Claim Belum di Pilih");
                        etNoclaim.performClick();
                    }else {
                        SimpanData(updateOrsave);
                    }
                }else if (spStatus.getSelectedItem().toString().equals("KIRIM PART")){
                    if (etResi.getText().toString().isEmpty()){
                        showWarning("No Resi Belum di Pilih");
                        etResi.performClick();
                    }else {
                        SimpanData(updateOrsave);
                    }
                }else if (spStatus.getSelectedItem().toString().equals("TERIMA REFUND")){
                    if (etTotalRefund.getText().toString().isEmpty()){
                        showWarning("Total Refund Belum di Pilih");
                        etTotalRefund.performClick();
                    }else if (spNorek.getSelectedItem().toString().equals("--PILIH--")){
                        showWarning("Rekening Internal Belum di Pilih");
                        spNorek.performClick();
                    }else if (etBankTerbayar.getText().toString().isEmpty()) {
                        showWarning("Rekening Internal Belum di Pilih");
                        spNorek.performClick();
                    }else{
                        SimpanData(updateOrsave);
                    }
                }else{
                    SimpanData(updateOrsave);
                }

            }

        });
    }

    private void SimpanData(final String updateOrsimpan){
        String rek = spNorek.getSelectedItem().toString();
        if (spNorek.getSelectedItem().toString().contains("--PILIH--")){
            rek = spNorek.getSelectedItem().toString().replace("--PILIH--", "");
        }
        final String noRekSp = rek;
        final String tanggal = tvTanggal.getText().toString();

        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "add");
                args.put("kategori", "CLAIM");
                args.put("updateOrsimpan", updateOrsimpan);
                args.put("claimId", idClaim);
                args.put("status", spStatus.getSelectedItem().toString());
                args.put("noClaim", etNoclaim.getText().toString());
                args.put("tanggal", setFormatDayAndMonthToDb(tanggal));
                args.put("keterangan", etKeterangan.getText().toString().toUpperCase());
                args.put("noResi", etResi.getText().toString());
                args.put("bankTerbayar", etBankTerbayar.getText().toString().trim());
                args.put("biayaTransfer", formatOnlyNumber(etBiayaTf.getText().toString()));
                args.put("refundRp", formatOnlyNumber(etTotalRefund.getText().toString()));
                args.put("rekInternal", noRekSp);

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(SET_CLAIM), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showInfo("Sukses Menyimpan Data");
                    startActivity(new Intent(getActivity(),ClaimGaransiPart_Activity.class));
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
                    noRek = rekeningList.get(i).get("NO_REKENING").asString();
                    namaBank = rekeningList.get(i).get("BANK_NAME").asString();
//                    offUs = rekeningList.get(i).get("OFF_US").asString();
                    etBankTerbayar.setEnabled(true);
                    etBiayaTf.setEnabled(true);
                } else {
                    noRek = "";
                    namaBank = "";
//                    offUs = "";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void initAutoCompleteNamaBank() {
        etBankTerbayar.setThreshold(3);
        etBankTerbayar.setAdapter(new NsonAutoCompleteAdapter(getActivity()) {
            @Override
            public Nson onFindNson(Context context, String bookTitle) {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "AUTO COMPLETE");
                args.put("search", bookTitle);
                Nson result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(SET_REKENING_BANK), args));
                if (result.get("data").asArray().isEmpty()) {
                    return result.get("message");
                }
                return result.get("data");
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.item_suggestion, parent, false);
                }
                findView(convertView, R.id.title, TextView.class).setText((getItem(position).get("BANK_NAME").asString()));
                return convertView;
            }
        });

        etBankTerbayar.setLoadingIndicator((android.widget.ProgressBar) findViewById(R.id.progress_bar));
        etBankTerbayar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Nson n = Nson.readJson(String.valueOf(adapterView.getItemAtPosition(position)));
                String bankName = n.get("BANK_NAME").asString();
                etBiayaTf.setEnabled(!bankName.equalsIgnoreCase(namaBank));
                etBankTerbayar.setText(bankName);
                etBankTerbayar.setSelection(etBankTerbayar.getText().length());
                if (namaBank.isEmpty() && noRek.isEmpty()) {
                    viewFocus(spNorek);
                    TextView errorText = (TextView) spNorek.getSelectedView();
                    errorText.setError("REKENING INTERNAL BELUM DI PILIH");
                    errorText.setTextColor(Color.RED);//just to highlight that this is an error
                }
            }
        });
    }

    private void CekData(){
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "CEKDATA");
                args.put("idClaim", idClaim);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(SET_CLAIM), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    Integer isUpdate = result.get("data").get(0).get("COUNT").asInteger();
                    if(isUpdate == 0){
                        updateOrsave = "SAVE";
                    }else {
                        updateOrsave = "UPDATE";
                    }
                }
            }
        });
    }

    private void loadData(){
        data = Nson.readJson(getIntentStringExtra(DATA));
        idClaim = data.get("ID").asString();
        totalRefund = data.get("TOTAL_REFUND").asString();
    }

}