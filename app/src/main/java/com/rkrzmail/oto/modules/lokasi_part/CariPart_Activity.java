package com.rkrzmail.oto.modules.lokasi_part;

import android.content.Context;
import android.content.Intent;
import android.service.autofill.TextValueSanitizer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NikitaAutoComplete;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.srv.NsonAutoCompleteAdapter;

import java.util.ArrayList;
import java.util.Map;

public class CariPart_Activity extends AppActivity {

    private RecyclerView rvCariPart;
    private NikitaAutoComplete cariPart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cari_part_);
        initToolbar();
        initComponent();
    }

    private void initToolbar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_cariPart);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Cari Part");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //toolbar.animate().translationY(-toolbar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
    }


    private void initComponent(){
        rvCariPart = (RecyclerView) findViewById(R.id.recyclerView_cariPart);
        cariPart = (NikitaAutoComplete) findViewById(R.id.et_cariPart);


        cariPart.setThreshold(3);
        //final String nama = bookTitle.getText().toString();
        cariPart.setAdapter(new NsonAutoCompleteAdapter(getActivity()){
            Nson result;
            @Override
            public Nson onFindNson(Context context, String bookTitle) {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("search", bookTitle);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("caripart"),args)) ;

                return result;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.find_nama_part, parent, false);
                }
                ArrayList<String> data = new ArrayList<>();
                StringBuilder str = new StringBuilder();
                for(int i = 0; i < result.get("data").size(); i++){
                    data.add(result.get("data").get(i).get("NAMA").asString());
                }
                for(String nama : data){
                    str.append(nama);
                    str.append(", ");
                }
                //find(R.id.tv_cari_merkPart, TextView.class).setText((getItem(position).get("").asString()));
                findView(convertView, R.id.tv_find_cari_namaPart, TextView.class).setText((getItem(position).get(String.valueOf(str)).asString()));
//               find(R.id.tv_cari_noPart, TextView.class).setText((getItem(position).get("NO_PART_ID").asString()));
//               find(R.id.tv_cari_stockPart, TextView.class).setText((getItem(position).get("STOCK").asString()));


                return convertView;
            }
        });

        cariPart.setLoadingIndicator((android.widget.ProgressBar) findViewById(R.id.pb_tv_cariPart));
        cariPart.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Nson n = Nson.readJson(String.valueOf(adapterView.getItemAtPosition(position)));
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append( n.get("NAMA").asString()  ).append(" ");

                find(R.id.et_cariPart, NikitaAutoComplete.class).setText(stringBuilder.toString());
                find(R.id.et_cariPart, NikitaAutoComplete.class).setTag(String.valueOf(adapterView.getItemAtPosition(position)));

                find (R.id.tv_find_cari_namaPart, TextView.class).setText(n.get("NAMA").asString());

            }
        });


        rvCariPart.setLayoutManager(new LinearLayoutManager(this));
        rvCariPart.setHasFixedSize(true);

        rvCariPart.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_daftar_cari_part){
            @Override
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);

                if(nListArray.get("MERK_PART").asString() == null){
                    find(R.id.tv_cari_merkPart, TextView.class).setVisibility(View.GONE);
                }
                viewHolder.find(R.id.tv_cari_merkPart, TextView.class).setText(nListArray.get(position).get("").asString());
                viewHolder.find(R.id.tv_cari_namaPart, TextView.class).setText(nListArray.get(position).get("NAMA").asString());
                viewHolder.find(R.id.tv_cari_noPart, TextView.class).setText(nListArray.get(position).get("NO_PART_ID").asString());
                viewHolder.find(R.id.tv_cari_stockPart, TextView.class).setText(nListArray.get(position).get("STOCK").asString());
            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Nson parent, View view, int position) {

                        Intent i = new Intent(getActivity(), AturLokasiPart_Activity.class);
                        i.putExtra("MERK", nListArray.get(position).get("MERK").asString());
                        i.putExtra("NAMA", nListArray.get(position).get("NAMA").asString());
                        i.putExtra("NO_PART_ID", nListArray.get(position).get("NO_PART_ID"));
                        i.putExtra("NAMA", nListArray.get(position).toJson());

                        startActivity(i);
                    }
                })
        );

        cariPart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String data = cariPart.getText().toString();
                cariPart(data);
            }
        });
    }

    private void cariPart(final String cari){

        newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            Nson result2;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("search", cari);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("caripart"), args));
                result2 = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("viewlokasipart"), args));


            }

            @Override
            public void runUI() {
                if(result.get("status").asString().equalsIgnoreCase("OK")){
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    rvCariPart.getAdapter().notifyDataSetChanged();
                    Log.d("CARI PART", result.get("status").asString());

                }else{
                    showError("Gagal Mencari Part");
                }
                if(result2.get("status").asString().equalsIgnoreCase("OK")){
                    nListArray.asArray().clear();
                    nListArray.asArray().addAll(result.get("data").asArray());
                    rvCariPart.getAdapter().notifyDataSetChanged();
                    Log.d("CARI PART", result2.get("data").get("STOCK").asString());
                    Log.d("CARI PART", result2.get("status").asString());
                }
                else{
                    showError("Gagal Mencari Part");
                }

            }
        });
    }

   private void getView(){


   }
}
