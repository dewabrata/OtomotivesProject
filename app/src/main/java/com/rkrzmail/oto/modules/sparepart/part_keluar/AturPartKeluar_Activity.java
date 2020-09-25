package com.rkrzmail.oto.modules.sparepart.part_keluar;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.sparepart.CariPart_Activity;
import com.rkrzmail.oto.modules.tenda.AturTenda_Activity;
import com.rkrzmail.oto.modules.user.AturUser_Activity;

import java.util.Map;
import java.util.Objects;

public class AturPartKeluar_Activity extends AppActivity {

    public static final int REQUEST_MEKANIK = 10, REQUEST_LOKASI = 11, REQUEST_PART = 12;
    private Nson mekanikArray = Nson.newArray(), lokasiArray = Nson.newArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_part_keluar);
        initComponent();
    }

    @SuppressLint("NewApi")
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Part Keluar");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {
        initToolbar();
        setSpMekanik();
        setSpLokasi();
        find(R.id.btn_simpan, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), CariPart_Activity.class);
                i.putExtra("bengkel", "");
                startActivityForResult(i, REQUEST_PART);
            }
        });
    }

    private void setSpMekanik() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson data;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                data = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("mekanik"), args));
            }

            @Override
            public void runUI() {
                if (data.get("status").asString().equalsIgnoreCase("OK")) {
                    if (data.get("data").asArray().size() == 0) {
                        showInfo("Mekanik Belum Tercatatkan, Silahkan Daftarkan Mekanik Di Menu USER");
                        Messagebox.showDialog(getActivity(), "Mekanik Belum Di Catatkan", "Catatkan Mekanik ?", "Ya", "Tidak", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivityForResult(new Intent(getActivity(), AturUser_Activity.class), REQUEST_MEKANIK);
                            }
                        }, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        return;
                    }
                    mekanikArray.add("--PILIH--");
                    for (int i = 0; i < data.get("data").size(); i++) {
                        //idMekanikArray.add(Nson.newObject().set("ID", data.get("data").get(i).get("ID").asString()).set("NAMA", data.get("data").get(i).get("NAMA").asString()));
                        mekanikArray.add(data.get("data").get(i).get("NAMA").asString());
                    }
                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, mekanikArray.asArray());
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    find(R.id.sp_namaMekanik_checkin4, Spinner.class).setAdapter(spinnerAdapter);
                } else {
                    showInfoDialog("Nama Mekanik Gagal Di Muat, Muat Ulang ?", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setSpMekanik();
                        }
                    });
                }
            }
        });
    }

    private void setSpLokasi() {
        newProses(new Messagebox.DoubleRunnable() {
            Nson data;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "view");
                data = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("aturtenda"), args));
            }

            @Override
            public void runUI() {
                if (data.get("status").asString().equalsIgnoreCase("OK")) {
                    if (data.get("data").asArray().size() == 0) {
                        Messagebox.showDialog(getActivity(), "Konfirmasi", "Lokasi Tenda Belum Tercatatkan, Catatkan Lokasi Tenda ?", "Ya", "Tidak", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                startActivityForResult(new Intent(getActivity(), AturTenda_Activity.class), REQUEST_LOKASI);
                            }
                        }, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                showInfo("Lokasi Tersedia Hanya Bengkel");
                            }
                        });
                    }

                    lokasiArray.add("--PILIH--");
                    for (int i = 0; i < data.get("data").size(); i++) {
                        lokasiArray.add(data.get("data").get(i).get("NAMA_LOKASI").asString());
                    }
                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, lokasiArray.asArray());
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    find(R.id.sp_namaMekanik_checkin4, Spinner.class).setAdapter(spinnerAdapter);

                } else {
                    showInfoDialog("Lokasi Gagal Di Muat, Muat Ulang ?", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setSpLokasi();
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            switch (requestCode){
                case REQUEST_LOKASI:
                    setSpLokasi();
                    break;
                case REQUEST_MEKANIK:
                    setSpMekanik();
                    break;
                case REQUEST_PART:
                    Intent i = new Intent(getActivity(), Jumlah_PartKeluar_Activity.class);
                    i.putExtra("bengkel", Nson.readJson(getIntentStringExtra(data, "part")).toJson());
                    startActivityForResult(i, REQUEST_PART);
                    break;

            }
        }

    }
}
