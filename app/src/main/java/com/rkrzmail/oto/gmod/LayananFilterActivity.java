package com.rkrzmail.oto.gmod;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.internal.Primitives;
import com.google.gson.reflect.TypeToken;
import com.naa.data.Nson;
import com.naa.data.UtilityAndroid;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.DaftarkanPelayananActivity;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

public class LayananFilterActivity extends AppActivity {
    final int BARCODE_RESULT = 12;
    private RecyclerView recyclerView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layanan_flter);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setTitle("DAFTAR PELAYANAN FILTER");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        setupLokasi();
        setupMekanik();
        setupStatus();



        find(R.id.tblSimpan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        find(R.id.tblBatal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    public void setupLokasi() {
        MessageMsg.newTask(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();


                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrl("checkin/lokasi"), args));
            }

            public void runUI() {
                ArrayAdapter<List> adapter = new ArrayAdapter<List>(getActivity(), android.R.layout.simple_spinner_dropdown_item, result.asArray()) {
                    public View getView(int position, @Nullable View convertView, ViewGroup parent) {
                        View v = super.getView(position, convertView, parent);
                        if (v instanceof TextView) {
                            ((TextView) v).setText("");
                        }
                        return v;
                    }

                    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                        View v = super.getView(position, convertView, parent);
                        if (v instanceof TextView) {
                            ((TextView) v).setText("");
                        }
                        return v;
                    }
                };
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                Spinner prov = findViewById(R.id.spnlokasi);
                prov.setAdapter(adapter);
            }
        });
    }
    public void setupMekanik() {
        MessageMsg.newTask(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();


                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrl("checkin/mekanik"), args));
            }

            public void runUI() {
                ArrayAdapter<List> adapter = new ArrayAdapter<List>(getActivity(), android.R.layout.simple_spinner_dropdown_item, result.asArray()) {
                    public View getView(int position, @Nullable View convertView, ViewGroup parent) {
                        View v = super.getView(position, convertView, parent);
                        /*if (v instanceof TextView) {
                            ((TextView) v).setText("");
                        }*/
                        return v;
                    }

                    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                        View v = super.getView(position, convertView, parent);
                        /*if (v instanceof TextView) {
                            ((TextView) v).setText("");
                        }*/
                        return v;
                    }
                };
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                Spinner prov = findViewById(R.id.spnmekanik);
                prov.setAdapter(adapter);
            }
        });
    }

    public void setupStatus() {
        MessageMsg.newTask(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                result = Nson.readNson("['KONFIRMASI','BATAL','ANTRIAN','MEKANIK','SERVIS SELESAI','TERBAYAR','CHECK OUT']");
            }

            public void runUI() {
                ArrayAdapter<List> adapter = new ArrayAdapter<List>(getActivity(), android.R.layout.simple_spinner_dropdown_item, result.asArray()) {
                    public View getView(int position, @Nullable View convertView, ViewGroup parent) {
                        View v = super.getView(position, convertView, parent);
                       /* if (v instanceof TextView) {
                            ((TextView) v).setText("");
                        }*/
                        return v;
                    }

                    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                        View v = super.getView(position, convertView, parent);
                       /* if (v instanceof TextView) {
                            ((TextView) v).setText("");
                        }*/
                        return v;
                    }
                };
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                Spinner prov = findViewById(R.id.spnstatus);
                prov.setAdapter(adapter);

            }
        });
    }
}
