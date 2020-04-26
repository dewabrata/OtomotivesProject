package com.rkrzmail.oto.gmod;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.MenuActivity;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.part.PartActivity;
import com.rkrzmail.srv.NikitaAutoComplete;
import com.rkrzmail.srv.NikitaRecyclerAdapter;
import com.rkrzmail.srv.NikitaViewHolder;
import com.rkrzmail.srv.NsonAutoCompleteAdapter;

import java.util.List;
import java.util.Map;

public class Pendaftaran3 extends AppActivity {
    final int REQUEST_PENDAFTARAN = 123;
    final int REQUEST_PART  = 15;
    final int REQUEST_JASA  = 16;
    final int REQUEST_JASA_BERKALA  = 17;
    final int REQUEST_PART_BERKALA  = 19;
    final int REQUEST_PART_EXTERNAL  = 20;
    final int REQUEST_CODE_SIGN = 66;

    private Nson layanan = Nson.newArray();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pendaftaran3);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setTitle("PENDAFTARAN");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        find(R.id.tblTtg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Capture.class);
                intent.putExtra("STATUS", "");

                startActivityForResult(intent, REQUEST_CODE_SIGN);
            }
        });

        find(R.id.tblBatal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        find(R.id.tblSimpan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Messagebox.showDialog(getActivity(), "Konfirmasi", "Apakah yakin akan simpan ?", "Ya", "Tidak", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        save();
                        Intent i = new Intent(Pendaftaran3.this, MenuActivity.class);
                        startActivity(i);
                        finish();
                    }
                }, null);


//                Intent intent =  new Intent(getActivity(), PersetujuanLayanan.class);
//                Nson nson = Nson.readNson(getIntentStringExtra("dx"));
//
//                nson.set("layanan", find(R.id.txtLayanan, EditText.class).getText().toString());
//                nson.set("mekanik", find(R.id.txtMekanik, EditText.class).getText().toString());
//                intent.putExtra("dx", nson.toJson());
//
//                intent.putExtra("data", nListArray.toJson());
//                startActivityForResult(intent, REQUEST_PENDAFTARAN);
            }
        });

        find(R.id.tblSparepart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent =  new Intent(getActivity(), PartActivity.class);
                intent.putExtra("x","x");

                startActivityForResult(intent,REQUEST_PART);

            }
        });

        find(R.id.tblJasaLain).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent =  new Intent(getActivity(), JasaLainActivity.class);

                startActivityForResult(intent,REQUEST_JASA);
            }
        });

        find(R.id.tblJasaLainBerkala).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent =  new Intent(getActivity(), JasaLainBerkalaActivity.class);

                startActivityForResult(intent,REQUEST_JASA_BERKALA);
            }
        });

        find(R.id.tblSparepartBerkala).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent =  new Intent(getActivity(), PartBerkalaActivity.class);

                startActivityForResult(intent,REQUEST_PART_BERKALA);
            }
        });

        find(R.id.tblPerbaikanRingan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent =  new Intent(getActivity(), PartActivity.class);
                intent.putExtra("z","z");

                startActivityForResult(intent,REQUEST_PART_EXTERNAL);
            }
        });
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rViewPart);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        recyclerView.setAdapter(new NikitaRecyclerAdapter(nListArray,R.layout.item_part_persetujuan){
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                    viewHolder.find(R.id.txtName, TextView.class).setText(nListArray.get(position).get("NAMA").asString());
                    viewHolder.find(R.id.txtBiaya, TextView.class).setText("Rp " + nListArray.get(position).get("HARGA").asString());
                    viewHolder.find(R.id.txtDisc, TextView.class).setText("Disc : " + " -0");
            }
        }.setOnitemClickListener(new NikitaRecyclerAdapter.OnItemClickListener() {
            public void onItemClick(Nson parent, View view, int position) {
                //Toast.makeText(getActivity(),"HHHHH "+position, Toast.LENGTH_SHORT).show();
                /*Intent intent =  new Intent(getActivity(), ControlLayanan.class);
                intent.putExtra("ID", nListArray.get(position).get("MEKANIK").asInteger());
                intent.putExtra("DATA", nListArray.get(position).toJson());
                startActivityForResult(intent, REQUEST_CONTROL);*/
            }
        }));

