package com.rkrzmail.oto.modules.Fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.naa.data.Nson;
import com.naa.utils.InternetX;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.AppApplication;
import com.rkrzmail.oto.R;
import com.rkrzmail.oto.modules.Adapter.AturUser_MainTab_Activity;
import com.rkrzmail.srv.NumberFormatUtils;
import com.rkrzmail.utils.Tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.rkrzmail.utils.APIUrls.KARYAWAN;
import static com.rkrzmail.utils.APIUrls.VIEW_MST;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.REQUEST_DETAIL;
import static com.rkrzmail.utils.ConstUtils.RP;

public class AturGajiUser_User_Fragment extends Fragment {

    private View fragmentView;
    private AppActivity activity;
    private Spinner spSatuanGaji, spNamaBank, spPeriodeGajian, spTglGaji, spMaxJamMinggu, spMinWaktuTerlambat;
    private EditText etUpah, etPotongan, etNoRek, etNamaRek, etLemburPerJam;
    private Button btnSimpan;
    private CheckBox cbAlpha, cbKomisi;
    private RelativeLayout rlTglGaji;

    private int userID = 0;
    private String satuanGaji = "";
    private String namaBank = "";
    private String tglGaji = "", maxJamMinggu = "", minMenitTerlambat = "", periodeGaji = "";

