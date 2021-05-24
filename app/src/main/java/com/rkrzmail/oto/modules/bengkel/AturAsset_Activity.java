package com.rkrzmail.oto.modules.bengkel;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.data.UtilityAndroid;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.mekanik.PerintahKerjaMekanik_Activity;
import com.rkrzmail.srv.DateFormatUtils;
import com.rkrzmail.srv.NumberFormatUtils;
import com.rkrzmail.utils.Tools;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.ASSET;
import static com.rkrzmail.utils.APIUrls.ATUR_KONTROL_LAYANAN;
import static com.rkrzmail.utils.APIUrls.JURNAL;
import static com.rkrzmail.utils.APIUrls.SET_REKENING_BANK;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.ERROR_INFO;
import static com.rkrzmail.utils.ConstUtils.REQUEST_CONTACT;
import static com.rkrzmail.utils.ConstUtils.RP;

public class AturAsset_Activity extends AppActivity implements View.OnClickListener {

    private Spinner spStatus, spTipePembayaran, spNoRek;

    private Nson data;
    private Nson rekeningList = Nson.newArray();
    private int idAsset = 0;
    private String namaBank = "", noRek = "";
    private String status = "", tipePembayaran = "";
    private boolean isPembeli = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_asset);
        initToolbar();
        initComponent();
        initData();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Update Status Aset");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @SuppressLint("SetTextI18n")
    private void initData() {
        data = Nson.readJson(getIntentStringExtra(DATA));
        idAsset = data.get("ID").asInteger();
        find(R.id.et_nama_asset, EditText.class).setText(data.get("NAMA_ASET").asString());
        //find(R.id.et_no_asset, EditText.class).setText(data.get("NILAI_PENYUSUTAN").asString());
        find(R.id.et_nilai_sisa, EditText.class).setText(RP + NumberFormatUtils.formatRp(data.get("NILAI_SISA").asString()));

        setSpinnerOffline(Arrays.asList("--PILIH--", "TRANSFER", "CASH"), spTipePembayaran, data.get("TIPE_PEMBAYARAN").asString());
        setSpinnerOffline(Arrays.asList("--PILIH--", "TERJUAL", "RUSAK TOTAL", "HILANG"), spStatus, data.get("STATUS").asString());
    }


    private void initComponent() {
        spStatus = findViewById(R.id.sp_status);
        spTipePembayaran = findViewById(R.id.sp_tipe_pembayaran);
        spNoRek = findViewById(R.id.sp_norek);

        spStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                status = adapterView.getItemAtPosition(i).toString();

                Tools.setViewAndChildrenEnabled(find(R.id.vg_kontak, RelativeLayout.class), status.equals("TERJUAL"));//ly_pembayaran
                Tools.setViewAndChildrenEnabled(find(R.id.ly_pembayaran, RelativeLayout.class), status.equals("TERJUAL"));
                isPembeli = status.equals("TERJUAL");
                find(R.id.et_harga_jual).setEnabled(status.equals("TERJUAL"));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spTipePembayaran.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                tipePembayaran = adapterView.getItemAtPosition(i).toString();
                Tools.setViewAndChildrenEnabled(find(R.id.ly_norek, RelativeLayout.class), tipePembayaran.equals("TRANSFER"));
                if (!tipePembayaran.equals("TRANSFER")) {
                    spNoRek.setSelection(0);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        setSpRek();
        find(R.id.et_harga_jual, EditText.class).addTextChangedListener(new NumberFormatUtils().rupiahTextWatcher(find(R.id.et_harga_jual, EditText.class)));
        find(R.id.vg_kontak).setOnClickListener(this);
        find(R.id.btn_simpan).setOnClickListener(this);
    }

    @SuppressLint({"NonConstantResourceId", "IntentReset"})
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.vg_kontak:
                try {
                    Uri uri = Uri.parse("content://contacts");
                    Intent intent = new Intent(Intent.ACTION_PICK, uri);
                    intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                    startActivityForResult(intent, REQUEST_CONTACT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_simpan:

                saveData();
                break;
        }
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
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, newStr);
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

    private void saveData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                RequestBody formBody = new FormEncodingBuilder()
                        .add("CID", UtilityAndroid.getSetting(getApplicationContext(), "CID", "").trim())
                        .add("status", status)
                        .add("id", String.valueOf(idAsset))
                        .add("tipe_pembayaran", tipePembayaran)
                        .add("no_rekening", noRek)
                        .add("nama_bank", namaBank)
                        .add("harga_jual", NumberFormatUtils.formatOnlyNumber(find(R.id.et_harga_jual, EditText.class).getText().toString()))
                        .add("keterangan", find(R.id.et_keterangan, EditText.class).getText().toString())
                        .add("no_ponsel_pembeli", NumberFormatUtils.formatOnlyNumber(find(R.id.tv_pembeli, TextView.class).getText().toString()))
                        .add("nama_pembeli", find(R.id.tv_pembeli, TextView.class).getText().toString().replaceAll("[^a-zA-Z]", ""))
                        .add("tanggal_jatuh_tempo", "")
                        .add("tanggal", currentDateTime("yyyy-MM-dd"))
                        .build();
                try {
                    Request request = new Request.Builder()
                            .url(AppApplication.getBaseUrlV4(ASSET))
                            .post(formBody)
                            .build();
                    OkHttpClient client = new OkHttpClient();
                    Response response = client.newCall(request).execute();
                    result = Nson.readJson(response.body().string());
                } catch (final Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showError(e.getMessage());
                        }
                    });
                }
            }

            @Override
            public void runUI() {
                if (result.get("status").asBoolean()) {
                    showSuccess("AKTIVITAS BERHASIL DI PERBAHARUI");
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showInfo(ERROR_INFO);
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CONTACT) {
            Uri contactData = data != null ? data.getData() : null;
            Cursor cursor = managedQuery(contactData, null, null, null, null);
            cursor.moveToFirst();
            String number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
            String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            find(R.id.tv_pembeli, TextView.class).setText(contactName + "\n" + number);
        }
    }

}
