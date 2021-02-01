package com.rkrzmail.oto.modules.Fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.bengkel.Dashboard_MainTab_Activity;
import com.rkrzmail.oto.modules.bengkel.ProfileBengkel_Activity;
import com.rkrzmail.srv.MultiSelectionSpinner;
import com.rkrzmail.utils.Tools;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.rkrzmail.utils.APIUrls.VIEW_DASHBOARD;
import static com.rkrzmail.utils.APIUrls.VIEW_JENIS_KENDARAAN;
import static com.rkrzmail.utils.APIUrls.VIEW_MASTER;
import static com.rkrzmail.utils.APIUrls.VIEW_PROFILE;
import static com.rkrzmail.utils.Tools.setFormatDayAndMonthToDb;

public class TabUsaha_Fragment extends Fragment {

    private EditText etNamaBengkel, etAlamat, etBadanUsaha, etKotaKab, etNoponsel, etNib, etNpwp, etKodePos;
    private Spinner spAfiliasi, spPrincial, spJenisKendaraan, spBidangUsaha, spMerkKendaraan, spAktivitasUsaha;
    private Button btnSimpan;
    private CheckBox cbPkp;
    private Nson merkKendaraanList = Nson.newArray(), bidangUsahaList  = Nson.newArray();
    private AppActivity activity;
    private final List<String> jenisKendaraanList = Arrays.asList(
            "--PILIH--",
            "MOTOR",
            "MOBIL"
    );
    private final List<String> afiliasiList = Arrays.asList(
            "--PILIH--",
            "JARINGAN",
            "INDIVIDUAL"
    );


    public TabUsaha_Fragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_tab_usaha_bengkel, container, false);
        activity = ((ProfileBengkel_Activity) getActivity());
        initComponent(v);
        return v;
    }

    private void initComponent(View v) {
        viewprofileusaha();
        etNamaBengkel = v.findViewById(R.id.et_namaBengkel_usaha);
        etAlamat = v.findViewById(R.id.et_alamat_usaha);
        etKodePos =v.findViewById(R.id.et_kodepos_usaha);
        etBadanUsaha = v.findViewById(R.id.et_namaUsaha_usaha);
        etKotaKab = v.findViewById(R.id.et_kotaKab_usaha);
        etNoponsel = v.findViewById(R.id.et_noPhone_usaha);
        etNib = v.findViewById(R.id.et_nib_usaha);
        etNpwp = v.findViewById(R.id.et_npwp_usaha);
        spAfiliasi = v.findViewById(R.id.sp_afiliasi_usaha);
        spPrincial = v.findViewById(R.id.sp_namaPrincial_usaha);
        spJenisKendaraan = v.findViewById(R.id.sp_jenisKendaraan_usaha);
        spBidangUsaha = v.findViewById(R.id.sp_bidangUsaha_usaha);
        spMerkKendaraan = v.findViewById(R.id.sp_merkKendaraan_usaha);
        btnSimpan = v.findViewById(R.id.btn_simpan_usaha);
        cbPkp = v.findViewById(R.id.cb_pkp_usaha);

        activity.setSpinnerOffline(afiliasiList, spAfiliasi,"");
        spPrincial.setEnabled(false);

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });
    }

    private void saveData() {
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            @Override
            public void run() {

            }
            @Override
            public void runUI() {

            }
        });
    }

    private void viewprofileusaha(){
        activity.newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "view");

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_PROFILE), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data");
                    for (int i = 0; i < result.size(); i++) {
                        etNamaBengkel.setText(result.get(i).get("NAMA_BENGKEL").asString());
                        etAlamat.setText(result.get(i).get("ALAMAT").asString());
                        etKodePos.setText(result.get(i).get("KODE_POS").asString());
                        etKotaKab.setText(result.get(i).get("KOTA_KABUPATEN").asString());
                        etBadanUsaha.setText(result.get(i).get("NAMA_USAHA").asString());
                        setSpJenisKendaraan(result.get(i).get("JENIS_KENDARAAN").asString());
                        setSpMerkKendaraan(result.get(i).get("MERK_KENDARAAN").asString());
                        setSpBidangUsaha(result.get(i).get("KATEGORI_BENGKEL").asString());
                    }
                } else {
                    activity.showInfo(result.get("message").asString());
                }
            }
        });
    }

    private void setSpJenisKendaraan(String selection) {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, jenisKendaraanList) {
            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                final View v = super.getDropDownView(position, convertView, parent);
                v.post(new Runnable() {
                    @SuppressLint("WrongConstant")
                    @Override
                    public void run() {
                        ((TextView) v.findViewById(android.R.id.text1)).setSingleLine(false);
                        ((TextView) v.findViewById(android.R.id.text1)).setGravity(Gravity.CENTER);
                        ((TextView) v.findViewById(android.R.id.text1)).setTextAlignment(Gravity.CENTER);
                    }
                });
                return v;
            }
        };
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spJenisKendaraan.setAdapter(spinnerAdapter);
        spJenisKendaraan.setEnabled(false);
        if (!selection.isEmpty()) {
            for (int in = 0; in < spJenisKendaraan.getCount(); in++) {
                if (spJenisKendaraan.getItemAtPosition(in).toString().contains(selection)) {
                    spJenisKendaraan.setSelection(in);
                    break;
                }
            }
        }
    }

    private void setSpMerkKendaraan(final String selection){
        spMerkKendaraan.setEnabled(false);
        activity.newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_JENIS_KENDARAAN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    for (int i = 0; i < result.size(); i++) {
                        merkKendaraanList.add(result.get("data").get(i).get("MERK") + " - " + result.get("data").get(i).get("TYPE"));
                    }
                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, merkKendaraanList.asArray());
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spMerkKendaraan.setAdapter(spinnerAdapter);
                    if (!selection.isEmpty()) {
                        for (int i = 0; i < spMerkKendaraan.getCount(); i++) {
                            if (spJenisKendaraan.getItemAtPosition(i).toString().contains(selection)) {
                                spJenisKendaraan.setSelection(i);
                                break;
                            }
                        }
                    }
                } else {
                    activity.showInfo("Merk Kendaraan Gagal Di Muat");
                }
            }
        });
    }

    private void setSpBidangUsaha(final String selection){
        spBidangUsaha.setEnabled(false);
        activity.newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("nama", "BENGKEL");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_MASTER), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {

                    for (int i = 0; i < result.size(); i++) {
                        bidangUsahaList.add(result.get("data").get(i).get("KATEGORI") + " - " + result.get("data").get(i).get("TYPE"));
                    }
                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, bidangUsahaList.asArray());
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spBidangUsaha.setAdapter(spinnerAdapter);
                    if (!selection.isEmpty()) {
                        for (int i = 0; i < spBidangUsaha.getCount(); i++) {
                            if (spBidangUsaha.getItemAtPosition(i).toString().contains(selection)) {
                                spBidangUsaha.setSelection(i);
                                break;
                            }
                        }
                    }
                } else {
                    activity.showInfo("Merk Kendaraan Gagal Di Muat");
                }
            }
        });
    }

}
