package com.rkrzmail.oto.modules.Fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
import com.rkrzmail.oto.modules.bengkel.ProfileBengkel_Activity;
import com.rkrzmail.srv.MultiSelectionSpinner;
import com.rkrzmail.srv.NumberFormatUtils;
import com.rkrzmail.utils.Tools;

import java.text.NumberFormat;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static com.rkrzmail.srv.NumberFormatUtils.clearPercent;
import static com.rkrzmail.srv.NumberFormatUtils.getPercentFilter;
import static com.rkrzmail.srv.NumberFormatUtils.setPercentage;
import static com.rkrzmail.utils.APIUrls.VIEW_PROFILE;

/**
 * A simple {@link Fragment} subclass.
 */
public class TabTambahan_Fragment extends Fragment {

    private Spinner spFasilitas, spBooking, spFreesimpan;
    private EditText etHomeKm, etEmergencyKm, etJemputKm, etMinLainnya, etMinDerek, etKapasitas, etDerekKm, etFreeBiaya,
            etDp,etOnus,etOffus,etkreditus,etAntrianExpress,etAntrianStandart;
    private Button btnSimpan;
    private TextView tvAntrianExpress, tvAntrianStandart;
    private CheckBox cbHome, cbJemput, cbEmg, cbDerek,cbOnlineBengkel,cbOnlinePelanggan;
    private LinearLayout lyLayanan, lyTambahan, lyEntryMax, lyEntryKm;
    private MultiSelectionSpinner spLayanan;
    private AppActivity activity;

    public TabTambahan_Fragment() {
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
        return v;
    }

    private void initComponent(View v) {
        spFasilitas = v.findViewById(R.id.sp_fasilitas_tambahan);
        spBooking = v.findViewById(R.id.sp_booking_tambahan);
        spFreesimpan = v.findViewById(R.id.sp_freesimpanan_tambahan);
        etHomeKm = v.findViewById(R.id.et_homeKm_tambahan);
        etEmergencyKm = v.findViewById(R.id.et_emgKm_tambahan);
        etJemputKm = v.findViewById(R.id.et_jemputKm_tambahan);
        etDerekKm = v.findViewById(R.id.et_derekKm_tambahan);
        etMinLainnya = v.findViewById(R.id.et_minLainnya_tambahan);
        etMinDerek = v.findViewById(R.id.et_minDerek_tambahan);
        etKapasitas = v.findViewById(R.id.et_kapasitas_tambahan);
        etFreeBiaya = v.findViewById(R.id.et_freesimpanan_tambahan);
        etDp = v.findViewById(R.id.et_downpayment_tambahan);
        etOnus = v.findViewById(R.id.et_onUs_tambahan);
        etOffus = v.findViewById(R.id.et_offUs_tambahan);
        etkreditus = v.findViewById(R.id.et_kredit_tambahan);
        etAntrianExpress = v.findViewById(R.id.et_maxAntrianExpress);
        etAntrianStandart = v.findViewById(R.id.et_maxAntrianStandart);
        btnSimpan = v.findViewById(R.id.btn_simpan_tambahan);
        lyEntryKm = v.findViewById(R.id.ly_entryKm_tambahan);
        lyEntryMax = v.findViewById(R.id.ly_entryMax_tambahan);
        lyTambahan = v.findViewById(R.id.ly_tambahan);
        cbDerek = v.findViewById(R.id.cbDerek);
        cbEmg = v.findViewById(R.id.cbEmg);
        cbHome = v.findViewById(R.id.cbHome);
        cbJemput = v.findViewById(R.id.cbJemput);
        cbOnlineBengkel = v.findViewById(R.id.cbPartOnlineBengkel_tambahan);
        cbOnlinePelanggan = v.findViewById(R.id.cbPartOnlinePelanggan_tambahan);
        tvAntrianExpress = v.findViewById(R.id.ic_AntrianExpress_tambahan);
        tvAntrianStandart = v.findViewById(R.id.ic_AntrianStandart_tambahan);
    }

