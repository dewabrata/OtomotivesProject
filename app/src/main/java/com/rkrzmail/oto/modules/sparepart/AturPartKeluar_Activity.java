package com.rkrzmail.oto.modules.sparepart;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.bengkel.AturTenda_Activity;
import com.rkrzmail.oto.modules.bengkel.AturUser_Activity;

import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.ConstUtils.ATUR;
import static com.rkrzmail.utils.ConstUtils.LANJUT;
import static com.rkrzmail.utils.ConstUtils.REQUEST_CARI_PART;
import static com.rkrzmail.utils.ConstUtils.REQUEST_DAFTAR_PART_KELUAR;
import static com.rkrzmail.utils.ConstUtils.REQUEST_LOKASI;
import static com.rkrzmail.utils.ConstUtils.REQUEST_MEKANIK;
import static com.rkrzmail.utils.ConstUtils.REQUEST_PART_KELUAR;

public class AturPartKeluar_Activity extends AppActivity {

    private Nson
            mekanikArray = Nson.newArray(),
            lokasiArray = Nson.newArray(), partKeluarJson = Nson.newObject();
    private boolean isSelected = false;

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
        find(R.id.rg_lokasi, RadioGroup.class).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(group.getCheckedRadioButtonId() == checkedId)
                   isSelected = true;
            }
        });
        find(R.id.btn_simpan, Button.class).setText(LANJUT);
        find(R.id.btn_simpan, Button.class).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (find(R.id.sp_nama_mekanik, Spinner.class).getSelectedItem().toString().equals("--PILIH--")) {
                    showWarning("Nama Mekanik Belum Di Pilih");
                    find(R.id.sp_nama_mekanik, Spinner.class).performClick();
                } else if (!isSelected) {
                    showWarning("Penggunaan Belum Di Pilih");
                } else if (find(R.id.sp_lokasi_tenda, Spinner.class).getSelectedItem().toString().equals("--PILIH--")) {
                    showWarning("Tenda Belum Di Pilih");
                    find(R.id.sp_lokasi_tenda, Spinner.class).performClick();
                } else {
                    Intent i = new Intent(getActivity(), CariPart_Activity.class);
                    i.putExtra("cari_part_lokasi", "RUANG PART");
                    startActivityForResult(i, REQUEST_CARI_PART);
                }
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
                    find(R.id.sp_nama_mekanik, Spinner.class).setAdapter(spinnerAdapter);
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
                    find(R.id.sp_lokasi_tenda, Spinner.class).setAdapter(spinnerAdapter);

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
        if (resultCode == RESULT_OK) {
            Intent i;
            switch (requestCode) {
                case REQUEST_LOKASI:
                    setSpLokasi();
                    break;
                case REQUEST_MEKANIK:
                    setSpMekanik();
                    break;
                case REQUEST_CARI_PART:
                    Nson nson = Nson.readJson(getIntentStringExtra(data, "part"));
                    partKeluarJson.set("NAMA_PART", nson.get("NAMA_PART"));
                    partKeluarJson.set("NO_PART", nson.get("NO_PART"));
                    partKeluarJson.set("MERK", nson.get("MERK"));
                    partKeluarJson.set("STOCK_RUANG_PART", nson.get("STOCK_RUANG_PART"));
                    partKeluarJson.set("KODE", nson.get("KODE"));
                    partKeluarJson.set("NAMA_MEKANIK", find(R.id.sp_nama_mekanik, Spinner.class).getSelectedItem().toString());
                    partKeluarJson.set("LOKASI", find(R.id.sp_lokasi_tenda, Spinner.class).getSelectedItem().toString());
                    partKeluarJson.set("PART_ID", nson.get("PART_ID"));
                    partKeluarJson.set("PENDING", nson.get("PENDING"));
                    i = new Intent(getActivity(), JumlahPartKeluar_Activity.class);
                    i.putExtra("part", partKeluarJson.toJson());
                    i.putExtra(ATUR, "");
                    startActivityForResult(i, REQUEST_DAFTAR_PART_KELUAR);
                    break;
                case REQUEST_DAFTAR_PART_KELUAR:
                    Nson nson2 = Nson.readJson(getIntentStringExtra(data, "part"));
                    i = new Intent(getActivity(), DetailPartKeluar_Activity.class);
                    i.putExtra(ATUR, "");
                    i.putExtra("part", nson2.toJson());
                    startActivityForResult(i, REQUEST_PART_KELUAR);
                    break;
                case REQUEST_PART_KELUAR:
                    setResult(RESULT_OK);
                    finish();
                    break;

            }
        }

    }
}
