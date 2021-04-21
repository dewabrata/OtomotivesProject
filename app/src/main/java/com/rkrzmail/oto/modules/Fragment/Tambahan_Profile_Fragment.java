package com.rkrzmail.oto.modules.Fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.bengkel.ProfileBengkel_Activity;
import com.rkrzmail.srv.MultiSelectionSpinner;
import com.rkrzmail.srv.NumberFormatUtils;
import com.rkrzmail.utils.Tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static android.support.constraint.Constraints.TAG;
import static com.rkrzmail.srv.NumberFormatUtils.clearPercent;
import static com.rkrzmail.srv.NumberFormatUtils.formatOnlyNumber;
import static com.rkrzmail.utils.APIUrls.VIEW_JENIS_KENDARAAN;
import static com.rkrzmail.utils.APIUrls.VIEW_PROFILE;
import static com.rkrzmail.utils.ConstUtils.RP;

/**
 * A simple {@link Fragment} subclass.
 */
public class Tambahan_Profile_Fragment extends Fragment {

    private Spinner spBooking, spFreesimpan;
    private MultiSelectionSpinner spFasilitas, spMerkLkkWajib, spLuarBengkel;
    private EditText etBiayaHomeKm, etBiayaEmergencyKm, etBiayaJemputKm, etBiayaMinLainnya, etBiayaMinDerek, etKapasitas, etBiayaDerekKm, etFreeBiayaSimpan,
            etDp, etOnus, etOffus, etkreditus, etAntrianExpress, etAntrianStandart, etMaxHome, etMaxEmg, etMaxDerek, etMaxJemput;
    private Button btnSimpan;
    private CheckBox cbOnlineBengkel, cbOnlinePelanggan;
    private LinearLayout lyLayanan, lyTambahan, lyEntryMax, lyEntryKm;
    private AppActivity activity;

    private final List<String> fasilitasList = Arrays.asList(
            "RUANG TUNGGU",
            "RUANG TUNGGU AC",
            "WIFI INTERNET",
            "TV",
            "AQUA GRATIS"),
            merkMotorList = new ArrayList<>(), merkMobilList = new ArrayList<>(), allMerkList = new ArrayList<>();

    private final List<String> luarlist = Arrays.asList(
            "HOME",
            "ANTAR - JEMPUT",
            "EMERGENCY",
            "DEREK");

