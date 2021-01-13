package com.rkrzmail.oto.modules.bengkel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.rkrzmail.srv.NumberFormatUtils;
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
    private Nson partIdList;
    private String nominalDonasi = "";
    private String jenis = "";
    private String
            tipePembayaran = "",
            noRek = "",
            namaBankPembayar = "",
            donasi = "",
            namaBank = "",
            offUs = "",
            bankEdc = "";
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
            totalBiaya = 0,
            totalDue = 0,
            totalDp = 0,
            sisaBiayaDp = 0;
    private boolean
            isCheckin = false,
            isJualPart = false,
            isDp = false,
            isDonasi = false,
            isOffUs = false,
            isOnUs = false,
            isMdrKreditCard = false,
            isMdrBank = false,
            isPpn = false;

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

    @SuppressLint("SetTextI18n")
    private void initData() {
        Nson nson = Nson.readJson(getIntentStringExtra(DATA));
        Log.d("test__", "initData: " + nson);
        idCheckin = nson.get("CHECKIN_ID").asInteger();
        idJualPart = nson.get("JUAL_PART_ID").asInteger();
        if (nson.get("JENIS").asString().equals("CHECKIN")) {
            isCheckin = true;
            jenis = "CHECKIN";
            find(R.id.ly_dp).setVisibility(View.GONE);
        } else if (nson.get("JENIS").asString().equals("DP")) {
            Tools.setViewAndChildrenEnabled(find(R.id.tl_reminder, TableLayout.class), false);
            isDp = true;
            jenis = "DP";
            totalDp = nson.get("TOTAL_DP").asInteger();
            sisaBiayaDp = nson.get("SISA_DP").asInteger();
            partIdList = Nson.readJson(getIntentStringExtra("PART_ID_LIST"));

            find(R.id.cb_checkout, CheckBox.class).setChecked(false);
            find(R.id.et_percent_dp, EditText.class).setText(nson.get("DP_PERCENT").asString() + "%");
            find(R.id.et_rp_dp, EditText.class).setText(RP + formatRp(String.valueOf(totalDp)));
            find(R.id.et_sisa_dp, EditText.class).setText(RP + formatRp(String.valueOf(sisaBiayaDp)));
            find(R.id.et_total_biaya, EditText.class).setText(RP + formatRp(String.valueOf(totalDp)));
            find(R.id.et_grandTotal, EditText.class).setText(RP + formatRp(String.valueOf(totalDp)));
        } else {
            Tools.setViewAndChildrenEnabled(find(R.id.tl_reminder, TableLayout.class), false);
            isJualPart = true;
            jenis = "JUAL PART";
            find(R.id.cb_checkout, CheckBox.class).setChecked(false);
            find(R.id.ly_dp).setVisibility(View.GONE);
        }

        if (nson.get("PEMILIK").asString().equals("Y")) {
            find(R.id.cb_pemilik, CheckBox.class).setChecked(true);
        }

        mdrOfUs = nson.get("MDR_OFF_US").asDouble();
        mdrOnUs = nson.get("MDR_ON_US").asDouble();
        mdrKreditCard = nson.get("MDR_KREDIT_CARD").asDouble();
        totalBiaya = isDp ? totalDp : nson.get("TOTAL").asInteger();

        if (nson.get("PKP").asString().equals("Y") && !isDp) {
            totalPpn = (int) (ppn * totalBiaya);
            grandTotal = totalPpn + totalBiaya;
            isPpn = true;
            find(R.id.et_ppn, EditText.class).setText(RP + formatRp(String.valueOf(totalPpn)));
        } else {
            grandTotal = totalBiaya;
        }

        find(R.id.et_total_biaya, EditText.class).setText(RP + formatRp(String.valueOf(totalBiaya)));
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
                    offUs = rekeningList.get(i).get("OFF_US").asString();
                } else {
                    noRek = "";
                    namaBank = "";
                    offUs = "";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void initListener() {
        spTipePembayaran.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tipePembayaran = parent.getItemAtPosition(position).toString();

                if (tipePembayaran.equals("CASH") || tipePembayaran.equals("INVOICE")) {
                    if (!isDp) {
                        find(R.id.et_grandTotal, EditText.class).setText(RP + formatRp(String.valueOf(grandTotal)));
                    }
                    setBlankCash();
                    spNoRek.setEnabled(false);
                    find(R.id.et_noTrack, EditText.class).setEnabled(false);
                    find(R.id.et_namaBankEpay).setEnabled(false);
                    find(R.id.et_totalBayar, EditText.class).setEnabled(true);
                } else {
                    if (tipePembayaran.equals("TRANSFER")) {
                        setSpRek();
                        spNoRek.setEnabled(true);
                        find(R.id.et_namaBankEpay).setEnabled(false);
                        totalDue += grandTotal;
                        if (!isDp) {
                            find(R.id.et_grandTotal, EditText.class).setText(RP + formatRp(String.valueOf(grandTotal)));
                            find(R.id.et_totalBayar, EditText.class).setText(RP + formatRp(String.valueOf(grandTotal)));
                        }
                    } else {
                        isMdrKreditCard = tipePembayaran.equals("KREDIT");

                        grandTotal = totalPpn + totalBiaya;
                        totalMdrOffUs = (int) (((mdrOfUs / 100) * grandTotal));
                        totalMdrOnUs = (int) (((mdrOnUs / 100) * grandTotal));
                        totalMdrKreditCard = (int) (((mdrKreditCard / 100) * grandTotal));

                        find(R.id.et_ppn, EditText.class).setText(isDp ? "" : RP + formatRp(String.valueOf(totalPpn)));
                        find(R.id.et_namaBankEpay).setEnabled(true);
                        spNoRek.setSelection(0);
                        spNoRek.setEnabled(false);
                    }

                    find(R.id.et_noTrack, EditText.class).setEnabled(true);
                    find(R.id.et_totalBayar, EditText.class).setEnabled(false);
                    find(R.id.et_kembalian, EditText.class).setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        find(R.id.et_totalBayar, EditText.class).addTextChangedListener(new NumberFormatUtils().rupiahTextWatcher(find(R.id.et_totalBayar, EditText.class)));
        find(R.id.et_totalBayar, EditText.class).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    if (tipePembayaran.equals("CASH")) {
                        if (!find(R.id.et_totalBayar, EditText.class).getText().toString().isEmpty()) {
                            int bayar = Integer.parseInt(formatOnlyNumber(find(R.id.et_totalBayar, EditText.class).getText().toString()));
                            int totalKembalian = bayar - grandTotal;

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
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void afterTextChanged(Editable editable) {

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
                    if (Integer.parseInt(formatOnlyNumber(find(R.id.et_totalBayar, EditText.class).getText().toString())) < Integer.parseInt(formatOnlyNumber(find(R.id.et_grandTotal, EditText.class).getText().toString()))) {
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
                            if (castRatusRupiah >= 500 || castRatusRupiah == 0) {
                                saveData();
                            } else {
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
                    } else if (find(R.id.et_noTrack, EditText.class).getText().toString().isEmpty()) {
                        find(R.id.et_noTrack, EditText.class).setError("No Trace Belum di Isi");
                        find(R.id.et_noTrack, EditText.class).requestFocus();
                    } else {
                        saveData();
                    }
                } else {
                    if (find(R.id.et_namaBankEpay, NikitaAutoComplete.class).getText().toString().isEmpty()) {
                        find(R.id.et_namaBankEpay, NikitaAutoComplete.class).setError("Masukkan Nama Bank");
                        find(R.id.et_namaBankEpay, NikitaAutoComplete.class).requestFocus();
                    } else if (find(R.id.et_noTrack, EditText.class).getText().toString().isEmpty()) {
                        find(R.id.et_noTrack, EditText.class).setError("No Trace Belum di Isi");
                        find(R.id.et_noTrack, EditText.class).requestFocus();
                    } else {
                        saveData();
                    }
                }
            }
        });
    }

    private void setBlankCash(){
        find(R.id.et_namaBankEpay, NikitaAutoComplete.class).setText("");
        find(R.id.et_percent_disc_merc, EditText.class).setText("");
        find(R.id.et_rp_disc_merc, EditText.class).setText("");
        find(R.id.et_noTrack, EditText.class).setText("");
        find(R.id.et_totalBayar, EditText.class).setText("");
        if (spNoRek.getCount() > 0) {
            spNoRek.setSelection(0);
        }
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
                        saveData();
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
        final Nson data = Nson.readJson(getIntentStringExtra(DATA));
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "add");
                args.put("biayaLayanan", data.get("BIAYA_LAYANAN").asString());
                args.put("discountLayanan", data.get("DISC_LAYANAN").asString());
                args.put("biayaLayananNet", data.get("BIAYA_LAYANAN_NET").asString());
                args.put("hargaPart", data.get("HARGA_PART").asString());
                args.put("discountPart", data.get("DISC_PART").asString());
                args.put("hargaPartNet", data.get("HARGA_PART_NET").asString());
                args.put("biayaJasaLain", data.get("HARGA_JASA_LAIN").asString());
                args.put("discountJasaLain", data.get("DISC_JASA").asString());
                args.put("biayaJasaLainNet", data.get("HARGA_JASA_LAIN_NET").asString());
                args.put("dp", data.get("DP").asString());
                args.put("sisaBiaya", isDp ? String.valueOf(sisaBiayaDp) : data.get("SISA_BIAYA").asString());
                args.put("biayaSimpan", data.get("BIAYA_SIMPAN").asString());
                args.put("discountSpot", data.get("DISC_SPOT").asString());
                args.put("noBuktiBayar", currentDateTime());
                args.put("biayaJasaPart", data.get("HARGA_JASA_PART").asString());
                args.put("discountJasaPart", data.get("DISC_JASA_PART").asString());
                args.put("netJasaPart", data.get("HARGA_JASA_PART_NET").asString());
                args.put("biayaAntarJemput", data.get("BIAYA_ANTAR_JEMPUT").asString());
                args.put("biayaDerek", data.get("BIAYA_DEREK").asString());
                args.put("noPonsel", data.get("NO_PONSEL").asString());
                args.put("noKunci", data.get("NO_KUNCI").asString());
                args.put("nopol", formatNopol(data.get("NOPOL").asString()));
                args.put("partIdList", partIdList == null ? "" : partIdList.toJson());

                args.put("idJualPart", String.valueOf(idJualPart));
                args.put("idCheckin", String.valueOf(idCheckin));
                args.put("tipePembayaran", tipePembayaran);
                args.put("checkOut", find(R.id.cb_checkout, CheckBox.class).isChecked() ? "Y" : "N");
                args.put("kredit", String.valueOf(totalBiaya));
                args.put("jenisAkun", getSetting("TIPE_USER"));
                args.put("partArray", getIntentStringExtra("PART_LIST"));
                args.put("namaBankPembayar", find(R.id.et_namaBankEpay, EditText.class).getText().toString());
                args.put("noTrace", find(R.id.et_noTrack, EditText.class).getText().toString());
                args.put("reminderPelanggan", find(R.id.cb_pelanggan, CheckBox.class).isChecked() ? "Y" : "N");
                args.put("reminderPemilik", find(R.id.cb_pemilik, CheckBox.class).isChecked() ? "Y" : "N");
                args.put("donasi", formatOnlyNumber(nominalDonasi));
                args.put("noInvoice", "?");
                args.put("noKartu", "?");
                args.put("tipePembayaran1", jenis);
                args.put("ppn", String.valueOf(totalPpn));
                args.put("transaksi", jenis);
                args.put("kembalian", formatOnlyNumber(find(R.id.et_kembalian, EditText.class).getText().toString()));
                if (tipePembayaran.equals("CASH")) {
                    int totalPlusDonasi = grandTotal + (!nominalDonasi.equals("") ? Integer.parseInt(nominalDonasi) : 0);
                    args.put("debit", String.valueOf(totalPlusDonasi));
                    args.put("grandTotal", String.valueOf(totalPlusDonasi));
                    args.put("totalBiaya", formatOnlyNumber(find(R.id.et_total_biaya, EditText.class).getText().toString()));
                    args.put("aktivitas", "TERIMA");
                    args.put("nominal", String.valueOf((totalPlusDonasi)));
                    args.put("totalBayar", formatOnlyNumber(find(R.id.et_totalBayar, EditText.class).getText().toString()));
                    args.put("totalDue", formatOnlyNumber(find(R.id.et_totalBayar, EditText.class).getText().toString()));
                    args.put("edc", "");
                    args.put("merchDiscRateRp", "0");
                    args.put("mechDiscRate", "0");
                    args.put("balance", formatOnlyNumber(find(R.id.et_totalBayar, EditText.class).getText().toString()));
                } else {
                    if (tipePembayaran.equals("TRANSFER")) {
                        args.put("aktivitas", "TERIMA");
                        args.put("nominal", String.valueOf(grandTotal));
                        args.put("balance", String.valueOf(totalDue));
                        args.put("merchDiscRateRp", "0");
                        args.put("mechDiscRate", "0");
                        args.put("totalDue", String.valueOf(totalDue));
                        args.put("namaBank", namaBank);
                        args.put("noRekening", noRek);
                        args.put("bankRekInternal", namaBank);
                        args.put("noRekInternal", noRek);
                        args.put("keterangan", "Transfer");
                    } else {
                        if (tipePembayaran.equals("KREDIT")) {
                            args.put("keterangan", "Kredit");
                        } else {
                            args.put("keterangan", "Debet");
                        }
                        if (isJualPart && !find(R.id.et_rp_disc_merc, EditText.class).getText().toString().isEmpty()) {
                            args.remove("transaksi");
                            args.put("aktivitas", "MDR JUAL PART");
                            args.put("transaksi", "MDR JUAL PART");
                        }
                        if (isMdrBank) {
                            args.remove("transaksi");
                            args.put("aktivitas", "MDR BANK");
                            args.put("transaksi", "MDR BANK");
                        } else {
                            args.put("aktivitas", "TERIMA");
                        }
                        args.put("noRekening", "");
                        args.put("namaBank", find(R.id.et_namaBankEpay, EditText.class).getText().toString());
                        args.put("mechDiscRate", find(R.id.et_percent_disc_merc, EditText.class).getText().toString());
                        args.put("merchDiscRateRp", formatOnlyNumber(find(R.id.et_rp_disc_merc, EditText.class).getText().toString()));
                        int totalNominalPkp = isPpn ? grandTotal - totalPpn : grandTotal;
                        args.put("nominal", String.valueOf(totalNominalPkp));
                    }

                    args.put("edc", bankEdc);
                    args.put("totalBiaya", String.valueOf((grandTotal + totalPpn)));
                    args.put("grandTotal", String.valueOf(grandTotal + totalPpn));
                    args.put("totalBayar", String.valueOf(totalDue));
                    args.put("balance", String.valueOf((grandTotal + totalPpn)));
                    //totalDueBy Debit/Kredit
                    if (isOnUs) {
                        args.put("totalDue", String.valueOf(grandTotal + totalMdrOnUs));
                    } else if (isOffUs) {
                        args.put("totalDue", String.valueOf(grandTotal + totalMdrOffUs));
                    } else if (isMdrKreditCard) {
                        args.put("totalDue", String.valueOf(grandTotal + totalMdrKreditCard));
                    }
                }

                if (isDonasi) {
                    args.put("isDonasi", "YA");
                    args.put("nominalDonasi", formatOnlyNumber(nominalDonasi));
                }
                if (isPpn) {
                    args.put("isPpn", "YA");
                    args.put("nominalPpn", String.valueOf(totalPpn));
                    args.put("balancePpn", String.valueOf(totalDue));
                }

                if (isDp) {
                    args.put("isDp", "YA");
                    args.put("status", tipePembayaran + " DP," + " ANTRIAN");
                }

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(ATUR_PEMBAYARAN), args));
            }

            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    if(data.get("NO_PONSEL").asString().length() > 13){
                        showNotification(
                                getActivity(),
                                "PEMBAYARAN",
                                "MESSAGE TIDAK AKTIVE, PRINT BUKTI BAYAR MANDIRI",
                                "PEMBAYARAN",
                                new Intent(getActivity(), Pembayaran_MainTab_Activity.class)
                        );
                    }
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
                    boolean notMdr = false;
                    result = result.get("data");
                    bankEdc = "";
                    for (int i = 0; i < result.size(); i++) {
                        if (result.get(i).get("BANK_NAME").asString().equals(namaBank)) {
                            isOnUs = true;
                            bankEdc = result.get(i).get("BANK_NAME").asString();
                            break;
                        } else {
                            if (result.get(i).get("OFF_US").asString().equals("Y")) {
                                isOffUs = true;
                                bankEdc = result.get(i).get("BANK_NAME").asString();
                            }
                        }
                        if (i == (result.size() - 1) && !isOffUs && !isOnUs) {
                            showWarning("Anda tidak memiliki EDC OFF US!", Toast.LENGTH_LONG);
                            notMdr = true;
                        }
                    }
                    if (!notMdr) {
                        int finalMdrRp = 0;
                        double finalMdrPercent = 0;
                        if (isMdrKreditCard) {
                            finalMdrPercent = mdrKreditCard;
                            finalMdrRp = totalMdrKreditCard;
                        } else {
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
                        isMdrBank = true;
                        totalDue = isDp ? finalMdrRp + totalDp : finalMdrRp + grandTotal;
                        find(R.id.et_percent_disc_merc, EditText.class).setText(finalMdrPercent + "%");
                        find(R.id.et_rp_disc_merc, EditText.class).setText(RP + formatRp(String.valueOf(finalMdrRp)));
                        find(R.id.et_grandTotal, EditText.class).setText(RP + formatRp(String.valueOf(totalDue)));
                        find(R.id.et_totalBayar, EditText.class).setText(RP + formatRp(String.valueOf(totalDue)));
                    }
                } else {
                    showError("Gagal Memuat Data MDR");
                }
            }
        });
    }
}