//        NikitaAutoComplete bookTitle = (NikitaAutoComplete) findViewById(R.id.txtMekanik);
//        bookTitle.setThreshold(3);
//        bookTitle.setAdapter(new NsonAutoCompleteAdapter(this){
//            @Override
//            public Nson onFindNson(Context context, String bookTitle) {
//                Map<String, String> args = AppApplication.getInstance().getArgsData();
//                args.put("nama", bookTitle);
//                Nson result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrl("mekanik.php"),args)) ;
//
//                return result;
//            }
//            @Override
//            public View getView(int position, View convertView, ViewGroup parent) {
//                if (convertView == null) {
//                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//                    convertView = inflater.inflate(R.layout.find_nopol, parent, false);
//                }
//               findView(convertView, R.id.txtNopol, TextView.class).setText(getItem(position).get("MEKANIK").asString());
//
//                return convertView;
//            }
//            public View getDropDownView(int position, View convertView, ViewGroup parent) {
//                View v =  super.getView(position, convertView, parent);
//                if (v instanceof TextView){
//                    ((TextView) v).setText(  (getItem(position).get("MEKANIK").asString())  );
//                }
//                return v;
//            }
//        }); // 'this' is Activity instance
//        bookTitle.setLoadingIndicator(  (android.widget.ProgressBar) findViewById(R.id.pb_txtMekanik));
//        bookTitle.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
//                /*Book book = (Book) adapterView.getItemAtPosition(position);
//                bookTitle.setText(book.getTitle())*/;
//                Nson n = Nson.readJson(String.valueOf(adapterView.getItemAtPosition(position)));
//                find(R.id.txtMekanik, NikitaAutoComplete.class).setText(  n.get("MEKANIK").asString()  );
//            }
//        });

        layanan();
//        pengambilan();
//        kompliment();
        hitung();
//        mekanik();
    }

    private void layanan() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrl("layanan.php"), args));
                if (!result.isNsonArray()) {
                    result = Nson.newArray();
                }
                result.add(Nson.readNson("{'NAMA_LAYANAN':'PERBAIKAN RINGAN','MODE':'HCODE'}"));
            }


            public void runUI() {
                Spinner spinner = find(R.id.spnLayanan);
                layanan = result;
                ArrayAdapter<List> adapter = new ArrayAdapter<List>(getActivity(), android.R.layout.simple_spinner_dropdown_item, result.asArray()) {
                    public View getView(int position, @Nullable View convertView, ViewGroup parent) {
                        View v = super.getView(position, convertView, parent);
                        if (v instanceof TextView) {
                            ((TextView) v).setText(result.get(position).get("NAMA_LAYANAN").asString());

                        }
                        return v;
                    }

                    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                        View v = super.getView(position, convertView, parent);
                        if (v instanceof TextView) {
                            ((TextView) v).setText(result.get(position).get("NAMA_LAYANAN").asString());
                        }
                        return v;
                    }
                };
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
        });

        Nson nson = Nson.newArray().add("LAIN WAKTU");
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
        Spinner spinner = findViewById(R.id.spnKompliment);
        spinner.setAdapter(adapter);

//    private void kompliment() {
//        newProses(new Messagebox.DoubleRunnable() {
//            Nson result;
//
//            public void run() {
//                Map<String, String> args = AppApplication.getInstance().getArgsData();
//                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrl("checkin.php"), args));
//                if (!result.isNsonArray()) {
//                    result = Nson.newArray();
//                }
//                result.add(Nson.readNson("{'KOMPLIMENT':'LAIN WAKTU','MODE':'HCODE'}"));
//            }
//
//
//            public void runUI() {
//                Spinner spinner = find(R.id.spnKompliment);
//                ArrayAdapter<List> adapter = new ArrayAdapter<List>(getActivity(), android.R.layout.simple_spinner_dropdown_item, result.asArray()) {
//                    public View getView(int position, @Nullable View convertView, ViewGroup parent) {
//                        View v = super.getView(position, convertView, parent);
//                        if (v instanceof TextView) {
//                            ((TextView) v).setText(result.get(position).get("KOMPLIMENT").asString());
//
//                        }
//                        return v;
//                    }
//
//                    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
//                        View v = super.getView(position, convertView, parent);
//                        if (v instanceof TextView) {
//                            ((TextView) v).setText(result.get(position).get("KOMPLIMENT").asString());
//                        }
//                        return v;
//                    }
//                };
//                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                hitung();
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    final Nson nson1 = Nson.newArray().add("SELESAI").add("JAM");
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
    Spinner spinner1 = findViewById(R.id.spnPengambilan);
    spinner1.setAdapter(adapter1);