    public Tambahan_Profile_Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_tab_tambahan_bengkel, container, false);
        activity = ((ProfileBengkel_Activity) getActivity());
        initComponent(v);
        validation();
        initListener();
        initData();
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(isVisible()){

        }
    }

    private void initComponent(View v) {
        spMerkLkkWajib = v.findViewById(R.id.sp_jualparonline_tambahan);
        spFasilitas = v.findViewById(R.id.sp_fasilitas_tambahan);
        spBooking = v.findViewById(R.id.sp_booking_tambahan);
        spFreesimpan = v.findViewById(R.id.sp_freesimpanan_tambahan);
        spLuarBengkel = v.findViewById(R.id.sp_luarbengkel);
        etBiayaHomeKm = v.findViewById(R.id.et_homeKm_tambahan);
        etBiayaEmergencyKm = v.findViewById(R.id.et_emgKm_tambahan);
        etBiayaJemputKm = v.findViewById(R.id.et_jemputKm_tambahan);
        etBiayaDerekKm = v.findViewById(R.id.et_derekKm_tambahan);
        etBiayaMinLainnya = v.findViewById(R.id.et_minLainnya_tambahan);
        etBiayaMinDerek = v.findViewById(R.id.et_minDerek_tambahan);
        etKapasitas = v.findViewById(R.id.et_kapasitas_tambahan);
        etFreeBiayaSimpan = v.findViewById(R.id.et_freesimpanan_tambahan);
        etDp = v.findViewById(R.id.et_downpayment_tambahan);
        etOnus = v.findViewById(R.id.et_onUs_tambahan);
        etOffus = v.findViewById(R.id.et_offUs_tambahan);
        etkreditus = v.findViewById(R.id.et_kredit_tambahan);
        etMaxDerek = v.findViewById(R.id.et_maxDerek_tambahan);
        etMaxEmg = v.findViewById(R.id.et_maxEmg_tambahan);
        etMaxHome = v.findViewById(R.id.et_maxHome_tambahan);
        etMaxJemput = v.findViewById(R.id.et_maxJemput_tambahan);
        btnSimpan = v.findViewById(R.id.btn_simpan_tambahan);
        lyEntryKm = v.findViewById(R.id.ly_entryKm_tambahan);
        lyTambahan = v.findViewById(R.id.ly_tambahan);
        cbOnlineBengkel = v.findViewById(R.id.cbPartOnlineBengkel_tambahan);
        cbOnlinePelanggan = v.findViewById(R.id.cbPartOnlinePelanggan_tambahan);
    }

    private void initData() {
        if (activity != null) {
            ((ProfileBengkel_Activity) getActivity()).getDataTambahan(new ProfileBengkel_Activity.TambahanData() {
                @SuppressLint("SetTextI18n")
                @Override
                public void getData(Nson nson) {
                    etMaxHome.setText(nson.get("MAX_RADIUS_HOME").asString());
                    etMaxEmg.setText(nson.get("MAX_RADIUS_EMERGENCY").asString());
                    etMaxJemput.setText(nson.get("MAX_RADIUS_ANTAR_JEMPUT").asString());
                    etMaxDerek.setText(nson.get("MAX_RADIUS_DEREK").asString());
                    etKapasitas.setText(nson.get("KAPASITAS_SIMPAN").asString());
                    etFreeBiayaSimpan.setText(RP + NumberFormatUtils.formatRp(nson.get("BIAYA_PENYIMPANAN").asString()));
                    etDp.setText(nson.get("DP_PERSEN").asString() + " %");
                    etOnus.setText(nson.get("MDR_ON_US").asString());
                    etOffus.setText(nson.get("MDR_OFF_US").asString());
                    etkreditus.setText(nson.get("MDR_KREDIT_CARD").asString());
                    etBiayaDerekKm.setText(nson.get("").asString());
                    etBiayaEmergencyKm.setText(nson.get("BIAYA_TRANSPORT_KM_EMERGENCY").asString());
                    etBiayaHomeKm.setText(nson.get("BIAYA_TRANSPORT_KM_HOME").asString());
                    etBiayaJemputKm.setText(nson.get("BIAYA_TRANSPORT_KM_ANTAR_JEMPUT").asString());
                    etBiayaMinDerek.setText(nson.get("BIAYA_TRANSPORT_MIN_DEREK").asString());
                    etBiayaMinLainnya.setText(nson.get("BIAYA_TRANSPORT_MIN_LAINNYA").asString());
                    cbOnlineBengkel.setChecked(nson.get("JUAL_PART_OL_BENGKEL").asString().equals("Y"));
                    cbOnlinePelanggan.setChecked(nson.get("JUAL_PART_OL_PELANGGAN").asString().equals("Y"));

                    List<String> loadFasilitas = new ArrayList<>(), listLkkWajib = new ArrayList<>();
                    if(nson.get("MERK_LKK_WAJIB").asString().contains(",")){
                        String[] splitLkkWajib = nson.get("MERK_LKK_WAJIB").asString().trim().split(", ");
                        listLkkWajib.addAll(Arrays.asList(splitLkkWajib));
                    }else{
                        listLkkWajib.add(nson.get("MERK_LKK_WAJIB").asString());
                    }

                    if(nson.get("FASILITAS_PELANGGAN").asString().contains(",")){
                        String[] splitFasilitas = nson.get("FASILITAS_PELANGGAN").asString().trim().split(", ");
                        loadFasilitas.addAll(Arrays.asList(splitFasilitas));
                    }else{
                        loadFasilitas.add(nson.get("FASILITAS_PELANGGAN").asString());
                    }

                    if(loadFasilitas.size() > 0){
                        setSpFasilitas(loadFasilitas);
                    }else{
                        setSpFasilitas(null);
                    }
                    if(listLkkWajib.size() > 0){
                        setSpMerkLkkWajib(listLkkWajib);
                    }else{
                        setSpMerkLkkWajib(null);
                    }

                    activity.setSpinnerOffline(Arrays.asList(getResources().getStringArray(R.array.number)), spFreesimpan, nson.get("MAX_FREE_PENYIMPANAN").asString());
                    setSpLuarBengkel();
                }
            });
        }
    }

    private void initListener() {
        etBiayaHomeKm.addTextChangedListener(new NumberFormatUtils().rupiahTextWatcher(etBiayaHomeKm));
        etBiayaDerekKm.addTextChangedListener(new NumberFormatUtils().rupiahTextWatcher(etBiayaDerekKm));
        etBiayaEmergencyKm.addTextChangedListener(new NumberFormatUtils().rupiahTextWatcher(etBiayaEmergencyKm));
        etBiayaJemputKm.addTextChangedListener(new NumberFormatUtils().rupiahTextWatcher(etBiayaJemputKm));
        etBiayaMinDerek.addTextChangedListener(new NumberFormatUtils().rupiahTextWatcher(etBiayaMinDerek));
        etBiayaMinLainnya.addTextChangedListener(new NumberFormatUtils().rupiahTextWatcher(etBiayaMinLainnya));
        etFreeBiayaSimpan.addTextChangedListener(new NumberFormatUtils().rupiahTextWatcher(etFreeBiayaSimpan));
        etOnus.addTextChangedListener(new NumberFormatUtils().percentTextWatcher(etOnus));
        etOffus.addTextChangedListener(new NumberFormatUtils().percentTextWatcher(etOffus));
        etkreditus.addTextChangedListener(new NumberFormatUtils().percentTextWatcher(etkreditus));

        etMaxJemput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                if (text.isEmpty()) return;
                etMaxJemput.removeTextChangedListener(this);
                try {
                    validation();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                etMaxJemput.addTextChangedListener(this);
            }
        });

        etMaxHome.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                if (text.isEmpty()) return;
                etMaxHome.removeTextChangedListener(this);
                try {
                    validation();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                etMaxHome.addTextChangedListener(this);
            }
        });

        etMaxEmg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                if (text.isEmpty()) return;
                etMaxEmg.removeTextChangedListener(this);
                try {
                    validation();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                etMaxEmg.addTextChangedListener(this);
            }
        });

        etMaxDerek.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                if (text.isEmpty()) return;
                etMaxDerek.removeTextChangedListener(this);
                try {
                    validation();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                etMaxDerek.addTextChangedListener(this);
            }
        });

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });

    }

    private void setSpFasilitas(List<String> loadItem) {
        spFasilitas.setItems(fasilitasList, loadItem);
        spFasilitas.setListener(new MultiSelectionSpinner.OnMultipleItemsSelectedListener() {
            @Override
            public void selectedIndices(List<Integer> indices) {

            }

            @Override
            public void selectedStrings(List<String> strings) {

            }
        });
    }

    private void setSpLuarBengkel() {
        spLuarBengkel.setItems(luarlist);
        spLuarBengkel.setListener(new MultiSelectionSpinner.OnMultipleItemsSelectedListener() {
            @Override
            public void selectedIndices(List<Integer> indices) {

            }

            @Override
            public void selectedStrings(List<String> strings) {

            }
        });
    }

    private void setSpMerkLkkWajib(final List<String> loadItem) {
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("CID", "kosong");
                args.put("flag", "Merk");
                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(VIEW_JENIS_KENDARAAN), args));
            }

            @Override
            public void runUI() {
                for (int i = 0; i < result.get("data").size(); i++) {
                    if (result.get("data").get(i).get("TYPE").asString().equalsIgnoreCase("MOTOR")) {
                        merkMotorList.add(result.get("data").get(i).get("MERK") + " - " + result.get("data").get(i).get("TYPE"));
                    } else if (result.get("data").get(i).get("TYPE").asString().equalsIgnoreCase("MOBIL")) {
                        merkMobilList.add(result.get("data").get(i).get("MERK") + " - " + result.get("data").get(i).get("TYPE"));
                    }
                }

                try {
                    allMerkList.addAll(merkMotorList);
                    allMerkList.addAll(merkMobilList);
                    Log.d(TAG, "runUI: " + allMerkList);
                    spMerkLkkWajib.setItems(allMerkList, loadItem);
//                    if (count > 0) {
//                        spJuallPartOnline.setItems(allMerkList);
//                        //spMerkKendaraan.setSelection(allMerkList, false);
//                    } else {
//                        spJuallPartOnline.setItems(isKategori ? merkMotorList : merkMobilList);
//                        //spMerkKendaraan.setSelection(isKategori ? merkMotorList : merkMobilList, false);
//                    }

                    spMerkLkkWajib.setListener(new MultiSelectionSpinner.OnMultipleItemsSelectedListener() {
                        @Override
                        public void selectedIndices(List<Integer> indices) {

                        }

                        @Override
                        public void selectedStrings(List<String> strings) {

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    activity.showInfo("Perlu di Muat Ulang");
                }
            }
        });
    }

    private void saveData() {
        MessageMsg.showProsesBar(getActivity(), new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();

                String spFree = spFreesimpan.getSelectedItem().toString();
                if (spFree.contains("--PILIH--")) {
                    spFree = spFree.replace("--PILIH--", "");
                }

                args.put("action", "update");
                args.put("kategori", "TAMBAHAN");
                args.put("fasilitasPelanggan", spFasilitas.getSelectedItemsAsString());
                args.put("luarBengkel", spLuarBengkel.getSelectedItemsAsString());
                args.put("booking", spBooking.getSelectedItem().toString().toUpperCase());
                args.put("maxRadiusHome", etMaxHome.getText().toString());
                args.put("maxRadiusEmg", etMaxEmg.getText().toString());
                args.put("maxRadiusJemput", etMaxJemput.getText().toString());
                args.put("maxRadiusDerek", etMaxDerek.getText().toString());
                args.put("biayaMinDerek", formatOnlyNumber(etBiayaMinDerek.getText().toString()));
                args.put("biayaMinLainnya", formatOnlyNumber(etBiayaMinLainnya.getText().toString()));
                args.put("biayaKmHome", formatOnlyNumber(etBiayaHomeKm.getText().toString()));
                args.put("biayaKmEmg", formatOnlyNumber(etBiayaEmergencyKm.getText().toString()));
                args.put("biayaKmJemput", formatOnlyNumber(etBiayaJemputKm.getText().toString()));
                args.put("biayaKmDerek", formatOnlyNumber(etBiayaDerekKm.getText().toString()));
                args.put("kapasitasSimpan", etKapasitas.getText().toString().toUpperCase());
                args.put("freeSimpan", spFree);
                args.put("freeBiaya", formatOnlyNumber(etFreeBiayaSimpan.getText().toString()));
                args.put("dpPersen", etDp.getText().toString().toUpperCase());
                args.put("mdrOnUs", clearPercent(etOnus.getText().toString().toUpperCase()));
                args.put("mdrOffUs", clearPercent(etOffus.getText().toString().toUpperCase()));
                args.put("mdrKredit", clearPercent(etkreditus.getText().toString().toUpperCase()));
                args.put("jualPartOlBengkel", cbOnlineBengkel.isChecked() ? "Y" : "N");
                args.put("jualPartOlPelanggan", cbOnlinePelanggan.isChecked() ? "Y" : "N");
                args.put("merkLkkWajib", spMerkLkkWajib.getSelectedItem().toString());

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

    private void validation() {
        spBooking.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String item = adapterView.getItemAtPosition(i).toString();
                if (item.equalsIgnoreCase("TIDAK")) {
                    Tools.setViewAndChildrenEnabled(lyTambahan, false);
                } else {
                    Tools.setViewAndChildrenEnabled(lyTambahan, true);
                    etBiayaMinLainnya.setEnabled(false);
                    etBiayaMinDerek.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if (!etMaxDerek.getText().toString().isEmpty() && (!etMaxEmg.getText().toString().isEmpty() ||
                !etMaxJemput.getText().toString().isEmpty() || !etMaxHome.getText().toString().isEmpty())) {
            etBiayaMinLainnya.setEnabled(true);
            etBiayaMinDerek.setEnabled(true);
        } else if (!etMaxEmg.getText().toString().isEmpty() || !etMaxJemput.getText().toString().isEmpty() ||
                !etMaxHome.getText().toString().isEmpty()) {
            etBiayaMinLainnya.setEnabled(true);
            etBiayaMinDerek.setEnabled(false);
        } else if (!etMaxDerek.getText().toString().isEmpty()) {
            etBiayaMinLainnya.setEnabled(false);
            etBiayaMinDerek.setEnabled(true);
        }
    }

}
