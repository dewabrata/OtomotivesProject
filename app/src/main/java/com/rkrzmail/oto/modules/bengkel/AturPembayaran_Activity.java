package com.rkrzmail.oto.modules.bengkel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NikitaAutoComplete;
import com.rkrzmail.srv.NsonAutoCompleteAdapter;
import com.rkrzmail.utils.Tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.ATUR_PEMBAYARAN;
import static com.rkrzmail.utils.APIUrls.SET_REKENING_BANK;
import static com.rkrzmail.utils.APIUrls.VIEW_MST;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.ERROR_INFO;
import static com.rkrzmail.utils.ConstUtils.RP;

public class AturPembayaran_Activity extends AppActivity {

    private Spinner spTipePembayaran, spNoRek;

    private Nson rekeningList = Nson.newArray();
    private String nominalDonasi = "";
    private String jenis = "";
    private String
            tipePembayaran = "",
            noRek = "",
            namaBankPembayar = "",
            donasi = "",
            namaBank = "",
            edcAktif = "",
            offUs = "",
            isPkp = "";
    private double ppn = 0.1;
    private double
            mdrOnUs = 0,
            mdrOfUs = 0,
            mdrKreditCard = 0;
    private int
            idCheckin = 0,
            idJualPart = 0;
    private int
            totalPpn = 0,
            totalMdrOnUs = 0,
            totalMdrOffUs = 0,
            totalMdrKreditCard = 0,
            grandTotal = 0,
            totalBiaya = 0;
    private boolean
            isCheckin = false,
            isJualPart = false,
            isDp = false,
            isDonasi = false,
            isOffUs = false,
            isOnUs = false,
            isMdrKreditCard = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_pembayaran_);
        initToolbar();
        initComponent();
        initData();
        initListener();
    }

    @SuppressLint("NewApi")
    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Pembayaran");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        spTipePembayaran = findViewById(R.id.sp_tipe_pembayaran);
        spNoRek = findViewById(R.id.sp_norek);

        initAutoCompleteNamaBank();
        setSpinnerOffline(new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.tipe_pembayaran))), spTipePembayaran, "");
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
                spNoRek.setAdapter(adapter);
            }
        });

        spNoRek.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (adapterView.getSelectedItem().toString().equals(rekeningList.get(i).get("COMPARISON").asString())) {
                    noRek = rekeningList.get(i).get("NO_REKENING").asString();
                    namaBank = rekeningList.get(i).get("BANK_NAME").asString();
                    edcAktif = rekeningList.get(i).get("EDC").asString();
                    offUs = rekeningList.get(i).get("OFF_US").asString();
                } else {
                    noRek = "";
                    namaBank = "";
                    edcAktif = "";
                    offUs = "";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void initData() {
        Nson nson = Nson.readJson(getIntentStringExtra(DATA));
        if (nson.get("JENIS").asString().equals("CHECKIN")) {
            isCheckin = true;
            idCheckin = nson.get("CHECKIN_ID").asInteger();
            jenis = "CHECKIN";
        } else if (nson.get("JENIS").asString().equals("DP")) {
            Tools.setViewAndChildrenEnabled(find(R.id.tl_reminder, TableLayout.class), false);
            isDp = true;
            jenis = "DP";
            idCheckin = nson.get("CHECKIN_ID").asInteger();
        } else {
            Tools.setViewAndChildrenEnabled(find(R.id.tl_reminder, TableLayout.class), false);
            isJualPart = true;
            jenis = "JUAL PART";
            idJualPart = nson.get("JUAL_PART_ID").asInteger();
        }

        if (nson.get("PEMILIK").asString().equals("Y")) {
            find(R.id.cb_pemilik, CheckBox.class).setChecked(true);
        }

        mdrOfUs = nson.get("MDR_OFF_US").asDouble();
        mdrOnUs = nson.get("MDR_ON_US").asDouble();
        mdrKreditCard = nson.get("MDR_KREDIT_CARD").asDouble();
        totalBiaya = nson.get("TOTAL").asInteger();
        isPkp = nson.get("PKP").asString();

        find(R.id.et_total_biaya, EditText.class).setText(RP + formatRp(String.valueOf(totalBiaya)));
    }

    private void initListener() {
        spTipePembayaran.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tipePembayaran = parent.getItemAtPosition(position).toString();

                if (tipePembayaran.equals("CASH") || tipePembayaran.equals("INVOICE")) {
                    find(R.id.et_grandTotal, EditText.class).setText(RP + formatRp(String.valueOf(totalBiaya)));
                    spNoRek.setEnabled(false);
                    find(R.id.et_noTrack, EditText.class).setEnabled(false);
                    find(R.id.et_namaBankEpay, NikitaAutoComplete.class).setEnabled(false);
                    find(R.id.et_totalBayar, EditText.class).setEnabled(true);
                    find(R.id.et_totalBayar, EditText.class).setText("");
                    if (spNoRek.getCount() > 0) {
                        spNoRek.setSelection(0);
                    }
                } else {
                    if (tipePembayaran.equals("TRANSFER")) {
                        setSpRek();
                        spNoRek.setEnabled(true);
                    } else {
                        isMdrKreditCard = tipePembayaran.equals("KREDIT");

                        totalPpn = (int) (ppn * totalBiaya);
                        grandTotal = totalPpn + totalBiaya;
                        totalMdrOffUs = (int) (((mdrOfUs / 100) * grandTotal) + grandTotal);
                        totalMdrOnUs = (int) (((mdrOnUs / 100) * grandTotal) + grandTotal);
                        totalMdrKreditCard = (int) (((mdrKreditCard / 100) * grandTotal) + grandTotal);

                        find(R.id.et_grandTotal, EditText.class).setText(RP + formatRp(String.valueOf(grandTotal)));
                        find(R.id.et_ppn, EditText.class).setText(RP + formatRp(String.valueOf(totalPpn)));
                        find(R.id.et_totalBayar, EditText.class).setText(RP + formatRp(String.valueOf(grandTotal)));
                        find(R.id.et_namaBankEpay, NikitaAutoComplete.class).setEnabled(true);
                        spNoRek.setSelection(0);
                        spNoRek.setEnabled(false);
                    }

                    find(R.id.et_noTrack, EditText.class).setEnabled(true);
                    find(R.id.et_totalBayar, EditText.class).setEnabled(false);
                    find(R.id.et_kembalian, EditText.class).setText("");
                    find(R.id.et_totalBayar, EditText.class).setText(find(R.id.et_total_biaya, EditText.class).getText().toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        find(R.id.et_totalBayar, EditText.class).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @SuppressLint("SetTextI18n")
            @Override
            public void afterTextChanged(Editable editable) {
                find(R.id.et_totalBayar, EditText.class).removeTextChangedListener(this);

                try {
                    if (tipePembayaran.equals("CASH")) {
                        String cleanString = editable.toString().replaceAll("[^0-9]", "");
                        String formatted = Tools.formatRupiah(cleanString);
                        find(R.id.et_totalBayar, EditText.class).setText(formatted);
                        find(R.id.et_totalBayar, EditText.class).setSelection(formatted.length());

                        if (!find(R.id.et_total_biaya, EditText.class).getText().toString().isEmpty() &&
                                !find(R.id.et_totalBayar, EditText.class).getText().toString().isEmpty()) {
                            int biaya = Integer.parseInt(formatOnlyNumber(find(R.id.et_total_biaya, EditText.class).getText().toString()));
                            int bayar = Integer.parseInt(formatOnlyNumber(find(R.id.et_totalBayar, EditText.class).getText().toString()));
                            int totalKembalian = bayar - biaya;

                            if (totalKembalian < -1) {
                                find(R.id.et_kembalian, EditText.class).setText(RP + "0");
                            } else {
                                find(R.id.et_kembalian, EditText.class).setText(RP + formatRp(String.valueOf(totalKembalian)));
                            }
                        }

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                find(R.id.et_totalBayar, EditText.class).addTextChangedListener(this);
            }
        });

        find(R.id.btn_simpan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tipePembayaran.equals("CASH")) {
                    if (find(R.id.et_totalBayar, EditText.class).getText().toString().isEmpty()) {
                        find(R.id.et_totalBayar, EditText.class).setError("Total Bayar Belum di Isi");
                        return;
                    }
                    if (Integer.parseInt(formatOnlyNumber(find(R.id.et_totalBayar, EditText.class).getText().toString())) < Integer.parseInt(formatOnlyNumber(find(R.id.et_total_biaya, EditText.class).getText().toString()))) {
                        find(R.id.et_totalBayar, EditText.class).setError("Total Bayar Tidak Valid");
                        return;
                    }
                    int kembalian = 0;
                    if (!find(R.id.et_kembalian, EditText.class).getText().toString().isEmpty()) {
                        kembalian = Integer.parseInt(formatOnlyNumber(find(R.id.et_kembalian, EditText.class).getText().toString()));
                    }

                    if (kembalian < 500 && kembalian > 0) {
                        initMssgDonasi(RP + formatRp(String.valueOf(kembalian)), kembalian);
                    } else {
                        String ratusRupiah = String.valueOf(kembalian);
                        if (ratusRupiah.length() >= 3) {
                            ratusRupiah = ratusRupiah.substring(ratusRupiah.length() - 3);
                            int castRatusRupiah = Integer.parseInt(ratusRupiah);
                            castRatusRupiah = castRatusRupiah > 500 && castRatusRupiah < 1000 ? castRatusRupiah - 500 : castRatusRupiah;
                            if(castRatusRupiah >= 500 || castRatusRupiah == 0){
                                saveData();
                            }else{
                                initMssgDonasi(RP + formatRp(String.valueOf(castRatusRupiah)), castRatusRupiah);
                            }

                        } else {
                            saveData();
                        }
                    }

                } else if (tipePembayaran.equals("TRANSFER")) {
                    if (spNoRek.getSelectedItem().toString().equals("--PILIH--")) {
                        showWarning("Rekening Internal Belum di Pilig");
                        spNoRek.performClick();
                    } else {
                        saveData();
                    }
                }
            }
        });
    }

    private void initMssgDonasi(String message, final int kembalianDonasi) {
        Messagebox.showDialog(getActivity(),
                "Konfirmasi", "Pelanggan Setuju Untuk Mendonasikan Kembalian " + message, "Ya", "Tidak", new DialogInterface.OnClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isDonasi = true;
                        showSuccess("Kembalian di Donasikan");
                        nominalDonasi = String.valueOf(kembalianDonasi);
                        saveData();
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

    }

    private void initAutoCompleteNamaBank() {
        find(R.id.et_namaBankEpay, NikitaAutoComplete.class).setThreshold(3);
        find(R.id.et_namaBankEpay, NikitaAutoComplete.class).setAdapter(new NsonAutoCompleteAdapter(getActivity()) {
            @Override
            public Nson onFindNson(Context context, String bookTitle) {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("nama", "BANK");
                args.put("search", bookTitle);
                Nson result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_MST), args));
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

        find(R.id.et_namaBankEpay, NikitaAutoComplete.class).setLoadingIndicator((android.widget.ProgressBar) findViewById(R.id.progress_bar));
        find(R.id.et_namaBankEpay, NikitaAutoComplete.class).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Nson n = Nson.readJson(String.valueOf(adapterView.getItemAtPosition(position)));
                String bankName = n.get("BANK_NAME").asString();
                find(R.id.et_namaBankEpay, NikitaAutoComplete.class).setText(bankName);
                find(R.id.et_namaBankEpay, NikitaAutoComplete.class).setSelection(find(R.id.et_namaBankEpay, NikitaAutoComplete.class).getText().length());
                viewBankBengkel(bankName);

            }
        });
    }

    private void saveData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                Nson data = Nson.readJson(getIntentStringExtra(DATA));

                args.put("action", "add");
                args.put("idCheckin", String.valueOf(idCheckin));
                args.put("aktivitas", tipePembayaran);
                args.put("transaksi", jenis);
                args.put("namaBank", namaBank);
                args.put("noRekening", noRek);
                args.put("keterangan", data.get("KETERANGAN").asString());
                args.put("tipePembayaran", tipePembayaran);
                args.put("biayaLayanan", data.get("BIAYA_LAYANAN").asString());
                args.put("discountLayanan", data.get("DISC_LAYANAN").asString());
                args.put("biayaLayananNet", data.get("BIAYA_LAYANAN_NET").asString());
                args.put("hargaPart", data.get("HARGA_PART").asString());
                args.put("discountPart", data.get("DISC_PART").asString());
                args.put("hargaPartNet", data.get("HARGA_PART_NET").asString());
                args.put("biayaJasaLain", data.get("HARGA_JASA_LAIN").asString());
                args.put("discountJasaLain", data.get("DISC_JASA").asString());
                args.put("biayaJasaLainNet", data.get("HARGA_JASA_LAIN_NET").asString());
                args.put("totalBiaya", formatOnlyNumber(find(R.id.et_total_biaya, EditText.class).getText().toString()));
                args.put("dp", data.get("DP").asString());
                args.put("sisaBiaya", data.get("SISA_BIAYA").asString());
                args.put("biayaSimpan", data.get("BIAYA_SIMPAN").asString());
                args.put("discountSpot", data.get("DISC_SPOT").asString());
                args.put("ppn", formatOnlyNumber(find(R.id.et_ppn, EditText.class).getText().toString()));
                args.put("grandTotal", formatOnlyNumber(find(R.id.et_grandTotal, EditText.class).getText().toString()));
                args.put("merchDiscRateRp", formatOnlyNumber(find(R.id.et_rp_disc_merc, EditText.class).getText().toString()));
                args.put("totalDue", formatOnlyNumber(nominalDonasi));
                args.put("totalBayar", formatOnlyNumber(find(R.id.et_totalBayar, EditText.class).getText().toString()));
                args.put("kembalian", formatOnlyNumber(find(R.id.et_kembalian, EditText.class).getText().toString()));
                args.put("donasi", formatOnlyNumber(nominalDonasi));
                args.put("checkOut", find(R.id.cb_checkout, CheckBox.class).isChecked() ? "Y" : "N");
                args.put("noBuktiBayar", currentDateTime() + "NNN");
                args.put("tipePembayaran1", data.get("").asString());
                args.put("edc", data.get("").asString());
                args.put("bankRekInternal", "0");
                args.put("noRekInternal", noRek);
                args.put("namaBankPembayar", find(R.id.et_namaBankEpay, EditText.class).getText().toString());
                args.put("noKartu", "?");
                args.put("noTrace", find(R.id.et_noTrack, EditText.class).getText().toString());
                args.put("mechDiscRate", find(R.id.et_percent_disc_merc, EditText.class).getText().toString());
                args.put("tanggal", currentDateTime());
                args.put("biayaJasaPart", data.get("HARGA_JASA_PART").asString());
                args.put("discountJasaPart", data.get("DISC_JASA_PART").asString());
                args.put("netJasaPart", data.get("HARGA_JASA_PART_NET").asString());
                args.put("biayaAntarJemput", data.get("BIAYA_ANTAR_JEMPUT").asString());
                args.put("biayaDerek", data.get("BIAYA_DEREK").asString());
                args.put("noInvoice", "0");
                args.put("reminderPelanggan", find(R.id.cb_pelanggan, CheckBox.class).isChecked() ? "Y" : "N");
                args.put("reminderPemilik", find(R.id.cb_pemilik, CheckBox.class).isChecked() ? "Y" : "N");
                args.put("kredit", String.valueOf(totalBiaya));
                args.put("debit", String.valueOf(totalBiaya));
                args.put("nominal", String.valueOf(totalBiaya));
                args.put("balance", String.valueOf(totalBiaya));
                args.put("jenisAkun", getSetting("TIPE_USER"));
                args.put("partArray", getIntentStringExtra("PART_LIST"));
                if (isDonasi) {
                    args.put("isDonasi", "YA");
                    args.put("nominalDonasi", nominalDonasi);
                }

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(ATUR_PEMBAYARAN), args));
            }

            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Sukses Menyimpan Aktifitas");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError(ERROR_INFO);
                }
            }
        });
    }

    private void viewBankBengkel(final String namaBank) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(SET_REKENING_BANK), args));
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data");
                    for (int i = 0; i < result.size(); i++) {
                        if (result.get(i).get("BANK_NAME").asString().equals(namaBank)) {
                            isOnUs = true;
                        } else {
                            if (result.get(i).get("OFF_US").asString().equals("Y")) {
                                isOffUs = true;
                            }
                        }
                        if (i == (result.size() - 1) && !isOffUs && !isOnUs) {
                            showWarning("Anda tidak memiliki EDC OFF US!", Toast.LENGTH_LONG);
                        }
                    }
                    int finalMdrRp = 0;
                    double finalMdrPercent = 0;
                    if(isMdrKreditCard){
                        finalMdrPercent = mdrKreditCard;
                        finalMdrRp = totalMdrKreditCard;
                    }else{
                        if (isOffUs) {
                            showInfo("Anda Menggunakan EDC OFF US");
                            finalMdrPercent = mdrOfUs;
                            finalMdrRp = totalMdrOffUs;
                        }
                        if (isOnUs) {
                            showInfo("Anda Menggunakan EDC ON US");
                            finalMdrPercent = mdrOnUs;
                            finalMdrRp = totalMdrOnUs;
                        }
                    }
                    find(R.id.et_percent_disc_merc, EditText.class).setText(finalMdrPercent + "%");
                    find(R.id.et_rp_disc_merc, EditText.class).setText(RP + formatRp(String.valueOf(finalMdrRp)));
                }
            }
        });
    }
}
