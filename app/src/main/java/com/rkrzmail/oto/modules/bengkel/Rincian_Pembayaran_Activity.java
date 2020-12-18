package com.rkrzmail.oto.modules.bengkel;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.checkin.Checkin2_Activity;
import com.rkrzmail.srv.NikitaMultipleViewAdapter;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.srv.PercentFormat.calculatePercentage;
import static com.rkrzmail.utils.APIUrls.VIEW_PEMBAYARAN;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.DAYS;
import static com.rkrzmail.utils.ConstUtils.ERROR_INFO;
import static com.rkrzmail.utils.ConstUtils.ID;
import static com.rkrzmail.utils.ConstUtils.ONEDAY;
import static com.rkrzmail.utils.ConstUtils.REQUEST_DETAIL;
import static com.rkrzmail.utils.ConstUtils.REQUEST_KONFIRMASI;
import static com.rkrzmail.utils.ConstUtils.REQUEST_NEW_CS;
import static com.rkrzmail.utils.ConstUtils.RINCIAN_JUAL_PART;
import static com.rkrzmail.utils.ConstUtils.RINCIAN_LAYANAN;
import static com.rkrzmail.utils.ConstUtils.RP;

public class Rincian_Pembayaran_Activity extends AppActivity {

    View dialogView;
    RecyclerView rvDetail, rvPartJualPart;
    AlertDialog alertDialog;

