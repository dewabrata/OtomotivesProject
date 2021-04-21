package com.rkrzmail.oto.modules.hutang;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.DateFormatUtils;
import com.rkrzmail.srv.NikitaAutoComplete;
import com.rkrzmail.srv.NsonAutoCompleteAdapter;
import com.rkrzmail.srv.NumberFormatUtils;
import com.rkrzmail.utils.Tools;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import static com.rkrzmail.utils.APIUrls.HUTANG;
import static com.rkrzmail.utils.APIUrls.SET_REKENING_BANK;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.REQUEST_CONTACT;
import static com.rkrzmail.utils.ConstUtils.REQUEST_FOTO_PART;
import static com.rkrzmail.utils.ConstUtils.RP;

public class AturPembayaranHutang_Activity extends AppActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, View.OnFocusChangeListener {

    private EditText etTotalHutang, etDiscRp, etDiscPercent, etTotalBayar, etSelisih,
            etNorek, etNamarek, etNoTrace, etBiayaTf, etNoInvoice;
    private NikitaAutoComplete etBankTerbayar;
    private Spinner spPembayaran, spRekInternal;
    private TextView tvTglBayar, tvTglJatuhTempo;
    private Bitmap bitmapBukti;

    private Nson dataRekeningList = Nson.newArray();
    private String tipePembayaran = "";
    private String namaBank = "", noRek = "";
    private String tipeHutang = "", jenisHutang = "";
    private String noInvoice = "";
    private long tglHutangTimeMilis = 0;

    private int idKasHutang = 0;
    private int totalBayar = 0, totalHutang = 0, selisih = 0;
    private int discount = 0;
    private int noHutang = 0;

