package com.rkrzmail.oto.gmod;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.part.PartActivity;
import com.rkrzmail.oto.modules.part.People;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.utils.DataGenerator;

import java.util.List;
import java.util.Map;

public class Lokasi_PersediaanActivity extends AppActivity {
    final int REQUEST_PART3 = 125;
    private RecyclerView recyclerView;
    private View parent_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lokasi__persediaan);
        parent_view = findViewById(android.R.id.content);

        initComponent();
        initToolbar();

        find(R.id.tblSimpan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PartActivity.class);
                intent.putExtra("DATA3", getIntentStringExtra("DATA3"));
                startActivityForResult(intent, REQUEST_PART3);
            }
        });

        find(R.id.tblSimpan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simpan();

            }
        });

        Nson nson = Nson.newArray().add("GUDANG PART").add("RUANG PART").add("RECEPTION").add("LAINNYA").add("DISPLAY");
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
        Spinner prov = findViewById(R.id.spnLokasi);
        prov.setAdapter(adapter);



        Nson nson1 = Nson.newArray().add("PALET").add("RAK").add("FOLDER");
        ArrayAdapter<List> adapter1 = new ArrayAdapter<List>(getActivity(), android.R.layout.simple_spinner_dropdown_item, nson1.asArray()) {
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
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner prov1 = findViewById(R.id.spnPenempatan);
        prov1.setAdapter(adapter1);

        Nson nson2 = Nson.newArray().add("1").add("2").add("3").add("4").add("5").add("6").add("7").add("8").add("9").add("10")
                .add("11").add("12").add("13").add("14").add("15").add("16").add("17").add("18").add("19").add("20")
                .add("21").add("22").add("23").add("24").add("25").add("26").add("27").add("28").add("29").add("30")
                .add("31").add("32").add("33").add("34").add("35").add("36").add("37").add("38").add("39").add("40")
                .add("41").add("42").add("43").add("44").add("45").add("46").add("47").add("48").add("49").add("50")
                .add("51").add("52").add("53").add("54").add("55").add("56").add("57").add("58").add("59").add("60")
                .add("61").add("62").add("63").add("64").add("65").add("66").add("67").add("68").add("69").add("70")
                .add("71").add("72").add("73").add("74").add("75").add("76").add("77").add("78").add("79").add("80")
                .add("81").add("82").add("83").add("84").add("85").add("86").add("87").add("88").add("89").add("90")
                .add("91").add("92").add("93").add("94").add("95").add("96").add("97").add("98").add("99").add("100");
        ArrayAdapter<List> adapter2 = new ArrayAdapter<List>(getActivity(), android.R.layout.simple_spinner_dropdown_item, nson2.asArray()) {
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
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner prov2 = findViewById(R.id.spnRakPalet);
        prov2.setAdapter(adapter2);

        Nson nson4 = Nson.newArray().add("1").add("2").add("3").add("5").add("5").add("6");
        ArrayAdapter<List> adapter4 = new ArrayAdapter<List>(getActivity(), android.R.layout.simple_spinner_dropdown_item, nson4.asArray()) {
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
        adapter4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner prov4 = findViewById(R.id.spnRak);
        prov4.setAdapter(adapter4);


        Nson nson3 = Nson.newArray().add("1").add("2").add("3").add("4").add("5").add("6").add("7").add("8").add("9").add("10")
                .add("11").add("12").add("13").add("14").add("15").add("16").add("17").add("18").add("19").add("20")
                .add("21").add("22").add("23").add("24").add("25").add("26").add("27").add("28").add("29").add("30")
                .add("31").add("32").add("33").add("34").add("35").add("36").add("37").add("38").add("39").add("40")
                .add("41").add("42").add("43").add("44").add("45").add("46").add("47").add("48").add("49").add("50")
                .add("51").add("52").add("53").add("54").add("55").add("56").add("57").add("58").add("59").add("60");
        ArrayAdapter<List> adapter3 = new ArrayAdapter<List>(getActivity(), android.R.layout.simple_spinner_dropdown_item, nson3.asArray()) {
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
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner prov3 = findViewById(R.id.spnNoFolder);
        prov3.setAdapter(adapter3);



        Nson result = Nson.readNson(getIntentStringExtra("DATA"));

        if (result.isNson()) {
            if (result.size() >= 1) {


                find(R.id.txtOngkosKirim, TextView.class).setText(result.get("NAMA").asString());
                find(R.id.txtNoPart, TextView.class).setText(result.get("NO_PART").asString());
                return;
            }
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rViewLokasi);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);


        recyclerView.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_part_detail_lokasi) {
            public void onBindViewHolder( NikitaViewHolder viewHolder, int position) {
                viewHolder.find(R.id.txtOngkosKirim, TextView.class).setText(nListArray.get(position).get("NAMA").asString());
                viewHolder.find(R.id.txtNoPart, TextView.class).setText(nListArray.get(position).get("NO_PART").asString());
                viewHolder.find(R.id.txtStock, TextView.class).setText(nListArray.get(position).get("STOCK").asString());


            }
        });

    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //toolbar.setNavigationIcon(R.drawable.ic_menu);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("PART");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void initComponent() {


        List<People> items = DataGenerator.getPeopleData(this);
        items.addAll(DataGenerator.getPeopleData(this));
        items.addAll(DataGenerator.getPeopleData(this));
//
   /* //set data and list adapter
        mAdapter = new AdapterListBasic(this, items);
        recyclerView.setAdapter(mAdapter);

        // on item list clicked
        mAdapter.setOnItemClickListener(new AdapterListBasic.OnItemClickListener() {
            @Override
            public void onItemClick(View view, People obj, int position) {
                Snackbar.make(parent_view, "Item " + obj.name + " clicked", Snackbar.LENGTH_SHORT).show();
            }
        });*/


        // reload("");
    }
    private void simpan() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

//getSelectedSpinnerText(R.id.spnLokasi, Spinner.class);
                String lokasi = getSelectedSpinnerText(R.id.spnLokasi);
                lokasi = lokasi.toUpperCase().trim().replace(" ", "");

                String penempatan = getSelectedSpinnerText(R.id.spnPenempatan);
                penempatan = penempatan.toUpperCase().trim().replace(" ", "");

                String palet = getSelectedSpinnerText(R.id.spnRakPalet);
                palet = palet.toUpperCase().trim().replace(" ", "");

                String rak = getSelectedSpinnerText(R.id.spnRak);
                rak = rak.toUpperCase().trim().replace(" ", "");

                String no_folder = getSelectedSpinnerText(R.id.spnNoFolder);
                no_folder = no_folder.toUpperCase().trim().replace(" ", "");

                args.put("lokasi", lokasi);
                args.put("penempatan", penempatan);
                args.put("palet", palet);
                args.put("rak", rak);
                args.put("no_folder", no_folder);
                args.put("parts", nListArray.toJson());
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrl("saveitemlokasi.php"), args));
            }

            public void runUI() {


            }
        });
    }


    @Override
        protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == REQUEST_PART3 && resultCode == RESULT_OK) {
                nListArray.add(Nson.readJson(getIntentStringExtra(data, "DATA3")));
                if (nListArray.get(nListArray.size()-1).containsKey("STOK")){
                }else{
                    nListArray.get(nListArray.size()-1).set("STOK",0);
                }
                find(R.id.rViewLokasi, RecyclerView.class).getAdapter().notifyDataSetChanged();
            }
    }
}


