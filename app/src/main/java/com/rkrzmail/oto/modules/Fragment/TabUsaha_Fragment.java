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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static com.rkrzmail.utils.APIUrls.SET_CLAIM;
import static com.rkrzmail.utils.APIUrls.VIEW_DASHBOARD;
import static com.rkrzmail.utils.APIUrls.VIEW_JENIS_KENDARAAN;
import static com.rkrzmail.utils.APIUrls.VIEW_MASTER;
import static com.rkrzmail.utils.APIUrls.VIEW_PROFILE;
import static com.rkrzmail.utils.Tools.setFormatDayAndMonthToDb;

public class TabUsaha_Fragment extends Fragment {

    private EditText etNamaBengkel, etAlamat, etBadanUsaha, etKotaKab, etNoponsel, etNib, etNpwp, etKodePos, etnoPhoneMessage;
    private Spinner spAfiliasi, spPrincial;
    private MultiSelectionSpinner spJenisKendaraan,spMerkKendaraan,spBidangUsaha;
    private Button btnSimpan, btnLokasi;
    private CheckBox cbPkp;
    private Nson merkKendaraanList = Nson.newArray(), bidangUsahaList  = Nson.newArray(), principalList = Nson.newArray();
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
        etnoPhoneMessage = v.findViewById(R.id.et_noPhone_message);
        etNib = v.findViewById(R.id.et_nib_usaha);
        etNpwp = v.findViewById(R.id.et_npwp_usaha);
        spAfiliasi = v.findViewById(R.id.sp_afiliasi_usaha);
        spPrincial = v.findViewById(R.id.sp_namaPrincial_usaha);
        spJenisKendaraan = v.findViewById(R.id.sp_jenisKendaraan_usaha);
        spBidangUsaha = v.findViewById(R.id.sp_bidangUsaha_usaha);
        spMerkKendaraan = v.findViewById(R.id.sp_merkKendaraan_usaha);
        btnSimpan = v.findViewById(R.id.btn_simpan_usaha);
        btnLokasi = v.findViewById(R.id.btn_lokasi_tambahan);
        cbPkp = v.findViewById(R.id.cb_pkp_usaha);

        activity.setSpinnerOffline(afiliasiList, spAfiliasi,"");
        setSpNamaPrincipal("");

        btnLokasi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });
    }

    private void saveData() {
        activity.newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                args.put("action", "update");
                args.put("kategori", "USAHA");
                args.put("kodePos", etKodePos.getText().toString().toUpperCase());
                args.put("namaUsaha", etBadanUsaha.getText().toString().toUpperCase());
                args.put("nib", etNib.getText().toString().toUpperCase());
                args.put("npwp", etNpwp.getText().toString().toUpperCase());
                args.put("pkp", cbPkp.isChecked() ? "Y" : "N");
                args.put("afliasi", spAfiliasi.getSelectedItem().toString());
                args.put("noTelp", etNoponsel.getText().toString().toUpperCase());
                args.put("hpMessage", etnoPhoneMessage.getText().toString().toUpperCase());

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_PROFILE), args));
            }
            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    activity.showInfo("Sukses Menyimpan Data");
                    activity.setResult(RESULT_OK);
                } else {
                    activity.showInfo("Gagagl Menyimpan Data");
                }
            }
        });
    }

    private void viewprofileusaha(){
        activity.newProses(new Messagebox.DoubleRunnable() {
            Nson result;
            List<String> jenisList = new ArrayList<>(), merkList = new ArrayList<>(),
            bidangList = new ArrayList<>();
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
                        jenisList.add(result.get(i).get("JENIS_KENDARAAN").asString());
                        merkList.add(result.get(i).get("MERK_KENDARAAN").asString());
                        bidangList.add(result.get(i).get("KATEGORI_BENGKEL").asString());



                        setJenisKendaraan(jenisList);
                        setMerkKendaraan(merkList);
                        setSpBidangUsaha(bidangList);

                    }
                } else {
                    activity.showInfo(result.get("message").asString());
                }
            }
        });
    }

    private void setJenisKendaraan(List<String> string){
        spJenisKendaraan.setEnabled(false);
        spJenisKendaraan.setItems(string);
    }

    private void setMerkKendaraan(List<String> string){
        spMerkKendaraan.setEnabled(false);
        spMerkKendaraan.setItems(string);
    }

    private void setSpBidangUsaha(List<String> string){
        spBidangUsaha.setEnabled(false);
        spBidangUsaha.setItems(string);
    }

    private void setSpNamaPrincipal(final String principal) {
        activity.newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", "Principal");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3("databengkel"), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    result = result.get("data");
                    principalList.add("--PILIH--");
                    for (int i = 0; i < result.size(); i++) {
                        principalList.add(result.get(i).get("NAMA"));
                    }
                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, principalList.asArray());
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spPrincial.setAdapter(spinnerAdapter);
                    if (!principal.isEmpty()) {
                        for (int in = 0; in < spPrincial.getCount(); in++) {
                            if (spPrincial.getItemAtPosition(in).toString().contains(principal)) {
                                spPrincial.setSelection(in);
                                break;
                            }
                        }
                    }
                }
            }
        });
    }

}
