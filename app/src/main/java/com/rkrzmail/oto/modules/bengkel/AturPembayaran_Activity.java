package com.rkrzmail.oto.modules.bengkel;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.naa.data.Nson;
import com.naa.data.UtilityAndroid;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NikitaAutoComplete;
import com.rkrzmail.oto.modules.Adapter.NsonAutoCompleteAdapter;
import com.rkrzmail.srv.NumberFormatUtils;
import com.rkrzmail.utils.Tools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.rkrzmail.utils.APIUrls.ATUR_PEMBAYARAN;
import static com.rkrzmail.utils.APIUrls.GET_QRIS_IMAGE;
import static com.rkrzmail.utils.APIUrls.SET_REKENING_BANK;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.PERMISSION_REQUEST_CODE;
import static com.rkrzmail.utils.ConstUtils.REQUEST_FOTO;
import static com.rkrzmail.utils.ConstUtils.REQUEST_KONFIRMASI;
import static com.rkrzmail.utils.ConstUtils.RP;

public class AturPembayaran_Activity extends AppActivity {

    private Spinner spTipePembayaran, spNoRek;
    private AlertDialog alertDialog;

    private Nson rekeningList = Nson.newArray();
    private Nson partIdList;
    private String nominalDonasi = "";
    private String jenis = "";
    private String namaMerchant = "";
    String namaEwallet = "";
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
            mdrOffUs = 0,
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
            isPpn = false,
            isPelunasanSisaBiaya = false,
            isEwallet = false,
            isLoadEwallet = false;

