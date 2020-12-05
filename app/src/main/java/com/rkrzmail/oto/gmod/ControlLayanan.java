package com.rkrzmail.oto.gmod;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.data.Utility;
import com.naa.data.UtilityAndroid;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;

import java.util.List;
import java.util.Map;

public class ControlLayanan extends AppActivity {
    private RecyclerView recyclerView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_layanan);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setTitle("KONTROL PELAYANAN");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Nson data = Nson.readNson(getIntentStringExtra("DATA"));

        find(R.id.txtStatus, TextView.class).setText(data.get("STATUS").asString());
        find(R.id.tv_text_suggesttion, TextView.class).setText(data.get("NOPOL").asString());
//        find(R.id.txtMerkModel, TextView.class).setText(data.get("MODEL").asString());



        find(R.id.txttotalbayar, EditText.class).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int l = Utility.getInt(find(R.id.txtTotalBiaya, EditText.class).getText().toString());
                int i = Utility.getInt(find(R.id.txttotalbayar, EditText.class).getText().toString());
                String sisa = Utility.formatCurrencyBulat(l - i);
                find(R.id.txttotalKembalian, EditText.class).setText(sisa);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        find(R.id.tblSimpan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Messagebox.showDialog(getActivity(), "Konfirmasi", "Apakah yakin akan simpan ?", "Ya", "Tidak", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        save();
                    }
                }, null);
            }
        });


        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("custid", UtilityAndroid.getSetting(getApplicationContext(), "CID", ""));
                args.put("email", UtilityAndroid.getSetting(getApplicationContext(), "EMA", ""));
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrl("control.php"), args));

            }

            @Override
            public void runUI() {

            }
        });

        Nson nson = Nson.newArray().add("DATA").add("CALL").add("MESSAGE").add("ASSIGN TO MEKANIK");
        ArrayAdapter<List> adapter = new ArrayAdapter<List>(getActivity(), android.R.layout.simple_spinner_dropdown_item, nson.asArray()) {
            public View getView(int position, @Nullable View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                /*if (v instanceof TextView){
                    ((TextView) v).setText( ));
                }*/
                return v;
            }

            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                /*if (v instanceof TextView){
                    ((TextView) v).setText(result.get(position).get("KELURAHAN").asString());
                }*/
                return v;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner prov = findViewById(R.id.spnAktifitas);
        prov.setAdapter(adapter);


        nListArray.add("");//hedaer

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rViewPart);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        recyclerView.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_part_persetujuan) {
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                if (position >= 1) {
                    viewHolder.find(R.id.txtName, TextView.class).setText(nListArray.get(position).get("NAMA").asString());
                    viewHolder.find(R.id.txtBiaya, TextView.class).setText(nListArray.get(position).get("").asString());
                    viewHolder.find(R.id.txtDisc, TextView.class).setText(nListArray.get(position).get("").asString());

                }


            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Nson parent, View view, int position) {
                //Toast.makeText(getActivity(),"HHHHH "+position, Toast.LENGTH_SHORT).show();
                /*Intent intent =  new Intent(getActivity(), ControlLayanan.class);
                intent.putExtra("ID", nListArray.get(position).get("MEKANIK").asInteger());
                intent.putExtra("DATA", nListArray.get(position).toJson());
                startActivityForResult(intent, REQUEST_CONTROL);*/
            }
        }));


        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("nopol", "helo");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrl("checkin.php"), args));

            }

            public void runUI() {


                find(R.id.txtNoKunci, TextView.class).setText(result.get(0).get("NO_KUNCI").asString());
                find(R.id.txtNamaPelanggan, TextView.class).setText(result.get(0).get("NAMA").asString());
                find(R.id.txtLayanan, TextView.class).setText(result.get(0).get("LAYANAN").asString());
                find(R.id.txtTotalBiaya, EditText.class).setText(result.get(0).get("TOTAL_BIAYA").asString());
                find(R.id.txtDP, EditText.class).setText(result.get(0).get("DP").asString());
                find(R.id.txtSisa, EditText.class).setText(result.get(0).get("SISA").asString());
                find(R.id.txtEstimasiSebelum, EditText.class).setText(result.get(0).get("WAKTU_LAYANAN_SEBELUM").asString());
                find(R.id.txtEstimasiSesudah, EditText.class).setText(result.get(0).get("WAKTU_LAYANAN_TIME").asString());
                find(R.id.txtEstimasiSelesai, EditText.class).setText(result.get(0).get("WAKTU_LAYANAN_SELESAI").asString());


                if (result.get(0).get("KENDARAAN_DITINGGAL").asString().equalsIgnoreCase("YES")) {
                    find(R.id.cckKendaraanDitinggal, CheckBox.class).setChecked(true);
                }

                if (result.get(0).get("TUNGGU_KONFIRMASI").asString().equalsIgnoreCase("YES")) {
                    find(R.id.cckTungguKonfirmasi, CheckBox.class).setChecked(true);
                }

                find(R.id.txtSelesaiJam, EditText.class).setText(result.get(0).get("JAM_PENGAMBILAN").asString());

                if (result.get(0).get("KONFIRMASI_TAMBAHAN").asString().equalsIgnoreCase("YES")) {
                    find(R.id.cckKonfirmasiTambahan, CheckBox.class).setChecked(true);
                }

                if (result.get(0).get("TINGGALKAN_STNK").asString().equalsIgnoreCase("YES")) {
                    find(R.id.cckTinggalkanSTNK, CheckBox.class).setChecked(true);
                }

                if (result.get(0).get("BUANG_PART").asString().equalsIgnoreCase("YES")) {
                    find(R.id.cckBuangPartBekas, CheckBox.class).setChecked(true);
                }

                find(R.id.txtMekanik, EditText.class).setText(result.get(0).get("MEKANIK").asString());


            }
        });
    }

    public void save(){
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result ;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("custid", UtilityAndroid.getSetting(getApplicationContext(),"CID",""));
                args.put("email", UtilityAndroid.getSetting(getApplicationContext(),"EMA",""));
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrl("controlsimpan.php"),args)) ;

            }

            @Override
            public void runUI() {

                showError("Terajadi Kesalahan diiserver");

              /*  setResult(RESULT_OK);

                finish();*/
            }
        });
    }
}
