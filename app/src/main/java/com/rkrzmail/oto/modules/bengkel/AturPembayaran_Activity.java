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

import static com.rkrzmail.utils.APIUrls.SET_REKENING_BANK;
import static com.rkrzmail.utils.APIUrls.VIEW_NOMOR_POLISI;
import static com.rkrzmail.utils.ConstUtils.CARI_PART_LOKASI;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.REQUEST_CARI_PART;
import static com.rkrzmail.utils.ConstUtils.RUANG_PART;

public class AturPembayaran_Activity extends AppActivity {

    private Spinner spTipePembayaran, spNoRek;

    private String tipePembayaran = "";

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
                str.add("--PILIH--");
                for (int i = 0; i < result.get("data").size(); i++) {
                    str.add(result.get("data").get(i).get("BANK_NAME").asString() + " - " + result.get("data").get(i).get("NO_REKENING").asString());
                }

                ArrayList<String> newStr = Tools.removeDuplicates(str);
                ArrayAdapter adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, newStr);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spNoRek.setAdapter(adapter);
            }
        });
    }

    private void initData() {
        Nson nson = Nson.readJson(getIntentStringExtra(DATA));
        find(R.id.et_total_biaya, EditText.class).setText(formatRp(nson.get("").asString()));
        find(R.id.et_namaBankEpay, EditText.class).setText(formatRp(nson.get("").asString()));
        find(R.id.et_noTrack, EditText.class).setText(formatRp(nson.get("").asString()));
        find(R.id.et_ppn, EditText.class).setText(formatRp(nson.get("").asString()));
        find(R.id.et_percent_disc_merc, EditText.class).setText(formatRp(nson.get("").asString()));
        find(R.id.et_rp_disc_merc, EditText.class).setText(formatRp(nson.get("").asString()));
        find(R.id.et_grandTotal, EditText.class).setText(formatRp(nson.get("").asString()));
        find(R.id.et_totalBayar, EditText.class).setText(formatRp(nson.get("").asString()));
        find(R.id.et_kembalian, EditText.class).setText(formatRp(nson.get("").asString()));

        find(R.id.cb_pelanggan, CheckBox.class).setChecked(true);
        find(R.id.cb_pemilik, CheckBox.class).setChecked(true);
        find(R.id.cb_checkout, CheckBox.class).setChecked(true);

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
                    find(R.id.et_totalBayar, EditText.class).setVisibility(View.VISIBLE);
                    find(R.id.et_noTrack).setVisibility(View.GONE);
                } else {
                    find(R.id.et_totalBayar, EditText.class).setVisibility(View.GONE);
                    find(R.id.et_noTrack).setVisibility(View.VISIBLE);
                }

                if (tipePembayaran.equals("DEBIT") || tipePembayaran.equals("KREDIT") || tipePembayaran.equals("E-PAY")) {
                    find(R.id.et_namaBankEpay).setVisibility(View.VISIBLE);
                    find(R.id.et_ppn, EditText.class).setVisibility(View.GONE);
                } else {
                    find(R.id.et_ppn, EditText.class).setVisibility(View.VISIBLE);
                    find(R.id.et_namaBankEpay).setVisibility(View.GONE);
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
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(""), args));
            }

            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showInfo("Sukses Menyimpan Aktifitas");
                    startActivity(new Intent(getActivity(), Pembayaran_Activity.class));
                    finish();
                } else {
                    showError("Gagal Menyimpan Aktifitas");
                }
            }
        });
    }
}
