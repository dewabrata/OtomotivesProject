package com.rkrzmail.oto.gmod;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import com.naa.data.Nson;
import com.naa.data.UtilityAndroid;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.NsonAutoCompleteAdapter;
import com.rkrzmail.srv.NikitaAutoComplete;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Pendaftaran1 extends AppActivity {


    private static final int TRIGGER_AUTO_COMPLETE = 100;
    private static final long AUTO_COMPLETE_DELAY = 300;
    private Handler handler;
    private AutoSuggestAdapter autoSuggestAdapter;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pendaftaran1);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

         getSupportActionBar().setTitle("PENDAFTARAN");
         getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        find(R.id.txtNopol).setOnClickListener(new View.OnClickListener() {
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



        find(R.id.tblLanjut).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nopol = find(R.id.txtNopol, EditText.class).getText().toString().replace(" ","").toUpperCase();
                if (nopol.equalsIgnoreCase("")){
                    showError("Nopol Tidak Boleh Kosong");return;
                }else if (find(R.id.txtNamaPelanngan, EditText.class).getText().toString().equalsIgnoreCase("")){
                    showError("NamaPelanngan Tidak Boleh Kosong");return;
                }else if (find(R.id.txtPhone, EditText.class).getText().toString().equalsIgnoreCase("")){
                    showError("No Telp. Tidak Boleh Kosong");return;
                }
                lanjut(nopol);
            }
        });

        find(R.id.tblHistory).setEnabled(false);
        find(R.id.tblHistory).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nopol = find(R.id.txtNopol, EditText.class).getText().toString().replace(" ","").toUpperCase();
                Intent intent = new Intent(getActivity(), HistoryActivity.class);
                intent.putExtra("NOPOL", nopol);
                startActivity(intent);
            }
        });

        find(R.id.imgBarcode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), BarcodeActivity.class);
                startActivityForResult(intent, REQUEST_BARCODE);
            }
        });


        NikitaAutoComplete bookTitle = (NikitaAutoComplete) findViewById(R.id.txtJenisKen);
        bookTitle.setThreshold(3);
        bookTitle.setAdapter(new NsonAutoCompleteAdapter(this){
            @Override
            public Nson onFindNson(Context context, String bookTitle) {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("nama", bookTitle);
                Nson result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrl("mstkendaraan.php"),args)) ;
                return result;
            }
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.find_jenisken, parent, false);
                }

                findView(convertView, R.id.txtMerk, TextView.class).setText( (getItem(position).get("MERK").asString()) );
                findView(convertView, R.id.txtModel, TextView.class).setText( (getItem(position).get("MODEL").asString()) );
                findView(convertView, R.id.txtJenisVarian, TextView.class).setText( (getItem(position).get("JENIS").asString()) + " " + (getItem(position).get("VARIAN").asString())  );

                return convertView;
            }
           /* public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View v =  super.getView(position, convertView, parent);
                if (v instanceof TextView){

                }
                return v;
            }*/
        }); // 'this' is Activity instance
        bookTitle.setLoadingIndicator(  (android.widget.ProgressBar) findViewById(R.id.pb_txtJenisKen));
        bookTitle.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Nson n = Nson.readJson(String.valueOf(adapterView.getItemAtPosition(position)));
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append( n.get("MERK").asString()  ).append(" ");
                stringBuilder.append( n.get("MODEL").asString()  ).append(" ");
                stringBuilder.append( n.get("JENIS").asString()  ).append(" ");
                stringBuilder.append( n.get("VARIAN").asString()  ).append(" ");

                find(R.id.txtJenisKen, NikitaAutoComplete.class).setText(  stringBuilder.toString()  );
                find(R.id.txtJenisKen, NikitaAutoComplete.class).setTag(String.valueOf(adapterView.getItemAtPosition(position)));

                find (R.id.txtJenisVarian, TextView.class).setText(n.get("JENIS") + n.get("VARIAN").asString());
                find (R.id.txtModel, TextView.class).setText(n.get("MODEL").asString());
                find (R.id.txtMerk, TextView.class).setText(n.get("MERK").asString());


            }
        });


        bookTitle = (NikitaAutoComplete) findViewById(R.id.txtNopol);
        bookTitle.setThreshold(1);
        bookTitle.setAdapter(new NsonAutoCompleteAdapter(this){
            @Override
            public Nson onFindNson(Context context, String bookTitle) {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                String nopol = bookTitle.replace(" ","").toUpperCase();
                args.put("nopol", nopol);
                Nson result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrl("nopol.php"),args)) ;

                return result;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.find_nopol, parent, false);
                }
                findView(convertView, R.id.txtNopol, TextView.class).setText( formatNopol(getItem(position).get("NOPOL").asString()) );

                return convertView;
            }


        }); // 'this' is Activity instance
        bookTitle.setLoadingIndicator(  (android.widget.ProgressBar) findViewById(R.id.pb_txtNopol));
        bookTitle.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                /*Book book = (Book) adapterView.getItemAtPosition(position);
                bookTitle.setText(book.getTitle())*/;
                Nson n = Nson.readJson(String.valueOf(adapterView.getItemAtPosition(position)));
                find(R.id.txtNopol, NikitaAutoComplete.class).setText(  formatNopol(n.get("NOPOL").asString())  );
                checkHistory( n.get("NOPOL").asString());
            }
        });

        bookTitle = (NikitaAutoComplete) findViewById(R.id.txtPhone);
        bookTitle.setThreshold(3);
        bookTitle.setAdapter(new NsonAutoCompleteAdapter(this){
            @Override
            public Nson onFindNson(Context context, String bookTitle) {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                String phone = bookTitle.replace(" ","").toUpperCase();
                args.put("phone", phone);
                Nson result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrl("checkin.php"),args)) ;

                return result;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.find_nophone, parent, false);
                }
                findView(convertView, R.id.txtPhone, TextView.class).setText( formatNopol(getItem(position).get("PHONE").asString()) );

                return convertView;
            }


        }); // 'this' is Activity instance
        bookTitle.setLoadingIndicator(  (android.widget.ProgressBar) findViewById(R.id.pb_txtPhone));
        bookTitle.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                /*Book book = (Book) adapterView.getItemAtPosition(position);
                bookTitle.setText(book.getTitle())*/;
                Nson n = Nson.readJson(String.valueOf(adapterView.getItemAtPosition(position)));
                find(R.id.txtPhone, NikitaAutoComplete.class).setText(  formatNopol(n.get("PHONE").asString())  );
                checkHistory( n.get("PHONE").asString());
            }
        });

        // setLayanan();
    }

    private void checkHistory(final String nopol){
        newProses( new Messagebox.DoubleRunnable() {
            Nson result;
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                String nopol = find(R.id.txtNopol,EditText.class).getText().toString();
                nopol = nopol.toUpperCase().trim().replace(" ", "");
                args.put("nopol", nopol);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrl("checkhistory.php"),args)) ;
            }
            public void runUI() {
                if (result.get("HISTORY").asString().equalsIgnoreCase("TRUE")){
                    find(R.id.tblHistory).setEnabled(true);
                }else{
                    find(R.id.tblHistory).setEnabled(false);
                }
                if (result.get("STATUS").asString().equalsIgnoreCase("OK")){
                    if (result.containsKey("PHONE")){
                        find(R.id.txtPhone, EditText.class).setText(result.get("PHONE").asString());
                    }
                    if (result.containsKey("NAMA")){
                        find(R.id.txtNamaPelanngan, EditText.class).setText(result.get("NAMA").asString());
                    }
                    if (result.containsKey("PEMILIK")&& result.get("PEMILIK").asBoolean()){
                        find(R.id.cckPemilik, CheckBox.class).setChecked(true);
                    }
                    if (result.containsKey("KM")){
                        find(R.id.txtKMSebelum, EditText.class).setText(result.get("KM").asString());
                    }
                }
            }
        });
    }

    private void lanjut(final String nopol){
        newProses( new Messagebox.DoubleRunnable() {
            Nson result;
            Map<String, String> args = AppApplication.getInstance().getArgsData();
            public void run() {
                String nopol = find(R.id.txtNopol,EditText.class).getText().toString();
                nopol = nopol.toUpperCase().trim().replace(" ", "");
                args.put("nopol", nopol);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrl("pendaftaran1.php"),args)) ;
            }
            public void runUI() {
                Nson nson = Nson.newObject();
                nson.set("nopol", nopol);
                nson.set("nama", find(R.id.txtNamaPelanngan, EditText.class).getText().toString());
                nson.set("phone", find(R.id.txtPhone, EditText.class).getText().toString());
                nson.set("km", find(R.id.txtKM, EditText.class).getText().toString());

                nson.set("kendaraan", find(R.id.txtJenisKen, EditText.class).getText().toString());
                Nson n = Nson.readJson(String.valueOf(  find(R.id.txtJenisKen, EditText.class).getTag()  ));
                nson.set("merk",  n.get("MERK").asString());
                nson.set("model", n.get("MODEL").asString());
                nson.set("jenis",  n.get("JENIS").asString());
                nson.set("varian", n.get("VARIAN").asString());

                if (result.get("new").asString().equalsIgnoreCase("true")) {
                    Intent intent = new Intent(getActivity(), Pendaftaran2.class);
                    intent.putExtra("dx", nson.toJson());
                    startActivityForResult(intent, REQUEST_PENDAFTARAN);
                } else if (result.get("disc").asString().equalsIgnoreCase("true")) {
                    Intent intent = new Intent(getActivity(), Pendaftaran2.class);
                    intent.putExtra("dx", nson.toJson());
                    startActivityForResult(intent, REQUEST_PENDAFTARAN);
                } else {
                  Intent intent = new Intent(getActivity(), Pendaftaran3.class);
                  intent.putExtra("dx", nson.toJson());
                  startActivityForResult(intent, REQUEST_PENDAFTARAN);
                }
            }
        });
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
        if (requestCode == REQUEST_PENDAFTARAN && resultCode == RESULT_OK){
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

    final int REQUEST_BARCODE = 13;
    final int REQUEST_PENDAFTARAN = 123;
    final int REQUEST_PART  = 15;

    public static class AutoSuggestAdapter extends ArrayAdapter<String> implements Filterable {
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
