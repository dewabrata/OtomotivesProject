package com.rkrzmail.oto.modules.sparepart;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.sparepart.part_keluar.PengisianPartKeluar_Activity;
import com.rkrzmail.srv.NikitaAutoComplete;
import com.rkrzmail.srv.NsonAutoCompleteAdapter;

import java.util.Map;

public class AturPenjualanPart_Activity extends AppActivity {


    private static final int REQUEST_CARI_PART = 11;
    private static final int REQEST_DAFTAR_JUAL = 12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_penjualan_part_);
        initComponent();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_atur_jualPart);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Penjualan Part");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        find(R.id.btn_lanjut_jualPart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CariPart_Activity.class);
                intent.putExtra("flag", "ALL");
                startActivityForResult(intent, REQUEST_CARI_PART);
            }
        });

        find(R.id.et_jenisKendaraan_jualPart, NikitaAutoComplete.class).setThreshold(3);
        find(R.id.et_jenisKendaraan_jualPart, NikitaAutoComplete.class).setAdapter(new NsonAutoCompleteAdapter(getActivity()) {
            @Override
            public Nson onFindNson(Context context, String bookTitle) {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("search", bookTitle);
                Nson result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("jeniskendaraan"), args));
                return result.get("data");
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.find_jenisken, parent, false);
                }

                findView(convertView, R.id.txtMerk, TextView.class).setText((getItem(position).get("VARIAN").asString()));
                findView(convertView, R.id.txtModel, TextView.class).setText((getItem(position).get("MODEL").asString()));
                findView(convertView, R.id.txtJenisVarian, TextView.class).setText((getItem(position).get("JENIS").asString()));

                return convertView;
            }
        });

        find(R.id.et_jenisKendaraan_jualPart, NikitaAutoComplete.class).setLoadingIndicator((android.widget.ProgressBar) findViewById(R.id.pb));
        find(R.id.et_jenisKendaraan_jualPart, NikitaAutoComplete.class).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Nson n = Nson.readJson(String.valueOf(adapterView.getItemAtPosition(position)));

                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(n.get("JENIS").asString()).append(" ");
                stringBuilder.append(n.get("MODEL").asString()).append(" ");
                stringBuilder.append(n.get("VARIAN").asString()).append(" ");

                find(R.id.et_jenisKendaraan_jualPart, NikitaAutoComplete.class).setText(stringBuilder.toString());
                find(R.id.et_jenisKendaraan_jualPart, NikitaAutoComplete.class).setTag(String.valueOf(adapterView.getItemAtPosition(position)));
            }
        });

        find(R.id.et_noPhone_jualPart, NikitaAutoComplete.class).setThreshold(7);
        find(R.id.et_noPhone_jualPart, NikitaAutoComplete.class).setAdapter(new NsonAutoCompleteAdapter(getActivity()) {
            @Override
            public Nson onFindNson(Context context, String bookTitle) {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                String phone = bookTitle.replace(" ", "").toUpperCase();
                args.put("nomorponsel", phone);
                Nson result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("nomorponsel"), args));

                return result.get("nomorponsel");
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.find_nophone, parent, false);
                }
                findView(convertView, R.id.txtPhone, TextView.class).setText(formatNopol(getItem(position).get("NO_PONSEL").asString()));

                return convertView;
            }
        });

        find(R.id.et_noPhone_jualPart, NikitaAutoComplete.class).setLoadingIndicator((android.widget.ProgressBar) findViewById(R.id.pb2));
        find(R.id.et_noPhone_jualPart, NikitaAutoComplete.class).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Nson n = Nson.readJson(String.valueOf(adapterView.getItemAtPosition(position)));
                find(R.id.et_noPhone_jualPart, NikitaAutoComplete.class).setText(formatNopol(n.get("NO_PONSEL").asString()));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CARI_PART) {
            Nson nson = Nson.readJson(getIntentStringExtra(data, "part"));
            nson.set("jenis", find(R.id.et_jenisKendaraan_jualPart, NikitaAutoComplete.class).getText().toString());
            nson.set("namapelanggan", find(R.id.et_namaPelanggan_jualPart, EditText.class).getText().toString());
            nson.set("nusaha", find(R.id.et_namaUsaha_jualPart, EditText.class).getText().toString());
            nson.set("nohp", find(R.id.et_noPhone_jualPart, NikitaAutoComplete.class).getText().toString());

            Intent i = new Intent(getActivity(), DetailJualPart_Activity.class);
            i.putExtra("part", nson.toJson());
            //Log.d("datanihcuy", "data "+  nson.toJson());
            startActivityForResult(i, REQEST_DAFTAR_JUAL);
        }else if (resultCode == RESULT_OK && requestCode == REQEST_DAFTAR_JUAL) {
            Log.d("datanihcuy", "data "+  Nson.readJson(getIntentStringExtra(data, "part")));
            Intent i = new Intent(getActivity(), DaftarJualPart_Activity.class);
            i.putExtra("data",  Nson.readJson(getIntentStringExtra(data, "part")).toJson());
            startActivityForResult(i, PenjualanPart_Activity.REQUEST_PENJUALAN);
        }else if(resultCode == RESULT_OK && requestCode == PenjualanPart_Activity.REQUEST_PENJUALAN){
            setResult(RESULT_OK);
            finish();
        }
    }
}

