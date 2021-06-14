package com.rkrzmail.oto.modules.Fragment;

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
import com.rkrzmail.oto.modules.bengkel.AturUser_MainTab_Activity;
import com.rkrzmail.srv.NumberFormatUtils;
import com.rkrzmail.utils.Tools;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.rkrzmail.utils.APIUrls.KARYAWAN;
import static com.rkrzmail.utils.APIUrls.VIEW_MST;
import static com.rkrzmail.utils.ConstUtils.DATA;
import static com.rkrzmail.utils.ConstUtils.REQUEST_DETAIL;

public class AturGajiUser_User_Fragment extends Fragment {

    private View fragmentView;
    private AppActivity activity;
    private Spinner spSatuanGaji, spNamaBank;
    private EditText etUpah, etPotongan, etNoRek, etNamaRek, etTglGaji;
    private Button btnSimpan;
    private CheckBox cbAlpha, cbKomisi;
    private RelativeLayout rlTglGaji;

    private int userID = 0;
    private String satuanGaji = "";
    private String namaBank = "";

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
        etTglGaji = fragmentView.findViewById(R.id.et_tgl_gaji);
    }

    private void setComponent() {
        final Nson data = Nson.readJson(activity.getIntentStringExtra(DATA));
        boolean isUpdate = false;
        final List<String> satuanList = Arrays.asList("--PILIH--", "JAM", "HARI", "BULAN");

        if (!data.asString().isEmpty()) {
            userID = data.get("ID").asInteger();
            isUpdate = true;
            cbKomisi.setChecked(data.get("PERHITUNGAN_KOMISI").asString().equals("Y"));
            cbAlpha.setChecked(data.get("POTONGAN_ALPHA").asString().equals("Y"));
            etUpah.setText(data.get("SATUAN_UPAH").asString());
            etPotongan.setText(data.get("POTONGAN_TERLAMBAT").asString());
            etNamaRek.setText(data.get("NAMA_REKENING").asString());
            etNoRek.setText(data.get("NO_REKENING").asString());
            etTglGaji.setText(data.get("TANGGAL_GAJI").asString());
            activity.setSpinnerOffline(satuanList, spSatuanGaji, data.get("SATUAN_UPAH").asString());
            activity.setSpinnerFromApi(spNamaBank, "nama", "NAMA BANK", VIEW_MST, "BANK_NAME", data.get("NAMA_BANK").asString());
        } else {
            activity.setSpinnerOffline(satuanList, spSatuanGaji, "");
            activity.setSpinnerFromApi(spNamaBank, "nama", "NAMA BANK", VIEW_MST, "BANK_NAME", "");
        }

        etUpah.addTextChangedListener(new NumberFormatUtils().rupiahTextWatcher(etUpah));
        etPotongan.addTextChangedListener(new NumberFormatUtils().rupiahTextWatcher(etPotongan));
        spSatuanGaji.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                satuanGaji = adapterView.getItemAtPosition(i).toString();
                cbAlpha.setEnabled(satuanGaji.equals("BULAN"));
                etPotongan.setEnabled(!satuanGaji.equals("JAM"));
                etTglGaji.setEnabled(satuanGaji.equals("BULAN"));
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
                //if rekening fill gaji must fill
                saveData(finalIsUpdate);
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
                args.put("tanggalGaji", etTglGaji.getText().toString());
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
