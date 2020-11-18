package com.rkrzmail.oto.modules.bengkel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.sparepart.CariPart_Activity;
import com.rkrzmail.srv.NikitaAutoComplete;
import com.rkrzmail.srv.NsonAutoCompleteAdapter;
import com.rkrzmail.utils.Tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.ATUR_PEMBAYARAN;
import static com.rkrzmail.utils.APIUrls.SET_REKENING_BANK;
import static com.rkrzmail.utils.APIUrls.VIEW_NOMOR_POLISI;
import static com.rkrzmail.utils.ConstUtils.CARI_PART_LOKASI;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.ERROR_INFO;
import static com.rkrzmail.utils.ConstUtils.REQUEST_CARI_PART;
import static com.rkrzmail.utils.ConstUtils.RUANG_PART;

public class AturPembayaran_Activity extends AppActivity {

    private Spinner spTipePembayaran, spNoRek;

    private Nson rekeningList = Nson.newArray();
    private String idCheckin = "", idJualPart = "";
    private String jenis = "";
    private String tipePembayaran = "", noRek = "", namaBankPembayar = "", donasi = "";
    private boolean isCheckin = false, isJualPart = false, isDp = false, isDonasi = false;
    private String nominalDonasi = "";

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
        setSpRek();
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
                            .set("COMPARISON", result.get(i).get("BANK_NAME").asString() + " - " + result.get(i).get("NO_REKENING").asString()));
                    str.add(result.get(i).get("BANK_NAME").asString() + " - " + result.get(i).get("NO_REKENING").asString());
                }

                ArrayList<String> newStr = Tools.removeDuplicates(str);
                ArrayAdapter adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, newStr);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spNoRek.setAdapter(adapter);
            }
        });

        spNoRek.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(adapterView.getSelectedItem().toString().equals(rekeningList.get(i).get("COMPARISON").asString())){
                    noRek = rekeningList.get(i).get("NO_REKENING").asString();

                }
            }
        });
    }

    private void initData() {
        Nson nson = Nson.readJson(getIntentStringExtra(DATA));
        if(nson.get("JENIS").asString().equals("CHECKIN")){
            isCheckin = true;
            idCheckin = nson.get("CHECKIN_ID").asString();
            jenis = "CHECKIN";
        }else if(nson.get("JENIS").asString().equals("DP")){
            Tools.setViewAndChildrenEnabled(find(R.id.tl_reminder, TableLayout.class), false);
            isDp = true;
            jenis = "DP";
            idCheckin = nson.get("CHECKIN_ID").asString();
        }else{
            Tools.setViewAndChildrenEnabled(find(R.id.tl_reminder, TableLayout.class), false);
            isJualPart = true;
            jenis = "JUAL PART";
            idJualPart = nson.get("JUAL_PART_ID").asString();
        }

        if(nson.containsKey("PEMILIK") && nson.get("PEMILIK").asString().equals("Y")){
            find(R.id.cb_pemilik, CheckBox.class).setEnabled(true);
        }

    }

    private void initListener() {
        spTipePembayaran.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tipePembayaran = parent.getItemAtPosition(position).toString();
                if (tipePembayaran.equals("TRANSFER")) {
                    find(R.id.ly_norek).setVisibility(View.VISIBLE);
                } else {
                    find(R.id.ly_norek).setVisibility(View.GONE);
                }

                if (tipePembayaran.equals("CASH") || tipePembayaran.equals("INVOICE")) {
                    find(R.id.tl_totalBayar).setVisibility(View.VISIBLE);
                    find(R.id.tl_noTrack).setVisibility(View.GONE);
                } else {
                    find(R.id.tl_totalBayar).setVisibility(View.GONE);
                    find(R.id.tl_noTrack).setVisibility(View.VISIBLE);
                }

                if (tipePembayaran.equals("DEBIT") || tipePembayaran.equals("KREDIT") || tipePembayaran.equals("E-PAY")) {
                    find(R.id.tl_namaBankEpay).setVisibility(View.VISIBLE);
                    find(R.id.tl_ppn).setVisibility(View.GONE);
                } else {
                    find(R.id.tl_ppn).setVisibility(View.VISIBLE);
                    find(R.id.tl_namaBankEpay).setVisibility(View.GONE);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        find(R.id.btn_simpan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int kembalian = 0;
                if (!find(R.id.et_kembalian, EditText.class).getText().toString().isEmpty()) {
                    kembalian = Integer.parseInt(formatOnlyNumber(find(R.id.et_kembalian, EditText.class).getText().toString()));
                }
                if (kembalian < 500) {
                    initMssgDonasi();
                }else{
                    saveData();
                }
            }
        });
    }

    private void initMssgDonasi() {
        Messagebox.showDialog(getActivity(),
                "Konfirmasi", "Pelanggan Setuju Untuk Mendonasikan Kembalian < Rp.500", "Ya", "Tidak", new DialogInterface.OnClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isDonasi = true;
                        nominalDonasi = find(R.id.et_kembalian, EditText.class).getText().toString();
                        showSuccess("Kembalian di Donasikan");
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

                String nopol = bookTitle.replaceAll(" ", "").toUpperCase();
                args.put("nopol", nopol);

                Nson result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(""), args));
                return result.get("data");
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.item_suggestion, parent, false);
                }
                findView(convertView, R.id.title, TextView.class).setText(formatNopol(getItem(position).get("NOPOL").asString()));
                return convertView;
            }
        });

        find(R.id.et_namaBankEpay, NikitaAutoComplete.class).setLoadingIndicator((android.widget.ProgressBar) findViewById(R.id.pb_nopol_checkin));
        find(R.id.et_namaBankEpay, NikitaAutoComplete.class).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Nson n = Nson.readJson(String.valueOf(adapterView.getItemAtPosition(position)));

            }
        });
    }

    private void saveData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                Nson data = Nson.readJson(getIntentStringExtra(DATA));

                args.put("idCheckin", idCheckin);
                args.put("aktivitas", tipePembayaran);
                args.put("transaksi", jenis);
                args.put("namaBank", find(R.id.et_namaBankEpay, NikitaAutoComplete.class).getText().toString());
                args.put("noRekening", find(R.id.sp_norek, Spinner.class).getSelectedItem().toString());
                args.put("keterangan", data.get("").asString());
                args.put("tipePembayaran",  tipePembayaran);
                args.put("biayaLayanan",  data.get("BIAYA_LAYANAN").asString());
                args.put("discountLayanan",  data.get("DISC_LAYANAN").asString());
                args.put("biayaLayananNet",  data.get("BIAYA_LAYANAN_NET").asString());
                args.put("hargaPart",  data.get("HARGA_PART").asString());
                args.put("discountPart",  data.get("DISC_PART").asString());
                args.put("hargaPartNet",  data.get("HARGA_PART_NET").asString());
                args.put("biayaJasaLain",  data.get("HARGA_JASA_LAIN").asString());
                args.put("discountJasaLain",  data.get("DISC_JASA").asString());
                args.put("biayaJasaLainNet",  data.get("HARGA_JASA_LAIN_NET").asString());
                args.put("totalBiaya", find(R.id.et_total_biaya, EditText.class).getText().toString());
                args.put("dp",  data.get("DP").asString());
                args.put("sisaBiaya",  data.get("SISA_BIAYA").asString());
                args.put("biayaSimpan",  data.get("BIAYA_SIMPAN").asString());
                args.put("discountSpot",  data.get("DISC_SPOT").asString());
                args.put("ppn",   find(R.id.et_ppn, EditText.class).getText().toString());
                args.put("grandTotal",  find(R.id.et_grandTotal, EditText.class).getText().toString());
                args.put("merchDiscRateRp",  find(R.id.et_rp_disc_merc, EditText.class).getText().toString());
                args.put("totalDue",  "?");
                args.put("totalBayar",  find(R.id.et_totalBayar, EditText.class).getText().toString());
                args.put("kembalian",  find(R.id.et_kembalian, EditText.class).getText().toString());
                args.put("donasi",  "?");
                args.put("checkOut",  find(R.id.cb_checkout, CheckBox.class).isChecked() ? "Y" : "N");
                args.put("noBuktiBayar",  "?");
                args.put("tipePembayaran1",  data.get("").asString());
                args.put("edc",  data.get("").asString());
                args.put("bankRekInternal", "?" );
                args.put("noRekInternal",  noRek);
                args.put("namaBankPembayar",   find(R.id.et_namaBankEpay, EditText.class).getText().toString());
                args.put("noKartu",  "?");
                args.put("noTrace",  find(R.id.et_noTrack, EditText.class).getText().toString());
                args.put("mechDiscRate",  find(R.id.et_percent_disc_merc, EditText.class).getText().toString());
                args.put("tanggal", currentDateTime());
                args.put("biayaJasaPart",  data.get("HARGA_JASA_PART").asString());
                args.put("discountJasaPart",  data.get("DISC_JASA_PART").asString());
                args.put("netJasaPart",  data.get("HARGA_JASA_PART_NET").asString());
                args.put("biayaAntarJemput",  data.get("BIAYA_ANTAR_JEMPUT").asString());
                args.put("biayaDerek",  data.get("BIAYA_DEREK").asString());
                args.put("noInvoice",  "?");
                args.put("reminderPelanggan", find(R.id.cb_pelanggan, CheckBox.class).isChecked() ? "Y" : "N");
                args.put("reminderPemilik", find(R.id.cb_pemilik, CheckBox.class).isChecked() ? "Y" : "N");
                args.put("kredit", "");
                args.put("debit", "");
                args.put("nominal", "");
                args.put("balance", "");
                args.put("jenisAkun", "");
                args.put("partArray", getIntentStringExtra("PART_LIST"));
                if(isDonasi){
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
}
