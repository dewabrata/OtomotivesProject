package com.rkrzmail.oto.modules.Fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
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

import com.naa.utils.MessageMsg;
import com.naa.utils.Messagebox;
import com.rkrzmail.oto.R;
import com.rkrzmail.srv.MultiSelectionSpinner;
import com.rkrzmail.utils.Tools;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class TabTambahan_Fragment extends Fragment {

    private Spinner spFasilitas, spBooking;
    private EditText etHomeKm, etEmergencyKm, etJemputKm, etMinLainnya, etMinDerek, etKapasitas, etMaxHari, etBiaya;
    private Button btnSimpan;
    private CheckBox cbHome, cbJemput, cbEmg, cbDerek;
    private LinearLayout lyLayanan, lyTambahan, lyEntryMax, lyEntryKm;
    private MultiSelectionSpinner spLayanan;

    public TabTambahan_Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_tab_tambahan_bengkel, container, false);
        initComponent(v);
        validation();
        return v;
    }

    private void initComponent(View v) {
        spFasilitas = v.findViewById(R.id.sp_fasilitas_tambahan);
        spBooking = v.findViewById(R.id.sp_booking_tambahan);
        etHomeKm = v.findViewById(R.id.et_homeKm_tambahan);
        etEmergencyKm = v.findViewById(R.id.et_emgKm_tambahan);
        etJemputKm = v.findViewById(R.id.et_jemputKm_tambahan);
        etMinLainnya = v.findViewById(R.id.et_minLainnya_tambahan);
        etMinDerek = v.findViewById(R.id.et_minDerek_tambahan);
        etKapasitas = v.findViewById(R.id.et_kapasitas_tambahan);
        btnSimpan = v.findViewById(R.id.btn_simpan_tambahan);
        lyEntryKm = v.findViewById(R.id.ly_entryKm_tambahan);
        lyEntryMax = v.findViewById(R.id.ly_entryMax_tambahan);
        lyTambahan = v.findViewById(R.id.ly_tambahan);
        cbDerek= v.findViewById(R.id.cbDerek);
        cbEmg= v.findViewById(R.id.cbEmg);
        cbHome= v.findViewById(R.id.cbHome);
        cbJemput= v.findViewById(R.id.cbJemput);


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