    private String fotoBuktiBase64 = "";
    private Bitmap bitmapQris = null, bitmapBuktiBayar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_pembayaran);
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
        find(R.id.cb_pemilik, CheckBox.class).setChecked(nson.get("PEMILIK").asString().equals("Y"));

        isPelunasanSisaBiaya = nson.get("PELUNASAN_SISA_BIAYA").asString().equals("Y");
        mdrOffUs = nson.get("MDR_OFF_US").asDouble();
        mdrOnUs = nson.get("MDR_ON_US").asDouble();
        mdrKreditCard = nson.get("MDR_KREDIT_CARD").asDouble();
        grandTotal = nson.get("GRAND_TOTAL").asInteger();
        totalBiaya = isDp ? totalDp : nson.get("TOTAL").asInteger();

        if(nson.get("IS_EWALLET").asBoolean()){
            getQrisImage();
            isLoadEwallet = true;
        }

        if (nson.get("PKP").asString().equals("Y") && !isDp) {
            totalPpn = (int) (ppn * totalBiaya);
            grandTotal += totalPpn;
            isPpn = true;
            find(R.id.et_ppn, EditText.class).setText(RP + formatRp(String.valueOf(totalPpn)));
        }

        boolean isZero = nson.get("IS_ZERO").asBoolean();
        find(R.id.et_total_biaya, EditText.class).setText(RP + formatRp(String.valueOf(grandTotal == 0 && !isZero ? grandTotal : totalBiaya)));
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
                setSpinnerOffline(newStr, spNoRek, "");
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
                isEwallet = parent.getItemAtPosition(position).toString().equals("E-WALLET");

                if(!isLoadEwallet && tipePembayaran.equals("E-WALLET")){
                    showWarning("Rekening E-WALLET Belum di Daftarkan");
                    spTipePembayaran.setSelection(0);
                    spTipePembayaran.performClick();
                }

                find(R.id.btn_qris).setEnabled(isEwallet);
                find(R.id.btn_foto_bukti).setEnabled(isEwallet || tipePembayaran.equals("TRANSFER"));

                if (tipePembayaran.equals("CASH")) {
                    if (!isDp) {
                        find(R.id.et_grandTotal, EditText.class).setText(RP + formatRp(String.valueOf(grandTotal)));
                    }
                    setBlankCash();
                    spNoRek.setEnabled(false);
                    find(R.id.et_noTrack, EditText.class).setEnabled(false);
                    find(R.id.et_namaBankEpay).setEnabled(false);
                    find(R.id.et_totalBayar, EditText.class).setEnabled(true);
                } else if (tipePembayaran.equals("INVOICE")) {
                    find(R.id.et_totalBayar, EditText.class).setEnabled(false);
                    find(R.id.et_totalBayar, EditText.class).setText(RP + "0");
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
                        grandTotal = grandTotal == totalBiaya ? totalPpn + totalBiaya : grandTotal;
                        totalMdrOffUs = (int) (((mdrOffUs / 100) * grandTotal));
                        totalMdrOnUs = (int) (((mdrOnUs / 100) * grandTotal));
                        totalMdrKreditCard = (int) (((mdrKreditCard / 100) * grandTotal));

                        find(R.id.et_totalBayar, EditText.class).setText(" ");
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

            @SuppressLint("SetTextI18n")
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
                switch (tipePembayaran) {
                    case "CASH":
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
                            showDialogDonasi(RP + formatRp(String.valueOf(kembalian)), "", kembalian);
                        } else {
                            String ratusRupiah = String.valueOf(kembalian);
                            if (ratusRupiah.length() >= 3) {
                                ratusRupiah = ratusRupiah.substring(ratusRupiah.length() - 3);
                                int donasi2 = Integer.parseInt(ratusRupiah);
                                int donasi1 = donasi2 > 500 && donasi2 < 1000 ? donasi2 - 500 : donasi2;
                                if (donasi2 == 0) {
                                    saveData();
                                } else {
                                    if (donasi2 == 500) {
                                        showDialogDonasi(RP + formatRp(String.valueOf(donasi1)), "", donasi2);
                                    } else {
                                        showDialogDonasi(RP + formatRp(String.valueOf(donasi1)), RP + formatRp(String.valueOf(donasi2)), donasi2);

                                    }
                                }

                            } else {
                                saveData();
                            }
                        }

                        break;
                    case "TRANSFER":
                        if (spNoRek.getSelectedItem().toString().equals("--PILIH--")) {
                            showWarning("Rekening Internal Belum di Pilih");
                            spNoRek.performClick();
                        } else if (find(R.id.et_noTrack, EditText.class).getText().toString().isEmpty()) {
                            find(R.id.et_noTrack, EditText.class).setError("NO TRACE BELUM DI ISI");
                            find(R.id.et_noTrack, EditText.class).requestFocus();
                        } else {
                            saveData();
                        }
                        break;
                    case "INVOICE":
                        saveData();
                        break;
                    default:
                        if (find(R.id.et_namaBankEpay, NikitaAutoComplete.class).getText().toString().isEmpty()) {
                            find(R.id.et_namaBankEpay, NikitaAutoComplete.class).setError("Masukkan Nama Bank");
                            find(R.id.et_namaBankEpay, NikitaAutoComplete.class).requestFocus();
                        } else if (find(R.id.et_noTrack, EditText.class).getText().toString().isEmpty()) {
                            find(R.id.et_noTrack, EditText.class).setError("No Trace Belum di Isi");
                            find(R.id.et_noTrack, EditText.class).requestFocus();
                        } else {
                            saveData();
                        }
                        break;
                }
            }
        });

        find(R.id.btn_foto_bukti).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //moveToCamera();
                getImagePickOrCamera();
            }
        });

        find(R.id.btn_qris).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bitmapQris != null){
                    showDialogPreviewQris();
                }else{
                    showWarning("Foto QRIS Belum di Masukkan");
                }
            }
        });
    }

    private void moveToCamera() {
        if (!checkPermission()) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, REQUEST_KONFIRMASI);
        } else {
            if (checkPermission()) {
                requestPermissionAndContinue();
            } else {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, REQUEST_KONFIRMASI);
            }
        }
    }

    private void getImagePickOrCamera() {
        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()), new String[]{WRITE_EXTERNAL_STORAGE
                    , READ_EXTERNAL_STORAGE, CAMERA}, PERMISSION_REQUEST_CODE);
        } else {
            final List<Intent> intents = new ArrayList<>();
            intents.add(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
            intents.add(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));

            Intent result = Intent.createChooser(intents.remove(0), null);
            result.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toArray(new Parcelable[]{}));
            startActivityForResult(result, REQUEST_FOTO);
        }
    }


    private void setBlankCash() {
        find(R.id.et_namaBankEpay, NikitaAutoComplete.class).setText("");
        find(R.id.et_percent_disc_merc, EditText.class).setText("");
        find(R.id.et_rp_disc_merc, EditText.class).setText("");
        find(R.id.et_noTrack, EditText.class).setText("");
        find(R.id.et_totalBayar, EditText.class).setText("");
        if (spNoRek.getCount() > 0) {
            spNoRek.setSelection(0);
        }
    }

    private void showDialogDonasi(String donasi1, String donasi2, final int kembalianDonasi) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_donasi, null);
        builder.setView(dialogView);
        alertDialog = builder.create();

        Button btnTidak = dialogView.findViewById(R.id.btn_tidak);
        Button btnDonasi1 = dialogView.findViewById(R.id.btn_donasi1);
        Button btnDonasi2 = dialogView.findViewById(R.id.btn_donasi2);

        btnDonasi1.setText(donasi1);
        if (!donasi2.isEmpty()) {
            btnDonasi2.setText(donasi2);
        } else {
            btnDonasi2.setVisibility(View.GONE);
        }

        btnTidak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                saveData();
            }
        });

        btnDonasi1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isDonasi = true;
                showSuccess("Kembalian di Donasikan");
                nominalDonasi = String.valueOf(kembalianDonasi);
                saveData();
            }
        });

        btnDonasi2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isDonasi = true;
                showSuccess("Kembalian di Donasikan");
                nominalDonasi = String.valueOf(kembalianDonasi);
                saveData();
            }
        });

        alertDialog.setCancelable(false);
        if (alertDialog.getWindow() != null)
            alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        alertDialog.show();
    }

    private void initAutoCompleteNamaBank() {
        find(R.id.et_namaBankEpay, NikitaAutoComplete.class).setThreshold(3);
        find(R.id.et_namaBankEpay, NikitaAutoComplete.class).setAdapter(new NsonAutoCompleteAdapter(getActivity()) {
            @Override
            public Nson onFindNson(Context context, String bookTitle) {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "AUTO COMPLETE");
                args.put("isEwallet", isEwallet ? "Y" : "N");
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
                findView(convertView, R.id.title, TextView.class).setText(isEwallet ? (getItem(position).get("NAMA_EWALLET").asString()) : (getItem(position).get("BANK_NAME").asString()));
                return convertView;
            }
        });

        find(R.id.et_namaBankEpay, NikitaAutoComplete.class).setLoadingIndicator((android.widget.ProgressBar) findViewById(R.id.progress_bar));
        find(R.id.et_namaBankEpay, NikitaAutoComplete.class).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Nson n = Nson.readJson(String.valueOf(adapterView.getItemAtPosition(position)));
                String bankNameOrEpay = n.get("BANK_NAME").asString();
                if (isEwallet) {
                    bankNameOrEpay = n.get("NAMA_EWALLET").asString();
                } else {
                    bankNameOrEpay = n.get("BANK_NAME").asString();
                }
                find(R.id.et_namaBankEpay, NikitaAutoComplete.class).setText(bankNameOrEpay);
                find(R.id.et_namaBankEpay, NikitaAutoComplete.class).setSelection(find(R.id.et_namaBankEpay, NikitaAutoComplete.class).getText().length());
                viewBankBengkel(bankNameOrEpay);

            }
        });
    }

    private void saveData() {
        final Nson data = Nson.readJson(getIntentStringExtra(DATA));
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "add");
                args.put("jumlahPart", data.get("JUMLAH_PART").asString());
                args.put("hppPart", data.get("HPP").asString());
                args.put("isPelunasanSisaBiaya", isPelunasanSisaBiaya ? "Y" : "N");
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
                args.put("biayaJasaPart", data.get("HARGA_JASA_PART").asString());
                args.put("discountJasaPart", data.get("DISC_JASA_PART").asString());
                args.put("netJasaPart", data.get("HARGA_JASA_PART_NET").asString());
                args.put("biayaAntarJemput", data.get("BIAYA_ANTAR_JEMPUT").asString());
                args.put("biayaDerek", data.get("BIAYA_DEREK").asString());
                args.put("noPonsel", data.get("NO_PONSEL").asString());
                args.put("noKunci", data.get("NO_KUNCI").asString());
                args.put("nopol", formatNopol(data.get("NOPOL").asString()));
                args.put("partIdList", partIdList == null ? "" : partIdList.toJson());
                args.put("partsList", getIntentStringExtra("PART_LIST"));
                args.put("googleReview", find(R.id.cb_google_review, CheckBox.class).isChecked() ? "Y" : "N");

                args.put("idJualPart", String.valueOf(idJualPart));
                args.put("idCheckin", String.valueOf(idCheckin));
                args.put("tipePembayaran", tipePembayaran);
                args.put("checkOut", find(R.id.cb_checkout, CheckBox.class).isChecked() ? "Y" : "N");
                args.put("kredit", String.valueOf(totalBiaya));
                args.put("jenisAkun", getSetting("TIPE_USER"));
                args.put("namaBankPembayar", find(R.id.et_namaBankEpay, EditText.class).getText().toString());
                args.put("noTrace", find(R.id.et_noTrack, EditText.class).getText().toString());
                args.put("reminderPelanggan", find(R.id.cb_pelanggan, CheckBox.class).isChecked() ? "Y" : "N");
                args.put("reminderPemilik", find(R.id.cb_pemilik, CheckBox.class).isChecked() ? "Y" : "N");
                args.put("donasi", formatOnlyNumber(nominalDonasi));
                args.put("noInvoice", "");
                args.put("noKartu", "");
                args.put("tipePembayaran1", jenis);
                args.put("ppn", String.valueOf(totalPpn));
                args.put("transaksi", jenis);
                args.put("kembalian", formatOnlyNumber(find(R.id.et_kembalian, EditText.class).getText().toString()));
                args.put("fotoBukti", fotoBuktiBase64);

                if (tipePembayaran.equals("CASH")) {
                    int totalPlusDonasi = grandTotal + (!nominalDonasi.isEmpty() ? Integer.parseInt(nominalDonasi) : 0);
                    args.put("debit", String.valueOf(totalPlusDonasi));
                    args.put("grandTotal", String.valueOf(grandTotal));
                    args.put("totalBiaya", formatOnlyNumber(find(R.id.et_total_biaya, EditText.class).getText().toString()));
                    args.put("aktivitas", "TERIMA");
                    args.put("nominal", String.valueOf((totalPlusDonasi)));
                    args.put("totalBayar", formatOnlyNumber(find(R.id.et_totalBayar, EditText.class).getText().toString()));
                    args.put("totalDue", String.valueOf(grandTotal));
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
                    } else if (tipePembayaran.equals("INVOICE")) {
                        args.put("totalDue", "0");
                        args.put("totalBayar", "0");
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
                        //args.put("namaBank", find(R.id.et_namaBankEpay, EditText.class).getText().toString());
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
                        args.put("isOnUs", "Y");
                    } else if (isOffUs) {
                        args.put("totalDue", String.valueOf(grandTotal + totalMdrOffUs));
                        args.put("isOffUs", "Y");
                    } else if (isMdrKreditCard) {
                        args.put("totalDue", String.valueOf(grandTotal + totalMdrKreditCard));
                        args.put("isMdrKreditCard", "Y");
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

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    showSuccess("Sukses Menyimpan Aktifitas");
                    AppApplication.getMessageTrigger();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError(result.get("message").asString());
                }
            }
        });
    }

    private void setDisabledEwallet(boolean isEnable) {
        spNoRek.setEnabled(isEnable);
    }

    private void viewBankBengkel(final String namaBankOrEwallet) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                args.put("isEwallet", isEwallet ? "Y" : "N");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(SET_REKENING_BANK), args));
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    boolean notMdr = false;
                    result = result.get("data");
                    bankEdc = "";

                    if (result.size() > 0) {
                        if (isEwallet) {
                            result = result.get(0);
                            mdrOnUs = result.get("MDR_ON_US").asDouble();
                            mdrOffUs = result.get("MDR_OFF_US").asDouble();

                            if (result.get("NAMA_EWALLET").asString().equals(namaBankOrEwallet)) {
                                isOnUs = true;
                            } else {
                                isOffUs = true;
                            }
                        } else {
                            for (int i = 0; i < result.size(); i++) {
                                if (result.get(i).get("BANK_NAME").asString().equals(namaBankOrEwallet)) {
                                    isOnUs = true;
                                    bankEdc = result.get(i).get("BANK_NAME").asString();
                                    break;
                                } else {
                                    if (result.get(i).get("OFF_US").asString().equals("Y")) {
                                        isOffUs = true;
                                        bankEdc = result.get(i).get("BANK_NAME").asString();
                                    }
                                }
                                if (i == (result.size() - 1) && (!isOffUs && !isOnUs)) {
                                    showWarning("Anda tidak memiliki EDC ON US!", Toast.LENGTH_LONG);
                                    notMdr = true;
                                    isOffUs = true;
                                }
                            }
                        }
                    } else {
                        isOffUs = true;
                        isOnUs = false;
                    }

                    double finalMdrPercent = 0;
                    int finalMdrRp = 0;

                    if (isEwallet) {
                        totalMdrOffUs = (int) (((mdrOffUs / 100) * grandTotal));
                        totalMdrOnUs = (int) (((mdrOnUs / 100) * grandTotal));
                        if (isOffUs) {
                           // showInfo("Anda Menggunakan EDC OFF US");
                            finalMdrPercent = mdrOffUs;
                            finalMdrRp = totalMdrOffUs;
                        }
                        if (isOnUs) {
                           // showInfo("Anda Menggunakan EDC ON US");
                            finalMdrPercent = mdrOnUs;
                            finalMdrRp = totalMdrOnUs;
                        }

                        isMdrBank = true;
                        totalDue = isDp ? finalMdrRp + totalDp : finalMdrRp + grandTotal;
                    } else {
                        if (!notMdr) {
                            if (isMdrKreditCard) {
                                finalMdrPercent = mdrKreditCard;
                                finalMdrRp = totalMdrKreditCard;
                            } else {
                                if (isOffUs) {
                                    showInfo("Anda Menggunakan EDC OFF US");
                                    finalMdrPercent = mdrOffUs;
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
                        }
                    }

                    find(R.id.et_percent_disc_merc, EditText.class).setText(finalMdrPercent + "%");
                    find(R.id.et_rp_disc_merc, EditText.class).setText(RP + formatRp(String.valueOf(finalMdrRp)));
                    find(R.id.et_grandTotal, EditText.class).setText(RP + formatRp(String.valueOf(totalDue)));
                    find(R.id.et_totalBayar, EditText.class).setText(RP + formatRp(String.valueOf(totalDue)));
                } else {
                    showError(result.get("message").asString(), Toast.LENGTH_LONG);
                }
            }
        });
    }

    private void getQrisImage() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                String[] args = new String[3];
                args[0] = "CID=" + UtilityAndroid.getSetting(getApplicationContext(), "CID", "").trim();
                result = Nson.readJson(InternetX.getHttpConnectionX(AppApplication.getBaseUrlV4(GET_QRIS_IMAGE), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asBoolean()) {
                    result = result.get("data");
                    namaMerchant = result.get("NAMA_MERCHANT").asString();
                    if (!result.get("QRIS_IMAGE").asString().isEmpty()) {
                        String base64String = result.get("QRIS_IMAGE").asString();
                        byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);
                        bitmapQris = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    }
                }
            }
        });

    }

    @SuppressLint("SetTextI18n")
    private void showDialogPreviewQris() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_alert_camera, null);
        builder.setView(dialogView);
        alertDialog = builder.create();

        Toolbar toolbar = dialogView.findViewById(R.id.toolbar);
        TextView tvTittle = dialogView.findViewById(R.id.tv_tittle_img);
        ImageView img = (ImageView) dialogView.findViewById(R.id.img_alert_foto);
        Button btnCancel = dialogView.findViewById(R.id.btn_alert_cancel);
        Button btnSimpan = dialogView.findViewById(R.id.btn_alert_save);

        tvTittle.setVisibility(View.VISIBLE);
        btnSimpan.setVisibility(View.GONE);
        tvTittle.setText(namaMerchant);
        btnCancel.setText("TUTUP");

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle("Qris");

        if (bitmapQris != null)
            img.setImageBitmap(bitmapQris);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });


        if (alertDialog.getWindow() != null)
            alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        alertDialog.show();
    }

    private void getImageUri(final Uri imageUri, final Bundle imgBundle) {
        newProses(new Messagebox.DoubleRunnable() {
            @Override
            public void run() {
                if (imageUri != null) {
                    ImageView dummy = new ImageView(getActivity());
                    dummy.setImageURI(imageUri);
                    BitmapDrawable drawable = (BitmapDrawable) dummy.getDrawable();
                    bitmapBuktiBayar = drawable.getBitmap();
                } else {
                    bitmapBuktiBayar = (Bitmap) (imgBundle != null ? imgBundle.get("data") : null);
                }

                if (bitmapBuktiBayar != null) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    bitmapBuktiBayar.compress(Bitmap.CompressFormat.PNG, 100, bos);
                }
            }

            @Override
            public void runUI() {
               showSuccess("BERHASIL MENYIMPAN BUKTI BAYAR");
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_KONFIRMASI) {
                Bundle extras = null;
                if (data != null) {
                    extras = data.getExtras();
                }
                bitmapQris = (Bitmap) (extras != null ? extras.get("data") : null);
                fotoBuktiBase64 = bitmapToBase64(bitmapQris);
            }else if(requestCode == REQUEST_FOTO){
                try {
                    Bundle extras = null;
                    Uri imageUri = null;
                    if (data != null) {
                        extras = data.getExtras();
                        if (extras == null)
                            imageUri = data.getData();
                    }
                    getImageUri(imageUri, extras);
                } catch (Exception e) {
                    showError(e.getMessage(), Toast.LENGTH_LONG);
                }
            }
        }
    }
}
