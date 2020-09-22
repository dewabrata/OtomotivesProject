package com.rkrzmail.oto.modules.sparepart;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NikitaAutoComplete;
import com.rkrzmail.srv.NsonAutoCompleteAdapter;

import java.util.Map;

public class AturPenjualanPart_Activity extends AppActivity {

    private static final int REQUEST_CARI_PART = 11;
    private static final int REQEST_DAFTAR_JUAL = 12;
    public static final String ERROR = "Silahkan Isi ";
    private String noHp = "";
    private boolean isNoHp = false;

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
               if (find(R.id.et_namaPelanggan_jualPart, EditText.class).getText().toString().isEmpty() || find(R.id.et_namaPelanggan_jualPart, EditText.class).getText().toString().length() < 5) {
                    find(R.id.et_namaPelanggan_jualPart, EditText.class).setError(ERROR);
                    find(R.id.et_namaPelanggan_jualPart, EditText.class).requestFocus();
                }else{
                    setIntent();
                }
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

                findView(convertView, R.id.txtJenis, TextView.class).setText((getItem(position).get("JENIS").asString()));
                findView(convertView, R.id.txtVarian, TextView.class).setText((getItem(position).get("VARIAN").asString()));
                findView(convertView, R.id.txtMerk, TextView.class).setText((getItem(position).get("MERK").asString()));
                findView(convertView, R.id.txtModel, TextView.class).setText((getItem(position).get("MODEL").asString()));
                return convertView;
            }
        });

        find(R.id.et_jenisKendaraan_jualPart, NikitaAutoComplete.class).setLoadingIndicator((android.widget.ProgressBar) findViewById(R.id.pb_jenisKendaraan));
        find(R.id.et_jenisKendaraan_jualPart, NikitaAutoComplete.class).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Nson n = Nson.readJson(String.valueOf(adapterView.getItemAtPosition(position)));

                StringBuilder stringBuilder = new StringBuilder();
                //stringBuilder.append(n.get("MODEL").asString()).append(" ");
                stringBuilder.append(n.get("MERK").asString()).append(" ");
                stringBuilder.append(n.get("JENIS").asString()).append(" ");
                stringBuilder.append(n.get("VARIAN").asString()).append(" ");

                find(R.id.et_jenisKendaraan_jualPart, NikitaAutoComplete.class).setText(stringBuilder.toString());
                find(R.id.et_jenisKendaraan_jualPart, NikitaAutoComplete.class).setTag(String.valueOf(adapterView.getItemAtPosition(position)));
            }
        });

        find(R.id.et_noPhone_jualPart, NikitaAutoComplete.class).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.toString().length() == 0) {
                    find(R.id.tl_nohp, TextInputLayout.class).setErrorEnabled(false);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int counting = (s == null) ? 0 : s.toString().length();
                if (counting == 0) {
                    find(R.id.tl_nohp, TextInputLayout.class).setErrorEnabled(false);
                } else if (counting < 4) {
                    find(R.id.et_noPhone_jualPart, NikitaAutoComplete.class).setText("+62 ");
                    Selection.setSelection(find(R.id.et_noPhone_jualPart, NikitaAutoComplete.class).getText(),
                            find(R.id.et_noPhone_jualPart, NikitaAutoComplete.class).getText().length());
                } else if (counting < 11) {
                    find(R.id.tl_nohp, TextInputLayout.class).setError("No. Hp Min. 6 Karakter");
                    find(R.id.et_noPhone_jualPart, NikitaAutoComplete.class).requestFocus();
                } else {
                    find(R.id.tl_nohp, TextInputLayout.class).setErrorEnabled(false);
                }
            }
        });

        find(R.id.et_namaPelanggan_jualPart, NikitaAutoComplete.class).setThreshold(0);
        find(R.id.et_namaPelanggan_jualPart, NikitaAutoComplete.class).setAdapter(new NsonAutoCompleteAdapter(getActivity()) {
            @Override
            public Nson onFindNson(Context context, String bookTitle) {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "Pelanggan");
                args.put("nama", bookTitle);
                Nson result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("datapelanggan"), args));
                return result.get("data");
            }

            @SuppressLint("SetTextI18n")
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.find_nophone, parent, false);
                }
                String nama = getItem(position).get("NAMA_PELANGGAN").asString();
                String nomor = "";
                noHp = getItem(position).get("NO_PONSEL").asString();
                if(noHp.length() > 4){
                    nomor += noHp.substring(noHp.length() - 4);
                }else{
                    nomor = noHp;
                }
                findView(convertView, R.id.txtPhone, TextView.class).setText(nama + " " + nomor);
                return convertView;
            }
        });

        find(R.id.et_namaPelanggan_jualPart, NikitaAutoComplete.class).setLoadingIndicator((android.widget.ProgressBar) findViewById(R.id.pb_namaPelanggan));
        find(R.id.et_namaPelanggan_jualPart, NikitaAutoComplete.class).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Nson n = Nson.readJson(String.valueOf(adapterView.getItemAtPosition(position)));
                isNoHp = true;
                find(R.id.et_noPhone_jualPart, NikitaAutoComplete.class).setText(n.get("NO_PONSEL").asString());
                find(R.id.et_namaPelanggan_jualPart, NikitaAutoComplete.class).setText(n.get("NAMA_PELANGGAN").asString());
                find(R.id.tl_nohp, TextInputLayout.class).setHelperTextEnabled(false);
            }
        });

        watcherNamaPelanggan(find(R.id.img_clear, ImageButton.class), find(R.id.et_namaPelanggan_jualPart, NikitaAutoComplete.class));
        find(R.id.img_clear, ImageButton.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                find(R.id.et_namaPelanggan_jualPart, NikitaAutoComplete.class).setText("");
                find(R.id.et_noPhone_jualPart, NikitaAutoComplete.class).setText("");
            }
        });
    }


    private void setIntent(){
        Intent intent = new Intent(getActivity(), CariPart_Activity.class);
        intent.putExtra("bengkel", "");
        startActivityForResult(intent, REQUEST_CARI_PART);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CARI_PART) {
            Nson nson = Nson.readJson(getIntentStringExtra(data, "part"));
            nson.set("JENIS_KENDARAAN", find(R.id.et_jenisKendaraan_jualPart, NikitaAutoComplete.class).getText().toString());
            nson.set("NAMA_PELANGGAN", find(R.id.et_namaPelanggan_jualPart, EditText.class).getText().toString());
            nson.set("NAMA_USAHA", find(R.id.et_namaUsaha_jualPart, EditText.class).getText().toString());
            nson.set("NO_PONSEL", isNoHp ? noHp : find(R.id.et_noPhone_jualPart, NikitaAutoComplete.class).getText().toString().replaceAll("[^0-9]+", ""));

            Intent i = new Intent(getActivity(), DetailJualPart_Activity.class);
            i.putExtra("part", nson.toJson());
            Log.d("datanihcuy", "data " + nson.toJson());
            startActivityForResult(i, REQEST_DAFTAR_JUAL);
        } else if (resultCode == RESULT_OK && requestCode == REQEST_DAFTAR_JUAL) {
            Nson nson = Nson.readJson(getIntentStringExtra(data, "part"));
            Log.d("datanihcuy", "containsName : " + Nson.readJson(getIntentStringExtra(data, "part")));
            Intent i = new Intent(getActivity(), DaftarJualPart_Activity.class);
            i.putExtra("data", nson.toJson());
            startActivityForResult(i, PenjualanPart_Activity.REQUEST_PENJUALAN);
        } else if (resultCode == RESULT_OK && requestCode == PenjualanPart_Activity.REQUEST_PENJUALAN) {
            setResult(RESULT_OK);
            finish();
        }
    }
}

