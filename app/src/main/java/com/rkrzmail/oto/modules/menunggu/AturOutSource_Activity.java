package com.rkrzmail.oto.modules.menunggu;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.MultiSelectionSpinner;
import com.rkrzmail.utils.Tools;

import java.util.Map;

public class AturOutSource_Activity extends AppActivity implements View.OnClickListener {

    private static final int REQUEST_CONTACT = 10;
    private EditText etLayanan, etTglCheckin, etBiaya;
    private Spinner spNopol, spKondisi, spPembayaran, spNoRek;
    private MultiSelectionSpinner spJasa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_out_source_);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Out Source");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent(){
        initToolbar();
        etLayanan = findViewById(R.id.et_layanan_outS);
        etTglCheckin = findViewById(R.id.et_tglCheckin_outS);
        etBiaya = findViewById(R.id.et_totalBiaya_outS);
        spJasa = findViewById(R.id.sp_namaJasa_outS);
        spNopol = findViewById(R.id.sp_nopol_outS);
        spKondisi = findViewById(R.id.sp_kondisiPart_outS);
        spPembayaran = findViewById(R.id.sp_pembayaran_outS);
        spNoRek = findViewById(R.id.sp_noRek_outS);

        find(R.id.tv_tglSelesai_outS).setOnClickListener(this);
        find(R.id.tv_supplier_outS).setOnClickListener(this);

        find(R.id.btn_simpan_outS).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });
    }

    private void saveData() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
//                args.put("action", "view");
//                args.put("search", cari);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(""), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                } else {
                    showInfo("GAGAL!");
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_tglSelesai_outS:
                getDatePickerDialogTextView(getActivity(), find(R.id.tv_tglSelesai_outS, TextView.class));
                break;
            case R.id.tv_supplier_outS:
                try {
                    Uri uri = Uri.parse("content://contacts");
                    Intent intent = new Intent(Intent.ACTION_PICK, uri);
                    intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                    startActivityForResult(intent, REQUEST_CONTACT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CONTACT) {
            if (resultCode == RESULT_OK) {
                Uri contactData = data.getData();
                Cursor cursor = managedQuery(contactData, null, null, null, null);
                cursor.moveToFirst();
                String number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                find(R.id.tv_supplier_outS, TextView.class).setText(contactName + "\n" + number);
            }
        }
    }
}
