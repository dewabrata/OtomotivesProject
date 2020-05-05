package com.rkrzmail.oto.gmod;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

public class PenjualanPartActivity extends AppActivity {

    final int REQUEST_PART2 = 110;
    private RecyclerView recyclerView;
    private View parent_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_penjualan_part);
        parent_view = findViewById(android.R.id.content);

        //initComponent();
        initToolbar();


        find(R.id.txtNopol).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
                    Nson result;

                    public void run() {
                        Map<String, String> args = AppApplication.getInstance().getArgsData();
                        String nopol = find(R.id.txtNopol, EditText.class).getText().toString();
                        nopol = nopol.toUpperCase().trim().replace(" ", "");
                        args.put("nopol", nopol);
                        result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV2("carinopol"), args));
                    }

                    @Override
                    public void runUI() {
                        if (result.isNson()) {
                            if (result.size() >= 1) {
                                //PHONE
                                find(R.id.txtPhone, EditText.class).setText(result.get(0).get("PHONE").asString());
                                return;
                            }
                        }
                        //error
                        showError("Tidak Ditemukan");
                    }
                });
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), PartActivity.class);
                intent.putExtra("y", "y");
                startActivityForResult(intent, REQUEST_PART2);

            }


        });

        find(R.id.tblSimpan).setVisibility(View.GONE);
        find(R.id.tblSimpan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String nopol = find(R.id.txtNopol, EditText.class).getText().toString().replace(" ", "").toUpperCase();
                if (nopol.equalsIgnoreCase("")) {
                    showError("No. Polisi Tidak Boleh Kosong");
                    return;
                } else if (find(R.id.txtPhone, EditText.class).getText().toString().equalsIgnoreCase("")) {
                    showError("No. Ponsel Tidak Boleh Kosong");
                    return;
                }
                simpan();
                Intent intent = new Intent(getActivity(), MenuActivity.class);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        penjualan();

    }

    private void penjualan() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrl("jualpart.php"), args));
                if (!result.isNsonArray()) {
                    result = Nson.newArray();
                }
                result.add(Nson.readNson("{'JENIS_PENJUALAN':'KENDARAAN','MODE':'HCODE'}"));
                result.add(Nson.readNson("{'JENIS_PENJUALAN':'GROSIR','MODE':'HCODE'}"));
            }


            public void runUI() {
                final Spinner spinner = find(R.id.sp_penjualan_part);
                ArrayAdapter<List> adapter = new ArrayAdapter<List>(getActivity(), android.R.layout.simple_spinner_dropdown_item, result.asArray()) {
                    public View getView(int position, @Nullable View convertView, ViewGroup parent) {
                        View v = super.getView(position, convertView, parent);
                        if (v instanceof TextView) {
                            ((TextView) v).setText(result.get(position).get("JENIS_PENJUALAN").asString());

                        }
                        return v;
                    }

                    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                        View v = super.getView(position, convertView, parent);
                        if (v instanceof TextView) {
                            ((TextView) v).setText(result.get(position).get("JENIS_PENJUALAN").asString());
                        }
                        return v;
                    }
                };
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                        if (position == result.size() -1) {
                            find(R.id.txtNamaUsaha).setVisibility(View.VISIBLE);
                            find(R.id.txtNopol).setVisibility(View.GONE);
                            find(R.id.txtJenisKendaraan).setVisibility(View.GONE);
                        } else {
                            find(R.id.txtNamaUsaha).setVisibility(View.GONE);
                            find(R.id.txtNopol).setVisibility(View.VISIBLE);
                            find(R.id.txtJenisKendaraan).setVisibility(View.VISIBLE);
                        }

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
        });


        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rViewPenjualanPart);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        recyclerView.setAdapter(new NikitaRecyclerAdapter(nListArray, R.layout.item_part) {
            public void onBindViewHolder(@NonNull NikitaViewHolder viewHolder, int position) {
                viewHolder.find(R.id.txtNoPart, TextView.class).setText("No. Part : " + nListArray.get(position).get("NO_PART").asString());
                viewHolder.find(R.id.txtOngkosKirim, TextView.class).setText(nListArray.get(position).get("NAMA_PART").asString());
                viewHolder.find(R.id.txtHargaPart, TextView.class).setText("Harga Part : " + nListArray.get(position).get("HARGA_PART").asString());
                viewHolder.find(R.id.txtHargaBersih, TextView.class).setText("Harga Bersih : " + nListArray.get(position).get("HARGA_BERSIH").asString());
                viewHolder.find(R.id.txtJumlahPart, TextView.class).setText("Jumlah : " + nListArray.get(position).get("JUMLAH").asString());
                viewHolder.find(R.id.txtDiscount, TextView.class).setText("Discount : " + nListArray.get(position).get("DISC").asString());

                if(nListArray != null){
                    find(R.id.tblSimpan).setVisibility(View.VISIBLE);
                }

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


        NikitaAutoComplete bookTitle = (NikitaAutoComplete) findViewById(R.id.txtNopol);
        bookTitle.setThreshold(3);
        bookTitle.setAdapter(new NsonAutoCompleteAdapter(this) {
            @Override
            public Nson onFindNson(Context context, String bookTitle) {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                String nopol = bookTitle.replace(" ", "").toUpperCase();
                args.put("nopol", nopol);
                Nson result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrl("nopol.php"), args));

                return result;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.find_nopol, parent, false);
                }
                findView(convertView, R.id.txtNopol, TextView.class).setText(formatNopol(getItem(position).get("NOPOL").asString()));

                return convertView;
            }


        }); // 'this' is Activity instance
        bookTitle.setLoadingIndicator((android.widget.ProgressBar) findViewById(R.id.pb_txtNopol));
        bookTitle.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                /*Book book = (Book) adapterView.getItemAtPosition(position);
                bookTitle.setText(book.getTitle())*/
                ;
                Nson n = Nson.readJson(String.valueOf(adapterView.getItemAtPosition(position)));
                find(R.id.txtNopol, NikitaAutoComplete.class).setText(formatNopol(n.get("NOPOL").asString()));
                checkHistory(n.get("NOPOL").asString());
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

    }

    private void checkHistory(final String nopol) {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                String nopol = find(R.id.txtNopol, EditText.class).getText().toString();
                nopol = nopol.toUpperCase().trim().replace(" ", "");
                args.put("nopol", nopol);
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrl("checkhistory.php"), args));
            }

            public void runUI() {
                if (result.get("STATUS").asString().equalsIgnoreCase("OK")) {
                    if (result.containsKey("PHONE")) {
                        find(R.id.txtPhone, EditText.class).setText(result.get("PHONE").asString());
                    }

                }
            }
        });
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //toolbar.setNavigationIcon(R.drawable.ic_menu);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("PENJUALAN PART");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }


    private void simpan() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                String penjualan = getSelectedSpinnerText(R.id.sp_penjualan_part);
                penjualan = penjualan.toUpperCase().trim().replace(" ", "");

                String nopol = find(R.id.txtNopol, EditText.class).getText().toString();
                nopol = nopol.toUpperCase().trim().replace(" ", "");

                String phone = find(R.id.txtPhone, EditText.class).getText().toString();
                phone = phone.toUpperCase().trim().replace(" ", "");


                args.put("jenis_penjualan", penjualan);
                args.put("nopol", nopol);
                args.put("jenis_kendaraan", find(R.id.txtJenisKendaraan, EditText.class).getText().toString());
                args.put("phone", phone);
                args.put("nama", find(R.id.txtNamaPelanggan, EditText.class).getText().toString());
                args.put("nama_usaha", find(R.id.txtNamaUsaha, EditText.class).getText().toString());
                args.put("parts", nListArray.toJson());

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrl("jualpart.php"), args));

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

        protected void onActivityResult ( int requestCode, int resultCode, @Nullable Intent data){
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == REQUEST_PART2 && resultCode == RESULT_OK) {
                nListArray.add(Nson.readJson(getIntentStringExtra(data, "DATA2")));
                find(R.id.rViewPenjualanPart, RecyclerView.class).getAdapter().notifyDataSetChanged();
            }
        }
    }