//    private void pengambilan() {
//        newProses(new Messagebox.DoubleRunnable() {
//            Nson result;
//
//            public void run() {
//                Map<String, String> args = AppApplication.getInstance().getArgsData();
//                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrl("checkin.php"), args));
//                if (!result.isNsonArray()) {
//                    result = Nson.newArray();
//                }
//                result.add(Nson.readNson("{'PENGAMBILAN':'SELESAI','MODE':'HCODE'}"));
//                result.add(Nson.readNson("{'PENGAMBILAN':'JAM','MODE':'HCODE'}"));
//
//            }
//
//
//            public void runUI() {
//                Spinner spinner = find(R.id.spnPengambilan);
//                ArrayAdapter<List> adapter = new ArrayAdapter<List>(getActivity(), android.R.layout.simple_spinner_dropdown_item, result.asArray()) {
//                    public View getView(int position, @Nullable View convertView, ViewGroup parent) {
//                        View v = super.getView(position, convertView, parent);
//                        if (v instanceof TextView) {
//                            ((TextView) v).setText(result.get(position).get("PENGAMBILAN").asString());
//
//                        }
//                        return v;
//                    }
//
//                    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
//                        View v = super.getView(position, convertView, parent);
//                        if (v instanceof TextView) {
//                            ((TextView) v).setText(result.get(position).get("PENGAMBILAN").asString());
//                        }
//                        return v;
//                    }
//                };
//                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                spinner.setAdapter(adapter);
                spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (position == nson1.size() - 1) {
                            find(R.id.txtJamPengambilan).setVisibility(View.VISIBLE);
                        } else {
                            find(R.id.txtJamPengambilan).setVisibility(View.GONE);
                        }

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

    final Nson nson2 = Nson.newArray().add("BRAMA").add("XERKA").add("REN").add("EXAD").add("ERKA");
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

        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinner2 = findViewById(R.id.spnMekanik);
        spinner2.setAdapter(adapter2);