    private void initListener(){
        cbDerek.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                validation();
            }
        });

        cbHome.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                validation();
            }
        });

        cbJemput.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                validation();
            }
        });

        cbEmg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                validation();
            }
        });

        tvAntrianExpress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.getTimePickerDialogTextView(getContext(), etAntrianExpress);
            }
        });

        tvAntrianStandart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.getTimePickerDialogTextView(getContext(), etAntrianStandart);
            }
        });

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });
        etOnus.addTextChangedListener(new TextWatcher() {
            int prevLength = 0; // detected keyEvent action delete

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                prevLength = charSequence.length();
            }


            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {


            }

            @SuppressLint("SetTextI18n")
            @Override
            public void afterTextChanged(Editable editable) {
                String text = editable.toString();
                if (text.isEmpty()) return;
                etOnus.removeTextChangedListener(this);

                try {
                    text = new NumberFormatUtils().formatOnlyNumber(text);
                    double percentValue = Double.parseDouble(text.isEmpty() ? "0" : text) / 1000;

                    NumberFormat percentageFormat = NumberFormat.getPercentInstance();
                    percentageFormat.setMinimumFractionDigits(1);
                    String percent = percentageFormat.format(percentValue);

                    InputFilter[] filterArray = new InputFilter[1];
                    filterArray[0] = new InputFilter.LengthFilter(6);

                    etOnus.setFilters(filterArray);
                    etOnus.setText(percent);
                    etOnus.setSelection(percent.length() - 1);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                etOnus.addTextChangedListener(this);
            }
        });

        etOffus.addTextChangedListener(new TextWatcher() {
            int prevLength = 0; // detected keyEvent action delete

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                prevLength = charSequence.length();
            }


            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {


            }

            @SuppressLint("SetTextI18n")
            @Override
            public void afterTextChanged(Editable editable) {
                String text = editable.toString();
                if (text.isEmpty()) return;
                etOffus.removeTextChangedListener(this);

                try {
                    text = new NumberFormatUtils().formatOnlyNumber(text);
                    double percentValue = Double.parseDouble(text.isEmpty() ? "0" : text) / 1000;

                    NumberFormat percentageFormat = NumberFormat.getPercentInstance();
                    percentageFormat.setMinimumFractionDigits(1);
                    String percent = percentageFormat.format(percentValue);

                    InputFilter[] filterArray = new InputFilter[1];
                    filterArray[0] = new InputFilter.LengthFilter(6);

                    etOffus.setFilters(filterArray);
                    etOffus.setText(percent);
                    etOffus.setSelection(percent.length() - 1);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                etOffus.addTextChangedListener(this);
            }
        });

        etkreditus.addTextChangedListener(new TextWatcher() {
            int prevLength = 0; // detected keyEvent action delete

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                prevLength = charSequence.length();
            }


            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {


            }

            @SuppressLint("SetTextI18n")
            @Override
            public void afterTextChanged(Editable editable) {
                String text = editable.toString();
                if (text.isEmpty()) return;
                etOffus.removeTextChangedListener(this);

                try {
                    text = new NumberFormatUtils().formatOnlyNumber(text);
                    double percentValue = Double.parseDouble(text.isEmpty() ? "0" : text) / 1000;

                    NumberFormat percentageFormat = NumberFormat.getPercentInstance();
                    percentageFormat.setMinimumFractionDigits(1);
                    String percent = percentageFormat.format(percentValue);

                    InputFilter[] filterArray = new InputFilter[1];
                    filterArray[0] = new InputFilter.LengthFilter(6);

                    etkreditus.setFilters(filterArray);
                    etkreditus.setText(percent);
                    etkreditus.setSelection(percent.length() - 1);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                etkreditus.addTextChangedListener(this);
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
                args.put("kategori", "TAMBAHAN");
                //args.put("fasilitasPelanggan", spFasilitas.getSelectedItem().toString());
                //luarbengkel
                args.put("booking", spBooking.getSelectedItem().toString().toUpperCase());
                args.put("maxRadiusHome", cbHome.isChecked() ? "Y" : "N");
                args.put("maxRadiusEmg", cbEmg.isChecked() ? "Y" : "N");
                args.put("maxRadiusJemput", cbJemput.isChecked() ? "Y" : "N");
                args.put("maxRadiusDerek", cbDerek.isChecked() ? "Y" : "N");
                args.put("biayaMinDerek", etMinDerek.getText().toString().toUpperCase());
                args.put("biayaMinLainnya", etMinLainnya.getText().toString().toUpperCase());
                args.put("biayaKmHome", etHomeKm.getText().toString().toUpperCase());
                args.put("biayaKmEmg", etEmergencyKm.getText().toString().toUpperCase());
                args.put("biayaKmJemput", etJemputKm.getText().toString().toUpperCase());
                args.put("biayaKmDerek", etDerekKm.getText().toString().toUpperCase());
                args.put("kapasitasSimpan", etKapasitas.getText().toString().toUpperCase());
                args.put("freeSimpan", spFreesimpan.getSelectedItem().toString());
                args.put("freeBiaya", etFreeBiaya.getText().toString().toUpperCase());
                args.put("dpPersen", etDp.getText().toString().toUpperCase());
                args.put("mdrOnUs", clearPercent(etOnus.getText().toString().toUpperCase()));
                args.put("mdrOffUs", clearPercent(etOffus.getText().toString().toUpperCase()));
                args.put("mdrKredit", clearPercent(etkreditus.getText().toString().toUpperCase()));
                args.put("antrianExpres", etAntrianExpress.getText().toString().toUpperCase());
                args.put("antrianStandart", etAntrianStandart.getText().toString().toUpperCase());
                args.put("jualPartOlBengkel", cbOnlineBengkel.isChecked() ? "Y" : "N");
                args.put("jualPartOlPelanggan", cbOnlinePelanggan.isChecked() ? "Y" : "N");

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
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if(cbDerek.isChecked() && (cbEmg.isChecked() || cbJemput.isChecked() || cbHome.isChecked())){
            etMinLainnya.setEnabled(true);
            etMinDerek.setEnabled(true);
        }else if (cbHome.isChecked() || cbJemput.isChecked() || cbEmg.isChecked()){
            etMinLainnya.setEnabled(true);
            etMinDerek.setEnabled(false);
        }else if (cbDerek.isChecked()){
            etMinLainnya.setEnabled(false);
            etMinDerek.setEnabled(true);
        }
    }
}