    private boolean isHutangBerikutnya = false;
    private boolean isFotoBukti = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pembayaran_hutang);
        initToolbar();
        initComponent();
        loadData();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Pembayaran Hutang");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        etTotalBayar = findViewById(R.id.et_totalBayar_bayarHutang);
        etTotalHutang = findViewById(R.id.et_totalHutang_bayarHutang);
        etDiscRp = findViewById(R.id.et_disc_rupiah);
        etDiscPercent = findViewById(R.id.et_disc_percent);
        etSelisih = findViewById(R.id.et_selisih_bayarHutang);
        etBankTerbayar = findViewById(R.id.et_bankTerbayar);
        etNorek = findViewById(R.id.et_noRek_bayarHutang);
        etNamarek = findViewById(R.id.et_namaRek_bayarHutang);
        etNoTrace = findViewById(R.id.et_noTrace_bayarHutang);
        etBiayaTf = findViewById(R.id.et_biayaTf_bayarHutang);
        spPembayaran = findViewById(R.id.sp_tipe_pembayaran);
        spRekInternal = findViewById(R.id.sp_norek);
        tvTglJatuhTempo = findViewById(R.id.tv_tgl_jatuh_tempo);
        tvTglBayar = findViewById(R.id.tv_tglBayar_bayarPiutang);
        etNoInvoice = findViewById(R.id.et_no_invoice);

        setSpPembayaran();
        setSpRek();
        initAutoCompleteNamaBank();

        etBankTerbayar.setOnFocusChangeListener(this);
        Tools.setViewAndChildrenEnabled(find(R.id.ly_tgl_jatuh_tempo, LinearLayout.class), false);
        find(R.id.ly_tgl_jatuh_tempo).setOnClickListener(this);
        find(R.id.ly_tgl_bayar).setOnClickListener(this);
        find(R.id.btn_simpan_bayarHutang).setOnClickListener(this);
        find(R.id.cb_kirimBukti_bayarHutang, CheckBox.class).setOnCheckedChangeListener(this);
        //find(R.id.btn_foto_bukti).setOnClickListener(this);
        /*etDiscPercent.setOnFocusChangeListener(this);
        etDiscRp.setOnFocusChangeListener(this);
        etDiscPercent.addTextChangedListener(new NumberFormatUtils().percentTextWatcher(etDiscPercent));
        etDiscRp.addTextChangedListener(new NumberFormatUtils().rupiahTextWatcher(etDiscRp));*/
        //etTotalBayar.addTextChangedListener(new NumberFormatUtils().rupiahTextWatcher(etTotalBayar));
        etTotalBayar.addTextChangedListener(totalBayarWatcher);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ly_tgl_bayar:
                getDatePicker(true);
                break;
            case R.id.btn_simpan_bayarHutang:
                if (tvTglBayar.getText().toString().isEmpty()) {
                    viewFocus(tvTglBayar);
                    tvTglBayar.setError("TANGGAL BAYAR HARUS DI ISI");
                } else if (etTotalBayar.getText().toString().equals("Rp. 0") || etTotalBayar.getText().toString().isEmpty()) {
                    viewFocus(etTotalBayar);
                    etTotalBayar.setError("TOTAL BAYAR HARUS DI ISI");
                } else if (find(R.id.tl_total_bayar, TextInputLayout.class).isErrorEnabled()) {
                    viewFocus(etTotalBayar);
                } else if (tipePembayaran.equals("--PILIH--")) {
                    setErrorSpinner(spPembayaran, "TIPE PEMBAYARAN HARUS DI PILIH");
                } else if (tipePembayaran.equals("TRANSFER") && namaBank.isEmpty()) {
                    setErrorSpinner(spRekInternal, "REKENING INTERNAL HARUS DI PILIH");
                } else if (etBankTerbayar.isEnabled() && etBankTerbayar.getText().toString().isEmpty()) {
                    viewFocus(etBankTerbayar);
                    etBankTerbayar.setError("BANK TERBAYAR HARUS DI ISI ");
                } else if (etNoTrace.isEnabled() && etNoTrace.getText().toString().isEmpty()) {
                    viewFocus(etNoTrace);
                    etNoTrace.setError("NO. TRACE HARUS DI ISI ");
                } else if (etBiayaTf.isEnabled() && etBiayaTf.getText().toString().isEmpty()) {
                    viewFocus(etBiayaTf);
                    etBiayaTf.setError("BANK BERBEDA, BIAYA TRANSFER HARUS DI ISI");
                } else if (isHutangBerikutnya) {
                    showDialogHutangBerikutnya();
                } else {
                    saveData();
                }
                break;
            case R.id.ly_tgl_jatuh_tempo:
                getDatePicker(false);
                break;
            case R.id.btn_foto_bukti:
                if (isFotoBukti)
                    uriToCamera();
                else
                    showWarning("CHECK FOTO BUKTI UNTUK MENGIRIM BUKTI BAYAR", Toast.LENGTH_LONG);
                break;

        }
    }

    @SuppressLint("SetTextI18n")
    private void loadData() {
        Nson data = Nson.readJson(getIntentStringExtra(DATA));
        tipeHutang = data.get("TIPE_HUTANG").asString();
        totalHutang = data.get("TOTAL_HUTANG").asInteger();
        if (tipeHutang.equals("KEWAJIBAN")) {
            jenisHutang = data.get("TIPE_KEWAJIBAN").asString();
        } else if (tipeHutang.equals("USAHA")) {
            jenisHutang = "HUTANG USAHA";
        }

        idKasHutang = data.get("HUTANG_ID").asInteger();
        noInvoice = data.get("NO_INVOICE").asString();
        noHutang = data.get("NO_HUTANG_BERIKUTNYA").asInteger() + 1;

        String tglHutang = data.get("TANGGAL").asString();
        tglHutang = DateFormatUtils.formatDate(tglHutang, "yyyy-MM-dd", "dd/MM/yyyy");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date dateHutang = null;
        try{
            dateHutang = sdf.parse(tglHutang);
        }catch (Exception e) {
            e.printStackTrace();
        }
        tglHutangTimeMilis = dateHutang.getTime();

        etNoInvoice.setText(noInvoice);
        etTotalHutang.setText(RP + NumberFormatUtils.formatRp(String.valueOf(totalHutang)));
    }

    private void getDatePicker(final boolean minOrMax) {
        final Calendar cldr = Calendar.getInstance();
        final int day = cldr.get(Calendar.DAY_OF_MONTH);
        final int month = cldr.get(Calendar.MONTH);
        final int year = cldr.get(Calendar.YEAR);
        DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                String newDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                Date date = null;
                try {
                    date = sdf.parse(newDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                String formattedTime = sdf.format(date);
                if (minOrMax)
                    tvTglBayar.setText(formattedTime);
                else
                    tvTglJatuhTempo.setText(formattedTime);
            }
        }, year, month, day);

        datePickerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {

            }
        });

        if(minOrMax){
//            Calendar tanggalHutang = Calendar.getInstance();
//            tanggalHutang.setTimeInMillis(tglHutangTimeMilis);
            datePickerDialog.setMaxDate(cldr);
        }else{
            datePickerDialog.setMinDate(cldr);
        }
        datePickerDialog.show(getFragmentManager(), "Datepickerdialog");
    }

    TextWatcher totalBayarWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @SuppressLint("SetTextI18n")
        @Override
        public void afterTextChanged(Editable editable) {
            String text = editable.toString();
            if (text.isEmpty()) return;
            etTotalBayar.removeTextChangedListener(this);
            text = NumberFormatUtils.formatOnlyNumber(text);
            try {
                totalBayar = Integer.parseInt(text);
                String formatted = RP + NumberFormatUtils.formatRp(text);
                etTotalBayar.setText(formatted);
                etTotalBayar.setSelection(formatted.length());
            } catch (Exception e) {
                e.printStackTrace();
                totalBayar = 0;
            }
           /* String discRp = etDiscRp.getText().toString();
            if (!discRp.isEmpty()) discRp = NumberFormatUtils.formatOnlyNumber(discRp);
            String discPercent = etDiscPercent.getText().toString();
            if (!discPercent.isEmpty()) discPercent = NumberFormatUtils.clearPercent(discPercent);*/

            try {
                if (totalHutang > 0 && totalBayar > 0) {
                   /*
                    if (etDiscRp.isEnabled()) {
                        discount = Integer.parseInt(discRp);
                        totalHutang = totalHutang - discount;
                    } else if (etDiscPercent.isEnabled()) {
                        discount = (int) ((totalHutang * Double.parseDouble(discPercent)) / 100);
                        totalHutang = totalHutang - discount;
                    }*/
                    selisih = totalHutang - totalBayar;
                    if(totalBayar > totalHutang){
                        find(R.id.tl_total_bayar, TextInputLayout.class).setError("TOTAL BAYAR MELEBIHI TOTAL HUTANG");
                        selisih = 0;
                    }else{
                        find(R.id.tl_total_bayar, TextInputLayout.class).setErrorEnabled(false);
                    }

                    isHutangBerikutnya = selisih > 0;
                    Tools.setViewAndChildrenEnabled(find(R.id.ly_tgl_jatuh_tempo, LinearLayout.class), totalBayar < totalHutang);
                    etSelisih.setText(RP + NumberFormatUtils.formatRp(String.valueOf(selisih)));
                }
            } catch (Exception e) {
                showWarning(e.getMessage());
            }

            etTotalBayar.addTextChangedListener(this);
        }
    };

    private void setSpPembayaran() {
        setSpinnerOffline(Arrays.asList("--PILIH--", "TRANSFER", "CASH"), spPembayaran, "");
        spPembayaran.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                tipePembayaran = adapterView.getItemAtPosition(i).toString();
                etBankTerbayar.setEnabled(tipePembayaran.equals("TRANSFER"));
                etNoTrace.setEnabled(tipePembayaran.equals("TRANSFER"));
                etBiayaTf.setEnabled(tipePembayaran.equals("TRANSFER") && !etBankTerbayar.getText().toString().equals(namaBank));
                Tools.setViewAndChildrenEnabled(find(R.id.vg_rek_internal, RelativeLayout.class), tipePembayaran.equals("TRANSFER"));

                if (tipePembayaran.equals("CASH")) {
                    spRekInternal.setSelection(0);
                    etBankTerbayar.setText("");
                    etBiayaTf.setText("");
                    etNoTrace.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

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
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    ArrayList<String> str = new ArrayList<>();
                    result = result.get("data");
                    str.add("--PILIH--");
                    dataRekeningList.add("");
                    for (int i = 0; i < result.size(); i++) {
                        dataRekeningList.add(Nson.newObject()
                                .set("ID", result.get(i).get("ID"))
                                .set("BANK_NAME", result.get(i).get("BANK_NAME"))
                                .set("NO_REKENING", result.get(i).get("NO_REKENING").asString())
                                .set("EDC", result.get(i).get("EDC_ACTIVE"))
                                .set("OFF_US", result.get(i).get("OFF_US"))
                                .set("COMPARISON", result.get(i).get("BANK_NAME").asString() + " - " + result.get(i).get("NO_REKENING").asString()));
                        str.add(result.get(i).get("BANK_NAME").asString() + " - " + result.get(i).get("NO_REKENING").asString());
                    }

                    ArrayList<String> newStr = Tools.removeDuplicates(str);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, newStr);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spRekInternal.setAdapter(adapter);
                } else {
                    showError("GAGAL MEMUAT DATA BANK INTERNAL");
                }
            }
        });

        spRekInternal.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (adapterView.getSelectedItem().toString().equals(dataRekeningList.get(i).get("COMPARISON").asString())) {
                    noRek = dataRekeningList.get(i).get("NO_REKENING").asString();
                    namaBank = dataRekeningList.get(i).get("BANK_NAME").asString();
                } else {
                    noRek = "";
                    namaBank = "";
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
                    showWarning(result.get("message").asString());
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
                etBiayaTf.setEnabled(!bankName.equals(namaBank));
                etBankTerbayar.setText(bankName);
                etBankTerbayar.setSelection(etBankTerbayar.getText().length());
            }
        });
    }

    private void showDialogHutangBerikutnya() {
        showInfoDialog("Konfirmasi", "TOTAL HUTANG KURANG DARI JUMLAH HUTANG \n BUAT HUTANG BARU ?", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                isHutangBerikutnya = true;
                saveData();
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                isHutangBerikutnya = false;
                viewFocus(etTotalBayar);
            }
        });
    }


    private void saveData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                int jumlah1 = totalBayar + Integer.parseInt(NumberFormatUtils.formatOnlyNumber(etBiayaTf.getText().toString()));

                args.put("action", "add");
                args.put("jenis", jenisHutang);
                args.put("tglBayar", DateFormatUtils.formatDateToDatabase(tvTglBayar.getText().toString()));
                args.put("totalBayar", NumberFormatUtils.formatOnlyNumber(String.valueOf(totalBayar)));
                args.put("tipePembayaran", tipePembayaran);
                args.put("namaBankInternal", namaBank);
                args.put("noRekInternal", noRek);
                args.put("namaBankTerbayar", etBankTerbayar.getText().toString());
                args.put("biayaTransfer", NumberFormatUtils.formatOnlyNumber(etBiayaTf.getText().toString()));
                args.put("noTrace", etNoTrace.getText().toString());
                args.put("jumlah1", String.valueOf(jumlah1));
                args.put("total", String.valueOf(totalHutang));
                args.put("frekwensi", "");
                args.put("noHutangBerikutnya", isHutangBerikutnya ? String.valueOf(noHutang) : "");
                args.put("jumlahHutangBerikutnya", isHutangBerikutnya ? String.valueOf(selisih) : "");
                args.put("idKasHutang", String.valueOf(idKasHutang));
                args.put("selesih", String.valueOf(selisih));
                args.put("tglJatuhTempo", DateFormatUtils.formatDateToDatabase(tvTglJatuhTempo.getText().toString()));

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(HUTANG), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("BERHASIL MENAMBAHKAN AKTIVITAS");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError(result.get("message").asString());
                }
            }
        });
    }

  /*  @SuppressLint("NonConstantResourceId")
    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        switch (view.getId()) {
            case R.id.et_disc_percent:
                etDiscRp.setText(" ");
                etDiscRp.setEnabled(!hasFocus);
                break;
            case R.id.et_disc_rupiah:
                etDiscPercent.setText(" ");
                etDiscPercent.setEnabled(!hasFocus);
                break;
        }
    }*/

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (compoundButton.getId() == R.id.cb_kirimBukti_bayarHutang) {
            isFotoBukti = compoundButton.isChecked();
        }
    }

    private void uriToCamera() {
        if (!checkPermission()) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, REQUEST_CONTACT);
        } else {
            if (checkPermission()) {
                requestPermissionAndContinue();
            } else {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, REQUEST_CONTACT);
            }
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CONTACT) {
            Bundle extras = null;
            if (data != null) {
                extras = data.getExtras();
            }
            bitmapBukti = (Bitmap) (extras != null ? extras.get("data") : null);
            find(R.id.btn_foto_bukti, Button.class).setText("Preview Bukti");
        }
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if(view.getId() == R.id.et_bankTerbayar){
            if(namaBank.isEmpty() && hasFocus){
                setErrorSpinner(spRekInternal, "REKENING INTERNAL HARUS DI PILIH TERLEBIH DAHULU");
            }
        }
    }
}