//        private void mekanik() {
//            newProses(new Messagebox.DoubleRunnable() {
//                Nson result;
//
//                public void run() {
//                    Map<String, String> args = AppApplication.getInstance().getArgsData();
//                    result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrl("checkin.php"), args));
//                    if (!result.isNsonArray()) {
//                        result = Nson.newArray();
//                    }
//                }
//
//
//                public void runUI() {
//                    Spinner spinner = find(R.id.spnMekanik);
//                    ArrayAdapter<List> adapter = new ArrayAdapter<List>(getActivity(), android.R.layout.simple_spinner_dropdown_item, result.asArray()) {
//                        public View getView(int position, @Nullable View convertView, ViewGroup parent) {
//                            View v = super.getView(position, convertView, parent);
//                            if (v instanceof TextView) {
//                                ((TextView) v).setText(result.get(position).get("MEKANIK").asString());
//
//                            }
//                            return v;
//                        }
//
//                        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
//                            View v = super.getView(position, convertView, parent);
//                            if (v instanceof TextView) {
//                                ((TextView) v).setText(result.get(position).get("MEKANIK").asString());
//                            }
//                            return v;
//                        }
//                    };
//                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                    spinner.setAdapter(adapter);
                    spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });



        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("nopol", "helo");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrl("checkin.php"), args));

            }

            public void runUI() {

                find(R.id.txtTanggal, EditText.class).setText(result.get(0).get("TANGGAL").asString());
                find(R.id.txtKM, EditText.class).setText(result.get(0).get("KM").asString());
//                find(R.id.txtEstimasiSebelum, EditText.class).setText(result.get(0).get("ESTIMASI_SEBELUM").asString());
//                find(R.id.txtEstimasiSesudah, EditText.class).setText(result.get(0).get("ESTIMASI_SESUDAH").asString());
//                find(R.id.txtEstimasiSelesai, EditText.class).setText(result.get(0).get("ESTIMASI_SELESAI").asString());

//                if (result.get(0).get("PRINT_ESTIMASI_BIAYA").asString().equalsIgnoreCase("YES")){
//                    find(R.id.cckPrintEstimasi, CheckBox.class).setChecked(true);
//                }
//
//                if (result.get(0).get("KENDARAAN_DITINGGAL").asString().equalsIgnoreCase("YES")){
//                    find(R.id.cckKendaraanDitinggal, CheckBox.class).setChecked(true);
//                }
//
//                if (result.get(0).get("TUNGGU_KONFIRMASI").asString().equalsIgnoreCase("YES")){
//                    find(R.id.cckTungguKonfirmasi, CheckBox.class).setChecked(true);
//                }

//                find(R.id.txtJamPengambilan, EditText.class).setText(result.get(0).get("JAM_PENGAMBILAN").asString());

//                if (result.get(0).get("KONFIRMASI_TAMBAHAN").asString().equalsIgnoreCase("YES")){
//                    find(R.id.cckKonfirmasiTambahan, CheckBox.class).setChecked(true);
//                }
//
//                if (result.get(0).get("TINGGALKAN_STNK").asString().equalsIgnoreCase("YES")){
//                    find(R.id.cckTinggalkanSTNK, CheckBox.class).setChecked(true);
//                }
//
//                if (result.get(0).get("BUANG_PART").asString().equalsIgnoreCase("YES")){
//                    find(R.id.cckBuangPartBekas, CheckBox.class).setChecked(true);
//                }
//
                find(R.id.txtLevelBBM, EditText.class).setText(result.get(0).get("LEVEL_BBM").asString());
//
//
//                if (result.get(0).get("PERSETUJUAN").asString().equalsIgnoreCase("YES")){
//                    find(R.id.cckSetuju, CheckBox.class).setChecked(true);
//                }

//                find(R.id.txtMekanik, EditText.class).setText(result.get(0).get("MEKANIK").asString());

            }
        });
    }

    public void save() {
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Nson nson = Nson.readNson(getIntentStringExtra("dx"));
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("nopol", nson.get("nopol").asString());
                args.put("nama", nson.get("nama").asString());
                args.put("layanan", nson.get("layanan").asString());
                args.put("phone", nson.get("phone").asString());
                args.put("status", nson.get("status").asString());
                args.put("mekanik", nson.get("mekanik").asString());
                args.put("merk", nson.get("merk").asString());
                args.put("total_biaya", find(R.id.txtTotalBiaya, EditText.class).getText().toString());
                args.put("sisa", find(R.id.txtSisa, EditText.class).getText().toString());
                args.put("dp", find(R.id.txtDP, EditText.class).getText().toString());
                args.put("estimasi_sebelum", find(R.id.txtEstimasiSebelum, EditText.class).getText().toString());
                args.put("estimasi_sesudah", find(R.id.txtEstimasiSesudah, EditText.class).getText().toString());
                args.put("estimasi_selesai", find(R.id.txtEstimasiSelesai, EditText.class).getText().toString());
                args.put("jam_pengambilan", find(R.id.txtJamPengambilan, EditText.class).getText().toString());
                args.put("level_bbm", find(R.id.txtLevelBBM, EditText.class).getText().toString());

                args.put("print_estimasi_biaya", find(R.id.cckPrintEstimasi, CheckBox.class).isChecked()?"YES":"NO");
                args.put("kendaraan_ditinggal", find(R.id.cckKendaraanDitinggal, CheckBox.class).isChecked()?"YES":"NO");
                args.put("tunggu_konfirmasi", find(R.id.cckTungguKonfirmasi, CheckBox.class).isChecked()?"YES":"NO");
                args.put("konfirmasi_tambahan", find(R.id.cckKonfirmasiTambahan, CheckBox.class).isChecked()?"YES":"NO");
                args.put("tinggalkan_stnk", find(R.id.cckTinggalkanSTNK, CheckBox.class).isChecked()?"YES":"NO");
                args.put("buang_part", find(R.id.cckBuangPartBekas, CheckBox.class).isChecked()?"YES":"NO");
                args.put("persetujuan", find(R.id.cckSetuju, CheckBox.class).isChecked()?"YES":"NO");

                String out = InternetX.postHttpConnection(AppApplication.getBaseUrl("save.php"), args);

                result = Nson.readJson(out);

            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showError(result.get("message").asString());
                }


            }
        });
    }



        /*NikitaAutoComplete bookTitle = (NikitaAutoComplete) findViewById(R.id.txtLayanan);
        bookTitle.setThreshold(3);
        bookTitle.setAdapter(new NsonAutoCompleteAdapter(this){
            @Override
            public Nson onFindNson(Context context, String bookTitle) {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("nama", bookTitle);
                Nson result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrl("layanan.php"),args)) ;

                return result;
            }
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.find_nopol, parent, false);
                }
                findView(convertView, R.id.txtNopol, TextView.class).setText(getItem(position).get("LAYANAN").asString());

                return convertView;
            }
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View v =  super.getView(position, convertView, parent);
                if (v instanceof TextView){
                    ((TextView) v).setText(  (getItem(position).get("LAYANAN").asString())  );
                }
                return v;
            }
        }); // 'this' is Activity instance
        bookTitle.setLoadingIndicator(  (android.widget.ProgressBar) findViewById(R.id.pb_txtLayanaan));
        bookTitle.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                *//*Book book = (Book) adapterView.getItemAtPosition(position);
                bookTitle.setText(book.getTitle())*//*;
                Nson n = Nson.readJson(String.valueOf(adapterView.getItemAtPosition(position)));
                find(R.id.txtLayanan, NikitaAutoComplete.class).setText(  n.get("LAYANAN").asString()  );
            }
        });*/

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PENDAFTARAN && resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            finish();
        } else if (requestCode == REQUEST_PART && resultCode == RESULT_OK) {
            nListArray.add(Nson.readJson(getIntentStringExtra(data, "DATA")));
            find(R.id.rViewPart, RecyclerView.class).getAdapter().notifyDataSetChanged();
            hitung();
        } else if (requestCode == REQUEST_JASA && resultCode == RESULT_OK) {
            nListArray.add(Nson.readJson(getIntentStringExtra(data, "DATA")));
            find(R.id.rViewPart, RecyclerView.class).getAdapter().notifyDataSetChanged();
            hitung();
        } else if (requestCode == REQUEST_JASA_BERKALA && resultCode == RESULT_OK) {
            nListArray.add(Nson.readJson(getIntentStringExtra(data, "DATA")));
            find(R.id.rViewPart, RecyclerView.class).getAdapter().notifyDataSetChanged();
            hitung();
        } else if (requestCode == REQUEST_PART_BERKALA && resultCode == RESULT_OK) {
            nListArray.add(Nson.readJson(getIntentStringExtra(data, "DATA")));
            find(R.id.rViewPart, RecyclerView.class).getAdapter().notifyDataSetChanged();
            hitung();
        } else if (requestCode == REQUEST_PART_EXTERNAL && resultCode == RESULT_OK) {
            nListArray.add(Nson.readJson(getIntentStringExtra(data, "DATA")));
            find(R.id.rViewPart, RecyclerView.class).getAdapter().notifyDataSetChanged();
            hitung();
        }
    }

   void hitung(){
        int sp = find(R.id.spnLayanan,Spinner.class).getSelectedItemPosition();
        int total = layanan.get(sp).get("BIAYA").asInteger();
        for (int i = 0; i < nListArray.size() ; i++) {
            total=total+nListArray.get(i).get("HARGA_JUAL").asInteger();

        }
        find(R.id.txtTotalBiaya,EditText.class).setText(String.valueOf(total));
        find(R.id.txtSisa,EditText.class).setText(String.valueOf(total));

    }
}