    private Nson partList = Nson.newArray();
    private boolean isLayanan = false, isDp = false, isJualPart = false, isMdrOffUs = false, isBatal = false;
    private Nson data;
    private Nson sendData = Nson.newObject();
    private String
            namaPelanggan = "",
            nopol = "",
            noHp = "",
            jenisKendaraan = "",
            layanan = "",
            tanggal = "",
            namaLayanan = "",
            pemilik = "",
            isPkp = "",
            ket = "",
            catatanMekanik = "", merkKendaraan = "";
    private String idCheckin = "", idJualPart = "";
    private String tglLayanan = "";
    private int maxFreePenyimpanan = 0;
    int
            total1 = 0,
            total2 = 0,
            sisaBiaya = 0,
            totalDp = 0,
            totalPart = 0,
            totalJasa = 0,
            totalJasaPart = 0,
            biayaSimpanBengkel = 0,
            biayaLayanan = 0,
            biayaDerek = 0,
            totalBiayaSimpan = 0,
            sisaBiayaDp = 0;
    double
            discPart = 0,
            discJasaPart = 0,
            discJasa = 0,
            discLayanan = 0,
            discSpot = 0,
            discFrekwensi = 0,
            mdrOnUs = 0,
            mdrOfUs = 0,
            mdrCreditCard = 0,
            dpPercent = 0;
    private final double ppn = 0.1;
    private String kodeTipe = "", noRangka = "", noMesin = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_rincian_pembayaran);
        initToolbar();
        initComponent();
    }

    @SuppressLint("NewApi")
    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        data = Nson.readJson(getIntentStringExtra(DATA));
        if (data.containsKey("RINCIAN_CHECKIN")) {
            if (data.get("STATUS").asString().contains("BATAL"))
                isBatal = true;
            idCheckin = data.get(ID).asString();
            sendData.set("CHECKIN_ID", idCheckin);
            sendData.set("PEMILIK", data.get("PEMILIK").asString());
            if (data.get("STATUS").asString().equals("TUNGGU DP")) {
                sendData.set("JENIS", "DP");
                isDp = true;
                find(R.id.ly_rincian_jual_part).setVisibility(View.GONE); //layout rincian pembelian
                Objects.requireNonNull(getSupportActionBar()).setTitle(RINCIAN_LAYANAN);
                find(R.id.row_total_1).setVisibility(View.GONE);
                find(R.id.row_dp).setVisibility(View.GONE);
                find(R.id.row_layanan).setVisibility(View.GONE);
                find(R.id.row_disc_spot).setVisibility(View.GONE);
                find(R.id.row_total_2).setVisibility(View.GONE);
                find(R.id.row_biaya_jasa_part).setVisibility(View.GONE);
                find(R.id.ly_ket).setVisibility(View.GONE);
                find(R.id.et_catatan).setVisibility(View.GONE);
            } else {
                sendData.set("JENIS", "CHECKIN");
                isLayanan = true;
                find(R.id.ly_rincian_jual_part).setVisibility(View.GONE); //layout rincian pembelian
                Objects.requireNonNull(getSupportActionBar()).setTitle(RINCIAN_LAYANAN);
                find(R.id.row_dp_persen).setVisibility(View.GONE);
                find(R.id.row_sisa_dp_persen).setVisibility(View.GONE);
            }
        } else if (data.containsKey("RINCIAN_JUAL_PART")) {
            idJualPart = data.get("JUAL_PART_ID").asString();
            sendData.set("JUAL_PART_ID", idJualPart);
            sendData.set("JENIS", "JUAL PART");
            isJualPart = true;
            find(R.id.ly_rincian_layanan).setVisibility(View.GONE); //layout rincian layanan
            Objects.requireNonNull(getSupportActionBar()).setTitle(RINCIAN_JUAL_PART);
            initRecylerviewPartPembelian();
        }
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        loadData();
        viewRincianPembayaran();
        find(R.id.btn_jasa_part, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewJasaPart();
            }
        });
        find(R.id.btn_data_kendaraan, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Checkin2_Activity.class);
                intent.putExtra(ID, idCheckin);
                intent.putExtra("NO_PONSEL", noHp);
                intent.putExtra("KONFIRMASI DATA", "");
                intent.putExtra("MERK", merkKendaraan);
                startActivityForResult(intent, REQUEST_NEW_CS);
            }
        });
        find(R.id.btn_data_pelanggan, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), KonfirmasiData_Pembayaran_Activity.class);
                i.putExtra(ID, idCheckin);
                i.putExtra("NO_PONSEL", noHp);
                startActivityForResult(i, REQUEST_KONFIRMASI);
            }
        });
        find(R.id.btn_lanjut, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLayanan || isDp) {
                    if (noRangka.isEmpty() || noMesin.isEmpty() || kodeTipe.isEmpty()) {
                        showError("Data Kendaraan Tidak Lengkap", Toast.LENGTH_LONG);
                        find(R.id.btn_data_kendaraan, Button.class).requestFocus();
                    } else {
                        setIntent();
                    }
                } else {
                    setIntent();
                }

            }
        });
    }

    private void loadData() {
        namaPelanggan = data.get("NAMA_PELANGGAN").asString();
        nopol = data.get("NOPOL").asString();
        noHp = data.get("NO_PONSEL").asString();
        tanggal = data.get("TANGGAL").asString();
        jenisKendaraan = data.get("JENIS_KENDARAAN").asString();
        layanan = data.get("LAYANAN").asString();
        pemilik = data.get("PEMILIK").asString();
        ket = data.get("KETERANGAN_TAMBAHAN").asString();
        catatanMekanik = data.get("CATATAN_MEKANIK").asString();
        tglLayanan = data.get("TANGGAL_CHECKIN").asString();
        merkKendaraan = data.get("MERK").asString();
    }


    private void setIntent() {
        sendData.set("BIAYA_LAYANAN", biayaLayanan);
        sendData.set("DISC_LAYANAN", biayaLayanan > 0 ? discLayanan : 0);
        sendData.set("BIAYA_LAYANAN_NET", biayaLayanan > 0 ? biayaLayanan - discLayanan : biayaLayanan);
        sendData.set("HARGA_PART", totalPart);
        sendData.set("DISC_PART", totalPart > 0 ? discPart : 0);
        sendData.set("HARGA_PART_NET", totalPart > 0 ? totalPart - discPart : totalPart);
        sendData.set("HARGA_JASA_LAIN", totalJasa);
        sendData.set("DISC_JASA", totalJasa > 0 ? discJasa : 0);
        sendData.set("HARGA_JASA_LAIN_NET", totalJasa > 0 ? totalJasa - discJasa : totalJasa);
        sendData.set("DP", totalDp);
        sendData.set("SISA_BIAYA", sisaBiaya);
        sendData.set("BIAYA_SIMPAN", totalBiayaSimpan);
        sendData.set("DISC_SPOT", discSpot);
        sendData.set("HARGA_JASA_PART", totalJasaPart);
        sendData.set("DISC_JASA_PART", totalJasaPart > 0 ? discJasaPart : 0);
        sendData.set("HARGA_JASA_PART_NET", totalJasaPart > 0 ? totalJasaPart - discJasaPart : totalJasaPart);
        sendData.set("BIAYA_DEREK", biayaDerek);
        sendData.set("PEMILIK", pemilik);
        sendData.set("MDR_ON_US", mdrOnUs);
        sendData.set("MDR_OFF_US", mdrOfUs);
        sendData.set("MDR_KREDIT_CARD", mdrCreditCard);
        sendData.set("IS_OFF_US", isMdrOffUs);
        sendData.set("PKP", isPkp);
        sendData.set("JENIS", isLayanan ? "CHECKIN" : (isDp ? "DP" : (isJualPart ? "JUAL PART" : "")));
        sendData.set("NO_PONSEL", noHp);
        sendData.set("NOPOL", nopol);
        sendData.set("JUAL_PART_ID", idJualPart);
        sendData.set("DP_PERCENT", dpPercent);
        sendData.set("SISA_DP", sisaBiayaDp);
        sendData.set("TOTAL_DP", totalDp);
        sendData.set("TOTAL", isBatal ? 0 : (total2 > 0 ? total2 : (total1 > 0 ? total1 : (isDp ? totalDp : 0))));

        Intent i = new Intent(getActivity(), AturPembayaran_Activity.class);
        i.putExtra(DATA, sendData.toJson());
        i.putExtra("PART_LIST", partList.toJson());
        startActivityForResult(i, REQUEST_DETAIL);
    }

    private void viewRincianPembayaran() {
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "TRANSAKSI");
                if (isLayanan || isDp) {
                    args.put("jenisPembayaran", "CHECKIN");
                    args.put("checkinId", idCheckin);
                    args.put("detail", "RINCIAN LAYANAN");
                }

                if (isJualPart) {
                    args.put("jenisPembayaran", "JUAL PART");
                    args.put("jualPartId", idJualPart);
                    args.put("detail", "RINCIAN JUAL PART");
                }

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_PEMBAYARAN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    for (int i = 0; i < nListArray.size(); i++) {
                        partList.add(Nson.newObject()
                                .set("PART_ID", nListArray.get(i).get("PART_ID").asString())
                                .set("JUMLAH", nListArray.get(i).get("JUMLAH").asString())
                        );
                    }
                    if (isLayanan || isDp) {
                        loadDataRincianLayanan(result);
                    }
                    if (isJualPart) {
                        loadRincianJualPart(result);
                        rvPartJualPart.getAdapter().notifyDataSetChanged();
                    }
                } else {
                    showError(ERROR_INFO);
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void loadDataRincianLayanan(Nson result) {
        result = result.get("data");
        for (int i = 0; i < result.size(); i++) {
            if (!isBatal) {
                if (isDp && result.get(i).get("DP_DETAIL").asInteger() > 0) {
                    totalPart = result.get(i).get("HARGA_PART").asInteger() * result.get(i).get("JUMLAH").asInteger();
                } else {
                    totalPart += result.get(i).get("HARGA_PART").asInteger();
                }

                totalJasaPart += result.get(i).get("HARGA_JASA_PART").asInteger();
                totalJasa += result.get(i).get("HARGA_JASA_LAIN").asInteger();
                totalDp = result.get(i).get("DP").asInteger();
                sisaBiayaDp = result.get(i).get("SISA").asInteger();

                dpPercent = result.get(i).get("DP_PERSEN").asDouble();
                discPart += result.get(i).get("DISCOUNT_PART").asInteger();
                discJasaPart += result.get(i).get("DISCOUNT_JASA_PART").asInteger();
                discJasa += result.get(i).get("DISCOUNT_JASA_PART").asInteger();
                discLayanan = result.get(i).get("DISCOUNT_LAYANAN").asInteger();
                discSpot = result.get(i).get("DISCOUNT_SPOT").asInteger();
                mdrOnUs = result.get(i).get("MDR_ON_US").asDouble();
                mdrOfUs = result.get(i).get("MDR_OFF_US").asDouble();
                mdrCreditCard = result.get(i).get("MDR_KREDIT_CARD").asDouble();

                maxFreePenyimpanan = result.get(i).get("MAX_FREE_PENYIMPANAN").asInteger();
                biayaDerek = result.get(i).get("BIAYA_DEREK").asInteger();
                biayaSimpanBengkel = result.get(i).get("BIAYA_PENYIMPANAN").asInteger();
                biayaLayanan = result.get(i).get("BIAYA_LAYANAN").asInteger();

                namaLayanan = result.get(i).get("LAYANAN").asString();
                isPkp = result.get(i).get("PKP").asString();

                noMesin = result.get(i).get("NO_MESIN_PELANGGAN").asString();
                noRangka = result.get(i).get("NO_RANGKA_PELANGGAN").asString();
                kodeTipe = result.get(i).get("KODE_TYPE_PELANGGAN").asString();
            }


            if (result.get(i).get("OFF_US").asString().equals("Y")) {
                isMdrOffUs = true;
            }
            if (result.get(i).get("DEREK").asString().equals("Y")) {
                find(R.id.cb_derek, CheckBox.class).setChecked(true);
            }
            if (result.get(i).get("BUANG_PART").asString().equals("Y")) {
                find(R.id.cb_buangPart, CheckBox.class).setChecked(true);
            }
            if (result.get(i).get("ANTAR_JEMPUT").asString().equals("Y")) {
                find(R.id.cb_antar_jemput, CheckBox.class).setChecked(true);
            }

            find(R.id.tv_nama_pelanggan, TextView.class).setText(result.get(i).get("NAMA_PELANGGAN").asString());
            find(R.id.tv_no_ponsel, TextView.class).setText((result.get(i).get("NO_PONSEL").asString()));
            find(R.id.tv_layanan, TextView.class).setText((result.get(i).get("LAYANAN").asString()));
            find(R.id.tv_nopol, TextView.class).setText((result.get(i).get("NOPOL").asString()));
            find(R.id.tv_frek, TextView.class).setText((result.get(i).get("FREKWENSI").asString()));
        }

        parseBiayaSimpan();

        if (discLayanan > 0) {
            discLayanan = calculatePercentage(discLayanan, biayaLayanan);
        }

        total1 = (int) (
                biayaLayanan +
                        totalJasa +
                        totalJasaPart +
                        totalPart +
                        discPart +
                        discJasaPart +
                        discJasa +
                        biayaDerek + totalBiayaSimpan
        );

        if (!isDp && totalDp > 0) {
            sisaBiaya = total1 - totalDp;
        }
        if (discSpot > 0) {
            discSpot = calculatePercentage(discSpot, sisaBiaya > 0 ? sisaBiaya : total1);
            total2 = (int) ((sisaBiaya > 0 ? sisaBiaya : total1) - discSpot);
        }

        find(R.id.row_total_2).setVisibility(total2 == 0 | isDp ? View.GONE : View.VISIBLE);
        find(R.id.row_sisa_biaya).setVisibility(sisaBiaya == 0 | isDp ? View.GONE : View.VISIBLE);
        find(R.id.row_penyimpanan).setVisibility(totalBiayaSimpan == 0 | isDp ? View.GONE : View.VISIBLE);
        find(R.id.row_disc_layanan).setVisibility(discLayanan == 0 | biayaLayanan == 0 | isDp ? View.GONE : View.VISIBLE);
        find(R.id.row_disc_frekwensi).setVisibility(discFrekwensi == 0 | isDp ? View.GONE : View.VISIBLE);
        find(R.id.row_disc_part).setVisibility(discPart == 0 ? View.GONE : View.VISIBLE);
        find(R.id.row_disc_jasa_part).setVisibility(discJasaPart == 0 | isDp ? View.GONE : View.VISIBLE);
        find(R.id.row_disc_jasa_lain).setVisibility(discJasa == 0 | isDp ? View.GONE : View.VISIBLE);
        find(R.id.row_transport).setVisibility(biayaDerek == 0 | isDp ? View.GONE : View.VISIBLE);
        find(R.id.row_dp).setVisibility(totalDp == 0 ? View.GONE : View.VISIBLE);
        find(R.id.row_disc_spot).setVisibility(discSpot == 0 | isDp ? View.GONE : View.VISIBLE);
        find(R.id.row_biaya_jasa_part).setVisibility(totalJasaPart == 0 | isDp ? View.GONE : View.VISIBLE);
        find(R.id.row_biaya_jasa_lain).setVisibility(totalJasa == 0 | isDp ? View.GONE : View.VISIBLE);
        find(R.id.row_biaya_part).setVisibility(totalPart == 0 ? View.GONE : View.VISIBLE);

        find(R.id.tv_sisa_biaya_dp, TextView.class).setText(RP + formatRp(String.valueOf(sisaBiayaDp)));
        find(R.id.tv_dp_percent, TextView.class).setText(dpPercent + "%");
        find(R.id.tv_total_dp, TextView.class).setText(RP + formatRp(String.valueOf(totalDp)));
        find(R.id.tv_biaya_layanan, TextView.class).setText(RP + formatRp(String.valueOf(biayaLayanan)));
        find(R.id.tv_harga_jasa_lain, TextView.class).setText(RP + formatRp(String.valueOf(totalJasa)));
        find(R.id.tv_harga_jasa_part, TextView.class).setText(RP + formatRp(String.valueOf(totalJasaPart)));
        find(R.id.tv_totalPart, TextView.class).setText(RP + formatRp(String.valueOf(totalPart)));
        find(R.id.tv_total_1, TextView.class).setText(RP + (isBatal ? "0" : formatRp(String.valueOf(total1))));
        find(R.id.tv_total_2, TextView.class).setText(RP + formatRp(String.valueOf(total2)));
        find(R.id.tv_sisa_biaya, TextView.class).setText(RP + formatRp(String.valueOf(sisaBiaya)));
        find(R.id.tv_disc_spot, TextView.class).setText(RP + formatRp(String.valueOf(discSpot)));
        find(R.id.tv_harga_penyimpanan, TextView.class).setText(RP + formatRp(String.valueOf(totalBiayaSimpan)));
        find(R.id.tv_disc_layanan, TextView.class).setText(RP + formatRp(String.valueOf(discLayanan)));
        find(R.id.tv_disc_frekwensi, TextView.class).setText(RP + formatRp(String.valueOf(discFrekwensi)));
        find(R.id.tv_disc_part, TextView.class).setText(RP + formatRp(String.valueOf(discPart)));
        find(R.id.tv_disc_jasa_part, TextView.class).setText(RP + formatRp(String.valueOf(discJasaPart)));
        find(R.id.tv_disc_jasa_lain, TextView.class).setText(RP + formatRp(String.valueOf(discJasa)));
        find(R.id.tv_harga_derek_transport, TextView.class).setText(RP + formatRp(String.valueOf(biayaDerek)));
        find(R.id.et_ket_tambahan, EditText.class).setText(ket);
        find(R.id.et_catatan, EditText.class).setText(catatanMekanik);
    }

    private void setDefault(){
        totalJasaPart = 0;
        totalJasa = 0;
        totalPart = 0;
        discPart = 0;
        discJasa = 0;
        total1 = 0;
        discLayanan = 0;
        sisaBiaya = 0;
        total2 = 0;
        discSpot = 0;
    }

    @SuppressLint("SetTextI18n")
    private void loadRincianJualPart(Nson result) {
        result = result.get("data");
        for (int i = 0; i < result.size(); i++) {
            totalPart += result.get(i).get("TOTAL").asInteger();
            discPart += result.get(i).get("DISCOUNT").asDouble();
            mdrOnUs = result.get(i).get("MDR_ON_US").asDouble();
            mdrOfUs = result.get(i).get("MDR_OFF_US").asDouble();
            mdrCreditCard = result.get(i).get("MDR_KREDIT_CARD").asDouble();
            dpPercent = result.get(i).get("DP_PERSEN").asDouble();
            isPkp = result.get(i).get("PKP").asString();

            find(R.id.tv_nama_pelanggan_jual_part, TextView.class).setText(result.get(i).get("NAMA_PELANGGAN").asString());
            find(R.id.tv_no_ponsel_jual_part, TextView.class).setText(result.get(i).get("NO_PONSEL").asString());
            find(R.id.tv_frek_jual_part, TextView.class).setText(result.get(i).get("FREKWENSI").asString());
            if (result.get(i).get("NAMA_USAHA").asString().isEmpty()) {
                find(R.id.tr_usaha).setVisibility(View.GONE);
            } else {
                find(R.id.tv_nama_usaha_jual_part, TextView.class).setText(result.get(i).get("NAMA_USAHA").asString());
            }
        }

        if (discPart > 0) {
            discPart = (discPart / 100) * totalPart;
        } else {
            find(R.id.tr_disc_part).setVisibility(View.GONE);
        }

        int totalKeseluruhan = (int) (totalPart + discPart);

        find(R.id.tv_harga_part_jual_part, TextView.class).setText(RP + formatRp(String.valueOf(totalPart)));
        find(R.id.tv_harga_disc_jual_part, TextView.class).setText(RP + formatRp(String.valueOf(discPart)));
        find(R.id.tv_total_jual_part, TextView.class).setText(RP + formatRp(String.valueOf(totalKeseluruhan)));
        find(R.id.tv_total_ppn_jual_part, TextView.class).setText(RP + formatRp(setPPN(totalKeseluruhan)));
        find(R.id.tv_total_penjualan_jual_part, TextView.class).setText(RP + formatRp(String.valueOf(totalKeseluruhan)));

        sendData.set("TOTAL", totalKeseluruhan);
    }

    private String setPPN(int totalBiaya) {
        if (isPkp.equals("Y")) {
            int totalPPN = (int) (ppn * totalBiaya);
            return String.valueOf(totalPPN);
        }
        return "0";
    }

    @SuppressLint({"NewApi", "SetTextI18n"})
    private void viewJasaPart() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        dialogView = inflater.inflate(R.layout.item_rincian_layanan, null);
        builder.setView(dialogView);

        TextView tvNamaLayanan = dialogView.findViewById(R.id.tv_nama_layanan);
        TextView tvBiayaLayanan = dialogView.findViewById(R.id.tv_biaya_layanan);
        tvNamaLayanan.setText(namaLayanan);
        tvBiayaLayanan.setText(RP + formatRp(String.valueOf(biayaLayanan)));

        initToolbarDialog();
        initRecylerViewRincianCheckin();
        if (nListArray.size() > 0) {
            Objects.requireNonNull(rvDetail.getAdapter()).notifyDataSetChanged();
        }

        builder.create();
        alertDialog = builder.show();
    }

    @SuppressLint("NewApi")
    private void initToolbarDialog() {
        Toolbar toolbar = dialogView.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Part & Jasa");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    @SuppressLint("SetTextI18n")
    private void initRecylerviewPartPembelian() {
        rvPartJualPart = findViewById(R.id.recyclerView_rincian_pembelian);
        rvPartJualPart.setHasFixedSize(true);
        rvPartJualPart.setLayoutManager(new LinearLayoutManager(this));
        rvPartJualPart.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_part_booking3_checkin3) {
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);
                viewHolder.find(R.id.tv_namaPart_booking3_checkin3, TextView.class)
                        .setText(nListArray.get(position).get("NAMA_PART").asString());
                viewHolder.find(R.id.tv_noPart_booking3_checkin3, TextView.class)
                        .setText(nListArray.get(position).get("NO_PART").asString());
                viewHolder.find(R.id.tv_hargaNet_booking3_checkin3, TextView.class).setText(
                        RP + formatRp(nListArray.get(position).get("HARGA_PART").asString()));
                viewHolder.find(R.id.tv_jasaNet_booking3_checkin3, TextView.class).setText(
                        RP + formatRp(nListArray.get(position).get("HARGA_JASA").asString()));
                viewHolder.find(R.id.tv_merk_booking3_checkin3, TextView.class)
                        .setText(nListArray.get(position).get("MERK").asString());
            }
        });
    }

    private void initRecylerViewRincianCheckin() {
        rvDetail = dialogView.findViewById(R.id.recyclerView);
        rvDetail.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true));
        rvDetail.setHasFixedSize(false);
        rvDetail.setAdapter(new NikitaMultipleViewAdapter(nListArray, R.layout.item_part_booking3_checkin3, R.layout.item_jasalain_booking_checkin) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
                super.onBindViewHolder(viewHolder, position);
                final int itemType = getItemViewType(position);

                if (itemType == ITEM_VIEW_1) {
                    viewHolder.find(R.id.tv_namaPart_booking3_checkin3, TextView.class)
                            .setText(nListArray.get(position).get("NAMA_PART").asString());
                    viewHolder.find(R.id.tv_noPart_booking3_checkin3, TextView.class)
                            .setText(nListArray.get(position).get("NO_PART").asString());
                    viewHolder.find(R.id.tv_merk_booking3_checkin3, TextView.class)
                            .setText(nListArray.get(position).get("MERK").asString());
                    viewHolder.find(R.id.tv_no, TextView.class).setVisibility(View.VISIBLE);
                    viewHolder.find(R.id.tv_no, TextView.class).setText(nListArray.get(position).get("NO").asString() + ". ");
                    viewHolder.find(R.id.tv_hargaNet_booking3_checkin3, TextView.class).setText(
                            RP + formatRp(nListArray.get(position).get("HARGA_PART").asString()));
                    viewHolder.find(R.id.tv_jasaNet_booking3_checkin3, TextView.class).setText(
                            RP + formatRp(nListArray.get(position).get("HARGA_JASA_PART").asString()));
                } else if (itemType == ITEM_VIEW_2) {
                    viewHolder.find(R.id.tv_jasaLainNet_booking3_checkin3, TextView.class)
                            .setText(RP + formatRp(nListArray.get(position).get("HARGA_JASA_LAIN").asString()));
                    viewHolder.find(R.id.tv_no, TextView.class).setText(nListArray.get(position).get("NO").asString() + ". ");
                    viewHolder.find(R.id.tv_kelompokPart_booking3_checkin3, TextView.class)
                            .setText(nListArray.get(position).get("KELOMPOK_PART").asString());
                    viewHolder.find(R.id.tv_aktifitas_booking3_checkin3, TextView.class)
                            .setText(nListArray.get(position).get("AKTIVITAS").asString());
                }
            }
        });
    }

    private void parseBiayaSimpan() {
        long tglBayar;
        long tglCheckin;
        try {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date now = sdf.parse(currentDateTime());
            Date dayLayanan = sdf.parse(tglLayanan);
            tglBayar = now.getTime();
            tglCheckin = dayLayanan.getTime();
        } catch (ParseException e) {
            tglBayar = 0;
            tglCheckin = 0;
            showError(e.getMessage());
        }
        if (tglBayar > tglCheckin) {
            long dummy = tglBayar - tglCheckin;
            long maxFree = maxFreePenyimpanan * ONEDAY;
            if (maxFree < dummy) {
                int selisih = DAYS((dummy - maxFree));
                totalBiayaSimpan = biayaSimpanBengkel * selisih;
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_KONFIRMASI) {
            showSuccess("Sukses Memperharui Data Pelanggan");
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_NEW_CS) {
            setDefault();
            viewRincianPembayaran();
            showSuccess("Sukses Memperharui Data Kendraan");
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_DETAIL) {
            setResult(RESULT_OK);
            finish();
            showSuccess("Pembayaran Selesai");
        }
    }
}