    public AturGajiUser_User_Fragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_atur_gaji_user, container, false);
        activity = (AturUser_MainTab_Activity) getActivity();
        initHideToolbar();
        initComponent();
        setComponent();
        return fragmentView;
    }

    private void initHideToolbar() {
        AppBarLayout appBarLayout = fragmentView.findViewById(R.id.appbar);
        appBarLayout.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isVisible()) {

        }
    }

    private void initComponent() {
        etNamaRek = fragmentView.findViewById(R.id.et_nama_rekening);
        etNoRek = fragmentView.findViewById(R.id.et_no_rekening);
        spNamaBank = fragmentView.findViewById(R.id.sp_nama_bank);
        spSatuanGaji = fragmentView.findViewById(R.id.sp_satuan_gaji);
        btnSimpan = fragmentView.findViewById(R.id.btn_simpan);
        cbAlpha = fragmentView.findViewById(R.id.cb_potongan_alpha);
        etPotongan = fragmentView.findViewById(R.id.et_potongan_jam);
        etUpah = fragmentView.findViewById(R.id.et_upah_satuan);
        cbKomisi = fragmentView.findViewById(R.id.cb_perhitungan_komisi);
        spPeriodeGajian = fragmentView.findViewById(R.id.sp_periode_gajian);
        spTglGaji = fragmentView.findViewById(R.id.sp_tgl_penggajian);
        spMaxJamMinggu = fragmentView.findViewById(R.id.sp_max_jam);
        spMinWaktuTerlambat = fragmentView.findViewById(R.id.sp_min_terlambat);
        etLemburPerJam = fragmentView.findViewById(R.id.et_lembur);
    }

    @SuppressLint("SetTextI18n")
    private void setComponent() {
        final Nson data = Nson.readJson(activity.getIntentStringExtra(DATA));
        boolean isUpdate = false;

        if (!data.asString().isEmpty()) {
            isUpdate = true;
            userID = data.get("ID").asInteger();
            tglGaji = data.get("TANGGAL_GAJI").asString();
            maxJamMinggu = data.get("MAX_JAM_MINGGU").asString();
            minMenitTerlambat = data.get("MIN_MENIT_TERLAMBAT").asString();
            periodeGaji = data.get("PERIODE_GAJIAN").asString();
            satuanGaji = data.get("SATUAN_UPAH").asString();
            namaBank = data.get("NAMA_BANK").asString();

            cbKomisi.setChecked(data.get("PERHITUNGAN_KOMISI").asString().equals("Y"));
            cbAlpha.setChecked(data.get("POTONGAN_ALPHA").asString().equals("Y"));
            etLemburPerJam.setText(RP + NumberFormatUtils.formatRp(data.get("LEMBUR_JAM_RP").asString()));
            etUpah.setText(RP + NumberFormatUtils.formatRp(data.get("UPAH_SATUAN").asString()));
            etPotongan.setText(RP + NumberFormatUtils.formatRp(data.get("POTONGAN_TERLAMBAT").asString()));
            etNamaRek.setText(data.get("NAMA_REKENING").asString());
            etNoRek.setText(data.get("NO_REKENING").asString());

        }

        setSpView();

        etLemburPerJam.addTextChangedListener(new NumberFormatUtils().rupiahTextWatcher(etLemburPerJam));
        etUpah.addTextChangedListener(new NumberFormatUtils().rupiahTextWatcher(etUpah));
        etPotongan.addTextChangedListener(new NumberFormatUtils().rupiahTextWatcher(etPotongan));

        spNamaBank.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                namaBank = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        final boolean finalIsUpdate = isUpdate;
        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData(finalIsUpdate);
            }
        });
    }

    private void setSpView() {
        List<String> angkaList = new ArrayList<>();
        List<String> tglGajiList = new ArrayList<>();
        tglGajiList.add("--PILIH--");
        for (int i = 1; i <= 60; i++) {
            if (i <= 28)
                tglGajiList.add(String.valueOf(i));
            angkaList.add(String.valueOf(i));
        }

        activity.setSpinnerOffline(angkaList, spMaxJamMinggu, maxJamMinggu);
        activity.setSpinnerOffline(angkaList, spMinWaktuTerlambat, minMenitTerlambat);
        activity.setSpinnerOffline(tglGajiList, spTglGaji, tglGaji);
        activity.setSpinnerOffline(Arrays.asList("HARI", "MINGGU", "BULAN"), spPeriodeGajian, tglGaji);
        activity.setSpinnerOffline(Arrays.asList("--PILIH--", "JAM", "HARI", "BULAN"), spSatuanGaji, satuanGaji);
        activity.setSpinnerFromApi(spNamaBank, "nama", "NAMA BANK", VIEW_MST, "BANK_NAME", namaBank);

        spSatuanGaji.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                satuanGaji = adapterView.getItemAtPosition(i).toString();
                cbAlpha.setEnabled(satuanGaji.equals("BULAN"));
                etPotongan.setEnabled(!satuanGaji.equals("JAM"));

                if (satuanGaji.equals("BULAM")) {
                    cbAlpha.setChecked(false);
                }
                if (satuanGaji.equals("JAM")) {
                    etPotongan.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spPeriodeGajian.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                Tools.setViewAndChildrenEnabled(activity.findView(fragmentView, R.id.rl_tgl_gaji, RelativeLayout.class), item.equals("BULAN"));
                if(!item.equals("BULAN")){
                    spTglGaji.setSelection(0);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void saveData(final boolean isUpdate) {
        activity.newProses(new Messagebox.DoubleRunnable() {
            Nson result;

            @Override
            public void run() {
                Map<String, String> args = AppApplication.getInstance().getArgsData();
                args.put("action", isUpdate ? "update" : "add");
                args.put("satuanUpah", satuanGaji);
                args.put("upahSatuan", etUpah.getText().toString());
                args.put("potonganAlpha", cbAlpha.isChecked() ? "Y" : "N");
                args.put("potonganTerlambat", NumberFormatUtils.formatOnlyNumber(etPotongan.getText().toString()));
                args.put("namaRekening", etNoRek.getText().toString());
                args.put("namaBank", namaBank);
                args.put("noRekening", etNoRek.getText().toString());
                args.put("idUser", String.valueOf(userID));
                args.put("perhitunganKomisi", cbKomisi.isChecked() ? "Y" : "N");
                args.put("jenisTab", "UPAH");

                String tglGaji = spTglGaji.getSelectedItem().toString();
                args.put("tanggalGaji", tglGaji.equals("--PILIH--") ? "" : tglGaji);
                args.put("periodeGajian", spPeriodeGajian.getSelectedItem().toString());
                args.put("lemburPerJam", NumberFormatUtils.formatOnlyNumber(etLemburPerJam.getText().toString()));
                args.put("maxJamMinggu", spMaxJamMinggu.getSelectedItem().toString());
                args.put("minMenitTerlambat", spMinWaktuTerlambat.getSelectedItem().toString());

                result = Nson.readJson(InternetX.postHttpConnection(AppApplication.getBaseUrlV3(KARYAWAN), args));
            }

            @Override
            public void runUI() {
                if (result.get("status").asString().equalsIgnoreCase("OK")) {
                    activity.showSuccess("BERHASIL MENYIMPAN AKTIVITAS");
                } else {
                    activity.showError(result.get("message").asString());
                }
            }
        });
    }

    public void viewFocus(final View view) {
        view.post(new Runnable() {
            @Override
            public void run() {
                view.setFocusable(true);
                view.requestFocusFromTouch();
                view.requestFocus();
                view.performClick();
            }
        });
    }

    public void setErrorSpinner(Spinner errorSpinner, String errorMessage) {
        TextView tvError = (TextView) errorSpinner.getSelectedView();
        tvError.setError(errorMessage);
        viewFocus(tvError);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_DETAIL) {

        }
    }
}
