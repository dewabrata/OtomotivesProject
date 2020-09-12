package com.rkrzmail.oto.modules.profile;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.naa.data.UtilityAndroid;
import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.AppActivity;
import com.rkrzmail.oto.R;
import com.rkrzmail.utils.SpinnerNoDefault;
import com.rkrzmail.utils.Tools;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static com.naa.utils.InternetX.setSetting;

/**
 * A simple {@link Fragment} subclass.
 */
public class TabSchedule_Fragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, AdapterView.OnItemSelectedListener {

    private CheckBox cbSeninJumat, cbJumat, cbSabtu, cbMingu, cbLibur;
    private TextView tvBukaSJ, tvTutupSJ, tvBukaJ, tvTutupJ, tvBukaSab, tvTutupSab, tvBukaM, tvTutupM, tvBukaL, tvTutupL;
    private EditText etBukaSJ, etTutupSJ, etBukaJ, etTutupJ, etBukaSab, etTutupSab, etBukaM, etTutupM, etBukaL, etTutupL;
    private Button btnSimpan;
    private Spinner spTipeLayanan;
    private LinearLayout lySj, lyJ, lySab, lyM, lyLib, frameSchedule;

    public TabSchedule_Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_tab_schedule_, container, false);
        initComponent(v);
        return v;
    }

    private void initComponent(View view) {
        cbSeninJumat = view.findViewById(R.id.cb_seninJumat_schedule);
        cbJumat = view.findViewById(R.id.cb_jumat_schedule);
        cbSabtu = view.findViewById(R.id.cb_sabtu_schedule);
        cbMingu = view.findViewById(R.id.cb_minggu_schedule);
        cbLibur = view.findViewById(R.id.cb_libur_schedule);
        tvBukaSJ = view.findViewById(R.id.tv_jamBukaSJ_schedule);
        tvTutupSJ = view.findViewById(R.id.tv_jamTutupSJ_schedule);
        tvBukaJ = view.findViewById(R.id.tv_jamBukaJ_schedule);
        tvTutupJ = view.findViewById(R.id.tv_jamTutupJ_schedule);
        tvBukaSab = view.findViewById(R.id.tv_jamBukaSab_schedule);
        tvTutupSab = view.findViewById(R.id.tv_jamTutupSab_schedule);
        tvBukaM = view.findViewById(R.id.tv_jamBukaM_schedule);
        tvTutupM = view.findViewById(R.id.tv_jamTutupM_schedule);
        tvBukaL = view.findViewById(R.id.tv_jamBukaL_schedule);
        tvTutupL = view.findViewById(R.id.tv_jamTutupL_schedule);
        etBukaSJ = view.findViewById(R.id.et_jamBukaSJ_schedule);
        etTutupSJ = view.findViewById(R.id.et_jamTutupSJ_schedule);
        etBukaJ = view.findViewById(R.id.et_jamBukaJ_schedule);
        etTutupJ = view.findViewById(R.id.et_jamTutupJ_schedule);
        etBukaSab = view.findViewById(R.id.et_jamBukaSab_schedule);
        etTutupSab = view.findViewById(R.id.et_jamTutupSab_schedule);
        etBukaM = view.findViewById(R.id.et_jamBukaM_schedule);
        etTutupM = view.findViewById(R.id.et_jamTutupM_schedule);
        etBukaL = view.findViewById(R.id.et_jamBukaL_schedule);
        etTutupL = view.findViewById(R.id.et_jamTutupL_schedule);
        btnSimpan = view.findViewById(R.id.btn_simpan_schedule);
        lySj = view.findViewById(R.id.ly_sj_schedule);
        lyJ = view.findViewById(R.id.ly_j_schedule);
        lySab = view.findViewById(R.id.ly_sab_schedule);
        lyM = view.findViewById(R.id.ly_m_schedule);
        lyLib = view.findViewById(R.id.ly_l_schedule);
        frameSchedule = view.findViewById(R.id.frame_schedule);
        spTipeLayanan = view.findViewById(R.id.sp_tipe_schedule);

        listener();

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View view) {
                timeValidation("");
                Log.d("jamjadwal", ((ProfileBengkel_Activity) Objects.requireNonNull(getActivity())).getSetting("jambuka" + "OPERASIONAL"));
                Log.d("jamjadwal", ((ProfileBengkel_Activity) getActivity()).getSetting("jamtutup" + "OPERASIONAL"));

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

    private void listener() {
        Tools.setViewAndChildrenEnabled(lySj, false);
        Tools.setViewAndChildrenEnabled(lyJ, false);
        Tools.setViewAndChildrenEnabled(lySab, false);
        Tools.setViewAndChildrenEnabled(lyM, false);
        Tools.setViewAndChildrenEnabled(lyLib, false);

        cbSeninJumat.setOnCheckedChangeListener(this);
        cbJumat.setOnCheckedChangeListener(this);
        cbSabtu.setOnCheckedChangeListener(this);
        cbMingu.setOnCheckedChangeListener(this);
        cbLibur.setOnCheckedChangeListener(this);
        tvBukaSJ.setOnClickListener(this);
        tvTutupSJ.setOnClickListener(this);
        tvBukaJ.setOnClickListener(this);
        tvTutupJ.setOnClickListener(this);
        tvBukaSab.setOnClickListener(this);
        tvTutupSab.setOnClickListener(this);
        tvBukaM.setOnClickListener(this);
        tvTutupM.setOnClickListener(this);
        tvBukaL.setOnClickListener(this);
        tvTutupL.setOnClickListener(this);

        String[] tipe = getResources().getStringArray(R.array.tipe_schedule);
        ((ProfileBengkel_Activity) getActivity()).spinnerNoDefaultOffline(spTipeLayanan, tipe);
        spTipeLayanan.setOnItemSelectedListener(this);
    }

    private void timeValidation(String items) {
        ArrayList<EditText> etBuka = new ArrayList<>();
        ArrayList<EditText> etTutup = new ArrayList<>();
        etBuka.add(etBukaSJ);
        etTutup.add(etTutupSJ);
        etBuka.add(etBukaJ);
        etTutup.add(etTutupJ);
        etBuka.add(etBukaSab);
        etTutup.add(etTutupSab);
        etBuka.add(etBukaM);
        etTutup.add(etTutupM);
        etBuka.add(etBukaL);
        etTutup.add(etTutupL);
        for (int i = 0; i < etBuka.size(); i++) {
            for (int j = 0; j < etTutup.size(); j++) {
                if (etBuka.get(i).isEnabled() && etTutup.get(j).isEnabled()) {
                    String buka = etBuka.get(i).getText().toString();
                    String tutup = etTutup.get(j).getText().toString();
                    ((ProfileBengkel_Activity) getActivity()).setSetting("jambuka" + items, buka);
                    ((ProfileBengkel_Activity) getActivity()).setSetting("jamtutup" + items, tutup);
                    try {
                        @SuppressLint("SimpleDateFormat") Date jamBuka = new SimpleDateFormat("HH:mm").parse(buka);
                        @SuppressLint("SimpleDateFormat") Date jamTutup = new SimpleDateFormat("HH:mm").parse(tutup);
                        if (!jamTutup.after(jamBuka)) {
                            ((ProfileBengkel_Activity) getActivity()).showInfo("Jam Buka Tidak Sesuai / Jam Tutup Tidak Sesuai");
                            etBuka.get(i).requestFocus();
                            etTutup.get(j).requestFocus();
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_jamBukaSJ_schedule:
                AppActivity.getTimePickerDialogTextView(getContext(), etBukaSJ);
                break;
            case R.id.tv_jamTutupSJ_schedule:
                AppActivity.getTimePickerDialogTextView(getContext(), etTutupSJ);
                break;
            case R.id.tv_jamBukaJ_schedule:
                AppActivity.getTimePickerDialogTextView(getContext(), etBukaJ);
                break;
            case R.id.tv_jamTutupJ_schedule:
                AppActivity.getTimePickerDialogTextView(getContext(), etTutupJ);
                break;
            case R.id.tv_jamBukaSab_schedule:
                AppActivity.getTimePickerDialogTextView(getContext(), etBukaSab);
                break;
            case R.id.tv_jamTutupSab_schedule:
                AppActivity.getTimePickerDialogTextView(getContext(), etTutupSab);
                break;
            case R.id.tv_jamBukaM_schedule:
                AppActivity.getTimePickerDialogTextView(getContext(), etBukaM);
                break;
            case R.id.tv_jamTutupM_schedule:
                AppActivity.getTimePickerDialogTextView(getContext(), etTutupM);
                break;
            case R.id.tv_jamBukaL_schedule:
                AppActivity.getTimePickerDialogTextView(getContext(), etBukaL);
                break;
            case R.id.tv_jamTutupL_schedule:
                AppActivity.getTimePickerDialogTextView(getContext(), etTutupL);
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.cb_seninJumat_schedule:
                if (b) {
                    Tools.setViewAndChildrenEnabled(lySj, true);
                } else {
                    Tools.setViewAndChildrenEnabled(lySj, false);
                }
                break;
            case R.id.cb_jumat_schedule:
                if (b) {
                    Tools.setViewAndChildrenEnabled(lyJ, true);
                } else {
                    Tools.setViewAndChildrenEnabled(lyJ, false);
                }
                break;
            case R.id.cb_sabtu_schedule:
                if (b) {
                    Tools.setViewAndChildrenEnabled(lySab, true);
                } else {
                    Tools.setViewAndChildrenEnabled(lySab, false);
                }
                break;
            case R.id.cb_minggu_schedule:
                if (b) {
                    Tools.setViewAndChildrenEnabled(lyM, true);
                } else {
                    Tools.setViewAndChildrenEnabled(lyM, false);
                }
                break;
            case R.id.cb_libur_schedule:
                if (b) {
                    Tools.setViewAndChildrenEnabled(lyLib, true);
                } else {
                    Tools.setViewAndChildrenEnabled(lyLib, false);
                }
                break;
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String item = adapterView.getItemAtPosition(i).toString();
        timeValidation(item);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
