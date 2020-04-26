package com.rkrzmail.oto.gmod;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Spinner;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.data.UtilityAndroid;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.part.PartActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PendaftaranLayananActivity extends AppActivity {


    private static final int TRIGGER_AUTO_COMPLETE = 100;
    private static final long AUTO_COMPLETE_DELAY = 300;
    private Handler handler;
    private AutoSuggestAdapter autoSuggestAdapter;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftarlayanan);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


         getSupportActionBar().setTitle("PENDAFTARAN");
         getSupportActionBar().setDisplayHomeAsUpEnabled(true);

         find(R.id.lnrTanggalRangka).setVisibility(View.GONE);

         find(R.id.tblCari).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {

                 Intent intent =  new Intent(getActivity(), KendaraanCari.class);
                 intent.putExtra("cari",find(R.id.txtCari, EditText.class).getText().toString());
                 startActivity(intent);
             }
         });

        find(R.id.tblNopol).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
                    Nson result;
                    public void run() {
                        Map<String, String> args = AppApplication.getInstance().getArgsData();
                        String nopol = find(R.id.txtNopol,EditText.class).getText().toString();
                        nopol = nopol.toUpperCase().trim().replace(" ", "");
                        args.put("nopol", nopol);
                        result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV2("carinopol"),args)) ;
                    }

                    @Override
                    public void runUI() {
                        find(R.id.tblHistory).setEnabled(false);
                        if (result.isNson()){
                            if (result.size()>=1){
                                //PHONE
                                find(R.id.txtPhone,EditText.class).setText(result.get(0).get("PHONE").asString());
                                find(R.id.txtNamaPelanngan,EditText.class).setText(result.get(0).get("NAMA").asString());

                                find(R.id.tblHistory).setEnabled(true);
                                return;
                            }
                        }
                        //error
                        showError("Tidak Ditemukan");
                    }
                });
            }
        });

        final AppCompatAutoCompleteTextView autoCompleteTextView = find(R.id.txtNopol);

        //Setting up the adapter for AutoSuggest
        autoSuggestAdapter = new AutoSuggestAdapter(this,
                android.R.layout.simple_dropdown_item_1line);
        autoCompleteTextView.setThreshold(2);
        autoCompleteTextView.setAdapter(autoSuggestAdapter);
        autoCompleteTextView.setOnItemClickListener(
                new AdapterView.OnItemClickListener()  {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        //selectedText.setText(autoSuggestAdapter.getObject(position));
                    }
                });
        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int
                    count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                handler.removeMessages(TRIGGER_AUTO_COMPLETE);
                handler.sendEmptyMessageDelayed(TRIGGER_AUTO_COMPLETE,
                        AUTO_COMPLETE_DELAY);
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == TRIGGER_AUTO_COMPLETE) {
                    if (!TextUtils.isEmpty(autoCompleteTextView.getText())) {
                        makeApiCall(autoCompleteTextView.getText().toString());
                    }
                }
                return false;
            }
        });


        find(R.id.tblSelanjutnya).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent =  new Intent(getActivity(), PersetujuanLayanan.class);
                intent.putExtra("nopol",find(R.id.txtNopol,EditText.class).getText().toString());
                intent.putExtra("nama", find(R.id.txtNamaPelanngan,EditText.class).getText().toString());
                intent.putExtra("layanan", getSelectedSpinnerText(R.id.spnLayanan));
                intent.putExtra("data", nListArray.toJson());

                startActivityForResult(intent, REQUEST_PERSETUAN);
            }
        });

        find(R.id.tblHistory).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), HistoryActivity.class);
                intent.putExtra("NOPOL", find(R.id.txtNopol,EditText.class).getText().toString());
                startActivityForResult(intent, REQUEST_BARCODE);
            }
        });

        find(R.id.imgBarcode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), BarcodeActivity.class);
                startActivityForResult(intent, REQUEST_BARCODE);
            }
        });


        find(R.id.tblSparePart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent =  new Intent(getActivity(), PartActivity.class);
                intent.putExtra("cari",find(R.id.txtCari, EditText.class).getText().toString());
                startActivityForResult(intent,REQUEST_PART);
            }
        });

        setLayanan();
    }
    private void makeApiCall(String text) {
        MessageMsg.newTask(getActivity(), new Messagebox.DoubleRunnable() {
        Nson result;
        public void run() {
            Map<String, String> args = AppApplication.getInstance().getArgsData();
            String nopol = find(R.id.txtNopol,EditText.class).getText().toString();
            nopol = nopol.toUpperCase().trim().replace(" ", "");
            args.put("nopol", nopol);
            result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV2("carinopol"),args)) ;
        }

        @Override
        public void runUI() {
            find(R.id.tblHistory).setEnabled(false);
            if (result.isNson()){
                if (result.size()>=1){
                    //PHONE

                    List<String> stringList = new ArrayList<>();
                    for (int i = 0; i < result.size(); i++) {

                        stringList.add(result.get(0).get("NOPOL").asString());
                    }

                    autoSuggestAdapter.setData(stringList);
                    autoSuggestAdapter.notifyDataSetChanged();

                    find(R.id.txtPhone,EditText.class).setText(result.get(0).get("PHONE").asString());
                    find(R.id.txtNamaPelanngan,EditText.class).setText(result.get(0).get("NAMA").asString());

                    find(R.id.tblHistory).setEnabled(true);
                    return;
                }
            }
            //error
            //showError("Tidak Ditemukan");
        }
    });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PERSETUAN && resultCode == RESULT_OK){
            setResult(RESULT_OK);
            finish();
        }else  if (requestCode == REQUEST_BARCODE && resultCode == RESULT_OK){
            String bacode = getIntentStringExtra(data, "TEXT");
            barcode();
        } else  if (requestCode == REQUEST_PART && resultCode == RESULT_OK){
            nListArray.add(Nson.readJson(getIntentStringExtra(data, "DATA")));
        }
    }
    public void barcode(){
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result ;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("custid", UtilityAndroid.getSetting(getApplicationContext(),"CID",""));
                args.put("email", UtilityAndroid.getSetting(getApplicationContext(),"EMA",""));
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrl("barcode.php"),args)) ;

            }

            @Override
            public void runUI() {


                setResult(RESULT_OK);

                finish();
            }
        });
    }
    public void setLayanan(){
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result ;
            Nson resultMekanik ;
            Nson resultPekerjaan ;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                String nopol = find(R.id.txtNopol,EditText.class).getText().toString();
                nopol = nopol.toUpperCase().trim().replace(" ", "");
                args.put("nopol", nopol);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV2("layanan"),args)) ;

                resultMekanik = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV2("mekanik"),args)) ;

                resultPekerjaan = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV2("pekerjaan"),args)) ;
            }

            public void runUI() {


                ArrayAdapter<List> adapter = new ArrayAdapter<List>(getActivity(), android.R.layout.simple_spinner_dropdown_item, result.asArray()){
                    public View getView(int position, @Nullable View convertView,   ViewGroup parent) {
                        View v =  super.getView(position, convertView, parent);
                        /*if (v instanceof TextView){
                            ((TextView) v).setText(result.get(position).get("KODE_POS").asString());
                        }*/
                        return v;
                    }
                    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                        View v =  super.getView(position, convertView, parent);
                        /*if (v instanceof TextView){
                            ((TextView) v).setText(result.get(position).get("KODE_POS").asString());
                        }*/
                        return v;
                    }
                };
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                Spinner prov =  findViewById(R.id.spnLayanan);
                prov.setAdapter(adapter);

                adapter = new ArrayAdapter<List>(getActivity(), android.R.layout.simple_spinner_dropdown_item, resultMekanik.asArray()){
                    public View getView(int position, @Nullable View convertView,   ViewGroup parent) {
                        View v =  super.getView(position, convertView, parent);
                        /*if (v instanceof TextView){
                            ((TextView) v).setText(result.get(position).get("KODE_POS").asString());
                        }*/
                        return v;
                    }
                    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                        View v =  super.getView(position, convertView, parent);
                        /*if (v instanceof TextView){
                            ((TextView) v).setText(result.get(position).get("KODE_POS").asString());
                        }*/
                        return v;
                    }
                };
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                prov =  findViewById(R.id.spnMekanik);
                prov.setAdapter(adapter);


                if (resultPekerjaan.asArray().size()>=1){
                    resultPekerjaan.asArray().add(0, Nson.newObject().set("PEKERJAAN","").asObject());
                }
                adapter = new ArrayAdapter<List>(getActivity(), android.R.layout.simple_spinner_dropdown_item, resultPekerjaan.asArray()){
                    public View getView(int position, @Nullable View convertView,   ViewGroup parent) {
                        View v =  super.getView(position, convertView, parent);
                        if (v instanceof TextView){
                            ((TextView) v).setText(resultPekerjaan.get(position).get("PEKERJAAN").asString());
                        }
                        return v;
                    }
                    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                        View v =  super.getView(position, convertView, parent);
                        if (v instanceof TextView){
                            ((TextView) v).setText(resultPekerjaan.get(position).get("PEKERJAAN").asString());
                        }
                        return v;
                    }
                };
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                prov =  findViewById(R.id.spnPekerjaan);
                prov.setAdapter(adapter);

            }
        });


    }

    final int REQUEST_BARCODE = 13;
    final int REQUEST_PERSETUAN  = 123;
    final int REQUEST_PART  = 15;




    public class AutoSuggestAdapter extends ArrayAdapter<String> implements Filterable {
        private List<String> mlistData;
        public AutoSuggestAdapter(@NonNull Context context, int resource) {
            super(context, resource);
            mlistData = new ArrayList<>();
        }
        public void setData(List<String> list) {
            mlistData.clear();
            mlistData.addAll(list);
        }
        @Override
        public int getCount() {
            return mlistData.size();
        }
        @Nullable
        @Override
        public String getItem(int position) {
            return mlistData.get(position);
        }
        /**
         * Used to Return the full object directly from adapter.
         *
         * @param position
         * @return
         */
        public String getObject(int position) {
            return mlistData.get(position);
        }
        @NonNull
        @Override
        public Filter getFilter() {
            Filter dataFilter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        filterResults.values = mlistData;
                        filterResults.count = mlistData.size();
                    }
                    return filterResults;
                }
                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && (results.count > 0)) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };
            return dataFilter;
        }
    }
}
